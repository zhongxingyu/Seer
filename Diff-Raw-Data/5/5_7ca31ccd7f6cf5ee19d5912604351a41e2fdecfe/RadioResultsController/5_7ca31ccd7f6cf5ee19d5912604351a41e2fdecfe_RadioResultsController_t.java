 package org.openmrs.module.radiotest.web.controller;
 
import java.util.Collections;
 import java.util.List;
 
 import javax.servlet.http.HttpSession;
 
 import org.openmrs.api.context.Context;
 import org.openmrs.module.radiotest.RadioPatient;
 import org.openmrs.module.radiotest.RadioResult;
 import org.openmrs.module.radiotest.RadioTransExam;
 import org.openmrs.module.radiotest.RadioTransaction;
 import org.openmrs.module.radiotest.api.RadioPatientService;
 import org.openmrs.module.radiotest.api.RadioTransactionService;
import org.openmrs.module.radiotest.propertyeditor.RadioComparator;
 import org.openmrs.module.radiotest.propertyeditor.RadioTransExamPropertyEditor;
 import org.openmrs.module.radiotest.propertyeditor.RadioTransactionPropertyEditor;
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
 public class RadioResultsController {
 	
 	private final String RESULTS_PAGE = "/module/radiotest/results";
 	private final String RESULTS_FORM = "/module/radiotest/resultsForm";
 	private final String TRANSACTION_PAGE = "/module/radiotest/transactions";
 	
 	@InitBinder
 	public void initBinder(WebRequest request, WebDataBinder binder){
 		binder.registerCustomEditor(RadioTransaction.class, new RadioTransactionPropertyEditor());
 		binder.registerCustomEditor(RadioTransExam.class, new RadioTransExamPropertyEditor());
 	}
 	
 	@ModelAttribute("result")
 	public RadioResult getResult(){
 		return new RadioResult();
 	}
 	
 	@ModelAttribute("transList")
 	public List<RadioTransaction> getTransactions(HttpSession session, ModelMap model){
 		RadioPatient patient = (RadioPatient) session.getAttribute("patient");
 		List<RadioTransaction> transList = null;
 		if (patient != null){
 			transList = Context.getService(RadioTransactionService.class).getTransactions(patient);
			Collections.sort(transList, Collections.reverseOrder(new RadioComparator()));
 		}
 		
 		return transList;
 	}
 	
 	@RequestMapping(value = {RESULTS_PAGE, TRANSACTION_PAGE}, method = RequestMethod.GET)
 	public void showTransactions(HttpSession session, ModelMap model){
 		RadioPatient patient = (RadioPatient) session.getAttribute("patient");
 		
 		if(patient != null){
 			patient = Context.getService(RadioPatientService.class).updatePatient(patient);
 			model.addAttribute("patient", patient);
 		}
 	}
 	
 	@RequestMapping(value = "/module/radiotest/getExamList", method = RequestMethod.POST)
 	public ModelAndView getExamList(@RequestParam("transId") RadioTransaction trans, ModelMap model){
 		model.addAttribute("exams", trans.getExams());
 		
 		return new ModelAndView("/module/radiotest/ajax/examList", model);
 	}	
 	
 	@RequestMapping(value = RESULTS_PAGE, method = RequestMethod.POST)
 	public ModelAndView editExamResults(@RequestParam("examId") RadioTransExam e, WebRequest request, ModelMap model){
 		model.addAttribute("transExam", e);
 		model.addAttribute("count", request.getParameter("count"));
 		model.addAttribute("template", escapeNewline(e.getExam().getType().getTemplate(), "\\n"));
 		if(e.hasResult()){
 			RadioResult result = e.getResult();
 			model.addAttribute("result", result);
 			model.addAttribute("findings", escapeNewline(result.getFindings(), "<br>"));
 		}
 		
 		return new ModelAndView("/module/radiotest/resultsForm", model);
 	}
 	
 	@RequestMapping(value = "/module/radiotest/borrowResults", method = RequestMethod.POST)
 	public ModelAndView toggleBorrowed(@RequestParam("examId") RadioTransExam exam, ModelMap model){
 		RadioTransactionService ts = Context.getService(RadioTransactionService.class);
 		
 		exam = ts.updateTransExam(exam);
 		exam.setBorrowed(!exam.isBorrowed());
 		ts.saveTransExam(exam);
 		
 		RadioTransaction trans = exam.getTransaction();
 		model.addAttribute("exams", trans.getExams());
 		
 		return new ModelAndView("/module/radiotest/ajax/examList", model);
 	}
 	
 	@RequestMapping(value = RESULTS_FORM, method = RequestMethod.POST)
 	public ModelAndView saveResult(@ModelAttribute("result") RadioResult result, @RequestParam("examId") RadioTransExam e, 
 								ModelMap model){
 		if(!result.isDraft()){
 			e.setPending(false);
 		}
 		e.addFinding(result);
 		e.getTransaction().update();
 		
 		model.addAttribute("transExam", Context.getService(RadioTransactionService.class).saveTransExam(e));
 		model.addAttribute("result", result);
 		model.addAttribute("findings", escapeNewline(result.getFindings(), "<br>"));
 		
 		return new ModelAndView("/module/radiotest/resultsForm", model);
 	}
 	
 	@RequestMapping(value = "/module/radiotest/editResultForm", method = RequestMethod.POST)
 	public ModelAndView editResult(@RequestParam("examId") RadioTransExam exam, ModelMap model){
 		model.addAttribute("transExam", exam);
 		model.addAttribute("result", new RadioResult());
 		
 		return new ModelAndView("/module/radiotest/resultsForm", model);
 	}
 	
 	private String escapeNewline(String str, String escapeStr){
 		return str.replace("\n", escapeStr);
 	}
 }
