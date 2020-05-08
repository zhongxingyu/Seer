 package org.daum.library.android.sitac;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Map;
 
 import org.daum.common.genmodel.impl.*;
 import org.daum.library.android.sitac.engine.IEngine;
 import org.daum.library.android.sitac.listener.OnEngineStateChangeListener;
 import org.daum.library.ormH.persistence.PersistenceConfiguration;
 import org.daum.library.ormH.persistence.PersistenceSession;
 import org.daum.library.ormH.persistence.PersistenceSessionFactoryImpl;
 import org.daum.library.ormH.store.ReplicaStore;
 import org.daum.library.ormH.utils.PersistenceException;
 
 import android.util.Log;
 import org.daum.library.replica.listener.PropertyChangeEvent;
 import org.daum.library.replica.listener.PropertyChangeListener;
 import org.daum.library.replica.listener.SyncListener;
 import org.daum.library.replica.msg.SyncEvent;
 import org.daum.common.genmodel.*;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * SITACEngine save/update/delete data from/to the Replica/Persistence system
  */
 public class SITACEngine implements IEngine {
 	
 	private static final String TAG = "SITACEngine";
     private static final Logger logger = LoggerFactory.getLogger(SITACEngine.class);
 
     /** Persisted classes in replica */
     private final Class[] classes = {
             VehiculeImpl.class,
             ArrowActionImpl.class,
             ZoneActionImpl.class,
             CibleImpl.class,
             SourceDangerImpl.class,
             AgentImpl.class
     };
 
     private PersistenceSessionFactoryImpl factory;
 	private OnEngineStateChangeListener listener;
 	
 	public SITACEngine(final String nodeName, ReplicaStore store) {
         try {
             // configuring persistence
             PersistenceConfiguration configuration = new PersistenceConfiguration(nodeName);
             configuration.setStore(store);
             for (Class c : classes) configuration.addPersistentClass(c);
 
             // retrieve the persistence factory
             this.factory = configuration.getPersistenceSessionFactory();
 
         } catch (PersistenceException ex) {
             Log.e(TAG, "Error while initializing persistence in engine", ex);
         }
 
         // add a callback to populate engine when sync is ready
         SITACComponent.getChangeListenerInstance().addSyncListener(new SyncListener() {
             @Override
             public void sync(SyncEvent e) {
                 if (listener != null) {
                     PersistenceSession session = null;
                     try {
                         session = factory.getSession();
                         ArrayList<IModel> data = new ArrayList<IModel>();
                         for (Class c : classes) {
                             Map<String, IModel> map = session.getAll(c);
                             if (map != null && !map.isEmpty()) {
                                 data.addAll(map.values());
                             }
                         }
                         listener.onReplicaSynced(data);
 
                     } catch (PersistenceException ex) {
                         Log.e(TAG, "Error while retrieving data on sync", ex);
 
                     } finally {
                         if (session != null) session.close();
                     }
                 }
             }
         });
 
         // add callback to handle remote events on replica
         for (final Class c : classes) {
             SITACComponent.getChangeListenerInstance().addEventListener(c, new PropertyChangeListener() {
                 @Override
                 public void update(PropertyChangeEvent e) {
                     if (listener != null) {
                         PersistenceSession session = null;
                         try {
                             session = factory.getSession();
                             IModel m = null;
                             switch (e.getEvent()) {
                                 case ADD:
                                     m = (IModel) session.get(c, e.getId());
                                    logger.debug(TAG, "Replica Event: ADD "+m);
                                     listener.onAdd(m);
                                     break;
 
                                 case DELETE:
                                    logger.debug(TAG, "Replica Event: DELETE "+e.getId());
                                     listener.onDelete((String) e.getId());
                                     break;
 
                                 case UPDATE:
                                     m = (IModel) session.get(c, e.getId());
                                    logger.debug(TAG, "Replica Event: UPDATE "+m);
                                     listener.onUpdate(m);
                                     break;
                             }
 
                         } catch (PersistenceException ex) {
                             Log.e(TAG, "Error while initializing replica events handler", ex);
                         } finally {
                             if (session != null) session.close();
                         }
                     }
                 }
             });
         }
 	}
 	
 	public void add(IModel m) {
         PersistenceSession session = null;
         try {
             session = factory.getSession();
             session.save(m);
             
         } catch (PersistenceException ex) {
             Log.e(TAG, "Error while adding object in Replica", ex);
         } finally {
             if (session != null) session.close();
         }
 	}
 
 	public void update(IModel m) {
         PersistenceSession session = null;
         try {
             session = factory.getSession();
             session.update(m);
 
         } catch (PersistenceException ex) {
             Log.e(TAG, "Error while updating object in Replica", ex);
         } finally {
             if (session != null) session.close();
         }
 	}
 
     public void updateAll() {
         PersistenceSession session = null;
         try {
             session = factory.getSession();
             ArrayList<IModel> data = new ArrayList<IModel>();
             for (Class c : classes) {
                 Map<Object, IModel> map = session.getAll(c);
                 if (map != null && !map.isEmpty()) {
                     data.addAll(map.values());
                 }
             }
             if (listener != null) listener.onUpdateAll(data);
 
         } catch (PersistenceException ex) {
             Log.e(TAG, "Error while updating object in Replica", ex);
         } finally {
             if (session != null) session.close();
         }
     }
 	
 	public void delete(IModel m) {
         PersistenceSession session = null;
         try {
             session = factory.getSession();
             session.delete(m);
             
         } catch (PersistenceException ex) {
             Log.e(TAG, "Error while deleting object in Replica", ex);
         } finally {
             if (session != null) session.close();
         }
 	}
 
     public void setHandler(OnEngineStateChangeListener handler) {
         this.listener = handler;
         updateAll();
     }
 }
