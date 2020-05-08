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
 package org.amplafi.flow.web.components;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.amplafi.flow.Flow;
 import org.amplafi.flow.FlowActivity;
 import org.amplafi.flow.FlowDefinitionsManager;
 import org.amplafi.flow.FlowStateLifecycle;
 import org.amplafi.flow.FlowStateProvider;
 import org.amplafi.flow.FlowState;
 import org.amplafi.flow.impl.FlowStateImplementor;
 import org.amplafi.flow.launcher.StartFromDefinitionFlowLauncher;
 import org.amplafi.flow.validation.FlowValidationException;
 import org.amplafi.flow.web.BaseFlowComponent;
 import org.amplafi.flow.web.FlowAwarePage;
 import org.amplafi.flow.web.FlowFormDelegate;
 import org.amplafi.flow.web.FlowWebUtils;
 import org.apache.tapestry.IActionListener;
 import org.apache.tapestry.IComponent;
 import org.apache.tapestry.IJSONRender;
 import org.apache.tapestry.IPage;
 import org.apache.tapestry.IRender;
 import org.apache.tapestry.IRequestCycle;
 import org.apache.tapestry.PageRedirectException;
 import org.apache.tapestry.annotations.Bean;
 import org.apache.tapestry.annotations.Parameter;
 import org.apache.tapestry.event.PageEvent;
 import org.apache.tapestry.event.PageValidateListener;
 import org.apache.tapestry.json.IJSONWriter;
 import org.apache.tapestry.valid.IValidationDelegate;
 import org.apache.tapestry.valid.ValidationDelegate;
 
 
 import static org.apache.commons.collections.CollectionUtils.*;
 
 import java.util.Arrays;
 
 /**
  * Tapestry component class that generates the html template for a flow.
  * This component has a variable component type. The component type name
  * is the name of the flow + 'FullFlow'. So for example,
  * <pre>
  * &lt;form jwcid="@CreateAlertFullFlow"&gt;&lt;/form&gt;
  * </pre>
  *
  * will create the html that represents the 'CreateAlert' flow.
  * <p/>
  * By default the flow referenced by this component will be started
  * if a flow of the given type has not already been started.
  * <p/>
  * If a flow of that type has been started, the flow will be displayed
  * in whatever state it is in. This can be turned off with {@link #isAutoStart()}=false.
  * Usually, autoStart should be false if multiple flows are on the same page.
  * Otherwise, the first flow is started automatically. If there are multiple
  * FullFlow component objects on the page, the flow that should be started is determined
  * by the user clicking a {@link FlowEntryPoint} link/button. Starting flows
  * any other way is usually not correct.
  * <p/>
  * So in our earlier example, if a CreateAlert flow is not active in the current user session,
  * then the CreateAlert flow will be started by FullFlowComponent.
  * If a CreateAlert flow is in progress on the third FlowActivity then that running flow
  * will be displayed with the third FlowActivity displayed to the user.
  * <p/>
  * Like {@link FlowEntryPoint}, initial values can be passed to the
  * flow with {@link #getInitialValues()}. (see {@link FlowEntryPoint#getInitialValues()}.)
  * <p/>
  * Each {@link org.amplafi.flow.FlowActivity} in the {@link Flow}
  * should reference a component. FullFlowComponent wires all of the FlowActivity's component's required parameters
  * wired to the FlowActivity using a {@link org.amplafi.flow.web.bindings.FlowPropertyBinding}
  */
 public abstract class FullFlowComponent extends BaseFlowComponent implements FlowStateProvider,
         IJSONRender, PageValidateListener {
 
     /**
      *
      */
     public static final String FLOW_BORDER_COMPONENT_NAME = "flowBorder";
 
     /**
      * @return true if the flow control widget should be displayed.
      */
     @Parameter
     public abstract boolean getHideFlowControl();
 
     /**
      * @param listener runs if flow ends somehow ( cancel or finish).
      * Runs after cancel/finish listener.
      */
     @Parameter
     public abstract void setEndListener(IActionListener listener);
     public abstract IActionListener getEndListener();
 
     /**
      * @param listener runs if flow is canceled.
      */
     @Parameter(aliases="onCancel")
     public abstract void setCancelListener(IActionListener listener);
     public abstract IActionListener getCancelListener();
 
     /**
      * @param listener What to do on next button. Default behavior it to advance
      * the flow to the next step.
      */
     @Parameter(aliases="onNext")
     public abstract void setNextListener(IActionListener listener);
     public abstract IActionListener getNextListener();
 
     /**
      * @param listener What to do on previous button. Default behavior it to move back
      * the flow to the previous step.
      */
     @Parameter(aliases="onPrevious")
     public abstract void setPreviousListener(IActionListener listener);
     public abstract IActionListener getPreviousListener();
 
     /**
      * @param listener runs if flow is finished.
      */
     @Parameter(aliases="onFinish")
     public abstract void setFinishListener(IActionListener listener);
     public abstract IActionListener getFinishListener();
 
     @Override
     @Parameter(defaultValue = "true")
     public abstract boolean isAsync();
 
     /**
      *
      * @return the id of an active flow of type {@link #getFlowName()} that this
      * flow component will control.
      */
     @Parameter
     public abstract String getFlowId();
     public abstract FlowDefinitionsManager getFlowDefinitionsManager();
 
     /**
      * Automatically start a flow of the required type if one doesn't already exist.
      * @return should automatically start an instance of the required flow.
      */
     @Parameter
     public abstract boolean isAutoStart();
 
     /**
      * @return The css class to pass to the form.
      */
     @Parameter(name="class")
     public abstract String getClassName();
 
     /**
      * @return The form delegate.
      */
     @Parameter(defaultValue = "bean:defaultDelegate")
     public abstract IValidationDelegate getDelegate();
 
     /**
      * follows same rules as {@link FlowEntryPoint#getInitialValues()}
      * when starting a flow.
      * @return  {@link FlowEntryPoint#getInitialValues()}
      */
     @Parameter
     public abstract Iterable<String> getInitialValues();
     @Bean(ValidationDelegate.class)
     public abstract IValidationDelegate getDefaultDelegate();
 
     /**
      * @return
      * The {@link FlowBorder} component used in this flow.
      */
     public FlowBorder getFlowBorder() {
         return (FlowBorder)this.getComponent(FLOW_BORDER_COMPONENT_NAME);
     }
 
     // cancel handling needed due to TAPESTRY-1673
     public void doCancelForm() {
         FlowState state = getFlowState();
         if (state!=null) {
             String page = state.cancelFlow();
             FlowWebUtils.activatePageIfNotNull(getPage().getRequestCycle(), page, state);
         }
     }
 
     /**
      * Refreshes form, mainly to enable async server validation.
      * @param cycle
      */
     public void doRefreshForm(IRequestCycle cycle) {
         cycle.getResponseBuilder().updateComponent(this.getForm().getClientId());
     }
 
     @Override
     public void renderComponent(IJSONWriter writer, IRequestCycle cycle) {
         FlowFormDelegate delegate = (FlowFormDelegate) getDelegate();
         List<IRender> tracking = delegate.getStoredRenderers();
         writer.object().put("type", "validation");
         writer.object().put("form", getComponent("flowForm").getClientId());
         for (IRender err : tracking) {
             if (err instanceof IJSONRender) {
                 ((IJSONRender)err).renderComponent(writer, cycle);
             }
         }
     }
     @Override
     public void pageValidate(PageEvent event) {
         /* the broadcastProvider is not initialized yet -- has security run yet? */
         FlowStateImplementor flow = (FlowStateImplementor) getFlowState();
         if ( flow != null ) {
             // Check to see if the current page is the page that the flow things should be displayed
             // while a flow is running the page ( not just the active component ) may change.
             // this happens in autostart situations, when a FlowActivity has a different page than the flow's default pages
             // or if a flow is morphed into another flow.
 
             String flowPageName = flow.getCurrentPage();
             String pageName = this.getPage().getPageName();
             if( flowPageName != null && !pageName.equals(flowPageName)) {
                if (!(this.getPage() instanceof FlowAwarePage) || isEmpty(((FlowAwarePage)this.getPage()).getExpectedFlowDefinitions())
                    || !((FlowAwarePage)this.getPage()).getExpectedFlowDefinitions().contains(flow.getFlowTypeName())) {
                     throw new PageRedirectException(flowPageName);
                 } else {
                     flow.setCurrentPage(pageName);
                 }
             }
         }
     }
     /**
      * This value cannot be cached because one FlowState may be ending and another beginning.
      * note: this method name is used in {@link org.amplafi.flow.web.resolvers.FlowAwareTemplateSourceDelegate}.
      * @return the flow attached to this FullFlow instance
      */
     @Override
     public FlowState getFlowState() {
         FlowState flow = getAttachedFlowState();
         if ( flow == null && getFlowId() != null ) {
             flow = getFlowManagement().getFlowState(getFlowId());
         }
         if ( flow != null ) {
             if (flow.getFlowStateLifecycle() != FlowStateLifecycle.started) {
                 // this situation arises if the user clears
                 // an flow autostarted
                 flow = null;
             } else if(!flow.getFlowTypeName().equals(getFlowName())) {
                 // another flow component on the page should be handling this.
                 return null;
             }
         }
         if ( flow == null ) {
             // HACK: what happens if 2 different flows of same type are active?
             List<String> expectedFlows = getExpectedFlows();
             flow = getFlowManagement().getFirstFlowStateByType(expectedFlows);
             if ( flow != null && !flow.getFlowTypeName().equals(getFlowName())) {
                 // only display this FullFlowComponent if handling the flow that is the 'first' flow of the flows displayed on the current page.
                 // however, another FullFlowComponent is handling displaying this flow. So this FullFlowComponent should
                 // quietly do nothing.
                 flow = null;
             } else if ( flow == null && isShouldAutoStart() ) {
                 // no flows on this page are active. This FullFlowComponent is an autoStart so it should do its thing.
                 // and start.
                 getFlowManagement().getLog().debug("Auto starting "+getFlowName()+" on page "+getPage().getPageName()+" activeflows="+getFlowManagement().getFlowStates());
                 StartFromDefinitionFlowLauncher flowLauncher = new StartFromDefinitionFlowLauncher(getFlowName(), getContainer(), getInitialValues(), getFlowManagement(), getFlowName());
                 try {
                     flow = flowLauncher.call();
                 } catch (FlowValidationException e) {
 //                    getFlowResultHandler().handleValidationTrackings(e.getTrackings(), this);
                     return null;
                 }
             }
         }
         return flow;
     }
 
     /**
      * @return
      */
     private boolean isShouldAutoStart() {
         if ( !isAutoStart() ) {
             return false;
         } else {
             return isNoExpectedFlowStarted();
         }
     }
 
     /**
      * @return
      */
     private boolean isNoExpectedFlowStarted() {
         IPage page = getPage();
         if ( page instanceof FlowAwarePage) {
             return ((FlowAwarePage)page).isNoExpectedFlowStarted();
         } else {
             return true;
         }
     }
 
     private List<String> getExpectedFlows() {
         IPage page = getPage();
         List<String> result = null;
         if ( page instanceof FlowAwarePage) {
             result = ((FlowAwarePage)page).getExpectedFlowDefinitions();
         }
         // if not explicitly listed then assume this flow can be on the page.
         if(isEmpty(result)){
             result = Arrays.asList(getFlowName());
         }
         return result;
     }
 
     /**
      * note: this method name is used in {@link org.amplafi.flow.web.resolvers.FlowAwareTemplateSourceDelegate}.
      * @return true if this component should be rendered.
      */
     public boolean isVisibleFlow() {
         FlowState flowToUse = getFlowState();
         return flowToUse != null;
     }
     public String getFlowName() {
         return getSpecification().getDescription();
     }
 
     public IComponent getCurrentBlock() {
         FlowState flow = getFlowState();
         if ( flow != null ) {
             if ( flow.getFlowStateLifecycle() != FlowStateLifecycle.started) {
                 String message = getStartErrorMessage(flow) + "' but flow is not in the 'started' state. Flow state: " + flow.getFlowStateLifecycle() + "; Values: " + flow.getFlowValuesMap();
                 throw new IllegalStateException(message);
             }
             int activity = flow.getCurrentActivityIndex();
             String blockName = FlowWebUtils.getBlockName(activity);
             IComponent comp = getComponentSafe(blockName);
             if(comp==null) {
                 FlowActivity flowActivity = flow.getCurrentActivity();
                 if(flowActivity == null){
                     String message = getStartErrorMessage(flow) + "' but current activity (#" + activity + ") is null. Flow state: " + flow.getFlowStateLifecycle() + "; Values: " + flow.getFlowValuesMap() ;
                     throw new IllegalStateException(message);
                 } else {
                     String message = getStartErrorMessage(flow) + "' but there is no component named '" + blockName + "'. This should be the block containing the FlowActivity named '" + flowActivity.getFlowPropertyProviderName() + "' (activity #" + activity + ") ";
                     if ( flowActivity.isInvisible() ) {
                         throw new IllegalStateException(message+" -- this is an invisible activity");
                     } else {
                         throw new IllegalStateException(message+" -- flow activity claims it is supposed to be visible. Are you sure this flow is defined on this page?");
                     }
                 }
             }
             return comp;
         } else {
             return null;
         }
     }
 
     /**
      * @param flow
      * @return message string
      */
     private String getStartErrorMessage(FlowState flow) {
         return "On page '" +this.getPage().getPageName()+"' trying to display flow '"+
                         flow.getFlowTypeName();
     }
 
     /**
      * @return
      * The flow that this component implements.
      */
     public Flow getComponentFlow() {
         return getFlowDefinitionsManager().getFlowDefinition(getFlowName());
     }
 
     private IComponent getComponentSafe(String id) {
         return (IComponent)getComponents().get(id);
     }
 
     /**
      * @return
      * List of flow components that were declared to form this flow.
      */
     public List<IComponent> getFlowComponents() {
         List<IComponent> components = new ArrayList<IComponent>();
         for(int counter = 0; getComponent(FlowWebUtils.getFlowComponentName(counter)) != null; counter++) {
             components.add(getComponent(FlowWebUtils.getFlowComponentName(counter)));
         }
         return components;
     }
 }
