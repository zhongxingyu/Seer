 /*******************************************************************************
  * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
  * This program and the accompanying materials are made available under the terms
  * of the Eclipse Public License v1.0 which accompanies this distribution, and is
  * available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * Wind River Systems - initial API and implementation
  *******************************************************************************/
 package org.eclipse.tcf.te.tcf.launch.ui.editor;
 
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.core.runtime.IAdaptable;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.debug.core.DebugPlugin;
 import org.eclipse.debug.core.ILaunchConfiguration;
 import org.eclipse.debug.core.ILaunchConfigurationListener;
 import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
 import org.eclipse.tcf.te.launch.ui.editor.AbstractLaunchTabContainerEditorPage;
 import org.eclipse.tcf.te.runtime.concurrent.util.ExecutorsUtil;
 import org.eclipse.tcf.te.runtime.persistence.PersistenceManager;
 import org.eclipse.tcf.te.runtime.persistence.interfaces.IPersistenceDelegate;
 import org.eclipse.tcf.te.runtime.services.ServiceManager;
 import org.eclipse.tcf.te.runtime.services.interfaces.IPropertiesAccessService;
 import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerModel;
 
 /**
  * TCF launch configuration tab container page implementation.
  */
 public abstract class AbstractTcfLaunchTabContainerEditorPage extends AbstractLaunchTabContainerEditorPage implements ILaunchConfigurationListener {
 
 	protected ILaunchConfigurationListener launchConfigListener = null;
 
 	protected static final String PROP_LAUNCH_CONFIG_WC = "launchConfigWorkingCopy.transient.silent"; //$NON-NLS-1$
 	protected static final String PROP_ORIGINAL_LAUNCH_CONFIG_ATTRIBUTES = "launchConfigAttributes.transient.silent"; //$NON-NLS-1$
 
 	/**
 	 * Get the peer model from the editor input.
 	 * @param input The editor input.
 	 * @return The peer model.
 	 */
 	public IPeerModel getPeerModel(Object input) {
 		return (IPeerModel)((IAdaptable)input).getAdapter(IPeerModel.class);
 	}
 
 	/**
 	 * Get the launch configuration from the peer model.
 	 * @param peerModel The peer model.
 	 * @return The launch configuration.
 	 */
 	public ILaunchConfigurationWorkingCopy getLaunchConfig(final IPeerModel peerModel) {
 		ILaunchConfigurationWorkingCopy wc = null;
 		if (peerModel != null) {
 			IPropertiesAccessService service = ServiceManager.getInstance().getService(peerModel, IPropertiesAccessService.class);
 			Assert.isNotNull(service);
 			if (service.getProperty(peerModel, PROP_LAUNCH_CONFIG_WC) instanceof ILaunchConfigurationWorkingCopy) {
 				wc = (ILaunchConfigurationWorkingCopy)service.getProperty(peerModel, PROP_LAUNCH_CONFIG_WC);
 			}
 			else {
 				wc = (ILaunchConfigurationWorkingCopy)Platform.getAdapterManager().getAdapter(peerModel, ILaunchConfigurationWorkingCopy.class);
 				if (wc == null) {
 					wc = (ILaunchConfigurationWorkingCopy)Platform.getAdapterManager().loadAdapter(peerModel, "org.eclipse.debug.core.ILaunchConfigurationWorkingCopy"); //$NON-NLS-1$
 				}
 				service.setProperty(peerModel, PROP_LAUNCH_CONFIG_WC, wc);
 				IPersistenceDelegate delegate = PersistenceManager.getInstance().getDelegate(wc, String.class);
 				String launchConfigAttributes = null;
 				try {
					launchConfigAttributes = (String)delegate.write(wc, String.class);
 				}
 				catch (Exception e) {
 				}
 				service.setProperty(peerModel, PROP_ORIGINAL_LAUNCH_CONFIG_ATTRIBUTES, launchConfigAttributes);
 			}
 		}
 		return wc;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.launch.ui.editor.AbstractLaunchTabContainerEditorPage#setupData(java.lang.Object)
 	 */
 	@Override
 	public boolean setupData(Object input) {
 		ILaunchConfigurationWorkingCopy wc = getLaunchConfig(getPeerModel(input));
 		if (wc != null) {
 			getLaunchConfigurationTab().initializeFrom(wc);
 			checkLaunchConfigDirty();
 			return true;
 		}
 		return false;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.launch.ui.editor.AbstractLaunchTabContainerEditorPage#extractData()
 	 */
 	@Override
 	public boolean extractData() {
 		ILaunchConfigurationWorkingCopy wc = getLaunchConfig(getPeerModel(getEditorInput()));
 		if (wc != null && checkLaunchConfigDirty()) {
 			getLaunchConfigurationTab().performApply(wc);
 			try {
 				wc.doSave();
 				IPeerModel peerModel = getPeerModel(getEditorInput());
 				IPropertiesAccessService service = ServiceManager.getInstance().getService(peerModel, IPropertiesAccessService.class);
 				Assert.isNotNull(service);
 				service.setProperty(peerModel, PROP_LAUNCH_CONFIG_WC, null);
 				checkLaunchConfigDirty();
 				return true;
 			}
 			catch (Exception e) {
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * Check if the launch configuration has changed.
 	 * If it has changed, the page is set dirty.
 	 * @return <code>true</code> if the launch configuration has changed since last save.
 	 */
 	public boolean checkLaunchConfigDirty() {
 		boolean dirty = false;
 		IPeerModel peerModel = getPeerModel(getEditorInput());
 		IPropertiesAccessService service = ServiceManager.getInstance().getService(peerModel, IPropertiesAccessService.class);
 		String oldLaunchConfigAttributes = (String)service.getProperty(peerModel, PROP_ORIGINAL_LAUNCH_CONFIG_ATTRIBUTES);
 		IPersistenceDelegate delegate = PersistenceManager.getInstance().getDelegate(getLaunchConfig(peerModel), String.class);
 		String launchConfigAttributes = null;
 		try {
 			launchConfigAttributes = (String)delegate.write(getLaunchConfig(peerModel), String.class);
 			dirty = !launchConfigAttributes.equals(oldLaunchConfigAttributes);
 		}
 		catch (Exception e) {
 		}
 		setDirty(dirty);
 		return dirty;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.launch.ui.editor.AbstractLaunchTabContainerEditorPage#setDirty(boolean)
 	 */
 	@Override
 	public void setDirty(boolean dirty) {
 		super.setDirty(dirty);
 		ExecutorsUtil.executeInUI(new Runnable() {
 			@Override
 			public void run() {
 				getManagedForm().dirtyStateChanged();
 			}
 		});
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.launch.ui.editor.AbstractLaunchTabContainerEditorPage#setActive(boolean)
 	 */
 	@Override
 	public void setActive(boolean active) {
 		super.setActive(active);
 		if (active && launchConfigListener == null) {
 			launchConfigListener = this;
 			DebugPlugin.getDefault().getLaunchManager().addLaunchConfigurationListener(this);
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.launch.ui.editor.AbstractLaunchTabContainerEditorPage#dispose()
 	 */
 	@Override
 	public void dispose() {
 		super.dispose();
 		IPeerModel peerModel = getPeerModel(getEditorInput());
 		IPropertiesAccessService service = ServiceManager.getInstance().getService(peerModel, IPropertiesAccessService.class);
 		service.setProperty(peerModel, PROP_ORIGINAL_LAUNCH_CONFIG_ATTRIBUTES, null);
 		service.setProperty(peerModel, PROP_LAUNCH_CONFIG_WC, null);
 		DebugPlugin.getDefault().getLaunchManager().removeLaunchConfigurationListener(this);
 		launchConfigListener = null;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.debug.core.ILaunchConfigurationListener#launchConfigurationAdded(org.eclipse.debug.core.ILaunchConfiguration)
 	 */
 	@Override
 	public void launchConfigurationAdded(ILaunchConfiguration configuration) {
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.debug.core.ILaunchConfigurationListener#launchConfigurationRemoved(org.eclipse.debug.core.ILaunchConfiguration)
 	 */
 	@Override
 	public void launchConfigurationRemoved(ILaunchConfiguration configuration) {
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.debug.core.ILaunchConfigurationListener#launchConfigurationChanged(org.eclipse.debug.core.ILaunchConfiguration)
 	 */
 	@Override
 	public void launchConfigurationChanged(ILaunchConfiguration configuration) {
 		if (!(configuration instanceof ILaunchConfigurationWorkingCopy)) {
 			IPeerModel peerModel = getPeerModel(getEditorInput());
 			IPropertiesAccessService service = ServiceManager.getInstance().getService(peerModel, IPropertiesAccessService.class);
 			ILaunchConfigurationWorkingCopy wc = (ILaunchConfigurationWorkingCopy)service.getProperty(peerModel, PROP_LAUNCH_CONFIG_WC);
 			if (wc != null && configuration.getName().equals(wc.getName())) {
 				service.setProperty(peerModel, PROP_ORIGINAL_LAUNCH_CONFIG_ATTRIBUTES, null);
 				service.setProperty(peerModel, PROP_LAUNCH_CONFIG_WC, null);
 				ExecutorsUtil.executeInUI(new Runnable() {
 					@Override
 					public void run() {
 						setActive(isActive());
 					}
 				});
 			}
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.ui.views.editor.pages.AbstractEditorPage#doValidate()
 	 */
 	@Override
 	protected ValidationResult doValidate() {
 	    return null;
 	}
 }
