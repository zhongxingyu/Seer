 package com.thoughtworks.orteroid.utilities;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class ColorSticky {
 
     private static List<String> colours = new ArrayList<String>() {{
         add("#ffff88");
        add("#ffb058");
        add("#bcee6b");
        add("#d07ddf");
         add("#ccffff");
        add("#17b6ff");
     }};
 
     public static String getColorCode(int selectedIndex){
         return colours.get(selectedIndex%6);
     }
 }
