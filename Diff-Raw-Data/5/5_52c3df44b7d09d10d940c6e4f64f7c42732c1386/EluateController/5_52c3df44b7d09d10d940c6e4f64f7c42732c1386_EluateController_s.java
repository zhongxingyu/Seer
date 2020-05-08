 package ohtu.radioaine.controller;
 
 import java.util.ArrayList;
 import java.util.List;
 import javax.validation.Valid;
 import ohtu.radioaine.domain.Batch;
 import ohtu.radioaine.domain.Eluate;
 import ohtu.radioaine.domain.EluateFormObject;
 import ohtu.radioaine.domain.Substance;
 import ohtu.radioaine.service.*;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import ohtu.radioaine.tools.Time;
 import org.springframework.ui.Model;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMethod;
 
 /**
  * Controllers for eluate creation ja viewing
  *
  * @author rmjheino
  */
 @Controller
 public class EluateController {
 
     @Autowired
     private EluateService eluateService;
     @Autowired
     private BatchService batchService;
     @Autowired
     private EventService eventService;
     @Autowired
     private SubstanceService substanceService;
     @Autowired
     private StorageService storageService;
     int GENERATOR = 1;
     int KIT = 0;
     int OTHER = 2;
 
     @RequestMapping(value = "eluate/{id}", method = RequestMethod.GET)
     public String getEluateById(@PathVariable Integer id, Model model) {
         model.addAttribute("eluate", eluateService.read(id));
         return "eluateView";
     }
 
     @RequestMapping(value = "createEluate", method = RequestMethod.GET)
     public String createEluate(Model model) {
         model.addAttribute("eluate", new EluateFormObject());
         model.addAttribute("generators", batchService.getBatchesByType(GENERATOR));
         model.addAttribute("others", batchService.getBatchesByType(OTHER));
         model.addAttribute("storages",  storageService.list());
         return "createEluate";
     }
 
     private List<Batch> getSpecificTypesFromBatches(List<Batch> batches, int type) {
         List<Batch> typeList = new ArrayList<Batch>();
         for (Batch batch : batches) {
             if (batch.getSubstance().getType() == type) {
                 typeList.add(batch);
             }
         }
         return typeList;
     }
 
     @RequestMapping(value = "createEluate", method = RequestMethod.POST)
     public String newEluate(@Valid @ModelAttribute("eluate") EluateFormObject efo, BindingResult result) {
         if (result.hasErrors()) {
             System.out.println(result);
             return "createEluate";
         }
         Eluate newEluate = eluateService.createOrUpdate(createEluate(efo));
         return "redirect:/frontpage";
     }
     
     @RequestMapping(value = "modifyEluate/{id}", method = RequestMethod.GET)
     public String modifyEluate(Model model, @PathVariable Integer id) {
        model.addAttribute("eluateForm", new EluateFormObject());
        model.addAttribute("generators", batchService.getBatchesByType(GENERATOR));
        model.addAttribute("others", batchService.getBatchesByType(OTHER));
        model.addAttribute("storages",  storageService.list()); 
       model.addAttribute("elaute", eluateService.read(id));
         return "eluateUpdateView";
     }
     
     @RequestMapping(value = "modifyEluate/{id}", method = RequestMethod.POST)
    public String modifyEluate(@Valid @ModelAttribute("eluate") EluateFormObject efo, BindingResult result) {
         if (result.hasErrors()) {
             System.out.println(result);
             return "createEluate";
         }
         Eluate newEluate = eluateService.createOrUpdate(createEluate(efo));
         return "redirect:/frontpage";
     }
 
     private Eluate createEluate(EluateFormObject efo) {
         Eluate eluate = new Eluate();
         if(efo.getStrength().equals("")){
             eluate.setStrength(0.0);
         }
         else{
             eluate.setStrength(Double.parseDouble(efo.getStrength()));
         }
         eluate.setUnit(efo.getUnit());
         eluate.setVolume(efo.getVolume());
         eluate.setTimestamp(Time.parseTimeStamp(efo.getDate() + " " + efo.getHours() + ":" + efo.getMinutes()));
         eluate.setSignature(efo.getSignature());
         eluate.setNote(efo.getNote());
         eluate.setStorageLocation(efo.getStorageLocation());
 
         List<Batch> generators = new ArrayList<Batch>();
         int[] generatorsTable = efo.getGenerators();
         for (int i = 0; i < generatorsTable.length; ++i) {
 
             generators.add(batchService.read(generatorsTable[i]));
         }
 
         List<Batch> others = new ArrayList<Batch>();
         int[] othersTable = efo.getOthers();
         for (int i = 0; i < othersTable.length; ++i) {
 
             others.add(batchService.read(othersTable[i]));
         }
 //        updateAmounts(generators, others);
         eluate.setGenerators(generators);
         eluate.setOthers(others);
         return eluate;
     }
 
     private void updateAmounts(List<Batch> generators, List<Batch> others) {
         for (Batch gen : generators) {
             Batch batch = batchService.read(gen.getId());
             Substance substance = (Substance) substanceService.read(batch.getSubstance().getId());
             batch.useOne();
             substance.useOne();
             batchService.createOrUpdate(batch);
             substanceService.createOrUpdate(substance);
         }
         for (Batch other : others) {
             Batch batch = batchService.read(other.getId());
             Substance substance = (Substance) substanceService.read(batch.getSubstance().getId());
             batch.useOne();
             substance.useOne();
             batchService.createOrUpdate(batch);
             substanceService.createOrUpdate(substance);
         }
     }
 }
