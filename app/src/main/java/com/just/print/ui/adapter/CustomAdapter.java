package com.just.print.ui.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.just.print.R;

import java.util.HashMap;
import java.util.List;

public class CustomAdapter extends SimpleAdapter {
    private Context context;
    private List<HashMap<String, String>> data;
    private String[] langArray;
    private String[] roleArray;

    public CustomAdapter(Context context, List<HashMap<String, String>> data, int resource, String[] from, int[] to, String[] langArray, String[] roleArray) {
        super(context, data, resource, from, to);
        this.context = context;
        this.data = data;
        this.langArray = langArray;
        this.roleArray = roleArray;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = super.getView(position, convertView, parent);

        // 获取当前项的数据
        HashMap<String, String> item = data.get(position);

        // 获取并转换 Lang 字段
        TextView langTextView = view.findViewById(R.id.txtLang);
        int langIndex = Integer.parseInt(item.get("LANG"));
        langTextView.setText(langArray[langIndex]);

        // 获取并转换 Role 字段
        TextView roleTextView = view.findViewById(R.id.txtRole);
        int roleIndex = Integer.parseInt(item.get("ROLE"));
        roleTextView.setText(roleArray[roleIndex]);

        return view;
    }
}
