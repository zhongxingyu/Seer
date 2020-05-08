 package com.capgemini.sbjornse.kata.tennis;
 
 /**
  * The score of a tennis player.
  * <p/>
 * Note that there is not explicit ordering of
 * the scores. Instead, each score only knows the next value of a player's score
 * after a win or a loss.
  */
 public class Score {
     final String label;
     final Score onWin;
     final Score onLose;
     
     public Score(String label, Score onWin, Score onLose) {
         this.label = label;
         this.onWin = onWin;
         this.onLose = onLose;
     }
     
     public Score(String label, Score onWin) {
         this.label = label;
         this.onWin = onWin;
         onLose = this;
     }
     
     public Score(String label) {
         this.label = label;
         this.onWin = this;
         onLose = this;
     }
 
     public boolean isWinningScore() {
         return false;
     }
 
     /**
      * @return a Score with the value of this Score after it has won over the opponent 
      */
     public Score wonOver(Score opponent) {
         return onWin;
     }
     
     /**
      * @return a Score with the value of this Score after it has lost 
      */
     public Score lost() {
         return onLose;
     }
     
     public boolean isWinnableInNextRound(Score opponent) {
         return false;
     }
 
     @Override
     public String toString() {
         return label;
     }
 }
