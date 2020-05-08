 package controllers.user;
 
 import com.avaje.ebean.Ebean;
 import controllers.EController;
 import controllers.util.InputChecker;
 import controllers.util.PasswordHasher;
 import models.EMessages;
 import models.data.Link;
 import models.dbentities.UserModel;
 import models.user.AuthenticationManager;
 import models.user.Gender;
 import models.user.Role;
 import play.data.DynamicForm;
 import play.data.validation.Constraints.Required;
 import play.mvc.Content;
 import play.mvc.Result;
 import play.mvc.Results;
 import views.html.commons.noaccess;
 import views.html.user.settings;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 
 /**
  * Code for Bebras Application by AVANK
  * User: thomas
  */
 public class PersonalPageController extends EController {
 
     public static boolean isAuthorized() {
         return AuthenticationManager.getInstance().getUser().hasRole(Role.SETTINGS);
     }
 
     public static Result show(int tab) {
         ArrayList<Link> breadcrumbs = new ArrayList<Link>();
         Content showpage;
         breadcrumbs.add(new Link(EMessages.get("app.home"), "/"));
         breadcrumbs.add(new Link(EMessages.get("user.pip.settings"), "/settings"));
         if (isAuthorized()) {
             showpage = settings.render(EMessages.get("user.pip.personalinfo"), breadcrumbs, tab);
         } else {
             showpage = noaccess.render(breadcrumbs);
         }
         return ok(showpage);
     }
 
     public static Result changeInformation() {
         boolean error = false;
         Date bd = new Date();
         UserModel userModel = Ebean.find(UserModel.class).where().eq(
                 "id", AuthenticationManager.getInstance().getUser().getID()).findUnique();
 
         // get new given information about user and save in data
         DynamicForm editInfo = form().bindFromRequest();
 
         // name
         if (!editInfo.get("fname").equals("")) {
             userModel.name = editInfo.get("fname");
             AuthenticationManager.getInstance().getUser().data.name = editInfo.get("fname");
         } else {
             flash("error", EMessages.get(EMessages.get("error.no_input_fname")));
             return Results.redirect(controllers.user.routes.PersonalPageController.show(1));
         }
 
         // email
         if (!InputChecker.getInstance().isCorrectEmail(editInfo.get("email"))) {
             flash("error", EMessages.get(EMessages.get("user.error.wrong_email")));
             return Results.redirect(controllers.user.routes.PersonalPageController.show(1));
         } else {
             userModel.email = editInfo.get("email");
             AuthenticationManager.getInstance().getUser().data.email = editInfo.get("email");
         }
 
         // bday
         if (!editInfo.get("bday").equals("")) {
             try {
                 bd = new SimpleDateFormat("MM/dd/yyyy").parse(editInfo.get("bday"));
                 Date currentDate = new Date();
                 if (bd.after(currentDate)) {
                     flash("error", EMessages.get(EMessages.get("error.wrong_date_time")));
                     return Results.redirect(controllers.user.routes.PersonalPageController.show(1));
                 }
             } catch (ParseException e) {
                 flash("error", EMessages.get(EMessages.get("error.date")));
                 return Results.redirect(controllers.user.routes.PersonalPageController.show(1));
             }
             userModel.birthdate = bd;
             AuthenticationManager.getInstance().getUser().data.birthdate = bd;
         } else {
             flash("error", EMessages.get(EMessages.get("error.date")));
             return Results.redirect(controllers.user.routes.PersonalPageController.show(1));
         }
 
         // gender
         AuthenticationManager.getInstance().getUser().data.preflanguage = editInfo.get("prefLanguage");
         Gender gen = Gender.Female;
         if (editInfo.get("gender").equals("Male")) {
             gen = Gender.Male;
         } else if (editInfo.get("gender").equals("Other")) {
             gen = Gender.Other;
         }
         AuthenticationManager.getInstance().getUser().data.gender = gen;
 
         // language
         userModel.preflanguage = editInfo.get("prefLanguage");
         EMessages.setLang(editInfo.get("prefLanguage"));
         userModel.gender = gen;
 
         // save new information in db
         Ebean.save(userModel);
 
         // success
         flash("success", EMessages.get(EMessages.get("info.successedit")));
         return Results.redirect(controllers.user.routes.PersonalPageController.show(1));
     }
 
     // returns a date in a better readable string
     public static String dateToString(Date dt) {
         Calendar cal = new GregorianCalendar();
         String newdate = new String();
         cal.setTime(dt);
         newdate = newdate + Integer.toString(cal.get(Calendar.MONTH) + 1) + "/" + Integer.toString(cal.get(Calendar.DATE))
                 + "/" + Integer.toString(cal.get(Calendar.YEAR));
         return newdate;
     }
 
     public static Result changePassword() throws Exception {
         ArrayList<Link> breadcrumbs = new ArrayList<>();
         breadcrumbs.add(new Link("Home", "/"));
         breadcrumbs.add(new Link(EMessages.get("edit_pwd.edit_pwd"), "/passwedit"));
 
         DynamicForm editPass = form().bindFromRequest();
         UserModel userModel = Ebean.find(UserModel.class).where().eq(
                 "id", AuthenticationManager.getInstance().getUser().getID()).findUnique();
 
         System.out.println(userModel.id);
 
         //TODO: check current_pwd
 
         System.out.println(userModel.password);
        PasswordHasher.SaltAndPassword sap = PasswordHasher.fullyHash(editPass.get("current_pwd"));
         System.out.println(sap.password);
         System.out.println(userModel.password.equals(sap.password));
 
         if (userModel == null || !editPass.get("n_password").equals(editPass.get("controle_password"))) {
             return ok(noaccess.render(breadcrumbs));
         }
         if (editPass.hasErrors()) {
             flash("error", EMessages.get(EMessages.get("forms.error")));
             return Results.redirect(controllers.user.routes.PersonalPageController.show(2));
         }
        PasswordHasher.SaltAndPassword sp = PasswordHasher.fullyHash(editPass.get("n_password"));
         String passwordHEX = sp.password;
         String saltHEX = sp.salt;
 
         userModel.password = passwordHEX;
         userModel.hash = saltHEX;
 
         userModel.save();
 
         flash("success", EMessages.get("edit_pwd.success"));
         return Results.redirect(controllers.user.routes.PersonalPageController.show(2));
     }
 }
