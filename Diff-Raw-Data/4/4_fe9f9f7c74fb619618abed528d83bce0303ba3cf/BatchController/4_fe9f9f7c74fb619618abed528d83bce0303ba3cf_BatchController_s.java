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
 
 import java.util.List;
 import javax.validation.Valid;
 import ohtu.radioaine.domain.*;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import ohtu.radioaine.service.BatchService;
 import ohtu.radioaine.service.EventService;
 import ohtu.radioaine.service.StorageService;
 import ohtu.radioaine.service.SubstanceService;
 import ohtu.radioaine.tools.EventHandler;
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
     @Autowired
     private StorageService storageService;
 
     @RequestMapping(value = "batch/{id}", method = RequestMethod.GET)
     public String getBatchByIdCTRL(@PathVariable Long id, Model model) {
         model.addAttribute("batch", batchService.read(id));
         model.addAttribute("storages", storageService.list());
         return "batchView";
     }
 
     @RequestMapping(value = "doCheck/{id}+{sid}", method = RequestMethod.POST)
     public String qualityCheckCTRL(@PathVariable Long id,
             @PathVariable Long sid,
             @RequestParam String sig,
             @RequestParam Integer qualityCheckStatus) {
         if (sig.length() < 2) {
             if (sid <= 0) {
                 return "redirect:/batch/" + id;
             }
             return "redirect:/substance/" + sid;
         }
         setQualityCheck(id, qualityCheckStatus, sig);
         if (sid <= 0) {
             return "redirect:/batch/" + id;
         }
         return "redirect:/substance/" + sid;
     }
 
     private void setQualityCheck(Long id, Integer qualityCheck, String sig) {
         Batch batch = batchService.read(id);
         batch.setQualityCheck(qualityCheck);
         batchService.createOrUpdate(batch);
         updateSubstance(batch.getSubstance());
         Event event = EventHandler.qualityCheckEvent(batch, sig);
         eventService.createOrUpdate(event);
     }
 
     @RequestMapping(value = "batch", method = RequestMethod.GET)
     public String batchListCTRL(Model model) {
         model.addAttribute("batches", batchService.list());
         return "batchView";
     }
 
     @RequestMapping(value = "addBatch", method = RequestMethod.GET)
     public String addbatchViewCTRL(Model model) {
         model.addAttribute("batch", new BatchFormObject());
         model.addAttribute("substances", substanceService.list());
         model.addAttribute("storages", storageService.list());
         addNames(model);
         return "addBatchView";
     }
 
     private void addNames(Model model) {
         String names = "'";
         for (int i = 0; i < storageService.storageNamesList().size(); i++) {
             if (!storageService.list().get(i).isHidden()) {
                 names += storageService.storageNamesList().get(i) + "^separate^";
             } else {
                 names += "^hidden^^separate^";
             }
         }
         names += "'";
         model.addAttribute("storageNames", names);
     }
 
     @RequestMapping(value = "removeFromBatch/{id}", method = RequestMethod.GET)
     public String removeFromBatchViewCTRL(@PathVariable Long id, Model model) {
         model.addAttribute("batch", batchService.read(id));
         model.addAttribute("storages", storageService.list());
         return "removeFromBatchView";
     }
 
     @RequestMapping(value = "removeFromBatch/{id}", method = RequestMethod.POST)
     public String removeFromBatchCTRL(@PathVariable Long id, @RequestParam Integer[] amounts, @RequestParam String remover, @RequestParam String reason) {
         System.out.println("JUTTUU "+amounts[0]);
         removeItemsFromBatch(id, amounts, reason, remover);
         return "redirect:/batch/" + id;
     }
 
     private void removeItemsFromBatch(Long id, Integer[] amounts, String reason, String remover) {
         Batch temp = batchService.read(id);
         Substance substance = (Substance)substanceService.read(temp.getSubstance().getId());
         Long[][] locs = temp.getStorageLocations();
         int tempTotalAmount = 0;
         int totalRemoved = 0;
         for (int i = 0; i < amounts.length; ++i) {
             if (amounts[i] != null && locs[i][1] != null) {
                 System.out.println("TÄÄL");
                 if (amounts[i] > 0 && locs[i][1] >= amounts[i]) {
                     totalRemoved += amounts[i];
                     System.out.println("Tuut "+locs[i][1]);
                     locs[i][1] -= amounts[i];
                     System.out.println("Tuut2 "+locs[i][1]);
                 }
             }
             if (locs[i][1] != null) {
                 tempTotalAmount += locs[i][1];
             }
         }
         temp.setAmount(tempTotalAmount);
         temp.setStorageLocations(locs);
         substance.setTotalAmount(substance.getTotalAmount() - totalRemoved);
         batchService.createOrUpdate(temp);
         substanceService.createOrUpdate(substance);
         Event event = EventHandler.removeFromBatchEvent(temp, remover, reason, totalRemoved);
         eventService.createOrUpdate(event);
     }
 
     @RequestMapping(value = "batch", method = RequestMethod.POST)
     public String addBatchCTRL(@Valid @ModelAttribute("batch") BatchFormObject bfo, BindingResult result) {
         if (result.hasErrors()) {
             System.out.println(result);
             return "redirect:/addBatch";
         }
         Batch batch = addBatchToDatabase(bfo);
         return "redirect:/batch/" + batch.getId();
     }
 
     private Batch addBatchToDatabase(BatchFormObject bfo) {
         Batch batch = createBatch(bfo);
         Batch temp = batchService.read(batch.getBatchNumber(), bfo.getSubstance());
         if (temp == null) {
             batch = batchService.createOrUpdate(batch);
             Event event = EventHandler.newBatchEvent(batch, bfo.getSignature());
             eventService.createOrUpdate(event);
         } else {
             batch = addToBatch(temp.getId(), bfo);
         }
         return batch;
     }
 
     @RequestMapping(value = "updateBatch/{id}")
     public String batchUpdateRequestCTRL(Model model, @PathVariable Long id) {
         model.addAttribute("substances", substanceService.list());
        model.addAttribute("batch", batchService.read(id));
         model.addAttribute("storages", storageService.list());
         setStorageNames(model);
         return "batchUpdateView";
     }
 
     private void setStorageNames(Model model) {
         String names = "'";
         for (int i = 0; i < storageService.storageNamesList().size(); i++) {
             names += storageService.storageNamesList().get(i) + "^separate^";
         }
         names += "'";
         model.addAttribute("storageNames", names);
     }
 
     @RequestMapping(value = "updateBatch/{id}", method = RequestMethod.POST)
     public String batchUpdateCTRL(@Valid @ModelAttribute("batch") BatchFormObject bfm,
             BindingResult result,
             Model model,
             @PathVariable Long id) {
         int newTotalAmount = countAmount(bfm);
         Batch batchToUpdate = batchService.read(id);
         if (totalAmountDiffers(result, batchToUpdate, newTotalAmount)) {
 //            System.out.println(result);
             return "redirect:/updateBatch/" + id;
         }
         System.out.println("ZZZ3");
         updateBatch(id, bfm);
         return "redirect:/batch/" + id;
     }
 
     private boolean totalAmountDiffers(BindingResult result, Batch temp, int newTotalAmount) {
         return result.hasErrors() || temp.getAmount() != newTotalAmount;
     }
 
     private int countAmount(BatchFormObject bfm) {
         //Checks if the new total amount differs from the old total amount and if it does, the update fails
         int newTotalAmount = 0;
         for (int i = 0; i < bfm.getStorageLocations().length; i++) {
             System.out.println("storagelocationi on : " + bfm.getStorageLocations().length);
             if (bfm.getStorageLocations()[i][1] != null) {
                 newTotalAmount += bfm.getStorageLocations()[i][1];
             }
         }
         return newTotalAmount;
     }
 
     private Batch updateBatch(Long id, BatchFormObject bfo) {
         Batch batch = batchService.read(id);
         Substance substance = batch.getSubstance();
         batch.setStorageLocations(bfo.getStorageLocations());
         batch.setSubstanceVolume(bfo.getSubstanceVolume());
         batch.setBatchNumber(bfo.getBatchNumber());
         batch.setQualityCheck(bfo.getQualityCheck());
         batch.setNote(bfo.getNote());
         batch.setArrivalDate(Time.parseDate(bfo.getArrivalDate()));
         batch.setExpDate(Time.parseDate(bfo.getExpDate()));
         long temp = 0;
         for (int i = 0; i < bfo.getStorageLocations().length; i++) {
             if (bfo.getStorageLocations()[i][1] != null) {
                 temp += bfo.getStorageLocations()[i][1];
             }
         }
         bfo.setAmount((int) temp);
         //Checks if batch substance has been changed
         if (batch.getSubstance().getId() != bfo.getSubstance()) {
             int oldAmount = batch.getAmount();
             batch.setAmount(bfo.getAmount());
             Substance newSubstance = (Substance) substanceService.read(bfo.getSubstance());
             substance.setTotalAmount(substance.getTotalAmount() - oldAmount);
             newSubstance.setTotalAmount(newSubstance.getTotalAmount() + batch.getAmount());
             batch.setSubstance(newSubstance);
             substanceService.createOrUpdate(newSubstance);
         } else {
             int amountChange = amountChange(batch, bfo);
             batch.setAmount(batch.getAmount() + amountChange);
             substance.setTotalAmount(substance.getTotalAmount() + amountChange);
         }
         batch = batchService.createOrUpdate(batch);
         substanceService.createOrUpdate(substance);
         Event event = EventHandler.updateBatchEvent(batch, bfo.getSignature());
         eventService.createOrUpdate(event);
         return batch;
     }
 
     private int amountChange(Batch batch, BatchFormObject bfm) {
         int tempAmount;
         for (int i = 0; i < bfm.getStorageLocations().length; i++) {
             System.out.println("kaapissa " + i + " " + bfm.getStorageLocations()[i][1] + " kpl");
         }
         System.out.println("bfm.getAmount() : " + bfm.getAmount());
         System.out.println("batch.getAmount() : " + batch.getAmount());
         if (batch.getAmount() > bfm.getAmount()) {
             tempAmount = -(batch.getAmount() - bfm.getAmount());
         } else if (batch.getAmount() < bfm.getAmount()) {
             tempAmount = (bfm.getAmount() - batch.getAmount());
         } else {
             tempAmount = 0;
         }
         System.out.println("Palautuva tempAmount : " + tempAmount);
         return tempAmount;
     }
 
     @RequestMapping(value = "batchDelete/{id}", method = RequestMethod.POST)
     public String deleteBatchCTRL(@RequestParam String name, @RequestParam Integer amount, @PathVariable Long id) {
         deleteBatchFromDatabase(id, amount, name);
         return "redirect:/batch/" + id;
     }
 
     private void deleteBatchFromDatabase(Long id, Integer amount, String name) {
         Batch batch = batchService.read(id);
         Substance substance = batch.getSubstance();
         int total = batch.getAmount() - amount;
         if (total >= 0 && name.length() >= 1) {
             substance.setTotalAmount(substance.getTotalAmount() - amount);
             substanceService.createOrUpdate(substance);
             batch.setAmount(total);
             batchService.createOrUpdate(batch);
         }
     }
 
     private Batch createBatch(BatchFormObject bfo) {
         Batch batch = new Batch();
         batch.setBatchNumber(bfo.getBatchNumber());
         batch.setNote(bfo.getNote());
         batch.setArrivalDate(Time.parseDate(bfo.getArrivalDate()));
         batch.setExpDate((Time.parseDate(bfo.getExpDate())));
         long temp = 0;
         for (int i = 0; i < bfo.getStorageLocations().length; i++) {
             if (bfo.getStorageLocations()[i][1] != null) {
                 temp += bfo.getStorageLocations()[i][1];
             }
         }
 
         bfo.setAmount((int) temp);
         batch.setAmount(bfo.getAmount());
         batch.setSubstanceVolume(bfo.getSubstanceVolume());
         batch.setStorageLocations(bfo.getStorageLocations());
         Substance substance = (Substance) substanceService.read(bfo.getSubstance());
         if (batch.getExpDate().compareTo(substance.getOldestDate()) < 0) {
             substance.setOldestDate(batch.getExpDate());
             substance.setWarningDate(Time.parseWarningDate(batch.getExpDate()));
         }
         substance.setTotalAmount(substance.getTotalAmount() + bfo.getAmount());
         substanceService.createOrUpdate(substance);
         batch.setSubstance(substance);
         batch.setManufacturer(substance.getManufacturer());
         batch.setSupplier(substance.getSupplier());
         return batch;
     }
 
     private Batch addToBatch(Long id, BatchFormObject bfo) {
         Batch batch = batchService.read(id);
         batch.setAmount(batch.getAmount() + bfo.getAmount());
         Long[][] newStorages = combineStorages(batch.getStorageLocations(), bfo.getStorageLocations());
         batch.setStorageLocations(newStorages);
         batch.setNote(batch.getNote() + "\n" + bfo.getNote());
         Event event = EventHandler.addToBatchEvent(batch, bfo.getSignature());
         eventService.createOrUpdate(event);
         return batchService.createOrUpdate(batch);
 
     }
 
     private void updateSubstance(Substance substance) {
         Substance temp = (Substance) substanceService.read(substance.getId());
         int status = temp.getQualityStatus();
 
         List<Batch> batches = batchService.listSubstanceBatches(temp.getId());
         for (Batch batch : batches) {
             if (batch.getQualityCheck() == 2 && status == 0) {
                 status = 2;
             } else if (batch.getQualityCheck() == 1 && (status == 0 | status == 2)) {
                 status = 1;
             }
         }
 
         temp.setQualityStatus(status);
         substanceService.createOrUpdate(temp);
     }
 
     private Long[][] combineStorages(Long[][] storageTemp, Long[][] addStorage) {
         for (int i = 0; i < addStorage.length; ++i) {
             for (int j = 0; i < storageTemp.length; ++j) {
                 if (storageTemp[j][0] != null && storageTemp[j][0].equals(addStorage[i][0])) {
                     storageTemp[j][1] += addStorage[i][1];
                     break;
                 } else if (storageTemp[j][1] == null) {
                     storageTemp[j] = addStorage[i];
                     break;
                 }
             }
 
         }
         return storageTemp;
     }
 }
