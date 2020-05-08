 /*******************************************************************************
  * Copyright (c) Emil Crumhorn and others - Hexapixel.com - emil.crumhorn@gmail.com
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    emil.crumhorn@gmail.com  - initial API and implementation
  *    eclipse-dev@volanakis.de - added STYLE_PUSH, auto-redraw on enablement change,
  *                               support for dispose listeners, lazy init for selection
  *                               listeners
  *******************************************************************************/ 
 
 package com.hexapixel.widgets.ribbon;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.DisposeEvent;
 import org.eclipse.swt.events.DisposeListener;
 import org.eclipse.swt.events.MenuEvent;
 import org.eclipse.swt.events.MenuListener;
 import org.eclipse.swt.events.MouseEvent;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.graphics.Rectangle;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Menu;
 
 public abstract class AbstractRibbonGroupItem implements MenuListener, IDisposable {
 
 	// BITVECTORS!
 	public static final int STYLE_NONE = SWT.NONE;
 	public static final int STYLE_ARROW_DOWN = 1 << 0;
 	// TODO: Activate, later
 	public static final int STYLE_ARROW_DOWN_SPLIT = 1 << 1;
 	/**
 	 * An element with this style does <b>not</b> show a 'pushed down' state on click.
 	 */
 	public static final int STYLE_NO_DEPRESS = 1 << 2;
 	public static final int STYLE_TOGGLE = 1 << 3;
 	public static final int STYLE_TWO_LINE_TEXT = 1 << 4;
 	/**
 	 * An element with this style will show a 'pushed down' state on click.
 	 */
 	public static final int STYLE_PUSH = 1 << 5;
 		
 	private int mXLocation;
 	private int mXWidth;
 	
 	private boolean mHover;
 	private boolean mSelected;
 	private boolean mEnabled = true; // default is true
 	
 	private boolean mTopHover;
 	private boolean mBottomHover;
 	private boolean mTopSelected;
 	private boolean mBottomSelected;
 	private boolean mLeftHover;
 	private boolean mRightHover;
 	private boolean mLeftSelected;
 	private boolean mRightSelected;
 	
 	private Rectangle mBounds;
 	private int mImageVerticalAlignment = SWT.TOP;
 	
 	private int mAccelerator;
 	
 	private String mName;
 	private Image mImage;
 	private Image mDisabledImage;
 	private int mStyle;
 	private RibbonTooltip mTooltip;
 	private RibbonTooltip mTooltipBottomOrRight;
 	
 	private ButtonSelectGroup mButtonSelectGroup;
 	private RibbonGroup mParent;
 	private QuickAccessShellToolbar mParentQAST;
 	
 	private boolean mToolbarButton;	
 	public static final int TOOLBAR_SIDE_LEFT = 1;
 	public static final int TOOLBAR_SIDE_NOT_LEFT_OR_RIGHT = 2;
 	public static final int TOOLBAR_SIDE_RIGHT = 3;
 	public static final int TOOLBAR_SIDE_LEFT_AND_RIGHT = 4;
 	private int mToolbarSide = TOOLBAR_SIDE_NOT_LEFT_OR_RIGHT;
 	
 	private List<SelectionListener> mSelectionListeners;
 	private List<IDisposeListener>   mDisposeListeners; 
 	
 	private Menu mMenu;
 	
 	public AbstractRibbonGroupItem(RibbonGroup parent, String name, Image image, int style) {
 		//if (style == SWT.NONE)
 			//style = RibbonButton.STYLE_NO_DEPRESS;
 		
 		mParent = parent;
 		mName = name;
 		mImage = image;
 		mStyle = style;
 		init();
 	}
 
 	public AbstractRibbonGroupItem(RibbonGroup parent, String name, Image image, Image disabledImage, int style) {
 		mParent = parent;
 		mName = name;
 		mImage = image;
 		mDisabledImage = disabledImage;
 		mStyle = style;
 		init();
 	}
 
 	public AbstractRibbonGroupItem(RibbonGroup parent, String name, Image image, Image disabledImage, RibbonTooltip toolTip, int style) {
 		mParent = parent;
 		mName = name;
 		mImage = image;
 		mDisabledImage = disabledImage;
 		mStyle = style;
 		mTooltip = toolTip;
 		init();
 	}
 	
 	public AbstractRibbonGroupItem(QuickAccessShellToolbar parent, Image image, Image disabledImage, RibbonTooltip toolTip, int style) {
 		mParentQAST = parent;
 		mName = null;
 		mImage = image;
 		mDisabledImage = disabledImage;
 		mStyle = style;
 		mTooltip = toolTip;
 		init();
 	}
 
 	private void init() {
 		if (mParent != null) {
 			mMenu = new Menu(mParent.getParent());
 			mMenu.addMenuListener(this);
 		}
 	}
 	
 	void createMenu(RibbonShell rs) {
 		mMenu = new Menu(rs.getShell());
 		mMenu.addMenuListener(this);
 	}
 	
 	void createMenu(Control control) {
 		mMenu = new Menu(control);
 		mMenu.addMenuListener(this);
 	}
 	
 	public void menuHidden(MenuEvent e) {
 		setBottomSelected(false);
 	}
 
 	public void menuShown(MenuEvent e) {
 	}
 
 	public void showMenu() {
 		if (mMenu.getItemCount() == 0)
 			return;
 		
 		mMenu.setLocation(mParent.toDisplay(mBounds.x, mBounds.height+mBounds.y));
 		mMenu.setVisible(true);
 	}
 	
 	public RibbonGroup getParent() {
 		return mParent;
 	}
 
 	public void setButtonSelectGroup(ButtonSelectGroup group) {
 		mButtonSelectGroup = group;
 		group.add(this);
 	}
 	
 	public ButtonSelectGroup getButtonSelectGroup() { 
 		return mButtonSelectGroup;
 	}
 	
 	public void removeButtonSelectGroup(ButtonSelectGroup group) {
 		group.remove(this);
 	}
 
 	public void setToolTip(RibbonTooltip toolTip) {
 		mTooltip = toolTip;
 	}
 	
 	public RibbonTooltip getToolTip() {
 		return mTooltip;
 	}
 	
 	public void setTopOrLeftToolTip(RibbonTooltip toolTip) {
 		mTooltip = toolTip;
 	}
 	
 	public RibbonTooltip getTopOrLeftToolTip() {
 		return mTooltip;
 	}
 
 	public void setBottomOrRightToolTip(RibbonTooltip toolTip) {
 		mTooltipBottomOrRight = toolTip;
 	}
 	
 	public RibbonTooltip getBottomOrRightToolTip() {
 		return mTooltipBottomOrRight;
 	}
 
 	public boolean isHoverButton() {
 		return mHover;
 	}
 		
 	public void setHoverButton(boolean hover) {
 		mHover = hover;
 		if (!hover) {
 			mTopHover = false;
 			mBottomHover = false;
 			mLeftHover = false;
 			mRightHover = false;
 		}
 	}
 	
 	public void setSelected(boolean selected) {
 		mSelected = selected;
 		if (!selected) {
 			mTopSelected = false;
 			mBottomSelected = false;
 			mLeftSelected = false;
 			mRightSelected = false;
 		}
 	}
 	
 	public boolean isChecked() {
 		return isSelected();
 	}
 	
 	public void setChecked(boolean checked) {
 		setSelected(checked);
 	}
 	
 	public boolean isToolbarButton() {
 		return mToolbarButton;
 	}
 	
 	protected void setToolbarButton(boolean toolbarButton) {
 		mToolbarButton = toolbarButton;
 	}
 	
 	protected void setToolbarSide(int side) {
 		mToolbarSide = side;
 	}
 	
 	protected int getToolbarSide() {
 		return mToolbarSide;
 	}
 	
 	public boolean isSelected() {
 		return mSelected;
 	}
 	
 	public void setTopHovered(boolean hovered) {
 		mTopHover = hovered;
 		mBottomHover = false;
 		mLeftHover = false;
 		mRightHover = false;
 	}
 	
 	public boolean isTopHovered() {
 		return mTopHover;
 	}
 	
 	public void setBottomHovered(boolean hovered) {
 		mBottomHover = hovered;
 		mTopHover = false;
 		mLeftHover = false;
 		mRightHover = false;
 	}
 	
 	public boolean isBottomHovered() {
 		return mBottomHover;
 	}
 	
 	public void setTopSelected(boolean selected) {
 		mTopSelected = selected;
 		mSelected = selected;
 		mBottomSelected = false;
 		mLeftSelected = false;
 		mRightSelected = false;
 		mBottomHover = false;
 		mLeftHover = false;
 		mRightHover = false;
 	}
 	
 	public boolean isTopSelected() {
 		return mTopSelected;
 	}
 	
 	public void setBottomSelected(boolean selected) {
 		mBottomSelected = selected;
 		mSelected = selected;
 		mTopSelected = false;
 		mLeftSelected = false;
 		mRightSelected = false;
 		mTopHover = false;
 		mLeftHover = false;
 		mRightHover = false;
 	}
 	
 	public boolean isBottomSelected() {
 		return mBottomSelected;
 	}
 	
 	// --
 	
 	public void setLeftHovered(boolean hovered) {
 		mLeftHover = hovered;
 		mRightHover = false;
 	}
 	
 	public boolean isLeftHovered() {
 		return mLeftHover;
 	}
 	
 	public void setRightHovered(boolean hovered) {
 		mRightHover = hovered;
 		mLeftHover = false;
 		mTopHover = false;
 		mBottomHover = false;
 	}
 	
 	public boolean isRightHovered() {
 		return mRightHover;
 	}
 	
 	public void setLeftSelected(boolean selected) {
 		mLeftSelected = selected;
 		mSelected = selected;
 		mRightSelected = false;
 		mTopSelected = false;
 		mBottomSelected = false;
 	}
 	
 	public boolean isLeftSelected() {
 		return mLeftSelected;
 	}
 	
 	public void setRightSelected(boolean selected) {
 		mRightSelected = selected;
 		mSelected = selected;
 		mLeftSelected = false;
 		mTopSelected = false;
 		mBottomSelected = false;
 	}
 	
 	public boolean isRightSelected() {
 		return mRightSelected;
 	}
 	
 	public int getX() {
 		return mBounds.x;
 	}
 		
 	public int getWidth() {
 		return mBounds.width;
 	}		
 
 	public Rectangle getBounds() {
 		return mBounds;
 	}
 	
 	public void setBounds(Rectangle bounds) {
 		mBounds = bounds;
 	}
 	
 	public Rectangle getTopBounds() {
 		return new Rectangle(mBounds.x, mBounds.y, mBounds.width, 38); 
 	}
 	
 	public Rectangle getBottomBounds() {
 		return new Rectangle(mBounds.x, mBounds.y+35, mBounds.width, mBounds.height-35);
 	}
 	
 	public Rectangle getLeftBounds() {
 		return new Rectangle(mBounds.x, mBounds.y, mBounds.width-12, mBounds.height);
 	}
 	
 	public Rectangle getRightBounds() {
 		return new Rectangle(mBounds.x+mBounds.width-11, mBounds.y, 11, mBounds.height);
 	}
 	
 	public int getImageVerticalAlignment() {
 		return mImageVerticalAlignment;
 	}
 	
 	public void setImageVerticalAlignment(int alignment) {
 		mImageVerticalAlignment = alignment;
 	}
 
 	public void setAccelerator(int keycode) {
 		mAccelerator = keycode;
 	}
 	
 	public int getAccelerator() {
 		return mAccelerator;
 	}
 	
 	public String getName() {
 		return mName;
 	}
 	
 	public Image getImage() {
 		return mImage;
 	}
 
 	public void setImage(Image image) {
 		//ImageCache.ensureImageIsCached(image);
 		mImage = image;
 	}
 
 	public Image getDisabledImage() {
 		return mDisabledImage;
 	}
 
 	public void setDisabledImage(Image image) {
 		//ImageCache.ensureImageIsCached(image);
 		mDisabledImage = image;
 	}
 
 	public void setName(String name) {
 		mName = name;
 	}
 	
 	public void setEnabled(boolean enabled) {
 		if (mEnabled != enabled) {
 			mEnabled = enabled;
 			redraw();
 		}
 	}
 	
 	public boolean isEnabled() {
 		return mEnabled;
 	}
 	
 	public int getStyle() {
 		return mStyle;
 	}
 	
 	public void setStyle(int style) {
 		mStyle = style;
 	}
 	
 	public void addSelectionListener(SelectionListener listener) {
 		if (mSelectionListeners == null)
 			mSelectionListeners = new ArrayList<SelectionListener>();
 		if (!mSelectionListeners.contains(listener))
 			mSelectionListeners.add(listener);
 	}
 	
 	public void removeSelectionListener(SelectionListener listener) {
 		if(mSelectionListeners != null)
 			mSelectionListeners.remove(listener);
 	}
 	
 	protected void notifySelectionListeners(MouseEvent me) {
 		if (mSelectionListeners == null)
 			return;
 		
 		Event e = new Event();
 		e.button = me.button;
 		e.data = this;
 		e.display = me.display;
 		e.stateMask = me.stateMask;
 		e.widget = me.widget;
 		e.x = me.x;
 		e.y = me.y;
 		SelectionEvent se = new SelectionEvent(e);
 		for (SelectionListener listener : mSelectionListeners) 
 			listener.widgetSelected(se);
 	}
 	
 	public void addDisposeListener(IDisposeListener listener) {
 		if (mDisposeListeners == null)
 			mDisposeListeners = new ArrayList<IDisposeListener>();
 		if (!mDisposeListeners.contains(listener))
 			mDisposeListeners.add(listener);
 	}
 	
 	public void removeDisposeListener(IDisposeListener listener) {
 		if (mDisposeListeners != null)
 			mDisposeListeners.remove(listener);
 	}
 	
 	public void dispose() {
 		notifyDisposeListeners();
 		mDisposeListeners = null;
 	}
 	
 	private void notifyDisposeListeners() {
 		if (mDisposeListeners == null) 
 			return;
 		for (IDisposeListener listener : mDisposeListeners) 
 			listener.itemDisposed(this);
 	}
 	
 	public boolean isSplit() {
 		return (mStyle & STYLE_ARROW_DOWN_SPLIT) != 0;
 	}
 	
 	public boolean isArrow() {
 		return (mStyle & STYLE_ARROW_DOWN) != 0;
 	}
 	
 	public boolean isTwoLineText() {
 		return (mStyle & RibbonButton.STYLE_TWO_LINE_TEXT) != 0;
 	}
 	
 /*	public void setMenu(Menu menu) {
 		mMenu = menu;
 	}
 */	
 	public Menu getMenu() {
 		return mMenu;
 	}
 	
 	public void redraw() {
		if(mParent != null && !mParent.isDisposed()) {
 			mParent.redraw(mBounds.x-4, mBounds.y, mBounds.width+8, mBounds.height, false);
		} 
		else if (mParentQAST != null) {
 			mParentQAST.redraw();
 		}
 	}
 	
 	public String toString() {
 		return getClass().getName() + " - " + getName() + ". loc; [bounds:" + mBounds + "] parent: " + mParent;
 	}
 	
 }
