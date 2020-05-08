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
 
 package com.mokee.permissionsmanager.activities;
 
 import com.mokee.permissionsmanager.R;
 import com.mokee.permissionsmanager.adapter.PermissionAdapter;
 import com.mokee.permissionsmanager.domain.AppInfoDomain;
 
 import android.app.ActionBar;
 import android.app.Activity;
 import android.content.Intent;
 import android.content.pm.PackageManager;
 import android.graphics.Paint;
 import android.graphics.Typeface;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.widget.CompoundButton;
 import android.widget.CompoundButton.OnCheckedChangeListener;
 import android.widget.ListView;
 import android.widget.TextView;
 
 public class PermissionInfoActivity extends Activity implements OnCheckedChangeListener {
     private ListView listView;
 
     private AppInfoDomain aid;
 
     private PackageManager packageManager;
 
     private PermissionAdapter perAdapter;
 
     private int position;
 
     public static final String OS_PER_PREFIX = "android.permission.";
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         // TODO Auto-generated method stub
         super.onCreate(savedInstanceState);
         setContentView(R.layout.mk_app_list);
         packageManager = getPackageManager();
         // this.aid= getIntent().getParcelableExtra("aid");
         this.position = getIntent().getIntExtra("position", 0);
         aid = MainActivity.aidList.get(position);
         // MainActivity.aidList.remove(position);
         listView = (ListView) findViewById(R.mk.listView);
         perAdapter = new PermissionAdapter(this, aid, this);
         listView.setAdapter(perAdapter);
         setTitle(aid.appName + " " + aid.appVersionName);
         ActionBar actionBar = getActionBar();
         actionBar.setDisplayHomeAsUpEnabled(true);
         actionBar.setIcon(aid.icon);
     }
     
     public boolean onCreateOptionsMenu(Menu menu) {
         super.onCreateOptionsMenu(menu);
         menu.clear();
         return true;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
 
             case android.R.id.home:
                 // The user clicked on the Messaging icon in the action bar. Take them back from
                 // wherever they came from
                 finish();
                 return true;
         }
         return false;
     }
 
     @Override
     public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
         TextView perNameView = (TextView) buttonView.getTag();
        if (isChecked) {
             aid.revokedPerList.add(OS_PER_PREFIX + perNameView.getText());
             perNameView.setPaintFlags(perNameView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
             perNameView.setTypeface(null, Typeface.BOLD_ITALIC);
             aid.enabledNum--;
             aid.disabledNum++;
         } else {
             aid.revokedPerList.remove(OS_PER_PREFIX + perNameView.getText());
             perNameView.setPaintFlags(perNameView.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
             perNameView.setTypeface(null, Typeface.NORMAL);
             aid.disabledNum--;
             aid.enabledNum++;
         }
         try {
             String[] revoked = new String[aid.getRevokedPerList().size()];
             aid.getRevokedPerList().toArray(revoked);
             packageManager.setRevokedPermissions(aid.packageInfo.packageName, revoked);
         } catch (Exception e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
     }
 
     @Override
     public void onBackPressed() {
         // TODO Auto-generated method stub
         // MainActivity.aidList.add(aid);
         setResult(1024, new Intent().putExtra("newRevokedNum", aid.getRevokedPerList().size()));
         super.onBackPressed();
     }
 
 }
