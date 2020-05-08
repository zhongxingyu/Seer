 /**********************************************************************
  * Copyright (c) 2003, 2005 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
 * 
  * Contributors:
  *     IBM Corporation - Initial API and implementation
  **********************************************************************/
 package org.eclipse.wst.server.ui.internal.editor;
 
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.core.resources.IFolder;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IWorkspaceRoot;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.debug.core.ILaunchConfiguration;
 import org.eclipse.debug.core.ILaunchConfigurationType;
 import org.eclipse.debug.core.ILaunchManager;
 import org.eclipse.debug.ui.DebugUITools;
 import org.eclipse.jface.window.Window;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.*;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Group;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Spinner;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.ui.IEditorInput;
 import org.eclipse.ui.IEditorSite;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.dialogs.ContainerSelectionDialog;
 import org.eclipse.ui.forms.FormColors;
 import org.eclipse.ui.forms.events.HyperlinkAdapter;
 import org.eclipse.ui.forms.events.HyperlinkEvent;
 import org.eclipse.ui.forms.widgets.*;
 import org.eclipse.ui.help.IWorkbenchHelpSystem;
 import org.eclipse.wst.server.core.*;
 import org.eclipse.wst.server.core.internal.Server;
 import org.eclipse.wst.server.core.internal.ServerType;
 import org.eclipse.wst.server.core.util.SocketUtil;
 import org.eclipse.wst.server.ui.editor.*;
 import org.eclipse.wst.server.ui.internal.ContextIds;
 import org.eclipse.wst.server.ui.internal.Messages;
 import org.eclipse.wst.server.ui.internal.ServerUIPlugin;
 import org.eclipse.wst.server.ui.internal.Trace;
 import org.eclipse.wst.server.ui.internal.command.*;
 import org.eclipse.wst.server.ui.internal.wizard.ClosableWizardDialog;
 import org.eclipse.wst.server.ui.internal.wizard.TaskWizard;
 import org.eclipse.wst.server.ui.internal.wizard.WizardTaskUtil;
 import org.eclipse.wst.server.ui.wizard.WizardFragment;
 /**
  * Server general editor page.
  */
 public class OverviewEditorPart extends ServerEditorPart {
 	protected Text serverName;
 	protected Text serverConfigurationName;
 	protected Text hostname;
 	protected Combo runtimeCombo;
 	protected Button autoPublishDefault;
 	protected Button autoPublishOverride;
 	protected Spinner autoPublishTime;
 
 	protected boolean updating;
 
 	protected IRuntime[] runtimes;
 
 	protected PropertyChangeListener listener;
 
 	/**
 	 * OverviewEditorPart constructor comment.
 	 */
 	public OverviewEditorPart() {
 		super();
 	}
 
 	/**
 	 * 
 	 */
 	protected void addChangeListener() {
 		listener = new PropertyChangeListener() {
 			public void propertyChange(PropertyChangeEvent event) {
 				if (updating)
 					return;
 				updating = true;
 				if (event.getPropertyName().equals("name"))
 					updateNames();
 				else if (event.getPropertyName().equals("hostname") && hostname != null) {
 					hostname.setText((String) event.getNewValue());
 				} else if (event.getPropertyName().equals("runtime-id")) {
 					String runtimeId = (String) event.getNewValue();
 					IRuntime runtime = null;
 					if (runtimeId != null)
 						runtime = ServerCore.findRuntime(runtimeId);
 					int size = runtimes.length;
 					for (int i = 0; i < size; i++) {
 						if (runtimes[i].equals(runtime))
 							runtimeCombo.select(i);
 					}
 				} else if (event.getPropertyName().equals("configuration-id") && serverConfigurationName != null) {
 					String path = (String) event.getNewValue();
 					serverConfigurationName.setText(path);
 				}
 				updating = false;
 			}
 		};
 		if (server != null)
 			server.addPropertyChangeListener(listener);
 	}
 
 	protected void updateNames() {
 		if (serverName != null)
 			serverName.setText(server.getName());
 	}
 
 	/**
 	 * Creates the SWT controls for this workbench part.
 	 *
 	 * @param parent the parent control
 	 */
 	public final void createPartControl(final Composite parent) {
 		FormToolkit toolkit = getFormToolkit(parent.getDisplay());
 		
 		ScrolledForm form = toolkit.createScrolledForm(parent);
 		form.setText(Messages.serverEditorOverviewPageTitle);
 		form.getBody().setLayout(new GridLayout());
 		
 		Composite columnComp = toolkit.createComposite(form.getBody());
 		GridLayout layout = new GridLayout();
 		layout.numColumns = 2;
 		//layout.marginHeight = 10;
 		//layout.marginWidth = 10;
 		layout.verticalSpacing = 0;
 		layout.horizontalSpacing = 10;
 		columnComp.setLayout(layout);
 		columnComp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_FILL));
 		
 		// left column
 		Composite leftColumnComp = toolkit.createComposite(columnComp);
 		layout = new GridLayout();
 		layout.marginHeight = 0;
 		layout.marginWidth = 0;
 		layout.verticalSpacing = 10;
 		layout.horizontalSpacing = 0;
 		leftColumnComp.setLayout(layout);
 		leftColumnComp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_FILL));
 		
 		Section section = toolkit.createSection(leftColumnComp, ExpandableComposite.TWISTIE|ExpandableComposite.EXPANDED | ExpandableComposite.TITLE_BAR | Section.DESCRIPTION | ExpandableComposite.FOCUS_TITLE);
 		section.setText(Messages.serverEditorOverviewSection);
 		section.setDescription(Messages.serverEditorOverviewDescription);
 		section.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_FILL));
 
 		Composite composite = toolkit.createComposite(section);
 		layout = new GridLayout();
 		layout.numColumns = 2;
 		layout.marginHeight = 5;
 		layout.marginWidth = 10;
 		layout.verticalSpacing = 5;
 		layout.horizontalSpacing = 15;
 		composite.setLayout(layout);
 		composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_FILL));
 		IWorkbenchHelpSystem whs = PlatformUI.getWorkbench().getHelpSystem();
 		whs.setHelp(composite, ContextIds.EDITOR_OVERVIEW_PAGE);
 		toolkit.paintBordersFor(composite);
 		section.setClient(composite);
 
 		// server name
 		if (server != null) {
 			createLabel(toolkit, composite, Messages.serverEditorOverviewServerName);
 			
 			serverName = toolkit.createText(composite, server.getName());
 			GridData data = new GridData(GridData.FILL_HORIZONTAL);
 			serverName.setLayoutData(data);
 			serverName.addModifyListener(new ModifyListener() {
 				public void modifyText(ModifyEvent e) {
 					if (updating)
 						return;
 					updating = true;
 					execute(new SetServerNameCommand(getServer(), serverName.getText()));
 					updating = false;
 				}
 			});
 			whs.setHelp(serverName, ContextIds.EDITOR_SERVER);
 		}
 		
 		// hostname
 		if (server != null) {
 			createLabel(toolkit, composite, Messages.serverEditorOverviewServerHostname);
 			
 			hostname = toolkit.createText(composite, server.getHost());
 			GridData data = new GridData(GridData.FILL_HORIZONTAL);
 			hostname.setLayoutData(data);
 			hostname.addModifyListener(new ModifyListener() {
 				public void modifyText(ModifyEvent e) {
 					if (updating)
 						return;
 					updating = true;
 					execute(new SetServerHostnameCommand(getServer(), hostname.getText()));
 					updating = false;
 				}
 			});
 			whs.setHelp(hostname, ContextIds.EDITOR_HOSTNAME);
 		}
 		
 		// runtime
 		if (server != null && server.getServerType() != null && server.getServerType().hasRuntime()) {
 			final IRuntime runtime = server.getRuntime();
 			createLabel(toolkit, composite, Messages.serverEditorOverviewRuntime);
 			
 			IRuntimeType runtimeType = server.getServerType().getRuntimeType();
 			runtimes = ServerUIPlugin.getRuntimes(runtimeType);
 			
 			if (runtimes == null || runtimes.length == 0)
 				toolkit.createLabel(composite, "");
 			else if (runtimes.length == 1)
 				toolkit.createLabel(composite, runtime.getName());
 			else {
 				runtimeCombo = new Combo(composite, SWT.READ_ONLY);
 				runtimeCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 				updateRuntimeCombo();
 				
 				int size = runtimes.length;
 				for (int i = 0; i < size; i++) {
 					if (runtimes[i].equals(runtime))
 						runtimeCombo.select(i);
 				}
 				
 				runtimeCombo.addSelectionListener(new SelectionListener() {
 					public void widgetSelected(SelectionEvent e) {
 						try {
 							if (updating)
 								return;
 							updating = true;
 							IRuntime newRuntime = runtimes[runtimeCombo.getSelectionIndex()];
 							execute(new SetServerRuntimeCommand(getServer(), newRuntime));
 							updating = false;
 						} catch (Exception ex) {
 							// ignore
 						}
 					}
 					public void widgetDefaultSelected(SelectionEvent e) {
 						widgetSelected(e);
 					}
 				});
 				whs.setHelp(runtimeCombo, ContextIds.EDITOR_RUNTIME);
 			}
 			
 			createLabel(toolkit, composite, "");
 			Hyperlink link = toolkit.createHyperlink(composite, Messages.serverEditorOverviewRuntimeEdit, SWT.NONE);
 			link.addHyperlinkListener(new HyperlinkAdapter() {
 				public void linkActivated(HyperlinkEvent e) {
 					editRuntime(runtime);
 				}
 			});
 			link.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
 		}
 		
 		// server configuration path
 		if (server != null && server.getServerType().hasServerConfiguration()) {
 			createLabel(toolkit, composite, Messages.serverEditorOverviewServerConfigurationPath);
 			
 			IFolder folder = server.getServerConfiguration();
 			if (folder == null)
 				serverConfigurationName = toolkit.createText(composite, Messages.elementUnknownName);
 			else
 				serverConfigurationName = toolkit.createText(composite, "" + server.getServerConfiguration().getFullPath());
 			//if (!server.getServerConfiguration().getFullPath().toFile().exists())
 			
 			serverConfigurationName.setEditable(false);
 			serverConfigurationName.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 			
 			whs.setHelp(serverConfigurationName, ContextIds.EDITOR_CONFIGURATION);
 			
 			createLabel(toolkit, composite, "");
 			final IFolder currentFolder = server.getServerConfiguration();
 			Hyperlink link = toolkit.createHyperlink(composite, Messages.serverEditorOverviewServerConfigurationEdit, SWT.NONE);
 			link.addHyperlinkListener(new HyperlinkAdapter() {
 				public void linkActivated(HyperlinkEvent e) {
 					ContainerSelectionDialog dialog = new ContainerSelectionDialog(serverConfigurationName.getShell(),
 						currentFolder, true, Messages.serverEditorOverviewServerConfigurationEditMessage);
 					dialog.showClosedProjects(false);
 					
 					if (dialog.open() != Window.CANCEL) {
 						Object[] result = dialog.getResult();
 						if (result != null && result.length == 1) {
 							IPath path = (IPath) result[0];
 							
 							IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
 							IResource resource = root.findMember(path);
 							if (resource != null && resource instanceof IFolder) {
 								IFolder folder2 = (IFolder) resource;
 							
 								if (updating)
 									return;
 								updating = true;
 								execute(new SetServerConfigurationFolderCommand(getServer(), folder2));
 								serverConfigurationName.setText(folder2.getFullPath().toString());
 								updating = false;
 							}
 						}
 					}
 				}
 			});
 			link.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
 		}
 		
 		// auto-publish
 		if (server != null) {
 			Group group = new Group(composite, SWT.NONE);
 			group.setBackground(composite.getBackground());
 			group.setText(Messages.serverEditorOverviewPublishing);
 			layout = new GridLayout();
 			layout.numColumns = 2;
 			layout.marginHeight = 5;
 			layout.marginWidth = 10;
 			layout.verticalSpacing = 5;
 			layout.horizontalSpacing = 15;
 			group.setLayout(layout);
 			GridData data = new GridData(GridData.FILL_HORIZONTAL);
 			data.horizontalSpan = 2;
 			group.setLayoutData(data);
 			
 			autoPublishDefault = toolkit.createButton(group, Messages.serverEditorOverviewAutoPublishDefault, SWT.RADIO);
 			data = new GridData(GridData.FILL_HORIZONTAL);
 			data.horizontalSpan = 2;
 			autoPublishDefault.setLayoutData(data);
 			final Server svr = (Server) server;
 			autoPublishDefault.setSelection(svr.getAutoPublishDefault());
 			whs.setHelp(autoPublishDefault, ContextIds.EDITOR_AUTOPUBLISH_DEFAULT);
 			
 			autoPublishOverride = toolkit.createButton(group, Messages.serverEditorOverviewAutoPublishOverride, SWT.RADIO);
 			autoPublishOverride.setSelection(!svr.getAutoPublishDefault());
 			whs.setHelp(autoPublishOverride, ContextIds.EDITOR_AUTOPUBLISH_OVERRIDE);
 			
 			autoPublishOverride.addSelectionListener(new SelectionAdapter() {
 				public void widgetSelected(SelectionEvent e) {
 					if (updating)
 						return;
 					updating = true;
 					execute(new SetServerAutoPublishDefaultCommand(getServer(), autoPublishDefault.getSelection()));
 					updating = false;
 					autoPublishTime.setEnabled(autoPublishOverride.getSelection());
 				}
 			});
 			
 			//autoPublishTime = toolkit.createText(composite, svr.getAutoPublishTime() + "");
 			autoPublishTime = new Spinner(group, SWT.BORDER);
 			autoPublishTime.setMinimum(0);
 			autoPublishTime.setMaximum(120);
 			autoPublishTime.setSelection(svr.getAutoPublishTime());
 			data = new GridData(GridData.FILL_HORIZONTAL);
 			autoPublishTime.setLayoutData(data);
 			autoPublishTime.setEnabled(autoPublishOverride.getSelection());
 			whs.setHelp(autoPublishTime, ContextIds.EDITOR_AUTOPUBLISH_TIME);
 			
 			autoPublishTime.addModifyListener(new ModifyListener() {
 				public void modifyText(ModifyEvent e) {
 					if (updating)
 						return;
 					updating = true;
 					try {
 						execute(new SetServerAutoPublishTimeCommand(getServer(), autoPublishTime.getSelection()));
 					} catch (Exception ex) {
 						// ignore
 					}
 					updating = false;
 				}
 			});
 			
 			IServerType serverType = server.getServerType();
 			if (serverType.supportsLaunchMode(ILaunchManager.RUN_MODE) || serverType.supportsLaunchMode(ILaunchManager.DEBUG_MODE)
 					|| serverType.supportsLaunchMode(ILaunchManager.PROFILE_MODE)) {
 				ILaunchConfigurationType launchType = ((ServerType) serverType).getLaunchConfigurationType();
 				if (launchType.isPublic()) {
 					Hyperlink link = toolkit.createHyperlink(composite, Messages.serverEditorOverviewOpenLaunchConfiguration, SWT.NONE);
 					data = new GridData();
 					data.horizontalSpan = 2;
 					link.setLayoutData(data);
 					link.addHyperlinkListener(new HyperlinkAdapter() {
 						public void linkActivated(HyperlinkEvent e) {
 							try {
 								ILaunchConfiguration launchConfig = svr.getLaunchConfiguration(true, null);
 								// TODO: use correct launch group
 								DebugUITools.openLaunchConfigurationPropertiesDialog(parent.getShell(), launchConfig, "org.eclipse.debug.ui.launchGroup.run");
 							} catch (CoreException ce) {
 								Trace.trace(Trace.SEVERE, "Could not create launch configuration", ce);
 							}
 						}
 					});
 				}
 			}
 		}
 		
 		insertSections(leftColumnComp, "org.eclipse.wst.server.editor.overview.left");
 		
 		// right column
 		Composite rightColumnComp = toolkit.createComposite(columnComp);
 		layout = new GridLayout();
 		layout.marginHeight = 0;
 		layout.marginWidth = 0;
 		layout.verticalSpacing = 10;
 		layout.horizontalSpacing = 0;
 		rightColumnComp.setLayout(layout);
 		rightColumnComp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_FILL));
 		
 		insertSections(rightColumnComp, "org.eclipse.wst.server.editor.overview.right");
 		
 		form.setContent(columnComp);
 		form.reflow(true);
 
 		initialize();
 	}
 	
 	protected void editRuntime(IRuntime runtime) {
 		IRuntimeWorkingCopy runtimeWorkingCopy = runtime.createWorkingCopy();
 		if (showWizard(runtimeWorkingCopy) != Window.CANCEL) {
 			try {
 				runtimeWorkingCopy.save(false, null);
 			} catch (Exception ex) {
 				// ignore
 			}
 		}
 	}
 	
 	protected int showWizard(final IRuntimeWorkingCopy runtimeWorkingCopy) {
 		String title = Messages.wizEditRuntimeWizardTitle;
 		final WizardFragment fragment2 = ServerUIPlugin.getWizardFragment(runtimeWorkingCopy.getRuntimeType().getId());
 		if (fragment2 == null)
 			return Window.CANCEL;
 		
 		TaskModel taskModel = new TaskModel();
 		taskModel.putObject(TaskModel.TASK_RUNTIME, runtimeWorkingCopy);
 
 		WizardFragment fragment = new WizardFragment() {
 			protected void createChildFragments(List list) {
 				list.add(fragment2);
 				list.add(new WizardFragment() {
 					public void performFinish(IProgressMonitor monitor) throws CoreException {
 						WizardTaskUtil.saveRuntime(getTaskModel(), monitor);
 					}
 				});
 			}
 		};
 		
 		TaskWizard wizard = new TaskWizard(title, fragment, taskModel);
 		wizard.setForcePreviousAndNextButtons(true);
 		ClosableWizardDialog dialog = new ClosableWizardDialog(getEditorSite().getShell(), wizard);
 		return dialog.open();
 	}
 	
 	protected void updateRuntimeCombo() {
 		IRuntimeType runtimeType = server.getServerType().getRuntimeType();
 		runtimes = ServerUIPlugin.getRuntimes(runtimeType);
 		
 		if (SocketUtil.isLocalhost(server.getHost()) && runtimes != null) {
 			List runtimes2 = new ArrayList();
 			int size = runtimes.length;
 			for (int i = 0; i < size; i++) {
 				IRuntime runtime2 = runtimes[i];
 				if (!runtime2.isStub())
 					runtimes2.add(runtime2);
 			}
 			runtimes = new IRuntime[runtimes2.size()];
 			runtimes2.toArray(runtimes);
 		}
 		
 		int size = runtimes.length;
 		String[] items = new String[size];
 		for (int i = 0; i < size; i++)
 			items[i] = runtimes[i].getName();
 		
 		runtimeCombo.setItems(items);
 	}
 
 	protected Label createLabel(FormToolkit toolkit, Composite parent, String text) {
 		Label label = toolkit.createLabel(parent, text);
 		label.setForeground(toolkit.getColors().getColor(FormColors.TITLE));
 		return label;
 	}
 	
 	public void dispose() {
 		super.dispose();
 		
 		if (server != null)
 			server.removePropertyChangeListener(listener);
 	}
 
 	/* (non-Javadoc)
 	 * Initializes the editor part with a site and input.
 	 */
 	public void init(IEditorSite site, IEditorInput input) {
 		super.init(site, input);
 		
 		addChangeListener();
 		initialize();
 	}
 	
 	/**
 	 * Initialize the fields in this editor.
 	 */
 	protected void initialize() {
 		if (serverName == null)
 			return;
 		updating = true;
 		
 		if (server != null) {
 			serverName.setText(server.getName());
 			if (readOnly)
 				serverName.setEditable(false);
 			else
 				serverName.setEditable(true);
 		}
 		
 		updating = false;
 	}
 	
 	//protected void validate() { }
 
 	/*
 	 * @see IWorkbenchPart#setFocus()
 	 */
 	public void setFocus() {
 		if (serverName != null)
 			serverName.setFocus();
 		else if (serverConfigurationName != null)
 			serverConfigurationName.setFocus();
 	}
 }
