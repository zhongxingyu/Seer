 package com.ctp.javaone.archiver.main;
 
 import java.util.List;
 
 import javax.enterprise.context.ApplicationScoped;
 import javax.enterprise.event.Observes;
 import javax.inject.Inject;
 
 import org.jboss.weld.environment.se.bindings.Parameters;
 import org.jboss.weld.environment.se.events.ContainerInitialized;
 
import com.ctp.javaone.archiver.persistence.Auditable;
 import com.ctp.javaone.archiver.plugin.Status;
 import com.ctp.javaone.archiver.shell.Shell;
 import com.ctp.javaone.archiver.shell.ShellColor;
 
 @ApplicationScoped
 public class Archiver {
 
     @SuppressWarnings("unused")
     @Inject @Parameters
     private List<String> vargs;
 
     @Inject
     private CommandExecutor executor;
 
     @Inject
     private Shell shell;
     
     private boolean shutdownRequested = false;
 
     public void archive(@Observes ContainerInitialized init) {
         shell.info(greet());
         while (!shutdownRequested) {
             final String command = shell.readLine(ShellColor.GREEN, ">> ");
             runCommand(command);
         }
     }
 
    @Auditable
     private void runCommand(String command) {
         executor.executeCommand(command);
     }
 
     private String greet() {
         StringBuilder greeting = new StringBuilder();
         greeting.append("Welcome to archiver!\n");
         greeting.append("Run 'list' for a list of all available commands. \n");
         return greeting.toString();
     }
 
     void printCommandResult(@Observes CommandExecutedEvent event) {
         String message = event.getMessage();
         if (event.getStatus() == Status.SUCCESS)
             shell.info(message);
         else
             shell.error(message);
     }
 
     void terminate(@Observes ExitEvent event) {
         shutdownRequested = true;
         shell.info(event.getReason());
     }
 
 }
