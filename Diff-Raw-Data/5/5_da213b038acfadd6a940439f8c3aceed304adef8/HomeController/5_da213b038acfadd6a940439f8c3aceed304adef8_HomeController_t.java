 package services;
 
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.amqp.core.AmqpTemplate;
 
 import dse_domain.domain.Doctor;
 
 //import at.ac.tuwien.infosys.dse_domain.domain.Doctor;
 
 /**
  * Handles requests for the application home page.
  */
 @Controller
 public class HomeController {
 	@Autowired
 	AmqpTemplate amqpTemplate;
 	
 	Doctor doc;
 
 	@RequestMapping(value = "/")
 	public String home(Model model) {
 		model.addAttribute(new Message());
 		return "WEB-INF/views/home.jsp";
 	}
 
 	@RequestMapping(value = "/sendTo_messenger", method = RequestMethod.POST)
 	public String sendToMessenger(Model model, Message message) {
		doc = new Doctor();
		doc.setFirstName("WAGI");
		amqpTemplate.convertAndSend("messenger", doc.getFirstName());
 		model.addAttribute("publishedMessenger", true);
 
 		return home(model);
 	}
 
 	@RequestMapping(value = "/sendTo_allocator", method = RequestMethod.POST)
 	public String sendToAllocator(Model model, Message message) {
 
 		amqpTemplate.convertAndSend("allocator", message.getValue());
 		model.addAttribute("publishedAllocator", true);
 
 		return home(model);
 	}
 
 	@RequestMapping(value = "/get", method = RequestMethod.POST)
 	public String get(Model model) {
 
 		String message = (String) amqpTemplate.receiveAndConvert("allocator");
 		if (message != null)
 			model.addAttribute("got", message);
 		else
 			model.addAttribute("got_queue_empty", true);
 
 		return home(model);
 	}
 
 }
