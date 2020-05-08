 package org.sakaiproject.assignment2.tool.entity;
 
 import java.text.DateFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.sakaiproject.assignment2.logic.AssignmentBundleLogic;
 import org.sakaiproject.assignment2.logic.AssignmentLogic;
 import org.sakaiproject.assignment2.logic.AssignmentPermissionLogic;
 import org.sakaiproject.assignment2.logic.AssignmentSubmissionLogic;
 import org.sakaiproject.assignment2.logic.ExternalContentReviewLogic;
 import org.sakaiproject.assignment2.logic.ExternalGradebookLogic;
 import org.sakaiproject.assignment2.logic.ExternalLogic;
 import org.sakaiproject.assignment2.logic.GradeInformation;
 import org.sakaiproject.assignment2.model.Assignment2;
 import org.sakaiproject.assignment2.model.AssignmentSubmission;
 import org.sakaiproject.assignment2.model.AssignmentSubmissionVersion;
 import org.sakaiproject.assignment2.model.SubmissionAttachment;
 import org.sakaiproject.assignment2.model.constants.AssignmentConstants;
 import org.sakaiproject.assignment2.tool.DisplayUtil;
 import org.sakaiproject.assignment2.tool.beans.SubmissionTableViewState;
 import org.sakaiproject.assignment2.tool.beans.SubmissionTableViewStateHolder;
 import org.sakaiproject.assignment2.tool.params.GradeViewParams;
 import org.sakaiproject.assignment2.tool.producers.GradeProducer;
 import org.sakaiproject.entitybroker.EntityReference;
 import org.sakaiproject.entitybroker.entityprovider.CoreEntityProvider;
 import org.sakaiproject.entitybroker.entityprovider.capabilities.RESTful;
 import org.sakaiproject.entitybroker.entityprovider.capabilities.RequestAware;
 import org.sakaiproject.entitybroker.entityprovider.capabilities.RequestStorable;
 import org.sakaiproject.entitybroker.entityprovider.extension.RequestGetter;
 import org.sakaiproject.entitybroker.entityprovider.extension.RequestStorage;
 import org.sakaiproject.entitybroker.entityprovider.search.Order;
 import org.sakaiproject.entitybroker.entityprovider.search.Search;
 import org.sakaiproject.entitybroker.util.AbstractEntityProvider;
 import org.sakaiproject.tool.api.Session;
 import org.sakaiproject.tool.api.SessionManager;
 import org.sakaiproject.tool.api.ToolSession;
 
 import uk.org.ponder.rsf.components.UIBranchContainer;
 import uk.org.ponder.rsf.components.UIInternalLink;
 import uk.org.ponder.rsf.components.UIOutput;
 
 public class Assignment2SubmissionEntityProvider extends AbstractEntityProvider implements
 CoreEntityProvider, RESTful, RequestStorable, RequestAware{
     
     public static final String SUBMISSIONVIEW_SESSION_ATTR_ASCENDING = "ascending";
     public static final String SUBMISSIONVIEW_SESSION_ATTR_ORDER_BY = "orderBy";
     public static final String SUBMISSIONVIEW_SESSION_ATTR_PAGE_SIZE = "pageSize";
     public static final String SUBMISSIONVIEW_SESSION_ATTR = "x-asnn2-submissionview";
     
     public static final String STUDENT_NAME_PROP = "studentName";
     public static final String STUDENT_ID_PROP = "studentId";
     public static final String SUBMITTED_DATE = "submittedDate";
     public static final String SUBMITTED_DATE_FORMATTED = "submittedDateFormat";
     public static final String SUBMISSION_STATUS = "submissionStatus";
     public static final String SUBMISSION_GRADE = "grade";
     public static final String SUBMISSION_FEEDBACK_RELEASED = "feedbackReleased";
 
     /**
      * Dependency
      */
     private AssignmentSubmissionLogic submissionLogic;
     public void setSubmissionLogic(AssignmentSubmissionLogic submissionLogic) {
         this.submissionLogic = submissionLogic;
     }
 
     /**
      * Dependency
      */
     private AssignmentLogic assignmentLogic;
     public void setAssignmentLogic(AssignmentLogic assignmentLogic) {
         this.assignmentLogic = assignmentLogic;
     }
 
     /**
      * Dependency
      */
     private ExternalGradebookLogic externalGradebookLogic;
     public void setExternalGradebookLogic(ExternalGradebookLogic externalGradebookLogic) {
         this.externalGradebookLogic = externalGradebookLogic;
     }
 
     /**
      * Dependency
      */
     private ExternalLogic externalLogic;
     public void setExternalLogic(ExternalLogic externalLogic) {
         this.externalLogic = externalLogic;
     }
 
     /**
      * Dependency
      */
     private AssignmentPermissionLogic permissionLogic;
     public void setPermissionLogic(AssignmentPermissionLogic permissionLogic) {
         this.permissionLogic = permissionLogic;
     }
 
     /**
      * Dependency
      */
     private RequestStorage requestStorage;
     public void setRequestStorage(RequestStorage requestStorage) {
         this.requestStorage = requestStorage;
     }
 
     /**
      * Dependency
      */
     private RequestGetter requestGetter;
     public void setRequestGetter(RequestGetter requestGetter) {
         this.requestGetter = requestGetter;
     }
     
 
     /**
      * Dependency
      */
     private AssignmentBundleLogic assignmentBundleLogic;
     public void setAssignmentBundleLogic(AssignmentBundleLogic assignmentBundleLogic) {
         this.assignmentBundleLogic = assignmentBundleLogic;
     }
     
     /**
      * Dependency
      */
     private SessionManager sessionManager;
     public void setSessionManager(SessionManager sessionManager) {
         this.sessionManager = sessionManager;
     }
     
     private ExternalContentReviewLogic contentReviewLogic;
     public void setExternalContentReviewLogic(ExternalContentReviewLogic contentReviewLogic) {
         this.contentReviewLogic = contentReviewLogic;
     }
 
     public boolean entityExists(String id) {
         // TODO Auto-generated method stub
         return false;
     }
 
     public final static String PREFIX = "assignment2submission";
     public String getEntityPrefix() {
         return PREFIX;
     }
     
     private SubmissionTableViewStateHolder submissionTableViewStateHolder;
     public void setSubmissionTableViewStateHolder(
             SubmissionTableViewStateHolder submissionTableViewStateHolder) {
         this.submissionTableViewStateHolder = submissionTableViewStateHolder;
     }
 
     public String createEntity(EntityReference ref, Object entity,
             Map<String, Object> params) {
         // TODO Auto-generated method stub
         return null;
     }
 
     public Object getSampleEntity() {
         // TODO Auto-generated method stub
         return null;
     }
 
     public void updateEntity(EntityReference ref, Object entity,
             Map<String, Object> params) {
         // TODO Auto-generated method stub
 
     }
 
     public Object getEntity(EntityReference ref) {
         // TODO Auto-generated method stub
         return null;
     }
 
     public void deleteEntity(EntityReference ref, Map<String, Object> params) {
         // TODO Auto-generated method stub
 
     }
 
     @SuppressWarnings("unchecked")
     public List<?> getEntities(EntityReference ref, Search search) {
         Long assignmentId = requestStorage.getStoredValueAsType(Long.class, "asnnid");
         String filterGroupId = requestStorage.getStoredValueAsType(String.class, "groupId");
         String placementId = requestStorage.getStoredValueAsType(String.class, "placementId");
         
         String orderByTest = null;
         if (search.getOrders() != null && search.getOrders().length > 0) {
             orderByTest = search.getOrders()[0].getProperty();
         }
         /*
         SubmissionTableViewState curViewState = new SubmissionTableViewState(assignmentId, filterGroupId, orderByTest, search.getStart(), search.getLimit() );
         
         if (firstcall != null && assignmentId != null) {
             if (firstcall.booleanValue()) {
                 submissionTableViewStateHolder.setSubmissionTableViewState(curViewState);
             }
             else {
                 curViewState = submissionTableViewStateHolder.getSubmissionTableViewState(assignmentId);
                 filterGroupId = curViewState.getGroupId();
                 search.setOrders(new Order[] {new Order(curViewState.getOrderBy())});
                 search.setLimit(curViewState.getSize());
                 search.setStart(curViewState.getStart());
             }
         } 
         */
         
 
         DateFormat df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, assignmentBundleLogic.getLocale());
 
         List togo = new ArrayList();
         
         List<AssignmentSubmission> submissions = submissionLogic.getViewableSubmissionsWithHistoryForAssignmentId(assignmentId, filterGroupId);
 
         if (submissions == null) {
             return togo;
         }
 
         // put studentIds in a list
         List<String> studentIdList = new ArrayList<String>();
         for (AssignmentSubmission submission : submissions) {
             studentIdList.add(submission.getUserId());
         }
 
         Assignment2 assignment = assignmentLogic.getAssignmentById(assignmentId);
 
         Map<String, GradeInformation> studentIdGradeInfoMap = new HashMap<String, GradeInformation>();
         if (submissions != null && assignment.isGraded() && assignment.getGradebookItemId() != null) {
             // now retrieve all of the GradeInformation
             studentIdGradeInfoMap = externalGradebookLogic.getGradeInformationForStudents(
                     studentIdList, assignment.getContextId(), assignment.getGradebookItemId());
         }
 
         Map<String, String> studentIdSortNameMap = externalLogic.getUserIdToSortNameMap(studentIdList);
         
         boolean contentReviewEnabled = assignment.isContentReviewEnabled() && contentReviewLogic.isContentReviewAvailable(assignment.getContextId());
         
         if (contentReviewEnabled) {
             populateReviewProperties(assignment, submissions);
         }
 
         for (AssignmentSubmission as : submissions) {
             Map submap = new HashMap();
             submap.put("studentName", studentIdSortNameMap.get(as.getUserId()));
             submap.put("studentId", as.getUserId());
 
             // submission info columns are not displayed for non-electronic assignments
             if (assignment.getSubmissionType() != AssignmentConstants.SUBMIT_NON_ELECTRONIC) {
                 if (as.getCurrentSubmissionVersion() != null && as.getCurrentSubmissionVersion().getSubmittedDate() != null){
                     submap.put("submittedDate", as.getCurrentSubmissionVersion().getSubmittedDate());
                     submap.put("submittedDateFormat", df.format(as.getCurrentSubmissionVersion().getSubmittedDate()));
                 } else {
                     // We're not addign this to the subject.
                 }
 
                 // set the textual representation of the submission status
                 String status = "";
                 int statusConstant = AssignmentConstants.SUBMISSION_NOT_STARTED;
                 if (as != null) {
                     statusConstant = submissionLogic.getSubmissionStatusConstantForCurrentVersion(
                             as.getCurrentSubmissionVersion(), assignment.getDueDate());
                     status = assignmentBundleLogic.getString(
                             "assignment2.assignment_grade-assignment.submission_status." + 
                             statusConstant);
                     submap.put("submissionStatus", status);
                 }
 
             }
 
             if (assignment.isGraded()) {
                 String grade = "";
                 GradeInformation gradeInfo = studentIdGradeInfoMap.get(as.getUserId());
                 if (gradeInfo != null) {
                     grade = gradeInfo.getGradebookGrade();
                 }
                 submap.put("grade", grade);
             }
 
             if (as.getCurrentSubmissionVersion() != null)  {
                 submap.put("feedbackReleased",as.getCurrentSubmissionVersion().isFeedbackReleased());
             }
             else {
                 submap.put("feedbackReleased", false);
             }
             
             if (contentReviewEnabled) {
                 // content review is enabled, so we need to figure out what to display for the 
                 // review column
                 AssignmentSubmissionVersion currVersion = as.getCurrentSubmissionVersion();
                 Set<SubmissionAttachment> submittedAttachSet = 
                     currVersion != null ? currVersion.getSubmissionAttachSet() : null;
                 if (submittedAttachSet != null && submittedAttachSet.size() > 0) {
                     // iterate through the attachments to see if any were reviewed
                     List<SubmissionAttachment> reviewedAttach = new ArrayList<SubmissionAttachment>();
                     
                     // get the informational text associated with the different options
                     String reportLinkInfo = assignmentBundleLogic.getString("assignment2.content_review.report_link");
                     String reviewPendingInfo = assignmentBundleLogic.getString("assignment2.content_review.pending.info");
                     String reviewPendingScoreDisplay = assignmentBundleLogic.getString("assignment2.content_review.pending.display");   
                     String reviewMultipleInfo = assignmentBundleLogic.getString("assignment2.content_review.multiple_reports");
                     
                     for (SubmissionAttachment attach : submittedAttachSet) {
                         if (attach.getProperties() != null) {
                             String status = (String)attach.getProperties().get(AssignmentConstants.PROP_REVIEW_STATUS);
                             if (status != null && !status.equals(AssignmentConstants.REVIEW_STATUS_NONE)) {
                                 reviewedAttach.add(attach);
                             }
                         }
                     }
                     
                     if (reviewedAttach.size() > 1) {
                         submap.put("reviewMultiple", true);
                         submap.put("reviewMultipleInfo", reviewMultipleInfo);
                     } else if (reviewedAttach.size() == 1) {
                         // we need to either display an error indicator
                         // or a score
                         SubmissionAttachment attach = reviewedAttach.get(0);
                         Map properties = attach.getProperties();
                         String status = (String)attach.getProperties().get(AssignmentConstants.PROP_REVIEW_STATUS);
                         if (status.equals(AssignmentConstants.REVIEW_STATUS_ERROR)) {
                             submap.put("reviewError", true);
                             Long statusErrorCode = (Long)attach.getProperties().get(AssignmentConstants.PROP_REVIEW_ERROR_CODE);
                             String errorMsg = contentReviewLogic.getErrorMessage(statusErrorCode);
                             submap.put("reviewErrorMsg", errorMsg);
                         } else if (status.equals(AssignmentConstants.REVIEW_STATUS_SUCCESS)) {
                             String score = (String)properties.get(AssignmentConstants.PROP_REVIEW_SCORE_DISPLAY);
                             String link = (String)properties.get(AssignmentConstants.PROP_REVIEW_URL);
                             submap.put("reviewScore", score);
                             submap.put("reviewScoreLink", link);
                             submap.put("reviewError", false);
                             submap.put("reviewPending", false);
                             submap.put("reviewScoreLinkInfo", reportLinkInfo);
                             
                             Integer scoreAsNum = (Integer)properties.get(AssignmentConstants.PROP_REVIEW_SCORE);
                             String styleClass = DisplayUtil.getCssClassForReviewScore(scoreAsNum);
                             submap.put("reviewScoreClass", styleClass);
                         } else if (status.equals(AssignmentConstants.REVIEW_STATUS_PENDING)) {
                             submap.put("reviewPending", true);
                             submap.put("reviewError", false);
                             submap.put("reviewScore", reviewPendingScoreDisplay);
                             submap.put("reviewPendingText", reviewPendingInfo);
                         }
                     } 
                 }
             }
 
             togo.add(submap);
         }
 
         String orderBy = null;
         Boolean ascending = null;
         if (search.getOrders() != null && search.getOrders().length > 0) {
             Order order = search.getOrders()[0];
             // We can sort by:
             // studentName, submittedDate, submissionStatus, grade, and feedbackReleased
             final String orderByComp = order.getProperty();
             orderBy = orderByComp;
             final boolean ascendingComp = order.ascending;
             ascending = ascendingComp;
             if (orderByComp.equals(STUDENT_NAME_PROP) || orderByComp.equals(SUBMITTED_DATE) ||
                     orderByComp.equals(SUBMISSION_STATUS) || 
                     (orderByComp.equals(SUBMISSION_GRADE) && assignment.isGraded()) ||
                     orderByComp.equals(SUBMISSION_FEEDBACK_RELEASED)) {
 
                 Collections.sort(togo, new Comparator() {
                     public int compare(Object o1, Object o2) {
                         Map m1, m2;
                         if (ascendingComp) {
                             m1 = (Map) o1;
                             m2 = (Map) o2;
                         }
                         else {
                             m2 = (Map) o1;
                             m1 = (Map) o2;
                         }
                         if (orderByComp.equals(SUBMISSION_GRADE)) {
                             boolean useDouble = true;
                             Double d1 = null;
                             Double d2 = null;
                             try {
                                d1 = Double.parseDouble((String) m1.get(orderByComp));
                                d2 = Double.parseDouble((String) m2.get(orderByComp));
                             } catch (NumberFormatException e) {
                                 useDouble = false;
                             }
                             if (d1 != null && d2 != null && useDouble) {
                                 return d1.compareTo(d2);
                             } 
                             else {
                                return m1.get(orderByComp).toString().compareTo(m2.get(orderByComp).toString());
                             }
                         }
                        // handle null data
                        if (m1.get(orderByComp) == null && m2.get(orderByComp) == null) {
                            return 0;
                        }
                        if (m1.get(orderByComp) == null && m2.get(orderByComp) != null) {
                            return -1;
                        }
                        if (m1.get(orderByComp) != null && m2.get(orderByComp) == null) {
                            return 1;
                        }
                         if (m1.get(orderByComp) instanceof Date) {
                             return ((Date)m1.get(orderByComp)).compareTo(((Date)m2.get(orderByComp)));
                         } 
                         else if (m1.get(orderByComp) instanceof Boolean) {
                             return ((Boolean)m1.get(orderByComp)).compareTo(((Boolean)m2.get(orderByComp)));
                         }
                         else {
                             return m1.get(orderByComp).toString().compareTo(m2.get(orderByComp).toString());
                         }
                     }});
             }
         }
 
         long start = (int) search.getStart();
         if (togo.size() == 0) {
             start = 0;
         }
         else if (start >= togo.size()) {
             start = togo.size() - 1;
         }
 
         long end = togo.size();
 
         if (search.getLimit() > 0) {
             end = start + search.getLimit();
             if (end > togo.size()) {
                 end = togo.size();
             }
         }
 
         requestGetter.getResponse().setHeader("x-asnn2-pageSize", search.getLimit()+"");
         requestGetter.getResponse().setHeader("x-asnn2-pageIndex", (start / search.getLimit())+"");
         
         if (placementId != null) {
             Session session = sessionManager.getCurrentSession();
             ToolSession toolSession = session.getToolSession(placementId);
             Map attr = new HashMap();
             attr.put(SUBMISSIONVIEW_SESSION_ATTR_PAGE_SIZE, search.getLimit());
             attr.put(SUBMISSIONVIEW_SESSION_ATTR_ORDER_BY, orderBy);
             attr.put(SUBMISSIONVIEW_SESSION_ATTR_ASCENDING, ascending);
             toolSession.setAttribute(SUBMISSIONVIEW_SESSION_ATTR, attr);
         }
         
         return togo.subList((int)start, (int)end);
     }
     
     private void populateReviewProperties(Assignment2 assign, List<AssignmentSubmission> submissions) {
         if (assign == null) {
             throw new IllegalArgumentException("Null assign passed to populateReviewProperties");
         }
         Map<String, Set<SubmissionAttachment>> studentIdAttachListMap = new HashMap<String, Set<SubmissionAttachment>>();
         
         if (submissions != null && !submissions.isEmpty()) {
             // collect all of the attachments for review
             List<SubmissionAttachment> attachToReview = new ArrayList<SubmissionAttachment>();
             for (AssignmentSubmission submission : submissions) {
                 AssignmentSubmissionVersion currVersion = submission.getCurrentSubmissionVersion();
                 if (currVersion != null && currVersion.getSubmissionAttachSet() != null) {
                     attachToReview.addAll(currVersion.getSubmissionAttachSet());
                 }
             }
             
             // populate the review properties on the attachments
             contentReviewLogic.populateReviewProperties(assign, attachToReview, true);
         }
     }
 
     public String[] getHandledOutputFormats() {
         // TODO Auto-generated method stub
         return null;
     }
 
     public String[] getHandledInputFormats() {
         // TODO Auto-generated method stub
         return null;
     }
 
 }
