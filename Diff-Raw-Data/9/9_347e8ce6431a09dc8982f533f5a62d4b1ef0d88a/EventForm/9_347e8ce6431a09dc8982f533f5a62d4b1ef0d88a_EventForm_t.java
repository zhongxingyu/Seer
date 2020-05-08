 package forms;
 
 import models.*;
import utils.DateUtils;
 
import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.LinkedList;
 import java.util.List;
 
 public final class EventForm extends ModelForm<Event> {
     private EventGroup parent;
     private String title;
     private String comment;
     private Calendar startTime;
     private Calendar endTime;
     private UserProfile profile;
     private List<Resource> resources;
     private EventPriority priority;
     
     public EventForm(Event instance, UserProfile profile) {
         if ( profile == null )
             throw new NullPointerException("Profile cannot be null");
         this.instance = instance;
         this.profile = profile;
     }
 
     public EventForm(UserProfile profile) {
         this(null, profile);
     }
 
     @Override
     public boolean isValid() {
         clean();
 
         if ( parent == null )
             return false;
         if ( comment == null )
             return false;
 
         boolean valid = true;
 
         if ( startTime == null ){
             this.getErrors().appendError("Time", "Start time is invalid.");
             valid = false;
         }
         if ( endTime == null ){
             this.getErrors().appendError("Time", "End time is invalid.");
             valid = false;
         }
 
         if ( title.isEmpty()) {
             this.getErrors().appendError("Title", "Cannot be empty");
         	valid = false;
         }
         if ( startTime != null && endTime != null && !startTime.before(endTime) ) {
             this.getErrors().appendError("General", "End time has to be after start time");
             valid = false;
         }
 
         EventSet events = profile.getEvents().all();
         if(startTime != null && endTime != null ){
             for ( Event event : events ) {
                 if ( event == instance )
                     continue;
                 boolean overlap = false;
                 if(event.isBetween(startTime,endTime))
                     overlap = true;
                 if(event.getStartTime().before(endTime)&&event.getEndTime().after(endTime))
                     overlap = true;
                 if(event.getStartTime().before(startTime)&&event.getEndTime().after(startTime))
                     overlap = true;
                 if ( overlap ) {
                    SimpleDateFormat df = new SimpleDateFormat("dd MMM yyyy HH:mm");
                    this.getErrors().appendError("General", "Event overlaps existing one " + event.getTitle() +  "<br>" +
                            event.getTitle()+" starts: "+ df.format(event.getStartTime().getTime())+
                            " ends: " + df.format(event.getEndTime().getTime()) );
                     valid =  false;
                     break;
                 }
             }
         }
 
         if ( instance != null ) {
             if ( instance.getProfile() != profile )
                 return false;
             if ( instance.getParent() != parent )
                 return false;
         }
 
         return valid;
     }
 
     @SuppressWarnings("unused")
     private void cleanStartTime() {
         if ( startTime == null )
             return;
         startTime.set(Calendar.SECOND, 0);
     }
 
     @SuppressWarnings("unused")
     private void cleanEndTime() {
         if ( endTime == null )
             return;
         endTime.set(Calendar.SECOND, 0);
     }
 
     @Override
     public Event save() throws ValidationException {
         boolean isCreate = instance == null;
         if ( instance == null ) {
             setInstance(new Event(
                     parent,
                     comment,
                     startTime,
                     endTime,
                     profile,
                     priority,
                     resources
             ));
         }
         super.save();
         if ( isCreate ) {
             EventManager manager = profile.getEvents();
             getInstance().getParent().addEvent(getInstance());
             manager.add(getInstance());
         } else {
             instance.setEndTime(endTime);
             instance.setStartTime(startTime);
             instance.setTitle(title);
             instance.setComment(comment);
             instance.setResources(resources);
             instance.setPriority(priority);
         }
         return getInstance();
     }
 
     public void setEndTime(Calendar endTime) {
         this.endTime = endTime;
     }
 
     public void setStartTime(Calendar startTime) {
         this.startTime = startTime;
     }
 
     public void setTitle(String title) {
         this.title = title;
 
     }
 
     public void setComment(String comment) {
         this.comment = comment;
     }
 
     public void setParent(EventGroup parent) {
         this.parent = parent;
     }
 
     public void setResources(List<Resource> resources){
     	this.resources = new LinkedList<>();
     	
     	for(Resource r : resources) {
     		if(r instanceof ResourceFile)
     			this.resources.add(new ResourceFile((ResourceFile)r));
     		else
     			this.resources.add(r);
     	}
     }
     
     public void setPriority(EventPriority priority) {
     	this.priority = priority;
     }
 }
