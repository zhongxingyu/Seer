 package org.openmrs.module.radiotest.web.controller;
 
 import java.util.List;
 
 import org.openmrs.api.context.Context;
 import org.openmrs.module.radiotest.RadioCategory;
 import org.openmrs.module.radiotest.RadioExam;
 import org.openmrs.module.radiotest.RadioExamType;
 import org.openmrs.module.radiotest.RadioFeeType;
 import org.openmrs.module.radiotest.api.RadioExamService;
 import org.openmrs.module.radiotest.api.RadioPatientService;
 import org.openmrs.module.radiotest.api.RadioTransactionService;
 import org.openmrs.module.radiotest.model.RadioExamModel;
 import org.openmrs.module.radiotest.propertyeditor.RadioCategoryPropertyEditor;
 import org.openmrs.module.radiotest.propertyeditor.RadioExamPropertyEditor;
 import org.openmrs.module.radiotest.propertyeditor.RadioExamTypePropertyEditor;
 import org.openmrs.module.radiotest.propertyeditor.RadioFeeTypePropertyEditor;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.web.bind.WebDataBinder;
 import org.springframework.web.bind.annotation.InitBinder;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.context.request.WebRequest;
 import org.springframework.web.servlet.ModelAndView;
 
 @Controller
 public class RadioExamController {
 
 	private final String EXAM_FORM = "/module/radiotest/examForm";
 	
 	@InitBinder
 	public void initBinder(WebRequest request, WebDataBinder binder){
 		binder.registerCustomEditor(RadioExamType.class, new RadioExamTypePropertyEditor());
 		binder.registerCustomEditor(RadioCategory.class, new RadioCategoryPropertyEditor());
 		binder.registerCustomEditor(RadioExam.class, new RadioExamPropertyEditor());
 		binder.registerCustomEditor(RadioFeeType.class, new RadioFeeTypePropertyEditor());
 	}
 	
 	@RequestMapping(value = EXAM_FORM, method = RequestMethod.GET)
 	public void showExamForm(){
 		
 	}
 	
 	@ModelAttribute("exams")
 	public List<RadioExam> getExams(){
 		return Context.getService(RadioExamService.class).getAllExams(true);
 	}
 	
 	@ModelAttribute("categories")
 	public List<RadioCategory> getCategories(){
 		return Context.getService(RadioPatientService.class).getAllCategories();
 	}
 	
 	@ModelAttribute("types")
 	public List<RadioExamType> getExamTypes() {
 		return Context.getService(RadioExamService.class).getAllExamTypes();
 	}
 	
 	@ModelAttribute("feeTypes")
 	public List<RadioFeeType> getFeeTypes(){
 		return Context.getService(RadioTransactionService.class).getAllFeeTypes();
 	}
 	
 	@ModelAttribute("examModel")
 	public RadioExamModel getExamModel(){
 		return new RadioExamModel();
 	}
 	
 	@RequestMapping(value = EXAM_FORM, method = RequestMethod.POST)
 	public ModelAndView saveExam(@ModelAttribute("examModel") RadioExamModel examModel, ModelMap model){
 		RadioExam exam = examModel.getFullExam();
 		Context.getService(RadioExamService.class).saveExam(exam);
 		
 		return new ModelAndView("redirect:" + EXAM_FORM + ".htm");
 	}
 	
 	@RequestMapping(value = "/module/radiotest/loadExam", method = RequestMethod.POST)
 	public ModelAndView loadExam(@RequestParam("examId") RadioExam exam, ModelMap model){
 		exam = Context.getService(RadioExamService.class).updateExam(exam);
 		model.addAttribute("examModel", new RadioExamModel(exam));
 		
 		return new ModelAndView(EXAM_FORM, model);
 	}
 	
 	@RequestMapping(value = "/module/radiotest/nullExam", method = RequestMethod.POST)
 	public ModelAndView nullExam(@RequestParam("eid") RadioExam exam, @RequestParam("action") String action){
 		RadioExamService es = Context.getService(RadioExamService.class);
 		if (action.equalsIgnoreCase("void")){
 			exam.setVoided(!exam.getVoided());
 			es.saveExam(exam);
 		} else if (action.equalsIgnoreCase("delete")){
 			es.deleteExam(exam);
 		}
 		
 		return new ModelAndView("redirect:" + EXAM_FORM + ".htm"); 
 	}
 }
