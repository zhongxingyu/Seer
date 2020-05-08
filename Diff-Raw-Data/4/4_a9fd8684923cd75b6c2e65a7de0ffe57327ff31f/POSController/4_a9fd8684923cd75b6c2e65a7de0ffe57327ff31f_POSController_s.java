 package scheduelp.controller;
 
 import javax.annotation.Resource;
 
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.ResponseBody;
 import org.springframework.web.bind.annotation.SessionAttributes;
 
 import scheduelp.common.ScheduelpException;
 import scheduelp.dto.UserSessionTO;
 import scheduelp.service.POSService;
 
 @Controller
 @SessionAttributes("userDetail")
 public class POSController extends BaseController {
 
 	@Resource
 	private POSService posService;
 
 	@RequestMapping(value = "/pos/add", method = RequestMethod.POST)
 	public @ResponseBody String addCourse(@ModelAttribute("userDetail") UserSessionTO userDetail,
 			@RequestParam("cid") String courseCode, Model model) throws ScheduelpException {
 		posService.addCourse(userDetail.getUserID(), userDetail.getDegree(), courseCode);
 		
 		return courseCode.concat(" has been added to your Program of Study");
 	}
 	
 	@RequestMapping(value = "/pos/remove", method = RequestMethod.POST)
 	public @ResponseBody String removeCourse(@ModelAttribute("userDetail") UserSessionTO userDetail,
 			@RequestParam("cid") String courseCode, Model model) throws ScheduelpException {
//		posService.removeCourse(userDetail.getUserID(), courseCode);
		
		return courseCode.concat(" has been removed from your Program of Study");
 	}
 
 }
