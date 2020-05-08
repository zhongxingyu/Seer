 package com.uwusoft.timesheet.model;
 
 import java.sql.Timestamp;
 import java.util.Date;
 import java.util.List;
 
 import javax.persistence.EntityManager;
 import javax.persistence.Query;
 
 import org.eclipse.jface.preference.IPreferenceStore;
 
 import com.uwusoft.timesheet.Activator;
 import com.uwusoft.timesheet.TimesheetApp;
 import com.uwusoft.timesheet.extensionpoint.LocalStorageService;
 import com.uwusoft.timesheet.extensionpoint.StorageService;
 import com.uwusoft.timesheet.util.BusinessDayUtil;
 import com.uwusoft.timesheet.util.ExtensionManager;
 import com.uwusoft.timesheet.util.MessageBox;
 
 public class WholeDayTasks {
 	private static WholeDayTasks instance;
 	private static String BEGIN_WDT = "BEGIN_WDT";
 	private static LocalStorageService localStorageService;
 	private Date nextBegin;
 	private float total;
 	private EntityManager em;
 	
 	public static WholeDayTasks getInstance() {
 		if (instance == null) instance = new WholeDayTasks();
 		return instance;
 	}
 	
 	private WholeDayTasks() {
 		em = TimesheetApp.factory.createEntityManager();
 		Query q = em.createQuery("select t from TaskEntry t where t.wholeDay=true order by t.dateTime desc");
 		@SuppressWarnings("unchecked")
 		List<TaskEntry> taskEntryList = q.getResultList();
 		Date begin;
 		if (taskEntryList.isEmpty()) {
 			begin = BusinessDayUtil.getNextBusinessDay(new Date());
 			em.getTransaction().begin();
 			@SuppressWarnings("unchecked")
 			List<Task> beginTasks = em.createQuery("select t from Task t where t.name = :name")
 				.setParameter("name", BEGIN_WDT)
 				.getResultList();
 			Task beginTask;
 			if (beginTasks.isEmpty()) {
 				beginTask = new Task(BEGIN_WDT);
 				em.persist(beginTask);
 			}
 			else
 				beginTask = beginTasks.iterator().next();
 			em.persist(new TaskEntry(begin, beginTask));
 			em.getTransaction().commit();
 		}
 		else
 			begin = BusinessDayUtil.getNextBusinessDay(taskEntryList.iterator().next().getDateTime());
 		nextBegin = begin;
 		
 		IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore(); 
 		total = new Float(preferenceStore.getInt(TimesheetApp.WORKING_HOURS) / 5); // TODO define non working days
 		
 		localStorageService = new LocalStorageService();
 	}
 
 	public void addNextTask(Date to, String name) {
 		em.getTransaction().begin();
 		Task task = TimesheetApp.createTask(name);
 		Project project = localStorageService.findProjectByNameAndSystem(task.getProject().getName(), task.getProject().getSystem());
 		if (project == null) em.persist(task.getProject());
 		else task.setProject(project);
 		
 		Task foundTask = localStorageService.findTaskByNameProjectAndSystem(
 				task.getName(), task.getProject().getName(), task.getProject().getSystem());
 		if (foundTask == null) em.persist(task);
 		else task = foundTask;
 		
 		TaskEntry taskEntry = new TaskEntry(to, task, total, true);
 		em.persist(taskEntry);
 		em.getTransaction().commit();
 		
 		nextBegin = BusinessDayUtil.getNextBusinessDay(to);
 	}	
 	
 	/**
 	 * @return next business day after end date of all stored whole day tasks
 	 */
 	public Date getNextBegin() {
 		return nextBegin;
 	}
 
 	public void createTaskEntries() {
 		IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();
 		StorageService storageService = new ExtensionManager<StorageService>(StorageService.SERVICE_ID)
 				.getService(preferenceStore.getString(StorageService.PROPERTY));
 		@SuppressWarnings("unchecked")
 		List<TaskEntry> taskEntryList = em.createQuery("select t from TaskEntry t where t.wholeDay=true order by t.dateTime asc")
 				.getResultList();
 		if (taskEntryList.isEmpty()) return;
 		
 		@SuppressWarnings("unchecked")
 		List<TaskEntry> beginTaskEntryList = em.createQuery("select t from TaskEntry t where t.task.name = :name")
 				.setParameter("name", BEGIN_WDT)
 				.getResultList();
 		if (beginTaskEntryList.isEmpty()) return;
 		
 		em.getTransaction().begin();
 		TaskEntry beginTaskEntry = beginTaskEntryList.iterator().next();
 		Date begin = beginTaskEntry.getDateTime();
 		em.remove(beginTaskEntry);
 		
 		for (TaskEntry taskEntry : taskEntryList) {
 			Date end = new Date(taskEntry.getDateTime().getTime());
 			do {
				if (BusinessDayUtil.isAnotherWeek(begin, end))
					storageService.storeLastWeekTotal(preferenceStore.getString(TimesheetApp.WORKING_HOURS)); // store Week and Overtime
 				taskEntry.setDateTime(new Timestamp(begin.getTime()));
 				storageService.createTaskEntry(taskEntry);
 				MessageBox.setMessage("Set whole day task", begin + "\n" + taskEntry); // TODO create confirm dialog
 			} while (!(begin = BusinessDayUtil.getNextBusinessDay(begin)).after(end));
 			em.remove(taskEntry);
 		}
 		em.getTransaction().commit();
 	}
 }
