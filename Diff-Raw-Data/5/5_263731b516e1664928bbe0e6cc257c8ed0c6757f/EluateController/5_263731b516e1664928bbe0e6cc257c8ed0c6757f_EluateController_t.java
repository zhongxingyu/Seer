 package ohtu.radioaine.controller;
 
 import java.util.ArrayList;
 import java.util.List;
 import javax.validation.Valid;
 import ohtu.radioaine.domain.Batch;
 import ohtu.radioaine.domain.Eluate;
 import ohtu.radioaine.domain.EluateFormObject;
 import ohtu.radioaine.domain.Substance;
 import ohtu.radioaine.service.BatchService;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import ohtu.radioaine.service.EluateService;
 import ohtu.radioaine.service.EventService;
 import ohtu.radioaine.service.SubstanceService;
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
     int GENERATOR = 1;
     int KIT = 0;
     int OTHER = 2;
 
     @RequestMapping(value = "createEluate", method = RequestMethod.GET)
     public String createEluate(Model model) {
         model.addAttribute("eluate", new EluateFormObject());
         model.addAttribute("generators", getSpecificTypesFromSubstances(substanceService.list(), GENERATOR));
 //        List<Batch> batches = batchService.list();
 //        model.addAttribute("kits", getSpecificTypesFromBatches(batches, KIT));
 //        model.addAttribute("solvents", getSpecificTypesFromBatches(batches, OTHER));
         model.addAttribute("generators", batchService.getBatchesByType(GENERATOR));
         model.addAttribute("kits", batchService.getBatchesByType(KIT));
         model.addAttribute("others", batchService.getBatchesByType(OTHER));
         return "createEluate";
     }
 
     private List<Substance> getSpecificTypesFromSubstances(List<Substance> substances, int type) {
         List<Substance> typeList = new ArrayList<Substance>();
         for (Substance substance : substances) {
             if (substance.getType() == type) {
                 typeList.add(substance);
             }
         }
         return typeList;
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
             return "createEluate";
         }
         Eluate newEluate = eluateService.createOrUpdate(createEluate(efo));
         return "redirect:/frontpage";
     }
 
     @RequestMapping("Eluate/{id}")
     public String eluateView(@PathVariable Integer id) {
         return "frontpage";
     }
 
     /**
      *
      * @param efo
      * @return
      */
     private Eluate createEluate(EluateFormObject efo) {
         Eluate eluate = new Eluate();
         System.out.println(efo.getStrength());
         eluate.setStrength(efo.getStrength());
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
 
         List<Batch> kits = new ArrayList<Batch>();
        int[] kitsTable = efo.getKits();
         for (int i = 0; i < kitsTable.length; ++i) {
             kits.add(batchService.read(kitsTable[i]));
         }
 
         List<Batch> others = new ArrayList<Batch>();
        int[] othersTable = efo.getOthers();
         for (int i = 0; i < othersTable.length; ++i) {
             others.add(batchService.read(othersTable[i]));
         }
 
         eluate.setGenerators(generators);
         eluate.setOthers(others);
         eluate.setKits(kits);
         return eluate;
     }
 }
