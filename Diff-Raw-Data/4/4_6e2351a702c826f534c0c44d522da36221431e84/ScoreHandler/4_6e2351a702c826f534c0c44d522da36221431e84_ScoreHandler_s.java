 package no.hist.gruppe5.pvu;
 
 /**
  *
  * @author linnk
  */
 public class ScoreHandler {
 
     public static final int VISION = 0;
     public static final int REQ = 1;
    public static final int UMLBLOCKS = 2;
    public static final int SEQ = 3;
     public static final int CODE = 4;
     private static float[] totalScore;
     private static boolean completedAllLevels;
     private static float total;
 
     /**
      *
      */
     public static void load() {
         totalScore = new float[5];
         completedAllLevels = false;
         total = 0;
     }
 
     /**
      * Checks if a minigame is completed.
      *
      * @param minigame
      * @return
      */
     public static boolean isMinigameCompleted(int minigame) {
         try {
             if (totalScore[minigame] > 0f) {
                 return true;
             } else {
                 return false;
             }
         } catch (IndexOutOfBoundsException e) {
             return false;
         }
     }
 
     /**
      * Returns the score of a certain minigame
      *
      * @param minigame Constant, e.g ScoreHandler.VISION
      * @return The percentage of the minigame, 0-1f
      */
     public static float getMiniGameGrade(int minigame) {
         try {
             return totalScore[minigame];
         } catch (IndexOutOfBoundsException e) {
             return -1;
         }
     }
 
     public static int numberOfGamesCompleted() {
         for (int i = 0; i < totalScore.length; i++) {
             if (totalScore[i] == 0f) {
                 return i;
             }
         }
 
         return totalScore.length;
     }
 
     /**
      * Checks if all minigames are completed.
      *
      * @return true/false
      */
     private static boolean checkScore() {
         for (int i = 0; i < totalScore.length; i++) {
             if (totalScore[i] == 0) {
                 return false;
             }
         }
         return true;
     }
 
     /**
      * Updates totalscore based on the result of current minigame.
      *
      * @param miniGame
      * @param percent
      * @return if the total score was modified
      */
     public static boolean updateScore(int miniGame, float percent) {
         if (miniGame < totalScore.length) {
             totalScore[miniGame] = percent;
             total += percent;
             if (checkScore()) {
                 completedAllLevels = true;
             }
             return true;
         }
         return false;
     }
 
     /**
      *
      * @return a grade based on the total score.
      */
     public static Character getGrade() {
       float average = total /(float)numberOfGamesCompleted();
         if (average > 0.9) {
             return 'A';
         } else if (average > 0.8) {
             return 'B';
         } else if (average > 0.7) {
             return 'C';
         } else if (average > 0.6) {
             return 'D';
         } else {
             return 'E';
         }
     }
 }
