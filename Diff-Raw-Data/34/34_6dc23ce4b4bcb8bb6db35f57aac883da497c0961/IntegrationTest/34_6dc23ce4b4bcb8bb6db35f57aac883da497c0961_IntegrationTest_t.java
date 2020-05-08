 package com.google.protobuf.java;
 
 import com.google.protobuf.Message;
 import japa.parser.JavaParser;
 import junit.framework.TestCase;
 
 import java.io.*;
 
 import static java.io.File.separator;
 
 /**
  * @author: alexeagle@google.com (Alex Eagle)
  */
 public class IntegrationTest extends TestCase {
   File filesystem;
   File srcProto;
   File pluginExecutable;
 
   @Override
   protected void setUp() throws Exception {
     super.setUp();
     filesystem = new File(System.getProperty("java.io.tmpdir")
         + separator + getName());
     new File(filesystem, "src/com/google/testing").mkdirs();
     srcProto = new File(filesystem, "src/sample.proto");
     pluginExecutable = new File(filesystem, "protoc-gen-richjava");
     pluginExecutable.createNewFile();
     assertTrue(pluginExecutable.setExecutable(true));
 
     writeToFile(pluginExecutable, String.format(
         "#!/bin/sh\n" +
         "java -classpath %s:%s:%s %s",
         getPath(Message.class), getPath(JavaParser.class),
         getPath(JavaExtensionPlugin.class),
         JavaExtensionPlugin.class.getCanonicalName()));
   }
 
   public void testShouldAugmentJavaProto() throws Exception {
     writeToFile(srcProto,
        "package example;\n" +
         "message User {\n" +
         "  required string name = 1;\n" +
         "}");
 
     Process process = new ProcessBuilder(
         "/usr/local/bin/protoc", "--java_out=src", "src/sample.proto",
         "--plugin=protoc-gen-richjava=" + pluginExecutable.getAbsolutePath(),
         "--richjava_out=src")
         .directory(filesystem)
         .redirectErrorStream(true)
         .start();
 
     int exitCode = process.waitFor();
     if (exitCode != 0) {
       fail(String.format("protoc returned %d\n%s", exitCode,
           readAll(new InputStreamReader(process.getInputStream()))));
     }
    File generatedJava = new File(filesystem, "src/example/Sample.java");
     assertTrue("Java file should be created", generatedJava.exists());
     assertTrue("Method should be inlined",
         readAll(new FileReader(generatedJava)).contains("IsAwesome"));
   }
 
   private void writeToFile(File proto, String contents) throws IOException {
     FileWriter writer = new FileWriter(proto);
     writer.write(contents);
     writer.close();
   }
 
   private String readAll(Reader reader) throws IOException {
     StringWriter writer = new StringWriter();
     PrintWriter printWriter = new PrintWriter(writer);
     BufferedReader bufferedReader = new BufferedReader(reader);
     while(bufferedReader.ready()) {
       printWriter.println(bufferedReader.readLine());
     }
     return writer.toString();
   }
 
   private String getPath(Class clazz) {
     String className = clazz.getName().replace('.', '/') + ".class";
     String path = getClass().getClassLoader().getResource(className).getPath().replaceFirst("file:", "");
     int jarDelimiter = path.indexOf("!");
     if (jarDelimiter >= 0) {
       return path.substring(0, jarDelimiter);
     } else {
       int packageStart = path.indexOf(clazz.getPackage().getName().replace('.', '/'));
       if (packageStart >= 0) {
         return path.substring(0, packageStart);
       } else {
         return path;
       }
     }
   }
 }
