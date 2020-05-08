 /*
 Copyright 2010 Alexandre Gellibert
 
 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at http://www.apache.org/licenses/
 LICENSE-2.0 Unless required by applicable law or agreed to in writing,
 software distributed under the License is distributed on an "AS IS"
 BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 implied. See the License for the specific language governing permissions
 and limitations under the License.
 */
 
 package com.beoui.utils;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.jdo.PersistenceManager;
 
 import junit.framework.Assert;
 import junit.framework.TestCase;
 
 import com.beoui.geocell.GeocellManager;
 import com.beoui.geocell.model.BoundingBox;
 import com.beoui.geocell.model.CostFunction;
 import com.beoui.geocell.model.GeocellQuery;
 import com.beoui.geocell.model.Point;
 
 /**
  * Unit test also used to explain how to use Geocell class.
  *
  * @author Alexandre Gellibert <alexandre.gellibert@gmail.com>
  *
  */
 public class HowToUseGeocell extends TestCase {
 
     private final Logger log = Logger.getLogger("com.beoui.utils");
 
     /**
      * First step is to save your entities.
      * In database, you don't save only latitude and longitude of your point but also geocells around this point.
      */
     public void testHowToSaveGeocellsInDatabase() {
         // Incoming data: latitude and longitude (Bordeaux for instance)
         double lat = 44.838611;
         double lon = -0.578333;
 
         // Transform it to a point
         Point p = new Point(lat, lon);
 
         // Generates the list of GeoCells
         List<String> cells = GeocellManager.generateGeoCell(p);
 
         // Save your instance
         ObjectToSave obj = new ObjectToSave();
         obj.setLatitude(lat);
         obj.setLongitude(lon);
         obj.setGeocells(cells);
 
         //objDao.save(obj);
 
         // Just checking that cells are not empty
         Assert.assertTrue(cells.size() > 0);
 
         // Show in the log what cells are going to be saved
         log.log(Level.INFO, "Geocells to be saved for Point("+lat+","+lon+") are: "+cells);
     }
 
     /**
      * Second step, now entities are in database, we can query on them.
      * Here is the example of a bounding box query.
      *
      */
     public void testHowToQueryOnABoundingBox() {
         // Incoming data: latitude and longitude of south-west and north-east points (around Bordeaux for instance =) )
         double latS = 44.8;
         double latN = 44.9;
         
         double lonW = -0.6;
         double lonE = -0.7;
 
         // Transform this to a bounding box
         BoundingBox bb = new BoundingBox(latN, lonE, latS, lonW);
 
         // Calculate the geocells list to be used in the queries (optimize list of cells that complete the given bounding box)
         List<String> cells = GeocellManager.bestBboxSearchCells(bb, null);
 
         // OR if you want to use a custom "cost function"
         List<String> cells2 = GeocellManager.bestBboxSearchCells(bb, new CostFunction() {
 
             public double defaultCostFunction(int numCells, int resolution) {
                 if(numCells > 100) {
                     return Double.MAX_VALUE;
                 } else {
                     return 0;
                 }
             }
         });
 
         // Use this in a query
         // In Google App Engine, you'll have something like below. In hibernate (or whatever else), it might be a little bit different.
 //		String queryString = "select from ObjectToSave where geocellsParameter.contains(geocells)";
//		Query query = pm.newQuery(queryString);
 //	    query.declareParameters("String geocellsParameter");
 //	    List<ObjectToSave> objects = (List<ObjectToSave>) query.execute(cells);
 
         // Just checking that cells are not empty
         Assert.assertTrue(cells.size() > 0);
         Assert.assertTrue(cells2.size() > 0);
 
         // Show in the log what cells shoud be used in the query
         log.log(Level.INFO, "Geocells to use in query for PointSW("+latS+","+lonW+") ; PointNE("+latN+","+lonE+") are: "+cells);
     }
 
     /**
      * To test proximity search, you have to give your base query and it will be enhanced with geocells restrictions.
      *
      */
     // TODO configure persistent manager to run a real test
     public void testHowToQueryWithProximitySearch() {
         Point center = new Point(20.0, 12.4);
         PersistenceManager pm = null;// here put your persistent manager
         List<Object> params = new ArrayList<Object>();
         params.add("John");
         GeocellQuery baseQuery = new GeocellQuery("lastName == lastNameParam", "String lastNameParam", params);
 
         List<ObjectToSave> objects = null;
         try {
             objects = GeocellManager.proximityFetch(center, 40, 0, ObjectToSave.class, baseQuery, pm);
             Assert.assertTrue(objects.size() > 0);
         } catch (Exception e) {
            // We catch exception here because we have not configured the PersistentManager (and so the queries won't work)
         }
     }
 
 }
