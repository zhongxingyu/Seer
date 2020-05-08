 /*
  ** 2013 June 15
  **
  ** The author disclaims copyright to this source code.  In place of
  ** a legal notice, here is a blessing:
  **    May you do good and not evil.
  **    May you find forgiveness for yourself and forgive others.
  **    May you share freely, never taking more than you give.
  */
 package info.ata4.util.io;
 
 import java.io.DataOutput;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.io.RandomAccessFile;
 import java.nio.ByteBuffer;
 import org.apache.commons.io.EndianUtils;
 
 /**
  * DataOutput extension for more data access methods.
  * 
  * @author Nico Bergemann <barracuda415 at yahoo.de>
  */
 public class DataOutputWriter extends DataOutputWrapper implements DataOutputExtended {
     
     private static final String DEFAULT_CHARSET = "ASCII";
     private boolean swap;
     
     public DataOutputWriter(DataOutput out) {
         super(out);
     }
     
     public DataOutputWriter(RandomAccessFile raf) {
         super(new RandomAccessFileWrapper(raf));
     }
     
     public DataOutputWriter(OutputStream is) {
         super(new DataOutputStream(is));
     }
     
     public DataOutputWriter(ByteBuffer bb) {
         super(new ByteBufferOutput(bb));
     }
     
     @Override
     public boolean isSwap() {
         if (super.isSwappable()) {
             return super.isSwap();
         } else {
             return swap;
         }
     }
 
     @Override
     public void setSwap(boolean swap) {
         if (super.isSwappable()) {
             super.setSwap(swap);
         } else {
             this.swap = swap;
         }
     }
 
     @Override
     public boolean isSwappable() {
         // supports manual swapping using EndianUtils if required
         return true;
     }
     
     @Override
     public void writeShort(int v) throws IOException {
         if (swap) {
            v = EndianUtils.swapInteger(v);
         }
         super.writeShort(v);
     }
     
     @Override
     public void writeInt(int v) throws IOException {
         if (swap) {
             v = EndianUtils.swapInteger(v);
         }
         super.writeInt(v);
     }
     
     @Override
     public void writeFloat(float v) throws IOException {
         if (swap) {
             // NOTE: don't use writeFloat() plus EndianUtils.swapFloat() here!
             writeInt(Float.floatToRawIntBits(v));
         } else {
             super.writeFloat(v);
         }
     }
     
     @Override
     public void writeDouble(double v) throws IOException {
         if (swap) {
             // NOTE: don't use writeDouble() plus EndianUtils.swapDouble() here!
             writeLong(Double.doubleToRawLongBits(v));
         } else {
             super.writeDouble(v);
         }
     }
 
     @Override
     public void writeLong(long v) throws IOException {
         if (swap) {
             v = EndianUtils.swapLong(v);
         }
         super.writeLong(v);
     }
     
     @Override
     public void write(ByteBuffer bb) throws IOException {
         DataOutput out = getWrapped();
         if (out instanceof ByteBufferOutput) {
             // write natively
             ((ByteBufferOutput) out).write(bb);
         } else if (bb.hasArray()) {
             // write as byte array
             write(bb.array());
         } else {
             // write byte by byte
             while (bb.hasRemaining()) {
                 write(bb.get());
             }
         }
     }
     
     @Override
     public void writeHalf(float f) throws IOException {
         int sval = HalfFloat.floatToIntBits(f);
         writeShort(sval);
     }
     
     @Override
     public void writeStringFixed(String str, String charset) throws IOException {
         write(str.getBytes(charset));
     }
     
     @Override
     public void writeStringFixed(String str) throws IOException {
         writeStringFixed(str, DEFAULT_CHARSET);
     }
 
     @Override
     public void writeStringNull(String str, String charset) throws IOException {
         writeStringFixed(str, charset);
         writeByte(0);
     }
     
     @Override
     public void writeStringNull(String str) throws IOException {
         writeStringNull(str, DEFAULT_CHARSET);
     }
     
     @Override
     public void skipBytes(int n) throws IOException {
         write(new byte[n]);
     }
 
     @Override
     public void align(int length, int align) throws IOException {
         if (align > 0) {
             int rem = length % align;
             if (rem != 0) {
                 skipBytes(align - rem);
             }
         }
     }
 }
