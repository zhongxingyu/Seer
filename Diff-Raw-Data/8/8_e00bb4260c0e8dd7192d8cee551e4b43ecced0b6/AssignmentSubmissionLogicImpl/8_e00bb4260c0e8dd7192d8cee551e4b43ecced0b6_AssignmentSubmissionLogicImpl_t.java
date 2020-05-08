 /**********************************************************************************
  * $URL:https://source.sakaiproject.org/contrib/assignment2/trunk/impl/logic/src/java/org/sakaiproject/assignment2/logic/impl/AssignmentSubmissionLogicImpl.java $
  * $Id:AssignmentSubmissionLogicImpl.java 48274 2008-04-23 20:07:00Z wagnermr@iupui.edu $
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
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.hibernate.StaleObjectStateException;
 import org.sakaiproject.assignment2.dao.AssignmentDao;
 import org.sakaiproject.assignment2.exception.AssignmentNotFoundException;
 import org.sakaiproject.assignment2.exception.StaleObjectModificationException;
 import org.sakaiproject.assignment2.exception.SubmissionNotFoundException;
 import org.sakaiproject.assignment2.exception.VersionNotFoundException;
 import org.sakaiproject.assignment2.logic.AssignmentLogic;
 import org.sakaiproject.assignment2.logic.AssignmentPermissionLogic;
 import org.sakaiproject.assignment2.logic.AssignmentSubmissionLogic;
 import org.sakaiproject.assignment2.logic.ExternalGradebookLogic;
 import org.sakaiproject.assignment2.logic.ExternalLogic;
 import org.sakaiproject.assignment2.logic.utils.ComparatorsUtils;
 import org.sakaiproject.assignment2.model.Assignment2;
 import org.sakaiproject.assignment2.model.AssignmentSubmission;
 import org.sakaiproject.assignment2.model.AssignmentSubmissionVersion;
 import org.sakaiproject.assignment2.model.FeedbackAttachment;
 import org.sakaiproject.assignment2.model.FeedbackVersion;
 import org.sakaiproject.assignment2.model.SubmissionAttachment;
 import org.sakaiproject.assignment2.model.SubmissionAttachmentBase;
 import org.sakaiproject.assignment2.model.constants.AssignmentConstants;
 import org.springframework.orm.hibernate3.HibernateOptimisticLockingFailureException;
 
 /**
  * This is the interface for interaction with the AssignmentSubmission object
  * 
  * @author <a href="mailto:wagnermr@iupui.edu">michelle wagner</a>
  */
 public class AssignmentSubmissionLogicImpl implements AssignmentSubmissionLogic{
 	
 	private static Log log = LogFactory.getLog(AssignmentSubmissionLogicImpl.class);
 	
 	private ExternalLogic externalLogic;
 	public void setExternalLogic(ExternalLogic externalLogic) {
 		this.externalLogic = externalLogic;
 	}
 
 	private AssignmentDao dao;
 	public void setDao(AssignmentDao dao) {
 		this.dao = dao;
 	}
 
 	private ExternalGradebookLogic gradebookLogic;
 	public void setExternalGradebookLogic(ExternalGradebookLogic gradebookLogic) {
 		this.gradebookLogic = gradebookLogic;
 	}
 
 	private AssignmentPermissionLogic permissionLogic;
 	public void setPermissionLogic(AssignmentPermissionLogic permissionLogic) {
 		this.permissionLogic = permissionLogic;
 	}
 	
 	private AssignmentLogic assignmentLogic;
 	public void setAssignmentLogic(AssignmentLogic assignmentLogic) {
 		this.assignmentLogic = assignmentLogic;
 	}
 
 	public void init(){
 		if (log.isDebugEnabled()) log.debug("init");
 	}
 	
 	public AssignmentSubmission getAssignmentSubmissionById(Long submissionId){
 		if (submissionId == null) {
 			throw new IllegalArgumentException("Null submissionId passed to getAssignmentSubmissionById");
 		}
 
 		AssignmentSubmission submission =  (AssignmentSubmission) dao.findById(AssignmentSubmission.class, submissionId);
 		if (submission == null) {
 			throw new SubmissionNotFoundException("No submission found with id: " + submissionId);
 		}
 		
 		String currentUserId = externalLogic.getCurrentUserId();
 
 		// if the submission rec exists, we need to grab the most current version
 
 		Assignment2 assignment = submission.getAssignment();
 
 		if (!permissionLogic.isUserAbleToViewStudentSubmissionForAssignment(submission.getUserId(), assignment.getId())) {
 			throw new SecurityException("user" + currentUserId + " attempted to view submission with id " + submissionId + " but is not authorized");
 		}
 
 		AssignmentSubmissionVersion currentVersion = dao.getCurrentSubmissionVersionWithAttachments(submission);
 
 		if (currentVersion != null) {
 			filterOutRestrictedVersionInfo(currentVersion, currentUserId);
 		}
 
 		submission.setCurrentSubmissionVersion(currentVersion);
 
 		return submission;
 	}
 	
 	public AssignmentSubmissionVersion getSubmissionVersionById(Long submissionVersionId) {
 		if (submissionVersionId == null) {
 			throw new IllegalArgumentException("null submissionVersionId passed to getSubmissionVersionById");
 		}
 		
 		AssignmentSubmissionVersion version = dao.getAssignmentSubmissionVersionByIdWithAttachments(submissionVersionId);
 		
 		if (version == null) {
 			throw new VersionNotFoundException("No AssignmentSubmissionVersion exists with id: " + submissionVersionId);
 		}
 		
 		String currentUserId = externalLogic.getCurrentUserId();
 		
 		if (version != null) {		
 			AssignmentSubmission submission = version.getAssignmentSubmission();
 			Assignment2 assignment = submission.getAssignment();
 			
 			// ensure that the current user is authorized to view this user for this assignment
 			if (!permissionLogic.isUserAbleToViewStudentSubmissionForAssignment(submission.getUserId(), assignment.getId())) {
 				throw new SecurityException("User " + currentUserId + " attempted to access the version " + 
 						submissionVersionId + " for student " + submission.getUserId() + " without authorization");
 			}
 			
 			filterOutRestrictedVersionInfo(version, currentUserId);
 		}
 		
 		return version;
 	}
 	
 	public AssignmentSubmission getCurrentSubmissionByAssignmentIdAndStudentId(Long assignmentId, String studentId) {
 		if (assignmentId == null || studentId == null) {
 			throw new IllegalArgumentException("Null assignmentId or userId passed to getCurrentSubmissionByAssignmentAndUser");
 		}
 
 		String currentUserId = externalLogic.getCurrentUserId();
 		
 		Assignment2 assignment = dao.getAssignmentByIdWithGroups(assignmentId);
 		
 		if (assignment == null) {
 			throw new AssignmentNotFoundException("No assignment found with id: " + assignmentId);
 		}
 
 		if (!permissionLogic.isUserAbleToViewStudentSubmissionForAssignment(studentId, assignment.getId())) {
 			throw new SecurityException("Current user " + currentUserId + " is not allowed to view submission for " + studentId + " for assignment " + assignment.getId());
 		}
 
 		AssignmentSubmission submission = dao.getSubmissionWithVersionHistoryForStudentAndAssignment(studentId, assignment);
 
 		if (submission == null) {
 			// return an "empty" submission
 			submission = new AssignmentSubmission(assignment, studentId);
 		} else {
 			
 			filterOutRestrictedInfo(submission, currentUserId, true);
 		} 
 
 		return submission;
 	}
 	
 	public void saveStudentSubmission(String userId, Assignment2 assignment, boolean draft, 
 			String submittedText, Set<SubmissionAttachment> subAttachSet) {
 		if (userId == null || assignment == null) {
 			throw new IllegalArgumentException("null userId, or assignment passed to saveAssignmentSubmission");
 		}
 		
 		if (assignment.getId() == null) {
 			throw new IllegalArgumentException("assignment without an id passed to saveStudentSubmission");
 		}
 		
 		Date currentTime = new Date();
 		String currentUserId = externalLogic.getCurrentUserId();
 		String contextId = externalLogic.getCurrentContextId();
 		
 		if (!currentUserId.equals(userId)) {
 			throw new SecurityException("User " + currentUserId + " attempted to save a submission for " +
 					userId + ". You may only make a submission for yourself!");
 		}
 		
 		if (!permissionLogic.isUserAbleToMakeSubmissionForAssignment(contextId, assignment)) {
 			log.warn("User " + currentUserId + " attempted to make a submission " +
 					"without authorization for assignment " + assignment.getId());
 			throw new SecurityException("User " + currentUserId + " attempted to make a submission " +
 					"without authorization for assignment " + assignment.getId());
 		}
 		
 		if (!submissionIsOpenForStudentForAssignment(currentUserId, assignment.getId())) {
 			log.warn("User " + currentUserId + " attempted to make a submission " +
 					"but submission for this user for assignment " + assignment.getId() + " is closed.");
 			throw new SecurityException("User " + currentUserId + " attempted to make a submission " +
 					"for closed assignment " + assignment.getId());
 		}
 		
 		// if there is no current version or the most recent version was submitted, we will need
 		// to create a new version. If the current version is draft, we will continue to update
 		// this version until it is submitted
 		
 		// retrieve the latest submission for this user and assignment
 		AssignmentSubmission submission = getAssignmentSubmissionForStudentAndAssignment(assignment, userId);
 		AssignmentSubmissionVersion version = null;
 
 		boolean isAnUpdate = false;
 		
 		if (submission == null) {
 			// there is no submission for this user yet
 			submission = new AssignmentSubmission(assignment, userId);
 			submission.setCreatedBy(currentUserId);
 			submission.setCreatedDate(currentTime);
 			submission.setModifiedBy(currentUserId);
 			submission.setModifiedDate(currentTime);
 			
 			// we need to create a new version for this submission
 			version = new AssignmentSubmissionVersion();
 		} else {
 			// the submission exists, so we need to check the current version
 			version = dao.getCurrentSubmissionVersionWithAttachments(submission);
 			
 			// if the current version hasn't been submitted yet, we will
 			// update that one. otherwise, create a new version
 			// note: the submittedVersionNumber = 0 is reserved for feedback-only
 			// versions. if the current version is number 0, we need to 
 			// create a new version
 			if (version != null && version.getSubmittedDate() == null && version.getSubmittedVersionNumber() != 0) {
 				
 				isAnUpdate = true;
 				
 			} else {
 				// we need to create a new version for this submission
 				version = new AssignmentSubmissionVersion();
 			}
 		}
 
 		// now let's set the new data
 		version.setAssignmentSubmission(submission);
 		version.setDraft(draft);
 		version.setSubmittedText(submittedText);
 
 		if (!isAnUpdate) {
 			version.setCreatedBy(currentUserId);
 			version.setCreatedDate(currentTime);
 			
 			// set the version number for this submission - if this submission
 			// doesn't exist yet, set the version to 1 --> 0 is reserved for feedback before submission
 			if (submission.getId() == null) {
 				version.setSubmittedVersionNumber(1);
 			} else {
 				version.setSubmittedVersionNumber(dao.getHighestSubmittedVersionNumber(submission) + 1);
 			}
 		}
 		
 		version.setModifiedBy(currentUserId);
 		version.setModifiedDate(currentTime);
 
 		if (!version.isDraft()) {
 			version.setSubmittedDate(currentTime);
 			
 			// if this isn't a draft, set the annotated text to be the submitted text
 			// to allow instructor to provide inline comments for submitted text
 			version.setAnnotatedText(submittedText);
 			// mark this submission as completed
 			submission.setCompleted(true);
 			submission.setModifiedBy(currentUserId);
 			submission.setModifiedDate(currentTime);
 		}
 
 		// identify any attachments that were deleted or need to be created
 		// - we don't update attachments
 		Set<SubmissionAttachmentBase> attachToDelete = identifyAttachmentsToDelete(version.getSubmissionAttachSet(), subAttachSet);
 		Set<SubmissionAttachmentBase> attachToCreate = identifyAttachmentsToCreate(subAttachSet);
 
 		// make sure the version was populated on the FeedbackAttachments
 		populateVersionForAttachmentSet(attachToCreate, version);
 		populateVersionForAttachmentSet(attachToDelete, version);
 
 		try {
 
 			Set<AssignmentSubmissionVersion> versionSet = new HashSet<AssignmentSubmissionVersion>();
 			versionSet.add(version);
 
 			Set<AssignmentSubmission> submissionSet = new HashSet<AssignmentSubmission>();
 			submissionSet.add(submission);
 			
 			if (attachToCreate == null) {
 				attachToCreate = new HashSet<SubmissionAttachmentBase>();
 			}
 
 			dao.saveMixedSet(new Set[] {submissionSet, versionSet, attachToCreate});
 			if (log.isDebugEnabled()) log.debug("Updated/Added student submission version " + 
 					version.getId() + " for user " + submission.getUserId() + " for assignment " + 
 					submission.getAssignment().getTitle()+ " ID: " + submission.getAssignment().getId());
 			if (log.isDebugEnabled()) log.debug("New submission attachments created: " + attachToCreate.size());
 
 			if (attachToDelete != null && !attachToDelete.isEmpty()) {
 				dao.deleteSet(attachToDelete);
 				if (log.isDebugEnabled()) log.debug("Removed " + attachToDelete.size() + 
 						"sub attachments deleted for updated version " + 
 						version.getId() + " by user " + currentUserId);
 			}
 		} catch (HibernateOptimisticLockingFailureException holfe) {
 			if(log.isInfoEnabled()) log.info("An optimistic locking failure occurred while attempting to update submission version" + version.getId());
 			throw new StaleObjectModificationException("An optimistic locking failure occurred while attempting to update submission version" + version.getId(), holfe);
 		} catch (StaleObjectStateException sose) {
 			if(log.isInfoEnabled()) log.info("An optimistic locking failure occurred while attempting to update submission version" + version.getId());
 			throw new StaleObjectModificationException("An optimistic locking failure occurred while attempting to update submission version" + version.getId(), sose);
 		}
 	}
 
 	public void saveInstructorFeedback(Long versionId, String studentId, Assignment2 assignment, 
 			Integer numSubmissionsAllowed, Date resubmitCloseDate, String annotatedText, 
 			String feedbackNotes, Date releasedDate, Set<FeedbackAttachment> feedbackAttachSet) {
 		
 		if (studentId == null || assignment == null) {
 			throw new IllegalArgumentException("Null studentId or assignment passed" +
 					"to saveInstructorFeedback");
 		}
 		
 		Date currentTime = new Date();
 		String currentUserId = externalLogic.getCurrentUserId();
 		
 		if (!permissionLogic.isUserAbleToProvideFeedbackForStudentForAssignment(studentId, assignment)) {
 			throw new SecurityException("User " + currentUserId + 
 					" attempted to submit feedback for student " + studentId + " without authorization");
 		}
 		
 		// let's retrieve the student's submission
 		AssignmentSubmission submission = getAssignmentSubmissionForStudentAndAssignment(assignment, studentId);
 		
 		if (submission != null && versionId == null) {
 			// check to see if any versions exist - if they do, we have an error
 			List<AssignmentSubmissionVersion> existingVersions = getVersionHistoryForSubmission(submission);
 			if (existingVersions != null && !existingVersions.isEmpty()) {
 				throw new IllegalArgumentException("Null versionId passed to saveInstructorFeedback " +
 					"even though at least one submission version exists for this student");
 			}
 		}
 		
 		if (submission == null) {
 			submission = new AssignmentSubmission(assignment, studentId);
 			submission.setCreatedBy(currentUserId);
 			submission.setCreatedDate(currentTime);
 		}
 		
 		AssignmentSubmissionVersion version = null;
 		boolean newVersion = false;
 		
 		// let's try to retrieve the version to update
 		if (versionId != null) {
 			version = dao.getAssignmentSubmissionVersionByIdWithAttachments(versionId);
 			if (version != null) {
 				// double check that this version matches the student and assignment
 				if (!(version.getAssignmentSubmission().getUserId().equals(studentId) &&
 						version.getAssignmentSubmission().getAssignment().getId().equals(assignment.getId()))) {
 					throw new IllegalArgumentException("The versionId passed is not" +
 							" associated with the given student and assignment");
 				}
 				
 			} else {
 				throw new IllegalArgumentException("No version exists with the given versionId: " + versionId);
 			}
 		} else {
 			// if null, we should be creating a new version b/c the instructor
 			// wants to provide feedback but the student has not made a submission
 			version = new AssignmentSubmissionVersion();
 			newVersion = true;
 		}
 		
 		if (newVersion) {
 			version.setCreatedBy(currentUserId);
 			version.setCreatedDate(currentTime);
 			version.setDraft(false);
 			// the only time an instructor can create a new version is if no
 			// submitted version exists. in this case, we always set the
 			// submittedVersionNumber to 0 to differentiate it
 			version.setSubmittedVersionNumber(0);
 		}
 		
 		submission.setResubmitCloseDate(resubmitCloseDate);
 		submission.setNumSubmissionsAllowed(numSubmissionsAllowed);
 		submission.setModifiedBy(currentUserId);
 		submission.setModifiedDate(currentTime);
 		
 		version.setAssignmentSubmission(submission);
 		version.setAnnotatedText(annotatedText);
 		version.setFeedbackNotes(feedbackNotes);
 		version.setFeedbackReleasedDate(releasedDate);
 		version.setLastFeedbackSubmittedBy(currentUserId);
 		version.setLastFeedbackDate(currentTime);
 		version.setModifiedBy(currentUserId);
 		version.setModifiedDate(currentTime);
 		
 		// identify any attachments that were deleted
 		Set<SubmissionAttachmentBase> attachToDelete = identifyAttachmentsToDelete(version.getFeedbackAttachSet(), feedbackAttachSet);
 		Set<SubmissionAttachmentBase> attachToCreate = identifyAttachmentsToCreate(feedbackAttachSet);
 		
 		// make sure the version was populated on the FeedbackAttachments
 		populateVersionForAttachmentSet(attachToCreate, version);
 		populateVersionForAttachmentSet(attachToDelete, version);
 
 		try {
 
 			Set<AssignmentSubmissionVersion> versionSet = new HashSet<AssignmentSubmissionVersion>();
 			versionSet.add(version);
 
 			Set<AssignmentSubmission> submissionSet = new HashSet<AssignmentSubmission>();
 			submissionSet.add(submission);
 			
 			if (attachToCreate == null) {
 				attachToCreate = new HashSet<SubmissionAttachmentBase>();
 			}
 
 			dao.saveMixedSet(new Set[] {submissionSet, versionSet, attachToCreate});
 			if (log.isDebugEnabled()) log.debug("Updated/Added feedback for version " + 
 					version.getId() + " for user " + submission.getUserId() + " for assignment " + 
 					submission.getAssignment().getTitle()+ " ID: " + submission.getAssignment().getId());
 			if (log.isDebugEnabled()) log.debug("Created feedbackAttachments: " + attachToCreate.size());
 
 			if (attachToDelete != null && !attachToDelete.isEmpty()) {
 				dao.deleteSet(attachToDelete);
 				if (log.isDebugEnabled()) log.debug("Removed feedback attachments deleted for updated version " + version.getId() + " by user " + currentUserId);
 			}
 
 		} catch (HibernateOptimisticLockingFailureException holfe) {
 			if(log.isInfoEnabled()) log.info("An optimistic locking failure occurred while attempting to update submission version" + version.getId());
 			throw new StaleObjectModificationException("An optimistic locking failure occurred while attempting to update submission version" + version.getId(), holfe);
 		} catch (StaleObjectStateException sose) {
 			if(log.isInfoEnabled()) log.info("An optimistic locking failure occurred while attempting to update submission version" + version.getId());
 			throw new StaleObjectModificationException("An optimistic locking failure occurred while attempting to update submission version" + version.getId(), sose);
 		}
 	}
 	
 	public List<AssignmentSubmission> getViewableSubmissionsWithHistoryForAssignmentId(Long assignmentId) {
 		return getViewableSubmissions(assignmentId, true);
 	}
 	
 	public List<AssignmentSubmission> getViewableSubmissionsForAssignmentId(Long assignmentId) {
 		return getViewableSubmissions(assignmentId, false);
 	}
 	
 	private List<AssignmentSubmission> getViewableSubmissions(Long assignmentId, boolean includeVersionHistory) {
 		if (assignmentId == null) {
 			throw new IllegalArgumentException("null assignmentId passed to getViewableSubmissionsForAssignmentId");
 		}
 
 		List<AssignmentSubmission> viewableSubmissions = new ArrayList<AssignmentSubmission>();
 
 		Assignment2 assignment = (Assignment2)dao.findById(Assignment2.class, assignmentId);
 		if (assignment == null) {
 			throw new AssignmentNotFoundException("No assignment found with id: " + assignmentId);
 		}
 
 		// get a list of all the students that the current user may view for the given assignment
 		List<String> viewableStudents = permissionLogic.getViewableStudentsForUserForItem(assignment);
 
 		if (viewableStudents != null && !viewableStudents.isEmpty()) {
 			
 			// get the submissions for these students
 			Set<AssignmentSubmission> existingSubmissions;
 			
 			if (includeVersionHistory) {
 				existingSubmissions = dao.getSubmissionsWithVersionHistoryForStudentListAndAssignment(viewableStudents, assignment);
 			} else {
 				existingSubmissions = dao.getCurrentSubmissionsForStudentsForAssignment(viewableStudents, assignment);
 			}
 
 			Map<String, AssignmentSubmission> studentIdSubmissionMap = new HashMap<String, AssignmentSubmission>();
 			if (existingSubmissions != null) {
 				for (AssignmentSubmission submission : existingSubmissions) {
 					if (submission != null) {
 						studentIdSubmissionMap.put(submission.getUserId(), submission);
 					}
 				}
 			}
 
 			// now, iterate through the students and create empty AssignmentSubmission recs
 			// if no submission exists yet
 			for (String studentId : viewableStudents) {
 				if (studentId != null) {
 					AssignmentSubmission thisSubmission = 
 						(AssignmentSubmission)studentIdSubmissionMap.get(studentId);
 
 					if (thisSubmission == null) {
 						// no submission exists for this student yet, so just
 						// add an empty rec to the returned list
 						thisSubmission = new AssignmentSubmission(assignment, studentId);
 					} else {
 						// we need to filter restricted info from instructor
 						// if this is draft
 						filterOutRestrictedInfo(thisSubmission, externalLogic.getCurrentUserId(), false);
 					}
 
 					viewableSubmissions.add(thisSubmission);
 				}
 			}
 		}
 
 		return viewableSubmissions;
 	}
 	
 	public Map<Assignment2, Integer> getSubmissionStatusConstantForAssignments(List<Assignment2> assignments, String studentId) {
 		if (studentId == null) {
 			throw new IllegalArgumentException("Null studentId passed to setSubmissionStatusForAssignments");
 		}
 		
 		Map<Assignment2, Integer> assignToStatusMap = new HashMap<Assignment2, Integer>();
 		
 		if (assignments != null) {
 			// retrieve the associated submission recs with current version data populated
 			List<AssignmentSubmission> submissions = dao.getCurrentAssignmentSubmissionsForStudent(assignments, studentId);
 			Map<Long, AssignmentSubmission> assignmentIdToSubmissionMap = new HashMap<Long, AssignmentSubmission>();
 			if (submissions != null) {
 				for (AssignmentSubmission submission : submissions) {
 					if (submission != null) {
 						Assignment2 assign = submission.getAssignment();
 						if (assign != null) {
 							assignmentIdToSubmissionMap.put(assign.getId(), submission);
 						}
 					}
 				}
 			}
 			
 			for (Assignment2 assign : assignments) {
 				if (assign != null) {
 					AssignmentSubmission currSubmission = (AssignmentSubmission)assignmentIdToSubmissionMap.get(assign.getId());
 					AssignmentSubmissionVersion currVersion = currSubmission != null ? 
 							currSubmission.getCurrentSubmissionVersion() : null;
 					
 					Integer status = getSubmissionStatusConstantForCurrentVersion(currVersion, assign.getDueDate());
 					assignToStatusMap.put(assign, status);
 				}
 			}
 		}
 		
 		return assignToStatusMap;
 	}
 	
 	public Integer getSubmissionStatusConstantForCurrentVersion(AssignmentSubmissionVersion currentVersion,
 			Date dueDate) {
 		int status = AssignmentConstants.SUBMISSION_NOT_STARTED;
 		
 		if (currentVersion == null) {
 			status = AssignmentConstants.SUBMISSION_NOT_STARTED;
 		} else if (currentVersion.getId() == null) {
 			status = AssignmentConstants.SUBMISSION_NOT_STARTED;
 		} else if (currentVersion.isDraft()) {
 			status = AssignmentConstants.SUBMISSION_IN_PROGRESS;
 		} else if (currentVersion.getSubmittedDate() != null) {
 			if (dueDate != null && dueDate.before(currentVersion.getSubmittedDate())) {
 				status = AssignmentConstants.SUBMISSION_LATE;
 			} else {
 				status = AssignmentConstants.SUBMISSION_SUBMITTED;
 			}
 		}
 		
 		return status;
 	}
 	
 	private void populateVersionForAttachmentSet(Set<? extends SubmissionAttachmentBase> attachSet,
 			AssignmentSubmissionVersion version)
 	{
 		if (attachSet != null && !attachSet.isEmpty())
 			for (SubmissionAttachmentBase attach : attachSet)
 				if (attach != null)
 					attach.setSubmissionVersion(version);
 	}
 	
 	private Set<SubmissionAttachmentBase> identifyAttachmentsToDelete(
 			Set<? extends SubmissionAttachmentBase> existingAttachSet,
 			Set<? extends SubmissionAttachmentBase> updatedAttachSet)
 	{
 		Set<SubmissionAttachmentBase> attachToRemove = new HashSet<SubmissionAttachmentBase>();
 
 		if (existingAttachSet != null)
 			for (SubmissionAttachmentBase attach : existingAttachSet)
 				if (attach != null)
 					if (updatedAttachSet == null || !updatedAttachSet.contains(attach))
 						// we need to delete this attachment
 						attachToRemove.add(attach);
 
 		return attachToRemove;
 	}
 	
 	private Set<SubmissionAttachmentBase> identifyAttachmentsToCreate(
 			Set<? extends SubmissionAttachmentBase> updatedAttachSet)
 	{
 		Set<SubmissionAttachmentBase> attachToCreate = new HashSet<SubmissionAttachmentBase>();
 
 		if (updatedAttachSet != null)
 			for (SubmissionAttachmentBase attach : updatedAttachSet)
 				if (attach != null)
 					if (attach.getId() == null)
 						attachToCreate.add(attach);
 
 		return attachToCreate;
 	}
 	
 	public int getNumberOfRemainingSubmissionsForStudent(String studentId, Long assignmentId) {
 		if (studentId == null || assignmentId == null) {
 			throw new IllegalArgumentException("null parameter passed to studentAbleToSubmit");
 		} 
 
 		Assignment2 assignment = (Assignment2)dao.findById(Assignment2.class, assignmentId);
 		if (assignment == null) {
 			throw new AssignmentNotFoundException("No assignment exists with id " + assignmentId);
 		}
 
 		int numSubmissionsRemaining = 0;
 
 		if (assignment.isRequiresSubmission()) {
 
 			// retrieve the submission history for this student for this assignment
 			AssignmentSubmission submission = dao.getSubmissionWithVersionHistoryForStudentAndAssignment(studentId, assignment);
 			List<AssignmentSubmissionVersion> versionHistory = null;
 			if (submission != null) {
 				versionHistory = dao.getVersionHistoryForSubmission(submission);
 			}
 
 			// we need to determine if this is the first submission for the student
 			int currNumSubmissions = 0;
 			if (submission == null) {
 				currNumSubmissions = 0;
 			} else if (versionHistory == null) {
 				currNumSubmissions = 0;
 			} else {
 				// we need to look at the submission history to determine if there
 				// are any submission by the student (not drafts and not versions
 				// created by instructor feedback when no submission)
 				for (AssignmentSubmissionVersion version : versionHistory) {
 					if (version != null) {
 						if (version.getSubmittedDate() != null) {
 							currNumSubmissions++;
 						}
 					}
 				}
 			}
 
 			/* A student is allowed to submit if:
 		 	1) student has not made a submission yet and assignment is open -OR-
 		 	2) instructor has set resubmission settings on the submission level,
 		 		and the resubmission date has not passed and the limit on the num
 		 		resubmissions has not been reached -OR-
 		 	3) there are no submission-level settings but there are on the assignment level
 		 		the assignment is still open and the number submissions allowed on
 		 		the assignment level has not been reached
 			 */
 
 			boolean assignmentIsOpen = assignment.getOpenDate().before(new Date()) && 
 			(assignment.getAcceptUntilDate() == null ||
 					(assignment.getAcceptUntilDate() != null && 
 							assignment.getAcceptUntilDate().after(new Date())));
 			
 			int numAllowedOnAssignLevel = assignment.getNumSubmissionsAllowed();
 			Integer numAllowedOnSubLevel = submission != null ? submission.getNumSubmissionsAllowed() : null;
 			
 			boolean resubmitSettingsOnAssignLevel = numAllowedOnAssignLevel > 0;
 			boolean resubmitSettingsOnSubmissionLevel = numAllowedOnSubLevel != null;
 			
 
 			if (currNumSubmissions == 0 && assignmentIsOpen) {
 				numSubmissionsRemaining = determineNumSubmissionRemaining(numAllowedOnAssignLevel, 
 						numAllowedOnSubLevel, currNumSubmissions);
 			} else if (resubmitSettingsOnSubmissionLevel) {
 				// these setting override any settings on the assignment level
 				if (submission.getResubmitCloseDate() == null || 
 						submission.getResubmitCloseDate().after(new Date())) {
 						numSubmissionsRemaining = determineNumSubmissionRemaining(numAllowedOnAssignLevel, 
 								numAllowedOnSubLevel, currNumSubmissions);
 				}
 			} else if (resubmitSettingsOnAssignLevel) {
 				if (assignmentIsOpen) { 
 					numSubmissionsRemaining = determineNumSubmissionRemaining(numAllowedOnAssignLevel, 
 							numAllowedOnSubLevel, currNumSubmissions);
 				} 
 			}
 		}
 		
 		return numSubmissionsRemaining;
 	}
 	
 	public boolean submissionIsOpenForStudentForAssignment(String studentId, Long assignmentId) {
 		int numRemainingSubmissions = getNumberOfRemainingSubmissionsForStudent(studentId, assignmentId);
 		boolean isOpen = false;
 		if (numRemainingSubmissions == -1 || numRemainingSubmissions > 0) {
 			isOpen = true;
 		}
 		
 		return isOpen;
 	}
 	
 	/**
 	 * 
 	 * @param assignmentLevelNumAllowed
 	 * @param subLevelNumAllowed
 	 * @param numAlreadySubmitted
 	 * @return the number of submissions remaining based upon the submission level num
 	 * submissions allowed, assign level num submission allowed, and the number of
 	 * submissions that have already occurred.  does NOT account for the assignment
 	 * being open or resubmission deadlines having passed
 	 */
 	private int determineNumSubmissionRemaining(int assignmentLevelNumAllowed, Integer subLevelNumAllowed, int numAlreadySubmitted) {
 		int numRemaining = 0;
 		// first, check settings on the submission level. these override any other setting
 		if (subLevelNumAllowed != null) {
 			if (subLevelNumAllowed.equals(-1)) {
 				numRemaining = -1;
 			} else if (numAlreadySubmitted < subLevelNumAllowed ){
 				numRemaining = subLevelNumAllowed.intValue() - numAlreadySubmitted;
 			} else {
 				numRemaining = 0;
 			} 
 		} else {
 			// then check assignment level
 			if (assignmentLevelNumAllowed == -1) {
 				numRemaining = -1;
 			} else if (numAlreadySubmitted < assignmentLevelNumAllowed){
 				numRemaining = assignmentLevelNumAllowed - numAlreadySubmitted; 
 			} else {
 				numRemaining = 0;
 			}
 		}
 
 		return numRemaining;
 	}
 
 	public boolean isMostRecentVersionDraft(AssignmentSubmission submission) {
 		if (submission == null) {
 			throw new IllegalArgumentException("null submission passed to currentVersionIsDraft");
 		}
 
 		boolean currVersionIsDraft = false;
 		
 		// if the id is null, there is no current version
 		if (submission.getId() != null) {
 			AssignmentSubmissionVersion version = dao.getCurrentSubmissionVersionWithAttachments(submission);
 	
 			if (version != null && version.isDraft()) {
 				currVersionIsDraft = true;
 			}
 		}
 
 		return currVersionIsDraft;
 	}
 	
 	public void releaseAllFeedbackForAssignment(Long assignmentId) {
 		if (assignmentId == null) {
 			throw new IllegalArgumentException("null assignmentId passed to releaseAllFeedbackForAssignment");
 		}
 
 		String contextId = externalLogic.getCurrentContextId();
 		String currUserId = externalLogic.getCurrentUserId();
 		Date now = new Date();
 		
 		if (!gradebookLogic.isCurrentUserAbleToGrade(contextId)) {
 			throw new SecurityException("User attempted to release feedback for assignment " + assignmentId + " without authorization");
 		}
 		
 		Assignment2 assignment = (Assignment2) dao.findById(Assignment2.class, assignmentId);
 		if (assignment == null) {
 			throw new AssignmentNotFoundException("Assignment with id " + assignmentId + " does not exist");
 		}
 		
 		List<String> gradableStudents = permissionLogic.getGradableStudentsForUserForItem(assignment);
 		if (gradableStudents != null && !gradableStudents.isEmpty()) {
 			Set<AssignmentSubmission> submissionList = dao.getSubmissionsWithVersionHistoryForStudentListAndAssignment(
 					gradableStudents, assignment);
 			
 			if (submissionList != null && !submissionList.isEmpty()) {
 				
 				Set<AssignmentSubmissionVersion> versionsToUpdate = new HashSet<AssignmentSubmissionVersion>();
 				
 				for (AssignmentSubmission submission : submissionList) {
 					if (submission != null) {
 						if (submission.getSubmissionHistorySet() != null &&
 								!submission.getSubmissionHistorySet().isEmpty()) {
 							// we need to iterate through all of the versions and
 							// release them
 							for (AssignmentSubmissionVersion version : submission.getSubmissionHistorySet())
 							{
 								if (version != null) {
 									version.setFeedbackReleasedDate(now);
 									version.setModifiedBy(currUserId);
 									version.setModifiedDate(now);
 									
 									versionsToUpdate.add(version);
 								}
 							}
 						}
 					}
 				}
 				
 				try {
 					dao.saveMixedSet(new Set[] { versionsToUpdate });
 					if (log.isDebugEnabled()) log.debug("All versions for assignment " + assignmentId + " released by " + externalLogic.getCurrentUserId());
 				} catch (HibernateOptimisticLockingFailureException holfe) {
 					if(log.isInfoEnabled()) log.info("An optimistic locking failure occurred while attempting to update submission versions for assignment " + assignmentId);
 		            throw new StaleObjectModificationException("An optimistic locking " +
 		            		"failure occurred while attempting to update submission " +
 		            		"versions for assignment " + assignmentId, holfe);
 				} catch (StaleObjectStateException sose) {
 					if(log.isInfoEnabled()) log.info("An optimistic locking failure " +
 							"occurred while attempting to update submission versions for assignment " + assignmentId);
 					throw new StaleObjectModificationException("An optimistic locking " +
 							"failure occurred while attempting to update submission versions for assignment " + assignmentId, sose);
 				}
 			}
 		}
 	}
 	
 	public void releaseAllFeedbackForSubmission(Long submissionId) {
 		if (submissionId == null) {
 			throw new IllegalArgumentException("null submissionId passed to releaseAllFeedbackForSubmission");
 		}
 
 		String currUserId = externalLogic.getCurrentUserId();
 		Date now = new Date();
 		
 		AssignmentSubmission subWithHistory = dao.getSubmissionWithVersionHistoryById(submissionId);
 
 		if (subWithHistory == null) {
 			throw new SubmissionNotFoundException("No submission exists with id " + submissionId);
 		}
 
 		if (!permissionLogic.isUserAbleToProvideFeedbackForStudentForAssignment(subWithHistory.getUserId(), subWithHistory.getAssignment())) {
 			throw new SecurityException("User " + currUserId + " attempted to release feedback" +
 					" for student " + subWithHistory.getUserId() + " and assignment " + 
 					subWithHistory.getAssignment().getId() + "without authorization");
 		}
 
 		if (subWithHistory.getSubmissionHistorySet() != null &&
 				!subWithHistory.getSubmissionHistorySet().isEmpty()) {
 			// we need to iterate through all of the versions and
 			// release them
 			Set<AssignmentSubmissionVersion> updatedVersions = new HashSet<AssignmentSubmissionVersion>();
 			for (AssignmentSubmissionVersion version : subWithHistory.getSubmissionHistorySet()) {
 				if (version != null) {
 					version.setFeedbackReleasedDate(now);
 					version.setModifiedBy(currUserId);
 					version.setModifiedDate(now);
 					updatedVersions.add(version);
 				}
 			}
 			
 			try {
 				dao.saveMixedSet(new Set[] { updatedVersions });
 				if (log.isDebugEnabled()) log.debug("All submission versions for submission " + submissionId + " released by " + externalLogic.getCurrentUserId());
 			} catch (HibernateOptimisticLockingFailureException holfe) {
 				if(log.isInfoEnabled()) log.info("An optimistic locking failure occurred while attempting to update release all version for submission " + submissionId);
 	            
 				throw new StaleObjectModificationException("An optimistic locking " +
 						"failure occurred while attempting to update release all version for submission " + submissionId, holfe);
 			}
 		}
 	}
 	
 	public void releaseFeedbackForVersion(Long submissionVersionId) {
 		if (submissionVersionId == null) {
 			throw new IllegalArgumentException("Null submissionVersionId passed to releaseFeedbackForVersion");
 		}
 		
 		String currUserId = externalLogic.getCurrentUserId();
 		Date now = new Date();
 		
 		AssignmentSubmissionVersion version = (AssignmentSubmissionVersion)dao.findById(
 				AssignmentSubmissionVersion.class, submissionVersionId);
 		if (version == null) {
 			throw new VersionNotFoundException("No version " + submissionVersionId + " exists");
 		}
 		
 		AssignmentSubmission submission = version.getAssignmentSubmission();
 		
 		if (!permissionLogic.isUserAbleToProvideFeedbackForStudentForAssignment(submission.getUserId(), submission.getAssignment())) {
 			throw new SecurityException("User " + externalLogic.getCurrentUserId() + " attempted to release feedback" +
 					" for student " + submission.getUserId() + " and assignment " + 
 					submission.getAssignment().getId() + "without authorization");
 		}
 		
 		version.setFeedbackReleasedDate(now);
 		version.setModifiedBy(currUserId);
 		version.setModifiedDate(now);
 		
 		try {
 			dao.update(version);
 			if (log.isDebugEnabled()) log.debug("Version " + version.getId() + " released by " + externalLogic.getCurrentUserId());
 		} catch (HibernateOptimisticLockingFailureException holfe) {
 			if(log.isInfoEnabled()) log.info("An optimistic locking failure occurred while attempting to update submission version" + version.getId());
             throw new StaleObjectModificationException("An optimistic locking failure occurred " +
             		"while attempting to update submission version" + version.getId(), holfe);
 		}
 	}
 	
 	/**
 	 * when retrieving a submission and/or version, some fields may be restricted
 	 * for the curr user. If curr user is the submitter and feedback has not been
 	 * released, we do not want to return feedback. If curr user is not the submitter
 	 * and the version is draft, we do not want to return the submission text and attach.
 	 * this method will also evict the submission and version(s) from session since we do not want
 	 * to accidentally save or re-load these modified objects
 	 * @param submission 
 	 * @param currentUserId
 	 * @param includeHistory - true if the submissionHistorySet was populated and
 	 * needs to be filtered, as well
 	 */
 	private void filterOutRestrictedInfo(AssignmentSubmission submission, String currentUserId, boolean includeHistory) {
 		// if the current user is the submitter and feedback has not been 
 		// released, do not return the feedback info
 		// if the current user is not the submitter and a version is draft, 
 		// do not return any of the submission info
 		
 		// evict the submission from the session since we are making modifications
 		// that we don't want to be saved or re-loaded
 		dao.evictObject(submission);
 
 		// check the version history
 		if (includeHistory) {
 			if (submission.getSubmissionHistorySet() != null && !submission.getSubmissionHistorySet().isEmpty()) {
 				for (AssignmentSubmissionVersion version : submission.getSubmissionHistorySet()) {
 					filterOutRestrictedVersionInfo(version, currentUserId);
 				}
 			}
 		}
 		
 		// also check the currentVersion
 		if (submission.getCurrentSubmissionVersion() != null) {
 			filterOutRestrictedVersionInfo(submission.getCurrentSubmissionVersion(), currentUserId);
 		}
 	}
 	
 	/**
 	 * when retrieving a submission and/or version, some fields may be restricted
 	 * for the curr user. If curr user is the submitter and feedback has not been
 	 * released, we do not want to return feedback. If curr user is not the submitter
 	 * and the version is draft, we do not want to return the submission text and attach.
 	 * this method will also evict the version from session since we do not want
 	 * to accidentally save this modified object
 	 * @param version 
 	 * want to save the changes we are making
 	 * @param currentUserId
 	 */
 	private void filterOutRestrictedVersionInfo(AssignmentSubmissionVersion version, String currentUserId) {
 		if (version != null) {
 			
 			// since we may modify the versions before returning them to filter
 			// out restricted info, we don't want to return the persistent objects.
 			// we have no intention of saving the modified object
 			dao.evictObject(version);
 			
 			// if the current user is the submitter
 			if (version.getAssignmentSubmission().getUserId().equals(currentUserId)) {
 				if (version.getFeedbackReleasedDate() == null || version.getFeedbackReleasedDate().after(new Date())) {
 					// do not populate the feedback since not released 
 					version.setFeedbackAttachSet(new HashSet<FeedbackAttachment>());
 					version.setFeedbackNotes("");
 					version.setAnnotatedText("");
 					if (log.isDebugEnabled()) log.debug("Not populating feedback-specific info b/c curr user is submitter and feedback not released");
 				}
 			} else {
 				// do not populate submission info if still draft
 				if (version.isDraft()) {
 					version.setSubmittedText("");
 					version.setSubmissionAttachSet(new HashSet<SubmissionAttachment>());
 					if (log.isDebugEnabled()) log.debug("Not populating submission-specific info b/c draft status and current user is not submitter");
 				}
 			}
 		}
 	}
 	
 	public List<AssignmentSubmissionVersion> getVersionHistoryForSubmission(AssignmentSubmission submission) {
 		if (submission == null) {
 			throw new IllegalArgumentException("Null submission passed to getVersionHistoryForSubmission");
 		}
 		
 		List<AssignmentSubmissionVersion> filteredVersionHistory = new ArrayList<AssignmentSubmissionVersion>();
 		
 		if (!permissionLogic.isUserAbleToViewStudentSubmissionForAssignment(submission.getUserId(), submission.getAssignment().getId())) {
 			throw new SecurityException("User " + externalLogic.getCurrentUserId() +
 					" attempted to access version history for student " + submission.getUserId() +
 					" without authorization!");
 		}
 
 		// if id is null, this submission does not exist yet - will return empty
 		// version history
 		if (submission.getId() != null) {
 			String currentUserId = externalLogic.getCurrentUserId();
 
 			List<AssignmentSubmissionVersion> historySet = dao.getVersionHistoryForSubmission(submission);
 			if (historySet != null && !historySet.isEmpty()) {
 				for (AssignmentSubmissionVersion version : historySet) {
 					if (version != null) {					
 						filterOutRestrictedVersionInfo(version, currentUserId);
 						filteredVersionHistory.add(version);
 					}
 
 				}}
 		} else {
 			if (log.isDebugEnabled()) log.debug("Submission does not exist so no version history retrieved");
 		}
 		
 		Collections.sort(filteredVersionHistory, new ComparatorsUtils.VersionCreatedDateComparatorDesc());
 		
 		return filteredVersionHistory;
 	}
 	
 	private AssignmentSubmission getAssignmentSubmissionForStudentAndAssignment(Assignment2 assignment, String studentId) {
 		AssignmentSubmission submission = null;
 		
 		List<AssignmentSubmission> subList = dao.findByProperties(AssignmentSubmission.class, 
 				new String[] {"assignment", "userId"}, new Object[] {assignment, studentId});
 		
 		if (subList != null && subList.size() > 0) {
 			submission = (AssignmentSubmission) subList.get(0);
 		}
 		
 		return submission;
 	}
 
 	public FeedbackVersion getFeedbackByUserIdAndSubmittedDate(String userId, Date submittedDate)
 	{
 		AssignmentSubmissionVersion v = dao.getVersionByUserIdAndSubmittedDate(userId,
 				submittedDate);
 		return v;
 	}
 
 	public void updateFeedbackForVersion(FeedbackVersion feedback)
 	{
 		if (feedback == null)
 			throw new IllegalArgumentException("Feedback cannot be null.");
 		if (feedback.getId() == null)
 			throw new IllegalArgumentException("Cannot update feedback without ID.  Please create a submission version first.");
 
 		AssignmentSubmissionVersion asv = (AssignmentSubmissionVersion) dao.findById(
 				AssignmentSubmissionVersion.class, feedback.getId());
 
 		if (asv == null)
 			throw new IllegalArgumentException("Cannot find submission version with id = " + feedback.getId());
 
 		String currUserId = externalLogic.getCurrentUserId();
 		Date now = new Date();
 		
 		if (feedback.getAnnotatedText() != null)
 			asv.setAnnotatedText(feedback.getAnnotatedText());
 
 		if (feedback.getFeedbackAttachSet() != null)
 		{
 			for (FeedbackAttachment fa : feedback.getFeedbackAttachSet())
 				fa.setSubmissionVersion(asv);
 			dao.saveSet(feedback.getFeedbackAttachSet());
 			asv.setFeedbackAttachSet(feedback.getFeedbackAttachSet());
 		}
 
 		if (feedback.getFeedbackNotes() != null)
 			asv.setFeedbackNotes(feedback.getFeedbackNotes());
 
 		asv.setLastFeedbackDate(now);
 		asv.setLastFeedbackSubmittedBy(currUserId);
 		asv.setModifiedDate(now);
 		asv.setModifiedBy(currUserId);
 
 		dao.update(asv);
 	}
 	
 	public int getNumSubmittedVersions(String studentId, Long assignmentId) {
 		if (studentId == null || assignmentId == null) {
 			throw new IllegalArgumentException("Null studentId or assignmentId passed " +
 			"to getTotalNumSubmissionsForStudentForAssignment");
 		}
 
 		return dao.getNumSubmittedVersions(studentId, assignmentId);
 	}
 	
 	public int getNumStudentsWithASubmission(Assignment2 assignment, List<String> studentIdList) {
 		if (assignment == null) {
 			throw new IllegalArgumentException ("Null assignment passed to getNumStudentsWithASubmission");
 		}
 		
 		return dao.getNumStudentsWithASubmission(assignment, studentIdList);
 	}
 	
 	public void markFeedbackAsViewed(Long submissionId, List<Long> versionIdList) {
 		if (submissionId == null) {
 			throw new IllegalArgumentException("Null submissionId passed to markFeedbackAsViewed");
 		}
 		
 		if (versionIdList != null) {
 			// retrieve the submission and version history
 			AssignmentSubmission subWithHistory = dao.getSubmissionWithVersionHistoryById(submissionId);
 			if (subWithHistory == null) {
 				throw new SubmissionNotFoundException("No submission exists with id " + submissionId);
 			}
 			
 			String currentUserId = externalLogic.getCurrentUserId();
 			if (!currentUserId.equals(subWithHistory.getUserId())) {
 				throw new SecurityException("User " + currentUserId + " attempted to mark " +
 						"feedback as viewed for student " + subWithHistory.getUserId());
 			}
 			
 			if (subWithHistory.getSubmissionHistorySet() != null) {
 				Set<AssignmentSubmissionVersion> updatedVersions = new HashSet<AssignmentSubmissionVersion>();
 				Date now = new Date();
 				
 				for (AssignmentSubmissionVersion existingVersion : subWithHistory.getSubmissionHistorySet()) {
 					if (versionIdList.contains(existingVersion.getId())) {
 						// double check that this feedback has actually been released
 						if (existingVersion.getFeedbackReleasedDate() != null && 
 								existingVersion.getFeedbackReleasedDate().before(now)) {
 							// this version needs to be marked as viewed and updated
 							existingVersion.setFeedbackLastViewed(now);
 							existingVersion.setModifiedBy(currentUserId);
 							existingVersion.setModifiedDate(now);
 							
 							updatedVersions.add(existingVersion);
 						} else {
 							if (log.isDebugEnabled()) log.debug("Version " + existingVersion.getId() +
 									" was not marked as read b/c feedback has not been released yet");
 						}
 					}
 				}
 				
 				if (!updatedVersions.isEmpty()) {
 					dao.saveSet(updatedVersions);
 				}
 				
 				if (log.isDebugEnabled()) log.debug(updatedVersions.size() + 
 				" versions marked as feedback viewed");
 			}
 		}
 	}
 	
 	public void markAssignmentsAsCompleted(String studentId, Map<Long, Boolean> assignmentIdToCompletedMap) {
 		if (studentId == null) {
 			throw new IllegalArgumentException("Null studentId passed to markAssignmentAsCompleted");
 		}
 
 		String currentUserId = externalLogic.getCurrentUserId();
 		
 		if (!studentId.equals(currentUserId)) {
 			throw new SecurityException("User " + currentUserId + " attempted to mark assignments as " +
 					"complete for student " + studentId);
 		}
 		
 		if (assignmentIdToCompletedMap != null && !assignmentIdToCompletedMap.isEmpty()) {
 			// first, retrieve the Assignment2 objects associated with the list
 			Set<Assignment2> assignToMarkComplete = dao.getAssignmentsWithGroupsAndAttachmentsById(assignmentIdToCompletedMap.keySet());
 			if (assignToMarkComplete != null) {
 				// now, let's retrieve the submissions
 				List<AssignmentSubmission> submissions = dao.getCurrentAssignmentSubmissionsForStudent(assignToMarkComplete, studentId);
				Map<Long, AssignmentSubmission> assignmentIdToSubmissionMap = new HashMap<Long, AssignmentSubmission>();
 				if (submissions != null) {
 					for (AssignmentSubmission sub : submissions) {
						assignmentIdToSubmissionMap.put(sub.getAssignment().getId(), sub);
 					}
 				}
 				
 				// now, iterate through the assignments to see if any do not have
 				// a submission yet - we will need to create it
 				Set<AssignmentSubmission> submissionsToSave = new HashSet<AssignmentSubmission>();
 				for (Assignment2 assign : assignToMarkComplete) {
 					Boolean complete = assignmentIdToCompletedMap.get(assign.getId());
 					if (complete == null) {
 						throw new IllegalArgumentException("Null value for completed passed in map to markAssignmentsAsCompleted");
 					}
 					
					AssignmentSubmission submission = (AssignmentSubmission)assignmentIdToSubmissionMap.get(assign.getId());
 					if (submission == null) {
 						// we need to create this submission record
 						submission = new AssignmentSubmission(assign, studentId);
 						submission.setCreatedBy(currentUserId);
 						submission.setCreatedDate(new Date());
 					}
 					
 					submission.setCompleted(complete);
 					submission.setModifiedBy(currentUserId);
 					submission.setModifiedDate(new Date());
 					
 					submissionsToSave.add(submission);
 				}
 				
 				if (!submissionsToSave.isEmpty()) {
 					dao.saveSet(submissionsToSave);
 					if (log.isDebugEnabled()) log.debug(submissionsToSave.size() + " submissions updated through markAssignmentsAsCompleted");
 				}
 			}
 		}
 	}
 	
 	public List<AssignmentSubmission> getSubmissionsForCurrentUser() {
 		List<AssignmentSubmission> userSubmissions = new ArrayList<AssignmentSubmission>();
 		
 		String currentUserId = externalLogic.getCurrentUserId();
 		String currentContextId = externalLogic.getCurrentContextId();
 		
 		if (!permissionLogic.isCurrentUserAbleToSubmit(currentContextId)) {
 			throw new SecurityException("Attempt to retrieve submissions for a non-student user");
 		}
 
 		List<Assignment2> viewableAssignments = assignmentLogic.getViewableAssignments();
 
 		if (viewableAssignments != null) {
 			Set<AssignmentSubmission> existingSubmissions = dao.getSubmissionsForStudentWithVersionHistoryAndAttach(currentUserId, viewableAssignments);
 
 			// put these submissions into a map so we can determine which assignments don't
 			// have submissions yet
 			Map<Long, AssignmentSubmission> assignIdSubmissionMap = new HashMap<Long, AssignmentSubmission>();
 			if (existingSubmissions != null) {
 				for (AssignmentSubmission existingSub : existingSubmissions) {
 					assignIdSubmissionMap.put(existingSub.getAssignment().getId(), existingSub);
 				}
 			}
 			
 			// now, let's iterate through all of the viewable assignments. we may need to
 			// add filler recs for assignments with no submissions yet
 			for (Assignment2 assign : viewableAssignments) {
 				// try to get the existing submission
 				AssignmentSubmission existingSub = assignIdSubmissionMap.get(assign.getId());
 				if (existingSub == null) {
 					// we need to add an "empty" submission to the list as a placeholder
 					// for this assignment
 					AssignmentSubmission newSub = new AssignmentSubmission(assign, currentUserId);
 					userSubmissions.add(newSub);
 				} else {
 					filterOutRestrictedInfo(existingSub, currentUserId, true);
 					userSubmissions.add(existingSub);
 				}
 			}
 		}
 		
 		// sort by completed, then by assignment sortIndex
 		Collections.sort(userSubmissions, new ComparatorsUtils.SubmissionCompletedSortOrderComparator());
 		
 		return userSubmissions;
 	}
 	
 }
