 /*
  * Copyright (C) 2013 Project-Phoenix
  * 
  * This file is part of library.
  * 
  * library is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, version 3 of the License.
  * 
  * library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with library.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package de.phoenix.rs.entity;
 
 import java.util.List;
 
 import javax.ws.rs.core.GenericEntity;
 
 import org.joda.time.DateTimeConstants;
 import org.joda.time.LocalDate;
 import org.joda.time.LocalTime;
 import org.joda.time.Period;
 
 import com.sun.jersey.api.client.Client;
 import com.sun.jersey.api.client.ClientResponse;
 import com.sun.jersey.api.client.GenericType;
 import com.sun.jersey.api.client.WebResource;
 
 import de.phoenix.date.Weekday;
 import de.phoenix.rs.EntityUtil;
 import de.phoenix.rs.key.Key;
 import de.phoenix.rs.key.PhoenixEntity;
 
 public class PhoenixDetails implements PhoenixEntity {
 
     /** URI of the details resource */
     public static final String WEB_RESOURCE_ROOT = "details";
 
     /** Sub URI of the details resource to update a single details */
     public static final String WEB_RESOURCE_UPDATE = "update";
 
     /** Sub URI of the details resource to delete a single details */
     public static final String WEB_RESOURCE_DELETE = "delete";
 
     @Key
     private String room;
     @Key
     private Weekday weekday;
 
     @Key
     private LocalTime startTime;
     @Key
     private LocalTime endTime;
 
     @Key
     private Period interval;
 
     @Key
     private LocalDate startDate;
     @Key
     private LocalDate endDate;
 
     /**
      * Constructor for json-transport
      */
     protected PhoenixDetails() {
 
     }
 
     /**
      * Constructor for client/server
      * 
      * @param room
      *            The room where the event is happening
      * @param weekday
      *            The day of the week, defined by the constants of
      *            {@link DateTimeConstants} (Monday-Sunday)
      * @param startTime
      *            The time when the events starts
      * @param interval
      *            The interval of the event. Will be replaced by an enum
      * @param startDate
      *            The start date of the event(before this date, the event does
      *            not exist!)
      * @param endDate
      *            The end date of the event(after this date, the event does not
      *            exist!)
      * @deprecated Use
      *             {@link #PhoenixDetails(String, Weekday, LocalTime, LocalTime, Period, LocalDate, LocalDate)}
      *             using the {@link Weekday} instead of unsafe constants
      */
 
     public PhoenixDetails(String room, int weekday, LocalTime startTime, LocalTime endTime, Period interval, LocalDate startDate, LocalDate endDate) {
         this(room, Weekday.forID(weekday), startTime, endTime, interval, startDate, endDate);
     }
 
     public PhoenixDetails(String room, Weekday weekday, LocalTime startTime, LocalTime endTime, Period interval, LocalDate startDate, LocalDate endDate) {
         this.room = room;
         this.weekday = weekday;
         this.startTime = startTime;
         this.endTime = endTime;
         this.interval = interval;
         this.startDate = startDate;
         this.endDate = endDate;
     }
 
     /**
      * @return The room where the event is happening
      */
     public String getRoom() {
         return room;
     }
 
     /**
      * @return The day of the week
      */
     public Weekday getWeekday() {
         return weekday;
     }
 
     /**
      * @return The start time of the event
      */
     public LocalTime getStartTime() {
         return startTime;
     }
 
     /**
      * @return The end time of the event
      */
     public LocalTime getEndTime() {
         return endTime;
     }
 
     /**
      * @return The interval of the event
      */
     public Period getInterval() {
         return interval;
     }
 
     /**
      * @return The start date of the event(before this date, the event does not
      *         exist!)
      */
     public LocalDate getStartDate() {
         return startDate;
     }
 
     /**
      * @return The end date of the event(after this date, the event does not
      *         exist!)
      */
     public LocalDate getEndDate() {
         return endDate;
     }
 
     /**
      * Generic Type for {@link PhoenixDetails}
      * 
      * @deprecated No longer necessary for sending and receiving lists
      */
 
     private final static GenericType<List<PhoenixDetails>> GENERIC_TYPE = new GenericType<List<PhoenixDetails>>() {
     };
 
     /**
      * Convert a list to an generic entity to send it via JX-RS
      * 
      * @param list
      *            List containing {@link PhoenixDetails}
      * @return Generic Entity to send via JX-RS
      * @deprecated No longer necessary for sending and receiving lists
      */
     public final static GenericEntity<List<PhoenixDetails>> toSendableList(List<PhoenixDetails> list) {
         return new GenericEntity<List<PhoenixDetails>>(list, GENERIC_TYPE.getType());
     }
 
     /**
      * Extract the entity from the response and convert it to a normal list
      * 
      * @param response
      *            Response containg an list from JX-RS
      * @return List containg values as {@link PhoenixDetails}
      * @deprecated No longer necessary for sending and receiving lists. Use
      *             instead {@link EntityUtil#extractEntityList(ClientResponse)}
      */
     public final static List<PhoenixDetails> fromSendableList(ClientResponse response) {
         return response.getEntity(GENERIC_TYPE);
     }
 
     public static WebResource updateResource(Client client, String baseURL) {
         return base(client, baseURL).path(WEB_RESOURCE_UPDATE);
     }
 
     public static WebResource deleteResource(Client client, String baseURL) {
         return base(client, baseURL).path(WEB_RESOURCE_DELETE);
     }
 
     private static WebResource base(Client client, String baseURL) {
         return client.resource(baseURL).path(WEB_RESOURCE_ROOT);
     }
 
     @Override
     public String toString() {
        return String.format("PhoenixDetails={Room=%s;WeekDay=%s;StartDate=%s;EndDate=%s;StartTime=%s;EndTime=%s;Period=%s}", this.getRoom(), this.getWeekday(), this.getStartDate(), this.getEndDate(), this.getStartTime(), this.getEndTime(), this.getInterval());
     }
 }
