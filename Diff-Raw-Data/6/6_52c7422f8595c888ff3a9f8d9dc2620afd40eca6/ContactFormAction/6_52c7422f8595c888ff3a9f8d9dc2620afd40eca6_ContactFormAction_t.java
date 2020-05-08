 package com.worthsoln.patientview;
 
 import com.worthsoln.database.action.DatabaseAction;
 import com.worthsoln.patientview.logon.LogonUtils;
 import com.worthsoln.utils.LegacySpringUtils;
 import org.apache.struts.action.ActionForm;
 import org.apache.struts.action.ActionForward;
 import org.apache.struts.action.ActionMapping;
 
import javax.servlet.ServletContext;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 public class ContactFormAction extends DatabaseAction {
 
     public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request,
                                  HttpServletResponse response) throws Exception {
 
         Patient patient = PatientUtils.retrievePatient(request, getDao(request));
         if (patient != null) {
             String message = request.getParameter("message");
             String type = request.getParameter("type");
             String email = request.getParameter("email");
             String subject = "Renal Patient View Enquiry";
 
             message = createMessage(message, patient, email);
 
             ServletContext context = request.getSession().getServletContext();
 
             if ("unit".equals(type)) {
                 // Send to unit
                String unitEmail = request.getParameter("unit.rpvadminemail");
                 if (unitEmail != null && unitEmail.length() > 0) {
                     if (email != null && email.length() > 0) {
                         EmailUtils.sendEmail(context, email, unitEmail, subject, message);
                     } else {
                         EmailUtils.sendEmail(context, unitEmail, subject, message);
                     }
                 }
             } else if ("admin".equals(type)) {
                 // Send  to admin
                 String superadminEmail = context.getInitParameter("admin.email.to");
 
                 if (email != null && email.length() > 0) {
                     EmailUtils.sendEmail(context, email, superadminEmail, subject, message);
                 } else {
                     EmailUtils.sendEmail(context, superadminEmail, subject, message);
                 }
             }
         } else if (!LegacySpringUtils.getSecurityUserManager().isRolePresent("patient")) {
             return LogonUtils.logonChecks(mapping, request, "control");
         }
         request.setAttribute("emailSent", true);
         return LogonUtils.logonChecks(mapping, request);
     }
 
 
     private String createMessage(String message, Patient patient, String email) {
         String completeMessage = "";
 
         completeMessage += "Patient name - " + patient.getForename() + " " + patient.getSurname() + "\n";
         completeMessage += "NHS no - " + patient.getNhsno() + "\n";
         completeMessage += "Email - " + email + "\n";
         completeMessage += "\n";
         completeMessage += "Message:" + "\n";
         completeMessage += "------------" + "\n";
         completeMessage += message+ "\n";
         completeMessage += "------------" + "\n";
 
         return completeMessage;
     }
 
     public String getDatabaseName() {
         return "patientview";
     }
 
     public String getIdentifier() {
         return "patient";
     }
 }
