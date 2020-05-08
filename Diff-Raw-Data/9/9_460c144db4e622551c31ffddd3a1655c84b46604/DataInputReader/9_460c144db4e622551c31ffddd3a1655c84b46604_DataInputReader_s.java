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
 
 import java.io.DataInput;
 import java.io.DataInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.RandomAccessFile;
 import java.math.BigInteger;
 import java.nio.ByteBuffer;
 import org.apache.commons.io.EndianUtils;
 import org.apache.commons.lang3.ArrayUtils;
 
 /**
  * DataInput extension for more data access methods.
  * 
  * @author Nico Bergemann <barracuda415 at yahoo.de>
  */
 public class DataInputReader extends DataInputWrapper implements DataInputExtended {
     
     private static final String DEFAULT_CHARSET = "ASCII";
     private boolean swap;
     
     public DataInputReader(DataInput in) {
         super(in);
     }
     
     public DataInputReader(RandomAccessFile raf) {
         super(new RandomAccessFileWrapper(raf));
     }
     
     public DataInputReader(InputStream is) {
         super(new DataInputStream(is));
     }
     
     public DataInputReader(ByteBuffer bb) {
         super(new ByteBufferInput(bb));
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
     public short readShort() throws IOException {
         short r = super.readShort();
         if (swap) {
             r = EndianUtils.swapShort(r);
         }
         return r;
     }
     
     @Override
     public int readUnsignedShort() throws IOException {
         int r = super.readUnsignedShort();
         if (swap) {
            r = EndianUtils.swapInteger(r);
         }
         return r;
     }
 
     @Override
     public int readInt() throws IOException {
         int r = super.readInt();
         if (swap) {
             r = EndianUtils.swapInteger(r);
         }
         return r;
     }
     
     @Override
     public long readLong() throws IOException {
         long r = super.readLong();
         if (swap) {
             r = EndianUtils.swapLong(r);
         }
         return r;
     }
     
     @Override
     public float readFloat() throws IOException {
         if (swap) {
             // NOTE: don't use readFloat() plus EndianUtils.swapFloat() here!
             return Float.intBitsToFloat(readInt());
         } else {
             return super.readFloat();
         }
     }
     
     @Override
     public double readDouble() throws IOException {
         if (swap) {
             // NOTE: don't use readDouble() plus EndianUtils.swapDouble() here!
             return Double.longBitsToDouble(readLong());
         } else {
             return super.readDouble();
         }
     }
 
     @Override
     public long readUnsignedInt() throws IOException {
         return readInt() & 0xffffffffL;
     }
     
     @Override
     public BigInteger readUnsignedLong() throws IOException {
         byte[] raw = new byte[8];
         readFully(raw);
         if (swap) {
             ArrayUtils.reverse(raw);
         }
         return new BigInteger(raw);
     }
     
     @Override
     public float readHalf() throws IOException {
         int hbits = readUnsignedShort();
         return HalfFloat.intBitsToFloat(hbits);
     }
     
     @Override
     public String readStringNull(int limit, String charset, boolean padded) throws IOException {
         if (limit <= 0) {
             throw new IllegalArgumentException("Invalid limit");
         }
         
         // read raw byte array until the size is equal to limit or the byte is null
         byte[] raw = new byte[limit];
         int length = 0;
         for (byte b; length < raw.length && (b = readByte()) != 0; length++) {
             raw[length] = b;
         }
         
         // skip padding bytes
         if (padded) {
             skipBytes(limit - length - 1);
         }
         
         return new String(raw, 0, length, charset);
     }
     
     @Override
     public String readStringNull(int limit, String charset) throws IOException {
         return readStringNull(limit, charset, false);
     }
     
     @Override
     public String readStringNull(int limit) throws IOException {
         return readStringNull(limit, DEFAULT_CHARSET);
     }
 
     @Override
     public String readStringNull() throws IOException {
         return readStringNull(256);
     }
     
     @Override
     public String readStringFixed(int length, String charset) throws IOException {
         byte[] raw = new byte[length];
         readFully(raw);
         return new String(raw, charset);
     }
     
     @Override
     public String readStringFixed(int length) throws IOException {
         return readStringFixed(length, DEFAULT_CHARSET);
     }
     
     @Override
     public String readStringInt(int limit, String charset, int align) throws IOException {
         int length = readInt();
         if (limit > 0 && length > limit) {
             return null;
         }
         
         String str = readStringFixed(length, charset);
         align(length, align);
         
         return str;
     }
     
     @Override
     public String readStringInt(int limit, String charset) throws IOException {
         return readStringInt(limit, charset, 0);
     }
     
     @Override
     public String readStringInt(int limit) throws IOException {
         return readStringInt(limit, DEFAULT_CHARSET);
     }
     
     @Override
     public String readStringInt() throws IOException {
         return readStringInt(0);
     }
     
     @Override
     public String readStringShort(int limit, String charset, int align) throws IOException {
         int length = readUnsignedShort();
         if (limit > 0 && length > limit) {
             return null;
         }
         
         String str = readStringFixed(length, charset);
         align(length, align);
         
         return str;
     }
     
     @Override
     public String readStringShort(int limit, String charset) throws IOException {
         return readStringShort(limit, charset, 0);
     }
     
     @Override
     public String readStringShort(int limit) throws IOException {
         return readStringShort(limit, DEFAULT_CHARSET);
     }
 
     @Override
     public String readStringShort() throws IOException {
         return readStringShort(0);
     }
     
     @Override
     public String readStringByte(String charset, int align) throws IOException {
         int length = readUnsignedByte();
         
         String str = readStringFixed(length, charset);
         align(length, align);
         
         return str;
     }
     
     @Override
     public String readStringByte(String charset) throws IOException {
         return readStringByte(charset, 0);
     }
     
     @Override
     public String readStringByte() throws IOException {
         return readStringByte(DEFAULT_CHARSET);
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
