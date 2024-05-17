package com.just.print.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class MyAdapter extends ArrayAdapter<String[]> {
    private List<String[]> mData;
    private Context mContext;

    public MyAdapter(Context context, List<String[]> data) {
        super(context, android.R.layout.simple_list_item_1, data);
        mContext = context;
        mData = data;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(android.R.layout.simple_list_item_1, parent, false);
            holder = new ViewHolder();
            holder.textView = convertView.findViewById(android.R.id.text1);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        // 假设每行数据显示为一个拼接的字符串
        String[] rowData = mData.get(position);
        StringBuilder sb = new StringBuilder();
        for (String cell : rowData) {
            sb.append(cell).append(" ");
        }
        holder.textView.setText(sb.toString().trim());

        return convertView;
    }

    public void setValueAt(int position, int column, String value) {
        mData.get(position)[column] = value;
        notifyDataSetChanged(); // 通知ListView更新界面
    }

    static class ViewHolder {
        TextView textView;
    }
}
