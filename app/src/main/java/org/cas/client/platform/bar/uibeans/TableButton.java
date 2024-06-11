package org.cas.client.platform.bar.uibeans;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.just.print.R;
import com.just.print.app.BarFrame;

import org.cas.client.platform.bar.BarOption;
import org.cas.client.platform.bar.BarUtil;
import org.cas.client.platform.bar.dialog.TableDlg;
import org.cas.client.platform.bar.model.DBConsts;
import org.cas.client.platform.bar.model.DBUtil;
import org.cas.client.platform.casutil.L;

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
        int resId = prepareIcon(type,1);
        if (resId != 0) {
            setBackgroundResource(resId);
        }

        String openTime = BarUtil.df.format(new Date());
        setOpenTime(openTime);
        setWaiterName(BarFrame.instance.getCurrentUser().getNickName());

        DBUtil.updateTableStatusInDB(getTableName(), openTime);
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
//            StringBuilder sql = new StringBuilder("update dining_Table set displayIdx = ").append(customerMatchingDlg.selectedID).append(",")
//                    .append(" opentime = '").append(openTime).append("'")
//                    .append(" WHERE name = '").append(tableName).append("'");
//            try {
//                PIMDBModel.getStatement().executeUpdate(sql.toString());
//                sql = new StringBuilder("update bill set opentime = '").append(openTime).append("' where _id = ").append(billId);
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
        if (status > this.status) {
            this.setStatus(status);
        } else {
            status = this.status; // if status from bill is 0, while the button's status is opened, then make the status opened, to make the icon and color right.
        }
        this.setTableName(tableName);
        this.setOpenTime(openTime);
        this.setStartTime(startTime);
        this.type = type;

        SpannableString spannableString;
        RelativeLayout.LayoutParams params = null;

        // 3 types of tables: logo, customized icon, out of box icon
        if (type == DBConsts.LOGO_TYPE) { // logo button case==========================
            setBackgroundResource(prepareIcon(type, status));
            params = setSize(60, 60);
        } else if (type > DBConsts.LOGO_TYPE) { // user customized icon case==========================
            if (status == 0 || BarOption.isCounterMode()) { // not opened or in counter mode.
                setText(tableName);
                params = setSize(DEFALTSIZE, DEFALTSIZE);
                setTextSize(24);
                setTypeface(null, Typeface.NORMAL);
                setAlpha(1.0f);
                setBackgroundResource(prepareIcon(type, status)); // counter mode all chair will not be displaying.
            } else { // opened
                long minute = 0;
                try {
                    minute = (openTime == null || openTime.length() == 0) ? 0L : (new Date().getTime() - BarUtil.df.parse(openTime).getTime()) / 1000 / 60;
                } catch (Exception e) {
                    L.e("TableButton ", "exception when parser openTime which is :" + openTime, e);
                }

                String tableInfo = tableName;
                if (BarOption.showWaiterOnOpennedTable()) {
                    tableInfo += "\n" + waiterName;
                }
                if (BarOption.showTimeOnOpennedTable()) {
                    tableInfo += "\n" + minute + "m";
                }
                if (BarOption.showMoneyOnOpennedTable()) {
                    tableInfo += "\n" + BarOption.getMoneySign() + BarUtil.formatMoney(total / 100.0);
                }

                spannableString = new SpannableString(tableInfo);
                // Set styles for each part of the text
                int start = 0;
                int end = tableName.length();
                spannableString.setSpan(new StyleSpan(Typeface.BOLD), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                if (BarOption.showWaiterOnOpennedTable()) {
                    start = end + 1;
                    end = start + waiterName.length();
                    spannableString.setSpan(new RelativeSizeSpan(0.8f), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    spannableString.setSpan(new StyleSpan(Typeface.NORMAL), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }

                if (BarOption.showTimeOnOpennedTable()) {
                    start = end + 1;
                    end = start + String.valueOf(minute).length() + 1; // include 'm'
                    spannableString.setSpan(new RelativeSizeSpan(0.8f), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    spannableString.setSpan(new StyleSpan(Typeface.NORMAL), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }

                if (BarOption.showMoneyOnOpennedTable()) {
                    start = end + 1;
                    end = start + (BarOption.getMoneySign() + BarUtil.formatMoney(total / 100.0)).length();
                    spannableString.setSpan(new RelativeSizeSpan(0.8f), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    spannableString.setSpan(new StyleSpan(Typeface.NORMAL), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }

                setText(spannableString);
                setTextSize(14);
                setTypeface(null, Typeface.BOLD);
                params = setSize(DEFALTSIZE, DEFALTSIZE);
                setAlpha(0.5f);
                setBackgroundResource(prepareIcon(type, status));
                setBackgroundColor(dicideColor(minute, status));
            }
        } else { // system tables==========================
            if (type < 0)
                type = 0 - type;

            if (status == 0) { // not opened or in counter mode， make the icon opaque.
                setText(tableName);
                setTextSize(14);
                setTypeface(null, Typeface.BOLD);
                setTextColor(Color.parseColor(BarOption.getTableFontColor()));

                if (BarOption.isCounterMode()) {
                    params = setSize(DEFALTSIZE, DEFALTSIZE);
                    setBackgroundResource(prepareIcon(0, status));
                } else {
                    int p = typeList.indexOf(Math.abs(type)); // which kind of table.
                    params = setSize(TableDlg.widthAry[p], TableDlg.heightAry[p]);
                    setBackgroundResource(prepareIcon(type, status));
                }
                setAlpha(1.0f);
            } else { // if opened and in table mode, then the icon will be not opaque.
                long minute = 0;
                startTime = BarOption.isCounterMode() ? startTime : openTime; // table mode use opentime to calculate the waited time.
                try {
                    minute = (startTime == null || startTime.length() == 0 || total == 0) ? 0L : (new Date().getTime() - BarUtil.df.parse(startTime).getTime()) / 1000 / 60;
                } catch (Exception e) {
                    L.e("TableButton ", "exception when parser startTime which is :" + startTime, e);
                }

                String tableInfo = tableName;
                if (BarOption.showNumberOnTable()) {
                    tableInfo += "\n" + tableName;
                }
                if (BarOption.showWaiterOnOpennedTable()) {
                    tableInfo += "\n" + waiterName;
                }
                if (BarOption.showTimeOnOpennedTable()) {
                    tableInfo += "\n" + minute + "m";
                }
                if (BarOption.showMoneyOnOpennedTable()) {
                    tableInfo += "\n" + BarOption.getMoneySign() + BarUtil.formatMoney(total / 100.0);
                }

                spannableString = new SpannableString(tableInfo);
                // Set styles for each part of the text
                int start = 0;
                int end = tableName.length();
                if (BarOption.showNumberOnTable()) {
                    spannableString.setSpan(new StyleSpan(Typeface.BOLD), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }

                if (BarOption.showWaiterOnOpennedTable()) {
                    start = end + 1;
                    end = start + waiterName.length();
                    spannableString.setSpan(new RelativeSizeSpan(0.8f), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    spannableString.setSpan(new StyleSpan(Typeface.NORMAL), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }

                if (BarOption.showTimeOnOpennedTable()) {
                    start = end + 1;
                    end = start + String.valueOf(minute).length() + 1; // include 'm'
                    spannableString.setSpan(new RelativeSizeSpan(0.8f), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    spannableString.setSpan(new StyleSpan(Typeface.NORMAL), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }

                if (BarOption.showMoneyOnOpennedTable()) {
                    start = end + 1;
                    end = start + (BarOption.getMoneySign() + BarUtil.formatMoney(total / 100.0)).length();
                    spannableString.setSpan(new RelativeSizeSpan(0.8f), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    spannableString.setSpan(new StyleSpan(Typeface.NORMAL), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }

                setText(spannableString);
                setTextSize(14);
                setTypeface(null, Typeface.BOLD);
                setTextColor(Color.parseColor(BarOption.getOpenTableFontColor()));

                int p = typeList.indexOf(Math.abs(type)); // which kind of table.
                params = setSize(TableDlg.widthAry[p], TableDlg.heightAry[p]);
                if ((BarOption.showWaiterOnOpennedTable() || BarOption.showTimeOnOpennedTable() || BarOption.showMoneyOnOpennedTable())
                        && TableDlg.heightAry[p] < DEFALTSIZE) { // if the height is not enough, make it bigger.
                    params = setSize(TableDlg.widthAry[p], DEFALTSIZE);
                }
                setAlpha(0.5f);
                createLayeredBackground(prepareIcon(type, status), dicideColor(minute, status));
            }
        }

        // final adjustment of the icon, if the table name starts from HDFUX, will use special icons.
        if (tableName != null && tableName.length() > 0) {
            char firstLetter = tableName.toUpperCase().charAt(0);
            if (firstLetter >= 'A' && firstLetter <= 'Z') {
                String path = "t".concat(String.valueOf(firstLetter));
                int resId = getResources().getIdentifier(path.concat(".png"), "drawable", getContext().getPackageName());
                if (resId < 0) {
                    path = "t0";
                    if (status > 0) {
                        path = path.concat("s");
                    }
                    resId = getResources().getIdentifier(path.concat(".png"), "drawable", getContext().getPackageName());
                }
                setBackgroundResource(resId);
                params = setSize(DEFALTSIZE, DEFALTSIZE);
            }
        }
        return params;
    }

    public void createLayeredBackground(int bkImage, int bkColor) {
        // 加载背景图片
        Drawable backgroundImage = BarFrame.instance.getResources().getDrawable(bkImage);

        // 创建颜色Drawable，例如半透明黑色
        int translucentColor = (128 << 24) | (bkColor & 0x00FFFFFF);
        Drawable colorOverlay = new ColorDrawable(translucentColor);  // 半透明黑色

        // 创建Drawable数组以放入LayerDrawable
        Drawable[] layers = new Drawable[2];
        layers[0] = backgroundImage;
        layers[1] = colorOverlay;

        // 创建LayerDrawable
        LayerDrawable layerDrawable = new LayerDrawable(layers);

        // 将LayerDrawable设置为视图背景
        setBackground(layerDrawable);
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
