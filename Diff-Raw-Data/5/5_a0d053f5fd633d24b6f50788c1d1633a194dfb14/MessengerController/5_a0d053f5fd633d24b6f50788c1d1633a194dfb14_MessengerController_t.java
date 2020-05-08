 package nl.tomvanzummeren.msnapi.messenger.web;
 
 import nl.tomvanzummeren.msnapi.common.JsonView;
 import nl.tomvanzummeren.msnapi.common.SuccessJson;
 import nl.tomvanzummeren.msnapi.messenger.json.LoginStatusJson;
 import nl.tomvanzummeren.msnapi.messenger.json.events.ReceivedEventsJson;
 import nl.tomvanzummeren.msnapi.messenger.service.MessengerService;
 import nl.tomvanzummeren.msnapi.messenger.service.events.MsnEvent;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestParam;
 
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 import java.io.IOException;
 import java.util.List;
 
 /**
 * Controller for the entire API available to the Surf & Chat iPad client. It allows the client to log in, log out,
 * send messages, receive events, etc.
 *
  * @author Tom van Zummeren
  */
 @Controller
 @RequestMapping("/api")
 public class MessengerController {
 
     private MessengerService messengerService;
 
     @Autowired
     public MessengerController(MessengerService messengerService) {
         this.messengerService = messengerService;
     }
 
     @RequestMapping("/getloginstatus")
     public JsonView getLoginStatus(HttpSession session) {
         boolean loggedIn = messengerService.sendContactListIfLoggedIn(session.getId());
         return new JsonView(new LoginStatusJson(loggedIn));
     }
 
     @RequestMapping("/requestcontactlist")
     public JsonView requestContactList(HttpSession session) {
         messengerService.requestContactList(session.getId());
         return new JsonView(new SuccessJson());
     }
 
     @RequestMapping("/login")
     public JsonView login(@RequestParam String email, @RequestParam String password, HttpSession session) {
         messengerService.login(email, password, session.getId());
         return new JsonView(new SuccessJson());
     }
 
     @RequestMapping("/logout")
     public JsonView logout(HttpSession session) {
         messengerService.logout(session.getId());
         return new JsonView(new SuccessJson());
     }
 
     @RequestMapping("/receive")
     public JsonView receiveEvent(HttpSession session) {
         List<MsnEvent> events = messengerService.waitForEvent(session.getId());
         return new JsonView(new ReceivedEventsJson(events));
     }
 
     @RequestMapping("/stopreceive")
     public JsonView appClosed(HttpSession session) {
         messengerService.stopWaitingForEvent(session.getId());
         return new JsonView(new SuccessJson());
     }
 
     @RequestMapping("/send")
     public JsonView sendMessage(@RequestParam String email, @RequestParam String text, HttpSession session) {
         messengerService.sendInstantMessage(email, text, session.getId());
         return new JsonView(new SuccessJson());
     }
 
     @RequestMapping("/changeprofile")
     public JsonView changeProfile(@RequestParam String displayName, @RequestParam String personalMessage, HttpSession session) {
         messengerService.changeProfile(displayName, personalMessage, session.getId());
         return new JsonView(new SuccessJson());
     }
 
     @RequestMapping("/requestavatar")
     public JsonView requestAvatar(@RequestParam String contactId, HttpSession session) {
         messengerService.requestContactAvatar(contactId, session.getId());
         return new JsonView(new SuccessJson());
     }
 
     @RequestMapping("/downloadavatar")
     public void downloadAvatar(@RequestParam String contactId, HttpServletResponse response, HttpSession session) throws IOException {
         byte[] imageData = messengerService.getContactAvatar(contactId, session.getId());
         response.setContentType("image/png");
         response.getOutputStream().write(imageData);
     }
 }
