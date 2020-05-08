 package com.ne0nx3r0.quantum;
 
 import com.ne0nx3r0.quantum.circuits.CircuitManager;
 import com.ne0nx3r0.quantum.circuits.PendingCircuit;
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 public class QuantumConnectorsCommandExecutor implements CommandExecutor {
     private QuantumConnectors plugin;
     
     public QuantumConnectorsCommandExecutor(QuantumConnectors plugin){
         this.plugin = plugin;
     }
     
     @Override
     public boolean onCommand(CommandSender cs, Command cmd, String alias, String[] args) {
         if (!(cs instanceof Player)){
             plugin.log("You can't run this from the console!");
         }
         
         Player player = (Player) cs;
         
 // Command was: "/qc"
         if(args.length == 0 || args[0].equalsIgnoreCase("?")){    
             plugin.msg(player, "To create a quantum circuit, use /qc <circuit>; and click   on a sender and then a receiver with redstone.");
 
            plugin.msg(player, ChatColor.YELLOW + "Available circuits: " + ChatColor.WHITE + CircuitManager.getValidSendersString());
         }
       
 // Command was: "/qc cancel"
         else if(args[0].equalsIgnoreCase("cancel")){
         
         //Pending circuit exists
             if(CircuitManager.hasPendingCircuit(player)){
                 
                 CircuitManager.removePendingCircuit(player);
                 
                 plugin.msg(player, "Your pending circuit has been removed!");
             }
         //No pending circuit
             else{
                 plugin.msg(player, "No pending circuit to remove.");
             }
         }
 
 // Command was: "/qc done"
         else if(args[0].equalsIgnoreCase("done")){
         
         //They typed "/qc <circuit>"
             if(CircuitManager.hasPendingCircuit(player)){
                 PendingCircuit pc = CircuitManager.getPendingCircuit(player);
             //They also setup a sender
                 if(pc.hasSenderLocation()){
                 //Finally, they also setup at least one receiver
                     if(pc.hasReceiver()){
                         CircuitManager.addCircuit(pc); 
                         
                     // I hate doors, I hate all the wooden doors.
                     // I just want to break them all, but I can't
                     // Can't break all wood doors.
                         if(pc.getSenderLocation().getBlock().getType() == Material.WOODEN_DOOR){
                             Block bDoor = pc.getSenderLocation().getBlock();
                             int iData = (int) bDoor.getData();
                             Block bOtherPiece = bDoor.getRelative((iData & 0x08) == 0x08 ? BlockFace.DOWN : BlockFace.UP);
 
                             //TODO: Clone instead of reference the circuit?
                             //TODO: On break check if the circuit has a twin
                             CircuitManager.addCircuit(bOtherPiece.getLocation(),pc.getCircuit());
                         }
                         
                         CircuitManager.removePendingCircuit(player);
 
                         plugin.msg(player, "Quantum circuit created!");
                     }
                     //They have not setup at least one receiver
                     else{
                         plugin.msg(player,"You need to setup at least one receiver first!");
                     }
                 }
                 //They didn't setup a sender
                 else{
                    plugin.msg(player, "You need to setup a sender and receiver first!"); 
                 }
             }else{
                 plugin.msg(player,"No pending action to finish.");
             }
         }
         
 // Command was: "/qc <valid circuit type>"
         else if(CircuitManager.isValidCircuitType(args[0])){
             
         //Player has permission to create the circuit
             if(player.hasPermission("QuantumConnectors.create."+args[0])){
 
             //Figure out if there's a delay, or use 0 for no delay
                 double dDelay = 0;
 
                 if(args.length > 1){
                     try { 
                         dDelay = Double.parseDouble(args[1]);
                     }
                     catch (NumberFormatException e){
                         dDelay = -1;
                     }      
 
                     if(dDelay < 0 || dDelay > QuantumConnectors.MAX_DELAY_TIME){
                         dDelay = 0;
                         
                         plugin.msg(player,ChatColor.RED + "Invalid delay time (min:0s max:"+QuantumConnectors.MAX_DELAY_TIME+"s, assuming no delay");  
                     }
                 }
                 
                 String sDelayMsg = " ("+args[0]+" "+(dDelay == 0 ? "no" : dDelay+"s")+" delay)";
                 
                 int iDelayTicks = (int) Math.round(dDelay*20);
                 
                 if(!CircuitManager.hasPendingCircuit(player)){
                     CircuitManager.addPendingCircuit(
                             player,
                             CircuitManager.getCircuitType(args[0]),
                             iDelayTicks);
                     
                     plugin.msg(player, args[0]+" circuit ready to be created!"+sDelayMsg);
                 }
                 else{       
                     CircuitManager.getPendingCircuit(player).setCircuitType(
                             CircuitManager.getCircuitType(args[0]),
                             iDelayTicks);
                     
                     plugin.msg(player, "Circuit type switched to: "+args[0]+sDelayMsg);
                 }
             }
             
         //Player doesn't have permission
             else{
                 plugin.msg(player, ChatColor.RED + "You don't have permission to create the " + args[0] + " circuit!");
             }
         }
         
 // Command was invalid
         else{
             plugin.msg(player,"Invalid circuit specified. '/qc' for usage.");
         }
        
         return true;
         
     }//End onCommand
 }
