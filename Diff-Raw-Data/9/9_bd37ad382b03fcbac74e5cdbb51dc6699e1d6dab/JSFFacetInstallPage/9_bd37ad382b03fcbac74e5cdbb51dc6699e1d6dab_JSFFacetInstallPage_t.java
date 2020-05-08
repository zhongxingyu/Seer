 /*******************************************************************************
  * Copyright (c) 2005 Oracle Corporation.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Gerry Kessler - initial API and implementation
  *    Debajit Adhikary - Fixes for bug 255097 ("Request to remove input fields 
  *                       from facet install page")
  *******************************************************************************/
 package org.eclipse.jst.jsf.ui.internal.project.facet;
 
 import java.util.Iterator;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.jface.dialogs.DialogSettings;
 import org.eclipse.jface.dialogs.IDialogSettings;
 import org.eclipse.jface.dialogs.IInputValidator;
 import org.eclipse.jface.dialogs.InputDialog;
 import org.eclipse.jface.window.Window;
 import org.eclipse.jst.common.project.facet.core.libprov.LibraryInstallDelegate;
 import org.eclipse.jst.common.project.facet.ui.libprov.LibraryProviderFrameworkUi;
 import org.eclipse.jst.j2ee.project.facet.IJ2EEModuleFacetInstallDataModelProperties;
 import org.eclipse.jst.jsf.core.internal.project.facet.IJSFFacetInstallDataModelProperties;
 import org.eclipse.jst.jsf.core.internal.project.facet.JsfFacetConfigurationUtil;
 import org.eclipse.jst.jsf.ui.internal.JSFUiPlugin;
 import org.eclipse.jst.jsf.ui.internal.Messages;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.List;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.wst.common.frameworks.datamodel.AbstractDataModelProvider;
 import org.eclipse.wst.common.frameworks.datamodel.DataModelEvent;
 import org.eclipse.wst.common.frameworks.datamodel.DataModelFactory;
 import org.eclipse.wst.common.frameworks.datamodel.IDataModel;
 import org.eclipse.wst.common.frameworks.internal.datamodel.ui.DataModelWizardPage;
 import org.eclipse.wst.common.project.facet.core.IFacetedProject;
 import org.eclipse.wst.common.project.facet.core.IProjectFacetVersion;
 import org.eclipse.wst.common.project.facet.ui.IFacetWizardPage;
 import org.eclipse.wst.common.project.facet.ui.IWizardContext;
 
 /**
  * JSF Facet installation wizard page.
  * 
  * @author Gerry Kessler - Oracle
  */
 public class JSFFacetInstallPage extends DataModelWizardPage implements
 		IJSFFacetInstallDataModelProperties, IFacetWizardPage {
     
     private final boolean jsfFacetConfigurationEnabled = JsfFacetConfigurationUtil.isJsfFacetConfigurationEnabled();
     
 	// UI
     private Button btnConfigureServlet;
     private Composite configureServletComposite;
 	private Label lblJSFConfig;
 	private Text txtJSFConfig;
 	private Label lblJSFServletName;
 	private Text txtJSFServletName;
 	private Label lblJSFServletClassName;
 	private Text txtJSFServletClassName;	
 	private Label lblJSFServletURLPatterns;
 	private List lstJSFServletURLPatterns;
 	private Button btnAddPattern;
 	private Button btnRemovePattern;
 
 	private IDialogSettings dialogSettings;
 	private IDataModel webAppDataModel;
 	private static final String SETTINGS_ROOT = JSFUiPlugin.PLUGIN_ID
 			+ ".jsfFacetInstall"; //$NON-NLS-1$
 	private static final String SETTINGS_CONFIG = "configPath"; //$NON-NLS-1$
 	private static final String SETTINGS_SERVLET = "servletName"; //$NON-NLS-1$
 	private static final String SETTINGS_SERVLET_CLASSNAME = "servletClassname"; //$NON-NLS-1$
 	private static final String SETTINGS_URL_MAPPINGS = "urlMappings"; //$NON-NLS-1$
 	private static final String SETTINGS_URL_PATTERN = "pattern"; //$NON-NLS-1$
 	private static final String SETTINGS_CONFIGURE_SERVLET = "configureServlet"; //$NON-NLS-1$
 	
 	// private String projectName = null;
 	private Composite composite = null;
 
 	/**
 	 * Zero argument constructor
 	 */
 	public JSFFacetInstallPage() {
 		// FIXME: following WebFacetInstallPage pattern which will be fixed at somepoint
 		super(DataModelFactory.createDataModel(new AbstractDataModelProvider() {/*
 																				 * do
 																				 * nothing
 																				 */
 		}), "jsf.facet.install.page"); //$NON-NLS-1$
 		setTitle(Messages.JSFFacetInstallPage_title);
 		setDescription(Messages.JSFFacetInstallPage_description);
 		dialogSettings = JSFUiPlugin.getDefault().getDialogSettings();
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.wst.common.frameworks.internal.datamodel.ui.DataModelWizardPage#createTopLevelComposite(org.eclipse.swt.widgets.Composite)
 	 */
 	protected Composite createTopLevelComposite(final Composite parent) {
 		initializeDialogUnits(parent);
 		composite = new Composite(parent, SWT.NONE);
 		final GridLayout jsfCompositeLayout = new GridLayout(3, false);
 		jsfCompositeLayout.marginTop = 0;
 		jsfCompositeLayout.marginBottom = 0;
 		jsfCompositeLayout.marginRight = 0;
 		jsfCompositeLayout.marginLeft = 0;
 		composite.setLayout(jsfCompositeLayout);
 		
 		final LibraryInstallDelegate librariesInstallDelegate
 		    = (LibraryInstallDelegate) getDataModel().getProperty( LIBRARY_PROVIDER_DELEGATE );
 		
 		final Control librariesComposite
 		    = LibraryProviderFrameworkUi.createInstallLibraryPanel( composite, librariesInstallDelegate,
 		                                                            Messages.JSFFacetInstallPage_JSFImplementationLibrariesFrame );
 		
 		GridData gd = new GridData( GridData.FILL_HORIZONTAL );
 		gd.horizontalSpan = 3;
 		
 		librariesComposite.setLayoutData( gd );
 		
 		final Label spacer = new Label( composite, SWT.NONE );
 		spacer.setText( "" ); //$NON-NLS-1$
 
         gd = new GridData( GridData.FILL_HORIZONTAL );
         gd.horizontalSpan = 3;
         
 		spacer.setLayoutData( gd );
 
         if (jsfFacetConfigurationEnabled)
         {
         	btnConfigureServlet = new Button(composite, SWT.CHECK);
         	btnConfigureServlet.setText(Messages.JSFFacetInstallPage_ConfigureServletLabel);
         	GridData gd0 = new GridData(GridData.FILL_HORIZONTAL);
         	gd0.horizontalSpan = 3;
         	btnConfigureServlet.setLayoutData(gd0);
         	btnConfigureServlet.addSelectionListener(new SelectionAdapter() {
         		public void widgetSelected(SelectionEvent se) {
         			setCompositeEnabled(configureServletComposite, btnConfigureServlet.getSelection());
         		}
         	});
 
         	configureServletComposite = new Composite(composite, SWT.NONE);
     		final GridLayout csLayout = new GridLayout(3, false);
     		csLayout.marginTop = 0;
     		csLayout.marginBottom = 0;
     		csLayout.marginRight = 0;
     		csLayout.marginLeft = 5;
     		configureServletComposite.setLayout(csLayout);
         	GridData csGridData = new GridData(GridData.FILL_HORIZONTAL);
         	csGridData.horizontalSpan = 3;
         	configureServletComposite.setLayoutData(csGridData);
 
     		lblJSFConfig = new Label(configureServletComposite, SWT.NONE);
     		lblJSFConfig.setText(Messages.JSFFacetInstallPage_JSFConfigLabel);
     		lblJSFConfig.setLayoutData(new GridData(GridData.BEGINNING));
     
     		txtJSFConfig = new Text(configureServletComposite, SWT.BORDER);
     		GridData gd1 = new GridData(GridData.FILL_HORIZONTAL);
     		gd1.horizontalSpan = 2;
     		txtJSFConfig.setLayoutData(gd1);
     
     		lblJSFServletName = new Label(configureServletComposite, SWT.NONE);
     		lblJSFServletName
     				.setText(Messages.JSFFacetInstallPage_JSFServletNameLabel);
     		lblJSFServletName.setLayoutData(new GridData(GridData.BEGINNING));
     
     		txtJSFServletName = new Text(configureServletComposite, SWT.BORDER);
     		GridData gd2 = new GridData(GridData.FILL_HORIZONTAL);
     		gd2.horizontalSpan = 2;
     		txtJSFServletName.setLayoutData(gd2);
     
     		lblJSFServletClassName = new Label(configureServletComposite, SWT.NONE);
     		lblJSFServletClassName
     				.setText(Messages.JSFFacetInstallPage_JSFServletClassNameLabel);
     		lblJSFServletClassName.setLayoutData(new GridData(GridData.BEGINNING));
     
     		txtJSFServletClassName = new Text(configureServletComposite, SWT.BORDER);
     		GridData gd2c = new GridData(GridData.FILL_HORIZONTAL);
     		gd2c.horizontalSpan = 2;
     		txtJSFServletClassName.setLayoutData(gd2c);
     		
     		lblJSFServletURLPatterns = new Label(configureServletComposite, SWT.NULL);
     		lblJSFServletURLPatterns
     				.setText(Messages.JSFFacetInstallPage_JSFURLMappingLabel);
     		lblJSFServletURLPatterns.setLayoutData(new GridData(GridData.BEGINNING
     				| GridData.VERTICAL_ALIGN_BEGINNING));
     		lstJSFServletURLPatterns = new List(configureServletComposite, SWT.BORDER);
     		GridData gd3 = new GridData(GridData.FILL_HORIZONTAL);
     		gd3.heightHint = convertHeightInCharsToPixels(5);
     		lstJSFServletURLPatterns.setLayoutData(gd3);
     		lstJSFServletURLPatterns.addSelectionListener(new SelectionAdapter() {
     			public void widgetSelected(SelectionEvent e) {
     				btnRemovePattern.setEnabled(lstJSFServletURLPatterns
     						.getSelectionCount() > 0);
     			}
     		});
     
     		Composite btnComposite = new Composite(configureServletComposite, SWT.NONE);
     		GridLayout gl = new GridLayout(1, false);
     		// gl.marginBottom = 0;
     		// gl.marginTop = 0;
     		// gl.marginRight = 0;
     		gl.marginLeft = 0;
     		btnComposite.setLayout(gl);
     		btnComposite.setLayoutData(new GridData(GridData.END
     				| GridData.VERTICAL_ALIGN_FILL));
     
     		btnAddPattern = new Button(btnComposite, SWT.NONE);
     		btnAddPattern.setText(Messages.JSFFacetInstallPage_Add2);
     		btnAddPattern.setLayoutData(new GridData(GridData.FILL_HORIZONTAL
     				| GridData.VERTICAL_ALIGN_BEGINNING));
     		btnAddPattern.addSelectionListener(new SelectionAdapter() {
     			public void widgetSelected(SelectionEvent e) {
     				InputDialog dialog = new InputDialog(getShell(),
     						Messages.JSFFacetInstallPage_PatternDialogTitle,
     						Messages.JSFFacetInstallPage_PatternDialogDesc, null,
     						new IInputValidator() {
     
     							public String isValid(String newText) {
     								return isValidPattern(newText);
     							}
     
     						});
     				dialog.open();
     				if (dialog.getReturnCode() == Window.OK) {
     					addItemToList(dialog.getValue(), true);
     				}
     			}
     		});
     
     		btnRemovePattern = new Button(btnComposite, SWT.NONE);
     		btnRemovePattern.setText(Messages.JSFFacetInstallPage_Remove);
     		btnRemovePattern.setLayoutData(new GridData(GridData.FILL_HORIZONTAL
     				| GridData.VERTICAL_ALIGN_BEGINNING));
     		btnRemovePattern.setEnabled(false);
     		btnRemovePattern.addSelectionListener(new SelectionAdapter() {
     			public void widgetSelected(SelectionEvent e) {
     				removeItemFromList(lstJSFServletURLPatterns.getSelection());
     				btnRemovePattern.setEnabled(false);
     			}
     		});
     
     		addModificationListeners();
         }
 		
 		return composite;
 	}
 
 	private void setCompositeEnabled(Composite composite, boolean enabled) {
 		for (Control child: composite.getChildren()) {
 			child.setEnabled(enabled);
 			if (child instanceof Composite) {
 				setCompositeEnabled((Composite)child, enabled);
 			}
 		}
 	}
 
 	private void initializeValues() {
 		IDialogSettings root = dialogSettings.getSection(SETTINGS_ROOT);
 
 		initJSFCfgCtrlValues(root);
 
 		String conf = null;
 		if (root != null)
 			conf = root.get(SETTINGS_CONFIG);
 		if (conf == null || conf.equals("")) { //$NON-NLS-1$
 			conf = (String) model
 					.getDefaultProperty(IJSFFacetInstallDataModelProperties.CONFIG_PATH);
 		}
 		txtJSFConfig.setText(conf);
 
 		String servletName = null;
 		if (root != null)
 			servletName = root.get(SETTINGS_SERVLET);
 		if (servletName == null || servletName.equals("")) { //$NON-NLS-1$
 			servletName = (String) model
 					.getDefaultProperty(IJSFFacetInstallDataModelProperties.SERVLET_NAME);
 		}
 		txtJSFServletName.setText(servletName);
 
 		String servletClassname = null;
 		if (root != null)
 			servletClassname = root.get(SETTINGS_SERVLET_CLASSNAME);
 		if (servletClassname == null || servletClassname.equals("")) { //$NON-NLS-1$
 			servletClassname = (String) model
 					.getDefaultProperty(IJSFFacetInstallDataModelProperties.SERVLET_CLASSNAME);
 		}
 		txtJSFServletClassName.setText(servletClassname);
 
 		loadURLMappingPatterns(root);
 
 		String configureServlet = Boolean.TRUE.toString();
 		if (root != null) {
 			configureServlet = root.get(SETTINGS_CONFIGURE_SERVLET);
 			if (configureServlet == null || configureServlet.equals("")) { //$NON-NLS-1$
 				configureServlet = ((Boolean) model.getDefaultProperty(CONFIGURE_SERVLET)).toString();
 			}
 		}
 		boolean enabled = Boolean.parseBoolean(configureServlet);
 		btnConfigureServlet.setSelection(enabled);
 		setCompositeEnabled(configureServletComposite, enabled);
 	}
 
 	private void initJSFCfgCtrlValues(IDialogSettings root) {
 		/*IDialogSettings complibs = null;
 		if (root != null) {
 			complibs = root.getSection(SETTINGS_COMPLIB);
 		}
 
 		String[] selection = null;
 		if (complibs != null) {
 			selection = complibs.getArray(SETTINGS_COMPLIB_SELECT_DEPLOY);
 		}
 
 		JSFLibraryConfigDialogSettingData source = new JSFLibraryConfigDialogSettingData(selection);
 		jsfLibCfgComp.loadControlValuesFromModel(source);*/
 	}
 
 	
 	private void saveSettings() {
 		DialogSettings root = new DialogSettings(SETTINGS_ROOT);
 		dialogSettings.addSection(root);
 
 		root.put(SETTINGS_CONFIGURE_SERVLET, getConfigureServlet());
 		root.put(SETTINGS_CONFIG, getJSFConfig());
 		root.put(SETTINGS_SERVLET, getJSFServletName());
 		root.put(SETTINGS_SERVLET_CLASSNAME, getJSFServletClassname());
 		DialogSettings mappings = new DialogSettings(SETTINGS_URL_MAPPINGS);
 		root.addSection(mappings);
 		mappings.put(SETTINGS_URL_PATTERN, getJSFPatterns());
 	}
 
 	private String getJSFConfig() {
 		return txtJSFConfig.getText().trim();
 	}
 
 	private String getJSFServletName() {
 		return txtJSFServletName.getText().trim();
 	}
 
 	private String getJSFServletClassname() {
 		return txtJSFServletClassName.getText().trim();
 	}
 	
 	private String[] getJSFPatterns() {
 		return lstJSFServletURLPatterns.getItems();
 	}
 
 	private boolean getConfigureServlet() {
 		return btnConfigureServlet.getSelection();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.wst.common.project.facet.ui.IFacetWizardPage#setConfig(java.lang.Object)
 	 */
 	public void setConfig(Object config) {
 		model.removeListener(this);
 		synchHelper.dispose();
 
 		model = (IDataModel) config;
 		model.addListener(this);
 		synchHelper = initializeSynchHelper(model);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.wst.common.project.facet.ui.IFacetWizardPage#transferStateToConfig()
 	 */
 	public void transferStateToConfig() {
 	    if (jsfFacetConfigurationEnabled)
 	    {
     		saveSettings(); // convenient place for this. don't want to save if user
     						// cancelled.
 	    }
 	}
 
 	private void addModificationListeners() {
 		 synchHelper.synchCheckbox(btnConfigureServlet, CONFIGURE_SERVLET, null); 
 		 synchHelper.synchText(txtJSFConfig, CONFIG_PATH, null);
 		 synchHelper.synchText(txtJSFServletName, SERVLET_NAME, null);
 		 synchHelper.synchText(txtJSFServletClassName, SERVLET_CLASSNAME, null);
 		 synchHelper.synchList(lstJSFServletURLPatterns, SERVLET_URL_PATTERNS, null);
 	}
 
 	private String isValidPattern(String value) {
 		if (value == null || value.trim().equals("")) //$NON-NLS-1$
 			return Messages.JSFFacetInstallPage_PatternEmptyMsg;
 		if (lstJSFServletURLPatterns.indexOf(value) >= 0)
 			return Messages.JSFFacetInstallPage_PatternSpecifiedMsg;
 
 		return null;
 	}
 
 	private void loadURLMappingPatterns(IDialogSettings root) {
 		lstJSFServletURLPatterns.removeAll();
 		IDialogSettings mappings = null;
 		if (root != null)
 			mappings = root.getSection(SETTINGS_URL_MAPPINGS);
 		String[] patterns = null;
 		if (mappings != null)
 			patterns = mappings.getArray(SETTINGS_URL_PATTERN);
 
 		if (patterns == null || patterns.length == 0) {
 			patterns = (String[]) model
 					.getDefaultProperty(IJSFFacetInstallDataModelProperties.SERVLET_URL_PATTERNS);
 		}
 		for (int i = 0; i < patterns.length; i++) {
 			addItemToList(patterns[i], false);
 		}
 	}
 
 	private void addItemToList(String pattern, boolean selectMe) {
 		lstJSFServletURLPatterns.add(pattern == null ? "" : pattern); //$NON-NLS-1$
 		if (pattern == null && selectMe)
 			lstJSFServletURLPatterns.setSelection(lstJSFServletURLPatterns
 					.getItemCount() - 1);
 		// When 119321 is fixed - remove code below
 		updateModelForURLPattern();
 	}
 
 	private void removeItemFromList(String[] selection) {
 		for (int i = 0; i < selection.length; i++) {
 			String sel = selection[i];
 			lstJSFServletURLPatterns.remove(sel);
 		}
 		// When 119321 is fixed - remove code below
 		updateModelForURLPattern();
 	}
 
 	private void updateModelForURLPattern() {
 		model.setProperty(
 				IJSFFacetInstallDataModelProperties.SERVLET_URL_PATTERNS,
 				lstJSFServletURLPatterns.getItems());
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.wst.common.frameworks.internal.datamodel.ui.DataModelWizardPage#getValidationPropertyNames()
 	 */
 	protected String[] getValidationPropertyNames() {
 
 	    if (jsfFacetConfigurationEnabled)
 	    {
 	        return new String[] { CONFIG_PATH, SERVLET_NAME, SERVLET_CLASSNAME, COMPONENT_LIBRARIES, LIBRARY_PROVIDER_DELEGATE };
 	    }
 	    
         return new String[] { LIBRARY_PROVIDER_DELEGATE };
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.wst.common.project.facet.ui.IFacetWizardPage#setWizardContext(org.eclipse.wst.common.project.facet.ui.IWizardContext)
 	 */
 	public void setWizardContext(IWizardContext context) {
 		// hook into web datamodel if new project wizard.
 		Iterator it = context.getSelectedProjectFacets().iterator();
 		IProjectFacetVersion webFacetVersion = null;
 		while (it.hasNext()) {
 			// find Web facet
 			IProjectFacetVersion pfv = (IProjectFacetVersion) it.next();
 			if (pfv.getProjectFacet().getId().equals("jst.web")) { //$NON-NLS-1$
 				webFacetVersion = pfv;
 				break;
 			}
 		}
 		if (webFacetVersion != null) {
 			try {
 				webAppDataModel = (IDataModel) context.getConfig(
 						webFacetVersion, IFacetedProject.Action.Type.INSTALL,
 						context.getProjectName());
 				if (webAppDataModel != null) {
 					webAppDataModel.addListener(this);
 				}
 			} catch (CoreException e) {
 				JSFUiPlugin.log(IStatus.ERROR,
 						Messages.JSFFacetInstallPage_ErrorNoWebAppDataModel, e);
 			}
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.wst.common.frameworks.internal.datamodel.ui.DataModelWizardPage#propertyChanged(org.eclipse.wst.common.frameworks.datamodel.DataModelEvent)
 	 */
 	public void propertyChanged(DataModelEvent event) {
		if (webAppDataModel != null && jsfFacetConfigurationEnabled) {
 			String propertyName = event.getPropertyName();
			if (propertyName.equals(IJ2EEModuleFacetInstallDataModelProperties.CONFIG_FOLDER)) {
				model.setStringProperty(WEBCONTENT_DIR, event.getProperty().toString());
 			}
 		}
 		super.propertyChanged(event);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.wst.common.frameworks.internal.datamodel.ui.DataModelWizardPage#dispose()
 	 */
 	public void dispose() {
 		if (webAppDataModel != null)
 			webAppDataModel.removeListener(this);
 		
 		//jsfLibCfgComp.dispose();
 		super.dispose();
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.wst.common.frameworks.internal.datamodel.ui.DataModelWizardPage#restoreDefaultSettings()
 	 */
 	protected void restoreDefaultSettings() {
 	    if (jsfFacetConfigurationEnabled)
 	    {
 	        initializeValues();
 	    }
 	}
 	/* (non-Javadoc)
 	 * @see org.eclipse.wst.common.frameworks.internal.datamodel.ui.DataModelWizardPage#showValidationErrorsOnEnter()
 	 */
 	protected boolean showValidationErrorsOnEnter() {
 		return true;
 	}
 
 
     /**
      * Fix for Bug Bug 300454: "Finish button in New Project wizard is enabled
      * even if JSF facet does not have library information"
      * https://bugs.eclipse.org/bugs/show_bug.cgi?id=300454
      * 
      * (non-Javadoc)
      * 
      * @see org.eclipse.jface.wizard.WizardPage#isPageComplete()
      */
     @Override
     public boolean isPageComplete()
     {
         final LibraryInstallDelegate librariesInstallDelegate = (LibraryInstallDelegate) getDataModel().getProperty(LIBRARY_PROVIDER_DELEGATE);
         if (librariesInstallDelegate == null)
             throw new IllegalArgumentException("LibraryInstallDelegate is expected to be non-null"); //$NON-NLS-1$
 
         return super.isPageComplete() && (librariesInstallDelegate.validate().getSeverity() != IStatus.ERROR);
     }
 }
