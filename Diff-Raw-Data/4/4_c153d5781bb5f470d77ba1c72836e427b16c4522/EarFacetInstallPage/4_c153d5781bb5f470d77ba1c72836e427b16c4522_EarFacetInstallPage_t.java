 /******************************************************************************
  * Copyright (c) 2005 BEA Systems, Inc.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Konstantin Komissarchik - initial API and implementation
  ******************************************************************************/
 
 package org.eclipse.jst.j2ee.ui.project.facet;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IWorkspaceRoot;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.jem.util.emf.workbench.ProjectUtilities;
 import org.eclipse.jface.dialogs.IDialogConstants;
 import org.eclipse.jface.viewers.CheckStateChangedEvent;
 import org.eclipse.jface.viewers.CheckboxTableViewer;
 import org.eclipse.jface.viewers.ICheckStateListener;
 import org.eclipse.jface.viewers.TableLayout;
 import org.eclipse.jface.wizard.WizardDialog;
 import org.eclipse.jst.j2ee.internal.actions.IJ2EEUIContextIds;
 import org.eclipse.jst.j2ee.internal.common.J2EEVersionUtil;
 import org.eclipse.jst.j2ee.internal.earcreation.DefaultJ2EEComponentCreationDataModelProvider;
 import org.eclipse.jst.j2ee.internal.earcreation.IDefaultJ2EEComponentCreationDataModelProperties;
 import org.eclipse.jst.j2ee.internal.earcreation.IEarFacetInstallDataModelProperties;
 import org.eclipse.jst.j2ee.internal.plugin.J2EEUIMessages;
 import org.eclipse.jst.j2ee.internal.plugin.J2EEUIPlugin;
 import org.eclipse.jst.j2ee.internal.plugin.J2EEUIPluginIcons;
 import org.eclipse.jst.j2ee.internal.wizard.AvailableJ2EEComponentsContentProvider;
 import org.eclipse.jst.j2ee.internal.wizard.DefaultJ2EEComponentCreationWizard;
 import org.eclipse.jst.j2ee.internal.wizard.J2EEComponentLabelProvider;
 import org.eclipse.osgi.util.NLS;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.wst.common.componentcore.datamodel.properties.IFacetDataModelProperties;
 import org.eclipse.wst.common.frameworks.datamodel.DataModelFactory;
 import org.eclipse.wst.common.frameworks.datamodel.IDataModel;
 import org.eclipse.wst.common.project.facet.core.IProjectFacetVersion;
 import org.eclipse.wst.common.project.facet.core.runtime.IRuntime;
 import org.eclipse.wst.web.ui.internal.wizards.DataModelFacetInstallPage;
 
 /**
  * @author <a href="mailto:kosta@bea.com">Konstantin Komissarchik</a>
  */
 
 public final class EarFacetInstallPage extends DataModelFacetInstallPage implements IEarFacetInstallDataModelProperties {
 	
 	private Button selectAllButton;
 	private Button deselectAllButton;
 	private Button newModuleButton;
 	private CheckboxTableViewer moduleProjectsViewer;
 	private boolean ignoreCheckedState = false;
 	
 	
 	private Label contentDirLabel;
 	private Text contentDir;
 
 	public EarFacetInstallPage() {
 		super("ear.facet.install.page"); //$NON-NLS-1$
 		setTitle(J2EEUIMessages.getResourceString(J2EEUIMessages.EAR_COMPONENT_SECOND_PG_TITLE));
 		setDescription(J2EEUIMessages.getResourceString(J2EEUIMessages.EAR_COMPONENT_SECOND_PG_DESC));
 		setImageDescriptor(J2EEUIPlugin.getDefault().getImageDescriptor(J2EEUIPluginIcons.EAR_WIZ_BANNER));
 	}
 
 	protected String[] getValidationPropertyNames() {
 		return new String[]{CONTENT_DIR, J2EE_PROJECTS_LIST};
 	}
 
 	protected Composite createTopLevelComposite(Composite parent) {
 		Composite modulesGroup = new Composite(parent, SWT.NONE);
 		GridLayout layout = new GridLayout();
 		modulesGroup.setLayout(layout);
 		setInfopopID(IJ2EEUIContextIds.NEW_EAR_ADD_MODULES_PAGE);
 		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
 		modulesGroup.setLayoutData(gridData);
 		createModuleProjectOptions(modulesGroup);
 		createButtonsGroup(modulesGroup);
 		
 		final Composite composite = new Composite(modulesGroup, SWT.NONE);
 		composite.setLayout(new GridLayout(1, false));
 
 		this.contentDirLabel = new Label(composite, SWT.NONE);
 		this.contentDirLabel.setText(Resources.contentDirLabel);
 		this.contentDirLabel.setLayoutData(gdhfill());
 
 		this.contentDir = new Text(composite, SWT.BORDER);
 		this.contentDir.setLayoutData(gdhfill());
 		synchHelper.synchText(contentDir, CONTENT_DIR, null);
 
 		return modulesGroup;
 	}
 
 	protected int getJ2EEVersion() {
 		IProjectFacetVersion version = (IProjectFacetVersion)getDataModel().getProperty(FACET_VERSION);
 		return J2EEVersionUtil.convertVersionStringToInt(version.getVersionString());
 	}
 	
 	/**
 	 * @param modulesGroup
 	 */
 	private void createModuleProjectOptions(Composite modulesGroup) {
 		moduleProjectsViewer = CheckboxTableViewer.newCheckList(modulesGroup, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
 		GridData gData = new GridData(GridData.FILL_BOTH);
 		gData.widthHint = 200;
 		gData.heightHint = 80;
 		moduleProjectsViewer.getControl().setLayoutData(gData);
 		int j2eeVersion = getJ2EEVersion();
 		AvailableJ2EEComponentsContentProvider provider = new AvailableJ2EEComponentsContentProvider(j2eeVersion);
 		moduleProjectsViewer.setContentProvider(provider);
 		moduleProjectsViewer.setLabelProvider(new J2EEComponentLabelProvider());
 		setCheckedItemsFromModel();
 		
 		moduleProjectsViewer.addCheckStateListener(new ICheckStateListener() {
 			public void checkStateChanged(CheckStateChangedEvent event) {
 				if (!ignoreCheckedState) {
 					getDataModel().setProperty(J2EE_PROJECTS_LIST, getCheckedJ2EEElementsAsList());
 					getDataModel().setProperty(JAVA_PROJECT_LIST, getCheckedJavaProjectsAsList());
                 }
 			}
 		});
 		TableLayout tableLayout = new TableLayout();
 		moduleProjectsViewer.getTable().setLayout(tableLayout);
 		moduleProjectsViewer.getTable().setHeaderVisible(false);
 		moduleProjectsViewer.getTable().setLinesVisible(false);
 		moduleProjectsViewer.setSorter(null);
 	}
 
 	/**
 	 *  
 	 */
 	private void setCheckedItemsFromModel() {
 		List components = (List) getDataModel().getProperty(J2EE_PROJECTS_LIST);
 		moduleProjectsViewer.setCheckedElements(components.toArray());
 	}
 
 	private void refreshModules() {
 		moduleProjectsViewer.refresh();
 		setCheckedItemsFromModel();
 	}
 
 	protected List getCheckedJ2EEElementsAsList() {
 		Object[] elements = moduleProjectsViewer.getCheckedElements();
 		List list;
 		if (elements == null || elements.length == 0)
 			list = Collections.EMPTY_LIST;
 		else{
 			list = new ArrayList(); 
 			for( int i=0; i< elements.length; i++){
 				if( elements[i] instanceof IProject ) {
 					list.add(elements[i]);
 				}
 			}
 		}	
 		return list;
 	}
 	
 	protected List getCheckedJavaProjectsAsList() {
 		Object[] elements = moduleProjectsViewer.getCheckedElements();
 		List list;
 		if (elements == null || elements.length == 0)
 			list = Collections.EMPTY_LIST;
 		else{
 			list = new ArrayList(); 
 			for( int i=0; i< elements.length; i++){
 				if( elements[i] instanceof IProject ) {
 					list.add(elements[i]);
 				}
 			}
 		}	
 		return list;
 	}
 	
 	
 	protected void createButtonsGroup(org.eclipse.swt.widgets.Composite parent) {
 		Composite buttonGroup = new Composite(parent, SWT.NONE);
 		GridLayout layout = new GridLayout();
 		layout.numColumns = 4;
 		buttonGroup.setLayout(layout);
 		buttonGroup.setLayoutData(new GridData(GridData.FILL_BOTH));
 		selectAllButton = new Button(buttonGroup, SWT.PUSH);
 		selectAllButton.setText(J2EEUIMessages.getResourceString(J2EEUIMessages.APP_PROJECT_MODULES_PG_SELECT));
 		selectAllButton.addListener(SWT.Selection, this);
 		GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
 		gd.widthHint = 120;
 		selectAllButton.setLayoutData(gd);
 		deselectAllButton = new Button(buttonGroup, SWT.PUSH);
 		deselectAllButton.setText(J2EEUIMessages.getResourceString(J2EEUIMessages.APP_PROJECT_MODULES_PG_DESELECT));
 		deselectAllButton.addListener(SWT.Selection, this);
 		gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
 		gd.widthHint = 120;
 		deselectAllButton.setLayoutData(gd);
 		newModuleButton = new Button(buttonGroup, SWT.PUSH);
 		newModuleButton.setText(J2EEUIMessages.getResourceString(J2EEUIMessages.APP_PROJECT_MODULES_PG_NEW));
 		newModuleButton.addListener(SWT.Selection, this);
		gd = new GridData(GridData.GRAB_HORIZONTAL);
		gd.minimumWidth = 120;
 		newModuleButton.setLayoutData(gd);
 	}
 
 	/**
 	 * @see org.eclipse.swt.widgets.Listener#handleEvent(Event)
 	 */
 	public void handleEvent(Event evt) {
 		if (evt.widget == selectAllButton)
 			handleSelectAllButtonPressed();
 		else if (evt.widget == deselectAllButton)
 			handleDeselectAllButtonPressed();
 		else if (evt.widget == newModuleButton)
 			handleNewModuleButtonPressed();
 		else
 			super.handleEvent(evt);
 	}
 
 	/**
 	 *  
 	 */
 	private void handleNewModuleButtonPressed() {
 		IDataModel aModel = createNewModuleModel();
 		DefaultJ2EEComponentCreationWizard wizard = new DefaultJ2EEComponentCreationWizard(aModel);
 		WizardDialog dialog = new WizardDialog(getShell(), wizard);
 		dialog.create();
 		if (dialog.open() != IDialogConstants.CANCEL_ID) {
 			IWorkspaceRoot input = ResourcesPlugin.getWorkspace().getRoot();
 			moduleProjectsViewer.setInput(input);
             setNewModules(aModel);
             refreshModules();
 		}
 	}
     /**
      * @param model
      */
     private void setNewModules(IDataModel defaultModel) {
         List newComponents = new ArrayList();
         collectNewComponents(defaultModel, newComponents);
         List oldComponents = (List) getDataModel().getProperty(J2EE_PROJECTS_LIST);
         newComponents.addAll(oldComponents);
         getDataModel().setProperty(J2EE_PROJECTS_LIST, newComponents);
     }
     
     private void collectNewComponents(IDataModel defaultModel, List newProjects) {
         collectComponents(defaultModel.getNestedModel(IDefaultJ2EEComponentCreationDataModelProperties.NESTED_MODEL_EJB), newProjects);
         collectComponents(defaultModel.getNestedModel(IDefaultJ2EEComponentCreationDataModelProperties.NESTED_MODEL_WEB), newProjects);
         collectComponents(defaultModel.getNestedModel(IDefaultJ2EEComponentCreationDataModelProperties.NESTED_MODEL_CLIENT), newProjects);
         collectComponents(defaultModel.getNestedModel(IDefaultJ2EEComponentCreationDataModelProperties.NESTED_MODEL_JCA), newProjects);
     }
     private void collectComponents(IDataModel compDM, List newProjects) {
         if (compDM != null) {
         	String projectName = compDM.getStringProperty(IFacetDataModelProperties.FACET_PROJECT_NAME);
             if(projectName == null) return;
             IProject project = ProjectUtilities.getProject(projectName);
             if (project != null && project.exists())
                 newProjects.add(project);
         }
     }
     
 	private IDataModel createNewModuleModel() {
 		IDataModel defaultModel = DataModelFactory.createDataModel(new DefaultJ2EEComponentCreationDataModelProvider());
 		// transfer properties, project name
 		String projectName = model.getStringProperty(FACET_PROJECT_NAME);
 		defaultModel.setProperty(IDefaultJ2EEComponentCreationDataModelProperties.PROJECT_NAME, projectName);
 		// ear component name
 		String earName = model.getStringProperty(FACET_PROJECT_NAME);
 		defaultModel.setProperty(IDefaultJ2EEComponentCreationDataModelProperties.EAR_COMPONENT_NAME, earName);
 		// ear j2ee version
 		int j2eeVersion = getJ2EEVersion();
 		defaultModel.setProperty(IDefaultJ2EEComponentCreationDataModelProperties.J2EE_VERSION, new Integer(j2eeVersion));
 		
 		IRuntime rt = (IRuntime) model.getProperty(FACET_RUNTIME);
 		defaultModel.setProperty(IDefaultJ2EEComponentCreationDataModelProperties.FACET_RUNTIME, rt);
 		
 		return defaultModel;
 	}
 
 	/**
 	 *  
 	 */
 	private void handleDeselectAllButtonPressed() {
 		ignoreCheckedState = true;
 		try {
 			moduleProjectsViewer.setAllChecked(false);
 			//getDataModel().setProperty(J2EE_COMPONENT_LIST, null);
 			//IDataModel nestedModel = (IDataModel)getDataModel().getProperty(NESTED_ADD_COMPONENT_TO_EAR_DM);	
 			//(nestedModel).setProperty(AddComponentToEnterpriseApplicationDataModelProvider., getCheckedJ2EEElementsAsList());
 			getDataModel().setProperty(J2EE_PROJECTS_LIST, null);
 			getDataModel().setProperty(JAVA_PROJECT_LIST, null);			
 		} finally {
 			ignoreCheckedState = false;
 		}
 	}
 
 	/**
 	 *  
 	 */
 	private void handleSelectAllButtonPressed() {
 		ignoreCheckedState = true;
 		try {
 			moduleProjectsViewer.setAllChecked(true);
 			//getDataModel().setProperty(J2EE_COMPONENT_LIST, getCheckedElementsAsList());
 			//IDataModel nestedModel = (IDataModel)getDataModel().getProperty(NESTED_ADD_COMPONENT_TO_EAR_DM);
 			//(nestedModel).setProperty(AddComponentToEnterpriseApplicationDataModelProvider., getCheckedJ2EEElementsAsList());
 			
 			getDataModel().setProperty(J2EE_PROJECTS_LIST, getCheckedJ2EEElementsAsList());
 			getDataModel().setProperty(JAVA_PROJECT_LIST, getCheckedJavaProjectsAsList());
 			
 		} finally {
 			ignoreCheckedState = false;
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.wst.common.frameworks.internal.ui.wizard.J2EEWizardPage#enter()
 	 */
 	protected void enter() {
 		IWorkspaceRoot input = ResourcesPlugin.getWorkspace().getRoot();
 		moduleProjectsViewer.setInput(input);
 		super.enter();
 	}
 	
 	
 	private static final class Resources
 
 	extends NLS
 
 	{
 		public static String pageTitle;
 		public static String pageDescription;
 		public static String contentDirLabel;
 		public static String contentDirLabelInvalid;
 
 		static {
 			initializeMessages(EarFacetInstallPage.class.getName(), Resources.class);
 		}
 	}
 
 }
