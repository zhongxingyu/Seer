 package org.openmrs.module.radiotest.web.controller;
 
 import java.util.List;
 
 import javax.servlet.http.HttpSession;
 
 import org.openmrs.api.context.Context;
 import org.openmrs.module.radiotest.RadioPatient;
 import org.openmrs.module.radiotest.api.RadioPatientService;
 import org.openmrs.module.radiotest.propertyeditor.RadioPatientPropertyEditor;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.web.bind.WebDataBinder;
 import org.springframework.web.bind.annotation.InitBinder;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.context.request.WebRequest;
 import org.springframework.web.servlet.ModelAndView;
 import org.springframework.web.servlet.view.json.MappingJacksonJsonView;
 
 @Controller
 public class RadioSearchFormController {
 
 	private final String PATIENT_FORM = "/module/radiotest/searchPatientForm";
 	
 	@InitBinder
 	public void initBinder(WebRequest request, WebDataBinder binder){
 		binder.registerCustomEditor(RadioPatient.class, new RadioPatientPropertyEditor());
 	}
 	
 	@RequestMapping(value = PATIENT_FORM, method = RequestMethod.GET)
	public void showPatientForm(HttpSession session){
		RadioPatient patient = (RadioPatient) session.getAttribute("patient");
 		
		if (patient != null){
			session.removeAttribute("patient");
		}
 	}
 	
 	@RequestMapping(value = "/module/radiotest/searchPatient", method = RequestMethod.POST)
 	public ModelAndView searchPatient(@RequestParam("searchText") String searchString, ModelMap model){
 		List<RadioPatient> list = Context.getService(RadioPatientService.class).search(searchString);
 		if(list.isEmpty() || searchString.isEmpty()){
 			MappingJacksonJsonView view = new MappingJacksonJsonView();
 			view.addStaticAttribute("url", "/module/radiotest/patientForm.htm");
 			return new ModelAndView(view);
 		} else {
 			model.addAttribute("list", list);
 			return new ModelAndView("/module/radiotest/ajax/patientList", model);
 		}
 	}
 	
 	@RequestMapping(value = PATIENT_FORM, method = RequestMethod.POST)
 	public ModelAndView getPatient(@RequestParam("patientId") RadioPatient patient, HttpSession session){
 		session.setAttribute("patient", patient);
 		
 		return new ModelAndView("redirect:/module/radiotest/patientProfile.htm");
 	}
 }
