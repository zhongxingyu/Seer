 /*******************************************************************************
  * Copyright (c) 2007, 2008 compeople AG and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    compeople AG - initial API and implementation
  *******************************************************************************/
 package org.eclipse.riena.internal.ui.ridgets.swt;
 
 import org.eclipse.riena.core.util.ListenerList;
 import org.eclipse.riena.ui.ridgets.AbstractRidget;
 import org.eclipse.riena.ui.ridgets.IWindowRidget;
 import org.eclipse.riena.ui.ridgets.UIBindingFailure;
 import org.eclipse.riena.ui.ridgets.listener.IWindowRidgetListener;
 import org.eclipse.riena.ui.ridgets.uibinding.IBindingPropertyLocator;
 import org.eclipse.riena.ui.swt.utils.ImageUtil;
 import org.eclipse.riena.ui.swt.utils.SWTBindingPropertyLocator;

import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.swt.events.ShellEvent;
 import org.eclipse.swt.events.ShellListener;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Shell;
 
 /**
  * The ridget for a Shell control.
  */
 public class ShellRidget extends AbstractRidget implements IWindowRidget {
 
 	private static Image missingImage;
 	private Shell shell;
 	private String toolTip = null;
 	private boolean blocked;
 	private boolean closeable;
 	private boolean active;
 	private String title;
 	private String icon;
 	private ListenerList<IWindowRidgetListener> windowRidgetListeners;
 	private ShellListener shellListener;
 
 	public ShellRidget() {
 
 		super();
 
 		title = ""; //$NON-NLS-1$
 		closeable = true;
 		active = true;
 		windowRidgetListeners = new ListenerList<IWindowRidgetListener>(IWindowRidgetListener.class);
 		shellListener = new RidgetShellListener();
 	}
 
 	public ShellRidget(Shell shell) {
 		this();
 		setUIControl(shell);
 	}
 
 	/*
 	 * @see org.eclipse.riena.ui.ridgets.IRidget#getUIControl()
 	 */
 	public Shell getUIControl() {
 		return shell;
 	}
 
 	/*
 	 * @see org.eclipse.riena.ui.ridgets.IRidget#setUIControl(java.lang.Object)
 	 */
 	public void setUIControl(Object uiControl) {
 		if (uiControl != null && !(uiControl instanceof Shell)) {
 			throw new UIBindingFailure("uiControl of a ShellRidget must be a Shell but was a " //$NON-NLS-1$
 					+ uiControl.getClass().getSimpleName());
 		}
 
 		removeShellListener();
 		shell = (Shell) uiControl;
 		addShellListener();
 		updateToolTip();
 	}
 
 	private void addShellListener() {
 		if (getUIControl() != null) {
 			getUIControl().addShellListener(shellListener);
 		}
 	}
 
 	private void removeShellListener() {
 		if (getUIControl() != null) {
 			getUIControl().removeShellListener(shellListener);
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.riena.ui.ridgets.IWindowRidget#addWindowRidgetListener(org
 	 * .eclipse.riena.ui.ridgets.listener.IWindowRidgetListener)
 	 */
 	public void addWindowRidgetListener(IWindowRidgetListener listener) {
 		windowRidgetListeners.add(listener);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.riena.ui.ridgets.IWindowRidget#removeWindowRidgetListener
 	 * (org.eclipse.riena.ui.ridgets.listener.IWindowRidgetListener)
 	 */
 	public void removeWindowRidgetListener(IWindowRidgetListener listener) {
 		windowRidgetListeners.remove(listener);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.riena.ui.ridgets.IRidget#isVisible()
 	 */
 	public boolean isVisible() {
 		return getUIControl().isVisible();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.riena.ui.ridgets.IRidget#setVisible(boolean)
 	 */
 	public void setVisible(boolean visible) {
 		getUIControl().setVisible(visible);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.riena.ui.ridgets.IWindowRidget#dispose()
 	 */
 	public void dispose() {
 		getUIControl().dispose();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.riena.ui.ridgets.IWindowRidget#setTitle(java.lang.String)
 	 */
 	public void setTitle(String title) {
		if (title != null && !this.title.equals(title)) {
 			this.title = title;
 			updateTitle();
 		}
 	}
 
 	private void updateTitle() {
 		if (getUIControl() != null) {
 			getUIControl().setText(title);
 		}
 	}
 
 	public String getTitle() {
 		return title;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.riena.ui.ridgets.IWindowRidget#setIcon(java.lang.String)
 	 */
 	public void setIcon(String icon) {
 		String oldIcon = this.icon;
 		this.icon = icon;
 		if (hasChanged(oldIcon, icon)) {
 			updateIcon();
 		}
 	}
 
 	/*
 	 * Updates the icon of the UI control.
 	 */
 	private void updateIcon() {
 		Shell control = getUIControl();
 		if (control != null) {
 			Image image = null;
 			if (icon != null) {
 				image = getManagedImage(icon);
 			}
 			control.setImage(image);
 		}
 	}
 
 	protected Image getManagedImage(String key) {
 		Image image = ImageUtil.getImage(key);
 		if (image == null) {
 			image = getMissingImage();
 		}
 		return image;
 	}
 
 	public final synchronized Image getMissingImage() {
 		if (missingImage == null) {
 			missingImage = ImageDescriptor.getMissingImageDescriptor().createImage();
 		}
 		return missingImage;
 	}
 
 	/*
 	 * Compares the two given values.
 	 * 
 	 * @param oldValue - old value
 	 * 
 	 * @param newValue - new value
 	 * 
 	 * @return true, if value has changed; otherwise false
 	 */
 	protected boolean hasChanged(Object oldValue, Object newValue) {
 		if (oldValue == null && newValue == null) {
 			return false;
 		}
 		return (oldValue == null && newValue != null) || (oldValue != null && newValue == null)
 				|| !oldValue.equals(newValue);
 	}
 
 	/*
 	 * @see org.eclipse.riena.ui.ridgets.IWindowRidget#setDefaultButton(JButton)
 	 */
 	public void setDefaultButton(Object defaultButton) {
 		if (defaultButton instanceof Button) {
 			getUIControl().setDefaultButton((Button) defaultButton);
 		}
 	}
 
 	/*
 	 * @see org.eclipse.riena.ui.ridgets.IWindowRidget#getDefaultButton()
 	 */
 	public Object getDefaultButton() {
 		return getUIControl().getDefaultButton();
 	}
 
 	/*
 	 * @see org.eclipse.riena.ui.ridgets.IRidget#requestFocus()
 	 */
 	public void requestFocus() {
 		if (getUIControl() != null) {
 			getUIControl().setFocus();
 		}
 	}
 
 	/*
 	 * @see org.eclipse.riena.ui.ridgets.IRidget#hasFocus()
 	 */
 	public boolean hasFocus() {
 		if (getUIControl() != null) {
 			return getUIControl().isFocusControl();
 		}
 		return false;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.riena.ui.ridgets.IRidget#isFocusable()
 	 */
 	public boolean isFocusable() {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.riena.ui.ridgets.IRidget#setFocusable(boolean)
 	 */
 	public void setFocusable(boolean focusable) {
 		// TODO Auto-generated method stub
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.riena.ui.ridgets.IRidget#getToolTipText()
 	 */
 	public String getToolTipText() {
 		return toolTip;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.riena.ui.ridgets.IRidget#setToolTipText(java.lang.String)
 	 */
 	public void setToolTipText(String toolTipText) {
 		toolTip = toolTipText;
 	}
 
 	private void updateToolTip() {
 		if (getUIControl() != null) {
 			getUIControl().setToolTipText(toolTip);
 		}
 	}
 
 	/*
 	 * @see org.eclipse.riena.ui.ridgets.IRidget#isBlocked()
 	 */
 	public boolean isBlocked() {
 		return blocked;
 	}
 
 	/*
 	 * @see org.eclipse.riena.ui.ridgets.IRidget#setBlocked(boolean)
 	 */
 	public void setBlocked(boolean blocked) {
 		if (this.blocked != blocked) {
 			this.blocked = blocked;
 		}
 	}
 
 	/*
 	 * @see org.eclipse.riena.ui.ridgets.IWindowRidget#setActive(boolean)
 	 */
 	public void setActive(boolean active) {
 		if (this.active != active) {
 			this.active = active;
 			updateActive();
 		}
 	}
 
 	/*
 	 * @see org.eclipse.riena.ui.ridgets.IWindowRidget#setCloseable(boolean)
 	 */
 	public void setCloseable(boolean closeable) {
 		if (this.closeable != closeable) {
 			this.closeable = closeable;
 			updateCloseable();
 		}
 	}
 
 	private void updateCloseable() {
 		// TODO
 	}
 
 	private void updateActive() {
 		if (getUIControl() != null) {
 			getUIControl().setEnabled(active);
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.riena.ui.ridgets.AbstractRidget#updateFromModel()
 	 */
 	@Override
 	public void updateFromModel() {
 
 		super.updateFromModel();
 
 		if (getUIControl() != null) {
 			updateTitle();
 			updateIcon();
 			updateActive();
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.riena.ui.ridgets.IRidget#getID()
 	 */
 	public String getID() {
 		if (getUIControl() != null) {
 			IBindingPropertyLocator locator = SWTBindingPropertyLocator.getInstance();
 			return locator.locateBindingProperty(getUIControl());
 		}
 
 		return null;
 	}
 
 	private class RidgetShellListener implements ShellListener {
 
 		/*
 		 * (non-Javadoc)
 		 * 
 		 * @see
 		 * org.eclipse.swt.events.ShellListener#shellActivated(org.eclipse.swt
 		 * .events.ShellEvent)
 		 */
 		public void shellActivated(ShellEvent e) {
 			for (IWindowRidgetListener l : windowRidgetListeners.getListeners()) {
 				l.activated();
 			}
 		}
 
 		/*
 		 * (non-Javadoc)
 		 * 
 		 * @see
 		 * org.eclipse.swt.events.ShellListener#shellClosed(org.eclipse.swt.
 		 * events.ShellEvent)
 		 */
 		public void shellClosed(ShellEvent e) {
 			for (IWindowRidgetListener l : windowRidgetListeners.getListeners()) {
 				l.closed();
 			}
 		}
 
 		/*
 		 * (non-Javadoc)
 		 * 
 		 * @see
 		 * org.eclipse.swt.events.ShellListener#shellDeactivated(org.eclipse
 		 * .swt.events.ShellEvent)
 		 */
 		public void shellDeactivated(ShellEvent e) {
 			// do nothing yet
 		}
 
 		/*
 		 * (non-Javadoc)
 		 * 
 		 * @see
 		 * org.eclipse.swt.events.ShellListener#shellDeiconified(org.eclipse
 		 * .swt.events.ShellEvent)
 		 */
 		public void shellDeiconified(ShellEvent e) {
 			// do nothing yet
 		}
 
 		/*
 		 * (non-Javadoc)
 		 * 
 		 * @see
 		 * org.eclipse.swt.events.ShellListener#shellIconified(org.eclipse.swt
 		 * .events.ShellEvent)
 		 */
 		public void shellIconified(ShellEvent e) {
 			// do nothing yet
 		}
 	}
 }
