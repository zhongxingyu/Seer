 package org.sakaiproject.assignment2.tool.producers;
 
 import java.text.DateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.Locale;
 
 import org.sakaiproject.assignment2.exception.ConflictingAssignmentNameException;
 import org.sakaiproject.assignment2.logic.AssignmentLogic;
 import org.sakaiproject.assignment2.logic.ExternalLogic;
 import org.sakaiproject.assignment2.model.Assignment2;
 import org.sakaiproject.assignment2.tool.beans.Assignment2Bean;
 import org.sakaiproject.assignment2.tool.beans.Assignment2Creator;
 import org.sakaiproject.assignment2.tool.beans.PagerBean;
 import org.sakaiproject.assignment2.tool.params.AssignmentListSortViewParams;
 import org.sakaiproject.assignment2.tool.params.SimpleAssignmentViewParams;
 import org.sakaiproject.assignment2.tool.params.AssignmentAddViewParams;
 import org.sakaiproject.assignment2.tool.producers.AssignmentAddProducer;
 import org.sakaiproject.assignment2.tool.producers.AssignmentGradeAssignmentProducer;
 import org.sakaiproject.assignment2.tool.producers.AssignmentListReorderProducer;
 import org.sakaiproject.assignment2.tool.producers.NavBarRenderer;
 import org.sakaiproject.assignment2.tool.producers.PagerRenderer;
 import org.sakaiproject.assignment2.tool.producers.SortHeaderRenderer;
 
 
 import uk.org.ponder.messageutil.MessageLocator;
 import uk.org.ponder.messageutil.TargettedMessage;
 import uk.org.ponder.messageutil.TargettedMessageList;
 import uk.org.ponder.rsf.components.*;
 import uk.org.ponder.rsf.components.decorators.DecoratorList;
 import uk.org.ponder.rsf.components.decorators.UILabelTargetDecorator;
 import uk.org.ponder.rsf.evolvers.TextInputEvolver;
 import uk.org.ponder.rsf.flow.ARIResult;
 import uk.org.ponder.rsf.flow.ActionResultInterceptor;
 import uk.org.ponder.rsf.flow.jsfnav.NavigationCase;
 import uk.org.ponder.rsf.flow.jsfnav.NavigationCaseReporter;
 import uk.org.ponder.rsf.view.ComponentChecker;
 import uk.org.ponder.rsf.view.DefaultView;
 import uk.org.ponder.rsf.view.ViewComponentProducer;
 import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
 import uk.org.ponder.rsf.viewstate.ViewParameters;
 import uk.org.ponder.rsf.viewstate.ViewParamsReporter;
 
 public class AssignmentListSortViewProducer implements ViewComponentProducer, ViewParamsReporter {
 
     public static final String VIEW_ID = "assignment_list-sortview";
    
     //sorting strings
     public static final String SORT_DIR_ASC = "asc";
     public static final String SORT_DIR_DESC = "desc";
     public static final String SORT_BY_SORT_INDEX = "sortIndex";
     public static final String SORT_BY_ASSIGNMENT = "title";
     public static final String SORT_BY_FOR = "restrictedToGroups";
     public static final String SORT_BY_STATUS = "draft";
     public static final String SORT_BY_OPEN = "openTime";
     public static final String SORT_BY_DUE = "dueDateForUngraded";		//change me to due date
     public static final String SORT_BY_UNGRADED = "in";		//fix me
     public static final String SORT_BY_IN = "in";				//fix me
     public static final String SORT_BY_NEW = "new";				//fix me
     //public static final String SORT_BY_SCALE = "scale";			//fix me
     public static final String DEFAULT_SORT_DIR = SORT_DIR_ASC;
     public static final String DEFAULT_OPPOSITE_SORT_DIR = SORT_DIR_DESC;
     public static final String DEFAULT_SORT_BY = SORT_BY_SORT_INDEX;
     
     private String current_sort_by = DEFAULT_SORT_BY;
     private String current_sort_dir = DEFAULT_SORT_DIR;
     private String opposite_sort_dir = DEFAULT_OPPOSITE_SORT_DIR;
     
     //images
     public static final String BULLET_UP_IMG_SRC = "/sakai-assignment2-tool/content/images/bullet_arrow_up.png";
     public static final String BULLET_DOWN_IMG_SRC = "/sakai-assignment2-tool/content/images/bullet_arrow_down.png";
     public static final String ATTACH_IMG_SRC = "/sakai-assignment2-tool/content/images/attach.png";
     
     public String getViewID() {
         return VIEW_ID;
     }
 
     private NavBarRenderer navBarRenderer;
     private PagerRenderer pagerRenderer;
     private MessageLocator messageLocator;
     private PagerBean pagerBean;
     private AssignmentLogic assignmentLogic;
     private ExternalLogic externalLogic;
     private Locale locale;
     private Assignment2Bean assignment2Bean;
     private SortHeaderRenderer sortHeaderRenderer;
     
     public void fillComponents(UIContainer tofill, ViewParameters viewparams, ComponentChecker checker) {
 
     	String currentUserId = externalLogic.getCurrentUserId();
     	
     	// use a date which is related to the current users locale
         DateFormat df = DateFormat.getDateInstance(DateFormat.MEDIUM, locale);
     	
     	//get parameters
     	AssignmentListSortViewParams params = (AssignmentListSortViewParams) viewparams;
     	if (params.sort_by == null) params.sort_by = DEFAULT_SORT_BY;
     	if (params.sort_dir == null) params.sort_dir = DEFAULT_SORT_DIR;
     	current_sort_by = params.sort_by;
     	current_sort_dir = params.sort_dir;
     	opposite_sort_dir = (SORT_DIR_ASC.equals(current_sort_dir) ? SORT_DIR_DESC : SORT_DIR_ASC);
 
     	//check if we need to duplicate an assignment, params.assignmentIdToDuplicate is not null
     	if (params.assignmentIdToDuplicate != null){
     		assignment2Bean.createDuplicate(params.assignmentIdToDuplicate);
     		params.assignmentIdToDuplicate = null;
     	}
     	
     	//get paging data
     	int total_count = assignmentLogic.getTotalCountViewableAssignments(currentUserId);
     	pagerBean.setTotalCount(total_count);
     		
         UIMessage.make(tofill, "page-title", "assignment2.assignment_list-sortview.title");
         navBarRenderer.makeNavBar(tofill, "navIntraTool:", VIEW_ID);
         pagerRenderer.makePager(tofill, "pagerDiv:", VIEW_ID, viewparams);
         
         UIVerbatim.make(tofill, "debug_info", "Currently, you are sorting by: <strong>" + current_sort_by + " " + 
         			current_sort_dir + "</strong>,   starting from record: <strong>" + params.current_start + "</strong> and paging: <strong>" + params.current_count + "</strong> items.");
         
         UIMessage.make(tofill, "heading", "assignment2.assignment_list-sortview.heading");
         //Links
         UIInternalLink.make(tofill, "assignment_list-reorder-link",
 					UIMessage.make("assignment2.assignment_list-reorder.title"),
 				new SimpleViewParameters(AssignmentListReorderProducer.VIEW_ID));
         UIMessage.make(tofill, "current_page", "assignment2.assignment_list-sortview.title");
         
         //table headers and sorting links
         UIMessage.make(tofill, "tableheader.remove", "assignment2.assignment_list-sortview.tableheader.remove");
         sortHeaderRenderer.makeSortingLink(tofill, "tableheader.assignment", viewparams, 
         		SORT_BY_ASSIGNMENT, "assignment2.assignment_list-sortview.tableheader.assignment");
         sortHeaderRenderer.makeSortingLink(tofill, "tableheader.for", viewparams, 
         		SORT_BY_FOR, "assignment2.assignment_list-sortview.tableheader.for");
         sortHeaderRenderer.makeSortingLink(tofill, "tableheader.status", viewparams, 
         		SORT_BY_STATUS, "assignment2.assignment_list-sortview.tableheader.status");
         sortHeaderRenderer.makeSortingLink(tofill, "tableheader.open", viewparams, 
         		SORT_BY_OPEN, "assignment2.assignment_list-sortview.tableheader.open");
         sortHeaderRenderer.makeSortingLink(tofill, "tableheader.due", viewparams, 
         		SORT_BY_DUE, "assignment2.assignment_list-sortview.tableheader.due");
         sortHeaderRenderer.makeSortingLink(tofill, "tableheader.ungraded", viewparams, 
         		SORT_BY_UNGRADED, "assignment2.assignment_list-sortview.tableheader.ungraded");
 
               
         UIForm form = UIForm.make(tofill, "form");
         
         List<Assignment2> entries = new ArrayList<Assignment2>();
         /*entries = assignmentLogic.getViewableAssignments(currentUserId, current_sort_by, current_sort_dir.equals(SORT_DIR_ASC), 
         		params.current_start, params.current_count);*/
         
         entries = assignmentLogic.getViewableAssignments();
         
         entries = assignment2Bean.filterListForPaging(entries, params.current_start, params.current_count);
         assignment2Bean.populateNonPersistedFieldsForAssignments(entries);
         
         if (entries.size() <= 0) {
             UIMessage.make(tofill, "assignment_empty", "assignment2.assignment_list-sortview.assignment_empty");
             return;
         }
         
         //Fill out Table
         for (Assignment2 assignment : entries){
         	UIBranchContainer row = UIBranchContainer.make(form, "assignment-row:");
         	UIBoundBoolean.make(row, "assignment_row_remove", 
         			"Assignment2Bean.selectedIds." + assignment.getAssignmentId().toString(),
         			Boolean.FALSE);
         	UIMessage.make(row, "assignment_row_remove_label", "assignment2.assignment_list-sortview.assignment_row_remove_label");
         	/** FIX ME Because Assignment was not retrieved with attachments****
         	if (assignment.getAttachmentSet() != null && !assignment.getAttachmentSet().isEmpty()){
         		UILink.make(row, "assignment_row_attach_img", "/sakai-assignment2-tool/content/images/attach.png");
         	}
         	**/
         	UIInternalLink.make(row, "assignment_row_link", assignment.getTitle(), 
         			new AssignmentAddViewParams(AssignmentPreviewProducer.VIEW_ID, assignment.getAssignmentId(), AssignmentListSortViewProducer.VIEW_ID));
         	UIInternalLink.make(row, "assignment_row_edit", 
         			UIMessage.make("assignment2.assignment_list-sortview.assignment_row_edit"), 
         			new AssignmentAddViewParams(AssignmentAddProducer.VIEW_ID, assignment.getAssignmentId()));
         	UIInternalLink.make(row, "assignment_row_duplicate", 
         			UIMessage.make("assignment2.assignment_list-sortview.assignment_row_duplicate"), 
         			new AssignmentListSortViewParams(AssignmentListSortViewProducer.VIEW_ID, current_sort_by, current_sort_dir, 
         					params.current_start, params.current_count, assignment.getAssignmentId()));
         	UIInternalLink.make(row, "assignment_row_grade", 
         			UIMessage.make("assignment2.assignment_list-sortview.assignment_row_grade"), 
         			new SimpleAssignmentViewParams(AssignmentGradeAssignmentProducer.VIEW_ID, assignment.getAssignmentId()));
         	
         	UIOutput.make(row, "assignment_row_for", assignment.getRestrictedToText());
         	if (assignment.isDraft()){
         		UIOutput.make(row, "assignment_row_draft_td");
         		UIOutput.make(row, "assignment_row_draft", assignment.getStatus());
         	} else {
         	   	UIOutput.make(row, "assignment_row_open_text", assignment.getStatus());
         	}
         	UIOutput.make(row, "assignment_row_open", df.format(assignment.getOpenTime()));
         	if (assignment.isUngraded()) {
         		if (assignment.getDueDateForUngraded() != null) {
         			UIOutput.make(row, "assignment_row_due", df.format(assignment.getDueDateForUngraded()));
         		} else {
        			UIOutput.make(row, "assignment_row_due", messageLocator.getMessage("assignment2.assignment_list-sortview.no_due_date"));
         		}
         	} else {
         		if (assignment.getDueDate() != null) {
         			UIOutput.make(row, "assignment_row_due", df.format(assignment.getDueDate()));
         		} else {
         			UIOutput.make(row, "assignment_row_due", messageLocator.getMessage("assignment2.assignment_list-sortview.no_due_date"));	
         		}
         	}
         	UIInternalLink.make(row, "assignment_row_in_new", "2/4", new SimpleViewParameters(AssignmentGradeAssignmentProducer.VIEW_ID));
         }
         
         UICommand.make(form, "submit_remove", UIMessage.make("assignment2.assignment_list-sortview.submit_remove"),
         		"Assignment2Bean.processActionRemove");
         
 
     }
     
     public ViewParameters getViewParameters() {
     	return new AssignmentListSortViewParams();
     }
     
     public void setMessageLocator(MessageLocator messageLocator) {
         this.messageLocator = messageLocator;
     }
  
     public void setNavBarRenderer(NavBarRenderer navBarRenderer) {
         this.navBarRenderer = navBarRenderer;
     }
     
     public void setPagerRenderer(PagerRenderer pagerRenderer) {
     	this.pagerRenderer = pagerRenderer;
     }
     
     public void setPagerBean(PagerBean pagerBean) {
     	this.pagerBean = pagerBean;
     }
     
     public void setAssignmentLogic (AssignmentLogic assignmentLogic) {
     	this.assignmentLogic = assignmentLogic;
     }
     
     public void setExternalLogic(ExternalLogic externalLogic) {
     	this.externalLogic = externalLogic;
     }
     
     public void setLocale(Locale locale) {
     	this.locale = locale;
     }
     
     public void setAssignment2Bean(Assignment2Bean assignment2Bean) {
     	this.assignment2Bean = assignment2Bean;
     }
     
     public void setSortHeaderRenderer(SortHeaderRenderer sortHeaderRenderer) {
     	this.sortHeaderRenderer = sortHeaderRenderer;
     }
 }
