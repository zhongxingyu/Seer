 package org.andnav.osm.tileprovider;
 
 import gnu.trove.map.hash.TLongObjectHashMap;
 
 import java.io.InputStream;
 import java.util.concurrent.ExecutorService;
 
 import org.andnav.osm.tileprovider.constants.OpenStreetMapTileProviderConstants;
 import org.andnav.osm.views.util.IOpenStreetMapRendererInfo;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import android.graphics.drawable.Drawable;
 
 /**
  * An abstract child class of {@link OpenStreetMapTileProvider} which acquires tile images
  * asynchronously from some network source.
  * The key unimplemented methods are 'threadGroupname' and 'getTileLoader'.
  */
 public abstract class OpenStreetMapAsyncTileProvider implements OpenStreetMapTileProviderConstants {
 
 	
 	protected ExecutorService getExecutorService()
 	{
 		return getOSMThreadFactory().mExeServicenew;
 	}
 	/**
 	 * 
 	 * @return
 	 */
 	protected String threadGroupName()
 	{
 		return getOSMThreadFactory().mThreadPool.getName();
 	};
 	
 	protected abstract OSMThreadFactory getOSMThreadFactory();
 
 	/**
 	 * It is expected that the implementation will construct an internal member which
 	 * internally implements a {@link TileLoader}.  This method is expected to return
 	 * a that internal member to methods of the parent methods.
 	 *  
 	 * @return the internal member of this tile provider.
 	 */
 	protected abstract Runnable getTileLoader();
 
 	protected static final Logger logger = LoggerFactory.getLogger(OpenStreetMapAsyncTileProvider.class);
 
 	protected final ThreadGroup mThreadPool = new ThreadGroup(threadGroupName());
 	// protected final ConcurrentHashMap<OpenStreetMapTile, Object> mWorking;
 	protected final TLongObjectHashMap<IOpenStreetMapRendererInfo> mWorking;
 	// protected final LinkedHashMap<OpenStreetMapTile, Object> mPending;
 	// protected final LongObjectLRUCache<IOpenStreetMapRendererInfo> mPending;
 	protected final LongObjectLRUCache<IOpenStreetMapRendererInfo> mWaiting;
 	
 	
 	protected static final Object PRESENT = new Object();
 
 	protected final IOpenStreetMapTileProviderCallback mCallback;
 
 	public OpenStreetMapAsyncTileProvider(final IOpenStreetMapTileProviderCallback pCallback, final int aThreadPoolSize, final int aPendingQueueSize) 
 	{
 		mCallback = pCallback;
 		mWorking = new TLongObjectHashMap<IOpenStreetMapRendererInfo>();
 
 		// mPending = new LongObjectLRUCache<IOpenStreetMapRendererInfo>(aPendingQueueSize); 
 		mWaiting = new LongObjectLRUCache<IOpenStreetMapRendererInfo>(aPendingQueueSize); 
 
 	}
 
 	public void loadMapTileAsync(final OpenStreetMapTile aTile) {
 
 		final int activeCount = getOSMThreadFactory().numberActive();
 
 			// sanity check
 			if (activeCount == 0 && !mWaiting.isEmpty()) {
 				logger.warn("Unexpected - no active threads but pending queue not empty");
 			}
 
 			// if we are alredy working on this forget.
 			synchronized(mWorking)
 			{
 				if (mWorking.containsKey(aTile.getTileId()))
 				{
 					return;
 				}
 			}
 			// this will put the tile in the queue, or move it to the front of
 			// the queue if it's already present
 
 			mWaiting.put(aTile.getTileId(), aTile.getRenderer());
 
 		if (DEBUGMODE)
 			logger.debug(activeCount + " active threads");
 
 		ExecutorService ex = this.getExecutorService();
 		ex.execute(getTileLoader());
 
 //		if (activeCount < mThreadPoolSize) {
 //			final Thread t = new Thread(mThreadPool, getTileLoader());
 //			t.start();
 //		}
 	}
 
 	private void clearQueue() {
 //		mPending.clear();
 		mWorking.clear();
 		mWaiting.clear();
 	}
 
 	/**
 	 * Stops all workers - we're shutting down.
 	 */
 	public void stopWorkers()
 	{
 		this.clearQueue();
 		getOSMThreadFactory().mThreadPool.interrupt();
 	}
 
 	/**
 	 * Load the requested tile.
      * An abstract internal class whose objects are used by worker threads to acquire tiles from servers.
      * It processes tiles from the 'pending' set to the 'working' set as they become available.
      * The key unimplemented method is 'loadTile'.
 	 * 
 	 * @param aTile the tile to load
 	 * @throws CantContinueException if it is not possible to continue with processing the queue
 	 * @throws CloudmadeException if there's an error authorizing for Cloudmade tiles
 	 */
 	protected abstract class TileLoader implements Runnable {
 		
 		/**
 		 * The key unimplemented method.
 		 * 
 		 * @param tileId
 		 * @throws CantContinueException
 		 */
 		protected abstract void loadTile(long tileId, IOpenStreetMapRendererInfo renderer) throws CantContinueException;
 
 		private long nextTile() 
 		{
 
 			long result = -1;
 			boolean found = false;
 
 			// get the most recently accessed tile
 			// - the last item in the iterator that's not already being processed
 //			TLongIterator iterator = mPending.keySet().iterator();
 //
 //			// TODO this iterates the whole list, make this faster...
 //			try 
 //			{
 //				while (iterator.hasNext()) 
 //				{
 //					final long tileId = iterator.next();
 //					if (!mWorking.containsKey(tileId)) 
 //					{
 //						result = tileId;
 //						found = true;
 //					}
 //				}
 //			} 
 //			catch (final ConcurrentModificationException e) 
 //			{
 //				if (DEBUGMODE)
 //					logger.warn("ConcurrentModificationException break: " + (found));
 //			}
 //			if (!mWaiting.checkQueue())
 //			{
 //				logger.debug("Corrupt already");
 //			}
 
 			IOpenStreetMapRendererInfo obj =  null;
 
 			
 			synchronized(mWaiting)
 			{
 				result = mWaiting.getLastKey();
 				if (result != -1)
 				{
 					obj =  mWaiting.get(result);
 					mWaiting.remove(result);
 					found = true;
 					
 					synchronized(mWorking)
 					{
 						mWorking.put(result, obj);
 					}
 				}
 			}
 		
 //			if (!found)
 //			{
 //				// need to do some checking see if this is working.
 //				if (DEBUGMODE)
 //				{
 //					if (!mPending.isEmpty())
 //					{
 //						logger.info("Returned null with " + mPending.size() + " in queue");
 //					}
 //				}
 //			}
 			
 			return result;
 		}
 
 		/**
 		 * A tile has loaded.
 		 * @param aTile the tile that has loaded
 		 * @param aTilePath the path of the file.
 		 */
 		public void tileLoaded(final OpenStreetMapTile aTile, final String aTilePath) {
 
 
 	//		mPending.remove(aTile.getTileId());
 
 			synchronized(mWorking)
 			{
 				mWorking.remove(aTile.getTileId());
 			}
 
 			mCallback.mapTileRequestCompleted(aTile, aTilePath);
 
 		}
 
 		/**
 		 * A tile has loaded.
 		 * @param aTile the tile that has loaded
 		 * @param aTileInputStream the input stream of the file.
 		 */
 		public void tileLoaded(final OpenStreetMapTile aTile, final InputStream aTileInputStream) {
 
 			
 //			mPending.remove(aTile.getTileId());
 			synchronized(mWorking)
 			{
 				mWorking.remove(aTile.getTileId());
 			}
 
 			mCallback.mapTileRequestCompleted(aTile, aTileInputStream);
 
 		}
 
 		/**
 		 * A tile has loaded.
 		 * @param aTile the tile that has loaded
 		 * @param aRefresh whether to redraw the screen so that new tiles will be used
 		 */
 		public void tileLoaded(final OpenStreetMapTile aTile, final boolean aRefresh) {
 			
 
 //			synchronized (mPending) {
 //				mPending.remove(aTile.getTileId());
 //			}
 
 			synchronized(mWorking)
 			{
 			mWorking.remove(aTile.getTileId());
 			}
 			mCallback.mapTileRequestCompleted(aTile);
 
 		}
 
 		/**
 		 * A tile has loaded.
 		 * @param aTile the tile that has loaded
 		 * @param aDrawable the drawable that has returned.
 		 */
 		public void tileLoaded(final OpenStreetMapTile aTile, final Drawable aDrawable) {
 
 
 //			mPending.remove(aTile.getTileId());
 			
 			synchronized(mWorking)
 			{
 				mWorking.remove(aTile.getTileId());
 			}
 
 			mCallback.mapTileRequestCompleted(aTile, aDrawable);
 
 		}
 
 		
 		/**
 		 * A tile has not loaded.
 		 * @param aTile the tile that has not loaded
 		 */
 		public void tileNotLoaded(final OpenStreetMapTile aTile) 
 		{
 			if (DEBUGMODE)
 				logger.debug("Got a tile not loaded");
 
 			mWaiting.put(aTile.getTileId(), aTile.getRenderer());
 
 			synchronized(mWorking)
 			{
 				mWorking.remove(aTile.getTileId());
 			}
 		}
 		
 		/**
 		 * A tile has been requested from another mechanism.
 		 * That one will tell screen to refresh
 		 * @param aTile the tile that has loaded
 		 */
 
 		public void tilePassedOn(final OpenStreetMapTile aTile) 
 		{
 //			mPending.remove(aTile.getTileId());
 			
 			synchronized(mWorking)
 			{
 				mWorking.remove(aTile.getTileId());
 			}
 		}
 
 		
 		
 		/**
 		 * This is a functor class of type Runnable.
 		 * The run method is the encapsulated function.
 		 */
 		@Override
 		final public void run() {
 
 			long tileId;
 			
 			while ((tileId = nextTile()) != -1 ) {
 				if (DEBUGMODE)
 					logger.debug("Next tile: " + tileId);
 				try {
					IOpenStreetMapRendererInfo renderer = null;
					synchronized(mWorking)
					{
						renderer = mWorking.get(tileId);
					}
 					if (renderer != null)
 					{
 						loadTile(tileId, renderer);
 					}
 					// got a tile so back up and running
 				} catch (final CantContinueException e) {
 					logger.info("Tile loader can't continue", e);
 					sleep();
 				} catch (final Throwable e) {
 					logger.error("Error downloading tile: " + tileId, e);
 					sleep();
 				}
 			}
 		}
 	}
 
 	public class CantContinueException extends Exception {
 		private static final long serialVersionUID = 146526524087765133L;
 
 		public CantContinueException(final String aDetailMessage) {
 			super(aDetailMessage);
 		}
 
 		public CantContinueException(final Throwable aThrowable) {
 			super(aThrowable);
 		}
 	}
 	
 	private void sleep()
 	{
 		try {
 			Thread.sleep(5 *1000);
 		} catch (InterruptedException e) 
 		{
 		}
 	}
 
 }
