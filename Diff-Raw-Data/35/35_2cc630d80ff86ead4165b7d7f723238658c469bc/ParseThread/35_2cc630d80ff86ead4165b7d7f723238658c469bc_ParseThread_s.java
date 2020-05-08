 package com.argando.parcersample.backgroundupdate;
 
 import android.util.Log;
 import com.argando.parcersample.Cache;
 import com.argando.parcersample.parser.DataParcer;
 import com.argando.parcersample.data.LeaguesHandler;
 
 /**
  * User: argando
  * Date: 21.07.12
  * Time: 21:22
  */
 public class ParseThread implements Runnable
 {
 	private static final String LOG_TAG = ParseThread.class.getSimpleName();
 	private boolean mRunning = false;
 	Thread mSimpleThread;
	private final String mCacheDir;
 
 	public ParseThread(String cacheDir)
 	{
 		mCacheDir = cacheDir;
 	}
 
 	public void startParse()
 	{
 		Log.w(LOG_TAG, mRunning + " start parse" );
 		if (!mRunning)
 		{
 			Log.w(LOG_TAG,  "new thread" );
 			mSimpleThread = new Thread(this);
 			mSimpleThread.start();
 		}
 	}
 
 	public void run()
 	{
 		Log.w(LOG_TAG,  "run" );
 		mRunning = true;
 		DataParcer dataParcer = new DataParcer();
 		LeaguesHandler.mListLeauges = dataParcer.parceScoreboard();
 		Cache.INSTANCE.cacheResults(LeaguesHandler.mListLeauges, mCacheDir);
 		mRunning = false;
 	}
 }
