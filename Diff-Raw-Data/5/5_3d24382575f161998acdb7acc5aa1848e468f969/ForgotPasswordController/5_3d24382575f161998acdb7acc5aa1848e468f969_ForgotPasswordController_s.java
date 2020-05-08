 package org.motechproject.ghana.national.web;
 
 import org.motechproject.ghana.national.domain.Constants;
 import org.motechproject.ghana.national.service.StaffService;
 import org.motechproject.openmrs.advice.LoginAsAdmin;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.servlet.ModelAndView;
 
 import javax.servlet.http.HttpServletRequest;
 
 @Controller
 @RequestMapping(value = "/forgotPassword")
 public class ForgotPasswordController {
     private StaffService staffService;
 
     public ForgotPasswordController() {
     }
 
     @Autowired
     public ForgotPasswordController(StaffService staffService) {
         this.staffService = staffService;
     }
 
     @LoginAsAdmin
     @RequestMapping(value = "/forgotPassword", method = RequestMethod.GET)
     public ModelAndView changePasswordAndSendEmail(HttpServletRequest request) throws Exception {
         ModelAndView modelAndView = new ModelAndView("redirect:/forgotPasswordStatus.jsp");
         int status = staffService.changePasswordByEmailId(request.getParameter("emailId"));
 
         switch (status) {
             case Constants.EMAIL_SUCCESS:
                 modelAndView.addObject(Constants.FORGOT_PASSWORD_MESSAGE, Constants.FORGOT_PASSWORD_SUCCESS);
                 break;
             case Constants.EMAIL_FAILURE:
                 modelAndView.addObject(Constants.FORGOT_PASSWORD_MESSAGE, Constants.FORGOT_PASSWORD_FAILURE);
                 break;
             case Constants.EMAIL_USER_NOT_FOUND:
                 modelAndView.addObject(Constants.FORGOT_PASSWORD_MESSAGE, Constants.FORGOT_PASSWORD_USER_NOT_FOUND);
                 break;
         }
         return modelAndView;
     }
 }
