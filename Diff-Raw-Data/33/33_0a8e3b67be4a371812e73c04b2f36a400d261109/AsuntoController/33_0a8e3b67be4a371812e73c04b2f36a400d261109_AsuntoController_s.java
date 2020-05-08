 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package wad.controller;
 
 import javax.validation.Valid;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import wad.domain.Asunto;
 import wad.domain.Henkilo;
 import wad.domain.Lomake;
 import wad.service.AsuntoPalvelu;
 import wad.service.HenkiloPalvelu;
 
 /**
  *
  * @author tonykova
  */
 @Controller
 public class AsuntoController {
     @Autowired
     AsuntoPalvelu asuntoPalvelu;
     
     @Autowired
     HenkiloPalvelu henkiloPalvelu;
     
     
     @RequestMapping("*")
     public String ohjaaPerusnakymaan() {
         return "redirect:/asunnot";
     }
     @RequestMapping(value = "asunnot", method = RequestMethod.POST)
     public String kasitteleUusiAsunto(@Valid @ModelAttribute Lomake lomake, BindingResult result) {
         if (result.hasErrors()) {
             return "asunnot";
         }
         Asunto asunto = lomake.teeAsunto();
         Henkilo henkilo = lomake.teeHenkilo();
         asunto.setHenkilo(henkilo);
        henkilo.lisaaKohde(asunto);
         asuntoPalvelu.lisaa(asunto);
         henkiloPalvelu.lisaa(henkilo);
         return "redirect:/asunnot";
     }
     @RequestMapping(value = "asunnot", method=RequestMethod.GET)
     public String naytaAsunnot(Model model) {
        model.addAttribute("lomake", new Lomake());
        model.addAttribute("asunnot", asuntoPalvelu.listaa());
        return "asunnot";
     }
 }
