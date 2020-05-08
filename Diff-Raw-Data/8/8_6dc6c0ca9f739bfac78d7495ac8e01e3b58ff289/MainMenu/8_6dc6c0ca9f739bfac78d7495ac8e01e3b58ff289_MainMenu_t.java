 /*
  * Copyright (C) 2011 The original author or authors.
  * 
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
  * in compliance with the License. You may obtain a copy of the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software distributed under the License
  * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
  * or implied. See the License for the specific language governing permissions and limitations under
  * the License.
  */
 
 package com.zapta.apps.maniana.menus;
 
 import static com.zapta.apps.maniana.util.Assertions.checkNotNull;
 import android.graphics.drawable.BitmapDrawable;
 import android.media.AudioManager;
 import android.view.KeyEvent;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnTouchListener;
 import android.view.ViewGroup;
 import android.view.ViewGroup.LayoutParams;
 import android.view.WindowManager;
 import android.widget.ImageView;
 import android.widget.PopupWindow;
 import android.widget.PopupWindow.OnDismissListener;
 import android.widget.TextView;
 
 import com.zapta.apps.maniana.R;
 import com.zapta.apps.maniana.annotations.MainActivityScope;
 import com.zapta.apps.maniana.main.MainActivityState;
 import com.zapta.apps.maniana.util.DisplayUtil;
 import com.zapta.apps.maniana.util.PopupsTracker.TrackablePopup;
 
 /**
  * Main menu.
  * 
  * @author Tal Dayan (adapted to Maniana) Based on example by Lorensius W. L. T
  *         <lorenz@londatiga.net>.
  */
 @MainActivityScope
 public class MainMenu implements OnDismissListener, TrackablePopup {
 
     public interface OnActionItemOutcomeListener {
         void onOutcome(MainMenu source, MainMenuEntry selectedEntry);
     }
 
     private final MainActivityState mMainActivityState;
 
     /** The window that contains the menu's top view. */
     private final PopupWindow mMenuWindow;
 
     private View mTopView;
 
     private ViewGroup mItemContainerView;
 
     private final OnActionItemOutcomeListener mOutcomeListener;
 
     /**
      * Constructor allowing orientation override
      * 
      * @param mContext Context
      * @param orientation Layout orientation, can be vartical or horizontal
      */
     public MainMenu(MainActivityState mainActivityState, OnActionItemOutcomeListener outcomeListener) {
         mMainActivityState = mainActivityState;
         mMenuWindow = new PopupWindow(mainActivityState.context());
 
         mMenuWindow.setTouchInterceptor(new OnTouchListener() {
             @Override
             public boolean onTouch(View v, MotionEvent event) {
                 // TODO: dismiss if outside of mItemContainsView.
                 //LogUtil.debug("*** onTouch: %f, %f, action=%d", event.getX(), event.getY(), event.getAction());
                 if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
                     mMenuWindow.dismiss();
                     return true;
                 }
                 return false;
             }
         });
 
         mOutcomeListener = checkNotNull(outcomeListener);
 
         mTopView = (ViewGroup) mMainActivityState.services().layoutInflater()
                 .inflate(R.layout.main_menu, null);
 
         mItemContainerView = (ViewGroup) mTopView.findViewById(R.id.items_container);
 
         mTopView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
                 LayoutParams.WRAP_CONTENT));
 
         mMenuWindow.setContentView(mTopView);
         mMenuWindow.setOnDismissListener(this);
     }
     
     /**
      * Show the popup action menu over a given anchor view.
      * 
      * @param anchorView a view to which the action menu's arrow will point it.
      */
     public final void show(View anchorView) {
 
         for (MainMenuEntry entry : MainMenuEntry.values()) {
             if (entry != MainMenuEntry.DEBUG || mMainActivityState.debugController().isDebugMode()) {
                 addEntry(entry);
             }
         }
 
         // Set transparent window background. This will clear the horizontal strips above and below
         // the menu defined by the two arrows.
         mMenuWindow.setBackgroundDrawable(new BitmapDrawable());
 
         mMenuWindow.setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
         mMenuWindow.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
 
         mMenuWindow.setTouchable(true);
         mMenuWindow.setFocusable(true);
         mMenuWindow.setOutsideTouchable(true);
 
         mTopView.measure(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
 
         // NOTE: without calling setFocusableInTouchMode(), the key listenter is not called.
         mTopView.setFocusableInTouchMode(true);
         mTopView.setOnKeyListener(new View.OnKeyListener() {
             @Override
             public boolean onKey(View v, int keyCode, KeyEvent event) {
                 if (keyCode == KeyEvent.KEYCODE_MENU && event.getRepeatCount() == 0
                         && event.getAction() == KeyEvent.ACTION_DOWN) {
                     dismiss();
                     return true;
                 }
 
                 return false;
             }
         });
 
         mMenuWindow.setAnimationStyle(R.style.Animations_MainMenu);
         mMainActivityState.popupsTracker().track(this);
         // TODO: normalize dx, dy by density.
         //
         // NOTE: the large x offset is a workaround for the orientation change issue. To reproduce
         // (with xoff param = 0):
         // 1. While in portrait mode, open the main menu.
         // 2. Rotate the phone to landsape orientation and observe the main menu.
         //
         // Actual behavior: the main menu is positioned too much to the left on the main
         // menu button.
         //
         final float density = DisplayUtil.getDensity(mMainActivityState.context());
         final int xOffset = (int)(1000 * density);
         final int yOffset = (int)(-2 * density -0.5f);
         mMenuWindow.showAsDropDown(anchorView, xOffset, yOffset);
     }
     
     private final boolean isShowing() {
         return mMenuWindow.isShowing();
     }
 
     /**
      * Add an action item to the end of the list.
      */
     private final void addEntry(final MainMenuEntry entry) {
         final View entryTopView = mMainActivityState.services().layoutInflater()
                 .inflate(R.layout.main_menu_entry, null);
 
        final View highlightView = entryTopView.findViewById(R.id.main_menu_entry_highlight);
        
         final ImageView imageView = (ImageView) entryTopView
                 .findViewById(R.id.main_menu_entry_icon);
         imageView.setImageResource(entry.iconResourceId);
 
         final TextView textView = (TextView) entryTopView.findViewById(R.id.main_menu_entry_text);
         textView.setText(entry.textResourceId);
 
         // Set a listener to track touches and highlight pressed items.
         entryTopView.setOnTouchListener(new View.OnTouchListener() {
             @Override
             public boolean onTouch(View v, MotionEvent event) {
                 if (event.getAction() == MotionEvent.ACTION_DOWN) {
                     // NOTE: this clears any padding set on the top view (in case it is set in xml).
                    highlightView.setBackgroundResource(R.drawable.popup_menu_entry_selected);
                 } else if (event.getAction() == MotionEvent.ACTION_CANCEL
                         || event.getAction() == MotionEvent.ACTION_UP || !entryTopView.isPressed()) {
                    highlightView.setBackgroundResource(0);
                 }
                 return false;
             }
         });
 
         entryTopView.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 onMenuEntrySelected(entry);
             }
         });
 
         entryTopView.setFocusable(true);
         entryTopView.setClickable(true);
 
         mItemContainerView.addView(entryTopView);
     }
 
     /** Called when a menu entry is clicked */
     private final void onMenuEntrySelected(final MainMenuEntry entry) {
         mMainActivityState.services().maybePlayStockSound(AudioManager.FX_KEY_CLICK, false);
 
         mMenuWindow.dismiss();
 
         // Short delay to let the dismiss animation complete.
         final int animationTimeMillis = mMainActivityState.context().getResources().getInteger(R.integer.popup_menu_dismiss_animation_millis_id);
         mMainActivityState.view().getRootView().postDelayed(new Runnable() {
             @Override
             public void run() {
                 mOutcomeListener.onOutcome(MainMenu.this, entry);
             }
         }, animationTimeMillis);
     }
 
     /** Called when the window is dismissed. */
     @Override
     public final void onDismiss() {
         mMainActivityState.popupsTracker().untrack(this);
     }
 
     /** Public method to dismiss the menu. */
     public final void dismiss() {
         if (isShowing()) {
             mMenuWindow.dismiss();
         }
     }
 
     /** Called by the popup tracker. */
     @Override
     public final void closeLeftOver() {
         dismiss();
     }
 }
