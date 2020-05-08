 package org.openmrs.module.radiotest.web.controller;
 
 import java.util.Collections;
 import java.util.List;
 
 import javax.servlet.http.HttpSession;
 
 import org.openmrs.api.context.Context;
 import org.openmrs.module.radiotest.RadioPatient;
 import org.openmrs.module.radiotest.RadioResult;
 import org.openmrs.module.radiotest.RadioSignature;
 import org.openmrs.module.radiotest.RadioTransExam;
 import org.openmrs.module.radiotest.RadioTransaction;
 import org.openmrs.module.radiotest.api.RadioInventoryService;
 import org.openmrs.module.radiotest.api.RadioPatientService;
 import org.openmrs.module.radiotest.api.RadioTransactionService;
 import org.openmrs.module.radiotest.model.RadioStockModel;
 import org.openmrs.module.radiotest.propertyeditor.RadioComparator;
 import org.openmrs.module.radiotest.propertyeditor.RadioSignaturePropertyEditor;
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
 	private final String RESULT_PDF = "/module/radiotest/printResult";
 	
 	@InitBinder
 	public void initBinder(WebRequest request, WebDataBinder binder){
 		binder.registerCustomEditor(RadioTransaction.class, new RadioTransactionPropertyEditor());
 		binder.registerCustomEditor(RadioTransExam.class, new RadioTransExamPropertyEditor());
 		binder.registerCustomEditor(RadioSignature.class, new RadioSignaturePropertyEditor());
 	}
 	
 	@ModelAttribute("result")
 	public RadioResult getResult(){
 		return new RadioResult(true);
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
 	
 	@ModelAttribute("signatures")
 	public List<RadioSignature> getAllSignatures(){
 		return Context.getService(RadioTransactionService.class).getAllSignatures();
 	}
 	
 	@RequestMapping(value = {RESULTS_PAGE, RESULTS_FORM, RESULT_PDF, TRANSACTION_PAGE}, method = RequestMethod.GET)
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
 		e = Context.getService(RadioTransactionService.class).updateTransExam(e);
 		
 		model.addAttribute("transExam", e);
 		model.addAttribute("count", request.getParameter("count"));
		
		String template = escapeNewline(e.getExam().getType().getTemplate(), "\\\\n");
		System.out.println(template);
		model.addAttribute("template", template);
		
 		if(e.hasResult()){
 			RadioResult result = e.getResult();
 			model.addAttribute("result", result);
 			model.addAttribute("findings", escapeNewline(result.getFindings(), "<br>"));
 			addInventoryModel(Context.getService(RadioInventoryService.class), model);
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
 								ModelMap model, HttpSession session){
 		RadioTransactionService ts = Context.getService(RadioTransactionService.class);
 		RadioInventoryService is = Context.getService(RadioInventoryService.class);
 		RadioPatient patient = (RadioPatient) session.getAttribute("patient");
 		
 		if(!result.getDraft()){
 			e.setPending(false);
 			addInventoryModel(is, model);
 			model.addAttribute("findings", escapeNewline(result.getFindings(), "<br>"));
 		}
 		
 		if (result.getId() != null){
 			result = ts.saveResult(result);
 		} else {
 			e.addFinding(result);
 			e.getTransaction().update();
 			ts.saveTransExam(e);
 		}
 		
 		if(patient != null){
 			patient = Context.getService(RadioPatientService.class).updatePatient(patient);
 			model.addAttribute("patient", patient);
 		}
 		
 		model.addAttribute("transExam", ts.updateTransExam(e));
 		model.addAttribute("result", result);
 		model.addAttribute("patient", patient);
 		
 		return new ModelAndView("/module/radiotest/resultsForm", model);
 	}
 	
 	@RequestMapping(value = "/module/radiotest/editResultForm", method = RequestMethod.POST)
 	public ModelAndView editResult(@RequestParam("examId") RadioTransExam exam, ModelMap model){
 		model.addAttribute("transExam", exam);
 		model.addAttribute("result", new RadioResult(true));
 		
 		return new ModelAndView("/module/radiotest/resultsForm", model);
 	}
 	
 	@RequestMapping(value = "/module/radiotest/prtRes", method = RequestMethod.POST)
 	public ModelAndView printResult(@RequestParam("examId") RadioTransExam exam, @RequestParam("sign") RadioSignature sign, ModelMap model){
 		
 		model.addAttribute("transExam", exam);
 		model.addAttribute("patient", exam.getPatient());
 		
 		if (exam.hasResult()){
 			model.addAttribute("result", exam.getResult());
 		}
 		
 		model.addAttribute("signature", sign);
 		
 		return new ModelAndView("/module/radiotest/printResult", model);
 	}
 	
 	private void addInventoryModel(RadioInventoryService is, ModelMap model){
 		model.addAttribute("itemTypes", is.getAllItemTypes());
 		model.addAttribute("items", is.getAllItems());
 		model.addAttribute("stockModel", new RadioStockModel());
 	}
 	
 	private String escapeNewline(String str, String escapeStr){
		return str.replaceAll("(\r\n|\n)", escapeStr);
 	}
 }
