 package spitapp.controller;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 
 import spitapp.core.model.Appointment;
 import spitapp.core.model.ExpensesEntry;
 import spitapp.core.model.Patient;
 import spitapp.core.model.Task;
 import spitapp.core.service.UiServiceFacade;
 import spitapp.util.DateUtil;
 
 /**
  * controlls the appointment listing and fires an event, when an appointment
  * changes
  * 
  * @author Roger Jaggi
  * 
  */
 public class AppointmentController {
 	
 	/**
 	 * the list of appointments of a day
 	 */
 	protected HashMap<Integer, Appointment> appointments = new HashMap<Integer, Appointment>();
 
 	/**
 	 * the GUI Id of the current GUI selection
 	 */
 	protected Integer latestId = 0;
 
 	/**
 	 * the GUI Id of the current GUI selection
 	 */
 	protected Integer currentSelectionGuiId = null;
 
 	/**
 	 * the list of Listeners for the AppointmentChangedEvent
 	 */
 	private List<AppointmentChangedListener> listeners = new ArrayList<AppointmentChangedListener>();
 
 
 	/**
 	 * the constructor
 	 * 
 	 * @param dbservice
 	 *            The reference to the DatabaseService to use
 	 */
 	public AppointmentController() {
 	}
 
 	/**
 	 * Returns the appointment list with their associeted gui-IDs
 	 * 
 	 * @return map of appointments
 	 */
 	public HashMap<Integer, Appointment> getAppointments() {
 		return this.appointments;
 	}
 
 	/**
 	 * Gets an appointment object by it's GUI id
 	 * 
 	 * @param guiid
 	 *            the gui id
 	 * @return an appointment object or null if not found
 	 */
 	public Appointment getCurrentAppointment() {
 		return this.appointments.get(this.currentSelectionGuiId);
 	}
 
 	/**
 	 * sets the new current appointment
 	 * 
 	 * @param newguiid
 	 *            the new gui-ID
 	 * @return true, if successfull, fail, if ID not found
 	 */
 	public boolean changeAppointment(Integer newguiid) {
 		this.currentSelectionGuiId = newguiid;
 
 		// todo: check newguiid if valid
 
 		fireAppointmentChangedEvent();
 
 		return true;
 	}
 
 	/**
 	 * method is called when the date changes
 	 * 
 	 * @return true if method succeeded
 	 */
 	public boolean addAppointment(Appointment entry) {
 		if (entry != null) {
 			this.latestId += 1;
 			this.appointments.put(this.latestId, entry);
 			return true;
 		}
 
 		return false;
 	}
 
 	/**
 	 * Loads appointments by a given date
 	 * 
 	 * @param datetoload
 	 *            the date from that appointments should be loaded
 	 * @return the count of appointments loaded
 	 */
 	public Integer loadAppointmentsByDate(Date datetoload) {
 		if(datetoload == null) {
 			return -1;
 		}
 		
 		this.appointments.clear();
 
 		List<Appointment> entries = UiServiceFacade.getInstance().getAppointments(datetoload);
 		if (entries == null) {
 			return -2;
 		}
 
 		for (Appointment entry : entries) {
 			this.addAppointment(entry);
 		}
 
 		return entries.size();
 	}
 
 	/**
 	 * adds a further event listener
 	 * 
 	 * @param listener
 	 *            the listener to add
 	 */
 	public synchronized void addAppointmentChangedEventListener(
 			AppointmentChangedListener listener) {
 		this.listeners.add(listener);
 	}
 
 	/**
 	 * Removes an Event Listener
 	 * 
 	 * @param listener
 	 *            the Listener to remove
 	 */
 	public synchronized void removeAppointmentChangedEventListener(
 			AppointmentChangedListener listener) {
 		this.listeners.remove(listener);
 	}
 
 	/**
 	 * this method can be called whenever you want to notify the event listeners
 	 * of the AppointmentChangedEvent
 	 */
 	public synchronized void fireAppointmentChangedEvent() {
 		AppointmentChangedEvent event = new AppointmentChangedEvent(this);
 		Iterator<AppointmentChangedListener> i = this.listeners.iterator();
 		while (i.hasNext()) {
 			i.next().handleAppointmentChangedEvent(event);
 		}
 	}
 	
 	/**
 	 * Adds a new expense entry to current selected patient (via appointment)
 	 * @param descpription the descpription of the expense
 	 * @param amount the amount as decimal value in a string
 	 * @return 0 if successfull or lesser than 0 on failure
 	 */
 	public Integer addExpensetoCurrentPatient(String descpription, String amount) {
 		Double value = null;
 		
 		if(amount == null ) {
 			return -1;
 		}
 		try {
 			value = Double.parseDouble(amount);
 		} 
 		catch(NumberFormatException ex) {
 			return -1;
 		}
 		
 		if(descpription == null) {
 			return -2;
 		}
 		if(descpription.isEmpty()) {
 			return -2;
 		}
 		
 		try {
 			Appointment appointment = this.getCurrentAppointment();
 			
 			Patient patient = appointment.getPatient();
 			
 			ExpensesEntry newexpense = new ExpensesEntry();
 			newexpense.setExpensesDescription(descpription);
 			newexpense.setPrice(value);
 			
 			patient.getExpenses().add(newexpense);
 			
 			UiServiceFacade.getInstance().saveModel(appointment);
 		}
 		catch(NumberFormatException ex) {
 			return -4;
 		}
 		
 		return 0;
 	}
 	
 	/**
 	 * Deletes an expesense on the current selected patient
 	 * @param expense_id the id of the expense to delete
 	 * @return 1 if successfull, 0 if not found or lesser than 0 on failure
 	 */
 	public Integer deleteExepenseOfCurrentPatient(Long expense_id) {
 		if(expense_id == null) {
 			return -4;
 		}
 		
 		Patient patient = this.getCurrentAppointment().getPatient();
 		if(patient == null) {
 			return -3;
 		}
 		List<ExpensesEntry> expenses = patient.getExpenses();
 		if(expenses == null) {
 			return -1;
 		}
 		else {
 			try { 
 				for(ExpensesEntry entry : expenses) {
 					if(entry.getExpensesId() == expense_id) {
 						expenses.remove(entry);
 						UiServiceFacade.getInstance().saveModel(patient);
 						//this.dbservice.delete(entry); doesnt work
 						return 1;
 					}
 				}
 			}
 			catch(Exception ex) {
 				return -2;
 			}
 		}
 		
 		return 1;
 	}
 	
 	/**
 	 * Get's a task from the CURRENT PATIENT by Id
 	 * @param task_id the id of the task
 	 * @return The Task or null if not found
 	 */
 	public Task getTaskById(Long task_id) {
 		if(task_id == null) {
 			return null;
 		}
 		
 		Patient patient = this.getCurrentAppointment().getPatient();
 		if(patient == null) {
 			return null;
 		}
 		List<Task> tasks = patient.getTasks();
 		if(tasks == null) {
 			return null;
 		}
 		else {
 				for(Task entry : tasks) {
					if(entry.getTaskId() == task_id) {
 						return entry;
 					}
 				}
 		}
 		
 		return null;
 	}
 
 	/**
 	 * Marks a task on the current selected patient as completed and adds a time record
 	 * @param task_id the id of the expense to delete
 	 * @return 1 if successfull, 0 if not found or lesser than 0 on failure
 	 */
 	public Integer completeTaskOfCurrentPatient(Long task_id, String starttime_input, String duration_input) {
 		
 		if(starttime_input == null || starttime_input.isEmpty()) {
 			return -4;
 		}
 				
 		if(duration_input == null || duration_input.isEmpty()) {
 			return -3;
 		}
 
 
 		Date starttime = DateUtil.getTodayWithSpecificTime(starttime_input);
 		if(starttime == null) {
 			return -2;
 		}
 		
 		Integer duration = null;
 		try {
 			duration = Integer.parseInt(duration_input);
 		} 
 		catch(NumberFormatException ex) {
 			return -1;
 		}
 		
 		Task theTask = getTaskById(task_id);
 		if(theTask == null) {
 			return 0;
 		}
 		
 		theTask.setDone(true);
 		theTask.setStarttime(starttime);
 		theTask.setDuration(duration);
 		
 		
 		UiServiceFacade.getInstance().saveModel(theTask);
 		
 		return 1;
 	}
 	
 	/**
 	 * Reactivates a task of the current patient
 	 * @param task_id the id of the task
 	 * @return 1 on success or -1 on failure
 	 */
 	public Integer reactivateTaskOfCurrentPatient(Long task_id) {
 				
 		Task theTask = getTaskById(task_id);
 		if(theTask == null) {
 			return -1;
 		}
 		
 		theTask.setDone(false);
 		theTask.setStarttime(null);
 		theTask.setDuration(0);
 		
 		UiServiceFacade.getInstance().saveModel(theTask);
 		
 		return 1;
 	}
 }
