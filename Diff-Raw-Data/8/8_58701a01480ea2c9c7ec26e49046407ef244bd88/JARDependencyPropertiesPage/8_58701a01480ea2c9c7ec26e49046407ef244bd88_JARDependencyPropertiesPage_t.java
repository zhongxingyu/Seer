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
 import java.util.Arrays;
 import java.util.jar.Manifest;
 
 import org.eclipse.core.resources.IContainer;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.jdt.core.IJavaProject;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.preference.IPreferencePage;
import org.eclipse.jface.preference.PreferencePage;
 import org.eclipse.jface.viewers.CheckboxTableViewer;
 import org.eclipse.jface.viewers.ColumnWeightData;
 import org.eclipse.jface.viewers.TableLayout;
 import org.eclipse.jst.j2ee.application.operations.ClassPathSelection;
 import org.eclipse.jst.j2ee.commonarchivecore.internal.helpers.ArchiveManifest;
 import org.eclipse.jst.j2ee.commonarchivecore.internal.helpers.ArchiveManifestImpl;
 import org.eclipse.jst.j2ee.internal.common.ClasspathModel;
 import org.eclipse.jst.j2ee.internal.common.ClasspathModelEvent;
 import org.eclipse.jst.j2ee.internal.common.ClasspathModelListener;
 import org.eclipse.jst.j2ee.internal.common.operations.UpdateJavaBuildPathOperation;
 import org.eclipse.jst.j2ee.internal.earcreation.EARNatureRuntime;
 import org.eclipse.jst.j2ee.internal.listeners.IValidateEditListener;
 import org.eclipse.jst.j2ee.internal.listeners.ValidateEditListener;
 import org.eclipse.jst.j2ee.internal.project.J2EENature;
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
 import org.eclipse.ui.dialogs.PropertyPage;
 import org.eclipse.wst.common.frameworks.internal.ui.WTPUIPlugin;
 import org.eclipse.wst.common.frameworks.internal.ui.WorkspaceModifyComposedOperation;
 import org.eclipse.wst.common.frameworks.operations.IHeadlessRunnableWithProgress;
 
 import com.ibm.wtp.common.logger.proxy.Logger;
 import com.ibm.wtp.emf.workbench.ProjectUtilities;
 
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
     protected Text projectNameText;
     protected ClasspathModel model;
     protected CCombo availableAppsCombo;
     protected ClasspathTableManager tableManager;
     protected IValidateEditListener validateEditListener;
 
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
 
     private void updateModelManifest() {
         if (ProjectUtilities.isBinaryProject(project) || model.getAvailableEARNatures().length == 0)
             return;
 
         IContainer root = null;
         IFile manifestFile = null;
         J2EENature nature = J2EENature.getRegisteredRuntime(project);
         if (nature != null)
             root = nature.getEMFRoot();
         else
             root = ProjectUtilities.getSourceFolderOrFirst(project, null);
 
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
         createProjectLabelsGroup(composite);
         createListGroup(composite);
         createTextGroup(composite);
         refresh();
 
         return composite;
     }
 
     protected void createProjectLabelsGroup(Composite parent) {
 
         Composite labelsGroup = new Composite(parent, SWT.NONE);
         GridLayout layout = new GridLayout();
         layout.numColumns = 2;
         labelsGroup.setLayout(layout);
         labelsGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 
         Label label = new Label(labelsGroup, SWT.NONE);
         label.setText(ManifestUIResourceHandler.getString("Project_name__UI_")); //$NON-NLS-1$ = "Project name:"
 
         projectNameText = new Text(labelsGroup, SWT.BORDER);
         GridData data = new GridData(GridData.FILL_HORIZONTAL);
         projectNameText.setEditable(false);
         projectNameText.setLayoutData(data);
         projectNameText.setText(project.getName());
 
         label = new Label(labelsGroup, SWT.NONE);
         label.setText(ManifestUIResourceHandler.getString("EAR_Project_Name__UI__UI_")); //$NON-NLS-1$ = "EAR project name:"
 
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
 
         Label label = new Label(listGroup, SWT.NONE);
         gData = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_FILL);
         label.setText(ManifestUIResourceHandler.getString("Available_dependent_JARs__UI_")); //$NON-NLS-1$ = "Available dependent JARs:"
         label.setLayoutData(gData);
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
 
         Label label = new Label(textGroup, SWT.NONE);
         label.setText(ManifestUIResourceHandler.getString("Manifest_Class-Path__UI_")); //$NON-NLS-1$ = "Manifest Class-Path:"
 
         createClassPathText(textGroup);
     }
 
     protected void createClassPathText(Composite textGroup) {
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
 
     /*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.ibm.etools.j2ee.common.ui.classpath.IClasspathTableOwner#createHideEJBClientJARsButton(org.eclipse.swt.widgets.Composite)
 	 */
     public Button createHideEJBClientJARsButton(Composite parent) {
         return new Button(parent, SWT.CHECK);
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
     }
     protected void availableAppsSelected(Event event) {
         int index = availableAppsCombo.getSelectionIndex();
         model.selectEAR(index);
     }
     protected void populateApps() {
         EARNatureRuntime[] natures = model.getAvailableEARNatures();
         String[] values = new String[natures.length];
         for (int i = 0; i < natures.length; i++) {
             values[i] = natures[i].getProject().getName();
         }
         availableAppsCombo.setItems(values);
         EARNatureRuntime selected = model.getSelectedEARNature();
         if (selected != null) {
             int index = Arrays.asList(natures).indexOf(selected);
             availableAppsCombo.select(index);
         } else
             availableAppsCombo.clearSelection();
     }
 
     /**
 	 * Checks if the any associations with ear, if not display an error.
 	 */
     protected void checkForEar() {
         if (availableAppsCombo.getItemCount() == 0) {
             setErrorMessage(ClasspathModel.NO_EAR_MESSAGE);
         } else {
             setErrorMessage(null);
         } // if
     } // checkForEar
 
     protected void refresh() {
         populateApps();
         tableManager.refresh();
         refreshText();
         checkForEar();
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
     }
 
     /**
 	 * @see IPreferencePage#performOk()
 	 */
     public boolean performOk() {
         if (!isDirty)
             return true;
         WorkspaceModifyComposedOperation composed = new WorkspaceModifyComposedOperation(createManifestOperation());
         IHeadlessRunnableWithProgress pathOp = createBuildPathOperation();
         composed.addRunnable(WTPUIPlugin.getRunnableWithProgress(pathOp));
         try {
             new ProgressMonitorDialog(getShell()).run(true, true, composed);
         } catch (InvocationTargetException ex) {
             String title = ManifestUIResourceHandler.getString("An_internal_error_occurred_ERROR_"); //$NON-NLS-1$
             String msg = title;
             if (ex.getTargetException() != null && ex.getTargetException().getMessage() != null)
                 msg = ex.getTargetException().getMessage();
             MessageDialog.openError(this.getShell(), title, msg);
             com.ibm.wtp.common.logger.proxy.Logger.getLogger().logError(ex);
             return false;
         } catch (InterruptedException e) {
             // cancelled
             return false;
         }
         isDirty = false;
         return true;
     }
     protected IHeadlessRunnableWithProgress createBuildPathOperation() {
         IJavaProject javaProject = ProjectUtilities.getJavaProject(project);
         return new UpdateJavaBuildPathOperation(javaProject, model.getClassPathSelection());
     }
     protected UpdateManifestOperation createManifestOperation() {
         return new UpdateManifestOperation(project.getName(), model.getClassPathSelection().toString(), true);
     }
 
     protected boolean isReadOnly() {
         return ProjectUtilities.isBinaryProject(project);
     }
 
 }
