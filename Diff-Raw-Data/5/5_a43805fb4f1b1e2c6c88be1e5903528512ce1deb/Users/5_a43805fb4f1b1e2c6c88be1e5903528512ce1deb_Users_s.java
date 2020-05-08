 package controllers;
 
 import controllers.abstracts.AppController;
 import controllers.filters.UserFirstLoginOnly;
 import controllers.helper.PageHelper;
 import controllers.security.Auth;
 import controllers.security.LoggedAccess;
 import controllers.security.PublicAccess;
 import models.school.Course;
 import models.user.User;
 import org.apache.commons.lang3.StringUtils;
 import play.data.validation.Required;
 import play.mvc.Before;
 import play.mvc.Util;
 import service.CourseService;
 import service.UserService;
 import validation.EnhancedValidator;
 
 import javax.inject.Inject;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * @author Lukasz Piliszczuk <lukasz.piliszczuk AT zenika.com>
  */
 public class Users extends AppController {
 
     @Inject
     private static UserService userService;
 
     @Inject
     private static CourseService courseService;
 
     private static PageHelper pageHelper;
 
     @Before
     public static void before() {
         pageHelper = new PageHelper("users", renderArgs);
     }
 
     @PublicAccess(only = true)
     public static void login() {
 
         pageHelper.uniqueTitle("users.login");
 
         if (flash.contains("user.password")) {
             flash.remove("user.password");
         }
 
         render();
     }
 
     @PublicAccess(only = true)
     public static void authenticate(User user) {
 
         EnhancedValidator validator = validator();
 
         if (validator.validate(user).require("idBooster", "password").hasErrors()) {
             login();
         }
 
         user = userService.getFromLogin(user.idBooster, user.password);
 
         if (null == user) {
             params.flash();
             flashError("users.login.authentication.failure");
             login();
         }
 
         Auth.loginUser(user);
 
         if (!user.active) {
             firstLogin();
         }
 
         Dashboard.index();
     }
 
     @LoggedAccess
     public static void logout() {
         Auth.logoutUser();
         Users.login();
     }
 
     @LoggedAccess
     @UserFirstLoginOnly
     public static void firstLogin() {
         pageHelper.title("Activation de votre compte");
         List<Course> courses = courseService.getCourses();
         render(courses);
     }
 
     @LoggedAccess
     @UserFirstLoginOnly
     public static void completeFirstLogin(User user, @Required String passwordConfirmation, List<Long> skills) {
 
         EnhancedValidator validator = validator();
         validator.validate(user)
                 .requireFields("passwordConfirmation");
 
         requirePersonalInfo(validator);
 
         if (validator.hasErrors()) {
             List<Course> courses = courseService.getCourses();
             render("firstLogin.html", courses, user, skills);
         }
 
         if (!StringUtils.equals(user.password, passwordConfirmation)) {
 
             validator.addError("passwordConfirmation", "users.edit.error.passwordConfirmation", true);
 
             List<Course> courses = courseService.getCourses();
             render("firstLogin.html", courses, user, skills);
         }
 
         List<Course> courses = new ArrayList<Course>();
 
         if (null != skills) {
             for (Long id : skills) {
                 Course course = Course.findById(id);
                 if (null != course) {
                     courses.add(course);
                 }
             }
         }
 
         userService.updateFromFirstLogin(user, courses, Auth.getCurrentUser());
 
         Dashboard.index();
     }
 
     @LoggedAccess
     public static void show(Long id) {
 
     }
 
     @LoggedAccess
     public static void editPersonalInfo() {
 
         pageHelper.title("Modifiez vos informations personnelles");
 
         User user = Auth.getCurrentUser();
         render(user);
     }
 
     @LoggedAccess
     public static void savePersonalInfo(User user) {
 
         EnhancedValidator validator = validator();
         validator.validate(user);
 
         requirePersonalInfo(validator);
 
         if (validator.hasErrors()) {
             render("Users/editPersonalInfo.html", user);
         }
 
         userService.updateFromPersonalInfo(user, Auth.getCurrentUser());
 
         flashSuccess("users.savePersonalInfo.success");
 
         Dashboard.index();
     }
 
     @LoggedAccess
     public static void editPassword() {
 
         pageHelper.title("Modifiez votre mot de passe");
 
         render();
     }
 
     @LoggedAccess
     public static void savePassword(User user, @Required String passwordConfirmation) {
 
         EnhancedValidator validator = validator();
 
         if (validator.validate(user).require("password").hasErrors()) {
            editPassword();
         }
 
         if (!StringUtils.equals(user.password, passwordConfirmation)) {
             validator.addError("passwordConfirmation", "users.edit.error.passwordConfirmation", true).save();
            editPassword();
         }
 
         userService.updateFromPassword(user.password, Auth.getCurrentUser());
 
         flashSuccess("users.savePassword.success");
 
         Dashboard.index();
     }
 
     @LoggedAccess
     public static void editSkills() {
 
     }
 
     @LoggedAccess
     public static void saveSkills(List<Course> skills) {
 
     }
 
     @Util
     private static void requirePersonalInfo(EnhancedValidator validator) {
         validator.require("firstName", "lastName", "street", "postalCode", "city", "siret");
     }
 }
