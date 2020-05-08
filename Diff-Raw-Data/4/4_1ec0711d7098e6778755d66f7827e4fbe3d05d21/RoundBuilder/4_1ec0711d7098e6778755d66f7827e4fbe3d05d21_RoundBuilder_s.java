 package com.porvak.bracket.utils.builder;
 
 import com.google.common.collect.Lists;
 import com.porvak.bracket.domain.Game;
 import com.porvak.bracket.domain.Round;
 
 import java.util.List;
 
 public class RoundBuilder {
     private int roundId;
     private String roundName;
     private List<Game> games;
 
     public RoundBuilder(){
         init();
     }
 
     private void init(){
         roundId = 1;
         roundName = "Round Name";
         games = Lists.newLinkedList();
     }
     
     public RoundBuilder withRoundId(int id){
         roundId = id;
         return this;
     }
     
     public RoundBuilder withRoundName(String roundName){
         this.roundName = roundName;
         return this;
     }
     
     public RoundBuilder withGames(List<Game> games){
         this.games = games;
         return this;
     }
 
     public RoundBuilder addGameBuilder(GameBuilder gameBuilder){
         games.add(gameBuilder.build());
         return this;
     }
 
     public RoundBuilder addGame(Game game) {
         games.add(game);
         return this;
     }
 
     public Round build(){
         Round round = new Round();
         round.setRoundId(roundId);
         round.setRoundName(roundName);
 
         if(games != null && !games.isEmpty()){
//            round.setGames(games);
         }
 
         return round;
     }
 }
