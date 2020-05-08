 import javax.microedition.lcdui.Canvas;
 import javax.microedition.lcdui.Font;
 import javax.microedition.lcdui.Image;
 import javax.microedition.lcdui.Graphics;
 
 class LogView extends UIElementCommon {
 
 	/**
 	 * Chat internal bitmat that we draw on.
 	 */
 	Image chatImage = null;
 
 	/**
 	 * The font that will be used to draw messages.
 	 */
 	protected Font logFont;
 
 	/**
 	 * Ctor.
 	 */
 	public LogView(Parent parent) {
 		super(parent);
 
 		logFont = Font.getFont(
 				Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_SMALL
 				);
 
 		chatImage = Image.createImage(
 			parent.getXSize(),
			parent.getYSize() - parent.getYDivOn()
 			);
 	}
 
 	/**
 	 * Append string to log.
 	 *
 	 * FIX: binary search would be soo much better...
 	 */
 	public synchronized void appendString(String str) {
 		if (str.length() == 0) {
 			return;
 		}
 		for (int l = str.length(); l > 0; --l) {
			if (logFont.substringWidth(str, 0, l) < parent.getXSize()) {
 				drawString(str.substring(0, l));
 				appendString(str.substring(l, str.length()));
 				return;
 			}
 		}
 		drawString("??? ASSERT ERROR !!!");
 	}
 
 	/**
 	 * Draw one line of text from str.
 	 *
 	 * str should not exceed chat width or will not fit.
 	 */
 	protected void drawString(String str) {
 		Graphics g = chatImage.getGraphics();
 
 		g.copyArea(
 			0, logFont.getHeight(),
 			chatImage.getWidth(), chatImage.getHeight() - logFont.getHeight(),
 			0, 0,
 			Graphics.TOP | Graphics.LEFT
 			);
 		g.setColor(0x0f0f0f0);
 		g.fillRect(
 			0, chatImage.getHeight() - logFont.getHeight(),
 			chatImage.getWidth(), chatImage.getHeight()
 			);
 		g.setColor(0x0000040);
		g.setFont(logFont);
 		g.drawString(
 			str,
 			0, chatImage.getHeight(), Graphics.BOTTOM | Graphics.LEFT
 			);
 		markDirty();
 	}
 
 	protected void repaint(Graphics g) {
 		g.setClip(0, parent.getYDiv(), parent.getXSize(), parent.getYSize());
 		g.drawImage(chatImage, 0, parent.getYDiv(), Graphics.TOP|Graphics.LEFT);
 	}
 }
