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
 
 package org.sakaiproject.assignment2.logic.test.stubs;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.sakaiproject.assignment2.logic.ExternalLogic;
 import org.sakaiproject.assignment2.test.AssignmentTestDataLoad;
 
 import org.sakaiproject.service.gradebook.shared.AssessmentNotFoundException;
 import org.sakaiproject.service.gradebook.shared.Assignment;
 import org.sakaiproject.service.gradebook.shared.AssignmentHasIllegalPointsException;
 import org.sakaiproject.service.gradebook.shared.CategoryDefinition;
 import org.sakaiproject.service.gradebook.shared.CommentDefinition;
 import org.sakaiproject.service.gradebook.shared.ConflictingAssignmentNameException;
 import org.sakaiproject.service.gradebook.shared.ConflictingExternalIdException;
 import org.sakaiproject.service.gradebook.shared.GradeDefinition;
 import org.sakaiproject.service.gradebook.shared.GradebookNotFoundException;
 import org.sakaiproject.service.gradebook.shared.GradebookService;
 import org.sakaiproject.service.gradebook.shared.StaleObjectModificationException;
 import org.sakaiproject.service.gradebook.shared.GradebookService.PointsPossibleValidation;
 import org.sakaiproject.service.gradebook.shared.Assignment;
 
 /**
  * This is a stub class for testing purposes. Integrating the GradebookService
  * into our tests requires some messy dependencies, so we have a mock of the
  * GradebookService here.  Note!!!!  This will need to updated whenever
  * the GradebookService interface is changed (ie new methods are added). Use
  * your best judgment to determine whether you need much implementation here.
  * You may need to go back and re-think associated tests to see if they really
  * are just testing the gb functionality and can be removed or if we need
  * them to test assignment stuff.
  * 
  */
 public class GradebookServiceStub implements GradebookService {
 
     private ExternalLogic externalLogic;
     public void setExternalLogic(ExternalLogic externalLogic) {
         this.externalLogic = externalLogic;
     }
 
     public Assignment getGbItem1() {
         Assignment gbItem1 = new Assignment();
         gbItem1.setId(AssignmentTestDataLoad.GB_ITEM1_ID);
         gbItem1.setName(AssignmentTestDataLoad.GB_ITEM1_NAME);
         gbItem1.setPoints(AssignmentTestDataLoad.GB_ITEM1_PTS);
         gbItem1.setReleased(false);  // gb item 1 is not released
 
         return gbItem1;
     }
 
     public Assignment getGbItem2() {
         Assignment gbItem2 = new Assignment();
         gbItem2.setId(AssignmentTestDataLoad.GB_ITEM2_ID);
         gbItem2.setName(AssignmentTestDataLoad.GB_ITEM2_NAME);
         gbItem2.setPoints(AssignmentTestDataLoad.GB_ITEM2_PTS);
         gbItem2.setReleased(true);
 
         return gbItem2;
     }
 
     public Assignment getGbItem3() { 
         Assignment gbItem3 = new Assignment();
         gbItem3.setId(AssignmentTestDataLoad.GB_ITEM3_ID);
         gbItem3.setName(AssignmentTestDataLoad.GB_ITEM3_NAME);
         gbItem3.setPoints(AssignmentTestDataLoad.GB_ITEM3_PTS);
         gbItem3.setReleased(true);
 
         return gbItem3;
     }
 
     public GradeDefinition getSt1Item1GradeDef() {
         GradeDefinition gradeDef = new GradeDefinition();
         gradeDef.setGrade(AssignmentTestDataLoad.st1a3Grade.toString());
         gradeDef.setGradeComment(AssignmentTestDataLoad.st1a3Comment);
         gradeDef.setGradeEntryType(GradebookService.GRADE_TYPE_POINTS);
         gradeDef.setGradeReleased(getGbItem1().isReleased());
         gradeDef.setStudentUid(AssignmentTestDataLoad.STUDENT1_UID);
 
         return gradeDef;
     }
 
     public GradeDefinition getSt2Item2GradeDef() {
         GradeDefinition gradeDef = new GradeDefinition();
         gradeDef.setGrade(AssignmentTestDataLoad.st2a4Grade.toString());
         gradeDef.setGradeComment(AssignmentTestDataLoad.st2a4Comment);
         gradeDef.setGradeEntryType(GradebookService.GRADE_TYPE_POINTS);
         gradeDef.setGradeReleased(getGbItem2().isReleased());
         gradeDef.setStudentUid(AssignmentTestDataLoad.STUDENT2_UID);
 
         return gradeDef;
     }
 
     public boolean isAssignmentDefined(final String gradebookUid, final String assignmentName)
     throws GradebookNotFoundException {
         if (!isUserAbleToViewAssignments(gradebookUid)) {
             throw new SecurityException("You do not have permission to perform this operation");
         }
 
         if (assignmentName.equals(AssignmentTestDataLoad.GB_ITEM1_NAME) || assignmentName.equals(AssignmentTestDataLoad.GB_ITEM2_NAME)) {
             return true;
         }
         return false;
     }
 
     private boolean isUserAbleToViewAssignments(String gradebookUid) {
 
         // return false if not the current context
         if (!externalLogic.getCurrentContextId().equals(gradebookUid)) {
             return false;
         }
 
         String currentUserId = externalLogic.getCurrentUserId();
 
         if (currentUserId.equals(AssignmentTestDataLoad.TA_UID) ||
                 currentUserId.equals(AssignmentTestDataLoad.INSTRUCTOR_UID)) {
             return true;
         }
 
         return false;
     }
 
     public boolean isUserAbleToGradeItemForStudent(String gradebookUid, Long itemId, String studentUid) {
         // return false if not the current context
         if (!externalLogic.getCurrentContextId().equals(gradebookUid)) {
             return false;
         }
 
         String currentUser = externalLogic.getCurrentUserId();
         boolean canGrade = false;
         if (currentUser.equals(AssignmentTestDataLoad.STUDENT1_UID)) {
             canGrade = false;
         } else if (currentUser.equals(AssignmentTestDataLoad.STUDENT2_UID)) {
             canGrade = false;
         } else if (currentUser.equals(AssignmentTestDataLoad.STUDENT3_UID)) {
             canGrade = false;
         } else if (currentUser.equals(AssignmentTestDataLoad.TA_UID)) {
             // TA may only grade student 1
             if (studentUid.equals(AssignmentTestDataLoad.STUDENT1_UID)) {
                 canGrade = true;
             }
         } else if (currentUser.equals(AssignmentTestDataLoad.INSTRUCTOR_UID)) {
             canGrade = true;
         }
 
         return canGrade;
     }
 
     public boolean isUserAbleToGradeItemForStudent(String gradebookUid, String itemName, String studentUid) {
 
         // return false if not the current context
         if (!externalLogic.getCurrentContextId().equals(gradebookUid)) {
             return false;
         }
 
         // won't pay attention to the assignment for now b/c we don't have grader perms
         return isUserAbleToGradeItemForStudent(gradebookUid, 1L, studentUid);
 
     }
 
     public boolean isUserAbleToViewItemForStudent(String gradebookUid, Long itemId, String studentUid) {
         // return false if not the current context
         if (!externalLogic.getCurrentContextId().equals(gradebookUid)) {
             return false;
         }
 
         String currentUser = externalLogic.getCurrentUserId();
         boolean canView = false;
         if (currentUser.equals(AssignmentTestDataLoad.STUDENT1_UID)) {
             if (currentUser.equals(studentUid)) {
                 canView = true;
             }
         } else if (currentUser.equals(AssignmentTestDataLoad.STUDENT2_UID)) {
             if (currentUser.equals(studentUid)) {
                 canView = true;
             }
         } else if (currentUser.equals(AssignmentTestDataLoad.STUDENT3_UID)) {
             if (currentUser.equals(studentUid)) {
                 canView = true;
             }
         } else if (currentUser.equals(AssignmentTestDataLoad.TA_UID)) {
             // TA may only grade student 1
             if (studentUid.equals(AssignmentTestDataLoad.STUDENT1_UID)) {
                 canView = true;
             }
         } else if (currentUser.equals(AssignmentTestDataLoad.INSTRUCTOR_UID)) {
             canView = true;
         }
 
         return canView;
     }
 
     public boolean isUserAbleToViewItemForStudent(String gradebookUid, String itemName, String studentUid) {
 
         // return false if not the current context
         if (!externalLogic.getCurrentContextId().equals(gradebookUid)) {
             return false;
         }
 
         // assign doesn't matter b/c we don't have grader perms
         return isUserAbleToViewItemForStudent(gradebookUid, 1L, studentUid);
 
     }
 
     public String getGradeViewFunctionForUserForStudentForItem(String gradebookUid, Long itemId, String studentUid) {
         return getGradeViewFunctionForUserForStudentForItem(externalLogic.getCurrentUserId(), gradebookUid, itemId, studentUid);
     }
 
     private String getGradeViewFunctionForUserForStudentForItem(String userId, String gradebookUid, Long itemId, String studentUid) {
         String viewGrade = null;
 
         if (gradebookUid.equals(AssignmentTestDataLoad.CONTEXT_ID)) {
             // make sure item exists
             if (itemId.equals(AssignmentTestDataLoad.GB_ITEM1_ID) || 
                     itemId.equals(AssignmentTestDataLoad.GB_ITEM2_ID) ||
                     itemId.equals(AssignmentTestDataLoad.GB_ITEM2_ID)) {
 
                 if (userId.equals(AssignmentTestDataLoad.STUDENT1_UID)) {
                     viewGrade = null;
                 } else if (userId.equals(AssignmentTestDataLoad.STUDENT2_UID)) {
                     viewGrade = null;
                 } else if (userId.equals(AssignmentTestDataLoad.STUDENT3_UID)) {
                     viewGrade = null;
                 } else if (userId.equals(AssignmentTestDataLoad.TA_UID)) {
                     // TA may only grade student 1
                     if (studentUid.equals(AssignmentTestDataLoad.STUDENT1_UID)) {
                         viewGrade = GradebookService.gradePermission;
                     }
                 } else if (userId.equals(AssignmentTestDataLoad.INSTRUCTOR_UID)) {
                     viewGrade = GradebookService.gradePermission;
                 }
             }
         }
 
         return viewGrade;
     }
 
     public String getGradeViewFunctionForUserForStudentForItem(String gradebookUid, String itemName, String studentUid) {
         // assignment doesn't matter b/c we don't have grader perms
         return getGradeViewFunctionForUserForStudentForItem(gradebookUid, 1L, studentUid);
     }
 
     public List<org.sakaiproject.service.gradebook.shared.Assignment> getAssignments(String gradebookUid)
     throws GradebookNotFoundException {
         if (!isUserAbleToViewAssignments(gradebookUid)) {
             throw new SecurityException("You do not have permission to perform this operation");
         }
 
         List<Assignment> assignments = new ArrayList<Assignment>();
         assignments.add(getGbItem1());
         assignments.add(getGbItem2());
         assignments.add(getGbItem3());
         return assignments;
     }
 
     public Assignment getAssignment(final String gradebookUid, final String assignmentName) throws GradebookNotFoundException {
         if (!isUserAbleToViewAssignments(gradebookUid)) {
             throw new SecurityException("You do not have permission to perform this operation");
         }
 
         Assignment assign = null;
 
         if (assignmentName.equals(AssignmentTestDataLoad.GB_ITEM1_NAME)) {
             assign = getGbItem1();
         } else if (assignmentName.equals(AssignmentTestDataLoad.GB_ITEM2_NAME)) {
             assign = getGbItem2();
         } else if (assignmentName.equals(AssignmentTestDataLoad.GB_ITEM3_NAME)) {
             assign = getGbItem3();
         }
 
         return assign;
     }
 
     public org.sakaiproject.service.gradebook.shared.Assignment getAssignment(final String gradebookUid, final Long gbItemId) throws AssessmentNotFoundException {
         if (gbItemId == null || gradebookUid == null) {
             throw new IllegalArgumentException("null gbItemId passed to getAssignment");
         }
         if (!isUserAbleToViewAssignments(gradebookUid) && !currentUserHasViewOwnGradesPerm(gradebookUid)) {
             throw new SecurityException("You do not have permission to perform this operation");
         }
 
         Assignment assign = null;
 
         if (gbItemId.equals(AssignmentTestDataLoad.GB_ITEM1_ID)) {
             assign = getGbItem1();
         } else if (gbItemId.equals(AssignmentTestDataLoad.GB_ITEM2_ID)) {
             assign = getGbItem2();
         } else if (gbItemId.equals(AssignmentTestDataLoad.GB_ITEM3_ID)) {
             assign = getGbItem2();
         }
 
         return assign;
     }   
 
     public Double getAssignmentScore(final String gradebookUid, final String assignmentName, final String studentUid)
     throws GradebookNotFoundException, AssessmentNotFoundException {
 
 
         return null;
     }
 
     public Double getAssignmentScore(final String gradebookUid, final Long gbItemId, final String studentUid) {
         if (gradebookUid == null || gbItemId == null || studentUid == null) {
             throw new IllegalArgumentException("null parameter passed to getAssignmentScore");
         }
 
         return null;
     }
 
     public GradeDefinition getGradeDefinitionForStudentForItem(final String gradebookUid,
             final Long gbItemId, final String studentUid) {
 
         GradeDefinition gradeDef = null;
         if (externalLogic.getCurrentContextId().equals(gradebookUid)) {
             boolean studentRequestingOwnScore = externalLogic.getCurrentUserId().equals(studentUid);
 
             if (!studentRequestingOwnScore && !isUserAbleToGradeItemForStudent(gradebookUid, gbItemId, studentUid)) {
                 throw new SecurityException("User attempted to grade student w/o perm in getGradeDefinition...");
             }
 
             if (gbItemId.equals(AssignmentTestDataLoad.GB_ITEM1_ID)) {
                 if (studentUid.equals(AssignmentTestDataLoad.STUDENT1_UID)) {
                     gradeDef = getSt1Item1GradeDef();
                 }
             } else if (gbItemId.equals(AssignmentTestDataLoad.GB_ITEM2_ID)) {
                 if (studentUid.equals(AssignmentTestDataLoad.STUDENT2_UID)) {
                     gradeDef = getSt2Item2GradeDef();
                 }
             }
         }
         return gradeDef;
     }
 
     public void setAssignmentScore(final String gradebookUid, final String assignmentName, final String studentUid, final Double score, final String clientServiceDescription)
     throws GradebookNotFoundException, AssessmentNotFoundException {
         // do nothing
     }
 
     public CommentDefinition getAssignmentScoreComment(final String gradebookUid, final String assignmentName, final String studentUid) throws GradebookNotFoundException, AssessmentNotFoundException {
         CommentDefinition commentDef = new CommentDefinition();
         if (studentUid.equals(AssignmentTestDataLoad.STUDENT1_UID)) {
             commentDef.setCommentText(AssignmentTestDataLoad.st1a3Comment);
             commentDef.setStudentUid(studentUid);
         } else if (studentUid.equals(AssignmentTestDataLoad.STUDENT2_UID)) {
             commentDef.setCommentText(AssignmentTestDataLoad.st2a4Comment);
             commentDef.setStudentUid(studentUid);
         }
         return commentDef;
     }
 
     public CommentDefinition getAssignmentScoreComment(final String gradebookUid, final Long gbItemId, final String studentUid) throws GradebookNotFoundException, AssessmentNotFoundException {
         CommentDefinition commentDef = new CommentDefinition();
         if (studentUid.equals(AssignmentTestDataLoad.STUDENT1_UID)) {
             commentDef.setCommentText(AssignmentTestDataLoad.st1a3Comment);
             commentDef.setStudentUid(studentUid);
         } else if (studentUid.equals(AssignmentTestDataLoad.STUDENT2_UID)) {
             commentDef.setCommentText(AssignmentTestDataLoad.st2a4Comment);
             commentDef.setStudentUid(studentUid);
         }
         return commentDef;
     }
 
     public void setAssignmentScoreComment(final String gradebookUid, final String assignmentName, final String studentUid, final String commentText) throws GradebookNotFoundException, AssessmentNotFoundException {
         // do nothing since we aren't creating the gb tables
     }
 
     public String getGradebookDefinitionXml(String gradebookUid) {		
 
         return null;
     }
 
     public void transferGradebookDefinitionXml(String fromGradebookUid, String toGradebookUid, String fromGradebookXml) {
         // not needed
     }
 
     public void mergeGradebookDefinitionXml(String toGradebookUid, String fromGradebookXml) {
         // not needed
     }
 
     public void removeAssignment(final Long assignmentId) throws StaleObjectModificationException {
 
         // not needed
 
     }
 
     public void addAssignment(String gradebookUid, org.sakaiproject.service.gradebook.shared.Assignment assignmentDefinition) {
         // do nothing since we aren't creating the gb tables
     }
 
     public void updateAssignment(final String gradebookUid, final String assignmentName, final org.sakaiproject.service.gradebook.shared.Assignment assignmentDefinition) {		
         // do nothing since we aren't creating the gb tables
     }
 
 
 
     public List<Assignment> getAssignments(final Long gradebookId, final String sortBy, final boolean ascending) {
         List<Assignment> assignList = new ArrayList<Assignment>();
         assignList.add(getGbItem1());
         assignList.add(getGbItem2());
         assignList.add(getGbItem3());
 
         return assignList;
     }
 
     /*
      * (non-Javadoc)
      * @see org.sakaiproject.service.gradebook.shared.GradebookService#getViewableAssignmentsForCurrentUser(java.lang.String)
      */
     public List<org.sakaiproject.service.gradebook.shared.Assignment> getViewableAssignmentsForCurrentUser(String gradebookUid)
     throws GradebookNotFoundException {
 
         List<Assignment> assigns = new ArrayList<Assignment>();
         String currentUser = externalLogic.getCurrentUserId();
 
         if (currentUser.equals(AssignmentTestDataLoad.STUDENT1_UID) || 
                 currentUser.equals(AssignmentTestDataLoad.STUDENT2_UID) ||
                 currentUser.equals(AssignmentTestDataLoad.STUDENT3_UID)) {
             // students only see released gb items
             if (getGbItem1().isReleased()) {
                 assigns.add(getGbItem1());
             }
             if (getGbItem2().isReleased()) {
                 assigns.add(getGbItem2());
             }
             if (getGbItem3().isReleased()) {
                 assigns.add(getGbItem3());
             }
         } else if (currentUser.equals(AssignmentTestDataLoad.TA_UID)) {
             assigns.add(getGbItem1());
             assigns.add(getGbItem2());
             assigns.add(getGbItem3());
         } else if (currentUser.equals(AssignmentTestDataLoad.INSTRUCTOR_UID)) {
             assigns.add(getGbItem1());
             assigns.add(getGbItem2());
             assigns.add(getGbItem3());
         }
 
         return assigns;
 
     }
 
     public Map<String, String> getViewableStudentsForItemForCurrentUser(final String gradebookUid, final Long gradableObjectId) {
         if (gradebookUid == null || gradableObjectId == null) {
             throw new IllegalArgumentException("null gradebookUid or gradableObjectId passed to getViewableStudentsForUserForItem");
         }
 
         Map<String, String> studentIdFunctionMap = new HashMap<String, String>();
 
         List<String> studentList = new ArrayList<String>();
         studentList.add(AssignmentTestDataLoad.STUDENT1_UID);
         studentList.add(AssignmentTestDataLoad.STUDENT2_UID);
         studentList.add(AssignmentTestDataLoad.STUDENT3_UID);
 
         for (String studentId : studentList) {
             String function = getGradeViewFunctionForUserForStudentForItem(gradebookUid, gradableObjectId, studentId);
             if (function != null) {
                 studentIdFunctionMap.put(studentId, function);
             }
         }
 
         return studentIdFunctionMap;
     }
 
     public Map<String, String> getViewableStudentsForItemForUser(String userUid,
             String gradebookUid, Long gradableObjectId)
             {
         if (gradebookUid == null || gradableObjectId == null || userUid == null) {
             throw new IllegalArgumentException("null gradebookUid or gradableObjectId passed to getViewableStudentsForUserForItem");
         }
 
         Map<String, String> studentIdFunctionMap = new HashMap<String, String>();
 
         List<String> studentList = new ArrayList<String>();
         studentList.add(AssignmentTestDataLoad.STUDENT1_UID);
         studentList.add(AssignmentTestDataLoad.STUDENT2_UID);
         studentList.add(AssignmentTestDataLoad.STUDENT3_UID);
 
         for (String studentId : studentList) {
             String function = getGradeViewFunctionForUserForStudentForItem(userUid, gradebookUid, gradableObjectId, studentId);
             if (function != null) {
                 studentIdFunctionMap.put(studentId, function);
             }
         }
 
         return studentIdFunctionMap;
             }
 
     public boolean isGradableObjectDefined(Long gradableObjectId) {
         if (gradableObjectId.equals(AssignmentTestDataLoad.GB_ITEM1_ID) || gradableObjectId.equals(AssignmentTestDataLoad.GB_ITEM2_ID)) {
             return true;
         }
         return false;
     }
 
     public Map<String, String> getViewableSectionUuidToNameMap(String gradebookUid) {	
 
         Map<String, String> sectionIdNameMap = new HashMap<String, String>();
 
         // if not valid context, return empty map
         if (!externalLogic.getCurrentContextId().equals(gradebookUid)) {
             return sectionIdNameMap;
         }
 
         if (currentUserHasGradingPerm(gradebookUid)) {
             String currUser = externalLogic.getCurrentUserId();
 
             // add all for instructor. otherwise, check memberships
             if (currUser.equals(AssignmentTestDataLoad.INSTRUCTOR_UID)) {
                 sectionIdNameMap.put(AssignmentTestDataLoad.GROUP1_NAME, AssignmentTestDataLoad.GROUP1_NAME);
                 sectionIdNameMap.put(AssignmentTestDataLoad.GROUP2_NAME, AssignmentTestDataLoad.GROUP2_NAME);
                 sectionIdNameMap.put(AssignmentTestDataLoad.GROUP3_NAME, AssignmentTestDataLoad.GROUP3_NAME);
             } else if (currUser.equals(AssignmentTestDataLoad.TA_UID)){
                 // TAs may only grade in their own groups ootb
                 sectionIdNameMap.put(AssignmentTestDataLoad.GROUP1_NAME, AssignmentTestDataLoad.GROUP1_NAME);
             }
         }
 
         return sectionIdNameMap;
     }
 
     public boolean currentUserHasGradeAllPerm(String gradebookUid) {
         return isUserAllowedToGradeAll(gradebookUid, externalLogic.getCurrentUserId());
     }
 
     public boolean isUserAllowedToGradeAll(String gradebookUid, String userId) {
         // return false if not the current context
         if (!externalLogic.getCurrentContextId().equals(gradebookUid)) {
             return false;
         }
 
         boolean hasPerm = false;
         if (userId.equals(AssignmentTestDataLoad.STUDENT1_UID)) {
             hasPerm = false;
         } else if (userId.equals(AssignmentTestDataLoad.STUDENT2_UID)) {
             hasPerm = false;
         } else if (userId.equals(AssignmentTestDataLoad.STUDENT3_UID)) {
             hasPerm = false;
         } else if (userId.equals(AssignmentTestDataLoad.TA_UID)) {
             hasPerm = false;
         } else if (userId.equals(AssignmentTestDataLoad.INSTRUCTOR_UID)) {
             hasPerm = true;
         }
 
         return hasPerm;
     }
 
     public boolean currentUserHasGradingPerm(String gradebookUid) {
         return isUserAllowedToGrade(gradebookUid, externalLogic.getCurrentUserId());
     }
 
     public boolean isUserAllowedToGrade(String gradebookUid, String userId) {
         // return false if not the current context
         if (!externalLogic.getCurrentContextId().equals(gradebookUid)) {
             return false;
         }
 
         boolean hasPerm = false;
         if (userId.equals(AssignmentTestDataLoad.STUDENT1_UID)) {
             hasPerm = false;
         } else if (userId.equals(AssignmentTestDataLoad.STUDENT2_UID)) {
             hasPerm = false;
         } else if (userId.equals(AssignmentTestDataLoad.STUDENT3_UID)) {
             hasPerm = false;
         } else if (userId.equals(AssignmentTestDataLoad.TA_UID)) {
             hasPerm = true;
         } else if (userId.equals(AssignmentTestDataLoad.INSTRUCTOR_UID)) {
             hasPerm = true;
         }
 
         return hasPerm;
     }
 
     public boolean currentUserHasEditPerm(String gradebookUid) {
         // return false if not the current context
         if (!externalLogic.getCurrentContextId().equals(gradebookUid)) {
             return false;
         }
 
         String currentUser = externalLogic.getCurrentUserId();
         boolean hasPerm = false;
         if (currentUser.equals(AssignmentTestDataLoad.STUDENT1_UID)) {
             hasPerm = false;
         } else if (currentUser.equals(AssignmentTestDataLoad.STUDENT2_UID)) {
             hasPerm = false;
         } else if (currentUser.equals(AssignmentTestDataLoad.STUDENT3_UID)) {
             hasPerm = false;
         } else if (currentUser.equals(AssignmentTestDataLoad.TA_UID)) {
             hasPerm = false;
         } else if (currentUser.equals(AssignmentTestDataLoad.INSTRUCTOR_UID)) {
             hasPerm = true;
         }
 
         return hasPerm;
     }
 
     public boolean currentUserHasViewOwnGradesPerm(String gradebookUid) {
         // return false if not the current context
         if (!externalLogic.getCurrentContextId().equals(gradebookUid)) {
             return false;
         }
         String currentUser = externalLogic.getCurrentUserId();
         boolean hasPerm = false;
         if (currentUser.equals(AssignmentTestDataLoad.STUDENT1_UID)) {
             hasPerm = true;
         } else if (currentUser.equals(AssignmentTestDataLoad.STUDENT2_UID)) {
             hasPerm = true;
         } else if (currentUser.equals(AssignmentTestDataLoad.STUDENT3_UID)) {
             hasPerm = true;
         } else if (currentUser.equals(AssignmentTestDataLoad.TA_UID)) {
             hasPerm = false;
         } else if (currentUser.equals(AssignmentTestDataLoad.INSTRUCTOR_UID)) {
             hasPerm = false;
         }
 
         return hasPerm;
     }
 
     public List<GradeDefinition> getGradesForStudentsForItem(final String gradebookUid, final Long gradableObjectId, List<String> studentIds) {
         if (gradableObjectId == null) {
             throw new IllegalArgumentException("null gradableObjectId passed to getGradesForStudentsForItem");
         }
 
         List<GradeDefinition> studentGrades = new ArrayList<GradeDefinition>();
 
         if (studentIds != null) {
             // check auth
             for (String studentId : studentIds) {
                 if (!isUserAbleToViewItemForStudent(gradebookUid, gradableObjectId, studentId)) {
                     throw new SecurityException();
                 }
             }
             if (gradableObjectId.equals(AssignmentTestDataLoad.GB_ITEM1_ID)) {
                 studentGrades.add(getSt1Item1GradeDef());
             } else if (gradableObjectId.equals(AssignmentTestDataLoad.GB_ITEM2_ID)) {
                 studentGrades.add(getSt2Item2GradeDef());
             }
         }
 
         return studentGrades;
     }
 
     public boolean isGradeValid(String gradebookUuid, String grade) {
         return true;
     }
 
     public List<String> identifyStudentsWithInvalidGrades(String gradebookUid, Map<String, String> studentIdToGradeMap) {
         if (gradebookUid == null) {
             throw new IllegalArgumentException("null gradebookUid passed to identifyStudentsWithInvalidGrades");
         }
 
         return new ArrayList<String>();
     }
 
     public void saveGradeAndCommentForStudent(String gradebookUid, Long gradableObjectId, String studentUid, String grade, String comment) {
         // do nothing since we aren't creating the gb tables
     }
 
     public void saveGradesAndComments(final String gradebookUid, final Long gradableObjectId, List<GradeDefinition> gradeDefList) {
         // do nothing since we aren't creating the gb tables
     }
 
     public int getGradeEntryType(String gradebookUid) {
         return GradebookService.GRADE_TYPE_POINTS;
     }
 
     public void addExternalAssessment(String gradebookUid, String externalId,
             String externalUrl, String title, Double points, Date dueDate,
             String externalServiceDescription, Boolean ungraded)
     throws GradebookNotFoundException, ConflictingAssignmentNameException,
     ConflictingExternalIdException, AssignmentHasIllegalPointsException
     {
         // TODO Auto-generated method stub
 
     }
 
     public void addExternalAssessment(String gradebookUid, String externalId,
             String externalUrl, String title, double points, Date dueDate,
             String externalServiceDescription) throws GradebookNotFoundException,
             ConflictingAssignmentNameException, ConflictingExternalIdException,
             AssignmentHasIllegalPointsException
             {
         // TODO Auto-generated method stub
 
             }
 
     public void addGradebook(String uid, String name)
     {
         // TODO Auto-generated method stub
 
     }
 
     public boolean checkStuendsNotSubmitted(String gradebookUid)
     {
         // TODO Auto-generated method stub
         return false;
     }
 
     public void deleteGradebook(String uid) throws GradebookNotFoundException
     {
         // TODO Auto-generated method stub
 
     }
 
     public String getAssignmentScoreString(String gradebookUid, String assignmentName,
             String studentUid) throws GradebookNotFoundException,
             AssessmentNotFoundException
             {
         // TODO Auto-generated method stub
         return null;
             }
 
     public String getAssignmentScoreString(String gradebookUid, Long gbItemId,
             String studentUid) throws GradebookNotFoundException,
             AssessmentNotFoundException
             {
         // TODO Auto-generated method stub
         return null;
             }
 
     public Map getCalculatedCourseGrade(String gradebookUid)
     {
         // TODO Auto-generated method stub
         return null;
     }
 
     public List getCategories(Long gradebookId)
     {
         // TODO Auto-generated method stub
         return null;
     }
 
     public Map getEnteredCourseGrade(String gradebookUid)
     {
         // TODO Auto-generated method stub
         return null;
     }
 
     public Map getFixedGrade(String gradebookUid)
     {
         // TODO Auto-generated method stub
         return null;
     }
 
     public Map getFixedPoint(String gradebookUid)
     {
         // TODO Auto-generated method stub
         return null;
     }
 
     public Object getGradebook(String uid) throws GradebookNotFoundException
     {
         // TODO Auto-generated method stub
         return null;
     }
 
     public Map getImportCourseGrade(String gradebookUid)
     {
         // TODO Auto-generated method stub
         return null;
     }
 
     public Map getOldPoint(String gradebookUid)
     {
         // TODO Auto-generated method stub
         return null;
     }
 
     public boolean isExternalAssignmentDefined(String gradebookUid, String externalId)
     throws GradebookNotFoundException
     {
         // TODO Auto-generated method stub
         return false;
     }
 
     public boolean isGradebookDefined(String gradebookUid)
     {
         // TODO Auto-generated method stub
         return false;
     }
 
     public void removeCategory(Long categoryId) throws StaleObjectModificationException
     {
         // TODO Auto-generated method stub
 
     }
 
     public void removeExternalAssessment(String gradebookUid, String externalId)
     throws GradebookNotFoundException, AssessmentNotFoundException
     {
         // TODO Auto-generated method stub
 
     }
 
     public void setAssignmentScoreString(String gradebookUid, String assignmentName,
             String studentUid, String score, String clientServiceDescription)
     throws GradebookNotFoundException, AssessmentNotFoundException
     {
         // TODO Auto-generated method stub
 
     }
 
     public void setAvailableGradingScales(Collection gradingScaleDefinitions)
     {
         // TODO Auto-generated method stub
 
     }
 
     public void setDefaultGradingScale(String uid)
     {
         // TODO Auto-generated method stub
 
     }
 
     public void updateExternalAssessment(String gradebookUid, String externalId,
             String externalUrl, String title, double points, Date dueDate)
     throws GradebookNotFoundException, AssessmentNotFoundException,
     ConflictingAssignmentNameException, AssignmentHasIllegalPointsException
     {
         // TODO Auto-generated method stub
 
     }
 
     public void updateExternalAssessment(String gradebookUid, String externalId,
             String externalUrl, String title, Double points, Date dueDate)
     throws GradebookNotFoundException, AssessmentNotFoundException,
     ConflictingAssignmentNameException, AssignmentHasIllegalPointsException
     {
         // TODO Auto-generated method stub
 
     }
 
     public void updateExternalAssessmentScore(String gradebookUid, String externalId,
             String studentUid, Double points) throws GradebookNotFoundException,
             AssessmentNotFoundException
             {
         // TODO Auto-generated method stub
 
             }
 
     public void updateExternalAssessmentScores(String gradebookUid, String externalId,
             Map studentUidsToScores) throws GradebookNotFoundException,
             AssessmentNotFoundException
             {
         // TODO Auto-generated method stub
 
             }
 
     public void finalizeGrades(String gradebookUid) throws GradebookNotFoundException
     {
         // TODO Auto-generated method stub
 
     }
 
     public String getLowestPossibleGradeForGbItem(String gradebookUid,
             Long gradebookItemId)
     {
         // TODO Auto-generated method stub
         return null;
     }
 
     public List<CategoryDefinition> getCategoryDefinitions(String gradebookUid)
     {
         // TODO Auto-generated method stub
         return null;
     }
 
 	@Override
 	public boolean checkStudentsNotSubmitted(String arg0)
 	{
 		// TODO Auto-generated method stub
 		return false;
 	}
 	
     public PointsPossibleValidation isPointsPossibleValid(String gradebookUid, 
                                                           Assignment gradebookItem,  
                                                           Double pointsPossible)
     {
         return  PointsPossibleValidation.VALID;
     }
 }
