 package com.archmageinc.RandomEncounters;
 
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 /**
  *
  * @author ArchmageInc
  */
 public class CommandListener implements CommandExecutor{
 
     @Override
     public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
         if(command.getName().equalsIgnoreCase("re")){
             if(args.length<1){
                 return false;
             }
             if(args[0].equalsIgnoreCase("place")){
                 if((sender instanceof Player) && args.length<2){
                     sender.sendMessage("Usage: /re place <EncounterName> [<world> <x> <y> <z>]");
                     return true;
                 }
                 if(!(sender instanceof Player) && args.length<6){
                     sender.sendMessage("Usage: /re place <EncounterName> <world> <x> <y> <z>");
                     return true;
                 }
                 Location location;
                 if(args.length>=6){
                     try{
                         String worldName    =   args[2];
                         Integer x           =   Integer.parseInt(args[3]);
                         Integer y           =   Integer.parseInt(args[4]);
                         Integer z           =   Integer.parseInt(args[5]);
                         World world         =   RandomEncounters.getInstance().getServer().getWorld(worldName);
                         if(world==null){
                             sender.sendMessage("World "+worldName+" was not found!");
                             return true;
                         }
                         location    =   new Location(world,x,y,z);
                     }catch(NumberFormatException e){
                         sender.sendMessage("Coordinates must be numeric!");
                         return true;
                     }
                 }else{
                     location    =   ((Player) sender).getLocation();
                 }
                 String encounterName    =   args[1];
                 placeEncounter(sender,encounterName,location);
                 return true;
             }
             if(args[0].equalsIgnoreCase("reload")){
                 reloadConfigurations();
                 return true;
             }
         }
         return false;
     }
     
     protected void placeEncounter(CommandSender sender,String encounterName,Location location){
         Encounter encounter =   Encounter.getInstance(encounterName);
        if(encounterName==null){
            sender.sendMessage("Encounter "+encounter+" was not found!");
             return;
         }
         if(location==null){
             sender.sendMessage("Not a valid location!");
             return;
         }
         RandomEncounters.getInstance().addPlacedEncounter(PlacedEncounter.create(encounter, location));
     }
     
     protected void reloadConfigurations(){
         RandomEncounters.getInstance().loadConfigurations();
     }
     
 }
