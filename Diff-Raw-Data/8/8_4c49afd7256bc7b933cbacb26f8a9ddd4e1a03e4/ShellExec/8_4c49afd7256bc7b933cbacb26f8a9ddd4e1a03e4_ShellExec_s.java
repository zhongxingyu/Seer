 package hu.juranyi.zsolt.heritrixremote.communication;
 
 import java.io.IOException;
 
 /**
  * Communication with the OS shell: command execution and output fetching.
  *
  * @author Zsolt Jurányi
  */
 public class ShellExec {
 
     private final String command;
     private String stdout;
     private String stderr;
     private int exitCode;
 
     public ShellExec(String command) {
         this.command = command;
     }
 
     public void exec() throws IOException, InterruptedException {
        // Thanks to: http://www.javaworld.com/javaworld/jw-12-2000/jw-1229-traps.html?page=4
         Process proc = Runtime.getRuntime().exec(command);
 
         StreamGobbler errorGobbler = new StreamGobbler(proc.getErrorStream());
         StreamGobbler outputGobbler = new StreamGobbler(proc.getInputStream());
         errorGobbler.start();
         outputGobbler.start();
 
         exitCode = proc.waitFor();
 
         stderr = errorGobbler.getStreamAsString();
         stdout = outputGobbler.getStreamAsString();
     }
 
     public String getCommand() {
         return command;
     }
 
     public String getStdout() {
         return stdout;
     }
 
     public String getStderr() {
         return stderr;
     }
 
     public int getExitCode() {
         return exitCode;
     }
 }
