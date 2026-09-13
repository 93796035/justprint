package com.just.print.simclient;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.widget.*;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import com.just.print.simclient.ProductCatalog.Product;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Small standalone launcher: no POS database, login, timers or printing are initialized. */
public class SimClientActivity extends Activity {
    private final Handler main = new Handler(Looper.getMainLooper());
    private final ExecutorService io = Executors.newSingleThreadExecutor();
    private AutoCompleteTextView product;
    private Spinner category;
    private java.util.List<String> categories = new ArrayList<>();
    private String selectedCategory;
    private final EditText[] quantities = new EditText[4];
    private final String[] keys = {"stock1", "stock2", "stock3", "monque"};
    private TextView status;
    private LinearLayout refreshProgress;
    private ProgressBar refreshProgressBar;
    private TextView refreshProgressText;
    private Button submit, connection, refresh;
    private ProductCatalog catalog;
    private JSONObject selected;
    private String server;
    private boolean settingText, saving;
    private volatile boolean closed;
    private int generation;
    private Runnable pendingSearch;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        setTitle("SimClient");
        setFinishOnTouchOutside(false);
        server = getPreferences(MODE_PRIVATE).getString("server", "");
        getPreferences(MODE_PRIVATE).edit().remove("token").apply();
        LinearLayout root = column();
        int pad = dp(18);
        root.setPadding(pad, pad, pad, pad);
        TextView title = new TextView(this);
        title.setText("Inventory Editor");
        title.setTextSize(21);
        connection = new Button(this);
        connection.setText("Settings");
        connection.setMinWidth(0);
        connection.setMinimumWidth(0);
        connection.setOnClickListener(v -> configure());
        refresh = new Button(this);
        refresh.setText("Refresh");
        refresh.setOnClickListener(v -> refreshCatalog());
        LinearLayout toolbar = new LinearLayout(this);
        toolbar.setGravity(android.view.Gravity.CENTER_VERTICAL);
        for (Button button : new Button[]{connection, refresh}) {
            button.setTextSize(13);
            button.setAllCaps(false);
            button.setMinWidth(0);
            button.setMinimumWidth(0);
            button.setPadding(dp(10), 0, dp(10), 0);
        }
        toolbar.addView(connection, new LinearLayout.LayoutParams(-2, dp(48)));
        toolbar.addView(new View(this), new LinearLayout.LayoutParams(0, 1, 1));
        toolbar.addView(refresh, new LinearLayout.LayoutParams(-2, dp(48)));
        root.addView(toolbar, full());
        refreshProgress = column();
        refreshProgressText = new TextView(this);
        refreshProgressText.setAccessibilityLiveRegion(View.ACCESSIBILITY_LIVE_REGION_POLITE);
        refreshProgressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        refreshProgressBar.setMax(100);
        refreshProgress.addView(refreshProgressText, full());
        refreshProgress.addView(refreshProgressBar, full());
        refreshProgress.setVisibility(View.GONE);
        root.addView(refreshProgress, full());
        root.addView(title, full());
        TextView categoryLabel = new TextView(this);
        categoryLabel.setText("Category");
        root.addView(categoryLabel, full());
        category = new Spinner(this);
        root.addView(category, full());
        category.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onNothingSelected(AdapterView<?> parent) {}
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position >= categories.size()) return;
                String value = categories.get(position);
                if (value.equals(selectedCategory)) return;
                selectedCategory = value;
                invalidateSelection();
                String query = product.getText().toString().trim();
                if (!query.isEmpty()) search(query, generation, false);
            }
        });
        product = new AutoCompleteTextView(this) {
            @Override protected void performFiltering(CharSequence text, int keyCode) {
                // Local catalog handles substring matching instead of Android's prefix filter.
            }
            @Override protected void replaceText(CharSequence text) {
                settingText = true;
                try { super.replaceText(text); } finally { settingText = false; }
            }
        };
        product.setHint("Enter product name");
        product.setSingleLine(true);
        product.setThreshold(1);
        styleInput(product);
        product.setTextSize(20);
        product.setMinHeight(dp(64));
        TextView productLabel = new TextView(this);
        productLabel.setText("Product name");
        productLabel.setPadding(0, dp(20), 0, dp(8));
        root.addView(productLabel, full());
        root.addView(product, full());
        LinearLayout stocks = new LinearLayout(this);
        for (int i = 0; i < 3; i++) {
            LinearLayout field = numberField(i, "Stock" + (i + 1));
            stocks.addView(field, new LinearLayout.LayoutParams(0, -2, 1));
        }
        root.addView(stocks, full());
        root.addView(numberField(3, "Monque"), full());
        submit = new Button(this);
        submit.setText("Submit");
        submit.setOnClickListener(v -> save());
        root.addView(submit, full());
        status = new TextView(this);
        status.setTextSize(14);
        status.setAccessibilityLiveRegion(View.ACCESSIBILITY_LIVE_REGION_POLITE);
        root.addView(status, full());
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(0xfff5f7fa);
        scroll.addView(root);
        setContentView(scroll);
        getWindow().setLayout(-1, -1);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        enableValues(false);
        product.addTextChangedListener(new TextWatcher() {
            private boolean appended;
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!settingText) appended = count > 0 && start + count == s.length();
            }
            public void afterTextChanged(Editable text) {
                if (settingText) return;
                invalidateSelection();
                String query = text.toString().trim();
                if (query.isEmpty()) { message("Type a name, then select a product from the list.", false); return; }
                final boolean complete = appended;
                final int current = generation;
                pendingSearch = () -> {
                    if (closed || current != generation) return;
                    search(query, current, complete);
                };
                main.post(pendingSearch);
            }
        });
        product.setOnItemClickListener((parent, view, position, id) -> {
            Product item = (Product) parent.getItemAtPosition(position);
            generation++;
            if (pendingSearch != null) main.removeCallbacks(pendingSearch);
            try { fillProduct(item.row()); }
            catch (Exception ex) { message("Invalid cached product. Tap Refresh.", false); }
        });
        message(server.isEmpty() ? "Open Settings to enter the POS address." : "Type a name, then select a product from the list.", false);
        product.requestFocus();
        loadCatalog();
    }

    private void invalidateSelection() {
        generation++;
        if (pendingSearch != null) main.removeCallbacks(pendingSearch);
        selected = null;
        enableValues(false);
        for (EditText field : quantities) field.setText("");
        product.dismissDropDown();
        product.setAdapter(new ArrayAdapter<Product>(this, android.R.layout.simple_dropdown_item_1line, new ArrayList<Product>()));
    }

    private void search(String query, int current, boolean complete) {
        if (catalog == null || catalog.size() == 0) { message("No local products. Set the POS address, then tap Refresh.", false); return; }
        java.util.List<Product> matches = selectedCategory == null ? new ArrayList<>() : catalog.search(query, selectedCategory);
        Editable input = product.getText();
        if (complete && product.hasFocus() && product.getSelectionStart() == input.length()
                && product.getSelectionEnd() == input.length()
                && android.view.inputmethod.BaseInputConnection.getComposingSpanStart(input) < 0) {
            String typed = input.toString();
            String completed = ProductCatalog.complete(typed, matches);
            if (!completed.equals(typed)) {
                settingText = true;
                try {
                    product.setText(completed, false);
                    product.setSelection(completed.length());
                } finally { settingText = false; }
            }
        }
        product.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, matches));
        if (!matches.isEmpty() && product.hasFocus()) product.showDropDown();
        message(matches.isEmpty() ? "No matching products in this category." : "Found " + matches.size() + " products locally. Select one to continue.", false);
    }

    private void updateCategories(boolean reset) {
        categories = catalog == null ? new ArrayList<>() : catalog.categories();
        if (reset || !categories.contains(selectedCategory)) selectedCategory = catalog == null ? null : catalog.defaultCategory();
        java.util.List<String> labels = new ArrayList<>();
        for (String name : categories) labels.add(name.isEmpty() ? "Uncategorized" : name);
        final java.util.List<String> categoryNames = new ArrayList<>(categories);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, labels) {
            private View style(View view, int position, boolean dropdown) {
                TextView text = (TextView) view;
                int color = ProductCatalog.categoryColor(categoryNames.get(position));
                text.setBackgroundColor(color == 0 ? 0xffffffff : color);
                text.setTextColor(color == 0xffdc143c || color == 0xff228b22 ? 0xffffffff : 0xff111111);
                text.setTypeface(null, color == 0 ? android.graphics.Typeface.NORMAL : android.graphics.Typeface.BOLD);
                text.setTextSize(18);
                text.setMinHeight(dp(48));
                text.setGravity(android.view.Gravity.CENTER_VERTICAL);
                text.setPadding(dp(dropdown && color == 0 ? 28 : 12), dp(8), dp(12), dp(8));
                return text;
            }
            @Override public View getView(int position, View convertView, android.view.ViewGroup parent) {
                return style(super.getView(position, convertView, parent), position, false);
            }
            @Override public View getDropDownView(int position, View convertView, android.view.ViewGroup parent) {
                return style(super.getDropDownView(position, convertView, parent), position, true);
            }
        };
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        category.setAdapter(adapter);
        if (selectedCategory != null) category.setSelection(categories.indexOf(selectedCategory));
        category.setEnabled(!saving && !categories.isEmpty());
    }

    private void busy(boolean value) {
        saving = value;
        product.setEnabled(!value);
        category.setEnabled(!value && !categories.isEmpty());
        connection.setEnabled(!value);
        refresh.setEnabled(!value);
        enableValues(!value && selected != null);
    }

    private void loadCatalog() {
        invalidateSelection();
        catalog = null;
        busy(true);
        message("Loading local products…", false);
        String base = server;
        io.execute(() -> {
            ProductCatalog loaded = null;
            String feedback;
            try {
                if (getSharedPreferences("product_catalogs", MODE_PRIVATE).getBoolean("pending:" + base, false))
                    throw new IOException("An interrupted update needs Refresh");
                String data = getSharedPreferences("product_catalogs", MODE_PRIVATE).getString(base, "[]");
                loaded = new ProductCatalog(new JSONArray(data));
                feedback = loaded.size() + " products loaded locally. Tap Refresh to download the latest data.";
            } catch (Exception ex) { feedback = "Local data needs verification. Tap Refresh before editing."; }
            ProductCatalog result = loaded;
            String text = feedback;
            main.post(() -> {
                if (closed) return;
                catalog = result;
                updateCategories(true);
                busy(false);
                message(text, false);
            });
        });
    }

    private void persistCatalog(String base, ProductCatalog snapshot) throws Exception {
        if (!getSharedPreferences("product_catalogs", MODE_PRIVATE).edit().putString(base, snapshot.serialize())
                .remove("pending:" + base).commit())
            throw new IOException("Local storage could not be updated");
    }

    private void refreshCatalog() {
        if (saving) return;
        if (server.isEmpty()) { message("Enter the POS address in Settings first.", false); return; }
        invalidateSelection();
        busy(true);
        refreshProgress.setVisibility(View.VISIBLE);
        showRefreshProgress("Connecting to POS…", -1);
        message("Refreshing products…", false);
        String base = server;
        io.execute(() -> {
            ProductCatalog downloaded = null;
            String feedback;
            try {
                JSONObject response = request(base, "/AikaPos/stockProducts", null,
                        (text, percent) -> main.post(() -> {
                            if (!closed) showRefreshProgress(text, percent);
                        }));
                main.post(() -> { if (!closed) showRefreshProgress("Preparing product inventory…", -1); });
                ProductCatalog candidate = new ProductCatalog(response.getJSONArray("products"));
                main.post(() -> { if (!closed) showRefreshProgress("Saving " + candidate.size() + " products on this phone…", -1); });
                persistCatalog(base, candidate);
                downloaded = candidate;
                feedback = candidate.size() + " products refreshed and saved locally.";
            } catch (Exception ex) { feedback = "Refresh failed. Previous local data retained. " + ex.getMessage(); }
            ProductCatalog result = downloaded;
            String text = feedback;
            main.post(() -> {
                if (closed) return;
                if (result != null) { catalog = result; updateCategories(false); }
                refreshProgress.setVisibility(View.GONE);
                busy(false);
                product.requestFocus(); product.selectAll();
                message(text, true);
            });
        });
    }

    private void fillProduct(JSONObject row) {
        try {
            selected = row;
            settingText = true;
            String name = row.optString("mnemonic", "").trim();
            if (name.isEmpty()) name = row.optString("subject", "").trim();
            if (name.isEmpty()) name = row.optString("code", "");
            product.setText(name, false);
            settingText = false;
            for (int i = 0; i < keys.length; i++) quantities[i].setText(String.valueOf(row.getInt(keys[i])));
            enableValues(true);
            message("Stock loaded locally. Quantities use POS inventory units.", false);
        } catch (Exception ex) { settingText = false; invalidateSelection(); message(ex.getMessage(), false); }
    }

    private void save() {
        if (saving || selected == null) return;
        final JSONObject payload = new JSONObject();
        try {
            payload.put("productId", selected.getInt("id"));
            JSONObject values = new JSONObject(), expected = new JSONObject();
            for (int i = 0; i < keys.length; i++) {
                String value = quantities[i].getText().toString().trim();
                if (!value.matches("-?[0-9]+")) { quantities[i].setError("Enter a whole number"); quantities[i].requestFocus(); return; }
                int count;
                try { count = Integer.parseInt(value); }
                catch (NumberFormatException ex) { quantities[i].setError("Quantity is out of range"); return; }
                if (count == Integer.MIN_VALUE) { quantities[i].setError("Quantity is out of range"); return; }
                values.put(keys[i], count);
                expected.put(keys[i], selected.getInt(keys[i]));
            }
            payload.put("values", values).put("expected", expected);
        } catch (Exception ex) { message(ex.getMessage(), false); return; }
        busy(true);
        product.dismissDropDown();
        generation++;
        if (pendingSearch != null) main.removeCallbacks(pendingSearch);
        product.setEnabled(false);
        connection.setEnabled(false);
        enableValues(false);
        message("Submitting…", false);
        String base = server;
        ProductCatalog snapshot = catalog;
        io.execute(() -> {
            String feedback;
            ProductCatalog updated = null;
            try {
                // Write the recovery marker before sending: a process death must never silently
                // expose pre-submit quantities as current after a potentially successful update.
                if (!getSharedPreferences("product_catalogs", MODE_PRIVATE).edit()
                        .putBoolean("pending:" + base, true).commit())
                    throw new IOException("Unable to save recovery state. Nothing was submitted.");
                JSONObject result = request(base, "/AikaPos/stockProducts", payload);
                try {
                    updated = snapshot.updated(result.getJSONObject("product"));
                    persistCatalog(base, updated);
                    feedback = "Inventory updated successfully. Local data saved.";
                } catch (Exception ex) {
                    feedback = "POS updated, but local storage could not be saved. Tap Refresh before continuing.";
                }
            } catch (Exception ex) { feedback = ex.getMessage() + " Tap Refresh to load the current stock."; }
            String finalFeedback = feedback;
            ProductCatalog finalUpdated = updated;
            main.post(() -> {
                if (closed) return;
                catalog = finalUpdated;
                busy(false);
                invalidateSelection();
                product.requestFocus();
                product.selectAll();
                message(finalFeedback, true);
            });
        });
    }

    private void showRefreshProgress(String text, int percent) {
        refreshProgressText.setText(text);
        refreshProgressBar.setIndeterminate(percent < 0);
        if (percent >= 0) refreshProgressBar.setProgress(percent);
    }

    private interface DownloadProgress {
        void update(String text, int percent);
    }

    private static JSONObject request(String base, String path, JSONObject body) throws Exception {
        return request(base, path, body, null);
    }

    private static JSONObject request(String base, String path, JSONObject body, DownloadProgress progress) throws Exception {
        HttpURLConnection c = (HttpURLConnection) new URL(base + path).openConnection();
        c.setConnectTimeout(5000);
        c.setReadTimeout(10000);
        c.setInstanceFollowRedirects(false);

        try {
            if (body != null) {
                c.setRequestMethod("POST");
                c.setDoOutput(true);
                c.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
                try (OutputStream out = c.getOutputStream()) { out.write(body.toString().getBytes(StandardCharsets.UTF_8)); }
            }
            int code = c.getResponseCode();
            InputStream stream = code >= 400 ? c.getErrorStream() : c.getInputStream();
            if (stream == null) throw new IOException("HTTP " + code);
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            int total = c.getContentLength();
            long lastProgress = android.os.SystemClock.elapsedRealtime();
            if (progress != null) progress.update("Downloading products…", total > 0 ? 0 : -1);
            try (InputStream in = stream) {
                byte[] buf = new byte[4096]; int n;
                while ((n = in.read(buf)) != -1) {
                    if (bytes.size() + n > 64 * 1024 * 1024) throw new IOException("Server response is too large");
                    bytes.write(buf, 0, n);
                    long now = android.os.SystemClock.elapsedRealtime();
                    if (progress != null && now - lastProgress >= 250) {
                        int percent = total > 0 ? (int) Math.min(100, bytes.size() * 100L / total) : -1;
                        progress.update("Downloading products… " + ((bytes.size() + 1023) / 1024) + " KB received"
                                + (percent >= 0 ? " (" + percent + "%)" : ""), percent);
                        lastProgress = now;
                    }
                }
            }
            if (progress != null) progress.update("Reading downloaded products…", -1);
            JSONObject result;
            try { result = new JSONObject(new String(bytes.toByteArray(), StandardCharsets.UTF_8)); }
            catch (Exception ex) { throw new IOException("HTTP " + code + ": Check that POS supports the inventory API"); }
            if (code != 200) throw new IOException("HTTP " + code + ": " + httpError(code));
            return result;
        } catch (java.net.SocketTimeoutException ex) {
            throw new IOException(body == null ? "Connection timed out" : "Response timed out. Update status is unconfirmed", ex);
        } finally { c.disconnect(); }
    }

    private void configure() {
        LinearLayout box = column();
        box.setPadding(dp(20), 0, dp(20), 0);
        EditText address = new EditText(this);
        styleInput(address);
        TextView ipLabel = new TextView(this);
        ipLabel.setText("AikaPos computer IP (default port: 8880)");
        box.addView(ipLabel);
        address.setHint("Enter POS computer IP"); address.setSingleLine(); address.setText(server);

        box.addView(address);
        Button viewLocal = new Button(this);
        viewLocal.setText("View Local Inventory");
        viewLocal.setOnClickListener(v -> viewLocalInventory());
        box.addView(viewLocal);
        AlertDialog dialog = new AlertDialog.Builder(this).setTitle("Connection Settings")
                .setView(box).setNegativeButton("Cancel", null).setPositiveButton("Save", null).create();
        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String url = address.getText().toString().trim().replaceAll("/+$", "");
            boolean bareAddress = !url.contains("://");
            if (bareAddress) url = "http://" + url;
            try {
                URL parsed = new URL(url);
                if (!(parsed.getProtocol().equals("http") || parsed.getProtocol().equals("https")) || parsed.getHost().isEmpty()
                        || !parsed.getPath().isEmpty() || parsed.getQuery() != null || parsed.getRef() != null || parsed.getUserInfo() != null)
                    throw new Exception();
                if (bareAddress && parsed.getPort() == -1) url += ":8880";
            } catch (Exception ex) { address.setError("Enter a server address, e.g. http://192.168.1.20:8880"); return; }

            String savedAddress = url;
            android.content.SharedPreferences preferences = getPreferences(MODE_PRIVATE);
            address.setEnabled(false);
            viewLocal.setEnabled(false);
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setText("Saving…");
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setEnabled(false);
            dialog.setCancelable(false);
            busy(true);
            io.execute(() -> {
                boolean saved = preferences.edit().putString("server", savedAddress).remove("token").commit();
                main.post(() -> {
                    if (closed) return;
                    busy(false);
                    if (!saved) {
                        address.setEnabled(true);
                        viewLocal.setEnabled(true);
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setText("Save");
                        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setEnabled(true);
                        dialog.setCancelable(true);
                        address.setError("Could not save the address. Please try again.");
                        return;
                    }
                    server = savedAddress;
                    loadCatalog();
                    dialog.dismiss();
                    Toast.makeText(this, "Connection saved: " + server, Toast.LENGTH_LONG).show();
                    product.requestFocus(); product.selectAll();
                });
            });
        }));
        dialog.show();
    }

    private void viewLocalInventory() {
        // Read persisted data, not the editable fields or in-memory snapshot.
        LinearLayout content = column();
        content.setPadding(dp(16), dp(12), dp(16), dp(12));
        TextView summary = new TextView(this);
        summary.setText("Loading saved inventory…");
        summary.setPadding(0, 0, 0, dp(12));
        content.addView(summary, full());
        EditText filter = new EditText(this);
        filter.setHint("Filter saved products");
        filter.setSingleLine(true);
        styleInput(filter);
        filter.setEnabled(false);
        content.addView(filter, full());
        ListView list = new ListView(this);
        content.addView(list, new LinearLayout.LayoutParams(-1, 0, 1));
        AlertDialog viewer = new AlertDialog.Builder(this).setTitle("Local Inventory")
                .setView(content).setPositiveButton("Close", null).create();
        viewer.show();
        viewer.getWindow().setLayout(-1, -1);
        viewer.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
                | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        String base = server;
        io.execute(() -> {
            try {
                android.content.SharedPreferences prefs = getSharedPreferences("product_catalogs", MODE_PRIVATE);
                ProductCatalog saved = new ProductCatalog(new JSONArray(prefs.getString(base, "[]")));
                boolean pending = prefs.getBoolean("pending:" + base, false);
                main.post(() -> {
                    if (closed || !viewer.isShowing()) return;
                    summary.setText(saved.size() + " saved products\n" + (base.isEmpty() ? "No server configured" : base)
                            + (pending ? "\nAn update was interrupted. Tap Refresh to verify these saved values."
                            : saved.size() == 0 ? "\nTap Refresh on the main screen to download products." : "\nStored on this phone. No server request."));
                    filter.setEnabled(true);
                    showLocalRows(list, saved.all());
                    filter.addTextChangedListener(new TextWatcher() {
                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                        public void onTextChanged(CharSequence s, int start, int before, int count) {}
                        public void afterTextChanged(Editable text) {
                            showLocalRows(list, text.toString().trim().isEmpty() ? saved.all() : saved.search(text.toString()));
                        }
                    });
                });
            } catch (Exception ex) {
                main.post(() -> { if (!closed && viewer.isShowing()) summary.setText("Unable to read saved inventory. Tap Refresh to rebuild it."); });
            }
        });
    }

    private void showLocalRows(ListView list, java.util.List<Product> rows) {
        java.util.List<java.util.Map<String, String>> items = new ArrayList<>();
        for (Product item : rows) {
            java.util.Map<String, String> entry = new java.util.HashMap<>();
            entry.put("name", item.name);
            try {
                JSONObject row = item.row();
                entry.put("details", "ID: " + item.id + "   SKU: " + item.code
                        + "\nStock1: " + row.getInt("stock1") + "   Stock2: " + row.getInt("stock2")
                        + "   Stock3: " + row.getInt("stock3") + "\nMonque: " + row.getInt("monque"));
            } catch (Exception ex) { entry.put("details", "Invalid saved record. Tap Refresh."); }
            items.add(entry);
        }
        list.setAdapter(new SimpleAdapter(this, items, android.R.layout.simple_list_item_2,
                new String[]{"name", "details"}, new int[]{android.R.id.text1, android.R.id.text2}));
    }

    private void report(Exception ex, int current) {
        main.post(() -> { if (!closed && generation == current) message("Unable to load products: " + ex.getMessage(), false); });
    }
    private LinearLayout numberField(int index, String name) {
        LinearLayout field = column();
        field.setPadding(dp(4), dp(16), dp(4), dp(8));
        TextView label = new TextView(this); label.setText(name); field.addView(label);
        EditText input = new EditText(this); input.setSingleLine();
        styleInput(input);
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED);
        input.setSelectAllOnFocus(true); input.setContentDescription(name);
        LinearLayout.LayoutParams inputLayout = full();
        if (index == 3) {
            inputLayout.width = (int) Math.ceil(label.getPaint().measureText(name));
            input.setPadding(dp(4), dp(12), dp(4), dp(12));
        }
        quantities[index] = input; field.addView(input, inputLayout); return field;
    }
    private void enableValues(boolean enabled) { for (EditText field : quantities) field.setEnabled(enabled); submit.setEnabled(enabled); }
    private void styleInput(EditText input) {
        android.graphics.drawable.StateListDrawable backgrounds = new android.graphics.drawable.StateListDrawable();
        int[][] states = {new int[]{-android.R.attr.state_enabled}, new int[]{android.R.attr.state_focused}, new int[]{}};
        int[] borders = {0xffcbd5e1, 0xff167d8d, 0xff8899aa};
        for (int i = 0; i < states.length; i++) {
            android.graphics.drawable.GradientDrawable box = new android.graphics.drawable.GradientDrawable();
            box.setColor(i == 0 ? 0xffe9eef3 : 0xffffffff);
            box.setCornerRadius(dp(8));
            box.setStroke(dp(i == 1 ? 2 : 1), borders[i]);
            backgrounds.addState(states[i], box);
        }
        input.setBackground(backgrounds);
        input.setPadding(dp(12), dp(12), dp(12), dp(12));
        input.setMinHeight(dp(52));
        input.setTextColor(0xff152638);
        input.setHintTextColor(0xff627386);
    }
    private static String httpError(int code) {
        switch (code) {
            case 400: return "Invalid product or quantity. Tap Refresh and check your input.";
            case 401: return "This POS server requires an update. Install the version with IP-only inventory access.";
            case 404: return "Inventory endpoint not found. Check the POS server version.";
            case 409: return "Stock changed or POS is busy. Finish editing on POS, then tap Refresh.";
            case 413: return "Request is too large.";
            default: return "Server request failed. Check POS and reload stock before retrying.";
        }
    }
    private void message(String text, boolean toast) {
        status.setText(text); submit.setTooltipText(text);
        if (toast) Toast.makeText(this, text, Toast.LENGTH_LONG).show();
    }
    private LinearLayout column() { LinearLayout view = new LinearLayout(this); view.setOrientation(LinearLayout.VERTICAL); return view; }
    private LinearLayout.LayoutParams full() { return new LinearLayout.LayoutParams(-1, -2); }
    private int dp(int value) { return Math.round(value * getResources().getDisplayMetrics().density); }
    @Override protected void onDestroy() { closed = true; main.removeCallbacksAndMessages(null); io.shutdown(); super.onDestroy(); }
}
