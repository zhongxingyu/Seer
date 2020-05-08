 package gov.va.mumps.debug.ui.launching;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import gov.va.mumps.debug.core.MDebugConstants;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IWorkspaceRoot;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.debug.core.ILaunchConfiguration;
 import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
 import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
 import org.eclipse.debug.ui.ILaunchConfigurationDialog;
 import org.eclipse.jface.dialogs.IInputValidator;
 import org.eclipse.jface.dialogs.InputDialog;
 import org.eclipse.jface.layout.TableColumnLayout;
 import org.eclipse.jface.viewers.ArrayContentProvider;
 import org.eclipse.jface.viewers.ColumnLabelProvider;
 import org.eclipse.jface.viewers.ColumnWeightData;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.LabelProvider;
 import org.eclipse.jface.viewers.TableViewer;
 import org.eclipse.jface.viewers.TableViewerColumn;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.KeyEvent;
 import org.eclipse.swt.events.KeyListener;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Table;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.ui.IEditorInput;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.IWorkbenchPage;
 import org.eclipse.ui.IWorkbenchWindow;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.dialogs.ElementListSelectionDialog;
 import org.eclipse.ui.dialogs.ListSelectionDialog;
 import org.eclipse.ui.dialogs.ResourceListSelectionDialog;
 
 import us.pwc.vista.eclipse.core.helper.SWTHelper;
 import us.pwc.vista.eclipse.core.resource.ResourceUtilExtension;
 import us.pwc.vista.eclipse.core.validator.TrueInputValidator;
 
 public class MMainTab extends AbstractLaunchConfigurationTab {
     private static class ProjectLabelProvider extends LabelProvider {
 		@Override
 		public Image getImage(Object element) {
 			return null;
 		}
 		
 		@Override
 		public String getText(Object element) {
 			if(element instanceof IProject) {
 				IProject p = (IProject) element;
 				return p.getName();
 			}
 			return null;
 		}
 	}
 	
 	private Text projectCtrl;
 	private Button browseProjectBtn;	
 	
 	private Text fileCtrl;
 	private Button browseFileBtn;
 	
 	private Text entryTagCtrl;
 	private Button browseEntryTagBtn;
 	
 	private Button extrinsicBtn;
 	
 	private TableViewer viewer;
 	private Button editBtn;
 	
 	private IProject project;
 	private List<EntryTag> entryTags;
 	private ParameterInfo[] parameterInfos;
 	
 	@Override
 	public void createControl(Composite parent) {
 		Composite container = SWTHelper.createComposite(parent, 3);
 	
 		this.projectCtrl = SWTHelper.createLabelTextPair(container, "Project:");
 		this.browseProjectBtn = SWTHelper.createButton(container, "Browse");
 		
 		this.fileCtrl = SWTHelper.createLabelTextPair(container, "M file:");
 		this.browseFileBtn = SWTHelper.createButton(container, "Browse");
 
 		this.entryTagCtrl = SWTHelper.createLabelTextPair(container, "Entry tag:");
 		this.browseEntryTagBtn = SWTHelper.createButton(container, "Browse");
 		
 		SWTHelper.addEmptyLabel(container, 1);
 		this.extrinsicBtn = SWTHelper.createCheckButton(container, "Extrinsic function", 2);
 
 		Label label = new Label(container, SWT.WRAP | SWT.LEFT);
 		label.setText("Parameters:");		
 		GridData gd = SWTHelper.setGridData(label, SWT.LEFT, false, SWT.BEGINNING, false);
 		label.setLayoutData(gd);
 		
 		this.viewer = this.addParameterTable(container);
 		Button[] buttons = SWTHelper.createButtons(container, new String[]{"Edit"});
 		this.editBtn = buttons[0];
 		
 		this.attachListeners();
 
 		this.setControl(container);
 	}
 
 	private void createParamTableColumns(TableViewer viewer, TableColumnLayout tableColumnLayout) {
 		TableViewerColumn nameColumn = SWTHelper.createTableViewerColumn(viewer, "Name", 0);
 		nameColumn.setLabelProvider(new ColumnLabelProvider() {
 			@Override
 			public String getText(Object element) {
 				ParameterInfo info = (ParameterInfo) element;
 				return info.getName();
 			}
 		});
 		tableColumnLayout.setColumnData(nameColumn.getColumn(), new ColumnWeightData(50, true));
 
 		TableViewerColumn valueColumn = SWTHelper.createTableViewerColumn(viewer, "Type", 0);
 		valueColumn.setLabelProvider(new ColumnLabelProvider() {
 			@Override
 			public String getText(Object element) {
 				ParameterInfo info = (ParameterInfo) element;
 				return info.getValueForDisplay();
 			}
 		});
 		tableColumnLayout.setColumnData(valueColumn.getColumn(), new ColumnWeightData(50, true));
 	}
 	
 	private TableViewer addParameterTable(Composite parent) {
 		Composite tableComposite = new Composite(parent, SWT.NONE);		
 		GridData gd = new GridData(GridData.FILL_BOTH); 
 		tableComposite.setLayoutData(gd);
 		TableColumnLayout tcl = new TableColumnLayout();
 		tableComposite.setLayout(tcl);
 		tableComposite.setFont(parent.getFont());
 		
 		TableViewer viewer = new TableViewer(tableComposite, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
 		viewer.setContentProvider(new ArrayContentProvider());
 		viewer.getTable().setHeaderVisible(true);
 		Table table = viewer.getTable();
 		table.setFont(parent.getFont());
 			
 		this.createParamTableColumns(viewer, tcl);		
 		return viewer;
 	}
 
 	private void attachListeners() {
 		this.projectCtrl.addKeyListener(new KeyListener() {
 			@Override
 			public void keyPressed(KeyEvent e) {
 			}
 
 			@Override
 			public void keyReleased(KeyEvent e) {
 				MMainTab.this.handleProjectChanged(true);			
 			}
 		});		
 
 		this.browseProjectBtn.addSelectionListener(new SelectionListener() {			
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				MMainTab.this.handleBrowseProject();			
 			}
 			
 			@Override
 			public void widgetDefaultSelected(SelectionEvent e) {
 			}
 		});
 		this.fileCtrl.addKeyListener(new KeyListener() {
 			@Override
 			public void keyPressed(KeyEvent e) {
 			}
 
 			@Override
 			public void keyReleased(KeyEvent e) {
 				MMainTab.this.handleFileChanged(true);			
 			}
 		});		
 		this.browseFileBtn.addSelectionListener(new SelectionListener() {			
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				MMainTab.this.handleBrowseFile();			
 			}
 			
 			@Override
 			public void widgetDefaultSelected(SelectionEvent e) {
 			}
 		});
 		this.entryTagCtrl.addKeyListener(new KeyListener() {
 			@Override
 			public void keyPressed(KeyEvent e) {
 			}
 
 			@Override
 			public void keyReleased(KeyEvent e) {
 				MMainTab.this.handleEntryTagChanged(true);			
 			}
 		});		
 		this.browseEntryTagBtn.addSelectionListener(new SelectionListener() {			
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				MMainTab.this.handleBrowseEntryTag();			
 			}
 			
 			@Override
 			public void widgetDefaultSelected(SelectionEvent e) {
 			}
 		});
 		this.extrinsicBtn.addSelectionListener(new SelectionListener() {			
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				MMainTab.this.handleExtrinsicSelected();			
 			}
 			
 			@Override
 			public void widgetDefaultSelected(SelectionEvent e) {
 			}
 		});
 		this.viewer.getTable().addSelectionListener(new SelectionListener() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				MMainTab.this.handleTableSelectionChanged();
 			}
 			
 			@Override
 			public void widgetDefaultSelected(SelectionEvent e) {
 			}
 		});
 		this.editBtn.addSelectionListener(new SelectionListener() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				MMainTab.this.handleEditParameter();
 			}
 			
 			@Override
 			public void widgetDefaultSelected(SelectionEvent e) {
 			}
 		});
 	}
 	
 	private void handleProjectChanged(boolean user) {
 		this.project = null;
 		String projectName = this.projectCtrl.getText();		
 		if (projectName.isEmpty()) {
 			this.updateProject("Project is not specified.", user);
 			return;
 		}				
 		IWorkspaceRoot r = ResourcesPlugin.getWorkspace().getRoot();
 		IProject project = r.getProject(projectName);
 		if (! project.exists()) {
 			this.updateProject("Project " + projectName + " does not exist.", user);
 			return;
 		}
 		this.project = project;
 		this.entryTags = null;
 		this.parameterInfos = null;
 		this.updateProject(null, user);
 	}
 	
 	private void updateProject(String errorMessage, boolean user) {
 		boolean enabled = (errorMessage == null);
 		this.fileCtrl.setText("");
 		this.clearEntryTagCtrls();
 		this.setFileCtrlsEnabled(enabled);
 		this.setEntryTagCtrlsEnabled(false);
 		if (errorMessage == null) {
 			if (user) this.updateFile("File path is not specified.", user);			
 		} else {			
 			this.setErrorMessage(errorMessage);
 		}
 		if (user) this.updateLaunchConfigurationDialog();		
 	}
 	
 	private void clearEntryTagCtrls() {
 		this.entryTagCtrl.setText("");
 		this.viewer.setInput(null);
 		this.viewer.refresh();		
 	}
 	
 	private void setFileCtrlsEnabled(boolean enabled) {
 		this.fileCtrl.setEnabled(enabled);
 		this.browseFileBtn.setEnabled(enabled);
 	}
 	
 	private void setEntryTagCtrlsEnabled(boolean enabled) {
 		this.entryTagCtrl.setEnabled(enabled);
 		this.browseEntryTagBtn.setEnabled(enabled);
 		this.extrinsicBtn.setEnabled(enabled);		
 	}
 	
 	private void handleBrowseProject() {
 		IWorkspaceRoot r = ResourcesPlugin.getWorkspace().getRoot();
 		IProject[] projects = r.getProjects();
 		LabelProvider lp = new ProjectLabelProvider();
 		Shell shell = this.getShell();
 		ElementListSelectionDialog dlg = new ElementListSelectionDialog(shell, lp);
 		dlg.setMultipleSelection(false);
 		dlg.setTitle("Project Selection");
 		dlg.setMessage("Please select a project:");
 		dlg.setElements(projects);
 		if (ListSelectionDialog.OK == dlg.open()) {
 			IProject project = (IProject) dlg.getFirstResult();
 			this.projectCtrl.setText(project.getName());
 			this.handleProjectChanged(true);
 		}	
 	}
 	
 	private void handleFileChanged(boolean user) {
 		this.entryTags = null;
 		String filePath = this.fileCtrl.getText();
 		if (filePath.isEmpty()) {
 			this.updateFile("File path is not specified.", user);
 			return;
 		}				
 		Path path = new Path(filePath);
 		if (path.isAbsolute()) {
 			this.updateFile("File path must be relative.", user);
 			return;
 		}
 		IFile file = this.project.getFile(path);
 		if (! file.exists()) {
 			this.updateFile("File " + file.getName() + " does not exist.", user);
 			return;		
 		}
 		if (! file.getName().endsWith(".m")) {
 			this.updateFile("File " + file.getName() + " is not an M file.", user);
 			return;					
 		}
 		this.entryTags = TagUtilities.getTags(file);
 		if ((this.entryTags == null) || (this.entryTags.size() == 0) ) {
 			this.entryTags = null;
 			this.parameterInfos = null;
 			this.updateFile("No entry tags was extracted from the file.", user);			
 		} else {		
 			this.updateFile(null, user);
 		}
 	}
 	
 	private void updateFile(String errorMessage, boolean user) {
 		boolean enabled = (errorMessage == null);
 		this.entryTagCtrl.setText("");
 		this.viewer.setInput(null);
 		this.viewer.refresh();
 		this.setEntryTagCtrlsEnabled(enabled);
 		if (enabled) {
 			if (user) this.setErrorMessage("Entry tag is not specified.");			
 		} else {
 			this.setErrorMessage(errorMessage);
 		}
 		if (user) this.updateLaunchConfigurationDialog();							
 	}
 
 	private void handleBrowseFile() {
 		Shell shell = this.getShell();		
 		ResourceListSelectionDialog dlg = new ResourceListSelectionDialog(shell, this.project, IResource.FILE);
 		if (ListSelectionDialog.OK == dlg.open()) {
 			IFile file = (IFile) dlg.getResult()[0];
 			String path = ResourceUtilExtension.getRelativePath(this.project, file);		
 			this.fileCtrl.setText(path);
 			this.handleFileChanged(true);
 		}		
 	}
 	
 	private EntryTag findEntryTag(String label) {
 		for (EntryTag routineEntryTag : this.entryTags) {
 			if (routineEntryTag.getLabel().equals(label)) {
 				return routineEntryTag;
 			}
 		}			
 		return null;
 	}
 	
 	private void handleEntryTagChanged(boolean user) {
 		String entryTag = this.entryTagCtrl.getText();
 		if (entryTag.isEmpty()) {
 			this.updateEntryTag("Entry tag is not specified.", null, user);
 			return;
 		}			
 		EntryTag userEntryTag = this.findEntryTag(entryTag);
 		if (userEntryTag == null) {
 			this.updateEntryTag("Entry tag " + entryTag + " does not exist.", null, user);
 		} else {			
 			this.updateEntryTag(null, userEntryTag, user);
 		}
 	}
 	
 	private void handleBrowseEntryTag() {
 		EntryTag tag = TagUtilities.selectTag(this.entryTags);
 		if (tag != null) {
 			this.entryTagCtrl.setText(tag.getLabel());
 			handleEntryTagChanged(true);
 		}
 	}
 	
 	private void updateEntryTag(String errorMessage, EntryTag foundTag, boolean user) {
 		if (errorMessage == null) {
 			String[] params = foundTag.getParameters();
 			ParameterInfo[] input = ParameterInfo.valueOf(params);
 			this.parameterInfos = input;
 			this.viewer.setInput(input);
 		} else {
 			this.viewer.setInput(null);
 		}
		this.viewer.refresh();
 		this.editBtn.setEnabled(false);
 		this.setErrorMessage(errorMessage);		
 		if (user) this.updateLaunchConfigurationDialog();		
 	}
 	
 	private void handleExtrinsicSelected() {
 		this.updateLaunchConfigurationDialog();		
 	}
 	
 	private void handleTableSelectionChanged() {
 		boolean enabled = this.viewer.getTable().getSelectionCount() == 1;
 		this.editBtn.setEnabled(enabled);
 	}
 	
 	private void handleEditParameter() {
 		if (this.parameterInfos != null) {
 			int index = this.viewer.getTable().getSelectionIndex();
 			if (index < this.parameterInfos.length) {
 				String paramName = this.parameterInfos[index].getName();
 				Shell shell = this.viewer.getTable().getParent().getShell();
 				IInputValidator v = new TrueInputValidator();
 				InputDialog d = new InputDialog(shell, "Enter Value", "Enter value for parameter " + paramName + ":", "", v);
 				if (InputDialog.OK == d.open()) {
 					String value = d.getValue();
 					this.parameterInfos[index].setValue(value);
 					this.viewer.refresh();
 					this.updateLaunchConfigurationDialog();
 				}
 			}
 		}		
 	}
 	
 	@Override
 	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
 		IWorkbenchWindow w = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
 		if (w != null) {
 			IWorkbenchPage page = w.getActivePage();
 			ISelection selection = page.getSelection();
 			if (selection instanceof IStructuredSelection) {
 				IStructuredSelection iss = (IStructuredSelection) selection;
 				if (! iss.isEmpty()) {
 					Object obj = iss.getFirstElement();
 					if (obj instanceof IResource) {
 						this.setDefault(configuration, (IResource) obj);
 						return;
 					}
 				}
 			}
 			IEditorPart part = page.getActiveEditor();
 			if (part != null) {
 				IEditorInput input = part.getEditorInput();
 				if (input != null) {
 					IResource resource = (IResource) input.getAdapter(IResource.class);
 					this.setDefault(configuration, resource);
 				}
 			}
 		}
 	}
 	
 	private void setDefault(ILaunchConfigurationWorkingCopy configuration, IResource resource) {
 		if (resource != null) {
 			IProject project = resource.getProject();
 			if (project != null) {
 				String name = project.getName();
 				configuration.setAttribute(MDebugConstants.ATTR_M_PROJECT_NAME, name);
 			}
 			if (resource instanceof IFile) {
 				IFile file = (IFile) resource;
 				String name = file.getName();
 				if (name.endsWith(".m")) {
 					String relativePath = ResourceUtilExtension.getRelativePath(project, file);
 					configuration.setAttribute(MDebugConstants.ATTR_M_FILE_PATH, relativePath.toString());
 					ILaunchConfigurationDialog dialog = this.getLaunchConfigurationDialog();
 					if (dialog != null) {
 						String routineName = name.substring(0, name.length()-2);
 						String newName = dialog.generateName(routineName);
 						configuration.rename(newName);
 					}
 				}
 			}
 		}
 		
 	}
 	
 	@Override
 	public void initializeFrom(ILaunchConfiguration configuration) {
 		try {
 			String projectName = configuration.getAttribute(MDebugConstants.ATTR_M_PROJECT_NAME, (String) null);
 			if (projectName == null) {
 				this.projectCtrl.setText("");
 			} else {				
 				this.projectCtrl.setText(projectName);
 			}
 			this.handleProjectChanged(false);
 			if (this.project == null) return;			
 			
 			String filePath = configuration.getAttribute(MDebugConstants.ATTR_M_FILE_PATH, (String) null);
 			if (filePath == null) {
 				this.fileCtrl.setText("");
 				return;
 			} else {				
 				this.fileCtrl.setText(filePath);
 			}
 			this.handleFileChanged(false);
 			if (this.entryTags == null) return;
 						
 			String entryTag = configuration.getAttribute(MDebugConstants.ATTR_M_ENTRY_TAG, (String) null);
 			if (entryTag == null) {
 				this.entryTagCtrl.setText("");
 			} else {
 				this.entryTagCtrl.setText(entryTag);
 			}
 			this.handleEntryTagChanged(false);
 			if (this.parameterInfos == null) return;
 			
 			boolean isExtrinsic = configuration.getAttribute(MDebugConstants.ATTR_M_IS_EXTRINSIC, false);
 			this.extrinsicBtn.setSelection(isExtrinsic);
 
 			@SuppressWarnings({ "rawtypes", "unchecked"})
 			List<String> params = configuration.getAttribute(MDebugConstants.ATTR_M_PARAMS, (List) null);
 			if (params != null) {
 				int index = 0;
 				for (String param : params) {
 					this.parameterInfos[index].setValue(param);
					++index;
 				}
 				this.viewer.refresh();
 			}			
 		} catch (CoreException e) {
 			this.projectCtrl.setText("");
 			this.handleProjectChanged(false);
 			setErrorMessage(e.getMessage());
 		}
 	}
 	
 	@Override
 	@SuppressWarnings("rawtypes")
 	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
 		this.updateForApply(configuration, this.projectCtrl, MDebugConstants.ATTR_M_PROJECT_NAME);
 		this.updateForApply(configuration, this.fileCtrl, MDebugConstants.ATTR_M_FILE_PATH);
 		this.updateForApply(configuration, this.entryTagCtrl, MDebugConstants.ATTR_M_ENTRY_TAG);
 		if (this.parameterInfos == null) {
 			configuration.setAttribute(MDebugConstants.ATTR_M_PARAMS, (List) null);
 		} else {
 			List<String> toBeStored = new ArrayList<String>();
 			for (ParameterInfo param : this.parameterInfos) {
 				toBeStored.add(param.getValue());
 			}
 			configuration.setAttribute(MDebugConstants.ATTR_M_PARAMS, toBeStored);
 		}
 		configuration.setAttribute(MDebugConstants.ATTR_M_IS_EXTRINSIC, this.extrinsicBtn.getSelection());
 	}
 	
 	private void updateForApply(ILaunchConfigurationWorkingCopy configuration, Text textCtrl, String attrName) {
 		String value = textCtrl.getText();
 		if (value.isEmpty()) {
 			value = null;
 		}
 		configuration.setAttribute(attrName, value);	
 	}
 	
 	@Override
 	public String getName() {
 		return "Main";
 	}
 	
 	@Override
 	public boolean isValid(ILaunchConfiguration launchConfig) {
 		if (this.getErrorMessage() == null) {
 			return super.isValid(launchConfig);
 		} else {
 			return false;
 		}
 	}
 }
