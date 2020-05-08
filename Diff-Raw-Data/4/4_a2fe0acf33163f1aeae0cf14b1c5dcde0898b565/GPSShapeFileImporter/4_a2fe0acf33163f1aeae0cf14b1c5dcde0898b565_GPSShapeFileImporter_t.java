 package org.life.sl.importers;
 
 /*
 JMapMatcher
 
 Copyright (c) 2011 Bernhard Barkow, Hans Skov-Petersen, Bernhard Snizek and Contributors
 
 mail: bikeability@life.ku.dk
 web: http://www.bikeability.dk
 
 This program is free software; you can redistribute it and/or modify it under 
 the terms of the GNU General Public License as published by the Free Software 
 Foundation; either version 3 of the License, or (at your option) any later version.
 
 This program is distributed in the hope that it will be useful, but WITHOUT 
 ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS 
 FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 
 You should have received a copy of the GNU General Public License along with 
 this program; if not, see <http://www.gnu.org/licenses/>.
 */
 
 import java.io.File;
 import java.io.IOException;
 import java.io.Serializable;
 import java.sql.Timestamp;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.log4j.Logger;
 import org.geotools.data.DataStore;
 import org.geotools.data.DataStoreFinder;
 import org.geotools.data.FeatureSource;
 import org.geotools.feature.FeatureCollection;
 import org.geotools.feature.FeatureIterator;
 import org.hibernate.Query;
 import org.hibernate.Session;
 import org.hibernate.cfg.Configuration;
 import org.life.sl.orm.HibernateUtil;
 import org.life.sl.orm.Respondent;
 import org.life.sl.orm.SourcePoint;
 import org.life.sl.orm.SourceRoute;
 import org.opengis.feature.simple.SimpleFeature;
 import org.opengis.feature.simple.SimpleFeatureType;
 import org.opengis.feature.type.AttributeDescriptor;
 import org.openstreetmap.josm.io.IllegalDataException;
 
 import com.vividsolutions.jts.geom.Geometry;
 import com.vividsolutions.jts.geom.Point;
 
 public class GPSShapeFileImporter {
 
 	private Session session;
 	private Logger logger = Logger.getRootLogger();
 	
 	public GPSShapeFileImporter(String directory) {
 		// TODO: loop over files.
 	}
 	
 	public GPSShapeFileImporter(File file) throws IOException {
 		Integer batchSize = Integer.getInteger(new Configuration().getProperty("hibernate.jdbc.batch_size"), 30);
 		logger.info("Database batch size: " + batchSize);
 		setUp();
 
 		Map<String,Serializable> connectParameters = new HashMap<String,Serializable>();
 		connectParameters.put("url", file.toURI().toURL());
 		// connectParameters.put("create spatial index", true );
 		DataStore dataStore = DataStoreFinder.getDataStore(connectParameters);
 
 		String[] typeNames = dataStore.getTypeNames();
 		String typeName = typeNames[0];
 
 		logger.info("Reading content " + typeName);
 
 		FeatureSource<SimpleFeatureType, SimpleFeature> featureSource;
 		FeatureCollection<SimpleFeatureType, SimpleFeature> collection;
 		FeatureIterator<SimpleFeature> iterator;
 
 		featureSource = dataStore.getFeatureSource(typeName);
 		collection = featureSource.getFeatures();
 		iterator = collection.features();
 
 		// let us get the schema and pop it into a simple list of strings
 		SimpleFeatureType schema = featureSource.getSchema();
 		List<AttributeDescriptor> ads = schema.getAttributeDescriptors();
 		ArrayList<String> fieldnames = new ArrayList<String>();
 
 		for (AttributeDescriptor ad : ads) {
 			String ln = ad.getLocalName();
 			if (ln != "geom") {
 				fieldnames.add(ln);
 			}
 		}
 
 		Map<Integer, Integer> routeIDs = new HashMap<Integer,Integer>();
 		Map<Integer, Integer> respondentIDs = new HashMap<Integer,Integer>();
 		int nPoints =0;
 		int numberPoints = collection.size();
 		try {
 			while (iterator.hasNext()) {
 				nPoints++;
 
 				SimpleFeature feature = iterator.next();
 				Geometry geometry = (Geometry) feature.getDefaultGeometry();
 				if(geometry.getClass() != Point.class && geometry.getClass() != Point.class) {
 					//    				ErrorHandler.getInstance().error("Error, shapefile must contain lines", 2);
 					System.out.println("ERROR: Shapefile must contain points, but does not.");
 					return;
 				}
 				
 				HashMap<String, Object> attributes = new HashMap<String, Object>();
 				for (String fn : fieldnames) {
 					attributes.put(fn, feature.getAttribute(fn));
 				}
 				
 				int route_id = (Integer) attributes.get("tripstay");
 				int respondent_id = (Integer) attributes.get("RespID");
 				Date date_time = (Date) attributes.get("DATE_TIME");
 				
 				// check if route_id was already handled:
 				if (!routeIDs.containsKey(route_id)) {
 					routeIDs.put(route_id, respondent_id);
 					// if not, check if it exists in the database:
 					Query result = session.createQuery("from SourceRoute WHERE id=" + route_id);
 					if (result.list().size() == 0) {	// if not, add the SourceRoute
 						SourceRoute r = new SourceRoute();
 						r.setId(route_id);
 						r.setRespondentid(respondent_id);
 						session.save(r);
 					}
 				}
 				
 				// check if route_id was already handled:
 				if (!routeIDs.containsKey(respondent_id)) {
 					routeIDs.put(respondent_id, route_id);
 					// if not, check if it exists in the database:
 					Query result2 = session.createQuery("from Respondent WHERE id=" + respondent_id);
 					if (result2.list().size() == 0) {	// if not, add the Respondent
 						Respondent r = new Respondent();
 						r.setId(respondent_id);
 						session.save(r);
 					}
 				}
 				
 				SourcePoint sp = new SourcePoint();
 				sp.setGeometry((Point) geometry.reverse());
 				sp.setSourcerouteid(route_id);
 				sp.setT((Timestamp) date_time);
 				sp.setDateTime(date_time);
 				session.save(sp);
 				if (nPoints % batchSize == 0) {
 					session.flush();
 					session.clear();
 					System.out.print(".");
 				}
 				if (nPoints % 1000 == 0) {
 					System.out.printf("%2.2f%% finished (%d/%d)\n", ((double)nPoints/(double)numberPoints)*100., nPoints, numberPoints);
 				//	session.getTransaction().commit();
 				//	setUp();
 				}
 			}
 		}
 		finally {
 			if( iterator != null ){
 				if (nPoints % batchSize == 0) {
 					session.flush();
 					session.clear();
 				}
 				// YOU MUST CLOSE THE ITERATOR!
 				iterator.close();
 				session.getTransaction().commit();
 				try {
 					session.close();
 				} catch(Exception e) {
 					System.out.println("session was already closed - pyt ! ");
 				}
 			}
 		}
 	}
 	
 	public void setUp() {
 		session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 	}
 	
 	public static void main(String[] args) throws IllegalDataException, IOException {
 
//		String filename = "geodata/CopenhagenGPS/BiCycleTrips.shp";
 //		String filename = "testdata/CopenhagenTEst/TripTest.shp";
		String filename = "testdata/CPH2/GPS_Bikeability_ver4.shp";
 
 		@SuppressWarnings("unused")
 		GPSShapeFileImporter gfi = new GPSShapeFileImporter(new File(filename));
 		System.out.println("Shapefile imported !");
 	}
 
 	
 	
 }
