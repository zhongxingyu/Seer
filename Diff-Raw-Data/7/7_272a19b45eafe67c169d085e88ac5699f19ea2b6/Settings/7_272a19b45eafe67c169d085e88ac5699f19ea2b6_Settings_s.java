 package Game;
 
 import java.io.*;
 import org.ini4j.*;
 import Log.Logger;
 
 public class Settings {
 
     private int roundTime;
     private int respawnTime;
     private int countdownTime;
     private int baudRate;
     private String settingsFile;
     private int windowWidth, windowHeigth;
 
     public Settings() {
         String settingsDir = System.getProperty("user.home") + "/.pbcontrol";
 
         /* We maken een settings directory aan, hierin slaan we oa de gametime op */
         File dir = new File(settingsDir);
         if (!dir.exists()) {
             if (dir.mkdir()) {
                 Logger.msg("Info", "Created settings directory (" + settingsDir +")");
             } else {
                 Logger.msg("Info", "Could not create settings directory (" + settingsDir +")");
             }
         }
 
         this.settingsFile = settingsDir + "/settings.ini";
 
     }
 
     public Boolean save() {
 
         /* Eerst maken we de settingsfile aan als deze er niet is */
         File file = new File(this.settingsFile);
         if (!file.exists()) {
             Logger.msg("Info", "Creating settings file (" + this.settingsFile + ")");
             try {
            file.createNewFile();
             } catch (IOException e) {
            Logger.msg("Warn", "Couldn't create settings file, message was: " + e.getMessage());
            return false;
             }
         }
 
         /* Hierna stellen we de ini samen */
         try {
             Ini ini = new Ini(file);
             ini.put("game", "roundtime", this.roundTime);
             ini.put("game", "respawntime", this.respawnTime);
             ini.put("game", "countdowntime", this.countdownTime);
             ini.put("game", "windowwidth", this.windowWidth);
             ini.put("game", "windowheigth", this.windowHeigth);
             ini.put("serial", "baudrate", this.baudRate);
             ini.store();
         } catch (IOException e) {
             Logger.msg("Warn", "Couldn't save settings, message was: " + e.getMessage());
             return false;
         }
 
         return true;
     }
 
     public Boolean load() {
         try {
             Ini ini = new Ini(new File(this.settingsFile));
             this.roundTime = ini.get("game", "roundtime", int.class);
             this.respawnTime = ini.get("game", "respawntime", int.class);
             this.countdownTime = ini.get("game", "countdowntime", int.class);
             this.windowWidth = ini.get("game", "windowwidth", int.class);
             this.windowHeigth = ini.get("game", "windowheigth", int.class);
             this.baudRate = ini.get("serial", "baudrate", int.class);
         } catch (IOException e) {
             Logger.msg("Warn", "Couldn't load settings, message was: " + e.getMessage());
             return false;
         }
 
         return true;
     }
 
     public int getRoundTime() {
         return this.roundTime * 60;
     }
 
     public int getRespawnTime() {
         return this.respawnTime * 60;
     }
 
     public int getCountdownTime() {
         return this.countdownTime;
     }
 
     public int getBaudRate() {
         return this.baudRate;
     }
 
     public void setRoundTime(int roundTime) {
         this.roundTime = roundTime;
     }
 
     public void setRespawnTime(int respawnTime) {
         this.respawnTime = respawnTime;
     }
 
     public void setCountdownTime(int countdownTime) {
         this.countdownTime = countdownTime;
     }
 
     public void setBaudRate(int baudRate) {
         this.baudRate = baudRate;
     }
 
     public int getWindowWidth() {
 
         if (this.windowWidth == 0) {
             this.windowWidth = 900;
         }
 
         return this.windowWidth;
     }
 
     public int getWindowHeigth() {
 
         if (this.windowHeigth == 0) {
             this.windowHeigth = 600;
         }
 
         return this.windowHeigth;
     }
 
     public void setWindowWidth(int width) {
         this.windowWidth = width;
     }
 
     public void setWindowHeigth(int heigth) {
         this.windowHeigth = heigth;
     }
 
 }
