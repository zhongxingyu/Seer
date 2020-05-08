 package com.zotyo.diary.jsonws;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.List;
 import java.util.Properties;
 
 import javax.annotation.PostConstruct;
 
 import org.apache.log4j.Logger;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.http.HttpStatus;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.ExceptionHandler;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.ResponseBody;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.ResponseStatus;
 
 import com.zotyo.diary.exception.DayNotFoundException;
 import com.zotyo.diary.persistence.DiaryDAO;
 import com.zotyo.diary.pojos.Day;
 import com.zotyo.diary.util.DateUtil;
 import com.zotyo.diary.web.DiaryHelper;
 import com.zotyo.diary.pojos.Event;
 
 @Controller
 @RequestMapping("/days")
 public class DayJSONController {
 	private static Logger logger = Logger.getLogger(DayJSONController.class);
 
 	@Autowired
 	private DiaryHelper diaryHelper;
 
 	@Autowired
 	private DiaryDAO diaryDAO;
 
 	private String password;
 
 	@RequestMapping(value = "/form", method = RequestMethod.POST, consumes = "application/x-www-form-urlencoded")
 	@ResponseBody
 	public Day addDayForm(@RequestParam String keyword,
 			@RequestParam String theDay,
 			@RequestParam String descriptionOfTheDay,
 			@RequestParam String startDate, @RequestParam String duration,
 			@RequestParam String initialEvent) {
 		if (diaryHelper.md5(keyword).equals(password)) {
 			Day day = new Day();
 			GregorianCalendar theDayCal = diaryHelper.getDayCal(theDay);
 			day.setTheDay(DateUtil.resetHMS(theDayCal.getTime()));
 			day.setDescriptionOfTheDay(descriptionOfTheDay);
 
 			Event event = new Event();
 			event.setDescription(initialEvent);
 			event.setDuration(diaryHelper.getDuration(duration));
 			GregorianCalendar startDateCal = diaryHelper
 					.getStartDateCal(startDate);
 			event.setStartTime(startDateCal.getTime());
 			day.getEventsOfTheDay().add(event);
 			day.setId(diaryDAO.addDay(day));
 
 			return day;
 		}
 		return new Day();
 	}
 
 	@RequestMapping(method = RequestMethod.GET)
 	@ResponseBody
 	public List<Day> getDays() {
 		return diaryDAO.getAllDaysInDiary();
 	}
 
 	@RequestMapping(value = "/{year}/{month}", method = RequestMethod.GET)
 	@ResponseBody
 	public List<Day> getDaysForAMonth(@PathVariable int year,
 			@PathVariable int month) {
 		return diaryDAO.getDaysForAMonth(year, month - 1);
 	}
 
 	@RequestMapping(value = "/{year}/{month}/{day}", method = RequestMethod.GET)
 	@ResponseBody
 	public Day getDay(@PathVariable int year, @PathVariable int month,
 			@PathVariable int day) throws DayNotFoundException {
 		Calendar c = GregorianCalendar.getInstance();
		logger.info("*** TimeZone details" + c.getTimeZone());
 		c.set(year, month - 1, day);
 		Date date = DateUtil.resetHMS(c.getTime());
		logger.info("*** Checking the date: " + date);
		logger.info("*** Checking the date: " + date.getTime());
 		Day d = diaryDAO.getDay(date);
 
 		/*if (d == null) {
 			throw new DayNotFoundException("Day not found");
 		}*/
 		return d;
 	}
 
 	@PostConstruct
 	public void init() {
 		try {
 			InputStream inputStream = ClassLoader
 					.getSystemResourceAsStream("diary.properties");
 			Properties props = new Properties();
 			props.load(inputStream);
 
 			password = props.getProperty("keyword");
 		} catch (IOException ioex) {
 			ioex.printStackTrace();
 		}
 	}
 
 	@ExceptionHandler(DayNotFoundException.class)
 	@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Day not found in diary!")
 	public void handleDataFormatException(DayNotFoundException ex) {
 
 		logger.info("Handlng DayNotFoundException - Catching: "
 				+ ex.getClass().getSimpleName());
 	}
 
 }
