 package org.easysoa.discovery.code.handler;
 
 import com.thoughtworks.qdox.model.DocletTag;
 import java.net.URL;
 import java.net.URLClassLoader;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.jar.JarEntry;
 import java.util.jar.JarFile;
 
 import org.apache.maven.artifact.Artifact;
 import org.apache.maven.plugin.logging.Log;
 import org.apache.maven.project.MavenProject;
 import org.easysoa.discovery.code.CodeDiscoveryMojo;
 import org.easysoa.discovery.code.CodeDiscoveryRegistryClient;
 import org.easysoa.discovery.code.ParsingUtils;
 import org.easysoa.discovery.code.handler.consumption.AnnotatedServicesConsumptionFinder;
 import org.easysoa.discovery.code.handler.consumption.ImportedServicesConsumptionFinder;
 import org.easysoa.discovery.code.handler.consumption.ServiceConsumptionFinder;
 import org.easysoa.discovery.code.model.JavaServiceInterfaceInformation;
 import org.easysoa.registry.rest.client.types.java.MavenDeliverableInformation;
 import org.easysoa.registry.rest.marshalling.SoaNodeInformation;
 
 import com.thoughtworks.qdox.model.JavaClass;
 import com.thoughtworks.qdox.model.JavaMethod;
 import com.thoughtworks.qdox.model.JavaParameter;
 import com.thoughtworks.qdox.model.JavaSource;
 import com.thoughtworks.qdox.model.Type;
 import net.oauth.OAuth.Parameter;
 
 
 /**
  * Provides Java-generic discovery features for interfaces, for now only
  * detection of Java interface-typed injected (through various methods) members.
  *
  * This provides only potential, coarse grain (among a set of deployed deliverables,
  * any service provider implementation MAY use any service consumer implementation) &
  * partial (services may also be consumed after being looked up in a registry, not
  * only through member injection) dependencies between services.
  *
  * These dependencies should be refined by providing EasySOA with explicit
  * architecture information about architectural components (classified deliverable
  * may already be a good guide there) and business processes.
  *
  * LATER Thoughts about more injection strategies :
  * recursive (including native Java services being injected in one another, but adds complexity),
  * imports (but qdox can't), non-null assigned variable (but requires almost being runtime)
  *
  * @author mdutoo
  *
  */
 public abstract class AbstractJavaSourceHandler implements SourcesHandler {
 
     /* jaxb annotations */
     protected static final String ANN_XMLROOTELEMENT = " javax.xml.bind.annotation.XmlRootElement";
 
     /* injection annotations */
     protected static final String ANN_INJECT = "javax.inject.Inject";
 
     /* test annotations */
     protected static final String ANN_JUNIT_TEST = "org.junit.Test";
     protected static final String ANN_TESTNG_TEST = "org.testng.annotations.Test"; // http://testng.org/javadoc/org/testng/annotations/package-summary.html
 
     private List<ServiceConsumptionFinder> serviceConsumptionFinders = new ArrayList<ServiceConsumptionFinder>();
 
     private AnnotatedServicesConsumptionFinder annotatedServicesFinder;
 
     protected CodeDiscoveryMojo codeDiscovery;
 
     /**
      *
      * @param codeDiscovery provides access to maven plugin configuration params
      */
     protected AbstractJavaSourceHandler(CodeDiscoveryMojo codeDiscovery) {
         this.codeDiscovery = codeDiscovery;
 
         this.annotatedServicesFinder = new AnnotatedServicesConsumptionFinder(null);
         this.annotatedServicesFinder.addAnnotationToDetect(ANN_INJECT); // Java 6
         this.annotatedServicesFinder.addAnnotationToDetect("org.osoa.sca.annotations.Reference");
         this.annotatedServicesFinder.addAnnotationToDetect("org.springframework.beans.factory.annotation.Autowired");
         this.annotatedServicesFinder.addAnnotationToDetect("com.google.inject.Inject");
         this.annotatedServicesFinder.addAnnotationToDetect("javax.ejb.EJB");
         this.serviceConsumptionFinders.add(this.annotatedServicesFinder);
         this.serviceConsumptionFinders.add(new ImportedServicesConsumptionFinder());
     }
 
     protected void addAnnotationToDetect(String annotationToDetect) {
         annotatedServicesFinder.addAnnotationToDetect(annotationToDetect);
     }
 
     protected JavaClass getWsItf(JavaClass c, Map<String, JavaServiceInterfaceInformation> serviceInterfaces) {
         for (JavaClass itfClass : c.getImplementedInterfaces()) {
             String itfClassName = itfClass.getFullyQualifiedName();
             if (!itfClassName.contains(".")) {
                 itfClassName = c.getPackageName() + "." + itfClassName;
                 itfClass.setName(itfClassName);
             }
             if (serviceInterfaces.containsKey(itfClassName)) {
                 return itfClass;
             }
         }
         return null;
     }
 
     public Collection<SoaNodeInformation> handleSources(JavaSource[] sources,
             MavenDeliverableInformation mavenDeliverable,
             CodeDiscoveryRegistryClient registryClient, Log log) throws Exception {
         List<SoaNodeInformation> discoveredNodes = new LinkedList<SoaNodeInformation>();
 
         // Find WS interfaces in sources
         Map<String, JavaServiceInterfaceInformation> wsInterfaces =
                 new HashMap<String, JavaServiceInterfaceInformation>();
         for (JavaSource source : sources) {
             wsInterfaces.putAll(findWSInterfaces(source, mavenDeliverable, registryClient, log));
         }
 
         // Find WS interfaces from dependencies
         if (codeDiscovery.getMavenProject() != null) { // may be null in unit tests
             MavenProject mavenProject = codeDiscovery.getMavenProject();
             for (Object dependencyObject : mavenProject.getDependencyArtifacts()) {
                 Artifact dependency = (Artifact) dependencyObject;
                 if (codeDiscovery.shouldLookupInterfaces(dependency)
                 		&& dependency.getFile() != null) { // happens in pom projects (but should be skipped before)
                     URLClassLoader jarClassloader = new URLClassLoader(
                             new URL[] { dependency.getFile().toURI().toURL() },
                             this.getClass().getClassLoader());
                     JarFile jarFile = new JarFile(dependency.getFile());
 
                     Enumeration<JarEntry> jarEntries = jarFile.entries();
                     while (jarEntries.hasMoreElements()) {
                         JarEntry element = jarEntries.nextElement();
                         if (element.getName().endsWith(".class")) {
                             String className = element.getName().replace(".class", "").replaceAll("/", "\\.");
                             try {
                                 Class<?> candidateClass = jarClassloader.loadClass(className);
 
                                 JavaServiceInterfaceInformation newWSInterface =
                                         findWSInterfaceInClasspath(candidateClass,
                                         new MavenDeliverableInformation(dependency.getGroupId(), dependency.getArtifactId()),
                                         registryClient, log);
 
                                 if (newWSInterface != null) {
                                     wsInterfaces.put(newWSInterface.getInterfaceName(), newWSInterface);
                                 }
                             } catch (Throwable e) {
                                 log.warn("Failed to load class " + className + " to inspect potiential web services: "
                                         + e.getClass().getName() + " - " + e.getMessage());
                             }
                         }
                     }
 
                 }
              }
         }
 
         // Find WS implementations
         Collection<SoaNodeInformation> wsImpls = findWSImplementations(sources,
                 wsInterfaces, mavenDeliverable, registryClient, log);
         discoveredNodes.addAll(wsImpls);
 
         // Find WS consumptions
         for (JavaSource source : sources) {
             for (ServiceConsumptionFinder serviceConsumptionFinder : this.serviceConsumptionFinders) {
                 discoveredNodes.addAll(serviceConsumptionFinder.find(source, mavenDeliverable, wsInterfaces));
             }
         }
 
         // Additional discovery
         discoveredNodes.addAll(handleAdditionalDiscovery(sources, wsInterfaces,
                 mavenDeliverable, registryClient, log));
 
         return discoveredNodes;
     }
 
 
     protected String formatParameter(String parameterName, String parameterType) {
         return parameterName + "=" + parameterType;
     }
 
     protected boolean isUnitTestingClass(boolean isUnitTestingClass, JavaMethod method) {
         return isUnitTestingClass
                 || ParsingUtils.hasAnnotation(method, ANN_JUNIT_TEST)
                 || ParsingUtils.hasAnnotation(method, ANN_TESTNG_TEST); // A TestNG class is a Java class that contains at least one TestNG annotation http://testng.org/doc/documentation-main.html
     }
 
     protected String getParameterType(Type parameterType) {
         String webParameterType = ParsingUtils.getAnnotationPropertyString(parameterType.getJavaClass(), ANN_XMLROOTELEMENT, "name");//TODO ??
         if (webParameterType == null) {
             webParameterType = parameterType.getFullyQualifiedName();
         }
         return webParameterType;
     }
 
     protected String getReturnParameterType(JavaMethod method) {
         String returnParameterType;
         Type returnType = method.getReturnType(); // until qdox 1.12 procedure return type was null so nullpointerexception http://jira.codehaus.org/browse/QDOX-214
         if (returnType == null) {
             // complementary patch to qdox 1.12.1
             returnType = Type.VOID; // would still nullpointerexception in getParameterType because no javaClassName
             returnParameterType = returnType.toString(); // NB. a void return returns an empty element
         } else {
             returnParameterType = getParameterType(returnType);
         }
         return returnParameterType;
     }
 
     protected String getParameterType(Class<?> parameterType) {
         String webParameterType = ParsingUtils.getAnnotationPropertyString(parameterType, ANN_XMLROOTELEMENT, "name");
         if (webParameterType == null) {
             webParameterType = parameterType.getName();
         }
         return webParameterType;
     }
 
 
     public abstract Map<String, JavaServiceInterfaceInformation> findWSInterfaces(JavaSource source,
             MavenDeliverableInformation mavenDeliverable,
             CodeDiscoveryRegistryClient registryClient, Log log) throws Exception;
 
     public abstract JavaServiceInterfaceInformation findWSInterfaceInClasspath(Class<?> candidateClass,
             MavenDeliverableInformation mavenDeliverable,
             CodeDiscoveryRegistryClient registryClient, Log log) throws Exception;
 
     public abstract Collection<SoaNodeInformation> findWSImplementations(JavaSource[] sources,
             Map<String, JavaServiceInterfaceInformation> wsInterfaces, MavenDeliverableInformation mavenDeliverable,
             CodeDiscoveryRegistryClient registryClient, Log log) throws Exception;
 
     public Collection<SoaNodeInformation> handleAdditionalDiscovery(JavaSource[] sources,
             Map<String, JavaServiceInterfaceInformation> wsInterfaces,
             MavenDeliverableInformation mavenDeliverable,
             CodeDiscoveryRegistryClient registryClient, Log log) throws Exception {
         return Collections.emptyList();
     }
 
     /**
      * Format the class documentation for Nuxeo registry, especially by adding doclets (eg : @author) ommitted by qdox
      * @param itfClass Java class
      * @return formatted comment bloc with doctlets tags
      */
     protected String formatDoc(JavaClass itfClass) {
         StringBuilder formattedDoc = new StringBuilder();
         if(itfClass.getComment() != null){
             formattedDoc.append(itfClass.getComment());
         }
         for(DocletTag tag : itfClass.getTags()){
             if(formattedDoc.length() > 0){
                 formattedDoc.append("\n");
             }
             formattedDoc.append("@");
             formattedDoc.append(tag.getName());
             formattedDoc.append(" ");
             formattedDoc.append(tag.getValue());
         }
         return formattedDoc.toString();
     }
 
     /**
      * Format the method documentation for Nuxeo registry, especially by adding doclets (eg : @param) ommitted by qdox
      * @param method Java method
      * @return formatted comment bloc with doctlets tags
      */
     protected String formatDoc(JavaMethod method) {
         StringBuilder formattedDoc = new StringBuilder();
         if(method.getComment() != null){
             formattedDoc.append(method.getComment());
         }
        for(JavaParameter param : method.getParameters()){
             if(formattedDoc.length() > 0){
                 formattedDoc.append("\n");
             }
            formattedDoc.append("@param ");
            formattedDoc.append(param.getName());
         }
         return formattedDoc.toString();
     }
 
 }
