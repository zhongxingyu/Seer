 package org.sukrupa.event;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.lang.builder.EqualsBuilder;
 import org.apache.commons.lang.builder.HashCodeBuilder;
 import org.apache.commons.lang.builder.ToStringBuilder;
 import org.apache.commons.lang.builder.ToStringStyle;
 import org.hibernate.annotations.Proxy;
 import org.hibernate.annotations.Type;
 import org.springframework.web.util.HtmlUtils;
 import org.sukrupa.platform.RequiredByFramework;
 import org.sukrupa.platform.date.Date;
 import org.sukrupa.student.Student;
 
 import javax.persistence.*;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 @Entity
 @Proxy(lazy = false)
 public class Event {
 
     private static final String[] EXCLUDE_THESE_FIELDS_FROM_EQUALS_HASHCODE = new String[]{"id", "attendees"};
 
     @Id
     @GeneratedValue
     @Column(name = "ID")
     private Integer id;
 
     @Column(name = "TITLE")
     private String title;
 
     @Column(name = "VENUE")
     private String venue;
 
     @Column(name = "COORDINATOR")
     private String coordinator;
 
     @Column(name = "DESCRIPTION")
     private String description;
 
     @Column(name = "NOTES")
     private String notes;
 
     @Type(type = "org.sukrupa.platform.date.PersistentDate")
     @Column(name = "END_DATE")
     private Date endDate;
 
     @ManyToMany
     @JoinTable(name = "EVENT_ATTENDEES",
             joinColumns = @JoinColumn(name = "EVENT_ID"),
             inverseJoinColumns = @JoinColumn(name = "STUDENT_ID"))
     private Set<Student> attendees;
 
     @Type(type = "org.sukrupa.platform.date.PersistentDate")
     @Column(name = "START_DATE")
     private Date startDate;
 
     @RequiredByFramework
     public Event() {
     }
 
 
     public Event(String title, Date endDate, String venue, String coordinator, String description, String notes,
                  Set<Student> attendees, Date startDate) {
         this.title = title;
         this.endDate = endDate;
         this.venue = venue;
         this.coordinator = coordinator;
         this.description = description;
         this.notes = notes;
         this.attendees = attendees;
         this.startDate = startDate;
     }
 
     public Event(String title, Date endDate, String venue, String coordinator, String description, String notes, Date startDate) {
         this(title, endDate, venue, coordinator, description, notes, new HashSet<Student>(), startDate);
     }
 
     public Integer getId() {
         return id;
     }
 
     public String getTitle() {
         return title;
     }
 
     public Date getDate() {
        return endDate;
     }
 
     public String getDay() {
        return endDate.getDay();
     }
 
     public String getEndTime() {
         return endDate.getTime();
     }
 
     public String getEndTimeWithAmPm() {
         String amPm = endDate.isInTheAfternoon() ? "PM" : "AM";
         return String.format("%s %s", endDate.getTime(), amPm);
     }
 
     public void addAttendees(Set<Student> attendees) {
         this.attendees.addAll(attendees);
     }
 
     public Set<Student> getAttendees() {
         return attendees;
     }
 
     public boolean equals(Object other) {
         return EqualsBuilder.reflectionEquals(this, other, EXCLUDE_THESE_FIELDS_FROM_EQUALS_HASHCODE);
     }
 
     public int hashCode() {
         return HashCodeBuilder.reflectionHashCode(this, EXCLUDE_THESE_FIELDS_FROM_EQUALS_HASHCODE);
     }
 
     public String toString() {
         return ToStringBuilder.reflectionToString(this, ToStringStyle.SIMPLE_STYLE);
     }
 
     public String getVenue() {
         return venue;
     }
 
     public String getDescription() {
         return description.trim();
     }
 
     public String getNotes() {
         return notes;
     }
 
     public String getAttendeesForDisplay() {
         return StringUtils.join(getAttendeeNames(), ", ");
     }
 
     public String getAttendeesIdsForDisplay() {
         List<String> attendeeIds = new ArrayList<String>();
         for (Student attendee : attendees) {
             attendeeIds.add(attendee.getName());
         }
 
         return StringUtils.join(attendeeIds, ", ");
     }
 
     private List<String> getAttendeeNames() {
         List<String> attendeeNameList = new ArrayList<String>();
         for (Student attendee : attendees) {
             attendeeNameList.add(attendee.getName());
         }
         return attendeeNameList;
     }
 
     public String getCoordinator() {
         return coordinator;
     }
 
     public void updateFrom(EventForm eventParam, Set<Student> attendees) {
         this.title = eventParam.getTitle();
         this.endDate = Date.parse(eventParam.getDate(), new Time (eventParam.getEndTime(), eventParam.getEndTimeAmPm()));
         this.venue = eventParam.getVenue();
         this.coordinator = eventParam.getCoordinator();
         this.description = eventParam.getDescription();
         this.notes = eventParam.getNotes();
         this.attendees = attendees;
         this.startDate = Date.parse(eventParam.getDate(), new Time (eventParam.getStartTime(), eventParam.getStartTimeAmPm()));
     }
 
     public boolean isEndTimePm() {
         return endDate.isInTheAfternoon();
     }
 
     public String getStartTime() {
         return startDate.getTime();
     }
 
     public String getStartTimeWithAmPm() {
         String amPm = startDate.isInTheAfternoon() ? "PM" : "AM";
         return String.format("%s %s", startDate.getTime(), amPm);
     }
 
     public boolean isStartTimePm() {
         return startDate.isInTheAfternoon();
     }
 }
