 /*******************************************************************************
  * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
  * This program and the accompanying materials are made available under the terms
  * of the Eclipse Public License v1.0 which accompanies this distribution, and is
  * available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * Wind River Systems - initial API and implementation
  *******************************************************************************/
 package org.eclipse.tcf.te.tcf.ui.internal.adapters;
 
 import java.util.Map;
 
 import org.eclipse.core.runtime.IAdaptable;
 import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.ILocatorModel;
 import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerModel;
 import org.eclipse.tcf.te.tcf.locator.interfaces.services.ILocatorModelPeerNodeQueryService;
 import org.eclipse.tcf.te.tcf.locator.model.Model;
 import org.eclipse.ui.IElementFactory;
 import org.eclipse.ui.IMemento;
 
 /**
  * The element factory to create IPeerModel from a memento which is read
  * from an external persistent storage.
  */
 public class PeerModelFactory implements IElementFactory {
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.eclipse.ui.IElementFactory#createElement(org.eclipse.ui.IMemento)
 	 */
 	@Override
 	public IAdaptable createElement(IMemento memento) {
 		String peerId = memento.getString("peerId"); //$NON-NLS-1$
 		Map<String, IPeerModel> map = (Map<String, IPeerModel>) Model.getModel().getAdapter(Map.class);
 		IPeerModel node = map.get(peerId);
 		// Make sure the remote services are up to date so 
 		// that content extension could correctly activated!
		ILocatorModel model = node.getModel();
		ILocatorModelPeerNodeQueryService queryService = model.getService(ILocatorModelPeerNodeQueryService.class);
		queryService.queryRemoteServices(node);
 		return node;
 	}
 }
