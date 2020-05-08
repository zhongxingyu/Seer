 // $codepro.audit.disable com.instantiations.assist.eclipse.analysis.audit.rule.effectivejava.alwaysOverridetoString.alwaysOverrideToString, com.instantiations.assist.eclipse.analysis.deserializeabilitySecurity, com.instantiations.assist.eclipse.analysis.disallowReturnMutable, com.instantiations.assist.eclipse.analysis.enforceCloneableUsageSecurity, com.instantiations.assist.eclipse.analysis.mutabilityOfArrays
 /*******************************************************************************
  * Copyright (c) 2010, 2012 Ericsson AB and others.
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
 import java.util.Calendar;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.List;
 
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.jface.util.LocalSelectionTransfer;
 import org.eclipse.jface.viewers.AbstractTreeViewer;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.window.Window;
 import org.eclipse.mylyn.reviews.frame.core.model.Comment;
 import org.eclipse.mylyn.reviews.frame.core.model.ReviewComponent;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EAnomaly;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EComment;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4ECommentType;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EParticipant;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EReviewComponent;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EReviewPhase;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EReviewState;
 import org.eclipse.mylyn.reviews.r4e.core.model.RModelFactory;
 import org.eclipse.mylyn.reviews.r4e.core.model.serial.impl.CompatibilityException;
 import org.eclipse.mylyn.reviews.r4e.core.model.serial.impl.OutOfSyncException;
 import org.eclipse.mylyn.reviews.r4e.core.model.serial.impl.ResourceHandlingException;
 import org.eclipse.mylyn.reviews.r4e.ui.R4EUIPlugin;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.dialogs.ICommentInputDialog;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.dialogs.R4EUIDialogFactory;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.preferences.PreferenceConstants;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.properties.general.AnomalyBasicProperties;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.utils.R4EUIConstants;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.utils.UIUtils;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.ui.views.properties.IPropertySource;
 
 /**
  * @author Sebastien Dubois
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
 	 * Field NEW_CHILD_ELEMENT_COMMAND_NAME. (value is ""New Comment..."")
 	 */
 	private static final String NEW_CHILD_ELEMENT_COMMAND_NAME = "New Comment...";
 
 	/**
 	 * Field NEW_CHILD_ELEMENT_COMMAND_TOOLTIP. (value is ""Add a New comment to the current anomaly"")
 	 */
 	private static final String NEW_CHILD_ELEMENT_COMMAND_TOOLTIP = "Add a New Comment to the Current Anomaly";
 
 	/**
 	 * Field COPY_ELEMENT_COMMAND_NAME. (value is ""Copy Anomalies"")
 	 */
 	private static final String COPY_ELEMENT_COMMAND_NAME = "Copy Anomalies";
 
 	/**
 	 * Field COPY_ELEMENT_COMMAND_TOOLTIP. (value is ""Copy Anomalies to Clipboard"")
 	 */
 	private static final String COPY_ELEMENT_COMMAND_TOOLTIP = "Copy Anomalies to Clipboard";
 
 	/**
 	 * Field PASTE_ELEMENT_COMMAND_NAME. (value is ""Paste Comments"")
 	 */
 	private static final String PASTE_ELEMENT_COMMAND_NAME = "Paste Comments";
 
 	/**
 	 * Field PASTE_ELEMENT_COMMAND_TOOLTIP. (value is ""Clone Comments in Clipboard to this Anomaly" +
 	 * " from its Parent Container"")
 	 */
 	private static final String PASTE_ELEMENT_COMMAND_TOOLTIP = "Clone Comments in Clipboard to this Anomaly";
 
 	/**
 	 * Field REMOVE_ELEMENT_ACTION_NAME. (value is ""Delete Anomaly"")
 	 */
 	private static final String REMOVE_ELEMENT_COMMAND_NAME = "Disable Anomaly";
 
 	/**
 	 * Field REMOVE_ELEMENT_ACTION_TOOLTIP. (value is ""Remove this anomaly from its parent file or review item"")
 	 */
 	private static final String REMOVE_ELEMENT_COMMAND_TOOLTIP = "Remove this Anomaly "
 			+ "from its parent file or review";
 
 	/**
 	 * Field RESTORE_ELEMENT_COMMAND_NAME. (value is ""Restore Anomaly"")
 	 */
 	private static final String RESTORE_ELEMENT_COMMAND_NAME = "Restore Anomaly";
 
 	/**
 	 * Field RESTORE_ELEMENT_ACTION_TOOLTIP. (value is ""Restore this disabled Anomaly"")
 	 */
 	private static final String RESTORE_ELEMENT_COMMAND_TOOLTIP = "Restore this disabled Anomaly";
 
 	/**
 	 * Field ANOMALY_LABEL_TITLE_LENGTH. (value is 20)
 	 */
 	private static final int ANOMALY_LABEL_TITLE_LENGTH = 20;
 
 	/**
 	 * Field CREATE_COMMENT_MESSAGE. (value is ""Creating New Comment..."")
 	 */
 	private static final String CREATE_COMMENT_MESSAGE = "Creating New Comment...";
 
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
 		fReadOnly = aParent.isReadOnly();
 		fAnomaly = aAnomaly;
 		fComments = new ArrayList<R4EUIComment>();
 		fPosition = aPosition;
 	}
 
 	// ------------------------------------------------------------------------
 	// Methods
 	// ------------------------------------------------------------------------
 
 	/**
 	 * Method isSameAs. Used to avoid duplicated in collections
 	 * 
 	 * @param aSource
 	 *            - R4EUIAnomalyBasic
 	 * @return boolean
 	 * @see lang.java.Object#equals(Object)
 	 */
 	public boolean isSameAs(R4EUIAnomalyBasic aSource) {
 		if (aSource instanceof R4EUIAnomalyBasic) {
 			if (this.getAnomaly().getTitle().equals(aSource.getAnomaly().getTitle())
 					&& this.getAnomaly().getDescription().equals(aSource.getAnomaly().getDescription())) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * Method getImageLocation.
 	 * 
 	 * @return String
 	 * @see org.eclipse.mylyn.reviews.r4e.ui.internal.model.IR4EUIModelElement#getImageLocation()
 	 */
 	public String getImageLocation() {
 		return ANOMALY_ICON_FILE;
 	}
 
 	/**
 	 * Method getToolTip.
 	 * 
 	 * @return String
 	 * @see org.eclipse.mylyn.reviews.r4e.ui.internal.model.IR4EUIModelElement#getToolTip()
 	 */
 	@Override
 	public String getToolTip() {
 		if (isDueDatePassed()) {
 			return R4EUIConstants.DUE_DATE_PASSED_MSG + buildAnomalyToolTip(fAnomaly);
 		}
 		return buildAnomalyToolTip(fAnomaly);
 	}
 
 	/**
 	 * Method getToolTipColor.
 	 * 
 	 * @return Color
 	 * @see org.eclipse.mylyn.reviews.r4e.ui.internal.model.IR4EUIModelElement#getToolTipColor()
 	 */
 	@Override
 	public Color getToolTipColor() {
 		if (isDueDatePassed()) {
 			return Display.getCurrent().getSystemColor(SWT.COLOR_DARK_RED);
 		}
 		return Display.getCurrent().getSystemColor(SWT.COLOR_BLACK);
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
 		final List<ReviewComponent> tempComments = new ArrayList<ReviewComponent>();
 		R4EUIModelController.setJobInProgress(true);
 
 		final ICommentInputDialog dialog = R4EUIDialogFactory.getInstance().getCommentInputDialog();
 		final int result = dialog.open();
 		if (result == Window.OK) {
 			final R4EComment tempComment = RModelFactory.eINSTANCE.createR4EComment();
 			tempComment.setDescription(dialog.getCommentValue());
 			tempComments.add(tempComment);
 		}
 		R4EUIModelController.setJobInProgress(false);
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
 		if (null != ((R4EAnomaly) aModelComponent).getType()) {
 			final R4ECommentType commentType = RModelFactory.eINSTANCE.createR4ECommentType();
 			commentType.setType(((R4ECommentType) ((R4EAnomaly) aModelComponent).getType()).getType());
 			fAnomaly.setType(commentType);
 		}
 		fAnomaly.setRank(((R4EAnomaly) aModelComponent).getRank());
 		fAnomaly.setRuleID(((R4EAnomaly) aModelComponent).getRuleID());
 		R4EUIModelController.FResourceUpdater.checkIn(bookNum);
 	}
 
 	/**
 	 * Set extra serialization model data by copying it from the passed-in object
 	 * 
 	 * @param aModelComponent
 	 *            - a serialization model element to copy information from
 	 * @throws ResourceHandlingException
 	 * @throws OutOfSyncException
 	 */
 	public void setExtraModelData(ReviewComponent aModelComponent) throws ResourceHandlingException, OutOfSyncException {
 		final Long bookNum = R4EUIModelController.FResourceUpdater.checkOut(fAnomaly,
 				R4EUIModelController.getReviewer());
 		fAnomaly.setDueDate(((R4EAnomaly) aModelComponent).getDueDate());
 		fAnomaly.getAssignedTo().addAll(((R4EAnomaly) aModelComponent).getAssignedTo());
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
 		return (null == aPosition) ? adjustTitleLength(aAnomaly) : aPosition.toString() + "->"
 				+ adjustTitleLength(aAnomaly);
 	}
 
 	/**
 	 * Method adjustTitleLength.
 	 * 
 	 * @param aAnomaly
 	 *            R4EAnomaly
 	 * @return String
 	 */
 	protected static String adjustTitleLength(R4EAnomaly aAnomaly) {
 		String anomalyTitle = aAnomaly.getTitle();
 
 		if (anomalyTitle == null) {
 			return ""; //return an empty string for the null title
 		}
 		if (anomalyTitle.length() > ANOMALY_LABEL_TITLE_LENGTH) {
 			return anomalyTitle.substring(0, ANOMALY_LABEL_TITLE_LENGTH) + R4EUIConstants.ELLIPSIS_STR;
 		} else {
 			return anomalyTitle;
 		}
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
 	 * @see org.eclipse.mylyn.reviews.r4e.ui.internal.model.IR4EUIModelElement#setEnabled(boolean)
 	 */
 	@Override
 	public void setEnabled(boolean aEnabled) throws ResourceHandlingException, OutOfSyncException {
 		final Long bookNum = R4EUIModelController.FResourceUpdater.checkOut(fAnomaly,
 				R4EUIModelController.getReviewer());
 		fAnomaly.setEnabled(true);
 		R4EUIModelController.FResourceUpdater.checkIn(bookNum);
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
 	}
 
 	/**
 	 * Method open. Load the serialization model data into UI model
 	 * 
 	 * @see org.eclipse.mylyn.reviews.r4e.ui.internal.model.IR4EUIModelElement#open()
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
 	}
 
 	/**
 	 * Method addChildren.
 	 * 
 	 * @param aModelComponent
 	 *            - the serialization model component object
 	 * @return IR4EUIModelElement
 	 * @throws ResourceHandlingException
 	 * @throws OutOfSyncException
 	 * @throws CompatibilityException
 	 * @see org.eclipse.mylyn.reviews.r4e.ui.internal.model.IR4EUIModelElement#createChildren(R4EReviewComponent)
 	 */
 	@Override
 	public IR4EUIModelElement createChildren(ReviewComponent aModelComponent) throws ResourceHandlingException,
 			OutOfSyncException, CompatibilityException {
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
 	 * @param aRejectionComment
 	 *            - boolean
 	 * @return boolean
 	 */
 	public boolean createComment(boolean aRejectionComment) {
 
 		//Get comment details from user
 		final ICommentInputDialog dialog = R4EUIDialogFactory.getInstance().getCommentInputDialog();
 		final IR4EUIModelElement commentParent = this;
 		final int[] result = new int[1]; //We need this to be able to pass the result value outside.  This is safe as we are using SyncExec
 		Display.getDefault().syncExec(new Runnable() {
 			public void run() {
 				result[0] = dialog.open();
 			}
 		});
 
 		if (result[0] == Window.OK) {
 			final Job job = new Job(CREATE_COMMENT_MESSAGE) {
 				public String familyName = R4EUIConstants.R4E_UI_JOB_FAMILY;
 
 				@Override
 				public boolean belongsTo(Object family) {
 					return familyName.equals(family);
 				}
 
 				@Override
 				public IStatus run(IProgressMonitor monitor) {
 
 					try {
 						//Create comment model element
 						final R4EUIReviewBasic uiReview = R4EUIModelController.getActiveReview();
 						final R4EParticipant participant = uiReview.getParticipant(R4EUIModelController.getReviewer(),
 								true);
 
 						final R4EComment comment = R4EUIModelController.FModelExt.createR4EComment(participant,
 								fAnomaly);
 						final Long bookNum = R4EUIModelController.FResourceUpdater.checkOut(comment,
 								R4EUIModelController.getReviewer());
 						comment.setDescription(dialog.getCommentValue());
 						R4EUIModelController.FResourceUpdater.checkIn(bookNum);
 
 						//Create and set UI model element
 						final R4EUIComment uiComment = new R4EUIComment(commentParent, comment);
 						addChildren(uiComment);
 						R4EUIModelController.setJobInProgress(false);
 						UIUtils.setNavigatorViewFocus(uiComment, AbstractTreeViewer.ALL_LEVELS);
 					} catch (ResourceHandlingException e) {
 						UIUtils.displayResourceErrorDialog(e);
 					} catch (OutOfSyncException e) {
 						UIUtils.displaySyncErrorDialog(e);
 					}
 					monitor.done();
 					return Status.OK_STATUS;
 				}
 			};
 			job.setUser(true);
 			job.schedule();
 		} else {
 			if (aRejectionComment) {
 				return false;
 			}
 		}
 		return true;
 	}
 
 	/**
 	 * Method removeChildren.
 	 * 
 	 * @param aChildToRemove
 	 *            IR4EUIModelElement
 	 * @param aFileRemove
 	 *            - also remove from file (hard remove)
 	 * @throws ResourceHandlingException
 	 * @throws OutOfSyncException
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
 		}
 	}
 
 	/**
 	 * Method removeAllChildren.
 	 * 
 	 * @param aFileRemove
 	 *            boolean
 	 * @throws ResourceHandlingException
 	 * @throws OutOfSyncException
 	 * @see org.eclipse.mylyn.reviews.r4e.ui.internal.model.IR4EUIModelElement#removeAllChildren(boolean)
 	 */
 	@Override
 	public void removeAllChildren(boolean aFileRemove) throws ResourceHandlingException, OutOfSyncException {
 		//Recursively remove all children
 		for (R4EUIComment comment : fComments) {
 			removeChildren(comment, aFileRemove);
 		}
 	}
 
 	/**
 	 * Method restore.
 	 * 
 	 * @throws CompatibilityException
 	 * @throws OutOfSyncException
 	 * @throws ResourceHandlingException
 	 */
 	@Override
 	public void restore() throws ResourceHandlingException, OutOfSyncException, CompatibilityException {
 		super.restore();
 
 		//Update inline markings (local anomalies only)
 		if (getParent().getParent() instanceof R4EUIFileContext) {
 			final R4EUIAnomalyBasic currentAnomaly = this;
 			Display.getDefault().syncExec(new Runnable() {
 				public void run() {
 					UIUtils.updateAnnotation(currentAnomaly, (R4EUIFileContext) getParent().getParent());
 				}
 			});
 		}
 
 		//Also restore any participant assigned to this element
 		for (String participant : fAnomaly.getAssignedTo()) {
			if (!(null == participant || participant.equals(""))) { //Filter out invalid participants
				R4EUIModelController.getActiveReview().getParticipant(participant, true);
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
 		if (isEnabled() && null != R4EUIModelController.getActiveReview()
 				&& null != ((R4EUIFileContext) getParent().getParent()).getTargetFileVersion()) {
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * Method isCopyElementCmd.
 	 * 
 	 * @return boolean
 	 * @see org.eclipse.mylyn.reviews.r4e.ui.internal.model.IR4EUIModelElement#isCopyElementCmd()
 	 */
 	@Override
 	public boolean isCopyElementCmd() {
 		if (isEnabled()
 				&& !isReadOnly()
 				&& null != R4EUIModelController.getActiveReview()
 				&& !(((R4EReviewState) R4EUIModelController.getActiveReview().getReview().getState()).getState().equals(R4EReviewPhase.R4E_REVIEW_PHASE_COMPLETED))
 				&& getParent().getParent() instanceof R4EUIFileContext) {
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * Method getCopyElementCmdName.
 	 * 
 	 * @return String
 	 * @see org.eclipse.mylyn.reviews.r4e.ui.internal.model.IR4EUIModelElement#getCopyElementCmdName()
 	 */
 	@Override
 	public String getCopyElementCmdName() {
 		return COPY_ELEMENT_COMMAND_NAME;
 	}
 
 	/**
 	 * Method getCopyElementCmdTooltip.
 	 * 
 	 * @return String
 	 * @see org.eclipse.mylyn.reviews.r4e.ui.internal.model.IR4EUIModelElement#getCopyElementCmdTooltip()
 	 */
 	@Override
 	public String getCopyElementCmdTooltip() {
 		return COPY_ELEMENT_COMMAND_TOOLTIP;
 	}
 
 	/**
 	 * Method isPasteElementCmd.
 	 * 
 	 * @return boolean
 	 * @see org.eclipse.mylyn.reviews.r4e.ui.internal.model.IR4EUIModelElement#isPasteElementCmd()
 	 */
 	@Override
 	public boolean isPasteElementCmd() {
 		if (isEnabled()
 				&& !isReadOnly()
 				&& null != R4EUIModelController.getActiveReview()
 				&& !(((R4EReviewState) R4EUIModelController.getActiveReview().getReview().getState()).getState().equals(R4EReviewPhase.R4E_REVIEW_PHASE_COMPLETED))) {
 			//We can only paste if there is a least 1 Comment in the clipboard
 			Object element = null;
 			ISelection selection = LocalSelectionTransfer.getTransfer().getSelection();
 			if (selection instanceof IStructuredSelection) {
 				for (final Iterator<?> iterator = ((IStructuredSelection) selection).iterator(); iterator.hasNext();) {
 					element = iterator.next();
 					if (element instanceof R4EUIComment) {
 						return true;
 					}
 				}
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * Method getPasteElementCmdName.
 	 * 
 	 * @return String
 	 * @see org.eclipse.mylyn.reviews.r4e.ui.internal.model.IR4EUIModelElement#getPasteElementCmdName()
 	 */
 	@Override
 	public String getPasteElementCmdName() {
 		return PASTE_ELEMENT_COMMAND_NAME;
 	}
 
 	/**
 	 * Method getPasteElementCmdTooltip.
 	 * 
 	 * @return String
 	 * @see org.eclipse.mylyn.reviews.r4e.ui.internal.model.IR4EUIModelElement#getPasteElementCmdTooltip()
 	 */
 	@Override
 	public String getPasteElementCmdTooltip() {
 		return PASTE_ELEMENT_COMMAND_TOOLTIP;
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
 				&& !isReadOnly()
 				&& null != R4EUIModelController.getActiveReview()
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
 				&& !isReadOnly()
 				&& null != R4EUIModelController.getActiveReview()
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
 		R4EReviewPhase phase = ((R4EReviewState) R4EUIModelController.getActiveReview().getReview().getState()).getState();
 		if (isEnabled() || isReadOnly() || phase.equals(R4EReviewPhase.R4E_REVIEW_PHASE_COMPLETED)
 				|| phase.equals(R4EReviewPhase.R4E_REVIEW_PHASE_REWORK)) {
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
 		if (isEnabled() && null != R4EUIModelController.getActiveReview()) {
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * Method isTitleEnabled.
 	 * 
 	 * @return boolean
 	 */
 	public boolean isTitleEnabled() {
 		if (null != R4EUIModelController.getActiveReview()) {
 			if (null == fAnomaly.getRuleID() || fAnomaly.getRuleID().equals("")) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * Method isClassEnabled.
 	 * 
 	 * @return boolean
 	 */
 	public boolean isClassEnabled() {
 		if (null != R4EUIModelController.getActiveReview()) {
 			if (null == fAnomaly.getRuleID() || fAnomaly.getRuleID().equals("")) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * Method isRankEnabled.
 	 * 
 	 * @return boolean
 	 */
 	public boolean isRankEnabled() {
 		if (null != R4EUIModelController.getActiveReview()) {
 			if (null == fAnomaly.getRuleID() || fAnomaly.getRuleID().equals("")) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * Method isDueDateEnabled.
 	 * 
 	 * @return boolean
 	 */
 	public boolean isDueDateEnabled() {
 		return true;
 	}
 
 	/**
 	 * Method isTerminalState.
 	 * 
 	 * @return boolean
 	 */
 	public boolean isTerminalState() {
 		return false;
 	}
 
 	/**
 	 * Method isDueDatePassed.
 	 * 
 	 * @return boolean
 	 * @see org.eclipse.mylyn.reviews.r4e.ui.internal.model.IR4EUIModelElement#isDueDatePassed()
 	 */
 	@Override
 	public boolean isDueDatePassed() {
 		if (isEnabled()) {
 			if (null != fAnomaly.getDueDate()) {
 				IR4EUIModelElement element = getParent().getParent().getParent().getParent();
 				Calendar cal = Calendar.getInstance();
 				cal.setTime(new Date());
 				cal.add(Calendar.DAY_OF_YEAR, -1);
 				if (fAnomaly.getDueDate().before(cal.getTime())) {
 
 					if (!(element instanceof R4EUIReviewBasic)) {
 						//Assume global anomaly
 						element = getParent().getParent();
 					}
 					if (!((R4EReviewState) ((R4EUIReviewBasic) element).getReview().getState()).getState().equals(
 							R4EReviewPhase.R4E_REVIEW_PHASE_COMPLETED)) {
 						return true;
 					}
 				}
 			}
 		}
 		return false;
 	}
 }
