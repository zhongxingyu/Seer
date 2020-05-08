 /*******************************************************************************
  * Copyright (c) 2011, 2013 Wind River Systems, Inc. and others. All rights reserved.
  * This program and the accompanying materials are made available under the terms
  * of the Eclipse Public License v1.0 which accompanies this distribution, and is
  * available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * Wind River Systems - initial API and implementation
  *******************************************************************************/
 package org.eclipse.tcf.te.tcf.locator;
 
 import java.net.ConnectException;
 import java.net.InetAddress;
 import java.net.SocketTimeoutException;
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.atomic.AtomicReference;
 
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.tcf.core.Command;
 import org.eclipse.tcf.protocol.IChannel;
 import org.eclipse.tcf.protocol.IPeer;
 import org.eclipse.tcf.protocol.IToken;
 import org.eclipse.tcf.protocol.Protocol;
 import org.eclipse.tcf.services.ILocator;
 import org.eclipse.tcf.te.runtime.callback.Callback;
 import org.eclipse.tcf.te.runtime.interfaces.callback.ICallback;
 import org.eclipse.tcf.te.runtime.utils.net.IPAddressUtil;
 import org.eclipse.tcf.te.tcf.core.Tcf;
 import org.eclipse.tcf.te.tcf.core.peers.Peer;
 import org.eclipse.tcf.te.tcf.locator.activator.CoreBundleActivator;
 import org.eclipse.tcf.te.tcf.locator.interfaces.IScanner;
 import org.eclipse.tcf.te.tcf.locator.interfaces.ITracing;
 import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.ILocatorModel;
 import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerModel;
 import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerModelProperties;
 import org.eclipse.tcf.te.tcf.locator.interfaces.services.ILocatorModelLookupService;
 import org.eclipse.tcf.te.tcf.locator.interfaces.services.ILocatorModelUpdateService;
 import org.eclipse.tcf.te.tcf.locator.nodes.PeerModel;
 import org.eclipse.tcf.te.tcf.locator.nodes.PeerRedirector;
 
 
 /**
  * Scanner runnable to be executed for each peer to probe within the
  * TCF event dispatch thread.
  */
 public class ScannerRunnable implements Runnable, IChannel.IChannelListener {
 
 	// Reference to the parent model scanner
 	private final IScanner parentScanner;
 	// Reference to the peer model node to update
 	/* default */ final IPeerModel peerNode;
 	// Reference to the channel
 	/* default */ IChannel channel = null;
 	// Mark if the used channel is a shared channel instance
 	/* default */ boolean sharedChannel = false;
 
 	// Optional callback to invoke once the scan has been completed
 	private final ICallback callback;
 
 	/**
 	 * Constructor.
 	 *
 	 * @param scanner The parent model scanner or <code>null</code> if the runnable is constructed from outside a scanner.
 	 * @param peerNode The peer model instance. Must not be <code>null</code>.
 	 */
 	public ScannerRunnable(IScanner scanner, IPeerModel peerNode) {
 		this(scanner, peerNode, null);
 	}
 
 	/**
 	 * Constructor.
 	 *
 	 * @param scanner The parent model scanner or <code>null</code> if the runnable is constructed from outside a scanner.
 	 * @param peerNode The peer model instance. Must not be <code>null</code>.
 	 * @param callback The callback to invoke once the scan has been completed or <code>null</code>.
 	 */
 	public ScannerRunnable(IScanner scanner, IPeerModel peerNode, ICallback callback) {
 		super();
 
 		parentScanner = scanner;
 
 		Assert.isNotNull(peerNode);
 		this.peerNode = peerNode;
 
 		this.callback = callback;
 	}
 
 	/**
 	 * Returns the parent scanner instance.
 	 *
 	 * @return The parent scanner instance or <code>null</code>.
 	 */
 	protected final IScanner getParentScanner() {
 		return parentScanner;
 	}
 
 	/* (non-Javadoc)
 	 * @see java.lang.Runnable#run()
 	 */
 	@Override
 	public void run() {
 		Assert.isTrue(Protocol.isDispatchThread(), "Illegal Thread Access"); //$NON-NLS-1$
 
 		// If the parent scanner is terminated, don't do anything
 		IScanner scanner = getParentScanner();
 		if (scanner != null && scanner.isTerminated()) {
 			if (callback != null) callback.done(this, Status.OK_STATUS);
 			return;
 		}
 
 		// If a scanner runnable already active for this peer node, there
 		// is no need to run another scan.
 		if (peerNode.getProperty("scanner.transient") != null) { //$NON-NLS-1$
 			if (callback != null) callback.done(this, Status.OK_STATUS);
 			return;
 		}
 		peerNode.setProperty("scanner.transient", this); //$NON-NLS-1$
 
 		// Determine the peer
 		IPeer peer = peerNode.getPeer();
 		if (peer == null) {
 			if (callback != null) callback.done(this, Status.OK_STATUS);
 			return;
 		}
 
 		// Don't scan value-adds
 		String value = peer.getAttributes().get("ValueAdd"); //$NON-NLS-1$
 		boolean isValueAdd = value != null && ("1".equals(value.trim()) || Boolean.parseBoolean(value.trim())); //$NON-NLS-1$
 
 		if (isValueAdd) {
 			if (callback != null) callback.done(this, Status.OK_STATUS);
 			return;
 		}
 
 		// Do not open a channel to incomplete peer nodes
 		if (peerNode.isComplete()) {
 			if (CoreBundleActivator.getTraceHandler().isSlotEnabled(ITracing.ID_TRACE_SCANNER)) {
 				CoreBundleActivator.getTraceHandler().trace("Scanner runnable invoked for peer '" + peerNode.getName() + "' (" + peerNode.getPeerId() + "). Attempting to open channel ...", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
 															ITracing.ID_TRACE_SCANNER, ScannerRunnable.this);
 			}
 
 			// Check if there is a shared channel available which is still in open state
 			channel = Tcf.getChannelManager().getChannel(peer);
 			if (channel == null || channel.getState() != IChannel.STATE_OPEN) {
 				sharedChannel = false;
 				// Open the channel
 				channel = peer.openChannel();
 				// Add ourself as channel listener
 				channel.addChannelListener(this);
 			} else {
 				sharedChannel = true;
 				// Shared channel is in open state -> use it
 				onChannelOpened();
 			}
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.protocol.IChannel.IChannelListener#onChannelOpened()
 	 */
     @Override
 	public void onChannelOpened() {
 		// Peer is reachable
 		if (channel != null && !sharedChannel) {
 			// Remove ourself as channel listener
 			channel.removeChannelListener(this);
 		}
 
 		if (CoreBundleActivator.getTraceHandler().isSlotEnabled(ITracing.ID_TRACE_SCANNER)) {
 			CoreBundleActivator.getTraceHandler().trace("Scanner runnable onChannelOpened invoked for peer '" + peerNode.getName() + "' (" + peerNode.getPeerId() + ").", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
 														ITracing.ID_TRACE_SCANNER, ScannerRunnable.this);
 		}
 
 		// Turn off change notifications temporarily
 		final boolean changed = peerNode.setChangeEventsEnabled(false);
 
 		// Set the peer state property
 		int counter = peerNode.getIntProperty(IPeerModelProperties.PROP_CHANNEL_REF_COUNTER);
 		if (!peerNode.isProperty(IPeerModelProperties.PROP_STATE, IPeerModelProperties.STATE_WAITING_FOR_READY)) {
 			peerNode.setProperty(IPeerModelProperties.PROP_STATE, counter > 0 ? IPeerModelProperties.STATE_CONNECTED : IPeerModelProperties.STATE_REACHABLE);
 			peerNode.setProperty(IPeerModelProperties.PROP_LAST_SCANNER_ERROR, null);
 		}
 
 		// Get the parent model from the model mode
 		final ILocatorModel model = (ILocatorModel)peerNode.getAdapter(ILocatorModel.class);
 
 		if (channel != null && channel.getState() == IChannel.STATE_OPEN) {
 
 			// Update the services lists
 			ILocatorModelUpdateService updateService = model != null ? model.getService(ILocatorModelUpdateService.class) : null;
 			if (updateService != null) {
 				Collection<String> localServices = channel.getLocalServices();
 				Collection<String> remoteServices = channel.getRemoteServices();
 
 				updateService.updatePeerServices(peerNode, localServices, remoteServices);
 
 				if (CoreBundleActivator.getTraceHandler().isSlotEnabled(ITracing.ID_TRACE_SCANNER)) {
 					CoreBundleActivator.getTraceHandler().trace("Services: local = " + localServices + ", remote = " + remoteServices, //$NON-NLS-1$ //$NON-NLS-2$
 																ITracing.ID_TRACE_SCANNER, ScannerRunnable.this);
 				}
 			}
 
 			// If we don't queried the DNS name of the peer, or the peer IP changed,
 			// trigger a query (can run in any thread, outside TCF dispatch and UI
 			// thread). This make sense only if there is an IP address to query at all.
 			final String ip = channel.getRemotePeer().getAttributes().get(IPeer.ATTR_IP_HOST);
 			if (ip != null && !"".equals(ip)) { //$NON-NLS-1$
 				if (peerNode.getStringProperty("dns.name.transient") == null || !ip.equals(peerNode.getStringProperty("dns.lastIP.transient"))) { //$NON-NLS-1$ //$NON-NLS-2$
 					// If the IP address changed, reset the "do not query again" marker
 					if (!ip.equals(peerNode.getStringProperty("dns.lastIP.transient"))) { //$NON-NLS-1$
 						peerNode.setProperty("dns.lastIP.transient", ip); //$NON-NLS-1$
 						peerNode.setProperty("dns.skip.transient", false); //$NON-NLS-1$
 					}
 
 					if (!peerNode.getBooleanProperty("dns.skip.transient")) { //$NON-NLS-1$
 						Runnable runnable = new Runnable() {
 							@Override
 							public void run() {
 								try {
 									InetAddress address = InetAddress.getByName(ip);
 									final AtomicReference<String> nameRef = new AtomicReference<String>();
 									nameRef.set(address.getCanonicalHostName());
 
 									if (ip.equals(nameRef.get()) && IPAddressUtil.getInstance().isLocalHost(ip)) {
 										String[] candidates = IPAddressUtil.getInstance().getCanonicalHostNames();
 										for (String candidate : candidates) {
 											if (!ip.equals(candidate)) {
 												nameRef.set(candidate);
 												break;
 											}
 										}
 									}
 
 									Protocol.invokeLater(new Runnable() {
 										@Override
 										public void run() {
 											String name = nameRef.get();
 											if (name != null && !"".equals(name) && !ip.equals(name)) { //$NON-NLS-1$
 												String dnsName = name.indexOf('.') != -1 ? name.substring(0, name.indexOf('.')) : name;
 												if (!ip.equalsIgnoreCase(dnsName)) {
 													peerNode.setProperty("dns.name.transient", dnsName.toLowerCase()); //$NON-NLS-1$
 												}
 											}
 										}
 									});
 								}
 								catch (UnknownHostException e) {
 									Protocol.invokeLater(new Runnable() {
 										@Override
                                         public void run() {
 											peerNode.setProperty("dns.skip.transient", true); //$NON-NLS-1$
 										}
 									});
 								}
 							}
 						};
 
 						Thread thread = new Thread(runnable, "DNS Query Thread for " + ip); //$NON-NLS-1$
 						thread.start();
 					}
 				}
 			}
 
 			// Check if the agent ID is already set
 			String agentID = channel.getRemotePeer().getAgentID();
 			if (agentID == null && channel.getRemotePeer() instanceof Peer) {
 				// Determine the agent ID of the remote agent
 				ILocator locator = channel.getRemoteService(ILocator.class);
 				if (locator != null) {
 					locator.getAgentID(new ILocator.DoneGetAgentID() {
 						@Override
 						public void doneGetAgentID(IToken token, Exception error, String agentID) {
 							// Ignore errors. If the agent does not implement this command, we
 							// do not fail.
 							if (agentID != null) {
 								// Update the peer attributes
 								Map<String, String> attrs = new HashMap<String, String>(channel.getRemotePeer().getAttributes());
 								attrs.put(IPeer.ATTR_AGENT_ID, agentID);
 								peerNode.setProperty(IPeerModelProperties.PROP_INSTANCE, new Peer(attrs));
 							}
 
 							// Get the peers from the remote locator
 							getPeers(channel, model, ip, new Callback() {
 								@Override
 								protected void internalDone(Object caller, IStatus status) {
 									// Complete
 									onDone(peerNode, changed);
 								}
 							});
 						}
 					});
 				} else {
 					// Complete
 					onDone(peerNode, changed);
 				}
 			} else {
 				// Get the peers from the remote locator
 				getPeers(channel, model, ip, new Callback() {
 					@Override
 					protected void internalDone(Object caller, IStatus status) {
 						// Complete
 						onDone(peerNode, changed);
 					}
 				});
 			}
 		} else {
 			// Complete
 			onDone(peerNode, changed);
 		}
 	}
 
     /**
      * Query the peers from the remote locator.
      *
      * @param channel The channel. Must not be <code>null</code>.
      * @param model The locator model. Must not be <code>null</code>.
      * @param ip The IP address or <code>null</code>.
      * @param callback The callback. Must not be <code>null</code>.
      */
 	@SuppressWarnings("unused")
     protected void getPeers(final IChannel channel, final ILocatorModel model, final String ip, final ICallback callback) {
 		Assert.isNotNull(channel);
 		Assert.isNotNull(model);
 		Assert.isNotNull(callback);
 
 		// Keep the channel open as long as the query for the remote peers is running.
 		boolean keepOpen = false;
 
 		// Get the agent ID of the remote agent we are connected too.
 		// Have to use the peer model node here.
 		final String agentID = peerNode.getPeer().getAgentID();
 
 		// Ask for discovered peers from the remote agents POV.
 		//
 		// Note: For simulators connected via NAT, we have to do this for localhost address
 		//       as well. Otherwise we miss the discoverable agents only known to the simulator.
 		//       The same applies to agent being discovered. If you don't ask for discovered peers
 		//       here too, we may miss some routes.
 		if (ip != null && !"".equals(ip)) { //$NON-NLS-1$
 			// Use the open channel to ask the remote peer what other peers it knows
 			ILocator locator = channel.getRemoteService(ILocator.class);
 			if (locator != null) {
 				// Channel must be kept open as long as the command runs
 				keepOpen = true;
 				// Issue the command
 				new Command(channel, locator, "getPeers", null) { //$NON-NLS-1$
 					@Override
 					public void done(Exception error, Object[] args) {
 						if (error == null) {
 							Assert.isTrue(args.length == 2);
 							error = toError(args[0]);
 						}
 						// If the error is still null here, process the returned peers
 						if (error == null && args[1] != null) {
 							// Get the parent peer
 							IPeer parentPeer = channel.getRemotePeer();
 							// Get the old child list
 							List<IPeerModel> oldChildren = new ArrayList<IPeerModel>(model.getChildren(parentPeer.getID()));
 
 							// "getPeers" returns a collection of peer attribute maps
 							@SuppressWarnings("unchecked")
 							Collection<Map<String,String>> peerAttributesList = (Collection<Map<String,String>>)args[1];
 							for (Map<String,String> attributes : peerAttributesList) {
 								// Don't process value-add's
 								String value = attributes.get("ValueAdd"); //$NON-NLS-1$
 								boolean isValueAdd = value != null && ("1".equals(value.trim()) || Boolean.parseBoolean(value.trim())); //$NON-NLS-1$
 
 								if (isValueAdd) continue;
 
 								// Get the peer id
 								String peerId = attributes.get(IPeer.ATTR_ID);
 								// Create a peer instance
 								IPeer peer = new PeerRedirector(parentPeer, attributes);
 								// Try to find an existing peer node first
 								IPeerModel peerNode = model.getService(ILocatorModelLookupService.class).lkupPeerModelById(parentPeer.getID(), peerId);
 								if (peerNode == null) {
 									// Not yet known -> add it
 									peerNode = new PeerModel(model, peer);
 									peerNode.setParent(ScannerRunnable.this.peerNode);
 									peerNode.setProperty(IPeerModelProperties.PROP_SCANNER_EXCLUDE, true);
 									// Validate the peer node before adding
 									peerNode = model.validateChildPeerNodeForAdd(peerNode);
 									if (peerNode != null) {
 										// Add the child peer node to model
 										model.getService(ILocatorModelUpdateService.class).addChild(peerNode);
 									}
 								} else {
 									// The parent node should be set and match
 									Assert.isTrue(peerNode.getParent(IPeerModel.class) != null && peerNode.getParent(IPeerModel.class).equals(ScannerRunnable.this.peerNode));
 									// Peer node found, update the peer instance
 									peerNode.setProperty(IPeerModelProperties.PROP_INSTANCE, peer);
 									// And remove it from the old child list
 									oldChildren.remove(peerNode);
 								}
 							}
 
 							// Everything left in the old child list is not longer known to the remote peer
 							// However, the child list may include manual redirected static peers. Do not
 							// remove them here.
 							for (IPeerModel child : oldChildren) {
 								if (!child.isStatic()) {
 									// Remove the child peer node from the model
 									model.getService(ILocatorModelUpdateService.class).removeChild(child);
 								}
 							}
 						}
 
 						// Once everything is processed, close the channel
 						if (!sharedChannel) channel.close();
 						// Invoke the callback
 						callback.done(ScannerRunnable.this, Status.OK_STATUS);
 					}
 				};
 			}
 		}
 
 		// And close the channel
 		if (!sharedChannel && !keepOpen) channel.close();
 
 		// Invoke the callback
 		if (!keepOpen) callback.done(ScannerRunnable.this, Status.OK_STATUS);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.protocol.IChannel.IChannelListener#onChannelClosed(java.lang.Throwable)
 	 */
 	@Override
 	public void onChannelClosed(Throwable error) {
 		// Peer is not reachable
 
 		if (channel != null) {
 			// Remove ourself as channel listener
 			channel.removeChannelListener(this);
 		}
 
 		if (CoreBundleActivator.getTraceHandler().isSlotEnabled(ITracing.ID_TRACE_SCANNER)) {
 			CoreBundleActivator.getTraceHandler().trace("Scanner runnable onChannelClosed invoked for peer '" + peerNode.getName() + "' (" + peerNode.getPeerId() + "). Error was: " + (error != null ? error.getLocalizedMessage() : null), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
 														ITracing.ID_TRACE_SCANNER, ScannerRunnable.this);
 		}
 
 		// Set the peer state property, if the scanner the runnable
 		// has been scheduled from is still active.
 		if (parentScanner == null || !parentScanner.isTerminated()) {
 			// Turn off change notifications temporarily
 			boolean changed = peerNode.setChangeEventsEnabled(false);
 
 			peerNode.setProperty(IPeerModelProperties.PROP_CHANNEL_REF_COUNTER, null);
 			if (!peerNode.isProperty(IPeerModelProperties.PROP_STATE, IPeerModelProperties.STATE_WAITING_FOR_READY)) {
 				boolean timeout = error instanceof SocketTimeoutException || (error instanceof ConnectException && error.getMessage() != null && error.getMessage().startsWith("Connection timed out:")); //$NON-NLS-1$
 				peerNode.setProperty(IPeerModelProperties.PROP_STATE, timeout ? IPeerModelProperties.STATE_NOT_REACHABLE : IPeerModelProperties.STATE_ERROR);
 				peerNode.setProperty(IPeerModelProperties.PROP_LAST_SCANNER_ERROR, error instanceof SocketTimeoutException ? null : error);
 			}
 
 			// Clear out previously determined services
 			ILocatorModel model = (ILocatorModel)peerNode.getAdapter(ILocatorModel.class);
 			if (model != null) {
 				ILocatorModelUpdateService updateService = model.getService(ILocatorModelUpdateService.class);
 				updateService.updatePeerServices(peerNode, null, null);
 
 				// Clean out possible child nodes
 				model.setChildren(peerNode.getPeerId(), null);
 			}
 
 			// Clean out DNS name detection
 			peerNode.setProperty("dns.name.transient", null); //$NON-NLS-1$
 			peerNode.setProperty("dns.lastIP.transient", null); //$NON-NLS-1$
 			peerNode.setProperty("dns.skip.transient", null); //$NON-NLS-1$
 
 			// Complete
 			onDone(peerNode, changed);
 		}
 	}
 
 	/**
 	 * Called from {@link #onChannelOpened()} and {@link #onChannelClosed(Throwable)} once
 	 * all operations of the scanner are completed.
 	 *
 	 * @param node The peer model node. Must not be <code>null</code>.
 	 * @param changed <code>True</code> if the change events shall be enabled, <code>false</code> otherwise.
 	 */
 	protected void onDone(IPeerModel node, boolean changed) {
 		Assert.isNotNull(node);
 
 		// Reset the scanner runnable marker
 		node.setProperty("scanner.transient", null); //$NON-NLS-1$
 
 		// Re-enable the change events and fire a "properties" change event
 		if (changed) {
 			node.setChangeEventsEnabled(true);
 			node.fireChangeEvent("properties", null, peerNode.getProperties()); //$NON-NLS-1$
 		}
 
 		if (callback != null) callback.done(this, Status.OK_STATUS);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.protocol.IChannel.IChannelListener#congestionLevel(int)
 	 */
 	@Override
 	public void congestionLevel(int level) {
 	}
 
 }
