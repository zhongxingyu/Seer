 /*******************************************************************************
  * Copyright (c) 2005 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.jst.j2ee.internal;
 
 import java.lang.reflect.InvocationTargetException;
 import java.util.ArrayList;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.jdt.core.IJavaProject;
 import org.eclipse.jdt.core.JavaCore;
 import org.eclipse.jdt.ui.wizards.BuildPathDialogAccess;
 import org.eclipse.jem.workbench.utility.JemProjectUtilities;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.dialogs.ProgressMonitorDialog;
 import org.eclipse.jface.operation.IRunnableWithProgress;
 import org.eclipse.jst.j2ee.application.internal.operations.ClassPathSelection;
 import org.eclipse.jst.j2ee.application.internal.operations.ClasspathElement;
 import org.eclipse.jst.j2ee.internal.common.ClasspathModelListener;
 import org.eclipse.jst.j2ee.internal.common.operations.UpdateJavaBuildPathOperation;
 import org.eclipse.jst.j2ee.internal.project.J2EEProjectUtilities;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.wst.common.componentcore.ComponentCore;
 import org.eclipse.wst.common.componentcore.internal.impl.ModuleURIUtil;
 import org.eclipse.wst.common.componentcore.internal.resources.VirtualArchiveComponent;
 import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
 import org.eclipse.wst.common.componentcore.resources.IVirtualReference;
 import org.eclipse.wst.common.frameworks.internal.ui.WTPUIPlugin;
 import org.eclipse.wst.common.frameworks.internal.ui.WorkspaceModifyComposedOperation;
 
 public class WebLibDependencyPropertiesPage extends JARDependencyPropertiesPage implements IClasspathTableOwner, Listener, ClasspathModelListener {
 
 	public WebLibDependencyPropertiesPage(final IProject project, 
     		final J2EEDependenciesPage page) {
 		super(project, page);
 	}
 	
     public Composite createContents(Composite parent) {
     	initialize(); 
 		Composite composite = createBasicComposite(parent);
 		if( model.getComponent() != null ){
 			if (!isValidWebModule())
 				return composite;
 	        J2EEDependenciesPage.createDescriptionComposite(composite, ManifestUIResourceHandler.Web_Libraries_Desc);
 			//createProjectLabelsGroup(composite);
 			createListGroup(composite);
 			handleWLPSupport();
 			model.setWLPModel(true);
 			setEnablement();
 		}
 		return composite;
 	}
     
     protected void createProjectLabelsGroup(Composite parent) {
 
 		Composite labelsGroup = new Composite(parent, SWT.NONE);
 		GridLayout layout = new GridLayout();
 		layout.numColumns = 2;
 		labelsGroup.setLayout(layout);
 		labelsGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 
 		Label label = new Label(labelsGroup, SWT.NONE);
 		label.setText(ManifestUIResourceHandler.Project_name__UI_); 
 
 		componentNameText = new Text(labelsGroup, SWT.BORDER);
 		GridData data = new GridData(GridData.FILL_HORIZONTAL);
 		componentNameText.setEditable(false);
 		componentNameText.setLayoutData(data);
 		componentNameText.setText(project.getName());
 	}
     
     protected void createListGroup(Composite parent) {
         Composite listGroup = new Composite(parent, SWT.NONE);
         GridLayout layout = new GridLayout();
         layout.numColumns = 1;
         layout.marginWidth = 0;
         layout.marginHeight = 0;
         listGroup.setLayout(layout);
         GridData gData = new GridData(GridData.FILL_BOTH);
         gData.horizontalIndent = 5;
         listGroup.setLayoutData(gData);
 
         availableDependentJars = new Label(listGroup, SWT.NONE);
         gData = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_FILL);
         availableDependentJars.setText(ManifestUIResourceHandler.Available_dependent_JARs__UI_); 
         availableDependentJars.setLayoutData(gData);
         createTableComposite(listGroup);
     }
    
     protected void createTableComposite(Composite parent) {
         Composite composite = new Composite(parent, SWT.NONE);
         GridData gData = new GridData(GridData.FILL_BOTH);
         composite.setLayoutData(gData);
         tableManager = new ClasspathTableManager(this, model, validateEditListener);
         tableManager.setReadOnly(isReadOnly());
         tableManager.fillWLPComposite(composite);
     }
     
     protected boolean isValidWebModule() {
 		if (!J2EEProjectUtilities.isDynamicWebProject(project)) {
 			propPage.setErrorMessage(ManifestUIResourceHandler.Web_Lib_Error); 
 			return false;
 		}
 		return true;
 	}
     
     protected void setEnablement() {
 		if (tableManager.availableJARsViewer.getTable().getItems().length == 0) {
 			tableManager.selectAllButton.setEnabled(false);
 			tableManager.deselectAllButton.setEnabled(false);
 		} else {
 			tableManager.selectAllButton.setEnabled(true);
 			tableManager.deselectAllButton.setEnabled(true);
 		}
 	}
     
     private void handleWLPSupport() {
 		tableManager.setWLPEntry(true);
 		availableDependentJars.setText("Select utility projects to add as Web Library projects to the web module"); //$NON-NLS-1$
 		tableManager.refresh();
 	}
     
     public boolean performOk() {
 		if( model.getComponent() == null || !isValidWebModule()){
 			return true;
 		}
 		if (!isDirty) {
             return true;
 		}
 		try {
 			boolean createdFlexProjects = runWLPOp(createFlexProjectOperations());
 			boolean createdComponentDependency = false;
 			if (createdFlexProjects)
 				createdComponentDependency = runWLPOp(createComponentDependencyOperations());
 			boolean createdBuildPathSettings = false;
 			if (createdComponentDependency) {
 				WorkspaceModifyComposedOperation composedOp = new WorkspaceModifyComposedOperation();
 				composedOp.addRunnable(createWLPBuildPathOperation());
 				createdBuildPathSettings = runWLPOp(composedOp);
 			}
 			isDirty = false;
 			return createdBuildPathSettings;
 		} finally {
 			model.dispose();
 		}
 	}
     
     private boolean runWLPOp(WorkspaceModifyComposedOperation composed) {
     	try {
 			if (composed != null)
 				new ProgressMonitorDialog(propPage.getShell()).run(true, true, composed);
 		} catch (InvocationTargetException ex) {
 			ex.printStackTrace();
 			ex.getCause().printStackTrace();
 			String title = ManifestUIResourceHandler.An_internal_error_occurred_ERROR_; 
 			String msg = title;
 			if (ex.getTargetException() != null && ex.getTargetException().getMessage() != null)
 				msg = ex.getTargetException().getMessage();
 			MessageDialog.openError(propPage.getShell(), title, msg);
 			org.eclipse.jem.util.logger.proxy.Logger.getLogger().logError(ex);
 			return false;
 		} catch (InterruptedException e) {
 			// cancelled
 			return false;
 		}
 		return true;
     }
 	public void handleSelectExternalJarButton(){
 		
 		if (J2EEProjectUtilities.isDynamicWebProject(project)){
 			
 			IPath[] selected= BuildPathDialogAccess.chooseExternalJAREntries(propPage.getShell());
 	
 			if (selected != null) {
 				for (int i= 0; i < selected.length; i++) {
 					
 					String type = VirtualArchiveComponent.LIBARCHIVETYPE + IPath.SEPARATOR;
 //					String name = selected[i].toString();
 					
 //					java.io.File file = new java.io.File(name);					
 					IVirtualComponent archive = ComponentCore.createArchiveComponent( model.getComponent().getProject(), type +
 								selected[i].toString());
 					
 					ArrayList vlist = new ArrayList();
 					IVirtualReference[] oldrefs = model.getComponent().getReferences();
 					for (int j = 0; j < oldrefs.length; j++) {
 						IVirtualReference ref = oldrefs[j];
 						vlist.add(ref);
 					}		
 				
 					//To do: check if archive component already exists
 					IVirtualReference ref = ComponentCore.createReference( model.getComponent(), archive, new Path("/WEB-INF/lib") ); //$NON-NLS-1$
 					vlist.add(ref);	
 					
 					IVirtualReference[] refs = new IVirtualReference[vlist.size()];
 					for (int j = 0; j < vlist.size(); j++) {
 						IVirtualReference tmpref = (IVirtualReference) vlist.get(j);
 						refs[j] = tmpref;
 					}				
 					model.getComponent().setReferences(refs);
 
 					
 					//ClasspathElement element = createClassPathElement(archive, file.getName());
 					ClasspathElement element = createClassPathElement(archive, archive.getName());
 					ClassPathSelection selection = createClassPathSelectionForExternalJar(element);
 					createBuildPathOperationForExternalJar(selection);
 					
 			        WorkspaceModifyComposedOperation composed = new WorkspaceModifyComposedOperation(createBuildPathOperationForExternalJar(selection));
 			        try {
 			            new ProgressMonitorDialog(propPage.getShell()).run(true, true, composed);
 			        } catch (InvocationTargetException ex) {
 			            String title = ManifestUIResourceHandler.An_internal_error_occurred_ERROR_; 
 			            String msg = title;
 			            if (ex.getTargetException() != null && ex.getTargetException().getMessage() != null)
 			                msg = ex.getTargetException().getMessage();
 			            MessageDialog.openError(propPage.getShell(), title, msg);
 			            org.eclipse.jem.util.logger.proxy.Logger.getLogger().logError(ex);
 			            
 			        } catch (InterruptedException e) {
 			            // cancelled
 
 			        }
 					model.getClassPathSelectionForWLPs().getClasspathElements().add(element);
 				}
 
 				refresh();
 			}
 		}
 		
 	}
 	
 	public void handleSelectVariableButton(){
 		
 		if (J2EEProjectUtilities.isDynamicWebProject(project)){
 			IPath existingPath[] = new Path[0];
 			IPath[] paths =  BuildPathDialogAccess.chooseVariableEntries(propPage.getShell(), existingPath);
 			
 			if (paths != null) {
 				for (int i = 0; i < paths.length; i++) {
 					IPath resolvedPath= JavaCore.getResolvedVariablePath(paths[i]);
 	
 					java.io.File file = new java.io.File(resolvedPath.toOSString());
 					if( file.isFile() && file.exists()){
 						String type = VirtualArchiveComponent.VARARCHIVETYPE + IPath.SEPARATOR;
 						
 						IVirtualComponent archive = ComponentCore.createArchiveComponent( model.getComponent().getProject(), type +
 									paths[i].toString());
 						
 						ArrayList vlist = new ArrayList();
 						IVirtualReference[] oldrefs = model.getComponent().getReferences();
 						for (int j = 0; j < oldrefs.length; j++) {
 							IVirtualReference ref = oldrefs[j];
 							vlist.add(ref);
 						}		
 					
 						//To do: check if archive component already exists
 						IVirtualReference ref = ComponentCore.createReference( model.getComponent(), archive, new Path("/WEB-INF/lib") ); //$NON-NLS-1$
 						vlist.add(ref);	
 						
 						IVirtualReference[] refs = new IVirtualReference[vlist.size()];
 						for (int j = 0; j < vlist.size(); j++) {
 							IVirtualReference tmpref = (IVirtualReference) vlist.get(j);
 							refs[j] = tmpref;
 						}				
 						model.getComponent().setReferences(refs);
 						
 						//ClasspathElement element = createClassPathElement(archive, file.getName());
 						ClasspathElement element = createClassPathElement(archive, archive.getName());
 						
 						ClassPathSelection selection = createClassPathSelectionForExternalJar(element);
 						createBuildPathOperationForExternalJar(selection);
 						
 				        WorkspaceModifyComposedOperation composed = new WorkspaceModifyComposedOperation(createBuildPathOperationForExternalJar(selection));
 				        try {
 				            new ProgressMonitorDialog(propPage.getShell()).run(true, true, composed);
 				        } catch (InvocationTargetException ex) {
 				            String title = ManifestUIResourceHandler.An_internal_error_occurred_ERROR_; 
 				            String msg = title;
 				            if (ex.getTargetException() != null && ex.getTargetException().getMessage() != null)
 				                msg = ex.getTargetException().getMessage();
 				            MessageDialog.openError(propPage.getShell(), title, msg);
 				            org.eclipse.jem.util.logger.proxy.Logger.getLogger().logError(ex);
 				            
 				        } catch (InterruptedException e) {
 				            // cancelled
 
 				        }
 						model.getClassPathSelectionForWLPs().getClasspathElements().add(element);						
 
 					}else{
 						//display error
 					}
 
 				}
 				refresh();
 			}	
 		}	
 	}	
 	
 	    private ClasspathElement createClassPathElement(IVirtualComponent archiveComp,
 				String unresolvedName ) {
 		
 		URI uri = URI.createURI(ModuleURIUtil.getHandleString(archiveComp));
 		ClasspathElement element = new ClasspathElement(uri);
 		element.setValid(false);
 		element.setSelected(true);
 		element.setRelativeText(unresolvedName);
 		element.setText(unresolvedName);
 		element.setEarProject(null);
 		return element;
 	}	
 	
 	private ClassPathSelection createClassPathSelectionForExternalJar(ClasspathElement element){
 		ClassPathSelection selection = new ClassPathSelection();
 		selection.getClasspathElements().add(element);
 		return selection;
 	}
 	private ClassPathSelection createClassPathSelectionForProjectJar(ClasspathElement element){
 		ClassPathSelection selection = new ClassPathSelection();
 		selection.getClasspathElements().add(element);
 		return selection;
 	}	
 	
 	
 	protected IRunnableWithProgress createBuildPathOperationForExternalJar(ClassPathSelection selection) {
 	    IJavaProject javaProject = JemProjectUtilities.getJavaProject(project);
 	    return WTPUIPlugin.getRunnableWithProgress(new UpdateJavaBuildPathOperation(javaProject, selection));
 	}
 
 	public void handleSelectProjectJarButton(){
 			
 			if (J2EEProjectUtilities.isDynamicWebProject(project)){
 				
 				IPath[] selected= BuildPathDialogAccess.chooseJAREntries(propPage.getShell(), project.getLocation(), new IPath[0]);
 		
 				if (selected != null) {
 					for (int i= 0; i < selected.length; i++) {
 						
 						String type = VirtualArchiveComponent.LIBARCHIVETYPE + IPath.SEPARATOR;
 	//					String name = selected[i].toString();
 						
 						//IPath fullPath = project.getFile(selected[i]).getFullPath();				
 						IVirtualComponent archive = ComponentCore.createArchiveComponent( model.getComponent().getProject(), type +
									selected[i].toString());
 						
 						ArrayList vlist = new ArrayList();
 						IVirtualReference[] oldrefs = model.getComponent().getReferences();
 						for (int j = 0; j < oldrefs.length; j++) {
 							IVirtualReference ref = oldrefs[j];
 							vlist.add(ref);
 						}		
 					
 						//To do: check if archive component already exists
 						IVirtualReference ref = ComponentCore.createReference( model.getComponent(), archive, new Path("/WEB-INF/lib") ); //$NON-NLS-1$
 						vlist.add(ref);	
 						
 						IVirtualReference[] refs = new IVirtualReference[vlist.size()];
 						for (int j = 0; j < vlist.size(); j++) {
 							IVirtualReference tmpref = (IVirtualReference) vlist.get(j);
 							refs[j] = tmpref;
 						}				
 						model.getComponent().setReferences(refs);
 	
 						
 						//ClasspathElement element = createClassPathElement(archive, file.getName());
 						ClasspathElement element = createClassPathElement(archive, archive.getName());
 						ClassPathSelection selection = createClassPathSelectionForProjectJar(element);
 						createBuildPathOperationForExternalJar(selection);
 						
 				        WorkspaceModifyComposedOperation composed = new WorkspaceModifyComposedOperation(createBuildPathOperationForExternalJar(selection));
 				        try {
 				            new ProgressMonitorDialog(propPage.getShell()).run(true, true, composed);
 				        } catch (InvocationTargetException ex) {
 				            String title = ManifestUIResourceHandler.An_internal_error_occurred_ERROR_; 
 				            String msg = title;
 				            if (ex.getTargetException() != null && ex.getTargetException().getMessage() != null)
 				                msg = ex.getTargetException().getMessage();
 				            MessageDialog.openError(propPage.getShell(), title, msg);
 				            org.eclipse.jem.util.logger.proxy.Logger.getLogger().logError(ex);
 				            
 				        } catch (InterruptedException e) {
 				            // cancelled
 	
 				        }
 						model.getClassPathSelectionForWLPs().getClasspathElements().add(element);
 					}
 	
 					refresh();
 				}
 			}
 			
 		}    
 	
 //	private void enableExternalJarControls(boolean b) {
 //		tableManager.externalJarButton.setVisible(b);
 //		tableManager.addVariableButton.setVisible(b);
 //	}
 	
 }
