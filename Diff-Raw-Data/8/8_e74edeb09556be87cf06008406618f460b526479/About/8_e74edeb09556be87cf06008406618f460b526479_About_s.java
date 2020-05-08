 /*
  * Copyright (C) 2012 Android Open Source Project
  * Copyright (C) 2012 RootBox Project
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
 
 package com.android.settings.rb;
 
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Bundle;
 import android.preference.Preference;
 import android.preference.PreferenceGroup;
 import android.preference.PreferenceScreen;
 
 import com.android.settings.SettingsPreferenceFragment;
 import com.android.settings.R;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 
 public class About extends SettingsPreferenceFragment {
 
     public static final String TAG = "About";
 
    Preference mBlogUrl;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         addPreferencesFromResource(R.xml.about_rom);
        mBlogUrl = findPreference("rootbox_blog");
 
         PreferenceGroup devsGroup = (PreferenceGroup) findPreference("devs");
         ArrayList<Preference> devs = new ArrayList<Preference>();
         for (int i = 0; i < devsGroup.getPreferenceCount(); i++) {
             devs.add(devsGroup.getPreference(i));
         }
         devsGroup.removeAll();
         devsGroup.setOrderingAsAdded(false);
         Collections.shuffle(devs);
         for(int i = 0; i < devs.size(); i++) {
             Preference p = devs.get(i);
             p.setOrder(i);
 
             devsGroup.addPreference(p);
         }
     }
 
     @Override
     public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == mBlogUrl) {
             launchUrl("http://www.rootbox.ca");
         }
 
         return super.onPreferenceTreeClick(preferenceScreen, preference);
     }
 
     private void launchUrl(String url) {
         Uri uriUrl = Uri.parse(url);
         Intent donate = new Intent(Intent.ACTION_VIEW, uriUrl);
         getActivity().startActivity(donate);
         Intent github = new Intent(Intent.ACTION_VIEW, uriUrl);
         getActivity().startActivity(github);
         Intent google = new Intent(Intent.ACTION_VIEW, uriUrl);
         getActivity().startActivity(google);
         Intent facebook = new Intent(Intent.ACTION_VIEW, uriUrl);
         getActivity().startActivity(facebook);
     }
 }
