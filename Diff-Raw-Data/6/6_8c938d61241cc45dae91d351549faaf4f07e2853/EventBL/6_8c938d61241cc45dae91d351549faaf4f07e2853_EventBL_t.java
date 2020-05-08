 package uk.frequency.glance.server.business;
 
 import java.util.Date;
 import java.util.List;
 
 import org.hibernate.ObjectNotFoundException;
 
 import uk.frequency.glance.server.business.util.GoogleAPIs;
 import uk.frequency.glance.server.data_access.EventDAL;
 import uk.frequency.glance.server.data_access.UserDAL;
 import uk.frequency.glance.server.model.component.Location;
 import uk.frequency.glance.server.model.component.Media;
 import uk.frequency.glance.server.model.component.Media.MediaType;
 import uk.frequency.glance.server.model.component.Position;
 import uk.frequency.glance.server.model.event.Event;
 import uk.frequency.glance.server.model.event.EventScore;
 import uk.frequency.glance.server.model.event.EventType;
 import uk.frequency.glance.server.model.event.StayEvent;
 import uk.frequency.glance.server.model.trace.PositionTrace;
 import uk.frequency.glance.server.model.user.User;
 
 public class EventBL extends GenericBL<Event>{
 
 	EventDAL eventDal;
 	UserDAL userDal;
 
 	public EventBL() {
 		super(new EventDAL());
 		eventDal = (EventDAL)dal;
 		userDal = new UserDAL();
 	}
 	
 	public List<Event> findByAuthor(long userId) throws ObjectNotFoundException {
 		User user = userDal.findById(userId);
 		user.getId(); //make hibernate do the actual select so it can throw ObjectNotFound. TODO find appropriate way to accomplish this. 
 		List<Event> list = eventDal.findByAuthor(userId);
 		return list;
 	}
 	
 	public List<Event> findByTimeRange(Date start, Date end){
 		return eventDal.findByTimeRange(start, end);
 	}
 	
 	public long generateStayEvent(PositionTrace trace){
 		Position pos = trace.getPosition();
 		String imageUrl = GoogleAPIs.getStreetViewImageUrl(pos); //TODO get more from google places and also streetview with different headings, choose best image somehow
		String name = GoogleAPIs.getLocationName(pos); //TODO get from google places
 		String address = GoogleAPIs.getLocationName(pos);
 
 		Location location = new Location();
 		location.setPosition(pos);
 		location.setAddress(address);
		location.setName(name);
 		Media media = new Media();
 		media.setType(MediaType.IMAGE);
 		media.setUrl(imageUrl);
 		StayEvent event = new StayEvent();
 		event.setType(EventType.STAY);
 		event.setCreationTime(new Date()); //TODO set this as default value directly on the database
 		event.setUser(trace.getUser());
 		event.setStartTime(trace.getTime());
 		event.setEndTime(trace.getTime());
 		event.setLocation(location);
 		event.setMedia(media);
 		event.setScore(new EventScore());
 		
 		create(event);
 		
 		return event.getId();
 	}
 	
 }
