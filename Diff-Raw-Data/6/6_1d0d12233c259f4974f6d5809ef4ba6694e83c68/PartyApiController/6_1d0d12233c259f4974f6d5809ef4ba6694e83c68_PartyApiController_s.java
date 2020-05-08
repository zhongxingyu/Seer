 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package drinkcounter.web.controllers.api.v2;
 
 import com.google.gson.Gson;
 import drinkcounter.DrinkCounterService;
 import drinkcounter.UserService;
 import drinkcounter.alcoholcalculator.AlcoholCalculator;
 import drinkcounter.authentication.CurrentUser;
 import drinkcounter.model.Party;
 import drinkcounter.model.User;
 import java.text.MessageFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import org.joda.time.DateTime;
 import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.*;
 
 /**
  *
  * @author Toni
  */
 @Controller
 @RequestMapping("/v2")
 public class PartyApiController {
     
     @Autowired
     private CurrentUser currentUser;
     @Autowired
     private DrinkCounterService drinkCounterService;
     @Autowired
     private UserService userService;
     
     private Gson gson = new Gson();
     
     @RequestMapping(value="/parties", method= RequestMethod.GET)
     public @ResponseBody String getParties(){
         List<Party> parties = currentUser.getUser().getParties();
         List<PartyDTO> partyDTOs = new ArrayList<PartyDTO>();
         for (Party party : parties) {
             partyDTOs.add(PartyDTO.fromParty(party));
         }
         return gson.toJson(partyDTOs);
     }
     
     @RequestMapping(value="/parties/{partyId}", method=RequestMethod.GET)
     public @ResponseBody String getParty(@PathVariable Integer partyId){
         Party party = drinkCounterService.getParty(partyId);
         PartyDTO partyDTO = PartyDTO.fromParty(party);
         return gson.toJson(partyDTO);
     }
     
     @RequestMapping(value="/parties/{partyId}/participants", method=RequestMethod.GET)
     public @ResponseBody String getParticipants(@PathVariable Integer partyId){
         Party party = drinkCounterService.getParty(partyId);
         List<User> participants = party.getParticipants();
         List<ParticipantDTO> participantDTOs = new ArrayList<ParticipantDTO>();
         for (User participant : participants) {
             participantDTOs.add(ParticipantDTO.fromUser(participant));
         }
         return gson.toJson(participantDTOs);
     }
     
     @RequestMapping(value="/parties/{partyId}/participants", method=RequestMethod.POST)
     public void addParticipant(@PathVariable Integer partyId, @RequestParam(value="email", required=false) String email,
             @RequestParam(value="name", required=false) String name,
             @RequestParam(value="sex", required=false) User.Sex sex,
             @RequestParam(value="weight", required=false) Float weight){
         if(email != null){
             User user = userService.getUserByEmail(email);
             drinkCounterService.linkUserToParty(user.getId(), partyId);
             return;
         }
         
         if (name == null || sex == null || weight == null) {
             throw new RuntimeException("If email is not provided, give name, sex and weight to create a guest participant");
         }
         
         User user = new User();
         user.setName(name);
         user.setSex(sex);
         user.setWeight(weight);
         user.setGuest(true);
         userService.addUser(user);
         drinkCounterService.linkUserToParty(user.getId(), partyId);
     }
     
     @RequestMapping(value="/parties/{partyId}/participants/{participantId}", method= RequestMethod.GET)
     public @ResponseBody String getParticipant(@PathVariable Integer partyId, @PathVariable Integer participantId){
         Party party = drinkCounterService.getParty(partyId);
         User participant = userService.getUser(participantId);
         if(!party.getParticipants().contains(participant)){
             throw new RuntimeException(MessageFormat.format("Participant {} doesn't belong to party {}", participant.getId(), party.getId()));
         }
         return gson.toJson(ParticipantDTO.fromUser(participant));
     }
     
     @RequestMapping(value="/parties/{partyId}/participants/{participantId}/drinks", method=RequestMethod.POST)
     public void drink(@PathVariable Integer partyId, @PathVariable Integer participantId,
             @RequestParam(value="volume", required=false) Float volume,
             @RequestParam(value="alcohol", required=false) Float alcoholPercentage,
             @RequestParam(value="timestamp", required=false) String timestamp){
         Party party = drinkCounterService.getParty(partyId);
         User participant = userService.getUser(participantId);
         if(!party.getParticipants().contains(participant)){
             throw new RuntimeException(MessageFormat.format("Participant {} doesn't belong to party {}", participant.getId(), party.getId()));
         }
         float alcoholAmount = (float)AlcoholCalculator.STANDARD_DRINK_ALCOHOL_GRAMS;
         if (volume != null && alcoholPercentage != null) {
             alcoholAmount = AlcoholCalculator.getAlcoholAmount(volume, alcoholPercentage);
         }
         Date time = null;
         if(timestamp != null){
             time = new Date(new DateTime(timestamp).getMillis());
         }
         drinkCounterService.addDrink(participantId, time, alcoholAmount);
     }
 }
