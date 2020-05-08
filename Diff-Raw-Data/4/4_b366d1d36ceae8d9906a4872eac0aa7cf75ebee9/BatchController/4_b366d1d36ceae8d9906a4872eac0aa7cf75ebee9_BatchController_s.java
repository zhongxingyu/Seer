 /*
  * Contains following controllers for batch page:
  *  - batch/{id}: fetches batch by id from db, gives it in model to view 'batchView'
  *  - batch: fetches all batches from db, gives them in model to view 'batch' 
  *  - addBatch: 
  */
 /**
  *
  */
 package ohtu.radioaine.controller;
 
 import javax.validation.Valid;
 import ohtu.radioaine.domain.*;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import ohtu.radioaine.service.BatchService;
 import ohtu.radioaine.service.EventService;
 import ohtu.radioaine.service.SubstanceService;
 import ohtu.radioaine.tools.EventHandler;
 import ohtu.radioaine.tools.EventHandler3;
 import org.springframework.ui.Model;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMethod;
 import ohtu.radioaine.tools.Time;
 import org.springframework.web.bind.annotation.*;
 
 /**
  * Controllers for batch creation and viewing
  *
  * @author rmjheino
  */
 @Controller
 public class BatchController {
 
     @Autowired
     private BatchService batchService;
     @Autowired
     private SubstanceService substanceService;
     @Autowired
     private EventService eventService;
 
     @RequestMapping(value = "batch/{id}", method = RequestMethod.GET)
     public String getBatchById(@PathVariable Integer id, Model model) {
         model.addAttribute("batch", batchService.read(id));
         return "batchView";
     }
     
     @RequestMapping(value = "doCheck/{id}+{sid}", method = RequestMethod.POST)
     public String qualityCheck(@PathVariable Integer id, 
     @PathVariable Integer sid, 
     @RequestParam Integer qualityCheck) {
         Batch temp = batchService.read(id);
         temp.setQualityCheck(qualityCheck);
         batchService.createOrUpdate(temp);
         return "redirect:/substance/"+sid;
     }
 
     @RequestMapping(value = "batch", method = RequestMethod.GET)
     public String batchList(Model model) {
         model.addAttribute("batches", batchService.list());
         return "batchView";
     }
 
     @RequestMapping(value = "addBatch", method = RequestMethod.GET)
     public String addbatchView(Model model) {
         model.addAttribute("batch", new BatchFormObject());
         model.addAttribute("substances", substanceService.list());
         return "addBatchView";
     }
 
     @RequestMapping(value = "batch", method = RequestMethod.POST)
     public String addBatch(@Valid @ModelAttribute("batch") BatchFormObject bfm, BindingResult result) {
         if (result.hasErrors()) {
             return "addBatchView";
         }
         Batch batch = createBatch(bfm);
         Batch temp = batchService.read(batch.getBatchNumber(), bfm.getSubstance());
         if (temp == null) {
             batch = batchService.createOrUpdate(batch);
             Event event = EventHandler.newBatchEvent(batch);
             eventService.createOrUpdate(event);
         } else {
             batch = updateBatchSaato(temp.getId(), bfm);
         }
         return "redirect:/batch/" + batch.getId();
     }
 
     
 
     //Batchin päivittämiseen kesken
     @RequestMapping(value = "updateBatch/{id}")
     public String batchUpdateRequest(Model model, @PathVariable Integer id) {
         model.addAttribute("substances", substanceService.list());
         model.addAttribute("batch", batchService.read(id));
         return "batchUpdateView";
     }
     //Batchin päivittämiseen keskenlog
 
     @RequestMapping(value = "updateBatch/{id}", method = RequestMethod.POST)
     public String batchUpdate(@Valid @ModelAttribute("batch") BatchFormObject bfm,
             BindingResult result,
             Model model,
             @PathVariable Integer id) {
         if (result.hasErrors()) {
             return "redirect:/updateBatch/" + id;
         }
         updateBatch(id, bfm);
         
         return "redirect:/batch/" + id;
     }
 
     private Batch updateBatch(Integer id, BatchFormObject bfo) {
         int temp = 0;
         for(int i=0; i < bfo.getStorageLocations().length; i++) {
             temp += bfo.getStorageLocations()[i][1];
             
         }
         bfo.setAmount(temp);
         Batch batch = batchService.read(id);
         batch.setStorageLocations(bfo.getStorageLocations());
         batch.setAmount(bfo.getAmount());
         batch.setSubstanceVolume(bfo.getSubstanceVolume());
         batch.setBatchNumber(bfo.getBatchNumber());
         batch.setNote(bfo.getNote());
         
         
         Substance substance = batch.getSubstance();
         
         if(batch.getSubstance().getId() != bfo.getSubstance()){
             Substance newSubstance = (Substance) substanceService.read(bfo.getSubstance());
            substance.setTotalAmount(substance.getTotalAmount() - batch.getAmount());
             newSubstance.setTotalAmount(newSubstance.getTotalAmount() + batch.getAmount());
             batch.setSubstance(newSubstance);
             substanceService.createOrUpdate(newSubstance);  
         }
         else{
             int amountChange = amountChange(batch, bfo);
             substance.setTotalAmount(substance.getTotalAmount() + amountChange);
         }
         batch = batchService.createOrUpdate(batch);
         substanceService.createOrUpdate(substance);
 //        Event3 event = EventHandler3.updateBatchEvent(batch, bfo.getUserName());
 //        eventService.createOrUpdate(event);
         Event event = EventHandler.updateBatchEvent(batch);
         eventService.createOrUpdate(event);
         return batch;
     }
 
     private int amountChange(Batch batch, BatchFormObject bfm) {
         int tempAmount;
         if (batch.getAmount() > bfm.getAmount()) {
             tempAmount = -(batch.getAmount() - bfm.getAmount());
         } else if (batch.getAmount() < bfm.getAmount()) {
             tempAmount = (bfm.getAmount() - batch.getAmount());
         } else {
             tempAmount = 0;
         }
         return tempAmount;
     }
 
     @RequestMapping(value = "batchDelete/{id}", method = RequestMethod.POST)
     public String deleteBatch(@RequestParam String name, @RequestParam Integer amount, @PathVariable Integer id) {
         Batch batch = batchService.read(id);
         Substance substance = batch.getSubstance();
 
         int total = batch.getAmount() - amount;
         if (total >= 0 && name.length() >= 1) {
             substance.setTotalAmount(substance.getTotalAmount() - amount);
             substanceService.createOrUpdate(substance);
             batch.setAmount(total);
             batchService.createOrUpdate(batch);
         }
         return "redirect:/batch/" + id;
     }
 
     /**
      *
      * @param bfo
      * @return
      */
     private Batch createBatch(BatchFormObject bfo) {
         Batch batch = new Batch();
         batch.setBatchNumber(bfo.getBatchNumber());
         batch.setNote(bfo.getNote());
         batch.setArrivalDate(Time.parseDate(bfo.getArrivalDate()));
         batch.setExpDate((Time.parseDate(bfo.getExpDate())));
         
         //Counts and sets the correct amount of the batch from storageLocations
         int temp = 0;
         for(int i=0; i < bfo.getStorageLocations().length; i++)
             temp += bfo.getStorageLocations()[i][1];
         
         bfo.setAmount(temp);
         
         batch.setAmount(bfo.getAmount());
         batch.setSubstanceVolume(bfo.getSubstanceVolume());
         batch.setStorageLocations(bfo.getStorageLocations());
         Substance substance = (Substance) substanceService.read(bfo.getSubstance());
         substance.setTotalAmount(substance.getTotalAmount() + bfo.getAmount());
         substanceService.createOrUpdate(substance);
         batch.setSubstance(substance);
         batch.setManufacturer(substance.getManufacturer());
         batch.setSupplier(substance.getSupplier());
         return batch;
     }
 
     private Batch updateBatchSaato(int id, BatchFormObject bfm) {
         Batch batch = batchService.read(id);
         batch.setAmount(batch.getAmount()+bfm.getAmount());
         batch.setNote(batch.getNote()+"\n"+bfm.getNote());
         Event event = EventHandler.addToBatchEvent(batch);
         eventService.createOrUpdate(event);
         return batchService.createOrUpdate(batch);
         
     }
 }
