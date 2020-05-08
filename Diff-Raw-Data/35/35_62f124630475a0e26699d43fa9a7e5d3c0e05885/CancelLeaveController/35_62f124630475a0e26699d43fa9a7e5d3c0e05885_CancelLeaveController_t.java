 package com.madrone.lms.controller;
 
 import java.util.List;
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
 import com.madrone.lms.entity.EmployeeLeave;
 import com.madrone.lms.form.LeaveDetailsGrid;
 import com.madrone.lms.form.LeaveForm;
 import com.madrone.lms.service.EmployeeLeaveService;
 import com.madrone.lms.utils.JSONUtils;
 
 @Controller
 public class CancelLeaveController {
 
 	@Autowired
 	private EmployeeLeaveService empLeaveService;
 
 	private static final Logger logger = LoggerFactory
 			.getLogger(CancelLeaveController.class);
 
 	@Autowired
 	private MessageSource messageSource;
 
 	@RequestMapping(value = "/cancelLeave", method = RequestMethod.GET)
 	public String cancelLeave(Model model, LeaveForm form, HttpSession session) {
 		logger.info("Inside cancelLeave()");
 		model.addAttribute("CancelLeaveForm", new LeaveForm());
 
 		// Setting values into Cancel - Leave Grid
 		String userName = (String) session.getAttribute("sessionUser");
 		List<LeaveDetailsGrid> cancelLeaveList = empLeaveService
 				.getPendingAndApprovalLeaveList(userName);
 		String jsonString = JSONUtils.convertListToJson(cancelLeaveList);
 		model.addAttribute("jsonString", jsonString);
 		return LMSConstants.CANCEL_LEAVE_SCR + "_"
 				+ session.getAttribute("sessionRole");
 	}
 
 	@RequestMapping(value = "/submitCancelLeave", method = RequestMethod.POST)
	public ModelAndView submitCancelLeave(@ModelAttribute("cancelLeaveForm") LeaveForm form,
			                        BindingResult result, Map<String, Object> map, HttpSession session,
			                        RedirectAttributes ra) {
 
 		logger.info("submitCancelLeave");
		ModelAndView modelView = new ModelAndView(new RedirectView(LMSConstants.CANCEL_LEAVE_URL));
 		String jsonString1 = form.getSelecteddata();
 
 		LeaveForm cancelForm = JSONUtils.convertJsonToObjectToClass(jsonString1);
 		cancelForm.setReason(form.getReason());
 		if (cancelForm != null) {
 			String operation = LMSConstants.LEAVE_CANCEL;
 			EmployeeLeave el  = empLeaveService.setBeanValuesForSave(cancelForm,operation);
 			empLeaveService.cancelEmployeeLeave(el);
			ra. addFlashAttribute("SucessMessage", messageSource.getMessage(
 					"lms.cancelLeave_success_message", new Object[] { "" },
 					Locale.getDefault()));
 		}
		return modelView;
 	}
 }
