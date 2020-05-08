 package com.damgeek.countdownwidget;
 
 import java.util.Calendar;
 
 public class EventInfo {
 	public int id;
 	public String name;
 	public String logo;
 	public Calendar eventDate;
 	
 	public int getId() {
 		return id;
 	}
 	public void setId(int id) {
 		this.id = id;
 	}
 	public String getName() {
 		return name;
 	}
 	public void setName(String name) {
 		this.name = name;
 	}
 	public String getLogo() {
 		return logo;
 	}
 	public void setLogo(String logo) {
 		this.logo = logo;
 	}
 	public Calendar getEventDate() {
 		return eventDate;
 	}
 	public void setEventDate(Calendar eventDate) {
 		this.eventDate = eventDate;
 	}
 	@Override
 	public String toString() {
 		return name;
 	}
 	public EventInfo(int id, String name, String logo, Calendar eventDate) {
 		super();
 		this.id = id;
 		this.name = name;
 		this.logo = logo;
 		this.eventDate = eventDate;
 	}
 	
 	public EventInfo() {
 		// TODO Auto-generated constructor stub
 	}
 }
