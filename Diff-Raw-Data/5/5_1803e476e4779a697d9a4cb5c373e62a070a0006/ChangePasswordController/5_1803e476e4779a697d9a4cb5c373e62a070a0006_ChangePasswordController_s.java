 package com.madrone.lms.controller;
 
 import java.util.Locale;
 import java.util.Map;
 
 import javax.servlet.http.HttpSession;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.context.MessageSource;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.servlet.ModelAndView;
 import org.springframework.web.servlet.mvc.support.RedirectAttributes;
 import org.springframework.web.servlet.view.RedirectView;
 
 import com.madrone.lms.constants.LMSConstants;
 import com.madrone.lms.entity.User;
 import com.madrone.lms.form.ChangePasswordForm;
 import com.madrone.lms.service.UserService;
 
 @Controller
 public class ChangePasswordController {
 	private static final Logger logger = LoggerFactory
 			.getLogger(ChangePasswordController.class);
 
 	@Autowired
 	private UserService userService;
 
 	@Autowired
 	private MessageSource messageSource;
 
 	@RequestMapping(value = "/changePassword", method = RequestMethod.GET)
 	public String changePassword(Model model, ChangePasswordForm form,
 			HttpSession session) {
 		model.addAttribute("ChangePasswordForm", new ChangePasswordForm());
 		return LMSConstants.CHANGE_PASSWORD_SCR + "_"
 				+ session.getAttribute("sessionRole");
 
 	}
 
 	@RequestMapping(value = "/submitChangePassword", method = RequestMethod.POST)
 	public ModelAndView submitChangePassword(@ModelAttribute("ChangePasswordForm") ChangePasswordForm changePassword,
 			                           BindingResult result, Map<String, Object> map, HttpSession session,
 			                           RedirectAttributes ra) {
 
 		logger.info("Inside submitChangePassword method");
 		ModelAndView modelView = new ModelAndView(new RedirectView(LMSConstants.CHANGE_PASSWORD_URL));
 
 		if (!userService.authenticateUser(changePassword.getUserName(),
 				changePassword.getOldPassword())) {
			result.rejectValue("oldPassword",
					"lms.password.oldPassword.notvalid");
 		} else {
 			User user = userService
 					.findByUserName(changePassword.getUserName());
 			user.setPassword(changePassword.getNewPassword());
 			userService.saveUser(user);
 			ra. addFlashAttribute("SucessMessage", messageSource.getMessage(
 					"lms.password_changed_successfully", new Object[] { "" },
 					Locale.getDefault()));
 		}
 
 		ra. addFlashAttribute("userName", changePassword.getUserName());
 		ra. addFlashAttribute("empName", changePassword.getEmpName());
 		return modelView;
 
 	}
 }
