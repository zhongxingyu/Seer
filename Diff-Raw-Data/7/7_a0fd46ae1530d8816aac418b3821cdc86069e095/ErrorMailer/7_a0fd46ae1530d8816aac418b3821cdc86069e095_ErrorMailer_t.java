 /**
  * @author maklemenz
  */
 package controllers.hermes;
 
 import helper.hermes.SysInfo;
 import jobs.hermes.AsyncErrorMailer;
import play.Invoker.Suspend;
 import play.mvc.Catch;
 import play.mvc.Controller;
 import play.mvc.Scope.Session;
 
 public class ErrorMailer extends Controller {
 
 	@Catch(Exception.class)
 	public static void sendErrorMail(Throwable t) {
		if(t instanceof Suspend) {
			// thanks to Israel Tsadok. Could leed to blacklisting or performance issues on the mailserver.
		    // see https://github.com/maklemenz/errorMailer/issues/2
			return;
		}
 		SysInfo sysInfo = new SysInfo();
 		new AsyncErrorMailer(request, params, t, renderArgs, response, Session.current(), sysInfo).now();
 	}
 
 }
