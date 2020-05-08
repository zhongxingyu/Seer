 
 package com.google.code.geobeagle.activity;
 
 import com.google.code.geobeagle.Geocache;
 import com.google.code.geobeagle.activity.ActivityDI.ActivityTypeFactory;
 import com.google.code.geobeagle.activity.cachelist.CacheList;
 import com.google.code.geobeagle.activity.cachelist.GeocacheListController;
 import com.google.code.geobeagle.activity.main.GeoBeagle;
 import com.google.code.geobeagle.activity.main.GeocacheFromPreferencesFactory;
 
 import org.easymock.EasyMock;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.powermock.api.easymock.PowerMock;
 import org.powermock.core.classloader.annotations.PrepareForTest;
 import org.powermock.modules.junit4.PowerMockRunner;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.content.SharedPreferences;
 
 @RunWith(PowerMockRunner.class)
 @PrepareForTest( {
         Intent.class, ActivityRestorer.class
 })
 public class ActivityRestorerTest {
     @Test
     public void createCacheListIntent() throws Exception {
         ActivityTypeFactory activityTypeFactory = PowerMock
                 .createMock(ActivityDI.ActivityTypeFactory.class);
         SharedPreferences sharedPreferences = PowerMock.createMock(SharedPreferences.class);
         Activity parent = PowerMock.createMock(Activity.class);
         Intent intent = PowerMock.createMock(Intent.class);
 
         final int cacheList = ActivityType.CACHE_LIST.toInt();
         EasyMock.expect(
                 sharedPreferences.getInt(ActivitySaver.LAST_ACTIVITY, ActivityType.NONE.mIx))
                 .andReturn(cacheList);
         EasyMock.expect(activityTypeFactory.fromInt(cacheList)).andReturn(ActivityType.CACHE_LIST);
         PowerMock.expectNew(Intent.class, parent, CacheList.class).andReturn(intent);
         parent.startActivity(intent);
        parent.finish();
 
         PowerMock.replayAll();
         new ActivityRestorer(parent, null, activityTypeFactory, sharedPreferences)
                 .restore(Intent.FLAG_ACTIVITY_NEW_TASK);
         PowerMock.verifyAll();
     }
 
     @Test
     public void createNull() throws Exception {
         ActivityTypeFactory activityTypeFactory = PowerMock
                 .createMock(ActivityDI.ActivityTypeFactory.class);
         SharedPreferences sharedPreferences = PowerMock.createMock(SharedPreferences.class);
         Activity parent = PowerMock.createMock(Activity.class);
 
         EasyMock.expect(
                 sharedPreferences.getInt(ActivitySaver.LAST_ACTIVITY, ActivityType.NONE.mIx))
                 .andReturn(ActivityType.NONE.mIx);
         EasyMock.expect(activityTypeFactory.fromInt(ActivityType.NONE.mIx)).andReturn(ActivityType.NONE);
 
         PowerMock.replayAll();
         new ActivityRestorer(parent, null, activityTypeFactory, sharedPreferences)
                 .restore(Intent.FLAG_ACTIVITY_NEW_TASK);
         PowerMock.verifyAll();
     }
 
     @Test
     public void notNewTask() throws Exception {
         PowerMock.replayAll();
         new ActivityRestorer(null, null, null, null).restore(0);
         PowerMock.verifyAll();
     }
 
     @Test
     public void createSearchOnlineIntent() throws Exception {
         ActivityTypeFactory activityTypeFactory = PowerMock
                 .createMock(ActivityDI.ActivityTypeFactory.class);
         SharedPreferences sharedPreferences = PowerMock.createMock(SharedPreferences.class);
         Activity parent = PowerMock.createMock(Activity.class);
 
         final int searchOnline = ActivityType.SEARCH_ONLINE.toInt();
         EasyMock.expect(
                 sharedPreferences.getInt(ActivitySaver.LAST_ACTIVITY, ActivityType.NONE.mIx))
                 .andReturn(searchOnline);
         EasyMock.expect(activityTypeFactory.fromInt(searchOnline)).andReturn(
                 ActivityType.SEARCH_ONLINE);
 
         PowerMock.replayAll();
         new ActivityRestorer(parent, null, activityTypeFactory, sharedPreferences)
                 .restore(Intent.FLAG_ACTIVITY_NEW_TASK);
         PowerMock.verifyAll();
     }
 
     @Test
     public void createViewCache() throws Exception {
         ActivityDI.ActivityTypeFactory activityTypeFactory = PowerMock
                 .createMock(ActivityDI.ActivityTypeFactory.class);
         SharedPreferences sharedPreferences = PowerMock.createMock(SharedPreferences.class);
         Activity parent = PowerMock.createMock(Activity.class);
         GeocacheFromPreferencesFactory geocacheFromPreferencesFactory = PowerMock
                 .createMock(GeocacheFromPreferencesFactory.class);
         Geocache geocache = PowerMock.createMock(Geocache.class);
         Intent intent = PowerMock.createMock(Intent.class);
 
         final int viewCache = ActivityType.VIEW_CACHE.toInt();
         EasyMock.expect(
                 sharedPreferences.getInt(ActivitySaver.LAST_ACTIVITY, ActivityType.NONE.mIx))
                 .andReturn(viewCache);
         EasyMock.expect(activityTypeFactory.fromInt(viewCache)).andReturn(ActivityType.VIEW_CACHE);
         EasyMock.expect(geocacheFromPreferencesFactory.create(sharedPreferences)).andReturn(
                 geocache);
         PowerMock.expectNew(Intent.class, parent, GeoBeagle.class).andReturn(intent);
         EasyMock.expect(intent.putExtra("geocache", geocache)).andReturn(intent);
         EasyMock.expect(intent.setAction(GeocacheListController.SELECT_CACHE)).andReturn(intent);
         parent.startActivity(intent);
        parent.finish();
 
         PowerMock.replayAll();
         new ActivityRestorer(parent, geocacheFromPreferencesFactory, activityTypeFactory,
                 sharedPreferences).restore(Intent.FLAG_ACTIVITY_NEW_TASK);
         PowerMock.verifyAll();
     }
 }
