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
 
 package org.sciflex.plugins.synapse.esper.core.admin;
 
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.axis2.context.MessageContext;
 import org.apache.axis2.engine.AxisConfiguration;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import org.sciflex.plugins.synapse.esper.core.activities.MonitoredMediatorActivity;
 import org.sciflex.plugins.synapse.esper.core.util.UniquelyIdentifiableChild;
 import org.sciflex.plugins.synapse.esper.ui.Browsable;
 import org.sciflex.plugins.synapse.esper.ui.Editable;
 
 /*import org.wso2.carbon.registry.core.Registry;*/
 
 /**
  * Admin Service for SCI-Flex Synapse Esper Plugin.
  */
 public class SEPluginAdminService extends AdminComponentServiceConstants {
 
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
 
     /*protected final Registry registry;*/
     /*private static final String REGISTRY_INSTANCE = "WSO2Registry";*/
 
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
         } else {
             axisConfig = null;
             log.fatal("Unable to Setup Service Instance");
             throw new SEPluginAdminServiceException();
         }
        log.info("Creating Service Instance");
         initialize();
         // Optional code block if required to add a reference to the registry some day.
         /*if (this.axisConfig != null) {
             Object regParamValue = this.axisConfig.getParameterValue(REGISTRY_INSTANCE);
             if (regParamValue != null && regParamValue instanceof Registry) {
                 this.registry = (Registry) regParamValue;
             } else {
                 this.registry = null;
             }
         } else {
             this.registry = null;
         }*/
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
                log.info("Initializing Service");
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
                         log.info("Service Successfully Initialized");
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
             if (object == null || !(object instanceof Browsable)) {
                 throw new SEPluginAdminServiceException("Invalid Dynamic EPL Editor UID");
             }
             Browsable browsableDynamicEPL = (Browsable)object;
             String response = (String) browsableDynamicEPL.browse();
             log.trace("Found Dynamic EPL: " + response);
             return response;
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
         } catch (Exception e) {
             log.error("An error occured while refreshing Mediator");
             throw new SEPluginAdminServiceException("Unable to refresh Mediator", e);
         }
     }
 
 }
