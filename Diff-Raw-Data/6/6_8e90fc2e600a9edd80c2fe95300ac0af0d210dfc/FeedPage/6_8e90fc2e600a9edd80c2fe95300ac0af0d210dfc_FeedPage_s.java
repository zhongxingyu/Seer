 package org.CommunityService.ManagedBeans;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.ManagedProperty;
 import javax.faces.bean.RequestScoped;
 
 import org.CommunityService.EntitiesMapped.Event;
 import org.CommunityService.EntitiesMapped.EventVolunteer;
 import org.CommunityService.EntitiesMapped.Volunteer;
 
 @ManagedBean
 @RequestScoped
 public class FeedPage {
 	@ManagedProperty(value = "#{currentVolunteerBean}")
 	private CurrentVolunteerBean currentVolunteer;
 	
 	List<Event> volunteerEvents;
 	
	public FeedPage() {
 		Volunteer volunteer = currentVolunteer.getVolunteer();
 		Set<EventVolunteer> eventVolunteers = volunteer.getEventVolunteers();
 		Map<EventVolunteer, Event> eventVolunteersMap = new HashMap<>();
 		for (Iterator<EventVolunteer> iterator = eventVolunteers.iterator(); iterator.hasNext();) {
 			eventVolunteersMap.put((EventVolunteer) iterator, ((EventVolunteer) iterator).getEvent());
 		}
 		volunteerEvents = new ArrayList<Event>(eventVolunteersMap.values());
	}

	public List<Event> getVolunteerEvents() {
 		return volunteerEvents;
 	}
 
 	public void setVolunteerEvents(List<Event> volunteerEvents) {
 		this.volunteerEvents = volunteerEvents;
 	}
 
 	public CurrentVolunteerBean getCurrentVolunteer() {
 		return currentVolunteer;
 	}
 
 	public void setCurrentVolunteer(CurrentVolunteerBean currentVolunteer) {
 		this.currentVolunteer = currentVolunteer;
 	}
 }
 
