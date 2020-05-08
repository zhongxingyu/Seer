 package org.amplafi.flow.web;
 
 import static com.sworddance.util.CUtilities.isNotEmpty;
 import static com.sworddance.util.CUtilities.put;
 import static org.amplafi.flow.FlowConstants.FSREFERRING_URL;
 import static org.amplafi.flow.FlowConstants.FSRENDER_RESULT;
 import static org.amplafi.flow.FlowConstants.HTML;
 import static org.amplafi.flow.FlowConstants.JSON;
 import static org.amplafi.flow.launcher.FlowLauncher.ADVANCE_TO_END;
 import static org.amplafi.flow.launcher.FlowLauncher.AS_FAR_AS_POSSIBLE;
 import static org.amplafi.flow.launcher.FlowLauncher.COMPLETE_FLOW;
 import static org.amplafi.flow.launcher.FlowLauncher.FLOW_ID;
 import static org.amplafi.flow.launcher.FlowLauncher.FLOW_STATE_JSON_KEY;
 import static org.apache.commons.lang.StringUtils.isNotBlank;
 import static org.apache.commons.lang.StringUtils.join;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.io.Writer;
 import java.util.Collection;
 import java.util.List;
 import java.util.Map;
 
 import org.amplafi.flow.Flow;
 import org.amplafi.flow.FlowActivity;
 import org.amplafi.flow.FlowActivityPhase;
 import org.amplafi.flow.FlowConstants;
 import org.amplafi.flow.FlowDefinitionsManager;
 import org.amplafi.flow.FlowManagement;
 import org.amplafi.flow.FlowManager;
 import org.amplafi.flow.FlowPropertyDefinition;
 import org.amplafi.flow.FlowState;
 import org.amplafi.flow.FlowStateJsonRenderer;
 import org.amplafi.flow.FlowStateLifecycle;
 import org.amplafi.flow.FlowStepDirection;
 import org.amplafi.flow.FlowUtils;
 import org.amplafi.flow.ServicesConstants;
 import org.amplafi.flow.launcher.FlowLauncher;
 import org.amplafi.flow.validation.FlowValidationException;
 import org.amplafi.flow.validation.FlowValidationResult;
 import org.amplafi.json.JSONWriter;
 import org.amplafi.json.renderers.IterableJsonOutputRenderer;
 
 import org.apache.commons.logging.Log;
 import com.sworddance.util.CUtilities;
 
 public class BaseFlowService implements FlowService {
 
     public static final String USE_CURRENT = "current";
 
     // see InsertionPoint.js
     private static final String FS_BACKGROUND_FLOW = "fsInBackground";
 
     protected static final String SCRIPT_CONTENT_TYPE = "text/javascript";
 
     private FlowManager flowManager;
 
     private Log log;
 
     /**
      * if {@link org.amplafi.flow.launcher.FlowLauncher#COMPLETE_FLOW} is not supplied - this is the
      * default value to use.
      */
     protected String defaultComplete;
 
     /**
      * the default way the results should be rendered. "html", "json", etc.
      */
     protected String renderResultDefault;
 
     protected boolean discardSessionOnExit;
 
     private boolean assumeApiCall;
 
     protected FlowDefinitionsManager flowDefinitionsManager;
 
     public BaseFlowService() {
         super();
     }
 
     public BaseFlowService(FlowDefinitionsManager flowDefinitionsManager, FlowManager flowManager) {
         this.setFlowDefinitionsManager(flowDefinitionsManager);
         this.setFlowManager(flowManager);
     }
 
     /**
      * @param flowDefinitionsManager the flowDefinitionsManager to set
      */
     public void setFlowDefinitionsManager(FlowDefinitionsManager flowDefinitionsManager) {
         this.flowDefinitionsManager = flowDefinitionsManager;
     }
 
     /**
      * @return the flowDefinitionsManager
      */
     public FlowDefinitionsManager getFlowDefinitionsManager() {
         return flowDefinitionsManager;
     }
 
     public void service(FlowRequest flowRequest) throws IOException, FlowNotFoundException, FlowRedirectException {
         String flowType = flowRequest.getParameter(ServicesConstants.FLOW_TYPE);
         String flowId = flowRequest.getParameter(FLOW_ID);
         String renderResult = flowRequest.getParameter(FSRENDER_RESULT);
         PrintWriter writer = flowRequest.getWriter();
 
         if (FlowConstants.JSON_DESCRIBE.equals(renderResult)) {
             if (flowType == null) {
                 // if no flow type then return a json array of all the flow types.
                 Collection<String> flowTypes = flowDefinitionsManager.getFlowDefinitions().keySet();
                 JSONWriter jWriter = new JSONWriter();
                 IterableJsonOutputRenderer.INSTANCE.toJson(jWriter, flowTypes);
                 writer.append(jWriter.toString());
                 return;
             } else {
                 CharSequence description = describeService(flowType, renderResult);
                 writer.append(description);
                 return;
             }
         }
 
         Map<String, String> initial = FlowUtils.INSTANCE.createState(FlowConstants.FSAPI_CALL, isAssumeApiCall());
         // TODO map cookie to the json flow state.
         String cookieString = flowRequest.getParameter(ServicesConstants.COOKIE_OBJECT);
         if (isNotBlank(cookieString)) {
             initial.put(ServicesConstants.COOKIE_OBJECT, cookieString);
         }
 
         List<String> keyList = flowRequest.getParameterNames();
         if (isNotEmpty(keyList)) {
             for (String key : keyList) {
                 String value = flowRequest.getParameter(key);
                 if (value != null) {
                     // we do allow the value to be blank ( value may be existence of parameter)
                     initial.put(key, value);
                 }
             }
         }
 
         String referingUri = flowRequest.getReferingUri();
         put(initial, FSREFERRING_URL, referingUri);
         String complete = flowRequest.getParameter(COMPLETE_FLOW);
         String background = flowRequest.getParameter(FS_BACKGROUND_FLOW);
         String advanceToActivity = flowRequest.getParameter(FlowLauncher.ADV_FLOW_ACTIVITY);
         boolean currentFlow = background == null;
         doActualService(flowRequest, flowType, flowId, renderResult, writer, initial, complete, advanceToActivity, currentFlow);
     }
 
     protected FlowState doActualService(FlowRequest request, String flowType, String flowId, String renderResult, PrintWriter writer,
         Map<String, String> initial, String complete, String advanceToActivity, boolean currentFlow) throws IOException, FlowNotFoundException, FlowRedirectException {
         return completeFlowState(flowType, flowId, renderResult, initial, complete, currentFlow, writer, advanceToActivity);
     }
 
     protected FlowState getFlowState(String flowType, String flowId, String renderResult, Map<String, String> initial, Writer writer,
         boolean currentFlow) throws IOException {
         FlowState flowState = null;
         if (isNotBlank(flowId)) {
             flowState = getFlowManagement().getFlowState(flowId);
         }
         if (flowState == null) {
             if (isNotBlank(flowType)) {
                 if (!getFlowManager().isFlowDefined(flowType)) {
                     renderError(writer, flowType + ": no such flow type", renderResult, null, new FlowNotFoundException(flowType));
                     return null;
                 }
 
                 if (USE_CURRENT.equals(flowId)) {
                     flowState = getFlowManagement().getFirstFlowStateByType(flowType);
                 }
 
                 if (flowState == null) {
                     String returnToFlowLookupKey = null;
                     flowState = getFlowManagement().startFlowState(flowType, currentFlow, initial, returnToFlowLookupKey);
                     if (flowState == null || flowState.getFlowStateLifecycle() == FlowStateLifecycle.failed) {
                         renderError(writer, flowType + ": could not start flow type", renderResult, flowState, null);
                         return null;
                     }
                 }
             } else {
                String error = String.format("Query String for request didn't contain %s or %s. At least one needs to be specified.",
                    ServicesConstants.FLOW_TYPE, FLOW_ID);
                renderError(writer, error, renderResult, null, null);
                 return null;
             }
         }
         return flowState;
     }
 
     public void setFlowManager(FlowManager flowManager) {
         this.flowManager = flowManager;
     }
 
     public FlowManager getFlowManager() {
         return flowManager;
     }
 
     public FlowManagement getFlowManagement() {
         return getFlowManager().getFlowManagement();
     }
 
     public void setLog(Log log) {
         this.log = log;
     }
 
     public Log getLog() {
         return log;
     }
 
     protected String advanceFlow(FlowState flowState) {
         String error = "some error occured";
         if (flowState.isCurrentActivityCompletable()) {
             try {
                 flowState.next();
                 error = null;
             } catch (FlowValidationException flowValidationException) {
                 getLog().debug(flowState.getLookupKey(), flowValidationException);
             } catch (Exception e) {
                 // TODO attach exception to output somehow.
                 getLog().error(flowState.getLookupKey(), e);
                 error = flowState.getLookupKey() + " " + e.getMessage() + join(e.getStackTrace(), ", ");
             }
         }
         return error;
     }
 
     public void setDefaultComplete(String defaultComplete) {
         this.defaultComplete = defaultComplete;
     }
 
     public String getDefaultComplete() {
         return defaultComplete;
     }
 
     public void setRenderResultDefault(String renderResultDefault) {
         this.renderResultDefault = renderResultDefault;
     }
 
     public String getRenderResultDefault() {
         return renderResultDefault;
     }
 
     public void setDiscardSessionOnExit(boolean discardSession) {
         this.discardSessionOnExit = discardSession;
     }
 
     public boolean isDiscardSessionOnExit() {
         return discardSessionOnExit;
     }
 
     public void continueFlowState(String flowLookupKey, Map<String, String> propertyChanges) throws FlowRedirectException {
         FlowState flowState = getFlowManagement().continueFlowState(flowLookupKey, true, propertyChanges);
         if (flowState != null) {
             throw new FlowRedirectException(flowState.getCurrentPage(), null);
         }
     }
 
     protected void renderValidationException(FlowValidationException e, String flowType, Writer writer) throws IOException {
         // TODO: we should be looking at render code.
         // but could be bad urls from searchbots
         String m = "Cannot start " + flowType + " :\n" + e.getTrackings();
         getLog().info(m, e);
         writer.append(m + e);
     }
 
     /**
      * @param assumeApiCall the assumeApiCall to set
      */
     public void setAssumeApiCall(boolean assumeApiCall) {
         this.assumeApiCall = assumeApiCall;
     }
 
     /**
      * @return the assumeApiCall
      */
     public boolean isAssumeApiCall() {
         return assumeApiCall;
     }
 
     protected void renderHtml(FlowState flowState) throws FlowNotFoundException, FlowRedirectException {
         String page = flowState.getCurrentPage();
         // page should always be not null - if that's not the case, then
         // check the pageName attribute of flow definitions in the xml files
         if (page == null) {
             throw new FlowNotFoundException("pageName not defined for flow " + flowState.getFlowTypeName());
         }
         throw new FlowRedirectException(page, flowState);
     }
 
     protected JSONWriter getFlowStateWriter() {
         JSONWriter jsonWriter = new JSONWriter();
         jsonWriter.addRenderer(FlowState.class, new FlowStateJsonRenderer());
         return jsonWriter;
     }
 
     public void renderError(Writer writer, String message, String renderResult, FlowState flowState, Exception exception) throws IOException {
         getLog().error("Exception while running flowState=" + flowState, exception);
         if (JSON.equalsIgnoreCase(renderResult) && writer != null) {
             JSONWriter jsonWriter = getFlowStateWriter();
             jsonWriter.object();
             jsonWriter.keyValueIfNotBlankValue(ServicesConstants.ERROR_MESSAGE, message);
             if (flowState != null) {
                 jsonWriter.key(FLOW_STATE_JSON_KEY).value(flowState);
                 // TODO : probably need to check on PropertyRequired.finish
                 Map<String, FlowValidationResult> result = flowState.getFlowValidationResults(FlowActivityPhase.advance, FlowStepDirection.forward);
                 writeValidationResult(jsonWriter, result);
             } else if (exception instanceof FlowValidationException) {
                 FlowValidationException e = (FlowValidationException) exception;
                 Map<String, FlowValidationResult> validationResult = CUtilities.createMap("flow-result", e.getFlowValidationResult());
                 writeValidationResult(jsonWriter, validationResult);
             }
             jsonWriter.endObject();
             writer.append(jsonWriter.toString());
         } else if (exception == null) {
             throw new IOException(message);
         } else {
             throw new IOException(exception);
         }
     }
 
     private void writeValidationResult(JSONWriter jsonWriter, Map<String, FlowValidationResult> result) {
         if (result != null && !result.isEmpty()) {
             jsonWriter.key(ServicesConstants.VALIDATION_ERRORS).value(result);
         }
     }
 
     /**
      * Render a json description of the flow. This includes parameters (name, type, required).
      *
      * @param flowType
      * @param renderResult TODO
      * @return TODO
      * @throws IOException
      */
     public CharSequence describeService(String flowType, String renderResult) throws IOException {
         if (isNotBlank(flowType)) {
             final Flow flow = getFlowManagement().getFlowDefinition(flowType);
             JSONWriter jsonWriter = getFlowStateWriter();
             jsonWriter.object();
             renderFlowParameterJSON(jsonWriter, flow);
             jsonWriter.endObject();
             return jsonWriter.toString();
         } else {
             return "";
         }
     };
 
     /**
      * TODO: Move in separate JsonRenderer when finalized
      */
     public void renderFlowParameterJSON(JSONWriter jsonWriter, Flow flow) {
         jsonWriter.key("flowParameters");
         jsonWriter.array();
         Map<String, FlowPropertyDefinition> propertyDefinitions = flow.getPropertyDefinitions();
         renderFlowPropertyDefinitions(jsonWriter, propertyDefinitions);
         jsonWriter.endArray();
         if (isNotEmpty(flow.getActivities())) {
             jsonWriter.key("activities");
             jsonWriter.array();
             for (FlowActivity flowActivity : flow.getActivities()) {
                 jsonWriter.object();
                 jsonWriter.key("activity");
                 jsonWriter.value(flowActivity.getFlowPropertyProviderName());
                 jsonWriter.key("parameters");
                 jsonWriter.array();
                 renderFlowPropertyDefinitions(jsonWriter, flowActivity.getPropertyDefinitions());
                 jsonWriter.endArray();
                 jsonWriter.endObject();
             }
             jsonWriter.endArray();
         }
     }
 
     /**
      * @param jsonWriter
      * @param propertyDefinitions
      */
     private void renderFlowPropertyDefinitions(JSONWriter jsonWriter, Map<String, FlowPropertyDefinition> propertyDefinitions) {
         for (Map.Entry<String, FlowPropertyDefinition> entry : propertyDefinitions.entrySet()) {
             final FlowPropertyDefinition definition = entry.getValue();
             if (definition.isExportable()) {
                 definition.toJson(jsonWriter);
             }
         }
     }
 
     protected FlowState completeFlowState(String flowType, String flowId, String renderResult, Map<String, String> initial, String complete,
         boolean currentFlow, Writer writer, String advanceToActivity) throws IOException, FlowNotFoundException, FlowRedirectException {
         FlowState flowState = null;
         try {
             flowState = getFlowState(flowType, flowId, renderResult, initial, writer, currentFlow);
             if (flowState != null && completeFlow(flowState, renderResult, complete, writer, advanceToActivity)) {
                 if (JSON.equalsIgnoreCase(renderResult)) {
                     renderJSON(flowState, writer);
                 } else if (HTML.equalsIgnoreCase(renderResult)) {
                     renderHtml(flowState);
                 }
             }
         } catch (FlowNotFoundException e) {
             throw e;
         } catch (FlowRedirectException e) {
             throw e;
         } catch (Exception e) {
             renderError(writer, "Error", renderResult, flowState, e);
             return flowState;
         }
         return flowState;
     }
 
     private void renderJSON(FlowState flowState, Writer writer) throws IOException {
         JSONWriter jsonWriter = getFlowStateWriter();
         jsonWriter.object();
         jsonWriter.keyValueIfNotNullValue(FLOW_STATE_JSON_KEY, flowState);
         jsonWriter.endObject();
         writer.append(jsonWriter.toString());
     }
 
     private boolean completeFlow(FlowState flowState, String renderResult, String complete, Writer writer, String advanceToActivity) throws IOException {
         if (complete == null) {
             // blank means don't try to complete flow.
             complete = defaultComplete;
         }
         if (ADVANCE_TO_END.equalsIgnoreCase(complete)) {
             while (!flowState.isCompleted()) {
                 String error = advanceFlow(flowState);
                 if (error != null) {
                     renderError(writer, "could not advance to the end. activity not completeable " + error, renderResult, flowState, null);
                     // if the flow has not been persisted then we drop the flow
                     // so it is not
                     // cluttering the session.
                     if (!flowState.isPersisted()) {
                         getFlowManagement().dropFlowState(flowState);
                     }
                     return false;
                 }
             }
         } else if (AS_FAR_AS_POSSIBLE.equalsIgnoreCase(complete)) {
             String success = null;
             while (success == null && !flowState.isCompleted() && !flowState.isFinishable()
                 && !flowState.getCurrentActivityByName().equals(advanceToActivity)) {
                 success = advanceFlow(flowState);
             }
         }
         return true;
     }
 
 }
