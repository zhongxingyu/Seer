 /**********************************************************************************
  * $URL: https://source.sakaiproject.org/contrib/assignment2/trunk/tool/src/java/org/sakaiproject/assignment2/tool/params/Assignment2ViewParamsInterceptor.java $
  * $Id: Assignment2ViewParamsInterceptor.java 53899 2008-10-13 15:07:58Z swgithen@mtu.edu $
  ***********************************************************************************
  *
  * Copyright (c) 2007, 2008 The Sakai Foundation.
  *
  * Licensed under the Educational Community License, Version 1.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.opensource.org/licenses/ecl1.php
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *
  **********************************************************************************/
 
 package org.sakaiproject.assignment2.tool;
 
 import org.sakaiproject.assignment2.tool.params.AssignmentListSortViewParams;
 import org.sakaiproject.assignment2.tool.params.AssignmentViewParams;
 import org.sakaiproject.assignment2.tool.params.SimpleAssignmentViewParams;
 import org.sakaiproject.assignment2.tool.params.StudentSubmissionParams;
 import org.sakaiproject.assignment2.tool.params.VerifiableViewParams;
 import org.sakaiproject.assignment2.tool.params.ViewSubmissionsViewParams;
 import org.sakaiproject.assignment2.tool.producers.AssignmentDetailProducer;
 import org.sakaiproject.assignment2.tool.producers.AssignmentProducer;
 import org.sakaiproject.assignment2.tool.producers.ListProducer;
 import org.sakaiproject.assignment2.tool.producers.AuthorizationFailedProducer;
 import org.sakaiproject.assignment2.tool.producers.PreviewAsStudentProducer;
 import org.sakaiproject.assignment2.tool.producers.RedirectToAssignmentProducer;
 import org.sakaiproject.assignment2.tool.producers.StudentAssignmentListProducer;
 import org.sakaiproject.assignment2.tool.producers.StudentSubmitProducer;
 import org.sakaiproject.assignment2.tool.producers.ViewSubmissionsProducer;
 
 import uk.org.ponder.rsf.flow.ARIResult;
 import uk.org.ponder.rsf.flow.ActionResultInterceptor;
 import uk.org.ponder.rsf.viewstate.AnyViewParameters;
 import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
 import uk.org.ponder.rsf.viewstate.ViewParameters;
 import uk.org.ponder.rsf.viewstate.ViewParamsInterceptor;
 
 /**
  * The primary HTTP GET interceptor for Assignments2, that checks URL's for 
  * permissions and other logic, and then changes where they should be going or
  * their parameters if necessary.
  * 
  * @author rjlowe
  * @author sgithens
  *
  */
 public class Assignment2WorkFlowLogic implements ViewParamsInterceptor, ActionResultInterceptor {
 
     private LocalPermissionLogic localPermissionLogic;
     public void setLocalPermissionLogic(LocalPermissionLogic localPermissionLogic){
         this.localPermissionLogic = localPermissionLogic;
     }
 
 
     public AnyViewParameters adjustViewParameters(ViewParameters incoming) {
         if (AuthorizationFailedProducer.VIEWID.equals(incoming.viewID)) {
             //Always return incoming if we are going to the Authorization Failed Page
             return incoming;
         }
 
         if (AssignmentDetailProducer.VIEW_ID.equals(incoming.viewID)) {
             //This is a entitybroker "helper" that is always visible
             //TODO make sure that this is always visible
             return incoming;
         }
 
         //Verify View Params for completeness
         if (incoming instanceof VerifiableViewParams) {
             if(!((VerifiableViewParams)incoming).verify()){
                 return new AssignmentListSortViewParams(ListProducer.VIEW_ID);
             }
         }
 
         // depending on the user's perms in the site, will redirect them to either
         // the "View Submissions" page or Student Summary/Submit page from this generic link
         if (RedirectToAssignmentProducer.VIEWID.equals(incoming.viewID)) {
             if (incoming instanceof SimpleAssignmentViewParams) {
                 SimpleAssignmentViewParams params = (SimpleAssignmentViewParams) incoming;
                 if (localPermissionLogic.checkCurrentUserHasViewPermission(new ViewSubmissionsViewParams(ViewSubmissionsProducer.VIEW_ID, params.assignmentId))) {
                     return new ViewSubmissionsViewParams(ViewSubmissionsProducer.VIEW_ID, params.assignmentId);
                 } else if (localPermissionLogic.checkCurrentUserHasViewPermission(new SimpleAssignmentViewParams(StudentSubmitProducer.VIEW_ID, params.assignmentId))) {
                     return new SimpleAssignmentViewParams(StudentSubmitProducer.VIEW_ID, params.assignmentId);
                 }
             }
         }
 
        if (localPermissionLogic.checkCurrentUserHasViewPermission(incoming)){
            return incoming;
        }
        else if (localPermissionLogic.checkCurrentUserHasViewPermission(new AssignmentListSortViewParams(StudentAssignmentListProducer.VIEW_ID))) {
             return new AssignmentListSortViewParams(StudentAssignmentListProducer.VIEW_ID);
         }
 
         return new SimpleViewParameters(AuthorizationFailedProducer.VIEWID);
     }
 
     private void interceptWorkFlowResult(ARIResult result, ViewParameters incoming, WorkFlowResult actionReturn) {
         Long assignmentId = null;
         if (incoming instanceof StudentSubmissionParams) {
             assignmentId = ((StudentSubmissionParams) incoming).assignmentId;
         }
         else if (incoming instanceof SimpleAssignmentViewParams) {
             assignmentId = ((SimpleAssignmentViewParams) incoming).assignmentId;
         }
         else if (incoming instanceof AssignmentViewParams) {
             assignmentId = ((AssignmentViewParams) incoming).assignmentId;
         }
         
         switch (actionReturn) {
         case INSTRUCTOR_CANCEL_ASSIGNMENT:
             result.resultingView = new SimpleViewParameters(ListProducer.VIEW_ID);
             result.propagateBeans = ARIResult.FLOW_END;
             break;
         case INSTRUCTOR_POST_ASSIGNMENT:
             result.resultingView = new SimpleViewParameters(ListProducer.VIEW_ID);
             result.propagateBeans = ARIResult.FLOW_END;
             break;
         case INSTRUCTOR_PREVIEW_ASSIGNMENT:
             result.resultingView = new SimpleViewParameters(PreviewAsStudentProducer.VIEW_ID);
             result.propagateBeans = ARIResult.FLOW_FASTSTART;
             break;
         case INSTRUCTOR_SAVE_DRAFT_ASSIGNMENT:
             result.resultingView = new SimpleViewParameters(ListProducer.VIEW_ID);
             result.propagateBeans = ARIResult.FLOW_END;
             break;
         case INSTRUCTOR_CONTINUE_EDITING_ASSIGNMENT:
             result.resultingView = new SimpleViewParameters(AssignmentProducer.VIEW_ID);
             result.propagateBeans = ARIResult.PROPAGATE;
             break;
         case STUDENT_CONTINUE_EDITING_SUBMISSION:
             result.resultingView = new StudentSubmissionParams(StudentSubmitProducer.VIEW_ID, assignmentId, false);
             result.propagateBeans = ARIResult.PROPAGATE;
             break;
         case STUDENT_PREVIEW_SUBMISSION:
             result.resultingView = new StudentSubmissionParams(StudentSubmitProducer.VIEW_ID, assignmentId, true);
             result.propagateBeans = ARIResult.FLOW_FASTSTART;
             break;
         case STUDENT_SAVE_DRAFT_SUBMISSION:
             result.resultingView = new SimpleViewParameters(StudentAssignmentListProducer.VIEW_ID);
             break;
         case STUDENT_SUBMISSION_FAILURE:
             break;
         case STUDENT_SUBMIT_SUBMISSION:
             result.resultingView = new SimpleViewParameters(StudentAssignmentListProducer.VIEW_ID);
             break;
         default:
             break;
         }
     }
     
     public void interceptActionResult(ARIResult result, ViewParameters incoming, Object actionReturn) {
         if (actionReturn instanceof WorkFlowResult) {
             interceptWorkFlowResult(result, incoming, (WorkFlowResult) actionReturn);
         }
     }
 
 }
