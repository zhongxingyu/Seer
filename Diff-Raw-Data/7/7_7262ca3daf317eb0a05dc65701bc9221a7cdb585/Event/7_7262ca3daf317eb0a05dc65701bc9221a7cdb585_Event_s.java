 package com.tuvistavie.meetup.event.model;
 
 import com.tuvistavie.meetup.model.AbstractEntity;
 import com.tuvistavie.meetup.util.DateTimeUtil;
 import com.tuvistavie.meetup.util.Routes;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import java.security.cert.PKIXCertPathBuilderResult;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 
 /**
  * Created by daniel on 8/31/13.
  */
 public class Event extends AbstractEntity {
     protected String name;
     protected String description;
     protected ArrayList<EventDate> datePossibilities;
 
     protected Participant creator;
 
     public Event() {
         super();
         this.remoteUri = Routes.EVENTS.getRoute();
     }
 
     public Event(String name, String description, EventDate... dates) {
         this();
         this.name = name;
         this.description = description;
         for(EventDate d: dates) {
             datePossibilities.add(d);
         }
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
         if(datePossibilities == null) {
             datePossibilities = new ArrayList<EventDate>();
         }
         try {
             id = jsonObject.getInt("id");
             name = jsonObject.getString("name");
             description = jsonObject.getString("description");
             creator = new Participant(jsonObject.getJSONObject("creator"));
             JSONArray dates = jsonObject.getJSONArray("event_dates");
             for(int i = 0; i < dates.length(); i++) {
                 datePossibilities.add(new EventDate(dates.getJSONObject(i)));
             }
         } catch (JSONException e) {
             e.printStackTrace();
         }
     }
 
     @Override
     public JSONObject toJSON() {
         JSONObject jsonEvent = new JSONObject();
         JSONArray jsonDates = new JSONArray();
         try {
             jsonEvent.put("name", name);
             jsonEvent.put("description", description);
             for(EventDate date : datePossibilities) {
                 jsonDates.put(date.toJSON());
             }
             jsonEvent.put("event_dates_attributes", jsonDates);
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
 
     public ArrayList<EventDate> getDatePossibilities() {
         return datePossibilities;
     }
 
     public void setDatePossibilities(ArrayList<EventDate> datePossibilities) {
         this.datePossibilities = datePossibilities;
     }
 
     public Participant getCreator() {
         return creator;
     }
 
     public String getStartDateString() {
         if(datePossibilities.isEmpty()) {
             return "";
         }
         return DateTimeUtil.formatDate(datePossibilities.get(0).getStartDateTime());
     }
 
     public String getEndDateString() {
         if(datePossibilities.isEmpty()) {
             return "";
         }
         return DateTimeUtil.formatDate(datePossibilities.get(0).getEndDateTime());
     }
 }
