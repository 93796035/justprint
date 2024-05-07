package com.just.print.ui.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.just.print.R;
import com.just.print.app.BarOption;
import com.just.print.app.AikaStyleApplication;
import com.just.print.app.BaseFragment;
import com.just.print.db.bean.Mark;
import com.just.print.ui.holder.ConfigTableViewHolder;
import com.stupid.method.adapter.OnClickItemListener;
import com.stupid.method.adapter.XAdapter2;
import com.stupid.method.reflect.StupidReflect;
import com.stupid.method.reflect.annotation.XClick;
import com.stupid.method.reflect.annotation.XGetValueByView;
import com.stupid.method.reflect.annotation.XViewByID;
import com.stupid.method.widget.flowlayout.FlowListView;

public class ConfigTableFragment extends BaseFragment implements OnClickItemListener {

    @XViewByID(R.id.tableFlowListView)
    FlowListView flowListView;
    XAdapter2<Mark> tableXAdapter;

    /**
     * for storing tempral modification
     */
    private Mark onEditTable = null;

    @XViewByID(R.id.name)
    private TextView name = null;

    @XViewByID(R.id.dspIdx)
    private TextView dspIdx = null;

    @XViewByID(R.id.viewSwitcher)
    private ViewSwitcher modifyViewSwitch = null;

    @Override
    protected int getLayoutId() {
        return R.layout.config_table_fragment;
    }

    int onEditIdx = 0;

    @Override
    public void onCreated(Bundle savedInstanceState) {
        new StupidReflect(this, getView()).init();
        tableXAdapter = new XAdapter2<Mark>(getContext(), ConfigTableViewHolder.class);
        flowListView.setAdapter(tableXAdapter);
        tableXAdapter.setClickItemListener(this);
        loadTables();
    }

    private void loadTables() {
        OrderIdentifierFragment.loadTables();
        tableXAdapter.setData(OrderIdentifierFragment.tables);
        tableXAdapter.notifyDataSetChanged();
    }

    @XClick({R.id.addTable})
    private void addTable(@XGetValueByView(fromId = R.id.etTable) String tableName,
                         @XGetValueByView(fromId = R.id.dspIdxTable) String dspIdx) {

        Mark table = new Mark();        //currently we are using Mark to save table( when deleted flag is set, then it's table.
        table.setName1(tableName);

        try{
            table.setStatus((int)(Float.valueOf(dspIdx) * 100)); //table can be ordered by name, so this field can be used for other stuff if needed.
        }catch(NumberFormatException e){
            table.setStatus(0);
        }
        table.setVersion(-1);  //negative means it's a table not mark. while we can still give it some nubmer to indicate the bill.
        AikaStyleApplication.instance.getDaoMaster().newSession().getMarkDao().insertOrReplace(table);
//        table.updateAndUpgrade();
        BarOption.updataeLastModifyTime(null);
        loadTables();
    }


    @Override
    public void onClickItem(View view, int i) {
        onEditIdx = i;
        final Mark table = tableXAdapter.getItem(i);
        onEditTable = table;

        name.setText(table.getName1());
        dspIdx.setText(String.valueOf(((float) table.getStatus()) / 100.00));

        if (modifyViewSwitch.getDisplayedChild() == 0){
            modifyViewSwitch.setDisplayedChild(1);
        }
    }

    @XClick({R.id.modifyLable})
    private void confirmChange(){
        if (onEditTable != null) {
//            if (!isIP(modifIP.getText().toString())) {
//                showToast("Please input correct ip");
//                return;
//            }

            onEditTable.setName1(name.getText().toString());

            try{
                onEditTable.setStatus((int)(Float.valueOf(dspIdx.getText().toString()) * 100));
            }catch(NumberFormatException e){
                //do nothing.
            }

            onEditTable.update();
            onEditTable = null;
            tableXAdapter.notifyDataSetChanged();
            BarOption.updataeLastModifyTime(null);
        }
        modifyViewSwitch.setDisplayedChild(0);
    }

    @XClick({R.id.delete})
    private void delete(){
        final Mark table = tableXAdapter.getItem(onEditIdx);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("Are you sure to delete \"" + table.getName1() + "\"?");
        builder.setTitle("Notice").setNegativeButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                table.delete();
                loadTables();

                modifyViewSwitch.setDisplayedChild(0);
            }
        });
        builder.setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }
}
