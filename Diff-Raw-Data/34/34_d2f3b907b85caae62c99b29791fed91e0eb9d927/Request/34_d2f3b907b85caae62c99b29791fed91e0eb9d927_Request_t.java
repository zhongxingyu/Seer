 package models;
 
 import java.util.List;
 
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.ManyToOne;
 
 import org.joda.time.DateTime;
 import org.joda.time.format.DateTimeFormat;
 import org.joda.time.format.DateTimeFormatter;
 
 import play.data.validation.Constraints.Required;
 import play.db.ebean.Model;
 import controllers.Application;
 
 
 /**
  * Represents a request for a tutoring session
  */
 @Entity
 public class Request extends Model {
 
   private static final long serialVersionUID = -8504063147212754838L;
 
   @Id
   @GeneratedValue
   public Long id;
 
   public static final String ROOT_URL = "http://localhost:9000";
 
   // The student requesting this session
   @ManyToOne
   private Student requestingStudent;
 
   // The tutor this request is being made of
   @ManyToOne
   private Tutor requestedTutor;
 
   // The requested start time
   @Required
   private long requestedStartTime;
 
   // The requested end time
   @Required
   private long requestedEndTime;
 
   public Request(Student requestingStudent, Tutor requestedTutor,
       long requestedStartTime, long requestedEndTime) {
     this.requestingStudent = requestingStudent;
     this.requestedTutor = requestedTutor;
     this.requestedStartTime = requestedStartTime;
     this.requestedEndTime = requestedEndTime;
     Request.create(this);
   }
 
   public static Finder<Long, Request> find = new Finder<Long, Request>(
       Long.class, Request.class);
 
   /**
    * @return the list of all requests
    */
   public static List<Request> all() {
     return find.all();
   }
 
   public static void create(Request request) {
     request.save();
   }
 
   public static void delete(Long id) {
     find.ref(id).delete();
   }
 
   /**
    * @return the requestinStudent
    */
   public Student getRequestingStudent() {
     return requestingStudent;
   }
 
   /**
    * @param requestinStudent the requestinStudent to set
    */
   public void setRequestingStudent(Student requestingStudent) {
     this.requestingStudent = requestingStudent;
   }
 
   /**
    * @return the requestedTutor
    */
   public Tutor getRequestedTutor() {
     return requestedTutor;
   }
 
   /**
    * @param requestedTutor the requestedTutor to set
    */
   public void setRequestedTutor(Tutor requestedTutor) {
     this.requestedTutor = requestedTutor;
   }
  /**
   * Is the request valid
   * 
   * @return True if valid and false if not
   */
  	public boolean isValid(){
  		//Check if request is not for a session in the past
  		//Retrieve all session information and check if it overlaps with any session
  		//return true if not overlapping with another session 
  	}
   /**
    * @return the startTime
    */
   public long getStartTime() {
     return requestedStartTime;
   }
 
   /**
    * @param startTime the startTime to set
    */
   public void setStartTime(long startTime) {
     this.requestedStartTime = startTime;
   }
 
   /**
    * @return the endTime
    */
   public long getEndTime() {
     return requestedEndTime;
   }
 
   /**
    * @param endTime the endTime to set
    */
   public void setEndTime(long endTime) {
     this.requestedEndTime = endTime;
   }
 
   /**
    * 
    */
   public DateTimeFormatter getDateTimeFormatter() {
     return DateTimeFormat.forPattern("MMM dd, YYYY @ hh:mm a");
   }
 
   /**
    * Notifies a student and tutor that a request has been created
    */
   public void sendRequestNotifications() {
     String emailSubject = "Tutor.me Mailer Test";
     DateTime startTimeDate = new DateTime(requestedStartTime);
     String startTimeString = getDateTimeFormatter().print(startTimeDate);
     DateTime endTimeDate = new DateTime(requestedEndTime);
     String endTimeString = getDateTimeFormatter().print(endTimeDate);
 
     // Send Student email
     String studentRecipient = ""
       + requestingStudent.getEmail() + "";
     String studentHtml =
       "Hi "
       + requestedTutor.getName()
       + ",<br /><br />You have create a new tutoring request for "
       + requestedTutor.getUsername()
       + " from "
       + startTimeString
       + " to "
       + endTimeString
       + ".  To cancel it click the at <a href=\""
       + ROOT_URL
       + controllers.routes.Students.cancelRequest(id)
       + "\">"
       + controllers.routes.Students.cancelRequest(id)
       + "<br /><br />Cheers,<br />The Tutor.me team";
     Application.sendEmail(emailSubject, studentRecipient, studentHtml);
 
     // Send tutor email
     String tutorRecipient = ""
       + requestedTutor.getEmail() + "";
     String tutorHtml =
       "Hi "
       + requestedTutor.getName()
       + ",<br /><br />You have a new tutoring request from "
       + requestingStudent.getUsername()
       + " for "
       + startTimeString
       + " to "
       + endTimeString
       +	".  To accept it, click the link at <a href=\""
       + ROOT_URL
       + controllers.routes.Tutors.respondToRequest(id, true)
       + "\">"
       + ROOT_URL
       + controllers.routes.Tutors.respondToRequest(id, true)
       + "</a> or to reject click the link at <a href=\""
       + ROOT_URL
       + controllers.routes.Tutors.respondToRequest(id, false)
       + "\">."
       + controllers.routes.Tutors.respondToRequest(id, false)
       + "<br /><br />Cheers,<br />The Tutor.me team";
     Application.sendEmail(emailSubject, tutorRecipient, tutorHtml);
   }
 
   /**
    * Notifies a student or tutor of a canceled request
    * 
    * @param student: True if the recipient is a student, false otherwise
    */
   public void sendCancellationNotification(boolean student) {
     String emailSubject = "Cancelled Tutor Request";
     String recipientName;
     String initiatorName;
     String email;
     if (student) {
       recipientName = requestingStudent.getUsername();
       initiatorName = requestedTutor.getUsername();
       email = requestingStudent.getEmail();
     } else {
       recipientName = requestedTutor.getUsername();
       initiatorName = requestingStudent.getUsername();
       email = requestedTutor.getEmail();
     }
     String emailRecipient = recipientName + "<" + email + ">";
     DateTime startTimeDate = new DateTime(requestedStartTime);
     String startTimeString = getDateTimeFormatter().print(startTimeDate);
     DateTime endTimeDate = new DateTime(requestedEndTime);
     String endTimeString = getDateTimeFormatter().print(endTimeDate);
     String emailHtml =
       "Hi, "
       + recipientName
       + "<br /><br />Your tutoring request from "
       + startTimeString
       + " to "
       + endTimeString
       + " with "
       + initiatorName
       + " has been cancelled at their request.";
     Application.sendEmail(emailSubject, emailRecipient, emailHtml);
   }
 
   /**
    * Notifies a student and tutor of an upcoming sesssion
    * 
    * @param session: The upcoming session
    */
   public void sendSessionNotifications(TMSession session) {
     String sessionNotificationSubject = "New Tutor.me Session";
     Tutor sessionTutor = session.getTutor();
     String tutorRecipient = sessionTutor.getName() + " <"
     + sessionTutor.getEmail() + ">";
     Student sessionStudent = session.getStudent();
     String studentRecipient = sessionStudent.getName() + " <"
     + sessionStudent.getEmail() + ">";
     DateTime startTimeDate = new DateTime(session.getStartTime());
     String startTimeString = getDateTimeFormatter().print(startTimeDate);
     DateTime endTimeDate = new DateTime(session.getEndTime());
     String endTimeString = getDateTimeFormatter().print(endTimeDate);
     String emailText =
       "  You have been signed up for a Tutor.Me session! <br/>"
       + "  Your session is from "
       + startTimeString
       + " to "
       + endTimeString
       + "<br/>"
       + "  Please use the following link to access your session at the specified time: ";
     String tutorLink = ROOT_URL + "/classroom/" + session.getScribblarId() + "/" + sessionTutor.getScribblarId();
     String studentLink = ROOT_URL + "/classroom/" + session.getScribblarId() + "/" + sessionStudent.getScribblarId();
     String tutorEmailHtml = "<hmtl>" + emailText + "<br/>" + tutorLink
     + "</hmtl>";
     String studentEmailHtml = "<hmtl>" + emailText + "<br/>" + studentLink
     + "</hmtl>";
     Application.sendEmail(sessionNotificationSubject, tutorRecipient,
         tutorEmailHtml);
     Application.sendEmail(sessionNotificationSubject, studentRecipient,
         studentEmailHtml);
   }
 
   public TMSession generateSession() {
     TMSession session = new TMSession(requestingStudent, requestedTutor,
         requestedStartTime, requestedEndTime);
     return session;
   }
 
   @Override
   public boolean equals(Object obj) {
     if (obj instanceof Request) {
       Request other = (Request) obj;
       return this.getRequestingStudent().equals(other.getRequestingStudent())
           && this.getRequestedTutor().equals(other.getRequestedTutor())
           && this.getStartTime() == other.getStartTime()
           && this.getEndTime() == other.getEndTime();
     } else {
       return false;
     }
   }
 }
