 /**
  * Copyright 2006-2008 by Amplafi. All rights reserved.
  * Confidential.
  */
 package org.amplafi.flow.flowproperty;
 
 import org.amplafi.flow.FlowActivity;
 import org.amplafi.flow.FlowPropertyDefinition;
 import org.amplafi.flow.FlowPropertyValueProvider;
 import org.amplafi.flow.FlowState;
 
 import static org.amplafi.flow.FlowConstants.*;
 import static org.apache.commons.lang.StringUtils.*;
 /**
  * @author patmoore
  *
  */
 public class CancelTextFlowPropertyValueProvider implements FlowPropertyValueProvider<FlowActivity> {
     public static final CancelTextFlowPropertyValueProvider INSTANCE = new CancelTextFlowPropertyValueProvider();
     /**
      * @see org.amplafi.flow.FlowPropertyValueProvider#get(org.amplafi.flow.FlowActivity, org.amplafi.flow.FlowPropertyDefinition)
      */
     @Override
     public <T> T get(FlowActivity flowActivity, FlowPropertyDefinition flowPropertyDefinition) {
         String label = "message:flow.label-cancel";
         String lookupKey =flowActivity.getProperty(FSRETURN_TO_FLOW);
         if ( lookupKey != null ) {
             FlowState flowState = flowActivity.getFlowManagement().getFlowState(lookupKey);
             if ( flowState != null) {
                label = flowState.getCurrentActivity().getProperty(FSRETURN_TO_TEXT);
                 if (isBlank(label)) {
                     // TODO -- how to internationalize?
                     label = "Return to "+flowState.getFlowTitle();
                 }
             }
         }
         return (T) label;
     }
 
 }
