 package edu.mit.cci.turksnet.web;
 
 
import com.sun.xml.internal.ws.server.StatefulInstanceResolver;
 import edu.mit.cci.turksnet.Node;
 import edu.mit.cci.turksnet.Session_;
 import edu.mit.cci.turksnet.plugins.LoomPlugin;
 import org.apache.log4j.Logger;
 import org.springframework.context.ApplicationContext;
 import org.springframework.mail.SimpleMailMessage;
 import org.springframework.mail.javamail.JavaMailSender;
 import org.springframework.mail.javamail.JavaMailSenderImpl;
 import org.springframework.roo.addon.web.mvc.controller.RooWebScaffold;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.ResponseBody;
 import org.springframework.web.context.ContextLoader;
 
 import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.Collections;
 import java.util.Date;
 import java.util.Map;
 
 
 @RooWebScaffold(path = "session_s", formBackingObject = Session_.class)
 @RequestMapping("/session_s")
 @Controller
 public class Session_Controller {
 
     private static Logger log = Logger.getLogger(Session_Controller.class);
 
     @RequestMapping(value = "/{id}/turk/{turkerid}", method = RequestMethod.GET, headers = "accept=text/xml")
     @ResponseBody
     public Node getNodeForTurker(@PathVariable("id") Long id, @PathVariable("turkerid") String turkerid) {
         Session_ s = Session_.findSession_(id);
         Node n = s.getNodeForTurker(turkerid);
         if (n != null) {
             return n;
         }
         throw new IllegalArgumentException("Could not identify turker with id " + turkerid + " in session " + id);//forward to error page
 
 
     }
 
     //for the html app
 
     @RequestMapping(value = "/{id}/turk/feedback", method = RequestMethod.GET)
     public String getForm(@PathVariable("id") Long id, @RequestParam("assignmentId") String assignmentId, @RequestParam("workerId") String workerId, Model model) {
         model.addAttribute("workerId", workerId);
         model.addAttribute("assignmentId", assignmentId);
         model.addAttribute("submission",false);
 
         return "session_s/node/feedback";
     }
 
 
     @RequestMapping(value = "/{id}/turk/feedback", method = RequestMethod.POST)
     public String postFeedback(@PathVariable("id") Long id, @RequestParam("assignmentId") String assignmentId, @RequestParam("workerId") String workerId, @RequestParam("feedback") String feedback, Model model) {
         StringBuilder builder = new StringBuilder();
         builder.append("Worker: ").append(workerId).append("\n");
         builder.append("Session: ").append(id).append("\n");
         builder.append("Assignment: ").append(assignmentId).append("\n");
         builder.append("Message:\n\n");
         builder.append(feedback);
 
 
 
         ApplicationContext context =  ContextLoader.getCurrentWebApplicationContext();
         SimpleMailMessage tmpl = context.getBean("mailMessage",SimpleMailMessage.class);
         JavaMailSender sender = context.getBean("mailSender", JavaMailSenderImpl.class);
         SimpleMailMessage message = new SimpleMailMessage(tmpl);
         message.setSentDate(new Date());
         message.setText(builder.toString());
         sender.send(message);
         model.addAttribute("submission",true);
 
         return "session_s/node/feedback";
     }
 
 
     @RequestMapping(value = "/{id}/turk/app", method = RequestMethod.GET)
     public String getTurkerApp(@PathVariable("id") Long id, @RequestParam("assignmentId") String assignmentId, Model model, HttpServletRequest request) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
         //, , @RequestParam("turkerId") String turkerid\
         String turkerid = "" + -1;
         Node n = null;
         String submitTo = null;
         String hitId = request.getParameter("hitId");
         Session_ s = null;
         if ("ASSIGNMENT_ID_NOT_AVAILABLE".equals(assignmentId)) {
             n = Node.getDummyNode();
 
         } else {
             s = Session_.findSession_(id);
             turkerid = request.getParameter("workerId");
             submitTo = request.getParameter("turkSubmitTo");
             n = s.getNodeForTurker(turkerid);
             if (n == null) {
                 throw new IllegalArgumentException("Could not identify turker with id " + turkerid + " in session " + id);//forward to error page
 
             }
         }
 
         model.addAttribute("node", n);
         NodeForm nf = new NodeForm();
 //        nf.setAssignmentId(assignmentId);
 //        nf.setSubmitTo(submitTo);
 //        nf.setHitId(hitId);
        Map<String,String> bonus = s==null? Collections.<String,String>emptyMap():s.getExperiment().getActualPlugin().getBonus(n);
 
         model.addAttribute("currentbonus",bonus.containsKey("CumulativeBonus")?bonus.get("CumulativeBonus"):"0.0");
         model.addAttribute("round", s == null ? 0 : s.getIteration());
         model.addAttribute("rounds", s == null ? 0 : s.getExperiment().getPropsAsMap().get(LoomPlugin.PROP_ITERATION_COUNT));
         model.addAttribute("nodeForm", nf);
         model.addAttribute("turkerId", turkerid);
         model.addAttribute("assignmentId", assignmentId);
         model.addAttribute("submitTo", submitTo);
         model.addAttribute("hitId", hitId);
         return "session_s/node/app";
     }
 
 
 }
