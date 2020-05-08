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
 import drinkcounter.model.Drink;
 import drinkcounter.model.User;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import org.joda.time.DateTime;
 import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.*;
 
 /**
  *
  * @author Toni
  */
 @Controller
 @RequestMapping("/v2")
 public class ProfileApiController {
     
     @Autowired
     private DrinkCounterService drinkCounterService;
     
     @Autowired
     private UserService userService;
     
     @Autowired
     private CurrentUser currentUser;
     
     private Gson gson = new Gson();
     
     @RequestMapping(value="/profile", method=RequestMethod.GET)
     public @ResponseBody String getUser() {
         UserDTO userDTO = UserDTO.fromUser(currentUser.getUser());
         return gson.toJson(userDTO);
     }
     
     @RequestMapping(value="/profile", method=RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
     public void updateUser(
                         @RequestParam("name") String name,
                         @RequestParam("email") String email,
                         @RequestParam("sex") User.Sex sex,
                         @RequestParam("weight") Float weight){
         User user = currentUser.getUser();
         user.setName(name);
         user.setEmail(email);
         user.setSex(sex);
         user.setWeight(weight);
         userService.updateUser(user);
     }
     
     @RequestMapping(value="/profile/drinks", method= RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
     public void drink(
             @RequestParam(value="volume", required=false) Float volume,
             @RequestParam(value="alcohol", required=false) Float alcoholPercentage,
             @RequestParam(value="timestamp", required=false) String timestamp){
         Integer userId = currentUser.getUser().getId();
         double alcoholAmount = AlcoholCalculator.STANDARD_DRINK_ALCOHOL_GRAMS;
         if (volume != null && alcoholPercentage != null) {
             alcoholAmount = AlcoholCalculator.getAlcoholAmount(volume, alcoholPercentage);
         }
         Date time = null;
         if(timestamp != null){
             time = new Date(new DateTime(timestamp).getMillis());
         }
         drinkCounterService.addDrink(userId, time, (float)alcoholAmount);
         
     }
     
     @RequestMapping(value="/profile/drinks", method=RequestMethod.GET)
     public @ResponseBody String getDrinks(){
         List<Drink> drinks = currentUser.getUser().getDrinks();
         List<DrinkDTO> drinkDTOs = new ArrayList<DrinkDTO>();
         for (Drink drink : drinks) {
             DrinkDTO drinkDTO = new DrinkDTO();
             drinkDTO.setId(drink.getId());
             drinkDTO.setTimestamp(new DateTime(drink.getTimeStamp()).toString());
             drinkDTOs.add(drinkDTO);
         }
         return gson.toJson(drinkDTOs);
     }
     
     @RequestMapping(value="/profile/drinks", method=RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.OK)
     public void deleteDrink(@RequestParam(value="drinkId") Integer drinkId){
         drinkCounterService.removeDrinkFromUser(currentUser.getUser().getId(), drinkId);
     }
 }
