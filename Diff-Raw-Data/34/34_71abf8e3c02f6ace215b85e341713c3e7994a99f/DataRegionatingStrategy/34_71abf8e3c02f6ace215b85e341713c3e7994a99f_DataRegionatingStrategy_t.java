 package org.vfny.geoserver.wms.responses.map.kml;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Iterator;
 import java.util.List;
 import java.util.logging.Logger;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.io.Serializable;
 import java.io.PrintWriter;
 
 import org.geotools.data.DefaultQuery;
 import org.geotools.data.FeatureSource;
 import org.geotools.data.Query;
 import org.geotools.factory.CommonFactoryFinder;
 import org.geotools.feature.FeatureCollection;
 import org.geotools.geometry.jts.ReferencedEnvelope;
 import org.geotools.referencing.CRS;
 import org.geotools.map.MapLayer;
 import org.opengis.filter.FilterFactory;
 import org.opengis.filter.sort.SortBy;
 import org.opengis.filter.sort.SortOrder;
 import org.opengis.feature.simple.SimpleFeature;
 import org.opengis.feature.simple.SimpleFeatureType;
 import org.opengis.referencing.crs.CoordinateReferenceSystem;
 import org.vfny.geoserver.config.FeatureTypeConfig;
 import org.vfny.geoserver.global.MapLayerInfo;
 import org.vfny.geoserver.wms.WMSMapContext;
 import org.vfny.geoserver.global.GeoserverDataDirectory;
 
 import com.vividsolutions.jts.geom.Envelope;
 import com.vividsolutions.jts.geom.Geometry;
 
 /**
  * Strategy for regionating based on algorithmic stuff related to the actual
  * data. This strategy is fairly ill-defined and should be considered highly
  * experimental.
  * 
  * @author David Winslow
  */
 public class DataRegionatingStrategy implements RegionatingStrategy {
 
     private static Logger LOGGER = org.geotools.util.logging.Logging.getLogger("org.geoserver.geosearch");
     private static long MAX_LEVELS = 5;
     private static long FEATURES_PER_TILE = 5;
     private static String myAttributeName;
 
     private TileLevel myRanges;
     private Number myMin;
     private Number myMax;
     private long myZoomLevel;
 
     /**
      * Create a data regionating strategy that uses the specified attribute.
      * @param attname the name of the attribute to use.  The attribute must be numeric.
      */
     public DataRegionatingStrategy(String attname){
         myAttributeName = attname;
     }
 
     public void preProcess(WMSMapContext con, MapLayer layer) {
         myZoomLevel = getZoomLevel(con, layer);
         myRanges = getRangesFromCache(con, layer);
         if (myRanges == null){
             myRanges = preProcessHierarchical(con, layer);
             addRangesToCache(con, layer, myRanges);
         }
         setRange(myRanges, con);

        LOGGER.info("Found range for request: " + myMin + " <-> " + myMax);
     }
 
     /**
      * Check the cache and find the cached range values for the current combination of (layer, zoomlevel, attribute). 
      * If the values are not cached, return null.
      *
      * @param con the WMSMapContext for the current request
      * @param index the numeric index of the layer currently being processed
      */
     private TileLevel getRangesFromCache(WMSMapContext con, MapLayer layer){
         try{
             File cache = findCacheFile(con, layer, myZoomLevel);
             ObjectInputStream in = new ObjectInputStream(new FileInputStream(cache));
             return (TileLevel)in.readObject();
         } catch (Exception e){
             LOGGER.info("Error while trying to write range cache to disk: " + e + ": " + e.getMessage());
         }
 
         return null;
     }
 
     /**
      * Cache the range values for the current combination of (layer, zoomlevel, attribute)
      * @param con the WMSMapContext for the current request
      * @param index the numeric index of the layer currently being processed
      * @param ranges the range values for the current request 
      */
     private void addRangesToCache(WMSMapContext con, MapLayer layer, TileLevel ranges){
         try{
             File cache = findCacheFile(con, layer, myZoomLevel);
             ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(cache));
             out.writeObject(ranges);
             out.flush();
             out.close();
         } catch (Exception e){
             // it's okay, we just won't cache these values
             LOGGER.info("Error while trying to cache calculated range values: " + e + ": " + e.getMessage());
         }
     }
 
     private File findCacheFile(WMSMapContext con, MapLayer layer, long zoomLevel){
         File f = null;
         try{
             FeatureSource source = layer.getFeatureSource();
             MapLayerInfo[] config = con.getRequest().getLayers();
             for (int i = 0; i < config.length; i++){
                 MapLayerInfo theLayer = config[i];
                 if (theLayer.getName().equals(layer.getTitle())){
                     f = GeoserverDataDirectory.findCreateConfigDir("featureTypes");
                     f = new File(f, theLayer.getDirName());
                     f = new File(f, myAttributeName + ".cache");
                     break;
                 } 
             }
         } catch (Exception e){
             LOGGER.severe("Exception while finding the location for the cachefile!");
             e.printStackTrace();
         }
         return f;
     }
 
     private void setRange(TileLevel levels, WMSMapContext con){
         if (levels == null) return;
 
         TileLevel theTile = levels.findTile(con.getAreaOfInterest());
         theTile = (theTile == null ? levels : theTile); // if the bbox is bogus we just assume the lowest zoomlevel
         myMax = theTile.getMax();
         myMin = theTile.getMin();
     }
 
     private void setRange(List ranges){
     	if (ranges.size() == 0) return;
     	
         myMin = (myZoomLevel <= 1) ? null : new Long(((Number)ranges.get((int)(myZoomLevel - 1))).longValue());
         myMax = (myZoomLevel >= ranges.size()) 
             ? new Long(((Number)ranges.get((int)(ranges.size() - 1))).longValue()) 
             : new Long(((Number)ranges.get((int)(myZoomLevel))).longValue());
     }
 
     private boolean inRange(Number n){
         if ((myMin != null) && (myMin.longValue() > n.longValue())) return false;
         if ((myMax != null) && (myMax.longValue() < n.longValue())) return false;
         return true;
     }
 
     private TileLevel preProcessHierarchical(WMSMapContext con, MapLayer layer){
         LOGGER.info("Getting ready to do the hierarchy thing!");
         try{
             FeatureSource<SimpleFeatureType, SimpleFeature> source = 
                 (FeatureSource<SimpleFeatureType, SimpleFeature>) layer.getFeatureSource();
             ReferencedEnvelope fullBounds = source.getBounds();
             TileLevel level = new TileLevel(fullBounds, FEATURES_PER_TILE, 1);
 
             FilterFactory ff = (FilterFactory)CommonFactoryFinder.getFilterFactory(null);
             DefaultQuery query = new DefaultQuery(Query.ALL);
             SortBy sortBy = ff.sort(myAttributeName, SortOrder.DESCENDING);
             query.setSortBy(new SortBy[]{sortBy});
             FeatureCollection col = source.getFeatures(query);
             Iterator it = col.iterator();
 
             try{
                 while (it.hasNext()){
                     SimpleFeature f = (SimpleFeature)it.next();
                     level.add(f);
                 }
             } finally {
                 col.close(it);
             }
 
             // LOGGER.info("Found levels: " + level);
             return level;
             
         } catch (Exception e){
             LOGGER.severe("Error while trying to regionate by data (hierarchical)): " + e);
             e.printStackTrace();
         }
 
         return null;
     }
 
     private List preProcessQuantiles(WMSMapContext con, MapLayer layer) {
         List ranges = new ArrayList();
         try {
             FilterFactory ff = (FilterFactory)CommonFactoryFinder.getFilterFactory(null);
             FeatureSource source = layer.getFeatureSource();
 
             DefaultQuery query =
                 new DefaultQuery(Query.ALL);
             SortBy sortBy = ff.sort(myAttributeName, SortOrder.DESCENDING);
             query.setSortBy(new SortBy[]{sortBy});
             FeatureCollection col = source.getFeatures(query); 
 
             Iterator it = col.iterator();
 
             try{
                 SimpleFeature f = null;
                 for (int i = 0; i < MAX_LEVELS; i++){
                     int j = 0;
                     do {
                         f = (SimpleFeature)it.next();
                     } while (j < getFeatureCountAtZoomLevel(i));
                     ranges.add(f.getAttribute(myAttributeName));
                 }
             } finally {
                 col.close(it);
             }
         } catch (Exception e) {
             LOGGER.severe("Failure while trying to regionate by data (quantiles): " + e);
             e.printStackTrace();
         }
 
         return ranges;
     }
 
     private List preProcessBasic(WMSMapContext con, MapLayer layer) {
         List ranges = new ArrayList();
         try {
             long zoomLevel = getZoomLevel(con, layer);
             // LOGGER.info("I've decided this is at zoom level: " + zoomLevel);
 
             FeatureCollection col = layer.getFeatureSource().getFeatures(Query.ALL);
 
             Iterator it = col.iterator();
             Long min = null;
             Long max = null;
 
             try {
                 while (it.hasNext()) {
                     SimpleFeature f = (SimpleFeature) it.next();
 
                     Number value = (Number) f.getAttribute(myAttributeName);
                     if (min == null || min.longValue() > value.longValue())
                         min = new Long(value.longValue());
                     if (max == null || max.longValue() < value.longValue())
                         max = new Long(value.longValue());
                 }
             } finally {
                 col.close(it);
             }
 
             long range = max - min;
 
             for (long i = 1; i < MAX_LEVELS; i++){
                 ranges.add(min + (i * range / MAX_LEVELS));
             }
         } catch (NullPointerException npe){
         	LOGGER.severe("Failed to find specified attribute; check the attribute name in your request.");
         } catch (Exception e) {
             LOGGER.severe("Failure while trying to regionate by data (linear): " + e);
             e.printStackTrace();
         }
 
         return ranges;
     }
 
     /**
      * Find the number of features that are at lower zoomlevels than the one provided.  When
      * skipping around the sorted list of features to find the minimum value of the range 
      * included at this zoomlevel.
      */
     private long findSkipAmount(long zoomLevel){
         long skipAmount = 0;
         for (long i = 0; i < zoomLevel; i++){
             skipAmount += getFeatureCountAtZoomLevel(i);
         }
         return skipAmount;
     }
 
     private long getFeatureCountAtZoomLevel(long zoomLevel){
         return (long)(Math.pow(4, zoomLevel) * FEATURES_PER_TILE);
     }
 
     private long findLayerSize(long zoomLevel){
         return (long)Math.pow(4,zoomLevel) * FEATURES_PER_TILE;
     }
 
     private long getZoomLevel(WMSMapContext context, MapLayer layer){
         try{
             FeatureSource<SimpleFeatureType, SimpleFeature> source = 
                 (FeatureSource<SimpleFeatureType, SimpleFeature>) layer.getFeatureSource();
             ReferencedEnvelope fullBounds = source.getBounds();
             ReferencedEnvelope requestBounds = context.getAreaOfInterest();
             requestBounds = reproject(requestBounds, fullBounds.getCoordinateReferenceSystem());
             return Math.max(1, 1 - (int) (Math.log(requestBounds.getWidth() / fullBounds.getWidth())));
         } catch (Exception e){
             LOGGER.severe("Zoom Level calculation failed, error was: " + e);
             return 1;
         }
     }
 
     private static ReferencedEnvelope reproject(ReferencedEnvelope renv, CoordinateReferenceSystem crs) throws Exception{
         boolean reprojectBBox = (crs != null)
                 && !CRS.equalsIgnoreMetadata(renv.getCoordinateReferenceSystem(), crs);
             if (reprojectBBox) {
                 renv = renv.transform(crs, true);
             } else {
                 LOGGER.info("Data-based regionating code couldn't reproject, please make sure your "
                 + "datastore is configured properly (hint: for postgis add -s <srsid> to the "
                 + "shp2psql command arguments)"
                 );
             } 
             return renv;
     }
 
     public boolean include(SimpleFeature feature) {
         try {
             Object att = feature.getAttribute(myAttributeName);
             Number num = (Number)att;
 
             // return myMin.longValue() <= l && myMax.longValue() >= l;
             return inRange(num);
         } catch (Exception e) {
             LOGGER.info("Encountered problem while trying to apply data regionating filter: " + e);
         }
         return false;
     }
 
     private static class TileLevel implements Serializable {
         ReferencedEnvelope myBBox;
         String myAttributeName;
         long myFeaturesPerTile;
         long myZoomLevel;
 
         Number myMax;
         Number myMin;
         long myFeatureCount;
 
         List myChildren;
 
         public TileLevel(ReferencedEnvelope bbox, long featuresPerTile, long zoomLevel){
             myBBox = bbox;
             myFeaturesPerTile = featuresPerTile;
             myZoomLevel = zoomLevel;
             myFeatureCount = 0;
             myChildren = null;
         }
 
         public boolean withinTileBounds(SimpleFeature f){
             return myBBox.intersects(((Geometry)f.getDefaultGeometry()).getEnvelopeInternal());
         }
 
         public void add(SimpleFeature f){
             if (myFeatureCount < myFeaturesPerTile) {
                 Number num = (Number)f.getAttribute(myAttributeName);
                 if (myMin == null || myMin.doubleValue() > num.doubleValue())
                     myMin = num;
                 if (myMax == null || myMax.doubleValue() < num.doubleValue())
                     myMax = num;
                myFeatureCount++;
             } else {
                 addToChild(f);
             }
         }
 
         private void addToChild(SimpleFeature f){
             if (myChildren == null){
                 myChildren = createChildTiles();
             }
             
             Iterator it = myChildren.iterator();
             while (it.hasNext()){
                 TileLevel child = (TileLevel)it.next();
                 if (child.withinTileBounds(f)){
                 	child.add(f);
                 	continue;
                 }
             }
         }
 
         private List createChildTiles(){
             List children = new ArrayList();
             ReferencedEnvelope topLeft = new ReferencedEnvelope(myBBox.getCoordinateReferenceSystem());
             topLeft.expandToInclude(myBBox.getCenter(0), myBBox.getMaximum(1));
             topLeft.expandToInclude(myBBox.getMinimum(0), myBBox.getCenter(1));
             
             ReferencedEnvelope topRight = new ReferencedEnvelope(myBBox.getCoordinateReferenceSystem());
             topLeft.expandToInclude(myBBox.getMaximum(0), myBBox.getMaximum(1));
             topLeft.expandToInclude(myBBox.getCenter(0), myBBox.getCenter(1));
             
             ReferencedEnvelope bottomLeft = new ReferencedEnvelope(myBBox.getCoordinateReferenceSystem());
             topLeft.expandToInclude(myBBox.getCenter(0), myBBox.getCenter(1));
             topLeft.expandToInclude(myBBox.getMinimum(0), myBBox.getMinimum(1));
 
             ReferencedEnvelope bottomRight = new ReferencedEnvelope(myBBox.getCoordinateReferenceSystem());
             topLeft.expandToInclude(myBBox.getMaximum(0), myBBox.getCenter(1));
             topLeft.expandToInclude(myBBox.getCenter(0), myBBox.getMinimum(1));
             
             children.add(new TileLevel(topLeft, myFeaturesPerTile, myZoomLevel + 1));
             children.add(new TileLevel(topRight, myFeaturesPerTile, myZoomLevel + 1));
             children.add(new TileLevel(bottomLeft, myFeaturesPerTile, myZoomLevel + 1));
             children.add(new TileLevel(bottomRight, myFeaturesPerTile, myZoomLevel + 1));
             return children;
         }
 
         private TileLevel findTile(ReferencedEnvelope bounds){
             try{
                 bounds = reproject(bounds, myBBox.getCoordinateReferenceSystem());
             } catch (Exception e){
                 return null;
             }
             if (!myBBox.contains((Envelope)bounds)){
                 LOGGER.severe("Requested tile (" + bounds + ") outside bounds (" + myBBox + ")...");
                 return null;
             }
 
             TileLevel theTile = null;
 
            if (myChildren != null){
                Iterator it = myChildren.iterator();
                while (it.hasNext()){
                    TileLevel child = (TileLevel)it.next();
                    theTile = child.findTile(bounds);
                    if (theTile != null) break;
                }
             }
 
             if (theTile == null){
                 return this;
             }
 
             return this;
         }
 
         public Number getMax(){
             return myMax;
         }
 
         public Number getMin(){
             return myMin;
         } 
         
         public String toString(){ // TODO: this should do newlines + indentation
             String newline = "\n";
             StringBuffer result = new StringBuffer().append("TileLevel: ").append(myBBox);
             result.append(newline);
 
             result.append("Max: ").append(myMax).append(", Min: ").append(myMin).append(newline);
             
             for (int i = 0; myChildren != null && i < myChildren.size(); i++)
                 result.append(myChildren.get(i).toString()).append(newline);
             
             return result.toString();
         }
     }
 }
