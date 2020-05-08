 package hialin.lib;
 
 import java.nio.ByteBuffer;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.IOException;
 import java.lang.Math;
 
 public class NioUtils {
 
     /** returns an input stream reading from this buffer; DANGEROUS when used asynchronously */
     public static InputStream asInputStream(final ByteBuffer buf) {
         buf.rewind();
         return new InputStream() {
             public synchronized int read() throws IOException {
                 if (!buf.hasRemaining()) {
                     return -1;
                 }
                 return buf.get() & 0xFF;
             }
 
             public synchronized int read(byte[] bytes, int off, int len) throws IOException {
                 if (!buf.hasRemaining()) {
                     return -1;
                 }
 
                 len = Math.min(len, buf.remaining());
                 buf.get(bytes, off, len);
                 return len;
             }
         };
     }
 
     /** returns an input stream reading from this buffer; DANGEROUS when used asynchronously */
     public static OutputStream asOutputStream(final ByteBuffer buf) {
         buf.clear();
         return new OutputStream() {
             public synchronized void write(int b) throws IOException {
                 buf.put((byte) b);
             }
 
             public synchronized void write(byte[] bytes, int off, int len) throws IOException {
                 buf.put(bytes, off, len);
             }
         };
     }
 
 
 }
