 package com.ocbcmcd.monitoring.web;
 
 import static org.springframework.web.bind.annotation.RequestMethod.GET;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.support.SessionStatus;
 import org.springframework.web.servlet.ModelAndView;
 
 import com.ocbcmcd.monitoring.domain.User;
 import com.ocbcmcd.monitoring.domain.UserUpdateCommand;
 import com.ocbcmcd.monitoring.service.IRegistrationService;
 import com.ocbcmcd.monitoring.validator.UserUpdateValidator;
 
 @Controller
 @RequestMapping("/user/edit/{id}")
 public class UserUpdateController {
 	
 	protected Log log = LogFactory.getLog(getClass());
 	
 	@Autowired
 	private UserUpdateValidator validator;
 	
 	@Autowired
 	private IRegistrationService registrationService;
 	
 	@RequestMapping(method = GET)
 	public ModelAndView setupForm(@PathVariable("id") int id, @RequestParam(required = false) UserUpdateCommand command) {
 		User user = registrationService.getUser(id);
 		if (user == null) {
 			return new ModelAndView("redirect:/user/message/0");
 		}
 		
 		if (command == null) {
 			command = new UserUpdateCommand(user);
 		}
 		
 		return new ModelAndView("user_edit", "command", command);
 	}
 	
 	@RequestMapping(method = RequestMethod.POST)
 	public ModelAndView submitForm(@PathVariable("id") int id, @ModelAttribute("command") UserUpdateCommand command, BindingResult result, SessionStatus status) {
		log.info("id: " + command.getId());
 		validator.validate(command, result);
 		if (result.hasErrors())
 			return new ModelAndView("user_edit");
 		else {
			return new ModelAndView("redirect:/user/edit/" + id + "?message=1");
 		}
 	}
 }
