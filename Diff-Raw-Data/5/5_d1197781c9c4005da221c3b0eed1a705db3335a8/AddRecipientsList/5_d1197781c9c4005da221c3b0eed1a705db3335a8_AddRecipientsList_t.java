 /*
  * Copyright (C) 2013 The CyanogenMod Project
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
 
 package com.android.mms.ui;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 
 import android.app.ActionBar;
 import android.app.ListActivity;
 import android.content.Intent;
 import android.content.res.Configuration;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Gravity;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.Button;
 import android.widget.ListView;
 import android.widget.TextView;
 
 import com.android.mms.R;
 import com.android.mms.data.Contact;
 import com.android.mms.data.GMembership;
 import com.android.mms.data.Group;
 import com.android.mms.data.PhoneNumber;
 
 public class AddRecipientsList extends ListActivity {
 
     private static final String TAG = "AddRecipientsList";
     private static final boolean DEBUG = false;
     private static final boolean DEBUGCLEANUP = true;
     private static final boolean LOCAL_LOGV = DEBUG;
 
     public static boolean mIsRunning;
 
     private AddRecipientsListAdapter mListAdapter;
     private Button mOkButton;
     private Button mCancelButton;
     private int mSavedFirstVisiblePosition = AdapterView.INVALID_POSITION;
     private int mSavedFirstItemOffset;
     private ArrayList<PhoneNumber> mPhoneNumbers;
     private ArrayList<Group> mGroups;
     private ArrayList<GMembership> mGroupMemberships;
     private ArrayList<PhoneNumber> mCheckedPhoneNumbers;
 
     // Keys for extras and icicles
     private final static String LAST_LIST_POS = "last_list_pos";
     private final static String LAST_LIST_OFFSET = "last_list_offset";
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         setContentView(R.layout.add_recipients_list_screen);
 
         // Buttons
         int bottomBarHeight = getStatusBarHeight();
         mOkButton = (Button) findViewById(R.id.ok_button);
         mOkButton.setHeight(bottomBarHeight);
         mOkButton.setEnabled(false);
         mOkButton.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 int count = mCheckedPhoneNumbers.size();
                 String[] resultData = new String[count];
                 for (int i = 0; i < count; i++) {
                     PhoneNumber phoneNumber = mCheckedPhoneNumbers.get(i);
                     if (phoneNumber.isChecked()) {
                         resultData[i] = phoneNumber.getNumber();
                     }
                 }
 
                 Intent intent = new Intent();
                 intent.putExtra("com.android.mms.ui.AddRecipients", resultData);
                 setResult(RESULT_OK, intent);
                 finish();
             }
         });
 
         mCancelButton = (Button) findViewById(R.id.cancel_button);
         mCancelButton.setHeight(bottomBarHeight);
         mCancelButton.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 finish();
             }
         });
 
         // List
         ListView listView = getListView();
         listView.setChoiceMode(ListView.CHOICE_MODE_NONE);
         listView.setFastScrollEnabled(true);
         listView.setFastScrollAlwaysVisible(true);
         listView.setDivider(null);
         listView.setDividerHeight(0);
 
         // Tell the list view which view to display when the list is empty
         listView.setEmptyView(findViewById(R.id.empty));
         listView.setOnItemClickListener(new OnItemClickListener() {
             @Override
             public void onItemClick(AdapterView<?> adapter, View view, int position, long arg) {
                 AddRecipientsListItem item  = (AddRecipientsListItem) adapter.getItemAtPosition(position);
 
                 if (item.isGroup()) {
                     Group group = item.getGroup();
                     checkGroup(group, !group.isChecked());
                 } else {
                     PhoneNumber phoneNumber = item.getPhoneNumber();
                     checkPhoneNumber(phoneNumber, !phoneNumber.isChecked());
                 }
 
                 mOkButton.setEnabled(mCheckedPhoneNumbers.size() > 0);
                 mListAdapter.notifyDataSetChanged();
             }
          });
 
         initListAdapter();
         setupActionBar();
 
         if (mListAdapter == null) {
             ((TextView)(listView.getEmptyView())).setText(R.string.no_recipients);
         }
 
         if (savedInstanceState != null) {
             mSavedFirstVisiblePosition = savedInstanceState.getInt(LAST_LIST_POS, AdapterView.INVALID_POSITION);
             mSavedFirstItemOffset = savedInstanceState.getInt(LAST_LIST_OFFSET, 0);
         } else {
             mSavedFirstVisiblePosition = AdapterView.INVALID_POSITION;
             mSavedFirstItemOffset = 0;
         }
     }
 
     @Override
     public void onSaveInstanceState(Bundle outState) {
         super.onSaveInstanceState(outState);
         outState.putInt(LAST_LIST_POS, mSavedFirstVisiblePosition);
         outState.putInt(LAST_LIST_OFFSET, mSavedFirstItemOffset);
     }
 
     @Override
     public void onPause() {
         super.onPause();
 
         // Remember where the list is scrolled to so we can restore the scroll position
         // when we come back to this activity and *after* we complete querying for the
         // contacts.
         ListView listView = getListView();
         mSavedFirstVisiblePosition = listView.getFirstVisiblePosition();
         View firstChild = listView.getChildAt(0);
         mSavedFirstItemOffset = (firstChild == null) ? 0 : firstChild.getTop();
         mIsRunning = false;
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.add_recipients_list_menu, menu);
         return super.onCreateOptionsMenu(menu);
     }
 
     @Override
     public boolean onPrepareOptionsMenu(Menu menu) {
         return super.onPrepareOptionsMenu(menu);
     }
 
     private void checkPhoneNumber(PhoneNumber phoneNumber, boolean check) {
         phoneNumber.setChecked(check);
 
         if (check) {
             if (!mCheckedPhoneNumbers.contains(phoneNumber)) {
                 mCheckedPhoneNumbers.add(phoneNumber);
             }
         } else {
             if (mCheckedPhoneNumbers.contains(phoneNumber)) {
                 mCheckedPhoneNumbers.remove(phoneNumber);
             }
 
             ArrayList<Group> phoneGroups = phoneNumber.getGroups();
             int count = phoneGroups.size();
             for (int i = 0; i < count; i++) {
                 Group group = phoneGroups.get(i);
                 if (group.isChecked()) {
                     group.setChecked(false);
                 }
             }
         }
     }
 
     private void checkGroup(Group group, boolean check) {
         group.setChecked(check);
         ArrayList<PhoneNumber> phoneNumbers = group.getPhoneNumbers();
         int count = phoneNumbers.size();
 
         for (int i = 0; i < count; i++) {
             PhoneNumber phoneNumber = phoneNumbers.get(i);
             if (phoneNumber.isDefault() || phoneNumber.isFirst()) {
                 checkPhoneNumber(phoneNumber, check);
             }
         }
     }
 
 
     public int getStatusBarHeight() {
         int result = 0;
         int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
 
         if (resourceId > 0) {
             result = getResources().getDimensionPixelSize(resourceId);
         }
         return result;
       }
 
 
     private void setupActionBar() {
         ActionBar actionBar = getActionBar();
 
         ViewGroup v = (ViewGroup)LayoutInflater.from(AddRecipientsList.this)
                 .inflate(R.layout.add_recipients_list_actionbar, null);
         actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM, ActionBar.DISPLAY_SHOW_CUSTOM);
         actionBar.setCustomView(v, new ActionBar.LayoutParams(ActionBar.LayoutParams.WRAP_CONTENT,
                 ActionBar.LayoutParams.WRAP_CONTENT, Gravity.CENTER_VERTICAL | Gravity.RIGHT));
 
         actionBar.setTitle(R.string.add_recipients);
     }
 
     private void initListAdapter() {
         mPhoneNumbers = PhoneNumber.getPhoneNumbers(this);
 
         if (mPhoneNumbers == null) {
             return;
         }
 
         mCheckedPhoneNumbers = new ArrayList<PhoneNumber>();
         mGroups = Group.getGroups(this);
         mGroupMemberships = GMembership.getGroupMemberships(this);
 
         Map<Long,ArrayList<Long>> groupIdWithContactsId = new HashMap<Long, ArrayList<Long>>();
 
         // Store GID with all its CIDs
         int GMCount = mGroupMemberships.size();
 
         for (int i = 0; i < GMCount; i++) {
             GMembership groupMembership = mGroupMemberships.get(i);
             Long gid = groupMembership.getGroupId();
             Long uid = groupMembership.getContactId();
 
             if (!groupIdWithContactsId.containsKey(gid)) {
                 groupIdWithContactsId.put(gid, new ArrayList<Long>());
             }
 
             if (!groupIdWithContactsId.get(gid).contains(uid)) {
                 groupIdWithContactsId.get(gid).add(uid);
             }
         }
 
         // For each PhoneNumber, find its GID, and add it to correct Group
         int phoneNumbersCount = mPhoneNumbers.size();
        int groupsCount = 0;
        if (mGroups != null) {
            groupsCount = mGroups.size();
        }
 
         for (int i = 0; i < phoneNumbersCount; i++) {
             PhoneNumber phoneNumber = mPhoneNumbers.get(i);
             long cid = phoneNumber.getContactId();
 
             Iterator<Long> iterator = groupIdWithContactsId.keySet().iterator();
             while (iterator.hasNext()) {
                 long gid = (Long)iterator.next();
                 if (groupIdWithContactsId.get(gid).contains(cid)) {
                     for (int j = 0; j < groupsCount; j++) {
                         Group group = mGroups.get(j);
                         if (group.getId() == gid) {
                             group.addPhoneNumber(phoneNumber);
                             phoneNumber.addGroup(group);
                         }
                     }
                 }
             }
         }
 
         ArrayList<AddRecipientsListItem> items = new ArrayList<AddRecipientsListItem>();
         for (int i = 0; i < groupsCount; i++) {
             Group group = mGroups.get(i);
             items.add(i, new AddRecipientsListItem(this, group));
         }
 
         for (int i = 0; i < phoneNumbersCount; i++) {
             PhoneNumber phoneNumber = mPhoneNumbers.get(i);
             items.add(i + groupsCount, new AddRecipientsListItem(this, phoneNumber));
         }
 
         mListAdapter = new AddRecipientsListAdapter(this, items);
         setListAdapter(mListAdapter);
     }
 
     @Override
     public void onConfigurationChanged(Configuration newConfig) {
         // We override this method to avoid restarting the entire activity when the
         // keyboard is opened (declared in AndroidManifest.xml).  Because the only
         // translatable text in this activity is "New Message", which has the full
         // width of phone to work with, localization shouldn't be a problem:
         // Test code used for various scenarios where its desirable
         // to insert a delay in responding to query complete. To use,
         // uncomment out the block below and then comment out the
         // @Override and onQueryComplete line.
 
         super.onConfigurationChanged(newConfig);
         if (DEBUG) {
             Log.v(TAG, "onConfigurationChanged: " + newConfig);
         }
     }
 
     private void log(String format, Object... args) {
         String s = String.format(format, args);
         Log.d(TAG, "[" + Thread.currentThread().getId() + "] " + s);
     }
 }
