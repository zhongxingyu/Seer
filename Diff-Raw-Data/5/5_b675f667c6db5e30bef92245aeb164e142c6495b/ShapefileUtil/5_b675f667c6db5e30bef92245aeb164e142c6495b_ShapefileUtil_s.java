 /**
  * Copyright 2011 Jason Ferguson.
  *
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 package org.jason.mapmaker.server.util;
 
 import com.vividsolutions.jts.geom.*;
 import org.apache.commons.compress.archivers.ArchiveException;
 import org.geotools.data.DataUtilities;
 import org.geotools.data.FeatureSource;
 import org.geotools.data.FileDataStore;
 import org.geotools.data.FileDataStoreFinder;
 import org.geotools.data.simple.SimpleFeatureCollection;
 import org.geotools.feature.FeatureCollection;
 import org.geotools.feature.FeatureCollections;
 import org.geotools.feature.FeatureIterator;
 import org.geotools.feature.SchemaException;
 import org.geotools.feature.simple.SimpleFeatureBuilder;
 import org.geotools.geometry.jts.JTSFactoryFinder;
 import org.jason.mapmaker.server.service.BorderPointService;
 import org.jason.mapmaker.server.service.LocationService;
 import org.jason.mapmaker.shared.exceptions.ServiceException;
 import org.jason.mapmaker.shared.exceptions.UtilityClassException;
 import org.jason.mapmaker.shared.model.BorderPoint;
 import org.jason.mapmaker.shared.model.Feature;
 import org.jason.mapmaker.shared.model.Location;
 import org.jason.mapmaker.shared.model.ShapefileMetadata;
 import org.jason.mapmaker.shared.util.GeographyUtils;
 import org.opengis.feature.simple.SimpleFeature;
 import org.opengis.feature.simple.SimpleFeatureType;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.*;
 
 /**
  * Utility class used for file management of the Shapefiles and split them into Location/BorderPoint objects as
  * appropriate.
  * <p/>
  * Shapefiles really aren't a concept in the application, so the idea of representing them with a service is
  * confusing. Using this utility class, we can eliminate the ShapefileService.  I also put alot of effort into
  * refactoring to make this class more testable.
  *
  * @author Jason Ferguson
  * @since 0.4
  */
 @Component
 @SuppressWarnings("unused")
 public class ShapefileUtil {
 
     private static Logger log = LoggerFactory.getLogger(ShapefileUtil.class);
 
     private LocationService locationService;
     private BorderPointService borderPointService;
 
     @Autowired
     public void setLocationService(LocationService locationService) {
         this.locationService = locationService;
     }
 
     @Autowired
     public void setBorderPointService(BorderPointService borderPointService) {
         this.borderPointService = borderPointService;
     }
 
     public void processShapefile(ShapefileMetadata sm) throws UtilityClassException {
 
         // check the URL
         URL u;
         try {
             u = new URL(sm.getUrl());
         } catch (MalformedURLException e) {
             throw new UtilityClassException(e);
         }
 
 
        List<File> unzippedFileList = null;
         try {
             unzippedFileList = ZipUtil.decompress(u);
         } catch (ArchiveException e) {
             throw new UtilityClassException(e);
         } catch (IOException e) {
             throw new UtilityClassException(e);
         }
 
         for (File file : unzippedFileList) {
 
             if (!file.exists()) {
                 log.debug(file.getName() + ": File does not exist!");
             }
 
             try {
                 // need to get the .shp and .shp.xml files from the downloaded shapefile archive
                 if ((file.getName().indexOf(".shp") > -1) && (file.getName().indexOf(".shp.xml") == -1)) {
                     //Process the shapefile
                     FileDataStore dataStore = FileDataStoreFinder.getDataStore(file);
                     String[] typeNames = dataStore.getTypeNames();
 
                     for (String typeName : typeNames) {
 
                         FeatureSource featureSource;
                         FeatureCollection featureCollection;
 
                         try {
                             featureSource = dataStore.getFeatureSource(typeName);
                             featureCollection = featureSource.getFeatures();
 
                             List<Location> locationList = getLocationsFromFeatureCollection(featureCollection);
 
                             // loop through and set the locationlist's shapefilemetadata field
                             for (Location location : locationList) {
                                 location.setShapefileMetadata(sm);
                             }
 
                             // loop through and add the Location to the SM's location list (will this be handled via cascade? I always get confused)
                             locationService.saveList(locationList);
 
                             FeatureIterator<SimpleFeature> iterator = featureCollection.features();
                             while (iterator.hasNext()) {
                                 SimpleFeature feature = iterator.next();
                                 List<BorderPoint> borderPointList = getBorderPointsFromSimpleFeature(feature);
                                 borderPointService.saveList(borderPointList);
                             }
 
                             for (Location l : locationList) {
                                 Map<String, Double> locationBounds = borderPointService.getBoundsByLocation(l);
                                 l.setMaxLat(locationBounds.get("MAXLAT"));
                                 l.setMaxLng(locationBounds.get("MAXLNG"));
                                 l.setMinLat(locationBounds.get("MINLAT"));
                                 l.setMinLng(locationBounds.get("MINLNG"));
 
                                 locationService.update(l);
                             }
 
                             iterator.close();
 
                         } catch (IOException e) {
                             throw new ServiceException("processShapefile() threw IOException", e);
                         }
 
                     }
                 }
             } catch (IOException e) {
                 throw new UtilityClassException(e);
             } catch (ServiceException e) {
                 throw new UtilityClassException(e);
             }
         }
     }
 
     private List<Location> getLocationsFromFeatureCollection(FeatureCollection featureCollection) throws UtilityClassException {
         List<Location> locationList = new ArrayList<Location>(featureCollection.size());
 
         FeatureIterator<SimpleFeature> iterator = featureCollection.features();
         while (iterator.hasNext()) {
 
             SimpleFeature feature = iterator.next();
             Location location = new Location();
             locationService.populateLocationFromFeature(location, feature);
             locationList.add(location);
 
         }
 
         iterator.close();
 
         return locationList;
     }
 
     private List<BorderPoint> getBorderPointsFromSimpleFeature(SimpleFeature feature) throws UtilityClassException {
 
        List<BorderPoint> result;

         String geoId = (String) feature.getAttribute("GEOID10");
         String mtfcc = (String) feature.getAttribute("MTFCC10");
 
         Location location = locationService.getByGeoIdAndMtfcc(geoId, mtfcc);
 
         if (location == null) {
             log.debug("Exception: locationService.getByGeoIdAndMtfcc() returned null");
             throw new UtilityClassException("saveBorderPoints() threw ServiceException due to null Location");
         }
 
         MultiPolygon multiPolygon = (MultiPolygon) feature.getDefaultGeometry();
 
         List<BorderPoint> borderPointList = getBorderPointsFromGeometry(multiPolygon.getBoundary(), 0.0001, location);
         return borderPointList;
 
     }
 
     public List<BorderPoint> getBorderPointsFromGeometry(Geometry geometry, double tolerance, Location location) {
 
         Geometry simplifiedGeometry = GeographyUtils.simplifyGeometry(geometry, tolerance);
 
         Coordinate[] coordinates = simplifiedGeometry.getCoordinates();
 
         // TODO: Does the simplifier throw out duplicates? Is this the cause of the weird artifacts I've seen?
         Set<BorderPoint> borderPointSet = new LinkedHashSet<BorderPoint>(simplifiedGeometry.getNumPoints());
         for (Coordinate c : coordinates) {
             BorderPoint bp = new BorderPoint();
             bp.setLocation(location);
             bp.setLat(c.y);
             bp.setLng(c.x);
             borderPointSet.add(bp);
         }
 
         return new ArrayList<BorderPoint>(borderPointSet);
     }
 
     public static Coordinate[] getCoordinatesFromBorderPointList(List<BorderPoint> borderPointList) {
 
         List<Coordinate> coordinateList = new ArrayList<Coordinate>();
         for (BorderPoint bp : borderPointList) {
             Coordinate c = new Coordinate(bp.getLng(), bp.getLat());
             coordinateList.add(c);
         }
 
         Coordinate cArray[] = new Coordinate[coordinateList.size()];
         return coordinateList.toArray(cArray);
     }
 
     public FeatureCollection createFeatureCollection(List<Feature> featureList) {
 
         SimpleFeatureType TYPE = null; // <- a String attribute
         try {
             TYPE = DataUtilities.createType("Location",
                     "location:Point:srid=4326," + // <- the geometry attribute: Point type
                             "name:String");
         } catch (SchemaException e) {
             e.printStackTrace();
         }
 
         SimpleFeatureCollection featureCollection = FeatureCollections.newCollection();
 
         GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory(null);
 
         SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(TYPE);
 
         for (Feature f: featureList) {
             Point point = geometryFactory.createPoint(new Coordinate(f.getLng(), f.getLat()));
             featureBuilder.add(point);
             featureBuilder.add(f.getName());
             SimpleFeature feature = featureBuilder.buildFeature(null);
             featureCollection.add(feature);
         }
 
         return featureCollection;
 
     }
 
 
 }
