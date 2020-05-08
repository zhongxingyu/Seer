 /*
  * Copyright 2005-2006 The Apache Software Foundation.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.apache.servicemix.jbi.framework;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.StringReader;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import javax.jbi.JBIException;
 import javax.jbi.component.ServiceUnitManager;
 import javax.jbi.management.DeploymentException;
 import javax.jbi.management.DeploymentServiceMBean;
 import javax.management.JMException;
 import javax.management.MBeanAttributeInfo;
 import javax.management.MBeanOperationInfo;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.servicemix.jbi.container.EnvironmentContext;
 import org.apache.servicemix.jbi.container.JBIContainer;
 import org.apache.servicemix.jbi.deployment.Descriptor;
 import org.apache.servicemix.jbi.deployment.DescriptorFactory;
 import org.apache.servicemix.jbi.deployment.ServiceAssembly;
 import org.apache.servicemix.jbi.deployment.ServiceUnit;
 import org.apache.servicemix.jbi.management.AttributeInfoHelper;
 import org.apache.servicemix.jbi.management.BaseSystemService;
 import org.apache.servicemix.jbi.management.OperationInfoHelper;
 import org.apache.servicemix.jbi.management.ParameterHelper;
 import org.apache.servicemix.jbi.util.DOMUtil;
 import org.apache.servicemix.jbi.util.FileUtil;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NodeList;
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 
 /**
  * The deployment service MBean allows administrative tools to manage service assembly deployments.
  * 
  * @version $Revision$
  */
 public class DeploymentService extends BaseSystemService implements DeploymentServiceMBean {
     
     private static final Log log = LogFactory.getLog(DeploymentService.class);
     private EnvironmentContext environmentContext;
     private Registry registry;
     
     //
     // ServiceMix service implementation
     //
 
     /**
      * Initialize the Service
      * 
      * @param container
      * @throws JBIException 
      * @throws DeploymentException
      */
     public void init(JBIContainer container) throws JBIException {
         this.environmentContext = container.getEnvironmentContext();
         this.registry = container.getRegistry();
         super.init(container);
         buildState();
     }
     
     protected Class getServiceMBean() {
         return DeploymentServiceMBean.class;
     }
 
     public void start() throws javax.jbi.JBIException {
         super.start();
         String[] sas = registry.getDeployedServiceAssemblies();
         for (int i = 0; i < sas.length; i++) {
             try {
                 ServiceAssemblyLifeCycle sa = registry.getServiceAssembly(sas[i]);
                 sa.restore();
             } catch (Exception e) {
                 log.error("Unable to restore state for service assembly " + sas[i], e);
             }
         }
     }
     
     /**
      * Get an array of MBeanAttributeInfo
      * 
      * @return array of AttributeInfos
      * @throws JMException
      */
     public MBeanAttributeInfo[] getAttributeInfos() throws JMException {
         AttributeInfoHelper helper = new AttributeInfoHelper();
         helper.addAttribute(getObjectToManage(), "deployedServiceAssemblies", "list of deployed SAs");
         return AttributeInfoHelper.join(super.getAttributeInfos(), helper.getAttributeInfos());
     }
 
     /**
      * Get an array of MBeanOperationInfo
      * 
      * @return array of OperationInfos
      * @throws JMException
      */
     public MBeanOperationInfo[] getOperationInfos() throws JMException {
         OperationInfoHelper helper = new OperationInfoHelper();
         ParameterHelper ph = helper.addOperation(getObjectToManage(), "deploy", 1, "deploy An SA");
         ph.setDescription(0, "saZipURL", "location of SA zip file");
         ph = helper.addOperation(getObjectToManage(), "undeploy", 1, "undeploy An SA");
         ph.setDescription(0, "saName", "SA name");
         ph = helper.addOperation(getObjectToManage(), "getDeployedServiceUnitList", 1,
                 "list of SU's currently deployed");
         ph.setDescription(0, "componentName", "Component name");
         ph = helper.addOperation(getObjectToManage(), "getServiceAssemblyDescriptor", 1, "Get descriptor for a SA");
         ph.setDescription(0, "saName", "SA name");
         ph = helper.addOperation(getObjectToManage(), "getDeployedServiceAssembliesForComponent", 1,
                 "list of SA's for a Component");
         ph.setDescription(0, "componentName", "Component name");
         ph = helper.addOperation(getObjectToManage(), "getComponentsForDeployedServiceAssembly", 1,
                 "list of Components  for a SA");
         ph.setDescription(0, "saName", "SA name");
         ph = helper.addOperation(getObjectToManage(), "isDeployedServiceUnit", 2, "is SU deployed at a Component ?");
         ph.setDescription(0, "componentName", "Component name");
         ph.setDescription(1, "suName", "SU name");
         ph = helper
                 .addOperation(getObjectToManage(), "canDeployToComponent", 1, "Can a SU be deployed to a Component?");
         ph.setDescription(0, "componentName", "Component name");
         ph = helper.addOperation(getObjectToManage(), "start", 1, "start an SA");
         ph.setDescription(0, "saName", "SA name");
         ph = helper.addOperation(getObjectToManage(), "stop", 1, "stop an SA");
         ph.setDescription(0, "saName", "SA name");
         ph = helper.addOperation(getObjectToManage(), "shutDown", 1, "shutDown an SA");
         ph.setDescription(0, "saName", "SA name");
         ph = helper.addOperation(getObjectToManage(), "getState", 1, "Running state of an SA");
         ph.setDescription(0, "saName", "SA name");
         return OperationInfoHelper.join(super.getOperationInfos(), helper.getOperationInfos());
     }
     /**
      * Get the description
      * 
      * @return description
      */
     public String getDescription() {
         return "Allows admin tools to manage service deployments";
     }
 
     //
     // DeploymentServiceMBean implementation
     //
 
     /**
      * Deploys the given SA to the JBI environment.
      * 
      * @param saZipURL String containing the location of the Service Assembly zip file.
      * @return Result/Status of the SA deployment in xml format.
      * @throws Exception in xml format if complete deployment fails.
      */
     public String deploy(String saZipURL) throws Exception {
         try {
             if (saZipURL == null) {
                 throw ManagementSupport.failure("deploy", "saZipURL must not be null");
             }
             File tmpDir = null;
             try {
                 tmpDir = AutoDeploymentService.unpackLocation(environmentContext.getTmpDir(), saZipURL);
             } catch (Exception e) {
                 throw ManagementSupport.failure("deploy", "Unable to unpack archive: " + saZipURL, e);
             }
             // unpackLocation returns null if no jbi descriptor is found
             if (tmpDir == null) {
                 throw ManagementSupport.failure("deploy", "Unable to find jbi descriptor: " + saZipURL);
             }
             Descriptor root = null;
             try {
                 root = DescriptorFactory.buildDescriptor(tmpDir);
             } catch (Exception e) {
                 throw ManagementSupport.failure("deploy", "Unable to build jbi descriptor: " + saZipURL, e);
             }
             if (root == null) {
                 throw ManagementSupport.failure("deploy", "Unable to find jbi descriptor: " + saZipURL);
             }
             ServiceAssembly sa = root.getServiceAssembly();
             if (sa == null) {
                 throw ManagementSupport.failure("deploy", "JBI descriptor is not an assembly descriptor: " + saZipURL);
             }
             return deployServiceAssembly(tmpDir, sa);
         } catch (Exception e) {
             log.error("Error deploying service assembly", e);
             throw e;
         }
     }
 
     /**
      * Undeploys the given SA from the JBI environment.
      * 
      * @param saName name of the SA that has to be undeployed.
      * @return Result/Status of the SA undeployment.
      * @throws Exception if compelete undeployment fails.
      */
     public String undeploy(String saName) throws Exception {
         if (saName == null) {
             throw ManagementSupport.failure("undeploy", "SA name must not be null");
         }
         ServiceAssemblyLifeCycle sa = registry.getServiceAssembly(saName);
         if (sa == null) {
             throw ManagementSupport.failure("undeploy", "SA has not been deployed: " + saName);
         }
         String state = sa.getCurrentState();
         if (!DeploymentServiceMBean.SHUTDOWN.equals(state)) {
             throw ManagementSupport.failure("undeploy", "SA must be shut down: " + saName);
         }
         try {
             // Make sure the all service units in the assembly are shutdown.
             // SUs can have different states (if a previous shutdown failed).
             try {
                 sa.shutDown();
             } catch (Exception e) {
             }
             String result = null;
             if (sa != null) {
                 String assemblyName = sa.getName();
                 registry.unregisterServiceAssembly(assemblyName);
                 File saDirectory = environmentContext.getSARootDirectory(assemblyName);
                 ServiceUnitLifeCycle[] sus = sa.getDeployedSUs();
                 if (sus != null) {
                     for (int i = 0;i < sus.length; i++) {
                         undeployServiceUnit(sus[i]);
                     }
                 }
                 FileUtil.deleteFile(saDirectory);
             }
             return result;
         } catch (Exception e) {
             log.info("Unable to undeploy assembly", e);
             throw e;
         }
     }
 
     /**
      * Returns a list of Service Units that are currently deployed to the given component.
      * 
      * @param componentName name of the component.
      * @return List of deployed ASA Ids.
      */
     public String[] getDeployedServiceUnitList(String componentName) throws Exception {
         try {
             ServiceUnitLifeCycle[] sus = registry.getDeployedServiceUnits(componentName);
             String[] names = new String[sus.length];
             for (int i = 0; i < names.length; i++) {
                 names[i] = sus[i].getName();
             }
             return names;
         } catch (Exception e) {
             log.info("Unable to get deployed service unit list", e);
             throw e;
         }
     }
 
     /**
      * Returns a list of Service Assemblies deployed to the JBI enviroment.
      * 
      * @return list of Service Assembly Name's.
      */
     public String[] getDeployedServiceAssemblies() throws Exception {
         try {
             return registry.getDeployedServiceAssemblies();
         } catch (Exception e) {
             log.info("Unable to get deployed service assemblies", e);
             throw e;
         }
     }
 
     /**
      * Returns the descriptor of the Service Assembly that was deployed to the JBI enviroment.
      * 
      * @param saName name of the service assembly.
      * @return descriptor of the Service Assembly.
      */
     public String getServiceAssemblyDescriptor(String saName) throws Exception {
         ServiceAssemblyLifeCycle sa = registry.getServiceAssembly(saName);
         if (sa != null) {
             return sa.getDescriptor();
         } else {
             return null;
         }
     }
 
     /**
      * Returns a list of Service Assemblies that contain SUs for the given component.
      * 
      * @param componentName name of the component.
      * @return list of Service Assembly names.
      * @throws Exception if unable to retrieve service assembly list.
      */
     public String[] getDeployedServiceAssembliesForComponent(String componentName) throws Exception {
         try {
             return registry.getDeployedServiceAssembliesForComponent(componentName);
         } catch (Exception e) {
             log.info("Error in getDeployedServiceAssembliesForComponent", e);
             throw e;
         }
     }
 
     /**
      * Returns a list of components(to which SUs are targeted for) in a Service Assembly.
      * 
      * @param saName name of the service assembly.
      * @return list of component names.
      * @throws Exception if unable to retrieve component list.
      */
     public String[] getComponentsForDeployedServiceAssembly(String saName) throws Exception {
         try {
             return registry.getComponentsForDeployedServiceAssembly(saName);
         } catch (Exception e) {
             log.info("Error in getComponentsForDeployedServiceAssembly", e);
             throw e;
         }
     }
 
     /**
      * Returns a boolean value indicating whether the SU is currently deployed.
      * 
      * @param componentName - name of component.
      * @param suName - name of the Service Unit.
      * @return boolean value indicating whether the SU is currently deployed.
      * @throws Exception if unable to return status of service unit.
      */
     public boolean isDeployedServiceUnit(String componentName, String suName) throws Exception {
         try {
             return registry.isSADeployedServiceUnit(componentName, suName);
         } catch (Exception e) {
             log.info("Error in isSADeployedServiceUnit", e);
             throw e;
         }
     }
 
     /**
      * Returns a boolean value indicating whether the SU can be deployed to a component.
      * 
      * @param componentName - name of the component.
      * @return boolean value indicating whether the SU can be deployed.
      */
     public boolean canDeployToComponent(String componentName) {
         ComponentMBeanImpl lcc = container.getComponent(componentName);
         return lcc != null && lcc.isStarted() && lcc.getServiceUnitManager() != null;
     }
 
     /**
      * Starts the service assembly and puts it in STARTED state.
      * 
      * @param serviceAssemblyName - name of the service assembly.
      * @return Result/Status of this operation.
      * @throws Exception if operation fails.
      */
     public String start(String serviceAssemblyName) throws Exception {
         try {
             ServiceAssemblyLifeCycle sa = registry.getServiceAssembly(serviceAssemblyName);
             return sa.start(true);
         } catch (Exception e) {
             log.info("Error in start", e);
             throw e;
         }
     }
     
     
 
     /**
      * Stops the service assembly and puts it in STOPPED state.
      * 
      * @param serviceAssemblyName - name of the service assembly.
      * @return Result/Status of this operation.
      * @throws Exception if operation fails.
      */
     public String stop(String serviceAssemblyName) throws Exception {
         try {
             ServiceAssemblyLifeCycle sa = registry.getServiceAssembly(serviceAssemblyName);
             return sa.stop(true, false);
         } catch (Exception e) {
             log.info("Error in stop", e);
             throw e;
         }
     }
 
     /**
      * Shutdown the service assembly and puts it in SHUTDOWN state.
      * 
      * @param serviceAssemblyName - name of the service assembly.
      * @return Result/Status of this operation.
      * @throws Exception if operation fails.
      */
     public String shutDown(String serviceAssemblyName) throws Exception {
         try {
             ServiceAssemblyLifeCycle sa = registry.getServiceAssembly(serviceAssemblyName);
             return sa.shutDown(true);
         } catch (Exception e) {
             log.info("Error in shutDown", e);
             throw e;
         }
     }
 
     /**
      * Returns the state of service assembly.
      * 
      * @param serviceAssemblyName - name of the service assembly.
      * @return State of the service assembly.
      * @throws Exception if operation fails.
      */
     public String getState(String serviceAssemblyName) throws Exception {
         try {
             ServiceAssemblyLifeCycle sa = registry.getServiceAssembly(serviceAssemblyName);
             return sa.getCurrentState();
         } catch (Exception e) {
             log.info("Error in getState", e);
             throw e;
         }
     }
 
     /**
      * See if an Sa is already deployed
      * 
      * @param serviceAssemblyName - name of the service assembly.
      * @return true if already deployed
      */
     protected boolean isSaDeployed(String serviceAssemblyName) {
         return registry.getServiceAssembly(serviceAssemblyName) != null;
     }
 
     /**
      * Deploy an SA
      * 
      * @param tmpDir
      * @param sa
      * @return result/status of the deployment in xml format
      * @throws Exception  in xml format
      */
     protected String deployServiceAssembly(File tmpDir, ServiceAssembly sa) throws Exception {
         String assemblyName = sa.getIdentification().getName();
         File oldSaDirectory = environmentContext.getSARootDirectory(assemblyName);
         FileUtil.deleteFile(oldSaDirectory);
         File saDirectory = environmentContext.createSARootDirectory(assemblyName);
 
         // move the assembly to a well-named holding area
         if (log.isDebugEnabled()) {
             log.debug("Moving " + tmpDir.getAbsolutePath() + " to " + saDirectory.getAbsolutePath());
         }
         if (!tmpDir.renameTo(saDirectory)) {
             throw ManagementSupport.failure("deploy", "Failed to rename " + tmpDir + " to " + saDirectory);
         }
         // Check all SUs requirements
         ServiceUnit[] sus = sa.getServiceUnits();
         if (sus != null) {
             for (int i = 0; i < sus.length; i++) {
                 String suName = sus[i].getIdentification().getName();
                 String artifact = sus[i].getTarget().getArtifactsZip();
                 String componentName = sus[i].getTarget().getComponentName();
                 File artifactFile = new File(saDirectory, artifact);
                 if (!artifactFile.exists()) {
                     throw ManagementSupport.failure("deploy", "Artifact " + artifact + " not found for service unit " + suName);
                 }
                 ComponentMBeanImpl lcc = container.getComponent(componentName);
                 if (lcc == null) {
                     throw ManagementSupport.failure("deploy", "Target component " + componentName + " for service unit " + suName + " is not installed");
                 }
                 if (!lcc.isStarted()) {
                     throw ManagementSupport.failure("deploy", "Target component " + componentName + " for service unit " + suName + " is not started");
                 }
                 if (lcc.getServiceUnitManager() == null) {
                     throw ManagementSupport.failure("deploy", "Target component " + componentName + " for service unit " + suName + " does not accept deployments");
                 }
                 // TODO: check duplicates here ?
                 if (isDeployedServiceUnit(componentName, suName)) {
                     throw ManagementSupport.failure("deploy", "Service unit " + suName + " is already deployed on component " + componentName);
                 }
             }
         }
         // Everything seems ok, so deploy all SUs
         int nbSuccess = 0;
         int nbFailures = 0;
         List componentResults = new ArrayList();
         List suKeys = new ArrayList();
         if (sus != null) {
             for (int i = 0; i < sus.length; i++) {
                 File targetDir = null;
                 String suName = sus[i].getIdentification().getName();
                 String artifact = sus[i].getTarget().getArtifactsZip();
                 String componentName = sus[i].getTarget().getComponentName();
                 // TODO: skip duplicates
                 // Unpack SU
                 try {
                     File artifactFile = new File(saDirectory, artifact);
                     targetDir = environmentContext.getServiceUnitDirectory(componentName, suName, assemblyName);
                     if (log.isDebugEnabled()) {
                         log.debug("Unpack service unit archive " + artifactFile + " to " + targetDir);
                     }
                     FileUtil.unpackArchive(artifactFile, targetDir);
                 } catch (IOException e) {
                     nbFailures++;
                     componentResults.add(ManagementSupport.createComponentFailure(
                             "deploy", componentName,
                             "Error unpacking service unit", e));
                     continue;
                 }
                 // Deploy it
                 boolean success = false;
                 try {
                     ComponentMBeanImpl lcc = container.getComponent(componentName);
                     ServiceUnitManager sum = lcc.getServiceUnitManager();
                     ClassLoader cl = Thread.currentThread().getContextClassLoader();
                     try {
                         Thread.currentThread().setContextClassLoader(lcc.getComponent().getClass().getClassLoader());
                         String resultMsg = sum.deploy(suName, targetDir.getAbsolutePath());
                         success = getComponentTaskResult(resultMsg, componentName, componentResults, true);
                     } finally {
                         Thread.currentThread().setContextClassLoader(cl);
                     }
                     // TODO: need to register the SU somewhere to keep track of its state
                 } catch (Exception e) {
                     getComponentTaskResult(e.getMessage(), componentName, componentResults, false);
                 }
                 if (success) {
                     nbSuccess++;
                     suKeys.add(registry.registerServiceUnit(sus[i], assemblyName));
                 } else {
                     nbFailures++;
                 }
             }
         }
         // Note: the jbi spec says that if at least one deployment succeeds, 
         // this should be a SUCCESS.  However, ServiceMix handles SA in an
         // atomic way: for a given operation on an SA, all operations on SU
         // should succeed.  This is clearly a minor violation of the spec.
         //
         // Failure
         if (nbFailures > 0) {
             // Undeploy SUs
             for (Iterator iter = suKeys.iterator(); iter.hasNext();) {
                 try {
                     String suName = (String) iter.next();
                     ServiceUnitLifeCycle su = registry.getServiceUnit(suName);
                     undeployServiceUnit(su);
                 } catch (Exception e) {
                     log.warn("Error undeploying SU", e);
                 }
             }
             // Delete SA deployment directory 
             FileUtil.deleteFile(saDirectory);
             throw ManagementSupport.failure("deploy", componentResults);
         }
         // Success
         else {
             // Register SA
             String[] deployedSUs = (String[]) suKeys.toArray(new String[suKeys.size()]);
             ServiceAssemblyLifeCycle salc = registry.registerServiceAssembly(sa, deployedSUs);
             salc.writeRunningState();
             // Build result string
             if (nbFailures > 0) {
                 return ManagementSupport.createWarningMessage("deploy", "Failed to deploy some service units", componentResults);
             } else {
                 return ManagementSupport.createSuccessMessage("deploy", componentResults);
             }
         }
     }
     
     protected boolean getComponentTaskResult(String resultMsg, String component, List results, boolean success) {
         Element result = null;
         try {
             Document doc = parse(resultMsg);
             result = getElement(doc, "component-task-result");
             Element e = getChildElement(result, "component-task-result-details");
             e = getChildElement(e, "task-result-details");
             e = getChildElement(e, "task-result");
             String r = DOMUtil.getElementText(e);
             if (!"SUCCESS".equals(r)) {
                 success = false;
             }
         } catch (Exception e) {
             // The component did not throw an exception upon deployment,
             // but the result string is not compliant, so issue a warning
             // and consider this is a successfull deployment
             try {
                 if (success) {
                    result = ManagementSupport.createComponentFailure(
                             "deploy", component,
                             "Unable to parse result string", e);
                 } else {
                    result = ManagementSupport.createComponentWarning(
                             "deploy", component,
                             "Unable to parse result string", e);
                 }
             } catch (Exception e2) {
                 log.error(e2);
                 result = null;
             }
         }
         if (result != null) {
             results.add(result);
         }
         return success;
     }
     
     protected Document parse(String result) throws ParserConfigurationException, SAXException, IOException {
         DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
         factory.setNamespaceAware(true);
         factory.setIgnoringElementContentWhitespace(true);
         factory.setIgnoringComments(true);
         DocumentBuilder builder = factory.newDocumentBuilder();
         return builder.parse(new InputSource(new StringReader(result)));
     }
     
     protected Element getElement(Document doc, String name) {
         NodeList l = doc.getElementsByTagNameNS("http://java.sun.com/xml/ns/jbi/management-message", name);
         Element e = (Element) l.item(0);
         return e;
     }
     
     protected Element getChildElement(Element element, String name) {
         NodeList l = element.getElementsByTagNameNS("http://java.sun.com/xml/ns/jbi/management-message", name);
         Element e = (Element) l.item(0);
         return e;
     }
 
     protected void undeployServiceUnit(ServiceUnitLifeCycle su) throws DeploymentException {
         String name = su.getName();
         String componentName = su.getComponentName();
         File targetDir = su.getServiceUnitRootPath();
         registry.unregisterServiceUnit(su.getKey());
         // unpack the artifact
         // now get the component and give it a SA
         ComponentMBeanImpl component = container.getComponent(componentName);
         if (component != null) {
             ServiceUnitManager sum = component.getServiceUnitManager();
             if (sum != null) {
                 ClassLoader cl = Thread.currentThread().getContextClassLoader();
                 try {
                     Thread.currentThread().setContextClassLoader(component.getComponent().getClass().getClassLoader());
                     sum.undeploy(name, targetDir.getAbsolutePath());
                 } finally {
                     Thread.currentThread().setContextClassLoader(cl);
                 }
                 FileUtil.deleteFile(targetDir);
             }
         }
         else {
             FileUtil.deleteFile(targetDir);
         }
         log.info("UnDeployed ServiceUnit " + name + " from Component: " + componentName);
     }
 
     /**
      * Find runnning state and things deployed before shutdown
      */
     protected void buildState() {
         log.info("Restoring service assemblies");
         // walk through deployed SA's
         File top = environmentContext.getServiceAssembliesDirectory();
         if (top != null && top.exists() && top.isDirectory()) {
             File[] files = top.listFiles();
             if (files != null) {
             	// Initialize all assemblies
                 for (int i = 0; i < files.length; i++) {
                     if (files[i].isDirectory()) {
                         String assemblyName = files[i].getName();
                         try {
                         	File assemblyDir = environmentContext.getSARootDirectory(assemblyName);
 	                        Descriptor root = DescriptorFactory.buildDescriptor(assemblyDir);
 	                        if (root != null) {
 	                            ServiceAssembly sa = root.getServiceAssembly();
 	                            if (sa != null && sa.getIdentification() != null) {
 	                                registry.registerServiceAssembly(sa);
 	                            }
 	                        }
                         } catch(Exception e) {
                             log.error("Failed to initialized service assembly: " + assemblyName,e);
                         }
                     }
                 }
             }
         }
     }
 
 }
