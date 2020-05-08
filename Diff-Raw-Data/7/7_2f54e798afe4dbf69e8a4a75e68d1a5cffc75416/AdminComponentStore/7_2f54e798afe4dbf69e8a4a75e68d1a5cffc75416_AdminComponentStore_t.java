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
 
 package org.sciflex.plugins.synapse.esper.mediators.helpers;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.apache.axis2.AxisFault;
 import org.apache.axis2.engine.AxisConfiguration;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.synapse.MessageContext;
 import org.apache.synapse.registry.Registry;
 
 import org.sciflex.plugins.synapse.esper.core.util.UniquelyIdentifiable;
 import org.sciflex.plugins.synapse.esper.core.util.UniquelyIdentifiableChild;
 import org.sciflex.plugins.synapse.esper.mediators.SynapseEsperMediator;
 import org.sciflex.plugins.synapse.esper.mediators.editors.CEPInstanceEditor;
 import org.sciflex.plugins.synapse.esper.mediators.editors.DynamicEPLEditor;
 import org.sciflex.plugins.synapse.esper.mediators.editors.RegistryKeyEditor;
 import org.sciflex.plugins.synapse.esper.mediators.editors.StaticEPLEditor;
 import org.sciflex.plugins.synapse.esper.mediators.helpers.EPLStatementHelper;
 import org.sciflex.plugins.synapse.esper.mediators.monitors.MediatorActivityMonitor;
 import org.sciflex.plugins.synapse.esper.mediators.monitors.MediatorStatisticsMonitor;
 import org.sciflex.plugins.synapse.esper.mediators.monitors.QueryActivityMonitor;
 
 /**
  * The Admin Component Store is capable of stroring a set of admin
  * components associated with the {@link SynapseEsperMediator}.
  */
 public class AdminComponentStore extends AdminComponentStoreConstants 
     implements InvokableHelper, ComponentStore {
 
     /**
      * Log associated with the EPL Statement Helper.
      */
     private static final Log log = LogFactory.getLog(AdminComponentStore.class);
 
     /**
      * Map of mediators
      */
     private static final Map ALL_MEDIATOR_MAP = new HashMap<String, UniquelyIdentifiable>();
 
     /**
      * Map of mediator components
      */
     private final Map MEDIATOR_COMPONENT_MAP = new HashMap<String, UniquelyIdentifiableChild>();
 
     /**
      * Map of Dynamic EPL Editors
      */
     private static final Map DYNAMIC_EPL_EDITOR_MAP = new HashMap<String, UniquelyIdentifiableChild>();
 
     /**
      * Map of Static EPL Editors
      */
     private static final Map STATIC_EPL_EDITOR_MAP = new HashMap<String, UniquelyIdentifiableChild>();
 
     /**
      * Map of CEP Instance Editors
      */
     private static final Map CEP_INSTANCE_EDITOR_MAP = new HashMap<String, UniquelyIdentifiableChild>();
 
     /**
      * Map of Registry Key Editors
      */
     private static final Map REGISTRY_KEY_EDITOR_MAP = new HashMap<String, UniquelyIdentifiableChild>();
 
     /**
      * Map of Query Activity Monitors
      */
     private static final Map QUERY_ACTIVITY_MONITOR_MAP = new HashMap<String, UniquelyIdentifiableChild>();
 
     /**
      * Map of Mediator Activity Monitors
      */
     private static final Map MEDIATOR_ACTIVITY_MONITOR_MAP = new HashMap<String, UniquelyIdentifiableChild>();
 
     /**
      * Map of Mediator Statistics Monitors
      */
     private static final Map MEDIATOR_STATISTICS_MONITOR_MAP = new HashMap<String, UniquelyIdentifiableChild>();
 
     /**
      * Indicates whether the one-time configuration has been done or not.
      */
    private static boolean isConfigured = false;
 
     /**
      * Associated {@link EPLStatementHelper} instance
      */
     private EPLStatementHelper eplStatementHelper = null;
 
     /**
      * Associated {@link SynapseEsperMediator} instance
      */
     private SynapseEsperMediator mediator = null;
 
     /**
      * States whether a Dynamic EPL Editor is set.
      */
     private boolean isDynamicEPLEditorSet = false;
 
     /**
      * Dynamic EPL Editor
      */
     private UniquelyIdentifiableChild dee = null;
 
     /**
      * Static EPL Editor
      */
     private UniquelyIdentifiableChild see = null;
 
     /**
      * Registry Key Editor
      */
     private UniquelyIdentifiableChild rke = null;
 
     /**
      * Query Activity Monitors
      */
     private UniquelyIdentifiableChild qam = null;
 
     /**
      * Changes {@link EPLStatementHelper} instance
      *
      * @param eplStatementHelper new {@link EPLStatementHelper} instance
      */
     public synchronized void changeEPLStatementHelper(EPLStatementHelper eplStatementHelper) {
         if (eplStatementHelper == null || this.eplStatementHelper == eplStatementHelper) {
             return;
         }
         this.eplStatementHelper = eplStatementHelper;
         if (qam != null) {
             removeUniquelyIdentifiableChild(qam);
         }
         qam = eplStatementHelper.getQueryActivity();
         if (rke != null) {
             removeUniquelyIdentifiableChild(rke);
         }
         rke = new RegistryKeyEditor(eplStatementHelper);
         if (see != null) {
             removeUniquelyIdentifiableChild(see);
         }
         see = new StaticEPLEditor(eplStatementHelper);
         if (dee != null) {
             removeUniquelyIdentifiableChild(dee);
         }
         isDynamicEPLEditorSet = false;
         addUniquelyIdentifiableChild(qam);
         addUniquelyIdentifiableChild(rke);
         addUniquelyIdentifiableChild(see);
     }
 
     /**
      * Constructor accepting {@link SynapseEsperMediator}.
      * 
      * @param mediator {@link SynapseEsperMediator} instance.
      */
     public AdminComponentStore(SynapseEsperMediator mediator) {
         this.mediator = mediator;
         if (mediator != null) {
             ALL_MEDIATOR_MAP.put(mediator.getUID(), MEDIATOR_COMPONENT_MAP);
         }
     }
 
     /**
      * Adds Uniquely Identifiable Component.
      *
      * @param component Uniquely Identifiable Component.
      */
     public synchronized void addUniquelyIdentifiableChild(UniquelyIdentifiableChild component) {
         Map map = null;
         if (component.getParentUID() != null) {
             map = (Map) ALL_MEDIATOR_MAP.get(component.getParentUID());
             if (map == null) {
                 return;
             }
         } else if (mediator == null) {
             return;
         } else {
             map = MEDIATOR_COMPONENT_MAP;
             component.setParentUID(mediator.getUID());
         }
         map.put(component.getUID(), component);
         if (component instanceof DynamicEPLEditor) {
             DYNAMIC_EPL_EDITOR_MAP.put(component.getUID(), component);
         } else if (component instanceof StaticEPLEditor) {
             STATIC_EPL_EDITOR_MAP.put(component.getUID(), component);
         } else if (component instanceof RegistryKeyEditor) {
             REGISTRY_KEY_EDITOR_MAP.put(component.getUID(), component);
         } else if (component instanceof CEPInstanceEditor) {
             CEP_INSTANCE_EDITOR_MAP.put(component.getUID(), component);
         } else if (component instanceof QueryActivityMonitor) {
             QUERY_ACTIVITY_MONITOR_MAP.put(component.getUID(), component);
         } else if (component instanceof MediatorActivityMonitor) {
             MEDIATOR_ACTIVITY_MONITOR_MAP.put(component.getUID(), component);
         } else if (component instanceof MediatorStatisticsMonitor) {
             MEDIATOR_STATISTICS_MONITOR_MAP.put(component.getUID(), component);
         }
     }
 
     /**
      * Removes Uniquely Identifiable Component.
      *
      * @param component Uniquely Identifiable Component.
      */
     public synchronized void removeUniquelyIdentifiableChild(UniquelyIdentifiableChild component) {
         Map map = null;
         if (component.getParentUID() != null) {
             map = (Map) ALL_MEDIATOR_MAP.get(component.getParentUID());
             if (map == null) {
                 return;
             }
         } else if (mediator == null) {
             return;
         } else {
             map = MEDIATOR_COMPONENT_MAP;
             component.setParentUID(mediator.getUID());
         }
         map.remove(component.getUID());
         if (component instanceof DynamicEPLEditor) {
             DYNAMIC_EPL_EDITOR_MAP.remove(component.getUID());
         } else if (component instanceof StaticEPLEditor) {
             STATIC_EPL_EDITOR_MAP.remove(component.getUID());
         } else if (component instanceof RegistryKeyEditor) {
             REGISTRY_KEY_EDITOR_MAP.remove(component.getUID());
         } else if (component instanceof CEPInstanceEditor) {
             CEP_INSTANCE_EDITOR_MAP.remove(component.getUID());
         } else if (component instanceof QueryActivityMonitor) {
             QUERY_ACTIVITY_MONITOR_MAP.remove(component.getUID());
         } else if (component instanceof MediatorActivityMonitor) {
             MEDIATOR_ACTIVITY_MONITOR_MAP.remove(component.getUID());
         } else if (component instanceof MediatorStatisticsMonitor) {
             MEDIATOR_STATISTICS_MONITOR_MAP.remove(component.getUID());
         }
     }
 
     /**
      * Configure {@link AdminComponentStore} just once
      * @param mc Synapse Message Context
      */
     private void configure(MessageContext mc) {
         // this function will run only once
         isConfigured = true;
         Map componentRoot = new HashMap<String, Map>();
         componentRoot.put(ALL_MEDIATOR_MAP_NAME, ALL_MEDIATOR_MAP);
         componentRoot.put(MEDIATOR_COMPONENT_MAP_NAME, MEDIATOR_COMPONENT_MAP);
         componentRoot.put(DYNAMIC_EPL_EDITOR_MAP_NAME, DYNAMIC_EPL_EDITOR_MAP);
         componentRoot.put(STATIC_EPL_EDITOR_MAP_NAME, STATIC_EPL_EDITOR_MAP);
         componentRoot.put(CEP_INSTANCE_EDITOR_MAP_NAME, CEP_INSTANCE_EDITOR_MAP);
         componentRoot.put(REGISTRY_KEY_EDITOR_MAP_NAME, REGISTRY_KEY_EDITOR_MAP);
         componentRoot.put(QUERY_ACTIVITY_MONITOR_MAP_NAME, QUERY_ACTIVITY_MONITOR_MAP);
         componentRoot.put(MEDIATOR_ACTIVITY_MONITOR_MAP_NAME, MEDIATOR_ACTIVITY_MONITOR_MAP);
         componentRoot.put(MEDIATOR_STATISTICS_MONITOR_MAP_NAME, MEDIATOR_STATISTICS_MONITOR_MAP);
         if (mc != null && mc.getConfiguration() != null) {
            AxisConfiguration conf = mc.getConfiguration().getAxisConfiguration();
             if (conf == null) {
                 return;
             }
             try {
                 conf.addParameter(COMPONENT_ROOT_MAP_NAME, componentRoot);
             } catch (AxisFault e) {
                 log.fatal("Unable to add Admin Components to Server Description: " +
                     e.getMessage());
             }
         }
     }
 
 
 
     /**
      * Invokes EPL Statement Helper before mediating the current message.
      * @param mc Message Context of the current message.
      */
     public void invoke(MessageContext mc) {
         if (!isConfigured) {
             configure(mc);
         } else if (isDynamicEPLEditorSet) {
             return;
         } else if (mc != null && mc.getConfiguration() != null) {
             dee = new DynamicEPLEditor(eplStatementHelper, 
                 mc.getConfiguration().getRegistry());
             addUniquelyIdentifiableChild(dee);
         }
         isDynamicEPLEditorSet = true;
     }
 }
