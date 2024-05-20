package com.just.print.ui.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.fragment.app.DialogFragment;

import com.just.print.R;
import com.just.print.app.AikaPos;
import com.just.print.app.BarFrame;
import com.just.print.db.bean.Bill;
import com.just.print.db.bean.User;
import com.just.print.db.expand.DaoExpand;
import com.just.print.ui.panel.TablesPanel;
import com.just.print.util.JOptionPane;

import org.cas.client.platform.bar.BarOption;
import org.cas.client.platform.bar.BarUtil;
import org.cas.client.platform.bar.dialog.CmdBtnsManagerDlg;
import org.cas.client.platform.bar.dialog.statistics.ReportDlg;
import org.cas.client.platform.bar.i18n.BarDlgConst0;
import org.cas.client.platform.bar.i18n.BarDlgConst1;
import org.cas.client.platform.bar.i18n.BarDlgConst2;
import org.cas.client.platform.bar.model.DBUtil;
import org.cas.client.platform.bar.print.PrintService;
import org.cas.client.platform.cascustomize.CustOpts;
import org.cas.client.platform.casutil.L;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class LoginDlg extends DialogFragment {

    private static List<User> users = new ArrayList<User>();
    public static int failedTimes = 0;
    public static boolean PASSED;
    public static int USERID = -1;
    public static String USERNAME = "";
    private static int USERTYPE = -1;
    public static String MODETRANS = "O";
    public static int USERLANG;
    public static final int USER_STATUS = 1;
    public static  final int ADMIN_STATUS = 2;
    public static  final int SUPER_ADMIN = 3;

    public boolean noRefresh;
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_login, null);
        // 数字按钮事件
        for (int id : new int[]{
                R.id.button1, R.id.button2, R.id.button3, R.id.button4, R.id.button5,
                R.id.button6, R.id.button7, R.id.button8, R.id.button9, R.id.button0}) {
            view.findViewById(id).setOnClickListener(this::onNumberClick);
        }

        // 设置后退按钮事件
        Button backButton = view.findViewById(R.id.button_left_arrow);
        backButton.setOnClickListener(v -> {
            EditText passwordField = view.findViewById(R.id.password_field);
            int length = passwordField.getText().length();
            if (length > 0) {
                passwordField.setText(passwordField.getText().delete(length - 1, length));
            }
        });

        view.findViewById(R.id.button_ok).setOnClickListener(v -> {
            EditText passwordField = view.findViewById(R.id.password_field);
            String inputPassword = passwordField.getText().toString();
            attemptLogin(inputPassword);
        });
        initContent();
        builder.setView(view);
        return builder.create();
    }

    private void attemptLogin(String inputPassword) {
        if(inputPassword.length() > 1 && inputPassword.equals(BarOption.getSuperPassword())){
            PASSED = true;
            LoginDlg.failedTimes = 0;
            setUSERTYPE(LoginDlg.SUPER_ADMIN);
            USERNAME = "superAdmin";
            ((BarFrame)getActivity()).switchFragment(TablesPanel.getInstance());
            BarFrame.instance.resetInactivityTimer();
            return;
        }
        // Object tUserName = general.cmbUserName.getSelectedItem();
//        String sql = "select type from useridentity where ID < 6 order by id";
//        try {
//            Cursor rs = AikaPos.db.rawQuery(sql, null);
//            int tmpPos = rs.getCount();
//            if (tmpPos < 6)
//                L.e("LoginDlg", "No userIdentity found, data base was destroyed.", null);
//            String[] tSuperKey = new String[tmpPos];
//            tmpPos = 0;
//            while (rs.moveToNext()) {
//                tSuperKey[tmpPos] = DBUtil.getCursorString(rs,"type");
//                tmpPos++;
//            }
//            rs.close();// 关闭
//
//            if (new StringBuilder(tSuperKey[2]).append(tSuperKey[4]).append(tSuperKey[0]).append(tSuperKey[1])
//                    .append(tSuperKey[5]).append(tSuperKey[3]).toString().equals(tPassword)) {
//                PASSED = true;
//                LoginDlg.failedTimes = 0;
//                setUSERTYPE(ADMIN_STATUS);// 无论姓名选的是什么，只要密码和超级密码相符，则级别就是经理人（或者更高）。
//                USERNAME = "System";
//                MODETRANS = "F";	//if logged in with internal user account, the mev will be set into training mode.
//                setVisible(false);
//
//                BarFrame.resetInactivityTimer();
//                return;
//            }
//        } catch (SQLException exp) {
//            ErrorUtil.write(exp);
//        }

        // 以下为密码不是超级密码的情况：
        User user = matchingWithExistingPassword(inputPassword);
        if (user == null) {
            if ("9".equals(inputPassword)) {//for internal testing time saving.
                matchingWithExistingPassword("2222");
                ((BarFrame)getActivity()).switchFragment(TablesPanel.getInstance());
            } else {
                Toast.makeText(getActivity(), "Incorrect password", Toast.LENGTH_SHORT).show();
            }
        } else {
            BarFrame.instance.setCurrentUser(user);
            PASSED = true; // 吻合就标记通过。
            LoginDlg.failedTimes = 0;
            USERID = user.getId().intValue();
            setUSERTYPE(user.getRole()); // 并标记下用户的级别
            USERNAME = user.getNickName();
//            MODETRANS = general.modeTrans[i];
            USERLANG = user.getLang();
            CustOpts.custOps.setUserLang(USERLANG);//next start up will use it as default interface language.

            ((BarFrame)getActivity()).switchFragment(TablesPanel.getInstance());

            //if it's not for temparal manager login(like discount bill), then need to re-init the interfaces
            if(!noRefresh) {
                noRefresh = true;
                switch (USERLANG) {
                    case 0:
                        BarFrame.consts = new BarDlgConst0();
                        break;
                    case 1:
                        BarFrame.consts = new BarDlgConst1();
                        break;
                    case 2:
                        BarFrame.consts = new BarDlgConst2();
                        break;
                    case 3:
                        BarFrame.consts = new BarDlgConst2();
                        break;
                    default:
                        break;
                }
                CmdBtnsManagerDlg.initButtons();
//                    BarFrame.instance.initComponent();
                ((TablesPanel)BarFrame.instance.panels[0]).initContent();
            }
            if(BarOption.isTrainingMode()) {
                MODETRANS = "F";
            }

            //check if there's bills forgot to close batch yesterday. temporally use getParent().isVisible() to filter out the case not for printing report.
            if(BarOption.getAutomaticCloseBatch() && getUSERTYPE() == ADMIN_STATUS && BarFrame.instance.isActivityVisible()) {
                //check if there's unclosed bill
                String endTime = BarUtil.df.format(new Date()).substring(0, 11) + BarOption.getStartTime();
                String lastPrintTime = "";
                StringBuilder sqlBuilder = new StringBuilder("select ANNIVERSARY from employee where id = ").append(LoginDlg.USERID);
                try {
                    Cursor rs = AikaPos.db.rawQuery(sqlBuilder.toString(), null);
                    if(rs.moveToNext()) {
                        lastPrintTime = DBUtil.getCursorString(rs, "ANNIVERSARY");
                        if(lastPrintTime != null && lastPrintTime.length() > 10) {
                            if(lastPrintTime.indexOf(".") > 0) {
                                lastPrintTime = lastPrintTime.substring(0, lastPrintTime.indexOf(".") );
                            }
                        }
                    }
                }catch(Exception exp) {
                    L.e("LoginDlg ", "Exception when ertiving ANNIVERSARY." + sqlBuilder, exp);
                }//so, by default, we set time to currentday(customized start time + current time), while if this user has printed before, then we set start time to last print time.

                List<Bill> salesAndRefunds = ReportDlg.queryRelevantCompleteAndRefundBillList(lastPrintTime, endTime, false);
                if (salesAndRefunds.size() > 0) {
                    String curBatchIdx = "1";
                    //get current batchIdx
                    String sql = new StringBuilder("select * from systeminfo where version >= '").append(endTime).append("' order by id desc").toString();
                    try {
                        Cursor rs = AikaPos.db.rawQuery(sql, null);
                        //if didn't found any batch, means never print yet, set current batchIdx to 1
                        while(rs.moveToNext()) {
                            if(LoginDlg.getUSERTYPE() == LoginDlg.ADMIN_STATUS) {
                                curBatchIdx = String.valueOf(DBUtil.getCursorInt(rs, "id") + 1);
                                break;
                            }else {	// if it's super admin, then no need to show "next" batch number.
                                curBatchIdx = curBatchIdx.concat(String.valueOf(DBUtil.getCursorInt(rs,"id"))).concat(",");
                            }
                        }
                    }catch(Exception exp) {
                        L.e("ReportDlg ", "Exception when ertiving last record." + sql, exp);
                    }
                    if(curBatchIdx.endsWith(",")) {
                        curBatchIdx = curBatchIdx.substring(0, curBatchIdx.length() - 1);
                    }

                    // if there is, then automatically generate a report.
                    // if it's manager(admin) update db batch report time and in memory curBatchIdx
                    // value.
                    // while every one will update the last print time except the manager(admin) has
                    // chance to select not to.(when manager select not to close, then will not
                    // update the last print time).
                    // notice user to confirm that batch will be closed.
                    // if user selected to close batch, then update the sytemInfo_version with the
                    // endtime of current report (the row id will the be batch #.
                    // then reset the NextIdx system config file
                    String printerIP = BarFrame.menuPanel.getPrinters()[0].getIp();
                    PrintService.printReport(printerIP, PrintService.formatContentForReport(salesAndRefunds,
                            printerIP, lastPrintTime, endTime, curBatchIdx));

                    String sqlStr = "insert into systeminfo (version) values ('" + endTime + "')";
                    try {
                        AikaPos.db.execSQL(sqlStr);
                    } catch (Exception exp) {
                        L.e("Report", " exception when trying to add batch report time" + sqlStr, exp);
                    }

                    ReportDlg.updateLastPrintTime(endTime);
                    JOptionPane.showMessageDialog(BarFrame.instance, BarFrame.consts.BatchClosedSuccefully());
                    CustOpts.custOps.setKeyAndValue("NextIdx", 0);//reset invoice number to 0.
                    CustOpts.custOps.saveData();
                }
            }

            BarFrame.instance.resetInactivityTimer();
            return;
        }

        if(LoginDlg.failedTimes >= 3) {
            L.e("LoginSecurity", "suspecious try of password more than 3 times", null);
            LoginDlg.failedTimes = 0;
        }else {
            JOptionPane.showMessageDialog(BarFrame.instance, "Invalid Password");
        }
        LoginDlg.failedTimes++;
        ((EditText)getActivity().findViewById(R.id.password_field)).selectAll();
    }

    private void initContent() {
        users = DaoExpand.queryAllTables(AikaPos.daoSession.getUserDao());
        String sql = "select ID, Type, UserName, PASSWORD, LANG from useridentity where ID > 5";
    }
    public static int getUSERTYPE() {
        return USERTYPE;
    }

    public static void setUSERTYPE(int uSERTYPE) {
        USERTYPE = uSERTYPE;
        if(USERTYPE >= ADMIN_STATUS) {
            BarFrame.instance.findViewById(R.id.BarFrame).setBackgroundColor(Color.parseColor(BarOption.getAdminMainFramelBK()));//"AdminMainFramelBK"));
        }else {
            BarFrame.instance.findViewById(R.id.BarFrame).setBackgroundColor(Color.parseColor(BarOption.getMainFrameBK()));//"AdminMainFrameBK"));
        }
    }

    public static User matchingWithExistingPassword(String input) {
        for (User user : users) {
            if(input.equals(user.getPwd())) {
                return user;
            }
        }
        return null;
    }

    private void onNumberClick(View v) {
        Button button = (Button) v;
        EditText passwordField = getDialog().findViewById(R.id.password_field);
        passwordField.append(button.getText());
    }

    private static LoginDlg instance = null;
    public static LoginDlg getInstance(){
        if(instance == null){
            instance = new LoginDlg();
        }
        return instance;
    }
}
