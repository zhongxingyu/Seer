 package net.jhorstmann.base64;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.Reader;
 
 public class Base64StreamDecoder {
 
     private static final byte[] codes = new byte[256];
 
     static {
         for (int i = 0; i < 256; i++) {
             codes[i] = -1;
         }
         for (int i = 'A'; i <= 'Z'; i++) {
             codes[i] = (byte)(i - 'A');
         }
         for (int i = 'a'; i <= 'z'; i++) {
             codes[i] = (byte)(26 + i - 'a');
         }
         for (int i = '0'; i <= '9'; i++) {
             codes[i] = (byte)(52 + i - '0');
         }
         codes['+'] = 62;
         codes['/'] = 63;
     }
     private static final int STATE_EOF = -1;
     private static final int STATE_START = 0;
     private static final int STATE_SECOND = 1;
     private static final int STATE_THIRD = 2;
     private static final int STATE_PADDING = 3;
     private static final int STATE_FOURTH = 4;
     private int state = 0;
     private int buf = 0;
 
     void reset() {
         this.state = 0;
         this.buf = 0;
     }
 
     private static boolean isSpace(int ch) {
         return ch == '\r' || ch == '\n' || ch == '\t' || ch == '\f' || ch == ' ';
     }
 
     private static int decodeChar(int ch) throws IOException {
        if (ch > codes.length) {
             throw new InvalidCharacterException(ch);
         } else {
             int val = codes[ch];
             if (val >= 0) {
                 return val;
             } else {
                 throw new InvalidCharacterException(ch);
             }
         }
     }
 
     final void decode(final int ch, final OutputStream out) throws IOException {
         switch (this.state) {
             case STATE_EOF:
                 break;
             case STATE_START:
                 if (ch == -1) {
                     this.state = STATE_EOF;
                 } else if (ch == '=') {
                     throw new InvalidCharacterException(ch);
                 } else if (!isSpace(ch)) {
                     this.buf = decodeChar(ch);
                     this.state = STATE_SECOND;
                 }
                 break;
             case STATE_SECOND:
                 if (ch == -1) {
                     throw new IncompleteStreamException(ch);
                 } else if (ch == '=') {
                     throw new InvalidCharacterException(ch);
                 } else {
                     int val = decodeChar(ch);
                     this.buf = (this.buf << 6) | val;
                     this.state = STATE_THIRD;
                 }
                 break;
             case STATE_THIRD:
                 if (ch == -1) {
                     throw new IncompleteStreamException(ch);
                 } else if (ch == '=') {
                     this.state = STATE_PADDING;
                 } else {
                     int val = decodeChar(ch);
                     this.buf = (this.buf << 6) | val;
                     this.state = STATE_FOURTH;
                 }
                 break;
             case STATE_PADDING:
                 if (ch == -1) {
                     throw new IncompleteStreamException(ch);
                 } else if (ch == '=') {
                     out.write((this.buf >> 4) & 0xFF);
                     this.state = STATE_START;
                 } else {
                     throw new InvalidCharacterException(ch);
                 }
                 break;
             case STATE_FOURTH:
                 if (ch == -1) {
                     throw new IncompleteStreamException(ch);
                 } else if (ch == '=') {
                     int tmp = this.buf;
                     out.write((tmp >> 10) & 0xFF);
                     out.write((tmp >> 2) & 0xFF);
                     this.state = STATE_START;
                 } else {
                     int val = decodeChar(ch);
                     int tmp = (this.buf << 6) | val;
                     out.write((tmp >> 16) & 0xFF);
                     out.write((tmp >> 8) & 0xFF);
                     out.write((tmp) & 0xFF);
                     this.state = STATE_START;
                 }
                 break;
             default:
                 throw new IllegalStateException("Invalid State " + this.state);
         }
     }
 
     public boolean isEOF() {
         return this.state == STATE_EOF;
     }
 
     public boolean isComplete() {
         return this.state == STATE_START || this.state == STATE_EOF;
     }
 
     public void checkComplete() throws IOException {
         if (!isComplete()) {
             throw new IncompleteStreamException("Incomplete Base64 Stream");
         }
     }
 
     public void decode(char[] in, int offset, int len, OutputStream out) throws IOException {
         int idx = offset;
         while (this.state != STATE_EOF && idx < offset + len) {
             int ch = in[idx];
             decode(ch, out);
             idx++;
         }
     }
 
     public void decode(char[] in, OutputStream out) throws IOException {
         decode(in, 0, in.length, out);
         checkComplete();
     }
 
     public byte[] decode(char[] in, int offset, int len) throws IOException {
         ByteArrayOutputStream out = new ByteArrayOutputStream();
         decode(in, offset, len, out);
         return out.toByteArray();
     }
 
     public byte[] decode(char[] in) throws IOException {
         ByteArrayOutputStream out = new ByteArrayOutputStream();
         decode(in, out);
         checkComplete();
         return out.toByteArray();
     }
 
     public void decode(byte[] in, int offset, int len, OutputStream out) throws IOException {
         int idx = offset;
         while (idx < offset + len && this.state != STATE_EOF) {
             decode(in[idx] & 0xFF, out);
             idx++;
         }
     }
 
     public void decode(byte[] in, OutputStream out) throws IOException {
         decode(in, 0, in.length, out);
         checkComplete();
     }
 
     public byte[] decode(byte[] bytes) throws IOException {
         ByteArrayOutputStream out = new ByteArrayOutputStream();
         decode(bytes, 0, bytes.length, out);
         checkComplete();
         return out.toByteArray();
     }
 
     public void decode(InputStream in, OutputStream out) throws IOException {
         int ch;
         while ((ch = in.read()) != -1 && this.state != STATE_EOF) {
             decode(ch, out);
         }
         checkComplete();
     }
 
     public void decode(InputStream in, OutputStream out, int bufferSize) throws IOException {
         byte[] buffer = new byte[bufferSize];
         int len;
         while ((len = in.read(buffer)) != -1) {
             decode(buffer, 0, len, out);
         }
         checkComplete();
     }
 
     public void decode(Reader in, OutputStream out) throws IOException {
         int ch;
         while ((ch = in.read()) != -1 && this.state != STATE_EOF) {
             decode(ch, out);
         }
         checkComplete();
     }
 
     public void decode(Reader in, OutputStream out, int bufferSize) throws IOException {
         char[] buffer = new char[bufferSize];
         int len;
         while ((len = in.read(buffer)) != -1) {
             decode(buffer, 0, len, out);
         }
         checkComplete();
     }
 
     public void decode(CharSequence str, OutputStream out) throws IOException {
         int len = str.length();
         int idx = 0;
         while (idx < len && this.state != STATE_EOF) {
             decode(str.charAt(idx), out);
             idx++;
         }
         checkComplete();
     }
 
     public byte[] decode(CharSequence str) throws IOException {
         ByteArrayOutputStream out = new ByteArrayOutputStream();
         decode(str, out);
         return out.toByteArray();
     }
 
     public void decode(File inFile, File outFile) throws IOException {
         decode(inFile, outFile, 32 * 1024);
     }
 
     public void decode(File inFile, File outFile, int bufferSize) throws IOException {
         InputStream in = new BufferedInputStream(new FileInputStream(inFile), bufferSize);
         try {
             OutputStream out = new BufferedOutputStream(new FileOutputStream(outFile), bufferSize);
             try {
                 decode(in, out, bufferSize);
             } finally {
                 out.close();
             }
         } finally {
             in.close();
         }
     }
 }
