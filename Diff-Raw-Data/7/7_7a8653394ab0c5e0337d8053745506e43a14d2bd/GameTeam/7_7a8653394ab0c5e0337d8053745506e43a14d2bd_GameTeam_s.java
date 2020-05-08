 package com.porvak.bracket.domain;
 
 
 import org.codehaus.jackson.map.annotate.JsonSerialize;
 
 public class GameTeam extends AbstractBracket {
     
    private int position;
     private String id;
     private String name;
 
     @JsonSerialize(include = JsonSerialize.Inclusion.NON_DEFAULT)
     private int seed;
 
     @JsonSerialize(include = JsonSerialize.Inclusion.NON_DEFAULT)
     private int score;
 
     private boolean winner;
 
     @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
     private GamePointer previousGame;
 
     public int getPosition() {
         return position;
     }
 
     public void setPosition(int position) {
         this.position = position;
     }
 
     public int getSeed() {
         return seed;
     }
 
     public void setSeed(int seed) {
         this.seed = seed;
     }
 
     public int getScore() {
         return score;
     }
 
     public void setScore(int score) {
         this.score = score;
     }
 
     public boolean isWinner() {
         return winner;
     }
 
     public void setWinner(boolean winner) {
         this.winner = winner;
     }
 
     public String getId() {
         return id;
     }
 
     public void setId(String id) {
         this.id = id;
     }
 
     public String getName() {
         return name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     public GamePointer getPreviousGame() {
         return previousGame;
     }
 
     public void setPreviousGame(GamePointer previousGame) {
         this.previousGame = previousGame;
     }
 }
