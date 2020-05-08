 package net.jhorstmann.base64;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.nio.ByteBuffer;
 import java.nio.CharBuffer;
 import java.nio.MappedByteBuffer;
 import java.nio.channels.FileChannel;
 
 public class Base64NIODecoder {
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
 
     final void decode(final int ch, final ByteBuffer out) throws IOException {
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
                     out.put((byte)((this.buf >> 4) & 0xFF));
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
                     out.put((byte)((tmp >> 10) & 0xFF));
                     out.put((byte)((tmp >> 2) & 0xFF));
                     this.state = STATE_START;
                 } else {
                     int val = decodeChar(ch);
                     int tmp = (this.buf << 6) | val;
                     out.put((byte)((tmp >> 16) & 0xFF));
                     out.put((byte)((tmp >> 8) & 0xFF));
                     out.put((byte)(tmp & 0xFF));
                     this.state = STATE_START;
                 }
                 break;
             default:
                 throw new IllegalStateException("Invalid State " + this.state);
         }
     }
 
     public boolean isComplete() {
         return this.state == STATE_START || this.state == STATE_EOF;
     }
 
     public void checkComplete() throws IOException {
         if (!isComplete()) {
             throw new IncompleteStreamException("Incomplete Base64 Stream");
         }
     }
 
     public void decode(byte[] in, int offset, int len, ByteBuffer out) throws IOException {
         int idx = offset;
         while (idx < offset + len && this.state != STATE_EOF) {
             decode(in[idx] & 0xFF, out);
             idx++;
         }
     }
 
     public void decode(char[] in, int offset, int len, ByteBuffer out) throws IOException {
         int idx = offset;
         while (this.state != STATE_EOF && idx < offset + len) {
             int ch = in[idx];
             decode(ch, out);
             idx++;
         }
     }
 
     void decodeImpl(CharBuffer in, ByteBuffer out) throws IOException {
         int ch;
         while (in.hasRemaining() && this.state != STATE_EOF) {
             ch = in.get();
             decode(ch, out);
         }
     }
 
     public void decode(CharBuffer in, ByteBuffer out) throws IOException {
         if (in.hasArray()) {
             int offset = in.arrayOffset();
             int length = in.limit()-offset;
             decode(in.array(), offset, length, out);
         }
         else {
             decodeImpl(in, out);
         }
     }
 
     void decodeImpl(ByteBuffer in, ByteBuffer out) throws IOException {
         int ch;
         while (in.hasRemaining() && this.state != STATE_EOF) {
             ch = in.get();
             decode(ch, out);
         }
     }
 
     public void decode(ByteBuffer in, ByteBuffer out) throws IOException {
         if (in.hasArray()) {
             int offset = in.arrayOffset();
             int length = in.limit()-offset;
             decode(in.array(), offset, length, out);
         }
         else {
             decodeImpl(in, out);
         }
     }
 
     public void decode(FileChannel in, ByteBuffer byteBuffer, ByteBuffer out) throws IOException {
         byteBuffer.rewind();
         while (-1 != in.read(byteBuffer) && this.state != STATE_EOF) {
             byteBuffer.flip();
             decode(byteBuffer, out);
         }
     }
 
     public void decode(File inFile, File outFile, int bufferSize, boolean directIn, boolean directOut) throws IOException {
         FileChannel in = new FileInputStream(inFile).getChannel();
         try {
             FileChannel out = new FileOutputStream(outFile).getChannel();
             try {
                 ByteBuffer inBuffer  = directIn  ? ByteBuffer.allocateDirect(bufferSize) : ByteBuffer.allocate(bufferSize);
                 ByteBuffer outBuffer = directOut ? ByteBuffer.allocateDirect(bufferSize) : ByteBuffer.allocate(bufferSize);
                 while (-1 != in.read(inBuffer) && this.state != -1) {
                     inBuffer.flip();
                     //System.out.println(inBuffer.position() + " " + inBuffer.limit() + " " + outBuffer.position() + " " + outBuffer.limit());
                     decodeImpl(inBuffer, outBuffer);
                     inBuffer.clear();
 
                     outBuffer.flip();
                     //System.out.println(outBuffer.position() + " " + outBuffer.limit());
                     out.write(outBuffer);
                     outBuffer.clear();
                     //System.out.println(outBuffer.position() + " " + outBuffer.limit());
                 }
             }
             finally {
                 out.close();
             }
         }
         finally {
             in.close();
         }
     }
 
     public void decodeMapped(File inFile, File outFile, int bufferSize, boolean directOut) throws IOException {
         FileChannel in = new FileInputStream(inFile).getChannel();
         try {
             FileChannel out = new FileOutputStream(outFile).getChannel();
             try {
                 int size = (int)in.size();
                 MappedByteBuffer inBuffer = in.map(FileChannel.MapMode.READ_ONLY, 0, size);
                 ByteBuffer outBuffer = directOut ? ByteBuffer.allocateDirect(bufferSize) : ByteBuffer.allocate(bufferSize);
                 int limit = bufferSize;
                 while (inBuffer.position() < size) {
                     //System.out.println(inBuffer.position() + " " + inBuffer.limit() + " " + outBuffer.position() + " " + outBuffer.limit());
                     inBuffer.limit(Math.min(limit, size));
                     decodeImpl(inBuffer, outBuffer);
                     outBuffer.flip();
                     out.write(outBuffer);
                     outBuffer.clear();
                     limit += bufferSize;
                 }
             }
             finally {
                 out.close();
             }
         }
         finally {
             in.close();
         }
 
     }
 
 }
