package org.cas.client.platform.bar.dialog.modifyDish;

/**
 * 本类封装一个字符串和一个布尔型对象
 */

public class CheckItem {

    /**
     * 创建一个 CheckItem 的实例
     * 
     * @param prmName
     *            标签
     * @param prmIsSelected
     *            是否选中
     */
    public CheckItem(String prmName, boolean prmIsSelected) {
        isSelected = prmIsSelected ? Boolean.TRUE : Boolean.FALSE;
        string = prmName;
    }

    /**
     * 创建一个 CheckItem 的实例
     * 
     * @param prmIsSelected
     *            是否选中
     * @param prmName
     *            标签
     */
    public CheckItem(boolean prmIsSelected, String prmName) {
        isSelected = prmIsSelected ? Boolean.TRUE : Boolean.FALSE;
        string = prmName;
    }

    /**
     * 创建一个 CheckItem 的实例
     * 
     * @param prmArray
     *            数据内容
     */
    public CheckItem(Object[] prmArray) {
        isSelected = (Boolean) prmArray[0];
        string = (String) prmArray[1];
    }

    /**
     * 返回选中状态
     * 
     * @return 是否选中
     */
    public boolean isSelected() {
        return isSelected.booleanValue();
    }

    /**
     * 返回字符串
     * 
     * @return 标签字符串
     */
    public String getName() {
        return string;
    }

    /** 保存是否选中的状态 */
    private Boolean isSelected;
    /** 保存一个字符串 */
    private String string;
}
