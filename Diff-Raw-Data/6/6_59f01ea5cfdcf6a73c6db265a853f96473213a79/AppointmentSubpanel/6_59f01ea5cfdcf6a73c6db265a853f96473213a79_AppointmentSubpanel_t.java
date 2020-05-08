 /**
  * An appointment subpanel is the panel containing the set of rooms (but not the time indicators on the 
  * left hand side of the appointment panel. So, the headings with practitioner information as well as
  * all of the available appointment blocks for the day. 
  */
 
 package gui.main;
 
 import gui.TimeSlot;
 
 import java.awt.GridLayout;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.swing.JPanel;
 
 import backend.DataService.DataServiceImpl;
 import backend.DataTransferObjects.AppointmentDto;
 import backend.DataTransferObjects.SchedulePractitionerDto;
 
 @SuppressWarnings("serial")
 public class AppointmentSubpanel extends JPanel {
 	
 	EmptyDayPanel empty = new EmptyDayPanel();
 	DayPanel dp;
 	ArrayList<RoomPanel> rooms = new ArrayList<RoomPanel>();
 	MainWindow main;
         
 	/** Constructs an appointment subpanel given a day panel object. */
 	public AppointmentSubpanel(DayPanel dp, MainWindow main) {
 		this.dp = dp;
        this.main = main;
 		GridLayout gl = new GridLayout(1,0);
 		gl.setHgap(0);
 		gl.setVgap(0);
 		setLayout(gl);
 		
 		
 		add(empty);
 		
 		dp.registerAppointmentSubpanel(this);
 	}
 	
 	/** Adds a practitioner to the day's schedule. */
 	public void addRoom(SchedulePractitionerDto room) {
 		if (rooms.size() == 0) remove(empty);
 		RoomPanel r = new RoomPanel(room, dp, main);
 		rooms.add(r);
 		add(r);
 		revalidate();
 	}
 	
 	/** Removes a practitioner from the day's schedule. */
 	public void removeRoom(SchedulePractitionerDto room) {
 		ArrayList<RoomPanel> remRooms = new ArrayList<RoomPanel>();
 		for (RoomPanel r : rooms) {
 			if (r.getRoom().equals(room))
 				remRooms.add(r);
 		}
 		for (RoomPanel r : remRooms) {
 			rooms.remove(r);
 			remove(r);
 		}
 		if (rooms.size() == 0) add(empty); 
		
 		revalidate();
		repaint();
 	}
 	
 	public void resetHours(TimeSlot timeSlot) {
 		ArrayList<RoomPanel> remRooms = new ArrayList<RoomPanel>();
 		for (RoomPanel rp : rooms) {
 			if (rp.room.getStart() > (timeSlot.getEndTime() - rp.room.getPractitioner().getApptLength()) ||
 					rp.room.getEnd() < (timeSlot.getStartTime() + rp.room.getPractitioner().getApptLength())) {
 				remRooms.add(rp);
 			} else {
 				DataServiceImpl.GLOBAL_DATA_INSTANCE.changePractitionerHoursForDay(
 						rp.room, dp.getDay(), dp.getDay().getStart(),
 						dp.getDay().getEnd());
 				List<AppointmentDto> remove = new ArrayList<AppointmentDto>();
 				for (AppointmentDto appt : rp.room.getAppointments()) {
 					if (appt.getStart() < rp.room.getStart() || appt.getEnd() > rp.room.getEnd()) {
 						remove.add(appt);
 					}
 				}
 				for (AppointmentDto appt : remove) {
 					rp.room.getAppointments().remove(appt);
 				}
 				rp.resetPractitionerHours(rp.room);
 			}
 		}
 		for (RoomPanel rp : remRooms) {
 			removeRoom(rp.room);
 			DataServiceImpl.GLOBAL_DATA_INSTANCE.removePractitionerFromDay(
 					rp.room.getPractSchedID(), dp.getDay());
 		}
 		revalidate();
 	}
 }
