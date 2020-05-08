 package ch.zhaw.arsphema.model;
 
 import com.badlogic.gdx.utils.Json;
 import com.badlogic.gdx.utils.Json.Serializable;
 import com.badlogic.gdx.utils.OrderedMap;
 
 import java.util.ArrayList;
 import java.util.Collections;
 
 
 /**
  * Class for the players Profile with Highscores and Stettings
  */
 public class PlayerProfile implements Serializable {
 
     private ArrayList<HighscoreEntry> highscore;
     private String playerName;
    private float soundVolume, musicVolume;
 
     public PlayerProfile() {
         highscore = new ArrayList<HighscoreEntry>();
         playerName = "";
         musicVolume = 1f;
         soundVolume = 1f;
     }
 
     public ArrayList<HighscoreEntry> getHighscore() {
         return highscore;
     }
 
     /**
      * Adds a new Entry to the right position in the highscore if the score is high enough.
      *
      * @param newEntry the new highscore entry
      */
     public void addHighscoreEntry(HighscoreEntry newEntry) {
         boolean entryAllowed = highscore.size() < 10;
         for (HighscoreEntry highscoreEntry : highscore) {
             if (entryAllowed || highscoreEntry.getScore() < newEntry.getScore()) {
                 entryAllowed = true;
                 break;
             }
         }
 
         if (entryAllowed) {
             highscore.add(newEntry);
             Collections.sort(highscore);
             if (highscore.size() > 10) {
                 highscore.remove(10);
             }
         }
     }
 
     public int getMinimalScore() {
         return highscore.size() < 10 ? 0 : highscore.get(highscore.size() - 1).getScore() + 1;
     }
 
     public String getPlayerName() {
         return playerName;
     }
 
     public void setPlayerName(String playerName) {
         this.playerName = playerName;
     }
 
     public float getSoundVolume() {
         return soundVolume;
     }
 
     public void setSoundVolume(float soundVolume) {
         this.soundVolume = soundVolume;
     }
 
     public float getMusicVolume() {
         return musicVolume;
     }
 
     public void setMusicVolume(float musicVolume) {
         this.musicVolume = musicVolume;
     }
 
     @Override
     public void write(Json json) {
         json.writeValue("highscore", highscore);
         json.writeValue("playerName", playerName);
         json.writeValue("musicVolume", musicVolume);
        json.writeValue("SoundVolume", soundVolume);
     }
 
     @Override
     @SuppressWarnings("unchecked")
     public void read(Json json, OrderedMap<String, Object> jsonData) {
         highscore = json.readValue("highscore", ArrayList.class, jsonData);
         playerName = json.readValue("playerName", String.class, jsonData);
         musicVolume = json.readValue("musicVolume", Float.class, jsonData);
         soundVolume = json.readValue("soundVolume", Float.class, jsonData);
     }
 }
