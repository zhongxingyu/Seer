 package org.sample.ma.controller;
 
 import java.util.List;
 
 import org.sample.ma.model.BookingService;
 import org.sample.ma.model.Meeting;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.ResponseBody;
 
 @Controller
 @RequestMapping("api")
 public class MeetingController {
 	
 	@Autowired
 	private BookingService bookingService;
 	
 	@RequestMapping(value="meetings")
 	@ResponseBody
 	public List<Meeting> getMeetingsList(){ 
 		System.out.println("get meetings ");
 		
 		return bookingService.getMeetingsList();
 	}
 	
 	@RequestMapping(value="add")
 	@ResponseBody
 	public List<Meeting> addMeeting(@RequestParam("userId") String userId, 
 			@RequestParam("meetingStart") String meetingStart,
 			@RequestParam("duration") String duration){ 
		//it should be a better way to pass form data into controller
 		//I did not find, why this declaration is not working (@RequestBody Meeting meeting)
		System.out.println("adding meeting " + userId);
 		
 		boolean res = bookingService.bookMeeting(userId, meetingStart, duration);
 		if(res){
 			return bookingService.getMeetingsList();
 		}
 		System.out.println("meeting was not added");
 		return null;
 	}
 
 }
 
