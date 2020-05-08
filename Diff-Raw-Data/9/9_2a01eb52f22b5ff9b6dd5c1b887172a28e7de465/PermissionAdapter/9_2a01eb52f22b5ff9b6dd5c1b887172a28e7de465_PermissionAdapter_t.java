 /*
 **
 ** Copyright 2013, The MoKee OpenSource Project
 **
 ** Licensed under the Apache License, Version 2.0 (the "License");
 ** you may not use this file except in compliance with the License.
 ** You may obtain a copy of the License at
 **
 **     http://www.apache.org/licenses/LICENSE-2.0
 **
 ** Unless required by applicable law or agreed to in writing, software
 ** distributed under the License is distributed on an "AS IS" BASIS,
 ** WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 ** See the License for the specific language governing permissions and
 ** limitations under the License.
 */
 
 package com.mokee.permissionsmanager.adapter;
 
 import com.mokee.permissionsmanager.R;
 import com.mokee.permissionsmanager.activities.PermissionInfoActivity;
 import com.mokee.permissionsmanager.domain.AppInfoDomain;
 
 import android.content.Context;
 import android.content.pm.PackageManager;
 import android.content.pm.PermissionInfo;
 import android.graphics.Paint;
 import android.graphics.Typeface;
 import android.text.TextUtils;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.CompoundButton.OnCheckedChangeListener;
 import android.widget.ImageView;
 import android.widget.Switch;
 import android.widget.TextView;
 
 public class PermissionAdapter extends BaseAdapter {
 
     private LayoutInflater layoutInflater;
 
     private AppInfoDomain aid;
 
     private OnCheckedChangeListener onCheckedChangeListener;
 
     private PackageManager packageManager;
 
     public PermissionAdapter(Context context, AppInfoDomain aid,
             OnCheckedChangeListener onCheckedChangeListener) {
         super();
         this.aid = aid;
         this.onCheckedChangeListener = onCheckedChangeListener;
         this.packageManager = context.getPackageManager();
         this.layoutInflater = LayoutInflater.from(context);
     }
 
     @Override
     public int getCount() {
         // TODO Auto-generated method stub
         return aid.getPackageInfo().requestedPermissions.length;
     }
 
     @Override
     public Object getItem(int position) {
         // TODO Auto-generated method stub
         return null;
     }
 
     @Override
     public long getItemId(int position) {
         // TODO Auto-generated method stub
         return 0;
     }
 
     @Override
     public View getView(int position, View convertView, ViewGroup parent) {
         // TODO Auto-generated method stub
         WrapperView wrapperView;
         // AppInfoDomain aid=aidList.get(position);
         if (convertView == null) {
             convertView = layoutInflater.inflate(R.layout.mk_per_list_item, null);
             wrapperView = new WrapperView();
             wrapperView.appName_textView = (TextView) convertView
                     .findViewById(R.item.permissionName);
             wrapperView.permissionStatus_switch = (Switch) convertView
                     .findViewById(R.item.permissionStatus);
             wrapperView.permissionInfo_textView = (TextView) convertView
                     .findViewById(R.item.permissionInfo);
             convertView.setTag(wrapperView);
         } else {
             wrapperView = (WrapperView) convertView.getTag();
         }
         String permName = aid.getPackageInfo().requestedPermissions[position];
         boolean isRevoked = aid.getRevokedPerList().contains(permName);
         wrapperView.permissionInfo_textView.setText(showPermInfo(permName));
         wrapperView.permissionStatus_switch.setOnCheckedChangeListener(null);
        wrapperView.permissionStatus_switch.setChecked(!isRevoked);
         wrapperView.permissionStatus_switch.setOnCheckedChangeListener(onCheckedChangeListener);
         wrapperView.appName_textView.setText(permName.replace(PermissionInfoActivity.OS_PER_PREFIX,
                 ""));
         wrapperView.permissionStatus_switch.setTag(wrapperView.appName_textView);
         if (isRevoked) {
             wrapperView.appName_textView.setPaintFlags(wrapperView.appName_textView.getPaintFlags()
                     | Paint.STRIKE_THRU_TEXT_FLAG);
             wrapperView.appName_textView.setTypeface(null, Typeface.BOLD_ITALIC);
         } else {
             wrapperView.appName_textView.setPaintFlags(wrapperView.appName_textView.getPaintFlags()
                     & ~Paint.STRIKE_THRU_TEXT_FLAG);
             wrapperView.appName_textView.setTypeface(null, Typeface.NORMAL);
         }
         return convertView;
     }
 
     class WrapperView {
         TextView appName_textView, totalPermission_textView,
                 showPermission_textView, permissionInfo_textView;
 
         ImageView icon_imageView;
 
         Switch permissionStatus_switch;
     }
 
     private String showPermInfo(String permName) {
         PermissionInfo pi = null;
         String info = "";
         try {
             pi = packageManager.getPermissionInfo(permName, 0);
             CharSequence description = pi.loadDescription(packageManager);
             if (!TextUtils.isEmpty(description)) {
                 info = description.toString();
             }
 
         } catch (PackageManager.NameNotFoundException e) {
 
         }
         return info;
     }
 }
