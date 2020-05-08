 /*******************************************************************************
  * Copyright (c) 2005, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.debug.ui.launchConfigurations;
 
 import java.net.URI;
 import java.net.URISyntaxException;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.ListenerList;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.debug.core.ILaunchConfiguration;
 import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
 import org.eclipse.dltk.core.DLTKCore;
 import org.eclipse.dltk.core.IModelElement;
 import org.eclipse.dltk.core.IScriptProject;
 import org.eclipse.dltk.core.ISourceModule;
 import org.eclipse.dltk.core.environment.EnvironmentManager;
 import org.eclipse.dltk.core.environment.IEnvironment;
 import org.eclipse.dltk.core.environment.IFileHandle;
 import org.eclipse.dltk.debug.ui.messages.DLTKLaunchConfigurationsMessages;
 import org.eclipse.dltk.internal.launching.LaunchConfigurationUtils;
 import org.eclipse.dltk.launching.ScriptLaunchConfigurationConstants;
 import org.eclipse.dltk.ui.DLTKPluginImages;
 import org.eclipse.dltk.ui.preferences.FieldValidators;
 import org.eclipse.dltk.ui.preferences.FieldValidators.FilePathValidator;
 import org.eclipse.dltk.ui.util.SWTFactory;
 import org.eclipse.dltk.utils.PlatformFileUtils;
 import org.eclipse.jface.dialogs.IDialogConstants;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Group;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
 import org.eclipse.ui.model.WorkbenchContentProvider;
 import org.eclipse.ui.model.WorkbenchLabelProvider;
 import org.eclipse.ui.views.navigator.ResourceComparator;
 
 public abstract class MainLaunchConfigurationTab extends
 		ScriptLaunchConfigurationTab implements
 		IMainLaunchConfigurationTabListenerManager {
 
 	private Text fScriptText;
 
 	private Button interactiveConsoleCheck;
 
 	private boolean useInteractiveConsoleGroup = false;
 
 	ListenerList listeners = new ListenerList();
 
 	public MainLaunchConfigurationTab(String mode) {
 		super(mode);
 	}
 
 	public void addListener(IMainLaunchConfigurationTabListener listener) {
 		this.listeners.add(listener);
 	}
 
 	public void removeListener(IMainLaunchConfigurationTabListener listener) {
 		this.listeners.remove(listener);
 	}
 
 	private void notifyProjectChangedListeners(IProject project) {
 		Object[] list = this.listeners.getListeners();
 		for (int i = 0; i < list.length; i++) {
 			((IMainLaunchConfigurationTabListener) list[i])
 					.projectChanged(project);
 		}
 	}
 
 	private void notifyInteractiveChangedListeners(boolean value) {
 		Object[] list = this.listeners.getListeners();
 		for (int i = 0; i < list.length; i++) {
 			((IMainLaunchConfigurationTabListener) list[i])
 					.interactiveChanged(value);
 		}
 	}
 
 	/**
 	 * Enable this to allow interactive group support.
 	 */
 	protected void enableInteractiveConsoleGroup() {
 		this.useInteractiveConsoleGroup = true;
 	}
 
 	protected void doInitializeForm(ILaunchConfiguration config) {
 		updateMainModuleFromConfig(config);
 		initializeDebugConsole(config);
 		initializeInteractiveConsoleFrom(config);
 	}
 
 	private void initializeDebugConsole(ILaunchConfiguration config) {
 		if (debugConsole != null) {
 			debugConsole
 					.setSelection(LaunchConfigurationUtils
 							.getBoolean(
 									config,
 									ScriptLaunchConfigurationConstants.ATTR_DEBUG_CONSOLE,
 									true));
 		}
 	}
 
 	protected void initializeInteractiveConsoleFrom(ILaunchConfiguration config) {
 		if (useInteractiveConsoleGroup) {
 			boolean console = LaunchConfigurationUtils
 					.getBoolean(
 							config,
 							ScriptLaunchConfigurationConstants.ATTR_USE_INTERACTIVE_CONSOLE,
 							false);
 			this.interactiveConsoleCheck.setSelection(console);
 		}
 	}
 
 	private Button fSearchButton;
 
 	protected static final String FIELD_SCRIPT = "mainScript"; //$NON-NLS-1$
 
 	/**
 	 * Creates the widgets for specifying a main type.
 	 * 
 	 * @param parent
 	 *            the parent composite
 	 */
 	protected void createMainModuleEditor(Composite parent, String text) {
 		final Composite editParent;
 		if (needGroupForField(FIELD_SCRIPT)) {
 			Group mainGroup = new Group(parent, SWT.NONE);
 			mainGroup.setText(text);
 			GridData gd = new GridData(GridData.FILL_HORIZONTAL);
 			mainGroup.setLayoutData(gd);
 			GridLayout layout = new GridLayout();
 			layout.numColumns = 2;
 			mainGroup.setLayout(layout);
 			editParent = mainGroup;
 		} else {
 			createLabelForField(parent, FIELD_SCRIPT, text);
 			editParent = parent;
 		}
 		fScriptText = new Text(editParent, SWT.SINGLE | SWT.BORDER);
 		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
 
 		WidgetListener listener = getWidgetListener();
 
 		fScriptText.setLayoutData(gd);
 		fScriptText.addModifyListener(listener);
 
 		fSearchButton = createPushButton(editParent,
 				DLTKLaunchConfigurationsMessages.mainTab_searchButton, null);
 		fSearchButton.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				handleSearchButtonSelected();
 			}
 		});
 	}
 
 	/**
 	 * @param enabled
 	 */
 	protected void setEnableScriptField(boolean enabled) {
 		fScriptText.setEnabled(enabled);
 		fSearchButton.setEnabled(enabled && getProjectName().length() > 0);
 	}
 
 	/**
 	 * The select button pressed handler
 	 */
 	protected void handleSearchButtonSelected() {
 		ElementTreeSelectionDialog dialog = new ElementTreeSelectionDialog(
 				getShell(), new WorkbenchLabelProvider(),
 				new WorkbenchContentProvider());
 		dialog
 				.setTitle(DLTKLaunchConfigurationsMessages.mainTab_searchButton_title);
 		dialog
 				.setMessage(DLTKLaunchConfigurationsMessages.mainTab_searchButton_message);
 		IScriptProject proj = getProject();
 		if (proj == null)
 			return;
 		dialog.setInput(proj.getProject());
 		dialog.setComparator(new ResourceComparator(ResourceComparator.NAME));
 		if (dialog.open() == IDialogConstants.OK_ID) {
 			IResource resource = (IResource) dialog.getFirstResult();
 			String arg = resource.getProjectRelativePath().toPortableString();
 			// check extension
 			fScriptText.setText(arg);
 		}
 	}
 
 	/**
 	 * Loads the main type from the launch configuration's preference store
 	 * 
 	 * @param config
 	 *            the config to load the main type from
 	 */
 	protected void updateMainModuleFromConfig(ILaunchConfiguration config) {
 		fScriptText.setText(getMainModuleName(config));
 	}
 
 	/*
 	 * @see
 	 * org.eclipse.dltk.debug.ui.launchConfigurations.ScriptLaunchConfigurationTab
 	 * #doCreateControl(org.eclipse.swt.widgets.Composite)
 	 */
 	protected void doCreateControl(Composite composite) {
 		createMainModuleEditor(composite,
 				DLTKLaunchConfigurationsMessages.mainTab_mainModule);
 	}
 
 	protected void createDebugOptions(Composite group) {
 		super.createDebugOptions(group);
 		if (canSelectDebugConsoleType()) {
 			debugConsole = SWTFactory.createCheckButton(group,
 					"Debug console redirection");
 			debugConsole.addSelectionListener(getWidgetListener());
 		}
 	}
 
 	protected boolean canSelectDebugConsoleType() {
 		return false;
 	}
 
 	private Button debugConsole;
 
 	/*
 	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getName()
 	 */
 	public String getName() {
 		return DLTKLaunchConfigurationsMessages.mainTab_title;
 	}
 
 	protected void setDefaults(ILaunchConfigurationWorkingCopy configuration,
 			IModelElement element) {
 		super.setDefaults(configuration, element);
 		if (element instanceof ISourceModule) {
 			configuration.setAttribute(
 					ScriptLaunchConfigurationConstants.ATTR_MAIN_SCRIPT_NAME,
 					element.getResource().getProjectRelativePath().toString());
 		}
 		setDefaultsDebugConsole(configuration, element);
 	}
 
 	private void setDefaultsDebugConsole(
 			ILaunchConfigurationWorkingCopy configuration, IModelElement element) {
 		if (debugConsole != null) {
 			debugConsole.setSelection(true);
 		}
 	}
 
 	/*
 	 * @see
 	 * org.eclipse.dltk.debug.ui.launchConfigurations.ScriptLaunchConfigurationTab
 	 * #doPerformApply(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
 	 */
 	protected void doPerformApply(ILaunchConfigurationWorkingCopy config) {
 		config.setAttribute(
 				ScriptLaunchConfigurationConstants.ATTR_MAIN_SCRIPT_NAME,
 				getScriptName());
 		if (debugConsole != null) {
 			config.setAttribute(
 					ScriptLaunchConfigurationConstants.ATTR_DEBUG_CONSOLE,
 					debugConsole.getSelection());
 		}
 		performApplyConnectionTimeout(config);
 		performApplyInteractiveConsole(config);
 	}
 
 	protected void performApplyConnectionTimeout(
 			ILaunchConfigurationWorkingCopy config) {
 		config
 				.setAttribute(
 						ScriptLaunchConfigurationConstants.ATTR_DLTK_DBGP_WAITING_TIMEOUT,
 						(String) null);
 	}
 
 	protected IResource getResource(ILaunchConfiguration config)
 			throws CoreException {
 		final String projectName = LaunchConfigurationUtils
 				.getProjectName(config);
		if (projectName == null || projectName.length() == 0
				|| !Path.ROOT.isValidSegment(projectName)) {
 			return null;
 		}
 		final IProject project = getWorkspaceRoot().getProject(projectName);
 		if (project.exists() && project.isOpen()) {
 			final String scriptName = config.getAttribute(
 					ScriptLaunchConfigurationConstants.ATTR_MAIN_SCRIPT_NAME,
 					(String) null);
			if (scriptName != null && scriptName.length() != 0
					&& new Path(scriptName).segmentCount() > 0
					&& Path.ROOT.isValidPath(scriptName)) {
 				final IFile scriptFile = project.getFile(scriptName);
 				if (scriptFile.exists()) {
 					return scriptFile;
 				}
 			}
 		}
 		return project;
 	}
 
 	protected void performApplyInteractiveConsole(
 			ILaunchConfigurationWorkingCopy config) {
 		if (useInteractiveConsoleGroup) {
 			config
 					.setAttribute(
 							ScriptLaunchConfigurationConstants.ATTR_USE_INTERACTIVE_CONSOLE,
 							this.interactiveConsoleCheck.getSelection());
 			String old = null;
 			try {
 				old = config
 						.getAttribute(
 								ScriptLaunchConfigurationConstants.ATTR_DLTK_CONSOLE_ID,
 								(String) null);
 			} catch (CoreException e) {
 				if (DLTKCore.DEBUG) {
 					e.printStackTrace();
 				}
 			}
 			if (old == null) {
 				config
 						.setAttribute(
 								ScriptLaunchConfigurationConstants.ATTR_DLTK_CONSOLE_ID,
 								"dltk_" + System.currentTimeMillis()); //$NON-NLS-1$
 			}
 		}
 	}
 
 	protected String getScriptName() {
 		return fScriptText.getText().trim();
 	}
 
 	protected void setScriptName(String value) {
 		fScriptText.setText(value);
 	}
 
 	/**
 	 * Validates the selected launch script.
 	 * 
 	 * @return true if the selected script is valid, false otherwise
 	 */
 	protected boolean validateScript() {
 		URI script = validateAndGetScriptPath();
 		IScriptProject project = getProject();
 		IEnvironment environment = EnvironmentManager.getEnvironment(project);
 		if (script != null) {
 			FilePathValidator validator = new FieldValidators.FilePathValidator();
 			IStatus result = validator.validate(script.getPath(), environment);
 
 			if (!result.isOK()) {
 				IFileHandle file = PlatformFileUtils
 						.findAbsoluteOrEclipseRelativeFile(environment, Path
 								.fromPortableString(script.getPath()));
 				if (file.exists() && file.isDirectory()) {
 					if (useInteractiveConsoleGroup) {
 						if (!interactiveConsoleCheck.getSelection()) {
 							setErrorMessage(DLTKLaunchConfigurationsMessages.MainLaunchConfigurationTab_0);
 							return false;
 						}
 						return true;
 					} else {
 						setErrorMessage(DLTKLaunchConfigurationsMessages.error_scriptNotFound);
 						return false;
 					}
 				}
 				setErrorMessage(DLTKLaunchConfigurationsMessages.error_scriptNotFound);
 				return false;
 			}
 		}
 		return true;
 	}
 
 	/**
 	 * @deprecated typo in method name
 	 * @return
 	 */
 	protected final URI validatAndGetScriptPath() {
 		return validateAndGetScriptPath();
 	}
 
 	/**
 	 * Gets the currently selected {@link ISourceModule}.
 	 * 
 	 * @return the selected source module or <code>null</code>
 	 */
 	protected ISourceModule getSourceModule() {
 		final IScriptProject project = this.getProject();
 		if (project == null) {
 			return null;
 		}
 		final String scriptName = getScriptName();
 		if (scriptName.length() == 0) {
 			return null;
 		}
 		final IFile file = project.getProject().getFile(scriptName);
 		return (ISourceModule) DLTKCore.create(file);
 	}
 
 	protected URI validateAndGetScriptPath() {
 		String projectName = getProjectName();
 		IScriptProject proj = getScriptModel().getScriptProject(projectName);
 		if (proj != null) {
 			notifyProjectChangedListeners(proj.getProject());
 		} else {
 			return null;
 		}
 		URI location = proj.getProject().getLocationURI();
 		if (location == null) {
 			setErrorMessage(DLTKLaunchConfigurationsMessages.error_notAValidProject);
 			return null;
 		}
 
 		URI script = null;
 		try {
 			script = new URI(location.getScheme(), location.getHost(), location
 					.getPath()
 					+ '/' + getScriptName(), location.getFragment());
 		} catch (URISyntaxException e) {
 			if (DLTKCore.DEBUG) {
 				e.printStackTrace();
 			}
 		}
 		if (script != null) {
 			IFile[] files = getWorkspaceRoot().findFilesForLocationURI(script);
 			if (files.length != 1) {
 				return script;
 			}
 
 			IFile file = files[0];
 			if (file.exists() && file.getLocationURI() != null) {
 				script = file.getLocationURI();
 			}
 		}
 		return script;
 	}
 
 	protected boolean validate() {
 		return super.validate() && validateScript();
 	}
 
 	/*
 	 * @see org.eclipse.debug.ui.AbstractLaunchConfigurationTab#getImage()
 	 */
 	public Image getImage() {
 		return DLTKPluginImages.get(DLTKPluginImages.IMG_OBJS_CLASS);
 	}
 
 	private String getMainModuleName(ILaunchConfiguration config) {
 		return LaunchConfigurationUtils.getString(config,
 				ScriptLaunchConfigurationConstants.ATTR_MAIN_SCRIPT_NAME,
 				EMPTY_STRING);
 	}
 
 	protected void createCustomSections(Composite parent) {
 		if (useInteractiveConsoleGroup) {
 			Group group = new Group(parent, SWT.NONE);
 			group
 					.setText(DLTKLaunchConfigurationsMessages.MainLaunchConfigurationTab_1);
 			GridData gd = new GridData(GridData.FILL_HORIZONTAL);
 			group.setLayoutData(gd);
 			GridLayout layout = new GridLayout();
 			layout.numColumns = 2;
 			group.setLayout(layout);
 			interactiveConsoleCheck = createCheckButton(
 					group,
 					DLTKLaunchConfigurationsMessages.MainLaunchConfigurationTab_2);
 			interactiveConsoleCheck.addSelectionListener(getWidgetListener());
 			interactiveConsoleCheck
 					.addSelectionListener(new SelectionAdapter() {
 						public void widgetSelected(SelectionEvent e) {
 							notifyInteractiveChangedListeners(interactiveConsoleCheck
 									.getSelection());
 						}
 					});
 		}
 	}
 }
