 /*******************************************************************************
  * Copyright (c) 2004, 2010 BREDEX GmbH.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     BREDEX GmbH - initial API and implementation and/or initial documentation
  *******************************************************************************/
 package org.eclipse.jubula.client.ui.properties;
 
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.Set;
 
 import javax.persistence.EntityNotFoundException;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.jubula.client.core.businessprocess.ComponentNamesBP;
 import org.eclipse.jubula.client.core.businessprocess.ProjectNameBP;
 import org.eclipse.jubula.client.core.events.DataEventDispatcher;
 import org.eclipse.jubula.client.core.events.DataEventDispatcher.DataState;
 import org.eclipse.jubula.client.core.events.DataEventDispatcher.UpdateState;
 import org.eclipse.jubula.client.core.model.IProjectPO;
 import org.eclipse.jubula.client.core.model.IProjectPropertiesPO;
 import org.eclipse.jubula.client.core.model.IReusedProjectPO;
 import org.eclipse.jubula.client.core.model.PoMaker;
 import org.eclipse.jubula.client.core.persistence.EditSupport;
 import org.eclipse.jubula.client.core.persistence.GeneralStorage;
 import org.eclipse.jubula.client.core.persistence.IncompatibleTypeException;
 import org.eclipse.jubula.client.core.persistence.PMException;
 import org.eclipse.jubula.client.core.persistence.ProjectPM;
 import org.eclipse.jubula.client.ui.Plugin;
 import org.eclipse.jubula.client.ui.constants.ContextHelpIds;
 import org.eclipse.jubula.client.ui.constants.Layout;
 import org.eclipse.jubula.client.ui.controllers.PMExceptionHandler;
 import org.eclipse.jubula.client.ui.factory.ControlFactory;
 import org.eclipse.jubula.client.ui.i18n.Messages;
 import org.eclipse.jubula.client.ui.provider.ControlDecorator;
 import org.eclipse.jubula.client.ui.utils.Utils;
 import org.eclipse.jubula.client.ui.widgets.CheckedIntText;
 import org.eclipse.jubula.client.ui.widgets.CheckedProjectNameText;
 import org.eclipse.jubula.client.ui.widgets.CheckedText;
 import org.eclipse.jubula.client.ui.widgets.DirectCombo;
 import org.eclipse.jubula.client.ui.widgets.JBText;
 import org.eclipse.jubula.tools.constants.StringConstants;
 import org.eclipse.jubula.tools.exception.Assert;
 import org.eclipse.jubula.tools.exception.JBException;
 import org.eclipse.jubula.tools.exception.ProjectDeletedException;
 import org.eclipse.jubula.tools.messagehandling.MessageIDs;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.KeyEvent;
 import org.eclipse.swt.events.KeyListener;
 import org.eclipse.swt.events.ModifyEvent;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Label;
 
 
 /**
  * This is the class for the test data property page of a project.
  *
  * @author BREDEX GmbH
  * @created 08.02.2005
  */
 public class ProjectGeneralPropertyPage extends AbstractProjectPropertyPage {
 
     /**
     * 
     *
      * @author BREDEX GmbH
      * @created Aug 21, 2007
      */
     public interface IOkListener {
         
         /**
          * The OK button has been pressed.
          */
         public void okPressed() throws PMException;
     }
 
 
     /** number of columns = 1 */
     private static final int NUM_COLUMNS_1 = 1; 
     /** number of columns = 2 */
     private static final int NUM_COLUMNS_2 = 2;
 
     /**
      * the logger
      */
     private static Log log = 
         LogFactory.getLog(ProjectGeneralPropertyPage.class);
     
     /** the m_text field for the project name */
     private CheckedText m_projectNameTextField;
     /** the m_isReusable checkbox for if the project is reusable */
     private Button m_isReusableCheckbox;
     /** the m_isProtected checkbox for if the project is protected */
     private Button m_isProtectedCheckbox;
     /** the GDStateController */
     private final WidgetModifyListener m_modifyListener = 
         new WidgetModifyListener();
     /** the GDStateController */
     private final ToolkitComboSelectionListener m_toolkitComboListener = 
         new ToolkitComboSelectionListener();
     /** the old project name */
     private final String m_oldProjectName;
     /** the old project isReusable */
     private final boolean m_oldIsReusable;
     /** the old project isProtected */
     private final boolean m_oldIsProtected;
     /** the project major version number */
     private final Integer m_majorVersionNumber;
     /** the project minor version number */
     private final Integer m_minorVersionNumber;
     /** the Combo to select the toolkit */
     private DirectCombo<String> m_projectToolkitCombo;
     /** the project GUID */
     private String m_projectGuid;
     
     /** the new project name */
     private String m_newProjectName;
     
     /**  Checkbox to decide if testresults should be deleted after specified days */
     private Button m_cleanTestresults = null;
     
     /**  textfield to specify days after which testresults should be deleted after from database */
     private CheckedIntText m_cleanResultDays = null; 
     
     /** set of listeners to be informed when ok has been pressed */
     private Set<IOkListener> m_okListenerList = new HashSet<IOkListener>();
     
     /**
      * @param es the editSupport
      */
     public ProjectGeneralPropertyPage(EditSupport es) {
         super(es);
         IProjectPO project = getProject();
         m_oldProjectName = project.getName();
         m_newProjectName = m_oldProjectName;
         m_oldIsReusable = project.getIsReusable();
         m_oldIsProtected = project.getIsProtected();
         m_majorVersionNumber = project.getMajorProjectVersion();
         m_minorVersionNumber = project.getMinorProjectVersion();
         m_projectGuid = project.getGuid();
     }
 
     /**
      * {@inheritDoc}
      */
     protected Control createContents(Composite parent) {
         Composite composite = createComposite(parent, NUM_COLUMNS_1,
             GridData.FILL, false);
         Composite projectNameComposite = createComposite(composite,
             NUM_COLUMNS_2, GridData.FILL, false);
         noDefaultAndApplyButton();       
 
         createEmptyLabel(projectNameComposite);
         createEmptyLabel(projectNameComposite);
         
         createProjectNameField(projectNameComposite);
         
         createEmptyLabel(projectNameComposite);
         separator(projectNameComposite, NUM_COLUMNS_2); 
         createEmptyLabel(projectNameComposite);
 
         createAutToolKit(projectNameComposite);
         separator(projectNameComposite, NUM_COLUMNS_2);
         createEmptyLabel(projectNameComposite);
         
         createIsReusable(projectNameComposite);
         createIsProtected(projectNameComposite);
         createVersionInfo(projectNameComposite);
         createGuidInfo(projectNameComposite);
         
         separator(projectNameComposite, NUM_COLUMNS_2);
         createEmptyLabel(projectNameComposite);
         
         createCleanTestResults(projectNameComposite);
         Composite innerComposite = new Composite(composite, SWT.NONE);
         GridLayout compositeLayout = new GridLayout();
         compositeLayout.numColumns = NUM_COLUMNS_1;
         compositeLayout.marginHeight = 0;
         compositeLayout.marginWidth = 0;
         innerComposite.setLayout(compositeLayout);
         GridData compositeData = new GridData();
         compositeData.horizontalSpan = NUM_COLUMNS_2;
         compositeData.horizontalAlignment = GridData.FILL;
         compositeData.grabExcessHorizontalSpace = true;
         innerComposite.setLayoutData(compositeData);
 
         addListener();
         Plugin.getHelpSystem().setHelp(parent,
             ContextHelpIds.PROJECT_PROPERTY_PAGE);
         return composite;
     }
 
     /**
      * @param parent the parent composite
      */
     private void createEmptyLabel(Composite parent) {
         createLabel(parent, StringConstants.EMPTY);
     }
 
     /**
      * @param parent the parent composite
      */
     private void createVersionInfo(Composite parent) {
         Composite leftComposite = createComposite(parent, NUM_COLUMNS_1, 
             GridData.BEGINNING, false);
         Composite rightComposite = createComposite(parent, 3, 
             GridData.FILL, true);
         
         createLabel(leftComposite, 
             Messages.ProjectPropertyPageProjectVersion);
         createLabel(rightComposite, m_majorVersionNumber.toString() 
                 + StringConstants.DOT + m_minorVersionNumber);
         Label l = createLabel(rightComposite, StringConstants.EMPTY);
         GridData textGD = new GridData();
         textGD.grabExcessHorizontalSpace = true;
         textGD.horizontalAlignment = GridData.FILL;
         l.setLayoutData(textGD);
 
     }
 
     /**
      * @param parent the parent composite
      */
     private void createGuidInfo(Composite parent) {
         Composite leftComposite = createComposite(parent, NUM_COLUMNS_1, 
             GridData.BEGINNING, false);
         Composite rightComposite = createComposite(parent, 3, 
             GridData.FILL, true);
         
         ControlDecorator.decorateInfo(createLabel(leftComposite, 
             Messages.ProjectPropertyPageProjectGuid), 
            "GDControlDecorator.ProjectPropertiesGUID", false);
         
         JBText projectGuid = new JBText(rightComposite, 
             SWT.READ_ONLY | SWT.BORDER);
         projectGuid.setText(m_projectGuid);
         Label l = createLabel(rightComposite, StringConstants.EMPTY);
         GridData textGD = new GridData();
         textGD.grabExcessHorizontalSpace = true;
         textGD.horizontalAlignment = GridData.FILL;
         l.setLayoutData(textGD);
 
     }
 
     /**
      * @param parent
      *            the parent composite
      */
     private void createIsReusable(Composite parent) {
         Composite leftComposite = createComposite(parent, NUM_COLUMNS_1,
                 GridData.BEGINNING, false);
         Composite rightComposite = createComposite(parent, NUM_COLUMNS_2,
                 GridData.FILL, true);
         ControlDecorator.decorateInfo(createLabel(leftComposite, 
                 Messages.ProjectPropertyPageIsReusable),
                 "GDControlDecorator.NewProjectIsReusable", false); //$NON-NLS-1$
         m_isReusableCheckbox = new Button(rightComposite, SWT.CHECK);
 
         m_isReusableCheckbox.setSelection(m_oldIsReusable);
         m_isReusableCheckbox.addSelectionListener(new SelectionListener() {
             public void widgetSelected(SelectionEvent e) {
                 boolean isReusable = m_isReusableCheckbox.getSelection();
                 if (isReusable) {
                     m_isProtectedCheckbox.setSelection(true);
                 }
             }
 
             public void widgetDefaultSelected(SelectionEvent e) {
                 widgetSelected(e);
             }
         });
     }
     
     /**
      * @param parent
      *            the parent composite
      */
     private void createIsProtected(Composite parent) {
         Composite leftComposite = createComposite(parent, NUM_COLUMNS_1,
                 GridData.BEGINNING, false);
         Composite rightComposite = createComposite(parent, NUM_COLUMNS_2,
                 GridData.FILL, true);
         ControlDecorator.decorateInfo(createLabel(leftComposite, 
                 Messages.ProjectPropertyPageIsProtected),
                 "GDControlDecorator.NewProjectIsProtected", false); //$NON-NLS-1$
         m_isProtectedCheckbox = new Button(rightComposite, SWT.CHECK);
 
         m_isProtectedCheckbox.setSelection(m_oldIsProtected);
 
     }
 
     /**
      * Creates a new composite.
      * @param parent The parent composite.
      * @param numColumns the number of columns for this composite.
      * @param alignment The horizontalAlignment (grabExcess).
      * @param horizontalSpace The horizontalSpace.
      * @return The new composite.
      */
     private Composite createComposite(Composite parent, int numColumns, 
             int alignment, boolean horizontalSpace) {
         
         Composite composite = new Composite(parent, SWT.NONE);
         GridLayout compositeLayout = new GridLayout();
         compositeLayout.numColumns = numColumns;
         compositeLayout.marginHeight = 0;
         compositeLayout.marginWidth = 0;
         composite.setLayout(compositeLayout);
         GridData compositeData = new GridData();
         compositeData.horizontalAlignment = alignment;
         compositeData.grabExcessHorizontalSpace = horizontalSpace;
         composite.setLayoutData(compositeData);
         return composite;       
     }
     
     /**
      * Creates a label for this page.
      * @param text The label text to set.
      * @param parent The composite.
      * @return a new label
      */
     private Label createLabel(Composite parent, String text) {
         Label label = new Label(parent, SWT.NONE);
         label.setText(text);
         GridData labelGrid = new GridData(GridData.BEGINNING, GridData.CENTER, 
             false , false, 1, 1);
         label.setLayoutData(labelGrid);
         return label;
     }
     
     /**
      * Creates a separator line.
      * @param composite The parent composite.
      * @param horSpan The horizonzal span.
      */
     private void separator(Composite composite, int horSpan) {
         createLabel(composite, StringConstants.EMPTY);
         Label sep = new Label(composite, SWT.SEPARATOR | SWT.HORIZONTAL);
         GridData sepData = new GridData();
         sepData.horizontalAlignment = GridData.FILL;
         sepData.horizontalSpan = horSpan;
         sep.setLayoutData(sepData);
         createLabel(composite, StringConstants.EMPTY);
     }
 
     /**
      * Creates the textfield for the project name.
      * 
      * @param parent
      *            The parent composite.
      */
     private void createProjectNameField(Composite parent) {
         Composite leftComposite = createComposite(parent, NUM_COLUMNS_1, 
             GridData.BEGINNING, false);
         Composite rightComposite = createComposite(parent, NUM_COLUMNS_1, 
             GridData.FILL, true);
         createLabel(leftComposite, Messages.ProjectPropertyPageProjectName);
         m_projectNameTextField = new CheckedProjectNameText(rightComposite, 
             SWT.BORDER);
         m_projectNameTextField.setText(getProject().getName());
         GridData textGridData = new GridData();
         textGridData.grabExcessHorizontalSpace = true;
         textGridData.horizontalAlignment = GridData.FILL;
         Layout.addToolTipAndMaxWidth(textGridData, m_projectNameTextField);
         m_projectNameTextField.setLayoutData(textGridData);
         Layout.setMaxChar(m_projectNameTextField);
     }
     
     /**
      * @param parent the parent composite
      */
     private void createAutToolKit(Composite parent) {
         Composite leftComposite = createComposite(parent, NUM_COLUMNS_1, 
             GridData.BEGINNING, false);
         Composite rightComposite = createComposite(parent, NUM_COLUMNS_2, 
             GridData.FILL, true);
         createLabel(leftComposite, Messages.ProjectPropertyPageAutToolKitLabel);
         m_projectToolkitCombo = createToolkitCombo(rightComposite);
         m_projectToolkitCombo.setSelectedObject(getProject().getToolkit());
         GridData textGridData = new GridData();
         textGridData.grabExcessHorizontalSpace = true;
         textGridData.horizontalAlignment = GridData.FILL;
         m_projectToolkitCombo.setLayoutData(textGridData);
         Label l = createLabel(rightComposite, StringConstants.EMPTY);
         GridData txtGridData = new GridData();
         txtGridData.grabExcessHorizontalSpace = true;
         txtGridData.horizontalAlignment = GridData.FILL;
         l.setLayoutData(txtGridData);
     }
     
     /**
      * Creates the toolkit combo.
      * @param parent the parent.
      * @return the toolkit combo.
      */
     private DirectCombo<String> createToolkitCombo(Composite parent) {
         return ControlFactory.createProjectToolkitCombo(parent);
     }
     
     /**
      * 
      */
     protected void enableCleanResultDaysTextfield() {
         m_cleanResultDays.setEnabled(m_cleanTestresults.getSelection());
     }
     
     /**
      * @param parent The parent <code>Composite</code>
      */
     private void createCleanTestResults(Composite parent) {
         m_cleanTestresults = new Button(parent, SWT.CHECK);
         m_cleanTestresults.setText(Messages
                 .TestResultViewPreferencePageCleanResults);
         GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
         gridData.horizontalSpan = 1;
         gridData.grabExcessHorizontalSpace = false;
         m_cleanTestresults.setLayoutData(gridData);
         int testResultCleanupInterval = getProject()
                 .getTestResultCleanupInterval();
         m_cleanTestresults.setSelection(testResultCleanupInterval
                 != IProjectPO.NO_CLEANUP);
         m_cleanTestresults.addSelectionListener(new SelectionListener() {
             public void widgetSelected(SelectionEvent e) {
                 enableCleanResultDaysTextfield();
                 checkCompleteness();
             }
 
             public void widgetDefaultSelected(SelectionEvent e) {
                 // nothing here
             }
         });
         m_cleanResultDays = new CheckedIntText(
                 parent, SWT.BORDER, false, 1, Integer.MAX_VALUE);
         gridData = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
         gridData.horizontalSpan = 1;
         gridData.grabExcessHorizontalSpace = false;
         gridData.widthHint = 80;
         m_cleanResultDays.setLayoutData(gridData);
         if (testResultCleanupInterval > 0) {
             m_cleanResultDays
                     .setText(String.valueOf(testResultCleanupInterval));
         }
         m_cleanResultDays.addKeyListener(new KeyListener() {
             public void keyPressed(KeyEvent e) {
                 // nothing
             }
             public void keyReleased(KeyEvent e) {
                 checkCompleteness();
             }
             
         });
         Label label = new Label(parent, SWT.NONE);
         label.setText(Messages.TestResultViewPreferencePageCleanDaysLabel);
         gridData = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
         gridData.horizontalSpan = 1;
         gridData.grabExcessHorizontalSpace = false;
         label.setLayoutData(gridData);
         enableCleanResultDaysTextfield();
         ControlDecorator.decorateInfo(label,  
                 "TestResultViewPreferencePage.cleanResultsInfo", false); //$NON-NLS-1$
     }
     
     /**
      * Checks if Preference Page is complete and valid
      */
     protected void checkCompleteness() {
         if (m_cleanResultDays.isEnabled() 
                 && m_cleanResultDays.getValue() <= 0) {
             setErrorMessage(Messages
                     .TestResultViewPreferencePageCleanResultDaysEmpty);
             setValid(false);
             return;
         }
         setErrorMessage(null);
         setValid(true);
     }
     
     /**
      * {@inheritDoc}
      */
     public boolean performOk() {
         try {
             if (!m_oldProjectName.equals(m_newProjectName)) {
                 if (ProjectPM.doesProjectNameExist(m_newProjectName)) {
 
                     Utils.createMessageDialog(
                             MessageIDs.E_PROJECTNAME_ALREADY_EXISTS,
                             new Object[] { m_newProjectName }, null);
                     return false;
                 }
             }
             if (m_isReusableCheckbox != null) {
                 getProject().setIsReusable(
                         m_isReusableCheckbox.getSelection());
             }
             if (m_isProtectedCheckbox != null) {
                 getProject().setIsProtected(
                         m_isProtectedCheckbox.getSelection());
             }
             storeAutoTestResultCleanup();
 
             if (!m_oldProjectName.equals(m_newProjectName)) {
                 ProjectNameBP.getInstance().setName(
                         getEditSupport().getSession(), getProject().getGuid(),
                         m_newProjectName);
             }
             fireOkPressed();
             Set<IReusedProjectPO> origReused = 
                 ((IProjectPropertiesPO)getEditSupport().getOriginal())
                     .getUsedProjects();
             Set<IReusedProjectPO> newReused = new HashSet<IReusedProjectPO>(
                 ((IProjectPropertiesPO)getEditSupport().getWorkVersion())
                     .getUsedProjects());
             newReused.removeAll(origReused);
             getEditSupport().saveWorkVersion();
             refreshAutMainList();
             DataEventDispatcher.getInstance().fireProjectPropertiesModified();
             for (IReusedProjectPO reused : newReused) {
                 try {
                     IProjectPO reusedProject = 
                         ProjectPM.loadReusedProject(reused);
                     ComponentNamesBP.getInstance().refreshNames(
                             reusedProject.getId());
                 } catch (JBException e) {
                     // Could not refresh Component Name information for 
                     // reused proejct. Log the exception.
                     log.error(Messages
                             .ErrorWhileRetrievingReusedProjectInformation, e);
                 }
             }
             // FIXME zeb This updates the Test Case Browser. Once we have separate
             //           EditSupports for each property page, then we can use 
             //           "real" ReusedProjectPOs instead of a placeholder.
             DataEventDispatcher.getInstance().fireDataChangedListener(
                     PoMaker.createReusedProjectPO("1", 1, 1), //$NON-NLS-1$
                     DataState.ReuseChanged, UpdateState.notInEditor);
             DataEventDispatcher.getInstance().fireDataChangedListener(
                     GeneralStorage.getInstance().getProject(),
                     DataState.Renamed, UpdateState.notInEditor);
             IProjectPO project = getProject();
             Plugin.setProjectNameInTitlebar(project.getName(), 
                     project.getMajorProjectVersion(), 
                     project.getMinorProjectVersion());
         } catch (PMException e) {
             Utils.createMessageDialog(e, null, null);
         } catch (ProjectDeletedException e) {
             PMExceptionHandler.handleGDProjectDeletedException();
         } catch (IncompatibleTypeException ite) {
             Utils.createMessageDialog(ite, ite.getErrorMessageParams(), null);
         }
 
         return true;
     }
 
     /**
      * store preferences made for auto test result cleanup
      */
     private void storeAutoTestResultCleanup() {
         if (m_cleanResultDays != null) {
             if (m_cleanTestresults != null) {
                 boolean autoClean = m_cleanTestresults.getSelection();
                 if (autoClean) {
                     getProject().setTestResultCleanupInterval(
                             Integer.valueOf(m_cleanResultDays.getText()));
                 } else {
                     getProject().setTestResultCleanupInterval(
                             IProjectPO.NO_CLEANUP);
                 }
             }
         }
     }
 
     /**
      * Notify listeners that OK was pressed.
      */
     private void fireOkPressed() throws PMException {
         for (IOkListener listener : m_okListenerList) {
             listener.okPressed();
         }
     }
 
     /**
      * Refreshes the AutMainList of the Project.
      */
     private void refreshAutMainList() throws ProjectDeletedException {
         try {
             GeneralStorage.getInstance().getMasterSession().refresh(
                 GeneralStorage.getInstance().getProject().getAutCont());
         } catch (EntityNotFoundException enfe) {
             // Occurs if any Object Mapping information has been deleted while
             // the Project Properties were being edited.
             // Refresh the entire master session to ensure that AUT settings
             // and Object Mappings are in sync
             GeneralStorage.getInstance().reloadMasterSession(
                     new NullProgressMonitor());
         }
     }
 
 
     /**
      * Adds necessary listeners.
      */
     private void addListener() {
         m_projectNameTextField.addModifyListener(m_modifyListener);
         m_projectToolkitCombo.addSelectionListener(m_toolkitComboListener);
     }
     
     /** 
      * The action of the project name field.
      * @param isProjectNameVerified True, if the project name was verified.
      * @return false, if the project name field contents an error:
      * the project name starts or end with a blank, or the field is empty
      */
     boolean modifyProjectNameFieldAction(
         boolean isProjectNameVerified) {
         
         boolean isCorrect = true;
         String projectName = m_projectNameTextField.getText();
         int projectNameLength = projectName.length();
         super.getShell().setText(Messages.ProjectPropertyPageShellTitle 
                 + projectName);
         if ((projectNameLength == 0) || (projectName
                 .startsWith(StringConstants.SPACE))
             || (projectName.charAt(projectNameLength - 1) == ' ')) {
             
             isCorrect = false;
         }
         if (isCorrect) {
             setErrorMessage(null);
             setMessage(Messages.PropertiesActionPage1, NONE);
             setValid(true);
             if (isProjectNameVerified) {
                 m_newProjectName = projectName;
             }
             if (ProjectPM.doesProjectNameExist(projectName)
                 && !m_oldProjectName.equals(projectName)) {
                     
                 setErrorMessage(Messages
                         .ProjectSettingWizardPageDoubleProjectName); 
                 isCorrect = false;
                 setValid(false);
             }
         } else {
             if (projectNameLength == 0) {
                 setErrorMessage(Messages.ProjectWizardEmptyProject);
                 setValid(false);
             } else {
                 setErrorMessage(Messages.ProjectWizardNotValidProject);
                 setValid(false);
             }
         }
         return isCorrect;
     }
     
     
     /**
      * This private inner class contains a new ModifyListener.
      * @author BREDEX GmbH
      * @created 11.07.2005
      */
     @SuppressWarnings("synthetic-access")
     private class WidgetModifyListener implements ModifyListener {
         /**
          * {@inheritDoc}
          */
         public void modifyText(ModifyEvent e) {
             Object o = e.getSource();
             if (o.equals(m_projectNameTextField)) {
                 modifyProjectNameFieldAction(true);
                 return;
             }           
         }       
     }
     
     /**
      * This private inner class contains a new SelectionListener.
      * @author BREDEX GmbH
      * @created 10.02.2005
      */
     @SuppressWarnings("synthetic-access")
     private class ToolkitComboSelectionListener implements SelectionListener {
 
         /**
          * {@inheritDoc}
          */
         public void widgetSelected(SelectionEvent e) {
             Object o = e.getSource();
             if (o.equals(m_projectToolkitCombo)) {
                 handleAutToolkitSelection();
                 return;
             }
             Assert.notReached(Messages.EventActivatedUnknownWidget 
                     + StringConstants.DOT);
         }
 
         /**
          * Handles the selection of the autToolkitCombo
          */
         private void handleAutToolkitSelection() {
             final String newToolkit = m_projectToolkitCombo
                 .getSelectedObject();
             final IProjectPO project = getProject();
             project.setToolkit(newToolkit);
         }
 
         /**
          * {@inheritDoc}
          */
         public void widgetDefaultSelected(SelectionEvent e) {
             Assert.notReached(Messages.EventActivatedUnknownWidget 
                     + StringConstants.DOT);
         }        
     }
 
     /**
      * @param toAdd The listener to add. 
      */
     public void addOkListener(IOkListener toAdd) {
         m_okListenerList.add(toAdd);
     }
 
     /**
      * {@inheritDoc}
      */
     public void setVisible(boolean visible) {
         refreshAutToolkitCombo();
         super.setVisible(visible);
     }
 
     /**
      * Refreshes the m_autToolkitCombo.
      */
     private void refreshAutToolkitCombo() {
         final Composite parent = m_projectToolkitCombo.getParent();
         final DirectCombo<String> tmpCombo = createToolkitCombo(parent);
         m_projectToolkitCombo.setItems(tmpCombo.getValues(), Arrays.asList(
             tmpCombo.getItems()));
         tmpCombo.dispose();
         m_projectToolkitCombo.setSelectedObject(getProject().getToolkit());
     }
     
 }
