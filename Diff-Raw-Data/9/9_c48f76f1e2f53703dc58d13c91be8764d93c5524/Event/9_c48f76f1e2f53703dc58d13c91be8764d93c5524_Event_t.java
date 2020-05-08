 package com.tuvistavie.meetup.event.model;
 
 import com.tuvistavie.meetup.contact.model.Contact;
 import com.tuvistavie.meetup.contact.model.ContactList;
 import com.tuvistavie.meetup.model.AbstractEntity;
 import com.tuvistavie.meetup.util.DateTimeUtil;
 import com.tuvistavie.meetup.util.Routes;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
import java.io.UnsupportedEncodingException;
 import java.util.List;
 
 /**
  * Created by daniel on 8/31/13.
  */
 public class Event extends AbstractEntity {
     protected String name;
     protected String description;
     protected EventDateCollection eventDateCollection;
     protected ContactList participants;
 
     protected Participant creator;
 
     public Event() {
         super();
         this.remoteUri = Routes.EVENTS.getRoute();
     }
 
     public Event(String name, String description, EventDateCollection eventDateCollection, ContactList participants) {
         this();
         this.name = name;
         this.description = description;
         this.eventDateCollection = eventDateCollection;
         this.participants = participants;
     }
 
     public Event(JSONObject jsonObject) {
         super(jsonObject);
         this.remoteUri = Routes.EVENTS.getRoute();
     }
 
     public Event(int id) {
         super(id);
         this.remoteUri = Routes.EVENTS.getRoute();
     }
 
     @Override
     public void fromJSON(JSONObject jsonObject) {
         try {
             id = jsonObject.getInt("id");
            name = new String(jsonObject.getString("name").getBytes("ISO-8859-1"), "UTF-8");
            description = new String(jsonObject.getString("description").getBytes("ISO-8859-1"), "UTF-8");
             creator = new Participant(jsonObject.getJSONObject("creator"));
             this.eventDateCollection = new EventDateCollection();
             eventDateCollection.fromJSON(jsonObject.getJSONArray("event_dates"));
         } catch (JSONException e) {
             e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
         }
     }
 
     @Override
     public JSONObject toJSONObject() {
         JSONObject jsonEvent = new JSONObject();
         try {
             jsonEvent.put("name", name);
             jsonEvent.put("description", description);
             jsonEvent.put("event_dates_attributes", eventDateCollection.toJSONArray());
             JSONArray users = new JSONArray();
             for(Contact contact: participants.getEntities()) {
                 List<String> emails = contact.getEmails();
                 if(!emails.isEmpty()) {
                     users.put(emails.get(0));
                 }
             }
             jsonEvent.put("users", users);
             return jsonEvent;
         } catch (JSONException e) {
             e.printStackTrace();
             return null;
         }
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
 
 
     public Participant getCreator() {
         return creator;
     }
 
     public String getStartDateString() {
         if(eventDateCollection.getEntities().isEmpty()) {
             return "";
         }
         EventDate e = eventDateCollection.getEntities().get(0);
         return DateTimeUtil.formatDate(eventDateCollection.getEntities().get(0).getStartDateTime());
     }
 
     public String getEndDateString() {
         if(eventDateCollection.getEntities().isEmpty()) {
             return "";
         }
         return DateTimeUtil.formatDate(eventDateCollection.getEntities().get(0).getEndDateTime());
     }
 }
