 package gov.nih.nci.evs.browser.webapp;
 
 import gov.nih.nci.evs.browser.properties.*;
 import gov.nih.nci.evs.browser.utils.*;
 
 import javax.servlet.http.*;
 
 public class ContactUsRequest extends FormRequest {
     // List of attribute name(s):
     public static final String SUBJECT = "subject";
     public static final String EMAIL_MSG = "email_msg";
    public static final String EMAIL_ADDRESS = SuggestionRequest.EMAIL;
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
         clearAttributes(new String[] { WARNING_TYPE });
     }
     
     public String submitForm() {
         clearAttributes(FormRequest.ALL_PARAMETERS);
         clearAttributes(new String[] { WARNING_TYPE });
         updateAttributes();
         AppProperties appProperties = AppProperties.getInstance();
         
         try {
             String mailServer = appProperties.getMailSmtpServer();
             String subject = _request.getParameter(SUBJECT);
             String emailMsg = _request.getParameter(EMAIL_MSG);
             String from = _request.getParameter(EMAIL_ADDRESS);
             String recipients[] = appProperties.getContactUsRecipients();
             MailUtils.postMail(mailServer, from, recipients, subject, emailMsg, _isSendEmail);
         } catch (UserInputException e) {
             String warnings = e.getMessage();
             _request.setAttribute(WARNINGS, StringUtils.toHtml(warnings));
             _request.setAttribute(WARNING_TYPE, Prop.WarningType.User.name());
             return WARNING_STATE;
         } catch (Exception e) {
             String warnings = "System Error: Your message was not sent.\n";
             warnings += "    (If possible, please contact NCI systems team.)\n";
             warnings += "\n";
             warnings += e.getMessage();
             _request.setAttribute(WARNINGS, StringUtils.toHtml(warnings));
             _request.setAttribute(WARNING_TYPE, Prop.WarningType.System.name());
             e.printStackTrace();
             return WARNING_STATE;
         }
 
         clearAttributes(MOST_PARAMETERS);
         String msg = "Your message was successfully sent.";
         _request.setAttribute(MESSAGE, StringUtils.toHtml(msg));
         printSendEmailWarning();
         return SUCCESSFUL_STATE;
     }
     
     protected String printSendEmailWarning() {
         if (_isSendEmail)
             return "";
         
         String warning = super.printSendEmailWarning();
         StringBuffer buffer = new StringBuffer(warning);
         AppProperties appProperties = AppProperties.getInstance();
         String[] recipients = appProperties.getContactUsRecipients();
         buffer.append("Debug:\n");
         buffer.append("    * recipient(s): " + StringUtils.toString(recipients, ", ") + "\n");
         buffer.append("    * Subject: " + _request.getParameter(SUBJECT) + "\n");
         buffer.append("    * Message: ");
         String emailMsg = _request.getParameter(EMAIL_MSG);
         emailMsg = INDENT + emailMsg.replaceAll("\\\n", "\n" + INDENT);
         buffer.append(emailMsg + "\n");
         buffer.append("    * Email: " + _request.getParameter(EMAIL_ADDRESS) + "\n");
         
         _request.setAttribute(WARNINGS, buffer.toString());
         return buffer.toString();
     }
 }
 
