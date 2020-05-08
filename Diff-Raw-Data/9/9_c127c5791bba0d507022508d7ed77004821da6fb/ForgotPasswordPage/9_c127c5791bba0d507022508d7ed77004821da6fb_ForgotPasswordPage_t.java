 package co.uk.techmeetup.pages;
 
 import org.apache.commons.mail.EmailException;
 import org.apache.commons.mail.SimpleEmail;
 import org.apache.tapestry5.annotations.InjectComponent;
 import org.apache.tapestry5.annotations.OnEvent;
 import org.apache.tapestry5.annotations.Property;
 import org.apache.tapestry5.corelib.components.Form;
 import org.apache.tapestry5.ioc.annotations.Inject;
 import org.hibernate.Criteria;
 import org.hibernate.Transaction;
 import org.hibernate.criterion.Restrictions;
 
 import co.uk.techmeetup.data.User;
 import co.uk.techmeetup.misc.ControlVars;
 import co.uk.techmeetup.services.PasswordService;
 
 public class ForgotPasswordPage extends BasePage {
 
 	public static final String SUBJECT_TEMPLATE = "Password reset - TechMeetup";
	public static final String BODY_TEMPLATE = "Hi %s. You new password is: %s \n\n"
 			+ "Please remember to change your password in Settings when you log back in.\n\n"
 			+ "Thanks!";
 	@Property
 	private String email;
 
 	@Inject
 	private PasswordService passwordService;
 
 	@InjectComponent
 	private Form resetPasswordForm;
 
 	@Property
 	private boolean passwordReset = false;
 
 	@OnEvent(component = "resetPasswordForm", value = "submit")
 	public void resetPassword() {
 		Criteria criteria = getSession().createCriteria(User.class);
 		criteria.add(Restrictions.eq("email", email));
 		User user = (User) criteria.uniqueResult();
 		if (user != null) {
 
 			String newPass = passwordService.resetPassword(user);
 			Transaction transaction = getSession().getTransaction();
 			if (transaction != null && transaction.isActive()) {
 				transaction.commit();
 			}
 
			passwordReset = sendEmail(String.format(BODY_TEMPLATE, user
					.getName(), newPass), SUBJECT_TEMPLATE, user);
 
 		} else {
 			resetPasswordForm
 					.recordError("Sorry but we do not have anyonw with that email registered");
 		}
 
 	}
 
 	private boolean sendEmail(String message, String subject, User user) {
 		try {
 			SimpleEmail email = new SimpleEmail();
 			email.setHostName(ControlVars.SMTP_HOST);
 			email.setFrom("noreply@techmeetup.co.uk", "Techmeetup");
 			email.addTo(user.getEmail(), user.getName());
 			email.setSubject(subject);
 			email.setMsg(message);
 			email.send();
 			return true;
 		} catch (EmailException e) {
 			e.printStackTrace();
 			return false;
 		}
 	}
 
 }
