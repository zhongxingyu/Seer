 package db.migration.app;
 
 import org.jruby.Ruby;
 import org.jruby.javasupport.JavaEmbedUtils;
 import org.jruby.runtime.builtin.IRubyObject;
 import org.jruby.util.KCode;
 import org.springframework.util.FileCopyUtils;
 
 import java.io.*;
 import java.util.Arrays;
 
 /**
  * migrate database using rails and jruby library
  * @author jcai
  * @version 0.1
  */
 public class RailsMigrator {
    private static final int BUFFER_SIZE = 4096;
 
     public static String migrate(String config,String scriptDir) throws IOException {
         // 装载database-migrator ruby文件
         String script = FileCopyUtils.copyToString(
                 new InputStreamReader(RailsMigrator.class.getResourceAsStream("/database-migration.rb"),
                         "UTF-8")
         );
         Ruby ruby = createRubyEnv();
         IRubyObject rubyObject = ruby.evalScriptlet(script);
 
         // 调用ruby里面的方法.
        return JavaEmbedUtils.invokeMethod(ruby, rubyObject, "migrate2",
                 new Object []{config,scriptDir},Object.class).toString();
     }
     private static Ruby createRubyEnv(){
         //构造ruby运行环境
        Ruby ruby = JavaEmbedUtils.initialize(Arrays.asList(
                ".",
                "META-INF/jruby.home/lib/ruby/1.8",
                "META-INF/jruby.home/lib/ruby/site_ruby/1.8"));
        //ruby.evalScriptlet("ENV['GEM_PATH'] = '.'");
         ruby.setKCode(KCode.UTF8);
         return ruby;
     }
     /**
      * Copy the contents of the given Reader to the given Writer.
      * Closes both when done.
      * @param in the Reader to copy from
      * @param out the Writer to copy to
      * @return the number of characters copied
      * @throws IOException in case of I/O errors
      */
     public static int copy(Reader in, Writer out) throws IOException {
         try {
             int byteCount = 0;
             char[] buffer = new char[BUFFER_SIZE];
             int bytesRead = -1;
             while ((bytesRead = in.read(buffer)) != -1) {
                 out.write(buffer, 0, bytesRead);
                 byteCount += bytesRead;
             }
             out.flush();
             return byteCount;
         }
         finally {
             try {
                 in.close();
             }
             catch (IOException ex) {
             }
             try {
                 out.close();
             }
             catch (IOException ex) {
             }
         }
     }
 
     /**
      * Copy the contents of the given Reader into a String.
      * Closes the reader when done.
      * @param in the reader to copy from
      * @return the String that has been copied to
      * @throws IOException in case of I/O errors
      */
     public static String copyToString(Reader in) throws IOException {
         StringWriter out = new StringWriter();
         copy(in, out);
         return out.toString();
     }
 }
