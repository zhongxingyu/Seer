 package org.apache.felix.ipojo.everest.osgi.bundle;
 
 import org.apache.felix.ipojo.everest.impl.DefaultParameter;
 import org.apache.felix.ipojo.everest.impl.DefaultRelation;
 import org.apache.felix.ipojo.everest.impl.DefaultResource;
 import org.apache.felix.ipojo.everest.impl.ImmutableResourceMetadata;
 import org.apache.felix.ipojo.everest.osgi.AbstractResourceCollection;
 import org.apache.felix.ipojo.everest.services.*;
 import org.osgi.framework.Bundle;
 import org.osgi.framework.BundleException;
 import org.osgi.framework.Constants;
 import org.osgi.framework.Version;
 import org.osgi.framework.startlevel.BundleStartLevel;
 import org.osgi.framework.wiring.*;
 
 import java.io.ByteArrayInputStream;
 import java.io.InputStream;
 import java.util.*;
 
 import static org.apache.felix.ipojo.everest.osgi.OsgiResourceUtils.BundleNamespace.*;
 import static org.apache.felix.ipojo.everest.osgi.OsgiResourceUtils.*;
 
 
 /**
  * Resource representing a bundle.
  */
 public class BundleResource extends AbstractResourceCollection {
 
     /**
      * Relation name for update relation
      */
     public final static String UPDATE_RELATION = "update";
 
     /**
      * Relation name for uninstall relation
      */
     public static final String UNINSTALL_RELATION = "uninstall";
 
     /**
      * Parameter for state change for update relation
      */
     public final static String NEW_STATE_RELATION_PARAMETER = "newState";
 
     /**
      * Parameter for input for update relation
      */
     public final static String UPDATE_INPUT_PARAMETER = "input";
 
     /**
      * Parameter for update for update relation
      */
     public static final String UPDATE_PARAMETER = "update";
 
     /**
      * Parameter for refresh for update relation
      */
     public static final String REFRESH_PARAMETER = "refresh";
 
     /**
      * Parameter for bundle start level for update relation
      */
     public static final String START_LEVEL_PARAMETER = "startLevel";
 
     /**
      * Name for capabilities
      */
     public static final String CAPABILITIES_PATH = "capabilities";
 
     /**
      * Path for capabilities
      */
     private final Path capabilitiesPath = getPath().addElements(CAPABILITIES_PATH);
 
     /**
      * Name for requirements
      */
     public static final String REQUIREMENTS_PATH = "requirements";
 
     /**
      * Path for requirements
      */
     private final Path requirementsPath = getPath().addElements(REQUIREMENTS_PATH);
 
     /**
      * Name for wires
      */
     public static final String WIRES_PATH = "wires";
 
     /**
      * Path for wires
      */
     private final Path wiresPath = getPath().addElements(WIRES_PATH);
 
     /**
      * Represented bundle
      */
     private final Bundle m_bundle;
 
     /**
      * if this bundle is a fragment
      */
     private final boolean isFragment;
 
     /**
      * Reference to bundle resource manager
      */
     private final BundleResourceManager m_bundleResourceManager;
 
     /**
      * Child resource for bundle headers
      */
     private final BundleHeadersResource m_bundleHeadersResource;
 
     /**
      * Child resource for services registered by this bundle
      */
     private final BundleServicesResource m_bundleServicesResource;
 
     /**
      * Capabilities of this bundle
      */
     private final Map<String, Resource> m_capabilitiesResourceMap;
 
     /**
      * Requirements of this bundle
      */
     private final Map<String, Resource> m_requirementsResourceMap;
 
     /**
      * Constructor for bundle resource
      *
      * @param bundle                represented {@code Bundle}
      * @param bundleResourceManager {@code BundleResourceManager}
      */
     public BundleResource(Bundle bundle, BundleResourceManager bundleResourceManager) {
         super(BundleResourceManager.BUNDLE_PATH.addElements(Long.toString(bundle.getBundleId())));
         m_bundle = bundle;
         m_bundleResourceManager = bundleResourceManager;
 
         // Check if is fragment
         BundleRevision rev = m_bundle.adapt(BundleRevision.class);
         isFragment = (rev != null && (rev.getTypes() & BundleRevision.TYPE_FRAGMENT) != 0);
 
         m_bundleHeadersResource = new BundleHeadersResource(getPath(), m_bundle);
         m_bundleServicesResource = new BundleServicesResource(getPath(), m_bundle);
 
         m_capabilitiesResourceMap = new HashMap<String, Resource>();
         m_requirementsResourceMap = new HashMap<String, Resource>();
 
         initializeCapabilitiesRequirements();
 
         setRelations(
                 new DefaultRelation(getPath(), Action.UPDATE, UPDATE_RELATION,
                         new DefaultParameter()
                                 .name(START_LEVEL_PARAMETER)
                                .description(START_LEVEL_PARAMETER)
                                 .optional(true)
                                 .type(Integer.class),
                         new DefaultParameter()
                                 .name(UPDATE_PARAMETER)
                                .description(UPDATE_PARAMETER)
                                 .optional(true)
                                 .type(Boolean.class),
                         new DefaultParameter()
                                 .name(UPDATE_INPUT_PARAMETER)
                                 .description(UPDATE_INPUT_PARAMETER)
                                 .optional(true)
                                 .type(ByteArrayInputStream.class),
                         new DefaultParameter()
                                 .name(NEW_STATE_RELATION_PARAMETER)
                                 .description(BUNDLE_STATE)
                                 .optional(true)
                                 .type(String.class),
                         new DefaultParameter()
                                 .name(REFRESH_PARAMETER)
                                 .description(REFRESH_PARAMETER)
                                 .optional(true)
                                 .type(Boolean.class)),
                 new DefaultRelation(getPath(), Action.DELETE, UNINSTALL_RELATION)
         );
     }
 
     public ResourceMetadata getSimpleMetadata() {
         ImmutableResourceMetadata.Builder metadataBuilder = new ImmutableResourceMetadata.Builder();
         metadataBuilder.set(BUNDLE_ID, m_bundle.getBundleId());
         metadataBuilder.set(BUNDLE_STATE, bundleStateToString(m_bundle.getState()));
         metadataBuilder.set(Constants.BUNDLE_SYMBOLICNAME_ATTRIBUTE, m_bundle.getSymbolicName());
         metadataBuilder.set(Constants.BUNDLE_VERSION_ATTRIBUTE, m_bundle.getVersion());
         return metadataBuilder.build();
     }
 
     @Override
     public ResourceMetadata getMetadata() {
         ImmutableResourceMetadata.Builder metadataBuilder = new ImmutableResourceMetadata.Builder(getSimpleMetadata());
         metadataBuilder.set(BUNDLE_LOCATION, m_bundle.getLocation());
         metadataBuilder.set(BUNDLE_LAST_MODIFIED, m_bundle.getLastModified());
         metadataBuilder.set(BUNDLE_FRAGMENT, isFragment);
         //TODO find some properties to add here!
         return metadataBuilder.build();
     }
 
     @Override
     public List<Resource> getResources() {
         ArrayList<Resource> resources = new ArrayList<Resource>();
 
         // add headers
         resources.add(m_bundleHeadersResource);
         // add capabilities
         DefaultResource.Builder builder = new Builder().fromPath(capabilitiesPath);
         for (Resource bundleCapabilityResource : m_capabilitiesResourceMap.values()) {
             builder.with(bundleCapabilityResource);
             builder.with(new DefaultRelation(bundleCapabilityResource.getPath(), Action.READ, capabilitiesPath.getLast() + ":" + bundleCapabilityResource.getPath().getLast()));
         }
         try {
             resources.add(builder.build());
         } catch (IllegalResourceException e) {
             // should never happen
         }
         // add requirements
         builder = new Builder().fromPath(requirementsPath);
         for (Resource bundleRequirementResource : m_requirementsResourceMap.values()) {
             builder.with(bundleRequirementResource);
             builder.with(new DefaultRelation(bundleRequirementResource.getPath(), Action.READ, requirementsPath.getLast() + ":" + bundleRequirementResource.getPath()));
         }
         try {
             resources.add(builder.build());
         } catch (IllegalResourceException e) {
             // should never happen
         }
         // add wires
         builder = new Builder().fromPath(wiresPath);
         BundleWiring wiring = m_bundle.adapt(BundleWiring.class);
         if (wiring != null) {
             // get provided wires from all namespaces
             List<BundleWire> providedWires = wiring.getProvidedWires(null);
             if (providedWires != null) {
                 for (BundleWire providedWire : providedWires) {
                     builder.with(new BundleWireResource(wiresPath, providedWire));
                 }
             }
             // get required wires from all namespaces
             List<BundleWire> requiredWires = wiring.getRequiredWires(null);
             if (requiredWires != null) {
                 for (BundleWire requiredWire : requiredWires) {
                     builder.with(new BundleWireResource(wiresPath, requiredWire));
                 }
             }
         }
         try {
             resources.add(builder.build());
         } catch (IllegalResourceException e) {
             // should never happen
         }
         // add services
         resources.add(m_bundleServicesResource);
         return resources;
     }
 
     @Override
     public <A> A adaptTo(Class<A> clazz) {
         if (Bundle.class.equals(clazz)) {
             return (A) m_bundle;
         } else if (BundleResource.class.equals(clazz)) {
             return (A) this;
         } else {
             return null;
         }
     }
 
     @Override
     public boolean isObservable() {
         return true;
     }
 
     @Override
     public Resource update(Request request) throws IllegalActionOnResourceException {
         // start level update
         Integer startLevel = request.get(START_LEVEL_PARAMETER, Integer.class);
         if (startLevel != null) {
             this.setStartLevel(startLevel);
         }
         // update bundle
         Boolean update = request.get(UPDATE_PARAMETER, Boolean.class);
         if (update != null && update) {
             InputStream ioStream = request.get(UPDATE_INPUT_PARAMETER, ByteArrayInputStream.class);
             try {
                 this.update(ioStream);
             } catch (IllegalActionOnResourceException e) {
                 throw new IllegalActionOnResourceException(request, e.getMessage());
             }
         }
         // Change bundle state
         String newStateString = request.get(NEW_STATE_RELATION_PARAMETER, String.class);
         if (newStateString != null) {
             try {
                 this.changeState(newStateString);
             } catch (IllegalActionOnResourceException e) {
                 throw new IllegalActionOnResourceException(request, e.getMessage());
             }
         }
         // Refresh bundle
         Boolean refresh = request.get(REFRESH_PARAMETER, Boolean.class);
         if (refresh != null && refresh) {
             this.refresh();
         }
 
         return this;
     }
 
     @Override
     public Resource delete(Request request) throws IllegalActionOnResourceException {
         try {
             this.uninstall();
         } catch (IllegalActionOnResourceException e) {
             throw new IllegalActionOnResourceException(request, e.getMessage());
         }
         return this;
     }
 
     public Bundle getBundle() {
         return m_bundle;
     }
 
     public String getState() {
         return bundleStateToString(m_bundle.getState());
     }
 
     public long getBundleId() {
         return m_bundle.getBundleId();
     }
 
     public String getSymbolicName() {
         return m_bundle.getSymbolicName();
     }
 
     public Version getVersion() {
         return m_bundle.getVersion();
     }
 
     public String getLocation() {
         return m_bundle.getLocation();
     }
 
     public long getLastModified() {
         return m_bundle.getLastModified();
     }
 
     public int getStartLevel() {
         BundleStartLevel bundleStartLevel = m_bundle.adapt(BundleStartLevel.class);
         return bundleStartLevel.getStartLevel();
     }
 
     public void setStartLevel(int startLevel) {
         BundleStartLevel bundleStartLevel = m_bundle.adapt(BundleStartLevel.class);
         bundleStartLevel.setStartLevel(startLevel);
     }
 
     public void update(InputStream inputStream) throws IllegalActionOnResourceException {
         try {
             if (inputStream != null) {
                 m_bundle.update(inputStream);
             } else {
                 m_bundle.update();
             }
         } catch (BundleException e) {
             throw new IllegalActionOnResourceException(null, e.getMessage());
         }
     }
 
     public String changeState(String newStateString) throws IllegalActionOnResourceException {
         try {
             int newState = toBundleState(newStateString);
             // calculate new state
             if (m_bundle.getState() != newState) {
                 switch (newState) {
                     case Bundle.ACTIVE:
                         m_bundle.start();
                         break;
                     case Bundle.RESOLVED:
                         if (m_bundle.getState() == Bundle.INSTALLED) {
                             this.m_bundleResourceManager.resolveBundles(Collections.singletonList(m_bundle.getBundleId()));
                         } else if (m_bundle.getState() == Bundle.ACTIVE) {
                             m_bundle.stop();
                         }
                         break;
                     case Bundle.INSTALLED:
                         // TODO update with cached bundle
                         // this is not a good idea.. should do m_bundle.update(cached bundle);  instead
                         m_bundle.stop();
                         m_bundle.update();
                     case Bundle.UNINSTALLED:
                         m_bundle.uninstall();
                         break;
                     default:
                         break;
                 }
             }
         } catch (BundleException e) {
             throw new IllegalActionOnResourceException(null, e.getMessage());
         } catch (IllegalArgumentException e) {
             throw new IllegalActionOnResourceException(null, e.getMessage());
         }
         return this.getState();
     }
 
     public void refresh() {
         this.m_bundleResourceManager.refreshBundles(Collections.singletonList(m_bundle.getBundleId()));
     }
 
     public void uninstall() throws IllegalActionOnResourceException {
         try {
             m_bundle.uninstall();
         } catch (BundleException e) {
             throw new IllegalActionOnResourceException(null, e.getMessage());
         }
     }
 
     protected void initializeCapabilitiesRequirements() {
 
         m_requirementsResourceMap.clear();
         m_capabilitiesResourceMap.clear();
 
         BundleWiring wiring = m_bundle.adapt(BundleWiring.class);
         if (wiring != null) {
             List<BundleCapability> capabilities = wiring.getCapabilities(null);
             if (capabilities != null) {
                 for (BundleCapability capability : capabilities) {
                     BundleCapabilityResource bundleCapabilityResource = new BundleCapabilityResource(capabilitiesPath, capability);
                     m_capabilitiesResourceMap.put(uniqueCapabilityId(capability), bundleCapabilityResource);
                 }
             }
             List<BundleRequirement> requirements = wiring.getRequirements(null);
             if (requirements != null) {
                 for (BundleRequirement requirement : requirements) {
                     BundleRequirementResource bundleRequirementResource = new BundleRequirementResource(requirementsPath, requirement);
                     m_requirementsResourceMap.put(uniqueRequirementId(requirement), bundleRequirementResource);
                 }
             }
         }
     }
 
 }
