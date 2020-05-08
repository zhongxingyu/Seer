 /*
  * Copyright (C) 2011 The Android Open Source Project Licensed under the Apache
  * License, Version 2.0 (the "License"); you may not use this file except in
  * compliance with the License. You may obtain a copy of the License at
  * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
  * or agreed to in writing, software distributed under the License is
  * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied. See the License for the specific language
  * governing permissions and limitations under the License.
  */
 package com.kii.demo.sync.ui;
 
 import java.util.ArrayList;
 
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.os.Bundle;
 import android.support.v4.app.DialogFragment;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentPagerAdapter;
 import android.support.v4.view.ViewPager;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.TabHost;
 import android.widget.TabWidget;
 
 import com.kii.cloud.sync.BackupService;
 import com.kii.cloud.sync.KiiSyncClient;
 import com.kii.demo.sync.R;
 import com.kii.demo.sync.ui.fragments.FileListFragment;
 import com.kii.demo.sync.ui.fragments.KiiFileFragment;
 import com.kii.demo.sync.utils.Utils;
 
 /**
  * Demonstrates combining a TabHost with a ViewPager to implement a tab UI that
  * switches between tabs and also allows the user to perform horizontal flicks
  * to move between the tabs.
  */
 public class FragmentTabsPager extends FragmentActivity {
     private static FileListFragment mFileFragment = null;
     private static KiiFileFragment mKiiFragment = null;
 
     public static class AlertDialogFragment extends DialogFragment {
         private int id;
 
         public static AlertDialogFragment newInstance(int id) {
             AlertDialogFragment frag = new AlertDialogFragment();
             frag.id = id;
             return frag;
         }
 
         @Override
         public Dialog onCreateDialog(Bundle savedInstanceState) {
             switch (id) {
                 case FileListFragment.DIALOG_UPDATE:
                     AlertDialog.Builder builder = new AlertDialog.Builder(
                             getActivity());
                     builder.setMessage(
                             FileListFragment.getLocalChanges().size()
                                     + " file(s) has changed.")
                             .setCancelable(false)
                             .setPositiveButton("Update Now",
                                     new DialogInterface.OnClickListener() {
                                         @Override
                                         public void onClick(
                                                 DialogInterface dialog, int id) {
                                             updateFileChange();
                                         }
                                     })
                             .setNegativeButton("Cancel",
                                     new DialogInterface.OnClickListener() {
                                         @Override
                                         public void onClick(
                                                 DialogInterface dialog, int id) {
                                             dialog.cancel();
                                         }
                                     });
                     AlertDialog dialog = builder.create();
                     return dialog;
                 default:
                     break;
             }
             return null;
         }
 
         private void updateFileChange() {
             KiiSyncClient client = KiiSyncClient.getInstance(getActivity());
             client.updateBody(FileListFragment.getLocalChanges());
             Utils.startSync(getActivity(), BackupService.ACTION_REFRESH);
         }
     }
 
     TabHost mTabHost;
     ViewPager mViewPager;
     TabsAdapter mTabsAdapter;
     private static final String TAG_DEVICE = "device";
     private static final String TAG_CLOUD = "cloud";
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         setContentView(R.layout.fragment_tabs_pager);
         mTabHost = (TabHost) findViewById(android.R.id.tabhost);
         mTabHost.setup();
 
         mViewPager = (ViewPager) findViewById(R.id.pager);
 
         mTabsAdapter = new TabsAdapter(this, mTabHost, mViewPager);
 
         mTabsAdapter.addTab(
                 mTabHost.newTabSpec(TAG_DEVICE).setIndicator(
                         getString(R.string.tab_device),
                         getResources().getDrawable(R.drawable.device)),
                 FileListFragment.class, null);
         mTabsAdapter.addTab(
                 mTabHost.newTabSpec(TAG_CLOUD).setIndicator(
                         getString(R.string.tab_cloud),
                         getResources().getDrawable(R.drawable.cloud)),
                 KiiFileFragment.class, null);
         if (savedInstanceState != null) {
             mTabHost.setCurrentTabByTag(savedInstanceState.getString("tab"));
         }
     }
 
     @Override
     protected void onSaveInstanceState(Bundle outState) {
         super.onSaveInstanceState(outState);
         outState.putString("tab", mTabHost.getCurrentTabTag());
     }
 
     /**
      * This is a helper class that implements the management of tabs and all
      * details of connecting a ViewPager with associated TabHost. It relies on a
      * trick. Normally a tab host has a simple API for supplying a View or
      * Intent that each tab will show. This is not sufficient for switching
      * between pages. So instead we make the content part of the tab host 0dp
      * high (it is not shown) and the TabsAdapter supplies its own dummy view to
      * show as the tab content. It listens to changes in tabs, and takes care of
      * switch to the correct paged in the ViewPager whenever the selected tab
      * changes.
      */
     public static class TabsAdapter extends FragmentPagerAdapter implements
             TabHost.OnTabChangeListener, ViewPager.OnPageChangeListener {
         private final Context mContext;
         private final TabHost mTabHost;
         private final ViewPager mViewPager;
         private final ArrayList<TabInfo> mTabs = new ArrayList<TabInfo>();
 
         static final class TabInfo {
             private final String tag;
             private final Class<?> clss;
             private final Bundle args;
 
             TabInfo(String _tag, Class<?> _class, Bundle _args) {
                 tag = _tag;
                 clss = _class;
                 args = _args;
             }
 
             public String getTag() {
                 return tag;
             }
         }
 
         static class DummyTabFactory implements TabHost.TabContentFactory {
             private final Context mContext;
 
             public DummyTabFactory(Context context) {
                 mContext = context;
             }
 
             @Override
             public View createTabContent(String tag) {
                 View v = new View(mContext);
                 v.setMinimumWidth(0);
                 v.setMinimumHeight(0);
                 return v;
             }
         }
 
         public TabsAdapter(FragmentActivity activity, TabHost tabHost,
                 ViewPager pager) {
             super(activity.getSupportFragmentManager());
             mContext = activity;
             mTabHost = tabHost;
             mViewPager = pager;
             mTabHost.setOnTabChangedListener(this);
             mViewPager.setAdapter(this);
             mViewPager.setOnPageChangeListener(this);
         }
 
         public void addTab(TabHost.TabSpec tabSpec, Class<?> clss, Bundle args) {
             tabSpec.setContent(new DummyTabFactory(mContext));
             String tag = tabSpec.getTag();
             TabInfo info = new TabInfo(tag, clss, args);
             mTabs.add(info);
             mTabHost.addTab(tabSpec);
             notifyDataSetChanged();
         }
 
         @Override
         public int getCount() {
             return mTabs.size();
         }
 
         @Override
         public Fragment getItem(int position) {
             TabInfo info = mTabs.get(position);
             Fragment f = Fragment.instantiate(mContext, info.clss.getName(),
                     info.args);
             if (info.clss.getName().contentEquals(
                     FileListFragment.class.getName())) {
                 mFileFragment = (FileListFragment) f;
             } else if (info.clss.getName().contentEquals(
                     KiiFileFragment.class.getName())) {
                 mKiiFragment = (KiiFileFragment) f;
             }
             return f;
         }
 
         @Override
         public void onTabChanged(String tabId) {
             int position = mTabHost.getCurrentTab();
             mViewPager.setCurrentItem(position);
             if ((tabId != null)) {
                 if ((tabId.contentEquals(TAG_DEVICE))
                         && (mFileFragment != null)) {
                    mFileFragment.refreshFilesList();
                } else if((tabId.contentEquals(TAG_CLOUD))
                         && (mKiiFragment != null)) {
                    //TODO: if necessary, do KiiFragment.refresh
                 }
             }
         }
 
         @Override
         public void onPageScrolled(int position, float positionOffset,
                 int positionOffsetPixels) {
         }
 
         @Override
         public void onPageSelected(int position) {
             // Unfortunately when TabHost changes the current tab, it kindly
             // also takes care of putting focus on it when not in touch mode.
             // The jerk.
             // This hack tries to prevent this from pulling focus out of our
             // ViewPager.
             TabWidget widget = mTabHost.getTabWidget();
             int oldFocusability = widget.getDescendantFocusability();
             widget.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
             mTabHost.setCurrentTab(position);
             widget.setDescendantFocusability(oldFocusability);
         }
 
         @Override
         public void onPageScrollStateChanged(int state) {
         }
     }
 
 }
