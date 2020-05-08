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
 
 package org.sciflex.plugins.synapse.esper.service;
 
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.axis2.context.MessageContext;
 import org.apache.axis2.engine.AxisConfiguration;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import org.sciflex.plugins.synapse.esper.core.activities.MonitoredMediatorActivity;
 import org.sciflex.plugins.synapse.esper.core.activities.MonitoredStatisticActivity;
 import org.sciflex.plugins.synapse.esper.core.admin.AdminComponentServiceConstants;
 import org.sciflex.plugins.synapse.esper.core.util.AdminStatisticsObject;
 import org.sciflex.plugins.synapse.esper.core.util.EPLQueryObject;
 import org.sciflex.plugins.synapse.esper.core.util.ResetEnabled;
 import org.sciflex.plugins.synapse.esper.core.util.UniquelyIdentifiableChild;
 import org.sciflex.plugins.synapse.esper.ui.Browsable;
 import org.sciflex.plugins.synapse.esper.ui.Editable;
 
 /**
  * Admin Service for SCI-Flex Synapse Esper Plugin.
  */
 public class SEPluginAdminService extends AdminComponentServiceConstants 
     implements SEPluginAdminServiceMBean {
 
     /**
      * Log associated with the Synapse Esper Plugin Admin Service.
      */
     private static final Log log = LogFactory.getLog(SEPluginAdminService.class);
 
     /**
      * Axis Configuration associated with WSO2 Carbon Framework.
      */
     protected final AxisConfiguration axisConfig;
 
     /**
      * Indicates whether the Service has been configured.
      */
     private volatile boolean isConfigured = false;
 
     /**
      * Map of mediators
      */
     private Map allMediatorMap = null;
 
     /**
      * Map of Dynamic EPL Editors
      */
     private Map dynamicEPLEditorMap = null;
 
     /**
      * Map of Static EPL Editors
      */
     private Map staticEPLEditorMap = null;
 
     /**
      * Map of CEP Instance Editors
      */
     private Map cepInstanceEditorMap = null;
 
     /**
      * Map of Registry Key Editors
      */
     private Map registryKeyEditorMap = null;
 
     /**
      * Map of Query Activity Monitors
      */
     private Map queryActivityMonitorMap = null;
 
     /**
      * Map of Mediator Activity Monitors
      */
     private Map mediatorActivityMonitorMap = null;
 
     /**
      * Map of Mediator Statistics Monitors
      */
     private Map mediatorStatisticsMonitorMap = null;
 
     /**
      * Object used to control access to operation of {@link #initialize}.
      * This ensures that only one thread can attempt to initialize the 
      * service at a time.
      */
     private Object initLock = new Object();
 
     /**
      * Constructor that initializes service environment.
     * Retrieves references to the {@link SynapseEsperMediator} Admin Component
      * store, and the {@link AxisConfiguration} itself.
      *
      * @throws SEPluginAdminServiceException throws exception if unable to retrieve
      *                                       Axis Configuration. If this exception was
      *                                       thrown, the admin service instance is
      *                                       un-usable.
      */
     public SEPluginAdminService() throws SEPluginAdminServiceException {
         MessageContext currentContext = MessageContext.getCurrentMessageContext();
         if (currentContext != null && currentContext.getConfigurationContext() != null) {
             axisConfig = currentContext.getConfigurationContext().getAxisConfiguration();
             log.debug("Creating Service Instance");
             initialize();
         } else {
             axisConfig = null;
             log.warn("Setting up Admin Service without Axis Configuration");
         }
     }
 
     /**
      * Initializes SCI-Flex Synapse Esper Plugin Admin Service.
      *
      * @throws SEPluginAdminServiceException throws exception if an exception occured while
      *                                       retrieving Admin Components from the Axis Configuration.
      */
     public void initialize() throws SEPluginAdminServiceException {
         if (isConfigured()) {
             return;
         }
         synchronized(initLock) {
             // The second isConfigured check is required because if the service was not initialized
             // and two threads attempted initialization with one able to acquire the initLock and
             // the other unable to, the second thread will go into this block and attempt to re-init
             // the service, which is not harmful but wasteful. This can be a factor that makes the
             // system a slow-starter if we remove this second isConfigured check.
             if (!isConfigured()) {
                 log.trace("Initializing Service");
                 Map componentRoot = null;
                 if (axisConfig != null) {
                     try {
                         componentRoot = (Map) axisConfig.getParameterValue(COMPONENT_ROOT_MAP_NAME);
                     } catch (Exception e) {
                         throw new SEPluginAdminServiceException(e);
                     }
                 }
                 if (componentRoot != null) {
                     try {
                         allMediatorMap = (Map) componentRoot.get(ALL_MEDIATOR_MAP_NAME);
                         dynamicEPLEditorMap = (Map) componentRoot.get(DYNAMIC_EPL_EDITOR_MAP_NAME);
                         staticEPLEditorMap = (Map) componentRoot.get(STATIC_EPL_EDITOR_MAP_NAME);
                         cepInstanceEditorMap = (Map) componentRoot.get(CEP_INSTANCE_EDITOR_MAP_NAME);
                         registryKeyEditorMap = (Map) componentRoot.get(REGISTRY_KEY_EDITOR_MAP_NAME);
                         queryActivityMonitorMap = (Map) componentRoot.get(QUERY_ACTIVITY_MONITOR_MAP_NAME);
                         mediatorActivityMonitorMap = (Map) componentRoot.get(MEDIATOR_ACTIVITY_MONITOR_MAP_NAME);
                         mediatorStatisticsMonitorMap = (Map) componentRoot.get(MEDIATOR_STATISTICS_MONITOR_MAP_NAME);
                         isConfigured = true;
                         log.debug("Service Successfully Initialized");
                     } catch (Exception e) {
                         throw new SEPluginAdminServiceException(e);
                     }
                 } else {
                     allMediatorMap = null;
                     dynamicEPLEditorMap = null;
                     staticEPLEditorMap = null;
                     cepInstanceEditorMap = null;
                     registryKeyEditorMap = null;
                     queryActivityMonitorMap = null;
                     mediatorActivityMonitorMap = null;
                     mediatorStatisticsMonitorMap = null;
                     log.warn("Initialization Failed");
                 }
             }
         }
     }
 
     /**
      * Useful to test whether the SCI-Flex Synapse Esper Plugin Admin Service is active.
      *
      * @return Name of SCI-Flex Synapse Esper Plugin Admin Service class.
      */
     public String ping() {
         log.trace("Recieved ping request");
         return SEPluginAdminService.class.getName();
     }
 
     /**
      * Indicates whether the SCI-Flex Synapse Esper Plugin Admin Service is configured.
      *
      * @return true if configured and false otherwise.
      */
     public boolean isConfigured() {
         return isConfigured;
     }
 
     /**
      * Gets CEP Instance URI.
      *
      * @param cepInstanceEditorUID UID of CEP Instance Editor.
      * @return                     CEP Instance URI.
      * @throws SEPluginAdminServiceException
      */
     public String getCEPInstanceURI(String cepInstanceEditorUID) throws SEPluginAdminServiceException {
         log.trace("Finding CEP Instance URI");
         initialize();
         if (!isConfigured()) {
             log.warn("Unable to get CEP Instance URI Editor. Admin Service is not Configured");
             return null;
         }
         try {
             UniquelyIdentifiableChild object =
                 (UniquelyIdentifiableChild) cepInstanceEditorMap.get(cepInstanceEditorUID);
             if (object == null || !(object instanceof Browsable)) {
                 throw new SEPluginAdminServiceException("Invalid CEP Instance Editor UID");
             }
             Browsable browsableCEPInstance = (Browsable)object;
             String response = (String) browsableCEPInstance.browse();
             log.trace("Found CEP Instance URI: " + response);
             return response;
         } catch (SEPluginAdminServiceException e) {
             throw e;
             // We don't want to catch our own exception
         } catch (Exception e) {
             log.error("An error occured while retrieving CEP Instance URI");
             throw new SEPluginAdminServiceException("Unable to get CEP Instance URI", e);
         }
     }
 
     /**
      * Sets CEP Instance URI.
      *
      * @param cepInstanceEditorUID UID of CEP Instance Editor.
      * @param instanceURI          CEP Instance URI.
      * @return                     true if operation was successful, false otherwise.
      * @throws SEPluginAdminServiceException
      */
     public boolean setCEPInstanceURI(String cepInstanceEditorUID, String instanceURI) 
         throws SEPluginAdminServiceException {
         log.trace("Changing CEP Instance URI");
         initialize();
         if (!isConfigured()) {
             log.warn("Unable to get CEP Instance URI Editor. Admin Service is not Configured");
             return false;
         }
         try {
             UniquelyIdentifiableChild object = 
                 (UniquelyIdentifiableChild) cepInstanceEditorMap.get(cepInstanceEditorUID);
             if (object == null || !(object instanceof Editable)) {
                 throw new SEPluginAdminServiceException("Invalid CEP Instance Editor UID");
             }
             Editable editableCEPInstance = (Editable)object;
             boolean response = editableCEPInstance.edit(instanceURI);
             if (response) {
                 log.trace("Changed CEP Instance URI");
             } else {
                 log.warn("Modifying CEP Instance URI failed");
             }
             return response;
         } catch (SEPluginAdminServiceException e) {
             throw e;
             // We don't want to catch our own exception
         } catch (Exception e) {
             log.error("An error occured while modifying CEP Instance URI");
             throw new SEPluginAdminServiceException("Unable to set CEP Instance URI", e);
         }
     }
 
     /**
      * Gets set of mediator UIDs.
      *
      * @return set of mediator UIDs.
      * @throws SEPluginAdminServiceException
      */
     public String[] getMediatorUIDs() throws SEPluginAdminServiceException {
         log.trace("Fetching Mediator UIDs");
         initialize();
         if (!isConfigured()) {
             log.warn("Unable to get List of Mediator UIDs. Admin Service is not Configured");
             return null;
         }
         try {
             Set<String> mediatorUIDSet = ((Map<String, Map>)allMediatorMap).keySet();
             if (mediatorUIDSet == null) {
                 throw new SEPluginAdminServiceException("Unable to fetch set of Mediator UIDs");
             }
             String[] response = mediatorUIDSet.toArray(new String[0]);
             if (response == null) {
                 throw new SEPluginAdminServiceException("Map of Mediator UIDs does not have any keys");
             }
             log.trace("Found " + response.length + " Mediator UIDs");
             return response;
         } catch (SEPluginAdminServiceException e) {
             throw e;
             // We don't want to catch our own exception
         } catch (Exception e) {
             log.error("An error occured while fetching Mediator UIDs");
             throw new SEPluginAdminServiceException("Unable to get Mediator UIDs", e);
         }
     }
 
     /**
      * Gets parent of the CEP Instance Editor. The parent is a Mediator in this case
      *
      * @param cepInstanceEditorUID UID of CEP Instance Editor.
      * @return                     parent mediator's UID
      * @throws SEPluginAdminServiceException
      */
     public String getParentOfCEPInstanceEditor(String cepInstanceEditorUID)
         throws SEPluginAdminServiceException {
         log.trace("Fetching Parent of CEP Instance Editor");
         initialize();
         if (!isConfigured()) {
             log.warn("Unable to get CEP Instance URI Editor. Admin Service is not Configured");
             return null;
         }
         try {
             UniquelyIdentifiableChild object =
                 (UniquelyIdentifiableChild) cepInstanceEditorMap.get(cepInstanceEditorUID);
             if (object == null) {
                 throw new SEPluginAdminServiceException("Invalid CEP Instance Editor UID");
             }
             log.trace("Found Parent of CEP Instance Editor with UID: " + object.getParentUID());
             return object.getParentUID();
         } catch (SEPluginAdminServiceException e) {
             throw e;
             // We don't want to catch our own exception
         } catch (Exception e) {
             log.error("An error occured while fetching Parent of CEP Instance Editor");
             throw new SEPluginAdminServiceException("Unable to get Parent CEP Instance Editor", e);
         }
     }
 
     /**
      * Gets CEP Instance Editor UID for given parent. The parent is a Mediator in this case
      *
      * @param mediatorUID parent mediator's UID
      * @return            UID of CEP Instance Editor.
      * @throws SEPluginAdminServiceException
      */
     public String getCEPInstanceEditorUID(String mediatorUID) throws SEPluginAdminServiceException {
         log.trace("Fetching CEP Instance Editor UID");
         initialize();
         if (!isConfigured()) {
             log.warn("Unable to get CEP Instance Editor UID. Admin Service is not Configured");
             return null;
         }
         try {
             Map mediatorComponentMap = (Map) allMediatorMap.get(mediatorUID);
             if (mediatorComponentMap == null) {
                 throw new SEPluginAdminServiceException("Unable to fetch set of Mediator Components");
             }
             UniquelyIdentifiableChild object =
                 (UniquelyIdentifiableChild) mediatorComponentMap.get(CEP_INSTANCE_EDITOR_MAP_NAME);
             if (object == null) {
                 throw new SEPluginAdminServiceException("Unable to fetch CEP Instance Editor Component");
             }
             if (!mediatorUID.equals(object.getParentUID())) {
                 throw new SEPluginAdminServiceException("fetched CEP Instance Editor Component is invalid");
             }
             log.trace("Found CEP Instance Editor UID: " + object.getUID());
             return object.getUID();
         } catch (SEPluginAdminServiceException e) {
             throw e;
             // We don't want to catch our own exception
         } catch (Exception e) {
             log.error("An error occured while fetching CEP Instance Editor UIDs");
             throw new SEPluginAdminServiceException("Unable to get CEP Instance Editor UIDs", e);
         }
     }
 
     /**
      * Gets Registry Key.
      *
      * @param registryKeyEditorUID UID of Registry Key Editor.
      * @return                     Registry Key.
      * @throws SEPluginAdminServiceException
      */
     public String getRegistryKey(String registryKeyEditorUID) throws SEPluginAdminServiceException {
         log.trace("Finding Registry Key");
         initialize();
         if (!isConfigured()) {
             log.warn("Unable to get Registry Key Editor. Admin Service is not Configured");
             return null;
         }
         try {
             UniquelyIdentifiableChild object =
                 (UniquelyIdentifiableChild) registryKeyEditorMap.get(registryKeyEditorUID);
             if (object == null || !(object instanceof Browsable)) {
                 throw new SEPluginAdminServiceException("Invalid Registry Key Editor UID");
             }
             Browsable browsableRegistryKey = (Browsable)object;
             String response = (String) browsableRegistryKey.browse();
             log.trace("Found Registry Key: " + response);
             return response;
         } catch (SEPluginAdminServiceException e) {
             throw e;
             // We don't want to catch our own exception
         } catch (Exception e) {
             log.error("An error occured while retrieving Registry Key");
             throw new SEPluginAdminServiceException("Unable to get Registry Key", e);
         }
     }
 
     /**
      * Sets Registry Key.
      *
      * @param registryKeyEditorUID UID of Registry Key Editor.
      * @param registryKey          Registry Key.
      * @return                     true if operation was successful, false otherwise.
      * @throws SEPluginAdminServiceException
      */
     public boolean setRegistryKey(String registryKeyEditorUID, String registryKey) 
         throws SEPluginAdminServiceException {
         log.trace("Changing Registry Key");
         initialize();
         if (!isConfigured()) {
             log.warn("Unable to get Registry Key Editor. Admin Service is not Configured");
             return false;
         }
         try {
             UniquelyIdentifiableChild object = 
                 (UniquelyIdentifiableChild) registryKeyEditorMap.get(registryKeyEditorUID);
             if (object == null || !(object instanceof Editable)) {
                 throw new SEPluginAdminServiceException("Invalid Registry Key Editor UID");
             }
             Editable editableRegistryKey = (Editable)object;
             boolean response = editableRegistryKey.edit(registryKey);
             if (response) {
                 log.trace("Changed Registry Key");
             } else {
                 log.warn("Modifying Registry Key failed");
             }
             return response;
         } catch (SEPluginAdminServiceException e) {
             throw e;
             // We don't want to catch our own exception
         } catch (Exception e) {
             log.error("An error occured while modifying Registry Key");
             throw new SEPluginAdminServiceException("Unable to set Registry Key", e);
         }
     }
 
     /**
      * Gets parent of the Registry Key Editor. The parent is a Mediator in this case
      *
      * @param registryKeyEditorUID UID of Registry Key Editor.
      * @return                     parent mediator's UID
      * @throws SEPluginAdminServiceException
      */
     public String getParentOfRegistryKeyEditor(String registryKeyEditorUID)
         throws SEPluginAdminServiceException {
         log.trace("Fetching Parent of Registry Key Editor");
         initialize();
         if (!isConfigured()) {
             log.warn("Unable to get Registry Key Editor. Admin Service is not Configured");
             return null;
         }
         try {
             UniquelyIdentifiableChild object =
                 (UniquelyIdentifiableChild) registryKeyEditorMap.get(registryKeyEditorUID);
             if (object == null) {
                 throw new SEPluginAdminServiceException("Invalid Registry Key Editor UID");
             }
             log.trace("Found Parent of Registry Key Editor with UID: " + object.getParentUID());
             return object.getParentUID();
         } catch (SEPluginAdminServiceException e) {
             throw e;
             // We don't want to catch our own exception
         } catch (Exception e) {
             log.error("An error occured while fetching Parent of Registry Key Editor");
             throw new SEPluginAdminServiceException("Unable to get Parent Registry Key Editor", e);
         }
     }
 
     /**
      * Gets Registry Key Editor UID for given parent. The parent is a Mediator in this case
      *
      * @param mediatorUID parent mediator's UID
      * @return            UID of Registry Key Editor.
      * @throws SEPluginAdminServiceException
      */
     public String getRegistryKeyEditorUID(String mediatorUID) throws SEPluginAdminServiceException {
         log.trace("Fetching Registry Key Editor UID");
         initialize();
         if (!isConfigured()) {
             log.warn("Unable to get Registry Key Editor UID. Admin Service is not Configured");
             return null;
         }
         try {
             Map mediatorComponentMap = (Map) allMediatorMap.get(mediatorUID);
             if (mediatorComponentMap == null) {
                 throw new SEPluginAdminServiceException("Unable to fetch set of Mediator Components");
             }
             UniquelyIdentifiableChild object =
                 (UniquelyIdentifiableChild) mediatorComponentMap.get(REGISTRY_KEY_EDITOR_MAP_NAME);
             if (object == null) {
                 throw new SEPluginAdminServiceException("Unable to fetch Registry Key Editor Component");
             }
             if (!mediatorUID.equals(object.getParentUID())) {
                 throw new SEPluginAdminServiceException("fetched Registry Key Editor Component is invalid");
             }
             log.trace("Found Registry Key Editor UID: " + object.getUID());
             return object.getUID();
         } catch (SEPluginAdminServiceException e) {
             throw e;
             // We don't want to catch our own exception
         } catch (Exception e) {
             log.error("An error occured while fetching Registry Key Editor UIDs");
             throw new SEPluginAdminServiceException("Unable to get Registry Key Editor UIDs", e);
         }
     }
 
     /**
      * Gets Static EPL.
      *
      * @param staticEPLEditorUID UID of Static EPL Editor.
      * @return                   Static EPL.
      * @throws SEPluginAdminServiceException
      */
     public String getStaticEPL(String staticEPLEditorUID) throws SEPluginAdminServiceException {
         log.trace("Finding Static EPL");
         initialize();
         if (!isConfigured()) {
             log.warn("Unable to get Static EPL Editor. Admin Service is not Configured");
             return null;
         }
         try {
             UniquelyIdentifiableChild object =
                 (UniquelyIdentifiableChild) staticEPLEditorMap.get(staticEPLEditorUID);
             if (object == null || !(object instanceof Browsable)) {
                 throw new SEPluginAdminServiceException("Invalid Static EPL Editor UID");
             }
             Browsable browsableStaticEPL = (Browsable)object;
             String response = (String) browsableStaticEPL.browse();
             log.trace("Found Static EPL: " + response);
             return response;
         } catch (SEPluginAdminServiceException e) {
             throw e;
             // We don't want to catch our own exception
         } catch (Exception e) {
             log.error("An error occured while retrieving Static EPL");
             throw new SEPluginAdminServiceException("Unable to get Static EPL", e);
         }
     }
 
     /**
      * Sets Static EPL.
      *
      * @param staticEPLEditorUID UID of Static EPL Editor.
      * @param staticEPL          Static EPL.
      * @return                   true if operation was successful, false otherwise.
      * @throws SEPluginAdminServiceException
      */
     public boolean setStaticEPL(String staticEPLEditorUID, String staticEPL) 
         throws SEPluginAdminServiceException {
         log.trace("Changing Static EPL");
         initialize();
         if (!isConfigured()) {
             log.warn("Unable to get Static EPL Editor. Admin Service is not Configured");
             return false;
         }
         try {
             UniquelyIdentifiableChild object = 
                 (UniquelyIdentifiableChild) staticEPLEditorMap.get(staticEPLEditorUID);
             if (object == null || !(object instanceof Editable)) {
                 throw new SEPluginAdminServiceException("Invalid Static EPL Editor UID");
             }
             Editable editableStaticEPL = (Editable)object;
             boolean response = editableStaticEPL.edit(staticEPL);
             if (response) {
                 log.trace("Changed Static EPL");
             } else {
                 log.warn("Modifying Static EPL failed");
             }
             return response;
         } catch (SEPluginAdminServiceException e) {
             throw e;
             // We don't want to catch our own exception
         } catch (Exception e) {
             log.error("An error occured while modifying Static EPL");
             throw new SEPluginAdminServiceException("Unable to set Static EPL", e);
         }
     }
 
     /**
      * Gets parent of the Static EPL Editor. The parent is a Mediator in this case
      *
      * @param staticEPLEditorUID UID of Static EPL Editor.
      * @return                   parent mediator's UID
      * @throws SEPluginAdminServiceException
      */
     public String getParentOfStaticEPLEditor(String staticEPLEditorUID)
         throws SEPluginAdminServiceException {
         log.trace("Fetching Parent of Static EPL Editor");
         initialize();
         if (!isConfigured()) {
             log.warn("Unable to get Static EPL Editor. Admin Service is not Configured");
             return null;
         }
         try {
             UniquelyIdentifiableChild object =
                 (UniquelyIdentifiableChild) staticEPLEditorMap.get(staticEPLEditorUID);
             if (object == null) {
                 throw new SEPluginAdminServiceException("Invalid Static EPL Editor UID");
             }
             log.trace("Found Parent of Static EPL Editor with UID: " + object.getParentUID());
             return object.getParentUID();
         } catch (SEPluginAdminServiceException e) {
             throw e;
             // We don't want to catch our own exception
         } catch (Exception e) {
             log.error("An error occured while fetching Parent of Static EPL Editor");
             throw new SEPluginAdminServiceException("Unable to get Parent Static EPL Editor", e);
         }
     }
 
     /**
      * Gets Static EPL Editor UID for given parent. The parent is a Mediator in this case
      *
      * @param mediatorUID parent mediator's UID
      * @return            UID of Static EPL Editor.
      * @throws SEPluginAdminServiceException
      */
     public String getStaticEPLEditorUID(String mediatorUID) throws SEPluginAdminServiceException {
         log.trace("Fetching Static EPL Editor UID");
         initialize();
         if (!isConfigured()) {
             log.warn("Unable to get Static EPL Editor UID. Admin Service is not Configured");
             return null;
         }
         try {
             Map mediatorComponentMap = (Map) allMediatorMap.get(mediatorUID);
             if (mediatorComponentMap == null) {
                 throw new SEPluginAdminServiceException("Unable to fetch set of Mediator Components");
             }
             UniquelyIdentifiableChild object =
                 (UniquelyIdentifiableChild) mediatorComponentMap.get(STATIC_EPL_EDITOR_MAP_NAME);
             if (object == null) {
                 throw new SEPluginAdminServiceException("Unable to fetch Static EPL Editor Component");
             }
             if (!mediatorUID.equals(object.getParentUID())) {
                 throw new SEPluginAdminServiceException("fetched Static EPL Editor Component is invalid");
             }
             log.trace("Found Static EPL Editor UID: " + object.getUID());
             return object.getUID();
         } catch (SEPluginAdminServiceException e) {
             throw e;
             // We don't want to catch our own exception
         } catch (Exception e) {
             log.error("An error occured while fetching Static EPL Editor UIDs");
             throw new SEPluginAdminServiceException("Unable to get Static EPL Editor UIDs", e);
         }
     }
 
     /**
      * Gets Dynamic EPL.
      *
      * @param dynamicEPLEditorUID UID of Dynamic EPL Editor.
      * @return                   Dynamic EPL.
      * @throws SEPluginAdminServiceException
      */
     public String getDynamicEPL(String dynamicEPLEditorUID) throws SEPluginAdminServiceException {
         log.trace("Finding Dynamic EPL");
         initialize();
         if (!isConfigured()) {
             log.warn("Unable to get Dynamic EPL Editor. Admin Service is not Configured");
             return null;
         }
         try {
             UniquelyIdentifiableChild object =
                 (UniquelyIdentifiableChild) dynamicEPLEditorMap.get(dynamicEPLEditorUID);
             if (object == null) {
                 // This is not an exceptional situation, as there can be instances where the 
                 // dynamic EPL editor was never set.
                 log.warn("Unable to get Dynamic EPL Editor. Dynamic EPL Editor may have not been set");
                 return null;
             } else if (!(object instanceof Browsable)) {
                 throw new SEPluginAdminServiceException("Invalid Dynamic EPL Editor UID");
             }
             Browsable browsableDynamicEPL = (Browsable)object;
             String response = (String) browsableDynamicEPL.browse();
             log.trace("Found Dynamic EPL: " + response);
             return response;
         } catch (SEPluginAdminServiceException e) {
             throw e;
             // We don't want to catch our own exception
         } catch (Exception e) {
             log.error("An error occured while retrieving Dynamic EPL");
             throw new SEPluginAdminServiceException("Unable to get Dynamic EPL", e);
         }
     }
 
     /**
      * Sets Dynamic EPL.
      *
      * @param dynamicEPLEditorUID UID of Dynamic EPL Editor.
      * @param dynamicEPL          Dynamic EPL.
      * @return                   true if operation was successful, false otherwise.
      * @throws SEPluginAdminServiceException
      */
     public boolean setDynamicEPL(String dynamicEPLEditorUID, String dynamicEPL) 
         throws SEPluginAdminServiceException {
         log.trace("Changing Dynamic EPL");
         initialize();
         if (!isConfigured()) {
             log.warn("Unable to get Dynamic EPL Editor. Admin Service is not Configured");
             return false;
         }
         try {
             UniquelyIdentifiableChild object = 
                 (UniquelyIdentifiableChild) dynamicEPLEditorMap.get(dynamicEPLEditorUID);
             if (object == null || !(object instanceof Editable)) {
                 throw new SEPluginAdminServiceException("Invalid Dynamic EPL Editor UID");
             }
             Editable editableDynamicEPL = (Editable)object;
             boolean response = editableDynamicEPL.edit(dynamicEPL);
             if (response) {
                 log.trace("Changed Dynamic EPL");
             } else {
                 log.warn("Modifying Dynamic EPL failed");
             }
             return response;
         } catch (SEPluginAdminServiceException e) {
             throw e;
             // We don't want to catch our own exception
         } catch (Exception e) {
             log.error("An error occured while modifying Dynamic EPL");
             throw new SEPluginAdminServiceException("Unable to set Dynamic EPL", e);
         }
     }
 
     /**
      * Gets parent of the Dynamic EPL Editor. The parent is a Mediator in this case
      *
      * @param dynamicEPLEditorUID UID of Dynamic EPL Editor.
      * @return                   parent mediator's UID
      * @throws SEPluginAdminServiceException
      */
     public String getParentOfDynamicEPLEditor(String dynamicEPLEditorUID)
         throws SEPluginAdminServiceException {
         log.trace("Fetching Parent of Dynamic EPL Editor");
         initialize();
         if (!isConfigured()) {
             log.warn("Unable to get Dynamic EPL Editor. Admin Service is not Configured");
             return null;
         }
         try {
             UniquelyIdentifiableChild object =
                 (UniquelyIdentifiableChild) dynamicEPLEditorMap.get(dynamicEPLEditorUID);
             if (object == null) {
                 throw new SEPluginAdminServiceException("Invalid Dynamic EPL Editor UID");
             }
             log.trace("Found Parent of Dynamic EPL Editor with UID: " + object.getParentUID());
             return object.getParentUID();
         } catch (SEPluginAdminServiceException e) {
             throw e;
             // We don't want to catch our own exception
         } catch (Exception e) {
             log.error("An error occured while fetching Parent of Dynamic EPL Editor");
             throw new SEPluginAdminServiceException("Unable to get Parent Dynamic EPL Editor", e);
         }
     }
 
     /**
      * Gets Dynamic EPL Editor UID for given parent. The parent is a Mediator in this case
      *
      * @param mediatorUID parent mediator's UID
      * @return            UID of Dynamic EPL Editor.
      * @throws SEPluginAdminServiceException
      */
     public String getDynamicEPLEditorUID(String mediatorUID) throws SEPluginAdminServiceException {
         log.trace("Fetching Dynamic EPL Editor UID");
         initialize();
         if (!isConfigured()) {
             log.warn("Unable to get Dynamic EPL Editor UID. Admin Service is not Configured");
             return null;
         }
         try {
             Map mediatorComponentMap = (Map) allMediatorMap.get(mediatorUID);
             if (mediatorComponentMap == null) {
                 throw new SEPluginAdminServiceException("Unable to fetch set of Mediator Components");
             }
             UniquelyIdentifiableChild object =
                 (UniquelyIdentifiableChild) mediatorComponentMap.get(STATIC_EPL_EDITOR_MAP_NAME);
             if (object == null) {
                 throw new SEPluginAdminServiceException("Unable to fetch Dynamic EPL Editor Component");
             }
             if (!mediatorUID.equals(object.getParentUID())) {
                 throw new SEPluginAdminServiceException("fetched Dynamic EPL Editor Component is invalid");
             }
             log.trace("Found Dynamic EPL Editor UID: " + object.getUID());
             return object.getUID();
         } catch (SEPluginAdminServiceException e) {
             throw e;
             // We don't want to catch our own exception
         } catch (Exception e) {
             log.error("An error occured while fetching Dynamic EPL Editor UIDs");
             throw new SEPluginAdminServiceException("Unable to get Dynamic EPL Editor UIDs", e);
         }
     }
 
     /**
      * Gets Mediator Activity Monitor UID for given parent. The parent is a Mediator in this case
      * It is the responsibility of the caller to make sure that {@link #isConfigured} is checked
      * before calling this method.
      *
      * @param mediatorUID parent mediator's UID
      * @return            UID of Mediator Activity Monitor.
      * @throws SEPluginAdminServiceException
      */
     private String getMediatorActivityMonitorUID(String mediatorUID) throws SEPluginAdminServiceException {
         log.trace("Fetching Mediator Activity Monitor UID");
         try {
             Map mediatorComponentMap = (Map) allMediatorMap.get(mediatorUID);
             if (mediatorComponentMap == null) {
                 throw new SEPluginAdminServiceException("Unable to fetch set of Mediator Components");
             }
             UniquelyIdentifiableChild object =
                 (UniquelyIdentifiableChild) mediatorComponentMap.get(MEDIATOR_ACTIVITY_MONITOR_MAP_NAME);
             if (object == null) {
                 throw new SEPluginAdminServiceException("Unable to fetch Mediator Activity Monitor Component");
             }
             if (!mediatorUID.equals(object.getParentUID())) {
                 throw new SEPluginAdminServiceException("fetched Mediator Activity Monitor Component is invalid");
             }
             log.trace("Found Mediator Activity Monitor UID: " + object.getUID());
             return object.getUID();
         } catch (SEPluginAdminServiceException e) {
             throw e;
             // We don't want to catch our own exception
         } catch (Exception e) {
             log.error("An error occured while fetching Mediator Activity Monitor UIDs");
             throw new SEPluginAdminServiceException("Unable to get Mediator Activity Monitor UIDs", e);
         }
     }
 
     /**
      * Stops Mediator
      *
      * @param mediatorUID mediator's UID
      * @throws SEPluginAdminServiceException
      */
     public void stopMediator(String mediatorUID) 
         throws SEPluginAdminServiceException {
         log.trace("Stopping Mediator");
         initialize();
         if (!isConfigured()) {
             log.warn("Unable to get Mediator Activity Monitor. Admin Service is not Configured");
             return;
         }
         try {
             String mediatorActivityMonitorUID = getMediatorActivityMonitorUID(mediatorUID);
             if (mediatorActivityMonitorUID == null) {
                 log.error("Unable to retrieve Mediator Activity Monitor for given Mediator UID");
                 throw new SEPluginAdminServiceException("Invalid Mediator Activity Monitor UID");
             }
             UniquelyIdentifiableChild object = 
                 (UniquelyIdentifiableChild) mediatorActivityMonitorMap.get(mediatorActivityMonitorUID);
             if (object == null || !(object instanceof MonitoredMediatorActivity)) {
                 throw new SEPluginAdminServiceException("Invalid Mediator Activity Monitor UID");
             }
             MonitoredMediatorActivity monitoredMediatorActivity = (MonitoredMediatorActivity)object;
             monitoredMediatorActivity.stopMediator();
             log.trace("Successfully Stopped Mediator");
         } catch (SEPluginAdminServiceException e) {
             throw e;
             // We don't want to catch our own exception
         } catch (Exception e) {
             log.error("An error occured while stopping Mediator");
             throw new SEPluginAdminServiceException("Unable to stop Mediator", e);
         }
     }
 
    /**
      * Starts Mediator
      *
      * @param mediatorUID mediator's UID
      * @throws SEPluginAdminServiceException
      */
     public void startMediator(String mediatorUID) 
         throws SEPluginAdminServiceException {
         log.trace("Starting Mediator");
         initialize();
         if (!isConfigured()) {
             log.warn("Unable to get Mediator Activity Monitor. Admin Service is not Configured");
             return;
         }
         try {
             String mediatorActivityMonitorUID = getMediatorActivityMonitorUID(mediatorUID);
             if (mediatorActivityMonitorUID == null) {
                 log.error("Unable to retrieve Mediator Activity Monitor for given Mediator UID");
                 throw new SEPluginAdminServiceException("Invalid Mediator Activity Monitor UID");
             }
             UniquelyIdentifiableChild object = 
                 (UniquelyIdentifiableChild) mediatorActivityMonitorMap.get(mediatorActivityMonitorUID);
             if (object == null || !(object instanceof MonitoredMediatorActivity)) {
                 throw new SEPluginAdminServiceException("Invalid Mediator Activity Monitor UID");
             }
             MonitoredMediatorActivity monitoredMediatorActivity = (MonitoredMediatorActivity)object;
             monitoredMediatorActivity.startMediator();
             log.trace("Successfully Started Mediator");
         } catch (SEPluginAdminServiceException e) {
             throw e;
             // We don't want to catch our own exception
         } catch (Exception e) {
             log.error("An error occured while starting Mediator");
             throw new SEPluginAdminServiceException("Unable to start Mediator", e);
         }
     }
 
    /**
      * Refreshes Mediator
      *
      * @param mediatorUID mediator's UID
      * @throws SEPluginAdminServiceException
      */
     public void refreshMediator(String mediatorUID) 
         throws SEPluginAdminServiceException {
         log.trace("Refreshing Mediator");
         initialize();
         if (!isConfigured()) {
             log.warn("Unable to get Mediator Activity Monitor. Admin Service is not Configured");
             return;
         }
         try {
             String mediatorActivityMonitorUID = getMediatorActivityMonitorUID(mediatorUID);
             if (mediatorActivityMonitorUID == null) {
                 log.error("Unable to retrieve Mediator Activity Monitor for given Mediator UID");
                 throw new SEPluginAdminServiceException("Invalid Mediator Activity Monitor UID");
             }
             UniquelyIdentifiableChild object = 
                 (UniquelyIdentifiableChild) mediatorActivityMonitorMap.get(mediatorActivityMonitorUID);
             if (object == null || !(object instanceof MonitoredMediatorActivity)) {
                 throw new SEPluginAdminServiceException("Invalid Mediator Activity Monitor UID");
             }
             MonitoredMediatorActivity monitoredMediatorActivity = (MonitoredMediatorActivity)object;
             monitoredMediatorActivity.refreshMediator();
             log.trace("Successfully Refreshed Mediator");
         } catch (SEPluginAdminServiceException e) {
             throw e;
             // We don't want to catch our own exception
         } catch (Exception e) {
             log.error("An error occured while refreshing Mediator");
             throw new SEPluginAdminServiceException("Unable to refresh Mediator", e);
         }
     }
 
    /**
      * Gets how long the Mediator has been active.
      *
      * @param mediatorUID mediator's UID
      * @return            active time
      * @throws SEPluginAdminServiceException
      */
     public long getMediatorActiveTime(String mediatorUID) 
         throws SEPluginAdminServiceException {
         log.trace("Getting Mediator Active Time");
         initialize();
         if (!isConfigured()) {
             log.warn("Unable to get Mediator Activity Monitor. Admin Service is not Configured");
             throw new SEPluginAdminServiceException("Unable to Return Response");
         }
         try {
             String mediatorActivityMonitorUID = getMediatorActivityMonitorUID(mediatorUID);
             if (mediatorActivityMonitorUID == null) {
                 log.error("Unable to retrieve Mediator Activity Monitor for given Mediator UID");
                 throw new SEPluginAdminServiceException("Invalid Mediator Activity Monitor UID");
             }
             UniquelyIdentifiableChild object = 
                 (UniquelyIdentifiableChild) mediatorActivityMonitorMap.get(mediatorActivityMonitorUID);
             if (object == null || !(object instanceof MonitoredMediatorActivity)) {
                 throw new SEPluginAdminServiceException("Invalid Mediator Activity Monitor UID");
             }
             MonitoredMediatorActivity monitoredMediatorActivity = (MonitoredMediatorActivity)object;
             long response = monitoredMediatorActivity.getActiveTime();
             log.trace("Successfully Found Mediator Active Time: " + response);
             return response;
         } catch (SEPluginAdminServiceException e) {
             throw e;
             // We don't want to catch our own exception
         } catch (Exception e) {
             log.error("An error occured while getting Mediator Active Time");
             throw new SEPluginAdminServiceException("Unable to get Mediator Active Time", e);
         }
     }
 
    /**
      * Indicates whether the mediator is active.
      *
      * @param mediatorUID mediator's UID
      * @return            true if active, false if not
      * @throws SEPluginAdminServiceException
      */
     public boolean getIsMediatorActive(String mediatorUID) 
         throws SEPluginAdminServiceException {
         log.trace("Checking whether Mediator is active");
         initialize();
         if (!isConfigured()) {
             log.warn("Unable to get Mediator Activity Monitor. Admin Service is not Configured");
             throw new SEPluginAdminServiceException("Unable to Return Response");
         }
         try {
             String mediatorActivityMonitorUID = getMediatorActivityMonitorUID(mediatorUID);
             if (mediatorActivityMonitorUID == null) {
                 log.error("Unable to retrieve Mediator Activity Monitor for given Mediator UID");
                 throw new SEPluginAdminServiceException("Invalid Mediator Activity Monitor UID");
             }
             UniquelyIdentifiableChild object = 
                 (UniquelyIdentifiableChild) mediatorActivityMonitorMap.get(mediatorActivityMonitorUID);
             if (object == null || !(object instanceof MonitoredMediatorActivity)) {
                 throw new SEPluginAdminServiceException("Invalid Mediator Activity Monitor UID");
             }
             MonitoredMediatorActivity monitoredMediatorActivity = (MonitoredMediatorActivity)object;
             boolean response = monitoredMediatorActivity.getIsMediatorActive();
             log.trace("Successfully Found Mediator Active State: " + 
                 (response ? "active" : "inactive"));
             return response;
         } catch (SEPluginAdminServiceException e) {
             throw e;
             // We don't want to catch our own exception
         } catch (Exception e) {
             log.error("An error occured while checking whether Mediator is active");
             throw new SEPluginAdminServiceException("Unable to check whether Mediator is active", e);
         }
     }
 
    /**
      * Resets Mediator Activity Timer.
      *
      * @param mediatorUID mediator's UID
      * @throws SEPluginAdminServiceException
      */
     public void resetMediatorActivityTimer(String mediatorUID) 
         throws SEPluginAdminServiceException {
         log.trace("Resetting Mediator Activity Timer");
         initialize();
         if (!isConfigured()) {
             log.warn("Unable to get Mediator Activity Monitor. Admin Service is not Configured");
             return;
         }
         try {
             String mediatorActivityMonitorUID = getMediatorActivityMonitorUID(mediatorUID);
             if (mediatorActivityMonitorUID == null) {
                 log.error("Unable to retrieve Mediator Activity Monitor for given Mediator UID");
                 throw new SEPluginAdminServiceException("Invalid Mediator Activity Monitor UID");
             }
             UniquelyIdentifiableChild object = 
                 (UniquelyIdentifiableChild) mediatorActivityMonitorMap.get(mediatorActivityMonitorUID);
             if (object == null || !(object instanceof ResetEnabled)) {
                 throw new SEPluginAdminServiceException("Invalid Mediator Activity Monitor UID");
             }
             ResetEnabled monitoredMediatorActivity = (ResetEnabled)object;
             monitoredMediatorActivity.reset();
             log.trace("Successfully Reset Mediator Activity Timer");
         } catch (SEPluginAdminServiceException e) {
             throw e;
             // We don't want to catch our own exception
         } catch (Exception e) {
             log.error("An error occured while resetting Mediator Activity Timer");
             throw new SEPluginAdminServiceException("Unable to reset Mediator Activity Timer", e);
         }
     }
 
     /**
      * Gets Mediator Statistics Monitor UID for given parent. The parent is a Mediator in this case
      * It is the responsibility of the caller to make sure that {@link #isConfigured} is checked
      * before calling this method.
      *
      * @param mediatorUID parent mediator's UID
      * @return            UID of Mediator Statistics Monitor.
      * @throws SEPluginAdminServiceException
      */
     private String getMediatorStatisticsMonitorUID(String mediatorUID) throws SEPluginAdminServiceException {
         log.trace("Fetching Mediator Statistics Monitor UID");
         try {
             Map mediatorComponentMap = (Map) allMediatorMap.get(mediatorUID);
             if (mediatorComponentMap == null) {
                 throw new SEPluginAdminServiceException("Unable to fetch set of Mediator Components");
             }
             UniquelyIdentifiableChild object =
                 (UniquelyIdentifiableChild) mediatorComponentMap.get(MEDIATOR_STATISTICS_MONITOR_MAP_NAME);
             if (object == null) {
                 throw new SEPluginAdminServiceException("Unable to fetch Mediator Statistics Monitor Component");
             }
             if (!mediatorUID.equals(object.getParentUID())) {
                 throw new SEPluginAdminServiceException("fetched Mediator Statistics Monitor Component is invalid");
             }
             log.trace("Found Mediator Statistics Monitor UID: " + object.getUID());
             return object.getUID();
         } catch (SEPluginAdminServiceException e) {
             throw e;
             // We don't want to catch our own exception
         } catch (Exception e) {
             log.error("An error occured while fetching Mediator Statistics Monitor UIDs");
             throw new SEPluginAdminServiceException("Unable to get Mediator Statistics Monitor UIDs", e);
         }
     }
 
    /**
      * Resets Mediator Statistics Monitor.
      *
      * @param mediatorUID mediator's UID
      * @throws SEPluginAdminServiceException
      */
     public void resetMediatorStatisticsMonitor(String mediatorUID) 
         throws SEPluginAdminServiceException {
         log.trace("Resetting Mediator Statistics Monitor");
         initialize();
         if (!isConfigured()) {
             log.warn("Unable to get Mediator Statistics Monitor. Admin Service is not Configured");
             return;
         }
         try {
             String mediatorStatisticsMonitorUID = getMediatorStatisticsMonitorUID(mediatorUID);
             if (mediatorStatisticsMonitorUID == null) {
                 log.error("Unable to retrieve Mediator Statistics Monitor for given Mediator UID");
                 throw new SEPluginAdminServiceException("Invalid Mediator Statistics Monitor UID");
             }
             UniquelyIdentifiableChild object = 
                 (UniquelyIdentifiableChild) mediatorStatisticsMonitorMap.get(mediatorStatisticsMonitorUID);
             if (object == null || !(object instanceof ResetEnabled)) {
                 throw new SEPluginAdminServiceException("Invalid Mediator Statistics Monitor UID");
             }
             ResetEnabled monitoredStatisticActivity = (ResetEnabled)object;
             monitoredStatisticActivity.reset();
             log.trace("Successfully Reset Mediator Statistics Monitor");
         } catch (SEPluginAdminServiceException e) {
             throw e;
             // We don't want to catch our own exception
         } catch (Exception e) {
             log.error("An error occured while resetting Mediator Statistics Monitor");
             throw new SEPluginAdminServiceException("Unable to reset Mediator Statistics Monitor", e);
         }
     }
 
    /**
      * Gets Maximum Response Time.
      *
      * @param mediatorUID mediator's UID
      * @return            Maximum Response Time
      * @throws SEPluginAdminServiceException
      */
     public long getMaximumResponseTime(String mediatorUID) 
         throws SEPluginAdminServiceException {
         log.trace("Getting Maximum Response Time");
         initialize();
         if (!isConfigured()) {
             log.warn("Unable to get Mediator Statistics Monitor. Admin Service is not Configured");
             throw new SEPluginAdminServiceException("Unable to Return Response");
         }
         try {
             String mediatorStatisticsMonitorUID = getMediatorStatisticsMonitorUID(mediatorUID);
             if (mediatorStatisticsMonitorUID == null) {
                 log.error("Unable to retrieve Mediator Statistics Monitor for given Mediator UID");
                 throw new SEPluginAdminServiceException("Invalid Mediator Statistics Monitor UID");
             }
             UniquelyIdentifiableChild object = 
                 (UniquelyIdentifiableChild) mediatorStatisticsMonitorMap.get(mediatorStatisticsMonitorUID);
             if (object == null || !(object instanceof MonitoredStatisticActivity)) {
                 throw new SEPluginAdminServiceException("Invalid Mediator Statistics Monitor UID");
             }
             MonitoredStatisticActivity monitoredStatisticActivity = (MonitoredStatisticActivity)object;
             long response = monitoredStatisticActivity.getMaxResponseTime();
             log.trace("Successfully Got Maximum Response Time");
             return response;
         } catch (SEPluginAdminServiceException e) {
             throw e;
             // We don't want to catch our own exception
         } catch (Exception e) {
             log.error("An error occured while getting Maximum Response Time");
             throw new SEPluginAdminServiceException("Unable to get Maximum Response Time", e);
         }
     }
 
    /**
      * Gets Maximum Request Time.
      *
      * @param mediatorUID mediator's UID
      * @return            Maximum Request Time
      * @throws SEPluginAdminServiceException
      */
     public long getMaximumRequestTime(String mediatorUID) 
         throws SEPluginAdminServiceException {
         log.trace("Getting Maximum Request Time");
         initialize();
         if (!isConfigured()) {
             log.warn("Unable to get Mediator Statistics Monitor. Admin Service is not Configured");
             throw new SEPluginAdminServiceException("Unable to Return Response");
         }
         try {
             String mediatorStatisticsMonitorUID = getMediatorStatisticsMonitorUID(mediatorUID);
             if (mediatorStatisticsMonitorUID == null) {
                 log.error("Unable to retrieve Mediator Statistics Monitor for given Mediator UID");
                 throw new SEPluginAdminServiceException("Invalid Mediator Statistics Monitor UID");
             }
             UniquelyIdentifiableChild object = 
                 (UniquelyIdentifiableChild) mediatorStatisticsMonitorMap.get(mediatorStatisticsMonitorUID);
             if (object == null || !(object instanceof MonitoredStatisticActivity)) {
                 throw new SEPluginAdminServiceException("Invalid Mediator Statistics Monitor UID");
             }
             MonitoredStatisticActivity monitoredStatisticActivity = (MonitoredStatisticActivity)object;
             long response = monitoredStatisticActivity.getMaxRequestTime();
             log.trace("Successfully Got Maximum Request Time");
             return response;
         } catch (SEPluginAdminServiceException e) {
             throw e;
             // We don't want to catch our own exception
         } catch (Exception e) {
             log.error("An error occured while getting Maximum Request Time");
             throw new SEPluginAdminServiceException("Unable to get Maximum Request Time", e);
         }
     }
 
    /**
      * Gets Minimum Response Time.
      *
      * @param mediatorUID mediator's UID
      * @return            Minimum Response Time
      * @throws SEPluginAdminServiceException
      */
     public long getMinimumResponseTime(String mediatorUID) 
         throws SEPluginAdminServiceException {
         log.trace("Getting Minimum Response Time");
         initialize();
         if (!isConfigured()) {
             log.warn("Unable to get Mediator Statistics Monitor. Admin Service is not Configured");
             throw new SEPluginAdminServiceException("Unable to Return Response");
         }
         try {
             String mediatorStatisticsMonitorUID = getMediatorStatisticsMonitorUID(mediatorUID);
             if (mediatorStatisticsMonitorUID == null) {
                 log.error("Unable to retrieve Mediator Statistics Monitor for given Mediator UID");
                 throw new SEPluginAdminServiceException("Invalid Mediator Statistics Monitor UID");
             }
             UniquelyIdentifiableChild object = 
                 (UniquelyIdentifiableChild) mediatorStatisticsMonitorMap.get(mediatorStatisticsMonitorUID);
             if (object == null || !(object instanceof MonitoredStatisticActivity)) {
                 throw new SEPluginAdminServiceException("Invalid Mediator Statistics Monitor UID");
             }
             MonitoredStatisticActivity monitoredStatisticActivity = (MonitoredStatisticActivity)object;
             long response = monitoredStatisticActivity.getMinResponseTime();
             log.trace("Successfully Got Minimum Response Time");
             return response;
         } catch (SEPluginAdminServiceException e) {
             throw e;
             // We don't want to catch our own exception
         } catch (Exception e) {
             log.error("An error occured while getting Minimum Response Time");
             throw new SEPluginAdminServiceException("Unable to get Minimum Response Time", e);
         }
     }
 
    /**
      * Gets Minimum Request Time.
      *
      * @param mediatorUID mediator's UID
      * @return            Minimum Request Time
      * @throws SEPluginAdminServiceException
      */
     public long getMinimumRequestTime(String mediatorUID) 
         throws SEPluginAdminServiceException {
         log.trace("Getting Minimum Request Time");
         initialize();
         if (!isConfigured()) {
             log.warn("Unable to get Mediator Statistics Monitor. Admin Service is not Configured");
             throw new SEPluginAdminServiceException("Unable to Return Response");
         }
         try {
             String mediatorStatisticsMonitorUID = getMediatorStatisticsMonitorUID(mediatorUID);
             if (mediatorStatisticsMonitorUID == null) {
                 log.error("Unable to retrieve Mediator Statistics Monitor for given Mediator UID");
                 throw new SEPluginAdminServiceException("Invalid Mediator Statistics Monitor UID");
             }
             UniquelyIdentifiableChild object = 
                 (UniquelyIdentifiableChild) mediatorStatisticsMonitorMap.get(mediatorStatisticsMonitorUID);
             if (object == null || !(object instanceof MonitoredStatisticActivity)) {
                 throw new SEPluginAdminServiceException("Invalid Mediator Statistics Monitor UID");
             }
             MonitoredStatisticActivity monitoredStatisticActivity = (MonitoredStatisticActivity)object;
             long response = monitoredStatisticActivity.getMinRequestTime();
             log.trace("Successfully Got Minimum Request Time");
             return response;
         } catch (SEPluginAdminServiceException e) {
             throw e;
             // We don't want to catch our own exception
         } catch (Exception e) {
             log.error("An error occured while getting Minimum Request Time");
             throw new SEPluginAdminServiceException("Unable to get Minimum Request Time", e);
         }
     }
 
    /**
      * Gets Average Response Time.
      *
      * @param mediatorUID mediator's UID
      * @return            Average Response Time
      * @throws SEPluginAdminServiceException
      */
     public double getAverageResponseTime(String mediatorUID) 
         throws SEPluginAdminServiceException {
         log.trace("Getting Average Response Time");
         initialize();
         if (!isConfigured()) {
             log.warn("Unable to get Mediator Statistics Monitor. Admin Service is not Configured");
             throw new SEPluginAdminServiceException("Unable to Return Response");
         }
         try {
             String mediatorStatisticsMonitorUID = getMediatorStatisticsMonitorUID(mediatorUID);
             if (mediatorStatisticsMonitorUID == null) {
                 log.error("Unable to retrieve Mediator Statistics Monitor for given Mediator UID");
                 throw new SEPluginAdminServiceException("Invalid Mediator Statistics Monitor UID");
             }
             UniquelyIdentifiableChild object = 
                 (UniquelyIdentifiableChild) mediatorStatisticsMonitorMap.get(mediatorStatisticsMonitorUID);
             if (object == null || !(object instanceof MonitoredStatisticActivity)) {
                 throw new SEPluginAdminServiceException("Invalid Mediator Statistics Monitor UID");
             }
             MonitoredStatisticActivity monitoredStatisticActivity = (MonitoredStatisticActivity)object;
             double response = monitoredStatisticActivity.getAvgResponseTime();
             log.trace("Successfully Got Average Response Time");
             return response;
         } catch (SEPluginAdminServiceException e) {
             throw e;
             // We don't want to catch our own exception
         } catch (Exception e) {
             log.error("An error occured while getting Average Response Time");
             throw new SEPluginAdminServiceException("Unable to get Average Response Time", e);
         }
     }
 
    /**
      * Gets Average Request Time.
      *
      * @param mediatorUID mediator's UID
      * @return            Average Request Time
      * @throws SEPluginAdminServiceException
      */
     public double getAverageRequestTime(String mediatorUID) 
         throws SEPluginAdminServiceException {
         log.trace("Getting Average Request Time");
         initialize();
         if (!isConfigured()) {
             log.warn("Unable to get Mediator Statistics Monitor. Admin Service is not Configured");
             throw new SEPluginAdminServiceException("Unable to Return Response");
         }
         try {
             String mediatorStatisticsMonitorUID = getMediatorStatisticsMonitorUID(mediatorUID);
             if (mediatorStatisticsMonitorUID == null) {
                 log.error("Unable to retrieve Mediator Statistics Monitor for given Mediator UID");
                 throw new SEPluginAdminServiceException("Invalid Mediator Statistics Monitor UID");
             }
             UniquelyIdentifiableChild object = 
                 (UniquelyIdentifiableChild) mediatorStatisticsMonitorMap.get(mediatorStatisticsMonitorUID);
             if (object == null || !(object instanceof MonitoredStatisticActivity)) {
                 throw new SEPluginAdminServiceException("Invalid Mediator Statistics Monitor UID");
             }
             MonitoredStatisticActivity monitoredStatisticActivity = (MonitoredStatisticActivity)object;
             double response = monitoredStatisticActivity.getAvgRequestTime();
             log.trace("Successfully Got Average Request Time");
             return response;
         } catch (SEPluginAdminServiceException e) {
             throw e;
             // We don't want to catch our own exception
         } catch (Exception e) {
             log.error("An error occured while getting Average Request Time");
             throw new SEPluginAdminServiceException("Unable to get Average Request Time", e);
         }
     }
 
    /**
      * Gets load on mediator as a percentage of total load.
      *
      * @param mediatorUID mediator's UID
      * @return            load on mediator
      * @throws SEPluginAdminServiceException
      */
     public double getLoadOnMediator(String mediatorUID) 
         throws SEPluginAdminServiceException {
         log.trace("Getting Load on Mediator");
         initialize();
         if (!isConfigured()) {
             log.warn("Unable to get Mediator Statistics Monitor. Admin Service is not Configured");
             throw new SEPluginAdminServiceException("Unable to Return Response");
         }
         try {
             String mediatorStatisticsMonitorUID = getMediatorStatisticsMonitorUID(mediatorUID);
             if (mediatorStatisticsMonitorUID == null) {
                 log.error("Unable to retrieve Mediator Statistics Monitor for given Mediator UID");
                 throw new SEPluginAdminServiceException("Invalid Mediator Statistics Monitor UID");
             }
             UniquelyIdentifiableChild object = 
                 (UniquelyIdentifiableChild) mediatorStatisticsMonitorMap.get(mediatorStatisticsMonitorUID);
             if (object == null || !(object instanceof MonitoredStatisticActivity)) {
                 throw new SEPluginAdminServiceException("Invalid Mediator Statistics Monitor UID");
             }
             MonitoredStatisticActivity monitoredStatisticActivity = (MonitoredStatisticActivity)object;
             double response = monitoredStatisticActivity.getLoadAsPercentage();
             log.trace("Successfully Got Load on Mediator");
             return response;
         } catch (SEPluginAdminServiceException e) {
             throw e;
             // We don't want to catch our own exception
         } catch (Exception e) {
             log.error("An error occured while getting Load on Mediator");
             throw new SEPluginAdminServiceException("Unable to get Load on Mediator", e);
         }
     }
 
     /**
      * Gets Query Activity Monitor UID for given parent. The parent is a Mediator in this case
      * It is the responsibility of the caller to make sure that {@link #isConfigured} is checked
      * before calling this method.
      *
      * @param mediatorUID parent mediator's UID
      * @return            UID of Query Activity Monitor.
      * @throws SEPluginAdminServiceException
      */
     private String getQueryActivityMonitorUID(String mediatorUID) throws SEPluginAdminServiceException {
         log.trace("Fetching Query Activity Monitor UID");
         try {
             Map mediatorComponentMap = (Map) allMediatorMap.get(mediatorUID);
             if (mediatorComponentMap == null) {
                 throw new SEPluginAdminServiceException("Unable to fetch set of Mediator Components");
             }
             UniquelyIdentifiableChild object =
                 (UniquelyIdentifiableChild) mediatorComponentMap.get(QUERY_ACTIVITY_MONITOR_MAP_NAME);
             if (object == null) {
                 throw new SEPluginAdminServiceException("Unable to fetch Query Activity Monitor Component");
             }
             if (!mediatorUID.equals(object.getParentUID())) {
                 throw new SEPluginAdminServiceException("fetched Query Activity Monitor Component is invalid");
             }
             log.trace("Found Query Activity Monitor UID: " + object.getUID());
             return object.getUID();
         } catch (SEPluginAdminServiceException e) {
             throw e;
             // We don't want to catch our own exception
         } catch (Exception e) {
             log.error("An error occured while fetching Query Activity Monitor UIDs");
             throw new SEPluginAdminServiceException("Unable to get Query Activity Monitor UIDs", e);
         }
     }
 
    /**
      * Resets Query Activity Monitor.
      *
      * @param mediatorUID mediator's UID
      * @throws SEPluginAdminServiceException
      */
     public void resetQueryActivityMonitor(String mediatorUID) 
         throws SEPluginAdminServiceException {
         log.trace("Resetting Query Activity Monitor");
         initialize();
         if (!isConfigured()) {
             log.warn("Unable to get Query Activity Monitor. Admin Service is not Configured");
             return;
         }
         try {
             String queryActivityMonitorUID = getQueryActivityMonitorUID(mediatorUID);
             if (queryActivityMonitorUID == null) {
                 log.error("Unable to retrieve Query Activity Monitor for given Mediator UID");
                 throw new SEPluginAdminServiceException("Invalid Query Activity Monitor UID");
             }
             UniquelyIdentifiableChild object = 
                 (UniquelyIdentifiableChild) queryActivityMonitorMap.get(queryActivityMonitorUID);
             if (object == null || !(object instanceof ResetEnabled)) {
                 throw new SEPluginAdminServiceException("Invalid Query Activity Monitor UID");
             }
             ResetEnabled queryActivity = (ResetEnabled)object;
             queryActivity.reset();
             log.trace("Successfully Reset Query Activity Monitor");
         } catch (SEPluginAdminServiceException e) {
             throw e;
             // We don't want to catch our own exception
         } catch (Exception e) {
             log.error("An error occured while resetting Query Activity Monitor");
             throw new SEPluginAdminServiceException("Unable to reset Query Activity Monitor", e);
         }
     }
 
    /**
      * Browse Query Activities.
      *
      * @param mediatorUID mediator's UID
      * @return            array of query objects
      * @throws SEPluginAdminServiceException
      */
     public EPLQueryObject[] getQueryActivities(String mediatorUID) 
         throws SEPluginAdminServiceException {
         log.trace("Fetching Query Activity Objects");
         initialize();
         if (!isConfigured()) {
             log.warn("Unable to get Query Activity Monitor. Admin Service is not Configured");
             return null;
         }
         try {
             String queryActivityMonitorUID = getQueryActivityMonitorUID(mediatorUID);
             if (queryActivityMonitorUID == null) {
                 log.error("Unable to retrieve Query Activity Monitor for given Mediator UID");
                 throw new SEPluginAdminServiceException("Invalid Query Activity Monitor UID");
             }
             UniquelyIdentifiableChild object = 
                 (UniquelyIdentifiableChild) queryActivityMonitorMap.get(queryActivityMonitorUID);
             if (object == null || !(object instanceof Browsable)) {
                 throw new SEPluginAdminServiceException("Invalid Query Activity Monitor UID");
             }
             Browsable queryActivity = (Browsable)object;
             Map<String, EPLQueryObject> queryActivityObjects = (Map<String, EPLQueryObject>)queryActivity.browse();
             if (queryActivityObjects == null) {
                 log.warn("No Query Activity Objects were returned");
                 return null;
             }
             if (queryActivityObjects.size() == 0) {
                 return null;
             }
             EPLQueryObject[] response = queryActivityObjects.values().toArray(new EPLQueryObject[0]);
             if (response == null) {
                 throw new SEPluginAdminServiceException("Error while fetching Query Activity Objects");
             }
             log.trace("Successfully fetched " + queryActivityObjects.size() + " Query Activity Objects");
             return response;
         } catch (SEPluginAdminServiceException e) {
             throw e;
             // We don't want to catch our own exception
         } catch (Exception e) {
             log.error("An error occured while fetching Query Activity Objects");
             throw new SEPluginAdminServiceException("Unable to fetch Query Activity Objects", e);
         }
     }
 
     /**
      * Gets all Mediator statistics.
      *
      * @param mediatorUID mediator's UID
      * @return            object containing Mediator statistics
      * @throws SEPluginAdminServiceException
      */
     public AdminStatisticsObject getAllStatistics(String mediatorUID)
         throws SEPluginAdminServiceException {
         log.trace("Getting Mediator Statistics");
         initialize();
         if (!isConfigured()) {
             log.warn("Unable to get Mediator Statistics Monitor. Admin Service is not Configured");
             return null;
         }
         try {
             String mediatorStatisticsMonitorUID = getMediatorStatisticsMonitorUID(mediatorUID);
             if (mediatorStatisticsMonitorUID == null) {
                 log.error("Unable to retrieve Mediator Statistics Monitor for given Mediator UID");
                 throw new SEPluginAdminServiceException("Invalid Mediator Statistics Monitor UID");
             }
             UniquelyIdentifiableChild object =
                 (UniquelyIdentifiableChild) mediatorStatisticsMonitorMap.get(mediatorStatisticsMonitorUID);
             if (object == null || !(object instanceof MonitoredStatisticActivity)) {
                 throw new SEPluginAdminServiceException("Invalid Mediator Statistics Monitor UID");
             }
             MonitoredStatisticActivity monitoredStatisticActivity = (MonitoredStatisticActivity)object;
             String mediatorActivityMonitorUID = getMediatorActivityMonitorUID(mediatorUID);
             if (mediatorActivityMonitorUID == null) {
                 log.error("Unable to retrieve Mediator Activity Monitor for given Mediator UID");
                 throw new SEPluginAdminServiceException("Invalid Mediator Activity Monitor UID");
             }
             object = (UniquelyIdentifiableChild) mediatorActivityMonitorMap.get(mediatorActivityMonitorUID);
             if (object == null || !(object instanceof MonitoredMediatorActivity)) {
                 throw new SEPluginAdminServiceException("Invalid Mediator Activity Monitor UID");
             }
             MonitoredMediatorActivity monitoredMediatorActivity = (MonitoredMediatorActivity)object;
             AdminStatisticsObject response = null;
             response = new AdminStatisticsObject(monitoredStatisticActivity.getMaxRequestTime(),
                 monitoredStatisticActivity.getMaxResponseTime(), monitoredStatisticActivity.getMinRequestTime(),
                 monitoredStatisticActivity.getMinResponseTime(), monitoredStatisticActivity.getAvgRequestTime(),
                 monitoredStatisticActivity.getAvgResponseTime(), monitoredStatisticActivity.getLoadAsPercentage(),
                 monitoredMediatorActivity.getIsMediatorActive(), monitoredMediatorActivity.getActiveTime(),
                 monitoredMediatorActivity.getBaseType(), monitoredMediatorActivity.getBaseAlias()); 
             if (response != null) {
                 log.trace("Successfully Got Mediator Statistics");
             } else {
                 log.error("Mediator Statistics Object Creation Failed");
             }
             return response;
         } catch (SEPluginAdminServiceException e) {
             throw e;
             // We don't want to catch our own exception
         } catch (Exception e) {
             log.error("An error occured while getting Mediator Statistics");
             throw new SEPluginAdminServiceException("Unable to get Mediator Statistics", e);
         }
     }
 
     /**
      * Resets all Mediator Timers and Counters.
      *
      * @param mediatorUID mediator's UID
      * @throws SEPluginAdminServiceException
      */
     public void resetAll(String mediatorUID) throws SEPluginAdminServiceException {
         resetMediatorActivityTimer(mediatorUID);
         resetMediatorStatisticsMonitor(mediatorUID);
         resetQueryActivityMonitor(mediatorUID);
     }
 
 }
