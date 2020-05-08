 // 
 // MobeelizerApplicationTest.java
 // 
 // Copyright (C) 2012 Mobeelizer Ltd. All Rights Reserved.
 //
 // Mobeelizer SDK is free software; you can redistribute it and/or modify it 
 // under the terms of the GNU Affero General Public License as published by 
 // the Free Software Foundation; either version 3 of the License, or (at your
 // option) any later version.
 //
 // This program is distributed in the hope that it will be useful, but WITHOUT
 // ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or 
 // FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License
 // for more details.
 //
 // You should have received a copy of the GNU Affero General Public License 
 // along with this program; if not, write to the Free Software Foundation, Inc., 
 // 51 Franklin St, Fifth Floor, Boston, MA  02110-1301 USA
 // 
 
 package com.mobeelizer.mobile.android;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertNull;
 import static org.junit.Assert.assertSame;
 import static org.junit.Assert.assertTrue;
 import static org.mockito.Matchers.any;
 import static org.mockito.Matchers.anyBoolean;
 import static org.mockito.Matchers.anySet;
 import static org.mockito.Matchers.anyString;
 import static org.mockito.Matchers.eq;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.never;
 import static org.mockito.Mockito.times;
 import static org.mockito.Mockito.verify;
 import static org.mockito.Mockito.when;
 import static org.powermock.api.mockito.PowerMockito.verifyNew;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.HashSet;
 import java.util.Set;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.powermock.api.mockito.PowerMockito;
 import org.powermock.core.classloader.annotations.PrepareForTest;
 import org.powermock.modules.junit4.PowerMockRunner;
 
 import android.content.Context;
 import android.content.pm.ApplicationInfo;
 import android.content.pm.PackageManager;
 import android.content.res.AssetManager;
 import android.os.Bundle;
 import android.os.Environment;
 import android.telephony.TelephonyManager;
 import android.util.Log;
 
 import com.mobeelizer.java.api.MobeelizerModel;
 import com.mobeelizer.java.definition.MobeelizerApplicationDefinition;
 import com.mobeelizer.java.definition.MobeelizerDefinitionConverter;
 import com.mobeelizer.java.definition.MobeelizerDefinitionParser;
 import com.mobeelizer.mobile.android.api.MobeelizerLoginStatus;
 import com.mobeelizer.mobile.android.api.MobeelizerSyncStatus;
 
 @RunWith(PowerMockRunner.class)
 @PrepareForTest({ MobeelizerApplication.class, Bundle.class, AssetManager.class, MobeelizerDefinitionParser.class, Log.class,
        Environment.class, MobeelizerSyncServicePerformer.class, MobeelizerDefinitionConverter.class })
 public class MobeelizerApplicationTest {
 
     private MobeelizerApplication application;
 
     private MobeelizerApplicationDefinition definition;
 
     private Bundle bundle;
 
     private Mobeelizer mobeelizer;
 
     private AssetManager assetManager;
 
     private InputStream definitionXmlAsset;
 
     private MobeelizerRealConnectionManager connectionManager;
 
     private MobeelizerDatabaseImpl database;
 
     private MobeelizerDefinitionConverter definitionManager;
 
     private Set<MobeelizerModel> models;
 
     private TelephonyManager telephonyManager;
 
     private MobeelizerInternalDatabase internalDatabase;
 
     private MobeelizerSyncServicePerformer syncPerformer;
 
     @Before
     public void init() throws Exception {
         PowerMockito.mockStatic(Log.class);
         PowerMockito.when(Log.class, "i", anyString(), anyString()).thenReturn(0);
 
         PowerMockito.mockStatic(Environment.class);
         PowerMockito.when(Environment.class, "getExternalStorageState").thenReturn(Environment.MEDIA_MOUNTED);
 
         mobeelizer = mock(Mobeelizer.class);
 
         PackageManager packageManager = mock(PackageManager.class);
         ApplicationInfo applicationInfo = mock(ApplicationInfo.class);
 
         bundle = PowerMockito.mock(Bundle.class);
 
         applicationInfo.metaData = bundle;
 
         telephonyManager = PowerMockito.mock(TelephonyManager.class);
         when(mobeelizer.getSystemService(Context.TELEPHONY_SERVICE)).thenReturn(telephonyManager);
         when(telephonyManager.getDeviceId()).thenReturn("deviceIdentifier");
 
         when(bundle.getString("MOBEELIZER_DEVICE")).thenReturn("device");
         when(bundle.getString("MOBEELIZER_URL")).thenReturn("url");
         when(bundle.getString("MOBEELIZER_DEFINITION_ASSET")).thenReturn("definition.xml");
         when(bundle.getString("MOBEELIZER_DB_NAME")).thenReturn("databaseName");
         when(bundle.getString("MOBEELIZER_PACKAGE")).thenReturn("com.mobeelizer.orm");
         when(bundle.getString("MOBEELIZER_DEVELOPMENT_ROLE")).thenReturn(null);
         when(bundle.getString("MOBEELIZER_MODE")).thenReturn("production");
         when(bundle.getInt("MOBEELIZER_DB_VERSION", 1)).thenReturn(10);
 
         when(packageManager.getApplicationInfo("packageName", PackageManager.GET_META_DATA)).thenReturn(applicationInfo);
 
         when(mobeelizer.getPackageManager()).thenReturn(packageManager);
         when(mobeelizer.getPackageName()).thenReturn("packageName");
 
         definitionXmlAsset = mock(InputStream.class);
 
         assetManager = PowerMockito.mock(AssetManager.class);
         when(assetManager.open("definition.xml")).thenReturn(definitionXmlAsset);
 
         when(mobeelizer.getAssets()).thenReturn(assetManager);
 
         definition = mock(MobeelizerApplicationDefinition.class);
         when(definition.getApplication()).thenReturn("application");
         when(definition.getVendor()).thenReturn("vendor");
         when(definition.getDigest()).thenReturn("digest");
 
         MobeelizerDefinitionParser xmlUtil = mock(MobeelizerDefinitionParser.class);
 
         PowerMockito.whenNew(MobeelizerDefinitionParser.class).withNoArguments().thenReturn(xmlUtil);
 
         PowerMockito.mockStatic(MobeelizerDefinitionParser.class);
         PowerMockito.when(MobeelizerDefinitionParser.class, "parse", definitionXmlAsset).thenReturn(definition);
 
         connectionManager = mock(MobeelizerRealConnectionManager.class);
 
         PowerMockito.whenNew(MobeelizerRealConnectionManager.class).withArguments(any(MobeelizerApplication.class))
                 .thenReturn(connectionManager);
 
         definitionManager = mock(MobeelizerDefinitionConverter.class);
 
         PowerMockito.whenNew(MobeelizerDefinitionConverter.class).withNoArguments().thenReturn(definitionManager);
 
         models = new HashSet<MobeelizerModel>();
 
         when(definitionManager.convert(definition, "com.mobeelizer.orm", "role")).thenReturn(models);
 
         internalDatabase = mock(MobeelizerInternalDatabase.class);
 
         PowerMockito.whenNew(MobeelizerInternalDatabase.class).withArguments(any(MobeelizerApplication.class))
                 .thenReturn(internalDatabase);
 
         database = mock(MobeelizerDatabaseImpl.class);
 
         application = MobeelizerApplication.createApplication(mobeelizer);
 
         MobeelizerLoginResponse loginResponse = new MobeelizerLoginResponse(MobeelizerLoginStatus.OK, "0000", "role", false);
 
         when(connectionManager.login()).thenReturn(loginResponse);
         when(connectionManager.isNetworkAvailable()).thenReturn(true);
 
         PowerMockito.whenNew(MobeelizerDatabaseImpl.class).withArguments(eq(application), eq(models)).thenReturn(database);
 
         syncPerformer = PowerMockito.mock(MobeelizerSyncServicePerformer.class);
         PowerMockito.whenNew(MobeelizerSyncServicePerformer.class).withArguments(any(MobeelizerApplication.class), anyBoolean())
                 .thenReturn(syncPerformer);
     }
 
     @Test(expected = IllegalStateException.class)
     public void shouldCreateFailIfExternalStorageReadOnly() throws Exception {
         // given
         PowerMockito.when(Environment.class, "getExternalStorageState").thenReturn(Environment.MEDIA_MOUNTED_READ_ONLY);
 
         // when
         MobeelizerApplication.createApplication(mobeelizer);
     }
 
     @Test(expected = IllegalStateException.class)
     public void shouldCreateFailIfExternalStoragenotMounted() throws Exception {
         // given
         PowerMockito.when(Environment.class, "getExternalStorageState").thenReturn(Environment.MEDIA_UNMOUNTED);
 
         // when
         MobeelizerApplication.createApplication(mobeelizer);
     }
 
     @Test(expected = IllegalStateException.class)
     public void shouldCreateFailIfDeviceNotSet() throws Exception {
         // given
         when(bundle.getString("MOBEELIZER_DEVICE")).thenReturn(null);
 
         // when
         MobeelizerApplication.createApplication(mobeelizer);
     }
 
     @Test(expected = IllegalStateException.class)
     public void shouldCreateFailIfPackageNotSet() throws Exception {
         // given
         when(bundle.getString("MOBEELIZER_PACKAGE")).thenReturn(null);
 
         // when
         MobeelizerApplication.createApplication(mobeelizer);
     }
 
     @Test(expected = IllegalStateException.class)
     public void shouldCreateFailIfDeviceIdNotSet() throws Exception {
         // given
         when(telephonyManager.getDeviceId()).thenReturn(null);
 
         // when
         MobeelizerApplication.createApplication(mobeelizer);
     }
 
     @Test
     public void shouldCreateWithDefaultDefinitionXml() throws Exception {
         // given
         when(bundle.getString("MOBEELIZER_DEFINITION_ASSET")).thenReturn(null);
         when(assetManager.open("application.xml")).thenReturn(definitionXmlAsset);
 
         // when
         MobeelizerApplication application = MobeelizerApplication.createApplication(mobeelizer);
 
         // then
         assertEquals(definition, application.getDefinition());
     }
 
     @Test(expected = IllegalStateException.class)
     public void shouldCreateFailIfDefinitionXmlAssetNotFound() throws Exception {
         // given
         when(bundle.getString("MOBEELIZER_DEFINITION_ASSET")).thenReturn(null);
         when(assetManager.open("application.xml")).thenThrow(new IOException("message"));
 
         // when
         MobeelizerApplication.createApplication(mobeelizer);
     }
 
     @Test
     public void shouldCreateWithDefaultUrl() throws Exception {
         // given
         when(bundle.getString("MOBEELIZER_URL")).thenReturn(null);
 
         // when
         MobeelizerApplication application = MobeelizerApplication.createApplication(mobeelizer);
 
         // then
         assertNull(application.getUrl());
     }
 
     @Test
     public void shouldCreateWithDefaultUrlInTestMode() throws Exception {
         // given
         when(bundle.getString("MOBEELIZER_URL")).thenReturn(null);
         when(bundle.getString("MOBEELIZER_MODE")).thenReturn("test");
 
         // when
         MobeelizerApplication application = MobeelizerApplication.createApplication(mobeelizer);
 
         // then
         assertNull(application.getUrl());
     }
 
     @Test
     public void shouldCreateWithDefaultDatabaseVersion() throws Exception {
         // given
         when(bundle.getInt("MOBEELIZER_DB_VERSION", 1)).thenReturn(1);
 
         // when
         MobeelizerApplication application = MobeelizerApplication.createApplication(mobeelizer);
 
         // then
         assertEquals(1, application.getDatabaseVersion());
     }
 
     @Test
     public void shouldCreateInDevelopmentMode() throws Exception {
         // given
         when(bundle.getString("MOBEELIZER_DEVELOPMENT_ROLE")).thenReturn("devRole");
         when(bundle.getString("MOBEELIZER_MODE")).thenReturn("development");
 
         MobeelizerDevelopmentConnectionManager connectionManager = new MobeelizerDevelopmentConnectionManager("devRole");
         PowerMockito.whenNew(MobeelizerDevelopmentConnectionManager.class).withArguments("devRole").thenReturn(connectionManager);
 
         when(definitionManager.convert(definition, "com.mobeelizer.orm", "devRole")).thenReturn(models);
 
         // when
         MobeelizerApplication application = MobeelizerApplication.createApplication(mobeelizer);
 
         // then
         PowerMockito.whenNew(MobeelizerDatabaseImpl.class).withArguments(eq(application), eq(models)).thenReturn(database);
         verifyNew(MobeelizerDevelopmentConnectionManager.class).withArguments("devRole");
         assertEquals(MobeelizerLoginStatus.OK, application.login("anyInstance", "anyUser", "anyPassword"));
         assertTrue(application.isLoggedIn());
         assertEquals(MobeelizerSyncStatus.NONE, application.checkSyncStatus());
         application.sync();
         application.syncAll();
         application.logout();
     }
 
     @Test
     public void shouldCreate() throws Exception {
         // then
         verifyNew(MobeelizerRealConnectionManager.class).withArguments(application);
         assertEquals("vendor", application.getVendor());
         assertEquals("application", application.getApplication());
         assertEquals("digest", application.getVersionDigest());
         assertEquals("device", application.getDevice());
         assertEquals("deviceIdentifier", application.getDeviceIdentifier());
         assertEquals("url", application.getUrl());
         assertEquals(10, application.getDatabaseVersion());
         assertEquals(definition, application.getDefinition());
         assertEquals(internalDatabase, application.getInternalDatabase());
         assertEquals(connectionManager, application.getConnectionManager());
     }
 
     @Test
     public void shouldLoginFail() throws Exception {
         // given
         MobeelizerLoginResponse loginResponse = new MobeelizerLoginResponse(MobeelizerLoginStatus.AUTHENTICATION_FAILURE);
         when(connectionManager.login()).thenReturn(loginResponse);
 
         // when
         MobeelizerLoginStatus status = application.login("instance", "user", "other-password");
 
         // then
         assertEquals(MobeelizerLoginStatus.AUTHENTICATION_FAILURE, status);
         verifyNew(MobeelizerDatabaseImpl.class, never()).withArguments(eq(application), anySet());
     }
 
     @Test
     public void shouldLogin() throws Exception {
         // when
         MobeelizerLoginStatus status = application.login("instance", "user", "password");
         application.setSyncStatus(MobeelizerSyncStatus.FINISHED_WITH_SUCCESS);
 
         // then
         assertEquals(MobeelizerLoginStatus.OK, status);
         assertTrue(application.isLoggedIn());
         assertEquals("user", application.getUser());
         assertEquals("instance", application.getInstance());
         assertEquals("password", application.getPassword());
         assertSame(database, application.getDatabase());
         verify(database).open();
         verify(syncPerformer, never()).sync();
     }
 
     @Test
     public void shouldLoginWithInitialSync() throws Exception {
         // when
         MobeelizerLoginResponse loginResponse = new MobeelizerLoginResponse(MobeelizerLoginStatus.OK, "0000", "role", true);
         when(connectionManager.login()).thenReturn(loginResponse);
         when(connectionManager.isNetworkAvailable()).thenReturn(true);
 
         MobeelizerLoginStatus status = application.login("instance", "user", "password");
         application.setSyncStatus(MobeelizerSyncStatus.FINISHED_WITH_SUCCESS);
 
         // then
         assertEquals(MobeelizerLoginStatus.OK, status);
         assertTrue(application.isLoggedIn());
         assertEquals("user", application.getUser());
         assertEquals("instance", application.getInstance());
         assertEquals("password", application.getPassword());
         assertSame(database, application.getDatabase());
         verify(database).open();
         verify(syncPerformer).sync();
     }
 
     @Test
     public void shouldLogoutBeforeLogin() throws Exception {
         // given
         application.login("instance", "user", "password");
 
         // when
         MobeelizerLoginStatus status = application.login("instance", "user", "password");
 
         // then
         assertEquals(MobeelizerLoginStatus.OK, status);
         assertTrue(application.isLoggedIn());
         assertEquals("user", application.getUser());
         assertEquals("instance", application.getInstance());
         assertSame(database, application.getDatabase());
         verify(database).close();
         verify(database, times(2)).open();
     }
 
     @Test
     public void shouldLogout() throws Exception {
         // given
         application.login("instance", "user", "password");
 
         // when
         application.logout();
 
         // then
         assertFalse(application.isLoggedIn());
         verify(database).close();
     }
 
     @Test
     public void shouldIgnoreLogoutWhenNotLoggedIn() throws Exception {
         // when
         application.logout();
 
         // then
         assertFalse(application.isLoggedIn());
         verify(database, never()).close();
     }
 
     @Test(expected = IllegalStateException.class)
     public void shouldFailOnGettingDatabaseWhenNotLoggedIn() throws Exception {
         // when
         application.getDatabase();
     }
 
     @Test(expected = IllegalStateException.class)
     public void shouldFailOnSyncWhenNotLoggedIn() throws Exception {
         // when
         application.sync();
     }
 
     @Test(expected = IllegalStateException.class)
     public void shouldFailOnSyncAllWhenNotLoggedIn() throws Exception {
         // when
         application.syncAll();
     }
 
     @Test(expected = IllegalStateException.class)
     public void shouldFailOnCheckSyncStatusWhenNotLoggedIn() throws Exception {
         // when
         application.checkSyncStatus();
     }
 
     @Test
     public void shouldCheckSyncStatus() throws Exception {
         // given
         application.login("instance", "user", "password");
 
         // when
         MobeelizerSyncStatus status = application.checkSyncStatus();
 
         // then
         assertEquals(MobeelizerSyncStatus.NONE, status);
     }
 
     @Test
     public void shouldChangeSyncStatus() throws Exception {
         // given
         application.login("instance", "user", "password");
 
         // when
         application.setSyncStatus(MobeelizerSyncStatus.FILE_RECEIVED);
 
         // then
         assertEquals(MobeelizerSyncStatus.FILE_RECEIVED, application.checkSyncStatus());
     }
 
     @Test
     public void shouldIgnoreSyncWhenAlreadyRunning() throws Exception {
         // given
         application.login("instance", "user", "password");
         application.setSyncStatus(MobeelizerSyncStatus.FILE_RECEIVED);
 
         // when
         application.sync();
 
         // then
         verify(syncPerformer, never()).sync();
     }
 
     @Test
     public void shouldIgnoreSyncAllWhenAlreadyRunning() throws Exception {
         // given
         application.login("instance", "user", "password");
         application.setSyncStatus(MobeelizerSyncStatus.FILE_CREATED);
 
         // when
         application.syncAll();
 
         // then
         verify(syncPerformer, never()).sync();
     }
 
     @Test
     public void shouldSync() throws Exception {
         // given
         application.login("instance", "user", "password");
         application.setSyncStatus(MobeelizerSyncStatus.NONE);
 
         // when
         application.sync();
 
         // then
         verify(syncPerformer).sync();
     }
 
     @Test
     public void shouldSync2() throws Exception {
         // given
         application.login("instance", "user", "password");
         application.setSyncStatus(MobeelizerSyncStatus.FINISHED_WITH_FAILURE);
 
         // when
         application.sync();
 
         // then
         verify(syncPerformer).sync();
     }
 
     @Test
     public void shouldSync3() throws Exception {
         // given
         application.login("instance", "user", "password");
         application.setSyncStatus(MobeelizerSyncStatus.FINISHED_WITH_SUCCESS);
 
         // when
         application.syncAll();
 
         // then
         verify(syncPerformer).sync();
     }
 
 }
