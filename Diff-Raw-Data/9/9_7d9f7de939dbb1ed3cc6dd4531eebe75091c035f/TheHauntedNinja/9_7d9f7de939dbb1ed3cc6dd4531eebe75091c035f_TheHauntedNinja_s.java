 package mprz.textline;
 
 import java.io.IOException;
 import java.util.ResourceBundle;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.fusesource.jansi.Ansi.Color;
 
 /**
  *
  * @author michcioperz <michcioperz@gmail.com>
  */
 public class TheHauntedNinja {
     private static final ResourceBundle bundle = ResourceBundle.getBundle("mprz/textline/Bundle");
     mSheep sheep;
     LE7ELS le7els;
     
     public TheHauntedNinja() {
         sheep = mSheep.getInstance();
         le7els = new LE7ELS();
         sheep.loadLocation(le7els.start);
     }
     
     public static void main(String[] args) {
         try {
             TheHauntedNinja game = new TheHauntedNinja();
             game.sheep.run();
         } catch (IOException ex) {
             Logger.getLogger(TheHauntedNinja.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
     
     
     public class LE7ELS {
         
         public Location start;
         public Location hallway;
         
         public LE7ELS() {
             start = new Location() {
                 
                 GameObject[] map;
 
                 @Override
                 public String getName() {
                     return bundle.getString("LOCATION_START_NAME");
                 }
 
                 @Override
                 public GameObject[] getObjectsList() {
                     return map;
                 }
 
                 @Override
                 public void onArrival() {
                     map = new GameObject[2];
                     map[0] = new GameObject() {
                         
                         public boolean produced = false;
 
                         @Override
                         public String getName() {
                             return bundle.getString("ITEM_MACHINE_NAME");
                         }
 
                         @Override
                         public void onUse() {
                             if (produced) {
                                 sheep.slowSay(bundle.getString("ITEM_MACHINE_ONUSE2"), 50);
                                 sheep.slowSay(sheep.color(bundle.getString("ITEM_MACHINE_ONUSE3"), Color.YELLOW) + bundle.getString("ITEM_MACHINE_ONUSE4"), 50);
                             } else {
                                 sheep.slowSay(bundle.getString("ITEM_MACHINE_ONUSE1"), 50);
                                 produced = true;
                                 sheep.inventory[0] = new GameObject() {
 
                                     @Override
                                     public String getName() {
                                         return bundle.getString("ITEM_CHOCOBAR_NAME");
                                     }
 
                                     @Override
                                     public void onUse() {
                                         sheep.inventory[0] = null;
                                         sheep.effects[0] = null;
                                         sheep.slowSay(bundle.getString("ITEM_CHOCOBAR_ONUSE1"), 100);
                                         sheep.sleep(2000);
                                         sheep.slowSay(bundle.getString("ITEM_CHOCOBAR_ONUSE2"), 100);
                                         sheep.sleep(4000);
                                         sheep.slowSay(bundle.getString("ITEM_CHOCOBAR_ONUSE3"), 115);
                                         sheep.slowSay(bundle.getString("ITEM_CHOCOBAR_ONUSE4"), 130);
                                         sheep.effects[1] = bundle.getString("EFFECT_VISIONS");
                                         sheep.sleep(2000);
                                         sheep.currentLoc.onVision();
                                     }
 
                                     @Override
                                     public GameObject[] getParent() {
                                         return sheep.inventory;
                                     }
 
                                     @Override
                                     public String getCodename() {
                                         return bundle.getString("ITEM_CHOCOBAR_CODENAME");
                                     }
 
                                     @Override
                                     public boolean isVisible() {
                                         return true;
                                     }
 
                                     @Override
                                     public void onNinjaCollide() {
                                         sheep.slowSay(bundle.getString("ITEM_CHOCOBAR_ONNINJACOLLIDE"), Color.YELLOW, 100);
                                     }
                                 };
                             }
                         }
 
                         @Override
                         public GameObject[] getParent() {
                             return start.getObjectsList();
                         }
 
                         @Override
                         public String getCodename() {
                             return bundle.getString("ITEM_MACHINE_CODENAME");
                         }
 						
                         @Override
                         public boolean isVisible() {
                             return true;
                         }
 
                         @Override
                         public void onNinjaCollide() {
                             sheep.slowSay(bundle.getString("ITEM_MACHINE_ONNINJACOLLIDE"), 50);
                         }
                     };
                     map[1] = new GameObject() {
                         @Override
                         public String getName() {
                             return bundle.getString("ITEM_DOOR_NAME");
                         }
                     
                         @Override
                         public void onUse() {
                             if (sheep.effects[0] == null) {
                                 sheep.slowSay(bundle.getString("ITEM_DOOR_ONUSE1"), 50);
                             }
                         }
                     
                         @Override
                         public GameObject[] getParent() {
                             return start.getObjectsList();
                         }
                     
                         @Override
                         public String getCodename() {
                             return bundle.getString("ITEM_DOOR_CODENAME");
                         }
                     
                         @Override
                         public boolean isVisible() {
                             if (sheep.effects[0] == null) {
                                 return true;
                             } else {
                                 return false;
                             }
                         }
 
                         @Override
                         public void onNinjaCollide() {
                             sheep.slowSay(bundle.getString("ITEM_DOOR_ONNINJACOLLIDE1"), 115);
                             sheep.say("");
                             sheep.slowSay(bundle.getString("ITEM_DOOR_ONNINJACOLLIDE2"), 200);
                             sheep.currentLoc.onLeave();
                         }
                     };
                     sheep.slowSay(bundle.getString("LOCATION_START_ONARRIVAL1"), 100);
                     sheep.slowSay(bundle.getString("LOCATION_START_ONARRIVAL2"), 100);
                     sheep.slowSay(bundle.getString("LOCATION_START_ONARRIVAL3"), 150);
                     sheep.say("", 1000);
                     sheep.slowSay(bundle.getString("LOCATION_START_EPTITLE"), Color.CYAN, 250);
                     sheep.say("", 1000);
                     sheep.slowSay(bundle.getString("LOCATION_START_ONARRIVAL4"), 100);
                     sheep.effects[0] = bundle.getString("EFFECT_HUNGER");
                 }
 
                 @Override
                 public void onLookover() {
                     sheep.slowSay(bundle.getString("LOCATION_START_ONLOOKOVER1"), 50);
                     if (map[1].isVisible()) {
                         sheep.slowSay(bundle.getString("LOCATION_START_ONLOOKOVER1"), 75);
                     }
                 }
 
                 @Override
                 public void onLeave() {
                     sheep.slowSay(bundle.getString("LOCSWITCH_TBC"), Color.CYAN, 250);
                     sheep.sleep(2000);
                     try {
                         sheep.console.clearScreen();
                     } catch (IOException ex) {
                         Logger.getLogger(LE7ELS.class.getName()).log(Level.SEVERE, null, ex);
                     }
                     sheep.loadingScreen();
                     sheep.loadLocation(hallway);
                 }
 
                 @Override
                 public void onVision() {
                     sheep.visionSay(bundle.getString("LOCATION_START_ONVISION1"));
                     sheep.slowSay(bundle.getString("LOCATION_START_ONVISION2"), 140);
                     
                     sheep.slowSay(bundle.getString("LOCATION_START_ONVISION3"), Color.YELLOW, 200);
                 }
             };
             hallway = new Location() {
                 
                 GameObject[] map;
                 boolean visionDone = false;
 
                 @Override
                 public String getName() {
                     return bundle.getString("LOCATION_HALLWAY_NAME");
                 }
 
                 @Override
                 public GameObject[] getObjectsList() {
                     return map;
                 }
 
                 @Override
                 public void onLookover() {
                     sheep.say(bundle.getString("LOCATION_HALLWAY_LOOKOVER1"), 1500);
                     sheep.say(bundle.getString("LOCATION_HALLWAY_LOOKOVER2"), 1300);
                     if (visionDone) {
                         sheep.say(bundle.getString("LOCATION_HALLWAY_LOOKOVER3"), 1100);
                     } else {
                         onVision();
                     }
                 }
 
                 @Override
                 public void onVision() {
                     visionDone = true;
                     sheep.visionSay(bundle.getString("LOCATION_HALLWAY_ONVISION1"));
                     sheep.say(bundle.getString("LOCATION_HALLWAY_ONVISION2"), 1200);
                     sheep.say(bundle.getString("LOCATION_HALLWAY_ONVISION3"), Color.YELLOW, 3000);
                     sheep.say(bundle.getString("LOCATION_HALLWAY_ONVISION4"), Color.YELLOW, 1000);
                     sheep.say(bundle.getString("LOCATION_HALLWAY_ONVISION5"), Color.YELLOW, 2000);
                     sheep.say(bundle.getString("LOCATION_HALLWAY_ONVISION6"), 1000);
                 }
 
                 @Override
                 public void onArrival() {
                     map = new GameObject[3];
                     
                     map[0] = new GameObject() {
 
                         @Override
                         public String getName() {
                             return bundle.getString("ITEM_LAMP_NAME");
                         }
 
                         @Override
                         public void onUse() {
                             sheep.say(bundle.getString("ITEM_LAMP_ONUSE"), Color.YELLOW, 1500);
                         }
 
                         @Override
                         public GameObject[] getParent() {
                             return map;
                         }
 
                         @Override
                         public String getCodename() {
                             return bundle.getString("ITEM_LAMP_CODENAME");
                         }
 
                         @Override
                         public boolean isVisible() {
                             return true;
                         }
 
                         @Override
                         public void onNinjaCollide() {
                             sheep.say(bundle.getString("ITEM_LAMP_ONNINJACOLLIDE1"), 2000);
                             sheep.say(bundle.getString("ITEM_LAMP_ONNINJACOLLIDE2"), 1000);
                             sheep.inventory[1] = new GameObject() {
 
                                 @Override
                                 public String getName() {
                                     return bundle.getString("ITEM_KEYS_NAME");
                                 }
 
                                 @Override
                                 public void onUse() {
                                     
                                 }
 
                                 @Override
                                 public GameObject[] getParent() {
                                     return sheep.inventory;
                                 }
 
                                 @Override
                                 public String getCodename() {
                                     return bundle.getString("ITEM_KEYS_CODENAME");
                                 }
 
                                 @Override
                                 public boolean isVisible() {
                                     return true;
                                 }
 
                                 @Override
                                 public void onNinjaCollide() {
                                     sheep.say(bundle.getString("ITEM_KEYS_ONNINJACOLLIDE"), 250);
                                 }
                             };
                         }
                     };
                     
                     map[1] = new GameObject() {
                         
                         boolean closed = true;
 
                         @Override
                         public String getName() {
                             return bundle.getString("ITEM_DOOR1_NAME");
                         }
 
                         @Override
                         public void onUse() {
                             throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                         }
 
                         @Override
                         public GameObject[] getParent() {
                             return map;
                         }
 
                         @Override
                         public String getCodename() {
                             return bundle.getString("ITEM_DOOR1_CODENAME");
                         }
 
                         @Override
                         public boolean isVisible() {
                             return true;
                         }
 
                         @Override
                         public void onNinjaCollide() {
                             throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                         }
                     };
                     
                     map[2] = new GameObject() {
                         
                         boolean closed;
 
                         @Override
                         public String getName() {
                             return bundle.getString("ITEM_DOOR2_NAME");
                         }
 
                         @Override
                         public void onUse() {
                             throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                         }
 
                         @Override
                         public GameObject[] getParent() {
                             return map;
                         }
 
                         @Override
                         public String getCodename() {
                             return bundle.getString("ITEM_DOOR2_CODENAME");
                         }
 
                         @Override
                         public boolean isVisible() {
                             return true;
                         }
 
                         @Override
                         public void onNinjaCollide() {
                             throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                         }
                     };
                 }
 
                 @Override
                 public void onLeave() {
                     try {
                         sheep.slowSay("TO BE CONTINUED...", Color.CYAN, 250);
                         sheep.sleep(2000);
                         sheep.stop();
                     } catch (IOException ex) {
                         Logger.getLogger(LE7ELS.class.getName()).log(Level.SEVERE, null, ex);
                     }
                 }
             };
         }
     }
 }
