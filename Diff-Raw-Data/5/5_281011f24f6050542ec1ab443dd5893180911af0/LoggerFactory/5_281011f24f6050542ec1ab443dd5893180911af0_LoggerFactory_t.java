 package http.server.logger;
 
 import java.io.File;
 import java.io.IOException;
 
 public class LoggerFactory {
   public static Logger build(String env, File workingDirectoryFullPath) throws IOException {
     if (env.equals("production"))
         return new ConsoleLogger();
     else {
      return new FileLogger(workingDirectoryFullPath);
     }
   }
 }
