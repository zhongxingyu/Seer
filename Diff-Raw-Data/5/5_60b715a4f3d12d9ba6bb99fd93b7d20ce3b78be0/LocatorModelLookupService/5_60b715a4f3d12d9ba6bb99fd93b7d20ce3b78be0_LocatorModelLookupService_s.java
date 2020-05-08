 /*******************************************************************************
  * Copyright (c) 2011, 2012 Wind River Systems, Inc. and others. All rights reserved.
  * This program and the accompanying materials are made available under the terms
  * of the Eclipse Public License v1.0 which accompanies this distribution, and is
  * available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * Wind River Systems - initial API and implementation
  *******************************************************************************/
 package org.eclipse.tcf.te.tcf.locator.services;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.tcf.protocol.IPeer;
 import org.eclipse.tcf.protocol.Protocol;
 import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.ILocatorModel;
 import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerModel;
 import org.eclipse.tcf.te.tcf.locator.interfaces.services.ILocatorModelLookupService;
 import org.eclipse.tcf.te.tcf.locator.interfaces.services.ILocatorModelPeerNodeQueryService;
 
 
 /**
  * Default locator model lookup service implementation.
  */
 public class LocatorModelLookupService extends AbstractLocatorModelService implements ILocatorModelLookupService {
 
 	/**
 	 * Constructor.
 	 *
 	 * @param parentModel The parent locator model instance. Must not be <code>null</code>.
 	 */
 	public LocatorModelLookupService(ILocatorModel parentModel) {
 		super(parentModel);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.tcf.locator.core.interfaces.services.ILocatorModelLookupService#lkupPeerModelById(java.lang.String)
 	 */
 	@Override
 	public IPeerModel lkupPeerModelById(String id) {
 		Assert.isNotNull(id);
 		Assert.isTrue(Protocol.isDispatchThread(), "Illegal Thread Access"); //$NON-NLS-1$
 
 		IPeerModel node = null;
 		for (IPeerModel candidate : getLocatorModel().getPeers()) {
 			IPeer peer = candidate.getPeer();
 			if (id.equals(peer.getID())) {
 				node = candidate;
 				break;
 			} else if (peer.getAttributes().get("remote.id.transient") != null //$NON-NLS-1$
							&& peer.getAttributes().get("remote.id.transient").equals(peer.getID())) { //$NON-NLS-1$
 				node = candidate;
 				break;
 			}
 		}
 
 		return node;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.tcf.locator.interfaces.services.ILocatorModelLookupService#lkupPeerModelById(java.lang.String, java.lang.String)
 	 */
 	@Override
 	public IPeerModel lkupPeerModelById(String parentId, String id) {
 		Assert.isNotNull(parentId);
 		Assert.isNotNull(id);
 		Assert.isTrue(Protocol.isDispatchThread(), "Illegal Thread Access"); //$NON-NLS-1$
 
 		IPeerModel node = null;
 		for (IPeerModel candidate : getLocatorModel().getChildren(parentId)) {
 			IPeer peer = candidate.getPeer();
 			if (id.equals(peer.getID())) {
 				node = candidate;
 				break;
 			} else if (peer.getAttributes().get("remote.id.transient") != null //$NON-NLS-1$
							&& peer.getAttributes().get("remote.id.transient").equals(peer.getID())) { //$NON-NLS-1$
 				node = candidate;
 				break;
 			}
 		}
 
 		return node;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.tcf.locator.interfaces.services.ILocatorModelLookupService#lkupPeerModelByAgentId(java.lang.String)
 	 */
 	@Override
 	public IPeerModel[] lkupPeerModelByAgentId(String agentId) {
 		Assert.isNotNull(agentId);
 		Assert.isTrue(Protocol.isDispatchThread(), "Illegal Thread Access"); //$NON-NLS-1$
 
 		List<IPeerModel> nodes = new ArrayList<IPeerModel>();
 		for (IPeerModel candidate : getLocatorModel().getPeers()) {
 			IPeer peer = candidate.getPeer();
 			if (agentId.equals(peer.getAgentID())) {
 				nodes.add(candidate);
 			}
 		}
 
 		return nodes.toArray(new IPeerModel[nodes.size()]);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.tcf.locator.interfaces.services.ILocatorModelLookupService#lkupPeerModelByAgentId(java.lang.String, java.lang.String)
 	 */
 	@Override
 	public IPeerModel[] lkupPeerModelByAgentId(String parentId, String agentId) {
 		Assert.isNotNull(parentId);
 		Assert.isNotNull(agentId);
 		Assert.isTrue(Protocol.isDispatchThread(), "Illegal Thread Access"); //$NON-NLS-1$
 
 		List<IPeerModel> nodes = new ArrayList<IPeerModel>();
 		for (IPeerModel candidate : getLocatorModel().getChildren(parentId)) {
 			IPeer peer = candidate.getPeer();
 			if (agentId.equals(peer.getAgentID())) {
 				nodes.add(candidate);
 			}
 		}
 
 		return nodes.toArray(new IPeerModel[nodes.size()]);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.tcf.locator.interfaces.services.ILocatorModelLookupService#lkupPeerModelBySupportedServices(java.lang.String[], java.lang.String[])
 	 */
 	@Override
 	public IPeerModel[] lkupPeerModelBySupportedServices(String[] expectedLocalServices, String[] expectedRemoteServices) {
 		Assert.isTrue(!Protocol.isDispatchThread(), "Illegal Thread Access"); //$NON-NLS-1$
 
 		ILocatorModel model = getLocatorModel();
 		ILocatorModelPeerNodeQueryService queryService = model.getService(ILocatorModelPeerNodeQueryService.class);
 
 		List<IPeerModel> nodes = new ArrayList<IPeerModel>();
 		for (IPeerModel candidate : model.getPeers()) {
 			String services = queryService.queryLocalServices(candidate);
 
 			boolean matchesExpectations = true;
 
 			// Ignore the local services if not expectations are set
 			if (expectedLocalServices != null && expectedLocalServices.length > 0) {
 				if (services != null) {
 					for (String service : expectedLocalServices) {
 						if (!services.contains(service)) {
 							matchesExpectations = false;
 							break;
 						}
 					}
 				} else {
 					matchesExpectations = false;
 				}
 			}
 
 			if (!matchesExpectations) continue;
 
 			services = queryService.queryRemoteServices(candidate);
 
 			// Ignore the remote services if not expectations are set
 			if (expectedRemoteServices != null && expectedRemoteServices.length > 0) {
 				if (services != null) {
 					for (String service : expectedRemoteServices) {
 						if (!services.contains(service)) {
 							matchesExpectations = false;
 							break;
 						}
 					}
 				} else {
 					matchesExpectations = false;
 				}
 			}
 
 			if (matchesExpectations) nodes.add(candidate);
 		}
 
 		return nodes.toArray(new IPeerModel[nodes.size()]);
 	}
 
 }
