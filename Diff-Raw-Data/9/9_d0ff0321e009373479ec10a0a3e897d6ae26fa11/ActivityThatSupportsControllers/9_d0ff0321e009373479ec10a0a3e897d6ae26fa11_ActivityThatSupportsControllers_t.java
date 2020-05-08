 /*
  *
  *  Copyright (C) 2012-2013 Alex Birkett
  *
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  *
  */
 
 package com.birkett.controllers;
 
 import java.util.Iterator;
 import java.util.ArrayList;
 import java.util.List;
 
 import android.annotation.SuppressLint;
 import android.app.Dialog;
 import android.content.Context;
 import android.content.Intent;
 import android.content.res.Configuration;
 import android.content.res.Resources;
 import android.graphics.Bitmap;
 import android.graphics.Canvas;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentActivity;
 import android.util.AttributeSet;
 import android.view.ActionMode;
 import android.view.ContextMenu;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.WindowManager;
 
 @SuppressLint("NewApi")
 public abstract class ActivityThatSupportsControllers extends FragmentActivity {
 
     protected ArrayList<Controller> mControllers;
 
     protected ActivityThatSupportsControllers() {
         mControllers = new ArrayList<Controller>();
     }
 
     public void addController(Controller controller) {
         mControllers.add(controller);
     }
 
     public void removeController(Controller controller) {
         mControllers.remove(controller);
     }
 
     public List getControllersList() {
         return mControllers;
     }
 
     @Override
     public void onContentChanged() {
         super.onContentChanged();
         Iterator<Controller> iterator = mControllers.iterator();
         while (iterator.hasNext()) {
             iterator.next().onContentChanged();
         }
     }
 
     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
         super.onActivityResult(requestCode, resultCode, data);
         Iterator<Controller> iterator = mControllers.iterator();
         while (iterator.hasNext()) {
             iterator.next().onActivityResult(requestCode, resultCode, data);
         }
     }
 
     @Override
     protected void onApplyThemeResource(Resources.Theme theme, int resid, boolean first) {
         super.onApplyThemeResource(theme, resid, first);
         Iterator<Controller> iterator = mControllers.iterator();
         while (iterator.hasNext()) {
             iterator.next().onApplyThemeResource(theme, resid, first);
         }
     }
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         Iterator<Controller> iterator = mControllers.iterator();
         while (iterator.hasNext()) {
             iterator.next().onCreate(savedInstanceState);
         }
     }
 
     @Override
     protected Dialog onCreateDialog(int id) {
         Dialog dialog = null;
         Iterator<Controller> iterator = mControllers.iterator();
         while (iterator.hasNext()) {
             dialog = iterator.next().onCreateDialog(id);
             if (dialog != null) {
                 break;
             }
         }
         return dialog;
     }
 
     @Override
     protected Dialog onCreateDialog(int id, Bundle args) {
         Dialog dialog = null;
         Iterator<Controller> iterator = mControllers.iterator();
         while (iterator.hasNext()) {
             dialog = iterator.next().onCreateDialog(id, args);
             if (dialog != null) {
                 break;
             }
         }
         return dialog;
     }
 
     protected void onDestroy() {
         super.onDestroy();
         Iterator<Controller> iterator = mControllers.iterator();
         while (iterator.hasNext()) {
             iterator.next().onDestroy();
         }
     }
 
     @Override
     protected void onNewIntent(Intent intent) {
         super.onNewIntent(intent);
         Iterator<Controller> iterator = mControllers.iterator();
         while (iterator.hasNext()) {
             iterator.next().onNewIntent(intent);
         }
     }
 
     @Override
     protected void onPause() {
         super.onPause();
         Iterator<Controller> iterator = mControllers.iterator();
         while (iterator.hasNext()) {
             iterator.next().onPause();
         }
     }
 
     @Override
     protected void onPostCreate(Bundle savedInstanceState) {
         super.onPostCreate(savedInstanceState);
         Iterator<Controller> iterator = mControllers.iterator();
         while (iterator.hasNext()) {
             iterator.next().onPostCreate(savedInstanceState);
         }
     }
 
     @Override
     protected void onPostResume() {
         super.onPostResume();
         Iterator<Controller> iterator = mControllers.iterator();
         while (iterator.hasNext()) {
             iterator.next().onPostResume();
         }
     }
 
     @SuppressWarnings("deprecation")
     @Override
     protected void onPrepareDialog(int id, Dialog dialog) {
         super.onPrepareDialog(id, dialog);
         Iterator<Controller> iterator = mControllers.iterator();
         while (iterator.hasNext()) {
             iterator.next().onPrepareDialog(id, dialog);
         }
     }
 
     @SuppressWarnings("deprecation")
     @Override
     protected void onPrepareDialog(int id, Dialog dialog, Bundle args) {
         super.onPrepareDialog(id, dialog, args);
         Iterator<Controller> iterator = mControllers.iterator();
         while (iterator.hasNext()) {
             iterator.next().onPrepareDialog(id, dialog, args);
         }
     }
 
     @Override
     protected void onRestart() {
         super.onRestart();
         Iterator<Controller> iterator = mControllers.iterator();
         while (iterator.hasNext()) {
             iterator.next().onRestart();
         }
     }
 
     @Override
     protected void onRestoreInstanceState(Bundle savedInstanceState) {
         super.onRestoreInstanceState(savedInstanceState);
         Iterator<Controller> iterator = mControllers.iterator();
         while (iterator.hasNext()) {
             iterator.next().onRestoreInstanceState(savedInstanceState);
         }
     }
 
     @Override
     protected void onResume() {
         super.onResume();
         Iterator<Controller> iterator = mControllers.iterator();
         while (iterator.hasNext()) {
             iterator.next().onResume();
         }
     }
 
     @Override
     protected void onSaveInstanceState(Bundle outState) {
         super.onSaveInstanceState(outState);
         Iterator<Controller> iterator = mControllers.iterator();
         while (iterator.hasNext()) {
             iterator.next().onSaveInstanceState(outState);
         }
     }
 
     @Override
     protected void onStart() {
         super.onStart();
         Iterator<Controller> iterator = mControllers.iterator();
         while (iterator.hasNext()) {
             iterator.next().onStart();
         }
     }
 
     @Override
     protected void onStop() {
         super.onStop();
         Iterator<Controller> iterator = mControllers.iterator();
         while (iterator.hasNext()) {
             iterator.next().onStop();
         }
     }
 
     @Override
     protected void onTitleChanged(CharSequence title, int color) {
         super.onTitleChanged(title, color);
         Iterator<Controller> iterator = mControllers.iterator();
         while (iterator.hasNext()) {
             iterator.next().onTitleChanged(title, color);
         }
     }
 
     @Override
     protected void onUserLeaveHint() {
         super.onUserLeaveHint();
         Iterator<Controller> iterator = mControllers.iterator();
         while (iterator.hasNext()) {
             iterator.next().onUserLeaveHint();
         }
     }
 
     @Override
     public void onActionModeFinished(ActionMode mode) {
         super.onActionModeFinished(mode);
         Iterator<Controller> iterator = mControllers.iterator();
         while (iterator.hasNext()) {
             iterator.next().onActionModeFinished(mode);
         }
     }
 
     @Override
     public void onActionModeStarted(ActionMode mode) {
         super.onActionModeStarted(mode);
         Iterator<Controller> iterator = mControllers.iterator();
         while (iterator.hasNext()) {
             iterator.next().onActionModeStarted(mode);
         }
     }
 
     @Override
     public void onAttachFragment(Fragment fragment) {
         super.onAttachFragment(fragment);
         Iterator<Controller> iterator = mControllers.iterator();
         while (iterator.hasNext()) {
             iterator.next().onAttachFragment(fragment);
         }
     }
 
     @Override
     public void onAttachedToWindow() {
         super.onAttachedToWindow();
         Iterator<Controller> iterator = mControllers.iterator();
         while (iterator.hasNext()) {
             iterator.next().onAttachedToWindow();
         }
     }
 
     @Override
     public void onBackPressed() {
         Iterator<Controller> iterator = mControllers.iterator();
         boolean consumed = false;
         while (iterator.hasNext() && !consumed) {
             consumed = iterator.next().onBackPressed();
         }
         if (!consumed) {
             super.onBackPressed();
         }
     }
 
     @Override
     public void onConfigurationChanged(Configuration newConfig) {
         super.onConfigurationChanged(newConfig);
         Iterator<Controller> iterator = mControllers.iterator();
         while (iterator.hasNext()) {
             iterator.next().onConfigurationChanged(newConfig);
         }
     }
 
     @Override
     public void onContextMenuClosed(Menu menu) {
         super.onContextMenuClosed(menu);
         Iterator<Controller> iterator = mControllers.iterator();
         while (iterator.hasNext()) {
             iterator.next().onContextMenuClosed(menu);
         }
     }
 
     @Override
     public void onCreateContextMenu(ContextMenu menu, View v,
             ContextMenu.ContextMenuInfo menuInfo) {
         super.onCreateContextMenu(menu, v, menuInfo);
         Iterator<Controller> iterator = mControllers.iterator();
         while (iterator.hasNext()) {
             iterator.next().onCreateContextMenu(menu, v, menuInfo);
         }
     }
 
     @Override
     public CharSequence onCreateDescription() {
         CharSequence sequence = super.onCreateDescription();
 
         if (sequence == null) {
             Iterator<Controller> iterator = mControllers.iterator();
             while (iterator.hasNext()) {
                 sequence = iterator.next().onCreateDescription();
                 if (sequence != null) {
                     break;
                 }
             }
         }
         return sequence;
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater inflater = getMenuInflater();
         boolean returnValue = super.onCreateOptionsMenu(menu);
         Iterator<Controller> iterator = mControllers.iterator();
         while (iterator.hasNext()) {
             if (iterator.next().onCreateOptionsMenu(menu, inflater)) {
                 returnValue = true;
             }
         }
         return returnValue;
     }
 
     @Override
     public boolean onCreatePanelMenu(int featureId, Menu menu) {
         boolean returnValue = super.onCreatePanelMenu(featureId, menu);
         Iterator<Controller> iterator = mControllers.iterator();
         while (iterator.hasNext()) {
             if (iterator.next().onCreatePanelMenu(featureId, menu)) {
                 returnValue = true;
             }
         }
         return returnValue;
     }
 
     @Override
     public View onCreatePanelView(int featureId) {
         View view = super.onCreatePanelView(featureId);
         if (view == null) {
             Iterator<Controller> iterator = mControllers.iterator();
             while (iterator.hasNext()) {
                 view = iterator.next().onCreatePanelView(featureId);
                 if (view != null) {
                     break;
                 }
             }
         }
         return view;
     }
 
     @Override
     public boolean onCreateThumbnail(Bitmap outBitmap, Canvas canvas) {
         boolean result = super.onCreateThumbnail(outBitmap, canvas);
 
         if (!result) {
             Iterator<Controller> iterator = mControllers.iterator();
             while (iterator.hasNext()) {
                 result = iterator.next().onCreateThumbnail(outBitmap, canvas);
 
                 if (result) {
                     break;
                 }
             }
         }
         return result;
     }
 
     @Override
     public View onCreateView(View parent, String name, Context context,
             AttributeSet attrs) {
         View view = super.onCreateView(parent, name, context, attrs);
 
         if (view == null) {
             Iterator<Controller> iterator = mControllers.iterator();
             while (iterator.hasNext()) {
                 view = iterator.next().onCreateView(parent, name, context,
                         attrs);
                 if (view != null) {
                     break;
                 }
             }
         }
         return view;
     }
 
     @Override
     public View onCreateView(String name, Context context, AttributeSet attrs) {
         View view = super.onCreateView(name, context, attrs);
 
         if (view == null) {
             Iterator<Controller> iterator = mControllers.iterator();
             while (iterator.hasNext()) {
                 view = iterator.next().onCreateView(name, context,
                         attrs);
                 if (view != null) {
                     break;
                 }
             }
         }
         return view;
     }
 
     @Override
     public void onDetachedFromWindow() {
         super.onDetachedFromWindow();
         Iterator<Controller> iterator = mControllers.iterator();
         while (iterator.hasNext()) {
             iterator.next().onDetachedFromWindow();
         }
     }
 
     @Override
     public boolean onGenericMotionEvent(MotionEvent event) {
         boolean consumed = super.onGenericMotionEvent(event);
         if (!consumed) {
             Iterator<Controller> iterator = mControllers.iterator();
             while (iterator.hasNext()) {
                 consumed = iterator.next().onGenericMotionEvent(event);
                 if (consumed) {
                     break;
                 }
             }
         }
         return consumed;
     }
 
     @Override
     public boolean onKeyDown(int keyCode, KeyEvent event) {
         boolean consumed = super.onKeyDown(keyCode, event);
         if (!consumed) {
             Iterator<Controller> iterator = mControllers.iterator();
             while (iterator.hasNext()) {
                 consumed = iterator.next().onKeyDown(keyCode, event);
                 if (consumed) {
                     break;
                 }
             }
         }
         return consumed;
     }
 
     @Override
     public boolean onKeyLongPress(int keyCode, KeyEvent event) {
         boolean consumed = super.onKeyLongPress(keyCode, event);
         if (!consumed) {
             Iterator<Controller> iterator = mControllers.iterator();
             while (iterator.hasNext()) {
                 consumed = iterator.next().onKeyLongPress(keyCode, event);
                 if (consumed) {
                     break;
                 }
             }
         }
         return consumed;
     }
 
     @Override
     public boolean onKeyMultiple(int keyCode, int repeatCount, KeyEvent event) {
         boolean consumed = super.onKeyMultiple(keyCode, repeatCount, event);
         if (!consumed) {
             Iterator<Controller> iterator = mControllers.iterator();
             while (iterator.hasNext()) {
                 consumed = iterator.next().onKeyMultiple(keyCode, repeatCount, event);
                 if (consumed) {
                     break;
                 }
             }
         }
         return consumed;
     }
 
     @Override
     public boolean onKeyShortcut(int keyCode, KeyEvent event) {
         boolean consumed = super.onKeyShortcut(keyCode, event);
         if (!consumed) {
             Iterator<Controller> iterator = mControllers.iterator();
             while (iterator.hasNext()) {
                 consumed = iterator.next().onKeyShortcut(keyCode, event);
                 if (consumed) {
                     break;
                 }
             }
         }
         return consumed;
     }
 
     @Override
     public boolean onKeyUp(int keyCode, KeyEvent event) {
         boolean consumed = super.onKeyUp(keyCode, event);
         if (!consumed) {
             Iterator<Controller> iterator = mControllers.iterator();
             while (iterator.hasNext()) {
                 consumed = iterator.next().onKeyUp(keyCode, event);
                 if (consumed) {
                     break;
                 }
             }
         }
         return consumed;
     }
 
     @Override
     public void onLowMemory() {
         super.onLowMemory();
         Iterator<Controller> iterator = mControllers.iterator();
         while (iterator.hasNext()) {
             iterator.next().onLowMemory();
         }
     }
 
     @Override
     public boolean onMenuItemSelected(int featureId, MenuItem item) {
         boolean consumed = super.onMenuItemSelected(featureId, item);
         if (!consumed) {
             Iterator<Controller> iterator = mControllers.iterator();
             while (iterator.hasNext()) {
                 consumed = iterator.next().onMenuItemSelected(featureId, item);
                 if (consumed) {
                     break;
                 }
             }
         }
         return consumed;
     }
 
     @Override
     public boolean onMenuOpened(int featureId, Menu menu) {
         boolean consumed = super.onMenuOpened(featureId, menu);
         if (!consumed) {
             Iterator<Controller> iterator = mControllers.iterator();
             while (iterator.hasNext()) {
                 consumed = iterator.next().onMenuOpened(featureId, menu);
                 if (consumed) {
                     break;
                 }
             }
         }
         return consumed;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         boolean consumed = super.onOptionsItemSelected(item);
         if (!consumed) {
             Iterator<Controller> iterator = mControllers.iterator();
             while (iterator.hasNext()) {
                 consumed = iterator.next().onOptionsItemSelected(item);
                 if (consumed) {
                     break;
                 }
             }
         }
         return consumed;
     }
 
     @Override
     public void onOptionsMenuClosed(Menu menu) {
         super.onOptionsMenuClosed(menu);
         Iterator<Controller> iterator = mControllers.iterator();
         while (iterator.hasNext()) {
             iterator.next().onOptionsMenuClosed(menu);
         }
     }
 
     @Override
     public void onPanelClosed(int featureId, Menu menu) {
         super.onPanelClosed(featureId, menu);
         Iterator<Controller> iterator = mControllers.iterator();
         while (iterator.hasNext()) {
             iterator.next().onPanelClosed(featureId, menu);
         }
     }
 
     @Override
     public boolean onPrepareOptionsMenu(Menu menu) {
         boolean consumed = super.onPrepareOptionsMenu(menu);
         if (!consumed) {
             Iterator<Controller> iterator = mControllers.iterator();
             while (iterator.hasNext()) {
                 consumed = iterator.next().onPrepareOptionsMenu(menu);
                 if (consumed) {
                     break;
                 }
             }
         }
         return consumed;
     }
 
     @Override
     public boolean onPreparePanel(int featureId, View view, Menu menu) {
         boolean consumed = super.onPreparePanel(featureId, view, menu);
         if (!consumed) {
             Iterator<Controller> iterator = mControllers.iterator();
             while (iterator.hasNext()) {
                 consumed = iterator.next().onPreparePanel(featureId, view, menu);
                 if (consumed) {
                     break;
                 }
             }
         }
         return consumed;
     }
 
     @Override
     public Object onRetainCustomNonConfigurationInstance() {
         Object object = super.onRetainCustomNonConfigurationInstance();
 
         if (object == null) {
             Iterator<Controller> iterator = mControllers.iterator();
             while (iterator.hasNext()) {
                object = iterator.next().onRetainNonConfigurationInstance();
                 if (object != null) {
                     break;
                 }
             }
         }
         return object;
     }
 
     @Override
     public boolean onSearchRequested() {
         boolean consumed = super.onSearchRequested();
         if (!consumed) {
             Iterator<Controller> iterator = mControllers.iterator();
             while (iterator.hasNext()) {
                 consumed = iterator.next().onSearchRequested();
                 if (consumed) {
                     break;
                 }
             }
         }
         return consumed;
     }
 
     @Override
     public boolean onTouchEvent(MotionEvent event) {
         boolean consumed = super.onTouchEvent(event);
         if (!consumed) {
             Iterator<Controller> iterator = mControllers.iterator();
             while (iterator.hasNext()) {
                 consumed = iterator.next().onTouchEvent(event);
                 if (consumed) {
                     break;
                 }
             }
         }
         return consumed;
     }
 
     @Override
     public boolean onTrackballEvent(MotionEvent event) {
         boolean consumed = super.onTrackballEvent(event);
         if (!consumed) {
             Iterator<Controller> iterator = mControllers.iterator();
             while (iterator.hasNext()) {
                 consumed = iterator.next().onTrackballEvent(event);
                 if (consumed) {
                     break;
                 }
             }
         }
         return consumed;
     }
 
     @Override
     public void onTrimMemory(int level) {
         super.onTrimMemory(level);
         Iterator<Controller> iterator = mControllers.iterator();
         while (iterator.hasNext()) {
             iterator.next().onTrimMemory(level);
         }
     }
 
     @Override
     public void onUserInteraction() {
         super.onUserInteraction();
         Iterator<Controller> iterator = mControllers.iterator();
         while (iterator.hasNext()) {
             iterator.next().onUserInteraction();
         }
     }
 
     @Override
     public void onWindowAttributesChanged(WindowManager.LayoutParams params) {
         super.onWindowAttributesChanged(params);
         Iterator<Controller> iterator = mControllers.iterator();
         while (iterator.hasNext()) {
             iterator.next().onWindowAttributesChanged(params);
         }
     }
 
     @Override
     public void onWindowFocusChanged(boolean hasFocus) {
         super.onWindowFocusChanged(hasFocus);
         Iterator<Controller> iterator = mControllers.iterator();
         while (iterator.hasNext()) {
             iterator.next().onWindowFocusChanged(hasFocus);
         }
     }
 
     @Override
     public ActionMode onWindowStartingActionMode(ActionMode.Callback callback) {
         ActionMode actionMode = super.onWindowStartingActionMode(callback);
 
         if (actionMode == null) {
             Iterator<Controller> iterator = mControllers.iterator();
             while (iterator.hasNext()) {
                 actionMode = iterator.next().onWindowStartingActionMode(callback);
                 if (actionMode != null) {
                     break;
                 }
             }
         }
         return actionMode;
     }
 }
