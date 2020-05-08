 /*******************************************************************************
  * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
  * This program and the accompanying materials are made available under the terms
  * of the Eclipse Public License v1.0 which accompanies this distribution, and is
  * available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * Wind River Systems - initial API and implementation
  *******************************************************************************/
 
 package org.eclipse.tcf.te.tcf.launch.core.lm.delegates;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.eclipse.debug.core.ILaunchConfiguration;
 import org.eclipse.debug.core.ILaunchConfigurationListener;
 import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
 import org.eclipse.tcf.core.AbstractPeer;
 import org.eclipse.tcf.core.TransientPeer;
 import org.eclipse.tcf.protocol.IPeer;
 import org.eclipse.tcf.protocol.Protocol;
 import org.eclipse.tcf.te.launch.core.lm.delegates.DefaultLaunchManagerDelegate;
 import org.eclipse.tcf.te.launch.core.lm.interfaces.ICommonLaunchAttributes;
 import org.eclipse.tcf.te.launch.core.lm.interfaces.ILaunchContextLaunchAttributes;
 import org.eclipse.tcf.te.launch.core.lm.interfaces.ILaunchSpecification;
 import org.eclipse.tcf.te.launch.core.persistence.launchcontext.LaunchContextsPersistenceDelegate;
 import org.eclipse.tcf.te.launch.core.selection.interfaces.IRemoteSelectionContext;
 import org.eclipse.tcf.te.launch.core.selection.interfaces.ISelectionContext;
 import org.eclipse.tcf.te.runtime.model.interfaces.IModelNode;
 import org.eclipse.tcf.te.runtime.persistence.PersistenceManager;
 import org.eclipse.tcf.te.runtime.persistence.interfaces.IPersistenceDelegate;
 import org.eclipse.tcf.te.tcf.core.peers.Peer;
 import org.eclipse.tcf.te.tcf.launch.core.interfaces.IAttachLaunchAttributes;
 import org.eclipse.tcf.te.tcf.launch.core.interfaces.IPeerModelProperties;
 import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerModel;
 import org.eclipse.tcf.te.tcf.locator.interfaces.services.ILocatorModelUpdateService;
 import org.eclipse.tcf.te.tcf.locator.model.Model;
 import org.eclipse.tcf.te.tcf.locator.nodes.PeerRedirector;
 
 /**
  * RemoteAppLaunchManagerDelegate
  */
 public class AttachLaunchManagerDelegate extends DefaultLaunchManagerDelegate implements ILaunchConfigurationListener {
 
 	// mandatory attributes for attach launch configurations
 	private static final String[] MANDATORY_CONFIG_ATTRIBUTES = new String[] {
 		ILaunchContextLaunchAttributes.ATTR_LAUNCH_CONTEXTS
 	};
 
 	/**
 	 * Constructor.
 	 */
 	public AttachLaunchManagerDelegate() {
 		super();
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.launch.core.lm.delegates.DefaultLaunchManagerDelegate#updateLaunchConfigAttributes(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy, org.eclipse.tcf.te.launch.core.lm.interfaces.ILaunchSpecification)
 	 */
 	@Override
 	public void updateLaunchConfigAttributes(ILaunchConfigurationWorkingCopy wc, ILaunchSpecification launchSpec) {
 		super.updateLaunchConfigAttributes(wc, launchSpec);
 
 		wc.setAttribute(IAttachLaunchAttributes.ATTR_ATTACH_SERVICES, (List<?>)null);
 		copySpecToConfig(launchSpec, wc);
 
 		wc.rename(getDefaultLaunchName(wc));
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.launch.core.lm.delegates.DefaultLaunchManagerDelegate#initLaunchConfigAttributes(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy, org.eclipse.tcf.te.launch.core.lm.interfaces.ILaunchSpecification)
 	 */
 	@Override
 	public void initLaunchConfigAttributes(ILaunchConfigurationWorkingCopy wc, ILaunchSpecification launchSpec) {
 		super.initLaunchConfigAttributes(wc, launchSpec);
 
 		wc.setAttribute(IAttachLaunchAttributes.ATTR_ATTACH_SERVICES, (List<?>)null);
 		copySpecToConfig(launchSpec, wc);
 
 		wc.rename(getDefaultLaunchName(wc));
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.launch.core.lm.delegates.DefaultLaunchManagerDelegate#updateLaunchConfig(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy, org.eclipse.tcf.te.launch.core.selection.interfaces.ISelectionContext, boolean)
 	 */
 	@Override
 	public void updateLaunchConfig(ILaunchConfigurationWorkingCopy wc, ISelectionContext selContext, boolean replace) {
 		super.updateLaunchConfig(wc, selContext, replace);
 
 		if (selContext instanceof IRemoteSelectionContext) {
 			IRemoteSelectionContext remoteCtx = (IRemoteSelectionContext)selContext;
 			LaunchContextsPersistenceDelegate.setLaunchContexts(wc, new IModelNode[]{remoteCtx.getRemoteCtx()});
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.launch.core.lm.delegates.DefaultLaunchManagerDelegate#addLaunchSpecAttributes(org.eclipse.tcf.te.launch.core.lm.interfaces.ILaunchSpecification, java.lang.String, org.eclipse.tcf.te.launch.core.selection.interfaces.ISelectionContext)
 	 */
 	@Override
 	protected ILaunchSpecification addLaunchSpecAttributes(ILaunchSpecification launchSpec, String launchConfigTypeId, ISelectionContext selectionContext) {
 		launchSpec = super.addLaunchSpecAttributes(launchSpec, launchConfigTypeId, selectionContext);
 
 		if (selectionContext instanceof IRemoteSelectionContext) {
 			List<IModelNode> launchContexts = new ArrayList<IModelNode>(Arrays.asList(LaunchContextsPersistenceDelegate.getLaunchContexts(launchSpec)));
 			IModelNode remoteCtx = ((IRemoteSelectionContext)selectionContext).getRemoteCtx();
 			if (!launchContexts.contains(remoteCtx)) {
 				launchContexts.add(remoteCtx);
 				LaunchContextsPersistenceDelegate.setLaunchContexts(launchSpec, launchContexts.toArray(new IModelNode[launchContexts.size()]));
 			}
 
 			if (remoteCtx instanceof IPeerModel) {
 				String launchConfigAttributes = ((IPeerModel)remoteCtx).getPeer().getAttributes().get(IPeerModelProperties.PROP_LAUNCH_CONFIG_ATTRIBUTES);
 				if (launchConfigAttributes != null) {
 					IPersistenceDelegate delegate = PersistenceManager.getInstance().getDelegate(Map.class, launchConfigAttributes, false);
 					try {
 						Map<String, String> attributes = (Map<String,String>)delegate.read(Map.class, launchConfigAttributes, null);
 						attributes.remove(ILaunchContextLaunchAttributes.ATTR_LAUNCH_CONTEXTS);
 						attributes.remove(ICommonLaunchAttributes.ATTR_UUID);
 						attributes.remove(ICommonLaunchAttributes.ATTR_LAST_LAUNCHED);
 						for (Entry<String, String> entry : attributes.entrySet()) {
 							launchSpec.addAttribute(entry.getKey(), entry.getValue(), true);
 						}
 					}
 					catch (Exception e) {
 					}
 				}
 			}
 
 			launchSpec.setLaunchConfigName(getDefaultLaunchName(launchSpec));
 		}
 
 		return launchSpec;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.launch.core.lm.delegates.DefaultLaunchManagerDelegate#getDefaultLaunchName(org.eclipse.tcf.te.launch.core.lm.interfaces.ILaunchSpecification)
 	 */
 	@Override
 	public String getDefaultLaunchName(ILaunchSpecification launchSpec) {
 		IModelNode[] contexts = LaunchContextsPersistenceDelegate.getLaunchContexts(launchSpec);
 		String name = getDefaultLaunchName((contexts != null && contexts.length > 0 ? contexts[0] : null));
 		return name.trim().length() > 0 ? name.trim() : super.getDefaultLaunchName(launchSpec);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.launch.core.lm.delegates.DefaultLaunchManagerDelegate#getDefaultLaunchName(org.eclipse.debug.core.ILaunchConfiguration)
 	 */
 	@Override
 	public String getDefaultLaunchName(ILaunchConfiguration launchConfig) {
 		IModelNode[] contexts = LaunchContextsPersistenceDelegate.getLaunchContexts(launchConfig);
 		String name = getDefaultLaunchName((contexts != null && contexts.length > 0 ? contexts[0] : null));
 		return name.trim().length() > 0 ? name.trim() : super.getDefaultLaunchName(launchConfig);
 	}
 
 	private String getDefaultLaunchName(IModelNode context) {
 		if (context != null) {
 			return context.getName();
 		}
 		return ""; //$NON-NLS-1$
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.launch.core.lm.delegates.DefaultLaunchManagerDelegate#getMandatoryAttributes()
 	 */
 	@Override
 	protected List<String> getMandatoryAttributes() {
 		return Arrays.asList(MANDATORY_CONFIG_ATTRIBUTES);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.launch.core.lm.delegates.DefaultLaunchManagerDelegate#getNumAttributes()
 	 */
 	@Override
 	protected int getNumAttributes() {
 		return 1;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.launch.core.lm.delegates.DefaultLaunchManagerDelegate#getAttributeRanking(java.lang.String)
 	 */
 	@Override
 	protected int getAttributeRanking(String attributeKey) {
 		if (ILaunchContextLaunchAttributes.ATTR_LAUNCH_CONTEXTS.equals(attributeKey)) {
 			return getNumAttributes() * 2;
 		}
 		return 1;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.debug.core.ILaunchConfigurationListener#launchConfigurationAdded(org.eclipse.debug.core.ILaunchConfiguration)
 	 */
 	@Override
 	public void launchConfigurationAdded(ILaunchConfiguration configuration) {
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.debug.core.ILaunchConfigurationListener#launchConfigurationChanged(org.eclipse.debug.core.ILaunchConfiguration)
 	 */
 	@Override
 	public void launchConfigurationChanged(ILaunchConfiguration configuration) {
 		try {
 			IModelNode[] contexts = LaunchContextsPersistenceDelegate.getLaunchContexts(configuration);
 			if (contexts != null && contexts.length == 1 && contexts[0] instanceof IPeerModel) {
 				final IPeerModel peerModel = (IPeerModel)contexts[0];
 				@SuppressWarnings({ "unchecked", "rawtypes" })
 				Map<?,?> attributes = new LinkedHashMap(configuration.getAttributes());
 				attributes.remove(ILaunchContextLaunchAttributes.ATTR_LAUNCH_CONTEXTS);
 				attributes.remove(ICommonLaunchAttributes.ATTR_UUID);
 				attributes.remove(ICommonLaunchAttributes.ATTR_LAST_LAUNCHED);
 				attributes.remove(ICommonLaunchAttributes.ATTR_CAPTURE_IN_FILE);
 				attributes.remove(ICommonLaunchAttributes.ATTR_CAPTURE_OUTPUT);
 
 				String oldLaunchConfigAttributes = peerModel.getPeer().getAttributes().get(IPeerModelProperties.PROP_LAUNCH_CONFIG_ATTRIBUTES);
 				oldLaunchConfigAttributes = oldLaunchConfigAttributes == null ? "" : oldLaunchConfigAttributes.trim(); //$NON-NLS-1$
 
 				IPersistenceDelegate delegate = PersistenceManager.getInstance().getDelegate(Map.class, String.class, false);
 				final String launchConfigAttributes = attributes.isEmpty() ? "" : (String)delegate.write(attributes, String.class, null); //$NON-NLS-1$
 
 				if (!launchConfigAttributes.equals(oldLaunchConfigAttributes)) {
 					Protocol.invokeAndWait(new Runnable() {
 						@Override
 						public void run() {
 							IPeer oldPeer = peerModel.getPeer();
 							Map<String, String> attributes = new HashMap<String, String>(peerModel.getPeer().getAttributes());
 							if (launchConfigAttributes.trim().length() == 0) {
 								attributes.remove(IPeerModelProperties.PROP_LAUNCH_CONFIG_ATTRIBUTES);
 							}
 							else {
 								attributes.put(IPeerModelProperties.PROP_LAUNCH_CONFIG_ATTRIBUTES, launchConfigAttributes);
 							}
 							IPeer newPeer = new Peer(attributes);
 							if (oldPeer instanceof TransientPeer && !(oldPeer instanceof AbstractPeer || oldPeer instanceof PeerRedirector || oldPeer instanceof Peer)) {
 								peerModel.setProperty(org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerModelProperties.PROP_INSTANCE, newPeer);
 							} else {
 								Model.getModel().getService(ILocatorModelUpdateService.class).mergeUserDefinedAttributes(peerModel, newPeer, false);
 							}
 						}
 					});
 				}
 			}
 		}
 		catch (Exception e) {
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.debug.core.ILaunchConfigurationListener#launchConfigurationRemoved(org.eclipse.debug.core.ILaunchConfiguration)
 	 */
 	@Override
 	public void launchConfigurationRemoved(ILaunchConfiguration configuration) {
 	}
 }
