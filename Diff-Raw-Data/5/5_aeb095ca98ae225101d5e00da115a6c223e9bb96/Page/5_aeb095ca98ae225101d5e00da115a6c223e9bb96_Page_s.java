 package me.limebyte.battlenight.core.Other;
 
 import java.util.ArrayList;
 import java.util.List;
 
import me.limebyte.battlenight.core.BattleNight;

 import org.bukkit.ChatColor;
 
 public class Page {
 
     String title, text, header, footer;
 
     public Page(String title, String text) {
         this.title = title;
         this.text = text;
         header = getHeader();
         footer = getFooter();
     }
 
     public String[] getPage() {
         List<String> page = new ArrayList<String>();
         page.add(header);
         page.addAll(processText(text));
         page.add(footer);
         return page.toArray(new String[page.size()]);
     }
 
     private String getHeader() {
         String formattedTitle = " " + ChatColor.WHITE + title + " ";
         int dashCount = 0;
 
         // Calculate total number of dashes
         int dashSpace = getStringWidth("-");
         int spaceAvailable = getWidth() - getStringWidth(formattedTitle);
         int spaceRemaining = spaceAvailable;
 
         while (true) {
             if (dashSpace > spaceRemaining) {
                 break;
             }
 
             dashCount++;
             spaceRemaining -= dashSpace;
         }
 
         // Create dashes String for a single side
         String dashes = ChatColor.DARK_GRAY + "";
         for (int i = 0; i < dashCount / 2; i++) {
             dashes += "-";
         }
 
         // Add the remaining dash if the dashCount was an odd number
         String end = (dashCount % 2 == 0) ? "" : "-";
 
         return dashes + formattedTitle + dashes + end;
     }
 
     private String getFooter() {
         String dashes = ChatColor.DARK_GRAY + "";
         int dashSpace = getStringWidth("-");
         int spaceAvailable = getStringWidth(header);
         int spaceRemaining = spaceAvailable;
 
         while (true) {
             if (dashSpace > spaceRemaining) {
                 break;
             }
 
             dashes += "-";
             spaceRemaining -= dashSpace;
         }
 
         return dashes;
     }
 
     private static int getStringWidth(String text) {
         final int[] characterWidths = new int[] {
                 1, 9, 9, 8, 8, 8, 8, 7, 9, 8, 9, 9, 8, 9, 9, 9,
                 8, 8, 8, 8, 9, 9, 8, 9, 8, 8, 8, 8, 8, 9, 9, 9,
                 4, 2, 5, 6, 6, 6, 6, 3, 5, 5, 5, 6, 2, 6, 2, 6,
                 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 2, 2, 5, 6, 5, 6,
                 7, 6, 6, 6, 6, 6, 6, 6, 6, 4, 6, 6, 6, 6, 6, 6,
                 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 4, 6, 4, 6, 6,
                 3, 6, 6, 6, 6, 6, 5, 6, 6, 2, 6, 5, 3, 6, 6, 6,
                 6, 6, 6, 6, 4, 6, 6, 6, 6, 6, 6, 5, 2, 5, 7, 6,
                 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 4, 6, 3, 6, 6,
                 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 4, 6,
                 6, 3, 6, 6, 6, 6, 6, 6, 6, 7, 6, 6, 6, 2, 6, 6,
                 8, 9, 9, 6, 6, 6, 8, 8, 6, 8, 8, 8, 8, 8, 6, 6,
                 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9,
                 9, 9, 9, 9, 9, 9, 9, 9, 9, 6, 9, 9, 9, 5, 9, 9,
                 8, 7, 7, 8, 7, 8, 8, 8, 7, 8, 8, 7, 9, 9, 6, 7,
                 7, 7, 7, 7, 9, 6, 7, 8, 7, 6, 6, 9, 7, 6, 7, 1
         };
         final String allowedCharacters = " !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_'abcdefghijklmnopqrstuvwxyz {|}~?Ã³ÚÔõÓÕþÛÙÞ´¯ý─┼╔µã¶÷‗¹¨ Í▄°úÏÎâßÝ¾·±Ð¬║┐«¼¢╝í½╗";
         int length = 0;
         for (String line : ChatColor.stripColor(text).split("\n")) {
             int lineLength = 0;
             boolean skip = false;
             for (char ch : line.toCharArray()) {
                 if (skip) {
                     skip = false;
                 } else if (ch == '\u00A7') {
                     skip = true;
                 } else if (allowedCharacters.indexOf(ch) != -1) {
                     lineLength += characterWidths[ch];
                 }
             }
             length = Math.max(length, lineLength);
         }
         return length;
     }
 
     public static int getWidth() {
        return BattleNight.config.getInt("chat-width", 320);
     }
 
     private static List<String> processText(String text) {
         List<String> result = new ArrayList<String>();
 
         if (text.contains("\n")) {
             String[] lines = text.split("\n");
             for (String line : lines) {
                 result.addAll(wrapText(line));
             }
             return result;
         } else {
             return wrapText(text);
         }
     }
 
     private static List<String> wrapText(String text) {
         List<String> lines = new ArrayList<String>();
 
         if (getStringWidth(text) <= getWidth()) {
             lines.add(text);
             return lines;
         }
 
         int spaceSpace = getStringWidth(" ");
         int spaceAvailable = getWidth();
         int spaceRemaining = spaceAvailable;
         String[] words = text.split(" ");
         String currentLine = "";
 
         for (int i = 0; i < words.length; i++) {
             int wordSpace = getStringWidth(words[i]);
 
             if (wordSpace > spaceRemaining) {
                 lines.add(currentLine);
                 spaceRemaining = spaceAvailable;
                 currentLine = "";
             }
 
             currentLine += words[i];
             spaceRemaining -= wordSpace;
 
             if (spaceSpace > spaceRemaining) {
                 lines.add(currentLine);
                 spaceRemaining = spaceAvailable;
                 currentLine = "";
             } else {
                 currentLine += " ";
                 spaceRemaining -= spaceSpace;
             }
         }
 
         return lines;
     }
 }
