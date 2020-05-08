 /*
  * Copyright (c) 2010 BlipIt Committers
  * All rights reserved.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
  * implied. See the License for the specific language governing
  * permissions and limitations under the License.
  *
  * See also http://www.apache.org/licenses/LICENSE-2.0.html for an
  * explanation of the license and how it is applied.
  */
 
 package com.thoughtworks.blipit.persistance;
 
 import javax.jdo.JDOHelper;
 import javax.jdo.PersistenceManager;
 import javax.jdo.PersistenceManagerFactory;
 import javax.jdo.Query;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 public class DataStoreHelper {
     private static final String PERSISTENCE_PROP_NAME = "transactions-optional";
     private static PersistenceManagerFactory factory = JDOHelper.getPersistenceManagerFactory(PERSISTENCE_PROP_NAME);
     private static final Logger log = Logger.getLogger(DataStoreHelper.class.getName());
 
     private DataStoreHelper() {
     }
 
     public static PersistenceManager getPersistenceManager() {
         return factory.getPersistenceManager();
     }
 
     public static <T> T save(T instance) {
         PersistenceManager persistenceManager = null;
         try {
             persistenceManager = getPersistenceManager();
             return persistenceManager.makePersistent(instance);
         } finally {
             if (persistenceManager != null) persistenceManager.close();
         }
     }
 
     public static <T> List<T> retrieveAllAndProcess(Class<T> clazz) throws Exception {
         PersistenceManager persistenceManager = null;
         Query query = null;
         try {
             persistenceManager = getPersistenceManager();
             query = persistenceManager.newQuery(clazz);
            List<T> entities = (List<T>) query.execute();
            persistenceManager.detachCopyAll(entities);
            return entities;
             
         } catch (Exception e) {
             log.log(Level.SEVERE, "Error occured while fetching elements of type " + clazz.getSimpleName() + " from data store", e);
             throw e;
         } finally {
             if (query != null) query.closeAll();
             if (persistenceManager != null) persistenceManager.close();
         }
     }
 }
