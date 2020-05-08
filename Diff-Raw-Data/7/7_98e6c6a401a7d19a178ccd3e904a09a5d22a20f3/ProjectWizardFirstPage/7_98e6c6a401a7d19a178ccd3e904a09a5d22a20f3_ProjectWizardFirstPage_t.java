 /*******************************************************************************
  * Copyright (c) 2000, 2006 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.dltk.ui.wizards;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Observable;
 import java.util.Observer;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IWorkspace;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.dltk.core.DLTKCore;
 import org.eclipse.dltk.internal.corext.util.Messages;
 import org.eclipse.dltk.internal.ui.wizards.NewWizardMessages;
 import org.eclipse.dltk.internal.ui.wizards.dialogfields.ComboDialogField;
 import org.eclipse.dltk.internal.ui.wizards.dialogfields.DialogField;
 import org.eclipse.dltk.internal.ui.wizards.dialogfields.IDialogFieldListener;
 import org.eclipse.dltk.internal.ui.wizards.dialogfields.IStringButtonAdapter;
 import org.eclipse.dltk.internal.ui.wizards.dialogfields.LayoutUtil;
 import org.eclipse.dltk.internal.ui.wizards.dialogfields.SelectionButtonDialogField;
 import org.eclipse.dltk.internal.ui.wizards.dialogfields.StringButtonDialogField;
 import org.eclipse.dltk.internal.ui.wizards.dialogfields.StringDialogField;
 import org.eclipse.dltk.launching.IInterpreterInstall;
 import org.eclipse.dltk.launching.IInterpreterInstallType;
 import org.eclipse.dltk.launching.InterpreterStandin;
 import org.eclipse.dltk.launching.ScriptRuntime;
 import org.eclipse.dltk.ui.DLTKUIPlugin;
 import org.eclipse.dltk.ui.PreferenceConstants;
 import org.eclipse.dltk.ui.preferences.NewDLTKProjectPreferencePage;
 import org.eclipse.jface.dialogs.Dialog;
 import org.eclipse.jface.dialogs.IDialogConstants;
 import org.eclipse.jface.wizard.WizardPage;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.DirectoryDialog;
 import org.eclipse.swt.widgets.Group;
 import org.eclipse.swt.widgets.Link;
 import org.eclipse.ui.dialogs.PreferencesUtil;
 
 
 /**
  * The first page of the <code>SimpleProjectWizard</code>.
  */
 public abstract class ProjectWizardFirstPage extends WizardPage {
 	/**
 	 * Request a project name. Fires an event whenever the text field is
 	 * changed, regardless of its content.
 	 */
 	private final class NameGroup extends Observable implements IDialogFieldListener {
 		protected final StringDialogField fNameField;
 
 		public NameGroup(Composite composite, String initialName) {
 			final Composite nameComposite = new Composite(composite, SWT.NONE);
 			nameComposite.setFont(composite.getFont());
 			nameComposite.setLayout(initGridLayout(new GridLayout(2, false), false));
 			nameComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 			// text field for project name
 			fNameField = new StringDialogField();
 			fNameField.setLabelText(NewWizardMessages.ScriptProjectWizardFirstPage_NameGroup_label_text);
 			fNameField.setDialogFieldListener(this);
 			setName(initialName);
 			fNameField.doFillIntoGrid(nameComposite, 2);
 			LayoutUtil.setHorizontalGrabbing(fNameField.getTextControl(null));
 		}
 
 		protected void fireEvent() {
 			setChanged();
 			notifyObservers();
 		}
 
 		public String getName() {
 			return fNameField.getText().trim();
 		}
 
 		public void postSetFocus() {
 			fNameField.postSetFocusOnDialogField(getShell().getDisplay());
 		}
 
 		public void setName(String name) {
 			fNameField.setText(name);
 		}
 		
 		public void dialogFieldChanged(DialogField field) {
 			fireEvent();
 		}
 	}
 	/**
 	 * Request a location. Fires an event whenever the checkbox or the location
 	 * field is changed, regardless of whether the change originates from the
 	 * user or has been invoked programmatically.
 	 */
 	private final class LocationGroup extends Observable implements Observer, IStringButtonAdapter, IDialogFieldListener {
 		protected final SelectionButtonDialogField fWorkspaceRadio;
 		protected final SelectionButtonDialogField fExternalRadio;
 		protected final StringButtonDialogField fLocation;
 		private String fPreviousExternalLocation;
 		private static final String DIALOGSTORE_LAST_EXTERNAL_LOC = DLTKUIPlugin.PLUGIN_ID + ".last.external.project"; //$NON-NLS-1$
 
 		public LocationGroup(Composite composite) {
 			final int numColumns = 3;
 			final Group group = new Group(composite, SWT.NONE);
 			group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 			group.setLayout(initGridLayout(new GridLayout(numColumns, false), true));
 			group.setText(NewWizardMessages.ScriptProjectWizardFirstPage_LocationGroup_title);
 			fWorkspaceRadio = new SelectionButtonDialogField(SWT.RADIO);
 			fWorkspaceRadio.setDialogFieldListener(this);
 			fWorkspaceRadio.setLabelText(NewWizardMessages.ScriptProjectWizardFirstPage_LocationGroup_workspace_desc);
 			fExternalRadio = new SelectionButtonDialogField(SWT.RADIO);
 			fExternalRadio.setLabelText(NewWizardMessages.ScriptProjectWizardFirstPage_LocationGroup_external_desc);
 			fLocation = new StringButtonDialogField(this);
 			fLocation.setDialogFieldListener(this);
 			fLocation.setLabelText(NewWizardMessages.ScriptProjectWizardFirstPage_LocationGroup_locationLabel_desc);
 			fLocation.setButtonLabel(NewWizardMessages.ScriptProjectWizardFirstPage_LocationGroup_browseButton_desc);
 			fExternalRadio.attachDialogField(fLocation);
 			fWorkspaceRadio.setSelection(true);
 			fExternalRadio.setSelection(false);
 			fPreviousExternalLocation = ""; //$NON-NLS-1$
 			fWorkspaceRadio.doFillIntoGrid(group, numColumns);
 			fExternalRadio.doFillIntoGrid(group, numColumns);
 			fLocation.doFillIntoGrid(group, numColumns);
 			LayoutUtil.setHorizontalGrabbing(fLocation.getTextControl(null));
 		}
 
 		protected void fireEvent() {
 			setChanged();
 			notifyObservers();
 		}
 
 		protected String getDefaultPath(String name) {
 			final IPath path = Platform.getLocation().append(name);
 			return path.toOSString();
 		}
 
 		/*
 		 * (non-Javadoc)
 		 * 
 		 * @see java.util.Observer#update(java.util.Observable,
 		 *      java.lang.Object)
 		 */
 		public void update(Observable o, Object arg) {
 			if (isInWorkspace()) {
 				fLocation.setText(getDefaultPath(fNameGroup.getName()));
 			}
 			fireEvent();
 		}
 
 		public IPath getLocation() {
 			if (isInWorkspace()) {
 				return Platform.getLocation();
 			}
 			return Path.fromOSString(fLocation.getText().trim());
 		}
 
 		public boolean isInWorkspace() {
 			return fWorkspaceRadio.isSelected();
 		}
 		
 		public void changeControlPressed(DialogField field) {
 			final DirectoryDialog dialog = new DirectoryDialog(getShell());
 			dialog.setMessage(NewWizardMessages.ScriptProjectWizardFirstPage_directory_message);
 			String directoryName = fLocation.getText().trim();
 			if (directoryName.length() == 0) {
 				String prevLocation = DLTKUIPlugin.getDefault().getDialogSettings().get(DIALOGSTORE_LAST_EXTERNAL_LOC);
 				if (prevLocation != null) {
 					directoryName = prevLocation;
 				}
 			}
 			if (directoryName.length() > 0) {
 				final File path = new File(directoryName);
 				if (path.exists())
 					dialog.setFilterPath(directoryName);
 			}
 			final String selectedDirectory = dialog.open();
 			if (selectedDirectory != null) {
 				fLocation.setText(selectedDirectory);
 				DLTKUIPlugin.getDefault().getDialogSettings().put(DIALOGSTORE_LAST_EXTERNAL_LOC, selectedDirectory);
 			}
 		}
 		
 		public void dialogFieldChanged(DialogField field) {
 			if (field == fWorkspaceRadio) {
 				final boolean checked = fWorkspaceRadio.isSelected();
 				if (checked) {
 					fPreviousExternalLocation = fLocation.getText();
 					fLocation.setText(getDefaultPath(fNameGroup.getName()));
 				} else {
 					fLocation.setText(fPreviousExternalLocation);
 				}
 			}
 			fireEvent();
 		}
 	}
 	/**
 	 * Request a project layout.
 	 */
 	
 	private final class LayoutGroup implements Observer, SelectionListener {
 		private final SelectionButtonDialogField fStdRadio, fSrcBinRadio;
 		private final Group fGroup;
 		private final Link fPreferenceLink;
 
 		public LayoutGroup(Composite composite) {
 			fGroup = new Group(composite, SWT.NONE);
 			fGroup.setFont(composite.getFont());
 			fGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 			fGroup.setLayout(initGridLayout(new GridLayout(3, false), true));
 			fGroup.setText(NewWizardMessages.ScriptProjectWizardFirstPage_LayoutGroup_title);
 			fStdRadio = new SelectionButtonDialogField(SWT.RADIO);
 			fStdRadio.setLabelText(NewWizardMessages.ScriptProjectWizardFirstPage_LayoutGroup_option_oneFolder);
 			fSrcBinRadio = new SelectionButtonDialogField(SWT.RADIO);
 			fSrcBinRadio.setLabelText(NewWizardMessages.ScriptProjectWizardFirstPage_LayoutGroup_option_separateFolders);
 			fStdRadio.doFillIntoGrid(fGroup, 3);
 			LayoutUtil.setHorizontalGrabbing(fStdRadio.getSelectionButton(null));
 			fSrcBinRadio.doFillIntoGrid(fGroup, 2);
 			fPreferenceLink = new Link(fGroup, SWT.NONE);
 			fPreferenceLink.setText(NewWizardMessages.ScriptProjectWizardFirstPage_LayoutGroup_link_description);
 			fPreferenceLink.setLayoutData(new GridData(GridData.END, GridData.END, false, false));
 			fPreferenceLink.addSelectionListener(this);
 			if (DLTKCore.DEBUG) {
 				System.err.println("TOTO: Possible language dependent code here required...");
 			}
 			boolean useSrcBin = DLTKUIPlugin.getDefault().getPreferenceStore().getBoolean(PreferenceConstants.SRCBIN_FOLDERS_IN_NEWPROJ);
 			fSrcBinRadio.setSelection(useSrcBin);
 			fStdRadio.setSelection(!useSrcBin);
 		}
 
 		public void update(Observable o, Object arg) {
 			final boolean detect = fDetectGroup.mustDetect();
 			fStdRadio.setEnabled(!detect);
 			fSrcBinRadio.setEnabled(!detect);
 			fPreferenceLink.setEnabled(!detect);
 			fGroup.setEnabled(!detect);
 		}
 
 		public boolean isSrcBin() {
 			return fSrcBinRadio.isSelected();
 		}
 
 		/*
 		 * (non-Javadoc)
 		 * 
 		 * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
 		 */
 		public void widgetSelected(SelectionEvent e) {
 			widgetDefaultSelected(e);
 		}
 
 		/*
 		 * (non-Javadoc)
 		 * 
 		 * @see org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent)
 		 */
 		public void widgetDefaultSelected(SelectionEvent e) {
 			String id = NewDLTKProjectPreferencePage.ID;
 			PreferencesUtil.createPreferenceDialogOn(getShell(), id, new String[] {
 				id
 			}, null).open();			
 			// fInterpreterEnvironmentGroup.handlePossibleJInterpreterChange();
 			if (supportInterpreter()) {
 				handlePossibleInterpreterChange();
 			}
 		}
 	}
 	
 
 	protected abstract class AbstractInterpreterGroup extends Observable implements Observer, SelectionListener, IDialogFieldListener {
 
 		private final SelectionButtonDialogField fUseDefaultInterpreterEnvironment, fUseProjectInterpreterEnvironment;
 		private final ComboDialogField fInterpreterEnvironmentCombo;
 		private final Group fGroup;
 		private String[] fComplianceLabels;
 		private final Link fPreferenceLink;
 		private IInterpreterInstall[] fInstalledJInterpreters;
 		
 		public AbstractInterpreterGroup(Composite composite) {
 			fGroup= new Group(composite, SWT.NONE);
 			fGroup.setFont(composite.getFont());
 			fGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 			fGroup.setLayout(initGridLayout(new GridLayout(3, false), true));
 			fGroup.setText(NewWizardMessages.ScriptProjectWizardFirstPage_InterpreterEnvironmentGroup_title); 
 						
 			fUseDefaultInterpreterEnvironment= new SelectionButtonDialogField(SWT.RADIO);
 			fUseDefaultInterpreterEnvironment.setLabelText(getDefaultInterpreterLabel());
 			fUseDefaultInterpreterEnvironment.doFillIntoGrid(fGroup, 2);
 			
 			fPreferenceLink= new Link(fGroup, SWT.NONE);
 			fPreferenceLink.setFont(fGroup.getFont());
 			fPreferenceLink.setText(NewWizardMessages.ScriptProjectWizardFirstPage_InterpreterEnvironmentGroup_link_description);
 			fPreferenceLink.setLayoutData(new GridData(GridData.END, GridData.CENTER, false, false));
 			fPreferenceLink.addSelectionListener(this);
 		
 			fUseProjectInterpreterEnvironment= new SelectionButtonDialogField(SWT.RADIO);
 			fUseProjectInterpreterEnvironment.setLabelText(NewWizardMessages.ScriptProjectWizardFirstPage_InterpreterEnvironmentGroup_specific_compliance);
 			fUseProjectInterpreterEnvironment.doFillIntoGrid(fGroup, 1);
 			fUseProjectInterpreterEnvironment.setDialogFieldListener(this);
 						
			fInterpreterEnvironmentCombo= new ComboDialogField(SWT.READ_ONLY);			
 			fillInstalledInterpreterEnvironments(fInterpreterEnvironmentCombo);
 			fInterpreterEnvironmentCombo.setDialogFieldListener(this);
 
 			Combo comboControl= fInterpreterEnvironmentCombo.getComboControl(fGroup);
			GridData gridData = new GridData(GridData.BEGINNING, GridData.CENTER, true, false);
			gridData.minimumWidth = 100;
			comboControl.setLayoutData(gridData); // make sure column 2 is grabing (but no fill)
 			comboControl.setVisibleItemCount(20);
 			
 			DialogField.createEmptySpace(fGroup);
 			
 			fUseDefaultInterpreterEnvironment.setSelection(true);
 			fInterpreterEnvironmentCombo.setEnabled(fUseProjectInterpreterEnvironment.isSelected());
 		}
 
 		private void fillInstalledInterpreterEnvironments(ComboDialogField comboField) {
 			String selectedItem= null;
 			int selectionIndex= -1;
 			if (fUseProjectInterpreterEnvironment.isSelected()) {
 				selectionIndex= comboField.getSelectionIndex();
 				if (selectionIndex != -1) {//paranoia
 					selectedItem= comboField.getItems()[selectionIndex];
 				}
 			}
 			
 			fInstalledJInterpreters= getWorkspaceInterpeters();
 			
 			selectionIndex= -1;//find new index
 			fComplianceLabels= new String[fInstalledJInterpreters.length];
 			for (int i= 0; i < fInstalledJInterpreters.length; i++) {
 				fComplianceLabels[i]= fInstalledJInterpreters[i].getName();
 				if (selectedItem != null && fComplianceLabels[i].equals(selectedItem)) {
 					selectionIndex= i;
 				}				
 			}
 			comboField.setItems(fComplianceLabels);
 			if (selectionIndex == -1) {
 				fInterpreterEnvironmentCombo.selectItem(getDefaultInterpreterName());
 			} else {
 				fInterpreterEnvironmentCombo.selectItem(selectedItem);
 			}		
 			interpretersPresent = (fInstalledJInterpreters.length > 0);
 		}
 		
 		private IInterpreterInstall[] getWorkspaceInterpeters() {
 			List standins = new ArrayList();
 			IInterpreterInstallType[] types = ScriptRuntime.getInterpreterInstallTypes(getCurrentLanguageNature ());
 			for (int i = 0; i < types.length; i++) {
 				IInterpreterInstallType type = types[i];
 				IInterpreterInstall[] installs = type.getInterpreterInstalls();
 				for (int j = 0; j < installs.length; j++) {
 					IInterpreterInstall install = installs[j];
 					standins.add(new InterpreterStandin(install));
 				}
 			}
 			return ((IInterpreterInstall[])standins.toArray(new IInterpreterInstall[standins.size()]));	
 		}
 
 		private String getDefaultInterpreterName() {
 			IInterpreterInstall inst = ScriptRuntime.getDefaultInterpreterInstall(getCurrentLanguageNature ());
 			if (inst != null)
 				return inst.getName();
 			else
 				return "undefined";
 		}
 
 		private String getDefaultInterpreterLabel() {
 			return Messages.format(NewWizardMessages.ScriptProjectWizardFirstPage_InterpreterEnvironmentGroup_default_compliance, getDefaultInterpreterName());
 		}
 
 		public void update(Observable o, Object arg) {
 			updateEnableState();
 		}
 
 		private void updateEnableState() {
 			if (fDetectGroup == null)
 				return;
 			final boolean detect= fDetectGroup.mustDetect();
 			fUseDefaultInterpreterEnvironment.setEnabled(!detect);
 			fUseProjectInterpreterEnvironment.setEnabled(!detect);
 			fInterpreterEnvironmentCombo.setEnabled(!detect && fUseProjectInterpreterEnvironment.isSelected());
 			fPreferenceLink.setEnabled(!detect);
 			fGroup.setEnabled(!detect);
 		}
 		
 		/* (non-Javadoc)
 		 * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
 		 */
 		public void widgetSelected(SelectionEvent e) {
 			widgetDefaultSelected(e);
 		}
 		
 		/**
 		 * Shows window with appropriate language preference page.
 		 *
 		 */
 		protected abstract void showInterpreterPreferencePage ();
 		
 		protected abstract String getCurrentLanguageNature ();
 
 		/* (non-Javadoc)
 		 * @see org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent)
 		 */
 		public void widgetDefaultSelected(SelectionEvent e) {
 			showInterpreterPreferencePage();			
 			handlePossibleInterpreterChange();
 			//fDetectGroup.handlePossibleJInterpreterChange();
 		}
 		
 		public void handlePossibleInterpreterChange() {
 			fUseDefaultInterpreterEnvironment.setLabelText(getDefaultInterpreterLabel());
 			fillInstalledInterpreterEnvironments(fInterpreterEnvironmentCombo);
 			setChanged();
 			notifyObservers();
 		}
 	
 		public void dialogFieldChanged(DialogField field) {
 			updateEnableState();			
 			//fDetectGroup.handlePossibleInterpreterChange();
 		}
 		
 		public boolean isUseSpecific() {
 			return fUseProjectInterpreterEnvironment.isSelected();
 		}
 		
 		public IInterpreterInstall getSelectedJInterpreter() {
 			if (fUseProjectInterpreterEnvironment.isSelected()) {
 				int index= fInterpreterEnvironmentCombo.getSelectionIndex();
 				if (index >= 0 && index < fComplianceLabels.length) { // paranoia
 					return fInstalledJInterpreters[index];
 				}
 			}
 			return null;
 		}
 				
 	}	
 	
 	
 	//	
 	// //TODO: Add
 	// static {
 	// if (DLTKCore.DEBUG) {
 	// System.err.println("TODO: Add to DLTKProjectWizardFirstPage");
 	// }
 	// }
 	/**
 	 * Show a warning when the project location contains files.
 	 */
 	private final class DetectGroup extends Observable implements Observer, SelectionListener {
 		private final Link fHintText;
 		private boolean fDetect;
 
 		public DetectGroup(Composite composite) {
 			Link InterpreterEnvironment50Text = new Link(composite, SWT.WRAP);
 			InterpreterEnvironment50Text.setFont(composite.getFont());
 			InterpreterEnvironment50Text.addSelectionListener(this);
 			GridData gridData = new GridData(GridData.FILL, SWT.FILL, true, true);
 			gridData.widthHint = convertWidthInCharsToPixels(50);
 			InterpreterEnvironment50Text.setLayoutData(gridData);
 			fHintText = InterpreterEnvironment50Text;
 			if (supportInterpreter()) {
 				handlePossibleInterpreterChange();
 			}
 		}
 
 		public void update(Observable o, Object arg) {
 			if (o instanceof LocationGroup) {
 				boolean oldDetectState = fDetect;
 				if (fLocationGroup.isInWorkspace()) {
 					String name = getProjectName();
 					if (name.length() == 0 || DLTKUIPlugin.getWorkspace().getRoot().findMember(name) != null) {
 						fDetect = false;
 					} else {
 						final File directory = fLocationGroup.getLocation().append(getProjectName()).toFile();
 						fDetect = directory.isDirectory();
 					}
 				} else {
 					final File directory = fLocationGroup.getLocation().toFile();
 					fDetect = directory.isDirectory();
 				}
 				if (oldDetectState != fDetect) {
 					setChanged();
 					notifyObservers();
 					if (fDetect) {
 						fHintText.setVisible(true);
 						fHintText.setText(NewWizardMessages.ScriptProjectWizardFirstPage_DetectGroup_message);
 					} else {
 						if (supportInterpreter()) {
 							handlePossibleInterpreterChange();
 						}
 					}
 				}
 			}
 		}		
 
 		public boolean mustDetect() {
 			return fDetect;
 		}
 
 		/*
 		 * (non-Javadoc)
 		 * 
 		 * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
 		 */
 		public void widgetSelected(SelectionEvent e) {
 			widgetDefaultSelected(e);
 		}
 
 		/*
 		 * (non-Javadoc)
 		 * 
 		 * @see org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent)
 		 */
 		public void widgetDefaultSelected(SelectionEvent e) {
 			if (DLTKCore.DEBUG) {
 				System.err.println("DetectGroup show compilancePreferencePage...");
 			}
 			// String InterpreterEnvironmentID= BuildPathSupport.InterpreterEnvironment_PREF_PAGE_ID;
 			// String complianceId= CompliancePreferencePage.PREF_ID;
 			// Map data= new HashMap();
 			// data.put(PropertyAndPreferencePage.DATA_NO_LINK, Boolean.TRUE);
 			// PreferencesUtil.createPreferenceDialogOn(getShell(),
 			// complianceId, new String[] { InterpreterEnvironmentID, complianceId }, data).open();
 			// fInterpreterEnvironmentGroup.handlePossibleJInterpreterChange();
 			// handlePossibleJInterpreterChange();
 			if (supportInterpreter()) {
 				handlePossibleInterpreterChange();
 			}
 		}
 	}
 	/**
 	 * Validate this page and show appropriate warnings and error
 	 * NewWizardMessages.
 	 */
 	private final class Validator implements Observer {
 		public void update(Observable o, Object arg) {
 			final IWorkspace workspace = DLTKUIPlugin.getWorkspace();
 			final String name = fNameGroup.getName();
 			// check whether the project name field is empty
 			if (name.length() == 0) {
 				setErrorMessage(null);
 				setMessage(NewWizardMessages.ScriptProjectWizardFirstPage_Message_enterProjectName);
 				setPageComplete(false);
 				return;
 			}
 			// check whether the project name is valid
 			final IStatus nameStatus = workspace.validateName(name, IResource.PROJECT);
 			if (!nameStatus.isOK()) {
 				setErrorMessage(nameStatus.getMessage());
 				setPageComplete(false);
 				return;
 			}
 			// check whether project already exists
 			final IProject handle = getProjectHandle();
 			if (handle.exists()) {
 				setErrorMessage(NewWizardMessages.ScriptProjectWizardFirstPage_Message_projectAlreadyExists);
 				setPageComplete(false);
 				return;
 			}
 			final String location = fLocationGroup.getLocation().toOSString();
 			// check whether location is empty
 			if (location.length() == 0) {
 				setErrorMessage(null);
 				setMessage(NewWizardMessages.ScriptProjectWizardFirstPage_Message_enterLocation);
 				setPageComplete(false);
 				return;
 			}
 			// check whether the location is a syntactically correct path
 			if (!Path.EMPTY.isValidPath(location)) {
 				setErrorMessage(NewWizardMessages.ScriptProjectWizardFirstPage_Message_invalidDirectory);
 				setPageComplete(false);
 				return;
 			}
 			// check whether the location has the workspace as prefix
 			IPath projectPath = Path.fromOSString(location);
 			if (!fLocationGroup.isInWorkspace() && Platform.getLocation().isPrefixOf(projectPath)) {
 				setErrorMessage(NewWizardMessages.ScriptProjectWizardFirstPage_Message_cannotCreateInWorkspace);
 				setPageComplete(false);
 				return;
 			}
 			// If we do not place the contents in the workspace validate the
 			// location.
 			if (!fLocationGroup.isInWorkspace()) {
 				final IStatus locationStatus = workspace.validateProjectLocation(handle, projectPath);
 				if (!locationStatus.isOK()) {
 					setErrorMessage(locationStatus.getMessage());
 					setPageComplete(false);
 					return;
 				}
 			}
 			if (supportInterpreter() && interpeterRequired()) {
 				if (!interpretersPresent) {
 					setErrorMessage("Please configure interpreters");
 					setPageComplete(false);
 					return;
 				}
 			}
 			setPageComplete(true);
 			setErrorMessage(null);
 			setMessage(null);
 		}
 	}
 	private NameGroup fNameGroup;
 	private LocationGroup fLocationGroup;
 	// private LayoutGroup fLayoutGroup;
 	// private InterpreterEnvironmentGroup fInterpreterEnvironmentGroup;
 	private boolean interpretersPresent;
 	private DetectGroup fDetectGroup;
 	private Validator fValidator;
 	private String fInitialName;
 	private static final String PAGE_NAME = NewWizardMessages.ScriptProjectWizardFirstPage_page_pageName;
 
 	/**
 	 * Create a new <code>SimpleProjectFirstPage</code>.
 	 */
 	public ProjectWizardFirstPage() {
 		super(PAGE_NAME);
 		setPageComplete(false);
 		setTitle(NewWizardMessages.ScriptProjectWizardFirstPage_page_title);
 		setDescription(NewWizardMessages.ScriptProjectWizardFirstPage_page_description);
 		fInitialName = ""; //$NON-NLS-1$
 		// initializeDefaultInterpreter();
 	}
 
 	// private void initializeDefaultInterpreter() {
 	// scriptRuntime.getDefaultInterpreterInstall();
 	// }
 	
 	
 	public void setName(String name) {
 		fInitialName = name;
 		if (fNameGroup != null) {
 			fNameGroup.setName(name);
 		}
 	}
 	
 	protected abstract boolean interpeterRequired();
 	
 	protected abstract boolean supportInterpreter();
 
 	protected abstract void createInterpreterGroup(Composite parent);
 		
 	protected abstract void handlePossibleInterpreterChange();
 	
 	protected abstract Observable getInterpreterGroupObservable();
 	
 	protected abstract IInterpreterInstall getInterpreter();
 
 	public void createControl(Composite parent) {
 		initializeDialogUnits(parent);
 		final Composite composite = new Composite(parent, SWT.NULL);
 		composite.setFont(parent.getFont());
 		composite.setLayout(initGridLayout(new GridLayout(1, false), true));
 		composite.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
 		// create UI elements
 		fNameGroup = new NameGroup(composite, fInitialName);
 		fLocationGroup = new LocationGroup(composite);
 		
 		// fInterpreterEnvironmentGroup= new InterpreterEnvironmentGroup(composite);
 		ProjectWizardFirstPage.AbstractInterpreterGroup interpGroup = null;
 		if (supportInterpreter()) {
 			createInterpreterGroup(composite);
 		}
 		// fLayoutGroup= new LayoutGroup(composite);
 		fDetectGroup = new DetectGroup(composite);
 		// establish connections
 		fNameGroup.addObserver(fLocationGroup);
 		// fDetectGroup.addObserver(fLayoutGroup);
 		// fDetectGroup.addObserver(fInterpreterEnvironmentGroup);
 
 		fLocationGroup.addObserver(fDetectGroup);
 		// initialize all elements
 		fNameGroup.notifyObservers();
 		// create and connect validator
 		fValidator = new Validator();
 		Observable interpreterGroupObservable = getInterpreterGroupObservable();
 		if( supportInterpreter() && interpreterGroupObservable != null) {
 //			fDetectGroup.addObserver(getInterpreterGroupObservable());
 			interpreterGroupObservable.addObserver(fValidator);
 			handlePossibleInterpreterChange();
 		}
 		fNameGroup.addObserver(fValidator);
 		fLocationGroup.addObserver(fValidator);
 		
 		setControl(composite);
 		Dialog.applyDialogFont(composite);
 		if (DLTKCore.DEBUG) {
 			System.err.println("Add help support here...");
 		}
 		// PlatformUI.getWorkbench().getHelpSystem().setHelp(composite,
 		// IDLTKHelpContextIds.NEW_JAVAPROJECT_WIZARD_PAGE);
 	}
 
 	/**
 	 * Returns the current project location path as entered by the user, or its
 	 * anticipated initial value. Note that if the default has been returned the
 	 * path in a project description used to create a project should not be set.
 	 * <p>
 	 * TODO At some point this method has to be converted to return an URI
 	 * instead of an path. However, this first requires support from Platform/UI
 	 * to specify a project location different than in a local file system.
 	 * </p>
 	 * 
 	 * @return the project location path or its anticipated initial value.
 	 */
 	public IPath getLocationPath() {
 		return fLocationGroup.getLocation();
 	}
 
 	/**
 	 * Creates a project resource handle for the current project name field
 	 * value.
 	 * <p>
 	 * This method does not create the project resource; this is the
 	 * responsibility of <code>IProject::create</code> invoked by the new
 	 * project resource wizard.
 	 * </p>
 	 * 
 	 * @return the new project resource handle
 	 */
 	public IProject getProjectHandle() {
 		return ResourcesPlugin.getWorkspace().getRoot().getProject(fNameGroup.getName());
 	}
 
 	public boolean isInWorkspace() {
 		return fLocationGroup.isInWorkspace();
 	}
 
 	public String getProjectName() {
 		return fNameGroup.getName();
 	}
 
 	public boolean getDetect() {
 		return fDetectGroup.mustDetect();
 	}
 
 	public boolean isSrc() {
 		return false;
 		// return true;//fLayoutGroup.isSrcBin();
 	}
 
 	/*
 	 * see @DialogPage.setVisible(boolean)
 	 */
 	public void setVisible(boolean visible) {
 		super.setVisible(visible);
 		if (visible) {
 			fNameGroup.postSetFocus();
 		}
 	}
 
 	/**
 	 * Initialize a grid layout with the default Dialog settings.
 	 */
 	protected GridLayout initGridLayout(GridLayout layout, boolean margins) {
 		layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
 		layout.verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
 		if (margins) {
 			layout.marginWidth = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
 			layout.marginHeight = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
 		} else {
 			layout.marginWidth = 0;
 			layout.marginHeight = 0;
 		}
 		return layout;
 	}
 
 	/**
 	 * Set the layout data for a button.
 	 */
 	protected GridData setButtonLayoutData(Button button) {
 		return super.setButtonLayoutData(button);
 	}
 }
