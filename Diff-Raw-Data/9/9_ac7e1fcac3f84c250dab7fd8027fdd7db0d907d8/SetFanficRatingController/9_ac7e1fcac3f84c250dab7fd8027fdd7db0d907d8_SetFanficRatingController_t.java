 package by.itransition.fanfic.controller.serviceController;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.security.core.context.SecurityContextHolder;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.ResponseBody;
 
 import by.itransition.fanfic.domain.Fanfic;
 import by.itransition.fanfic.domain.User;
 import by.itransition.fanfic.domain.Vote;
 import by.itransition.fanfic.service.FanficService;
 import by.itransition.fanfic.service.UserService;
 
 /**
  * Controller that set rating for fanfic of user.
  */
 @Controller
 @RequestMapping("/setFanficRating")
 public class SetFanficRatingController {
 
 	@Autowired
 	private FanficService fanficService;
 	
 	@Autowired
 	private UserService userService;
 	
 	@RequestMapping(value = "/{fanficId}/{userRating}",
 			method = RequestMethod.POST)
 	public @ResponseBody
 	String setFanficRating(@PathVariable("fanficId") int fanficId,
 			@PathVariable("userRating") int userRating, Model model) {
 		User user = userService.getUserByName(
 				SecurityContextHolder.getContext().getAuthentication().getName());
 		Fanfic fanfic = fanficService.getFanficById(fanficId);
 		Vote vote = fanfic.makeVote(userRating, user);
 		vote.setFanfic(fanfic);
		user.getVotes().remove(vote);
 		user.addVote(vote);
 		fanficService.save(fanfic);
 		userService.save(user);
 		return Double.toString(fanfic.getRating());
 	}
 }
