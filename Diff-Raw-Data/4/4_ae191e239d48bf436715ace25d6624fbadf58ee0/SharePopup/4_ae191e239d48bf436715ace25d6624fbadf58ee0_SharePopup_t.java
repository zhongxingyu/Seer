 /*
  * Copyright (C) 2010 The Android Open Source Project
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
 
 package com.android.camera.ui;
 
 import com.android.camera.R;
 import com.android.camera.Util;
 
 import android.app.Activity;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.pm.PackageManager;
 import android.content.pm.ResolveInfo;
 import android.content.res.Resources;
 import android.graphics.Bitmap;
 import android.graphics.drawable.ColorDrawable;
 import android.graphics.drawable.Drawable;
 import android.net.Uri;
 import android.view.LayoutInflater;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.ViewGroup.LayoutParams;
 import android.view.WindowManager;
 import android.widget.AdapterView;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.PopupWindow;
 import android.widget.SimpleAdapter;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 // A popup window that contains a big thumbnail and a list of apps to share.
 public class SharePopup extends PopupWindow implements View.OnClickListener,
         View.OnTouchListener, AdapterView.OnItemClickListener {
     private static final String TAG = "SharePopup";
     private static final String ADAPTER_COLUMN_ICON = "icon";
     private Context mContext;
     private Uri mUri;
     private String mMimeType;
     private ImageView mThumbnail;
     private int mBitmapWidth;
     private int mBitmapHeight;
     private int mOrientation;
     // A view that contains a thumbnail and an arrow icon.
     private ViewGroup mShareView;
     // A view that contains a list of application icons and the share view.
     private View mRootView;
     // The list of the application icons.
     private ListView mShareList;
     // A rotated view that contains the thumbnail.
     private RotateLayout mThumbnailRotateLayout;
     private View mPreviewFrame;
     private ArrayList<ComponentName> mComponent = new ArrayList<ComponentName>();
 
     private class MySimpleAdapter extends SimpleAdapter {
         public MySimpleAdapter(Context context, List<? extends Map<String, ?>> data,
                 int resource, String[] from, int[] to) {
             super(context, data, resource, from, to);
         }
 
         @Override
         public View getView(int position, View convertView, ViewGroup parent) {
             View v = super.getView(position, convertView, parent);
             RotateLayout r = (RotateLayout) v.findViewById(R.id.share_icon_rotate_layout);
             r.setOrientation(mOrientation);
             return v;
         }
     }
 
     private final SimpleAdapter.ViewBinder mViewBinder =
         new SimpleAdapter.ViewBinder() {
             @Override
             public boolean setViewValue(final View view, final Object data,
                     final String text) {
                 if (view instanceof ImageView) {
                     ((ImageView)view).setImageDrawable((Drawable) data);
                     return true;
                 }
                 return false;
             }
         };
 
     public SharePopup(Activity activity, Uri uri, Bitmap bitmap, int orientation,
             View previewFrame) {
         super(activity);
 
         // Initialize variables
         mContext = activity;
         mUri = uri;
         mMimeType = mContext.getContentResolver().getType(mUri);
         mPreviewFrame = previewFrame;
         LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(
                 Context.LAYOUT_INFLATER_SERVICE);
         ViewGroup sharePopup = (ViewGroup) inflater.inflate(R.layout.share_popup, null, false);
         // This is required because popup window is full screen.
         sharePopup.setOnTouchListener(this);
         mThumbnailRotateLayout = (RotateLayout) sharePopup.findViewById(R.id.thumbnail_rotate_layout);
         mShareList = (ListView) sharePopup.findViewById(R.id.share_list);
         mShareList.setDivider(null);
         mThumbnail = (ImageView) sharePopup.findViewById(R.id.thumbnail);
         mThumbnail.setImageBitmap(bitmap);
         mShareView = (ViewGroup) sharePopup.findViewById(R.id.share_view);
         mShareView.setOnClickListener(this);
        // Show play button if this is a video thumbnail.
        if (mMimeType.startsWith("video/")) {
            sharePopup.findViewById(R.id.play).setVisibility(View.VISIBLE);
        }
         mBitmapWidth = bitmap.getWidth();
         mBitmapHeight = bitmap.getHeight();
         Resources res = mContext.getResources();
 
         // Initialize popup window size.
         mRootView = (View) sharePopup.findViewById(R.id.root);
         LayoutParams params = mRootView.getLayoutParams();
         params.width = previewFrame.getWidth();
         params.height = previewFrame.getHeight();
         mRootView.setLayoutParams(params);
 
         // Initialize popup window.
         setWidth(WindowManager.LayoutParams.MATCH_PARENT);
         setHeight(WindowManager.LayoutParams.MATCH_PARENT);
         setBackgroundDrawable(new ColorDrawable());
         setContentView(sharePopup);
         setOrientation(orientation);
         setFocusable(true);
         setAnimationStyle(R.style.AnimationPopup);
         createShareMenu();
     }
 
     public void setOrientation(int orientation) {
         mOrientation = orientation;
 
         int hPaddingRootView = mRootView.getPaddingLeft() + mRootView.getPaddingRight();
         int vPaddingRootView = mRootView.getPaddingTop() + mRootView.getPaddingBottom();
         int hPadding = mShareView.getPaddingLeft() + mShareView.getPaddingRight();
         int vPadding = mShareView.getPaddingTop() + mShareView.getPaddingBottom();
 
         // Calculate the width and the height of the thumbnail. Reserve the
         // space for paddings.
         float maxWidth = mPreviewFrame.getWidth() - hPadding - hPaddingRootView;
         float maxHeight = mPreviewFrame.getHeight() - vPadding - vPaddingRootView;
         // Swap the width and height if it is portrait mode.
         if (orientation == 90 || orientation == 270) {
             float temp = maxWidth;
             maxWidth = maxHeight;
             maxHeight = temp;
         }
         float actualAspect = maxWidth / maxHeight;
         float desiredAspect = (float) mBitmapWidth / mBitmapHeight;
         LayoutParams params = mThumbnail.getLayoutParams();
         if (actualAspect > desiredAspect) {
             params.width = Math.round(maxHeight * desiredAspect);
             params.height = Math.round(maxHeight);
         } else {
             params.width = Math.round(maxWidth);
             params.height = Math.round(maxWidth / desiredAspect);
         }
         mThumbnail.setLayoutParams(params);
 
         // Calculate the width and the height of the share view.
         int width = params.width + hPadding;
         int height = params.height + vPadding;
         LayoutParams shareViewParams = mShareView.getLayoutParams();
         if (orientation == 90 || orientation == 270) {
             shareViewParams.width = height;
             shareViewParams.height = width;
         } else {
             shareViewParams.width = width;
             shareViewParams.height = height;
         }
         mShareView.setLayoutParams(shareViewParams);
 
         if (mThumbnailRotateLayout != null) mThumbnailRotateLayout.setOrientation(orientation);
 
         int count = mShareList.getChildCount();
         for (int i = 0; i < count; i++) {
             ViewGroup f = (ViewGroup) mShareList.getChildAt(i);
             RotateLayout r = (RotateLayout) f.findViewById(R.id.share_icon_rotate_layout);
             r.setOrientation(orientation);
         }
     }
 
     @Override
     public void onClick(View v) {
         switch (v.getId()) {
             case R.id.share_view:
                 Util.viewUri(mUri, mContext);
                 break;
         }
     }
 
     @Override
     public boolean onTouch(View v, MotionEvent event) {
         if (event.getAction() == MotionEvent.ACTION_DOWN) {
             dismiss();
             return true;
         }
         return false;
     }
 
     public void createShareMenu() {
         PackageManager packageManager = mContext.getPackageManager();
         List<ResolveInfo> infos;
         infos = packageManager.queryIntentActivities(
                 new Intent(Intent.ACTION_SEND).setType(mMimeType), 0);
 
         ArrayList<HashMap<String, Object>> listItem =
                 new ArrayList<HashMap<String, Object>>();
         for(ResolveInfo info: infos) {
             ComponentName component = new ComponentName(
                     info.activityInfo.packageName, info.activityInfo.name);
             HashMap<String, Object> map = new HashMap<String, Object>();
             map.put(ADAPTER_COLUMN_ICON, info.loadIcon(packageManager));
             listItem.add(map);
             mComponent.add(component);
         }
         SimpleAdapter listItemAdapter = new MySimpleAdapter(mContext, listItem,
                 R.layout.share_icon,
                 new String[] {ADAPTER_COLUMN_ICON},
                 new int[] {R.id.icon});
         listItemAdapter.setViewBinder(mViewBinder);
         mShareList.setAdapter(listItemAdapter);
         mShareList.setOnItemClickListener(this);
     }
 
     public Uri getUri() {
         return mUri;
     }
 
     @Override
     public void onItemClick(AdapterView<?> parent, View view, int index, long id) {
         Intent intent = new Intent(Intent.ACTION_SEND);
         intent.setType(mMimeType);
         intent.putExtra(Intent.EXTRA_STREAM, mUri);
         intent.setComponent(mComponent.get(index));
         mContext.startActivity(intent);
     }
 }
