 // $codepro.audit.disable com.instantiations.assist.eclipse.analysis.audit.rule.effectivejava.alwaysOverridetoString.alwaysOverrideToString, com.instantiations.assist.eclipse.analysis.deserializeabilitySecurity, com.instantiations.assist.eclipse.analysis.disallowReturnMutable, com.instantiations.assist.eclipse.analysis.enforceCloneableUsageSecurity, com.instantiations.assist.eclipse.analysis.instanceFieldSecurity, com.instantiations.assist.eclipse.analysis.mutabilityOfArrays
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
  * This class implements the Review element of the UI model
  * 
  * Contributors:
  *   Sebastien Dubois - Created for Mylyn Review R4E project
  *   Jacques Bouthillier - Add definition for Report
  *   
  *******************************************************************************/
 
 package org.eclipse.mylyn.reviews.r4e.ui.internal.model;
 
 import java.io.FileNotFoundException;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.List;
 import java.util.concurrent.atomic.AtomicReference;
 
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.common.util.EMap;
 import org.eclipse.mylyn.reviews.frame.core.model.Item;
 import org.eclipse.mylyn.reviews.frame.core.model.ReviewComponent;
 import org.eclipse.mylyn.reviews.notifications.core.IMeetingData;
 import org.eclipse.mylyn.reviews.notifications.spi.NotificationsConnector;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EDecision;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EFileContext;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EFormalReview;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EItem;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EMeetingData;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EParticipant;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EReview;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EReviewComponent;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EReviewDecision;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EReviewPhase;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EReviewState;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EReviewType;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EUserReviews;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EUserRole;
 import org.eclipse.mylyn.reviews.r4e.core.model.serial.Persistence.RModelFactoryExt;
 import org.eclipse.mylyn.reviews.r4e.core.model.serial.Persistence.ResourceUpdater;
 import org.eclipse.mylyn.reviews.r4e.core.model.serial.impl.OutOfSyncException;
 import org.eclipse.mylyn.reviews.r4e.core.model.serial.impl.ResourceHandlingException;
 import org.eclipse.mylyn.reviews.r4e.core.versions.ReviewVersionsException;
 import org.eclipse.mylyn.reviews.r4e.ui.R4EUIPlugin;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.commands.ImportPostponedHandler;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.navigator.ReviewNavigatorContentProvider;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.preferences.PreferenceConstants;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.properties.general.ReviewProperties;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.utils.CommandUtils;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.utils.R4EUIConstants;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.utils.UIUtils;
 import org.eclipse.mylyn.versions.core.ChangeSet;
 import org.eclipse.ui.views.properties.IPropertySource;
 
 /**
  * @author lmcdubo
  * @version $Revision: 1.0 $
  */
 public class R4EUIReviewBasic extends R4EUIModelElement {
 
 	// ------------------------------------------------------------------------
 	// Constants
 	// ------------------------------------------------------------------------
 
 	/**
 	 * Field REVIEW_BASIC_ICON_FILE. (value is ""icons/obj16/reviewbas_obj.gif"")
 	 */
 	public static final String REVIEW_BASIC_ICON_FILE = "icons/obj16/reviewbas_obj.gif";
 
 	/**
 	 * Field REVIEW_BASIC_CLOSED_ICON_FILE. (value is ""icons/obj16/revbasclsd_obj.gif"")
 	 */
 	public static final String REVIEW_BASIC_CLOSED_ICON_FILE = "icons/obj16/revbasclsd_obj.gif";
 
 	/**
 	 * Field REVIEW_INFORMAL_ICON_FILE. (value is ""icons/obj16/reviewinf_obj.gif"")
 	 */
 	public static final String REVIEW_INFORMAL_ICON_FILE = "icons/obj16/reviewinf_obj.gif";
 
 	/**
 	 * Field REVIEW_INFORMAL_CLOSED_ICON_FILE. (value is ""icons/obj16/revinfclsd_obj.gif"")
 	 */
 	public static final String REVIEW_INFORMAL_CLOSED_ICON_FILE = "icons/obj16/revinfclsd_obj.gif";
 
 	/**
 	 * Field OPEN_ELEMENT_COMMAND_NAME. (value is ""Open Review"")
 	 */
 	private static final String OPEN_ELEMENT_COMMAND_NAME = "Open Review";
 
 	/**
 	 * Field OPEN_ELEMENT_COMMAND_TOOLTIP. (value is ""Open and Load Data for this Review"")
 	 */
 	private static final String OPEN_ELEMENT_COMMAND_TOOLTIP = "Open and Load Data for this Review";
 
 	/**
 	 * Field CLOSE_ELEMENT_COMMAND_NAME. (value is ""Close Review"")
 	 */
 	private static final String CLOSE_ELEMENT_COMMAND_NAME = "Close Review";
 
 	/**
 	 * Field CLOSE_ELEMENT_COMMAND_TOOLTIP. (value is ""Close and Unload Data for this Review"")
 	 */
 	private static final String CLOSE_ELEMENT_COMMAND_TOOLTIP = "Close and Unload Data for this Review";
 
 	/**
 	 * Field NEXT_STATE_ELEMENT_COMMAND_NAME. (value is ""Progress Review"")
 	 */
 	private static final String NEXT_STATE_ELEMENT_COMMAND_NAME = "Progress Review";
 
 	/**
 	 * Field NEXT_STATE_ELEMENT_COMMAND_TOOLTIP. (value is ""Progress Review to Next Phase"")
 	 */
 	private static final String NEXT_STATE_ELEMENT_COMMAND_TOOLTIP = "Progress Review to Next Phase";
 
 	/**
 	 * Field PREVIOUS_STATE_ELEMENT_COMMAND_NAME. (value is ""Regress Review"")
 	 */
 	private static final String PREVIOUS_STATE_ELEMENT_COMMAND_NAME = "Regress Review";
 
 	/**
 	 * Field PREVIOUS_STATE_ELEMENT_COMMAND_TOOLTIP. (value is ""Regress Review to Previous Phase"")
 	 */
 	private static final String PREVIOUS_STATE_ELEMENT_COMMAND_TOOLTIP = "Regress Review to Previous Phase";
 
 	/**
 	 * Field REMOVE_ELEMENT_ACTION_NAME. (value is ""Delete Review"")
 	 */
 	private static final String REMOVE_ELEMENT_COMMAND_NAME = "Disable Review";
 
 	/**
 	 * Field REMOVE_ELEMENT_ACTION_TOOLTIP. (value is ""Remove this review from its parent review group"")
 	 */
 	private static final String REMOVE_ELEMENT_COMMAND_TOOLTIP = "Disable (and Optionally Remove) this Review from "
 			+ "its Parent Review Group";
 
 	/**
 	 * Field REPORT_ELEMENT_COMMAND_TOOLTIP. (value is ""Create a report for this Review"")
 	 */
 	private static final String REPORT_ELEMENT_COMMAND_TOOLTIP = "Generate a report for this Review"; //$NON-NLS-1$
 
 	/**
 	 * Field FBasicPhaseValues.
 	 */
 	private static final String[] BASIC_PHASE_VALUES = { R4EUIConstants.REVIEW_PHASE_STARTED,
 			R4EUIConstants.REVIEW_PHASE_COMPLETED };
 
 	/**
 	 * Field EXIT_DECISION_NONE. (value is ""No Decision"")
 	 */
 	private static final String EXIT_DECISION_NONE = "No Decision";
 
 	/**
 	 * Field EXIT_DECISION_ACCEPTED. (value is ""Accepted"")
 	 */
 	private static final String EXIT_DECISION_ACCEPTED = "Accepted";
 
 	/**
 	 * Field EXIT_DECISION_ACCEPTED_FOLLOWUP. (value is ""Accepted with Follow-up"")
 	 */
 	private static final String EXIT_DECISION_ACCEPTED_FOLLOWUP = "Accepted with Follow-up";
 
 	/**
 	 * Field EXIT_DECISION_REJECTED. (value is ""Rejected"")
 	 */
 	private static final String EXIT_DECISION_REJECTED = "Rejected";
 
 	/**
 	 * Field decisionValues.
 	 */
 	private static final String[] DECISION_VALUES = { EXIT_DECISION_NONE, EXIT_DECISION_ACCEPTED,
 			EXIT_DECISION_ACCEPTED_FOLLOWUP, EXIT_DECISION_REJECTED }; //NOTE: This has to match R4EDecision in R4E core plugin
 
 	// ------------------------------------------------------------------------
 	// Member variables
 	// ------------------------------------------------------------------------
 
 	/**
 	 * Field fReview.
 	 */
 	protected R4EReview fReview;
 
 	/**
 	 * Field fReviewName.
 	 */
 	protected final String fReviewName;
 
 	/**
 	 * Field fItems.
 	 */
 	protected final List<R4EUIReviewItem> fItems;
 
 	/**
 	 * Field fParticipantsContainer.
 	 */
 	protected R4EUIParticipantContainer fParticipantsContainer = null;
 
 	/**
 	 * Field fAnomalyContainer.
 	 */
 	protected R4EUIAnomalyContainer fAnomalyContainer = null;
 
 	/**
 	 * Field fPostponedContainer.
 	 */
 	protected R4EUIPostponedContainer fPostponedContainer = null;
 
 	// ------------------------------------------------------------------------
 	// Constructors
 	// ------------------------------------------------------------------------
 
 	/**
 	 * Constructor for R4EUIReview.
 	 * 
 	 * @param aParent
 	 *            R4EUIReviewGroup
 	 * @param aReview
 	 *            R4EReview
 	 * @param aType
 	 *            R4EReviewType
 	 * @param aOpen
 	 *            boolean
 	 * @throws ResourceHandlingException
 	 */
 	public R4EUIReviewBasic(R4EUIReviewGroup aParent, R4EReview aReview, R4EReviewType aType, boolean aOpen)
 			throws ResourceHandlingException {
 		super(aParent, getReviewDisplayName(aReview.getName(), aType), aReview.getExtraNotes());
 		fReview = aReview;
 		fReviewName = aReview.getName();
 		fParticipantsContainer = new R4EUIParticipantContainer(this, R4EUIConstants.PARTICIPANTS_LABEL);
 		fAnomalyContainer = new R4EUIAnomalyContainer(this, R4EUIConstants.GLOBAL_ANOMALIES_LABEL);
 		fItems = new ArrayList<R4EUIReviewItem>();
 		if (aOpen) {
 			//Open the new review and make it the active one (close any other that is open)
 			if (aType.equals(R4EReviewType.R4E_REVIEW_TYPE_BASIC)) {
 				setImage(REVIEW_BASIC_ICON_FILE);
 			} else {
 				setImage(REVIEW_INFORMAL_ICON_FILE);
 			}
 			fOpen = true;
 			final List<R4EUserRole> role = new ArrayList<R4EUserRole>(1);
 			role.add(R4EUserRole.R4E_ROLE_LEAD);
 			final R4EParticipant participant = R4EUIModelController.FModelExt.createR4EParticipant(fReview,
 					R4EUIModelController.getReviewer(), role);
 			fParticipantsContainer.addChildren(new R4EUIParticipant(fParticipantsContainer, participant));
 			final R4EUIReviewBasic activeReview = R4EUIModelController.getActiveReview();
 			if (null != activeReview) {
 				activeReview.close();
 			}
 			R4EUIModelController.setActiveReview(this);
 		} else {
 			if (aType.equals(R4EReviewType.R4E_REVIEW_TYPE_BASIC)) {
 				setImage(REVIEW_BASIC_CLOSED_ICON_FILE);
 			} else {
 				setImage(REVIEW_INFORMAL_CLOSED_ICON_FILE);
 			}
 			fOpen = false;
 		}
 	}
 
 	// ------------------------------------------------------------------------
 	// Methods
 	// ------------------------------------------------------------------------
 
 	/**
 	 * Method getAdapter.
 	 * 
 	 * @param adapter
 	 *            Class
 	 * @return Object
 	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(Class)
 	 */
 	@Override
 	public Object getAdapter(@SuppressWarnings("rawtypes")
 	Class adapter) {
 		if (IR4EUIModelElement.class.equals(adapter)) {
 			return this;
 		}
 		if (IPropertySource.class.equals(adapter)) {
 			return new ReviewProperties(this);
 		}
 		return null;
 	}
 
 	//Attributes
 
 	/**
 	 * Method getReviewDisplayName.
 	 * 
 	 * @param aName
 	 *            String
 	 * @param aType
 	 *            R4EReviewType
 	 * @return String
 	 */
 	private static String getReviewDisplayName(String aName, R4EReviewType aType) {
 		if (aType.equals(R4EReviewType.R4E_REVIEW_TYPE_INFORMAL)) {
 			return R4EUIConstants.REVIEW_TYPE_INFORMAL + ": " + aName;
 		} else if (aType.equals(R4EReviewType.R4E_REVIEW_TYPE_BASIC)) {
 			return R4EUIConstants.REVIEW_TYPE_BASIC + ": " + aName;
 		} else {
 			//No change.  For formal review the name is set in the subclass
 			return aName;
 		}
 	}
 
 	/**
 	 * Set serialization model data by copying it from the passed-in object
 	 * 
 	 * @param aModelComponent
 	 *            - a serialization model element to copy information from
 	 * @throws ResourceHandlingException
 	 * @throws OutOfSyncException
 	 * @see org.eclipse.mylyn.reviews.r4e.ui.internal.model.IR4EUIModelElement#setModelData(R4EReviewComponent)
 	 */
 	@Override
 	public void setModelData(ReviewComponent aModelComponent) throws ResourceHandlingException, OutOfSyncException {
 		//Set data in model element
 		final Long bookNum = R4EUIModelController.FResourceUpdater.checkOut(fReview, R4EUIModelController.getReviewer());
 		fReview.setExtraNotes(((R4EReview) aModelComponent).getExtraNotes());
 		fReview.setType(((R4EReview) aModelComponent).getType());
 
 		//Optional properties
 		fReview.setProject(((R4EReview) aModelComponent).getProject());
 		fReview.getComponents().addAll(((R4EReview) aModelComponent).getComponents());
 		fReview.setEntryCriteria(((R4EReview) aModelComponent).getEntryCriteria());
 		fReview.setObjectives(((R4EReview) aModelComponent).getObjectives());
 		fReview.setReferenceMaterial(((R4EReview) aModelComponent).getReferenceMaterial());
 		R4EUIModelController.FResourceUpdater.checkIn(bookNum);
 	}
 
 	/**
 	 * Method getReview.
 	 * 
 	 * @return R4EReview
 	 */
 	public R4EReview getReview() {
 		return fReview;
 	}
 
 	/**
 	 * Method getAnomalyContainer.
 	 * 
 	 * @return R4EUIAnomalyContainer
 	 */
 	public R4EUIAnomalyContainer getAnomalyContainer() {
 		return fAnomalyContainer;
 	}
 
 	/**
 	 * Method getPostponedContainer.
 	 * 
 	 * @return R4EUIPostponedContainer
 	 */
 	public R4EUIPostponedContainer getPostponedContainer() {
 		return fPostponedContainer;
 	}
 
 	/**
 	 * Add a new participant
 	 * 
 	 * @param aParticipant
 	 *            - the new particpant name/ID
 	 * @param aCreate
 	 *            boolean
 	 * @return R4EParticipant
 	 * @throws ResourceHandlingException
 	 */
 	public R4EParticipant getParticipant(String aParticipant, boolean aCreate) throws ResourceHandlingException {
 		R4EParticipant participant = null;
 		if (isParticipant(aParticipant)) {
 			participant = (R4EParticipant) fReview.getUsersMap().get(aParticipant);
 			if (aCreate && !participant.isEnabled()) {
 				try {
					fParticipantsContainer.getParticipant(participant).setEnabled(true);
 				} catch (OutOfSyncException e) {
 					R4EUIPlugin.Ftracer.traceError("Exception: " + e.toString() + " (" + e.getMessage() + ")");
 					R4EUIPlugin.getDefault().logError("Exception: " + e.toString(), e);
 				}
 			}
 		} else {
 			if (aCreate) {
 				final List<R4EUserRole> role = new ArrayList<R4EUserRole>(1);
 				role.add(R4EUserRole.R4E_ROLE_REVIEWER);
 				participant = R4EUIModelController.FModelExt.createR4EParticipant(fReview, aParticipant, role);
 				fParticipantsContainer.addChildren(new R4EUIParticipant(fParticipantsContainer, participant));
 			}
 		}
 		return participant;
 	}
 
 	/**
 	 * Checks whether the participant is in the participants list for this review
 	 * 
 	 * @param aParticipant
 	 *            - the participant to look for
 	 * @return true/false
 	 */
 	public boolean isParticipant(String aParticipant) {
 		if (null != fReview) {
 			if (isOpen()) {
 				if (null != ((R4EParticipant) fReview.getUsersMap().get(aParticipant))) {
 					return true;
 				}
 			} else {
 				final R4EUserReviews userReviews = ((R4EUIReviewGroup) getParent()).getReviewGroup()
 						.getUserReviews()
 						.get(aParticipant);
 				if (null != userReviews && userReviews.getInvitedToMap().containsKey(fReview.getName())) {
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * Method getParticipants.
 	 * 
 	 * @return List<R4EParticipant>
 	 */
 	public List<R4EParticipant> getParticipants() {
 		final Object[] users = fReview.getUsersMap().values().toArray();
 
 		//Cast list to R4EParticipants
 		final List<R4EParticipant> participants = new ArrayList<R4EParticipant>();
 		for (Object user : users) {
 			participants.add((R4EParticipant) user);
 		}
 		return participants;
 	}
 
 	/**
 	 * Method getParticipantIDs.
 	 * 
 	 * @return List<String>
 	 */
 	public List<String> getParticipantIDs() {
 		final Object[] users = fReview.getUsersMap().values().toArray();
 
 		//Cast list to R4EParticipants
 		final List<String> participantIDs = new ArrayList<String>();
 		for (Object user : users) {
 			participantIDs.add(((R4EParticipant) user).getId());
 		}
 		return participantIDs;
 	}
 
 	/**
 	 * Method isReviewed.
 	 * 
 	 * @return boolean
 	 * @see org.eclipse.mylyn.reviews.r4e.ui.internal.model.IR4EUIModelElement#isUserReviewed()
 	 */
 	@Override
 	public boolean isUserReviewed() {
 		try {
 			final R4EParticipant participant = getParticipant(R4EUIModelController.getReviewer(), false);
 			if (null != participant) {
 				return participant.isReviewCompleted();
 			}
 		} catch (ResourceHandlingException e) {
 			R4EUIPlugin.Ftracer.traceError("Exception: " + e.toString() + " (" + e.getMessage() + ")");
 			R4EUIPlugin.getDefault().logError("Exception: " + e.toString(), e);
 			return fUserReviewed;
 		}
 		return fUserReviewed;
 	}
 
 	/**
 	 * Method setReviewed.
 	 * 
 	 * @param aReviewed
 	 *            boolean
 	 * @throws ResourceHandlingException
 	 * @throws OutOfSyncException
 	 * @see org.eclipse.mylyn.reviews.r4e.ui.internal.model.IR4EUIModelElement#setUserReviewed(boolean)
 	 */
 	@Override
 	public void setUserReviewed(boolean aReviewed) throws ResourceHandlingException, OutOfSyncException { // $codepro.audit.disable emptyMethod, unnecessaryExceptions
 		final R4EParticipant participant = getParticipant(R4EUIModelController.getReviewer(), true);
 		if (null != participant) {
 			final Long bookNum = R4EUIModelController.FResourceUpdater.checkOut(fReview,
 					R4EUIModelController.getReviewer());
 			participant.setReviewCompleted(aReviewed);
 			R4EUIModelController.FResourceUpdater.checkIn(bookNum);
 		}
 		fUserReviewed = aReviewed;
 
 		if (fUserReviewed) {
 			//Also set the children
 			final int length = fItems.size();
 			for (int i = 0; i < length; i++) {
 				fItems.get(i).setUserReviewed(aReviewed);
 			}
 		}
 		fireUserReviewStateChanged(this);
 	}
 
 	/**
 	 * Close the model element (i.e. disable it)
 	 * 
 	 * @see org.eclipse.mylyn.reviews.r4e.ui.internal.model.IR4EUIModelElement#close()
 	 */
 	@Override
 	public void close() {
 		//Remove all children references
 		R4EUIReviewItem reviewItem = null;
 		final int itemsSize = fItems.size();
 		for (int i = 0; i < itemsSize; i++) {
 
 			reviewItem = fItems.get(i);
 			reviewItem.close();
 		}
 		fParticipantsContainer.close();
 		fAnomalyContainer.close();
 		if (null != fPostponedContainer) {
 			fPostponedContainer.close();
 		}
 
 		fItems.clear();
 		fOpen = false;
 		R4EUIModelController.FModelExt.closeR4EReview(fReview); //Notify model
 		R4EUIModelController.clearAnomalyMap();
 		if (fReview.getType().equals(R4EReviewType.R4E_REVIEW_TYPE_BASIC)) {
 			fImage = UIUtils.loadIcon(REVIEW_BASIC_CLOSED_ICON_FILE);
 		} else {
 			fImage = UIUtils.loadIcon(REVIEW_INFORMAL_CLOSED_ICON_FILE);
 		}
 		R4EUIModelController.setActiveReview(null);
 		fireUserReviewStateChanged(this);
 	}
 
 	/**
 	 * Open the model element (i.e. enable it)
 	 * 
 	 * @throws ResourceHandlingException
 	 * @throws ReviewVersionsException
 	 * @throws FileNotFoundException
 	 * @see org.eclipse.mylyn.reviews.r4e.ui.internal.model.IR4EUIModelElement#open()
 	 */
 	@Override
 	public void open() throws ResourceHandlingException, FileNotFoundException, ReviewVersionsException {
 		fReview = R4EUIModelController.FModelExt.openR4EReview(((R4EUIReviewGroup) getParent()).getReviewGroup(),
 				fReviewName);
 
 		final EList<Item> items = fReview.getReviewItems();
 		if (null != items) {
 
 			IR4EUIModelElement uiItem = null;
 
 			R4EUIModelController.mapAnomalies(fReview);
 			final int itemsSize = items.size();
 			R4EItem item = null;
 			for (int i = 0; i < itemsSize; i++) {
 				item = (R4EItem) items.get(i);
 				if (item.isEnabled()
 						|| R4EUIPlugin.getDefault()
 								.getPreferenceStore()
 								.getBoolean(PreferenceConstants.P_SHOW_DISABLED)) {
 
 					if (R4EUIConstants.TRUE_ATTR_VALUE_STR.equals(item.getInfoAtt().get(
 							R4EUIConstants.POSTPONED_ATTR_STR))) {
 						uiItem = new R4EUIPostponedContainer(this, item, R4EUIConstants.POSTPONED_ELEMENTS_LABEL_NAME);
 					} else if (null == item.getRepositoryRef() || "".equals(item.getRepositoryRef())) {
 						//Resource
 						EList<R4EFileContext> contextList = item.getFileContextList();
 						StringBuilder name = new StringBuilder("Resource: "); //$NON-NLS-1$
 						if (contextList.size() > 0) {
 							name = name.append(item.getFileContextList().get(0).getTarget().getName());
 						} else {
 							R4EUIPlugin.Ftracer.traceError("Context list empty in a review item, index: " + i); //$NON-NLS-1$
 						}
 						uiItem = new R4EUIReviewItem(this, item, name.toString(), "");
 					} else {
 						//Commit
 						String description = item.getDescription();
 						int endIndex = (description.length() > R4EUIConstants.END_STRING_NAME_INDEX)
 								? R4EUIConstants.END_STRING_NAME_INDEX
 								: description.length();
 						String name = "Commit: " + description.substring(R4EUIConstants.START_STRING_INDEX, endIndex)
 								+ "...";
 						uiItem = new R4EUIReviewItem(this, item, name, item.getDescription());
 					}
 
 					if (uiItem.isEnabled()) {
 						uiItem.open();
 					}
 					addChildren(uiItem);
 				}
 			}
 		}
 
 		fAnomalyContainer.open();
 		fParticipantsContainer.open();
 
 		//Check if we should show the postponed elements
 		try {
 			CommandUtils.showPostponedElements(this);
 		} catch (OutOfSyncException e) {
 			R4EUIPlugin.Ftracer.traceError("Exception: " + e.toString() + " (" + e.getMessage() + ")");
 			R4EUIPlugin.getDefault().logError("Exception: " + e.toString(), e);
 		}
 
 		fOpen = true;
 		if (fReview.getType().equals(R4EReviewType.R4E_REVIEW_TYPE_BASIC)) {
 			fImage = UIUtils.loadIcon(REVIEW_BASIC_ICON_FILE);
 		} else if (fReview.getType().equals(R4EReviewType.R4E_REVIEW_TYPE_INFORMAL)) {
 			fImage = UIUtils.loadIcon(REVIEW_INFORMAL_ICON_FILE);
 		}
 		R4EUIModelController.setActiveReview(this);
 		fireUserReviewStateChanged(this);
 
 		//Automatically import postponed anomalies if set in preferences
 		if (R4EUIPlugin.getDefault().getPreferenceStore().getBoolean(PreferenceConstants.P_AUTO_IMPORT_POSTPONED)) {
 			ImportPostponedHandler.importPostponedElements();
 		}
 	}
 
 	/**
 	 * Method setEnabled.
 	 * 
 	 * @param aEnabled
 	 *            boolean
 	 * @throws ResourceHandlingException
 	 * @throws OutOfSyncException
 	 * @see org.eclipse.mylyn.reviews.r4e.ui.internal.model.IR4EUIModelElement#setUserReviewed(boolean)
 	 */
 	@Override
 	public void setEnabled(boolean aEnabled) throws ResourceHandlingException, OutOfSyncException {
 		//NOTE we need to open the model element temporarly to be able to set the enabled state
 		fReview = R4EUIModelController.FModelExt.openR4EReview(((R4EUIReviewGroup) getParent()).getReviewGroup(),
 				fReviewName);
 		final Long bookNum = R4EUIModelController.FResourceUpdater.checkOut(fReview, R4EUIModelController.getReviewer());
 		fReview.setEnabled(aEnabled);
 		R4EUIModelController.FResourceUpdater.checkIn(bookNum);
 		R4EUIModelController.FModelExt.closeR4EReview(fReview);
 		R4EUIModelController.getNavigatorView().getTreeViewer().refresh();
 	}
 
 	/**
 	 * Method isEnabled.
 	 * 
 	 * @return boolean
 	 * @see org.eclipse.mylyn.reviews.r4e.ui.internal.model.IR4EUIModelElement#isEnabled()
 	 */
 	@Override
 	public boolean isEnabled() {
 		return fReview.isEnabled();
 	}
 
 	/**
 	 * Method setMeetingData.
 	 * 
 	 * @param aMeetingData
 	 *            IMeetingData
 	 * @throws ResourceHandlingException
 	 * @throws OutOfSyncException
 	 */
 	public void setMeetingData(IMeetingData aMeetingData) throws ResourceHandlingException, OutOfSyncException {
 		if (null != aMeetingData) {
 			final R4EMeetingData coreMeetingData = R4EUIModelController.FModelExt.createR4EMeetingData(fReview);
 			final Long bookNum = R4EUIModelController.FResourceUpdater.checkOut(coreMeetingData,
 					R4EUIModelController.getReviewer());
 			coreMeetingData.setId(aMeetingData.getCustomID());
 			coreMeetingData.setSender(aMeetingData.getSender());
 			for (String receiver : aMeetingData.getReceivers()) {
 				coreMeetingData.getReceivers().add(receiver);
 			}
 			coreMeetingData.setBody(aMeetingData.getBody());
 			coreMeetingData.setSubject(aMeetingData.getSubject());
 			coreMeetingData.setLocation(aMeetingData.getLocation());
 			coreMeetingData.setStartTime(aMeetingData.getStartTime().longValue());
 			coreMeetingData.setDuration(aMeetingData.getDuration().intValue());
 			coreMeetingData.setSentCount(coreMeetingData.getSentCount() + 1);
 			R4EUIModelController.FResourceUpdater.checkIn(bookNum);
 		}
 	}
 
 	/**
 	 * Method refreshMeetingData.
 	 * 
 	 * @throws OutOfSyncException
 	 * @throws ResourceHandlingException
 	 */
 	public void refreshMeetingData() throws ResourceHandlingException, OutOfSyncException {
 		final NotificationsConnector mailConnector = R4EUIModelController.getMailConnector();
 		final R4EMeetingData coreMeetingData = fReview.getActiveMeeting();
 		final ResourceUpdater resUpdater = R4EUIModelController.FResourceUpdater;
 
 		if (null == coreMeetingData) {
 			return;
 		}
 
 		IMeetingData meetingData = new IMeetingData() {
 
 			public int getSentCounter() {
 				return coreMeetingData.getSentCount();
 			}
 
 			public void incrementSentCounter() {
 				try {
 					final Long bookNum = resUpdater.checkOut(coreMeetingData, R4EUIModelController.getReviewer());
 					coreMeetingData.setSentCount(coreMeetingData.getSentCount() + 1);
 					resUpdater.checkIn(bookNum);
 				} catch (ResourceHandlingException e) {
 					R4EUIPlugin.Ftracer.traceError("Exception: " + e.toString() + " (" + e.getMessage() + ")");
 				} catch (OutOfSyncException e) {
 					R4EUIPlugin.Ftracer.traceError("Exception: " + e.toString() + " (" + e.getMessage() + ")");
 				}
 			}
 
 			public void clearSentCounter() {
 				try {
 					final Long bookNum = resUpdater.checkOut(coreMeetingData, R4EUIModelController.getReviewer());
 					coreMeetingData.setSentCount(0);
 					resUpdater.checkIn(bookNum);
 				} catch (ResourceHandlingException e) {
 					R4EUIPlugin.Ftracer.traceError("Exception: " + e.toString() + " (" + e.getMessage() + ")");
 				} catch (OutOfSyncException e) {
 					R4EUIPlugin.Ftracer.traceError("Exception: " + e.toString() + " (" + e.getMessage() + ")");
 				}
 			}
 
 			public String getCustomID() {
 				return coreMeetingData.getId();
 			}
 
 			public void setCustomID(String aId) {
 				try {
 					final Long bookNum = resUpdater.checkOut(coreMeetingData, R4EUIModelController.getReviewer());
 					coreMeetingData.setId(aId);
 					resUpdater.checkIn(bookNum);
 				} catch (ResourceHandlingException e) {
 					R4EUIPlugin.Ftracer.traceError("Exception: " + e.toString() + " (" + e.getMessage() + ")");
 				} catch (OutOfSyncException e) {
 					R4EUIPlugin.Ftracer.traceError("Exception: " + e.toString() + " (" + e.getMessage() + ")");
 				}
 			}
 
 			public String getSubject() {
 				return coreMeetingData.getSubject();
 			}
 
 			public void setSubject(String aSubject) {
 				try {
 					final Long bookNum = resUpdater.checkOut(coreMeetingData, R4EUIModelController.getReviewer());
 					coreMeetingData.setSubject(aSubject);
 					resUpdater.checkIn(bookNum);
 				} catch (ResourceHandlingException e) {
 					R4EUIPlugin.Ftracer.traceError("Exception: " + e.toString() + " (" + e.getMessage() + ")");
 				} catch (OutOfSyncException e) {
 					R4EUIPlugin.Ftracer.traceError("Exception: " + e.toString() + " (" + e.getMessage() + ")");
 				}
 			}
 
 			public String getBody() {
 				return coreMeetingData.getBody();
 			}
 
 			public void setBody(String aBody) {
 				try {
 					final Long bookNum = resUpdater.checkOut(coreMeetingData, R4EUIModelController.getReviewer());
 					coreMeetingData.setBody(aBody);
 					resUpdater.checkIn(bookNum);
 				} catch (ResourceHandlingException e) {
 					R4EUIPlugin.Ftracer.traceError("Exception: " + e.toString() + " (" + e.getMessage() + ")");
 				} catch (OutOfSyncException e) {
 					R4EUIPlugin.Ftracer.traceError("Exception: " + e.toString() + " (" + e.getMessage() + ")");
 				}
 			}
 
 			public String getLocation() {
 				return coreMeetingData.getLocation();
 			}
 
 			public void setLocation(String aLocation) {
 				try {
 					final Long bookNum = resUpdater.checkOut(coreMeetingData, R4EUIModelController.getReviewer());
 					coreMeetingData.setLocation(aLocation);
 					resUpdater.checkIn(bookNum);
 				} catch (ResourceHandlingException e) {
 					R4EUIPlugin.Ftracer.traceError("Exception: " + e.toString() + " (" + e.getMessage() + ")");
 				} catch (OutOfSyncException e) {
 					R4EUIPlugin.Ftracer.traceError("Exception: " + e.toString() + " (" + e.getMessage() + ")");
 				}
 			}
 
 			public Long getStartTime() {
 				return Long.valueOf(coreMeetingData.getStartTime());
 			}
 
 			public void setStartTime(Long aStartTime) {
 				try {
 					final Long bookNum = resUpdater.checkOut(coreMeetingData, R4EUIModelController.getReviewer());
 					coreMeetingData.setStartTime(aStartTime.longValue());
 					resUpdater.checkIn(bookNum);
 				} catch (ResourceHandlingException e) {
 					R4EUIPlugin.Ftracer.traceError("Exception: " + e.toString() + " (" + e.getMessage() + ")");
 				} catch (OutOfSyncException e) {
 					R4EUIPlugin.Ftracer.traceError("Exception: " + e.toString() + " (" + e.getMessage() + ")");
 				}
 			}
 
 			public Integer getDuration() {
 				return Integer.valueOf(coreMeetingData.getDuration());
 			}
 
 			public void setDuration(Integer aDuration) {
 				try {
 					final Long bookNum = resUpdater.checkOut(coreMeetingData, R4EUIModelController.getReviewer());
 					coreMeetingData.setDuration(aDuration.intValue());
 					resUpdater.checkIn(bookNum);
 				} catch (ResourceHandlingException e) {
 					R4EUIPlugin.Ftracer.traceError("Exception: " + e.toString() + " (" + e.getMessage() + ")");
 				} catch (OutOfSyncException e) {
 					R4EUIPlugin.Ftracer.traceError("Exception: " + e.toString() + " (" + e.getMessage() + ")");
 				}
 			}
 
 			public String getSender() {
 				return coreMeetingData.getSender();
 			}
 
 			public void setSender(String aSender) {
 				try {
 					final Long bookNum = resUpdater.checkOut(coreMeetingData, R4EUIModelController.getReviewer());
 					coreMeetingData.setSender(aSender);
 					resUpdater.checkIn(bookNum);
 				} catch (ResourceHandlingException e) {
 					R4EUIPlugin.Ftracer.traceError("Exception: " + e.toString() + " (" + e.getMessage() + ")");
 				} catch (OutOfSyncException e) {
 					R4EUIPlugin.Ftracer.traceError("Exception: " + e.toString() + " (" + e.getMessage() + ")");
 				}
 			}
 
 			public String[] getReceivers() {
 				final EList<String> recieversL = coreMeetingData.getReceivers();
 				final String[] receivers = recieversL.toArray(new String[recieversL.size()]);
 				return receivers;
 			}
 
 			public void clearReceivers() {
 				try {
 					final Long bookNum = resUpdater.checkOut(coreMeetingData, R4EUIModelController.getReviewer());
 					coreMeetingData.getReceivers().clear();
 					resUpdater.checkIn(bookNum);
 				} catch (ResourceHandlingException e) {
 					R4EUIPlugin.Ftracer.traceError("Exception: " + e.toString() + " (" + e.getMessage() + ")");
 				} catch (OutOfSyncException e) {
 					R4EUIPlugin.Ftracer.traceError("Exception: " + e.toString() + " (" + e.getMessage() + ")");
 				}
 			}
 
 			public void addReceiver(String aReceiver) {
 				try {
 					final Long bookNum = resUpdater.checkOut(coreMeetingData, R4EUIModelController.getReviewer());
 					coreMeetingData.getReceivers().add(aReceiver);
 					resUpdater.checkIn(bookNum);
 				} catch (ResourceHandlingException e) {
 					R4EUIPlugin.Ftracer.traceError("Exception: " + e.toString() + " (" + e.getMessage() + ")");
 				} catch (OutOfSyncException e) {
 					R4EUIPlugin.Ftracer.traceError("Exception: " + e.toString() + " (" + e.getMessage() + ")");
 				}
 			}
 
 			public void removeReceiver(String aReceiver) {
 				try {
 					final Long bookNum = resUpdater.checkOut(coreMeetingData, R4EUIModelController.getReviewer());
 					coreMeetingData.getReceivers().remove(aReceiver);
 					resUpdater.checkIn(bookNum);
 				} catch (ResourceHandlingException e) {
 					R4EUIPlugin.Ftracer.traceError("Exception: " + e.toString() + " (" + e.getMessage() + ")");
 				} catch (OutOfSyncException e) {
 					R4EUIPlugin.Ftracer.traceError("Exception: " + e.toString() + " (" + e.getMessage() + ")");
 				}
 			}
 
 		};
 
 		if (null != mailConnector) {
 			meetingData = mailConnector.fetchSystemMeetingData(meetingData, fReview.getStartDate());
 			if (null != meetingData) {
 				setMeetingData(meetingData);
 			}
 		}
 	}
 
 	//Hierarchy
 
 	/**
 	 * Method hasChildren
 	 * 
 	 * @return boolean
 	 * @see org.eclipse.mylyn.reviews.r4e.ui.internal.model.IR4EUIModelElement#hasChildren()
 	 */
 	@Override
 	public boolean hasChildren() {
 		if (isOpen()) {
 			if (fItems.size() > 0 || null != fAnomalyContainer || null != fParticipantsContainer
 					|| null != fPostponedContainer) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * Method getChildren.
 	 * 
 	 * @return IR4EUIModelElement[]
 	 * @see org.eclipse.mylyn.reviews.r4e.ui.internal.model.IR4EUIModelElement#getChildren()
 	 */
 	@Override
 	public IR4EUIModelElement[] getChildren() {
 		final List<IR4EUIModelElement> newList = new ArrayList<IR4EUIModelElement>();
 		newList.addAll(fItems);
 		newList.add(fAnomalyContainer);
 		newList.add(fParticipantsContainer);
 		if (null != fPostponedContainer) {
 			newList.add(fPostponedContainer);
 		}
 		return newList.toArray(new IR4EUIModelElement[newList.size()]);
 	}
 
 	/**
 	 * Method getReviewItems.
 	 * 
 	 * @return List<R4EUIReviewItem>
 	 */
 	public List<R4EUIReviewItem> getReviewItems() {
 		//Get review items only
 		final IR4EUIModelElement[] reviewChildren = getChildren();
 		final List<R4EUIReviewItem> reviewItems = new ArrayList<R4EUIReviewItem>();
 		for (IR4EUIModelElement child : reviewChildren) {
 			if (child instanceof R4EUIReviewItem) {
 				reviewItems.add((R4EUIReviewItem) child);
 			}
 		}
 		return reviewItems;
 	}
 
 	/**
 	 * Method addChildren.
 	 * 
 	 * @param aChildToAdd
 	 *            IR4EUIModelElement
 	 * @see org.eclipse.mylyn.reviews.r4e.ui.internal.model.IR4EUIModelElement#addChildren(IR4EUIModelElement)
 	 */
 	@Override
 	public void addChildren(IR4EUIModelElement aChildToAdd) {
 		if (aChildToAdd instanceof R4EUIPostponedContainer) {
 			fPostponedContainer = (R4EUIPostponedContainer) aChildToAdd;
 		} else if (aChildToAdd instanceof R4EUIReviewItem) {
 			fItems.add((R4EUIReviewItem) aChildToAdd);
 		} else if (aChildToAdd instanceof R4EUIAnomalyContainer) {
 			fAnomalyContainer = (R4EUIAnomalyContainer) aChildToAdd;
 		} else if (aChildToAdd instanceof R4EUIParticipantContainer) {
 			fParticipantsContainer = (R4EUIParticipantContainer) aChildToAdd;
 		}
 
 		aChildToAdd.addListener((ReviewNavigatorContentProvider) R4EUIModelController.getNavigatorView()
 				.getTreeViewer()
 				.getContentProvider());
 		fireAdd(aChildToAdd);
 	}
 
 	/**
 	 * Method createResourceReviewItem
 	 * 
 	 * @param aFilename
 	 *            String
 	 * @return R4EUIReviewItem
 	 * @throws ResourceHandlingException
 	 */
 	public R4EUIReviewItem createResourceReviewItem(String aFilename) throws ResourceHandlingException {
 
 		//Create and set review item model element
 		final R4EParticipant participant = getParticipant(R4EUIModelController.getReviewer(), true);
 		final R4EItem reviewItem = R4EUIModelController.FModelExt.createR4EItem(participant);
 
 		final String name = "Resource: " + aFilename;
 
 		//Create and set UI model element
 		final R4EUIReviewItem uiReviewItem = new R4EUIReviewItem(this, reviewItem, name, "");
 		addChildren(uiReviewItem);
 		return uiReviewItem;
 	}
 
 	/**
 	 * Method createCommitReviewItem
 	 * 
 	 * @param aChangeSet
 	 *            ChangeSet
 	 * @param aFilename
 	 *            String
 	 * @return R4EUIReviewItem
 	 * @throws ResourceHandlingException
 	 * @throws OutOfSyncException
 	 */
 	public R4EUIReviewItem createCommitReviewItem(ChangeSet aChangeSet, String aFilename)
 			throws ResourceHandlingException, OutOfSyncException {
 
 		//Create and set review item model element
 		final R4EParticipant participant = getParticipant(R4EUIModelController.getReviewer(), true);
 		final R4EItem reviewItem = R4EUIModelController.FModelExt.createR4EItem(participant);
 
 		final Long bookNum = R4EUIModelController.FResourceUpdater.checkOut(reviewItem,
 				R4EUIModelController.getReviewer());
 		reviewItem.setDescription(aChangeSet.getMessage());
 		reviewItem.setAuthorRep(aChangeSet.getAuthor().getId());
 		reviewItem.setRepositoryRef(aChangeSet.getId());
 		reviewItem.setSubmitted(aChangeSet.getDate());
 		R4EUIModelController.FResourceUpdater.checkIn(bookNum);
 		final String message = aChangeSet.getMessage();
 		final int endIndex = (message.length() > R4EUIConstants.END_STRING_NAME_INDEX)
 				? R4EUIConstants.END_STRING_NAME_INDEX
 				: message.length();
 
 		final String name = "Commit: " + message.substring(R4EUIConstants.START_STRING_INDEX, endIndex) + "...";
 
 		//Create and set UI model element
 		final R4EUIReviewItem uiReviewItem = new R4EUIReviewItem(this, reviewItem, name, reviewItem.getDescription());
 		addChildren(uiReviewItem);
 		return uiReviewItem;
 	}
 
 	/**
 	 * Method createPostponedContainer
 	 * 
 	 * @return R4EUIPostponedContainer
 	 * @throws ResourceHandlingException
 	 * @throws OutOfSyncException
 	 */
 	public R4EUIPostponedContainer createPostponedContainer() throws ResourceHandlingException, OutOfSyncException {
 
 		//Create and set postponed container model element.  We use the R4EItem as placeholder
 		final R4EParticipant participant = getParticipant(R4EUIModelController.getReviewer(), true);
 		final R4EItem reviewItem = R4EUIModelController.FModelExt.createR4EItem(participant);
 
 		final Long bookNum = R4EUIModelController.FResourceUpdater.checkOut(reviewItem,
 				R4EUIModelController.getReviewer());
 		final EMap<String, String> info = reviewItem.getInfoAtt(); //We use the R4EItem attribute map to mark this as postponed
 		info.put(R4EUIConstants.POSTPONED_ATTR_STR, R4EUIConstants.TRUE_ATTR_VALUE_STR);
 		R4EUIModelController.FResourceUpdater.checkIn(bookNum);
 
 		//Create and set UI model element
 		fPostponedContainer = new R4EUIPostponedContainer(this, reviewItem,
 				R4EUIConstants.POSTPONED_ELEMENTS_LABEL_NAME);
 		addChildren(fPostponedContainer);
 		return fPostponedContainer;
 	}
 
 	/**
 	 * Method removeChildren.
 	 * 
 	 * @param aChildToRemove
 	 *            IR4EUIModelElement
 	 * @param aFileRemove
 	 *            - also remove from file (hard remove)
 	 * @throws OutOfSyncException
 	 * @throws ResourceHandlingException
 	 * @see org.eclipse.mylyn.reviews.r4e.ui.internal.model.IR4EUIModelElement#removeChildren(IR4EUIModelElement)
 	 */
 	@Override
 	public void removeChildren(IR4EUIModelElement aChildToRemove, boolean aFileRemove)
 			throws ResourceHandlingException, OutOfSyncException {
 		if (aChildToRemove instanceof R4EUIFileContainer) {
 			final R4EUIFileContainer removedElement = (R4EUIFileContainer) aChildToRemove;
 
 			//Also recursively remove all children 
 			removedElement.removeAllChildren(aFileRemove);
 
 			/* TODO uncomment when core model supports hard-removing of elements
 			if (aFileRemove) removedElement.getItem().remove());
 			else */
 			final R4EItem modelItem = removedElement.getItem();
 			final Long bookNum = R4EUIModelController.FResourceUpdater.checkOut(modelItem,
 					R4EUIModelController.getReviewer());
 			modelItem.setEnabled(false);
 			R4EUIModelController.FResourceUpdater.checkIn(bookNum);
 
 			//Remove element from UI if the show disabled element option is off
 			if (!(R4EUIPlugin.getDefault().getPreferenceStore().getBoolean(PreferenceConstants.P_SHOW_DISABLED))) {
 				fPostponedContainer = null;
 				aChildToRemove.removeListeners();
 				fireRemove(aChildToRemove);
 			} else {
 				R4EUIModelController.getNavigatorView().getTreeViewer().refresh();
 			}
 		} else if (aChildToRemove instanceof R4EUIAnomalyContainer) {
 			fAnomalyContainer.removeAllChildren(aFileRemove);
 			if (!(R4EUIPlugin.getDefault().getPreferenceStore().getBoolean(PreferenceConstants.P_SHOW_DISABLED))) {
 				fAnomalyContainer = null;
 				aChildToRemove.removeListeners();
 				fireRemove(aChildToRemove);
 			} else {
 				R4EUIModelController.getNavigatorView().getTreeViewer().refresh();
 			}
 		} else if (aChildToRemove instanceof R4EUIParticipantContainer) {
 			fParticipantsContainer.removeAllChildren(aFileRemove);
 			if (!(R4EUIPlugin.getDefault().getPreferenceStore().getBoolean(PreferenceConstants.P_SHOW_DISABLED))) {
 				fParticipantsContainer = null;
 				aChildToRemove.removeListeners();
 				fireRemove(aChildToRemove);
 			} else {
 				R4EUIModelController.getNavigatorView().getTreeViewer().refresh();
 			}
 		}
 	}
 
 	/**
 	 * Method removeAllChildren.
 	 * 
 	 * @param aFileRemove
 	 *            boolean
 	 * @throws OutOfSyncException
 	 * @throws ResourceHandlingException
 	 * @see org.eclipse.mylyn.reviews.r4e.ui.internal.model.IR4EUIModelElement#removeAllChildren(boolean)
 	 */
 	@Override
 	public void removeAllChildren(boolean aFileRemove) throws ResourceHandlingException, OutOfSyncException {
 		//Recursively remove all children
 		for (R4EUIReviewItem item : fItems) {
 			removeChildren(item, aFileRemove);
 		}
 	}
 
 	//Listeners
 
 	/**
 	 * Method addListener.
 	 * 
 	 * @param aProvider
 	 *            ReviewNavigatorContentProvider
 	 * @see org.eclipse.mylyn.reviews.r4e.ui.internal.model.IR4EUIModelElement#addListener(ReviewNavigatorContentProvider)
 	 */
 	@Override
 	public void addListener(ReviewNavigatorContentProvider aProvider) {
 		super.addListener(aProvider);
 		if (null != fItems) {
 			R4EUIReviewItem element = null;
 			for (final Iterator<R4EUIReviewItem> iterator = fItems.iterator(); iterator.hasNext();) {
 				element = iterator.next();
 				element.addListener(aProvider);
 			}
 		}
 		if (null != fAnomalyContainer) {
 			fAnomalyContainer.addListener(aProvider);
 		}
 		if (null != fParticipantsContainer) {
 			fParticipantsContainer.addListener(aProvider);
 		}
 		if (null != fPostponedContainer) {
 			fPostponedContainer.addListener(aProvider);
 		}
 	}
 
 	/**
 	 * Method removeListener.
 	 * 
 	 * @param aProvider
 	 *            ReviewNavigatorContentProvider
 	 * @see org.eclipse.mylyn.reviews.r4e.ui.internal.model.IR4EUIModelElement#removeListener()
 	 */
 	@Override
 	public void removeListener(ReviewNavigatorContentProvider aProvider) {
 		super.removeListener(aProvider);
 		if (null != fItems) {
 			R4EUIReviewItem element = null;
 			for (final Iterator<R4EUIReviewItem> iterator = fItems.iterator(); iterator.hasNext();) {
 				element = iterator.next();
 				element.removeListener(aProvider);
 			}
 		}
 		if (null != fAnomalyContainer) {
 			fAnomalyContainer.removeListener(aProvider);
 		}
 		if (null != fParticipantsContainer) {
 			fParticipantsContainer.removeListener(aProvider);
 		}
 		if (null != fPostponedContainer) {
 			fPostponedContainer.removeListener(aProvider);
 		}
 	}
 
 	//Commands
 
 	/**
 	 * Method isChangeReviewStateCmd.
 	 * 
 	 * @return boolean
 	 * @see org.eclipse.mylyn.reviews.r4e.ui.internal.model.IR4EUIModelElement#isChangeUserReviewStateCmd()
 	 */
 	@Override
 	public boolean isChangeUserReviewStateCmd() {
 		if (isEnabled()
 				&& isOpen()
 				&& !(((R4EReviewState) fReview.getState()).getState().equals(R4EReviewPhase.R4E_REVIEW_PHASE_COMPLETED))) {
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * Method isOpenElementCmd.
 	 * 
 	 * @return boolean
 	 * @see org.eclipse.mylyn.reviews.r4e.ui.internal.model.IR4EUIModelElement#isOpenElementCmd()
 	 */
 	@Override
 	public boolean isOpenElementCmd() {
 		if (!isEnabled() || isOpen()) {
 			return false;
 		}
 		return true;
 	}
 
 	/**
 	 * Method getOpenElementCmdName.
 	 * 
 	 * @return String
 	 * @see org.eclipse.mylyn.reviews.r4e.ui.internal.model.IR4EUIModelElement#getOpenElementCmdName()
 	 */
 	@Override
 	public String getOpenElementCmdName() {
 		return OPEN_ELEMENT_COMMAND_NAME;
 	}
 
 	/**
 	 * Method getOpenElementCmdTooltip.
 	 * 
 	 * @return String
 	 * @see org.eclipse.mylyn.reviews.r4e.ui.internal.model.IR4EUIModelElement#getOpenElementCmdTooltip()
 	 */
 	@Override
 	public String getOpenElementCmdTooltip() {
 		return OPEN_ELEMENT_COMMAND_TOOLTIP;
 	}
 
 	/**
 	 * Method isReportElementCmd.
 	 * 
 	 * @return boolean
 	 * @see org.eclipse.mylyn.reviews.r4e.ui.internal.model.IR4EUIModelElement#isReportElementCmd()
 	 */
 	@Override
 	public boolean isReportElementCmd() {
 		//Any type of review, is allowed
 		if (isEnabled()) {
 			return R4EUIPlugin.isUserReportAvailable();
 		}
 		return false;
 	}
 
 	/**
 	 * Method getReportElementCmdName.
 	 * 
 	 * @return String
 	 * @see org.eclipse.mylyn.reviews.r4e.ui.internal.model.IR4EUIModelElement#getOpenElementCmdName()
 	 */
 	@Override
 	public String getReportElementCmdName() {
 		return R4EUIConstants.REPORT_ELEMENT_COMMAND_NAME;
 	}
 
 	/**
 	 * Method getReportElementCmdTooltip.
 	 * 
 	 * @return String
 	 * @see org.eclipse.mylyn.reviews.r4e.ui.internal.model.IR4EUIModelElement#getOpenElementCmdTooltip()
 	 */
 	@Override
 	public String getReportElementCmdTooltip() {
 		return REPORT_ELEMENT_COMMAND_TOOLTIP;
 	}
 
 	/**
 	 * Method isCloseElementCmd.
 	 * 
 	 * @return boolean
 	 * @see org.eclipse.mylyn.reviews.r4e.ui.internal.model.IR4EUIModelElement#isCloseElementCmd()
 	 */
 	@Override
 	public boolean isCloseElementCmd() {
 		if (isOpen()) {
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * Method getCloseElementCmdName.
 	 * 
 	 * @return String
 	 * @see org.eclipse.mylyn.reviews.r4e.ui.internal.model.IR4EUIModelElement#getCloseElementCmdName()
 	 */
 	@Override
 	public String getCloseElementCmdName() {
 		return CLOSE_ELEMENT_COMMAND_NAME;
 	}
 
 	/**
 	 * Method isNextStateElementCmd.
 	 * 
 	 * @return boolean
 	 * @see org.eclipse.mylyn.reviews.r4e.ui.internal.model.IR4EUIModelElement#isNextStateElementCmd()
 	 */
 	@Override
 	public boolean isNextStateElementCmd() {
 		if (isOpen()) {
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * Method getNextStateElementCmdName.
 	 * 
 	 * @return String
 	 * @see org.eclipse.mylyn.reviews.r4e.ui.internal.model.IR4EUIModelElement#getNextStateElementCmdName()
 	 */
 	@Override
 	public String getNextStateElementCmdName() {
 		return NEXT_STATE_ELEMENT_COMMAND_NAME;
 	}
 
 	/**
 	 * Method getNextStateElementCmdTooltip.
 	 * 
 	 * @return String
 	 * @see org.eclipse.mylyn.reviews.r4e.ui.internal.model.IR4EUIModelElement#getNextStateElementCmdTooltip()
 	 */
 	@Override
 	public String getNextStateElementCmdTooltip() {
 		return NEXT_STATE_ELEMENT_COMMAND_TOOLTIP;
 	}
 
 	/**
 	 * Method isPreviousStateElementCmd.
 	 * 
 	 * @return boolean
 	 * @see org.eclipse.mylyn.reviews.r4e.ui.internal.model.IR4EUIModelElement#isPreviousStateElementCmd()
 	 */
 	@Override
 	public boolean isPreviousStateElementCmd() {
 		if (isOpen()) {
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * Method getPreviousStateElementCmdName.
 	 * 
 	 * @return String
 	 * @see org.eclipse.mylyn.reviews.r4e.ui.internal.model.IR4EUIModelElement#getPreviousStateElementCmdName()
 	 */
 	@Override
 	public String getPreviousStateElementCmdName() {
 		return PREVIOUS_STATE_ELEMENT_COMMAND_NAME;
 	}
 
 	/**
 	 * Method getPreviousStateElementCmdTooltip.
 	 * 
 	 * @return String
 	 * @see org.eclipse.mylyn.reviews.r4e.ui.internal.model.IR4EUIModelElement#getPreviousStateElementCmdTooltip()
 	 */
 	@Override
 	public String getPreviousStateElementCmdTooltip() {
 		return PREVIOUS_STATE_ELEMENT_COMMAND_TOOLTIP;
 	}
 
 	/**
 	 * Method getCloseElementCmdTooltip.
 	 * 
 	 * @return String
 	 * @see org.eclipse.mylyn.reviews.r4e.ui.internal.model.IR4EUIModelElement#getCloseElementCmdTooltip()
 	 */
 	@Override
 	public String getCloseElementCmdTooltip() {
 		return CLOSE_ELEMENT_COMMAND_TOOLTIP;
 	}
 
 	/**
 	 * Method isRemoveElementCmd.
 	 * 
 	 * @return boolean
 	 * @see org.eclipse.mylyn.reviews.r4e.ui.internal.model.IR4EUIModelElement#isRemoveElementCmd()
 	 */
 	@Override
 	public boolean isRemoveElementCmd() {
 		if (!isOpen() && isEnabled()) {
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * Method isRestoreElementCmd.
 	 * 
 	 * @return boolean
 	 * @see org.eclipse.mylyn.reviews.r4e.ui.internal.model.IR4EUIModelElement#iisRestoreElementCmd()
 	 */
 	@Override
 	public boolean isRestoreElementCmd() {
 		if (!(getParent().isEnabled())) {
 			return false;
 		}
 		if (isOpen() || isEnabled()) {
 			return false;
 		}
 		return true;
 	}
 
 	/**
 	 * Method getRemoveElementCmdName.
 	 * 
 	 * @return String
 	 * @see org.eclipse.mylyn.reviews.r4e.ui.internal.model.IR4EUIModelElement#getRemoveElementCmdName()
 	 */
 	@Override
 	public String getRemoveElementCmdName() {
 		return REMOVE_ELEMENT_COMMAND_NAME;
 	}
 
 	/**
 	 * Method getRemoveElementCmdTooltip.
 	 * 
 	 * @return String
 	 * @see org.eclipse.mylyn.reviews.r4e.ui.internal.model.IR4EUIModelElement#getRemoveElementCmdTooltip()
 	 */
 	@Override
 	public String getRemoveElementCmdTooltip() {
 		return REMOVE_ELEMENT_COMMAND_TOOLTIP;
 	}
 
 	/**
 	 * Method isSendEmailCmd.
 	 * 
 	 * @return boolean
 	 * @see org.eclipse.mylyn.reviews.r4e.ui.internal.model.IR4EUIModelElement#isSendEmailCmd()
 	 */
 	@Override
 	public boolean isSendEmailCmd() {
 		if (isOpen()) {
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * Method isImportPostponedCmd.
 	 * 
 	 * @return boolean
 	 * @see org.eclipse.mylyn.reviews.r4e.ui.internal.model.IR4EUIModelElement#isImportPostponedCmd()
 	 */
 	@Override
 	public boolean isImportPostponedCmd() {
 		if ((!getReview().getType().equals(R4EReviewType.R4E_REVIEW_TYPE_BASIC))
 				&& isOpen()
 				&& !(((R4EReviewState) fReview.getState()).getState().equals(R4EReviewPhase.R4E_REVIEW_PHASE_COMPLETED))) {
 			return true;
 		}
 		return false;
 	}
 
 	//Phase Management
 
 	/**
 	 * Method setDate.
 	 * 
 	 * @param aNewPhase
 	 *            R4EReviewPhase
 	 * @throws OutOfSyncException
 	 * @throws ResourceHandlingException
 	 */
 	public void setDate(R4EReviewPhase aNewPhase) throws ResourceHandlingException, OutOfSyncException {
 		final Long bookNum = R4EUIModelController.FResourceUpdater.checkOut(fReview, R4EUIModelController.getReviewer());
 		final Date date = Calendar.getInstance().getTime();
 		if (aNewPhase.equals(R4EReviewPhase.R4E_REVIEW_PHASE_PREPARATION)) {
 			((R4EFormalReview) fReview).getCurrent().setStartDate(date);
 		} else if (aNewPhase.equals(R4EReviewPhase.R4E_REVIEW_PHASE_DECISION)) {
 			((R4EFormalReview) fReview).getCurrent().setStartDate(date);
 		} else if (aNewPhase.equals(R4EReviewPhase.R4E_REVIEW_PHASE_REWORK)) {
 			((R4EFormalReview) fReview).getCurrent().setStartDate(date);
 		} else if (aNewPhase.equals(R4EReviewPhase.R4E_REVIEW_PHASE_COMPLETED)) {
 			fReview.setEndDate(date);
 		} else {
 			fReview.setEndDate(null);
 		}
 		R4EUIModelController.FResourceUpdater.checkIn(bookNum);
 	}
 
 	/**
 	 * Method updatePhase.
 	 * 
 	 * @param aNewPhase
 	 *            R4EReviewPhase
 	 * @throws OutOfSyncException
 	 * @throws ResourceHandlingException
 	 */
 	public void updatePhase(R4EReviewPhase aNewPhase) throws ResourceHandlingException, OutOfSyncException {
 		//Update review state
 		final Long bookNum = R4EUIModelController.FResourceUpdater.checkOut(fReview, R4EUIModelController.getReviewer());
 		((R4EReviewState) fReview.getState()).setState(aNewPhase);
 		//Set end date when the review is completed
 		if (aNewPhase.equals(R4EReviewPhase.R4E_REVIEW_PHASE_COMPLETED)) {
 			R4EUIModelController.getActiveReview().getReview().setEndDate(Calendar.getInstance().getTime());
 		} else {
 			R4EUIModelController.getActiveReview().getReview().setEndDate(null);
 		}
 		R4EUIModelController.FResourceUpdater.checkIn(bookNum);
 	}
 
 	/**
 	 * Method getPhaseString.
 	 * 
 	 * @param aNewPhase
 	 *            R4EReviewPhase
 	 * @return String
 	 */
 	public String getPhaseString(R4EReviewPhase aNewPhase) {
 		if (aNewPhase.equals(R4EReviewPhase.R4E_REVIEW_PHASE_STARTED)) {
 			return R4EUIConstants.REVIEW_PHASE_STARTED;
 		} else if (aNewPhase.equals(R4EReviewPhase.R4E_REVIEW_PHASE_COMPLETED)) {
 			return R4EUIConstants.REVIEW_PHASE_COMPLETED;
 		} else {
 			return "";
 		}
 	}
 
 	/**
 	 * Method getStateFromString.
 	 * 
 	 * @param aNewPhase
 	 *            String
 	 * @return R4EReviewPhase
 	 */
 	public R4EReviewPhase getPhaseFromString(String aNewPhase) {
 		if (aNewPhase.equals(R4EUIConstants.REVIEW_PHASE_STARTED)) {
 			return R4EReviewPhase.R4E_REVIEW_PHASE_STARTED;
 		} else if (aNewPhase.equals(R4EUIConstants.REVIEW_PHASE_COMPLETED)) {
 			return R4EReviewPhase.R4E_REVIEW_PHASE_COMPLETED;
 		} else {
 			return null; //should never happen
 		}
 	}
 
 	/**
 	 * Method getPhases.
 	 * 
 	 * @return String[]
 	 */
 	public String[] getPhases() {
 		return BASIC_PHASE_VALUES;
 	}
 
 	/**
 	 * Method getAvailablePhases.
 	 * 
 	 * @return String[]
 	 */
 	public String[] getAvailablePhases() {
 		//Peek state machine to get available states
 		final R4EReviewPhase[] phases = getAllowedPhases(((R4EReviewState) getReview().getState()).getState());
 		final List<String> phaseStrings = new ArrayList<String>();
 		for (R4EReviewPhase phase : phases) {
 			phaseStrings.add(getPhaseString(phase));
 		}
 		return phaseStrings.toArray(new String[phaseStrings.size()]);
 	}
 
 	/**
 	 * Method mapPhaseToIndex.
 	 * 
 	 * @param aPhase
 	 *            R4EReviewPhase
 	 * @return int
 	 */
 	public int mapPhaseToIndex(R4EReviewPhase aPhase) {
 		//Peek state machine to get available states
 		final R4EReviewPhase[] phases = getAllowedPhases(((R4EReviewState) getReview().getState()).getState());
 		for (int i = 0; i < phases.length; i++) {
 			if (phases[i].getValue() == aPhase.getValue()) {
 				return i;
 			}
 		}
 		return R4EUIConstants.INVALID_VALUE; //should never happen
 	}
 
 	/**
 	 * Method getAllowedPhases.
 	 * 
 	 * @param aCurrentPhase
 	 *            R4EReviewPhase
 	 * @return R4EReviewPhase[]
 	 */
 	protected R4EReviewPhase[] getAllowedPhases(R4EReviewPhase aCurrentPhase) {
 		final List<R4EReviewPhase> phases = new ArrayList<R4EReviewPhase>();
 
 		switch (aCurrentPhase.getValue()) {
 		case R4EReviewPhase.R4E_REVIEW_PHASE_STARTED_VALUE:
 			phases.add(R4EReviewPhase.R4E_REVIEW_PHASE_STARTED);
 			phases.add(R4EReviewPhase.R4E_REVIEW_PHASE_COMPLETED);
 			break;
 
 		case R4EReviewPhase.R4E_REVIEW_PHASE_COMPLETED_VALUE:
 			phases.add(R4EReviewPhase.R4E_REVIEW_PHASE_COMPLETED);
 			phases.add(R4EReviewPhase.R4E_REVIEW_PHASE_STARTED);
 			break;
 
 		default:
 			//should never happen
 		}
 
 		return phases.toArray(new R4EReviewPhase[phases.size()]);
 	}
 
 	/**
 	 * Method validatePhaseChange.
 	 * 
 	 * @param aNextPhase
 	 *            - R4EReviewPhase
 	 * @param aErrorMessage
 	 *            - AtomicReference<String>
 	 * @return R4EReviewDecision
 	 */
 	public boolean validatePhaseChange(R4EReviewPhase aNextPhase, AtomicReference<String> aErrorMessage) { // $codepro.audit.disable booleanMethodNamingConvention
 
 		switch (aNextPhase.getValue()) {
 		case R4EReviewPhase.R4E_REVIEW_PHASE_COMPLETED_VALUE:
 			if (!checkCompletionStatus(aErrorMessage)) {
 				return false;
 			}
 			break;
 
 		default:
 			//Nothing to do
 		}
 		return true;
 	}
 
 	/**
 	 * Method checkCompletionStatus.
 	 * 
 	 * @param aErrorMessage
 	 *            AtomicReference<String>
 	 * @return boolean
 	 */
 	public boolean checkCompletionStatus(AtomicReference<String> aErrorMessage) { // $codepro.audit.disable booleanMethodNamingConvention
 		if (!(fReview.getType().equals(R4EReviewType.R4E_REVIEW_TYPE_BASIC))) {
 			if (null == fReview.getDecision() || null == fReview.getDecision().getValue()) {
 				aErrorMessage.set("Phase cannot be changed to " + R4EUIConstants.REVIEW_PHASE_COMPLETED
 						+ " as review exit decision information is missing");
 				return false;
 			}
 			if (fReview.getDecision().getValue().equals(R4EDecision.R4E_REVIEW_DECISION_NONE)) {
 				aErrorMessage.set("Phase cannot be changed to " + R4EUIConstants.REVIEW_PHASE_COMPLETED
 						+ " as review exit decision information is set to NONE");
 				return false;
 			}
 			if (fReview.getDecision().getValue().equals(R4EDecision.R4E_REVIEW_DECISION_REJECTED)) {
 				aErrorMessage.set("Phase cannot be changed to " + R4EUIConstants.REVIEW_PHASE_COMPLETED
 						+ " as review exit decision information is set to REJECTED");
 				return true;
 			}
 
 			//Check global anomalies state
 			final AtomicReference<String> resultMsg = new AtomicReference<String>(null);
 			final StringBuilder sb = new StringBuilder();
 			boolean resultOk = true;
 			if (!(fAnomalyContainer.checkCompletionStatus(resultMsg))) {
 				sb.append("Phase cannot be changed to " + R4EUIConstants.REVIEW_PHASE_COMPLETED
 						+ " as some anomalies are in the wrong state:" + System.getProperty("line.separator"));
 				sb.append(resultMsg);
 				resultOk = false;
 			}
 
 			for (R4EUIReviewItem item : fItems) {
 				R4EUIFileContext[] contexts = (R4EUIFileContext[]) item.getChildren();
 				for (R4EUIFileContext context : contexts) {
 					R4EUIAnomalyContainer container = (R4EUIAnomalyContainer) context.getAnomalyContainerElement();
 					if (null != container && !(container.checkCompletionStatus(resultMsg))) {
 						if (resultOk) {
 							sb.append("Phase cannot be changed to " + R4EUIConstants.REVIEW_PHASE_COMPLETED
 									+ " as some anomalies are in the wrong state:"
 									+ System.getProperty("line.separator"));
 							resultOk = false;
 						}
 						if (null != resultMsg) {
 							sb.append(resultMsg);
 						}
 					}
 				}
 			}
 			if (!resultOk) {
 				aErrorMessage.set(sb.toString());
 				return false;
 			}
 		}
 		return true;
 	}
 
 	/**
 	 * Method isExitDecisionEnabled.
 	 * 
 	 * @return boolean
 	 */
 	public boolean isExitDecisionEnabled() {
 		if (((R4EReviewState) fReview.getState()).getState().equals(R4EReviewPhase.R4E_REVIEW_PHASE_COMPLETED)) {
 			return false;
 		}
 		return true;
 	}
 
 	/**
 	 * Method getExitDecisionValues.
 	 * 
 	 * @return String[]
 	 */
 	public static String[] getExitDecisionValues() {
 		return DECISION_VALUES;
 	}
 
 	/**
 	 * Method getDecisionValueFromString.
 	 * 
 	 * @param aDecision
 	 *            - String
 	 * @return R4EReviewDecision
 	 */
 	public static R4EReviewDecision getDecisionValueFromString(String aDecision) {
 		final R4EReviewDecision reviewDecision = RModelFactoryExt.eINSTANCE.createR4EReviewDecision();
 		if (aDecision.equals(EXIT_DECISION_ACCEPTED)) {
 			reviewDecision.setValue(R4EDecision.R4E_REVIEW_DECISION_ACCEPTED);
 		} else if (aDecision.equals(EXIT_DECISION_ACCEPTED_FOLLOWUP)) {
 			reviewDecision.setValue(R4EDecision.R4E_REVIEW_DECISION_ACCEPTED_FOLLOWUP);
 		} else if (aDecision.equals(EXIT_DECISION_REJECTED)) {
 			reviewDecision.setValue(R4EDecision.R4E_REVIEW_DECISION_REJECTED);
 		} else {
 			reviewDecision.setValue(R4EDecision.R4E_REVIEW_DECISION_NONE);
 		}
 		return reviewDecision;
 	}
 }
