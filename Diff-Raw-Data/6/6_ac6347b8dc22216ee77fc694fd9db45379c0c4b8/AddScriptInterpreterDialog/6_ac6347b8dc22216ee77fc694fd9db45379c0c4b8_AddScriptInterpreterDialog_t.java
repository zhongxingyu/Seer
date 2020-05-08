 /*******************************************************************************
  * Copyright (c) 2005, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.internal.debug.ui.interpreters;
 
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.dltk.core.environment.IEnvironment;
 import org.eclipse.dltk.core.environment.IFileHandle;
 import org.eclipse.dltk.debug.ui.DLTKDebugUIPlugin;
 import org.eclipse.dltk.internal.ui.dialogs.StatusInfo;
 import org.eclipse.dltk.internal.ui.wizards.dialogfields.ComboDialogField;
 import org.eclipse.dltk.internal.ui.wizards.dialogfields.DialogField;
 import org.eclipse.dltk.internal.ui.wizards.dialogfields.IDialogFieldListener;
 import org.eclipse.dltk.internal.ui.wizards.dialogfields.IStringButtonAdapter;
 import org.eclipse.dltk.internal.ui.wizards.dialogfields.StringButtonDialogField;
 import org.eclipse.dltk.internal.ui.wizards.dialogfields.StringDialogField;
 import org.eclipse.dltk.launching.EnvironmentVariable;
 import org.eclipse.dltk.launching.IInterpreterInstall;
 import org.eclipse.dltk.launching.IInterpreterInstallType;
 import org.eclipse.dltk.launching.InterpreterStandin;
 import org.eclipse.dltk.ui.environment.IEnvironmentUI;
 import org.eclipse.dltk.utils.PlatformFileUtils;
 import org.eclipse.jface.dialogs.IDialogConstants;
 import org.eclipse.jface.dialogs.IDialogSettings;
 import org.eclipse.jface.dialogs.StatusDialog;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.BusyIndicator;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Text;
 
 public abstract class AddScriptInterpreterDialog extends StatusDialog {
 
 	private IAddInterpreterDialogRequestor fRequestor;
 
 	private IInterpreterInstallType[] fInterpreterTypes;
 
 	private IInterpreterInstallType fSelectedInterpreterType;
 
 	private ComboDialogField fInterpreterTypeCombo;
 
 	private IInterpreterInstall fEditedInterpreter;
 
 	private AbstractInterpreterLibraryBlock fLibraryBlock;
 	private AbstractInterpreterEnvironmentVariablesBlock fEnvironmentVariablesBlock;
 
 	private StringButtonDialogField fInterpreterPath;
 
 	private StringDialogField fInterpreterName;
 
 	private StringDialogField fInterpreterArgs;
 
 	private IStatus[] fStati;
 	private int fPrevIndex = -1;
 
 	private IEnvironment environment;
 
 	protected boolean useInterpreterArgs() {
 		return true;
 	}
 
 	public AddScriptInterpreterDialog(IAddInterpreterDialogRequestor requestor,
 			Shell shell, IInterpreterInstallType[] interpreterInstallTypes,
 			IInterpreterInstall editedInterpreter) {
 		super(shell);
 		setShellStyle(getShellStyle() | SWT.RESIZE);
 		fRequestor = requestor;
 		fStati = new IStatus[5];
 		for (int i = 0; i < fStati.length; i++) {
 			fStati[i] = new StatusInfo();
 		}
 
 		fInterpreterTypes = interpreterInstallTypes;
 		fSelectedInterpreterType = editedInterpreter != null ? editedInterpreter
 				.getInterpreterInstallType()
 				: interpreterInstallTypes[0];
 
 		fEditedInterpreter = editedInterpreter;
 	}
 
 	/**
 	 * @see Windows#configureShell
 	 */
 	protected void configureShell(Shell newShell) {
 		super.configureShell(newShell);
 		// PlatformUI.getWorkbench().getHelpSystem().setHelp(newShell,
 		// IScriptDebugHelpContextIds.EDIT_InterpreterEnvironment_DIALOG);
 	}
 
 	protected void createDialogFields() {
 		fInterpreterTypeCombo = new ComboDialogField(SWT.READ_ONLY);
 		fInterpreterTypeCombo
 				.setLabelText(InterpretersMessages.addInterpreterDialog_InterpreterEnvironmentType);
 		fInterpreterTypeCombo.setItems(getInterpreterTypeNames());
 
 		fInterpreterName = new StringDialogField();
 		fInterpreterName
 				.setLabelText(InterpretersMessages.addInterpreterDialog_InterpreterEnvironmentName);
 
 		fInterpreterPath = new StringButtonDialogField(
 				new IStringButtonAdapter() {
 					public void changeControlPressed(DialogField field) {
 						browseForInstallation();
 					}
 				});
 		fInterpreterPath
 				.setLabelText(InterpretersMessages.addInterpreterDialog_InterpreterExecutableName);
 		fInterpreterPath
 				.setButtonLabel(InterpretersMessages.addInterpreterDialog_browse1);
 
 		if (this.useInterpreterArgs()) {
 			fInterpreterArgs = new StringDialogField();
 			fInterpreterArgs
 					.setLabelText(InterpretersMessages.AddInterpreterDialog_iArgs);
 		}
 	}
 
 	protected void createFieldListeners() {
 
 		fInterpreterTypeCombo
 				.setDialogFieldListener(new IDialogFieldListener() {
 					public void dialogFieldChanged(DialogField field) {
 						updateInterpreterType();
 					}
 				});
 
 		fInterpreterName.setDialogFieldListener(new IDialogFieldListener() {
 			public void dialogFieldChanged(DialogField field) {
 				setInterpreterNameStatus(validateInterpreterName());
 				updateStatusLine();
 			}
 		});
 
 		fInterpreterPath.setDialogFieldListener(new IDialogFieldListener() {
 			public void dialogFieldChanged(DialogField field) {
 				setInterpreterLocationStatus(validateInterpreterLocation());
 				updateStatusLine();
 			}
 		});
 
 	}
 
 	protected String getInterpreterName() {
 		return fInterpreterName.getText();
 	}
 
 	// protected File getInstallLocation() {
 	// return new File(fInterpreterPath.getText()).getAbsoluteFile();
 	// }
 
 	protected abstract AbstractInterpreterLibraryBlock createLibraryBlock(
 			AddScriptInterpreterDialog dialog);
 
 	protected AbstractInterpreterEnvironmentVariablesBlock createEnvironmentVariablesBlock(
 			AddScriptInterpreterDialog dialog) {
 		return null;
 	}
 
 	protected Control createDialogArea(Composite ancestor) {
 		createDialogFields();
 		Composite parent = (Composite) super.createDialogArea(ancestor);
 		((GridLayout) parent.getLayout()).numColumns = 3;
 
 		fInterpreterTypeCombo.doFillIntoGrid(parent, 3);
 		((GridData) fInterpreterTypeCombo.getComboControl(null).getLayoutData()).widthHint = convertWidthInCharsToPixels(50);
 
 		fInterpreterName.doFillIntoGrid(parent, 3);
 
 		fInterpreterPath.doFillIntoGrid(parent, 3);
 
 		if (this.useInterpreterArgs()) {
 			fInterpreterArgs.doFillIntoGrid(parent, 3);
 			((GridData) fInterpreterArgs.getTextControl(null).getLayoutData()).widthHint = convertWidthInCharsToPixels(50);
 		}
 
 		Label l = new Label(parent, SWT.NONE);
 		l
 				.setText(InterpretersMessages.AddInterpreterDialog_Interpreter_system_libraries__1);
 		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
 		gd.horizontalSpan = 3;
 		l.setLayoutData(gd);
 
 		fLibraryBlock = createLibraryBlock(this);
 		Control block = fLibraryBlock.createControl(parent);
 		gd = new GridData(GridData.FILL_BOTH);
 		gd.horizontalSpan = 3;
 		block.setLayoutData(gd);
 
 		l = new Label(parent, SWT.NONE);
 		l
 				.setText(InterpretersMessages.AddScriptInterpreterDialog_interpreterEnvironmentVariables);
 		gd = new GridData(GridData.FILL_HORIZONTAL);
 		gd.horizontalSpan = 3;
 		l.setLayoutData(gd);
 		fEnvironmentVariablesBlock = createEnvironmentVariablesBlock(this);
 		if (fEnvironmentVariablesBlock != null) {
 			block = fEnvironmentVariablesBlock.createControl(parent);
 			gd = new GridData(GridData.FILL_BOTH);
 			gd.horizontalSpan = 3;
 			block.setLayoutData(gd);
 		} else {
 			l.dispose();
 		}
 
 		Text t = fInterpreterPath.getTextControl(parent);
 		gd = (GridData) t.getLayoutData();
 		gd.grabExcessHorizontalSpace = true;
 		gd.widthHint = convertWidthInCharsToPixels(50);
 
 		initializeFields();
 		createFieldListeners();
 		applyDialogFont(parent);
 		return parent;
 	}
 
 	private void updateInterpreterType() {
 		int selIndex = fInterpreterTypeCombo.getSelectionIndex();
 		if (selIndex == fPrevIndex) {
 			return;
 		}
 		fPrevIndex = selIndex;
 		if (selIndex >= 0 && selIndex < fInterpreterTypes.length) {
 			fSelectedInterpreterType = fInterpreterTypes[selIndex];
 		}
 		setInterpreterLocationStatus(validateInterpreterLocation());
 		fLibraryBlock.initializeFrom(fEditedInterpreter,
 				fSelectedInterpreterType);
 		if (fEnvironmentVariablesBlock != null) {
 			fEnvironmentVariablesBlock.initializeFrom(fEditedInterpreter,
 					fSelectedInterpreterType);
 		}
 		updateStatusLine();
 	}
 
 	public void create() {
 		super.create();
 		fInterpreterName.setFocus();
 		selectInterpreterType();
 	}
 
 	private String[] getInterpreterTypeNames() {
 		String[] names = new String[fInterpreterTypes.length];
 		for (int i = 0; i < fInterpreterTypes.length; i++) {
 			names[i] = fInterpreterTypes[i].getName();
 		}
 		return names;
 	}
 
 	private void selectInterpreterType() {
 		for (int i = 0; i < fInterpreterTypes.length; i++) {
 			if (fSelectedInterpreterType == fInterpreterTypes[i]) {
 				fInterpreterTypeCombo.selectItem(i);
 				return;
 			}
 		}
 	}
 
 	private void initializeFields() {
 		fInterpreterTypeCombo.setItems(getInterpreterTypeNames());
 		if (fEditedInterpreter == null) {
 			fInterpreterName.setText(""); //$NON-NLS-1$
 			fInterpreterPath.setText(""); //$NON-NLS-1$
 			fLibraryBlock.initializeFrom(null, fSelectedInterpreterType);
 			if (fEnvironmentVariablesBlock != null) {
 				fEnvironmentVariablesBlock.initializeFrom(null,
 						fSelectedInterpreterType);
 			}
 			if (this.useInterpreterArgs()) {
 				fInterpreterArgs.setText(""); //$NON-NLS-1$
 			}
 		} else {
 			fInterpreterTypeCombo.setEnabled(false);
 			fInterpreterName.setText(fEditedInterpreter.getName());
 			fInterpreterPath.setText(fEditedInterpreter.getRawInstallLocation()
 					.toOSString());
 			if (fEnvironmentVariablesBlock != null) {
 				fEnvironmentVariablesBlock.initializeFrom(fEditedInterpreter,
 						fSelectedInterpreterType);
 			}
 			fLibraryBlock.initializeFrom(fEditedInterpreter,
 					fSelectedInterpreterType);
 			String InterpreterArgs = fEditedInterpreter.getInterpreterArgs();
 			if (InterpreterArgs != null) {
 				fInterpreterArgs.setText(InterpreterArgs);
 			}
 		}
 		setInterpreterNameStatus(validateInterpreterName());
 		updateStatusLine();
 	}
 
 	private IInterpreterInstallType getInterpreterType() {
 		return fSelectedInterpreterType;
 	}
 
 	IStatus validateInterpreterLocation() {
 		IEnvironment selectedEnv = getEnvironment();
 		String locationName = fInterpreterPath.getText();
 		IStatus s = null;
 		IFileHandle file = null;
 		if (locationName.length() == 0) {
 			s = new StatusInfo(IStatus.INFO,
 					InterpretersMessages.addInterpreterDialog_enterLocation);
 		} else {
 			file = PlatformFileUtils.findAbsoluteOrEclipseRelativeFile(
 					selectedEnv, new Path(locationName));
 			if (!file.exists()) {
 				s = new StatusInfo(
 						IStatus.ERROR,
 						InterpretersMessages.addInterpreterDialog_locationNotExists);
 			} else {
 				final IStatus[] temp = new IStatus[1];
 				final IFileHandle tempFile = file;
 				Runnable r = new Runnable() {
 					/**
 					 * @see java.lang.Runnable#run()
 					 */
 					public void run() {
 						temp[0] = getInterpreterType().validateInstallLocation(
 								tempFile);
 					}
 				};
 				BusyIndicator.showWhile(getShell().getDisplay(), r);
 				s = temp[0];
 			}
 		}
 		if (s.isOK()) {
 			fLibraryBlock.setHomeDirectory(file);
 
 			String name = fInterpreterName.getText();
			if ((name == null || name.trim().length() == 0) && file != null) {
 				// auto-generate interpreter name
 				String genName = null;
 				IPath path = new Path(file.getCanonicalPath());
 				int segs = path.segmentCount();
 				if (segs == 1) {
 					genName = path.segment(0);
 				} else if (segs >= 2) {
 					String last = path.lastSegment();
 					genName = last;
 				}
 				// Add number if interpreter with such name already exists.
 				String pName = genName;
 				int index = 0;
 				while (true) {
 					boolean found = false;
 					for (int i = 0; i < this.fInterpreterTypes.length; i++) {
 						IInterpreterInstallType type = this.fInterpreterTypes[i];
						IInterpreterInstall inst = type.findInterpreterInstallByName(pName);
						if (inst != null) {
 							pName = genName + "(" + String.valueOf(++index) + ")";
 							found = true;
 							break;
 						}
 					}
 					if( !found ) {
 						break;
 					}
 				}
 				if (pName != null) {
 					fInterpreterName.setText(pName);
 				}
 			}
 		} else {
 			fLibraryBlock.setHomeDirectory(null);
 		}
 		fLibraryBlock.restoreDefaultLibraries();
 		// if (fEnvironmentVariablesBlock != null) {
 		// fEnvironmentVariablesBlock.restoreDefaultVariables();
 		// }
 		return s;
 	}
 
 	public IEnvironment getEnvironment() {
 		return this.environment;
 	}
 
 	public void setEnvironment(IEnvironment environment) {
 		this.environment = environment;
 	}
 
 	private IStatus validateInterpreterName() {
 		StatusInfo status = new StatusInfo();
 		String name = fInterpreterName.getText();
 		if (name == null || name.trim().length() == 0) {
 			status.setInfo(InterpretersMessages.addInterpreterDialog_enterName);
 		} else {
 			if (fRequestor.isDuplicateName(name)
 					&& (fEditedInterpreter == null || !name
 							.equals(fEditedInterpreter.getName()))) {
 				status
 						.setError(InterpretersMessages.addInterpreterDialog_duplicateName);
 			}
 		}
 		return status;
 	}
 
 	public void updateStatusLine() {
 		IStatus max = null;
 		for (int i = 0; i < fStati.length; i++) {
 			IStatus curr = fStati[i];
 			if (curr.matches(IStatus.ERROR)) {
 				updateStatus(curr);
 				return;
 			}
 			if (max == null || curr.getSeverity() > max.getSeverity()) {
 				max = curr;
 			}
 		}
 		updateStatus(max);
 	}
 
 	private void browseForInstallation() {
 		IEnvironment environment = getEnvironment();
 		IEnvironmentUI environmentUI = (IEnvironmentUI) environment
 				.getAdapter(IEnvironmentUI.class);
 		if (environmentUI != null) {
 			String newPath = environmentUI.selectFile(getShell(),
 					IEnvironmentUI.EXECUTABLE);
 			if (newPath != null) {
 				fInterpreterPath.setText(newPath);
 			}
 		}
 	}
 
 	protected void okPressed() {
 		doOkPressed();
 		super.okPressed();
 	}
 
 	private IInterpreterInstall lastInstall = null;
 
 	private void doOkPressed() {
 		if (fEditedInterpreter == null) {
 			IInterpreterInstall install = new InterpreterStandin(
 					fSelectedInterpreterType,
 					createUniqueId(fSelectedInterpreterType));
 			setFieldValuesToInterpreter(install);
 			fRequestor.interpreterAdded(install);
 			lastInstall = install;
 		} else {
 			setFieldValuesToInterpreter(fEditedInterpreter);
 			lastInstall = fEditedInterpreter;
 		}
 	}
 
 	private String createUniqueId(IInterpreterInstallType InterpreterType) {
 		String id = null;
 		do {
 			id = String.valueOf(System.currentTimeMillis());
 		} while (InterpreterType.findInterpreterInstall(id) != null);
 		return id;
 	}
 
 	protected void setFieldValuesToInterpreter(IInterpreterInstall install) {
 		IEnvironment selectedEnv = getEnvironment();
 		IFileHandle dir = selectedEnv.getFile(new Path(fInterpreterPath
 				.getText()));
 		install.setInstallLocation(dir);
 		install.setName(fInterpreterName.getText());
 
 		if (this.useInterpreterArgs()) {
 			String argString = fInterpreterArgs.getText().trim();
 
 			if (argString != null && argString.length() > 0) {
 				install.setInterpreterArgs(argString);
 			} else {
 				install.setInterpreterArgs(null);
 			}
 		} else {
 			install.setInterpreterArgs(null);
 		}
 
 		fLibraryBlock.performApply(install);
 		if (fEnvironmentVariablesBlock != null) {
 			fEnvironmentVariablesBlock.performApply(install);
 		}
 	}
 
 	// protected File getAbsoluteFileOrEmpty(String path) {
 	// if (path == null || path.length() == 0) {
 	// return new File(""); //$NON-NLS-1$
 	// }
 	// return new File(path).getAbsoluteFile();
 	// }
 
 	private void setInterpreterNameStatus(IStatus status) {
 		fStati[0] = status;
 	}
 
 	private void setInterpreterLocationStatus(IStatus status) {
 		fStati[1] = status;
 	}
 
 	protected IStatus getSystemLibraryStatus() {
 		return fStati[3];
 	}
 
 	public void setSystemLibraryStatus(IStatus status) {
 		fStati[3] = status;
 	}
 
 	/**
 	 * Updates the status of the ok button to reflect the given status.
 	 * Subclasses may override this method to update additional buttons.
 	 * 
 	 * @param status
 	 *            the status.
 	 */
 	protected void updateButtonsEnableState(IStatus status) {
 		Button ok = getButton(IDialogConstants.OK_ID);
 		if (ok != null && !ok.isDisposed())
 			ok.setEnabled(status.getSeverity() == IStatus.OK);
 	}
 
 	/**
 	 * @see org.eclipse.jface.dialogs.Dialog#setButtonLayoutData(org.eclipse.swt.widgets.Button)
 	 */
 	public void setButtonLayoutData(Button button) {
 		super.setButtonLayoutData(button);
 	}
 
 	/**
 	 * Returns the name of the section that this dialog stores its settings in
 	 * 
 	 * @return String
 	 */
 	protected String getDialogSettingsSectionName() {
 		return "ADD_INTERPRETER_DIALOG_SECTION"; //$NON-NLS-1$
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.jface.dialogs.Dialog#getDialogBoundsSettings()
 	 */
 	protected IDialogSettings getDialogBoundsSettings() {
 		IDialogSettings settings = DLTKDebugUIPlugin.getDefault()
 				.getDialogSettings();
 		IDialogSettings section = settings
 				.getSection(getDialogSettingsSectionName());
 		if (section == null) {
 			section = settings.addNewSection(getDialogSettingsSectionName());
 		}
 		return section;
 	}
 
 	public EnvironmentVariable[] getEnvironmentVariables() {
 		AbstractInterpreterEnvironmentVariablesBlock environmentVariablesBlock = this.fEnvironmentVariablesBlock;
 		if (environmentVariablesBlock != null) {
 			return environmentVariablesBlock.fEnvironmentVariablesContentProvider
 					.getVariables();
 		}
 		if (this.fEditedInterpreter != null) {
 			return this.fEditedInterpreter.getEnvironmentVariables();
 		}
 		return null;
 	}
 
 	/**
 	 * Re discover libraries if environment variables are changed.
 	 * 
 	 * @param environmentVariables
 	 */
 	public void updateLibraries(EnvironmentVariable[] newVars,
 			EnvironmentVariable[] oldVars) {
 		fLibraryBlock.reDiscover(newVars, oldVars);
 	}
 
 	protected boolean isRediscoverSupported() {
 		return false;
 	}
 
 	/**
 	 * This method could be used only after okPressed.
 	 */
 	protected IInterpreterInstall getLastInterpreterInstall() {
 		return this.lastInstall;
 	}
 }
