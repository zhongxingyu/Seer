 package model;
 
 import java.sql.Timestamp;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 
 import com.sun.corba.se.impl.encoding.OSFCodeSetRegistry.Entry;
 
 import net.DBMethods;
 
 import model.HaveCalendar;
 import model.Person;
 import model.Room;
 
 public class Event implements java.io.Serializable {
 	
 	private String name, description, place;
 	private Person createdBy;
 	private int eventId;
 	private Room room;
 	private Timestamp startTime, endTime;
 	private Boolean isActive;
 	private ArrayList<HaveCalendar> attenders;
 	private HashMap<Person, Timestamp> alarms;
 	
 	public Event(int eventId, Person createdBy, Timestamp startTime, Timestamp endTime, String name, 
 	String description, String place, Room room, ArrayList<HaveCalendar> attenders) {
 		this.eventId = eventId;
 		this.createdBy = createdBy;
 		this.startTime = startTime;
 		this.endTime = endTime;
 		this.name = name;
 		this.description = description;
 		this.place = place;
 		this.room = room;
 		this.attenders = attenders;
 
 	}
 	
 	public void setCreatedBy(Person createdBy){
 		this.createdBy = createdBy;
 	}
 	public Person getCreatedBy(){
 		return createdBy;
 	}
 	
 	public int getEventId() {
 		return eventId;
 	}
 
 	public void setEventId(int eventId) {
 		this.eventId = eventId;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	public String getDescription() {
 		return description;
 	}
 
 	public void setDescription(String description) {
 		this.description = description;
 	}
 
 	public String getPlace() {
 		return place;
 	}
 
 	public void setPlace(String place) {
 		this.place = place;
 	}
 
 	public Room getRoom() {
 		return room;
 	}
 
 	public void setRoom(Room room) {
 		this.room = room;
 	}
 
 	public Timestamp getStartTime() {
 		return startTime;
 	}
 
 	public void setStartTime(Timestamp time) {
 		this.startTime = time;
 	}
 	public Timestamp getEndTime(){
 		return endTime;
 	}
 	public void setEndTime(Timestamp time){
 		this.endTime = time;
 	}
 
 	public Boolean getIsActive() {
 		return isActive;
 	}
 
 	public void setIsActive(Boolean isActive) {
 		this.isActive = isActive;
 	}
 	
 	public void setAttenders(ArrayList<HaveCalendar> attenders){
 		this.attenders = attenders;
 	}
 	
 	public ArrayList<HaveCalendar> getAttenders(){
 		return attenders;
 	}
 	
 	public void addAttender(HaveCalendar haveCalendar){
 		if (attenders == null){
 			attenders = new ArrayList<HaveCalendar>();
 		}
 		attenders.add(haveCalendar); 
 	}
 	
 	public void removeAttender(HaveCalendar haveCalendar){
 		attenders.remove(haveCalendar); 
 	}
 	
 	public int getNumberOfAttenders(){
 		return attenders.size();
 	}
 
 	public void setAlarm(Person person, Timestamp alarm) {
 		if (alarms == null){
 			alarms = new HashMap<Person, Timestamp>();
 		}
 		alarms.put(person, alarm);
 	}
 
 	public HashMap<Person, Timestamp> getAlarms() {
 		HashMap<Person, Timestamp> aMap = new HashMap<Person, Timestamp>();
 		
 		for(Person p : alarms.keySet()){
 			
 			aMap.put(p, alarms.get(p));
 		}
 		
 		return aMap;
 	}
 	
 }
