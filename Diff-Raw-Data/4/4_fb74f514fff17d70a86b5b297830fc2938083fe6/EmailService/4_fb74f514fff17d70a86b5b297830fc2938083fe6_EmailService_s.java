 package cz.cvut.fel.bupro.service;
 
 import java.util.Locale;
 import java.util.Properties;
 
 import javax.mail.Authenticator;
 import javax.mail.Message;
 import javax.mail.MessagingException;
 import javax.mail.PasswordAuthentication;
 import javax.mail.Session;
 import javax.mail.Transport;
 import javax.mail.internet.AddressException;
 import javax.mail.internet.InternetAddress;
 import javax.mail.internet.MimeMessage;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Qualifier;
 import org.springframework.context.MessageSource;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 
 import cz.cvut.fel.bupro.config.Qualifiers;
 import cz.cvut.fel.bupro.model.Email;
 import cz.cvut.fel.bupro.model.MembershipState;
 import cz.cvut.fel.bupro.model.Project;
 import cz.cvut.fel.bupro.model.User;
 
 /**
  * Service for sending auto-generated messages (notifications) from system
  */
 @Service
 public class EmailService {
 	private final Log log = LogFactory.getLog(getClass());
 
 	@Autowired
 	@Qualifier(Qualifiers.EMAIL)
 	private MessageSource emailsMessageSource;
 	@Autowired
 	private MessageSource messageSource;
 	@Autowired
 	@Qualifier(Qualifiers.EMAIL)
 	private Properties properties;
 
 	private static InternetAddress[] parse(String to) throws AddressException {
 		String[] values = to.split("\\s*;\\s*");
 		InternetAddress[] addresses = new InternetAddress[values.length];
 		for (int i = 0; i < addresses.length && i < values.length; i++) {
 			addresses[i] = new InternetAddress(values[i]);
 		}
 		return addresses;
 	}
 
 	private static String encapsulateHtmlBody(String body) {
 		StringBuilder sb = new StringBuilder();
 		sb.append("<!DOCTYPE html>");
 		sb.append("<html xmlns=\"http://www.w3.org/1999/xhtml\">");
 		sb.append("<head>");
 		sb.append("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />");
 		sb.append("</head>");
 		sb.append("<body>");
 		sb.append(body);
 		sb.append("</body>");
 		sb.append("</html>");
 		return sb.toString();
 	}
 
 	private static String projectLink(String urlBase, Project project) {
 		if (!urlBase.endsWith("/")) {
 			urlBase += "/";
 		}
 		return urlBase + "project/view/" + project.getId();
 	}
 
 	private static String userLink(String urlBase, User user) {
 		if (!urlBase.endsWith("/")) {
 			urlBase += "/";
 		}
 		return urlBase + "user/view/" + user.getId();
 	}
 
 	private String getLocalizedFullName(Locale locale, String firstName, String lastName) {
 		return messageSource.getMessage("format.fullname", new String[] { firstName, lastName }, firstName + " " + lastName, locale);
 	}
 
 	private String getLocalizedFullName(Locale locale, User user) {
 		return getLocalizedFullName(locale, user.getFirstName(), user.getLastName());
 	}
 
 	public void sendMembershipAutoapproved(String linkUrl, Project project, User user) {
 		Locale locale = new Locale(project.getOwner().getLang());
 		String title = emailsMessageSource.getMessage("notify.new.membership.autoapproved.title", new String[] {}, "Bupro: membership automaticaliy approved",
 				locale);
 		String defaultBody = "User " + getLocalizedFullName(locale, user) + " joined your project " + project.getName();
 		String[] args = new String[] { project.getName(), projectLink(linkUrl, project), getLocalizedFullName(locale, user), userLink(linkUrl, user) };
 		String body = emailsMessageSource.getMessage("notify.new.membership.autoapproved.text", args, defaultBody, locale);
 		if (isHtmlEmail()) {
 			body = encapsulateHtmlBody(body);
 		}
 		sendEmail(project.getOwner(), title, body);
 	}
 
 	public void sendMembershipRequest(String linkUrl, Project project, User user) {
 		Locale locale = new Locale(project.getOwner().getLang());
 		String title = emailsMessageSource.getMessage("notify.new.membership.request.title", new String[] {}, "Bupro: membership request", locale);
 		String defaultBody = "User " + getLocalizedFullName(locale, user) + " requested to join your project " + project.getName();
 		String[] args = new String[] { project.getName(), projectLink(linkUrl, project), getLocalizedFullName(locale, user), userLink(linkUrl, user) };
 		String body = emailsMessageSource.getMessage("notify.new.membership.request.text", args, defaultBody, locale);
 		if (isHtmlEmail()) {
 			body = encapsulateHtmlBody(body);
 		}
 		sendEmail(project.getOwner(), title, body);
 	}
 
 	public void sendMembershipState(String linkUrl, Project project, User user, MembershipState membershipState) {
 		Locale locale = new Locale(user.getLang());
 		final String titleKey = "notify.membership.request." + String.valueOf(membershipState).toLowerCase() + ".title";
 		final String textKey = "notify.membership.request." + String.valueOf(membershipState).toLowerCase() + ".text";
 		String title = emailsMessageSource.getMessage(titleKey, new String[] {}, "Bupro: membership " + String.valueOf(membershipState), locale);
 		String[] args = new String[] { project.getName(), projectLink(linkUrl, project) };
 		String defaultText = "Your request to join project " + project.getName() + " " + String.valueOf(membershipState).toLowerCase();
 		String body = emailsMessageSource.getMessage(textKey, args, defaultText, locale);
 		if (isHtmlEmail()) {
 			body = encapsulateHtmlBody(body);
 		}
 		sendEmail(user, title, body);
 	}
 
 	private Session createSession() {
		final String username = (String) properties.get("mail.smpt.auth.username");
		final String password = (String) properties.get("mail.smpt.auth.password");
 		Authenticator authenticator = new Authenticator() {
 			@Override
 			protected PasswordAuthentication getPasswordAuthentication() {
 				return new PasswordAuthentication(username, password);
 			}
 		};
 		boolean passwordAuth = "true".equalsIgnoreCase(properties.getProperty("mail.smtp.auth", null));
 		return (passwordAuth) ? Session.getDefaultInstance(properties, authenticator) : Session.getDefaultInstance(properties);
 	}
 
 	private boolean isHtmlEmail() {
 		return "html".equalsIgnoreCase(properties.getProperty("mail.text.type", null));
 	}
 
 	@Transactional
 	public void sendEmail(User to, String subject, String text) {
 		sendEmail(to.getEmail(), null, subject, text);
 	}
 
 	public void sendEmail(String to, String cc, String subject, String text) {
 		log.info("Sending email to:" + to + " cc:" + cc + " title:'" + subject + "' body:'" + text + "'");
 		Session session = createSession();
 		MimeMessage message = new MimeMessage(session);
 		try {
 			String from = (String) session.getProperties().get("mail.from");
 			if (from != null && !from.trim().isEmpty()) {
 				log.debug("use mail.from=" + from);
 				message.setFrom(new InternetAddress(from));
 			}
 			message.addRecipients(Message.RecipientType.TO, parse(to));
 			message.setSubject(subject);
 			if (isHtmlEmail()) {
 				message.setText(text, "utf-8", "html");
 			} else {
 				message.setText(text);
 			}
 			Transport.send(message);
 			log.info("Email successfully sent");
 		} catch (AddressException e) {
 			log.error(e);
 		} catch (MessagingException e) {
 			log.error(e);
 		}
 	}
 
 	public void sendEmail(Email email) {
 		sendEmail(email.getTo(), email.getC(), email.getTitle(), email.getText());
 	}
 
 	public void sendRegistrationEmail(User user, String password, Locale locale) {
 		final String titleKey = "notify.user.registration.title";
 		final String textKey = "notify.user.registration.text";
 		String title = emailsMessageSource.getMessage(titleKey, new String[] {}, "Bupro: registration complete", locale);
 		String[] args = new String[] { user.getUsername(), password };
 		String defaultText = "Welcome to bupro. Your registration user:" + user.getUsername() + " pass:" + password;
 		String text = emailsMessageSource.getMessage(textKey, args, defaultText, locale);
 		sendEmail(user, title, text);
 	}
 
 	public void sendProjectExpiresWarning(Project project, int days) {
 		Locale locale = new Locale(project.getOwner().getLang());
 		final String titleKey = "notify.project.expires.soon.title";
 		final String textKey = "notify.project.expires.soon.text";
 		String title = emailsMessageSource.getMessage(titleKey, new String[] {}, "Bupro: project expiration", locale);
 		String[] args = new String[] { project.getName(), String.valueOf(project.getId()), String.valueOf(days) };
 		String defaultText = "Your project:" + args[0] + " expire in " + args[2] + " days";
 		String text = emailsMessageSource.getMessage(textKey, args, defaultText, locale);
 		sendEmail(project.getOwner(), title, text);
 	}
 }
