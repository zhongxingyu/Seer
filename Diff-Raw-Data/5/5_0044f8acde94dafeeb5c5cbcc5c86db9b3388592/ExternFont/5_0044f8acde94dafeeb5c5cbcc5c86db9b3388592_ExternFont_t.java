 /*
  * Copyright (C) 2010 France Telecom
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package memoplayer;
 
 import java.io.*;
 
 //#ifndef BlackBerry
 import javax.microedition.lcdui.Graphics;
 import javax.microedition.lcdui.Font;
 import javax.microedition.lcdui.Image;
 //#endif
 
 // class to store the glyph position inside the image
 class GlyphInfo {
     int id, x, y, w, h, l, o;
     int color;
     
     GlyphInfo (DataInputStream is) {
         id = Decoder.readUnsignedByte (is)*256 + Decoder.readUnsignedByte (is);
         x = Decoder.readUnsignedByte (is); // the left position in the image 
         y = Decoder.readUnsignedByte (is); // the top position in the image 
         w = Decoder.readUnsignedByte (is); // the width in the image 
         h = Decoder.readUnsignedByte (is); // the height in the image 
         l = Decoder.readUnsignedByte (is); // the left offset before drawing
         if (l > 127) { l = l - 256; }
         o = Decoder.readUnsignedByte (is); // the offset to draw the next char i.e. the "length"
         // Logger.println ("Glyph #"+id+": "+x+", "+y+" x "+w+", "+h+" / "+l+", "+o);
         color = 0;
     }
 }
 
 public abstract class ExternFont {
     String m_name; // family_size
     int m_refCount; // the number of times this font is used
     ExternFont m_next; // to link fonts
 
     static ExternFont m_root = null;
 
     // check if an existig font is already linked, create it otherwize
     static ExternFont open (Decoder decoder, String name, int size) {
         String baseName = name+"_"+size;
         // check if already stored
         ExternFont target = m_root != null ? m_root.find (baseName) : null;
         if (target != null) {
             target.m_refCount++;
             //Logger.println ("ExternFont: reuse existing font "+target+ "for "+baseName+ "refcount="+target.m_refCount);
             return target;
         }
         // does not exist yet, create it
         //Logger.println ("BitmapFont.open with decoder "+decoder+" and name "+baseName);
         byte [] data = decoder.getFontDesc (baseName+".desc");
         Image image = decoder.getImage (baseName+".png");
         //Logger.println ("ExternFont open: image="+image+", data="+data);
         if (data != null && image != null) {
             DataInputStream is = new DataInputStream (new ByteArrayInputStream (data));
             m_root = new BitmapFont (baseName, image, is, m_root);
             //Logger.println ("ExternFont: adding new font "+m_root+ "for "+baseName);
             return m_root;
         }
         return null;
    }
 
     // check if an existig font is already linked, create it otherwize
     static ExternFont open (int family, int style, int size) {
         String baseName = "System_"+family+"_"+style+"_"+size;
         // check if already stored
         ExternFont target = m_root != null ? m_root.find (baseName) : null;
         if (target != null) {
             target.m_refCount++;
             return target;
         }
         // does not exist yet, create it
         //Logger.println ("SystemFont.open with name "+baseName);
         m_root = new SystemFont (baseName, family, style, size, m_root);
         return m_root;
    }
 
     ExternFont (String name, ExternFont next) {
         m_refCount = 1;
         m_name = name;
         m_next = next;
     }
 
     ExternFont find (String name) {
         if (name.equals (m_name)) {
             return this;
         } else if (m_next != null) {
             return m_next.find (name);
         } else {
             return null;
         }
     }
 
     ExternFont skip (ExternFont target) {
         if (target == this) {
             return m_next;
         } else if (m_next != null) {
             m_next = m_next.skip (target);
         } else {
             Logger.println ("ExternFont.skip: unexpected end fo chain!");
         }
         return this;
     }
 
     void release () {
         m_refCount--;
         //Logger.println ("ExternFont: releasing font "+this+ ", refCount="+m_refCount);
         if (m_refCount == 0) { // remove from the global linked chain
             m_root = m_root.skip (this); // root cannot be nul as contains at least this
         }
     }
 
     abstract int getHeight ();
 
     abstract int getTopPosition ();
     
     abstract int getBaselinePosition ();
 
     abstract int stringWidth (String text);
 
     abstract int charWidth (char c);
 
     abstract int charsWidth (char [] ch, int offset, int length);
 
     abstract void setAsCurrent (Graphics g, int color);
 
     abstract void drawString (Graphics g, String text, int x, int y, int anchor);
 }
 
 class BitmapFont extends ExternFont {
     int m_nbGlyphs, m_maxBaseline, m_maxHeight;
     GlyphInfo [] m_glyphs; // the array of glyphs
     int [] m_image; // the font pixels stored as an image data
     int m_width, m_height; // the image size
     int m_color; // the color to draw the font with
 
     BitmapFont (String name, Image image, DataInputStream is, ExternFont next) {
         super (name, next);
         // retrieve the pixels
         m_width = image.getWidth();
         m_height = image.getHeight();
        m_image = new int [m_width*(m_height+1)]; // +1 : Alcatel OT-800 bugs with drawRGB() on last pixel line
         image.getRGB (m_image, 0, m_width, 0, 0, m_width, m_height);
         // the font itself
         m_maxBaseline = Decoder.readUnsignedByte (is);
        m_maxHeight = Decoder.readUnsignedByte (is);
         m_nbGlyphs = Decoder.readUnsignedByte (is)*256 + Decoder.readUnsignedByte (is);
         //Logger.println ("Extern font "+m_name+ ", maxBaseline="+m_maxBaseline+", maxHeight="+m_maxHeight+", nbChars="+m_nbGlyphs);
         m_glyphs = new GlyphInfo [m_nbGlyphs];
         for (int i = 0; i <m_nbGlyphs; i++) {
             m_glyphs[i] = new GlyphInfo (is);
         }
     }
 
     GlyphInfo getGlyph (int id) {
         int mini = 0; 
         int maxi = m_nbGlyphs-1; 
         //Logger.println ("getGlyph: -------------------------------");
         while (maxi >= mini) {
             int middle = (mini+maxi)/2;
             GlyphInfo g = m_glyphs[middle];
             //Logger.print ("getGlyph: mini="+mini+", middle="+((mini+maxi)/2)+", maxi="+maxi+", g.id="+g.id+", id="+id);
             if (g.id == id) { 
                 //Logger.println (" => found");
                 return g;
             } else if (g.id < id) {
                 mini = middle+1;
             } else {
                 maxi = middle-1;
             }
             //Logger.println (" => : mini="+mini+", maxi="+maxi);
         }
         //Logger.println ("ExterFont.getGlyph: cannot find:"+id);
         return null;
     }
 
     int getHeight () { return m_maxHeight; };
 
     int getTopPosition () { return 0; }
     
     int getBaselinePosition () { return m_maxBaseline; }
 
     int stringWidth (String text) { 
         int total = 0;
         int nbc = text.length ();
         for (int i = 0; i < nbc; i++) {
             GlyphInfo g = getGlyph (text.charAt (i));
             if (g != null) {
                 total += g.o;
             }
         }
         return total;
     }
 
     int charsWidth (char [] ch, int offset, int length) { 
         int total = 0;
         for (int i = 0; i < length; i++) {
             GlyphInfo g = getGlyph (ch[offset+i]);
             if (g != null) {
                 total += g.o;
             }
         }
         return total;
     }
     int charWidth (char c) {
         GlyphInfo g = getGlyph (c);
         return g != null ? g.o : 0;
     }
 
     void setAsCurrent (Graphics g, int color) {
         m_color = color & 0xFFFFFF;
     }
     
     void updatePixels (GlyphInfo g) {
         int base = g.x + g.y*m_width;
         for (int j = m_maxHeight; j > 0; j--) {
             int p = base;
             for (int i = g.w; i > 0; i--, p++) {
                 m_image[p] = (m_image[p] & 0xFF000000)|m_color;
             }
             base += m_width;
         }
     }
     
     void drawString (Graphics g, String text, int x, int y, int anchor) {
         // the hard stuff
         int nbc = text.length ();
         for (int i = 0; i < nbc; i++) {
             GlyphInfo glyph = getGlyph (text.charAt (i));
             if (glyph != null) {
                 if (glyph.w > 0) {
                     // check the color
                     if (glyph.color != m_color) {
                         glyph.color = m_color;
                         updatePixels (glyph);
                     }
                     // draw the char
                     ImageContext.blit (g, m_image, glyph.x + glyph.y*m_width, m_width, x + glyph.l, y, glyph.w, m_maxHeight, true);
                 }
                 x += glyph.o;
             }
         }
     }
 
 }
 
 class SystemFont extends ExternFont {
     static int s_vertOffset = 0;
     static {
         String s = MiniPlayer.getJadProperty ("MeMo-SystemFont-VOffset");
         if (s != "") {
             try { s_vertOffset = Integer.parseInt(s); } catch (Exception e) { }
         }
     }
     
     Font m_font;
 
     SystemFont (String name, int family, int style, int size, ExternFont next) {
         super (name, next);
         // the font itself
         m_font = Font.getFont (family, style, size);
     }
 
     int getHeight () { return m_font.getHeight (); };
 
     int getTopPosition () { return -s_vertOffset; }
     
     int getBaselinePosition () { return m_font.getBaselinePosition () - s_vertOffset; }
 
     int stringWidth (String text) { return m_font.stringWidth (text); }
 
     int charWidth (char ch) { return m_font.charWidth (ch); }
 
     int charsWidth (char [] ch, int offset, int length) { return m_font.charsWidth (ch, offset, length); }
 
     void setAsCurrent (Graphics g, int color) {
         g.setFont (m_font);
         g.setColor (color);
     }
 
     void drawString (Graphics g, String text, int x, int y, int anchor) {
         g.drawString (text, x, y, anchor);
     }
 
 
 }
