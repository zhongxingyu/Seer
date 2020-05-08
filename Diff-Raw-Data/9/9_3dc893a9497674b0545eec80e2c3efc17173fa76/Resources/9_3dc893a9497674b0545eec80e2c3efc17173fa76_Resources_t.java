 import java.util.*;
 import java.awt.*;
 import java.io.*;
 
 public class Resources {
     public static Image dirtImage;
 
     public static void load() {
        dirtImage = Util.loadImage("Dirt.png");
         imageMap = new HashMap<String, Image>();
 
         for (File pathname: new File("resources/img/").listFiles()) {
             String filename = pathname.getName();
             String[] hunks = filename.split("\\.");
 
             if (hunks[1].equals("png")) {
                 imageMap.put(hunks[0], Util.loadImage(filename));
             }
         }
 
         System.out.println(imageMap.keySet());
     }
 
     private static Map<String, Image> imageMap;
     public static Image imageByName(String name) {
         return imageMap.get(name);
     }
 }
