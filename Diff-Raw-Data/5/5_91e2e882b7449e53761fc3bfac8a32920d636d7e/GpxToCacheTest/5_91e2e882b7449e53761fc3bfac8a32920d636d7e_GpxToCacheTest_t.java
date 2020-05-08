 
 package com.google.code.geobeagle.io;
 
 import static org.easymock.EasyMock.expect;
 import static org.easymock.classextension.EasyMock.createMock;
 import static org.easymock.classextension.EasyMock.replay;
 import static org.easymock.classextension.EasyMock.verify;
 
 import com.google.code.geobeagle.io.CacheDetailsWriter.CacheDetailsWriterFactory;
 import com.google.code.geobeagle.io.GpxToCache.EventHelper;
 import com.google.code.geobeagle.io.GpxToCache.GpxCaches;
 import com.google.code.geobeagle.io.GpxToCache.XmlPathBuilder;
 import com.google.code.geobeagle.io.LoadGpx.Cache;
 
 import org.xmlpull.v1.XmlPullParser;
 import org.xmlpull.v1.XmlPullParserException;
 
 import java.io.IOException;
 import java.util.Iterator;
 
 import junit.framework.TestCase;
 
 public class GpxToCacheTest extends TestCase {
 
     private XmlPullParser xmlPullParser = createMock(XmlPullParser.class);
 
     private void endTagAndDocument(XmlPullParser xmlPullParser) throws XmlPullParserException,
             IOException {
         expectTag(XmlPullParser.END_TAG, "wpt");
         expect(xmlPullParser.next()).andReturn(XmlPullParser.END_TAG);
     }
 
     private void expectTag(int eventType, String name) throws XmlPullParserException, IOException {
         expect(xmlPullParser.next()).andReturn(eventType);
         expect(xmlPullParser.getName()).andReturn(name);
     }
 
     private void expectText(String text) throws XmlPullParserException, IOException {
         expect(xmlPullParser.next()).andReturn(XmlPullParser.TEXT);
         expect(xmlPullParser.getText()).andReturn(text);
     }
 
     private void startTag(XmlPullParser xmlPullParser) throws XmlPullParserException, IOException {
         expect(xmlPullParser.getEventType()).andReturn(XmlPullParser.START_TAG);
         expect(xmlPullParser.getName()).andReturn("gpx");
         expectTag(XmlPullParser.START_TAG, "wpt");
         expect(xmlPullParser.getAttributeValue(null, "lat")).andReturn("123");
         expect(xmlPullParser.getAttributeValue(null, "lon")).andReturn("37");
 
         expectTag(XmlPullParser.START_TAG, "name");
         expectText("GC123");
         expectTag(XmlPullParser.END_TAG, "name");
     }
 
     public void testEventHelperStart() throws IOException {
         XmlPathBuilder xmlPathBuilder = createMock(XmlPathBuilder.class);
         GpxEventHandler gpxEventHandler = createMock(GpxEventHandler.class);
         XmlPullParser xmlPullParser = createMock(XmlPullParser.class);
 
         expect(xmlPullParser.getName()).andReturn("some tag");
         xmlPathBuilder.startTag("some tag");
         expect(xmlPathBuilder.getPath()).andReturn("/foo");
         gpxEventHandler.startTag("/foo", xmlPullParser);
 
         replay(xmlPathBuilder);
         replay(gpxEventHandler);
         replay(xmlPullParser);
         EventHelper eventHelper = new EventHelper(xmlPathBuilder, gpxEventHandler, xmlPullParser);
         assertEquals(null, eventHelper.handleEvent(XmlPullParser.START_TAG));
         verify(xmlPathBuilder);
         verify(gpxEventHandler);
         verify(xmlPullParser);
     }
 
     public void testEventHelperEnd() throws IOException {
         XmlPathBuilder xmlPathBuilder = createMock(XmlPathBuilder.class);
         GpxEventHandler gpxEventHandler = createMock(GpxEventHandler.class);
         XmlPullParser xmlPullParser = createMock(XmlPullParser.class);
         Cache cache = createMock(Cache.class);
 
         expect(xmlPathBuilder.getPath()).andReturn("/path");
         expect(gpxEventHandler.endTag("/path")).andReturn(cache);
         expect(xmlPullParser.getName()).andReturn("name");
         xmlPathBuilder.endTag("name");
 
         replay(xmlPathBuilder);
         replay(gpxEventHandler);
         replay(xmlPullParser);
         EventHelper eventHelper = new EventHelper(xmlPathBuilder, gpxEventHandler, xmlPullParser);
         assertEquals(cache, eventHelper.handleEvent(XmlPullParser.END_TAG));
         verify(xmlPathBuilder);
         verify(gpxEventHandler);
         verify(xmlPullParser);
     }
 
     public void testEventHelperText() throws IOException {
         XmlPathBuilder xmlPathBuilder = createMock(XmlPathBuilder.class);
         GpxEventHandler gpxEventHandler = createMock(GpxEventHandler.class);
         XmlPullParser xmlPullParser = createMock(XmlPullParser.class);
 
         expect(xmlPathBuilder.getPath()).andReturn("/path");
         expect(xmlPullParser.getText()).andReturn("text");
         gpxEventHandler.text("/path", "text");
 
         replay(xmlPathBuilder);
         replay(gpxEventHandler);
         replay(xmlPullParser);
         EventHelper eventHelper = new EventHelper(xmlPathBuilder, gpxEventHandler, xmlPullParser);
         assertEquals(null, eventHelper.handleEvent(XmlPullParser.TEXT));
         verify(xmlPathBuilder);
         verify(gpxEventHandler);
         verify(xmlPullParser);
     }
 
     public void testGpxToCacheId() throws XmlPullParserException, IOException {
         CacheDetailsWriterFactory cacheDetailsWriterFactory = createMock(CacheDetailsWriterFactory.class);
         CacheDetailsWriter cacheDetailsWriter = createMock(CacheDetailsWriter.class);
 
         startTag(xmlPullParser);
         endTagAndDocument(xmlPullParser);
         expect(cacheDetailsWriterFactory.create(GpxToCache.GEOBEAGLE_DIR + "/GC123.html"))
                 .andReturn(cacheDetailsWriter);
 
         replay(cacheDetailsWriterFactory);
         replay(xmlPullParser);
         GpxToCache gpxToCache = new GpxToCache(xmlPullParser, new EventHelper(new XmlPathBuilder(),
                new GpxEventHandler(cacheDetailsWriterFactory, new Cache(), cacheDetailsWriter), xmlPullParser));
         Cache cache = gpxToCache.load();
         assertEquals("GC123", cache.mId);
         verify(xmlPullParser);
         verify(cacheDetailsWriterFactory);
     }
 
     public void testGpxToCacheName() throws XmlPullParserException, IOException {
         CacheDetailsWriterFactory cacheDetailsWriterFactory = createMock(CacheDetailsWriterFactory.class);
         CacheDetailsWriter cacheDetailsWriter = createMock(CacheDetailsWriter.class);
         startTag(xmlPullParser);
 
         expect(cacheDetailsWriterFactory.create(GpxToCache.GEOBEAGLE_DIR + "/GC123.html"))
                 .andReturn(cacheDetailsWriter);
         expectTag(XmlPullParser.START_TAG, "groundspeak:cache");
         expectTag(XmlPullParser.START_TAG, "groundspeak:name");
         expectText("a fun little cache");
         expectTag(XmlPullParser.END_TAG, "groundspeak:name");
         expectTag(XmlPullParser.END_TAG, "groundspeak:cache");
 
         endTagAndDocument(xmlPullParser);
 
         replay(xmlPullParser);
         replay(cacheDetailsWriterFactory);
         GpxToCache gpxToCache = new GpxToCache(xmlPullParser, new EventHelper(new XmlPathBuilder(),
                new GpxEventHandler(cacheDetailsWriterFactory, new Cache(), cacheDetailsWriter), xmlPullParser));
         Cache cache = gpxToCache.load();
         assertEquals("a fun little cache", cache.mName);
         verify(xmlPullParser);
         verify(cacheDetailsWriterFactory);
     }
 
     public void testLoad() throws XmlPullParserException, IOException {
         XmlPullParser xmlPullParser = createMock(XmlPullParser.class);
         EventHelper eventHelper = createMock(EventHelper.class);
         Cache cache = createMock(Cache.class);
 
         expect(xmlPullParser.getEventType()).andReturn(XmlPullParser.START_DOCUMENT);
         expect(eventHelper.handleEvent(XmlPullParser.START_DOCUMENT)).andReturn(null);
 
         expect(xmlPullParser.next()).andReturn(XmlPullParser.START_TAG);
         expect(eventHelper.handleEvent(XmlPullParser.START_TAG)).andReturn(null);
 
         expect(xmlPullParser.next()).andReturn(XmlPullParser.START_TAG);
         expect(eventHelper.handleEvent(XmlPullParser.START_TAG)).andReturn(cache);
         expect(xmlPullParser.next()).andReturn(XmlPullParser.START_TAG);
 
         replay(xmlPullParser);
         replay(eventHelper);
         GpxToCache gpxToCache = new GpxToCache(xmlPullParser, eventHelper);
         assertEquals(cache, gpxToCache.load());
         verify(xmlPullParser);
         verify(eventHelper);
     }
 
     public void testLoadEmpty() throws XmlPullParserException, IOException {
         XmlPullParser xmlPullParser = createMock(XmlPullParser.class);
         EventHelper eventHelper = createMock(EventHelper.class);
 
         expect(xmlPullParser.getEventType()).andReturn(XmlPullParser.END_DOCUMENT);
 
         replay(xmlPullParser);
         replay(eventHelper);
         GpxToCache gpxToCache = new GpxToCache(xmlPullParser, eventHelper);
         assertEquals(null, gpxToCache.load());
         verify(xmlPullParser);
         verify(eventHelper);
     }
 
     public void testXmlPathBuilderEmpty() {
         XmlPathBuilder xmlPathBuilder = new XmlPathBuilder();
         assertEquals("", xmlPathBuilder.getPath());
     }
 
     public void testXmlPathBuilderOne() {
         XmlPathBuilder xmlPathBuilder = new XmlPathBuilder();
         xmlPathBuilder.startTag("test");
         assertEquals("/test", xmlPathBuilder.getPath());
         xmlPathBuilder.endTag("test");
         assertEquals("", xmlPathBuilder.getPath());
     }
 
     public void testXmlPathBuilderTwo() {
         XmlPathBuilder xmlPathBuilder = new XmlPathBuilder();
 
         xmlPathBuilder.startTag("test");
         assertEquals("/test", xmlPathBuilder.getPath());
 
         xmlPathBuilder.startTag("foo");
         assertEquals("/test/foo", xmlPathBuilder.getPath());
         xmlPathBuilder.endTag("foo");
 
         assertEquals("/test", xmlPathBuilder.getPath());
         xmlPathBuilder.endTag("test");
         assertEquals("", xmlPathBuilder.getPath());
     }
 
     public void testGpxCachesEmpty() throws XmlPullParserException, IOException {
         GpxToCache gpxToCache = createMock(GpxToCache.class);
 
         expect(gpxToCache.load()).andReturn(null);
 
         replay(gpxToCache);
         GpxCaches gpxCaches = new GpxCaches(gpxToCache, null);
         Iterator<Cache> iterator = gpxCaches.iterator();
         assertFalse(iterator.hasNext());
         assertNull(iterator.next());
         verify(gpxToCache);
     }
 
     public void testGpxCachesOne() throws XmlPullParserException, IOException {
         GpxToCache gpxToCache = createMock(GpxToCache.class);
         Cache cache = createMock(Cache.class);
 
         expect(gpxToCache.load()).andReturn(cache);
         expect(gpxToCache.load()).andReturn(null);
 
         replay(gpxToCache);
         GpxCaches gpxCaches = new GpxCaches(gpxToCache, null);
         Iterator<Cache> iterator = gpxCaches.iterator();
         assertTrue(iterator.hasNext());
         assertEquals(cache, iterator.next());
         assertFalse(iterator.hasNext());
         verify(gpxToCache);
     }
 
     public void xtestNameNotTB() throws XmlPullParserException, IOException {
         startTag(xmlPullParser);
 
         expectTag(XmlPullParser.START_TAG, "groundspeak:cache");
         expectTag(XmlPullParser.START_TAG, "groundspeak:name");
         expectText("a fun little cache");
         expectTag(XmlPullParser.END_TAG, "groundspeak:name");
         expectTag(XmlPullParser.END_TAG, "groundspeak:cache");
 
         expectTag(XmlPullParser.START_TAG, "groundspeak:travelbug");
         expectTag(XmlPullParser.START_TAG, "groundspeak:name");
         // expect(xmlPullParser.next()).andReturn(XmlPullParser.TEXT);
         expectTag(XmlPullParser.END_TAG, "groundspeak:name");
         expectTag(XmlPullParser.END_TAG, "groundspeak:travelbug");
 
         endTagAndDocument(xmlPullParser);
 
         replay(xmlPullParser);
         GpxToCache gpxToCache = new GpxToCache(xmlPullParser, new EventHelper(new XmlPathBuilder(),
                 new GpxEventHandler(null, new Cache()), xmlPullParser));
         Cache cache = gpxToCache.load();
         assertEquals("a fun little cache", cache.mName);
         verify(xmlPullParser);
     }
 
     public void xtestWpt() throws XmlPullParserException, IOException {
         startTag(xmlPullParser);
         endTagAndDocument(xmlPullParser);
 
         replay(xmlPullParser);
         GpxToCache gpxToCache = new GpxToCache(xmlPullParser, null);
         Cache cache = gpxToCache.load();
         assertEquals(123.0, cache.mLatitude);
         assertEquals(37.0, cache.mLongitude);
         verify(xmlPullParser);
     }
 }
