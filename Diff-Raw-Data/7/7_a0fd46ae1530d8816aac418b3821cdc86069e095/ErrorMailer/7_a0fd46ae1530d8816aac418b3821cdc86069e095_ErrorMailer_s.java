 /**
  * @author maklemenz
  */
 package controllers.hermes;
 
 import helper.hermes.SysInfo;
 import jobs.hermes.AsyncErrorMailer;
 import play.mvc.Catch;
 import play.mvc.Controller;
 import play.mvc.Scope.Session;
 
 public class ErrorMailer extends Controller {
 
 	@Catch(Exception.class)
 	public static void sendErrorMail(Throwable t) {
 		SysInfo sysInfo = new SysInfo();
 		new AsyncErrorMailer(request, params, t, renderArgs, response, Session.current(), sysInfo).now();
 	}
 
 }
