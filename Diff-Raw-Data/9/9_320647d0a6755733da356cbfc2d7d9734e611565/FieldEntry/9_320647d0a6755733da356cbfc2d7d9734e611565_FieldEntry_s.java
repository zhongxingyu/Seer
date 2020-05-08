 package sword.java.class_analyzer.pool;
 
 import java.io.IOException;
 import java.io.InputStream;
 
 import sword.java.class_analyzer.FileError;
 
 public class FieldEntry extends AbstractMemberEntry {
 
    public FieldEntry(InputStream inStream) throws IOException, FileError {
         super(inStream);
     }
 }
