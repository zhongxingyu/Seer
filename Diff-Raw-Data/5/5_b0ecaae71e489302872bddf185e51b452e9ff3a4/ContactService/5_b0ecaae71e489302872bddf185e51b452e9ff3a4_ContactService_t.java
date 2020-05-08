 package gov.usgs;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.io.UnsupportedEncodingException;
 import java.util.Properties;
 
 import javax.mail.Message;
 import javax.mail.MessagingException;
 import javax.mail.Session;
 import javax.mail.Transport;
 import javax.mail.internet.AddressException;
 import javax.mail.internet.InternetAddress;
 import javax.mail.internet.MimeMessage;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Servlet implementation class ContactService
  */
 public class ContactService extends HttpServlet {
 	
 	private static class FeedbackRequest {
 		private String comments;
 		private String email;
 		private boolean emailResponseRequired;
 
 		public FeedbackRequest(HttpServletRequest req) {
 			comments = req.getParameter("comments");
 			email = req.getParameter("email");
 			String rrq = req.getParameter("replyrequired");
 			emailResponseRequired = Boolean.parseBoolean(rrq);
 		}
 
 		public String getEmail() {
 			return email;
 		}
 
 		public boolean getEmailResponseRequired() {
 			return emailResponseRequired;
 		}
 
 		public String getComments() {
 			return comments;
 		}
 
 	}
 
 	private static final long serialVersionUID = 1L;
        
     public ContactService() {
         super();
     }
 
 	private final static Logger logger = LoggerFactory.getLogger(ContactService.class);
 
 	@Override
 	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
 	throws ServletException, IOException {
 		logger.info("Serving: {}", req.getQueryString());
 		String remoteAddr = req.getRemoteAddr();
 		String email = req.getParameter("email");
 		String emailResponseRequired = req.getParameter("replyrequired");
 
 		logger.debug("Request Email: {}, ResponseRequired: {}", email, emailResponseRequired);
 		logger.info("From: {}", remoteAddr);
 		
 		FeedbackRequest cue = new FeedbackRequest(req);
 		emailPubs(cue);
 		
 		PrintWriter writer = resp.getWriter();
 		try {
 			writer.append("<status>success</status>");
 			writer.flush();
 		} finally {
 			if (writer != null ) writer.flush();
 		}
 	}
 
 	@Override
 	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
 	throws ServletException, IOException {
 		doGet(req, resp);
 	}
 
 
 	private static final String USGS_REMEDY = "servicedesk@usgs.gov";
 
 	private static final String NO_REPLY_NAME = "NGWMN Help";
 	private static final String NO_REPLY_ADDRESS = "gwdp_help@usgs.gov";
 
 	public static final InternetAddress DEFAULT_REPLY_TO_ADDRESS = makeDefaultReplyToAddress();
 	public static final InternetAddress[] DEFAULT_SENDTO_ADDRESSES = makeDefaultSendToAddress();
 	public static final InternetAddress[] SENDTO_ADDRESS = DEFAULT_SENDTO_ADDRESSES;
 
 	private static InternetAddress[] makeDefaultSendToAddress() {
 		try {
 			return new InternetAddress[] {new InternetAddress(USGS_REMEDY)};
 		} catch (AddressException e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 
 	private static InternetAddress makeDefaultReplyToAddress() {
 		try {
 			return new InternetAddress(NO_REPLY_ADDRESS, NO_REPLY_NAME);
 		} catch (UnsupportedEncodingException e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 
 	public static void emailPubs(FeedbackRequest cue) {
 
 		boolean hasEmail = cue.getEmail() != null && !"".equals(cue.getEmail());
 		String userEmail = (hasEmail)? cue.getEmail() : "" ;
 		String auto = "This is an auto-generated email from the USGS NGWMN.  " +
 		"Below is a copy of the message you (" + userEmail + ") submitted.  If you feel you have received this message erroneously, " +
 		"please contact servicedesk@usgs.gov.\n\n";
 
 		try {
 			// FROM ADDRESS:
 			InternetAddress addressFrom = (hasEmail)? new InternetAddress(userEmail): DEFAULT_REPLY_TO_ADDRESS;
 
 			// TO ADDRESS:
 			InternetAddress[] addressTo = null;
 
 			// CC/REPLY-TO ADDRESS:
 			InternetAddress addressReply = null;
 
 			//Set the host smtp address
 			Properties props = new Properties();
 
 			/*
 			 * DEBUGGING
 			 */
 			boolean debug = true; //"127.0.0.1".equals(cue.getRemoteAddr());
 			if (debug) {
 				props.put("mail.smtp.host", "gsvaresh01.er.usgs.gov");
				addressTo = new InternetAddress[] {
						new InternetAddress("rhayes@usgs.gov"),
						new InternetAddress("jlucido@usgs.gov")
						};
 			} else {
 				/**
 				 * This is changed because of our node in Denver.
 				 * the routing traffic comes from itself.
 				 */
 				props.put("mail.smtp.host", "localhost");
 				addressTo = DEFAULT_SENDTO_ADDRESSES;
 			}
 
 
 			// create some properties and get the default Session
 			Session session = Session.getDefaultInstance(props, null);
 			session.setDebug(debug);
 
 			// create a message
 			Message msg = new MimeMessage(session);
 			{
 				// set the from and to address
 				msg.setFrom(addressFrom);
 				msg.setRecipients(Message.RecipientType.TO, addressTo);
 
 				// set the reply to address if there is one.
 				if (hasEmail) {
 					addressReply = new InternetAddress(cue.getEmail());
 					InternetAddress[] replyTo = {addressReply};
 					msg.setReplyTo(replyTo);
 				}
 
 				// Setting the Subject and Content Type
 				msg.setSubject("NGWMN Data Portal");
 				StringBuilder sb = new StringBuilder();
 				if (hasEmail) {
 					sb.append("A NGWMN user with email address ").append(userEmail).append(" sends some feedback.\n");
 				} else {
 					sb.append("A NGWMN user who does not care to provide their email address sends this feedback.\n");
 				}
 				if (cue.getEmailResponseRequired()) {
 					sb.append("A response is requested, to email: ").append(userEmail).append("\n");
 				}
 				sb.append("\n\n");
 				sb.append("Comment:\n");
 				sb.append(cue.getComments());
 				msg.setContent(sb.toString(), "text/plain");
 			}
 			Transport.send(msg);
 			logger.info("Email sent to Remedy");
 		} catch (MessagingException e) {
 			logger.error("Error emailing Remedy.", e);
 		}
 
 	}
 }
