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
 package org.amplafi.flow.launcher;
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.amplafi.flow.FlowException;
import org.amplafi.flow.FlowExecutionException;
 import org.amplafi.flow.FlowManagement;
 import org.amplafi.flow.FlowState;
 import org.apache.commons.lang.StringUtils;
 
 import com.sworddance.util.NotNullIterator;
 
 import static com.sworddance.util.CUtilities.*;
 
 /**
  * {@link FlowLauncher} that starts a new {@link FlowState}.
  * TODO:
  * Check login case. evaluatedValues has email and password repeated
  * @author Patrick Moore
  */
 public class StartFromDefinitionFlowLauncher extends BaseFlowLauncher implements ListableFlowLauncher {
     private static final long serialVersionUID = 7909909329479094947L;
     /**
      * A list of strings in the form:
      *  "flowPropertyDefinitionName=valueSource"
      *  or
      *  "flowPropertyDefinitionName" (which is equivalent to the first form: "flowPropertyDefinitionName=flowPropertyDefinitionName" )
      *
      *
      * When the flow is launched with {@link #call()}, if propertyRoot != null, then the value returned by the {@link FlowManagement#getValueFromBindingProvider()}
      * is put in the initialStateMap for the flow initialization.
      *
      * If propertyRoot == null or
      * the result of the ValueFromBinding is null (only if the string was not the short hand form "flowPropertyDefinitionName" )
      * then valueSource is treated as a literal string.
      *
      */
     private List<String> evaluatedValues;
     private transient Object propertyRoot;
 
     public StartFromDefinitionFlowLauncher() {
 
     }
     public StartFromDefinitionFlowLauncher(String flowTypeName, FlowManagement flowManagement) {
         this(flowTypeName, null, flowManagement);
     }
     public StartFromDefinitionFlowLauncher(String flowTypeName, Map<String, String> initialFlowState) {
         this(flowTypeName, initialFlowState, null, flowTypeName);
     }
     public StartFromDefinitionFlowLauncher(String flowTypeName, Map<String, String> initialFlowState,
             FlowManagement flowManagement) {
         this(flowTypeName, initialFlowState, flowManagement, flowTypeName);
     }
 
     public StartFromDefinitionFlowLauncher(String flowTypeName, Map<String, String> initialFlowState,
             FlowManagement flowManagement, Serializable keyExpression) {
         super(flowTypeName, flowManagement, initialFlowState, keyExpression);
     }
     public StartFromDefinitionFlowLauncher(String flowTypeName, Map<String, String> initialFlowState, Serializable keyExpression) {
         super(flowTypeName, initialFlowState, keyExpression);
     }
     /**
     *
     * @param flowTypeName
     * @param initialFlowState map of strings - not evaluated like evaluatedValues is.
     * @param flowManagement
     * @param keyExpression for html rendering identification.
     * @param propertyRoot must be provided if the evaluatedValues need to be evaluated when the flow is launched.
     * @param overridingValues used to define the initial values for flow. This is a
     * list of strings. Each string is 'key=value'. if value is the same name as a component
     * that has a 'value' attribute (like TextField components) then the initial value.
     * If value is a container's property then that value is used. Otherwise the value
     * provided is used as a literal.
     */
     public StartFromDefinitionFlowLauncher(String flowTypeName, Map<String, String> initialFlowState, FlowManagement flowManagement,
             Serializable keyExpression, Object propertyRoot, Iterable<String> overridingValues) {
         this(flowTypeName, initialFlowState, flowManagement, keyExpression);
         this.setEvaluatedValues(overridingValues);
         this.propertyRoot = propertyRoot;
     }
 
     public StartFromDefinitionFlowLauncher(String flowTypeName, Iterable<String> initialFlowState, FlowManagement flowManagement,
         Serializable keyExpression, Object propertyRoot, Iterable<String> evaluatedValues) {
         this(flowTypeName, null, flowManagement, keyExpression);
         this.setEvaluatedValues(evaluatedValues);
         this.setInitialFlowState(initialFlowState);
         this.propertyRoot = propertyRoot;
     }
 
     private void setInitialFlowState(Iterable<String> initialFlowState) {
         if (isNotEmpty( initialFlowState)) {
             Map<String, String> staticInitialFlowState = convertToMap(null, initialFlowState);
             this.putAll(staticInitialFlowState);
         }
     }
     @Override
     public FlowState call() {
         Map<String, String> launchMap = getInitialFlowState();
         try {
             FlowState flowState = getFlowManagementWithCheck().startFlowState(getFlowTypeName(), true, launchMap, getReturnToFlow());
             return flowState;
         } catch(FlowException e) {
         	throw e;
         } catch(RuntimeException e) {
            throw new FlowExecutionException("While trying to start flow="+getFlowTypeName()+"; launchMap="+launchMap, e);
         }
     }
     @Override
     public Map<String, String> getInitialFlowState() {
         Map<String,String> launchMap = new LinkedHashMap<String, String>();
         launchMap.putAll(super.getInitialFlowState());
         if(this.evaluatedValues != null && !this.evaluatedValues.isEmpty()) {
             launchMap.putAll(convertToMap(this.getPropertyRoot(), this.getEvaluatedValues()));
         }
         return launchMap;
     }
 
     public void setPropertyRoot(Object propertyRoot) {
         this.propertyRoot = propertyRoot;
     }
     public Object getPropertyRoot() {
         return propertyRoot;
     }
     private void setEvaluatedValues(Iterable<String> evaluatedValues) {
         this.evaluatedValues = new ArrayList<String>();
         if ( evaluatedValues != null ) {
             NotNullIterator.notNullAdd(this.evaluatedValues, evaluatedValues.iterator());
         }
     }
     public StartFromDefinitionFlowLauncher addEvaluatedValues(Iterable<String> additionalEvaluatedValues) {
         if ( isEmpty(this.evaluatedValues)) {
             setEvaluatedValues(additionalEvaluatedValues);
         } else if ( additionalEvaluatedValues != null ) {
             NotNullIterator.notNullAdd(this.evaluatedValues, additionalEvaluatedValues.iterator());
         }
         return this;
     }
     public Iterable<String> getEvaluatedValues() {
         return evaluatedValues;
     }
     /**
      * @see org.amplafi.flow.launcher.BaseFlowLauncher#getFlowState()
      */
     @Override
     protected <FS extends FlowState> FS getFlowState() {
         return null;
     }
 
     /**
      * @param propertyRoot
      * @param evaluatedValues
      * @return
      */
     private Map<String, String> convertToMap(Object evaluationRoot, Iterable<String> keyValueList) {
         Map<String, String> initialMap = new HashMap<String, String>();
         if ( keyValueList != null) {
             final ValueFromBindingProvider valueFromBindingProvider = getValueFromBindingProvider();
 
             for(String entry: NotNullIterator.<String>newNotNullIterator(keyValueList)) {
                 String[] v = entry.split("=", 2);
                 String key = v[0].trim();
                 String lookup;
                 Object value;
                 if ( v.length < 2 ) {
                     // shorthand form (default value is null if no valueFromBindingProvider)
                     lookup = key;
                     value = null;
                 } else {
                     // long form: default value is the other side of equals
                     value = lookup = v[1];
                 }
                 if ( valueFromBindingProvider != null) {
                     value = valueFromBindingProvider.getValueFromBinding(evaluationRoot, lookup);
                 }
                 if ( value != null) {
                     initialMap.put(key, value.toString());
                 }
             }
         }
         return initialMap;
     }
     private ValueFromBindingProvider getValueFromBindingProvider() {
         return this.getFlowManagementWithCheck().getValueFromBindingProvider();
     }
     @Override
     public String toString() {
         return super.toString()+ " evaluatedValues=["+ StringUtils.join(this.evaluatedValues,",")+"]";
     }
 }
