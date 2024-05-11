package org.cas.client.platform.cascustomize;

interface PropertyName {
    /* 链接路径 */
    String LINK_STRING = " = ";

    String LATEST_FOLDER_ID = "latestFolderID";
    // The follow properties are used for layout information
    String WINDOW_STATE = "WindowState";
    /** window horizontal coordinate */
    String WINDOW_POSITION_X = "Horizontal_Coordinate";
    /** window vertical coordinate */
    String WINDOW_POSITION_Y = "Vertical_Coordinate";
    /** window's width */
    String WINDOW_WIDTH = "Window_Width";
    /** window's height */
    String WINDOW_HEIGHT = "Window_Height";

    /** 日期格式，用于读取用户自定义设置时的key值 */
    String DATE_FORMAT = "Date_Format";
    /** 缺省的日期格式 */
    int DEFAULT_DATE_FORMAT = 0;

    /** 表示为的首选格式，用于读取用户自定义设置时的key值 */
    String FILE_AS_FORMAT = "File_As_Format";
    /** 缺省的表示为的首选格式 */
    int DEFAULT_FILE_AS_FORMAT = 0;

    /** 是否显示远程控制面板，用于读取用户自定义设置时的key值 */
    String REMOTECTL_DLG_SHOW = "RemoteCtl_Dlg_Show";

    /** 日期选择区的高度 */
    String DATE_PANE_HIGHT = "Date_Pane_Hight";
    /** 缺省的日期选择区的高度 */
    int DEFAULT_DATE_PANE_HIGHT = 141;

    /** 当前应用的类型 */
    String ACTIVE_APP_TYPE = "ActiveApp";
    /** 当前路径 */
    String ACTIVE_FOLDER_PATH = "Active_Folder_Path";

    /** 所有应用的表名 */
    String ALL_TABLE_NAMES = "ALL_TABLE_NAMES";
    /** 应用的删除项表名 */
    String RECYCLEBIN_TABLE_NAMES = "RECYCLEBIN_TABLE_NAMES";
    /** 表字段数组名 */
    String TABLE_FIELD = "TABLE_FIELD";

    // keys for Contacts=======================================================================
    String CONTACTS_PHONE1 = "first phone number";
    //
    String CONTACTS_PHONE2 = "second phone number";
    //
    String CONTACTS_PHONE3 = "third phone number";
    //
    String CONTACTS_PHONE4 = "fourth phone number";
}
