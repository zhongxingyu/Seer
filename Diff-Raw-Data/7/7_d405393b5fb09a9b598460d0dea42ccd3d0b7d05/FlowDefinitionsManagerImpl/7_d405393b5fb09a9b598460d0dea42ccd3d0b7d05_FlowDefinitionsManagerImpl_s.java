 /*
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy
  * of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software distributed
  * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
  * OR CONDITIONS OF ANY KIND, either express or implied. See the License for
  * the specific language governing permissions and limitations under the
  * License.
  */
 
 package org.amplafi.flow.impl;
 
 import java.util.Collection;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ConcurrentMap;
 import java.util.concurrent.CopyOnWriteArrayList;
 
 import org.amplafi.flow.Flow;
 import org.amplafi.flow.FlowDefinitionsManager;
 import org.amplafi.flow.FlowImplementor;
 import org.amplafi.flow.FlowTranslatorResolver;
 import org.amplafi.flow.definitions.DefinitionSource;
 import org.amplafi.flow.definitions.FlowFromFlowPropertyDefinitionDefinitionSource;
 import org.amplafi.flow.definitions.XmlDefinitionSource;
 import org.amplafi.flow.flowproperty.FlowPropertyDefinitionBuilder;
 import org.amplafi.flow.flowproperty.FlowPropertyDefinitionProvider;
 import org.amplafi.flow.validation.FlowValidationException;
 import org.amplafi.flow.validation.MissingRequiredTracking;
 
 import com.sworddance.util.ApplicationIllegalArgumentException;
 import com.sworddance.util.ApplicationIllegalStateException;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 /**
  * Basic implementation for managing Flow definitions. Provides no persistence mechanism.
  *
  */
 public class FlowDefinitionsManagerImpl implements FlowDefinitionsManager {
     private FlowTranslatorResolver flowTranslatorResolver;
     private ConcurrentMap<String, FlowImplementor> flowDefinitions;
     private List<String> flowsFilenames;
 
     private List<FlowPropertyDefinitionProvider> factoryFlowPropertyDefinitionProviders;
 
     private Log log;
     public FlowDefinitionsManagerImpl() {
         flowDefinitions = new ConcurrentHashMap<String, FlowImplementor>();
         flowsFilenames = new CopyOnWriteArrayList<String>();
         factoryFlowPropertyDefinitionProviders = new CopyOnWriteArrayList<FlowPropertyDefinitionProvider>();
         this.addFactoryFlowPropertyDefinitionProvider(FactoryFlowPropertyDefinitionProvider.FLOW_INSTANCE);
         this.addFactoryFlowPropertyDefinitionProvider(FactoryFlowPropertyDefinitionProvider.FLOW_ACTIVITY_INSTANCE);
     }
 
     /**
      *
      */
     public void initializeService() {
         RuntimeException storedExceptions = null;
         for(String fileName: getFlowsFilenames()) {
             try {
                 XmlDefinitionSource definitionSource = new XmlDefinitionSource(fileName);
                 addDefinitions(definitionSource);
             } catch(IllegalArgumentException e) {
                 if ( storedExceptions == null) {
                     storedExceptions = e;
                 } else {
                     storedExceptions.addSuppressed(e);
                 }
                 getLog().error("Problem reading flow definitions in file '"+fileName+"'", e);
             }
         }
         if(storedExceptions !=null ) {
             throw storedExceptions;
         }
     }
 
     @Override
     public void addDefinitions(DefinitionSource... definitionSources) {
         for(DefinitionSource<FlowImplementor> definitionSource: definitionSources) {
             Collection<FlowImplementor> flows = definitionSource.getFlowDefinitions().values();
             for(FlowImplementor flow: flows) {
                 addDefinition(flow);
             }
         }
     }
     @Override
     public void addDefinition(FlowImplementor flow) {
         ApplicationIllegalStateException.checkState(!flow.isInstance(), flow, " is an instance not a definition");
         getFlowDefinitions().put(flow.getFlowPropertyProviderFullName(), flow);
     }
 
     /**
      * @see org.amplafi.flow.FlowDefinitionsManager#getFlowDefinition(java.lang.String)
      */
     @Override
     public FlowImplementor getFlowDefinition(String flowTypeName) {
         ApplicationIllegalArgumentException.notNull(flowTypeName, "null flowTypeName");
         FlowImplementor flow = this.getFlowDefinitions().get(flowTypeName);
         if (flow==null) {
             flow = this.getFlowDefinitions().get(flowTypeName+FlowFromFlowPropertyDefinitionDefinitionSource.FLOW_PREFIX);
         }
         if ( flow == null) {
             throw new FlowValidationException(null, "flow.definition-not-found", new MissingRequiredTracking(flowTypeName));
         } else {
             // cannot do this any more at initializeService() time because of infinite-loop.
             // BaseFlowTranslatorResolver.resolveFlow() calls back to FlowDefinitionManager
             this.flowTranslatorResolver.resolveFlow(flow);
         }
         return flow;
     }
 
     /**
      * @see org.amplafi.flow.FlowDefinitionsManager#getFlowDefinitions()
      */
     @Override
     public Map<String, FlowImplementor> getFlowDefinitions() {
         return this.flowDefinitions;
     }
 
     public Log getLog() {
         if ( this.log == null ) {
             this.log = LogFactory.getLog(this.getClass());
         }
         return this.log;
     }
 
     @Override
     public void addFactoryFlowPropertyDefinitionProvider(FlowPropertyDefinitionProvider factoryFlowPropertyDefinitionProvider) {
        addAllIfNotContains(this.factoryFlowPropertyDefinitionProviders, factoryFlowPropertyDefinitionProvider);
     }
     @Override
     public void addFactoryFlowPropertyDefinitionProviders(Collection<FlowPropertyDefinitionProvider> factoryFlowPropertyDefinitionProviders) {
        addAllIfNotContains(this.factoryFlowPropertyDefinitionProviders, factoryFlowPropertyDefinitionProviders);
     }
 
     @Override
     public FlowPropertyDefinitionBuilder getFactoryFlowPropertyDefinitionBuilder(String propertyName, Class<?> dataClass) {
         FlowPropertyDefinitionBuilder flowPropertyDefinitionBuilder = null;
         for(FlowPropertyDefinitionProvider flowPropertyDefinitionProvider: this.factoryFlowPropertyDefinitionProviders) {
             flowPropertyDefinitionBuilder = flowPropertyDefinitionProvider.getFlowPropertyDefinitionBuilder(propertyName, dataClass);
             if ( flowPropertyDefinitionBuilder != null ) {
                 break;
             }
         }
         return flowPropertyDefinitionBuilder;
     }
 
     /**
      * @see org.amplafi.flow.FlowDefinitionsManager#isFlowDefined(java.lang.String)
      */
     @Override
     public boolean isFlowDefined(String flowTypeName) {
         Flow flow = this.getFlowDefinitions().get(flowTypeName);
         return flow != null;
     }
     public void setFlowTranslatorResolver(FlowTranslatorResolver flowTranslatorResolver) {
         this.flowTranslatorResolver = flowTranslatorResolver;
     }
     public FlowTranslatorResolver getFlowTranslatorResolver() {
         return flowTranslatorResolver;
     }
 
     /**
      * @param flowsFilenames the flowsFilenames to set
      */
     public void setFlowsFilenames(List<String> flowsFilenames) {
         this.flowsFilenames.clear();
         this.flowsFilenames.addAll(flowsFilenames);
     }
 
     /**
      * @return the flowsFilenames
      */
     public List<String> getFlowsFilenames() {
         return flowsFilenames;
     }
 }
