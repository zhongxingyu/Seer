 /*
  * INSERT COPYRIGHT HERE
  */
 
 package com.wadpam.open.user.web;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.ResponseBody;
 
 import com.wadpam.open.exceptions.NotFoundException;
 import com.wadpam.open.mvc.CrudController;
 import com.wadpam.open.user.domain.DOpenUser;
 import com.wadpam.open.user.json.JOpenUser;
 import com.wadpam.open.user.service.OpenUserService;
 
 /**
  * 
  * @author sosandstrom
  */
 @Controller
 @RequestMapping("{domain}/user")
 public class OpenUserController extends CrudController<JOpenUser, DOpenUser, Long, OpenUserService> {
 
    // Default attribute name is matching the name set by the interceptor
    // Can configure a different value in the Spring context
    private String attributeNameUsername = "com.wadpam.open.security.username";
 
     public OpenUserController() {
         super(JOpenUser.class);
     }
 
     @RequestMapping(value = "v10", method = RequestMethod.GET, params = "email")
     @ResponseBody
     public JOpenUser getByEmail(@RequestParam String email) {
         DOpenUser dUser = service.getByEmail(email);
         if (null == dUser) {
             throw new NotFoundException(ERR_CRUD_BASE, email);
         }
         return convertDomain(dUser);
     }
 
     @RequestMapping(value = "v10/me", method = RequestMethod.GET)
     @ResponseBody
     public JOpenUser getMe(HttpServletRequest request, HttpServletResponse response, @PathVariable String domain, Model model) {
         final String username = (String) request.getAttribute(attributeNameUsername);
         final Long userId = Long.parseLong(username);
         return get(request, response, domain, userId, null, model);
     }
 
     // ----------------------- Converter and setters ---------------------------
 
     @Override
     public void convertDomain(DOpenUser from, JOpenUser to) {
         convertDOpenUser(from, to);
     }
 
     @Override
     public void convertJson(JOpenUser from, DOpenUser to) {
         convertJOpenUser(from, to);
     }
 
     public static void convertDOpenUser(DOpenUser from, JOpenUser to) {
         convertLongEntity(from, to);
         to.setDisplayName(from.getDisplayName());
         to.setEmail(from.getEmail());
         to.setUsername(from.getUsername());
     }
 
     public static void convertJOpenUser(JOpenUser from, DOpenUser to) {
         convertJLong(from, to);
         to.setDisplayName(from.getDisplayName());
         to.setEmail(from.getEmail());
         to.setUsername(from.getUsername());
     }
 
     @Autowired
     public void setOpenUserService(OpenUserService openUserService) {
         this.service = openUserService;
     }
 
     public void setAttributeNameUsername(String attributeNameUsername) {
         this.attributeNameUsername = attributeNameUsername;
     }
 
 }
