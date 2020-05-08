 /*
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
 
 package com.google.code.geobeagle.activity.cachelist;
 
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 
 import com.google.code.geobeagle.IPausable;
import com.google.code.geobeagle.actions.CacheContextMenu;
 import com.google.code.geobeagle.activity.ActivitySaver;
 import com.google.code.geobeagle.activity.ActivityType;
 import com.google.code.geobeagle.activity.cachelist.CacheListDelegate.ImportIntentManager;
 import com.google.code.geobeagle.activity.cachelist.presenter.DistanceFormatterManager;
 import com.google.code.geobeagle.activity.cachelist.presenter.GeocacheSummaryRowInflater;
 import com.google.code.geobeagle.database.DbFrontend;
 
 import org.easymock.classextension.EasyMock;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.powermock.api.easymock.PowerMock;
 import org.powermock.core.classloader.annotations.PrepareForTest;
 import org.powermock.modules.junit4.PowerMockRunner;
 
 import android.app.Activity;
 import android.app.ListActivity;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.preference.PreferenceManager;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.ListView;
 
 @RunWith(PowerMockRunner.class)
 @PrepareForTest( {
         CacheListDelegate.class, Log.class, PreferenceManager.class
 })
 public class CacheListDelegateTest {
     @Before
     public void mockLogging() {
         PowerMock.mockStatic(Log.class);
         EasyMock.expect(
                 Log.d((String)(EasyMock.anyObject()), (String)(EasyMock
                         .anyObject()))).andReturn(0).anyTimes();
     }
 
     @Test
     public void testController() {
         MenuItem menuItem = PowerMock.createMock(MenuItem.class);
        GeocacheListController geocacheListController = PowerMock.createMock(GeocacheListController.class);
 
         EasyMock.expect(geocacheListController.onOptionsItemSelected(menuItem))
                 .andReturn(true);
 
         PowerMock.replayAll();
        CacheListDelegate cacheListDelegate = new CacheListDelegate(null, null, geocacheListController, null, null, null, null, null, null, null, null, null, null, null);
        assertTrue(cacheListDelegate.onOptionsItemSelected(menuItem));
         PowerMock.verifyAll();
     }
 
     @Test
     public void testImportAlreadyTriggered() {
         Activity activity = PowerMock.createMock(Activity.class);
         Intent intent = PowerMock.createMock(Intent.class);
 
         EasyMock.expect(activity.getIntent()).andReturn(intent);
         EasyMock.expect(intent.getAction()).andReturn(
                 "android.intent.action.VIEW");
         EasyMock.expect(
                 intent.getBooleanExtra(
                         ImportIntentManager.INTENT_EXTRA_IMPORT_TRIGGERED,
                         false)).andReturn(true);
 
         PowerMock.replayAll();
         assertFalse(new ImportIntentManager(activity).isImport());
         PowerMock.verifyAll();
     }
 
     @Test
     public void testImportEmptyAction() {
         Activity activity = PowerMock.createMock(Activity.class);
         Intent intent = PowerMock.createMock(Intent.class);
 
         EasyMock.expect(activity.getIntent()).andReturn(intent);
         EasyMock.expect(intent.getAction()).andReturn(null);
 
         PowerMock.replayAll();
         assertFalse(new ImportIntentManager(activity).isImport());
         PowerMock.verifyAll();
     }
 
     @Test
     public void testImportNotView() {
         Activity activity = PowerMock.createMock(Activity.class);
         Intent intent = PowerMock.createMock(Intent.class);
 
         EasyMock.expect(activity.getIntent()).andReturn(intent);
         EasyMock.expect(intent.getAction()).andReturn(
                 "android.intent.action.EDIT");
 
         PowerMock.replayAll();
         assertFalse(new ImportIntentManager(activity).isImport());
         PowerMock.verifyAll();
     }
 
     @Test
     public void testImportNullIntent() {
         Activity activity = PowerMock.createMock(Activity.class);
 
         EasyMock.expect(activity.getIntent()).andReturn(null);
 
         PowerMock.replayAll();
         assertFalse(new ImportIntentManager(activity).isImport());
         PowerMock.verifyAll();
     }
 
     @Test
     public void testImportTrue() {
         Activity activity = PowerMock.createMock(Activity.class);
         Intent intent = PowerMock.createMock(Intent.class);
 
         EasyMock.expect(activity.getIntent()).andReturn(intent);
         EasyMock.expect(intent.getAction()).andReturn(
                 "android.intent.action.VIEW");
 
         final boolean booleanExtra = intent.getBooleanExtra(
                 ImportIntentManager.INTENT_EXTRA_IMPORT_TRIGGERED, false);
         EasyMock.expect(booleanExtra).andReturn(false);
 
         final Intent putExtra = intent.putExtra(
                 ImportIntentManager.INTENT_EXTRA_IMPORT_TRIGGERED, true);
         EasyMock.expect(putExtra).andReturn(intent);
 
         PowerMock.replayAll();
         assertTrue(new ImportIntentManager(activity).isImport());
         PowerMock.verifyAll();
     }
 
     @Test
     public void testOnContextItemSelected() {
         MenuItem menuItem = PowerMock.createMock(MenuItem.class);
         GeocacheListController geocacheListController = PowerMock
                 .createStrictMock(GeocacheListController.class);
        CacheContextMenu menuCreater = PowerMock.createMock(CacheContextMenu.class);
        
        EasyMock.expect(menuCreater.onContextItemSelected(menuItem)).andReturn(true);
 
         PowerMock.replayAll();
        assertTrue(new CacheListDelegate(null, null, geocacheListController, null, null,
                null, menuCreater, null, null, null, null, null, null, null)
                .onContextItemSelected(menuItem));
         PowerMock.verifyAll();
     }
 
     @Test
     public void testOnCreateOptionsMenu() {
         Menu menu = PowerMock.createMock(Menu.class);
         GeocacheListController geocacheListController = PowerMock
                 .createStrictMock(GeocacheListController.class);
 
         EasyMock.expect(geocacheListController.onCreateOptionsMenu(menu))
                 .andReturn(true);
 
         PowerMock.replayAll();
         assertTrue(new CacheListDelegate(null, null, geocacheListController,
                 null, null, null, null, null, null, null, null, null, null,
                 null).onCreateOptionsMenu(menu));
         PowerMock.verifyAll();
     }
 
     @Test
     public void testOnListItemClick() {
         GeocacheListController geocacheListController = PowerMock
                 .createStrictMock(GeocacheListController.class);
         ListView listView = PowerMock.createMock(ListView.class);
         View view = PowerMock.createMock(View.class);
 
         geocacheListController.onListItemClick(listView, view, 28, 42);
 
         PowerMock.replayAll();
         new CacheListDelegate(null, null, geocacheListController, null, null,
                 null, null, null, null, null, null, null, null, null)
                 .onListItemClick(listView, view, 28, 42);
         PowerMock.verifyAll();
     }
 
     @Test
     public void testOnMenuOpened() {
         GeocacheListController geocacheListController = PowerMock
                 .createStrictMock(GeocacheListController.class);
         Menu menu = PowerMock.createMock(Menu.class);
 
         EasyMock.expect(geocacheListController.onMenuOpened(27, menu))
                 .andReturn(true);
 
         PowerMock.replayAll();
         new CacheListDelegate(null, null, geocacheListController, null, null,
                 null, null, null, null, null, null, null, null, null)
                 .onMenuOpened(27, menu);
         PowerMock.verifyAll();
     }
 
     @Test
     public void testOnOptionsItemSelected() {
         GeocacheListController geocacheListController = PowerMock
                 .createStrictMock(GeocacheListController.class);
         MenuItem menuItem = PowerMock.createMock(MenuItem.class);
 
         EasyMock.expect(geocacheListController.onOptionsItemSelected(menuItem))
                 .andReturn(true);
 
         PowerMock.replayAll();
         new CacheListDelegate(null, null, geocacheListController, null, null,
                 null, null, null, null, null, null, null, null, null)
                 .onOptionsItemSelected(menuItem);
         PowerMock.verifyAll();
     }
 
     @Test
     public void testOnPause() {
         IPausable pausable = PowerMock.createMock(IPausable.class);
         GeocacheListController geocacheListController = PowerMock
                 .createMock(GeocacheListController.class);
         ActivitySaver activitySaver = PowerMock.createMock(ActivitySaver.class);
         DbFrontend dbFrontend = PowerMock.createMock(DbFrontend.class);
         IPausable pausables[] = {
             pausable
         };
 
         pausable.onPause();
         geocacheListController.onPause();
         activitySaver.save(ActivityType.CACHE_LIST);
         dbFrontend.closeDatabase();
 
         PowerMock.replayAll();
         new CacheListDelegate(null, activitySaver, geocacheListController,
                 dbFrontend, null, null, null, null, null, null, null, null,
                 null, pausables).onPause();
         PowerMock.verifyAll();
     }
 
     @Test
     public void testOnResume() {
         DistanceFormatterManager distanceFormatterManager = PowerMock
                 .createMock(DistanceFormatterManager.class);
         GeocacheListController controller = PowerMock
                 .createMock(GeocacheListController.class);
         ImportIntentManager importIntentManager = PowerMock
                 .createMock(ImportIntentManager.class);
         ListActivity context = PowerMock.createMock(ListActivity.class);
         SharedPreferences sharedPreferences = PowerMock
                 .createMock(SharedPreferences.class);
         PowerMock.mockStatic(PreferenceManager.class);
         GeocacheSummaryRowInflater geocacheSummaryRowInflater = PowerMock
                 .createMock(GeocacheSummaryRowInflater.class);
         IPausable pausable = PowerMock.createMock(IPausable.class);
         IPausable pausables[] = {
             pausable
         };
 
         distanceFormatterManager.setFormatter();
         EasyMock.expect(PreferenceManager.getDefaultSharedPreferences(context))
                 .andReturn(sharedPreferences);
         EasyMock
                 .expect(sharedPreferences.getBoolean("absolute-bearing", false))
                 .andReturn(true);
         geocacheSummaryRowInflater.setBearingFormatter(true);
         controller.onResume(true);
         pausable.onResume();
         EasyMock.expect(importIntentManager.isImport()).andReturn(true);
 
         PowerMock.replayAll();
         new CacheListDelegate(importIntentManager, null, controller, null,
                 null, null, null, null, geocacheSummaryRowInflater, context,
                 null, distanceFormatterManager, null, pausables).onResume();
         PowerMock.verifyAll();
     }
 }
