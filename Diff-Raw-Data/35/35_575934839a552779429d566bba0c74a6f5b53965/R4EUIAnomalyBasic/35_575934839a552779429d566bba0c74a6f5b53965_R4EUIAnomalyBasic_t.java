 // $codepro.audit.disable com.instantiations.assist.eclipse.analysis.audit.rule.effectivejava.alwaysOverridetoString.alwaysOverrideToString, com.instantiations.assist.eclipse.analysis.deserializeabilitySecurity, com.instantiations.assist.eclipse.analysis.disallowReturnMutable, com.instantiations.assist.eclipse.analysis.enforceCloneableUsageSecurity, com.instantiations.assist.eclipse.analysis.mutabilityOfArrays
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
  * This class implements the Anomaly element of the UI model
  * 
  * Contributors:
  *   Sebastien Dubois - Created for Mylyn Review R4E project
  *   
  *******************************************************************************/
 
 package org.eclipse.mylyn.reviews.r4e.ui.internal.model;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import org.eclipse.jface.window.Window;
 import org.eclipse.mylyn.reviews.frame.core.model.Comment;
 import org.eclipse.mylyn.reviews.frame.core.model.ReviewComponent;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EAnomaly;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EComment;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EParticipant;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EReviewComponent;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EReviewPhase;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EReviewState;
 import org.eclipse.mylyn.reviews.r4e.core.model.RModelFactory;
 import org.eclipse.mylyn.reviews.r4e.core.model.serial.impl.OutOfSyncException;
 import org.eclipse.mylyn.reviews.r4e.core.model.serial.impl.ResourceHandlingException;
 import org.eclipse.mylyn.reviews.r4e.ui.R4EUIPlugin;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.dialogs.ICommentInputDialog;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.dialogs.R4EUIDialogFactory;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.navigator.ReviewNavigatorContentProvider;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.preferences.PreferenceConstants;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.properties.general.AnomalyBasicProperties;
 import org.eclipse.ui.views.properties.IPropertySource;
 
 /**
  * @author lmcdubo
  * @version $Revision: 1.0 $
  */
 public class R4EUIAnomalyBasic extends R4EUIModelElement {
 
 	// ------------------------------------------------------------------------
 	// Constants
 	// ------------------------------------------------------------------------
 
 	/**
 	 * Field fAnomalyFile. (value is ""icons/obj16/anmly_obj.gif"")
 	 */
 	public static final String ANOMALY_ICON_FILE = "icons/obj16/anmly_obj.gif";
 
 	/**
 	 * Field NEW_CHILD_ELEMENT_COMMAND_NAME. (value is ""Add a New Comment"")
 	 */
 	private static final String NEW_CHILD_ELEMENT_COMMAND_NAME = "New Comment";
 
 	/**
 	 * Field NEW_CHILD_ELEMENT_COMMAND_TOOLTIP. (value is ""Add a New comment to the current anomaly"")
 	 */
 	private static final String NEW_CHILD_ELEMENT_COMMAND_TOOLTIP = "Add a New Comment to the Current Anomaly";
 
 	/**
 	 * Field REMOVE_ELEMENT_ACTION_NAME. (value is ""Delete Anomaly"")
 	 */
 	private static final String REMOVE_ELEMENT_COMMAND_NAME = "Disable Anomaly";
 
 	/**
 	 * Field REMOVE_ELEMENT_ACTION_TOOLTIP. (value is ""Remove this anomaly from its parent file or review item"")
 	 */
 	private static final String REMOVE_ELEMENT_COMMAND_TOOLTIP = "Disable (and Optionally Remove) this Anomaly "
 			+ "from its parent file or review";
 
 	/**
 	 * Field RESTORE_ELEMENT_COMMAND_NAME. (value is ""Restore Anomaly"")
 	 */
 	private static final String RESTORE_ELEMENT_COMMAND_NAME = "Restore Anomaly";
 
 	/**
 	 * Field RESTORE_ELEMENT_ACTION_TOOLTIP. (value is ""Restore this disabled Anomaly"")
 	 */
 	private static final String RESTORE_ELEMENT_COMMAND_TOOLTIP = "Restore this disabled Anomaly";
 
 	// ------------------------------------------------------------------------
 	// Member variables
 	// ------------------------------------------------------------------------
 
 	/**
 	 * Field fAnomaly.
 	 */
 	protected final R4EAnomaly fAnomaly;
 
 	/**
 	 * Field fComments.
 	 */
 	private final List<R4EUIComment> fComments;
 
 	/**
 	 * Field fPosition.
 	 */
 	private final IR4EUIPosition fPosition;
 
 	// ------------------------------------------------------------------------
 	// Constructors
 	// ------------------------------------------------------------------------
 
 	/**
 	 * Constructor for R4EUIAnomaly.
 	 * 
 	 * @param aParent
 	 *            IR4EUIModelElement
 	 * @param aAnomaly
 	 *            R4EAnomaly
 	 * @param aPosition
 	 *            IR4EUIPosition
 	 */
 	public R4EUIAnomalyBasic(IR4EUIModelElement aParent, R4EAnomaly aAnomaly, IR4EUIPosition aPosition) {
 		super(aParent, buildAnomalyName(aAnomaly, aPosition));
 		fAnomaly = aAnomaly;
 		fComments = new ArrayList<R4EUIComment>();
 		setImage(ANOMALY_ICON_FILE);
 		fPosition = aPosition;
 	}
 
 	// ------------------------------------------------------------------------
 	// Methods
 	// ------------------------------------------------------------------------
 
 	/**
 	 * Method getToolTip.
 	 * 
 	 * @return String
 	 * @see org.eclipse.mylyn.reviews.r4e.ui.internal.model.IR4EUIModelElement#getToolTip()
 	 */
 	@Override
 	public String getToolTip() {
 		return buildAnomalyToolTip(fAnomaly);
 	}
 
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
 			return new AnomalyBasicProperties(this);
 		}
 		return null;
 	}
 
 	//Attributes
 
 	/**
 	 * Method getAnomaly.
 	 * 
 	 * @return R4EAnomaly
 	 */
 	public R4EAnomaly getAnomaly() {
 		return fAnomaly;
 	}
 
 	/**
 	 * Method getPosition.
 	 * 
 	 * @return IR4EPosition
 	 */
 	public IR4EUIPosition getPosition() {
 		return fPosition;
 	}
 
 	/**
 	 * Create a serialization model element object
 	 * 
 	 * @return the new serialization element object
 	 * @see org.eclipse.mylyn.reviews.r4e.ui.internal.model.IR4EUIModelElement#createChildModelDataElement()
 	 */
 	@Override
 	public List<ReviewComponent> createChildModelDataElement() {
 		//Get Comment from user and set it in model data
 		List<ReviewComponent> tempComments = new ArrayList<ReviewComponent>();
 
 		R4EUIModelController.setJobInProgress(true);
 		final ICommentInputDialog dialog = R4EUIDialogFactory.getInstance().getCommentInputDialog();
 		final int result = dialog.open();
 		if (result == Window.OK) {
 			R4EComment tempComment = RModelFactory.eINSTANCE.createR4EComment();
 			tempComment.setDescription(dialog.getCommentValue());
 			tempComments.add(tempComment);
 		}
 		R4EUIModelController.setJobInProgress(false); //Enable view
 		return tempComments;
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
 		final Long bookNum = R4EUIModelController.FResourceUpdater.checkOut(fAnomaly,
 				R4EUIModelController.getReviewer());
 		fAnomaly.setTitle(((R4EAnomaly) aModelComponent).getTitle());
 		fAnomaly.setDescription(((R4EAnomaly) aModelComponent).getDescription());
 		fAnomaly.setType(((R4EAnomaly) aModelComponent).getType());
 		fAnomaly.setRank(((R4EAnomaly) aModelComponent).getRank());
 		fAnomaly.setDueDate(((R4EAnomaly) aModelComponent).getDueDate());
 		fAnomaly.setRuleID(((R4EAnomaly) aModelComponent).getRuleID());
 		R4EUIModelController.FResourceUpdater.checkIn(bookNum);
 	}
 
 	/**
 	 * Method buildAnomalyName.
 	 * 
 	 * @param aAnomaly
 	 *            - the anomaly to use
 	 * @param aPosition
 	 *            IR4EUIPosition
 	 * @return String - the new name
 	 */
 	public static String buildAnomalyName(R4EAnomaly aAnomaly, IR4EUIPosition aPosition) {
 		return (null == aPosition) ? aAnomaly.getTitle() : aPosition.toString() + "->" + aAnomaly.getTitle();
 	}
 
 	/**
 	 * Method buildAnomalyToolTip.
 	 * 
 	 * @param aAnomaly
 	 *            - the anomaly to use
 	 * @return String - the new tooltip
 	 */
 	public static String buildAnomalyToolTip(R4EAnomaly aAnomaly) {
 		return aAnomaly.getUser().getId() + ": " + aAnomaly.getDescription();
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
 		final Long bookNum = R4EUIModelController.FResourceUpdater.checkOut(fAnomaly,
 				R4EUIModelController.getReviewer());
 		fAnomaly.setEnabled(true);
 		R4EUIModelController.FResourceUpdater.checkIn(bookNum);
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
 		return fAnomaly.isEnabled();
 	}
 
 	//Hierarchy
 
 	/**
 	 * Method getChildren.
 	 * 
 	 * @return IR4EUIModelElement[]
 	 * @see org.eclipse.mylyn.reviews.r4e.ui.internal.model.IR4EUIModelElement#getChildren()
 	 */
 	@Override
 	public IR4EUIModelElement[] getChildren() {
 		return fComments.toArray(new R4EUIComment[fComments.size()]);
 	}
 
 	/**
 	 * Method hasChildren.
 	 * 
 	 * @return boolean
 	 * @see org.eclipse.mylyn.reviews.r4e.ui.internal.model.IR4EUIModelElement#hasChildren()
 	 */
 	@Override
 	public boolean hasChildren() {
 		if (fComments.size() > 0) {
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * Close the model element (i.e. disable it)
 	 * 
 	 * @see org.eclipse.mylyn.reviews.r4e.ui.internal.model.IR4EUIModelElement#close()
 	 */
 	@Override
 	public void close() {
 		//Remove all children references
 		R4EUIComment comment = null;
 		final int commentsSize = fComments.size();
 		for (int i = 0; i < commentsSize; i++) {
 
 			comment = fComments.get(i);
 			comment.close();
 		}
 		fComments.clear();
 		fOpen = false;
 		removeListeners();
 	}
 
 	/**
 	 * Method open. Load the serialization model data into UI model
 	 */
 	@Override
 	public void open() {
 		final List<Comment> comments = fAnomaly.getComments();
 		if (null != comments) {
 			R4EComment r4eComment = null;
 			final int commentsSize = comments.size();
 			for (int i = 0; i < commentsSize; i++) {
 				r4eComment = (R4EComment) comments.get(i);
 				if (r4eComment.isEnabled()
 						|| R4EUIPlugin.getDefault()
 								.getPreferenceStore()
 								.getBoolean(PreferenceConstants.P_SHOW_DISABLED)) {
 					addChildren(new R4EUIComment(this, r4eComment));
 				}
 			}
 		}
 		fOpen = true;
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
 		fComments.add((R4EUIComment) aChildToAdd);
 		aChildToAdd.addListener((ReviewNavigatorContentProvider) R4EUIModelController.getNavigatorView()
 				.getTreeViewer()
 				.getContentProvider());
 		fireAdd(aChildToAdd);
 	}
 
 	/**
 	 * Method addChildren.
 	 * 
 	 * @param aModelComponent
 	 *            - the serialization model component object
 	 * @return IR4EUIModelElement
 	 * @throws ResourceHandlingException
 	 * @throws OutOfSyncException
 	 * @see org.eclipse.mylyn.reviews.r4e.ui.internal.model.IR4EUIModelElement#createChildren(R4EReviewComponent)
 	 */
 	@Override
 	public IR4EUIModelElement createChildren(ReviewComponent aModelComponent) throws ResourceHandlingException,
 			OutOfSyncException {
 		final String user = R4EUIModelController.getReviewer();
 		R4EParticipant participant = null;
 		if (getParent().getParent().getParent().getParent() instanceof R4EUIReviewBasic) { // $codepro.audit.disable methodChainLength
 			participant = ((R4EUIReviewBasic) getParent().getParent().getParent().getParent()).getParticipant(user,
 					true); // $codepro.audit.disable methodChainLength
 		} else {
 			//Global anomaly
 			participant = ((R4EUIReviewBasic) getParent().getParent()).getParticipant(user, true);
 		}
 		final R4EComment comment = R4EUIModelController.FModelExt.createR4EComment(participant, fAnomaly);
 		final Long bookNum = R4EUIModelController.FResourceUpdater.checkOut(comment, R4EUIModelController.getReviewer());
 		comment.setDescription(((Comment) aModelComponent).getDescription());
 		R4EUIModelController.FResourceUpdater.checkIn(bookNum);
 		final R4EUIComment addedChild = new R4EUIComment(this, comment);
 		addChildren(addedChild);
 		return addedChild;
 	}
 
 	/**
 	 * Method createComment.
 	 * 
 	 * @return R4EUIComment
 	 * @throws ResourceHandlingException
 	 * @throws OutOfSyncException
 	 */
 	public R4EUIComment createComment() throws ResourceHandlingException, OutOfSyncException {
 
 		R4EUIComment uiComment = null;
 
 		//Get comment details from user
 		R4EUIModelController.setJobInProgress(true);
 		final ICommentInputDialog dialog = R4EUIDialogFactory.getInstance().getCommentInputDialog();
 		final int result = dialog.open();
 
 		if (result == Window.OK) {
 
 			//Create comment model element
 			final R4EUIReviewBasic uiReview = R4EUIModelController.getActiveReview();
 			final R4EParticipant participant = uiReview.getParticipant(R4EUIModelController.getReviewer(), true);
 
 			final R4EComment comment = R4EUIModelController.FModelExt.createR4EComment(participant, fAnomaly);
 			final Long bookNum = R4EUIModelController.FResourceUpdater.checkOut(comment,
 					R4EUIModelController.getReviewer());
 			comment.setDescription(dialog.getCommentValue());
 			R4EUIModelController.FResourceUpdater.checkIn(bookNum);
 
 			//Create and set UI model element
 			uiComment = new R4EUIComment(this, comment);
 			addChildren(uiComment);
 		}
 		R4EUIModelController.setJobInProgress(false); //Enable commands in case of error
 		return uiComment;
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
 
 		final R4EUIComment removedElement = fComments.get(fComments.indexOf(aChildToRemove));
 		/* TODO uncomment when core model supports hard-removing of elements
 		if (aFileRemove) removedElement.getComment().remove());
 		else */
 		final R4EComment modelComment = removedElement.getComment();
 		final Long bookNum = R4EUIModelController.FResourceUpdater.checkOut(modelComment,
 				R4EUIModelController.getReviewer());
 		modelComment.setEnabled(false);
 		R4EUIModelController.FResourceUpdater.checkIn(bookNum);
 
 		//Remove element from UI if the show disabled element option is off
 		if (!(R4EUIPlugin.getDefault().getPreferenceStore().getBoolean(PreferenceConstants.P_SHOW_DISABLED))) {
 			fComments.remove(removedElement);
 			aChildToRemove.removeListeners();
 			fireRemove(aChildToRemove);
 		} else {
 			R4EUIModelController.getNavigatorView().getTreeViewer().refresh();
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
 		for (R4EUIComment comment : fComments) {
 			removeChildren(comment, aFileRemove);
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
 		if (null != fComments) {
 			R4EUIComment element = null;
 			for (final Iterator<R4EUIComment> iterator = fComments.iterator(); iterator.hasNext();) {
 				element = iterator.next();
 				element.addListener(aProvider);
 			}
 		}
 	}
 
 	/**
 	 * Method removeListener.
 	 * 
 	 * @param aProvider
 	 *            - the treeviewer content provider
 	 * @see org.eclipse.mylyn.reviews.r4e.ui.internal.model.IR4EUIModelElement#removeListener()
 	 */
 	@Override
 	public void removeListener(ReviewNavigatorContentProvider aProvider) {
 		super.removeListener(aProvider);
 		if (null != fComments) {
 			R4EUIComment element = null;
 			for (final Iterator<R4EUIComment> iterator = fComments.iterator(); iterator.hasNext();) {
 				element = iterator.next();
 				element.removeListener(aProvider);
 			}
 		}
 	}
 
 	//Commands
 
 	/**
 	 * Method isOpenEditorCmd.
 	 * 
 	 * @return boolean
 	 * @see org.eclipse.mylyn.reviews.r4e.ui.internal.model.IR4EUIModelElement#isOpenEditorCmd()
 	 */
 	@Override
 	public boolean isOpenEditorCmd() {
 		if (!(getParent().getParent() instanceof R4EUIFileContext)) {
 			return false;
 		}
 		if (isEnabled() && null != ((R4EUIFileContext) getParent().getParent()).getTargetFileVersion()) {
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * Method isAddChildElementCmd.
 	 * 
 	 * @return boolean
 	 * @see org.eclipse.mylyn.reviews.r4e.ui.internal.model.IR4EUIModelElement#isNewChildElementCmd()
 	 */
 	@Override
 	public boolean isNewChildElementCmd() {
 		if (isEnabled()
 				&& !(((R4EReviewState) R4EUIModelController.getActiveReview().getReview().getState()).getState().equals(R4EReviewPhase.R4E_REVIEW_PHASE_COMPLETED))) {
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * Method getAddChildElementCmdName.
 	 * 
 	 * @return String
 	 * @see org.eclipse.mylyn.reviews.r4e.ui.internal.model.IR4EUIModelElement#getNewChildElementCmdName()
 	 */
 	@Override
 	public String getNewChildElementCmdName() {
 		return NEW_CHILD_ELEMENT_COMMAND_NAME;
 	}
 
 	/**
 	 * Method getAddChildElementCmdTooltip.
 	 * 
 	 * @return String
 	 * @see org.eclipse.mylyn.reviews.r4e.ui.internal.model.IR4EUIModelElement#getNewChildElementCmdTooltip()
 	 */
 	@Override
 	public String getNewChildElementCmdTooltip() {
 		return NEW_CHILD_ELEMENT_COMMAND_TOOLTIP;
 	}
 
 	/**
 	 * Method isRemoveElementCmd.
 	 * 
 	 * @return boolean
 	 * @see org.eclipse.mylyn.reviews.r4e.ui.internal.model.IR4EUIModelElement#isRemoveElementCmd()
 	 */
 	@Override
 	public boolean isRemoveElementCmd() {
 		if (isEnabled()
 				&& !(((R4EReviewState) R4EUIModelController.getActiveReview().getReview().getState()).getState().equals(R4EReviewPhase.R4E_REVIEW_PHASE_COMPLETED))) {
 			return true;
 		}
 		return false;
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
 	 * Method isRestoreElementCmd.
 	 * 
 	 * @return boolean
 	 * @see org.eclipse.mylyn.reviews.r4e.ui.internal.model.IR4EUIModelElement#iisRestoreElementCmd()
 	 */
 	@Override
 	public boolean isRestoreElementCmd() {
 		if (!(getParent().getParent().isEnabled())) {
 			return false;
 		}
 		if (isEnabled()
 				|| ((R4EReviewState) R4EUIModelController.getActiveReview().getReview().getState()).getState().equals(
 						R4EReviewPhase.R4E_REVIEW_PHASE_COMPLETED)) {
 			return false;
 		}
 		return true;
 	}
 
 	/**
 	 * Method getRestoreElementCmdName.
 	 * 
 	 * @return String
 	 * @see org.eclipse.mylyn.reviews.r4e.ui.internal.model.IR4EUIModelElement#getRestoreElementCmdName()
 	 */
 	@Override
 	public String getRestoreElementCmdName() {
 		return RESTORE_ELEMENT_COMMAND_NAME;
 	}
 
 	/**
 	 * Method getRestoreElementCmdTooltip.
 	 * 
 	 * @return String
 	 * @see org.eclipse.mylyn.reviews.r4e.ui.internal.model.IR4EUIModelElement#getRestoreElementCmdTooltip()
 	 */
 	@Override
 	public String getRestoreElementCmdTooltip() {
 		return RESTORE_ELEMENT_COMMAND_TOOLTIP;
 	}
 
 	/**
 	 * Method isSendEmailCmd.
 	 * 
 	 * @return boolean
 	 * @see org.eclipse.mylyn.reviews.r4e.ui.internal.model.IR4EUIModelElement#isSendEmailCmd()
 	 */
 	@Override
 	public boolean isSendEmailCmd() {
		if (isEnabled()) {
			return true;
		}
		return false;
 	}
 }
