 package com.porvak.bracket.domain;
 
 import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.map.annotate.JsonSerialize;
 
 import static com.google.common.base.Preconditions.*;
 
 public class Game extends AbstractBracket{
 
     private int gameId;
     private GameStatus status;

     private GameTeam[] teams;

    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
     private GamePointer nextGame;
 
     public Game() {
         teams = new GameTeam[2];
     }
 
     public int getGameId() {
         return gameId;
     }
 
     public void setGameId(int gameId) {
         this.gameId = gameId;
     }
 
     public GameStatus getStatus() {
         return status;
     }
 
     public void setStatus(GameStatus status) {
         this.status = status;
     }
 
     public GameTeam[] getTeams() {
         return teams;
     }
     
     public void setNextGame(GamePointer nextGame){
         this.nextGame = nextGame;
     }
 
     public GamePointer getNextGame() {
         return nextGame;
     }
 
     public GameTeam getTeamByPosition(int position){
         validatePosition(position);
         return teams[position];
     }
 
     public void addTeam(GameTeam team){
         team = checkNotNull(team, "Team cannot be null");
         validatePosition(team.getPosition());
         teams[team.getPosition()] =  team;
     }
 
     /**
      * Returns the winning team
      *
      * @return null if no team has won.
      */
     @JsonIgnore
     public GameTeam getWinningTeam(){
         if(teams[0] != null && teams[0].isWinner()){
             return teams[0];
         }
         else if(teams[1] != null && teams[1].isWinner()){
             return teams[1];
         }
         
         return null;
     }
 
     public static void validatePosition(int position){
         checkArgument(position == 0 || position == 1, "Team position must be 0 or 1. Received %s", position);
     }
 
 }
