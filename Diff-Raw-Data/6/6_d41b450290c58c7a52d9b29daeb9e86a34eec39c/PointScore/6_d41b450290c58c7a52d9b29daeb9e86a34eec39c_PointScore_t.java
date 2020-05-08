 package no.finntech.kundefront.kata;
 
 import com.google.common.base.Objects;
 
 public class PointScore implements Score {
     private int server;
     private int receiver;
 
     public PointScore(int server, int receiver) {
 
         this.server = server;
         this.receiver = receiver;
     }
 
     public Score registerBallWinner(Player player) {
        if (player == Player.SERVER && server == 40 && receiver < 40) {
            return new Winner(Player.SERVER);
        }
        if (player == Player.RECEIVER && server < 40 && receiver == 40) {
            return new Winner(Player.RECEIVER);
        }
         Score score = calculateScore(player);
         if (score.equals(new PointScore(40, 40))) {
             return new DeuceScore();
         }
         return score;
     }
 
     private Score calculateScore(Player player) {
         if (player == Player.RECEIVER) {
             return new PointScore(this.getFor(Player.SERVER), incrementScore(this.getFor(Player.RECEIVER)));
         } else {
             return new PointScore(incrementScore(this.getFor(Player.SERVER)), this.getFor(Player.RECEIVER));
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
 
 
     @SuppressWarnings("all")
     @Override
     public boolean equals(Object o) {
         if (this == o) return true;
         if (o == null || getClass() != o.getClass()) return false;
 
         PointScore pointScore = (PointScore) o;
 
         if (receiver != pointScore.receiver) return false;
         if (server != pointScore.server) return false;
 
         return true;
     }
 
     @Override
     public int hashCode() {
         int result = server;
         result = 31 * result + receiver;
         return result;
     }
 
     @Override
     public String toString() {
         return Objects.toStringHelper(this)
                 .add("server", server)
                 .add("receiver", receiver)
                 .toString();
     }
 
     public int getFor(Player player) {
         if (player == Player.SERVER) {
             return server;
         } else {
             return receiver;
         }
     }
 }
