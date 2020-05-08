 /* Copyright (c) 2001 - 2008 TOPP - www.openplans.org. All rights reserved.
  * This code is licensed under the GPL 2.0 license, available at the root
  * application directory.
  */
 
 package org.geoserver.catalog.hibernate;
 
 import java.io.Serializable;
 import java.lang.reflect.Method;
 import java.lang.reflect.Proxy;
 import java.net.URI;
 import java.rmi.server.UID;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.apache.commons.collections.MultiHashMap;
 import org.apache.commons.io.FilenameUtils;
 import org.geoserver.catalog.Catalog;
 import org.geoserver.catalog.CatalogInfo;
 import org.geoserver.catalog.CatalogVisitor;
 import org.geoserver.catalog.CoverageDimensionInfo;
 import org.geoserver.catalog.CoverageInfo;
 import org.geoserver.catalog.CoverageStoreInfo;
 import org.geoserver.catalog.DataStoreInfo;
 import org.geoserver.catalog.FeatureTypeInfo;
 import org.geoserver.catalog.LayerGroupInfo;
 import org.geoserver.catalog.LayerInfo;
 import org.geoserver.catalog.MapInfo;
 import org.geoserver.catalog.MetadataMap;
 import org.geoserver.catalog.NamespaceInfo;
 import org.geoserver.catalog.ResourceInfo;
 import org.geoserver.catalog.ResourcePool;
 import org.geoserver.catalog.StoreInfo;
 import org.geoserver.catalog.StyleInfo;
 import org.geoserver.catalog.WorkspaceInfo;
 import org.geoserver.catalog.event.CatalogAddEvent;
 import org.geoserver.catalog.event.CatalogEvent;
 import org.geoserver.catalog.event.CatalogListener;
 import org.geoserver.catalog.event.CatalogModifyEvent;
 import org.geoserver.catalog.event.CatalogPostModifyEvent;
 import org.geoserver.catalog.event.CatalogRemoveEvent;
 import org.geoserver.catalog.event.impl.CatalogAddEventImpl;
 import org.geoserver.catalog.event.impl.CatalogModifyEventImpl;
 import org.geoserver.catalog.event.impl.CatalogPostModifyEventImpl;
 import org.geoserver.catalog.event.impl.CatalogRemoveEventImpl;
 import org.geoserver.catalog.hibernate.beans.LayerInfoImplHb;
 import org.geoserver.catalog.hibernate.beans.NamespaceInfoImplHb;
 import org.geoserver.catalog.hibernate.beans.StyleInfoImplHb;
 import org.geoserver.catalog.hibernate.beans.WorkspaceInfoImplHb;
 import org.geoserver.catalog.impl.CoverageDimensionImpl;
 import org.geoserver.catalog.impl.CoverageInfoImpl;
 import org.geoserver.catalog.impl.FeatureTypeInfoImpl;
 import org.geoserver.catalog.impl.LayerGroupInfoImpl;
 import org.geoserver.catalog.impl.ModificationProxy;
 import org.geoserver.catalog.impl.ResolvingProxy;
 import org.geoserver.catalog.impl.ResourceInfoImpl;
 import org.geoserver.catalog.impl.StoreInfoImpl;
 import org.geoserver.catalog.impl.StyleInfoImpl;
 import org.geoserver.hibernate.dao.CatalogDAO;
 import org.geoserver.ows.util.ClassProperties;
 import org.geoserver.ows.util.OwsUtils;
 import org.geoserver.platform.GeoServerResourceLoader;
 import org.geotools.util.SoftValueHashMap;
 import org.geotools.util.logging.Logging;
 import org.opengis.feature.type.Name;
 import org.springframework.beans.BeansException;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.ApplicationContextAware;
 import org.springframework.web.context.WebApplicationContext;
 import org.vfny.geoserver.global.GeoserverDataDirectory;
 
 /**
  * A {@link Catalog} implementation based on Hibernate
  * 
  * @author ETj
  */
 
 public class HibCatalogImpl implements Catalog, Serializable, ApplicationContextAware {
 
     private final static Logger LOGGER = Logging.getLogger(HibCatalogImpl.class);
 
     /**
      *
      */
     private SoftValueHashMap<String, ResourceInfo> resourceInfoCache = new SoftValueHashMap<String, ResourceInfo>();
 
     /**
      *
      */
     private CatalogDAO catalogDAO;
 
     /**
      * Flag indicating whether events are fired on commit or as they happen
      */
     private boolean fireEventsOnCommit = false;
 
     /**
      * listeners
      */
     private List<CatalogListener> listeners = new ArrayList<CatalogListener>();
 
     /**
      * events
      * 
      * TODO: ETJ rework this: what do we need the key for in this map?
      */
     private Map<HibCatalogImpl, CatalogEvent> events = Collections
             .synchronizedMap(new MultiHashMap());
 
     /**
      * resources
      */
     private ResourcePool resourcePool;
 
     // really needed??
     private GeoServerResourceLoader resourceLoader;
 
     private HibCatalogImpl() {
         super();
         resourcePool = new ResourcePool(this);
 
         listeners.add(new HibCatalogUpdater());
     }
 
     public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
         GeoserverDataDirectory.init((WebApplicationContext) applicationContext);
     }
 
     /**
      * @see Catalog#getFactory()
      * @see HibernateCatalogFactory
      */
     public HibCatalogFactoryImpl getFactory() {
         return new HibCatalogFactoryImpl(this);
     }
 
     /**
      * Sets whether to fire events on commits, intended to be used by unit tests only?
      */
     void setFireEventsOnCommit(boolean fireEventsOnCommit) {
         this.fireEventsOnCommit = fireEventsOnCommit;
     }
 
     boolean isFireEventsOnCommit() {
         return fireEventsOnCommit;
     }
 
     // Store methods
     public void add(StoreInfo store) {
 
         if (LOGGER.isLoggable(Level.INFO))
             LOGGER.info("Adding store " + store.getClass().getSimpleName() + " " + store.getName());
 
         if (store.getWorkspace() == null) {
             store.setWorkspace(catalogDAO.getDefaultWorkspace());
         }
 
         validate(store, true);
         resolve(store);
         StoreInfo unwrapped = ModificationProxy.unwrap(store);
         unwrapped.setWorkspace(ModificationProxy.unwrap(unwrapped.getWorkspace()));
         catalogDAO.save(unwrapped);
         ((StoreInfoImpl) store).setId(store.getId()); // make the ModificationProxy aware of the id
         if (LOGGER.isLoggable(Level.INFO))
             LOGGER.info("Added store " + store.getClass().getSimpleName() + "[id: " + store.getId()
                     + " name:" + store.getName() + "]");
         added(store);
     }
 
     void validate(StoreInfo store, boolean isNew) {
         if (isNull(store.getName())) {
             throw new IllegalArgumentException("Store name must not be null");
         }
         if (store.getWorkspace() == null) {
             throw new IllegalArgumentException("Store must be part of a workspace");
         }
 
         WorkspaceInfo workspace = store.getWorkspace();
         StoreInfo existing = getStoreByName(workspace, store.getName(), StoreInfo.class);
         if (existing != null && !existing.getId().equals(store.getId())) {
             String msg = "Store '" + store.getName() + "' already exists in workspace '"
                     + workspace.getName() + "'";
             throw new IllegalArgumentException(msg);
         }
     }
 
     public void remove(StoreInfo store) {
         if (!getResourcesByStore(store, ResourceInfo.class).isEmpty()) {
             throw new IllegalArgumentException("Unable to delete non-empty store.");
         }
         store = unwrap(store);
         catalogDAO.delete(store);
         removed(store);
     }
 
     public void save(StoreInfo store) {
         validate(store, false);
 
         if (store.getId() == null) {
             // add it instead of saving
             add(store);
             return;
         }
 
         saved(store);
     }
 
     private Class<? extends StoreInfo> mapStoreClass(StoreInfo store) {
         if (store instanceof DataStoreInfo)
             return DataStoreInfo.class;
         else if (store instanceof CoverageStoreInfo)
             return CoverageStoreInfo.class;
         else
             return StoreInfo.class; // should not happen
     }
 
     private Class<? extends ResourceInfo> mapResourceClass(ResourceInfo resource) {
         if (resource instanceof FeatureTypeInfo)
             return FeatureTypeInfo.class;
         else if (resource instanceof CoverageInfo)
             return CoverageInfo.class;
         else
             return ResourceInfo.class; // should not happen
     }
 
     protected <T> T createProxy(T obj, Class<T> clazz) {
         return obj;
     }
 
     protected <T> List<T> createProxyList(List<T> list, Class<T> clazz) {
         return list;
     }
 
     public <T extends StoreInfo> T getStore(String id, Class<T> clazz) {
         StoreInfo store = catalogDAO.getStore(id, clazz);
         if (store == null) {
             return null;
         } else {
             resolve(store);
             return createProxy((T) store, (Class<T>) mapStoreClass(store));
         }
     }
 
     public <T extends StoreInfo> T getStoreByName(String name, Class<T> clazz) {
         T freestore = getStoreByName((WorkspaceInfo) null, name, clazz);
         if (freestore != null) {
             return freestore;
         }
 
         // look for secondary match
         List matches = catalogDAO.getStoresByName(name, clazz);
         if (matches.size() == 1) {
             StoreInfo store = (StoreInfo) matches.get(0);
             resolve(store);
             return createProxy((T) store, (Class<T>) mapStoreClass(store));
         }
 
         return null;
     }
 
     public <T extends StoreInfo> T getStoreByName(WorkspaceInfo workspace, String name,
             Class<T> clazz) {
 
         if (workspace == null) {
             workspace = catalogDAO.getDefaultWorkspace();
         } else {
             workspace = ModificationProxy.unwrap(workspace);
         }
 
         StoreInfo store = catalogDAO.getStoreByName(workspace, name, clazz);
         if (store == null) {
             return null;
         } else {
             resolve(store);
             return createProxy((T) store, (Class<T>) mapStoreClass(store));
         }
 
     }
 
     public <T extends StoreInfo> T getStoreByName(String workspaceName, String name, Class<T> clazz) {
         return getStoreByName(workspaceName != null ? getWorkspaceByName(workspaceName) : null,
                 name, clazz);
     }
 
     public <T extends StoreInfo> List<T> getStoresByWorkspace(String workspaceName, Class<T> clazz) {
 
         WorkspaceInfo workspace = null;
         if (workspaceName != null) {
             workspace = getWorkspaceByName(workspaceName);
             if (workspace == null) {
                 return Collections.EMPTY_LIST;
             }
         }
 
         return getStoresByWorkspace(workspace, clazz);
     }
 
     public <T extends StoreInfo> List<T> getStoresByWorkspace(WorkspaceInfo workspace,
             Class<T> clazz) {
 
         if (workspace == null) {
             workspace = catalogDAO.getDefaultWorkspace();
         } else
             workspace = ModificationProxy.unwrap(workspace);
 
         List matches = catalogDAO.getStoresByWorkspace(workspace, clazz);
 
         for (StoreInfo store : (List<StoreInfo>) matches) {
             resolve(store);
         }
 
         Class proxyclass = matches.isEmpty() ? clazz : mapStoreClass((StoreInfo) matches.get(0));
         return createProxyList(matches, proxyclass);
     }
 
     public List getStores(Class clazz) {
         List stores = catalogDAO.getStores(clazz);
         for (StoreInfo store : (List<StoreInfo>) stores) {
             resolve(store);
         }
         Class proxyclass = stores.isEmpty() ? clazz : mapStoreClass((StoreInfo) stores.get(0));
         return createProxyList(stores, proxyclass);
     }
 
     public DataStoreInfo getDataStore(String id) {
         return (DataStoreInfo) getStore(id, DataStoreInfo.class);
     }
 
     public DataStoreInfo getDataStoreByName(String name) {
         return (DataStoreInfo) getStoreByName(name, DataStoreInfo.class);
     }
 
     public DataStoreInfo getDataStoreByName(String workspaceName, String name) {
         return (DataStoreInfo) getStoreByName(workspaceName, name, DataStoreInfo.class);
     }
 
     public DataStoreInfo getDataStoreByName(WorkspaceInfo workspace, String name) {
         return (DataStoreInfo) getStoreByName(workspace, name, DataStoreInfo.class);
     }
 
     public List<DataStoreInfo> getDataStoresByWorkspace(String workspaceName) {
         return getStoresByWorkspace(workspaceName, DataStoreInfo.class);
     }
 
     public List<DataStoreInfo> getDataStoresByWorkspace(WorkspaceInfo workspace) {
         return getStoresByWorkspace(workspace, DataStoreInfo.class);
     }
 
     public List<DataStoreInfo> getDataStores() {
         return getStores(DataStoreInfo.class);
     }
 
     public CoverageStoreInfo getCoverageStore(String id) {
         return (CoverageStoreInfo) getStore(id, CoverageStoreInfo.class);
     }
 
     public CoverageStoreInfo getCoverageStoreByName(String name) {
         return (CoverageStoreInfo) getStoreByName(name, CoverageStoreInfo.class);
     }
 
     public CoverageStoreInfo getCoverageStoreByName(String workspaceName, String name) {
         return getStoreByName(workspaceName, name, CoverageStoreInfo.class);
     }
 
     public CoverageStoreInfo getCoverageStoreByName(WorkspaceInfo workspace, String name) {
         return getStoreByName(workspace, name, CoverageStoreInfo.class);
     }
 
     public List<CoverageStoreInfo> getCoverageStoresByWorkspace(String workspaceName) {
         return getStoresByWorkspace(workspaceName, CoverageStoreInfo.class);
     }
 
     public List<CoverageStoreInfo> getCoverageStoresByWorkspace(WorkspaceInfo workspace) {
         return getStoresByWorkspace(workspace, CoverageStoreInfo.class);
     }
 
     public List<CoverageStoreInfo> getCoverageStores() {
         return getStores(CoverageStoreInfo.class);
     }
 
     // Resource methods
     public void add(ResourceInfo resource) {
         if (resource.getNamespace() == null) {
             // default to default namespace
             resource.setNamespace(catalogDAO.getDefaultNamespace());
         }
 
         validate(resource, true);
         resolve(resource);
         // resources.put(resource.getClass(), resource);
         ResourceInfo unwrappedResource = ModificationProxy.unwrap(resource);
         unwrappedResource.setNamespace(ModificationProxy.unwrap(unwrappedResource.getNamespace()));
         StoreInfo unwrappedStore = ModificationProxy.unwrap(resource.getStore());
         unwrappedResource.setStore(unwrappedStore);
         unwrappedStore.setWorkspace(ModificationProxy.unwrap(unwrappedStore.getWorkspace()));
 
         fixNativeName(unwrappedResource);
 
         catalogDAO.save(unwrappedResource);
         added(resource);
     }
 
     void validate(ResourceInfo resource, boolean isNew) {
         if (isNull(resource.getName())) {
             throw new NullPointerException("Resource name must not be null");
         }
         if (resource.getStore() == null) {
             throw new IllegalArgumentException("Resource must be part of a store");
         }
         if (resource.getNamespace() == null) {
             throw new IllegalArgumentException("Resource must be part of a namespace");
         }
 
         StoreInfo store = resource.getStore();
         ResourceInfo existing = getResourceByStore(store, resource.getName(), ResourceInfo.class);
         if (existing != null && !existing.getId().equals(resource.getId())) {
             String msg = "Resource named '" + resource.getName() + "' already exists in store: '"
                     + store.getName() + "'";
             throw new IllegalArgumentException(msg);
         }
 
         NamespaceInfo namespace = resource.getNamespace();
         existing = getResourceByName(namespace, resource.getName(), ResourceInfo.class);
         if (existing != null && !existing.getId().equals(resource.getId())) {
             String msg = "Resource named '" + resource.getName()
                     + "' already exists in namespace: '" + namespace.getPrefix() + "'";
             throw new IllegalArgumentException(msg);
         }
 
     }
 
     public void remove(ResourceInfo resource) {
         // ensure no references to the resource
         if (!getLayers(resource).isEmpty()) {
             throw new IllegalArgumentException("Unable to delete resource referenced by layer");
         }
         resource = unwrap(resource);
         catalogDAO.delete(resource);
         removed(resource);
     }
 
     public void save(ResourceInfo resource) {
         validate(resource, false);
         saved(resource);
     }
 
     public <T extends ResourceInfo> T getResource(String id, Class<T> clazz) {
         ResourceInfo resource = catalogDAO.getResource(id, clazz);
         if (resource == null)
             return null;
         else {
             resolve(resource);
             return createProxy((T) resource, clazz);
         }
         // ModificationProxy.create((T) resource, clazz );
 
         // List l = lookup(clazz, resources);
         // for (Iterator i = l.iterator(); i.hasNext();) {
         // ResourceInfo resource = (ResourceInfo) i.next();
         // if (id.equals(resource.getId())) {
         // return ModificationProxy.create((T) resource, clazz );
         // }
         // }
         //
         // return null;
     }
 
     public <T extends ResourceInfo> T getResourceByName(String ns, String name, Class<T> clazz) {
 
         NamespaceInfo namespace = null;
         if ("".equals(ns)) {
             ns = null;
         }
         if (ns == null) {
             // if namespace was null, try the default namespace
             if (getDefaultNamespace() != null) {
                 namespace = getDefaultNamespace();
             }
         } else {
             namespace = getNamespaceByPrefix(ns);
             if (namespace == null) {
                 namespace = getNamespaceByURI(ns);
             }
         }
 
         if (namespace != null) {
             ResourceInfo resource = catalogDAO.getResourceByName(namespace.getName(), name, clazz);
            if (resource != null)
                 resolve(resource);
             // return ModificationProxy.create( (T) resource, clazz );
            return createProxy((T) resource, (Class<T>) mapResourceClass(resource));

         }
 
         // List l = lookup(clazz, resources);
         // if ( namespace != null ) {
         // for (Iterator i = l.iterator(); i.hasNext();) {
         // ResourceInfo resource = (ResourceInfo) i.next();
         // if (name.equals(resource.getName())) {
         // NamespaceInfo namespace1 = resource.getNamespace();
         // if (namespace1 != null && namespace1.equals( namespace )) {
         // return ModificationProxy.create( (T) resource, clazz );
         // }
         // }
         // }
         // }
 
         if (ns == null) {
             // no namespace was specified, so do an exhaustive lookup
             List<ResourceInfo> matches = (List<ResourceInfo>) catalogDAO.getResourcesByName(name,
                     clazz);
             // List matches = new ArrayList();
             // for (Iterator i = l.iterator(); i.hasNext();) {
             // ResourceInfo resource = (ResourceInfo) i.next();
             // if (name.equals(resource.getName())) {
             // matches.add( resource );
             // }
             // }
 
             if (matches.size() == 1) {
                 ResourceInfo ret = matches.get(0);
                 resolve(ret);
                 return createProxy((T) ret, (Class<T>) mapResourceClass(ret));
                 // return ModificationProxy.create( (T) matches.get( 0 ), clazz );
             }
         }
         return null;
     }
 
     public <T extends ResourceInfo> T getResourceByName(NamespaceInfo ns, String name,
             Class<T> clazz) {
         return getResourceByName(ns != null ? ns.getPrefix() : null, name, clazz);
     }
 
     public <T extends ResourceInfo> T getResourceByName(Name name, Class<T> clazz) {
         return getResourceByName(name.getNamespaceURI(), name.getLocalPart(), clazz);
     }
 
     public <T extends ResourceInfo> T getResourceByName(String name, Class<T> clazz) {
         ResourceInfo resource;
 
         // check is the name is a fully qualified one
         int colon = name.indexOf(':');
         if (colon != -1) {
             String ns = name.substring(0, colon);
             String localName = name.substring(colon + 1);
             return getResourceByName(ns, localName, clazz);
         } else {
             return getResourceByName((String) null, name, clazz);
         }
     }
 
     public List getResources(Class clazz) {
         // return ModificationProxy.createList( lookup(clazz,resources), clazz );
         // return ModificationProxy.createList( catalogDAO.getResources(clazz), clazz );
         List<ResourceInfo> list = catalogDAO.getResources(clazz);
         for (ResourceInfo resourceInfo : list) {
             resolve(resourceInfo);
         }
 
         return createProxyList(list, clazz);
     }
 
     public List getResourcesByNamespace(NamespaceInfo namespace, Class clazz) {
         // List all = lookup(clazz, resources);
         // List matches = new ArrayList();
 
         if (namespace == null) {
             namespace = getDefaultNamespace();
         }
 
         // for (Iterator r = all.iterator(); r.hasNext();) {
         // ResourceInfo resource = (ResourceInfo) r.next();
         // if (namespace != null ) {
         // if (namespace.equals(resource.getNamespace())) {
         // matches.add( resource );
         // }
         // }
         // else if ( resource.getNamespace() == null ) {
         // matches.add(resource);
         // }
         // }
 
         List<ResourceInfo> matches = catalogDAO.getResourcesByNamespace(namespace, clazz);
         for (ResourceInfo resource : matches) {
             resolve(resource);
         }
         Class proxyclass = matches.isEmpty() ? clazz : mapResourceClass((ResourceInfo) matches
                 .get(0));
         // return ModificationProxy.createList( matches, clazz );
         return createProxyList(matches, proxyclass);
     }
 
     public <T extends ResourceInfo> List<T> getResourcesByNamespace(String namespace, Class<T> clazz) {
         if (namespace == null) {
             return getResourcesByNamespace((NamespaceInfo) null, clazz);
         }
 
         NamespaceInfo ns = getNamespaceByPrefix(namespace);
         if (ns == null) {
             ns = getNamespaceByURI(namespace);
         }
         if (ns == null) {
             return Collections.EMPTY_LIST;
         }
 
         return getResourcesByNamespace(ns, clazz);
     }
 
     public <T extends ResourceInfo> T getResourceByStore(StoreInfo store, String name,
             Class<T> clazz) {
 
         store = ModificationProxy.unwrap(store);
         ResourceInfo resource = catalogDAO.getResourceByStore(store, name, clazz);
         if (resource == null)
             return null;
         else {
             resolve(resource);
             return createProxy((T) resource, (Class<T>) mapResourceClass(resource));
         }
 
         // List all = lookup(clazz,resources);
         // for (Iterator r = all.iterator(); r.hasNext(); ) {
         // ResourceInfo resource = (ResourceInfo) r.next();
         // if ( name.equals( resource.getName() ) && store.equals( resource.getStore() ) ) {
         // return ModificationProxy.create((T)resource, clazz);
         // }
         //
         // }
         //
         // return null;
     }
 
     public <T extends ResourceInfo> List<T> getResourcesByStore(StoreInfo store, Class<T> clazz) {
         store = ModificationProxy.unwrap(store);
         // refine clazz type searching
         Class typedClazz = clazz;
         if (clazz.equals(ResourceInfo.class)) {
             if (store instanceof DataStoreInfo) {
                 typedClazz = FeatureTypeInfo.class;
             } else if (store instanceof CoverageStoreInfo) {
                 typedClazz = CoverageInfo.class;
             } else
                 throw new IllegalArgumentException("Unknown store type "
                         + store.getClass().getName());
         }
         List<T> resources = catalogDAO.getResourcesByStore(store, (Class<T>) typedClazz);
         for (ResourceInfo resource : resources) {
             resolve(resource);
         }
         Class proxyclazz = resources.isEmpty() ? typedClazz : mapResourceClass(resources.get(0));
         // return ModificationProxy.createList( resources, proxyclazz );
         return createProxyList(resources, proxyclazz);
 
         // List all = lookup(clazz,resources);
         // List matches = new ArrayList();
         //
         // for (Iterator r = all.iterator(); r.hasNext();) {
         // ResourceInfo resource = (ResourceInfo) r.next();
         // if (store.equals(resource.getStore())) {
         // matches.add(resource);
         // }
         // }
         //
         // return ModificationProxy.createList( matches, clazz );
     }
 
     public FeatureTypeInfo getFeatureType(String id) {
         return (FeatureTypeInfo) getResource(id, FeatureTypeInfo.class);
     }
 
     public FeatureTypeInfo getFeatureTypeByName(String ns, String name) {
         return (FeatureTypeInfo) getResourceByName(ns, name, FeatureTypeInfo.class);
     }
 
     public FeatureTypeInfo getFeatureTypeByName(NamespaceInfo ns, String name) {
         return getResourceByName(ns, name, FeatureTypeInfo.class);
     }
 
     public FeatureTypeInfo getFeatureTypeByName(Name name) {
         return getResourceByName(name, FeatureTypeInfo.class);
     }
 
     public FeatureTypeInfo getFeatureTypeByName(String name) {
         return (FeatureTypeInfo) getResourceByName(name, FeatureTypeInfo.class);
     }
 
     public List<FeatureTypeInfo> getFeatureTypes() {
         return getResources(FeatureTypeInfo.class);
     }
 
     public List getFeatureTypesByNamespace(NamespaceInfo namespace) {
         return getResourcesByNamespace(namespace, FeatureTypeInfo.class);
     }
 
     public FeatureTypeInfo getFeatureTypeByStore(DataStoreInfo dataStore, String name) {
         return getFeatureTypeByDataStore(dataStore, name);
     }
 
     public FeatureTypeInfo getFeatureTypeByDataStore(DataStoreInfo dataStore, String name) {
         return getResourceByStore(dataStore, name, FeatureTypeInfo.class);
     }
 
     public List<FeatureTypeInfo> getFeatureTypesByStore(DataStoreInfo store) {
         return getFeatureTypesByDataStore(store);
     }
 
     public List<FeatureTypeInfo> getFeatureTypesByDataStore(DataStoreInfo store) {
         return getResourcesByStore(store, FeatureTypeInfo.class);
     }
 
     public CoverageInfo getCoverage(String id) {
         return (CoverageInfo) getResource(id, CoverageInfo.class);
     }
 
     public CoverageInfo getCoverageByName(String ns, String name) {
         return (CoverageInfo) getResourceByName(ns, name, CoverageInfo.class);
     }
 
     public CoverageInfo getCoverageByName(NamespaceInfo ns, String name) {
         return (CoverageInfo) getResourceByName(ns, name, CoverageInfo.class);
     }
 
     public CoverageInfo getCoverageByName(Name name) {
         return getResourceByName(name, CoverageInfo.class);
     }
 
     public CoverageInfo getCoverageByName(String name) {
         return (CoverageInfo) getResourceByName(name, CoverageInfo.class);
     }
 
     public List<CoverageInfo> getCoverages() {
         return getResources(CoverageInfo.class);
     }
 
     public List getCoveragesByNamespace(NamespaceInfo namespace) {
         return getResourcesByNamespace(namespace, CoverageInfo.class);
     }
 
     public List<CoverageInfo> getCoveragesByStore(CoverageStoreInfo store) {
         return getResourcesByStore(store, CoverageInfo.class);
     }
 
     public CoverageInfo getCoverageByCoverageStore(CoverageStoreInfo coverageStore, String name) {
         return getResourceByStore(coverageStore, name, CoverageInfo.class);
     }
 
     public List<CoverageInfo> getCoveragesByCoverageStore(CoverageStoreInfo store) {
         return getResourcesByStore(store, CoverageInfo.class);
     }
 
     // Layer methods
     public void add(LayerInfo layer) {
         validate(layer, true);
         resolve(layer);
 
         if (layer.getType() == null) {
             if (layer.getResource() instanceof FeatureTypeInfo) {
                 layer.setType(LayerInfo.Type.VECTOR);
             } else if (layer.getResource() instanceof CoverageInfo) {
                 layer.setType(LayerInfo.Type.RASTER);
             } else {
                 String msg = "Layer type not set and can't be derived from resource";
                 throw new IllegalArgumentException(msg);
             }
         }
 
         // layers.add(layer);
         LayerInfo unwrapped = unwrap(layer);
         ResourceInfo resource = unwrapped.getResource();
         if (resource != null) {
             resource = unwrap(resource);
             unwrapped.setResource(resource);
         }
 
         LOGGER.warning("SAVING LAYER id:" + unwrapped.getId() + " name:" + unwrapped.getName());
         LOGGER.warning("  layer.resource " + unwrapped.getResource().getClass().getSimpleName()
                 + "[" + "id:" + unwrapped.getResource().getId() + " name:"
                 + unwrapped.getResource().getName() + "]");
 
         if (unwrapped.getDefaultStyle() != null)
             LOGGER.warning("  layer.style " + "[" + "id:" + unwrapped.getDefaultStyle().getId()
                     + " name:" + unwrapped.getDefaultStyle().getName() + "]");
 
         fixNativeName(resource);
 
         catalogDAO.save(unwrapped);
         added(layer);
     }
 
     /**
      * <B>FIXME This is a workaround to fill in nativeName in coverages.</B> Please investigate why
      * its value is not assigned.
      * 
      * @deprecated this is a workaround
      */
     private void fixNativeName(ResourceInfo resource) {
         if (resource != null && resource.getNativeName() == null) {
             if (resource instanceof CoverageInfo) {
                 LOGGER.warning("FIXME Coverage is missing nativeName (" + "id:" + resource.getId()
                         + " name:" + resource.getName() + ")");
                 String storeurl = ((CoverageInfo) resource).getStore().getURL();
                 String name = FilenameUtils.getBaseName(storeurl);
                 resource.setNativeName(name);
                 LOGGER.warning("FIXME setting coverage nativeName to " + name);
             }
         }
     }
 
     void validate(LayerInfo layer, boolean isNew) {
         if (isNull(layer.getName())) {
             throw new NullPointerException("Layer name must not be null");
         }
 
         LayerInfo existing = getLayerByName(layer.getName());
         if (existing != null && !existing.getId().equals(layer.getId())) {
             // JD: since layers are not qualified by anything (yet), check
             // namespace of the resource, if they are different then allow the
             // layer to be added
             if (existing.getResource().getNamespace().equals(layer.getName())) {
                 throw new IllegalArgumentException("Layer named '" + layer.getName()
                         + "' already exists.");
             }
         }
 
         if (layer.getResource() == null) {
             throw new NullPointerException("Layer resource must not be null");
         }
         // (JD): not sure if default style should be mandatory
         // if ( layer.getDefaultStyle() == null ){
         // throw new NullPointerException( "Layer default style must not be null" );
         // }
     }
 
     public void remove(LayerInfo layer) {
         // ensure no references to the layer
         for (LayerGroupInfo lg : catalogDAO.getLayerGroups()) {
             if (lg.getLayers().contains(layer)) {
                 String msg = "Unable to delete layer referenced by layer group '" + lg.getName()
                         + "'";
                 throw new IllegalArgumentException(msg);
             }
         }
         // layers.remove(unwrap(layer));
         catalogDAO.delete(layer);
         removed(layer);
     }
 
     public void save(LayerInfo layer) {
         validate(layer, false);
         saved(layer);
     }
 
     public LayerInfo getLayer(String id) {
         LayerInfo layer = catalogDAO.getLayer(id);
         if (layer == null)
             return null;
         else {
             resolve(layer);
             return createProxy(layer, LayerInfo.class);
         }
 
     }
 
     public LayerInfo getLayerByName(Name name) {
         if (name.getNamespaceURI() != null) {
             NamespaceInfo ns = getNamespaceByURI(name.getNamespaceURI());
             if (ns != null) {
                 return getLayerByName(ns.getPrefix() + ":" + name.getLocalPart());
             }
         }
 
         return getLayerByName(name.getLocalPart());
     }
 
     public LayerInfo getLayerByName(String name) {
         String prefix = null;
         String resource = null;
 
         LayerInfo layer;
 
         int colon = name.indexOf(':');
         if (colon != -1) {
             // search by resource name
             prefix = name.substring(0, colon);
             resource = name.substring(colon + 1);
 
             // todo: check original code: it compared vs the resource name
 
             layer = catalogDAO.getLayerByName(prefix, resource);
 
         } else {
             // search by layer name
             layer = catalogDAO.getLayerByName(name);
 
         }
 
         if (layer != null) {
             resolve(layer);
             return createProxy(layer, LayerInfo.class);
         } else
             return null;
     }
 
     public List<LayerInfo> getLayers(ResourceInfo resource) {
 
         List<LayerInfo> layers = catalogDAO.getLayersByResourceId(resource.getId());
         for (LayerInfo layer : layers) {
             resolve(layer);
         }
         return createProxyList(layers, LayerInfo.class);
 
     }
 
     public List<LayerInfo> getLayers(StyleInfo style) {
         List<LayerInfo> matches = new ArrayList<LayerInfo>();
         for (LayerInfo layer : catalogDAO.getLayers()) {
             if (style.equals(layer.getDefaultStyle()) || layer.getStyles().contains(style)) {
                 resolve(layer);
                 matches.add(layer);
             }
         }
 
         return createProxyList(matches, LayerInfo.class);
     }
 
     public List<LayerInfo> getLayers() {
         List<LayerInfo> layers = catalogDAO.getLayers();
         for (LayerInfo layerInfo : layers) {
             resolve(layerInfo);
         }
         return createProxyList(layers, LayerInfo.class);
     }
 
     // Map methods
     public MapInfo getMap(String id) {
         MapInfo map = catalogDAO.getMap(id);
         if (map != null) {
             resolve(map);
             return createProxy(map, MapInfo.class);
         }
         return null;
 
     }
 
     public MapInfo getMapByName(String name) {
         MapInfo map = catalogDAO.getMapByName(name);
         if (map != null) {
             resolve(map);
             return createProxy(map, MapInfo.class);
         }
         return null;
 
     }
 
     public List<MapInfo> getMaps() {
         List<MapInfo> maps = catalogDAO.getMaps();
         for (MapInfo map : maps) {
             resolve(map);
         }
 
         return createProxyList(maps, MapInfo.class);
     }
 
     public void add(LayerGroupInfo layerGroup) {
         validate(layerGroup, true);
         resolve(layerGroup);
 
         if (layerGroup.getStyles().isEmpty()) {
             for (LayerInfo l : layerGroup.getLayers()) {
                 // default style
                 layerGroup.getStyles().add(null);
             }
         }
 
         layerGroup = ModificationProxy.unwrap(layerGroup);
         catalogDAO.save(layerGroup);
         added(layerGroup);
     }
 
     void validate(LayerGroupInfo layerGroup, boolean isNew) {
         if (isNull(layerGroup.getName())) {
             throw new NullPointerException("Layer group name must not be null");
         }
 
         LayerGroupInfo existing = getLayerGroupByName(layerGroup.getName());
         if (existing != null && !existing.getId().equals(layerGroup.getId())) {
             throw new IllegalArgumentException("Layer group named '" + layerGroup.getName()
                     + "' already exists.");
         }
 
         if (!isNew) {
             if (layerGroup.getLayers() == null || layerGroup.getLayers().isEmpty()) {
                 throw new NullPointerException("Layer group must not be empty");
             }
         }
 
         if (layerGroup.getStyles() != null && !layerGroup.getStyles().isEmpty()
                 && !(layerGroup.getStyles().size() == layerGroup.getLayers().size())) {
             throw new IllegalArgumentException(
                     "Layer group has different number of styles than layers");
         }
     }
 
     public void remove(LayerGroupInfo layerGroup) {
         catalogDAO.delete(unwrap(layerGroup));
         removed(layerGroup);
     }
 
     public void save(LayerGroupInfo layerGroup) {
         validate(layerGroup, false);
         saved(layerGroup);
     }
 
     public List<LayerGroupInfo> getLayerGroups() {
         final List<LayerGroupInfo> layerGroups = catalogDAO.getLayerGroups();
         for (LayerGroupInfo layerGroupInfo : layerGroups) {
             resolve(layerGroupInfo);
         }
         return createProxyList(layerGroups, LayerGroupInfo.class);
     }
 
     public LayerGroupInfo getLayerGroup(String id) {
         final LayerGroupInfo layerGroup = catalogDAO.getLayerGroup(id);
         if (layerGroup != null) {
             resolve(layerGroup);
             return createProxy(layerGroup, LayerGroupInfo.class);
         } else
             return null;
 
     }
 
     public LayerGroupInfo getLayerGroupByName(String name) {
         LayerGroupInfo layerGroup = catalogDAO.getLayerGroupByName(name);
         if (layerGroup != null) {
             resolve(layerGroup);
             return createProxy(layerGroup, LayerGroupInfo.class);
         } else
             return null;
     }
 
     public void add(MapInfo map) {
         resolve(map);
         catalogDAO.save(ModificationProxy.unwrap(map));
         added(map);
     }
 
     public void remove(MapInfo map) {
         catalogDAO.delete(unwrap(map));
         removed(map);
     }
 
     public void save(MapInfo map) {
         saved(map);
     }
 
     // Namespace methods
     public NamespaceInfo getNamespace(String id) {
         final NamespaceInfo namespace = catalogDAO.getNamespace(id);
         if (namespace != null) {
             resolve(namespace);
             return createProxy(namespace, NamespaceInfo.class);
         } else
             return null;
 
     }
 
     public NamespaceInfo getNamespaceByPrefix(String prefix) {
         NamespaceInfo namespace = catalogDAO.getNamespaceByPrefix(prefix);
         if (namespace != null) {
             resolve(namespace);
             return createProxy(namespace, NamespaceInfo.class);
         } else
             return null;
     }
 
     public NamespaceInfo getNamespaceByURI(String uri) {
         NamespaceInfo namespace = catalogDAO.getNamespaceByURI(uri);
         if (namespace != null) {
             resolve(namespace);
             return createProxy(namespace, NamespaceInfo.class);
         } else
             return null;
 
     }
 
     public List<NamespaceInfo> getNamespaces() {
         final List<NamespaceInfo> namespaces = catalogDAO.getNamespaces();
         for (NamespaceInfo namespace : namespaces) {
             resolve(namespace);
         }
         return createProxyList(namespaces, NamespaceInfo.class);
     }
 
     public void add(NamespaceInfo namespace) {
         validate(namespace, true);
         resolve(namespace);
 
         boolean existsDefault = catalogDAO.getDefaultNamespace() != null;
         ((NamespaceInfoImplHb) namespace).setDefault(!existsDefault);
 
         catalogDAO.save(namespace);
 
         added(namespace);
     }
 
     void validate(NamespaceInfo namespace, boolean isNew) {
         if (isNull(namespace.getPrefix())) {
             throw new NullPointerException("Namespace prefix must not be null");
         }
 
         NamespaceInfo existing = catalogDAO.getNamespaceByPrefix(namespace.getPrefix());
         if (existing != null && !existing.getId().equals(namespace.getId())) {
             throw new IllegalArgumentException("Namespace with prefix '" + namespace.getPrefix()
                     + "' already exists.");
         }
 
         if (namespace.getURI() == null) {
             throw new NullPointerException("Namespace uri must not be null");
         }
 
         if (isNull(namespace.getURI())) {
             throw new NullPointerException("Namespace uri must not be null");
         }
 
         try {
             new URI(namespace.getURI());
         } catch (Exception e) {
             throw new IllegalArgumentException("Invalid URI syntax for '" + namespace.getURI()
                     + "' in namespace '" + namespace.getPrefix() + "'");
         }
     }
 
     public void remove(NamespaceInfo namespace) {
         if (!catalogDAO.getResourcesByNamespace(namespace, ResourceInfo.class).isEmpty()) {
             throw new IllegalArgumentException("Unable to delete non-empty namespace.");
         }
         NamespaceInfoImplHb realns = catalogDAO.getNamespaceByPrefix(namespace.getPrefix());
         if (realns == null) {
             throw new IllegalArgumentException("Can't find namespace '" + namespace.getPrefix()
                     + "' for deletion.");
         }
 
         catalogDAO.delete(realns);
 
         if (realns.isDefault()) {
             LOGGER.warning("Removing default namespace '" + namespace.getName() + "'");
             // elect a random ns as default
             List<NamespaceInfo> nslist = catalogDAO.getNamespaces();
             if (!nslist.isEmpty()) {
                 NamespaceInfoImplHb ns0 = (NamespaceInfoImplHb) nslist.get(0);
                 LOGGER.warning("Electing '" + namespace.getName() + "' to default namespace.");
                 ns0.setDefault(true);
                 catalogDAO.update(ns0);
             }
         }
         removed(namespace);
     }
 
     public void save(NamespaceInfo namespace) {
         validate(namespace, false);
 
         saved(namespace);
     }
 
     public NamespaceInfo getDefaultNamespace() {
         final NamespaceInfoImplHb defaultNamespace = catalogDAO.getDefaultNamespace();
         resolve(defaultNamespace);
         return createProxy(defaultNamespace, NamespaceInfo.class);
 
     }
 
     public void setDefaultNamespace(NamespaceInfo defaultNamespace) {
         NamespaceInfo nsold = catalogDAO.getDefaultNamespace();
         NamespaceInfo nsnew = catalogDAO.getNamespaceByPrefix(defaultNamespace.getPrefix());
         if (nsnew == null) {
             throw new IllegalArgumentException("No such namespace: '"
                     + defaultNamespace.getPrefix() + "'");
         }
 
         if (nsold != null && nsold.getPrefix().equals(nsnew.getPrefix())) // setting existing
                                                                           // default
             return;
 
         ((NamespaceInfoImplHb) nsnew).setDefault(true);
         catalogDAO.update(nsnew);
         if (nsold != null) {
             ((NamespaceInfoImplHb) nsold).setDefault(false);
             catalogDAO.update(nsold);
         }
 
     }
 
     // Workspace methods
     public void add(WorkspaceInfo workspace) {
         validate(workspace, true);
         if (catalogDAO.getWorkspaceByName(workspace.getName()) != null) {
             throw new IllegalArgumentException("Workspace with name '" + workspace.getName()
                     + "' already exists.");
         }
 
         resolve(workspace);
 
         boolean existsDefault = catalogDAO.getDefaultWorkspace() != null;
         ((WorkspaceInfoImplHb) workspace).setDefault(!existsDefault);
 
         catalogDAO.save(workspace);
 
         added(workspace);
     }
 
     void validate(WorkspaceInfo workspace, boolean isNew) {
         if (isNull(workspace.getName())) {
             throw new NullPointerException("workspace name must not be null");
         }
 
         WorkspaceInfo existing = catalogDAO.getWorkspaceByName(workspace.getName());
         if (existing != null && !existing.getId().equals(workspace.getId())) {
             throw new IllegalArgumentException("Workspace named '" + workspace.getName()
                     + "' already exists.");
         }
 
     }
 
     public void remove(WorkspaceInfo workspace) {
         // JD: maintain the link between namespace and workspace, remove this when this is no
         // longer necessary
         if (catalogDAO.getNamespaceByPrefix(workspace.getName()) != null) {
             throw new IllegalArgumentException("Cannot delete workspace with linked namespace");
         }
         if (!getStoresByWorkspace(workspace, StoreInfo.class).isEmpty()) {
             throw new IllegalArgumentException("Cannot delete non-empty workspace.");
         }
         catalogDAO.delete(workspace);
         removed(workspace);
     }
 
     public void save(WorkspaceInfo workspace) {
         validate(workspace, false);
 
         saved(workspace);
     }
 
     public WorkspaceInfo getDefaultWorkspace() {
         WorkspaceInfo workspace = catalogDAO.getDefaultWorkspace();
         if (workspace != null) {
             resolve(workspace);
             return createProxy(workspace, WorkspaceInfo.class);
         } else
             return null;
 
     }
 
     public void setDefaultWorkspace(WorkspaceInfo workspace) {
 
         WorkspaceInfo wsold = catalogDAO.getDefaultWorkspace();
         WorkspaceInfo wsnew = catalogDAO.getWorkspaceByName(workspace.getName());
         if (wsnew == null) {
             throw new IllegalArgumentException("No such workspace: '" + workspace.getName() + "'");
         }
 
         if (wsold != null && wsold.getName().equals(wsnew.getName())) // setting existing default
             return;
 
         ((WorkspaceInfoImplHb) wsnew).setDefault(true);
         catalogDAO.update(wsnew);
         if (wsold != null) {
             ((WorkspaceInfoImplHb) wsold).setDefault(false);
             catalogDAO.update(wsold);
         }
 
     }
 
     public List<WorkspaceInfo> getWorkspaces() {
         final List<WorkspaceInfo> workspaces = catalogDAO.getWorkspaces();
         for (WorkspaceInfo workspace : workspaces) {
             resolve(workspace);
         }
         return createProxyList(workspaces, WorkspaceInfo.class);
 
     }
 
     public WorkspaceInfo getWorkspace(String id) {
         WorkspaceInfo workspace = catalogDAO.getWorkspace(id);
         if (workspace != null) {
             resolve(workspace);
             return createProxy(workspace, WorkspaceInfo.class);
         } else
             return null;
 
     }
 
     public WorkspaceInfo getWorkspaceByName(String name) {
         WorkspaceInfo workspace = catalogDAO.getWorkspaceByName(name);
         if (workspace != null) {
             resolve(workspace);
             return createProxy(workspace, WorkspaceInfo.class);
         } else
             return null;
 
     }
 
     // Style methods
     public StyleInfo getStyle(String id) {
         StyleInfo style = catalogDAO.getStyle(id);
         if (style != null) {
             resolve(style);
             return createProxy(style, StyleInfo.class);
         } else
             return null;
 
     }
 
     public StyleInfo getStyleByName(String name) {
         StyleInfo style = catalogDAO.getStyleByName(name);
         if (style != null) {
             resolve(style);
             return createProxy(style, StyleInfo.class);
         } else
             return null;
 
     }
 
     public List<StyleInfo> getStyles() {
         final List<StyleInfo> styles = catalogDAO.getStyles();
         for (StyleInfo style : styles) {
             resolve(style);
         }
 
         return createProxyList(styles, StyleInfo.class);
     }
 
     public void add(StyleInfo style) {
         validate(style, true);
         resolve(style);
         catalogDAO.save(style);
         added(style);
     }
 
     void validate(StyleInfo style, boolean isNew) {
         if (isNull(style.getName())) {
             throw new NullPointerException("Style name must not be null");
         }
         if (isNull(style.getFilename())) {
             throw new NullPointerException("Style fileName must not be null");
         }
 
         StyleInfo existing = getStyleByName(style.getName());
         if (existing != null && !existing.getId().equals(style.getId())) {
             throw new IllegalArgumentException("Style named '" + style.getName()
                     + "' already exists.");
         }
     }
 
     public void remove(StyleInfo style) {
         // ensure no references to the style
         for (LayerInfo l : catalogDAO.getLayers()) {
             if (style.equals(l.getDefaultStyle()) || l.getStyles().contains(style)) {
                 throw new IllegalArgumentException("Unable to delete style referenced by '"
                         + l.getName() + "'");
             }
         }
         StyleInfo realObj = catalogDAO.getStyle(style.getId());
         catalogDAO.delete(realObj);
         removed(style);
     }
 
     public void save(StyleInfo style) {
         validate(style, false);
         saved(style);
     }
 
     // Event methods
     public Collection getListeners() {
         return Collections.unmodifiableCollection(listeners);
     }
 
     public void addListener(CatalogListener listener) {
         listeners.add(listener);
 
     }
 
     public void removeListener(CatalogListener listener) {
         listeners.remove(listener);
     }
 
     public ResourcePool getResourcePool() {
         return resourcePool;
     }
 
     public void setResourcePool(ResourcePool resourcePool) {
         this.resourcePool = resourcePool;
     }
 
     public GeoServerResourceLoader getResourceLoader() {
         return resourceLoader;
     }
 
     public void setResourceLoader(GeoServerResourceLoader resourceLoader) {
         this.resourceLoader = resourceLoader;
     }
 
     public void dispose() {
         if (listeners != null)
             listeners.clear();
 
         if (resourcePool != null)
             resourcePool.dispose();
     }
 
     protected void added(CatalogInfo object) {
         fireAdded(object);
     }
 
     protected void fireAdded(CatalogInfo object) {
         CatalogAddEventImpl event = new CatalogAddEventImpl();
         event.setSource(object);
 
         event(event);
     }
 
     @SuppressWarnings("unchecked")
     protected void saved(CatalogInfo object) {
         // this object is a proxy
         if (!Proxy.isProxyClass(object.getClass())) {
             LOGGER.severe("WORKAROUND: CatalogInfo object is a "
                     + object.getClass().getSimpleName() + " -- Faking delta values");
 
             // fire out what changed
             List propertyNames = new ArrayList();
             List newValues = new ArrayList();
             List oldValues = new ArrayList();
 
             // TODO: protect this original object, perhaps with another proxy
             fireModified(object, propertyNames, oldValues, newValues);
             firePostModified(object);
             return;
         }
 
         ModificationProxy h = (ModificationProxy) Proxy.getInvocationHandler(object);
 
         // get the real object
         CatalogInfo real = (CatalogInfo) h.getProxyObject();
 
         // fire out what changed
         List propertyNames = h.getPropertyNames();
         List newValues = h.getNewValues();
         List oldValues = h.getOldValues();
 
         // TODO: protect this original object, perhaps with another proxy
         fireModified(real, propertyNames, oldValues, newValues);
 
         // commit to the original object
         h.commit();
 
         // resolve to do a sync on the object
         // syncIdWithName(real);
 
         // fire the post modify event
         firePostModified(real);
     }
 
     protected void fireModified(CatalogInfo object, List propertyNames, List oldValues,
             List newValues) {
         CatalogModifyEventImpl event = new CatalogModifyEventImpl();
 
         event.setSource(object);
         event.setPropertyNames(propertyNames);
         event.setOldValues(oldValues);
         event.setNewValues(newValues);
 
         event(event);
     }
 
     protected void firePostModified(CatalogInfo object) {
         CatalogPostModifyEventImpl event = new CatalogPostModifyEventImpl();
         event.setSource(object);
 
         event(event);
     }
 
     protected void removed(CatalogInfo object) {
         CatalogRemoveEventImpl event = new CatalogRemoveEventImpl();
         event.setSource(object);
 
         event(event);
     }
 
     protected void event(CatalogEvent event) {
         for (CatalogListener listener : listeners) {
             try {
                 if (event instanceof CatalogAddEvent) {
                     listener.handleAddEvent((CatalogAddEvent) event);
                 } else if (event instanceof CatalogRemoveEvent) {
                     listener.handleRemoveEvent((CatalogRemoveEvent) event);
                 } else if (event instanceof CatalogModifyEvent) {
                     listener.handleModifyEvent((CatalogModifyEvent) event);
                 } else if (event instanceof CatalogPostModifyEvent) {
                     listener.handlePostModifyEvent((CatalogPostModifyEvent) event);
                 }
             } catch (Exception e) {
                 LOGGER.log(Level.WARNING, "Catalog listener threw exception handling event.", e);
             }
 
         }
     }
 
     /**
      * Implementation method for resolving all {@link ResolvingProxy} instances.
      */
     public void resolve() {
         if (listeners == null) {
             listeners = new ArrayList<CatalogListener>();
         }
 
         if (resourcePool == null) {
             resourcePool = new ResourcePool(this);
         }
     }
 
     protected void resolve(WorkspaceInfo workspace) {
         resolveCollections(workspace);
     }
 
     protected void resolve(NamespaceInfo namespace) {
         resolveCollections(namespace);
     }
 
     protected void resolve(StoreInfo store) {
         StoreInfoImpl s = (StoreInfoImpl) store;
 
         // resolve the workspace
         WorkspaceInfo resolved = ResolvingProxy.resolve(this, s.getWorkspace());
         if (resolved != null) {
             s.setWorkspace(resolved);
         } else {
             // this means the workspace has not yet been added to the catalog, keep the proxy around
         }
         resolveCollections(s);
 
         s.setCatalog(this);
     }
 
     protected void resolve(ResourceInfo resource) {
         ResourceInfoImpl r = (ResourceInfoImpl) resource;
 
         // resolve the store
         StoreInfo resolved = ResolvingProxy.resolve(this, r.getStore());
         if (resolved != null) {
             resolve(resolved);
             r.setStore(resolved);
         }
 
         if (resource instanceof FeatureTypeInfo) {
             resolve((FeatureTypeInfo) resource);
         }
         if (r instanceof CoverageInfo) {
             resolve((CoverageInfo) resource);
         }
 
         r.setCatalog(this);
     }
 
     private void resolve(CoverageInfo r) {
         CoverageInfoImpl c = (CoverageInfoImpl) r;
         if (c.getDimensions() == null) {
             c.setDimensions(new ArrayList<CoverageDimensionInfo>());
         } else {
             for (CoverageDimensionInfo dim : c.getDimensions()) {
                 if (dim.getNullValues() == null)
                     ((CoverageDimensionImpl) dim).setNullValues(new ArrayList<Double>());
             }
         }
         resolveCollections(r);
     }
 
     /**
      * We don't want the world to be able and call this without going trough
      * {@link #resolve(ResourceInfo)}
      * 
      * @param featureType
      */
     private void resolve(FeatureTypeInfo featureType) {
         FeatureTypeInfoImpl ft = (FeatureTypeInfoImpl) featureType;
         resolveCollections(ft);
     }
 
     protected void resolve(LayerInfo layer) {
         if (layer.getAttribution() == null) {
             layer.setAttribution(getFactory().createAttribution());
         }
         if (layer.getDefaultStyle() != null) {
             resolve(layer.getDefaultStyle());
         }
         resolve(layer.getResource());
         resolveCollections(layer);
     }
 
     protected void resolve(LayerGroupInfo layerGroup) {
         resolveCollections(layerGroup);
         LayerGroupInfoImpl lg = (LayerGroupInfoImpl) layerGroup;
 
         for (int i = 0; i < lg.getLayers().size(); i++) {
             LayerInfo l = lg.getLayers().get(i);
             LayerInfo resolved = ResolvingProxy.resolve(this, l);
             resolve(resolved);
             lg.getLayers().set(i, resolved);
         }
 
         for (int i = 0; i < lg.getStyles().size(); i++) {
             StyleInfo s = lg.getStyles().get(i);
             if (s != null) {
                 StyleInfo resolved = ResolvingProxy.resolve(this, s);
                 ((StyleInfoImplHb)resolved).setCatalog(this);
                 lg.getStyles().set(i, resolved);
             }
         }
 
     }
 
     protected void resolve(StyleInfo style) {
         ((StyleInfoImpl) style).setCatalog(this);
     }
 
     protected void resolve(MapInfo map) {
         for (LayerInfo layerInfo : map.getLayers()) {
             resolve(layerInfo);
         }
     }
 
     /**
      * Method which reflectively sets all collections when they are null.
      */
     protected void resolveCollections(Object object) {
         ClassProperties properties = OwsUtils.getClassProperties(object.getClass());
         for (String property : properties.properties()) {
             Method g = properties.getter(property, null);
             if (g == null) {
                 continue;
             }
 
             Class type = g.getReturnType();
             // only continue if this is a collection or a map
             if (!(Map.class.isAssignableFrom(type) || Collection.class.isAssignableFrom(type))) {
                 continue;
             }
 
             // only continue if there is also a setter as well
             Method s = properties.setter(property, null);
             if (s == null) {
                 continue;
             }
 
             // if the getter returns null, call the setter
             try {
                 Object value = g.invoke(object, null);
                 if (value == null) {
                     if (Map.class.isAssignableFrom(type)) {
                         if (MetadataMap.class.isAssignableFrom(type)) {
                             value = new MetadataMap();
                         } else {
                             value = new HashMap();
                         }
                     } else if (List.class.isAssignableFrom(type)) {
                         value = new ArrayList();
                     } else if (Set.class.isAssignableFrom(type)) {
                         value = new HashSet();
                     } else {
                         throw new RuntimeException("Unknown collection type:" + type.getName());
                     }
 
                     // initialize
                     s.invoke(object, value);
                 }
             } catch (Exception e) {
                 throw new RuntimeException(e);
             }
         }
     }
 
     protected void setId(Object o) {
         if (OwsUtils.get(o, "id") == null) {
             String uid = new UID().toString();
             OwsUtils.set(o, "id", o.getClass().getSimpleName() + "-" + uid);
         }
     }
 
     protected boolean isNull(String string) {
         return string == null || "".equals(string.trim());
     }
 
     public static <T> T unwrap(T obj) {
         return ModificationProxy.unwrap(obj);
     }
 
     public void setCatalogDAO(CatalogDAO catalogDAO) {
         this.catalogDAO = catalogDAO;
     }
 
     class HibCatalogUpdater implements CatalogListener {
 
         public void handleAddEvent(CatalogAddEvent event) {
         }
 
         public void handleRemoveEvent(CatalogRemoveEvent event) {
         }
 
         public void handleModifyEvent(CatalogModifyEvent event) {
         }
 
         public void handlePostModifyEvent(CatalogPostModifyEvent event) {
             CatalogInfo src = event.getSource();
             src.accept(catalogVisitor);
         }
 
         public void reloaded() {
         }
 
         private CatalogVisitor catalogVisitor = new CatalogVisitor() {
 
             public void visit(WorkspaceInfo obj) {
                 obj = ModificationProxy.unwrap(obj);
                 catalogDAO.update(obj);
             }
 
             public void visit(NamespaceInfo obj) {
                 obj = ModificationProxy.unwrap(obj);
                 catalogDAO.update(obj);
             }
 
             public void visit(DataStoreInfo obj) {
                 obj = ModificationProxy.unwrap(obj);
                 catalogDAO.update(obj);
             }
 
             public void visit(CoverageStoreInfo obj) {
                 obj = ModificationProxy.unwrap(obj);
                 catalogDAO.update(obj);
             }
 
             public void visit(FeatureTypeInfo obj) {
                 obj = ModificationProxy.unwrap(obj);
                 catalogDAO.update(obj);
             }
 
             public void visit(CoverageInfo obj) {
                 obj = ModificationProxy.unwrap(obj);
                 catalogDAO.update(obj);
             }
 
             public void visit(LayerInfo obj) {
                 obj = ModificationProxy.unwrap(obj);
                 catalogDAO.update(obj);
             }
 
             public void visit(StyleInfo obj) {
                 obj = ModificationProxy.unwrap(obj);
                 catalogDAO.update(obj);
             }
 
             public void visit(LayerGroupInfo obj) {
                 obj = ModificationProxy.unwrap(obj);
                 catalogDAO.update(obj);
             }
         };
 
     }
 
 }
