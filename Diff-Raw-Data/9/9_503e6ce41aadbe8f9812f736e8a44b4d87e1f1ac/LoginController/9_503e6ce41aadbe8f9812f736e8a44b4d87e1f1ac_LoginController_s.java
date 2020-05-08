 package controllers;
 
 import play.*;
 import play.mvc.*;
 
 import views.html.*;
 import views.html.defaultpages.error;
 
 import play.data.*;
 import static play.mvc.Controller.session;
 
 /**
  * Login controller class.
  * @author Stanislav Nepochatov <spoilt.exile@gmail.com>
  */
 public class LoginController extends Controller {
     
     /**
      * Remote login error display string.
      */
     private static String errorLogin = null;
 
     public static Result index() {
         if (!MiniGate.isGateReady) {
             return ok(ribbon_error.render(MiniGate.gateErrorStr));
         } else {
             if (session("connected") != null) {
                 return redirect(routes.SimpleReleaseContoller.index());
             } else {
                return ok(login.render("Вхід до системи...", null));
             }
         }
     }
     
     public static Result index_with_error(String error) {
         if (!MiniGate.isGateReady) {
             return ok(ribbon_error.render(MiniGate.gateErrorStr));
         } else {
            return ok(login.render("Вхід до системи...", error));
         }
     }
     
     public static Result login() {
     	models.Session newSession = Form.form(models.Session.class).bindFromRequest().get();
         String loginErr = MiniGate.gate.sendCommandWithCheck("RIBBON_NCTL_REM_LOGIN:{" + newSession.username + "}," + MiniGate.getHash(newSession.password));
         if (loginErr == null) {
             session("connected", newSession.username);
             return redirect(routes.SimpleReleaseContoller.index());
         } else {
            return redirect(routes.LoginController.index_with_error(loginErr));
         }
     }
     
     public static Result logout() {
         session().remove("connected");
         return redirect(routes.LoginController.index());
     }
   
 }
