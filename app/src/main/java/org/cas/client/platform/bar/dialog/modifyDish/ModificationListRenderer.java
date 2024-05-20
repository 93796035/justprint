package org.cas.client.platform.bar.dialog.modifyDish;

//import javax.swing.JList;
//import javax.swing.ListModel;
//import javax.swing.ListSelectionModel;

public class ModificationListRenderer
//        extends JCheckBox implements ListCellRenderer, Serializable
{

//    /**
//     * 创建一个 Ca tegoryListRenderer 的实例
//     */
//    public ModificationListRenderer() {
//
//        super();
//        if (noFocusBorder == null) {
//            noFocusBorder = new EmptyBorder(1, 1, 1, 1);
//        }
//        setOpaque(true);
//        setBorder(noFocusBorder);
//    }

    /**
     * Return a component that has been configured to display the specified value. That component's <code>paint</code>
     * method is then called to "render" the cell. If it is necessary to compute the dimensions of a list because the
     * list cells do not have a fixed size, this method is called to generate a component on which
     * <code>getPreferredSize</code> can be invoked.
     *
     * @param list
     *            The JList we're painting.
     * @param value
     *            The value returned by list.getModel().getElementAt(index).
     * @param index
     *            The cells index.
     * @param isSelected
     *            True if the specified cell was selected.
     * @param cellHasFocus
     *            True if the specified cell has the focus.
     * @return A component whose paint() method will render the specified value.
     *
     * @see JList
     * @see ListSelectionModel
     * @see ListModel
     */
//    public Component getListCellRendererComponent(
//            JList list,
//            Object value,
//            int index,
//            boolean isSelected,
//            boolean cellHasFocus) {
//        setComponentOrientation(list.getComponentOrientation());
//        if (isSelected) {
//            setBackground(list.getSelectionBackground());
//            setForeground(list.getSelectionForeground());
//        } else {
//            setBackground(list.getBackground());
//            setForeground(list.getForeground());
//        }
//
//        if (value instanceof CheckItem) {
//            CheckItem tmpCheckItem = (CheckItem) value;
//            setSelected(tmpCheckItem.isSelected());
//            setText(tmpCheckItem.getName());
//        } else {
//            setSelected(false);
//            setText((value == null) ? CASUtility.EMPTYSTR : value.toString());
//        }
//
//        setEnabled(list.isEnabled());
//        Font font = new Font("宋体",Font.PLAIN,36);
//        setFont(font);
//        setBorder((cellHasFocus) ? UIManager.getBorder("List.focusCellHighlightBorder") : noFocusBorder);
//
//        return this;
//    }

    /**
     * Overridden for performance reasons. See the <a href="#override">Implementation Note</a> for more information.
     */
    public void validate() {
    }

    /**
     * Overridden for performance reasons. See the <a href="#override">Implementation Note</a> for more information.
     */
    public void revalidate() {
    }

    /**
     * Overridden for performance reasons. See the <a href="#override">Implementation Note</a> for more information.
     * 
     * @param tm
     *            时间
     * @param x
     *            X坐标
     * @param y
     *            Y坐标
     * @param width
     *            宽度
     * @param height
     *            高度
     */
    public void repaint(
            long tm,
            int x,
            int y,
            int width,
            int height) {
    }

//    /**
//     * Overridden for performance reasons. See the <a href="#override">Implementation Note</a> for more information.
//     *
//     * @param r
//     *            一个矩形
//     */
//    public void repaint(
//            Rectangle r) {
//    }

//    /**
//     * Overridden for performance reasons. See the <a href="#override">Implementation Note</a> for more information.
//     */
//    protected void firePropertyChange(
//            String propertyName,
//            Object oldValue,
//            Object newValue) {
//        // Strings get interned...
//        if (propertyName == "text") {
//            super.firePropertyChange(propertyName, oldValue, newValue);
//        }
//    }

    /**
     * Overridden for performance reasons. See the <a href="#override">Implementation Note</a> for more information.
     * 
     * @param propertyName
     *            属性名
     * @param oldValue
     *            旧值
     * @param newValue
     *            新值
     */
    public void firePropertyChange(
            String propertyName,
            byte oldValue,
            byte newValue) {
    }

    /**
     * Overridden for performance reasons. See the <a href="#override">Implementation Note</a> for more information.
     * 
     * @param propertyName
     *            属性名
     * @param oldValue
     *            旧值
     * @param newValue
     *            新值
     */
    public void firePropertyChange(
            String propertyName,
            char oldValue,
            char newValue) {
    }

    /**
     * Overridden for performance reasons. See the <a href="#override">Implementation Note</a> for more information.
     * 
     * @param propertyName
     *            属性名
     * @param oldValue
     *            旧值
     * @param newValue
     *            新值
     */
    public void firePropertyChange(
            String propertyName,
            short oldValue,
            short newValue) {
    }

    /**
     * Overridden for performance reasons. See the <a href="#override">Implementation Note</a> for more information.
     * 
     * @param propertyName
     *            属性名
     * @param oldValue
     *            旧值
     * @param newValue
     *            新值
     */
    public void firePropertyChange(
            String propertyName,
            int oldValue,
            int newValue) {
    }

    /**
     * Overridden for performance reasons. See the <a href="#override">Implementation Note</a> for more information.
     * 
     * @param propertyName
     *            属性名
     * @param oldValue
     *            旧值
     * @param newValue
     *            新值
     */
    public void firePropertyChange(
            String propertyName,
            long oldValue,
            long newValue) {
    }

    /**
     * Overridden for performance reasons. See the <a href="#override">Implementation Note</a> for more information.
     * 
     * @param propertyName
     *            属性名
     * @param oldValue
     *            旧值
     * @param newValue
     *            新值
     */
    public void firePropertyChange(
            String propertyName,
            float oldValue,
            float newValue) {
    }

    /**
     * Overridden for performance reasons. See the <a href="#override">Implementation Note</a> for more information.
     * 
     * @param propertyName
     *            属性名
     * @param oldValue
     *            旧值
     * @param newValue
     *            新值
     */
    public void firePropertyChange(
            String propertyName,
            double oldValue,
            double newValue) {
    }

    /**
     * Overridden for performance reasons. See the <a href="#override">Implementation Note</a> for more information.
     * 
     * @param propertyName
     *            属性名
     * @param oldValue
     *            旧值
     * @param newValue
     *            新值
     */
    public void firePropertyChange(
            String propertyName,
            boolean oldValue,
            boolean newValue) {
    }

//    protected static Border noFocusBorder;
//
//    /**
//     * A subclass of DefaultListCellRenderer that implements UIResource. DefaultListCellRenderer doesn't implement
//     * UIResource directly so that applications can safely override the cellRenderer property with
//     * DefaultListCellRenderer subclasses.
//     * <p>
//     * <strong>Warning:</strong> Serialized objects of this class will not be compatible with future Swing releases. The
//     * current serialization support is appropriate for short term storage or RMI between applications running the same
//     * version of Swing. As of 1.4, support for long term storage of all JavaBeans<sup><font size="-2">TM</font></sup>
//     * has been added to the <code>java.beans</code> package. Please see {@link java.beans.XMLEncoder}.
//     */
//    public static class UIResource extends ModificationListRenderer implements javax.swing.plaf.UIResource {
//    }

}
