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
 
 package com.google.code.geobeagle.xmlimport;
 
 import com.google.code.geobeagle.GeocacheFactory.Source;
 import com.google.code.geobeagle.cachedetails.CacheDetailsLoader;
 import com.google.code.geobeagle.cachedetails.CacheDetailsWriter;
 import com.google.code.geobeagle.xmlimport.CachePersisterFacadeDI.FileFactory;
 import com.google.inject.Inject;
 
 import android.os.PowerManager.WakeLock;
import android.util.Log;
 
 import java.io.File;
 import java.io.IOException;
 
 public class CachePersisterFacade {
     private final CacheDetailsWriter mCacheDetailsWriter;
     private String mCacheName = "";
     private final CacheTagSqlWriter mCacheTagWriter;
     private final FileFactory mFileFactory;
     private MessageHandlerInterface mMessageHandler;
     private final WakeLock mWakeLock;
     private String mLastModified;
 
     @Inject
     CachePersisterFacade(CacheTagSqlWriter cacheTagSqlWriter, FileFactory fileFactory,
             CacheDetailsWriter cacheDetailsWriter, MessageHandlerInterface messageHandler,
             WakeLock wakeLock) {
         mCacheDetailsWriter = cacheDetailsWriter;
         mCacheTagWriter = cacheTagSqlWriter;
         mFileFactory = fileFactory;
         mMessageHandler = messageHandler;
         mWakeLock = wakeLock;
     }
 
     void cacheType(String text) {
         mCacheTagWriter.cacheType(text);
     }
 
     void close(boolean success) {
         mCacheTagWriter.stopWriting(success);
     }
 
     void container(String text) {
         mCacheTagWriter.container(text);
     }
 
     void difficulty(String text) {
         mCacheTagWriter.difficulty(text);
     }
 
     void end() {
         mCacheTagWriter.end();
     }
 
     void endCache(Source source) throws IOException {
         mMessageHandler.updateName(mCacheName);
         mCacheDetailsWriter.close();
         mCacheTagWriter.write(source);
     }
 
     boolean gpxTime(String gpxTime) {
         return mCacheTagWriter.gpxTime(gpxTime);
     }
 
     void groundspeakName(String text) {
         mCacheTagWriter.cacheName(text);
     }
 
     void hint(String text) throws IOException {
         mCacheDetailsWriter.writeHint(text);
     }
 
     void line(String text) throws IOException {
         mCacheDetailsWriter.writeLine(text);
     }
 
     void logDate(String text) throws IOException {
         mCacheDetailsWriter.writeLogDate(text);
     }
 
     void open(String path) {
         mMessageHandler.updateSource(path);
         mCacheTagWriter.startWriting();
         mCacheTagWriter.gpxName(path);
         mCacheDetailsWriter.gpxName(path);
     }
 
     void start() {
         File file = mFileFactory.createFile(CacheDetailsLoader.DETAILS_DIR);
         file.mkdirs();
     }
 
     void startCache() {
         mCacheName = "";
         mCacheTagWriter.clear();
     }
 
     void symbol(String text) {
         mCacheTagWriter.symbol(text);
     }
 
     void terrain(String text) {
         mCacheTagWriter.terrain(text);
     }
 
     void wpt(String latitude, String longitude) {
         mCacheTagWriter.latitudeLongitude(latitude, longitude);
         mCacheDetailsWriter.latitudeLongitude(latitude, longitude);
     }
 
     void wptDesc(String cacheName) {
         mCacheName = cacheName;
         mCacheTagWriter.cacheName(cacheName);
     }
 
     void wptName(String wpt) throws IOException {
         mCacheDetailsWriter.open(wpt);
         mCacheDetailsWriter.writeWptName(wpt);
         mCacheTagWriter.id(wpt);
         mMessageHandler.updateWaypointId(wpt);
         mWakeLock.acquire(GpxLoader.WAKELOCK_DURATION);
     }
 
     public void lastModified(String trimmedText) {
         mLastModified = trimmedText;
     }
 
     public String getLastModified() {
         return mLastModified;
     }
 
     public void archived(String attributeValue) {
        mCacheTagWriter.archived(attributeValue.equalsIgnoreCase("True"));
     }
 
     public void available(String attributeValue) {
        mCacheTagWriter.available(attributeValue.equalsIgnoreCase("True"));
     }
 
 }
