 /**
  * Written by Long Nguyen <chautinhlong@gmail.com>
  * FREE FOR ALL BUT DOES NOT MEAN THERE IS NO PRICE.
  */
 package mantech.controller;
 
 import java.util.Date;
 import java.util.List;
 
 import org.apache.commons.lang.StringUtils;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.http.ResponseEntity;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.SessionAttributes;
 
 import net.lilylnx.springnet.core.SessionManager;
 import net.lilylnx.springnet.util.ClientUtils;
 
 import mantech.controller.helpers.RName;
 import mantech.controller.helpers.RStatus;
 import mantech.controller.helpers.ResponseMessage;
 import mantech.controller.helpers.TemplateKeys;
 import mantech.domain.CategoryPriority;
 import mantech.domain.Complaint;
 import mantech.domain.ComplaintStatus;
 import mantech.domain.Equipment;
 import mantech.domain.User;
 import mantech.domain.UserRole;
 import mantech.repository.CategoryPriorityRepository;
 import mantech.repository.ComplaintRepository;
 import mantech.repository.ComplaintStatusRepository;
 import mantech.repository.EquipmentRepository;
 import mantech.repository.UserRepository;
 import mantech.service.ComplaintService;
 
 /**
  * @author Long Nguyen
  * @version $Id: ComplaintController.java,v 1.0 Sep 9, 2011 12:56:51 AM nguyenlong Exp $
  */
 @Controller
 @SessionAttributes("complaint")
 public class ComplaintController {
   
   @Autowired
   private SessionManager sessionManager;
   
   @Autowired
   private ComplaintService complaintService;
 
   @Autowired
   private ComplaintRepository complaintRepo;
   
   @Autowired
   private EquipmentRepository equipmentRepo;
   
   @Autowired
   private UserRepository userRepo;
   
   @Autowired
   private ComplaintStatusRepository statusRepo;
   
   @Autowired
   private CategoryPriorityRepository priorityRepo;
   
   @Autowired
   private ClientUtils clientUtils;
   
   public ComplaintController () {}
 
   @RequestMapping(value = "/complaint", params = "action=list", method = RequestMethod.GET)
   public String list(ModelMap model) throws Exception {
     byte role = sessionManager.getUser().getRole().getId();
     
     if (UserRole.ADMIN == role) {
       model.addAttribute("complaints", complaintRepo.findAll());
       model.addAttribute("listStatus", statusRepo.findAll());
       model.addAttribute("listPriority", priorityRepo.findAll());
       model.addAttribute("countAll", complaintRepo.count());
       model.addAttribute("countWaiting", complaintRepo.countByStatus((byte)1));
       model.addAttribute("countAccepted", complaintRepo.countByStatus((byte)2));
       model.addAttribute("countRejected", complaintRepo.countByStatus((byte)3));
       model.addAttribute("countCompleted", complaintRepo.countByStatus((byte)4));
     }
     else {
       model.addAttribute("complaints", complaintRepo.getByUser(sessionManager.getUser().getId()));
     }
 
     return TemplateKeys.COMPLAINT_LIST;
   }
   
   @RequestMapping(value = "/complaint", params = "action=report", method = RequestMethod.GET)
   public String reportMonthly(@RequestParam(value = "month") String month, ModelMap model) {
     
     return null;
   }
   
   @RequestMapping(value = "/complaint", params = "action=add", method = RequestMethod.GET)
   public String insert(ModelMap model){
     // TODO Sẽ cần chỉnh sửa lại userId sẽ được lấy từ session của employee đã đăng nhập.
     
     model.addAttribute("user", userRepo.get(sessionManager.getUser().getId()));
     model.addAttribute("list", equipmentRepo.findAll());
 
     return TemplateKeys.COMPLAINT_ADD;
   }
   
   @RequestMapping(value = "/complaint/addSave", method = RequestMethod.POST)
   public ResponseEntity<String> insertSave(@RequestParam(value="equip_id") int equipId,
       @RequestParam(value="complaint_title") String title,
       @RequestParam(value="complaint_content") String content, ModelMap model)
   {
     ResponseMessage respMessage = new ResponseMessage(RName.ADD, RStatus.FAIL, null);
     
     try {
       User user = userRepo.get(sessionManager.getUser().getId());
       Equipment equipment = equipmentRepo.get(equipId);
       CategoryPriority priority = equipment.getCategory().getPriority();
       
      complaintService.add(user, equipment, title, content, priority);
      respMessage.setStatusAndMessage(RStatus.SUCC, String.format("Sent the complaint: <strong>%s</strong>", title));
     }
     catch (Exception e) {
       respMessage.setStatusAndMessage(RStatus.ERROR, e.getMessage());
     }
     return clientUtils.createJsonResponse(respMessage);
   }
   
   @RequestMapping(value = "/complaint", params = "action=edit", method = RequestMethod.GET)
   public String update(@RequestParam(value="id") int id, ModelMap model) {
     Complaint complaint = complaintRepo.get(id);
 
     if (complaint == null) {
       return TemplateKeys.FILE_NOT_FOUND;
     }
     
     model.addAttribute("complaint", complaint);
     model.addAttribute("listStatus", statusRepo.findAll(false, "id"));
     model.addAttribute("listPriority", priorityRepo.findAll(false, "id"));
     return TemplateKeys.COMPLAINT_EDIT;
   }
   
   @RequestMapping(value = "/complaint/editSave", method = RequestMethod.POST)
   public ResponseEntity<String> updateSave(@RequestParam("id") int id,
       @RequestParam("status") byte statusId, 
       @RequestParam("priority") byte priorityId, ModelMap model)
   {
     ResponseMessage respMessage = new ResponseMessage(RName.UPDATE, RStatus.FAIL, null);
       try {
         ComplaintStatus status = statusRepo.get(statusId);
         CategoryPriority priority = priorityRepo.get(priorityId);
         
         if (status == null || priority == null) {
           throw new Exception("You are hacking :)");
         }
     
         Complaint complaint = complaintService.update(id, status, priority);
         respMessage.setStatusAndMessage(RStatus.SUCC, String.format("Updated complaint: <strong>%s (ID: %d)</strong> successfully.",
             complaint.getTitle(), complaint.getId()));
       }
       catch (Exception e) {
         respMessage.setStatusAndMessage(RStatus.ERROR, e.getMessage());
       }
       
       return clientUtils.createJsonResponse(respMessage);
   }
   
   @RequestMapping(value = "/complaint/search", method = RequestMethod.POST)
   public String search(@RequestParam("q") String searchText,
       @RequestParam("f") byte selectedField,
       @RequestParam(value="dateFrom", required=false) Date dateFrom,
       @RequestParam(value="dateTo", required=false) Date dateTo,
       @RequestParam(value="status") byte status,
       @RequestParam(value="priority") byte priority,
       ModelMap model)
   {
     List<Complaint> complaints = null;
     searchText = StringUtils.isBlank(searchText) ? null : searchText.trim();
     
     switch (selectedField) {
       case 1: complaints = complaintRepo.search(searchText, null, dateFrom, dateTo, status, priority); break;
       case 2: complaints = complaintRepo.search(null, searchText, dateFrom, dateTo, status, priority); break;
       default: complaints = complaintRepo.search(null, null, dateFrom, dateTo, status, priority); break;
     }
    
     if (complaints.size() != 0) {
      model.addAttribute("complaints", complaints);
       return TemplateKeys.COMPLAINT_LIST_RESULT;
     }
     else {
       return TemplateKeys.NULL;
     }
   }
 }
