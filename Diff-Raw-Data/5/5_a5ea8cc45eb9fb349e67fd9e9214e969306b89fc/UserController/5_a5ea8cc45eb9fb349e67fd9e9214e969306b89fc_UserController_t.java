 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.posabro.ocsys.security.controller;
 
 import com.posabro.ocsys.commons.JQueryPage;
 import com.posabro.ocsys.commons.Misc;
 import com.posabro.ocsys.commons.PageRequestBuilder;
 import com.posabro.ocsys.commons.ReportSpec;
 import com.posabro.ocsys.excel.ReportExcelView;
 import com.posabro.ocsys.security.domain.User;
 import com.posabro.ocsys.security.services.UserService;
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.validation.Valid;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.data.domain.Page;
 import org.springframework.data.domain.Pageable;
 import org.springframework.security.access.prepost.PreAuthorize;
 import org.springframework.security.core.Authentication;
 import org.springframework.security.core.context.SecurityContextHolder;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestBody;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.ResponseBody;
 import org.springframework.web.servlet.ModelAndView;
 
 /**
  *
  * @author Carlos Juarez
  */
 @Controller
 @RequestMapping("/userController/*")
 public class UserController extends ValidationController{
 
     final org.slf4j.Logger logger = LoggerFactory.getLogger(UserController.class);
     
     @Autowired
     private UserService userService;
 
     @RequestMapping("filter")
     @PreAuthorize("hasRole('ROLE_ADMIN')")
     public @ResponseBody JQueryPage filter(HttpServletRequest request) {
         Pageable pageable = PageRequestBuilder.build(request);
         String echo = request.getParameter("sEcho");
         String searchPattern = request.getParameter("sSearch");
         DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
         Page<User> page;
         try {
             Date datePattern = dateFormat.parse(searchPattern);
             page = userService.queryPageByDatePattern(datePattern, pageable);
         } catch (ParseException ex) {
             page = userService.queryPageByStringPattern(searchPattern, pageable);
         }
         return Misc.pageToJQueryPage(page, echo);
     }
 
     @RequestMapping(value = "store", method = RequestMethod.POST)
     @PreAuthorize("hasRole('ROLE_ADMIN')")
     public void store(@Valid @RequestBody User user, HttpServletResponse response) {
         logger.debug("storeUser init");
         userService.registerUser(user);
         Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
         if (authentication != null && authentication.getName().equals(user.getName())) {
             SecurityContextHolder.clearContext();
         }
     }
 
     @RequestMapping(value = "update", method = RequestMethod.POST)
     public void update(@Valid @RequestBody User user, HttpServletResponse response) {
         userService.updateUser(user);
     }
 
     @RequestMapping(value = "delete", method = RequestMethod.POST)
     public void delete(@RequestBody String name, HttpServletResponse response) {
         logger.debug("deleteUser init : " + name);
         userService.removeUser(name);
     }
 
     @RequestMapping(value = "findById")
     public @ResponseBody
     User findById(@RequestBody String name) {
         return userService.findUser(name);
     }
     
     @RequestMapping("getAll")
     public @ResponseBody
     List<User> getAll() {
         return userService.getAllUsers();
     }
     
     @RequestMapping(value = "confirmEmailAddress/{userName}/{key}", method= RequestMethod.GET)
     public ModelAndView confirmEmailAddress(@PathVariable String userName, @PathVariable String key){
         ModelAndView mav = new ModelAndView("emailVerified");
         if(userName!=null && key!=null){
             User user = userService.confirmEmailAddress(key, userName);
             mav.addObject("userVerified", user);
         }
         return mav;
     }
     
     @RequestMapping(value = "export/{output}", method= RequestMethod.GET)
     public ModelAndView export(@PathVariable String output){
         List<User> allUsers = userService.getAllUsers();
         ReportSpec<User> reportSpec = new ReportSpec<User>(allUsers);
         reportSpec.setTitleKey("users.report.title");
         reportSpec.addColumn(new ReportSpec.Column("user.name","name")).addColumn(new ReportSpec.Column("user.password","password")).
                 addColumn(new ReportSpec.Column("user.creationDate","auditData.createdDate"));
         ModelAndView mav = new ModelAndView();
         if(output.equals("excel")){
             mav.setViewName("reportExcelView");
         }else if(output.equals("pdf")){
             mav.setViewName("reportPdfView");
         }
         mav.addObject(ReportExcelView.REPORT_SPEC, reportSpec);
         return mav;
     }
     
     @RequestMapping(value = "registerGuest", method = RequestMethod.POST)
    @PreAuthorize("not isAuthenticated()")
     public void registerGuest(@Valid @RequestBody User user, HttpServletResponse response) {
         logger.debug("registerGuest init");
         userService.registerGuest(user);
     }
     
     @RequestMapping(value="getTempPassword", method= RequestMethod.POST)
    @PreAuthorize("not isAuthenticated()")
     public void getTemPassword(@RequestBody Map<String,String> params, HttpServletResponse response){
         String userName = params.get("userName");
         String email = params.get("email");
         logger.debug("generate temp password for " + userName +" email address " + email);
         userService.generateTempPassword(userName, email);
     }
     
     @RequestMapping(value = "verifyTempPasswordKey/{userName}/{key}", method= RequestMethod.GET)
     public ModelAndView verifyTempPasswordKey(@PathVariable String userName, @PathVariable String key){
         boolean verifyTempPassword = userService.isVerifiedTempPassword(userName, key);
         ModelAndView mav = new ModelAndView("generateNewPassword");
         mav.addObject("userName", userName);
         mav.addObject("key", key);
         mav.addObject("verifyTempPassword", verifyTempPassword);
         return mav;
     }
     
     @RequestMapping(value="getNewPassword", method= RequestMethod.POST)
    @PreAuthorize("not isAuthenticated()")
     public void getNewPassword(@RequestBody Map<String,String> params, HttpServletResponse response){
         String userName = params.get("userName");
         String tempPassword = params.get("tempPassword");
         String newPassword = params.get("newPassword");
         String key = params.get("key");
         logger.debug("retrieve password for userName {0} and key {1}", new Object[]{userName, key});
         userService.retrievePassword(userName,tempPassword.toCharArray(), newPassword.toCharArray(),key);
     }
     
 }
