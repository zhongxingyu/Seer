 package littlegruz.arpeegee.commands;
 
 import littlegruz.arpeegee.ArpeegeeMain;
 
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 
 public class Join implements CommandExecutor{
    private ArpeegeeMain plugin;
    
    public Join(ArpeegeeMain instance){
       plugin = instance;
    }
 
    @Override
    public boolean onCommand(CommandSender sender, Command cmd,
          String commandLabel, String[] args){
       if(sender.hasPermission("arpeegee.text")){
          if(cmd.getName().compareToIgnoreCase("setrpgintro") == 0){
            if(args.length < 1){
                String msg;
                
                msg = args[0];
                for(int i = 1; i < args.length; i++)
                   msg += " " + args[i];
                
                plugin.getTextsMap().put("intro", msg);
                
                sender.sendMessage("Introductory message set");
             }
             else
                sender.sendMessage("Wrong number of arguments");
          }
          else if(cmd.getName().compareToIgnoreCase("setrpgreturn") == 0){
            if(args.length < 1){
                String msg;
                
                msg = args[0];
                for(int i = 1; i < args.length; i++)
                   msg += " " + args[i];
                
                plugin.getTextsMap().put("return", msg);
                
                sender.sendMessage("Returning player message set");
             }
             else
                sender.sendMessage("Wrong number of arguments");
          }
       }
       else
          sender.sendMessage("You do not have sufficient permissions");
       return true;
    }
 
 }
