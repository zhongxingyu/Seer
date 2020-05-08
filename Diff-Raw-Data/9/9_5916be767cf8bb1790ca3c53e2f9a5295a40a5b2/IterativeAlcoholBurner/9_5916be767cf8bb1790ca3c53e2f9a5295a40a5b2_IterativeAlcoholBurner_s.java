 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package drinkcounter;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.scheduling.annotation.Scheduled;
 import org.springframework.stereotype.Component;
 
 /**
  *
  * @author toni
  */
 @Component
 public class IterativeAlcoholBurner {
 
     @Autowired
     private DrinkCounterService drinkCounterService;
 
     @Scheduled(fixedRate=5000)
     public void burnAlcohol(){
         drinkCounterService.timePassed(1.0f/60.0f/60.0f*5.0f);
     }
 }
