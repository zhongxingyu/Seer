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
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.WindowManager;
 
 @SuppressLint("NewApi")
 public abstract class Controller {
 
     protected Context mContext;
 
     protected Controller(Context context) {
         mContext = context;
     }
 
     public void onActivityResult(int requestCode, int resultCode, android.content.Intent data) { }
     
     protected void onApplyThemeResource(Resources.Theme theme, int resid, boolean first) { }
 
     protected void onCreate(Bundle savedInstanceState) { }
 
     protected Dialog onCreateDialog(int id) {
         return null;
     }
 
     protected Dialog onCreateDialog(int id, Bundle args) {
         return null;
     }
 
     protected void onDestroy() { }
 
     protected void onNewIntent(Intent intent) { }
 
     protected void onPause() { }
 
     protected void onPostCreate(Bundle savedInstanceState) { }
 
     protected void onPostResume() { }
 
     protected void onPrepareDialog(int id, Dialog dialog) { }
 
     protected void onPrepareDialog(int id, Dialog dialog, Bundle args) { }
 
     protected void onRestart() { }
 
     protected void onRestoreInstanceState(Bundle savedInstanceState) { }
 
     protected void onResume() { }
 
     protected void onSaveInstanceState(Bundle outState) { }
 
     protected void onStart() { }
 
     protected void onStop() { }
 
     protected void onTitleChanged(CharSequence title, int color) { }
 
     protected void onUserLeaveHint() { }
 
     protected void onActionModeFinished(ActionMode mode) { }
 
     protected void onActionModeStarted(ActionMode mode) { }
 
     protected void onAttachFragment(Fragment fragment) { }
 
     protected void onAttachedToWindow() { }
 
     protected boolean onBackPressed() { return false; }
 
     protected void onConfigurationChanged(Configuration newConfig) { }
 
     protected void onContentChanged() { }
 
     protected boolean onContextItemSelected(MenuItem item) {
         return false;
     }
 
     protected void onContextMenuClosed(Menu menu) { }
 
     protected void onCreateContextMenu(ContextMenu menu, View v,
             ContextMenu.ContextMenuInfo menuInfo) {
     }
 
     protected CharSequence onCreateDescription() {
         return null;
     }
 
     public boolean onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
         return false;
     }
 
     protected boolean onCreatePanelMenu(int featureId, Menu menu) {
         return false;
     }
 
     protected View onCreatePanelView(int featureId) {
         return null;
     }
 
     protected boolean onCreateThumbnail(Bitmap outBitmap, Canvas canvas) {
         return false;
     }
 
     protected View onCreateView(View parent, String name, Context context,
             AttributeSet attrs) {
                 return null;
     }
 
     protected View onCreateView(String name, Context context, AttributeSet attrs) {
         return null;
     }
 
     protected void onDetachedFromWindow() { }
 
     protected boolean onGenericMotionEvent(MotionEvent event) {
         return false;
     }
 
     protected boolean onKeyDown(int keyCode, KeyEvent event) {
         return false;
     }
 
     protected boolean onKeyLongPress(int keyCode, KeyEvent event) {
         return false;
     }
 
     protected boolean onKeyMultiple(int keyCode, int repeatCount, KeyEvent event) {
         return false;
     }
 
     protected boolean onKeyShortcut(int keyCode, KeyEvent event) {
         return false;
     }
 
     protected boolean onKeyUp(int keyCode, KeyEvent event) {
         return false;
     }
 
     protected void onLowMemory() { }
 
     protected boolean onMenuItemSelected(int featureId, MenuItem item) {
         return false;
     }
 
     protected boolean onMenuOpened(int featureId, Menu menu) {
         return false;
 
     }
 
     protected boolean onOptionsItemSelected(MenuItem item) {
         return false;
     }
 
     protected void onOptionsMenuClosed(Menu menu) { }
 
     protected void onPanelClosed(int featureId, Menu menu) { }
 
     protected boolean onPrepareOptionsMenu(Menu menu) {
         return false;
     }
 
     protected boolean onPreparePanel(int featureId, View view, Menu menu) {
         return false;
     }
 
    protected Object onRetainCustomNonConfigurationInstance() {
         return null;
     }
 
     protected boolean onSearchRequested() {
         return false;
     }
 
     protected boolean onTouchEvent(MotionEvent event) {
         return false;
     }
 
     protected boolean onTrackballEvent(MotionEvent event) {
         return false;
     }
 
     protected void onTrimMemory(int level) { }
 
     protected void onUserInteraction() { }
 
     protected void onWindowAttributesChanged(WindowManager.LayoutParams params) { }
 
     protected void onWindowFocusChanged(boolean hasFocus) { }
 
     protected ActionMode onWindowStartingActionMode(ActionMode.Callback callback) {
         return null;
     }
 
 
 
 
     // Fragment specific methods
     public void onInflate(FragmentActivity activity, AttributeSet attrs, Bundle savedInstanceState) { }
 
     public void onAttach(FragmentActivity activity) { }
 
     //public Animator onCreateAnimator(int transit, boolean enter, int nextAnim) { return null; }
 
     public void onViewCreated(View view, Bundle savedInstanceState) { }
 
     public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState, View view) { return view; }
 
     // public View getView() { return null; }
 
     public void onActivityCreated(Bundle savedInstanceState) { }
 
     public void onDestroyView() { }
 
     public void onDetach() { }
 
     public void onDestroyOptionsMenu() { }
 
 }
