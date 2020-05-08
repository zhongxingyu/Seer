 package com.rackspacecloud.client.service_registry.curator;
 
 import com.netflix.curator.utils.ThreadUtils;
 import com.netflix.curator.x.discovery.ServiceCacheBuilder;
 import com.netflix.curator.x.discovery.ServiceDiscovery;
 import com.netflix.curator.x.discovery.ServiceInstance;
 import com.netflix.curator.x.discovery.ServiceProviderBuilder;
 import com.netflix.curator.x.discovery.strategies.RoundRobinStrategy;
 import com.rackspacecloud.client.service_registry.Client;
 import com.rackspacecloud.client.service_registry.HeartBeater;
 import com.rackspacecloud.client.service_registry.PaginationOptions;
 import com.rackspacecloud.client.service_registry.SessionCreateResponse;
 import com.rackspacecloud.client.service_registry.events.client.ClientEvent;
 import com.rackspacecloud.client.service_registry.events.client.HeartbeatAckEvent;
 import com.rackspacecloud.client.service_registry.events.client.HeartbeatErrorEvent;
 import com.rackspacecloud.client.service_registry.events.client.HeartbeatEventListener;
 import com.rackspacecloud.client.service_registry.events.client.HeartbeatStoppedEvent;
 import com.rackspacecloud.client.service_registry.objects.Service;
 import com.rackspacecloud.client.service_registry.objects.Session;
 
 import java.io.IOException;
 import java.lang.annotation.Annotation;
 import java.lang.reflect.Field;
 import java.lang.reflect.Method;
 import java.lang.reflect.Modifier;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.MissingResourceException;
 import java.util.Set;
 
 public class RSRServiceDiscoveryImpl<T> implements ServiceDiscovery<T> {
     public static final String DISCOVERY = "discovery";
     
     private static final String NAME = "name";
     private static final String ADDRESS = "address";
     private static final String PORT = "port";
     private static final String REG_TIME = "regtime";
     private static final String SVC_TYPE = "svcType";
     private static final String SSL_PORT = "sslPort";
     private static final String URI_SPEC = "uriSpec";
     
     private final Client client;
     private final Class<T> typeClass;
     private final String typeTag;
     private final Method convert;
     
     private volatile Session session;
     private volatile HeartBeater heartbeater;
     
     private final HeartbeatEventListener heartbeatEventListener;
     private final Map<String, ServiceInstance> services = new HashMap<String, ServiceInstance>(); // needs synchronized
         
     public RSRServiceDiscoveryImpl(Client client, Class<T> type) {
         // deep validation has already been done.
         try {
             Method m = type.getMethod("convert", Service.class);
             m.setAccessible(true);
             convert = m;
         } catch (NoSuchMethodException ex) {
             throw new MissingResourceException("Class does not implement static convert() method", type.getName(), "convert");
         } catch (Exception ex) {
             throw new MissingResourceException(ex.getMessage(), type.getName(), "convert");
         }
         
         this.client = client;
         this.typeClass = type;
         this.typeTag = Utils.sanitizeTag(type.getName());
         this.heartbeatEventListener = new CuratorHeartbeatEventListener();
     }
     public void start() throws Exception {
         // create a session. keep in mind that session ids are ephemeral and may change frequently.
         getSession();
     }
 
     public void registerService(ServiceInstance<T> service) throws Exception {
         List<String> tags = new ArrayList<String>();
         tags.add(typeTag);
         tags.add(service.getName());
         tags.add("curator-x-discovery");
         Service fsService = client.getServicesClient().create(
                 service.getId(), 
                 getSession().getId(), 
                 tags, 
                 getMetadata(service));
         services.put(service.getId(), service);
     }
 
     public void updateService(ServiceInstance<T> service) throws Exception {
         unregisterService(service);
         registerService(service);
     }
 
     public void unregisterService(ServiceInstance<T> service) throws Exception {
         client.getServicesClient().delete(service.getId());
         services.remove(service.getId());
     }
 
     public ServiceCacheBuilder<T> serviceCacheBuilder() {
         return new RSRServiceCacheBuilderImpl<T>(this)
                 .threadFactory(ThreadUtils.newThreadFactory("RSRServiceCache"));
         // todo: whither 'name'?
     }
 
     public Collection<String> queryForNames() throws Exception {
         Set<String> names = new HashSet<String>();
         PaginationOptions options = new PaginationOptions(100, null);
         List<Service> services = null;
         
         do {
             services = client.getServicesClient().list(options);
             for (Service service : services) {
                 options = options.withMarker(service.getId());
                 if (!service.getTags().contains(typeTag)) {
                     continue;
                 }
                 String name = service.getMetadata().get(NAME);
                 if (!names.contains(name)) {
                     names.add(name);
                 }
             }
         } while (services != null && services.size() > 1);
         
         return names;
     }
 
     public Collection<ServiceInstance<T>> queryForInstances(String name) throws Exception {
         List<ServiceInstance<T>> serviceInstances = new ArrayList<ServiceInstance<T>>();
         PaginationOptions options = new PaginationOptions(100, null);
         List<Service> services = null;
         do {
             services = client.getServicesClient().list(options);
             for (Service service : services) {
                 if (service.getTags().contains(typeTag) && service.getMetadata().get(NAME).equals(name)) {
                     // does the job of the serializer in the curator code (theirs is just a json marshaller anyway).
                     serviceInstances.add(convert(service));
                 }
                 options = options.withMarker(service.getId());
             }
         } while (services != null && services.size() > 1);
         return serviceInstances;
     }
 
     public ServiceInstance<T> queryForInstance(String name, String id) throws Exception {
         return (ServiceInstance<T>) convert.invoke(typeClass, client.getServicesClient().get(id));
     }
 
     public ServiceProviderBuilder<T> serviceProviderBuilder() {
         return new RSRServiceProviderBuilderImpl<T>(this)
                 // todo: what about these pieces?
                 //.refreshPaddingMs(1000)
                 //.serviceName("foo")
                 .providerStrategy(new RoundRobinStrategy<T>())
                 .threadFactory(ThreadUtils.newThreadFactory("RSRServiceProvider")); 
     }
 
     public void close() throws IOException {
         if (this.session != null && this.heartbeater != null) {
             this.heartbeater.removeEventListener(this.heartbeatEventListener);
             this.heartbeater.stop();
         }
     }
     
     //
     // helpers
     //
     
     public Client getClient() { return client; }
     public String getType() { return typeTag; }
     
     public ServiceInstance<T> convert(Service service) throws Exception {
         return (ServiceInstance<T>) convert.invoke(typeClass, service);
     }
     
     private synchronized Session getSession() throws Exception {
         if (this.session == null) {
             Map<String, String> sessionMeta = new HashMap<String, String>();
             sessionMeta.put(DISCOVERY, typeTag);
             SessionCreateResponse res = client.getSessionsClient().create(30, sessionMeta);
             heartbeater = res.getHeartbeater();
             heartbeater.addEventListener(this.heartbeatEventListener);
             heartbeater.start();
             this.session = res.getSession();
         }
         return session;
     }
     
     private synchronized void registerAll() throws Exception {
         for (ServiceInstance svc : services.values()) {
             registerService(svc);
         }
     }
     
     private static Map<String, String> getMetadata(ServiceInstance service) {
         Map<String, String> map = new HashMap<String, String>();
         
         map.put(NAME, service.getName());
         map.put(ADDRESS, service.getAddress());
         if (service.getPort() != null)
             map.put(PORT, service.getPort().toString());
         map.put(REG_TIME, Long.toString(service.getRegistrationTimeUTC()));
         map.put(SVC_TYPE, service.getServiceType().name());
         if (service.getSslPort() != null)
             map.put(SSL_PORT, service.getSslPort().toString());
         if (service.getUriSpec() != null)
             map.put(URI_SPEC, service.getUriSpec().build());
         
         // what else?
         for (Field f : getMetaFields(service.getPayload().getClass())) {
             try {
                 f.setAccessible(true);
                 map.put(f.getName(), f.get(service.getPayload()).toString());
             } catch (Exception ex) {
                 // todo: log
             }
         }
         
         return map;
     }
     
     private static Collection<Field> getMetaFields(Class cls) {
         List<Field> allFields = new ArrayList<Field>();
         List<Field> metaFields = new ArrayList<Field>();
         for (Field f : cls.getDeclaredFields())
             allFields.add(f);
         for (Field f : cls.getFields())
             allFields.add(f);
         for (Field f : allFields) {
             for (Annotation a : f.getAnnotations()) {
                 if (a.annotationType().equals(Meta.class)) {
                     metaFields.add(f);
                 }
             }
         }
         return metaFields;
         
     }
     
     private class CuratorHeartbeatEventListener extends HeartbeatEventListener {   
         private void clearSession(ClientEvent event) {
             ((HeartBeater)event.getSource()).removeEventListener(this);
             RSRServiceDiscoveryImpl.this.session = null;
             RSRServiceDiscoveryImpl.this.heartbeater = null;
         }
         
         @Override
         public void onAck(HeartbeatAckEvent ack) {
             // do nothing.
         }
  
         @Override
         public void onStopped(HeartbeatStoppedEvent stopped) {
             // session was stopped cleanly.
             clearSession(stopped);
             if (stopped.isError()) {
                 try {
                     registerAll();
                 } catch (Exception ex) {
                     // depends on what the exception policy is.
                 }
             }
         }
  
         @Override
         public void onError(HeartbeatErrorEvent error) {
             clearSession(error);
             if (error.isError()) {
                 try {
                     registerAll();
                 } catch (Exception ex) {
                     // depends on what the exception policy is.
                 }
             }
             
         }
     }
 }
