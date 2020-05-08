 /*
  * Made by Wannes 'W' De Smet
  * (c) 2011 Wannes De Smet
  * All rights reserved.
  * 
  */
 package net.wgr.xenmaster.api.util;
 
 import java.lang.reflect.Constructor;
 import java.lang.reflect.InvocationTargetException;
 import java.util.Map;
 import java.util.concurrent.CopyOnWriteArrayList;
 import net.wgr.xenmaster.api.Event;
 import net.wgr.xenmaster.api.Task;
 import net.wgr.xenmaster.api.XenApiEntity;
 import net.wgr.xenmaster.controller.BadAPICallException;
 import net.wgr.xenmaster.controller.Controller;
 import net.wgr.xenmaster.monitoring.EventHandler.EventListener;
 import net.wgr.xenmaster.monitoring.MonitoringAgent;
 import org.apache.log4j.Logger;
 import org.infinispan.Cache;
 import org.infinispan.configuration.cache.CacheMode;
 import org.infinispan.configuration.cache.ConfigurationBuilder;
 import org.infinispan.configuration.global.GlobalConfigurationBuilder;
 import org.infinispan.manager.DefaultCacheManager;
 import org.infinispan.manager.EmbeddedCacheManager;
 import org.infinispan.remoting.transport.jgroups.JGroupsTransport;
 
 /**
  * 
  * @created Dec 19, 2011
  * @author double-u
  */
 public class CachingFacility {
 
     protected Mode mode;
     protected Cache<String, XenApiEntity> cache;
     protected CopyOnWriteArrayList<Class> loadedEntityClasses;
     protected EmbeddedCacheManager ecm;
     private static CachingFacility instance;
 
     private CachingFacility(boolean distributed) {
         this.mode = Mode.LAZY;
         this.cache = buildCache(distributed);
         this.loadedEntityClasses = new CopyOnWriteArrayList<>();
         registerCacheUpdater();
     }
 
     protected final void registerCacheUpdater() {
         MonitoringAgent.instance().getEventHandler().addListener(new EventListener() {
 
             @Override
             public void eventOcurred(Event event) {
                 // Tasks are bound to a process, not the a result so we're not interested in storing these
                 if (event.getSnapshot() == null || Task.class.isAssignableFrom(event.getSnapshot().getClass())) {
                     return;
                 }
 
                 if (event.getOperation() == Event.Operation.DEL) {
                     remove(event.getSnapshot());
                 } else {
                     update(event.getSnapshot(), event.getOperation() == Event.Operation.ADD);
                 }
             }
         });
     }
 
     protected Cache buildCache(boolean distributed) {
         ConfigurationBuilder cb = new ConfigurationBuilder();
         GlobalConfigurationBuilder gcb = new GlobalConfigurationBuilder();
         gcb.transport().transport(new JGroupsTransport());
         gcb.transport().clusterName("XenMaster");
         cb.clustering().cacheMode((distributed ? CacheMode.DIST_SYNC : CacheMode.LOCAL));
 
         if (distributed) {
             ecm = new DefaultCacheManager(gcb.build(), cb.build());
         } else {
             ecm = new DefaultCacheManager(cb.build());
         }
         return ecm.getCache();
     }
 
     public static CachingFacility instance() {
         return instance(true);
     }
 
     public static CachingFacility instance(boolean distributed) {
         if (instance == null) {
             instance = new CachingFacility(distributed);
         }
         return instance;
     }
 
     public void stop() {
         cache.stop();
     }
 
     public EmbeddedCacheManager getCacheManager() {
         return ecm;
     }
 
     public static enum Mode {
 
         PREHEAT, LAZY
     }
 
     protected <T extends XenApiEntity> void heatCache(Class<T> target) {
         try {
             Map<String, Object> objects = (Map<String, Object>) Controller.dispatch(XenApiEntity.getAPIName(target) + ".get_all_records");
             Constructor<T> ctor = target.getConstructor(String.class, boolean.class);
             for (Map.Entry<String, Object> entry : objects.entrySet()) {
                 T obj = ctor.newInstance(entry.getKey(), false);
                 obj.fillOut((Map<String, Object>) entry.getValue());
                 cache.put(entry.getKey(), obj);
             }
             loadedEntityClasses.add(target);
         } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
             Logger.getLogger(getClass()).debug("Failed to contruct object of type " + target.getCanonicalName(), ex);
         } catch (BadAPICallException ex) {
             Logger.getLogger(getClass()).debug(target.getCanonicalName() + " does not have a getAll method", ex);
         }
     }
 
     public static <T extends XenApiEntity> T get(String reference, Class<T> target) {
         return instance().getEntity(reference, target);
     }
 
     public <T extends XenApiEntity> void update(T object, boolean force) {
         if (object == null) {
             throw new IllegalArgumentException("Cannot update null");
         }
 
        if (isCached(object.getReference(false), object.getClass()) || (object.getReference() != null && force)) {
             cache.put(object.getReference(), object);
         } else {
             // We only are interested in updates for things we've cached, others will always be newest available ones when they are retreived
             Logger.getLogger(getClass()).debug("Object " + object.getReference(false) + '(' + object.getClass().getCanonicalName() + ") was not inside cache and therefore could not be updated.");
         }
     }
 
     public void remove(XenApiEntity object) {
         if (object == null) {
             throw new IllegalArgumentException("Cannot remove null");
         }
 
         if (isCached(object.getReference(false), object.getClass())) {
             cache.remove(object.getReference());
         } else {
             // We only are interested in updates for things we've cached, others will always be newest available ones when they are retreived
             Logger.getLogger(getClass()).debug("Object " + object.getReference(false) + '(' + object.getClass().getCanonicalName() + ") was not inside cache and therefore could not be updated.");
         }
     }
 
     public boolean isCached(String reference, Class target) {
         return reference != null && cache.containsKey(reference) && target != null && target.isAssignableFrom(cache.get(reference).getClass());
     }
 
     public <T extends XenApiEntity> T getEntity(String reference, Class<T> target) {
         if (reference == null) {
             return null;
         }
         if (!cache.containsKey(reference) && !loadedEntityClasses.contains(target)) {
             heatCache(target);
         }
 
         if (cache.containsKey(reference)) {
             if (target != null && target.isAssignableFrom(cache.get(reference).getClass())) {
                 return (T) cache.get(reference);
             } else {
                 Logger.getLogger(getClass()).error("Cached entity has an illegal type " + cache.get(reference).getClass().getCanonicalName() + " instead of " + target.getCanonicalName());
                 return null;
             }
         }
 
         try {
             Constructor c = target.getConstructor(String.class, boolean.class);
             T newObject = (T) c.newInstance(reference, !reference.isEmpty());
             return newObject;
         } catch (IllegalAccessException | IllegalArgumentException | InstantiationException | InvocationTargetException | NoSuchMethodException ex) {
             Logger.getLogger(getClass()).error("Failed to initialize object of type " + target.getCanonicalName(), ex);
         }
 
         return null;
     }
 }
