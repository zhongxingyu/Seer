 /**
  * Created On: Mar 6, 2007 10:39:07 PM
  * $Id$
  */
 package com.thinkparity.ophelia.browser.util.swing.plaf;
 
 import java.awt.*;
 import java.awt.image.BufferedImage;
 
 import javax.swing.AbstractButton;
 import javax.swing.ButtonModel;
 import javax.swing.JComponent;
 import javax.swing.LookAndFeel;
 import javax.swing.plaf.ComponentUI;
 import javax.swing.plaf.basic.BasicButtonUI;
 import javax.swing.plaf.basic.BasicGraphicsUtils;
 
 import com.thinkparity.ophelia.browser.util.ImageIOUtil;
 
 import sun.swing.SwingUtilities2;
 
 /**
  * @author rob_masako@shaw.ca
  * @version $Revision$
  */
 public class ThinkParityButtonUI extends BasicButtonUI {
 
     /**
      * Create a thinkParity button ui.
      * 
      * @param c
      *            A <code>JComponent</code>.
      * @return A <code>ComponentUI</code>.
      */
     public static ComponentUI createUI(final JComponent c) {
         return new ThinkParityButtonUI();
     }
 
     /**
      * A client property. When set to Boolean.TRUE, the "windows" look is
      * used instead of the "thinkParity shadow" look. Use for small buttons.
      */
     static final String WINDOWS_BUTTON_STYLE_CLIENT_KEY =
         new StringBuilder("ThinkParityButtonUI.windowsButtonStyle").toString();
 
     /** The series of <code>BufferedImage</code>s for the button bottom. */
     private static final BufferedImage[] BUTTON_BOTTOM;
 
     /** The series of <code>BufferedImage</code>s for the button left. */
     private static final BufferedImage[] BUTTON_LEFT;
 
     /** The series of <code>BufferedImage</code>s for the button middle. */
     private static final BufferedImage[] BUTTON_MIDDLE;
 
     /** The series of <code>BufferedImage</code>s for the button right. */
     private static final BufferedImage[] BUTTON_RIGHT;
 
     /** The series of <code>BufferedImage</code>s for the button top. */
     private static final BufferedImage[] BUTTON_TOP;
 
     /** Disabled text colour. */
     private static final Color COLOR_DISABLED_TEXT;
 
     /** The fill colour for pressed button, windows style. */
     private static final Color COLOR_FILL_PRESSED_WINDOWS_STYLE;
 
     /** Focus rectangle colour. */  
     private static final Color COLOR_FOCUS_RECT;
 
     /** Transparent colour. */
     private static final Color COLOR_TRANSPARENT;
 
     /** The colours for the bottom line. */
     private static final Color[] COLORS_BOTTOM;
 
     /** The colours for the pressed outline, windows style. */
     private static final Color[] COLORS_PRESSED_WINDOWS_STYLE;
 
     /** The colours for the rollover outline, windows style. */
     private static final Color[] COLORS_ROLLOVER_WINDOWS_STYLE;
 
     /** The colours for the top line. */
     private static final Color[] COLORS_TOP;
 
     /** Focus rectangle inset X. */  
     private static final int FOCUS_RECT_INSET_X;
 
     /** Focus rectangle inset X. */  
     private static final int FOCUS_RECT_INSET_Y;
 
     /** Focus rectangle inset X for windows style buttons. */
     private static final int FOCUS_RECT_INSET_X_WINDOWS_STYLE;
 
     /** Focus rectangle inset Y for windows style buttons. */
     private static final int FOCUS_RECT_INSET_Y_WINDOWS_STYLE;
 
     /** The scaled background images for the button bottom. */
     private BufferedImage[] scaledButtonBottom;  
 
     /** The scaled background images for the button left. */
     private BufferedImage[] scaledButtonLeft;    
 
     /** The scaled background images for the button middle. */
     private BufferedImage[] scaledButtonMiddle;
 
     /** The scaled background images for the button right. */
     private BufferedImage[] scaledButtonRight;
 
     /** The scaled background images for the button top. */
     private BufferedImage[] scaledButtonTop;
 
     static {
         BUTTON_BOTTOM = new BufferedImage[] {
                 ImageIOUtil.read("ButtonNormalBottom.png"),
                 ImageIOUtil.read("ButtonRolloverBottom.png"),
                 ImageIOUtil.read("ButtonPressedBottom.png"),
                 ImageIOUtil.read("ButtonDisabledBottom.png") };
         BUTTON_LEFT = new BufferedImage[] {
                 ImageIOUtil.read("ButtonNormalLeft.png"),
                 ImageIOUtil.read("ButtonRolloverLeft.png"),
                 ImageIOUtil.read("ButtonPressedLeft.png"),
                 ImageIOUtil.read("ButtonDisabledLeft.png") };
         // Note that the rollover middle is the same as the normal middle
         BUTTON_MIDDLE = new BufferedImage[] {
                 ImageIOUtil.read("ButtonNormalMiddle.png"),
                 ImageIOUtil.read("ButtonNormalMiddle.png"),
                 ImageIOUtil.read("ButtonPressedMiddle.png"),
                 ImageIOUtil.read("ButtonDisabledMiddle.png") };
         BUTTON_RIGHT = new BufferedImage[] {
                 ImageIOUtil.read("ButtonNormalRight.png"),
                 ImageIOUtil.read("ButtonRolloverRight.png"),
                 ImageIOUtil.read("ButtonPressedRight.png"),
                 ImageIOUtil.read("ButtonDisabledRight.png") };
         BUTTON_TOP = new BufferedImage[] {
                 ImageIOUtil.read("ButtonNormalTop.png"),
                 ImageIOUtil.read("ButtonRolloverTop.png"),
                 ImageIOUtil.read("ButtonPressedTop.png"),
                 ImageIOUtil.read("ButtonDisabledTop.png") };
         COLORS_BOTTOM = new Color[] {
                 Color.WHITE,
                 Color.WHITE,
                 new Color(222, 222, 222, 255),
                 new Color(197, 197, 197, 223) };
         COLORS_PRESSED_WINDOWS_STYLE = new Color[] {
                 new Color(157, 157, 146, 255),
                 new Color(179, 178, 168, 255),
                 new Color(208, 208, 197, 255) };
         COLORS_ROLLOVER_WINDOWS_STYLE = new Color[] {
                 new Color(206, 206, 195, 255),
                 new Color(216, 216, 205, 255),
                 new Color(228, 228, 217, 255) };
         COLORS_TOP = new Color[] {
                 new Color(194, 206, 221, 255),
                 new Color(194, 206, 221, 255),
                 new Color(222, 222, 222, 255),
                 new Color(250, 250, 250, 223) };
         COLOR_FILL_PRESSED_WINDOWS_STYLE = new Color(227, 226, 218, 255);
         COLOR_DISABLED_TEXT = new Color(150, 150, 150, 255);
         COLOR_FOCUS_RECT = new Color(125, 132, 136, 255);
         COLOR_TRANSPARENT = new Color(0, 0, 0, 0);
         FOCUS_RECT_INSET_X = 4;
         FOCUS_RECT_INSET_Y = 4;
         FOCUS_RECT_INSET_X_WINDOWS_STYLE = 3;
         FOCUS_RECT_INSET_Y_WINDOWS_STYLE = 3;
     }
 
     /**
      * Create a ThinkParityButtonUI.
      */
     public ThinkParityButtonUI() {
         super();
         scaledButtonBottom = new BufferedImage[4];
         scaledButtonLeft = new BufferedImage[4];
         scaledButtonMiddle = new BufferedImage[4];
         scaledButtonRight = new BufferedImage[4];
         scaledButtonTop = new BufferedImage[4];
     }
 
     /**
      * @see javax.swing.plaf.basic.BasicButtonUI#getPreferredSize(javax.swing.JComponent)
      * 
      * Ensure the width and height of the button is odd, to allow for the focus line.
      */
     @Override
     public Dimension getPreferredSize(final JComponent c) {
         final Dimension d = super.getPreferredSize(c);
         final AbstractButton button = (AbstractButton)c;
         if (d != null && button.isFocusPainted()) {
             if (d.width % 2 == 0) { d.width += 1; }
             if (d.height % 2 == 0) { d.height += 1; }
         }
         return d;
     }
 
     /**
      * @see javax.swing.plaf.basic.BasicButtonUI#paint(java.awt.Graphics, javax.swing.JComponent)
      */
     @Override
     public void paint(final Graphics g, final JComponent c) {
         final AbstractButton button = (AbstractButton)c;
         if (Boolean.TRUE.equals(c.getClientProperty(WINDOWS_BUTTON_STYLE_CLIENT_KEY))) {
             paintButtonBackgroundWindowsStyle(g, button);
         } else {
             paintButtonBackground(g, button);
         }
         super.paint(g, c);
     }
 
     /**
      * @see javax.swing.plaf.basic.BasicButtonUI#installDefaults(javax.swing.AbstractButton)
      */
     @Override
     protected void installDefaults(final AbstractButton b) {
         super.installDefaults(b);
         LookAndFeel.installProperty(b, "rolloverEnabled", Boolean.TRUE);
     }
 
     /**
      * @see javax.swing.plaf.basic.BasicButtonUI#paintFocus(java.awt.Graphics, javax.swing.AbstractButton, java.awt.Rectangle, java.awt.Rectangle, java.awt.Rectangle)
      * 
      * A dashed line is drawn when the control has focus, ie. when the user presses the button or tabs to it.
      */
     @Override
     protected void paintFocus(final Graphics g, final AbstractButton button,
             final Rectangle viewRect, final Rectangle textRect,
             final Rectangle iconRect) {
         g.setColor(COLOR_FOCUS_RECT);
         final int insetX;
         final int insetY;
         if (Boolean.TRUE.equals(button.getClientProperty(WINDOWS_BUTTON_STYLE_CLIENT_KEY))) {
             insetX = FOCUS_RECT_INSET_X_WINDOWS_STYLE;
             insetY = FOCUS_RECT_INSET_Y_WINDOWS_STYLE;
         } else {
             insetX = FOCUS_RECT_INSET_X;
             insetY = FOCUS_RECT_INSET_Y;
         }
         BasicGraphicsUtils.drawDashedRect(g, insetX, insetY,
                 button.getWidth() - 2*insetX, button.getHeight() - 2*insetY);
     }
 
     /**
      * @see javax.swing.plaf.basic.BasicButtonUI#paintText(java.awt.Graphics, javax.swing.AbstractButton, java.awt.Rectangle, java.lang.String)
      * 
      * Disabled text is drawn gray without offset or shadow.
      */
     @Override
     protected void paintText(final Graphics g, final AbstractButton button,
             final Rectangle textRect, final String text) {                    
         final FontMetrics fm = SwingUtilities2.getFontMetrics(button, g);
         final int mnemonicIndex = button.getDisplayedMnemonicIndex();
 
         if (isEnabled(button)) {
             g.setColor(button.getForeground());
         } else {
             g.setColor(COLOR_DISABLED_TEXT);
         }
         SwingUtilities2.drawStringUnderlineCharAt(button, g, text, mnemonicIndex,
                       textRect.x + getTextShiftOffset(),
                       textRect.y + fm.getAscent() + getTextShiftOffset());
     }
 
     /**
      * Fill the button background.
      * 
      * @param g
      *            The <code>Graphics</code>.
      * @param button
      *            The <code>AbstractButton</code>.
      * @param color
      *            The <code>Color</code>.
      * @param inset
      *            The inset <code>int</code>.
      */
     private void fillButtonBackground(final Graphics g, final AbstractButton button, final Color color, final int inset) {
         final Graphics2D g2 = (Graphics2D)g.create();
         try {
             g2.setPaint(color);
             g2.fillRect(inset, inset, button.getWidth()-2*inset, button.getHeight()-2*inset);
         }
         finally { g2.dispose(); }
     }
 
     /**
      * Paint the button background.
      * 
      * @param g
      *            The <code>Graphics</code>. 
      * @param button
      *            The <code>AbstractButton</code>.    
      */
     private void paintButtonBackground(final Graphics g, final AbstractButton button) {
         if ( button.isContentAreaFilled() ) {
             final int index = getBufferIndex(button);
             scaleImages(g, index, button.getWidth(), button.getHeight());
             final Graphics2D g2 = (Graphics2D)g.create();
             try {
                 // Draw the background including the shadow border
                 final int heightTop = scaledButtonTop[index].getHeight();
                 final int widthLeft = scaledButtonLeft[index].getWidth();
                 final int heightMid = scaledButtonMiddle[index].getHeight();
                 final int widthMid = scaledButtonMiddle[index].getWidth();
                 g2.drawImage(scaledButtonTop[index], 0, 0, null);
                 g2.drawImage(scaledButtonLeft[index], 0, heightTop, null);
                 g2.drawImage(scaledButtonBottom[index], 0, heightTop+heightMid, null);
                 g2.drawImage(scaledButtonRight[index], widthLeft+widthMid, heightTop, null);
                 g2.drawImage(scaledButtonMiddle[index], widthLeft, heightTop, null);
 
                 // Draw additional border lines
                 g2.setPaint(COLORS_TOP[index]);
                 g2.drawLine(widthLeft+1, heightTop-1, widthLeft+widthMid-2, heightTop-1);
                 g2.setPaint(COLORS_BOTTOM[index]);
                 g2.drawLine(widthLeft+1, heightTop+heightMid, widthLeft+widthMid-2, heightTop+heightMid);
                if (!isEnabled(button)) {
                     g2.drawLine(widthLeft, heightTop, widthLeft, heightTop+heightMid-1);
                     g2.drawLine(widthLeft+widthMid-1, heightTop, widthLeft+widthMid-1, heightTop+heightMid-1);
                 }
             }
             finally { g2.dispose(); }
         }
     }
 
     /**
      * Paint the button background in windows style.
      * 
      * @param g
      *            The <code>Graphics</code>.
      * @param button
      *            The <code>AbstractButton</code>.
      */
     private void paintButtonBackgroundWindowsStyle(final Graphics g, final AbstractButton button) {
         if ( button.isContentAreaFilled() ) {
             if (isPressed(button)) {
                 fillButtonBackground(g, button, COLOR_FILL_PRESSED_WINDOWS_STYLE, 1);
                 paintRoundedOutline(g, button, COLORS_PRESSED_WINDOWS_STYLE);
             } else if (isRollover(button)) {
                 paintRoundedOutline(g, button, COLORS_ROLLOVER_WINDOWS_STYLE);
             }
         }
     }
 
     /**
      * Paint a rounded outline.
      * 
      * @param g
      *            The <code>Graphics</code>.
      * @param button
      *            The <code>AbstractButton</code>.
      */
     private void paintRoundedOutline(final Graphics g, final AbstractButton button, final Color[] colors) {
         final Graphics2D g2 = (Graphics2D)g.create();
         try {
             final int width = button.getWidth();
             final int height = button.getHeight();
 
             // Paint outer line
             g2.setPaint(colors[0]);
             g2.drawLine(3, 0, width-4, 0);
             g2.drawLine(3, height-1, width-4, height-1);
             g2.drawLine(0, 3, 0, height-4);
             g2.drawLine(width-1, 3, width-1, height-4);
 
             // Paint rounded corners
             g2.setPaint(colors[1]);
             g2.drawLine(2, 0, 0, 2);
             g2.drawLine(width-3, 0, width-1, 2);
             g2.drawLine(2, height-1, 0, height-3);
             g2.drawLine(width-3, height-1, width-1, height-3);
             g2.setPaint(colors[2]);
             g2.drawLine(1, 0, 0, 1);
             g2.drawLine(width-2, 0, width-1, 1);
             g2.drawLine(1, height-1, 0, height-2);
             g2.drawLine(width-2, height-1, width-1, height-2);
         }
         finally { g2.dispose(); }
     }
 
     /**
      * Scale one image. Smooth interpolation is used because otherwise jaggies are visible.
      * Alpha values are preserved.
      * 
      * @param g
      *            The <code>Graphics</code>.
      * @param input
      *            The input array of <code>BufferedImage</code>.
      * @param output
      *            The output array of <code>BufferedImage</code>.
      * @param index
      *            The buffer index <code>int</code>.
      * @param width
      *            The width <code>int</code>.
      * @param height
      *            The height <code>int</code>.
      */
     private void scaleImage(final Graphics g, final BufferedImage[] input,
             final BufferedImage[] output, final int index, final int width,
             final int height) {
         if ((null == output[index]) || (output[index].getWidth() != width) || (output[index].getHeight() != height)) {
             final Image image = input[index].getScaledInstance(width, height, Image.SCALE_SMOOTH);
             output[index] = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
             final Graphics gImage = output[index].createGraphics();
             try {
                 gImage.drawImage(image, 0, 0, COLOR_TRANSPARENT, null);
             }
             finally { gImage.dispose(); }
         }
     }
 
     /**
      * Scale images.
      * 
      * @param g
      *            The <code>Graphics</code>. 
      * @param index
      *            The buffer index <code>int</code>.
      * @param width
      *            The width <code>int</code>.
      * @param height
      *            The height <code>int</code>.
      */
     private void scaleImages(final Graphics g, final int index, final int width, final int height) {
         final int heightTop = BUTTON_TOP[index].getHeight();
         final int widthLeft = BUTTON_LEFT[index].getWidth();
         final int heightBottom = BUTTON_BOTTOM[index].getHeight();
         final int widthRight = BUTTON_RIGHT[index].getWidth();
         scaleImage(g, BUTTON_BOTTOM, scaledButtonBottom, index, width, heightBottom);
         scaleImage(g, BUTTON_LEFT, scaledButtonLeft, index, widthLeft, height-heightTop-heightBottom);
         scaleImage(g, BUTTON_MIDDLE, scaledButtonMiddle, index, width-widthLeft-widthRight, height-heightTop-heightBottom);
         scaleImage(g, BUTTON_RIGHT, scaledButtonRight, index, widthRight, height-heightTop-heightBottom);
         scaleImage(g, BUTTON_TOP, scaledButtonTop, index, width, heightTop);
     }
 
     /**
      * Get the index to the image buffers.
      * 0 == normal
      * 1 == rollover
      * 2 == pressed
      * 3 == disabled
      * 
      * @param button
      *            The <code>AbstractButton</code>.   
      * @return The index <code>int</code>.
      */
     private int getBufferIndex(final AbstractButton button) {
         if (isPressed(button)) {
             return 2;
         } else if (!isEnabled(button)) {
             return 3;
         } else if (isRollover(button)) {
             return 1;
         } else {
             return 0;
         }
     }
 
     /**
      * Determine if the button is enabled.
      * 
      * @param button
      *            The <code>AbstractButton</code>.   
      * @return true if the button is enabled, false otherwise.
      */
     private Boolean isEnabled(final AbstractButton button) {
         return button.getModel().isEnabled();
     }
 
     /**
      * Determine if the button is pressed.
      * 
      * @param button
      *            The <code>AbstractButton</code>.   
      * @return true if the button is pressed, false otherwise.
      */
     private Boolean isPressed(final AbstractButton button) {
         final ButtonModel model = button.getModel();
         return (model.isArmed() && model.isPressed());
     }
 
     /**
      * Determine if the mouse is rolling over the button.
      * 
      * @param button
      *            The <code>AbstractButton</code>.   
      * @return true if the mouse is rolling over the button, false otherwise.
      */
     private Boolean isRollover(final AbstractButton button) {
         return button.getModel().isRollover();
     }
 }
