 /*
  * Copyright (C) 2012 Pixmob (http://github.com/pixmob)
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.pixmob.fm2.ui;
 
 import org.pixmob.fm2.R;
 
 import android.app.NotificationManager;
 import android.os.Bundle;
 import android.support.v4.app.ActionBar;
 import android.support.v4.app.FragmentActivity;
 
 /**
  * Application entry point.
  * @author Pixmob
  */
 public class FM2 extends FragmentActivity {
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         
         final ActionBar actionBar = getSupportActionBar();
         actionBar.setDisplayShowCustomEnabled(true);
         actionBar.setCustomView(R.layout.nav);
         actionBar.setDisplayShowHomeEnabled(false);
         actionBar.setDisplayShowTitleEnabled(false);
         
         setContentView(R.layout.main);
     }
     
     @Override
     protected void onResume() {
         super.onResume();
         
         // Clear the notification about account updates.
         final NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
         nm.cancel(R.string.notif_account_updated);
     }
 }
