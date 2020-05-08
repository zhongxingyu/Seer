 /*
  * Copyright (C) 2008 The Android Open Source Project
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
 
 package com.android.systemui.statusbar;
 
 import com.android.internal.statusbar.StatusBarIcon;
 import com.android.internal.statusbar.StatusBarNotification;
 import android.app.Notification;
 import android.app.PendingIntent;
 import android.content.Context;
 import android.os.IBinder;
 import android.os.RemoteException;
 import android.util.AttributeSet;
 import android.view.LayoutInflater;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.RemoteViews;
 import android.widget.ScrollView;
 import android.widget.TextView;
 
 import com.android.systemui.R;
 
 public class IntruderView extends LinearLayout {
     StatusBarService mService;
     ItemTouchDispatcher mTouchDispatcher;
     private ScrollView mIntruderScrollView;
     private TextView mIntruderLatestTitle;
     private LinearLayout mIntruderLatestItems;
     private LinearLayout mIntruderNotificationLinearLayout;
     private NotificationData mNotificationData = new NotificationData();
     private Context mContext;
     private ImageView mClearButton;
 
     public IntruderView(Context context, AttributeSet attrs) {
         super(context, attrs);
         mContext = context;
     }
 
     @Override
     protected void onFinishInflate() {
         super.onFinishInflate();
         mIntruderScrollView = (ScrollView) findViewById(R.id.intruderscroll);
         mIntruderNotificationLinearLayout = (LinearLayout) findViewById(R.id.intrudernotificationLinearLayout);
         mIntruderLatestItems = (LinearLayout) findViewById(R.id.intruderlatestItems);
         mClearButton = (ImageView) findViewById(R.id.intruder_clear_all_button);
         mClearButton.setOnClickListener(mClearButtonListener);
     }
 
     private View.OnClickListener mClearButtonListener = new View.OnClickListener() {
         @Override
         public void onClick(View v) {
             mService.toggleClearNotif();
             mService.animateCollapse();
         }
     };
 
     public void updateLayout() {
         mIntruderNotificationLinearLayout.removeAllViews();
         mIntruderNotificationLinearLayout.addView(mIntruderLatestItems);
     }
 
     /** We want to shrink down to 0, and ignore the background. */
     @Override
     public int getSuggestedMinimumHeight() {
         return 0;
     }
 
     @Override
     public boolean onInterceptTouchEvent(MotionEvent event) {
         if (mTouchDispatcher.needsInterceptTouch(event)) {
             return true;
         }
         return super.onInterceptTouchEvent(event);
     }
 
     @Override
     public boolean onTouchEvent(MotionEvent event) {
         boolean handled = mTouchDispatcher.handleTouchEvent(event);
         
         if (super.onTouchEvent(event)) {
             handled = true;
         }
 	
         return handled;
     }
 
     public void setAreThereNotifications() {
         boolean latest = mNotificationData.hasVisibleItems();
 
         if (mNotificationData.hasClearableItems()) {
             mClearButton.setVisibility(View.VISIBLE);
         } else {
             mClearButton.setVisibility(View.GONE);
         }
     }
 
     public void updateNotification(IBinder key, StatusBarNotification notification) {
         NotificationData oldList;
         int oldIndex = mNotificationData.findEntry(key);
        if (oldIndex >= 0) {
             return;
         } else {
            oldIndex = mNotificationData.findEntry(key);
            if (oldIndex < 0) {
                return;
            }
             oldList = mNotificationData;
         }
         final NotificationData.Entry oldEntry = oldList.getEntryAt(oldIndex);
         final StatusBarNotification oldNotification = oldEntry.notification;
         final RemoteViews oldContentView = oldNotification.notification.contentView;
         final RemoteViews contentView = notification.notification.contentView;
 
         // Can we just reapply the RemoteViews in place?  If when didn't change, the order
         // didn't change.
         if (notification.notification.when == oldNotification.notification.when
                 && notification.isOngoing() == oldNotification.isOngoing()
                 && oldEntry.expanded != null
                 && contentView != null && oldContentView != null
                 && contentView.getPackage() != null
                 && oldContentView.getPackage() != null
                 && oldContentView.getPackage().equals(contentView.getPackage())
                 && oldContentView.getLayoutId() == contentView.getLayoutId()) {
             oldEntry.notification = notification;
             try {
                 // Reapply the RemoteViews
                 contentView.reapply(mContext, oldEntry.content);
                 // update the contentIntent
                 final PendingIntent contentIntent = notification.notification.contentIntent;
                 if (contentIntent != null) {
                     oldEntry.content.setOnClickListener(mService.makeLauncher(contentIntent,
                                 notification.pkg, notification.tag, notification.id));
                 }
                 // Update the icon.
                 final StatusBarIcon ic = new StatusBarIcon(notification.pkg,
                         notification.notification.icon, notification.notification.iconLevel,
                         notification.notification.number);
                 if (!oldEntry.icon.set(ic)) {
                     return;
                 }
             }
             catch (RuntimeException e) {
                 removeNotificationViews(key);
                 addNotificationViews(key, notification);
             }
         } else {
             removeNotificationViews(key);
             addNotificationViews(key, notification);
         }
         setAreThereNotifications();
     }
 
     private View[] makeNotificationView(final IBinder key, final StatusBarNotification notification, ViewGroup parent) {
         Notification n = notification.notification;
         RemoteViews remoteViews = n.contentView;
         if (remoteViews == null) {
             return null;
         }
 
         // create the row view
         LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
         LatestItemContainer row = (LatestItemContainer) inflater.inflate(R.layout.status_bar_latest_event, parent, false);
         if ((n.flags & Notification.FLAG_ONGOING_EVENT) == 0 && (n.flags & Notification.FLAG_NO_CLEAR) == 0) {
             row.setOnSwipeCallback(mTouchDispatcher, new Runnable() {
                 @Override
                 public void run() {
                     try {
                         mService.getServicesBar().onNotificationClear(notification.pkg, notification.tag, notification.id);
                         mService.setClearLauncherNotif(notification.pkg);
                         NotificationData list = mNotificationData;
                         int index = mNotificationData.findEntry(key);
                         if (index < 0) {
                             list = mNotificationData;
                             index = mNotificationData.findEntry(key);
                         }
                         if (index >= 0) {
                             list.getEntryAt(index).cancelled = true;
                         }
                     } catch (RemoteException e) {
                         // Skip it, don't crash.
                     }
                 }
             });
         }
 
         // bind the click event to the content area
         ViewGroup content = (ViewGroup) row.findViewById(R.id.content);
         content.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
         content.setOnFocusChangeListener(mFocusChangeListener);
         PendingIntent contentIntent = n.contentIntent;
         if (contentIntent != null) {
             content.setOnClickListener(mService.makeLauncher(contentIntent, notification.pkg,
                         notification.tag, notification.id));
         }
 
         View expanded = null;
         Exception exception = null;
         try {
             expanded = remoteViews.apply(mContext, content);
         }
         catch (RuntimeException e) {
             exception = e;
         }
         if (expanded == null) {
             String ident = notification.pkg + "/0x" + Integer.toHexString(notification.id);
             return null;
         } else {
             mService.resetTextViewColors(expanded);
             content.addView(expanded);
             row.setDrawingCacheEnabled(true);
         }
 
         return new View[] { row, content, expanded };
     }
 
     public StatusBarIconView addNotificationViews(IBinder key, StatusBarNotification notification) {
         ViewGroup parent;
         final boolean isOngoing = notification.isOngoing();
         if (!isOngoing) {
             parent = mIntruderLatestItems;
         } else {
             return null;
         }
         // Construct the expanded view.
         final View[] views = makeNotificationView(key, notification, parent);
         if (views == null) {
             return null;
         }
         final View row = views[0];
         final View content = views[1];
         final View expanded = views[2];
         // Construct the icon.
         final StatusBarIconView iconView = new StatusBarIconView(mContext,
                 notification.pkg + "/0x" + Integer.toHexString(notification.id));
         final StatusBarIcon ic = new StatusBarIcon(notification.pkg, notification.notification.icon,
                     notification.notification.iconLevel, notification.notification.number);
         if (!iconView.set(ic)) {
             return null;
         }
         // Add the expanded view.
         final int viewIndex = mNotificationData.add(key, notification, row, content, expanded, iconView);
         parent.addView(row, viewIndex);
         return iconView;
     }
 
     public StatusBarNotification removeNotificationViews(IBinder key) {
         NotificationData.Entry entry = mNotificationData.remove(key);
         if (entry == null) {
             entry = mNotificationData.remove(key);
             if (entry == null) {
                 return null;
             }
         }
         // Remove the expanded view.
         ((ViewGroup)entry.row.getParent()).removeView(entry.row);
         // Remove the icon.
         if ((entry.icon != null) && (((ViewGroup)entry.icon.getParent()) != null)) {
            ((ViewGroup)entry.icon.getParent()).removeView(entry.icon);
         }
 
         if (entry.cancelled) {
             if (!mNotificationData.hasClearableItems()) {
                 mService.setIntruderAlertVisibility(false);
             }
         }
         return entry.notification;
     }
 
     private View.OnFocusChangeListener mFocusChangeListener = new View.OnFocusChangeListener() {
         public void onFocusChange(View v, boolean hasFocus) {
             // Because 'v' is a ViewGroup, all its children will be (un)selected
             // too, which allows marqueeing to work.
             v.setSelected(hasFocus);
         }
     };
 }
