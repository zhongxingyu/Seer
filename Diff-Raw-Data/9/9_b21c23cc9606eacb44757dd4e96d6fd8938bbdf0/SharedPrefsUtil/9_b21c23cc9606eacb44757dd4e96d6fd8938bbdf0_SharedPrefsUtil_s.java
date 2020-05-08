 package knaps.hacker.school.utils;
 
 import android.content.Context;
 import android.content.SharedPreferences;
 
 /**
  * Created by lisaneigut on 22 Sep 2013.
  */
 public class SharedPrefsUtil {
     static final String PREFS_USER = "user_prefs";
     static final String USER_EMAIL = "user_email";
     private static final String USER_HIGH_SCORE = "user_highscore";
     private static final String USER_TOTAL_CORRECT = "user_correct";
     private static final String USER_TOTAL_TRIES = "user_tries";
 
     public static String getUserEmail(Context context) {
         SharedPreferences prefs = context.getSharedPreferences(PREFS_USER, Context.MODE_PRIVATE);
         return prefs.getString(USER_EMAIL, "");
     }
 
     public static void saveUserEmail(Context context, String email) {
         SharedPreferences prefs = context.getSharedPreferences(PREFS_USER, Context.MODE_PRIVATE);
         prefs.edit().putString(USER_EMAIL, email.toLowerCase().trim()).apply();
     }
 
     public static int getHighScore(Context context) {
         SharedPreferences prefs = context.getSharedPreferences(PREFS_USER, Context.MODE_PRIVATE);
         return prefs.getInt(USER_HIGH_SCORE, -1);
     }
 
     public static void saveUserHighScore(Context context, int highScore) {
         SharedPreferences prefs = context.getSharedPreferences(PREFS_USER, Context.MODE_PRIVATE);
         int oldHighScore = prefs.getInt(USER_HIGH_SCORE, -1);
 
         if (highScore > oldHighScore) {
             prefs.edit().putInt(USER_HIGH_SCORE, highScore).apply();
         }
     }
 
     public static void saveUserStats(Context context, int mCurrentScore, int mCurrentGuesses) {
         SharedPreferences prefs = context.getSharedPreferences(PREFS_USER, Context.MODE_PRIVATE);
         int oldTries = prefs.getInt(USER_TOTAL_TRIES, 0);
         int oldHits = prefs.getInt(USER_TOTAL_CORRECT, 0);
 
         SharedPreferences.Editor editor = prefs.edit();
         editor.putInt(USER_TOTAL_TRIES, oldTries + mCurrentGuesses);
         editor.putInt(USER_TOTAL_CORRECT, oldHits + mCurrentScore);
 
         editor.apply();
     }
 
     public static String getAllTimeScore(Context context) {
         SharedPreferences prefs = context.getSharedPreferences(PREFS_USER, Context.MODE_PRIVATE);
 
         int tries = prefs.getInt(USER_TOTAL_TRIES, 1);
         int correct = prefs.getInt(USER_TOTAL_CORRECT, 1);
 
        return String.format("%.2f%%", (double) tries * 100.0d / (correct* (1.0d)));
     }
 
     public static int getTries(Context context) {
         return context.getSharedPreferences(PREFS_USER, Context.MODE_PRIVATE).getInt(USER_TOTAL_TRIES, 1);
     }
 }
