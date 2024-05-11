package com.just.print.ui.panel;

import android.database.Cursor;
import android.os.Bundle;

import com.just.print.R;
import com.just.print.app.AikaPos;
import com.just.print.app.BarFrame;
import com.just.print.app.BaseFragment;
import com.just.print.util.L;
import com.stupid.method.reflect.StupidReflect;

import org.cas.client.platform.bar.BarUtil;
import org.cas.client.platform.bar.DBUtil;
import org.cas.client.platform.bar.model.DBConsts;

public class BillListPanel extends BaseFragment {

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_bill_list;
    }

    @Override
    public void onCreated(Bundle savedInstanceState) {
        new StupidReflect(this, getView()).init();
    }

    public static int getANewBillIdx(String tableName, String openTime){
        if(BarUtil.empty(tableName)) {
            tableName = BarFrame.instance.cmbCurTable.getSelectedItem().toString();
        }
        if(BarUtil.empty(openTime)) {
            openTime = BarFrame.instance.valOpenTime.getText().toString();
        }

        int num = 0;
        try {
            StringBuilder sql = new StringBuilder("select billIndex from bill where tableId = '").append(tableName).append("'")
                    .append(" and opentime = '").append(openTime).append("'");
            //TBD:the status curently used "expired", means void and completed bills will all be included, to ensure the new customer number
            //number not duplicated with existing one.
            sql.append(" and (status is null or status < ").append(DBConsts.EXPIRED).append(" and status >= 0)");
            sql.append(" order by id");//if order by billIndex, then "9" will be the last one. so should order by id
            Cursor cursor = AikaPos.db.rawQuery(sql.toString(), null);
            if (cursor != null && cursor.moveToFirst()) {
                num = DBUtil.getCursorInt(cursor, "billIndex");
            }
        } catch (Exception exp) {
            L.d("BillListPane.getANewBillNumber", "found no bill on an already opened table.");
            return 1;
        }
        return num + 1;
    }
}
