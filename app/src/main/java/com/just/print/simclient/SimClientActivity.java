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
    private static final int PRODUCT_PHOTO_REQUEST = 2401;
    private static final int PRODUCT_BACKUP_REQUEST = 2402;
    private static final int PRODUCT_IMPORT_REQUEST = 2403;
    private final Handler main = new Handler(Looper.getMainLooper());
    private final ExecutorService io = Executors.newSingleThreadExecutor();
    private AutoCompleteTextView product;
    private Button editProductName;
    private boolean editingProductName;
    private Spinner category;
    private Button editCategory;
    private boolean editingCategory;
    private java.util.List<String> categories = new ArrayList<>();
    private String selectedCategory;
    private final EditText[] quantities = new EditText[4];
    private final String[] keys = {"stock1", "stock2", "stock3", "monque"};
    private TextView status;
    private TextView weeklySales;
    private TextView priceWithTax;
    private TextView totalNumber;
    private TextView totalPrice;
    private TextView cartButton;
    private TextView cartBadge;
    private AlertDialog cartDialog;
    private LinearLayout cartContent;
    private final java.util.LinkedHashMap<Integer, CartLine> cart = new java.util.LinkedHashMap<>();
    private ImageView productPhoto;
    private File pendingProductPhoto;
    private String pendingProductBackup;
    private AlertDialog localInventoryDialog;
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
        // Recovery mode: keep the known POS address, but never download over the
        // phone's surviving catalog during startup.  The local catalog is the
        // source of truth until the operator explicitly starts a recovery.
        server = normalizeServerAddress(getPreferences(MODE_PRIVATE)
                .getString("server", "192.168.2.111"));
        // The activation code is the server-side personId/client namespace for
        // website-backed synchronization. It is intentionally kept separate
        // from the POS address and from any logged-in employee ID.
        LinearLayout root = column();
        int pad = dp(18);
        root.setPadding(pad, pad, pad, pad);
        // The compact totals row intentionally reaches into the top padding.
        // Allow its cart icon to remain fully visible instead of clipping its
        // upper edge at the content boundary.
        root.setClipChildren(false);
        root.setClipToPadding(false);
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
        editCategory = new Button(this);
        editCategory.setText("✎"); editCategory.setTextSize(24); editCategory.setAllCaps(false);
        editCategory.setScaleX(-1f);
        editCategory.setContentDescription("Edit category"); editCategory.setEnabled(false);
        editCategory.setOnClickListener(v -> { if (selected != null && !saving) { editingCategory = true; editCategory.setSelected(true); category.setEnabled(true); } });
        categoryBar.addView(editCategory, new LinearLayout.LayoutParams(dp(58), dp(48)));
        category.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onNothingSelected(AdapterView<?> parent) {}
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position >= categories.size()) return;
                String value = categories.get(position);
                if (value.equals(selectedCategory)) return;
                if (editingCategory && selected != null && !settingValues) {
                    editingCategory = false; editCategory.setSelected(false);
                    selectedCategory = value;
                    saveCategory(value);
                    return;
                }
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
            else {
                completionEcho.reset();
                if (editingProductName) {
                    editingProductName = false;
                    editProductName.setSelected(false);
                    product.setEnabled(false);
                    autoSaveIfChanged(product, "mnemonic", false, true);
                }
            }
        });
        styleInput(product);
        product.setTextSize(20);
        product.setMinHeight(dp(64));
        TextView productLabel = new TextView(this);
        productLabel.setText("Product name");
        productLabel.setIncludeFontPadding(false);
        productLabel.setPadding(0, 0, 0, 0);
        LinearLayout productRow = new LinearLayout(this);
        productRow.setGravity(android.view.Gravity.CENTER_VERTICAL);
        productRow.addView(product, new LinearLayout.LayoutParams(0, -2, 1));
        editProductName = new Button(this);
        editProductName.setText("✎");
        editProductName.setTextSize(24);
        editProductName.setAllCaps(false);
        editProductName.setScaleX(-1f);
        editProductName.setContentDescription("Edit product name");
        editProductName.setTooltipText("Edit product name");
        editProductName.setEnabled(false);
        editProductName.setOnClickListener(v -> {
            if (selected == null || saving) return;
            editingProductName = true;
            editProductName.setSelected(true);
            product.setEnabled(true);
            product.requestFocus();
            product.selectAll();
        });
        productRow.addView(editProductName, new LinearLayout.LayoutParams(dp(58), dp(64)));

        LinearLayout totals = new LinearLayout(this);
        totals.setGravity(android.view.Gravity.CENTER_VERTICAL | android.view.Gravity.RIGHT);
        totals.setPadding(0, 0, 0, 0);
        cartButton = new TextView(this);
        cartButton.setText("🛒");
        cartButton.setTextSize(25);
        cartButton.setGravity(android.view.Gravity.CENTER);
        cartButton.setIncludeFontPadding(false);
        cartButton.setPadding(0, 0, 0, 0);
        cartButton.setContentDescription("Show cart");
        cartButton.setClickable(true);
        cartButton.setOnClickListener(v -> showCartDialog());
        FrameLayout cartHolder = new FrameLayout(this);
        cartHolder.setClipChildren(false);
        cartHolder.addView(cartButton, new FrameLayout.LayoutParams(dp(36), dp(32)));
        cartBadge = new TextView(this);
        cartBadge.setTextSize(9);
        cartBadge.setTextColor(0xffffffff);
        cartBadge.setGravity(android.view.Gravity.CENTER);
        cartBadge.setBackground(roundBackground(0xffe53935, 0xffe53935, 20));
        FrameLayout.LayoutParams badgeParams = new FrameLayout.LayoutParams(dp(20), dp(20), android.view.Gravity.TOP | android.view.Gravity.LEFT);
        badgeParams.topMargin = dp(2);
        badgeParams.leftMargin = dp(3);
        cartHolder.addView(cartBadge, badgeParams);
        TextView addToCart = new TextView(this);
        addToCart.setText("Add to cart");
        addToCart.setTextSize(12);
        addToCart.setTextColor(0xff087f8c);
        addToCart.setGravity(android.view.Gravity.CENTER_VERTICAL);
        addToCart.setIncludeFontPadding(false);
        addToCart.setContentDescription("Add selected product to cart");
        addToCart.setTooltipText("Add selected product to cart");
        addToCart.setPaintFlags(addToCart.getPaintFlags() | android.graphics.Paint.UNDERLINE_TEXT_FLAG);
        addToCart.setPadding(0, 0, dp(8), 0);
        addToCart.setOnClickListener(v -> addSelectedProductToCart());
        totals.addView(addToCart, new LinearLayout.LayoutParams(-2, dp(32)));
        totals.addView(cartHolder, new LinearLayout.LayoutParams(dp(36), dp(32)));
        totalNumber = summaryText("QTY: 0", "Total number of products");
        totalNumber.setTextSize(12);
        totalNumber.setGravity(android.view.Gravity.CENTER_VERTICAL | android.view.Gravity.RIGHT);
        totalNumber.setTypeface(null, android.graphics.Typeface.NORMAL);
        totalNumber.setIncludeFontPadding(false);
        totalNumber.setPadding(0, 0, dp(8), 0);
        totalPrice = summaryText("Total: $0.00", "Total price of products");
        totalPrice.setTextSize(12);
        totalPrice.setGravity(android.view.Gravity.CENTER_VERTICAL | android.view.Gravity.RIGHT);
        totalPrice.setTypeface(null, android.graphics.Typeface.NORMAL);
        totalPrice.setIncludeFontPadding(false);
        totalPrice.setClickable(false);
        totalPrice.setTextColor(0xff48636d);
        totalPrice.setPadding(0, 0, dp(8), 0);
        LinearLayout.LayoutParams totalParams = new LinearLayout.LayoutParams(-2, dp(32));
        totalParams.leftMargin = dp(12);
        totals.addView(totalPrice, totalParams);
        LinearLayout.LayoutParams totalsParams = full();
        totalsParams.topMargin = -dp(14);
        totalsParams.bottomMargin = -dp(4);
        root.addView(totals, totalsParams);
        root.addView(productLabel, full());
        root.addView(productRow, full());
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
        LinearLayout salesAndTax = new LinearLayout(this);
        salesAndTax.setGravity(android.view.Gravity.CENTER_VERTICAL);
        weeklySales = summaryText("Weekly sales: —", "Weekly sales, read only");
        priceWithTax = summaryText("+ Tax: —", "Price with Tax, read only");
        salesAndTax.addView(weeklySales, new LinearLayout.LayoutParams(0, -2, 1));
        salesAndTax.addView(priceWithTax, new LinearLayout.LayoutParams(0, -2, 1));
        root.addView(salesAndTax, full());
        // Category belongs below all editable inventory fields and above the status text.
        LinearLayout.LayoutParams categoryParams = full();
        categoryParams.topMargin = dp(12);
        root.addView(categoryBar, categoryParams);

        // Reserve the large lower panel for the selected product photo.  Keep
        // it in the product page layout even before a photo is available so
        // the status message stays at the bottom instead of attaching itself
        // to the category controls.
        productPhoto = new ImageView(this);
        productPhoto.setContentDescription("Product photo");
        productPhoto.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        android.graphics.drawable.GradientDrawable photoBackground = new android.graphics.drawable.GradientDrawable();
        photoBackground.setColor(0xffe8f2f4);
        photoBackground.setStroke(dp(1), 0xffb7cdd2);
        productPhoto.setBackground(photoBackground);
        // Keep the frame above the bitmap. An opaque product image can cover
        // an ImageView background, so the outline belongs in the foreground.
        productPhoto.setForeground(roundBackground(0x00000000, 0xff8faeb5, 0));
        productPhoto.setClickable(true);
        productPhoto.setFocusable(true);
        productPhoto.setOnClickListener(v -> captureProductPhoto());
        LinearLayout.LayoutParams photoParams = new LinearLayout.LayoutParams(dp(180), dp(120));
        photoParams.topMargin = dp(16);
        photoParams.gravity = android.view.Gravity.CENTER_HORIZONTAL;
        root.addView(productPhoto, photoParams);

        // Consume any remaining viewport height so the message sits below the
        // photo area, with breathing room above the bottom divider.
        root.addView(new Space(this), new LinearLayout.LayoutParams(-1, 0, 1));
        status = new TextView(this);
        status.setTextSize(14);
        status.setGravity(android.view.Gravity.CENTER_VERTICAL);
        status.setMinHeight(dp(48));
        status.setPadding(0, dp(8), 0, dp(8));
        LinearLayout.LayoutParams statusParams = full();
        statusParams.bottomMargin = dp(20);
        status.setAccessibilityLiveRegion(View.ACCESSIBILITY_LIVE_REGION_POLITE);
        root.addView(status, statusParams);
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setClipToPadding(false);
        scroll.setBackgroundColor(0xfff0f8fa);
        scroll.addView(root);
        editingPage = scroll;
        LinearLayout shell = column();
        shell.addView(toolbar, full());
        pages = new FrameLayout(this);
        pages.addView(editingPage, new FrameLayout.LayoutParams(-1, -1));
        shell.addView(pages, new LinearLayout.LayoutParams(-1, 0, 1));
        View divider = new View(this); divider.setBackgroundColor(0xffd9ecef);
        shell.addView(divider, new LinearLayout.LayoutParams(-1, dp(1)));
        shell.addView(refreshProgress, full());
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
                if (settingText || editingProductName) return;
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
        // Do not auto-refresh here.  Refreshing would replace the phone's
        // surviving catalog with the damaged/empty server PRODUCT table.
        loadCatalog(false);
        if (state != null && state.getBoolean("showingId")) showIdPage(state);
    }

    private void invalidateSelection() {
        completionEcho.reset();
        generation++;
        if (pendingSearch != null) main.removeCallbacks(pendingSearch);
        selected = null;
        updateCatalogTotals();
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
        if (!categories.isEmpty()) {
            int position = selectedCategory == null ? 0 : categories.indexOf(selectedCategory);
            if (position < 0) position = 0;
            selectedCategory = categories.get(position);
            category.setSelection(position);
        } else {
            selectedCategory = null;
        }
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
        String syncToken = getPreferences(MODE_PRIVATE).getString("token", "");
        io.execute(() -> {
            ProductCatalog loaded = null;
            String feedback;
            try {
                boolean pending = getSharedPreferences("product_catalogs", MODE_PRIVATE)
                        .getBoolean("pending:" + base, false);
                String data = getSharedPreferences("product_catalogs", MODE_PRIVATE).getString(base, "[]");
                loaded = new ProductCatalog(new JSONArray(data));
                feedback = loaded.size() + " products loaded locally. "
                        + (pending ? "A previous update was not confirmed; use Refresh or Force Upload to recover."
                        : "Tap Refresh to download the latest data.");
            } catch (Exception ex) { feedback = "Local data needs verification. Tap Refresh before editing."; }
            ProductCatalog result = loaded;
            String text = feedback;
            main.post(() -> {
                if (closed) return;
                catalog = result;
                updateCatalogTotals();
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
        // The phone's catalog is the protected source of truth until an
        // explicit, verified upload has completed. Never let a refresh-only
        // download replace precious local product data.
        try {
            String saved = getSharedPreferences("product_catalogs", MODE_PRIVATE).getString(server, "[]");
            ProductCatalog protectedCatalog = new ProductCatalog(new JSONArray(saved));
        } catch (Exception ex) {
            message("Refresh is blocked because the saved product data could not be verified. Do not overwrite it.", true);
            return;
        }
        invalidateSelection();
        busy(true);
        refreshProgress.setVisibility(View.VISIBLE);
        showRefreshProgress("Connecting to POS…", -1);
        message("Refreshing products…", false);
        String base = server;
        String saved = getSharedPreferences("product_catalogs", MODE_PRIVATE).getString(server, "[]");
        ProductCatalog protectedCatalog;
        try { protectedCatalog = new ProductCatalog(new JSONArray(saved)); }
        catch (Exception ex) { message("Refresh is blocked because the saved product data could not be verified. Do not overwrite it.", true); return; }
        String syncToken = getPreferences(MODE_PRIVATE).getString("token", "");
        io.execute(() -> {
            ProductCatalog downloaded = null;
            String feedback;
            try {
                JSONObject response = requestSync(base, syncToken.isEmpty() ? "/AikaPos/stockProducts" : "/products/mobileSync/products", null,
                        (text, percent) -> main.post(() -> {
                            if (!closed) showRefreshProgress(text, percent);
                        }));
                main.post(() -> { if (!closed) showRefreshProgress("Preparing product inventory…", -1); });
                ProductCatalog candidate = new ProductCatalog(response.getJSONArray("products"));
                JSONArray localRows = new JSONArray(saved);
                java.util.List<ProductSyncDiff.Item> differences = ProductSyncDiff.compare(response.getJSONArray("products"), localRows);
                if (!differences.isEmpty() && protectedCatalog.size() > 0) {
                    boolean hideReport = getPreferences(MODE_PRIVATE).getBoolean("hideSyncReport", false);
                    if (hideReport && canAutoResolve(differences)) {
                        main.post(() -> autoResolveSync(base, response, differences));
                    } else {
                        main.post(() -> showSyncReport(base, response, differences));
                    }
                    feedback = "Comparison ready: " + differences.size() + " product difference(s).";
                    ProductCatalog result = null;
                    String text = feedback;
                    main.post(() -> { if (!closed) { refreshProgress.setVisibility(View.GONE); busy(false); message(text, true); } });
                    return;
                }
                main.post(() -> { if (!closed) showRefreshProgress("Saving " + candidate.size() + " products on this phone…", -1); });
                persistCatalog(base, candidate);
                downloaded = candidate;
                feedback = candidate.size() + " products refreshed and saved locally.";
            } catch (Exception ex) {
                String error = "Refresh failed. Previous local data retained. " + ex.getMessage();
                main.post(() -> {
                    if (!closed) showRefreshRecovery(base, error);
                });
                feedback = error;
            }
            ProductCatalog result = downloaded;
            String text = feedback;
            main.post(() -> {
                if (closed) return;
                if (result != null) { catalog = result; updateCatalogTotals(); updateCategories(false); }
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
            updateCatalogTotals();
            showLocalProductPhoto(row.optInt("id", -1));
            editingCategory = false;
            if (editCategory != null) { editCategory.setSelected(false); editCategory.setEnabled(true); }
            weeklySales.setText("Weekly sales: " + weeklySales(row));
            priceWithTax.setText("+ Tax: " + formatMoneyCents(finalPriceCents(row)));
            editingProductName = false;
            if (editProductName != null) { editProductName.setSelected(false); editProductName.setEnabled(true); }
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
        final boolean name;
        final boolean category;
        AutoSaveRequest(int productId, EditText field, String key, boolean price) {
            this(productId, field, key, price, false);
        }
        AutoSaveRequest(int productId, EditText field, String key, boolean price, boolean name) {
            this(productId, field, key, price, name, false);
        }
        AutoSaveRequest(int productId, EditText field, String key, boolean price, boolean name, boolean category) {
            this.productId = productId; this.field = field; this.key = key; this.price = price; this.name = name; this.category = category;
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
        if (request.name) {
            if (edited.isEmpty()) throw new IllegalArgumentException("Product name cannot be empty");
            values.put("mnemonic", edited);
            values.put("subject", edited);
            expected.put("mnemonic", selected.optString("mnemonic", ""));
            expected.put("subject", selected.optString("subject", ""));
        }
        if (request.category) {
            values.put("category", edited);
            expected.put("category", selected.optString("category", ""));
        }
        return payload.put("values", values).put("expected", expected);
    }

    private void startAutoSave(AutoSaveRequest request) {
        startAutoSave(request, null);
    }

    private void showSyncReport(String base, JSONObject response, java.util.List<ProductSyncDiff.Item> differences) {
        android.text.SpannableStringBuilder report = new android.text.SpannableStringBuilder("Products different on server and phone:\n\n");
        int shown = 0;
        for (ProductSyncDiff.Item item : differences) {
            if (shown++ >= 80) { report.append("… and ").append(String.valueOf(differences.size() - 80)).append(" more\n"); break; }
            JSONObject s = item.server, l = item.local;
            String id = s != null ? s.optString("local_id", s.optString("id")) : l.optString("local_id", l.optString("id"));
            report.append("ID ").append(id).append("\n");
            int serverStart = report.length(); report.append("SERVER: ").append(summary(s)).append("\n");
            int serverEnd = report.length();
            int localStart = report.length(); report.append("LOCAL:  ").append(summary(l)).append("\n\n");
            int localEnd = report.length();
            if (item.serverNewer) report.setSpan(new android.text.style.ForegroundColorSpan(0xff16803c), serverStart, serverEnd, android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            if (item.localNewer) report.setSpan(new android.text.style.ForegroundColorSpan(0xff16803c), localStart, localEnd, android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        CheckBox skip = new CheckBox(this);
        skip.setText("Do not show this comparison next time");
        LinearLayout box = column();
        box.setPadding(dp(24), 0, dp(24), 0);
        TextView text = new TextView(this); text.setText(report); box.addView(text); box.addView(skip);
        Button uploadAll = new Button(this);
        uploadAll.setText("Upload all local products");
        box.addView(uploadAll);
        Button forceUploadAll = new Button(this);
        forceUploadAll.setText("Force upload all local products");
        box.addView(forceUploadAll);
        AlertDialog dialog = new AlertDialog.Builder(this).setTitle("Product synchronization report").setView(box)
                .setNegativeButton("Keep phone data", null)
                .setPositiveButton("Upload phone data", (d, w) -> uploadLocalProducts(base, differences))
                .setNeutralButton("Download server data", (d, w) -> {
                    try {
                        ProductCatalog downloaded = new ProductCatalog(response.getJSONArray("products"));
                        persistCatalog(base, downloaded); catalog = downloaded; updateCatalogTotals(); updateCategories(false);
                        message("Server product data downloaded to the phone.", true);
                    } catch (Exception ex) { message("Download cancelled: " + ex.getMessage(), true); }
                }).create();
        uploadAll.setOnClickListener(v -> { dialog.dismiss(); uploadAllLocalProducts(base, response); });
        forceUploadAll.setOnClickListener(v -> new AlertDialog.Builder(this)
                .setTitle("Force upload all local products")
                .setMessage("This will overwrite the server Product data for the current POS ID. Continue?")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Force upload", (d, w) -> { dialog.dismiss(); forceUploadAllLocalProducts(base, response); })
                .show());
        dialog.setOnDismissListener(d -> getPreferences(MODE_PRIVATE).edit().putBoolean("hideSyncReport", skip.isChecked()).apply());
        dialog.show();
    }

    /**
     * Keep the local catalog available for upload testing when the read endpoint
     * returns a non-JSON page or is otherwise unavailable. The normal comparison
     * report still remains the preferred path when a server snapshot is present.
     */
    private void showRefreshRecovery(String base, String error) {
        LinearLayout box = column();
        box.setPadding(dp(24), 0, dp(24), 0);
        TextView text = new TextView(this);
        text.setText(error + "\n\nThe server snapshot was not available. You can still test uploading the phone's local catalog.");
        box.addView(text);
        Button uploadAll = new Button(this);
        uploadAll.setText("Upload all local products");
        box.addView(uploadAll);
        Button forceUploadAll = new Button(this);
        forceUploadAll.setText("Force upload all local products");
        box.addView(forceUploadAll);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Product sync recovery")
                .setView(box)
                .setNegativeButton("Keep phone data", null)
                .create();
        JSONObject emptyServer = new JSONObject();
        uploadAll.setOnClickListener(v -> {
            dialog.dismiss();
            uploadAllLocalProducts(base, emptyServer);
        });
        forceUploadAll.setOnClickListener(v -> new AlertDialog.Builder(this)
                .setTitle("Force upload all local products")
                .setMessage("The server snapshot could not be downloaded. This will send the entire local catalog and request overwrite on the server. Continue?")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Force upload", (d, w) -> {
                    dialog.dismiss();
                    forceUploadAllLocalProducts(base, emptyServer);
                })
                .show());
        dialog.show();
    }

    private boolean canAutoResolve(java.util.List<ProductSyncDiff.Item> differences) {
        boolean upload = false, download = false;
        for (ProductSyncDiff.Item item : differences) {
            if (item.serverNewer == item.localNewer) return false;
            upload |= item.localNewer; download |= item.serverNewer;
        }
        return upload ^ download;
    }

    private void autoResolveSync(String base, JSONObject response, java.util.List<ProductSyncDiff.Item> differences) {
        boolean upload = differences.get(0).localNewer;
        if (upload) {
            uploadLocalProducts(base, differences);
        } else {
            try {
                ProductCatalog downloaded = new ProductCatalog(response.getJSONArray("products"));
                persistCatalog(base, downloaded); catalog = downloaded; updateCatalogTotals(); updateCategories(false);
                message("Synchronization completed: server data downloaded.", true);
            } catch (Exception ex) { message("Automatic download failed. Local product data was retained. " + ex.getMessage(), true); }
        }
    }

    private void uploadLocalProducts(String base, java.util.List<ProductSyncDiff.Item> differences) {
        String token = getPreferences(MODE_PRIVATE).getString("token", "");
        io.execute(() -> {
            try {
                JSONArray rows = new JSONArray();
                for (ProductSyncDiff.Item item : differences) if (item.local != null) {
                    JSONObject row = new JSONObject();
                    if (item.local.has("local_id") && !item.local.isNull("local_id") && item.local.optLong("local_id") > 0)
                        row.put("local_id", item.local.optLong("local_id"));
                    row.put("expected_last_modified", item.server == null ? "" : item.server.optString("last_modified", ""));
                    row.put("data", item.local);
                    rows.put(row);
                }
                JSONObject body = new JSONObject().put("products", rows);
                JSONObject response = requestSync(base, "/products/mobileSync/products", body, null);
                String personId = getPreferences(MODE_PRIVATE).getString("personId", "unknown");
                int uploaded = response.optJSONArray("uploaded") == null ? 0 : response.optJSONArray("uploaded").length();
                int failed = response.optJSONArray("failed") == null ? 0 : response.optJSONArray("failed").length();
                int conflicts = response.optJSONArray("conflicts") == null ? 0 : response.optJSONArray("conflicts").length();
                String result = "Phone Product data uploaded. POS ID: " + personId + ". Requested: " + rows.length() + ", uploaded: " + uploaded + ", failed: " + failed + ", conflicts: " + conflicts;
                System.out.println("[ProductUpload] POS_ID=" + personId + " requested=" + rows.length() + " uploaded=" + uploaded + " failed=" + failed + " conflicts=" + conflicts);
                main.post(() -> message(result, true));
            } catch (Exception ex) {
                main.post(() -> message("Upload failed. Local product data was retained. " + ex.getMessage(), true));
            }
        });
    }

    private void uploadAllLocalProducts(String base, JSONObject serverResponse) {
        uploadAllLocalProducts(base, serverResponse, false);
    }

    private void forceUploadAllLocalProducts(String base, JSONObject serverResponse) {
        uploadAllLocalProducts(base, serverResponse, true);
    }

    private void uploadAllLocalProducts(String base, JSONObject serverResponse, boolean force) {
        String token = getPreferences(MODE_PRIVATE).getString("token", "");
        io.execute(() -> {
            try {
                ProductCatalog localCatalog = catalog;
                if (localCatalog == null) {
                    String saved = getSharedPreferences("product_catalogs", MODE_PRIVATE).getString(base, "[]");
                    localCatalog = new ProductCatalog(new JSONArray(saved));
                }
                if (localCatalog.size() == 0) throw new IOException("No local products are available to upload");
                JSONArray localRows = new JSONArray(localCatalog.serialize());
                java.util.Map<Long, String> expected = new java.util.HashMap<>();
                JSONArray serverRows = serverResponse.optJSONArray("products");
                if (serverRows != null) for (int i = 0; i < serverRows.length(); i++) {
                    JSONObject row = serverRows.getJSONObject(i);
                    expected.put(row.optLong("local_id", row.optLong("id")), row.optString("last_modified", ""));
                }
                JSONArray rows = new JSONArray();
                for (int i = 0; i < localRows.length(); i++) {
                    JSONObject local = localRows.getJSONObject(i);
                    long id = local.optLong("local_id", 0);
                    JSONObject upload = new JSONObject()
                            .put("expected_last_modified", id > 0 && expected.containsKey(id) ? expected.get(id) : "")
                            .put("data", local);
                    if (id > 0) upload.put("local_id", id);
                    rows.put(upload);
                }
                JSONObject response = requestSync(base, "/products/mobileSync/products", new JSONObject().put("products", rows).put("force", force), null);
                int uploaded = response.optJSONArray("uploaded") == null ? 0 : response.optJSONArray("uploaded").length();
                int failed = response.optJSONArray("failed") == null ? 0 : response.optJSONArray("failed").length();
                int conflicts = response.optJSONArray("conflicts") == null ? 0 : response.optJSONArray("conflicts").length();
                String personId = getPreferences(MODE_PRIVATE).getString("personId", "unknown");
                String result = (force ? "FORCE upload" : "All local Products upload") + " finished. POS ID: " + personId + ". Requested: " + rows.length() + ", uploaded: " + uploaded + ", failed: " + failed + ", conflicts: " + conflicts;
                System.out.println("[ProductUploadAll] force=" + force + " POS_ID=" + personId + " requested=" + rows.length() + " uploaded=" + uploaded + " failed=" + failed + " conflicts=" + conflicts);
                main.post(() -> message(result, failed == 0 && conflicts == 0));
            } catch (Exception ex) {
                main.post(() -> message("All local Products upload failed. Local data was retained. " + ex.getMessage(), true));
            }
        });
    }

    private static String summary(JSONObject row) {
        if (row == null) return "<missing>";
        return row.optString("mnemonic", row.optString("subject", row.optString("code", "")))
                + " | modified=" + row.optString("server_modified", row.optString("last_modified", ""));
    }

    private void showLocalProductPhoto(int productId) {
        if (productPhoto == null) return;
        productPhoto.setImageDrawable(null);
        productPhoto.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        if (productId <= 0) return;
        File file = new File(new File(getFilesDir(), "product-photos"), "product-" + productId + ".jpg");
        if (file.isFile()) {
            android.graphics.Bitmap bitmap = android.graphics.BitmapFactory.decodeFile(file.getAbsolutePath());
            if (bitmap != null) {
                productPhoto.setImageBitmap(bitmap);
                productPhoto.setScaleType(ImageView.ScaleType.CENTER_CROP);
            }
        }
    }

    private int weeklySales(JSONObject row) {
        int weekly = Math.max(row.optInt("weekly_sales_qty", row.optInt("weeklySalesQty", 0)),
                row.optInt("lastSevenDaysQty", 0));
        JSONObject sales = row.optJSONObject("movingSales");
        if (sales != null) {
            int recent = 0;
            for (java.util.Iterator<String> it = sales.keys(); it.hasNext();) recent += Math.max(0, sales.optInt(it.next(), 0));
            weekly = Math.max(weekly, recent);
        }
        return weekly;
    }

    private void updateCatalogTotals() {
        if (totalNumber == null || totalPrice == null) return;
        int count = 0;
        long cents = 0;
        for (CartLine line : cart.values()) {
            count += line.quantity;
            cents += (long) line.unitPriceCents * line.quantity;
        }
        totalNumber.setText("QTY: " + count);
        totalPrice.setText("Total: " + formatMoneyCents((int) Math.min(Integer.MAX_VALUE, cents)));
        if (cartBadge != null) {
            cartBadge.setText(String.valueOf(count));
            cartBadge.setVisibility(count > 0 ? View.VISIBLE : View.GONE);
        }
    }

    private void addSelectedProductToCart() {
        if (selected == null) {
            message("Select a product before adding it to the cart.", false);
            return;
        }
        int id = selected.optInt("id", -1);
        if (id <= 0) return;
        CartLine line = cart.get(id);
        if (line == null) cart.put(id, new CartLine(id, selected.optString("mnemonic", "Product"),
                selected.optString("category", ""), finalPriceCents(selected)));
        else line.quantity++;
        cartButton.animate().cancel();
        cartButton.animate().scaleX(1.25f).scaleY(1.25f).setDuration(90)
                .withEndAction(() -> cartButton.animate().scaleX(1f).scaleY(1f).setDuration(120).start()).start();
        cartBadge.animate().cancel();
        cartBadge.animate().scaleX(1.3f).scaleY(1.3f).setDuration(90)
                .withEndAction(() -> cartBadge.animate().scaleX(1f).scaleY(1f).setDuration(120).start()).start();
        updateCatalogTotals();
        message("Added to cart.", false);
    }

    private void showCartDialog() {
        LinearLayout content = cartContent;
        if (content == null) {
            content = column();
            cartContent = content;
        } else {
            content.removeAllViews();
        }
        content.setPadding(dp(20), dp(8), dp(20), 0);
        content.setMinimumHeight(dp(220));
        if (cart.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText("Cart is empty");
            empty.setGravity(android.view.Gravity.CENTER);
            empty.setMinHeight(dp(130));
            content.addView(empty, full());
        } else {
            for (java.util.Map.Entry<Integer, CartLine> entry : cart.entrySet()) {
                int id = entry.getKey(); CartLine line = entry.getValue();
                LinearLayout row = new LinearLayout(this);
                row.setGravity(android.view.Gravity.CENTER_VERTICAL);
                TextView label = new TextView(this);
                label.setText(line.name + "  x" + line.quantity + "  " + formatMoneyCents(line.unitPriceCents * line.quantity));
                label.setTextSize(15);
                row.addView(label, new LinearLayout.LayoutParams(0, dp(48), 1));
                LinearLayout quantityControls = new LinearLayout(this);
                quantityControls.setGravity(android.view.Gravity.CENTER_VERTICAL);
                Button minus = new Button(this);
                minus.setText("−"); minus.setTextSize(18); minus.setAllCaps(false);
                minus.setOnClickListener(v -> {
                    if (--line.quantity <= 0) cart.remove(id);
                    updateCatalogTotals(); showCartDialog();
                });
                quantityControls.addView(minus, new LinearLayout.LayoutParams(dp(44), dp(48)));
                Button plus = new Button(this);
                plus.setText("+"); plus.setTextSize(18); plus.setAllCaps(false);
                plus.setOnClickListener(v -> { line.quantity++; updateCatalogTotals(); showCartDialog(); });
                quantityControls.addView(plus, new LinearLayout.LayoutParams(dp(44), dp(48)));
                LinearLayout.LayoutParams quantityParams = new LinearLayout.LayoutParams(dp(88), dp(48));
                quantityParams.leftMargin = dp(12);
                row.addView(quantityControls, quantityParams);
                content.addView(row, full());
            }
        }
        LinearLayout actions = new LinearLayout(this);
        actions.setGravity(android.view.Gravity.CENTER);
        Button submit = new Button(this);
        submit.setText("Submit");
        stylePrimaryButton(this, submit);
        submit.setTextColor(0xffffffff);
        submit.setOnClickListener(v -> submitCart());
        actions.addView(submit, new LinearLayout.LayoutParams(dp(150), dp(52)));
        content.addView(actions, full());
        if (cartDialog != null && cartDialog.isShowing()) {
            return;
        }
        if (content.getParent() instanceof android.view.ViewGroup) {
            ((android.view.ViewGroup) content.getParent()).removeView(content);
        }
        cartDialog = new AlertDialog.Builder(this).setTitle("Selected Products").setView(content).create();
        cartDialog.setOnDismissListener(dialog -> cartDialog = null);
        cartDialog.show();
        if (cartDialog.getWindow() != null) {
            cartDialog.getWindow().setLayout(Math.min(getResources().getDisplayMetrics().widthPixels - dp(32), dp(460)), -2);
        }
    }

    private void submitCart() {
        int quantity = 0;
        long cents = 0;
        for (CartLine line : cart.values()) {
            quantity += line.quantity;
            cents += (long) line.unitPriceCents * line.quantity;
        }
        if (quantity == 0) {
            message("Cart submission failed: the cart is empty.", true);
            return;
        }
        if (server == null || server.trim().isEmpty()) {
            message("Cart submission failed: configure the iK POS address first. Cart remains.", true);
            return;
        }
        final int submittedQuantity = quantity;
        final int submittedCents = (int) Math.min(Integer.MAX_VALUE, cents);
        final JSONObject payload = new JSONObject();
        try {
            payload.put("saleId", "SIM-" + System.currentTimeMillis());
            payload.put("totalCents", submittedCents);
            JSONArray items = new JSONArray();
            for (CartLine line : cart.values()) {
                items.put(new JSONObject().put("productId", line.productId)
                        .put("name", line.name).put("category", line.category)
                        .put("quantity", line.quantity).put("unitPrice", line.unitPriceCents)
                        .put("totalCents", line.unitPriceCents * line.quantity)
                        .put("stockField", "stock1"));
            }
            payload.put("items", items);
        } catch (Exception ex) {
            message("Cart submission failed: could not prepare the POS order. Cart remains.", true);
            return;
        }
        if (cartDialog != null) cartDialog.dismiss();
        final boolean websiteOrder = isWebsiteDomain(server);
        message(websiteOrder ? "Submitting cart to website…" : "Submitting cart to iK POS…", false);
        io.execute(() -> {
            try {
                if (websiteOrder) requestSync(server, "/products/mobileSync/sales", payload, null);
                else request(server, "/AikaPos/sales", payload);
                main.post(() -> {
                    cart.clear();
                    updateCatalogTotals();
                    message("Cart submitted successfully to " + (websiteOrder ? "website" : "iK POS") + ": " + submittedQuantity
                            + " item(s), total " + formatMoneyCents(submittedCents) + ". Cart cleared.", false);
                });
            } catch (Exception ex) {
                main.post(() -> message("Cart submission failed: " + detail(ex) + ". Cart remains.", true));
            }
        });
    }

    private static final class CartLine {
        final int productId;
        final String name;
        final String category;
        final int unitPriceCents;
        int quantity = 1;
        CartLine(int productId, String name, int unitPriceCents) {
            this(productId, name, "", unitPriceCents);
        }
        CartLine(int productId, String name, String category, int unitPriceCents) {
            this.productId = productId;
            this.name = name;
            this.category = category == null ? "" : category;
            this.unitPriceCents = unitPriceCents;
        }
    }

    private void saveCategory(String value) {
        if (selected == null || value.equals(selected.optString("category", ""))) return;
        EditText field = new EditText(this);
        field.setText(value);
        AutoSaveRequest request = new AutoSaveRequest(selected.optInt("id", -1), field, "category", false, false, true);
        pendingAutoSaves.put("category", request);
        main.post(this::drainAutoSaves);
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
            java.util.Iterator<String> valueKeys = values.keys();
            while (valueKeys.hasNext()) {
                String key = valueKeys.next();
                optimistic.put(key, values.get(key));
            }
            ProductCatalog localUpdated = catalog.updated(optimistic);
            persistCatalog(server, localUpdated);
            catalog = localUpdated;
            updateCatalogTotals();
            selected = optimistic;
            if (priceWithTax != null) priceWithTax.setText("+ Tax: " + formatMoneyCents(finalPriceCents(optimistic)));
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
        final boolean websiteMode = !getPreferences(MODE_PRIVATE).getString("token", "").trim().isEmpty();
        final JSONObject websiteProduct;
        try { websiteProduct = new JSONObject(selected.toString()); }
        catch (Exception ex) { productSaveBusy(false); message("Could not prepare the product for upload: " + detail(ex), false); return; }
        io.execute(() -> {
            String feedback = "Product sync did not complete. Tap Refresh to load the current stock.";
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
                if (websiteMode) {
                    main.post(() -> { if (!closed) message("Saving changes to website…", false); });
                    JSONObject row = new JSONObject();
                    if (websiteProduct.has("local_id") && !websiteProduct.isNull("local_id")
                            && websiteProduct.optLong("local_id") > 0)
                        row.put("local_id", websiteProduct.optLong("local_id"));
                    row.put("expected_last_modified", websiteProduct.optString("last_modified", ""));
                    row.put("data", websiteProduct);
                    JSONObject response = requestSync(base, "/products/mobileSync/products",
                            new JSONObject().put("products", new JSONArray().put(row)), null);
                    JSONArray failed = response.optJSONArray("failed");
                    JSONArray conflicts = response.optJSONArray("conflicts");
                    if (!response.optBoolean("ok", false) || (failed != null && failed.length() > 0)
                            || (conflicts != null && conflicts.length() > 0))
                        throw new IOException("Website rejected the product update");
                    verified = true;
                    storedProduct = websiteProduct;
                    updated = snapshot;
                    // The optimistic catalog was already saved before the request;
                    // persist again here to remove the recovery marker only after
                    // the website has acknowledged the upload.
                    persistCatalog(base, snapshot);
                    feedback = "Website saved and verified. Stock 1: " + websiteProduct.optInt("stock1", 0)
                            + ", Stock 2: " + websiteProduct.optInt("stock2", 0)
                            + ", Stock 3: " + websiteProduct.optInt("stock3", 0);
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
                    updateCatalogTotals();
                    selected = finalStoredProduct;
                    // Stock Moving keeps its own calculated input/draft while it
                    // is hidden.  Invalidate that snapshot after a verified edit
                    // so the next tab switch cannot calculate from old Manque.
                    if (movingPlan != null) movingPlan.invalidateForInventoryChange();
                    if (request.field.getText().toString().trim().equals(submittedText)) {
                        settingValues = true;
                        try {
                            request.field.setText(request.name
                                    ? finalStoredProduct.optString(request.key, "")
                                    : request.price
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
        editCategory.setEnabled(!value && selected != null && !categories.isEmpty());
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
            if (!ProductFieldChange.changed(selected, request.key, request.field.getText().toString(), request.price, request.name)) continue;
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
        return request(base, path, body, null, null);
    }

    private static JSONObject request(String base, String path, JSONObject body, DownloadProgress progress) throws Exception {
        return request(base, path, body, progress, null);
    }

    private static JSONObject request(String base, String path, JSONObject body, DownloadProgress progress, String syncToken) throws Exception {
        HttpURLConnection c = (HttpURLConnection) new URL(base + path).openConnection();
        c.setConnectTimeout(5000);
        // Full Product uploads can legitimately take longer than a normal
        // catalog read. A timeout here means the server may have committed
        // the update even though the response was not received.
        c.setReadTimeout(body == null ? 15000 : 120000);
        c.setInstanceFollowRedirects(false);
        c.setUseCaches(false);
        if (syncToken != null && !syncToken.trim().isEmpty()) c.setRequestProperty("X-Mobile-Sync-Token", syncToken);

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
                String raw = new String(bytes.toByteArray(), StandardCharsets.UTF_8).trim();
                if (raw.length() > 240) raw = raw.substring(0, 240);
                throw new IOException("HTTP " + code + ": Server returned non-JSON response: " + raw);
            }
            if (code != 200) throw new RequestException(code, "HTTP " + code + ": "
                    + result.optString("error", httpError(code)));
            return result;
        } catch (java.net.SocketTimeoutException ex) {
            throw new IOException(body == null ? "Connection timed out" : "Response timed out. Update status is unconfirmed", ex);
        } finally { c.disconnect(); }
    }

    /** Product sync requests silently renew the short-lived server session once. */
    private JSONObject requestSync(String base, String path, JSONObject body, DownloadProgress progress) throws Exception {
        String token = getPreferences(MODE_PRIVATE).getString("token", "").trim();
        try {
            return request(base, path, body, progress, token);
        } catch (RequestException ex) {
            if (ex.code != HttpURLConnection.HTTP_UNAUTHORIZED) throw ex;
            String licence = getPreferences(MODE_PRIVATE).getString("personId", "").trim();
            if (licence.isEmpty()) throw ex;
            validateWebsiteActivation(base, licence);
            String renewed = getPreferences(MODE_PRIVATE).getString("token", "").trim();
            return request(base, path, body, progress, renewed);
        }
    }

    private EditText settingsDecimal(String label, double value) {
        EditText input = new EditText(this);
        input.setHint(label + " (%)"); input.setSingleLine(true);
        input.setText(String.format(java.util.Locale.ROOT, "%.3f", value));
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        styleInput(input);
        return input;
    }

    private EditText settingsMoney(String label, int cents) {
        EditText input = new EditText(this);
        input.setHint(label + " ($ per product)"); input.setSingleLine(true);
        input.setText(String.format(java.util.Locale.ROOT, "%.2f", cents / 100.0));
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        styleInput(input);
        return input;
    }

    private LinearLayout settingsField(String label, EditText input) {
        LinearLayout field = column();
        TextView title = new TextView(this);
        title.setText(label);
        title.setTextColor(0xff16344b);
        field.addView(title, full());
        field.addView(input, full());
        return field;
    }

    private int parseRate(EditText input, int fallback) {
        try {
            double percent = Double.parseDouble(input.getText().toString().trim());
            if (percent < 0 || percent > 100) throw new NumberFormatException();
            return (int) Math.round(percent * 100.0);
        } catch (Exception ex) { return fallback; }
    }

    private int parseMoneySetting(EditText input) {
        try { return ProductCatalog.moneyCents(input.getText().toString().trim()); }
        catch (Exception ex) { return 0; }
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
        EditText activationCode = new EditText(this);
        styleInput(activationCode);
        activationCode.setHint("Enter activation code");
        activationCode.setSingleLine(true);
        activationCode.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
        activationCode.setText(getPreferences(MODE_PRIVATE).getString("personId", ""));
        selectAllOnFocus(activationCode);
        TextView activationLabel = new TextView(this);
        activationLabel.setText("Activation code (website synchronization)");
        box.addView(activationLabel);
        box.addView(activationCode);

        // Keep activation hidden while the address is being entered. It is
        // shown only after Save identifies a website domain.
        activationLabel.setVisibility(View.GONE);
        activationCode.setVisibility(View.GONE);
        TextView taxLabel = new TextView(this);
        taxLabel.setText("Price calculation settings (%)");
        box.addView(taxLabel);
        EditText gstRate = settingsDecimal("GST", settingRate("tax_gst_basis_points", 500) / 100.0);
        EditText qstRate = settingsDecimal("QST", settingRate("tax_qst_basis_points", 9975) / 100.0);
        EditText ecoTax = settingsMoney("Eco Tax", getPreferences(MODE_PRIVATE).getInt("tax_eco_cents", 1));
        EditText bottleFee = settingsMoney("Bottle Fee", getPreferences(MODE_PRIVATE).getInt("tax_bottle_cents", 10));
        LinearLayout taxRow1 = new LinearLayout(this);
        taxRow1.addView(settingsField("GST (%)", gstRate), new LinearLayout.LayoutParams(0, -2, 1));
        taxRow1.addView(settingsField("QST (%)", qstRate), new LinearLayout.LayoutParams(0, -2, 1));
        LinearLayout taxRow2 = new LinearLayout(this);
        taxRow2.addView(settingsField("Eco Tax ($)", ecoTax), new LinearLayout.LayoutParams(0, -2, 1));
        taxRow2.addView(settingsField("Bottle Fee ($)", bottleFee), new LinearLayout.LayoutParams(0, -2, 1));
        box.addView(taxRow1, full());
        box.addView(taxRow2, full());
        TextView languageLabel = new TextView(this);
        languageLabel.setText("Language");
        box.addView(languageLabel, full());
        RadioGroup languages = new RadioGroup(this);
        languages.setOrientation(RadioGroup.HORIZONTAL);
        RadioButton french = new RadioButton(this); french.setText("Français"); french.setTag("fr");
        RadioButton english = new RadioButton(this); english.setText("English"); english.setTag("en");
        RadioButton chinese = new RadioButton(this); chinese.setText("中文"); chinese.setTag("zh");
        languages.addView(french, new RadioGroup.LayoutParams(0, -2, 1));
        languages.addView(english, new RadioGroup.LayoutParams(0, -2, 1));
        languages.addView(chinese, new RadioGroup.LayoutParams(0, -2, 1));
        String language = getPreferences(MODE_PRIVATE).getString("language", "en");
        if ("fr".equals(language)) french.setChecked(true);
        else if ("zh".equals(language)) chinese.setChecked(true);
        else english.setChecked(true);
        box.addView(languages, full());
        Button viewLocal = new Button(this);
        viewLocal.setText("View Local Inventory");
        viewLocal.setOnClickListener(v -> viewLocalInventory());
        box.addView(viewLocal);
        Button backup = new Button(this);
        backup.setText("Back Up Protected Products");
        backup.setOnClickListener(v -> startProductBackup());
        box.addView(backup);
        Button forceUpload = new Button(this);
        forceUpload.setText("Force Upload All Local Products");
        forceUpload.setOnClickListener(v -> {
            final String uploadTarget = server;
            if (uploadTarget == null || uploadTarget.trim().isEmpty()) {
                message("Save the server address before forcing an upload.", true);
                return;
            }
            new AlertDialog.Builder(this)
                    .setTitle("Force upload all local products")
                    .setMessage("Send the complete local product catalog to the configured server and overwrite matching products. Continue?")
                    .setNegativeButton("Cancel", null)
                    .setPositiveButton("Force upload", (d, w) ->
                            forceUploadAllLocalProducts(uploadTarget, new JSONObject()))
                    .show();
        });
        box.addView(forceUpload);
        AlertDialog dialog = new AlertDialog.Builder(this).setTitle("Connection Settings")
                .setView(box).setNegativeButton("Cancel", null).setPositiveButton("Save", null).create();
        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String entered = address.getText().toString().trim();
            boolean websiteMode = isWebsiteDomain(entered);
            String url = normalizeServerAddress(entered);
            try {
                URL parsed = new URL(url);
                if (!(parsed.getProtocol().equals("http") || parsed.getProtocol().equals("https")) || parsed.getHost().isEmpty()
                        || !parsed.getPath().isEmpty() || parsed.getQuery() != null || parsed.getRef() != null || parsed.getUserInfo() != null)
                    throw new Exception();
            } catch (Exception ex) { address.setError("Enter a server address, e.g. http://192.168.1.20:8880"); return; }

            android.content.SharedPreferences preferences = getPreferences(MODE_PRIVATE);
            String previousServer = preferences.getString("server", "").trim();
            boolean serverChanged = !previousServer.isEmpty() && !previousServer.equalsIgnoreCase(url);
            if (serverChanged && websiteMode) {
                // A website URL identifies the activation namespace. Changing it
                // invalidates the old token/personId, but must not touch local products.
                preferences.edit().remove("token").apply();
                activationLabel.setVisibility(View.VISIBLE);
                activationCode.setVisibility(View.VISIBLE);
            }
            String personId = activationCode.getText().toString().trim();
            // A previously saved personId is not proof that this installation
            // has a valid sync token. Allow reactivation whenever the token
            // is missing, even if the old activation code is still stored.
            boolean needsWebsiteActivation = websiteMode
                    && getPreferences(MODE_PRIVATE).getString("token", "").trim().isEmpty();
            if (needsWebsiteActivation && personId.isEmpty()) {
                activationLabel.setVisibility(View.VISIBLE);
                activationCode.setVisibility(View.VISIBLE);
                activationCode.setError("Enter the activation code for this website");
                activationCode.requestFocus();
                return;
            }
            if (needsWebsiteActivation) {
                activationLabel.setVisibility(View.VISIBLE);
                activationCode.setVisibility(View.VISIBLE);
            }

            String savedAddress = url;
            address.setEnabled(false);
            viewLocal.setEnabled(false);
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setText("Saving…");
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setEnabled(false);
            dialog.setCancelable(false);
            busy(true);
            io.execute(() -> {
                boolean saved = false;
                String failure = null;
                try {
                    // Welp uses the existing activeJustPrintAccount endpoint.
                    // Its successful response is a positive number (remaining
                    // activation time), so do not persist an unverified code.
                    if (websiteMode) validateWebsiteActivation(savedAddress, personId);
                    android.content.SharedPreferences.Editor settings = preferences.edit()
                            .putString("server", savedAddress);
                    settings.putInt("tax_gst_basis_points", parseRate(gstRate, 500));
                    settings.putInt("tax_qst_basis_points", parseRate(qstRate, 9975));
                    settings.putInt("tax_eco_cents", parseMoneySetting(ecoTax));
                    settings.putInt("tax_bottle_cents", parseMoneySetting(bottleFee));
                    View checkedLanguage = languages.findViewById(languages.getCheckedRadioButtonId());
                    settings.putString("language", checkedLanguage == null ? "en" : String.valueOf(checkedLanguage.getTag()));
                    if (websiteMode) settings.putString("personId", personId);
                    if (!websiteMode) {
                        settings.remove("personId");
                    }
                    saved = settings.commit();
                } catch (Exception ex) { failure = ex.getMessage(); }
                boolean savedResult = saved;
                String failureResult = failure;
                main.post(() -> {
                    if (closed) return;
                    busy(false);
                    if (!savedResult) {
                        address.setEnabled(true);
                        viewLocal.setEnabled(true);
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setText("Save");
                        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setEnabled(true);
                        dialog.setCancelable(true);
                        activationCode.setError(failureResult == null
                                ? "Could not save the connection. Please try again."
                                : failureResult);
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
                    // Address changes must also remain offline-first during
                    // recovery; an explicit Refresh is required to download.
                    loadCatalog(false);
                    dialog.dismiss();
                    Toast.makeText(this, websiteMode
                            ? "Activation successful. synchronizing data with server"
                            : "Connection saved: " + server, Toast.LENGTH_LONG).show();
                    product.requestFocus(); product.selectAll();
                });
            });
        }));
        dialog.show();
    }

    private static boolean isWebsiteDomain(String value) {
        String normalized = value == null ? "" : value.trim().toLowerCase(java.util.Locale.ROOT);
        if (normalized.startsWith("http://")) normalized = normalized.substring(7);
        else if (normalized.startsWith("https://")) normalized = normalized.substring(8);
        normalized = normalized.replaceAll("/+$", "");
        return normalized.endsWith(".com")
                || normalized.contains(".com/");
    }

    /**
     * Canonicalize the address used by the direct AikaPos desktop listener.
     * The legacy POS listener is HTTP on port 8880; operators traditionally
     * enter only the computer IP (for example, 192.168.1.20). Website URLs
     * keep their explicit protocol and port.
     */
    private static String normalizeServerAddress(String value) {
        String normalized = value == null ? "" : value.trim().replaceAll("/+$", "");
        if (normalized.isEmpty()) return "";
        if (normalized.contains("://")) return normalized;
        String host = normalized;
        int colon = normalized.lastIndexOf(':');
        boolean hasPort = colon > 0 && normalized.indexOf(':') == colon
                && normalized.substring(colon + 1).matches("\\d+");
        // Only the direct desktop POS address gets the legacy 8880 default.
        // A bare website hostname uses the normal HTTP port.
        if (!hasPort && (host.matches("\\d{1,3}(\\.\\d{1,3}){3}") || "localhost".equalsIgnoreCase(host)))
            host += ":8880";
        return "http://" + host;
    }

    private void validateWebsiteActivation(String base, String activationCode) throws Exception {
        String endpoint = base + (base.endsWith("/") ? "" : "/") + "products/mobileSync/activate";
        HttpURLConnection connection = (HttpURLConnection) new URL(endpoint).openConnection();
        connection.setConnectTimeout(10000);
        connection.setReadTimeout(15000);
        connection.setInstanceFollowRedirects(true);
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setUseCaches(false);
        connection.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
        // This is the AikaPos/SanjiPos activation protocol. The activation
        // code is the shared client personId used by the remote Product table.
        JSONObject body = new JSONObject().put("licence", activationCode);
        try (OutputStream out = connection.getOutputStream()) {
            out.write(body.toString().getBytes(StandardCharsets.UTF_8));
        }
        int code = connection.getResponseCode();
        InputStream stream = code >= 400 ? connection.getErrorStream() : connection.getInputStream();
        String response = "";
        if (stream != null) {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            try (InputStream in = stream) {
                byte[] buffer = new byte[1024]; int n;
                while ((n = in.read(buffer)) != -1) bytes.write(buffer, 0, n);
            }
            response = new String(bytes.toByteArray(), StandardCharsets.UTF_8).trim();
        }
        connection.disconnect();
        if (code != HttpURLConnection.HTTP_OK) throw new IOException("Activation failed (HTTP " + code + ")");
        if (response.isEmpty() || response.startsWith("0_") || "0".equals(response)) {
            String detail = response.isEmpty() ? "empty response" : response;
            throw new IOException("Activation rejected by server: " + detail);
        }
        JSONObject activated;
        try { activated = new JSONObject(response); }
        catch (Exception ex) {
            throw new IOException("Server returned non-JSON activation response: " + response, ex);
        }
        String token = activated.optString("token", "").trim();
        if (token.isEmpty()) throw new IOException("Activation rejected by server: no sync token");
        // The token authorizes Product reads/writes; personId remains the
        // shared Product namespace and is only retained for diagnostics.
        android.content.SharedPreferences prefs = getPreferences(MODE_PRIVATE);
        prefs.edit().putString("token", token)
                .putString("personId", activated.optString("personId", activationCode)).apply();
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
            }, id -> showEditingPage(id));
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
        if (requestCode == PRODUCT_BACKUP_REQUEST) {
            if (resultCode != RESULT_OK || data == null || data.getData() == null) {
                message("Product backup cancelled. Local data was not changed.", false);
                pendingProductBackup = null;
                return;
            }
            try (OutputStream out = getContentResolver().openOutputStream(data.getData())) {
                if (out == null) throw new IOException("Could not open the selected backup file");
                out.write(pendingProductBackup.getBytes(StandardCharsets.UTF_8));
                out.flush();
                message("Protected product backup saved successfully. Local data was not changed.", false);
            } catch (Exception ex) {
                message("Product backup failed: " + ex.getMessage(), true);
            } finally { pendingProductBackup = null; }
            return;
        }
        if (requestCode == PRODUCT_IMPORT_REQUEST) {
            importProductBackup(data, resultCode);
            return;
        }
        if (requestCode == PRODUCT_PHOTO_REQUEST) {
            if (resultCode == RESULT_OK && pendingProductPhoto != null && pendingProductPhoto.length() > 0) {
                android.graphics.Bitmap bitmap = android.graphics.BitmapFactory.decodeFile(pendingProductPhoto.getAbsolutePath());
                if (bitmap != null) {
                    productPhoto.setImageBitmap(bitmap);
                    productPhoto.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    message("Product photo saved locally. Remote upload is pending POS image support.", false);
                } else {
                    message("The product photo could not be opened.", true);
                }
            } else {
                message("No product photo captured.", false);
            }
            pendingProductPhoto = null;
            return;
        }
        if (idChecking != null) idChecking.onActivityResult(requestCode, resultCode, data);
    }

    private void captureProductPhoto() {
        android.widget.Toast.makeText(this, "Opening camera…", android.widget.Toast.LENGTH_SHORT).show();
        if (selected == null || saving) {
            android.widget.Toast.makeText(this, "Select a product first.", android.widget.Toast.LENGTH_LONG).show();
            message("Select a product before taking its photo.", false);
            return;
        }
        try {
            File directory = new File(getFilesDir(), "product-photos");
            if (!directory.isDirectory() && !directory.mkdirs()) throw new IOException("Cannot create photo directory");
            pendingProductPhoto = new File(directory, "product-" + selected.getInt("id") + ".jpg");
            android.content.Intent intent = new android.content.Intent(this, CameraCaptureActivity.class);
            intent.putExtra(CameraCaptureActivity.EXTRA_OUTPUT_PATH, pendingProductPhoto.getAbsolutePath());
            startActivityForResult(intent, PRODUCT_PHOTO_REQUEST);
        } catch (Exception ex) {
            message("Unable to prepare product photo: " + ex.getMessage(), true);
        }
    }

    private void startProductBackup() {
        if (server == null || server.isEmpty()) {
            message("Enter the server address before creating a product backup.", true);
            return;
        }
        try {
            android.content.SharedPreferences prefs = getSharedPreferences("product_catalogs", MODE_PRIVATE);
            String raw = prefs.getString(server, null);
            if (raw == null) throw new IOException("No saved product catalog was found");
            ProductCatalog saved = new ProductCatalog(new JSONArray(raw));
            if (saved.size() == 0) throw new IOException("The saved product catalog is empty");
            JSONObject backup = new JSONObject();
            backup.put("format", "JustPrint protected product backup");
            backup.put("version", 1);
            backup.put("server", server);
            backup.put("personId", getPreferences(MODE_PRIVATE).getString("personId", ""));
            backup.put("createdAt", new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", java.util.Locale.ROOT)
                    .format(new java.util.Date()));
            backup.put("products", new JSONArray(saved.serialize()));
            pendingProductBackup = backup.toString(2);
            android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_CREATE_DOCUMENT);
            intent.addCategory(android.content.Intent.CATEGORY_OPENABLE);
            intent.setType("application/json");
            intent.putExtra(android.content.Intent.EXTRA_TITLE, "justprint-products-" + System.currentTimeMillis() + ".json");
            startActivityForResult(intent, PRODUCT_BACKUP_REQUEST);
        } catch (Exception ex) {
            message("Product backup was not created: " + ex.getMessage(), true);
        }
    }

    private void importProductBackup(android.content.Intent data, int resultCode) {
        if (resultCode != RESULT_OK || data == null || data.getData() == null) {
            message("Product import cancelled. Local data was not changed.", false);
            return;
        }
        io.execute(() -> {
            try (InputStream in = getContentResolver().openInputStream(data.getData())) {
                if (in == null) throw new IOException("Could not open the selected backup file");
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                byte[] buffer = new byte[4096]; int n;
                while ((n = in.read(buffer)) != -1) bytes.write(buffer, 0, n);
                JSONObject backup = new JSONObject(new String(bytes.toByteArray(), StandardCharsets.UTF_8));
                JSONArray products = backup.optJSONArray("products");
                if (!"JustPrint protected product backup".equals(backup.optString("format")) || products == null)
                    throw new IOException("This is not a valid JustPrint product backup");
                ProductCatalog restored = new ProductCatalog(products);
                if (restored.size() == 0) throw new IOException("The backup contains no products");
                String key = server;
                boolean saved = getSharedPreferences("product_catalogs", MODE_PRIVATE).edit()
                        .putString(key, restored.serialize()).remove("pending:" + key).commit();
                if (!saved) throw new IOException("Could not save the restored product data");
                main.post(() -> {
                    catalog = restored;
                    updateCatalogTotals();
                    updateCategories(true);
                    message(restored.size() + " products imported. Local data restored; server was not changed.", false);
                    if (localInventoryDialog != null && localInventoryDialog.isShowing()) {
                        localInventoryDialog.dismiss();
                        localInventoryDialog = null;
                    }
                    viewLocalInventory();
                });
            } catch (Exception ex) {
                main.post(() -> message("Product import failed: " + ex.getMessage(), true));
            }
        });
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
        // The platform Spinner background can cover the left side of a custom
        // outline. Keep the arrow as the background, then draw the complete
        // rounded frame in the foreground so the content area stays framed.
        android.graphics.drawable.Drawable arrow = spinner.getBackground();
        spinner.setBackground(arrow);
        android.graphics.drawable.GradientDrawable foreground = new android.graphics.drawable.GradientDrawable();
        foreground.setColor(0x00000000);
        foreground.setCornerRadius(12 * density);
        foreground.setStroke(Math.max(1, Math.round(density)), 0xffb6d5df);
        spinner.setForeground(foreground);
        spinner.setClipToOutline(true);
        spinner.setOutlineProvider(new android.view.ViewOutlineProvider() {
            @Override public void getOutline(View view, android.graphics.Outline outline) {
                outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), 12 * density);
            }
        });
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
        Button importBackup = new Button(this);
        importBackup.setText("Import Product Backup");
        importBackup.setOnClickListener(v -> startActivityForResult(
                new android.content.Intent(android.content.Intent.ACTION_OPEN_DOCUMENT)
                        .addCategory(android.content.Intent.CATEGORY_OPENABLE)
                        .setType("application/json"), PRODUCT_IMPORT_REQUEST));
        content.addView(importBackup, full());
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
        localInventoryDialog = viewer;
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
    private TextView summaryText(String text, String description) {
        TextView view = new TextView(this);
        view.setText(text);
        view.setTextSize(16);
        view.setTextColor(0xff48636d);
        view.setTypeface(android.graphics.Typeface.create("sans-serif", android.graphics.Typeface.BOLD));
        view.setPadding(0, dp(8), dp(8), dp(8));
        view.setContentDescription(description);
        return view;
    }

    private android.graphics.drawable.Drawable roundBackground(int fill, int stroke, int radius) {
        android.graphics.drawable.GradientDrawable background = new android.graphics.drawable.GradientDrawable();
        background.setColor(fill);
        background.setStroke(dp(1), stroke);
        background.setCornerRadius(dp(radius));
        return background;
    }

    private int settingRate(String key, int defaultBasisPoints) {
        return getPreferences(MODE_PRIVATE).getInt(key, defaultBasisPoints);
    }

    private int finalPriceCents(JSONObject row) {
        long price = Math.max(0L, row.optLong("price", 0));
        long total = price;
        int gstRate = settingRate("tax_gst_basis_points", 500);
        int qstRate = settingRate("tax_qst_basis_points", 9975);
        if (row.optInt("folderid", 0) != 0) total += Math.round(price * gstRate / 10000.0);
        if (row.optInt("qst", 0) != 0) total += Math.round(price * qstRate / 10000.0);
        boolean bottleProduct = bottleCategory(row.optString("category", ""));
        int ecoFee = feeCents(row, bottleProduct ? settingRate("tax_eco_cents", 1) : 0,
                "eco_tax", "ecoTax", "ecotax", "clear_fee", "clearFee", "clear_fee_cents");
        int bottleFee = feeCents(row, bottleProduct ? defaultBottleFeeCents(row) : 0,
                "bottle_fee", "bottleFee", "bottlefee", "deposit", "bottle_fee_cents");
        total += ecoFee + bottleFee;
        // BO Panel applies GST/QST to the eco/clear fee when clear-fee taxation
        // is enabled. Bottle Fee itself is added without another tax layer.
        if (ecoFee != 0 && getPreferences(MODE_PRIVATE).getBoolean("tax_eco_taxable", true)) {
            if (row.optInt("folderid", 0) != 0) total += Math.round(ecoFee * gstRate / 10000.0);
            if (row.optInt("qst", 0) != 0) total += Math.round(ecoFee * qstRate / 10000.0);
        }
        return (int) Math.min(Integer.MAX_VALUE, total);
    }

    private boolean bottleCategory(String category) {
        String value = category == null ? "" : category.trim().toUpperCase(java.util.Locale.ROOT);
        return value.equals("BIERE") || value.equals("MOLSON") || value.equals("LABATT")
                || value.equals("SLEEMAN") || value.equals("POPPERS");
    }

    private int defaultBottleFeeCents(JSONObject row) {
        String name = (row.optString("mnemonic", "") + " " + row.optString("subject", ""))
                .toUpperCase(java.util.Locale.ROOT);
        int perBottle = name.matches(".*\\b[2-9](?:\\.\\d+)?\\s*L\\b.*") ? 25 : settingRate("tax_bottle_cents", 10);
        java.util.regex.Matcher multiplier = java.util.regex.Pattern.compile("\\b(\\d+)\\s*[X×]").matcher(name);
        int count = multiplier.find() ? Math.max(1, Integer.parseInt(multiplier.group(1))) : 1;
        return perBottle * count;
    }

    private int feeCents(JSONObject row, int fallbackCents, String... keys) {
        for (String key : keys) if (row.has(key) && !row.isNull(key)) {
            Object value = row.opt(key);
            if (value instanceof Number) return (int) Math.round(((Number) value).doubleValue() < 10
                    ? ((Number) value).doubleValue() * 100 : ((Number) value).doubleValue());
            try { return ProductCatalog.moneyCents(String.valueOf(value)); }
            catch (Exception ignored) { return 0; }
        }
        return fallbackCents;
    }

    private String formatMoneyCents(int cents) {
        return String.format(java.util.Locale.ROOT, "$%.2f", cents / 100.0);
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
        autoSaveIfChanged(field, key, price, false);
    }
    private void autoSaveIfChanged(EditText field, String key, boolean price, boolean name) {
        if (settingValues || selected == null || !field.isEnabled()
                || !ProductFieldChange.changed(selected, key, field.getText().toString(), price, name)) return;
        AutoSaveRequest request = new AutoSaveRequest(selected.optInt("id", -1), field, key, price, name);
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
