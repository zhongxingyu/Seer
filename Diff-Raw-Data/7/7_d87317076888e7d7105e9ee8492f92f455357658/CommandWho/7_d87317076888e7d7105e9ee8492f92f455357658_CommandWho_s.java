 package cybermoo.ChatCommands;
 
 import cybermoo.Server;
 import cybermoo.ThreadedClient;
 
 public class CommandWho implements Command {
 
     public void call(String[] arguments, ThreadedClient source) {
        Server.getInstance().sendToClient(source, "The following " + Server.getInstance().getClients().size() + " user(s) are connected:");
         for (ThreadedClient c : Server.getInstance().getClients()) {
            Server.getInstance().sendToClient(source, c.getPlayer().getName());
         }
     }
 
     public Boolean isCleared(ThreadedClient source) {
         return true;
     }
 }
