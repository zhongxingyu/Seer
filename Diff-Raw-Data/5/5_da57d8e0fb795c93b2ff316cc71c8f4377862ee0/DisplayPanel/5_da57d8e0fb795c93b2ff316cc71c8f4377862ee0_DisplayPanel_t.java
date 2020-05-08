 package driver;
 
 import java.awt.Graphics;
 import java.awt.Insets;
 import java.awt.event.ComponentEvent;
 import java.awt.event.ComponentListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.lang.reflect.InvocationTargetException;
 
 import javax.swing.JComponent;
 import javax.swing.SwingUtilities;
 
 import cobweb.Agent;
 import cobweb.UIInterface;
 import cobweb.UIInterface.MouseMode;
 
 /**
  * DisplayPanel is a Panel derivative useful for displaying a cobweb simulation. It uses an offscreen image to buffer
  * drawing, for flicker-free performance at the cost of memory and perhaps a little speed. Use of DisplayPanel is not
  * required in Cobweb, but it does automate display handling. Future enhancement: implement a ScrollingDisplayPanel for
  * large simulations.
  */
 public class DisplayPanel extends JComponent implements ComponentListener {
 
 	private final class MarkDonePainting implements Runnable {
 		public void run() {
 			donePainting = true;
 		}
 	}
 
 	/**
 	 * Mouse event listener for the simulation display panel
 	 *
 	 */
 	private abstract class Mouse extends MouseAdapter {
 
 		int storedX = -1;
 
 		int storedY = -1;
 
 		long storedTick = -1;
 
 		private boolean convertCoords(int x, int y, int[] out) {
 			long realTick = theUI.getCurrentTime();
 			{
 				if (!(     x >= borderLeft   && x < tileWidth  * mapWidth  + borderRight
 						&& y >= borderHeight && y < tileHeight * mapHeight + borderHeight)) {
 					return false;
 				}
 
 				int realX = (x - borderLeft) / tileWidth;
 				int realY = (y - borderHeight) / tileHeight;
 
 				// Avoid multiple clicks on one spot
 				if (storedX != realX || storedY != realY || storedTick != realTick) {
 					out[0] = realX;
 					out[1] = realY;
 				}
 
 				// Update
 				storedX = realX;
 				storedY = realY;
 				storedTick = realTick;
 
 				return true;
 			}
 		}
 
 		@Override
 		public void mouseClicked(MouseEvent e) {
 			dragMode = DragMode.Click;
 			int[] out = { 0, 0 };
 			if (convertCoords(e.getX(), e.getY(), out)) {
 				click(out[0], out[1]);
 			}
 		}
 
 
 
 		@Override
 		public void mouseReleased(MouseEvent e) {
 			dragMode = DragMode.Click;
 			super.mouseReleased(e);
 		}
 
 		@Override
 		public void mouseDragged(MouseEvent e) {
 			if (dragMode == DragMode.Click) {
 				dragMode = DragMode.DragStart;
 			}
 			int[] out = {0,0};
 			if (convertCoords(e.getX(), e.getY(), out)) {
 				dragMode = drag(out[0], out[1], dragMode);
 			}
 		}
 
 
 
 		private void click(int x, int y) {
 			if (canSetOn(x, y)) {
 				setOn(x, y);
 			} else if (canSetOff(x, y)) {
 				setOff(x, y);
 			}
 		}
 
 		private DragMode drag(int x, int y, DragMode dragmode) {
 			if (dragmode == DragMode.DragStart) {
 				if (canSetOn(x, y)) {
 					dragmode = DragMode.DragOn;
 				} else if (canSetOff(x, y)) {
 					dragmode = DragMode.DragOff;
 				}
 			} 
 
 			if (dragmode == DragMode.DragOn && canSetOn(x, y)) {
 				setOn(x, y);
 			} else if (dragmode == DragMode.DragOff && canSetOff(x, y)) {
 				setOff(x, y);
 			}
 			return dragmode;
 		}
 		abstract boolean canClick(int x, int y);
 
 		abstract boolean canSetOn(int x, int y);
 		abstract boolean canSetOff(int x, int y);
 		abstract void setOn(int x, int y);
 		abstract void setOff(int x, int y);
 	} // Mouse
 
 
 	private class ObserveMouseListener extends Mouse {
 
 		@Override
 		public boolean canClick(int x, int y) {
 			return true;
 		}
 
 		@Override
 		boolean canSetOn(int x, int y) {
 			return theUI.getAgent(x, y) != null;
 		}
 
 		@Override
 		boolean canSetOff(int x, int y) {
 			return !canSetOn(x, y);
 		}
 
 		@Override
 		void setOn(int x, int y) {
 			theUI.observe(x, y);
 		}
 
 		@Override
 		void setOff(int x, int y) {
 			theUI.unObserve();
 		}
 
 	}
 
 	private class StoneMouseListener extends Mouse {
 
 		@Override
 		public boolean canClick(int x, int y) {
 			return theUI.getAgent(x, y) != null;
 		}
 
 		@Override
 		boolean canSetOn(int x, int y) {
			return !canSetOff(x, y);
 		}
 
 		@Override
 		boolean canSetOff(int x, int y) {
			return theUI.hasStone(x, y);
 		}
 
 		@Override
 		void setOn(int x, int y) {
 			theUI.addStone(x, y);
 		}
 
 		@Override
 		void setOff(int x, int y) {
 			theUI.removeStone(x, y);
 		}
 
 	}
 
 	private class AgentMouseListener extends Mouse {
 
 		private int mytype;
 		public AgentMouseListener(int type) {
 			mytype = type;
 		}
 
 		@Override
 		public boolean canClick(int x, int y) {
 			Agent a = theUI.getAgent(x, y);
 			return (a == null && !theUI.hasStone(x, y)) || 
 			(a != null && a.type() == mytype);
 		}
 
 		@Override
 		boolean canSetOn(int x, int y) {
 			Agent a = theUI.getAgent(x, y);
 			return (a == null && !theUI.hasStone(x, y));
 		}
 
 		@Override
 		boolean canSetOff(int x, int y) {
 			Agent a = theUI.getAgent(x, y);
 			return (a != null && a.type() == mytype);
 		}
 
 		@Override
 		void setOn(int x, int y) {
 			theUI.addAgent(x, y, mytype);
 		}
 
 		@Override
 		void setOff(int x, int y) {
 			theUI.removeAgent(x, y);
 		}
 
 	}
 
 	private class FoodMouseListener extends Mouse {
 
 		int mytype;
 
 		public FoodMouseListener(int type) {
 			mytype = type;
 		}
 
 		@Override
 		public boolean canClick(int x, int y) {
 			return !theUI.hasStone(x, y);
 		}
 
 		@Override
 		boolean canSetOn(int x, int y) {
 			return !theUI.hasFood(x, y) || theUI.hasStone(x, y);
 		}
 
 		@Override
 		boolean canSetOff(int x, int y) {
 			return theUI.hasFood(x, y) && theUI.getFood(x, y) == mytype;
 		}
 
 		@Override
 		void setOn(int x, int y) {
 			theUI.addFood(x, y, mytype);
 		}
 
 		@Override
 		void setOff(int x, int y) {
 			theUI.removeFood(x, y);
 		}
 
 	}
 
 	private static final int THERMAL_MARKER_WIDTH = 16;
 
 	private Mouse myMouse;
 
 	private static final int PADDING = 10;
 
 	private int tileWidth = 0;
 
 	private int tileHeight = 0;
 
 	private int borderLeft = 0;
 
 	private int borderHeight = 0;
 
 	private int mapWidth = 0;
 
 	private int mapHeight = 0;
 
 	cobweb.UIInterface theUI;
 
 	public static final long serialVersionUID = 0x09FE6158DCF2CA3BL;
 
 	private boolean donePainting = true;
 
 	private final Runnable markReadyRefresh = new MarkDonePainting();
 
 	private int borderRight;
 
 	public DisplayPanel(UIInterface ui) {
 		theUI = ui;
 		addComponentListener(this);
 
 		setMouse(new ObserveMouseListener());
 	}
 
 	private void setMouse(Mouse m) {
 		removeMouseListener(myMouse);
 		removeMouseMotionListener(myMouse);
 
 		myMouse = m;
 
 		addMouseListener(myMouse);
 		addMouseMotionListener(myMouse);
 	}
 
 	enum DragMode {
 		Click,
 		DragStart,
 		DragOn,
 		DragOff
 	}
 
 	private DragMode dragMode = DragMode.Click;
 
 	public void componentHidden(ComponentEvent e) {
 		// nothing
 	}
 
 	public void componentMoved(ComponentEvent e) {
 		// nothing
 	}
 
 	public void componentResized(ComponentEvent e) {
 		updateScale();
 	}
 
 	public void componentShown(ComponentEvent e) {
 		// nothing
 	}
 
 	/**
 	 * Is the display grid done repainting since the last refresh(false) ?
 	 *
 	 * @return true when repaint is done
 	 */
 	public boolean isReadyToRefresh() {
 		return donePainting;
 	}
 
 	@Override
 	public void paintComponent(Graphics g) {
 		super.paintComponent(g);
 		//TODO LOW : antialias?
 		//		Graphics2D g2 = (Graphics2D) g;
 		//		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
 
 		g.translate(borderLeft, borderHeight);
 		theUI.draw(g, tileWidth, tileHeight);
 		g.translate(-borderLeft, -borderHeight);
 	}
 
 	/**
 	 * Refreshes the display grid
 	 *
 	 * @param wait wait for repaint to finish before returning?
 	 */
 	public void refresh(boolean wait) {
 		if (wait) {
 			donePainting = false;
 			repaint();
 			// Wait for displayPanel to repaint
 			if (SwingUtilities.isEventDispatchThread()) {
 				// When we are in the Swing thread, repaint() executes synchronously
 				donePainting = true;
 			} else {
 				// Otherwise we need to wait for Swing thread to finish the repaint
 				try {
 					SwingUtilities.invokeAndWait(markReadyRefresh);
 				} catch (InterruptedException ex) {
 					donePainting = true;
 				} catch (InvocationTargetException ex) {
 					donePainting = true;
 				}
 			}
 		} else if (donePainting) {
 			// Start painting a new frame without waiting
 			donePainting = false;
 			repaint();
 			SwingUtilities.invokeLater(markReadyRefresh);
 		}
 	}
 
 	public void setMouseMode(MouseMode mode) {
 		switch (mode) {
 			case AddStone:
 				setMouse(new StoneMouseListener());
 				break;
 			case Observe:
 				setMouse(new ObserveMouseListener());
 				break;
 			default:
 				break;
 		}
 	}
 
 	public void setMouseMode(MouseMode mode, int type) {
 		switch (mode) {
 			case AddAgent:
 				setMouse(new AgentMouseListener(type));
 				break;
 			case AddFood:
 				setMouse(new FoodMouseListener(type));
 				break;
 			default:
 				break;
 		}
 	}
 
 	public void setUI(UIInterface ui) {
 		theUI = ui;
 		updateScale();
 	}
 
 	void updateScale() {
 		java.awt.Dimension size = getSize();
 		if (size.width <= 0 || size.height <= 0) {
 			return;
 		}
 
 		Insets ins = getInsets();
 		size.width -= ins.left + ins.right + PADDING + THERMAL_MARKER_WIDTH;
 		size.height -= ins.top + ins.bottom + PADDING;
 
 		mapWidth = theUI.getWidth();
 		mapHeight = theUI.getHeight();
 		if (mapWidth != 0) {
 			tileWidth = size.width / mapWidth;
 		}
 		if (mapHeight != 0) {
 			tileHeight = size.height / mapHeight;
 		}
 		tileWidth = Math.min(tileWidth, tileHeight);
 		tileHeight = tileWidth;
 		int borderWidth = (size.width - tileWidth * theUI.getWidth() + PADDING) / 2;
 		borderLeft = borderWidth + THERMAL_MARKER_WIDTH;
 		borderRight = borderWidth - THERMAL_MARKER_WIDTH;
 		borderHeight = (size.height - tileHeight * theUI.getHeight() + PADDING) / 2;
 
 		this.refresh(false);
 	}
 }
