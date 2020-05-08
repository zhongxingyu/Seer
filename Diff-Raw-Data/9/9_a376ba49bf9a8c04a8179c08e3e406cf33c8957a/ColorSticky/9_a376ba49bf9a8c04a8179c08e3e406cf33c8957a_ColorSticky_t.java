 package com.thoughtworks.orteroid.utilities;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class ColorSticky {
 
     private static List<String> colours = new ArrayList<String>() {{
         add("#ffff88");
        add("#eed2fe");
        add("#dcffe0");
        add("#ffc690");
         add("#ccffff");
        add("#c3ee8f");
     }};
 
     public static String getColorCode(int selectedIndex){
         return colours.get(selectedIndex%6);
     }
 }
