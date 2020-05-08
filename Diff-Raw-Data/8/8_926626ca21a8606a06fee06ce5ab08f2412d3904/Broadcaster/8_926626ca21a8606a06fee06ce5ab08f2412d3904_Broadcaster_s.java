 package no.niths.external;
 
 import java.io.EOFException;
 
 import javax.servlet.http.HttpServletResponse;
 
 import no.niths.common.AppConstants;
 import no.niths.common.SecurityConstants;
 import no.niths.services.interfaces.MailSenderService;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.http.HttpStatus;
 import org.springframework.security.access.prepost.PreAuthorize;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.ExceptionHandler;
 import org.springframework.web.bind.annotation.RequestBody;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.ResponseStatus;
 
 import com.sun.mail.smtp.SMTPAddressFailedException;
 
 @Controller
 @RequestMapping(AppConstants.BROADCAST)
 public class Broadcaster {
     private Logger logger = LoggerFactory.getLogger(Broadcaster.class);
 
     @Autowired
     private MailSenderService service;
 
     @RequestMapping(method = RequestMethod.POST)
     @ResponseStatus(value = HttpStatus.OK)
     @PreAuthorize(SecurityConstants.ADMIN_SR_FADDER_LEADER)
     public void broadcast(@RequestBody Email email) {
        String[] recipinentAddresses = email.getRecipientAddresses();
 
        for (String recipinetAddress : recipinentAddresses) {
             service.composeAndSend(
                    recipinetAddress,
                     "NITHs",
                     email.getSubject(),
                     "Hei.\n\nDette er " + email.getSenderName() + ".\n\n"
                         + email.getBody());            
         }
     }
 
     /**
      * 
      * @param e The exception thrown
      * @param res The response
      */
     @ExceptionHandler(EOFException.class)
     @ResponseStatus(value = HttpStatus.BAD_REQUEST)
     public void endOfFile(EOFException e, HttpServletResponse res) {
         res.setHeader("Error", "Wrong input");
     }
 
     /**
      * 
      * @param e The exception thrown
      * @param res The response
      */
     @ExceptionHandler(SMTPAddressFailedException.class)
     @ResponseStatus(value = HttpStatus.BAD_REQUEST)
     public void handleNonexistentEmailAddresses(SMTPAddressFailedException e,
             HttpServletResponse res) {
         res.setHeader("Error", "Email addresse(s) don't exist");
     }
 }
