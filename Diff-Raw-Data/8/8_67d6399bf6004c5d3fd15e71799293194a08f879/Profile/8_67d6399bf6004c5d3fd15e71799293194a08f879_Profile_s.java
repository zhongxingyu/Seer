 package controllers;
 
 import static play.data.Form.form;
 
 import java.io.File;
 import java.util.List;
 
 import models.Student;
 import models.Tutor;
 
 import org.joda.time.DateTime;
 import org.joda.time.format.DateTimeFormat;
 import org.joda.time.format.DateTimeFormatter;
 
 import play.data.DynamicForm;
 import play.mvc.Controller;
 import play.mvc.Http.MultipartFormData;
 import play.mvc.Http.MultipartFormData.FilePart;
 import play.mvc.Result;
 import views.html.profile;
 
 import com.avaje.ebean.Query;
 
 public class Profile extends Controller {
 
   public static boolean isLoggedIn(){
     String user = session("connected");
     if(user==null){
       return false;
     }
     else{
       return true;
     }
   }
 
   public static String loggedUser(){
     String username = session("connected");
     return username;
   }
 
   public static Result viewProfile(String username) {
     if (isLoggedIn()) {
       String localUser = loggedUser();
 
       Query<Tutor> tutorResults  = Tutor.find.where().contains("username", username).orderBy("rating");
       List<Tutor> tutors = tutorResults.findList();
       Tutor tutor = tutors.get(0);
 
       // If the user viewing the profile is the tutor himself, pass in 1 so view knows to show the edit button
       if(localUser.equals(username)){
         return ok(profile.render(tutor, 1));
       } else {
         return ok(profile.render(tutor, 0));
       }
     } else {
       return unauthorized("Oops, you are not connected");
     }
   }
 
 
   public static Result postForm(String username) {
     DynamicForm requestData = form().bindFromRequest();
     double type = Double.parseDouble(requestData.get("type"));
 
     Query<Tutor> tutorResults  = Tutor.find.where().contains("username", username).orderBy("rating");
     List<Tutor> tutors = tutorResults.findList();
     Tutor tutor = tutors.get(0);
 
     // Edit Profile
     if(type == 2){
       String tagline = requestData.get("editTagline");
       String description = requestData.get("editAbout");
      double cost = Double.parseDouble(requestData.get("editPrice"));
       MultipartFormData body = request().body().asMultipartFormData();
       FilePart picture = body.getFile("picture");
       if (picture != null) {
         String filename = picture.getFilename();
         picture.getContentType();
         File file = picture.getFile();
         file.renameTo(new File("assets/images/profilePics", filename));
         tutor.setPicture(filename);
       } else {
         flash("error", "Missing file");
       }
       tutor.setTagline(tagline);
       tutor.setDescription(description);
       tutor.setCostUSD(cost);
 
     } else {
       String startTime = requestData.get("startTime");
       String endTime = requestData.get("endTime");
 
       DateTimeFormatter formatter = DateTimeFormat.forPattern("hh/mm/YYYY hh:mm a");
       DateTime st = formatter.parseDateTime(startTime);
       DateTime et = formatter.parseDateTime(endTime);
 
       long startMillis = st.getMillis();
       long endMillis = et.getMillis();
 
       // Test student that we are currently send the request to
       Student theStudent = Student.find.ref(1L);
       //This create request needs to test wether this request is valid or not.
       //Error would be handled accordingly
       theStudent.createRequest(tutor, startMillis, endMillis);
       //Error would be rendered if an invalid request was submitted
     }
 
     return ok(profile.render(tutor, 1));
   }
 }
