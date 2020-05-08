 package beans;
 
 import google.Appointment;
 import google.IAppointment;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.TimeZone;
 
 import com.google.gdata.data.DateTime;
 
 import core.EmployeeManager;
 import core.IEmployee;
 import core.IEmployeeManager;
 
 public class AppointmentTable{
 	
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 
 	private List<IAppointment> appointments;
 	//private List<String> appointments;
 	
 	private IEmployeeManager employeeManager;
 	
 	private String startDay;
 	
 	public AppointmentTable() {
 		this.employeeManager = new EmployeeManager();
 	}
 	
 	public IEmployeeManager getEmployeeManager(){
 		return this.employeeManager;
 	}
 	
 	public void setEmployeeManager(IEmployeeManager employeeManager) {
 		this.employeeManager = employeeManager;
 	}
 	
 	public List<IAppointment> getAppointments(){
 		return this.appointments;
 	}
 	
 	/*public void setAppointments(List<IAppointment> appointments) {
 		this.appointments = appointments;
 		 //this.appointmentBeginning = this.employeeManager.getAppointments(employees, startDate, duration);
 	}*/
 	
 	public void setAppointments(List<String> employeeNames, String startDateString, String durationString) {
 		try {
 			List<IEmployee> employees = this.employeeManager.getEmployees();
 			List<IEmployee> employeesToMatch = new ArrayList<IEmployee>();
 			for(IEmployee employee: employees) {
 				for(String name: employeeNames) {
 					if(employee.getName().equals(name)) {
 						employeesToMatch.add(employee);
 						System.out.println(name);
 						break;
 					}						
 				}
 			}
 			
 			DateTime startDate;
 			
 			if(startDateString.equalsIgnoreCase("") || startDateString.equalsIgnoreCase("Now")) {
 				
 				startDate = new DateTime(new Date().getTime());
 				
 			} else {
 				
 				Calendar c = Calendar.getInstance();
 
 				String[] splittedDate = startDateString.split(" ");
 				this.startDay = splittedDate[0];
 				String[] date = splittedDate[1].split("\\.");
 				int day = Integer.parseInt(date[0]);
 				int month = Integer.parseInt(date[1]);
 				int year = Integer.parseInt(date[2]);
 
 				c.set(year, month - 1, day, 0, 0, 0);
 				c.set(Calendar.MILLISECOND, 0);
 				
 				startDate = new DateTime(c.getTimeInMillis());
 			}	
 			
 			DateTime duration;
 			
 			if(durationString.split(" ")[0].equalsIgnoreCase("")) {
 				
 				duration = new DateTime(3600000);
 	
 			} else {
 				
 				duration = new DateTime(3600000 * Long.parseLong(durationString.split(" ")[0]));
 			}
 
 			this.appointments = this.employeeManager.getAppointments(employeesToMatch, startDate, duration);
			
			/*if(this.appointments.get(this.appointments.size()-1).getEndTime().getValue() > startDate.getValue()+432000000){
				System.out.println("Letztes Appointment der setAppointments-Methode dauert bis:");
				System.out.println(this.appointments.get(this.appointments.size()-1).getEndTime());
				System.out.println("und wird deshalb aus der Liste entfernt.");
				this.appointments.remove(this.appointments.size()-1);
			}*/
			
			System.out.println("Appointments nach der setAppointments-Methode:");
			for(IAppointment test : this.appointments){
				System.out.println(test.getStartTime().toUiString() + " --- " + test.getEndTime().toUiString());
			}
 		} catch(Exception ex) {
 			ex.printStackTrace();
 		}
 	}
 	
 	public void setAppointments_Test() {
 		System.out.println("Test");
 		this.appointments = new ArrayList<IAppointment>();
 		for(int i = 0; i < 5; i++) {
 			Calendar calendarStart = Calendar.getInstance();
 			calendarStart.set(Calendar.DAY_OF_MONTH, calendarStart.get(Calendar.DAY_OF_MONTH) + i);
 			calendarStart.set(Calendar.HOUR_OF_DAY, 6+i);
 			calendarStart.set(Calendar.MINUTE, 0);
 			Calendar calendarEnd = calendarStart;
 			calendarEnd.set(Calendar.HOUR_OF_DAY, 6+i+1);
 			IAppointment app = new Appointment(new DateTime(calendarStart.getTime()),new DateTime(calendarEnd.getTime()));
 			this.appointments.add(app);
 			calendarStart.set(Calendar.HOUR_OF_DAY, 7+i);
 			calendarStart.set(Calendar.HOUR_OF_DAY, 7+i+1);
 			app = new Appointment(new DateTime(calendarStart.getTime()), new DateTime(calendarEnd.getTime()));
 			this.appointments.add(app);
 			calendarStart.set(Calendar.HOUR_OF_DAY, 8+i);
 			calendarStart.set(Calendar.HOUR_OF_DAY, 8+i+1);
 			app = new Appointment(new DateTime(calendarStart.getTime()), new DateTime(calendarEnd.getTime()));
 			this.appointments.add(app);
 			calendarStart.set(Calendar.HOUR_OF_DAY, 9+i);
 			calendarStart.set(Calendar.HOUR_OF_DAY, 9+i+1);
 			app = new Appointment(new DateTime(calendarStart.getTime()), new DateTime(calendarEnd.getTime()));
 			this.appointments.add(app);
 		}
 	}
 	
 	public List<String> getAppointmentStrings(int dayOfWeek) {
		
		System.out.println("getAppointmentStrings mit " + dayOfWeek + " aufgerufen...");
 		List<String> dailyAppointments = new LinkedList<String>();
 		Calendar calendar = new GregorianCalendar(TimeZone.getTimeZone("CEST"));
		
		for(IAppointment app : this.appointments){
			calendar.setTimeInMillis(app.getStartTime().getValue());
			if(calendar.get(Calendar.DAY_OF_WEEK) == dayOfWeek){
				String timeSlot = app.getStartTime().toUiString().substring(11) + " - " + app.getEndTime().toUiString().substring(11);
				dailyAppointments.add(timeSlot);
			}
		}
		
		
		/*Calendar calendar = new GregorianCalendar(TimeZone.getTimeZone("CEST"));
 		for(IAppointment app: this.appointments) {
 			calendar.setTimeInMillis(app.getStartTime().getValue());
 			if(calendar.get(Calendar.DAY_OF_WEEK) == dayOfWeek){
 				String timeSlot = calendar.get(Calendar.HOUR_OF_DAY) + ":" + calendar.get(Calendar.MINUTE);
 				timeSlot += " - ";
 				calendar.setTimeInMillis(app.getEndTime().getValue());
 				timeSlot += calendar.get(Calendar.HOUR_OF_DAY) + ":" + calendar.get(Calendar.MINUTE);
 				dailyAppointments.add(timeSlot);
 			}
		}*/
		
		System.out.println("Appointments nach der getAppointmentStrings-Methode:");
		for(String s : dailyAppointments){
			System.out.println(s);
 		}
		
 		return dailyAppointments;
 	}
 	
 	public List<String> getAppointmentStrings_Test(int dayOfWeek) {
 		List<String> appointments = new LinkedList<String>();
 		for(int i = 0; i < 4; i++) {
 				String timeSlot = (6+i) + ":00";
 				timeSlot += " - ";
 				timeSlot += (6+i+1) + ":00";
 				appointments.add(timeSlot);
 		}
 		return appointments;
 	}
 }
