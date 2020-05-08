 package com.uwusoft.timesheet.dialog;
 
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.Date;
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
 import org.eclipse.jface.viewers.ITableLabelProvider;
 import org.eclipse.jface.viewers.LabelProvider;
 import org.eclipse.jface.viewers.SelectionChangedEvent;
 import org.eclipse.jface.viewers.StructuredSelection;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.ModifyEvent;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.DateTime;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.dialogs.ListDialog;
 import org.eclipse.ui.plugin.AbstractUIPlugin;
 import org.eclipse.ui.services.ISourceProviderService;
 
 import com.uwusoft.timesheet.Activator;
 import com.uwusoft.timesheet.TimesheetApp;
 import com.uwusoft.timesheet.commands.CheckinHandler;
 import com.uwusoft.timesheet.commands.SessionSourceProvider;
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
     private boolean showComment;
 	private String[] systems;
 	private Map<String, SubmissionEntry> tasksMap;
     private Combo systemCombo, projectCombo;
     private Text commentText;
     private AutoCompleteField commentCompleteField;
     private String projectSelected, systemSelected, task, comment, changeTitle, selectedTask;
 	private Date changeDate, rememberedTime;
     private int day, month, year, hours, minutes;
     private boolean original;
     private Job setProposals;
 	private StatusLineManager statusLineManager = new StatusLineManager();
 
 	class TaskLabelProvider extends LabelProvider implements ITableLabelProvider {
 		public String getColumnText(Object obj, int index) {
 			return getText(obj);
 		}
 
 		public Image getColumnImage(Object obj, int index) {
 			return getImage(obj);
 		}
 
 		public Image getImage(Object obj) {
 			return AbstractUIPlugin.imageDescriptorFromPlugin("com.uwusoft.timesheet", "/icons/task_16.png").createImage();
 		}
 	}
 
     public TaskListDialog(Shell shell, Task taskSelected, boolean showComment) {
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
        task = taskSelected.getName();
         this.showComment = showComment;
 		setContentProvider(ArrayContentProvider.getInstance());
 		setLabelProvider(new TaskLabelProvider());
 		setTitle("Tasks");
 		setMessage("Select task");
 		setWidthInChars(70);
     }
     
     public TaskListDialog(Shell shell, Task taskSelected, String comment) {
     	this(shell, taskSelected, true);
     	this.comment = comment;
     }
     
     public TaskListDialog(Display display, Task taskSelected, Date changeDate, String changeTitle) {
     	this(new Shell(display, SWT.NO_TRIM | SWT.ON_TOP), taskSelected, true);
     	this.changeDate = changeDate;
     	this.changeTitle = changeTitle;
 		Calendar calendar = Calendar.getInstance();
 		calendar.setTime(changeDate);
 		day = calendar.get(Calendar.DAY_OF_MONTH);
 		month = calendar.get(Calendar.MONTH);
 		year = calendar.get(Calendar.YEAR);
 		hours = calendar.get(Calendar.HOUR_OF_DAY);
 		minutes = calendar.get(Calendar.MINUTE);		
     }
     
     @Override
     protected Control createDialogArea(Composite composite) {
         if (changeDate != null) {
             final Composite timePanel = new Composite(composite, SWT.NONE);
             timePanel.setLayout(new GridLayout(3, false));
         	(new Label(timePanel, SWT.NULL)).setText(changeTitle + " at : ");
         	(new Label(timePanel, SWT.NULL)).setText(DateFormat.getDateInstance(DateFormat.SHORT).format(changeDate));
             final Button rememberButton = new Button(timePanel, SWT.PUSH);
             rememberButton.setImage(AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, "/icons/signs_16.png").createImage());
             final String text = "Remember time";
             rememberButton.setText(text);
             rememberButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
             rememberButton.setVisible(false);
             rememberButton.addSelectionListener(new SelectionAdapter() {
                 public void widgetSelected(SelectionEvent e) {
                 	if (rememberButton.getText().equals(text)) {
                 		rememberedTime = new Date();
                 		rememberButton.setText(new SimpleDateFormat("HH:mm").format(rememberedTime));
                 	}
                 	else {
                 		rememberedTime = null;
                 		rememberButton.setText(text);
                 	}
                 }
             });
             rememberButton.getDisplay().timerExec(5 * 60 * 1000, new Runnable() { // enable after five minutes
 				public void run() {
 					if (!rememberButton.isDisposed()) {
 						rememberButton.setVisible(true);
 						timePanel.pack();
 					}
 				}            	
             });
         	(new Label(timePanel, SWT.NULL)).setText("");
         	DateTime timeEntry = new DateTime(timePanel, SWT.TIME | SWT.SHORT);
             timeEntry.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
             timeEntry.setHours(hours);
             timeEntry.setMinutes(minutes);
             timeEntry.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
             timeEntry.addSelectionListener(new SelectionListener() {			
     			public void widgetSelected(SelectionEvent e) {
     				hours = ((DateTime) e.getSource()).getHours();
     				minutes = ((DateTime) e.getSource()).getMinutes();
     			}
     			public void widgetDefaultSelected(SelectionEvent e) {
     			}
     		});
             timeEntry.setFocus();
             Button breakButton = new Button(timePanel, SWT.PUSH);
             breakButton.setImage(AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, "/icons/pause_16.png").createImage());
             breakButton.setText("Set Break");
     		ISourceProviderService sourceProviderService = (ISourceProviderService) PlatformUI.getWorkbench().getService(ISourceProviderService.class);
     		SessionSourceProvider commandStateService = (SessionSourceProvider) sourceProviderService.getSourceProvider(SessionSourceProvider.SESSION_STATE);
             breakButton.setVisible(!CheckinHandler.title.equals(changeTitle)
             		&& commandStateService.getCurrentState().get(SessionSourceProvider.BREAK_STATE) == SessionSourceProvider.DISABLED);
             breakButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
             breakButton.addSelectionListener(new SelectionAdapter() {
                 public void widgetSelected(SelectionEvent e) {
                 	selectedTask = StorageService.BREAK;
                 	projectSelected = null;
                 	systemSelected = null;
                 	okPressed();
                 }
             });
         }
         
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
 
         if (showComment) {
             (new Label(parent, SWT.NULL)).setText("Comment: ");
             commentText = new Text(parent, SWT.NONE);
             commentCompleteField = new AutoCompleteField(commentText, new TextContentAdapter(), new String[] {StringUtils.EMPTY});
             commentText.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
             commentText.setText(comment == null ? "" : comment);
             commentText.addModifyListener(new ModifyListener() {
     			public void modifyText(ModifyEvent e) {
     				if (!StringUtils.isEmpty(task))
     				comment = commentText.getText();
     			}        	
             });
     		setProposals = new Job("Search available comments") {
     			protected IStatus run(IProgressMonitor monitor) {
     				commentCompleteField.setProposals(storageService.getUsedCommentsForTask(task, projectSelected, systemSelected));
     		        return Status.OK_STATUS;
     			}
     		};
     		getTableViewer().addSelectionChangedListener(new ISelectionChangedListener() {
     			@Override
     			public void selectionChanged(SelectionChangedEvent event) {
     				setProposals.cancel();
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
         }
 
        if (taskSelected.getProject().getSystem() != null) {
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
         else projectCombo.setEnabled(true);
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
 	        getTableViewer().setInput(tasks);
			if (taskSelected.getProject().getName() != null && taskSelected.getProject().getName().equals(projectSelected) && taskSelected.getProject().getSystem().equals(systemSelected)) {
 				for (int i = 0; i < tasks.size(); i++) {
 					if (tasks.get(i).equals(this.taskSelected.getName())) {
 						ISelection selection = new StructuredSelection(getTableViewer().getTable().getItem(i).getData());
 						getTableViewer().setSelection(selection);
 				        getTableViewer().getTable().setFocus();
 				        break;
 					}
 				}
 			}
 		}
 	}
 	
 	@Override
 	protected void okPressed() {
 		super.okPressed();
 		if (setProposals != null) setProposals.cancel();
 	    if (selectedTask == null) {
 	    	selectedTask = Arrays.toString(getResult());
 	    	selectedTask = selectedTask.substring(selectedTask.indexOf("[") + 1, selectedTask.indexOf("]"));
 	    }
 		if (StringUtils.isEmpty(selectedTask)) return;
 		if (original) {
 			Map<String, Set<SubmissionEntry>> projects = new HashMap<String, Set<SubmissionEntry>>();
 			projects.put(projectSelected, new HashSet<SubmissionEntry>());
 			projects.get(projectSelected).add(tasksMap.get(selectedTask));
 			storageService.importTasks(systemSelected, projects);
 		}
 	}
 
 	@Override
 	protected void cancelPressed() {
 		super.cancelPressed();
 		if (setProposals != null) setProposals.cancel();
 	}
 
     public String getTask() {
 		return selectedTask;
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
     
     public Date getTime() {
 		Calendar calendar = Calendar.getInstance();
 		calendar.set(Calendar.DAY_OF_MONTH, day);
 		calendar.set(Calendar.MONTH, month);
 		calendar.set(Calendar.YEAR, year);
 		calendar.set(Calendar.HOUR_OF_DAY, hours);
 		calendar.set(Calendar.MINUTE, minutes);
 		return calendar.getTime();
     }
 
 	public Date getRememberedTime() {
 		return rememberedTime;
 	}
 }
