 /**
  * 
  */
 package ecologylab.oodss.distributed.impl;
 
 import java.io.IOException;
 import java.nio.channels.CancelledKeyException;
 import java.nio.channels.SelectionKey;
 import java.nio.channels.Selector;
 import java.nio.channels.SocketChannel;
 import java.util.Iterator;
 import java.util.Queue;
 import java.util.concurrent.ConcurrentLinkedQueue;
 
 import ecologylab.generic.Debug;
 import ecologylab.generic.ResourcePool;
 import ecologylab.generic.StartAndStoppable;
 import ecologylab.oodss.distributed.common.NetworkingConstants;
 import ecologylab.oodss.exceptions.BadClientException;
 import ecologylab.oodss.exceptions.ClientOfflineException;
 
 /**
  * Provides core functionality for NIO-based servers or clients. This class is Runnable and
  * StartAndStoppable; it's run method automatically handles interest-switching on a selector's keys,
  * as well as calling appropriate abstract methods whenever interest ops are selected.
  * 
  * Subclasses are required to configure their own selector.
  * 
  * @author Zachary O. Toups (toupsz@cs.tamu.edu)
  * 
  */
 public abstract class NIOCore extends Debug implements StartAndStoppable, NetworkingConstants
 {
 	private Queue<SocketModeChangeRequest>	pendingSelectionOpChanges	= new ConcurrentLinkedQueue<SocketModeChangeRequest>();
 
 	protected Selector											selector;
 
 	private String													networkingIdentifier			= "NIOCore";
 
 	private volatile boolean								running;
 
 	private Thread													thread;
 
 	protected int														portNumber;
 
 	private SocketModeChangeRequestPool			mReqPool									= new SocketModeChangeRequestPool(
 																																				20, 10);
 
 	/**
 	 * Instantiates a new NIOCore object.
 	 * 
 	 * @param networkingIdentifier
 	 *          the name to identify this object when its thread is created.
 	 * @param portNumber
 	 *          the port number that this object will use for network communications.
 	 * @throws IOException
 	 *           if an I/O error occurs while trying to open a Selector from the system.
 	 */
 	protected NIOCore(String networkingIdentifier, int portNumber) throws IOException
 	{
 		this.networkingIdentifier = networkingIdentifier;
 		this.portNumber = portNumber;
 	}
 
 	/**
 	 * THIS METHOD SHOULD NOT BE CALLED DIRECTLY!
 	 * 
 	 * Proper use of this method is through the start / stop methods.
 	 * 
 	 * Main run method. Performs a loop of changing the mode (read/write) for each socket, if
 	 * requested, then checks for and performs appropriate I/O for each socket that is ready. Ends
 	 * when running is set to false (through the stop method).
 	 * 
 	 * @see java.lang.Runnable#run()
 	 */
 	public final void run()
 	{
 		while (running)
 		{
 			// update pending selection operation changes
 			synchronized (this.pendingSelectionOpChanges)
 			{
 				for (SocketModeChangeRequest changeReq : pendingSelectionOpChanges)
 				{
 					if (changeReq.key.channel().isRegistered())
 					{
 						/*
 						 * Perform any changes to the interest ops on the keys, before selecting.
 						 */
 						switch (changeReq.type)
 						{
 						case CHANGEOPS:
 							try
 							{
 								changeReq.key.interestOps(changeReq.ops);
 							}
 							catch (CancelledKeyException e)
 							{
 								debug("tried to change ops after key was cancelled.");
 							}
 							catch (IllegalArgumentException e1)
 							{
 								debug("illegal argument for interestOps: " + changeReq.ops);
 							}
 							break;
 						case INVALIDATE_PERMANENTLY:
 							debug(">>>>>>>>>>>>>>>> invalidating permanently: " + changeReq.key.attachment());
 							invalidateKey(changeReq.key, true);
 							break;
 						case INVALIDATE_TEMPORARILY:
 							debug(">>>>>>>>>>>>>>>> invalidating temporarily: " + changeReq.key.attachment());
 							invalidateKey(changeReq.key, false);
 							break;
 						}
 					}
 
 					// release the SocketModeChangeRequest when done
 					changeReq = this.mReqPool.release(changeReq);
 				}
 
 				this.pendingSelectionOpChanges.clear();
 			}
 
 			// check selection operations
 			try
 			{
 				if (selector.select() > 0)
 				{
 					/*
 					 * get an iterator of the keys that have something to do we have to do it this way,
 					 * because we have to be able to call remove() which will not work in a foreach loop
 					 */
 					Iterator<SelectionKey> selectedKeyIter = selector.selectedKeys().iterator();
 
 					while (selectedKeyIter.hasNext())
 					{
 						/*
 						 * get the key corresponding to the event and process it appropriately, then remove it
 						 */
 						SelectionKey key = selectedKeyIter.next();
 
 						selectedKeyIter.remove();
 
 						if (!key.isValid())
 						{
 							debug("invalid key");
 							setPendingInvalidate(key, false);
 						}
 						else if (key.isReadable())
 						{
 							/*
 							 * incoming readable, valid key; have to double-check validity here, because accept
 							 * key may have rejected an incoming connection
 							 */
 							if (key.channel().isOpen() && key.isValid())
 							{
 								try
 								{
 									readReady(key);
 									readFinished(key);
 								}
 								catch (ClientOfflineException e)
 								{
 									warning(e.getMessage());
 									setPendingInvalidate(key, false);
 								}
 								catch (BadClientException e)
 								{
 									// close down this evil connection!
 									error(e.getMessage());
 									this.removeBadConnections(key);
 								}
 							}
 							else
 							{
 								debug("Channel closed on " + key.attachment() + ", removing.");
 								invalidateKey(key, false);
 							}
 						}
 						else if (key.isWritable())
 						{
 							try
 							{
 								writeReady(key);
 								writeFinished(key);
 							}
 							catch (IOException e)
 							{
 								debug("IO error when attempting to write to socket; stack trace follows.");
 
 								e.printStackTrace();
 							}
 
 						}
 						else if (key.isAcceptable())
 						{ // incoming connection; accept
 							this.acceptReady(key);
 							this.acceptFinished(key);
 						}
 						else if (key.isConnectable())
 						{
 							this.connectReady(key);
 							this.connectFinished(key);
 						}
 					}
 				}
 			}
 			catch (IOException e)
 			{
 				this.stop();
 
 				debug("attempted to access selector after it was closed! shutting down");
 
 				e.printStackTrace();
 			}
 
 			// remove any that were idle for too long
 			this.checkAndDropIdleKeys();
 		}
 
 		this.close();
 	}
 
 	public void setPriority(int priority)
 	{
 		Thread thread = this.thread;
 		if (thread != null)
 		{
 			thread.setPriority(priority);
 		}
 	}
 
 	/**
 	 * @param key
 	 */
 	protected abstract void acceptReady(SelectionKey key);
 
 	/**
 	 * @param key
 	 */
 	protected abstract void connectReady(SelectionKey key);
 
 	/**
 	 * @param key
 	 */
 	protected abstract void readFinished(SelectionKey key);
 
 	/**
 	 * @param key
 	 */
 	protected abstract void readReady(SelectionKey key) throws ClientOfflineException,
 			BadClientException;
 
 	/**
 	 * Queues a request to change key's interest operations back to READ.
 	 * 
 	 * This method is automatically called after acceptReady(SelectionKey) in the main operating loop.
 	 * 
 	 * @param key
 	 */
 	public abstract void acceptFinished(SelectionKey key);
 
 	/**
 	 * Queues a request to change key's interest operations back to READ.
 	 * 
 	 * This method is automatically called after connectReady(SelectionKey) in the main operating
 	 * loop.
 	 * 
 	 * @param key
 	 */
 	public void connectFinished(SelectionKey key)
 	{
 		this.queueForRead(key);
 
 		selector.wakeup();
 	}
 
 	/**
 	 * Queues a request to change key's interest operations back to READ.
 	 * 
 	 * This method is automatically called after writeReady(SelectionKey) in the main operating loop.
 	 * 
 	 * Perform any actions necessary after all data has been written from the outgoing queue to the
 	 * client for this key. This is a hook method so that subclasses can provide specific
 	 * functionality (such as, for example, invalidating the connection once the data has been sent.
 	 * 
 	 * @param key
 	 *          - the SelectionKey that is finished writing.
 	 */
 	protected void writeFinished(SelectionKey key)
 	{
 		this.queueForRead(key);
 
 		selector.wakeup();
 	}
 
 	protected abstract void removeBadConnections(SelectionKey key);
 
 	/**
 	 * Sets up a pending invalidate command for the given input.
 	 * 
 	 * @param key
 	 *          the key to invalidate
 	 * @param forcePermanent
 	 *          ignore any settings for the client and invalidate permanently no matter what
 	 */
 	public void setPendingInvalidate(SelectionKey key, boolean forcePermanent)
 	{
 		// allow subclass processing of the key being invalidated, and find out if
 		// it should be permanent
 		boolean permanent = this.handleInvalidate(key, forcePermanent);
 
 		SocketModeChangeRequest req = this.mReqPool.acquire();
 		req.key = key;
 		req.type = ((forcePermanent ? true : permanent) ? SocketModeChangeRequestType.INVALIDATE_PERMANENTLY
 				: SocketModeChangeRequestType.INVALIDATE_TEMPORARILY);
 
 		synchronized (pendingSelectionOpChanges)
 		{
 			this.pendingSelectionOpChanges.offer(req);
 		}
 
 		selector.wakeup();
 	}
 
 	/**
 	 * Checks the key to see what type of invalidation it should be (permanent, or temporary) and
 	 * handles any housecleaning associated with invalidating that key.
 	 * 
 	 * @param key
 	 *          - the key
 	 * @param forcePermanent
 	 * @return true if the key should be invalidated permanently, or false otherwise
 	 */
 	protected abstract boolean handleInvalidate(SelectionKey key, boolean forcePermanent);
 
 	/**
 	 * Shut down the connection associated with this SelectionKey. Subclasses should override to do
 	 * your own housekeeping, then call super.invalidateKey(SelectionKey) to utilize the functionality
 	 * here.
 	 * 
 	 * @param chan
 	 *          The SocketChannel that needs to be shut down.
 	 */
 	protected void invalidateKey(SocketChannel chan)
 	{
 		try
 		{
 			chan.close();
 		}
 		catch (IOException e)
 		{
 			debug(e.getMessage());
 		}
 		catch (NullPointerException e)
 		{
 			debug(e.getMessage());
 		}
 
 		if (chan.keyFor(selector) != null)
 		{ /*
 			 * it's possible that they key was somehow disposed of already, perhaps it was already
 			 * invalidated once
 			 */
 			chan.keyFor(selector).cancel();
 		}
 	}
 
 	/**
 	 * @see ecologylab.oodss.distributed.impl.NIONetworking#invalidateKey(java.nio.channels.SocketChannel,
 	 *      boolean)
 	 */
 	protected abstract void invalidateKey(SelectionKey key, boolean permanent);
 
 	public void start()
 	{
 		// start the server running
 		running = true;
 
 		if (thread == null)
 		{
 			thread = new Thread(this, networkingIdentifier + " running on port " + portNumber);
 			synchronized (thread)
 			{
 				thread.start();
 			}
 		}
 
 		
 	}
 
 	protected void openSelector() throws IOException
 	{
 		selector = Selector.open();
 	}
 
 	public synchronized void stop()
 	{
 		running = false;
 
 		try
 		{
 			this.selector.wakeup();
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 		}
 		this.close();
 
 		if (thread != null)
 		{
 			synchronized (thread)
 			{ // we cannot re-use the Thread object.
 				thread = null;
 			}
 		}
 	}
 
 	protected void close()
 	{
 	}
 
 	/**
 	 * Check for timeout on all allocated keys; deallocate those that are hanging around, but no
 	 * longer in use.
 	 */
 	protected abstract void checkAndDropIdleKeys();
 
 	/**
 	 * @param key
 	 */
 	protected abstract void writeReady(SelectionKey key) throws IOException;
 
 	protected void queueForAccept(SelectionKey key)
 	{
 		SocketModeChangeRequest req = this.mReqPool.acquire();
 		req.key = key;
 		req.type = SocketModeChangeRequestType.CHANGEOPS;
 		req.ops = SelectionKey.OP_ACCEPT;
 
 		synchronized (this.pendingSelectionOpChanges)
 		{
 			// queue the socket channel for writing
 			this.pendingSelectionOpChanges.offer(req);
 		}
 	}
 
 	protected void queueForConnect(SelectionKey key)
 	{
 		SocketModeChangeRequest req = this.mReqPool.acquire();
 		req.key = key;
 		req.type = SocketModeChangeRequestType.CHANGEOPS;
 		req.ops = SelectionKey.OP_CONNECT;
 
 		synchronized (this.pendingSelectionOpChanges)
 		{
 			// queue the socket channel for writing
 			this.pendingSelectionOpChanges.offer(req);
 		}
 	}
 
 	protected void queueForRead(SelectionKey key)
 	{
 		SocketModeChangeRequest req = this.mReqPool.acquire();
 		req.key = key;
 		req.type = SocketModeChangeRequestType.CHANGEOPS;
 		req.ops = SelectionKey.OP_READ;
 
 		synchronized (this.pendingSelectionOpChanges)
 		{
 			// queue the socket channel for writing
 			this.pendingSelectionOpChanges.offer(req);
 		}
 	}
 
 	protected void queueForWrite(SelectionKey key)
 	{
 		SocketModeChangeRequest req = this.mReqPool.acquire();
 		req.key = key;
 		req.type = SocketModeChangeRequestType.CHANGEOPS;
 		req.ops = SelectionKey.OP_WRITE;
 
 		synchronized (this.pendingSelectionOpChanges)
 		{
 			// queue the socket channel for writing
 			this.pendingSelectionOpChanges.offer(req);
 		}
 	}
 
 	/**
 	 * @return the port number the server is listening on.
 	 */
 	public int getPortNumber()
 	{
 		return portNumber;
 	}
 
 	protected enum SocketModeChangeRequestType
 	{
 		/**
 		 * Indicates that the socket mode should not actually be changed; only used for fresh
 		 * SocketModeChangeRequests that have not yet been specified.
 		 */
 		NONE,
 
 		/**
 		 * Indicates that the socket mode should change it's interest ops to those specified by the
 		 * SocketModeChangeRequest.
 		 */
 		CHANGEOPS,
 
 		/**
 		 * Indicates that the socket should be permanently invalidated and it's matching client manager
 		 * should be destroyed. This should only happen when clients purposefully disconnect or when
 		 * they have been banned. Some special servers (such as HTTP servers) may also invalidate
 		 * permanently when the socket disconnects.
 		 */
 		INVALIDATE_PERMANENTLY,
 
 		/**
 		 * Indicates that the socket should be temporarily disconnected. This results from an unexpected
 		 * disconnect by the client. The result is that the matching client manager should be retained
 		 * for some period of time, so that the client can reconnect if desired.
 		 */
 		INVALIDATE_TEMPORARILY
 	}
 
 	/**
 	 * A signaling object for modifying interest ops and socket invalidation in a thread-safe way.
 	 * 
 	 * @author James Greenfield
 	 */
 	class SocketModeChangeRequest
 	{
 		public SelectionKey									key;
 
 		public SocketModeChangeRequestType	type;
 
 		public int													ops;
 
 		public SocketModeChangeRequest(SelectionKey key, SocketModeChangeRequestType type, int ops)
 		{
 			this.key = key;
 			this.type = type;
 			this.ops = ops;
 		}
 	}
 
 	/**
 	 * A resource pool that handles socket mode change requests to prevent unnecessary instantiations.
 	 * 
 	 * @author Zachary O. Toups (toupsz@cs.tamu.edu)
 	 */
 	class SocketModeChangeRequestPool extends ResourcePool<SocketModeChangeRequest>
 	{
 		/**
 		 * @param initialPoolSize
 		 * @param minimumPoolSize
 		 */
 		public SocketModeChangeRequestPool(int initialPoolSize, int minimumPoolSize)
 		{
 			super(initialPoolSize, minimumPoolSize);
 		}
 
 		/**
 		 * @see ecologylab.generic.ResourcePool#clean(java.lang.Object)
 		 */
 		@Override
 		protected void clean(SocketModeChangeRequest objectToClean)
 		{
 			objectToClean.key = null;
 			objectToClean.ops = 0;
 			objectToClean.type = SocketModeChangeRequestType.NONE;
 		}
 
 		/**
 		 * @see ecologylab.generic.ResourcePool#generateNewResource()
 		 */
 		@Override
 		protected SocketModeChangeRequest generateNewResource()
 		{
 			return new SocketModeChangeRequest(null, SocketModeChangeRequestType.NONE, 0);
 		}
 	}
 }
