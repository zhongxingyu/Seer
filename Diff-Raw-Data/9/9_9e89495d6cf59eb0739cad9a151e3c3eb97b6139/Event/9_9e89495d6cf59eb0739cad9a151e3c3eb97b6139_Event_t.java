 package fi.helsinki.cs.oato;
 
 import java.util.Date;
 
 /**
  * Represents an event that occurs at specific location and time.
  */
 public class Event implements Comparable<Event> {
     private Date startDate;
     private Date endDate;
     private String description;
     private String location;
 
     /**
      * Construct a new <code>Event</code> with specified details.
      *
      * @param startDate the beginning date and time
      * @param endDate the ending date and time
      * @param description the description
      * @param location the location
      */
     public Event(Date startDate, Date endDate, String description,
                  String location) {
 
         this.startDate   = startDate;
         this.endDate     = endDate;
         this.description = description;
         this.location    = location;
     }
 
     /**
      * Get event start date.
      * @return the start date
      */
     public Date getStartDate() {
         return startDate;
     }
 
      /**
      * Set event start date.
      * @param startDate the start date
      */
     public void setStartDate(Date startDate) {
         this.startDate = startDate;
     }
 
      /**
      * Get event end date.
      * @return the end date
      */
     public Date getEndDate() {
         return endDate;
     }
 
      /**
      * Set event end date.
      * @param endDate the end date
      */
     public void setEndDate(Date endDate) {
         this.endDate = endDate;
     }
 
     /**
      * Get event description.
      * @return the description
      */
     public String getDescription() {
         return description;
     }
 
     /**
      * Set event description.
      * @param description the description
      */
     public void setDescription(String description) {
         this.description = description;
     }
 
     /**
      * Get event location.
      * @return the location
      */
     public String getLocation() {
         return location;
     }
 
     /**
      * Set event location.
      * @param location the location
      */
     public void setLocation(String location) {
         this.location = location;
     }
 
     /**
      * Compares this event to given event.
      *
      * @param another the other event
      * @return <code>-1</code> if this event starts earlier than the other;
      *         <code>0</code> if the events start at the same time;
      *         <code>1</code> if this event starts later than the other
      */
     public int compareTo(Event another) {
         if (this.equals(another)) {
             return 0;
         } else {
            return this.getStartDate().compareTo(another.getStartDate());
         }
     }
     
     public boolean equals(Object another) {
         if (!(another instanceof Event)) {
             return false;
         }
 
         if (another == this) {
             return true;
         } else {
             Event other = (Event)another;
             
             boolean equals = this.getDescription().equals(other.getDescription());
             equals = equals && this.getLocation().equals(other.getLocation());
             equals = equals && this.getStartDate().equals(other.getStartDate());
             equals = equals && this.getEndDate().equals(other.getEndDate());
 
             return equals;
         }
     }
 }
