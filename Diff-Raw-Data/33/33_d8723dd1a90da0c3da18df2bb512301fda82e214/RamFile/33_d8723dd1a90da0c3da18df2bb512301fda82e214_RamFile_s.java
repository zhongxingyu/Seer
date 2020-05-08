 package dk.apaq.vfs.impl.ram;
 
 import dk.apaq.vfs.File;
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 
 /**
  *
  */
 public class RamFile extends RamNode implements File {
 
    private byte[] buffer=new byte[0];


    private class BufferUpdatingOutputStream extends OutputStream {

        private final ByteArrayOutputStream wrapped;

        public BufferUpdatingOutputStream(ByteArrayOutputStream wrapped) {
            this.wrapped = wrapped;
        }

       @Override
        public void write(int b) throws IOException {
            wrapped.write(b);
        }

        @Override
        public void close() throws IOException {
            super.close();
            buffer = wrapped.toByteArray();
        }

    }
 
     public RamFile(RamDirectory parent, String name) {
         super(parent, name);
     }
 
     
     public InputStream getInputStream() throws IOException {
        return new ByteArrayInputStream(buffer);
     }
 
     public OutputStream getOutputStream() throws IOException {
        return new BufferUpdatingOutputStream(new ByteArrayOutputStream());
     }
 
     public long getLength() throws IOException {
        return buffer.length;
     }
 
 }
