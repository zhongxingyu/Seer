 /*******************************************************************************
  * Copyright (c) 2007, 2009 compeople AG and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    compeople AG - initial API and implementation
  *******************************************************************************/
 package org.eclipse.riena.ui.ridgets;
 
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.eclipse.core.databinding.BindingException;
 
 import org.eclipse.riena.ui.common.IComplexComponent;
 
 /**
  * 
  */
 public abstract class AbstractCompositeRidget extends AbstractRidget implements IComplexRidget {
 
 	private final PropertyChangeListener propertyChangeListener;
 	private final Map<String, IRidget> ridgets;
 
 	private IComplexComponent uiControl;
 	protected boolean markedHidden; // TODO add MarkerSupport instead of boolean flag
 
 	private boolean enabled = true;
 	private String toolTip = null;
 
 	/**
 	 * Constructor
 	 */
 	public AbstractCompositeRidget() {
 		super();
 		propertyChangeListener = new PropertyChangeHandler();
 		ridgets = new HashMap<String, IRidget>();
 		markedHidden = false;
 	}
 
 	public boolean isVisible() {
 		// check for "hidden.marker". This marker overrules any other visibility rule
 		if (markedHidden) {
 			return false;
 		}
 
 		if (getUIControl() != null) {
 			// the swt control is bound
 			return isUIControlVisible();
 		}
 		// control is not bound
 		return savedVisibleState;
 	}
 
 	public void setVisible(boolean visible) {
 		if (this.markedHidden == visible) {
 			this.markedHidden = !visible;
 			updateVisible();
 		}
 	}
 
 	public boolean isEnabled() {
 		return enabled;
 	}
 
 	public void setEnabled(boolean enabled) {
 		if (this.enabled != enabled) {
 			this.enabled = enabled;
 			updateEnabled();
 		}
 	}
 
 	public IComplexComponent getUIControl() {
 		return uiControl;
 	}
 
 	public void setUIControl(Object uiControl) {
 		if (uiControl != null && !(uiControl instanceof IComplexComponent)) {
 			throw new UIBindingFailure("uiControl of a AbstractCompositeRidget must be a IComplexComponent but was a " //$NON-NLS-1$
 					+ uiControl.getClass().getSimpleName());
 		}
 		checkUIControl(uiControl);
 		unbindUIControl();
 		// save state
 		this.savedVisibleState = getUIControl() != null ? isUIControlVisible() : savedVisibleState;
 		this.uiControl = (IComplexComponent) uiControl;
 		updateVisible();
 		updateEnabled();
 		updateToolTipText();
 		bindUIControl();
 	}
 
 	/**
 	 * Performs checks on the control about to be bound by this ridget.
 	 * <p>
 	 * Implementors must make sure the given <tt>uiControl</tt> has the expected
 	 * type.
 	 * 
 	 * @param uiControl
 	 *            a {@link Widget} instance or null
 	 * @throws BindingException
 	 *             if the <tt>uiControl</tt> fails the check
 	 * @since 1.2
 	 */
 	protected void checkUIControl(Object uiControl) {
 		// implementors should overwrite
 	}
 
 	/**
 	 * Bind the current <tt>uiControl</tt> to the ridget.
 	 * <p>
 	 * Implementors must call {@link #getUIControl()} to obtain the current
 	 * control. If the control is non-null they must do whatever necessary to
 	 * bind it to the ridget.
 	 * 
 	 * @since 1.2
 	 */
 	protected void bindUIControl() {
 		// implementors should overwrite
 	}
 
 	/**
 	 * Unbind the current <tt>uiControl</tt> from the ridget.
 	 * <p>
 	 * Implementors ensure they dispose the control-to-ridget binding and
 	 * dispose any data structures that are not necessary in an unbound state.
 	 * 
 	 * @since 1.2
 	 */
 	protected void unbindUIControl() {
 		// implementors should overwrite
 	}
 
 	public void addRidget(String id, IRidget ridget) {
 		ridget.addPropertyChangeListener(propertyChangeListener);
 		ridgets.put(id, ridget);
 	}
 
 	/**
 	 * This method is provisional and may be removed without warning.
 	 * <p>
 	 * TODO [ev] make api?
 	 * 
 	 * @since 1.2
 	 */
 	public void removeRidget(String key) {
 		ridgets.remove(key);
 	}
 
 	public IRidget getRidget(String id) {
 		return ridgets.get(id);
 	}
 
 	public Collection<? extends IRidget> getRidgets() {
		return ridgets.values();
 	}
 
 	public void requestFocus() {
 		if (!getRidgets().isEmpty()) {
 			getRidgets().iterator().next().requestFocus();
 		}
 	}
 
 	public boolean hasFocus() {
 		Collection<? extends IRidget> myRidgets = getRidgets();
 		for (IRidget ridget : myRidgets) {
 			if (ridget.hasFocus()) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	public void setFocusable(boolean focusable) {
 		for (IRidget ridget : getRidgets()) {
 			ridget.setFocusable(focusable);
 		}
 	}
 
 	public boolean isFocusable() {
 		for (IRidget ridget : getRidgets()) {
 			if (ridget.isFocusable()) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	public String getToolTipText() {
 		return toolTip;
 	}
 
 	public void setToolTipText(String toolTipText) {
 		String oldValue = toolTip;
 		toolTip = toolTipText;
 		updateToolTipText();
 		firePropertyChange(IRidget.PROPERTY_TOOLTIP, oldValue, toolTip);
 	}
 
 	public void configureRidgets() {
 	}
 
 	public String getID() {
 		return null;
 	}
 
 	// protected methods
 	////////////////////
 
 	/**
 	 * Returns true if the control of this ridget is visible, false otherwise.
 	 * This default implementation always returns true and should be overriden
 	 * by subclasses.
 	 */
 	protected boolean isUIControlVisible() {
 		return true;
 	}
 
 	/**
 	 * Updates the visibility of the complex UI control (and of the UI controls
 	 * it contains). This default implementation does nothing and should be
 	 * overridden by subclasses.
 	 */
 	protected void updateVisible() {
 		// empty default implementation
 	}
 
 	/**
 	 * Updates the enabled state of the complex UI control (and of the UI
 	 * controls it contains). This default implementation does nothing and
 	 * should be overridden by subclasses.
 	 */
 	protected void updateEnabled() {
 		// empty default implementation
 	}
 
 	/**
 	 * Does nothing by default.
 	 * <p>
 	 * Subclasses should override to update the tooltip(s) of their controls in
 	 * an appropriate way.
 	 */
 	protected void updateToolTipText() {
 		// does nothing
 	}
 
 	// helping classes 
 	//////////////////
 
 	/**
 	 * Forwards a property change event fired by a (sub) ridget contained in
 	 * this composite ridget, to the listeners of the composite ridget.
 	 */
 	private class PropertyChangeHandler implements PropertyChangeListener {
 		public void propertyChange(PropertyChangeEvent evt) {
 			propertyChangeSupport.firePropertyChange(evt);
 		}
 	}
 
 }
