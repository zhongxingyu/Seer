 package jam.ld26.levels;
 
 import jam.ld26.game.LevelEditorState;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Scanner;
 import java.util.logging.Logger;
 import org.json.simple.JSONArray;
 import org.json.simple.JSONObject;
 import org.json.simple.parser.JSONParser;
 import org.json.simple.parser.ParseException;
 
 public class LevelManager {
     String fileName;
     String filePath;
     ArrayList<String> levelNames = null;
     private int currentLevelId = -1;
     
     public LevelManager(String filePath) throws ParseException, FileNotFoundException {
         JSONParser parser = new JSONParser();
         JSONObject obj = (JSONObject) parser.parse(new Scanner(new File(filePath + "/list.json")).useDelimiter("\\Z").next());
         JSONArray levelsObj = (JSONArray) obj.get("levels");
         
         levelNames = new ArrayList<String>();
         for (int i = 0; i < levelsObj.size(); i += 1) {
             levelNames.add(levelsObj.get(i).toString());
         }
         
         this.filePath = filePath;
     }
     
     public Level nextLevel() {
         if(levelNames.isEmpty()) {
             return null;
         }
         currentLevelId = ((currentLevelId + 1) % levelNames.size());
         return loadLevel();
     }
     
     public Level prevLevel() {
         if(levelNames.isEmpty()) {
             return null;
         }
        currentLevelId = Math.abs((currentLevelId - 1) % levelNames.size());
         return loadLevel();
     }
     
     public void addLevel(String level) {
         levelNames.add(level);
         JSONObject obj = new JSONObject();
         JSONArray levelsObj = new JSONArray();
                      
         for (int i = 0; i < levelNames.size(); i += 1) {
             levelsObj.add(levelNames.get(i));
         }
         obj.put("levels", levelsObj);
 
         FileWriter fileWriter = null;
         File newTextFile = new File(this.filePath + "/list.json");
         try {
             fileWriter = new FileWriter(newTextFile);
             fileWriter.write(obj.toString());
             fileWriter.close();
         } catch (IOException ex) {
             Logger.getLogger(LevelManager.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         }
     }
     
     public Level loadLevel() {
         Level lvl = new Level(this.filePath, levelNames.get(currentLevelId));
         
         try {
             lvl.load();
         } catch (FileNotFoundException ex) {
             Logger.getLogger(LevelEditorState.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         } catch (ParseException ex) {
             Logger.getLogger(LevelEditorState.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         }
         
         return lvl;
     }
 }
