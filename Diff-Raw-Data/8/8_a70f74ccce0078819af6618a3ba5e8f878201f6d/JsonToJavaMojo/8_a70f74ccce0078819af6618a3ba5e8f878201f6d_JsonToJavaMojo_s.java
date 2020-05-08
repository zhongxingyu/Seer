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
  * @goal generate
  * @phase generate-sources
  */
 public class JsonToJavaMojo extends AbstractMojo {
     /**
      * @parameter
     *   expression="${package}"
      *   default-value="com.example"
      */
     private String _package;
     
     /**
      * @parameter
     *   expression="${output}"
      *   default-value="target/gen"
      */
     private String _output;
 
     /**
      * @parameter
     *   expression="${filepath}"
      */
     private File _jsonFile;
 
     public void execute() throws MojoExecutionException {
         if (_jsonFile != null) {
             JavaFileWriter writer = new ClassWriter(_package, _output);
             JsonToJava toJava = new JsonToJava(writer);
             toJava.parse(_jsonFile);
         }
     }
 }
