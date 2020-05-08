 package gov.nih.nci.evs.browser.webapp;
 
 import gov.nih.nci.evs.browser.properties.*;
 import gov.nih.nci.evs.browser.utils.*;
 
 import javax.servlet.http.*;
 
 public class ContactUsRequest extends FormRequest {
     // List of session attribute name(s):
     public static final String SUBJECT = "subject";
     public static final String EMAIL_MSG = "email_msg";
     public static final String EMAIL_ADDRESS = "email_address";
     public static final String WARNING_TYPE = "warning_type";
 
     public static final String[] ALL_PARAMETERS = new String[] { 
         SUBJECT, EMAIL_MSG, EMAIL_ADDRESS };
     public static final String[] MOST_PARAMETERS = new String[] { 
         SUBJECT, EMAIL_MSG };
 
     public ContactUsRequest(HttpServletRequest request) {
         super(request);
         setParameters(ALL_PARAMETERS);
     }
     
    public void clear() {
        super.clear();
        clearSessionAttributes(new String[] { WARNING_TYPE });
    }
    
     public String submitForm() {
         clearSessionAttributes(FormRequest.ALL_PARAMETERS);
         clearSessionAttributes(new String[] { WARNING_TYPE });
         updateSessionAttributes();
         AppProperties appProperties = AppProperties.getInstance();
         
         try {
             String mailServer = appProperties.getMailSmtpServer();
             String subject = _request.getParameter(SUBJECT);
             String emailMsg = _request.getParameter(EMAIL_MSG);
             String from = _request.getParameter(EMAIL_ADDRESS);
             String recipients[] = appProperties.getContactUsRecipients();
             MailUtils.postMail(mailServer, from, recipients, subject, emailMsg);
         } catch (UserInputException e) {
             String warnings = e.getMessage();
             _request.getSession().setAttribute(WARNINGS, StringUtils.toHtml(warnings));
             _request.getSession().setAttribute(WARNING_TYPE, Prop.WarningType.User.name());
             return WARNING_STATE;
         } catch (Exception e) {
             String warnings = "System Error: Your message was not sent.\n";
             warnings += "    (If possible, please contact NCI systems team.)\n";
             warnings += "\n";
             warnings += e.getMessage();
             _request.getSession().setAttribute(WARNINGS, StringUtils.toHtml(warnings));
             _request.getSession().setAttribute(WARNING_TYPE, Prop.WarningType.System.name());
             e.printStackTrace();
             return WARNING_STATE;
         }
 
         String msg = "Your message was successfully sent.";
         clearSessionAttributes(MOST_PARAMETERS);
         _request.getSession().setAttribute("message", StringUtils.toHtml(msg));
         return SUCCESSFUL_STATE;
     }
 }
 
