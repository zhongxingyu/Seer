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
 package org.amplafi.flow.validation;
 
 import java.net.URI;
 import java.util.List;
 
 import org.amplafi.flow.FlowActivity;
 import org.amplafi.flow.FlowException;
 import org.amplafi.flow.FlowState;
 
 import org.apache.commons.lang.ObjectUtils;
 import org.apache.commons.lang.StringUtils;
 
 import com.sworddance.util.NotNullIterator;
 
 
 /**
  * Exception thrown when there is a problem detected with the data already in the {@link org.amplafi.flow.FlowState},
  * or being passed into the flow to be saved. For example, trying to save a person's name in a property
  * expecting a number.
  *
  */
 public class FlowValidationException extends FlowException {
 
     private final FlowValidationResult flowValidationResult;
 
     public FlowValidationException(FlowState flowState, FlowValidationResult flowValidationResult) {
      	super(flowState);
         this.flowValidationResult = flowValidationResult;
     }
 
     public FlowValidationException(FlowState flowState, FlowValidationTracking...flowValidationTrackings) {
         super(flowState, "Validation Problem");
         this.flowValidationResult = new ReportAllValidationResult(flowValidationTrackings);
     }
     public FlowValidationException(FlowState flowState, Throwable cause, FlowValidationTracking...flowValidationTrackings) {
         this(flowState, flowValidationTrackings);
         initCause(cause);
     }
 
     public FlowValidationException(FlowState flowState, String key, FlowValidationTracking...flowValidationTrackings) {
         this(flowState, flowValidationTrackings);
         this.flowValidationResult.addTracking(new SimpleValidationTracking(key));
     }
 
     /**
      * @param currentActivity
      * @param flowValidationResult
      */
     public FlowValidationException(FlowState flowState, FlowActivity currentActivity, FlowValidationResult flowValidationResult) {
         super(flowState, currentActivity.getFlowPropertyProviderFullName());
         this.flowValidationResult = flowValidationResult;
     }
 
     public List<FlowValidationTracking> getTrackings() {
         return this.flowValidationResult.getTrackings();
     }
     @Override
     public synchronized FlowValidationException initCause(Throwable cause) {
         super.initCause(cause);
         setStackTrace(cause.getStackTrace());
         return this;
     }
 
     public URI getRedirectUri() {
     	URI redirectUri = null;
     	List<FlowValidationTracking> trackings = getFlowValidationResult().getTrackings();
     	for (FlowValidationTracking flowValidationTracking : NotNullIterator.<FlowValidationTracking>newNotNullIterator(trackings)) {
     		redirectUri = flowValidationTracking.getRedirectUri();
     		if (redirectUri != null) {
     			break;
     		}
     	}
 		return redirectUri;
     }
 
     /**
      * @return the result causing the problem
      */
     public FlowValidationResult getFlowValidationResult() {
         return this.flowValidationResult;
     }
     @Override
     public String toString() {
         return this.getMessage()+" : "+ this.flowValidationResult
             // CHECK Is the toString() used to print on the screen and that is why the stack trace is not visible?
             + StringUtils.join(super.getStackTrace(), "\n");
     }
 
     /**
      * @param flowState
      * @param flowValidationResult
      * @throws FlowValidationException thrown if flowValidationResult != null && !flowValidationResult.isValid()
      */
     public static void valid(FlowState flowState, FlowValidationResult flowValidationResult) throws FlowValidationException {
         if ( flowValidationResult != null && !flowValidationResult.isValid()) {
             throw new FlowValidationException(flowState, flowValidationResult);
         }
     }
     public static void valid(FlowState flowState, boolean condition, Object property, Object...messages) throws FlowValidationException {
         if ( !condition) {
             String[] actual = new String[] {ObjectUtils.toString(property, ""), StringUtils.join(messages)};
             FlowValidationResult flowValidationResult = new ReportAllValidationResult(new MissingRequiredTracking(actual));
             throw new FlowValidationException(flowState, flowValidationResult);
         }
     }
     
    public static void fail(FlowState flowState, String key, String message) {
        FlowValidationResult flowValidationResult = new ReportAllValidationResult(new SimpleValidationTracking(key, message));
         throw new FlowValidationException(flowState, flowValidationResult);
     }
     
     public static void notNull(Object notNull, Object property, Object...messages) {
         valid(null, notNull != null, property, messages);
     }
 }
