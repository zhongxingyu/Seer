 package com.uwusoft.timesheet.dialog;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.commons.lang3.StringUtils;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.jface.action.StatusLineManager;
 import org.eclipse.jface.fieldassist.AutoCompleteField;
 import org.eclipse.jface.fieldassist.TextContentAdapter;
 import org.eclipse.jface.preference.IPreferenceStore;
 import org.eclipse.jface.viewers.ArrayContentProvider;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.ISelectionChangedListener;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.LabelProvider;
 import org.eclipse.jface.viewers.SelectionChangedEvent;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.ModifyEvent;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.ui.dialogs.ListDialog;
 
 import com.uwusoft.timesheet.Activator;
 import com.uwusoft.timesheet.TimesheetApp;
 import com.uwusoft.timesheet.extensionpoint.StorageService;
 import com.uwusoft.timesheet.extensionpoint.SubmissionService;
 import com.uwusoft.timesheet.extensionpoint.model.SubmissionEntry;
 import com.uwusoft.timesheet.model.Task;
 import com.uwusoft.timesheet.util.ExtensionManager;
 
 /**
  * todo: add class doc
  *
  * @author Uta Wunderlich
  * @version $Revision: $, $Date: Nov 29, 2011
  * @since Nov 29, 2011
  */
 public class TaskListDialog extends ListDialog {
 
     private StorageService storageService;
     private SubmissionService submissionService;
     private Task taskSelected;
 	private String[] systems;
 	private Map<String, SubmissionEntry> tasksMap;
     private Combo systemCombo, projectCombo;
     private Text commentText;
     private AutoCompleteField commentCompleteField;
     private String projectSelected, systemSelected, task, comment;
     private boolean original;
 	private StatusLineManager statusLineManager = new StatusLineManager();
 
     public TaskListDialog(Shell shell, Task taskSelected) {
         super(shell);
 		IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();
 		storageService = new ExtensionManager<StorageService>(
                 StorageService.SERVICE_ID).getService(preferenceStore.getString(StorageService.PROPERTY));
 		Set<String> systemsList = TimesheetApp.getSubmissionSystems().keySet();
 		if (systemsList.isEmpty()) {
 			systemsList = new HashSet<String>();
 			systemsList.add("Local");
 		}
         systems = systemsList.toArray(new String[systemsList.size()]);
         this.taskSelected = taskSelected;
 		setContentProvider(ArrayContentProvider.getInstance());
 		setLabelProvider(new LabelProvider());
     }
     
     @Override
     protected Control createDialogArea(Composite composite) {
         Composite parent = (Composite) super.createDialogArea(composite);
         
         Composite systemPanel = new Composite(parent, SWT.NONE);
         systemPanel.setLayout(new GridLayout(4, false));
         
         (new Label(systemPanel, SWT.NULL)).setText("Submission System: ");
         systemCombo = new Combo(systemPanel, SWT.READ_ONLY);
         systemCombo.setItems(systems);
         if (systems.length == 1) systemCombo.setEnabled(false);
         
         (new Label(systemPanel, SWT.NULL)).setText("Project: ");
         projectCombo = new Combo(systemPanel, SWT.READ_ONLY);
         
         Button originalButton = new Button(parent, SWT.CHECK);
         originalButton.setText("original");
         originalButton.addSelectionListener(new SelectionAdapter() {
             public void widgetSelected(SelectionEvent e) {
             	original = ((Button) e.getSource()).getSelection();
                 setTasksAndProjects(true);
             }
         });
 
         (new Label(parent, SWT.NULL)).setText("Comment: ");
         commentText = new Text(parent, SWT.NONE);
         commentCompleteField = new AutoCompleteField(commentText, new TextContentAdapter(), new String[] {StringUtils.EMPTY});
         commentText.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
         commentText.addModifyListener(new ModifyListener() {
 			public void modifyText(ModifyEvent e) {
 				if (!StringUtils.isEmpty(task))
 				comment = commentText.getText();
 			}        	
         });
 		final Job setProposals = new Job("Search available comments") {
 			protected IStatus run(IProgressMonitor monitor) {
 				commentCompleteField.setProposals(storageService.getUsedCommentsForTask(task, projectSelected, systemSelected));
 		        return Status.OK_STATUS;
 			}
 		};
 		getTableViewer().addSelectionChangedListener(new ISelectionChangedListener() {
 			@Override
 			public void selectionChanged(SelectionChangedEvent event) {
 				ISelection rawSelection = getTableViewer().getSelection();
 				if (rawSelection != null
 						&& rawSelection instanceof IStructuredSelection) {
 					IStructuredSelection selection = (IStructuredSelection) rawSelection;
 					if (selection.size() == 1) {
 						task = (String) selection.getFirstElement();
 						setProposals.schedule();
 					}
 				}
 			}			
 		});
 
        if (taskSelected != null) {
         	for (int i = 0; i < systems.length; i++) {
         		if (taskSelected.getProject().getSystem().equals(systems[i])) {
                 	systemCombo.select(i);
                 	break;
         		}
         	}
         }
         else systemCombo.select(0);
         setTasksAndProjects(true);
         
         systemCombo.setBounds(50, 50, 180, 65);
         systemCombo.addSelectionListener(new SelectionAdapter() {
             public void widgetSelected(SelectionEvent e) {
                 setTasksAndProjects(false);
             }
         });
         
         projectCombo.setBounds(50, 50, 180, 65);
         projectCombo.addSelectionListener(new SelectionAdapter() {
             public void widgetSelected(SelectionEvent e) {
                 setTasks();
             }
         });
         statusLineManager.createControl(parent);
         return parent;
     }
 
 	private void setTasksAndProjects(boolean first) {
 		systemSelected = systemCombo.getText();
 		List<String> projectList;
 		if (original) {
 			submissionService = new ExtensionManager<SubmissionService>(SubmissionService.SERVICE_ID).getService(
 					TimesheetApp.getSubmissionSystems().get(systemSelected));
 			projectList = new ArrayList<String>(submissionService.getAssignedProjects().keySet());
 		}
 		else {
 			projectList = storageService.getProjects(systemSelected);
 		}
 		String[] projects = projectList.toArray(new String[projectList.size()]);
         projectCombo.setItems(projects);
         if (projects.length == 1) projectCombo.setEnabled(false);
 		if (projectSelected == null && taskSelected != null && projectList.contains(taskSelected.getProject().getName()))
 			projectSelected = taskSelected.getProject().getName();
 		if (projectSelected != null) {
 			for (int i = 0; i < projects.length; i++) {
 				if (projectSelected.equals(projects[i])) {
 					projectCombo.select(i);
 					break;
 				}
 			}
 		}
         else projectCombo.select(0);
 		setTasks();
 	}
 
 	private void setTasks() {
 		projectSelected = projectCombo.getText();
 		if (original) {
 			submissionService = new ExtensionManager<SubmissionService>(SubmissionService.SERVICE_ID).getService(
 					TimesheetApp.getSubmissionSystems().get(systemSelected));
 			Map<String, Set<SubmissionEntry>> assignedProjects = submissionService.getAssignedProjects();
 			Set<SubmissionEntry> submissionTasks = assignedProjects.get(projectSelected);
 			if (submissionTasks == null) {
 				projectSelected = assignedProjects.keySet().iterator().next();
 				submissionTasks = assignedProjects.get(projectSelected);
 			}
 			List<String> tasks = new ArrayList<String>(storageService.findTasksBySystemAndProject(systemSelected, projectSelected));
 			tasksMap = new HashMap<String, SubmissionEntry>();
 			for (SubmissionEntry task : submissionTasks) {
 				if (!tasks.contains(task.getName())) tasksMap.put(task.getName(), task);
 			}
 			getTableViewer().setInput(tasksMap.keySet());
 		}
 		else {
 			List<String> tasks = new ArrayList<String>(storageService.findTasksBySystemAndProject(systemSelected, projectSelected));
			if (taskSelected != null && taskSelected.getProject().getName().equals(projectSelected)) {
 				if (taskSelected.getProject().getSystem().equals(systemSelected)) tasks.remove(this.taskSelected);
 			}
 	        getTableViewer().setInput(tasks);
 		}
 	}
 	
 	@Override
 	protected void okPressed() {
 		super.okPressed();
 		if (original) {
 		    String selectedTask = Arrays.toString(getResult());
 		    selectedTask = selectedTask.substring(selectedTask.indexOf("[") + 1, selectedTask.indexOf("]"));
 			if (StringUtils.isEmpty(selectedTask)) return;
 			Map<String, Set<SubmissionEntry>> projects = new HashMap<String, Set<SubmissionEntry>>();
 			projects.put(projectSelected, new HashSet<SubmissionEntry>());
 			projects.get(projectSelected).add(tasksMap.get(selectedTask));
 			storageService.importTasks(systemSelected, projects);
 		}
 	}
 
 	public String getSystem() {
 		return systemSelected;
 	}
 	
 	public String getProject() {
 		return projectSelected;
 	}
 
 	public String getComment() {
 		return comment;
 	}	
 }
