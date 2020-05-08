 package com.jpii.navalbattle.pavo.gui.controls;
 
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.Graphics2D;
 import java.awt.image.BufferedImage;
 import java.util.ArrayList;
 
 import com.jpii.navalbattle.pavo.PavoHelper;
 import com.jpii.navalbattle.pavo.gui.GameWindow;
 import com.jpii.navalbattle.pavo.gui.events.PMouseListener;
 import com.jpii.navalbattle.pavo.io.PavoImage;
 
 
 /**
  * Master control class. Essential for all other controls.
  * @author maximusvladimir
  *
  */
 public class Control {
 	protected BufferedImage buffer;
 	protected int width;
 	protected int height;
 	private int x;
 	private int y;
 	private Control parent;
 	private boolean isPerPieceUpdateSupported = true;
 	protected boolean lastKnownTransMode = true;
 	private boolean visible = true;
 	protected ArrayList<Control> controls;
 	private static long HANDLE_COUNTER = 0;
 	private long HANDLE = 0;
 	private boolean disposed = false;
 	private Font controlFont = new Font("Arial",0,12);
 	private Color foreColor = Color.black;
 	private Color backColor = new Color(193,172,134);
 	private boolean intermediate = false;
 	protected ArrayList<PMouseListener> pml = new ArrayList<PMouseListener>();
 	public Control(Control parent) {
 		this.parent = parent;
 		width = 100;
 		height = 100;
 		createBuffer(false);
 		controls = new ArrayList<Control>();
 		HANDLE = ++HANDLE_COUNTER;
 		//repaint();
 	}
 	
 	public void addMouseListener(PMouseListener pmls) {
 		pml.add(pmls);
 	}
 	
 	public PMouseListener getMouseListener(int index) {
 		return pml.get(index);
 	}
 	
 	public int getTotalMouseListeners() {
 		return pml.size();
 	}
 	
 	protected void bufferNeedsIntemediatePaint() {
 		intermediate = true;
 	}
 	
 	/**
 	 * NO TOUCHING!!!
 	 * @return NO TOUCHING!!!
 	 */
 	public long alo_livrezon_pa_pmt() {
 		return HANDLE;
 	}
 	
 	public Control(Control parent, int x, int y) {
 		pml = new ArrayList<PMouseListener>();
 		this.parent = parent;
 		width = 100;
 		height = 100;
 		createBuffer(false);
 		controls = new ArrayList<Control>();
 		HANDLE = ++HANDLE_COUNTER;
 		this.x = x;
 		this.y = y;
 		//repaint();
 	}
 	
 	public Control(Control parent, int x, int y, int width, int height) {
 		pml = new ArrayList<PMouseListener>();
 		this.parent = parent;
 		controls = new ArrayList<Control>();
 		HANDLE = ++HANDLE_COUNTER;
 		this.width = width;
 		this.height = height;
 		createBuffer(false);
 		this.x = x;
 		this.y = y;
 		this.width = width;
 		this.height= height;
 		//repaint();
 	}
 	
 	public void dispose() {
 		for (int c = 0; c < getTotalControls(); c++) {
 			Control cn = getControl(c);
 			if (cn != null)
 				cn.dispose();
 		}
		pml.clear();
		pml = null;
 		
 		HANDLE = 0;
 		HANDLE_COUNTER--;
 		buffer = null;
 		disposed = true;
 	}
 	
 	public void setFont(Font font) {
 		if (font == null)
 			throw new IllegalArgumentException("Font parameter is null.");
 		controlFont = font;
 		if (isForcingIndividualChanges())
 			paintUpdate();
 	}
 	
 	public Font getFont() {
 		return controlFont;
 	}
 	
 	public boolean isVisible() {
 		return visible;
 	}
 	
 	public void setVisible(boolean value) {
 		if (visible != value) {
 			visible = value;
 			paintUpdate();
 		}
 	}
 	
 	/**
 	 * Adds a control to the control.
 	 * 
 	 * Note that adding a control that has already been placed
 	 * above the current control (in other words, it would be a
 	 * parent), will most likely result in a 
 	 * <code>IllegalArgumentException</code>.
 	 * @param c The control to add to the control.
 	 */
 	public void addControl(Control c) {
 		if (c == null)
 			return;
 		
 		if (c.HANDLE == this.HANDLE)
 			throw new IllegalArgumentException("FATAL ERROR: Cannot add a control to itself!!!");
 		
 		try {
 			Control badC = searchForControl(this,c.HANDLE,5);
 			if (badC != null)
 				throw new IllegalArgumentException("FATAL ERROR: Cannot add a control to itself!!!");
 		}
 		catch (Throwable t) {
 			
 		}
 		
 		controls.add(c);
 		repaint();
 	}
 	
 	public Color getForegroundColor() {
 		return foreColor;
 	}
 	
 	public Color getBackgroundColor() {
 		return backColor;
 	}
 	
 	public void setForegroundColor(Color foreground) {
 		if (!foreground.equals(foreColor)) {
 			foreColor = foreground;
 			if (isForcingIndividualChanges())
 				paintUpdate();
 		}
 	}
 	
 	public void setBackgroundColor(Color background) {
 		if (!background.equals(foreColor)) {
 			foreColor = background;
 			if (isForcingIndividualChanges())
 				paintUpdate();
 		}
 	}
 	
 	/**
 	 * Gets the total number of controls that the current
 	 * control controls.
 	 * 
 	 * (That's a mouthful!)
 	 * @return The total number of controls.
 	 */
 	public int getTotalControls() {
 		return controls.size();
 	}
 	
 	/**
 	 * Search all controls, and all controls below it for
 	 * a control with a certain handle.
 	 * 
 	 * This method is recursive and thus may require some
 	 * time to complete.
 	 * 
 	 * May return null.
 	 * 
 	 * @param searchIn The control to search inside of.
 	 * @param handle The handle to look for.
 	 * @param maxlevels The maximum number of levels to iterate.
 	 * @return The control (if it was found.)
 	 */
 	private Control searchForControl(Control searchIn, long handle, int maxlevels) {
 		if (searchIn == null)
 			return null;
 		else {
 			if (maxlevels-- > 0) {
 				for (int c = 0; c < searchIn.getTotalControls(); c++) {
 					Control cn = searchIn.getControl(c);
 					if (cn != null && cn.HANDLE == handle)
 						return cn;
 					else if (cn != null)
 						return searchForControl(cn,handle,maxlevels);
 				}
 			}
 		}
 		return null;
 	}
 	
 	public boolean isDisposed() {
 		return disposed;
 	}
 	
 	/**
 	 * Finds a control based on its handle.
 	 * @param handle The Handle to search for.
 	 */
 	public Control getControlByHandle(long handle) {
 		for (int c = 0; c < getTotalControls(); c++) {
 			Control cn = getControl(c);
 			if (cn != null && cn.HANDLE == handle)
 				return cn;
 		}
 		return null;
 	}
 	
 	/**
 	 * Gets the control from a given index.
 	 * @param index The index to retrieve the control at.
 	 * @return The control.
 	 */
 	public Control getControl(int index) {
 		return controls.get(index);
 	}
 	
 	/**
 	 * Should a repaint occur if a basic method is
 	 * called?
 	 * 
 	 * (For example:
 	 * 
 	 * Lets persume there was this situation:
 	 * <code>
 	 * public void myAwesomeMethod() {
 	 *     control.setHeight(560);
 	 *     control.setWidth(600);
 	 *     control.setBackgroundColor(Color.red);
 	 *     control.setForegroundColor(Color.black);
 	 *     ...
 	 * }
 	 * </code>
 	 * 
 	 * If <code>isForcingIndividualChanges()</code>
 	 * is set to true, then all those methods above
 	 * will force a re-render everytime one of those
 	 * methods is called.
 	 * 
 	 * If <code>isForcingIndividualChanges()</code>
 	 * is set to false, then you <i>should</i> do the
 	 * following:
 	 * 
 	 * <code>
 	 * public void myAwesomeMethod() {
 	 *     control.setHeight(560);
 	 *     control.setWidth(600);
 	 *     control.setBackgroundColor(Color.red);
 	 *     control.setForegroundColor(Color.black);
 	 *     ...
 	 *     control.repaint();
 	 * }
 	 * </code>
 	 * To force the buffer to update.)
 	 * 
 	 * This method can have its value set using:
 	 * <code>forceIndividualChanges(boolean value)</code>.
 	 * 
 	 * @return A value indicating the stuff above.
 	 */
 	public boolean isForcingIndividualChanges() {
 		return isPerPieceUpdateSupported;
 	}
 	
 	/**
 	 * Sets a value forcing (or not) individual changes.
 	 * 
 	 * See the documentation for 
 	 * <code>isForcingIndividualChanges()</code> for more
 	 * details.
 	 * 
 	 * @param value The value forcing it or not.
 	 */
 	public void forceIndividualChanges(boolean value) {
 		isPerPieceUpdateSupported = value;
 	}
 	
 	/**
 	 * Sets the width of the control.
 	 * 
 	 * This method applies to the
 	 * <code>isForcingIndividualChanges()</code>
 	 * policy.
 	 * 
 	 * @param width The width to set the control to.
 	 */
 	public void setWidth(int width) {
 		setSize(width,height);
 	}
 	
 	/**
 	 * Sets the height of the control.
 	 * 
 	 * This method applies to the
 	 * <code>isForcingIndividualChanges()</code>
 	 * policy.
 	 * 
 	 * @param height The height to set the control to.
 	 */
 	public void setHeight(int height) {
 		setSize(width,height);
 	}
 	
 	/**
 	 * Sets the size of the control.
 	 * 
 	 * This method applies to the
 	 * <code>isForcingIndividualChanges()</code>
 	 * policy.
 	 * 
 	 * @param width The width to set the control to.
 	 * @param height The height to set the control to.
 	 */
 	public void setSize(int width, int height) {
 		boolean flag = false;
 		if (this.width != width || this.height != height)
 			flag = true;
 		this.width = width;
 		this.height = height;
 		
 		if (width == 0 || height == 0)
 			throw new IllegalArgumentException("The width and/or height CANNOT be zero.");
 		
 		if (flag) {
 			createBuffer(lastKnownTransMode);
 			paintUpdate();
 			//parentRepaint();
 		}
 	}
 	
 	/**
 	 * Gets the width of the control.
 	 * @return The width of the control.
 	 */
 	public int getWidth() {
 		return width;
 	}
 	
 	/**
 	 * Gets the height of the control.
 	 * @return The height of the control.
 	 */
 	public int getHeight() {
 		return height;
 	}
 	
 	/**
 	 * Create a temporary Graphics buffer of the
 	 * current image buffer.
 	 * 
 	 * Note: This method should not be called
 	 * consistently. To update a buffer, overload
 	 * the <code>paint(Graphics2D g)</code> method.
 	 * 
 	 * @return A Graphics object.
 	 */
 	public Graphics2D createGraphics() {
 		return PavoHelper.createGraphics(buffer);
 	}
 	
 	/**
 	 * This method will call a repaint if it is
 	 * needed.
 	 */
 	protected void paintUpdate() {
 		throwBadState();
 		if (isPerPieceUpdateSupported) {
 			repaint();
 		}
 	}
 	
 	/**
 	 * The paint method.
 	 * 
 	 * @param g The graphics object of the current buffer.
 	 */
 	protected void paint(Graphics2D g) {
 		g.setColor(Color.blue);
 		g.fillRect(0,0,getWidth(),getHeight());
 	}
 	
 	public void setLocX(int x) {
 		setLoc(x,y);
 	}
 	
 	public Control getParent() {
 		return parent;
 	}
 	
 	public void setLocY(int y) {
 		setLoc(x,y);
 	}
 	
 	public int getLocX() {
 		return x;
 	}
 	
 	public int getLocY() {
 		return y;
 	}
 	
 	public void setLoc(int x, int y) {
 		boolean flag = false;
 		if (this.x != x || this.y != y)
 			flag = true;
 		this.x = x;
 		this.y = y;
 		
 		if (flag) {
 			parentRepaint();
 		}
 	}
 	
 	/**
 	 * This method is called after the sub-controls are rendered.
 	 * @param g The graphics object.
 	 */
 	public void paintAfter(Graphics2D g) {
 		
 	}
 	
 	public BufferedImage getBuffer() {
 		return buffer;
 	}
 	
 	/**
 	 * Paints sub controls.
 	 * @param g The graphics object of the current buffer.
 	 */
 	protected void paintWinControls(Graphics2D g) {
 		for (int c = 0; c < getTotalControls(); c++) {
 			Control cn = getControl(c);
 			if (cn != null && cn.isVisible()) {
 				g.drawImage(cn.getBuffer(), cn.getLocX(), cn.getLocY(), null);
 			}
 		}
 	}
 	
 	/**
 	 * Pretty much has no functionality right now. Basically fires <code>onFocus()</code> event.
 	 */
 	public void focus() {
 		onFocus();
 	}
 	
 	/**
 	 * Called when the control regains focus.
 	 */
 	public void onFocus() {
 		
 	}
 	
 	public void onMouseHover(int x, int y) {
 		for (int c = 0; c < getTotalMouseListeners(); c++) {
 			PMouseListener p = pml.get(c);
 			if (p != null)
 				p.mouseHover(x, y);
 		}
 		for (int c = 0; c < getTotalControls(); c++) {
 			Control cn = getControl(c);
 			if (cn != null) {
 				int lx = x - cn.getLocX();
 				int ly = y - cn.getLocY();
 				if (lx >= 0 && ly >= 0 && lx < cn.getWidth() && ly < cn.getHeight())
 					cn.onMouseHover(lx,ly);
 			}
 		}
 	}
 	
 	public void onMouseDown(int x, int y, int buttonid) {
 		for (int c = 0; c < getTotalMouseListeners(); c++) {
 			PMouseListener p = pml.get(c);
 			if (p != null)
 				p.mouseDown(x, y, buttonid);
 		}
 		for (int c = 0; c < getTotalControls(); c++) {
 			Control cn = getControl(c);
 			if (cn != null) {
 				int lx = x - cn.getLocX();
 				int ly = y - cn.getLocY();
 				if (lx >= 0 && ly >= 0 && lx < cn.getWidth() && ly < cn.getHeight()) {
 					cn.onMouseDown(lx,ly,buttonid);
 					cn.focus();
 				}
 			}
 		}
 	}
 	
 	public void onMouseDrag(int x, int y) {
 		for (int c = 0; c < getTotalMouseListeners(); c++) {
 			PMouseListener p = pml.get(c);
 			if (p != null)
 				p.mouseDrag(x, y);
 		}
 		for (int c = 0; c < getTotalControls(); c++) {
 			Control cn = getControl(c);
 			if (cn != null) {
 				int lx = x - cn.getLocX();
 				int ly = y - cn.getLocY();
 				if (lx >= 0 && ly >= 0 && lx < cn.getWidth() && ly < cn.getHeight())
 					cn.onMouseDrag(lx,ly);
 			}
 		}
 	}
 	
 	public void onMouseUp(int x, int y, int buttonid) {
 		for (int c = 0; c < getTotalMouseListeners(); c++) {
 			PMouseListener p = pml.get(c);
 			if (p != null)
 				p.mouseUp(x, y, buttonid);
 		}
 		for (int c = 0; c < getTotalControls(); c++) {
 			Control cn = getControl(c);
 			if (cn != null) {
 				int lx = x - cn.getLocX();
 				int ly = y - cn.getLocY();
 				if (lx >= 0 && ly >= 0 && lx < cn.getWidth() && ly < cn.getHeight())
 					cn.onMouseUp(lx,ly,buttonid);
 			}
 		}
 	}
 	
 	private void throwBadState() {
 		if (disposed)
 			throw new IllegalStateException("The specified control is disposed. It can no longer be used, however the majority of its" +
 					" properties can most likely be retrieved. (The majority of its properties can most likely not be set either.)");
 		if (buffer == null)
 			throw new IllegalStateException("Buffer has entered null state.");
 	}
 	
 	/**
 	 * Forces the control to repaint.
 	 */
 	public void repaint() {
 		if (intermediate)
 			intermediate = false;
 		throwBadState();
 		Graphics2D g = createGraphics();
 		paint(g);
 		paintWinControls(g);
 		paintAfter(g);
 		g.dispose();
 		
 		parentRepaint();
 		
 		if (intermediate)
 			repaint();
 	}
 	
 	public void parentRepaint() {
 		if (parent != null && !parent.isDisposed()) {
 			parent.repaint();
 		}
 	}
 	
 	protected void createBuffer(boolean transparencyEnabled) {
 		lastKnownTransMode = transparencyEnabled;
 		if (buffer != null)
 			buffer.flush();
 		if (transparencyEnabled) {
 			buffer = new BufferedImage(getWidth(),getHeight(),BufferedImage.TYPE_4BYTE_ABGR);
 		}
 		else {
 			buffer = new BufferedImage(getWidth(),getHeight(),BufferedImage.TYPE_3BYTE_BGR);
 		}
 	}
 }
