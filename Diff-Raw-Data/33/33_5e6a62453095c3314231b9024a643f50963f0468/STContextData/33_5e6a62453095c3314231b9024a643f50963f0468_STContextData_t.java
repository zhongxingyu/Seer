 /*
  * Copyright (c) 2013 KloudTek Ltd
  */
 
 package com.kloudtek.systyrant.context;
 
 import com.kloudtek.systyrant.*;
 import com.kloudtek.systyrant.host.Host;
 import com.kloudtek.systyrant.provider.ProvidersManagementService;
 import com.kloudtek.systyrant.util.ListHashMap;
 import org.reflections.Reflections;
 import org.slf4j.MDC;
 
 import javax.script.ScriptEngine;
 import javax.script.ScriptEngineManager;
 import java.io.File;
 import java.net.URL;
 import java.util.*;
 import java.util.concurrent.locks.ReadWriteLock;
 import java.util.concurrent.locks.ReentrantReadWriteLock;
 
 /**
  * This object contains all the belonging to a STContext
  */
 public class STContextData {
     // Generic
 
    public Stage stage;
     public final ReentrantReadWriteLock executionLock = new ReentrantReadWriteLock();
     public List<File> tempFiles = new ArrayList<>();
     public List<Class<? extends Exception>> fatalExceptions;
     public boolean executing;
     public ThreadLocal<List<String>> importPaths = new ThreadLocal<>();
     public ResourceManager resourceManager;
     public ServiceManager serviceManager;
     public ProvidersManagementService providersManagementService = new ProvidersManagementService();
 
     // Libraries
 
     public final ReentrantReadWriteLock libraryLock = new ReentrantReadWriteLock();
     public boolean newLibAdded;
     public Reflections reflections;
     public List<Library> libraries = new ArrayList<>();
     public LibraryClassLoader libraryClassloader = new LibraryClassLoader(new URL[0], STContext.class.getClassLoader());
 
     // Resources
 
     public final ReadWriteLock rootResourceLock = new ReentrantReadWriteLock();
     public ReentrantReadWriteLock resourceListLock = new ReentrantReadWriteLock();
     public List<Resource> resources = new ArrayList<>();
     public ThreadLocal<Resource> resourceScope = new ThreadLocal<>();
     public final Map<Resource, List<Resource>> parentToPendingChildrenMap = new HashMap<>();
     public final HashSet<Resource> postChildrenExecuted = new HashSet<>();
     public Resource defaultParent;
     public List<ResourceDefinition> resourceDefinitions = new ArrayList<>();
     public HashMap<String, Resource> resourcesUidIndex = new HashMap<>();
     public HashMap<Resource, List<Resource>> parentChildIndex;
     /**
      * Flag indicating if element creation is allowed
      */
     public boolean createAllowed = true;
     public final Map<FQName, ResourceDefinition> resourceDefinitionsFQNIndex = new HashMap<>();
     public HashSet<FQName> uniqueResourcesCreated = new HashSet<>();
     public HashSet<ManyToManyResourceDependency> m2mDependencies = new HashSet<>();
     public HashSet<OneToManyResourceDependency> o2mDependencies = new HashSet<>();
 
     // Hosts
 
     public Host host;
 
     // Scripting
 
     public ScriptEngineManager scriptEngineManager = new ScriptEngineManager();
     public final HashMap<String, ScriptEngine> scriptEnginesByExtCache = new HashMap<>();
 
     // Notifications
 
     public List<AutoNotify> autoNotifications = new ArrayList<>();
     public ListHashMap<Resource, AutoNotify> autoNotificationsSourceIndex = new ListHashMap<>();
     public ListHashMap<Resource, AutoNotify> autoNotificationsTargetIndex = new ListHashMap<>();
     public final LinkedList<Notification> notificationsPending = new LinkedList<>();
 
 
     public List<Resource> getChildrensInternalList(Resource resource) {
         List<Resource> childrens = parentChildIndex.get(resource);
         if (childrens == null) {
             childrens = new ArrayList<>();
             parentChildIndex.put(resource, childrens);
         }
         return childrens;
     }
 
     public Resource getUnpreparedResource(Stage stage) {
         for (Resource resource : resources) {
             if (resource.isExecutable() && !resource.isFailed() && resource.getStage().ordinal() < stage.ordinal()) {
                 return resource;
             }
         }
         return null;
     }
 
     public void setResourceScope(Resource resource) {
         resourceScope.set(resource);
         MDC.put("resource", resource.toString());
     }
 
     public void clearResourceScope() {
         resourceScope.remove();
         MDC.remove("resource");
     }
 
     public List<AutoNotify> findAutoNotificationBySource(Resource source) {
         return autoNotificationsSourceIndex.get(source);
     }
 
     public List<AutoNotify> findAutoNotificationByTarget(Resource target) {
         return autoNotificationsTargetIndex.get(target);
     }
 
     public Notification popPendingNotifications() {
         synchronized (notificationsPending) {
             if (notificationsPending.isEmpty()) {
                 return null;
             } else {
                 return notificationsPending.removeFirst();
             }
         }
     }
 
     public void add(AutoNotify autoNotify) {
         synchronized (autoNotifications) {
             autoNotifications.add(autoNotify);
             for (Resource source : autoNotify.getSources()) {
                 autoNotificationsSourceIndex.get(source).add(autoNotify);
             }
             autoNotificationsTargetIndex.get(autoNotify.getTarget()).add(autoNotify);
         }
     }
 
     public void remove(AutoNotify autoNotify) {
         synchronized (autoNotifications) {
             autoNotifications.remove(autoNotify);
             for (Resource source : autoNotify.getSources()) {
                 autoNotificationsSourceIndex.get(source).remove(autoNotify);
             }
             for (ArrayList<AutoNotify> sourceIdxList : autoNotificationsSourceIndex.values()) {
                 sourceIdxList.remove(autoNotify);
             }
             for (ArrayList<AutoNotify> targetIdxList : autoNotificationsTargetIndex.values()) {
                 targetIdxList.remove(autoNotify);
             }
         }
     }
 
     public void mergeNotification(AutoNotify autonotify, AutoNotify toMerge) {
         synchronized (autoNotifications) {
             remove(toMerge);
             for (Resource source : toMerge.getSources()) {
                 autoNotificationsSourceIndex.get(source).add(autonotify);
             }
         }
     }
 }
