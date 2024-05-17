package com.just.print.ui.panel;

import android.media.Image;
import android.os.Bundle;

import com.just.print.app.BaseFragment;

/**
 * 面板基类，主要实现两个功能：1、绘制背景图片；2、将自己设为自己的布局管理器（子类只需重写layoutContainer方法， 即可定制自己的布局方案）。 TODO:
 * 可以改善：如果图片尺寸大，则缩小至this的尺寸，如果图片小，则平铺。
 */

public class PicturePane extends BaseFragment //implements LayoutManager2
{
	public int MAX_CMDBTN_QT;
	Image image;
//    public PicturePane(Image image) {
//        if (image != null) {// 如果传入的图片不是空，则用它初始化picture对象。
//        	this.image = image;
//            preparePicture();
//        }
//        setDoubleBuffered(true);
//        setLayout(this); // 改变布局管理器。
//    }

    public void preparePicture() {
    	preparePicture(this.image);
    }
    /**
     * 初始化图片
     * 
     * @param image
     *            传入的图片
     * @called by: emo.pim.pimview.CoverPane;
     */
    public void preparePicture(Image image) {
//    	int width = getWidth();
//    	int height = getHeight();
//    	if(width == 0 || height == 0) {
//    		Dimension scrSize = Toolkit.getDefaultToolkit().getScreenSize();
//    		width = scrSize.width;
//    		height = scrSize.height;
//    	}
//    	image = image.getScaledInstance(width, height, Image.SCALE_DEFAULT);
//        MediaTracker track = new MediaTracker(this);
//        track.addImage(image, 0);
//        try {
//            track.waitForID(0);
//        } catch (InterruptedException e) {
//        }
//
//        int tmpImgWidth = image.getWidth(this);
//        int tmpImgHeight = image.getHeight(this);
//        if(tmpImgWidth > 0 && tmpImgHeight > 0) {
//	        Rectangle2D tmpRect = new Rectangle2D.Double(0, 0, tmpImgWidth, tmpImgHeight);
//	        BufferedImage tmpImgBuf = new BufferedImage(tmpImgWidth, tmpImgHeight, BufferedImage.SCALE_FAST);
//	        tmpImgBuf.getGraphics().drawImage(image, 0, 0, this);
//	        picture = new TexturePaint(tmpImgBuf, tmpRect);
//        }
    }

//    // ------------------------------------------------------------------------
//    /**
//     * 绘制组件的内容
//     *
//     * @param g
//     *            绘制图形的句柄
//     */
//    public void paintComponent(Graphics g) {
//        // if (skipPaint)改标记暂时尚未用到,故先注掉,等用到时打开.
//        // {
//        // return ;
//        // }
//        if (picture == null) {
//            g.setColor(getBackground());
//        } else {
//            ((Graphics2D) g).setPaint(picture);
//        }
//        // 如果实例的paintAreaWidth,paintAreaHeigh属性被赋予过>=0的值,则按照赋予的值的范围进行绘制.
//        // @TODO:补注释:什么情况下需要对实际的绘制范围进行设置?
//        if (paintAreaWidth > -1 && paintAreaHeight > -1) {
//            g.fillRect(paintAreaX, paintAreaY, paintAreaWidth, paintAreaHeight);
//        } else // 否则按照实例的实际面积进行绘制.
//        {
//            g.fillRect(paintAreaX, paintAreaY, getWidth(), getHeight());
//        }
//    }

    /**
     * 设置绘制区域
     * 
     * @called by: emo.pim.pimview.CoverPane;
     * @param prmX
     *            X坐标
     * @param prmY
     *            Y坐标
     * @param prmWidth
     *            宽度
     * @param prmHeight
     *            高度
     */
    protected void setPaintArea(
            int prmX,
            int prmY,
            int prmWidth,
            int prmHeight) {
        paintAreaX = prmX;
        paintAreaY = prmY;
        paintAreaWidth = prmWidth;
        paintAreaHeight = prmHeight;
    }

    // 以下为实现LayoutManager2的接口-------------------------------------------------
//    /**
//     * Adds the specified component to the layout, using the specified constraint object.
//     *
//     * @param comp
//     *            the component to be added
//     * @param constraints
//     *            where/how the component is added to the layout.
//     */
//    public void addLayoutComponent(Component comp, Object constraints) {}
//
//    /**
//     * If the layout manager uses a per-component string, adds the component <code>comp</code> to the layout,
//     * associating it with the string specified by <code>name</code>.
//     *
//     * @param name
//     *            the string to be associated with the component
//     * @param comp
//     *            the component to be added
//     */
//    public void addLayoutComponent(String name, Component comp) {}
//
//    /**
//     * Returns the alignment along the x axis. This specifies how the component would like to be aligned relative to
//     * other components. The value should be a number between 0 and 1 where 0 represents alignment along the origin, 1
//     * is aligned the furthest away from the origin, 0.5 is centered, etc.
//     */
//    public float getLayoutAlignmentX(Container target) {return 0.0f;}
//
//    /**
//     * Returns the alignment along the y axis. This specifies how the component would like to be aligned relative to
//     * other components. The value should be a number between 0 and 1 where 0 represents alignment along the origin, 1
//     * is aligned the furthest away from the origin, 0.5 is centered, etc.
//     */
//    public float getLayoutAlignmentY(Container target) {return 0.0f;}
//
//    /**
//     * Invalidates the layout, indicating that if the layout manager has cached information it should be discarded.
//     */
//    public void invalidateLayout(Container target) {}
//
//    /**
//     * *调自己的将有子类实现的方法实现布局.Lays out the specified container.
//     *
//     * @param parent
//     *            the container to be laid out
//     */
//    public void layoutContainer(Container parent) {}
//
//    /**
//     * Calculates the maximum size dimensions for the specified container, given the components it contains.
//     *
//     * @see java.awt.Component#getMaximumSize
//     * @see LayoutManager
//     */
//    public Dimension maximumLayoutSize(Container target) {return getSize();}
//
//    /**
//     * Calculates the minimum size dimensions for the specified container, given the components it contains.
//     *
//     * @param parent
//     *            the component to be laid out
//     * @see #preferredLayoutSize
//     */
//    public Dimension minimumLayoutSize(Container parent) {return new Dimension(0, 0);}
//
//    /**
//     * Calculates the preferred size dimensions for the specified container, given the components it contains.
//     *
//     * @param parent
//     *            the container to be laid out
//     * @see #minimumLayoutSize
//     */
//    public Dimension preferredLayoutSize(Container parent) {return getSize();}
//
//    /**
//     * Removes the specified component from the layout.
//     *
//     * @param comp
//     *            the component to be removed
//     */
//    public void removeLayoutComponent(Component comp) {}
//
//    // over-----------------------------------------------
//    private TexturePaint picture; // 两边纹理绘制的图片/
    protected int paintAreaX; // 绘制的X坐标/
    protected int paintAreaY; // 绘制的Y坐标/
    protected int paintAreaWidth = -1; // 绘制的宽度/
    protected int paintAreaHeight = -1; // 绘制的高度/

    @Override
    protected int getLayoutId() {
        return 0;
    }

    @Override
    public void onCreated(Bundle savedInstanceState) {

    }
}
