 package org.flowdev.base.op.io;
 
 import org.flowdev.base.Getter;
 import org.flowdev.base.Setter;
 import org.flowdev.base.data.EmptyConfig;
 import org.flowdev.base.op.Transform;
 
 import java.io.IOException;
 import java.nio.charset.Charset;
 import java.nio.file.Files;
 import java.nio.file.Path;
 import java.nio.file.Paths;
 
 /**
  * This operation reads the content of a file as a UTF-8 text into a string.
  */
 public class ReadTextFile<T, U> extends Transform<T, U, EmptyConfig> {
     public static class Params<T, U> {
         public Getter<T, String> getFileName;
         public Setter<String, T, U> setFileContent;
     }
 
     public static final Charset UTF8 = Charset.forName("UTF-8");
 
     private final Params<T, U> params;
 
     public ReadTextFile(Params<T, U> params) {
         this.params = params;
     }
 
     protected void transform(T data) throws IOException {
         String fileName = params.getFileName.get(data);
 
         Path path = Paths.get(fileName);
         byte[] buf = Files.readAllBytes(path);
         String content = new String(buf, UTF8);
 
        U ret = params.setFileContent.set(data, content);
         outPort.send(ret);
     }
 }
