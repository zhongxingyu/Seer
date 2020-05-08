 package hk.com.novare.tempoplus.bmnmanager.consolidation;
 
 import hk.com.novare.tempoplus.bmnmanager.mantis.Mantis;
 import hk.com.novare.tempoplus.employee.Employee;
 
 import java.sql.SQLException;
 import java.util.ArrayList;
 
 import javax.inject.Inject;
 
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.ResponseBody;
 
 @Controller
 @RequestMapping("/consolidation")
 public class ConsolidationController {
 
 	@Inject
 	ConsolidationService consolidationService;
 
 	@RequestMapping(value = "/create", method = RequestMethod.POST)
 	private String createConsolidatedTimesheet(@RequestParam String name,
 			@RequestParam String periodStart, @RequestParam String periodEnd) {
 
 		// consolidationService.createConsolidatedTimesheet(name, periodStart,
 		// periodEnd);
 
 		return "upload";
 	}
 
 //	@RequestMapping(value = "/check", method = RequestMethod.POST)
 //	private String isReadyForConsolidation(@RequestParam int id) {
 //		return consolidationService.isReadyForConsolidation(id);
 //	}
 	
 	@RequestMapping(value = "/ajaxFetchConsolidations", method=RequestMethod.GET)
 	public @ResponseBody ArrayList<ConsolidationDTO> fetchConsolidations(ModelMap modelMap)  {
 		return consolidationService.viewConsolidation();
 	}
 	

	
 	@RequestMapping(value = "/view", method = RequestMethod.GET)
 	public String viewConsolidated() throws SQLException {
		return "ViewBMN";
 	}
 	
	
	
 	@RequestMapping(value = "/mail", method = RequestMethod.GET)
 	public String mailTimeSheet() {
 	return "ViewSendMail";
 	}
 	
 	@RequestMapping(value = "/ajaxUpdateConsolidations", method = RequestMethod.POST)
 	public @ResponseBody Boolean updateConsolidations(@RequestParam String timeIn,
 			@RequestParam String timeOut,
 			@RequestParam String employeeId,
 			@RequestParam String date) {
 		System.out.println("here at controller");
 		consolidationService.updateConsolidations(employeeId, timeIn, timeOut, date);
 		
 		return true;
 	}
 	
 	@RequestMapping(value = "/ajaxFetchTickets", method = RequestMethod.POST)
 	public @ResponseBody ArrayList<Mantis> fetchTicketDetails(
 			@RequestParam String employeeId) {
 		
 		System.out.println("@ajaxfetchtickets");
 		return consolidationService.fetchTicket(employeeId);
 		
 	}
 	
 	
 	
 	
 
 	
 	
 	
 }
