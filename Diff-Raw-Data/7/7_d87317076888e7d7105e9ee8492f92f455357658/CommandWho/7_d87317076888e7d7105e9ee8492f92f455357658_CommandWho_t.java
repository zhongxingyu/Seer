 package cybermoo.ChatCommands;
 
 import cybermoo.Server;
 import cybermoo.ThreadedClient;
 
 public class CommandWho implements Command {
 
     public void call(String[] arguments, ThreadedClient source) {
        Server.getInstance().sendToClient(source, Server.getInstance().getClients().size() + " user(s) are connected, the following are logged in:");
         for (ThreadedClient c : Server.getInstance().getClients()) {
            if (c.getPlayer() != null) {
                Server.getInstance().sendToClient(source, c.getPlayer().getName());
            }
         }
     }
 
     public Boolean isCleared(ThreadedClient source) {
         return true;
     }
 }
