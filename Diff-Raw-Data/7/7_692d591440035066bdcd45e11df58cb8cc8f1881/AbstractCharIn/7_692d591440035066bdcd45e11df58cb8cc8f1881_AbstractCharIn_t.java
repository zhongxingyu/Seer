 package net.bodz.bas.sio;
 
 import java.io.IOException;
 import java.io.Reader;
 import java.nio.CharBuffer;
 
 public abstract class AbstractCharIn
         implements ICharIn {
 
     @Override
     public int read(char[] chars)
             throws IOException {
         return read(chars, 0, chars.length);
     }
 
     @Override
     public int read(CharBuffer buffer)
             throws IOException {
         int ccRead = 0;
         while (buffer.hasRemaining()) {
             int ch = read();
             if (ch == -1)
                 return ccRead == 0 ? -1 : ccRead;
             buffer.put((char) ch);
            ccRead++;
         }
         return ccRead;
     }
 
     @Override
     public String readString(int maxCharacters)
             throws IOException {
         char[] chars = new char[maxCharacters];
         int ccRead = read(chars);
         if (ccRead == -1)
             return null;
         return new String(chars, 0, ccRead);
     }
 
     @Override
     public void close()
             throws IOException {
     }
 
     public Reader toReader() {
         return new CharInReader(this);
     }
 
 }
