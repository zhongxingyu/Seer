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
 package org.amanzi.neo.services.ui;
 
 import java.util.ArrayList;
 
 import org.amanzi.neo.db.manager.INeoManager;
 import org.amanzi.neo.db.manager.NeoServiceProvider;
 import org.eclipse.jface.preference.IPreferenceStore;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.ui.PlatformUI;
 import org.neo4j.graphdb.GraphDatabaseService;
 import org.neo4j.index.lucene.LuceneIndexService;
 import org.neo4j.neoclipse.Activator;
 import org.neo4j.neoclipse.graphdb.GraphDbServiceEvent;
 import org.neo4j.neoclipse.graphdb.GraphDbServiceEventListener;
 import org.neo4j.neoclipse.graphdb.GraphDbServiceManager;
 import org.neo4j.neoclipse.preference.Preferences;
 
 /**
  * Provider that give access to NeoService
  * 
  * @author Lagutko_N
  * @since 1.0.0
  */
 
 public class NeoServiceProviderUi implements INeoManager{
     
 
     private static NeoServiceProviderUi provider=new NeoServiceProviderUi();
 
    
 
     
     /*
      * Listeners of NeoServiceProvider
      */
     private ArrayList<INeoServiceProviderListener> listeners = new ArrayList<INeoServiceProviderListener>(0);
     
 
 
     /*
      * Current Display 
      */
     private Display display;
 
 
     private String databaseLocation;
     
  
     
     /**
      * Creates an instance of NeoServiceProvider
      *
      * @return instance of NeoServiceProvider
      */
     
     public static NeoServiceProviderUi getProvider() {
         return provider;
     }
     
     /**
      * Creates an instance of NeoServiceProvider
      *
      * @return instance of NeoServiceProvider
      */
     
     public static void initProvider(GraphDatabaseService service) {
         getProvider().init(service);
     }
     private void init(GraphDatabaseService service) {
         NeoServiceProvider.getProvider().init(service,getDefaultDatabaseLocation(),this);
     } 
     /**
      * Protected constructor
      */
     
     protected NeoServiceProviderUi() {
         try {
             this.display = PlatformUI.getWorkbench().getDisplay();
         } catch (RuntimeException e) {
             //We are probably running unit tests, log and error and continue
             System.err.println("Failed to get display: "+e);
         }
     }
     
 
 
 
     
 
 
     /**
      * Returns current location of Neo-database
      * 
      * @return
      */
     
     public String getDefaultDatabaseLocation() {
         if (databaseLocation == null) {
             IPreferenceStore store = Activator.getDefault().getPreferenceStore();
             databaseLocation = store.getString(Preferences.DATABASE_RESOURCE_URI);
             if ((databaseLocation == null) || (databaseLocation.trim().isEmpty())) {
                 databaseLocation = store.getString(Preferences.DATABASE_LOCATION);
             }
         }
         
         return databaseLocation;
     }
     
     /**
      * Add listener to Provider 
      *
      * @param listener listener for NeoServiceProvider
      */
     
     public void addServiceProviderListener(INeoServiceProviderListener listener) {
         listeners.add(listener);       
         
     }
     public GraphDatabaseService getService() {
         NeoServiceProvider neoPr = NeoServiceProvider.getProvider();
        neoPr.init(Activator.getDefault().getGraphDbServiceSafely(),getDefaultDatabaseLocation(),this);
        return neoPr.getService();
     }   
     /**
      * Removes listener from Provider 
      *
      * @param listener listener of NeoServiceProvider
      */
     
     public void removeServiceProviderListener(INeoServiceProviderListener listener) {
         listeners.remove(listener);
     }
     
     /**
      * Shutdown NeoServiceManager
      */
     
     public void shutdown() {        
         GraphDbServiceManager neoManager=Activator.getDefault().getGraphDbServiceManager();
         if (neoManager != null) {
             neoManager.commit();
             neoManager = null;            
         }
     }
     
     /**
      * Commits changes
      */
     @Override
     public void commit() {
         final GraphDbServiceManager neoManager=Activator.getDefault().getGraphDbServiceManager();
         //Lagutko, 12.10.2009, also check is Workbech closing?
         boolean currentThread = (display == null) ||
         						PlatformUI.getWorkbench().isClosing() ||                                 
                                 Thread.currentThread().equals(display.getThread());
         if (currentThread) {
             if (neoManager!=null) {
                 neoManager.commit();
             }
         } else {
             display.syncExec(new Runnable() {
 
                 @Override
                 public void run() {
                     neoManager.commit();
                 }
             });
         }
     }
     
     /**
      * Rollback changes
      */
     public void rollback() {
         final GraphDbServiceManager neoManager=Activator.getDefault().getGraphDbServiceManager();
         //Lagutko, 12.10.2009, also check is Workbech closing?
         boolean currentThread = PlatformUI.getWorkbench().isClosing() || 
                                 (display == null) || 
                                 Thread.currentThread().equals(display.getThread());
         if (currentThread) {
             neoManager.rollback();
         } else {
             display.syncExec(new Runnable() {
 
                 @Override
                 public void run() {
                     neoManager.rollback();
                 }
             });
         }
     }
     
     /**
      * Listener that listens for event of NeoServiceManager and provides them
      * to listeners of NeoServiceProvides
      * 
      * @author Lagutko_N
      * @since 1.0.0
      */
     
     private class DefaultServiceListener implements GraphDbServiceEventListener {
 
     	@SuppressWarnings("unchecked")
         public void serviceChanged(GraphDbServiceEvent event) {
             //Lagutko, 12.10.2009, copy list of listeners to local variable
             ArrayList<INeoServiceProviderListener> copiedListener = (ArrayList<INeoServiceProviderListener>)listeners.clone();
             for (INeoServiceProviderListener listener : copiedListener) {
                 Object source = event.getSource();
                 switch (event.getStatus()) {
                 case STOPPED:                    
                     listener.onNeoStop(source);
                     shutdown();
                     break;
                 case STARTED:
                     listener.onNeoStart(source);
                     break;
                 case COMMIT:
                     listener.onNeoCommit(source);
                     break;
                 case ROLLBACK:
                     listener.onNeoRollback(source);
                     break;
                 }                    
             }
         }
         
     }
 
     
     /**
      * Stops Neo Service 
      * 
      */
     
     public void stopNeo() {
         final GraphDbServiceManager neoManager=Activator.getDefault().getGraphDbServiceManager();
     	if (neoManager != null) {
     	    neoManager.commit();
     		neoManager.stopGraphDbService();
     	}
     	NeoServiceProvider.getProvider().stopNeo();
     }
     
     public LuceneIndexService getIndexService() {
     	return NeoServiceProvider.getProvider().getIndexService();
     }
 
 }
