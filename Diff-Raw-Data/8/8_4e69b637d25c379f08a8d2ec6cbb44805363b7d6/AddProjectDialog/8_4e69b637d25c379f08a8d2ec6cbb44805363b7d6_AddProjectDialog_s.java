 /*******************************************************************************
  *  Copyright 2007 Ketan Padegaonkar http://ketan.padegaonkar.name
  *  
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *  
  *      http://www.apache.org/licenses/LICENSE-2.0
  *  
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  ******************************************************************************/
 package net.sourceforge.jcctray.ui.settings;
 
 import java.util.Collection;
 
 import net.sourceforge.jcctray.model.DashBoardProject;
 import net.sourceforge.jcctray.model.Host;
 import net.sourceforge.jcctray.model.JCCTraySettings;
 import net.sourceforge.jcctray.ui.Utils;
 import net.sourceforge.jcctray.ui.settings.providers.EnabledProjectsFilter;
 import net.sourceforge.jcctray.ui.settings.providers.HostContentProvider;
 import net.sourceforge.jcctray.ui.settings.providers.HostLabelProvider;
 import net.sourceforge.jcctray.ui.settings.providers.ProjectContentProvider;
 import net.sourceforge.jcctray.ui.settings.providers.ProjectLabelProvider;
 
 import org.apache.log4j.Logger;
 import org.eclipse.jface.viewers.DoubleClickEvent;
 import org.eclipse.jface.viewers.IDoubleClickListener;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.ISelectionChangedListener;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.ListViewer;
 import org.eclipse.jface.viewers.SelectionChangedEvent;
 import org.eclipse.jface.viewers.ViewerFilter;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.events.ShellAdapter;
 import org.eclipse.swt.events.ShellEvent;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Group;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.List;
 import org.eclipse.swt.widgets.Shell;
 
 public class AddProjectDialog {
 
 	private static final Logger	log	= Logger.getLogger(AddProjectDialog.class);
 
 	private class OkButtonListener implements SelectionListener {
 		public void widgetDefaultSelected(SelectionEvent e) {
 			shell.close();
 		}
 
 		public void widgetSelected(SelectionEvent e) {
 			widgetDefaultSelected(e);
 		}
 	}
 
 	public class ButtonEnabler implements ISelectionChangedListener {
 
 		private final Button	button;
 
 		public ButtonEnabler(Button button) {
 			this.button = button;
 		}
 
 		public void selectionChanged(SelectionChangedEvent event) {
 			ISelection selection = event.getSelection();
 			if (selection.isEmpty())
 				button.setEnabled(false);
 			else
 				button.setEnabled(true);
 		}
 
 	}
 
 	public class AddProjectListener implements SelectionListener, IDoubleClickListener {
 
 		private final ListViewer	projectListViewer;
 		private final ListViewer	serverListViewer;
 
 		public AddProjectListener(ListViewer serverListViewer, ListViewer projectListViewer) {
 			this.serverListViewer = serverListViewer;
 			this.projectListViewer = projectListViewer;
 		}
 
 		public void widgetDefaultSelected(SelectionEvent e) {
 			addProject();
 		}
 
 		public void widgetSelected(SelectionEvent e) {
 			addProject();
 		}
 
 		public void doubleClick(DoubleClickEvent event) {
 			addProject();
 		}
 
 		private void addProject() {
 			ISelection sel = projectListViewer.getSelection();
 			if (sel instanceof IStructuredSelection) {
 				IStructuredSelection selection = (IStructuredSelection) sel;
 				if (selection.isEmpty())
 					return;
 
 				Host selectedHost = (Host) ((IStructuredSelection) serverListViewer.getSelection()).getFirstElement();
 				Object[] selections = selection.toArray();
 				for (int i = 0; i < selections.length; i++) {
 					DashBoardProject dashBoardProject = new DashBoardProject();
 					dashBoardProject.setName(((DashBoardProject) selections[i]).getName());
 					dashBoardProject.setEnabled(true);
 					selectedHost.addConfiguredProject(dashBoardProject);
 				}
 				Utils.saveSettings(shell);
 			}
 			projectListViewer.refresh();
 		}
 
 	}
 
 	public class RemoveServerListener implements SelectionListener {
 
 		private final ListViewer	serverListViewer;
 
 		public RemoveServerListener(ListViewer serverListViewer) {
 			this.serverListViewer = serverListViewer;
 		}
 
 		public void widgetDefaultSelected(SelectionEvent e) {
 			ISelection sel = this.serverListViewer.getSelection();
 			if ((sel instanceof IStructuredSelection) && !sel.isEmpty()) {
 				Host host = (Host) ((IStructuredSelection) sel).getFirstElement();
 				JCCTraySettings.getInstance().removeHost(host);
 				Utils.saveSettings(shell);
 			}
 			refreshUI();
 		}
 
 		public void widgetSelected(SelectionEvent e) {
 			widgetDefaultSelected(e);
 		}
 	}
 
 	private final class ProjectListPopulator implements ISelectionChangedListener {
 
 		private final ListViewer	projectListViewer;
 
 		public ProjectListPopulator(ListViewer projectListViewer) {
 			this.projectListViewer = projectListViewer;
 		}
 
 		public void selectionChanged(SelectionChangedEvent event) {
 			ISelection sel = event.getSelection();
 			Host host = null;
 			projectListViewer.setInput(null);
 			if (sel instanceof IStructuredSelection) {
 				try {
 					IStructuredSelection selection = (IStructuredSelection) sel;
 					if (selection.isEmpty())
 						return;
 					host = (Host) selection.getFirstElement();
 					projectListViewer.setInput(host.getCruiseProjects());
 				} catch (Exception e) {
 					log.error("Exception getting project list from host: " + host, e);
 				}
 			}
 		}
 	}
 
 	private final class AddServerListener implements SelectionListener {
 		private final Shell	shell;
 
 		public AddServerListener(Shell shell) {
 			this.shell = shell;
 		}
 
 		public void widgetDefaultSelected(SelectionEvent e) {
 			new AddServerDialog(shell).open();
 		}
 
 		public void widgetSelected(SelectionEvent e) {
 			widgetDefaultSelected(e);
 		}
 	}
 
 	private Shell		shell;
 	private final Shell	parentShell;
 	private ListViewer	serverListViewer;
 	private ListViewer	projectListViewer;
 	private Button		addProjectButton;
 	private Button		removeServerButton;
 	private Button		addServerButton;
 	private Button		okButton;
 
 	public AddProjectDialog(Shell shell) {
 		parentShell = shell;
 		initialize();
 	}
 
 	private void initialize() {
 		createShell();
 		createLabels();
 		createGroups();
 		createButtons();
 		hookEvents();
 	}
 
 	private void hookEvents() {
 		serverListViewer.addSelectionChangedListener(new ProjectListPopulator(projectListViewer));
 		serverListViewer.addSelectionChangedListener(new ButtonEnabler(removeServerButton));
 
 		projectListViewer.addSelectionChangedListener(new ButtonEnabler(addProjectButton));
 		projectListViewer.addDoubleClickListener(new AddProjectListener(serverListViewer, projectListViewer));
 		
 		addProjectButton.addSelectionListener(new AddProjectListener(serverListViewer, projectListViewer));
 
 		addServerButton.addSelectionListener(new AddServerListener(shell));
 		removeServerButton.addSelectionListener(new RemoveServerListener(serverListViewer));
 		okButton.addSelectionListener(new OkButtonListener());
 	}
 
 	private void createButtons() {
 		okButton = new Button(shell, SWT.NONE);
 		okButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 		okButton.setText("&Ok");
 		shell.setDefaultButton(okButton);
 	}
 
 	private void createGroups() {
 		Composite composite = new Composite(shell, SWT.NONE);
 		composite.setLayout(new GridLayout(2, false));
 		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
 		createBuildServerGroup(composite);
 		createAvailableProjectsGroup(composite);
 	}
 
 	private void createAvailableProjectsGroup(Composite composite) {
 		Group availableProjectsGroup = new Group(composite, SWT.NONE);
 		availableProjectsGroup.setText("A&vailable Projects");
 		availableProjectsGroup.setLayout(new GridLayout(1, false));
 		availableProjectsGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
 
 		List projectList = new List(availableProjectsGroup, SWT.V_SCROLL | SWT.MULTI);
 		projectList.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
 		projectListViewer = new ListViewer(projectList);
 		projectListViewer.setContentProvider(new ProjectContentProvider());
 		projectListViewer.setLabelProvider(new ProjectLabelProvider());
 		projectListViewer.setFilters(new ViewerFilter[]{new EnabledProjectsFilter(false)});
 		
 		addProjectButton = new Button(availableProjectsGroup, SWT.NONE);
 		addProjectButton.setText("Add &Project");
 		addProjectButton.setEnabled(false);
 	}
 
 	private void createBuildServerGroup(Composite parent) {
 		Group buildServerGroup = new Group(parent, SWT.NONE);
 		buildServerGroup.setText("&Build Servers");
 		buildServerGroup.setLayout(new GridLayout(1, false));
 		buildServerGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
 
 		List serverList = new List(buildServerGroup, SWT.V_SCROLL);
 		serverList.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
 		serverListViewer = new ListViewer(serverList);
 		serverListViewer.setLabelProvider(new HostLabelProvider());
 		serverListViewer.setContentProvider(new HostContentProvider());
 
 		Composite composite = new Composite(buildServerGroup, SWT.NONE);
 		composite.setLayout(new GridLayout(2, false));
 		addServerButton = new Button(composite, SWT.NONE);
 		addServerButton.setText("&Add Server");
 
 		removeServerButton = new Button(composite, SWT.NONE);
 		removeServerButton.setText("&Remove Server");
 		removeServerButton.setEnabled(false);
 	}
 
 	private void createLabels() {
 		Label label = new Label(shell, SWT.WRAP);
 		label.setText("The list on the left shows the build servers that JCCTray currently knows about. "
 				+ "Select a build server, then select one or more projects to add.");
 		label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 		new Label(shell, SWT.NONE);
 		label = new Label(shell, SWT.NONE);
 		label.setText("If you want to add a new build server, click Add Server");
 		label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 	}
 
 	private void createShell() {
 		shell = new Shell(parentShell, SWT.DIALOG_TRIM);
 		shell.setText("Manage Projects");
 		shell.setLayout(new GridLayout());
 		shell.setSize(600, 300);
 		shell.addShellListener(new ShellAdapter() {
 			public void shellActivated(ShellEvent e) {
 				refreshUI();
 			}
 		});
 	}
 
 	protected void refreshUI() {
 		Collection hosts = JCCTraySettings.getInstance().getHosts();
 		serverListViewer.setInput(hosts);
 		projectListViewer.setInput(null);
 	}
 
 	public void open() {
 		shell.open();
		shell.pack();
 	}
 
 }
