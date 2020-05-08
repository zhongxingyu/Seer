 package models;
 
 import java.util.List;
 
 import javax.persistence.Entity;
 
 import com.avaje.ebean.Query;
import java.util.Date;
 
 /**
  * Represents a Tutor.me student
  */
 @Entity
 public class Student extends User {
 
   protected static final String requestSelfFieldName = "requestingStudent";
   protected static final String sessionSelfFieldName = "student";
 
   private static final long serialVersionUID = -6347603101193360297L;
 
   public static Finder<Long, Student> find = new Finder<Long, Student>(
       Long.class, Student.class);
 
   public static List<Student> all() {
     return find.all();
   }
 
   public static void create(Student student) {
     student.save();
   }
 
   public static void delete(Long id) {
     find.ref(id).delete();
   }
 
   /**
    * Validates a student
    * @param identifier: The student's email or username
    * @param password: The student's password
    * @return: True if the validation was succesful, false otherwise
    */
   public static boolean authenticate(String identifier, String password){
     Student student = findStudent(identifier);
     if(encrypt(password,student.getSalt()).equals(student.getPwhash())){
       return true;
     }
     return false;
   }
 
   /**
    * Finds the student corresponding to the given identifier
    * @param identifier: The student's email or username
    * @return: The student corresponding to the given identifier
    */
   public static Student findStudent(String identifier) {
     Student matchingUser = find.where().eq("email",identifier).findUnique();
     if(matchingUser==null){
       matchingUser = find.where().eq("username",identifier).findUnique();
       if(matchingUser==null){
         return null;
       }
       else{
         return matchingUser;
       }
     }
     else{
       return matchingUser;
     }
   }
 
   /**
    * Checks if student exists in database
    * 
    * @param username: The potential username for the Student
    * @param email: The potential email for the Student
    */
   public static boolean existsStudent(String username, String email){
     Student matchingStudent = find.where()
     .or(com.avaje.ebean.Expr.eq("username",username),
         com.avaje.ebean.Expr.eq("email", email)).findUnique();
     if(matchingStudent!=null){
       return true;
     }
     else{
       return false;
     }
   }
 
   /**
    * Creates a new tutoring session request
    * 
    * @param tutor: The tutor that is being request to hold the session
    * @param startTime: The start time of the tutoring session
    * @param endTime: The end time of the tutoring session
    * @return: The request that was created
    */
   public Request createRequest(Tutor tutor, long startTime, long endTime) {
     Request request = new Request(this, tutor, startTime, endTime);
    //Request must be validated before saved
    //if(!request.isValid()){
    //  return null;
    //}
     request.save();
     request.sendRequestNotifications();
     return request;
   }
 
   /**
    * Cancels a tutoring session request
    * 
    * @param request: The request to be canceled
    */
   public void cancelRequest(Request request) {
     request.sendCancellationNotification(false);
     request.delete();
   }
 
   /**
    * Finds all tutors that teach the specified subject
    * 
    * @param subject: The subject the student is searching for a tutor for
    * 
    * @return: The tutors who teach the given subject
    */
   public static List<Tutor> searchForTutors(String subject) {
     return searchForTutors(subject, 0, Double.MAX_VALUE);
   }
 
   /**
    * Finds all tutors that teach the specified subject
    * 
    * @param subject: The subject the student is searching for a tutor for
    * @param minCost: The minimum cost a tutor can have
    * @param maxCost: The maximum cost a tutor can have
    * @return: The tutors who teach the given subject, ordered by their cost
    */
   public static List<Tutor> searchForTutors(String subject, double minCost,
       double maxCost) {
     return searchForTutors(subject, minCost, maxCost, Double.MAX_VALUE);
   }
 
   /**
    * Finds all tutors that teach the specified subject
    * 
    * @param subject: The subject the student is searching for a tutor for
    * @param minCost: The minimum cost a tutor can have
    * @param maxCost: The maximum cost a tutor can have
    * @param minRating: The minimum rating a tutor can have
    * @return: The tutors who teach the given subject, ordered by their ratings
    */
   public static List<Tutor> searchForTutors(String subject, double minCost, double maxCost, double minRating) {
     Query<Tutor> tutorResults = Tutor.find.where()
     .contains("subjectsCSV", subject).ge("costUSD", new Double(minCost))
     .le("costUSD", new Double(maxCost)).ge("rating", minRating)
     .orderBy("rating");
     return tutorResults.findList();
   }
 
   /**
    * Rates a tutor on their performance
    * 
    * @param tutor: The tutor to rate
    * @param rating: The rating to give the tutor
    */
   /*
    * public static void rateTutor(Tutor tutor, int rating) { int numRaters =
    * tutor.getNumRaters(); int updateNumRaters = numRaters + 1; double
    * ratingAggregate = tutor.getRating() * numRaters;
    * tutor.setRating((ratingAggregate + rating) / updateNumRaters);
    * tutor.setNumRaters(updateNumRaters); }
    */
 }
