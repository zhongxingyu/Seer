 package org.jboss.pressgang.ccms.rest;
 
 import org.jboss.pressgang.ccms.rest.v1.client.PressGangCCMSProxyFactoryV1;
 import org.jboss.pressgang.ccms.rest.v1.jaxrsinterfaces.RESTInterfaceV1;
 import org.jboss.pressgang.ccms.utils.RESTCollectionCache;
 import org.jboss.pressgang.ccms.utils.RESTEntityCache;
 
 /**
  * A class to manage components needed to interact with the REST Interface.
  */
 public class RESTManager {
 
     private final PressGangCCMSProxyFactoryV1 proxyFactory;
    private final RESTInterfaceV1 client;
     private final RESTEntityCache entityCache = new RESTEntityCache();
     private final RESTCollectionCache collectionCache = new RESTCollectionCache(entityCache);
 
     public RESTManager(final String serverUrl) {
         proxyFactory = PressGangCCMSProxyFactoryV1.create(serverUrl);
        client = proxyFactory.getRESTClient();
     }
 
     public RESTInterfaceV1 getRESTClient() {
         return client;
     }
 
     public RESTEntityCache getRESTEntityCache() {
         return entityCache;
     }
 
     public RESTCollectionCache getRESTCollectionCache() {
         return collectionCache;
     }
 
     public PressGangCCMSProxyFactoryV1 getProxyFactory() {
         return proxyFactory;
     }
 }
