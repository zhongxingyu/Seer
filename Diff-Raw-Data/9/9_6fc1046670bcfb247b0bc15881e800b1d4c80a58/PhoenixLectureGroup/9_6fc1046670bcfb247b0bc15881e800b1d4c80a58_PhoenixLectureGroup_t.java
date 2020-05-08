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
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.ws.rs.core.GenericEntity;
 
 import org.joda.time.DateTimeConstants;
 import org.joda.time.LocalTime;
 
 import com.sun.jersey.api.client.Client;
 import com.sun.jersey.api.client.ClientResponse;
 import com.sun.jersey.api.client.GenericType;
 import com.sun.jersey.api.client.WebResource;
 
 import de.phoenix.date.Weekday;
 import de.phoenix.rs.EntityUtil;
 import de.phoenix.rs.key.Key;
 import de.phoenix.rs.key.PhoenixEntity;
 
 public class PhoenixLectureGroup implements PhoenixEntity {
 
     /** URI of the lecture group resource */
     public static final String WEB_RESOURCE_ROOT = "lectureGroup";
 
     /**
      * SubURI of the lecture group resource to create a lecture group and assign
      * it to a lecture
      * 
      * @param {@link PhoenixLectureGroup} Task to create
      * @deprecated Use the {@link PhoenixLecture#WEB_RESOURCE_ADD_GROUP} instead
      */
     public static final String WEB_RESOURCE_CREATE = "create";
 
     public static final String WEB_RESOURCE_GET = "get";
 
     public static final String WEB_RESOURCE_DELETE = "delete";
 
     @Key
     private String name;
     private int maxMember;
 
     private Weekday submissionDeadlineWeekday;
     private LocalTime submissionDeadlineTime;
 
     private List<PhoenixDetails> details;
 
     @Key
     private PhoenixLecture lecture;
 
     /**
      * Constructor for json-transport
      */
     protected PhoenixLectureGroup() {
 
     }
 
     /**
      * Constructor for client
      * 
      * @param name
      *            The name of the group
      * @param maxMember
      *            The max possible member of the group
      * @param submissionDeadlineWeekday
      *            The weekday of the default submission. Use
      *            {@link DateTimeConstants} Monday-Sunday
      * @param submissionDeadlineTime
      *            The default deadline time for this group to assign submission.
      *            The day of the deadline is defined by
      *            submissionDeadlineWeekyday
      * @param details
      *            Detail information about the lecture group (room, time and how
      *            often)
      * @param lecture
      *            The lecture the group is assigned to
      * @deprecated Use
      *             {@link #PhoenixLectureGroup(String, int, Weekday, LocalTime, List, PhoenixLecture)}
      *             instead using {@link Weekday}
      */
     public PhoenixLectureGroup(String name, int maxMember, int submissionDeadlineWeekday, LocalTime submissionDeadlineTime, List<PhoenixDetails> details, PhoenixLecture lecture) {
         this(name, maxMember, Weekday.forID(submissionDeadlineWeekday), submissionDeadlineTime, details, lecture);
     }
 
     public PhoenixLectureGroup(String name, int maxMember, Weekday submissionDeadlineWeekday, LocalTime submissionDeadlineTime, List<PhoenixDetails> details, PhoenixLecture lecture) {
         this.name = name;
         this.maxMember = maxMember;
         this.submissionDeadlineWeekday = submissionDeadlineWeekday;
         this.submissionDeadlineTime = submissionDeadlineTime;
         this.details = new ArrayList<PhoenixDetails>(details);
         this.lecture = lecture;
     }
 
     /**
      * @return The maximum member count of this group
      */
     public int getMaxMember() {
         return maxMember;
     }
 
     /**
      * @return The group name
      */
     public String getName() {
         return name;
     }
 
     /**
      * @return Returns the default deadline time for this group to assign
      *         submission. The day of the deadline is defined by
      *         {@link #getSubmissionDeadlineWeekday()}
      */
     public LocalTime getSubmissionDeadlineTime() {
         return submissionDeadlineTime;
     }
 
     /**
      * @return The weekday of the default submission.
      */
     public Weekday getSubmissionDeadlineWeekday() {
         return submissionDeadlineWeekday;
     }
 
     /**
      * @return Copy of detail information list about the lecture group (room,
      *         time and how often)
      */
     public List<PhoenixDetails> getDetails() {
         return new ArrayList<PhoenixDetails>(details);
     }
 
     /**
      * @return The assigned lecture of this group
      */
     public PhoenixLecture getLecture() {
         return lecture;
     }
 
     /**
      * Generic Type for {@link PhoenixLectureGroup}
      * 
      * @deprecated No longer necessary for sending and receiving lists
      */
     private final static GenericType<List<PhoenixLectureGroup>> GENERIC_TYPE = new GenericType<List<PhoenixLectureGroup>>() {
     };
 
     /**
      * Convert a list to an generic entity to send it via JX-RS
      * 
      * @param list
      *            List containing {@link PhoenixLectureGroup}
      * @return Generic Entity to send via JX-RS
      * @deprecated No longer necessary for sending and receiving lists
      */
     public final static GenericEntity<List<PhoenixLectureGroup>> toSendableList(List<PhoenixLectureGroup> list) {
         return new GenericEntity<List<PhoenixLectureGroup>>(list, GENERIC_TYPE.getType());
     }
 
     /**
      * Extract the entity from the response and convert it to a normal list
      * 
      * @param response
      *            Response containg an list from JX-RS
      * @return List containg values as {@link PhoenixLectureGroup}
      * @deprecated No longer necessary for sending and receiving lists. Use
      *             instead {@link EntityUtil#extractEntityList(ClientResponse)}
      */
     public final static List<PhoenixLectureGroup> fromSendableList(ClientResponse response) {
         return response.getEntity(GENERIC_TYPE);
     }
 
     public static WebResource getResource(Client client, String baseURL) {
         return base(client, baseURL).path(WEB_RESOURCE_GET);
     }
 
     public static WebResource deleteResource(Client client, String baseURL) {
         return base(client, baseURL).path(WEB_RESOURCE_DELETE);
     }
 
     private static WebResource base(Client client, String baseURL) {
         return client.resource(baseURL).path(WEB_RESOURCE_ROOT);
     }
 
     /**
      * @Key private String name; private int maxMember;
      * 
      *      private int submissionDeadlineWeekday; private LocalTime
      *      submissionDeadlineTime;
      * 
      *      private List<PhoenixDetails> details;
      * @Key private PhoenixLecture lecture;
      */
 
     @Override
     public String toString() {
        return String.format("PhoenixLectureGroup={Name=%s;MaxMember=%d;SubmissionDeadlineWeekday=%s;SubmissionDeadlineTime=%s;Details=%s}", name, maxMember, submissionDeadlineWeekday, submissionDeadlineTime, details);
     }
 }
