 package org.sakaiproject.assignment2.tool.beans;
 
 import org.sakaiproject.assignment2.logic.AssignmentLogic;
 import org.sakaiproject.assignment2.logic.ExternalAnnouncementLogic;
 import org.sakaiproject.assignment2.logic.ExternalLogic;
 import org.sakaiproject.assignment2.model.Assignment2;
 import org.sakaiproject.assignment2.model.AssignmentGroup;
 import org.sakaiproject.assignment2.model.AssignmentAttachment;
 import org.sakaiproject.assignment2.tool.beans.Assignment2Validator;
 import org.sakaiproject.assignment2.exception.ConflictingAssignmentNameException;
 import org.sakaiproject.site.api.Group;
 import org.sakaiproject.tool.api.SessionManager;
 import org.sakaiproject.tool.api.ToolSession;
 import org.sakaiproject.assignment2.exception.AnnouncementPermissionException;
 import org.sakaiproject.assignment2.exception.StaleObjectModificationException;
 import org.sakaiproject.assignment2.tool.beans.locallogic.LocalAssignmentLogic;
 
 import uk.org.ponder.beanutil.entity.EntityBeanLocator;
 import uk.org.ponder.messageutil.MessageLocator;
 import uk.org.ponder.messageutil.TargettedMessage;
 import uk.org.ponder.messageutil.TargettedMessageList;
 
 import java.util.Collection;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.HashSet;
 
 public class Assignment2Bean {
 	
 	public Assignment2 assignment = new Assignment2();
 	public Date openDate;
 	
 	private static final String REMOVE = "remove";
 	private static final String BACK_TO_LIST = "back_to_list";
 	private static final String POST = "post";
 	private static final String PREVIEW = "preview";
 	private static final String REFRESH = "refresh";
 	private static final String SAVE_DRAFT = "save_draft";
 	private static final String EDIT = "edit";
 	private static final String CANCEL = "cancel";
 	private static final String FAILURE = "failure";
 	
 	
 	private Boolean requireAcceptUntil;
 	public void setRequireAcceptUntil(Boolean requireAcceptUntil) {
 		this.requireAcceptUntil = requireAcceptUntil;
 	}
 	
 	public Map selectedIds = new HashMap();
 	public Boolean restrictedToGroups;
 	
     private TargettedMessageList messages;
     public void setMessages(TargettedMessageList messages) {
     	this.messages = messages;
     }
 	
 	private AssignmentLogic logic;
 	public void setLogic(AssignmentLogic logic) {
 		this.logic = logic;
 	}
 	
 	private Map<String, Assignment2> OTPMap;
 	@SuppressWarnings("unchecked")
 	public void setAssignment2EntityBeanLocator(EntityBeanLocator entityBeanLocator) {
 		this.OTPMap = entityBeanLocator.getDeliveredBeans();
 	}
 		
 	private ExternalLogic externalLogic;
 	public void setExternalLogic(ExternalLogic externalLogic) {
 		this.externalLogic = externalLogic;
 	}
 	
 	private ExternalAnnouncementLogic announcementLogic;
 	public void setExternalAnnouncementLogic(ExternalAnnouncementLogic announcementLogic) {
 		this.announcementLogic = announcementLogic;
 	}
 	
 	private PreviewAssignmentBean previewAssignmentBean;
 	public void setPreviewAssignmentBean (PreviewAssignmentBean previewAssignmentBean) {
 		this.previewAssignmentBean = previewAssignmentBean;
 	}
 	
 	private MessageLocator messageLocator;
 	public void setMessageLocator (MessageLocator messageLocator) {
 		this.messageLocator = messageLocator;
 	}
 	
 	private LocalAssignmentLogic localAssignmentLogic;
 	public void setLocalAssignmentLogic (LocalAssignmentLogic localAssignmentLogic) {
 		this.localAssignmentLogic = localAssignmentLogic;
 	}
 	
 	private SessionManager sessionManager;
 	public void setSessionManager(SessionManager sessionManager) {
 		this.sessionManager = sessionManager;
 	}
 	
 	public String processActionBackToList() {
 		return BACK_TO_LIST;
 	}
 	
 	public String processActionPost() {
 		String result = POST;
 		for (String key : OTPMap.keySet()) {
 			Assignment2 assignment = OTPMap.get(key);
 			 result = internalProcessPost(assignment, key);
 		}
 		
 		return result;
 	}
 	
 	private String internalProcessPost(Assignment2 assignment, String key){
 		assignment.setDraft(Boolean.FALSE);
 		
 		if (this.requireAcceptUntil != null && this.requireAcceptUntil == Boolean.FALSE) {
 			assignment.setAcceptUntilTime(null);
 		}
 
 		Set<AssignmentAttachment> set = new HashSet();
 		if (assignment.getAttachmentSet() != null) {
 			set.addAll(assignment.getAttachmentSet());
 		}
 		
     	//get New attachments from session set
     	ToolSession session = sessionManager.getCurrentToolSession();
     	if (session.getAttribute("attachmentRefs") != null) {
     		for (String ref : (Set<String>)session.getAttribute("attachmentRefs")) {
     			AssignmentAttachment aa = new AssignmentAttachment();
     			aa.setAttachmentReference(ref);
     			set.add(aa);
     		}
     	}
     	Set<AssignmentAttachment> final_set = new HashSet();
     	//Now check for attachments that have been removed
     	if (session.getAttribute("removedAttachmentRefs") != null) {
 	    	for (AssignmentAttachment aa : set) {
 	    		//If this item in the set does not have a reference id that is 
 	    		// located in the removed attachment reference ids set
 	    		if (!((Set<String>) session.getAttribute("removedAttachmentRefs")).contains(aa.getAttachmentReference())){
 	    			final_set.add(aa);
 	    		}
 	    	}
     	} else {
     		final_set.addAll(set);
     	}
     	assignment.setAttachmentSet(final_set);
 
     	//Since in the UI, the select box bound to the gradableObjectId is always present
 		// we need to manually remove this value if the assignment is ungraded
 		if (assignment.isUngraded()) {
 			assignment.setGradableObjectId(null);
 		}
 		
 		//REMOVE THESE - TODO
 		assignment.setNotificationType(0);
 		//END REMOVE THESE 
 		
 		//do groups
 		Set<AssignmentGroup> newGroups = new HashSet();
		if (assignment.isRestrictedToGroups()){
 			//now add any new groups
 			if (assignment.getAssignmentGroupSet() != null) {
 				newGroups.addAll(assignment.getAssignmentGroupSet());
 			} 
 			
 			Set<AssignmentGroup> remGroups = new HashSet();
 			for (Iterator groupIter = selectedIds.keySet().iterator(); groupIter.hasNext();) {
 				String selectedId = (String)groupIter.next();
 				if (selectedIds.get(selectedId) == Boolean.TRUE) {
 					//Then add the group
 					AssignmentGroup newGroup = new AssignmentGroup();
 					newGroup.setAssignment(assignment);
 					newGroup.setGroupId(selectedId);
 					newGroups.add(newGroup);
 				} else if (selectedIds.get(selectedId) == Boolean.FALSE) {
 					//then remove the group
 					for (AssignmentGroup ag : newGroups) {
 						if (ag.getGroupId().equals(selectedId)) {
 							remGroups.add(ag);
 						}
 					}
 				}
 			}
 			newGroups.removeAll(remGroups);
 		}
 		assignment.setAssignmentGroupSet(newGroups);
 		
 		
 		//start the validator
 		Assignment2Validator validator = new Assignment2Validator();
 		if (validator.validate(assignment, messages)){
 			//Validation Passed!
 			try {
 				Assignment2 assignmentFromDb = null;
 				if (assignment.getId() != null) {
 					assignmentFromDb = logic.getAssignmentByIdWithGroups(assignment.getId());
 				}
 				
 				logic.saveAssignment(assignment);
 				localAssignmentLogic.handleAnnouncement(assignment, assignmentFromDb);
 				
 			} catch( ConflictingAssignmentNameException e){
 				messages.addMessage(new TargettedMessage("assignment2.assignment_post.conflicting_assignment_name",
 						new Object[] { assignment.getTitle() }, "Assignment2." + key + ".title"));
 				return FAILURE;
 			} 
 			
 			//set Messages
 			if (key.startsWith(EntityBeanLocator.NEW_PREFIX)) {
 				messages.addMessage(new TargettedMessage("assignment2.assignment_post",
 						new Object[] { assignment.getTitle() }, TargettedMessage.SEVERITY_INFO));
 			}
 			else {
 				messages.addMessage(new TargettedMessage("assignment2.assignment_save",
 						new Object[] { assignment.getTitle() }, TargettedMessage.SEVERITY_INFO));
 			}
 		} else {
 			messages.addMessage(new TargettedMessage("assignment2.assignment_post_error"));
 			return FAILURE;
 		}
 		//Clear out session attachment information if everything successful
     	session.removeAttribute("attachmentRefs");
     	session.removeAttribute("removedAttachmentRefs");
 		return POST;
 	}
 	
 	
 	public String processActionPreview() {
 		for (String key : OTPMap.keySet()) {
 			Assignment2 assignment = OTPMap.get(key);
 			if (this.requireAcceptUntil != null && this.requireAcceptUntil == Boolean.FALSE) {
 				assignment.setAcceptUntilTime(null);
 			}
 			previewAssignmentBean.setAssignment(assignment);
 			previewAssignmentBean.setOTPKey(key);
 		}
 		return PREVIEW;
 	}
 	
 	public String processActionEdit() {
 		return EDIT;
 	}
 	
 	public String processActionSaveDraft() {
 		String currentUserId = externalLogic.getCurrentUserId();
 		for (String key : OTPMap.keySet()) {
 			Assignment2 assignment = OTPMap.get(key);
 
 			assignment.setDraft(Boolean.TRUE);
 
 			if (this.requireAcceptUntil != null && this.requireAcceptUntil == Boolean.FALSE) {
 				assignment.setAcceptUntilTime(null);
 			}
 			
 			//REMOVE THESE - TODO
 			assignment.setNotificationType(0);
 			//END REMOVE THESE
 			
 			//start the validator
 			Assignment2Validator validator = new Assignment2Validator();
 			if (validator.validate(assignment, messages)){
 				//Validation Passed!
 				try {
 					Assignment2 assignmentFromDb = null;
 					if (assignment.getId() != null) {
 						assignmentFromDb = logic.getAssignmentByIdWithGroups(assignment.getId());
 					}
 					
 					logic.saveAssignment(assignment);
 					
 					localAssignmentLogic.handleAnnouncement(assignment, assignmentFromDb);
 				} catch( ConflictingAssignmentNameException e){
 					messages.addMessage(new TargettedMessage("assignment2.assignment_save_draft.conflicting_assignment_name",
 							new Object[] { assignment.getTitle() }, "Assignment2." + key + ".title"));
 					return FAILURE;
 				} 
 				
 				//set Messages
 				messages.addMessage(new TargettedMessage("assignment2.assignment_save_draft",
 					new Object[] { assignment.getTitle() }, TargettedMessage.SEVERITY_INFO));
 				
 			} else {
 				messages.addMessage(new TargettedMessage("assignment2.assignment_save_draft_error"));
 				return FAILURE;
 			}
 		}
 		return SAVE_DRAFT;
 	}
 	
 	public String processActionCancel() {
 		//Clear out session attachment information if everything successful
     	ToolSession session = sessionManager.getCurrentToolSession();
     	session.removeAttribute("attachmentRefs");
     	session.removeAttribute("removedAttachmentRefs");
 		return CANCEL;
 	}
 	
 	public String processActionRemove() {
 		String currentUserId = externalLogic.getCurrentUserId();
 		List<Assignment2> entries = logic.getViewableAssignments();
 		int assignmentsRemoved = 0;
 		for (Assignment2 assignment : entries) {
 			if (selectedIds.get(assignment.getId().toString()) == Boolean.TRUE){
 				try {
 					logic.deleteAssignment(assignment);
 					assignmentsRemoved++;
 				} catch (AnnouncementPermissionException ape) {
 					// TODO the assign was deleted, but announcement was not
 					// b/c user did not have delete perm in annc tool
 				} catch (StaleObjectModificationException some) {
 					// TODO provide a message to user that someone else was editing
 					// this object at the same time
 				}
 			}
 		}
 		messages.addMessage( new TargettedMessage("assignment2.assignments_remove",
 				new Object[] { new Integer(assignmentsRemoved) },
 		        TargettedMessage.SEVERITY_INFO));
 		return REMOVE;
 	}
 	
 	public void createDuplicate(Long assignmentId) {
 		Assignment2Creator creator = new Assignment2Creator();
 		creator.setExternalLogic(externalLogic);
 		creator.setMessageLocator(messageLocator);
 		Assignment2 duplicate = creator.createDuplicate(logic.getAssignmentByIdWithGroupsAndAttachments(assignmentId));
 		try {
 			logic.saveAssignment(duplicate);
 			localAssignmentLogic.handleAnnouncement(duplicate, null);
 			
 		} catch(ConflictingAssignmentNameException e){
 			messages.addMessage(new TargettedMessage("assignment2.assignment_post.duplicate_conflicting_assignment_name",
 					new Object[]{ duplicate.getTitle() }));
 			return;
 		} catch (SecurityException e) {
 			messages.addMessage(new TargettedMessage("assignment2.assignment_post.security_exception"));
 			return;
 		}
 		messages.addMessage(new TargettedMessage("assignment2.assignment_post.duplicate",
 			new Object[] {duplicate.getTitle() }, TargettedMessage.SEVERITY_INFO));
 	}
 	
 
 }
