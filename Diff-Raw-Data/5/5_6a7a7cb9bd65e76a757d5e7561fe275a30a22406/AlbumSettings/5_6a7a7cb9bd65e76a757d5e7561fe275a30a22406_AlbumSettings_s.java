 /*
  * Copyright (C) 2012 The Android Open Source Project
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
 package com.android.dreams.phototable;
 
 import android.content.SharedPreferences;
 
 import java.util.HashSet;
 import java.util.Set;
 
 /**
  * Common utilities for album settings.
  */
 public class AlbumSettings {
     public static final String ALBUM_SET = "Enabled Album Set";
 
     public static Set<String> getEnabledAlbums(SharedPreferences settings) {
         Set<String> enabled = settings.getStringSet(ALBUM_SET, null);
         if (enabled == null) {
            enabled= new HashSet<String>();
             enabled.add(StockSource.ALBUM_ID);
             setEnabledAlbums(settings, enabled);
         }
         return enabled;
     }
 
     public static void setEnabledAlbums(SharedPreferences settings, Set<String> value) {
         SharedPreferences.Editor editor = settings.edit();
         editor.putStringSet(ALBUM_SET, value);
        editor.commit();
     }
 }
