 /*
  * Created On: Nov 19, 2006 10:26:39 AM
  */
 package com.thinkparity.ophelia.browser.util.swing.plaf;
 
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Rectangle;
 import java.awt.image.BufferedImage;
 
 import javax.swing.ButtonModel;
 import javax.swing.JButton;
 import javax.swing.JComponent;
 import javax.swing.JScrollBar;
 import javax.swing.SwingConstants;
 import javax.swing.plaf.ComponentUI;
 import javax.swing.plaf.basic.BasicScrollBarUI;
 
 import com.thinkparity.codebase.assertion.Assert;
 
 import com.thinkparity.ophelia.browser.util.ImageIOUtil;
 
 /**
  * <b>Title:</b>thinkParity 
  * @author raymond@thinkparity.com
  * @version 1.1.2.1
  */
 public class ThinkParityScrollBarUI extends BasicScrollBarUI {
 
     /**
      * Create a thinkParity scroll bar ui.
      * 
      * @param c
      *            A <code>JComponent</code>.
      * @return A <code>ComponentUI</code>.
      */
     public static ComponentUI createUI(final JComponent c) {
         return new ThinkParityScrollBarUI();
     }
 
     /** The series of <code>BufferedImage</code>s for the scroll down button. */
     private static final BufferedImage[] SCROLL_DOWN;
 
     /** The series of <code>BufferedImage</code>s for the scroll left button. */
     private static final BufferedImage[] SCROLL_LEFT;
 
     /** The series of <code>BufferedImage</code>s for the scroll right button. */
     private static final BufferedImage[] SCROLL_RIGHT;
 
     /** The series of <code>BufferedImage</code>s for the scroll up button. */
     private static final BufferedImage[] SCROLL_UP;
 
     /** The series of <code>BufferedImage</code>s for the horizontal thumb. */
     private static final BufferedImage[][] THUMB_HORIZ;
 
     /** The series of <code>BufferedImage</code>s for the vertical thumb. */
     private static final BufferedImage[][] THUMB_VERT;
 
     /** The <code>BufferedImage</code>s for the horizontal track. */
     private static final BufferedImage[] TRACK_HORIZ;
 
     /** The <code>BufferedImage</code>s for the vertical track. */
     private static final BufferedImage[] TRACK_VERT;
 
     static {
         SCROLL_DOWN = new BufferedImage[] {
                 ImageIOUtil.read("ScrollDownButton.png"),
                 ImageIOUtil.read("ScrollDownButtonRollover.png"),
                 ImageIOUtil.read("ScrollDownButtonPressed.png") };
         SCROLL_LEFT = new BufferedImage[] {
                 ImageIOUtil.read("ScrollLeftButton.png"),
                 ImageIOUtil.read("ScrollLeftButtonRollover.png"),
                 ImageIOUtil.read("ScrollLeftButtonPressed.png") };
         SCROLL_RIGHT = new BufferedImage[] {
                 ImageIOUtil.read("ScrollRightButton.png"),
                 ImageIOUtil.read("ScrollRightButtonRollover.png"),
                 ImageIOUtil.read("ScrollRightButtonPressed.png") };
         SCROLL_UP = new BufferedImage[] {
                 ImageIOUtil.read("ScrollUpButton.png"),
                 ImageIOUtil.read("ScrollUpButtonRollover.png"),
                 ImageIOUtil.read("ScrollUpButtonPressed.png") };
         THUMB_HORIZ = new BufferedImage[2][4];
         THUMB_HORIZ[0][0] = ImageIOUtil.read("ScrollHorizThumbLeft.png");
         THUMB_HORIZ[0][1] = ImageIOUtil.read("ScrollHorizThumbHash.png");
         THUMB_HORIZ[0][2] = ImageIOUtil.read("ScrollHorizThumbRight.png");
         THUMB_HORIZ[0][3] = ImageIOUtil.read("ScrollHorizThumb.png");
         THUMB_HORIZ[1][0] = ImageIOUtil.read("ScrollHorizThumbPressedLeft.png");
         THUMB_HORIZ[1][1] = ImageIOUtil.read("ScrollHorizThumbPressedHash.png");
         THUMB_HORIZ[1][2] = ImageIOUtil.read("ScrollHorizThumbPressedRight.png");
         THUMB_HORIZ[1][3] = ImageIOUtil.read("ScrollHorizThumbPressed.png");
         THUMB_VERT = new BufferedImage[2][4];
         THUMB_VERT[0][0] = ImageIOUtil.read("ScrollVertThumbTop.png");
         THUMB_VERT[0][1] = ImageIOUtil.read("ScrollVertThumbHash.png");
         THUMB_VERT[0][2] = ImageIOUtil.read("ScrollVertThumbBottom.png");
         THUMB_VERT[0][3] = ImageIOUtil.read("ScrollVertThumb.png");
         THUMB_VERT[1][0] = ImageIOUtil.read("ScrollVertThumbPressedTop.png");
         THUMB_VERT[1][1] = ImageIOUtil.read("ScrollVertThumbPressedHash.png");
         THUMB_VERT[1][2] = ImageIOUtil.read("ScrollVertThumbPressedBottom.png");
         THUMB_VERT[1][3] = ImageIOUtil.read("ScrollVertThumbPressed.png");
         TRACK_HORIZ = new BufferedImage[] {
                 ImageIOUtil.read("ScrollHorizTrack.png") };
         TRACK_VERT = new BufferedImage[] {
                 ImageIOUtil.read("ScrollVertTrack.png") };
     }
 
     /**
      * Create ThinkParityScrollBarUI.
      * 
      */
     public ThinkParityScrollBarUI() {
         super();
     }
 
     /**
      * @see javax.swing.plaf.basic.BasicScrollBarUI#createDecreaseButton(int)
      * 
      */
     @Override
     protected JButton createDecreaseButton(final int orientation) {
         if (SwingConstants.NORTH == orientation)
             return new ScrollButton(SCROLL_UP);
         else if (SwingConstants.EAST == orientation
                 || SwingConstants.WEST == orientation)
             return new ScrollButton(SCROLL_LEFT);
         else
             throw Assert.createUnreachable("Unknown orientation.");
     }
 
     /**
      * @see javax.swing.plaf.basic.BasicScrollBarUI#createIncreaseButton(int)
      * 
      */
     @Override
     protected JButton createIncreaseButton(final int orientation) {
         if (SwingConstants.SOUTH == orientation)
             return new ScrollButton(SCROLL_DOWN);
         else if (SwingConstants.EAST == orientation
                     || SwingConstants.WEST == orientation)
             return new ScrollButton(SCROLL_RIGHT);
         else
             throw Assert.createUnreachable("Unknown orientation.");
     }
 
     /**
      * @see javax.swing.plaf.basic.BasicScrollBarUI#installDefaults()
      * 
      */
     @Override
     protected void installDefaults() {
         super.installDefaults();
         this.minimumThumbSize = new Dimension(17, 17);
     }
 
     /**
      * @see javax.swing.plaf.basic.BasicScrollBarUI#paintThumb(java.awt.Graphics,
      *      javax.swing.JComponent, java.awt.Rectangle)
      * 
      */
     @Override
     protected void paintThumb(final Graphics g, final JComponent c,
             final Rectangle thumbBounds) {
         if (JScrollBar.VERTICAL == scrollbar.getOrientation())
             paintThumbVertical(THUMB_VERT, g, c, thumbBounds);
         else if (JScrollBar.HORIZONTAL == this.scrollbar.getOrientation())
             paintThumbHorizontal(THUMB_HORIZ, g, c, thumbBounds);
         else
             throw Assert.createUnreachable("Unknown scrollbar orientation.");
     }
 
     /**
      * Paint the horizontal "thumb" of a scrollbar.
      * 
      * @param images
      *            The images that constitute the horizonal scrollbar.
      * @param g
      *            The <code>Graphics</code> to paint on.
      * @param c
      *            The <code>JComponent</code>.
      * @param thumbBounds
      *            The thumb bounds <code>Rectangle</code>.
      */
     protected void paintThumbHorizontal(final BufferedImage[][] images,
             final Graphics g, final JComponent c, final Rectangle thumbBounds) {
         final BufferedImage[] i;
         if (isDragging)
             i = images[1];
         else
             i = images[0];
         final Graphics2D g2 = (Graphics2D) g;
         final int defaultWidth = (thumbBounds.width - (i[0].getWidth() + i[1].getWidth() + i[2].getWidth())) / 2;
 
         // left
         g2.drawImage(i[0], thumbBounds.x, thumbBounds.y, null);
         // left half
         g2.drawImage(i[3], thumbBounds.x + i[0].getWidth(),
                 thumbBounds.y, defaultWidth, i[3].getHeight(),
                 null);
         // right half
         g2.drawImage(i[3],
                 thumbBounds.x + i[0].getWidth() + defaultWidth + i[1].getWidth(),
                 thumbBounds.y, defaultWidth + 2, i[3].getHeight(),
                 null);
         // right
         g2.drawImage(i[2],
                 thumbBounds.x + thumbBounds.width - i[2].getWidth(),
                 thumbBounds.y, null);
         // hash
         g2.drawImage(i[1],
                 thumbBounds.x + i[0].getWidth() + defaultWidth,
                 thumbBounds.y, null);
     }
 
     /**
      * Paint the vertical "thumb" of a scrollbar.
      * 
      * @param images
      *            The images that constitute the horizonal scrollbar.
      * @param g
      *            The <code>Graphics</code> to paint on.
      * @param c
      *            The <code>JComponent</code>.
      * @param thumbBounds
      *            The thumb bounds <code>Rectangle</code>.
      */
     protected void paintThumbVertical(final BufferedImage[][] images,
             final Graphics g, final JComponent c, final Rectangle thumbBounds) {
         final BufferedImage[] i;
         if (isDragging)
             i = images[1];
         else
             i = images[0];
         final Graphics2D g2 = (Graphics2D) g;
         final int defaultHeight = (thumbBounds.height - (i[0].getHeight() + i[1].getHeight() + i[2].getHeight())) / 2;
 
         // top
         g2.drawImage(i[0], thumbBounds.x, thumbBounds.y, null);
         // top half
         g2.drawImage(i[3], thumbBounds.x,
                 thumbBounds.y + i[0].getHeight(), i[3].getWidth(),
                 defaultHeight,
                 null);
         // bottom half
         g2.drawImage(i[3], thumbBounds.x,
                 thumbBounds.y + i[0].getHeight() + defaultHeight + i[1].getHeight(),
                 i[3].getWidth(), defaultHeight + 2,
                 null);
         // bottom
         g2.drawImage(i[2], thumbBounds.x,
                 thumbBounds.y + thumbBounds.height - i[2].getHeight(), null);
         // hash
         g2.drawImage(i[1], thumbBounds.x,
                 thumbBounds.y + i[0].getHeight() + defaultHeight, null);
     }
 
     /**
      * @see javax.swing.plaf.basic.BasicScrollBarUI#paintTrack(java.awt.Graphics,
      *      javax.swing.JComponent, java.awt.Rectangle)
      * 
      */
     @Override
     protected void paintTrack(final Graphics g, final JComponent c,
             final Rectangle trackBounds) {
         if (JScrollBar.VERTICAL == scrollbar.getOrientation())
             paintTrackVertical(TRACK_VERT, g, c, trackBounds);
         else if (JScrollBar.HORIZONTAL == this.scrollbar.getOrientation())
             paintTrackHorizontal(TRACK_HORIZ, g, c, trackBounds);
         else
             throw Assert.createUnreachable("Unknown scrollbar orientation.");
     }
 
     /**
      * Paint the horizontal track.
      * 
      * @param images
      *            The horizontal track <code>BufferedImage</code>s.
      * @param g
      *            The <code>Graphics</code>.
      * @param c
      *            The <code>JComponent</code>.
      * @param trackBounds
      *            The track bounds <code>Rectangle</code>.
      */
     protected void paintTrackHorizontal(final BufferedImage[] images,
             final Graphics g, final JComponent c, final Rectangle trackBounds) {
         final Graphics2D g2 = (Graphics2D) g;
         g2.drawImage(images[0], trackBounds.x, trackBounds.y, trackBounds.width,
                 trackBounds.height, null);
     }
 
     /**
      * Paint the vertical track.
      * 
      * @param images
      *            The horizontal track <code>BufferedImage</code>s.
      * @param g
      *            The <code>Graphics</code>.
      * @param c
      *            The <code>JComponent</code>.
      * @param trackBounds
      *            The track bounds <code>Rectangle</code>.
      */
     protected void paintTrackVertical(final BufferedImage[] images,
             final Graphics g, final JComponent c, final Rectangle trackBounds) {
         final Graphics g2 = (Graphics2D) g;
         g2.drawImage(images[0], trackBounds.x, trackBounds.y, trackBounds.width,
                 trackBounds.height, null);
     }
 
     /**
      * A scroll button (up, down, left, right) that uses a predefined set of
      * images to display the button based upon the button model's state.
      * 
      */
     private static class ScrollButton extends JButton {
         private final BufferedImage[] images;
         private ScrollButton(final BufferedImage[] images) {
             super();
             this.images = images;
         }
         @Override
         public Dimension getPreferredSize() {
             final Dimension size = super.getPreferredSize();
             size.height = images[0].getHeight();
             size.width = images[0].getWidth();
             return size;
         }
         @Override
         public void paint(final Graphics g) {
             final Graphics2D g2 = (Graphics2D) g;
             final ButtonModel model = getModel();
             if (model.isRollover())
                 if (model.isPressed())
                     g2.drawImage(images[2], 0, 0, null);
                 else
                     g2.drawImage(images[1], 0, 0, null);
             else
                 g2.drawImage(images[0], 0, 0, null);
         }
     }
 }
