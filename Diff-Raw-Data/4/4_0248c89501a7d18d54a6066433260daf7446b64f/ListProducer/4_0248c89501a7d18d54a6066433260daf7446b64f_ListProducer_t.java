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
 import java.util.List;
 import java.util.Locale;
 
 import org.sakaiproject.assignment2.logic.AssignmentLogic;
 import org.sakaiproject.assignment2.logic.AssignmentPermissionLogic;
 import org.sakaiproject.assignment2.logic.AssignmentSubmissionLogic;
 import org.sakaiproject.assignment2.logic.ExternalLogic;
 import org.sakaiproject.assignment2.model.Assignment2;
 import org.sakaiproject.assignment2.model.AssignmentSubmission;
 import org.sakaiproject.assignment2.taggable.api.AssignmentActivityProducer;
 import org.sakaiproject.assignment2.tool.beans.Assignment2Bean;
 import org.sakaiproject.assignment2.tool.beans.locallogic.DecoratedTaggingProvider;
 import org.sakaiproject.assignment2.tool.params.AssignmentViewParams;
 import org.sakaiproject.assignment2.tool.params.ViewSubmissionsViewParams;
 import org.sakaiproject.component.cover.ComponentManager;
 import org.sakaiproject.taggable.api.TaggingManager;
 import org.sakaiproject.taggable.api.TaggingProvider;
 
 import uk.org.ponder.messageutil.MessageLocator;
 import uk.org.ponder.rsf.components.UIBranchContainer;
 import uk.org.ponder.rsf.components.UICommand;
 import uk.org.ponder.rsf.components.UIContainer;
 import uk.org.ponder.rsf.components.UIForm;
 import uk.org.ponder.rsf.components.UIInput;
 import uk.org.ponder.rsf.components.UIInternalLink;
 import uk.org.ponder.rsf.components.UIMessage;
 import uk.org.ponder.rsf.components.UIOutput;
 import uk.org.ponder.rsf.components.decorators.DecoratorList;
 import uk.org.ponder.rsf.components.decorators.UIStyleDecorator;
 import uk.org.ponder.rsf.flow.jsfnav.NavigationCase;
 import uk.org.ponder.rsf.flow.jsfnav.NavigationCaseReporter;
 import uk.org.ponder.rsf.view.ComponentChecker;
 import uk.org.ponder.rsf.view.DefaultView;
 import uk.org.ponder.rsf.view.ViewComponentProducer;
 import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
 import uk.org.ponder.rsf.viewstate.ViewParameters;
 
 public class ListProducer implements ViewComponentProducer, NavigationCaseReporter, DefaultView {
 
     public static final String VIEW_ID = "list";
     
     public String getViewID() {
         return VIEW_ID;
     }
 
     private MessageLocator messageLocator;
     private AssignmentLogic assignmentLogic;
     private AssignmentSubmissionLogic submissionLogic;
     private ExternalLogic externalLogic;
     private AssignmentPermissionLogic permissionLogic;
     private Locale locale;
     
     public void fillComponents(UIContainer tofill, ViewParameters viewparams, ComponentChecker checker) {
       
     	DateFormat df = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, locale);
         //Edit Permission
         Boolean edit_perm = permissionLogic.isCurrentUserAbleToEditAssignments(externalLogic.getCurrentContextId());
         
     	List<Assignment2> entries = assignmentLogic.getViewableAssignments();
     	
     	//Breadcrumbs
     	UIMessage.make(tofill, "last_breadcrumb", "assignment2.list.heading");
     	
     	//Links to settings and reorder
     	UIInternalLink.make(tofill, "settings_link", new SimpleViewParameters(ListProducer.VIEW_ID));
     	UIInternalLink.make(tofill, "reorder_link", new SimpleViewParameters(ListReorderProducer.VIEW_ID));
     	
         UIMessage.make(tofill, "page-title", "assignment2.list.title");
         
         //Links
         if (edit_perm){
         	UIInternalLink.make(tofill, "add_assignment", UIMessage.make("assignment2.list.add_assignment"),
         		new SimpleViewParameters(AssignmentProducer.VIEW_ID));
         }
                                       
         if (entries.size() <= 0) {
             UIMessage.make(tofill, "assignment_empty", "assignment2.list.assignment_empty");
             return;
         }
         
         //Fill out Table
         for (Assignment2 assignment : entries){
         	UIBranchContainer row = UIBranchContainer.make(tofill, "assignment-row:");
         	UIOutput title = UIOutput.make(row, "assignment_title", (assignment != null) ? assignment.getTitle() : "");
         	
         	//If Current User has the ability to edit or duplicate the assignment
         	if (edit_perm) {
                 UIForm form = UIForm.make(row, "form");
         		UIInput.make(form, "current_assignment", "Assignment2Bean.currentAssignmentId", assignment.getId().toString());
         		UICommand.make(form, "assignment_delete", "", "#{Assignment2Bean.processActionRemoteCurrent}");
 	        	UIInternalLink.make(form, "assignment_edit",  UIMessage.make("assignment2.list.edit"), 
 	        			new AssignmentViewParams(AssignmentProducer.VIEW_ID, assignment.getId()));
         	}
         	
         	// Tag provider stuff
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
         				//	helper.getPlacement() + "/" + helper.getHelperId() + 
         				//	".helper?1=1";
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
 
         	// Submitted/Total display
         	int total = 0;
         	int withSubmission = 0;
         	List<String> viewableStudents = permissionLogic.getViewableStudentsForUserForItem(assignment);
         	if (viewableStudents != null) {
         		total = viewableStudents.size();
         		if (total > 0) {
         			withSubmission = submissionLogic.getNumStudentsWithASubmission(assignment, viewableStudents);
         		}
         	}
 
         	UIInternalLink.make(row, "grade", 
         			messageLocator.getMessage("assignment2.list.submissions_link", new Object[]{ withSubmission, total}), 
         			new ViewSubmissionsViewParams(ViewSubmissionsProducer.VIEW_ID, assignment.getId()));
         	
         	
         	// group restrictions
         	if (assignment.getAssignmentGroupSet() != null && !assignment.getAssignmentGroupSet().isEmpty()) {
         		title.decorators = new DecoratorList(new UIStyleDecorator("group"));
         	}
         	
         	if (assignment.isDraft()){
         		UIMessage.make(row, "draft", "assignment2.list.draft");
         	}
         	
         	UIOutput divLeftContainer = UIOutput.make(row, "div-left-container");
         	//find active
         	if (assignment.isOpen())
         	{
         		//show active styleclass
         		divLeftContainer.decorators = new DecoratorList(new UIStyleDecorator("assignActive"));
         		
         	}else{
         		//show inactive styleclass
         		divLeftContainer.decorators = new DecoratorList(new UIStyleDecorator("assignInactive"));
         	}
         	
 	    	UIOutput.make(row, "assignment_row_open", df.format(assignment.getOpenTime()));
 	
 	    	if (assignment.getDueDate() != null) {
 	    		UIOutput.make(row, "assignment_row_due", df.format(assignment.getDueDate()));
 	    	} else {
 	    		UIMessage.make(row, "assignment_row_due", "assignment2.list.no_due_date");	
 	    	}
         }
         
     }
 	public List reportNavigationCases()
 	{
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
     
     public void setMessageLocator(MessageLocator messageLocator) {
         this.messageLocator = messageLocator;
     }
       
     public void setAssignmentLogic (AssignmentLogic assignmentLogic) {
     	this.assignmentLogic = assignmentLogic;
     }
     
     public void setExternalLogic(ExternalLogic externalLogic) {
     	this.externalLogic = externalLogic;
     }
     
 	public void setPermissionLogic(AssignmentPermissionLogic permissionLogic) {
 		this.permissionLogic = permissionLogic;
 	}
 	public Locale getLocale()
 	{
 		return locale;
 	}
 	public void setLocale(Locale locale)
 	{
 		this.locale = locale;
 	}
 	
 	public void setAssignmentSubmissionLogic(AssignmentSubmissionLogic submissionLogic) {
 		this.submissionLogic = submissionLogic;
 	}
 }
