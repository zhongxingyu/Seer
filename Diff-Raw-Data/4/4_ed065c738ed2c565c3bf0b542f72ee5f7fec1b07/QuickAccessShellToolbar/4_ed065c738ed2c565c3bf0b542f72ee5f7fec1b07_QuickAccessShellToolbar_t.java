 /*******************************************************************************
  * Copyright (c) Emil Crumhorn and others - Hexapixel.com - emil.crumhorn@gmail.com
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    emil.crumhorn@gmail.com  - initial API and implementation
  *    eclipse-dev@volanakis.de - push button support for QuickAccessToolbar,
  *                               auto-redraw on enablement change
  *******************************************************************************/ 
 
 package com.hexapixel.widgets.ribbon;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.swt.events.MouseEvent;
 import org.eclipse.swt.graphics.Rectangle;
 
 public class QuickAccessShellToolbar extends RibbonToolbar {
 
 	private RibbonShell mRibbonShell;
 	private List<RibbonButton> mButtons;
 	private List<RibbonButton> mSelectedButtons;
 	private Rectangle mBounds;
 	private RibbonButton mHoverButton;
 	private RibbonTooltip mArrowTooltip;
 	
 	public QuickAccessShellToolbar(RibbonShell parent) {
 		super(parent);
 		mRibbonShell = parent;
 		mButtons = new ArrayList<RibbonButton>();
 		mSelectedButtons = new ArrayList<RibbonButton>();
 	}
 	
 	public RibbonShell getRibbonShell() {
 		return mRibbonShell;
 	}
 	
 	public List<RibbonButton> getButtons() {
 		return mButtons;
 	}
 	
 	void addButton(RibbonButton button) {
 		if (!mButtons.contains(button)) {
 			mButtons.add(button);
 			updateBounds();
 		}		
 	}
 	
 	public void removeButton(RibbonButton button) {
 		mButtons.remove(button);
 		mRibbonShell.redrawContents();
 		updateBounds();
 	}
 	
 	public void setArrowTooltip(RibbonTooltip tooltip) {
 		mArrowTooltip = tooltip;
 	}
 	
 	public RibbonTooltip getArrowTooltip() {
 		return mArrowTooltip;
 	}
 	
 	void updateBounds() {
 		if (mButtons.size() == 0)
 			return;
 		
 		int x = 0;
 		int y = 0;
 		int width = 0;
 		int height = 0;
 		
 		for (int i = 0; i < mButtons.size(); i++) {
 			RibbonButton button = mButtons.get(i);
 			Rectangle bounds = button.getBounds();			
 			if (bounds == null)
 				continue;			
 			
 			y = Math.min(y, bounds.y);
 			x = Math.min(x, bounds.x);
 			width = Math.max(width, bounds.x+bounds.width);
 			height = Math.max(height, bounds.y+bounds.height);
 		}
 		
 		// so that we can de-hover when mouse exits an area, we want this slightly higher and wider than any strip of buttons, just a few pixels
 		y -= 3;
 		x -= 3;
 		width += 6;
 		height += 6;
 		mBounds = new Rectangle(x, y, width, height);
 	}
 	
 	public Rectangle getBounds() {
 		return mBounds;
 	}
 	
 	boolean dehover() {
 		if (mHoverButton != null) {
 			mHoverButton.setHoverButton(false);
 			mHoverButton = null;
 			return true;
 		}
 		
 		return false;
 	}
 	
 	boolean mouseMove(MouseEvent me) {
 		boolean redraw = false;
 		boolean any = false;
 		
 		for (int i = 0; i < mButtons.size(); i++) {
 			RibbonButton button = mButtons.get(i);
 			if (!button.isEnabled())
 				continue;
 			
 			if (isInside(me.x, me.y, button.getBounds())) {
 				any = true;				
 
 				if (!button.isHoverButton()) {
 					if (mHoverButton != null && mHoverButton != button) {
 						mHoverButton.setHoverButton(false);
 						redraw = true;
 					}
 					button.setHoverButton(true);
 					mHoverButton = button;
 					redraw = true;
 				}
 
 				// if split button, determine what part is hover
 				if (button.isSplit() && !button.isToolbarButton()) {
 					if (isInside(me.x, me.y, button.getTopBounds())) {
 						if (!button.isTopHovered()) {
 							redraw = true;
 						}
 						button.setTopHovered(true);
 					}
 
 					if (isInside(me.x, me.y, button.getBottomBounds())) {
 						if (!button.isBottomHovered()) {
 							redraw = true;
     					}
 						button.setBottomHovered(true);
 					}
 				}
 				else if (button.isToolbarButton()) {
 					boolean changed = false;
 					
 					// if split item, determine what part is hover
 					if ((button.getStyle() & RibbonButton.STYLE_ARROW_DOWN_SPLIT) != 0) {        								
 						if (isInside(me.x, me.y, button.getLeftBounds())) {
 							if (!button.isLeftHovered()) {
 								changed = true;
 							}
 							button.setLeftHovered(true);
 						}
 						else if (isInside(me.x, me.y, button.getRightBounds())) {
 							if (!button.isRightHovered()) {
 								changed = true;
 							}
 							button.setRightHovered(true);
 						}
 					
 						if (!changed)
 							continue;
 						else
 							redraw = true;
 					}		    	
 				}		
 			}
 		}
 		
 		if (!any) {
 			if (mHoverButton != null)
 				mHoverButton.setHoverButton(false);
 			
 			mHoverButton = null;
 			redraw = true;
 		}
 		
 		return redraw;
 	}
 	
 	boolean mouseDown(MouseEvent me) {
 		boolean redraw = false;
 		
 		for (int i = 0; i < mButtons.size(); i++) {
 			RibbonButton button = mButtons.get(i);
 			if (!button.isEnabled())
 				continue;
 			
 			if (isInside(me.x, me.y, button.getBounds())) {
 				if (!button.isSelected()) {
 					redraw = true;
 					if (!mSelectedButtons.contains(button))
 						mSelectedButtons.add(button);
 					
 					button.setSelected(true);
 					
 					// if split item, determine what part is hover
 					if ((button.getStyle() & RibbonButton.STYLE_ARROW_DOWN_SPLIT) != 0) {
 						if (isInside(me.x, me.y, button.getLeftBounds())) {
 							// if it's a toggle, untoggle, but only left / top side
 							if ((button.getStyle() & RibbonButton.STYLE_TOGGLE) != 0) {
 								if (button.isLeftSelected()) 
 									button.setLeftSelected(false);
 								else
 									button.setLeftSelected(true);
 							}
 						}
 						
 						if (isInside(me.x, me.y, button.getRightBounds())) {
 							button.setRightSelected(true);
 						}
 					}
 					
 					if (button.isSelected())
 						button.notifySelectionListeners(me);
 				}
 			}
 		}
 		
 		return redraw;
 	}
 	
 	boolean mouseUp(MouseEvent me) {
 		boolean redraw = false;
 		
 		for (int i = 0; i < mButtons.size(); i++) {
 			RibbonButton button = mButtons.get(i);
 			if (!button.isEnabled())
 				continue;
 			
 			if (isInside(me.x, me.y, button.getBounds())) {
 				if (button.isSelected()) {
 					redraw = true;
 					button.setSelected(false);
 					mSelectedButtons.remove(button);
 					
 					// if split button, determine what part is hover
 //					if ((button.getStyle() & RibbonButton.STYLE_ARROW_DOWN_SPLIT) != 0) {
 //						if (isInside(me.x, me.y, button.getLeftBounds())) 
 //							button.setLeftSelected(true);
 //						
 //						if (isInside(me.x, me.y, button.getRightBounds())) 
 //							button.setRightSelected(true);
 //					}
 				}
 			}
 		}
 		
 		return redraw;
 	}
 	
 	private boolean isInside(int x, int y, Rectangle rect) {
 		if (rect == null) {
 			return false;
 		}
 
 		return x >= rect.x && y >= rect.y && x <= (rect.x + rect.width-1) && y <= (rect.y + rect.height-1);
 	}
 	
 	void redraw() {
 		RibbonTabFolder tabFolder = mRibbonShell.getRibbonTabFolder();
		if (!tabFolder.isDisposed()) {
			tabFolder.redraw(mBounds.x, mBounds.y, mBounds.width, mBounds.height, false);
		}
 	}
 
 }
