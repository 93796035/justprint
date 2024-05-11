package org.cas.client.platform.cascustomize;

import com.just.print.util.L;

import org.cas.client.platform.casutil.CASUtility;
import org.cas.client.platform.casutil.PIMPool;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.Vector;

class FileCustomOptions extends CustOpts {
    public static final String FIRST_YEAR_WEEK = "First Week of Year";// first week in a year
    final String FIRST_WEEK_DAY = "First Day of Week"; // first day in a week
    final String WORK_WEEK_DAY = "Work Days of Week"; // work week days
    final String WEEK_NUMBER_VISIBLE = "Show Week Number";// show weeknumber in calendar
    final String NAVIGATION_PANE_SHOW = "Navigation_Pane_Show"; // 是否显示导航面板，用于读取用户自定义设置时的key值
    final String BOOK_EDGE_HIDE = "Book_Edge_Hide"; // 是否显示书本边界，用于读取用户自定义设置时的key值
    final String VIEW_TOP_DOWN = "View_Top_Down"; // 是否显示书本边界，用于读取用户自定义设置时的key值
    final String VIEW_PREVIEW = "View_Preview"; // 是否显示预览，用于读取用户自定义设置时的key值
    final String APP_CAPTIONS = "APP_Captions"; // hash2中标识已经安装的应用的key值
    final String APP_NAMES = "APP_Names";

    public Vector<String> APPCapsVecInSystem;
    public Vector<String> APPNameVecInSystem;
    /**
     * Constructor--------------------------------------------------------
     */
    FileCustomOptions() {
        DefaultConfigInfoLoader tmpConfigInfoLoader = new DefaultConfigInfoLoader();
        tmpConfigInfoLoader.loadCustmizedData(context, userConfigs);
        tmpConfigInfoLoader.loadSystemData(systemConfigs);

        String capsInConfigFile = (String) userConfigs.get(APP_CAPTIONS); // 取出可能经用户改动的Caps。Caption是统一用Path作key的。
        String namesInConfigFile = (String) userConfigs.get(APP_NAMES);
        String capsInSystem = getInstalledFunctions(); // 取出原始的Caps。
        String NamesInSystem = getInstalledAppNames();
        APPNameVecInSystem = CASUtility.parserStrToVec(NamesInSystem, CustOpts.BIAS, NamesInSystem.length());
        APPCapsVecInSystem = CASUtility.parserStrToVec(capsInSystem, CustOpts.BIAS, capsInSystem.length());
        // 如果Config中没有CaptionStr信息，则说明被破坏，或第一次使用（Config文件刚刚才被创建）。
        // 有应用被删除的情况在这里判断，而且s是在导航树上还要再判断。应为导航树上能照顾到Config中信息被手动改乱掉的可能。稳定性会再提高一点。
        // 有应用被增加的情况,则需要额外的工作－－把相应的代码包下的Config信心读到hash中来。
        if (!NamesInSystem.equals(namesInConfigFile)) {
            Vector<String> tmpOldNameVec = null;
            Vector<String> tmpOldCapsVec = null;
            if (namesInConfigFile == null) { // 处理应用名:新建的话赋和原始应用名向量等长的空,---------------
                tmpOldNameVec = new Vector<String>();
                for (int i = 0, tmpLen = APPNameVecInSystem.size(); i < tmpLen; i++)
                    tmpOldNameVec.add(CASUtility.EMPTYSTR);
            } else
                // 改动的话,则解析其中的内容进入向量.
                tmpOldNameVec = CASUtility.parserStrToVec(namesInConfigFile, CustOpts.BIAS, namesInConfigFile.length());

            if (capsInConfigFile == null) { // 处理Caption:新建的话赋和原始Caption向量等长的空,------------
                tmpOldCapsVec = new Vector<String>();
                for (int i = 0, tmpLen = APPCapsVecInSystem.size(); i < tmpLen; i++)
                    tmpOldCapsVec.add(CASUtility.EMPTYSTR);
            } else
                // 改动的话,则解析其中的内容进入向量.
                tmpOldCapsVec = CASUtility.parserStrToVec(capsInConfigFile, CustOpts.BIAS, capsInConfigFile.length());

            // 遍历原始应用名,逐个与config中的进行比较,多删少补,使和原始的一致.@NOTE:少补的时候要注意从对应应用中载入相关数据.
            for (int i = 0, tmpLen = APPNameVecInSystem.size(); i < tmpLen; i++) {
                final String tmpNameNew = APPNameVecInSystem.get(i); // Config2.ini中的应用名
                String tmpNameOld = i < tmpOldNameVec.size() ? tmpOldNameVec.get(i) : null;// Config.ini中的应用名
                if (!tmpNameNew.equals(tmpNameOld)) { // 如果某个元素相同的话，跳过．不同则进入．
                    if (tmpOldNameVec.indexOf(tmpNameNew) < 0) { // 有新应用增加。增加，int值不需要专门调整。
                        tmpOldNameVec.insertElementAt(tmpNameNew, i);
                        tmpOldCapsVec.insertElementAt(APPCapsVecInSystem.get(i), i);
                        tmpConfigInfoLoader.loadAppData(tmpNameNew, userConfigs); // 吸收其对应的Config中的内容。
                        //following part need to be done later, because it need to wait for DBconnection to be
                        //initialized first, otherwise db access will cause null pointer.
//                        SwingUtilities.invokeLater(new Runnable(){
//                            public void run(){
//                                IApplication app = MainPane.getApp(tmpNameNew);
//                                if (app != null) {
//                                    app.initInfoInDB(); // 初始化应用的ViewInfo, Table等
//                                }
//                            }
//                        });
                    } else if (APPNameVecInSystem.indexOf(tmpNameOld) < 0) { // 如果发现有应用被删除了，就移除，影响到int值的遍历,所以调整。
                        tmpOldNameVec.remove(i);
                        tmpOldCapsVec.remove(i);
                        i--;
                    }
                }
            }
            APPCapsVecInSystem = tmpOldCapsVec; // APPCapsVec吸收了新增加的Caption，并去掉了已删除的应用。

            capsInConfigFile = CASUtility.EMPTYSTR; // 得到调整之后的CapString。
            for (int i = 0, tmpLen = APPCapsVecInSystem.size(); i < tmpLen; i++)
                capsInConfigFile = capsInConfigFile.concat(APPCapsVecInSystem.get(i)) + CustOpts.BIAS;
            int tmpPos = capsInConfigFile.indexOf("//");
            if (tmpPos > 0)
                capsInConfigFile = capsInConfigFile.substring(0, tmpPos);
            else
                capsInConfigFile = capsInConfigFile.substring(0, capsInConfigFile.length() - 1);
            namesInConfigFile = NamesInSystem; // 因为有区别，所以调整了（指吸收了新的Config）之后以Config2为主。

            userConfigs.put("DefaultFont", systemConfigs.get("DefaultFont"));// 另外在第一次用和Config被损坏的情况下,还需要将默认字体信息重新载入.
        } // 至此，appName调整结束。AppNameVec和tmpOldNameVec中内容完全一致。并完成了Config的吸收工作。－－－－－－－－－－－－－－

        userConfigs.put(APP_CAPTIONS, capsInConfigFile); // 存入hash中不是因为还有人使用，而是为了存盘时候用。
        userConfigs.put(APP_NAMES, namesInConfigFile);

        APPCapsVecInSystem = CASUtility.parserStrToVec(capsInConfigFile, CustOpts.BIAS, capsInConfigFile.length());// 因为常用。
        APPNameVecInSystem = CASUtility.parserStrToVec(namesInConfigFile, CustOpts.BIAS, namesInConfigFile.length());

        String osString = System.getProperty("os.name").toLowerCase(); // 操作系统的文本
        if (osString.startsWith("win"))
            OS = WINDOWS;
        else if (osString.startsWith("linux")) // 判断Linux操作系统
            OS = LINUX;
        else
            OS = -1; // 未知操作系统
    }

    @Override
    public void setInstallPath(
            String path) {
        userConfigs.put(CustOptsConsts.INSTALLPATH, path);
    }

    @Override
    public String getInstallPath() {
        String installedPath = (String) userConfigs.get(CustOptsConsts.INSTALLPATH);
        return installedPath == null || installedPath.length() < 1 ? "c:\\AikaPos" : installedPath;
    }
    @Override
    public String getMainPaneBGImg() {
        String tStr = (String)userConfigs.get(CustOptsConsts.MainBGImgPath);
        if (tStr != null) {
            return tStr.toString().startsWith("#") ? tStr : "#" + tStr;
        } else
            return super.getMainPaneBGImg();
    }

    public void setMainPaneBGImg(
            String pPath) {
        if (pPath != null)
            userConfigs.put(CustOptsConsts.MainBGImgPath, pPath);
    }

    @Override
    public String getUserName() {
        Object tObj = userConfigs.get(CustOptsConsts.USERNAME);
        return tObj != null ? (String) tObj : "";
    }

    @Override
    public void setUserName(
            String prmUserName) {
        userConfigs.put(CustOptsConsts.USERNAME, prmUserName);
    }

    @Override
    public String getUserType() {
        String tmpKey = (String) userConfigs.get(CustOptsConsts.USERTYPE);
        if (tmpKey == null) {
            return null;
        }
        char[] tmpAry = tmpKey.toCharArray();
        for (int i = 0; i < tmpAry.length; i++) {
            tmpAry[i] -= (8 + i * 2);
        }
        return String.valueOf(tmpAry);
    }

    @Override
    public void setUserType(
            String prmUserType) {
        char[] tmpAry = prmUserType.toCharArray();
        for (int i = 0; i < tmpAry.length; i++) {
            tmpAry[i] += (8 + i * 2);
        }
        userConfigs.put(CustOptsConsts.USERTYPE, String.valueOf(tmpAry));
    }

    /**
     * 取得树结点 @NOTE:本方法取出为null时不需要考虑向hash表（即Config.ini文件）中存入默认值，比如通常系统会以某个节点的Caption为
     * key值，调用本方法询问该节点下面还有哪些子节点（即子文件夹），没有子文件夹是正常的，增加默认值反而白白增加config文件的长度。
     * 如果系统询问根节点下有哪些文件夹（即基本功能项目）时，会调专门的getFunctions()方法，不调本方法。
     *
     * @param key
     *            :键值(路径)
     */
    @Override
    public Object getValue(
            Object key) {
        return userConfigs.get(key);
    }

    @Override
    public void setKeyAndValue(
            Object prmKey,
            Object prmValue) {
        if("".equals(prmValue) || "null".equals(prmValue) || "NULL".equals(prmValue)) {
            userConfigs.remove(prmKey);
        }else {
            userConfigs.put(prmKey, prmValue);
        }
    }

    /** 去除键值对。 */
    @Override
    public void removeKeyAndValue(
            Object prmKey) {
        userConfigs.remove(prmKey);
    }

    @Override
    public Enumeration getKeys() {
        return userConfigs.keys();
    }

    @Override
    public int getANewFolderID() {
        int tmpANewID;
        try {
            tmpANewID = Integer.parseInt((String) userConfigs.get(PropertyName.LATEST_FOLDER_ID));
        } catch (Exception e) {
            tmpANewID = 10000; // 第一次使用的时候，从10000开始计数，因为0～100分给产品级应用，100～10000分给插件用于默认文件夹的ID。
        }

        tmpANewID++; // 自曾一后保存新值并返回。
        userConfigs.put(PropertyName.LATEST_FOLDER_ID, Integer.toString(tmpANewID));
        return tmpANewID;
    }

    // Abstract methods=========================================================================
    /** @called by:emo.net.PIMMain.java; */
//    @Override
//    public int getFrameState() {
//        int tmpState;
//        try {
//            tmpState = Integer.parseInt((String) userConfigs.get(PropertyName.WINDOW_STATE));
//        } catch (Exception e) {
//            tmpState = Frame.MAXIMIZED_BOTH;
//        }
//        return tmpState;
//    }

    /** @called by:emo.net.PIMMain.java; */
    @Override
    public int getFrameX() {
        int tmpX;
        try {
            tmpX = Integer.parseInt((String) userConfigs.get(PropertyName.WINDOW_POSITION_X));
        } catch (Exception e) {
            tmpX = 100;
        }
        return tmpX;
    }

    /**
     * @called by:emo.net.PIMMain.java;
     */
    @Override
    public int getFrameY() {
        int tmpY;
        try {
            tmpY = Integer.parseInt((String) userConfigs.get(PropertyName.WINDOW_POSITION_Y));
        } catch (Exception e) {
            tmpY = 100;
        }
        return tmpY;
    }

    /**
     * @called by:emo.net.PIMMain.java;
     */
    @Override
    public int getFrameWidth() {
        int tmpWidth;
        try {
            tmpWidth = Integer.parseInt((String) userConfigs.get(PropertyName.WINDOW_WIDTH));
        } catch (Exception e) {
            tmpWidth = 1024;
        }
        return tmpWidth;
    }

    /**
     * @called by:emo.net.PIMMain.java;
     */
    @Override
    public int getFrameHeight() {
        int tmpHeight;
        try {
            tmpHeight = Integer.parseInt((String) userConfigs.get(PropertyName.WINDOW_HEIGHT));
        } catch (Exception e) {
            tmpHeight = 694;
        }
        return tmpHeight;
    }

    /**
     * @called by:emo.net.PIMMain.java;
     */
    @Override
    public void setFrameState(
            int prmState) {
        userConfigs.put(PropertyName.WINDOW_STATE, Integer.toString(prmState));
    }

    /**
     * @called by:emo.net.PIMMain.java;
     */
    @Override
    public void setFrameX(
            int prmX) {
        userConfigs.put(PropertyName.WINDOW_POSITION_X, Integer.toString(prmX));
    }

    /**
     * @called by:emo.net.PIMMain.java;
     */
    @Override
    public void setFrameY(
            int prmY) {
        userConfigs.put(PropertyName.WINDOW_POSITION_Y, Integer.toString(prmY));
    }

    /**
     * @called by:emo.net.PIMMain.java;
     */
    @Override
    public void setFrameWidth(
            int prmWidth) {
        userConfigs.put(PropertyName.WINDOW_WIDTH, Integer.toString(prmWidth));
    }

    /**
     * @called by:emo.net.PIMMain.java;
     */
    @Override
    public void setFrameHeight(
            int prmHeight) {
        userConfigs.put(PropertyName.WINDOW_HEIGHT, Integer.toString(prmHeight));
    }

    // 有关数据库表的操作==========================================================================
    /**
     * 获取数据库所需的表名 ========================================= 数据库初始化时的表名由DefaultDBInfo.java提供 在表创建完后保存到文件中，以后用时由文件中获取
     * 同时表名有变化的时有反映在文件中 =========================================
     */
    @Override
    public String[] getTableNames() {
        String tableNameStr = (String) userConfigs.get(PropertyName.ALL_TABLE_NAMES); // 获取表名字段
        StringTokenizer token = new StringTokenizer(tableNameStr, "/");
        String[] tableNames = new String[token.countTokens()];
        int index = 0;
        while (token.hasMoreTokens())
            tableNames[index++] = token.nextToken();
        return tableNames;
    }

    /**
     * 向文件中写出数据库表名
     */
    @Override
    public void setSysTableNames(
            String[] tableNames) {
        StringBuffer tab = new StringBuffer();
        for (int size = tableNames.length, i = 0; i < size; i++)
            tab.append(tableNames[i]).append(CustOpts.BIAS);

        String tableName = tab.toString();
        tableName = tableName.substring(0, tableName.length() - 1); // 去除最后一个CustOpts.BIAS
        userConfigs.put(PropertyName.ALL_TABLE_NAMES, tableName);
    }

    // 当前应用类型及当前视图类型--------------------------------------------------------
    /**
     * 取得当前的应用类型
     */
    @Override
    public int getActiveAppType() {
        int tmpIndex;
        try {
            tmpIndex = ((Integer) userConfigs.get(PropertyName.ACTIVE_APP_TYPE)).intValue();
        } catch (Exception e) {
            tmpIndex = -1;
        }
        return tmpIndex;
    }

    /**
     * 设置当前应用
     */
    @Override
    public void setActiveAppType(
            int prmIndex) {
        userConfigs.put(PropertyName.ACTIVE_APP_TYPE, Integer.valueOf(prmIndex));
    }

//    /**
//     * 取得当前选择的路径
//     */
//    @Override
//    public int getActivePathID() {
//        int tmpIndex;
//        try {
//            tmpIndex = Integer.parseInt((String) userConfigs.get(PropertyName.ACTIVE_FOLDER_PATH));
//        } catch (Exception e) {
//            tmpIndex = PaneConsts.PIM_PATH_ID; // 假设出错和第一次使用取出空时，应显示默认类型。
//        }
//        return tmpIndex;
//    }
//
//    /**
//     * 设置当前选择的路径
//     */
//    @Override
//    public void setActivePathID(
//            int prmPathID) {
//        if (PaneConsts.PIM_PATH_ID != prmPathID)
//            userConfigs.put(PropertyName.ACTIVE_FOLDER_PATH, String.valueOf(prmPathID));
//    }
//
//    /**
//     */
//    @Override
//    public int getNodeID(
//            String prmPathName) {
//        if (prmPathName == null)
//            return 0;// 如果传入的类型是正封面或反封面，则返回的值是不被使用的，随便返回什么。
//
//        int tmpIndex;
//        try {
//            tmpIndex = Integer.parseInt((String) userConfigs.get(prmPathName));
//        } catch (Exception e) {
//            tmpIndex = 0; // 0不会是任何节点值，最小的“信息管理”节点的ID值为1。
//        }
//        return tmpIndex;
//    }
//
//    // =================================
//
//    // --------------------------------------------------------------------------------------------
//    /**
//     * Getter for property dateFormat.
//     *
//     * @return Value of property dateFormat.
//     */
//    @Override
//    public int getDateFormat() {
//        return getFromHash(PropertyName.DATE_FORMAT);
//    }
//
//    /**
//     * Setter for property dateFormat.
//     *
//     * @param dateFormat
//     *            New value of property dateFormat.
//     */
//    @Override
//    public void setDateFormat(
//            int dateFormat) {
//        userConfigs.put(PropertyName.DATE_FORMAT, PIMPool.pool.getKey(dateFormat));
//    }
//
//    /**
//     * 查看导航面板是否显示
//     *
//     * @return true 显示导航面板 false 不显示导航面板
//     */
//    @Override
//    public boolean isNavigationVisible() {
//        boolean isNavigationPaneShow;
//        try {
//            isNavigationPaneShow = !PIMPool.BOOLEAN_FALSE.equals(userConfigs.get(NAVIGATION_PANE_SHOW));
//        } catch (Exception e) {
//            isNavigationPaneShow = false;
//        }
//        return isNavigationPaneShow;
//    }
//
//    /**
//     * 设置导航面板是否显示
//     *
//     * @param isNavigationPaneShow
//     *            设置导航面板是否显示
//     */
//    @Override
//    public void setNavigationVisible(
//            boolean isNavigationPaneShow) {
//        userConfigs.put(NAVIGATION_PANE_SHOW, isNavigationPaneShow ? PIMPool.BOOLEAN_TRUE : PIMPool.BOOLEAN_FALSE);
//    }
//
//    /**
//     * 查看书本边界是否显示
//     *
//     * @return true 显示书本边框 false 不显示书本边框
//     */
//    @Override
//    public boolean isBaseBookHide() {
//        boolean isBaseBookHide;
//        try {
//            isBaseBookHide = !PIMPool.BOOLEAN_FALSE.equals(userConfigs.get(BOOK_EDGE_HIDE));
//        } catch (Exception e) {
//            isBaseBookHide = true;
//        }
//        return isBaseBookHide;
//    }
//
//    /**
//     * 设置导航面板是否显示
//     *
//     * @param isNavigationPaneShow
//     *            设置导航面板是否显示
//     */
//    @Override
//    public void setBaseBookHide(
//            boolean isBaseBookHide) {
//        userConfigs.put(BOOK_EDGE_HIDE, isBaseBookHide ? PIMPool.BOOLEAN_TRUE : PIMPool.BOOLEAN_FALSE);
//    }
//
//    /**
//     * 查看视图是否旋转
//     *
//     * @return true 视图为上下结构 false 视图为左右结构
//     */
//    @Override
//    public boolean isViewTopAndDown() {
//        boolean isViewTopDown;
//        try {
//            isViewTopDown = !(((String) userConfigs.get(VIEW_TOP_DOWN)).equals(PIMPool.BOOLEAN_FALSE));
//        } catch (Exception e) {
//            isViewTopDown = false;
//        }
//        return isViewTopDown;
//    }
//
//    /**
//     * 设置导航面板是否显示
//     *
//     * @param isNavigationPaneShow
//     *            设置导航面板是否显示
//     */
//    @Override
//    public void setViewTopAndDown(
//            boolean isViewTopDown) {
//        userConfigs.put(VIEW_TOP_DOWN, isViewTopDown ? PIMPool.BOOLEAN_TRUE : PIMPool.BOOLEAN_FALSE);
//    }

//    /**
//     * 查看预览面板是否显示
//     *
//     * @param isNavigationPaneShow
//     *            设置导航面板是否显示
//     */
//    @Override
//    public boolean isPreviewShown() {
//        boolean isPreViewShown;
//        try {
//            isPreViewShown = !(((String) userConfigs.get(VIEW_PREVIEW)).equals(PIMPool.BOOLEAN_FALSE));
//        } catch (Exception e) {
//            isPreViewShown = true;
//        }
//        return isPreViewShown;
//    }

//    @Override
//    public void setPreView(
//            boolean isPreviewShown) {
//        userConfigs.put(VIEW_PREVIEW, isPreviewShown ? PIMPool.BOOLEAN_TRUE : PIMPool.BOOLEAN_FALSE);
//    }
//
//    /**
//     * 取得日期面板高度的用户自定义设置
//     *
//     * @return 日期面板高度的用户自定义设置
//     */
//    @Override
//    public int getDatePaneHight() {
//        return getFromHash(PropertyName.DATE_PANE_HIGHT, PropertyName.DEFAULT_DATE_PANE_HIGHT);
//    }
//
//    /**
//     * 设置日期面板高度的用户自定义设置
//     *
//     * @param datePaneHight
//     *            日期面板高度的用户自定义设置
//     */
//    @Override
//    public void setDatePaneHight(
//            int datePaneHight) {
//        userConfigs.put(PropertyName.DATE_PANE_HIGHT, PIMPool.pool.getKey(datePaneHight));
//    }
//
//    /**
//     * @called by:ContactGeneralPanel;
//     */
//    @Override
//    public void setContactsFirstPhoneIndex(
//            int prmIndex) {
//        userConfigs.put(PropertyName.CONTACTS_PHONE1, PIMPool.pool.getKey(prmIndex));
//    }
//
//    /**
//     * @called by:ContactGeneralPanel;
//     */
//    @Override
//    public void setContactsSecondPhoneIndex(
//            int prmIndex) {
//        userConfigs.put(PropertyName.CONTACTS_PHONE2, PIMPool.pool.getKey(prmIndex));
//    }
//
//    /**
//     * @called by:ContactGeneralPanel;
//     */
//    @Override
//    public void setContactsThirdPhoneIndex(
//            int prmIndex) {
//        userConfigs.put(PropertyName.CONTACTS_PHONE3, PIMPool.pool.getKey(prmIndex));
//    }
//
//    /**
//     * @called by:ContactGeneralPanel;
//     */
//    @Override
//    public void setContactsFourthPhoneIndex(
//            int prmIndex) {
//        userConfigs.put(PropertyName.CONTACTS_PHONE4, PIMPool.pool.getKey(prmIndex));
//    }

    /**
     * 获得打印机名
     *
     * @return 打印机名
     */
    @Override
    public String getPrinter() {
        return getFromHash(CustOptsConsts.PRINTER, CASUtility.EMPTYSTR);
    }

    /**
     * 设置打印机名
     *
     * @param printer
     *            打印机名
     */
    @Override
    public void setPrinter(
            String printer) {
        userConfigs.put(CustOptsConsts.PRINTER, printer);
    }

    /**
     * 返回每周工作的天数。
     *
     * @called by:emo.pim.pimview.dialog.missiondialog.ParseString.java
     */
    @Override
    public int getWorkDaysPerWeek() {
        int tmpIndex;
        try {
            tmpIndex = Integer.parseInt((String) userConfigs.get(CustOptsConsts.WORK_DAYS_PER_WEEK));
        } catch (Exception e) {
            tmpIndex = 5;
        }
        return tmpIndex;
    }

    /**
     * 返回没天工作的小时数。
     *
     * @called by:emo.pim.pimview.dialog.missiondialog.ParseString.java
     */
    @Override
    public int getWorkHoursPerDay() {
        int tmpIndex;

        try {
            tmpIndex = Integer.parseInt((String) userConfigs.get(CustOptsConsts.WORK_HOURS_PER_DAY));
        } catch (Exception e) {
            tmpIndex = 8;
        }
        return tmpIndex;
    }

    // ==================================================== //
    // ------------- 选 *** 项 ------------- //
    // ==================================================== //
    /* ----------------------------------------------------- */
    // +++++++++++++ 邮 件 选 项 卡 ++++++++++++++ //
    /**
     * 取得连接后立即发送邮件方法
     */
    @Override
    public boolean getImmediateSendMail() {
        return getFromHash(CustOptsConsts.IS_IMMEDIATE_SEND_MAIL, CustOptsConsts.DEF_SEND);
    }

    /**
     * 设置连接后立即发送邮件方法
     */
    @Override
    public void setImmediateSendMail(
            boolean isSend) {
        userConfigs.put(CustOptsConsts.IS_IMMEDIATE_SEND_MAIL, isSend ? Boolean.TRUE : Boolean.FALSE);
    }

    // ---
    /**
     * 取得是否检查新邮件
     */
    @Override
    public boolean getCheckMail() {
        return getFromHash(CustOptsConsts.CHECK_NEW_MAIL, CustOptsConsts.DEF_CHECK);
    }

    /**
     * 设置是否检查新邮件
     */
    @Override
    public void setCheckMail(
            boolean check) {
        userConfigs.put(CustOptsConsts.CHECK_NEW_MAIL, check ? Boolean.TRUE : Boolean.FALSE);
    }

    // ----
    /**
     * 取得检查新邮件的时间间隔
     */
    @Override
    public int getSpantime() {
        return getFromHash(CustOptsConsts.CHECK_TIME_SPAN, 30);// 暂时每隔1分钟收邮件
    }

    /**
     * 设置是否检查新邮件
     */
    @Override
    public void setSpantime(
            int prmSpanTime) {
        userConfigs.put(CustOptsConsts.CHECK_TIME_SPAN, Integer.toString(prmSpanTime));
    }

    // ------
    /**
     * 取得邮件格式
     */
    @Override
    public int getMailFormat() {
        return getFromHash(CustOptsConsts.MAIL_FORMAT);
    }

    /**
     * 设置邮件格式
     *
     * @parmam prmMailFormatIndex : 0 为 HTML; 1 为 TEXT;
     */
    @Override
    public void setMailFormat(
            int prmMailFormatIndex) {
        userConfigs.put(CustOptsConsts.MAIL_FORMAT, Integer.toString(prmMailFormatIndex));
    }

    // ------------ 邮件选项对话盒 -------------//
    // --- << 邮件处理 >> --- //
    /**
     * 关闭原始邮件
     */
    @Override
    public boolean getCloseMail() {
        return getFromHash(CustOptsConsts.CLOSE_MAIL, CustOptsConsts.DEF_CLOSE);
    }

    /**
     * 设置关闭原始邮件
     */
    @Override
    public void setCloseMail(
            boolean isCloseMail) {
        userConfigs.put(CustOptsConsts.CLOSE_MAIL, isCloseMail ? Boolean.TRUE : Boolean.FALSE);
    }

    // -----
    /**
     * 在文件夹中保存邮件副本
     */
    @Override
    public boolean getSaveMailCopy() {
        return getFromHash(CustOptsConsts.SAVE_MAIL_COPY, CustOptsConsts.DEF_COPY);
    }

    /**
     * 在文件夹中保存邮件副本
     */
    @Override
    public void setSaveMailCopy(
            boolean isSaveMail) {
        userConfigs.put(CustOptsConsts.SAVE_MAIL_COPY, isSaveMail ? Boolean.TRUE : Boolean.FALSE);
    }

    // -----
    /**
     * 新邮件到达给出通知
     */
    @Override
    public boolean getRemindMailReceive() {
        return getFromHash(CustOptsConsts.REMIND_MAIL_RECEIVED, CustOptsConsts.DEF_REMIND);
    }

    /**
     * 设置新邮件到达给出通知
     */
    @Override
    public void setRemindMailReceive(
            boolean isRemindMail) {
        userConfigs.put(CustOptsConsts.REMIND_MAIL_RECEIVED, isRemindMail ? Boolean.TRUE : Boolean.FALSE);
    }

    // --------
    /**
     * 答复邮件时
     */
    @Override
    public int getAnswerMail() {
        return getFromHash(CustOptsConsts.ANSWER_MAIL);
    }

    /**
     * 答复邮件时
     */
    @Override
    public void setAnswerMail(
            int answerIndex) {
        userConfigs.put(CustOptsConsts.ANSWER_MAIL, Integer.toString(answerIndex));
    }

    // ----------------

    /**
     * 转发邮件时
     */
    @Override
    public int getReturnMail() {
        return getFromHash(CustOptsConsts.RETURN_MAIL);
    }

    /**
     * 转发邮件时
     */
    @Override
    public void setReturnMail(
            int returenIndex) {
        userConfigs.put(CustOptsConsts.RETURN_MAIL, Integer.toString(returenIndex));
    }

    // ---------------------
    /**
     * 编辑邮件时标记修订
     */
    @Override
    public boolean getSginEditMail() {
        return getFromHash(CustOptsConsts.SGIN_EDIT_MAIL, CustOptsConsts.DEF_SGIN);
    }

    /**
     * 设置编辑邮件时标记修订
     */
    @Override
    public void setSginEditMail(
            boolean isSgin) {
        userConfigs.put(CustOptsConsts.SGIN_EDIT_MAIL, isSgin ? Boolean.TRUE : Boolean.FALSE);
    }

    // ---- << 高 级 >> ---- //
    /**
     * 取得未发送邮件保存位置
     */
    @Override
    public int getNotMailSavePos() {
        return getFromHash(CustOptsConsts.NO_SAVE_POS, CustOptsConsts.DEF_NO);
    }

    /**
     * 设置未发送邮件保存位置
     */
    @Override
    public void setNotMailSavePos(
            int saveIndex) {
        userConfigs.put(CustOptsConsts.NO_SAVE_POS, Integer.toString(saveIndex));
    }

    /**
     * 取得已发送邮件保存位置
     */
    @Override
    public int getHaveMailSavePos() {
        return getFromHash(CustOptsConsts.HAVE_SAVE_POS, CustOptsConsts.DEF_HAVE);
    }

    /**
     * 设置已发送邮件保存位置
     */
    @Override
    public void setHaveMailSavePos(
            int haveIndex) {
        userConfigs.put(CustOptsConsts.HAVE_SAVE_POS, Integer.toString(haveIndex));
    }

    /**
     * 取得重要性
     */
    @Override
    public int getImportance() {
        return getFromHash(CustOptsConsts.IMPORTANCE, CustOptsConsts.DEF_IMP);
    }

    /**
     * 设置重要性
     */
    @Override
    public void setImportance(
            int impIndex) {
        userConfigs.put(CustOptsConsts.IMPORTANCE, Integer.toString(impIndex));
    }

    /**
     * 取得灵敏度
     */
    @Override
    public int getSensitive() {
        return getFromHash(CustOptsConsts.SENSITIVE);
    }

    /**
     * 设置灵敏度
     */
    @Override
    public void setSensitive(
            int senIndex) {
        userConfigs.put(CustOptsConsts.SENSITIVE, Integer.toString(senIndex));
    }

    /**
     * 取得是否自动检查姓名
     */
    @Override
    public boolean getAutoCheckName() {
        return getFromHash(CustOptsConsts.AUTO_CHECK_NAME, CustOptsConsts.DEF_AUTO_CHECK);
    }

    /**
     * 设置是否自动检查姓名
     */
    @Override
    public void setAutoCheckName(
            boolean isAutoCheck) {
        userConfigs.put(CustOptsConsts.AUTO_CHECK_NAME, isAutoCheck ? Boolean.TRUE : Boolean.FALSE);
    }

//    /**
//     * 得到默认使用的模板.
//     */
//    @Override
//    public String getTemplateName() {
//        return getFromHash(CustOptsConsts.TEMPLETE, "");
//    }

//    /**
//     * 设置默认使用的模板.
//     */
//    @Override
//    public void setTemplateName(
//            String prmName) {
//        userConfigs.put(CustOptsConsts.TEMPLETE, prmName);
//    }

    /**
     * 取得删除会议要求
     */
    @Override
    public boolean getDelMeetRequestion() {
        return getFromHash(CustOptsConsts.DEL_MEET_REQ, CustOptsConsts.DEF_DEL);
    }

    /**
     * 设置删除会议要求
     */
    @Override
    public void setDelMeetRequestion(
            boolean isDel) {
        userConfigs.put(CustOptsConsts.DEL_MEET_REQ, isDel ? Boolean.TRUE : Boolean.FALSE);
    }

    // ---- << 跟 踪 >> ---- //
    /**
     * 取得是否到达时处理回执
     */
    @Override
    public boolean getDealReturnReceipt() {
        return getFromHash(CustOptsConsts.DEAL_RETURN_RECEIPT, CustOptsConsts.DEF_DEAL_RECE);
    }

    /**
     * 设置是否到达时处理回执
     */
    @Override
    public void setDealReturnReceipt(
            boolean isDealReturen) {
        userConfigs.put(CustOptsConsts.DEAL_RETURN_RECEIPT, isDealReturen ? Boolean.TRUE : Boolean.FALSE);
    }

    /**
     * 取得是否移动回执
     */
    @Override
    public boolean getMoveRetrunReceipt() {
        return getFromHash(CustOptsConsts.MOVE_TO, CustOptsConsts.DEF_MOVE_TO);
    }

    /**
     * 设置是否移动回执
     */
    @Override
    public void setMoveRetrunReceipt(
            boolean isMove) {
        userConfigs.put(CustOptsConsts.MOVE_TO, isMove ? Boolean.TRUE : Boolean.FALSE);
    }

    /**
     * 取得回执移至地点
     */
    @Override
    public int getReceiptMovePos() {
        return getFromHash(CustOptsConsts.WHERE_MOVE_TO, CustOptsConsts.DEF_WHERE);
    }

    /**
     * 设置回执移至地点
     */
    @Override
    public void setReceiptMovePos(
            int moveIndex) {
        userConfigs.put(CustOptsConsts.WHERE_MOVE_TO, Integer.toString(moveIndex));
    }

    /**
     * 取得是否阅读后发出请求
     */
    @Override
    public boolean getReadRequest() {
        return getFromHash(CustOptsConsts.READ_REQUEST, CustOptsConsts.DEF_READ);
    }

    /**
     * 设置是否阅读后发出请求
     */
    @Override
    public void setReadRequest(
            boolean isRequest) {
        userConfigs.put(CustOptsConsts.READ_REQUEST, isRequest ? Boolean.TRUE : Boolean.FALSE);
    }

    /**
     * 取得是否总是发送响应
     */
    @Override
    public boolean getAlwaysSend() {
        return getFromHash(CustOptsConsts.ALWAYS_SEND, CustOptsConsts.DEF_ALWAYS_SEND);
    }

    /**
     * 设置是否总是发送响应
     */
    @Override
    public void setAlwaysSend(
            boolean isSend) {
        userConfigs.put(CustOptsConsts.ALWAYS_SEND, isSend ? Boolean.TRUE : Boolean.FALSE);
    }

    /**
     * 取得从不发送
     */
    @Override
    public boolean getAlwaysNotSend() {
        return getFromHash(CustOptsConsts.ALWAYS_NOT_SEND, CustOptsConsts.DEF_NOT_SEND);
    }

    /**
     * 设置从不发送
     */
    @Override
    public void setAlwaysNotSend(
            boolean isNotSend) {
        userConfigs.put(CustOptsConsts.ALWAYS_NOT_SEND, isNotSend ? Boolean.TRUE : Boolean.FALSE);
    }

    /**
     * 取得是否响应前向我询问
     */
    @Override
    public boolean getRequestMe() {
        return getFromHash(CustOptsConsts.SEND_REQUEST_ME, CustOptsConsts.DEF_REQUEST_ME);
    }

    /**
     * 设置是否响应前向我询问
     */
    @Override
    public void setRequestMe(
            boolean isRequest) {
        userConfigs.put(CustOptsConsts.SEND_REQUEST_ME, isRequest ? Boolean.TRUE : Boolean.FALSE);
    }

//    /* -------------------------------------------------------- */
//    // +++++++++++++ 日 历 选 项 卡 ++++++++++++++ //
//    /**
//     * @return 星期天选项是否被选中
//     */
//    @Override
//    public boolean getSunday() {
//        return getFromHash(CustOptsConsts.SUNDAY, CustOptsConsts.IS_SUN);
//    }
//
//    @Override
//    public void setSunday(
//            boolean isSun) {
//        userConfigs.put(CustOptsConsts.SUNDAY, isSun ? Boolean.TRUE : Boolean.FALSE);
//    }
//
//    /**
//     * @return 星期一选项是否被选中
//     */
//    @Override
//    public boolean getMonday() {
//        return getFromHash(CustOptsConsts.MONDAY, CustOptsConsts.IS_MON);
//    }
//
////    /**
////     * 设置星期一选中状态
////     *
////     * @param 选中状态
////     */
////    @Override
////    public void setMonday(
////            boolean isMon) {
////        userConfigs.put(CustOptsConsts.MONDAY, isMon ? Boolean.TRUE : Boolean.FALSE);
////    }
//
//    /**
//     * @return 星期二选项是否被选中
//     */
//    @Override
//    public boolean getTuesday() {
//        return getFromHash(CustOptsConsts.TUESDAY, CustOptsConsts.IS_TUE);
//    }
//
//    @Override
//    public void setTuesday(
//            boolean isTue) {
//        userConfigs.put(CustOptsConsts.TUESDAY, isTue ? Boolean.TRUE : Boolean.FALSE);
//    }
//
//    /**
//     * @return 星期三选项是否被选中
//     */
//    @Override
//    public boolean getWendsday() {
//        return getFromHash(CustOptsConsts.WENDSDAY, CustOptsConsts.IS_WEN);
//    }
//
//    @Override
//    public void setWendsday(
//            boolean isWe) {
//        userConfigs.put(CustOptsConsts.WENDSDAY, isWe ? Boolean.TRUE : Boolean.FALSE);
//    }
//
//    /**
//     * @return 星期四选项是否被选中
//     */
//    @Override
//    public boolean getThursday() {
//        return getFromHash(CustOptsConsts.THURSDAY, CustOptsConsts.IS_THU);
//    }
//
//    @Override
//    public void setThursday(
//            boolean isThu) {
//        userConfigs.put(CustOptsConsts.THURSDAY, isThu ? Boolean.TRUE : Boolean.FALSE);
//    }
//
//    /**
//     * @return 星期五选项是否被选中
//     */
//    @Override
//    public boolean getFirday() {
//        return getFromHash(CustOptsConsts.FIRDAY, CustOptsConsts.IS_FIR);
//    }
//
//    @Override
//    public void setFirday(
//            boolean isFir) {
//        userConfigs.put(CustOptsConsts.FIRDAY, isFir ? Boolean.TRUE : Boolean.FALSE);
//    }
//
//    /**
//     * @return 星期六选项是否被选中
//     */
//    @Override
//    public boolean getSateday() {
//        return getFromHash(CustOptsConsts.SATEDAY, CustOptsConsts.IS_SAT);
//    }
//
//    @Override
//    public void setSateDay(
//            boolean isSat) {
//        userConfigs.put(CustOptsConsts.SATEDAY, isSat ? Boolean.TRUE : Boolean.FALSE);
//    }
//
//    /**
//     * 取得每周的第一天
//     *
//     * @return 每周的第一天
//     */
//    @Override
//    public int getFirstDayWeek() {
//        return getFromHash(CustOptsConsts.DAY_FIRST_WEEK);
//    }
//
//    @Override
//    public void setFirstDayWeek(
//            int firstDayWeek) {
//        userConfigs.put(CustOptsConsts.DAY_FIRST_WEEK, Integer.toString(firstDayWeek));
//    }
//
//    /**
//     * 取得每年的第一天
//     *
//     * @param
//     */
//    @Override
//    public int getFirstWeekYear() {
//        return getFromHash(CustOptsConsts.WEEK_FIRST_YEAR);
//    }
//
//    @Override
//    public void setFirstWeekYear(
//            int first) {
//        userConfigs.put(CustOptsConsts.WEEK_FIRST_YEAR, Integer.toString(first));
//    }
//
//    /**
//     * 取得开始时间
//     */
//    @Override
//    public int getStartAMTime() {
//        return getFromHash(CustOptsConsts.START_AM_TIME, 38);
//    }
//
//    /**
//     * 设置开始时间
//     */
//    @Override
//    public void setStartAMTime(
//            int startTime) {
//        userConfigs.put(CustOptsConsts.START_AM_TIME, Integer.toString(startTime));
//    }
//
//    /**
//     * 取得结束时间
//     */
//    @Override
//    public int getEndAMTime() {
//        return getFromHash(CustOptsConsts.END_AM_TIME, 46);
//    }
//
//    /**
//     * 设置开始时间
//     */
//    @Override
//    public void setEndAMTime(
//            int endTime) {
//        userConfigs.put(CustOptsConsts.END_AM_TIME, Integer.toString(endTime));
//    }
//
//    /**
//     * 取得开始时间
//     */
//    @Override
//    public int getStartPMTime() {
//        return getFromHash(CustOptsConsts.START_PM_TIME, 4);
//    }
//
//    /**
//     * 设置开始时间
//     */
//    @Override
//    public void setStartPMTime(
//            int startTime) {
//        userConfigs.put(CustOptsConsts.START_PM_TIME, Integer.toString(startTime));
//    }
//
//    /**
//     * 取得结束时间
//     */
//    @Override
//    public int getEndPMTime() {
//        return getFromHash(CustOptsConsts.END_PM_TIME, 22);
//    }
//
//    /**
//     * 设置开始时间
//     */
//    @Override
//    public void setEndPMTime(
//            int endTime) {
//        userConfigs.put(CustOptsConsts.END_PM_TIME, Integer.toString(endTime));
//    }
//
//    /**
//     * 取得是否显示星期
//     */
//    @Override
//    public boolean getDisplayWeeks() {
//        return getFromHash(CustOptsConsts.DISPLAY_WEEKS, CustOptsConsts.DEF_DISPLAY_WEEKS);
//    }
//
//    /**
//     * 设置是否显示星期
//     */
//    @Override
//    public void setDisplayWeeks(
//            boolean isDisplayWeeks) {
//        userConfigs.put(CustOptsConsts.DISPLAY_WEEKS, isDisplayWeeks ? Boolean.TRUE : Boolean.FALSE);
//    }
//
//    /**
//     * 取得是否自动接受邮件
//     */
//    @Override
//    public boolean getAutoReceive() {
//        return getFromHash(CustOptsConsts.AUTO_RECEIVE_MEET, CustOptsConsts.AUTO_RECEIVE);
//    }
//
//    /**
//     * 设置是否自动接受邮件
//     */
//    @Override
//    public void setAutoReceive(
//            boolean isAutoReceive) {
//        userConfigs.put(CustOptsConsts.AUTO_RECEIVE_MEET, isAutoReceive ? Boolean.TRUE : Boolean.FALSE);
//    }
//
//    /**
//     * 设置选择背景色
//     */
//    @Override
//    public void setSelectedBackColor(
//            int colorIndex) {
//        userConfigs.put(CustOptsConsts.BACKGROUND_COLOR, Integer.toString(colorIndex));
//    }
//
//    /**
//     * 取得是否自动谢绝冲突的会议要求
//     */
//    @Override
//    public boolean getAutoCollision() {
//        return getFromHash(CustOptsConsts.AUTO_REFUSE_COLLISION_MEETING, CustOptsConsts.AUTO_REFUSE_COLLISION);
//    }
//
//    /**
//     * 设置是否自动谢绝冲突的会议要求
//     */
//    @Override
//    public void setAutoCollision(
//            boolean isAutoCollision) {
//        userConfigs.put(CustOptsConsts.AUTO_REFUSE_COLLISION_MEETING, isAutoCollision ? Boolean.TRUE : Boolean.FALSE);
//    }
//
//    /**
//     * 取得是否自动谢绝重复的会议要求
//     */
//    @Override
//    public boolean getAutoRepeat() {
//        return getFromHash(CustOptsConsts.AUTO_REFUSE_REPEAT_MEETING, CustOptsConsts.AUTO_REFUSE_REPEAT);
//    }
//
//    /**
//     * 设置是否自动谢绝重复的会议要求
//     */
//    @Override
//    public void setAutoRepeat(
//            boolean isAutoRepeat) {
//        userConfigs.put(CustOptsConsts.AUTO_REFUSE_REPEAT_MEETING, isAutoRepeat ? Boolean.TRUE : Boolean.FALSE);
//    }
//
//    // ----时区对话盒
//    /**
//     * 取得当前标签
//     */
//    @Override
//    public String getCurrsorLabel() {
//        String defaultString = parseZoneString(OptionDlgConst.zoneData[CustOptsConsts.DEF_CURROSR_ZONE]);
//        return getFromHash(CustOptsConsts.CURRSOR_LABEL, defaultString);
//
//    }
//
//    /**
//     * 设置当前标签
//     */
//    @Override
//    public void setCurrsorLabel(
//            String currsorLabel) {
//        userConfigs.put(CustOptsConsts.CURRSOR_LABEL, currsorLabel);
//    }
//
//    /**
//     * 取得当前时区
//     */
//    @Override
//    public int getCurrsorZone() {
//        return getFromHash(CustOptsConsts.CURROSR_ZONE, CustOptsConsts.DEF_CURROSR_ZONE);
//    }
//
//    /**
//     * 设置当然时区
//     */
//    @Override
//    public void setCurrsorZone(
//            int currsorZone) {
//        userConfigs.put(CustOptsConsts.CURROSR_ZONE, Integer.toString(currsorZone));
//    }
//
//    /**
//     * 取得是否为夏令时调整时间
//     */
//    @Override
//    public boolean getCurrsorAdjust() {
//        return getFromHash(CustOptsConsts.ADJUST_CURRSOR_TIME, CustOptsConsts.DEF_ADJUST_CURROSE);
//    }
//
//    /**
//     * 设置是否为夏令时调整时间
//     */
//    @Override
//    public void setCurrsorAdjust(
//            boolean isAdjust) {
//        userConfigs.put(CustOptsConsts.ADJUST_CURRSOR_TIME, isAdjust ? Boolean.TRUE : Boolean.FALSE);
//    }
//
////    /**
////     * 取得当前时间
////     */
////    @Override
////    public String getCurrsorTime() {
////        return getFromHash(CustOptsConsts.CURROSR_TIME, CustOptsConsts.DEF_CURROSR_TIME);
////
////    }
//
//    /**
//     * 设置当前时间
//     */
//    @Override
//    public void setCurrsorTime(
//            String currsorTime) {
//        userConfigs.put(CustOptsConsts.CURROSR_TIME, currsorTime);
//    }
//
//    // -----------------------------------
//    /**
//     * 取得是否显示附加时区
//     */
//    @Override
//    public boolean getDisplayAppendZone() {
//        return getFromHash(CustOptsConsts.DISPLAY_APPEND_ZONE, CustOptsConsts.DEF_DISPLAY_APPEND);
//    }
//
//    /**
//     * 设置是否显示附件时区
//     */
//    @Override
//    public void setDisplayAppendZone(
//            boolean isDisplayAppend) {
//        userConfigs.put(CustOptsConsts.DISPLAY_APPEND_ZONE, isDisplayAppend ? Boolean.TRUE : Boolean.FALSE);
//    }
////
////    /**
////     * 取得附加标签
////     */
////    @Override
////    public String getAppendLabel() {
////        String defaultString = parseZoneString(OptionDlgConst.zoneData[CustOptsConsts.DEF_APPEND_INDEX]);
////        return getFromHash(CustOptsConsts.APPEND_LABEL, defaultString);
////
////    }
//
//    /**
//     * 设置附加标签
//     */
//    @Override
//    public void setAppendLabel(
//            String appendLabel) {
//        userConfigs.put(CustOptsConsts.APPEND_LABEL, appendLabel);
//    }
//
//    /**
//     * 取得是否附加为夏令时调整时间
//     */
//    @Override
//    public boolean getAppendAdjust() {
//        return getFromHash(CustOptsConsts.ADJUST_APPEND_TIME, CustOptsConsts.DEF_ADJUST_APPEND);
//    }
//
//    /**
//     * 设置是否附加为夏令时调整时间
//     */
//    @Override
//    public void setAppendAdjust(
//            boolean isAdjust) {
//        userConfigs.put(CustOptsConsts.ADJUST_APPEND_TIME, isAdjust ? Boolean.TRUE : Boolean.FALSE);
//    }
//
//    /**
//     * 取得附加时区
//     */
//    @Override
//    public int getAppendZone() {
//        return getFromHash(CustOptsConsts.APPEND_ZONE, CustOptsConsts.DEF_APPEND_INDEX);
//    }
//
//    /**
//     * 设置附加时区
//     */
//    @Override
//    public void setAppendZone(
//            int appendZoneIndex) {
//        userConfigs.put(CustOptsConsts.APPEND_ZONE, Integer.toString(appendZoneIndex));
//    }
//
//    /* -------------------------------------------------------- */
//    // +++++++++++++ 联 系 人 选 项 卡 ++++++++++++++ //
//    /**
//     * 取得默认全名排序
//     */
//    @Override
//    public int getAllNameOrder() {
//        return getFromHash(CustOptsConsts.ALL_NAME_ORDER, CustOptsConsts.DEF_NAME_INDEX);
//    }
//
//    /**
//     * 设置默认全名排序
//     */
//    @Override
//    public void setAllNameOrder(
//            int orderIndex) {
//        userConfigs.put(CustOptsConsts.ALL_NAME_ORDER, Integer.toString(orderIndex));
//    }
//
//    /**
//     * 取得默认表示为顺序
//     */
//    @Override
//    public int getDisplayAsOrder() {
//        return getFromHash(CustOptsConsts.DISPLAY_AS_ORDER, CustOptsConsts.DEF_DISPLAY_AS);
//    }
//
//    /**
//     * 设置默认表示为顺序
//     */
//    @Override
//    public void setDisplayAsOrder(
//            int orderIndex) {
//        userConfigs.put(CustOptsConsts.DISPLAY_AS_ORDER, Integer.toString(orderIndex));
//    }
//
//    /**
//     * 取得是否自动加入要答复的人
//     */
//    @Override
//    public boolean getAutoAddToAnswer() {
//        return getFromHash(CustOptsConsts.AUTO_ADD_ANSWER, CustOptsConsts.DEF_ADD_ANSWER);
//    }
//
//    /**
//     * 设置是否自动加入要答复的人
//     */
//    @Override
//    public void setAutoAddToAnswer(
//            boolean isAdd) {
//        userConfigs.put(CustOptsConsts.AUTO_ADD_ANSWER, isAdd ? Boolean.TRUE : Boolean.FALSE);
//    }
//
//    /**
//     * 取得是否发送邮件是作为Card发送
//     */
//    @Override
//    public boolean getSendCard() {
//        return getFromHash(CustOptsConsts.SEND_AS_CARD, CustOptsConsts.DEF_SEND_CARD);
//    }
//
//    /**
//     * 设置是否发送邮件是作为Card发送
//     */
//    @Override
//    public void setSendCard(
//            boolean isSend) {
//        userConfigs.put(CustOptsConsts.SEND_AS_CARD, isSend ? Boolean.TRUE : Boolean.FALSE);
//    }
//
//    /* -------------------------------------------------------- */
//    // +++++++++++++ 任 务 选 项 卡 ++++++++++++++ //
//    /**
//     * 取得过期任务
//     */
//    @Override
//    public int getOverdueTask() {
//        return getFromHash(CustOptsConsts.OVERDUE_TASK);
//    }
//
//    /**
//     * 设置过期任务
//     */
//    @Override
//    public void setOverdueTask(
//            int taskIndex) {
//        userConfigs.put(CustOptsConsts.OVERDUE_TASK, Integer.toString(taskIndex));
//    }
//
//    /**
//     * 取得已完成任务
//     */
//    @Override
//    public int getFinishedTask() {
//        return getFromHash(CustOptsConsts.FINISHED_TASK);
//    }
//
//    /** 设置已完成任务 */
//    @Override
//    public void setFinishedTask(
//            int taskIndex) {
//        userConfigs.put(CustOptsConsts.FINISHED_TASK, Integer.toString(taskIndex));
//    }
//
//    /** 是否给带有截至日期的任务设置提醒 */
//    @Override
//    public boolean getEndDate() {
//        return getFromHash(CustOptsConsts.CONFIG_REMIND, CustOptsConsts.DEF_CONFIG_REMIND);
//    }
//
//    /** 设置给带有截至日期的任务设置提醒 */
//    @Override
//    public void setEndDate(
//            boolean isRemind) {
//        userConfigs.put(CustOptsConsts.CONFIG_REMIND, isRemind ? Boolean.TRUE : Boolean.FALSE);
//    }
//
//    /**
//     * 取得提醒时间
//     */
//    @Override
//    public int getRemindTime() {
//        return getFromHash(CustOptsConsts.REMIND_TIME);
//    }
//
//    /** 设置提醒时间 */
//    @Override
//    public void setRemindTime(
//            int timeIndex) {
//        userConfigs.put(CustOptsConsts.REMIND_TIME, Integer.toString(timeIndex));
//    }
//
//    /** 取得是否在任务列表保留更新副本 */
//    @Override
//    public boolean getSaveUpdateCopy() {
//        return getFromHash(CustOptsConsts.SAVE_TASK_COPY, CustOptsConsts.DEF_SAVE_TAST_COPY);
//    }
//
//    /** 设置是否在任务列表保留更新副本 */
//    @Override
//    public void setSaveUpdateCopy(
//            boolean isSaveUpdateCopy) {
//        userConfigs.put(CustOptsConsts.SAVE_TASK_COPY, isSaveUpdateCopy ? Boolean.TRUE : Boolean.FALSE);
//    }
//
//    /** 取得是否分配的任务完成后发送状态报告 */
//    @Override
//    public boolean getSendStaticReport() {
//        return getFromHash(CustOptsConsts.SEND_STATIC_REPORT, CustOptsConsts.DEF_SEND_REPORT);
//    }
//
//    /** 设置是否分配的任务完成后发送状态报告 */
//    @Override
//    public void setSendStaticReport(
//            boolean isSend) {
//        userConfigs.put(CustOptsConsts.SEND_STATIC_REPORT, isSend ? Boolean.TRUE : Boolean.FALSE);
//    }
//
//    /* -------------------------------------------------------- */
//    // +++++++++++++ 日 记 选 项 卡 ++++++++++++++ //
//    /** 取得字形 */
//    @Override
//    public int getFontStyle() {
//        return getFromHash(CustOptsConsts.FONT_STYLE);
//    }
//
//    /** 设置字形 */
//    @Override
//    public void setFontStyle(
//            int styleIndex) {
//        userConfigs.put(CustOptsConsts.FONT_STYLE, Integer.toString(styleIndex));
//    }
//
//    /** 取得字体颜色 */
//    @Override
//    public int getFontColor() {
//        return getFromHash(CustOptsConsts.FONT_COLOR);
//    }
//
//    /** 设置字体颜色 */
//    @Override
//    public void setFontColor(
//            int colorIndex) {
//        userConfigs.put(CustOptsConsts.FONT_COLOR, Integer.toString(colorIndex));
//    }
//
//    /**
//     * 取得背景
//     */
//    @Override
//    public int getBackColor() {
//        return getFromHash(CustOptsConsts.BACK_COLOR);
//    }
//
//    /**
//     * 设置背景
//     */
//    @Override
//    public void setBackColor(
//            int colorIndex) {
//        userConfigs.put(CustOptsConsts.BACK_COLOR, Integer.toString(colorIndex));
//    }
//
//    /**
//     * 取得字号
//     */
//    @Override
//    public int getFontSize() {
//        int tSize = getFromHash(CustOptsConsts.FONT_SIZE);
//        return tSize == 0 ? 16 : tSize;
//    }
//
//    /**
//     * 设置字号
//     */
//    @Override
//    public void setFontSize(
//            int fontSizeIndex) {
//        userConfigs.put(CustOptsConsts.FONT_SIZE, Integer.toString(fontSizeIndex));
//    }
//
//    /**
//     * 取得当前密码
//     */
//    @Override
//    public String getCurrsorPassword() {
//        return getFromHash(CustOptsConsts.CURRSOR_PASSWORD, CASUtility.EMPTYSTR);
//    }
//
//    /**
//     * 设置当前密码
//     */
//    @Override
//    public void setCurrsorPassword(
//            String password) {
//        userConfigs.put(CustOptsConsts.CURRSOR_PASSWORD, password);
//    }
//
//    /* -------------------------------------------------------- */
//    // +++++++++++++ 其 他 选 项 卡 ++++++++++++++ //
//    /**
//     * 取得是否退出时清空已删除邮件
//     */
//    @Override
//    public boolean getClearHaveDelMail() {
//        return getFromHash(CustOptsConsts.OUT_CLEAR_DELETED_MAIL, CustOptsConsts.DEF_CLEAR);
//    }
//
//    /**
//     * 设置是否退出时清空已删除邮件
//     */
//    @Override
//    public void setClearHaveDelMail(
//            boolean isDel) {
//        userConfigs.put(CustOptsConsts.OUT_CLEAR_DELETED_MAIL, isDel ? Boolean.TRUE : Boolean.FALSE);
//    }
//
//    /**
//     * 取得定位到
//     */
//    @Override
//    public int getGotoPos() {
//        return getFromHash(CustOptsConsts.GOTO);
//    }
//
//    /**
//     * 设置定位
//     */
//    @Override
//    public void setGotoPos(
//            int posIndex) {
//        userConfigs.put(CustOptsConsts.GOTO, Integer.toString(posIndex));
//    }
//
//    /**
//     * 取得是否永久删除项目前提出警告
//     */
//    @Override
//    public boolean getDelWarning() {
//        return getFromHash(CustOptsConsts.WARNING, CustOptsConsts.DEF_WARNING);
//    }
//
//    /**
//     * 设置是否永久删除项目前提出警告
//     */
//    @Override
//    public void setDelWarning(
//            boolean isDel) {
//        userConfigs.put(CustOptsConsts.WARNING, isDel ? Boolean.TRUE : Boolean.FALSE);
//    }
//
//    /**
//     * 取得存档路径
//     */
//    @Override
//    public String getSavePath() {
//        return getFromHash(CustOptsConsts.SAVE_PATH, CustOptsConsts.DEF_PATH);
//    }
//
//    /**
//     * 设置存档路径
//     */
//    @Override
//    public void setSavePath(
//            String savePath) {
//        userConfigs.put(CustOptsConsts.SAVE_PATH, savePath);
//    }
//
//    /**
//     * 取得是否在预览窗口中将邮件标记为已读
//     */
//    @Override
//    public boolean getSginHaveRead() {
//        return getFromHash(CustOptsConsts.SGIN_HAVE_READ, CustOptsConsts.DEF_SGIN_READ);
//    }
//
//    /**
//     * 设置是否在预览窗口中将邮件标记为已读
//     */
//    @Override
//    public void setSginHaveRead(
//            boolean isSgin) {
//        userConfigs.put(CustOptsConsts.SGIN_HAVE_READ, isSgin ? Boolean.TRUE : Boolean.FALSE);
//    }
//
//    /**
//     * 取得等待时间
//     */
//    @Override
//    public int getWaitTime() {
//        return getFromHash(CustOptsConsts.WAIT_TIME, CustOptsConsts.DEF_WAIT_TIME);
//    }
//
//    /**
//     * 设置等待时间
//     */
//    @Override
//    public void setWaitTime(
//            int waitTime) {
//        userConfigs.put(CustOptsConsts.WAIT_TIME, Integer.toString(waitTime));
//    }
//
//    /**
//     * 取得当选定内容更改时将项目标记为已读
//     */
//    @Override
//    public boolean getSginHaveReadWhenSelected() {
//        return getFromHash(CustOptsConsts.SGIN_HAVE_READ_WHEN_SELECTED, CustOptsConsts.DEF_SGIN_READ_SELECTED);
//    }
//
//    /**
//     * 设置当选定内容更改时将项目标记为已读
//     */
//    @Override
//    public void setSginHaveReadWhenSelected(
//            boolean isSelected) {
//        userConfigs.put(CustOptsConsts.SGIN_HAVE_READ_WHEN_SELECTED, isSelected ? Boolean.TRUE : Boolean.FALSE);
//    }
//
//    // ------
//
//    /**
//     *
//     */
//    private String parseZoneString(
//            String zoneString) {
//        int signIndex = zoneString.indexOf(") ") + 1;
//        String newString = zoneString.substring(signIndex, zoneString.length());
//        int signPosIndex = newString.indexOf(",");
//        if (signPosIndex != -1) {
//            return newString.substring(0, signPosIndex);
//        } else {
//            return newString;
//        }
//    }
//
//    /* -------------------------------------------------------- */
//    // +++++++++++++ 查找对话盒 ++++++++++++++ //
//    /**
//     * 取得查找文字的历史
//     */
//    @Override
//    public String getFindHistory() {
//        return getFromHash(CustOptsConsts.FIND_HISTORY, CASUtility.EMPTYSTR);
//    }
//
//    /**
//     * 保存查找文字的历史
//     */
//    @Override
//    public void setFindHistory(
//            String findHistory) {
//        userConfigs.put(CustOptsConsts.FIND_HISTORY, findHistory);
//    }
//
//    /**
//     * @called by:EFolderTree;
//     * @return
//     */
//    @Override
//    public int getLastUpdtedMonth() {
//        return getFromHash(CustOptsConsts.LASTUPDATEMONTH, 0);
//    }
//
//    /**
//     * @called by:EFolderTree;
//     * @return
//     */
//    @Override
//    public void setLastUpdtedMonth(
//            int prmLastUpdatedMonth) {
//        userConfigs.put(CustOptsConsts.LASTUPDATEMONTH, Integer.toString(prmLastUpdatedMonth));
//    }
//
//    /**
//     * 获取信件的打开方式
//     */
//    @Override
//    public boolean getOpenType() {
//        Object value = userConfigs.get(CustOptsConsts.OPEN_TYPE);
//        return value == null ? false : (Boolean.valueOf((String) value)).booleanValue();
//    }
//
//    /**
//     * 设置邮件打开方式（用PIM打开还是用系统默认的邮件客户端打开）
//     */
//    @Override
//    public void setOpenType(
//            boolean openType) {
//        userConfigs.put(CustOptsConsts.OPEN_TYPE, Boolean.toString(openType));
//    }
//
//    @Override
//    public void setSplitHeightScale(
//            double scale) {
//        if (scale > 0 && scale < 1)
//            userConfigs.put(CustOptsConsts.SPLIT_SCALE, Double.toString(scale));
//    }
//
//    @Override
//    public double getSplitHeightScale() {
//        Object value = userConfigs.get(CustOptsConsts.SPLIT_SCALE);
//        return value == null ? 0.50d : (Double.valueOf((String) value)).doubleValue();
//    }

    // 返回MainPane上面至面板的缩进信息。
    @Override
    public double getContentX() {
        Object value = userConfigs.get(CustOptsConsts.CONTENT_POSITION_X);
        return value == null ? 0.025d : (Double.valueOf((String) value)).doubleValue();
    }

    @Override
    public double getContentY() {
        Object value = userConfigs.get(CustOptsConsts.CONTENT_POSITION_Y);
        return value == null ? 0.025d : (Double.valueOf((String) value)).doubleValue();
    }

    @Override
    public double getContentWidth() {
        Object value = userConfigs.get(CustOptsConsts.CONTENT_WIDTH);
        return value == null ? 0.95d : (Double.valueOf((String) value)).doubleValue();
    }

    @Override
    public double getContentHeight() {
        Object value = userConfigs.get(CustOptsConsts.CONTENT_HEIGHT);
        return value == null ? 0.95d : (Double.valueOf((String) value)).doubleValue();
    }

    // 设置MainPane上面至面板的缩进信息。
    @Override
    public void setContentX(
            double scale) {
        if (scale > 0 && scale < 1)
            userConfigs.put(CustOptsConsts.CONTENT_POSITION_X, Double.toString(scale));
    }

    @Override
    public void setContentY(
            double scale) {
        if (scale > 0 && scale < 1)
            userConfigs.put(CustOptsConsts.CONTENT_POSITION_Y, Double.toString(scale));
    }

    @Override
    public void setContentWidth(
            double scale) {
        if (scale > 0 && scale < 1)
            userConfigs.put(CustOptsConsts.CONTENT_WIDTH, Double.toString(scale));
    }

    @Override
    public void setContentHeight(
            double scale) {
        if (scale > 0 && scale < 1)
            userConfigs.put(CustOptsConsts.CONTENT_HEIGHT, Double.toString(scale));
    }

    @Override
    public String getInstalledFunctions() {
        return systemConfigs.get(APP_CAPTIONS) == null ? "" : (String) systemConfigs.get(APP_CAPTIONS);
    }

    @Override
    public String getInstalledAppNames() {
        return systemConfigs.get(APP_NAMES) == null ? "" : (String) systemConfigs.get(APP_NAMES);
    }
//
//    @Override
//    public int getFIRST_WEEK_DAY() {
//        int tmpIndex;
//        try {
//            tmpIndex = Integer.parseInt((String) userConfigs.get(FIRST_WEEK_DAY));
//        } catch (Exception e) {
//            tmpIndex = 0;
//        }
//        return tmpIndex;
//    }
//
//    @Override
//    public int getWORK_WEEK_DAY() {
//        int tmpIndex;
//        try {
//            tmpIndex = Integer.parseInt((String) userConfigs.get(WORK_WEEK_DAY));
//        } catch (Exception e) {
//            tmpIndex = 0;
//        }
//        return tmpIndex;
//    }
//
//    @Override
//    public int getFIRST_YEAR_WEEK() {
//        int tmpIndex;
//        try {
//            tmpIndex = Integer.parseInt((String) userConfigs.get(FIRST_YEAR_WEEK));
//        } catch (Exception e) {
//            tmpIndex = 0;
//        }
//        return tmpIndex;
//    }

//    /**
//     * TODO:因该改为int型。布尔值与String转换时（存盘时）可能会麻烦。
//     */
//    @Override
//    public boolean isWEEK_NUMBER_VISIBLE() {
//        String tmpNumberVisible = (String) userConfigs.get(WEEK_NUMBER_VISIBLE);
//        if (tmpNumberVisible == null) {
//            tmpNumberVisible = PIMPool.BOOLEAN_FALSE;
//            userConfigs.put(WEEK_NUMBER_VISIBLE, tmpNumberVisible);
//        }
//        return tmpNumberVisible.equals(PIMPool.BOOLEAN_TRUE);
//    }

    @Override
    public boolean getDeleteOriginalTransaction() {
        Object value = userConfigs.get(CustOptsConsts.Delete_Original_Transaction);
        return value == null ? false : (Boolean.valueOf((String) value)).booleanValue();
    }

//    @Override
//    public Vector getMenuText(
//            String prmText) {
//        Object tmpResult = MenuTextVec.get(prmText);
//        if (tmpResult == null) {
//            String tmpText = (String) systemConfigs.get(prmText); // 并以之（tmpText）为Key得到Config2中对应的子菜单字串。
//            if (tmpText == null) {
//                // ErrorUtil.write("find no Content match the key" + tmpText);
//                return null;
//            }
//            tmpResult = CASUtility.parserStrToVec(tmpText, CustOpts.BIAS, tmpText.length()); // 解析存于vector中。
//            MenuTextVec.put(prmText, tmpResult);
//        }
//        return (Vector) tmpResult;
//    }
//
//    @Override
//    public Vector getMenuMnem(
//            String prmText) {
//        Object tmpResult = MenuMnemVec.get(prmText);
//        if (tmpResult == null) {
//            String tmpText = (String) systemConfigs.get(prmText.concat("_Mnemonic")); // 得到Config2中的对应的助记符字串。
//            if (tmpText == null) {
//                ErrorUtil.write("find no Content match the key" + tmpText);
//                return null;
//            }
//            tmpResult = CASUtility.parserStrToVec(tmpText, CustOpts.BIAS, tmpText.length());// 解析后存与Vector中。
//            MenuMnemVec.put(prmText, tmpResult);
//        }
//        return (Vector) tmpResult;
//    }
//
//    @Override
//    public Vector getActionName(
//            String prmText) {
//        Object tmpResult = MenuFlagVec.get(prmText);
//        if (tmpResult == null) {
//            String tmpText = (String) systemConfigs.get(prmText.concat("_ActionID")); // 得到Config2中的对应的助记符字串。
//            if (tmpText == null) {
//                L.e("FileCustomOptions","find no Content match the key" + tmpText, null);
//                return null;
//            }
//            tmpResult = CASUtility.parserStrToVec(tmpText, CustOpts.BIAS, tmpText.length());// 解析后存与Vector中。
//            MenuFlagVec.put(prmText, tmpResult);
//        }
//        return (Vector) tmpResult;
//    }

    /**
     * 保存全部PIM数据。
     *
     * @called by:emo.pim.PIMControl;
     */
    @Override
    public void saveData() {
        new DefaultConfigInfoWriter().saveData(userConfigs);
    }

    // --------------------------------------------------------------------------
    // 避免从Hash中直接取值，取值采用以下的方法
    /*
     * 根据key从hash中取得相应的int值。
     * @param key hash中的key值。
     * @return 相应的int值，缺省值为0
     */
    private int getFromHash(
            String key) {
        return getFromHash(key, 0);
    }

    /*
     * 根据key从hash中取得相应的int值。
     * @param key hash中的key值。
     * @param defaultValue 若取不到返回的缺省值
     * @return 相应的int值
     */
    private int getFromHash(
            Object key,
            int defaultValue) {
        int tmpIndex;
        try {
            tmpIndex = Integer.parseInt((String) userConfigs.get(key));
        } catch (Exception e) {
            tmpIndex = defaultValue;
        }
        return tmpIndex;
    }

    /*
     * 根据key从hash中取得相应的字符串。
     * @param key hash中的key值。
     * @param defaultValue 若取不到返回的缺省值
     * @return 相应的字符串
     */
    private String getFromHash(
            Object key,
            String defaultValue) {
        String tmpValue = (String) userConfigs.get(key);
        if (tmpValue == null)
            tmpValue = defaultValue;
        return tmpValue;
    }

    /*
     * 根据key从hash中取得相应的boolean值。
     * @param key hash中的key值。
     * @param defaultValue 若取不到返回的缺省值
     * @return 相应的boolean值
     */
    private boolean getFromHash(
            String key,
            boolean defaultValue) {
        boolean tmp = defaultValue;
        Object value = userConfigs.get(key);
        if (value != null) {
            if (value instanceof String)
                tmp = ((String) value).equals(PIMPool.BOOLEAN_TRUE);
            else if (value instanceof Boolean)
                tmp = ((Boolean) value).booleanValue();
        }
        return tmp;
    }

    // variables-----------------------------------------------------------------
    private HashMap MenuTextVec = new HashMap();
    private HashMap MenuMnemVec = new HashMap();
    private HashMap MenuFlagVec = new HashMap();

    @Override
    public void setUserLang(int prmUserLang) {
        userConfigs.put(CustOptsConsts.USERNAME, Integer.toString(prmUserLang));
    }

    @Override
    public int getUserLang() {
        Object tObj = userConfigs.get(CustOptsConsts.USERNAME);
        return tObj != null ? Integer.valueOf((String) tObj) : 0;
    }

    @Override
    public String getDBPort() {
        String port = (String) userConfigs.get(CustOptsConsts.DBPORT);
        if(port == null || port.trim().length() == 0)
            return "1214";
        try {
            int p =Integer.parseInt(port);
            if(p < 100 || p > 65535)
                return "1214";
        }catch(Exception e) {
            return "1214";
        }
        return port;
    }

    @Override
    public String getDBHost() {
        String host = (String) userConfigs.get(CustOptsConsts.DBHOST);
        if(host == null || host.trim().length() == 0) {
            host = "localhost";
        }
        return host;
    }

}
