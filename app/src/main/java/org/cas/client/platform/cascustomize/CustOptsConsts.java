package org.cas.client.platform.cascustomize;

import java.util.Date;

public interface CustOptsConsts {
    // -------------
    // 临时处理
    Date date = new Date();
    int year = date.getYear();
//    String string = CASUtility.EMPTYSTR + year;
//    String yearString = "20" + string.substring(string.length() - 2, string.length());
    String INSTALLPATH = "installpath";
    // 密码
    String USERNAME = "username";
    String USERTYPE = "CHRRY";
    String USERLANG = "lang";
    // -------------
    String DBPORT = "dbport";
    String DBHOST = "dbHost";
    /** 用于存取用户选择的打印机的名称 */
    String PRINTER = "printer";
    /** 每周的工作天数 */
    String WORK_DAYS_PER_WEEK = "WorkDaysAWeek";
    /** 每天的工作时数 */
    String WORK_HOURS_PER_DAY = "WorkHoursADay";

    // ********************* 以下为 **********************//
    // -------------- 选项对话盒字段值 ----------------//
    // ****************************************************//
    // 注 : 第一行为Key值 ; 第二行为默认值
    // =====================================//
    // ********* 邮件选项卡 ******** //
    // =====================================//
    /**
     * 邮件选项对话盒
     */
    // 邮件处理------------
    String SPLIT_SCALE = "SplitScale";
    // 信件打开方式
    String OPEN_TYPE = "Opentype";
    // 在答复和转发时关闭原始邮件
    String CLOSE_MAIL = "CloseMail";
    //
    boolean DEF_CLOSE = true;
    // 在已发送的邮件中保存副本
    String SAVE_MAIL_COPY = "SaveMailCopy";
    //
    boolean DEF_COPY = true;
    // 新邮件到达时给出通知
    String REMIND_MAIL_RECEIVED = "RemindMailReceived";
    //
    boolean DEF_REMIND = false;
    // 答复邮件时
    String ANSWER_MAIL = "AnswerMail";
    // 转发邮件时
    String RETURN_MAIL = "ReturenMail";
    // 编辑邮件时标记修订
    String SGIN_EDIT_MAIL = "SginEditMail";
    //
    boolean DEF_SGIN = true;
    // 高级----------------------
    // 未发送邮件的保存位置
    String NO_SAVE_POS = "NoSavePosition";
    //
    int DEF_NO = 2;
    // 已发送邮件的保存位置
    String HAVE_SAVE_POS = "HaveSavePosition";
    //
    int DEF_HAVE = 3;
    // 重要性
    String IMPORTANCE = "Importance";
    //
    int DEF_IMP = 1;
    // 敏感度
    String SENSITIVE = "Sensitive";
    // 自动检查姓名
    String AUTO_CHECK_NAME = "AutoCheckName";
    //
    boolean DEF_AUTO_CHECK = true;
    // 应答时从收件箱删除会议要求
    String DEL_MEET_REQ = "DeleteMeetingRequestion";
    //
    boolean DEF_DEL = true;
    // 跟踪---------------------------
    // 在到达时处理回执
    String DEAL_RETURN_RECEIPT = "DealReturnReceipt";
    //
    boolean DEF_DEAL_RECE = true;
    // 处理后将回执移至
    String MOVE_TO = "AfterDealReturnReceiptMoveTo";
    //
    boolean DEF_MOVE_TO = true;
    // 移至?????
    String WHERE_MOVE_TO = "WhereMoveto";
    //
    int DEF_WHERE = 4;
    // 请在阅读所有邮件后给除"已读"回执的请求
    String READ_REQUEST = "ReadAndRequest";
    //
    boolean DEF_READ = false;
    // 总是发送相应
    String ALWAYS_SEND = "AlwaysSend";
    //
    boolean DEF_ALWAYS_SEND = true;
    // 从不发送相应
    String ALWAYS_NOT_SEND = "AlwaysNotSend";
    //
    boolean DEF_NOT_SEND = false;
    // 发送相应前向我询问
    String SEND_REQUEST_ME = "RequestMeBeforeSend";
    //
    boolean DEF_REQUEST_ME = false;
    /* 连是否接后立即发送邮件 */
    String IS_IMMEDIATE_SEND_MAIL = "ImmediatelySendMail";
    //
    boolean DEF_SEND = true;
    /* 是否检查新邮件 */
    String CHECK_NEW_MAIL = "CheckNewMail";
    //
    boolean DEF_CHECK = true;
    /* 检查新邮件的时间间隔 */
    String CHECK_TIME_SPAN = "CheckTimeSpan";
    //
    int DEF_TIME_SPAN = 5;
    /* 邮件格式 */
    String MAIL_FORMAT = "MailFormat";
    /* 默认使用的模板 */
    String TEMPLETE = "MailTemplete";
    // =====================================//
    // ********* 日历选项卡 ******** //
    // =====================================//
    // sunday
    String SUNDAY = "Sunday";
    //
    boolean IS_SUN = false;
    // Monday
    String MONDAY = "Monday";
    //
    boolean IS_MON = true;
    // Tuesday
    String TUESDAY = "Tuesday";
    //
    boolean IS_TUE = true;
    // Wendsday
    String WENDSDAY = "Wendsday";
    //
    boolean IS_WEN = true;
    // Thursday
    String THURSDAY = "Thursday";
    //
    boolean IS_THU = true;
    // Firday
    String FIRDAY = "Firday";
    //
    boolean IS_FIR = true;
    // Sateday
    String SATEDAY = "Sateday";
    //
    boolean IS_SAT = false;
    // 一周的第一天
    String DAY_FIRST_WEEK = "dayFirstWeek";
    // 一年的第一周
    String WEEK_FIRST_YEAR = "weekFirstYear";
    // 开始时间
    String START_AM_TIME = "startAMTime";
    // 结束时间
    String END_AM_TIME = "endAMTime";
    String START_PM_TIME = "startPMTime";
    String END_PM_TIME = "endPMTime";
    //
    int DEF_END_TIME = 47;
    // 在日期选择区中显示周数
    String DISPLAY_WEEKS = "displayWeeks";
    //
    boolean DEF_DISPLAY_WEEKS = false;
    // 背景颜色
    String BACKGROUND_COLOR = "backgroundColor";
    //
    int DEF_BACK_COLOR_INDEX = 0;
    // 自动显示接收会议并处理取消通知
    String AUTO_RECEIVE_MEET = "autoReceiveMeeting";
    //
    boolean AUTO_RECEIVE = false;
    // 自动谢绝会议要求
    String AUTO_REFUSE_COLLISION_MEETING = "autoRefuseCollisionMeetig";
    //
    boolean AUTO_REFUSE_COLLISION = false;
    // 自动谢绝重复的会议要求
    String AUTO_REFUSE_REPEAT_MEETING = "autoRefuseRepeatMeeting";
    //
    boolean AUTO_REFUSE_REPEAT = false;
    /******** 时 区 对 话 盒 *********/
    // 当前标签
    String CURRSOR_LABEL = "currsorLabel";
    // 当前时区
    String CURROSR_ZONE = "currsorZone";
    //
    int DEF_CURROSR_ZONE = 19;
    // 调整时间
    String ADJUST_CURRSOR_TIME = "adjustTime";
    //
    boolean DEF_ADJUST_CURROSE = false;
    // 当前时间
    String CURROSR_TIME = "currsorTime";
    //
//    String DEF_CURROSR_TIME = CASUtility.EMPTYSTR + yearString + "/" + date.getMonth() + "/" + date.getDay() + "  "
//            + date.getHours() + ":" + date.getMinutes();
    // 显示附加时区
    String DISPLAY_APPEND_ZONE = "displayAppendZone";
    //
    boolean DEF_DISPLAY_APPEND = false;
    // 附加标签
    String APPEND_LABEL = "appendLabel";
    // 附加时区
    String APPEND_ZONE = "appendZone";
    //
    int DEF_APPEND_INDEX = 19;
    // 调整附加时间
    String ADJUST_APPEND_TIME = "adjustAppendTime";
    //
    boolean DEF_ADJUST_APPEND = false;
    // =====================================//
    // ********* 联系人选项卡 ******** //
    // =====================================//
    // 默认全名顺序
    String ALL_NAME_ORDER = "AllNameOrder";
    //
    int DEF_NAME_INDEX = 4;
    // 默认表示为顺序
    String DISPLAY_AS_ORDER = "DisplayAsOrder";
    //
    int DEF_DISPLAY_AS = 4;
    // 自动加入答复人
    String AUTO_ADD_ANSWER = "AutoAddAnswer";
    //
    boolean DEF_ADD_ANSWER = false;
    // 发送作为Card
    String SEND_AS_CARD = "SendAsCard";
    //
    boolean DEF_SEND_CARD = false;

    // =====================================//
    // ********* 任务选项卡 ******** //
    // =====================================//
    // 过期任务
    String OVERDUE_TASK = "OverdueTask";
    // 已完成任务
    String FINISHED_TASK = "FinishedTask";
    // 设置提醒
    String CONFIG_REMIND = "ConfigRemind";
    //
    boolean DEF_CONFIG_REMIND = true;
    // 提醒时间
    String REMIND_TIME = "RemindTime";
    // 保存副本
    String SAVE_TASK_COPY = "SaveTaskCopy";
    //
    boolean DEF_SAVE_TAST_COPY = true;
    // 发送状态报告
    String SEND_STATIC_REPORT = "SendStaticReport";
    //
    boolean DEF_SEND_REPORT = true;

    // =====================================//
    // ********* 日记选项卡 ******** //
    // =====================================//
    // 字形
    String FONT_STYLE = "FontStyle";
    // 字体颜色
    String FONT_COLOR = "FontColor";
    // 背景
    String BACK_COLOR = "BackColor";
    // 字号
    String FONT_SIZE = "FontSize";
    // 当前密码
    String CURRSOR_PASSWORD = "CurrsorPassword";
    // =====================================//
    // ********* 其他选项卡 ******** //
    // =====================================//
    // 退出清空已删除邮件
    String OUT_CLEAR_DELETED_MAIL = "OutClearDeletedMail";
    //
    boolean DEF_CLEAR = false;
    // 启动时定位到
    String GOTO = "Goto";
    // 永久删除项目前提相互警告
    String WARNING = "Warning";
    //
    boolean DEF_WARNING = false;
    // 存档路径
    String SAVE_PATH = "SavePath";
    //
//    String DEF_PATH = CASUtility.getPIMDirPath();
    // 在预览窗口中将邮件标记已读
    String SGIN_HAVE_READ = "SginHaveRead";
    //
    boolean DEF_SGIN_READ = true;
    // 等待时间
    String WAIT_TIME = "WaitTime";
    //
    int DEF_WAIT_TIME = 5;
    // 选定内容更改时将项目标记有已读
    String SGIN_HAVE_READ_WHEN_SELECTED = "SginHaveReadSelected";
    //
    boolean DEF_SGIN_READ_SELECTED = false;
    // =====================================//
    // ********* 查找对话盒 ******** //
    // =====================================//

    String FIND_HISTORY = "FindHistory";

    String LASTUPDATEMONTH = "LASTUPDATEMONTH";

    /** window horizontal coordinate */
    String CONTENT_POSITION_X = "Horizontal_ConPos";
    /** window vertical coordinate */
    String CONTENT_POSITION_Y = "Vertical_ConPos";
    /** window's width */
    String CONTENT_WIDTH = "ConPos_Width";
    /** window's height */
    String CONTENT_HEIGHT = "ConPos_Height";

    String MainBGImgPath = "MainBGImgPath";
    String MainBGIndex = "MainBGIndex";
	Object Delete_Original_Transaction = "Delete_Original_Transaction";
}
