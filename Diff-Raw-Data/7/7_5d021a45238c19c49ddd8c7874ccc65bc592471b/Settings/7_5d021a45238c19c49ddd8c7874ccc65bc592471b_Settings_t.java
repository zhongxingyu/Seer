 package settings;
 
 import graphics.Renderer;
 import graphics.particles.ParticleSystem;
 
 import java.awt.event.KeyEvent;
 import java.io.File;
 import java.io.IOException;
 
 import com.sun.org.apache.xml.internal.security.exceptions.Base64DecodingException;
 
 public class Settings {
     public static class Profile{
         public static class Keys{
             public static int forward = 0;
             public static int backward = 0;
 
             public static int pitchUp = 0;
             public static int pitchDown = 0;
 
             public static int rollLeft = 0;
             public static int rollRight = 0;
 
             public static int yawLeft = 0;
             public static int yawRight = 0;
 
             public static int shoot = 0;
             public static int previousWeapon = 0;
             public static int nextWeapon = 0;
 
             public static int energyGun = 0;
             public static int energyShield = 0;
             public static int energySpeed = 0;
 
             public static int energyModifier=0;
         }
         public static boolean particlesOn = false;
         public static boolean soundOn = true;
         public static String shader = null;
     }
 
     public static void debugKeys(){
         System.out.println("forward: "+Profile.Keys.forward);
         System.out.println("backward: "+Profile.Keys.backward);
         System.out.println("pitchUp: "+Profile.Keys.pitchUp);
         System.out.println("pitchDown: "+Profile.Keys.pitchDown);
         System.out.println("rollLeft: "+Profile.Keys.rollLeft);
         System.out.println("rollRight: "+Profile.Keys.rollRight);
         System.out.println("yawLeft: "+Profile.Keys.yawLeft);
         System.out.println("yawRight: "+Profile.Keys.yawRight);
         System.out.println("shoot: "+Profile.Keys.shoot);
         System.out.println("nextWeapon: "+Profile.Keys.nextWeapon);
         System.out.println("previousWeapon: "+Profile.Keys.previousWeapon);
     }
 
     public static void init() throws IOException, Base64DecodingException{
         setDefaults();
         File settingsFile = new File("config/settings.ini");
         Util.checkSettingFile(settingsFile);
 
         File configFile = null;
 
         String[] settingsLines = Util.stringFromFile(settingsFile).split("\n");
         for(String line:settingsLines){
             String[] rmComment = line.split(";");
             String[] part = rmComment[0].split("=");
 
             if(part[0].equalsIgnoreCase("conf")){
                 configFile = new File("config/"+part[1]);
                 Util.checkConfFile(configFile);
             }
             if(part[0].equalsIgnoreCase("shader")){
                 setShader(part[1]);
             }
             if(part[0].equalsIgnoreCase("particles")){
                 if(part[1].equalsIgnoreCase("on")){
                     setParticles(true);
                 }
                 else{
                     setParticles(false);
                 }
             }
             if(part[0].equalsIgnoreCase("sound")){
                 if(part[1].equalsIgnoreCase("on")){
                     setSound(true);
                 }
                 else{
                     setSound(false);
                 }
             }
             if(part[0].equalsIgnoreCase("debug")){
                 if(part[1].equalsIgnoreCase("on")){
                     Debug.debugConsole();
                 }
                 else if (part[1].equalsIgnoreCase("off")){
                     Debug.debugOff();
                 }
                 else{
                     try{
                         Debug.debugFile(new File(part[1]));
                     }
                     catch(Exception e){
                         e.printStackTrace();
                         Debug.debugOff();
                     }
                 }
             }
         }
 
         parseKeys(configFile);
     }
     
     private static void parseKeys(File configFile) throws IOException{
         String[] configLines = Util.stringFromFile(configFile).split("\n");
         for(String line: configLines){
             String[] rmComment = line.split(";");
             String[] part = rmComment[0].split("=");
 
             if(part[0].equalsIgnoreCase("forward")){
                 Profile.Keys.forward = stringToKey(part[1]);
             }
             else if(part[0].equalsIgnoreCase("backward")){
                 Profile.Keys.backward = stringToKey(part[1]);
             }
             else if(part[0].equalsIgnoreCase("pitch_up")){
                 Profile.Keys.pitchUp = stringToKey(part[1]);
             }
             else if(part[0].equalsIgnoreCase("pitch_down")){
                 Profile.Keys.pitchDown = stringToKey(part[1]);
             }
             else if(part[0].equalsIgnoreCase("roll_left")){
                 Profile.Keys.rollLeft = stringToKey(part[1]);
             }
             else if(part[0].equalsIgnoreCase("roll_right")){
                 Profile.Keys.rollRight = stringToKey(part[1]);
             }
             else if(part[0].equalsIgnoreCase("yaw_left")){
                 Profile.Keys.yawLeft = stringToKey(part[1]);
             }
             else if(part[0].equalsIgnoreCase("yaw_right")){
                 Profile.Keys.yawRight = stringToKey(part[1]);
             }
             else if(part[0].equalsIgnoreCase("shoot")){
                 Profile.Keys.shoot = stringToKey(part[1]);
             }
             else if(part[0].equalsIgnoreCase("next_weapon")){
                 Profile.Keys.nextWeapon  = stringToKey(part[1]);
             }
             else if(part[0].equalsIgnoreCase("previous_weapon")){
                 Profile.Keys.previousWeapon  = stringToKey(part[1]);
             }
             else if(part[0].equalsIgnoreCase("energy_gun")){
                 Profile.Keys.energyGun = stringToKey(part[1]);
             }
             else if(part[0].equalsIgnoreCase("energy_shield")){
                 Profile.Keys.energyShield = stringToKey(part[1]);
             }
             else if(part[0].equalsIgnoreCase("energy_speed")){
                 Profile.Keys.energySpeed = stringToKey(part[1]);
             }
             else if(part[0].equalsIgnoreCase("energy_modifier")){
                 Profile.Keys.energyModifier=stringToKey(part[1]);
             }
         }
     }
 
     private static int stringToKey(String input){
 
         /**
          * LETTERS
          */
         if(input.equalsIgnoreCase("a")){
             return KeyEvent.VK_A;
         }
         if(input.equalsIgnoreCase("b")){
             return KeyEvent.VK_B;
         }
         if(input.equalsIgnoreCase("c")){
             return KeyEvent.VK_C;
         }
         if(input.equalsIgnoreCase("d")){
             return KeyEvent.VK_D;
         }
         if(input.equalsIgnoreCase("e")){
             return KeyEvent.VK_E;
         }
         if(input.equalsIgnoreCase("f")){
             return KeyEvent.VK_F;
         }
         if(input.equalsIgnoreCase("g")){
             return KeyEvent.VK_G;
         }
         if(input.equalsIgnoreCase("h")){
             return KeyEvent.VK_H;
         }
         if(input.equalsIgnoreCase("i")){
             return KeyEvent.VK_I;
         }
         if(input.equalsIgnoreCase("j")){
             return KeyEvent.VK_J;
         }
         if(input.equalsIgnoreCase("k")){
             return KeyEvent.VK_K;
         }
         if(input.equalsIgnoreCase("l")){
             return KeyEvent.VK_L;
         }
         if(input.equalsIgnoreCase("m")){
             return KeyEvent.VK_M;
         }
         if(input.equalsIgnoreCase("n")){
             return KeyEvent.VK_N;
         }
         if(input.equalsIgnoreCase("o")){
             return KeyEvent.VK_O;
         }
         if(input.equalsIgnoreCase("p")){
             return KeyEvent.VK_P;
         }
         if(input.equalsIgnoreCase("q")){
             return KeyEvent.VK_R;
         }
         if(input.equalsIgnoreCase("s")){
             return KeyEvent.VK_S;
         }
         if(input.equalsIgnoreCase("t")){
             return KeyEvent.VK_T;
         }
         if(input.equalsIgnoreCase("u")){
             return KeyEvent.VK_U;
         }
         if(input.equalsIgnoreCase("v")){
             return KeyEvent.VK_V;
         }
         if(input.equalsIgnoreCase("w")){
             return KeyEvent.VK_W;
         }
         if(input.equalsIgnoreCase("x")){
             return KeyEvent.VK_X;
         }
         if(input.equalsIgnoreCase("y")){
             return KeyEvent.VK_Y;
         }
         if(input.equalsIgnoreCase("z")){
             return KeyEvent.VK_Z;
         }
 
         /**
          * Arrows
          */
         if(input.equalsIgnoreCase("up")){
             return KeyEvent.VK_UP;
         }
         if(input.equalsIgnoreCase("down")){
             return KeyEvent.VK_DOWN;
         }
         if(input.equalsIgnoreCase("left")){
             return KeyEvent.VK_LEFT;
         }
         if(input.equalsIgnoreCase("right")){
             return KeyEvent.VK_RIGHT;
         }
 
         /**
          * NUMBERS
          */
         if(input.equalsIgnoreCase("0")){
             return KeyEvent.VK_0;
         }
         if(input.equalsIgnoreCase("1")){
             return KeyEvent.VK_1;
         }
         if(input.equalsIgnoreCase("2")){
             return KeyEvent.VK_2;
         }
         if(input.equalsIgnoreCase("3")){
             return KeyEvent.VK_3;
         }
         if(input.equalsIgnoreCase("4")){
             return KeyEvent.VK_4;
         }
         if(input.equalsIgnoreCase("5")){
             return KeyEvent.VK_5;
         }
         if(input.equalsIgnoreCase("6")){
             return KeyEvent.VK_6;
         }
         if(input.equalsIgnoreCase("7")){
             return KeyEvent.VK_7;
         }
         if(input.equalsIgnoreCase("8")){
             return KeyEvent.VK_8;
         }
         if(input.equalsIgnoreCase("9")){
             return KeyEvent.VK_9;
         }
 
         /**
          * Numpad
          */
         if(input.equalsIgnoreCase("num_0")){
             return KeyEvent.VK_NUMPAD0;
         }
         if(input.equalsIgnoreCase("num_1")){
             return KeyEvent.VK_NUMPAD1;
         }
         if(input.equalsIgnoreCase("num_2")){
             return KeyEvent.VK_NUMPAD2;
         }
         if(input.equalsIgnoreCase("num_3")){
             return KeyEvent.VK_NUMPAD3;
         }
         if(input.equalsIgnoreCase("num_4")){
             return KeyEvent.VK_NUMPAD4;
         }
         if(input.equalsIgnoreCase("num_5")){
             return KeyEvent.VK_NUMPAD5;
         }
         if(input.equalsIgnoreCase("num_6")){
             return KeyEvent.VK_NUMPAD6;
         }
         if(input.equalsIgnoreCase("num_7")){
             return KeyEvent.VK_NUMPAD7;
         }
         if(input.equalsIgnoreCase("num_8")){
             return KeyEvent.VK_NUMPAD8;
         }
         if(input.equalsIgnoreCase("num_9")){
             return KeyEvent.VK_NUMPAD9;
         }
 
         /**
          * misc keys
          */
         if(input.equalsIgnoreCase("space")){
             return KeyEvent.VK_SPACE;
         }
         if(input.equalsIgnoreCase("shift")){
             return KeyEvent.VK_SHIFT;
         }
         if(input.equalsIgnoreCase("ctrl")){
             return KeyEvent.VK_CONTROL;
         }
         if(input.equalsIgnoreCase("alt")){
             return KeyEvent.VK_ALT;
         }
 
         try{
             return Integer.parseInt(input);
         }
         catch(Exception e){
             System.err.println("illegal key modifier: "+input);
             return -1;
         }
     }
 
 
 
     private static void setDefaults(){
         setParticles(true);
         setSound(true);
         setShader("texture");
     }
     
     private static void setParticles(boolean b){
         Profile.particlesOn=b;
         ParticleSystem.enabled=b;
     }
     private static void setSound(boolean b){
         Profile.soundOn=b;
         sound.Manager.enabled=b;
     }
     private static void setShader(String s){
         Profile.shader=s;
         Renderer.shaderString=s;
     }
     
     public static void main(String[] args) throws IOException, Base64DecodingException{
         init();
         Settings.debugKeys();
     }
 }
