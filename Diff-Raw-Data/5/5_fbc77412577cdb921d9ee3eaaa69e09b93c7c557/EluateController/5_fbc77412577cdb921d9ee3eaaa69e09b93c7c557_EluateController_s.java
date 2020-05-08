 package ohtu.radioaine.controller;
 
 import java.util.ArrayList;
 import java.util.List;
 import javax.validation.Valid;
 import ohtu.radioaine.domain.*;
 import ohtu.radioaine.service.*;
 import ohtu.radioaine.tools.EventHandler;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import ohtu.radioaine.tools.Time;
 import org.springframework.ui.Model;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.bind.annotation.*;
 
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
     private RadioMedService radioMedicineService;
     @Autowired
     private StorageService storageService;
     long GENERATOR = 1;
     long KIT = 0;
     long OTHER = 2;
 
     @RequestMapping(value = "eluate/{id}", method = RequestMethod.GET)
     public String getEluateByIdCTRL(@PathVariable Long id, Model model) {
         model.addAttribute("eluate", eluateService.read(id));
         model.addAttribute("storages", storageService.list());
         return "eluateView";
     }
 
     @RequestMapping(value = "createEluate", method = RequestMethod.GET)
     public String createEluateCTRL(Model model) {
         model.addAttribute("eluate", new EluateFormObject());
         model.addAttribute("generators", batchService.getBatchesByType(GENERATOR));
         model.addAttribute("others", batchService.getBatchesByType(OTHER));
         model.addAttribute("storages", storageService.list());
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
     public String newEluateCTRL(@Valid @ModelAttribute("eluate") EluateFormObject efo, BindingResult result) {
         if (result.hasErrors()) {
             System.out.println(result);
             return "createEluate";
         }
         createNewEluateAndEvent(efo);
         return "redirect:/frontpage";
     }
 
     private void createNewEluateAndEvent(EluateFormObject efo) {
         Eluate newEluate = eluateService.createOrUpdate(createEluate(efo));
         Event event = EventHandler.newEluateEvent(newEluate, efo.getSignature());
         eventService.createOrUpdate(event);
     }
 
     private Eluate createEluate(EluateFormObject efo) {
         Eluate eluate = new Eluate();
         if (efo.getStrength().equals("")) {
             eluate.setStrength(0.0);
         } else {
             eluate.setStrength(Double.parseDouble(efo.getStrength()));
         }
         eluate.setUnit(efo.getUnit());
         eluate.setVolume(Double.parseDouble(efo.getVolume()));
         eluate.setTimestamp(Time.parseTimeStamp(efo.getDate() + " " + efo.getHours() + ":" + efo.getMinutes()));
         eluate.setSignature(efo.getSignature());
         eluate.setNote(efo.getNote());
         eluate.setStorageLocation(efo.getStorageLocation());
 
         List<Batch> generators = new ArrayList<Batch>();
         Long[] generatorsTable = efo.getGenerators();
         for (int i = 0; i < generatorsTable.length; ++i) {
             if (generatorsTable[i] != null) {
                 generators.add(batchService.read(generatorsTable[i]));
             }
         }
 
         List<Batch> others = new ArrayList<Batch>();
         Long[] othersTable = efo.getOthers();
         for (int i = 0; i < othersTable.length; ++i) {
             if (othersTable[i] != null) {
                 others.add(batchService.read(othersTable[i]));
             }
         }
         eluate.setGenerators(generators);
         eluate.setOthers(others);
         eluate.setName();
         return eluate;
     }
 
     @RequestMapping(value = "modifyEluate/{id}", method = RequestMethod.GET)
     public String modifyEluateCTRL(Model model, @PathVariable Long id) {
         model.addAttribute("eluateForm", new EluateFormObject());
         model.addAttribute("generators", batchService.getBatchesByType(GENERATOR));
         model.addAttribute("others", batchService.getBatchesByType(OTHER));
         model.addAttribute("storages", storageService.list());
         model.addAttribute("eluate", eluateService.read(id));
         return "eluateUpdateView";
     }
 
     @RequestMapping(value = "modifyEluate/{id}", method = RequestMethod.POST)
     public String modifyEluateCTRL(@Valid @ModelAttribute("eluateForm") EluateFormObject efo,
             BindingResult result,
             @PathVariable Long id) {
         if (result.hasErrors()) {
             System.out.println(result);
             return "createEluate";
         }
         updateEluate(id, efo);
         return "redirect:/frontpage";
     }
 
     @RequestMapping("removeEluateRequest/{id}")
     public String removeEluate(Model model, @PathVariable Long id) {
         model.addAttribute("eluate", eluateService.read(id));
         model.addAttribute("storages", storageService.list());
         return "eluateRemovalView";
     }
 
     @RequestMapping(value = "removeEluate/{id}", method = RequestMethod.POST)
     public String removeRadioMed(Model model, @RequestParam String reason,
             @RequestParam String remover,
             @PathVariable Long id) {
         Eluate eluate = eluateService.read(id);
         List<RadioMedicine> radiomeds = radioMedicineService.list();
         boolean radiomedForEluateFound = false;
         for (RadioMedicine radiomed : radiomeds) {
             List<Eluate> eluates = radiomed.getEluates();
             for (Eluate eluateCandidate : eluates) {
                 if (eluateCandidate.getId() == eluate.getId()) {
                     radiomedForEluateFound = true;
                     break;
                 }
             }
             if (radiomedForEluateFound) {
                 break;
             }
         }
         if (radiomedForEluateFound) {
             model.addAttribute("eluate", eluateService.read(id));
             model.addAttribute("storages", storageService.list());
             model.addAttribute("removeError", 1);
             return "eluateRemovalView";
         }
         Event event = EventHandler.removeEluateEvent(reason, remover, eluateService.read(id));
         eventService.createOrUpdate(event);
         eluateService.delete(id);
         return "redirect:/frontpage";
     }
 
     private void updateEluate(Long id, EluateFormObject efo) {
         Eluate eluate = eluateService.read(id);
         if (efo.getStrength().equals("")) {
             eluate.setStrength(0.0);
         } else {
             eluate.setStrength(Double.parseDouble(efo.getStrength()));
         }
         eluate.setUnit(efo.getUnit());
        if (efo.getStrength().equals("")) {
             eluate.setVolume(0.0);
         } else {
            eluate.setStrength(Double.parseDouble(efo.getVolume()));
         }
         eluate.setTimestamp(Time.parseTimeStamp(efo.getDate() + " " + efo.getHours() + ":" + efo.getMinutes()));
         eluate.setNote(efo.getNote());
         eluate.setStorageLocation(efo.getStorageLocation());
 
         List<Batch> generators = new ArrayList<Batch>();
         Long[] generatorsTable = efo.getGenerators();
         for (int i = 0; i < generatorsTable.length; ++i) {
             if (generatorsTable[i] != null) {
                 generators.add(batchService.read(generatorsTable[i]));
             }
         }
 
         List<Batch> others = new ArrayList<Batch>();
         Long[] othersTable = efo.getOthers();
         for (int i = 0; i < othersTable.length; ++i) {
             if (othersTable[i] != null) {
                 others.add(batchService.read(othersTable[i]));
             }
         }
 //        updateAmounts(generators, others);
         eluate.setGenerators(null);
         eluate.setOthers(null);
         eluateService.createOrUpdate(eluate);
         eluate.setGenerators(generators);
         eluate.setOthers(others);
         eluateService.createOrUpdate(eluate);
         Event event = EventHandler.updateEluateEvent(eluate, efo.getSignature());
         eventService.createOrUpdate(event);
     }
 }
