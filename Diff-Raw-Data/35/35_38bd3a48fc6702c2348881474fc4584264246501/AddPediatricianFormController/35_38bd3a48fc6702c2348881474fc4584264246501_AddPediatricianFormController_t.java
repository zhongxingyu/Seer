 package com.tda.presentation.controller;
 
 import java.util.List;
 
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
 
 import com.tda.model.patient.Patient;
 import com.tda.model.patient.PatientInTrain;
 import com.tda.model.pediatrician.PediatricianForm;
 import com.tda.service.api.PatientInTrainService;
 import com.tda.service.api.PatientService;
 import com.tda.service.api.PediatricianFormService;
 
 @Controller
 @RequestMapping(value = "/patient/{patientId}/pediatrician/new")
 @SessionAttributes({ "pediatricianForm", "nurseForm" })
 public class AddPediatricianFormController extends
 		BasePediatricianFormController {
 	private static final String PEDIATRICIAN_ADD_FORM = "pediatricianform/form";
 	private static final String REDIRECT_AFTER_SAVE = "redirect:/";
 
 	private PediatricianFormService pediatricianFormService;
 	private PatientService patientService;
 	private PatientInTrainService patientInTrainService;
 
 	@RequestMapping(method = RequestMethod.GET)
 	public String setupForm(@PathVariable("patientId") long patientId,
 			Model model) {
 		Patient patient = patientService.findById(patientId);
 		PediatricianForm pediatricianForm = new PediatricianForm();
 		pediatricianForm.setPatient(patient);
 		model.addAttribute("pediatricianForm", pediatricianForm);
 
		PatientInTrain pit = patientInTrainService.findByPatient(patient);
 
		if (pit.getNurseform() != null)
			model.addAttribute("nurseForm", pit.getNurseform());
 
 		return PEDIATRICIAN_ADD_FORM;
 	}
 
 	@RequestMapping(method = RequestMethod.POST)
 	public String processSubmit(
 			@Valid @ModelAttribute("pediatricianForm") PediatricianForm pediatricianForm,
 			BindingResult result, SessionStatus status) {
 
 		if (result.hasErrors()) {
 			return PEDIATRICIAN_ADD_FORM;
 		} else {
 			pediatricianFormService.save(pediatricianForm);
 
 			Patient patient = pediatricianForm.getPatient();
 			List<PatientInTrain> patientInTrain = patientInTrainService
 					.findByDni(patient.getDni());
 
 			if (patientInTrain != null && patientInTrain.size() == 1) {
 				patientInTrain.get(0).setPadiatricianform(pediatricianForm);
 				patientInTrainService.save(patientInTrain.get(0));
 			}
 
 			// status.setComplete();
 			return REDIRECT_AFTER_SAVE;
 		}
 	}
 
 	@Autowired
 	public void setPediatricianFormService(
 			PediatricianFormService pediatricianFormService) {
 		this.pediatricianFormService = pediatricianFormService;
 	}
 
 	@Autowired
 	public void setPatientService(PatientService patientService) {
 		this.patientService = patientService;
 	}
 
 	@Autowired
 	public void setPatientInTrainService(
 			PatientInTrainService patientInTrainService) {
 		this.patientInTrainService = patientInTrainService;
 	}
 }
