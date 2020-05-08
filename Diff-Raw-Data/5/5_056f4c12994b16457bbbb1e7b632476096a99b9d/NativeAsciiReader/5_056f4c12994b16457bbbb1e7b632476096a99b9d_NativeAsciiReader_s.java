 package org.webmacro.util;
 
 import java.io.BufferedReader;
 import java.io.Reader;
 import java.io.IOException;
 /**
  * Reads a file that has been encoded with Java's unicode escape syntax,
  * typically converted with native2ascii.
  *
 * All character sequences like \uxxxx will be converted to a single unicode
  * character.  Other '\' escaped characters will be passed unchanged. An
 * error will be thrown if the \u is not followed by 4 hexadecimal characters
  *
  *
  */
 public class NativeAsciiReader extends BufferedReader {
 
 /*
     The constructors simply echo those in the super-class
 */
     private static int defaultCharBufferSize = 8192;
     public static final String RCS = "$Id$";
 
     public NativeAsciiReader(Reader in, int sz) {
     	super(in, sz);
     }
 
     public NativeAsciiReader(Reader in) {
     	this(in, defaultCharBufferSize);
     }    
 
 /**
  *  Read a sequence of characters into the given buffer.   Since we're 
  *  extending BufferedReader, it is efficient enough to read one character
  *  at a time
  */
     public int read(char cbuf[], int off, int len) throws IOException {
         int i=0;
         while (i<len) {
             int c = read();
             if (i==0 && c == -1) return -1;
             if (c==-1) return i;
             if (c=='\\') {
                 mark(1);
                 if (read() != 'u') {
                     reset();
                 }
                 int value = 0;
                 int a;
 
 		        for (int j=0; j<4; j++) {
 		            a = read();
 		            switch (a) {
                         case '0': case '1': case '2': case '3': case '4':
                         case '5': case '6': case '7': case '8': case '9':
                             value = (value << 4) + a - '0';
                             break;
                         case 'a': case 'b': case 'c':
                         case 'd': case 'e': case 'f':
                             value = (value << 4) + 10 + a - 'a';
                             break;
                         case 'A': case 'B': case 'C':
                         case 'D': case 'E': case 'F':
                             value = (value << 4) + 10 + a - 'A';
                             break;
                         default:
                             throw new IllegalArgumentException(
                                     "Malformed \\uxxxx encoding.");
                     }
                 }
                 c = (char) value;
             }
             cbuf[i+off] = (char) c;
             i++;
         }
         return i;
     }
 
 }
