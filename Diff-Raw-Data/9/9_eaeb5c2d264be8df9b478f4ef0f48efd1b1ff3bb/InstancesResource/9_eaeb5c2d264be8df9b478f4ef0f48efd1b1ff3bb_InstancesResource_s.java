 package org.apache.felix.ipojo.everest.ipojo;
 
 import org.apache.felix.ipojo.*;
 import org.apache.felix.ipojo.architecture.Architecture;
 import org.apache.felix.ipojo.everest.impl.DefaultReadOnlyResource;
 import org.apache.felix.ipojo.everest.impl.DefaultRequest;
 import org.apache.felix.ipojo.everest.impl.ImmutableResourceMetadata;
 import org.apache.felix.ipojo.everest.services.*;
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.InvalidSyntaxException;
 import org.osgi.framework.ServiceReference;
 import org.osgi.framework.Version;
 
 import java.util.*;
 
 import static java.lang.String.format;
 
 /**
  * '/ipojo/instance' resource.
  */
 public class InstancesResource extends DefaultReadOnlyResource {
 
     public static final Path PATH = IpojoResource.PATH.addElements("instance");
     public static final String FACTORY_NAME = "factory.name";
     public static final String FACTORY_VERSION = "factory.version";
 
     private final BundleContext m_context;
     private final Map<String, InstanceNameResource> m_instances = new LinkedHashMap<String, InstanceNameResource>();
 
     public InstancesResource(BundleContext context) {
         super(PATH);
         m_context = context;
     }
 
     void addInstance(Architecture instance) {
         synchronized (m_instances) {
             m_instances.put(instance.getInstanceDescription().getName(), new InstanceNameResource(instance));
         }
     }
 
     void removeInstance(Architecture instance) {
         synchronized (m_instances) {
             m_instances.remove(instance.getInstanceDescription().getName()).setStale();
         }
     }
 
     @Override
     public List<Resource> getResources() {
         synchronized (m_instances) {
             return new ArrayList<Resource>(m_instances.values());
         }
     }
 
     @Override
     public ResourceMetadata getMetadata() {
         ImmutableResourceMetadata.Builder b = new ImmutableResourceMetadata.Builder();
         synchronized (m_instances) {
             // For each instance name...
             for (String name : m_instances.keySet()) {
                 b.set(name, m_instances.get(name).getMetadata());
             }
         }
         return b.build();
     }
 
     @Override
     public List<Relation> getRelations() {
         // TODO aggregate relations of m_instances
         return super.getRelations();
     }
 
     /**
      * We need to override this to handle the creation of non-existing instances, i.e. a CREATE request on an unknown resource.
      */
     @Override
     public Resource process(Request request) throws IllegalActionOnResourceException, ResourceNotFoundException {
 
         // Filter out non-CREATE requests
         if (request.action() != Action.CREATE) {
             return super.process(request);
         }
 
         // Retrieve the mandatory factory name parameter
         String factoryName = null;
         try {
             factoryName = request.get(FACTORY_NAME, String.class);
         } catch (Exception e) {
             // Not a string : ignored!
         }
         if (factoryName == null) {
             return super.process(request);
         }
 
         // Retrieve the optional factory version parameter
         Version factoryVersion = null;
         try {
             String v = request.get(FACTORY_VERSION, String.class);
             if (v != null) {
                factoryVersion = Version.parseVersion(v);
             }
         } catch (Exception e) {
             // Ignored, version set to null.
         }
 
         // Retrieve the instance name
         // To be eligible for an instance creation, the remaining path must contain exactly one element.
         Path instanceName = null;
         try {
             instanceName = request.path().subtract(PATH);
         } catch (Exception e) {
             // Ignored!
         }
         if (instanceName == null || instanceName.getCount() != 1) {
             return super.process(request);
         }
 
         // Now we have enough to try to create the instance...
 
         Hashtable<String, Object> config = new Hashtable<String, Object>(request.parameters());
         // Not part of the instance configuration
         config.remove(FACTORY_NAME);
         config.remove(FACTORY_VERSION);
         config.put("instance.name", instanceName.getElement(0));
 
         // Get the factory
        ServiceReference<Factory> ref = getFactory(factoryName, factoryVersion.toString());
         Factory factory =  m_context.getService(ref);
         ComponentInstance instance;
         try {
             // Create an instance with the configuration
             instance = factory.createComponentInstance(config);
         } catch (Exception e) {
             IllegalActionOnResourceException ee = new IllegalActionOnResourceException(request, this, "cannot create component instance");
             ee.initCause(e);
             throw ee;
         } finally {
             m_context.ungetService(ref);
         }
 
         // Return the
         Resource result;
         try {
             result = process(new DefaultRequest(Action.READ, PATH.addElements(instance.getInstanceName()), null));
         } catch (ResourceNotFoundException e) {
             // An instance has been created, however its resource is not here.
             //TODO Should we fail here??? Can null returned value be considered as a confession of failure?
             return null;
         }
 
         return result;
     }
 
 
 
     private ServiceReference<Factory> getFactory(String name, String version) {
         // Scientifically build the selection filter.
         String filter = "(&(factory.name=" + name + ")";
         if (version != null) {
             filter += "(factory.version=" + version + ")";
         } else {
             filter += "(!(factory.version=*))";
         }
         filter += ")";
         Collection<ServiceReference<Factory>> refs = null;
         try {
             refs = m_context.getServiceReferences(Factory.class, filter);
         } catch (InvalidSyntaxException e) {
             throw new IllegalStateException(format("cannot get factory service with same name/version: %s/%s", name, version), e);
         }
         if (refs.isEmpty()) {
             throw new IllegalStateException(format("no factory service with same name/version: %s/%s", name, version));
         } else if (refs.size() > 1) {
             // Should never happen!
             throw new IllegalStateException(format("multiple factory service with same name/version: %s/%s", name, version));
         }
         return refs.iterator().next();
     }
 }
