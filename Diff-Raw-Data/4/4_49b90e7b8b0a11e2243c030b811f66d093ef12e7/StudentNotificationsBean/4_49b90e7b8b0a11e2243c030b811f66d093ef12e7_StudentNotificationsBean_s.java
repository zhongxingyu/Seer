 /**
  * This document is a part of the source code and related artifacts for
  * SMSystem.
  * www.apeironsol.com
  * Copyright © 2012 apeironsol
  */
 package com.apeironsol.need.notifications.portal;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.List;
 
 import javax.annotation.Resource;
 import javax.faces.application.FacesMessage;
 import javax.faces.context.FacesContext;
 import javax.inject.Named;
 
 import org.springframework.context.annotation.Scope;
 
 import com.apeironsol.need.academics.model.SectionExam;
 import com.apeironsol.need.academics.service.SectionExamService;
 import com.apeironsol.need.core.portal.AbstractTabbedBean;
 import com.apeironsol.need.core.portal.StudentBean;
 import com.apeironsol.need.notifications.model.BatchLog;
 import com.apeironsol.need.notifications.model.BatchLogMessage;
 import com.apeironsol.need.notifications.model.BranchNotification;
 import com.apeironsol.need.notifications.producer.util.BatchLogBuilder;
 import com.apeironsol.need.notifications.service.BatchLogMessageService;
 import com.apeironsol.need.notifications.service.BatchLogService;
 import com.apeironsol.need.notifications.service.BranchNotificationService;
 import com.apeironsol.need.notifications.service.NotificationService;
 import com.apeironsol.need.util.DateUtil;
 import com.apeironsol.need.util.comparator.BatchLogMessageComparator;
 import com.apeironsol.need.util.constants.BatchStatusConstant;
 import com.apeironsol.need.util.constants.NotificationLevelConstant;
 import com.apeironsol.need.util.constants.NotificationSubTypeConstant;
 import com.apeironsol.need.util.constants.NotificationTypeConstant;
 import com.apeironsol.need.util.portal.ViewUtil;
 
 /**
  * Managed bean for section notifications.
  * 
  * @author Pradeep
  */
 @Named
 @Scope(value = "session")
 public class StudentNotificationsBean extends AbstractTabbedBean {
 
 	/**
 	 * Unique serial version id for this class
 	 */
 	private static final long						serialVersionUID			= 1636561636879564270L;
 
 	@Resource
 	private StudentBean								studentBean;
 
 	/**
 	 * Notification sub type.
 	 */
 	private NotificationSubTypeConstant				notificationSubTypeConstant;
 
 	/**
 	 * Batch log messages for the section for selected batch log.
 	 */
 	private Collection<BatchLogMessage>				studentBatchLogMessages		= new ArrayList<BatchLogMessage>();
 
 	/**
 	 * Batch log message service.
 	 */
 	@Resource
 	private BatchLogMessageService					batchLogMessageService;
 
 	/**
 	 * Batch log service.
 	 */
 	@Resource
 	private BatchLogService							batchLogService;
 
 	/**
 	 * Batch log.
 	 */
 	private BatchLog								batchLog;
 
 	/**
 	 * Scheduled batch log.
 	 */
 	private BatchLog								scheduledBatchLog;
 
 	/**
 	 * Variable to decide what has to be displayed.
 	 */
 	private ViewAction								viewActionString			= ViewAction.VIEW_BATCH_LOG_MESSAGES;
 
 	/**
 	 * Variable to hole batch log message error message.
 	 */
 	private String									batchLogMessageErrorMessage;
 
 	/**
 	 * Variable to hold batch log message sent.
 	 */
 	private String									batchLogMessageSentMessage;
 
 	/**
 	 * Indicator to specify if batch logs has to be fetched form DB.
 	 */
 	private boolean									loadBatchLogMessagesFromDB	= false;
 
 	/**
 	 * Boolean to indicate if current scheduled batch has finished.
 	 */
 	private boolean									batchFinished				= true;
 
 	/**
 	 * Variable holding progress bar value indicating percentage of batch
 	 * finished.
 	 */
 	private int										progressBarIncrementor;
 
 	private NotificationTypeConstant				notificationTypeConstant;
 
 	/**
 	 * Notification sub type.
 	 */
 	private String									notificationText;
 
 	@Resource
 	private NotificationService						notificationService;
 
 	@Resource
 	private BranchNotificationService				branchNotificationService;
 
 	private Collection<BranchNotification>			branchNotifications;
 
 	private Collection<NotificationSubTypeConstant>	notificationSubTypeAvailable;
 
 	/**
 	 * Section Exam service.
 	 */
 	@Resource
 	private SectionExamService						sectionExamService;
 
 	private Collection<SectionExam>					sectionExams;
 
 	/**
 	 * Boolean to indicate if current scheduled batch has finished.
 	 */
 	private boolean									renderSectionExamIndicator	= false;
 
 	private SectionExam								selectedSectionExam;
 
 	/**
 	 * Enum class used for deciding what has to be displayed on screen.
 	 * 
 	 * @author pradeep
 	 * 
 	 */
 	public enum ViewAction {
 		VIEW_BATCH_LOG_MESSAGES, VIEW_SEND_NOTIFICATION;
 	}
 
 	/**
 	 * Default constructor.
 	 */
 	public StudentNotificationsBean() {
 	}
 
 	/**
 	 * On tab change event.
 	 */
 	@Override
 	public void onTabChange() {
 		this.viewActionString = ViewAction.VIEW_BATCH_LOG_MESSAGES;
 		this.loadBatchLogMessagesFromDB = true;
 		this.loadBranchNotification();
 		this.getBranchNotificationByNotificationType();
 	}
 
 	/**
 	 * Adds a message to faces context.
 	 * 
 	 * @param message
 	 *            message to display.
 	 */
 	private void addMessage(final FacesMessage message) {
 		FacesContext.getCurrentInstance().addMessage(null, message);
 	}
 
 	/**
 	 * Send notification to JMS queue.
 	 * 
 	 * @return
 	 */
 	public String sendNotification() {
 		if (this.notificationTypeConstant == null) {
 			ViewUtil.addMessage("Please select notification type.", FacesMessage.SEVERITY_ERROR);
 			return null;
 		} else if (this.notificationSubTypeConstant == null) {
 			ViewUtil.addMessage("Please select notifications sub type.", FacesMessage.SEVERITY_ERROR);
 			return null;
 		} else {
 			try {
 				if (this.notificationSubTypeConstant.isMessageRequired() && ((this.notificationText == null) || this.notificationText.trim().isEmpty())) {
 					ViewUtil.addMessage("Message required for this notification type.", FacesMessage.SEVERITY_ERROR);
 					return null;
 				}
 				this.scheduledBatchLog = new BatchLogBuilder().branch(this.sessionBean.getCurrentBranch())
 						.notificationLevelId(this.studentBean.getStudentAcademicYear().getId()).notificationTypeConstant(this.notificationTypeConstant)
 						.notificationLevelConstant(NotificationLevelConstant.STUDENT_ACADEMIC_YEAR)
						.notificationSubTypeConstant(this.notificationSubTypeConstant).messageToBeSent(this.notificationText)
						.attendanceDate(DateUtil.getSystemDate()).build();
 
 				this.scheduledBatchLog = this.notificationService.sendNotificationForStudent(this.studentBean.getStudentAcademicYear(), this.scheduledBatchLog);
 			} catch (final Exception e) {
 				final FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, e.getMessage(), e.getMessage());
 				this.addMessage(message);
 			}
 			this.setViewBatchLogMessages();
 			this.loadBatchLogMessagesFromDB = true;
 			this.batchFinished = false;
 			this.loadBatchLogMessagesByStudentAcademicYear();
 			this.progressBarIncrementor = 1;
 			this.batchFinished = false;
 		}
 		return null;
 	}
 
 	/**
 	 * @return the notificationSubTypeConstant
 	 */
 	public NotificationSubTypeConstant getNotificationSubTypeConstant() {
 		return this.notificationSubTypeConstant;
 	}
 
 	/**
 	 * @param notificationSubTypeConstant
 	 *            the notificationSubTypeConstant to set
 	 */
 	public void setNotificationSubTypeConstant(final NotificationSubTypeConstant notificationSubTypeConstant) {
 		this.notificationSubTypeConstant = notificationSubTypeConstant;
 	}
 
 	/**
 	 * Fetch batch log messages for batch log from database.
 	 */
 	public void loadBatchLogMessagesByStudentAcademicYear() {
 		if (this.loadBatchLogMessagesFromDB) {
 			this.setStudentBatchLogMessages(this.batchLogMessageService.findBatchLogMessagesByStudentAcademicYearId(this.studentBean.getStudentAcademicYear()
 					.getId()));
 			Collections.sort((List<BatchLogMessage>) this.getStudentBatchLogMessages(), new BatchLogMessageComparator(BatchLogMessageComparator.Order.ID));
 			this.loadBatchLogMessagesFromDB = false;
 		}
 	}
 
 	/**
 	 * @return the sectionBatchLogMessages
 	 */
 	public Collection<BatchLogMessage> getStudentBatchLogMessages() {
 		return this.studentBatchLogMessages;
 	}
 
 	/**
 	 * @param sectionBatchLogs
 	 *            the sectionBatchLogs to set
 	 */
 	public void setStudentBatchLogMessages(final Collection<BatchLogMessage> batchLogMessages) {
 		this.studentBatchLogMessages = batchLogMessages;
 	}
 
 	/**
 	 * @return the viewActionString
 	 */
 	public ViewAction getViewActionString() {
 		return this.viewActionString;
 	}
 
 	/**
 	 * @param viewActionString
 	 *            the viewActionString to set
 	 */
 	public void setViewActionString(final ViewAction viewActionString) {
 		this.viewActionString = viewActionString;
 	}
 
 	public String setViewBatchLogMessages() {
 		this.viewActionString = ViewAction.VIEW_BATCH_LOG_MESSAGES;
 		return null;
 	}
 
 	public String setViewSendNotification() {
 		this.viewActionString = ViewAction.VIEW_SEND_NOTIFICATION;
 		this.notificationTypeConstant = null;
 		this.notificationSubTypeConstant = null;
 		this.notificationText = null;
 		this.getBranchNotificationByNotificationType();
 		return null;
 	}
 
 	/**
 	 * @return the batchLog
 	 */
 	public BatchLog getBatchLog() {
 		return this.batchLog;
 	}
 
 	/**
 	 * @param batchLog
 	 *            the batchLog to set
 	 */
 	public void setBatchLog(final BatchLog batchLog) {
 		this.batchLog = batchLog;
 	}
 
 	/**
 	 * @return the batchLogMessageErrorMessage
 	 */
 	public String getBatchLogMessageErrorMessage() {
 		return this.batchLogMessageErrorMessage;
 	}
 
 	/**
 	 * @param batchLogMessageErrorMessage
 	 *            the batchLogMessageErrorMessage to set
 	 */
 	public void setBatchLogMessageErrorMessage(final String batchLogMessageErrorMessage) {
 		this.batchLogMessageErrorMessage = batchLogMessageErrorMessage;
 	}
 
 	/**
 	 * @return the batchLogMessageSentMessage
 	 */
 	public String getBatchLogMessageSentMessage() {
 		return this.batchLogMessageSentMessage;
 	}
 
 	/**
 	 * @param batchLogMessageSentMessage
 	 *            the batchLogMessageSentMessage to set
 	 */
 	public void setBatchLogMessageSentMessage(final String batchLogMessageSentMessage) {
 		this.batchLogMessageSentMessage = batchLogMessageSentMessage;
 	}
 
 	/**
 	 * @return the loadBatchLogMessagesFromDB
 	 */
 	public boolean isLoadBatchLogMessagesFromDB() {
 		return this.loadBatchLogMessagesFromDB;
 	}
 
 	/**
 	 * @param loadBatchLogMessagesFromDB
 	 *            the loadBatchLogMessagesFromDB to set
 	 */
 	public void setLoadBatchLogMessagesFromDB(final boolean loadBatchLogMessagesFromDB) {
 		this.loadBatchLogMessagesFromDB = loadBatchLogMessagesFromDB;
 	}
 
 	/**
 	 * Checks if the current batch has finished.
 	 */
 	public void checkBatchStopped() {
 		this.batchFinished = true;
 		if (this.scheduledBatchLog != null) {
 			this.scheduledBatchLog = this.batchLogService.findBatchLogById(this.scheduledBatchLog.getId());
 			if (BatchStatusConstant.CREATED.equals(this.scheduledBatchLog.getBatchStatusConstant())
 					|| BatchStatusConstant.DISTRIBUTED.equals(this.scheduledBatchLog.getBatchStatusConstant())) {
 				this.batchFinished = false;
 			} else {
 				this.setBatchLog(this.scheduledBatchLog);
 				this.scheduledBatchLog = null;
 				this.setStudentBatchLogMessages(this.batchLogMessageService.findBatchLogMessagesByStudentAcademicYearId(this.studentBean
 						.getStudentAcademicYear().getId()));
 			}
 		}
 	}
 
 	/**
 	 * Listener for Poll which checks for every few seconds to check if batch
 	 * has finished.
 	 */
 	public void pollListener() {
 		this.checkBatchStopped();
 	}
 
 	/**
 	 * Returns true if batch has finished executing.
 	 * 
 	 * @return
 	 */
 	public boolean isBatchFinished() {
 		return this.batchFinished;
 	}
 
 	/**
 	 * Interval at which pool should check for status of scheduled batch.
 	 * 
 	 * @return
 	 */
 	public int getBatchPollInterval() {
 		return 15;
 	}
 
 	/**
 	 * Returns progress bar value indicating percentage of batch finished.
 	 * 
 	 * @return
 	 */
 	public Integer getProgressBarValue() {
 		int progress = this.progressBarIncrementor;
 		if (this.scheduledBatchLog != null) {
 			if (BatchStatusConstant.FINISHED.equals(this.scheduledBatchLog.getBatchStatusConstant())) {
 				progress = 100;
 				this.batchFinished = true;
 			} else {
 				final long totalElements = this.scheduledBatchLog.getNrElements();
 				final long totalProcessed = this.scheduledBatchLog.getNrElementsProcessed();
 				progress = totalElements > 0 ? Long.valueOf((totalProcessed * 100) / totalElements).intValue() : 100;
 			}
 
 		} else {
 			progress = 100;
 		}
 		if (progress == 100) {
 			this.batchFinished = true;
 		}
 		return progress == 0 ? this.progressBarIncrementor++ : progress;
 	}
 
 	/**
 	 * @return the notificationTypeConstant
 	 */
 	public NotificationTypeConstant getNotificationTypeConstant() {
 		return this.notificationTypeConstant;
 	}
 
 	/**
 	 * @param notificationTypeConstant
 	 *            the notificationTypeConstant to set
 	 */
 	public void setNotificationTypeConstant(final NotificationTypeConstant notificationTypeConstant) {
 		this.notificationTypeConstant = notificationTypeConstant;
 	}
 
 	/**
 	 * @return the notificationText
 	 */
 	public String getNotificationText() {
 		return this.notificationText;
 	}
 
 	/**
 	 * @param notificationText
 	 *            the notificationText to set
 	 */
 	public void setNotificationText(final String notificationText) {
 		if ((notificationText == null) || notificationText.trim().isEmpty()) {
 			this.notificationText = null;
 		} else {
 			this.notificationText = notificationText;
 		}
 	}
 
 	public String handleNotificationTypeChange() {
 		this.getBranchNotificationByNotificationType();
 		return null;
 	}
 
 	public void getBranchNotificationByNotificationType() {
 		this.notificationSubTypeAvailable = new ArrayList<NotificationSubTypeConstant>();
 		for (final BranchNotification branchNotification : this.branchNotifications) {
 			if (!branchNotification.getNotificationSubType().isImplicitMessage()) {
 				if (NotificationTypeConstant.EMAIL_NOTIFICATION.equals(this.notificationTypeConstant) && (null != branchNotification.getEmailIndicator())
 						&& branchNotification.getEmailIndicator()) {
 					this.notificationSubTypeAvailable.add(branchNotification.getNotificationSubType());
 				} else if (NotificationTypeConstant.SMS_NOTIFICATION.equals(this.notificationTypeConstant) && (null != branchNotification.getSmsIndicator())
 						&& branchNotification.getSmsIndicator()) {
 					this.notificationSubTypeAvailable.add(branchNotification.getNotificationSubType());
 				} else if ((null == this.notificationTypeConstant)
 						&& (((null != branchNotification.getSmsIndicator()) && branchNotification.getSmsIndicator()) || ((null != branchNotification
 								.getEmailIndicator()) && branchNotification.getEmailIndicator()))) {
 					this.notificationSubTypeAvailable.add(branchNotification.getNotificationSubType());
 				}
 			}
 		}
 	}
 
 	public void loadBranchNotification() {
 		this.setBranchNotifications(this.branchNotificationService.findBranchNotificationsByBranchId(this.sessionBean.getCurrentBranch().getId()));
 	}
 
 	/**
 	 * @return the branchNotifications
 	 */
 	public Collection<BranchNotification> getBranchNotifications() {
 		return this.branchNotifications;
 	}
 
 	/**
 	 * @param branchNotifications
 	 *            the branchNotifications to set
 	 */
 	public void setBranchNotifications(final Collection<BranchNotification> branchNotifications) {
 		this.branchNotifications = branchNotifications;
 	}
 
 	/**
 	 * @return the notificationSubTypeAvailable
 	 */
 	public Collection<NotificationSubTypeConstant> getNotificationSubTypeAvailable() {
 		return this.notificationSubTypeAvailable;
 	}
 
 	/**
 	 * @param notificationSubTypeAvailable
 	 *            the notificationSubTypeAvailable to set
 	 */
 	public void setNotificationSubTypeAvailable(final Collection<NotificationSubTypeConstant> notificationSubTypeAvailable) {
 		this.notificationSubTypeAvailable = notificationSubTypeAvailable;
 	}
 
 	/**
 	 * Event listener for Notification sub type.
 	 * 
 	 * @return
 	 */
 	public String handleNotificationSubTypeChange() {
 		this.selectedSectionExam = null;
 		this.notificationText = null;
 		if (this.getSectionExams() != null) {
 			this.getSectionExams().clear();
 		}
 		if (NotificationSubTypeConstant.EXAM_ABSENT_NOTIFICATION.equals(this.notificationSubTypeConstant)
 				|| NotificationSubTypeConstant.EXAM_SCHEDULE_NOTIFICATION.equals(this.notificationSubTypeConstant)) {
 			this.setSectionExams(this.sectionExamService.findSectionExamsBySectionId(this.studentBean.getStudentSection().getSection().getId()));
 			this.renderSectionExamIndicator = true;
 		} else {
 			this.renderSectionExamIndicator = false;
 		}
 		return null;
 	}
 
 	/**
 	 * @return the sectionExams
 	 */
 	public Collection<SectionExam> getSectionExams() {
 		return this.sectionExams;
 	}
 
 	/**
 	 * @param sectionExams
 	 *            the sectionExams to set
 	 */
 	public void setSectionExams(final Collection<SectionExam> sectionExams) {
 		this.sectionExams = sectionExams;
 	}
 
 	/**
 	 * @return the renderSectionExamIndicator
 	 */
 	public boolean isRenderSectionExamIndicator() {
 		return this.renderSectionExamIndicator;
 	}
 
 	/**
 	 * @param renderSectionExamIndicator
 	 *            the renderSectionExamIndicator to set
 	 */
 	public void setRenderSectionExamIndicator(final boolean renderSectionExamIndicator) {
 		this.renderSectionExamIndicator = renderSectionExamIndicator;
 	}
 
 	/**
 	 * @return the selectedsectionExam
 	 */
 	public SectionExam getSelectedSectionExam() {
 		return this.selectedSectionExam;
 	}
 
 	/**
 	 * @param selectedsectionExam
 	 *            the selectedsectionExam to set
 	 */
 	public void setSelectedSectionExam(final SectionExam selectedSectionExam) {
 		this.selectedSectionExam = selectedSectionExam;
 	}
 
 }
