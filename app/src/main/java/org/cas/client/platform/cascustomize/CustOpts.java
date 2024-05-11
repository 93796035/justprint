package org.cas.client.platform.cascustomize;

import android.content.Context;

import com.just.print.app.BarFrame;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

/**
 * <p>
 * 
 * @TODO:增加图片选择规律－－安装时要安装尺寸不等的各种图片，使用时先判断本地机器的分辨率，如果时高分辨率， 则应该使用较大的图片，如果分辨率低，则使用较小的分辨率。因为经实验：打开硬盘上的96K的jpg文件，消耗内存为2609k
 *                                                         打开尺寸小的jpg文件，内存消耗相应减少，反之也反：107k的jpg文件，打开消耗内存3M以上。
 */

public abstract class CustOpts {
    public static int OS;
    public static final int MAC = 2;
    public static final int LINUX = 1;
    public static final int WINDOWS = 0;

    public static final char BIAS = '/';
    public static final int WEEK_ONE_DAY = 0;
    /** The first week is from the first day of a year */
    public static final int WEEK_FOUR_DAY = WEEK_ONE_DAY + 1;
    /** The first week is from the four days of a year */
    public static final int WEEK_FULL_DAY = WEEK_FOUR_DAY + 1;
    /** The first week is from the seven days of a year */

    public static final int DIALOG_DELAY_TIME = 200; // ms
    public static final int MAX_FOR_SHOWING_DIALOG = 10;

    public static int SCRWIDTH;
    public static int SCRHEIGHT;
    public static int SIZE_EDGE;
    public static int SIZE_TITLE;
    public static int VER_GAP;
    public static int HOR_GAP;
    public static int BTN_WIDTH_NUM;
    public static int BTN_WIDTH;
    public static int BTN_HEIGHT;
    public static int BAR_HEIGHT;
    public static int LBL_HEIGHT;
    public static int SEP_HEIGHT;
    Context context;
    public static CustOpts custOps = new FileCustomOptions();
    public Hashtable userConfigs = new HashtableToString();
    public Hashtable systemConfigs = new HashtableToString();

    protected CustOpts() {
        SIZE_EDGE = 4; // 应该根据系统作调整。
        SIZE_TITLE = 40;
        VER_GAP = 6;
        HOR_GAP = 8;
        BTN_WIDTH_NUM = 100;
        BTN_WIDTH = 74;
        BTN_HEIGHT = 22;// 应验表名：按钮、TextField等组件的高度不能小于22，否则显得太小了。
        BAR_HEIGHT = 25;// 工具条25，否则显得太小了。
        LBL_HEIGHT = 20;// 当Lable和组件组合显示时(即setLableFor的时候)高度为20，和其它组件并排显示，或单独显示时，其占位应该用BTN_HEIGHT。
        SEP_HEIGHT = 10;
    }

    // TODO：临时的密码机制，存放于Config文件中，将来改为存到数据库中。
    public abstract String getUserName();

    public abstract void setUserName(
            String prmUserName);

    public abstract String getUserType();

    public abstract void setUserType(
            String prmUserType);
    
    public abstract void setUserLang(
            int prmUserLang);
    
    public abstract int getUserLang();
    
    /******************************************************************************************************/
    /* 主界面公共部分 */
    /******************************************************************************************************/

    // 安装路径===============================================================
    public abstract void setInstallPath(
            String path);

    public abstract String getInstallPath();

    // 基础方法===============================================================
    public abstract Enumeration getKeys();

    public abstract Object getValue(
            Object prmKey);

    public abstract void setKeyAndValue(
            Object prmKey,
            Object prmValue);

    public abstract int getANewFolderID(); // 每次新建一个文件夹，系统要赋予一个不重复的ID值。
    // Abstract methods 主窗体的大小和位置======================================

    public abstract int getFrameX(); // @called by:emo.net.PIMMain.java;

    public abstract int getFrameY(); // @called by:emo.net.PIMMain.java;

    public abstract int getFrameWidth(); // @called by:emo.net.PIMMain.java;
    // 返回MainPane上面至面板的缩进信息==========================================

    public abstract double getContentX();

    public abstract double getContentY();

    public abstract double getContentWidth();

    public abstract double getContentHeight();

//    /** @called by:emo.net.PIMMain.java; */
//    public abstract int getFrameState();

    /** @called by:emo.net.PIMMain.java; */
    public abstract void setFrameState(
            int prmState);

    /** @called by:emo.net.PIMMain.java; */
    public abstract int getFrameHeight();

    /** @called by:emo.net.PIMMain.java; */
    public abstract void setFrameX(
            int prmX);

    /** @called by:emo.net.PIMMain.java; */
    public abstract void setFrameY(
            int prmY);

    /** @called by:emo.net.PIMMain.java; */
    public abstract void setFrameWidth(
            int prmWidth);

    /** @called by:emo.net.PIMMain.java; */
    public abstract void setFrameHeight(
            int prmHeight);

    // 设置MainPane上面至面板的缩进信息。
    public abstract void setContentX(
            double prmX);

    public abstract void setContentY(
            double prmY);

    public abstract void setContentWidth(
            double prmWidth);

    public abstract void setContentHeight(
            double prmHeight);

    // 主面板的图像==========================================================================
    public String getFrameLogoImage() {
        // todo：取出Cell中的用户设置。
        // 判断是否为空
        // 为空则取出检查当前的风格设置。
        // 判断是否为空
        // 为空则赋返回null，调用着发现没有路径返回，将赋予主界面单纯的颜色背景。
        return "logo.png";
    }

    public String getFrameLogoIcon() {
        return "pim16.gif";
    }

    public String getTickIcon() {
        return "Tick.gif";
    }

    public String getMoreIcon() {
        return "More.gif";
    }

    public String getMainPaneBGImg() {
        Object tIdx = userConfigs.get(CustOptsConsts.MainBGIndex);
        if (tIdx == null)
            tIdx = "0";
        return "MainBack".concat((String) tIdx).concat(".jpg");
    }
//    public Image getTodayTitleIcon() {
//        return PIMPool.pool.getImage(PaneConsts.IAMGE_PATH.concat("activitiestoday.gif"));
//    }
//
//    public Image getAllDayEventIcon() {
//        return PIMPool.pool.getImage(PaneConsts.IAMGE_PATH.concat("alldayevent.gif"));
//    }
//
//    public Icon getImapItemIcon(
//            boolean prmBigIcon) {
//        return PIMPool.pool.getIcon(PaneConsts.IAMGE_PATH.concat((prmBigIcon ? "imapfolder1.gif" : "imapfolder.gif")));
//    }
//
//    /**
//     * @return Image:书本视图中夹板上的别针的图片，这种别针两端带黑洞洞，因为是别在活页纸上的。
//     * @see java.awt.Image
//     */
//    public Image getClampImage() {
//        return PIMPool.pool
//                .getImage(PaneConsts.IAMGE_PATH.concat(isViewTopAndDown() ? "clamp32_2.gif" : "clamp32.gif"));
//    }
//
//    /**
//     * @return Image：书本视图中夹板上的别针的图片，这种别针两端不带黑洞洞，因为是别在封面纸板上的。
//     * @see java.awt.Image
//     */
//    public Image getClampNoHoleImage() {
//        // todo：取出Cell中的用户设置。
//        // 判断是否为空
//        // 为空则取出检查当前的风格设置。
//        // 判断是否为空
//        // 为空则赋给默认值(49,97,90)一种难看的绿色，可能因该改的更好看些或用图片背景。
//        return PIMPool.pool.getImage(PaneConsts.IAMGE_PATH.concat(isViewTopAndDown() ? "clampNoHole_2.gif"
//                : "clampNoHole.gif"));
//    }
//
//    /**
//     * @return Image：书本视图中夹板上的上端装饰物的图片
//     * @see java.awt.Image
//     */
//    public Image getClampTopImage() {
//        // todo：取出Cell中的用户设置。
//        // 判断是否为空
//        // 为空则取出检查当前的风格设置。
//        // 判断是否为空
//        // 为空则赋给默认值(49,97,90)一种难看的绿色，可能因该改的更好看些或用图片背景。
//        return PIMPool.pool.getImage(PaneConsts.IAMGE_PATH.concat(isViewTopAndDown() ? "clampTop32_2.gif"
//                : "clampTop32.gif"));
//    }
//
//    /**
//     * @return Image：书本视图中夹板上的下端装饰物的图片
//     * @see java.awt.Image
//     */
//    public Image getClampEndImage() {
//        // todo：取出Cell中的用户设置。
//        // 判断是否为空
//        // 为空则取出检查当前的风格设置。
//        // 判断是否为空
//        // 为空则赋给默认值(49,97,90)一种难看的绿色，可能因该改的更好看些或用图片背景。
//        return PIMPool.pool.getImage(PaneConsts.IAMGE_PATH.concat(isViewTopAndDown() ? "clampEnd32_2.gif"
//                : "clampEnd32.gif"));
//    }
//
//    // 导航面板的背景色================================================
//    public Image getNavigationPaneImage() {
//        return PIMPool.pool.getImage(PaneConsts.IAMGE_PATH.concat("NavigationBK.jpg"));
//    }
//
//    public Color getNavigationPaneBG() {
//        // return getContentBG();
//        return PIMPool.pool.getColor(238, 238, 238);
//        // return getTableViewBG();
//    }
//
//    public Color getContentBG() {
//        //light blue //return new Color(205, 233, 247);
//        // return new Color(255,250,170);
//        // return PIMPool.pool.getColor(255, 255, 255);
//    	return PIMPool.pool.getColor(246, 246, 246);
//    }
//
//    public Color getTableBG() {
//        return Color.WHITE;
//    }
//
//    /** * @called by: PIMTable; */
//    public Color getGridColor() {
//        return PIMPool.pool.getColor(204, 204, 204);
//    }
//
//    // 表格中被选中行的颜色：================================================
//    /** * @called by: PIMTable; */
//    public Color getSelectedBackColor() {
//        return PIMPool.pool.getColor(184, 207, 229);
//    }
//
//    /**
//     * 被选中行的被选中Cell的背景色。
//     *
//     * @called by: PIMTable;
//     */
//    public Color getSelectedCellColor() {
//        return PIMPool.pool.getColor(255, 255, 204);
//    }

    // =============================================================================
    // 八大功能的图片=========================================================================================================
    // /**
    // * @return Icon
    // * @see javax.swing.Icon
    // */
    // private Image getCalendarImage(boolean prmIsBig)
    // {
    // //todo：取出Cell中的用户设置。
    // //判断是否为空
    // //为空则取出检查当前的风格设置。
    // //判断是否为空
    // //为空则赋给默认值(49,97,90)一种难看的绿色，可能因该改的更好看些或用图片背景。
    // return PIMPool.pool.getImage(PaneConstant.IAMGE_PATH.concat((prmIsBig ? "calendar32.gif" : "calendar16.gif")));
    // }
//    public abstract int getLastUpdtedMonth();
//
//    public abstract void setLastUpdtedMonth(
//            int prmLastUpdatedMonth);

    // 有关数据库表的操作========================================================================================================
    /** 获取数据库所需的表名包括插件应用的表和系统表（如ViewInfo）。可能是供版本检查、兼容时用。 */
    public abstract String[] getTableNames();

    /** 向文件中写出数据库表名 */
    public abstract void setSysTableNames(
            String[] tableNames);

    // /**
    // * 向文件中写出各主应用的删除项表名
    // * @param tableNames
    // */
    // public abstract void setRecycleBinTableNames(String[] tableNames);

    // /**
    // * 获取各应用的删除项的表名
    // * =============================================
    // * 数据库初始化时删除项的表名由DefaultDBInfo.java提供
    // * 在表创建完后保存到文件中，以后用时由文件中获取
    // * 同时表名有变化的时有反映在文件中
    // * @see
    // * 删除项表的添加整合进主应用表的添加方法中去，同理
    // * 移除时也放入主应用表的移除中，两者是联动的
    // * =============================================
    // * @return
    // */
    // public abstract String[] getRecycleBinTableNames();

    // 当前的应用类型，及当前每一个应用的视图类型=====================================================================================
    /** @called by:EVerticalTabbedPane; PIMControl; */
    public abstract int getActiveAppType();

    /** * @called by: emo.pim.PIMControl; */
    public abstract void setActiveAppType(
            int prmIndex);

//    /** 取得当前选择的路径 */
//    public abstract int getActivePathID();
//
//    /** 设置当前选择的路径 */
//    public abstract void setActivePathID(
//            int prmPathID);
//
//    /** 准备此方法用来在查找文件夹该对应哪种视图时使用。 */
//    public abstract int getNodeID(
//            String prmPathName);
//
    /** 清除键值对 */
    public abstract void removeKeyAndValue(
            Object prmKey);
//
//    /******************************************************************************************************************************/
//    /* 约会 */
//    /******************************************************************************************************************************/
//
//    /** * 约会图标 */
//    public Icon getAppointmentIcon() {
//        return PIMPool.pool.getIcon(PaneConsts.IAMGE_PATH.concat("appointment.png"));
//    }
//
//    // 八大功能的图标-------------------------------------------------------------
//    /**
//     * @return Icon：日历的Icon
//     * @see javax.swing.Icon
//     */
//    public Icon getCalendarIcon(
//            boolean prmBigIcon) {
//        // todo：取出Cell中的用户设置。
//        // 判断是否为空
//        // 为空则取出检查当前的风格设置。
//        // 判断是否为空
//        // 为空则赋给默认值(49,97,90)一种难看的绿色，可能因该改的更好看些或用图片背景。
//        return PIMPool.pool.getIcon(PaneConsts.IAMGE_PATH.concat((prmBigIcon ? "calendar32.png" : "calendar16.png")));
//    }
//
//    /******************************************************************************************************************************/
//    /* 任务 */
//    /******************************************************************************************************************************/
//    /**
//     * @return Icon：任务的Icon
//     * @see javax.swing.Icon
//     */
//    public Icon getTaskIcon(
//            boolean prmBigIcon) {
//        // todo：取出Cell中的用户设置。
//        // 判断是否为空
//        // 为空则取出检查当前的风格设置。
//        // 判断是否为空
//        // 为空则赋给默认值(49,97,90)一种难看的绿色，可能因该改的更好看些或用图片背景。
//        return PIMPool.pool.getIcon(PaneConsts.IAMGE_PATH.concat((prmBigIcon ? "tasks32.png" : "tasks16.png")));
//    }
//
//    /******************************************************************************************************************************/
//    /* 联系人 */
//    /******************************************************************************************************************************/
//    /**
//     * @return Icon：联系人的Icon
//     * @see javax.swing.Icon
//     */
//    public Icon getContactsIcon(
//            boolean prmBigIcon) {
//        // todo：取出Cell中的用户设置。
//        // 判断是否为空
//        // 为空则取出检查当前的风格设置。
//        // 判断是否为空
//        // 为空则赋给默认值(49,97,90)一种难看的绿色，可能因该改的更好看些或用图片背景。
//        return PIMPool.pool.getIcon(PaneConsts.IAMGE_PATH.concat((prmBigIcon ? "addNewMember.gif" : "contacts16.png")));
//    }
//
//    /** *属性对话盒调用======================================================== */
//    // 联系人图标
//    public Icon getContactGroupIcon() {
//        return PIMPool.pool.getIcon(PaneConsts.IAMGE_PATH.concat("contactsGroup.png"));
//    }
//
//    /******************************************************************************************************************************/
//    /* 日记 */
//    /******************************************************************************************************************************/
//    /**
//     * @return Icon：日记的Icon
//     * @see javax.swing.Icon
//     */
//    public Icon getDiaryIcon(
//            boolean prmBigIcon) {
//        // todo：取出Cell中的用户设置。
//        // 判断是否为空
//        // 为空则取出检查当前的风格设置。
//        // 判断是否为空
//        // 为空则赋给默认值(49,97,90)一种难看的绿色，可能因该改的更好看些或用图片背景。
//        return PIMPool.pool.getIcon(PaneConsts.IAMGE_PATH.concat((prmBigIcon ? "notes32.png" : "notes16.png")));
//    }
//
//    /**
//     * @return Image
//     * @see java.awt.Image called by: emo.
//     */
//    public Icon getDiarySubIcon() {
//        // todo：取出Cell中的用户设置。
//        // 判断是否为空
//        // 为空则取出检查当前的风格设置。
//        // 判断是否为空
//        // 为空则赋给默认值(49,97,90)一种难看的绿色，可能因该改的更好看些或用图片背景。
//        return PIMPool.pool.getIcon(PaneConsts.IAMGE_PATH.concat("journal16.gif"));
//    }
//
//    /** * @called by: PIMDiarylabel; */
//    public Icon getWeatherIcon(
//            int prmIndex) {
//        return getWeatherIcons()[prmIndex];
//    }
//
//    /** * @called by: PIMDiarylabel; */
//    public Icon[] getWeatherIcons() {
//        if (weatherImgs == null) {
//            weatherImgs = new Icon[3];
//            weatherImgs[0] = PIMPool.pool.getIcon(PaneConsts.IAMGE_PATH.concat("Weather_Sunny.gif"));
//            weatherImgs[1] = PIMPool.pool.getIcon(PaneConsts.IAMGE_PATH.concat("Weather_Cloudy.gif"));
//            weatherImgs[2] = PIMPool.pool.getIcon(PaneConsts.IAMGE_PATH.concat("Weather_Rainy.gif"));
//        }
//        return weatherImgs;
//    }
//
//    /** * @called by: PIMDiarylabel; */
//    public Icon getMoodIcon(
//            int prmIndex) {
//        return getMoodImgs()[prmIndex];
//    }
//
//    /** * @called by: PIMDiarylabel; */
//    public Icon[] getMoodImgs() {
//        if (moodImgs == null) {
//            moodImgs = new Icon[10];
//            moodImgs[0] = PIMPool.pool.getIcon(PaneConsts.IAMGE_PATH.concat("Mood_Pleasure.gif"));
//            moodImgs[1] = PIMPool.pool.getIcon(PaneConsts.IAMGE_PATH.concat("Mood_Wonder.gif"));
//            moodImgs[2] = PIMPool.pool.getIcon(PaneConsts.IAMGE_PATH.concat("Mood_Morose.gif"));
//            moodImgs[3] = PIMPool.pool.getIcon(PaneConsts.IAMGE_PATH.concat("Mood_Angery.gif"));
//            moodImgs[4] = PIMPool.pool.getIcon(PaneConsts.IAMGE_PATH.concat("Mood_Injured.gif"));
//            moodImgs[5] = PIMPool.pool.getIcon(PaneConsts.IAMGE_PATH.concat("Mood_lethargic.gif"));
//            moodImgs[6] = PIMPool.pool.getIcon(PaneConsts.IAMGE_PATH.concat("Mood_Afraid.gif"));
//            moodImgs[7] = PIMPool.pool.getIcon(PaneConsts.IAMGE_PATH.concat("Mood_Shy.gif"));
//            moodImgs[8] = PIMPool.pool.getIcon(PaneConsts.IAMGE_PATH.concat("Mood_Sentimental.gif"));
//            moodImgs[9] = PIMPool.pool.getIcon(PaneConsts.IAMGE_PATH.concat("Mood_Fun.gif"));
//        }
//        return moodImgs;
//    }
//
//    /******************************************************************************************************************************/
//    /* 邮件 */
//    /******************************************************************************************************************************/
//    /**
//     * @return Icon：邮件的Icon：以前在导航面板的树中有这么一个邮件图标， 它带着三个二级字节点：Inbox，OutBox，SendItem。后来规格变了，其字
//     *         节点被提为一级字节点，在PIMMain中本节点被注释掉了。
//     * @see java.awt.Image
//     *
//     *      public Icon getMailIcon(boolean prmBigIcon) { //todo：取出Cell中的用户设置。 //判断是否为空 //为空则取出检查当前的风格设置。 //判断是否为空
//     *      //为空则赋给默认值(49,97,90)一种难看的绿色，可能因该改的更好看些或用图片背景。 return
//     *      PIMPool.pool.getIcon(PaneConstant.IAMGE_PATH.concat((prmBigIcon ? "mail.gif" : "mail.gif")); }
//     */
//
//    /** * 获取信件的打开方式 */
//    public abstract boolean getOpenType();
//
//    /** * 设置邮件打开方式（用PIM打开还是用系统默认的邮件客户端打开） */
//    public abstract void setOpenType(
//            boolean openType);
//
//    /** * @called by: DefaultPIMCellRender; */
//    public Color getReadedMailColor() {
//        return PIMPool.pool.getColor(88, 88, 88);
//    }
//
//    /** 天视图图标 */
//    public Icon getDayIcon() {
//        return PIMPool.pool.getIcon(PaneConsts.IAMGE_PATH.concat("day16.png"));
//    }
//
//    /**
//     * 周视图图标
//     *
//     * @return
//     */
//    public Icon getWeekIcon() {
//        return PIMPool.pool.getIcon(PaneConsts.IAMGE_PATH.concat("workweek16.png"));
//    }
//
//    /**
//     * 月视图图标
//     *
//     * @return
//     */
//    public Icon getMonthIcon() {
//        return PIMPool.pool.getIcon(PaneConsts.IAMGE_PATH.concat("month16.png"));
//    }
//
//    public Icon getAccountIcon() {
//        return PIMPool.pool.getIcon(PaneConsts.IAMGE_PATH.concat("account.gif"));
//    }
//
//    /**
//     * @return Icon：Inbox的Icon.
//     * @see java.awt.Image
//     */
//    public Icon getInBoxIcon(
//            boolean prmBigIcon) {
//        // todo：取出Cell中的用户设置。
//        // 判断是否为空
//        // 为空则取出检查当前的风格设置。
//        // 判断是否为空
//        // 为空则赋给默认值(49,97,90)一种难看的绿色，可能因该改的更好看些或用图片背景。
//        return PIMPool.pool.getIcon(PaneConsts.IAMGE_PATH.concat((prmBigIcon ? "inbox32.png" : "inbox16.png")));
//    }
//
//    /**
//     * @return Icon：Outbox的Icon.
//     * @see java.awt.Image
//     */
//    public Icon getOutBoxIcon(
//            boolean prmBigIcon) {
//        // todo：取出Cell中的用户设置。
//        // 判断是否为空
//        // 为空则取出检查当前的风格设置。
//        // 判断是否为空
//        // 为空则赋给默认值(49,97,90)一种难看的绿色，可能因该改的更好看些或用图片背景。
//        return PIMPool.pool.getIcon(PaneConsts.IAMGE_PATH.concat((prmBigIcon ? "outbox32.png" : "outbox16.png")));
//    }
//
//    public Icon getDraftBoxIcon(
//            boolean prmBigIcon) {
//        return PIMPool.pool.getIcon(PaneConsts.IAMGE_PATH.concat((prmBigIcon ? "draft32.png" : "draft16.png")));
//    }
//
//    public Icon getSendedBoxIcon(
//            boolean prmBigIcon) {
//        // todo：取出Cell中的用户设置。
//        // 判断是否为空
//        // 为空则取出检查当前的风格设置。
//        // 判断是否为空
//        // 为空则赋给默认值(49,97,90)一种难看的绿色，可能因该改的更好看些或用图片背景。
//        return PIMPool.pool.getIcon(PaneConsts.IAMGE_PATH.concat((prmBigIcon ? "sendbox32.png" : "sendbox16.png")));
//    }
//
//    /**
//     * 发送图标
//     *
//     * @return Icon:SendItem的Icon.
//     * @see java.awt.Image
//     */
//    public Icon getSentItemsIcon() {
//        // todo：取出Cell中的用户设置。
//        // 判断是否为空
//        // 为空则取出检查当前的风格设置。
//        // 判断是否为空
//        // 为空则赋给默认值(49,97,90)一种难看的绿色，可能因该改的更好看些或用图片背景。
//        return PIMPool.pool.getIcon(PaneConsts.IAMGE_PATH.concat("sentitems16.png"));
//    }
//
//    /** 发送接收图标 */
//    public Icon getSendAndReceiveIcon() {
//        return PIMPool.pool.getIcon(PaneConsts.IAMGE_PATH.concat("sendandreceive.png"));
//    }
//
//    /** 答复图标 */
//    public Icon getReplyIcon() {
//        return PIMPool.pool.getIcon(PaneConsts.IAMGE_PATH.concat("reply.png"));
//    }
//
//    /** 全部答复图标 */
//    public Icon getReplyAllIcon() {
//        return PIMPool.pool.getIcon(PaneConsts.IAMGE_PATH.concat("replyall.png"));
//    }
//
//    /** 转发图标 */
//    public Icon getForwordIcon() {
//        return PIMPool.pool.getIcon(PaneConsts.IAMGE_PATH.concat("forward.png"));
//    }
//
//    /** 未读邮件图标 */
//    public Icon getUnreadMailIcon() {
//        return PIMPool.pool.getIcon(PaneConsts.IAMGE_PATH.concat("mail.png"));
//    }
//
//    public Image getUnreadMailImage() {
//        return PIMPool.pool.getImage(PaneConsts.IAMGE_PATH.concat("mail.png"));
//    }
//
//    /** 删除邮件的ICON */
//    public Icon getDeleteEmailIcon() {
//        return PIMPool.pool.getIcon(PaneConsts.IAMGE_PATH.concat("denysend16.gif"));
//    }
//
//    /** * 移至文件夹 */
//    public Icon getMoveToFolderIcon() {
//        return PIMPool.pool.getIcon(PaneConsts.IAMGE_PATH.concat("move.png"));
//    }
//
//    /** * @Called by: EmailBar; */
//    public Icon getCheckNameIcon() {
//        return PIMPool.pool.getIcon(PaneConsts.IAMGE_PATH.concat("Namecheck.png"));
//    }
//
//    /** * @called by: MailFrame(上的工具条)， InboxIconRenderer; */
//    public Icon getFeedbackIcon() {
//        return PIMPool.pool.getIcon(PaneConsts.IAMGE_PATH.concat("feedback.gif"));
//    }
//
//    /**
//     * @return Image
//     * @see java.awt.Image
//     */
//    public Icon getDenysendIcon() {
//        // todo：取出Cell中的用户设置。
//        // 判断是否为空
//        // 为空则取出检查当前的风格设置。
//        // 判断是否为空
//        // 为空则赋给默认值(49,97,90)一种难看的绿色，可能因该改的更好看些或用图片背景。
//        return PIMPool.pool.getIcon(PaneConsts.IAMGE_PATH.concat("denysend16.gif"));
//    }
//
//    public Icon getProcessingFlagIcon() {
//        return PIMPool.pool.getIcon(PaneConsts.IAMGE_PATH.concat("processingFlag.gif"));
//    }
//
//    public Icon getFailedFlagIcon() {
//        return PIMPool.pool.getIcon(PaneConsts.IAMGE_PATH.concat("failedFlag.gif"));
//    }
//
//    public Icon getCompletedFlagIcon() {
//        return PIMPool.pool.getIcon(PaneConsts.IAMGE_PATH.concat("completedFlag.gif"));
//    }
//
//    public abstract String getTemplateName();
//
//    public abstract void setTemplateName(
//            String prmName);
//
//    /**********************************************************************************************************/
//    /* 已删除项 */
//    /***********************************************************************************************************/
//
//    /** @return Icon:DeleteItem的Icon. */
//    public Icon getDeleteItemIcon(
//            boolean prmBigIcon) {
//        // todo：取出Cell中的用户设置。
//        // 判断是否为空
//        // 为空则取出检查当前的风格设置。
//        // 判断是否为空
//        // 为空则赋给默认值(49,97,90)一种难看的绿色，可能因该改的更好看些或用图片背景。
//        return PIMPool.pool.getIcon(PaneConsts.IAMGE_PATH.concat((prmBigIcon ? "delete32.png" : "delete16.png")));
//    }
//
//    /*********************************************************************************************************/
//    /* 表格 */
//    /*********************************************************************************************************/
//    // 表格中字段对应的图片=========================================================================================
//    public Icon getTypeFieldIcon() {
//        return PIMPool.pool.getIcon(PaneConsts.IAMGE_PATH.concat("AppType.gif"));
//    }
//
//    public Icon getAttachFieldIcon(
//            boolean prmBigIcon) {
//        return PIMPool.pool.getIcon(PaneConsts.IAMGE_PATH.concat((prmBigIcon ? "attach32.png" : "attach.png")));
//    }
//
//    public Icon getCompleteFieldIcon() {
//        return PIMPool.pool.getIcon(PaneConsts.IAMGE_PATH.concat("completed.gif"));
//    }
//
//    public Icon getInsertPicIcon() {
//        return PIMPool.pool.getIcon(PaneConsts.IAMGE_PATH.concat("Picture.gif"));
//    }
//
//    public Icon getHyplinkIcon() {
//        return PIMPool.pool.getIcon(PaneConsts.IAMGE_PATH.concat("HyperLink.gif"));
//    }
//
//    /** * @called by: ContactViewPane; */
//    public Icon getMarkStateFieldIcon(
//            int prmType) {
//        if (prmType == 0)
//            return PIMPool.pool.getIcon(PaneConsts.IAMGE_PATH.concat("markState.png"));
//        else if (prmType == 1)
//            return PIMPool.pool.getIcon(PaneConsts.IAMGE_PATH.concat("markState1.png"));
//        else
//            return PIMPool.pool.getIcon(PaneConsts.IAMGE_PATH.concat("markState2.png"));
//    }
//
//    public Icon getRecurrenceFieldIcon() {
//        return PIMPool.pool.getIcon(PaneConsts.IAMGE_PATH.concat("reminder16.gif"));
//    }
//
//    public Icon getRequireStateFieldIcon() {
//        return PIMPool.pool.getIcon(PaneConsts.IAMGE_PATH.concat("reminder16.gif"));
//    }
//
//    public Icon getImportantFieldIcon() {
//        return PIMPool.pool.getIcon(PaneConsts.IAMGE_PATH.concat("HighPriority.png"));
//    }
//
//    public Icon getUnImportantFieldIcon() {
//        return PIMPool.pool.getIcon(PaneConsts.IAMGE_PATH.concat("LowPriority.png"));
//    }
//
//    public Icon getRemoteStateFieldIcon() {
//        return PIMPool.pool.getIcon(PaneConsts.IAMGE_PATH.concat("reminder16.gif"));
//    }
//
//    public Icon getDownLoadStateFieldIcon() {
//        return PIMPool.pool.getIcon(PaneConsts.IAMGE_PATH.concat("reminder16.gif"));
//    }
//
//    public Icon getReadedStateFieldIcon() {
//        return PIMPool.pool.getIcon(PaneConsts.IAMGE_PATH.concat("ReadedMail.png"));
//    }
//
//    public Icon getRemindFieldIcon() {
//        return PIMPool.pool.getIcon(PaneConsts.IAMGE_PATH.concat("reminder16.gif"));
//    }
//
//    // =======================================================================================================
//    /** * @called by:emo.pim.pimview.ebeans.EWeekView; */
//    public int getMaxWeeks() {
//        return PaneConsts.MAX_WEEK;
//    }
//
//    // -----------------------------------------------------------------------------
//    /** */
//    public Image getWorkweekImage() {
//        // todo：取出Cell中的用户设置。
//        // 判断是否为空
//        // 为空则取出检查当前的风格设置。
//        // 判断是否为空
//        // 为空则赋给默认值(49,97,90)一种难看的绿色，可能因该改的更好看些或用图片背景。
//        return PIMPool.pool.getImage(PaneConsts.IAMGE_PATH.concat("workweek32.gif"));
//    }
//
//    /** */
//    public Image getCalendarOptionImage() {
//        // todo：取出Cell中的用户设置。
//        // 判断是否为空
//        // 为空则取出检查当前的风格设置。
//        // 判断是否为空
//        // 为空则赋给默认值(49,97,90)一种难看的绿色，可能因该改的更好看些或用图片背景。
//        return PIMPool.pool.getImage(PaneConsts.IAMGE_PATH.concat("calendaroption32.gif"));
//    }
//
//    /** */
//    public Icon getTodayworkIcon(
//            boolean prmBigIcon) {
//        // todo：取出Cell中的用户设置。
//        // 判断是否为空
//        // 为空则取出检查当前的风格设置。
//        // 判断是否为空
//        // 为空则赋给默认值(49,97,90)一种难看的绿色，可能因该改的更好看些或用图片背景。
//        return PIMPool.pool.getIcon(PaneConsts.IAMGE_PATH.concat((prmBigIcon ? "todaywork32.png" : "todaywork16.gif")));
//    }
//
//    public Icon getLocalFolderIcon(
//            boolean prmBigIcon) {
//        // TODO：取出Cell中的用户设置。
//        // 判断是否为空
//        // 为空则取出检查当前的风格设置。
//        // 判断是否为空
//        // 为空则赋给默认值(49,97,90)一种难看的绿色，可能因该改的更好看些或用图片背景。
//        return PIMPool.pool.getIcon(PaneConsts.IAMGE_PATH.concat((prmBigIcon ? "todaywork32.png" : "todaywork16.png")));
//    }
//
//    public Icon getIMAPFolderIcon(
//            boolean prmBigIcon) {
//        // TODO：取出Cell中的用户设置。
//        // 判断是否为空
//        // 为空则取出检查当前的风格设置。
//        // 判断是否为空
//        // 为空则赋给默认值(49,97,90)一种难看的绿色，可能因该改的更好看些或用图片背景。
//        return PIMPool.pool.getIcon(PaneConsts.IAMGE_PATH.concat((prmBigIcon ? "todaywork32.png" : "todaywork16.png")));
//    }
//
//    /** */
//    public Icon getEventupIcon() {
//        // todo：取出Cell中的用户设置。
//        // 判断是否为空
//        // 为空则取出检查当前的风格设置。
//        // 判断是否为空
//        // 为空则赋给默认值(49,97,90)一种难看的绿色，可能因该改的更好看些或用图片背景。
//        return PIMPool.pool.getIcon(PaneConsts.IAMGE_PATH.concat("eventup16.gif"));
//    }
//
//    /** */
//    public Icon getEventDownIcon() {
//        // todo：取出Cell中的用户设置。
//        // 判断是否为空
//        // 为空则取出检查当前的风格设置。
//        // 判断是否为空
//        // 为空则赋给默认值(49,97,90)一种难看的绿色，可能因该改的更好看些或用图片背景。
//        return PIMPool.pool.getIcon(PaneConsts.IAMGE_PATH.concat("eventdown16.gif"));
//    }
//
//    /** */
//    public Icon getReminderIcon() {
//        // todo：取出Cell中的用户设置。
//        // 判断是否为空
//        // 为空则取出检查当前的风格设置。
//        // 判断是否为空
//        // 为空则赋给默认值(49,97,90)一种难看的绿色，可能因该改的更好看些或用图片背景。
//        return PIMPool.pool.getIcon(PaneConsts.IAMGE_PATH.concat("reminder16.gif"));
//    }
//
//    /** */
//    public Icon getRecurrenceIcon() {
//        // todo：取出Cell中的用户设置。
//        // 判断是否为空
//        // 为空则取出检查当前的风格设置。
//        // 判断是否为空
//        // 为空则赋给默认值(49,97,90)一种难看的绿色，可能因该改的更好看些或用图片背景。
//        return PIMPool.pool.getIcon(PaneConsts.IAMGE_PATH.concat("recurrence.gif"));
//    }
//
//    /** */
//    public Icon getPrivateIcon() {
//        // todo：取出Cell中的用户设置。
//        // 判断是否为空
//        // 为空则取出检查当前的风格设置。
//        // 判断是否为空
//        // 为空则赋给默认值(49,97,90)一种难看的绿色，可能因该改的更好看些或用图片背景。
//        return PIMPool.pool.getIcon(PaneConsts.IAMGE_PATH.concat("private16.gif"));
//    }
//
//    /** */
//    public Icon getBCCIcon() {
//        // todo：取出Cell中的用户设置。
//        // 判断是否为空
//        // 为空则取出检查当前的风格设置。
//        // 判断是否为空
//        // 为空则赋给默认值(49,97,90)一种难看的绿色，可能因该改的更好看些或用图片背景。
//        return PIMPool.pool.getIcon(PaneConsts.IAMGE_PATH.concat("Bcc.png"));
//    }
//
//    /**
//     * 账户图标
//     *
//     * @return
//     */
//    public Icon getFromIcon() {
//        return PIMPool.pool.getIcon(PaneConsts.IAMGE_PATH.concat("account.gif"));
//    }
//
//    /**
//     * * @return Image
//     *
//     * @see java.awt.Image
//     */
//    public Icon getHorBarIcon() {
//        // todo：取出Cell中的用户设置。
//        // 判断是否为空
//        // 为空则取出检查当前的风格设置。
//        // 判断是否为空
//        // 为空则赋给默认值(49,97,90)一种难看的绿色，可能因该改的更好看些或用图片背景。
//        return PIMPool.pool.getIcon(PaneConsts.IAMGE_PATH.concat("HorBar.gif"));
//    }
//
//    /**
//     * * @return Image
//     *
//     * @see java.awt.Image
//     */
//    public Icon getNewIcon() {
//        // todo：取出Cell中的用户设置。
//        // 判断是否为空
//        // 为空则取出检查当前的风格设置。
//        // 判断是否为空
//        // 为空则赋给默认值(49,97,90)一种难看的绿色，可能因该改的更好看些或用图片背景。
//        return PIMPool.pool.getIcon(PaneConsts.IAMGE_PATH.concat("new.png"));
//    }
//
//    public Image getNewImage() {
//        return PIMPool.pool.getImage(PaneConsts.IAMGE_PATH.concat("new.png"));
//    }
//
//    /**
//     * * @return Image
//     *
//     * @see java.awt.Image
//     */
//    public Icon getOpenIcon() {
//        // todo：取出Cell中的用户设置。
//        // 判断是否为空
//        // 为空则取出检查当前的风格设置。
//        // 判断是否为空
//        // 为空则赋给默认值(49,97,90)一种难看的绿色，可能因该改的更好看些或用图片背景。
//        return PIMPool.pool.getIcon(PaneConsts.IAMGE_PATH.concat("open.gif"));
//    }
//
//    /**
//     * * @return Image
//     *
//     * @see java.awt.Image
//     */
//    public Icon getSaveIcon() {
//        // todo：取出Cell中的用户设置。
//        // 判断是否为空
//        // 为空则取出检查当前的风格设置。
//        // 判断是否为空
//        // 为空则赋给默认值(49,97,90)一种难看的绿色，可能因该改的更好看些或用图片背景。
//        return PIMPool.pool.getIcon(PaneConsts.IAMGE_PATH.concat("save.gif"));
//    }
//
//    /**
//     * * @return Image
//     *
//     * @see java.awt.Image
//     */
//    public Icon getCopyIcon() {
//        // todo：取出Cell中的用户设置。
//        // 判断是否为空
//        // 为空则取出检查当前的风格设置。
//        // 判断是否为空
//        // 为空则赋给默认值(49,97,90)一种难看的绿色，可能因该改的更好看些或用图片背景。
//        return PIMPool.pool.getIcon(PaneConsts.IAMGE_PATH.concat("copy.png"));
//    }
//
//    /**
//     * * @return Image
//     *
//     * @see java.awt.Image
//     */
//    public Icon getCutIcon() {
//        // todo：取出Cell中的用户设置。
//        // 判断是否为空
//        // 为空则取出检查当前的风格设置。
//        // 判断是否为空
//        // 为空则赋给默认值(49,97,90)一种难看的绿色，可能因该改的更好看些或用图片背景。
//        return PIMPool.pool.getIcon(PaneConsts.IAMGE_PATH.concat("cut.png"));
//    }
//
//    /**
//     * * @return Image
//     *
//     * @see java.awt.Image
//     */
//    public Icon getPasteIcon() {
//        // todo：取出Cell中的用户设置。
//        // 判断是否为空
//        // 为空则取出检查当前的风格设置。
//        // 判断是否为空
//        // 为空则赋给默认值(49,97,90)一种难看的绿色，可能因该改的更好看些或用图片背景。
//        return PIMPool.pool.getIcon(PaneConsts.IAMGE_PATH.concat("paste.png"));
//    }
//
//    /**
//     * * @return Image
//     *
//     * @see java.awt.Image
//     */
//    public Icon getBoldIcon() {
//        // todo：取出Cell中的用户设置。
//        // 判断是否为空
//        // 为空则取出检查当前的风格设置。
//        // 判断是否为空
//        // 为空则赋给默认值(49,97,90)一种难看的绿色，可能因该改的更好看些或用图片背景。
//        return PIMPool.pool.getIcon(PaneConsts.IAMGE_PATH.concat("bold.gif"));
//    }
//
//    /**
//     * * @return Image
//     *
//     * @see java.awt.Image
//     */
//    public Icon getItalicIcon() {
//        // todo：取出Cell中的用户设置。
//        // 判断是否为空
//        // 为空则取出检查当前的风格设置。
//        // 判断是否为空
//        // 为空则赋给默认值(49,97,90)一种难看的绿色，可能因该改的更好看些或用图片背景。
//        return PIMPool.pool.getIcon(PaneConsts.IAMGE_PATH.concat("italic.gif"));
//    }
//
//    /**
//     * * @return Image
//     *
//     * @see java.awt.Image
//     */
//    public Icon getUnderlineIcon() {
//        // todo：取出Cell中的用户设置。
//        // 判断是否为空
//        // 为空则取出检查当前的风格设置。
//        // 判断是否为空
//        // 为空则赋给默认值(49,97,90)一种难看的绿色，可能因该改的更好看些或用图片背景。
//        return PIMPool.pool.getIcon(PaneConsts.IAMGE_PATH.concat("underline.gif"));
//    }
//
//    /**
//     * * @return Image
//     *
//     * @see java.awt.Image
//     */
//    public Icon getCenterIcon() {
//        // todo：取出Cell中的用户设置。
//        // 判断是否为空
//        // 为空则取出检查当前的风格设置。
//        // 判断是否为空
//        // 为空则赋给默认值(49,97,90)一种难看的绿色，可能因该改的更好看些或用图片背景。
//        return PIMPool.pool.getIcon(PaneConsts.IAMGE_PATH.concat("center.gif"));
//    }
//
//    /**
//     * * @return Image
//     *
//     * @see java.awt.Image
//     */
//    public Icon getLeftIcon() {
//        // todo：取出Cell中的用户设置。
//        // 判断是否为空
//        // 为空则取出检查当前的风格设置。
//        // 判断是否为空
//        // 为空则赋给默认值(49,97,90)一种难看的绿色，可能因该改的更好看些或用图片背景。
//        return PIMPool.pool.getIcon(PaneConsts.IAMGE_PATH.concat("left.gif"));
//    }
//
//    /**
//     * * @return Image
//     *
//     * @see java.awt.Image
//     */
//    public Icon getRightIcon() {
//        // todo：取出Cell中的用户设置。
//        // 判断是否为空
//        // 为空则取出检查当前的风格设置。
//        // 判断是否为空
//        // 为空则赋给默认值(49,97,90)一种难看的绿色，可能因该改的更好看些或用图片背景。
//        return PIMPool.pool.getIcon(PaneConsts.IAMGE_PATH.concat("right.gif"));
//    }
//
//    /**
//     * * @return Image
//     *
//     * @see java.awt.Image
//     */
//    public Icon getIcon_FG() {
//        // todo：取出Cell中的用户设置。
//        // 判断是否为空
//        // 为空则取出检查当前的风格设置。
//        // 判断是否为空
//        // 为空则赋给默认值(49,97,90)一种难看的绿色，可能因该改的更好看些或用图片背景。
//        return PIMPool.pool.getIcon(PaneConsts.IAMGE_PATH.concat("fg.gif"));
//    }
//
//    /**
//     * * @return Image
//     *
//     * @see java.awt.Image
//     */
//    public Icon getUndoIcon() {
//        // todo：取出Cell中的用户设置。
//        // 判断是否为空
//        // 为空则取出检查当前的风格设置。
//        // 判断是否为空
//        // 为空则赋给默认值(49,97,90)一种难看的绿色，可能因该改的更好看些或用图片背景。
//        return PIMPool.pool.getIcon(PaneConsts.IAMGE_PATH.concat("Undo.gif"));
//    }
//
//    /**
//     * * @return Image
//     *
//     * @see java.awt.Image
//     */
//    public Icon getRedoIcon() {
//        // todo：取出Cell中的用户设置。
//        // 判断是否为空
//        // 为空则取出检查当前的风格设置。
//        // 判断是否为空
//        // 为空则赋给默认值(49,97,90)一种难看的绿色，可能因该改的更好看些或用图片背景。
//        return PIMPool.pool.getIcon(PaneConsts.IAMGE_PATH.concat("Redo.gif"));
//    }
//
//    /**
//     * * @return Image
//     *
//     * @see java.awt.Image
//     */
//    public Icon getFontColorIcon() {
//        // todo：取出Cell中的用户设置。
//        // 判断是否为空
//        // 为空则取出检查当前的风格设置。
//        // 判断是否为空
//        // 为空则赋给默认值(49,97,90)一种难看的绿色，可能因该改的更好看些或用图片背景。
//        return PIMPool.pool.getIcon(PaneConsts.IAMGE_PATH.concat("FontColor.gif"));
//    }
//
//    /**
//     * * called by PIMAdvancedBar
//     *
//     * @return Icon
//     */
//    public Icon getLastFolderIcon() {
//        return PIMPool.pool.getIcon(PaneConsts.IAMGE_PATH.concat("back.gif"));
//    }
//
//    /**
//     * * called by PIMAdvancedBar
//     *
//     * @return Icon
//     */
//    public Icon getNextFolderIcon() {
//        return PIMPool.pool.getIcon(PaneConsts.IAMGE_PATH.concat("forward.gif"));
//    }
//
//    /**
//     * 初始化PIM中的图片资源，该方式好像不太好，应该是切换到哪一种视图就去取相应的图片。
//     *
//     * private void initImageFile() { imageFileHash16.put(DAY_VIEW, PaneConstant.IAMGE_PATH.concat("day16.gif");
//     * imageFileHash16.put(WORK_WEEK_VIEW, PaneConstant.IAMGE_PATH.concat("workweek16.gif");
//     * imageFileHash16.put(WEEK_VIEW, PaneConstant.IAMGE_PATH.concat("week16.gif"); imageFileHash16.put(MONTH_VIEW,
//     * PaneConstant.IAMGE_PATH.concat("month16.gif"); imageFileHash16.put(YEAR_VIEW,
//     * PaneConstant.IAMGE_PATH.concat("year16.gif"); imageFileHash16.put(MEETING_ICON,
//     * PaneConstant.IAMGE_PATH.concat("meeting16.gif"); imageFileHash16.put(REQUEST_ICON,
//     * PaneConstant.IAMGE_PATH.concat("request16.gif");
//     *
//     * imageFileHash32.put(NOTE_PAPER, PaneConstant.IAMGE_PATH.concat("LotusSurface.gif");
//     * imageFileHash32.put(NOTE_BOOK, PaneConstant.IAMGE_PATH.concat("notebook32.gif"); imageFileHash32.put(TODAY_TITLE,
//     * PaneConstant.IAMGE_PATH.concat("activitiestoday.gif"); }
//     */
//
//    /**
//     * * 获得图标的静态方法
//     *
//     * @param name
//     *            图标的名字
//     */
//    public Icon getLinkGuideIcon(
//            int Index) {
//        // todo：取出Cell中的用户设置。
//        // 判断是否为空
//        // 为空则取出检查当前的风格设置。
//        // 判断是否为空
//        // 为空则赋给默认值(49,97,90)一种难看的绿色，可能因该改的更好看些或用图片背景。
//        if (Index == 0)
//            return PIMPool.pool.getIcon(PaneConsts.IAMGE_PATH.concat("yourname.jpg"));
//        else if (Index == 1)
//            return PIMPool.pool.getIcon(PaneConsts.IAMGE_PATH.concat("emailaddress.jpg"));
//        else if (Index == 2)
//            return PIMPool.pool.getIcon(PaneConsts.IAMGE_PATH.concat("emailSever.jpg"));
//        else if (Index == 3)
//            return PIMPool.pool.getIcon(PaneConsts.IAMGE_PATH.concat("entry.jpg"));
//        else if (Index == 4)
//            return PIMPool.pool.getIcon(PaneConsts.IAMGE_PATH.concat("empty.jpg"));
//        else
//            return PIMPool.pool.getIcon(PaneConsts.IAMGE_PATH.concat("congratulation.jpg"));
//    }
//
//    // Colors-----------------------------------------------------------------------------------
//    /**
//     * 返回主面板背景色
//     *
//     * @return Color
//     * @see java.awt.Color
//     */
//    public Color getMainPaneBGColor() {
//        // todo：取出Cell中的用户设置。
//        // 判断是否为空
//        // 为空则取出检查当前的风格设置。
//        // 判断是否为空
//        // 为空则赋给默认值(49,97,90)一种难看的绿色，可能因该改的更好看些或用图片背景。
//        return PIMPool.pool.getColor(100, 100, 100);
//    }
//
//    /**
//     * 返回初始的Note面板的背景色
//     *
//     * @return Color
//     * @see java.awt.Color
//     */
//    Color getInitialNotePaneBGColor() {
//        // todo：取出Cell中的用户设置。
//        // 判断是否为空
//        // 为空则取出检查当前的风格设置。
//        // 判断是否为空
//        // 为空则赋返回null，调用着发现没有路径返回，将赋予主界面单纯的颜色背景。
//        return getMainPaneBGColor();
//    }
//
//    /**
//     * @return Color
//     * @see java.awt.Color
//     */
//    public Color getBookCoverColor() {
//        // todo：取出Cell中的用户设置。
//        // 判断是否为空
//        // 为空则取出检查当前的风格设置。
//        // 判断是否为空
//        // 为空则赋给默认值(49,97,90)一种难看的绿色，可能因该改的更好看些或用图片背景。
//        return PIMPool.pool.getColor(0, 0, 0);
//    }
//
//    /**
//     * @return Color
//     * @see java.awt.Color
//     */
//    public Color getTentativeTimeColor() {
//        // todo：取出Cell中的用户设置。
//        // 判断是否为空
//        // 为空则取出检查当前的风格设置。
//        // 判断是否为空
//        // 为空则赋给默认值(49,97,90)一种难看的绿色，可能因该改的更好看些或用图片背景。
//        return PIMPool.pool.getColor(166, 202, 240);
//    }
//
//    /**
//     * 返回繁忙时间颜色
//     *
//     * @return Color
//     * @see java.awt.Color
//     */
//    public Color getBusyTimeColor() {
//        // todo：取出Cell中的用户设置。
//        // 判断是否为空
//        // 为空则取出检查当前的风格设置。
//        // 判断是否为空
//        // 为空则赋给默认值(0, 0, 255)一种难看的绿色，可能因该改的更好看些或用图片背景。
//        return PIMPool.pool.getColor(0, 0, 255);
//    }
//
//    /**
//     * 返回业余时间颜色
//     *
//     * @return Color
//     * @see java.awt.Color
//     */
//    public Color getSpareTimeColor() {
//        // todo：取出Cell中的用户设置。
//        // 判断是否为空
//        // 为空则取出检查当前的风格设置。
//        // 判断是否为空
//        // 为空则赋给默认值(49,97,90)蓝色。
//        return PIMPool.pool.getColor(128, 0, 128);
//    }
//
//    /**
//     * 返回闲暇时间颜色
//     *
//     * @return Color
//     * @see java.awt.Color
//     */
//    public Color getFreeTimeColor() {
//        // todo：取出Cell中的用户设置。
//        // 判断是否为空
//        // 为空则取出检查当前的风格设置。
//        // 判断是否为空
//        // 为空则赋给默认值(255, 255, 255):白色。
//        return PIMPool.pool.getColor(255, 255, 255);
//    }
//
//    public Color getOutWorkTimeBG() {
//        return getOnWorkTimeBG().darker();
//    }
//
//    public Color getOnWorkTimeBG() {
//        return getContentBG();
//    }
//
//    // --------------------------------------------------------------------------
//    /**
//     * @called by:emo.pim.pimview.NoteBookPane; 返回日历表格背景色
//     * @return Color
//     * @see java.awt.Color
//     */
//    public Color getTabBGAt(
//            int prmIndex) {
//        prmIndex = (prmIndex > 0 ? prmIndex : 0);
//        // todo：取出Cell中的用户设置。
//        // 判断是否为空
//        // 为空则取出检查当前的风格设置。
//        // 判断是否为空
//        // 为空则赋给默认值:白色。
//        if (prmIndex < tabBGList.size())
//            return tabBGList.get(prmIndex);
//        else
//            // TODO:返回用户设置的最下面一张分隔纸板的背景色，否则返回默认值。
//            return PIMPool.pool.getColor(168, 168, 168);
//    }
//
//    /**
//     * @called by:emo.pim.pimview.NoteBookPane; 返回日历表格前景色
//     * @return Color
//     * @see java.awt.Color
//     */
//    public Color getTabFGAt(
//            int prmIndex) {
//        prmIndex = (prmIndex > 0 ? prmIndex : 0);
//        // todo：取出Cell中的用户设置。
//        // 判断是否为空
//        // 为空则取出检查当前的风格设置。
//        // 判断是否为空
//        // 为空则赋给默认值:白色。
//        return tabFGList.get(prmIndex);
//    }
//
//    /**
//     * @called by:emo.pim.pimview.CoverPane; EBinderClamp 返回Note表背景色
//     * @return Color
//     * @see java.awt.Color
//     */
//    public Color getClampColor() {
//        // todo：取出Cell中的用户设置。
//        // 判断是否为空
//        // 为空则取出检查当前的风格设置。
//        // 判断是否为空
//        // 为空则赋给默认值:白色。
//        return PIMPool.pool.getColor(255, 255, 255);
//    }
//
//    /**
//     * 返回天视图字体 change to "public" because of the new package
//     *
//     * @return Font
//     * @called by: EMonthGroup;
//     */
//    public Font getFontOfDay() {
//        return PIMPool.pool.getFont((String) hash.get(PaneConsts.DFT_FONT), Font.PLAIN, 11);
//    }
//
//    /**
//     * 返回天视图字体 change to "public" because of the new package
//     *
//     * @return Font
//     * @called by: EMonthGroup;
//     */
//    public Font getFontOfDSABar() {
//        return PIMPool.pool.getFont((String) hash.get(PaneConsts.DFT_FONT), Font.PLAIN, 12);
//    }
//
//    /**
//     * 返回天视图字体 change to "public" because of the new package
//     *
//     * @return Font
//     * @called by: EMonthGroup;
//     */
//    public Font getFontOfWeek() {
//        return PIMPool.pool.getFont((String) hash.get(PaneConsts.DFT_FONT), Font.PLAIN, 9);
//    }
//
//    public Font getFontOfTodayDataPane() {
//        return PIMPool.pool.getFont((String) hash.get(PaneConsts.DFT_FONT), Font.PLAIN, 20);
//    }
//
//    public Font getFontOfDefault() {
//        return PIMPool.pool.getFont((String) hash.get(PaneConsts.DFT_FONT), Font.BOLD, getFontSize());
//    }
//
//    public Font getFontOfTableHeader() {
//        return PIMPool.pool.getFont((String) hash.get(PaneConsts.DFT_FONT), Font.PLAIN, getFontSize());
//    }
//
//    /** * @called by: emo.pim.pimview.pimDialog.MailFrame.java; */
//    public Font getFontOfMailEditor() {
//        return PIMPool.pool.getFont((String) hash.get(PaneConsts.DFT_FONT), Font.PLAIN, getFontSize());
//    }
//
//    /**
//     * 返回启动面板的类型 目前HardCode为ACTIVE_TODAY
//     *
//     * @return String
//     * @see String
//     */
//    public String getStartPaneType() {
//        return PaneConsts.ACTIVE_TODAY;
//    }
//
//    /**
//     * called by PaneManager; 根据传入的不同的参数，返回不同的Canlendar model类型
//     *
//     * @param prmPaneType
//     *            <code>String</code>面板类型
//     * @return int
//     */
//    public int getCalendarModel(
//            String prmPaneType) {
//        if (prmPaneType == PaneConsts.ACTIVE_TODAY)
//            return MonthConstant.DAY_MODEL;
//        else
//            return -1;
//    }
//
//    /** * @called by: PIMMain; */
//    public Color getTodayCanvasLineColor() {
//        // todo：取出Cell中的用户设置。
//        // 判断是否为空
//        // 为空则取出检查当前的风格设置。
//        // 判断是否为空
//        // 为空则赋给默认值:色。
//        return PIMPool.pool.getColor(132, 130, 57);
//    }
//
//    /** * @called by: PIMMain; */
//    public Color getTodayCanvasStringColor() {
//        // todo：取出Cell中的用户设置。
//        // 判断是否为空
//        // 为空则取出检查当前的风格设置。
//        // 判断是否为空
//        // 为空则赋给默认值:色。
//        return PIMPool.pool.getColor(132, 130, 57);
//    }
//
//    /** * @called by:PIMMain; */
//    public Color getTodayCanvasBGColor() {
//        // todo：取出Cell中的用户设置。
//        // 判断是否为空
//        // 为空则取出检查当前的风格设置。
//        // 判断是否为空
//        // 为空则赋给默认值:色。
//        return PIMPool.pool.getColor(255, 239, 206);
//    }
//
//    /** * @called by:emo.pim.pimview.CoverPane;emo.pim.pimview.ebeans.EBinderClamp; */
//    public int getVerticalDis() {
//        return 32;
//    }
//
//    /** * @called by:emo.pim.pimview.CoverPane;emo.pim.pimview.ebeans.EBinderClamp; */
//    public int getClampNumber() {
//        return 4;
//    }
//
//    // /**// * @called by PIMUtility;// */
//    // public int getFirstWeekDay()
//    // {
//    // return 0;
//    // }
//    // /**// *// */
//    // public int getWorkWeekDay()
//    // {
//    // return 0;
//    // }
//    //
//    // /**// *TODO:因该改为int型。布尔值与String转换时（存盘时）可能会麻烦。// */
//    // public boolean isWeekNumberVisible()
//    // {
//    // return false;
//    // }
//    // /***/
//    // public int getFirstYearWeek()
//    // {
//    // return 0;
//    // }
//    /** */
//    public int getTimebarCurrentMode() {
//        return 0;
//    }
//
//    /** * */
//    public int getFirstTimeZone() {
//        return 0;
//    }
//
//    /** * */
//    public int getSecondTimeZone() {
//        return 0;
//    }
//
//    public abstract int getFIRST_WEEK_DAY();
//
//    public abstract int getWORK_WEEK_DAY();
//
//    public abstract boolean isWEEK_NUMBER_VISIBLE();
//
//    public abstract int getFIRST_YEAR_WEEK();
//
//    /**
//     * 获得当前包含undo/redo按钮的toolbar
//     *
//     * @return UndoBar 当前包含undo/redo按钮的toolbar public UndoBar getUndoBar() { return
//     *         null;//BarInitialize.bar.getStandardBar(); }
//     */
//
//    /**
//     * 设置ToolBar上的undo/redo按钮的最大undo次数。 当前实现的是整合运行的方法
//     *
//     * @param prmLimit
//     *            ToolBar上的undo/redo按钮的最大undo次数
//     */
//    public void setUndoSizeInBar(
//            int prmLimit) {
//        // BarInitialize.bar.getStandardBar().setUndoSize(prmLimit);
//    }
//
//    // getters and setters----------------------------------------------------
//    public int getTabCount() {
//        return tabBGList.size();
//    }
//
    /** * @called by:emo.pim.PIMControl; */
    public void saveData() {
    }
//
//    /**
//     * Getter for property datePaneFloat.
//     *
//     * @return Value of property datePaneFloat.
//     */
//    public boolean isDatePaneFloat() {
//        return this.datePaneFloat;
//    }
//
//    /**
//     * Setter for property datePaneFloat.
//     *
//     * @param datePaneFloat
//     *            New value of property datePaneFloat.
//     */
//    public void setDatePaneFloat(
//            boolean datePaneFloat) {
//        this.datePaneFloat = datePaneFloat;
//    }
//
//    /**
//     * Getter for property datePaneVisible.
//     *
//     * @return Value of property datePaneVisible.
//     */
//    public boolean isDatePaneVisible() {
//        return this.datePaneVisible;
//    }
//
//    /**
//     * Setter for property datePaneVisible.
//     *
//     * @param datePaneVisible
//     *            New value of property datePaneVisible.
//     */
//    public void setDatePaneVisible(
//            boolean datePaneVisible) {
//        this.datePaneVisible = datePaneVisible;
//    }
//
//    /**
//     * Getter for property datePaneFloatSize.
//     *
//     * @return Value of property datePaneFloatSize.
//     */
//    public Dimension getDatePaneFloatSize() {
//        return this.datePaneFloatSize;
//    }
//
//    /**
//     * Setter for property datePaneFloatSize.
//     *
//     * @param datePaneFloatSize
//     *            New value of property datePaneFloatSize.
//     */
//    public void setDatePaneFloatSize(
//            Dimension datePaneFloatSize) {
//        this.datePaneFloatSize = datePaneFloatSize;
//    }
//
//    /**
//     * Getter for property datePaneFixedSize.
//     *
//     * @return Value of property datePaneFixedSize.
//     */
//    public Dimension getDatePaneFixedSize() {
//        return this.datePaneFixedSize;
//    }
//
//    /**
//     * Setter for property datePaneFixedSize.
//     *
//     * @param datePaneFixedSize
//     *            New value of property datePaneFixedSize.
//     */
//    public void setDatePaneFixedSize(
//            Dimension datePaneFixedSize) {
//        this.datePaneFixedSize = datePaneFixedSize;
//    }
//
//    /**
//     * Getter for property datePaneFloatLocation.
//     *
//     * @return Value of property datePaneFloatLocation.
//     */
//    public Point getDatePaneFloatLocation() {
//        return this.datePaneFloatLocation;
//    }
//
//    /**
//     * Setter for property datePaneFloatLocation.
//     *
//     * @param datePaneFloatLocation
//     *            New value of property datePaneFloatLocation.
//     */
//    public void setDatePaneFloatLocation(
//            Point datePaneFloatLocation) {
//        this.datePaneFloatLocation = datePaneFloatLocation;
//    }
//
//    /**
//     * Getter for property dateFormat.
//     *
//     * @return Value of property dateFormat.
//     */
//    public abstract int getDateFormat();
//
//    /**
//     * Setter for property dateFormat.
//     *
//     * @param dateFormat
//     *            New value of property dateFormat.
//     */
//    public abstract void setDateFormat(
//            int dateFormat);
//
//    /**
//     * 查看导航面板是否显示
//     *
//     * @return true 显示导航面板 false 不显示导航面板
//     */
//    public abstract boolean isNavigationVisible();
//
//    /**
//     * 设置导航面板是否显示
//     *
//     * @param isNavigationPaneShow
//     *            设置导航面板是否显示
//     */
//    public abstract void setNavigationVisible(
//            boolean isNavigationPaneShow);
//
//    /**
//     * 查看书本边界是否显示
//     *
//     * @return true 显示书本边框 false 不显示书本边框
//     */
//    public abstract boolean isBaseBookHide();
//
//    /**
//     * 设置导航面板是否显示
//     *
//     * @param isNavigationPaneShow
//     *            设置导航面板是否显示
//     */
//    public abstract void setBaseBookHide(
//            boolean isBaseBookHide);
//
//    /**
//     * 查看视图是否旋转
//     *
//     * @return true 视图为上下结构 false 视图为左右结构
//     */
//    public abstract boolean isViewTopAndDown();
//
//    /**
//     * 设置导航面板是否显示
//     *
//     * @param isNavigationPaneShow
//     *            设置导航面板是否显示
//     */
//    public abstract void setViewTopAndDown(
//            boolean isViewTopDown);
//
//    /**
//     * 查看预览面板是否显示
//     *
//     * @param isNavigationPaneShow
//     *            设置导航面板是否显示
//     */
//    public abstract boolean isPreviewShown();
//
//    /**
//     * 设置预览面板是否显示
//     *
//     * @param isNavigationPaneShow
//     *            设置导航面板是否显示
//     */
//    public abstract void setPreView(
//            boolean isPreviewShown);
//
//    /**
//     * 取得日期面板高度的用户自定义设置
//     *
//     * @return 日期面板高度的用户自定义设置
//     */
//    public abstract int getDatePaneHight();
//
//    /**
//     * 设置日期面板高度的用户自定义设置
//     *
//     * @param datePaneHight
//     *            日期面板高度的用户自定义设置
//     */
//    public abstract void setDatePaneHight(
//            int datePaneHight);
//
//    /** * @called by:ContactGeneralPanel; */
//    public abstract void setContactsFirstPhoneIndex(
//            int prmIndex);
//
//    /** * @called by:ContactGeneralPanel; */
//    public abstract void setContactsSecondPhoneIndex(
//            int prmIndex);
//
//    /** * @called by:ContactGeneralPanel; */
//    public abstract void setContactsThirdPhoneIndex(
//            int prmIndex);
//
//    /** * @called by:ContactGeneralPanel; */
//    public abstract void setContactsFourthPhoneIndex(
//            int prmIndex);

    /**
     * 获得打印机名
     * 
     * @return 打印机名
     */
    public abstract String getPrinter();

    /**
     * 设置打印机名
     * 
     * @param printer
     *            打印机名
     */
    public abstract void setPrinter(
            String printer);

    /** * @called by:emo.pim.pimview.dialog.missiondialog.ParseString.java */
    public abstract int getWorkDaysPerWeek();

    /** * @called by:emo.pim.pimview.dialog.missiondialog.ParseString.java */
    public abstract int getWorkHoursPerDay();

    // @ call by 选项对话盒
    // ******************************************************
    // ----------------------选项方法开始---------------------
    // *******************************************************
    /**************************************/
    /* &&&&&& 邮件选项卡 &&&&& */
    /**************************************/
    // ------- ** 邮件选项对话盒 ** ----------
    // 邮件处理-------------------------------
    /** * 关闭原始邮件 */
    public abstract boolean getCloseMail();

    /** * 设置关闭原始邮件 */
    public abstract void setCloseMail(
            boolean isCloseMail);

    /** * 在文件夹中保存邮件副本 */
    public abstract boolean getSaveMailCopy();

    /** * 设置在文件夹中保存邮件副本 */
    public abstract void setSaveMailCopy(
            boolean isSaveMail);

    /** * 新邮件到达给出通知 */
    public abstract boolean getRemindMailReceive();

    /** * 设置新邮件到达给出通知 */
    public abstract void setRemindMailReceive(
            boolean isRemindMail);

    /** * 答复邮件时 */
    public abstract int getAnswerMail();

    /** * 设置答复邮件时 */
    public abstract void setAnswerMail(
            int answerIndex);

    /** * 转发邮件时 */
    public abstract int getReturnMail();

    /** * 设置转发邮件时 */
    public abstract void setReturnMail(
            int returenIndex);

    /** * 编辑邮件时标记修订 */
    public abstract boolean getSginEditMail();

    /** * 设置编辑邮件时标记修订 */
    public abstract void setSginEditMail(
            boolean isSgin);

    // 高级----------------------------------------------
    /** * 取得未发送邮件保存位置 */
    public abstract int getNotMailSavePos();

    /** * 设置未发送邮件保存位置 */
    public abstract void setNotMailSavePos(
            int saveIndex);

    /** * 取得已发送邮件保存位置 */
    public abstract int getHaveMailSavePos();

    /** * 设置已发送邮件保存位置 */
    public abstract void setHaveMailSavePos(
            int haveIndex);

    /** * 取得重要性 */
    public abstract int getImportance();

    /** * 设置重要性 */
    public abstract void setImportance(
            int impIndex);

    /** * 取得灵敏度 */
    public abstract int getSensitive();

    /** * 设置灵敏度 */
    public abstract void setSensitive(
            int senIndex);

    /** * 取得是否自动检查姓名 */
    public abstract boolean getAutoCheckName();

    /** * 设置是否自动检查姓名 */
    public abstract void setAutoCheckName(
            boolean isAutoCheck);

    /** * 取得删除会议要求 */
    public abstract boolean getDelMeetRequestion();

    /** * 设置删除会议要求 */
    public abstract void setDelMeetRequestion(
            boolean isDel);

    // 跟踪----------------------------------------------
    /** * 取得是否到达时处理回执 */
    public abstract boolean getDealReturnReceipt();

    /** * 设置是否到达时处理回执 */
    public abstract void setDealReturnReceipt(
            boolean isDealReturen);

    /** * 取得是否移动回执 */
    public abstract boolean getMoveRetrunReceipt();

    /** * 设置是否移动回执 */
    public abstract void setMoveRetrunReceipt(
            boolean isMove);

    /** * 取得回执移至地点 */
    public abstract int getReceiptMovePos();

    /** * 设置回执移至地点 */
    public abstract void setReceiptMovePos(
            int moveIndex);

    /** * 取得是否阅读后发出请求 */
    public abstract boolean getReadRequest();

    /** * 设置是否阅读后发出请求 */
    public abstract void setReadRequest(
            boolean isRequest);

    /** * 取得是否总是发送响应 */
    public abstract boolean getAlwaysSend();

    /** * 设置是否总是发送响应 */
    public abstract void setAlwaysSend(
            boolean isSend);

    /** * 取得从不发送 */
    public abstract boolean getAlwaysNotSend();

    /** * 设置从不发送 */
    public abstract void setAlwaysNotSend(
            boolean isNotSend);

    /** * 取得是否响应前向我询问 */
    public abstract boolean getRequestMe();

    /** * 设置是否响应前向我询问 */
    public abstract void setRequestMe(
            boolean isRequest);

    // 邮件选项对话盒 END --------------------------------

    /**
     * * 连接后立即发送邮件方法
     * 
     * @call by : dialog.optionDialog.TabMail
     */
    public abstract boolean getImmediateSendMail();

    /**
     * * 取得是否检查新邮件
     * 
     * @call by : dialog.optionDialog.TabMail
     */
    public abstract boolean getCheckMail();

    /**
     * * 取得检查新邮件的时间间隔
     * 
     * @call by : dialog.optionDialog.TabMail
     */
    public abstract int getSpantime();

    /**
     * * 设置连接后立即发送邮件方法
     * 
     * @call by : dialog.optionDialog.TabMail
     */
    public abstract void setImmediateSendMail(
            boolean isImmediateSend);

    /**
     * * 设置是否检查新邮件
     * 
     * @call by : dialog.optionDialog.TabMail
     */
    public abstract void setCheckMail(
            boolean isCheck);

    /**
     * * 设置检查新邮件的时间间隔
     * 
     * @call by : dialog.optionDialog.TabMail
     */
    public abstract void setSpantime(
            int spanTime);

    /**
     * * 取得邮件格式
     * 
     * @call by : dialog.optionDialog.TabMail
     */
    public abstract int getMailFormat();

    /**
     * * 设置邮件格式
     * 
     * @parmam prmMailFormatIndex : 0 为 HTML; 1 为 TEXT;
     * @call by : dialog.optionDialog.TabMail
     */
    public abstract void setMailFormat(
            int prmMailFormatIndex);

//    /**************************************/
//    /* &&&&&& 日历选项卡 &&&&& */
//    /**************************************/
//    /** * 星期日 */
//    public abstract boolean getSunday();
//
//    /** * 星期日 */
//    public abstract void setSunday(
//            boolean isSun);
//
//    /** * 星期一 */
//    public abstract boolean getMonday();
//
//    /** * 星期一 */
//    public abstract void setMonday(
//            boolean isMon);
//
//    /** * 星期二 */
//    public abstract boolean getTuesday();
//
//    /** * 星期二 */
//    public abstract void setTuesday(
//            boolean isTue);
//
//    /** * 星期三 */
//    public abstract boolean getWendsday();
//
//    /** * 星期三 */
//    public abstract void setWendsday(
//            boolean isWen);
//
//    /** * 星期四 */
//    public abstract boolean getThursday();
//
//    /** * 星期四 */
//    public abstract void setThursday(
//            boolean isThu);
//
//    /** * 星期五 */
//    public abstract boolean getFirday();
//
//    /** * 星期五 */
//    public abstract void setFirday(
//            boolean isFir);
//
//    /** * 星期六 */
//    public abstract boolean getSateday();
//
//    /** * 星期六 */
//    public abstract void setSateDay(
//            boolean isSat);
//
//    /** * 每周的第一天 */
//    public abstract int getFirstDayWeek();
//
//    /** * 每周的第一天 */
//    public abstract void setFirstDayWeek(
//            int firstDayWeek);
//
//    /** * 每年的第一周 */
//    public abstract int getFirstWeekYear();
//
//    /** * 每年的第一周 */
//    public abstract void setFirstWeekYear(
//            int firstWeekYear);
//
//    /** * 上午开始时间 */
//    public abstract int getStartAMTime();
//
//    /** * 开始时间 */
//    public abstract void setStartAMTime(
//            int startTime);
//
//    /** * 结束时间 */
//    public abstract int getEndAMTime();
//
//    /** * 时间 */
//    public abstract void setEndAMTime(
//            int endTime);
//
//    /** * 下午开始时间 */
//    public abstract int getStartPMTime();
//
//    /** * 开始时间 */
//    public abstract void setStartPMTime(
//            int startTime);
//
//    /** * 结束时间 */
//    public abstract int getEndPMTime();
//
//    /** * 时间 */
//    public abstract void setEndPMTime(
//            int endTime);
//
//    /** * 显示周数 */
//    public abstract boolean getDisplayWeeks();
//
//    /** * 显示周数 */
//    public abstract void setDisplayWeeks(
//            boolean isDisplayWeeks);
//
//    /** * 选取背景色 */
//    public abstract void setSelectedBackColor(
//            int colorIndex);
//
//    /** * 自动接收 */
//    public abstract boolean getAutoReceive();
//
//    /** * 接收 */
//    public abstract void setAutoReceive(
//            boolean isAutoReceive);
//
//    /** * 拒绝自动冲突 */
//    public abstract boolean getAutoCollision();
//
//    /** * 拒绝自动冲突 */
//    public abstract void setAutoCollision(
//            boolean isAutoCollision);
//
//    /** * 自动接收 */
//    public abstract boolean getAutoRepeat();
//
//    /** * 自动接收 */
//    public abstract void setAutoRepeat(
//            boolean isAutoRepeat);
//
//    /** * 当前标签 */
//    public abstract String getCurrsorLabel();
//
//    /** * 当前标签 */
//    public abstract void setCurrsorLabel(
//            String currsorLabel);
//
//    /** * 取得当前时区 */
//    public abstract int getCurrsorZone();
//
//    /** * 设置当前时区 */
//    public abstract void setCurrsorZone(
//            int currsorZone);
//
//    /** * 调整当前夏令时 */
//    public abstract boolean getCurrsorAdjust();
//
//    /** * 调整当前夏令时 */
//    public abstract void setCurrsorAdjust(
//            boolean isAdjust);
//
//    /** * 当前时间 */
//    public abstract String getCurrsorTime();
//
//    /** * 当前时间 */
//    public abstract void setCurrsorTime(
//            String currsorTime);
//
//    /** * 是否显示附加时区 */
//    public abstract boolean getDisplayAppendZone();
//
//    /** * 是否显示附加时区 */
//    public abstract void setDisplayAppendZone(
//            boolean isDisplayAppend);
//
//    /** * 附加标签 */
//    public abstract String getAppendLabel();
//
//    /** * 附加标签 */
//    public abstract void setAppendLabel(
//            String appendLabel);
//
//    /** * 附加时区 */
//    public abstract int getAppendZone();
//
//    /** * 附加时区 */
//    public abstract void setAppendZone(
//            int appendZoneIndex);
//
//    /** * 附加夏令时调整 */
//    public abstract boolean getAppendAdjust();
//
//    /** * 附加夏令时调整 */
//    public abstract void setAppendAdjust(
//            boolean isAdjust);
//
//    /**************************************/
//    /* &&&&&& 联系人选项卡 &&&&& */
//    /**************************************/
//    /** * 取得默认全名排序 */
//    public abstract int getAllNameOrder();
//
//    /** * 设置默认全名排序 */
//    public abstract void setAllNameOrder(
//            int orderIndex);
//
//    /** * 取得默认表示为顺序 */
//    public abstract int getDisplayAsOrder();
//
//    /** * 设置默认表示为顺序 */
//    public abstract void setDisplayAsOrder(
//            int orderIndex);
//
//    /** * 取得是否自动加入要答复的人 */
//    public abstract boolean getAutoAddToAnswer();
//
//    /** * 设置默认表示为顺序 */
//    public abstract void setAutoAddToAnswer(
//            boolean isAdd);
//
//    /** * 取得是否发送邮件是作为Card发送 */
//    public abstract boolean getSendCard();
//
//    /** * 设置是否发送邮件是作为Card发送 */
//    public abstract void setSendCard(
//            boolean isSend);
//
//    /**************************************/
//    /* &&&&&& 任务选项卡 &&&&& */
//    /**************************************/
//    /** * 取得过期任务 */
//    public abstract int getOverdueTask();
//
//    /** * 设置过期任务 */
//    public abstract void setOverdueTask(
//            int taskIndex);
//
//    /** * 取得已完成任务 */
//    public abstract int getFinishedTask();
//
//    /** * 设置已完成任务 */
//    public abstract void setFinishedTask(
//            int taskIndex);
//
//    /** * 是否给带有截至日期的任务设置提醒 */
//    public abstract boolean getEndDate();
//
//    /** * 设置给带有截至日期的任务设置提醒 */
//    public abstract void setEndDate(
//            boolean isRemind);
//
//    /** * 取得提醒时间 */
//    public abstract int getRemindTime();
//
//    /** * 设置提醒时间 */
//    public abstract void setRemindTime(
//            int timeIndex);
//
//    /** * 取得是否在任务列表保留更新副本 */
//    public abstract boolean getSaveUpdateCopy();
//
//    /** * 设置是否在任务列表保留更新副本 */
//    public abstract void setSaveUpdateCopy(
//            boolean isSaveUpdateCopy);
//
//    /** * 取得是否分配的任务完成后发送状态报告 */
//    public abstract boolean getSendStaticReport();
//
//    /** * 设置是否分配的任务完成后发送状态报告 */
//    public abstract void setSendStaticReport(
//            boolean isSend);
//
//    /**************************************/
//    /* &&&&&& 日记选项卡 &&&&& */
//    /**************************************/
//    /** * 取得字形 */
//    public abstract int getFontStyle();
//
//    /** * 设置字形 */
//    public abstract void setFontStyle(
//            int styleIndex);
//
//    /** * 取得字体颜色 */
//    public abstract int getFontColor();
//
//    /** * 设置字体颜色 */
//    public abstract void setFontColor(
//            int colorIndex);
//
//    /** * 取得背景 */
//    public abstract int getBackColor();
//
//    /** * 设置背景 */
//    public abstract void setBackColor(
//            int colorIndex);
//
//    /** * 取得字号 */
//    public abstract int getFontSize();
//
//    /** * 设置字号 */
//    public abstract void setFontSize(
//            int fontSizeIndex);
//
//    /** * 取得当前密码 */
//    public abstract String getCurrsorPassword();
//
//    /** * 设置当前密码 */
//    public abstract void setCurrsorPassword(
//            String password);
//
//    /**************************************/
//    /* &&&&&& 其他选项卡 &&&&& */
//    /**************************************/
//    /** * 取得是否退出时清空已删除邮件 */
//    public abstract boolean getClearHaveDelMail();
//
//    /** * 设置是否退出时清空已删除邮件 */
//    public abstract void setClearHaveDelMail(
//            boolean isDel);
//
//    /** * 取得定位到 */
//    public abstract int getGotoPos();
//
//    /** * 设置定位 */
//    public abstract void setGotoPos(
//            int posIndex);
//
//    /** * 取得是否永久删除项目前提出警告 */
//    public abstract boolean getDelWarning();
//
//    /** * 设置是否永久删除项目前提出警告 */
//    public abstract void setDelWarning(
//            boolean isDel);
//
//    /** * 取得存档路径 */
//    public abstract String getSavePath();
//
//    /** * 设置存档路径 */
//    public abstract void setSavePath(
//            String savePath);
//
//    /** * 取得是否在预览窗口中将邮件标记为已读 */
//    public abstract boolean getSginHaveRead();
//
//    /** * 设置是否在预览窗口中将邮件标记为已读 */
//    public abstract void setSginHaveRead(
//            boolean isSgin);
//
//    /** * 取得等待时间 */
//    public abstract int getWaitTime();
//
//    /** * 设置等待时间 */
//    public abstract void setWaitTime(
//            int waitTime);
//
//    /** * 取得当选定内容更改时将项目标记为已读 */
//    public abstract boolean getSginHaveReadWhenSelected();
//
//    /** * 设置当选定内容更改时将项目标记为已读 */
//    public abstract void setSginHaveReadWhenSelected(
//            boolean isSelected);
//
//    /**************************************/
//    /* &&&&&& 查找对话盒 &&&&& */
//    /**************************************/
//    /** * 取得查找文字的历史 */
//    public abstract String getFindHistory();
//
//    /** * 保存查找文字的历史 */
//    public abstract void setFindHistory(
//            String findHistory);
//
//    /** 设置分隔高度比 */
//    public abstract void setSplitHeightScale(
//            double scale);
//
//    /** 获取分隔高度比 */
//    public abstract double getSplitHeightScale();
//
//    public int getSplitHeight() {
//        return sizeOfFirstPart;
//    }
//
//    public void setSplitHeight(
//            int prmInnerSize) {
//        sizeOfFirstPart = prmInnerSize;
//    }
//
//    int sizeOfFirstPart;
//    // *********************************************************
//    // ----------------------选项方法End------------------------
//    // *********************************************************
//
//
//    /*
//     * new Hashtable() { public synchronized String toString() { int max = size() - 1; StringBuffer buf = new
//     * StringBuffer(); java.util.Iterator it = entrySet().iterator(); buf.append("{"); for (int i = 0; i <= max; i++) {
//     * java.util.Map.Entry e = (java.util.Map.Entry) (it.next()); Object key = e.getKey(); Object value = e.getValue();
//     * buf.append((key == this ? "(this Map)" : key) + "=" + (value == this ? "(this Map)" : value)); if (i < max)
//     * buf.append("; "); } buf.append("}"); return buf.toString(); } };
//     */
//
    public abstract String getInstalledFunctions();

    public abstract String getInstalledAppNames();

//    public abstract Vector getMenuText(
//            String prmText);
//
//    public abstract Vector getMenuMnem(
//            String prmText);
//
//    public abstract Vector getActionName(
//            String prmText);
//
//    // //////////////////////////////////////////////////////////////////////
//
//    private void initTabColor() {
//        // Note：应该先从用户设置中取，取不出再设下列的默认值。
//        tabBGList.add(PIMPool.pool.getColor(16, 65, 99)); // 日历
//        tabBGList.add(PIMPool.pool.getColor(0, 97, 99)); // 任务
//        tabBGList.add(PIMPool.pool.getColor(0, 97, 57)); // 联系人
//        tabBGList.add(PIMPool.pool.getColor(132, 130, 57)); // 日记
//        tabBGList.add(PIMPool.pool.getColor(165, 97, 82)); // 收件箱
//        tabBGList.add(PIMPool.pool.getColor(165, 97, 82)); // 发件箱
//        tabBGList.add(PIMPool.pool.getColor(165, 97, 82)); // 已发送项
//        tabBGList.add(PIMPool.pool.getColor(132, 60, 99)); // 已删除项
//        tabBGList.add(PIMPool.pool.getColor(153, 51, 102)); // 草稿（暗紫红）
//        // tabBGList.add(PIMPool.pool.getColor(102,102,153));
//        // tabBGList.add(PIMPool.pool.getColor(153,51,0));
//        // tabBGList.add(PIMPool.pool.getColor(128,128,0));
//        // tabBGList.add(PIMPool.pool.getColor(255,153,0));
//        // tabBGList.add(PIMPool.pool.getColor(255,204,153));
//        // tabBGList.add(PIMPool.pool.getColor(255,153,204));
//        // tabBGList.add(PIMPool.pool.getColor(128,0,0));
//        // tabBGList.add(PIMPool.pool.getColor(255,102,0));
//        // tabBGList.add(PIMPool.pool.getColor(153,204,0));
//        // tabBGList.add(PIMPool.pool.getColor(0,128,0));
//        // tabBGList.add(PIMPool.pool.getColor(0,128,128));
//        // tabBGList.add(PIMPool.pool.getColor(51,51,153));
//        // tabBGList.add(PIMPool.pool.getColor(153,204,255));
//        // tabBGList.add(PIMPool.pool.getColor(128,128,128)); //灰色
//        // tabBGList.add(PIMPool.pool.getColor(51,51,0)); //发黑的青苔
//        // tabBGList.add(PIMPool.pool.getColor(0,51,0)); //发黑的青苔
//        // tabBGList.add(PIMPool.pool.getColor(51,102,255)); //嫩蓝
//        // tabBGList.add(PIMPool.pool.getColor(128,0,128)); //亮紫红
//        // tabBGList.add(PIMPool.pool.getColor(150,150,150));
//        // tabBGList.add(PIMPool.pool.getColor(255,0,255)); //cyrix
//        // tabBGList.add(PIMPool.pool.getColor(255,204,0)); //黄色
//        // tabBGList.add(PIMPool.pool.getColor(192,192,192));
//        // tabBGList.add(PIMPool.pool.getColor(255,255,153));
//        // tabBGList.add(PIMPool.pool.getColor(204,255,204));
//        // tabBGList.add(PIMPool.pool.getColor(204,255,255));
//        // tabBGList.add(PIMPool.pool.getColor(0,0,128));
//        // Note：应该先从用户设置中取，取不出再设下列的默认值。
//        tabFGList.add(PIMPool.pool.getColor(255, 255, 255)); // 日历
//        tabFGList.add(PIMPool.pool.getColor(255, 255, 255)); // 任务
//        tabFGList.add(PIMPool.pool.getColor(198, 255, 214)); // 联系人
//        tabFGList.add(PIMPool.pool.getColor(247, 243, 181)); // 日记
//        tabFGList.add(PIMPool.pool.getColor(255, 227, 181)); // 收件箱
//        tabFGList.add(PIMPool.pool.getColor(255, 227, 181)); // 发件箱
//        tabFGList.add(PIMPool.pool.getColor(255, 227, 181)); // 已发送项
//        tabFGList.add(PIMPool.pool.getColor(255, 195, 206)); // 已删除项
//        tabFGList.add(PIMPool.pool.getColor(255, 255, 255));
//        tabFGList.add(PIMPool.pool.getColor(255, 255, 255));
//        tabFGList.add(PIMPool.pool.getColor(255, 255, 255));
//        tabFGList.add(PIMPool.pool.getColor(255, 255, 255));
//        tabFGList.add(PIMPool.pool.getColor(255, 255, 255));
//        tabFGList.add(PIMPool.pool.getColor(255, 255, 255));
//        tabFGList.add(PIMPool.pool.getColor(255, 255, 255));
//        tabFGList.add(PIMPool.pool.getColor(255, 255, 255));
//        tabFGList.add(PIMPool.pool.getColor(255, 255, 255));
//    }

    /** 用到的Hashtable需要重载toString方法，原来用的是匿名类，现在改为内部静态类 */
    private static class HashtableToString extends Hashtable {
        /** 重载toString方法* @retrun String 将Hashtable的内容变为要求的字串返回 */
        @Override
        public synchronized String toString() {
            int max = size() - 1;
            StringBuffer buf = new StringBuffer();
            java.util.Iterator it = entrySet().iterator();

            buf.append("{");
            for (int i = 0; i <= max; i++) {
                Entry e = (Entry) (it.next());
                Object key = e.getKey();
                Object value = e.getValue();
                buf.append((key == this ? "(this Map)" : key) + "=" + (value == this ? "(this Map)" : value));

                if (i < max)
                    buf.append("; ");
            }
            buf.append("}");
            return buf.toString();
        }
    }

//    private Icon[] moodImgs;
//
//    private Icon[] weatherImgs;
//    public Vector<String> APPCapsVec;
//    public Vector<String> APPNameVec;
//    private ArrayList<Color> tabBGList = new ArrayList<Color>();
//    private ArrayList<Color> tabFGList = new ArrayList<Color>();
//
//    public Color appBG = PIMPool.pool.getColor(189, 121, 106);
//    /** 日期选择区是否浮动 */
//    private boolean datePaneFloat;
//
//    /** 日期选择区是否可见 */
//    private boolean datePaneVisible;
//
//    /** 日期选择区浮动时的大小 */
//    private Dimension datePaneFloatSize;
//
//    /** 日期选择区固定时的大小 */
//    private Dimension datePaneFixedSize;
//
//    /** 日期选择区浮动时的位置 */
//    private Point datePaneFloatLocation;

    /** DB port is customizable, in case the default port 9001 is occupied */
	public abstract String getDBPort();

	public abstract boolean getDeleteOriginalTransaction();

	public abstract String getDBHost();
}
