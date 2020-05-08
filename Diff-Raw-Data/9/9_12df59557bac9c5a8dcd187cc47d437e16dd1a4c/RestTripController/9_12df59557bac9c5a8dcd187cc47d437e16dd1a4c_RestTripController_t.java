 package be.kdg.groepi.controller;
 
 import be.kdg.groepi.model.Requirement;
 import be.kdg.groepi.model.Stop;
 import be.kdg.groepi.model.Trip;
 import be.kdg.groepi.model.User;
 import be.kdg.groepi.service.RequirementService;
 import be.kdg.groepi.service.StopService;
 import be.kdg.groepi.service.TripInstanceService;
 import be.kdg.groepi.service.TripService;
 import org.apache.log4j.Logger;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.*;
 import org.springframework.web.servlet.ModelAndView;
 
 import javax.servlet.http.HttpSession;
 import java.util.List;
 
 @Controller
 @RequestMapping("trips")
 public class RestTripController {
 
     private static final Logger logger = Logger.getLogger(RestTripController.class);
 
     @RequestMapping(value = "/addtrip")
     public String addtrip() {
         System.out.println("AddTrip: Passing through...");
         return "trips/addtrip";
     }
 
     @RequestMapping(value = "/createTrip", method = RequestMethod.POST)
     public ModelAndView createTrip(HttpSession session, @ModelAttribute("tripObject") Trip trip) {
 
         User user = (User) session.getAttribute("userObject");
 
         trip.setOrganiser(user);
         TripService.createTrip(trip);
         return new ModelAndView("trips/view", "tripId", trip.getId().toString());
     }
 
     @RequestMapping(value = "/doAddTripRequirement", method = RequestMethod.POST)
     public ModelAndView doAddTripRequirement(@RequestParam(value = "tripId") String tripId,/*
              @ModelAttribute("requirementObject") Requirement requirement*/
            @RequestParam(value = "name") String name,
            @RequestParam(value = "amount") Long amount,
            @RequestParam(value = "description") String description) {
 
         Trip trip = TripService.getTripById(Long.parseLong(tripId));
 
         Requirement requirement = new Requirement(name, amount, description, trip);
 
         RequirementService.createRequirement(requirement);
         //       trip.addRequirementToTrip(requirement);
         TripService.updateTrip(trip);
         return new ModelAndView("trips/addtriprequirement", "tripId", trip.getId().toString());
     }
 
     @RequestMapping(value = "/view/{tripId}", method = RequestMethod.GET)
     public ModelAndView getTrip(@PathVariable("tripId") String tripId, HttpSession session) {
         Trip trip;
         trip = TripService.getTripById(Long.parseLong(tripId));
 
         if (trip != null) {
             logger.debug("Returning Trip: " + trip.toString() + " with trip #" + tripId);
             ModelAndView modelAndView = new ModelAndView("trips/view");
             modelAndView.addObject("tripObject", trip);
             modelAndView.addObject("stopListObject", StopService.getAllTripStopsByTripId(trip.getId()));
             modelAndView.addObject("tripInstanceListObject", TripInstanceService.getAllTripInstancesByTripId(trip.getId()));
             return modelAndView;
         } else {
             return new ModelAndView("error/displayerror");
         }
     }
 
    @RequestMapping(value = "/addstop/{tripId}", method = RequestMethod.POST)
     public ModelAndView addStop(@PathVariable("tripId") String tripId) {
         Trip trip = TripService.getTripById(Long.parseLong(tripId));
         return new ModelAndView("trips/addstop", "tripObject", trip);
     }
 
     @RequestMapping(value = "/createStop", method = RequestMethod.POST)
     public ModelAndView createStop(HttpSession session, @ModelAttribute("stopObject") Stop stop, @RequestParam(value = "tripId") String tripId) {
         Trip trip = TripService.getTripById(Long.parseLong(tripId));
         stop.setTrip(trip);
         StopService.createStop(stop);
         return new ModelAndView("trips/addstop", "tripObject", trip);
     }
 
     @RequestMapping(value = "/list")
     public ModelAndView getAllTrips() {
         List<Trip> tripList = TripService.getAllTrips();
         if (tripList != null) {
             logger.debug("Returning TripList containing " + tripList.size() + " TripInstances");
         } else {
             logger.debug("Returning TripList = NULL");
         }
         return new ModelAndView("trips/list", "tripListObject", tripList);
     }
 
     @RequestMapping(value = "/editTrip/{tripId}", method = RequestMethod.GET)
     public ModelAndView editTripView(@PathVariable("tripId") String tripId) {
         ModelAndView modelAndView = new ModelAndView("trips/edittrip");
         modelAndView.addObject("tripObject", TripService.getTripById(Long.parseLong(tripId)));
         return modelAndView;
     }
 
     @RequestMapping(value = "/updateTrip", method = RequestMethod.POST)
     public ModelAndView editUser(@ModelAttribute("tripObject") Trip trip) {
         TripService.updateTrip(trip);
         return new ModelAndView("trips/view", "tripObject", trip);
     }
 }
