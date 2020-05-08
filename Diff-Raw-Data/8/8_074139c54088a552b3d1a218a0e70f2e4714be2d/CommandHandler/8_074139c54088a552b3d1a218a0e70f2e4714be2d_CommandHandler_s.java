 package rel.rogue.ircool;
 
 /**
  *
  * @author Spencer
  */
 public class CommandHandler {
     
     private static java.util.List<CommandExec> commandList = new java.util.ArrayList();
 
     public void setExecutor(CommandExec cmdExec) {
         commandList.remove(cmdExec);
         commandList.add(cmdExec);
     }
     
     public void parseCommand(String command) {
         command = command.substring(1);
         String[] temp = command.split(" ");
         String comm = temp[0].replace("/", " ");
         String s = "";
         for (int i=1; i<temp.length; i++) {
             s = s + temp[i] + " ";
         }
         temp = s.split(" ");
         sendCommand(comm, temp);
     }
     
     public void sendCommand(String command, String[] args) {
          CommandExec exec = getExecutor(command);
          
          if (exec == null) {
              return;
          }
          
         if (args[0].equals("")) {
             Utils.printAction("§current", "", exec.getUsage());
              return;
          }
          
          exec.onCommand(args);
          
     }
     
     public CommandExec getExecutor(String command) {
         command = command.trim();
         for (CommandExec exec : this.commandList) {
             for (String trigs : exec.getTriggers()) {
                 trigs = trigs.trim();
                 if (trigs.equalsIgnoreCase(command)) {
                     return exec;
                 }
             }
         }
         return null;
     }
 }
  
