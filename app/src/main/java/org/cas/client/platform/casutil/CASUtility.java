package org.cas.client.platform.casutil;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.EOFException;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 * 集中存放属于功能的方法。采用静态方法。 其中的方法与其它类的耦合性应最小。
 */
public class CASUtility {
    public static final String EMPTYSTR = "";
    public static final String NON_EDITOR = "N";
    public static final String UNDERLINE = "_";
    public static final String LEFT_BRACKET = "(";
    public static final String RIGHT_BRACKET = ")";
    public static final String LEFTSHARPBRACKET = "<";
    public static final String RIGHTSHARPBRACKET = ">";
    public static final String COMMA = ",";
    public static final String SEMICOLON = ";";
    public static final String SPACE = " ";
    public static String NODEPATHSTART = "[";
    public static String NODEPATHEND = "]";
    public static String DOUBlEQUOTATION = "\"";
    public static final String separator = System.getProperty("file.separator");
    public static final String sourcehtml = "<html><head></head><body><p></p></body></html>";

    static {
        initCalendarDate();
    }

    // 静态工具类，不允许实例化
    private CASUtility() {
    }

    /** 初始化日历中的数据 */
    private static void initCalendarDate() {
        cal = Calendar.getInstance();
        nowYear = cal.get(Calendar.YEAR);
        nowMonth = cal.get(Calendar.MONTH);
        // @NOTE: 因为cal.get(Calendar.MONTH)返回的值不知为何比实际值小1.
        nowDate = cal.get(Calendar.DATE);
    }

    /**
     * 获取当前日期的方法
     * 
     * @return new Date();
     */
    public static Date getDate() {
        return cal.getTime();
    }

    /**
     * @called by: emo.pim.pimview.PIMTextView; 该方法返回初始化的天的类型
     * @return 初始化的日期类型
     */
    public static int getTodayDateInt() {
        String tmpStr = String.valueOf(nowYear);
        if (nowMonth + 1 < 10)
            tmpStr = tmpStr.concat("0").concat(String.valueOf(nowMonth + 1));
        else
            tmpStr = tmpStr.concat(String.valueOf(nowMonth + 1));

        if (nowDate < 10)
            tmpStr = tmpStr.concat("0").concat(String.valueOf(nowDate));
        else
            tmpStr = tmpStr.concat(String.valueOf(nowDate));

        return Integer.parseInt(tmpStr);
    }
//
//    /** @called by:PIMTextView; */
//    public static int getYesterdayDateInt() {
//        EDate tmpEDate = new EDate(nowYear, nowMonth, nowDate).getLastDate();
//        String tmpStr = null;
//        int tmpInt = tmpEDate.getMonth();
//        if (tmpInt < 10)
//            tmpStr = String.valueOf(tmpEDate.getYear()).concat("0").concat(String.valueOf(tmpInt));
//        else
//            tmpStr = String.valueOf(tmpEDate.getYear()).concat(String.valueOf(tmpInt));
//
//        tmpInt = tmpEDate.getDate();
//        if (tmpInt < 10)
//            tmpStr = tmpStr.concat("0").concat(String.valueOf(tmpInt));
//        else
//            tmpStr = tmpStr.concat(String.valueOf(tmpInt));
//
//        return Integer.parseInt(tmpStr);
//    }

    /**
     * @called by ：emo.pim.pimview.dialog.contacts.ContactDetailPanel
     * @para prmString : 需要paser成时间的字符串。
     * @para prmType: 格式类型。只可以为0、1、2三个值中的一个。 0：返回yyyy/m/d格式，1：返回aaaa格式， 2：返回h：mm格式。
     */
    public static String getFormatedTime(
            String prmString,
            int prmType) {
        // FormulaTokenQuene tmpQuene =
        // new FormulaTokenQueneAnalysis().tokenQueneAnalysis(prmString, false, "", 0);
        // switch (tmpQuene.getQueneType())
        // {
        // case FormulaTokenQueneType.FORMATYEARMONTHDATE :
        // // format year month date
        // case FormulaTokenQueneType.FORMATYEARMONTHDATESTRING :
        // // format year month date with string
        // case FormulaTokenQueneType.FORMATMONTHDATE :
        // // format year month date or year month
        // case FormulaTokenQueneType.FORMATALLTIME : // format all the time
        // case FormulaTokenQueneType.FORMATALLTIMESTRING :
        // // format all the time with string
        // case FormulaTokenQueneType.FORMATMONTHDATETIME :
        // // format month date time
        // case FormulaTokenQueneType.FORMATTIME : // format the time
        // case FormulaTokenQueneType.FORMATCHINADATETIME :
        //
        // Object tmpFormatObject =
        // new InputObject(null, tmpQuene, prmString, 0, 0).getFormatObject();
        // double tmpValue = ((FormatObject)tmpFormatObject).getValue();
        // String tmpFormat = "yyyy/m/d";
        // int tmpFormattype = CellsConstants.DATE;
        // switch (prmType)
        // {
        //
        // case 2 :
        // tmpFormat = "aaaa";
        // tmpFormattype = CellsConstants.DATE;
        // break;
        //
        // case 3 :
        // tmpFormat = "h:mm";
        // tmpFormattype = CellsConstants.TIME;
        // break;
        //
        // default :
        // break;
        // }
        // NumberResult tmpResult =
        // ENumberFormat.getNumberResult(
        // 0,
        // new Double(tmpValue),
        // tmpFormattype,
        // tmpFormat,
        // null,
        // 1000,
        // false);
        // prmString = tmpResult.getCellString();
        // return prmString;
        // default :
        return prmString;
        // }
    }

    /**
     * 应该是开始的月份。 该方法返回当前的星期数 Get current week number in a order(the week including today is as No.26).
     * 
     * @called by:emo.pim.pimview.ebeans.EWeeiView;
     * @return 当前的星期数
     */
    public static int getCurrentWeek() {
        return 0;
    }

    /**
     * 得到某年某月的天数。
     * 
     * @called by emo.pim.pimcontrol.PaneManager
     */
    public static int getDays(
            int prmYear,
            int prmMonth) {
        // 对日月进行调整
        while (prmMonth < 0) {
            prmMonth += 12;
            --prmYear;
        }
        while (prmMonth >= 12) {
            prmMonth -= 12;
            ++prmYear;
        }
        cal.set(prmYear, prmMonth, 1);
        return cal.getActualMaximum(Calendar.DAY_OF_MONTH);
    }

//    /**
//     * @param date
//     * @return 自1900年1月1日到指定日期的天数
//     */
//    public static int getPastDays(
//            EDate date) {
//        int year = date.getYear();
//        int month = date.getMonth();
//        int day = date.getDate();
//        int daysofYear = (year - 1900) * 365 + (year - 1900) / 4;
//        int daysofMonth = 0;
//        if (year % 4 == 0) {
//            switch (month) {
//                case 1:
//                    daysofMonth = 0;
//                    break;
//                case 2:
//                    daysofMonth = 31;
//                    break;
//                case 3:
//                    daysofMonth = 60;
//                    break;
//                case 4:
//                    daysofMonth = 91;
//                    break;
//                case 5:
//                    daysofMonth = 121;
//                    break;
//                case 6:
//                    daysofMonth = 152;
//                    break;
//                case 7:
//                    daysofMonth = 182;
//                    break;
//                case 8:
//                    daysofMonth = 213;
//                    break;
//                case 9:
//                    daysofMonth = 244;
//                    break;
//                case 10:
//                    daysofMonth = 274;
//                    break;
//                case 11:
//                    daysofMonth = 305;
//                    break;
//                case 12:
//                    daysofMonth = 335;
//                    break;
//            }
//        } else {
//            switch (month) {
//                case 1:
//                    daysofMonth = 0;
//                    break;
//                case 2:
//                    daysofMonth = 31;
//                    break;
//                case 3:
//                    daysofMonth = 59;
//                    break;
//                case 4:
//                    daysofMonth = 90;
//                    break;
//                case 5:
//                    daysofMonth = 120;
//                    break;
//                case 6:
//                    daysofMonth = 151;
//                    break;
//                case 7:
//                    daysofMonth = 181;
//                    break;
//                case 8:
//                    daysofMonth = 212;
//                    break;
//                case 9:
//                    daysofMonth = 243;
//                    break;
//                case 10:
//                    daysofMonth = 273;
//                    break;
//                case 11:
//                    daysofMonth = 304;
//                    break;
//                case 12:
//                    daysofMonth = 334;
//                    break;
//            }
//        }
//        return daysofYear + daysofMonth + day;
//    }
//
//    /**
//     * 根据日期返回一个格式化的日期形式
//     *
//     * @param tmpDate
//     * @return str
//     */
//    public static String getDateString(
//            EDate tmpDate) {
//        int month = tmpDate.getMonth();
//        int date = tmpDate.getDate();
//        String monthStr = month < 10 ? "0" + month : "" + month;
//        String dateStr = date < 10 ? "0" + date : "" + date;
//        String str = tmpDate.getYear() + monthStr + dateStr;
//        return str;
//    }
//
//    /**
//     * 根据日记的ID返回一个日期
//     *
//     * @param tmpID
//     * @return EDate
//     */
//    public static EDate IDToDate(
//            int tmpID) {
//        int year = tmpID / 10000;
//        int month = tmpID / 100 - year * 100;
//        int day = tmpID - year * 10000 - month * 100;
//        return new EDate(year, month - 1, day);
//    }
//
//    /**
//     * 判断nodeString是否为格式(XXXX年X月) TODO: 现在判断还过于简单，以后想到有好的方法应该增加条件
//     *
//     * @param nodeString
//     * @return
//     */
//    public static boolean isSystemNode(
//            String nodeString) {
//        if (nodeString.endsWith(PaneConsts.MONTH) && nodeString.indexOf(PaneConsts.YEAR) == 4)
//            return true;
//        else
//            return false;
//    }
//
//    /**
//     * 根据ID号判断这条记录的路径
//     *
//     * @param tmpID
//     *            日记的ID号
//     * @return ID号所在记录的路径
//     * @called by PIMTextView
//     */
//    public static TreePath getActiveTreePath(
//            int tmpID) {
//        int year = tmpID / 10000;
//        int month = tmpID / 100 - year * 100;
//        String tmpNewNodeName =
//                Integer.toString(year).concat(PaneConsts.YEAR).concat(Integer.toString(month)).concat(PaneConsts.MONTH);
//
//        int tmpIndex = CustOpts.custOps.APPNameVec.indexOf("Diary");
//        String tmpCaption = CustOpts.custOps.APPCapsVec.get(tmpIndex);// 得到本应用的Caption。
//
//        StringBuffer tmpBuffer = new StringBuffer(PaneConsts.ROOTCAPTION);
//        tmpBuffer.append(PaneConsts.HEAD_PAGE);
//        tmpBuffer.append(", ");
//        tmpBuffer.append(tmpCaption);
//        tmpBuffer.append(']');
//        String tmpBasicFoderStr = tmpBuffer.toString();// 得到对于本应用的根本路径。
//
//        TreePath tmpNewNodePath = new TreePath(tmpBasicFoderStr.concat(COMMA).concat(tmpNewNodeName));
//        return tmpNewNodePath;
//    }
//
//    /**
//     * 对传入的表示节点完整路径做处理,返回其父节点路径字串. 即：去掉最后一个节点：[a,b,c]=>[a,b]
//     *
//     * @called by: PIMDBModel 获得父节点的ViewInfo PIMControl.changeApplicateion; 遍历父节点的路径,以遍历其所有子.
//     */
//    public static String getParentNodeStr(
//            String prmNodePath) {
//        if (prmNodePath != null && prmNodePath.startsWith(NODEPATHSTART) && prmNodePath.endsWith(NODEPATHEND)) {
//            if (prmNodePath.lastIndexOf(',') != -1)
//                return prmNodePath.substring(0, prmNodePath.lastIndexOf(',')).concat(NODEPATHEND);
//            else
//                return prmNodePath;
//        }
//        return null;
//    }
//
//    /**
//     * 在传入的路径前面加上字串“【PIM，咨询管理，”，后面加上“】”。
//     *
//     * @param prmSubPath子树的路径
//     *            ,例如： 收件箱， 新建文件夹
//     * @return String 树路径[PIM, 咨询管理， 收件箱， 新建文件夹]
//     */
//    public static String getFolderPath(
//            String prmSubPath) {
//        return (prmSubPath != null) ? PaneConsts.ROOTCAPTION.concat(PaneConsts.HEAD_PAGE).concat(", ")
//                .concat(prmSubPath.trim()).concat(NODEPATHEND) : prmSubPath;
//    }

    /**
     * 去掉传入路径的前面的“【PIM，咨询管理，”和后面的“】”。
     * 
     * @param prmFolderPath
     *            树路径[PIM, 咨询管理， 收件箱， 新建文件夹]
     * @return String 子树的路径,例如： 收件箱， 新建文件夹
     */
    public static String getSubPath(
            String prmFolderPath) {
        if (prmFolderPath == null)
            return null;

        int tmpBeg = prmFolderPath.indexOf(',', NODEPATHSTART.concat("PIM").length() + 1);
        return (tmpBeg == -1) ? null : prmFolderPath.substring(tmpBeg + 1, prmFolderPath.length() - 1).trim();
    }

    /**
     * 解析一个逗号分隔符处理的字符串 getTextAreaData
     */
    public static int[] stringToArray(
            String prmString) {
        if (prmString != null && prmString.trim().length() != 0) {
            // 构建字符串分隔器
            StringTokenizer token = new StringTokenizer(prmString, ",");
            int size = token.countTokens();
            // 构建相应容量的字符串数组
            int[] indexes = new int[size];
            size = 0;
            // 循环加入,去掉空格的
            String tmpValue = null;
            try {
                while (token.hasMoreTokens()) {
                    tmpValue = token.nextToken().trim();

                    indexes[size] = Integer.parseInt(tmpValue);
                    size++;
                }
            } catch (Exception e) {
                // e.printStackTrace();
            }
            return indexes;
        } else
            return null;
    }

    /**
     * 取得用逗号分隔的字符串中的字段 最后将字段保存在Vector中
     * 
     * @param prmChar
     *            为需要分段地符号
     * @param prmLimit
     *            为需要分段的位置
     * @return : 返回id的容器
     */
    public static Vector parserStrToVec(
            String prmString,
            int prmChar,
            int prmLimit) {
        Vector recordVector = new Vector();
        int tmpStrLen;
        tmpStrLen = prmString.length();
        int tmpCount = 0;
        for (int tmpDot = prmChar, tmpStartPos = 0, tmpPos = 0; tmpStartPos < tmpStrLen; tmpPos++, tmpStartPos = tmpPos, tmpCount++) {
            if (tmpCount >= prmLimit) {
                break;
            }
            tmpPos = prmString.indexOf(tmpDot, tmpPos); // 得到当前位置之后的第一个逗号的位置。
            if (tmpPos < 0) // 如果当前位置后面没有逗号了，就把当前位置设为字符串末尾。
            {
                tmpPos = tmpStrLen;
            }
            recordVector.add(prmString.substring(tmpStartPos, tmpPos));
            // 将起始位置和当前位置之间的字符串加入Vector。
        }
        return recordVector;
    }

//    /**
//     * 把以";"相隔的字符串，转化为以","相隔的字符串
//     */
//    // 取文本其框中的值
//    public static String getSelectedText(
//            JTextField prmSelectedField) {
//        String tmpSelected = prmSelectedField.getText();
//        if (tmpSelected == null || tmpSelected.equals("")) {
//            return tmpSelected = CASUtility.EMPTYSTR;
//        }
//        // 转换字符串格式为 contact,contact,contact,.....
//        StringBuffer tmpBuf = new StringBuffer();
//
//        StringTokenizer tmpKen = new StringTokenizer(tmpSelected, ";");
//        // 取出contact；contact；contact；.....中的第一个值
//        if (tmpKen != null && tmpKen.hasMoreTokens()) {
//            String tmpToken = tmpKen.nextToken().trim();
//            if (tmpToken != null && tmpToken.length() > 0) {
//                tmpBuf.append(tmpToken);
//            }
//        }
//        // 取出其余的值，在每一个值之前加上一个逗号
//        while (tmpKen != null && tmpKen.hasMoreTokens()) {
//            tmpBuf.append(",");
//            String tmpSubStr = tmpKen.nextToken().trim();
//            if (tmpSubStr != null && tmpSubStr.length() > 0) {
//                tmpBuf.append(tmpSubStr);
//            }
//        }
//        return tmpBuf.toString();
//    }
//
//    /**
//     * 取得按钮组的最大宽度
//     *
//     * @param button
//     *            :按钮
//     * @retrun 按钮的宽度
//     */
//    public static int getMaxWidth(
//            JButton button) {
//        return getMaxWidth(new JButton[] { button });
//    }
//
//    /**
//     * 取得按钮组的最大宽度
//     *
//     * @param button
//     *            :按钮数组
//     * @retrun 按钮的最大宽度
//     */
//    public static int getMaxWidth(
//            JButton[] button) {
//        int maxWidth = 0;
//        for (int i = 0; i < button.length; i++) {
//            int tMargin = button[i].getMargin().left + button[i].getMargin().right;
//            JLabel label = new JLabel(button[i].getText());
//            maxWidth =
//                    maxWidth > label.getPreferredSize().width + tMargin ? maxWidth : label.getPreferredSize().width
//                            + tMargin;
//        }
//        return maxWidth + 10;// 这个微调用于应付边框绘制的区间.
//    }
//
//    /**
//     * @param 根据对应的应用类型取得导航树上的路径
//     * @called by : CardTest;
//     * @return 返回对应应用的路径
//     */
//    public static String getTreePath(
//            String prmAppName) {
//        int tmpIndex = CustOpts.custOps.APPNameVec.indexOf(prmAppName);
//        String tmpCaption = CustOpts.custOps.APPCapsVec.get(tmpIndex);
//        CASTree tmpTree = CASTree.getInstance();
//        return tmpTree.getNodePath(tmpTree.getPathFromRoot(PaneConsts.HEAD_PAGE), tmpCaption).toString();
//    }
//
//    /**
//     * @param 树路径
//     * @called by: CardTest;
//     * @return 返回应用的索引值
//     */
//    public static int getAppIndexByFolderID(
//            int prmFolderID) {
//        if (prmFolderID < 100)
//            return prmFolderID;
//
//        Vector tmpVec = CustOpts.custOps.APPCapsVec;
//        CASNode tmpNode = CASControl.ctrl.getFolderTree().getRootNode();
//        while (tmpNode != null) // 深度遍历树的所有结点
//        {
//            if (tmpNode.getPathID() == prmFolderID) // 得到结点的字串在CapVec的Index并判断
//            { // 与参数匹配否?
//                while (tmpNode != null) {
//                    String tmpCaption = tmpNode.getUserObject().toString(); // 得到结点的Caption字串
//                    int tmpIndex = tmpVec.indexOf(tmpCaption);
//                    if (tmpIndex >= 0) // 如果这个String有被APPCaptionVec维护.
//                        return tmpIndex; // 则Index号就是AppIndex了.
//                    else
//                        // 否则得到其父结点, 并继续判断.
//                        tmpNode = (CASNode) tmpNode.getParent();
//                }
//                break; // 只要找到一个匹配的节点就够了，不要再继续循环了。
//            }
//            tmpNode = (CASNode) tmpNode.getNextNode();
//        }
//        return -1; // 本结点和所有的父节点都不是可识别的Str，返回。
//    }
//
//    /**
//     * TODO:能够在这个方法的执行时临时设置为"广度遍历"
//     *
//     * @param prmAppIndex
//     * @return
//     */
//    public static int getAPPNodeID(
//            int prmAppIndex) {
//        Vector tmpVec = CustOpts.custOps.APPCapsVec;
//        CASNode tmpNode = CASControl.ctrl.getFolderTree().getRootNode();
//        while (tmpNode != null) // 遍历树结点
//        {
//            if (tmpVec.indexOf(tmpNode.getUserObject()) == prmAppIndex) // 得到结点的字串在CapVec的Index并判断与参数匹配否
//            {
//                return tmpNode.getPathID(); // 匹配的话返回该结点的IntID.
//            }
//            tmpNode = (CASNode) tmpNode.getNextNode();
//        }
//        return -1;
//    }
//
//    public static int getMatchedNodeID(
//            String prmNodeStr) {
//        CASNode tmpNode = CASControl.ctrl.getFolderTree().getRootNode();
//        while (tmpNode != null) // 遍历树结点
//        {
//            if (tmpNode.toString().equals(prmNodeStr)) // 得到结点的字串与参数匹配否
//            {
//                return tmpNode.getPathID(); // 匹配的话返回该结点的IntID.
//            }
//            tmpNode = (CASNode) tmpNode.getNextNode();
//        }
//        return -1;
//    }
//
//    public static String getViewInfoPath(
//            String prmFolderPath) {
//        int tmpStartPos = prmFolderPath.indexOf(NODEPATHSTART);
//        int tmpEndPos = prmFolderPath.indexOf(NODEPATHEND);
//        if (tmpStartPos != -1 && tmpEndPos != -1) {
//            String tmpViewInfoPath = prmFolderPath.substring(tmpStartPos + 1, tmpEndPos).trim();
//            Vector tmpVector = parserStrToVec(tmpViewInfoPath, ',', tmpViewInfoPath.length());
//            tmpViewInfoPath = ((String) tmpVector.get(2)).trim();
//            tmpStartPos = prmFolderPath.indexOf(tmpViewInfoPath);
//            tmpViewInfoPath =
//                    prmFolderPath
//                            .substring(0, tmpStartPos)
//                            .concat(CustOpts.custOps.APPNameVec.get(CustOpts.custOps.APPCapsVec
//                                    .indexOf(tmpViewInfoPath)))
//                            .concat(prmFolderPath.substring(tmpStartPos + tmpViewInfoPath.length()));
//            return tmpViewInfoPath;
//        }
//        return null;
//    }
//
//    /**
//     * 取得任务信息,包装新的comment字段
//     *
//     * @called by : EFolderTree
//     */
//    public static String getTaskString(
//            Vector prmRecords) {
//        if (prmRecords == null) {
//            return null;
//        }
//        StringBuffer buffer = new StringBuffer();
//        for (int i = 0; i < prmRecords.size(); i++) {
//            PIMRecord tmpRecord = (PIMRecord) prmRecords.get(i);
//            Object caption = tmpRecord.getFieldValue(ModelDBCons.CAPTION);
//            // 主题
//            Object startDate = tmpRecord.getFieldValue(ModelDBCons.BEGIN_TIME);
//            // 开始时间
//            Object endDate = tmpRecord.getFieldValue(ModelDBCons.END_TIME);
//            // 截至时间
//            Object status = tmpRecord.getFieldValue(ModelDBCons.STATUS);
//            // 任务状态
//            Object finishPer = tmpRecord.getFieldValue(ModelDBCons.COMPLETED);
//            // 完成率
//            Object finishDate = tmpRecord.getFieldValue(ModelDBCons.FINISH_DATE);
//            // 完成日期
//            Object workAll = tmpRecord.getFieldValue(ModelDBCons.TASK_GROSS);
//            // 工作总量
//            Object factWord = tmpRecord.getFieldValue(ModelDBCons.REALLY_TASK);
//            // 实际工作
//            Object owner = tmpRecord.getFieldValue(ModelDBCons.OWNER);
//            Object accInfo = tmpRecord.getFieldValue(ModelDBCons.TALLY_INFO);
//            // 记帐信息
//            Object way = tmpRecord.getFieldValue(ModelDBCons.MILESTONE);
//            // 里程
//            Object comment = tmpRecord.getFieldValue(ModelDBCons.COMMENT);
//            // 附注信息
//            if (caption != null && caption.toString().length() > 0) {
//                buffer.append("---------------\n");
//                buffer.append(PaneConsts.SUBJECTTEXT + "\t" + caption.toString() + "\n");
//            } else {
//                return "";
//            }
//            if (startDate != null) {
//                buffer.append(PaneConsts.START_TIME + "\t" + getFormatDateString((Date) startDate) + "\n");
//            }
//            if (endDate != null) {
//                buffer.append(PaneConsts.END_TIME + "\t" + getFormatDateString((Date) endDate) + "\n");
//            }
//            if (status != null) {
//                buffer.append(PaneConsts.TASK_STATUS + "\t"
//                        + TaskDialogConstant.STATUS_DATA[((Integer) status).intValue()] + "\n");
//            }
//            if (finishPer != null) {
//                buffer.append(PaneConsts.FINISH_PR + "\t" + (String) finishPer + "%" + "\n");
//            }
//            if (finishDate != null) {
//                buffer.append(PaneConsts.FINISH_DATE + "\t" + getFormatDateString((Date) finishDate) + "\n");
//            }
//            if (workAll != null) {
//                buffer.append(PaneConsts.WORK_ALL + "\t" + workAll.toString() + "\n");
//            }
//            if (factWord != null) {
//                buffer.append(PaneConsts.FACT_WORK + "\t" + factWord.toString() + "\n");
//            }
//            if (owner != null) {
//                buffer.append(PaneConsts.OWNER + "\t" + owner.toString() + "\n");
//            }
//            if (accInfo != null) {
//                buffer.append(PaneConsts.ACC_INFO + "\t" + accInfo.toString() + "\n");
//            }
//            if (way != null) {
//                buffer.append(PaneConsts.WAY_LON + "\t" + way.toString() + "\n");
//            }
//            if (comment != null) {
//                buffer.append(PaneConsts.ANNOT_INFO + "\t" + comment.toString() + "\n");
//            }
//        }
//        return buffer.toString();
//    }
//
//    /**
//     * 取得格式化后的Stirng日期 call by remindDialog and this
//     */
//    public static String getFormatDateString(
//            Date prmDate) {
//        return new StringBuffer().append(prmDate.getYear() + 1900).append('-').append(prmDate.getMonth() + 1)
//                .append('-').append(prmDate.getDate()).append(" (")
//                .append(PIMTableRendererConstant.WEEKDAYS[prmDate.getDay()]).append(") ").append(prmDate.getHours())
//                .append(':').append(prmDate.getMinutes() > 9 ? "" + prmDate.getMinutes() : "0" + prmDate.getMinutes())
//                .toString();
//    }
//
    /* 取得PIM默认保存数据的路径 @return 默认路径 */
    public static String getPIMDirPath() {
        String tmpDataDirectoryStr =
                new StringBuilder("c:\\AikaPos")//System.getProperty("user.home"))
                .append(System.getProperty("file.separator"))
                .append(".Storm070111")
                .append(System.getProperty("file.separator")).toString();
        // 至此构造出PIM在系统目录下的路径字符串。
        File tmpDir = new File(tmpDataDirectoryStr);
        if (!tmpDir.exists()) {
            if (!tmpDir.mkdirs()) {
                ErrorUtil.write("create Pim directory failed！");
                System.exit(0);
            }
        }
        return tmpDataDirectoryStr;
    }

//    /**
//     * 取得保存邮件的路径
//     *
//     * @return 保存邮件的路径
//     */
//    public static String getPIMMailDirPath() {
//        String tmpMailPathStr = getPIMDirPath() + "mail";
//        // 至此构造出PIM的邮件文件在系统目录下的路径字符串。File tmpDir = new File(tmpDBPathStr);
//        File tmpDir = new File(tmpMailPathStr);
//        if (!tmpDir.exists())
//            if (!tmpDir.mkdirs()) {
//                ErrorUtil.write("create database directory failed！");
//                System.exit(0);
//            }
//        return tmpMailPathStr + System.getProperty("file.separator");
//    }
//
//    /**
//     * 获得指定应用的视图名称
//     *
//     * @param appIndex
//     *            应用的id
//     * @return 应用的视图名称
//     */
//    public static String[] getViewNames(
//            int appIndex) {
//        return AppConstant.SUB_APP_TYPE[appIndex];
//    }
//
//    /**
//     * 根据视图名称获得视图类型的id
//     *
//     * @param appIndex
//     *            应用的id
//     * @param appSubName
//     *            视图的名称
//     * @return 视图的id
//     */
//    public static int getAppSubType(
//            int appIndex,
//            String appSubName) {
//        String[] viewNames = getViewNames(appIndex);
//        for (int i = 0; i < viewNames.length; i++) {
//            if (viewNames[i] == appSubName) {
//                return i;
//            }
//        }
//        return -1;
//    }
//
//    /**
//     * 更据不同的类型解析出对应数据库表字段数组
//     *
//     * @param int prmApp 类型
//     * @return String[] 数据库表字段数组
//     */
//    public static String[] getFieldAry(
//            int prmApp) {
//        if (prmApp >= 0 && prmApp < CustOpts.custOps.APPNameVec.size()) {
//            String tmpKey = CustOpts.custOps.APPNameVec.get(prmApp);
//            return MainPane.getApp(tmpKey).getAppFields();
//        } else {
//            return null; // TODO:应该返回DefaultDBinfo中的其它表对应的字段名。
//        }
//    }
//
//    /**
//     * 更据类型得到条件数组
//     *
//     * @param prmType
//     *            字段的类型
//     * @return String[] 类型条件数组
//     */
//    public static String[] getTypesAry(
//            int prmType) {
//        switch (prmType) {
//            case CustViewConsts.STRING_TYPE:
//                return CustViewConsts.STR_CONDITION;
//
//            case CustViewConsts.NUM_TYPE:
//                return CustViewConsts.NUM_CONDITION;
//
//            case CustViewConsts.BOOL_TYPE:
//                return CustViewConsts.BOOL_CONDITION;
//
//            case CustViewConsts.TIME_TYPE:
//                return CustViewConsts.TIME_CONDITION;
//
//            default:
//                return null;
//        }
//    }
//
//    /**
//     * 根据视图类型的id获得视图名称
//     *
//     * @param appIndex
//     *            应用的id
//     * @param appSubType
//     *            视图的id
//     * @return 视图的名称
//     */
//    public static String getAppSubType(
//            int appIndex,
//            int appSubType) {
//        return AppConstant.SUB_APP_TYPE[appIndex][appSubType];
//    }
//
//    /**
//     * 根据邮件Record，驱除其中的附件名 供保存使用
//     */
//    public static String[] getAttachMentName(
//            PIMRecord prmRecord,
//            boolean prmBl) {
//        String prmAttachNames = CASUtility.EMPTYSTR;
//        if (prmRecord.getFieldValue(ModelDBCons.ATTACH) != null
//                || prmRecord.getFieldValue(ModelDBCons.ATTACHMENT) != null) {
//            prmAttachNames =
//                    prmBl ? prmRecord.getFieldValue(ModelDBCons.ATTACH).toString() : prmRecord.getFieldValue(
//                            ModelDBCons.ATTACHMENT).toString();
//            Vector attachNames = parseString(prmAttachNames);
//            String[] attachName = new String[attachNames.size()];
//            for (int i = 0; i < attachNames.size(); i++) {
//                attachName[i] = attachNames.elementAt(i).toString();
//            }
//            return attachName;
//        } else {
//            return null;
//        }
//    }
//
//    /**
//     * 判断应用是否为邮件类别，包括收件箱，发件箱，已发送项，已删除项。
//     *
//     * @param prmRecordType
//     *            被判断的应用的ID
//     * @return true 应用属于邮件类别 false 应用不属于邮件类别
//     * @called by PasteAction
//     */
//    public static boolean isMail(
//            int prmRecordType) {
//        if ((prmRecordType == ModelCons.INBOX_APP) || (prmRecordType == ModelCons.OUTBOX_APP)
//                || (prmRecordType == ModelCons.SENDED_APP) || (prmRecordType == ModelCons.DRAFT_APP)) {
//            return true;
//        }
//        return false;
//    }
//
//    /**
//     * 从邮件中取得相应的联系人记录。
//     *
//     * @param mailRecords
//     *            提取邮件记录的记录集
//     * @return 返回不为空的联系人记录的条件 1)mailRecords中只有一条记录 2)mailRecords只包含的记录属于邮件应用 3)mailRecords只包含的邮件记录包含有效的地址信息 否则返回null。
//     */
//    public static PIMRecord getContact(
//            PIMRecord prmMailRec) {
//        // 只处理从邮件中取得联系人信息
//        if (!isMail(prmMailRec.getAppIndex())) {
//            return null;
//        }
//        // 获得发信人的邮件地址
//        String addresser = (String) prmMailRec.getFieldValue(ModelDBCons.ADDRESSER);
//        if ((addresser == null) || (addresser.length() == 0)) {
//            return null;
//        }
//        // 构造包括有效地址信息的联系人记录
//        PIMRecord contactRecord = new PIMRecord();
//        Hashtable hash = new Hashtable();
//        hash.put(PIMPool.pool.getKey(ContactDefaultViews.EMAIL), addresser);
//        contactRecord.setFieldValues(hash);
//        contactRecord.setAppIndex(ModelCons.CONTACT_APP);
//        // 返回包括有效地址信息的联系人记录
//        return contactRecord;
//    }
//
//    /**
//     * 从邮件中取得发件人的地址
//     *
//     * @param mails
//     *            包含发件人地址的邮件
//     * @return 发件人地址信息
//     */
//    public static String[] getMailList(
//            Vector mails) {
//        // 若mails为空，则返回长度为0的String数组
//        if (mails == null) {
//            return new String[0];
//        }
//
//        final int size = mails.size();
//        // String[] mailList = new String[size];
//        Set mailList = new HashSet();
//        // 逐个取得邮件中的发件人地址
//        for (int i = 0; i < size; i++) {
//            PIMRecord mailRecord = (PIMRecord) (mails.get(i));
//            mailList.add(mailRecord.getFieldValue(ModelDBCons.ADDRESSER));
//        }
//
//        return (String[]) (mailList.toArray());
//    }

    /**
     * 解析以";"分隔的字符串
     * 
     * @param prm
     *            目的字符串
     * @return Vector 返回保存各字符串的vector
     */
    public static Vector parseString(
            String prm) {
        if (prm == null || prm.length() == 0) {
            return new Vector();
        }
        Vector attachName = new Vector();
        for (int i = 0, j = 0; i < prm.length(); i = j + 1) {
            j = prm.indexOf(";", i);
            if (j < 0) {
                attachName.add(prm.substring(prm.lastIndexOf(";") + 1, prm.length()));
                break;
            }
            attachName.add(prm.substring(i, j));
        }
        return attachName;
    }

    public static String parseWord(
            String text) {
        StringTokenizer token = new StringTokenizer(text.toString(), ",");
        int size = token.countTokens();
        String[] indexs = new String[size];
        String tmpAll = EMPTYSTR;// 所有要显示的文字
        size = 0;
        while (token.hasMoreTokens()) {
            indexs[size] = token.nextToken();
            size++;
        }
        for (int i = 0; i < indexs.length; i++) {
            int j = indexs[i].indexOf('<');// 得到<的位置j肯定不会等于-1
            if (j == 2) {// 无"表示为"内容，则加入邮件地址
                tmpAll = tmpAll + indexs[i].substring(j + 1, indexs[i].length() - 1) + ',';
            } else {
                String tmp = indexs[i].substring(1, j - 1);
                if (tmp.endsWith("\\)"))// outlook的问题
                {
                    int k = tmp.indexOf('\\');
                    tmpAll =
                            tmpAll + tmp.substring(0, k) + tmp.substring(k + 1, tmp.length() - 2).concat(RIGHT_BRACKET)
                                    + ',';
                } else {
                    tmpAll = tmpAll + indexs[i].substring(1, j - 1) + ',';
                }
            }
        }
        if (tmpAll.length() > 0) {
            tmpAll = tmpAll.substring(0, tmpAll.length() - 1);
        }
        return tmpAll;
    }
//
//    /**
//     * 解析一个逗号分隔符处理的字符串 getTextAreaData
//     *
//     * @return 经处理后的字符串数组
//     * @param prmString
//     *            一个逗号分隔的字符串
//     */
//    public static int[] commaStrToIntAry(
//            String prmString) {
//        if (prmString != null && prmString.trim().length() != 0) {
//            // 构建字符串分隔器
//            StringTokenizer token = new StringTokenizer(prmString, ",");
//            int size = token.countTokens();
//            // 构建相应容量的字符串数组
//            int[] indexes = new int[size];
//            size = 0;
//            // 循环加入,去掉空格的
//            String tmpValue = null;
//            try {
//                while (token.hasMoreTokens()) {
//                    tmpValue = token.nextToken().trim();
//
//                    indexes[size] = Integer.parseInt(tmpValue);
//                    size++;
//                }
//            } catch (Exception e) {
//                ErrorUtil.write("get prm which is not a int with comma(PIMUtiliey.commaStrToIntAry())");
//                // 传入参数错误,不是逗号分割的数字.");
//            }
//            return indexes;
//        } else {
//            return null;
//        }
//    }

    /**
     * 解析以逗号分隔的特殊地址
     * 
     * @param prmAddress
     *            目的地址串
     * @return String 返回去除"<>"号的地址字符串
     */
    public static String commaStrToPlainAddr(
            String prmAddress) {
        Vector tmpVector = new Vector();

        if (prmAddress.trim().length() == 0) {
            return null;
        }

        String tmpStr = null;
        StringTokenizer strTok = new StringTokenizer(prmAddress, ",");
        while (strTok.hasMoreElements()) {
            tmpStr = (String) strTok.nextElement();
            if (tmpStr.indexOf('<') != -1 && tmpStr.indexOf('>') != -1) {
                int start = 0;
                int end = tmpStr.indexOf('<');
                int tmpInt = tmpStr.indexOf('>');
                int mid = end - start - 2;
                String tmpMidStr = null;
                if (mid > 0) {
                    tmpMidStr = tmpStr.substring(start + 1, end - 1).trim();
                    if (tmpMidStr.length() < 1) {
                        tmpStr = tmpStr.substring(end + 1, tmpInt);
                    }
                } else {
                    tmpStr = tmpStr.substring(end + 1, tmpInt);
                }
            }
            tmpVector.addElement(tmpStr);
        }

        if (tmpVector.size() < 1) {
            return null;
        }

        Iterator tmpItera = tmpVector.iterator();
        StringBuffer tmpSB = new StringBuffer();
        tmpSB.append("");
        while (tmpItera.hasNext()) {
            tmpSB.append((String) tmpItera.next());
            tmpSB.append(',');
        }
        return tmpSB.toString().substring(0, tmpSB.toString().length() - 1);
    }

    /**
     * @callec by: QuickInfoField.java
     */
    public static String clearHeadSpace(
            String prmStr) {
        while (prmStr.startsWith(" ")) {
            prmStr = prmStr.substring(1);
        }
        return prmStr;
    }

    /**
     *
     */
    public static String[] processAttachName(
            Vector prmNames,
            int prmID) {
        Vector tmpVecsVec = new Vector();
        String tmpName = null;
        for (int a = 0; a < prmNames.size(); a++) {
            tmpName = (String) prmNames.get(a);
            Vector tmpVec = new Vector();
            tmpVec.add(tmpName);
            tmpVecsVec.add(tmpVec);
            for (int i = a + 1; i < prmNames.size();) {
                if (tmpName.equals(prmNames.get(i))) {
                    tmpVec.add(prmNames.get(i));
                    prmNames.remove(i);
                } else {
                    i++;
                }
            }
        }
        Vector tmpVec = new Vector();
        for (int i = tmpVecsVec.size(); i > 0; i--) {
            tmpVec = (Vector) tmpVecsVec.get(i - 1);
            if (tmpVec.size() > 1) {
                for (int j = tmpVec.size(); j > 0; j--) {
                    tmpVec.set(j - 1, ((String) tmpVec.get(j - 1)).concat("_evrmorpim_" + Integer.toString(j - 1)));
                }
            }
        }
        return null;
    }

    /*
     * 从distinguish name 中将代表证书的电子邮件地址取出
     */
    public static String parseDNE(
            String DN) {
        int tmp;
        int tmpComma;
        if ((tmp = DN.indexOf("E=")) != -1) {
            tmpComma = DN.indexOf(',', tmp);
            if (tmpComma != -1) {
                return DN.substring(tmp + 2, tmpComma);
            } else {
                return DN.substring(tmp + 2);
            }
        } else if ((tmp = DN.indexOf("EMAILADDRESS=")) != -1) {
            tmpComma = DN.indexOf(',', tmp);
            if (tmpComma != -1) {
                return DN.substring(tmp + 13, tmpComma);
            } else {
                return DN.substring(tmp + 13);
            }
        } else {
            return null;
        }
    }

    /*
     * 从distinguish name 中将代表证书的名字取出
     */
    public static String parseDNCN(
            String DN) {
        int tmp;
        int tmpComma;
        if ((tmp = DN.indexOf("CN=")) != -1) {
            tmpComma = DN.indexOf(',', tmp);
            if (tmpComma != -1) {
                return DN.substring(tmp + 3, tmpComma);

            } else {
                return DN.substring(tmp + 3);
            }
        } else {
            return null;
        }
    }

//    /**
//     * 从对话盒中得到的路径作为记录的infolder
//     *
//     * @param prmRecord
//     *            要赋infolder的记录
//     * @return SelectFolderDialog对话盒是按取消按钮退出,返回true,否则返回false
//     */
//    public static boolean setPathFromDialog(
//            Dialog prmDialog,
//            PIMRecord prmRecord,
//            int prmInt) {
//        int tmpPathID = CASNavigationPane.getPathIDByDlg(prmDialog);
//        if (tmpPathID == -1)
//            return true;
//        else {
//            prmRecord.setInfolderID(tmpPathID);
//            return false;
//        }
//    }

    public static boolean isFirstShow() {
        return isFirst;
    }

    public static void setIsFirst(
            boolean prmIsFirst) {
        isFirst = prmIsFirst;
    }

    /**
     * 获取一个标记的完整字段
     * 
     * @param prmMessage
     *            目标字符串
     * @param prmObj
     *            所取标记－－－－－格式 "<标记"
     * @return String 标记的完整字段
     */
    public static String getEndMark(
            String prmMessage,
            String prmObj) {
        if (prmMessage.toLowerCase().indexOf(prmObj.toLowerCase()) == -1) {
            return null;
        }
        String tmp = prmMessage.substring(prmMessage.toLowerCase().indexOf(prmObj.toLowerCase()), prmMessage.length());
        // ＝＝＝＝＝＝＝＝＝＝＝判断这个标记何时结束＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝
        return tmp.substring(0, tmp.indexOf(RIGHTSHARPBRACKET) + 1);
    }

    public static String parseEmailAddr(
            String prmAddressed) {
        if (prmAddressed == null || prmAddressed.length() < 1) {
            return null;
        }
        int tmpBegPos = prmAddressed.indexOf(LEFTSHARPBRACKET);
        int tmpEndPos = prmAddressed.indexOf(RIGHTSHARPBRACKET);
        int tmpLbPos = prmAddressed.lastIndexOf(LEFTSHARPBRACKET);
        int tmpLePos = prmAddressed.lastIndexOf(RIGHTSHARPBRACKET);
        if (tmpBegPos == -1 || tmpEndPos == -1 || tmpBegPos != tmpLbPos || tmpEndPos != tmpLePos) {
            return null;
        }
        return prmAddressed.substring(tmpBegPos + 1, tmpEndPos).trim(); // 邮件地址
    }

    /*
     * 从"FORM"<king80@emo3.com>,"FORM"<king80@emo3.com>,... 中解析出电子邮件地址king80@emo3.com列表来
     * @param prmAddressStr 用逗号分割的电子邮件地址 FORM"<king80@emo3.com>,"FORM"<king80@emo3.com>,...
     * @return Vector 电子邮件地址列表
     */
    public static Vector parseMailAddress(
            String prmAddressStr) {
        if (prmAddressStr == null || prmAddressStr.length() < 1) {
            return null;
        }
        Vector tmpMailVec = new Vector();
        if (prmAddressStr.indexOf(",") == -1) {
            String tmpMailAddr = parseEmailAddr(prmAddressStr);
            /* 电子邮件地址 */
            tmpMailVec.add(tmpMailAddr); /* 返回当前的字符串 */
            return tmpMailVec;
        } else {
            String tmpRepStr = prmAddressStr.replace(',', ';');
            Vector tmpAddrVec = parseString(tmpRepStr);

            for (int i = 0; i < tmpAddrVec.size(); i++) {
                String tmpMailAddr = parseEmailAddr((String) tmpAddrVec.elementAt(i));
                /* 电子邮件地址 */
                if (tmpMailAddr != null && tmpMailAddr.length() > 0) {
                    tmpMailVec.add(tmpMailAddr);
                }
            }
        }
        return tmpMailVec;
    }

//    /**
//     * 附件命名规则 +-------------------------------+ | 附件保存时,按原文件名保存, | | 如遇有同名文件,则在文件名 | | 后跟(1),(2)... |
//     * +-------------------------------+
//     */
//    public static String nameFile(
//            String prmFileName) {
//        String tmpName = prmFileName;
//        String separator = System.getProperty("file.separator");
//        if (prmFileName.indexOf(separator) != -1) {
//            tmpName = tmpName.substring(tmpName.lastIndexOf(separator) + 1);
//        }
//        String name = (tmpName.indexOf(".") == -1) ? tmpName : tmpName.substring(0, tmpName.lastIndexOf("."));
//        // 文件名
//        String back = (name.length() == tmpName.length()) ? EMPTYSTR : tmpName.substring(name.length());
//        String path = getPIMMailDirPath();
//        String filePath = path + tmpName;
//        File tmpFile = new File(filePath);
//        int index = 1;
//        while (true) // 判断文件名是否已存在，如已存在，则加一个累加值（例：newFile(1).txt,newFile(2).txt)
//        {
//            if (tmpFile.exists()) {
//                if (index > 1) {
//                    name = name.substring(0, name.lastIndexOf("("));
//                }
//                name = name + '(' + index + ')';
//                index++;
//                filePath = path + name + back;
//                tmpFile = new File(filePath);
//            } else {
//                break;
//            }
//        }
//        return name + back;
//    }

    /**
     * 打开文件
     * 
     * @return Process
     * @param prmFileName
     *            文件名
     */
    public static Process processOpenFile(
            String prmFileName) {
        String tmpOSName = System.getProperty("os.name");
        String[] tmpCmds = null;
        if (tmpOSName.equalsIgnoreCase("Windows NT")) {
            tmpCmds = new String[3];
            tmpCmds[0] = "cmd.exe";
            tmpCmds[1] = "/C";
            tmpCmds[2] = prmFileName;
        } else if (tmpOSName.equalsIgnoreCase("Windows 95") || tmpOSName.equalsIgnoreCase("Windows 98")) {
            tmpCmds = new String[2];
            tmpCmds[0] = "start";
            tmpCmds[1] = prmFileName;
        } else if (tmpOSName.equalsIgnoreCase("Windows 2000") || tmpOSName.equalsIgnoreCase("Windows XP")) {
            tmpCmds = new String[3];
            tmpCmds[0] = "cmd.exe";
            tmpCmds[1] = "/C";
            String temp =
                    prmFileName.charAt(0) + CASUtility.DOUBlEQUOTATION + prmFileName.substring(1)
                            + CASUtility.DOUBlEQUOTATION;
            tmpCmds[2] = temp;
        }

        Process tmpProcess = null;
        Runtime tmpRT = Runtime.getRuntime();
        try {
            tmpProcess = tmpRT.exec(tmpCmds);
        } catch (Exception e) {
            // ErrorUtil.writeErrorLog(
        }

        return tmpProcess;
    }

    public static boolean buildSameAttach(
            String prmOldAttachName,
            String prmAttachName) {

        Vector news = parseString(prmAttachName);
        Vector olds = parseString(prmOldAttachName);

        boolean suc = true;
        for (int size = news.size(), i = 0; i < size; i++) {
            File tmpOld = new File((String) olds.get(i));
            File tmpNew = new File((String) news.get(i));
            try {
                char[] tmpBuff = new char[10 * 1024]; // 10K
                BufferedReader tmpIn = new BufferedReader(new FileReader(tmpOld));
                BufferedWriter tmpOt = new BufferedWriter(new FileWriter(tmpNew));
                while ((tmpIn.read(tmpBuff)) != -1) {
                    tmpOt.write(tmpBuff);
                }
                tmpIn.close();
                tmpOt.close();
            } catch (IOException e) {
                suc = false;
            }
        }
        return suc;
    }

    /**
     * @根据邮件adc@emo3.com生成“adc@emo3.com”<adc@emo3.com>字符串
     * 
     * @param prmEmailAddr
     *            邮件地址
     * @return 返回发送邮件时邮件地址格式“adc@emo3.com”<adc@emo3.com>
     */
    public static String createCCString(
            String prmEmailAddr) {
        StringBuffer tmpEmailAddr = new StringBuffer();
        tmpEmailAddr.append('\"').append(prmEmailAddr.trim()).append("\"<").append(prmEmailAddr.trim()).append('>');
        return tmpEmailAddr.toString();
    }

    /**
     * 通过日期对象取得时间
     * 
     * @return 格式化后的string
     */
    public static String getTimeString(
            Date date) {
        String str = EMPTYSTR;
        int hour = date.getHours();
        int minute = date.getMinutes();
        str = str.concat(Integer.toString(hour));// hour < 10 ? str + "0" + hour :
        str = minute < 10 ? str + ":0" + minute : str + ':' + minute;
        return str;
    }

    /**
     * 把字符串vector中的值全都转化为小写的
     * 
     * @param prmStringVec
     *            字符串列表
     * @return Vector 字符串vector中的值全都转化为小写后的Vector
     */
    public static Vector vecToLowerCase(
            Vector prmStringVec) {
        if (prmStringVec == null || prmStringVec.size() < 1) // 不用转换
        {
            return null;
        }
        String[] tmpStrAry = (String[]) prmStringVec.toArray(new String[0]);
        prmStringVec.clear();
        for (int i = 0; i < tmpStrAry.length; i++) {
            String tmpArray = tmpStrAry[i];
            if (tmpArray != null) {
                prmStringVec.add(tmpArray.toLowerCase().trim()); // 全都转化为小写
            }
        }
        return prmStringVec;
    }


    /**
     * 监视信件收发任务是否结束
     * 
     * @param isAlive
     */
    public static void setTaskLife(
            boolean isAlive) {
        isTaskAlive = isAlive;
    }

    /**
     * 获了信件收发任务状态
     * 
     * @return
     */
    public static boolean getTaskLife() {
        return isTaskAlive;
    }

//    private static String[] getStringTime(
//            PIMRecord record) {
//        String[] time_array = new String[2];
//        Hashtable hash = record.getFieldValues();
//        Date date = (Date) hash.get(PIMPool.pool.getKey(ModelDBCons.CALENDAR_BEGIN_TIME));
//        Date endDate = (Date) hash.get(PIMPool.pool.getKey(ModelDBCons.CALENDAR_END_TIME));
//        if (date == null || endDate == null) {
//            return null;
//        }
//        time_array[0] = getTimeString(date);
//        time_array[1] = getTimeString(endDate);
//        ;
//        return time_array;
//    }
//
//    /**
//     * @param records
//     * @param mode
//     *            排序模式: 0: 按起始时间排序, 1: 按终止时间排序
//     * @return
//     */
//    public static PIMRecord[] timeSort(
//            PIMRecord[] records,
//            int mode) {
//        int size = records.length;
//        double[] time = getTime(records[0]);
//        // if (time == null || time[0] == 0 || time[1] == 0)
//        if (time == null) {
//            return null;
//        }
//        int num_sort = 0;
//        int x_d = mode == 0 ? 0 : mode == 1 ? 1 : 0;
//        while (num_sort < size) {
//            for (int idx = 1; idx < size - num_sort; idx++) {
//                if (x_d == 0) {
//                    if (getTime(records[idx])[x_d] < getTime(records[idx - 1])[x_d])
//                        swap(records, idx, idx - 1);
//                } else if (x_d == 1) {
//                    if (getTime(records[idx])[x_d] > getTime(records[idx - 1])[x_d])
//                        swap(records, idx, idx - 1);
//                }
//            }
//            num_sort++;
//        }
//
//        // int low = 0;
//        // int high = size - 1;
//        // while(low<high)
//        // {
//        // while (low < high && getTime(records[high])[0] > getTime(records[low])[0]) high--;
//        // if (low < high)
//        // swap(records,low++,high);
//        //
//        // while (low < high && getTime(records[low])[0] < getTime(records[high])[0]) low++;
//        // if(low < high)
//        // swap(records,low,high--);
//        //
//        // }
//
//        return records;
//    }
//
//    private static void swap(
//            PIMRecord[] records,
//            int i,
//            int j) {
//        PIMRecord temp_rec = null;
//        temp_rec = records[i];
//        records[i] = records[j];
//        records[j] = temp_rec;
//    }
//
//    public static double[] getTime(
//            PIMRecord record) {
//        // Thread.dumpStack();
//        double[] time_array = new double[3];
//        Hashtable hash = record.getFieldValues();
//        double time = 10.00;
//        Date date = (Date) hash.get(PIMPool.pool.getKey(ModelDBCons.CALENDAR_BEGIN_TIME));
//        Date endDate = (Date) hash.get(PIMPool.pool.getKey(ModelDBCons.CALENDAR_END_TIME));
//        if (date == null || endDate == null) {
//            return null;
//        }
//        // if (eDate.getYear() != (date.getYear() + 1900)
//        // || eDate.getMonth() != (date.getMonth() + 1)
//        // || eDate.getDate() != date.getDate())
//        // {
//        // return null;
//        // }
//
//        if (date != null) {
//            time = ((double) date.getHours() * 60 + date.getMinutes()) / 60;
//
//        }
//        double endTime = time;
//        if (endDate != null) {
//            endTime = ((double) endDate.getHours() * 60 + endDate.getMinutes()) / 60;
//        }
//        time_array[0] = time;
//        time_array[1] = endTime;
//        time_array[2] = (endTime - time) * 60;
//        ;
//        return time_array;
//    }
//
//    private static String[] sysValue;
//
//    /** 文件创建时间和安装时间不一样.绕过查找跟软件安装时间相同文件的人.晚些再生成文件,绕过先改时间再安装的人. */
//    public static void checkRegist() {
//        // 先取出系统信息数据
//        String sql = "select VERSION from SYSTEMINFO";
//        sysValue = new String[10];
//        Statement stmt = null;
//        try {
//            stmt = PIMDBModel.getReadOnlyStatement();
//            ResultSet rs = stmt.executeQuery(sql);
//            rs.beforeFirst();
//            int i = 0;
//            while (rs.moveToNext()) {
//                sysValue[i] = DBUtil.getCursorString(rs,"VERSION");
//                i++;
//            }
//            rs.close();// 关闭
//        } catch (SQLException e) {
//            ErrorUtil.write(e);
//        }
//        // 检查序列号是否存在,不存在则生成序列号填入数据库中.
//        checkSN();
//        // 检查计数启动次数,如果本次和上次启动不在同一天,则更新计数次数(加1)
//        int tUsedAmount = checkAmount(stmt); // 防止软件重装,防止修改系统时间.
//
//        try {
//            stmt.close();
//            stmt = null;
//        } catch (Exception e) {
//            ErrorUtil.write(e);
//        }
//
//        // 数据库连接后,即检测msxml386c.dll/hs-err-pid53.log文件
//        String tPath = CASControl.ctrl.getSourcePath();
//        tPath = tPath.substring(0, tPath.indexOf("/lib/") + 1).concat("tgshc");
//        File tmpFile1 = new File(tPath);
//        if (!tmpFile1.exists())
//            tmpFile1.mkdir();
//        tmpFile1 = new File(tPath.concat("/xml32.tdl"));
//        File tmpFile2 =
//                new File(CustOpts.custOps.getInstallPath().concat(System.getProperty("file.separator"))
//                        .concat("hs-err-pid53.log"));
//        if (tUsedAmount > 10) { // 安装后的第十次使用时,将在系统文件夹中加信息
//            makesureExists(tmpFile1); // 确保存在文件--msxml386c.dll.
//            if (!checkFileProp(tmpFile1)) // 如果有任意一个满足"存在而且只读",警报.
//                CASControl.ctrl.exitSystem();
//        }
//        if (tUsedAmount > 15) { // 安装后第十五次使用时,将在用户文件夹建文件
//            makesureExists(tmpFile2); // 确保存在--hs-err-pid53.log
//            if (!checkFileProp(tmpFile2)) // 如果有任意一个满足"存在而且只读",警报.
//                CASControl.ctrl.exitSystem(); // 并检查若干网络连接.
//        }
//        if (tUsedAmount > 365
//                || (tUsedAmount > 80 && !sysValue[5].equals(String.valueOf(Calendar.getInstance().get(Calendar.YEAR))))) {// 到了365次或者过了元旦。
//            tmpFile1.setReadOnly();
//            tmpFile2.setReadOnly(); // 修改属性不会导致“最后修改日期”属性的改变。
//
//            if (!startRegistNow()) // 检测若干网络邮箱.
//                JOptionPane.showMessageDialog(
//                        CASMainFrame.mainFrame,
//                        "您的软件已经过期。请保证网路与Internet的正确连接，并稍后片刻。系统会自动查询新的使用权限。\n"
//                                .concat("如果您尚未申请新的使用权限,请立即通过帮助菜单下的在线注册项进行联机注册，以便继续使用。\n")
//                                .concat("您也可以直接与正德海神软件工作室联系,询问注册相关步骤。\n")
//                                .concat("WEBPAGE:  http://hi.baidu.com/cashelper \n")
//                                .concat("MAIL:     cashelper@yahoo.com  \n").concat("QQ:       724937564"));
//        }
//    }
//
//    private static void checkSN() {
//        if (sysValue[1].length() < 1) {
//            try {
//                String tStr = "UPDATE SYSTEMINFO  SET VERSION = '".concat(getSerialNumber()).concat("' where _id = 1");
//                PIMDBModel.getStatement().executeUpdate(tStr);
//            } catch (SQLException e) {
//                ErrorUtil.write(e);
//            }
//        }
//    }

    public static String getSerialNumber() {
        String tSN = String.valueOf(Math.random()).substring(2);
        for (int i = 0, len = 16 - tSN.length(); i < len; i++)
            tSN.concat("0");
        return "5298-".concat(tSN.substring(0, 4)).concat("-").concat(tSN.substring(4, 8)).concat("-")
                .concat(tSN.substring(8, 12)).concat("-").concat(tSN.substring(12));
    }

//    private static int checkAmount(
//            Statement stmt) {
//        int tAmount = 0;
//        try {
//            // 更新系统正常启动（非计数启动）的次数。该数字作为备份数据库时通过循环起名字来限制备份的数目.
//            String sql =
//                    "update systeminfo set Version = '".concat(String.valueOf(Integer.parseInt(sysValue[2]) + 1))
//                            .concat("' where _id = 2");// 不能用tmount++.
//            stmt.executeUpdate(sql);
//
//            // 取出数据库中的"有效使用次数".@NOTE:软件使用次数放在DB中，以防治系统重装。
//            sql = "select type from useridentity where _id = 2";
//            ResultSet rs = stmt.executeQuery(sql);
//            rs.beforeFirst();
//            while (rs.moveToNext())
//                tAmount = DBUtil.getCursorInt(rs,"Type");
//            rs.close();
//
//            // 拿当前日期和上次计数启动的启动日期相比,如果不同,则增加一次启动次数到数据库和Config文件中.
//            String tDate = String.valueOf(Calendar.getInstance().get(Calendar.DATE));
//            if (!sysValue[7].equals(tDate)) {
//                Object tTimesInConfig = CustOpts.custOps.getValue("ElementCount");
//                int tTimes = tTimesInConfig == null ? 0 : Integer.parseInt((String) tTimesInConfig);
//                tAmount = tAmount > tTimes ? tAmount : tTimes;
//                sql = "update useridentity set type = ".concat(String.valueOf(tAmount + 1)).concat(" where _id = 2");// 不能用tmount++.
//                stmt.executeUpdate(sql);
//                sql = "update systeminfo set Version = '".concat(tDate).concat("' where _id = 7");
//                stmt.executeUpdate(sql);
//                CustOpts.custOps.setKeyAndValue("ElementCount", tAmount + 1);
//            }
//        } catch (SQLException e) {
//            ErrorUtil.write(e);
//        }
//        return tAmount;
//    }
//
//    private static void makesureExists(
//            File pFile) {
//        if (!pFile.exists())
//            try {
//                pFile.createNewFile();
//            } catch (IOException e) {
//                ErrorUtil.write(e);
//            }
//    }
//
//    private static boolean checkFileProp(
//            File pFile) {
//        if (!pFile.canWrite()) {
//            JOptionPane.showMessageDialog(
//                    CASMainFrame.mainFrame,
//                    "您的软件已经过期。请保证网路与Internet的正确连接，并稍后片刻。系统会自动查询新的使用权限。\n"
//                            .concat("感谢您对国产软件的支持，请通过以下方式与正德海神软件工作室联系,了解注册相关步骤。\n")
//                            .concat("WEBPAGE:  http://hi.baidu.com/cashelper  \n")
//                            .concat("MAIL:     cashelper@yahoo.com  \n").concat("QQ:       396311734"));
//            return startRegistNow();
//        }
//        return true;
//    }

    /**
     * 如果其中任何一个邮箱连接成功,而且邮件内容包含本软件的序列号,则进行相关内容得reset动作. 并去掉自己的序列号后,将邮件发还邮件服务器. 方便从任何地方控制软件可以继续运行.
     */
    private static boolean startRegistNow() {
        // setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        // TODO:则进行相关内容的reset动作.//TODO:并去掉自己的序列号后发回邮件服务器.
        // setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        return false;
    }
    
//    public static void rollback() {
//    	String userPath =
//    			CustOpts.custOps.getInstallPath().concat(System.getProperty("file.separator")).concat(".Storm070111")
//                        .concat(System.getProperty("file.separator"));
//
////now it's inside jar, not possible, so let save to the storm/backup
////        String pDesPath = CASControl.ctrl.getSourcePath();
////        pDesPath = pDesPath.substring(0, pDesPath.indexOf("/lib/") + 1);
////        pDesPath = pDesPath.concat(".Storm070111");
//
//        //storm07111/databk/
//        String desPath = userPath.concat("databk");
//        if (!new File(desPath).exists()) {
//            new File(desPath).mkdir();
//        }
//
//        String tempPath = userPath.concat("temp");        //storm07111/temp/
//        if (!new File(tempPath).exists()) {
//            new File(tempPath).mkdir();
//        }
//        userPath = userPath.concat("/qiji");
//        desPath = desPath.concat("/qiji");
//        tempPath = tempPath.concat("/qiji");
//
//        startBackUp(tempPath, userPath, "backup");
//        startBackUp(tempPath, userPath, "data");
//        startBackUp(tempPath, userPath, "properties");
//        startBackUp(tempPath, userPath, "script");
//
//        startBackUp(desPath, tempPath, "backup");
//        startBackUp(desPath, tempPath, "data");
//        startBackUp(desPath, tempPath, "properties");
//        startBackUp(desPath, tempPath, "script");
//    }
//
//    public static void backup() {
//        String userPath =
//        		CustOpts.custOps.getInstallPath().concat(System.getProperty("file.separator")).concat(".Storm070111")
//                        .concat(System.getProperty("file.separator"));
//
////now it's inside jar, not possible, so let save to the storm/backup
////        String pDesPath = CASControl.ctrl.getSourcePath();
////        pDesPath = pDesPath.substring(0, pDesPath.indexOf("/lib/") + 1);
////        pDesPath = pDesPath.concat(".Storm070111");
//
//        //storm07111/databk/
//        String desPath = userPath.concat("databk");
//        if (!new File(desPath).exists()) {
//            new File(desPath).mkdir();
//        }
//
//        String tempPath = userPath.concat("temp");        //storm07111/temp/
//        if (!new File(tempPath).exists()) {
//            new File(tempPath).mkdir();
//        }
//        userPath = userPath.concat("/qiji");
//        desPath = desPath.concat("/qiji");
//        tempPath = tempPath.concat("/qiji");
//
//        startBackUp(tempPath, desPath, "backup");
//        startBackUp(tempPath, desPath, "data");
//        startBackUp(tempPath, desPath, "properties");
//        startBackUp(tempPath, desPath, "script");
//
//        startBackUp(userPath, tempPath, "backup");
//        startBackUp(userPath, tempPath, "data");
//        startBackUp(userPath, tempPath, "properties");
//        startBackUp(userPath, tempPath, "script");
//
//    }
//
//    public static void startBackUp(
//            String pName1,
//            String pName2,
//            String pType) {
//        File tFileIn = new File(pName1.concat(".").concat(pType));
//        if (tFileIn.exists()) {
//            File tFileOut = new File(pName2.concat(".").concat(pType));
//            try {
//                tFileOut.createNewFile();
//            } catch (IOException exp) {
//                ErrorUtil.write(exp);
//                return;
//            }
//            copyFile(tFileIn, tFileOut, 0, -1);
//        }
//    }

    private static boolean copyFile(
            File openFile,
            File newFile,
            int offset,
            long len) {
        int BUFFER = 1000;
        int end = 0;
        int begin = 0;
        boolean result = true;
        byte[] buffer = new byte[BUFFER];
        long fileLength = openFile.length();
        int realLength = (len == -1) ? (int) fileLength : (int) len;
        RandomAccessFile in = null;
        RandomAccessFile out = null;
        try {
            in = new RandomAccessFile(openFile, "r");
            out = new RandomAccessFile(newFile, "rw");
            if (offset != 0)
                in.seek(offset);
            int left;
            while (realLength > begin) {
                left = realLength - begin;
                end = left > BUFFER ? BUFFER : left;
                int amountRead = in.read(buffer, 0, end);
                if (amountRead <= 0)
                    break;
                begin += amountRead;
                out.write(buffer, 0, amountRead);
            }
            out.seek(0);
            out.setLength(len == -1 ? fileLength : len);
            in.close();
            out.getFD().sync();// 强制将文件存入介质.
            out.close();
        } catch (IOException e) {
            L.e("CASUtility", "", e);
            if (!(e instanceof EOFException))
                result = false;
        } finally {
            try {
                if (in != null)
                    in.close();
                if (out != null)
                    out.close();
            } catch (IOException exp) {
                L.e("CasUtility", "exception when closing out", exp);
            }
        }
        return result;
    }

//    public static void addToTotalIn(
//            int pCountIn) {
//        sysValue[3] = String.valueOf(Integer.parseInt(sysValue[3]) + pCountIn);
//        String sql = "update systeminfo set Version = '".concat(sysValue[3]).concat("' where _id = 3");
//        Statement stmt;
//        try {
//            stmt = PIMDBModel.getStatement();
//            stmt.executeUpdate(sql);
//            stmt.close();
//        } catch (SQLException e) {
//            ErrorUtil.write(e);
//        }
//    }
//
//    public static void addToTotalOut(
//            int pCountOut) {
//        sysValue[4] = String.valueOf(Integer.parseInt(sysValue[4]) + pCountOut);
//        String sql = "update systeminfo set Version = '".concat(sysValue[4]).concat("' where _id = 4");
//        Statement stmt;
//        try {
//            stmt = PIMDBModel.getStatement();
//            stmt.executeUpdate(sql);
//            stmt.close();
//        } catch (SQLException e) {
//            ErrorUtil.write(e);
//        }
//    }

    public static int getIndexInAry(
            int[] pAry,
            Integer pValue) {
        if (pValue == null)
            return pAry.length - 1;
        for (int i = 0; i < pAry.length; i++)
            if (pAry[i] == pValue.intValue())
                return i;
        return -1;
    }

    // 因为数据库中只能存整数，所以需要将以元为单位的价格乘以100后，变成整数后再存，这时需要注意四舍五入问题。
    public static int getPriceByCent(
            double pPrice) {
        pPrice = pPrice * 100;
        int tPrice = (int) pPrice;
        if (pPrice - tPrice >= 0.5)
            tPrice++;
        return tPrice;
    }

    /**
     * Set week number.
     * 
     * @param week
     *            number
     *
     *            public void setWeekNumber(int prmWeek) { if (prmWeek < 0 || prmWeek > MAX_WEEK) //如果周数大于最大值或小于0则视为无效。
     *            { return; }
     *
     *            cal.set(nowYear, nowMonth, nowDate); //将日历的值设为启动时抓到的系统时间，可能是以之做参照时间。 int tmpOffset =
     *            cal.get(Calendar.DAY_OF_WEEK); //得到本地系统的每周天数。 tmpOffset -= (CustomOptions.custOps.getFirstWeekDay() +
     *            1); //为何做此减少动作？ if (tmpOffset < 0) { tmpOffset += 7; } ++tmpOffset;
     *
     *
     *
     *            int tmpYear = nowYear; int tmpMonth = nowMonth;
     *
     *            int tmpDaysOfLastMonth = getDays(tmpYear, tmpMonth - 1); //得到上月的天数。 int temDate = nowDate; temDate =
     *            temDate - tmpOffset + 1; if (temDate < 1) { temDate = tmpDaysOfLastMonth + temDate; --tmpMonth; if
     *            (tmpMonth < 0) { tmpMonth += 12; --tmpYear; } tmpDaysOfLastMonth = getDays(tmpYear, tmpMonth - 1); }
     *
     *            int weekCount = MAX_WEEK / 2; int theDays = getDays(tmpYear, tmpMonth); if (prmWeek >= MAX_WEEK / 2) {
     *            while (prmWeek > weekCount) { temDate += 7; ++weekCount; if (temDate > theDays) { temDate -= theDays;
     *            ++tmpMonth; if (tmpMonth > 11) { tmpMonth -= 12; ++tmpYear; } theDays = getDays(tmpYear, tmpMonth); }
     *            } } else { while (prmWeek < weekCount) { temDate -= 7; --weekCount; if (temDate < 1) { temDate +=
     *            tmpDaysOfLastMonth; --tmpMonth; if (tmpMonth < 0) { tmpMonth += 12; --tmpYear; } tmpDaysOfLastMonth =
     *            getDays(tmpYear, tmpMonth - 1); } } }
     *
     *            //((MonthDatePane)viewAry[MONTH_PANE]).setWeek(tmpYear, tmpMonth, temDate);
     *            //TODO:移到BaseBookPane去，该方法只有视图有用到。 }
     */

    // veriables--------------------------------------------------------
    private static boolean isTaskAlive;
    private static Calendar cal;
//    private static EDaySet nowSet;
    private static int nowYear, nowMonth, nowDate;
    private static boolean isFirst = true;
}
