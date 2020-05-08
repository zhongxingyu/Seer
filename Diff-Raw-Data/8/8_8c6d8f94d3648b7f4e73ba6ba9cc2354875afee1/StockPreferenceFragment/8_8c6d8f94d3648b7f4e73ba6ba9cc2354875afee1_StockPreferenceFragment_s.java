 package com.fizzbuzz.android.util;
 
 /***
  * Copyright (c) 2008-2012 CommonsWare, LLC
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy
  * of the License at http://www.apache.org/licenses/LICENSE-2.0. Unless required
  * by applicable law or agreed to in writing, software distributed under the
  * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS
  * OF ANY KIND, either express or implied. See the License for the specific
  * language governing permissions and limitations under the License.
  * 
  * From _The Busy Coder's Guide to Android Development_
  * http://commonsware.com/Android
  */
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.preference.PreferenceFragment;
 
 public class StockPreferenceFragment
         extends PreferenceFragment {
     private final Logger mLogger = LoggerFactory.getLogger(LoggingManager.TAG);
 
     @Override
     public void onCreate(final Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         mLogger.info("StockPreferenceFragment.onCreate: in fragment: {}", this);
 
        int res = getActivity()
                .getResources()
                .getIdentifier(getArguments().getString("resource"),
                        "xml",
                        getActivity().getPackageName());

         addPreferencesFromResource(res);
     }
 
     @Override
     public void onAttach(final Activity activity) {
         super.onAttach(activity);
         mLogger.info("StockPreferenceFragment.onAttach: in fragment: {}", this);
     }
 
     @Override
     public void onDetach() {
         super.onDetach();
         mLogger.info("StockPreferenceFragment.onDetach: in fragment: {}", this);
     }
 
     @Override
     public void onActivityCreated(final Bundle savedInstanceState) {
         super.onActivityCreated(savedInstanceState);
         mLogger.info("StockPreferenceFragment.onActivityCreated: in fragment: {}", this);
     }
 
     @Override
     public void onPause() {
         super.onPause();
         mLogger.info("StockPreferenceFragment.onPause: in fragment: {}", this);
     }
 
     @Override
     public void onResume() {
         super.onResume();
         mLogger.info("StockPreferenceFragment.onResume: in fragment: {}", this);
     }
 
     @Override
     public void onDestroy() {
         super.onDestroy();
         mLogger.info("StockPreferenceFragment.onDestroy: in fragment: {}", this);
     }
 }
