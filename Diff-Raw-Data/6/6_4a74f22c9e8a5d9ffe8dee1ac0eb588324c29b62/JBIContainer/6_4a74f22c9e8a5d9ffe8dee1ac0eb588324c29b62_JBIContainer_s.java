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
 package org.apache.servicemix.jbi.container;
 
 import java.io.File;
 import java.util.Calendar;
 import java.util.Collection;
import java.util.Date;
 import java.util.Iterator;
 import java.util.List;
 import java.util.MissingResourceException;
 import java.util.logging.Logger;
 
 import javax.jbi.JBIException;
 import javax.jbi.component.Component;
 import javax.jbi.component.ComponentLifeCycle;
 import javax.jbi.component.ServiceUnitManager;
 import javax.jbi.management.DeploymentException;
 import javax.jbi.management.LifeCycleMBean;
 import javax.jbi.messaging.ExchangeStatus;
 import javax.jbi.messaging.MessageExchange;
 import javax.jbi.messaging.MessagingException;
 import javax.jbi.servicedesc.ServiceEndpoint;
 import javax.management.JMException;
 import javax.management.MBeanServer;
 import javax.management.ObjectName;
 import javax.naming.InitialContext;
 import javax.naming.NamingException;
 import javax.resource.spi.work.WorkManager;
 import javax.transaction.TransactionManager;
 import javax.xml.namespace.QName;
 
 import org.apache.activemq.util.IdGenerator;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.geronimo.connector.work.GeronimoWorkManager;
 import org.apache.servicemix.JbiConstants;
 import org.apache.servicemix.MessageExchangeListener;
 import org.apache.servicemix.components.util.ComponentAdaptor;
 import org.apache.servicemix.components.util.ComponentAdaptorMEListener;
 import org.apache.servicemix.components.util.ComponentSupport;
 import org.apache.servicemix.components.util.PojoLifecycleAdaptor;
 import org.apache.servicemix.components.util.PojoSupport;
 import org.apache.servicemix.jbi.framework.AutoDeploymentService;
 import org.apache.servicemix.jbi.framework.ComponentContextImpl;
 import org.apache.servicemix.jbi.framework.ComponentNameSpace;
 import org.apache.servicemix.jbi.framework.DeploymentService;
 import org.apache.servicemix.jbi.framework.InstallationService;
 import org.apache.servicemix.jbi.framework.LocalComponentConnector;
 import org.apache.servicemix.jbi.framework.Registry;
 import org.apache.servicemix.jbi.framework.AdminCommandsService;
import org.apache.servicemix.jbi.framework.AutoDeploymentService.ArchiveEntry;
 import org.apache.servicemix.jbi.management.BaseLifeCycle;
 import org.apache.servicemix.jbi.management.ManagementContext;
 import org.apache.servicemix.jbi.messaging.DeliveryChannelImpl;
 import org.apache.servicemix.jbi.messaging.MessageExchangeImpl;
 import org.apache.servicemix.jbi.nmr.Broker;
 import org.apache.servicemix.jbi.nmr.flow.Flow;
 import org.jencks.factory.WorkManagerFactoryBean;
 import org.w3c.dom.DocumentFragment;
 
 import edu.emory.mathcs.backport.java.util.concurrent.CopyOnWriteArrayList;
 import edu.emory.mathcs.backport.java.util.concurrent.atomic.AtomicBoolean;
 
 /**
  * The main container
  *
  * @version $Revision$
  */
 public class JBIContainer extends BaseLifeCycle {
     /**
      * Default Container name - must be unique if used in a cluster
      */
     public static final String DEFAULT_NAME = "defaultJBI";
     private static final Log log = LogFactory.getLog(JBIContainer.class);
     private String name = DEFAULT_NAME;
     private InitialContext namingContext;
     private MBeanServer mbeanServer;
     private TransactionManager transactionManager;
     private String rootDir = "." + File.separator + "rootDir";
     private AtomicBoolean started = new AtomicBoolean(false);
     private AtomicBoolean containerInitialized = new AtomicBoolean(false);
     private IdGenerator idGenerator = new IdGenerator();
     protected Broker broker = new Broker();
     protected ServiceUnitManager serviceManager;
     protected ManagementContext managementContext = new ManagementContext();
     protected EnvironmentContext environmentContext = new EnvironmentContext();
     protected InstallationService installationService = new InstallationService();
     protected DeploymentService deploymentService = new DeploymentService();
     protected AutoDeploymentService autoDeployService = new AutoDeploymentService();
     protected AdminCommandsService adminCommandsService = new AdminCommandsService();
     protected Registry registry = new Registry();
     protected WorkManager workManager;
     protected boolean isWorkManagerCreated;
     protected boolean autoEnlistInTransaction = false;
     protected boolean persistent = false;
     protected List listeners = new CopyOnWriteArrayList();
     protected boolean embedded = false;
     protected boolean notifyStatistics = false;
     
     /**
      * Default Constructor
      */
     public JBIContainer() {
     }
 
     /**
      * @return Returns the unique nam for the Container
      */
     public String getName() {
         return name;
     }
 
     /**
      * @param name The name to set (must be unique within a cluster)
      */
     public void setName(String name) {
         this.name = name;
     }
 
     /**
      * Get the description
      *
      * @return descrption
      */
     public String getDescription() {
         return "ServiceMix JBI Container";
     }
 
     /**
      * @return Returns the flowName.
      */
     public String getFlowName() {
         return broker.getFlowName();
     }
 
     /**
      * @param flowName The flow to set.
      */
     public void setFlowName(String flowName) {
         broker.setFlowName(flowName);
     }
     
     /**
      * @return the subscriptionFlowName
      */
     public String getSubscriptionFlowName() {
 		return broker.getSubscriptionFlowName();
 	}
 
     /**
      * Set the subscription flow name
      * @param subscriptionFlowName
      */
 	public void setSubscriptionFlowName(String subscriptionFlowName) {
 		broker.setSubscriptionFlowName(subscriptionFlowName);
 	}
 
     /**
      * Set the broker message flow
      *
      * @param flow
      */
     public void setFlow(Flow flow) {
         this.broker.setFlow(flow);
     }
 
     /**
      * @return the broker message Flow
      */
     public Flow getFlow() {
         return this.broker.getFlow();
     }
 
     /**
      * Get the ManagementContext
      *
      * @return the ManagementContext
      */
     public ManagementContext getManagementContext() {
         return managementContext;
     }
 
     /**
      * @return Return the EnvironmentContext
      */
     public EnvironmentContext getEnvironmentContext() {
         return environmentContext;
     }
 
     /**
      * @return Return the registry
      */
     public Registry getRegistry()  {
         return registry;
     }
     
     /**
      * @return Return the NMR broker
      */
     public Broker getBroker(){
         return broker;
     }
 
     /**
      * @return true if creates own MBeanServer if none supplied
      */
     public boolean isCreateMBeanServer() {
         return managementContext.isCreateMBeanServer();
     }
 
     /**
      * Set the flag to create own MBeanServer if none supplied
      *
      * @param enableJMX
      */
     public void setCreateMBeanServer(boolean enableJMX) {
         managementContext.setCreateMBeanServer(enableJMX);
     }
 
     /**
      * @return Returns the useMBeanServer.
      */
     public boolean isUseMBeanServer() {
         return managementContext.isUseMBeanServer();
     }
 
     /**
      * @param useMBeanServer The useMBeanServer to set.
      */
     public void setUseMBeanServer(boolean useMBeanServer) {
         managementContext.setUseMBeanServer(useMBeanServer);
     }
 
     /**
      * @return Returns the monitorInstallationDirectory.
      */
     public boolean isMonitorInstallationDirectory() {
         return autoDeployService.isMonitorInstallationDirectory();
     }
 
     /**
      * @param monitorInstallationDirectory The monitorInstallationDirectory to set.
      */
     public void setMonitorInstallationDirectory(boolean monitorInstallationDirectory) {
         autoDeployService.setMonitorInstallationDirectory(monitorInstallationDirectory);
     }
     
     /**
      * @return Returns the monitorDeploymentDirectory.
      */
     public boolean isMonitorDeploymentDirectory(){
         return autoDeployService.isMonitorDeploymentDirectory();
     }
 
     /**
      * @param monitorDeploymentDirectory The monitorDeploymentDirectory to set.
      */
     public void setMonitorDeploymentDirectory(boolean monitorDeploymentDirectory){
         autoDeployService.setMonitorDeploymentDirectory(monitorDeploymentDirectory);
     }
 
     /**
      * @return Returns the installationDir.
      */
     public String getInstallationDirPath() {
         File dir = environmentContext.getInstallationDir();
         return dir != null ? dir.getAbsolutePath() : "";
     }
 
     /**
      * Set the installationDir - rge default location is root/<container name>/installation
      *
      * @param installationDir
      */
     public void setInstallationDirPath(String installationDir) {
         if (installationDir != null && installationDir.length() > 0) {
             environmentContext.setInstallationDir(new File(installationDir));
         }
     }
 
     /**
      * @return Returns the deploymentDir.
      */
     public String getDeploymentDirPath() {
         File dir = environmentContext.getDeploymentDir();
         return dir != null ? dir.getAbsolutePath() : "";
     }
 
     /**
      * @param deploymentDir The deploymentDir to set.
      */
     public void setDeploymentDirPath(String deploymentDir) {
         if (deploymentDir != null && deploymentDir.length() > 0) {
             environmentContext.setDeploymentDir(new File(deploymentDir));
         }
     }
 
     /**
      * @return Returns the statsInterval (in secs).
      */
     public int getStatsInterval() {
         return environmentContext.getStatsInterval();
     }
 
     /**
      * @param statsInterval The statsInterval to set (in secs).
      */
     public void setStatsInterval(int statsInterval) {
         environmentContext.setStatsInterval(statsInterval);
     }
     
     /**
      * @return Returns the monitorInterval (in secs).
      */
     public int getMonitorInterval() {
     	return autoDeployService.getMonitorInterval();
     }
     
     /**
      * @param monitorInterval The monitorInterval to set (in secs).
      */
     public void setMonitorInterval(int monitorInterval) {
     	autoDeployService.setMonitorInterval(monitorInterval);
     }
 
     /**
      * @return Returns the dumpStats.
      */
     public boolean isDumpStats() {
         return environmentContext.isDumpStats();
     }
 
     /**
      * @param value The dumpStats to set.
      */
     public void setDumpStats(boolean value) {
         environmentContext.setDumpStats(value);
     }
 
     /**
      * Install an component from a url
      *
      * @param url
      * @throws DeploymentException
      */
     public void installArchive(String url) throws DeploymentException {
         installationService.install(url, true);
     }
     
     /**
      * load an archive from an external location. 
      * The archive can be a Component, Service Assembly or Shared Library.
      * @param location - can either be a url or filename (if relative - must be relative to the container)
      * @param autoStart - if true will start the component/service assembly
      * @throws DeploymentException
      */
     public void updateExternalArchive(String location,boolean autoStart) throws DeploymentException {
        updateExternalArchive(location, autoStart);
     }
     
     /**
      * load an archive from an external location and starts it
      * The archive can be a Component, Service Assembly or Shared Library.
      * @param location - can either be a url or filename (if relative - must be relative to the container)
      * @throws DeploymentException
      */
     public void updateExternalArchive(String location) throws DeploymentException {
         updateExternalArchive(location, true);
     }
     
     
     
     
 
     /**
      * @return Returns the deploymentService.
      */
     public DeploymentService getDeploymentService() {
         return deploymentService;
     }
 
     /**
      * @return Returns the installationService.
      */
     public InstallationService getInstallationService() {
         return installationService;
     }
 
     /**
      * @return the AutomDeploymentService
      */
     public AutoDeploymentService getAutoDeploymentService() {
         return autoDeployService;
     }
 
     /**
      *
      * @return the AdminCommandsService
      */
     public AdminCommandsService getAdminCommandsService() {
         return adminCommandsService;
     }
 
 
     /**
      * light weight initialization - default values for mbeanSErver, TransactionManager etc are null
      *
      * @throws JBIException
      */
     public void init() throws JBIException {
         if (containerInitialized.compareAndSet(false, true)) {
             if (this.workManager == null) {
                 this.workManager = createWorkManager();
                 this.isWorkManagerCreated = true;
             }
             if (this.namingContext == null) {
                 try {
                     this.namingContext = new InitialContext();
                 }
                 catch (NamingException e) {
                     // Log a warning, with exception only in debug
                     if (log.isDebugEnabled()) {
                         log.warn("Failed to set InitialContext", e);
                     } else {
                         log.warn("Failed to set InitialContext");
                     }
                 }
             }
             this.managementContext.init(this, getMBeanServer());
             this.mbeanServer = this.managementContext.getMBeanServer();// just in case ManagementContext creates it
             environmentContext.init(this, rootDir);
             registry.init(this);
             broker.init(this);
             
             installationService.init(this);
             deploymentService.init(this);
             autoDeployService.init(this);
             adminCommandsService.init(this);
 
             // register self with the ManagementContext
             try {
                 managementContext.registerMBean(ManagementContext.getContainerObjectName(managementContext.getJmxDomainName(), getName()), 
                                                 this, LifeCycleMBean.class);
             } catch (JMException e) {
                 throw new JBIException(e);
             }
             log.info("ServiceMix JBI Container (http://servicemix.org/) name: " + getName() + " running version: "
                     + EnvironmentContext.getVersion());
         }
     }
 
     /**
      * start processing
      *
      * @throws JBIException
      */
     public void start() throws JBIException {
         checkInitialized();
         if (started.compareAndSet(false, true)) {
             registry.start();
             broker.start();
             managementContext.start();
             environmentContext.start();
             installationService.start();
             deploymentService.start();
             autoDeployService.start();
             adminCommandsService.start();
             super.start();
         }
     }
 
     /**
      * stop the container from processing
      *
      * @throws JBIException
      */
     public void stop() throws JBIException {
         checkInitialized();
         if (started.compareAndSet(true, false)) {
             registry.stop();
             broker.stop();
             managementContext.stop();
             environmentContext.stop();
             installationService.stop();
             deploymentService.stop();
             autoDeployService.stop();
             adminCommandsService.stop();
             super.stop();
         }
     }
 
     /**
      * After a shutdown the container will require an init before a start ...
      *
      * @throws JBIException
      */
     public void shutDown() throws JBIException {
         if (containerInitialized.compareAndSet(true, false)) {
             autoDeployService.shutDown();
             registry.shutDown();
             broker.shutDown();
             environmentContext.shutDown();
             installationService.shutDown();
             deploymentService.shutDown();
             adminCommandsService.shutDown();
             // shutdown the management context last, because it will close the mbean server
             super.shutDown();
             managementContext.unregisterMBean(this);
             managementContext.shutDown();
             if (isWorkManagerCreated && workManager instanceof GeronimoWorkManager) {
                 try {
                     ((GeronimoWorkManager) workManager).doStop();
                 } catch (Exception e) {
                     throw new JBIException("Could not stop workManager", e);
                 }
             }
         }
     }
 
     
 
     /**
      * @return theMBean server assocated with the JBI
      */
     public synchronized MBeanServer getMBeanServer() {
         return mbeanServer;
     }
 
     /**
      * Set the MBeanServer
      *
      * @param mbs
      */
     public synchronized void setMBeanServer(MBeanServer mbs) {
         this.mbeanServer = mbs;
     }
 
     /**
      * @return the naming context
      */
     public synchronized InitialContext getNamingContext() {
         return namingContext;
     }
 
     /**
      * Set the naming context
      *
      * @param ic
      */
     public synchronized void setNamingContext(InitialContext ic) {
         this.namingContext = ic;
     }
 
     /**
      * @return the TransactionManager for this implementation
      */
     public synchronized Object getTransactionManager() {
         if (transactionManager == null && namingContext != null) {
             try {
                 transactionManager = (TransactionManager) namingContext.lookup("java:appserver/TransactionManager");
             }
             catch (NamingException e) {
                 log.debug("No transaction manager found from naming context: " + e.getMessage());
                 try {
                     transactionManager = (TransactionManager) namingContext.lookup("javax.transaction.TransactionManager");
                 }
                 catch (NamingException e1) {
                     log.debug("No transaction manager found from naming context: " + e.getMessage());
                 }
             }
         }
         return transactionManager;
     }
 
     /**
      * Set the transaction manager
      *
      * @param tm
      */
     public synchronized void setTransactionManager(Object tm) {
         this.transactionManager = (TransactionManager) tm;
     }
 
     /**
      * @return the root directory path
      */
     public synchronized String getRootDir() {
         return this.rootDir;
     }
 
     /**
      * Set the workspace root
      *
      * @param root
      */
     public synchronized void setRootDir(String root) {
         this.rootDir = root;
     }
 
     /**
      * @return Returns the workManager.
      */
     public WorkManager getWorkManager() {
         return workManager;
     }
 
     /**
      * @param workManager The workManager to set.
      */
     public void setWorkManager(WorkManager workManager) {
         this.workManager = workManager;
     }
 
     /**
      * Route an ExchangePacket to a destination
      *
      * @param exchange
      * @throws MessagingException
      */
     public void sendExchange(MessageExchangeImpl exchange) throws MessagingException {
         try {
             broker.sendExchangePacket(exchange);
         }
         catch (MessagingException e) {
             throw e;
         }
         catch (JBIException e) {
             throw new MessagingException(e);
         }
     }
     
     
     /**
      * @param context
      * @param externalEndpoint
      * @throws JBIException
      */
     public void registerExternalEndpoint(ComponentContextImpl context, ServiceEndpoint externalEndpoint)
             throws JBIException {
         registry.registerExternalEndpoint(context, externalEndpoint);
     }
 
     /**
      * @param context
      * @param externalEndpoint
      * @throws JBIException
      */
     public void deregisterExternalEndpoint(ComponentContextImpl context, ServiceEndpoint externalEndpoint)
             throws JBIException {
         registry.deregisterExternalEndpoint(context, externalEndpoint);
     }
 
     /**
      * @param context
      * @param epr
      * @return matching endpoint or null
      */
     public ServiceEndpoint resolveEndpointReference(ComponentContextImpl context, DocumentFragment epr) {
         return registry.resolveEndpointReference(epr);
     }
 
     /**
      * @param context
      * @param service
      * @param name
      * @return the matching endpoint
      */
     public ServiceEndpoint getEndpoint(ComponentContextImpl context, QName service, String name) {
         return registry.getEndpoint(service, name);
     }
 
     /**
      * @param context
      * @param interfaceName
      * @return endpoints that match the interface name
      */
     public ServiceEndpoint[] getEndpoints(ComponentContextImpl context, QName interfaceName) {
         return registry.getEndpoints(interfaceName);
     }
 
     /**
      * @param context
      * @param serviceName
      * @return endpoints for a given service
      */
     public ServiceEndpoint[] getEndpointsForService(ComponentContextImpl context, QName serviceName) {
         return registry.getEndpointsForService(serviceName);
     }
 
     /**
      * @param context
      * @param interfaceName
      * @return endpoints matching the interface name
      */
     public ServiceEndpoint[] getExternalEndpoints(ComponentContextImpl context, QName interfaceName) {
         return registry.getExternalEndpoints(interfaceName);
     }
 
     /**
      * @param context
      * @param serviceName
      * @return external endpoints
      */
     public ServiceEndpoint[] getExternalEndpointsForService(ComponentContextImpl context, QName serviceName) {
         return registry.getExternalEndpointsForService(serviceName);
     }
 
     /**
      * @param suffix
      * @param resourceBundleName
      * @return the Logger
      * @throws MissingResourceException
      * @throws JBIException
      */
     public Logger getLogger(String suffix, String resourceBundleName) throws MissingResourceException, JBIException {
         return Logger.getLogger(suffix, resourceBundleName);
     }
 
     /**
      * Used for Simple POJO's
      *
      * @param id - the unique component ID
      * @throws JBIException
      */
     public void deactivateComponent(String id) throws JBIException {
         ComponentNameSpace cns = new ComponentNameSpace(name, id, id);
         Component component = registry.getComponent(cns);
         LocalComponentConnector lcc = registry.getLocalComponentConnector(cns);
         if (component != null && lcc != null) {
         	lcc.getComponentMBean().shutDown();
         	lcc.unregisterMbeans(managementContext);
             registry.deregisterComponent(component);
             environmentContext.unreregister(lcc);
             log.info("Deactivating component for name: " + id + " component: " + component);
         }
         else {
             throw new JBIException("Could not find Component with id " + id);
         }
     }
 
     /**
      * Delete a Component
      *
      * @param id
      * @throws JBIException
      */
     public void deleteComponent(String id) throws JBIException {
         deactivateComponent(id);
         environmentContext.removeComponentRootDirectory(id);
     }
 
     /**
      * Get the component associated with the given component ID
      *
      * @param id
      * @return the component
      */
     public Component getComponent(String id) {
         return registry.getComponent(id);
     }
 
     /**
      * @return all local ComponentConnectors
      */
     public Collection getLocalComponentConnectors() {
         return registry.getLocalComponentConnectors();
     }
 
     /**
      * Retrive the local component connector for a given name
      *
      * @param componentName
      * @return the LocalComponentConnector
      */
     public LocalComponentConnector getLocalComponentConnector(String componentName) {
         ComponentNameSpace cns = new ComponentNameSpace(name, componentName, componentName);
         return registry.getLocalComponentConnector(cns);
     }
 
     /**
      * Activates a new component
      *
      * @param activationSpec
      * @return Component
      * @throws JBIException
      */
     public Component activateComponent(ActivationSpec activationSpec) throws JBIException {
         if (activationSpec.getId() == null) {
             if (activationSpec.getComponentName() == null) {
                 // lets generate one
                 activationSpec.setId(createComponentID());
             } else {
                 activationSpec.setId(activationSpec.getComponentName());
             }
         }
         String id = activationSpec.getId();
         if (id == null) {
             throw new IllegalArgumentException("A Registration must have an ID");
         }
         if (activationSpec.getEndpoint() == null) {
             // lets default to the ID
             activationSpec.setEndpoint(id);
         }
         if (activationSpec.getComponentName() == null) {
             activationSpec.setComponentName(id);
         }
         Object bean = activationSpec.getComponent();
         if (bean == null) {
             throw new IllegalArgumentException("A Registration must have a component associated with it");
         }
         if (bean instanceof Component) {
             Component component = (Component) bean;
             if (component instanceof ComponentSupport) {
                 defaultComponentServiceAndEndpoint((ComponentSupport) component, activationSpec);
             }
             activateComponent(component, activationSpec);
             return component;
         }
         else if (bean instanceof ComponentLifeCycle) {
             // lets support just plain lifecycle pojos
             ComponentLifeCycle lifeCycle = (ComponentLifeCycle) bean;
             if (bean instanceof PojoSupport) {
                 defaultComponentServiceAndEndpoint((PojoSupport) bean, activationSpec);
             }
             Component adaptor = createComponentAdaptor(lifeCycle, activationSpec);
             activateComponent(adaptor, activationSpec);
             return adaptor;
         }
         else if (bean instanceof MessageExchangeListener) {
             // lets support just plain listener pojos
             MessageExchangeListener listener = (MessageExchangeListener) bean;
             Component adaptor = createComponentAdaptor(listener, activationSpec);
             activateComponent(adaptor, activationSpec);
             return adaptor;
         }
         else {
             throw new IllegalArgumentException("Component name: " + id
                     + " is bound to an object which is not a JBI component, it is of type: "
                     + bean.getClass().getName());
         }
     }
 
     /**
      * Activate a POJO Component
      *
      * @param component
      * @param name
      * @return the ObjectName of the MBean for the Component
      * @throws JBIException
      */
     public ObjectName activateComponent(Component component, String name) throws JBIException {
         ActivationSpec activationSpec = new ActivationSpec();
         ComponentNameSpace cns = new ComponentNameSpace(getName(), name, name);
         activationSpec.setComponent(component);
         activationSpec.setComponentName(cns.getName());
         return activateComponent(component, activationSpec);
     }
 
     /**
      * Activate A POJO Component
      *
      * @param component
      * @param activationSpec
      * @return the ObjectName of the MBean for the Component
      * @throws JBIException
      */
     public ObjectName activateComponent(Component component, ActivationSpec activationSpec) throws JBIException {
         return activateComponent(component, "POJO Component", activationSpec, true, true, true);
     }
 
     /**
      * Called by the Installer MBean
      *
      * @param installDir
      * @param component
      * @param description
      * @param context
      * @param binding
      * @param service
      * @return the ObjectName of the Component's MBean
      * @throws JBIException
      */
     public ObjectName activateComponent(File installDir, Component component, String description,
                                         ComponentContextImpl context, boolean binding, boolean service) throws JBIException {
         ComponentNameSpace cns = context.getComponentNameSpace();
         ActivationSpec activationSpec = new ActivationSpec();
         activationSpec.setComponent(component);
         activationSpec.setComponentName(cns.getName());
         return activateComponent(installDir, component, description, context, activationSpec, false, binding, service);
     }
 
     /**
      * @param component
      * @param description
      * @param activationSpec
      * @param pojo
      * @param binding
      * @param service
      * @return the ObjectName of the Component's MBean
      * @throws JBIException
      */
     public ObjectName activateComponent(Component component, String description, ActivationSpec activationSpec,
                                         boolean pojo, boolean binding, boolean service) throws JBIException {
         ComponentNameSpace cns = new ComponentNameSpace(getName(), activationSpec.getComponentName(), activationSpec.getId());
         ComponentContextImpl context = new ComponentContextImpl(this, cns);
         return activateComponent(new File("."), component, description, context, activationSpec, pojo, binding, service);
     }
 
     /**
      * @param installationDir
      * @param component
      * @param description
      * @param context
      * @param activationSpec
      * @param pojo
      * @param binding
      * @param service
      * @return the ObjectName of the Component's MBean
      * @throws JBIException
      */
     public ObjectName activateComponent(File installationDir, Component component, String description,
                                         ComponentContextImpl context, ActivationSpec activationSpec, boolean pojo, boolean binding, boolean service)
             throws JBIException {
         ObjectName result = null;
         ComponentNameSpace cns = new ComponentNameSpace(getName(), activationSpec.getComponentName(), activationSpec.getId());
         log.info("Activating component for: " + cns + " with service: " + activationSpec.getService() + " component: "
                 + component);
         DeliveryChannelImpl dc = new DeliveryChannelImpl(this, activationSpec.getComponentName());
         LocalComponentConnector lcc = registry.registerComponent(cns, description, component, dc, binding, service);
         if (lcc != null) {
             dc.setConnector(lcc);
             lcc.setPojo(pojo);
             ComponentEnvironment env = environmentContext.registerComponent(context.getEnvironment(),lcc);
             env.setInstallRoot(installationDir);
             context.activate(component, dc, env, activationSpec);
             lcc.setContext(context);
             lcc.setActivationSpec(activationSpec);
             
             if (lcc.isPojo()) {
                 //non-pojo's are either started by the auto deployer
                 //or manually
                 lcc.getComponentMBean().init();
                 if (started.get()) {
                     lcc.getComponentMBean().start();
                 }
             } else {
                 lcc.getComponentMBean().setCurrentState(LifeCycleMBean.SHUTDOWN);
             }
             result = lcc.registerMBeans(managementContext);
         }
         return result;
     }
 
     /**
      * Allow the service and endpoint name to be configured from the registration, to reduce the amount of XML which is
      * required to configure a ServiceMix component
      *
      * @param component
      * @param activationSpec
      */
     protected void defaultComponentServiceAndEndpoint(PojoSupport component, ActivationSpec activationSpec) {
         if (activationSpec.getService() != null) {
             component.setService(activationSpec.getService());
         }
         if (activationSpec.getEndpoint() != null) {
             component.setEndpoint(activationSpec.getEndpoint());
         }
     }
 
     /**
      * Factory method to create a new work manager instance
      *
      * @return a newly created work manager
      */
     protected WorkManager createWorkManager() throws JBIException {
         WorkManagerFactoryBean factory = new WorkManagerFactoryBean();
         try {
             return factory.getWorkManager();
         }
         catch (Exception e) {
             throw new JBIException("Failed to start WorkManager: " + e, e);
         }
     }
 
 
     /**
      * Factory method to create a new component adaptor from the given lifecycle
      *
      * @param lifeCycle
      * @param activationSpec
      * @return Component
      */
     protected Component createComponentAdaptor(ComponentLifeCycle lifeCycle, ActivationSpec activationSpec) {
         ComponentAdaptor answer = null;
         if (lifeCycle instanceof MessageExchangeListener) {
             answer = new ComponentAdaptorMEListener(lifeCycle, activationSpec.getService(), activationSpec
                     .getEndpoint(), (MessageExchangeListener) lifeCycle);
         }
         else {
             answer = new ComponentAdaptor(lifeCycle, activationSpec.getService(), activationSpec.getEndpoint());
         }
         answer.setServiceManager(serviceManager);
         return answer;
     }
 
     protected Component createComponentAdaptor(MessageExchangeListener listener, ActivationSpec activationSpec) {
         ComponentLifeCycle lifecCycle = new PojoLifecycleAdaptor(listener, activationSpec.getService(), activationSpec
                 .getEndpoint());
         return new ComponentAdaptorMEListener(lifecCycle, listener);
     }
 
     /**
      * Factory method to create a new component ID if none is specified
      *
      * @return uniqueId
      */
     protected String createComponentID() {
         return idGenerator.generateId();
     }
 
     protected void checkInitialized() throws JBIException {
         if (containerInitialized.get() == false) {
             throw new JBIException("The Container is not initialized - please call init(...)");
         }
     }
 
     /**
      * Retrieve the value for automatic transaction enlistment.
      * @return 
      */
 	public boolean isAutoEnlistInTransaction() {
 		return autoEnlistInTransaction;
 	}
 
 	/**
 	 * Set the new value for automatic transaction enlistment.
 	 * When this parameter is set to <code>true</code> and a transaction
 	 * is running when sending / receiving an exchange, this operation will
 	 * automatically be done in the current transaction.
 	 * 
 	 * @param autoEnlistInTransaction
 	 */
 	public void setAutoEnlistInTransaction(boolean autoEnlistInTransaction) {
 		this.autoEnlistInTransaction = autoEnlistInTransaction;
 	}
 
 	public boolean isPersistent() {
 		return persistent;
 	}
 
 	/**
 	 * Set the new default value for exchange persistence.
 	 * This value will be the default if none is configured on
 	 * the activation spec of the component or on the message.
 	 * 
 	 * @param persistent
 	 */
 	public void setPersistent(boolean persistent) {
 		this.persistent = persistent;
 	}
     
     public void addListener(MessageExchangeListener listener) {
         listeners.add(listener);
     }
     
     public void removeListener(MessageExchangeListener listener) {
         listeners.remove(listener);
     }
     
     public void callListeners(MessageExchange exchange) {
         for (Iterator iter = listeners.iterator(); iter.hasNext();) {
             MessageExchangeListener listener = (MessageExchangeListener) iter.next();
             try {
                 listener.onMessageExchange(exchange);
             } catch (Exception e) {
                 log.warn("Error calling listener: " + e.getMessage(), e);
             }
         }
     }
 
     public void resendExchange(MessageExchange exchange) throws JBIException {
         if (exchange instanceof MessageExchangeImpl == false) {
             throw new IllegalArgumentException("exchange should be a MessageExchangeImpl");
         }
         MessageExchangeImpl me = (MessageExchangeImpl) exchange;
         me.getPacket().setExchangeId(new IdGenerator().generateId());
         me.getPacket().setOut(null);
         me.getPacket().setFault(null);
         me.getPacket().setError(null);
         me.getPacket().setStatus(ExchangeStatus.ACTIVE);
         me.getPacket().setProperty(JbiConstants.DATESTAMP_PROPERTY_NAME, Calendar.getInstance());
         callListeners(me);
         me.handleSend(false);
         sendExchange(me.getMirror());
     }
 
     public boolean isEmbedded() {
         return embedded;
     }
 
     public void setEmbedded(boolean embedded) {
         this.embedded = embedded;
     }
     
     public void setRmiPort( int portNum ) {
     	getManagementContext().setNamingPort( portNum );
     }
 
     public int getRmiPort() {
     	return getManagementContext().getNamingPort();
     }
 
     /**
      * @return Returns the notifyStatistics.
      */
     public boolean isNotifyStatistics() {
         return notifyStatistics;
     }
 
     /**
      * @param notifyStatistics The notifyStatistics to set.
      */
     public void setNotifyStatistics(boolean notifyStatistics) {
         this.notifyStatistics = notifyStatistics;
     }
 }
