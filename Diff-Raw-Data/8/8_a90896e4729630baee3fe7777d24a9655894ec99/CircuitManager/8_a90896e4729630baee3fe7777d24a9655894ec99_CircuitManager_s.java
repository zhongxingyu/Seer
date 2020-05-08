 package com.ne0nx3r0.quantum.circuits;
 
 import com.ne0nx3r0.quantum.QuantumConnectors;
 import java.io.File;
 import java.io.IOException;
 import java.util.*;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import net.minecraft.server.EntityTNTPrimed;
 import org.bukkit.Effect;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.craftbukkit.CraftWorld;
 import org.bukkit.entity.Player;
 
 public final class CircuitManager{
     private static QuantumConnectors plugin;
     
 // Lookup/Storage for circuits, and subsequently their receivers
     private static Map<World,Map<Location, Circuit>> worlds = new HashMap<World,Map<Location, Circuit>>();
 
 // Temporary Holders for circuit creation
     public static Map<Player, PendingCircuit> pendingCircuits;
 
 // Allow circuitTypes/circuits
     public static Map<String, Integer> circuitTypes = new HashMap<String, Integer>();
     
     private static Material[] validSenders = new Material[]{
         Material.LEVER,
         Material.REDSTONE_WIRE,
         Material.STONE_BUTTON,
         Material.STONE_PLATE,
         Material.WOOD_PLATE,
         Material.REDSTONE_TORCH_OFF,
         Material.REDSTONE_TORCH_ON,
         Material.REDSTONE_LAMP_OFF,
         Material.REDSTONE_LAMP_ON,
         //Material.DIODE_BLOCK_OFF,
         //Material.DIODE_BLOCK_ON,//TODO: Figure out repeaters as senders
         Material.IRON_DOOR_BLOCK,
         Material.WOODEN_DOOR,
         Material.TRAP_DOOR,
         Material.FENCE_GATE,
         Material.CHEST,
         Material.BOOKSHELF,
        Material.BED,
         //Material.POWERED_RAIL,//TODO: Figure out powered rail as sender
         //TODO: Add chests?
     };
     private static Material[] validReceivers = new Material[]{
         Material.LEVER,
         Material.IRON_DOOR_BLOCK,
         Material.WOODEN_DOOR,
         Material.TRAP_DOOR,
         Material.POWERED_RAIL,
         Material.FENCE_GATE,
         //Material.REDSTONE_TORCH_OFF,
         //Material.REDSTONE_TORCH_ON,//TODO: Figure out torches&lamps as receivers
         //Material.REDSTONE_LAMP_OFF,
         //Material.REDSTONE_LAMP_ON,
         //Material.PISTON_BASE,
         //Material.PISTON_STICKY_BASE,//TODO: Figure out pistons as receivers
         Material.TNT
     };
 
 // Main
     public CircuitManager(final QuantumConnectors qc){
         CircuitManager.plugin = qc;
         
     //Setup available circuit types 
         for (CircuitTypes t : CircuitTypes.values()){
             circuitTypes.put(t.name, t.id);
         }
      
     //Create a holder for pending circuits
         pendingCircuits = new HashMap<Player,PendingCircuit>();
     
     //Convert circuits.yml to new structure    
         if(new File(plugin.getDataFolder(),"circuits.yml").exists()){
             convertOldCircuitsYml();
         }
         
     //Init any loaded worlds
         for(World world: plugin.getServer().getWorlds()){
             loadWorld(world);
         }
     }
     
 // Sender/Receiver checks
     public static boolean isValidSender(Block block) {
         Material mBlock = block.getType();
         for (int i = 0; i < validSenders.length; i++) {
             if (mBlock == validSenders[i]) {
                 return true;
             }
         }
 
         return false;
     }
 
     public static String getValidSendersString() {
         String msg = "";
         for (int i = 0; i < validSenders.length; i++) {
             msg += (i != 0 ? ", " : "") + validSenders[i].name().toLowerCase().replace("_", " ");
         }
 
         return msg;
     }
 
     public static boolean isValidReceiver(Block block) {
         Material mBlock = block.getType();
         for (int i = 0; i < validReceivers.length; i++) {
             if (mBlock == validReceivers[i]) {
                 return true;
             }
         }
 
         return false;
     }
 
     public static String getValidReceiversString() {
         String msg = "";
         for (int i = 0; i < validReceivers.length; i++) {
             msg += (i != 0 ? ", " : "") + validReceivers[i].name().toLowerCase().replace("_", " ");
         }
 
         return msg;
     }
     
     
 // Circuit (sender) CRUD
     public static void addCircuit(Location circuitLocation, Circuit newCircuit){
         //Notably circuits are now created from a temporary copy, rather than piecemeal here. 
         worlds.get(circuitLocation.getWorld()).put(circuitLocation, newCircuit);
     }
     public static void addCircuit(PendingCircuit pc){             
        worlds.get(pc.getSenderLocation().getWorld())
                .put(pc.getSenderLocation(),pc.getCircuit());
     }
     
     public static boolean circuitExists(Location circuitLocation){
         return worlds.get(circuitLocation.getWorld()).containsKey(circuitLocation);
     } 
     
     public static Circuit getCircuit(Location circuitLocation){
         return worlds.get(circuitLocation.getWorld()).get(circuitLocation);
     }
     
     public static void removeCircuit(Location circuitLocation) {
         if(circuitExists(circuitLocation)){
             worlds.get(circuitLocation.getWorld()).remove(circuitLocation);
         }
     }
     
 // Circuit activation    
     public static void activateCircuit(Location lSender, int current){
         activateCircuit(lSender, current, 0);
     }
     public static void activateCircuit(Location lSender, int current, int chain){
         Circuit circuit = getCircuit(lSender);
         List receivers = circuit.getReceivers();
         
         if(!receivers.isEmpty()){
             int iType;
             int iDelay;
 
             Receiver r;
             for(int i = 0; i < receivers.size(); i++){
                 r = (Receiver) receivers.get(i);
                 
                 iType = r.type;
                 iDelay = r.delay;
                 Block b = r.location.getBlock();
 
                 if (isValidReceiver(b)){
                     if (iType == CircuitTypes.QUANTUM.getId()) {
                         setReceiver(b, current > 0 ? true : false,iDelay);
                     } else if (iType == CircuitTypes.ON.getId()) {
                         if (current > 0) {
                             setReceiver(b, true,iDelay);
                         }
                     } else if (iType == CircuitTypes.OFF.getId()) {
                         if (current > 0) {
                             setReceiver(b, false,iDelay);
                         }
                     } else if (iType == CircuitTypes.TOGGLE.getId()) {
                         if (current > 0) {
                             setReceiver(b, getBlockCurrent(b) > 0 ? false : true,iDelay);
                         }
                     } else if (iType == CircuitTypes.REVERSE.getId()) {
                         setReceiver(b, current > 0 ? false : true,iDelay);
                     } else if (iType == CircuitTypes.RANDOM.getId()) {
                         if (current > 0) {
                             setReceiver(b, new Random().nextBoolean() ? true : false,iDelay);
                         }
                     }
 
                     if (b.getType() == Material.TNT) { // TnT is one time use!
                         circuit.delReceiver(r);
                     }
 
                     if (QuantumConnectors.MAX_CHAIN_LINKS > 0) { //allow zero to be infinite
                         chain++;
                     }
                     if(chain <= QuantumConnectors.MAX_CHAIN_LINKS && circuitExists(b.getLocation())){
                         activateCircuit(r.location, getBlockCurrent(b), chain);
                     }
                 }else{
                     circuit.delReceiver(r);
                 }
             }
         } 
     }
     
     private static class DelayedSetReceiver implements Runnable{
         private final Block block;
         private final boolean on;
 
         DelayedSetReceiver(Block block, boolean on){
             this.block = block;
             this.on = on;
         }
         
         @Override
         public void run(){
             setReceiver(block,on);
         }
     }
 
     public static int getBlockCurrent(Block b) {
         Material mBlock = b.getType();
         int iData = (int) b.getData();
 
         if (mBlock == Material.LEVER
                 || mBlock == Material.POWERED_RAIL) {
             return (iData & 0x08) == 0x08 ? 15 : 0;
         } else if (mBlock == Material.IRON_DOOR_BLOCK
                 || mBlock == Material.WOODEN_DOOR
                 || mBlock == Material.TRAP_DOOR
                 || mBlock == Material.FENCE_GATE) {
             return (iData & 0x04) == 0x04 ? 15 : 0;
         }
 
         return b.getBlockPower();
     }
     
     private static void setReceiver(Block block, boolean on,int iDelay){
         if(iDelay == 0){
             setReceiver(block,on);
         }else{
             plugin.getServer().getScheduler().scheduleSyncDelayedTask(
                 plugin,
                 new DelayedSetReceiver(block,on),
                 iDelay); 
         }
     }
     
     private static void setReceiver(Block block, boolean on){
         Material mBlock = block.getType();
         int iData = (int) block.getData();
 
         if (mBlock == Material.LEVER) {
             if (on && (iData & 0x08) != 0x08) { // Massive annoyance
                 iData |= 0x08; //send power on
             } else if (!on && (iData & 0x08) == 0x08) {
                 iData ^= 0x08; //send power off
             }
             int i1 = iData & 7;
             net.minecraft.server.World w = ((net.minecraft.server.World) ((CraftWorld) block.getWorld()).getHandle());
             Location l = block.getLocation();
             int i = (int) l.getX();
             int j = (int) l.getY();
             int k = (int) l.getZ();
             int id = block.getTypeId();
             w.setData(i, j, k, iData);
             w.applyPhysics(i, j, k, id);
             if (i1 == 1) {
                 w.applyPhysics(i - 1, j, k, id);
             } else if (i1 == 2) {
                 w.applyPhysics(i + 1, j, k, id);
             } else if (i1 == 3) {
                 w.applyPhysics(i, j, k - 1, id);
             } else if (i1 == 4) {
                 w.applyPhysics(i, j, k + 1, id);
             } else {
                 w.applyPhysics(i, j - 1, k, id);
             }
         } else if (mBlock == Material.POWERED_RAIL) {
             if (on && (iData & 0x08) != 0x08) {
                 iData |= 0x08; //send power on
             } else if (!on && (iData & 0x08) == 0x08) {
                 iData ^= 0x08; //send power off
             }
             block.setData((byte) iData);
         } else if(mBlock == Material.IRON_DOOR_BLOCK 
                || mBlock == Material.WOODEN_DOOR) {
             Block bOtherPiece = block.getRelative(((iData & 0x08) == 0x08) ? BlockFace.DOWN : BlockFace.UP);
             int iOtherPieceData = (int) bOtherPiece.getData();
 
             if (on && (iData & 0x04) != 0x04) {
                 iData |= 0x04;
                 iOtherPieceData |= 0x04;
             } else if (!on && (iData & 0x04) == 0x04) {
                 iData ^= 0x04;
                 iOtherPieceData ^= 0x04;
             }
             block.setData((byte) iData);
             bOtherPiece.setData((byte) iOtherPieceData);
             block.getWorld().playEffect(block.getLocation(), Effect.DOOR_TOGGLE, 0, 10);
         } else if(mBlock == Material.TRAP_DOOR
                || mBlock == Material.FENCE_GATE){
             if (on && (iData & 0x04) != 0x04) {
                 iData |= 0x04;//send open
             } else if (!on && (iData & 0x04) == 0x04) {
                 iData ^= 0x04;//send close
             }
             block.setData((byte) iData);
         } else if (mBlock == Material.TNT) {
             block.setType(Material.AIR);
             CraftWorld world = (CraftWorld) block.getWorld();
             EntityTNTPrimed tnt = new EntityTNTPrimed(world.getHandle(), block.getX() + 0.5F, block.getY() + 0.5F, block.getZ() + 0.5F);
             world.getHandle().addEntity(tnt);
             block.getWorld().playEffect(block.getLocation(), Effect.SMOKE, 1);
         } else if (mBlock == Material.PISTON_BASE || mBlock == Material.PISTON_STICKY_BASE) {
             // Makeshift piston code... Doesn't work!
             if (on && (iData & 0x08) != 0x08) {
                 iData |= 0x08; //send power on
             } else if (!on && (iData & 0x08) == 0x08) {
                 iData ^= 0x08; //send power off
             }
             block.setData((byte) iData);
             //net.minecraft.server.Block.PISTON.doPhysics(((CraftWorld)block.getWorld()).getHandle(), block.getX(), block.getY(), block.getZ(), -1);
         } else if (mBlock == Material.REDSTONE_TORCH_ON) {
             if (!on) {
                 block.setType(Material.REDSTONE_TORCH_OFF);
             }
         } else if (mBlock == Material.REDSTONE_TORCH_OFF) {
             if (on) {
                 block.setType(Material.REDSTONE_TORCH_ON);
             }
         } else if (mBlock == Material.REDSTONE_LAMP_ON) {
             if (!on) {
                 block.setType(Material.REDSTONE_LAMP_OFF);
             }
         } else if (mBlock == Material.REDSTONE_LAMP_OFF) {
             if (on) {
                 block.setType(Material.REDSTONE_LAMP_ON);
             }
         }
     }
 
     public void saveWorld(World world){
         if(worlds.containsKey(world)){
         //Alright let's do this!
             File ymlFile = new File(plugin.getDataFolder(),world.getName()+".circuits.yml");
             if(!ymlFile.exists()) {
                 try {
                     ymlFile.createNewFile();
                 } catch(IOException ex) {
                     plugin.error("Could not create "+ymlFile.getName());
                 }
             }
             FileConfiguration yml = YamlConfiguration.loadConfiguration(ymlFile);
             
             //plugin.log("Saving "+ymlFile.getName()+"...");
             
         //Prep this world's data for saving
             List<Object> tempCircuits = new ArrayList<Object>();
 
             Map<String,Object> tempCircuitObj;
             Map<String,Object> tempReceiverObj;
             ArrayList tempReceiverObjs;
             Circuit currentCircuit;
             List<Receiver> currentReceivers;
 
             Map<Location,Circuit> currentWorldCircuits = worlds.get(world);
             
             for(Location cLoc : currentWorldCircuits.keySet()){
 
                 currentCircuit = currentWorldCircuits.get(cLoc);
 
                 tempCircuitObj = new HashMap<String,Object>();
 
                 tempCircuitObj.put("x",cLoc.getBlockX());
                 tempCircuitObj.put("y",cLoc.getBlockY());
                 tempCircuitObj.put("z",cLoc.getBlockZ());
                 
                 tempCircuitObj.put("o", currentCircuit.getOwner());
 
                 currentReceivers = currentCircuit.getReceivers();
 
                 tempReceiverObjs = new ArrayList();
                 Receiver r;
                 for(int i = 0; i < currentReceivers.size(); i++){
                     r = currentReceivers.get(i);
                     
                     tempReceiverObj = new HashMap<String,Object>();
                     
                     tempReceiverObj.put("z",r.location.getBlockZ());
                     tempReceiverObj.put("y",r.location.getBlockY());
                     tempReceiverObj.put("x",r.location.getBlockX());
                     
                     tempReceiverObj.put("d",r.delay);
                     tempReceiverObj.put("t",r.type);
                     
                     tempReceiverObjs.add(tempReceiverObj);
                 }
 
                 tempCircuitObj.put("r",tempReceiverObjs);
 
                 tempCircuits.add(tempCircuitObj);
             }
             
             yml.set("fileVersion","2");
             yml.set("circuits", tempCircuits);
 
             try{
                 yml.save(ymlFile);
 
                 plugin.log(ymlFile.getName()+" Saved!");
             }catch(IOException IO) {
                 plugin.error("Failed to save "+ymlFile.getName());
             }  
         }else{
             plugin.error(world.getName() + " could not be saved!");
         }
     }
     public void saveAllWorlds(){
         plugin.log("Saving Circuits");
         
         for(World world : worlds.keySet()){
             saveWorld(world);
         }
         //huh, that was easy.
     }
 
     public void loadWorld(World world){
         //at least create a blank holder
         worlds.put(world,new HashMap<Location,Circuit>());
         
         File ymlFile = new File(plugin.getDataFolder(),world.getName()+".circuits.yml");
         
         plugin.log("Loading "+ymlFile.getName()+"...");
 
         if(!ymlFile.exists()) {
             plugin.error(ymlFile.getName() + " not found, will be created with the next save.");
             return;
         }
         
         FileConfiguration yml = YamlConfiguration.loadConfiguration(ymlFile);
         
         List<Map<String,Object>> tempCircuits = (List<Map<String,Object>>) yml.get("circuits");
 
         if(tempCircuits == null){
             plugin.log("No circuits found in "+ymlFile.getName());
             return;
         }
         
         Map<Location,Circuit> worldCircuits = new HashMap<Location,Circuit>();
         
         Location tempCircuitObjLoc;
         
         ArrayList tempReceiverObjs;
         Map<String,Object> tempReceiverObj;
         Location tempReceiverLoc;
 
         for(Map<String,Object> tempCircuitObj : tempCircuits){          
             //dummy value of # for owners
             Circuit tempCircuit = new Circuit((String) (tempCircuitObj.get("o") == null ? "" : tempCircuitObj.get("o")));
             tempReceiverObjs = (ArrayList) tempCircuitObj.get("r");
             
         //TODO: circuit/receiver verification
             for(int i = 0; i < tempReceiverObjs.size(); i++) {
                 tempReceiverObj = (Map<String, Object>) tempReceiverObjs.get(i);
                 tempReceiverLoc = new Location(
                         world,
                         (Integer) tempReceiverObj.get("x"),
                         (Integer) tempReceiverObj.get("y"),
                         (Integer) tempReceiverObj.get("z"));
                 
                 if(CircuitManager.isValidReceiver(tempReceiverLoc.getBlock())){
                     tempCircuit.addReceiver(
                             tempReceiverLoc,
                             (Integer) tempReceiverObj.get("t"),
                             (Integer) tempReceiverObj.get("d"));
                 }
                 //Invalid receiver block type
                 else{
                     plugin.log("Removed a " + world.getName() + " circuit's receiver; "+tempReceiverLoc.getBlock().getType().name()+" is not a valid receiver.");
                 }
             }
 
             // Verify there is at least one valid receiver
                 if(!tempCircuit.getReceivers().isEmpty()){
                     tempCircuitObjLoc = new Location(
                                 world,
                                 (Integer) tempCircuitObj.get("x"),
                                 (Integer) tempCircuitObj.get("y"),
                                 (Integer) tempCircuitObj.get("z"));
                     
                     //Verify the sender is a valid type
                     if(CircuitManager.isValidSender(tempCircuitObjLoc.getBlock())){
                         worldCircuits.put(tempCircuitObjLoc,tempCircuit); 
                     }
                     //Invalid sender type
                     else{
                         plugin.log("Removed a "+world.getName()+" circuit; "+tempCircuitObjLoc.getBlock().getType().name()+" is not a valid sender.");
                     }
                 }
             // No valid receivers for this circuit
                 else{
                     plugin.log("Removed a '"+world.getName()+"' circuit: no valid receivers.");
                 }
         }
         
         worlds.put(world,worldCircuits);
     }
 
 // Temporary circuit stuff 
 // I really don't know what order this deserves among the existing class methods
     public static PendingCircuit addPendingCircuit(Player player,int type,int delay){
         PendingCircuit pc = new PendingCircuit(player.getName(),type,delay);
         
         pendingCircuits.put(player, pc);
         
         return pc;
     }
     
     public static PendingCircuit getPendingCircuit(Player player){
         return pendingCircuits.get(player);
     }
     
     public static boolean hasPendingCircuit(Player player){
         return pendingCircuits.containsKey(player);
     }
     
     public static void removePendingCircuit(Player player){
         pendingCircuits.remove(player);
     }
     
 //Circuit Types
     public static boolean isValidCircuitType(String type){
         return circuitTypes.containsKey(type);
     }
     
     public static int getCircuitType(String sType){
         return circuitTypes.get(sType);
     }
     
     public static Map<String, Integer> getValidCircuitTypes(){
         return circuitTypes;
     }
     
 //1.2.3 circuits.yml Converter
     public void convertOldCircuitsYml(){
         File oldYmlFile = new File(plugin.getDataFolder(),"circuits.yml");
         if(oldYmlFile.exists()){
             plugin.log("Found circuits.yml, attempting to convert...");
 
             FileConfiguration oldYml = YamlConfiguration.loadConfiguration(oldYmlFile);
 
             for(String worldName : oldYml.getValues(false).keySet()){
                 ArrayList tempCircuitObjs = new ArrayList();
 
                 for (int x = 0;; x++){
                     String path = worldName + ".circuit_" + x;
                     
                     if (oldYml.get(path) == null) {
                         break;
                     }
 
                     Map<String,Object> tempCircuitObj = new HashMap<String,Object>();
                     
                     String[] senderXYZ = oldYml.get(path + ".sender").toString().split(",");
                     
                     tempCircuitObj.put("x", Integer.parseInt(senderXYZ[0]));
                     tempCircuitObj.put("y", Integer.parseInt(senderXYZ[1]));
                     tempCircuitObj.put("z", Integer.parseInt(senderXYZ[2]));
 
                     //they'll all be the same, should only ever be one anyway
                     String receiversType = oldYml.get(path+".type").toString();
                     
                    ArrayList tempReceiverObjs = new ArrayList();
                     for(Object receiver : oldYml.getList(path + ".receivers")){
                         Map<String,Object> tempReceiverObj = new HashMap<String,Object>();
                         
                         String[] sReceiverLoc = receiver.toString().split(",");
                         
                         tempReceiverObj.put("x", Integer.parseInt(sReceiverLoc[0]));
                         tempReceiverObj.put("y", Integer.parseInt(sReceiverLoc[1]));
                         tempReceiverObj.put("z", Integer.parseInt(sReceiverLoc[2]));
                         tempReceiverObj.put("d", 0);
                         tempReceiverObj.put("t", Integer.parseInt(receiversType));
 
                         tempReceiverObjs.add(tempReceiverObj);
                     }
                     
                     tempCircuitObj.put("r",tempReceiverObjs);
                     
                     tempCircuitObjs.add(tempCircuitObj);
                 }
                 
                 File newYmlFile = new File(plugin.getDataFolder(),worldName+".circuits.yml");
                 FileConfiguration newYml = YamlConfiguration.loadConfiguration(newYmlFile);
                 
                 newYml.set("fileVersion", 2);
                 newYml.set("circuits", tempCircuitObjs);
                 
                 try {
                     newYml.save(newYmlFile);
                 } catch (IOException ex) {
                     plugin.error("Unable to save "+newYmlFile.getName()+"!");
                     
                     Logger.getLogger(CircuitManager.class.getName()).log(Level.SEVERE, null, ex);
                 }
             }    
 
             //I dunno man. Java file operations are a mystery to me. These lines worked.
             File testFile = new File(plugin.getDataFolder(),"circuits.yml.bak");
             new File(plugin.getDataFolder(),"circuits.yml").renameTo(testFile);
         }
     }
 }
