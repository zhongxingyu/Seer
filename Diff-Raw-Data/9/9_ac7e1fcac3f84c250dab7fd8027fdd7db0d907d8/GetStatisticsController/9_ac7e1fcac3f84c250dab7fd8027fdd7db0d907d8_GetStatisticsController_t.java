 package by.itransition.fanfic.controller.serviceController;
 
 import java.util.List;
 
 import org.json.simple.JSONArray;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.ResponseBody;
 
 import by.itransition.fanfic.service.UserService;
 
 /**
  * Controller that is used for get statistics of registration users
  * for last 10 hours.
  */
 @Controller
 @RequestMapping("/getStatistics")
 public class GetStatisticsController {
 
 	@Autowired
 	private UserService userService;
 	
 	@RequestMapping(method = RequestMethod.POST)
 	public @ResponseBody
 	String getStatistics(Model model) {
 		List<Integer> statistics = userService.getStatistics();
 		JSONArray statisticsJSON = new JSONArray();
		for (int statisticsByTimeIndex = 0; statisticsByTimeIndex < statistics.size();
 				++statisticsByTimeIndex) {
 			JSONArray statisticsByTimeJSON = new JSONArray();
 			statisticsByTimeJSON.add( 
 					String.valueOf(statisticsByTimeIndex + 1));
 			statisticsByTimeJSON.add(
 					statistics.get(statisticsByTimeIndex));
 			statisticsJSON.add(statisticsByTimeJSON);
 		}
 		return statisticsJSON.toJSONString();
 	}
 }
