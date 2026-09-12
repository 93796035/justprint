package com.just.print.echo;

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
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Small standalone launcher: no POS database, login, timers or printing are initialized. */
public class EchoPostActivity extends Activity {
    private final Handler main = new Handler(Looper.getMainLooper());
    private final ExecutorService io = Executors.newSingleThreadExecutor();
    private AutoCompleteTextView product;
    private final EditText[] quantities = new EditText[4];
    private final String[] keys = {"stock1", "stock2", "stock3", "monque"};
    private TextView status;
    private Button submit, connection;
    private JSONObject selected;
    private String server, token;
    private boolean settingText, saving, closed;
    private int generation;
    private Runnable pendingSearch;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        setTitle("Echo Post Client");
        setFinishOnTouchOutside(false);
        server = getPreferences(MODE_PRIVATE).getString("server", "");
        token = getPreferences(MODE_PRIVATE).getString("token", "");
        LinearLayout root = column();
        int pad = dp(18);
        root.setPadding(pad, pad, pad, pad);
        TextView title = new TextView(this);
        title.setText("Echo Post Client · 库存修改");
        title.setTextSize(21);
        LinearLayout header = new LinearLayout(this);
        header.setGravity(android.view.Gravity.CENTER_VERTICAL);
        header.addView(title, new LinearLayout.LayoutParams(0, -2, 1));
        connection = new Button(this);
        connection.setText("设置");
        connection.setMinWidth(0);
        connection.setMinimumWidth(0);
        connection.setOnClickListener(v -> configure());
        header.addView(connection, new LinearLayout.LayoutParams(dp(72), -2));
        root.addView(header, full());
        product = new AutoCompleteTextView(this) {
            @Override protected void performFiltering(CharSequence text, int keyCode) {
                // Remote search already matches substrings; do not apply Android's prefix filter.
            }
            @Override protected void replaceText(CharSequence text) {
                settingText = true;
                try { super.replaceText(text); } finally { settingText = false; }
            }
        };
        product.setHint("输入啤酒名称（MNEMONIC）");
        product.setSingleLine(true);
        product.setThreshold(1);
        root.addView(product, full());
        LinearLayout stocks = new LinearLayout(this);
        for (int i = 0; i < 3; i++) {
            LinearLayout field = numberField(i, "Stock" + (i + 1));
            stocks.addView(field, new LinearLayout.LayoutParams(0, -2, 1));
        }
        root.addView(stocks, full());
        root.addView(numberField(3, "Monque"), full());
        submit = new Button(this);
        submit.setText("提交");
        submit.setOnClickListener(v -> save());
        root.addView(submit, full());
        status = new TextView(this);
        status.setTextSize(14);
        status.setAccessibilityLiveRegion(View.ACCESSIBILITY_LIVE_REGION_POLITE);
        root.addView(status, full());
        ScrollView scroll = new ScrollView(this);
        scroll.addView(root);
        setContentView(scroll);
        getWindow().setLayout(Math.min(getResources().getDisplayMetrics().widthPixels - dp(20), dp(540)), -2);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        enableValues(false);
        product.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            public void afterTextChanged(Editable text) {
                if (settingText) return;
                invalidateSelection();
                String query = text.toString().trim();
                if (query.isEmpty()) { message("输入名称后，从下拉列表选择产品。", false); return; }
                int current = generation;
                pendingSearch = () -> search(query, current);
                main.postDelayed(pendingSearch, 250);
            }
        });
        product.setOnItemClickListener((parent, view, position, id) -> {
            Product item = (Product) parent.getItemAtPosition(position);
            generation++;
            if (pendingSearch != null) main.removeCallbacks(pendingSearch);
            fillProduct(item.row);
        });
        message(server.isEmpty() ? "请在连接设置中填写 POS 地址和库存窗口令牌。" : "输入名称后，从下拉列表选择产品。", false);
        product.requestFocus();
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

    private void search(String query, int current) {
        if (server.isEmpty() || token.isEmpty()) { message("请先填写 POS 地址和库存窗口令牌。", false); return; }
        String base = server, auth = token;
        io.execute(() -> {
            try {
                JSONObject result = request(base, auth, "/AikaPos/stockProducts?q=" + URLEncoder.encode(query, "UTF-8"), null);
                JSONArray rows = result.getJSONArray("products");
                ArrayList<Product> matches = new ArrayList<>();
                for (int i = 0; i < rows.length(); i++) matches.add(new Product(rows.getJSONObject(i)));
                main.post(() -> {
                    if (closed || generation != current) return;
                    product.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, matches));
                    if (!matches.isEmpty() && product.hasFocus()) product.showDropDown();
                    message(matches.isEmpty() ? "没有匹配的啤酒。" : "找到 " + matches.size() + " 个产品，请选择。", false);
                });
            } catch (Exception ex) { report(ex, current); }
        });
    }

    private void fillProduct(JSONObject row) {
        try {
            selected = row;
            settingText = true;
            product.setText(row.getString("mnemonic"), false);
            settingText = false;
            for (int i = 0; i < keys.length; i++) quantities[i].setText(String.valueOf(row.getInt(keys[i])));
            enableValues(true);
            message("已填入搜索返回的库存；数量单位与 POS 库存调整一致。", false);
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
                if (!value.matches("-?[0-9]+")) { quantities[i].setError("请输入整数"); quantities[i].requestFocus(); return; }
                int count;
                try { count = Integer.parseInt(value); }
                catch (NumberFormatException ex) { quantities[i].setError("数量超出范围"); return; }
                if (count == Integer.MIN_VALUE) { quantities[i].setError("数量超出范围"); return; }
                values.put(keys[i], count);
                expected.put(keys[i], selected.getInt(keys[i]));
            }
            payload.put("values", values).put("expected", expected);
        } catch (Exception ex) { message(ex.getMessage(), false); return; }
        saving = true;
        product.dismissDropDown();
        generation++;
        if (pendingSearch != null) main.removeCallbacks(pendingSearch);
        product.setEnabled(false);
        connection.setEnabled(false);
        enableValues(false);
        message("正在提交…", false);
        String base = server, auth = token;
        io.execute(() -> {
            String feedback;
            try {
                JSONObject result = request(base, auth, "/AikaPos/stockProducts", payload);
                result.getJSONObject("product");
                feedback = "修改已成功更新";
            } catch (Exception ex) { feedback = ex.getMessage() + "；请重新搜索以获取当前库存。"; }
            String finalFeedback = feedback;
            main.post(() -> {
                if (closed) return;
                saving = false;
                product.setEnabled(true);
                connection.setEnabled(true);
                invalidateSelection();
                product.requestFocus();
                product.selectAll();
                message(finalFeedback, true);
            });
        });
    }

    private static JSONObject request(String base, String token, String path, JSONObject body) throws Exception {
        HttpURLConnection c = (HttpURLConnection) new URL(base + path).openConnection();
        c.setConnectTimeout(5000);
        c.setReadTimeout(10000);
        c.setInstanceFollowRedirects(false);
        c.setRequestProperty("X-AikaPos-Token", token);
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
            try (InputStream in = stream) {
                byte[] buf = new byte[4096]; int n;
                while ((n = in.read(buf)) != -1) {
                    if (bytes.size() + n > 8 * 1024 * 1024) throw new IOException("服务器响应过大");
                    bytes.write(buf, 0, n);
                }
            }
            JSONObject result;
            try { result = new JSONObject(new String(bytes.toByteArray(), StandardCharsets.UTF_8)); }
            catch (Exception ex) { throw new IOException("HTTP " + code + "：请确认 POS 已安装库存查询接口"); }
            if (code != 200) throw new IOException("HTTP " + code + ": " + result.optString("error", "请求失败"));
            return result;
        } catch (java.net.SocketTimeoutException ex) {
            throw new IOException(body == null ? "连接超时" : "响应超时，更新结果尚未确认", ex);
        } finally { c.disconnect(); }
    }

    private void configure() {
        LinearLayout box = column();
        box.setPadding(dp(20), 0, dp(20), 0);
        EditText address = new EditText(this), secret = new EditText(this);
        TextView ipLabel = new TextView(this);
        ipLabel.setText("AikaPos 电脑 IP（默认端口 8880）");
        box.addView(ipLabel);
        address.setHint("例如 192.168.1.20"); address.setSingleLine(); address.setText(server);
        secret.setHint("库存窗口的 X-AikaPos-Token"); secret.setSingleLine(); secret.setText(token);
        box.addView(address); box.addView(secret);
        AlertDialog dialog = new AlertDialog.Builder(this).setTitle("连接艾卡 POS")
                .setView(box).setNegativeButton("取消", null).setPositiveButton("保存", null).create();
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
            } catch (Exception ex) { address.setError("请输入服务器根地址，例如 http://192.168.1.20:8880"); return; }
            if (secret.getText().toString().trim().isEmpty()) { secret.setError("请填写库存窗口令牌"); return; }
            server = url; token = secret.getText().toString().trim();
            getPreferences(MODE_PRIVATE).edit().putString("server", server).putString("token", token).apply();
            invalidateSelection();
            message("连接设置已保存，请输入名称查询。", false);
            dialog.dismiss();
            product.requestFocus(); product.selectAll();
        }));
        dialog.show();
    }

    private void report(Exception ex, int current) {
        main.post(() -> { if (!closed && generation == current) message("读取失败：" + ex.getMessage(), false); });
    }
    private LinearLayout numberField(int index, String name) {
        LinearLayout field = column();
        TextView label = new TextView(this); label.setText(name); field.addView(label);
        EditText input = new EditText(this); input.setSingleLine();
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED);
        input.setSelectAllOnFocus(true); input.setContentDescription(name);
        quantities[index] = input; field.addView(input, full()); return field;
    }
    private void enableValues(boolean enabled) { for (EditText field : quantities) field.setEnabled(enabled); submit.setEnabled(enabled); }
    private void message(String text, boolean toast) {
        status.setText(text); submit.setTooltipText(text);
        if (toast) Toast.makeText(this, text, Toast.LENGTH_LONG).show();
    }
    private LinearLayout column() { LinearLayout view = new LinearLayout(this); view.setOrientation(LinearLayout.VERTICAL); return view; }
    private LinearLayout.LayoutParams full() { return new LinearLayout.LayoutParams(-1, -2); }
    private int dp(int value) { return Math.round(value * getResources().getDisplayMetrics().density); }
    @Override protected void onDestroy() { closed = true; main.removeCallbacksAndMessages(null); io.shutdownNow(); super.onDestroy(); }
    private static class Product {
        final int id; final String name, code; final JSONObject row;
        Product(JSONObject row) throws Exception { this.row = row; id = row.getInt("id"); name = row.getString("mnemonic"); code = row.optString("code"); }
        @Override public String toString() { return name + "  [" + (code.isEmpty() ? id : code) + "]"; }
    }
}
