 package pl.agh.enrollme.service;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowire;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.security.authentication.encoding.PasswordEncoder;
 import org.springframework.security.authentication.encoding.ShaPasswordEncoder;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 import pl.agh.enrollme.controller.PassResetController;
 import pl.agh.enrollme.model.Person;
 
 import javax.faces.application.FacesMessage;
 import javax.faces.context.FacesContext;
 import java.security.Principal;
 
 /**
  * Author: Piotr Turek
  */
 @Service
 public class PassResetService implements IPassResetService {
 
     private static final Logger LOGGER = LoggerFactory.getLogger(PassResetService.class);
 
     @Autowired
     private PersonService personService;
 
     @Transactional
     @Override
     public PassResetController createController() {
         return new PassResetController();
     }
 
     @Transactional
     @Override
     public void resetPassword(PassResetController controller) {
         Person person = personService.getCurrentUser();
 
         PasswordEncoder encoder = new ShaPasswordEncoder(256);
         String encodedPassword = encoder.encodePassword(controller.getOldPass(), null);
         if (!encodedPassword.equals(person.getPassword())) {
             FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Bad Password", "Wrong password provided!");
             addMessage(message);
             LOGGER.debug("Person: " + person + " tried to change his pass, but provided a faulty old one");
             return;
         }
 
 
 
     }
 
     private void addMessage(FacesMessage message) {
         FacesContext.getCurrentInstance().addMessage(null, message);
     }
 
 }
