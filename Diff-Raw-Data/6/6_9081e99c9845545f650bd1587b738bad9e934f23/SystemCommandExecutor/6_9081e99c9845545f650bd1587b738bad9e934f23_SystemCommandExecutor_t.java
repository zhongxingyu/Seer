 package com.frugs.filesync.local.system;
 
 import org.apache.commons.io.IOUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.io.IOException;
 import java.io.InputStream;
 
 public class SystemCommandExecutor {
 
     private final Logger logger;
 
     private static final Runtime currentRuntime = Runtime.getRuntime();
 
     public SystemCommandExecutor() {
         this.logger = LoggerFactory.getLogger(SystemCommandExecutor.class);
     }
 
     public InputStream gitDiffHead() throws IOException {
         logger.debug("performing git diff head");
         return execute("git --no-pager diff HEAD");
     }
 
     public InputStream interDiff(String diffFile1, String diffFile2) throws IOException {
         logger.debug("performing interdiff");
         return execute("interdiff " + diffFile1 + " " + diffFile2);
     }
 
     public void gitApply(String diffFile) throws IOException {
         logger.debug("applying diff");
         execute("patch -p1 -i " + diffFile);
     }
 
     private InputStream execute(String command) throws IOException {
         Process process = currentRuntime.exec(command);
        InputStream errorStream = process.getErrorStream();
        if (errorStream.available() > 0) {
            throw new RuntimeException(IOUtils.toString(errorStream));
         }
         return process.getInputStream();
     }
 }
