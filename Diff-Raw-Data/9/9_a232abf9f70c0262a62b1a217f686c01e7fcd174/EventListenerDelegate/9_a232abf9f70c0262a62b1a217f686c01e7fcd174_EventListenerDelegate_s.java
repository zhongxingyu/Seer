 /*******************************************************************************
  * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
  * This program and the accompanying materials are made available under the terms
  * of the Eclipse Public License v1.0 which accompanies this distribution, and is
  * available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * Wind River Systems - initial API and implementation
  *******************************************************************************/
 
 package org.eclipse.tcf.te.tcf.launch.core.internal;
 
 import java.util.EventObject;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.eclipse.debug.core.DebugPlugin;
 import org.eclipse.debug.core.ILaunchConfiguration;
 import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
 import org.eclipse.tcf.protocol.IPeer;
 import org.eclipse.tcf.te.launch.core.lm.LaunchConfigHelper;
 import org.eclipse.tcf.te.launch.core.lm.interfaces.ICommonLaunchAttributes;
 import org.eclipse.tcf.te.launch.core.lm.interfaces.ILaunchContextLaunchAttributes;
 import org.eclipse.tcf.te.launch.core.persistence.launchcontext.LaunchContextsPersistenceDelegate;
 import org.eclipse.tcf.te.runtime.concurrent.util.ExecutorsUtil;
 import org.eclipse.tcf.te.runtime.events.ChangeEvent;
 import org.eclipse.tcf.te.runtime.interfaces.events.IEventListener;
 import org.eclipse.tcf.te.runtime.model.interfaces.IModelNode;
 import org.eclipse.tcf.te.runtime.persistence.PersistenceManager;
 import org.eclipse.tcf.te.runtime.persistence.interfaces.IPersistenceDelegate;
 import org.eclipse.tcf.te.tcf.launch.core.interfaces.ILaunchTypes;
 import org.eclipse.tcf.te.tcf.launch.core.interfaces.IPeerModelProperties;
 import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerModel;
 
 /**
  * EventListenerDelegate
  */
 public class EventListenerDelegate implements IEventListener {
 
 	/**
 	 * Constructor.
 	 */
 	public EventListenerDelegate() {
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.runtime.interfaces.events.IEventListener#eventFired(java.util.EventObject)
 	 */
 	@Override
 	public void eventFired(EventObject event) {
 		if (event instanceof ChangeEvent && event.getSource() instanceof IPeer) {
 			IPeer peer = (IPeer)event.getSource();
 			String launchConfigAttributes = peer.getAttributes().get(IPeerModelProperties.PROP_LAUNCH_CONFIG_ATTRIBUTES);
 			try {
 				for (ILaunchConfiguration config : DebugPlugin.getDefault().getLaunchManager().getLaunchConfigurations(DebugPlugin.getDefault().getLaunchManager().getLaunchConfigurationType(ILaunchTypes.ATTACH))) {
 					IModelNode[] contexts = LaunchContextsPersistenceDelegate.getLaunchContexts(config);
 					if (contexts != null && contexts.length == 1 && contexts[0] instanceof IPeerModel && ((IPeerModel)contexts[0]).getPeerId().equalsIgnoreCase(peer.getID())) {
 						IPersistenceDelegate delegate = PersistenceManager.getInstance().getDelegate(Map.class, launchConfigAttributes, false);
 						try {
							Map<String, String> attributes = (Map<String,String>)delegate.read(Map.class, launchConfigAttributes, null);
 							if (attributes != null) {
 								attributes.remove(ILaunchContextLaunchAttributes.ATTR_LAUNCH_CONTEXTS);
 								attributes.remove(ICommonLaunchAttributes.ATTR_UUID);
 								attributes.remove(ICommonLaunchAttributes.ATTR_LAST_LAUNCHED);
 								final ILaunchConfigurationWorkingCopy wc = config.getWorkingCopy();
 								for (Entry<String, String> entry : attributes.entrySet()) {
 									LaunchConfigHelper.addLaunchConfigAttribute(wc, entry.getKey(), entry.getValue());
 								}
 								ExecutorsUtil.executeInUI(new Runnable() {
 									@Override
 									public void run() {
 										try {
 											wc.doSave();
 										}
 										catch (Exception e) {
 										}
 									}
 								});
 							}
 						}
 						catch (Exception e) {
 						}
 
 					}
 				}
 			}
 			catch (Exception e) {
 			}
 		}
 	}
 }
