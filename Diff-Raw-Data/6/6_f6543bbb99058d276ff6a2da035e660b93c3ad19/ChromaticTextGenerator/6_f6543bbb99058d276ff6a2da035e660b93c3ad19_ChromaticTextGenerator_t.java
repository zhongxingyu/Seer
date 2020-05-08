 package net.mirky.redis;
 
 import java.io.PrintStream;
 
 public final class ChromaticTextGenerator {
     private final BichromaticStringBuilder bsb;
     private final BichromaticStringBuilder.DelimitedMode del;
     private final PrintStream port;
     
     public ChromaticTextGenerator(char tokenListStart, char tokenListEnd, PrintStream port) {
        bsb = new BichromaticStringBuilder("34;1"); // blue
         del = new BichromaticStringBuilder.DelimitedMode(bsb, tokenListStart, ' ', tokenListEnd);
         this.port = port;
     }
 
     public final void appendLeftPadded(String s, char padding, int minWidth) {
         for (int i = minWidth - s.length(); i > 0; i--) {
             bsb.sb.append(padding);
         }
         bsb.sb.append(s);
     }
 
     public final void appendInteger(int i) {
         del.delimitForPlain();
         bsb.sb.append(i);
     }
 
     public final void appendChar(char c) {
         del.delimitForPlain();
         bsb.sb.append(c);
     }
 
     public final void appendToken(String s) {
        del.delimitForColour();
         bsb.sb.append(s);
     }
 
     public final void appendHexByteToken(byte b) {
         appendToken(Hex.b(b));
     }
 
     public final void terpri() {
         del.delimitForPlain();
         bsb.printLine(port);
        bsb.clear();
     }
 }
