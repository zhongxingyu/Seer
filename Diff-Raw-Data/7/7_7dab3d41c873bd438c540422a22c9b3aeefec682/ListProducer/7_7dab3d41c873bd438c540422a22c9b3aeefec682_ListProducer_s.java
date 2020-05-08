 /**********************************************************************************
  * $URL$
  * $Id$
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
 
 package org.sakaiproject.assignment2.tool.producers;
 
 import java.text.DateFormat;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 
 import org.sakaiproject.assignment2.logic.AssignmentLogic;
 import org.sakaiproject.assignment2.logic.AssignmentPermissionLogic;
 import org.sakaiproject.assignment2.logic.AssignmentSubmissionLogic;
 import org.sakaiproject.assignment2.logic.ExternalLogic;
 import org.sakaiproject.assignment2.model.Assignment2;
 import org.sakaiproject.assignment2.tool.DecoratedTaggingProvider;
 import org.sakaiproject.assignment2.tool.params.AssignmentViewParams;
 import org.sakaiproject.assignment2.tool.params.RemoveAssignmentParams;
 import org.sakaiproject.assignment2.tool.params.ViewSubmissionsViewParams;
 import org.sakaiproject.component.cover.ComponentManager;
 import org.sakaiproject.taggable.api.TaggingManager;
 import org.sakaiproject.taggable.api.TaggingProvider;
 
 import uk.org.ponder.messageutil.MessageLocator;
 import uk.org.ponder.rsf.components.UIBranchContainer;
 import uk.org.ponder.rsf.components.UIContainer;
 import uk.org.ponder.rsf.components.UIInternalLink;
 import uk.org.ponder.rsf.components.UIMessage;
 import uk.org.ponder.rsf.components.UIOutput;
 import uk.org.ponder.rsf.components.decorators.DecoratorList;
 import uk.org.ponder.rsf.components.decorators.UIFreeAttributeDecorator;
 import uk.org.ponder.rsf.components.decorators.UIStyleDecorator;
 import uk.org.ponder.rsf.flow.jsfnav.NavigationCase;
 import uk.org.ponder.rsf.flow.jsfnav.NavigationCaseReporter;
 import uk.org.ponder.rsf.view.ComponentChecker;
 import uk.org.ponder.rsf.view.DefaultView;
 import uk.org.ponder.rsf.view.ViewComponentProducer;
 import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
 import uk.org.ponder.rsf.viewstate.ViewParameters;
 
 /**
  * This renders the Instructor Landing Page that shows the list of assignments
  * in the course. Along with the ability to drag'n'drop reorder, delete, edit,
  * and go to the submissions.
  * 
  * @author rjlowe
  * @author sgithens
  *
  */
 public class ListProducer implements ViewComponentProducer, NavigationCaseReporter, DefaultView {
 
     public static final String VIEW_ID = "list";
 
     public String getViewID() {
         return VIEW_ID;
     }
 
     private MessageLocator messageLocator;
     public void setMessageLocator(MessageLocator messageLocator) {
         this.messageLocator = messageLocator;
     }
     
     private AssignmentLogic assignmentLogic;
     public void setAssignmentLogic(AssignmentLogic assignmentLogic) {
         this.assignmentLogic = assignmentLogic;
     }
     
     private AssignmentSubmissionLogic submissionLogic;
     public void setSubmissionLogic(AssignmentSubmissionLogic submissionLogic) {
         this.submissionLogic = submissionLogic;
     }
     
     private ExternalLogic externalLogic;
     public void setExternalLogic(ExternalLogic externalLogic) {
         this.externalLogic = externalLogic;
     }
     
     private AssignmentPermissionLogic permissionLogic;
     public void setPermissionLogic(AssignmentPermissionLogic permissionLogic) {
         this.permissionLogic = permissionLogic;
     }
     
     private Locale locale;
     public void setLocale(Locale locale) {
         this.locale = locale;
     }
 
     public void fillComponents(UIContainer tofill, ViewParameters viewparams, ComponentChecker checker) {
 
         DateFormat df = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, locale);
         //Edit Permission
         Boolean edit_perm = permissionLogic.isCurrentUserAbleToEditAssignments(externalLogic.getCurrentContextId());
         String currUserId = externalLogic.getCurrentUserId();
 
         List<Assignment2> entries = assignmentLogic.getViewableAssignments();
 
         renderPageTop(tofill, edit_perm);
 
         // Add/Edit Links
         //if (edit_perm){
         //    UIInternalLink.make(tofill, "add_assignment", UIMessage.make("assignment2.list.add_assignment"),
         //            new SimpleViewParameters(AssignmentProducer.VIEW_ID));
         //}
         
         // Only show the submissions/total header if there are actual assignments
         if (entries.size() > 0) {
             UIMessage.make(tofill, "submissions_total", "assignment2.list.submissions_total" );
         }
         else {
             if (edit_perm) {
                 UIMessage.make(tofill, "no-assignments-message", "assignment2.list.assignment_empty.editable");
             }
             else {
                 UIMessage.make(tofill, "no-assignments-message", "assignment2.list.assignment_empty");
             }
         }
         
         /////////// New Table Version //
         if (entries.size() > 0) {
             UIOutput.make(tofill, "assignmentTable");
             if (edit_perm) {
                 UIInternalLink.make(tofill, "add_assignment", UIMessage.make("assignment2.list.add_assignment"),
                         new SimpleViewParameters(AssignmentProducer.VIEW_ID));
             }
         }
         
         for (Assignment2 assignment : entries) {
             UIBranchContainer row = UIBranchContainer.make(tofill, "asnn-row:");
             
             UIOutput infoCell = UIOutput.make(row, "assignmentInfoCell");
             if (assignment.isOpen())
             {
                 //show active styleclass
                 infoCell.decorators = new DecoratorList(new UIStyleDecorator("assignmentActive"));
 
             } else {
                 //show inactive styleclass
                 infoCell.decorators = new DecoratorList(new UIStyleDecorator("assignmentInactive"));
             }
             UIOutput title = UIOutput.make(row, "assignment_title", (assignment != null) ? assignment.getTitle() : "");
             
             renderSubmissionStatusForAssignment(currUserId, assignment, row);
             
             renderDueDateOnRow(df, assignment, row);
             
             if (edit_perm) {
                 UIOutput.make(row, "delete-asnn-link").decorate(
                         new UIFreeAttributeDecorator("onclick",
                         "asnn2.removeAsnnDialog("+assignment.getId()+",jQuery(this).parents('tr.movable:first').get(0)); return false;"));
                 UIInternalLink.make(row, "assignment_edit",  UIMessage.make("assignment2.list.edit"), 
                         new AssignmentViewParams(AssignmentProducer.VIEW_ID, assignment.getId()));
             }
         }
         /////////// End New Table Version //
      
 
         for (Assignment2 assignment : entries) {
             UIBranchContainer row = UIBranchContainer.make(tofill, "assignment-row:");
             row.decorators = new DecoratorList(new UIStyleDecorator("sortable_" + assignment.getId().toString()));
 
             UIOutput title = UIOutput.make(row, "assignment_title", (assignment != null) ? assignment.getTitle() : "");
 
             //If Current User has the ability to edit or duplicate the assignment
             if (edit_perm) {
                 
                 //UIInternalLink.make(row, "delete-asnn-link", new RemoveAssignmentParams(RemoveAssignmentConfirmProducer.VIEW_ID, assignment.getId()));
                 UIOutput.make(row, "delete-asnn-link").decorate(
                         new UIFreeAttributeDecorator("onclick",
                         "asnn2.removeAsnnDialog("+assignment.getId()+",jQuery(this).parents('li.row:first').get(0)); return false;"));
                 UIInternalLink.make(row, "assignment_edit",  UIMessage.make("assignment2.list.edit"), 
                         new AssignmentViewParams(AssignmentProducer.VIEW_ID, assignment.getId()));
                 
             }
 
             // Tag provider removed for now ASNN-113
             // renderMatrixTagging();
 
             renderSubmissionStatusForAssignment(currUserId, assignment, row);
 
 
             // group restrictions
             if (assignment.getAssignmentGroupSet() != null && !assignment.getAssignmentGroupSet().isEmpty()) {
                 title.decorators = new DecoratorList(new UIStyleDecorator("group"));
             }
 
             if (assignment.isDraft()){
                 UIMessage.make(row, "draft", "assignment2.list.draft");
             }
 
             // Renders a Div + classes for assignment status
             renderLeftContainer(assignment, row);
 
             renderDueDateOnRow(df, assignment, row);
         }
 
         // The Javascript for Delete and Sorting Ajax
         if (edit_perm) {
             UIOutput.make(tofill, "edit-setup-javascript");
         }
         
     }
 
     /**
      * Renders the Div for the assignment title, draft, and open status.
      * 
      * @param assignment
      * @param row
      */
     private void renderLeftContainer(Assignment2 assignment,
             UIBranchContainer row) {
         //TODO FIXME Remove after list version is gone
         UIOutput divLeftContainer = UIOutput.make(row, "div-left-container");
         //find active
         if (assignment.isOpen())
         {
             //show active styleclass
             divLeftContainer.decorators = new DecoratorList(new UIStyleDecorator("assignActive"));
 
         } else {
             //show inactive styleclass
             divLeftContainer.decorators = new DecoratorList(new UIStyleDecorator("assignInactive"));
         }
     }
 
     /**
      * Renders the bit of the Assignment Row that says like 2 / 83 if two of the
      * students submitted the assignment, or N/A if the Assignment does not
      * require submissions.
      * 
      * @param currUserId
      * @param assignment
      * @param row
      */
     private void renderSubmissionStatusForAssignment(String currUserId,
             Assignment2 assignment, UIBranchContainer row) {
         if (assignment.isRequiresSubmission()) {
             // Submitted/Total display
             int total = 0;
             int withSubmission = 0;
 
             List<String> viewableStudents = permissionLogic.getViewableStudentsForUserForItem(currUserId, assignment);
             if (viewableStudents != null) {
                 total = viewableStudents.size();
                 if (total > 0) {
                     withSubmission = submissionLogic.getNumStudentsWithASubmission(assignment, viewableStudents);
                 }
             }
 
             UIInternalLink.make(row, "grade", 
                     messageLocator.getMessage("assignment2.list.submissions_link", new Object[]{ withSubmission, total}), 
                     new ViewSubmissionsViewParams(ViewSubmissionsProducer.VIEW_ID, assignment.getId()));
         } else {
             UIOutput.make(row, "no_submission_req", messageLocator.getMessage("assignment2.list.no_sub_required"));
         }
     }
 
     /**
      * Render the Due Date that appears under the Assignment Title in the List.
      * 
      * @param df
      * @param assignment
      * @param row
      */
     private void renderDueDateOnRow(DateFormat df, Assignment2 assignment,
             UIBranchContainer row) {
         if (assignment.getDueDate() != null) {
             UIOutput.make(row, "assignment_row_due", df.format(assignment.getDueDate()));
         } else {
             UIMessage.make(row, "assignment_row_due", "assignment2.list.no_due_date");	
         }
     }
 
     /**
      * Renders the Breadcrumbs, title, and other stuff at the top of the page.
      * 
      * @param tofill
      * @param edit_perm
      */
     private void renderPageTop(UIContainer tofill, Boolean edit_perm) {
         //Breadcrumbs
         UIMessage.make(tofill, "last_breadcrumb", "assignment2.list.heading");
 
         //Links to settings and reorder
         // Settings page not yet implemented. ASNN-207
         //UIInternalLink.make(tofill, "settings_link", new SimpleViewParameters(SettingsProducer.VIEW_ID));
        if (edit_perm) {
             UIInternalLink.make(tofill, "reorder_link", new SimpleViewParameters(ListReorderProducer.VIEW_ID));
        }
 
     }
     
     public List reportNavigationCases() {
         List<NavigationCase> nav= new ArrayList<NavigationCase>();
         nav.add(new NavigationCase("remove", new SimpleViewParameters(AjaxResultsProducer.VIEW_ID)));
         return nav;
     }
 
     private List<DecoratedTaggingProvider> initDecoratedProviders() {
         TaggingManager taggingManager = (TaggingManager) ComponentManager
         .get("org.sakaiproject.taggable.api.TaggingManager");
         List<DecoratedTaggingProvider> providers = new ArrayList<DecoratedTaggingProvider>();
         for (TaggingProvider provider : taggingManager.getProviders())
         {
             providers.add(new DecoratedTaggingProvider(provider));
         }
         return providers;
     }
 
     /**
      * Functionality for matrix tagging. Currently on Hold. ASNN-113
      */
     private void renderMatrixTagging() {
         /*** Removing support for Assignments2 and matrix linking for now
         TaggingManager taggingManager = (TaggingManager) ComponentManager.get("org.sakaiproject.taggable.api.TaggingManager");
         if (taggingManager.isTaggable() && assignment != null){
                 //TODO: optimize?
                 List<DecoratedTaggingProvider> providers = initDecoratedProviders();
 
                 AssignmentActivityProducer assignmentActivityProducer = (AssignmentActivityProducer) ComponentManager
                 .get("org.sakaiproject.assignment2.taggable.api.AssignmentActivityProducer");
 
                 for (DecoratedTaggingProvider provider : providers){
                         UIBranchContainer tagLinks = UIBranchContainer.make(row, "tag_provider_links:");
                         String ref = assignmentActivityProducer.getActivity(
                                                 assignment).getReference();
                         TaggingHelperInfo helper = provider.getProvider().getActivityHelperInfo(ref);
                         if (helper != null){
                                 //String url = ServerConfigurationService.getToolUrl() + "/" + 
                                 //      helper.getPlacement() + "/" + helper.getHelperId() + 
                                 //      ".helper?1=1";
                                 String url = "/?1=1";
                                 for (String key : helper.getParameterMap().keySet()) {
                                         url = url + "&" + key + "=" + helper.getParameterMap().get(key);
                                 }
 
                                 //UILink.make(tagLinks, "assignment_view_links", helper.getName(), url);                                        
 
                                  //This is commented out until RSF has some better helper support
                                 UIInternalLink.make(tagLinks, "assignment_view_links", helper.getName(),
                                         new TaggableHelperViewParams(TaggableHelperProducer.VIEWID, 
                                                         helper.getHelperId(), 
                                                         helper.getParameterMap().keySet().toArray(new String[0]), 
                                                         helper.getParameterMap().values().toArray(new String[0])));
                         }
                 }
         }
      */
     }
     
 }
