 /*******************************************************************************
  * Copyright (c) 2011, 2012 Wind River Systems, Inc. and others. All rights reserved.
  * This program and the accompanying materials are made available under the terms
  * of the Eclipse Public License v1.0 which accompanies this distribution, and is
  * available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * Wind River Systems - initial API and implementation
  *******************************************************************************/
 package org.eclipse.tcf.te.tcf.core.internal;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.atomic.AtomicInteger;
 import java.util.concurrent.atomic.AtomicReference;
 
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.OperationCanceledException;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.core.runtime.PlatformObject;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.osgi.util.NLS;
 import org.eclipse.tcf.core.AbstractPeer;
 import org.eclipse.tcf.protocol.IChannel;
 import org.eclipse.tcf.protocol.IPeer;
 import org.eclipse.tcf.protocol.Protocol;
 import org.eclipse.tcf.te.runtime.callback.Callback;
 import org.eclipse.tcf.te.tcf.core.activator.CoreBundleActivator;
 import org.eclipse.tcf.te.tcf.core.interfaces.IChannelManager;
 import org.eclipse.tcf.te.tcf.core.interfaces.tracing.ITraceIds;
 import org.eclipse.tcf.te.tcf.core.nls.Messages;
 import org.eclipse.tcf.te.tcf.core.peers.Peer;
 import org.eclipse.tcf.te.tcf.core.va.ValueAddManager;
 import org.eclipse.tcf.te.tcf.core.va.interfaces.IValueAdd;
 
 
 /**
  * TCF channel manager implementation.
  */
 public final class ChannelManager extends PlatformObject implements IChannelManager {
 	// The map of reference counters per peer id
 	/* default */ final Map<String, AtomicInteger> refCounters = new HashMap<String, AtomicInteger>();
 	// The map of channels per peer id
 	/* default */ final Map<String, IChannel> channels = new HashMap<String, IChannel>();
 
 	/**
 	 * Constructor.
 	 */
 	public ChannelManager() {
 		super();
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.tcf.core.interfaces.IChannelManager#openChannel(org.eclipse.tcf.protocol.IPeer, java.util.Map, org.eclipse.tcf.te.tcf.core.interfaces.IChannelManager.DoneOpenChannel)
 	 */
 	@Override
 	public void openChannel(final IPeer peer, final Map<String, Boolean> flags, final DoneOpenChannel done) {
 		if (CoreBundleActivator.getTraceHandler().isSlotEnabled(0, ITraceIds.TRACE_CHANNEL_MANAGER)) {
 			try {
 				throw new Throwable();
 			} catch (Throwable e) {
 				CoreBundleActivator.getTraceHandler().trace("ChannelManager#openChannel called from:", //$NON-NLS-1$
 															0, ITraceIds.TRACE_CHANNEL_MANAGER, IStatus.INFO, this);
 				e.printStackTrace();
 			}
 		}
 
 		Runnable runnable = new Runnable() {
 			@Override
             public void run() {
 				Assert.isTrue(Protocol.isDispatchThread(), "Illegal Thread Access"); //$NON-NLS-1$
 
 				// Check on the value-add's first
 				internalHandleValueAdds(peer, flags, new DoneHandleValueAdds() {
 					@Override
 					public void doneHandleValueAdds(final Throwable error, final IValueAdd[] valueAdds) {
 						// If the error is null, continue and open the channel
 						if (error == null) {
 							// Do we have any value add in the chain?
 							if (valueAdds != null && valueAdds.length > 0) {
 								// There are value-add's -> chain them now
 								internalChainValueAdds(valueAdds, peer, flags, done);
 							} else {
 								// No value-add's -> open a channel to the target peer directly
 								internalOpenChannel(peer, flags, done);
 							}
 						} else {
 							// Shutdown the value-add's launched
 							internalShutdownValueAdds(peer, valueAdds);
 							// Fail the channel opening
 							done.doneOpenChannel(error, null);
 						}
 					}
 				});
 			}
 		};
 		if (Protocol.isDispatchThread()) runnable.run();
 		else Protocol.invokeLater(runnable);
 	}
 
 	/**
 	 * Internal implementation of {@link #openChannel(IPeer, org.eclipse.tcf.te.tcf.core.interfaces.IChannelManager.DoneOpenChannel)}.
 	 * <p>
 	 * Reference counted channels are cached by the channel manager and must be closed via {@link #closeChannel(IChannel)} .
 	 * <p>
 	 * Method must be called within the TCF dispatch thread.
 	 *
 	 * @param peer The peer. Must not be <code>null</code>.
 	 * @param flags Map containing the flags to parameterize the channel opening, or <code>null</code>.
 	 * @param done The client callback. Must not be <code>null</code>.
 	 */
 	/* default */ void internalOpenChannel(final IPeer peer, final Map<String, Boolean> flags, final DoneOpenChannel done) {
 		Assert.isNotNull(peer);
 		Assert.isNotNull(done);
 		Assert.isTrue(Protocol.isDispatchThread(), "Illegal Thread Access"); //$NON-NLS-1$
 
 		// The channel instance to return
 		IChannel channel = null;
 
 		// Get the peer id
 		final String id = peer.getID();
 
 		if (CoreBundleActivator.getTraceHandler().isSlotEnabled(0, ITraceIds.TRACE_CHANNEL_MANAGER)) {
 			CoreBundleActivator.getTraceHandler().trace(NLS.bind(Messages.ChannelManager_openChannel_message, id, flags),
 														0, ITraceIds.TRACE_CHANNEL_MANAGER, IStatus.INFO, this);
 		}
 
 		// Extract the flags of interest form the given flags map
 		boolean forceNew = flags != null && flags.containsKey(IChannelManager.FLAG_FORCE_NEW) ? flags.get(IChannelManager.FLAG_FORCE_NEW).booleanValue() : false;
 		boolean noValueAdd = flags != null && flags.containsKey(IChannelManager.FLAG_NO_VALUE_ADD) ? flags.get(IChannelManager.FLAG_NO_VALUE_ADD).booleanValue() : false;
 		// If noValueAdd == true -> forceNew has to be true as well
 		if (noValueAdd) forceNew = true;
 
 		// Check if there is already a channel opened to this peer
 		channel = !forceNew ? channels.get(id) : null;
 		if (channel != null && (channel.getState() == IChannel.STATE_OPEN || channel.getState() == IChannel.STATE_OPENING)) {
 			// Increase the reference count
 			AtomicInteger counter = refCounters.get(id);
 			if (counter == null) {
 				counter = new AtomicInteger(0);
 				refCounters.put(id, counter);
 			}
 			counter.incrementAndGet();
 
 			if (CoreBundleActivator.getTraceHandler().isSlotEnabled(0, ITraceIds.TRACE_CHANNEL_MANAGER)) {
 				CoreBundleActivator.getTraceHandler().trace(NLS.bind(Messages.ChannelManager_openChannel_reuse_message, id, counter.toString()),
 															0, ITraceIds.TRACE_CHANNEL_MANAGER, IStatus.INFO, this);
 			}
 		} else if (channel != null) {
 			// Channel is not in open state -> drop the instance
 			channel = null;
 			channels.remove(id);
 			refCounters.remove(id);
 		}
 
 		// Opens a new channel if necessary
 		if (channel == null) {
 			if (CoreBundleActivator.getTraceHandler().isSlotEnabled(0, ITraceIds.TRACE_CHANNEL_MANAGER)) {
 				CoreBundleActivator.getTraceHandler().trace(NLS.bind(Messages.ChannelManager_openChannel_new_message, id),
 															0, ITraceIds.TRACE_CHANNEL_MANAGER, IStatus.INFO, this);
 			}
 
 			try {
 				channel = peer.openChannel();
 
 				if (channel != null) {
 					if (!forceNew) channels.put(id, channel);
 					if (!forceNew) refCounters.put(id, new AtomicInteger(1));
 
 					// Register the channel listener
 					final IChannel finChannel = channel;
 					channel.addChannelListener(new IChannel.IChannelListener() {
 
 						@Override
 						public void onChannelOpened() {
 							// Remove ourself as listener from the channel
 							finChannel.removeChannelListener(this);
 
 							if (CoreBundleActivator.getTraceHandler().isSlotEnabled(0, ITraceIds.TRACE_CHANNEL_MANAGER)) {
 								CoreBundleActivator.getTraceHandler().trace(NLS.bind(Messages.ChannelManager_openChannel_success_message, id),
 																			0, ITraceIds.TRACE_CHANNEL_MANAGER, IStatus.INFO, ChannelManager.this);
 							}
 
 							// Channel opening succeeded
 							done.doneOpenChannel(null, finChannel);
 						}
 
 						@Override
 						public void onChannelClosed(Throwable error) {
 							// Remove ourself as listener from the channel
 							finChannel.removeChannelListener(this);
 							// Clean the reference counter and the channel map
 							channels.remove(id);
 							refCounters.remove(id);
 
 							if (CoreBundleActivator.getTraceHandler().isSlotEnabled(0, ITraceIds.TRACE_CHANNEL_MANAGER)) {
 								CoreBundleActivator.getTraceHandler().trace(NLS.bind(Messages.ChannelManager_openChannel_failed_message, id, error),
 																			0, ITraceIds.TRACE_CHANNEL_MANAGER, IStatus.INFO, this);
 							}
 
 							// Channel opening failed
 							done.doneOpenChannel(error != null ? error : new OperationCanceledException(), finChannel);
 						}
 
 						@Override
 						public void congestionLevel(int level) {
 							// ignored
 						}
 					});
 				} else {
 					// Channel is null? Something went terrible wrong.
 					done.doneOpenChannel(new Exception("Unexpected null return value from IPeer#openChannel()!"), null); //$NON-NLS-1$
 				}
 			} catch (Throwable e) {
 				if (CoreBundleActivator.getTraceHandler().isSlotEnabled(0, ITraceIds.TRACE_CHANNEL_MANAGER)) {
 					CoreBundleActivator.getTraceHandler().trace(NLS.bind(Messages.ChannelManager_openChannel_failed_message, id, e),
 																0, ITraceIds.TRACE_CHANNEL_MANAGER, IStatus.INFO, this);
 				}
 
 				// Channel opening failed
 				done.doneOpenChannel(e, channel);
 			}
 		} else {
 			// Wait for the channel to be fully opened if still in "OPENING" state
 			if (channel.getState() == IChannel.STATE_OPENING) {
 				final IChannel finChannel = channel;
 				channel.addChannelListener(new IChannel.IChannelListener() {
 
 					@Override
 					public void onChannelOpened() {
 						done.doneOpenChannel(null, finChannel);
 					}
 
 					@Override
 					public void onChannelClosed(Throwable error) {
 						done.doneOpenChannel(error != null ? error : new OperationCanceledException(), finChannel);
 					}
 
 					@Override
 					public void congestionLevel(int level) {
 					}
 				});
 			}
 			else {
 				done.doneOpenChannel(null, channel);
 			}
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.tcf.core.interfaces.IChannelManager#openChannel(java.util.Map, java.util.Map, org.eclipse.tcf.te.tcf.core.interfaces.IChannelManager.DoneOpenChannel)
 	 */
 	@Override
 	public void openChannel(final Map<String, String> peerAttributes, final Map<String, Boolean> flags, final DoneOpenChannel done) {
 		Runnable runnable = new Runnable() {
 			@Override
             public void run() {
 				Assert.isTrue(Protocol.isDispatchThread(), "Illegal Thread Access"); //$NON-NLS-1$
 				internalOpenChannel(peerAttributes, flags, done);
 			}
 		};
 		if (Protocol.isDispatchThread()) runnable.run();
 		else Protocol.invokeLater(runnable);
 	}
 
 	/**
 	 * Internal implementation of {@link #openChannel(Map, org.eclipse.tcf.te.tcf.core.interfaces.IChannelManager.DoneOpenChannel)}.
 	 * <p>
 	 * Method must be called within the TCF dispatch thread.
 	 *
 	 * @param peerAttributes The peer attributes. Must not be <code>null</code>.
 	 * @param flags Map containing the flags to parameterize the channel opening, or <code>null</code>.
 	 * @param done The client callback. Must not be <code>null</code>.
 	 */
 	/* default */ void internalOpenChannel(final Map<String, String> peerAttributes, final Map<String, Boolean> flags, final DoneOpenChannel done) {
 		Assert.isNotNull(peerAttributes);
 		Assert.isNotNull(done);
 		Assert.isTrue(Protocol.isDispatchThread(), "Illegal Thread Access"); //$NON-NLS-1$
 		// Call openChannel(IPeer, ...) instead of calling internalOpenChannel(IPeer, ...) directly
 		// to include the value-add handling.
 		openChannel(getOrCreatePeerInstance(peerAttributes), flags, done);
 	}
 
 	/**
 	 * Tries to find an existing peer instance or create an new {@link IPeer}
 	 * instance if not found.
 	 * <p>
 	 * <b>Note:</b> This method must be invoked at the TCF dispatch thread.
 	 *
 	 * @param peerAttributes The peer attributes. Must not be <code>null</code>.
 	 * @return The peer instance.
 	 */
 	private IPeer getOrCreatePeerInstance(final Map<String, String> peerAttributes) {
 		Assert.isNotNull(peerAttributes);
 		Assert.isTrue(Protocol.isDispatchThread(), "Illegal Thread Access"); //$NON-NLS-1$
 
 		// Get the peer id from the properties
 		String peerId = peerAttributes.get(IPeer.ATTR_ID);
 		Assert.isNotNull(peerId);
 
 		// Check if we shall open the peer transient
 		boolean isTransient = peerAttributes.containsKey("transient") ? Boolean.parseBoolean(peerAttributes.remove("transient")) : false; //$NON-NLS-1$ //$NON-NLS-2$
 
 		// Look the peer via the Locator Service.
 		IPeer peer = Protocol.getLocator().getPeers().get(peerId);
 		// If not peer could be found, create a new one
 		if (peer == null) {
 			peer = isTransient ? new Peer(peerAttributes) : new AbstractPeer(peerAttributes);
 
 			if (CoreBundleActivator.getTraceHandler().isSlotEnabled(0, ITraceIds.TRACE_CHANNEL_MANAGER)) {
 				CoreBundleActivator.getTraceHandler().trace(NLS.bind(Messages.ChannelManager_createPeer_new_message, peerId, Boolean.valueOf(isTransient)),
 															0, ITraceIds.TRACE_CHANNEL_MANAGER, IStatus.INFO, this);
 			}
 		}
 
 		// Return the peer instance
 		return peer;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.tcf.core.interfaces.IChannelManager#getChannel(org.eclipse.tcf.protocol.IPeer)
 	 */
 	@Override
 	public IChannel getChannel(final IPeer peer) {
 		final AtomicReference<IChannel> channel = new AtomicReference<IChannel>();
 		Runnable runnable = new Runnable() {
 			@Override
 			public void run() {
 				Assert.isTrue(Protocol.isDispatchThread(), "Illegal Thread Access"); //$NON-NLS-1$
 				channel.set(internalGetChannel(peer));
 			}
 		};
 		if (Protocol.isDispatchThread()) runnable.run();
 		else Protocol.invokeAndWait(runnable);
 
 	    return channel.get();
 	}
 
 	/**
 	 * Returns the shared channel instance for the given peer.
 	 * <p>
 	 * <b>Note:</b> This method must be invoked at the TCF dispatch thread.
 	 *
 	 * @param peer The peer. Must not be <code>null</code>.
 	 * @return The channel instance or <code>null</code>.
 	 */
 	public IChannel internalGetChannel(IPeer peer) {
 		Assert.isNotNull(peer);
 		Assert.isTrue(Protocol.isDispatchThread(), "Illegal Thread Access"); //$NON-NLS-1$
 
 		// Get the peer id
 		String id = peer.getID();
 
 		// Get the channel
 		IChannel channel = channels.get(id);
 		if (channel != null && !(channel.getState() == IChannel.STATE_OPEN || channel.getState() == IChannel.STATE_OPENING)) {
 			// Channel is not in open state -> drop the instance
 			channel = null;
 			channels.remove(id);
 			refCounters.remove(id);
 		}
 
 		return channel;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.tcf.core.interfaces.IChannelManager#closeChannel(org.eclipse.tcf.protocol.IChannel)
 	 */
 	@Override
 	public void closeChannel(final IChannel channel) {
 		Runnable runnable = new Runnable() {
 			@Override
             public void run() {
 				Assert.isTrue(Protocol.isDispatchThread(), "Illegal Thread Access"); //$NON-NLS-1$
 				internalCloseChannel(channel);
 			}
 		};
 		if (Protocol.isDispatchThread()) runnable.run();
 		else Protocol.invokeLater(runnable);
 	}
 
 	/**
 	 * Closes the given channel.
 	 * <p>
 	 * If the given channel is a reference counted channel, the channel will be closed if the reference counter
 	 * reaches 0. For non reference counted channels, the channel is closed immediately.
 	 * <p>
 	 * <b>Note:</b> This method must be invoked at the TCF dispatch thread.
 	 *
 	 * @param channel The channel. Must not be <code>null</code>.
 	 */
 	/* default */ void internalCloseChannel(IChannel channel) {
 		Assert.isNotNull(channel);
 		Assert.isTrue(Protocol.isDispatchThread(), "Illegal Thread Access"); //$NON-NLS-1$
 
 		// Get the id of the remote peer
 		String id = channel.getRemotePeer().getID();
 
 		if (CoreBundleActivator.getTraceHandler().isSlotEnabled(0, ITraceIds.TRACE_CHANNEL_MANAGER)) {
 			CoreBundleActivator.getTraceHandler().trace(NLS.bind(Messages.ChannelManager_closeChannel_message, id),
 														0, ITraceIds.TRACE_CHANNEL_MANAGER, IStatus.INFO, this);
 		}
 
 		// Get the reference counter
 		AtomicInteger counter = refCounters.get(id);
 
 		// If the counter is null or get 0 after the decrement, close the channel
 		if (counter == null || counter.decrementAndGet() == 0) {
 			channel.close();
 
 			if (CoreBundleActivator.getTraceHandler().isSlotEnabled(0, ITraceIds.TRACE_CHANNEL_MANAGER)) {
 				CoreBundleActivator.getTraceHandler().trace(NLS.bind(Messages.ChannelManager_closeChannel_closed_message, id),
 															0, ITraceIds.TRACE_CHANNEL_MANAGER, IStatus.INFO, this);
 			}
 
 			// Clean the reference counter and the channel map
 			refCounters.remove(id);
 			channels.remove(id);
 		} else {
 			if (CoreBundleActivator.getTraceHandler().isSlotEnabled(0, ITraceIds.TRACE_CHANNEL_MANAGER)) {
 				CoreBundleActivator.getTraceHandler().trace(NLS.bind(Messages.ChannelManager_closeChannel_inuse_message, id, counter.toString()),
 															0, ITraceIds.TRACE_CHANNEL_MANAGER, IStatus.INFO, this);
 			}
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.tcf.core.interfaces.IChannelManager#shutdown(org.eclipse.tcf.protocol.IPeer)
 	 */
 	@Override
 	public void shutdown(final IPeer peer) {
 		Runnable runnable = new Runnable() {
 			@Override
             public void run() {
 				Assert.isTrue(Protocol.isDispatchThread(), "Illegal Thread Access"); //$NON-NLS-1$
 				internalShutdown(peer);
 			}
 		};
 		if (Protocol.isDispatchThread()) runnable.run();
 		else Protocol.invokeLater(runnable);
 	}
 
 	/**
 	 * Shutdown the communication to the given peer, no matter of the current
 	 * reference count. A possible associated value-add is shutdown as well.
 	 *
 	 * @param peer The peer. Must not be <code>null</code>.
 	 */
 	/* default */ void internalShutdown(IPeer peer) {
 		Assert.isTrue(Protocol.isDispatchThread(), "Illegal Thread Access"); //$NON-NLS-1$
 		Assert.isNotNull(peer);
 
 		// Get the peer id
 		String id = peer.getID();
 
 		// Get the channel
 		IChannel channel = internalGetChannel(peer);
 		if (channel != null) {
 			// Reset the reference count (will force a channel close)
 			refCounters.remove(id);
 
 			// Close the channel
 			internalCloseChannel(channel);
 
 			// Get the value-add's for the peer to shutdown
 			IValueAdd[] valueAdds = ValueAddManager.getInstance().getValueAdd(peer);
 			if (valueAdds != null && valueAdds.length > 0) {
 				internalShutdownValueAdds(peer, valueAdds);
 			}
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.tcf.core.interfaces.IChannelManager#closeAll()
 	 */
 	@Override
 	public void closeAll() {
 		Runnable runnable = new Runnable() {
 			@Override
             public void run() {
 				Assert.isTrue(Protocol.isDispatchThread(), "Illegal Thread Access"); //$NON-NLS-1$
 				internalCloseAll();
 			}
 		};
 		if (Protocol.isDispatchThread()) runnable.run();
 		else Protocol.invokeLater(runnable);
 	}
 
 	/**
 	 * Close all open channel, no matter of the current reference count.
 	 * <p>
 	 * <b>Note:</b> This method must be invoked at the TCF dispatch thread.
 	 */
 	/* default */ void internalCloseAll() {
 		Assert.isTrue(Protocol.isDispatchThread(), "Illegal Thread Access"); //$NON-NLS-1$
 
 		IChannel[] openChannels = channels.values().toArray(new IChannel[channels.values().size()]);
 
 		refCounters.clear();
 		channels.clear();
 
 		for (IChannel channel : openChannels) internalCloseChannel(channel);
 
 		internalShutdownAllValueAdds();
 	}
 
 	/**
 	 * Shutdown the given value-adds for the given peer.
 	 *
 	 * @param peer The peer. Must not be <code>null</code>.
 	 * @param valueAdds The list of value-adds. Must not be <code>null</code>.
 	 */
 	/* default */ void internalShutdownValueAdds(final IPeer peer, final IValueAdd[] valueAdds) {
 		Assert.isTrue(Protocol.isDispatchThread(), "Illegal Thread Access"); //$NON-NLS-1$
 		Assert.isNotNull(peer);
 		Assert.isNotNull(valueAdds);
 
 		// Get the peer id
 		final String id = peer.getID();
 
 		if (valueAdds.length > 0) {
 			doShutdownValueAdds(id, valueAdds);
 		}
 	}
 
 	/**
 	 * Shutdown the given value-adds for the given peer id.
 	 *
 	 * @param id The peer id. Must not be <code>null</code>.
 	 * @param valueAdds The list of value-add's. Must not be <code>null</code>.
 	 */
 	/* default */ void doShutdownValueAdds(final String id, final IValueAdd[] valueAdds) {
 		Assert.isTrue(Protocol.isDispatchThread(), "Illegal Thread Access"); //$NON-NLS-1$
 		Assert.isNotNull(id);
 		Assert.isNotNull(valueAdds);
 
 		for (IValueAdd valueAdd : valueAdds) {
 			valueAdd.shutdown(id, new Callback() {
 				@Override
 				protected void internalDone(Object caller, IStatus status) {
 				}
 			});
 		}
 	}
 
 	/**
 	 * Shutdown all value-add's running. Called from {@link #closeAll()}
 	 */
 	/* default */ void internalShutdownAllValueAdds() {
 		Assert.isTrue(Protocol.isDispatchThread(), "Illegal Thread Access"); //$NON-NLS-1$
 
 		// Get all value-add's
 		IValueAdd[] valueAdds = ValueAddManager.getInstance().getValueAdds(false);
 		for (IValueAdd valueAdd : valueAdds) {
 			valueAdd.shutdownAll(new Callback() {
 				@Override
 				protected void internalDone(Object caller, IStatus status) {
 				}
 			});
 		}
 	}
 
 	/**
 	 * Client call back interface for internalHandleValueAdds(...).
 	 */
 	interface DoneHandleValueAdds {
 		/**
 		 * Called when all the value-adds are launched or the launched failed.
 		 *
 		 * @param error The error description if operation failed, <code>null</code> if succeeded.
 		 * @param valueAdds The list of value-adds or <code>null</code>.
 		 */
 		void doneHandleValueAdds(Throwable error, IValueAdd[] valueAdds);
 	}
 
 	/**
 	 * Check on the value-adds for the given peer. Launch the value-adds
 	 * if necessary.
 	 *
 	 * @param peer The peer. Must not be <code>null</code>.
 	 * @param flags Map containing the flags to parameterize the channel opening, or <code>null</code>.
 	 * @param done The client callback. Must not be <code>null</code>.
 	 */
 	/* default */ void internalHandleValueAdds(final IPeer peer, final Map<String, Boolean> flags, final DoneHandleValueAdds done) {
 		Assert.isTrue(Protocol.isDispatchThread(), "Illegal Thread Access"); //$NON-NLS-1$
 		Assert.isNotNull(peer);
 		Assert.isNotNull(done);
 
 		// Get the peer id
 		final String id = peer.getID();
 
 		if (CoreBundleActivator.getTraceHandler().isSlotEnabled(0, ITraceIds.TRACE_CHANNEL_MANAGER)) {
 			CoreBundleActivator.getTraceHandler().trace(NLS.bind(Messages.ChannelManager_openChannel_valueAdd_check, id),
 														0, ITraceIds.TRACE_CHANNEL_MANAGER, IStatus.INFO, this);
 		}
 
 		// Extract the flags of interest form the given flags map
 		boolean forceNew = flags != null && flags.containsKey(IChannelManager.FLAG_FORCE_NEW) ? flags.get(IChannelManager.FLAG_FORCE_NEW).booleanValue() : false;
 		boolean noValueAdd = flags != null && flags.containsKey(IChannelManager.FLAG_NO_VALUE_ADD) ? flags.get(IChannelManager.FLAG_NO_VALUE_ADD).booleanValue() : false;
 		// If noValueAdd == true -> forceNew has to be true as well
 		if (noValueAdd) forceNew = true;
 
 		// Check if there is already a channel opened to this peer
 		IChannel channel = !forceNew ? channels.get(id) : null;
		if (noValueAdd || channel != null && (channel.getState() == IChannel.STATE_OPEN || channel.getState() == IChannel.STATE_OPENING)) {
			// Got an existing channel or a channel without value-add decoration
			// got requested -> drop out immediately
 			done.doneHandleValueAdds(null, null);
 			return;
 		}
 
 		internalHandleValueAdds(peer, done);
 	}
 
 	/* default */ final Map<IPeer, List<DoneHandleValueAdds>> inProgress = new HashMap<IPeer, List<DoneHandleValueAdds>>();
 
 	/**
 	 * Check on the value-adds for the given peer. Launch the value-adds
 	 * if necessary.
 	 *
 	 * @param peer The peer. Must not be <code>null</code>.
 	 * @param done The client callback. Must not be <code>null</code>.
 	 */
 	/* default */ void internalHandleValueAdds(final IPeer peer, final DoneHandleValueAdds done) {
 		Assert.isTrue(Protocol.isDispatchThread(), "Illegal Thread Access"); //$NON-NLS-1$
 		Assert.isNotNull(peer);
 		Assert.isNotNull(done);
 
 		// Get the peer id
 		final String id = peer.getID();
 
 		// If a launch for the same value add is in progress already, attach the new done to
 		// the list to call and drop out
 		if (inProgress.containsKey(peer)) {
 			List<DoneHandleValueAdds> dones = inProgress.get(peer);
 			Assert.isNotNull(dones);
 			dones.add(done);
 			return;
 		}
 
 		// Add the done callback to a list of waiting callbacks per peer
 		List<DoneHandleValueAdds> dones = new ArrayList<DoneHandleValueAdds>();
 		dones.add(done);
 		inProgress.put(peer, dones);
 
 		// The "myDone" callback is invoking the callbacks from the list
 		// of waiting callbacks.
 		final DoneHandleValueAdds myDone = new DoneHandleValueAdds() {
 
 			@Override
 			public void doneHandleValueAdds(Throwable error, IValueAdd[] valueAdds) {
 				// Get the list of the original done callbacks
 				List<DoneHandleValueAdds> dones = inProgress.remove(peer);
 				for (DoneHandleValueAdds done : dones) {
 					done.doneHandleValueAdds(error, valueAdds);
 				}
 			}
 		};
 
 		// Do we have applicable value-add contributions
 		final IValueAdd[] valueAdds = ValueAddManager.getInstance().getValueAdd(peer);
 		if (valueAdds.length == 0) {
 			// There are no applicable value-add's -> drop out immediately
 			if (CoreBundleActivator.getTraceHandler().isSlotEnabled(0, ITraceIds.TRACE_CHANNEL_MANAGER)) {
 				CoreBundleActivator.getTraceHandler().trace(NLS.bind(Messages.ChannelManager_openChannel_valueAdd_noneApplicable, id),
 															0, ITraceIds.TRACE_CHANNEL_MANAGER, IStatus.INFO, this);
 			}
 			myDone.doneHandleValueAdds(null, valueAdds);
 			return;
 		}
 
 		// There are at least applicable value-add contributions
 		if (CoreBundleActivator.getTraceHandler().isSlotEnabled(0, ITraceIds.TRACE_CHANNEL_MANAGER)) {
 			CoreBundleActivator.getTraceHandler().trace(NLS.bind(Messages.ChannelManager_openChannel_valueAdd_numApplicable, Integer.valueOf(valueAdds.length), id),
 														0, ITraceIds.TRACE_CHANNEL_MANAGER, IStatus.INFO, this);
 		}
 
 		final List<IValueAdd> available = new ArrayList<IValueAdd>();
 
 		final DoneLaunchValueAdd innerDone = new DoneLaunchValueAdd() {
 			@Override
 			public void doneLaunchValueAdd(Throwable error, List<IValueAdd> available) {
 				myDone.doneHandleValueAdds(error, available.toArray(new IValueAdd[available.size()]));
 			}
 		};
 
 		doLaunchValueAdd(id, valueAdds, 0, available, innerDone);
 	}
 
 	/**
 	 * Client call back interface for doLaunchValueAdd(...).
 	 */
 	interface DoneLaunchValueAdd {
 		/**
 		 * Called when a value-add has been chained.
 		 *
 		 * @param error The error description if operation failed, <code>null</code> if succeeded.
 		 * @param available The list of available value-adds.
 		 */
 		void doneLaunchValueAdd(Throwable error, List<IValueAdd> available);
 	}
 
 	/**
 	 * Test the value-add at the given index to be alive. Launch the value-add if necessary.
 	 *
 	 * @param id The peer id. Must not be <code>null</code>.
 	 * @param valueAdds The list of value-add's to check. Must not be <code>null</code>.
 	 * @param i The index.
 	 * @param available The list of available value-adds. Must not be <code>null</code>.
 	 * @param done The client callback. Must not be <code>null</code>.
 	 */
 	/* default */ void doLaunchValueAdd(final String id, final IValueAdd[] valueAdds, final int i, final List<IValueAdd> available, final DoneLaunchValueAdd done) {
 		Assert.isTrue(Protocol.isDispatchThread(), "Illegal Thread Access"); //$NON-NLS-1$
 		Assert.isNotNull(id);
 		Assert.isNotNull(valueAdds);
 		Assert.isTrue(valueAdds.length > 0);
 		Assert.isNotNull(available);
 		Assert.isNotNull(done);
 
 		// Get the value-add to launch
 		final IValueAdd valueAdd = valueAdds[i];
 
 		// Check if the value-add to launch is alive
 		valueAdd.isAlive(id, new Callback() {
 			@Override
 			protected void internalDone(Object caller, IStatus status) {
 				boolean alive = ((Boolean)getResult()).booleanValue();
 
 				if (CoreBundleActivator.getTraceHandler().isSlotEnabled(0, ITraceIds.TRACE_CHANNEL_MANAGER)) {
 					CoreBundleActivator.getTraceHandler().trace(NLS.bind(Messages.ChannelManager_openChannel_valueAdd_isAlive, new Object[] { Integer.valueOf(i), valueAdd.getLabel(), Boolean.valueOf(alive), id }),
 																0, ITraceIds.TRACE_CHANNEL_MANAGER, IStatus.INFO, ChannelManager.this);
 				}
 
 				if (!alive) {
 					// Launch the value-add
 					valueAdd.launch(id, new Callback() {
 						@Override
 						protected void internalDone(Object caller, IStatus status) {
 							Throwable error = status.getException();
 
 							if (CoreBundleActivator.getTraceHandler().isSlotEnabled(0, ITraceIds.TRACE_CHANNEL_MANAGER)) {
 								CoreBundleActivator.getTraceHandler().trace(NLS.bind(Messages.ChannelManager_openChannel_valueAdd_launch, new Object[] { Integer.valueOf(i), valueAdd.getLabel(),
 												(error == null ? "success" : "failed"), //$NON-NLS-1$ //$NON-NLS-2$
 												(error != null ? error.getLocalizedMessage() : null),
 												id }),
 												0, ITraceIds.TRACE_CHANNEL_MANAGER, IStatus.INFO, ChannelManager.this);
 							}
 
 							// If we got an error and the value-add is optional,
 							// log the error as warning and drop the value-add
 							if (error != null && valueAdd.isOptional()) {
 								status = new Status(IStatus.WARNING, CoreBundleActivator.getUniqueIdentifier(),
 												NLS.bind(Messages.ChannelManager_valueAdd_launchFailed, valueAdd.getLabel(), error.getLocalizedMessage()),
 												error);
 								Platform.getLog(CoreBundleActivator.getContext().getBundle()).log(status);
 
 								// Reset the error
 								error = null;
 							} else {
 								available.add(valueAdd);
 							}
 
 							// If the value-add failed to launch, no other value-add's are launched
 							if (error != null) {
 								done.doneLaunchValueAdd(error, available);
 							} else {
 								// Launch the next one, if there is any
 								if (i + 1 < valueAdds.length) {
 									DoneLaunchValueAdd innerDone = new DoneLaunchValueAdd() {
 										@Override
 										public void doneLaunchValueAdd(Throwable error, List<IValueAdd> available) {
 											done.doneLaunchValueAdd(error, available);
 										}
 									};
 									doLaunchValueAdd(id, valueAdds, i + 1, available, innerDone);
 								} else {
 									// Last value-add in chain launched -> call parent callback
 									done.doneLaunchValueAdd(null, available);
 								}
 							}
 						}
 					});
 				} else {
 					// Already alive -> add it to the list of available value-add's
 					available.add(valueAdd);
 					// Launch the next one, if there is any
 					if (i + 1 < valueAdds.length) {
 						DoneLaunchValueAdd innerDone = new DoneLaunchValueAdd() {
 							@Override
 							public void doneLaunchValueAdd(Throwable error, List<IValueAdd> available) {
 								done.doneLaunchValueAdd(error, available);
 							}
 						};
 						doLaunchValueAdd(id, valueAdds, i + 1, available, innerDone);
 					} else {
 						// Last value-add in chain launched -> call parent callback
 						done.doneLaunchValueAdd(null, available);
 					}
 				}
 			}
 		});
 	}
 
 	/**
 	 * Client call back interface for doChainValueAdd(...).
 	 */
 	interface DoneChainValueAdd {
 		/**
 		 * Called when a value-add has been chained.
 		 *
 		 * @param error The error description if operation failed, <code>null</code> if succeeded.
 		 * @param channel The channel object or <code>null</code>.
 		 */
 		void doneChainValueAdd(Throwable error, IChannel channel);
 	}
 
 	/**
 	 * Chain the value-adds until the original target peer is reached.
 	 *
 	 * @param valueAdds The list of value-add's to chain. Must not be <code>null</code>.
 	 * @param peer The original target peer. Must not be <code>null</code>.
 	 * @param flags Map containing the flags to parameterize the channel opening, or <code>null</code>.
 	 * @param done The client callback. Must not be <code>null</code>.
 	 */
 	/* default */ void internalChainValueAdds(final IValueAdd[] valueAdds, final IPeer peer, final Map<String, Boolean> flags, final DoneOpenChannel done) {
 		Assert.isTrue(Protocol.isDispatchThread(), "Illegal Thread Access"); //$NON-NLS-1$
 		Assert.isNotNull(valueAdds);
 		Assert.isNotNull(peer);
 		Assert.isNotNull(done);
 
 		// Get the peer id
 		final String id = peer.getID();
 
 		if (CoreBundleActivator.getTraceHandler().isSlotEnabled(0, ITraceIds.TRACE_CHANNEL_MANAGER)) {
 			CoreBundleActivator.getTraceHandler().trace(NLS.bind(Messages.ChannelManager_openChannel_valueAdd_startChaining, id),
 														0, ITraceIds.TRACE_CHANNEL_MANAGER, IStatus.INFO, this);
 		}
 
 		// Extract the flags of interest form the given flags map
 		boolean forceNew = flags != null && flags.containsKey(IChannelManager.FLAG_FORCE_NEW) ? flags.get(IChannelManager.FLAG_FORCE_NEW).booleanValue() : false;
 		boolean noValueAdd = flags != null && flags.containsKey(IChannelManager.FLAG_NO_VALUE_ADD) ? flags.get(IChannelManager.FLAG_NO_VALUE_ADD).booleanValue() : false;
 		// If noValueAdd == true -> forceNew has to be true as well
 		if (noValueAdd) forceNew = true;
 
 		// Check if there is already a channel opened to this peer
 		IChannel channel = !forceNew ? channels.get(id) : null;
 		if (channel != null && (channel.getState() == IChannel.STATE_OPEN || channel.getState() == IChannel.STATE_OPENING)) {
 			// Increase the reference count
 			AtomicInteger counter = refCounters.get(id);
 			if (counter == null) {
 				counter = new AtomicInteger(0);
 				refCounters.put(id, counter);
 			}
 			counter.incrementAndGet();
 
 			if (CoreBundleActivator.getTraceHandler().isSlotEnabled(0, ITraceIds.TRACE_CHANNEL_MANAGER)) {
 				CoreBundleActivator.getTraceHandler().trace(NLS.bind(Messages.ChannelManager_openChannel_reuse_message, id, counter.toString()),
 															0, ITraceIds.TRACE_CHANNEL_MANAGER, IStatus.INFO, this);
 			}
 			// Got an existing channel -> drop out immediately if the channel is
 			// already fully opened. Otherwise wait for the channel to be fully open.
 			if (channel.getState() == IChannel.STATE_OPENING) {
 				final IChannel finChannel = channel;
 				channel.addChannelListener(new IChannel.IChannelListener() {
 					@Override
 					public void onChannelOpened() {
 						done.doneOpenChannel(null, finChannel);
 					}
 					@Override
 					public void onChannelClosed(Throwable error) {
 						done.doneOpenChannel(error != null ? error : new OperationCanceledException(), finChannel);
 					}
 					@Override
 					public void congestionLevel(int level) {
 					}
 				});
 			}
 			else {
 				done.doneOpenChannel(null, channel);
 			}
 			return;
 		} else if (channel != null) {
 			// Channel is not in open state -> drop the instance
 			channels.remove(id);
 			refCounters.remove(id);
 		}
 
 		// No existing channel -> open a new one
 		final DoneChainValueAdd innerDone = new DoneChainValueAdd() {
 			@Override
 			public void doneChainValueAdd(Throwable error, IChannel channel) {
 				done.doneOpenChannel(error, channel);
 			}
 		};
 
 		doChainValueAdd(id, peer.getAttributes(), forceNew, valueAdds, innerDone);
 	}
 
 	/* default */ void doChainValueAdd(final String id, final Map<String, String> attrs, final boolean forceNew, final IValueAdd[] valueAdds, final DoneChainValueAdd done) {
 		Assert.isTrue(Protocol.isDispatchThread(), "Illegal Thread Access"); //$NON-NLS-1$
 		Assert.isNotNull(id);
 		Assert.isNotNull(attrs);
 		Assert.isNotNull(valueAdds);
 		Assert.isNotNull(done);
 
 		// The index of the currently processed value-add
 		final AtomicInteger index = new AtomicInteger(0);
 
 		// Get the value-add to chain
 		final AtomicReference<IValueAdd> valueAdd = new AtomicReference<IValueAdd>();
 		valueAdd.set(valueAdds[index.get()]);
 		Assert.isNotNull(valueAdd.get());
 		// Get the next value-add in chain
 		final AtomicReference<IValueAdd> nextValueAdd = new AtomicReference<IValueAdd>();
 		nextValueAdd.set(index.get() + 1 < valueAdds.length ? valueAdds[index.get() + 1] : null);
 
 		// Get the peer for the value-add to chain
 		final AtomicReference<IPeer> valueAddPeer = new AtomicReference<IPeer>();
 		valueAddPeer.set(valueAdd.get().getPeer(id));
 		if (valueAddPeer.get() == null) {
 			done.doneChainValueAdd(new IllegalStateException("Invalid value-add peer."), null); //$NON-NLS-1$
 			return;
 		}
 
 		// Get the peer for the next value-add in chain
 		final AtomicReference<IPeer> nextValueAddPeer = new AtomicReference<IPeer>();
 		nextValueAddPeer.set(nextValueAdd.get() != null ? nextValueAdd.get().getPeer(id) : null);
 		if (nextValueAdd.get() != null && nextValueAddPeer.get() == null) {
 			done.doneChainValueAdd(new IllegalStateException("Invalid value-add peer."), null); //$NON-NLS-1$
 			return;
 		}
 
 		IChannel channel = null;
 		try {
 			// Open a channel to the value-add
 			channel = valueAddPeer.get().openChannel();
 			if (channel != null) {
 				if (!forceNew) channels.put(id, channel);
 				if (!forceNew) refCounters.put(id, new AtomicInteger(1));
 
 				// Redirect the channel to the next value-add in chain
 				// Note: If the redirect succeeds, channel.getRemotePeer().getID() will be identical to id.
 				channel.redirect(nextValueAddPeer.get() != null ? nextValueAddPeer.get().getAttributes() : attrs);
 				// Attach the channel listener to catch open/closed events
 				final IChannel finChannel = channel;
 				channel.addChannelListener(new IChannel.IChannelListener() {
 					@Override
 					public void onChannelOpened() {
 						if (CoreBundleActivator.getTraceHandler().isSlotEnabled(0, ITraceIds.TRACE_CHANNEL_MANAGER)) {
 							CoreBundleActivator.getTraceHandler().trace(NLS.bind(Messages.ChannelManager_openChannel_valueAdd_redirect_succeeded,
 																				 new Object[] { valueAddPeer.get().getID(), finChannel.getRemotePeer().getID(), Integer.valueOf(index.get()) }),
 																		0, ITraceIds.TRACE_CHANNEL_MANAGER, IStatus.INFO, ChannelManager.this);
 						}
 
 						// Channel opened. Check if we are done.
 						if (nextValueAdd.get() == null) {
 							// Remove ourself as channel listener
 							finChannel.removeChannelListener(this);
 
 							// No other value-add in the chain -> all done
 							done.doneChainValueAdd(null, finChannel);
 						} else {
 							// Process the next value-add in chain
 							index.incrementAndGet();
 
 							// Update the value-add references
 							valueAdd.set(nextValueAdd.get());
 							valueAddPeer.set(nextValueAddPeer.get());
 
 							if (valueAddPeer.get() == null) {
 								// Remove ourself as channel listener
 								finChannel.removeChannelListener(this);
 								// Close the channel
 								finChannel.close();
 								// Invoke the callback
 								done.doneChainValueAdd(new IllegalStateException("Invalid value-add peer."), null); //$NON-NLS-1$
 								return;
 							}
 
 							nextValueAdd.set(index.get() + 1 < valueAdds.length ? valueAdds[index.get() + 1] : null);
 							nextValueAddPeer.set(nextValueAdd.get() != null ? nextValueAdd.get().getPeer(id) : null);
 							if (nextValueAdd.get() != null && nextValueAddPeer.get() == null) {
 								// Remove ourself as channel listener
 								finChannel.removeChannelListener(this);
 								// Close the channel
 								finChannel.close();
 								// Invoke the callback
 								done.doneChainValueAdd(new IllegalStateException("Invalid value-add peer."), null); //$NON-NLS-1$
 								return;
 							}
 
 							// Redirect the channel to the next value-add in chain
 							finChannel.redirect(nextValueAddPeer.get() != null ? nextValueAddPeer.get().getAttributes() : attrs);
 						}
 					}
 
 					@Override
 					public void onChannelClosed(Throwable error) {
 						// Remove ourself as channel listener
 						finChannel.removeChannelListener(this);
 
 						if (CoreBundleActivator.getTraceHandler().isSlotEnabled(0, ITraceIds.TRACE_CHANNEL_MANAGER)) {
 							CoreBundleActivator.getTraceHandler().trace(NLS.bind(Messages.ChannelManager_openChannel_valueAdd_redirect_failed, valueAddPeer.get().getID(),
 																				 nextValueAddPeer.get() != null ? nextValueAddPeer.get().getID() : id),
 																		0, ITraceIds.TRACE_CHANNEL_MANAGER, IStatus.INFO, ChannelManager.this);
 						}
 
 						// Clean the reference counter and the channel map
 						channels.remove(id);
 						refCounters.remove(id);
 						// Channel opening failed -> This will break everything
 						done.doneChainValueAdd(error, finChannel);
 					}
 
 					@Override
 					public void congestionLevel(int level) {
 					}
 				});
 			} else {
 				// Channel is null? Something went terrible wrong.
 				done.doneChainValueAdd(new Exception("Unexpected null return value from IPeer#openChannel()!"), null); //$NON-NLS-1$
 
 			}
 		} catch (Throwable e) {
 			if (CoreBundleActivator.getTraceHandler().isSlotEnabled(0, ITraceIds.TRACE_CHANNEL_MANAGER)) {
 				CoreBundleActivator.getTraceHandler().trace(NLS.bind(Messages.ChannelManager_openChannel_failed_message, id, e),
 															0, ITraceIds.TRACE_CHANNEL_MANAGER, IStatus.INFO, this);
 			}
 
 			// Channel opening failed
 			done.doneChainValueAdd(e, channel);
 		}
 	}
 }
