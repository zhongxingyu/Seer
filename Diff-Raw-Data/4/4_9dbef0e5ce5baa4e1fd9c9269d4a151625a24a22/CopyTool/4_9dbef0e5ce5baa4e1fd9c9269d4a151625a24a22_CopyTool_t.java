 package de.skuzzle.polly.tools.streams;
 
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 
 
 public class CopyTool {
 
     @SuppressWarnings("unchecked")
     public static <T> T copyOf(T root) {
         FastByteArrayOutputStream buffer = new FastByteArrayOutputStream();
         try {
             new ObjectOutputStream(buffer).writeObject(root);
             return (T) new ObjectInputStream(
                     new FastByteArrayInputStream(buffer)).readObject();
         } catch (Exception e) {
             throw new RuntimeException("clone fail", e);
        } finally {
            if (buffer != null) {
                buffer.close();
            }
         }
     }
     
     
     
     private CopyTool() {}
 }
