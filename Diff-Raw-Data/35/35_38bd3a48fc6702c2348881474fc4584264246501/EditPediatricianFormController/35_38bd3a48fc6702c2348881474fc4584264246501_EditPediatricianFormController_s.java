 package com.tda.presentation.controller;
 
 import javax.validation.Valid;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.SessionAttributes;
 import org.springframework.web.bind.support.SessionStatus;
 
 import com.tda.model.pediatrician.PediatricianForm;
 import com.tda.model.utils.FormType;
 import com.tda.service.api.PatientInTrainService;
 import com.tda.service.api.PediatricianFormService;
 
 @Controller
 @RequestMapping("/patient/*/pediatrician/{pediatricianFormId}/edit")
 @SessionAttributes("pediatricianForm")
 public class EditPediatricianFormController extends
 		BasePediatricianFormController {
 	private static final String PEDIATRICIAN_FORM = "pediatricianform/form";
 	private PediatricianFormService pediatricianFormService;
 	private PatientInTrainService patientInTrainService;
 
 	@Autowired
 	public void setPatientInTrainService(
 			PatientInTrainService patientInTrainService) {
 		this.patientInTrainService = patientInTrainService;
 	}
 
 	@RequestMapping(method = RequestMethod.GET)
 	public String setupForm(
 			@PathVariable("pediatricianFormId") long pediatricianFormId,
 			Model model) {
 		PediatricianForm pediatricianForm = pediatricianFormService
 				.findById(pediatricianFormId);
 		model.addAttribute("pediatricianForm", pediatricianForm);
 
 		boolean editable = patientInTrainService.isActiveForm(
 				pediatricianFormId, FormType.pediatrician);
 
 		model.addAttribute("editable", editable);
 
 		return PEDIATRICIAN_FORM;
 	}
 
 	@RequestMapping(method = { RequestMethod.POST })
 	public String processSubmit(
 			@Valid @ModelAttribute("pediatricianForm") PediatricianForm pediatricianForm,
 			BindingResult result, SessionStatus status) {
 		if (result.hasErrors()) {
 			return PEDIATRICIAN_FORM;
 		} else {
 			pediatricianFormService.update(pediatricianForm);
 			// status.setComplete();
 			return "redirect:/";
 		}
 	}
 
 	@Autowired
 	public void setPediatricianFormService(
 			PediatricianFormService pediatricianFormService) {
 		this.pediatricianFormService = pediatricianFormService;
 	}
 }
