 /*******************************************************************************
  * Copyright (c) 2012 Moarub Oy.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the GNU Public License v3.0
  * which accompanies this distribution, and is available at
  * http://www.gnu.org/licenses/gpl.html
  * 
  * Contributors:
  *     Moarub Oy - initial API and implementation
  ******************************************************************************/
 package com.moarub.sharemore;
 
 import android.app.Activity;
 import android.os.Bundle;
 
 public class ShareMorePreferencesActivity extends Activity {
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
		try {
			Class.forName("android.os.AsyncTask");
		} catch (ClassNotFoundException e) {
		} // Hack
 		super.onCreate(savedInstanceState);
 		getFragmentManager().beginTransaction().replace(android.R.id.content, new ShareMorePreferenceFragment())
         .commit();
 	}
 	
 	
 }
