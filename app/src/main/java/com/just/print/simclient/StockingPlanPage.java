package com.just.print.simclient;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import android.text.Editable;
import android.text.TextWatcher;
import org.json.*;
import java.util.*;

/** Local, editable stocking plan. POS synchronization is intentionally separate. */
final class StockingPlanPage {
    interface CatalogProvider { JSONArray products() throws Exception; }
    interface SyncHandler { void sync(JSONObject plan) throws Exception; }
    interface ProductClickHandler { void open(int productId); }
    private final Activity activity;
    private final CatalogProvider provider;
    private final SharedPreferences prefs;
    private final SyncHandler syncHandler;
    private final LinearLayout root;
    private final LinearLayout listContent;
    private final LinearLayout rows;
    private final LinearLayout header;
    private final Spinner category;
    private final Spinner weeks;
    private final TextView total;
    private final TextView status;
    private final EditText note;
    private final Button syncButton;
    private JSONObject failedPlan;
    private JSONArray products = new JSONArray();
    private String selectedCategory;
    private int selectedWeeks = 3;
    private final ProductClickHandler productClickHandler;

    StockingPlanPage(Activity activity, String base, CatalogProvider provider, SyncHandler syncHandler, ProductClickHandler productClickHandler) {
        this.activity = activity; this.provider = provider; this.syncHandler = syncHandler; this.productClickHandler = productClickHandler;
        root = new LinearLayout(activity); listContent = new LinearLayout(activity); rows = new LinearLayout(activity); header = new LinearLayout(activity); category = new Spinner(activity); weeks = new Spinner(activity); total = new TextView(activity); status = new TextView(activity); note = new EditText(activity);
        prefs = activity.getSharedPreferences("stocking_plans", Activity.MODE_PRIVATE);
        root.setOrientation(LinearLayout.VERTICAL); root.setPadding(dp(18), dp(18), dp(18), dp(8));
        listContent.setOrientation(LinearLayout.VERTICAL); header.setOrientation(LinearLayout.HORIZONTAL); rows.setOrientation(LinearLayout.VERTICAL);
        ScrollView scroll = new ScrollView(activity); scroll.addView(listContent);
        addHeader("Name", 1.0f); addHeader("QTY", 0.18f); addHeader("Price", 0.28f); addHeader("Total", 0.30f);
        listContent.addView(header, new LinearLayout.LayoutParams(-1, dp(38)));
        listContent.addView(rows);
        note.setHint("Note"); note.setSingleLine(false);
        note.setBackground(panelBorder(0xff9aa9b2, 5));
        total.setTextSize(16);
        listContent.addView(note, new LinearLayout.LayoutParams(-1, dp(52)));
        listContent.addView(total);
        root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));
        LinearLayout controls = new LinearLayout(activity);
        controls.setPadding(0, dp(2), 0, 0);
        category.setBackgroundResource(android.R.drawable.btn_dropdown);
        weeks.setBackgroundResource(android.R.drawable.btn_dropdown);
        controls.addView(category, new LinearLayout.LayoutParams(0, dp(52), 1));
        controls.addView(weeks, new LinearLayout.LayoutParams(0, dp(52), 1));
        note.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s,int st,int c,int a) {}
            public void onTextChanged(CharSequence s,int st,int b,int c) { saveNote(s.toString()); }
            public void afterTextChanged(Editable e) {}
        });
        status.setTextSize(14); status.setTextColor(0xff176b78); root.addView(status);
        root.addView(controls);
        syncButton = new Button(activity); syncButton.setText("Sync to POS"); syncButton.setOnClickListener(v -> sync()); root.addView(syncButton);
        category.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onNothingSelected(AdapterView<?> p) {}
            public void onItemSelected(AdapterView<?> p, View v, int pos, long id) {
                Adapter adapter = p.getAdapter();
                boolean enabled = adapter instanceof ArrayAdapter && ((ArrayAdapter<?>) adapter).isEnabled(pos);
                if (pos >= 0 && enabled) { selectedCategory = String.valueOf(p.getItemAtPosition(pos)); render(); }
                else if (adapter instanceof ArrayAdapter) {
                    int old = -1; for (int i=0;i<adapter.getCount();i++) if (String.valueOf(adapter.getItem(i)).equals(selectedCategory)) old=i;
                    if (old >= 0) p.setSelection(old);
                }
            }
        });
        weeks.setAdapter(new ArrayAdapter<>(activity, android.R.layout.simple_spinner_item, new String[]{"1 week","2 weeks","3 weeks","4 weeks","5 weeks"}));
        weeks.setSelection(2);
        weeks.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onNothingSelected(AdapterView<?> p) {}
            public void onItemSelected(AdapterView<?> p, View v, int pos, long id) { selectedWeeks = pos + 1; render(); }
        });
    }
    View view() { return root; }
    void activate() {
        try { products = provider.products(); loadCategories(); render(); }
        catch (Exception ex) { rows.removeAllViews(); rows.addView(text("Could not load local inventory: " + ex.getMessage())); }
    }
    private void loadCategories() throws Exception {
        // The catalog is already ordered like the POS Menu Panel. Preserve that
        // order so group headers stay immediately before their child brands.
        LinkedHashSet<String> names = new LinkedHashSet<>();
        for (int i=0;i<products.length();i++) {
            String name = products.getJSONObject(i).optString("category", "");
            boolean duplicate = false;
            for (String existing : names) if (existing.equalsIgnoreCase(name)) { duplicate = true; break; }
            if (!duplicate) names.add(name);
        }
        ArrayList<String> options = new ArrayList<>();
        String[] allowed = {"LABATT", "MOLSON", "SLEEMAN", "POPPERS", "BENSON", "IMPERIAL", "JTI MD",
                "COSTCO", "LAIT", "QUECAN", "MONDOUX"};
        for (String name : names) for (String wanted : allowed)
            if (name.equalsIgnoreCase(wanted)) { options.add(name); break; }
        if (selectedCategory == null || !options.contains(selectedCategory) || ProductCatalog.categoryColor(selectedCategory) != 0) {
            selectedCategory = options.contains("BENSON") ? "BENSON" : "";
            if (selectedCategory.isEmpty()) for (String option : options) if (ProductCatalog.categoryColor(option) == 0) { selectedCategory = option; break; }
        }
        category.setAdapter(new ArrayAdapter<String>(activity, android.R.layout.simple_spinner_item, options) {
            public boolean isEnabled(int position) { return true; }
            public boolean areAllItemsEnabled() { return true; }
            public View getView(int p, View c, ViewGroup parent) { TextView v=(TextView)super.getView(p,c,parent); int color=ProductCatalog.categoryColor(getItem(p)); v.setTextColor(color == 0 ? 0xff16344b : color); return v; }
            public View getDropDownView(int p, View c, ViewGroup parent) { TextView v=(TextView)super.getDropDownView(p,c,parent); int color=ProductCatalog.categoryColor(getItem(p)); v.setTextColor(color == 0 ? 0xff16344b : color); v.setEnabled(true); v.setAlpha(1f); return v; }
        });
        int pos = options.indexOf(selectedCategory); if (pos >= 0) category.setSelection(pos);
    }
    private void render() {
        rows.removeAllViews(); long grand=0; int count=0;
        note.setText(prefs.getString(noteKey(), ""));
        for (int i=0;i<products.length();i++) try {
            JSONObject p=products.getJSONObject(i); if (!selectedCategory.equalsIgnoreCase(p.optString("category"))) continue;
            int weekly=Math.max(p.optInt("weekly_sales_qty", p.optInt("weeklySalesQty", 0)), p.optInt("lastSevenDaysQty", 0));
            JSONObject sales=p.optJSONObject("movingSales");
            if (sales != null) { int recent=0; for (java.util.Iterator<String> it=sales.keys(); it.hasNext();) recent += Math.max(0, sales.optInt(it.next(), 0)); weekly=Math.max(weekly, recent); }
            int stock=p.optInt("stock1",0)+p.optInt("stock2",0)+p.optInt("stock3",0);
            int pack=Math.max(1,p.optInt("case_qty", p.optInt("caseQty",1)));
            int qty=Math.max(0,(weekly*selectedWeeks-stock+pack-1)/pack); if(qty<=0) continue;
            JSONObject saved=readPlan(p.optInt("id")); if(saved!=null) qty=saved.optInt("quantity",qty);
            String defaultPrice=ProductCatalog.money(p,"cost");
            int unitPrice=saved != null && saved.has("price") ? saved.optInt("price",0) : parse(defaultPrice);
            int rowTotal=saved != null && saved.has("total") ? saved.optInt("total",qty*unitPrice) : qty*unitPrice;
            EditText q=number(String.valueOf(qty)); EditText price=number(String.format(Locale.ROOT,"%.2f",unitPrice/100.0)); EditText lineTotal=number(String.format(Locale.ROOT,"%.2f",rowTotal/100.0));
            TextView name=text(p.optString("mnemonic",p.optString("subject",p.optString("code"))));
            name.setTextColor(0xff1976d2);
            name.setPaintFlags(name.getPaintFlags() | android.graphics.Paint.UNDERLINE_TEXT_FLAG);
            name.setClickable(true);
            name.setContentDescription("Edit " + name.getText());
            name.setOnClickListener(v -> productClickHandler.open(p.optInt("id", -1)));
            LinearLayout line=new LinearLayout(activity); line.setGravity(android.view.Gravity.CENTER_VERTICAL); line.addView(name,new LinearLayout.LayoutParams(0,dp(52),1)); line.addView(q,new LinearLayout.LayoutParams(dp(58),dp(48))); line.addView(price,new LinearLayout.LayoutParams(dp(78),dp(48))); line.addView(lineTotal,new LinearLayout.LayoutParams(dp(82),dp(48))); rows.addView(line); count++;
            final JSONObject product=p; final boolean[] syncing={false};
            TextWatcher link = new TextWatcher() {
                public void beforeTextChanged(CharSequence s,int st,int c,int a) {}
                public void onTextChanged(CharSequence s,int st,int before,int countChars) {
                    if (syncing[0]) return;
                    try {
                        int quantity=Math.max(0,Integer.parseInt(q.getText().toString()));
                        if (s == q.getText() || s == price.getText()) {
                            int unit=parse(price.getText().toString());
                            syncing[0]=true; lineTotal.setText(String.format(Locale.ROOT,"%.2f",quantity*unit/100.0)); syncing[0]=false;
                        } else if (quantity > 0) {
                            int line=parse(lineTotal.getText().toString());
                            syncing[0]=true; price.setText(String.format(Locale.ROOT,"%.2f",line/(double)quantity/100.0)); syncing[0]=false;
                        }
                        saveRow(product,q,price,lineTotal);
                    } catch(Exception ignored) {}
                }
                public void afterTextChanged(Editable e) {}
            };
            q.addTextChangedListener(link); price.addTextChangedListener(link); lineTotal.addTextChangedListener(link);
            q.setOnFocusChangeListener((v,f)->{if(!f)saveRow(product,q,price,lineTotal);}); price.setOnFocusChangeListener((v,f)->{if(!f)saveRow(product,q,price,lineTotal);}); lineTotal.setOnFocusChangeListener((v,f)->{if(!f)saveRow(product,q,price,lineTotal);});
            grand += Math.max(0, parse(lineTotal.getText().toString()));
        } catch(Exception ignored) {}
        total.setText("Total: " + String.format(Locale.ROOT,"%.2f",grand/100.0) + "  ("+count+" item(s))");
    }
    private JSONObject readPlan(int id) throws Exception { String s=prefs.getString(key(id),null); return s==null?null:new JSONObject(s); }
    private void saveRow(JSONObject p, EditText q, EditText price, EditText lineTotal) { try { prefs.edit().putString(key(p.getInt("id")),new JSONObject().put("quantity",Math.max(0,Integer.parseInt(q.getText().toString()))).put("price",parse(price.getText().toString())).put("total",parse(lineTotal.getText().toString())).put("note",note.getText().toString()).toString()).commit(); } catch(Exception ignored) {} }
    private String key(int id){return (selectedCategory==null?"":selectedCategory)+":"+selectedWeeks+":"+id;}
    private String noteKey(){return (selectedCategory==null?"":selectedCategory)+":"+selectedWeeks+":note";}
    private void saveNote(String value){prefs.edit().putString(noteKey(), value).commit();}
    private void sync() {
        try { JSONArray out=new JSONArray(); int totalCents=0;
            for(int i=0;i<products.length();i++){ JSONObject p=products.getJSONObject(i); if(!selectedCategory.equalsIgnoreCase(p.optString("category"))) continue; JSONObject s=readPlan(p.optInt("id")); if(s==null||s.optInt("quantity",0)<=0) continue; int qty=s.optInt("quantity"), price=s.optInt("price",0); out.put(new JSONObject().put("productId",p.getInt("id")).put("name",p.optString("mnemonic",p.optString("subject",p.optString("code")))).put("category",selectedCategory).put("quantity",qty).put("totalCents",qty*price)); totalCents += qty*price; }
            if(out.length()==0) throw new IllegalArgumentException("There are no stocking quantities to sync.");
            JSONObject plan=failedPlan == null ? new JSONObject().put("draftId",UUID.randomUUID().toString()).put("category",selectedCategory).put("weeks",selectedWeeks).put("note",note.getText().toString()).put("totalCents",totalCents).put("rows",out) : failedPlan;
            status.setText("Saving stocking plan to POS…"); syncButton.setEnabled(false);
            failedPlan = plan; syncHandler.sync(plan); for(int i=0;i<products.length();i++) { JSONObject p=products.getJSONObject(i); prefs.edit().remove(key(p.getInt("id"))).remove(noteKey()).commit(); } render();
            failedPlan = null; syncButton.setText("Sync to POS");
            status.setText("Saved to POS successfully. Local draft cleared."); syncButton.setEnabled(true);
            Toast.makeText(activity,"Stocking plan synced to POS.",Toast.LENGTH_LONG).show();
        } catch(Exception ex) { syncButton.setText("Retry sync"); syncButton.setEnabled(true); status.setText("POS sync failed: " + (ex.getMessage() == null ? ex.getClass().getSimpleName() : ex.getMessage()) + "\nDraft retained locally. Tap Retry sync."); Toast.makeText(activity,"Stocking sync failed. Draft retained; tap Retry sync.",Toast.LENGTH_LONG).show(); }
    }
    private EditText number(String value){EditText e=new EditText(activity);e.setText(value.equals("—")?"":value);e.setInputType(2|8192);e.setSelectAllOnFocus(true);return e;}
    private TextView text(String value){TextView t=new TextView(activity);t.setText(value);t.setTextSize(15);t.setTextColor(0xff16344b);return t;}
    private void addHeader(String value, float weight) { TextView t=text(value); t.setTextSize(12); t.setTypeface(null, android.graphics.Typeface.BOLD); int width = value.equals("QTY") ? 58 : value.equals("Price") ? 78 : value.equals("Total") ? 82 : 0; header.addView(t, width == 0 ? new LinearLayout.LayoutParams(0,dp(38),1) : new LinearLayout.LayoutParams(dp(width),dp(38))); }
    private GradientDrawable panelBorder(int color, int radius) { GradientDrawable d = new GradientDrawable(); d.setColor(0x00ffffff); d.setCornerRadius(dp(radius)); d.setStroke(dp(1), color); return d; }
    private int parse(String value){try{return (int)Math.round(Double.parseDouble(value.replace(',','.'))*100);}catch(Exception e){return 0;}}
    private int dp(int n){return Math.round(n*activity.getResources().getDisplayMetrics().density);}
}
