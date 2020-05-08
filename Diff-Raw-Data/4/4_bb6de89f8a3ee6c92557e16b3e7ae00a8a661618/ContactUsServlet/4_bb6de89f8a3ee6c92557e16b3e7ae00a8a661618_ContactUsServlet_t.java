 package my.com.rentalcarmalaysia.servlet;
 
 import java.io.IOException;
 import java.util.Properties;
 
 import javax.mail.Authenticator;
 import javax.mail.Message;
 import javax.mail.MessagingException;
 import javax.mail.PasswordAuthentication;
 import javax.mail.Session;
 import javax.mail.Transport;
 import javax.mail.internet.InternetAddress;
 import javax.mail.internet.MimeMessage;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import my.com.rentalcarmalaysia.util.Base64;
 import net.tanesha.recaptcha.ReCaptchaImpl;
 import net.tanesha.recaptcha.ReCaptchaResponse;
 
 import org.json.simple.JSONObject;
 
 @SuppressWarnings("serial")
 public class ContactUsServlet extends HttpServlet {
 
     public static final String RESP_FIELD_CODE = "code";
     public static final String RESP_RESULT_SUCCESS = "1";
     public static final String RESP_RESULT_ERR_UNKNOWN = "-1";
     public static final String RESP_RESULT_ERR_VERIFY_CAPTCHA = "-2";
     
     private String recaptcha_privatekey;
     private String smtp_pwd;
     
     @Override
     public void init() throws ServletException {
         super.init();
         recaptcha_privatekey = System.getenv("RECAPTCHA_PRI") == null ? "" : System.getenv("RECAPTCHA_PRI").trim();
         smtp_pwd = System.getenv("SMTP_PWD") == null ? "" : System.getenv("SMTP_PWD").trim();
         smtp_pwd = smtp_pwd.length() > 0 ? new String(Base64.decode(smtp_pwd.getBytes())) : smtp_pwd;
     }
 
     @Override
     protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
         JSONObject jsonobj = new JSONObject();
         if( recaptcha_privatekey.equals("") || smtp_pwd.equals("") ) {
             putJsonobj(jsonobj, RESP_FIELD_CODE, RESP_RESULT_ERR_UNKNOWN);
             commitJSONResp(resp, jsonobj);
             return;
         }
         
         String remoteAddr = req.getRemoteAddr() == null ? "" : req.getRemoteAddr().trim();
         String challenge = req.getParameter("captchaChall") == null ? "" : req.getParameter("captchaChall").trim();
         String uresponse = req.getParameter("captchaResp") == null ? "" : req.getParameter("captchaResp").trim();
         String contactName = req.getParameter("contactName") == null ? "" : req.getParameter("contactName").trim();
         String contactEmail = req.getParameter("contactEmail") == null ? "" : req.getParameter("contactEmail").trim();
         String contactMessage = req.getParameter("contactMessage") == null ? "" : req.getParameter("contactMessage").trim();
         String contactPhone = req.getParameter("contactPhone") == null ? "" : req.getParameter("contactPhone").trim();
         contactPhone = contactPhone.equals("") ? "-" : contactPhone;
         
         if( remoteAddr.equals("") || challenge.equals("") || 
                 contactName.equals("") || contactEmail.equals("") || contactMessage.equals("") ) {
             putJsonobj(jsonobj, RESP_FIELD_CODE, RESP_RESULT_ERR_UNKNOWN);
             commitJSONResp(resp, jsonobj);
             return;
         }
         
         ReCaptchaImpl reCaptcha = new ReCaptchaImpl();
         reCaptcha.setPrivateKey(recaptcha_privatekey);
         
         ReCaptchaResponse reCaptchaResponse = reCaptcha.checkAnswer(remoteAddr, challenge, uresponse);
         if( !reCaptchaResponse.isValid() ) {
             putJsonobj(jsonobj, RESP_FIELD_CODE, RESP_RESULT_ERR_VERIFY_CAPTCHA);
             commitJSONResp(resp, jsonobj);
             return;
         }
         
         Session session = getMailSession();
         if( !sendContactUsEmail(resp, jsonobj, session, 
                 contactName, contactEmail, contactPhone, contactMessage) ) {
             return;
         }
        session = getMailSession();
         sendAcknowledgementEmail(resp, jsonobj, session, 
                 contactName, contactEmail, contactPhone, contactMessage);
         
         putJsonobj(jsonobj, RESP_FIELD_CODE, RESP_RESULT_SUCCESS);
         commitJSONResp(resp, jsonobj);
     }
     
     private void commitJSONResp(HttpServletResponse resp, JSONObject jsonobj) throws IOException {
         String json = jsonobj.toString();
         resp.setStatus(HttpServletResponse.SC_OK);
         resp.setContentType("application/json");
         resp.getOutputStream().write(json.getBytes(), 0, json.getBytes().length);
         resp.getOutputStream().flush();
         resp.getOutputStream().close();
     }
 
     @SuppressWarnings("unchecked")
     private Object putJsonobj(JSONObject jsonobj, String key, String value) {
         return jsonobj.put(key, value);
     }
     
     private boolean sendContactUsEmail(
             HttpServletResponse resp, 
             JSONObject jsonobj,
             Session session,
             String contactName, 
             String contactEmail, 
             String contactPhone, 
             String contactMessage) throws IOException {        
         try {
             InternetAddress[] replyToAdd = new InternetAddress[1];
             replyToAdd[0] = new InternetAddress(contactEmail, contactName);
             
             Message message = new MimeMessage(session);
             message.setFrom(new InternetAddress("ssniwalee@gmail.com", "SSNiwa Mailer"));
             message.setReplyTo(replyToAdd);
             message.setRecipients(Message.RecipientType.TO,
                     InternetAddress.parse("bryklee@gmail.com"));
            message.setSubject("[Enquiry] [rentalcarmalaysia.com.my] [Contact Us] - " + contactName);
             message.setContent(
                     getContactUsContent(contactName, contactEmail, contactPhone, contactMessage)
                     , "text/html" );
     
             Transport.send(message);
         }
         catch(MessagingException me) {
             putJsonobj(jsonobj, RESP_FIELD_CODE, RESP_RESULT_ERR_UNKNOWN);
             commitJSONResp(resp, jsonobj);
             return false;
         }
         
         return true;
     }
     
     private boolean sendAcknowledgementEmail(
             HttpServletResponse resp, 
             JSONObject jsonobj,
             Session session,
             String contactName, 
             String contactEmail, 
             String contactPhone, 
             String contactMessage) throws IOException {        
         try {
             InternetAddress[] replyToAdd = new InternetAddress[1];
             replyToAdd[0] = new InternetAddress("ssniwa@gmail.com", "SSNiwa");
             
             Message message = new MimeMessage(session);
             message.setFrom(new InternetAddress("ssniwalee@gmail.com", "SSNiwa Mailer"));
             message.setReplyTo(replyToAdd);
             message.setRecipients(Message.RecipientType.TO,
                     InternetAddress.parse(contactEmail));
             message.setSubject("Rental Car Malaysia Acknowledgement - DO NOT REPLY");
             message.setContent(
                     getAcknowledgementContent(contactName, contactEmail, contactPhone, contactMessage)
                     , "text/html" );
     
             Transport.send(message);
         }
         catch(MessagingException me) {
             putJsonobj(jsonobj, RESP_FIELD_CODE, RESP_RESULT_ERR_UNKNOWN);
             commitJSONResp(resp, jsonobj);
             return false;
         }
         
         return true;
     }
     
     private Session getMailSession() {
         Properties props = new Properties();
         props.put("mail.smtp.host", "smtp.gmail.com");
         props.put("mail.smtp.socketFactory.port", "465");
         props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
         props.put("mail.smtp.auth", "true");
         props.put("mail.smtp.port", "465");
         Session session = Session.getDefaultInstance(props,
                 new Authenticator() {
                     protected PasswordAuthentication getPasswordAuthentication() {
                         return new PasswordAuthentication("ssniwalee@gmail.com", smtp_pwd);
                     }
                 }
         );
         return session;
     }
     
     private String getContactUsContent(String contactName, String contactEmail, 
             String contactPhone, String contactMessage) {
         StringBuilder sb = new StringBuilder();
         sb.append("<p><b>Name:</b> ");
         sb.append(contactName);
         sb.append("</p>");
         sb.append("<p><b>Email:</b> ");
         sb.append(contactEmail);
         sb.append("</p>");
         sb.append("<p><b>Phone:</b> ");
         sb.append(contactPhone);
         sb.append("</p>");
         sb.append("<p><b>Message:</b><br/>");
         sb.append(contactMessage);
         sb.append("</p>");
         return sb.toString();
     }
     
     private String getAcknowledgementContent(
             String contactName, 
             String contactEmail, 
             String contactPhone, 
             String contactMessage) {
         StringBuilder sb = new StringBuilder();
         sb.append("<p>");
         sb.append("Dear " + contactName + ",");
         sb.append("</p>");
         sb.append("<p>");
         sb.append("Thank you very much for writing to us. This is just an acknowledgement that we " +
         		"have received your enquiry message below.");
         sb.append("</p>");
         sb.append("<br/>");
         sb.append(getContactUsContent(contactName, contactEmail, contactPhone, contactMessage));
         sb.append("<br/>");
         sb.append("<p><b>");
         sb.append("IMPORTANT NOTE: This is an auto generated email. Please DO NOT reply on this email.");
         sb.append("</b></p>");
         return sb.toString();
     }
 }
