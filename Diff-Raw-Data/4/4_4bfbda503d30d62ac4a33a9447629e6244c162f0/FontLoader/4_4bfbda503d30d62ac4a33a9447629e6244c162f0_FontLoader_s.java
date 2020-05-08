 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package starparty.utilities;
 
 import java.awt.Font;
 import java.io.File;
 import java.util.HashMap;
 import java.util.Map;
 import org.newdawn.slick.UnicodeFont;
 import org.newdawn.slick.font.effects.ColorEffect;
 
 /**
  *
  * @author Tyler
  */
 public class FontLoader {
   private static Map<String, UnicodeFont> cache = new HashMap<String, UnicodeFont>();
   
   public static UnicodeFont load(String name, int size, boolean bold) {
     String id = name + "-" + size + "-" + bold;
     UnicodeFont font = null;
     
    if (cache.containsKey(id)) {
      return cache.get(id);
     }
     
     try {
       Font baseFont = Font.createFont(Font.TRUETYPE_FONT, new File("resources/font/" + name));
       baseFont.deriveFont(java.awt.Font.PLAIN, 12);
       
       int style = (bold ? java.awt.Font.BOLD : java.awt.Font.PLAIN);
       font = new UnicodeFont(baseFont.deriveFont(style, size));
       font.addAsciiGlyphs();
       font.getEffects().add(new ColorEffect(java.awt.Color.WHITE));
       font.loadGlyphs();
     } catch (Exception e) {
       System.out.println("Error loading font: " + name);
       e.printStackTrace();
     }
     
     cache.put(id, font);
     return font;
   }
   
   public static UnicodeFont load(String name, int size) {
     return load(name, size, false);
   }
 }
