 package ctrl;
 
 import gui.GuiInterface;
 import gui.GuiMain;
 import gui.ToolBar;
 import gui.custom.StatusTip;
 import gui.dialogs.EditSurvey;
 import gui.dialogs.ViewSurvey;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 
 import javax.swing.ImageIcon;
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 
 import lib.ProcessChildren;
 import lib.ProcessChildrenDivorcedFemales;
 import lib.ProcessChildrenDivorcedMales;
 import lib.ProcessCreditScore;
 import lib.ProcessCustodyChildSupport;
 import lib.ProcessJobs;
 import lib.ProcessMarried;
 import obj.Group;
 import obj.Job;
 import obj.Survey;
 import dao.GroupsDAO;
 import dao.JobsDAO;
 import dao.SurveysDAO;
 
 /**
  * Hosts constants and methods that are available to all other controllers.
  */
 public class Controller implements GuiInterface {
 
 	/** An instance of the Controller. */
 	private static Controller ourInstance = null;
 
 	/** The groups DAO. */
 	protected static GroupsDAO groupsDAO = new GroupsDAO();
 
 	/** The jobs DAO. */
 	protected static JobsDAO jobsDAO = new JobsDAO();
 
 	/** The surveys DAO. */
 	protected static SurveysDAO surveysDAO = new SurveysDAO();
 
 	/** The jobs map. Used to populate jobs and job categories */
 	private Map<String, List<Job>> mapJobs;
 
 	/** The current group. */
 	private Group group;
 
 	/** The groups list. */
 	private List<Group> lstGroups = new ArrayList<Group>();
 
 	/** The surveys list. */
 	private List<Survey> lstSurveys = new ArrayList<Survey>();
 
 	/** The jobs list. */
 	private List<Job> lstJobs = new ArrayList<Job>();
 
 	/** The deleted jobs list. */
 	private List<Job> delJobs = new ArrayList<Job>();
 
 	/** The deleted surveys list. */
 	private List<Survey> delSurveys = new ArrayList<Survey>();
 
 	/** The updated surveys list. */
 	private List<Survey> updSurveys = new ArrayList<Survey>();
 
 	/** The new jobs list. */
 	private List<Job> newJobs = new ArrayList<Job>();
 
 	/** The new surveys list. */
 	private List<Survey> newSurveys = new ArrayList<Survey>();
 
 	/** The processed boolean to tell if it should be processed or not. */
 	private boolean processed;
 
 	/**
 	 * Instantiates a new controller.
 	 */
 	public Controller() {
 		setGroupsList();
 		setJobsList();
 	}
 
 	// TODO: Setup the Undo Feature
 	/**
 	 * Gets the single instance of Controller.
 	 * 
 	 * @return single instance of Controller
 	 */
 	public static Controller getInstance() {
 
 		// If we do not yet have an instance of this controller
 		// create a new one. Otherwise, return the controller.
 		if (ourInstance == null) {
 			ourInstance = new Controller();
 		}
 		return ourInstance;
 	}
 
 	/**
 	 * Alter a survey in the DAO.
 	 * 
 	 * @param survey
 	 *            the survey being altered
 	 * @return whether or not altering the survey was successful
 	 */
 	private int alterSurvey(Survey survey) {
 		int result = 0;
 		result = surveysDAO.update(survey);
 		return result;
 	}
 
 	/**
 	 * Delete a survey in the DAO.
 	 * 
 	 * @param survey
 	 *            the survey being deleted
 	 * @return whether or not deleted the survey was successful
 	 */
 	private int deleteSurvey(Survey survey) {
 		int result = 0;
 		surveysDAO.delete(survey);
 		return result;
 	}
 
 	/**
 	 * Destroy the delSurveys list.
 	 */
 	private void destroyDelSurveys() {
 		delSurveys.clear();
 		ToolBar.setSaveEnabled(false);
 		isGroupChanged();
 	}
 
 	/**
 	 * Destroy the newSurveys list.
 	 */
 	private void destroyNewSurveys() {
 		newSurveys.clear();
 		ToolBar.setSaveEnabled(false);
 		isGroupChanged();
 	}
 
 	/**
 	 * Destroy updated surveys.
 	 */
 	private void destroyUpdSurveys() {
 		updSurveys.clear();
 		ToolBar.setSaveEnabled(false);
 		isGroupChanged();
 	}
 
 	/**
 	 * Delete a group in the DAO.
 	 * 
 	 * @param group
 	 *            the group being deleted
 	 * @return whether or not deleted the survey was successful
 	 */
 	// private void deleteGroup(Group group) {
 	// jobsDAO.delete(group.getID());
 	// lstJobs.remove(group);
 	// Controller.getInstance().refreshScreen();
 	// }
 
 	/**
 	 * Adds a Group in the DAO.
 	 * 
 	 * @param groupName
 	 *            : The group's name.
 	 * @return Returns an integer:<br>
 	 *         0: Failure<br>
 	 *         1: Success
 	 */
 	private int insertGroup(String groupName) {
 
 		int success = groupsDAO.insert(groupName);
 		lstGroups.add(groupsDAO.search("name", "'" + groupName + "'").get(0));
 
 		return success;
 
 	}
 
 	/**
 	 * Adds a new survey to the Group in the DAO.
 	 * 
 	 * @param survey
 	 *            the survey
 	 * @return 0: Failure<br>
 	 *         1: Success
 	 */
 	private int insertSurvey(Survey survey) {
 		int result = 0;
 		surveysDAO.insert(survey);
 		return result;
 	}
 
 	/**
 	 * Delete a job in the DAO..
 	 * 
 	 */
 	// private void deleteJob(Job job) {
 	// jobsDAO.delete(job.getID());
 	// lstJobs.remove(job);
 	// Controller.getInstance().refreshScreen();
 	// }
 
 	/**
 	 * Sets the groups list.
 	 */
 	private void setGroupsList() {
 		lstGroups = groupsDAO.search(null, null);
 	}
 
 	/**
 	 * Sets the jobs list.
 	 */
 	private void setJobsList() {
 		lstJobs = jobsDAO.find(null, null);
 	}
 
 	/**
 	 * Sets the surveys list.
 	 * 
 	 * @param group
 	 *            : the group containing the surveys
 	 */
 	private void setSurveysList(Group group) {
 		if (group != null)
 			lstSurveys = surveysDAO.search("groupID",
 					String.valueOf(group.getID()), null);
 	}
 
 	/**
 	 * Adds the group in memory.
 	 * 
 	 * @param groupName
 	 *            the name of the group being added
 	 * @return Returns an integer:<br>
 	 *         0: Failure<br>
 	 *         1: Success
 	 */
 	public int addGroup(String groupName) {
 		boolean exists = false;
 
 		for (Group group : lstGroups) {
 			if (group.getName().equalsIgnoreCase(groupName)) {
 				exists = true;
 			}
 		}
 		if (!exists) {
 			insertGroup(groupName);
 			lstGroups.add(group);
 			return 1;
 		} else {
 			return 0;
 		}
 	}
 
 	/**
 	 * Adds the job in memory.
 	 * 
 	 * @param job
 	 *            the job
 	 */
 	public void addJob(Job job) {
 		lstJobs.add(job);
 		newJobs.add(job);
 		gui.ToolBar.setSaveEnabled(true);
 	}
 
 	/**
 	 * Adds the survey in memory.
 	 * 
 	 * @param survey
 	 *            the survey
 	 */
 	public void addSurvey(Survey survey) {
 		// lstSurveys.add(survey);
 		newSurveys.add(survey);
 		gui.ToolBar.setSaveEnabled(true);
 		refreshScreen();
 
 	}
 
 	/**
 	 * Check tables.
 	 */
 	public void checkTables() {
 		if (!groupsDAO.checkTable())
 			groupsDAO.createTable();
 		if (!jobsDAO.checkTable())
 			jobsDAO.createTable();
 		if (!surveysDAO.checkTable())
 			surveysDAO.createTable();
 
 		mapJobs = jobsDAO.findJobsByCategory();
 	}
 
 	/**
 	 * Close the application and removes it from memory.
 	 */
 	public void closeApplication() {
 
 		int response = JOptionPane
 				.showConfirmDialog(
 						null,
 						new String(
 								"Are you sure you want to quit?\nAny work that is not saved will be lost!"),
 						"Exit Application", JOptionPane.YES_NO_OPTION, 0,
 						new ImageIcon(LG_EXIT));
 
 		if (response == JOptionPane.YES_OPTION)
 			System.exit(0);
 	}
 
 	/**
 	 * Gets surveys that have been deleted, but not yet saved
 	 * 
 	 * @return a List of surveys
 	 */
 	public List<Survey> getDelSurveys() {
 		if (delSurveys.size() > 0)
 			return delSurveys;
 		List<Survey> lstBlank = new ArrayList<Survey>();
 		return lstBlank;
 	}
 
 	/**
 	 * Gets Job categories.
 	 * 
 	 * @return a List of Strings containing job categories
 	 */
 	public List<String> getJobCategories() {
 
 		List<String> lstCategories = new ArrayList<String>();
 		lstCategories.addAll(mapJobs.keySet());
 
 		return lstCategories;
 	}
 
 	/**
 	 * Gets the frame of the application currently in memory.
 	 * 
 	 * @return the frame
 	 */
 	public JFrame getFrame() {
 		return GuiMain.getInstance().getParent();
 	}
 
 	/**
 	 * Gets the group in memory.
 	 * 
 	 * @return the current Group object.
 	 */
 	public Group getGroup() {
 		return group;
 	}
 
 	/**
 	 * Goes through all of the groups currently in memory and returns a Group
 	 * based on search criteria
 	 * 
 	 * @param search
 	 *            : The property of the object to look for. <br />
 	 * @param criteria
 	 *            : The term to look for in the column.<br />
 	 *            <ul>
 	 *            <strong>MUST be a String!</strong> -
 	 *            <i>Integer.toString(int))</i>
 	 *            <li>ID</li>
 	 *            <li>Name</li>
 	 *            </ul>
 	 * @return Returns a Group object.
 	 */
 	public Group getGroup(String search, String criteria) {
 		Group group = new Group();
 
 		if (!this.lstGroups.isEmpty()) {
 			try {
 				for (int i = 0; i < lstGroups.size(); i++) {
 					group = lstGroups.get(i);
 					switch (search) {
 					case "ID":
 					case "id":
 						if (group.getID() == Integer.parseInt(criteria)) {
 							return group;
 						}
 						break;
 					case "Name":
 					case "name":
 						if (group.getName().equalsIgnoreCase(criteria)) {
 							return group;
 						}
 						break;
 					}
 
 				}
 			} catch (NullPointerException npe) {
 				return group;
 			}
 		}
 
 		return group;
 	}
 
 	/**
 	 * Gets all of the groups currently in memory.
 	 * 
 	 * @return all of the groups
 	 */
 	public List<Group> getGroups() {
 		try {
 			lstGroups.size();
 		} catch (NullPointerException npe) {
 			// If it doesn't exist, create it
 			setGroupsList();
 		}
 		return lstGroups;
 	}
 
 	/**
 	 * Goes through the list of groups currently in memory and returns a list of
 	 * groups based on search criteria.
 	 * 
 	 * @param search
 	 *            : The property of the object to look for. <br />
 	 *            <ul>
 	 *            <strong>MUST be a String!</strong> -
 	 *            <i>Integer.toString(int))</i>
 	 *            <li>ID</li>
 	 *            <li>Name</li>
 	 *            </ul>
 	 * @param criteria
 	 *            : The term to look for in the column.
 	 * @return Returns a list of Group objects.
 	 */
 	public List<Group> getGroups(String search, String criteria) {
 		List<Group> lstSearchedGroups = new ArrayList<Group>();
 
 		if (!this.lstGroups.isEmpty()) {
 			try {
 				for (int i = 0; i < lstGroups.size(); i++) {
 					switch (search) {
 					case "ID":
 						if (lstGroups.get(i).getID() == Integer
 								.parseInt(criteria)) {
 							lstSearchedGroups.add(lstGroups.get(i));
 						}
 						break;
 					case "Name":
 						if (lstGroups.get(i).getName()
 								.equalsIgnoreCase(criteria)) {
 							lstSearchedGroups.add(lstGroups.get(i));
 						}
 						break;
 					}
 				}
 				if (lstSearchedGroups.size() > 0)
 					return lstSearchedGroups;
 			} catch (NullPointerException npe) {
 				return lstGroups;
 			}
 		}
 
 		// Should not reach this EVER
 		return null;
 	}
 
 	/**
 	 * Get a job from memory.
 	 * 
 	 * @param search
 	 *            : The property of the object to look for. <br />
 	 *            <ul>
 	 *            <strong>MUST be a String!</strong> -
 	 *            <i>Integer.toString(int))</i>
 	 *            <li>ID</li>
 	 *            <li>Name</li>
 	 *            </ul>
 	 * @param criteria
 	 *            : The term to look for in the table.
 	 * @return a Job object.
 	 */
 	public Job getJob(String search, String criteria) {
 		Job job = new Job();
 
 		if (!this.lstJobs.isEmpty()) {
 			try {
 				for (int i = 0; i < lstJobs.size(); i++) {
 					job = lstJobs.get(i);
 					switch (search) {
 					case "ID":
 					case "id":
 						if (job.getID() == Integer.parseInt(criteria)) {
 							return job;
 						}
 						break;
 					case "Name":
 					case "name":
 						if (job.getName().equalsIgnoreCase(criteria)) {
 							return job;
 						}
 						break;
 					}
 
 				}
 			} catch (NullPointerException npe) {
 				return job;
 			}
 		}
 
 		return job;
 	}
 
 	/**
 	 * Gets the jobs.
 	 * 
 	 * @return the jobs
 	 */
 	public List<Job> getJobs() {
 		return lstJobs;
 	}
 
 	/**
 	 * Get a list of jobs.
 	 * 
 	 * @param search
 	 *            : The property of the object to look for. <br />
 	 * @param criteria
 	 *            : The value of the property being searched.<br />
 	 *            <ul>
 	 *            <strong>MUST be a String!</strong> -
 	 *            <i>Integer.toString(int))</i>
 	 *            <li>ID</li>
 	 *            <li>Name</li>
 	 *            <li>GPA</li>
 	 *            <li>Category</li>
 	 *            <li>Industry</li>
 	 *            <li>Type</li>
 	 *            </ul>
 	 * @return a list of Job objects.
 	 */
 	public List<Job> getJobs(String search, String criteria) {
 
 		List<Job> lstRJobs = new ArrayList<Job>();
 
 		if (!lstJobs.isEmpty()) {
 			try {
 				for (int i = 0; i < lstJobs.size(); i++) {
 					Job job = lstJobs.get(i);
 					switch (search) {
 					case "ID":
 					case "id":
 						if (job.getID() == Integer.parseInt(criteria)) {
 							lstRJobs.add(job);
 						}
 						break;
 					case "Name":
 					case "name":
 						if (job.getName().equalsIgnoreCase(criteria)) {
 							lstRJobs.add(job);
 						}
 						break;
 					case "GPA":
 					case "gpa":
 						if (job.getGPA() == Integer.parseInt(criteria)) {
 							lstRJobs.add(job);
 						}
 						break;
 					case "Category":
 					case "category":
 						if (job.getCategory().equalsIgnoreCase(criteria)) {
 							lstRJobs.add(job);
 						}
 						break;
 					case "Industry":
 					case "industry":
 						if (job.getIndustry().equalsIgnoreCase(criteria)) {
 							lstRJobs.add(job);
 						}
 						break;
 					case "Type":
 					case "type":
 						if (job.getType().equalsIgnoreCase(criteria)) {
 							lstRJobs.add(job);
 						}
 						break;
 					}
 				}
 				if (lstRJobs.size() > 0)
 					return lstRJobs;
 			} catch (NullPointerException npe) {
 				return lstJobs;
 			}
 		}
 		return lstJobs;
 	}
 
 	/**
 	 * Gets the jobs for each category.
 	 * 
 	 * @param category
 	 *            : The job category to search
 	 * @return a List of Strings (Job Names)
 	 */
 	public List<String> getJobsForCategory(String category) {
 		List<Job> lstJobs = mapJobs.get(category);
 		List<String> lstJobNames = new ArrayList<String>();
 
 		for (Job job : lstJobs) {
 			lstJobNames.add(job.getName());
 		}
 
 		return lstJobNames;
 	}
 
 	/**
 	 * Gets surveys that have been added, but not yet saved
 	 * 
 	 * @return a List of surveys
 	 */
 	public List<Survey> getNewSurveys() {
 		if (newSurveys.size() > 0)
 			return newSurveys;
 		List<Survey> lstBlank = new ArrayList<Survey>();
 		return lstBlank;
 	}
 
 	/**
 	 * Get a survey.
 	 * 
 	 * @param column
 	 *            : The column in the table to look for. <br />
 	 * @param search
 	 *            : The term to look for in the table.
 	 *            <ul>
 	 *            <strong>MUST be a String!</strong> -
 	 *            <i>Integer.toString(int))</i>
 	 *            <li>ID</li>
 	 *            <li>FName</li>
 	 *            <li>LName</li>
 	 *            </ul>
 	 * @return a Survey object.
 	 */
 	public Survey getSurvey(String column, String search) {
 		Survey survey = new Survey();
 
 		if (!this.lstSurveys.isEmpty()) {
 			try {
 				for (int i = 0; i < lstSurveys.size(); i++) {
 					survey = lstSurveys.get(i);
 					switch (column) {
 					case "ID":
 					case "id":
 						if (survey.getID() == Integer.parseInt(search)) {
 							return survey;
 						}
 						break;
 					case "LName":
 					case "lname":
 						if (survey.getLName().equalsIgnoreCase(search)) {
 							return survey;
 						}
 						break;
 					case "FName":
 					case "fname":
 						if (survey.getLName().equalsIgnoreCase(search)) {
 							return survey;
 						}
 						break;
 					}
 
 				}
 			} catch (NullPointerException npe) {
 				return survey;
 			}
 		}
 
 		return survey;
 	}
 
 	/**
 	 * Gets the surveys.
 	 * 
 	 * @return a List of Survey objects
 	 */
 	public List<Survey> getSurveys() {
 		if (lstSurveys.size() > 0)
 			return lstSurveys;
 		List<Survey> lstBlank = new ArrayList<Survey>();
 		return lstBlank;
 	}
 
 	/**
 	 * Get a list of surveys.
 	 * 
 	 * @param column
 	 *            : The column in the table to look for. <br />
 	 * 
 	 * @param search
 	 *            : The term to look for in the table.
 	 *            <ul>
 	 *            <li>ID (i.e. <i>Integer.toString(parameter))</i></li>
 	 *            <li>FName</li>
 	 *            <li>LName</li>
 	 *            <li>GroupID (i.e. <i>Integer.toString(parameter))</i></li>
 	 *            </ul>
 	 * @return a List of Survey objects.
 	 */
 	public List<Survey> getSurveys(String column, String search) {
 
 		List<Survey> lstRSurveys = new ArrayList<Survey>();
 
 		if (!lstSurveys.isEmpty()) {
 			try {
 				for (int i = 0; i < lstSurveys.size(); i++) {
 					Survey survey = lstSurveys.get(i);
 					switch (column) {
 					case "ID":
 					case "id":
 						if (survey.getID() == Integer.parseInt(search)) {
 							lstRSurveys.add(survey);
 						}
 						break;
 					case "LName":
 					case "lname":
 						if (survey.getLName().equalsIgnoreCase(search)) {
 							lstRSurveys.add(survey);
 						}
 						break;
 					case "GroupID":
 					case "groupid":
 						if (survey.getGroupID() == Integer.parseInt(search)) {
 							lstRSurveys.add(survey);
 						}
 						break;
 					case "FName":
 					case "fname":
 						if (survey.getLName().equalsIgnoreCase(search)) {
 							lstRSurveys.add(survey);
 						}
 						break;
 					}
 				}
 				if (lstRSurveys.size() > 0)
 					return lstRSurveys;
 			} catch (NullPointerException npe) {
 				return lstSurveys;
 			}
 		}
 		return lstSurveys;
 	}
 
 	/**
 	 * Gets the teacher names.
 	 * 
 	 * @return a List of Strings (Teacher Names)
 	 */
 	public List<String> getTeacherNames() {
 
 		// Get All Surveys
 		List<Survey> lstSurveys = getSurveys();
 		List<String> lstTeachers = new ArrayList<String>();
 
 		// Return individual teacher names
 		for (Survey survey : lstSurveys) {
 			if (lstTeachers.size() > 0) {
 				// Add it to the list if it's not already in it.
 				if (!lstTeachers.contains(survey.getTeacher())) {
 					lstTeachers.add(survey.getTeacher());
 				}
 			} else {
 
 				// First entry
 				lstTeachers.add(survey.getTeacher());
 			}
 		}
 
 		if (lstTeachers.size() > 0) {
 			return lstTeachers;
 		} else {
 			List<String> emptyList = new ArrayList<String>();
 			emptyList.add("");
 			return emptyList;
 		}
 
 	}
 
 	/**
 	 * Checks if group has been modified.
 	 * 
 	 * @return true, if is group changed
 	 */
 	public boolean isGroupChanged() {
 		if (newSurveys.size() > 0)
 			return true;
 		if (delSurveys.size() > 0)
 			return true;
 		if (updSurveys.size() > 0)
 			return true;
 		if (processed)
 			return true;
 
 		return false;
 	}
 
 	/**
 	 * Checks if job has been modified.
 	 * 
 	 * @return true, if is job changed
 	 */
 	public boolean isJobChanged() {
 		if (newJobs.size() > 0)
 			return true;
 		if (delJobs.size() > 0)
 			return true;
 
 		return false;
 	}
 
 	/**
 	 * Open edit survey.
 	 * 
 	 * @param survey
 	 *            : the survey being edited
 	 */
 	public void openEditSurvey(Survey survey) {
 		new EditSurvey(survey);
 	}
 
 	/**
 	 * Open view survey.
 	 * 
 	 * @param survey
 	 *            : the survey being opened
 	 */
 	public void openViewSurvey(Survey survey) {
 		new ViewSurvey(survey);
 	}
 
 	/**
 	 * Process group.
 	 */
 	public void processGroup() {
 		gui.ToolBar.setSaveEnabled(true);
 		processed = true;
 	}
 
 	/**
 	 * Refresh screen.
 	 */
 	public void refreshScreen() {
 		// lstSurveys.clear();
 		// setSurveysList(group);
 		GuiMain.getInstance().refresh();
 	}
 
 	/**
 	 * Remove group.
 	 * 
 	 * @param group
 	 *            the group
 	 */
 	public void removeGroup(Group group) {
 		lstJobs.remove(group);
 		Controller.getInstance().refreshScreen();
 	}
 
 	/**
 	 * Remove job.
 	 * 
 	 * @param job
 	 *            the job
 	 */
 	public void removeJob(Job job) {
 		lstJobs.remove(job);
 		Controller.getInstance().refreshScreen();
 	}
 
 	/**
 	 * Removes the survey.
 	 * 
 	 * @param survey
 	 *            the survey
 	 */
 	public void removeSurvey(Survey survey) {
 		if (newSurveys.contains(survey))
 			newSurveys.remove(survey);
 
 		delSurveys.add(survey);
 		lstSurveys.remove(survey);
 		gui.ToolBar.setSaveEnabled(true);
 		refreshScreen();
 	}
 
 	/**
 	 * Save the group.
 	 * 
 	 * @param save
 	 *            : Whether to save the group or not
 	 */
 	public void saveGroup(boolean save) {
 		String message = "";
 
 		if (save) {
 			if (newSurveys.size() > 0) {
 				for (int i = 0; i < newSurveys.size(); i++) {
 					insertSurvey(newSurveys.get(i));
 				}
 				message += "\n Added " + newSurveys.size() + " survey(s) to "
 						+ group.getName() + ". ";
 			}
 			if (delSurveys.size() > 0) {
 				for (int i = 0; i < delSurveys.size(); i++) {
 					deleteSurvey(delSurveys.get(i));
 				}
 				message += "\n Removed " + delSurveys.size()
 						+ " survey(s) from " + group.getName() + ". ";
 			}
 			if (updSurveys.size() > 0) {
 				for (int i = 0; i < updSurveys.size(); i++) {
 					alterSurvey(updSurveys.get(i));
 				}
 				message += "\n Updated " + updSurveys.size() + " survey(s) in "
 						+ group.getName() + ". ";
 			}
 			if (processed) {
 				lstSurveys = new ProcessMarried().doProcess();
 				lstSurveys = new ProcessCreditScore().doProcess();
 				lstSurveys = new ProcessJobs().doProcess();
 				lstSurveys = new ProcessChildren().doProcess();
 				lstSurveys = new ProcessChildrenDivorcedFemales().doProcess();
 				lstSurveys = new ProcessChildrenDivorcedMales().doProcess();
 				lstSurveys = new ProcessCustodyChildSupport().doProcess();
 
 				for (int i = 0; i < lstSurveys.size(); i++) {
 					alterSurvey(lstSurveys.get(i));
 				}
 				message += "\n Processed " + lstSurveys.size()
 						+ " survey(s) in " + group.getName() + ". ";
 			}
 
 			if (newSurveys.size() > 0 || delSurveys.size() > 0
 					|| updSurveys.size() > 0 || processed == true)
 				new StatusTip(message, LG_SUCCESS);
 
 		}
		refreshScreen();
 		destroyDelSurveys();
 		destroyNewSurveys();
 		destroyUpdSurveys();
 		processed = false;
 
 	}
 
 	/**
 	 * Save jobs.
 	 */
 	public void saveJobs() {
 		// TODO: Save new jobs
 	}
 
 	/**
 	 * Sets the current Group object.
 	 * 
 	 * @param group
 	 *            the new group
 	 */
 	public void setGroup(Group group) {
 
 		if (isGroupChanged()) {
 			int response = JOptionPane.showConfirmDialog(null, new String(
 					"Would you like to save your changes to the group: "
 							+ this.group.getName() + "?"), "Unsaved Changes",
 					JOptionPane.YES_NO_OPTION, 0, new ImageIcon(LG_CAUTION));
 
 			if (response == JOptionPane.YES_OPTION) {
 				saveGroup(true);
 				this.group = group;
 			}
 		} else {
 			this.group = group;
 		}
 
 		destroyNewSurveys();
 		destroyDelSurveys();
 		destroyUpdSurveys();
 		setSurveysList(group);
 	}
 
 	/**
 	 * Updates current Group<br>
 	 * .
 	 * 
 	 * @param uGroup
 	 *            : If you want to update a specific group, pass it in.
 	 *            Otherwise it updates current group.
 	 * @return Returns an integer:<br>
 	 *         0: Failure<br>
 	 *         1: Success
 	 */
 	public int updateGroup(Group uGroup) {
 		Group group;
 		if (uGroup == null) {
 			group = getGroup();
 		} else {
 			group = uGroup;
 		}
 
 		group.setModified(new Date());
 
 		int success = groupsDAO.update(group);
 
 		return success;
 
 	}
 
 	/**
 	 * Update survey.
 	 * 
 	 * @param oldSurvey
 	 *            : the old version of the survey being updated
 	 * @param newSurvey
 	 *            : the new version of the survey being updated
 	 * 
 	 */
 	public void updateSurvey(Survey oldSurvey, Survey newSurvey) {
 
 		updSurveys.add(newSurvey);
 		lstSurveys.remove(oldSurvey);
 		lstSurveys.add(newSurvey);
 
 		gui.ToolBar.setSaveEnabled(true);
 
 	}
 }
