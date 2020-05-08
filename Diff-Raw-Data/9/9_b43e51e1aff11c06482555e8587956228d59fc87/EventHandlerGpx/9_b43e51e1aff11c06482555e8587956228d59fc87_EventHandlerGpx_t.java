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
 import com.google.code.geobeagle.xmlimport.GpxToCacheDI.XmlPullParserWrapper;
 
 import java.io.IOException;
 
 class EventHandlerGpx implements EventHandler {
     static final String XPATH_GEOCACHEHINT = "/gpx/wpt/geocache/hints";
     static final String XPATH_GEOCACHELOGDATE = "/gpx/wpt/geocache/logs/log/time";
     static final String XPATH_GEOCACHENAME = "/gpx/wpt/geocache/name";
     static final String XPATH_GPXNAME = "/gpx/name";
     static final String XPATH_GPXTIME = "/gpx/time";
     static final String XPATH_GROUNDSPEAKNAME = "/gpx/wpt/groundspeak:cache/groundspeak:name";
     static final String XPATH_HINT = "/gpx/wpt/groundspeak:cache/groundspeak:encoded_hints";
     static final String XPATH_LOGDATE = "/gpx/wpt/groundspeak:cache/groundspeak:logs/groundspeak:log/groundspeak:date";
     static final String[] XPATH_PLAINLINES = {
             "/gpx/wpt/cmt", "/gpx/wpt/desc", "/gpx/wpt/groundspeak:cache/groundspeak:type",
             "/gpx/wpt/groundspeak:cache/groundspeak:container",
             "/gpx/wpt/groundspeak:cache/groundspeak:short_description",
             "/gpx/wpt/groundspeak:cache/groundspeak:long_description",
             "/gpx/wpt/groundspeak:cache/groundspeak:logs/groundspeak:log/groundspeak:type",
             "/gpx/wpt/groundspeak:cache/groundspeak:logs/groundspeak:log/groundspeak:finder",
             "/gpx/wpt/groundspeak:cache/groundspeak:logs/groundspeak:log/groundspeak:text",
             /* here are the geocaching.com.au entries */
             "/gpx/wpt/geocache/owner", "/gpx/wpt/geocache/type", "/gpx/wpt/geocache/summary",
             "/gpx/wpt/geocache/description", "/gpx/wpt/geocache/logs/log/geocacher",
             "/gpx/wpt/geocache/logs/log/type", "/gpx/wpt/geocache/logs/log/text"
 
     };
     static final String XPATH_SYM = "/gpx/wpt/sym";
     static final String XPATH_WPT = "/gpx/wpt";
     static final String XPATH_WPTDESC = "/gpx/wpt/desc";
     static final String XPATH_WPTNAME = "/gpx/wpt/name";
 
     private final CachePersisterFacade mCachePersisterFacade;
 
     public EventHandlerGpx(CachePersisterFacade cachePersisterFacade) {
         mCachePersisterFacade = cachePersisterFacade;
     }
 
     public void endTag(String previousFullPath) throws IOException {
         if (previousFullPath.equals(XPATH_WPT)) {
             mCachePersisterFacade.endCache(Source.GPX);
         }
     }
 
     public void startTag(String mFullPath, XmlPullParserWrapper mXmlPullParser) {
         if (mFullPath.equals(XPATH_WPT)) {
             mCachePersisterFacade.startCache();
             mCachePersisterFacade.wpt(mXmlPullParser.getAttributeValue(null, "lat"), mXmlPullParser
                     .getAttributeValue(null, "lon"));
         }
     }
 
     public boolean text(String mFullPath, String text) throws IOException {
         text = text.trim();
         if (mFullPath.equals(XPATH_WPTNAME)) {
             mCachePersisterFacade.wptName(text);
         } else if (mFullPath.equals(XPATH_WPTDESC)) {
             mCachePersisterFacade.wptDesc(text);
         } else if (mFullPath.equals(XPATH_GPXTIME)) {
             return mCachePersisterFacade.gpxTime(text);
         } else if (mFullPath.equals(XPATH_GROUNDSPEAKNAME) || mFullPath.equals(XPATH_GEOCACHENAME)) {
             mCachePersisterFacade.groundspeakName(text);
         } else if (mFullPath.equals(XPATH_LOGDATE) || mFullPath.equals(XPATH_GEOCACHELOGDATE)) {
             mCachePersisterFacade.logDate(text);
         } else if (mFullPath.equals(XPATH_SYM)) {
             mCachePersisterFacade.symbol(text);
         } else if (mFullPath.equals(XPATH_HINT) || mFullPath.equals(XPATH_GEOCACHEHINT)) {
             if (!text.equals("")) {
                 mCachePersisterFacade.hint(text);
             }
         }
 
         for (String writeLineMatch : XPATH_PLAINLINES) {
             if (mFullPath.equals(writeLineMatch)) {
                 mCachePersisterFacade.line(text);
                 return true;
             }
         }
         return true;
     }
 }
