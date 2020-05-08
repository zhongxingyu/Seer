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
 
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 
 public enum GpxPath {
     NO_MATCH(null, PathType.NOP),
     GPX_AU_DESCRIPTION("/gpx/wpt/geocache/description", PathType.LINE),
     GPX_AU_GEOCACHER("/gpx/wpt/geocache/logs/log/geocacher", PathType.LINE),
     GPX_AU_LOGTEXT("/gpx/wpt/geocache/logs/log/text", PathType.LINE),
     GPX_AU_LOGTYPE("/gpx/wpt/geocache/logs/log/type", PathType.LINE),
     GPX_AU_OWNER("/gpx/wpt/geocache/owner", PathType.LINE),
     GPX_AU_SUMMARY("/gpx/wpt/geocache/summary", PathType.LINE),
     GPX_CACHE("/gpx/wpt/groundspeak:cache", PathType.CACHE),
     GPX_CACHE_CONTAINER("/gpx/wpt/groundspeak:cache/groundspeak:container", PathType.CONTAINER),
     GPX_CACHE_DIFFICULTY("/gpx/wpt/groundspeak:cache/groundspeak:difficulty", PathType.DIFFICULTY),
     GPX_CACHE_TERRAIN("/gpx/wpt/groundspeak:cache/groundspeak:terrain", PathType.TERRAIN),
     GPX_EXT_LONGDESC("/gpx/wpt/extensions/cache/long_description", PathType.LONG_DESCRIPTION),
     GPX_EXT_SHORTDESC("/gpx/wpt/extensions/cache/short_description", PathType.SHORT_DESCRIPTION),
     GPX_GEOCACHE_CONTAINER("/gpx/wpt/geocache/container", PathType.CONTAINER),
     GPX_GEOCACHE_DIFFICULTY("/gpx/wpt/geocache/difficulty", PathType.DIFFICULTY),
     GPX_GEOCACHE_EXT_DIFFICULTY("/gpx/wpt/extensions/cache/difficulty", PathType.DIFFICULTY),
     GPX_GEOCACHE_EXT_TERRAIN("/gpx/wpt/extensions/cache/terrain", PathType.TERRAIN),
     GPX_GEOCACHE_TERRAIN("/gpx/wpt/geocache/terrain", PathType.TERRAIN),
     GPX_GEOCACHE_TYPE("/gpx/wpt/geocache/type", PathType.CACHE_TYPE),
     GPX_GEOCACHEHINT("/gpx/wpt/geocache/hints", PathType.HINT),
     GPX_GEOCACHELOGDATE("/gpx/wpt/geocache/logs/log/time", PathType.LOG_DATE),
     GPX_GEOCACHENAME("/gpx/wpt/geocache/name", PathType.NAME),
     GPX_GPXTIME("/gpx/time", PathType.GPX_TIME),
     GPX_GROUNDSPEAKFINDER(
             "/gpx/wpt/groundspeak:cache/groundspeak:logs/groundspeak:log/groundspeak:finder",
             PathType.LINE),
     GPX_GROUNDSPEAKNAME("/gpx/wpt/groundspeak:cache/groundspeak:name", PathType.NAME),
     GPX_HINT("/gpx/wpt/groundspeak:cache/groundspeak:encoded_hints", PathType.HINT),
     GPX_LAST_MODIFIED("/gpx/wpt/bcaching:cache/bcaching:lastModified", PathType.LAST_MODIFIED),
     GPX_LOGDATE("/gpx/wpt/groundspeak:cache/groundspeak:logs/groundspeak:log/groundspeak:date",
             PathType.LOG_DATE),
     GPX_LOGFINDER("/gpx/wpt/groundspeak:cache/groundspeak:logs/groundspeak:log/groundspeak:finder",
             PathType.LOG_TEXT),
     GPX_LOGTEXT("/gpx/wpt/groundspeak:cache/groundspeak:logs/groundspeak:log/groundspeak:text",
             PathType.LOG_TEXT),
     GPX_LOGTYPE("/gpx/wpt/groundspeak:cache/groundspeak:logs/groundspeak:log/groundspeak:type",
             PathType.LOG_TYPE),
     GPX_LONGDESC("/gpx/wpt/groundspeak:cache/groundspeak:long_description",
             PathType.LONG_DESCRIPTION),
     GPX_OCNAME("/gpx/wpt/extensions/cache/name", PathType.NAME),
     GPX_OCOWNER("/gpx/wpt/extensions/cache/owner", PathType.PLACED_BY),
    GPX_OCLOGDATE("/gpx/wpt/extensions/cache/logs/log/time", PathType.LOG_DATE),
    GPX_OCLOGFINDER("/gpx/wpt/extensions/cache/logs/log/geocacher", PathType.LOG_TEXT),
     GPX_OCLOGTEXT("/gpx/wpt/extensions/cache/logs/log/text", PathType.LOG_TEXT),
     GPX_OCLOGTYPE("/gpx/wpt/extensions/cache/logs/log/type", PathType.LOG_TYPE),
     GPX_PLACEDBY("/gpx/wpt/groundspeak:cache/groundspeak:placed_by", PathType.PLACED_BY),
     GPX_SHORTDESC("/gpx/wpt/groundspeak:cache/groundspeak:short_description",
             PathType.SHORT_DESCRIPTION),
     GPX_SYM("/gpx/wpt/sym", PathType.SYMBOL),
     GPX_TERRACACHINGGPXTIME("/gpx/metadata/time", PathType.GPX_TIME),
     GPX_WAYPOINT_TYPE("/gpx/wpt/type", PathType.CACHE_TYPE),
     GPX_WPT("/gpx/wpt", PathType.WPT),
     GPX_WPT_COMMENT("/gpx/wpt/cmt", PathType.LINE),
     GPX_WPTDESC("/gpx/wpt/desc", PathType.DESC),
     GPX_WPTNAME("/gpx/wpt/name", PathType.WPT_NAME),
     GPX_WPTTIME("/gpx/wpt/time", PathType.WPT_TIME),
     LOC_COORD("/loc/waypoint/coord", PathType.LOC_COORD),
     LOC_WPT("/loc/waypoint", PathType.LOC_WPT),
     LOC_WPTNAME("/loc/waypoint/name", PathType.LOC_WPTNAME);
 
     private static final Map<String, GpxPath> stringToEnum = new HashMap<String, GpxPath>();
 
     static {
         for (GpxPath gpxPath : values())
             stringToEnum.put(gpxPath.getPath(), gpxPath);
     }
 
     public static GpxPath fromString(String symbol) {
         final GpxPath gpxPath = stringToEnum.get(symbol);
         if (gpxPath == null) {
             return GpxPath.NO_MATCH;
         }
         return gpxPath;
     }
 
     private final String path;
     private final PathType pathType;
 
     GpxPath(String path, PathType pathType) {
         this.path = path;
         this.pathType = pathType;
     }
 
     public void endTag(ICachePersisterFacade cachePersisterFacade) throws IOException {
         pathType.endTag(cachePersisterFacade);
     }
 
     public String getPath() {
         return path;
     }
 
     public void startTag(XmlPullParserWrapper xmlPullParser,
             ICachePersisterFacade cachePersisterFacade) throws IOException {
         pathType.startTag(xmlPullParser, cachePersisterFacade);
     }
 
     public boolean text(String text, ICachePersisterFacade cachePersisterFacade) throws IOException {
         String trimmedText = text.trim();
         if (trimmedText.length() <= 0)
             return true;
         return pathType.text(trimmedText, cachePersisterFacade);
     }
 }
