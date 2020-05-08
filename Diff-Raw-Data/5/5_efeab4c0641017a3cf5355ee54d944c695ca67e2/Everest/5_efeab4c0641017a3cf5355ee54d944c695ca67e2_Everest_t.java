 package org.apache.felix.ipojo.everest.core;
 
 import org.apache.felix.ipojo.annotations.*;
 import org.apache.felix.ipojo.everest.impl.DefaultReadOnlyResource;
 import org.apache.felix.ipojo.everest.managers.everest.EverestRootResource;
 import org.apache.felix.ipojo.everest.services.*;
 import org.osgi.service.event.Event;
 import org.osgi.service.event.EventAdmin;
 
 import java.util.*;
 
 /**
  * Everest Core.
  */
 @Component
 @Instantiate
 @Provides(specifications = EverestService.class)
 public class Everest extends DefaultReadOnlyResource implements EverestService {
 
     /**
      * The system property used to send events synchronously.
      */
     public static final String SYNCHRONOUS_PROCESSING = "everest.processing.synchronous";
 
     private Map<Path, Resource> resources = new LinkedHashMap<Path, Resource>();
     private List<ResourceExtender> extenders = new ArrayList<ResourceExtender>();
 
     public Everest() {
         super(Path.from("/"));
         // Add the everest domain
         resources.put(Path.from("/everest"), new EverestRootResource(this));
     }
 
     /**
      * The EventAdmin service, or {@code null} if it's not present.
      */
     private static volatile EventAdmin eventAdmin;
 
     @Bind(optional = true, aggregate = true)
     public void bindRootResource(Resource resource) {
         synchronized (this) {
             resources.put(resource.getCanonicalPath(), resource);
         }
     }
 
     @Unbind
     public void unbindRootResource(Resource resource) {
         synchronized (this) {
             resources.remove(resource.getCanonicalPath());
         }
     }
 
     @Bind(optional = true, aggregate = true)
     public void bindExtender(ResourceExtender extender) {
         synchronized (this) {
             extenders.add(extender);
         }
     }
 
     @Unbind
     public void unbindExtender(ResourceExtender extender) {
         synchronized (this) {
             extenders.remove(extender);
         }
     }
 
     public synchronized Map<Path, Resource> getEverestResources() {
         return new TreeMap<Path, Resource>(resources);
     }
 
     public synchronized List<Resource> getResources() {
         return new ArrayList<Resource>(resources.values());
     }
 
     public synchronized List<ResourceExtender> getExtenders() {
         return new ArrayList<ResourceExtender>(extenders);
     }
 
     public Resource process(Request request) throws IllegalActionOnResourceException, ResourceNotFoundException {
         // We can't extend when the original action fails.
 
         Resource result = super.process(request);
 
         // Extensions
         // We must update the resulted resource with the extensions
         for (ResourceExtender extender : getExtenders()) {
             if (extender.getFilter().accept(result)) {
                 result = extender.extend(request, result);
             }
         }
 
         return result;
     }
 
     @Bind(optional = true, proxy = false)
     public void bindEventAdmin(EventAdmin ea) {
         eventAdmin = ea;
     }
 
     @Unbind(optional = true, proxy = false)
     public void unbindEventAdmin(EventAdmin ea) {
         eventAdmin = null;
     }
 
     /**
      * Post (asynchronously) the state of the given resource.
      * <p>
      * The topic of the sent event is the complete canonical path of the resource ({@code /everest/...}).
      * </p>
      *
      * @param eventType type of posted resource event
      * @param resource  concerned resource
      * @return true if event is posted to event admin, else false.
      */
     public static boolean postResource(ResourceEvent eventType, Resource resource) {
         EventAdmin ea = eventAdmin;
         if (ea == null || !resource.isObservable()) {
             return false;
         }
 
         Map<String, Object> map = new LinkedHashMap<String, Object>();
        map.put("eventType", eventType.toString());
         map.put("canonicalPath", resource.getCanonicalPath().toString());
         map.put("metadata", resource.getMetadata());
         map.put("relations", resource.getRelations());
 
         Event e = new Event(topicFromPath(resource.getCanonicalPath()), map);
 
         String mode = System.getProperty(SYNCHRONOUS_PROCESSING);
         try {
             if (mode != null && mode.equalsIgnoreCase("true")) {
                 // Sync mode
                 ea.sendEvent(e);
             } else {
                 // Async mode (default)
                 ea.postEvent(e);
             }
         } catch (SecurityException ex) {
             return false;
         }
         return true;
     }
 
     /**
      * Transforms a path to event admin topic
      *
      * @param path resource path
      * @return topic string
      */
     public static String topicFromPath(Path path) {
         String pathString = path.toString();
         pathString = "everest".concat(pathString);
        pathString = pathString.replaceAll("\\.", "-");
         return pathString;
     }
 }
