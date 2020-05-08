 /*
  * Copyright (C) 2013 Gregory S. Meiste  <http://gregmeiste.com>
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.meiste.greg.ptwgame;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Logger;
 
 import com.google.appengine.api.datastore.DatastoreService;
 import com.google.appengine.api.datastore.DatastoreServiceFactory;
 import com.google.appengine.api.datastore.Entity;
 import com.google.appengine.api.datastore.FetchOptions;
 import com.google.appengine.api.datastore.PreparedQuery;
 import com.google.appengine.api.datastore.Query;
 import com.google.appengine.api.datastore.Query.FilterOperator;
 import com.google.appengine.api.datastore.Query.FilterPredicate;
 import com.google.appengine.api.datastore.Query.SortDirection;
 import com.google.appengine.api.datastore.Transaction;
 
 public final class DriverDatastore {
 
     private static final String DRIVER_TYPE = "Driver";
     private static final String DRIVER_FIRST_NAME_PROPERTY = "firstName";
     private static final String DRIVER_LAST_NAME_PROPERTY = "lastName";
     private static final String DRIVER_NUMBER_PROPERTY = "number";
 
     private static final FetchOptions DEFAULT_FETCH_OPTIONS = FetchOptions.Builder.withDefaults();
 
     private static final Logger logger =
             Logger.getLogger(DriverDatastore.class.getName());
     private static final DatastoreService datastore =
             DatastoreServiceFactory.getDatastoreService();
 
     private DriverDatastore() {
         throw new UnsupportedOperationException();
     }
 
     public static boolean add(final Driver driver) {
         final Transaction txn = datastore.beginTransaction();
         try {
             if (findDriverByNumber(driver.mNumber) != null) {
                 logger.warning(driver.mNumber + " is already taken; ignoring.");
                 return false;
             }
             final Entity entity = new Entity(DRIVER_TYPE);
             entity.setProperty(DRIVER_NUMBER_PROPERTY, driver.mNumber);
             entity.setProperty(DRIVER_FIRST_NAME_PROPERTY, driver.mFirstName);
             entity.setProperty(DRIVER_LAST_NAME_PROPERTY, driver.mLastName);
             datastore.put(entity);
             txn.commit();
         } finally {
             if (txn.isActive()) {
                 txn.rollback();
             }
         }
 
         return true;
     }
 
     public static List<Driver> getAll() {
         final List<Driver> drivers = new ArrayList<Driver>();
         final Transaction txn = datastore.beginTransaction();
         try {
             final Query query = new Query(DRIVER_TYPE);
             query.addSort(DRIVER_LAST_NAME_PROPERTY, SortDirection.ASCENDING);
             query.addSort(DRIVER_FIRST_NAME_PROPERTY, SortDirection.ASCENDING);
             final Iterable<Entity> entities =
                     datastore.prepare(query).asIterable(DEFAULT_FETCH_OPTIONS);
             for (final Entity entity : entities) {
                 drivers.add(entityToDriver(entity));
             }
             txn.commit();
         } finally {
             if (txn.isActive()) {
                 txn.rollback();
             }
         }
         return drivers;
     }
 
     public static Driver findDriverByNumber(final int num) {
         final Query query = new Query(DRIVER_TYPE)
         .setFilter(new FilterPredicate(DRIVER_NUMBER_PROPERTY, FilterOperator.EQUAL, num));
         final PreparedQuery preparedQuery = datastore.prepare(query);
         final List<Entity> entities = preparedQuery.asList(DEFAULT_FETCH_OPTIONS);
        Driver driver = null;
         if (!entities.isEmpty()) {
            driver = entityToDriver(entities.get(0));
         }
         final int size = entities.size();
         if (size > 1) {
             logger.severe(
                     "Found " + size + " entities for number " + num + ": " + entities);
         }
        return driver;
     }
 
     private static Driver entityToDriver(final Entity e) {
         final Driver driver = new Driver();
         driver.mFirstName = (String) e.getProperty(DRIVER_FIRST_NAME_PROPERTY);
         driver.mLastName = (String) e.getProperty(DRIVER_LAST_NAME_PROPERTY);
         driver.mNumber = ((Long) e.getProperty(DRIVER_NUMBER_PROPERTY)).intValue();
         return driver;
     }
 }
