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
 package org.apache.servicemix.console;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.servicemix.jbi.audit.AuditorMBean;
 import org.apache.servicemix.jbi.audit.jdbc.JdbcAuditor;
 import org.apache.servicemix.jbi.container.JBIContainer;
 import org.apache.servicemix.jbi.framework.DeploymentService;
import org.apache.servicemix.jbi.framework.FrameworkInstallationService;
 import org.apache.servicemix.jbi.framework.InstallationService;
 import org.apache.servicemix.jbi.management.ManagementContext;
 import org.apache.servicemix.jbi.management.ManagementContextMBean;
 
 import javax.jbi.management.DeploymentServiceMBean;
 import javax.jbi.management.LifeCycleMBean;
 import javax.management.MBeanServerConnection;
 import javax.management.MBeanServerInvocationHandler;
 import javax.management.ObjectName;
 import javax.management.remote.JMXConnector;
 import javax.management.remote.JMXConnectorFactory;
 import javax.management.remote.JMXServiceURL;
 import javax.portlet.ActionRequest;
 import javax.portlet.ActionResponse;
 import javax.portlet.GenericPortlet;
 import javax.portlet.PortletConfig;
 import javax.portlet.PortletContext;
 import javax.portlet.PortletException;
 import javax.portlet.PortletRequestDispatcher;
 import javax.portlet.RenderRequest;
 import javax.portlet.RenderResponse;
 import javax.portlet.WindowState;
 
 import java.io.IOException;
 import java.net.MalformedURLException;
 
 public abstract class ServiceMixPortlet extends GenericPortlet {
 
     protected final Log log = LogFactory.getLog(getClass());
     
     protected PortletRequestDispatcher normalView;
 
     protected PortletRequestDispatcher helpView;
 
     private JMXConnector jmxConnector;
     private String namingHost = "localhost";
     private String containerName = JBIContainer.DEFAULT_NAME;
     private String jmxDomainName = ManagementContext.DEFAULT_DOMAIN;
     private int namingPort = ManagementContext.DEFAULT_CONNECTOR_PORT;
     private String jndiPath = ManagementContext.DEFAULT_CONNECTOR_PATH;
 
     /**
      * Get the JMXServiceURL - built from the protocol used and host names
      * @return the url
      */
     public JMXServiceURL getServiceURL(){
         JMXServiceURL url = null;
         try {
             url = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://" + namingHost + ":" + namingPort + jndiPath);
         }
         catch (MalformedURLException e) {
             log.error("error creating serviceURL: ",e);
         }
         return url;
     }
     
     /**
      * Get a JMXConnector from a url
      * @param url
      * @return the JMXConnector
      * @throws IOException
      */
     public JMXConnector getJMXConnector (JMXServiceURL url) throws IOException {
         log.info("Connecting to JBI Container at: " + url);
         return JMXConnectorFactory.connect(url);
     }
     
     protected void doHelp(RenderRequest renderRequest, RenderResponse renderResponse) throws PortletException, IOException {
         log.debug("doHelp");
         helpView.include(renderRequest, renderResponse);
     }
 
     protected void doView(RenderRequest renderRequest, RenderResponse renderResponse) throws PortletException, IOException {
         log.debug("doView");
         if (WindowState.MINIMIZED.equals(renderRequest.getWindowState())) {
             return;
         }
         try {
             // Retrieve the jmx connector
             if (this.jmxConnector == null) {
                 this.jmxConnector = getJMXConnector(getServiceURL());
             }
             // Fill request
             fillViewRequest(renderRequest);
             // Render view
             normalView.include(renderRequest, renderResponse);
         } catch (PortletException e) {
             log.error("Error rendering portlet", e);
             closeConnector();
             throw e;
         } catch (IOException e) {
             log.error("Error rendering portlet", e);
             closeConnector();
             throw e;
         } catch (Exception e) {
             log.error("Error rendering portlet", e);
             closeConnector();
             throw new PortletException("Error rendering portlet", e);
         }
     }
     
     /**
      * Get a servicemix internal system management instance, from it's class name
      * @param systemClass
      * @return the object name
      */
     protected  ObjectName getObjectName (Class systemClass){
         return ManagementContext.getSystemObjectName(jmxDomainName, containerName, systemClass);
     }
     
     
     protected void fillViewRequest(RenderRequest request) throws Exception {
     }
 
     public void init(PortletConfig portletConfig) throws PortletException {
         log.debug("init");
         super.init(portletConfig);
         PortletContext pc = portletConfig.getPortletContext();
         normalView = pc.getRequestDispatcher("/WEB-INF/view/" + getPortletName() + "/view.jsp");
         helpView = pc.getRequestDispatcher("/WEB-INF/view/" + getPortletName() + "/help.jsp");
     }
 
     public void processAction(ActionRequest actionRequest, ActionResponse actionResponse) throws PortletException, IOException {
         log.debug("processAction: " + actionRequest);
         try {
             // Retrieve the jmx connector
             if (this.jmxConnector == null) {
                 this.jmxConnector = getJMXConnector(getServiceURL());
             }
             // Fill request
             doProcessAction(actionRequest, actionResponse);
         } catch (PortletException e) {
             log.error("Error processing action", e);
             closeConnector();
             throw e;
         } catch (IOException e) {
             log.error("Error processing action", e);
             closeConnector();
             throw e;
         } catch (Exception e) {
             log.error("Error processing action", e);
             closeConnector();
             throw new PortletException("Error processing action", e);
         }
     }
 
     protected void doProcessAction(ActionRequest actionRequest, ActionResponse actionResponse) throws Exception {
     }
 
     public void destroy() {
         closeConnector();
         super.destroy();
     }
     
     protected void closeConnector() {
         if (this.jmxConnector != null){
             try {
                 jmxConnector.close();
             } catch (Exception e) {
                 log.warn("caught an error closing the jmxConnector", e);
             } finally {
                 jmxConnector = null;
             }
         }
     }
     
     /**
      * Get the InstallationServiceMBean
      * @return the installation service MBean
      * @throws IOException
      */
    public FrameworkInstallationService getInstallationService() throws IOException {
         ObjectName objectName = getObjectName(InstallationService.class);
        return (FrameworkInstallationService) getProxy(objectName, FrameworkInstallationService.class);
     }
     
     /**
      * Get the DeploymentServiceMBean 
      * @return the deployment service mbean
      * @throws IOException
      */
     public DeploymentServiceMBean getDeploymentService() throws IOException {
         ObjectName objectName = getObjectName(DeploymentService.class);
         return (DeploymentServiceMBean) getProxy(objectName, DeploymentServiceMBean.class);
     }
     
     
     /**
      * Get the ManagementContextMBean 
      * @return the management service mbean
      * @throws IOException
      */
     public ManagementContextMBean getManagementContext() throws IOException {
         ObjectName objectName = getObjectName(ManagementContext.class);
         return (ManagementContextMBean) getProxy(objectName, ManagementContextMBean.class);
     }
     
     public AuditorMBean getJdbcAuditor() throws IOException {
         ObjectName objectName = getObjectName(JdbcAuditor.class);
         return (AuditorMBean) getProxy(objectName, AuditorMBean.class);
     }
     
     public LifeCycleMBean getJBIContainer() throws IOException {
         ObjectName objectName = ManagementContext.getContainerObjectName(jmxDomainName, containerName);
         return (LifeCycleMBean) getProxy(objectName, LifeCycleMBean.class);
     }
     
     public Object getProxy(ObjectName name, Class type) throws IOException {
         return MBeanServerInvocationHandler.newProxyInstance(getServerConnection(), name, type, true);
     }
     
     public MBeanServerConnection getServerConnection() throws IOException {
         return jmxConnector.getMBeanServerConnection();
     }
 
     public String getContainerName() {
         return containerName;
     }
 
     public void setContainerName(String containerName) {
         this.containerName = containerName;
     }
     
 }
