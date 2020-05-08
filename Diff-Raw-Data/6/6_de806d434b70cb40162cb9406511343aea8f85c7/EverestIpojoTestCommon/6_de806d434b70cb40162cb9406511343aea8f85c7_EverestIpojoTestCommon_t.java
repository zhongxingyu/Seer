 package org.apache.felix.ipojo.everest.ipojo.test;
 
 import org.apache.commons.io.FileUtils;
 import org.apache.felix.ipojo.ComponentInstance;
 import org.apache.felix.ipojo.Factory;
 import org.apache.felix.ipojo.HandlerFactory;
 import org.apache.felix.ipojo.IPojoFactory;
 import org.apache.felix.ipojo.architecture.Architecture;
 import org.apache.felix.ipojo.architecture.InstanceDescription;
 import org.apache.felix.ipojo.everest.impl.DefaultRequest;
 import org.apache.felix.ipojo.everest.services.*;
 import org.apache.felix.ipojo.extender.ExtensionDeclaration;
 import org.apache.felix.ipojo.extender.TypeDeclaration;
 import org.junit.Before;
 import org.junit.Test;
 import org.ops4j.pax.exam.Option;
 import org.ops4j.pax.tinybundles.core.TinyBundle;
 import org.ops4j.pax.tinybundles.core.TinyBundles;
 import org.osgi.framework.*;
 import org.osgi.service.event.Event;
 import org.osgi.service.event.EventConstants;
 import org.osgi.service.event.EventHandler;
 import org.ow2.chameleon.testing.helpers.BaseTest;
 import org.ow2.chameleon.testing.tinybundles.ipojo.IPOJOStrategy;
 
 import javax.inject.Inject;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.lang.reflect.Field;
 import java.lang.reflect.Method;
 import java.net.MalformedURLException;
 import java.util.Collection;
 import java.util.Dictionary;
 import java.util.Hashtable;
 import java.util.Map;
 
 import static java.util.Map.Entry;
 import static junit.framework.Assert.assertNotNull;
 import static org.ops4j.pax.exam.CoreOptions.*;
 
 /**
  * Common configuration for the everest iPOJO tests.
  */
 public class EverestIpojoTestCommon extends BaseTest {
 
     /**
      * The bundle symbolic name of the first generated test bundle.
      */
     public static final String TEST_BUNDLE_SYMBOLIC_NAME = "test.bundle";
 
     /**
      * The bundle symbolic name of the second generated test bundle.
      */
     public static final String TEST_BUNDLE_2_SYMBOLIC_NAME = "test.bundle2";
 
     /**
      * The iPOJO namespace.
      */
     public static final String IPOJO = "org.apache.felix.ipojo";
 
     /**
      * The name of the "Bar" factories.
      */
     public static final String BAR = IPOJO + ".everest.ipojo.test.b1.BarProviderImpl";
 
     /**
      * The class name of the "Bar" 2.0.0 factory.
      */
     public static final String BAR_2 = IPOJO + ".everest.ipojo.test.b2.BarProviderImpl";
 
     /**
      * The everest service.
      */
     @Inject
     EverestService everest;
 
     /**
      * The iPOJO bundle.
      */
     Bundle ipojoBundle;
 
     /**
      * The first generated test bundle.
      */
     Bundle testBundle;
 
     /**
      * The second generated test bundle.
      */
     Bundle testBundle2;
 
     /**
      * The registered event handler service.
      */
     ServiceRegistration<EventHandler> eventHandlerService;
 
     /**
      * Disable construction of the test bundle, as we need to construct manually two of them.
      *
      * @return {@code false}
      */
     @Override
     public boolean deployTestBundle() {
         return false;
     }
 
     /**
      * Enable deployment of the ConfigAdmin bundle, needed by everest OSGi.
      *
      * @return {@code true}
      */
     @Override
     public boolean deployConfigAdmin() {
         return true;
     }
 
     /**
      * Common test options.
      */
     @Override
     protected Option[] getCustomOptions() {
        // Because sometime we need more time...
        System.setProperty("TIME_FACTOR", "2");

         return options(
                 // Enable iPOJO synchronous mode
                 systemProperty("ipojo.processing.synchronous").value("true"),
                 // Enable everest synchronous event processing + debug of requests
                 systemProperty("everest.processing.synchronous").value("true"),
                 systemProperty("everest.debug.request").value("true"),
                 // everest bundles
                 mavenBundle(IPOJO, "everest-core").versionAsInProject(),
                 mavenBundle(IPOJO, "everest-ipojo").versionAsInProject(),
                 mavenBundle(IPOJO, "everest-osgi").versionAsInProject(),
                 // The EventAdmin service
                 mavenBundle("org.apache.felix", "org.apache.felix.eventadmin").versionAsInProject(),
                 // Generated test bundles
                 generateTestBundle(TEST_BUNDLE_SYMBOLIC_NAME, IPOJO + ".everest.ipojo.test.b1", "metadata.1.xml", null),
                 generateTestBundle(TEST_BUNDLE_2_SYMBOLIC_NAME, IPOJO + ".everest.ipojo.test.b2", "metadata.2.xml", null),
                 // Fest assert JARs wrapped as bundles
                 wrappedBundle(mavenBundle("org.easytesting", "fest-util").versionAsInProject()),
                 wrappedBundle(mavenBundle("org.easytesting", "fest-assert").versionAsInProject())
         );
     }
 
     /**
      * Common test setup.
      */
     @Before
     public void commonSetUp() {
         super.commonSetUp();
         // Get the interesting bundles
         ipojoBundle = osgiHelper.getBundle(IPOJO);
         testBundle = osgiHelper.getBundle(TEST_BUNDLE_SYMBOLIC_NAME);
         testBundle2 = osgiHelper.getBundle(TEST_BUNDLE_2_SYMBOLIC_NAME);
         // Register an EventHandler for debug
         Dictionary<String, String> props = new Hashtable<String, String>();
         props.put(EventConstants.EVENT_TOPIC, "everest/ipojo/*");
         eventHandlerService = context.registerService(EventHandler.class, new EventHandler() {
             public void handleEvent(Event event) {
                 System.out.println("Event " + event.getProperty("eventType") + " on path " + event.getProperty("canonicalPath"));
             }
         }, props);
     }
 
     /**
      * Common test tear down.
      */
     @Override
     public void commonTearDown() {
         eventHandlerService.unregister();
         super.commonTearDown();
     }
 
     /**
      * Generates a test bundle with the specified symbolic name.
      * <p>
      * The generated bundle contains all the classes and resources in the specified package name, and its sub-packages.
      * </p>
      *
      * @param symbolicName symbolic name of the bundle to generate
      * @param packageName  name of the package to include in the bundle
      * @param metadataXml  the name of the iPOJO metadata XML file to use, may be {@code null}
      * @param headers      additional bundle headers
      * @return the generated bundle
      */
     public static Option generateTestBundle(String symbolicName, String packageName, String metadataXml, Map<String, String> headers) {
         TinyBundle bundle = TinyBundles.bundle();
 
         // We look inside target/classes/$packageName to find the class and resources
         File classesDir = new File("target/classes/");
         File packageDir = new File(classesDir, packageName.replace('.', '/'));
         Collection<File> classes = FileUtils.listFiles(packageDir, null, true);
 
         // Add all classes and resources to the tiny bundle
         int index = classesDir.getAbsolutePath().length() + 1;
         for (File clazz : classes) {
             String relativePath = clazz.getAbsolutePath().substring(index);
             try {
                 bundle.add(relativePath, clazz.toURI().toURL());
             } catch (MalformedURLException e) {
                 throw new RuntimeException("cannot add resource/class: " + relativePath, e);
             }
         }
 
         // Add the bundle headers and generate the stream
         if (headers != null) {
             for (Entry<String, String> header : headers.entrySet()) {
                 bundle.set(header.getKey(), header.getValue());
             }
         }
         File metadata;
         if (metadataXml != null) {
             metadata = new File("src/main/resources/" + metadataXml);
         } else {
             metadata = null;
         }
         InputStream stream = bundle
                 .set(Constants.BUNDLE_SYMBOLICNAME, symbolicName)
                 .set(Constants.IMPORT_PACKAGE, "*")
                 .set(Constants.EXPORT_PACKAGE, packageName + ", " + packageName + ".*")
                 .build(IPOJOStrategy.withiPOJO(metadata));
 
         // Write the bundle and return its URL
         File output = new File("target/tested/" + symbolicName + ".jar");
         try {
             FileUtils.copyInputStreamToFile(stream, output);
             return bundle(output.toURI().toURL().toExternalForm());
         } catch (IOException e) {
             throw new RuntimeException("cannot write generated bundle: " + output.getAbsolutePath(), e);
         }
     }
 
     /**
      * Read the resource at the given path.
      *
      * @param path the path of the resource
      * @return the read resource
      * @throws ResourceNotFoundException if there is no resource at the given path
      * @throws IllegalActionOnResourceException
      *                                   if reading this resource is an illegal action
      */
     public Resource read(String path) throws ResourceNotFoundException, IllegalActionOnResourceException {
         return everest.process(new DefaultRequest(Action.READ, Path.from(path), null));
     }
 
     /**
      * Check that the EverestService service is present.
      * <p>
      * This test also avoids the test container to complain with a "no test method" error.
      * </p>
      */
     @Test
     public void testEverestServiceIsPresent() {
         assertNotNull(everest);
     }
 
     // UTILITY METHODS
 
     public Factory getFactory(String name, String version) {
         ServiceReference<Factory> ref = getFactoryReference(name, version);
         if (ref == null) {
             throw new AssertionError("no factory service with same name/version: " + name + "/" + version);
         }
         return context.getService(ref);
     }
 
     public HandlerFactory getHandlerFactory(String namespace, String name) {
         ServiceReference<HandlerFactory> ref = getHandlerFactoryReference(namespace, name);
         if (ref == null) {
             throw new AssertionError("no handler factory service with same namespace/name: " + namespace + "/" + name);
         }
         return context.getService(ref);
     }
 
     public Architecture getArchitecture(String instance) {
         ServiceReference<Architecture> ref = getArchitectureReference(instance);
         if (ref == null) {
             throw new AssertionError("no architecture service with same instance name: " + instance);
         }
         return context.getService(ref);
     }
 
     public TypeDeclaration getTypeDeclaration(String name, String version) {
         ServiceReference<TypeDeclaration> ref = getTypeDeclarationReference(name, version);
         if (ref == null) {
             throw new AssertionError("no type declaration service with same name/version: " + name + "/" + String.valueOf(version));
         }
         return context.getService(ref);
     }
 
     public ServiceReference<Factory> getFactoryReference(String name, String version) {
         // Scientifically build the selection filter.
         String filter = "(&(factory.name=" + name + ")";
         if (version != null) {
             filter += "(factory.version=" + version + ")";
         } else {
             filter += "(!(factory.version=*))";
         }
         filter += ")";
         Collection<ServiceReference<Factory>> refs;
         try {
             refs = context.getServiceReferences(Factory.class, filter);
         } catch (InvalidSyntaxException e) {
             // Should never happen!
             throw new AssertionError(e);
         }
         if (refs.isEmpty()) {
             return null;
         } else if (refs.size() > 1) {
             // Should never happen!
             throw new AssertionError("multiple factory service with same name/version: " + name + "/" + version);
         }
         return refs.iterator().next();
     }
 
     public ServiceReference<HandlerFactory> getHandlerFactoryReference(String namespace, String name) {
         // Scientifically build the selection filter.
         String filter = "(&(handler.namespace=" + namespace + ")(handler.name=" + name +"))";
         Collection<ServiceReference<HandlerFactory>> refs;
         try {
             refs = context.getServiceReferences(HandlerFactory.class, filter);
         } catch (InvalidSyntaxException e) {
             // Should never happen!
             throw new AssertionError(e);
         }
         if (refs.isEmpty()) {
             return null;
         } else if (refs.size() > 1) {
             // Should never happen!
             throw new AssertionError("multiple factory service with same namespace/name: " + namespace + "/" + name);
         }
         return refs.iterator().next();
     }
 
     public ServiceReference<Architecture> getArchitectureReference(String instance) {
         // Scientifically build the selection filter.
         String filter = "(architecture.instance=" + instance + ")";
         Collection<ServiceReference<Architecture>> refs;
         try {
             refs = context.getServiceReferences(Architecture.class, filter);
         } catch (InvalidSyntaxException e) {
             // Should never happen!
             throw new AssertionError(e);
         }
         if (refs.isEmpty()) {
             return null;
         } else if (refs.size() > 1) {
             // Should never happen!
             throw new AssertionError("multiple architecture service with same instance: " + instance);
         }
         return refs.iterator().next();
     }
 
     public ServiceReference<ExtensionDeclaration> getExtensionDeclarationReference(String extension) {
         // Get ALL the ExtensionDeclaration services
         Collection<ServiceReference<ExtensionDeclaration>> refs;
         try {
             refs = context.getServiceReferences(ExtensionDeclaration.class, null);
         } catch (InvalidSyntaxException e) {
             // Should never... NEVER happen!
             throw new AssertionError(e);
         }
         for (ServiceReference<ExtensionDeclaration> ref : refs) {
             ExtensionDeclaration e = context.getService(ref);
             try {
                 if (extension.equals(e.getExtensionName())) {
                     return ref;
                 }
             } finally {
                 context.ungetService(ref);
             }
         }
         return null;
     }
 
     public ServiceReference<TypeDeclaration> getTypeDeclarationReference(String name, String version) {
         // Get ALL the ExtensionDeclaration services
         Collection<ServiceReference<TypeDeclaration>> refs;
         try {
             refs = context.getServiceReferences(TypeDeclaration.class, null);
         } catch (InvalidSyntaxException e) {
             // Should never... NEVER happen!
             throw new AssertionError(e);
         }
         for (ServiceReference<TypeDeclaration> ref : refs) {
             TypeDeclaration t = context.getService(ref);
             try {
                 if (name.equals(t.getComponentName())) {
                     String v = t.getComponentVersion();
                     if (version == null && v == null || version != null && version.equals(v)) {
                         return ref;
                     }
                 }
             } finally {
                 context.ungetService(ref);
             }
         }
         return null;
     }
 
     // HACKs
 
     // WARN: this is a hack!!!
     public ComponentInstance getComponentInstance(String instance) {
         Architecture arch = getArchitecture(instance);
         InstanceDescription desc = arch.getInstanceDescription();
         Field shunt;
         try {
             shunt = InstanceDescription.class.getDeclaredField("m_instance");
         } catch (NoSuchFieldException e) {
             // Should never happen!
             throw new AssertionError(e);
         }
         shunt.setAccessible(true);
         try {
             return (ComponentInstance) shunt.get(desc);
         } catch (IllegalAccessException e) {
             // Should never happen!
             throw new AssertionError(e);
         } finally {
             shunt.setAccessible(false);
         }
     }
 
     // WARN: this is a hack!!!
     private boolean killInstance(String name) throws InvalidSyntaxException, NoSuchFieldException, IllegalAccessException {
         ComponentInstance instance = getComponentInstance(name);
         if (instance == null) {
             return false;
         }
         // FATALITY!!!
         instance.dispose();
         return true;
     }
 
     // WARN: this is a hack!!!
     private boolean killFactory(String name, String version) throws InvalidSyntaxException {
         Factory factory = getFactory(name, version);
         if (factory == null) {
             return false;
         }
 
         IPojoFactory f = (IPojoFactory) factory;
         Method weapon = null;
         try {
             weapon = IPojoFactory.class.getDeclaredMethod("dispose");
             weapon.setAccessible(true);
             // FATALITY!!!
             weapon.invoke(f);
         } catch (Exception e) {
             throw new IllegalStateException("cannot kill factory", e);
         } finally {
             // It's a bad idea to let kids play with such a weapon...
             if (weapon != null) {
                 weapon.setAccessible(false);
             }
         }
         return true;
     }
 
 }
