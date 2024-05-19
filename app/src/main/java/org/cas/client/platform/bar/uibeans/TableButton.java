package org.cas.client.platform.bar.uibeans;

import android.graphics.Color;
import android.graphics.Typeface;
import android.util.TypedValue;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.just.print.app.BarFrame;
import org.cas.client.platform.casutil.L;

import org.cas.client.platform.bar.BarOption;
import org.cas.client.platform.bar.BarUtil;
import org.cas.client.platform.bar.model.DBUtil.getCursorString(rs,;
import org.cas.client.platform.bar.dialog.TableDlg;
import org.cas.client.platform.bar.model.DBConsts;

import java.util.Arrays;
import java.util.Date;
import java.util.List;


public class TableButton extends Button {

    private int id = -1;
    private String tableName;
    private int type;
    private String startTime;
    private String openTime;
    private String waiterName;
    private int total;
    List<Integer> typeList = Arrays.asList(TableDlg.typeAry);
    private int status;
    private int DEFALTSIZE = BarOption.getDefaultTableWidth();

    public TableButton() {
        super(BarFrame.instance);
        String color = BarOption.getBK("Table");
        if(color != null) {
            setBackgroundColor(Color.parseColor(color));
        }else{
            setBackgroundColor(Color.GRAY);
        }
    }

    public void open() {
        setStatus(1);
        int resId = getResources().getIdentifier("t".concat(String.valueOf(type).concat("s")).concat(".png"),
                "drawable", getContext().getPackageName());
        if (resId != 0) {
            setBackgroundResource(resId);
        }

        String openTime = BarUtil.df.format(new Date());
        setOpenTime(openTime);
        setWaiterName(BarFrame.instance.getCurrentUser().getNickName());

        DBUtil.getCursorString(rs,.updateTableStatusInDB(getTableName(), openTime);
        int billId = BarFrame.instance.createAnEmptyBill(getTableName(), openTime, 0, 0);
        BarFrame.instance.setCurBillID(billId);

        //process the deliever type of table by making the opentime of both table and bill to the user set time.
        if(tableName.toUpperCase().startsWith("D")) {
//            CustomerMatchingDlg customerMatchingDlg = new CustomerMatchingDlg(AikaPos.instance);
//            customerMatchingDlg.setVisible(true);
//
//            //update the table with the client info
//            openTime = BarUtil.df.format(customerMatchingDlg.openTime);
//            setOpenTime(openTime);
//            StringBuilder sql = new StringBuilder("update dining_Table set dsp_index = ").append(customerMatchingDlg.selectedID).append(",")
//                    .append(" opentime = '").append(openTime).append("'")
//                    .append(" WHERE name = '").append(tableName).append("'");
//            try {
//                PIMDBModel.getStatement().executeUpdate(sql.toString());
//                sql = new StringBuilder("update bill set opentime = '").append(openTime).append("' where id = ").append(billId);
//                PIMDBModel.getStatement().executeUpdate(sql.toString());
//            }catch(Exception exp) {
//                L.e("openATable", "exception when openning a table: " + sql, exp);
//            }finally {
//                customerMatchingDlg.dispose();
//            }
        }
    }

    //these properties are better to be set together. because they need each other to determine the behavior.
    public RelativeLayout.LayoutParams setTypeStatusAndName(String openTime, String startTime, int type, int status, String tableName) {
        if(status > this.status) {
            this.setStatus(status);
        }else {
            status = this.status; 	//if status from bill is 0, while the button's status is opened, then make the status opened, to make the icon and color right.
        }
        this.setTableName(tableName);
        this.setOpenTime(openTime);
        this.setStartTime(startTime);
        this.type = type;

        String formatStart = "<p style = \"margin:0px\"><font style=\"line-height:0.5;\" size=\"";
        String formatEnd = "</font></p>";

        RelativeLayout.LayoutParams params = null;
        //3 type of tables:logo, customized icon, out of box icon
        if(type == DBConsts.LOGO_TYPE) {			//logo button case==========================
            setBackgroundResource(prepareIcon(type, status));
            params = setSize(60, 60);
        }else if(type > DBConsts.LOGO_TYPE) {		//user customized icon case==========================
            if (status == 0 || BarOption.isCounterMode()) {			//not openned or in counter mode.
                setText(tableName);
                params = setSize(DEFALTSIZE, DEFALTSIZE);
                setTextSize(24);
                setTypeface(null, Typeface.NORMAL);
                setAlpha(1.0f);
                setBackgroundResource(prepareIcon(type, status));	//counter mode all chair will not be displaying.
            }else {													//openned
                long minute = 0;
                try {
                    minute = (openTime == null || openTime.length() == 0) ? 0l : (new Date().getTime() - BarUtil.df.parse(openTime).getTime())/1000/60;
                }catch(Exception e) {
                    L.e("TableButton ", "exception when parser openTime which is :" + openTime, e);
                }
                StringBuilder sb = new StringBuilder("<html><center>");
                sb.append(formatStart).append(BarOption.getTableNameFontSize()).append("\">").append(tableName).append(formatEnd);

                if(BarOption.showWaiterOnOpennedTable()) {
                    sb.append(formatStart).append(BarOption.getWaiterOnTableFontSize()).append("\">").append(waiterName).append(formatEnd);
                }

                if(BarOption.showTimeOnOpennedTable()) {
                    sb.append(formatStart).append(BarOption.getTimeOnTableFontSize()).append("\">").append(minute).append("m").append(formatEnd);
                }

                if(BarOption.showMoneyOnOpennedTable()) {
                    sb.append(formatStart).append(BarOption.getMoneyOnTableFontSize()).append("\">").append(BarOption.getMoneySign()).append(BarUtil.formatMoney(total/100.0)).append(formatEnd);
                }
                sb.append("</center></html>");
                setText(sb.toString());

                setTextSize(14);
                setTypeface(null, Typeface.BOLD);
                params = setSize(DEFALTSIZE, DEFALTSIZE);
                setAlpha(0.5f);
                setBackgroundResource(prepareIcon(type, status));
                setBackgroundColor(dicideColor(minute, status));
            }
        }else {															//system tables==========================
            if (type < 0)
                type = 0 - type;

            if (status == 0) {// || BarOption.isCounterMode()) {		//not opened or in counter mode， make the icon opaque.
                setText(tableName);
                setTextSize(14);
                setTypeface(null, Typeface.BOLD);
                setTextColor(Color.parseColor(BarOption.getTableFontColor()));

                if(BarOption.isCounterMode()) {
                    params = setSize(DEFALTSIZE, DEFALTSIZE);
                    setBackgroundResource(prepareIcon(0, status));
                }else {
                    int p = typeList.indexOf(Math.abs(type));		//which kind of table.
                    params = setSize(TableDlg.widthAry[p], TableDlg.heightAry[p]);
                    setBackgroundResource(prepareIcon(type, status));
                }
                setAlpha(1.0f);
            } else {											//if opened and in table mode, then the icon will be not opaque.
                long minute = 0;
                startTime = BarOption.isCounterMode() ? startTime : openTime; //table mode use opentime to calculate the waited time.
                try {
                    minute = (startTime == null || startTime.length() == 0 || total == 0 )  ? 0l : (new Date().getTime() - BarUtil.df.parse(startTime).getTime())/1000/60;
                }catch(Exception e) {
                    L.e("TableButton ", "exceptio when parser startTime which is :" + startTime, e);
                }

                StringBuilder sb = new StringBuilder("<html><center>");
                if(BarOption.showNumberOnTable()) {
                    sb.append(formatStart).append(BarOption.getTableNameFontSize()).append("\">").append(tableName).append(formatEnd);
                }
                if(BarOption.showWaiterOnOpennedTable()) {
                    sb.append(formatStart).append(BarOption.getWaiterOnTableFontSize()).append("\">").append(waiterName).append(formatEnd);
                }

                if(BarOption.showTimeOnOpennedTable()) {
                    sb.append(formatStart).append(BarOption.getTimeOnTableFontSize()).append("\">").append(minute).append("m").append(formatEnd);
                }

                if(BarOption.showMoneyOnOpennedTable()) {
                    sb.append(formatStart).append(BarOption.getMoneyOnTableFontSize()).append("\">").append(BarOption.getMoneySign()).append(BarUtil.formatMoney(total/100.0)).append(formatEnd);
                }

                sb.append("</center></html>");
                setText(sb.toString());

                setTextSize(14);
                setTypeface(null, Typeface.BOLD);
                setTextColor(Color.parseColor(BarOption.getOpenTableFontColor()));

                int p = typeList.indexOf(Math.abs(type));		//which kind of table.
                params = setSize(TableDlg.widthAry[p], TableDlg.heightAry[p]);
                if((BarOption.showWaiterOnOpennedTable() || BarOption.showTimeOnOpennedTable() || BarOption.showMoneyOnOpennedTable())
                        && TableDlg.heightAry[p] < DEFALTSIZE) {	//if the height is not enough, make it bigger.
                    params = setSize(TableDlg.widthAry[p], DEFALTSIZE);
                }
                setAlpha(0.5f);
                setBackgroundResource(prepareIcon(type, status));
                setBackgroundColor(dicideColor(minute, status));
            }
        }

        //final adjustment of the icon, if the table name starts from HDFUX, will use spacial icons.
        if(tableName != null && tableName.length() > 0) {
            char firstLetter = tableName.toUpperCase().charAt(0);
            if(firstLetter >= 'A' && firstLetter <= 'Z'){
                String path = "t".concat(String.valueOf(firstLetter));
                int resId = getResources().getIdentifier(path.concat(".png"),"drawable", getContext().getPackageName());
                if(resId < 0){
                    path = "t0";
                    if(status > 0) {
                        path = path.concat("s");
                    }
                    resId = getResources().getIdentifier(path.concat(".png"),"drawable", getContext().getPackageName());
                }
                setBackgroundResource(resId);
                params = setSize(DEFALTSIZE, DEFALTSIZE);
            }
        }
        return params;
    }

    private RelativeLayout.LayoutParams setSize(int width, int height) {
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, width, getResources().getDisplayMetrics()),
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, height, getResources().getDisplayMetrics())
        );
        setLayoutParams(params);
        return params;
    }

    private int dicideColor(long minute, int status) {
        int colorFR = Color.GRAY;
        switch (status) {
            case DBConsts.ORIGINAL:
                colorFR = Color.parseColor("#fadc78");
                break;
            case DBConsts.OPENNED:
                colorFR = Color.parseColor("#dcfa78");
                break;
            case DBConsts.SAVED:
                colorFR = Color.parseColor("#fadc78");
                break;
            case DBConsts.BILL_PRINTED:
                colorFR = Color.parseColor("#6ec878");
                break;
            case DBConsts.SUSPENDED:
                colorFR = Color.parseColor(BarOption.getBK("TableSelected"));
                break;
        }

        if(BarOption.needNoticeWhenOvertime()) {
            if((minute > 30 && status == DBConsts.ORIGINAL)) {
                colorFR = Color.RED;
            }else if(minute > 20 && status == DBConsts.SAVED) {
                colorFR = Color.CYAN;
            }else if(minute > 60 && (status == DBConsts.ORIGINAL || status == DBConsts.SAVED)) {
                colorFR = Color.CYAN;
            }
        }

        return colorFR;
    }

    private int prepareIcon(int type, int status) {
        StringBuilder path = new StringBuilder();
        if(type > DBConsts.LOGO_TYPE) {	//customized table
            path.append("t").append(type - DBConsts.LOGO_TYPE);
        }else if(type == DBConsts.LOGO_TYPE) {  //logo
            path.append("t").append("logo");
        }else {
            path.append("t").append(type);
        }

        if(status > 0 && type < DBConsts.LOGO_TYPE) {
            path = path.append("s");
        }
//        path.append(".png");

        int resId = getResources().getIdentifier(path.toString(),"drawable", getContext().getPackageName());
        if(resId < 0){
            path = new StringBuilder("t0");
            if(status > 0 && status < DBConsts.LOGO_TYPE) {
                path = path.append("s");
            }
            resId = getResources().getIdentifier(path.toString(),"drawable", getContext().getPackageName());
        }
        return resId;
    }

    public String getOpenTime() {
        return openTime;
    }
    public void setOpenTime(String openTime) {
        this.openTime = openTime;
    }

    public int getStatus() {
        return status;
    }
    public void setStatus(int status) {
        this.status = status;
    }

    public String getWaiterName() {
        return waiterName;
    }
    public void setWaiterName(String waiterName) {
        this.waiterName = waiterName == null || waiterName.toLowerCase().equals("null") ? "" : waiterName;
    }

    public int getTotal() {
        return total;
    }
    public void setTotal(int total) {
        this.total = total;
    }

    public String getTableName() {
        return tableName;
    }
    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public int getId() {
        return id;
    }
    public void setId(int index) {
        this.id = index;
    }

    public int getType() {
        return type;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

}
