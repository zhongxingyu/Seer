 /*
  * MUSTARD: Android's Client for StatusNet
  * 
  * Copyright (C) 2009-2010 macno.org, Michele Azzolari
  *
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 2 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful, but
  * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
  * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
  * for more details.
  *
  * You should have received a copy of the GNU General Public License along
  * with this program; if not, write to the Free Software Foundation, Inc.,
  * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
  * 
  */
 
 package org.mustard.android.service;
 
 import java.net.URL;
 
 import org.mustard.android.MustardApplication;
 import org.mustard.android.MustardDbAdapter;
 import org.mustard.android.core.OAuthKeyFetcher;
 
 import android.app.Service;
 import android.content.Context;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.IBinder;
 import android.util.Log;
 
 public class OAuthKeysService extends Service {
 
 	private static final String TAG = "OAuthKeysFetcher";
 
	private Context mContext;
	
//	private MustardDbAdapter mDbHelper;
 	private static URL mURL;
 
 	public static void schedule(Context context) {
 		Intent checkKeys = new Intent(context,OAuthKeysService.class);
 		context.startService(checkKeys);
 	}
 	
 	@Override
 	public void onStart(Intent intent, int startId) {
 		super.onStart(intent, startId);
 		if(MustardApplication.DEBUG) Log.i(TAG, "onStart");
//		mDbHelper = new MustardDbAdapter(this);
//		mDbHelper.open();
		mContext=getBaseContext();
 		new OAuthKeysTask().execute();
 	}
 
 	@Override
 	public void onDestroy() {
 		super.onDestroy();
 		if(MustardApplication.DEBUG) Log.i(TAG, "onDestroy");
 		
 	}
 	
 	private void onEndKeyFetch() {
 		stopSelf();
 	}
 
 	private enum RetrieveResult {
 		OK, EMPTY, IO_ERROR, AUTH_ERROR, CANCELLED
 	}
 
 	private class OAuthKeysTask extends  AsyncTask<Void, Void, RetrieveResult> {
 
 		@Override
 		public RetrieveResult doInBackground(Void... params) {
			MustardDbAdapter mDbHelper = new MustardDbAdapter(mContext);
 			try {
				mDbHelper.open();
 				OAuthKeyFetcher okf = new OAuthKeyFetcher();
 				okf.execute(getBaseContext(),mDbHelper, mURL);
 			} catch (Exception e) {
			} finally {
				if(mDbHelper != null) {
					try {
						mDbHelper.close();
					} catch (Exception e) {
						if (MustardApplication.DEBUG) e.printStackTrace();
						stopSelf();
					}
				}
 			}
 			return RetrieveResult.OK;
 		}
 
 		@Override
 		public void onPostExecute(RetrieveResult result) {
 			onEndKeyFetch();
 		}
 		
 	}
 
 	@Override
 	public IBinder onBind(Intent intent) {
 		return null;
 	}
 
 
 }
