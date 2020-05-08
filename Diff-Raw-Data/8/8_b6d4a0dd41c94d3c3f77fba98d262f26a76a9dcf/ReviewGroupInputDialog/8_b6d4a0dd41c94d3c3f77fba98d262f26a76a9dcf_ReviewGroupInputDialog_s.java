 // $codepro.audit.disable com.instantiations.assist.eclipse.analysis.audit.rule.effectivejava.alwaysOverridetoString.alwaysOverrideToString, com.instantiations.assist.eclipse.analysis.audit.rule.effectivejava.constructorsOnlyInvokeFinalMethods, com.instantiations.assist.eclipse.analysis.deserializeabilitySecurity, com.instantiations.assist.eclipse.analysis.disallowReturnMutable, com.instantiations.assist.eclipse.analysis.enforceCloneableUsageSecurity, explicitThisUsage
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
  * This class implements the dialog used to fill-in the Review Group element 
  * details.  This is a modal dialog
  * 
  * Contributors:
  *   Sebastien Dubois - Created for Mylyn Review R4E project
  *   
  ******************************************************************************/
 
 package org.eclipse.mylyn.reviews.r4e.ui.internal.dialogs;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.jface.dialogs.ErrorDialog;
 import org.eclipse.jface.dialogs.IDialogConstants;
 import org.eclipse.jface.dialogs.IInputValidator;
 import org.eclipse.mylyn.reviews.r4e.ui.R4EUIPlugin;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.model.R4EUIModelController;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.model.R4EUIRootElement;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.model.R4EUIRuleSet;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.utils.EditableListWidget;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.utils.R4EUIConstants;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.CCombo;
 import org.eclipse.swt.events.ModifyEvent;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.DirectoryDialog;
 import org.eclipse.swt.widgets.Item;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.ui.ISharedImages;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.forms.FormDialog;
 import org.eclipse.ui.forms.IManagedForm;
 import org.eclipse.ui.forms.events.ExpansionAdapter;
 import org.eclipse.ui.forms.events.ExpansionEvent;
 import org.eclipse.ui.forms.widgets.ExpandableComposite;
 import org.eclipse.ui.forms.widgets.FormToolkit;
 import org.eclipse.ui.forms.widgets.ScrolledForm;
 import org.eclipse.ui.forms.widgets.Section;
 
 /**
  * @author lmcdubo
  * @version $Revision: 1.0 $
  */
 public class ReviewGroupInputDialog extends FormDialog implements IReviewGroupInputDialog {
 
 	// ------------------------------------------------------------------------
 	// Constants
 	// ------------------------------------------------------------------------
 
 	/**
 	 * Field ADD_REVIEW_GROUP_DIALOG_TITLE. (value is ""Enter Review Group details"")
 	 */
 	private static final String ADD_REVIEW_GROUP_DIALOG_TITLE = "Enter Review Group Details";
 
 	/**
 	 * Field ADD_REVIEW_GROUP_NAME_DIALOG_VALUE. (value is ""Enter the Review Group Name:"")
 	 */
 	private static final String ADD_REVIEW_GROUP_NAME_DIALOG_VALUE = "Group Name: ";
 
 	/**
 	 * Field ADD_REVIEW_GROUP_FOLDER_DIALOG_VALUE. (value is ""Enter the Review Group Folder:"")
 	 */
 	private static final String ADD_REVIEW_GROUP_FOLDER_DIALOG_VALUE = "Group Folder: ";
 
 	/**
 	 * Field ADD_REVIEW_GROUP_DESCRIPTION_DIALOG_VALUE. (value is ""Enter the Review Group Description:"")
 	 */
 	private static final String ADD_REVIEW_GROUP_DESCRIPTION_DIALOG_VALUE = "Group Description: ";
 
 	/**
 	 * Field ADD_REVIEW_GROUP_AVAILABLE_PROJECTS_DIALOG_VALUE. (value is ""Available Projects:"")
 	 */
 	private static final String ADD_REVIEW_GROUP_AVAILABLE_PROJECTS_DIALOG_VALUE = "Default Projects: ";
 
 	/**
 	 * Field ADD_REVIEW_GROUP_AVAILABLE_COMPONENTS_DIALOG_VALUE. (value is ""Available Components:"")
 	 */
 	private static final String ADD_REVIEW_GROUP_AVAILABLE_COMPONENTS_DIALOG_VALUE = "Default Components: ";
 
 	/**
 	 * Field ADD_REVIEW_GROUP_ENTRY_CRITERIA_DIALOG_VALUE. (value is ""Default Entry Criteria:"")
 	 */
 	private static final String ADD_REVIEW_GROUP_ENTRY_CRITERIA_DIALOG_VALUE = "Default Entry Criteria: ";
 
 	/**
 	 * Field ADD_RULE_SETS_DIALOG_VALUE. (value is ""Rule Sets: "")
 	 */
 	private static final String ADD_RULE_SETS_DIALOG_VALUE = "Rule Sets: ";
 
 	/**
 	 * Field BASIC_PARAMS_HEADER_MSG. (value is ""Enter the mandatory basic parameters for this Review Group"")
 	 */
 	private static final String BASIC_PARAMS_HEADER_MSG = "Enter the mandatory basic parameters for this Review Group";
 
 	/**
 	 * Field EXTRA_PARAMS_HEADER_MSG. (value is ""Enter the optional extra parameters for this Review Group"")
 	 */
 	private static final String EXTRA_PARAMS_HEADER_MSG = "Enter the optional extra parameters for this Review Group";
 
 	// ------------------------------------------------------------------------
 	// Member variables
 	// ------------------------------------------------------------------------
 
 	/**
 	 * The input value; the empty string by default.
 	 */
 	private String fGroupNameValue = "";
 
 	/**
 	 * Input text widget.
 	 */
 	private Text fGroupNameInputTextField;
 
 	/**
 	 * Field fGroupFolderValue.
 	 */
 	private String fGroupFolderValue = "";
 
 	/**
 	 * Field fGroupFolderInputTextField.
 	 */
 	private Text fGroupFolderInputTextField = null;
 
 	/**
 	 * Field fGroupDescriptionValue.
 	 */
 	private String fGroupDescriptionValue = "";
 
 	/**
 	 * Field fGroupDescriptionInputTextField.
 	 */
 	private Text fGroupDescriptionInputTextField = null;
 
 	/**
 	 * Field fAvailableProjectsValues.
 	 */
 	private String[] fAvailableProjectsValues = null;
 
 	/**
 	 * Field fAvailableProjects.
 	 */
 	private EditableListWidget fAvailableProjects = null;
 
 	/**
 	 * Field fAvailableComponentsValues.
 	 */
 	private String[] fAvailableComponentsValues = null;
 
 	/**
 	 * Field fAvailableComponents.
 	 */
 	private EditableListWidget fAvailableComponents = null;
 
 	/**
 	 * Field fDefaultEntryCriteriaValue.
 	 */
 	private String fDefaultEntryCriteriaValue = "";
 
 	/**
 	 * Field fDefaultEntryCriteriaTextField.
 	 */
 	private Text fDefaultEntryCriteriaTextField = null;
 
 	/**
 	 * Field fRuleSets.
 	 */
 	private EditableListWidget fRuleSets = null;
 
 	/**
 	 * Field fRuleSetsValues.
 	 */
 	private String[] fRuleSetsValues = null;
 
 	/**
 	 * The input validator, or <code>null</code> if none.
 	 */
 	private final IInputValidator fValidator;
 
 	// ------------------------------------------------------------------------
 	// Constructors
 	// ------------------------------------------------------------------------
 
 	/**
 	 * Constructor for R4EReviewGroupInputDialog.
 	 * 
 	 * @param aParentShell
 	 *            Shell
 	 */
 	public ReviewGroupInputDialog(Shell aParentShell) {
 		super(aParentShell);
 		setBlockOnOpen(true);
 		fValidator = new R4EInputValidator();
 	}
 
 	// ------------------------------------------------------------------------
 	// Methods
 	// ------------------------------------------------------------------------
 
 	/**
 	 * Method buttonPressed.
 	 * 
 	 * @param buttonId
 	 *            int
 	 * @see org.eclipse.jface.dialogs.Dialog#buttonPressed(int)
 	 */
 	@Override
 	protected void buttonPressed(int buttonId) {
 		if (buttonId == IDialogConstants.OK_ID) {
 			//Validate Name
 			String validateResult = validateEmptyInput(fGroupNameInputTextField);
 			if (null != validateResult) {
 				//Validate of input failed
 				final ErrorDialog dialog = new ErrorDialog(null, R4EUIConstants.DIALOG_TITLE_ERROR,
 						"No input given for Group Name", new Status(IStatus.ERROR, R4EUIPlugin.PLUGIN_ID, 0,
 								validateResult, null), IStatus.ERROR);
 				dialog.open();
 				return;
 			}
 			fGroupNameValue = fGroupNameInputTextField.getText();
 
 			//Validate Folder
 			validateResult = validateFolderInput(fGroupFolderInputTextField);
 			if (null != validateResult) {
 				//Validate of input failed
 				final ErrorDialog dialog = new ErrorDialog(null, R4EUIConstants.DIALOG_TITLE_ERROR,
 						"Invalid input folder", new Status(IStatus.ERROR, R4EUIPlugin.PLUGIN_ID, 0, validateResult,
 								null), IStatus.ERROR);
 				dialog.open();
 				return;
 			}
 
 			validateResult = validateGroupExists(fGroupFolderInputTextField);
 			if (null != validateResult) {
 				//Validate of input failed
 				final ErrorDialog dialog = new ErrorDialog(null, R4EUIConstants.DIALOG_TITLE_ERROR,
 						"Invalid input folder", new Status(IStatus.ERROR, R4EUIPlugin.PLUGIN_ID, 0, validateResult,
 								null), IStatus.ERROR);
 				dialog.open();
 				return;
 			}
 			fGroupFolderValue = fGroupFolderInputTextField.getText();
 
 			//Validate Projects (optional)
 			final ArrayList<String> projectsValues = new ArrayList<String>();
 			for (Item item : fAvailableProjects.getItems()) {
 				validateResult = validateEmptyInput(item.getText());
 				if (null == validateResult) {
 					projectsValues.add(item.getText());
 				}
 			}
 			fAvailableProjectsValues = projectsValues.toArray(new String[projectsValues.size()]);
 
 			//Validate Components (optional)
 			final ArrayList<String> componentsValues = new ArrayList<String>();
 			for (Item item : fAvailableComponents.getItems()) {
 				validateResult = validateEmptyInput(item.getText());
 				if (null == validateResult) {
 					componentsValues.add(item.getText());
 				}
 			}
 			fAvailableComponentsValues = componentsValues.toArray(new String[componentsValues.size()]);
 
 			//Validate Entry Criteria (optional)
 			validateResult = validateEmptyInput(fDefaultEntryCriteriaTextField);
 			if (null == validateResult) {
 				fDefaultEntryCriteriaValue = fDefaultEntryCriteriaTextField.getText();
 			}
 
 			//Validate Rule Sets (optional)
 			final ArrayList<String> ruleSetLocationsValues = new ArrayList<String>();
 			for (Item item : fRuleSets.getItems()) {
 				validateResult = validateEmptyInput(item.getText());
 				if (null == validateResult) {
 					ruleSetLocationsValues.add(item.getText());
 				}
 			}
 			fRuleSetsValues = ruleSetLocationsValues.toArray(new String[ruleSetLocationsValues.size()]);
 
 			fGroupDescriptionValue = fGroupDescriptionInputTextField.getText();
 
 		} else {
			fGroupNameValue = null;
			fGroupFolderValue = null;
			fGroupDescriptionValue = null;
			fDefaultEntryCriteriaValue = null;
 		}
 		super.buttonPressed(buttonId);
 	}
 
 	/**
 	 * Method configureShell.
 	 * 
 	 * @param shell
 	 *            Shell
 	 * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
 	 */
 	@Override
 	protected void configureShell(Shell shell) {
 		super.configureShell(shell);
 		shell.setText(ADD_REVIEW_GROUP_DIALOG_TITLE);
 		shell.setMinimumSize(R4EUIConstants.DIALOG_DEFAULT_WIDTH, R4EUIConstants.DIALOG_DEFAULT_HEIGHT);
 
 	}
 
 	/**
 	 * Configures the dialog form and creates form content. Clients should override this method.
 	 * 
 	 * @param mform
 	 *            the dialog form
 	 */
 	@Override
 	protected void createFormContent(final IManagedForm mform) {
 
 		final FormToolkit toolkit = mform.getToolkit();
 		final ScrolledForm sform = mform.getForm();
 		final Composite composite = sform.getBody();
 		composite.setLayout(new GridLayout());
 		GridData textGridData = null;
 
 		//Basic parameters section
 		final Section basicSection = toolkit.createSection(composite, Section.DESCRIPTION
 				| ExpandableComposite.TITLE_BAR | ExpandableComposite.TWISTIE | ExpandableComposite.EXPANDED);
 		basicSection.setLayout(new GridLayout());
 		basicSection.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true));
 		basicSection.setText(R4EUIConstants.BASIC_PARAMS_HEADER);
 		basicSection.setDescription(BASIC_PARAMS_HEADER_MSG);
 		basicSection.addExpansionListener(new ExpansionAdapter() {
 			@Override
 			public void expansionStateChanged(ExpansionEvent e) {
 				getShell().setSize(getShell().computeSize(SWT.DEFAULT, SWT.DEFAULT));
 			}
 		});
 
 		final Composite basicSectionClient = toolkit.createComposite(basicSection);
 		basicSectionClient.setLayout(new GridLayout(4, false));
 		basicSectionClient.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true));
 		basicSection.setClient(basicSectionClient);
 
 		//Review Group Name
 		Label label = toolkit.createLabel(basicSectionClient, ADD_REVIEW_GROUP_NAME_DIALOG_VALUE);
 		label.setToolTipText(R4EUIConstants.REVIEW_GROUP_NAME_TOOLTIP);
 		label.setLayoutData(new GridData(GridData.BEGINNING, GridData.BEGINNING, false, false));
 		fGroupNameInputTextField = toolkit.createText(basicSectionClient, "", SWT.SINGLE | SWT.BORDER);
 		textGridData = new GridData(GridData.FILL, GridData.FILL, true, false);
 		textGridData.horizontalSpan = 3;
 		fGroupNameInputTextField.setToolTipText(R4EUIConstants.REVIEW_GROUP_NAME_TOOLTIP);
 		fGroupNameInputTextField.setLayoutData(textGridData);
 		fGroupNameInputTextField.addModifyListener(new ModifyListener() {
 			public void modifyText(ModifyEvent e) {
 				// ignore
 				if (fGroupNameInputTextField.getText().length() > 0
 						&& fGroupFolderInputTextField.getText().length() > 0
 						&& fGroupDescriptionInputTextField.getText().length() > 0) {
 					getButton(IDialogConstants.OK_ID).setEnabled(true);
 				} else {
 					getButton(IDialogConstants.OK_ID).setEnabled(false);
 				}
 			}
 		});
 
 		//Group Folder
 		label = toolkit.createLabel(basicSectionClient, ADD_REVIEW_GROUP_FOLDER_DIALOG_VALUE);
 		label.setToolTipText(R4EUIConstants.REVIEW_GROUP_FILE_PATH_TOOLTIP);
 		label.setLayoutData(new GridData(GridData.BEGINNING, GridData.BEGINNING, false, false));
 		fGroupFolderInputTextField = toolkit.createText(basicSectionClient, "", SWT.SINGLE | SWT.BORDER);
 		final GridData folderTextData = new GridData(GridData.FILL, GridData.BEGINNING, true, false);
 		folderTextData.horizontalSpan = 2;
 		fGroupFolderInputTextField.setToolTipText(R4EUIConstants.REVIEW_GROUP_FILE_PATH_TOOLTIP);
 		fGroupFolderInputTextField.setLayoutData(folderTextData);
 		final Button folderButton = toolkit.createButton(basicSectionClient, "", SWT.NONE);
 		folderButton.setImage(PlatformUI.getWorkbench()
 				.getSharedImages()
 				.getImageDescriptor(ISharedImages.IMG_OBJ_FOLDER)
 				.createImage()); // $codepro.audit.disable methodChainLength
 		textGridData = new GridData(GridData.BEGINNING, GridData.BEGINNING, false, false);
 		textGridData.horizontalSpan = 1;
 		folderButton.setToolTipText(R4EUIConstants.REVIEW_GROUP_FOLDER_TOOLTIP);
 		folderButton.setLayoutData(textGridData);
 		folderButton.addSelectionListener(new SelectionAdapter() { // $codepro.audit.disable com.instantiations.assist.eclipse.analysis.avoidInnerClasses
 			@SuppressWarnings("synthetic-access")
 			@Override
 			public void widgetSelected(SelectionEvent event) {
 				final String result = folderButtonPressed();
 				if (null == result) {
 					fGroupFolderInputTextField.setText("");
 				} else {
 					fGroupFolderInputTextField.setText(result);
 				}
 			}
 		});
 		fGroupFolderInputTextField.addModifyListener(new ModifyListener() {
 			public void modifyText(ModifyEvent e) {
 				// ignore
 				if (fGroupNameInputTextField.getText().length() > 0
 						&& fGroupFolderInputTextField.getText().length() > 0
 						&& fGroupDescriptionInputTextField.getText().length() > 0) {
 					getButton(IDialogConstants.OK_ID).setEnabled(true);
 				} else {
 					getButton(IDialogConstants.OK_ID).setEnabled(false);
 				}
 			}
 		});
 
 		//Group Description
 		label = toolkit.createLabel(basicSectionClient, ADD_REVIEW_GROUP_DESCRIPTION_DIALOG_VALUE);
 		label.setToolTipText(R4EUIConstants.REVIEW_GROUP_DESCRIPTION_TOOLTIP);
 		label.setLayoutData(new GridData(GridData.BEGINNING, GridData.BEGINNING, false, false));
 		fGroupDescriptionInputTextField = toolkit.createText(basicSectionClient, "", SWT.MULTI | SWT.V_SCROLL
 				| SWT.BORDER);
 		textGridData = new GridData(GridData.FILL, GridData.FILL, true, false);
 		textGridData.horizontalSpan = 3;
 		textGridData.heightHint = fGroupNameInputTextField.getLineHeight() * 3;
 		fGroupDescriptionInputTextField.setToolTipText(R4EUIConstants.REVIEW_GROUP_DESCRIPTION_TOOLTIP);
 		fGroupDescriptionInputTextField.setLayoutData(textGridData);
 		fGroupDescriptionInputTextField.addModifyListener(new ModifyListener() {
 			public void modifyText(ModifyEvent e) {
 				// ignore
 				if (fGroupNameInputTextField.getText().length() > 0
 						&& fGroupFolderInputTextField.getText().length() > 0
 						&& fGroupDescriptionInputTextField.getText().length() > 0) {
 					getButton(IDialogConstants.OK_ID).setEnabled(true);
 				} else {
 					getButton(IDialogConstants.OK_ID).setEnabled(false);
 				}
 			}
 		});
 
 		//Extra parameters section
 		final Section extraSection = toolkit.createSection(composite, Section.DESCRIPTION
 				| ExpandableComposite.TITLE_BAR | ExpandableComposite.TWISTIE);
 		extraSection.setLayout(new GridLayout());
 		extraSection.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true));
 		extraSection.setText(R4EUIConstants.EXTRA_PARAMS_HEADER);
 		extraSection.setDescription(EXTRA_PARAMS_HEADER_MSG);
 		extraSection.addExpansionListener(new ExpansionAdapter() {
 			@Override
 			public void expansionStateChanged(ExpansionEvent e) {
 				getShell().setSize(getShell().computeSize(SWT.DEFAULT, SWT.DEFAULT));
 			}
 		});
 
 		final Composite extraSectionClient = toolkit.createComposite(extraSection);
 		extraSectionClient.setLayout(new GridLayout(4, false));
 		extraSectionClient.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true));
 		extraSection.setClient(extraSectionClient);
 
 		//Available Projects
 		label = toolkit.createLabel(extraSectionClient, ADD_REVIEW_GROUP_AVAILABLE_PROJECTS_DIALOG_VALUE);
 		label.setToolTipText(R4EUIConstants.REVIEW_GROUP_PROJECTS_TOOLTIP);
 		label.setLayoutData(new GridData(GridData.BEGINNING, GridData.BEGINNING, false, false));
 		textGridData = new GridData(GridData.FILL, GridData.FILL, true, false);
 		textGridData.horizontalSpan = 3;
 		fAvailableProjects = new EditableListWidget(toolkit, extraSectionClient, textGridData, null, 0, Text.class,
 				null);
 		fAvailableProjects.setToolTipText(R4EUIConstants.REVIEW_GROUP_PROJECTS_TOOLTIP);
 
 		//Available Components
 		label = toolkit.createLabel(extraSectionClient, ADD_REVIEW_GROUP_AVAILABLE_COMPONENTS_DIALOG_VALUE);
 		label.setToolTipText(R4EUIConstants.REVIEW_GROUP_COMPONENTS_TOOLTIP);
 		label.setLayoutData(new GridData(GridData.BEGINNING, GridData.BEGINNING, false, false));
 		textGridData = new GridData(GridData.FILL, GridData.FILL, true, false);
 		textGridData.horizontalSpan = 3;
 		fAvailableComponents = new EditableListWidget(toolkit, extraSectionClient, textGridData, null, 0, Text.class,
 				null);
 		fAvailableComponents.setToolTipText(R4EUIConstants.REVIEW_GROUP_COMPONENTS_TOOLTIP);
 
 		// Default Entry Criteria
 		label = toolkit.createLabel(extraSectionClient, ADD_REVIEW_GROUP_ENTRY_CRITERIA_DIALOG_VALUE);
 		label.setToolTipText(R4EUIConstants.REVIEW_GROUP_ENTRY_CRITERIA_TOOLTIP);
 		label.setLayoutData(new GridData(GridData.BEGINNING, GridData.BEGINNING, false, false));
 		fDefaultEntryCriteriaTextField = toolkit.createText(extraSectionClient, "", SWT.MULTI | SWT.V_SCROLL
 				| SWT.BORDER);
 		textGridData = new GridData(GridData.FILL, GridData.FILL, true, false);
 		textGridData.horizontalSpan = 3;
 		textGridData.heightHint = fGroupNameInputTextField.getLineHeight() * 3;
 		fDefaultEntryCriteriaTextField.setToolTipText(R4EUIConstants.REVIEW_GROUP_ENTRY_CRITERIA_TOOLTIP);
 		fDefaultEntryCriteriaTextField.setLayoutData(textGridData);
 
 		//RuleSet references
 		label = toolkit.createLabel(extraSectionClient, ADD_RULE_SETS_DIALOG_VALUE);
 		label.setToolTipText(R4EUIConstants.REVIEW_GROUP_RULESET_REFERENCE_TOOLTIP);
 		label.setLayoutData(new GridData(GridData.BEGINNING, GridData.BEGINNING, false, false));
 		final List<R4EUIRuleSet> uiRuleSets = ((R4EUIRootElement) R4EUIModelController.getRootElement()).getRuleSets();
 		final List<String> ruleLocations = new ArrayList<String>();
 		for (R4EUIRuleSet ruleSet : uiRuleSets) {
 			if (ruleSet.isOpen()) {
 				ruleLocations.add(ruleSet.getRuleSet().eResource().getURI().toFileString());
 			}
 		}
 		textGridData = new GridData(GridData.FILL, GridData.FILL, true, false);
 		textGridData.horizontalSpan = 3;
 		fRuleSets = new EditableListWidget(toolkit, extraSectionClient, textGridData, null, 0, CCombo.class,
 				ruleLocations.toArray(new String[ruleLocations.size()]));
 		if (0 == ruleLocations.size()) {
 			fRuleSets.setEnabled(false);
 		}
 		fRuleSets.setToolTipText(R4EUIConstants.REVIEW_GROUP_RULESET_REFERENCE_TOOLTIP);
 	}
 
 	/**
 	 * Configures the button bar.
 	 * 
 	 * @param parent
 	 *            the parent composite
 	 * @return Control
 	 */
 	@Override
 	protected Control createButtonBar(Composite parent) {
 		final Control bar = super.createButtonBar(parent);
 		getButton(IDialogConstants.OK_ID).setEnabled(false);
 		return bar;
 	}
 
 	/**
 	 * Method folderButtonPressed.
 	 * 
 	 * @return String
 	 */
 	protected String folderButtonPressed() {
 		//Open folder dialog
 		final DirectoryDialog dialog = new DirectoryDialog(getShell(), SWT.SHEET);
 		dialog.setMessage("Select Folder...");
 		String dir = dialog.open();
 		if (null != dir) {
 			dir = dir.trim();
 			if (0 == dir.length()) {
 				return null;
 			}
 		}
 		return dir;
 	}
 
 	/**
 	 * Method isResizable.
 	 * 
 	 * @return boolean * @see org.eclipse.jface.dialogs.Dialog#isResizable()
 	 */
 	@Override
 	protected boolean isResizable() {
 		return true;
 	}
 
 	/**
 	 * Returns the string typed into this input dialog.
 	 * 
 	 * @return the group name input string * @see
 	 *         org.eclipse.mylyn.reviews.r4e.ui.internal.dialogs.IReviewGroupInputDialog#getGroupNameValue()
 	 */
 	public String getGroupNameValue() {
 		return fGroupNameValue;
 	}
 
 	/**
 	 * Returns the string typed into this input dialog.
 	 * 
 	 * @return the group folder input string * @see
 	 *         org.eclipse.mylyn.reviews.r4e.ui.internal.dialogs.IReviewGroupInputDialog#getGroupFolderValue()
 	 */
 	public String getGroupFolderValue() {
 		return fGroupFolderValue;
 	}
 
 	/**
 	 * Returns the string typed into this input dialog.
 	 * 
 	 * @return the group description input string * @see
 	 *         org.eclipse.mylyn.reviews.r4e.ui.internal.dialogs.IReviewGroupInputDialog#getGroupDescriptionValue()
 	 */
 	public String getGroupDescriptionValue() {
 		return fGroupDescriptionValue;
 	}
 
 	/**
 	 * Returns the strings typed into this input dialog.
 	 * 
 	 * @return the Available projects input strings * @see
 	 *         org.eclipse.mylyn.reviews.r4e.ui.internal.dialogs.IReviewGroupInputDialog#getAvailableProjectsValues()
 	 */
 	public String[] getAvailableProjectsValues() {
 		return fAvailableProjectsValues;
 	}
 
 	/**
 	 * Returns the strings typed into this input dialog.
 	 * 
 	 * @return the Available components input strings * @see
 	 *         org.eclipse.mylyn.reviews.r4e.ui.internal.dialogs.IReviewGroupInputDialog#getAvailableComponentsValues()
 	 */
 	public String[] getAvailableComponentsValues() {
 		return fAvailableComponentsValues;
 	}
 
 	/**
 	 * Returns the string typed into this input dialog.
 	 * 
 	 * @return the Default entry criteria input string * @see
 	 *         org.eclipse.mylyn.reviews.r4e.ui.internal.dialogs.IReviewGroupInputDialog#getDefaultEntryCriteriaValue()
 	 */
 	public String getDefaultEntryCriteriaValue() {
 		return fDefaultEntryCriteriaValue;
 	}
 
 	/**
 	 * Returns the strings typed into this input dialog.
 	 * 
 	 * @return the Rule Set input strings * @see
 	 *         org.eclipse.mylyn.reviews.r4e.ui.internal.dialogs.IReviewGroupInputDialog#getRuleSetValues()
 	 */
 	public String[] getRuleSetValues() {
 		return fRuleSetsValues;
 	}
 
 	/**
 	 * Validates the input.
 	 * <p>
 	 * The default implementation of this framework method delegates the request to the supplied input validator object;
 	 * if it finds the input invalid, the error message is displayed in the dialog's message line. This hook method is
 	 * called whenever the text changes in the input field.
 	 * </p>
 	 * 
 	 * @param aText
 	 *            Text
 	 * @return String
 	 */
 	protected String validateFolderInput(Text aText) {
 		return ((R4EInputValidator) fValidator).isFolderValid(aText.getText());
 	}
 
 	/**
 	 * Method validateGroupExists.
 	 * 
 	 * @param aText
 	 *            Text
 	 * @return String
 	 */
 	private String validateGroupExists(Text aText) {
 		return ((R4EInputValidator) fValidator).isFolderEmpty(aText.getText());
 	}
 
 	/**
 	 * Method validateEmptyInput.
 	 * 
 	 * @param aText
 	 *            Text
 	 * @return String
 	 */
 	private String validateEmptyInput(Text aText) {
 		return fValidator.isValid(aText.getText());
 	}
 
 	/**
 	 * Method validateEmptyInput.
 	 * 
 	 * @param aString
 	 *            String
 	 * @return String
 	 */
 	private String validateEmptyInput(String aString) {
 		return fValidator.isValid(aString);
 	}
 }
