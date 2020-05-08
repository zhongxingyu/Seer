 /*******************************************************************************
  * Copyright (c) 2013 Jeff Mixon.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the GNU Public License v2.0
  * (or any later version, at your option) which accompanies this distribution,
  * and is available at http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
  * 
  * Contributors:
  *     Jeff Mixon - initial public release
  ******************************************************************************/
 package com.steelthorn.android.watch.smartsquare;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.widget.TextView;
 import br.com.condesales.EasyFoursquareAsync;
import br.com.condesales.constants.FoursquareConstants;
 import br.com.condesales.listeners.AccessTokenRequestListener;
 
 public class MainActivity extends Activity
 {
 	private static final String TAG = "MainActivity";
 
 	@Override
 	public void onCreate(Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 		
		// This is kind of a lame, but requires the smallest amount of modification of the original library
		// TODO: Your 4sq app keys here
		FoursquareConstants.CLIENT_ID = "YOUR_CLIENT_ID";
		FoursquareConstants.CLIENT_SECRET = "YOUR_CLIENT_SECRET";
		
 		EasyFoursquareAsync fsq = new EasyFoursquareAsync(this);
 		fsq.requestAccess(new FoursquareAccessCallback());
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu)
 	{
 		getMenuInflater().inflate(R.menu.activity_main, menu);
 		return true;
 	}
 
 	private class FoursquareAccessCallback implements AccessTokenRequestListener
 	{
 		@Override
 		public void onError(String errorMsg)
 		{
 			Log.e(TAG, "Couldn't authenticate to foursquare");
 		}
 
 		@Override
 		public void onAccessGrant(String accessToken)
 		{
 			Log.i(TAG, "Successfully authenticated to foursquare");
 			Log.i(TAG, "Key: " + accessToken);
 			
 			TextView tv = (TextView)findViewById(R.id.txtIntro);
 			tv.setText(R.string.success_text);
 		}
 	}
 }
