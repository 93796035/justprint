package org.cas.client.resource.international;

/***/
public interface PaneConsts {
    String REPETITIONDEFINE = "设定重复周期";
    /***************************************************************/
    // 错误,警告信息文字字段定义如下
    /***************************************************************/
    public static final String SELECTFOLDDG = "请选择一个本地联系人结点或其子结点，因新建记录是个联系人的记录！";

    public static final String SENDITEMAN_1 = "收件人不能为空";

    public static final String SENDITEMAN = "任务或约会不能只发给自已";

    public static final String ACHIVEAN = "保存对话盒内部出错!";

    public static final String NEWTEMPLATEAN = "无法打开新建对话框,对话框内部出现错误";

    public static final String OPENAN = "报告已损坏，无法处理";

    public static final String SAVEASAN = "保存对话盒内部出错";

    // 需要确认 user021
    public static final String GOTOTODAYAN = "时间跳转出错";

    public static final String MAILFRAME_1 = "是否需要保存？";

    public static final String SENDAN = "这个帐户没有数字ID，不能发送带数字签名的邮件。点确定发送不签名邮件，点取消返回。";

    public static final String MAILASENDER = "邮件发送失败";

    public static final String MAILSENDER = "发件箱中的部分信件没有指定有效的帐户。是否使用默认帐户发送这些信件？";

    public static final String LOCALMAILRULE = "发现错误的邮件规则...";

    public static final String CERTIFICATEDG = "“证书有效期”域内请输入一个整数";

    public static final String LOCALMAILRULE_2 = "生成证书成功";

    public static final String CERTMANAGEDG = "用于弹一个对话盒，显示证书详细内容";

    public static final String CONTACTSECUREPL = "敬请期待";

    public static final String FILTERDG = "数值无效";

    public static final String PIMUTILITY = "缺少默认帐号";

    public static final String FILTERDG_2 = "日期格式错误";

    public static final String DATAERROR = "约会结束时间不能早于开始时间";

    public static final String SENDFAILTOADDRESS = "邮件地址错误，发送失败";

    public static final String PLUGINMISSINGMSG1 = "系统发现插件";
    public static final String PLUGINMISSINGMSG2 = "出现问题，与之相关的功能不再有效，直至该插件被重新正确添加。";
    /*****************************************************************/

    /* path of images */
    public static final String IAMGE_PATH = "/org/cas/client/resource/img/pim/";

    // ------------------------------------------------------------BaseBook上标签的文字
    String HEAD_PAGE = "企业资源计划";
    String RECYCLE = "回收站";
    String TASK = "任务";
    String CONTACTS = "联系人";

    String INBOX = "收件箱";
    String OUTBOX = "发件箱";
    String SENT_ITEM = "已发送项";
    String DRAFT_ITEM = "草稿箱";
    String DELETE_ITEM = "已删除项";
    String NEW_FOLDER = "新建文件夹";

    String ROOTCAPTION = "[PIM,";
    String PIM_PATH = ROOTCAPTION + HEAD_PAGE + ']';
    int PIM_PATH_ID = 1;
    String DFT_FONT = "DefaultFont";

    // ------------------------------------------------------------------------
    /* width of navigator tree's title */
    int TREE_TITLE_HEIGHT = 20;
    /* height of navigator tree's title */
    int TREE_BUTTON_WIDTH = 20;
    // -----------------------------------------------------------------
    /* maxinum week numbers in a year */
    int MAX_WEEK = 52;

    String TITLE = "源道 EIM";
    String HELP_DIALOG_TITLE = "关于(A)...";
    String HELP_DIALOG_CONTENT2 = "版权 2003-2018 源道software";
    /* name of navigator pane */
    String TREE_FOLDER_LIST = "导航面板";
    // 日期选择区的名称。
    String SELECT_DATE_AREA = "日期选择区";
    /* save the customized content of activities today */
    String SAVE_CHANGES = "保存修改";
    /* cancel the customized content of activities today */
    String CALCEL_CHANGES = "取消";

    /* name of meeting request table in database */
    String MEETING_REQUEST = "会议请求";
    // 项目的名称
    String ITEMS = "项目";
    String PRINT_ITEMS = "打印内容：";
    /* Icon definitions */
    /* activities today item in navigator tree */
    String ACTIVE_TODAY = "今日";
    /* contact item in navigator tree */
    String CONTACTS_ICON = "联系人";
    /* task item in navigator tree */
    String TASK_ICON = "任务";
    /* note item in navigator tree */
    String NOTES_ICON = "便笺";
    /* deleted item in navigator tree */
    String JOURNAL_ICON = "自动日记";
    /* clamp */
    String CLAMP_ICON = "Binder Clamp";
    /* note page image */
    String NOTE_PAPER = "Note Paper";
    /* notebook image */
    String NOTE_BOOK = "Note Book";
    /* Activities today title image */
    String TODAY_TITLE = "Activities Today Title";
    /* Activities today messages */
    String MESSAGE_ITEM = "邮件";

    /* tree root name */
    String PERSONAL_FOLDERS = "Personal Folders";
    /* set days shown in calendar */
    String CALENDAR_DAYS = "Show the number of days in calendar: ";
    /* show folders */
    String SHOW_FOLDERS = "Show these folders: ";
    /* select folders */
    String CHOOSE_FOLDERS = "选择目录...";

    /* look event up */
    String EVENT_UP = "Event Up";
    /* look event down */
    String EVENT_DOWN = "Event Down";
    /* 32 x 32 work week image */
    String WORK_WEEK_ICON = "工作周";
    /* 32 x 32 calendar image */
    String CALENDAR_OPTION_ICON = "Calendar Icon";

    // the icons of calendar view
    /* bell */
    String REMINDER_ICON = "Reminder";
    /* recurrence */
    String RECURRENCE_ICON = "Recurrence";
    /* meeting */
    String MEETING_ICON = "会议";
    /* meeting request */
    String REQUEST_ICON = "会议请求";
    /* private */
    String PRIVATE_ICON = "私人";
    /* send mail */
    String SEND_MAIL = "发送邮件";
    String SAVE_MAIL = "保存";
    /* send and receive all mails */
    String SENDR_ALL_MAILS = "发送/接收邮件";
    String REPLAY = "回复";
    String REPLAYALL = "回复全部";
    String FORWORD = "转发";

    String DAY_VIEW = "天视图";
    String WEEK_VIEW = "周视图";
    String MONTH_VIEW = "月视图";

    /* send mail again */
    String SEND_MAIL_AGAIN = "再次发送";

    String CHECKTIP = "检查名字";
    String FEEDBACK = "需要已读回执";
    String SETPRIORITYTIP = "设置优先权";
    String HIGH_PRIORITY_TIP = "设置为高优先级.";
    String LOW_PRIORITY_TIP = "设置为低优先级.";
    String ATTACHFILETIP = "附加文件";
    String FROMTEXT = "发件人：";
    String TOTEXT = "收件人：";
    String CCTEXT = "抄送人：";
    String BCCTEXT = "暗送人：";
    String BCCTIP = "显示暗送人";
    String INSERTPIC = "插入图片";
    String INSERTHYP = "插入超链接";
    String FROMTIP = "显示发送人";
    String SUBJECTTEXT = "主题：";
    String NEW = "新建";
    String SANDR = "发送/接收(C)";
    String OPEN = "打开";
    String SAVE = "保存";
    String COPY = "复制";
    String CUT = "剪切";
    String PASTE = "粘贴";
    String BOLD = "加粗";
    String ITALIC = "倾斜";
    String UNDERLINE = "加下划线";
    String CENTER = "居中";
    String LEFT = "居左";
    String RIGHT = "居右";
    String FG = "字体颜色";
    String FS = "字体大小";
    String FONT = "字体";
    String UNDO = "撤销";
    String REDO = "恢复";
    String SONG = "宋体";
    String ENCODE = "编码";
    String HYPERLINK = "超链接...";
    String INSERDPIC = "插入图片...";
    String SETBACKGROUND = "设置背景";
    String SETBKCOLOR = "设置背景色";
    String SETBKPIC = "设置背景图片...";
    String INSERTLINE = "插入水平线";
    String INSERTFONT = "字体...";
    String SAVEMODEL = "另存为模板...";
    String SOURCECODE = "查看源文件(S)";
    String TEMPLATE_FILE = "模板文件";
    String PICTURE_FILE = "图片文件";

    String HYPERLINKINFOR = "超链接信息";
    String HYPERLINKSTYLE = "类型(T)";
    String HYPERLINKADRESS = "地址(U)";
    String HYPERLINK_OTHER = "(其它)";
    String HYPERLINK_OK = "确定";
    String HYPERLINK_CANCEL = "取消";

    String IMAGE = "图片";
    String IMAGESOURCE = "图片来源(P):";
    String REPLACEWORD = "替换文字(T):";
    String BROWSER = "浏览(R)...";
    String LAYOUT = "布局";
    String ALIGNMENT = "对齐(A)";
    String BORDERWIDTH = "边框宽度(B)";
    String SPACING = "间隔";
    String HORIZONTAL = "水平(H)";
    String VERTICAL = "垂直(V)";

    String ATTACHTEXT = "附加：";
    String DEFAULT_MAIL_TITLE = "新建邮件";
    String NOCAPTION_MAIL_TITLE = "无主题邮件";
    String HIGHPRIORITYTEXT = "此信息为高优先级.";
    String LOWPRIORITYTEXT = "此信息为低优先级.";
    String HIGHPRIORITYITEM = "高优先级";
    String NORMALPRIORITYITEM = "普通优先级";
    String LOWPRIORITYITEM = "低优先级";

    // mail info
    String SEND_TIME = "发送时间：";
    String CC_ITEM = "抄送：";
    String BCC_ITEM = "暗送:";
    String BEPATIENT = "这一条的内容怎么这么多?!";

    // task info
    String START_TIME = "开始时间：";
    String END_TIME = "截至时间：";
    String TASK_STATUS = "状态：";
    String FINISH_PR = "完成率：";
    String FINISH_DATE = "完成日期：";
    String WORK_ALL = "工作总量：";
    String FACT_WORK = "实际工作：";
    String OWNER = "所有者：";
    String COMPANY = "单位：";
    String ACC_INFO = "记帐信息：";
    String WAY_LON = "里程：";
    String ANNOT_INFO = "附注信息：";

    /* 用于IMAP面板 */
    String SYNCHRONIZATION = "同步";
    String FOLDERS = "上的文件夹";
    String SETTINGSTR = "要更改脱机设置，请选择文件夹，然后单击“设置”";
    String SYNCHACCOUNT = "同步帐户";
    String DONTSYNCH = "不要同步(D)";
    String ALLMESSAGES = "所有邮件(A)";
    String NEWMESSAGES = "新邮件(M)";
    String MESSAGEHEAD = "邮件标题(H)";

    /* 用于本地文件夹视图 */
    String USELOCAL = "对POP帐户使用本地文件夹，并用它们对其它帐户上的邮件进行存档";
    String SENDANDRECEIVE = "发送和接收全部邮件";

    /* do not send mail */
    String DENY_SEND = "Do Not Send";

    // event kind
    /* tentative time */
    String TENTATIVE_TIME = "Tentative";
    /* busy tine */
    String BUSY_TIME = "忙";
    /* out of office time */
    String SPARE_TIME = "外出";
    /* no information */
    String FREE_TIME = "No Information";

    /** 日记的天气ButtonMenu的ToolTip */
    String WEATHER = "天气";
    /** 日记的心情ButtonMenu的ToolTip */
    String MOOD = "心情";
    /**
     * 日记的天气选择面板的ToolTip。
     */
    String WEATHERS[] = { "晴", "多云", "阴雨" };
    /**
     * 日记的心情选择面板的ToolTip。
     */
    String MOODS[] = { "愉快", "惊奇", "郁闷", "发彪", "受伤", "昏睡", "怕怕", "羞羞", "伤感", "玩笑" };

    String ALIGN[] = { "不设置", "左", "右", "文本上方", "正中央", "基线", "正下方", "下", "中", "上" };

    String[] FONT_DATAS = { "仿宋_GB2312", "华文中宋", "华文仿宋", "华文彩云", "华文新魏", "华文细黑", "华文行楷", "宋体", "宋体-方正超大字符集", "幼圆",
            "新宋体", "方正姚体", "方正舒体", "楷体_GB2312", "仿宋", "宋体", "楷体", "粗黑", "隶书", "黑体" };

    String[] ENCODE_DATAS = { "纯文本(不作编码转换)", "波罗的海字符(Windows)", "波罗的海字符(IBM)", "中欧字符(Mac)", "西里尔字符(KOI8_R)",
            "西里尔字符(Windows)", "东欧字符(ISO)", "东欧字符(Windows)", "希腊字符(Windows)", "希腊字符(IBM)", "希伯来字符(Windows)",
            "拉丁 9 字符(ISO)", "简体中文(GB18030)", "简体中文(GBK)", "简体中文(GB2312)", "简体中文(Windows)", "繁体中文(BIG5)", "繁体中文(CNS)",
            "繁体中文(Windows)", "繁体中文(IBM)", "日语(Shift-JIS)", "日语(JIS)", "日语(EUC)", "日语(Auto-Select)", "朝鲜语(EUC)",
            "朝鲜语(ISO)", "朝鲜语(Windows)", "朝鲜语(Johab)", "俄语(KOI8-R)", "俄语(IBM)", "泰国语(Windows)", "泰国语(IBM)", "泰国语(Mac)",
            "Unicode", "Unicode(Big_Endian)", "Unicode(UTF-8)", "Unicode(UTF-16)", "越南语(Windows)", "西欧字符(ISO)",
            "西欧字符(Windows)", "西欧字符(IBM)", "阿拉伯字符(Windows)", "阿拉伯字符(IBM)", "阿拉伯字符(Mac)" };

    String YEAR = "年";
    String MONTH = "月";

}
