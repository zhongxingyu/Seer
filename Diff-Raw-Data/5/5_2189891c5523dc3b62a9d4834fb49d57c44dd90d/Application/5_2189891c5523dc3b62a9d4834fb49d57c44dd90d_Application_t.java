 package controllers;
 
 import static play.data.Form.form;
 
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
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
       return loginResult(requestData, true);
 
       // Student register
     } else if (type == 2) {
       return signUpResult(requestData, true);
 
       // Tutor sign in
     } else if (type == 3) {
       return loginResult(requestData, false);
 
       // Tutor register
     } else if (type == 4) {
       return signUpResult(requestData, false);
     } else {
       return unauthorized("Oops, you are not connected");
     }
   }
 
   /**
    * Decides what page to return when a sign up attempt is made
    * 
    * @param requestData: The form containing the sign up request data
    * @param isStudent: True if attempting to sign up a student, false if trying
    * to sign up as a tutor
    * @return: The page to render for this sign up attempt
    */
   public static Result signUpResult(DynamicForm requestData, boolean isStudent) {
     String username = requestData.get("username");
     String password = requestData.get("password");
     String fullName = requestData.get("fullName");
     String email = requestData.get("email");
 
     String errorMessage = validateUsernameAndPassword(username, password);
     if (errorMessage.isEmpty()) {
       errorMessage = validateNameAndEmail(fullName, email);
       if (errorMessage.isEmpty()) {
         boolean registerResult = isStudent ? studentRegister(username,
             password, fullName, email) : tutorRegister(username, password,
                 fullName, email);
        if (registerResult) {
           return getLoginRedirect(isStudent ? "" : username);
         } else {
           return ok(index.render((isStudent ? "Student" : "Tutor")
               + " already exists"));
         }
       } else {
         return ok(index.render(errorMessage));
       }
     } else {
       return ok(index.render(errorMessage));
     }
   }
 
   /**
    * Decides what page to return when a login attempt is made
    * 
    * @param requestData: The form containing the login request data
    * @param isStudent: True if attempting to sign up a student, false if trying
    * to sign up as a tutor
    * @return: The page to render for this login attempt
    */
   public static Result loginResult(DynamicForm requestData, boolean isStudent) {
 
     String username = requestData.get("username");
     String password = requestData.get("password");
 
     String errorMessage = validateUsernameAndPassword(username, password);
     if (errorMessage.isEmpty()) {
       boolean loginResult = isStudent ? studentLogin(username, password)
           : tutorLogin(username, password);
       if (loginResult) {
         return getLoginRedirect(isStudent ? "" : username);
       } else {
         return ok(index.render((isStudent ? "Student" : "Tutor")
             + " does not exist"));
       }
     } else {
       return ok(index.render(errorMessage));
     }
   }
 
   /**
    * Gets the redirect page for a user login
    * 
    * @param tutorUsername: The username of for the tutor or the empty string if
    * this is a student
    * @return: The page to redirect to when this user logs in
    */
   public static Result getLoginRedirect(String tutorUsername) {
     if (tutorUsername.isEmpty()) {
       return redirect(routes.Search.search());
     } else {
       Query<Tutor> tutorResults = Tutor.find.where()
      .contains("username", tutorUsername).orderBy("rating");
       List<Tutor> tutors = tutorResults.findList();
       Tutor tutor = tutors.get(0);
       return ok(profile.render(tutor, 1));
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
     } else{
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
 
   /**
    * Validates a username and password
    * 
    * @param username: The username to check
    * @param password: The password to check
    * @return: The error message produces or the empty string if no error is
    * caused
    */
   public static String validateUsernameAndPassword(String username,
       String password) {
     if (username.isEmpty()) {
       return "Username cannot be empty";
     } else if (!alphaNumeric(username)) {
       return "Username must be alphanumberic";
     } else if (password.isEmpty()) {
       return "Password cannot be empty";
     } else {
       return "";
     }
   }
 
   /**
    * Validates a name and email
    * 
    * @param name: The name to check
    * @param email: The password to check
    * @return: The error message produces or the empty string if no error is
    * caused
    */
   public static String validateNameAndEmail(String name, String email) {
     if (name.isEmpty()) {
       return "Name cannot be empty";
     } else if (!alphabetic(removeWhitespace(name))) {
       return "Name must be alphabetic";
     } else if (email.isEmpty()) {
       return "Email cannot be empty";
     } else if (!validEmail(email)) {
       return "Please enter a valid email";
     } else {
       return "";
     }
   }
 
   /**
    * Makes sure a string is alphabetic
    * 
    * @param toExamine: The string to check
    * @return: True for an alphabetic string, false otherwise
    */
   public static boolean alphabetic(String toExamine) {
     Pattern pattern = Pattern.compile("^[a-zA-Z]*$");
     Matcher matcher = pattern.matcher(toExamine);
     return matcher.matches();
   }
 
   /**
    * Makes sure a string is alphanumeric
    * 
    * @param toExamine: The string to check
    * @return: True for an alphanumeric string, false otherwise
    */
   public static boolean alphaNumeric(String toExamine) {
     Pattern pattern = Pattern.compile("^[a-zA-Z0-9]*$");
     Matcher matcher = pattern.matcher(toExamine);
     return matcher.matches();
   }
 
   /**
    * Makes sure a string is a valid email
    * @param toExamine: The string to check
    * @return: True for a valid email, false otherwise
    */
   public static boolean validEmail(String toExamine) {
     Pattern pattern = Pattern.compile("^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$");
     Matcher matcher = pattern.matcher(toExamine);
     return matcher.matches();
   }
 
   /**
    * Removes whitespace from a string
    * @param s: The string to remove whitespace from
    * @return The string without whitespace
    */
   public static String removeWhitespace(String s) {
     return s.replaceAll("\\s+","");
   }
 }
