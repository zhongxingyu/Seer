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
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.NoSuchElementException;
 
 import static com.sworddance.util.CUtilities.*;
 
 import org.amplafi.flow.Flow;
 import org.amplafi.flow.FlowActivity;
 import org.amplafi.flow.FlowActivityImplementor;
 import org.amplafi.flow.FlowActivityPhase;
 import org.amplafi.flow.FlowImplementor;
 import org.amplafi.flow.FlowManagement;
 import org.amplafi.flow.FlowPropertyDefinition;
 import org.amplafi.flow.FlowState;
 import org.amplafi.flow.FlowStateLifecycle;
 import org.amplafi.flow.FlowStepDirection;
 import org.amplafi.flow.FlowValueMapKey;
 import org.amplafi.flow.FlowValuesMap;
 import org.amplafi.flow.flowproperty.FlowPropertyDefinitionBuilder;
 import org.amplafi.flow.flowproperty.FlowPropertyDefinitionImplementor;
 import org.amplafi.flow.flowproperty.FlowPropertyProvider;
 import org.amplafi.flow.flowproperty.FlowPropertyValueChangeListener;
 import org.amplafi.flow.flowproperty.InvalidatingFlowPropertyValueChangeListener;
 import org.amplafi.flow.flowproperty.PropertyUsage;
 import org.amplafi.flow.validation.FlowValidationException;
 import org.amplafi.flow.validation.FlowValidationResult;
 import org.amplafi.flow.validation.ReportAllValidationResult;
 import org.amplafi.json.JSONWriter;
 
 import com.sworddance.util.ApplicationNullPointerException;
 import com.sworddance.util.NotNullIterator;
 import com.sworddance.util.RandomKeyGenerator;
 import com.sworddance.util.map.NamespaceMapKey;
 import com.sworddance.util.perf.LapTimer;
 
 import org.apache.commons.collections.map.MultiKeyMap;
 import org.apache.commons.lang.ObjectUtils;
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.logging.Log;
 
 import static org.amplafi.flow.FlowConstants.*;
 import static org.amplafi.flow.FlowStateLifecycle.*;
 import static org.apache.commons.lang.StringUtils.isNotBlank;
 import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.isNumeric;
 
 
 /**
  * Application State Object that tracks the current state of a flow. Holds any
  * state information related to a specific flow
  *
  * Each flow state has all the information to run the flow and re-enter it if
  * needed.
  *
  * defines an actively executing flow. Each FlowState has an attached Flow which
  * is the instantiated definition. This copy is made to avoid problems with flow
  * definitions changing while an instance of a flow is active.
  */
 public class FlowStateImpl implements FlowStateImplementor {
 
     private static final long serialVersionUID = -7694935572121566257L;
 
     /**
      * used when displaying the FlowEntryPoint.
      */
     private String activeFlowLabel;
 
     /**
      * unique flow id so that flows can be started, stopped, restarted easily.
      * Important! This key must be immutable as it is the key or part of the key for many for namespace lookups and maps.
      */
     private String lookupKey;
 
     /**
      * key, value pairs that will be used to hold the current state of the flow.
      * The values here should be fairly lightweight.
      */
     protected FlowValuesMap flowValuesMap;
 
     private String flowTypeName;
 
     protected transient FlowImplementor flow;
 
     /**
      * index into flow.activities.
      */
     private Integer currentActivityIndex;
 
     /**
      * to be used when advancing flow to a fixed step. Use case: changing
      * flowtype.
      */
     private String currentActivityByName;
 
     /**
      * flowManagement instance that this FlowState is attached to.
      */
     private transient FlowManagement flowManagement;
 
     /**
      * a map of values that the current components have placed here for the
      * FlowActivity and Flow definitions to use for processing this request.
      * This map only contains values up until the completion of the
      * selectActivity() call
      */
     private transient MultiKeyMap cachedValues;
 
     private FlowStateLifecycle flowStateLifecycle;
 
     private List<FlowPropertyValueChangeListener> globalFlowPropertyValueChangeListeners = new ArrayList<FlowPropertyValueChangeListener>(Arrays.asList(new InvalidatingFlowPropertyValueChangeListener()));
 
     public FlowStateImpl() {
 
     }
 
     public FlowStateImpl(String flowTypeName, FlowManagement sessionFlowManagement,
             Map<String, String> initialFlowState) {
         this(flowTypeName, sessionFlowManagement);
         //TODO Kostya: Should we put initial state into the flow namespace??
         this.setFlowValuesMap(new DefaultFlowValuesMap(initialFlowState));
     }
 
     public FlowStateImpl(String flowTypeName, FlowManagement sessionFlowManagement) {
         this();
         this.flowTypeName = flowTypeName;
         this.flowManagement = sessionFlowManagement;
         this.lookupKey = createLookupKey();
         this.setFlowLifecycleState(created);
     }
 
     // HACK : TODO lookupKey injection
     private String createLookupKey() {
         return this.flowTypeName +"_"+ new RandomKeyGenerator(8).nextKey().toString();
     }
 
     @Override
     public String begin() {
         FlowStateLifecycle lifecycle = this.getFlowStateLifecycle();
         if ( lifecycle != null ) {
             switch( lifecycle ) {
             case initialized:
                 // normal case continue with begin
                 break;
             case created:
                 // o.k. behavior - not initialized yet so do the initialization.
                 this.initializeFlow();
                 break;
             case initializing:
                 // begin() within an initializing ??? very odd. for now just log and return
                 getLog().debug(this+": begin() called but state is "+lifecycle);
                 return null;
             case started:
                 // double begin() called. slightly bad coding but o.k. otherwise
                 return this.getCurrentPage();
             case starting:
                 // nested begin()'s also odd but for now just log and return.
                 getLog().debug(this+": begin() called but state is "+lifecycle);
                 return getCurrentPage();
             default:
                 // the flowState has completed -- trying to restart is not supported.
                 getLog().debug(this+": begin() called but flow completed already state is "+lifecycle);
                 return getCurrentPage();
             }
         }
         this.setFlowLifecycleState(starting);
         FlowStateLifecycle nextFlowLifecycleState = failed;
         try {
             // TODO ... should we just be using next()... seems better.
             selectActivity(0, true);
             nextFlowLifecycleState = started;
         } finally {
             // because may throw flow validation exception
             if ( this.getFlowStateLifecycle() == starting) {
                 this.setFlowLifecycleState(nextFlowLifecycleState);
             }
         }
         return getCurrentPage();
     }
 
     /**
      * @see org.amplafi.flow.FlowState#resume()
      */
     @Override
     public String resume() {
         if (!isActive()) {
             return begin();
         } else {
             if (activateFlowActivity(getCurrentActivity(), FlowStepDirection.inPlace)) {
                 selectActivity(nextIndex(), true);
             }
             return getCurrentPage();
         }
     }
 
     /**
      *
      * @see org.amplafi.flow.FlowState#initializeFlow()
      */
     @Override
     public void initializeFlow() {
         this.setFlowLifecycleState(initializing);
         FlowStateLifecycle nextFlowLifecycleState = failed;
         try {
             Map<String, FlowPropertyDefinitionImplementor> propertyDefinitions = this.getFlow().getPropertyDefinitions();
             if (propertyDefinitions != null) {
                 Collection<FlowPropertyDefinitionImplementor> flowPropertyDefinitions = propertyDefinitions.values();
                 initializeFlowProperties(this, flowPropertyDefinitions);
             }
 
             int size = this.size();
             for (int i = 0; i < size; i++) {
                 FlowActivity activity = getActivity(i);
                 activity.initializeFlow();
                 LapTimer.sLap(activity.getFlowPropertyProviderFullName(), ".initializeFlow() completed");
             }
             nextFlowLifecycleState = initialized;
         } finally {
             // because may throw flow validation exception - which could have already changed the flowStateLifecycle.
             if ( this.getFlowStateLifecycle() == initializing) {
                 this.setFlowLifecycleState(nextFlowLifecycleState);
             }
         }
     }
     @Override
     public void initializeFlowProperties(FlowPropertyProvider flowPropertyProvider, Iterable<FlowPropertyDefinitionImplementor> flowPropertyDefinitions) {
         for (FlowPropertyDefinitionImplementor flowPropertyDefinition : flowPropertyDefinitions) {
             initializeFlowProperty(flowPropertyProvider, flowPropertyDefinition);
         }
     }
     /**
      * Look through the FlowState map to find all values with a valid key. ( see {@link FlowPropertyDefinitionImplementor#getNamespaceKeySearchList(FlowState, FlowPropertyProvider, boolean)} )
      * The first match found is used.
      *
      * See note in FactoryFlowPropertyDefinitionProvider - properties defined by FactoryFlowPropertyDefinitionProviders need to be initialized as well ( they are not ).
      * @param flowPropertyProvider
      * @param flowPropertyDefinition
      */
     @Override
     public void initializeFlowProperty(FlowPropertyProvider flowPropertyProvider, FlowPropertyDefinitionImplementor flowPropertyDefinition) {
         // move values from alternateNames to the true name.
         // or just clear out the alternate names of their values.
         List<String> namespaces = flowPropertyDefinition.getNamespaceKeySearchList(this, flowPropertyProvider, true);
         String value = null;
         boolean valueExternallySet = false;
         PropertyUsage propertyUsage = flowPropertyDefinition.getPropertyUsage();
         // make sure property clean up happens even for properties that cannot be set externally.
         for(String namespace: namespaces) {
             for (String alternateName : flowPropertyDefinition.getAllNames()) {
                 if ( getFlowValuesMap().containsKey(namespace, alternateName)) {
                     if ( !valueExternallySet ) {
                         value = getRawProperty(namespace, alternateName);
                         valueExternallySet = true;
                     }
                     if ( propertyUsage.isCleanOnInitialization()) {
                         // if clearing then we need to clear all possible matches - so we continue with loop.
                         remove(namespace, alternateName);
                     } else if ( valueExternallySet ) {
                         break;
                     }
                 }
             }
         }
         boolean valueSet = valueExternallySet;
         if ( !valueExternallySet || !propertyUsage.isExternallySettable()) {
             value = flowPropertyDefinition.getInitial();
             valueSet = true;
         }
         String namespace = flowPropertyDefinition.getNamespaceKey(this, flowPropertyProvider);
         String currentValue = getRawProperty(namespace, flowPropertyDefinition.getName());
         if (valueSet && !StringUtils.equals(value, currentValue)) {
             // This code allows FlowPropertyChangeListeners to be triggered when the flow starts up.
             if (!propertyUsage.isExternallySettable() && valueExternallySet) {
                 // TODO use ExternalPropertyAccessRestriction
                 // property cannot be overridden.
                 // note: this can happen when transitioning (morphing between 2 different flows).
                 // In this case internalState properties are being copied not just user externally supplied values.
                 // TODO: investigate to see if we can clean this up.
                 getLog().info(
                     (flowPropertyProvider==null?getFlow().getFlowPropertyProviderName():flowPropertyProvider.getFlowPropertyProviderFullName())
                                 + '.'
                                 + flowPropertyDefinition.getName()
                                 + " cannot be set to '"
                                 + currentValue
                                 + "' external to the flow. It is being force to the initial value of '"
                                 + value + "'");
             }
             setRawProperty(flowPropertyProvider, flowPropertyDefinition, value);
         }
     }
 
 
     /**
      * @see org.amplafi.flow.FlowState#getExportedValuesMap()
      */
     @SuppressWarnings("unchecked")
     @Override
     public FlowValuesMap getExportedValuesMap() {
         FlowValuesMap valuesMap =exportProperties(false);
         return valuesMap;
     }
     /**
      * Method to allow overriding.
      * @return a copied FlowValuesMap
      */
     @SuppressWarnings("unchecked")
     protected FlowValuesMap createFlowValuesMapCopy() {
         return new DefaultFlowValuesMap((FlowValuesMap<FlowValueMapKey, CharSequence>)getFlowValuesMap());
     }
 
     /**
      * When exporting we start from all the values in the flowValuesMap. This is because the current flow may not be aware of/understand
      * all the properties. This flow is just passing the values on unaltered.
      *
      * TODO: if flow is still in progress then flowLocal/activityLocal values should be in the export map -- so that callees can get values.
      * @param clearFrom removes the exported values from this's FlowValuesMap.
      * @return the exported values
      */
     @SuppressWarnings("unchecked")
     protected FlowValuesMap<? extends FlowValueMapKey, ? extends CharSequence> exportProperties(boolean clearFrom) {
         FlowValuesMap exportValueMap = createFlowValuesMapCopy();
         Map<String, FlowPropertyDefinitionImplementor> propertyDefinitions = this.getFlow().getPropertyDefinitions();
         if (isNotEmpty(propertyDefinitions)) {
             Collection<FlowPropertyDefinitionImplementor> flowPropertyDefinitions = propertyDefinitions.values();
             exportProperties(exportValueMap, flowPropertyDefinitions, null, clearFrom);
         }
 
         int size = this.size();
         for (int i = 0; i < size; i++) {
             FlowActivityImplementor activity = getActivity(i);
             Map<String, FlowPropertyDefinitionImplementor> activityFlowPropertyDefinitions = activity.getPropertyDefinitions();
             if ( isNotEmpty(activityFlowPropertyDefinitions)) {
                 exportProperties(exportValueMap, activityFlowPropertyDefinitions.values(), activity, clearFrom);
             }
         }
         // TODO should we clear all non-global namespace values? We have slight leak through when undefined properties are set on a flow.
         return exportValueMap;
     }
     @SuppressWarnings("unchecked")
     protected void exportProperties(FlowValuesMap exportValueMap, Iterable<FlowPropertyDefinitionImplementor> flowPropertyDefinitions, FlowActivityImplementor flowActivity, boolean clearFrom) {
         for (FlowPropertyDefinitionImplementor flowPropertyDefinition : flowPropertyDefinitions) {
             exportFlowProperty(exportValueMap, flowPropertyDefinition, flowActivity, clearFrom);
         }
     }
     /**
      * @param exportValueMap map with namespaced properties. All properties should be copied back to the global namespace when done.
      * @param flowPropertyDefinition
      * @param flowActivity
      * @param flowCompletingExport
      * The FlowState is completing so all clean up actions on the FlowState can be performed as part of this export.
      * (TODO: pat 3 Oct 2010 removing state is a one time operation - maybe make it an explicit separate method call?)
      */
     @SuppressWarnings("unchecked")
     protected void exportFlowProperty(FlowValuesMap exportValueMap, FlowPropertyDefinitionImplementor flowPropertyDefinition, FlowActivityImplementor flowActivity, boolean flowCompletingExport) {
         // move values from alternateNames to the true name.
         // or just clear out the alternate names of their values.
         String value = null;
         boolean valueSet = false;
         List<String> namespaces = flowPropertyDefinition.getNamespaceKeySearchList(this, flowActivity, false);
         for(String namespace: namespaces ) {
             for (String key : flowPropertyDefinition.getAllNames()) {
                 if ( getFlowValuesMap().containsKey(namespace, key)) {
                     if ( !valueSet ) {
                         // preserve the value from the most precise namespace.
                         value = getRawProperty(namespace, key);
                         valueSet = true;
                     }
                     if ( namespace != null ) {
                         // exclude the global namespace as we clear because global values may not be altered by this property ( propertyUsage.isCopyBackOnFlowSuccess() may be false )
                         exportValueMap.remove(namespace, key);
                         if ( flowCompletingExport) {
                             remove(namespace, key);
                         }
                     }
                 }
             }
         }
         if ( valueSet ) {
             // TODO HANDLE case where we need a to copy from caller to callee. This situation suggests that if PropertyUsage != internalState then the property should be exposed.
             // but need to know the situation: copy to callee or back to caller?
             if ( flowPropertyDefinition.isCopyBackOnFlowSuccess()) {
                 // HACK
                 // HACK: investigate. Any use of getExportedValuesMap() will trigger this copyback to the FlowState's default namespace.
                 put(null, flowPropertyDefinition.getName(), value);
                 exportValueMap.put(null, flowPropertyDefinition.getName(), value);
             }
         }
     }
     @Override
     public void copyTrustedValuesMapToFlowState(Map<String, String> trustedValues) {
         if ( isNotEmpty(trustedValues)) {
             for(Map.Entry<String, String> entry: trustedValues.entrySet()) {
                 String key = entry.getKey();
                 String value = entry.getValue();
                 FlowPropertyDefinition flowPropertyDefinition = getFlowPropertyDefinitionWithCreate(key, null, value);
                 setRawProperty(this, flowPropertyDefinition, value);
             }
         }
     }
     /**
      *
      * @see org.amplafi.flow.FlowState#morphFlow(java.lang.String, java.util.Map)
      */
     @Override
     public String morphFlow(String morphingToFlowTypeName, Map<String, String> initialFlowState) {
         if (isCompleted()) {
             return null;
         }
         if ( this.getFlowTypeName().equals(morphingToFlowTypeName)) {
             return this.getCurrentPage();
         }
         Flow nextFlow = getFlowManagement().getInstanceFromDefinition(morphingToFlowTypeName);
         List<FlowActivityImplementor> originalFAs = getActivities();
         List<FlowActivityImplementor> nextFAs = nextFlow.getActivities();
 
         // make sure FAs in both the flows are in order
         boolean inOrder = areFlowActivitiesInOrder(originalFAs, nextFAs);
         if (!inOrder) {
             throw new IllegalStateException("The FlowActivities in the original and the morphed flow are not in order"
                                             + "\nOriginal Flow FlowActivities : " + originalFAs
                                             + "\nNext Flow FlowActivities : " + nextFAs);
         }
 
         // complete the current FA in the current Flow
         FlowActivityImplementor currentFAInOriginalFlow = getCurrentFlowActivityImplementor();
         // So the current FlowActivity does not try to do validation.
         passivate(false, FlowStepDirection.inPlace);
 
         // morph and initialize to next flow
         setFlowTypeName(morphingToFlowTypeName);
         copyTrustedValuesMapToFlowState(initialFlowState);
         this.setCurrentActivityIndex(0);
         // new flow will have different flow activities (and properties ) that needs to be
         initializeFlow();
         begin();
 
         FlowActivityImplementor targetFAInNextFlow = getTargetFAInNextFlow(currentFAInOriginalFlow,
                                                                 originalFAs, nextFAs);
 
         // No common FAs, No need to run nextFlow at all, just return
         if (targetFAInNextFlow != null) {
 
             // move the second flow to appropriate FA
             while (hasNext() && !isEqualTo(getCurrentActivity(), targetFAInNextFlow)) {
                 next();
             }
         }
         return this.getCurrentPage();
     }
 
     private FlowActivityImplementor getTargetFAInNextFlow(FlowActivityImplementor currentFAInOriginalFlow,
             List<FlowActivityImplementor> originalFAs, List<FlowActivityImplementor> nextFAs) {
         FlowActivity flowActivity = this.getActivity(currentFAInOriginalFlow.getFlowPropertyProviderName());
         if ( flowActivity != null ) {
             // cool .. exact match on the names.
             return (FlowActivityImplementor) flowActivity;
         }
         // find the first FlowActivity that is after all the flowActivities with the same names
         // as FlowActivities in the previous flow.to find the same approximate spot in the the new flow.
         int newCurrentIndex = this.getCurrentActivityIndex();
         for (int prevIndex = 0; prevIndex < originalFAs.size(); prevIndex++) {
             FlowActivity originalFA = originalFAs.get(prevIndex);
             if ( isEqualTo(originalFA, currentFAInOriginalFlow)) {
                 break;
             }
             for(int nextIndex = newCurrentIndex; nextIndex < nextFAs.size(); nextIndex++) {
                 FlowActivity nextFA = nextFAs.get(nextIndex);
                 if(isEqualTo(originalFA, nextFA)) {
                     newCurrentIndex = nextIndex+1;
                 }
             }
         }
         return (FlowActivityImplementor) this.getActivity(newCurrentIndex);
     }
 
     private boolean isEqualTo(FlowActivity fa1, FlowActivity fa2) {
         return fa1 != null && fa2 != null && fa1.getFlowPropertyProviderName().equals(fa2.getFlowPropertyProviderName());
     }
 
     private boolean areFlowActivitiesInOrder(List<FlowActivityImplementor> prevFAs, List<FlowActivityImplementor> nextFAs) {
         int lastPrevIndex = -1;
         int lastNextIndex = -1;
         for(int prevIndex = 0; prevIndex < prevFAs.size(); prevIndex++) {
             for(int nextIndex = 0; nextIndex < nextFAs.size(); nextIndex++) {
                 if ( isEqualTo(prevFAs.get(prevIndex), nextFAs.get(nextIndex))) {
                     if ( nextIndex > lastNextIndex && prevIndex > lastPrevIndex ) {
                         lastNextIndex = nextIndex;
                         lastPrevIndex = prevIndex;
                     } else {
                         return false;
                     }
                 }
             }
         }
         return true;
     }
     /**
      *
      * @param verifyValues if true check the flow to validate the {@link FlowActivityPhase#finish} properties.
      * @return the next flowState 'this' FlowActivities believe should be run.
      */
     protected FlowState finishFlowActivities(boolean verifyValues) {
         FlowValidationResult flowValidationResult = null;
         if (verifyValues) {
             flowValidationResult = getFullFlowValidationResult(FlowActivityPhase.finish, FlowStepDirection.forward);
         }
         FlowValidationException.valid(this, flowValidationResult);
         FlowState currentNextFlowState = getFlowManagement().transitionToFlowState(this, FSFLOW_TRANSITIONS);
         int size = this.size();
         for (int i = 0; i < size; i++) {
             FlowActivity activity = getActivity(i);
             FlowState returned = activity.finishFlow(currentNextFlowState);
             // activity.refresh(); -- commented out because saves default values back to the flowState
             // avoids lose track of FlowState if another FA later in the Flow
             // definition returns a null. ( this means that a FA cannot override a previous decision ).
             if (returned != null && currentNextFlowState != returned) {
                 currentNextFlowState = returned;
             }
         }
         return currentNextFlowState;
     }
 
 
     /**
      * @see org.amplafi.flow.FlowState#selectActivity(int, boolean)
      */
     @SuppressWarnings("unchecked")
     @Override
     public <T extends FlowActivity> T selectActivity(int newActivity, boolean verifyValues) {
         if (isCompleted()) {
             return null;
         }
         FlowActivityIterator flowActivityIterator = new FlowActivityIterator(newActivity);
 
         do {
             if(this.isActive()) {
                 FlowValidationResult flowValidationResult;
                 // call passivate even if just returning to the current
                 // activity. but not if we are going back to a previous step
                 flowValidationResult = this.passivate(verifyValues, flowActivityIterator.getFlowStepDirection());
                 if ( !flowValidationResult.isValid()) {
                     activateFlowActivity(getCurrentActivity(), FlowStepDirection.inPlace);
                     throw new FlowValidationException(this, getCurrentActivity(), flowValidationResult);
                 }
             }
             if ( flowActivityIterator.hasNext()) {
                 flowActivityIterator.activate();
             }
         } while (flowActivityIterator.hasNext());
         if (flowActivityIterator.isTimeToFinish()) {
             // ran out .. time to complete...
             // if chaining FlowStates the actual page may be from another
             // flowState.
             finishFlow();
             return null;
         } else {
             return (T) getCurrentActivity();
         }
     }
 
     /**
      * @param flowStepDirection
      */
     private boolean activateFlowActivity(FlowActivity flowActivity, FlowStepDirection flowStepDirection) {
         getFlowManagement().activityChange(this, flowActivity, flowStepDirection, FlowActivityPhase.activate);
         return flowActivity.activate(flowStepDirection);
     }
 
     /**
      * @see org.amplafi.flow.FlowState#selectVisibleActivity(int)
      */
     @SuppressWarnings("unchecked")
     @Override
     public <T extends FlowActivity> T selectVisibleActivity(int visibleIndex) {
         int index = -1;
         int realIndex = -1;
         for (FlowActivity activity : getActivities()) {
             if (!activity.isInvisible()) {
                 index++;
             }
             realIndex++;
             if (index == visibleIndex) {
                 break;
             }
         }
         return (T) selectActivity(realIndex, false);
     }
 
     /**
      * @see org.amplafi.flow.FlowState#saveChanges()
      */
     @Override
     public void saveChanges() {
         LapTimer.sLap(this.getActiveFlowLabel()," beginning saveChanges()");
         for (int i = 0; i < this.size(); i++) {
             FlowActivity flowActivity = getActivity(i);
             FlowValidationResult flowActivityValidationResult  = flowActivity.getFlowValidationResult(FlowActivityPhase.saveChanges, FlowStepDirection.forward);
             FlowValidationException.valid(this, flowActivityValidationResult);
             flowActivity.saveChanges();
             // activity.refresh(); -- commented out because saves default values back to the flowState
             LapTimer.sLap(flowActivity.getFlowPropertyProviderFullName(), ".saveChanges() completed");
         }
         LapTimer.sLap(this.getActiveFlowLabel()," end saveChanges()");
     }
 
     /**
      * @see org.amplafi.flow.FlowState#finishFlow()
      */
     @Override
     public String finishFlow() {
         return completeFlow(FlowStateLifecycle.successful);
     }
 
     /**
      * @see org.amplafi.flow.FlowState#cancelFlow()
      */
     @Override
     public String cancelFlow() {
         return completeFlow(FlowStateLifecycle.canceled);
     }
 
     protected String completeFlow(FlowStateLifecycle nextFlowLifecycleState) {
         String pageName = null;
         if (!isCompleted()) {
             FlowState continueWithFlow = null;
             boolean verifyValues = nextFlowLifecycleState.isVerifyValues();
             FlowValidationResult flowValidationResult = passivate(verifyValues, FlowStepDirection.inPlace);
 
             if (verifyValues) {
                 FlowValidationException.valid(this, flowValidationResult);
                 saveChanges();
             }
 
             this.setFlowLifecycleState(nextFlowLifecycleState);
 
             boolean success = false;
             try {
                 // getting continueWithFlow should use FlowLauncher more correctly.
                 continueWithFlow = finishFlowActivities(verifyValues);
                 success = true;
             } finally {
                 this.setCurrentActivityByName(null);
                 clearCache();
                 if (!success) {
                     getFlowManagement().dropFlowState(this);
                 }
             }
             // pass on the return flow to the continuation flow.
             // need to set before starting continuation flow because continuation flow may run to completion.
             // HACK : seems like the continueFlow should have picked this up automatically
             String returnToFlow = this.getProperty(FSRETURN_TO_FLOW);
             this.setProperty(FSRETURN_TO_FLOW, null);
 
             // TODO: THIS block of code should be in the FlowManagement code.
             // TODO: Put this in a FlowPropertyValueProvider !!
             // OLD note but may still be valid:
             // if continueWithFlow is not null then we do not want start
             // any other flows except continueWithFlow. Autorun flows should
             // start only if we have no flow specified by the finishingActivity. This
             // caused bad UI behavior when we used TransitionFlowActivity to start new
             // flow.
             // make sure that don't get into trouble by a finishFlow that
             // returns the current FlowState.
             if (continueWithFlow == null || continueWithFlow == this) {
                 /* need to explore this more.
                    idea is that there should be some ability to copy back from the started flows.
                    but this really should be under the control of the caller. So right now best mechanism seems to be
                    to make caller pass in an object to be modified.
                 if ( nextFlowLifecycleState == successful && isNotBlank(returnToFlow)) {
                     FlowState returnFlow = getFlowManagement().getFlowState(returnToFlow);
                     Map exportedMap = this.getExportedValuesMap().getAsFlattenedStringMap();
                     returnFlow.setAllProperties(exportedMap);
                 }
                  */
                 pageName = getFlowManagement().completeFlowState(this, false, nextFlowLifecycleState);
             } else {
                 if ( isNotBlank(returnToFlow)) {
                     continueWithFlow.setProperty(FSRETURN_TO_FLOW, returnToFlow);
                 }
                 pageName = getFlowManagement().completeFlowState(this, true, nextFlowLifecycleState);
                 if (!continueWithFlow.isActive()) {
                     pageName = continueWithFlow.begin();
                 } else if (!continueWithFlow.isCompleted()) {
                     pageName = continueWithFlow.resume();
                 }
                 String continueWithFlowLookup;
                 if (continueWithFlow.isCompleted()) {
                     // the flow that was continued with immediately finished.
                     // find out what the next continue flow is ... shouldn't this be in a while loop???
                     // or passed over to the FlowManagement code for handling??
                     continueWithFlowLookup = continueWithFlow.getProperty(FSCONTINUE_WITH_FLOW);
                 } else {
                     continueWithFlowLookup = continueWithFlow.getLookupKey();
                 }
                 // save back to "this" so that if the current flowState is in turn part of a chain that the callers
                 // will find the correct continue flow state.
                 setProperty(FSCONTINUE_WITH_FLOW, continueWithFlowLookup);
 
             }
         }
         // if afterPage is already set then don't lose that information.
         if (pageName != null) {
             setAfterPage(pageName);
         }
         return pageName;
     }
 
     @Override
     public FlowValidationResult getFullFlowValidationResult(FlowActivityPhase flowActivityPhase, FlowStepDirection flowStepDirection) {
         FlowValidationResult flowValidationResult = new ReportAllValidationResult();
         // TODO : need to account for properties that earlier activities will create the property required by a later property.
         // we should look for PropertyUsage.create (and equivalents )
         for(FlowActivity flowActivity: this.getActivities()) {
             FlowValidationResult flowActivityValidationResult  = flowActivity.getFlowValidationResult(flowActivityPhase, flowStepDirection);
             flowValidationResult.merge(flowActivityValidationResult);
         }
         return flowValidationResult;
     }
 
     /**
      * @see org.amplafi.flow.FlowState#getFinishFlowValidationResult()
      */
     @Override
     public FlowValidationResult getFinishFlowValidationResult() {
         FlowValidationResult flowValidationResult = getCurrentActivityFlowValidationResult();
         if ( flowValidationResult == null || flowValidationResult.isValid()) {
             flowValidationResult = getFullFlowValidationResult(FlowActivityPhase.finish, FlowStepDirection.forward);
             if ( flowValidationResult == null || flowValidationResult.isValid()) {
                 flowValidationResult = getFullFlowValidationResult(FlowActivityPhase.saveChanges, FlowStepDirection.forward);
             }
         }
         return flowValidationResult;
     }
 
     /**
      * @param possibleReferencedState
      * @return true if this flowState references possibleReferencedState
      */
     @Override
     public boolean isReferencing(FlowState possibleReferencedState) {
         if ( this == possibleReferencedState) {
             // can't reference self.
             return false;
         } else {
             String possibleReferencedLookupKey = possibleReferencedState.getLookupKey();
             return possibleReferencedLookupKey.equals(this.getProperty(FSCONTINUE_WITH_FLOW))
                 || possibleReferencedLookupKey.equals(this.getProperty(FSRETURN_TO_FLOW));
         }
     }
 
     @Override
     public FlowValidationResult passivate(boolean verifyValues, FlowStepDirection flowStepDirection) {
         FlowActivityImplementor currentActivity = getCurrentActivity();
         if ( currentActivity != null ) {
             currentActivity.refresh();
             return currentActivity.passivate(verifyValues, flowStepDirection);
         }
         return null;
     }
     /**
      * @see org.amplafi.flow.FlowState#getCurrentPage()
      */
     @Override
     public String getCurrentPage() {
         if (isCompleted()) {
             return this.getAfterPage();
         }
         String pageName = null;
         if (isActive()) {
             FlowActivity flowActivity = getCurrentActivity();
             pageName = flowActivity.getProperty(FSPAGE_NAME);
         }
         if (isBlank(pageName)) {
             pageName = getProperty(FSPAGE_NAME);
             if (isBlank(pageName)) {
                 pageName = this.getFlow().getPageName();
             }
         }
         return pageName;
     }
 
     @Override
     public void setCurrentPage(String page) {
         setProperty(FSPAGE_NAME, page);
     }
 
     /**
      *
      * @see org.amplafi.flow.impl.FlowStateImplementor#setActiveFlowLabel(java.lang.String)
      */
     @Override
     public void setActiveFlowLabel(String activeFlowLabel) {
         this.activeFlowLabel = activeFlowLabel;
     }
 
     /**
      * @see org.amplafi.flow.FlowState#getActiveFlowLabel()
      */
     @Override
     public String getActiveFlowLabel() {
         if (this.activeFlowLabel != null) {
             return activeFlowLabel;
         } else {
             return getFlow().getContinueFlowTitle();
         }
     }
 
     // TODO: merge? with activeFlowLabel?
     /**
      * @return the text for a link to activate this FlowState
      */
     public String getLinkTitle() {
         String linkTitle = getProperty(FSLINK_TEXT);
         if ( isBlank(linkTitle)) {
             linkTitle = this.getFlow().getLinkTitle();
         }
         return linkTitle;
     }
 
     @SuppressWarnings("unchecked")
     @Override
     public <T extends FlowActivity> T getActivity(int activityIndex) {
         T flowActivity = (T) this.getFlow().getActivity(activityIndex);
         return resolveActivity(flowActivity);
     }
 
     /**
      * All accesses to a {@link FlowActivity} should occur through this method.
      * This allows {@link FlowState} implementations to a chance to add in any
      * objects needed to access other parts of the service (database transactions for example).
      * @param <T>
      * @see FlowManagement#wireDependencies(Object)
      * @param flowActivity
      * @return flowActivity
      */
     public <T extends FlowPropertyProvider> T resolveActivity(T flowActivity) {
         getFlowManagement().wireDependencies(flowActivity);
         return flowActivity;
     }
 
     /**
      * @see org.amplafi.flow.FlowState#getActivity(java.lang.String)
      */
     @SuppressWarnings("unchecked")
     @Override
     public <T extends FlowActivity> T  getActivity(String activityName) {
         // HACK we need to set up a map.
         if ( activityName != null ) {
             for (FlowActivity flowActivity : this.getFlow().getActivities()) {
                 if (flowActivity.isNamed(activityName)) {
                     return (T) resolveActivity(flowActivity);
                 }
             }
         }
         return null;
     }
 
     /**
      * @see org.amplafi.flow.FlowState#getLookupKey()
      */
     @Override
     public String getLookupKey() {
         return lookupKey;
     }
 
     @Override
     public boolean hasLookupKey(Object key) {
         if (key == null) {
             return false;
         } else {
             return getLookupKey().equals(key.toString());
         }
     }
 
     /**
      * @see org.amplafi.flow.FlowState#getCurrentActivity()
      */
     @SuppressWarnings("unchecked")
     @Override
     public  <T extends FlowActivity> T getCurrentActivity() {
         return (T) getActivity(this.getCurrentActivityIndex());
     }
     public FlowActivityImplementor getCurrentFlowActivityImplementor() {
         return (FlowActivityImplementor) getCurrentActivity();
     }
     /**
      * Use selectActivity to change the current activity.
      *
      * @param currentActivity The currentActivity to set.
      */
     private void setCurrentActivityIndex(int currentActivity) {
         if (currentActivity >= 0 && currentActivity < size()) {
             this.currentActivityIndex = currentActivity;
             this.currentActivityByName = getCurrentActivity().getFlowPropertyProviderName();
         } else {
             // required to match the iterator definition.
             throw new NoSuchElementException(currentActivity + ": incorrect index for "
                                              + this.activeFlowLabel);
         }
     }
 
     /**
      * @see org.amplafi.flow.FlowState#setCurrentActivityByName(java.lang.String)
      */
     @Override
     public void setCurrentActivityByName(String currentActivityByName) {
         this.currentActivityByName = currentActivityByName;
         this.currentActivityIndex = null;
     }
 
     /**
      * @see org.amplafi.flow.FlowState#getCurrentActivityByName()
      */
     @Override
     public String getCurrentActivityByName() {
         return this.currentActivityByName;
     }
 
     /**
      * @see org.amplafi.flow.FlowState#size()
      */
     @Override
     public int size() {
         if ( !isEmpty(getActivities())) {
             return getActivities().size();
         } else {
             return 0;
         }
     }
 
     /**
      * @see org.amplafi.flow.FlowState#getCurrentActivityIndex()
      */
     @Override
     public int getCurrentActivityIndex() {
         if (currentActivityIndex == null) {
             currentActivityIndex = -1;
             if (isNotBlank(currentActivityByName)) {
                 int i = 0;
                 for (FlowActivity flowActivity : this.getFlow().getActivities()) {
                     if (currentActivityByName.equals(flowActivity.getFlowPropertyProviderName())) {
                         currentActivityIndex = i;
                         break;
                     } else {
                         i++;
                     }
                 }
             }
         }
         return currentActivityIndex;
     }
 
     @Override
     public String getRawProperty(String key) {
         return getRawProperty((FlowActivity)null, key);
     }
 
     @Override
     public String getRawProperty(String namespace, String key) {
         return ObjectUtils.toString(getFlowValuesMap().get(namespace, key), null);
     }
 
     @SuppressWarnings("unchecked")
     @Override
     public <T> T getPropertyWithDefinition(FlowPropertyProvider flowPropertyProvider, FlowPropertyDefinitionImplementor propertyDefinition) {
         T result = (T) getCached(propertyDefinition, flowPropertyProvider);
         if ( result == null ) {
             getFlowManagement().wireDependencies(propertyDefinition);
             String value = getRawProperty(flowPropertyProvider, propertyDefinition);
             result = (T) propertyDefinition.deserialize(flowPropertyProvider, value);
             if (result == null && propertyDefinition.isAutoCreate()) {
                 result =  (T) propertyDefinition.getDefaultObject(flowPropertyProvider);
 
                 if ( !propertyDefinition.isCacheOnly()) {
                     // so the flowState has the generated value.
                     // this will make visible to json exporting.
                     // also triggers FlowPropertyValueChangeListeners on the initial set.
                     setPropertyWithDefinition(flowPropertyProvider, propertyDefinition, result);
                 }
             }
             setCached(propertyDefinition, flowPropertyProvider, result);
         }
         return result;
     }
 
     @Override
     public <T> void setPropertyWithDefinition(FlowPropertyProvider flowPropertyProvider, FlowPropertyDefinitionImplementor propertyDefinition, T value) {
         Object actual;
         String stringValue = null;
         getFlowManagement().wireDependencies(propertyDefinition);
         if (value instanceof String && propertyDefinition.getDataClass() != String.class) {
             // handle case for when initializing from string values.
             // or some other raw format.
             stringValue = (String) value;
             actual = propertyDefinition.deserialize(flowPropertyProvider, stringValue);
         } else {
             actual = value;
         }
         boolean cacheValue = !(actual instanceof String);
         if (!propertyDefinition.isCacheOnly()) {
             if ( stringValue==null ) {
                 stringValue = propertyDefinition.serialize(actual);
             }
             cacheValue &= this.setRawProperty(flowPropertyProvider, propertyDefinition, stringValue);
         }
         if (cacheValue) {
             // HACK FPD can't currently parse AmpEntites to actual objects.
             this.setCached(propertyDefinition, flowPropertyProvider, actual);
         }
 
     }
 
     /**
      * @param flowPropertyProvider
      * @param flowPropertyDefinition
      * @param value
      * @return true if the value has changed.
      */
     protected boolean setRawProperty(FlowPropertyProvider flowPropertyProvider, FlowPropertyDefinition flowPropertyDefinition, String value) {
         String namespace = ((FlowPropertyDefinitionImplementor)flowPropertyDefinition).getNamespaceKey(this, flowPropertyProvider);
         String key = flowPropertyDefinition.getName();
         String oldValue = getRawProperty(namespace, key);
         String newValue = value;
         if (!StringUtils.equals(newValue, oldValue)) {
             List<FlowPropertyValueChangeListener> flowPropertyValueChangeListeners = flowPropertyDefinition.getFlowPropertyValueChangeListeners();
             if ( isNotEmpty(flowPropertyValueChangeListeners)) {
                 for(FlowPropertyValueChangeListener flowPropertyValueChangeListener: flowPropertyValueChangeListeners) {
                     this.getFlowManagement().wireDependencies(flowPropertyValueChangeListener);
                     newValue = flowPropertyValueChangeListener.propertyChange(flowPropertyProvider, namespace, flowPropertyDefinition, newValue, oldValue);
                 }
             }
             if ( flowPropertyProvider instanceof FlowPropertyValueChangeListener) {
                 newValue = ((FlowPropertyValueChangeListener)flowPropertyProvider).propertyChange(flowPropertyProvider, namespace, flowPropertyDefinition, newValue, oldValue);
             }
 
             FlowActivityImplementor activity = getActivity(namespace);
             if ( activity == flowPropertyProvider || !(activity instanceof FlowPropertyValueChangeListener)) {
                 activity = getCurrentFlowActivityImplementor();
             }
             if ( activity instanceof FlowPropertyValueChangeListener && activity != flowPropertyProvider) {
                 newValue = ((FlowPropertyValueChangeListener)activity).propertyChange(flowPropertyProvider, namespace, flowPropertyDefinition, newValue, oldValue);
             }
 
             for(FlowPropertyValueChangeListener flowPropertyValueChangeListener: this.globalFlowPropertyValueChangeListeners) {
                 newValue = flowPropertyValueChangeListener.propertyChange(flowPropertyProvider, namespace, flowPropertyDefinition, newValue, oldValue);
             }
             put(namespace, key, newValue);
             return true;
         } else {
             return false;
         }
     }
 
 
     /**
      * @param key
      * @param value
      * @param namespace
      */
     private void put(String namespace, String key, String value) {
         getFlowValuesMap().put(namespace, key, value);
         // in other way wrong cached value returns in next get request
         setCached(namespace, key, null);
     }
     protected void remove(String namespace, String key) {
         getFlowValuesMap().remove(namespace, key);
         // in other way wrong cached value returns in next get request
         setCached(namespace, key, null);
     }
 
     /**
      * @see org.amplafi.flow.FlowState#hasProperty(java.lang.String)
      */
     @Override
     public boolean hasProperty(String key) {
         return getFlowValuesMap().containsKey(key);
     }
 
     /**
      * @see org.amplafi.flow.FlowState#getActivities()
      */
     @SuppressWarnings("unchecked")
     @Override
     public List<FlowActivityImplementor> getActivities() {
         return getFlow().getActivities();
     }
 
     /**
      * @see org.amplafi.flow.FlowState#getVisibleActivities()
      */
     @Override
     public List<FlowActivity> getVisibleActivities() {
         return getFlow().getVisibleActivities();
     }
 
     /**
      * @see org.amplafi.flow.FlowState#isFinishable()
      */
     @Override
     public boolean isFinishable() {
         if ( !isCompleted()) {
             FlowActivity currentActivity = this.getCurrentActivity();
             // may not have been started
             if ((currentActivity != null && currentActivity.isFinishingActivity()) || !hasVisibleNext()) {
                 // explicitly able to finish.
                 // or last visible step, which must always be able to finish.
                 return true;
             } else {
                 // all remaining activities claim they have valid data.
                 // this enables a user to go back to a previous step and still finish the flow.
                 // FlowActivities that have content that is required to be viewed (Terms of Service )
                 // should have a state flag so that the flow can not be finished until the ToS is viewed.
                 return getFinishFlowValidationResult().isValid();
             }
         } else {
             // if it is already completed then the flow is not finishable (it already is finished)
             // but may need to indicate that this is not an error as well.
             return false;
         }
     }
 
     @Override
     public void clearCache() {
         if (this.cachedValues != null) {
             this.cachedValues.clear();
             this.cachedValues = null;
         }
     }
 
     /**
      * @see org.amplafi.flow.FlowState#getFlowTitle()
      */
     @Override
     public String getFlowTitle() {
         String flowTitle = getProperty(FSTITLE_TEXT);
         if (isBlank(flowTitle)) {
             flowTitle = this.getFlow().getFlowTitle();
         }
         if (isBlank(flowTitle)) {
             flowTitle = getLinkTitle();
         }
         return flowTitle;
     }
 
 
     @Override
     public synchronized void setCached(String namespace, String key, Object value) {
         if (cachedValues == null) {
             if ( value == null) {
                 // nothing to cache and no cached values.
                 return;
             }
             cachedValues = new MultiKeyMap();
             flowManagement.registerForCacheClearing();
         }
         if ( value == null ) {
             cachedValues.remove(namespace, key);
         } else {
             cachedValues.put(namespace, key, value);
         }
     }
 
 
 
     /**
      * @see org.amplafi.flow.flowproperty.FlowPropertyProviderWithValues#clearCached(String, java.lang.String)
      */
     @Override
     public void clearCached(String namespace, String key) {
         setCached(namespace, key, null);
     }
 
     @SuppressWarnings("unchecked")
     @Override
     public <T> T getCached(String namespace, String key) {
         if(cachedValues != null) {
             T value = (T) cachedValues.get(namespace, key);
             return value;
         } else {
             return null;
         }
     }
 
     @Override
     @SuppressWarnings("unchecked")
     public <T> T getCached(FlowPropertyDefinitionImplementor flowPropertyDefinition, FlowPropertyProvider flowPropertyProvider) {
         String namespace = flowPropertyDefinition.getNamespaceKey(this, flowPropertyProvider);
         return (T) getCached(namespace, flowPropertyDefinition.getName());
     }
     @Override
     public void setCached(FlowPropertyDefinitionImplementor flowPropertyDefinition, FlowPropertyProvider flowPropertyProvider, Object value) {
         String namespace = flowPropertyDefinition.getNamespaceKey(this, flowPropertyProvider);
         setCached(namespace, flowPropertyDefinition.getName(), value);
     }
 
     @Override
     public void setFlowManagement(FlowManagement flowManagement) {
         this.flowManagement = flowManagement;
     }
 
     /**
      * @see org.amplafi.flow.FlowState#getFlowManagement()
      */
     @Override
     public FlowManagement getFlowManagement() {
         return flowManagement;
     }
 
     public void setFlowTypeName(String flowTypeName) {
         this.flowTypeName = flowTypeName;
         this.flow = null;
     }
 
     /**
      * @see org.amplafi.flow.FlowState#getFlowTypeName()
      */
     @Override
     public String getFlowTypeName() {
         return flowTypeName;
     }
 
     /**
      * @see org.amplafi.flow.FlowState#getFlow()
      */
     @Override
     public synchronized FlowImplementor getFlow() {
         if (this.flow == null && getFlowTypeName() != null) {
             this.flow = this.getFlowManagement().getInstanceFromDefinition(getFlowTypeName());
             if (this.flow == null) {
                 throw new IllegalArgumentException(getFlowTypeName() + ": no such flow definition");
             }
             this.flow.setFlowState(this);
         }
         return flow;
     }
 
     /**
      * @see org.amplafi.flow.FlowState#iterator()
      */
     @Override
     public Iterator<FlowActivity> iterator() {
         return this;
     }
 
     /**
      * @see org.amplafi.flow.FlowState#next()
      */
     @Override
     public FlowActivity next() {
         if (hasNext()) {
             return this.selectActivity(nextIndex(), true);
         } else {
             finishFlow();
             return null;
         }
 
     }
 
     /**
      * @see org.amplafi.flow.FlowState#previous()
      */
     @Override
     public FlowActivity previous() {
         return this.selectActivity(previousIndex(), false);
     }
 
     /**
      *
      * @see java.util.ListIterator#add(java.lang.Object)
      */
     @Override
     public void add(FlowActivity e) {
         throw new UnsupportedOperationException("cannot add FlowActivities");
     }
 
     /**
      * @see org.amplafi.flow.FlowState#hasVisibleNext()
      */
     @Override
     public boolean hasVisibleNext() {
         if (!hasNext()) {
             return false;
         }
         int count = getActivities().size();
         for (int i = getCurrentActivityIndex() + 1; i < count; i++) {
             if (!getActivity(i).isInvisible()) {
                 return true;
             }
         }
         return false;
     }
 
     /**
      * @see org.amplafi.flow.FlowState#hasVisiblePrevious()
      */
     @Override
     public boolean hasVisiblePrevious() {
         if (!hasPrevious()) {
             return false;
         }
         for (int i = getCurrentActivityIndex() - 1; i >= 0; i--) {
             if (!getActivity(i).isInvisible()) {
                 return true;
             }
         }
         return false;
     }
 
     /**
      * @see org.amplafi.flow.FlowState#hasNext()
      */
     @Override
     public boolean hasNext() {
         if (isCompleted()) {
             return false;
         } else {
 //        } else if ( getCurrentActivity().getFlowValidationResult().isValid()) {
             int count = getActivities().size();
             return this.getCurrentActivityIndex() < count - 1;
 //        } else {
 //            // TODO -- this seems bad because hasNext() seems like it should be constant.
 //            return false;
         }
     }
 
     /**
      * @see org.amplafi.flow.FlowState#hasPrevious()
      */
     @Override
     public boolean hasPrevious() {
         if (isCompleted()) {
             return false;
         } else {
             return this.getCurrentActivityIndex() > 0;
         }
     }
 
     /**
      * @see org.amplafi.flow.FlowState#nextIndex()
      */
     @Override
     public int nextIndex() {
         return this.getCurrentActivityIndex() + 1;
     }
 
     /**
      * @see org.amplafi.flow.FlowState#previousIndex()
      */
     @Override
     public int previousIndex() {
         return this.getCurrentActivityIndex() - 1;
     }
 
     /**
      * @see org.amplafi.flow.FlowState#remove()
      */
     @Override
     public void remove() {
         throw new UnsupportedOperationException("TODO: Auto generated");
     }
 
     @Override
     public void set(FlowActivity e) {
         throw new UnsupportedOperationException("TODO: Auto generated");
     }
 
     /**
      * @see org.amplafi.flow.FlowState#isTrue(java.lang.String)
      */
     @Override
     public boolean isTrue(String key) {
         Boolean b = getBoolean(key);
         return b != null && b;
     }
 
     /**
      * @see org.amplafi.flow.FlowState#getBoolean(java.lang.String)
      */
     @Override
     public Boolean getBoolean(String key) {
         Object value = getProperty(key);
         if (value == null) {
             return null;
         } else if ( value instanceof Boolean ) {
             return (Boolean) value;
         } else {
             return Boolean.valueOf(value.toString());
         }
     }
 
     /**
      * @see org.amplafi.flow.FlowState#getLong(java.lang.String)
      */
     @Override
     @Deprecated
     public Long getLong(String key) {
         return getRawLong(null, key);
     }
     @Override
     public FlowValidationResult getCurrentActivityFlowValidationResult(FlowActivityPhase flowActivityPhase, FlowStepDirection flowStepDirection) {
         FlowActivity currentActivity = this.getCurrentActivity();
         if (currentActivity == null) {
             return null;
         } else {
             if (FlowActivityPhase.advance == flowActivityPhase && flowStepDirection == FlowStepDirection.forward) {
                 // TODO temp hack
                 return currentActivity.getFlowValidationResult();
             } else {
                 return currentActivity.getFlowValidationResult(flowActivityPhase, flowStepDirection);
             }
         }
     }
     /**
      * @see org.amplafi.flow.FlowState#getCurrentActivityFlowValidationResult()
      */
     @Override
     public FlowValidationResult getCurrentActivityFlowValidationResult() {
         return this.getCurrentActivityFlowValidationResult(FlowActivityPhase.advance, FlowStepDirection.forward);
     }
 
     @Override
     public Map<String, FlowValidationResult> getFlowValidationResults(FlowActivityPhase flowActivityPhase, FlowStepDirection flowStepDirection) {
         Map<String, FlowValidationResult> result = new LinkedHashMap<String, FlowValidationResult>();
         for (FlowActivity activity : this.getActivities()) {
             FlowValidationResult flowValidationResult = activity.getFlowValidationResult(flowActivityPhase, flowStepDirection);
             if (!flowValidationResult.isValid()) {
                 result.put(activity.getFlowPropertyProviderName(), flowValidationResult);
             }
         }
         return result;
     }
 
     @Override
     public void setAfterPage(String afterPage) {
         this.setProperty(FSAFTER_PAGE, afterPage);
     }
 
     /**
      * @see org.amplafi.flow.FlowState#getAfterPage()
      */
     @Override
     public String getAfterPage() {
         // if (this.flowLifecycleState == canceled) {
         // return null;
         // }
         String page = getProperty(FSAFTER_PAGE, String.class);
         if (isNotBlank(page)) {
             return page;
         }
         page = getProperty(FSDEFAULT_AFTER_PAGE, String.class);
         if (isNotBlank(page)) {
             return page;
         } else {
             return flow == null ? null : flow.getDefaultAfterPage();
         }
     }
 
     /**
      * @see org.amplafi.flow.FlowState#isUpdatePossible()
      */
     @Override
     public boolean isUpdatePossible() {
        return isNotBlank(getUpdateText());
     }
 
     /**
      * @see org.amplafi.flow.FlowState#getUpdateText()
      */
     @Override
     public String getUpdateText() {
         return this.getProperty(FAUPDATE_TEXT, String.class);
     }
 
     /**
      * @see org.amplafi.flow.FlowState#isCancelPossible()
      */
     @Override
     public boolean isCancelPossible() {
         Boolean b = this.getBoolean(FSNO_CANCEL);
         return b == null || !b;
     }
 
     /**
      * @see org.amplafi.flow.FlowState#getCancelText()
      */
     @Override
     public String getCancelText() {
         return this.getProperty(FSCANCEL_TEXT, String.class);
     }
 
     @Override
     public void setCancelText(String cancelText) {
         this.setProperty(FSCANCEL_TEXT, cancelText);
     }
 
     /**
      * @see org.amplafi.flow.FlowState#getFinishText()
      */
     @Override
     public String getFinishText() {
         return this.getProperty(FSFINISH_TEXT, String.class);
     }
 
     @Override
     public void setFinishText(String finishText) {
         this.setProperty(FSFINISH_TEXT, finishText);
     }
 
     /**
      * @see org.amplafi.flow.FlowState#setFinishKey(java.lang.String)
      */
     @Override
     public void setFinishKey(String type) {
         this.setProperty(FSALT_FINISHED, type);
     }
 
     /**
      * @see org.amplafi.flow.FlowState#getFinishKey()
      */
     @Override
     public String getFinishKey() {
         return this.getProperty(FSALT_FINISHED, String.class);
     }
 
     @Override
     public void setFlowLifecycleState(FlowStateLifecycle flowStateLifecycle) {
         if ( this.flowStateLifecycle != flowStateLifecycle) {
             FlowStateLifecycle previousFlowLifecycleState = this.flowStateLifecycle;
             this.flowStateLifecycle = STATE_CHECKER.checkAllowed(this.flowStateLifecycle, flowStateLifecycle);
             this.getFlowManagement().lifecycleChange(this, previousFlowLifecycleState);
         }
     }
 
     /**
      * @see org.amplafi.flow.FlowState#getFlowStateLifecycle()
      */
     @Override
     public FlowStateLifecycle getFlowStateLifecycle() {
         return this.flowStateLifecycle;
     }
 
     /**
      * @see org.amplafi.flow.FlowState#isCompleted()
      */
     @Override
     public boolean isCompleted() {
         return this.flowStateLifecycle != null && this.flowStateLifecycle.isTerminalState();
     }
 
     /**
      * TODO: note that there exists an issue: if a property for the current FA is PropertyUsage.initialize,
      * because PropertyUsage.initialize does not clear out the old values but just ignores them.
      */
     @Override
     public boolean isPropertySet(String key) {
         // HACK : too problematic need better way to ask if a FlowPropertyValueProvider can actually return a value.
 //        FlowPropertyDefinition flowPropertyDefinition = getFlowPropertyDefinitionWithCreate(key, null, null);
 //        if ( !flowPropertyDefinition.isDefaultObjectAvailable(this)) {
             return isPropertyValueSet(key);
 //        } else {
 //            return true;
 //        }
     }
 
     /**
      * @see org.amplafi.flow.flowproperty.FlowPropertyProviderWithValues#isPropertyValueSet(java.lang.String)
      */
     @Override
     public boolean isPropertyValueSet(String key) {
         return getRawProperty(key) != null;
     }
 
     @SuppressWarnings("unchecked")
     @Override
     public <T> T getProperty(String key) {
         return (T) getProperty(key, null);
     }
 
     @SuppressWarnings("unchecked")
     @Override
     public <T> T getProperty(String key, Class<? extends T> expected) {
         if (isActive()) {
             FlowActivity currentActivity = getCurrentActivity();
             return currentActivity.getProperty(key, expected);
         } else {
             FlowPropertyDefinitionImplementor flowPropertyDefinition = getFlowPropertyDefinitionWithCreate(key, expected, null);
             return (T) getPropertyWithDefinition(this, flowPropertyDefinition);
         }
     }
     @Override
     public <T> T getProperty(Class<? extends T> expected) {
         return getProperty(FlowPropertyDefinitionBuilder.toPropertyName(expected), expected);
     }
 
     /**
      * @see org.amplafi.flow.FlowState#isActive()
      */
     @Override
     public boolean isActive() {
         // TODO | HACK Seems like we should be looking at FlowLifecycleState here not the index range.
         return this.getCurrentActivityIndex() >= 0 && getCurrentActivityIndex() < size();
     }
 
 
     /**
      * @see org.amplafi.flow.flowproperty.FlowPropertyProvider#getFlowPropertyProviderFullName()
      */
     @Override
     public String getFlowPropertyProviderFullName() {
         return getFlowTypeName()+"."+this.getFlowPropertyProviderName();
     }
 
     /**
      * @see org.amplafi.flow.flowproperty.FlowPropertyProvider#getFlowPropertyProviderName()
      */
     @Override
     public String getFlowPropertyProviderName() {
         return getLookupKey();
     }
 
     /**
      * @see org.amplafi.flow.flowproperty.FlowPropertyProvider#getPropertyDefinitions()
      */
     @Override
     public Map<String, FlowPropertyDefinition> getPropertyDefinitions() {
         return this.getFlow().getPropertyDefinitions();
     }
 
     @SuppressWarnings("unchecked")
     @Override
     public <T extends FlowPropertyDefinition> T getFlowPropertyDefinition(String key) {
         T flowPropertyDefinition = null;
         if ( this.getFlow() != null ) {
             flowPropertyDefinition = (T) this.getFlow().getFlowPropertyDefinition(key);
         }
         if (flowPropertyDefinition == null && this.getFlowManagement() != null) {
             // (may not be assigned to a flowManagement any more -- historical FlowState )
             FlowPropertyDefinitionBuilder flowPropertyDefinitionBuilder = this.getFlowManagement().getFactoryFlowPropertyDefinitionBuilder(key, null);
             if ( flowPropertyDefinitionBuilder !=null ) {
                 flowPropertyDefinition = (T) flowPropertyDefinitionBuilder.toFlowPropertyDefinition(getFlowManagement().getFlowTranslatorResolver());
             }
         }
         return flowPropertyDefinition;
     }
 
 
     /**
      * @see org.amplafi.flow.FlowState#setAllProperties(java.util.Map)
      */
     @Override
     public void setAllProperties(Map<?, ?> exportedMap) {
         for(Map.Entry<String, ?>entry : NotNullIterator.<Map.Entry<String, ?>>newNotNullIterator(exportedMap)) {
             Object value = entry.getValue();
             String key = entry.getKey();
             setProperty(key, value);
         }
     }
 
     /**
      * @see org.amplafi.flow.FlowState#setProperty(java.lang.String,
      *      java.lang.Object)
      */
     @SuppressWarnings("unchecked")
     @Override
     public <T> void setProperty(String key, T value) {
         if (isActive()) {
             FlowActivity currentActivity = getCurrentActivity();
             currentActivity.setProperty(key, value);
         } else {
             Class<T> expected = (Class<T>) (value == null?null:value.getClass());
             FlowPropertyDefinitionImplementor flowPropertyDefinition = getFlowPropertyDefinitionWithCreate(key, expected, value);
             setPropertyWithDefinition(null, flowPropertyDefinition, value);
         }
     }
     @Override
     public <T> void setProperty(T value) {
         ApplicationNullPointerException.notNull(value, "Cannot set by className if the property is null");
         if (isActive()) {
             FlowActivity currentActivity = getCurrentActivity();
             currentActivity.setProperty(value);
         } else {
             Class<T> expected = (Class<T>) value.getClass();
             String key = FlowPropertyDefinitionBuilder.toPropertyName(expected);
             FlowPropertyDefinitionImplementor flowPropertyDefinition = getFlowPropertyDefinitionWithCreate(key, expected, value);
             setPropertyWithDefinition(null, flowPropertyDefinition, value);
         }
     }
 
     @SuppressWarnings("unchecked")
     private <T, FP extends FlowPropertyDefinition> FP getFlowPropertyDefinitionWithCreate(String key, Class<T> expected, T value) {
         FP flowPropertyDefinition = (FP)getFlowPropertyDefinition(key);
 
         if (flowPropertyDefinition == null) {
             flowPropertyDefinition = (FP) getFlowManagement().createFlowPropertyDefinition(getFlow(), key, expected, value);
         }
         return flowPropertyDefinition;
     }
 
     /**
      * @see org.amplafi.flow.FlowState#setDefaultAfterPage(java.lang.String)
      */
     @Override
     public void setDefaultAfterPage(String pageName) {
         this.setProperty(FSDEFAULT_AFTER_PAGE, pageName);
     }
 
     /**
      * @see org.amplafi.flow.FlowState#getDefaultAfterPage()
      */
     @Override
     public String getDefaultAfterPage() {
         String property = this.getProperty(FSDEFAULT_AFTER_PAGE);
         return property == null ? this.getFlow().getDefaultAfterPage() : property;
     }
 
     /**
      * @see org.amplafi.flow.FlowState#isNotCurrentAllowed()
      */
     @Override
     public boolean isNotCurrentAllowed() {
         return this.getFlow().isNotCurrentAllowed();
     }
 
     /**
      * @see org.amplafi.flow.FlowState#getFlowValuesMap()
      */
     @Override
     public FlowValuesMap getFlowValuesMap() {
         if (this.flowValuesMap == null) {
             this.flowValuesMap = new DefaultFlowValuesMap();
         }
         return this.flowValuesMap;
     }
 
     @Override
     public void setFlowValuesMap(FlowValuesMap flowValuesMap) {
         this.flowValuesMap = flowValuesMap;
     }
 
     public Log getLog() {
         // TODO handle historical FlowStates ( no FlowManagement )
         if ( getFlowManagement() == null ) {
             return null;
         } else {
             return getFlowManagement().getLog();
         }
     }
 
     protected String getRawProperty(FlowPropertyProvider flowPropertyProvider, String key) {
         FlowPropertyDefinition propertyDefinition = getFlowPropertyDefinitionWithCreate(key, null, null);
         return getRawProperty(flowPropertyProvider, propertyDefinition);
     }
     @Override
     public String getRawProperty(FlowPropertyProvider flowPropertyProvider, FlowPropertyDefinition propertyDefinition) {
         String key = propertyDefinition.getName();
         String namespace = ((FlowPropertyDefinitionImplementor)propertyDefinition).getNamespaceKey(this, flowPropertyProvider);
         String value;
         if ( getFlowValuesMap().containsKey(namespace, key)) {
             // A flow may set a value to null that is *not* copied out to the global namespace (yet or depending on PropertyUsage never)
             // this is a reasonable use case, so allow for the (namespace,key) to have a null value.
             value = getRawProperty(namespace, key);
         } else if (propertyDefinition.getPropertyUsage().isExternallySettable()) {
         	//Property is externally settable so trying default namespace..
         	value = getRawProperty(NamespaceMapKey.NO_NAMESPACE, key);
         } else {
             value = null;
         }
         return value;
     }
 
     /**
      * Get an object from the database.  If the object does not exist in the database a value of null will be returned.
      *
      * This uses a Hibernate get() call rather than a load() call to prevent us from getting errors if an entity can't be found
      * by the ID passed in.  This exception gets thrown whenever an object field is first accessed which can be well above the data
      * access code.
      * @param <T>
      * @param <K>
      *
      * @param clazz The class of the object to load
      * @param entityId The id of the object to load
      * @return The loaded object or null if it doesn't exist
      */
     public <T, K> T load(Class<? extends T> clazz, K entityId) {
         // We need to use get() to load entities as opposed to load() in case we try to get an entity via an id that doesn't pair
         // to a record in the database.  If we can figure out a way to validate the record actually exists then we can
         // switch this to use load() and not incur the up-front overhead.
         return getFlowManagement().getFlowTx().get(clazz, entityId, true);
     }
 
     /**
      *
      * @see org.amplafi.flow.impl.FlowStateImplementor#getRawLong(org.amplafi.flow.FlowActivity, java.lang.String)
      */
     @Override
     public Long getRawLong(FlowActivity flowActivity, String key) {
         String value = getRawProperty(flowActivity, key);
         if (isNotEmpty(value) && isNumeric(value)) {
             return Long.parseLong(value);
         } else {
             return null;
         }
 
     }
 
     /**
      * @see org.amplafi.flow.FlowStateProvider#getFlowState()
      */
     @SuppressWarnings("unchecked")
     @Override
     public <FS extends FlowState> FS getFlowState() {
         return (FS) this;
     }
 
     protected void warn(String message) {
         Log log = getLog();
         if ( log != null ) {
             log.warn(message);
         }
     }
 
     @Override
     public boolean isSinglePropertyFlow() {
         return flow.isSinglePropertyFlow();
     }
 
     @Override
     public void serializeSinglePropertyValue(JSONWriter jsonWriter) {
         String singlePropertyName = flow.getSinglePropertyName();
         FlowPropertyDefinition flowPropertyDefinition = this.getFlowPropertyDefinition(singlePropertyName);
         // TODO : SECURITY : HACK This important security check to make sure that secure properties are not released
         // to users. This security check needs to built in to the flow code itself. We must not rely on the renderer to do
         // security checks.
         // this is an important valid use case for generating a temp api key that is returned via a callback uri not directly
         if ( flowPropertyDefinition.isExportable()) {
             flowPropertyDefinition.serialize(jsonWriter, this.getProperty(singlePropertyName));
         }
     }
 
     @Override
     public String toString() {
         return this.lookupKey + " [type:" + this.flowTypeName + "]; current Activity="+this.getCurrentActivity()+"; flowStateMap="+this.flowValuesMap;
     }
 
     @Override
     public boolean isPersisted() {
     	return false;
     }
 
     protected class FlowActivityIterator implements Iterator<FlowActivityImplementor> {
 
         private FlowStepDirection flowStepDirection;
         private int next;
         // based on the flowStepDirection. if true, then there another FlowActivity in the same direction as the current flowActivity
         private boolean canContinue;
         // if true, currentActivity indicated that it has finished processing and the FlowState should immediately advanced. Used primarily for invisible FlowActivities.
         // true by default to handle 0 FA flows.
         private boolean lastFlowActivityActivateAutoFinished =true;
 
         FlowActivityIterator(int next) {
             // used to help determine if the flow is not altering which FlowActivity is current. ( refresh case )
             int originalIndex = getCurrentActivityIndex();
             FlowStepDirection flowStepDirection = FlowStepDirection.get(originalIndex, next);
             this.flowStepDirection= flowStepDirection;
             this.next = next;
             this.canContinue = FlowStateImpl.this.size() > 0;
         }
         public boolean isTimeToFinish() {
             return isLastFlowActivityActivateAutoFinished() && getFlowStepDirection() == FlowStepDirection.forward && !isCanContinue();
         }
         public void activate() {
             FlowActivityImplementor currentActivity = next();
 
             lastFlowActivityActivateAutoFinished = activateFlowActivity(currentActivity, flowStepDirection);
         }
         @Override
         public boolean hasNext() {
             return lastFlowActivityActivateAutoFinished && canContinue;
         }
 
         @Override
         public FlowActivityImplementor next() {
             setCurrentActivityIndex(next);
 
             FlowActivityImplementor currentActivity = FlowStateImpl.this.getCurrentActivity();
             switch(flowStepDirection) {
             case forward:
                 next = FlowStateImpl.this.nextIndex();
                 canContinue = FlowStateImpl.this.hasNext();
                 break;
             case backward:
                 next = FlowStateImpl.this.previousIndex();
                 canContinue = FlowStateImpl.this.hasPrevious();
                 break;
             default:
                 canContinue = false;
                 break;
             }
             return currentActivity;
         }
 
         @Override
         public void remove() {
             throw new UnsupportedOperationException();
         }
         public boolean isCanContinue() {
             return canContinue;
         }
         public boolean isLastFlowActivityActivateAutoFinished() {
             return lastFlowActivityActivateAutoFinished;
         }
         public FlowStepDirection getFlowStepDirection() {
             return flowStepDirection;
         }
     }
 }
