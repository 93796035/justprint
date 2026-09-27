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
    private final EditText[] prices = new EditText[2];
    private final String[] priceKeys = {"cost", "price"};
    private LinearLayout refreshProgress;
    private ProgressBar refreshProgressBar;
    private TextView refreshProgressText;
    private Button moving;
    private Button stocking;
    private ImageButton connection, refresh;
    private Button editingTab;
    private ImageButton idTab;
    private FrameLayout pages;
    private View editingPage, movingPage, idPage;
    private View stockingPage;
    private StockingPlanPage stockingPlan;
    private IdCheckingPage idChecking;
    private LocalMovingPlanPage movingPlan;
    private boolean showingMoving;
    private boolean showingStocking;
    private boolean showingId;
    private Integer requestedProductId;
    private ProductCatalog catalog;
    private JSONObject selected;
    private Integer editedFromProductId;
    private String server;
    private boolean settingText, settingValues, saving;
    private final java.util.LinkedHashMap<String, AutoSaveRequest> pendingAutoSaves = new java.util.LinkedHashMap<>();
    private AutoSaveRequest autoSaveInFlight;
    private AutoSaveRequest failedAutoSave;
    private JSONObject failedAutoSavePayload;
    private boolean openMovingAfterSave;
    private boolean optimisticLocalPending;
    private volatile boolean closed;
    private int generation;
    private Runnable pendingSearch;
    private final CompletionEchoGuard completionEcho = new CompletionEchoGuard();

    // This Activity deliberately uses the platform Material theme, not AppCompat.
    @android.annotation.SuppressLint("AppCompatCustomView")
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
        android.text.SpannableString brand = new android.text.SpannableString("SimClient");
        brand.setSpan(new android.text.style.ForegroundColorSpan(0xff64e5ce), 0, 3, 0);
        brand.setSpan(new android.text.style.StyleSpan(android.graphics.Typeface.BOLD_ITALIC), 0, 9, 0);
        title.setText(brand);
        title.setTextColor(0xffffffff);
        title.setTypeface(android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.NORMAL));
        title.setLetterSpacing(0.035f);
        title.setSingleLine(true);
        title.setContentDescription("SimClient");
        title.setGravity(android.view.Gravity.CENTER);
        title.setAutoSizeTextTypeUniformWithConfiguration(16, 28, 1, android.util.TypedValue.COMPLEX_UNIT_SP);
        LinearLayout brandBlock = column();
        brandBlock.setGravity(android.view.Gravity.CENTER);
        brandBlock.addView(title, new LinearLayout.LayoutParams(-1, dp(36)));
        TextView tagline = new TextView(this);
        tagline.setText("--Twins of AikaPos");
        tagline.setTextSize(12);
        tagline.setTextColor(0xffa6d9da);
        tagline.setGravity(android.view.Gravity.CENTER);
        tagline.setSingleLine(true);
        tagline.setEllipsize(android.text.TextUtils.TruncateAt.END);
        brandBlock.addView(tagline, full());
        connection = toolbarIcon(com.just.print.R.drawable.ic_sim_settings, "Settings");
        connection.setOnClickListener(v -> configure());
        refresh = toolbarIcon(com.just.print.R.drawable.ic_sim_refresh, "Refresh");
        refresh.setOnClickListener(v -> refreshCatalog());
        LinearLayout toolbar = new LinearLayout(this);
        toolbar.setGravity(android.view.Gravity.CENTER_VERTICAL);
        toolbar.addView(connection, new LinearLayout.LayoutParams(dp(48), dp(48)));
        toolbar.addView(brandBlock, new LinearLayout.LayoutParams(0, -2, 1));
        toolbar.addView(refresh, new LinearLayout.LayoutParams(dp(48), dp(48)));
        toolbar.setPadding(dp(12), dp(4), dp(12), dp(4));
        toolbar.setMinimumHeight(dp(76));
        toolbar.setBackground(new android.graphics.drawable.GradientDrawable(
                android.graphics.drawable.GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{0xff112d46, 0xff17465a}));
        toolbar.setElevation(dp(4));
        moving = new Button(this);
        moving.setText("Stock Moving");
        moving.setAllCaps(false);
        moving.setOnClickListener(v -> openMovingPlans());
        refreshProgress = column();
        refreshProgressText = new TextView(this);
        refreshProgressText.setAccessibilityLiveRegion(View.ACCESSIBILITY_LIVE_REGION_POLITE);
        refreshProgressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        refreshProgressBar.setMax(100);
        refreshProgress.addView(refreshProgressText, full());
        refreshProgress.addView(refreshProgressBar, full());
        refreshProgress.setVisibility(View.GONE);

        TextView categoryLabel = new TextView(this);
        categoryLabel.setText("Category");
        LinearLayout categoryBar = new LinearLayout(this);
        categoryBar.setGravity(android.view.Gravity.CENTER_VERTICAL);
        categoryBar.addView(categoryLabel, new LinearLayout.LayoutParams(dp(80), dp(48)));
        categoryLabel.setGravity(android.view.Gravity.CENTER_VERTICAL);
        category = new Spinner(this);
        styleDropdown(this, category);
        categoryBar.addView(category, new LinearLayout.LayoutParams(0, dp(48), 1));
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
            @Override protected void onSelectionChanged(int start, int end) {
                super.onSelectionChanged(start, end);
                if (!settingText && (start != length() || end != length())) completionEcho.reset();
            }
            @Override protected void performFiltering(CharSequence text, int keyCode) {
                // Local catalog handles substring matching instead of Android's prefix filter.
            }
            @Override protected void replaceText(CharSequence text) {
                completionEcho.reset();
                settingText = true;
                try { super.replaceText(text); } finally { settingText = false; }
            }
        };
        product.setHint("Enter product name");
        product.setSingleLine(true);
        product.setSelectAllOnFocus(true);
        product.setThreshold(1);
        product.setFilters(new android.text.InputFilter[]{(source, start, end, dest, dstart, dend) -> {
            if (settingText) return null;
            int consumed = completionEcho.consume(dest.toString(), dstart, dend,
                    source.subSequence(start, end).toString(), android.os.SystemClock.uptimeMillis());
            return consumed == 0 ? null : source.subSequence(start + consumed, end);
        }});
        product.setOnTouchListener((view, event) -> {
            if (event.getActionMasked() == android.view.MotionEvent.ACTION_DOWN) completionEcho.reset();
            return false;
        });
        product.setOnFocusChangeListener((view, focused) -> {
            if (focused) product.post(() -> { if (product.hasFocus()) product.selectAll(); });
            else completionEcho.reset();
        });
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
        LinearLayout details = new LinearLayout(this);
        details.addView(numberField(3, "Monque"), new LinearLayout.LayoutParams(0, -2, 1));
        details.addView(priceField(0, "Cost"), new LinearLayout.LayoutParams(0, -2, 1));
        details.addView(priceField(1, "Price"), new LinearLayout.LayoutParams(0, -2, 1));
        root.addView(details, full());
        // Category belongs below all editable inventory fields and above the status text.
        LinearLayout.LayoutParams categoryParams = full();
        categoryParams.topMargin = dp(12);
        root.addView(categoryBar, categoryParams);
        status = new TextView(this);
        status.setTextSize(14);
        status.setPadding(0, dp(18), 0, dp(8));
        status.setAccessibilityLiveRegion(View.ACCESSIBILITY_LIVE_REGION_POLITE);
        root.addView(status, full());
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(0xfff0f8fa);
        scroll.addView(root);
        editingPage = scroll;
        LinearLayout shell = column();
        shell.addView(toolbar, full());
        shell.addView(refreshProgress, full());
        pages = new FrameLayout(this);
        pages.addView(editingPage, new FrameLayout.LayoutParams(-1, -1));
        shell.addView(pages, new LinearLayout.LayoutParams(-1, 0, 1));
        View divider = new View(this); divider.setBackgroundColor(0xffd9ecef);
        shell.addView(divider, new LinearLayout.LayoutParams(-1, dp(1)));
        LinearLayout tabs = new LinearLayout(this);
        tabs.setPadding(0, 0, 0, dp(8));
        tabs.setBackgroundColor(0xffffffff);
        tabs.setElevation(dp(8));
        editingTab = new Button(this); editingTab.setText("Product");
        editingTab.setAllCaps(false); editingTab.setOnClickListener(v -> showEditingPage(null));
        stocking = new Button(this); stocking.setText("Stocking"); stocking.setAllCaps(false);
        stocking.setOnClickListener(v -> showStockingPage());
        idTab = new ImageButton(this);
        idTab.setImageResource(com.just.print.R.drawable.ic_sim_camera);
        idTab.setScaleType(ImageView.ScaleType.CENTER);
        idTab.setContentDescription("Camera: find product or check ID");
        idTab.setTooltipText("Camera");
        idTab.setOnClickListener(v -> showIdPage(null));
        for (View tab : new View[]{editingTab, moving, stocking, idTab}) {
            if (tab instanceof Button) {
                Button label = (Button) tab;
                label.setTextSize(13); label.setMinWidth(0); label.setMinimumWidth(0);
                label.setMaxLines(2);
            }
            tab.setPadding(dp(4), dp(4), dp(4), dp(4));
            LinearLayout.LayoutParams tabLayout = new LinearLayout.LayoutParams(0, dp(56), 1);
            tabLayout.setMargins(0, 0, 0, 0);
            tabs.addView(tab, tabLayout);
        }
        shell.addView(tabs, full());
        setContentView(shell);
        updateTabs();
        getWindow().setLayout(-1, -1);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        enableValues(false);
        product.addTextChangedListener(new TextWatcher() {
            private boolean appended;
            private String previous;
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                previous = s.toString();
            }
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!settingText) appended = count > 0 && start + count == s.length();
            }
            public void afterTextChanged(Editable text) {
                if (settingText) return;
                if (text.toString().equals(previous)) return;
                if (selected != null) editedFromProductId = selected.optInt("id", -1);
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
        loadCatalog(true);
        if (state != null && state.getBoolean("showingId")) showIdPage(state);
    }

    private void invalidateSelection() {
        completionEcho.reset();
        generation++;
        if (pendingSearch != null) main.removeCallbacks(pendingSearch);
        selected = null;
        for (EditText field : prices) field.setText("");
        enableValues(false);
        for (EditText field : quantities) field.setText("");
        product.dismissDropDown();
        product.setAdapter(new ArrayAdapter<Product>(this, android.R.layout.simple_dropdown_item_1line, new ArrayList<Product>()));
    }

    private void search(String query, int current, boolean complete) {
        if (catalog == null || catalog.size() == 0) { message("No local products. Set the POS address, then tap Refresh.", false); return; }
        java.util.List<Product> matches = selectedCategory == null ? new ArrayList<>() : catalog.search(query, selectedCategory);
        if (ProductAutoSelection.shouldSelect(matches.size(), matches.isEmpty() ? -1 : matches.get(0).id, editedFromProductId)) {
            Product match = matches.get(0);
            generation++;
            if (pendingSearch != null) main.removeCallbacks(pendingSearch);
            completionEcho.reset();
            try {
                fillProduct(match.row());
                product.dismissDropDown();
                message("Product selected automatically: " + match.name, false);
            } catch (Exception ex) { message("Invalid cached product. Tap Refresh.", false); }
            return;
        }
        Editable input = product.getText();
        boolean uniqueCurrentProduct = matches.size() == 1 && editedFromProductId != null
                && matches.get(0).id == editedFromProductId;
        if (!uniqueCurrentProduct && complete && product.hasFocus() && product.getSelectionStart() == input.length()
                && product.getSelectionEnd() == input.length()
                && android.view.inputmethod.BaseInputConnection.getComposingSpanStart(input) < 0) {
            String typed = input.toString();
            String completed = ProductCatalog.complete(typed, matches);
            if (!completed.equals(typed)) {
                settingText = true;
                try {
                    product.setText(completed, false);
                    product.setSelection(completed.length());
                    completionEcho.arm(typed, completed, android.os.SystemClock.uptimeMillis());
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
        if (movingPlan != null) movingPlan.setExternalBusy(value && !optimisticLocalPending);
        product.setEnabled(!value);
        category.setEnabled(!value && !categories.isEmpty());
        connection.setEnabled(!value);
        refresh.setEnabled(!value);
        moving.setEnabled(!value);
        updateTabs();
        enableValues(!value && selected != null);
    }

    private void loadCatalog() {
        loadCatalog(false);
    }

    private void loadCatalog(boolean refreshAfterLoad) {
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
                // Load the cache first for offline fallback, then fetch current POS data.
                if (refreshAfterLoad && !base.isEmpty()) {
                    refreshCatalog();
                    return;
                }
                if (requestedProductId != null) {
                    int id = requestedProductId; requestedProductId = null;
                    selectProductById(id);
                }
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
                if (showingMoving && movingPlan != null) {
                    if (result != null) movingPlan.activate();
                } else { product.requestFocus(); product.selectAll(); }
                message(text, true);
            });
        });
    }

    private void fillProduct(JSONObject row) {
        settingValues = true;
        try {
            selected = row;
            editedFromProductId = null;
            settingText = true;
            String name = row.optString("mnemonic", "").trim();
            if (name.isEmpty()) name = row.optString("subject", "").trim();
            if (name.isEmpty()) name = row.optString("code", "");
            product.setText(name, false);
            settingText = false;
            for (int i = 0; i < keys.length; i++) quantities[i].setText(String.valueOf(row.getInt(keys[i])));
            for (int i = 0; i < priceKeys.length; i++) {
                String amount = ProductCatalog.money(row, priceKeys[i]);
                prices[i].setText(amount.equals("—") ? "" : amount);
                prices[i].setHint(row.has(priceKeys[i]) ? "Not set" : "Refresh");
            }
            enableValues(true);
            message("Changes save automatically when you leave a field.", false);
        } catch (Exception ex) { settingText = false; invalidateSelection(); message(ex.getMessage(), false); }
        finally { settingValues = false; }
    }

    private static final class AutoSaveRequest {
        final int productId;
        final EditText field;
        final String key;
        final boolean price;
        AutoSaveRequest(int productId, EditText field, String key, boolean price) {
            this.productId = productId; this.field = field; this.key = key; this.price = price;
        }
    }

    private JSONObject autoSavePayload(AutoSaveRequest request) throws Exception {
        JSONObject payload = new JSONObject().put("productId", request.productId);
        JSONObject values = new JSONObject(), expected = new JSONObject();
        String edited = request.field.getText().toString().trim();
        for (String key : keys) {
            int value = selected.getInt(key);
            if (!request.price && request.key.equals(key)) {
                if (!edited.matches("-?[0-9]+")) throw new IllegalArgumentException("Enter a whole number");
                try { value = Integer.parseInt(edited); }
                catch (NumberFormatException ex) { throw new IllegalArgumentException("Quantity is out of range"); }
                if (value == Integer.MIN_VALUE) throw new IllegalArgumentException("Quantity is out of range");
            }
            values.put(key, value);
            expected.put(key, selected.getInt(key));
        }
        if (request.price) {
            values.put(request.key, ProductCatalog.moneyCents(edited));
            expected.put(request.key, selected.get(request.key));
        }
        return payload.put("values", values).put("expected", expected);
    }

    private void startAutoSave(AutoSaveRequest request) {
        startAutoSave(request, null);
    }

    private void startAutoSave(AutoSaveRequest request, JSONObject retryPayload) {
        if (saving || selected == null || selected.optInt("id", -1) != request.productId) return;
        final JSONObject payload;
        try {
            payload = retryPayload == null ? autoSavePayload(request) : new JSONObject(retryPayload.toString());
        } catch (Exception ex) {
            request.field.setError(ex.getMessage());
            request.field.requestFocus();
            message(ex.getMessage(), false);
            return;
        }
        autoSaveInFlight = request;
        // Apply the user's edit locally before the network call. Stock Moving must
        // calculate from what the user entered, even while POS sync is in flight.
        try {
            JSONObject optimistic = new JSONObject(selected.toString());
            JSONObject values = payload.getJSONObject("values");
            for (String key : keys) if (values.has(key)) optimistic.put(key, values.get(key));
            ProductCatalog localUpdated = catalog.updated(optimistic);
            persistCatalog(server, localUpdated);
            catalog = localUpdated;
            selected = optimistic;
            optimisticLocalPending = true;
            if (movingPlan != null) movingPlan.invalidateForInventoryChange();
            message("Local inventory updated. Calculating Stock Moving from the new values…", false);
        } catch (Exception ex) {
            autoSaveInFlight = null;
            message("Could not update the local inventory before syncing: " + detail(ex), false);
            return;
        }
        productSaveBusy(true);
        product.dismissDropDown();
        generation++;
        if (pendingSearch != null) main.removeCallbacks(pendingSearch);
        message("Saving " + request.field.getContentDescription() + "…", false);
        String base = server;
        ProductCatalog snapshot = catalog;
        String submittedText = request.field.getText().toString().trim();
        io.execute(() -> {
            String feedback;
            ProductCatalog updated = null;
            JSONObject storedProduct = null;
            boolean verified = false;
            try {
                main.post(() -> { if (!closed) message("Preparing local recovery state…", false); });
                // Write the recovery marker before sending: a process death must never silently
                // expose pre-submit quantities as current after a potentially successful update.
                if (!getSharedPreferences("product_catalogs", MODE_PRIVATE).edit()
                        .putBoolean("pending:" + base, true).commit())
                    throw new IOException("Unable to save recovery state. Nothing was submitted.");
                JSONObject submitted = payload;
                boolean stockOnlyCompatibility = false;
                main.post(() -> { if (!closed) message("Saving changes to AikaPos…", false); });
                try {
                    request(base, "/AikaPos/stockProducts", payload);
                } catch (RequestException failure) {
                    if (!ProductSyncCompatibility.needsStockOnlyRetry(failure.code, failure.getMessage(), payload))
                        throw failure;
                    submitted = ProductSyncCompatibility.stockOnly(payload);
                    request(base, "/AikaPos/stockProducts", submitted);
                    stockOnlyCompatibility = true;
                }
                main.post(() -> { if (!closed) message("AikaPos saved the change. Verifying the saved product…", false); });
                JSONObject stored = request(base, "/AikaPos/stockProducts?id="
                        + payload.getInt("productId"), null).getJSONObject("product");
                ProductSyncVerification.verify(submitted, stored);
                verified = true;
                storedProduct = stored;
                try {
                    main.post(() -> { if (!closed) message("AikaPos verified. Updating the local inventory cache…", false); });
                    ProductCatalog saved = snapshot.updated(stored);
                    updated = saved;
                    persistCatalog(base, saved);
                    main.post(() -> { if (!closed) message("Local inventory updated. Recalculating Stock Moving on next open…", false); });
                    feedback = (stockOnlyCompatibility
                            ? "Stock saved and verified. This AikaPos version did not save Cost/Price. "
                            : "Saved and verified on AikaPos. ")
                            + "Stock 1: " + stored.getInt("stock1") + ", Stock 2: "
                            + stored.getInt("stock2") + ", Stock 3: " + stored.getInt("stock3");
                } catch (Exception ex) {
                    feedback = "POS updated and verified, but saving the local product cache failed: "
                            + detail(ex) + ". Tap Refresh before continuing.";
                }
            } catch (Exception ex) {
                feedback = "Product sync failed: " + detail(ex) + ". Tap Refresh to load the current stock.";
            }
            String finalFeedback = feedback;
            ProductCatalog finalUpdated = updated;
            JSONObject finalStoredProduct = storedProduct;
            boolean finalVerified = verified;
            main.post(() -> {
                if (closed) return;
                if (finalUpdated != null && finalStoredProduct != null) {
                    catalog = finalUpdated;
                    selected = finalStoredProduct;
                    // Stock Moving keeps its own calculated input/draft while it
                    // is hidden.  Invalidate that snapshot after a verified edit
                    // so the next tab switch cannot calculate from old Manque.
                    if (movingPlan != null) movingPlan.invalidateForInventoryChange();
                    if (request.field.getText().toString().trim().equals(submittedText)) {
                        settingValues = true;
                        try {
                            request.field.setText(request.price
                                    ? (finalStoredProduct.isNull(request.key) ? "" : ProductCatalog.money(finalStoredProduct, request.key))
                                    : String.valueOf(finalStoredProduct.getInt(request.key)));
                        } catch (Exception ignored) {}
                        finally { settingValues = false; }
                    }
                }
                productSaveBusy(false);
                autoSaveInFlight = null;
                optimisticLocalPending = false;
                if (!finalVerified) {
                    failedAutoSave = request;
                    failedAutoSavePayload = payload;
                } else {
                    failedAutoSave = null;
                    failedAutoSavePayload = null;
                }
                if (showingMoving && movingPlan != null && finalUpdated != null && finalVerified) {
                    movingPlan.activate();
                }
                if (!finalVerified) messageRetry(finalFeedback + "  Tap here to retry sync.");
                else message(finalFeedback, false);
                if (!finalVerified) new AlertDialog.Builder(this).setTitle("Sync not confirmed")
                        .setMessage(finalFeedback + "\n\nPOS: " + base)
                        .setPositiveButton("Refresh", (dialog, which) -> refreshCatalog())
                        .setNegativeButton("Close", null).show();
                main.post(this::drainAutoSaves);
                if (openMovingAfterSave && pendingAutoSaves.isEmpty() && finalUpdated != null && finalVerified) {
                    openMovingAfterSave = false;
                    main.post(this::openMovingPlans);
                } else if (openMovingAfterSave && pendingAutoSaves.isEmpty()) {
                    openMovingAfterSave = false;
                    message("Product sync did not complete. Stock Moving was not opened; refresh or retry before recalculating.", false);
                }
            });
        });
    }

    private static String detail(Exception ex) {
        String message = ex.getMessage();
        return message == null || message.trim().isEmpty() ? ex.getClass().getSimpleName() : message;
    }

    private void messageRetry(String text) {
        message(text, false);
        status.setTextColor(0xff087f8c);
        status.setOnClickListener(v -> {
            if (failedAutoSave != null && failedAutoSavePayload != null && !saving) {
                AutoSaveRequest retry = failedAutoSave;
                JSONObject payload = failedAutoSavePayload;
                failedAutoSave = null;
                failedAutoSavePayload = null;
                startAutoSave(retry, payload);
            }
        });
    }

    private void productSaveBusy(boolean value) {
        saving = value;
        if (movingPlan != null) movingPlan.setExternalBusy(value);
        product.setEnabled(!value);
        category.setEnabled(!value && !categories.isEmpty());
        connection.setEnabled(!value);
        refresh.setEnabled(!value);
        moving.setEnabled(!value || optimisticLocalPending);
        updateTabs();
        // Keep the six value fields enabled and visible while the network request runs.
    }

    private void drainAutoSaves() {
        if (saving || selected == null) return;
        java.util.Iterator<java.util.Map.Entry<String, AutoSaveRequest>> iterator = pendingAutoSaves.entrySet().iterator();
        while (iterator.hasNext()) {
            AutoSaveRequest request = iterator.next().getValue();
            iterator.remove();
            if (request.productId != selected.optInt("id", -1)) continue;
            if (!ProductFieldChange.changed(selected, request.key, request.field.getText().toString(), request.price)) continue;
            startAutoSave(request);
            return;
        }
    }

    private void showRefreshProgress(String text, int percent) {
        refreshProgressText.setText(text);
        refreshProgressBar.setIndeterminate(percent < 0);
        if (percent >= 0) refreshProgressBar.setProgress(percent);
    }

    private interface DownloadProgress {
        void update(String text, int percent);
    }

    static JSONObject request(String base, String path, JSONObject body) throws Exception {
        return request(base, path, body, null);
    }

    private static JSONObject request(String base, String path, JSONObject body, DownloadProgress progress) throws Exception {
        HttpURLConnection c = (HttpURLConnection) new URL(base + path).openConnection();
        c.setConnectTimeout(5000);
        c.setReadTimeout(10000);
        c.setInstanceFollowRedirects(false);
        c.setUseCaches(false);

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
            catch (Exception ex) {
                if (code >= 400) throw new RequestException(code, "HTTP " + code + ": " + httpError(code));
                throw new IOException("HTTP " + code + ": Check that POS supports the inventory API");
            }
            if (code != 200) throw new RequestException(code, "HTTP " + code + ": "
                    + result.optString("error", httpError(code)));
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
        selectAllOnFocus(address);

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
                    if (movingPlan != null) { movingPlan.dispose(); movingPlan = null; }
                    if (movingPage != null) { pages.removeView(movingPage); movingPage = null; }
                    showingMoving = false;
                    showingId = false;
                    if (idPage != null) idPage.setVisibility(View.GONE);
                    if (idChecking != null) idChecking.deactivate();
                    editingPage.setVisibility(View.VISIBLE);
                    updateTabs();
                    loadCatalog(true);
                    dialog.dismiss();
                    Toast.makeText(this, "Connection saved: " + server, Toast.LENGTH_LONG).show();
                    product.requestFocus(); product.selectAll();
                });
            });
        }));
        dialog.show();
    }

    private void openMovingPlans() {
        if ((saving && !optimisticLocalPending) || showingMoving || (idChecking != null && idChecking.isBusy())
                || (movingPlan != null && movingPlan.isBusy())) return;
        if (!optimisticLocalPending && (autoSaveInFlight != null || !pendingAutoSaves.isEmpty())) {
            openMovingAfterSave = true;
            message("Saving the product changes to AikaPos first… Stock Moving will calculate after sync completes.", false);
            drainAutoSaves();
            return;
        }
        if (server.isEmpty()) { configure(); return; }
        product.dismissDropDown();
        generation++;
        if (pendingSearch != null) main.removeCallbacks(pendingSearch);
        hideKeyboard();
        if (movingPlan == null) {
            String base = server;
            movingPlan = new LocalMovingPlanPage(this, base, () -> {
                if (!getSharedPreferences("product_catalogs", MODE_PRIVATE).edit()
                        .putBoolean("pending:" + base, true).commit())
                    throw new IOException("Unable to save recovery state. Nothing was submitted.");
            }, id -> showEditingPage(id), this::updateTabs);
            movingPage = movingPlan.createView();
            pages.addView(movingPage, new FrameLayout.LayoutParams(-1, -1));
        }
        showingMoving = true;
        showingStocking = false;
        showingId = false;
        if (idPage != null) idPage.setVisibility(View.GONE);
        if (stockingPage != null) stockingPage.setVisibility(View.GONE);
        if (idChecking != null) idChecking.deactivate();
        editingPage.setVisibility(View.GONE); movingPage.setVisibility(View.VISIBLE);
        updateTabs();
        movingPlan.activate();
    }

    private void showStockingPage() {
        if (saving || showingStocking || (movingPlan != null && movingPlan.isBusy())
                || (idChecking != null && idChecking.isBusy())) return;
        if (stockingPage == null) {
            stockingPlan = new StockingPlanPage(this, server, () -> {
                String saved = getSharedPreferences("product_catalogs", MODE_PRIVATE).getString(server, "[]");
                return new JSONArray(saved);
            }, plan -> {
                JSONObject result = request(server, "/AikaPos/stockingPlan", plan);
                if (result.optInt("billId", -1) < 0) throw new IOException("POS did not return a stocking plan ID");
            });
            stockingPage = stockingPlan.view();
            pages.addView(stockingPage, new FrameLayout.LayoutParams(-1, -1));
        }
        showingMoving = false; showingStocking = true; showingId = false;
        if (movingPage != null) movingPage.setVisibility(View.GONE);
        if (idPage != null) idPage.setVisibility(View.GONE);
        editingPage.setVisibility(View.GONE); stockingPage.setVisibility(View.VISIBLE);
        stockingPlan.activate();
        if (idChecking != null) idChecking.deactivate();
        updateTabs();
    }

    private void showEditingPage(Integer productId) {
        if (saving || (idChecking != null && idChecking.isBusy()) || (movingPlan != null && movingPlan.isBusy())) return;
        if (!showingMoving && !showingStocking && !showingId && productId == null) return;
        if (movingPlan != null && !movingPlan.flushQuantity()) return;
        showingMoving = false;
        showingStocking = false;
        showingId = false;
        if (idPage != null) idPage.setVisibility(View.GONE);
        if (idChecking != null) idChecking.deactivate();
        if (movingPlan != null) movingPlan.deactivate();
        if (movingPage != null) movingPage.setVisibility(View.GONE);
        if (stockingPage != null) stockingPage.setVisibility(View.GONE);
        editingPage.setVisibility(View.VISIBLE);
        hideKeyboard(); updateTabs();
        if (movingPlan != null && movingPlan.consumeInventoryChange()) {
            requestedProductId = productId;
            loadCatalog();
        } else if (productId != null) selectProductById(productId);
    }

    private void showIdPage(Bundle saved) {
        if (saved == null && (saving || (movingPlan != null && movingPlan.isBusy())
                || (idChecking != null && idChecking.isBusy()))) return;
        if (showingId) {
            if (saved == null) idChecking.capture();
            return;
        }
        if (showingMoving && movingPlan != null && !movingPlan.flushQuantity()) return;
        product.dismissDropDown();
        generation++;
        if (pendingSearch != null) main.removeCallbacks(pendingSearch);
        hideKeyboard();
        if (movingPlan != null) movingPlan.deactivate();
        if (idChecking == null) {
            idChecking = new IdCheckingPage(this, this::updateTabs, () -> {
                java.util.List<PhotoProductMatcher.Item> items = new ArrayList<>();
                if (catalog != null) for (Product item : catalog.all()) {
                    items.add(new PhotoProductMatcher.Item(item.id, item.name, item.category, item.code,
                            ProductCatalog.matchesCategory(selectedCategory, item.category)));
                }
                return items;
            }, this::showEditingPage, this::showEditingSearch);
            idPage = idChecking.createView(saved);
            pages.addView(idPage, new FrameLayout.LayoutParams(-1, -1));
        }
        showingMoving = false;
        showingId = true;
        editingPage.setVisibility(View.GONE);
        if (movingPage != null) movingPage.setVisibility(View.GONE);
        idPage.setVisibility(View.VISIBLE);
        idChecking.activate();
        updateTabs();
        if (saved == null) idChecking.capture();
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, android.content.Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (idChecking != null) idChecking.onActivityResult(requestCode, resultCode, data);
    }

    @Override protected void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);
        state.putBoolean("showingId", showingId);
        if (idChecking != null) idChecking.onSaveInstanceState(state);
    }

    @Override protected void onResume() {
        super.onResume();
        if (showingId && idChecking != null) idChecking.activate();
    }

    private void selectProductById(int id) {
        if (catalog != null) for (Product item : catalog.all()) {
            if (item.id != id) continue;
            invalidateSelection();
            selectedCategory = item.category;
            int position = categories.indexOf(item.category);
            if (position >= 0) category.setSelection(position);
            try { fillProduct(item.row()); }
            catch (Exception ex) { message("Unable to open product. Tap Refresh.", true); }
            ((ScrollView) editingPage).smoothScrollTo(0, 0);
            return;
        }
        invalidateSelection();
        message("Product is not available in verified local inventory. Tap Refresh.", true);
    }

    private void hideKeyboard() {
        ((android.view.inputmethod.InputMethodManager) getSystemService(INPUT_METHOD_SERVICE))
                .hideSoftInputFromWindow(pages.getWindowToken(), 0);
    }

    static void styleDropdown(Activity activity, Spinner spinner) {
        float density = activity.getResources().getDisplayMetrics().density;
        android.graphics.drawable.GradientDrawable border = new android.graphics.drawable.GradientDrawable();
        border.setColor(0xffffffff); border.setCornerRadius(12 * density);
        border.setStroke(Math.max(1, Math.round(density)), 0xffb6d5df);
        // Keep the platform dropdown arrow above the new outline.
        android.graphics.drawable.Drawable arrow = spinner.getBackground();
        spinner.setBackground(new android.graphics.drawable.LayerDrawable(
                new android.graphics.drawable.Drawable[]{border, arrow}));
    }

    private void updateTabs() {
        if (editingTab == null) return;
        boolean enabled = !saving && (movingPlan == null || !movingPlan.isBusy())
                && (idChecking == null || !idChecking.isBusy());
        editingTab.setEnabled(enabled); moving.setEnabled(enabled); stocking.setEnabled(enabled); idTab.setEnabled(enabled);
        connection.setEnabled(enabled); refresh.setEnabled(enabled);
        View[] tabs = {editingTab, moving, stocking, idTab};
        int[] tabColors = {0xff1976d2, 0xffef8b2c, 0xff2e9d61, 0xff8e5bb7};
        for (int i = 0; i < tabs.length; i++) {
            boolean active = i == (showingId ? 3 : showingStocking ? 2 : showingMoving ? 1 : 0);
            tabs[i].setSelected(active);
            if (tabs[i] instanceof Button) ((Button) tabs[i]).setTypeface(
                    android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.NORMAL));
            android.graphics.drawable.GradientDrawable background = new android.graphics.drawable.GradientDrawable();
            float bottomRadius = dp(14);
            background.setCornerRadii(new float[]{0, 0, 0, 0,
                    bottomRadius, bottomRadius, bottomRadius, bottomRadius});
            int tabColor = tabColors[i];
            if (active) background.setColors(new int[]{tabColor, darken(tabColor, 0.72f)});
            else background.setColor(blend(tabColor, 0xffffffff, 0.86f));
            background.setOrientation(android.graphics.drawable.GradientDrawable.Orientation.LEFT_RIGHT);
            int color = active ? 0xffffffff : darken(tabColor, 0.78f);
            if (tabs[i] instanceof Button) ((Button) tabs[i]).setTextColor(color);
            else ((ImageButton) tabs[i]).setImageTintList(android.content.res.ColorStateList.valueOf(color));
            tabs[i].setElevation(dp(active ? 3 : 0));
            tabs[i].setBackground(new android.graphics.drawable.RippleDrawable(
                    android.content.res.ColorStateList.valueOf(0x2270dccc), background, null));
        }
    }

    private static int darken(int color, float factor) {
        int r = (int) (((color >> 16) & 0xff) * factor);
        int g = (int) (((color >> 8) & 0xff) * factor);
        int b = (int) ((color & 0xff) * factor);
        return 0xff000000 | (r << 16) | (g << 8) | b;
    }

    private static int blend(int foreground, int background, float backgroundWeight) {
        float foregroundWeight = 1f - backgroundWeight;
        int r = (int) (((foreground >> 16) & 0xff) * foregroundWeight + ((background >> 16) & 0xff) * backgroundWeight);
        int g = (int) (((foreground >> 8) & 0xff) * foregroundWeight + ((background >> 8) & 0xff) * backgroundWeight);
        int b = (int) ((foreground & 0xff) * foregroundWeight + (background & 0xff) * backgroundWeight);
        return 0xff000000 | (r << 16) | (g << 8) | b;
    }

    @Override public void onBackPressed() {
        if (showingMoving || showingId) { showEditingPage(null); return; }
        super.onBackPressed();
    }

    static final class RequestException extends IOException {
        final int code;
        RequestException(int code, String message) { super(message); this.code = code; }
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
        selectAllOnFocus(filter);
        styleInput(filter);
        filter.setEnabled(false);
        content.addView(filter, full());
        ListView list = new ListView(this);
        list.setDivider(new android.graphics.drawable.ColorDrawable(0xffd9ecef));
        list.setDividerHeight(dp(1));
        HorizontalScrollView tableScroll = new HorizontalScrollView(this);
        tableScroll.setFillViewport(true);
        LinearLayout table = column();
        table.addView(localInventoryRow(new String[]{"Product", "Category", "Stock\n1", "Stock\n2", "Stock\n3", "Cost", "Price"}, true), full());
        table.addView(list, new LinearLayout.LayoutParams(-1, 0, 1));
        TextView empty = new TextView(this);
        empty.setText("No matching products.");
        empty.setPadding(dp(12), dp(24), dp(12), dp(24));
        empty.setVisibility(View.GONE);
        table.addView(empty, full());
        list.setEmptyView(empty);
        tableScroll.addView(table, new android.widget.FrameLayout.LayoutParams(dp(612), -1));
        LinearLayout.LayoutParams tableLayout = new LinearLayout.LayoutParams(-1, 0, 1);
        tableLayout.topMargin = dp(12);
        content.addView(tableScroll, tableLayout);
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
                    summary.setText(saved.size() + " saved products · Swipe table for more columns"
                            + (pending ? "\nAn update was interrupted. Tap Refresh to verify these saved values."
                            : saved.size() == 0 ? "\nTap Refresh on the main screen to download products." : ""));
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
        java.util.List<String[]> items = new ArrayList<>();
        for (Product item : rows) {
            String[] entry = {item.name, item.category, "—", "—", "—", "—", "—"};
            try {
                JSONObject row = item.row();
                for (int i = 0; i < 3; i++) entry[i + 2] = String.valueOf(row.getInt("stock" + (i + 1)));
                entry[5] = ProductCatalog.money(row, "cost");
                entry[6] = ProductCatalog.money(row, "price");
            } catch (Exception ex) { entry[0] += " (Invalid record — refresh)"; }
            items.add(entry);
        }
        list.setAdapter(new BaseAdapter() {
            public int getCount() { return items.size(); }
            public Object getItem(int position) { return items.get(position); }
            public long getItemId(int position) { return position; }
            public View getView(int position, View recycled, android.view.ViewGroup parent) {
                LinearLayout row = recycled instanceof LinearLayout ? (LinearLayout) recycled
                        : localInventoryRow(new String[7], false);
                String[] values = items.get(position);
                for (int i = 0; i < values.length; i++) {
                    TextView cell = (TextView) row.getChildAt(i);
                    cell.setText(values[i]);
                    cell.setTooltipText(values[i]);
                }
                row.setBackgroundColor(position % 2 == 0 ? 0xffffffff : 0xffedf7f8);
                return row;
            }
        });
    }

    private void showEditingSearch(String query) {
        showEditingPage(null);
        editedFromProductId = null;
        invalidateSelection();
        if (catalog != null) {
            String beerCategory = catalog.defaultCategory();
            int beerPosition = categories.indexOf(beerCategory);
            if (beerPosition >= 0) {
                selectedCategory = beerCategory;
                category.setSelection(beerPosition);
            }
        }
        String value = query == null ? "" : query.trim();
        settingText = true;
        product.setText(value);
        product.setSelection(value.length());
        settingText = false;
        product.requestFocus();
        if (!value.isEmpty()) search(value, generation, false);
        else message("Beer detected. Enter its barcode or product name.", false);
    }

    private LinearLayout localInventoryRow(String[] values, boolean heading) {
        LinearLayout row = new LinearLayout(this);
        row.setGravity(android.view.Gravity.CENTER_VERTICAL);
        row.setMinimumHeight(dp(52));
        int[] widths = {200, 96, 52, 52, 52, 80, 80};
        if (heading) row.setBackgroundColor(0xffdceff2);
        for (int i = 0; i < widths.length; i++) {
            TextView cell = new TextView(this);
            cell.setText(values[i]);
            cell.setTextSize(heading ? 12 : 14);
            cell.setTextColor(0xff16344b);
            cell.setPadding(dp(6), dp(8), dp(6), dp(8));
            cell.setGravity(android.view.Gravity.CENTER_VERTICAL | (i < 2 ? android.view.Gravity.LEFT
                    : i < 5 ? android.view.Gravity.CENTER_HORIZONTAL : android.view.Gravity.RIGHT));
            if (i < 2) {
                cell.setMaxLines(2);
                cell.setEllipsize(android.text.TextUtils.TruncateAt.END);
            }
            if (heading) cell.setTypeface(null, android.graphics.Typeface.BOLD);
            else if (i >= 2) cell.setTypeface(android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.NORMAL));
            row.addView(cell, new LinearLayout.LayoutParams(dp(widths[i]), -2));
        }
        return row;
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
        quantities[index] = input;
        input.setOnFocusChangeListener((view, focused) -> {
            if (focused) input.post(() -> { if (input.hasFocus()) input.selectAll(); });
            else autoSaveIfChanged(input, keys[index], false);
        });
        field.addView(input, inputLayout); return field;
    }
    private LinearLayout priceField(int index, String name) {
        LinearLayout field = column();
        field.setPadding(dp(4), dp(16), dp(4), dp(8));
        TextView label = new TextView(this); label.setText(name); field.addView(label);
        EditText input = new EditText(this); input.setSingleLine(); styleInput(input);
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        input.setSelectAllOnFocus(true); input.setContentDescription(name);
        prices[index] = input;
        input.setOnFocusChangeListener((view, focused) -> {
            if (focused) input.post(() -> { if (input.hasFocus()) input.selectAll(); });
            else autoSaveIfChanged(input, priceKeys[index], true);
        });
        field.addView(input, full()); return field;
    }
    private void autoSaveIfChanged(EditText field, String key, boolean price) {
        if (settingValues || selected == null || !field.isEnabled()
                || !ProductFieldChange.changed(selected, key, field.getText().toString(), price)) return;
        AutoSaveRequest request = new AutoSaveRequest(selected.optInt("id", -1), field, key, price);
        pendingAutoSaves.put(key, request);
        main.post(this::drainAutoSaves);
    }
    private void enableValues(boolean enabled) {
        for (EditText field : quantities) field.setEnabled(enabled);
        for (int i = 0; i < prices.length; i++) prices[i].setEnabled(enabled && selected != null && selected.has(priceKeys[i]));
    }
    private void styleInput(EditText input) {
        android.graphics.drawable.StateListDrawable backgrounds = new android.graphics.drawable.StateListDrawable();
        int[][] states = {new int[]{-android.R.attr.state_enabled}, new int[]{android.R.attr.state_focused}, new int[]{}};
        int[] borders = {0xffcde5eb, 0xff087f8c, 0xffb6d5df};
        for (int i = 0; i < states.length; i++) {
            android.graphics.drawable.GradientDrawable box = new android.graphics.drawable.GradientDrawable();
            box.setColor(i == 0 ? 0xffe4f1f5 : 0xffffffff);
            box.setCornerRadius(dp(12));
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
            case 500: return "POS reached the moving-plan service, but the service failed internally. Check the POS server log; the phone did submit the request.";
            default: return "Server request failed. Check POS and reload stock before retrying.";
        }
    }
    private void message(String text, boolean toast) {
        status.setText(text);
        status.setTextColor(0xff16344b);
        status.setOnClickListener(null);
        if (toast) Toast.makeText(this, text, Toast.LENGTH_LONG).show();
    }
    private ImageButton toolbarIcon(int drawable, String label) {
        ImageButton button = new ImageButton(this);
        button.setImageResource(drawable);
        button.setImageTintList(new android.content.res.ColorStateList(
                new int[][]{new int[]{-android.R.attr.state_enabled}, new int[]{}},
                new int[]{0xff688797, 0xffd8faf4}));
        button.setContentDescription(label);
        button.setTooltipText(label);
        button.setScaleType(ImageView.ScaleType.CENTER);
        android.util.TypedValue background = new android.util.TypedValue();
        getTheme().resolveAttribute(android.R.attr.selectableItemBackgroundBorderless, background, true);
        button.setBackgroundResource(background.resourceId);
        return button;
    }
    private void selectAllOnFocus(EditText input) {
        input.setSelectAllOnFocus(true);
        input.setOnFocusChangeListener((view, focused) -> {
            if (focused) input.post(() -> { if (input.hasFocus()) input.selectAll(); });
        });
    }
    static void stylePrimaryButton(Activity activity, Button button) {
        float density = activity.getResources().getDisplayMetrics().density;
        android.graphics.drawable.GradientDrawable enabled = new android.graphics.drawable.GradientDrawable(
                android.graphics.drawable.GradientDrawable.Orientation.LEFT_RIGHT, new int[]{0xff087f8c, 0xff146783});
        enabled.setCornerRadius(12 * density);
        android.graphics.drawable.GradientDrawable disabled = new android.graphics.drawable.GradientDrawable();
        disabled.setColor(0xffcce5ea); disabled.setCornerRadius(12 * density);
        android.graphics.drawable.StateListDrawable backgrounds = new android.graphics.drawable.StateListDrawable();
        backgrounds.addState(new int[]{-android.R.attr.state_enabled}, disabled);
        backgrounds.addState(new int[]{}, enabled);
        button.setBackground(new android.graphics.drawable.RippleDrawable(
                android.content.res.ColorStateList.valueOf(0x3370dccc), backgrounds, null));
        button.setTextColor(new android.content.res.ColorStateList(
                new int[][]{new int[]{-android.R.attr.state_enabled}, new int[]{}}, new int[]{0xff4b7180, 0xffffffff}));
        button.setTypeface(android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.NORMAL));
        button.setAllCaps(false);
        button.setMinHeight(Math.round(52 * density));
    }
    private LinearLayout column() { LinearLayout view = new LinearLayout(this); view.setOrientation(LinearLayout.VERTICAL); return view; }
    private LinearLayout.LayoutParams full() { return new LinearLayout.LayoutParams(-1, -2); }
    private int dp(int value) { return Math.round(value * getResources().getDisplayMetrics().density); }
    @Override protected void onDestroy() {
        closed = true;
        if (movingPlan != null) movingPlan.dispose();
        if (idChecking != null) idChecking.dispose();
        main.removeCallbacksAndMessages(null); io.shutdown(); super.onDestroy();
    }
}
