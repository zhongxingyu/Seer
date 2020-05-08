 // $codepro.audit.disable com.instantiations.assist.eclipse.analysis.audit.rule.effectivejava.alwaysOverridetoString.alwaysOverrideToString, com.instantiations.assist.eclipse.analysis.deserializeabilitySecurity, com.instantiations.assist.eclipse.analysis.enforceCloneableUsageSecurity, com.instantiations.assist.eclipse.analysis.instanceFieldSecurity, sourceLength, explicitThisUsage
 /*******************************************************************************
  * Copyright (c) 2010, 2012 Ericsson AB and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Ericsson AB - initial API and implementation
  *******************************************************************************/
 
 package org.eclipse.mylyn.reviews.r4e.ui.internal.properties.tabbed;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EReviewGroup;
 import org.eclipse.mylyn.reviews.r4e.core.model.serial.impl.OutOfSyncException;
 import org.eclipse.mylyn.reviews.r4e.core.model.serial.impl.ResourceHandlingException;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.model.R4EUIModelController;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.model.R4EUIReviewGroup;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.model.R4EUIRootElement;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.model.R4EUIRuleSet;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.utils.EditableListWidget;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.utils.IEditableListListener;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.utils.R4EUIConstants;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.utils.UIUtils;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.CCombo;
 import org.eclipse.swt.custom.CLabel;
 import org.eclipse.swt.custom.ScrolledComposite;
 import org.eclipse.swt.layout.FormAttachment;
 import org.eclipse.swt.layout.FormData;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Item;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.ui.forms.events.ExpansionAdapter;
 import org.eclipse.ui.forms.events.ExpansionEvent;
 import org.eclipse.ui.forms.widgets.ExpandableComposite;
 import org.eclipse.ui.views.properties.tabbed.ITabbedPropertyConstants;
 import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;
 import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetWidgetFactory;
 
 /**
  * This class implements the tabbed property section for the Review Group model element
  * 
  * @author Sebastien Dubois
  * @version $Revision: 1.0 $
  */
 public class ReviewGroupTabPropertySection extends ModelElementTabPropertySection implements IEditableListListener {
 
 	// ------------------------------------------------------------------------
 	// Constants
 	// ------------------------------------------------------------------------
 
 	/**
 	 * Field PROJ_COMP_SECTION_LABEL. (value is ""Projects & Components"")
 	 */
 	private static final String PROJ_COMP_SECTION_LABEL = "Projects and Components";
 
 	/**
 	 * Field RULE_SETS_SECTION_LABEL. (value is ""Rule Sets"")
 	 */
 	private static final String RULE_SETS_SECTION_LABEL = "Rule Sets";
 
 	// ------------------------------------------------------------------------
 	// Member variables
 	// ------------------------------------------------------------------------
 
 	/**
 	 * Field fNameText.
 	 */
 	private Text fNameText = null;
 
 	/**
 	 * Field fFolderText.
 	 */
 	private Text fFilePathText = null;
 
 	/**
 	 * Field fDescriptionText.
 	 */
 	protected Text fDescriptionText = null;
 
 	/**
 	 * Field fAvailableProjects.
 	 */
 	protected EditableListWidget fAvailableProjects = null;
 
 	/**
 	 * Field fAvailableComponents.
 	 */
 	protected EditableListWidget fAvailableComponents = null;
 
 	/**
 	 * Field fDefaultEntryCriteriaText.
 	 */
 	protected Text fDefaultEntryCriteriaText = null;
 
 	/**
 	 * Field fRuleSets.
 	 */
 	protected EditableListWidget fRuleSetLocations = null;
 
 	// ------------------------------------------------------------------------
 	// Methods
 	// ------------------------------------------------------------------------
 
 	/**
 	 * Method dispose.
 	 * 
 	 * @see org.eclipse.ui.views.properties.tabbed.ISection#dispose()
 	 */
 	@Override
 	public void dispose() {
 		if (null != fAvailableProjects) {
 			fAvailableProjects.dispose();
 		}
 		if (null != fAvailableComponents) {
 			fAvailableComponents.dispose();
 		}
 		super.dispose();
 	}
 
 	/**
 	 * Method shouldUseExtraSpace.
 	 * 
 	 * @return boolean
 	 * @see org.eclipse.ui.views.properties.tabbed.ISection#shouldUseExtraSpace()
 	 */
 	@Override
 	public boolean shouldUseExtraSpace() {
 		return true;
 	}
 
 	/**
 	 * Method createControls.
 	 * 
 	 * @param parent
 	 *            Composite
 	 * @param aTabbedPropertySheetPage
 	 *            TabbedPropertySheetPage
 	 * @see org.eclipse.ui.views.properties.tabbed.ISection#createControls(Composite, TabbedPropertySheetPage)
 	 */
 	@Override
 	public void createControls(Composite parent, TabbedPropertySheetPage aTabbedPropertySheetPage) {
 		super.createControls(parent, aTabbedPropertySheetPage);
 
 		//Tell element to build its own detailed tab layout
 		final TabbedPropertySheetWidgetFactory widgetFactory = aTabbedPropertySheetPage.getWidgetFactory();
 		final Composite composite = widgetFactory.createFlatFormComposite(parent);
 		FormData data = null;
 
 		//Group Name (Read-only)
 		widgetFactory.setBorderStyle(SWT.NULL);
 		fNameText = widgetFactory.createText(composite, "", SWT.NULL);
 		data = new FormData();
 		data.left = new FormAttachment(0, R4EUIConstants.TABBED_PROPERTY_LABEL_WIDTH);
 		data.right = new FormAttachment(100, 0); // $codepro.audit.disable numericLiterals
 		data.top = new FormAttachment(0, ITabbedPropertyConstants.VSPACE);
 		fNameText.setEditable(false);
 		fNameText.setToolTipText(R4EUIConstants.REVIEW_GROUP_NAME_TOOLTIP);
 		fNameText.setLayoutData(data);
 
 		final CLabel nameLabel = widgetFactory.createCLabel(composite, R4EUIConstants.NAME_LABEL);
 		data = new FormData();
 		data.left = new FormAttachment(0, 0);
 		data.right = new FormAttachment(fNameText, -ITabbedPropertyConstants.HSPACE);
 		data.top = new FormAttachment(fNameText, 0, SWT.CENTER);
 		nameLabel.setToolTipText(R4EUIConstants.REVIEW_GROUP_NAME_TOOLTIP);
 		nameLabel.setLayoutData(data);
 
 		//Group Folder (read-only)
 		fFilePathText = widgetFactory.createText(composite, "", SWT.NULL);
 		data = new FormData();
 		data.left = new FormAttachment(0, R4EUIConstants.TABBED_PROPERTY_LABEL_WIDTH);
 		data.right = new FormAttachment(100, 0); // $codepro.audit.disable numericLiterals
 		data.top = new FormAttachment(fNameText, ITabbedPropertyConstants.VSPACE);
 		fFilePathText.setEditable(false);
 		fFilePathText.setToolTipText(R4EUIConstants.REVIEW_GROUP_FILE_PATH_TOOLTIP);
 		fFilePathText.setLayoutData(data);
 
 		final CLabel folderLabel = widgetFactory.createCLabel(composite, R4EUIConstants.FILE_LABEL);
 		data = new FormData();
 		data.left = new FormAttachment(0, 0);
 		data.right = new FormAttachment(fFilePathText, -ITabbedPropertyConstants.HSPACE);
 		data.top = new FormAttachment(fFilePathText, 0, SWT.CENTER);
 		folderLabel.setToolTipText(R4EUIConstants.REVIEW_GROUP_FILE_PATH_TOOLTIP);
 		folderLabel.setLayoutData(data);
 
 		//Group Description
 		widgetFactory.setBorderStyle(SWT.BORDER);
 		fDescriptionText = widgetFactory.createText(composite, "", SWT.MULTI);
 		data = new FormData();
 		data.left = new FormAttachment(0, R4EUIConstants.TABBED_PROPERTY_LABEL_WIDTH);
 		data.right = new FormAttachment(100, 0); // $codepro.audit.disable numericLiterals
 		data.top = new FormAttachment(fFilePathText, ITabbedPropertyConstants.VSPACE);
 		fDescriptionText.setToolTipText(R4EUIConstants.REVIEW_GROUP_DESCRIPTION_TOOLTIP);
 		fDescriptionText.setLayoutData(data);
 		fDescriptionText.addListener(SWT.FocusOut, new Listener() {
 			public void handleEvent(Event event) {
 				if (!fRefreshInProgress && fDescriptionText.getForeground().equals(UIUtils.ENABLED_FONT_COLOR)) {
 					try {
 						final String currentUser = R4EUIModelController.getReviewer();
 						final R4EReviewGroup modelGroup = ((R4EUIReviewGroup) fProperties.getElement()).getReviewGroup();
 						String newValue = fDescriptionText.getText().trim();
 						if (!newValue.equals(modelGroup.getDescription())) {
 							final Long bookNum = R4EUIModelController.FResourceUpdater.checkOut(modelGroup, currentUser);
 							modelGroup.setDescription(newValue);
 							R4EUIModelController.FResourceUpdater.checkIn(bookNum);
 						}
 						fDescriptionText.setText(newValue);
 					} catch (ResourceHandlingException e1) {
 						UIUtils.displayResourceErrorDialog(e1);
 					} catch (OutOfSyncException e1) {
 						UIUtils.displaySyncErrorDialog(e1);
 					}
 				}
 			}
 		});
 		UIUtils.addTabbedPropertiesTextResizeListener(fDescriptionText);
 
 		final CLabel descriptionLabel = widgetFactory.createCLabel(composite, R4EUIConstants.DESCRIPTION_LABEL);
 		data = new FormData();
 		data.left = new FormAttachment(0, 0);
 		data.right = new FormAttachment(fDescriptionText, -ITabbedPropertyConstants.HSPACE);
 		data.top = new FormAttachment(fDescriptionText, 0, SWT.CENTER);
 		descriptionLabel.setToolTipText(R4EUIConstants.REVIEW_GROUP_DESCRIPTION_TOOLTIP);
 		descriptionLabel.setLayoutData(data);
 
 		//Entry Criteria
 		fDefaultEntryCriteriaText = widgetFactory.createText(composite, "", SWT.MULTI);
 		data = new FormData();
 		data.left = new FormAttachment(0, R4EUIConstants.TABBED_PROPERTY_LABEL_WIDTH);
 		data.right = new FormAttachment(100, 0); // $codepro.audit.disable numericLiterals
 		data.top = new FormAttachment(fDescriptionText, ITabbedPropertyConstants.VSPACE);
 		fDefaultEntryCriteriaText.setToolTipText(R4EUIConstants.REVIEW_GROUP_ENTRY_CRITERIA_TOOLTIP);
 		fDefaultEntryCriteriaText.setLayoutData(data);
 		fDefaultEntryCriteriaText.addListener(SWT.FocusOut, new Listener() {
 			public void handleEvent(Event event) {
 				if (!fRefreshInProgress && fDefaultEntryCriteriaText.getForeground().equals(UIUtils.ENABLED_FONT_COLOR)) {
 					try {
 						final String currentUser = R4EUIModelController.getReviewer();
 						final R4EReviewGroup modelGroup = ((R4EUIReviewGroup) fProperties.getElement()).getReviewGroup();
 						String newValue = fDefaultEntryCriteriaText.getText().trim();
 						if (!newValue.equals(modelGroup.getDefaultEntryCriteria())) {
 							final Long bookNum = R4EUIModelController.FResourceUpdater.checkOut(modelGroup, currentUser);
 							modelGroup.setDefaultEntryCriteria(newValue);
 							R4EUIModelController.FResourceUpdater.checkIn(bookNum);
 						}
 						fDefaultEntryCriteriaText.setText(newValue);
 					} catch (ResourceHandlingException e1) {
 						UIUtils.displayResourceErrorDialog(e1);
 					} catch (OutOfSyncException e1) {
 						UIUtils.displaySyncErrorDialog(e1);
 					}
 				}
 			}
 		});
 		UIUtils.addTabbedPropertiesTextResizeListener(fDefaultEntryCriteriaText);
 
 		final CLabel entryCriteriaLabel = widgetFactory.createCLabel(composite,
 				R4EUIConstants.DEFAULT_ENTRY_CRITERIA_LABEL);
 		data = new FormData();
 		data.left = new FormAttachment(0, 0);
 		data.right = new FormAttachment(fDefaultEntryCriteriaText, -ITabbedPropertyConstants.HSPACE);
 		data.top = new FormAttachment(fDefaultEntryCriteriaText, 0, SWT.CENTER);
 		entryCriteriaLabel.setToolTipText(R4EUIConstants.REVIEW_GROUP_ENTRY_CRITERIA_TOOLTIP);
 		entryCriteriaLabel.setLayoutData(data);
 
 		createRuleSetSection(widgetFactory, composite, createProjectCompSection(widgetFactory, composite));
 	}
 
 	/**
 	 * Method createProjectCompSection.
 	 * 
 	 * @param aWidgetFactory
 	 *            TabbedPropertySheetWidgetFactory
 	 * @param aComposite
 	 *            Composite
 	 * @return Composite
 	 */
 	private Composite createProjectCompSection(TabbedPropertySheetWidgetFactory aWidgetFactory,
 			final Composite aComposite) {
 		//Project & Components section
 		final ExpandableComposite projCompSection = aWidgetFactory.createExpandableComposite(aComposite,
 				ExpandableComposite.TWISTIE);
 		final FormData data = new FormData();
 		data.left = new FormAttachment(0, 0);
 		data.right = new FormAttachment(100, 0); // $codepro.audit.disable numericLiterals
 		data.top = new FormAttachment(fDefaultEntryCriteriaText, ITabbedPropertyConstants.VSPACE);
 		projCompSection.setLayoutData(data);
 		projCompSection.setText(PROJ_COMP_SECTION_LABEL);
 		projCompSection.addExpansionListener(new ExpansionAdapter() {
 			@Override
 			public void expansionStateChanged(ExpansionEvent e) {
 				final ScrolledComposite scrolledParent = (ScrolledComposite) aComposite.getParent()
 						.getParent()
 						.getParent()
 						.getParent()
 						.getParent();
 				scrolledParent.setMinSize(aComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
 				scrolledParent.layout(true, true);
 			}
 		});
 		projCompSection.setLayout(new GridLayout(1, false));
 
 		final Composite projCompSectionClient = aWidgetFactory.createComposite(projCompSection);
 		projCompSectionClient.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
 		projCompSectionClient.setLayout(new GridLayout(4, false));
 		projCompSection.setClient(projCompSectionClient);
 
 		//Projects
 		final CLabel projectsLabel = aWidgetFactory.createCLabel(projCompSectionClient,
 				R4EUIConstants.AVAILABLE_PROJECTS_LABEL);
 		GridData gridData = new GridData(SWT.FILL, SWT.CENTER, false, false);
 		gridData.horizontalSpan = 1;
 		projectsLabel.setToolTipText(R4EUIConstants.REVIEW_GROUP_PROJECTS_TOOLTIP);
 		projectsLabel.setLayoutData(gridData);
 
 		gridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
 		gridData.horizontalSpan = 3;
 		fAvailableProjects = new EditableListWidget(aWidgetFactory, projCompSectionClient, gridData, this, 1,
 				Text.class, null);
 		fAvailableProjects.setToolTipText(R4EUIConstants.REVIEW_GROUP_PROJECTS_TOOLTIP);
 
 		//Components
 		final CLabel componentsLabel = aWidgetFactory.createCLabel(projCompSectionClient,
 				R4EUIConstants.AVAILABLE_COMPONENTS_LABEL);
 		gridData = new GridData(SWT.FILL, SWT.CENTER, false, false);
 		gridData.horizontalSpan = 1;
 		componentsLabel.setToolTipText(R4EUIConstants.REVIEW_GROUP_COMPONENTS_TOOLTIP);
 		componentsLabel.setLayoutData(gridData);
 
 		gridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
 		gridData.horizontalSpan = 3;
 		fAvailableComponents = new EditableListWidget(aWidgetFactory, projCompSectionClient, gridData, this, 2,
 				Text.class, null);
 		fAvailableComponents.setToolTipText(R4EUIConstants.REVIEW_GROUP_COMPONENTS_TOOLTIP);
 		return projCompSection;
 	}
 
 	/**
 	 * Method createRuleSetSection.
 	 * 
 	 * @param aWidgetFactory
 	 *            TabbedPropertySheetWidgetFactory
 	 * @param aComposite
 	 *            Composite
 	 * @param aTopComposite
 	 *            Composite
 	 */
 	private void createRuleSetSection(TabbedPropertySheetWidgetFactory aWidgetFactory, final Composite aComposite,
 			final Composite aTopComposite) {
 		//Rule Sets section
 		final ExpandableComposite ruleSetsSection = aWidgetFactory.createExpandableComposite(aComposite,
 				ExpandableComposite.TWISTIE);
 		final FormData data = new FormData();
 		data.left = new FormAttachment(0, 0);
 		data.right = new FormAttachment(100, 0); // $codepro.audit.disable numericLiterals
 		data.top = new FormAttachment(aTopComposite, ITabbedPropertyConstants.VSPACE);
 		ruleSetsSection.setLayoutData(data);
 		ruleSetsSection.setText(RULE_SETS_SECTION_LABEL);
 		ruleSetsSection.addExpansionListener(new ExpansionAdapter() {
 			@Override
 			public void expansionStateChanged(ExpansionEvent e) {
 				final ScrolledComposite scrolledParent = (ScrolledComposite) aComposite.getParent()
 						.getParent()
 						.getParent()
 						.getParent()
 						.getParent();
 				scrolledParent.setMinSize(aComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
 				scrolledParent.layout(true, true);
 			}
 		});
 		ruleSetsSection.setLayout(new GridLayout(1, false));
 
 		final Composite ruleSetSectionClient = aWidgetFactory.createComposite(ruleSetsSection);
 		ruleSetSectionClient.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
 		ruleSetSectionClient.setLayout(new GridLayout(4, false));
 		ruleSetsSection.setClient(ruleSetSectionClient);
 
 		//Rule Sets
 		final CLabel ruleSetsLabel = aWidgetFactory.createCLabel(ruleSetSectionClient, R4EUIConstants.RULE_SETS_LABEL);
 		GridData gridData = new GridData(SWT.FILL, SWT.CENTER, false, false);
 		gridData.horizontalSpan = 1;
 		ruleSetsLabel.setToolTipText(R4EUIConstants.REVIEW_GROUP_RULESET_REFERENCE_TOOLTIP);
 		ruleSetsLabel.setLayoutData(gridData);
 
 		gridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
 		gridData.horizontalSpan = 3;
 		fRuleSetLocations = new EditableListWidget(aWidgetFactory, ruleSetSectionClient, gridData, this, 3,
 				CCombo.class, null);
 		fRuleSetLocations.setToolTipText(R4EUIConstants.REVIEW_GROUP_RULESET_REFERENCE_TOOLTIP);
 	}
 
 	/**
 	 * Method refresh.
 	 * 
 	 * @see org.eclipse.ui.views.properties.tabbed.ISection#refresh()
 	 */
 	@Override
 	public void refresh() {
 		fRefreshInProgress = true;
 		final R4EReviewGroup modelGroup = ((R4EUIReviewGroup) fProperties.getElement()).getReviewGroup();
 		fNameText.setText(modelGroup.getName());
 		if (null != ((R4EUIReviewGroup) fProperties.getElement()).getReviewGroup().eResource()) {
 			fFilePathText.setText(((R4EUIReviewGroup) fProperties.getElement()).getReviewGroup()
 					.eResource()
 					.getURI()
 					.toFileString());
 		}
 		if (null != modelGroup.getDescription()) {
 			fDescriptionText.setText(modelGroup.getDescription());
 		} else {
 			fDescriptionText.setText("");
 		}
 
 		EList<String> availableProjects = modelGroup.getAvailableProjects();
 		final String[] projects = availableProjects.toArray(new String[availableProjects.size()]);
 		fAvailableProjects.removeAll();
 		Item item = null;
 		String project = null;
 
 		for (int i = 0; i < projects.length; i++) {
 			project = projects[i];
 			if (i >= fAvailableProjects.getItemCount()) {
 				item = fAvailableProjects.addItem();
 			} else {
 				item = fAvailableProjects.getItem(i);
 				if (null == item) {
 					item = fAvailableProjects.addItem();
 				}
 			}
 			item.setText(project);
 		}
 		fAvailableProjects.updateButtons();
 
 		EList<String> availableComponents = modelGroup.getAvailableComponents();
 		final String[] components = availableComponents.toArray(new String[availableComponents.size()]);
 		fAvailableComponents.removeAll();
 		String component = null;
 		for (int i = 0; i < components.length; i++) {
 			component = components[i];
 			if (i >= fAvailableComponents.getItemCount()) {
 				item = fAvailableComponents.addItem();
 			} else {
 				item = fAvailableComponents.getItem(i);
 				if (null == item) {
 					item = fAvailableComponents.addItem();
 				}
 			}
 			item.setText(component);
 		}
 		fAvailableComponents.updateButtons();
 
 		fDefaultEntryCriteriaText.setText(modelGroup.getDefaultEntryCriteria());
 
 		final List<R4EUIRuleSet> uiRuleSets = ((R4EUIRootElement) ((R4EUIReviewGroup) fProperties.getElement()).getParent()).getRuleSets();
 		final List<String> tmpRuleSetLocations = new ArrayList<String>();
		final String[] ruleSetsLocations = (String[]) modelGroup.getDesignRuleLocations().toArray();
 		for (R4EUIRuleSet uiRuleSet : uiRuleSets) {
 			if (uiRuleSet.isEnabled()) {
 				tmpRuleSetLocations.add(uiRuleSet.getName());
 			}
 		}
 		fRuleSetLocations.setEditableValues(tmpRuleSetLocations.toArray(new String[tmpRuleSetLocations.size()]));
 		fRuleSetLocations.removeAll();
 		item = null;
 		String ruleSet = null;
 		for (int i = 0; i < ruleSetsLocations.length; i++) {
 			ruleSet = ruleSetsLocations[i];
 			if (i >= fRuleSetLocations.getItemCount()) {
 				item = fRuleSetLocations.addItem();
 			} else {
 				item = fRuleSetLocations.getItem(i);
 				if (null == item) {
 					item = fRuleSetLocations.addItem();
 				}
 			}
 			item.setText(ruleSet);
 
 			//Decorate rule set item if it is not loaded
 			item.setImage(null);
 			List<R4EUIRuleSet> loadedRuleSets = R4EUIModelController.getRootElement().getRuleSets();
 			for (R4EUIRuleSet loadedRuleSet : loadedRuleSets) {
 				if (ruleSet.equals(loadedRuleSet.getName())) {
 					item.setImage(fProperties.getElement().getUserReviewedImage());
 				}
 			}
 			if (null == item.getImage()) {
 				item.setImage(fProperties.getElement().getDisabledImage());
 			}
 		}
 
 		setEnabledFields();
 		fRefreshInProgress = false;
 
 		//Used only in test mode
 		R4EUIModelController.setCurrentPropertySection(this);
 	}
 
 	/**
 	 * Method setEnabledFields.
 	 */
 	@Override
 	protected void setEnabledFields() {
 		if (R4EUIModelController.isJobInProgress() || !((R4EUIReviewGroup) fProperties.getElement()).isOpen()
 				|| !fProperties.getElement().isEnabled() || fProperties.getElement().isReadOnly()) {
 			fNameText.setForeground(UIUtils.DISABLED_FONT_COLOR);
 			fFilePathText.setForeground(UIUtils.DISABLED_FONT_COLOR);
 			fDescriptionText.setForeground(UIUtils.DISABLED_FONT_COLOR);
 			fDescriptionText.setEditable(false);
 			fDefaultEntryCriteriaText.setForeground(UIUtils.DISABLED_FONT_COLOR);
 			fDefaultEntryCriteriaText.setEditable(false);
 			fAvailableProjects.setEnabled(false);
 			fAvailableComponents.setEnabled(false);
 			fRuleSetLocations.setEnabled(false);
 		} else {
 			fNameText.setForeground(UIUtils.ENABLED_FONT_COLOR);
 			fFilePathText.setForeground(UIUtils.ENABLED_FONT_COLOR);
 			fDescriptionText.setForeground(UIUtils.ENABLED_FONT_COLOR);
 			fDescriptionText.setEditable(true);
 			fDefaultEntryCriteriaText.setForeground(UIUtils.ENABLED_FONT_COLOR);
 			fDefaultEntryCriteriaText.setEditable(true);
 			fAvailableProjects.setEnabled(true);
 			fAvailableComponents.setEnabled(true);
 			fRuleSetLocations.setEnabled(true);
 		}
 	}
 
 	/**
 	 * Method itemsUpdated.
 	 * 
 	 * @param aItems
 	 *            Item[]
 	 * @param aInstanceId
 	 *            int
 	 * @see org.eclipse.ui.utils.IEditableListListener#itemsUpdated(Item[] aItems)
 	 */
 	public void itemsUpdated(Item[] aItems, int aInstanceId) {
 		// Update the core model data with new data
 		try {
 			if (!fRefreshInProgress) {
 				final String currentUser = R4EUIModelController.getReviewer();
 				final R4EReviewGroup modelGroup = ((R4EUIReviewGroup) fProperties.getElement()).getReviewGroup();
 				if (1 == aInstanceId) {
 					//First widget: available projects
 					List<String> newAddProjects = new ArrayList<String>();
 					List<String> newDeleteProjects = new ArrayList<String>();
 					List<String> storedProjects = new ArrayList<String>();
 					storedProjects.addAll(modelGroup.getAvailableProjects());
 					for (Item item : aItems) {
 						newAddProjects.add(item.getText());
 						newDeleteProjects.add(item.getText());
 					}
 
 					//Add all new elements
 					newAddProjects.removeAll(storedProjects);
 					if (newAddProjects.size() > 0) {
 						final Long bookNum = R4EUIModelController.FResourceUpdater.checkOut(modelGroup, currentUser);
 						modelGroup.getAvailableProjects().addAll(newAddProjects);
 						R4EUIModelController.FResourceUpdater.checkIn(bookNum);
 					}
 
 					//Delete old elements to remove
 					storedProjects.removeAll(newDeleteProjects);
 					if (storedProjects.size() > 0) {
 						Long bookNum = R4EUIModelController.FResourceUpdater.checkOut(modelGroup, currentUser);
 						modelGroup.getAvailableProjects().removeAll(storedProjects);
 						R4EUIModelController.FResourceUpdater.checkIn(bookNum);
 					}
 				} else if (2 == aInstanceId) {
 					//Second widget: available components
 					List<String> newAddComponents = new ArrayList<String>();
 					List<String> newDeleteComponents = new ArrayList<String>();
 					List<String> storedComponents = new ArrayList<String>();
 					storedComponents.addAll(modelGroup.getAvailableComponents());
 					for (Item item : aItems) {
 						newAddComponents.add(item.getText());
 						newDeleteComponents.add(item.getText());
 					}
 
 					//Add all new elements
 					newAddComponents.removeAll(storedComponents);
 					if (newAddComponents.size() > 0) {
 						final Long bookNum = R4EUIModelController.FResourceUpdater.checkOut(modelGroup, currentUser);
 						modelGroup.getAvailableComponents().addAll(newAddComponents);
 						R4EUIModelController.FResourceUpdater.checkIn(bookNum);
 					}
 
 					//Delete old elements to remove
 					storedComponents.removeAll(newDeleteComponents);
 					if (storedComponents.size() > 0) {
 						Long bookNum = R4EUIModelController.FResourceUpdater.checkOut(modelGroup, currentUser);
 						modelGroup.getAvailableComponents().removeAll(storedComponents);
 						R4EUIModelController.FResourceUpdater.checkIn(bookNum);
 					}
 				} else if (3 == aInstanceId) {
 					//Third widget: applied Rule Sets
 					List<String> newAddRuleSets = new ArrayList<String>();
 					List<String> newDeleteRuleSets = new ArrayList<String>();
 					List<String> storedRuleSets = new ArrayList<String>();
 					storedRuleSets.addAll(modelGroup.getDesignRuleLocations());
 					for (Item item : aItems) {
 						newAddRuleSets.add(item.getText());
 						newDeleteRuleSets.add(item.getText());
 					}
 
 					//Add all new elements
 					newAddRuleSets.removeAll(storedRuleSets);
 					if (newAddRuleSets.size() > 0) {
 						final Long bookNum = R4EUIModelController.FResourceUpdater.checkOut(modelGroup, currentUser);
 						modelGroup.getDesignRuleLocations().addAll(newAddRuleSets);
 						R4EUIModelController.FResourceUpdater.checkIn(bookNum);
 					}
 
 					//Delete old elements to remove
 					storedRuleSets.removeAll(newDeleteRuleSets);
 					if (storedRuleSets.size() > 0) {
 						Long bookNum = R4EUIModelController.FResourceUpdater.checkOut(modelGroup, currentUser);
 						modelGroup.getDesignRuleLocations().removeAll(storedRuleSets);
 						R4EUIModelController.FResourceUpdater.checkIn(bookNum);
 					}
 
 					//Update references in R4EUIReviewGroup
 					((R4EUIReviewGroup) fProperties.getElement()).getRuleSets().clear();
 					for (Item item : aItems) {
 						for (R4EUIRuleSet ruleSet : ((R4EUIRootElement) ((R4EUIReviewGroup) fProperties.getElement()).getParent()).getRuleSets()) {
 							if (ruleSet.getName().equals(item.getText())) {
 								((R4EUIReviewGroup) fProperties.getElement()).getRuleSets().add(ruleSet);
 								break;
 							}
 						}
 					}
 				}
 				refresh();
 			}
 
 		} catch (ResourceHandlingException e1) {
 			UIUtils.displayResourceErrorDialog(e1);
 		} catch (OutOfSyncException e1) {
 			UIUtils.displaySyncErrorDialog(e1);
 		}
 	}
 
 	//Getters and Setters.  These are used in JUnit testing and could
 	//	also be used in headless mode
 
 	/**
 	 * Method setDescription.
 	 * 
 	 * @param aDescription
 	 *            String
 	 */
 	public void setDescription(String aDescription) {
 		refresh();
 		fDescriptionText.setFocus();
 		fDescriptionText.setText(aDescription);
 		fDefaultEntryCriteriaText.setFocus(); //Set focus away to register change
 	}
 
 	/**
 	 * Method setDefaultEntryCriteria.
 	 * 
 	 * @param aCriteria
 	 *            String
 	 */
 	public void setDefaultEntryCriteria(String aCriteria) {
 		refresh();
 		fDefaultEntryCriteriaText.setFocus();
 		fDefaultEntryCriteriaText.setText(aCriteria);
 		fDescriptionText.setFocus(); //Set focus away to register change
 	}
 
 	/**
 	 * Method removeAvailableProject.
 	 * 
 	 * @param aProject
 	 *            String
 	 */
 	public void removeAvailableProject(String aProject) {
 		refresh();
 		fAvailableProjects.remove(aProject);
 		fDescriptionText.setFocus(); //Set focus away to register change
 	}
 
 	/**
 	 * Method addAvailableProject.
 	 * 
 	 * @param aProject
 	 *            String
 	 */
 	public void addAvailableProject(String aProject) {
 		refresh(); //This is needed for JUnit tests
 		fAvailableProjects.add(aProject);
 		fDescriptionText.setFocus(); //Set focus away to register change
 	}
 
 	/**
 	 * Method removeAvailableComponent.
 	 * 
 	 * @param aComponent
 	 *            String
 	 */
 	public void removeAvailableComponent(String aComponent) {
 		refresh();
 		fAvailableComponents.remove(aComponent);
 		fDescriptionText.setFocus(); //Set focus away to register change
 	}
 
 	/**
 	 * Method addAvailableComponent.
 	 * 
 	 * @param aComponent
 	 *            String
 	 */
 	public void addAvailableComponent(String aComponent) {
 		refresh();
 		fAvailableComponents.add(aComponent);
 		fDescriptionText.setFocus(); //Set focus away to register change
 	}
 
 	/**
 	 * Method removeAvailableComponent.
 	 * 
 	 * @param aRuleSet
 	 *            String
 	 */
 	public void removeRuleSet(String aRuleSet) {
 		refresh();
 		fRuleSetLocations.remove(aRuleSet);
 		fDescriptionText.setFocus(); //Set focus away to register change
 	}
 
 	/**
 	 * Method aRuleSet.
 	 * 
 	 * @param aRuleSet
 	 *            String
 	 */
 	public void addRuleSet(String aRuleSet) {
 		refresh();
 		fRuleSetLocations.add(aRuleSet);
 		fDescriptionText.setFocus(); //Set focus away to register change
 	}
 
 	/**
 	 * Method itemSelected.
 	 * 
 	 * @param aItem
 	 *            Item
 	 * @param aInstanceId
 	 *            int
 	 * @see org.eclipse.mylyn.reviews.r4e.ui.internal.utils.IEditableListListener#itemSelected(Item, int)
 	 */
 	public void itemSelected(Item aItem, int aInstanceId) {
 		// ignore
 
 	}
 }
