 package calendar.daydream.data;
 
 import android.provider.CalendarContract.Attendees;
import android.text.TextUtils;
 
 public class CalendarAttendee implements Comparable<CalendarAttendee> {
 
 	private String name;
 	private String email;
 	private int relationship;
 	private int status;
 	
 	public enum Type { ORGANIZER, ATTENDING, NOT_ATTENDING, UNKNOWN };
 	
 	public CalendarAttendee(String name, String email, int relationship,
 			int status) {
 		super();
 		this.name = name;
 		this.email = email;
 		this.relationship = relationship;
 		this.status = status;
 	}
 
 	public String getName() {
		if(TextUtils.isEmpty(name)) {
			return email;
		} else {
			return name;
		}
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	public String getEmail() {
 		return email;
 	}
 
 	public void setEmail(String email) {
 		this.email = email;
 	}
 
 	public int getRelationship() {
 		return relationship;
 	}
 
 	public void setRelationship(int relationship) {
 		this.relationship = relationship;
 	}
 
 	public int getStatus() {
 		return status;
 	}
 
 	public void setStatus(int status) {
 		this.status = status;
 	}
 
 	public Type getType() {
 		if(relationship == Attendees.RELATIONSHIP_ORGANIZER) {
 			return Type.ORGANIZER;
 		}
 		switch(status) {
 		case Attendees.ATTENDEE_STATUS_ACCEPTED:
 			return Type.ATTENDING;
 		case Attendees.ATTENDEE_STATUS_DECLINED:
 			return Type.NOT_ATTENDING;
 		//TODO add tentative
 		default:
 			return Type.UNKNOWN;
 		}
 	}
 	
 	@Override
 	public int compareTo(CalendarAttendee other) {
 		//TODO might need to add email fallback
 		return this.getName().compareToIgnoreCase(other.getName());
 	}
 	
 }
