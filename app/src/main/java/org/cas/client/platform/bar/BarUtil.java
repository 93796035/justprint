package org.cas.client.platform.bar;

import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.ToggleButton;

import com.just.print.R;
import com.just.print.app.AikaPos;
import com.just.print.app.BarFrame;
import com.just.print.ui.panel.PicturePane;
import com.just.print.ui.panel.SalesPanel;
import com.just.print.util.JOptionPane;

import org.cas.client.platform.bar.i18n.BarDlgConst;
import org.cas.client.platform.bar.model.DBConsts;
import org.cas.client.platform.bar.model.DBUtil;
import org.cas.client.platform.bar.panel.BillPanel;
import org.cas.client.platform.bar.print.PrintService;
import org.cas.client.platform.casutil.L;

import org.cas.client.platform.bar.uibeans.ISButton;
import org.cas.client.platform.bar.uibeans.MoreButton;
import org.cas.client.platform.cascustomize.CustOpts;
//import org.cas.client.platform.bar.model.DBConsts;
//import org.cas.client.platform.bar.model.DBUtil;
//import org.cas.client.platform.bar.model.Dish;
//import org.cas.client.platform.bar.pannel.BarFrame;
//import org.cas.client.platform.bar.pannel.BillPanel;
//import org.cas.client.platform.bar.pannel.SalesPanel;
//import org.cas.client.platform.bar.print.PrintService;
//import org.cas.client.platform.bar.uibeans.MoreButton;
//import org.cas.client.platform.cascustomize.CustOpts;
//import org.cas.client.platform.casutil.ErrorUtil;
//import org.cas.client.platform.casutil.L;
//import org.cas.client.platform.casutil.TaoEncrypt;
//import org.cas.client.platform.pimmodel.PIMDBModel;
//import org.cas.client.platform.pimview.PicturePane;
//import org.cas.client.resource.international.DlgConst;
//
//import java.awt.Component;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

//import javax.swing.JComponent;
//import javax.swing.JOptionPane;
//
//import gnu.io.CommPortIdentifier;
//import gnu.io.ParallelPort;
//import gnu.io.PortInUseException;

public class BarUtil {

    public static final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public static final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");

    public static DecimalFormat formatter = new DecimalFormat("#0.00");
    public static String SEP_STR1 = "-";
    public static String SEP_STR2 = "=";
    private static String[] sepLines;

//    //not sure the command still available. I am currently using Command.OPEN_CASHIER
//    public static void openMoneyBox() {
//        int[] ccs = new int[5];
//        ccs[0] = 27;
//        ccs[1] = 112;
//        ccs[2] = 0;
//        ccs[3] = 80;
//        ccs[4] = 250;
//
//        CommPortIdentifier tPortIdty;
//        try {
//            Enumeration tPorts = CommPortIdentifier.getPortIdentifiers();
//            if (tPorts == null)
//                JOptionPane.showMessageDialog(BarFrame.instance, "no comm ports found!");
//            else
//                while (tPorts.hasMoreElements()) {
//                    tPortIdty = (CommPortIdentifier) tPorts.nextElement();
//                    if (tPortIdty.getName().equalsIgnoreCase("com1")) {
//                        if (!tPortIdty.isCurrentlyOwned()) {
//                            ParallelPort tParallelPort = (ParallelPort) tPortIdty.open("serialBlackBox", 2000);
//                            DataOutputStream tOutStream = new DataOutputStream(tParallelPort.getOutputStream());
//                            for (int i = 0; i < 5; i++)
//                                tOutStream.write(ccs[i]);
//                            tOutStream.flush();
//                            tOutStream.close();
//                            tParallelPort.close();
//                        }
//                    }
//                }
//        } catch (PortInUseException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    // 先检查是否存在尚未输入完整信息的产品，如果检查到存在这种产品，方法中会自动弹出对话盒要求用户填写详细信息。
//    public static void checkUnCompProdInfo() {
//        String sql = "select * from Dish where subject = '' and DELETED != true"; // 是否存在上没有名字的产品？
//        try {
//            ResultSet rs = AikaStyle.db.execSQL(sql);
//            rs.afterLast();
//            rs.relative(-1);
//            int tRowCount = rs.getRow();
//            if (tRowCount > 0) {
//                int[] tIDAry = new int[tRowCount];
//                String[] tCodeAry = new String[tRowCount];
//                int[] tPriceAry = new int[tRowCount];
//                String[] tMnemonicAry = new String[tRowCount];
//                String[] tSubjectAry = new String[tRowCount];
//                int[] tStoreAry = new int[tRowCount];
//                String[] tUnitAry = new String[tRowCount];
//                String[] tCategoryAry = new String[tRowCount];
//                int[] tCostAry = new int[tRowCount];
//                String[] tContentAry = new String[tRowCount];
//
//                rs.beforeFirst();
//                int tIdx = 0;
//                while (rs.moveToNext()) {
//                    tIDAry[tIdx] = DBUtil.getCursorInt(rs,"_ID");
//                    tCodeAry[tIdx] = DBUtil.getCursorString(rs,"Code");
//                    tPriceAry[tIdx] = DBUtil.getCursorInt(rs,"Price");
//                    tMnemonicAry[tIdx] = DBUtil.getCursorString(rs,"Mnemonic");
//                    tSubjectAry[tIdx] = DBUtil.getCursorString(rs,"Subject");
//                    tStoreAry[tIdx] = DBUtil.getCursorInt(rs,"Store");
//                    tUnitAry[tIdx] = DBUtil.getCursorString(rs,"Unit");
//                    tCategoryAry[tIdx] = DBUtil.getCursorString(rs,"Category");
//                    tCostAry[tIdx] = DBUtil.getCursorInt(rs,"Cost");
//                    tContentAry[tIdx] = DBUtil.getCursorString(rs,"Content");
//                    tIdx++;
//                }
////                for (int i = 0; i < tRowCount; i++) {
////                    new MenuItem(BarFrame.instance, tIDAry[i], tCodeAry[i], tPriceAry[i], tMnemonicAry[i],
////                            tSubjectAry[i], tStoreAry[i], tUnitAry[i], tCategoryAry[i], tCostAry[i], tContentAry[i])
////                            .setVisible(true);
////
////                }
//            }
//            rs.close();// 关闭
//        } catch (SQLException exp) {// 如果没有匹配是否会到该代码块？
//            ErrorUtil.write(exp);
//        }
//    }

    public static String formatMoney(double number) {
        return formatter.format(number);
    }

//    public static boolean isNumber(int pKeyCode) {
//        return (pKeyCode >= 48 && pKeyCode <= 58) || (pKeyCode >= 96 && pKeyCode <= 106);
//    }
//
//    // 返回Option中设置的，条码的位数。
//    public static int getProdCodeLen() {
//        String tValue = (String) CustOpts.custOps.getValue(BarFrame.consts.ProdCodeLength());
//        int tProdCodeLen = 100;
//        try {
//            tProdCodeLen = Integer.parseInt(tValue);
//        } catch (Exception exp) {
//        }
//        return tProdCodeLen;
//    }
//
    public static int getPreferedWidth() {
        int tWidth = 46;
        try {
            tWidth = Integer.valueOf((String)CustOpts.custOps.getValue( "width"));
        }catch(Exception e){
            //do nothing, if no with property set, width will keep default value.
        }
        return tWidth;
    }


    public static String generateString(int length, String character){
        StringBuilder sb = new StringBuilder("");
        for (int i = 0; i < length; i++){
            sb.append(character);
        }
        return sb.toString();
    }

    public static int getLengthOfString(String content){
        int length = content.length();
        int realWidth = length;
        for(int i = 0; i < length; i++) {
            char c = content.charAt(i);
            if(c >=19968 && c <= 171941) {
                realWidth++;
            }
        }
        return realWidth;
    }

//    public static boolean isMoreThanOneBill() {
//        try {
//            StringBuilder sql = new StringBuilder("SELECT DISTINCT contactID from output where SUBJECT = '").append(BarFrame.instance.getCmbCurTable().getSelectedItem().toString())
//                    .append("' and (deleted is null or deleted = ").append(DBConsts.ORIGINAL)
//                    .append(" and time = '").append(BarFrame.instance.valOpenTime.getText()).append("' order by contactID");
//            ResultSet rs = AikaStyle.db.execSQL(sql.toString());
//            rs.afterLast();
//            rs.relative(-1);
//            int num = rs.getRow();
//
//            if (num < 2) {	//more than 1 record, means splitted.
//                return false;
//            } else {
//                return true;
//            }
//        }
//        catch(Exception e) {
//            return false;
//        }
//    }
//
////	private void setRefInfoComment(int billId) {
////		StringBuilder sb = new StringBuilder("update bill set comment = '").append(PrintUtil.REF_TO + billId).append("' where id = ").append(billId);
////		try {
////			PIMDBModel.getStatement().executeUpdate(sb.toString());
////		}catch(Exception exp) {
////			ErrorUtil.write(exp);
////		}
////	}
//
    //remove html tags e.g.<html><center>BILL<br>FOOT INFO</center></html>
    public static String getPlainTextOut(String string) {
        int p = string.indexOf("<html>");
        if(p >= 0) {
            string = string.substring(p + 6);
            p = string.indexOf("</html>");
            if(p >= 0) {
                string = string.substring(0, p);
            }
            p = string.indexOf("<center>");
            if(p >= 0) {
                string = string.substring(p + 8);
            }
            p = string.indexOf("</center>");
            if(p >= 0) {
                string = string.substring(0, p);
            }
            p = string.indexOf("<br>");
            if(p > 0) {
                string = string.substring(0, p) + " " + string.substring(p + 4);
            }
            p = string.indexOf("<br>");
            if(p > 0) {
                string = string.substring(0, p);
            }
        }
        return string;
    }


////	private static String centRound(String priceStr) {
////		Float price = Float.parseFloat(priceStr);
////		int cent = Math.round(price * 100);
////    	//get the last bit
////    	int lastNum = cent % 10;
////    	if(lastNum == 1 || lastNum == 2)
////    		lastNum = 0;
////    	else if(lastNum == 3 || lastNum == 4 || lastNum == 6 || lastNum == 7)
////    		lastNum = 5;
////    	else if(lastNum == 8 || lastNum == 9)
////    		lastNum = 10;
////    	return BarUtil.formatMoney((cent/ 10 * 10 + lastNum) / 100.0);
////    }
//
    public static String canadianPennyRound(String substring) {
        Float price = 0.00f;
        try{
            price = Float.parseFloat(substring.trim());
        }catch(Exception exp) {
            L.e("BarUtil", "Wrong format found", exp);
        }
        int cent = Math.round(price * 100);
        boolean isNegtive = cent < 0;
        if(isNegtive) {
            cent *= -1;
        }
        int lastNum = cent % 10;
        if(lastNum < 3) {
            cent = cent - lastNum;
        }else if(lastNum > 7) {
            cent = cent - lastNum + 10;
        }else {
            cent = cent - lastNum + 5;
        }

        if(isNegtive) {
            cent *= -1;
        }
        return BarUtil.formatMoney(cent/100f);
    }

    public static String getSeperatorLine(int index) {
        if(sepLines == null) {
            sepLines = new String[2];
            int tWidth = BarUtil.getPreferedWidth();
            String sep_str1 = (String)CustOpts.custOps.getValue("sep_str1");
            if(sep_str1 == null || sep_str1.length() == 0){
                sep_str1 = SEP_STR1;
            }
            sepLines[0] = BarUtil.generateString(tWidth, sep_str1);

            String sep_str2 = (String)CustOpts.custOps.getValue("sep_str2");
            if(sep_str2 == null || sep_str2.length() == 0){
                sep_str2 = SEP_STR2;
            }

            sepLines[1] = BarUtil.generateString(tWidth, sep_str2);
        }
        if(index > sepLines.length) {
            L.e("PrintService", "Unexpect index when getting item from sepLines with Index: " + index
                    + "the width of sepLines is " + sepLines.length, null);
        }
        return sepLines[index];
    }

    public static String getSeperatorLine(int index, int tWidth) {
        if(index == 0) {
            String sep_str1 = (String)CustOpts.custOps.getValue("sep_str1");
            if(sep_str1 == null || sep_str1.length() == 0){
                sep_str1 = SEP_STR1;
            }
            return BarUtil.generateString(tWidth, sep_str1);
        } else if(index == 1) {
            String sep_str2 = (String)CustOpts.custOps.getValue("sep_str2");
            if(sep_str2 == null || sep_str2.length() == 0){
                sep_str2 = SEP_STR2;
            }

            return BarUtil.generateString(tWidth, sep_str2);
        } else {
            L.e("PrintService", "Unexpect index when getting item from sepLines with Index: " + index
                    + "the width of sepLines is " + sepLines.length, null);
            return "";
        }
    }

//    public static void cleanDB() {
//        try {
//            Statement smt = PIMDBModel.getStatement();
//            try {
//                StringBuilder sql = new StringBuilder("update bill set status = ").append(DBConsts.DELETED);
//                smt.executeUpdate(sql.toString());
//                sql = new StringBuilder("update Output set deleted = ").append(DBConsts.DELETED);
//                smt.executeUpdate(sql.toString());
//                sql = new StringBuilder("update dining_table set status = 0, openTime = ''");
//                PIMDBModel.getStatement().executeUpdate(sql.toString());
//                sql = new StringBuilder("delete from WEBSRMBATCH");
//                PIMDBModel.getStatement().executeUpdate(sql.toString());
//                CustOpts.custOps.setKeyAndValue("KitchenBillIdx", "1");
//            }catch(Exception exp) {
//                L.e("SettingPanel", "Exception when clearing demo records", exp);
//            }
//        }catch(Exception exp) {
//            L.e("Report", " exception when trying to delete records from db", exp);
//        }
//
//    }
//
//    public static String encryptIfNeeded(String key, String substring) {
//        if(key.equals("superPassword")) {
//            substring = TaoEncrypt.encryptPassword(substring);
//        }
//        return substring;
//    }
//
//    public static List<Dish> generateAnEmptyDish() {
//        List<Dish> dishes = new ArrayList<Dish>();
//        Dish dish = new Dish();
//        dishes.add(dish);
//        return dishes;
//    }

    public static void addFunctionButtons(PicturePane panel, ArrayList<ISButton> buttons) {
        int max = buttons.size();
        if(max > panel.MAX_CMDBTN_QT) {
            MoreButton btnMore = new MoreButton(BarFrame.consts.MORE());
            boolean moreBtnAdded = buttons.get(panel.MAX_CMDBTN_QT - 1) instanceof MoreButton;
            for(int i =  moreBtnAdded? panel.MAX_CMDBTN_QT : panel.MAX_CMDBTN_QT - 1; i < buttons.size(); i++) {
                btnMore.addButton(buttons.get(i));
            }
            if(!moreBtnAdded) {
                buttons.add(panel.MAX_CMDBTN_QT - 1, btnMore);
            }
            max = panel.MAX_CMDBTN_QT;
        }

//        for(int i = 0; i < max ; i++) {
//            panel.add((ISButton)buttons.get(i));
//        }
    }
//    public static int hideCommandButtons(PicturePane panel, ArrayList<JComponent> buttons) {
//        if(buttons == null)
//            return -1;
//
//        // command buttons--------------
//        int max = buttons.size() > panel.MAX_CMDBTN_QT ? panel.MAX_CMDBTN_QT : buttons.size();
//        for(int i = 0; i < max; i++) {
//            buttons.get(i).setBounds(-100,0,0,0);
//        }
//        return buttons != null && buttons.size() > 0 ? buttons.get(0).getY() : BarFrame.instance.getHeight() - 83;
//    }
//
    public static int layoutCommandButtons(View view, ArrayList<ISButton> buttons, int maxCombineQT) {
        if (buttons == null || buttons.isEmpty()) {
            return -1;
        }

        ViewGroup buttonBar = (ViewGroup) view.findViewById(R.id.buttonBar);
        buttonBar.removeAllViews();

        int panelWidth = BarFrame.instance.getResources().getDisplayMetrics().widthPixels;
        int panelHeight = BarFrame.instance.getResources().getDisplayMetrics().heightPixels;

        int buttonQT = buttons.size();
        int col = buttonQT <= 10 ? buttonQT : 10;
        int row = (int) Math.ceil((double) buttonQT / col);

        int gap = CustOpts.HOR_GAP / 2;
        int v_gap = CustOpts.VER_GAP / 2;
        int tBtnWidth = col == 0 ? 100 : (panelWidth - gap * 2 * col) / col;
        int tBtnHeight = (panelHeight - 80) / 10;

        if (buttonBar instanceof GridLayout) {
            ((GridLayout) buttonBar).setRowCount(row);
            ((GridLayout) buttonBar).setColumnCount(col);
        }

        int max = buttonQT > maxCombineQT ? maxCombineQT : buttonQT;
        for (int i = 0; i < max; i++) {
            ISButton button = buttons.get(i);
            if (button instanceof View) {
                ViewGroup oldParent = (ViewGroup) ((View)button).getParent();
                if (oldParent != null) {
                    oldParent.removeView((View)button); // 从旧的父布局中移除视图
                }
                ViewGroup.LayoutParams params;
                if (buttonBar instanceof GridLayout) {
                    params = new GridLayout.LayoutParams(
                            GridLayout.spec(GridLayout.UNDEFINED, 1),
                            GridLayout.spec(GridLayout.UNDEFINED, 1)
                    );
                    ((GridLayout.LayoutParams) params).setMargins(gap, v_gap, gap, v_gap);
                    params.width = tBtnWidth;
                    params.height = tBtnHeight;
                } else { // Assume LinearLayout
                    params = new LinearLayout.LayoutParams(tBtnWidth, tBtnHeight);
                    ((LinearLayout.LayoutParams) params).setMargins(gap, v_gap, gap, v_gap);
                }
                buttonBar.addView((View) button, params);
            }
        }
        return row;
    }


    public static int layoutCommandButtons(PicturePane panel, ArrayList<ISButton> buttons) {
        if (buttons == null || buttons.size() == 0)
            return -1;

        int panelWidth = BarFrame.instance.getResources().getDisplayMetrics().widthPixels;
        int panelHeight = BarFrame.instance.getResources().getDisplayMetrics().heightPixels;
        int buttonQT = buttons.size();


        // Determine rows and columns
        int buttonCount = buttons.size();
        int rows, cols;
        if(buttonCount <= 0) {
            rows = 0;
            cols = 0;
        }if (buttonCount <= 10) {
            rows = 1;
            cols = buttonCount;
        } else {
            rows = 2;
            cols = Math.min(10, buttonCount);
            rows = panel.MAX_CMDBTN_QT > 10 ? 2 : 1;
            if(buttonCount >= panel.MAX_CMDBTN_QT){
                cols = 10;
            }else{
                cols = buttonQT%2 + buttonQT/2;
            }
        }
        int gap = CustOpts.HOR_GAP / 2;
        int v_gap = CustOpts.VER_GAP / 2;
        int buttonWidth = cols == 0 ? 100 : (panelWidth - gap * (cols + 1)) / cols;
        int buttonHeight = (panelHeight - 80) / 10;

//        panel.getRootView().removeAllViews();
//        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(buttonWidth, buttonHeight);
//        params.setMargins(5, 5, 5, 5); // margins in pixels
//
//        for (int i = 0; i < buttonCount; i++) {
//            Button button = buttons.get(i);
//            button.setLayoutParams(params);
//            panel.addView(button);
//        }
        return -1;
    }

    public static boolean empty(String content) {
        return content == null || content.trim().length() == 0;
    }

    public static String getMoneyStrOut(String strDisCount) {
        int idx = strDisCount.indexOf("-" + BarOption.getMoneySign());
        if(idx < 0) {
            return "0";
        }

        strDisCount = strDisCount.substring(idx + 2);

        idx = strDisCount.indexOf(BarDlgConst.delimiter);
        if(idx > 0) {
            strDisCount = strDisCount.substring(0, idx);
        }

        return strDisCount;
    }

//    //TODO:I'll make the price on interface, so no need to calculate every time, and more friendly for user.
//    public static float calculateLabelsPrices(String[] labels) {
//        Float pirce = 0.0f;
//        for(int i = 0; i < labels.length; i++) {
//            String[] langs = labels[i].split(BarDlgConst.semicolon);
//            String modifyName = addHTMLTAG(langs[0].trim());
//            int qty = 1;
//            int p = modifyName.lastIndexOf(" x");	//must contains a " ", to avoid the 'x' in name.
//            if(p > 0) {
//                try {
//                    qty = Integer.parseInt(modifyName.substring(p+2).trim());
//                    modifyName = modifyName.substring(0, p).trim();
//                }catch(Exception e) {
//                    //means after " x", it's not number. so the " x" might be "tang cu xia".
//                }
//            }
//            StringBuilder sql = new StringBuilder("select distinct lang6 from modification where type >= 0 and (lang1 = '")
//                    .append(modifyName).append("' or lang2 = '")
//                    .append(modifyName).append("' or lang3 = '")
//                    .append(modifyName).append("')");
//            try {
//                ResultSet rs = AikaStyle.db.execSQL(sql.toString());
//                rs.afterLast();
//                rs.relative(-1);
//                if(rs.getRow() > 1) {
//                    L.e("BarUtil", " Found two labels with same name but have different price!" + langs[0], null);
//                }else if(rs.getRow() == 0) {
//                    L.e("BarUtil", " Found no labels matching lang1 = " + langs[0] + ". if this label has price property, will affect the total price of this dish!", null);
//                }
//                rs.beforeFirst();
//                rs.moveToNext();
//                String lang6 = DBUtil.getCursorString(rs,"lang6");
//                if(lang6 != null && lang6.length() > 0) {
//                    pirce += Float.parseFloat(DBUtil.getCursorString(rs,"lang6"));
//                }
//            }catch(Exception e) {
//                L.e("AddModificationDlg", " Exception when searching price of modificaiton " + sql, null);
//            }
//        }
//        return pirce;
//    }
//
//    //shouldn't use this, should user PIMDBModel.getConnection().prepareStatement(sql.toString()) to replace the "?" in sql to achieve the safety
//    @Deprecated
//    private static String sqlSafeCheck(String value) {
//        StringBuilder sb = new StringBuilder(value);
//        int p = sb.indexOf("'");
//        while (p >= 0){
//
//            sb = new StringBuilder(sb.substring(0, p)).append("'").append(sb.substring(p));
//            p = p + 2;
//
//            if(p >= sb.length()) {
//                break;
//            }else {
//                p = sb.indexOf("'", p);
//            }
//        }
//        return sb.toString();
//    }
    public static String addHTMLTAG(String text){
//        if(text != null && !text.toLowerCase().trim().startsWith("<html>")) { //not null, and not start with
//            int p = text.indexOf("_");
//            if(p > 0) {
//                StringBuilder text2 = new StringBuilder("<html><center>").append("");
//                text2.append(text.substring(0, p));
//                text2.append("<br>");
//                text2.append(text.substring(p + 1));
//                text2.append("</center></html>");
//                return text2.toString();
//            }
//        }
        return text;
    }

    public static String removeHTMLTAG(String text) {
        int p = text.indexOf("<html>");
        if(p >= 0) {
            text = text.substring(0,p) + text.substring(p + 6);
            p = text.indexOf("</html>");
            if(p >= 0) {
                text = text.substring(0, p);
            }
        }

        p = text.indexOf("<center>");
        if(p >= 0) {
            text = text.substring(0,p) + text.substring(p + 8);
            p = text.indexOf("</center>");
            if(p >= 0) {
                text = text.substring(0, p);
            }
        }

        p = text.indexOf("<br>");
        if(p >= 0) {
            text = text.substring(0,p) + "_" + text.substring(p + 4);
        }
        return text;
    }

//    public static Date getNextDate(Date date){
//        long addTime = 1;		//以1为乘以的基数
//        addTime *= 1;			//1天以后，如果是30天以后则这里是30
//        addTime *= 24;			//1天24小时
//        addTime *= 60;			//1天60分钟
//        addTime *= 60;			//1天60秒钟
//        addTime *= 1000;		//1秒=1000毫秒
//        //用毫秒数构造新的日期
//        Date nextDate = new Date(date.getTime() + addTime);
//        return nextDate;
//    }
//
//    public static String changeLangForMenu(String aValue, int customerFrameLang) {
//        StringBuilder sql = new StringBuilder("select * from Dish where code = ? or mnemonic = ? or subject = ?");
//
//        try {
//            PreparedStatement pstmt = PIMDBModel.getConnection().prepareStatement(sql.toString());
//            pstmt.setString(1, aValue);
//            pstmt.setString(2, aValue);
//            pstmt.setString(3, aValue);
//            ResultSet rs = pstmt.executeQuery();
//            if(rs.moveToNext()) {//check if the properties are all same, if not, update local to make sure local are same with server.
//                //TODO: consider later  String localPrinterIdx = synchronizeLocalProduct(location, price, seletedPrinterIdStr, rs);
//                return DBUtil.getCursorString(rs,customerFrameLang == 0 ? "code" : (customerFrameLang == 1 ? "mnemonic" : "subject"));
//            }else {
//                L.e("BarUtil ", "WTH, Can not find such a category!" + sql, null);
//                return aValue;
//            }
//        } catch (SQLException e) {
//            ErrorUtil.write(e);
//        }
//        return aValue;
//    }

    public static String changeToLangForModify(String aValue, int customerFrameLang) {
        if(aValue == null || aValue.trim().length() < 1) {
            return "";
        }
        //remove the discount----------------------
        String discount = "";
        int p = aValue.indexOf("-" + BarOption.getMoneySign());
        if(p > -1) {
            discount += aValue.substring(p);
            aValue = aValue.substring(0,p).trim();
        }
        StringBuilder finalLabel = new StringBuilder();
        String[] labels = aValue.trim().split("\n");
        for(int i = 0; i < labels.length; i++) {
            int pSpace = labels[i].trim().lastIndexOf(" x");
            String num = "";
            if(pSpace > 0) {
                num = labels[i].substring(pSpace).trim();
                labels[i] = labels[i].trim().substring(0, pSpace).trim();
            }
            String name = BarUtil.addHTMLTAG(labels[i]);
            StringBuilder sql = new StringBuilder("select * from modification where name1 = ")
                    .append("'").append(name).append("' or name2 = ")
                    .append("'").append(name).append("' or name3 = ")
                    .append("'").append(name).append("'");
            try {
                Cursor rs = AikaPos.db.rawQuery(sql.toString(), null);
                if(rs.moveToNext()) {//check if the properties are all same, if not, update local to make sure local are same with server.
                    //TODO: consider later  String localPrinterIdx = synchronizeLocalProduct(location, price, seletedPrinterIdStr, rs);
                    labels[i] =  DBUtil.getCursorString(rs, "lang" + (customerFrameLang + 1));
                }else {
                    L.e("BarUtil ", "WTH, Can not find such a category!" + sql, null);
                    labels[i] = aValue;
                }
            } catch (Exception e) {
                labels[i] = aValue;
            }
            finalLabel.append(labels[i]).append(" ").append(num).append("\n");
        }
        finalLabel.append(discount);
        return finalLabel.toString();
    }

    //currently called from cmd_pay and cmd_paymethod
    public static boolean routineCheckBeforePrintingReceipt() {
        SalesPanel salesPanel = (SalesPanel)BarFrame.instance.panels[2];
        BillPanel billPanel = salesPanel.billPanel;
        if(billPanel.getOrderedDishAry().size() == 0) {
            JOptionPane.showMessageDialog(BarFrame.instance, BarFrame.consts.AtLeastOneShouldBeSelected());
            return false;
        }

        billPanel.unsavedItemsCheck();	//process the new added items (send output records to printer and db).

        //if it's already paid, show comfirmDialog.
        if(billPanel.getStatus() >= DBConsts.COMPLETED || billPanel.getStatus() < DBConsts.ORIGINAL) {
            if(JOptionPane.showConfirmDialog(BarFrame.consts.ConfirmPayAgain(), org.cas.client.resources.international.DlgConst.DlgTitle)) {
                return false;
            }
        }

        if(!BarOption.isCounterMode()) {
            //has price or item change, means should regenerate a temporal bill of billPrinted status before generating the closed bill,
            //so the closed bill will always be same with the last temporal bill. while be noticed that here is before printing receipt, while means not like in
            //Cmd_printBill, here, no expireAdnReGenerate() has been called, so the bill id is still pointing to the old bill, and, the old bill might be the one
            //which was generated when opening table (the creatTime is same as open time)
            String comment = billPanel.getComment();
            boolean hasModificationInPrice = comment.contains(BillPanel.SERVICEFEE) || comment.contains(BillPanel.DISCBILL);
            boolean hasItemAddedOrRemoved = DBUtil.isThereItemAddOrRemoved(billPanel.getBillID(), true);
            if(hasItemAddedOrRemoved || hasModificationInPrice) {
                if(billPanel.getStatus() >= DBConsts.BILL_PRINTED) { // if already printed, means there's record in web-mev alredy, make sure refs are right.
                    billPanel.expireAndReGenerateBill(null);
                }
                // if the bill was the one that was generated when table opened, it has save createTime as openTime. now we take
                //the chance to update it to the real printed time. then, if there's an other revisee bill printed, system will
                //not confuse about the outputs（ which was actually generated before first time print) should belongs to which bill.
                DBUtil.updatePriceAndCreateTimeInBillRecord(billPanel, DBConsts.BILL_PRINTED, BarUtil.df.format(new Date())); //already saved billRecord need to update price to reflect the new ordered dishes.
                //have to call this, because need to report mev there's a change. but inside, may not print bill out on printer.
                if(billPanel.getStatus() >= DBConsts.BILL_PRINTED) {
                    billPanel.setComment(billPanel.getComment() + PrintService.TRANSACTION_ONLY);
                    PrintService.printTemporaryBill(billPanel, billPanel.getOrderedDishAry(), true);
                    DBUtil.cleanTemporalInfoInComment(billPanel, comment);
                }
            }
        }
        return true;
    }

    public static String splitStringToThreshold(String text, int targetLength) {
        StringBuilder strings = new StringBuilder();
        int length = 0;
        List<Character> chars = new ArrayList<Character>();
        for (char c : text.toCharArray()) {
            if(c == '\n') {
                strings.append(chars.stream().map(String::valueOf).collect(Collectors.joining()));
                strings.append("\n");
                chars = new ArrayList<Character>();
                length = 0;
                continue;
            } else if (Character.UnicodeBlock.of(c) == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
                    || Character.UnicodeBlock.of(c) == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
                    || Character.UnicodeBlock.of(c) == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B
                    || Character.UnicodeBlock.of(c) == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
                    || Character.UnicodeBlock.of(c) == Character.UnicodeBlock.CJK_COMPATIBILITY_FORMS) {
                length += 2;
            } else {
                length += 1;
            }

            if(length > targetLength) {
                strings.append(chars.stream().map(String::valueOf).collect(Collectors.joining()));
                strings.append("\n");
                chars = new ArrayList<Character>();
                chars.add(c);
                length = 2;
            }else {
                chars.add(c);
                if(length == targetLength) {
                    strings.append(chars.stream().map(String::valueOf).collect(Collectors.joining()));
                    strings.append("\n");
                    chars = new ArrayList<Character>();
                    length = 0;
                }
            }
        }
        strings.append(chars.stream().map(String::valueOf).collect(Collectors.joining()));
        strings.append("\n");
        return strings.toString();
    }

    public static boolean isBetweenSummerTime() {
        Calendar calendar = Calendar.getInstance();
        int month = calendar.get(Calendar.MONTH); // January is 0, December is 11
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);

        // Check for March's second Sunday
        if (month == Calendar.MARCH) {
            if (dayOfWeek == Calendar.SUNDAY && dayOfMonth > 7 && dayOfMonth <= 14) {
                return true;
            }
            if (dayOfMonth > 14) {
                return true;
            }
        }

        // Check for months between March and November
        if (month > Calendar.MARCH && month < Calendar.NOVEMBER) {
            return true;
        }

        // Check for November's first Sunday
        if (month == Calendar.NOVEMBER) {
            if (dayOfWeek == Calendar.SUNDAY && dayOfMonth <= 7) {
                return false;
            }
            if (dayOfMonth <= 7) {
                return true;
            }
        }

        return false;
    }
}
