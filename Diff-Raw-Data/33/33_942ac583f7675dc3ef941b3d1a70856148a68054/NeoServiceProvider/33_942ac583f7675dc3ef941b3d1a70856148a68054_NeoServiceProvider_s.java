 /* AWE - Amanzi Wireless Explorer
  * http://awe.amanzi.org
  * (C) 2008-2009, AmanziTel AB
  *
  * This library is provided under the terms of the Eclipse Public License
  * as described at http://www.eclipse.org/legal/epl-v10.html. Any use,
  * reproduction or distribution of the library constitutes recipient's
  * acceptance of this agreement.
  *
  * This library is distributed WITHOUT ANY WARRANTY; without even the
  * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  */
 
 package org.amanzi.neo.db.manager;
 
 import org.neo4j.graphdb.GraphDatabaseService;
 import org.neo4j.index.lucene.LuceneIndexService;
 
 /**
  * <p>
  *NeoService provider - do not use in services - only for limited using. You should use NeoServiceProviderUi
  * </p>
  * @author tsinkel_a
  * @since 1.0.0
  */
  public class NeoServiceProvider {
     /*
      * Instance of NeoServiceProvider
      */
     private static NeoServiceProvider provider;
     /*
      * Location of Neo-database
      */
     private String databaseLocation;
     private LuceneIndexService indexService;
     /*
      * NeoService
      */
     private GraphDatabaseService neoService;
     private INeoManager manager;
     /**
      * Creates an instance of NeoServiceProvider
      *
      * @return instance of NeoServiceProvider
      */
     
     public static NeoServiceProvider getProvider() {
         
         if (provider == null) {
             provider = new NeoServiceProvider();
         }
         return provider;
     }
 
     /**
      * Returns NeoService
      *
      * @return
      */
     
     public GraphDatabaseService getService() {
         return neoService;
     }
     /**
      * Initializes NeoService and NeoServiceManager
      */
     
     public void init(GraphDatabaseService service,String databaseLocation,INeoManager manager) {
         this.manager = manager;
        if (neoService!=null){
            return;
        }
         this.databaseLocation = databaseLocation;
         neoService = service;
        indexService = new LuceneIndexService(neoService);
     }
     public LuceneIndexService getIndexService() {
         return indexService;
     }    
     /**
      * Stops Neo Service 
      * 
      */
     
     public void stopNeo() {
         if (indexService != null) {
             indexService.shutdown();
         }
         if (neoService != null) {
             neoService.shutdown();
         }
         indexService = null;
         neoService = null;
     }
 
     /**
      *
      * @return
      */
     public String getDefaultDatabaseLocation() {
         return databaseLocation;
     }
 
 
     public void commit() {
         if (manager!=null){
             manager.commit();
         }
     }
     public void rollback() {
         if (manager!=null){
             manager.rollback();
         }
     }
 
 
 }
