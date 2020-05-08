 package com.gooki.webapp.controller;
 
 import com.gooki.Constants;
 import com.gooki.model.Block;
 import com.gooki.model.Cong;
 import com.gooki.model.User;
 import com.gooki.service.*;
 import com.gooki.webapp.constant.TerritoryConstants;
 import com.gooki.webapp.exception.BlockExistsException;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.security.access.AccessDeniedException;
 import org.springframework.security.access.prepost.PreAuthorize;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ExtendedModelMap;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.*;
 import org.springframework.web.servlet.ModelAndView;
 
 import javax.servlet.http.HttpServletRequest;
 import java.util.*;
 
 @Controller
 @RequestMapping("/acl")
 public class AclController extends TerritoryBaseController {
     private CongManager congManager;
     private UserManager userManager;
     private SecurityContextService securityContextService;
 
     @Autowired
     public void setSecurityContextService(SecurityContextService securityContextService) {
         this.securityContextService = securityContextService;
     }
 
     @Autowired
     public void setCongManager(CongManager congManager) {
         this.congManager = congManager;
     }
 
     @Autowired
     public void setUserManager(UserManager userManager) {
         this.userManager = userManager;
     }
 
     @RequestMapping(value = "/edit/{congName}", method = RequestMethod.GET)
     public ModelAndView handleEditCongAclRequest(@PathVariable String congName) throws Exception {
         Model model = new ExtendedModelMap();
         Cong cong = congManager.findByCongName(congName);
        List<User> allUserList = userManager.getAll();
         model.addAttribute(TerritoryConstants.ALL_USER_LIST.getValue(), allUserList);
         model.addAttribute(TerritoryConstants.USER_LIST.getValue(), cong.getUsers());
         model.addAttribute(TerritoryConstants.CONGREGATION.getValue(), congName);
 
         return new ModelAndView("editAcl", model.asMap());
     }
 
     @RequestMapping(value = "/edit", method = RequestMethod.GET)
     public ModelAndView handleEditAclRequest(HttpServletRequest request) throws Exception {
         Model model = new ExtendedModelMap();
 
         if(request.isUserInRole(Constants.ADMIN_ROLE)) {
            model.addAttribute(TerritoryConstants.CONGREGATION_LIST.getValue(), congManager.getAll());
         } else {
             Set<Cong> congs = ((User)securityContextService.getAuthentication().getPrincipal()).getCongs();
             model.addAttribute(TerritoryConstants.CONGREGATION_LIST.getValue(), congs);
         }
 
         return new ModelAndView("editAclDefault", model.asMap());
     }
 
     @RequestMapping(value = "/add", method = RequestMethod.POST)
     public @ResponseBody Map<String, Object>  handleEditAclRequest(@RequestParam String congName, @RequestParam String userName) throws Exception {
         Map<String, Object> map = new HashMap<String, Object>();
 
         try {
             Cong cong = congManager.findByCongName(congName);
             User user = userManager.getUserByUsername(userName);
             congManager.addUser(cong, user);
             map.put("status", TerritoryConstants.JSON_OK_RESPONSE.getValue());
             map.put("message", "Successfully added!");
         } catch (AccessDeniedException ade) {
             map.put("status", TerritoryConstants.JSON_NO_PERMISSION_RESPONSE.getValue());
             map.put("message", "Oops. It looks like you don't have permission for this operation.");
         } catch (Exception e) {
             map.put("status", TerritoryConstants.JSON_FAIL_RESPONSE.getValue());
             map.put("message", "Oops. Something went wrong and could not add the user in the Access Control List. Please try later.");
         }
 
         return map;
     }
 
 
 }
