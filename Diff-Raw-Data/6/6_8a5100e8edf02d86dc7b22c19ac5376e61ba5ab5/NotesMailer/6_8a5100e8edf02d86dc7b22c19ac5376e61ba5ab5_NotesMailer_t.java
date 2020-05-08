 package controllers;
 
 import com.mysql.jdbc.Messages;
 
 import models.Note;
 import models.Receiver;
 import play.Play;
 import play.i18n.Lang;
 import play.mvc.Mailer;
 import play.mvc.Router;
 
 public class NotesMailer extends Mailer {
     public static void arrivalNotification(Note note, Receiver receiver) {
 	Lang.change(note.sender.language);
 	setFrom(Play.configuration.getProperty("mail.from"));
	setSubject(Messages.get("mail.arrival.subject"));
 	setReplyTo(note.sender.email);
 	addRecipient(receiver.email);
 	String loginUrl = Play.configuration.getProperty("baseUrl");
 	if (receiver.email.endsWith("@gmail.com")
 		|| receiver.email.endsWith("@googlemail.com")
 		|| receiver.email.endsWith("@mail.google.com")) {
 	    loginUrl += Router.reverse("auth.GoogleAuth.signInWithGoogle").url;
 	} else {
 	    loginUrl += Router.reverse("auth.Auth.login").url;
 	}
 	send(note, loginUrl);
     }
}
