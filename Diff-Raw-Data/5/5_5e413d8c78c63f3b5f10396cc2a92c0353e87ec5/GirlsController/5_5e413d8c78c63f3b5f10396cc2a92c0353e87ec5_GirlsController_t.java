 package com.acme.hooters;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestParam;
 
 import com.acme.hooters.model.Girl;
 import com.acme.hooters.service.GirlRepository;
 
 @Controller
 public class GirlsController {
 
     @Autowired
     private GirlRepository girlRepository;
     
     @RequestMapping("/girls/list")
     public String list(Model model) {
         
         
         model.addAttribute("girls", girlRepository.getAllGirls());
         
         return "girls/list";
     }
     
    @RequestMapping("/girls/remove")
     public String remove(@RequestParam String name) {
         
         girlRepository.remove(name);
 
         return "redirect:/girls/list";
     }
     
    @RequestMapping("/girls/add")
     public String add(@RequestParam String name, @RequestParam String basket, @RequestParam Integer breast) {
         
         Girl newGirl = new Girl(name, breast, basket);
         girlRepository.addGirl(newGirl);
         
         return "redirect:/girls/list";        
     }
 }
