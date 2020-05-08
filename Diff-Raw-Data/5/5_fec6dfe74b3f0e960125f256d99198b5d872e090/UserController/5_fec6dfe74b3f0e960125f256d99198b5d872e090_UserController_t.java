 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package wad.spring.controller;
 
 import java.security.Principal;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Scanner;
 import javax.validation.Valid;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.validation.BindingResult;
 import org.springframework.validation.FieldError;
 import org.springframework.web.bind.annotation.*;
 import wad.spring.domain.HistoryOccurrence;
 import wad.spring.domain.MeasurementForm;
 import wad.spring.domain.Place;
 import wad.spring.domain.User;
 import wad.spring.service.PlaceService;
 import wad.spring.service.UserService;
 
 /**
  *
  * @author tonykovanen
  */
 @Controller
 @RequestMapping("user")
 public class UserController {
 
     @Autowired
     private PlaceService placeService;
     @Autowired
     private UserService userService;
 
     @RequestMapping("/*")
     public String userHome(Principal principal, Model model) {
         model.addAttribute("user", userService.findByUsername(principal.getName()));
         return "user/home";
     }
 
     @RequestMapping("/friends")
     public String showFriends(Principal principal, Model model) {
         List<User> friends = userService.findByUsername(principal.getName()).getFriends();
         model.addAttribute("friends", friends);
         return "user/friends";
     }
 
     @RequestMapping(value = "/history", method = RequestMethod.GET)
     public String showHistory(Model model, Principal principal) {
         model.addAttribute("measurementform", new MeasurementForm());
         model.addAttribute("history", userService.findByUsername(principal.getName()).getHistory());
         return "user/history";
     }
 
     @RequestMapping(value = "/history", method = RequestMethod.POST)
     public String addMeasurementToHistory(@Valid @ModelAttribute("measurementform") MeasurementForm measurementform, BindingResult result, Principal principal) {
         if (result.hasErrors()) {
             return "user/history";
         }
         
         if (measurementform.getMeasurements().trim().equals("")) {
             result.addError(new FieldError("measurementform", "measurements", "The measurements should not be empty"));
             return "user/history";
         }
         
         String lines = measurementform.getMeasurements();
         Scanner scanner = new Scanner(lines);
         while (scanner.hasNextLine()) {
             String line = scanner.nextLine();
             String[] parts = line.split(" ");
 
             //First part of the line should be a mac address
             if (!parts[0].matches("[a-zA-Z0-9:-]+")) {
                 result.addError(new FieldError("measurementform", "measurements", "The first part of the measurement should be a valid mac address following separation standards of : and -"));
                 return "user/history";
             }
             //Second part of the line should be an integer (double precision not necessary here)
             if (!parts[1].matches("[-]?[0-9]+")) {
                 result.addError(new FieldError("measurementform", "measurements", "Second part of each line should be an integer with a negative sign or without a sign."));
                 return "user/history";
             }
             
             
         }
 
         userService.localize(principal.getName(), measurementform);
         return "redirect:/user/history";
     }
 
     @RequestMapping(value = "/friendRequests", method = RequestMethod.GET)
     public String showUnaddedUsers(Principal principal, Model model) {
         model.addAttribute("unadded", userService.getUnaddedAndNotSelf(principal.getName()));
         model.addAttribute("friendshipRequests", userService.getFriendshipRequests(principal.getName()));
         return "user/friendRequestPage";
     }
 
     @RequestMapping(value = "/friendRequests/{userId}", method = RequestMethod.GET)
     public String processFriendRequest(@PathVariable Long userId, Principal principal, Model model) {
         if (userService.findOne(userId) == null) {
             model.addAttribute("message", "Requested user does not exist");
             return "troubleshooting";
         }
         userService.sendOrAcceptFriendRequestByNameToById(principal.getName(), userId);
         return "redirect:/user/friends";
     }
 
     @RequestMapping(value = "/places/{placeId}")
     public String showPlaceInformation(@PathVariable Long placeId, Model model) {
         Place place = placeService.findOne(placeId);
         if (place == null) {
             model.addAttribute("message", "A place of requested id does not exist");
             return "troubleshooting";
         }
         model.addAttribute("place", place);
         return "user/place";
     }
     
     @RequestMapping(value= "/friends/{userId}", method = RequestMethod.GET)
     public String showFriendInformation(@PathVariable Long userId, Principal principal, Model model) {
         User friend = userService.findIfFriends(principal.getName(), userId);
         if (friend == null) {
             model.addAttribute("message", "The user of this id does not exist or is not your friend");
             return "troubleshooting";
         }
         model.addAttribute("friend", friend);
         return "user/friend";
     }
     
     @RequestMapping(value = "/history/stringRepresentation.json", method = RequestMethod.GET)
     @ResponseBody
    public String produceJSONHistoryAsString(Principal principal) {
         ArrayList<String> placenames = new ArrayList<String>();
         List<HistoryOccurrence> history = userService.findByUsername(principal.getName()).getHistory();
         for (HistoryOccurrence h : history) {
             placenames.add(h.getPlace().getName());
         }
        return placenames.toString();
     }
 }
