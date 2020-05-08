 package cz.muni.fi.pa165.pujcovnaStroju.web.controller;
 
 import cz.muni.fi.pa165.pujcovnaStroju.web.converter.StringToSystemUserTypeEnumDTOConverter;
 import cz.muni.fi.pa165.pujcovnastroju.dto.SystemUserDTO;
 import cz.muni.fi.pa165.pujcovnastroju.entity.UserTypeEnum;
 import cz.muni.fi.pa165.pujcovnastroju.service.SystemUserService;
 import java.util.Arrays;
 import java.util.LinkedList;
 import java.util.List;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.dao.DataAccessException;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.servlet.ModelAndView;
 
 /**
  * User Controller implementation
  * 
  * @author Vojtech Schlemmer
  */
 @Controller
 @RequestMapping("/user")
 public class UserController {
     
     private SystemUserService userService;
     
     @Autowired
     public UserController(SystemUserService userService){
         this.userService = userService;
     }
     
     @RequestMapping("")
     public String redirectToList(ModelMap model) {
             return "redirect:/user/list";
     }
 
     @RequestMapping("/")
     public String redirectToListBackslash(ModelMap model) {
             return "redirect:/user/list";
     }
     
     @RequestMapping(value = "/list")
     public ModelAndView listUsers(ModelMap model,
         @RequestParam(value = "storeStatus", required = false, defaultValue = "") String storeStatus,
         @RequestParam(value = "errorMessage", required = false, defaultValue = "") String errorMessage
         ) {
         model.addAttribute("users", userService.findAllSystemUsers());
         model.addAttribute("types", UserTypeEnum.class.getEnumConstants());
         model.addAttribute("list", "list of users");
         model.addAttribute("pageTitle", "lang.listUsersTitle");
         DefaultController.addHeaderFooterInfo(model);
         if (storeStatus.equalsIgnoreCase("true")) {
                 model.addAttribute("storeStatus","true");
         }
         if (storeStatus.equalsIgnoreCase("false")) {
                 model.addAttribute("storeStatus","false");
                 model.addAttribute("errorMessage",errorMessage);	
         }
         return new ModelAndView("listUsers", "command", new SystemUserDTO());
     }
     
     @RequestMapping(value = "/add", method = RequestMethod.POST)
     public String addUser(@ModelAttribute("user") SystemUserDTO user,
                     BindingResult result, ModelMap model) {
         boolean stored = false;
         String errorMsg = null;
         try {
             stored = userService.create(user) != null;
         } catch (DataAccessException e) {
             stored = false;
             errorMsg = e.getMessage();
         }
         model.addAttribute("storeStatus", stored);
         if (errorMsg != null) {
             model.addAttribute("errorMessage", errorMsg);
         }
         return "redirect:list";
     }
     
     @RequestMapping("/detail/{id}")
     public String viewUser(@PathVariable String id, ModelMap model) {
         DefaultController.addHeaderFooterInfo(model);
         model.addAttribute("pageTitle", "lang.detailUserTitle");
         SystemUserDTO user = null;
         boolean found = false;
         try {
             Long userID = Long.valueOf(id);
             user = userService.read(userID);
             found = true;
         } catch (DataAccessException | NumberFormatException e) {
             // TODO log
         }
         model.addAttribute("user", user);
         if (!found) {
             model.addAttribute("id", id);
         }
         return "userDetail";
     }
     
     @RequestMapping(value = "/delete/{id}")
     public String deleteUser(@PathVariable String id, ModelMap model) {
         boolean deleted = false;
         String errorMsg = null;
         SystemUserDTO userDTO = new SystemUserDTO();
         try {
             Long userID = Long.valueOf(id);
             userDTO = userService.read(userID);
             userService.delete(userDTO);
             deleted = true;
         } catch (DataAccessException | NumberFormatException
                         | NullPointerException e) {
             // TODO log
             deleted = false;
             errorMsg = e.getMessage();
         }
         model.addAttribute("deleteStatus", deleted);
         if (errorMsg != null) {
             model.addAttribute("errorMessage", errorMsg);
         }
         return "redirect:/user/list";
     }
     
     @RequestMapping(value = "/update/{id}")
     public ModelAndView updateUser(@PathVariable String id, ModelMap model) {
         DefaultController.addHeaderFooterInfo(model);
         model.addAttribute("pageTitle", "lang.updateUserTitle");
         SystemUserDTO user = null;
         boolean found = false;
         try {
             Long userID = Long.valueOf(id);
             user = userService.read(userID);
             found = true;
        } catch (DataAccessException | NumberFormatException e) {
             // TODO log
         }
        
         // prevent the actual type of the user to show in the list twice
         List<UserTypeEnum> enums = new LinkedList<UserTypeEnum>();
         for(UserTypeEnum enum1 : UserTypeEnum.class.getEnumConstants()){
             if (!enum1.toString().equals(user.getType().getTypeLabel())){
                 enums.add(enum1);
             }
         }
         UserTypeEnum[] types = (UserTypeEnum[]) enums.toArray(new UserTypeEnum[enums.size()]);
         
         model.addAttribute("types", enums);
         model.addAttribute("user", user);
         if (!found) {
             model.addAttribute("id", id);
         }
         return new ModelAndView("updateUser", "command", new SystemUserDTO());
     }
     
     @RequestMapping(value = "/update/update", method = RequestMethod.POST)
     public String editUser(@ModelAttribute("user") SystemUserDTO user,
                     BindingResult result, ModelMap model) {
         boolean updated = false;
         String errorMsg = null;
         try {
             updated = userService.update(user) != null;
         } catch (DataAccessException e) {
             updated = false;
             errorMsg = e.getMessage();
         }
         model.addAttribute("updateStatus", updated);
         if (errorMsg != null) {
             model.addAttribute("errorMessage", errorMsg);
         }
         return "redirect:/user/list";
     }
     
     @RequestMapping(value = "/filter", method = RequestMethod.GET, params="submit")
     public ModelAndView filterUsers(ModelMap model, 
             @RequestParam(required = false) String  firstName,
             @RequestParam(required = false) String  lastName,
             @RequestParam(required = false) String type){
         DefaultController.addHeaderFooterInfo(model);
         StringToSystemUserTypeEnumDTOConverter converter = new StringToSystemUserTypeEnumDTOConverter();
         if (type.equals("--no type--")){
             type = "";
         }
         if (firstName.equals("")){
             firstName = null;
         }
         if (lastName.equals("")){
             lastName = null;
         }
         model.addAttribute("users", userService.getSystemUsersByParams(firstName, 
                     lastName, converter.convert(type)));
         model.addAttribute("types", UserTypeEnum.class.getEnumConstants());
         model.addAttribute("list", "list of users");
         model.addAttribute("pageTitle", "lang.listUsersTitle");
         return new ModelAndView("listUsers", "command", new SystemUserDTO());
     }
     
     @RequestMapping(value = "/filter", method = RequestMethod.GET, params="void")
     public String voidFilter(ModelMap model){
         return "redirect:/user/list";
     }
 }
