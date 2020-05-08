 /**
  * Copyright (c) andreabont, 2013
  *
  * IrcIntegration is distributed under the terms of the General Minecraft Mod
  * Public License 1.0, or GMMPL. Please check the contents of the license
  * located in the file LICENSE
  */
 package it.antanicraft.ircintegration.colors;
 
 public class ColorManager {
 
     private static ColorManager instance;
 
     private final char MCE  = 0x00A7;   /* Minecraft Escape Character */
     private final char IRCE = 0x03;     /* IRC Escape Character */
 
     private ColorManager(){}
 
     public static ColorManager getInstance() {
         if(instance == null) {
             instance = new ColorManager();
         }
             return instance;
     }
 
    public String colorMCText(String text, McTextCodes color) {
        return color.toString() + text + McTextCodes.RESET.toString();

     }
 
 }
