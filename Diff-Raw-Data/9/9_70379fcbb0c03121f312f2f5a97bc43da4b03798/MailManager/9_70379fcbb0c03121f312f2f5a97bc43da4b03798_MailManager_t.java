 package org.inftel.ssa.web;
 
 import java.util.Properties;
 import java.util.ResourceBundle;
 import javax.faces.bean.ApplicationScoped;
 import javax.faces.bean.ManagedBean;
 import javax.mail.Message;
 import javax.mail.MessagingException;
 import javax.mail.Session;
 import javax.mail.Transport;
 import javax.mail.internet.InternetAddress;
 import javax.mail.internet.MimeMessage;
 
 @ManagedBean
 @ApplicationScoped
 public class MailManager {
 
 	private static final String HOST = PropertiesHelper.getProperty("host");
 	private static final String PORT = PropertiesHelper.getProperty("port_tls");
 	private static final String USERNAME = PropertiesHelper.getProperty("username_gmail");
 	private static final String PASSWORD = PropertiesHelper.getProperty("password_gmail");
 	private static final String SENDER = PropertiesHelper.getProperty("sender");
 
 	/**
 	 * Método principal para el envío de correos vía TLS
 	 */
 	private void send(String subject, String body, String receiver) {
 		Properties props = new Properties();
 		props.setProperty("mail.smtp.host", HOST);
 		props.setProperty("mail.smtp.starttls.enable", "true");
 		props.setProperty("mail.smtp.port", PORT);
 		props.setProperty("mail.smtp.user", SENDER);
 		props.setProperty("mail.smtp.auth", "true");
 
 		Session session = Session.getInstance(props);
 
 		try {
 			Message message = new MimeMessage(session);
 			message.setFrom(new InternetAddress(SENDER));
 			message.addRecipient(Message.RecipientType.TO, new InternetAddress(receiver));
 			message.setSubject(subject);
 			message.setText(body);
 
 			Transport transport = session.getTransport("smtp");
 			transport.connect(USERNAME, PASSWORD);
 
 			transport.sendMessage(message, message.getAllRecipients());
 			transport.close();
 			System.out.println("Done");
 		} catch (MessagingException e) {
 			throw new RuntimeException("Error intentando enviar correo: " + e.getLocalizedMessage(), e);
 		}
 	}
 
 	/**
 	 * Envía un correo a un recurso cuando se le asigna a un proyecto
 	 *
 	 * @param receiver Dirección de correo del recurso
 	 * @param project Descripción del proyecto
 	 */
	public void sendProjectAssigned(final String receiver, String project) {
		final String subject = PropertiesHelper.getProperty("subject_mail_project") + " " + project;
		final String body = PropertiesHelper.getProperty("body_mail_project") + " " + project;
		//FIXME esto no es muy elegante aqui, pero hay prisa!
		new Thread(new Runnable() { public void run() { send(subject, body, receiver);} } ).start();
 	}
 
 	/**
 	 * Envía un correo a un miembro del equipo con la tarea asignada
 	 *
 	 * @param receiver Dirección de correo del recurso
 	 * @param task Descripción de la tarea
 	 */
 	public void sendTaskAssigned(String receiver, String task) {
 		String subject = PropertiesHelper.getProperty("subject_mail_task") + " " + task;
 		String body = PropertiesHelper.getProperty("body_mail_task") + " " + task;
 		send(subject, body, receiver);
 	}
 
 	/**
 	 * Envía un correo a un miembro del equipo con un cambio de status de la tarea asignada.
 	 *
 	 * @param receiver Dirección de correo del recurso
 	 * @param task Descripción de la tarea
 	 * @param status Nuevo estado de la tarea
 	 */
 	public void sendTaskStatus(String receiver, String task, String status) {
 		String subject = PropertiesHelper.getProperty("subject_mail_status") + " " + task;
 		String body = PropertiesHelper.getProperty("body_mail_status") + " " + status;
 		send(subject, body, receiver);
 	}
 
 	// Lectura de propiedades por defecto de la aplicación
 	private final static class PropertiesHelper {
 
 		private static ResourceBundle propertiesByDefault;
 
 		static {
 			// Este bloque se ejecuta cuando se accede por primera vez a la clase
 			propertiesByDefault = ResourceBundle.getBundle("org.inftel.ssa.web.mail");
 		}
 
 		public static String getProperty(String property) {
 			return propertiesByDefault.getString(property);
 		}
 	}
 }
