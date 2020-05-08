 /**
  * Written by Tien Nguyen <lilylnx@users.sf.net>
  * FREE FOR ALL BUT DOES NOT MEAN THERE IS NO PRICE.
  */
 package mantech.controller;
 
 import java.util.List;
 
 import org.apache.commons.lang.StringUtils;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.http.ResponseEntity;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 
 import net.lilylnx.springnet.util.ClientUtils;
 import net.lilylnx.springnet.util.Pagination;
 import net.lilylnx.springnet.util.SpringConfig;
 
 import mantech.controller.helpers.RName;
 import mantech.controller.helpers.RStatus;
 import mantech.controller.helpers.ResponseMessage;
 import mantech.controller.helpers.TemplateKeys;
 import mantech.domain.Department;
 import mantech.domain.User;
 import mantech.domain.UserRole;
 import mantech.repository.DepartmentRepository;
 import mantech.repository.UserRepository;
 import mantech.repository.UserRoleRepository;
 import mantech.service.UserService;
 
 /**
  * 
  * @author Tien Nguyen
  * @version $Id: UserController.java,v 1.0 Sep 9, 2011 3:59:57 AM lilylnx Exp $
  */
 @Controller
 public class UserController {
 
   @Autowired
   private SpringConfig config;
   
   @Autowired
   private UserRepository userRepo;
   
   @Autowired
   private UserRoleRepository roleRepo;
   
   @Autowired
   private DepartmentRepository departmentRepo;
   
   @Autowired
   private UserService userService;
   
   @Autowired
   private ClientUtils clientUtils;
 
   @RequestMapping(value = "/user", params = "action=list", method = RequestMethod.GET)
   public String list(@RequestParam(value="page", required=false, defaultValue="1") int page, ModelMap model) {
     Pagination pagination = new Pagination(config, page)
         .forUsers(this.userService.countTotalUsers());
 
     model.addAttribute("users", userRepo.findAll())
         .addAttribute("pagination", pagination)
         .addAttribute("totalUsers", pagination.getTotalRecords());
     return TemplateKeys.USER_LIST;
   }
 
   @RequestMapping(value="/user", params = "action=add", method = RequestMethod.GET)
   public String insert(ModelMap model) {
     model.addAttribute("departList", departmentRepo.findAll())
         .addAttribute("roleList", roleRepo.findAll());
     return TemplateKeys.USER_ADD;
   }
   
   @RequestMapping(value = "/user/addSave", method = RequestMethod.POST)
   public ResponseEntity<String> insertSave(@RequestParam(value="username") String username,
         @RequestParam(value="passwd") String password, @RequestParam(value="email") String email,
         @RequestParam(value="department") byte departId, @RequestParam(value="role") byte roleId,
         @RequestParam(value="firstName") String firstName, @RequestParam(value="lastName") String lastName,
         @RequestParam(value="gender") String gender, @RequestParam(value="address") String address)
   {
     ResponseMessage respMessage = new ResponseMessage(RName.ADD, RStatus.FAIL, null);
 
     if (userRepo.isExistUser(username)) {
        respMessage.setMessage(String.format("Username <strong>%s</strong> already exists.", username));
     }
     if (userRepo.isExistUser(email)) {
       respMessage.setMessage(String.format("Email <strong>%s</strong> already exists.", email));
     }
 
     if (respMessage.getMessage() == null) {
       try {
         Department department = departmentRepo.get(departId);
         UserRole userRole = roleRepo.get(roleId);
   
         int newUserId = ((Integer)userService.add(username, password, email, firstName, lastName, gender, address, department, userRole)).intValue();
         respMessage.setStatusAndMessage(RStatus.SUCC, String.format("Inserted user: <strong>%s (ID: %d)</strong> successfully.", username, newUserId));
       }
       catch (Exception e) {
         respMessage.setStatusAndMessage(RStatus.ERROR, e.getMessage());
       }
     }
 
     return clientUtils.createJsonResponse(respMessage);
   }
   
   @RequestMapping(value = "/user", params = "action=edit", method = RequestMethod.GET)
   public String update(@RequestParam("id") int id, ModelMap model) {
     try {
       User user = userRepo.get(id);
   
       model.addAttribute("departList", departmentRepo.findAll());
       model.addAttribute("roleList", roleRepo.findAll());
       model.addAttribute("user", user);
       model.addAttribute("depart", user.getDepartment());
     }
     catch (Exception e) {
       return TemplateKeys.FILE_NOT_FOUND;
     }
     return TemplateKeys.USER_EDIT;
   }
   
   @RequestMapping(value = "/user/editSave", method = RequestMethod.POST)
   public ResponseEntity<String> updateSave(@RequestParam("id") int userId, @RequestParam("email") String email,
         @RequestParam("department") byte depart, @RequestParam("role") byte role,
         @RequestParam("address") String address)
   {
     ResponseMessage respMessage = new ResponseMessage(RName.UPDATE, RStatus.FAIL, null);
     Department department = departmentRepo.get(depart);
     UserRole userRole = roleRepo.get(role);
 
     try {
       User user = userService.update(userId, email, address, department, userRole);
       respMessage.setStatusAndMessage(RStatus.SUCC, String.format("Updated user: <strong>%s (ID: %d)</strong> successfully.",
           user.getFirstName().concat(" ").concat(user.getLastName()), user.getId()));
     }
     catch (Exception e) {
       respMessage.setStatusAndMessage(RStatus.ERROR, e.getMessage());
     }
     
     return clientUtils.createJsonResponse(respMessage);
   }
 
   @RequestMapping(value = "/user", params = "action=profile", method = RequestMethod.GET)
   public String profile(ModelMap model) {
     try {
       User user = userRepo.get(9);
       model.addAttribute("user", user);
     }
     catch (Exception e) {
       return TemplateKeys.FILE_NOT_FOUND;
     }
     return TemplateKeys.USER_PROFILE;
   }
   
   @RequestMapping(value = "/user/changepass", method = RequestMethod.POST)
   public ResponseEntity<String> changePasswd(@RequestParam(value="oldpass") String oldpass,
         @RequestParam(value="newpass") String newpass,
         @RequestParam(value="confirmpass") String confirmpass)
   {
     ResponseMessage respMessage = new ResponseMessage(RName.UPDATE, RStatus.FAIL, null);
     User user = userRepo.get(9);
    if (newpass.equals(confirmpass) && user.getPassword().equals(oldpass)) {
       try {
         user.setPassword(newpass);
         userRepo.save(user);
         respMessage.setStatusAndMessage(RStatus.SUCC, "Changed password successfully.");
       }
       catch (Exception e){
         respMessage.setStatusAndMessage(RStatus.ERROR, e.getMessage());
       } 
     }
     return clientUtils.createJsonResponse(respMessage);
   }
   
   @RequestMapping(value = "/user/search", method = RequestMethod.POST)
   public String search(@RequestParam("q") String searchText, @RequestParam("f") byte selectedField,
       ModelMap model)
   {
     searchText = StringUtils.isNotBlank(searchText) ? searchText.trim() : null;
     List<User> users = null;
 
     switch (selectedField) {
       case 1: users = userService.searchByUsername(searchText); break;
       case 2: users = userService.searchByDepartmentName(searchText); break;
       default: users = userRepo.findAll(); break;
     }
 
     if (users.size() != 0) {
       model.addAttribute("users", users);
       return TemplateKeys.USER_LIST_RESULT;
     }
     else {
       return TemplateKeys.NULL;
     }
   }
 
 }
