 /*
  * SCI-Flex: Flexible Integration of SOA and CEP
  * Copyright (C) 2008, 2009  http://sci-flex.org
  *
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 2 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License along
  * with this program; if not, write to the Free Software Foundation, Inc.,
  * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
  */
 
 package org.sciflex.plugins.synapse.esper.mediators;
 
 import java.io.ByteArrayInputStream;
 import java.io.File;
 import java.util.Map;
 
 import javax.xml.namespace.QName;
 import javax.xml.parsers.DocumentBuilderFactory;
 
 import com.espertech.esper.client.Configuration;
 import com.espertech.esper.client.ConfigurationException;
 import com.espertech.esper.client.EPServiceProvider;
 import com.espertech.esper.client.EPServiceProviderManager;
 import com.espertech.esper.client.EPStatement;
 import com.espertech.esper.client.EventTypeException;
 
 import org.apache.axiom.om.OMElement;
 import org.apache.axiom.om.OMException;
 import org.apache.axiom.om.OMSourcedElement;
 import org.apache.axiom.om.ds.MapDataSource;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.synapse.ManagedLifecycle;
 import org.apache.synapse.Mediator;
 import org.apache.synapse.MessageContext;
 import org.apache.synapse.core.SynapseEnvironment;
 
 import org.sciflex.plugins.synapse.esper.core.activities.MonitoredMediatorActivity;
 import org.sciflex.plugins.synapse.esper.core.workflows.MonitoredRequestResponseWorkflow;
 import org.sciflex.plugins.synapse.esper.core.util.UniquelyIdentifiableChild;
 import org.sciflex.plugins.synapse.esper.core.util.UUIDGenerator;
 import org.sciflex.plugins.synapse.esper.mediators.editors.CEPInstanceEditor;
 import org.sciflex.plugins.synapse.esper.mediators.helpers.AdminComponentStore;
 import org.sciflex.plugins.synapse.esper.mediators.helpers.EPLStatementHelper;
 import org.sciflex.plugins.synapse.esper.mediators.helpers.InvokableEPLStatementHelper;
 import org.sciflex.plugins.synapse.esper.mediators.monitors.MediatorActivityMonitor;
 import org.sciflex.plugins.synapse.esper.mediators.monitors.MediatorStatisticsMonitor;
 
 import org.w3c.dom.Document;
 
 /**
  * Class Mediator which can be used to connect Esper to synapse through its
  * Axiom communication API. This mediator is designed to be referrenced by the
  * synapse.xml.
  * <p>
  * A sample synapse.xml file is found in the examples folder.
  * @see XMLMediator
  */
 public class AxiomMediator implements SynapseEsperMediator {
 
     /**
      * Log associated with the Axiom Mediator.
      */
     private static final Log log = LogFactory.getLog(AxiomMediator.class);
 
     /**
      * Trace State.
      */
     private int traceState = 0;
 
     /**
      * The Instance URI to be used by EPServiceProviderManager.
      * @see com.espertech.esper.client.EPServiceProviderManager
      */
     private String instanceURI = null;
 
     /**
      * Esper Configuration instance.
      */
     protected Configuration configuration = new Configuration();
 
     /**
      * Synapse Listener Instance.
      */
     private SynapseListener listener = null;
 
     /**
      * Helper to handle EPL statements.
      */
     private InvokableEPLStatementHelper eplStatementHelper = null;
 
     /**
      * Indicates that the Mediator is {@link Map} aware.
      */
     private boolean isMapAware = false;
 
     /**
      * Monitors mediator activities.
      */
     private MonitoredMediatorActivity activityMonitor = null;
 
     /**
      * Monitors mediator statistics.
      */
     private MonitoredRequestResponseWorkflow statMonitor = null;
 
     /**
      * Admin component store.
      */
     private AdminComponentStore componentStore = null;
 
     /**
      * Unique Identifier of this object.
      */
     private String uid = null;
 
     /**
      * The Alias of Mediator
      */
     private String alias = null;
 
     /**
      * Whether inactive at start
      */
     private boolean inactive = false;
 
     /**
      * Object used to control access to {@link #getProvider}. Please
      * note that the {@link #mediate} method is calling getProvider()
      * without using this lock, for achieving higher concurrency levels
      * and any change to the outcome of {@link #getProvider} must
      * involve, temporarily stopping the mediator activity. A change to
      * {@link #getProvider} is considered a configuration change.
      */
     private Object providerLock = new Object();
 
     /**
      * Sets the EventToAddress. Please set listener before setting the
      * EventToAddress.
      * @param uri URI of To Address associated with Event.
      */
     public void setEventToAddress(String uri) {
         if (listener != null)
             listener.setEventToAddress(uri);
         else
             log.error("Listener has not been set");
     }
 
     /**
      * Sets the Esper Configuration details.
      * @param path Path to configuration file.
      */
     public void setConfiguration(String path) {
         log.debug("Setting configuration " + path);
         try {
             File file = new File(path);
             configuration.configure(file);
             log.info("Setting configuration complete");
         } catch (Exception e) {
             log.error("An error occured while setting the configuration "
                     + e.getMessage());
         }
     }
 
     /**
      * Sets the Esper Configuration details from an Axiom Element describing
      * the various details.
      * @param config Configuration Axiom Element.
      */
     public void setConfiguration(OMElement config) {
         log.debug("Setting configuration " + config);
         try {
             Document doc = DocumentBuilderFactory.newInstance()
                     .newDocumentBuilder().parse(
                     new ByteArrayInputStream(config.toString().getBytes()));
             configuration.configure(doc);
             log.info("Setting configuration complete");
         } catch (Exception e) {
             log.error("An error occured while setting the configuration "
                     + e.getMessage());
         }
     }
 
     /**
      * Gets the Trace State.
      * @return Trace State.
      */
     public int getTraceState() {
         return traceState;
     }
     
     /**
      * Sets the Trace State.
      * @param state Trace State.
      */
     public void setTraceState(int state) {
         traceState = state;
     }
 
     /**
      * Gets the Alias of Mediator
      * @return Alias of Mediator.
      */
     public String getAlias() {
         return alias;
     }
 
     /**
      * Sets the Alias of Mediator.
      * @param alias Alias of Mediator.
      */
     public void setAlias(String alias) {
         this.alias = alias;
     }
 
     /**
      * Sets the Instance URI.
      * @param uri the Instance URI.
      */
     public synchronized void setInstanceURI(String uri) {
         log.debug("Setting Instance URI " + uri);
         if (activityMonitor != null) {
             log.debug("Disabling Mediator Activity");
             activityMonitor.stopMediator();
         }
         synchronized(providerLock) {
             instanceURI = uri;
             if (eplStatementHelper != null) {
                 eplStatementHelper.changeProvider(getProvider());
             }
         }
         if (activityMonitor != null) {
             log.debug("Enabling Mediator Activity");
             activityMonitor.startMediator();
         }
     }
 
     /**
      * Gets the Instance URI.
      * @return the Instance URI.
      */
     public String getInstanceURI() {
         return instanceURI;
     }
 
     /**
      * Sets whether the Mediator is {@link Map} aware.
      * @param status this should be "true" if the Mediator
      *               is to be Map aware.
      */
     public void setIsMapAware(String status) {
         isMapAware = Boolean.valueOf(status);
         if (isMapAware) {    
             log.info("Mediator is Map aware");
         } else {
             log.info("Mediator is not Map aware");
         }
     }
 
     /**
      * Sets associated listener.
      * @param name name of listener class.
      */
     public void setListener(String name) {
         log.debug("Setting listener " + name);
         try {
             Class listenerClass = Class.forName(name);
             if (listenerClass == null) {
                 log.error("Invalid Class Name");
                 return;
             }
             Object o = listenerClass.newInstance();
             if (o instanceof SynapseListener) {
                 listener = (SynapseListener) o;
                 log.info("Listener " + name + " was successfully setup");
             }
             else
                 log.error("Setting listener failed");
         } catch (ClassNotFoundException e) {
             log.error("Class " + name + " was not found");
         } catch (Exception e) {
             log.error("Setting listener failed " + e.getMessage());
         }
     }
 
     /**
      * Sets the EPL Statement. This method is called only at init.
      * Subsequent invocations must be precedeed and followed by
      * disabling and enabling the mediator activity.
      * @param epl the EPL Statement element.
      */
     public void setStatement(OMElement epl) {
         if (listener == null) {
             listener = new SynapseListenerImpl();
         }
         log.debug("Setting EPL statement " + epl);
         String value = epl.getAttributeValue(new QName("value"));
         if (value == null) {
             value = epl.getAttributeValue(new QName("key"));
             if (value == null) {
                 log.error("Setting EPL statement failed. Got " + epl);
                 return;
             }
             else {
                 log.debug("Setting EPL statment using registry key " + value);
                 synchronized(providerLock) {
                     eplStatementHelper = new EPLStatementHelper(
                             EPLStatementHelper.EPLStatementType.INDIRECT, value, 
                             getProvider(), listener);
                 }
                 if (componentStore != null) {
                     componentStore.changeEPLStatementHelper((EPLStatementHelper)eplStatementHelper);
                 }
             }
         }
         else {
             log.debug("Setting EPL statement " + value);
             synchronized(providerLock) {
                 eplStatementHelper = new EPLStatementHelper(value, getProvider(), listener);
             }
             if (componentStore != null) {
                 componentStore.changeEPLStatementHelper((EPLStatementHelper)eplStatementHelper);
             }
         }
     }
 
     /**
      * Sets the EPL Statement. This method is called only at init.
      * Subsequent invocations must be precedeed and followed by
      * disabling and enabling the mediator activity.
      * @param epl the EPL Statement.
      */
     public void setStatement(String epl) {
         if (listener == null) {
             listener = new SynapseListenerImpl();
         }
         log.debug("Setting EPL statement " + epl);
         synchronized(providerLock) {
             eplStatementHelper = new EPLStatementHelper(epl, getProvider(), listener);
         }
         if (componentStore != null) {
             componentStore.changeEPLStatementHelper((EPLStatementHelper)eplStatementHelper);
         }
     }
 
     /**
      * Sets the mediator as inactive at the beginning.
      * @param state whether inactive or not.
      */
     public void setInactive(boolean state) {
         inactive = state;
     }
 
     /**
      * Gets an EPServiceProvider based on Mediator configuration details.
      * @return EPServiceProvider instance.
      */
     private EPServiceProvider getProvider() {
         try {
             if (instanceURI == null && configuration == null)
                 return EPServiceProviderManager.getDefaultProvider();
             else if (instanceURI == null)
                 return EPServiceProviderManager
                         .getDefaultProvider(configuration);
             else if (configuration == null)
                 return EPServiceProviderManager.getProvider(instanceURI);
             else
                 return EPServiceProviderManager.getProvider(instanceURI,
                         configuration);
         } catch (ConfigurationException e) {
             log.error("An error occured while retrieving provider "
                     + e.getMessage());
             return null;
         }
     }
 
     /**
      * Invokes the mediator passing the current message for mediation.
      * @param mc Message Context of the current message.
      * @return returns true if mediation should continue, or false if further 
      * mediation should be aborted.
      */
     public boolean mediate(MessageContext mc) {
         if (activityMonitor != null && !activityMonitor.getIsMediatorActive()) {
             log.warn("Cannot mediate. Mediator is inactive");
            return true;
         }
         long activityStamp = System.currentTimeMillis();
         log.trace("Beginning Mediation");
         // There is no providerLock in place at this point, to enable maximum
         // concurrent use of the mediate method. However, if someone is to
         // acquire a providerLock, that person must ensure that the mediate
         // method is not called.
         EPServiceProvider provider = getProvider();
         if (provider == null) {
             /* 
              * There should be an error if we couldn't obtain the provider.
              * Therefore, stop mediation
              */
             return false;
         }
         if (componentStore != null) {
             componentStore.invoke(mc);
         }
         eplStatementHelper.invoke(mc);
         // It takes this long to make the request.
         // You will have to setup all the required resources
         // to proceed with the servicing of the request.
         if (statMonitor != null) {
             long ts = System.currentTimeMillis();
             statMonitor.handleRequest(ts - activityStamp);
             activityStamp = ts;
         }
         OMElement bodyElement = null;
         try {
             bodyElement = mc.getEnvelope().getBody().getFirstElement();
         } catch (OMException e) {
             log.warn("An error occured while reading message "
                     + e.getMessage());
             // We don't mind an error while reading a message.
             return true;
         }
         if (bodyElement == null) {
             // FIXME Figure out proper response for null element.
             return true;
         }
         boolean handleMap = isMapAware;
         if (handleMap) {
             if (!(bodyElement instanceof OMSourcedElement) || !(
                 ((OMSourcedElement)bodyElement).getDataSource() != null &&
                 ((OMSourcedElement)bodyElement).getDataSource() instanceof MapDataSource)) {
                 handleMap = false;
             }
         }
         if (!handleMap) {
             bodyElement.build();
             try {
                 provider.getEPRuntime().getEventSender("AxiomEvent")
                         .sendEvent(bodyElement);
                 log.trace("Ending Mediation");
             } catch (EventTypeException e) {
                 log.error("Invalid Event Type " + e.getMessage());
             } catch (Exception e) {
                 log.error("An error occured while sending Event " + e.getMessage());
             }
         } else {
             try {
                 log.trace("Generating " + Map.class.getName() + " Event");
                 OMSourcedElement omse = (OMSourcedElement)bodyElement;
                 MapDataSource mds = (MapDataSource)omse.getDataSource();
                 Map bodyMap = (Map)mds.getObject();
                 if (bodyMap == null) {
                     // FIXME Figure out proper response for null element.
                     return true;
                 }
                 provider.getEPRuntime().sendEvent(bodyMap, "AxiomEvent");
                 log.trace("Ending Mediation");
             } catch (EventTypeException e) {
                 log.error("Invalid Event Type " + e.getMessage());
             } catch (Exception e) {
                 log.error("An error occured while sending Event " + e.getMessage());
             }
         }
         // You have to get here to say the mediator responded.
         // All other returns are failures rather.
         if (statMonitor != null) {
             long ts = System.currentTimeMillis();
             statMonitor.handleResponse(ts - activityStamp);
         }
         return true;
     }
 
     /**
      * Destruction of Initialized resources.
      */
     public void destroy() {
         if (activityMonitor != null) {
             log.debug("Disabling Mediator Activity");
             activityMonitor.stopMediator();
             activityMonitor = null;
         }
         if (statMonitor != null) {
             log.debug("Disabling Statistics Monitoring");
             statMonitor = null;
         }
         if (componentStore != null) {
             log.debug("Destroying Component Store");
             componentStore.destroy();
         }
     }
 
     /**
      * Initialization of resources.
      * @param se Synapse Environment passed by configuration.
      */
     public void init(SynapseEnvironment se) {
         // hack to get round lack of init(SynapseEnv) on mediator interface.
         if (listener != null) {
             listener.setSynapseEnvironment(se);
             if (statMonitor == null) {
                 log.debug("Enabling Statistics Monitoring");
                 statMonitor = new MediatorStatisticsMonitor(this);
             }
             if (activityMonitor == null) {
                 log.debug("Enabling Mediator Activity");
                 activityMonitor = new MediatorActivityMonitor(this);
                 if (!inactive) {
                     activityMonitor.startMediator();
                 } else {
                     activityMonitor.startMediator();
                     activityMonitor.stopMediator();
                 }
             }
             componentStore = new AdminComponentStore(this);
             componentStore.addUniquelyIdentifiableChild((UniquelyIdentifiableChild)statMonitor);
             componentStore.addUniquelyIdentifiableChild(activityMonitor);
             componentStore.addUniquelyIdentifiableChild(new CEPInstanceEditor(this));
             if (eplStatementHelper != null && eplStatementHelper instanceof EPLStatementHelper) {
                 componentStore.changeEPLStatementHelper((EPLStatementHelper)eplStatementHelper);
             }
         } else {
             log.error("Listener has not been set");
         }
     }
 
     /**
      * Returns the Unique Identifier of the resource.
      *
      * @return the Unique Identifier of the resource.
      */
     public String getUID() {
         if (uid == null) {
             uid = UUIDGenerator.createUUID();
         }
         return uid;
     }
 
     /**
      * Returns the Type of the resource.
      *
      * @return the Type of the resource.
      */
     public String getType() {
         return AxiomMediator.class.getName();
     }
 }
