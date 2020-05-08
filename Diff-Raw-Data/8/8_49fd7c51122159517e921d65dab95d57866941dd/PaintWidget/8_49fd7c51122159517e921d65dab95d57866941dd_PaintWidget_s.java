 package org.muis.test.widget;
 
 import java.awt.Graphics2D;
 import java.awt.Point;
 import java.awt.Rectangle;
 
 import org.muis.core.MuisElement;
 import org.muis.core.event.KeyBoardEvent;
 import org.muis.core.event.MouseEvent;
 import org.muis.core.event.MuisEvent;
 
 public class PaintWidget extends org.muis.base.widget.Block
 {
 	private java.awt.image.BufferedImage theImage;
 
 	public PaintWidget()
 	{
 	}
 
 	@Override
 	protected void postInit()
 	{
 		super.postInit();
 		setFocusable(true);
 		addListener(BOUNDS_CHANGED, new org.muis.core.event.MuisEventListener<Rectangle>() {
 			@Override
 			public void eventOccurred(MuisEvent<Rectangle> event, MuisElement element)
 			{
 				resized();
 			}
 
 			@Override
 			public boolean isLocal()
 			{
 				return true;
 			}
 		});
 		addListener(MOUSE_EVENT, new org.muis.core.event.MouseListener(true) {
 			private boolean isMouseDown;
 
 			@Override
 			public void mouseDown(MouseEvent mEvt, MuisElement element)
 			{
 				if(mEvt.getButtonType() == MouseEvent.ButtonType.LEFT)
 				{
 					isMouseDown = true;
 					drawPoint(mEvt);
 				}
 			}
 
 			@Override
 			public void mouseUp(MouseEvent mEvt, MuisElement element)
 			{
 				if(mEvt.getButtonType() == MouseEvent.ButtonType.LEFT)
 				{
 					drawPoint(mEvt);
 					isMouseDown = false;
 				}
 			}
 
 			@Override
 			public void mouseMoved(MouseEvent mEvt, MuisElement element)
 			{
 				drawPoint(mEvt);
 			}
 
 			@Override
 			public void mouseEntered(MouseEvent mEvt, MuisElement element)
 			{
 				isMouseDown = getDocument().isButtonPressed(MouseEvent.ButtonType.LEFT);
 			}
 
 			@Override
 			public void mouseExited(MouseEvent mEvt, MuisElement element)
 			{
 				drawPoint(mEvt);
 			}
 
 			void drawPoint(MouseEvent mEvt)
 			{
 				if(isMouseDown)
 				{
 					Point pos = mEvt.getPosition(PaintWidget.this);
 					try
 					{
 						theImage.setRGB(pos.x, pos.y, getStyle().get(org.muis.base.style.FontStyle.color).getRGB());
 					} catch(ArrayIndexOutOfBoundsException e)
 					{
 						System.err.println("Position " + pos + " out of bounds (" + getWidth() + "x" + getHeight() + ")");
 					}
 					repaint(new Rectangle(pos.x, pos.y, 1, 1), true);
 				}
 			}
 		});
 		addListener(KEYBOARD_EVENT, new org.muis.core.event.KeyBoardListener(true) {
 			@Override
			public void keyReleased(KeyBoardEvent kEvt, MuisElement element)
 			{
 				if(kEvt.getKeyCode() == KeyBoardEvent.KeyCode.SPACE && theImage != null)
 				{
 					for(int x = 0; x < theImage.getWidth(); x++)
 						for(int y = 0; y < theImage.getHeight(); y++)
 							theImage.setRGB(x, y, 0);
 				}
 				repaint(null, true);
 			}
 		});
 	}
 
 	@Override
 	public void paintSelf(Graphics2D graphics, Rectangle area)
 	{
 		super.paintSelf(graphics, area);
 		if(theImage != null)
 			graphics.drawImage(theImage, 0, 0, theImage.getWidth(), theImage.getHeight(),
 				getStyle().get(org.muis.core.style.BackgroundStyles.color), null);
 	}
 
 	void resized()
 	{
 		if(theImage == null && getWidth() > 0 && getHeight() > 0)
 		{
 			theImage = new java.awt.image.BufferedImage(getWidth(), getHeight(), java.awt.image.BufferedImage.TYPE_4BYTE_ABGR);
 			repaint(null, false);
 		}
 		else if(theImage != null && getWidth() > theImage.getWidth() || getHeight() > theImage.getHeight())
 		{
 			int w = getWidth() > theImage.getWidth() ? getWidth() : theImage.getWidth();
 			int h = getHeight() > theImage.getHeight() ? getHeight() : theImage.getHeight();
 			java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(w, h, java.awt.image.BufferedImage.TYPE_4BYTE_ABGR);
 			img.getGraphics().drawImage(theImage, 0, 0, theImage.getWidth(), theImage.getHeight(),
 				getStyle().get(org.muis.core.style.BackgroundStyles.color), null);
 			repaint(null, false);
 		}
 	}
 }
