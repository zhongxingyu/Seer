 package model;
 
 import java.util.ArrayList;
 import java.util.Date;
 
 
 public class Meeting extends Appointment{
 
 	private ArrayList<User> participants;
	private Date dateOfMeeting;
 	private User leader;
 	private Answer answer;
 	private MeetingRoom room;
 	private String description;
 
 
 	/**
 	 * Creates new meeting
 	 * 
 	 * @param participants
 	 * @param room
 	 * @param dateOfMeeting
 	 * @param description
 	 * @param leader
 	 */
 
 	public Meeting(ArrayList<User> participants, MeetingRoom room, Date dateOfMeeting, String description, User leader,
 			int start, int end){
 		super(dateOfMeeting, description, start, end, leader);
 		this.participants = participants;
 		this.room = room;
 	}
 
 
 	public ArrayList<User> getParticipants() {
 		return participants;
 	}
 
 
 	public void setParticipants(ArrayList<User> participants) {
 		this.participants = participants;
 	}
 
 
	public Date getDateOfMeeting() {
		return dateOfMeeting;
 	}
 
 
	public void setDateOfMeeting(Date dateOfMeeting) {
		this.dateOfMeeting = dateOfMeeting;
 	}
 
 
 	public User getLeader() {
 		return leader;
 	}
 
 
 	public void setLeader(User leader) {
 		this.leader = leader;
 	}
 
 
 	public Answer getAnswer() {
 		return answer;
 	}
 
 
 	public void setAnswer(Answer answer) {
 		this.answer = answer;
 	}
 
 
 	public MeetingRoom getRoom() {
 		return room;
 	}
 
 
 	public void setRoom(MeetingRoom room) {
 		this.room = room;
 	}
 
 
 	public String getDescription() {
 		return description;
 	}
 
 
 	public void setDescription(String description) {
 		this.description = description;
 	}
 
 }
