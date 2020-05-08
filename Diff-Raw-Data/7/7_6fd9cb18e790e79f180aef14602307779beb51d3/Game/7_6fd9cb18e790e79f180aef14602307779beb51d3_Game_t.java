 package no.finntech.kundefront.kata;
 
 public class Game {
 
     private Score score = new Score(0, 0);
 
     public Score getScore() {
         return score;
     }
 
     public void registerBallWinner(Player player) {
         if (player == Player.RECEIVER) {
             score = new Score(score.getFor(Player.SERVER), incrementScore(score.getFor(Player.RECEIVER)));
         } else {
             score = new Score(incrementScore(score.getFor(Player.SERVER)), score.getFor(Player.RECEIVER));
 
         }
 
     }
 
     private int incrementScore(int current) {
        if (current <= 15) {
            return current + 15;
        } else if (current == 30) {
             return current + 10;
         }
        return current;
     }
 }
