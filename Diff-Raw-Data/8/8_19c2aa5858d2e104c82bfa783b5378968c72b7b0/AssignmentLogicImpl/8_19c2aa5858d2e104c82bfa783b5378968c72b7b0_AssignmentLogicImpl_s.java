 /**********************************************************************************
  * $URL$
  * $Id$
  ***********************************************************************************
  *
  * Copyright (c) 2007 The Sakai Foundation.
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
 
 package org.sakaiproject.assignment2.logic.impl;
 
 import java.text.DateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.sakaiproject.assignment2.dao.AssignmentDao;
 import org.sakaiproject.assignment2.exception.AnnouncementPermissionException;
 import org.sakaiproject.assignment2.exception.AssignmentNotFoundException;
 import org.sakaiproject.assignment2.exception.CalendarPermissionException;
 import org.sakaiproject.assignment2.exception.NoGradebookItemForGradedAssignmentException;
 import org.sakaiproject.assignment2.exception.StaleObjectModificationException;
 import org.sakaiproject.assignment2.logic.AssignmentBundleLogic;
 import org.sakaiproject.assignment2.logic.AssignmentLogic;
 import org.sakaiproject.assignment2.logic.AssignmentPermissionLogic;
 import org.sakaiproject.assignment2.logic.ExternalAnnouncementLogic;
 import org.sakaiproject.assignment2.logic.ExternalCalendarLogic;
 import org.sakaiproject.assignment2.logic.ExternalGradebookLogic;
 import org.sakaiproject.assignment2.logic.ExternalLogic;
 import org.sakaiproject.assignment2.model.Assignment2;
 import org.sakaiproject.assignment2.model.AssignmentAttachment;
 import org.sakaiproject.assignment2.model.AssignmentGroup;
 import org.sakaiproject.assignment2.model.constants.AssignmentConstants;
 import org.sakaiproject.assignment2.taggable.api.AssignmentActivityProducer;
 import org.sakaiproject.component.cover.ComponentManager;
 import org.sakaiproject.exception.PermissionException;
 import org.sakaiproject.taggable.api.TaggingManager;
 import org.sakaiproject.taggable.api.TaggingProvider;
 import org.springframework.orm.hibernate3.HibernateOptimisticLockingFailureException;
 
 
 /**
  * This is the interface for the Assignment object
  * 
  * @author <a href="mailto:wagnermr@iupui.edu">michelle wagner</a>
  */
 public class AssignmentLogicImpl implements AssignmentLogic{
 	
 	private static Log log = LogFactory.getLog(AssignmentLogicImpl.class);
 	
 	private ExternalLogic externalLogic;
     public void setExternalLogic(ExternalLogic externalLogic) {
         this.externalLogic = externalLogic;
     }
     
     private ExternalGradebookLogic gradebookLogic;
     public void setExternalGradebookLogic(ExternalGradebookLogic gradebookLogic) {
         this.gradebookLogic = gradebookLogic;
     }
     
     private ExternalAnnouncementLogic announcementLogic;
     public void setExternalAnnouncementLogic(ExternalAnnouncementLogic announcementLogic) {
         this.announcementLogic = announcementLogic;
     }
     
     private ExternalCalendarLogic calendarLogic;
     public void setExternalCalendarLogic(ExternalCalendarLogic calendarLogic) {
         this.calendarLogic = calendarLogic;
     }
     
     private AssignmentPermissionLogic permissionLogic;
     public void setPermissionLogic(AssignmentPermissionLogic permissionLogic) {
         this.permissionLogic = permissionLogic;
     }
     
     private AssignmentDao dao;
     public void setDao(AssignmentDao dao) {
         this.dao = dao;
     }
     
     private AssignmentBundleLogic bundleLogic;
     public void setAssignmentBundleLogic(AssignmentBundleLogic bundleLogic) {
     	this.bundleLogic = bundleLogic;
     }
     
 	public void init(){
 		if(log.isDebugEnabled()) log.debug("init");
 	}
 
 	public Assignment2 getAssignmentById(Long assignmentId)
 	{
 		if (assignmentId == null) {
 			throw new IllegalArgumentException("Null assignmentId passed to getAssignmentById");
 		}
 		
 		Assignment2 assign = (Assignment2) dao.findById(Assignment2.class, assignmentId);
 		
 		if (assign == null) {
 			throw new AssignmentNotFoundException("No assignment found with id: " + assignmentId);
 		}
 		
 		return assign;
     }
 	
 	public Assignment2 getAssignmentByIdWithAssociatedData(Long assignmentId) {
 		if (assignmentId == null) {
 			throw new IllegalArgumentException("Null assignmentId passed to getAssignmentByIdWithAssociatedData");
 		}
 		// first, retrieve Assignment2 object
 		Assignment2 assign = (Assignment2) dao.getAssignmentByIdWithGroupsAndAttachments(assignmentId);
 		
 		if (assign == null) {
 			throw new AssignmentNotFoundException("No assignment found with id: " + assignmentId);
 		}
 
 		return assign;
 	}
 	
 	public Assignment2 getAssignmentByIdWithGroups(Long assignmentId) {
 		if (assignmentId == null) {
 			throw new IllegalArgumentException("Null assignmentId passed to getAssignmentByIdWithGroups");
 		}
 		
 		Assignment2 assign = (Assignment2) dao.getAssignmentByIdWithGroups(assignmentId);
 		if (assign == null) {
 			throw new AssignmentNotFoundException("No assignment found with id: " + assignmentId);
 		}
 		
 		return assign;
 	}
 	
 	public Assignment2 getAssignmentByIdWithGroupsAndAttachments(Long assignmentId) {
 		if (assignmentId == null) {
 			throw new IllegalArgumentException("Null assignmentId passed to getAssignmentByIdWithGroupsAndAttachments");
 		}
 		
 		Assignment2 assign = (Assignment2) dao.getAssignmentByIdWithGroupsAndAttachments(assignmentId);
 		
 		if (assign == null) {
 			throw new AssignmentNotFoundException("No assignment found with id: " + assignmentId);
 		}
 		
 		return assign;
 	}
 	
 	public void saveAssignment(Assignment2 assignment) {
 		String currentContextId = externalLogic.getCurrentContextId();
 		saveAssignment(assignment, currentContextId);
 	}
 	
 	public void saveAssignment(Assignment2 assignment, String contextId) throws SecurityException, 
 		NoGradebookItemForGradedAssignmentException
 	{
 		if (assignment == null || contextId == null) {
 			throw new IllegalArgumentException("Null assignment or contextId passed to saveAssignment");
 		}
 		
 		String currentUserId = externalLogic.getCurrentUserId();
 		
 		if (!assignment.isUngraded() && assignment.getGradableObjectId() == null) {
 			throw new NoGradebookItemForGradedAssignmentException("The assignment to save " + 
 					"was defined as graded but it had a null gradableObjectId");
 		}
 		
 		if (!permissionLogic.isCurrentUserAbleToEditAssignments(contextId)) {
 			throw new SecurityException("Current user may not save assignment " + assignment.getTitle()
                     + " because they do not have edit permission");
 		}
 		
 		boolean isNewAssignment = true;
 		Assignment2 existingAssignment = null;
 		
 		// determine if this is a new assignment
 		if (assignment.getId() != null) {
 			// check to see if assignment exists
 			existingAssignment = (Assignment2)dao.getAssignmentByIdWithGroupsAndAttachments(assignment.getId());	
 			if (existingAssignment != null) {
 				isNewAssignment = false;
 			}
 		}
 		
     	// trim trailing spaces on title
     	assignment.setTitle(assignment.getTitle().trim());
 		
 		if (isNewAssignment) {
 
         	// identify the next sort index to be used
         	Integer highestIndex = dao.getHighestSortIndexInSite(contextId);
         	if (highestIndex != null) {
         		assignment.setSortIndex(highestIndex + 1);
         	} else {
         		assignment.setSortIndex(0);
         	}
         	
         	assignment.setRemoved(Boolean.FALSE);
         	assignment.setCreateTime(new Date());
         	assignment.setCreator(currentUserId);
         	
         	Set<AssignmentAttachment> attachSet = new HashSet<AssignmentAttachment>();
         	if (assignment.getAttachmentSet() != null) {
         		attachSet = assignment.getAttachmentSet();
         	}
         	Set<AssignmentGroup> groupSet = new HashSet<AssignmentGroup>();
         	if (assignment.getAssignmentGroupSet() != null) {
         		groupSet = assignment.getAssignmentGroupSet();
         	}
         	
         	// make sure the assignment has been set for the attachments and groups
         	populateAssignmentForAttachmentAndGroupSets(attachSet, groupSet, assignment);
         	
         	Set<Assignment2> assignSet = new HashSet<Assignment2>();
         	assignSet.add(assignment);
         	
         	dao.saveMixedSet(new Set[] {assignSet, attachSet, groupSet});
         	if(log.isDebugEnabled()) log.debug("Created assignment: " + assignment.getTitle());
   
 		} else {
 			
 			assignment.setRemoved(Boolean.FALSE);
 			assignment.setModifiedBy(currentUserId);
 			assignment.setModifiedTime(new Date());
 			
 			Set<AssignmentAttachment> attachToDelete = identifyAttachmentsToDelete(existingAssignment, assignment);
 			Set<AssignmentGroup> groupsToDelete = identifyGroupsToDelete(existingAssignment, assignment);
 			
 			try {
 	        	Set<AssignmentAttachment> attachSet = new HashSet<AssignmentAttachment>();
 	        	if (assignment.getAttachmentSet() != null) {
 	        		attachSet = assignment.getAttachmentSet();
 	        	}
 	        	Set<AssignmentGroup> groupSet = new HashSet<AssignmentGroup>();
 	        	if (assignment.getAssignmentGroupSet() != null) {
 	        		groupSet = assignment.getAssignmentGroupSet();
 	        	}
 	        	
 	        	// make sure the assignment has been set for the attachments and groups
 	        	populateAssignmentForAttachmentAndGroupSets(attachSet, groupSet, assignment);
 	        	
 	        	Set<Assignment2> assignSet = new HashSet<Assignment2>();
 	        	assignSet.add(assignment);
 	        	
 	        	dao.saveMixedSet(new Set[] {assignSet, attachSet, groupSet});
 	        	if(log.isDebugEnabled())log.debug("Updated assignment: " + assignment.getTitle() + "with id: " + assignment.getId());
 	            
 	            if ((attachToDelete != null && !attachToDelete.isEmpty()) ||
 	            		(groupsToDelete != null && !groupsToDelete.isEmpty())) {
 	            	dao.deleteMixedSet(new Set[] {attachToDelete, groupsToDelete});
 	            	if(log.isDebugEnabled())log.debug("Attachments and/or groups removed for updated assignment " + assignment.getId());
 	            }
 			} catch (HibernateOptimisticLockingFailureException holfe) {
 				if(log.isInfoEnabled()) log.info("An optimistic locking failure occurred while attempting to update assignment with id: " + assignment.getId());
 	            throw new StaleObjectModificationException("An optimistic locking failure occurred while attempting to update assignment with id: " + assignment.getId(), holfe);
 			}
 		}
 		
 		// now let's handle the impact on announcements
 		if (externalLogic.siteHasTool(contextId, ExternalLogic.TOOL_ID_ANNC)) {
 			try {
 				saveAssignmentAnnouncement(existingAssignment, assignment);
 			} catch (AnnouncementPermissionException ape) {
 				throw new AnnouncementPermissionException("The current user is not " +
 						"authorized to update announcements in the announcements " +
 						"tool. Any related announcements were NOT updated", ape);
 			}
 		}
 		
 		// now let's handle the impact on the Schedule
 		if (externalLogic.siteHasTool(contextId, ExternalLogic.TOOL_ID_SCHEDULE)) {
 			try {
 				handleDueDateEvent(existingAssignment, assignment);
 			} catch (CalendarPermissionException cpe) {
 				throw new CalendarPermissionException("The current user is not " +
 						"authorized to update events in the Schedule " +
 						"tool. Any related events were NOT updated", cpe);
 			}
 		}
 	}
 	
 
 	public void deleteAssignment(Assignment2 assignment) throws SecurityException, AnnouncementPermissionException
 	{
 		if (assignment == null) {
 			throw new IllegalArgumentException("Null assignment passed to deleteAssignment");
 		}
 		
 		if (assignment.getId() == null) {
 			throw new IllegalArgumentException("The passed assignment does not have an id. Can only delete persisted assignments");
 		}
 		
 		String currentContextId = externalLogic.getCurrentContextId();
 		
 		if (!permissionLogic.isCurrentUserAbleToEditAssignments(currentContextId)) {
 			if (log.isDebugEnabled()) log.debug("User not authorized to add/delete/update announcements");
 			throw new SecurityException("Current user may not delete assignment " + assignment.getTitle()
                     + " because they do not have edit permission");
 		}
 
 		assignment.setRemoved(true);
 		assignment.setModifiedBy(externalLogic.getCurrentUserId());
 		assignment.setModifiedTime(new Date());
 		
 		// remove associated announcements, if appropriate
 		String announcementIdToDelete = null;
 		if (assignment.getAnnouncementId() != null) {
 			announcementIdToDelete = assignment.getAnnouncementId();
 			assignment.setAnnouncementId(null);
 			assignment.setHasAnnouncement(Boolean.FALSE);
 		}
 		
 		// remove associated Schedule/Calendar events, if appropriate
 		String eventIdToDelete = null;
 		if (assignment.getEventId() != null) {
 			eventIdToDelete = assignment.getEventId();
 			assignment.setEventId(null);
 			assignment.setAddedToSchedule(false);
 		}
 
 		try {
 			dao.update(assignment);
 			if(log.isDebugEnabled()) log.debug("Deleted assignment: " + assignment.getTitle() + " with id " + assignment.getId());
 			
 			// now remove the announcement, if applicable
 			if (announcementIdToDelete != null) {
 				announcementLogic.deleteOpenDateAnnouncement(announcementIdToDelete, currentContextId);
 				if(log.isDebugEnabled()) log.debug("Deleted announcement with id " + announcementIdToDelete + " for assignment " + assignment.getId());
 			}
 			
 			// now remove the event, if applicable
 			if (eventIdToDelete !=  null) {
 				calendarLogic.deleteDueDateEvent(eventIdToDelete, currentContextId);
 				if(log.isDebugEnabled()) log.debug("Deleted event with id " + eventIdToDelete + 
 						" for assignment " + assignment.getId());
 			}
 			
 			//clean up tags...
 			try
 			{
 				TaggingManager taggingManager = (TaggingManager) ComponentManager
 						.get("org.sakaiproject.taggable.api.TaggingManager");
 
 				AssignmentActivityProducer assignmentActivityProducer = (AssignmentActivityProducer) ComponentManager
 						.get("org.sakaiproject.assignment2.taggable.api.AssignmentActivityProducer");
 
 				if (taggingManager.isTaggable()) {
 					for (TaggingProvider provider : taggingManager
 							.getProviders()) {
 						provider.removeTags(assignmentActivityProducer
 								.getActivity(assignment));
 					}
 				}
 			}
 			catch (PermissionException pe)
 			{
 				throw new SecurityException("The current user is not authorized to remove tags in the assignment tool, " +
 						"but the assignment was deleted", pe);
 			}
 		} catch (HibernateOptimisticLockingFailureException holfe) {
 			if(log.isInfoEnabled()) log.info("An optimistic locking failure occurred " +
 					"while attempting to update an assignment");
 			throw new StaleObjectModificationException("Locking failure occurred " +
 					"while removing assignment with id: " + assignment.getId(), holfe);
 		} catch (AnnouncementPermissionException ape) {
 			if(log.isDebugEnabled()) log.debug("The current user is not authorized to " +
 					"remove announcements in the annc tool, " +
 					"but the assignment was deleted");
 			throw new AnnouncementPermissionException("The current user is not authorized " +
 					"to remove announcements in the annc tool, " +
 					"but the assignment was deleted", ape);
 		} catch (CalendarPermissionException cpe) {
 			if(log.isDebugEnabled()) log.debug("The current user is not authorized " +
 					"to remove events in the Schedule tool, " +
 					"but the assignment was deleted");
 			throw new CalendarPermissionException("The current user is not authorized " +
 					"to remove events in the Schedule tool, " +
 					"but the assignment was deleted", cpe);
 		}
 	}
 	
 
 	public List<Assignment2> getViewableAssignments()
 	{   
 		List<Assignment2> viewableAssignments = new ArrayList<Assignment2>();
 		String contextId = externalLogic.getCurrentContextId();
 		String userId = externalLogic.getCurrentUserId();
 
 		Set<Assignment2> allAssignments = dao.getAssignmentsWithGroupsAndAttachments(contextId);
 
 		if (allAssignments != null && !allAssignments.isEmpty()) {
 
 			List<Assignment2> gradedAssignments = new ArrayList<Assignment2>();
 
 			// users may view ungraded items if:
 			//  a) it is not restricted to groups
 			//  b) it is restricted, but user has grade all perm
 			//  c) it is restricted, but user is a member of restricted group
 			//  d) it is not draft or user has edit perm
 
 			List<String> userGroupIds = externalLogic.getUserMembershipGroupIdList(userId, contextId);	
 			boolean isUserAbleToEdit = permissionLogic.isCurrentUserAbleToEditAssignments(contextId);
 			boolean isUserAStudent = gradebookLogic.isCurrentUserAStudentInGb(contextId);
 
 			for (Assignment2 assignment : allAssignments) {
 				if (!assignment.isDraft() || isUserAbleToEdit) {
 					// students may not view if not open
 					if (!isUserAStudent || (isUserAStudent && assignment.getOpenTime().before(new Date()))) 
 						if (assignment.isUngraded()) {
 							if (permissionLogic.isUserAbleToViewUngradedAssignment(assignment, userGroupIds)) {
 								viewableAssignments.add(assignment);
 							} 
 
 						} else {
 							gradedAssignments.add(assignment);
 						}
 				}
 			}
 
 			if (gradedAssignments != null && !gradedAssignments.isEmpty()) {
 				// now, we need to filter the assignments that are associated with
 				// the gradebook according to grader permissions and populate the
 				// gradebook data
 				List<Assignment2> viewableGbAssignments = gradebookLogic.getViewableGradedAssignments(gradedAssignments, externalLogic.getCurrentContextId());
 				if (viewableGbAssignments != null) {
 
 					for (Assignment2 assignment : viewableGbAssignments) {
 						
 						boolean restrictedToGroups = assignment.getAssignmentGroupSet() != null
 						&& !assignment.getAssignmentGroupSet().isEmpty();
 						
 						// if user is a "student" in terms of the gb, we need to filter the view
 						// by AssignmentGroup restrictions.
 						if (restrictedToGroups && isUserAStudent) {
 							if (permissionLogic.isUserAMemberOfARestrictedGroup(userGroupIds, assignment.getAssignmentGroupSet())) {
 								viewableAssignments.add(assignment);
 							}
 						} else {
 							viewableAssignments.add(assignment);
 						}
 					}
 				}
 			}
 		}
 		
 		return viewableAssignments;
 	}
 	
 	public void setAssignmentSortIndexes(Long[] assignmentIds)
 	{
 		int numAssignsInSite = dao.countByProperties(Assignment2.class, 
 				new String[] {"contextId", "removed"}, new Object[]{externalLogic.getCurrentContextId(), false});
 		
 		if ((assignmentIds == null && numAssignsInSite > 0) ||
 				(assignmentIds != null && assignmentIds.length != numAssignsInSite)) {
 			throw new IllegalArgumentException("The length of the id list passed does not match the num assignments in the site");
 		}
 		
 		if (assignmentIds != null) {
 			String userId = externalLogic.getCurrentUserId();
 			//Assume array of longs is in correct order now
 			//so that the index of the array is the new 
 			//sort index
 			Set<Assignment2> assignSet = new HashSet<Assignment2>();
 			for (int i=0; i < assignmentIds.length; i++){
 				//get Assignment
 	    		Assignment2 assignment = getAssignmentById(assignmentIds[i]);
 	    		if (assignment != null){
 	    			//check if we need to update
 	    			if (assignment.getSortIndex() != i){
 	    				//update and save
 		    			assignment.setSortIndex(i);
 		    			assignment.setModifiedBy(userId);
 		    			assignment.setModifiedTime(new Date());
 		    			assignSet.add(assignment);
 		    			if(log.isDebugEnabled()) log.debug("Assignment " + assignment.getId() + " sort index changed to " + i);
 	    			}
 	    		}
 	    	}
 			try {
 				dao.saveMixedSet(new Set[]{assignSet});
 				if(log.isDebugEnabled()) log.debug("Reordered assignments saved. " + 
 						assignSet.size() + " assigns were updated");
 			} catch (HibernateOptimisticLockingFailureException holfe) {
 				if(log.isInfoEnabled()) log.info("An optimistic locking failure occurred while attempting to reorder the assignments");
 	            throw new StaleObjectModificationException("An optimistic locking failure occurred while attempting to reorder the assignments", holfe);
 			}
 		}
 	}
 	
 	public int getStatusForAssignment(Assignment2 assignment) {
 		if (assignment == null){
 			throw new IllegalArgumentException("Null assignment passed to getStatusForAssignment");
 		}
 		if (assignment.isDraft())
 			return AssignmentConstants.STATUS_DRAFT;
 		
 		Date currDate = new Date();
 		
 		if (currDate.before(assignment.getOpenTime()))
 			return AssignmentConstants.STATUS_NOT_OPEN;
 		
 		if (assignment.getAcceptUntilTime() != null && currDate.after(assignment.getAcceptUntilTime())) {
 			return AssignmentConstants.STATUS_CLOSED;
 		}
 		
 		if (assignment.getDueDate() != null) {
 			if (currDate.after(assignment.getDueDate()))
 				return AssignmentConstants.STATUS_DUE;				
 		}
 		
 		return AssignmentConstants.STATUS_OPEN;	
 	}
 	
 	private void populateAssignmentForAttachmentAndGroupSets(Set<AssignmentAttachment> attachSet, Set<AssignmentGroup> groupSet, Assignment2 assign) {
 		if (attachSet != null && !attachSet.isEmpty()) {
 			for (AssignmentAttachment attach : attachSet) {
 				if (attach != null) {
 					attach.setAssignment(assign);
 				}
 			}
 		}
 		if (groupSet != null && !groupSet.isEmpty()) {
 			for (AssignmentGroup group : groupSet) {
 				if (group != null) {
 					group.setAssignment(assign);
 				}
 			}
 		}
 	}
 	
 	private Set<AssignmentAttachment> identifyAttachmentsToDelete(Assignment2 existingAssign, Assignment2 updatedAssign) {
 		Set<AssignmentAttachment> attachToRemove = new HashSet<AssignmentAttachment>();
 		
 		if (updatedAssign != null && existingAssign != null && existingAssign.getAttachmentSet() != null) {
 			for (AssignmentAttachment attach : existingAssign.getAttachmentSet()) {
 				if (attach != null) {
 					if (updatedAssign.getAttachmentSet() == null ||
 							!updatedAssign.getAttachmentSet().contains(attach)) {
 						// we need to delete this attachment
 						attachToRemove.add(attach);
 					} 
 				}
 			}
 		}
 		
 		return attachToRemove;
 	}
 	
 	private Set<AssignmentGroup> identifyGroupsToDelete(Assignment2 existingAssign, Assignment2 updatedAssign) {
 		Set<AssignmentGroup> groupsToRemove = new HashSet<AssignmentGroup>();
 		
 		if (updatedAssign != null && existingAssign != null && existingAssign.getAssignmentGroupSet() != null) {
 			for (AssignmentGroup attach : existingAssign.getAssignmentGroupSet()) {
 				if (attach != null) {
 					if (updatedAssign.getAssignmentGroupSet() == null ||
 							!updatedAssign.getAssignmentGroupSet().contains(attach)) {
 						// we need to delete this group
 						groupsToRemove.add(attach);
 					} 
 				}
 			}
 		}
 		
 		return groupsToRemove;
 	}
 	
 	/**
 	 * Given the originalAssignment and the updated (or newly created) version, will determine if an
 	 * announcement needs to be added, updated, or deleted. Announcements are updated
 	 * if there is a change in title, open date, or group restrictions. They are
 	 * deleted if the assignment is changed to draft status. 
 	 * @param originalAssignmentWithGroups - original assignment with the group info populated
 	 * @param updatedAssignment - updated (or newly created) assignment with the group info populated
 	 */
 	private void saveAssignmentAnnouncement(Assignment2 originalAssignment, Assignment2 updatedAssignment) {
 		if (updatedAssignment == null) {
 			throw new IllegalArgumentException("Null updatedAssignment passed to saveAssignmentAnnouncement");
 		}
 		
 		if (updatedAssignment.getId() == null) {
 			throw new IllegalArgumentException("The updatedAssignment passed to saveAssignmentAnnouncement must have an id");
 		}
 		
 		if (!permissionLogic.isCurrentUserAbleToEditAssignments(updatedAssignment.getContextId())) {
 			throw new SecurityException("Current user is not allowed to edit assignments in context " + updatedAssignment.getContextId());
 		}
 
 		// make the open date locale-aware
 		// use a date which is related to the current users locale
         DateFormat df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, bundleLogic.getLocale());
 		
 		String newAnncSubject = bundleLogic.getFormattedMessage("assignment2.assignment_annc_subject",
     			new Object[] {updatedAssignment.getTitle()});
     	String newAnncBody = bundleLogic.getFormattedMessage("assignment2.assignment_annc_body",
    			new Object[] {updatedAssignment.getTitle(), df.format(updatedAssignment.getOpenTime())});
     	String updAnncSubject = bundleLogic.getFormattedMessage("assignment2.assignment_annc_subject_edited",
     			new Object[] {updatedAssignment.getTitle()});
     	String updAnncBody = bundleLogic.getFormattedMessage("assignment2.assignment_annc_subject_edited",
    			new Object[] {updatedAssignment.getTitle(), df.format(updatedAssignment.getOpenTime())});
 		
 		if (originalAssignment == null) {
 			// this was a new assignment
 			// check to see if there will be an announcement for the open date
 			if (updatedAssignment.getHasAnnouncement() && !updatedAssignment.isDraft()) {
 				// add an announcement for the open date for this assignment
 				String announcementId = announcementLogic.addOpenDateAnnouncement(
 						updatedAssignment.getListOfAssociatedGroupReferences(), 
 						updatedAssignment.getContextId(), newAnncSubject,
 						newAnncBody, updatedAssignment.getOpenTime());
 				updatedAssignment.setAnnouncementId(announcementId);
 				dao.update(updatedAssignment);
 			}
 		} else if (updatedAssignment.isDraft()) {
 			if (updatedAssignment.getAnnouncementId() != null) {
 				announcementLogic.deleteOpenDateAnnouncement(updatedAssignment.getAnnouncementId(), updatedAssignment.getContextId());
 				updatedAssignment.setAnnouncementId(null);
 				dao.update(updatedAssignment);
 			}
 		} else if (originalAssignment.getAnnouncementId() == null && updatedAssignment.getHasAnnouncement()) {
 			// this is a new announcement
 			String announcementId = announcementLogic.addOpenDateAnnouncement(updatedAssignment.getListOfAssociatedGroupReferences(), 
 					updatedAssignment.getContextId(), newAnncSubject, newAnncBody, updatedAssignment.getOpenTime());
 			updatedAssignment.setAnnouncementId(announcementId);
 			dao.update(updatedAssignment);
 		} else if (originalAssignment.getAnnouncementId() != null && !updatedAssignment.getHasAnnouncement()) {
 			// we must remove the original announcement
 			announcementLogic.deleteOpenDateAnnouncement(updatedAssignment.getAnnouncementId(), updatedAssignment.getContextId());
 			updatedAssignment.setAnnouncementId(null);
 			dao.update(updatedAssignment);
 		} else if (updatedAssignment.getHasAnnouncement()){
 			// if title, open date, or group restrictions were updated, we need to update the announcement
 			Date oldTime = (Date)originalAssignment.getOpenTime();
 			Date newTime = updatedAssignment.getOpenTime();
 			if (!originalAssignment.getTitle().equals(updatedAssignment.getTitle()) ||
 					(oldTime.after(newTime) || oldTime.before(newTime)) ||
 					!originalAssignment.getListOfAssociatedGroupReferences().equals(updatedAssignment.getListOfAssociatedGroupReferences())) {
 				announcementLogic.updateOpenDateAnnouncement(updatedAssignment.getAnnouncementId(), 
 						updatedAssignment.getListOfAssociatedGroupReferences(), 
 						updatedAssignment.getContextId(), updAnncSubject, updAnncBody, updatedAssignment.getOpenTime());
 				// don't need to re-save assignment b/c id already exists
 			}
 		}
 	}
 	
 	/**
 	 * will handle the business logic and updates required to determine if an event
 	 * needs to be added, updated, or deleted from the Schedule (Calendar) tool.
 	 * Compares the existing assignment (if not null) to the new assignment to
 	 * carry out any actions that are required for the relationship with the
 	 * Schedule tool.  Events are updated upon a change in the due date, title, or
 	 * group restrictions for the assignment.  Events are deleted if the assignment
 	 * is deleted, changed to draft status, or the due date is removed.  will also
 	 * add event when appropriate
 	 * @param originalAssignment - null if "updatedAssignment" is newly created
 	 * @param updatedAssignment
 	 */
 	private void handleDueDateEvent(Assignment2 originalAssignment, Assignment2 updatedAssignment) {
 		if (updatedAssignment == null) {
 			throw new IllegalArgumentException("Null updatedAssignment passed to saveDueDateEvent");
 		}
 		
 		if (updatedAssignment.getId() == null) {
 			throw new IllegalArgumentException("The updatedAssignment passed to " +
 					"saveDueDateEvent must have an id");
 		}
 		
 		if (!permissionLogic.isCurrentUserAbleToEditAssignments(updatedAssignment.getContextId())) {
 			throw new SecurityException("Current user is not allowed to edit assignments in context " + updatedAssignment.getContextId());
 		}
 		
 		String contextId = externalLogic.getCurrentContextId();
 		
 		// make the due date locale-aware
 		// use a date which is related to the current users locale
         DateFormat df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, bundleLogic.getLocale());
 		// create url to point back to this assignment to be included in the description
         String assignUrl = externalLogic.getAssignmentViewUrl(REDIRECT_ASSIGNMENT_VIEW_ID) + "/" + updatedAssignment.getId();
         
 		String eventTitle = "";
 		String eventDescription = "";
 		if (updatedAssignment.getDueDate() != null) {
 			eventTitle = bundleLogic.getFormattedMessage("assignment2.schedule_event_title",
 					new Object[] {updatedAssignment.getTitle()});
 			eventDescription = bundleLogic.getFormattedMessage("assignment2.schedule_event_description",
 					new Object[] {assignUrl, updatedAssignment.getTitle(), df.format(updatedAssignment.getDueDate())});
 		}
 		
 		if (originalAssignment == null) {
 			// this was a new assignment
 			// check to see if there will be an event added for the due date
 			if (updatedAssignment.getAddedToSchedule() && !updatedAssignment.isDraft() &&
 					updatedAssignment.getDueDate() != null) {
 				// add an event for the due date for this assignment
 				String eventId = calendarLogic.addDueDateToSchedule(updatedAssignment.getListOfAssociatedGroupReferences(), 
 						contextId, eventTitle, eventDescription, updatedAssignment.getDueDate(), updatedAssignment.getId());
 				updatedAssignment.setEventId(eventId);
 				dao.update(updatedAssignment);
 			}
 		} else if (updatedAssignment.isDraft()) {
 			if (updatedAssignment.getEventId() != null) {
 				calendarLogic.deleteDueDateEvent(updatedAssignment.getEventId(), contextId);
 				updatedAssignment.setEventId(null);
 				dao.update(updatedAssignment);
 			}
 		} else if (originalAssignment.getEventId() == null && updatedAssignment.getAddedToSchedule()) {
 			// this is a new event
 			String eventIdId = calendarLogic.addDueDateToSchedule(updatedAssignment.getListOfAssociatedGroupReferences(),
 					contextId, eventTitle, eventDescription, updatedAssignment.getDueDate(), updatedAssignment.getId());
 			updatedAssignment.setEventId(eventIdId);
 			dao.update(updatedAssignment);
 		} else if (originalAssignment.getEventId() != null && !updatedAssignment.getAddedToSchedule()) {
 			// we must remove the original event
 			calendarLogic.deleteDueDateEvent(originalAssignment.getEventId(), contextId);
 			updatedAssignment.setEventId(null);
 			dao.update(updatedAssignment);
 		} else if (updatedAssignment.getAddedToSchedule()){
 			// if title, due date, or group restrictions were updated, we need to update the event
 			Date oldDueDate = originalAssignment.getDueDate();
 			Date newDueDate = updatedAssignment.getDueDate();
 			
 			if (oldDueDate != null && newDueDate == null) {
 				// we need to remove this event because no longer has a due date
 				calendarLogic.deleteDueDateEvent(originalAssignment.getEventId(), contextId);
 				updatedAssignment.setEventId(null);
 				dao.update(updatedAssignment);
 			
 			} else if (!originalAssignment.getTitle().equals(updatedAssignment.getTitle()) ||
 					(oldDueDate.after(newDueDate) || oldDueDate.before(newDueDate)) ||
 					!originalAssignment.getListOfAssociatedGroupReferences().equals(updatedAssignment.getListOfAssociatedGroupReferences())) {
 				// otherwise, we update only if there is a change in the assignment title, due date,
 				// or group restrictions
 				calendarLogic.updateDueDateEvent(updatedAssignment.getEventId(), 
 						updatedAssignment.getListOfAssociatedGroupReferences(),
 						contextId, eventTitle, eventDescription, updatedAssignment.getDueDate(), 
 						updatedAssignment.getId());
 				// don't need to re-save assignment b/c id already exists
 			}
 		}
 	}
 }
