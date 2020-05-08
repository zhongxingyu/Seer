 /*******************************************************************************
  * Copyright (c) 2011 Ericsson Research Canada
  * 
  * All rights reserved. This program and the accompanying materials are
  * made available under the terms of the Eclipse Public License v1.0 which
  * accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Description:
  * 
  * This class implements a Factory that is used to create all R4E Input Dialogs
  * 
  * Contributors:
  *   Sebastien Dubois - Created for Mylyn Review R4E project
  *   
  ******************************************************************************/
 
 package org.eclipse.mylyn.reviews.r4e.ui.internal.dialogs;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.jface.dialogs.InputDialog;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.mylyn.reviews.notifications.core.NotificationsCore;
 import org.eclipse.mylyn.reviews.notifications.spi.NotificationsConnector;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.model.IR4EUIModelElement;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.model.R4EUIAnomalyBasic;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.model.R4EUIModelController;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.model.R4EUIReviewBasic;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.model.R4EUIReviewGroup;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.utils.UIUtils;
 import org.eclipse.mylyn.versions.ui.ScmUi;
 import org.eclipse.mylyn.versions.ui.spi.ScmConnectorUi;
 import org.eclipse.swt.widgets.Display;
 
 /**
  * @author lmcdubo
  * @version $Revision: 1.0 $
  */
 @SuppressWarnings("restriction")
 public class R4EUIDialogFactory {
 
 	// ------------------------------------------------------------------------
 	// Constants
 	// ------------------------------------------------------------------------
 
 	/**
 	 * Field MAIL_CONNECTOR_IDS.
 	 */
 	private static final String[] MAIL_CONNECTOR_IDS = { "reviews.r4e.mail.outlook.connector" }; //$NON-NLS-1$
 
 	// ------------------------------------------------------------------------
 	// Members
 	// ------------------------------------------------------------------------
 
 	/**
 	 * Field FInstance.
 	 */
 	private static R4EUIDialogFactory FInstance = null;
 
 	/**
 	 * Field fAnomalyInputDialog.
 	 */
 	private IAnomalyInputDialog fAnomalyInputDialog = null;
 
 	/**
 	 * Field fCalendarDialog.
 	 */
 	private ICalendarDialog fCalendarDialog = null;
 
 	/**
 	 * Field fChangeReviewStateDialog.
 	 */
 	private IChangeStateDialog fChangeReviewStateDialog = null;
 
 	/**
 	 * Field fChangeAnomalyStateDialog.
 	 */
 	private IChangeStateDialog fChangeAnomalyStateDialog = null;
 
 	/**
 	 * Field fCommentInputDialog.
 	 */
 	private ICommentInputDialog fCommentInputDialog = null;
 
 	/**
 	 * Field fFindUserDialog.
 	 */
 	private IFindUserDialog fFindUserDialog = null;
 
 	/**
 	 * Field fParticipantInputDialog.
 	 */
 	private IParticipantInputDialog fParticipantInputDialog = null;
 
 	/**
 	 * Field fParticipantUnassignDialog.
 	 */
 	private IParticipantUnassignDialog fParticipantUnassignDialog = null;
 
 	/**
 	 * Field fReviewGroupInputDialog.
 	 */
 	private IReviewGroupInputDialog fReviewGroupInputDialog = null;
 
 	/**
 	 * Field fReviewInputDialog.
 	 */
 	private IReviewInputDialog fReviewInputDialog = null;
 
 	/**
 	 * Field fRuleAreaInputDialog.
 	 */
 	private IRuleAreaInputDialog fRuleAreaInputDialog = null;
 
 	/**
 	 * Field fRuleInputDialog.
 	 */
 	private IRuleInputDialog fRuleInputDialog = null;
 
 	/**
 	 * Field fRuleSetInputDialog.
 	 */
 	private IRuleSetInputDialog fRuleSetInputDialog = null;
 
 	/**
 	 * Field fRuleViolationInputDialog.
 	 */
 	private IRuleViolationInputDialog fRuleViolationInputDialog = null;
 
 	/**
 	 * Field fSendNotificationInputDialog.
 	 */
 	private ISendNotificationInputDialog fSendNotificationInputDialog = null;
 
 	/**
 	 * Field fParticipantFilterInputDialog.
 	 */
 	private InputDialog fParticipantFilterInputDialog = null;
 
 	/**
 	 * Field fScmConnectorUi.
 	 */
 	private ScmConnectorUi fScmConnectorUi = null;
 
 	/**
 	 * Field fNotificationsConnector.
 	 */
 	private NotificationsConnector fNotificationsConnector = null;
 
 	// ------------------------------------------------------------------------
 	// Constructors
 	// ------------------------------------------------------------------------
 
 	/**
 	 * Constructor for R4EUIDialogFactory.
 	 */
 	private R4EUIDialogFactory() {
 	}
 
 	// ------------------------------------------------------------------------
 	// Methods
 	// ------------------------------------------------------------------------
 
 	/**
 	 * Method getInstance.
 	 * 
 	 * @return R4EUIDialogFactory
 	 */
 	public static R4EUIDialogFactory getInstance() {
 		if (null == FInstance) {
 			FInstance = new R4EUIDialogFactory();
 		}
 		return FInstance;
 	}
 
 	/**
 	 * Method getAnomalyInputDialog.
 	 * 
 	 * @return IAnomalyInputDialog
 	 */
 	public IAnomalyInputDialog getAnomalyInputDialog() {
 		if (null == fAnomalyInputDialog) {
 			fAnomalyInputDialog = new AnomalyInputDialog(R4EUIModelController.getNavigatorView()
 					.getSite()
 					.getWorkbenchWindow()
 					.getShell());
 		}
 		return fAnomalyInputDialog;
 	}
 
 	/**
 	 * Method setAnomalyInputDialog.
 	 * 
 	 * @param aNewAnomalyInputDialog
 	 *            IAnomalyInputDialog
 	 */
 	public void setAnomalyInputDialog(IAnomalyInputDialog aNewAnomalyInputDialog) {
 		fAnomalyInputDialog = aNewAnomalyInputDialog;
 	}
 
 	/**
 	 * Method getCalendarDialog.
 	 * 
 	 * @return ICalendarDialog
 	 */
 	public ICalendarDialog getCalendarDialog() {
 		if (null == fCalendarDialog) {
 			fCalendarDialog = new CalendarDialog(R4EUIModelController.getNavigatorView()
 					.getSite()
 					.getWorkbenchWindow()
 					.getShell(), false);
 		}
 		return fCalendarDialog;
 	}
 
 	/**
 	 * Method setCalendarDialog.
 	 * 
 	 * @param aCalendarDialog
 	 *            ICalendarDialog
 	 */
 	public void setCalendarDialog(ICalendarDialog aCalendarDialog) {
 		fCalendarDialog = aCalendarDialog;
 	}
 
 	/**
 	 * Method getChangeStateDialog.
 	 * 
 	 * @param aTargetClass
 	 *            Class<?>
 	 * @return IChangeStateDialog
 	 */
 	public IChangeStateDialog getChangeStateDialog(Class<?> aTargetClass) {
 		if (aTargetClass.equals(R4EUIReviewBasic.class)) {
 			if (null == fChangeReviewStateDialog) {
 				fChangeReviewStateDialog = new ChangeStateDialog(R4EUIModelController.getNavigatorView()
 						.getSite()
 						.getWorkbenchWindow()
 						.getShell(), R4EUIReviewBasic.class);
 			}
 			return fChangeReviewStateDialog;
 		}
 		//Assume AnomalyChangeStateDialog
 		if (null == fChangeAnomalyStateDialog) {
 			fChangeAnomalyStateDialog = new ChangeStateDialog(R4EUIModelController.getNavigatorView()
 					.getSite()
 					.getWorkbenchWindow()
 					.getShell(), R4EUIAnomalyBasic.class);
 		}
 		return fChangeAnomalyStateDialog;
 	}
 
 	/**
 	 * Method setChangeStateDialog.
 	 * 
 	 * @param aChangeStateDialog
 	 *            IChangeStateDialog
 	 * @param aTargetClass
 	 *            Class<?>
 	 */
 	public void setChangeStateDialog(IChangeStateDialog aChangeStateDialog, Class<?> aTargetClass) {
 		if (aTargetClass.equals(R4EUIReviewBasic.class)) {
 			fChangeReviewStateDialog = aChangeStateDialog;
 		}
 		//Assume AnomalyChangeStateDialog
 		fChangeAnomalyStateDialog = aChangeStateDialog;
 	}
 
 	/**
 	 * Method getCommentInputDialog.
 	 * 
 	 * @return ICommentInputDialog
 	 */
 	public ICommentInputDialog getCommentInputDialog() {
 		if (null == fCommentInputDialog) {
 			fCommentInputDialog = new CommentInputDialog(R4EUIModelController.getNavigatorView()
 					.getSite()
 					.getWorkbenchWindow()
 					.getShell());
 		}
 		return fCommentInputDialog;
 	}
 
 	/**
 	 * Method setCommentInputDialog.
 	 * 
 	 * @param aCommentInputDialog
 	 *            ICommentInputDialog
 	 */
 	public void setCommentInputDialog(ICommentInputDialog aCommentInputDialog) {
 		fCommentInputDialog = aCommentInputDialog;
 	}
 
 	/**
 	 * Method getFindUserDialog.
 	 * 
 	 * @return IFindUserDialog
 	 */
 	public IFindUserDialog getFindUserDialog() {
 		if (null == fFindUserDialog) {
 			fFindUserDialog = new FindUserDialog(R4EUIModelController.getNavigatorView()
 					.getSite()
 					.getWorkbenchWindow()
 					.getShell());
 		}
 		return fFindUserDialog;
 	}
 
 	/**
 	 * Method setFindUserDialog.
 	 * 
 	 * @param aFindUserDialog
 	 *            IFindUserDialog
 	 */
 	public void setFindUserDialog(IFindUserDialog aFindUserDialog) {
 		fFindUserDialog = aFindUserDialog;
 	}
 
 	/**
 	 * Method getParticipantInputDialog.
 	 * 
 	 * @param aShowExtraParams
 	 *            boolean
 	 * @return IParticipantInputDialog
 	 */
 	public IParticipantInputDialog getParticipantInputDialog(boolean aShowExtraParams) {
 		if (!UIUtils.TEST_MODE) {
 			fParticipantInputDialog = new ParticipantInputDialog(Display.getDefault().getActiveShell(),
 					aShowExtraParams);
			return fParticipantInputDialog;
 		}
 		return fParticipantInputDialog; //Test mode: return mockup reference
 	}
 
 	/**
 	 * Method removeParticipantInputDialog.
 	 */
 	public void removeParticipantInputDialog() {
 		fParticipantInputDialog.close();
 		fParticipantInputDialog = null;
 	}
 
 	/**
 	 * Method setParticipantInputDialog.
 	 * 
 	 * @param aParticipantInputDialog
 	 *            IParticipantInputDialog
 	 */
 	public void setParticipantInputDialog(IParticipantInputDialog aParticipantInputDialog) {
 		fParticipantInputDialog = aParticipantInputDialog;
 	}
 
 	/**
 	 * Method getParticipantUnassignDialog.
 	 * 
 	 * @param aElement
 	 *            - IR4EUIModelElement
 	 * @return IParticipantUnassignDialog
 	 */
 	public IParticipantUnassignDialog getParticipantUnassignDialog(IR4EUIModelElement aElement) {
 		if (!UIUtils.TEST_MODE) {
 			fParticipantUnassignDialog = new ParticipantUnassignDialog(Display.getCurrent().getActiveShell(), aElement);
			return fParticipantUnassignDialog;
 		}
 		return fParticipantUnassignDialog; //Test mode: return mockup reference
 	}
 
 	/**
 	 * Method removeParticipantUnassignDialog.
 	 */
 	public void removeParticipantUnassignDialog() {
 		fParticipantUnassignDialog.close();
 		fParticipantUnassignDialog = null;
 	}
 
 	/**
 	 * Method setParticipantUnassignDialog.
 	 * 
 	 * @param aParticipantUnassignDialog
 	 *            IParticipantUnassignDialog
 	 */
 	public void setParticipantUnassignDialog(IParticipantUnassignDialog aParticipantUnassignDialog) {
 		fParticipantUnassignDialog = aParticipantUnassignDialog;
 	}
 
 	/**
 	 * Method getReviewGroupInputDialog.
 	 * 
 	 * @return IReviewGroupInputDialog
 	 */
 	public IReviewGroupInputDialog getReviewGroupInputDialog() {
 		if (null == fReviewGroupInputDialog) {
 			fReviewGroupInputDialog = new ReviewGroupInputDialog(R4EUIModelController.getNavigatorView()
 					.getSite()
 					.getWorkbenchWindow()
 					.getShell());
 		}
 		return fReviewGroupInputDialog;
 	}
 
 	/**
 	 * Method setReviewGroupInputDialog.
 	 * 
 	 * @param aReviewGroupInputDialog
 	 *            IReviewGroupInputDialog
 	 */
 	public void setReviewGroupInputDialog(IReviewGroupInputDialog aReviewGroupInputDialog) {
 		fReviewGroupInputDialog = aReviewGroupInputDialog;
 	}
 
 	/**
 	 * Method getReviewInputDialog.
 	 * 
 	 * @param aParentGroup
 	 *            R4EUIReviewGroup
 	 * @return IReviewInputDialog
 	 */
 	public IReviewInputDialog getReviewInputDialog(R4EUIReviewGroup aParentGroup) {
		if (null == fReviewInputDialog) {
 			fReviewInputDialog = new ReviewInputDialog(R4EUIModelController.getNavigatorView()
 					.getSite()
 					.getWorkbenchWindow()
 					.getShell(), aParentGroup);
 		}
 		return fReviewInputDialog;
 	}
 
 	/**
 	 * Method setReviewInputDialog.
 	 * 
 	 * @param aReviewInputDialog
 	 *            IReviewInputDialog
 	 */
 	public void setReviewInputDialog(IReviewInputDialog aReviewInputDialog) {
 		fReviewInputDialog = aReviewInputDialog;
 	}
 
 	/**
 	 * Method getRuleAreaInputDialog.
 	 * 
 	 * @return IRuleAreaInputDialog
 	 */
 	public IRuleAreaInputDialog getRuleAreaInputDialog() {
 		if (null == fRuleAreaInputDialog) {
 			fRuleAreaInputDialog = new RuleAreaInputDialog(R4EUIModelController.getNavigatorView()
 					.getSite()
 					.getWorkbenchWindow()
 					.getShell());
 		}
 		return fRuleAreaInputDialog;
 	}
 
 	/**
 	 * Method setRuleAreaInputDialog.
 	 * 
 	 * @param aRuleAreaInputDialog
 	 *            IRuleAreaInputDialog
 	 */
 	public void setRuleAreaInputDialog(IRuleAreaInputDialog aRuleAreaInputDialog) {
 		fRuleAreaInputDialog = aRuleAreaInputDialog;
 	}
 
 	/**
 	 * Method getRuleInputDialog.
 	 * 
 	 * @return IRuleInputDialog
 	 */
 	public IRuleInputDialog getRuleInputDialog() {
 		if (null == fRuleInputDialog) {
 			fRuleInputDialog = new RuleInputDialog(R4EUIModelController.getNavigatorView()
 					.getSite()
 					.getWorkbenchWindow()
 					.getShell());
 		}
 		return fRuleInputDialog;
 	}
 
 	/**
 	 * Method setRuleInputDialog.
 	 * 
 	 * @param aRuleInputDialog
 	 *            IRuleInputDialog
 	 */
 	public void setRuleInputDialog(IRuleInputDialog aRuleInputDialog) {
 		fRuleInputDialog = aRuleInputDialog;
 	}
 
 	/**
 	 * Method getRuleSetInputDialog.
 	 * 
 	 * @return IRuleSetInputDialog
 	 */
 	public IRuleSetInputDialog getRuleSetInputDialog() {
 		if (null == fRuleSetInputDialog) {
 			fRuleSetInputDialog = new RuleSetInputDialog(R4EUIModelController.getNavigatorView()
 					.getSite()
 					.getWorkbenchWindow()
 					.getShell());
 		}
 		return fRuleSetInputDialog;
 	}
 
 	/**
 	 * Method setRuleSetInputDialog.
 	 * 
 	 * @param aRuleSetInputDialog
 	 *            IRuleSetInputDialog
 	 */
 	public void setRuleSetInputDialog(IRuleSetInputDialog aRuleSetInputDialog) {
 		fRuleSetInputDialog = aRuleSetInputDialog;
 	}
 
 	/**
 	 * Method getRuleViolationInputDialog.
 	 * 
 	 * @return IRuleViolationInputDialog
 	 */
 	public IRuleViolationInputDialog getRuleViolationInputDialog() {
 		if (null == fRuleViolationInputDialog) {
 			fRuleViolationInputDialog = new RuleViolationInputDialog(R4EUIModelController.getNavigatorView()
 					.getSite()
 					.getWorkbenchWindow()
 					.getShell());
 		}
 		return fRuleViolationInputDialog;
 	}
 
 	/**
 	 * Method setRuleViolationInputDialog.
 	 * 
 	 * @param aRuleViolationInputDialog
 	 *            IRuleViolationInputDialog
 	 */
 	public void setRuleViolationInputDialog(IRuleViolationInputDialog aRuleViolationInputDialog) {
 		fRuleViolationInputDialog = aRuleViolationInputDialog;
 	}
 
 	/**
 	 * Method getSendNotificationInputDialog.
 	 * 
 	 * @param aSource
 	 *            - ISelection
 	 * @return ISendNotificationInputDialog
 	 */
 	public ISendNotificationInputDialog getSendNotificationInputDialog(ISelection aSource) {
 		if (!UIUtils.TEST_MODE) {
 			fSendNotificationInputDialog = new SendNotificationInputDialog(R4EUIModelController.getNavigatorView()
 					.getSite()
 					.getWorkbenchWindow()
 					.getShell(), aSource);
 		}
 		return fSendNotificationInputDialog;
 	}
 
 	/**
 	 * Method setSendNotificationInputDialog.
 	 * 
 	 * @param aSendNotificationInputDialog
 	 *            ISendNotificationInputDialog
 	 */
 	public void setSendNotificationInputDialog(ISendNotificationInputDialog aSendNotificationInputDialog) {
 		fSendNotificationInputDialog = aSendNotificationInputDialog;
 	}
 
 	/**
 	 * Method getParticipantFilterInputDialog.
 	 * 
 	 * @return InputDialog
 	 */
 	public InputDialog getParticipantFilterInputDialog() {
 		if (null == fParticipantFilterInputDialog) {
 			fParticipantFilterInputDialog = new InputDialog(Display.getCurrent().getActiveShell(), "Set user name",
 					"Enter user name to filter on", null, null);
 		}
 		return fParticipantFilterInputDialog;
 	}
 
 	/**
 	 * Method setParticipantFilterInputDialog.
 	 * 
 	 * @param aParticipantFilterInputDialog
 	 *            InputDialog
 	 */
 	public void setParticipantFilterInputDialog(InputDialog aParticipantFilterInputDialog) {
 		fParticipantFilterInputDialog = aParticipantFilterInputDialog;
 	}
 
 	/**
 	 * Method setScmUIConnector.
 	 * 
 	 * @param aConnectorUi
 	 *            ScmConnectorUi
 	 */
 	public void setScmUIConnector(ScmConnectorUi aConnectorUi) {
 		fScmConnectorUi = aConnectorUi;
 	}
 
 	/**
 	 * Method getScmUiConnector.
 	 * 
 	 * @param aProject
 	 *            IProject
 	 * @return ScmConnectorUi
 	 */
 	public ScmConnectorUi getScmUiConnector(IProject aProject) {
 		if (!UIUtils.TEST_MODE) {
 			fScmConnectorUi = ScmUi.getUiConnector(aProject);
 			return fScmConnectorUi;
 		}
 		return fScmConnectorUi; //Test mode: return mockup reference
 	}
 
 	/**
 	 * Method setMailConnector.
 	 * 
 	 * @param aConnector
 	 *            NotificationsConnector
 	 */
 	public void setMailConnector(NotificationsConnector aConnector) {
 		fNotificationsConnector = aConnector;
 	}
 
 	/**
 	 * Method getScmUiConnector.
 	 * 
 	 * @return ScmConnectorUi
 	 */
 	public NotificationsConnector getMailConnector() {
 		if (!UIUtils.TEST_MODE) {
 			return NotificationsCore.getFirstEnabled(MAIL_CONNECTOR_IDS);
 		}
 		return fNotificationsConnector; //Test mode: return mockup reference
 	}
 }
