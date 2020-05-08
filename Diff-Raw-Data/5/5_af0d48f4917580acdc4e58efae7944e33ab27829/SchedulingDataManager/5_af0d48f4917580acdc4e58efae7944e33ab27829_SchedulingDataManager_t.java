 package datalogic;
 
 import java.io.Serializable;
 import java.text.ParseException;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collection;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 
 import javax.annotation.PostConstruct;
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.ManagedProperty;
 import javax.faces.bean.ViewScoped;
 
 import org.icefaces.ace.component.datatable.DataTable;
 import org.icefaces.ace.component.dialog.Dialog;
 import org.icefaces.ace.model.table.RowState;
 import org.icefaces.ace.model.table.RowStateMap;
 import org.quartz.CronExpression;
 
 import datalogic.SchedulingTab;
 
 import entities.Backend;
 import entities.Comment;
 import entities.Composite;
 import entities.Instance;
 import entities.Mode;
 import entities.Scheduling;
 
 @ManagedBean(name = "schedulingDataManager")
 @ViewScoped
 public class SchedulingDataManager implements Serializable {
 
 	@ManagedProperty(value = "#{sessionBean}")
 	private SessionBean session;
 
 	private SchedulingBuilder builder;
 
 	private List<Scheduling> schedulings = new ArrayList<Scheduling>();
 	private RowStateMap stateMap = new RowStateMap();
 	private DataTable dataTable;
 
 	private Collection<Mode> modes = ApplicationBean.MODES.values();
 	private Collection<Composite> composites = ApplicationBean.COMPOSITES
 			.values();
 	private Collection<Backend> backends = ApplicationBean.BACKENDS.values();
 
 	private boolean showAddError;
 	private String addErrorMessage;
 
 	private HttpConnector httpConnector = new HttpConnector();
 
 	private boolean responseDialogVisible;
 	private List<String> runReport = new ArrayList<String>();
 
 	private String schedulingList;
 
 	private Date startDate;
 	private Date endDate;
 
 	private Date maxStartDate;
 	private Date maxEndDate = new Date();
 
 	private String dateError;
 	private boolean showDateError = false;
 
 	private List<Run> matching = new ArrayList<Run>();
 	private RowStateMap runStateMap = new RowStateMap();
 
 	private static final int SCHEDULING_NAME_LEN = 30;
 
 	private boolean addSchedulingInf = false;
 	private boolean EditSchedulingInf = false;
 	
 	private String addSchedulingMessage;
 	private String EditSchedulingMessage;
 
 	private boolean editSchedulingSucces = false;
 
 	private boolean addSchedulingSucces = false;
 	
 	//http response messages
 	public static String EDIT_SUCCESS_MSG = "Changes have been saved.";
 	public static String ADD_SUCCESS_MSG = "Scheduling was added succesfully.";
 	public static String EMPTYPARAMETER_ERROR_MSG = "The Scheduling Service received empty parameter.";
 	public static String INTERNAL_ERROR_MSG =  "Error 500 - Internal Server Error";
 	public static String UNKNOWN_ERROR_MSG =  "ClientProtocolException or an IOException";
 
 
 	@PostConstruct
 	private void init() {
 
 		Calendar c = Calendar.getInstance();
 		c.setTime(maxEndDate);
 		c.add(Calendar.DATE, -1);
 		this.maxStartDate = c.getTime();
 		this.startDate = this.maxStartDate;
 		this.endDate = this.maxEndDate;
 
 		this.builder = new SchedulingBuilder();
 
 		this.schedulings = this.session.getSchedulings();
 	}
	
	public void clearScheduling(){
		this.builder = new SchedulingBuilder();
		System.out.println(this.builder.getContacts());
	}
 
 	public void listSelected() {
 		if (stateMap.getSelected().isEmpty()) {
 			this.schedulingList = "None";
 			return;
 		}
 
 		this.schedulingList = "";
 		for (Scheduling s : (List<Scheduling>) stateMap.getSelected()) {
 			this.schedulingList += getTrimmedName(s.getName());
 		}
 	}
 
 	public void listSelectedRuns() {
 		if (runStateMap.getSelected().isEmpty()) {
 			this.schedulingList = "None";
 			return;
 		}
 
 		this.schedulingList = "";
 		for (Run r : (List<Run>) runStateMap.getSelected()) {
 			this.schedulingList += getTrimmedName(r.getScheduling().getName());
 		}
 	}
 
 	public void listAllRuns() {
 		if (this.matching.isEmpty()) {
 			this.schedulingList = "None";
 			return;
 		}
 
 		this.schedulingList = "";
 		for (Run r : this.matching) {
 			this.schedulingList += getTrimmedName(r.getScheduling().getName());
 		}
 	}
 
 	private String getTrimmedName(String fullName) {
 		if (fullName.length() <= SCHEDULING_NAME_LEN)
 			return fullName + "<br/>";
 		else
 			return fullName.substring(0, SCHEDULING_NAME_LEN) + "<br/>";
 	}
 
 	/**
 	 * Method to be called from the UI when a new {@link Scheduling} is to be
 	 * constructed from the {@link SchedulingBuilder}
 	 */
 	public void addScheduling() {
 
 		try {
 
 			/*
 			 * Build the Scheduling from values inserted to the builder. Values
 			 * are validated within the builder and an IllegalOperationException
 			 * will be thrown if the values are invalid
 			 */
 			Scheduling s = this.builder.build();
 
 			// If the database connector returns true from the persisting of
 			// Scheduling we can safely add it to the table
 			if (this.session.getConnector().addScheduling(s))
 				this.schedulings.add(s);
 
 			/*
 			 * If there is some text in addComment, we'll add it straight away.
 			 * It will be connected by the ID of previously added Scheduling so
 			 * its important to add the Comment after the Scheduling has been
 			 * persisted and an ID has been assigned to it.
 			 */
 
 			this.submitCommentFromAdd(s);
 
 			this.builder = new SchedulingBuilder();
 
 			// If we've gotten this far, there were no errors and we can hide
 			// all error messages
 			showAddError = false;
 
 			// Http request, consult Hanzki for any details
 			int http_response = httpConnector.addId(
 					ApplicationBean.COMPOSITES.get(s.getServiceID())
 							.getDestinationURL(), s.getId());
 			
 			System.out.println("HttpConnector returned: "
 					+ http_response);
 		
 			switch (http_response)
 			{
 				case HttpConnector.RESPONSE_OK:
 					setAddSchedulingMessage(ADD_SUCCESS_MSG);
 					setAddSchedulingSucces(true);
 					break;
 				case HttpConnector.RESPONSE_EMPTY_PARAMETER:
 					setAddSchedulingMessage(EMPTYPARAMETER_ERROR_MSG);
 					setAddSchedulingSucces(false);
 					break;
 				case HttpConnector.RESPONSE_INTERNAL_ERROR:
 					setAddSchedulingMessage(INTERNAL_ERROR_MSG);
 					setAddSchedulingSucces(false);
 					break;
 				case HttpConnector.RESPONSE_UNKOWN_ERROR:
 					setAddSchedulingMessage(UNKNOWN_ERROR_MSG);
 					setAddSchedulingSucces(false);
 					break;
 				default:
 					setAddSchedulingMessage(UNKNOWN_ERROR_MSG);
 					break;
 			}
 			setAddSchedulingInf(true);
 			
 		} catch (IllegalOperationException e) {
 
 			/*
 			 * An error was thrown during the validation of the Scheduling: we
 			 * set the error message to be shown in Add Scheduling section and
 			 * set the visibility to true
 			 */
 			addErrorMessage = e.getMessage();
 			showAddError = true;
 		}
 	}
 
 	public void confirmEdit(SchedulingTab t) {
 
 		try {
 
 			/*
 			 * Retrieve the SchedulingBuilder corresponding to the id of
 			 * Scheduling passed as a parameter from the UI and build it.
 			 * Validation is carried out normally within the builder
 			 */
 			Scheduling n = t.getBuilder().build();
 			if (n.equals(t.getScheduling())) {
 				System.out.println("No changes");
 				return;
 			}
 			/*
 			 * In order to refresh the DataTable we need to "brute force" the
 			 * change by removing the unedited Scheduling and adding the newly
 			 * built one
 			 */
 			this.schedulings.remove(t.getScheduling());
 			this.schedulings.add(n);
 
 			this.session.getConnector().updateScheduling(n);
 
 			// If we've gotten this far there were no errors and we can hide all
 			// error messages
 			t.setShowEditError(false);
 			this.submitCommentFromEdit(t);
 
 			t.setScheduling(n);
 			// Http request, consult Hanzki for any details
 			
 			int http_response = httpConnector.editId(
 					ApplicationBean.COMPOSITES.get(
 							t.getScheduling().getServiceID())
 							.getDestinationURL(), t.getScheduling()
 							.getId());
 			System.out.println("HttpConnector returned: "
 					+ http_response);
 			switch (http_response)
 			{
 				case HttpConnector.RESPONSE_OK:
 					setEditSchedulingMessage(EDIT_SUCCESS_MSG);
 					setEditSchedulingSucces(true);
 					break;
 				case HttpConnector.RESPONSE_EMPTY_PARAMETER:
 					setEditSchedulingMessage(EMPTYPARAMETER_ERROR_MSG);
 					setEditSchedulingSucces(false);
 					break;
 				case HttpConnector.RESPONSE_INTERNAL_ERROR:
 					setEditSchedulingMessage(INTERNAL_ERROR_MSG);
 					setEditSchedulingSucces(false);
 					break;
 				case HttpConnector.RESPONSE_UNKOWN_ERROR:
 					setEditSchedulingMessage(UNKNOWN_ERROR_MSG);
 					setEditSchedulingSucces(false);
 					break;
 				default:
 					setEditSchedulingMessage(UNKNOWN_ERROR_MSG);
 					setEditSchedulingSucces(false);
 					break;
 			}
 			setEditSchedulingInf(true);
 			
 		} catch (IllegalOperationException e) {
 
 			/*
 			 * An error was thrown during the validation of the edited
 			 * Scheduling: we set the error message to be shown in Edit section
 			 * and set the visibility to true
 			 */
 			t.setEditErrorMessage(e.getMessage());
 			t.setShowEditError(true);
 		}
 
 	}
 
 	public void resetEdit(SchedulingTab t) {
 		t.reset();
 	}
 
 	private void submitCommentFromAdd(Scheduling s) {
 		this.builder.getComment().setSchedulingID(s.getId());
 		this.builder.getComment().setCreationDate(
 				ApplicationBean.DATE_FORMAT.format(new Date()));
 		if (session.getConnector().addComment(this.builder.getComment())) {
 			this.builder.setComment(new Comment());
 		}
 	}
 
 	private void submitCommentFromEdit(SchedulingTab t) {
 		if (this.submitComment(t.getBuilder().getComment(), t))
 			t.getBuilder().setComment((new Comment()));
 	}
 
 	public void submitNewComment(SchedulingTab t) {
 		if (this.submitComment(t.getAddComment(), t))
 			t.setAddComment(new Comment());
 	}
 
 	private boolean submitComment(Comment c, SchedulingTab t) {
 		c.setCreationDate(ApplicationBean.DATE_FORMAT.format(new Date()));
 		c.setSchedulingID(t.getScheduling().getId());
 
 		if (this.session.getConnector().addComment(c)) {
 			refreshComments(t);
 			return true;
 		}
 		return false;
 	}
 
 	private void refreshComments(SchedulingTab t) {
 		t.setComments(this.session.getConnector().getComments(
 				t.getScheduling().getId(), ApplicationBean.MAX_COMMENTS_SHOWN));
 	}
 
 	/**
 	 * Method to be called from the UI when Select All button is pressed and all
 	 * rows currently filtered (or lazily loaded) are to be selected. The [@link
 	 * DataTable} is bound to this bean so we can operate on the filtered data.
 	 */
 	public void selectFiltered() {
 
 		List<Scheduling> filteredRows = dataTable.getFilteredData();
 
 		/*
 		 * If the filtered data is empty or null, we do not currently have a
 		 * filter and do not wish to select anything.
 		 */
 		if (filteredRows == null || filteredRows.isEmpty()) {
 			return;
 		}
 
 		// Iterate through the filtered data and set all rows to be selected
 		for (Scheduling s : filteredRows) {
 			stateMap.get(s).setSelected(true);
 		}
 	}
 
 	/**
 	 * Method to be called from the UI when Deselect All button is pressed and
 	 * all selected rows are to be deselected.
 	 */
 	public void deselectAll() {
 		Collection<RowState> allRows = stateMap.values();
 
 		for (RowState s : allRows) {
 			s.setSelected(false);
 		}
 	}
 
 	// TODO Comments
 	public void runSelectedSchedules() {
 		run(stateMap.getSelected());
 	}
 
 	// TODO comments
 	public void resumeSelected() {
 
 		for (Object rowData : stateMap.getSelected()) {
 			Scheduling s = (Scheduling) rowData;
 
 			if (s.getStatusID() != ApplicationBean.REMOVED) {
 				s.setStatusID(ApplicationBean.ENABLED);
 
 				this.session.getConnector().updateScheduling(s);
 				System.out.println("HttpConnector returned: "
 						+ httpConnector.editId(
 								ApplicationBean.COMPOSITES
 										.get(s.getServiceID())
 										.getDestinationURL(), s.getId()));
 			}
 		}
 	}
 
 	// TODO comments
 	public void holdSelected() {
 
 		for (Object rowData : stateMap.getSelected()) {
 			Scheduling s = (Scheduling) rowData;
 
 			if (s.getStatusID() != ApplicationBean.REMOVED) {
 				s.setStatusID(ApplicationBean.DISABLED);
 
 				this.session.getConnector().updateScheduling(s);
 				System.out.println("HttpConnector returned: "
 						+ httpConnector.editId(
 								ApplicationBean.COMPOSITES
 										.get(s.getServiceID())
 										.getDestinationURL(), s.getId()));
 			}
 		}
 	}
 
 	// TODO comments
 	public void removeSelected() {
 
 		for (Object rowData : stateMap.getSelected()) {
 			Scheduling s = (Scheduling) rowData;
 
 			if (s.getStatusID() != ApplicationBean.REMOVED) {
 				s.setStatusID(ApplicationBean.REMOVED);
 
 				this.session.getConnector().updateScheduling(s);
 				System.out.println("HttpConnector returned: "
 						+ httpConnector.editId(
 								ApplicationBean.COMPOSITES
 										.get(s.getServiceID())
 										.getDestinationURL(), s.getId()));
 			}
 		}
 	}
 
 	/**
 	 * Method for refreshing the contents of the data table. We need to tell
 	 * {@link SessionBean} to refresh the [@link Scheduling} list and then
 	 * retrieve it to a local variable.
 	 */
 	public void refresh() {
 		this.schedulings.clear();
 		this.session.refreshSchedulings();
 		this.schedulings = this.session.getSchedulings();
 	}
 
 	/**
 	 * Executed when a date interval is submitted from the UI. We want to search
 	 * for active {@link Scheduling} instances that should've had a CRON trigger
 	 * during the interval. Then we check for any {@link Instance} objects
 	 * belonging to the {@link Scheduling} and with a date matching that trigger
 	 * . If we don't find any matches or the matching {@link Instance} failed,
 	 * we want to show the {@link Scheduling} in UI.
 	 */
 	public void dateSubmit() {
 
 		try {
 			if (validateDates(this.startDate, this.endDate))
 				;
 			this.showDateError = false;
 		} catch (IllegalOperationException e) {
 			this.dateError = e.getMessage();
 			this.showDateError = true;
 			return;
 		}
 
 		Date today = new Date();
 
 		// Clear any previous results
 		this.matching = new ArrayList<Run>();
 
 		for (Scheduling s : this.schedulings) {
 
 			// If the status isn't active we can skip this scheduling
 			if (s.getStatusID() != ApplicationBean.ENABLED)
 				continue;
 
 			try {
 
 				CronExpression cron = new CronExpression(s.getCron());
 
 				/*
 				 * Initialize a calendar and retrieve date one hour from this
 				 * moment
 				 */
 				Calendar cal = Calendar.getInstance();
 				cal.setTime(today);
 				cal.add(Calendar.HOUR_OF_DAY, 1);
 
 				// Earliest allowed launch from this moment
 				Date threshold = cal.getTime();
 
 				// The last launch between start date and end date
 				Date prev = this.getPreviousLaunch(cron, this.startDate,
 						this.endDate);
 
 				// Next launch from this moment
 				Date next = cron.getNextValidTimeAfter(today);
 
 				/*
 				 * Check that previous run is after given start date (this
 				 * should never happen but just to be sure) and before given end
 				 * date. Also check that next run isn't earlier than one hour
 				 * from now
 				 */
 				if (prev != null && prev.after(this.startDate)
 						&& prev.before(this.endDate) && next.after(threshold)) {
 
 					// Get instances matching the previous run
 					List<Instance> instances = this.session
 							.getInstancesByDate().get(prev);
 
 					Run r = new Run();
 					r.setScheduling(s);
 					r.setNext(next.toString());
 					r.setPrev(prev.toString());
 
 					/*
 					 * If instances is null, we know Scheduling was not run and
 					 * can add it to the list straight away
 					 */
 					if (instances == null || instances.isEmpty()) {
 						r.setStatus("None");
 						this.matching.add(r);
 					} else {
 
 						// If there were instances, iterate through them
 						for (Instance i : instances) {
 
 							/*
 							 * We want the instance to match the scheduling, but
 							 * its status musn't be Completed or Skipped. If we
 							 * get a match, no need to look further
 							 */
 							if (s.matchesInstance(i)
 									&& !(i.getStatusValue().equals("Completed") || i
 											.getStatusValue().equals("Skipped"))) {
 								r.setStatus(i.getStatusValue());
 								this.matching.add(r);
 								break;
 							}
 						}
 
 					}
 				}
 
 			} catch (ParseException e) {
 				// The CRON was invalid
 			}
 		}
 
 	}
 
 	private boolean validateDates(Date start, Date end)
 			throws IllegalOperationException {
 		boolean error = false;
 		Date today = new Date();
 		String msg = "";
 		if (this.startDate.after(today)) {
 			error = true;
 			msg += "Start date cannot be after present!\n";
 		}
 
 		if (this.endDate.after(today)) {
 			error = true;
 			msg += "End date cannot be after present!\n";
 		}
 
 		if (this.endDate.equals(this.startDate)) {
 			error = true;
 			msg += "End date cannot be the same as start date!\n";
 		}
 
 		if (this.endDate.before(this.startDate)) {
 			error = true;
 			msg += "End date cannot be before start date!\n";
 		}
 
 		if (error)
 			throw new IllegalOperationException(msg);
 
 		return true;
 	}
 
 	/*
 	 * Helper method to retrieve the previous run of the CRON between given
 	 * dates since CronExpression doesn't have this functionality.
 	 */
 	private Date getPreviousLaunch(CronExpression cron, Date min, Date max) {
 		Date candidate = min;
 		do {
 			candidate = cron.getNextValidTimeAfter(candidate);
 		} while (cron.getNextValidTimeAfter(candidate).before(max));
 
 		if (candidate.before(max))
 			return candidate;
 		else
 			return null;
 	}
 
 	public void deselectAllRuns() {
 		Collection<RowState> allRows = runStateMap.values();
 
 		for (RowState r : allRows) {
 			r.setSelected(false);
 		}
 	}
 
 	public void runSelectedRuns() {
 		List<Scheduling> schedulings = new ArrayList<Scheduling>();
 		for (Object r : runStateMap.getSelected()) {
 			schedulings.add(((Run) r).getScheduling());
 		}
 
 		run(schedulings);
 	}
 
 	public void runAllRuns() {
 		List<Scheduling> schedulings = new ArrayList<Scheduling>();
 		for (Run r : this.matching) {
 			schedulings.add(r.getScheduling());
 		}
 
 		run(schedulings);
 	}
 
 	private void run(List<Scheduling> schedulings) {
 		this.runReport.clear();
 		int succesfulRuns = 0;
 		String failedName = null;
 		int error = 0;
 		for (Scheduling s : schedulings) {
 			int response = httpConnector.runId(
 					ApplicationBean.COMPOSITES.get(s.getServiceID())
 							.getDestinationURL(), s.getId());
 
 			if (response == HttpConnector.RESPONSE_OK) {
 				succesfulRuns++;
 			} else {
 				failedName = getTrimmedName(s.getName());
 				error = response;
 				break;
 			}
 		}
 
 		if (failedName != null) {
 			this.runReport.add((succesfulRuns + 1) + " schedulings were run.");
 			this.runReport.add(succesfulRuns + " schedulings were succesful.");
 			this.runReport.add("Response for scheduling " + failedName
 					+ " was " + error + ".");
 		} else if (succesfulRuns > 0) {
 			this.runReport.add(succesfulRuns + " schedulings were run.");
 
 			this.runReport.add(succesfulRuns + " schedulings were succesful.");
 		} else
 			this.runReport.add("No schedulings were run.");
 		this.responseDialogVisible = true;
 	}
 
 	public void closeRunReport() {
 		this.responseDialogVisible = false;
 	}
 
 	public void closeAddSchedulingInf(){
 		this.addSchedulingInf = false;
 	}
 	public void closeEditSchedulingInf(){
 		this.EditSchedulingInf = false;
 	}
 	
 	// ==================== GETTERS & SETTERS ====================
 	public SessionBean getSession() {
 		return session;
 	}
 
 	public void setSession(SessionBean session) {
 		this.session = session;
 	}
 
 	public SchedulingBuilder getBuilder() {
 		return builder;
 	}
 
 	public void setBuilder(SchedulingBuilder builder) {
 		this.builder = builder;
 	}
 
 	public List<Scheduling> getSchedulings() {
 		return schedulings;
 	}
 
 	public void setSchedulings(List<Scheduling> schedulings) {
 		this.schedulings = schedulings;
 	}
 
 	public RowStateMap getStateMap() {
 		return stateMap;
 	}
 
 	public void setStateMap(RowStateMap stateMap) {
 		this.stateMap = stateMap;
 	}
 
 	public boolean isShowAddError() {
 		return showAddError;
 	}
 
 	public void setShowAddError(boolean addError) {
 		// Cannot be set from the UI but the setter has to exist
 	}
 
 	public String getAddErrorMessage() {
 		return addErrorMessage;
 	}
 
 	public void setAddErrorMessage(String addErrorMessage) {
 		this.addErrorMessage = addErrorMessage;
 	}
 
 	public DataTable getDataTable() {
 		return dataTable;
 	}
 
 	public void setDataTable(DataTable dataTable) {
 		this.dataTable = dataTable;
 	}
 
 	public Collection<Mode> getModes() {
 		return modes;
 	}
 
 	public void setModes(Collection<Mode> modes) {
 		this.modes = modes;
 	}
 
 	public Collection<Composite> getComposites() {
 		return composites;
 	}
 
 	public void setComposites(Collection<Composite> composites) {
 		this.composites = composites;
 	}
 
 	public Collection<Backend> getBackends() {
 		return backends;
 	}
 
 	public void setBackends(Collection<Backend> backends) {
 		this.backends = backends;
 	}
 
 	public boolean isResponseDialogVisible() {
 		return responseDialogVisible;
 	}
 
 	public void setResponseDialogVisible(boolean responseDialogVisible) {
 		this.responseDialogVisible = responseDialogVisible;
 	}
 
 	public List<String> getRunReport() {
 		return runReport;
 	}
 
 	public void setRunReport(List<String> runReport) {
 		this.runReport = runReport;
 	}
 
 	public String getSchedulingList() {
 		return schedulingList;
 	}
 
 	public void setSchedulingList(String schedulingList) {
 		this.schedulingList = schedulingList;
 	}
 
 	public Date getStartDate() {
 		return startDate;
 	}
 
 	public void setStartDate(Date startDate) {
 		this.startDate = startDate;
 	}
 
 	public Date getEndDate() {
 		return endDate;
 	}
 
 	public void setEndDate(Date endDate) {
 		this.endDate = endDate;
 	}
 
 	public List<Run> getMatching() {
 		return matching;
 	}
 
 	public void setMatching(List<Run> matching) {
 		this.matching = matching;
 	}
 
 	public Date getMaxStartDate() {
 		return maxStartDate;
 	}
 
 	public void setMaxStartDate(Date maxStartDate) {
 		this.maxStartDate = maxStartDate;
 	}
 
 	public Date getMaxEndDate() {
 		return maxEndDate;
 	}
 
 	public void setMaxEndDate(Date maxEndDate) {
 		this.maxEndDate = maxEndDate;
 	}
 
 	public String getDateError() {
 		return dateError;
 	}
 
 	public void setDateError(String dateError) {
 		this.dateError = dateError;
 	}
 
 	public boolean getShowDateError() {
 		return showDateError;
 	}
 
 	public void setShowDateError(boolean showDateError) {
 		this.showDateError = showDateError;
 	}
 
 	public RowStateMap getRunStateMap() {
 		return runStateMap;
 	}
 
 	public void setRunStateMap(RowStateMap runStateMap) {
 		this.runStateMap = runStateMap;
 	}
 
 	public boolean getAddSchedulingInf() {
 		return addSchedulingInf;
 	}
 
 	public void setAddSchedulingInf(boolean addSchedulingInf) {
 		this.addSchedulingInf = addSchedulingInf;
 	}
 
 	public Object getAddSchedulingMessage() {
 		return addSchedulingMessage;
 	}
 
 	public void setAddSchedulingMessage(String addSchedulingMessage) {
 		this.addSchedulingMessage = addSchedulingMessage;
 	}
 
 	public boolean getEditSchedulingInf() {
 		return EditSchedulingInf;
 	}
 
 	public void setEditSchedulingInf(boolean editSchedulingInf) {
 		EditSchedulingInf = editSchedulingInf;
 	}
 
 	public String getEditSchedulingMessage() {
 		return EditSchedulingMessage;
 	}
 
 	public void setEditSchedulingMessage(String editSchedulingMessage) {
 		EditSchedulingMessage = editSchedulingMessage;
 	}
 
 	public boolean getEditSchedulingSucces() {
 		return editSchedulingSucces;
 	}
 
 	public void setEditSchedulingSucces(boolean editSchedulingSucces) {
 		this.editSchedulingSucces = editSchedulingSucces;
 	}
 
 	public boolean getAddSchedulingSucces() {
 		return addSchedulingSucces;
 	}
 
 	public void setAddSchedulingSucces(boolean addSchedulingSucces) {
 		this.addSchedulingSucces = addSchedulingSucces;
 	}
 
 }
