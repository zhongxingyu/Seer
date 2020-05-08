 /*-
  * Copyright © 2009 Diamond Light Source Ltd.
  *
  * This file is part of GDA.
  *
  * GDA is free software: you can redistribute it and/or modify it under the
  * terms of the GNU General Public License version 3 as published by the Free
  * Software Foundation.
  *
  * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
  * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  *
  * You should have received a copy of the GNU General Public License along
  * with GDA. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package uk.ac.gda.common.rcp.util;
 
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.Set;
 import java.util.Stack;
 
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 
 /**
  * Class to deal with setting items in a grid layout invisible. Allows batch updating of controls to reduce flicker
  */
 public class GridUtils {
 
 	private static int depthCount = 0;
 	
 	/** FIXME - static collections with widgets in are a bad idea, causes memory leaks **/
 	private static Set<Control> controlsToLayout = new HashSet<Control>();
 	
 	/** FIXME - static collections with widgets in are a bad idea, causes memory leaks **/
 	private static Stack<Control> controlsToRedraw = new Stack<Control>();
 
 	/**
 	 * Start a batch layout update. Wrap multiple calls to setVisibleAndLayout or layout with start/end MultLayout.
 	 * <p>
 	 * Calls to startMultiLayout can be nested as a stack is used internally. The last call to endMultiLayout causes all
 	 * the layouts to happen at once.
 	 * </p>
 	 * 
 	 * @param parent
 	 *            a suitable control that encompasses all the controls that may have their visibility changed. parent is
 	 *            optional and can be null. If parent is "wrong" or null the effect is simply to have slightly more
 	 *            flicker in the UI that optimal
 	 */
 	public static void startMultiLayout(Control parent) {
 		if (parent != null)
 			parent.setRedraw(false);
 		controlsToRedraw.push(parent);
 		depthCount++;
 	}
 
 	/**
 	 * End a batch layout update. If the stack is empty, the all the controls that have had layout updated will now be
 	 * laid out and redrawn.
 	 */
 	public static void endMultiLayout() {
 		depthCount--;
 		if (depthCount == 0 && controlsToLayout.size() > 0) {
 			Control[] controls = controlsToLayout.toArray(new Control[controlsToLayout.size()]);
 			controls[0].getShell().layout(controls);
 			for (Control control : controls) {
 				control.setRedraw(true);
 			}
 			controlsToLayout.clear();
 		}
 		Control parent = controlsToRedraw.pop();
 		if (parent != null)
 			parent.setRedraw(true);
 	}
 
 	/**
 	 * Changes visibility and layout of a control. Takes into consideration excluding the control from the GridData
 	 * layout manager.
 	 * <p>
 	 * If this function is called within the scope of startMultiLayout, the final steps of layout will not take effect
 	 * until endMultiLayout is called.
 	 * </p>
 	 * 
 	 * @param widget
 	 *            the widget to make visible or invisible
 	 * @param isVisible
 	 *            is true to make widget visible, false to hide it
 	 */
 	public static void setVisibleAndLayout(final Control widget, final boolean isVisible) {
 		if (widget == null) return;
 		if (!(widget.getLayoutData() instanceof GridData)) {
 			throw new IllegalArgumentException("Widget must have GridData layout data applied");
 		}
 		final GridData data = (GridData) widget.getLayoutData();
 		if (data.exclude != !isVisible || widget.getVisible() != isVisible) {
 			data.exclude = !isVisible;
 
 			if (depthCount == 0) {
 				// perform update immediately
 				widget.setVisible(isVisible);
 				try {
					//changed from widget.getShell() to widget.getParent() as the former
					//led to views not laying out correctly when opened in running workbench despite working
					//ok if view is opened during workbench initialisation
					widget.getParent().layout(new Control[] { widget });
 				} catch (Exception ignored) {
 					// If we cannot layout parent then not a problem.
 				}
 			} else {
 				// defer update until endMultiUpdate is called
 
 				// don't turn off redraw multiple times for the same
 				// widget because we only turn it back on once.
 				// This is important since setRedraw is stacked
 				if (!controlsToLayout.contains(widget)) {
 					widget.setRedraw(false);
 				}
 				widget.setVisible(isVisible);
 				controlsToLayout.add(widget);
 			}
 		}
 	}
 
 	/**
 	 * Simplified version of setVisibleAndLayout(...) which cannot cause
 	 * a memory leak. Does not work with startMultiLayout() and endMultiLayout()
 	 * 
 	 * You need to call layout once on the parent widget after using this method. For instance:
 	 * 
 	 * 	    
 	        GridUtils.setVisible(wLabel, wVisible);
 			GridUtils.setVisible(w,      wVisible);
 			GridUtils.setVisible(kLabel, kVisible);
 			GridUtils.setVisible(kStart, kVisible);
 			getShell().layout();
 	 * 
 	 * @param widget
 	 * @param isVisible
 	 */
 	public static void setVisible(final Control widget, final boolean isVisible) {
 		
 		if (widget == null) return;
 		if (widget.getLayoutData() instanceof GridData) {
 			final GridData data = (GridData) widget.getLayoutData();
 			data.exclude = !isVisible;
 		}
 		widget.setVisible(isVisible);
 	}
 	
 	/**
 	 * Calls layout on the control, deferring the call if within a start/endMultiLayout call. Use this version of layout
 	 * only when you are sure that the bounding box has not changed, otherwise, use layoutFull
 	 * 
 	 * @param control
 	 *            is the widget to re-layout
 	 */
 	public static void layout(Composite control) {
 		if (depthCount == 0) {
 			control.layout();
 		} else {
 			controlsToLayout.add(control);
 			controlsToLayout.addAll(Arrays.asList(control.getChildren()));
 		}
 	}
 
 	/**
 	 * Calls layout on the control, deferring the call if within a start/endMultiLayout call. As opposed to layout(),
 	 * this forces a full layout.
 	 * 
 	 * @param control
 	 *            is the widget to re-layout
 	 */
 	public static void layoutFull(Composite control) {
 		if (depthCount == 0) {
 			try {
 				control.setRedraw(false);
 				control.getShell().layout(new Control[] { control });
 				control.layout();
 			} finally {
 				control.setRedraw(true);
 			}
 		} else {
 			controlsToLayout.add(control);
 			controlsToLayout.addAll(Arrays.asList(control.getChildren()));
 		}
 
 	}
 
 	/**
 	 * 
 	 * @param area
 	 */
 	public static void removeMargins(Composite area) {
 		final GridLayout layout = (GridLayout)area.getLayout();
 		layout.horizontalSpacing=0;
 		layout.verticalSpacing  =0;
 		layout.marginBottom     =0;
 		layout.marginTop        =0;
 		layout.marginLeft       =0;
 		layout.marginRight      =0;
 		layout.marginHeight     =0;
 		layout.marginWidth      =0;
 
 	}
 }
