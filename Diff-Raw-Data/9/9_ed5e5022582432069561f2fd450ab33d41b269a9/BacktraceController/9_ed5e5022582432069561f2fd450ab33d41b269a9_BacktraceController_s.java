 package com.krisjanis.balodis.controller;
 
 import java.text.SimpleDateFormat;
 import java.util.Map; 
 import java.util.Date;
 //import java.util.HashMap;
 
 import javax.validation.Valid;
 
 import org.springframework.beans.factory.annotation.Autowired;  
 import org.springframework.stereotype.Controller;  
 import org.springframework.validation.BindingResult;  
 import org.springframework.web.bind.annotation.ModelAttribute;  
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;  
 import org.springframework.web.bind.annotation.RequestMethod;   
 import org.springframework.web.servlet.mvc.support.RedirectAttributes;
 
 import com.krisjanis.balodis.model.Backtrace;
 import com.krisjanis.balodis.model.Comment;
 import com.krisjanis.balodis.model.Problem;
 import com.krisjanis.balodis.model.Version;
 import com.krisjanis.balodis.service.BacktraceService;
   
 @Controller  
 public class BacktraceController {
 	
 	@Autowired
     private BacktraceService backtraceService;
  
 	@RequestMapping("/index")
     public String home(Map<String, Object> map) {
 	
 		map.put("versionCount", backtraceService.listVersions().size());
 		map.put("problemCount", backtraceService.listProblems().size());
 		map.put("backtraceCount", backtraceService.listBacktraces().size());
 						
         return "home";
     }
 	
     @RequestMapping("/listBacktraces")
     public String listBacktrace(Map<String, Object> map) {
  
         map.put("backtraceList", backtraceService.listBacktraces());
  
         return "listBacktraces";
     }
  
     @RequestMapping("/viewBacktrace/{backtraceId}")
     public String viewBacktrace(@PathVariable("backtraceId") Integer backtraceId, Map<String, Object> map) {
  
         map.put("backtrace", backtraceService.viewBacktrace(backtraceId));
  
         return "viewBacktrace";
     }
     
     @RequestMapping(value = "/addBacktraceForm", method = RequestMethod.GET)
     public String backtraceForm(Map<String, Object> map) {
     	
     	map.put("newBacktrace", new Backtrace());
     	map.put("problemList", backtraceService.listProblems());
     	return "addBacktrace";
     }
     
     @RequestMapping(value = "/addNewBacktrace", method = RequestMethod.POST)
     public String addBacktrace(
     		@Valid @ModelAttribute("newBacktrace") Backtrace newBacktrace,
     		BindingResult result,
     		Map<String, Object> map,
     		RedirectAttributes attributes) {
     	
     	if (!result.hasErrors()) {
     		Date nowTemp = new Date();
 			String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(nowTemp);
         	newBacktrace.setDate(now);
         	newBacktrace.setDateModified(now);
         	newBacktrace.setIsDeleted(false);
         	backtraceService.addBacktrace(newBacktrace);
     		String success = "SUCCESS! You have added a new backtrace. Backtrace count: " + backtraceService.listBacktraces().size();
     		attributes.addFlashAttribute("message", success);
     		return "redirect:/listBacktraces.html";
     	} else {
     		String error = "OOPS! Error occured!";
     		map.put("problemList", backtraceService.listProblems());
     		map.put("message", error);
     		return "addBacktrace";
     	}
     }
  
     @RequestMapping("/deleteBacktrace/{backtraceId}")
     public String deleteBacktrace(
     		@PathVariable("backtraceId") Integer backtraceId,
     		RedirectAttributes attributes) {
  
     	backtraceService.removeBacktrace(backtraceId);
     	String success = "You have successfully deleted a backtrace. Backtrace count: " + backtraceService.listBacktraces().size();
 		attributes.addFlashAttribute("message", success);
 		
         return "redirect:/listBacktraces.html";
     }
     
     @RequestMapping(value = "/editBacktraceForm/{backtraceId}", method = RequestMethod.GET)
     public String backtraceEditForm(
     		@PathVariable("backtraceId") Integer backtraceId,
     		Map<String, Object> map) {
     	
     	map.put("backtrace", backtraceService.getBacktrace(backtraceId));
     	map.put("problemList", backtraceService.listProblems());
     	return "editBacktrace";
     }
     
     @RequestMapping(value = "/editBacktrace/{backtraceId}", method = RequestMethod.POST)
     public String editBacktrace(
     		@ModelAttribute("backtrace") @Valid Backtrace backtrace,
     		BindingResult result,
     		@PathVariable("backtraceId") Integer backtraceId,
     		Map<String, Object> map,
     		RedirectAttributes attributes) {
     	
     	if (!result.hasErrors()) {
     		Backtrace tempBacktrace = backtraceService.getBacktrace(backtraceId);
     		Date nowTemp = new Date();
 			String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(nowTemp);
         	backtrace.setDateModified(now);
         	backtrace.setDate(tempBacktrace.getDate());
         	backtrace.setIsDeleted(false);
         	backtrace.setId(backtraceId);
         	backtraceService.updateBacktrace(backtrace);
     		String success = "SUCCESS! You have updated the backtrace. Backtrace count: " + backtraceService.listBacktraces().size();
     		attributes.addFlashAttribute("message", success);
     		return "redirect:/listBacktraces.html";
     	} else {
     		String error = "OOPS! Error occured!";
     		map.put("problemList", backtraceService.listProblems());
     		map.put("message", error);
     		return "editBacktrace";
     	}
     }
     
     @RequestMapping("/listVersions")
     public String listVersion(Map<String, Object> map) {
 
         map.put("versionList", backtraceService.listVersions());
  
         return "listVersions";
     }
     
     @RequestMapping(value = "/addVersionForm", method = RequestMethod.GET)
     public String versionForm(Map<String, Object> map) {
     	
     	map.put("newVersion", new Version());
     	return "addVersion";
     }
     
     @RequestMapping(value = "/addNewVersion", method = RequestMethod.POST)
     public String addVersion(
     		@Valid @ModelAttribute("newVersion") Version newVersion, 
     		BindingResult result,
     		Map<String, Object> map,
     		RedirectAttributes attributes) {
     	
     	if (!result.hasErrors()){
     		String formInputVersion = newVersion.getVersion();
     		if (backtraceService.duplicateCheck(formInputVersion, Version.class, "version")){
     			Date nowTemp = new Date();
     			String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(nowTemp);
             	newVersion.setDate(now);
             	newVersion.setDateModified(now);
             	newVersion.setIsDeleted(false);
     			backtraceService.addVersion(newVersion);
         		String success = "SUCCESS! You have added a new software version. Version count: " + backtraceService.listVersions().size();
         		attributes.addFlashAttribute("message", success);
         		return "redirect:/listVersions.html";
     		} else {
     			String errorGlobal = "OOPS! Error occured!";
         		map.put("message", errorGlobal);
         		String errorDuplicate = "Duplicate record found!";
         		map.put("messageDuplicate", errorDuplicate);
         		return "addVersion";
     		}
     	} else {
     		String errorGlobal = "OOPS! Error occured!";
     		map.put("message", errorGlobal);
     		return "addVersion";
     	}
     }
     
     @RequestMapping(value = "/editVersionForm/{versionId}", method = RequestMethod.GET)
     public String versionEditForm(
     		@PathVariable("versionId") Integer versionId,
     		Map<String, Object> map) {
     	
     	map.put("version", backtraceService.getVersion(versionId));
     	return "editVersion";
     }
     
     @RequestMapping(value = "/editVersion/{versionId}", method = RequestMethod.POST)
     public String editVersion(
     		@Valid @ModelAttribute("version") Version version,
     		BindingResult result,
     		@PathVariable("versionId") Integer versionId,
     		Map<String, Object> map,
     		RedirectAttributes attributes) {
     	
     	if (!result.hasErrors()){
     		String formInputVersion = version.getVersion();
     		Version tempVersion = backtraceService.getVersion(versionId);
     		if ((tempVersion.getVersion()).equals(formInputVersion)) {
     			Date nowTemp = new Date();
     			String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(nowTemp);
             	version.setDateModified(now);
             	version.setDate(tempVersion.getDate());
             	version.setIsDeleted(false);
             	version.setId(versionId);
     			backtraceService.updateVersion(version);
         		String success = "SUCCESS! You have updated the software version. Version count: " + backtraceService.listVersions().size();
         		attributes.addFlashAttribute("message", success);
         		return "redirect:/listVersions.html";
     		} else {
 	    		if (backtraceService.duplicateCheck(formInputVersion, Version.class, "version")){
 	    			Date nowTemp = new Date();
 	    			String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(nowTemp);
 	            	version.setDateModified(now);
 	            	version.setDate(tempVersion.getDate());
 	            	version.setIsDeleted(false);
 	            	version.setId(versionId);
 	    			backtraceService.updateVersion(version);
 	        		String success = "SUCCESS! You have updated the software version. Version count: " + backtraceService.listVersions().size();
 	        		attributes.addFlashAttribute("message", success);
 	        		return "redirect:/listVersions.html";
 	    		} else {
 	    			String errorGlobal = "OOPS! Error occured!";
 	        		map.put("message", errorGlobal);
 	        		String errorDuplicate = "Duplicate record found!";
 	        		map.put("messageDuplicate", errorDuplicate);
 	        		return "editVersion";
 	    		}
     		}
     	} else {
     		String errorGlobal = "OOPS! Error occured!";
     		map.put("message", errorGlobal);
     		return "editVersion";
     	}
     }
     
     @RequestMapping("/listProblems")
     public String listProblems(Map<String, Object> map) {
     	
         map.put("problemList", backtraceService.listProblems());       
         
         return "listProblems";
     }
     
     @RequestMapping(value = "/addProblemForm", method = RequestMethod.GET)
     public String problemForm(Map<String, Object> map) {
     	
     	map.put("newProblem", new Problem());
     	map.put("versionList", backtraceService.listVersions());
     	return "addProblem";
     }
     
     @RequestMapping(value = "/addNewProblem", method = RequestMethod.POST)
     public String addProblem(
     		@Valid @ModelAttribute("newProblem") Problem newProblem, 
     		BindingResult result,
     		Map<String, Object> map,
     		RedirectAttributes attributes) {
     	
     	if (!result.hasErrors()){
     		String formInputProblem = newProblem.getProblem();
     		if (backtraceService.duplicateCheck(formInputProblem, Problem.class, "problem")){
     			Date nowTemp = new Date();
     			String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(nowTemp);
             	newProblem.setDate(now);
             	newProblem.setDateModified(now);
             	newProblem.setIsDeleted(false);
     			backtraceService.addProblem(newProblem);
         		String success = "SUCCESS! You have added a new problem. Problem count: " + backtraceService.listProblems().size();
         		attributes.addFlashAttribute("message", success);
         		return "redirect:/listProblems.html";
     		} else {
     			String errorGlobal = "OOPS! Error occured!";
         		map.put("message", errorGlobal);
         		String errorDuplicate = "Duplicate record found!";
         		map.put("messageDuplicate", errorDuplicate);
         		map.put("versionList", backtraceService.listVersions());
         		return "addProblem";
     		}
     	} else {
     		String error = "OOPS! Error occured!";
     		map.put("message", error);
     		map.put("versionList", backtraceService.listVersions());
     		return "addProblem";
     	}
     }
     
     @RequestMapping(value = "/editProblemForm/{problemId}", method = RequestMethod.GET)
     public String problemEditForm(
     		@PathVariable("problemId") Integer problemId,
     		Map<String, Object> map) {
     	
     	map.put("problem", backtraceService.getProblem(problemId));
     	map.put("versionList", backtraceService.listVersions());
     	return "editProblem";
     }
     
     @RequestMapping(value = "/editProblem/{problemId}", method = RequestMethod.POST)
     public String editProblem(
     		@Valid @ModelAttribute("problem") Problem problem,
     		BindingResult result,
     		@PathVariable("problemId") Integer problemId,
     		Map<String, Object> map,
     		RedirectAttributes attributes) {
     	
     	if (!result.hasErrors()){
     		String formInputProblem = problem.getProblem();
     		Problem tempProblem = backtraceService.getProblem(problemId);
     		if ((tempProblem.getProblem()).equals(formInputProblem)) {
     			Date nowTemp = new Date();
     			String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(nowTemp);
             	problem.setDateModified(now);
             	problem.setDate(tempProblem.getDate());
             	problem.setIsDeleted(false);
             	problem.setId(problemId);
     			backtraceService.updateProblem(problem);
         		String success = "SUCCESS! You have updated the problem. Problem count: " + backtraceService.listProblems().size();
         		attributes.addFlashAttribute("message", success);
         		return "redirect:/listProblems.html";
     		} else {
 	    		if (backtraceService.duplicateCheck(formInputProblem, Problem.class, "problem")){
 	    			Date nowTemp = new Date();
 	    			String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(nowTemp);
 	            	problem.setDateModified(now);
 	            	problem.setDate(tempProblem.getDate());
 	            	problem.setIsDeleted(false);
 	            	problem.setId(problemId);
 	    			backtraceService.updateProblem(problem);
 	        		String success = "SUCCESS! You have updated the problem. Problem count: " + backtraceService.listProblems().size();
 	        		attributes.addFlashAttribute("message", success);
 	        		return "redirect:/listProblems.html";
 	    		} else {
 	    			String errorGlobal = "OOPS! Error occured!";
 	        		map.put("message", errorGlobal);
 	        		String errorDuplicate = "Duplicate record found!";
 	        		map.put("messageDuplicate", errorDuplicate);
 	        		map.put("versionList", backtraceService.listVersions());
 	        		return "editProblem";
 	    		}
     		}
     	} else {
     		String errorGlobal = "OOPS! Error occured!";
     		map.put("message", errorGlobal);
     		map.put("versionList", backtraceService.listVersions());
     		return "editProblem";
     	}
     }
     
     @RequestMapping("/listComments")
     public String listComments(Map<String, Object> map) {
  
     	map.put("comment", new Comment());
         map.put("commentList", backtraceService.listComments());
  
         return "comments";
     }
     
     @RequestMapping(value = "/addComment", method = RequestMethod.POST)
     public String addComment(
     		@Valid @ModelAttribute("comment") Comment comment, 
     		BindingResult result,
     		Map<String, Object> map) {
  
         if (!result.hasErrors()) {
         	if (comment.getAuthor() == ""){
         		comment.setAuthor("anonymous");
         	}
         	Date nowTemp = new Date();
 			String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(nowTemp);
         	comment.setDate(now);
         	comment.setIsDeleted(false);
         	backtraceService.addComment(comment);
         	String success = "Thank You for commenting!";
        	map.put("message", success);
         	map.put("comment", new Comment());
             map.put("commentList", backtraceService.listComments());
             return "comments";
         } else {
         	String errorGlobal = "OOPS! Error occured while adding comment!";
         	map.put("message", errorGlobal);
             map.put("commentList", backtraceService.listComments());
         	return "comments";
         } 	
     }
 }
