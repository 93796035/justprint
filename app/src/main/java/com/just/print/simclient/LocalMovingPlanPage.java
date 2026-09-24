package com.just.print.simclient;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.*;
import org.json.*;
import org.cas.shared.inventory.MovingPlan;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Fresh moving suggestions with durable, idempotent per-item submission. */
final class LocalMovingPlanPage {
    interface BeforeStockWrite { void run() throws Exception; }
    private final Activity activity;
    private final String base;
    private final BeforeStockWrite beforeWrite;
    private final java.util.function.IntConsumer openProduct;
    private final Runnable busyChanged;
    private final SharedPreferences prefs;
    private final Handler main = new Handler(Looper.getMainLooper());
    private final ExecutorService io = Executors.newSingleThreadExecutor();
    private TextView summary, feedback;
    private Button retrySync;
    private EditText activeQuantity;
    private JSONObject activeRow;
    private boolean savingQuantity;
    private ListView list;
    private int sortOrder;
    private JSONObject input, draft, pending;
    private boolean externalBusy, busy, fresh, closed;
    private boolean inventoryTouched;
    private boolean pageVisible;

    LocalMovingPlanPage(Activity activity, String base, BeforeStockWrite beforeWrite,
            java.util.function.IntConsumer openProduct, Runnable busyChanged) {
        this.activity = activity; this.base = base; this.beforeWrite = beforeWrite;
        this.openProduct = openProduct; this.busyChanged = busyChanged;
        prefs = activity.getSharedPreferences("moving_drafts", Activity.MODE_PRIVATE);
    }
    View createView() {
        LinearLayout root = new LinearLayout(activity); root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(18), dp(18), dp(18), dp(8));
        LinearLayout sortBar = new LinearLayout(activity);
        sortBar.setGravity(android.view.Gravity.CENTER_VERTICAL);
        TextView sortLabel = new TextView(activity); sortLabel.setText("Sort by"); sortLabel.setGravity(android.view.Gravity.CENTER_VERTICAL);
        sortBar.addView(sortLabel, new LinearLayout.LayoutParams(dp(80), dp(48)));
        Spinner sort = new Spinner(activity);
        ArrayAdapter<String> sortOptions = new ArrayAdapter<>(activity, android.R.layout.simple_spinner_item,
                new String[]{"Moving quantity", "Product name", "Category"});
        sortOptions.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sort.setAdapter(sortOptions);
        SimClientActivity.styleDropdown(activity, sort);
        sort.setContentDescription("Sort moving plan");
        sort.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onNothingSelected(AdapterView<?> parent) {}
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                sortOrder = position;
                if (list != null && retrySync != null) render();
            }
        });
        sortBar.addView(sort, new LinearLayout.LayoutParams(0, dp(48), 1));
        summary = new TextView(activity); summary.setTextSize(16);
        list = new ListView(activity); list.setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS); root.addView(list, new LinearLayout.LayoutParams(-1, 0, 1));
        // Keep controls and the total below the moving list so the list gets visual priority.
        LinearLayout bottomControls = new LinearLayout(activity);
        bottomControls.setGravity(android.view.Gravity.CENTER_VERTICAL);
        bottomControls.addView(sortBar, new LinearLayout.LayoutParams(-2, -2));
        bottomControls.setGravity(android.view.Gravity.RIGHT | android.view.Gravity.CENTER_VERTICAL);
        root.addView(bottomControls);
        feedback = new TextView(activity); feedback.setAccessibilityLiveRegion(View.ACCESSIBILITY_LIVE_REGION_POLITE); root.addView(feedback);
        retrySync = new Button(activity); retrySync.setText("Retry sync");
        SimClientActivity.stylePrimaryButton(activity, retrySync);
        retrySync.setOnClickListener(v -> submit(null)); root.addView(retrySync);
        root.setBackgroundColor(0xfff0f8fa);
        return root;
    }

    void activate() { pageVisible = true; load(); }
    void deactivate() { flushQuantity(); pageVisible = false; }
    boolean isBusy() { return busy || externalBusy; }
    void setExternalBusy(boolean value) {
        externalBusy = value;

        render();
    }
    boolean consumeInventoryChange() { boolean changed = inventoryTouched; inventoryTouched = false; return changed; }
    void dispose() { closed = true; io.shutdown(); main.removeCallbacksAndMessages(null); }

    private JSONObject read(String key) throws Exception {
        String value = prefs.getString(key + base, null);
        return value == null ? null : new JSONObject(value);
    }
    private void load() {
        if (isBusy()) return;
        if (pending != null) { submit(null); return; }
        if (!flushQuantity()) return;
        fresh = false;
        setBusy(true); setFeedback("Refreshing inventory and calculating plan…");
        io.execute(() -> {
            try {
                JSONObject saved = read("draft:"), request = read("pending:");
                JSONObject savedInput = read("inputs:");
                if (request != null) {
                    main.post(() -> { if (alive()) { draft = saved; input = savedInput; pending = request; fresh = false;
                        setBusy(false); submit(null); } });
                    return;
                }
                saveCatalog(SimClientActivity.request(base, "/AikaPos/stockProducts", null));
                SharedPreferences local = activity.getSharedPreferences("product_catalogs", Activity.MODE_PRIVATE);
                JSONArray products = new JSONArray(local.getString(base, "[]"));
                JSONObject latest = LocalMovingInputs.fromCatalog(products);
                boolean verified = true;
                JSONObject generated = MovingPlan.generate(latest, null);
                if (!prefs.edit().putString("inputs:" + base, latest.toString()).putString("draft:" + base, generated.toString()).commit())
                    throw new IOException("Cannot save draft on this phone");
                JSONObject resultInput = latest;
                String message = generated.getJSONArray("rows").length() == 0
                        ? "No moves suggested by the saved Monque and Stock3 values."
                        : "";
                if (!verified) message = "Refresh inventory to verify the previous stock update before syncing.";
                String note = message;
                main.post(() -> { if (alive()) { input = resultInput; draft = generated; fresh = verified; setBusy(false); setFeedback(note); } });
            } catch (Exception ex) { fail("Could not load/save the draft. " + ex.getMessage()); }
        });
    }

    private void saveCatalog(JSONObject response) throws Exception {
        ProductCatalog validated = new ProductCatalog(response.getJSONArray("products"));
        if (!activity.getSharedPreferences("product_catalogs", Activity.MODE_PRIVATE).edit()
                .putString(base, validated.serialize()).remove("pending:" + base).commit()) throw new IOException("Could not save inventory cache");
    }

    private void submit(JSONObject selected) {
        if (isBusy() || (pending == null && (!fresh || draft == null))) return;
        if (pending == null && selected == null) return;
        if (!flushQuantity()) return;
        inventoryTouched = true;
        setBusy(true); setFeedback("Syncing moved item to AikaPos…");
        io.execute(() -> {
            try {
                JSONObject request = pending;
                if (request == null) {
                    JSONObject row = new JSONObject(selected.toString()).put("state", "done");
                    JSONObject single = new JSONObject().put("draftId", java.util.UUID.randomUUID().toString())
                            .put("rows", new JSONArray().put(row));
                    request = MovingPlan.command(input, single);
                }
                if (!prefs.edit().putString("pending:" + base, request.toString()).commit()) throw new IOException("Could not save submission recovery record");
                // Store the exact request before it can reach the server. Retries never regenerate it.
                pending = request;
                beforeWrite.run();
                JSONObject result = SimClientActivity.request(base, "/AikaPos/movingDraft", request);
                if (!request.getString("draftId").equals(result.getString("draftId")) || result.getInt("billId") < 0)
                    throw new IOException("Invalid submission receipt");
                JSONObject remaining = draft == null ? new JSONObject().put("draftId", java.util.UUID.randomUUID().toString()) : new JSONObject(draft.toString());
                JSONArray kept = new JSONArray(), original = remaining.optJSONArray("rows");
                java.util.Set<String> completed = new java.util.HashSet<>();
                JSONArray submitted = request.getJSONArray("rows");
                for (int i = 0; i < submitted.length(); i++) completed.add(submitted.getJSONObject(i).getString("group"));
                if (original != null) for (int i = 0; i < original.length(); i++) {
                    JSONObject row = original.getJSONObject(i);
                    if (!completed.contains(row.getString("group"))) kept.put(row);
                }
                remaining.put("rows", kept);
                if (!prefs.edit().putString("receipt:" + base, result.toString()).remove("pending:" + base).putString("draft:" + base, remaining.toString()).commit())
                    throw new IOException("POS saved the plan; retry to save the local receipt");
                String message = "Saved to POS as moving plan #" + result.getInt("billId") + ". Stock updated. Item completed.";
                boolean refreshed = false;
                try { saveCatalog(SimClientActivity.request(base, "/AikaPos/stockProducts", null));
                    refreshed = true;
                }
                catch (Exception ex) { message += " Refresh inventory before editing stock."; }
                String text = message;
                boolean verified = refreshed;
                main.post(() -> { if (alive()) { pending = null; draft = remaining; fresh = verified; setBusy(false); setFeedback(text); } });
            } catch (Exception ex) {
                // A missing endpoint has not accepted the request, so retain an editable local draft.
                if (ex instanceof SimClientActivity.RequestException && ((SimClientActivity.RequestException)ex).code == 404) {
                    if (prefs.edit().remove("pending:" + base).commit()) pending = null;
                    activity.getSharedPreferences("product_catalogs", Activity.MODE_PRIVATE).edit().remove("pending:" + base).commit();
                    fail("The POS does not support final plan sync yet. Update POS before submitting. Your local plan is saved and can still be edited.");
                    return;
                }
                if (ex instanceof SimClientActivity.RequestException && ((SimClientActivity.RequestException)ex).code == 409) {
                    if (prefs.edit().remove("pending:" + base).commit()) pending = null;
                    fresh = false;
                }
                fail(ex.getMessage() + (pending == null ? " Draft retained. Refresh and try again." : " Draft retained. Use Retry sync to confirm the same submission."));
            }
        });
    }

    private boolean alive() { return !closed && !activity.isDestroyed() && !activity.isFinishing(); }
    private void fail(String message) { main.post(() -> { if (alive()) { setBusy(false); setFeedback(message); } }); }
    private void setFeedback(String message) {
        feedback.setText(message);
        feedback.setVisibility(message == null || message.isEmpty() ? View.GONE : View.VISIBLE);
    }
    private void setBusy(boolean value) {
        busy = value;
        render(); busyChanged.run();
    }
    private void render() {
        flushQuantity();
        ArrayList<JSONObject> visible = new ArrayList<>();
        JSONArray rows = draft == null ? new JSONArray() : draft.optJSONArray("rows");
        if (rows != null) for (int i = 0; i < rows.length(); i++) {
            JSONObject row = rows.optJSONObject(i); if (row == null) continue;
            visible.add(row);
        }
        java.util.Map<Integer, String> categories = new java.util.HashMap<>();
        JSONArray products = input == null ? null : input.optJSONArray("products");
        if (products != null) for (int i = 0; i < products.length(); i++) {
            JSONObject product = products.optJSONObject(i);
            if (product != null) categories.put(product.optInt("id"), product.optString("category", ""));
        }
        java.util.Collections.sort(visible, (a, b) -> {
            int comparison = sortOrder == 0 ? Long.compare(b.optLong("cases"), a.optLong("cases"))
                    : sortOrder == 2 ? category(a, categories).compareToIgnoreCase(category(b, categories)) : 0;
            if (comparison == 0) comparison = a.optString("name").compareToIgnoreCase(b.optString("name"));
            return comparison != 0 ? comparison : Integer.compare(a.optInt("productId"), b.optInt("productId"));
        });
        retrySync.setVisibility(pending == null ? View.GONE : View.VISIBLE);
        retrySync.setEnabled(!isBusy() && pending != null);
        list.setAdapter(new BaseAdapter() {
            public int getCount() { return visible.size(); }
            public Object getItem(int p) { return visible.get(p); }
            public long getItemId(int p) { return visible.get(p).optLong("productId"); }
            public View getView(int position, View recycled, ViewGroup parent) {
                JSONObject row = visible.get(position);
                LinearLayout line = new LinearLayout(activity); line.setGravity(android.view.Gravity.CENTER_VERTICAL);
                line.setBackgroundColor(position % 2 == 0 ? 0xffffffff : 0xffedf7f8);
                CheckBox check = new CheckBox(activity); check.setMinHeight(dp(48)); check.setMinWidth(0); check.setPadding(0, 0, 0, 0);
                check.setChecked(false); check.setEnabled(!isBusy() && pending == null && fresh);
                check.setContentDescription("Already moved " + row.optString("name"));
                check.setOnClickListener(v -> submit(row));
                line.addView(check, new LinearLayout.LayoutParams(dp(32), dp(56)));
                EditText suggested = new EditText(activity); suggested.setTextSize(17);
                suggested.setSingleLine(true); suggested.setSelectAllOnFocus(true);
                suggested.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
                suggested.setGravity(android.view.Gravity.CENTER);
                suggested.setPadding(dp(2), 0, dp(2), 0);
                suggested.setText(String.valueOf(row.optLong("cases")));
                suggested.setContentDescription("Quantity in cases for " + row.optString("name"));
                suggested.setEnabled(!isBusy() && pending == null);
                android.graphics.drawable.GradientDrawable field = new android.graphics.drawable.GradientDrawable();
                field.setColor(0xffffffff); field.setCornerRadius(dp(6)); field.setStroke(dp(1), 0xffb7c5cf);
                suggested.setBackground(field);
                suggested.setOnFocusChangeListener((v, focused) -> {
                    if (focused) {
                        activeQuantity = suggested; activeRow = row;
                        suggested.post(() -> { if (suggested.hasFocus()) suggested.selectAll(); });
                    }
                    else if (activeQuantity == suggested) flushQuantity();
                });
                suggested.setOnEditorActionListener((v, action, event) -> { suggested.clearFocus(); return false; });
                line.addView(suggested, new LinearLayout.LayoutParams(dp(32), dp(40)));
                TextView name = new TextView(activity); name.setTextSize(16);
                name.setText(row.optString("name"));
                name.setPadding(dp(6), dp(12), dp(6), dp(12));
                name.setMinHeight(dp(56)); name.setGravity(android.view.Gravity.CENTER_VERTICAL);
                name.setEnabled(!isBusy() && pending == null);
                line.setContentDescription(row.optString("name") + ". Double-tap to edit product. Single-tap for actions.");
                line.setEnabled(!isBusy() && pending == null);
                line.setOnClickListener(v -> new AlertDialog.Builder(activity).setTitle(row.optString("name"))
                        .setMessage("Category: " + category(row, categories) + "\n" + row.optLong("cases")
                                + " case(s) × " + row.optLong("unitsPerCase") + " = " + row.optLong("units") + " units\n" + row.optString("code"))
                        .setNegativeButton("Close", null)
                        .setNeutralButton("Edit product", (d, which) -> openProduct.accept(row.optInt("productId")))
                        .show());
                GestureDetector gestures = new GestureDetector(activity, new GestureDetector.SimpleOnGestureListener() {
                    @Override public boolean onDown(MotionEvent event) { return true; }
                    @Override public boolean onSingleTapConfirmed(MotionEvent event) {
                        if (pageVisible && !isBusy() && pending == null && alive()) line.performClick();
                        return true;
                    }
                    @Override public boolean onDoubleTap(MotionEvent event) {
                        if (pageVisible && !isBusy() && pending == null && alive()) openProduct.accept(row.optInt("productId"));
                        return true;
                    }
                });
                line.setOnTouchListener((v, event) -> gestures.onTouchEvent(event));
                line.addView(name, new LinearLayout.LayoutParams(0, -2, 1));
                return line;
            }
        });
    }
    boolean flushQuantity() {
        if (activeQuantity == null || savingQuantity) return true;
        EditText editor = activeQuantity; JSONObject row = activeRow;
        savingQuantity = true;
        try {
            long count = Long.parseLong(editor.getText().toString().trim());
            long units = Math.multiplyExact(count, row.getLong("unitsPerCase"));
            if (count < 1 || units > Integer.MAX_VALUE) throw new IllegalArgumentException();
            if (count != row.getLong("cases")) {
                JSONObject updated = new JSONObject(draft.toString());
                JSONArray rows = updated.getJSONArray("rows");
                for (int i = 0; i < rows.length(); i++) {
                    JSONObject item = rows.getJSONObject(i);
                    if (item.getString("group").equals(row.getString("group")))
                        item.put("cases", count).put("units", units).put("manualQuantity", true);
                }
                if (!prefs.edit().putString("draft:" + base, updated.toString()).commit())
                    throw new IOException("Unable to save quantity");
                draft = updated;
                row.put("cases", count).put("units", units).put("manualQuantity", true);
            }
            activeQuantity = null; activeRow = null;
            return true;
        } catch (Exception ex) {
            editor.setText(String.valueOf(row.optLong("cases")));
            activeQuantity = null; activeRow = null;
            setFeedback("Quantity not saved. Enter a positive whole number. Previous quantity retained.");
            return false;
        } finally { savingQuantity = false; }
    }
    private TextView tableHeading(String label) {
        TextView view = new TextView(activity); view.setText(label); view.setTextSize(13);
        view.setTypeface(null, android.graphics.Typeface.BOLD);
        view.setGravity(android.view.Gravity.CENTER); view.setPadding(dp(2), 0, dp(2), 0); return view;
    }
    private String category(JSONObject row, java.util.Map<Integer, String> categories) {
        String value = categories.get(row.optInt("productId"));
        return value == null ? row.optString("category", "") : value;
    }
    private int dp(int n) { return Math.round(n * activity.getResources().getDisplayMetrics().density); }
}
