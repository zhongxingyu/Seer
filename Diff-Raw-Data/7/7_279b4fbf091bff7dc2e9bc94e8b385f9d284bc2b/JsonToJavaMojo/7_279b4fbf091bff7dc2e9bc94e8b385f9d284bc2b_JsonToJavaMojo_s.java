 package com.denarced.jsontojava.plugin;
 
 import com.denarced.jsontojava.JavaFileWriter;
 import com.denarced.jsontojava.ClassWriter;
 import com.denarced.jsontojava.JsonToJava;
 import java.io.File;
 import org.apache.maven.plugin.AbstractMojo;
 import org.apache.maven.plugin.MojoExecutionException;
 
 /**
  * Convert json file into Java source files.
  * 
  * @goal generate-sources
  * @phase generate-sources
  */
 public class JsonToJavaMojo extends AbstractMojo {
     /**
      * @parameter default-value="com.example"
      */
     private String targetPackage;
     
     /**
      * @parameter default-value="target/gen"
      */
     private String output;
 
     /**
      * @parameter
      * @required
      */
     private File jsonFile;
 
     public void execute() throws MojoExecutionException {
        if (_jsonFile != null) {
            JavaFileWriter writer = new ClassWriter(_package, _output);
             JsonToJava toJava = new JsonToJava(writer);
            toJava.parse(_jsonFile);
         }
     }
 }
