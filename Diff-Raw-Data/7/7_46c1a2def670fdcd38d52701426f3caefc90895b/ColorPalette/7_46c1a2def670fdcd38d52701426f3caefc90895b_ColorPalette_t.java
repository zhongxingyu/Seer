 package com.jediterm.terminal.emulator;
 
 import java.awt.*;
 
 /**
  * @author traff
  */
 @SuppressWarnings("UseJBColor")
 public class ColorPalette {
   private static final Color[] XTERM_PALETTE = new Color[]{
       new Color(0x000000), //Black
       new Color(0xcd0000), //Red 
       new Color(0x00cd00), //Green
       new Color(0xcdcd00), //Yellow
       new Color(0x1e90ff), //Blue 
       new Color(0xcd00cd), //Magenta
       new Color(0x00cdcd), //Cyan
       new Color(0xe5e5e5), //White
       //Bright versions of the ISO colors
       new Color(0x4c4c4c), //Black 
       new Color(0xff0000), //Red
       new Color(0x00ff00), //Green
       new Color(0xffff00), //Yellow
       new Color(0x4682b4), //Blue
       new Color(0xff00ff), //Magenta
       new Color(0x00ffff), //Cyan
       new Color(0xffffff), //White
   };
 
   private static final Color[] WINDOWS_PALETTE = new Color[]{
       new Color(0x000000), //Black
       new Color(0x800000), //Red 
       new Color(0x008000), //Green
       new Color(0x808000), //Yellow
       new Color(0x000080), //Blue 
       new Color(0x800080), //Magenta
       new Color(0x008080), //Cyan
       new Color(0xc0c0c0), //White
       //Bright versions of the ISO colors
       new Color(0x808080), //Black 
       new Color(0xff0000), //Red
       new Color(0x00ff00), //Green
       new Color(0xffff00), //Yellow
       new Color(0x4682b4), //Blue
       new Color(0xff00ff), //Magenta
       new Color(0x00ffff), //Cyan
       new Color(0xffffff), //White
   };
 
 
   public static Color[] getCurrentColorSettings() {
    return /*Platform.isWindows() ? WINDOWS_PALETTE :*/ XTERM_PALETTE;
    //TODO: move colors to terminal, emulator should deal only with color codes not real colors.
   }
 
   public static Color getIndexedColor(int index) {
     return (index < 16) ? getCurrentColorSettings()[index] : getXTerm256(index);
   }
 
   private static Color getXTerm256(int index) {
     return COL_RES_256[index - 16];
   }
 
   //The code below is translation of xterm's 256colres.pl
 
   // colors 16-231 are a 6x6x6 color cube
   private static final Color[] COL_RES_256 = new Color[240];
 
   static {
     for (int red = 0; red < 6; ++red) {
       for (int green = 0; green < 6; ++green) {
         for (int blue = 0; blue < 6; ++blue) {
           int code = 36 * red + 6 * green + blue;
           COL_RES_256[code] =
               new Color(red > 0 ? (40 * red + 55) : 0,
                   green > 0 ? (40 * green + 55) : 0,
                   blue > 0 ? (40 * blue + 55) : 0);
         }
       }
     }
 
     // colors 232-255 are a grayscale ramp, intentionally leaving out
     // black and white
     for (int gray = 0; gray < 24; ++gray) {
       int level = 10 * gray + 8;
       COL_RES_256[216 + gray] = new Color(level, level, level);
     }
   }
 }
