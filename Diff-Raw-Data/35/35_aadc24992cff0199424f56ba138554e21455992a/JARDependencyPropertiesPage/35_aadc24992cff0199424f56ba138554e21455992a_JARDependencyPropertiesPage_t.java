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
 /*
  * Created on Jan 17, 2005
  *
  * TODO To change the template for this generated file go to
  * Window - Preferences - Java - Code Style - Code Templates
  */
 package org.eclipse.jst.j2ee.internal;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.lang.reflect.InvocationTargetException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.jar.Manifest;
 
 import org.eclipse.core.resources.IContainer;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.jdt.core.IJavaProject;
 import org.eclipse.jem.util.logger.proxy.Logger;
 import org.eclipse.jem.workbench.utility.JemProjectUtilities;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.dialogs.ProgressMonitorDialog;
 import org.eclipse.jface.operation.IRunnableWithProgress;
 import org.eclipse.jface.preference.IPreferencePage;
 import org.eclipse.jface.viewers.CheckboxTableViewer;
 import org.eclipse.jface.viewers.ColumnWeightData;
 import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.ViewerSorter;
 import org.eclipse.jst.j2ee.application.internal.operations.ClassPathSelection;
 import org.eclipse.jst.j2ee.application.internal.operations.ClasspathElement;
 import org.eclipse.jst.j2ee.commonarchivecore.internal.helpers.ArchiveManifest;
 import org.eclipse.jst.j2ee.commonarchivecore.internal.helpers.ArchiveManifestImpl;
 import org.eclipse.jst.j2ee.internal.common.ClasspathModel;
 import org.eclipse.jst.j2ee.internal.common.ClasspathModelEvent;
 import org.eclipse.jst.j2ee.internal.common.ClasspathModelListener;
 import org.eclipse.jst.j2ee.internal.common.operations.UpdateJavaBuildPathOperation;
 import org.eclipse.jst.j2ee.internal.listeners.IValidateEditListener;
 import org.eclipse.jst.j2ee.internal.listeners.ValidateEditListener;
 import org.eclipse.jst.j2ee.internal.project.J2EEProjectUtilities;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.CCombo;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Group;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.swt.widgets.Table;
 import org.eclipse.swt.widgets.TableColumn;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.wst.common.componentcore.ComponentCore;
 import org.eclipse.wst.common.componentcore.ModuleCoreNature;
 import org.eclipse.wst.common.componentcore.UnresolveableURIException;
 import org.eclipse.wst.common.componentcore.internal.impl.ModuleURIUtil;
 import org.eclipse.wst.common.componentcore.internal.util.ComponentUtilities;
 import org.eclipse.wst.common.componentcore.internal.util.IModuleConstants;
 import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
 import org.eclipse.wst.common.componentcore.resources.IVirtualReference;
 import org.eclipse.wst.common.frameworks.internal.ui.WTPUIPlugin;
 import org.eclipse.wst.common.frameworks.internal.ui.WorkspaceModifyComposedOperation;
 
 /**
  * @author jialin
  *
  * TODO To change the template for this generated type comment go to
  * Window - Preferences - Java - Code Style - Code Templates
  */
 public class JARDependencyPropertiesPage implements IJ2EEDependenciesControl, IClasspathTableOwner, Listener, ClasspathModelListener {
 
     protected final IProject project;
     protected final J2EEDependenciesPage propPage;
     protected IOException caughtManifestException;
     protected boolean isDirty;
     protected Text classPathText;
     protected Text componentNameText;
     protected ClasspathModel model;
     protected CCombo availableAppsCombo;
     protected ClasspathTableManager tableManager;
     protected IValidateEditListener validateEditListener;
     protected Label manifestLabel;
     protected Label enterpriseApplicationLabel;
     protected Label availableDependentJars;
 
     /**
 	 * Constructor for JARDependencyPropertiesControl
 	 */
     public JARDependencyPropertiesPage(final IProject project, 
     		final J2EEDependenciesPage page) {
         super();
         this.project = project;
         this.propPage = page;
     }
 
     /**
      * Returns false if page should not be displayed 
      * for the project.
      */
     protected void initialize() {
         model = new ClasspathModel(null);
         model.setProject(project);
         if( model.getComponent() != null ){
 	        model.addListener(this);
 	        updateModelManifest();
 	        initializeValidateEditListener();
         }
     }
     
     public void dispose() {
     	if(model.earArtifactEdit != null) {
     		model.earArtifactEdit.dispose();
     		model.earArtifactEdit = null;
     	}
     }
 
     private void updateModelManifest() {
         if (JemProjectUtilities.isBinaryProject(project) || model.getAvailableEARComponents().length == 0)
             return;
         IContainer root = null;
         IFile manifestFile = null;
         if (project != null)
             root = project;
         else
             root = JemProjectUtilities.getSourceFolderOrFirst(project, null);
 
         if (root != null)
             manifestFile = root.getFile(new Path(J2EEConstants.MANIFEST_URI));
 
         if (manifestFile == null || !manifestFile.exists())
             return;
 
         InputStream in = null;
         try {
             in = manifestFile.getContents();
             ArchiveManifest mf = new ArchiveManifestImpl(new Manifest(in));
             model.primSetManifest(mf);
         } catch (CoreException e) {
             Logger.getLogger().logError(e);
             model.primSetManifest(new ArchiveManifestImpl());
         } catch (IOException iox) {
             Logger.getLogger().logError(iox);
             model.primSetManifest(new ArchiveManifestImpl());
             caughtManifestException = iox;
         } finally {
             if (in != null) {
                 try {
                     in.close();
                 } catch (IOException weTried) {
                 	//Ignore
                 }
             }
         }
     }
 
     protected void initializeValidateEditListener() {
         validateEditListener = new ValidateEditListener(null, model);
         validateEditListener.setShell(propPage.getShell());
     }
 
     public void setVisible(boolean visible) {
         if (visible && caughtManifestException != null && !model.isDirty())
             ManifestErrorPrompter.showManifestException(propPage.getShell(), ERROR_READING_MANIFEST_DIALOG_MESSAGE_PROP_PAGE, false, caughtManifestException);
 
     }
 
     public Composite createContents(Composite parent) {
     	initialize(); 
         Composite composite = createBasicComposite(parent);
         GridLayout layout = new GridLayout();
         layout.marginWidth = 0;
         layout.marginWidth = 0;
         composite.setLayout(layout);
         composite.setLayoutData(new GridData(GridData.FILL_BOTH));
         if( model.getComponent() != null ){        
 	        if(!isValidComponent())
 	        	return composite;
 	        J2EEDependenciesPage.createDescriptionComposite(composite, ManifestUIResourceHandler.J2EE_Modules_Desc);
 	        createProjectLabelsGroup(composite);
 	        createListGroup(composite);
 	        createTextGroup(composite);
 	        refresh();
     	}
         return composite;
     }
 	
 	/**
 	 * @param comp
 	 * @return
 	 */
 	protected Composite createBasicComposite(Composite comp) {
 		Composite composite = new Composite(comp, SWT.NONE);
 		GridLayout layout = new GridLayout();
         layout.marginWidth = 0;
         layout.marginWidth = 0;
         composite.setLayout(layout);
         composite.setLayoutData(new GridData(GridData.FILL_BOTH));
 		return composite;
 	}
 
 	protected boolean isValidComponent() {
 		if (J2EEProjectUtilities.isEARProject(project)) {
 			propPage.setErrorMessage(ManifestUIResourceHandler.EAR_Module_Dep_Error); 
 			return false;
 		} else if (J2EEProjectUtilities.isStandaloneProject(model.getComponent().getProject()) ) {
 			propPage.setErrorMessage(ClasspathModel.NO_EAR_MESSAGE);
 			return false;
 		}
 		return true;
 	}
 
     protected void createProjectLabelsGroup(Composite parent) {
 
 		Composite labelsGroup = new Composite(parent, SWT.NONE);
 		GridLayout layout = new GridLayout();
 		layout.numColumns = 2;
 		labelsGroup.setLayout(layout);
 		labelsGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 		/*
 		Label label = new Label(labelsGroup, SWT.NONE);
 		label.setText(ManifestUIResourceHandler.Project_name__UI_); 
 
 		componentNameText = new Text(labelsGroup, SWT.BORDER);
 		GridData data = new GridData(GridData.FILL_HORIZONTAL);
 		componentNameText.setEditable(false);
 		componentNameText.setLayoutData(data);
 		componentNameText.setText(project.getName());
 		*/
 		
 		createEnterpriseAppsControls(labelsGroup);
 
 	}
 
     private void createEnterpriseAppsControls(Composite labelsGroup) {
 
 		enterpriseApplicationLabel = new Label(labelsGroup, SWT.NONE);
 		enterpriseApplicationLabel.setText(ManifestUIResourceHandler.EAR_Project_Name__UI__UI_); 
 
 		availableAppsCombo = new CCombo(labelsGroup, SWT.READ_ONLY | SWT.BORDER);
 		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
 		availableAppsCombo.setLayoutData(gd);
 
 		availableAppsCombo.addListener(SWT.Selection, this);
 
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
 
     /*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.ibm.etools.j2ee.common.ui.classpath.IClasspathTableOwner#createGroup(org.eclipse.swt.widgets.Composite)
 	 */
     public Group createGroup(Composite parent) {
         return new Group(parent, SWT.NULL);
     }
 
     protected void createTextGroup(Composite parent) {
 
 		Composite textGroup = new Composite(parent, SWT.NONE);
 		GridLayout layout = new GridLayout();
 		layout.numColumns = 1;
 		textGroup.setLayout(layout);
 		textGroup.setLayoutData(new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL));
 
 		createClassPathText(textGroup);
 
 	}
 
     protected void createClassPathText(Composite textGroup) {
     	
     	manifestLabel = new Label(textGroup, SWT.NONE);
 		manifestLabel.setText(ManifestUIResourceHandler.Manifest_Class_Path__UI_); 
 		
         classPathText = new Text(textGroup, SWT.BORDER | SWT.MULTI | SWT.WRAP | SWT.V_SCROLL);
         GridData gData = new GridData(GridData.FILL_BOTH);
         gData.widthHint = 400;
         gData.heightHint = 100;
         classPathText.setLayoutData(gData);
         classPathText.setEditable(false);
     }
     
     protected void createTableComposite(Composite parent) {
         Composite composite = new Composite(parent, SWT.NONE);
         GridData gData = new GridData(GridData.FILL_BOTH);
         composite.setLayoutData(gData);
         tableManager = new ClasspathTableManager(this, model, validateEditListener);
         tableManager.setReadOnly(isReadOnly());
         tableManager.fillComposite(composite);
     }
 
     /**
 	 * @see IClasspathTableOwner#createAvailableJARsViewer(Composite)
 	 */
     public CheckboxTableViewer createAvailableJARsViewer(Composite parent) {
         int flags = SWT.CHECK | SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI;
 
         Table table = new Table(parent,flags);
         CheckboxTableViewer availableJARsViewer = new CheckboxTableViewer(table);
 
         // set up table layout
         TableLayout tableLayout = new org.eclipse.jface.viewers.TableLayout();
         tableLayout.addColumnData(new ColumnWeightData(200, true));
         tableLayout.addColumnData(new ColumnWeightData(200, true));
         table.setLayout(tableLayout);
         table.setHeaderVisible(true);
         table.setLinesVisible(true);
        availableJARsViewer.setSorter(new ViewerSorter());
 
         // table columns
         TableColumn fileNameColumn = new TableColumn(table, SWT.NONE, 0);
         fileNameColumn.setText(ManifestUIResourceHandler.JAR_Module_UI_); 
         fileNameColumn.setResizable(true);
 
         TableColumn projectColumn = new TableColumn(table, SWT.NONE, 1);
         projectColumn.setText(ManifestUIResourceHandler.Project_UI_); 
         projectColumn.setResizable(true);
         tableLayout.layout(table, true);
         return availableJARsViewer;
 
     }
 
     /**
 	 * @see IClasspathTableOwner#createButtonColumnComposite(Composite)
 	 */
     public Composite createButtonColumnComposite(Composite parent) {
         Composite buttonColumn = new Composite(parent, SWT.NONE);
         GridLayout layout = new GridLayout();
         layout.numColumns = 1;
         layout.marginHeight = 0;
         layout.marginWidth = 0;
         buttonColumn.setLayout(layout);
         GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_BEGINNING);
         buttonColumn.setLayoutData(data);
         return buttonColumn;
     }
 
     /**
 	 * @see IClasspathTableOwner
 	 */
     public Button primCreatePushButton(String label, Composite buttonColumn) {
         Button aButton = new Button(buttonColumn, SWT.PUSH);
         aButton.setText(label);
         return aButton;
     }
 
     /**
 	 * @see IClasspathTableOwner
 	 */
     public Button primCreateRadioButton(String label, Composite parent) {
         Button aButton = new Button(parent, SWT.RADIO);
         aButton.setText(label);
         return aButton;
     }
 
     /**
 	 * @see Listener#handleEvent(Event)
 	 */
     public void handleEvent(Event event) {
         if (event.widget == availableAppsCombo)
             availableAppsSelected(event);
     }
     
 	protected void availableAppsSelected(Event event) {
         int index = availableAppsCombo.getSelectionIndex();
         model.selectEAR(index);
     }
     protected void populateApps() {
 		IVirtualComponent[] components = model.getAvailableEARComponents();
 		String[] values = new String[components.length];
 		for (int i = 0; i < components.length; i++) {
 			values[i] = components[i].getProject().getName();
 		}
 		if (availableAppsCombo != null) {
 			availableAppsCombo.setItems(values);
 			IVirtualComponent selected = model.getSelectedEARComponent();
 			if (selected != null) {
 				int index = Arrays.asList(components).indexOf(selected);
 				availableAppsCombo.select(index);
 			} else
 				availableAppsCombo.clearSelection();
 		}
 	}
 
     protected void refresh() {
 		populateApps();
 		tableManager.refresh();
 		refreshText();
 	}
     
 
     public void refreshText() {
 		ClassPathSelection sel = model.getClassPathSelection();
 		if( sel != null && classPathText != null )
 			classPathText.setText(sel == null ? "" : sel.toString()); //$NON-NLS-1$
 	}
 
     /**
 	 * @see ClasspathModelListener#modelChanged(ClasspathModelEvent)
 	 */
     public void modelChanged(ClasspathModelEvent evt) {
         if (evt.getEventType() == ClasspathModelEvent.CLASS_PATH_CHANGED) {
             isDirty = true;
             refreshText();
         } else if (evt.getEventType() == ClasspathModelEvent.EAR_PROJECT_CHANGED)
             tableManager.refresh();
     }
 
     public void performDefaults() {
         model.resetClassPathSelection();
         refresh();
         isDirty = false;
         model.dispose();
     }
     
     public boolean performCancel() {
     	model.dispose();
     	return true;
     }
 
     /**
 	 * @see IPreferencePage#performOk() 
 	 */
     public boolean performOk() {
         if (!isDirty)
             return true;
         WorkspaceModifyComposedOperation composed = new WorkspaceModifyComposedOperation(createManifestOperation());
         composed.addRunnable(createBuildPathOperation());
         
         WorkspaceModifyComposedOperation op = createJ2EEComponentDependencyOperations();
         if( op != null )
         	composed.addRunnable(op);
         try {
             new ProgressMonitorDialog(propPage.getShell()).run(true, true, composed);
         } catch (InvocationTargetException ex) {
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
         } finally {
         	model.dispose();
         }
         isDirty = false;
         return true;
     }
     
 //    private boolean runWLPOp(WorkspaceModifyComposedOperation composed) {
 //    	try {
 //			if (composed != null)
 //				new ProgressMonitorDialog(getShell()).run(true, true, composed);
 //		} catch (InvocationTargetException ex) {
 //			String title = ManifestUIResourceHandler.getString("An_internal_error_occurred_ERROR_"); //$NON-NLS-1$
 //			String msg = title;
 //			if (ex.getTargetException() != null && ex.getTargetException().getMessage() != null)
 //				msg = ex.getTargetException().getMessage();
 //			MessageDialog.openError(this.getShell(), title, msg);
 //			org.eclipse.jem.util.logger.proxy.Logger.getLogger().logError(ex);
 //			return false;
 //		} catch (InterruptedException e) {
 //			// cancelled
 //			return false;
 //		}
 //		return true;
 //    }
 
     
     List getUnSelectedClassPathElementsForJ2EEDependency(){
 		List unselectedForJ2EE = getUnSelectedClassPathSelection().getClasspathElements();
 	
 		List unselected = new ArrayList();
 		if( model != null && model.getClassPathSelectionForWLPs() != null ){
 			List wlpSelected= model.getClassPathSelectionForWLPs().getSelectedClasspathElements();
 	
 			java.util.Iterator it = unselectedForJ2EE.iterator();
 			
 			while( it.hasNext()){
 				ClasspathElement element = (ClasspathElement)it.next();
 				java.util.Iterator wlpIterator = wlpSelected.iterator();
 				boolean found = false;
 				while(wlpIterator.hasNext()){
 					ClasspathElement wlpElement = (ClasspathElement)wlpIterator.next();
 					String text = element.getText();
 					int  index = text.indexOf(".jar");
 					if(index != -1){
 						text = text.substring(0, index);
 						if( text.equals(wlpElement.getText()) ){
 							found = true;
 							break;
 						}
 					}
 				}
 				if( !found ){
 					if( !unselected.contains(element))
 						unselected.add(element);
 				}
 				
 			}
 		}
 		return unselected;
     }
  
     
     List getUnSelectedClassPathElementsForWebDependency(){
 		List unselectedForWLP = getUnSelectedClassPathSelectionForWLPs().getClasspathElements();
 		List unselected = new ArrayList();
 		if( model != null && model.getClassPathSelection() != null ){
 			List j2eeSelected= model.getClassPathSelection().getSelectedClasspathElements();
 			java.util.Iterator it = unselectedForWLP.iterator();
 			
 			while( it.hasNext()){
 				ClasspathElement element = (ClasspathElement)it.next();
 				java.util.Iterator j2eeIterator = j2eeSelected.iterator();
 				boolean found = false;
 				while( j2eeIterator.hasNext() ){
 					ClasspathElement j2eeElement = (ClasspathElement)j2eeIterator.next();
 					String text = j2eeElement.getText();
 					int  index = text.indexOf(".jar");
 					text = text.substring(0, index);
 					
 					if( element.getText().equals( text )){
 						found = true;
 						break;
 					}
 				}
 				if( !found ){
 					if( !unselected.contains(element))
 						unselected.add(element);
 				}
 				
 			}
 		}else{
 			unselected = unselectedForWLP;
 		}
 		return unselected;
     }
     
 	protected WorkspaceModifyComposedOperation createJ2EEComponentDependencyOperations() {
 		WorkspaceModifyComposedOperation composedOp = null;
 		List selected = getSelectedClassPathSelection().getClasspathElements();
 		List unselected = getUnSelectedClassPathElementsForJ2EEDependency();
 			
 		List targetComponentsHandles = new ArrayList();
 		for (int i = 0; i < selected.size(); i++) {
 			ClasspathElement element = (ClasspathElement) selected.get(i);
 			IProject elementProject = element.getProject();
 			if( elementProject != null ){
 				IVirtualComponent targetComp = ComponentCore.createComponent(elementProject);
 				targetComponentsHandles.add(targetComp);
 			}
 		}
  		if (!targetComponentsHandles.isEmpty()) {
   			composedOp = new WorkspaceModifyComposedOperation();
  			composedOp.addRunnable(WTPUIPlugin.getRunnableWithProgress(ComponentUtilities.createReferenceComponentOperation(model.getComponent(), targetComponentsHandles)));
   		}
   		targetComponentsHandles = new ArrayList();
 		for (int i = 0; i < unselected.size(); i++) {
 			ClasspathElement element = (ClasspathElement) unselected.get(i);
 			IProject elementProject = element.getProject();
 			if( elementProject != null ){
 				if (ModuleCoreNature.isFlexibleProject(elementProject)) {
 					IVirtualComponent targetComp = ComponentCore.createComponent(elementProject);
 					targetComponentsHandles.add(targetComp);
 				}
 			}else{
 				URI archiveURI = element.getArchiveURI();
 				if( archiveURI != null && !archiveURI.equals("") ){ //$NON-NLS-1$
 					String name = ""; //$NON-NLS-1$
 					try {
 						String type = ModuleURIUtil.getArchiveType(archiveURI);
 						String tmpname = ModuleURIUtil.getArchiveName(archiveURI);
 						name = type + IPath.SEPARATOR + tmpname;
 					} catch (UnresolveableURIException e) {
 						Logger.getLogger().logError(e.getMessage());
 					}
 					if( !name.equals("")){ //$NON-NLS-1$
 						IVirtualReference ref = model.getComponent().getReference(name);
 						if( ref != null ){
 							IVirtualComponent referenced = ref.getReferencedComponent();
 							targetComponentsHandles.add(referenced);
 						}
 					}	
 				}
 			}
 		}
 		if (!targetComponentsHandles.isEmpty()) {
 			if(composedOp == null)
 				composedOp = new WorkspaceModifyComposedOperation();
 			composedOp.addRunnable(WTPUIPlugin.getRunnableWithProgress(ComponentUtilities.removeReferenceComponentOperation(model.getComponent(), targetComponentsHandles)));
 		}
 		return composedOp;
 	}
 	
 	
 	protected WorkspaceModifyComposedOperation createComponentDependencyOperations() {
 		WorkspaceModifyComposedOperation composedOp = null;
 		List selected = getSelectedClassPathSelectionForWLPs().getClasspathElements();
 		List unselected = getUnSelectedClassPathElementsForWebDependency();
 		
 		List targetComponentsHandles = new ArrayList();
 		for (int i = 0; i < selected.size(); i++) {
 			ClasspathElement element = (ClasspathElement) selected.get(i);
 			IProject elementProject = element.getProject();
 			if( elementProject != null ){
 				IVirtualComponent targetComp = ComponentCore.createComponent(elementProject);
 				targetComponentsHandles.add(targetComp);
 			}
 		}
 		if (!targetComponentsHandles.isEmpty()) {
 			composedOp = new WorkspaceModifyComposedOperation();
 			composedOp.addRunnable(WTPUIPlugin.getRunnableWithProgress(ComponentUtilities.createWLPReferenceComponentOperation(model.getComponent(), targetComponentsHandles)));
 		}
 		targetComponentsHandles = new ArrayList();
 		for (int i = 0; i < unselected.size(); i++) {
 			ClasspathElement element = (ClasspathElement) unselected.get(i);
 			IProject elementProject = element.getProject();
 			if( elementProject != null ){
 				if (ModuleCoreNature.isFlexibleProject(elementProject)) {
 					IVirtualComponent targetComp = ComponentCore.createComponent(elementProject);
 					targetComponentsHandles.add(targetComp);
 				}
 			}else{
 				URI archiveURI = element.getArchiveURI();
 				if( archiveURI != null && !archiveURI.equals("") ){ //$NON-NLS-1$
 					String name = ""; //$NON-NLS-1$
 					try {
 						String type = ModuleURIUtil.getArchiveType(archiveURI);
 						String tmpname = ModuleURIUtil.getArchiveName(archiveURI);
 						name = type + IPath.SEPARATOR + tmpname;
 					} catch (UnresolveableURIException e) {
 						Logger.getLogger().logError(e.getMessage());
 					}
 					if( !name.equals("")){ //$NON-NLS-1$
 						IVirtualReference ref = model.getComponent().getReference(name);
 						IVirtualComponent referenced = ref.getReferencedComponent();
 						targetComponentsHandles.add(referenced);
 					}	
 				}
 			}
 		}
 		if (!targetComponentsHandles.isEmpty()) {
 			if(composedOp == null)
 				composedOp = new WorkspaceModifyComposedOperation();
 			composedOp.addRunnable(WTPUIPlugin.getRunnableWithProgress(ComponentUtilities.removeWLPReferenceComponentOperation(model.getComponent(), targetComponentsHandles)));
 		}
 		return composedOp;
 	}
 	
 	protected WorkspaceModifyComposedOperation createFlexProjectOperations() {
 		WorkspaceModifyComposedOperation composedOp = null;
 		try {
 			Object[] elements = tableManager.availableJARsViewer.getCheckedElements();
 			for (int i = 0; i < elements.length; i++) {
 				ClasspathElement element = (ClasspathElement) elements[i];
 				IProject elementProject = element.getProject();
 				if ( elementProject != null && !elementProject.hasNature(IModuleConstants.MODULE_NATURE_ID)) {
 					if(composedOp == null)
 						composedOp = new WorkspaceModifyComposedOperation();
 					composedOp.addRunnable(WTPUIPlugin.getRunnableWithProgress(J2EEProjectUtilities.createFlexJavaProjectForProjectOperation(elementProject)));
 				}
 			}
 		} catch (CoreException ce) {
 		}
 		return composedOp;
 	}
 	
 	protected IRunnableWithProgress createBuildPathOperation() {
         IJavaProject javaProject = JemProjectUtilities.getJavaProject(project);
         return WTPUIPlugin.getRunnableWithProgress(new UpdateJavaBuildPathOperation(javaProject,getSelectedClassPathSelectionForWLPs(), getUnSelectedClassPathElementsForJ2EEDependency()));
     }
 	
 	protected IRunnableWithProgress createWLPBuildPathOperation() {
         IJavaProject javaProject = JemProjectUtilities.getJavaProject(project);
         return WTPUIPlugin.getRunnableWithProgress(new UpdateJavaBuildPathOperation(javaProject,getSelectedClassPathSelectionForWLPs(),getUnSelectedClassPathElementsForWebDependency()));
     }
 	
 	protected ClassPathSelection getUnSelectedClassPathSelectionForWLPs() {
 		ClassPathSelection selection = new ClassPathSelection();
 		Object[] checkedElements = tableManager.availableJARsViewer.getCheckedElements();
 		List modelElements = model.getClassPathSelectionForWLPs().getClasspathElements();
 		for (int i = 0; i < modelElements.size(); i++) {
 			List checkedElementsList = Arrays.asList(checkedElements);
 			if (!checkedElementsList.contains(modelElements.get(i))) {
 				selection.getClasspathElements().add(modelElements.get(i));
 			}
 		}
 		return selection;
 	}
 	
     private ClassPathSelection getSelectedClassPathSelection() {
 		ClassPathSelection selection = new ClassPathSelection();
 		Object[] checkedElements = tableManager.availableJARsViewer.getCheckedElements();
 		for(int i = 0; i < checkedElements.length; i++) {
 			selection.getClasspathElements().add(checkedElements[i]);
 		}
 		return selection;
     }
     
 	protected ClassPathSelection getUnSelectedClassPathSelection() {
 		ClassPathSelection selection = new ClassPathSelection();
 		Object[] checkedElements = tableManager.availableJARsViewer.getCheckedElements();
 		List modelElements = model.getClassPathSelection().getClasspathElements();
 		for (int i = 0; i < modelElements.size(); i++) {
 			List checkedElementsList = Arrays.asList(checkedElements);
 			if (!checkedElementsList.contains(modelElements.get(i))) {
 				selection.getClasspathElements().add(modelElements.get(i));
 			}
 		}
 		return selection;
 	}
 	
     
     private ClassPathSelection getSelectedClassPathSelectionForWLPs() {
     		ClassPathSelection selection = new ClassPathSelection();
     		Object[] checkedElements = tableManager.availableJARsViewer.getCheckedElements();
     		for(int i = 0; i < checkedElements.length; i++) {
     			selection.getClasspathElements().add(checkedElements[i]);
     		}
     		return selection;
 	}
     
     protected UpdateManifestOperation createManifestOperation() {
         return new UpdateManifestOperation(project.getName(), model.getClassPathSelection().toString(), true);
     }
 
     protected boolean isReadOnly() {
         return JemProjectUtilities.isBinaryProject(project);
     }
 
 }
