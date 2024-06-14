package org.cas.client.platform.bar.panel;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Button;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.just.print.ui.adapter.MyAdapter;

public class PIMTable extends RecyclerView {
    public int selectedRow;
    public MyAdapter adapter;
    private Button lessButton;

    public PIMTable(Context context) {
        super(context);
        init(context);
    }

    public PIMTable(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public PIMTable(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        setLayoutManager(new LinearLayoutManager(context));
    }

    public void setAdapter(MyAdapter adapter) {
        this.adapter = adapter;
        super.setAdapter(adapter);
    }

    public String getValueAt(int r, int c) {
        return adapter.getValueAt(r, c);
    }

    public void setValueAt(String s, int selectedRow, int i) {
        adapter.setValueAt(selectedRow, i, s);
    }

    public int getRowCount() {
        return adapter.getItemCount();
    }

    public void setSelectedRow(int i) {
        selectedRow = i;
        adapter.setSelectedPosition(selectedRow);
    }

    public int getSelectedRow() {
        return selectedRow;
    }
}
