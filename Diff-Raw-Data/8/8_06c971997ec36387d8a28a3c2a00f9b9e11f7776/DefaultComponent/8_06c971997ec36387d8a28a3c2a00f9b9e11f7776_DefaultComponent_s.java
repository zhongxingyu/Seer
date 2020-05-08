 /**
  *
  * Licensed to the Apache Software Foundation (ASF) under one or more
  * contributor license agreements.  See the NOTICE file distributed with
  * this work for additional information regarding copyright ownership.
  * The ASF licenses this file to You under the Apache License, Version 2.0
  * (the "License"); you may not use this file except in compliance with
  * the License.  You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.apache.servicemix.common;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.servicemix.common.xbean.XBeanServiceUnit;
 import org.apache.servicemix.common.xbean.BaseXBeanDeployer;
 import org.w3c.dom.Document;
 import org.w3c.dom.DocumentFragment;
 
 import javax.jbi.component.ComponentContext;
 import javax.jbi.component.ComponentLifeCycle;
 import javax.jbi.component.ServiceUnitManager;
 import javax.jbi.management.DeploymentException;
 import javax.jbi.messaging.MessageExchange;
 import javax.jbi.servicedesc.ServiceEndpoint;
 import java.util.Arrays;
 import java.util.List;
 import java.util.Iterator;
 import java.util.Collections;
 
 /**
  * A useful base class for writing new JBI components which includes the {@link ComponentLifeCycle} interface methods so that
  * you can write a new component in a single class with minimal overloading.
  *
  * @version $Revision$
  */
 public abstract class DefaultComponent extends BaseLifeCycle implements ServiceMixComponent {
 
     protected final transient Log logger = LogFactory.getLog(getClass());
 
     protected Registry registry;
     protected BaseServiceUnitManager serviceUnitManager;
     protected ServiceUnit serviceUnit;
 
     public DefaultComponent() {
         setComponent(this);
         registry = createRegistry();
         serviceUnitManager = createServiceUnitManager();
     }
 
     /* (non-Javadoc)
      * @see javax.jbi.component.Component#getLifeCycle()
      */
     public ComponentLifeCycle getLifeCycle() {
         return this;
     }
 
     /* (non-Javadoc)
      * @see javax.jbi.component.Component#getServiceUnitManager()
      */
     public ServiceUnitManager getServiceUnitManager() {
         return serviceUnitManager;
     }
 
     /* (non-Javadoc)
      * @see javax.jbi.component.Component#getServiceDescription(javax.jbi.servicedesc.ServiceEndpoint)
      */
     public Document getServiceDescription(ServiceEndpoint endpoint) {
         if (logger.isDebugEnabled()) {
             logger.debug("Querying service description for " + endpoint);
         }
         String key = EndpointSupport.getKey(endpoint);
         Endpoint ep = this.registry.getEndpoint(key);
         if (ep != null) {
             Document doc = ep.getDescription();
             if (doc == null) {
                 if (logger.isDebugEnabled()) {
                     logger.debug("No description found for " + key);
                 }
             }
             return doc;
         }
         else {
             if (logger.isDebugEnabled()) {
                 logger.debug("No endpoint found for " + key);
             }
             return null;
         }
     }
 
     /* (non-Javadoc)
      * @see javax.jbi.component.Component#isExchangeWithConsumerOkay(javax.jbi.servicedesc.ServiceEndpoint, javax.jbi.messaging.MessageExchange)
      */
     public boolean isExchangeWithConsumerOkay(ServiceEndpoint endpoint, MessageExchange exchange) {
         String key = EndpointSupport.getKey(endpoint);
         Endpoint ep = this.registry.getEndpoint(key);
         if (ep != null) {
             if (ep.getRole() != MessageExchange.Role.PROVIDER) {
                 if (logger.isDebugEnabled()) {
                     logger.debug("Endpoint " + key + " is a consumer. Refusing exchange with consumer.");
                 }
                 return false;
             }
             else {
                 return ep.isExchangeOkay(exchange);
             }
         }
         else {
             if (logger.isDebugEnabled()) {
                 logger.debug("No endpoint found for " + key + ". Refusing exchange with consumer.");
             }
             return false;
         }
     }
 
     /* (non-Javadoc)
      * @see javax.jbi.component.Component#isExchangeWithProviderOkay(javax.jbi.servicedesc.ServiceEndpoint, javax.jbi.messaging.MessageExchange)
      */
     public boolean isExchangeWithProviderOkay(ServiceEndpoint endpoint, MessageExchange exchange) {
         // TODO: check if the selected endpoint is good for us
         return true;
     }
 
     /* (non-Javadoc)
      * @see javax.jbi.component.Component#resolveEndpointReference(org.w3c.dom.DocumentFragment)
      */
     public ServiceEndpoint resolveEndpointReference(DocumentFragment epr) {
         return null;
     }
 
 
     /**
      * Create the service unit manager.
      * Derived classes should override this method and return a
      * BaseServiceUnitManager so that the component is able to
      * handle service unit deployment.
      *
      * The default implementation will create a @{link BaseXBeanDeployer} instance
      * using the value of @{link #getEndpointClasses()} if that method returns a non-null value
      * otherwise it returns null.
      *
      * @return a newly created service unit manager
      */
     protected BaseServiceUnitManager createServiceUnitManager() {
         Class[] classes = getEndpointClasses();
         if (classes == null) {
             return null;
         }
         Deployer[] deployers = new Deployer[] { new BaseXBeanDeployer(this, classes) };
         return new BaseServiceUnitManager(this, deployers);
     }
 
 
     protected Registry createRegistry() {
         return new Registry(this);
     }
 
     public ComponentContext getComponentContext() {
         return getContext();
     }
 
     public String getComponentName() {
         if (getComponentContext() == null) {
             return "Component (" + getClass().getName() + ") not yet initialized";
         }
         return getComponentContext().getComponentName();
     }
 
     /**
      * @return Returns the logger.
      */
     public Log getLogger() {
         return logger;
     }
 
     /**
      * @return Returns the registry.
      */
     public Registry getRegistry() {
         return registry;
     }
 
 
     /**
      * Returns the service unit, lazily creating one on demand
      *
      * @return the service unit if one is being used.
      */
     public ServiceUnit getServiceUnit() {
         if (serviceUnit == null) {
             serviceUnit = new XBeanServiceUnit();
             serviceUnit.setComponent(this);
         }
         return serviceUnit;
     }
 
     /**
      * Returns an array of configured endpoints for the component or null if there are no configured endpoints
      */
     protected abstract List getConfiguredEndpoints();
 
     /**
      * Returns a list of valid endpoint classes or null if the component does not wish to programmatically
      * restrict the list of possible endpoint classes
      *
      * @return the endpoint classes used to validate configuration or null to disable the validation
      */
     protected abstract Class[] getEndpointClasses();
 
     /**
     * A little helper method to turn a possibly null list of endpoitns into a list of endpoints
      */
     protected List asList(Endpoint[] endpoints) {
         if (endpoints == null) {
             return Collections.EMPTY_LIST;
         }
         return Arrays.asList(endpoints);
     }
 
     /* (non-Javadoc)
     * @see org.servicemix.common.BaseLifeCycle#doInit()
     */
     protected void doInit() throws Exception {
         super.doInit();
         List endpoints = getConfiguredEndpoints();
         if (endpoints != null && !endpoints.isEmpty()) {
             ServiceUnit su = getServiceUnit();
             Iterator iter = endpoints.iterator();
             while (iter.hasNext()) {
                 Endpoint endpoint = (Endpoint) iter.next();
                 if (endpoint == null) {
                     logger.warn("Ignoring null endpoint in list: " + endpoints);
                     continue;
                 }
                 endpoint.setServiceUnit(su);
                 validateEndpoint(endpoint);
                 endpoint.validate();
                 su.addEndpoint(endpoint);
             }
             getRegistry().registerServiceUnit(su);
         }
     }
 
 
     /**
      * Provides a hook to validate the statically configured endpoint
      */
     protected void validateEndpoint(Endpoint endpoint) throws DeploymentException {
         Class[] endpointClasses = getEndpointClasses();
         if (endpointClasses != null) {
             boolean valid = false;
             for (int i = 0; i < endpointClasses.length; i++) {
                 Class endpointClass = endpointClasses[i];
                 if (endpointClass.isInstance(endpoint)) {
                     valid = true;
                 }
             }
             if (!valid) {
                 throw new DeploymentException("The endpoint: " + endpoint
                         + " is not an instance of any of the allowable types: " + Arrays.asList(endpointClasses));
             }
         }
     }
 
 
     /* (non-Javadoc)
     * @see org.servicemix.common.BaseLifeCycle#doStart()
     */
     protected void doStart() throws Exception {
         super.doStart();
         if (serviceUnit != null) {
             serviceUnit.start();
         }
     }
 
     /* (non-Javadoc)
     * @see org.servicemix.common.BaseLifeCycle#doStop()
     */
     protected void doStop() throws Exception {
         if (serviceUnit != null) {
             serviceUnit.stop();
         }
         super.doStop();
     }
 
     /* (non-Javadoc)
     * @see org.servicemix.common.BaseLifeCycle#doShutDown()
     */
     protected void doShutDown() throws Exception {
         if (serviceUnit != null) {
             serviceUnit.shutDown();
         }
         super.doShutDown();
     }
 
 
 }
