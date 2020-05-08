 package controllers;
 
 import static play.data.Form.form;
 
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.List;
 import java.util.regex.Pattern;
 import java.util.regex.Matcher;
 
 import models.Student;
 import models.Tutor;
 import models.User;
 import play.data.DynamicForm;
 import play.data.Form;
 import play.mvc.Controller;
 import play.mvc.Result;
 import views.html.index;
 import views.html.profile;
 import views.html.search;
 
 import com.avaje.ebean.Query;
 import com.typesafe.plugin.MailerAPI;
 import com.typesafe.plugin.MailerPlugin;
 
 public class Application extends Controller {
   Form<Student> studentForm = form(Student.class);
   Form<Tutor> tutorForm = form(Tutor.class);
 
   public static Result loginRoute(){
     DynamicForm requestData = form().bindFromRequest();
     double type = Double.parseDouble(requestData.get("type"));
 
     // Student log in
     if (type == 1) {
       String username = requestData.get("username");
       String password = requestData.get("password");
       if(studentLogin(username, password)){
         List<Tutor> emptyList = Collections.<Tutor>emptyList();
         return ok(search.render(emptyList));
       }else{
        return ok(index.render("Student does not exist"));
       }
 
       // Student register
     } else if (type == 2) {
       String username = requestData.get("username");
       String password = requestData.get("password");
       String fullName = requestData.get("fullName");
       String email = requestData.get("email");
       if(!alphaNumeric(username) || !validEmail(email)){
         return ok(index.render("Welcome"));
       }else{
         if(!studentRegister(username, password, fullName, email)){
           return ok(index.render("Student already exists"));
         }
         List<Tutor> emptyList = Collections.<Tutor>emptyList();
         return ok(search.render(emptyList));
       }
 
       // Tutor sign in
     } else if (type == 3) {
       String username = requestData.get("username");
       String password = requestData.get("password");
       if(tutorLogin(username, password)){
         Query<Tutor> tutorResults  = Tutor.find.where().contains("username", username).orderBy("rating");
         List<Tutor> tutors = tutorResults.findList();
         Tutor tutor = tutors.get(0);
         return ok(profile.render(tutor, 1));
       }else{
        return ok(index.render("Tutor does not exist"));
       }
 
       // Tutor register
     } else if (type == 4) {
       String username = requestData.get("username");
       String password = requestData.get("password");
       String fullName = requestData.get("fullName");
       String email = requestData.get("email");
       if(!alphaNumeric(username) || !validEmail(email)){
         return ok(index.render("Welcome"));
       }else{
         if(!tutorRegister(username, password, fullName, email)){
           return ok(index.render("Tutor already exists"));
         }
         Query<Tutor> tutorResults  = Tutor.find.where().contains("username", username).orderBy("rating");
         List<Tutor> tutors = tutorResults.findList();
         Tutor tutor = tutors.get(0);
         return ok(profile.render(tutor, 1));
       }
     } else {
       return unauthorized("Oops, you are not connected");
     }
   }
 
   /**
    * Check if a user is logged in
    * 
    * @return: True if the user is logged in, false otherwise
    */
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
 
 
   /**
    * @return  index Page
    */
   public static Result index() {
     String user = session("connected");
     if(user != null) {
       //go to the users homepage
       if(Tutor.findTutor(user)!=null){
         //Return Tutor Homepage
         Tutor tutor = Tutor.findTutor(user);
         return ok(profile.render(tutor, 1));
       } else{
         List<Tutor> emptyList = Collections.<Tutor>emptyList();
         return ok(search.render(emptyList));
       }
     } else {
       //show signup or login
       return ok(index.render("Welcome"));
     }
   }
 
   /**
    *
    * @return Tutor Home
    */
   /*
 	 public static Result TutorHome(){
 		 if(!isLoggedIn()){
 			 return redirect("/");
 		 }
 		 //
 		 //return ok(signedInTutorMain.render());
 		 return ok("TutorHOME");
 	 }
 	public static Result StudentHome(){
 		if(!isLoggedIn()){
 			return redirect("/");
 		}
 		String user = session("connected");
 		//
 		return ok("StudentHOME");
 	}
    */
 
   /**
    * Logs a student in
    * 
    * @param username: The username of the student
    * @param password: The password of the student
    */
   public static boolean studentLogin(String username, String password){
     if(Student.authenticate(username, password)){
       session("connected",Student.findStudent(username).getUsername());
       return true;
     }else{
       return false;
     }
   }
 
   /**
    * Logs a tutor in
    * 
    * @param username: The username of the tutor
    * @param password: The password of the tutor
    */
 
   public static boolean tutorLogin(String username, String password){
     if(Tutor.authenticate(username, password)){
       session("connected",Tutor.findTutor(username).getUsername());
       return true;
     }else{
       return false;
     }
   }
 
   /**
    * Registers a new student
    * 
    * @param username: The username of the student
    * @param password: The password of the student
    * @param fullName: The student's full name
    * @param email: The student's email
    */
   public static boolean studentRegister(String username, String password, String fullName, String email) {
     form().bindFromRequest();
     if (Student.existsStudent(username, email)) {
       return false;
     } else {
       Student user = new Student();
       user.setUsername(username);
       user.setEmail(email);
       user.setName(fullName);
       String salt = User.saltGenerate();
       user.setSalt(salt);
       user.setPwhash(User.encrypt(password, salt));
 
       if (user.validate()) {
         Student.create(user);
         session("connected", username);
         index();
       }
       return true;
     }
   }
 
   /**
    * Registers a new tutor
    * 
    * @param username: The username of the tutor
    * @param password: The password of the tutor
    * @param fullName: The tutor's full name
    * @param email: The tutor's email
    */
   public static boolean tutorRegister(String username, String password,
       String fullName, String email) {
     // Validate Data
     if (Tutor.existsTutor(username, email)) {
       return false;
     } else {
       Tutor user = new Tutor();
       user.setUsername(username);
       user.setEmail(email);
       user.setName(fullName);
       String salt = User.saltGenerate();
       user.setSalt(salt);
       user.setPwhash(User.encrypt(password, salt));
       if (user.validate()) {
         Tutor.create(user);
         session("connected", username);
         index();
       }
       return true;
     }
   }
 
   /**
    * Logs a user out
    * 
    * @return mainSignedOutPage
    */
   public static Result logout(){
     session().clear();
     //Go to log out page
     return ok(index.render("Welcome"));
   }
 
   /**
    * Sends an email to the specified recipients
    * 
    * @param emailSubject: The subject of the email
    * @param emailRecipient: The recipient of the email
    * @param emailHtml: The html text contained in the email
    */
   public static void sendEmail(String emailSubject, String emailRecipient, String emailHtml) {
     sendEmail(emailSubject, Arrays.asList(emailRecipient), emailHtml);
   }
 
   /**
    * Sends an email to the specified recipients
    * 
    * @param emailSubject: The subject of the email
    * @param emailRecipients: The recipients of the email
    * @param emailHtml: The html text contained in the email
    */
   public static void sendEmail(String emailSubject,
       List<String> emailRecipients, String emailHtml) {
     MailerAPI mail = play.Play.application().plugin(MailerPlugin.class).email();
     mail.addFrom("Tutor.me Mailing Robot <tutor.me.mailer@gmail.com>");
     mail.setSubject(emailSubject);
     for (String emailRecipient : emailRecipients) {
       mail.addRecipient(emailRecipient);
     }
     mail.sendHtml(emailHtml);
   }
 
   public static boolean alphaNumeric(String toExamine) {
       Pattern pattern = Pattern.compile("^[a-zA-Z0-9]*$");
       Matcher matcher = pattern.matcher(toExamine);
       return matcher.matches();
   }
 
   public static boolean validEmail(String toExamine) {
       Pattern pattern = Pattern.compile("^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$");
       Matcher matcher = pattern.matcher(toExamine);
       return matcher.matches();
   }
 
 }
