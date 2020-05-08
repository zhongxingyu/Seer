 package controllers;
 
 import javax.mail.internet.MimeMessage;
 
 import play.Logger;
 import play.Play;
 import play.libs.F;
 import play.mvc.Action;
 import play.mvc.Controller;
 import play.mvc.Http.Context;
 import play.mvc.Result;
 import play.mvc.SimpleResult;
 import play.mvc.With;
 
 import com.icegreen.greenmail.util.GreenMailUtil;
 import com.play.greenmail.GreenMailPlugin;
 
 public class GreenMail extends Controller {
 
 	public static GreenMailPlugin GREENMAIL_PLUGIN = Play.application().plugin(
 			GreenMailPlugin.class);
 
 	@With(GreenMail.Enabled.class)
 	public static Result list() {
 		return ok(views.html.list.render(GREENMAIL_PLUGIN
 				.getAllMessagesSorted()));
 	}
 
 	@With(GreenMail.Enabled.class)
 	public static Result show(Integer id) {
 		MimeMessage message = GREENMAIL_PLUGIN.getMessage(id);
 		if (message != null) {
 			return ok(GreenMailUtil.getWholeMessage(message));
 		}
 		return redirect(routes.GreenMail.list());
 	}
 
 	@With(GreenMail.Enabled.class)
 	public static Result clear() {
 		try {
 			GREENMAIL_PLUGIN.clearMessages();
 		} catch (Exception ex) {
 			ex.printStackTrace();
 			Logger.error("clear not ok");
 		}
 		return redirect(routes.GreenMail.list());
 	}
 
 	public static Result disabled() {
 		return ok("greenmail plugin is "
 				+ (GREENMAIL_PLUGIN == null ? "disabled" : "enabled"));
 	}
 
 	@With(GreenMail.Enabled.class)
 	public static Result sendTestEmail() {
 		String to = "to-" + GreenMailUtil.random(3) + "@localhost.com";
 		String from = "from-" + GreenMailUtil.random(3) + "@localhost.com";
 		String subject = "subject-" + GreenMailUtil.random(5);
 		String body = GreenMailUtil.random(25);
 		GreenMailUtil.sendTextEmailTest(to, from, subject, body);
 		return redirect(routes.GreenMail.list());
 	}
 
 	public static class Enabled extends Action.Simple {
 		@Override
 		public play.libs.F.Promise<SimpleResult> call(Context ctx)
 				throws Throwable {
 			if (GreenMail.GREENMAIL_PLUGIN != null) {
 				return delegate.call(ctx);
 			}
 			return play.libs.F.Promise.promise(new F.Function0<SimpleResult>() {
 				public SimpleResult apply() throws Throwable {
					return delegate.redirect(routes.GreenMail.disabled());
 				}
 			});
 		}
 	}
 
 }
