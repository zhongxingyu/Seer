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
 
 package android.app.cts;
 
 import android.app.ListActivity;
 import android.content.pm.ActivityInfo;
 import android.test.ActivityInstrumentationTestCase2;
 import android.view.KeyEvent;
 import android.view.View;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import android.widget.TextView;
 import dalvik.annotation.TestLevel;
 import dalvik.annotation.TestTargetClass;
 import dalvik.annotation.TestTargetNew;
 import dalvik.annotation.TestTargets;
 
 @TestTargetClass(ListActivity.class)
 public class ListActivityTest extends ActivityInstrumentationTestCase2<ListActivityTestHelper> {
     private ListActivityTestHelper mStubListActivity;
     private int mScreenOrientation;
     public ListActivityTest() {
         super("com.android.cts.stub", ListActivityTestHelper.class);
     }
 
     @Override
     protected void setUp() throws Exception {
         super.setUp();
         mStubListActivity = getActivity();
         mScreenOrientation = mStubListActivity.getRequestedOrientation();
     }
 
     protected void waitForAction() throws InterruptedException {
         final long TIMEOUT_MSEC = 20000;
         final int TIME_SLICE_MSEC = 100;
         final long endTime = System.currentTimeMillis() + TIMEOUT_MSEC;
         while (!mStubListActivity.isSubActivityFinished && System.currentTimeMillis() < endTime) {
             Thread.sleep(TIME_SLICE_MSEC);
         }
     }
 
     @TestTargets({
         @TestTargetNew(
             level = TestLevel.COMPLETE,
             method = "onListItemClick",
             args = {ListView.class, View.class, int.class, long.class}
         ),
         @TestTargetNew(
             level = TestLevel.COMPLETE,
             method = "getListView",
             args = {}
         ),
         @TestTargetNew(
             level = TestLevel.COMPLETE,
             method = "getSelectedItemId",
             args = {}
         ),
         @TestTargetNew(
             level = TestLevel.COMPLETE,
             method = "getSelectedItemPosition",
             args = {}
         ),
         @TestTargetNew(
             level = TestLevel.COMPLETE,
             method = "setListAdapter",
             args = {android.widget.ListAdapter.class}
         ),
         @TestTargetNew(
             level = TestLevel.COMPLETE,
             method = "setSelection",
             args = {int.class}
         ),
         @TestTargetNew(
             level = TestLevel.COMPLETE,
             method = "getListAdapter",
             args = {}
         ),
         @TestTargetNew(
             level = TestLevel.COMPLETE,
             method = "onContentChanged",
             args = {}
         ),
         @TestTargetNew(
             level = TestLevel.COMPLETE,
             method = "onRestoreInstanceState",
             args = {android.os.Bundle.class}
         )
     })
     public void testListActivity() throws Throwable {
         waitForAction();
         sendKeys(KeyEvent.KEYCODE_DPAD_CENTER);
         String s = (String) ((TextView) mStubListActivity.view).getText();
         int pos = mStubListActivity.itemPosition;
         long id = mStubListActivity.itemId;
         assertEquals(0, id);
         assertEquals(0, pos);
         assertEquals(ListActivityTestHelper.STRING_ITEMS[pos], s);
         assertEquals(ListActivityTestHelper.STRING_ITEMS.length,
                 mStubListActivity.listView.getCount());
         assertEquals(id, mStubListActivity.getSelectedItemId());
         assertEquals(pos, mStubListActivity.getSelectedItemPosition());
 
         sendKeys(KeyEvent.KEYCODE_DPAD_DOWN);
         sendKeys(KeyEvent.KEYCODE_DPAD_CENTER);
         s = (String) ((TextView) mStubListActivity.view).getText();
         pos = mStubListActivity.itemPosition;
         id = mStubListActivity.itemId;
         assertEquals(1, id);
         assertEquals(1, pos);
         assertEquals(ListActivityTestHelper.STRING_ITEMS[pos], s);
         assertEquals(ListActivityTestHelper.STRING_ITEMS.length,
                 mStubListActivity.listView.getCount());
         assertEquals(id, mStubListActivity.getSelectedItemId());
         assertEquals(pos, mStubListActivity.getSelectedItemPosition());
 
         final int selectPos = 2;
         assertTrue(mStubListActivity.isOnContentChangedCalled);
         runTestOnUiThread(new Runnable() {
             public void run() {
                 mStubListActivity.setSelection(selectPos);
             }
         });
         sendKeys(KeyEvent.KEYCODE_DPAD_CENTER);
         s = (String) ((TextView) mStubListActivity.view).getText();
         pos = mStubListActivity.itemPosition;
         id = mStubListActivity.itemId;
         assertEquals(ListActivityTestHelper.STRING_ITEMS[selectPos], s);
         assertEquals(ListActivityTestHelper.STRING_ITEMS.length,
                 mStubListActivity.listView.getCount());
         assertEquals(selectPos, id);
         assertEquals(selectPos, pos);
         assertEquals(selectPos, mStubListActivity.getSelectedItemId());
         assertEquals(selectPos, mStubListActivity.getSelectedItemPosition());
 
         final ArrayAdapter<String> arrayAdapter = (ArrayAdapter<String>)
                 mStubListActivity.getListAdapter();
         assertNotNull(arrayAdapter);
         final String[] str = ListActivityTestHelper.STRING_ITEMS;
         final int len = str.length;
         assertEquals(len, arrayAdapter.getCount());
         for (int i = 0; i < len; i++) {
             assertEquals(str[i], arrayAdapter.getItem(i));
         }
 
         assertNotNull(mStubListActivity.getListView());
         assertEquals(arrayAdapter, mStubListActivity.getListView().getAdapter());
         assertTrue(mStubListActivity.isOnContentChangedCalled);
         assertFalse(ListActivityTestHelper.isOnRestoreInstanceStateCalled);
         mStubListActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
         getInstrumentation().waitForIdleSync();
         mStubListActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
         getInstrumentation().waitForIdleSync();
         assertTrue(ListActivityTestHelper.isOnRestoreInstanceStateCalled);
      }
 
     @Override
     protected void tearDown() throws Exception {
         super.tearDown();
         if (mStubListActivity != null) {
             mStubListActivity.setRequestedOrientation(mScreenOrientation);
             getInstrumentation().waitForIdleSync();
         }
     }
 
 }
