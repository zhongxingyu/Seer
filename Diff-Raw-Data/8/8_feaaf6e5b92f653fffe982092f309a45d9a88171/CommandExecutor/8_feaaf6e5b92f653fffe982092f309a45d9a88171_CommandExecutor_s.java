 package fitnesse.wiki.cmSystems;
 import fitnesse.components.CommandRunner;
 
 public class CommandExecutor {
   public CommandRunner exec(String command) {
	System.err.println(command);
     CommandRunner runner = new CommandRunner(command, "");
     try {
       runner.run();
       if (runner.getOutput().length() + runner.getError().length() > 0) {
        System.err.println("command: " + command);
        System.err.println("exit code: " + runner.getExitCode());
        System.err.println("out:" + runner.getOutput());
         System.err.println("err:" + runner.getError());
       }
     } catch (Exception e) {
       throw new RuntimeException(e);
     }
     return runner;
   }
 
 public void voidExec(String command) {
 	// TODO Auto-generated method stub
 	this.exec(command);
 }
 }
