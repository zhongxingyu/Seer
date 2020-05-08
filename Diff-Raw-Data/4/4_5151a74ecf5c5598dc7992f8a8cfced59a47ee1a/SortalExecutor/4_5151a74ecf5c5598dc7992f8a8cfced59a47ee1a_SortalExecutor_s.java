 package nl.lolmewn.sortal;
 
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.player.PlayerTeleportEvent;
 
 /**
  *
  * @author Lolmewn
  */
 public class SortalExecutor implements CommandExecutor {
 
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
                 if(sender instanceof Player && !this.getPlugin().pay((Player)sender, this.getPlugin().getSettings().getWarpCreatePrice())){
                     return true; 
                 }
                 Warp w = this.getPlugin().getWarpManager().addWarp(warp, ((Player)sender).getLocation());
                 w.setOwner(sender.getName());
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
                     int count = 0;
                     for(SignInfo s : this.getPlugin().getWarpManager().getSigns()){
                         if(s.hasWarp() && s.getWarp().equals(warp)){
                             count++;
                         }
                     }
                     if(count != 0){
                         sender.sendMessage("You've broken " + count + " signs by deleting warp " + warp);
                     }
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
             sender.sendMessage("Page " + page + "/" + (this.getPlugin().getWarpManager().getWarps().size() / 8 + 1));
             int count = -1;
             for(Warp warp : this.getPlugin().getWarpManager().getWarps()){
                 count++;
                 if(count < (page-1)*8 || count >= page*8){
                     continue;
                 }
                 sender.sendMessage(warp.getName() + ": " + warp.getLocationToString());
             }
             if(count == -1){
                 //no warps found!
                 sender.sendMessage(this.getLocalisation().getNoWarpsFound());
             }
             return true;
         }
         if(args[0].equalsIgnoreCase("version")){
             sender.sendMessage("===Sortal===");
             sender.sendMessage("Version " + this.getPlugin().getSettings().getVersion() + 
                     " build " + this.getPlugin().getDescription().getVersion());
             return true;
         }
         if(args[0].equalsIgnoreCase("unregister")){
             if(!(sender instanceof Player)){
                 sender.sendMessage(this.getLocalisation().getNoPlayer());
                 return true;
             }
             if(!sender.hasPermission("sortal.unregister")){
                 sender.sendMessage(this.getLocalisation().getNoPerms());
                 return true;
             }
             if(this.getPlugin().unregister.contains(sender.getName())){
                 this.getPlugin().unregister.remove(sender.getName());
                 sender.sendMessage("No longer unregistering!");
                 return true;
             }
             if(this.getPlugin().setcost.containsKey(sender.getName())){
                 sender.sendMessage("Please finish setting a cost first! (cancel is /sortal setprice)");
                 return true;
             }
             if(this.getPlugin().register.containsKey(sender.getName())){
                 sender.sendMessage("Please finish registering first! (cancel is /sortal register)");
                 return true;
             }
             this.getPlugin().unregister.add(sender.getName());
             sender.sendMessage("Now punch the sign you wish to be unregistered!");
             return true;
         }
         if(args[0].equalsIgnoreCase("register")){
             if(!(sender instanceof Player)){
                 sender.sendMessage(this.getLocalisation().getNoPlayer());
                 return true;
             }
             if(!sender.hasPermission("sortal.register")){
                 sender.sendMessage(this.getLocalisation().getNoPerms());
                 return true;
             }
             if(args.length == 1){
                 if(this.getPlugin().register.containsKey(sender.getName())){
                     this.getPlugin().register.remove(sender.getName());
                     sender.sendMessage("No longer registering!");
                     return true;
                 }
                 sender.sendMessage("Correct usage: /sortal register <warp>");
                 return true;
             }
             if(this.getPlugin().setcost.containsKey(sender.getName())){
                 sender.sendMessage("Please finish setting a cost first! (cancel is /sortal setprice)");
                 return true;
             }
             if(this.getPlugin().register.containsKey(sender.getName())){
                 sender.sendMessage("Please finish registering first! (cancel is /sortal register)");
                 return true;
             }
             if(this.getPlugin().unregister.contains(sender.getName())){
                 sender.sendMessage("Please finish unregistering first! (cancel is /sortal unregister)");
                 return true;
             }
             String warp = args[1];
             if(!this.getPlugin().getWarpManager().hasWarp(warp)){
                 sender.sendMessage(this.getLocalisation().getWarpNotFound(warp));
                 return true;
             }
             this.getPlugin().register.put(sender.getName(), warp);
             sender.sendMessage("Now punch the sign you wish to be pointing to " + warp);
             return true;
         }
         if(args[0].equalsIgnoreCase("setprice") || args[0].equalsIgnoreCase("setcost") || args[0].equalsIgnoreCase("price")){
             if(!(sender instanceof Player)){
                 sender.sendMessage(this.getLocalisation().getNoPlayer());
                 return true;
             }
             if(!sender.hasPermission("sortal.setprice")){
                 sender.sendMessage(this.getLocalisation().getNoPerms());
                 return true;
             }
             if(args.length == 1){
                 if(this.getPlugin().setcost.containsKey(sender.getName())){
                     this.getPlugin().setcost.remove(sender.getName());
                     sender.sendMessage("No longer setting a cost!");
                     return true;
                 }
                 sender.sendMessage("Correct usages: /sortal " + args[0] + " <cost>");
                 sender.sendMessage("Or /sortal " + args[0] + " warp <warpname> <cost>");
                 return true;
             }
             if(this.getPlugin().setcost.containsKey(sender.getName())){
                 sender.sendMessage("Please finish setting a cost first! (cancel is /sortal setprice)");
                 return true;
             }
             if(this.getPlugin().register.containsKey(sender.getName())){
                 sender.sendMessage("Please finish registering first! (cancel is /sortal register)");
                 return true;
             }
             if(this.getPlugin().unregister.contains(sender.getName())){
                 sender.sendMessage("Please finish unregistering first! (cancel is /sortal unregister)");
                 return true;
             }
             if(args[1].equalsIgnoreCase("warp")){
                if(args.length == 2){
                    sender.sendMessage("Correct usage: /sortal " + args[0] + " warp <warpname> <cost>");
                    return true;
                }
                if(args.length == 3){
                    sender.sendMessage("Correct usage: /sortal " + args[0] + " warp " + args[2] + " <cost>");
                    return true;
                }
                try{
                    int price = Integer.parseInt(args[3]);
                    String warp = args[2];
                    if(!this.getPlugin().getWarpManager().hasWarp(warp)){
                        sender.sendMessage(this.getLocalisation().getWarpNotFound(warp));
                        return true;
                    }
                    this.getPlugin().getWarpManager().getWarp(warp).setPrice(price);
                    sender.sendMessage(this.getLocalisation().getCostSet(Integer.toString(price)));
                    return true;
                }catch(NumberFormatException e){
                    sender.sendMessage("Expected int, got string. <price> should be int!");
                    return true;
                }
             }// End of args[1] = warp
             try{
                 int price = Integer.parseInt(args[1]);
                 this.getPlugin().setcost.put(sender.getName(), price);
                 sender.sendMessage("Now punch the sign you want to be costing " + price);
                 return true;
             }catch(NumberFormatException e){
                 sender.sendMessage("Expected int, got string.");
                 sender.sendMessage("Correct usage: /sortal " + args[0] + " <price>");
                 return true;
             }
         }
         if(args[0].equalsIgnoreCase("convert")){
             if(!sender.isOp()){
                 sender.sendMessage(this.getLocalisation().getNoPerms());
                 return true;
             }
             this.getPlugin().getSettings().setUseMySQL(!this.getPlugin().getSettings().useMySQL());
             if(this.getPlugin().getSettings().useMySQL()){
                 if(!this.getPlugin().initMySQL()){
                     sender.sendMessage("Something went wrong while enabling MySQL! Please check the logs. Using flatfile now.");
                     return true;
                 }
                 this.getPlugin().saveData();
                 sender.sendMessage("All data should have been saved to the MySQL table!");
                 return true;
             }
             this.getPlugin().saveData();
             sender.sendMessage("All data should have been saved to flatfiles!");
             return true;
         }
         if(args[0].equalsIgnoreCase("help")){
             sender.sendMessage("===Sortal Help Page===");
             if(sender.hasPermission("sortal.createwarp")){
                 sender.sendMessage("/sortal warp <name> - Creates a warp at your location");
             }
             if(sender.hasPermission("sortal.delwarp")){
                 sender.sendMessage("/sortal delwarp <name> - Deletes warp <name>");
             }
             if(sender.hasPermission("sortal.list")){
                 sender.sendMessage("/sortal list (page) - lists all available warps");
             }
             if(sender.hasPermission("sortal.setprice")){
                 sender.sendMessage("/sortal setprice <cost> - Set a price for a sign");
                 sender.sendMessage("/sortal setprice warp <warp> <cost> - Set a price for a warp");
             }
             if(sender.hasPermission("sortal.register")){
                 sender.sendMessage("/sortal register <warp> - Register a sign to TP to <warp>");
             }
             if(sender.isOp()){
                 sender.sendMessage("/sortal convert - Converts from flat-MySQL or back");
             }
             sender.sendMessage("/sortal version - Tells you the version you are using");
         }
         if(args[0].equalsIgnoreCase("goto") || args[0].equalsIgnoreCase("warpto")){
             if(!(sender instanceof Player)){
                 sender.sendMessage(this.getLocalisation().getNoPlayer());
                 return true;
             }
             if(!sender.hasPermission("sortal.directwarp")){
                 sender.sendMessage(this.getLocalisation().getNoPerms());
                 return true;
             }
             if(args.length == 1){
                 sender.sendMessage("Correct usage: /sortal " + args[0] + " <warp>");
                 return true;
             }
             String warp = args[1];
             if(this.getPlugin().getSettings().isPerWarpPerm()){
                 if(!sender.hasPermission("sortal.directwarp." + warp)){
                     sender.sendMessage(this.getLocalisation().getNoPerms());
                     return true;
                 }
             }
             if(!this.getPlugin().getWarpManager().hasWarp(warp)){
                 sender.sendMessage(this.getLocalisation().getWarpNotFound(warp));
                 return true;
             }
             Warp w = this.getPlugin().getWarpManager().getWarp(warp);
             Player p = (Player)sender;
             p.teleport(w.getLocation(), PlayerTeleportEvent.TeleportCause.PLUGIN);
             sender.sendMessage(this.getLocalisation().getPlayerTeleported(w.getName()));
             return true;
         }
         if(args[0].equalsIgnoreCase("setuses") || args[0].equalsIgnoreCase("uses")){
             if(!sender.hasPermission("sortal.setuses")){
                 sender.sendMessage(this.getLocalisation().getNoPerms());
                 return true;
             }
             if(args.length == 1){
                 sender.sendMessage("Correct usage: /sortal " + args[0] + " <amount> OR /sortal " + args[0] + " warp <warp> <amount>");
                 return true;
             }
             if(args.length == 2){
                 try{
                     int uses = Integer.parseInt(args[1]);
                     this.getPlugin().setuses.put(sender.getName(), uses);
                     sender.sendMessage("Now punch the sign you wish to be usable " + uses + " times!");
                     return true;
                 }catch(NumberFormatException e){
                     sender.sendMessage("ERR: Int expected, got string!");
                     return true;
                 }
             }
             if(args[1].equalsIgnoreCase("warp")){
                if(args.length == 3 || args.length == 4){
                    sender.sendMessage("Correct usage: /sortal " + args[0] + " warp " + (args.length == 3? "<warp>" : args[2]) + " <amount>");
                     return true;
                 }
                 String warp = args[2];
                 if(!this.getPlugin().getWarpManager().hasWarp(warp)){
                     sender.sendMessage(this.getLocalisation().getWarpNotFound(warp));
                     return true;
                 }
                 Warp w = this.getPlugin().getWarpManager().getWarp(warp);
                 try{
                     int uses = Integer.parseInt(args[3]);
                     w.setUses(uses);
                     sender.sendMessage(this.getLocalisation().getMaxUsesSet(args[3]));
                     return true;
                 }catch(NumberFormatException e){
                     sender.sendMessage("ERR: Int expected, got string!");
                     return true;
                 }
             }
             sender.sendMessage("Correct usage: /sortal " + args[0] + " <amount> OR /sortal " + args[0] + " warp <warp> <amount>");
             return true;
         }
         sender.sendMessage("Unknown syntax, /sortal help for commands");
         return true;
     }
 
 }
