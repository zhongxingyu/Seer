 package com.jpii.navalbattle.renderer;
 
 import java.awt.*;
 import java.awt.image.BufferedImage;
 
 /**
  * A bunch of rendering constants that effect stuff in com.jpii.navalbattle.renderer.
  * @author MKirkby
  *
  */
 public class RenderConstants {
 	// If it begins with GEN, it is related to the Generator.
 	// If it begins with OPT, then it is an option.
 	// If it begins with CLOUD, then it...
     public static double GEN_WATER_HEIGHT = 0.65;
     public static double GEN_MOUNTAIN_HEIGHT = 0.8;
     public static Color GEN_MOUNTAIN_COLOR = new Color(68, 74, 43); //new Color(131,111,65);
     public static Color GEN_WATER_COLOR = new Color(46, 61, 114); //new Color(61,54,188);
     public static Color GEN_GRASS_COLOR = new Color(60, 101, 42);
     public static Color GEN_SAND_COLOR = new Color(77, 95, 44);
     public static Color GEN_SAND_COLOR2 = new Color(78, 71, 33);
     public static double GEN_TERRAIN_ROUGHNESS = 0.35; //0.6;
     public static int GEN_COLOR_DIFF = 6;
     public static int GEN_SMOOTHIE = 30;
     public static int CLOUD_MAX_SIZE = 60;
     public static int CLOUD_MIN_SIZE = 40;
     public static int CLOUD_MAX = 50;
     public static boolean OPT_INVERSE_MOUSE = false;
     public static boolean OPT_CLOUDS_ON = true;
    public static boolean OPT_MULTITHREADING = false;
    public static int OPT_MAX_FPS = Integer.MAX_VALUE - 1;
     
     public static int NIGHT_MAX_DARKNESS = 100;
     
     public static Color GUI_GAME_FONT_COLOR = new Color(188,169,95);
     
     public static double CURRENT_TIME_OF_DAY = 60;
     public static int DAYNIGHT_LENGTH_IN_SECONDS = 120;
     public static BufferedImage TIME_OVERLAY;
 
     // Currently not fully implemented:
     public static boolean OPT_HIGH_PRIORITY_SCHEDULING = true;
     public static boolean OPT_NOISY_BACKGROUND_ON = true;
     public static boolean OPT_SHOW_OMNIMAP = true;
     public static int OPT_NUM_UPDATES = 10;
     public static boolean OPT_DAYNIGHT_CYCLES_ON = true;
     public static boolean OPT_WHALES = true;
     public static RenderingQuality OPT_RENDERING_QUALITY = RenderingQuality.FullSpeedAhead;
 }
