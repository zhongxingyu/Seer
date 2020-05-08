 package core.alarm;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.GregorianCalendar;
 import java.util.Stack;
 import java.util.concurrent.Executors;
 import java.util.concurrent.ScheduledExecutorService;
 import java.util.concurrent.ScheduledFuture;
 import java.util.concurrent.TimeUnit;
 
 
 import db.Appointment;
 
 public class AlarmHandler implements Runnable{
 
 	private AlarmEventSupport aes;
 	private Stack<Appointment> appointments;
 	
 	//constructor gets called when client-program loads. gets appointments from server.
 	public AlarmHandler(ArrayList<Appointment> appointments){
 		addAppointments(appointments);
 		aes = new AlarmEventSupport();
 	}
 	//let client listen for alarm events
 	public void addAlarmEventListener(AlarmListener listener){
 		aes.addAlarmEventListener(listener);
 	}
 	//When someone makes an new/edits appointment
 	public void addAppointment(Appointment app){
 		if(app.hasAlarm()){
 			appointments.push(app);
 			Collections.sort(appointments,new alarmComparator());
 		}
		else {
			System.out.println("this appointment does not have an alarm");
			//throw new IllegalArgumentException("This appointment does not have an alarm");
		}
 	}
 	//use this when a whole list needs to be imported, like in the start
 	public void addAppointments(ArrayList<Appointment> appointmentList){
 		appointments = new Stack<Appointment>();
 		for (Appointment appointment : appointmentList) {
 			if(appointment.hasAlarm())
 				appointments.push(appointment);
 		}
 		Collections.sort(appointments,new alarmComparator());
 	}
 	//deletes an alarm
 	public void removeAppointment(Appointment app){
 		appointments.remove(app);
 	}
 	
 	//this is the alarm thread, it will run contunously. 
 	@Override
 	public void run() {
 		long timeUntilNextAlarm;
 		Appointment current;
 		while(true){
 			//if there is an appointment in the list
 			if(!appointments.isEmpty()){
 			current = appointments.peek();
 			timeUntilNextAlarm = current.getAlarm().getMillis()-System.currentTimeMillis();
 			System.out.println("new alarm");
 			}
 			//if the list is empty it will sleep for 5 seconds and then begin at the start of the while-loop.
 			else{
 //				System.out.println("no alarm, waiting 5 seconds");
 				try {
 					Thread.sleep(5000);
 				} catch (InterruptedException e) {
 					System.out.println("interupted in standby sleep");
 //					e.printStackTrace();
 				}
 				continue;
 			}
 			//will sleep until next alarm. An interuption will wake it
 			try {
 //				System.out.println("new alarm, sleeping for " + (timeUntilNextAlarm/1000)+" seconds");
 				Thread.sleep(timeUntilNextAlarm);
 			} catch (InterruptedException e) {
 //				System.out.println("Interupted in waiting-for-alarm-sleep");
 //				e.printStackTrace();
 				continue;
 			}
 			soundAlarm(appointments.pop());
 		}
 	}
 	private void soundAlarm(Appointment alarm){
 		aes.fireAlarmEvent(alarm);
 	}
 }
