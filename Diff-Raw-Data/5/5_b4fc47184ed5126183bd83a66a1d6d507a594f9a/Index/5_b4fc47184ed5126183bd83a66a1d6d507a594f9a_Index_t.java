 package com.neogeohiscores.web.pages;
 
 import com.neogeohiscores.entities.Game;
 import com.neogeohiscores.web.services.GameService;
 import org.apache.tapestry5.annotations.Property;
 import org.apache.tapestry5.annotations.SetupRender;
 import org.apache.tapestry5.ioc.annotations.Inject;
 
 public class Index {
 
     @Inject
     private GameService gameService;
 
    public Game getGameOfTheDay() {
         return gameService.getGameOfTheDay();
     }
 
 }
