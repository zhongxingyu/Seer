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
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.jdt.core.IJavaProject;
 import org.eclipse.jem.util.logger.proxy.Logger;
 import org.eclipse.jem.workbench.utility.JemProjectUtilities;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.dialogs.ProgressMonitorDialog;
 import org.eclipse.jface.operation.IRunnableWithProgress;
 import org.eclipse.jface.preference.IPreferencePage;
 import org.eclipse.jface.preference.PreferencePage;
 import org.eclipse.jface.viewers.CheckboxTableViewer;
 import org.eclipse.jface.viewers.ColumnWeightData;
 import org.eclipse.jface.viewers.TableLayout;
 import org.eclipse.jst.common.componentcore.util.ComponentUtilities;
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
 import org.eclipse.jst.j2ee.internal.project.J2EEComponentUtilities;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.CCombo;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Group;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.swt.widgets.Table;
 import org.eclipse.swt.widgets.TableColumn;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.swt.widgets.Widget;
 import org.eclipse.ui.dialogs.PropertyPage;
 import org.eclipse.wst.common.componentcore.ComponentCore;
 import org.eclipse.wst.common.componentcore.internal.util.IModuleConstants;
 import org.eclipse.wst.common.componentcore.resources.IFlexibleProject;
 import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
 import org.eclipse.wst.common.frameworks.internal.ui.WTPUIPlugin;
 import org.eclipse.wst.common.frameworks.internal.ui.WorkspaceModifyComposedOperation;
 
 /**
  * @author jialin
  *
  * TODO To change the template for this generated type comment go to
  * Window - Preferences - Java - Code Style - Code Templates
  */
 public class JARDependencyPropertiesPage extends PropertyPage implements IClasspathTableOwner, Listener, ClasspathModelListener, ICommonManifestUIConstants {
 
     protected IProject project;
     protected IOException caughtManifestException;
     protected boolean isDirty;
     protected Text classPathText;
     protected Text componentNameText;
     protected ClasspathModel model;
     protected CCombo availableAppsCombo;
     protected ClasspathTableManager tableManager;
     protected IValidateEditListener validateEditListener;
     protected Button enableWLPCheckBox;
     protected Label manifestLabel;
     protected Label enterpriseApplicationLabel;
     protected Label availableDependentJars;
   
 
     /**
 	 * Constructor for JARDependencyPropertiesPage.
 	 */
     public JARDependencyPropertiesPage() {
         super();
     }
 
     protected void initialize() {
         project = (IProject) getElement().getAdapter(IResource.class);
         model = new ClasspathModel(null);
         model.setProject(project);
         model.addListener(this);
         updateModelManifest();
         initializeValidateEditListener();
     }
     
     public void dispose() {
     	super.dispose();
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
         validateEditListener.setShell(getShell());
     }
 
     /**
 	 * @see org.eclipse.jface.dialogs.IDialogPage#setVisible(boolean)
 	 */
     public void setVisible(boolean visible) {
         super.setVisible(visible);
         if (visible && caughtManifestException != null && !model.isDirty())
             ManifestErrorPrompter.showManifestException(getShell(), ERROR_READING_MANIFEST_DIALOG_MESSAGE_PROP_PAGE, false, caughtManifestException);
 
     }
 
     /**
 	 * @see PreferencePage#createContents(Composite)
 	 */
     protected Control createContents(Composite parent) {
         initialize();
         Composite composite = new Composite(parent, SWT.NONE);
         GridLayout layout = new GridLayout();
         layout.marginWidth = 0;
         layout.marginWidth = 0;
         composite.setLayout(layout);
         composite.setLayoutData(new GridData(GridData.FILL_BOTH));
         if(!isValidComponent())
         	return composite;
         createProjectLabelsGroup(composite);
         createListGroup(composite);
         createTextGroup(composite);
         refresh();
         handleStandaloneWebModule();
         return composite;
     }
 
 	private void handleStandaloneWebModule() {
 		if (enableWLPCheckBox != null && enableWLPCheckBox.getSelection() == true) {
 			model.setWLPModel(true);
 			handleWLPSupport(enableWLPCheckBox);
 			enableWLPCheckBox.setEnabled(false);
 		}
 	}
 
 	private boolean isValidComponent() {
 		if (model.getComponent().getComponentTypeId().equals(IModuleConstants.JST_EAR_MODULE)) {
 			this.setErrorMessage("Java Jar Dependencies is not valid for EAR modules");
 			return false;
 		} else if ((ComponentUtilities.getComponentsForProject(model.getProject())).length > 1) {
 			this.setErrorMessage("Java Jar Dependencies is valid only for one module per flexible project");
 			return false;
 		} else if (J2EEComponentUtilities.isStandaloneComponent(model.getComponent()) && !model.getComponent().getComponentTypeId().equals(IModuleConstants.JST_WEB_MODULE)) {
 			this.setErrorMessage(ClasspathModel.NO_EAR_MESSAGE);
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
 
 		Label label = new Label(labelsGroup, SWT.NONE);
 		label.setText(ManifestUIResourceHandler.getString("Project_name__UI_")); //$NON-NLS-1$ = "Project name:"
 
 		componentNameText = new Text(labelsGroup, SWT.BORDER);
 		GridData data = new GridData(GridData.FILL_HORIZONTAL);
 		componentNameText.setEditable(false);
 		componentNameText.setLayoutData(data);
 		componentNameText.setText(project.getName());
 		
 		if(J2EEComponentUtilities.isWebComponent(model.getComponent()))
         	createEnableWLPProjectCheckbox(labelsGroup);
 
 		createEnterpriseAppsControls(labelsGroup);
 
 	}
 
 	private void createEnterpriseAppsControls(Composite labelsGroup) {
 
 		enterpriseApplicationLabel = new Label(labelsGroup, SWT.NONE);
 		enterpriseApplicationLabel.setText(ManifestUIResourceHandler.getString("EAR_Project_Name__UI__UI_")); //$NON-NLS-1$ = "EAR project name:"
 
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
         availableDependentJars.setText(ManifestUIResourceHandler.getString("Available_dependent_JARs__UI_")); //$NON-NLS-1$ = "Available dependent JARs:"
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
 		manifestLabel.setText(ManifestUIResourceHandler.getString("Manifest_Class-Path__UI_")); //$NON-NLS-1$ = "Manifest Class-Path:"
 		
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
         availableJARsViewer.setSorter(null);
 
         // table columns
         TableColumn fileNameColumn = new TableColumn(table, SWT.NONE, 0);
         fileNameColumn.setText(ManifestUIResourceHandler.getString("JAR/Module_UI_")); //$NON-NLS-1$
         fileNameColumn.setResizable(true);
 
         TableColumn projectColumn = new TableColumn(table, SWT.NONE, 1);
         projectColumn.setText(ManifestUIResourceHandler.getString("Project_UI_")); //$NON-NLS-1$ = "Project"
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
         if(event.widget == enableWLPCheckBox) {
         	handleWLPSupport(event.widget);
         	tableManager.refresh();
         }
     }
     private void handleWLPSupport(Widget widget) {
 		if (((Button) widget).getSelection() == true) {
 			tableManager.setWLPEntry(true);
 			makeJarDependencyControlsInvisible();
 			availableDependentJars.setText("Select utility projects to add as Web Library projects to the web module"); //$NON-NLS-1$
 		} else {
 			tableManager.setWLPEntry(false);
 			makeJarDependencyControlsVisible();
 			availableDependentJars.setText(ManifestUIResourceHandler.getString("Available_dependent_JARs__UI_")); //$NON-NLS-1$ = "Available dependent JARs:"
 		}
 	}
 
 	private void makeJarDependencyControlsVisible() {
 		availableAppsCombo.setVisible(true);
 		tableManager.upButton.setVisible(true);
 		tableManager.downButton.setVisible(true);
 		tableManager.radioGroup.setVisible(true);
 		manifestLabel.setVisible(true);
 		classPathText.setVisible(true);
 		enterpriseApplicationLabel.setVisible(true);
 	}
 
 	private void makeJarDependencyControlsInvisible() {
 		availableAppsCombo.setVisible(false);
 		tableManager.upButton.setVisible(false);
 		tableManager.downButton.setVisible(false);
 		tableManager.radioGroup.setVisible(false);
 		manifestLabel.setVisible(false);
 		classPathText.setVisible(false);
 		enterpriseApplicationLabel.setVisible(false);
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
     
     protected void createEnableWLPProjectCheckbox(Composite parent) {
 		enableWLPCheckBox = new Button(parent, SWT.CHECK);
 		GridData data = new GridData(GridData.FILL_HORIZONTAL);
 		data.horizontalSpan = 2;
 		data.horizontalIndent = 20;
 		enableWLPCheckBox.setLayoutData(data);
 		enableWLPCheckBox.setText("Set Web Library Project dependency");
 		enableWLPCheckBox.addListener(SWT.Selection, this);
 		if (J2EEComponentUtilities.isStandaloneWebComponent(model.getComponent()))
 			enableWLPCheckBox.setSelection(true);
 		else
 			enableWLPCheckBox.setSelection(false);
 	}
 
     public void refreshText() {
 		ClassPathSelection sel = model.getClassPathSelection();
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
 
     /**
 	 * @see PreferencePage#performDefaults()
 	 */
     protected void performDefaults() {
         model.resetClassPathSelection();
         refresh();
         isDirty = false;
         model.dispose();
     }
     
     public boolean performCancel() {
     	model.dispose();
     	return super.performCancel();
     }
 
     /**
 	 * @see IPreferencePage#performOk()
 	 */
     public boolean performOk() {
     	if(isWLPProjectSetting())
     		return performWLPSettingOp();
     	else
     		return performJavaJarDependencyOp();
 	}
     
     private boolean performWLPSettingOp() {
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
 			return createdBuildPathSettings;
 		} finally {
 			model.dispose();
 		}
 	}
     
     private boolean runWLPOp(WorkspaceModifyComposedOperation composed) {
     	try {
 			if (composed != null)
 				new ProgressMonitorDialog(getShell()).run(true, true, composed);
 		} catch (InvocationTargetException ex) {
 			String title = ManifestUIResourceHandler.getString("An_internal_error_occurred_ERROR_"); //$NON-NLS-1$
 			String msg = title;
 			if (ex.getTargetException() != null && ex.getTargetException().getMessage() != null)
 				msg = ex.getTargetException().getMessage();
 			MessageDialog.openError(this.getShell(), title, msg);
 			org.eclipse.jem.util.logger.proxy.Logger.getLogger().logError(ex);
 			return false;
 		} catch (InterruptedException e) {
 			// cancelled
 			return false;
 		}
 		return true;
     }
 
 	private boolean performJavaJarDependencyOp() {
         if (!isDirty)
             return true;
         WorkspaceModifyComposedOperation composed = new WorkspaceModifyComposedOperation(createManifestOperation());
         composed.addRunnable(createBuildPathOperation());
         try {
             new ProgressMonitorDialog(getShell()).run(true, true, composed);
         } catch (InvocationTargetException ex) {
             String title = ManifestUIResourceHandler.getString("An_internal_error_occurred_ERROR_"); //$NON-NLS-1$
             String msg = title;
             if (ex.getTargetException() != null && ex.getTargetException().getMessage() != null)
                 msg = ex.getTargetException().getMessage();
             MessageDialog.openError(this.getShell(), title, msg);
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
 
 	private WorkspaceModifyComposedOperation createComponentDependencyOperations() {
 		WorkspaceModifyComposedOperation composedOp = null;
 		List selected = getSelectedClassPathSelectionForWLPs().getClasspathElements();
 		List unselected = getUnSelectedClassPathSelectionForWLPs().getClasspathElements();
 		
 		List targetComponentsHandles = new ArrayList();
 		for (int i = 0; i < selected.size(); i++) {
 			ClasspathElement element = (ClasspathElement) selected.get(i);
 			IProject elementProject = element.getProject();
 			IFlexibleProject flexProject = ComponentCore.createFlexibleProject(elementProject);
 			IVirtualComponent targetComp = flexProject.getComponents()[0];
 			targetComponentsHandles.add(targetComp.getComponentHandle());
 		}
 		if (!targetComponentsHandles.isEmpty()) {
 			composedOp = new WorkspaceModifyComposedOperation();
 			composedOp.addRunnable(WTPUIPlugin.getRunnableWithProgress(ComponentUtilities.createReferenceComponentOperation(model.getComponent().getComponentHandle(), targetComponentsHandles)));
 		}
 		targetComponentsHandles = new ArrayList();
 		for (int i = 0; i < unselected.size(); i++) {
 			ClasspathElement element = (ClasspathElement) unselected.get(i);
 			IProject elementProject = element.getProject();
 			IFlexibleProject flexProject = ComponentCore.createFlexibleProject(elementProject);
			IVirtualComponent targetComp = flexProject.getComponents()[0];
			targetComponentsHandles.add(targetComp.getComponentHandle());
 		}
 		if (!targetComponentsHandles.isEmpty()) {
 			composedOp = new WorkspaceModifyComposedOperation();
 			composedOp.addRunnable(WTPUIPlugin.getRunnableWithProgress(ComponentUtilities.removeReferenceComponentOperation(model.getComponent().getComponentHandle(), targetComponentsHandles)));
 		}
 		return composedOp;
 	}
 	
 	private WorkspaceModifyComposedOperation createFlexProjectOperations() {
 		WorkspaceModifyComposedOperation composedOp = null;
 		try {
 			Object[] elements = tableManager.availableJARsViewer.getCheckedElements();
 			for (int i = 0; i < elements.length; i++) {
 				ClasspathElement element = (ClasspathElement) elements[i];
 				IProject elementProject = element.getProject();
 				if (!elementProject.hasNature(IModuleConstants.MODULE_NATURE_ID)) {
 					if(composedOp == null)
 						composedOp = new WorkspaceModifyComposedOperation();
 					composedOp.addRunnable(WTPUIPlugin.getRunnableWithProgress(ComponentUtilities.createFlexJavaProjectForProjectOperation(elementProject)));
 				}
 			}
 		} catch (CoreException ce) {
 		}
 		return composedOp;
 	}
 	
 	protected IRunnableWithProgress createBuildPathOperation() {
         IJavaProject javaProject = JemProjectUtilities.getJavaProject(project);
         return WTPUIPlugin.getRunnableWithProgress(new UpdateJavaBuildPathOperation(javaProject,getSelectedClassPathSelectionForWLPs()));
     }
 	
 	protected IRunnableWithProgress createWLPBuildPathOperation() {
         IJavaProject javaProject = JemProjectUtilities.getJavaProject(project);
         return WTPUIPlugin.getRunnableWithProgress(new UpdateJavaBuildPathOperation(javaProject,getSelectedClassPathSelectionForWLPs(),getUnSelectedClassPathSelectionForWLPs()));
     }
 	
 	private ClassPathSelection getUnSelectedClassPathSelectionForWLPs() {
 		ClassPathSelection selection = new ClassPathSelection();
 		List uncheckedElements = new ArrayList();
 		Object[] checkedElements = tableManager.availableJARsViewer.getCheckedElements();
 		List modelElements = model.getClassPathSelectionForWLPs().getClasspathElements();
 		for (int i = 0; i < modelElements.size(); i++) {
 			List checkedElementsList = Arrays.asList(checkedElements);
 			if (!checkedElementsList.contains((ClasspathElement) modelElements.get(i))) {
 				selection.getClasspathElements().add((ClasspathElement) modelElements.get(i));
 			}
 		}
 		return selection;
 	}
 	
     
     private ClassPathSelection getSelectedClassPathSelectionForWLPs() {
     		ClassPathSelection selection = new ClassPathSelection();
     		Object[] checkedElements = tableManager.availableJARsViewer.getCheckedElements();
     		for(int i = 0; i < checkedElements.length; i++) {
     			selection.getClasspathElements().add((ClasspathElement)checkedElements[i]);
     		}
     		return selection;
 	}
 
 	protected boolean isWLPProjectSetting() {
     	if(enableWLPCheckBox != null)
     		return enableWLPCheckBox.getSelection();
     	return false;
     }
     
     protected UpdateManifestOperation createManifestOperation() {
         return new UpdateManifestOperation(project.getName(), model.getClassPathSelection().toString(), true);
     }
 
     protected boolean isReadOnly() {
         return JemProjectUtilities.isBinaryProject(project);
     }
 
 }
