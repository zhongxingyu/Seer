 /* The MIT License
  * 
  * Copyright (c) 2005 David Rice, Trevor Croft
  * 
  * Permission is hereby granted, free of charge, to any person 
  * obtaining a copy of this software and associated documentation files 
  * (the "Software"), to deal in the Software without restriction, 
  * including without limitation the rights to use, copy, modify, merge, 
  * publish, distribute, sublicense, and/or sell copies of the Software, 
  * and to permit persons to whom the Software is furnished to do so, 
  * subject to the following conditions:
  * 
  * The above copyright notice and this permission notice shall be 
  * included in all copies or substantial portions of the Software.
  * 
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, 
  * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF 
  * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND 
  * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS 
  * BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN 
  * ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN 
  * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE 
  * SOFTWARE.
  */
 package net.rptools.maptool.client;
 
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.image.BufferedImage;
 import java.io.IOException;
 
 import javax.swing.JComponent;
 
 import net.rptools.clientserver.ActivityListener;
 import static net.rptools.clientserver.ActivityListener.Direction.Inbound;
 import static net.rptools.clientserver.ActivityListener.Direction.Outbound;
 import static net.rptools.clientserver.ActivityListener.State.Complete;
 import static net.rptools.clientserver.ActivityListener.State.Start;
 import net.rptools.maptool.client.swing.Animatable;
 import net.rptools.maptool.client.swing.AnimationManager;
 import net.rptools.maptool.util.ImageUtil;
 
 /**
  * @author trevor
  */
 public class ActivityMonitorPanel extends JComponent implements ActivityListener, Animatable {
 
 	private static final int PADDING = 5;
 	private static final int ON_DELAY = 100;
 	
 	private boolean receiving;
 	private boolean transmitting;
 
 	private boolean receiveComplete;
 	private boolean transmitComplete;
 	
 	private static BufferedImage transmitOn;
 	private static BufferedImage transmitOff;
 	
 	private static BufferedImage receiveOn;
 	private static BufferedImage receiveOff;
 
 	private static long receiveStart;
 	private static long transmitStart;
 	
 	private static Dimension prefSize;
 
 	static {
 		
 		try {
 			transmitOn  = ImageUtil.getImage("net/rptools/maptool/client/image/transmitOn.png");
 			transmitOff = ImageUtil.getImage("net/rptools/maptool/client/image/activityOff.png");
 	
 			receiveOn  = ImageUtil.getImage("net/rptools/maptool/client/image/receiveOn.png");
 			receiveOff = ImageUtil.getImage("net/rptools/maptool/client/image/activityOff.png");
 			
 			int width = Math.max(transmitOn.getWidth(), transmitOff.getWidth()) + Math.max(receiveOn.getWidth(), receiveOff.getWidth());
 			int height = Math.max(transmitOn.getHeight(), transmitOff.getHeight()) + Math.max(receiveOn.getHeight(), receiveOff.getHeight());
 			
 			prefSize = new Dimension(width + (PADDING * 2) + 2, height);
 		} catch (IOException ioe) {
 			// TODO: handle this better
 			ioe.printStackTrace();
 		}
 		
 	}
 	
 	/* (non-Javadoc)
 	 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
 	 */
 	protected void paintComponent(Graphics g) {
 		
 		BufferedImage receiveImg = receiving ? receiveOn : receiveOff;
 		BufferedImage transmitImg = transmitting ? transmitOn : transmitOff;
 		
 		g.drawImage(receiveImg, PADDING, (getSize().height - receiveImg.getHeight())/2, this);
 		g.drawImage(transmitImg, getSize().width - transmitImg.getWidth() - PADDING, (getSize().height - transmitImg.getHeight())/2, this);
 	}
 	
 	/* (non-Javadoc)
 	 * @see javax.swing.JComponent#getPreferredSize()
 	 */
 	public Dimension getPreferredSize() {
 		return getMinimumSize();
 	}
 	
 	/* (non-Javadoc)
 	 * @see javax.swing.JComponent#getMinimumSize()
 	 */
 	public Dimension getMinimumSize() {
 		return prefSize;
 	}
 	
 	////
 	// ANIMATABLE
 	
 	public void animate() {
 		
 		long now = System.currentTimeMillis();
 		boolean turnOff = false;
 		
 		if (transmitting && transmitComplete && now > transmitStart + ON_DELAY) {
 			transmitting = false;
 			turnOff = true;
 		}
 
 		if (receiving && receiveComplete && now > receiveStart + ON_DELAY) {
 			turnOff = true;
 			receiving = false;
 		}
 		
 		if (!transmitting && !receiving && turnOff) {
 			AnimationManager.removeAnimatable(this);
 			repaint();
 		}
 	}
 	
 	//// 
 	// ACTIVITY LISTENER
 	
 	/* (non-Javadoc)
 	 * @see net.rptools.clientserver.ActivityListener#notify(net.rptools.clientserver.ActivityListener.Direction, net.rptools.clientserver.ActivityListener.State, int, int)
 	 */
 	public void notify(Direction direction, State state, int total, int current) {
 
 		switch (direction) {
 			case Inbound: {
 				
 				switch(state) {
 				case Start: {
 					receiving = true; 
 					receiveComplete = false;
 					receiveStart = System.currentTimeMillis();
 
 					AnimationManager.addAnimatable(this);
 					
 					repaint();
 					break;
 				}
 				case Complete: {
 					receiveComplete = true;
 					
 					if (System.currentTimeMillis() > receiveStart + ON_DELAY) {
 						receiving = false;
 					}
 					repaint();
 					break;
 				}
 				default: return;
 				}
				
				break;
 			}
 			
 			case Outbound: {
 				
 				switch(state) {
 				case Start: {
 					transmitting = true;
 					transmitComplete = false;
 					transmitStart = System.currentTimeMillis();
 
 					AnimationManager.addAnimatable(this);
 
 					repaint();
 					break;
 				}
 				case Complete: {
 					transmitComplete = true;
 					
 					if (System.currentTimeMillis() > transmitStart + ON_DELAY) {
 						transmitting = false;
 					}
 					repaint();
 					break;
 				}
 				default: return;
 				}
				
				break;
 			}
 		}
 		
 	}
 }
