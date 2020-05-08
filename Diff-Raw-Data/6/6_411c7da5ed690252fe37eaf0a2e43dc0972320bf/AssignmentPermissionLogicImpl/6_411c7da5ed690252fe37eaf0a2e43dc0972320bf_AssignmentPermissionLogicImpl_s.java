 /**********************************************************************************
  * $URL:https://source.sakaiproject.org/contrib/assignment2/trunk/impl/logic/src/java/org/sakaiproject/assignment2/logic/impl/AssignmentPermissionLogicImpl.java $
  * $Id:AssignmentPermissionLogicImpl.java 48274 2008-04-23 20:07:00Z wagnermr@iupui.edu $
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
 import java.util.Collection;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.sakaiproject.assignment2.dao.AssignmentDao;
 import org.sakaiproject.assignment2.exception.AssignmentNotFoundException;
 import org.sakaiproject.assignment2.exception.NoGradebookItemForGradedAssignmentException;
 import org.sakaiproject.assignment2.exception.SubmissionNotFoundException;
 import org.sakaiproject.assignment2.logic.AssignmentPermissionLogic;
 import org.sakaiproject.assignment2.logic.ExternalGradebookLogic;
 import org.sakaiproject.assignment2.logic.ExternalLogic;
 import org.sakaiproject.assignment2.model.Assignment2;
 import org.sakaiproject.assignment2.model.AssignmentGroup;
 import org.sakaiproject.assignment2.model.AssignmentSubmission;
 import org.sakaiproject.assignment2.model.constants.AssignmentConstants;
 import org.sakaiproject.site.api.Group;
 
 /**
  * This is the implementation for logic to answer common permissions questions
  * 
  * @author <a href="mailto:wagnermr@iupui.edu">michelle wagner</a>
  */
 public class AssignmentPermissionLogicImpl implements AssignmentPermissionLogic {
 
     private static Log log = LogFactory.getLog(AssignmentPermissionLogicImpl.class);
 
     public void init() {
     	if (log.isDebugEnabled()) log.debug("init");
     }
     
     private AssignmentDao dao;
     public void setDao(AssignmentDao dao) {
         this.dao = dao;
     }
     
 	private ExternalLogic externalLogic;
     public void setExternalLogic(ExternalLogic externalLogic) {
         this.externalLogic = externalLogic;
     }
     
     private ExternalGradebookLogic gradebookLogic;
     public void setExternalGradebookLogic(ExternalGradebookLogic gradebookLogic) {
         this.gradebookLogic = gradebookLogic;
     }
     
     public boolean isCurrentUserAbleToEditAssignments(String contextId) {
     	if (contextId == null) {
     		throw new IllegalArgumentException("null contextId passed to isCurrentUserAbleToEditAssignments");
     	}
     	return gradebookLogic.isCurrentUserAbleToEdit(contextId);
     }
     
     public boolean isUserAbleToViewStudentSubmissionForAssignment(String studentId, Long assignmentId) {
     	if (studentId == null || assignmentId == null) {
     		throw new IllegalArgumentException("Null studentId or assignmentId passed to isUserAbleToViewStudentSubmissionForAssignment");
     	}
     	
     	boolean viewable = false;
     	
     	Assignment2 assignment = (Assignment2)dao.findById(Assignment2.class, assignmentId);
     	if (assignment == null) {
     		throw new AssignmentNotFoundException("No assignment found with id: " + assignmentId + " found in isUserAbleToViewStudentSubmissionForAssignment");
     	}
     	
     	String currUserId = externalLogic.getCurrentUserId();
 
     	// double check to see if this is a removed assignment. if so, only
     	// the student may view it
     	if (assignment.isRemoved()) {
     	    if (currUserId.equals(studentId)) {
     	        viewable = true;
     	    }
     	} else if (currUserId.equals(studentId)) {
     	    viewable = true;
     	} else if (gradebookLogic.isCurrentUserAbleToGradeAll(assignment.getContextId())) {
     	    viewable = true;
     	} else {
     	    // if graded but assoc gb item no longer exists, treat as an ungraded item
     	    if (assignment.isGraded() && gradebookLogic.gradebookItemExists(assignment.getGradebookItemId())) {
     	        String function = gradebookLogic.getGradeViewPermissionForCurrentUserForStudentForItem(assignment.getContextId(), 
                         studentId, assignment.getGradebookItemId());
                 if (function != null && (function.equals(AssignmentConstants.GRADE) ||
                         function.equals(AssignmentConstants.VIEW))) {
                     viewable = true;
                 }
     	    } else {
     	        viewable = isUserAbleToViewSubmissionForUngradedAssignment(studentId, assignment);
     	    }
     	}
     	
     	return viewable;	
     }
     
     public boolean isUserAbleToProvideFeedbackForStudentForAssignment(String studentId, Assignment2 assignment) {
         if (studentId == null || assignment == null) {
             throw new IllegalArgumentException("null parameter passed to isUserAbleToProvideFeedbackForSubmission");
         }
 
         boolean allowed = false;
 
         if (assignment != null) {
             if (assignment.isGraded() && gradebookLogic.gradebookItemExists(assignment.getGradebookItemId())) {
                 allowed = gradebookLogic.isCurrentUserAbleToGradeStudentForItem(assignment.getContextId(), 
                         studentId, assignment.getGradebookItemId());
             } else {
                 allowed = isUserAbleToViewSubmissionForUngradedAssignment(studentId, assignment);
             }
         }
 
         return allowed;
     }
     
     public boolean isUserAbleToProvideFeedbackForSubmission(Long submissionId) {
     	if (submissionId == null) {
     		throw new IllegalArgumentException("Null submissionId passed to isUserAbleToProvideFeedbackForSubmission");
     	}
     	
     	AssignmentSubmission submission = (AssignmentSubmission)dao.findById(AssignmentSubmission.class, submissionId);
     	if (submission == null) {
     		throw new SubmissionNotFoundException("No submission exists with id: " + submissionId);
     	}
     	
     	return isUserAbleToProvideFeedbackForStudentForAssignment(submission.getUserId(), submission.getAssignment());
     }
     
     public boolean isUserAbleToViewSubmissionForUngradedAssignment(String studentId, Assignment2 assignment) {
     	if (studentId == null || assignment == null) {
     		throw new IllegalArgumentException("null studentId or assignment passed to isUserAbleToViewSubmissionForUngradedAssignment");
     	}
     	
     	boolean viewable = false;
     	if (gradebookLogic.isCurrentUserAbleToGradeAll(assignment.getContextId())) {
     		viewable = true;
     	} else if (gradebookLogic.isCurrentUserAbleToGrade(assignment.getContextId())) {
     		List<String> currentUserMemberships = externalLogic.getUserMembershipGroupIdList(externalLogic.getCurrentUserId(), assignment.getContextId());
     		List<String> studentMemberships = externalLogic.getUserMembershipGroupIdList(studentId, assignment.getContextId());
     		if (userMembershipsOverlap(currentUserMemberships, studentMemberships)) {
     			viewable = true;
     		}
     	}
     	
     	return viewable;
     }
     
     public List<Assignment2> filterViewableAssignments(String contextId, Collection<Assignment2> assignmentList) {
         if (contextId == null) {
             throw new IllegalArgumentException("Null contextId passed to filterViewableAssignments");
         }
         
         List<Assignment2> filteredAssignments = new ArrayList<Assignment2>();
         if (assignmentList != null && !assignmentList.isEmpty()) {
       
             for (Assignment2 assign : assignmentList) {               
                 // first, make sure all of these assignments belong to the given contextId
                 if (!assign.getContextId().equals(contextId)) {
                     throw new IllegalArgumentException("User attempted to filterViewableAssignments " +
                             "but assignmentList contained assignment with an associated contextId that " +
                             "did not match the contextIdParameter. contextId: " + contextId + 
                             " assignment.getContextId:" + assign.getContextId());
                 }      
             }
             
             String currUserId = externalLogic.getCurrentUserId();
             
             boolean userMayGradeAll = gradebookLogic.isCurrentUserAbleToGradeAll(contextId);
             boolean userMayEditAssigns = gradebookLogic.isCurrentUserAbleToEdit(contextId);
             
             if (userMayGradeAll || userMayEditAssigns) {
                 // only students may view removed assignments
                 // if a user has grade all, he/she can view all non-draft assigns.
                 // users with edit perm can view all assigns regardless of draft status.     
                 for (Assignment2 assign : assignmentList) {
                     if (!assign.isRemoved() && (userMayEditAssigns || !assign.isDraft())) {
                         filteredAssignments.add(assign);
                     }
                 }
                 
             } else {
                 // check for a student or grader
                 boolean userIsStudent = gradebookLogic.isCurrentUserAStudentInGb(contextId);
                 boolean userMayGrade = gradebookLogic.isCurrentUserAbleToGrade(contextId);
                 List<String> userMemberships = externalLogic.getUserMembershipGroupIdList(currUserId, contextId);
                 
                 for (Assignment2 assign: assignmentList) {
                     // you must have edit perm to see draft assigns, so if we have gotten to this
                     // point, don't allow drafts
                     if (!assign.isDraft()) {
                         boolean assignIsOpen = assign.getOpenDate().before(new Date());
 
                         if (assign.isRemoved()) {
                             // student may view removed assignment if they have made a submission
                             if (userIsStudent) {
                                 int numSubmissions = dao.getNumSubmittedVersions(currUserId, assign.getId());
                                 if (numSubmissions > 0) {
                                     filteredAssignments.add(assign);
                                 }
                             }
                         } else if ((userIsStudent && assignIsOpen) || userMayGrade) {
                             // we need to check group restrictions
                             boolean groupSettingsValid = false;
                             if (assign.getAssignmentGroupSet() != null && !assign.getAssignmentGroupSet().isEmpty()) {
                                 groupSettingsValid = isUserAMemberOfARestrictedGroup(userMemberships, assign.getAssignmentGroupSet());
                             } else {
                                 groupSettingsValid = true;
                             }
 
                             if (groupSettingsValid) {
                                 // TA may view all ungraded assignments if they pass the group restrictions. 
                                 // students may view all assigns regardless of grading since they pass the group restrictions
                                 if (!assign.isGraded() || userIsStudent) {
                                     filteredAssignments.add(assign);
                                 } else {
                                     // check to see if can view assign in gradebook
                                     // if gradebook item no longer exists, we treat it as ungraded
                                     if (!gradebookLogic.gradebookItemExists(assign.getGradebookItemId())) {
                                         // flag this scenario by setting the gradebookItemId to null
                                         assign.setGradebookItemId(null);
                                         filteredAssignments.add(assign);
                                     } else {
                                         if (gradebookLogic.isCurrentUserAbleToViewGradebookItem(contextId, assign.getGradebookItemId())) {
                                             filteredAssignments.add(assign);
                                         }
                                     }
                                 }
                             }
                         }
                     }
                 }
             }
         }
         
         return filteredAssignments;
     }
     
     public boolean isUserAMemberOfARestrictedGroup(Collection<String> groupMembershipIds, Collection<AssignmentGroup> assignmentGroupSet) {    	
     	if (assignmentGroupSet != null && !assignmentGroupSet.isEmpty()) {
         	if (groupMembershipIds != null) {
 	        	for (AssignmentGroup aGroup : assignmentGroupSet) {
 	        		if (aGroup != null) {
 	        			if (groupMembershipIds.contains(aGroup.getGroupId())) {
 	        				return true;
 	        			}
 	        		}
 	        	}
         	}
     	}
     	
     	return false;
     }
     
 	public boolean isUserAbleToAccessInstructorView(String contextId) {
 		if (contextId == null) {
 			throw new IllegalArgumentException("null contextId passed to isUserAbleToAccessInstructorView");
 		}
 		
 		boolean instructorView = false;
 		
 		if (gradebookLogic.isCurrentUserAbleToEdit(contextId) ||
 				gradebookLogic.isCurrentUserAbleToGrade(contextId)) {
 			instructorView = true;
 		}
 		
 		return instructorView;
 	}
 	
 	public boolean isUserAbleToMakeSubmissionForAssignment(Assignment2 assignment) {
 		if (assignment == null) {
 			throw new IllegalArgumentException("null assignment passed to isUserAbleToMakeSubmission");
 		}
 		
 		if (assignment.getId() == null || assignment.getContextId() == null) {
 			throw new IllegalArgumentException("null data in not-null fields for " +
 					"assignment passed to isUserAbleToMakeSubmission: assignment.getId: " + 
 					assignment.getId() + " contextId: " + assignment.getContextId());
 		} 
 		
 		// TODO - how do we handle certain roles that shouldn't be able to submit?
 		// ie guests? they don't have a section flag on their role
 		boolean userAbleToSubmit = false;
 	
 		if (!assignment.isGraded()) {
 			// TODO right now, we don't have any checks for ungraded assignments...
 			userAbleToSubmit = true;
 		} else {
 			// we must obey the gradebook permissions
 			if (gradebookLogic.isCurrentUserAStudentInGb(assignment.getContextId())) {
 				userAbleToSubmit = true;
 			}
 		}
 		
 		// check to make sure any group restrictions have been upheld
 		if (userAbleToSubmit) {
 		    String currUserId = externalLogic.getCurrentUserId();
 			List<AssignmentGroup> assignGroupRestrictions = 
 				dao.findByProperties(AssignmentGroup.class, new String[] {"assignment"}, new Object[] {assignment});
 			
 			List<String> groupMembershipIds = externalLogic.getUserMembershipGroupIdList(currUserId, assignment.getContextId());
 			if (assignGroupRestrictions != null && !assignGroupRestrictions.isEmpty()) {
 				if (!isUserAMemberOfARestrictedGroup(groupMembershipIds, assignGroupRestrictions)) {
 					userAbleToSubmit = false;
 				}
 			}
 		}
 		
 		return userAbleToSubmit;
 	}
 	
 	public Map<Assignment2, List<String>> getViewableStudentsForUserForAssignments(String userId, String contextId, List<Assignment2> assignmentList) {
 	    if (userId == null) {
 	        throw new IllegalArgumentException("Null userId passed to getViewableStudentsForUserForAssignments");
 	    }
 	    
 	    if (contextId == null) {
 	        throw new IllegalArgumentException("Null contextId passed to getViewableStudentsForUserForAssignments");
 	    }
 	    
 	    Map<Assignment2, List<String>> assignToViewableStudentsMap = new HashMap<Assignment2, List<String>>();
 	    if (assignmentList != null) {
 	        assignToViewableStudentsMap = getAvailableStudentsForUserForAssignments(userId, assignmentList, AssignmentConstants.VIEW, contextId);
 	    }
 	    
 	    return assignToViewableStudentsMap;
 	}
 	
 	public List<String> getViewableStudentsForUserForItem(String userId, Assignment2 assignment) {
 		if (assignment == null || userId == null) {
 			throw new IllegalArgumentException("null assignment or userId passed to " +
 					"getViewableStudentsForUserForItem. userId: " + userId + " assignment: " + assignment);
 		}
 
 		List<Assignment2> assignmentList = new ArrayList<Assignment2>();
 		assignmentList.add(assignment);
 
 		Map<Assignment2, List<String>> assignIdAvailStudentsMap = getAvailableStudentsForUserForAssignments(userId, assignmentList, AssignmentConstants.VIEW, assignment.getContextId());
 
 		List<String> availStudents = assignIdAvailStudentsMap.get(assignment);
 
 		return availStudents;
 	}
 	
 	public List<String> getGradableStudentsForUserForItem(String userId, Assignment2 assignment) {
 		if (assignment == null || userId == null) {
 			throw new IllegalArgumentException("null assignment or userId passed " +
 					"to getGradableStudentsForUserForItem. " +
 					"userId: " + userId + " assignment: " + assignment);
 		}
 		
 		List<Assignment2> assignmentList = new ArrayList<Assignment2>();
 		assignmentList.add(assignment);
 		
 		Map<Assignment2, List<String>> assignIdAvailStudentsMap = getAvailableStudentsForUserForAssignments(userId, assignmentList, AssignmentConstants.GRADE, assignment.getContextId());
 		List<String> availStudents = assignIdAvailStudentsMap.get(assignment);
 		
 		return availStudents;
 	}
 	
 	/**
 	 * 
 	 * @param userId
 	 * @param assignmentList
 	 * @param gradeOrView - indicate whether you want all the students a user may view or all that the user may grade.
 	 * use {@link AssignmentConstants.GRADE} or {@link AssignmentConstants.VIEW}
 	 * @param contextId 
 	 * @return a map of the Assignment2 object to a list of student uids that
 	 * the given user is allowed to view or grade (based upon the gradeOrView param).
 	 * if the user is not allowed to view any students for an assignment, the assignment
 	 * will be returned with an empty list
 	 */
 	private Map<Assignment2, List<String>> getAvailableStudentsForUserForAssignments(String userId, List<Assignment2> assignmentList, String gradeOrView, String contextId) {
 	    if (userId == null) {
 	        throw new IllegalArgumentException("null userId passed to getAvailableStudentsForUserForItem. userId: " + userId);
 	    }
 	    
 	    if (contextId == null) {
 	        throw new IllegalArgumentException("null contextId passed to " + this);
 	    }
 
 	    if (gradeOrView == null || (!gradeOrView.equals(AssignmentConstants.GRADE) && !gradeOrView.equals(AssignmentConstants.VIEW))) {
 	        throw new IllegalArgumentException("Invalid gradeOrView " + gradeOrView + " passed to getAvailableStudentsForUserForItem");
 	    }
 
 	    Map<Assignment2, List<String>> assignToAvailStudentsMap = new HashMap<Assignment2, List<String>>();
 	    
         List<String> allStudentsInSite = externalLogic.getStudentsInSite(contextId);
         boolean userAbleToGradeAll = gradebookLogic.isUserAbleToGradeAll(contextId, userId);
         boolean userAbleToGrade = gradebookLogic.isUserAbleToGrade(contextId, userId);
 
 	    if (assignmentList != null && !assignmentList.isEmpty()) {
 	        
 	        for (Assignment2 assignment : assignmentList) {
                 List<String> availStudents = new ArrayList<String>();
                 
 	            if (!assignment.isRemoved()) {
 
 	                List<AssignmentGroup> assignGroupRestrictions = 
 	                    dao.findByProperties(AssignmentGroup.class, new String[] {"assignment"}, new Object[] {assignment});
 
 	                // if user may grade all, then return all of the students who have this assignment.
 	                // we need to check for group restrictions
 	                if (userAbleToGradeAll) {
 	                    if (assignGroupRestrictions == null || assignGroupRestrictions.isEmpty()) {
 	                        availStudents.addAll(allStudentsInSite);
 	                    } else {
 	                        availStudents = getAllAvailableStudentsGivenGroupRestrictions(
 	                                contextId, assignGroupRestrictions);
 	                    }
 
 	                } else if(userAbleToGrade) {
 	                    // if assignment is ungraded or associated gb item has been deleted,
 	                    // we treat this assignment as ungraded
 	                    boolean treatAsUngraded = false;
 	                    if (assignment.isGraded() && assignment.getGradebookItemId() != null) {
 	                        // double check that gb item still exists
 	                        if (!gradebookLogic.gradebookItemExists(assignment.getGradebookItemId())) {
 	                            treatAsUngraded = true;
 	                        } else {
 	                            // we need to get the students who are viewable in the gb
 	                            List<String> viewableInGb = new ArrayList<String>();
 	                            Map<String, String> studentIdFunctionMap = 
 	                                gradebookLogic.getViewableStudentsForGradedItemMap(userId, contextId, assignment.getGradebookItemId());
 	                            if (studentIdFunctionMap != null) {
 	                                if (gradeOrView.equals(AssignmentConstants.VIEW)) {
 	                                    viewableInGb.addAll(studentIdFunctionMap.keySet());
 	                                } else {
 	                                    for (Map.Entry<String, String> entry : studentIdFunctionMap.entrySet()) {
 	                                        String studentId = entry.getKey();
 	                                        String function = entry.getValue();
 	                                        if (studentId != null && function != null
 	                                                && function.equals(AssignmentConstants.GRADE)) {
 	                                            viewableInGb.add(studentId);
 	                                        }
 	                                    }
 	                                }
 	                            }
 
 	                            // now we need to filter out the ones who aren't associated w/ this assignment
 	                            if (assignGroupRestrictions == null || assignGroupRestrictions.isEmpty()) {
 	                                availStudents.addAll(viewableInGb);
 	                            } else {
 	                                List<String> availForAssign = getAllAvailableStudentsGivenGroupRestrictions(
 	                                        contextId, assignGroupRestrictions);
 	                                if (availForAssign != null && !availForAssign.isEmpty()) {
 	                                    for (String studentId : availForAssign) {
 	                                        if (studentId != null && viewableInGb.contains(studentId)) {
 	                                            availStudents.add(studentId);
 	                                        }
 	                                    }
 	                                }
 	                            }
 	                        }
 	                    } else {
 	                        treatAsUngraded = true;
 	                    }
 	                    
 	                    if (treatAsUngraded) {
 	                     // if there are no restrictions, return students in user's group(s)
                             if (assignGroupRestrictions == null || assignGroupRestrictions.isEmpty()) {
                                 Set<String> sharedStudents = getStudentsInUsersGroups(userId, contextId);
                                 if (sharedStudents != null) {
                                     availStudents.addAll(sharedStudents);
                                 }
                             } else {
                                 // otherwise, only return students in his/her group if it is one
                                 // of the group restrictions
                                 List<String> memberships = externalLogic.getUserMembershipGroupIdList(userId, contextId);
                                 if (memberships != null && !memberships.isEmpty()) {
                                     for (AssignmentGroup group : assignGroupRestrictions) {
                                         if (group != null && memberships.contains(group.getGroupId())) {
                                             availStudents.addAll(externalLogic.getStudentsInGroup(group.getGroupId()));
                                         }
                                     }
                                 }
                             }
 	                    }
 	                }     
 	            }
 	            
 	            assignToAvailStudentsMap.put(assignment, availStudents);
 	        }
 	    }
 
 	    return assignToAvailStudentsMap;
 	}
 	
 	/**
 	 * Given 2 lists of group ids, will return true if any of the group ids overlap
 	 * @param user1GroupIds
 	 * @param user2GroupIds
 	 * @return
 	 */
 	private boolean userMembershipsOverlap(List<String> user1GroupIds, List<String> user2GroupIds) {
 		if (user1GroupIds != null && user2GroupIds != null ) {
 			for (String user1Group : user1GroupIds) {
 				if (user1Group != null && user2GroupIds.contains(user1Group)) {
 					return true;
 				}
 			}
 		}
 		
 		return false;
 	}
 	
 	private Set<String> getStudentsInUsersGroups(String userId, String contextId) {
 		// get the user's memberships
 		List<String> groupMemberships = externalLogic.getUserMembershipGroupIdList(userId, contextId);
 		
 		// use a set to eliminate group overlap with duplicate students
 		Set<String> sharedStudents = new HashSet<String>();
 		if (groupMemberships != null) {
 			for (String groupId : groupMemberships) {
 				if (groupId != null) {
 					List<String> students = externalLogic.getStudentsInGroup(groupId);
 					if (students != null) {
 						sharedStudents.addAll(students);
 					}
 				}
 			}
 
 		}
 		
 		return sharedStudents;
 	}
 	
 	public boolean isUserAllowedToProvideFeedbackForAssignment(Assignment2 assignment) {
 		// returns true if user is authorized to grade at least one student for
 		// this assignment
 		if (assignment == null) {
 			throw new IllegalArgumentException("null assignment passed to isUserAllowedToProvideFeedbackForAssignment");
 		}
 		
 		boolean allowedToGrade = false;
 		
 		if (gradebookLogic.isCurrentUserAbleToGradeAll(assignment.getContextId())) {
 		    allowedToGrade = true;
 		} else {
 		    String currUserId = externalLogic.getCurrentUserId();
 
 		    try {
 		        List<String> gradableStudents = getGradableStudentsForUserForItem(currUserId, assignment);
 		        if (gradableStudents != null && gradableStudents.size() > 0) {
 		            allowedToGrade = true;
 		        }
 		    } catch (SecurityException se) {
 		        // this user does not have grading privileges, so may not release
 		        allowedToGrade = false;
 		    }
 		}
 		
 		return allowedToGrade;
 	}
 	
 	public boolean isCurrentUserAbleToSubmit(String contextId) {
 		return gradebookLogic.isCurrentUserAStudentInGb(contextId);
 	}
 	
 	/**
 	 * 
 	 * @param contextId
 	 * @param assignGroupRestrictions
 	 * @return a list of all of the students who are able to view this assignment
 	 * given any group restrictions
 	 */
 	private List<String> getAllAvailableStudentsGivenGroupRestrictions(String contextId, Collection<AssignmentGroup> assignGroupRestrictions) {
 		List<String> allStudentsForAssign = new ArrayList<String>();
 		
 		// if there are group restrictions, only a subset of all of the students in the
 		// class will be available for this assignment
 		if (assignGroupRestrictions == null || assignGroupRestrictions.isEmpty()) {
 			allStudentsForAssign = externalLogic.getStudentsInSite(contextId);
 		} else {
 			// use a set to make sure students only appear once, even if they
 			// are in multiple groups
 			Set<String> studentsInRestrictedGroups = new HashSet<String>();
 			for (AssignmentGroup group : assignGroupRestrictions) {
 				if (group != null) {
 					studentsInRestrictedGroups.addAll(externalLogic.getStudentsInGroup(group.getGroupId()));
 				}
 			}
 			
 			allStudentsForAssign.addAll(studentsInRestrictedGroups);
 		}
 		
 		return allStudentsForAssign;
 	}
 
 	public boolean isUserAbleToViewAssignment(Long assignmentId) {
 		if (assignmentId == null) {
 			throw new IllegalArgumentException("Null assignmentId passed to isUserAbleToViewAssignment");
 		}
 		boolean allowed = false;
 
 		// retrieve the assignment
 		Assignment2 assign = dao.getAssignmentByIdWithGroups(assignmentId);
 		if (assign == null) {
 		    throw new AssignmentNotFoundException("No assignment found with id " + assignmentId);
 		}
 		
 		List<Assignment2> assignmentList = new ArrayList<Assignment2>();
 		assignmentList.add(assign);
 		
 		List<Assignment2> filteredAssignments = filterViewableAssignments(assign.getContextId(), assignmentList);
 		if (filteredAssignments != null && filteredAssignments.size() == 1) {
 		    allowed = true;
 		}
 		
 		return allowed;
 	}
 	
 	public List<String> getUsersAllowedToViewStudentForAssignment(String studentId, Assignment2 assignment) {
 	    if (studentId == null || assignment == null) {
 	        throw new IllegalArgumentException("Null studentId or assignmentId passed to getUsersAllowedToViewSubmission");
 	    }
 	    
 	    List<String> usersAllowedToViewStudent = new ArrayList<String>();
 
 	    // identify all of the instructors in this site
 	    Set<String> instructorsAndTas = new HashSet<String>();
 	    List<String> instructors = externalLogic.getInstructorsInSite(assignment.getContextId());
 	    List<String> tas = externalLogic.getTAsInSite(assignment.getContextId());
 	    
 	    if (instructors != null) {
 	        instructorsAndTas.addAll(instructors);
 	    }
 	    
 	    if (tas != null) {
 	        instructorsAndTas.addAll(tas);
 	    }
 	    
 	    for (String userId : instructorsAndTas) {
 	        List<String> viewableStudents = getViewableStudentsForUserForItem(userId, assignment);
 	        if (viewableStudents != null && viewableStudents.contains(studentId)) {
 	            usersAllowedToViewStudent.add(userId);
 	        }
 	    }
 	    
 	    return usersAllowedToViewStudent;
 	}
 	
 	public List<Group> getViewableGroupsForCurrUserForAssignment(Long assignmentId) {
 	    List<Group> viewableGroups = new ArrayList<Group>();
 
 	    Assignment2 assign = dao.getAssignmentByIdWithGroups(assignmentId);
 	    if (assign == null) {
 	        throw new AssignmentNotFoundException("getViewableGroupsForCurrUserForAssignment: " +
 	                "No assignment exists with id: " + assignmentId);
 	    }
 
         // first, we need to identify the viewable groups for this user in this site.
         // then we'll check the group restrictions for this assignment and
         // return the groups for this assignment that the user may view
 	    List<Group> allViewableGroups = getViewableGroupsForCurrentUser(assign.getContextId());
 	    
 	    if (allViewableGroups != null && !allViewableGroups.isEmpty()) {
 	        if (assign.getAssignmentGroupSet() != null && !assign.getAssignmentGroupSet().isEmpty()) {
 
 	            if (assign.getAssignmentGroupSet() == null || assign.getAssignmentGroupSet().isEmpty()) {
 	                // there are no group restrictions, so return all viewable
 	                viewableGroups = allViewableGroups;
 	            } else {
 	                // put the groups in a map for easier processing
 	                Map<String, Group> gbGroupIdToGroupMap = new HashMap<String, Group>();
 	                for (Group group : allViewableGroups) {
 	                    gbGroupIdToGroupMap.put(group.getId(), group);
 	                }
 
 	                // check to see if this user may view any of the restricted groups
 	                for (AssignmentGroup assignGroup : assign.getAssignmentGroupSet()) {
 	                    String groupId = assignGroup.getGroupId();
 
 	                    if (gbGroupIdToGroupMap.containsKey(groupId)) {
 	                        viewableGroups.add(gbGroupIdToGroupMap.get(groupId));
 	                    }
 	                }
 	            }
 	        } else {
 	            viewableGroups.addAll(allViewableGroups);
 	        }
 	    }
 
 	    return viewableGroups;
 	}
 	
 	public List<Group> getViewableGroupsForCurrentUser(String contextId) {
 	    if (contextId == null) {
 	        throw new IllegalArgumentException("Null contextId passed to " + this);
 	    }
 	    List<Group> viewableGroups = new ArrayList<Group>();
 	    
 	    // if a grader, return viewable groups in gradebook.
 	    // otherwise, return the user's group membership
 	    if (gradebookLogic.isCurrentUserAbleToGrade(contextId)) {
 	        viewableGroups = gradebookLogic.getViewableGroupsInGradebook(contextId);
 	    } else {
 	       Collection<Group> membership = externalLogic.getUserMemberships(externalLogic.getCurrentUserId(), contextId);
 	       if (membership != null) {
 	           viewableGroups = new ArrayList<Group>(membership);
 	       }
 	    }
 
         return viewableGroups;
 	}
 	
 	public boolean isUserAbleToProvideFeedbackForStudents(Collection<String> studentUids, Assignment2 assignment) {
 	    if (assignment == null) {
 	        throw new IllegalArgumentException("Null assignment passed to isUserAbleToProvideFeedbackForStudents");
 	    }
 	    boolean allowed = true;
 	    if (studentUids != null) {
 	        List<String> gradableStudents = getGradableStudentsForUserForItem(externalLogic.getCurrentUserId(), assignment);
 	        if (gradableStudents != null) {
 	            for (String studentUid : studentUids) {
 	                if (!gradableStudents.contains(studentUid)) {
 	                    allowed = false;
 	                    break;
 	                }
 	            }
 	        }
 	    }
 
 	    return allowed;    
 	}
 	
 	public boolean isUserAbleToProvideFeedbackForAllStudents(String contextId) {
 	    if (contextId == null) {
 	        throw new IllegalArgumentException("Null contextId passed to " + this);
 	    }
 	    return gradebookLogic.isCurrentUserAbleToGradeAll(contextId);
 	}
 
 }
