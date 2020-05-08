 // $codepro.audit.disable com.instantiations.assist.eclipse.analysis.audit.rule.effectivejava.alwaysOverridetoString.alwaysOverrideToString, com.instantiations.assist.eclipse.analysis.deserializeabilitySecurity, com.instantiations.assist.eclipse.analysis.disallowReturnMutable, com.instantiations.assist.eclipse.analysis.enforceCloneableUsageSecurity
 /*******************************************************************************
  * Copyright (c) 2010 Ericsson Research Canada
  * 
  * All rights reserved. This program and the accompanying materials are
  * made available under the terms of the Eclipse Public License v1.0 which
  * accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Description:
  * 
  * This class provides general utility methods used in the UI implementation
  * 
  * Contributors:
  *   Sebastien Dubois - Created for Mylyn Review R4E project
  *   
  ******************************************************************************/
 
 package org.eclipse.mylyn.reviews.r4e.ui.internal.utils;
 
 import java.text.DecimalFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 import java.util.List;
 import java.util.TimeZone;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.jface.dialogs.ErrorDialog;
 import org.eclipse.jface.text.IRegion;
 import org.eclipse.jface.text.TextSelection;
 import org.eclipse.mylyn.reviews.notifications.core.IMeetingData;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EDelta;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EFileContext;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EFileVersion;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EItem;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EParticipant;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EReviewComponent;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EReviewType;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4ETextPosition;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EUserRole;
 import org.eclipse.mylyn.reviews.r4e.core.model.serial.impl.OutOfSyncException;
 import org.eclipse.mylyn.reviews.r4e.core.model.serial.impl.ResourceHandlingException;
 import org.eclipse.mylyn.reviews.r4e.ui.Activator;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.editors.R4ECompareEditorInput;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.editors.R4EFileEditorInput;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.editors.R4EFileRevisionEditorInput;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.model.R4EUIAnomalyBasic;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.model.R4EUIAnomalyContainer;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.model.R4EUIContent;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.model.R4EUIFileContext;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.model.R4EUIModelController;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.model.R4EUIReviewBasic;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.model.R4EUIReviewItem;
 import org.eclipse.ui.IEditorInput;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.texteditor.ITextEditor;
 
 /**
  * @author lmcdubo
  * @version $Revision: 1.0 $
  */
 public class MailServicesProxy {
 
 	// ------------------------------------------------------------------------
 	// Constants
 	// ------------------------------------------------------------------------
 
 	/**
 	 * Field LINE_FEED_MSG_PART.
 	 */
 	private static final String LINE_FEED_MSG_PART = System.getProperty("line.separator");
 
 	/**
 	 * Field TAB_MSG_PART. (value is ""\t"")
 	 */
 	private static final String TAB_MSG_PART = "\t";
 
 	/**
 	 * Field SUBJECT_MSG_HEADER. (value is "" Review "")
 	 */
 	private static final String SUBJECT_MSG_HEADER = " Review ";
 
 	/**
 	 * Field INTRO_MSG_BODY. (value is ""Hi,"")
 	 */
 	private static final String INTRO_MSG_BODY = "Hi,";
 
 	/**
 	 * Field OUTRO_MSG_BODY. (value is ""Best Regards,"")
 	 */
 	private static final String OUTRO_MSG_BODY = "Best Regards,";
 
 	/**
 	 * Field ITEMS_READY_MSG_BODY. (value is ""The following Review Item(s) and Files are Ready for you to Review"")
 	 */
 	private static final String ITEMS_READY_MSG_BODY = "The following Review Item(s) and Files are Ready for you to Review";
 
 	/**
 	 * Field MEETING_REQUEST_MSG_BODY. (value is ""This invitation is for the decision phase." + LINE_FEED_MSG_PART +
 	 * "Please review the included items prior to the meeting."")
 	 */
 	private static final String MEETING_REQUEST_MSG_BODY = "This invitation is for the decision phase."
 			+ LINE_FEED_MSG_PART + "Please review the included items prior to the meeting.";
 
 	/**
 	 * Field ADDED_ITEMS_MSG_BODY. (value is ""The following Review Item(s) and Files have been Added." +
 	 * LINE_FEED_MSG_PART + "Please Refresh your Review if it is currently Open"")
 	 */
 	private static final String ADDED_ELEMENTS_MSG_BODY = "The following Review Element(s) have been Added."
 			+ LINE_FEED_MSG_PART + "Please Refresh your Review if it is currently Open";
 
 	/**
 	 * Field REMOVED_ITEMS_MSG_BODY. (value is ""The following Review Item(s) and Files have been Removed." +
 	 * LINE_FEED_MSG_PART + "Please Refresh your Review if it is currently Open"")
 	 */
 	private static final String REMOVED_ELEMENTS_MSG_BODY = "The following Element(s) have been Removed."
 			+ LINE_FEED_MSG_PART + "Please Refresh your Review if it is currently Open";
 
 	/**
 	 * Field PROGRESS_MESSAGE. (value is ""Progress Update: " + LINE_FEED_MSG_PART")
 	 */
 	private static final String PROGRESS_MESSAGE = "Progress Update: " + LINE_FEED_MSG_PART;
 
 	/**
 	 * Field COMPLETION_MESSAGE. (value is ""I have Completed this Review, see Details below: " + LINE_FEED_MSG_PART")
 	 */
 	private static final String COMPLETION_MESSAGE = "I have Completed this Review, see Details below: "
 			+ LINE_FEED_MSG_PART;
 
 	/**
 	 * Field QUESTION_MSG_BODY. (value is ""I have a Question concerning the Following "")
 	 */
 	private static final String QUESTION_MSG_BODY = "I have a Question concerning the Following ";
 
 	/**
 	 * Field DEFAULT_MEETING_DURATION. (value is "60")
 	 */
 	private static final Integer DEFAULT_MEETING_DURATION = new Integer(60);
 
 	/**
 	 * Field DEFAULT_MEETING_LOCATION. (value is """")
 	 */
 	private static final String DEFAULT_MEETING_LOCATION = "";
 
 	// ------------------------------------------------------------------------
 	// Methods
 	// ------------------------------------------------------------------------
 
 	//Notifications
 
 	/**
 	 * Method sendItemsReadyNotification
 	 * 
 	 * @throws CoreException
 	 * @throws ResourceHandlingException
 	 */
 	public static void sendItemsReadyNotification() throws CoreException, ResourceHandlingException {
 		if (null != R4EUIModelController.getMailConnector()) {
 			final String[] messageDestinations = createItemsUpdatedDestinations();
 			final String messageSubject = createSubject() + " - Items Ready for Review";
 			final String messageBody = createItemsReadyNotificationMessage(false);
 			sendMessage(messageDestinations, messageSubject, messageBody);
 		} else {
 			showNoEmailConnectorDialog();
 		}
 	}
 
 	/**
 	 * Method sendItemsAddedNotification
 	 * 
 	 * @param aAddedElements
 	 *            List<R4EReviewComponent>
 	 * @throws CoreException
 	 * @throws ResourceHandlingException
 	 */
 	public static void sendItemsAddedNotification(List<R4EReviewComponent> aAddedElements) throws CoreException,
 			ResourceHandlingException {
 		if (null != R4EUIModelController.getMailConnector()) {
 			final String[] messageDestinations = createItemsUpdatedDestinations();
 			final String messageSubject = createSubject() + " - Items Added for Review";
 			final String messageBody = createUpdatedItemsNotificationMessage(aAddedElements, true);
 			sendMessage(messageDestinations, messageSubject, messageBody);
 		} else {
 			showNoEmailConnectorDialog();
 		}
 	}
 
 	/**
 	 * Method sendItemsRemovedNotification
 	 * 
 	 * @param aRemovedElements
 	 *            List<R4EReviewComponent>
 	 * @throws CoreException
 	 * @throws ResourceHandlingException
 	 */
 	public static void sendItemsRemovedNotification(List<R4EReviewComponent> aRemovedElements) throws CoreException,
 			ResourceHandlingException {
 		if (null != R4EUIModelController.getMailConnector()) {
 			final String[] messageDestinations = createItemsUpdatedDestinations();
 			final String messageSubject = createSubject() + " - Items Removed from Review";
 			final String messageBody = createUpdatedItemsNotificationMessage(aRemovedElements, false);
 			sendMessage(messageDestinations, messageSubject, messageBody);
 		} else {
 			showNoEmailConnectorDialog();
 		}
 	}
 
 	/**
 	 * Method sendProgressNotification
 	 * 
 	 * @throws CoreException
 	 * @throws ResourceHandlingException
 	 */
 	public static void sendProgressNotification() throws CoreException, ResourceHandlingException {
 		if (null != R4EUIModelController.getMailConnector()) {
 			final String[] messageDestinations = createProgressDestinations();
 			final String messageSubject = createSubject() + " - Participant Progress";
 			final String messageBody = createProgressNotification(PROGRESS_MESSAGE);
 			sendMessage(messageDestinations, messageSubject, messageBody);
 		} else {
 			showNoEmailConnectorDialog();
 		}
 	}
 
 	/**
 	 * Method sendCompletionNotification
 	 * 
 	 * @throws CoreException
 	 * @throws ResourceHandlingException
 	 */
 	public static void sendCompletionNotification() throws CoreException, ResourceHandlingException {
 		if (null != R4EUIModelController.getMailConnector()) {
 			final String[] messageDestinations = createProgressDestinations();
 			final String messageSubject = createSubject() + " - Participant Progress (Completed)";
 			final String messageBody = createProgressNotification(COMPLETION_MESSAGE);
 			sendMessage(messageDestinations, messageSubject, messageBody);
 		} else {
 			showNoEmailConnectorDialog();
 		}
 	}
 
 	/**
 	 * Method sendQuestion
 	 * 
 	 * @param aSource
 	 *            Object
 	 * @throws CoreException
 	 * @throws ResourceHandlingException
 	 */
 	public static void sendQuestion(Object aSource) throws CoreException, ResourceHandlingException {
 		if (null != R4EUIModelController.getMailConnector()) {
 			String[] messageDestinations = null;
 			if (aSource instanceof R4EUIAnomalyBasic) {
 				messageDestinations = createAnomalyCreatorDestination((R4EUIAnomalyBasic) aSource);
 			} else {
 				messageDestinations = createQuestionDestinations();
 			}
 			final String messageSubject = createSubject() + " - Question regarding review ";
 			final String messageBody = createQuestionMessage(aSource);
 			sendMessage(messageDestinations, messageSubject, messageBody);
 		} else {
 			showNoEmailConnectorDialog();
 		}
 	}
 
 	/**
 	 * Method sendMessage
 	 * 
 	 * @param aDestinations
 	 *            String[]
 	 * @param aSubject
 	 *            String
 	 * @param aBody
 	 *            String
 	 * @throws CoreException
 	 * @throws ResourceHandlingException
 	 */
 	public static void sendMessage(String[] aDestinations, String aSubject, String aBody) throws CoreException,
 			ResourceHandlingException {
 		final String originatorEmail = R4EUIModelController.getActiveReview()
 				.getParticipant(R4EUIModelController.getReviewer(), false)
 				.getEmail();
 		R4EUIModelController.getMailConnector().sendEmailGraphical(originatorEmail, aDestinations, aSubject, aBody,
 				null, null);
 	}
 
 	/**
 	 * Method createSubject
 	 * 
 	 * @return String
 	 */
 	private static String createSubject() {
 		final StringBuilder subject = new StringBuilder();
 		subject.append("[r4e-mail] ");
 		subject.append(SUBJECT_MSG_HEADER);
 		subject.append(R4EUIModelController.getActiveReview().getName());
 		return subject.toString();
 	}
 
 	/**
 	 * Method createItemsUpdatedDestinations
 	 * 
 	 * @return String[]
 	 */
 	private static String[] createItemsUpdatedDestinations() {
 		final ArrayList<String> destinations = new ArrayList<String>();
 		final List<R4EParticipant> participants = R4EUIModelController.getActiveReview().getParticipants();
 		for (R4EParticipant participant : participants) {
			if (null != participant.getEmail()) {
 				//All participants should receive this email
 				destinations.add(participant.getEmail());
 			}
 		}
 		return destinations.toArray(new String[destinations.size()]);
 	}
 
 	/**
 	 * Method createProgressDestinations
 	 * 
 	 * @return String[]
 	 */
 	private static String[] createProgressDestinations() {
 		final ArrayList<String> destinations = new ArrayList<String>();
 		final List<R4EParticipant> participants = R4EUIModelController.getActiveReview().getParticipants();
 		for (R4EParticipant participant : participants) {
 			if (null != participant.getEmail()) {
 				if (!(R4EUIModelController.getActiveReview().getReview().getType().equals(R4EReviewType.R4E_REVIEW_TYPE_FORMAL))) {
					destinations.add(participant.getEmail());
 				} else {
 					//If this is a formal review, only send mail if we have the proper role
					if (participant.getRoles().contains(R4EUserRole.R4E_ROLE_LEAD)
							|| participant.getRoles().contains(R4EUserRole.R4E_ROLE_ORGANIZER)
							|| participant.getRoles().contains(R4EUserRole.R4E_ROLE_AUTHOR)) {
 						destinations.add(participant.getEmail());
 					}
 				}
 			}
 		}
 		return destinations.toArray(new String[destinations.size()]);
 	}
 
 	/**
 	 * Method createQuestionDestinations
 	 * 
 	 * @return String[]
 	 */
 	private static String[] createQuestionDestinations() {
 		final ArrayList<String> destinations = new ArrayList<String>();
 		final List<R4EParticipant> participants = R4EUIModelController.getActiveReview().getParticipants();
 		for (R4EParticipant participant : participants) {
 			if (null != participant.getEmail()) {
 				if (!(R4EUIModelController.getActiveReview().getReview().getType().equals(R4EReviewType.R4E_REVIEW_TYPE_FORMAL))) {
					destinations.add(participant.getEmail());
 				} else {
 					//If this is a formal review, only send mail if we have the proper role
					if (participant.getRoles().contains(R4EUserRole.R4E_ROLE_LEAD)
							|| participant.getRoles().contains(R4EUserRole.R4E_ROLE_ORGANIZER)
							|| participant.getRoles().contains(R4EUserRole.R4E_ROLE_AUTHOR)) {
 						destinations.add(participant.getEmail());
 					}
 				}
 			}
 		}
 		return destinations.toArray(new String[destinations.size()]);
 	}
 
 	/**
 	 * Method createAnomalyCreatorDestination
 	 * 
 	 * @param aAnomaly
 	 *            R4EUIAnomalyBasic
 	 * @return String
 	 */
 	private static String[] createAnomalyCreatorDestination(R4EUIAnomalyBasic aAnomaly) {
 		final ArrayList<String> destinations = new ArrayList<String>();
		destinations.add(aAnomaly.getAnomaly().getUser().getEmail());
 		return destinations.toArray(new String[destinations.size()]);
 	}
 
 	/**
 	 * Method createItemsReadyNotificationMessage
 	 * 
 	 * @param aMeetingRequestIncluded
 	 *            boolean
 	 * @return String
 	 */
 	private static String createItemsReadyNotificationMessage(boolean aMeetingRequestIncluded) {
 		final StringBuilder msgBody = new StringBuilder();
 
 		msgBody.append(createIntroPart());
 		if (aMeetingRequestIncluded) {
 			msgBody.append(MEETING_REQUEST_MSG_BODY + LINE_FEED_MSG_PART + LINE_FEED_MSG_PART);
 		} else {
 			msgBody.append(ITEMS_READY_MSG_BODY + LINE_FEED_MSG_PART + LINE_FEED_MSG_PART);
 		}
 		final List<R4EUIReviewItem> items = R4EUIModelController.getActiveReview().getReviewItems();
 		for (R4EUIReviewItem item : items) {
 			if (item.isEnabled()) {
 				msgBody.append("Review Item -> " + item.getName() + LINE_FEED_MSG_PART);
 				msgBody.append("Eclipse Project: File Path Relative to Eclipse Project[: Line range]"
 						+ LINE_FEED_MSG_PART);
 				R4EUIFileContext[] contexts = (R4EUIFileContext[]) item.getChildren();
 				for (R4EUIFileContext context : contexts) {
 					if (context.isEnabled() && null != context.getTargetFileVersion()
 							&& null != context.getTargetFileVersion().getResource()) {
 						msgBody.append(TAB_MSG_PART + context.getTargetFileVersion().getResource().getProject() + ": "
 								+ context.getTargetFileVersion().getResource().getProjectRelativePath());
 						if (null != context.getContentsContainerElement()) {
 							R4EUIContent[] contents = (R4EUIContent[]) context.getContentsContainerElement()
 									.getChildren();
 							msgBody.append(": ");
 							for (R4EUIContent content : contents) {
 								msgBody.append(content.getPosition().toString() + ", ");
 							}
 						} else {
 							msgBody.append(LINE_FEED_MSG_PART);
 						}
 					}
 				}
 				msgBody.append(LINE_FEED_MSG_PART);
 			}
 		}
 		msgBody.append(createReviewInfoPart());
 		msgBody.append(createOutroPart());
 		return msgBody.toString();
 	}
 
 	/**
 	 * Method createRemovedItemsNotificationMessage
 	 * 
 	 * @param aElements
 	 *            List<R4EReviewComponent>
 	 * @param aIsAdded
 	 *            boolean
 	 * @return String
 	 */
 	private static String createUpdatedItemsNotificationMessage(List<R4EReviewComponent> aElements, boolean aIsAdded) {
 		final StringBuilder msgBody = new StringBuilder();
 
 		msgBody.append(createIntroPart());
 		if (aIsAdded) {
 			msgBody.append(ADDED_ELEMENTS_MSG_BODY + LINE_FEED_MSG_PART + LINE_FEED_MSG_PART);
 		} else {
 			msgBody.append(REMOVED_ELEMENTS_MSG_BODY + LINE_FEED_MSG_PART + LINE_FEED_MSG_PART);
 		}
 		boolean legendAppended = false;
 		for (R4EReviewComponent component : aElements) {
 			if (component instanceof R4EItem) {
 				if (null != ((R4EItem) component).getDescription()) {
 					msgBody.append("Review Item -> " + ((R4EItem) component).getDescription() + LINE_FEED_MSG_PART);
 				}
 				msgBody.append("Eclipse Project: File Path Relative to Eclipse Project[: Line range]"
 						+ LINE_FEED_MSG_PART);
 				EList<R4EFileContext> contexts = ((R4EItem) component).getFileContextList();
 				for (R4EFileContext context : contexts) {
 					if (null != context.getTarget() && null != context.getTarget().getResource()) {
 						msgBody.append(TAB_MSG_PART + context.getTarget().getResource().getProject() + ": "
 								+ context.getTarget().getResource().getProjectRelativePath());
 						if (context.getDeltas().size() > 0) {
 							msgBody.append(": ");
 							EList<R4EDelta> deltas = context.getDeltas();
 							for (R4EDelta delta : deltas) {
 								msgBody.append(buildLineTag(delta) + ", ");
 							}
 						}
 					}
 					msgBody.append(LINE_FEED_MSG_PART);
 				}
 				msgBody.append(LINE_FEED_MSG_PART);
 			} else if (component instanceof R4EDelta) {
 				if (!legendAppended) {
 					msgBody.append("Eclipse Project: File Path Relative to Eclipse Project[: Line range]"
 							+ LINE_FEED_MSG_PART);
 					legendAppended = true;
 				}
 				R4EFileContext context = (R4EFileContext) ((R4EDelta) component).eContainer();
 				if (null != context.getTarget() && null != context.getTarget().getResource()) {
 					msgBody.append(context.getTarget().getResource().getProject() + ": "
 							+ context.getTarget().getResource().getProjectRelativePath() + ": "
 							+ buildLineTag((R4EDelta) component) + ", ");
 				}
 			}
 			msgBody.append(LINE_FEED_MSG_PART);
 		}
 
 		msgBody.append(createReviewInfoPart());
 		msgBody.append(createOutroPart());
 		return msgBody.toString();
 	}
 
 	/**
 	 * Method createProgressNotification
 	 * 
 	 * @param aHeader
 	 *            String
 	 * @return String
 	 */
 	private static String createProgressNotification(String aHeader) {
 		final StringBuilder msgBody = new StringBuilder();
 
 		msgBody.append(createIntroPart());
 		msgBody.append(aHeader + LINE_FEED_MSG_PART);
 
 		int numReviewedFiles = 0;
 		int numTotalFiles = 0;
 		int numTotalAnomalies = 0;
 		final List<R4EUIReviewItem> items = R4EUIModelController.getActiveReview().getReviewItems();
 		for (R4EUIReviewItem item : items) {
 			R4EUIFileContext[] contexts = (R4EUIFileContext[]) item.getChildren();
 			for (R4EUIFileContext context : contexts) {
 				if (context.isUserReviewed()) {
 					++numReviewedFiles;
 				}
 				++numTotalFiles;
 				if (null != (R4EUIAnomalyContainer) context.getAnomalyContainerElement()) {
 					R4EUIAnomalyBasic[] anomalies = (R4EUIAnomalyBasic[]) ((R4EUIAnomalyContainer) context.getAnomalyContainerElement()).getChildren();
 					for (R4EUIAnomalyBasic anomalie : anomalies) {
 						++numTotalAnomalies;
 					}
 				}
 			}
 		}
 
 		//Add current review progress
 		msgBody.append("Files Reviewed: " + numReviewedFiles + TAB_MSG_PART);
 		msgBody.append("Files Total: " + numTotalFiles + TAB_MSG_PART);
 		final double progress = (numReviewedFiles / new Integer(numTotalFiles).doubleValue()) * 100;
 		final DecimalFormat fmt = new DecimalFormat("#");
 		msgBody.append("Progress: " + fmt.format(progress) + "%");
 		msgBody.append(LINE_FEED_MSG_PART);
 
 		//Add anomalies created by current reviewer
 		msgBody.append("Anomalies Created by: " + R4EUIModelController.getReviewer() + LINE_FEED_MSG_PART);
 		msgBody.append("Count: " + numTotalAnomalies + LINE_FEED_MSG_PART + LINE_FEED_MSG_PART);
 		if (numTotalAnomalies > 0) {
 			msgBody.append("FileContext: " + TAB_MSG_PART + "Eclipse Project: File Path Relative to Eclipse Project: "
 					+ LINE_FEED_MSG_PART);
 			msgBody.append(TAB_MSG_PART + "Anomaly: " + TAB_MSG_PART + "Line Range: Title: Description"
 					+ LINE_FEED_MSG_PART + LINE_FEED_MSG_PART);
 		}
 
 		boolean titleWritten = false;
 		for (R4EUIReviewItem item : items) {
 			R4EUIFileContext[] contexts = (R4EUIFileContext[]) item.getChildren();
 			for (R4EUIFileContext context : contexts) {
 				if (null != (R4EUIAnomalyContainer) context.getAnomalyContainerElement()) {
 					R4EUIAnomalyBasic[] anomalies = (R4EUIAnomalyBasic[]) ((R4EUIAnomalyContainer) context.getAnomalyContainerElement()).getChildren();
 					titleWritten = false;
 					for (R4EUIAnomalyBasic anomaly : anomalies) {
 						if (anomaly.getAnomaly().getUser().getId().equals(R4EUIModelController.getReviewer())) {
 							if (!titleWritten) {
 								if (null != context.getTargetFileVersion()
 										&& null != context.getTargetFileVersion().getResource()) {
 									msgBody.append(context.getTargetFileVersion().getResource().getProject() + ": "
 											+ context.getTargetFileVersion().getResource().getProjectRelativePath()
 											+ LINE_FEED_MSG_PART);
 									titleWritten = true;
 								}
 							}
 							msgBody.append(TAB_MSG_PART + anomaly.getPosition().toString() + ": "
 									+ anomaly.getAnomaly().getTitle() + ": " + anomaly.getAnomaly().getDescription()
 									+ LINE_FEED_MSG_PART);
 						}
 					}
 				}
 			}
 		}
 		msgBody.append(LINE_FEED_MSG_PART);
 
 		//Add global anomalies
 		final R4EUIAnomalyBasic[] anomalies = (R4EUIAnomalyBasic[]) R4EUIModelController.getActiveReview()
 				.getAnomalyContainer()
 				.getChildren();
 		if (anomalies.length > 0) {
 			msgBody.append("Global Anomalies: " + LINE_FEED_MSG_PART);
 		}
 		for (R4EUIAnomalyBasic anomaly : anomalies) {
 			if (anomaly.getAnomaly().getUser().getId().equals(R4EUIModelController.getReviewer())) {
 				msgBody.append(anomaly.getAnomaly().getTitle() + ": " + anomaly.getAnomaly().getDescription()
 						+ LINE_FEED_MSG_PART);
 			}
 		}
 
 		msgBody.append(createReviewInfoPart());
 		msgBody.append(createOutroPart());
 		return msgBody.toString();
 	}
 
 	/**
 	 * Method createQuestionMessage
 	 * 
 	 * @param aSource
 	 *            Object
 	 * @return String
 	 */
 	private static String createQuestionMessage(Object aSource) {
 		final StringBuilder msgBody = new StringBuilder();
 		msgBody.append(createIntroPart());
 		msgBody.append(QUESTION_MSG_BODY);
 
 		if (aSource instanceof R4EUIReviewBasic) {
 			msgBody.append("Review :" + LINE_FEED_MSG_PART + LINE_FEED_MSG_PART);
 
 		} else if (aSource instanceof R4EUIAnomalyBasic) {
 			final R4EFileVersion file = ((R4EUIFileContext) ((R4EUIAnomalyBasic) aSource).getParent().getParent()).getTargetFileVersion();
 			msgBody.append("Anomaly :" + LINE_FEED_MSG_PART + LINE_FEED_MSG_PART);
 			if (null != file) {
 				if (null != file.getResource()) {
 					msgBody.append("File: " + file.getResource().getProject() + ": "
 							+ file.getResource().getProjectRelativePath() + LINE_FEED_MSG_PART);
 				} else {
 					msgBody.append("File: " + file.getRepositoryPath() + LINE_FEED_MSG_PART);
 				}
 				msgBody.append("Version: " + file.getVersionID() + LINE_FEED_MSG_PART);
 			} else {
 				msgBody.append("File: "
 						+ ((R4EUIFileContext) ((R4EUIAnomalyBasic) aSource).getParent().getParent()).getName()
 						+ LINE_FEED_MSG_PART);
 			}
 			msgBody.append("Line(s): " + ((R4EUIAnomalyBasic) aSource).getPosition().toString() + LINE_FEED_MSG_PART);
 			msgBody.append("Title: " + ((R4EUIAnomalyBasic) aSource).getAnomaly().getTitle() + LINE_FEED_MSG_PART);
 			msgBody.append("Description: " + ((R4EUIAnomalyBasic) aSource).getAnomaly().getDescription()
 					+ LINE_FEED_MSG_PART);
 
 		} else if (aSource instanceof R4EUIReviewItem) {
 			msgBody.append("Review Item :" + LINE_FEED_MSG_PART + LINE_FEED_MSG_PART);
 			msgBody.append("Description: " + ((R4EUIReviewItem) aSource).getItem().getDescription()
 					+ LINE_FEED_MSG_PART);
 
 		} else if (aSource instanceof R4EUIFileContext) {
 			msgBody.append("File:" + LINE_FEED_MSG_PART + LINE_FEED_MSG_PART);
 			final R4EFileVersion targetFile = ((R4EUIFileContext) aSource).getTargetFileVersion();
 			if (null != targetFile) {
 				if (null != targetFile.getResource()) {
 					msgBody.append("Target File: " + targetFile.getResource().getProject() + ": "
 							+ targetFile.getResource().getProjectRelativePath() + LINE_FEED_MSG_PART);
 				} else {
 					msgBody.append("File: " + targetFile.getRepositoryPath() + LINE_FEED_MSG_PART);
 				}
 				msgBody.append("Target Version: " + targetFile.getVersionID() + LINE_FEED_MSG_PART);
 			} else {
 				msgBody.append("File: " + ((R4EUIFileContext) aSource).getName() + LINE_FEED_MSG_PART);
 				msgBody.append("Target Version: None" + LINE_FEED_MSG_PART);
 			}
 			final R4EFileVersion baseFile = ((R4EUIFileContext) aSource).getBaseFileVersion();
 			if (null == baseFile) {
 				msgBody.append("Base Version: None" + LINE_FEED_MSG_PART);
 			} else {
 				msgBody.append("Base Version: " + baseFile.getVersionID() + LINE_FEED_MSG_PART);
 			}
 
 		} else if (aSource instanceof R4EUIContent) {
 			final R4EFileVersion file = ((R4EUIFileContext) ((R4EUIContent) aSource).getParent().getParent()).getTargetFileVersion();
 			msgBody.append("Selection :" + LINE_FEED_MSG_PART + LINE_FEED_MSG_PART);
 			if (null != file) {
 				if (null != file.getResource()) {
 					msgBody.append("File: " + file.getResource().getProject() + ": "
 							+ file.getResource().getProjectRelativePath() + LINE_FEED_MSG_PART);
 				} else {
 					msgBody.append("File: " + file.getRepositoryPath() + LINE_FEED_MSG_PART);
 				}
 				msgBody.append("Version: " + file.getVersionID() + LINE_FEED_MSG_PART);
 			} else {
 				msgBody.append("File: "
 						+ ((R4EUIFileContext) ((R4EUIContent) aSource).getParent().getParent()).getName()
 						+ LINE_FEED_MSG_PART);
 			}
 			msgBody.append("Line(s): " + ((R4EUIContent) aSource).getPosition().toString() + LINE_FEED_MSG_PART);
 
 		} else if (aSource instanceof ITextEditor) {
 			//Get the information from the text editor
 			final IRegion region = ((ITextEditor) aSource).getHighlightRange();
 			final IEditorInput input = ((ITextEditor) aSource).getEditorInput();
 			final TextSelection selectedText = new TextSelection(((ITextEditor) aSource).getDocumentProvider()
 					.getDocument(input), region.getOffset(), region.getLength());
 			((ITextEditor) aSource).getSelectionProvider().setSelection(selectedText);
 			R4EFileVersion file = null;
 			if (input instanceof R4EFileRevisionEditorInput) {
 				file = ((R4EFileRevisionEditorInput) input).getFileVersion();
 				msgBody.append("File: " + file.getRepositoryPath() + LINE_FEED_MSG_PART);
 			} else if (input instanceof R4EFileEditorInput) {
 				file = ((R4EFileEditorInput) input).getFileVersion();
 				msgBody.append("File: " + file.getResource().getProject() + ": "
 						+ file.getResource().getProjectRelativePath() + LINE_FEED_MSG_PART);
 			}
 			msgBody.append(LINE_FEED_MSG_PART);
 			msgBody.append("Position in File : " + CommandUtils.getPosition(selectedText).toString()
 					+ LINE_FEED_MSG_PART + LINE_FEED_MSG_PART);
 			msgBody.append("Contents :" + LINE_FEED_MSG_PART);
 			msgBody.append(selectedText.getText());
 
 		} else if (aSource instanceof TextSelection) {
 			final IEditorPart editorPart = PlatformUI.getWorkbench()
 					.getActiveWorkbenchWindow()
 					.getActivePage()
 					.getActiveEditor();
 			final IEditorInput input = editorPart.getEditorInput();
 			String filename = null;
 			R4EFileVersion file = null;
 			if (input instanceof R4ECompareEditorInput) {
 				filename = ((R4ECompareEditorInput) input).getLeftElement().getName();
 				msgBody.append("File: " + filename + LINE_FEED_MSG_PART);
 			} else if (input instanceof R4EFileRevisionEditorInput) {
 				file = ((R4EFileRevisionEditorInput) input).getFileVersion();
 				msgBody.append("File: " + file.getRepositoryPath() + LINE_FEED_MSG_PART);
 			} else if (input instanceof R4EFileEditorInput) {
 				file = ((R4EFileEditorInput) input).getFileVersion();
 				msgBody.append("File: " + file.getResource().getProject() + ": "
 						+ file.getResource().getProjectRelativePath() + LINE_FEED_MSG_PART);
 			}
 			final TextSelection selectedText = (TextSelection) aSource;
 			msgBody.append(LINE_FEED_MSG_PART);
 			msgBody.append("Position in File : " + CommandUtils.getPosition(selectedText).toString()
 					+ LINE_FEED_MSG_PART + LINE_FEED_MSG_PART);
 			msgBody.append("Contents :" + LINE_FEED_MSG_PART);
 			msgBody.append(selectedText.getText());
 		}
 		msgBody.append(LINE_FEED_MSG_PART);
 		msgBody.append(createReviewInfoPart());
 		msgBody.append(createOutroPart());
 		return msgBody.toString();
 	}
 
 	/**
 	 * Method createReviewInfoPart
 	 * 
 	 * @return String
 	 */
 	private static String createReviewInfoPart() {
 		final StringBuilder msgReviewInfo = new StringBuilder();
 
 		msgReviewInfo.append(LINE_FEED_MSG_PART);
 		msgReviewInfo.append("Review Information");
 		msgReviewInfo.append(LINE_FEED_MSG_PART);
 		msgReviewInfo.append("Group: " + TAB_MSG_PART + TAB_MSG_PART
 				+ R4EUIModelController.getActiveReview().getParent().getName() + LINE_FEED_MSG_PART);
 		msgReviewInfo.append("Review: " + TAB_MSG_PART + R4EUIModelController.getActiveReview().getReview().getName()
 				+ LINE_FEED_MSG_PART);
 		msgReviewInfo.append("Components: " + TAB_MSG_PART);
 		final List<String> components = R4EUIModelController.getActiveReview().getReview().getComponents();
 		for (String component : components) {
 			msgReviewInfo.append(component + ", ");
 		}
 		msgReviewInfo.append(LINE_FEED_MSG_PART);
 		msgReviewInfo.append("Project: " + TAB_MSG_PART
 				+ R4EUIModelController.getActiveReview().getReview().getProject() + LINE_FEED_MSG_PART);
 		msgReviewInfo.append("Participants: " + TAB_MSG_PART);
 		final List<String> participants = R4EUIModelController.getActiveReview().getParticipantIDs();
 		for (String participant : participants) {
 			msgReviewInfo.append(participant + ", ");
 		}
 		msgReviewInfo.append(LINE_FEED_MSG_PART);
 		msgReviewInfo.append(LINE_FEED_MSG_PART);
 
 		return msgReviewInfo.toString();
 	}
 
 	/**
 	 * Method createIntroPart
 	 * 
 	 * @return String
 	 */
 	private static String createIntroPart() {
 		final StringBuilder msgIntro = new StringBuilder();
 		msgIntro.append(LINE_FEED_MSG_PART);
 		msgIntro.append(INTRO_MSG_BODY);
 		msgIntro.append(LINE_FEED_MSG_PART);
 		msgIntro.append(LINE_FEED_MSG_PART);
 		return msgIntro.toString();
 	}
 
 	/**
 	 * Method createOutroPart
 	 * 
 	 * @return String
 	 */
 	private static String createOutroPart() {
 		final StringBuilder msgOutro = new StringBuilder();
 		msgOutro.append(OUTRO_MSG_BODY + LINE_FEED_MSG_PART);
 		msgOutro.append(R4EUIModelController.getReviewer());
 		return msgOutro.toString();
 	}
 
 	//Meetings
 
 	/**
 	 * Method sendMeetingRequest
 	 * 
 	 * @throws OutOfSyncException
 	 * @throws ResourceHandlingException
 	 * @throws CoreException
 	 */
 	public static void sendMeetingRequest() throws ResourceHandlingException, OutOfSyncException {
 		sendMeetingRequest(getDefaultStartTime(), DEFAULT_MEETING_DURATION, DEFAULT_MEETING_LOCATION);
 	}
 
 	/**
 	 * Method sendMeetingRequest
 	 * 
 	 * @param aStartDate
 	 *            Long
 	 * @param aDuration
 	 *            Integer
 	 * @param aLocation
 	 *            String
 	 * @throws CoreException
 	 * @throws ResourceHandlingException
 	 * @throws OutOfSyncException
 	 */
 	private static void sendMeetingRequest(Long aStartDate, Integer aDuration, String aLocation)
 			throws ResourceHandlingException, OutOfSyncException {
 		if (null != R4EUIModelController.getMailConnector()) {
 			final String[] messageDestinations = createItemsUpdatedDestinations();
 			String messageSubject = null;
 			if (null != R4EUIModelController.getActiveReview().getReview().getActiveMeeting()) {
 				messageSubject = createSubject() + " - Decision Meeting Request Updated";
 			} else {
 				messageSubject = createSubject() + " - Items Ready for Review & Decision Meeting Request";
 			}
 			final String messageBody = createItemsReadyNotificationMessage(true);
 
 			IMeetingData meetingData = null;
 			try {
 				meetingData = R4EUIModelController.getMailConnector().createMeetingRequest(messageSubject, messageBody,
 						messageDestinations, aStartDate, aDuration);
 			} catch (CoreException e) {
 				UIUtils.displayCoreErrorDialog(e);
 				return;
 			}
 			R4EUIModelController.getActiveReview().setMeetingData(meetingData);
 		} else {
 			showNoEmailConnectorDialog();
 		}
 	}
 
 	/**
 	 * Method getDefaultStartTime
 	 * 
 	 * @return Long
 	 */
 	public static Long getDefaultStartTime() {
 
 		// Make sure we leave 3 days to review and don't set a meeting on the week-end
 		final GregorianCalendar meetingDate = new GregorianCalendar();
 		switch (meetingDate.get(Calendar.DAY_OF_WEEK)) {
 		case Calendar.TUESDAY:
 			meetingDate.setTimeInMillis(meetingDate.getTimeInMillis() + 518400000);
 			break;
 		case Calendar.WEDNESDAY:
 			meetingDate.setTimeInMillis(meetingDate.getTimeInMillis() + 691200000);
 			break;
 		case Calendar.THURSDAY:
 			meetingDate.setTimeInMillis(meetingDate.getTimeInMillis() + 604800000);
 			break;
 		case Calendar.FRIDAY:
 			meetingDate.setTimeInMillis(meetingDate.getTimeInMillis() + 518400000);
 			break;
 		case Calendar.SATURDAY:
 			meetingDate.setTimeInMillis(meetingDate.getTimeInMillis() + 432000000);
 			break;
 		default:
 			meetingDate.setTimeInMillis(meetingDate.getTimeInMillis() + 345600000);
 			break;
 		}
 
 		// Set start time at 10 AM
 		meetingDate.set(Calendar.HOUR_OF_DAY, 10);
 		meetingDate.set(Calendar.MINUTE, 0);
 		meetingDate.set(Calendar.SECOND, 0);
 		// Add the current time zone offset
 		meetingDate.setTimeInMillis(meetingDate.getTimeInMillis()
 				+ TimeZone.getDefault().getOffset(System.currentTimeMillis()));
 		return Long.valueOf(meetingDate.getTimeInMillis());
 	}
 
 	/**
 	 * Method showNoEmailConnectorDialog
 	 */
 	private static void showNoEmailConnectorDialog() {
 		final ErrorDialog dialog = new ErrorDialog(
 				null,
 				R4EUIConstants.DIALOG_TITLE_WARNING,
 				"No Email connector detected"
 						+ "Take note that no Automatic Email can be sent because no Mail Services Connector is Present",
 				new Status(IStatus.WARNING, Activator.PLUGIN_ID, 0, null, null), IStatus.WARNING);
 		dialog.open();
 	}
 
 	/**
 	 * Method buildLineTag
 	 * 
 	 * @param aDelta
 	 *            R4EDelta
 	 * @return String
 	 */
 	private static String buildLineTag(R4EDelta aDelta) {
 		if (null != aDelta.getTarget() && null != aDelta.getTarget().getLocation()) {
 			final int startLine = ((R4ETextPosition) aDelta.getTarget().getLocation()).getStartLine();
 			final int endLineLine = ((R4ETextPosition) aDelta.getTarget().getLocation()).getEndLine();
 			final StringBuilder buffer = new StringBuilder(R4EUIConstants.DEFAULT_LINE_TAG_LENGTH);
 			if (startLine == endLineLine) {
 				buffer.append(R4EUIConstants.LINE_TAG + startLine);
 			} else {
 				buffer.append(R4EUIConstants.LINES_TAG + startLine + "-" + endLineLine);
 			}
 			return buffer.toString();
 		}
 		return "";
 	}
 }
