 package gov.usgs.cida.coastalhazards.uncy;
 
 import java.io.File;
 import java.io.Serializable;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.geotools.data.DataStore;
 import org.geotools.data.DataStoreFinder;
 import org.geotools.data.DefaultTransaction;
 import org.geotools.data.FeatureWriter;
 import org.geotools.data.Transaction;
 import org.geotools.data.shapefile.ShapefileDataStore;
 import org.geotools.data.shapefile.ShapefileDataStoreFactory;
 import org.geotools.data.simple.SimpleFeatureCollection;
 import org.geotools.data.simple.SimpleFeatureIterator;
 import org.geotools.data.simple.SimpleFeatureSource;
 import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
 import org.geotools.geometry.jts.JTSFactoryFinder;
 import org.geotools.referencing.crs.DefaultGeographicCRS;
 import org.opengis.feature.Feature;
 import org.opengis.feature.GeometryAttribute;
 import org.opengis.feature.Property;
 import org.opengis.feature.simple.SimpleFeature;
 import org.opengis.feature.simple.SimpleFeatureType;
 
 import com.vividsolutions.jts.geom.Coordinate;
 import com.vividsolutions.jts.geom.GeometryFactory;
 import com.vividsolutions.jts.geom.Point;
 // which Point class to use?
 
 
 /** Write a copy of the input shapefile.
  * 
  * @author rhayes
  *
  */
 public class NaNWriter {
 
 	public void write(String fn) throws Exception {
 
 		Map<String, Serializable> connect = new HashMap<String, Serializable> ();
 		File fout = new File(fn + ".shp");
 		connect.put("url", fout.toURI().toURL());
         connect.put("create spatial index", Boolean.TRUE);
         ShapefileDataStoreFactory dataStoreFactory = new ShapefileDataStoreFactory();
         ShapefileDataStore outputStore = (ShapefileDataStore) dataStoreFactory.createNewDataStore(connect);
 
 		String typeName = fn+"_with_NaN_attributes";
 
 		SimpleFeatureTypeBuilder typeBuilder = new SimpleFeatureTypeBuilder();
 		typeBuilder.setName(typeName);
 		typeBuilder.setCRS(DefaultGeographicCRS.WGS84);
 		typeBuilder.add("Location", Point.class);
 		typeBuilder.add("M1", Double.class);
 		
 		SimpleFeatureType outputFeatureType = typeBuilder.buildFeatureType();
 		outputStore.createSchema(outputFeatureType);
 
         GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory(null);
 		
 		double[][] pts = {
				// { 1001, 2001, Double.NaN },
 				{ 1002, 2002, -1.0e39},
 				{ 1003, 2003, 42.123 },
 				{ 1004, 2004, Double.MAX_VALUE },
 				{ 1005, 2005, Double.MIN_VALUE },
				{ 1006, 2006, 1999 },
				{ 1007, 2007, - Double.MAX_VALUE }
 		};
 		
         Transaction tx = new DefaultTransaction("create");
 		FeatureWriter<SimpleFeatureType, SimpleFeature> featureWriter = outputStore.getFeatureWriterAppend(tx);
 
 		int pointCt = 0;
 		
 		try {
 			for (int r = 0; r < pts.length; r++) {
 				Coordinate coord = new Coordinate(pts[r][0], pts[r][1]);
 				
 				Point newPoint = geometryFactory.createPoint(coord);
 				
 				SimpleFeature writeFeature = featureWriter.next();
 				writeFeature.setAttribute("Location", newPoint);
 				writeFeature.setAttribute("M1", pts[r][2]);
 				featureWriter.write();
 
 				pointCt ++;					
 
 			}
 		} finally {
 			
 		}
 		tx.commit();
 		
 		System.out.printf("Wrote %d points\n", pointCt);
 	}
 
 	public void read(String fn) throws Exception {
 
 		File file = new File(fn + ".shp");
 
 		Map<String, Serializable> connect = new HashMap<String, Serializable>();
 		connect.put("url", file.toURI().toURL());
 
 		DataStore dataStore = DataStoreFinder.getDataStore(connect);
 
 		String[] typeNames = dataStore.getTypeNames();
 
 		System.out.printf("Type names in %s:", file);
 		for (String tn : typeNames) {
 			System.out.println(tn);
 		}
 
 		String typeName = typeNames[0];
 
 		System.out.println("Reading content " + typeName);
 
 		SimpleFeatureSource featureSource = dataStore.getFeatureSource(typeName);
 		SimpleFeatureCollection collection = featureSource.getFeatures();
 
 		SimpleFeatureIterator iterator = collection.features();
 		
 		try {
 			while (iterator.hasNext()) {
 				Feature feature = iterator.next();
 				GeometryAttribute sourceGeometry = feature.getDefaultGeometryProperty();
 				Collection<? extends Property> pp = feature.getValue();
 				Property m1 = feature.getProperty("M1");
 				System.out.printf("value %s%n", pp);
 				System.out.printf("\tM1: %g%n", m1.getValue());
 			}
 		} finally {
 			iterator.close();
 		}
 	}
 
 	public static void main(String[] args) throws Exception {
 		for (String fn : args) {
 			NaNWriter ego = new NaNWriter();
 
 			ego.write(fn);
 			ego.read(fn);
 			ego.read("src/test/resources/handpts");
 		}
 	}
 
 }
