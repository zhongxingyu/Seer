 package nl.lolmewn.sortal;
 
import java.util.logging.Level;
import org.bukkit.Bukkit;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 /**
  *
  * @author Lolmewn
  */
 class SortalExecutor implements CommandExecutor {
 
     private Main plugin;
     
     public SortalExecutor(Main aThis) {
         this.plugin = aThis;
     }
     
     private Main getPlugin(){
         return this.plugin;
     }
     
     private Localisation getLocalisation(){
         return this.getPlugin().getSettings().getLocalisation();
     }
 
     public boolean onCommand(CommandSender sender, Command cmnd, String string, String[] args) {
         if(args.length == 0){
             sender.sendMessage("===Sortal===");
             sender.sendMessage("Made by Lolmewn");
             sender.sendMessage("For help: /sortal help");
             return true;
         }
         if(args[0].equalsIgnoreCase("warp") || args[0].equalsIgnoreCase("setwarp")){
             if(!sender.hasPermission("sortal.createwarp")){
                 sender.sendMessage(this.getLocalisation().getNoPerms());
                 return true;
             }
             if(!(sender instanceof Player)){
                 sender.sendMessage(this.getLocalisation().getNoPlayer());
                 return true;
             }
             if(args.length == 1){
                 sender.sendMessage(this.getLocalisation().getCreateNameForgot());
                 return true;
             }
             if(args.length == 2){
                 //Just creating a warp
                 String warp = args[1];
                 if(this.getPlugin().getWarpManager().hasWarp(warp)){
                     sender.sendMessage(this.getLocalisation().getNameInUse(warp));
                     return true;
                 }
                 this.getPlugin().getWarpManager().addWarp(warp, ((Player)sender).getLocation());
                 sender.sendMessage(this.getLocalisation().getWarpCreated(warp));
                 return true;
             }
             sender.sendMessage("Too many arguments! Correct usage: /sortal " + args[0] + " " + args[1]);
             return true;
         }
         if(args[0].equalsIgnoreCase("delwarp") || args[0].equalsIgnoreCase("delete")){
             if(!sender.hasPermission("sortal.delwarp")){
                 sender.sendMessage(this.getLocalisation().getNoPerms());
                 return true;
             }
             if(args.length == 1){
                 sender.sendMessage(this.getLocalisation().getDeleteNameForgot());
                 return true;
             }
             for(int i = 1; i < args.length; i++){
                 String warp = args[i];
                 if(this.getPlugin().getWarpManager().hasWarp(warp)){
                     this.getPlugin().getWarpManager().removeWarp(warp);
                     sender.sendMessage(this.getLocalisation().getWarpDeleted(warp));
                     continue;
                 }
                 sender.sendMessage(this.getLocalisation().getWarpNotFound(warp));
             }
             return true;
         }
         if(args[0].equalsIgnoreCase("list")){
             if(!sender.hasPermission("sortal.list")){
                 sender.sendMessage(this.getLocalisation().getNoPerms());
                 return true;
             }
             int page;
             if(args.length == 1){
                 //Get page 1
                 page=1;
             }else{
                 try{
                     page = Integer.parseInt(args[1]);
                 }catch(NumberFormatException e){
                     sender.sendMessage("Expected int, got String: ERR");
                     return true;
                 }
             }
             sender.sendMessage("===Sortal Warps===");
            sender.sendMessage("Page " + page + "/" + this.getPlugin().getWarpManager().getWarps().size() / 8 + 1);
             int count = -1;
             for(Warp warp : this.getPlugin().getWarpManager().getWarps()){
                 count++;
                 if(count < (page-1)*8 || count >= page*8){
                     continue;
                 }
                 sender.sendMessage(warp.getName() + ": " + warp.getLocationToString());
             }
             return true;
         }
         if(args[0].equalsIgnoreCase("version")){
             sender.sendMessage("===Sortal===");
             sender.sendMessage("Version " + this.getPlugin().getSettings().getVersion() + 
                     " build " + this.getPlugin().getDescription().getVersion());
             return true;
         }
         if(args[0].equalsIgnoreCase("register")){
             //TODO register
         }
         sender.sendMessage("Unknown syntax, /sortal help for commands");
         return true;
     }
 
 }
