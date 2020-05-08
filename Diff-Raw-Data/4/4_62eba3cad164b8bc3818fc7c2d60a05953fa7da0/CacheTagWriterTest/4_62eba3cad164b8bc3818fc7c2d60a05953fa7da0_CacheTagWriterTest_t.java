 
 package com.google.code.geobeagle.xmlimport;
 
 import static org.easymock.EasyMock.expect;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 
 import com.google.code.geobeagle.CacheType;
 import com.google.code.geobeagle.CacheTypeFactory;
 import com.google.code.geobeagle.Tags;
 import com.google.code.geobeagle.GeocacheFactory.Source;
 import com.google.code.geobeagle.database.CacheWriter;
 
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.powermock.api.easymock.PowerMock;
 import org.powermock.modules.junit4.PowerMockRunner;
 
 @RunWith(PowerMockRunner.class)
 public class CacheTagWriterTest {
     private final CacheWriter mCacheWriter = PowerMock.createMock(CacheWriter.class);
 
     @Test
     public void testClear() {
        expect(mCacheWriter.isLockedFromUpdating(null)).andReturn(false);

         expect(mCacheWriter.insertAndUpdateCache(null, null, 0, 0, 
                 Source.GPX, null, CacheType.NULL, 0, 0, 0)).andReturn(true);
         mCacheWriter.updateTag(null, Tags.NEW, true);
         
         PowerMock.replayAll();
         CacheTagSqlWriter cacheTagSqlWriter = new CacheTagSqlWriter(mCacheWriter, null);
         cacheTagSqlWriter.clear();
         cacheTagSqlWriter.write(Source.GPX);
         PowerMock.verifyAll();
     }
 
     @Test
     public void testEnd() {
         mCacheWriter.clearEarlierLoads();
 
         PowerMock.replayAll();
         new CacheTagSqlWriter(mCacheWriter, null).end();
         PowerMock.verifyAll();
     }
 
     @Test
     public void testGpxTimeDontLoad() {
         expect(mCacheWriter.isGpxAlreadyLoaded("foo.gpx", "2008-04-15 16:10:30")).andReturn(true);
 
         PowerMock.replayAll();
         CacheTagSqlWriter cacheTagSqlWriter = new CacheTagSqlWriter(mCacheWriter, null);
         cacheTagSqlWriter.gpxName("foo.gpx");
         assertFalse(cacheTagSqlWriter.gpxTime("2008-04-15T16:10:30"));
         PowerMock.verifyAll();
     }
 
     @Test
     public void testGpxTimeLoad() {
         expect(mCacheWriter.isGpxAlreadyLoaded("foo.gpx", "2008-04-15 16:10:30")).andReturn(false);
         mCacheWriter.clearCaches("foo.gpx");
 
         PowerMock.replayAll();
         CacheTagSqlWriter cacheTagSqlWriter = new CacheTagSqlWriter(mCacheWriter, null);
         cacheTagSqlWriter.gpxName("foo.gpx");
         assertTrue(cacheTagSqlWriter.gpxTime("2008-04-15T16:10:30"));
         PowerMock.verifyAll();
     }
 
     @Test
     public void testIsoTimeToSql() {
         assertEquals("2008-04-15 16:10:30", new CacheTagSqlWriter(null, null)
                 .isoTimeToSql("2008-04-15T16:10:30.7369220-08:00"));
     }
 
     @Test
     public void testStartWriting() {
         mCacheWriter.startWriting();
 
         PowerMock.replayAll();
         CacheTagSqlWriter cacheTagSqlWriter = new CacheTagSqlWriter(mCacheWriter, null);
         cacheTagSqlWriter.startWriting();
         PowerMock.verifyAll();
     }
 
     @Test
     public void testStopWritingFailure() {
         mCacheWriter.stopWriting();
 
         PowerMock.replayAll();
         CacheTagSqlWriter cacheTagSqlWriter = new CacheTagSqlWriter(mCacheWriter, null);
         cacheTagSqlWriter.stopWriting(false);
         PowerMock.verifyAll();
     }
 
     @Test
     public void testStopWritingSuccess() {
         mCacheWriter.stopWriting();
         expect(mCacheWriter.isGpxAlreadyLoaded("foo.gpx", "2008-04-15 16:10:30")).andReturn(true);
         mCacheWriter.writeGpx("foo.gpx", "2008-04-15 16:10:30");
 
         PowerMock.replayAll();
         CacheTagSqlWriter cacheTagSqlWriter = new CacheTagSqlWriter(mCacheWriter, null);
         cacheTagSqlWriter.gpxName("foo.gpx");
         cacheTagSqlWriter.gpxTime("2008-04-15T16:10:30.7369220-08:00");
         cacheTagSqlWriter.stopWriting(true);
         PowerMock.verifyAll();
     }
 
     @Test
     public void testWrite() {
        expect(mCacheWriter.isLockedFromUpdating("GC123")).andReturn(false);
         expect(mCacheWriter.insertAndUpdateCache("GC123", "my cache", 122, 37, Source.GPX, "foo.gpx",
                 CacheType.TRADITIONAL, 6, 5, 1)).andReturn(false);
         CacheTypeFactory cacheTypeFactory = PowerMock.createMock(CacheTypeFactory.class);
         expect(cacheTypeFactory.container("Micro")).andReturn(1);
         expect(cacheTypeFactory.stars("2.5")).andReturn(5);
         expect(cacheTypeFactory.stars("3")).andReturn(6);
         expect(cacheTypeFactory.fromTag("Traditional Cache")).andReturn(CacheType.TRADITIONAL);
         
         PowerMock.replayAll();
         CacheTagSqlWriter cacheTagSqlWriter = new CacheTagSqlWriter(mCacheWriter, cacheTypeFactory);
         cacheTagSqlWriter.id("GC123");
         cacheTagSqlWriter.cacheName("my cache");
         cacheTagSqlWriter.latitudeLongitude("122", "37");
         cacheTagSqlWriter.container("Micro");
         cacheTagSqlWriter.terrain("2.5");
         cacheTagSqlWriter.difficulty("3");
         
         cacheTagSqlWriter.gpxName("foo.gpx");
         cacheTagSqlWriter.cacheType("Traditional Cache");
         cacheTagSqlWriter.write(Source.GPX);
         PowerMock.verifyAll();
     }
 
 }
