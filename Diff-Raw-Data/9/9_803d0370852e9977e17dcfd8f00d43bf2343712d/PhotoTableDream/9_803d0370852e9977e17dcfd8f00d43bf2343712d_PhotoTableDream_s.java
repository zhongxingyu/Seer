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
 
 import android.content.Context;
 import android.service.dreams.Dream;
 import android.view.LayoutInflater;
 import android.view.ViewGroup;
 
 /**
  * Example interactive screen saver: flick photos onto a table.
  */
 public class PhotoTableDream extends Dream {
     public static final String TAG = "PhotoTableDream";
     private PhotoTable mTable;
 
     @Override
     public void onStart() {
         super.onStart();
         setInteractive(true);
     }
 
     @Override
     public void onAttachedToWindow() {
         super.onAttachedToWindow();
         LayoutInflater inflater =
                 (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
         ViewGroup view = (ViewGroup) inflater.inflate(R.layout.table, null);
         PhotoTable table = (PhotoTable) view.findViewById(R.id.table);
         table.setDream(this);
         setContentView(view);
        lightsOut();
     }
 }
