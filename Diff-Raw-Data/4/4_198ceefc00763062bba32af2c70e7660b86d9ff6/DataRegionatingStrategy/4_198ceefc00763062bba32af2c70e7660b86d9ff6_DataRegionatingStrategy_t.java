 package org.vfny.geoserver.wms.responses.map.kml;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Comparator;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeMap;
 import java.util.TreeSet;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.Statement;
 
 import org.geotools.data.DefaultQuery;
 import org.geotools.data.FeatureSource;
 import org.geotools.data.Query;
 import org.geotools.factory.CommonFactoryFinder;
 import org.geotools.feature.FeatureCollection;
 import org.geotools.geometry.jts.ReferencedEnvelope;
 import org.geotools.map.MapLayer;
 import org.geotools.referencing.CRS;
 import org.opengis.feature.simple.SimpleFeature;
 import org.opengis.feature.simple.SimpleFeatureType;
 import org.opengis.filter.FilterFactory;
 import org.opengis.filter.sort.SortBy;
 import org.opengis.filter.sort.SortOrder;
 import org.opengis.referencing.crs.CoordinateReferenceSystem;
 import org.vfny.geoserver.global.GeoserverDataDirectory;
 import org.vfny.geoserver.global.MapLayerInfo;
 import org.vfny.geoserver.wms.WMSMapContext;
 import org.geoserver.ows.HttpErrorCodeException;
 
 import com.vividsolutions.jts.geom.Envelope;
 import com.vividsolutions.jts.geom.Geometry;
 
 /**
  * Strategy for regionating based on algorithmic stuff related to the actual
  * data. This strategy is fairly ill-defined and should be considered highly
  * experimental.
  * 
  * @author David Winslow
  */
 public class DataRegionatingStrategy extends CachedHierarchyRegionatingStrategy {
 
     private static Logger LOGGER = org.geotools.util.logging.Logging.getLogger("org.geoserver.geosearch");
     private static int FEATURES_PER_TILE = 100;
     private static String myAttributeName;
 
     /**
      * Create a data regionating strategy that uses the specified attribute.
      * @param attname the name of the attribute to use.  The attribute must be numeric.
      */
     public DataRegionatingStrategy(String attname){
         myAttributeName = attname;
     }
 
     protected String findCacheTable(WMSMapContext con, MapLayer layer){
         return super.findCacheTable(con, layer) + "_" + myAttributeName;
     }
 
     protected TileLevel createTileHierarchy(WMSMapContext con, MapLayer layer){
         LOGGER.info("Getting ready to do the hierarchy thing!");
         try{
             FeatureSource<SimpleFeatureType, SimpleFeature> source = 
                 (FeatureSource<SimpleFeatureType, SimpleFeature>) layer.getFeatureSource();
            CoordinateReferenceSystem nativeCRS = source.getSchema().getCRS();
             ReferencedEnvelope fullBounds = TileLevel.getWorldBounds();
             fullBounds = fullBounds.transform(nativeCRS, true);
             TileLevel root = TileLevel.makeRootLevel(fullBounds, FEATURES_PER_TILE, new DataComparator());
             
             FilterFactory ff = (FilterFactory)CommonFactoryFinder.getFilterFactory(null);
             DefaultQuery query = new DefaultQuery(Query.ALL);
             FeatureCollection col = source.getFeatures(query);
 
             root.populate(col);
             
             return root;
         } catch (Exception e){
             LOGGER.log(Level.SEVERE, "Error while trying to regionate by data (hierarchical)): ", e);
             throw new HttpErrorCodeException(500, "Error while trying to regionate by " + myAttributeName, e);
         }
     }
 
     private class DataComparator implements Comparator{
         public int compare(Object a, Object b){
             if ((a == null) || (b == null))
                 return 0;
 
             SimpleFeature fa = (SimpleFeature)a;
             SimpleFeature fb = (SimpleFeature)b;
 
             Object attrA = fa.getAttribute(myAttributeName);
             Object attrB = fb.getAttribute(myAttributeName);
 
             if ((attrA == null) || (attrB == null))
                 return 0;
 
             if (attrA instanceof Comparable) 
                 return ((Comparable)attrA).compareTo(attrB);
 
             return 0;
         }
     }
 }
