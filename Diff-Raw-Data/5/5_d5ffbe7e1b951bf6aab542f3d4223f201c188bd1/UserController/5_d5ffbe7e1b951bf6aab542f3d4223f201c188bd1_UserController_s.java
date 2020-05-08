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
   private UserRepository userRepo;
   
   @Autowired
   private UserRoleRepository roleRepo;
   
   @Autowired
   private DepartmentRepository departmentRepo;
   
   @Autowired
   private UserService userService;
   
   @Autowired
   private ClientUtils clientUtils;
 
   @RequestMapping(value = "/user", params = "!p", method = RequestMethod.GET)
   public String list(ModelMap model) {
     model.addAttribute("listUser", userRepo.findAll());
     return TemplateKeys.USER_ADMIN;
   }
 
   @RequestMapping(value="/user", params = "p=add", method = RequestMethod.GET)
   public String insert(ModelMap model) {
     model.addAttribute("departList", departmentRepo.findAll());
     model.addAttribute("roleList", roleRepo.findAll());
     return TemplateKeys.USER_ADMIN;
   }
   
   @RequestMapping(value = "/user/addSave", method = RequestMethod.POST)
   public ResponseEntity<String> insertSave(@RequestParam(value="username") String username,
         @RequestParam(value="passwd") String password, @RequestParam(value="email") String email,
         @RequestParam(value="department") byte departId, @RequestParam(value="role") byte roleId,
         @RequestParam(value="firstName") String firstName, @RequestParam(value="lastName") String lastName,
         @RequestParam(value="gender") String gender, @RequestParam(value="address") String address)
   {
     // TODO Validate username, email...
     
     Department department = departmentRepo.get(departId);
     UserRole userRole = roleRepo.get(roleId);
 
     int newUserId = ((Integer)userService.add(username, password, email, firstName, lastName, gender, address, department, userRole)).intValue();
     return clientUtils.createJsonResponse(
        new ResponseMessage("insert", 1, String.format("Inserted user: <strong></strong>%s (ID: %d) successfully.", username, newUserId)));
   }
   
   @RequestMapping(value = "/user", params = "p=edit", method = RequestMethod.GET)
   public String update(@RequestParam("id") int id, ModelMap model) {
     User user = userRepo.get(id);
 
     model.addAttribute("departList", departmentRepo.findAll());
     model.addAttribute("roleList", roleRepo.findAll());
     model.addAttribute("user", userRepo.get(id));
     model.addAttribute("depart", user.getDepartment());
     return TemplateKeys.USER_ADMIN;
   }
   
   @RequestMapping(value = "/user/editSave", method = RequestMethod.POST)
   public ResponseEntity<String> updateSave(@RequestParam("id") int userId, @RequestParam("email") String email,
         @RequestParam("department") byte depart, @RequestParam("role") byte role,
         @RequestParam("address") String address)
   {
     Department department = departmentRepo.get(depart);
     UserRole userRole = roleRepo.get(role);
 
     User user = userService.update(userId, email, address, department, userRole);
     return clientUtils.createJsonResponse(
        new ResponseMessage("update", 0, String.format("Updated user: <strong>%s (ID: %d)</strong> successfully.",
             user.getFirstName().concat(" ").concat(user.getLastName()), user.getId())));
   }
 
   @RequestMapping(value = "/user/search", method = RequestMethod.POST)
   public String search(@RequestParam("q") String searchText, @RequestParam("f") byte selectedField,
       ModelMap model)
   {
     List<User> users = null;
     searchText = StringUtils.isBlank(searchText) ? null : searchText.trim();
     
     switch (selectedField) {
       case 1: users = userService.searchByUsername(searchText); break;
       case 2: users = userService.searchByDepartmentName(searchText); break;
       default: users = userRepo.findAll(); break;
     }
 
     if (users.size() != 0) {
       model.addAttribute("listUser", users);
       return TemplateKeys.USER_SEARCH_ADMIN;
     }
     else {
       return TemplateKeys.NULL;
     }
   }
 
 }
