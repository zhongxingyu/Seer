 package com.argando.parcersample.backgroundupdate;
 
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.util.Log;
 import com.argando.parcersample.data.DataNameHelper;
 import com.argando.parcersample.network.NetworkChecker;
 
 /**
  * User: argando
  * Date: 21.07.12
  * Time: 20:08
  */
 
 /**
  *Receiver receive alarm call backs to for updating parsing results
  */
 public class UpdateReceiver extends BroadcastReceiver
 {
 	private static final String LOG_TAG = UpdateReceiver.class.getSimpleName();
 	private static ParseThread parseThread = new ParseThread(DataNameHelper.EXTERNAL_CACHE_DIR);
 	@Override
 	public void onReceive(Context context, Intent intent)
 	{
 		Log.d(LOG_TAG, "onReceive " + context + intent.getAction());
 		if (NetworkChecker.isConnected(context))
 		{
			parseThread.startParse();
 		}
 	}
 }
