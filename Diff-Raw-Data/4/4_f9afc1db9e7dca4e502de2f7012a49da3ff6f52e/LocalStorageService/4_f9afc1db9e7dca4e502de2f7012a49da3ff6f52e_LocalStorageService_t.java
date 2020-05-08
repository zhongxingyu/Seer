 package com.uwusoft.timesheet.extensionpoint;
 
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.sql.Timestamp;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collection;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import javax.persistence.EntityManager;
 import javax.persistence.EntityManagerFactory;
 import javax.persistence.Persistence;
 import javax.persistence.criteria.CriteriaBuilder;
 import javax.persistence.criteria.CriteriaQuery;
 import javax.persistence.criteria.Path;
 import javax.persistence.criteria.Predicate;
 import javax.persistence.criteria.Root;
 
 import org.apache.commons.lang.time.DateUtils;
 import org.apache.commons.lang3.StringUtils;
 import org.eclipse.core.commands.common.EventManager;
 import org.eclipse.core.runtime.ILog;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.jface.preference.IPreferenceStore;
 
 import com.uwusoft.timesheet.Activator;
 import com.uwusoft.timesheet.TimesheetApp;
 import com.uwusoft.timesheet.extensionpoint.model.DailySubmissionEntry;
 import com.uwusoft.timesheet.extensionpoint.model.SubmissionEntry;
 import com.uwusoft.timesheet.model.AllDayTasks;
 import com.uwusoft.timesheet.model.Project;
 import com.uwusoft.timesheet.model.Project_;
 import com.uwusoft.timesheet.model.Task;
 import com.uwusoft.timesheet.model.TaskEntry;
 import com.uwusoft.timesheet.model.TaskEntry_;
 import com.uwusoft.timesheet.model.Task_;
 import com.uwusoft.timesheet.submission.model.SubmissionProject;
 import com.uwusoft.timesheet.submission.model.SubmissionTask;
 import com.uwusoft.timesheet.util.BusinessDayUtil;
 import com.uwusoft.timesheet.util.ExtensionManager;
 import com.uwusoft.timesheet.util.StorageSystemSetup;
 
 public class LocalStorageService extends EventManager implements StorageService {
 
 	private static final String PERSISTENCE_UNIT_NAME = "timesheet";
 	public static EntityManagerFactory factory;	
 	private static EntityManager em;
     private Map<String,String> submissionSystems;
     private Job syncEntriesJob, syncTasksJob;
     private static LocalStorageService instance;
     private StorageService storageService;
     private String submissionSystem;
     private TaskEntry lastTaskEntry;
     private ILog logger;
 
 	private LocalStorageService() {
 		Map<String, Object> configOverrides = new HashMap<String, Object>();
 		configOverrides.put("javax.persistence.jdbc.url",
 				"jdbc:derby:" + System.getProperty("user.home") + "/.eclipse/databases/test/timesheet;create=true"); // TODO
 		factory = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME, configOverrides);
 		em = factory.createEntityManager();
 		
 		CriteriaBuilder criteria = em.getCriteriaBuilder();
 		CriteriaQuery<Task> taskQuery = criteria.createQuery(Task.class);
 		Root<Task> taskRoot = taskQuery.from(Task.class);
 		
 		taskQuery.where(criteria.equal(taskRoot.get(Task_.name), StorageService.CHECK_IN));
 		List<Task> tasks = em.createQuery(taskQuery).getResultList(); 
 		if (tasks.isEmpty()) {
 			Task task = new Task(StorageService.CHECK_IN);
 			task.setSyncStatus(true);
 			em.persist(task);
 		}
 		taskQuery.where(criteria.equal(taskRoot.get(Task_.name), StorageService.BREAK));
 		tasks = em.createQuery(taskQuery).getResultList();
 		if (tasks.isEmpty()) {
 			Task task = new Task(StorageService.BREAK);
 			task.setSyncStatus(true);
 			em.persist(task);
 		}
 		taskQuery.where(criteria.equal(taskRoot.get(Task_.name), AllDayTasks.BEGIN_ADT));
 		tasks = em.createQuery(taskQuery).getResultList();
 		if (tasks.isEmpty()) {
 			Task task = new Task(AllDayTasks.BEGIN_ADT);
 			task.setSyncStatus(true);
 			em.persist(task);
 		}
 		
 		submissionSystems = TimesheetApp.getSubmissionSystems();
         logger = Activator.getDefault().getLog();
 		
         if (StringUtils.isEmpty(Activator.getDefault().getPreferenceStore().getString(StorageService.PROPERTY)))
 			StorageSystemSetup.execute();
 		
 		storageService = new ExtensionManager<StorageService>(StorageService.SERVICE_ID)
 				.getService(Activator.getDefault().getPreferenceStore().getString(StorageService.PROPERTY));
 		
 		for (String system : submissionSystems.keySet()) {
 			if (getProjects(system).isEmpty())
 				importTasks(system, storageService.getImportedProjects(system), true);
 		}
 		
 		Calendar cal = GregorianCalendar.getInstance();
 		cal.setTime(new Date());
 		cal.set(cal.get(Calendar.YEAR), Calendar.JANUARY, 1);
 		Date startDate = cal.getTime();
 		Date lastTaskEntryDate = getLastTaskEntryDate();
 		if (lastTaskEntryDate != null) {
 			for (TaskEntry entry : storageService.getTaskEntries(DateUtils.truncate(lastTaskEntryDate, Calendar.DATE),
 					DateUtils.truncate(lastTaskEntryDate, Calendar.DATE)))
 				createOrUpdate(entry);
 			startDate = BusinessDayUtil.getNextBusinessDay(lastTaskEntryDate, false);
 		}
 		cal.setTime(startDate);
 		int startWeek = cal.get(Calendar.WEEK_OF_YEAR);
 		cal.setTime(new Date());
 		int endWeek = cal.get(Calendar.WEEK_OF_YEAR);
 		for (int i = startWeek; i <= endWeek; i++) {
 			logger.log(new Status(IStatus.INFO, Activator.PLUGIN_ID, "import task entries for week " + i));
 			cal.set(Calendar.WEEK_OF_YEAR, i + 1);
 			cal.setFirstDayOfWeek(Calendar.MONDAY);
 			cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
 			Date endDate = DateUtils.truncate(cal.getTime(), Calendar.DATE);
 			for (TaskEntry entry : storageService.getTaskEntries(startDate, endDate)) {
 				entry.setRowNum(entry.getId());
 				entry.setSyncStatus(true);
 				logger.log(new Status(IStatus.INFO, Activator.PLUGIN_ID, "import task entry: " + entry));
 				createTaskEntry(entry);
 			}		
 			cal.set(Calendar.WEEK_OF_YEAR, i + 2);
 			cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
 			startDate = DateUtils.truncate(cal.getTime(), Calendar.DATE);
 		}
 		if (getLastTask() == null) {
 			TaskEntry lastTask = storageService.getLastTask();
 			if (lastTask != null) {
 				lastTask.setRowNum(lastTask.getId());
 				lastTask.setSyncStatus(true);
 				createTaskEntry(lastTask);
 			}
 		}
 		
 		syncEntriesJob = new Job("Synchronizing entries") {
 			@Override
 			protected IStatus run(IProgressMonitor monitor) {
 				if (lastTaskEntry != null) {
 					em.getTransaction().begin();
 					if (storageService.getLastTask() != null)
 						storageService.updateTaskEntry(lastTaskEntry);
 					lastTaskEntry.setSyncStatus(true);
 					em.persist(lastTaskEntry);
 					em.getTransaction().commit();
 				}
 				em.getTransaction().begin();
 				
 				CriteriaBuilder criteria = em.getCriteriaBuilder();
 				CriteriaQuery<TaskEntry> query = criteria.createQuery(TaskEntry.class);
 				Root<TaskEntry> taskEntry = query.from(TaskEntry.class);
 				query.where(criteria.notEqual(taskEntry.get(TaskEntry_.syncStatus), true));
 				List<TaskEntry> entries = em.createQuery(query).getResultList();
 				
 				monitor.beginTask("Synchronize " + entries.size() + " entries", entries.size());
 				int i = 0;
 				for (TaskEntry entry : entries) {
 					if(entry.getRowNum() == null) 
 						entry.setRowNum(storageService.createTaskEntry(entry));
 					else
 						storageService.updateTaskEntry(entry);
 					entry.setSyncStatus(true);
 					em.persist(entry);
 					monitor.worked(++i);
 				}
 				em.getTransaction().commit();
 				monitor.done();
 		        return Status.OK_STATUS;
 			}			
 		};
 		
 		syncTasksJob = new Job("Synchronizing tasks") {
 			@Override
 			protected IStatus run(IProgressMonitor monitor) {
 				em.getTransaction().begin();
 				
 				CriteriaBuilder criteria = em.getCriteriaBuilder();
 				CriteriaQuery<Task> query = criteria.createQuery(Task.class);
 				Root<Task> rootTask = query.from(Task.class);
 				query.where(criteria.notEqual(rootTask.get(Task_.syncStatus), true));
 				List<Task> tasks = em.createQuery(query).getResultList();
 				
 				Map<String, SubmissionProject> submissionProjects = new HashMap<String, SubmissionProject>();
 				for (Task task : tasks) {
 					SubmissionProject submissionProject = submissionProjects.get(task.getProject().getName());
 					if (submissionProject == null)
 						submissionProject = new SubmissionProject(task.getProject().getExternalId(), task.getProject().getName());
 					submissionProject.addTask(new SubmissionTask(task.getExternalId(), task.getName()));
 					submissionProjects.put(submissionProject.getName(), submissionProject);					
 				}
 				if (!submissionProjects.isEmpty()) {
 					storageService.importTasks(submissionSystem, submissionProjects.values());
 					for (Task task : tasks) {
 						task.setSyncStatus(true);
 						task.getProject().setSyncStatus(true);
 						em.persist(task);
 					}
 				}
 				em.getTransaction().commit();
 		        return Status.OK_STATUS;
 			}			
 		};
 	}
     
     public static LocalStorageService getInstance() {
     	if (instance == null)
     		instance = new LocalStorageService();
     	return instance;
     }
 	
 	public List<String> getProjects(String system) {
 		List<String> projects = new ArrayList<String>();
 		for (Project project : getProjectList(system)) projects.add(project.getName());
 		return projects;
 	}
 
 	private List<Project> getProjectList(String system) {
 		CriteriaBuilder criteria = em.getCriteriaBuilder();
 		CriteriaQuery<Project> query = criteria.createQuery(Project.class);
 		Root<Project> project = query.from(Project.class);
 		query.where(criteria.equal(project.get(Project_.system), system));
 		return em.createQuery(query).getResultList();
 	}
 
 	public Collection<SubmissionProject> getImportedProjects(String system) {
 		List<SubmissionProject> projects = new ArrayList<SubmissionProject>();
 		for (Project project : getProjectList(system)) {
 			SubmissionProject submissionProject = new SubmissionProject(project.getExternalId(), project.getName());
 			for (Task task : project.getTasks())
 				submissionProject.addTask(new SubmissionTask(task.getExternalId(), task.getName()));
 			projects.add(submissionProject);
 		}
 		return projects;
 	}
 	
 	public List<String> findTasksBySystemAndProject(String system, String project) {
 		List<Task> taskList = findTasksByProjectAndSystem(project, system);
 		List<String> tasks = new ArrayList<String>();
 		for (Task task : taskList) tasks.add(task.getName());
 		return tasks;
 	}
 
 	public void addPropertyChangeListener(PropertyChangeListener listener) {
 		addListenerObject(listener);
 	}
 
 	public void removePropertyChangeListener(PropertyChangeListener listener) {
 		removeListenerObject(listener);
 	}
 
     protected void firePropertyChangeEvent(final PropertyChangeEvent event) {
 		if (event == null) {
 			throw new NullPointerException();
 		}
 
         synchronized (getListeners()) {
         	for (Object listener : getListeners()) {
         		((PropertyChangeListener) listener).propertyChange(event);
         	}    
         }
     }
     
 	public List<TaskEntry> getTaskEntries(Date startDate, Date endDate) {
 		CriteriaBuilder criteria = em.getCriteriaBuilder();
 		CriteriaQuery<TaskEntry> query = criteria.createQuery(TaskEntry.class);
 		Root<TaskEntry> entry = query.from(TaskEntry.class);
 		Path<Task> task = entry.get(TaskEntry_.task);
 		query.where(criteria.and(criteria.notEqual(task.get(Task_.name), AllDayTasks.BEGIN_ADT),
 				criteria.greaterThanOrEqualTo(entry.get(TaskEntry_.dateTime), new Timestamp(startDate.getTime())),
 				criteria.lessThanOrEqualTo(entry.get(TaskEntry_.dateTime), new Timestamp(endDate.getTime()))));
 		return em.createQuery(query).getResultList();
 	}
 
 	public String[] getUsedCommentsForTask(String task, String project,	String system) {
 		Set<String> comments = new HashSet<String>();
 		CriteriaBuilder criteria = em.getCriteriaBuilder();
 		CriteriaQuery<TaskEntry> query = criteria.createQuery(TaskEntry.class);
 		Root<TaskEntry> taskEntry = query.from(TaskEntry.class);
 		Path<Task> rootTask = taskEntry.get(TaskEntry_.task);
 		Path<Project> rootProject = rootTask.get(Task_.project);
 		query.where(criteria.and(criteria.equal(rootTask.get(Task_.name), task),
 				criteria.equal(rootProject.get(Project_.name), project),
 				criteria.equal(rootProject.get(Project_.system), system)));
 		List<TaskEntry> results = em.createQuery(query).getResultList();
 		for (TaskEntry entry : results)
 			if (entry.getComment() != null)
 				comments.add(entry.getComment());
 		return comments.toArray(new String[comments.size()]);
 	}
 
 	public Long createTaskEntry(TaskEntry task) {
 		em.getTransaction().begin();
 		task.setTask(findTaskByNameProjectAndSystem(task.getTask().getName(),
 				task.getTask().getProject() == null ? null : task.getTask().getProject().getName(),
 						task.getTask().getProject() == null ? null : task.getTask().getProject().getSystem()));
 		em.persist(task);
 		em.getTransaction().commit();
         Calendar cal = new GregorianCalendar();
         cal.setTime(task.getDateTime() == null ? new Date() : task.getDateTime());
 		firePropertyChangeEvent(new PropertyChangeEvent(this, PROPERTY_WEEK, null, cal.get(Calendar.WEEK_OF_YEAR)));
 		return task.getRowNum();
 	}
 
 	public void updateTaskEntry(TaskEntry entry) {
 		em.getTransaction().begin();
 		entry.setSyncStatus(false);
         Calendar cal = new GregorianCalendar();
 		if (entry.getDateTime() != null && !CHECK_IN.equals(entry.getTask().getName()) && !BREAK.equals(entry.getTask().getName())) {
 			entry.setTotal(calculateTotal(entry));
 			cal.setTime(entry.getDateTime());
 		}
 		else cal.setTime(new Date());
 		em.persist(entry);
 		em.getTransaction().commit();
 		firePropertyChangeEvent(new PropertyChangeEvent(this, PROPERTY_WEEK, null, cal.get(Calendar.WEEK_OF_YEAR)));
 	}
 
 	private Float calculateTotal(TaskEntry entry) {
 		try {
 			CriteriaBuilder criteria = em.getCriteriaBuilder();
 			CriteriaQuery<TaskEntry> query = criteria.createQuery(TaskEntry.class);
 			Root<TaskEntry> taskEntry = query.from(TaskEntry.class);
 			query.where(criteria.equal(taskEntry.get(TaskEntry_.rowNum), entry.getRowNum() - 1));
 			TaskEntry previousEntry = (TaskEntry) em.createQuery(query).getSingleResult();
 			Calendar entryCalendar = Calendar.getInstance();
 			Calendar previousEntryCalendar = Calendar.getInstance();
 			entryCalendar.setTime(entry.getDateTime());
 			previousEntryCalendar.setTime(previousEntry.getDateTime());
 			Calendar totalCalendar = Calendar.getInstance();
 			totalCalendar.setTime(new Date(entryCalendar.getTimeInMillis() - previousEntryCalendar.getTimeInMillis()));
 			return totalCalendar.get(Calendar.HOUR) - 1 + totalCalendar.get(Calendar.MINUTE) / 60.0f;
 		} catch (Exception e) {}
 		return 0.0f;
 	}
 
     private void createOrUpdate(TaskEntry entry) {
 		CriteriaBuilder criteria = em.getCriteriaBuilder();
 		CriteriaQuery<TaskEntry> query = criteria.createQuery(TaskEntry.class);
 		Root<TaskEntry> taskEntry = query.from(TaskEntry.class);
 		query.where(criteria.equal(taskEntry.get(TaskEntry_.rowNum), entry.getId()));
 		try {
 			TaskEntry availableEntry = (TaskEntry) em.createQuery(query).getSingleResult();
 			if (availableEntry != null) {
 				em.getTransaction().begin();
 				availableEntry.setDateTime(entry.getDateTime());
 				if (!StorageService.CHECK_IN.equals(entry.getTask().getName())) {
 					availableEntry.setTask(findTaskByNameProjectAndSystem(entry.getTask().getName(),
 							entry.getTask().getProject() == null ? null : entry.getTask().getProject().getName(),
 									entry.getTask().getProject() == null ? null : entry.getTask().getProject().getSystem()));
 					availableEntry.setTotal(calculateTotal(availableEntry));
 				}
 				availableEntry.setComment(entry.getComment());
 				em.persist(availableEntry);
 				em.getTransaction().commit();
 			}
 			else
 				createTaskEntry(entry);
 		} catch (Exception e) {
 			createTaskEntry(entry);
 		}
 	}
 
 	public TaskEntry getLastTask() {
 		CriteriaBuilder criteria = em.getCriteriaBuilder();
 		CriteriaQuery<TaskEntry> query = criteria.createQuery(TaskEntry.class);
 		Root<TaskEntry> entry = query.from(TaskEntry.class);
 		query.orderBy(criteria.desc(entry.get(TaskEntry_.id)));
 		List<TaskEntry> taskEntries = em.createQuery(query).getResultList();
 		if (taskEntries.isEmpty()) return null;
 		return taskEntries.iterator().next();
 	}
 
 	public Date getLastTaskEntryDate() {
 		CriteriaBuilder criteria = em.getCriteriaBuilder();
 		CriteriaQuery<TaskEntry> query = criteria.createQuery(TaskEntry.class);
 		Root<TaskEntry> entry = query.from(TaskEntry.class);
 		Path<Task> task = entry.get(TaskEntry_.task);
 		query.where(criteria.and(criteria.notEqual(task.get(Task_.name), AllDayTasks.BEGIN_ADT),
 				entry.get(TaskEntry_.dateTime).isNotNull()));
 		query.orderBy(criteria.desc(entry.get(TaskEntry_.dateTime)));
 		List<TaskEntry> taskEntries = em.createQuery(query).getResultList();
 		if (taskEntries.isEmpty()) return null;
 		return taskEntries.iterator().next().getDateTime();
 	}
 
 	public void handleDayChange() {
 		storageService.handleDayChange();
 	}
 
 	public void handleWeekChange() {
 		storageService.handleWeekChange();
 	}
 
 	public void handleYearChange(int lastWeek) {
 		storageService.handleYearChange(lastWeek);
 	}
 
 	public void importTasks(String submissionSystem, Collection<SubmissionProject> projects) {
 		importTasks(submissionSystem, projects, false);
 	}
 	
 	private void importTasks(String submissionSystem, Collection<SubmissionProject> projects, boolean isSynchronized) {
 		for (SubmissionProject submissionProject : projects) {
 			for (SubmissionTask submissionTask : submissionProject.getTasks()) {
 				Task foundTask = findTaskByNameProjectAndSystem(submissionTask.getName(), submissionProject.getName(), submissionSystem);
 				if (foundTask == null) {
 					em.getTransaction().begin();
 					Project foundProject = findProjectByNameAndSystem(submissionProject.getName(), submissionSystem);
 					if (foundProject == null) {
 						foundProject = new Project(submissionProject.getName(), submissionSystem);
 						foundProject.setExternalId(submissionProject.getId());
 						em.persist(foundProject);
 						if (isSynchronized) foundProject.setSyncStatus(true);
 					}
 					Task task = new Task(submissionTask.getName(), foundProject);
 					task.setExternalId(submissionTask.getId());
 					if (isSynchronized) task.setSyncStatus(true);
 					
 					logger.log(new Status(IStatus.INFO, Activator.PLUGIN_ID, "Import task: " + submissionTask.getName() + " id=" + submissionTask.getId()
 		            		+ " (" + submissionProject.getName() + " id=" + submissionProject.getId() + ") "));
 					
 					em.persist(task);
 					em.getTransaction().commit();
 				}
 			}
 		}
 		if (!isSynchronized) {
 			this.submissionSystem = submissionSystem;
 			syncTasksJob.schedule();
 		}
 	}
 
 	public Set<String> submitEntries(Date startDate, Date endDate) {
         Set<String> systems = new HashSet<String>();
         em.getTransaction().begin();
 		CriteriaBuilder criteria = em.getCriteriaBuilder();
 		CriteriaQuery<TaskEntry> query = criteria.createQuery(TaskEntry.class);
 		Root<TaskEntry> entry = query.from(TaskEntry.class);
 		Path<Task> task = entry.get(TaskEntry_.task);
 		query.where(criteria.and(criteria.notEqual(entry.get(TaskEntry_.status), true),
 				criteria.notEqual(task.get(Task_.name), AllDayTasks.BEGIN_ADT),
				//criteria.notEqual(task.get(Task_.name), StorageService.CHECK_IN),
				//criteria.notEqual(task.get(Task_.name), StorageService.BREAK),
 				entry.get(TaskEntry_.dateTime).isNotNull(),
 				criteria.greaterThanOrEqualTo(entry.get(TaskEntry_.dateTime), new Timestamp(startDate.getTime())),
 				criteria.lessThanOrEqualTo(entry.get(TaskEntry_.dateTime), new Timestamp(endDate.getTime()))));
 		query.orderBy(criteria.asc(entry.get(TaskEntry_.dateTime)));
 		
 		List<TaskEntry> entries = em.createQuery(query).getResultList();
         if (entries.isEmpty()) return systems;
         
 		Date lastDate = DateUtils.truncate(entries.iterator().next().getDateTime(), Calendar.DATE);
         DailySubmissionEntry submissionEntry = new DailySubmissionEntry(lastDate);
         
         for (TaskEntry taskEntry : entries) {
         	Date date = DateUtils.truncate(taskEntry.getDateTime(), Calendar.DATE);
             if (!date.equals(lastDate)) { // another day
             	submissionEntry.submitEntries();
             	submissionEntry = new DailySubmissionEntry(date);
                 lastDate = date;
             }
 			String system = taskEntry.getTask().getProject() == null ? null : taskEntry.getTask().getProject().getSystem();
             if (submissionSystems.containsKey(system)) {
 				systems.add(system);
 				SubmissionEntry submissionTask = new SubmissionEntry(taskEntry.getTask().getProject().getExternalId(), taskEntry.getTask().getExternalId(),
 						taskEntry.getTask().getName(), taskEntry.getTask().getProject().getName(), system);
 				submissionEntry.addSubmissionEntry(submissionTask, taskEntry.getTotal());
 			}
             taskEntry.setStatus(true);
             taskEntry.setSyncStatus(false);
             em.persist(taskEntry);
 		}
         em.getTransaction().commit();
 		return systems;
 	}
 
 	public void submitFillTask(Date date) {
 		IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();
 		if (!StringUtils.isEmpty(preferenceStore.getString(TimesheetApp.DAILY_TASK))) {
 			Task task = TimesheetApp.createTask(TimesheetApp.DAILY_TASK);
 			if (submissionSystems.containsKey(task.getProject().getSystem())) {
 				SubmissionEntry submissionTask = getSubmissionTask(task.getName(), task.getProject().getName(), task.getProject().getSystem());
 				if (submissionTask != null)
 					new ExtensionManager<SubmissionService>(SubmissionService.SERVICE_ID).getService(submissionSystems.get(task.getProject().getSystem()))
 							.submit(date, submissionTask, Double.parseDouble(preferenceStore.getString(TimesheetApp.DAILY_TASK_TOTAL)));
 			}
 		}
 	}
 	
 	private SubmissionEntry getSubmissionTask(String task, String project, String system) {
 		try {
 			Task defaultTask = findTaskByNameProjectAndSystem(task, project, system);
 			return new SubmissionEntry(defaultTask.getProject().getExternalId(), defaultTask.getExternalId(), defaultTask.getName(), defaultTask.getProject().getName(), system);
 		} catch (Exception e) {
 			return null;
 		}
 	}
 	public void openUrl(String openBrowser) {
 		storageService.openUrl(openBrowser);
 	}
 	
 	public Project findProjectByNameAndSystem(String name, String system) {
 		try {	
 			CriteriaBuilder criteria = em.getCriteriaBuilder();
 			CriteriaQuery<Project> query = criteria.createQuery(Project.class);
 			Root<Project> project = query.from(Project.class);
 			query.where(criteria.and(criteria.equal(project.get(Project_.name), name),
 					criteria.equal(project.get(Project_.system), system)));
 			return (Project) em.createQuery(query).getSingleResult();
 		} catch (Exception e) {
 			return null;
 		}
 	}
 	
 	public List<Task> findTasksByProjectAndSystem(String project, String system) {
 		CriteriaBuilder criteria = em.getCriteriaBuilder();
 		CriteriaQuery<Task> query = criteria.createQuery(Task.class);
 		Root<Task> task = query.from(Task.class);
 		Path<Project> proj = task.get(Task_.project);
 		query.where(criteria.and(criteria.equal(proj.get(Project_.name), project),
 				criteria.equal(proj.get(Project_.system), system)));
 		return em.createQuery(query).getResultList();
 	}
 	
 	public Task findTaskByNameProjectAndSystem(String name, String project, String system) {
 		CriteriaBuilder criteria = em.getCriteriaBuilder();
 		CriteriaQuery<Task> query = criteria.createQuery(Task.class);
 		Root<Task> task = query.from(Task.class);
 		Predicate p = criteria.equal(task.get(Task_.name), name);
 		if (project == null)
 			query.where(p);
 		else {
 			Path<Project> proj = task.get(Task_.project);
 			query.where(criteria.and(p, criteria.equal(proj.get(Project_.name), project),
 					criteria.equal(proj.get(Project_.system), system)));
 		}
 		try {
 			return em.createQuery(query).getSingleResult();
 		} catch (Exception e) {
 			return null;
 		}
 	}
 
 	public void synchronize(TaskEntry lastTaskEntry) {
 		this.lastTaskEntry = lastTaskEntry;
 		syncEntriesJob.schedule();
 		storageService.openUrl(StorageService.OPEN_BROWSER_CHANGE_TASK);
 	}
 
 	@Override
 	public void reload() {
 		storageService.reload();		
 	}
 }
