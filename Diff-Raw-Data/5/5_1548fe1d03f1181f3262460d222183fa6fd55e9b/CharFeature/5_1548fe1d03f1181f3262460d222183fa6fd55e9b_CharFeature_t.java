 package net.bodz.bas.text.util;
 
 import java.net.URL;
 import java.util.HashMap;
 import java.util.Map;
 
 import net.bodz.bas.files.MapsFile;
 import net.bodz.bas.files.MapsFile.PartMap;
 import net.bodz.bas.io.Files;
import net.bodz.bas.io.ByteOuts.BByteOut;
 import net.bodz.bas.lang.err.ParseException;
 import net.bodz.bas.types.Bits;
 
 public class CharFeature {
 
     public static final Map<String, byte[]> octf;
     public static final Map<String, Bits>   bitf;
 
     static {
         octf = new HashMap<String, byte[]>();
         bitf = new HashMap<String, Bits>();
 
         URL part1 = Files.classData(CharFeature.class, "1");
         MapsFile mf = new MapsFile(part1);
         for (PartMap map : mf) {
             String name = map.get("name");
             String bitmap = map.getText();
             if (name.startsWith("~")) {
                 name = name.substring(1);
                 byte[] octs = parseOcts(bitmap);
                 octf.put(name, octs);
             } else {
                 Bits bits;
                 try {
                     bits = parseBits(bitmap);
                 } catch (ParseException e) {
                     throw new Error(e);
                 }
                 bitf.put(name, bits);
             }
         }
     }
 
     public static Bits parseBits(String s) throws ParseException {
         byte[] bitmap = octf.get("bitmap");
         if (bitmap == null)
             throw new IllegalStateException("bitmap isn't prepared");
         byte def = bitmap[bitmap.length - 1];
         char[] cv = s.toCharArray();
         Bits bits = new Bits.IntvLE(256);
         int bitIndex = 0;
         boolean lastBit = false;
         for (int i = 0; i < cv.length; i++) {
             byte c = (byte) cv[i];
             byte oct = c >= bitmap.length ? def : bitmap[c];
             switch (oct) {
             case '.':
                 bits.clear(bitIndex);
                 lastBit = false;
                 bitIndex++;
                 break;
             case '1':
                 bits.set(bitIndex);
                 lastBit = true;
                 bitIndex++;
                 break;
             case '#': // ignore comment
                 break;
             case '~': // ignore fill-term
                 while (bitIndex < bits.size())
                     bits.set(bitIndex++, lastBit);
                 return bits;
             default:
                 throw new ParseException("invalid bitmap char: '" + c + "'");
             }
         }
         return bits;
     }
 
     public static byte[] parseOcts(String bitmap) {
         char[] cv = bitmap.toCharArray();
        BByteOut octs = new BByteOut(128);
         for (int i = 0; i < cv.length; i++) {
             char c = cv[i];
             if (Character.isWhitespace(c))
                 continue;
             octs._write((byte) c);
         }
         return octs.toByteArray();
     }
 
 }
