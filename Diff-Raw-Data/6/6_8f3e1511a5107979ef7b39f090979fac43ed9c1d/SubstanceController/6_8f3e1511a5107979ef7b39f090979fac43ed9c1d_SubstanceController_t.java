 package ohtu.radioaine.controller;
 
 import javax.validation.Valid;
 import ohtu.radioaine.domain.Substance;
 import ohtu.radioaine.domain.SubstanceFormObject;
 import ohtu.radioaine.service.BatchService;
 import ohtu.radioaine.service.EventService;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import ohtu.radioaine.service.SubstanceService;
 import org.springframework.ui.Model;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMethod;
 
 @Controller
 public class SubstanceController {
 
     @Autowired
     private SubstanceService substanceService;
     @Autowired
     private BatchService batchService;
     @Autowired
     private EventService eventService;
     
     @RequestMapping(value = "substance/{id}", method = RequestMethod.GET)
     public String getSubstanceByIdCTRL(@PathVariable Long id, Model model) {
         Substance substance = (Substance) substanceService.read(id);
         model.addAttribute("substance", substance);
         model.addAttribute("substanceBatches", batchService.listSubstanceBatches(id));
         model.addAttribute("substanceHistory", eventService.list(substance.getName()));
         return "substanceBatches";
     }
 
     @RequestMapping(value = "substance", method = RequestMethod.POST)
     public String addSubstanceCTRL(@Valid @ModelAttribute("substance") SubstanceFormObject sfo, BindingResult result) {
         if (result.hasErrors()) {
             return "addSubstanceView";
         }
         substanceService.createOrUpdate(createSubstance(sfo));
         return "redirect:/substanceList";
     }
 
     private Substance createSubstance(SubstanceFormObject sfo) {
         Substance substance = new Substance();
         substance.setType(sfo.getType());
         substance.setName(sfo.getName());
         substance.setManufacturer(sfo.getManufacturer());
         substance.setSupplier(sfo.getSupplier());
         substance.setAlertLimit1(sfo.getAlertLimit1());
         substance.setAlertLimit2(sfo.getAlertLimit2());
         substance.setTotalAmount(0);
         substance.setHalflife(sfo.getHalflife());
 
         return substance;
     }
 
     @RequestMapping(value = "substance", method = RequestMethod.GET)
     public String listaaCTRL(Model model) {
         model.addAttribute("substances", substanceService.list());
         return "substanceViewTest";
     }
 
     @RequestMapping(value = "updateSubstance/{id}", method = RequestMethod.POST)
     public String updateSubstanceCTRL(@Valid @ModelAttribute("substance") SubstanceFormObject sfm,
             BindingResult result,
             Model model,
             @PathVariable Long id) {
         updateSubstance(id, sfm);
         return "redirect:/updateSubstance/" + id;
     }
 
     private void updateSubstance(Long id, SubstanceFormObject sfm) {
        Substance substance = (Substance) substanceService.read(id);
         substance.setName(sfm.getName());
         substance.setType(sfm.getType());
         substance.setAlertLimit1(sfm.getAlertLimit1());
         substance.setAlertLimit2(sfm.getAlertLimit2());
         substance.setManufacturer(sfm.getManufacturer());
         substance.setSupplier(sfm.getSupplier());
         substance.setHalflife(sfm.getHalflife());
         substanceService.createOrUpdate(substance);
     }
 
     @RequestMapping(value = "updateSubstance/{id}", method = RequestMethod.GET)
     public String updateSubstanceViewCTRL(Model model, @PathVariable Long id) {
         System.out.println(id);
         model.addAttribute("substance", substanceService.read(id));
         return "substanceUpdateView";
     }
 
     @RequestMapping(value = "addSubstance", method = RequestMethod.GET)
     public String addSubstanceViewCTRL(Model model) {
         model.addAttribute("substance", new SubstanceFormObject());
         return "addSubstanceView";
     }
     
     
 }
