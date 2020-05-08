 package com.twu.thoughtconf.web.controller;
 
 import com.twu.thoughtconf.domain.ConferenceSession;
 import com.twu.thoughtconf.repositories.ConferenceSessionRepository;
 import com.twu.thoughtconf.repositories.SessionAttendeeRepository;
 import org.joda.time.DateTime;
 import org.joda.time.format.DateTimeFormat;
 import org.joda.time.format.DateTimeFormatter;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.servlet.ModelAndView;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 
 // Responsible for creating and displaying conference sessions
 @Controller
 public class ConferenceSessionController {
 
     @Autowired
     private ConferenceSessionRepository repository;
 
     @Autowired
     private SessionAttendeeRepository sessionAttendeeRepository;
 
     public ConferenceSessionController() {
     }
 
     public ConferenceSessionController(ConferenceSessionRepository repository, SessionAttendeeRepository sessionAttendeeRepository) {
         this.repository = repository;
         this.sessionAttendeeRepository = sessionAttendeeRepository;
     }
 
     @RequestMapping(value = "/attendee/sessions", method = RequestMethod.GET)
     public ModelAndView displayAllSessions(){
         ModelAndView mv = new ModelAndView("attendeeHomepage");
         ModelMap modelMap = mv.getModelMap();
         modelMap.put("sessions", repository.getAllSessions());
         return mv;
     }
 
     @RequestMapping(value = "/attendee/session/{sessionId}", method = RequestMethod.GET)
     public ModelAndView displaySession(@PathVariable("sessionId") String sessionId, HttpServletRequest request) {
         ModelAndView mv = new ModelAndView("viewConferenceSession");
         ModelMap map = mv.getModelMap();
         map.put("going", sessionAttendeeRepository.hasSessionAttendee(request.getRemoteUser(), Integer.parseInt(sessionId)));
         map.put("session", repository.findById(sessionId));
         return mv;
     }
 
     @RequestMapping(value = "/attendee/session/castest", method = RequestMethod.GET)
     public ModelAndView welcome(HttpServletRequest request, HttpServletResponse response) throws Exception{
         ModelAndView modelAndView = new ModelAndView("homepage");
         modelAndView.addObject("username", request.getRemoteUser());
         return modelAndView;
     }
 
     @RequestMapping(value = "/organiser/new", method = RequestMethod.GET)
     public ModelAndView newConferenceSession() {
         ModelAndView mv = new ModelAndView("newConferenceSession");
         mv.getModelMap().put("formAction", "/thoughtconf/organiser/create");
         return mv;
     }
 
     @RequestMapping(value = "/organiser/create", method = RequestMethod.POST)
     public String create(@RequestParam("conferenceName") String conferenceName,
                          @RequestParam("name") String name,
                          @RequestParam("location") String location,
                          @RequestParam("date") String date,
                          @RequestParam("time") String time,
                          @RequestParam("abstract") String sessionAbstract,
                          @RequestParam("presenterName") String presenterName,
                          @RequestParam("aboutPresenter") String aboutPresenter) {
 
         DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
         String[] tokens = time.split("-");
         DateTime startTime = parseToJodaTime(date, dateTimeFormatter, tokens[0]);
         DateTime endTime = parseToJodaTime(date, dateTimeFormatter, tokens[1]);
         ConferenceSession conferenceSession = new ConferenceSession(conferenceName, name, location, startTime, endTime, sessionAbstract, presenterName, aboutPresenter);
         ConferenceSession conferenceSessionWithId = repository.save(conferenceSession);
         return "redirect:confirmation/" + conferenceSessionWithId.getId();
     }
 
     protected DateTime parseToJodaTime(String date, DateTimeFormatter dateTimeFormatter, String token) {
         String timeString = token +":00";
         String dateTimeString = date + " " + timeString;
         return dateTimeFormatter.parseDateTime(dateTimeString);
     }
 
     @RequestMapping(value = "/organiser/confirmation/{sessionId}", method= RequestMethod.GET)
     public ModelAndView confirm(@PathVariable("sessionId") int sessionId) {
         return new ModelAndView("sessionConfirmation", "session", repository.getSessionById(sessionId));
     }
 
     @RequestMapping(value = "/organiser")
     public ModelAndView displayAllSessionsOnOrganiser() {
         ModelAndView mv = new ModelAndView("organiserHomepage");
         ModelMap modelMap = mv.getModelMap();
         modelMap.put("sessions", repository.getAllSessions());
         return mv;
     }
 }
