 package com.github.groupENIGMA.journalEgocentrique;
 
 /**
  * This file is used as container for all constants of the Application
  */
 public class AppConstants {
 
     /**
      * SharedPreferences file name
      */
     public static final String SHARED_PREFERENCES_FILENAME =
             "journalEgocentrique_SharedPreferences";
     /**
      * SharedPreferences key name for the Note timeout.
      * <p>
      * Each Note can be modified or deleted for a limited time set by the
      * final user in the settings activity.
      * This is the name of the key used to save this time in the
      * SharedPreferences
      */
     public static final String PREFERENCES_KEY_NOTE_TIMEOUT = "note_timeout";
 
     /**
      * The default value for the Note Timeout
      * <p>
      * Each Note can be modified or deleted for a limited time set by the
      * final user in the settings activity.
      * This is the default value for this timeout (number of hours).
      */
     public static final int DEFAULT_NOTE_TIMEOUT = 3;
 
     /**
      * The file name prefix for the Mood images saved in res/drawable
      * <p>
      * This is used by model.Mood.getEmoteId() method to load the correct emote
      * id.
      * Each mood image must be saved using this prefix. For example the emote
      * for the Mood with id=1 an image must be named:
      *      MOOD_EMOTE_PREFIX_1.ext
      */
     public static final String MOOD_EMOTE_PREFIX = "mood_emote_";
 
}
