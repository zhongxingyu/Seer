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
 
 package org.sakaiproject.assignment2.model;
 
 import java.util.Date;
 import java.util.HashSet;
 import java.util.Set;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  * The AssignmentSubmissionVersion object
  * 
  * @author <a href="mailto:wagnermr@iupui.edu">michelle wagner</a>
  */
 public class AssignmentSubmissionVersion implements FeedbackVersion {
 	
 	private Long id;
 	private AssignmentSubmission assignmentSubmission;
 	private int submittedVersionNumber;
 	private Date submittedDate;
 	private Date feedbackReleasedDate;
 	private String annotatedText;
 	private String feedbackNotes;
 	private String submittedText;
 	private boolean draft;
 	private String createdBy;
 	private Date createdDate;
 	private String modifiedBy;
 	private Date modifiedDate;
 	private String lastFeedbackSubmittedBy;
 	private Date lastFeedbackDate;
	private Date studentSaveDate;
 	private Date feedbackLastViewed;
 	private int optimisticVersion;
 	private Set<FeedbackAttachment> feedbackAttachSet;
 	private Set<SubmissionAttachment> submissionAttachSet;
 
 	public AssignmentSubmissionVersion() {
 	}
 	
 	public Long getId() {
 		return id;
 	}
 
 	public void setId(Long id) {
 		this.id = id;
 	}
 	
 	/**
 	 * 
 	 * @return the parent submission record associated with this version
 	 */
 	public AssignmentSubmission getAssignmentSubmission() {
 		return assignmentSubmission;
 	}
 
 	/**
 	 * set the parent submission record associated with this version
 	 * @param assignmentSubmission
 	 */
 	public void setAssignmentSubmission(AssignmentSubmission assignmentSubmission) {
 		this.assignmentSubmission = assignmentSubmission;
 	}
 	
 	/**
 	 * 
 	 * @return which version is this? is this the second version of this submission?
 	 * keeps track of which version this is in the submission history. Version 0
 	 * is reserved for instructor feedback before a submission. All student submitted
 	 * versions start at 1.
 	 */
 	public int getSubmittedVersionNumber() {
 		return submittedVersionNumber; 
 	}
 	
 	/**
 	 * which version is this? is this the second version of this submission?
 	 * keeps track of which version this is in the submission history. Version 0
 	 * is reserved for instructor feedback before a submission. All student submitted
 	 * versions start at 1.
 	 * @param submittedVersionNumber
 	 */
 	public void setSubmittedVersionNumber(int submittedVersionNumber) {
 		this.submittedVersionNumber = submittedVersionNumber;
 	}
 	
 	/* ************* Information related to the student's submission ************* */
 	
 	/**
 	 * @return Set the date and time the assignment was submitted. Null if this has not
 	 *  yet been submitted for feedback yet.
 	 */
 	public Date getSubmittedDate() {
 		return submittedDate;
 	}
 	
 	/**
 	 * Set the date and time the assignment was submitted. Null if this has not
 	 *  yet been submitted for feedback yet.
 	 * @param submittedDate
 	 */
 	public void setSubmittedDate(Date submittedDate) {
 		this.submittedDate = submittedDate;
 	}
 	
 	/**
 	 * 
 	 * @return the text of the submission
 	 */
 	public String getSubmittedText() {
 		return submittedText;
 	}
 
 	/**
 	 * set the text of the submission
 	 * @param submittedText
 	 */
 	public void setSubmittedText(String submittedText) {
 		this.submittedText = submittedText;
 	}
 	
 	/**
 	 * 
 	 * @return true if the submitter has started working on the submission
 	 * but has not yet submitted it for review
 	 */
 	public boolean isDraft() {
 		return draft;
 	}
 
 	/**
 	 * set the draft status
 	 * @param draft
 	 */
 	public void setDraft(boolean draft) {
 		this.draft = draft;
 	}
 	
 	/**
 	 * 
 	 * @return the SubmissionAttachments associated with this
 	 * submission version
 	 */
 	public Set<SubmissionAttachment> getSubmissionAttachSet() {
 		return submissionAttachSet;
 	}
 
 	/**
 	 * 
 	 * @param submissionAttachSet
 	 * the SubmissionAttachments associated with this
 	 * submission version
 	 */
 	public void setSubmissionAttachSet(
 			Set<SubmissionAttachment> submissionAttachSet) {
 		this.submissionAttachSet = submissionAttachSet;
 	}
 	
 	/* ************* Information related to the feedback provided for this version ************* */
 	
 	/**
 	 * 
 	 * @return text composed of the submission with grader-added annotation
 	 */
 	public String getAnnotatedText() {
 		return annotatedText;
 	}
 
 	/**
 	 * set the text composed of the inline submission with grader-added annotation
 	 * @param annotatedText
 	 */
 	public void setAnnotatedText(String annotatedText) {
 		this.annotatedText = annotatedText;
 	}
 
 	/**
 	 * Additional feedback comments provided by the "grader"
 	 * @return feedbackNotes
 	 */
 	public String getFeedbackNotes() {
 		return feedbackNotes;
 	}
 
 	/**
 	 * Additional feedback comments provided by the "grader"
 	 * @param feedbackNotes
 	 */
 	public void setFeedbackNotes(String feedbackNotes) {
 		this.feedbackNotes = feedbackNotes;
 	}
 
 	/**
 	 * 
 	 * @return the Date and time feedback for this version was released to the submitter.
 	 */
 	public Date getFeedbackReleasedDate() {
 		return feedbackReleasedDate;
 	}
 
 	/**
 	 * set the Date and time feedback for this version was released to the submitter. 
 	 * @param releasedDate
 	 */
 	public void setFeedbackReleasedDate(Date releasedDate) {
 		this.feedbackReleasedDate = releasedDate;
 	}
 	
 	/**
 	 * 
 	 * @return the FeedbackAttachments associated with this submission
 	 * version
 	 */
 	public Set<FeedbackAttachment> getFeedbackAttachSet() {
 		return feedbackAttachSet;
 	}
 
 	/**
 	 * 
 	 * @param feedbackAttachSet
 	 * the FeedbackAttachments associated with this submission version
 	 */
 	public void setFeedbackAttachSet(
 			Set<FeedbackAttachment> feedbackAttachSet) {
 		this.feedbackAttachSet = feedbackAttachSet;
 	}
 	
 	/**
 	 * 
 	 * @return the userid who last submitted feedback on this version
 	 */
 	public String getLastFeedbackSubmittedBy() {
 		return lastFeedbackSubmittedBy;
 	}
 
 	/**
 	 *  the userid who last submitted feedback on this version
 	 * @param lastFeedbackSubmittedBy
 	 */
 	public void setLastFeedbackSubmittedBy(String lastFeedbackSubmittedBy) {
 		this.lastFeedbackSubmittedBy = lastFeedbackSubmittedBy;
 	}
 
 	/**
 	 * 
 	 * @return the date and time that the feedback for this version was last updated
 	 */
 	public Date getLastFeedbackDate() {
 		return lastFeedbackDate;
 	}
 
 	/**
 	 * the date and time that the feedback for this version was last updated
 	 * @param lastFeedbackDate
 	 */
 	public void setLastFeedbackDate(Date lastFeedbackDate) {
 		this.lastFeedbackDate = lastFeedbackDate;
 	}
 	
 	/**
 	 * 
	 * @return the date and time the student last saved this version 
     * (or when it was last auto-saved while student was working on it)
	 */
	public Date getStudentSaveDate()
    {
        return studentSaveDate;
    }

	/**
	 * the date and time the student last saved this version 
     * (or when it was last auto-saved while student was working on it)
	 * @param studentSaveDate
	 */
    public void setStudentSaveDate(Date studentSaveDate)
    {
        this.studentSaveDate = studentSaveDate;
    }

    /**
	 * 
 	 * @return the date and time this feedback was last viewed by this student
 	 */
 	public Date getFeedbackLastViewed()
 	{
 		return feedbackLastViewed;
 	}
 
 	/**
 	 * the date and time this feedback was last viewed by this student
 	 * @param feedbackLastViewed
 	 */
 	public void setFeedbackLastViewed(Date feedbackLastViewed)
 	{
 		this.feedbackLastViewed = feedbackLastViewed;
 	}
 	
 	/* ************* Other information ************* */
 
 	/**
 	 * 
 	 * @return the userId of the person who created this version
 	 */
 	public String getCreatedBy() {
 		return createdBy;
 	}
 
 	/**
 	 * set the userId of the person who created this version
 	 * @param createdBy
 	 */
 	public void setCreatedBy(String createdBy) {
 		this.createdBy = createdBy;
 	}
 	
 	/**
 	 * 
 	 * @return the date and time this version was created
 	 */
 	public Date getCreatedDate() {
 		return createdDate;
 	}
 
 	/**
 	 * the date and time this version was created
 	 * @param createdDate
 	 */
 	public void setCreatedDate(Date createdDate) {
 		this.createdDate = createdDate;
 	}
 	
 	/**
 	 * 
 	 * @return the userId of the person who made the last modification to
 	 * the submission
 	 */
 	public String getModifiedBy() {
 		return modifiedBy;
 	}
 
 	/**
 	 * set the userId of the person who made the last modification to
 	 * the submission
 	 * @param modifiedBy
 	 */
 	public void setModifiedBy(String modifiedBy) {
 		this.modifiedBy = modifiedBy;
 	}
 
 	/**
 	 * 
 	 * @return the date and time this version was last modified
 	 */
 	public Date getModifiedDate() {
 		return modifiedDate;
 	}
 
 	/**
 	 * set the date and time this version was last modified
 	 * @param modifiedDate
 	 */
 	public void setModifiedDate(Date modifiedDate) {
 		this.modifiedDate = modifiedDate;
 	}
 	
     /**
      * 
      * @return version stored for hibernate's automatic optimistic concurrency control.
      * this is not related to any of the submission version data for assignment2
      */
     public int getOptimisticVersion() {
     	return optimisticVersion;
     }
     
     /**
      * version stored for hibernate's automatic optimistic concurrency control.
      * this is not related to any of the submission version data for assignment2
      * @param optimisticVersion
      */
     public void setOptimisticVersion(int optimisticVersion) {
     	this.optimisticVersion = optimisticVersion;
     }
 	
 	// CONVENIENCE METHODS
 	
 	/**
 	 * 
 	 * @return formatted text composed of the inline submission with grader-added annotation
 	 */
 	public String getAnnotatedTextFormatted() {	
 		if (annotatedText == null) {
 			return "";
 		}
     	Pattern p = Pattern.compile("\\{\\{([^\\}]+|\\}(?!\\}))\\}\\}");
     	Matcher m = p.matcher(annotatedText);
     	StringBuffer sb = new StringBuffer();
     	while(m.find()){
     		m.appendReplacement(sb, "<span class=\"highlight\">$1</span>");
     	}
     	m.appendTail(sb);
 		return sb.toString();
 	}
 	
 	public String[] getFeedbackAttachmentRefs()
 	{
 		if (feedbackAttachSet == null) { return new String[]{}; }
 		String[] refs = new String[feedbackAttachSet.size()];   /// HEY FOR FUTURE RYAN _ THIS IS NULL, PROLLY THE OTP GETTER
 		int i = 0;
 		for (FeedbackAttachment fa : feedbackAttachSet) {
 			refs[i++] = fa.getAttachmentReference();
 		}
 		return refs;
 	}
 
 	public void setFeedbackAttachmentRefs(String[] attachmentRefs)
 	{
 		Set<FeedbackAttachment> set = new HashSet<FeedbackAttachment>();
 		for (int i = 0; i < attachmentRefs.length; i++) {
 			if (attachmentRefs[i] != null && !attachmentRefs[i].equals("")) {
 				FeedbackAttachment fa = new FeedbackAttachment();
 				fa.setSubmissionVersion(this);
 				fa.setAttachmentReference(attachmentRefs[i]);
 				set.add(fa);
 			}
 		}
 		this.feedbackAttachSet = set;
 	}
 	
 	public String[] getSubmittedAttachmentRefs()
 	{
 		if (submissionAttachSet == null) { return new String[]{}; }
 		String[] refs = new String[submissionAttachSet.size()];
 		int i = 0;
 		for (SubmissionAttachment sa : submissionAttachSet) {
 			refs[i++] = sa.getAttachmentReference();
 		}
 		return refs;
 	}
 
 	public void setSubmittedAttachmentRefs(String[] attachmentRefs)
 	{
 		Set<SubmissionAttachment> set = new HashSet<SubmissionAttachment>();
 		for (int i = 0; i < attachmentRefs.length; i++) {
 			if (attachmentRefs[i] != null && !attachmentRefs[i].equals("")) {
 				SubmissionAttachment sa = new SubmissionAttachment();
 				sa.setSubmissionVersion(this);
 				sa.setAttachmentReference(attachmentRefs[i]);
 				set.add(sa);
 			}
 		}
 		this.submissionAttachSet = set;
 	}
 	
 	/**
 	 * 
 	 * @return true if the submitter has read the most recent feedback
 	 * for this version
 	 */
 	public boolean isFeedbackRead() {
 		boolean feedbackRead = false;
 		if (lastFeedbackDate != null && feedbackLastViewed != null
 				&& lastFeedbackDate.before(feedbackLastViewed)) {
 			feedbackRead = true;
 		}
 
 		return feedbackRead;
 	}
 	
 	/**
 	 * 
 	 * @return true if the feedback for this version has been released to the student
 	 */
 	public boolean isFeedbackReleased() {
 	    boolean feedbackReleased = false;
 	    if (feedbackReleasedDate != null && feedbackReleasedDate.before(new Date())) {
 	        feedbackReleased = true;
 	    }
 	    
 	    return feedbackReleased;
 	}
 }
