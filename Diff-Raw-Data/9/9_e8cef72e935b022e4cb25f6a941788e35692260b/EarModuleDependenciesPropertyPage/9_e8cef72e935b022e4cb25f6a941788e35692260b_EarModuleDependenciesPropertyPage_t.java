 /******************************************************************************
  * Copyright (c) 2009 Red Hat, IBM
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Rob Stryker - initial implementation and ongoing maintenance
  *    Chuck Bridgham - additional support
  ******************************************************************************/
 package org.eclipse.jst.j2ee.internal.ui.preferences;
 
 import java.util.ArrayList;
 
 import org.eclipse.core.commands.ExecutionException;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jst.j2ee.application.internal.operations.AddComponentToEnterpriseApplicationDataModelProvider;
 import org.eclipse.jst.j2ee.application.internal.operations.RemoveComponentFromEnterpriseApplicationDataModelProvider;
 import org.eclipse.jst.j2ee.internal.J2EEConstants;
 import org.eclipse.jst.j2ee.internal.common.classpath.J2EEComponentClasspathUpdater;
 import org.eclipse.jst.j2ee.internal.componentcore.JavaEEModuleHandler;
 import org.eclipse.jst.j2ee.internal.plugin.J2EEUIMessages;
 import org.eclipse.jst.j2ee.internal.plugin.J2EEUIPlugin;
 import org.eclipse.jst.j2ee.model.IEARModelProvider;
 import org.eclipse.jst.j2ee.model.ModelProviderManager;
import org.eclipse.jst.j2ee.project.JavaEEProjectUtilities;
 import org.eclipse.jst.javaee.application.Application;
 import org.eclipse.jst.jee.project.facet.EarCreateDeploymentFilesDataModelProvider;
 import org.eclipse.jst.jee.project.facet.ICreateDeploymentFilesDataModelProperties;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.ModifyEvent;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.wst.common.componentcore.internal.IModuleHandler;
 import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
 import org.eclipse.wst.common.componentcore.resources.IVirtualFile;
 import org.eclipse.wst.common.componentcore.ui.propertypage.AddModuleDependenciesPropertiesPage;
 import org.eclipse.wst.common.componentcore.ui.propertypage.ModuleAssemblyRootPage;
 import org.eclipse.wst.common.frameworks.datamodel.DataModelFactory;
 import org.eclipse.wst.common.frameworks.datamodel.IDataModel;
 import org.eclipse.wst.common.frameworks.datamodel.IDataModelOperation;
 import org.eclipse.wst.common.frameworks.datamodel.IDataModelProvider;
 
 public class EarModuleDependenciesPropertyPage extends
 		AddModuleDependenciesPropertiesPage {
 	private String libDir = null;
 	private Text libDirText;
 	public EarModuleDependenciesPropertyPage(IProject project,
 			ModuleAssemblyRootPage page) {
 		super(project, page);
 	}
 	
 	@Override
 	protected void createTableComposite(Composite parent) {
 		Composite composite = new Composite(parent, SWT.NONE);
 		GridData gData = new GridData(GridData.FILL_BOTH);
 		composite.setLayoutData(gData);
 		fillTableComposite(composite);
		if( JavaEEProjectUtilities.isJEEComponent(rootComponent))
			addLibDirComposite(composite);
 	}
 
 	private String loadLibDirString() {
 		Application app = (Application)ModelProviderManager.getModelProvider(project).getModelObject();
 		String val = app.getLibraryDirectory();
 		if (val == null)
 			val = J2EEConstants.EAR_DEFAULT_LIB_DIR;
 		return val;
 	}
 	
 	protected void addLibDirComposite(Composite parent) {
 		libDir = loadLibDirString();
 		Composite c = new Composite(parent, SWT.NONE);
 		GridData mainData = new GridData(GridData.FILL_HORIZONTAL);
 		c.setLayoutData(mainData);
 		
 		GridLayout gl = new GridLayout(2,false);
 		c.setLayout(gl);
 		Label l = new Label(c, SWT.NONE);
 		l.setText(Messages.EarModuleDependenciesPropertyPage_LIBDIR);
 		libDirText = new Text(c, SWT.BORDER);
 		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
 		libDirText.setLayoutData(gd);
 		libDirText.setText(libDir);
 		libDirText.addModifyListener(new ModifyListener() {
 			public void modifyText(ModifyEvent e) {
 				libDirTextModified();
 			} });
 	}
 	
 	protected void libDirTextModified() {
 		libDir = libDirText.getText();
 	}
 
 	protected IDataModelOperation generateEARDDOperation() {
 		IDataModel model = DataModelFactory.createDataModel(new EarCreateDeploymentFilesDataModelProvider());
 		model.setProperty(ICreateDeploymentFilesDataModelProperties.GENERATE_DD, rootComponent);
 		model.setProperty(ICreateDeploymentFilesDataModelProperties.TARGET_PROJECT, project);
 		return model.getDefaultOperation();
 	}
 	
 	@Override
 	public boolean postHandleChanges(IProgressMonitor monitor) {
 		return true;
 	}
 	
 	@Override
 	protected void handleRemoved(ArrayList<IVirtualComponent> removed) {
 		super.handleRemoved(removed);
 		J2EEComponentClasspathUpdater.getInstance().queueUpdateEAR(rootComponent.getProject());
 	}
 
 	@Override
 	protected IDataModelProvider getAddReferenceDataModelProvider(IVirtualComponent component) {
 		return new AddComponentToEnterpriseApplicationDataModelProvider();
 	}
 
 	@Override
 	protected String getModuleAssemblyRootPageDescription() {
 		return Messages.EarModuleDependenciesPropertyPage_3;
 	}
 
 	@Override
 	protected IDataModelProvider getRemoveReferenceDataModelProvider(IVirtualComponent component) {
 		return new RemoveComponentFromEnterpriseApplicationDataModelProvider();
 	}
 	
 	protected void createDD(IProgressMonitor monitor) {
 		if( rootComponent != null ){
 			IDataModelOperation op = generateEARDDOperation();
 			try {
 				op.execute(monitor, null);
 			} catch (ExecutionException e) {
 				J2EEUIPlugin.logError(e);
 			}
 		}
 	}
 
 	@Override
 	public void handleEvent(Event event) {
 		super.handleEvent(event);
 	}
 
 	@Override
 	public boolean performOk() {
 		boolean result = super.performOk();
		if( JavaEEProjectUtilities.isJEEComponent(rootComponent))
			updateLibDir();
 		return result;
 	}
 	
 	private void updateLibDir() {
 		if (!libDir.equals(J2EEConstants.EAR_DEFAULT_LIB_DIR)) {
 			IVirtualFile vFile = rootComponent.getRootFolder().getFile(new Path(J2EEConstants.APPLICATION_DD_URI));
 			if (!vFile.exists()) {
 				if (!MessageDialog.openQuestion(null, 
 						J2EEUIMessages.getResourceString(J2EEUIMessages.NO_DD_MSG_TITLE), 
 						J2EEUIMessages.getResourceString(J2EEUIMessages.GEN_DD_QUESTION))) 
 					return;
 				createDD(new NullProgressMonitor());
 			}
 		}
 		
 		final IEARModelProvider earModel = (IEARModelProvider)ModelProviderManager.getModelProvider(project);
 		Application app = (Application)earModel.getModelObject();
 		String oldLibDir = app.getLibraryDirectory();
 		if ((oldLibDir != null && !oldLibDir.equals(libDir)) || oldLibDir == null) {
 			earModel.modify(new Runnable() {
 				public void run() {		
 				Application app2 = (Application)earModel.getModelObject();
 				app2.setLibraryDirectory(libDir);
 			}}, null);
 		}
 	}
 
 	@Override
 	protected IModuleHandler getModuleHandler() {
 		if(moduleHandler == null)
 			moduleHandler = new JavaEEModuleHandler();
 		return moduleHandler;
 	}
 
 }
