 package com.castrati.capcitypetcare.controller;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.naming.Context;
 import javax.validation.Valid;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.mail.MailSender;
 import org.springframework.mail.SimpleMailMessage;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.context.WebApplicationContext;
 
 import com.castrati.capcitypetcare.beans.ContactForm;
 import com.castrati.capcitypetcare.beans.EmailHandler;
 
 @Controller
 @RequestMapping(value="/")
 public class HelloController {
 
 	@Autowired
 	private EmailHandler eh;
 	
 	@RequestMapping(method = RequestMethod.GET, value = "")
 	public String root(ModelMap model){
 		return home(model);
 	}
 	
     @RequestMapping(method = RequestMethod.GET, value = "home")
     public String home(ModelMap model) {
 
         model.addAttribute("title", "Home");
         return "welcomePage";
 
     }
 
     @RequestMapping(method = RequestMethod.GET, value = "about")
     public String about(ModelMap model) {
 
         model.addAttribute("title", "About Us");
         return "about";
 
     }
 
     @RequestMapping(method = RequestMethod.GET, value = "contact")
     public String contact(ModelMap model) {
         ContactForm bean = new ContactForm();
         model.addAttribute("contactForm", bean);
         model.addAttribute("title", "Contact Us");
         return "contact";
     }
 
     @RequestMapping(method = RequestMethod.POST, value = "contact")
     public String contactPost(@Valid ContactForm contactForm, BindingResult result, ModelMap model) {
         if(result.hasErrors()){
             return "contact";
         }else{
         	String error = eh.sendMessage(contactForm);
         	if(error != null){
         		model.addAttribute("error",error);
         	}else{
     			model.addAttribute("success","Thank you for contacting us!  You will recieve a receipt of your request via email shortly.");
         	}
             return "contact";
         }
     }
 
     @RequestMapping(method = RequestMethod.GET, value = "services")
     public String services(ModelMap model) {
 
         model.addAttribute("title", "Services");
         return "services";
 
     }
 
      @RequestMapping(method = RequestMethod.GET, value = "servicearea")
     public String serviceArea(ModelMap model) {
 
         model.addAttribute("title", "Service Area");
         return "servicearea";
 
     }
 
    @RequestMapping(method = RequestMethod.GET, value = "clients")
     public String clients(ModelMap model) {
 	   	   
         model.addAttribute("title", "Our Clients");
         return "clients";
 
     }
    
    @RequestMapping(method = RequestMethod.GET, value = "payments")
    public String pay(@RequestParam(value = "action", required = false) String action, ModelMap model) {
 	   
       model.addAttribute("title", "Payments");
        if("cancel".equals(action)){
     	   model.addAttribute("warning","You have successfully cancelled your payment.");
     	   System.out.println(action);
        }
        return "payments";
 
    }
    
    @RequestMapping(method = RequestMethod.POST, value = "payments")
    public String payComplete(@RequestParam(value = "payment_gross")String amount, 
 	   	@RequestParam(value="payer_email")String email, ModelMap model) {
 	   
 	   model.addAttribute("success", "Thank you for your payment of $" + amount + ". You should receive email confirmation to '" + email +  "'.");
       model.addAttribute("title", "Payments");
        return "payments";
 
    }
 }
