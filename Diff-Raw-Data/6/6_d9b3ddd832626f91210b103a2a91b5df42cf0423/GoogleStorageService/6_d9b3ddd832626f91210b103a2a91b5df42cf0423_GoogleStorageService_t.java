 package com.uwusoft.timesheet.googlestorage;
 
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.HashSet;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.commons.lang3.StringUtils;
 import org.eclipse.core.commands.common.EventManager;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.ILog;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.jface.dialogs.Dialog;
 import org.eclipse.jface.preference.IPreferenceStore;
 import org.eclipse.swt.widgets.Display;
 
 import com.google.gdata.client.spreadsheet.FeedURLFactory;
 import com.google.gdata.client.spreadsheet.ListQuery;
 import com.google.gdata.client.spreadsheet.SpreadsheetService;
 import com.google.gdata.data.PlainTextConstruct;
 import com.google.gdata.data.spreadsheet.CellEntry;
 import com.google.gdata.data.spreadsheet.CellFeed;
 import com.google.gdata.data.spreadsheet.CustomElementCollection;
 import com.google.gdata.data.spreadsheet.ListEntry;
 import com.google.gdata.data.spreadsheet.ListFeed;
 import com.google.gdata.data.spreadsheet.WorksheetEntry;
 import com.google.gdata.data.spreadsheet.WorksheetFeed;
 import com.google.gdata.util.AuthenticationException;
 import com.google.gdata.util.ServiceException;
 import com.uwusoft.timesheet.Activator;
 import com.uwusoft.timesheet.TimesheetApp;
 import com.uwusoft.timesheet.dialog.LoginDialog;
 import com.uwusoft.timesheet.extensionpoint.StorageService;
 import com.uwusoft.timesheet.extensionpoint.SubmissionService;
 import com.uwusoft.timesheet.extensionpoint.model.DailySubmissionEntry;
 import com.uwusoft.timesheet.extensionpoint.model.SubmissionTask;
 import com.uwusoft.timesheet.model.Project;
 import com.uwusoft.timesheet.model.Task;
 import com.uwusoft.timesheet.util.DesktopUtil;
 import com.uwusoft.timesheet.util.ExtensionManager;
 import com.uwusoft.timesheet.util.MessageBox;
 import com.uwusoft.timesheet.util.SecurePreferencesManager;
 
 /**
  * storage service for Google Docs spreadsheet
  *
  * @author Uta Wunderlich
  * @version $Revision: $, $Date: Aug 15, 2011
  * @since Aug 15, 2011
  */
 public class GoogleStorageService extends EventManager implements StorageService {
 
 	public static final String PREFIX = "google.";
     public static final String SPREADSHEET_KEY="google.spreadsheet.key";
 
     private static final String dateFormat = "MM/dd/yyyy";
     private static final String timeFormat = "HH:mm";
     private String spreadsheetKey;
     private SpreadsheetService service;
     private FeedURLFactory factory;
     private URL listFeedUrl;
 	private Map<String, Integer> headingIndex;
     private List<WorksheetEntry> worksheets = new ArrayList<WorksheetEntry>();
     private WorksheetEntry defaultWorksheet;
     private Map<String,String> submissionSystems;
     private String message;
     private String title = "Google Storage Service";
     private ILog logger;
     
     public GoogleStorageService() throws CoreException {
         service = new SpreadsheetService("Timesheet");
         service.setProtocolVersion(SpreadsheetService.Versions.V1);
         boolean lastSuccess = true;
         do lastSuccess = authenticate(lastSuccess);
        	while (!lastSuccess);
 		factory = FeedURLFactory.getDefault();
 		headingIndex = new LinkedHashMap<String, Integer>();
 		submissionSystems = TimesheetApp.getSubmissionSystems();
         if (!reloadWorksheets()) return;
         logger = Activator.getDefault().getLog();
     }
     
     private boolean authenticate(boolean lastSuccess) throws CoreException {
         try {
 			IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();
 			SecurePreferencesManager secureProps = new SecurePreferencesManager("Google");
 	    	String userName = preferenceStore.getString(PREFIX + USERNAME);
 	    	String password = secureProps.getProperty(PREFIX + PASSWORD);
 	    	if (lastSuccess && !StringUtils.isEmpty(userName) && !StringUtils.isEmpty(password)) {
 	        	service.setUserCredentials(userName, password);
 	            spreadsheetKey = preferenceStore.getString(SPREADSHEET_KEY);
 	        	return true;
 	    	}
 	    	
 	    	Display display = Display.getDefault();
 	    	LoginDialog loginDialog = new LoginDialog(display, "Google Log in", message, userName, password);
 			if (loginDialog.open() == Dialog.OK) {
 	        	service.setUserCredentials(loginDialog.getUser(), loginDialog.getPassword());
 	        	preferenceStore.setValue(PREFIX + USERNAME, loginDialog.getUser());
 	        	if (loginDialog.isStorePassword())
 	        		secureProps.storeProperty(PREFIX + PASSWORD, loginDialog.getPassword());
 	        	else
 	        		secureProps.removeProperty(PREFIX + PASSWORD);
 	            spreadsheetKey = preferenceStore.getString(SPREADSHEET_KEY);
 	        	return true;
 			}
 		} catch (AuthenticationException e) {
 			message = e.getLocalizedMessage();
 			return false;
 		}
         throw new CoreException(new Status(IStatus.ERROR, Platform.PI_RUNTIME, IStatus.ERROR, message, null));
    }
 
     private boolean reloadSpreadsheetKey() {
     	if (StringUtils.isEmpty(spreadsheetKey)) {
 			IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();
             spreadsheetKey = preferenceStore.getString(SPREADSHEET_KEY);
     	}
 		try {
 	    	if (StringUtils.isEmpty(spreadsheetKey)) {
 	    		MessageBox.setMessage("Create spreadsheet", "Please manually create a spreadsheet and copy the spreadsheet key to the Google Spreadsheet Preferences!");
 	    		return false;
 	    	}
 			listFeedUrl = factory.getListFeedUrl(spreadsheetKey, "od6", "private", "full");
 			CellFeed cellFeed = service.getFeed(factory.getCellFeedUrl(spreadsheetKey, "od6", "private", "full"), CellFeed.class);
 			for (CellEntry entry : cellFeed.getEntries()) {
 				if (entry.getCell().getRow() == 1)
 					headingIndex.put(entry.getCell().getValue(), entry.getCell().getCol());
 				else
 					break;
 			}
 		} catch (MalformedURLException e) {
 			MessageBox.setError(title, e.getLocalizedMessage());
 		} catch (IOException e) {
 			MessageBox.setError(title, e.getLocalizedMessage());
 		} catch (ServiceException e) {
 			MessageBox.setError(title, e.getResponseBody());
 		}
     	return true;
     }
 
     private boolean reloadWorksheets() {
     	if (!reloadSpreadsheetKey()) return false;
 		try {
 			WorksheetFeed feed = service.getFeed(factory.getWorksheetFeedUrl(spreadsheetKey, "private", "full"), WorksheetFeed.class);
 	        worksheets = feed.getEntries();
 	        defaultWorksheet = worksheets.get(0);
 	        worksheets.remove(defaultWorksheet); // only task sheets remaining
 		} catch (MalformedURLException e) {
 			MessageBox.setError(title, e.getLocalizedMessage());
 		} catch (IOException e) {
 			MessageBox.setError(title, e.getLocalizedMessage());
 		} catch (ServiceException e) {
 			MessageBox.setError(title, e.getResponseBody());
 		}
 		return true;
     }
 
     public void addPropertyChangeListener(final PropertyChangeListener listener) { 
 		addListenerObject(listener);
     } 
    
     public void removePropertyChangeListener(final PropertyChangeListener listener) { 
 		removeListenerObject(listener);
     } 
 
     protected void firePropertyChangeEvent(final PropertyChangeEvent event) {
 		if (event == null) {
 			throw new NullPointerException();
 		}
 
 		for (Object listener : getListeners()) {
 			((PropertyChangeListener) listener).propertyChange(event);
 		}    	
     }
     
     public List<String> getSystems() {
     	List<String> systems = new ArrayList<String>();
     	for (WorksheetEntry worksheet : worksheets) {
             String title = worksheet.getTitle().getPlainText();
     		if(title.endsWith(SubmissionService.PROJECTS)) continue;
 	        systems.add(title); 			
     	}
         return systems;
     }
     
     public List<String> getProjects(String system) {
     	List<String> projects = new ArrayList<String>();
 		URL worksheetListFeedUrl = getListFeedUrl(system + SubmissionService.PROJECTS);
 		try {
 			ListFeed feed = service.getFeed(worksheetListFeedUrl, ListFeed.class);
 			for (ListEntry entry : feed.getEntries()) {
 				projects.add(entry.getCustomElements().getValue(PROJECT));
 			}
 		} catch (IOException e) {
 			MessageBox.setError(title, e.getLocalizedMessage());
 		} catch (ServiceException e) {
 			MessageBox.setError(title, e.getResponseBody());
 		}
         return projects;    	
     }
     
     public List<String> findTasksBySystemAndProject(String system, String project) {
     	List<String> tasks = new ArrayList<String>();
 		URL worksheetListFeedUrl = getListFeedUrl(system);
 		ListQuery query = new ListQuery(worksheetListFeedUrl);
 		query.setSpreadsheetQuery(PROJECT.toLowerCase() + " = \"" + project + "\"");
 		try {
 			ListFeed feed = service.query(query, ListFeed.class);
 			List<ListEntry> listEntries = feed.getEntries();
 			for (ListEntry entry : listEntries) {
 				tasks.add(entry.getCustomElements().getValue(TASK));
 			}
 		} catch (IOException e) {
 			MessageBox.setError(title, e.getLocalizedMessage());
 		} catch (ServiceException e) {
 			MessageBox.setError(title, e.getResponseBody());
 		}
     	return tasks;
     }
     
     public List<Task> getTaskEntries(Date date) {
     	List <Task> taskEntries = new ArrayList<Task>();
 		try {
             ListQuery query = new ListQuery(listFeedUrl);
     		query.setSpreadsheetQuery(DATE.toLowerCase() + " = " + new SimpleDateFormat(dateFormat).format(date));
 	        List<ListEntry> listEntries = service.query(query, ListFeed.class).getEntries();
 	        for (ListEntry listEntry : listEntries) {
 	            CustomElementCollection elements = listEntry.getCustomElements();
 	            if (elements.getValue(DATE) == null) continue;
 	            if (!new SimpleDateFormat(dateFormat).format(new SimpleDateFormat(dateFormat).parse(elements.getValue(DATE)))
 	            		.equals(new SimpleDateFormat(dateFormat).format(date))) break;
 	            
 	            String task = elements.getValue(TASK);
 	            if (task == null || elements.getValue(TIME) == null) break;
 	            Long id = Long.parseLong(elements.getValue(ID));
 	            if (CHECK_IN.equals(task) || BREAK.equals(task))
 	            	taskEntries.add(new Task(id, new SimpleDateFormat(timeFormat).parse(elements.getValue(TIME)),
 	            			task, 0, new Project()));
 	            else
 	            	taskEntries.add(new Task(id, new SimpleDateFormat(timeFormat).parse(elements.getValue(TIME)),
 	            			task, Float.parseFloat(elements.getValue(TOTAL)), new Project(elements.getValue(PROJECT), getSystem(id.intValue()))));
 	        }
 		} catch (IOException e) {
 			MessageBox.setError(title, e.getLocalizedMessage());
 		} catch (ServiceException e) {
 			MessageBox.setError(title, e.getResponseBody());
 		} catch (ParseException e) {
 			MessageBox.setError(title, e.getLocalizedMessage());
 		}
 		return taskEntries;
     }
     
     public void createTaskEntry(Task task) {
         try {
             if (!reloadWorksheets()) return;
 			ListEntry timeEntry = new ListEntry();
 			
 			if (task.getDateTime() != null) {
 		        Calendar cal = new GregorianCalendar();
 		        cal.setTime(task.getDateTime());
 		        //cal.setFirstDayOfWeek(Calendar.MONDAY);
 				timeEntry.getCustomElements().setValueLocal(WEEK, Integer.toString(cal.get(Calendar.WEEK_OF_YEAR)));
 				timeEntry.getCustomElements().setValueLocal(DATE,
 						new SimpleDateFormat(dateFormat).format(task.getDateTime()));			
 				if (task.getTotal() == 0)
 					timeEntry.getCustomElements().setValueLocal(TIME, new SimpleDateFormat(timeFormat).format(task.getDateTime()));
 			}
 			else timeEntry.getCustomElements().setValueLocal(TOTAL, Float.toString(task.getTotal()));
 			
 			String taskLink = null;
 			if (CHECK_IN.equals(task.getTask()) || BREAK.equals(task.getTask()))
 				timeEntry.getCustomElements().setValueLocal(TASK, task.getTask());
 			else {
 				if (task.getTotal() != 0) {
 					timeEntry.getCustomElements().setValueLocal(TOTAL, Float.toString(task.getTotal()));
 					if (task.isWholeDay())
 						timeEntry.getCustomElements().setValueLocal(DAILY_TOTAL, Float.toString(task.getTotal()));
 				}
 				taskLink = getTaskLink(task.getTask(), task.getProject().getName(), task.getProject().getSystem());
 			}
 			if (task.getComment() != null) timeEntry.getCustomElements().setValueLocal(COMMENT, task.getComment());
 			service.insert(listFeedUrl, timeEntry);
 
             if (!reloadWorksheets()) return;
             
             logger.log(new Status(IStatus.INFO, Activator.PLUGIN_ID, "create task entry: " + task
             		+ (taskLink == null ? "" : " (task link: " + taskLink +")")));
             
         	ListFeed feed = service.getFeed(listFeedUrl, ListFeed.class);
         	if (feed.getTotalResults() == 1) {
     			MessageBox.setError(title, "Couldn't insert cell (Maybe there's an empty line in the spreadsheet)");
     			return;
         	}
             createUpdateCellEntry(defaultWorksheet,	feed.getEntries().size() + 1, headingIndex.get(ID), "=ROW()");
             
             if (taskLink != null) {
 				updateTask(taskLink, feed.getEntries().size() + 1);
 				// if no total set: the (temporary) total of the task will be calculated by: end time - end time of the previous task
 				if (task.getTotal() == 0)
 					createUpdateCellEntry(defaultWorksheet,	feed.getEntries().size() + 1, headingIndex.get(TOTAL), "=(R[0]C[-1]-R[-1]C[-1])*24"); // calculate task total
 			}
 			firePropertyChangeEvent(new PropertyChangeEvent(this, "tasks", null, null));
 		} catch (IOException e) {
 			MessageBox.setError(title, e.getLocalizedMessage());
 		} catch (ServiceException e) {
 			MessageBox.setError(title, e.getResponseBody());
 		}
     }
 
 	@Override
 	public Task getLastTask() {
     	try {
 			ListFeed feed = service.getFeed(listFeedUrl, ListFeed.class);
 			CustomElementCollection elements = feed.getEntries().get(feed.getEntries().size() - 1).getCustomElements();
 			if (elements.getValue(DATE) == null && elements.getValue(TIME) == null) // if date and time isn't set yet this should be the last task
				return new Task(Long.parseLong(elements.getValue(ID)), elements.getValue(TASK), new Project(elements.getValue(PROJECT), getSystem(feed.getEntries().size() + 1)));
 			return null;
 		} catch (IOException e) {
 			MessageBox.setError(title, e.getLocalizedMessage());
 		} catch (ServiceException e) {
 			MessageBox.setError(title, e.getResponseBody());
 		}
 		return null;
 	}
 
 	private void updateTask(String taskLink, int row) {
 		createUpdateCellEntry(defaultWorksheet,	row,
 				headingIndex.get(TASK), "=" + taskLink);
 		createUpdateCellEntry(defaultWorksheet,	row,
 				headingIndex.get(PROJECT), "=" + taskLink.replace("!A", "!B")); // TODO hardcoded: project must be in the second column
 	}
 
     public void storeLastDailyTotal() {
         try {
             if (!reloadWorksheets()) return;
             ListFeed feed = service.getFeed(listFeedUrl, ListFeed.class);
             List<ListEntry> listEntries = feed.getEntries();			
             int rowsOfDay = 0;
             for (int i = listEntries.size() - 2; i > 0; i--, rowsOfDay++) {
                 CustomElementCollection elements = listEntries.get(i).getCustomElements();
                 if (CHECK_IN.equals(elements.getValue(TASK))) break; // begin of day
             }
             createUpdateCellEntry(defaultWorksheet, listEntries.size() + 1, headingIndex.get(DAILY_TOTAL), "=SUM(R[0]C[-1]:R[-" + rowsOfDay + "]C[-1])");
         } catch (IOException e) {
 			MessageBox.setError(title, e.getLocalizedMessage());
         } catch (ServiceException e) {
 			MessageBox.setError(title, e.getResponseBody());
         }
     }
 
     public void storeLastWeekTotal(String weeklyWorkingHours) {
         try {
             if (!reloadWorksheets()) return;
             ListFeed feed = service.getFeed(listFeedUrl, ListFeed.class);
             List<ListEntry> listEntries = feed.getEntries();
             int rowsOfWeek = 0;
             for (int i = listEntries.size() - 2; i > 0; i--, rowsOfWeek++) {
                 CustomElementCollection elements = listEntries.get(i).getCustomElements();
                 if (elements.getValue(WEEKLY_TOTAL) != null) break; // end of last week
                 if (i==1) break;
             }            
             ListEntry timeEntry = new ListEntry();
 			timeEntry.getCustomElements().setValueLocal(WEEKLY_TOTAL, "0");
 			service.insert(listFeedUrl, timeEntry);
             if (!reloadWorksheets()) return;
 			
 			createUpdateCellEntry(defaultWorksheet, listEntries.size() + 2, headingIndex.get(WEEKLY_TOTAL), "=SUM(R[-1]C[-1]:R[-" + ++rowsOfWeek + "]C[-1])");
 			createUpdateCellEntry(defaultWorksheet, listEntries.size() + 2, headingIndex.get(OVERTIME), "=R[0]C["
 						+ (headingIndex.get(WEEKLY_TOTAL) - headingIndex.get(OVERTIME)) + "]-" +weeklyWorkingHours+ "+" +"R[-" + ++rowsOfWeek + "]C[0]");
         } catch (IOException e) {
 			MessageBox.setError(title, e.getLocalizedMessage());
         } catch (ServiceException e) {
 			MessageBox.setError(title, e.getResponseBody());
         }        
     }
 
 	public void updateTaskEntry(Date time, Long id) {
 		createUpdateCellEntry(defaultWorksheet, id.intValue(), headingIndex.get(TIME), new SimpleDateFormat(timeFormat).format(time));
        createUpdateCellEntry(defaultWorksheet, id.intValue(), headingIndex.get(DATE), new SimpleDateFormat(dateFormat).format(time));			
         Calendar cal = new GregorianCalendar();
         cal.setTime(time);
         //cal.setFirstDayOfWeek(Calendar.MONDAY);
         createUpdateCellEntry(defaultWorksheet, id.intValue(), headingIndex.get(WEEK), Integer.toString(cal.get(Calendar.WEEK_OF_YEAR)));
 		firePropertyChangeEvent(new PropertyChangeEvent(this, "tasks", null, null));
 	}
 
 	public void updateTaskEntry(Task task, Long id) {
 		if (CHECK_IN.equals(task.getTask()) || BREAK.equals(task.getTask()))
 			createUpdateCellEntry(defaultWorksheet, id.intValue(), headingIndex.get(TASK), task.getTask());
 		else
 			updateTask(getTaskLink(task.getTask(), task.getProject().getName(), task.getProject().getSystem()), id.intValue());
 		firePropertyChangeEvent(new PropertyChangeEvent(this, "tasks", null, null));
 	}
 
 	public void importTasks(String submissionSystem, Map<String, Set<SubmissionTask>> projects) {
 		try {
 			URL worksheetListFeedUrl = null;
 			if ((worksheetListFeedUrl = getListFeedUrl(submissionSystem)) != null) {
 				for (String project : projects.keySet()) {
 					ListQuery query = new ListQuery(worksheetListFeedUrl);
 					query.setSpreadsheetQuery(PROJECT.toLowerCase() + " = \"" + project + "\"");
 					try {
 						ListFeed feed = service.query(query, ListFeed.class);
 						List<ListEntry> listEntries = feed.getEntries();
 						Set<SubmissionTask> tasks = new HashSet<SubmissionTask>(projects.get(project));
 						
 						for (ListEntry entry : listEntries) // collect available tasks
 							for (SubmissionTask task : projects.get(project))
 								if (task.getName().equals(entry.getCustomElements().getValue(TASK)))
 									tasks.remove(task);							
 						projects.put(project, tasks);
 					} catch (IOException e) {
 						MessageBox.setError(title, e.getLocalizedMessage());
 					} catch (ServiceException e) {
 						MessageBox.setError(title, e.getResponseBody());
 					}
 				}
 			}
 			else {
 				createWorksheet(submissionSystem);
 				
 	            if (!reloadWorksheets()) return;
 	            WorksheetEntry worksheet = getWorksheet(submissionSystem);
 			    worksheetListFeedUrl = worksheet.getListFeedUrl(); 			
 				// create column headers:
 			    createUpdateCellEntry(worksheet, 1, 1, TASK);
 				createUpdateCellEntry(worksheet, 1, 2, PROJECT);
 				createUpdateCellEntry(worksheet, 1, 3, ID);
 			}
 			for (String project : projects.keySet()) {
 				for (SubmissionTask task : projects.get(project)) {
 					ListEntry taskEntry = new ListEntry();
 					taskEntry.getCustomElements().setValueLocal(TASK, task.getName());
 					taskEntry.getCustomElements().setValueLocal(ID, Long.toString(task.getId()));
 					taskEntry.getCustomElements().setValueLocal(PROJECT + ID, Long.toString(task.getProjectId()));
 		            
 					logger.log(new Status(IStatus.INFO, Activator.PLUGIN_ID, "Import task: " + task.getName() + " id=" + task.getId()
 		            		+ " (" + task.getProjectName() + " id=" + task.getProjectId() + ") "));
 					
 					service.insert(worksheetListFeedUrl, taskEntry);
 					reloadWorksheets();
 					if (!StringUtils.isEmpty(project)) {
 						String projectLink = getProjectLink(submissionSystem, project, task.getProjectId(), true);
 						if (projectLink != null) {
 							WorksheetEntry worksheet = getWorksheet(submissionSystem);
 							ListFeed feed = service.getFeed(worksheet.getListFeedUrl(), ListFeed.class);
 							createUpdateCellEntry(worksheet, feed.getEntries().size() + 1,
 									getHeadingIndex(submissionSystem, PROJECT), "=" + projectLink);
 						}
 					}
 				}
 			}
 		} catch (MalformedURLException e) {
 			MessageBox.setError(title, e.getLocalizedMessage());
 		} catch (IOException e) {
 			MessageBox.setError(title, e.getLocalizedMessage());
 		} catch (ServiceException e) {
 			MessageBox.setError(title, e.getResponseBody());
 		}
 	}
 
 	private void createWorksheet(String system) throws IOException,
 			ServiceException, MalformedURLException {
 		WorksheetEntry newWorksheet = new WorksheetEntry();
 		newWorksheet.setTitle(new PlainTextConstruct(system));
 		newWorksheet.setColCount(6);
 		newWorksheet.setRowCount(20);
 		service.insert(factory.getWorksheetFeedUrl(spreadsheetKey, "private", "full"), newWorksheet);
 	}
 
 	public Set<String> submitEntries(int weekNum) {
         Set<String> systems = new HashSet<String>();
 		try {
             if (!reloadWorksheets()) return systems;
     		
             ListQuery query = new ListQuery(listFeedUrl);
     		query.setSpreadsheetQuery(SUBMISSION_STATUS.toLowerCase() + " != \"Submitted\" and "
     								+ WEEK.toLowerCase() + " = \"" + weekNum + "\"");
 	        List<ListEntry> listEntries = service.query(query, ListFeed.class).getEntries();
             
             if (listEntries.isEmpty()) return systems;
 	        Date lastDate = new SimpleDateFormat(dateFormat).parse(listEntries.get(0).getCustomElements().getValue(DATE));
             DailySubmissionEntry entry = new DailySubmissionEntry(lastDate);
 
             for (ListEntry listEntry : listEntries) {
             	CustomElementCollection elements = listEntry.getCustomElements();
 	            String task = elements.getValue(TASK);
 	            if ("Submitted".equals(elements.getValue(SUBMISSION_STATUS)) || task == null || CHECK_IN.equals(task) || BREAK.equals(task)) {
 	            	continue;
 	            }
 	            if (elements.getValue(DATE) == null) continue;
 	            Date date = new SimpleDateFormat(dateFormat).parse(elements.getValue(DATE));
 	            if (!date.equals(lastDate)) { // another day
 	            	entry.submitEntries();
 	                entry = new DailySubmissionEntry(date);
 	                lastDate = date;
 	            }
 
 	            String system = getSystem(Integer.parseInt(elements.getValue(ID)));
 	            String project = elements.getValue(PROJECT);
 				if (submissionSystems.containsKey(system)) {
 					systems.add(system);
 					SubmissionTask submissionTask = getSubmissionTask(task, project, system);
 					if (submissionTask != null)
 						entry.addSubmissionEntry(submissionTask, Double.valueOf(elements.getValue(TOTAL)));
 				}
             	createUpdateCellEntry(defaultWorksheet,	Integer.parseInt(elements.getValue(ID)), headingIndex.get(SUBMISSION_STATUS), "Submitted");
     		}
             entry.submitEntries();
 		} catch (IOException e) {
 			MessageBox.setError(title, e.getLocalizedMessage());
 		} catch (ServiceException e) {
 			MessageBox.setError(title, e.getResponseBody());
 		} catch (ParseException e) {
 			MessageBox.setError(title, e.getLocalizedMessage());
 		}
 		return systems;
     }
 
 	public void submitFillTask(Date date) {
 		IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();
 		if (!StringUtils.isEmpty(preferenceStore.getString(TimesheetApp.DAILY_TASK))) {
 			Task task = TimesheetApp.createTask(date, TimesheetApp.DAILY_TASK);
 			if (submissionSystems.containsKey(task.getProject().getSystem())) {
 				SubmissionTask submissionTask = getSubmissionTask(task.getTask(), task.getProject().getName(), task.getProject().getSystem());
 				if (submissionTask != null)
 					new ExtensionManager<SubmissionService>(SubmissionService.SERVICE_ID).getService(submissionSystems.get(task.getProject().getSystem()))
 							.submit(date, submissionTask, Double.parseDouble(preferenceStore.getString(TimesheetApp.DAILY_TASK_TOTAL)));
 			}
 		}
 	}
 	
 	private String getSystem(int row) throws IOException, ServiceException {
 		String system = getInputValue(row, headingIndex.get(TASK)).split("!")[0];
 		return system.substring(system.indexOf("=") + 1);
 	}
 
 	private String getInputValue(int row, int col) throws IOException, ServiceException,
 			MalformedURLException {
 		CellEntry cellEntry = service.getEntry(new URL(defaultWorksheet.getCellFeedUrl().toString()
 				+ "/" + "R" + row + "C" + col), CellEntry.class);
 		return cellEntry.getCell().getInputValue();
 	}
 	
 	private WorksheetEntry getWorksheet(String system) {
 		for (WorksheetEntry worksheet : worksheets) {
 		    if(system.equals(worksheet.getTitle().getPlainText())) {
 		        return worksheet; 			
 		    }
 		}
 		return null;
 	}
 	
 	private URL getListFeedUrl(String system) {
 		WorksheetEntry worksheet = getWorksheet(system);
 		if (worksheet == null) return null;
 		return worksheet.getListFeedUrl();
 	}
 	
 	private Integer getHeadingIndex(String system, String heading) {
 		WorksheetEntry worksheet = getWorksheet(system);
 		if (worksheet == null) return null;		
 		try {
 			CellFeed cellFeed = service.getFeed(worksheet.getCellFeedUrl(), CellFeed.class);
 			for (CellEntry entry : cellFeed.getEntries()) {
 				if (entry.getCell().getRow() == 1) {
 					if (heading.equals(entry.getCell().getValue()))
 						return entry.getCell().getCol();
 				}
 				else
 					break;
 			}
 		} catch (IOException e) {
 			MessageBox.setError(title, e.getLocalizedMessage());
 		} catch (ServiceException e) {
 			MessageBox.setError(title, e.getResponseBody());
 		}
 		return null;
 	}
 
 	private String getProjectLink(String system, String project, Long projectId, boolean createNew) {
 		String systemProjects = system + SubmissionService.PROJECTS;
 		URL worksheetListFeedUrl = getListFeedUrl(systemProjects);
 		try {
 			if (worksheetListFeedUrl == null) {
 				createWorksheet(systemProjects);
 				if (!reloadWorksheets()) return null;
 	            WorksheetEntry worksheet = getWorksheet(systemProjects);
 			    worksheetListFeedUrl = worksheet.getListFeedUrl(); 			
 				// create column headers:
 			    createUpdateCellEntry(worksheet, 1, 1, PROJECT);
 			    createUpdateCellEntry(worksheet, 1, 2, ID);
 			}
 			ListFeed feed = service.getFeed(worksheetListFeedUrl, ListFeed.class);
 			List<ListEntry> entries = feed.getEntries();
 			for (int i = 0; i < entries.size(); i++) {
 				if(project.equals(entries.get(i).getCustomElements().getValue(PROJECT)))
 					return systemProjects + "!A" + (i + 2); // TODO hardcoded: project must be in the first column					
 			}
 			if (createNew) {
 				ListEntry listEntry = new ListEntry();
 				listEntry.getCustomElements().setValueLocal(PROJECT, project);
 				listEntry.getCustomElements().setValueLocal(ID, Long.toString(projectId));
 				service.insert(worksheetListFeedUrl, listEntry);
 	            if (!reloadWorksheets()) return null;
 				return getProjectLink(system, project, projectId, false);
 			}
 		} catch (IOException e) {
 			MessageBox.setError(title, e.getLocalizedMessage());
 		} catch (ServiceException e) {
 			MessageBox.setError(title, e.getResponseBody());
 		}
 		return null;
 	}
 	
 	private String getTaskLink(String task, String project, String system) {
         if (system == null) { // search for task in all task worksheets
         	for (WorksheetEntry worksheet : worksheets) {
         		String title = worksheet.getTitle().getPlainText();
         		if(title.endsWith(SubmissionService.PROJECTS)) continue;
         		String taskLink = getTaskLink(task, project, worksheet);
         		if (taskLink != null) return taskLink;
         	}
         }
 		return getTaskLink(task, project, getWorksheet(system));
 	}
 
 	private String getTaskLink(String task, String project, WorksheetEntry worksheet) {
 		try {
 			ListFeed feed = service.getFeed(worksheet.getListFeedUrl(),	ListFeed.class);
 			List<ListEntry> entries = feed.getEntries();
 			for (int i = 0; i < entries.size(); i++) {
 				if (project == null && task.equals(entries.get(i).getCustomElements().getValue(TASK))
 					|| task.equals(entries.get(i).getCustomElements().getValue(TASK))
 						&& project.equals(entries.get(i).getCustomElements().getValue(PROJECT)))
 					return worksheet.getTitle().getPlainText() + "!A" + (i + 2); // TODO hardcoded: task must be in the first column
 			}
 		} catch (IOException e) {
 			MessageBox.setError(title, e.getLocalizedMessage());
 		} catch (ServiceException e) {
 			MessageBox.setError(title, e.getResponseBody());
 		}
 		return null;
 	}
 	
 	private SubmissionTask getSubmissionTask(String task, String project, String system) {
 		ListQuery query = new ListQuery(getWorksheet(system).getListFeedUrl());
 		query.setSpreadsheetQuery(TASK.toLowerCase() + " = \"" + task + "\" and "
 								+ PROJECT.toLowerCase() + " = \"" + project + "\"");
 		try {
 			ListFeed feed = service.query(query, ListFeed.class);
 			for (ListEntry entry : feed.getEntries()) {
 				if (entry.getCustomElements().getValue(PROJECT + ID) == null) return null;
 				return new SubmissionTask(Long.parseLong(entry.getCustomElements().getValue(PROJECT + ID)),
 						Long.parseLong(entry.getCustomElements().getValue(ID)),
 						entry.getCustomElements().getValue(TASK), entry.getCustomElements().getValue(PROJECT), system);
 			}
 		} catch (IOException e) {
 			MessageBox.setError(title, e.getLocalizedMessage());
 		} catch (ServiceException e) {
 			MessageBox.setError(title, e.getResponseBody());
 		}
 		return null;
 	}
 	
     private void createUpdateCellEntry(WorksheetEntry worksheet, int row, int col, String value) {
 		try {
 			CellEntry entry = service.getEntry(new URL(worksheet.getCellFeedUrl().toString() + "/" + "R" + row + "C" + col), CellEntry.class);
 	        entry.changeInputValueLocal(value);
 	        entry.update();
 		} catch (MalformedURLException e) {
 			MessageBox.setError(title, e.getLocalizedMessage());
 		} catch (IOException e) {
 			MessageBox.setError(title, e.getLocalizedMessage());
 		} catch (ServiceException e) {
 			MessageBox.setError(title, e.getResponseBody());
 		}
     }
 
 	@Override
 	public void openUrl(String openBrowser) {
 		IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();
 		if (preferenceStore.getBoolean(PREFIX + openBrowser))
 			DesktopUtil.openUrl("https://docs.google.com/spreadsheet/ccc?key=" + preferenceStore.getString(SPREADSHEET_KEY) + "&pli=1#gid=0");
 	}
 }
