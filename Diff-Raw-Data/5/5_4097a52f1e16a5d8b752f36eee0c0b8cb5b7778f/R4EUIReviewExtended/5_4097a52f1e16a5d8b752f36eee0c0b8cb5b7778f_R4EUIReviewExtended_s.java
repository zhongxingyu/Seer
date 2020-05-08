 /*******************************************************************************
  * Copyright (c) 2011, 2012 Ericsson AB and others.
  * 
  * All rights reserved. This program and the accompanying materials are
  * made available under the terms of the Eclipse Public License v1.0 which
  * accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Description:
  * 
  * This class extedns the Review element of the UI model to add 
  * implementation for formal reviews
  * 
  * Contributors:
  *   Sebastien Dubois - Created for Mylyn Review R4E project
  *   
  *******************************************************************************/
 
 package org.eclipse.mylyn.reviews.r4e.ui.internal.model;
 
 import java.io.FileNotFoundException;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collection;
 import java.util.List;
 import java.util.concurrent.atomic.AtomicReference;
 
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EDecision;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EFormalReview;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EReview;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EReviewPhase;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EReviewPhaseInfo;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EReviewState;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EReviewType;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EUser;
 import org.eclipse.mylyn.reviews.r4e.core.model.serial.impl.CompatibilityException;
 import org.eclipse.mylyn.reviews.r4e.core.model.serial.impl.OutOfSyncException;
 import org.eclipse.mylyn.reviews.r4e.core.model.serial.impl.ResourceHandlingException;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.properties.general.ReviewProperties;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.utils.R4EUIConstants;
 import org.eclipse.ui.views.properties.IPropertySource;
 
 /**
  * @author Sebastien Dubois
  * @version $Revision: 1.0 $
  */
 public class R4EUIReviewExtended extends R4EUIReviewBasic {
 
 	// ------------------------------------------------------------------------
 	// Constants
 	// ------------------------------------------------------------------------
 
 	/**
 	 * Field REVIEW_FORMAL_ICON_FILE. (value is ""icons/obj16/reviewfrm_obj.gif"")
 	 */
 	public static final String REVIEW_FORMAL_ICON_FILE = "icons/obj16/reviewfrm_obj.gif";
 
 	/**
 	 * Field REVIEW_FORMAL_CLOSED_ICON_FILE. (value is ""icons/obj16/revfrmclsd_obj.gif"")
 	 */
 	public static final String REVIEW_FORMAL_CLOSED_ICON_FILE = "icons/obj16/revfrmclsd_obj.gif";
 
 	/**
 	 * Field REVIEW_PHASE_REWORK. (value is ""REWORK"")
 	 */
 	protected static final String REVIEW_PHASE_REWORK = "REWORK";
 
 	/**
 	 * Field FFormalPhaseValues.
 	 */
 	private static final String[] FORMAL_PHASE_VALUES = { R4EUIConstants.PHASE_PLANNING_LABEL,
 			R4EUIConstants.PHASE_PREPARATION_LABEL, R4EUIConstants.PHASE_DECISION_LABEL,
 			R4EUIConstants.PHASE_REWORK_LABEL, R4EUIConstants.PHASE_COMPLETED_LABEL }; //NOTE: This has to match R4EReviewPhase in R4E core plugin
 
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
 	public R4EUIReviewExtended(R4EUIReviewGroup aParent, R4EReview aReview, R4EReviewType aType, boolean aOpen) {
 		super(aParent, aReview, aType, aOpen);
 		if (aOpen) {
 			setImage(REVIEW_FORMAL_ICON_FILE);
 		} else {
 			setImage(REVIEW_FORMAL_CLOSED_ICON_FILE);
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
 
 	/**
 	 * Close the model element (i.e. disable it)
 	 * 
 	 * @see org.eclipse.mylyn.reviews.r4e.ui.internal.model.IR4EUIModelElement#close()
 	 */
 	@Override
 	public void close() {
 		super.close();
 		setImage(REVIEW_FORMAL_CLOSED_ICON_FILE);
 	}
 
 	/**
 	 * Open the model element (i.e. enable it)
 	 * 
 	 * @throws ResourceHandlingException
 	 * @throws FileNotFoundException
 	 * @throws CompatibilityException
 	 * @see org.eclipse.mylyn.reviews.r4e.ui.internal.model.IR4EUIModelElement#open()
 	 */
 	@Override
 	public void open() throws ResourceHandlingException, FileNotFoundException, CompatibilityException {
 		super.open();
 		setImage(REVIEW_FORMAL_ICON_FILE);
 	}
 
 	//Commands
 
 	/**
 	 * Method isNextStateElementCmd.
 	 * 
 	 * @return boolean
 	 * @see org.eclipse.mylyn.reviews.r4e.ui.internal.model.IR4EUIModelElement#isNextStateElementCmd()
 	 */
 	@Override
 	public boolean isNextStateElementCmd() {
 		if (isOpen() && !isReadOnly() && 0 < getNextAvailablePhases().length) {
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * Method isPreviousStateElementCmd.
 	 * 
 	 * @return boolean
 	 * @see org.eclipse.mylyn.reviews.r4e.ui.internal.model.IR4EUIModelElement#isPreviousStateElementCmd()
 	 */
 	@Override
 	public boolean isPreviousStateElementCmd() {
 		if (isOpen() && !isReadOnly() && null != getPreviousPhase()) {
 			return true;
 		}
 		return false;
 	}
 
 	//Phase Management
 
 	/**
 	 * Method updatePhase.
 	 * 
 	 * @param aNewPhase
 	 *            R4EReviewPhase
 	 * @throws OutOfSyncException
 	 * @throws ResourceHandlingException
 	 */
 	@Override
 	public void updatePhase(R4EReviewPhase aNewPhase) throws ResourceHandlingException, OutOfSyncException {
 		final R4EFormalReview formalReview = (R4EFormalReview) fReview;
 
 		//Set old phase info
 		String owner = null;
 		Long bookNum = null;
 		if (null != formalReview.getCurrent()) {
 			owner = formalReview.getCurrent().getPhaseOwnerID();
 			bookNum = R4EUIModelController.FResourceUpdater.checkOut(fReview, R4EUIModelController.getReviewer());
 			setOldPhaseData(aNewPhase, formalReview.getCurrent());
 			R4EUIModelController.FResourceUpdater.checkIn(bookNum);
 		}
 
 		//Check if the phase already exists
 		R4EReviewPhaseInfo newPhase = null;
 		final EList<R4EReviewPhaseInfo> phases = formalReview.getPhases();
 		for (R4EReviewPhaseInfo phase : phases) {
 			if (phase.getType().equals(aNewPhase)) {
 				newPhase = phase;
 				break;
 			}
 		}
 		//If we did not find the phase, create it
 		if (null == newPhase) {
 			newPhase = R4EUIModelController.FModelExt.createR4EReviewPhaseInfo(formalReview);
 		}
 
 		//Set new phase info
 		bookNum = R4EUIModelController.FResourceUpdater.checkOut(fReview, R4EUIModelController.getReviewer());
 		newPhase.setStartDate(Calendar.getInstance().getTime());
 		newPhase.setType(aNewPhase);
 		if (null == formalReview.getCurrent()) {
 			newPhase.setPhaseOwnerID(R4EUIModelController.getReviewer());
 		} else {
 			newPhase.setPhaseOwnerID(owner); //Keep same owner for next phase
 		}
 		formalReview.setCurrent(newPhase);
 		R4EUIModelController.FResourceUpdater.checkIn(bookNum);
 
 		//Set common data
 		super.updatePhase(aNewPhase);
 
 		//Update header
 		setName(getPhaseString(aNewPhase) + ": " + fReview.getName());
 	}
 
 	/**
 	 * Method getPhaseString.
 	 * 
 	 * @param aNewPhase
 	 *            R4EReviewPhase
 	 * @return String
 	 */
 	@Override
 	public String getPhaseString(R4EReviewPhase aNewPhase) {
 		if (aNewPhase.equals(R4EReviewPhase.R4E_REVIEW_PHASE_STARTED)) {
 			return R4EUIConstants.PHASE_PLANNING_LABEL;
 		} else if (aNewPhase.equals(R4EReviewPhase.R4E_REVIEW_PHASE_PREPARATION)) {
 			return R4EUIConstants.PHASE_PREPARATION_LABEL;
 		} else if (aNewPhase.equals(R4EReviewPhase.R4E_REVIEW_PHASE_DECISION)) {
 			return R4EUIConstants.PHASE_DECISION_LABEL;
 		} else if (aNewPhase.equals(R4EReviewPhase.R4E_REVIEW_PHASE_REWORK)) {
 			return R4EUIConstants.PHASE_REWORK_LABEL;
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
 	@Override
 	public R4EReviewPhase getPhaseFromString(String aNewPhase) {
 		if (aNewPhase.equals(R4EUIConstants.PHASE_PLANNING_LABEL)) {
 			return R4EReviewPhase.R4E_REVIEW_PHASE_STARTED;
 		} else if (aNewPhase.equals(R4EUIConstants.PHASE_PREPARATION_LABEL)) {
 			return R4EReviewPhase.R4E_REVIEW_PHASE_PREPARATION;
 		} else if (aNewPhase.equals(R4EUIConstants.PHASE_DECISION_LABEL)) {
 			return R4EReviewPhase.R4E_REVIEW_PHASE_DECISION;
 		} else if (aNewPhase.equals(R4EUIConstants.PHASE_REWORK_LABEL)) {
 			return R4EReviewPhase.R4E_REVIEW_PHASE_REWORK;
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
 	public static String[] getFormalPhases() {
 		return FORMAL_PHASE_VALUES;
 	}
 
 	/**
 	 * Method getAvailablePhases.
 	 * 
 	 * @return String[]
 	 */
 	@Override
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
 	 * Method getNextAvailablePhases.
 	 * 
 	 * @return String[]
 	 */
 	public String[] getNextAvailablePhases() {
 		//Peek state machine to get next available states
 		final R4EReviewPhase[] phases = getNextAllowedPhases(((R4EReviewState) getReview().getState()).getState());
 		final List<String> phaseStrings = new ArrayList<String>();
 		for (R4EReviewPhase phase : phases) {
 			phaseStrings.add(getPhaseString(phase));
 		}
 		return phaseStrings.toArray(new String[phaseStrings.size()]);
 	}
 
 	/**
 	 * Method getPreviousPhase.
 	 * 
 	 * @return R4EReviewPhase
 	 */
 	public R4EReviewPhase getPreviousPhase() {
 		final R4EReviewPhase currentPhase = ((R4EReviewState) getReview().getState()).getState();
 
 		switch (currentPhase.getValue()) {
 
 		case R4EReviewPhase.R4E_REVIEW_PHASE_DECISION_VALUE:
 			return R4EReviewPhase.R4E_REVIEW_PHASE_PREPARATION;
 
 		case R4EReviewPhase.R4E_REVIEW_PHASE_REWORK_VALUE:
 			return R4EReviewPhase.R4E_REVIEW_PHASE_DECISION;
 
 		case R4EReviewPhase.R4E_REVIEW_PHASE_COMPLETED_VALUE:
 			return R4EReviewPhase.R4E_REVIEW_PHASE_REWORK;
 
 		default:
 			//should never happen
 			return null;
 		}
 	}
 
 	/**
 	 * Method mapPhaseToIndex.
 	 * 
 	 * @param aPhase
 	 *            R4EReviewPhase
 	 * @return int
 	 */
 	@Override
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
 	 * Method getPhaseInfo.
 	 * 
 	 * @param aPhase
 	 *            R4EReviewPhase
 	 * @return R4EReviewPhaseInfo
 	 */
 	public R4EReviewPhaseInfo getPhaseInfo(R4EReviewPhase aPhase) {
 		for (R4EReviewPhaseInfo phase : ((R4EFormalReview) fReview).getPhases()) {
 			if (phase.getType().equals(aPhase)) {
 				return phase;
 			}
 		}
 		return null;
 	}
 
 	//Review State Machine
 
 	/**
 	 * Method getAllowedPhases.
 	 * 
 	 * @param aCurrentPhase
 	 *            R4EReviewPhase
 	 * @return R4EReviewPhase[]
 	 */
 	@Override
 	protected R4EReviewPhase[] getAllowedPhases(R4EReviewPhase aCurrentPhase) {
 		final List<R4EReviewPhase> phases = new ArrayList<R4EReviewPhase>();
 
 		switch (aCurrentPhase.getValue()) {
 		case R4EReviewPhase.R4E_REVIEW_PHASE_STARTED_VALUE:
 			phases.add(R4EReviewPhase.R4E_REVIEW_PHASE_STARTED);
 			phases.add(R4EReviewPhase.R4E_REVIEW_PHASE_PREPARATION);
 			break;
 
 		case R4EReviewPhase.R4E_REVIEW_PHASE_PREPARATION_VALUE:
 			phases.add(R4EReviewPhase.R4E_REVIEW_PHASE_PREPARATION);
 			phases.add(R4EReviewPhase.R4E_REVIEW_PHASE_DECISION);
 			break;
 
 		case R4EReviewPhase.R4E_REVIEW_PHASE_DECISION_VALUE:
 			phases.add(R4EReviewPhase.R4E_REVIEW_PHASE_PREPARATION);
 			phases.add(R4EReviewPhase.R4E_REVIEW_PHASE_DECISION);
 			phases.add(R4EReviewPhase.R4E_REVIEW_PHASE_REWORK);
 			phases.add(R4EReviewPhase.R4E_REVIEW_PHASE_COMPLETED);
 			break;
 
 		case R4EReviewPhase.R4E_REVIEW_PHASE_REWORK_VALUE:
 			phases.add(R4EReviewPhase.R4E_REVIEW_PHASE_DECISION);
 			phases.add(R4EReviewPhase.R4E_REVIEW_PHASE_REWORK);
 			phases.add(R4EReviewPhase.R4E_REVIEW_PHASE_COMPLETED);
 			break;
 
 		case R4EReviewPhase.R4E_REVIEW_PHASE_COMPLETED_VALUE:
 			phases.add(R4EReviewPhase.R4E_REVIEW_PHASE_DECISION);
 			phases.add(R4EReviewPhase.R4E_REVIEW_PHASE_REWORK);
 			phases.add(R4EReviewPhase.R4E_REVIEW_PHASE_COMPLETED);
 			break;
 
 		default:
 			//should never happen
 		}
 
 		return phases.toArray(new R4EReviewPhase[phases.size()]);
 	}
 
 	/**
 	 * Method getNextAllowedPhases.
 	 * 
 	 * @param aCurrentPhase
 	 *            R4EReviewPhase
 	 * @return R4EReviewPhase[]
 	 */
 	protected R4EReviewPhase[] getNextAllowedPhases(R4EReviewPhase aCurrentPhase) {
 		final List<R4EReviewPhase> phases = new ArrayList<R4EReviewPhase>();
 
 		switch (aCurrentPhase.getValue()) {
 		case R4EReviewPhase.R4E_REVIEW_PHASE_STARTED_VALUE:
 			phases.add(R4EReviewPhase.R4E_REVIEW_PHASE_PREPARATION);
 			break;
 
 		case R4EReviewPhase.R4E_REVIEW_PHASE_PREPARATION_VALUE:
 			phases.add(R4EReviewPhase.R4E_REVIEW_PHASE_DECISION);
 			break;
 
 		case R4EReviewPhase.R4E_REVIEW_PHASE_DECISION_VALUE:
 			phases.add(R4EReviewPhase.R4E_REVIEW_PHASE_REWORK);
 			phases.add(R4EReviewPhase.R4E_REVIEW_PHASE_COMPLETED);
 			break;
 
 		case R4EReviewPhase.R4E_REVIEW_PHASE_REWORK_VALUE:
 			phases.add(R4EReviewPhase.R4E_REVIEW_PHASE_COMPLETED);
 			break;
 
 		default:
 			//should never happen
 		}
 
 		return phases.toArray(new R4EReviewPhase[phases.size()]);
 	}
 
 	/**
 	 * Method setOldPhaseData.
 	 * 
 	 * @param aNewPhase
 	 *            R4EReviewPhase
 	 * @param aOldPhaseInfo
 	 *            R4EReviewPhaseInfo
 	 */
 	public void setOldPhaseData(R4EReviewPhase aNewPhase, R4EReviewPhaseInfo aOldPhaseInfo) {
 		boolean clearOldPhaseData = false;
 
 		switch (aNewPhase.getValue()) {
 		case R4EReviewPhase.R4E_REVIEW_PHASE_STARTED_VALUE:
 			//nothing to do
 			break;
 
 		case R4EReviewPhase.R4E_REVIEW_PHASE_PREPARATION_VALUE:
 			if (aOldPhaseInfo.getType().equals(R4EReviewPhase.R4E_REVIEW_PHASE_DECISION)) {
 				clearOldPhaseData = true;
 			}
 			break;
 
 		case R4EReviewPhase.R4E_REVIEW_PHASE_DECISION_VALUE:
 			if (aOldPhaseInfo.getType().equals(R4EReviewPhase.R4E_REVIEW_PHASE_REWORK)
 					|| aOldPhaseInfo.getType().equals(R4EReviewPhase.R4E_REVIEW_PHASE_COMPLETED)) {
 				clearOldPhaseData = true;
 			}
 			break;
 
 		case R4EReviewPhase.R4E_REVIEW_PHASE_REWORK_VALUE:
 			if (aOldPhaseInfo.getType().equals(R4EReviewPhase.R4E_REVIEW_PHASE_COMPLETED)) {
 				clearOldPhaseData = true;
 			}
 			break;
 
 		case R4EReviewPhase.R4E_REVIEW_PHASE_COMPLETED_VALUE:
 			//nothing to do
 			break;
 
 		default:
 			//should never happen
 		}
 
 		if (clearOldPhaseData) {
 			aOldPhaseInfo.setPhaseOwnerID("");
 			aOldPhaseInfo.setStartDate(null);
 			aOldPhaseInfo.setEndDate(null);
 		} else {
 			aOldPhaseInfo.setEndDate(Calendar.getInstance().getTime());
 		}
 	}
 
 	/**
 	 * Method validatePhaseChange.
 	 * 
 	 * @param aNextPhase
 	 *            R4EReviewPhase
 	 * @param aErrorMessage
 	 *            AtomicReference<String>
 	 * @return R4EReviewPhase[]
 	 */
 	@Override
 	public boolean validatePhaseChange(R4EReviewPhase aNextPhase, AtomicReference<String> aErrorMessage) {
 
 		if (!R4EUIModelController.getReviewer().equals(((R4EFormalReview) fReview).getCurrent().getPhaseOwnerID())) {
 			aErrorMessage.set("Phase cannot be changed as you are not the phase owner");
 			return false;
 		}
 
 		switch (aNextPhase.getValue()) {
 		case R4EReviewPhase.R4E_REVIEW_PHASE_PREPARATION_VALUE:
 			//No other constraint
 			break;
 
 		case R4EReviewPhase.R4E_REVIEW_PHASE_DECISION_VALUE:
 			if (((R4EFormalReview) fReview).getCurrent().getType().equals(R4EReviewPhase.R4E_REVIEW_PHASE_PREPARATION)) {
 				//Check if all reviewers are done, otherwise do not prevent phase change, but notify phase owner
 				final Collection<R4EUser> users = fReview.getUsersMap().values();
 				final List<String> pendingUsers = new ArrayList<String>();
 				for (R4EUser user : users) {
 					if (!user.isReviewCompleted()) {
 						pendingUsers.add(user.getId());
 					}
 				}
 
 				if (pendingUsers.size() > 0) {
 					aErrorMessage.set("Take note that the following reviewers did not complete the preparation phase: "
 							+ pendingUsers.toString());
 				}
 			}
 			break;
 
 		case R4EReviewPhase.R4E_REVIEW_PHASE_REWORK_VALUE:
 			if (!checkReworkStatus(aErrorMessage)) {
 				return false;
 			}
 			break;
 
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
 	 * Method checkReworkStatus.
 	 * 
 	 * @param aErrorMessage
 	 *            AtomicReference<String>
 	 * @return boolean
 	 */
 	public boolean checkReworkStatus(AtomicReference<String> aErrorMessage) { // $codepro.audit.disable booleanMethodNamingConvention
 		if (null == fReview.getDecision() || null == fReview.getDecision().getValue()) {
 			aErrorMessage.set("Phase cannot be changed to " + REVIEW_PHASE_REWORK
 					+ " as review exit decision information is missing");
 			return false;
 		}
 		if (fReview.getDecision().getValue().equals(R4EDecision.R4E_REVIEW_DECISION_NONE)) {
 			aErrorMessage.set("Phase cannot be changed to " + REVIEW_PHASE_REWORK
 					+ " as review exit decision information is set to NONE");
 			return false;
 		}
 		if (fReview.getDecision().getValue().equals(R4EDecision.R4E_REVIEW_DECISION_REJECTED)) {
 			aErrorMessage.set("Phase cannot be changed to " + REVIEW_PHASE_REWORK
 					+ " as review exit decision information is set to REJECTED");
 			return false;
 		}
 
 		//Check global anomalies state
 		final AtomicReference<String> resultMsg = new AtomicReference<String>(null);
 		boolean resultOk = true;
 		final StringBuilder sb = new StringBuilder();
 		if (!(fAnomalyContainer.checkReworkStatus(resultMsg))) {
 			sb.append("Phase cannot be changed to " + REVIEW_PHASE_REWORK
 					+ " as some anomalies are in the wrong state:" + R4EUIConstants.LINE_FEED);
 			sb.append(resultMsg);
 			resultOk = false;
 		}
 
 		for (R4EUIReviewItem item : fItems) {
 			R4EUIFileContext[] contexts = (R4EUIFileContext[]) item.getChildren();
 			for (R4EUIFileContext context : contexts) {
 				R4EUIAnomalyContainer container = context.getAnomalyContainerElement();
 				if (!(container.checkReworkStatus(resultMsg))) {
 					if (resultOk) {
 						sb.append("Phase cannot be changed to " + REVIEW_PHASE_REWORK
 								+ " as some anomalies are in the wrong state:" + R4EUIConstants.LINE_FEED);
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
 		return true;
 	}
 
 	/**
 	 * Method isParticipantExtraDetailsEnabled.
 	 * 
 	 * @return boolean
 	 */
 	public boolean isParticipantExtraDetailsEnabled() {
 		if (((R4EReviewState) fReview.getState()).getState().equals(R4EReviewPhase.R4E_REVIEW_PHASE_STARTED)
 				|| ((R4EReviewState) fReview.getState()).getState().equals(R4EReviewPhase.R4E_REVIEW_PHASE_PREPARATION)) {
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * Method isParticipantTimeSpentEnabled.
 	 * 
 	 * @return boolean
 	 */
 	public boolean isParticipantTimeSpentEnabled() {
 		if (((R4EReviewState) fReview.getState()).getState().equals(R4EReviewPhase.R4E_REVIEW_PHASE_COMPLETED)) {
 			return false;
 		}
 		return true;
 	}
 
 	/**
 	 * Method isPreparationDateEnabled.
 	 * 
 	 * @return boolean
 	 */
 	public boolean isPreparationDateEnabled() {
 		if (((R4EReviewState) fReview.getState()).getState().equals(R4EReviewPhase.R4E_REVIEW_PHASE_PREPARATION)) {
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * Method isDecisionDateEnabled.
 	 * 
 	 * @return boolean
 	 */
 	public boolean isDecisionDateEnabled() {
 		if (((R4EReviewState) fReview.getState()).getState().equals(R4EReviewPhase.R4E_REVIEW_PHASE_DECISION)) {
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * Method isReworkDateEnabled.
 	 * 
 	 * @return boolean
 	 */
 	public boolean isReworkDateEnabled() {
 		if (((R4EReviewState) fReview.getState()).getState().equals(R4EReviewPhase.R4E_REVIEW_PHASE_REWORK)) {
 			return true;
 		}
 		return false;
 	}
 }
