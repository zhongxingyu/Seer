 import java.util.*;
 import java.io.*;
 import java.awt.event.KeyEvent;
 import java.awt.Robot;
 import java.lang.reflect.Field;
 public class Mappings {
   private final static String FILE_NAME = "chrome.settings";
   private final static String FILE_NAME2 = "desktop.settings";
   private HashMap<String, String[]> _map;
   private HashMap<String, String[]> _mapTwo;
   private final Robot _robot;
   Mappings(Robot robot) {
     _map = new HashMap<String, String[]>();
     _mapTwo = new HashMap<String, String[]>();
     _robot = robot;
     parse(FILE_NAME, _map);
    parse(FILE_NAME2, _mapTwo);
     /*
     _holdKeys = new HashSet<HoldKeys>();
     _holdKeys.add(HoldKeys.CTRL);
     _holdKeys.add(HoldKeys.SHIFT);
     _holdKeys.add(HoldKeys.ALT);
     */
   }
  
 
   public void switchMapping() {
     Map<String,String[]> tmp = _map;
     _map = _mapTwo;
    _mapTwo = tmp;
   }
 
   public Map<String,String[]> getMap() {
     return _map;
   }
 
   private void parse(String filename, HashMap<String, String[]> map) {
     try {
       FileInputStream fstream = new FileInputStream(FILE_NAME);
       DataInputStream in = new DataInputStream(fstream);
       BufferedReader br = new BufferedReader(new InputStreamReader(in));
       String line;
       String name;
       String[] value;
       String keys;
       while ((line = br.readLine()) != null) {
         name = line.split(",")[0];
         assert(Actions.actions.contains(name));
         value = line.split(",")[1].split("-");
         map.put(name,value);
       }
     } catch (Exception e) {
       System.err.println(e);
     }
   }
 
 
   private int getAction(String action)
   {
     try {
       Field f = KeyEvent.class.getField("VK_" + action);
       return f.getInt(null);
     } catch (Exception e) {
       System.err.println(e);
       return 0;
     }
   }
 
   private void doActionHelper(String[] actions)
   {
     if (actions.length == 1) {
       _robot.keyPress(getAction(actions[0]));
       _robot.delay(15);
       _robot.keyRelease(getAction(actions[0]));
     } else if (actions.length == 2) {
       _robot.keyPress(getAction(actions[0]));
       _robot.keyPress(getAction(actions[1]));
       _robot.delay(15);
       _robot.keyRelease(getAction(actions[1]));
       _robot.keyRelease(getAction(actions[0]));
     } else if (actions.length == 3) {
       _robot.keyPress(getAction(actions[0]));
       _robot.keyPress(getAction(actions[1]));
       _robot.keyPress(getAction(actions[2]));
       _robot.delay(15);
       _robot.keyRelease(getAction(actions[2]));
       _robot.keyRelease(getAction(actions[1]));
       _robot.keyRelease(getAction(actions[0]));
     }
   }
 
   public void doAction(String action) {
     doActionHelper(_map.get(action));
   }
 /*
   public enum HoldKeys {
     CTRL, SHIFT, ALT
   }
 */
 
 
 
 
 }
 
