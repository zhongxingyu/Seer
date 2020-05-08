 package com.dasanjos.match.controller;
 
 import java.util.Random;
 
 import org.joda.time.DateTime;
 import org.joda.time.Period;
 import org.joda.time.format.DateTimeFormat;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.servlet.ModelAndView;
 
 import com.dasanjos.match.MatchRepository;
 
 @Controller
 public class Matches {
 
 	private MatchRepository repository = new MatchRepository();
 
	@RequestMapping(value={"/", "/matches"}, method = RequestMethod.GET)
 	public ModelAndView root() {
 		ModelAndView result = new ModelAndView("index");
 		result.getModel().put("today", getUrl(new DateTime()));
 		return result;
 	}
 
	@RequestMapping(value={"/{year}/{month}/{day}", "/matches/{year}/{month}/{day}"}, method = RequestMethod.GET)
 	public ModelAndView matches(@PathVariable("year") int year,
 			@PathVariable("month") int month, @PathVariable("day") int day) {
 
 		DateTime date = new DateTime(year, month, day, new Random().nextInt(24), 0);
 		ModelAndView result = new ModelAndView("index");
 		result.getModel().put("today", getUrl(new DateTime()));
 		result.getModel().put("date", DateTimeFormat.forPattern("EEEE, dd MMMM").print(date));
 		result.getModel().put("matches", repository.getMatches(date));
 		result.getModel().put("earlier", getUrl(date.minus(Period.days(1))));
 		result.getModel().put("later", getUrl(date.plus(Period.days(1))));
 		return result;
 	}
 
 	protected String getUrl(DateTime date) {
 		return "/matches/" + date.getYear() + "/" + date.getMonthOfYear() + "/"
 				+ date.getDayOfMonth();
 	}
 	
 }
