 package net.bodz.bas.sio;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.nio.ByteBuffer;
 
 public abstract class AbstractByteIn
         implements IByteIn {
 
     @Override
     public int read(byte[] buf)
             throws IOException {
         return read(buf, 0, buf.length);
     }
 
     /**
      * XXX - Please check in more detail.
      */
     @Override
     public int read(ByteBuffer buffer)
             throws IOException {
         int cbRead = 0;
         while (buffer.hasRemaining()) {
             int b = read();
             if (b == -1)
                 return cbRead == 0 ? -1 : cbRead;
             buffer.put((byte) b);
            cbRead++;
         }
         return cbRead;
     }
 
     @Override
     public void close()
             throws IOException {
     }
 
     public InputStream toInputStream() {
         return new ByteInInputStream(this);
     }
 
 }
