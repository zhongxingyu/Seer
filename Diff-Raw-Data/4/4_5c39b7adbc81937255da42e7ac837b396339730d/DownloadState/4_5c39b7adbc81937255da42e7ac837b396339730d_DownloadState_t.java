 package ecologylab.concurrent;
 
 import java.io.IOException;
 
 import ecologylab.generic.Debug;
 import ecologylab.generic.Continuation;
 
 /**
  * Closure that keeps state about a download, as it progresses.
  */
 public class DownloadState<T extends Downloadable>
 extends Debug
 {
 	T													downloadable;
 	
 	private Continuation<T>		continuation;
 	private DownloadMonitor		downloadMonitor;
 	private Thread						downloadingThread;
 
 	private boolean						continued;
 
 	private boolean						recycled;
 
 	DownloadState(T downloadable, Continuation<T> dispatchTarget,DownloadMonitor downloadMonitor)
 	{
 		this.downloadable			= downloadable;
 		this.continuation			= dispatchTarget;
 		this.downloadMonitor	= downloadMonitor;
 	}
 
 	boolean shouldCancel()
 	{
 		boolean result				= downloadable.isRecycled();
 		if (!result)
 		{
 			BasicSite site = downloadable.getDownloadSite();
 			if (site != null)
 				result						= site.isDown();
 		}
 		return result;
 	}
 
 	/**
 	 * Do the work to download this.
 	 * @throws IOException 
 	 * 
 	 * @throws Exception
 	 */
 	void performDownload() throws IOException
 	{
 		downloadingThread		= Thread.currentThread();
 		//TODO need a lock here to prevent recycle() while downloading!!!!!!
 		if (downloadable != null && !downloadable.isRecycled())
 		{
 			//Update site statistics if available
 			BasicSite site = downloadable.getDownloadSite();
 			if(site != null)
 				site.beginActualDownload();
 			downloadable.performDownload();
 			if(site != null)
 				site.countNormalDownload();
			//ajit-added below condition for documents in local repository
			//download monitor can't do it after return from here
			if(site != null && downloadable.getDownloadLocation().isFile())
				site.endDownload();
 		}
 	}
 	protected synchronized void callContinuation()
 	{
 		if (!continued && !recycled)
 		{
 			//	 debug("dispatch()"+" "+downloadable+" -> "+dispatchTarget);
 			continued		= true;
 			downloadMonitor.dispatched++;
 			if (continuation != null)
 				continuation.callback(downloadable);
 		}
 	}
 
 	public String toString()
 	{
 		String downloadableString = downloadable == null ? "recycled" : downloadable.toString();
 		return super.toString() + "["+downloadableString +" "+
 		downloadingThread + "]";
 	}
 	
 	public void recycle(boolean recycleDownloadable)
 	{
 		if (!recycled)
 		{
 			recycled			= true;
 			
 			if (recycleDownloadable)
 				downloadable.recycle();
 			downloadable	= null;
 			continuation	= null;
 			downloadMonitor	= null;
 			downloadingThread	= null;
 		}
 	}
 
 }
