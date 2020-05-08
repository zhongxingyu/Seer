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
 
    private ByteArrayOutputStream output = new ByteArrayOutputStream();
 
     public RamFile(RamDirectory parent, String name) {
         super(parent, name);
     }
 
     
     public InputStream getInputStream() throws IOException {
        return new ByteArrayInputStream(output.toByteArray());
     }
 
     public OutputStream getOutputStream() throws IOException {
        return output;
     }
 
     public long getLength() throws IOException {
        return output.size();
     }
 
 }
