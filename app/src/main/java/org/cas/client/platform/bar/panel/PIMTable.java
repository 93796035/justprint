package org.cas.client.platform.bar.panel;

import android.content.Context;
import android.widget.ListView;

import com.just.print.ui.adapter.MyAdapter;

public class PIMTable extends ListView {
    public PIMTable(Context context) {
        super(context);
    }

    public void setSelectedRow(int i) {
        setSelection(i);
    }


    public Object getValueAt(int r, int c) {
        return ((MyAdapter)getAdapter()).getValueAt(r, c);
    }
    public void setValueAt(String s, int selectedRow, int i) {
        ((MyAdapter)getAdapter()).setValueAt(selectedRow, i, s);
    }

    public int getSelectedRow() {
        return getSelectedItemPosition();
    }

    public int getRowCount() {
        return getAdapter().getCount();
    }
}
