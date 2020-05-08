 package dk.itu.realms.controller;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.ResponseBody;
 
 import dk.itu.realms.model.entity.Mark;
 import dk.itu.realms.model.entity.reduced.MarkReduced;
 import dk.itu.realms.services.MarkService;
 
 @Controller
 @RequestMapping("/update")
 public class MarkServiceController {
 
 	@Autowired
 	private MarkService markService;
 
 	@RequestMapping(method = RequestMethod.GET)
 	@ResponseBody
 	public MarkReduced getMark(@RequestParam("lat") String lat,
 			@RequestParam("lon") String lon,
 			@RequestParam("realm") String realmId,
 			@RequestParam("userid") String userId) {
 		Mark mark = markService.getMark(lat, lon, Long.parseLong(realmId),
 				userId);
 		MarkReduced markre;
 		if(mark != null) {
 			markre = new MarkReduced(mark);
 		} else {
 			markre = new MarkReduced();
 		}
 		return markre;
 	}
 
 	@RequestMapping(method= RequestMethod.POST, value="/mark/rate")
 	public void rateInfo(@RequestParam("realmid") String realmId,
 			@RequestParam("markid") String markId,
 			@RequestParam("rating") String rating,
			@RequestParam("userId") String userId) {
		System.out.println("Rate ");
 		markService.rateInfo(Long.parseLong(realmId), Long.parseLong(markId), Integer.parseInt(rating), userId);
 	}
 	
 	@RequestMapping(method= RequestMethod.POST, value="/mark/markoption")
 	public void markOption(@RequestParam("realmid") String realmId,
 			@RequestParam("markid") String markId,
 			@RequestParam("optionid") String optionId,
 			@RequestParam("userid") String userId) {
		System.out.println("Mark option");
 		markService.markOption(Long.parseLong(realmId), Long.parseLong(markId), Integer.parseInt(optionId), userId);
 	}
 	
 	
 }
