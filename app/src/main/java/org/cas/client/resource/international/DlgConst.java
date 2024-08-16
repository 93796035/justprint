package org.cas.client.resources.international;

public interface DlgConst
{
    int IMAGE_SIZE = 32;	// 正方行图片的边大小

    //Product Info
    String ProductInfo = "ProductInfo";
    // the title of dialog
    String DlgTitle = "WARNING!";
    
    //For ConfirmDialog
    String CONFIRM_TITLE = "确认密码";
    String CONFIRM_LABEL = "再次输入密码(R):";
    String CONFIRM_CHANGE_TITLE = "确认密码:";
    String CONFIRM_NOTE = "警告！密码若被遗忘或丢失，将无法恢复。建议将密码清单保存到安全位置。";

    String NodeDelNotAllow = "当前选中的节点是系统预设的节点，不可以删除。用户只可以对自己增加的节点进行删除操作。";

    
    String OK = "OK";			// text of ok button
    String CANCEL = "Cancel"; 		// text of cancel button
    String APPLY = "应用(A)";	// text of apply button
    String YES = "是(Y)";
    String NO = "否(N)";
    String RETRY_BUTTON = "重试(R)";
    String HELP_BUTTON = "帮助(H)";
    String YESTOALL_BUTTON = "全是(A)";
    String DISABLE_BUTTON = "停用宏(D)";
    String ENABLE_BUTTON = "使用宏(E)";
    String CONTINUE_BUTTON = "继续(C)";
    String END_BUTTON = "结束(E)";
    String ABORT_BUTTON = "终止(A)";
    String IGNORE_BUTTON = "忽略(I)";
    String UPDATE_BUTTON = "添加";
    String UNUPDATE_BUTTON = "不更新";
    String FIX_BUTTON = "调整(F)";
    String BUY_BUTTON = "在线购买";
    String KEY_BUTTON = "输入CD－Key";
    String FINISH_BUTTON = "Close"; 
    String NO_FILE = "Please check and make sure all necessary field has been set value.";
    String NO_KEY = "系统无法找到相关的错误号码：";

    
    // Contacts...
    String CONTACTS = "联系人(C)...";
    String CATEGORIES = "Category...";
    /* The follow contents are used for option dialog */
    /* name of option dialog */
    String OPTION_DIALOG = "选项";
    
    /* reminder time */
    String REMINDER_STRING = "Default reminder:";
    /* the data of reminder time */
    String[] TIME_ARRAY =
    {
        "0 minutes",  "5 minutes", "10 minutes", "15 minutes",
        "30 minutes", "1 hour",    "2 hours",    "3 hours",
        "4 hours",    "5 hours",   "6 hours",    "7 hours",
        "8 hours",    "9 hours",   "10 hours",   "11 hours",
        "0.5 days",   "1 day",     "2 days"
    };
    /* calendar button to enter calendar dialog */
    String CALENDAR_BUTTON = "Calendar com.just.print.app.Options...";
    
    /* full name of week day */
    String[] DAY_NAME =
    {
        "星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六"
    };
    /* first week in year */
    String FIRST_WEEK = "First week of year:";
    /* the data of the first week in a year */
    String[] WEEK_KIND =
    {
        "Starts on Jan 1",
        "First 4-day week",
        "First full week"
    };
    /* start work time */
    String START_TIME = "开始时间:";
    /* end work time */
    String END_TIME = "结束时间:";
    
    /* title of calendar dialog */
    String OPTIONS_TITLE = "Calendar options";
    /* show week numbers in calendar */
    String SHOW_NUMBER = "Show week numbers in the Date Navigator";
    /* the model to send meeting request */
    String SEND_MEETING = "Send meeting requests using iCalendar by default";
    /* background color of calendar information view */
    String BACKGROUND_COLOR = "Background color:";
    /* resource scheduling */
    String RESOURCE_SCHEDULE = "Resource Scheduling...";
    
    /* apply button */
    String TEXT_APPLY = "Apply";
    
    /* The follow contents are used for new folder dialog dialog */
    /* creat a new node into navigator tree */
    String NEW_FOLDER_DIALOG = "建立新文件夹";
    /* name for new node */
    String FOLDER_NAME = "名称:";
    /* type for new node */
    String FOLDER_TYPE = "Folder contains:";
    /* select parent node for new node */
    String FOLDER_LIST = "选择新建文件夹所在的位置:";
    
    /* appointment type */
    String APPOINTMENT_ITEM = "Appointment Items";
    /* contact type */
    String CONTACT_ITEM = "Contact Items";
    /* journal type */
    String JOURNAL_ITEM = "Journal Items";
    /* mail type */
    String MAIL_ITEM = "Mail Items";
    /* note type */
    String NOTE_ITEM = "Note Items";
    /* task type */
    String TASK_ITEM = "Task Items";
    /* diary type */
    String DIARY_ITEM = "Diary Items";
        
    /* The follow contents are used for copy folder dialog */
    /* copy a node */
    String COPY_FOLDER_DIALOG = "复制文件夹";
    /* the destination for coping */
    String COPY_FOLDER_LABEL = "为被复制的文件夹选择目标位置:";
    
    /* The follow contents are used for move folder dialog */
    /* move a node */
    String MOVE_FOLDER_DIALOG = "移动文件夹";
    /* the destination for moving */
    String MOVE_FOLDER_LABEL = "为被移动的文件夹选择目标位置:";
    
    /* The follow contents are used for time zone dialog */
    /* time zone */
    String TIME_ZONE_TITLE = "Time Zone";
    /* current time zone */
    String CURRENT_ZONE = "Current time zone";
    /* swap time zone */
    String SWAP_BUTTON = "Swap Time Zones";
    /* labe */
    String ZONE_NAME = "Label:";
    /* select time zone */
    String ZONE_ITEM = "Time zone:";
    /* adjust for daylight */
    String DAYLIGHT_TIME = "Adjust for daylight saving time";
    /* show additional time zone */
    String ADDTIONAL_ZONE = "Show an additional time zone";
    
    /* The follow contents are used for goto date dialog */
    /* go to date */
    String GOTOTITLE = "转到日期";
    String SELECT_DATE = "日期:";
    /* the view model to show the selected date */
    String SELECT_MODEL = "转到:";
    /* calendar day view */
    String DAY_VIEW_MODEL = "日视图";
    /* calendar work week view */
    String WORK_VIEW_MODEL = "Work Week Calendar";
    /* calendar week view */
    String WEEK_VIEW_MODEL = "周视图";
    /* calendar month view */
    String MONTH_VIEW_MODEL = "月视图";
    /* calendar year view */
    String YEAR_VIEW_MODEL = "Year Calendar";
    /* today button */
    String TODAY_BUTTON = "今天";
    
    /* The follow contents are used for select folder dialog */
    /* the title of dialog */
    String SELECT_FOLDER = "Select Folder";
    /* folders list */
    String FOLDERS_LIST = "Folders:";
    /* clear all */
    String CLEAR_ALL = "Clear All";
    
    // The follow contents are used for contact dialog
    //
    String CONTACT_ACTIVITY_DISPLAY = "显示(W)";
    //
    String CONTACT_ALL_SELECTFROM = "选自(C)";
    
    String[] ACTIVE_ALL_ITEMS = 
    {
        "电子邮件",
        "任务",
        "约会与会议",
        "联系人",
        "手稿"
//        "所有项目"
    };
    

   
    final String VALUE = "值"; // 第四页,和表格有关:第二项
    final String PROPERTY = "属性";// 第四页,和表格有关:第一项
    
    String NEWMAIL = "<新建电子邮件>";
    String DIGITAL_ID = "添加、删除和查看此联系人的数字ID。";
    String SELECT_ADDRESS = "选择电子邮件地址(E)";
    String ASSOCIATE_ID = "与选定的电子邮件地址相关联的数字ID(D):";
    String PROPERTY_SECURE = "属性(R)";
    String DELETE = "DELETE";
    String SET_DEFAULT = "设为默认值(S)";
    String IMPORT = "导入(M)...";
    String EXPORT = "导出(X)...";
    String DEFAULT = "(默认)";
    String ID_FILE = "数字ID文件";
    String SELECT_FILE = "选定要导入的数字ID的文件";
    String SPECIFY_FILE = "指定数字ID文件名";
    
    //for DateSelectDialog
    String SELECTDATE = "选择日期";
    String DATE_DESCRIPTION1 = "请通过点击文本框右侧的下拉按钮，选择目标日期。";
    String DATE_DESCRIPTION2 = "也可以在文本框中直接输入日期，格式为\"年－月－日\"。";
    
    String FORMATERROR = "Invalid input format found, please correct and try again.";
    String SAVEBEFORECLOSE = "系统检测到对话框内的内容发生了改变，是否需要再关闭前保存改变的内容？";
	String InvalidInput = "Invalid input! Please input again.";

    String UserNameInUsed = "您刚才输入的用户名已经被他人使用，请重新输入。";
    String UserNameNotExist = "您刚才输入的用户名不存在，请重新输入。";
    String InvalidPassword = "Invalid password! please try again.";
    String UNNORMALCLOSED = "Please make sure the previouse app closed properly before open a new one.";
    String DEFAULTDBISREADY = "默认数据库创建完成！";
    String COMFIRMDELETEACTION = "该动作将导致选定的记录被删除，您确定吗？\n请选择[是]确定删除，或选择[否]取消删除。";
    

    String CategoryNameInUse = "The category name input is alreay used, please choose an other.";
}
