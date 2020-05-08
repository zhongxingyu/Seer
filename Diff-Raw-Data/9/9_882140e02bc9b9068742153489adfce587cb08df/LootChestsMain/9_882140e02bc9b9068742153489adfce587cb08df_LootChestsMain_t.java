 package com.coffeejawa.LootChests;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.logging.Logger;
 
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.Chest;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class LootChestsMain extends JavaPlugin  {
     public final Logger logger = Logger.getLogger("Minecraft");
     
     private HashMap<CustomChest, Player> ChestPlayerMap;
     public HashMap<CustomChest, Player> getChestPlayerMap() {
         return ChestPlayerMap;
     }
     
     public Boolean hasChest(Location location){
         for( CustomChest c : getChestPlayerMap().keySet() ){
             if(c.getChestBlock().getLocation().equals(location)){
                 return true;
             }
         }
         return false;
     }
     
     public Player getChestOwner(Location location){
         for( CustomChest c : getChestPlayerMap().keySet() ){
             if(c.getChestBlock().getLocation().equals(location)){
                 return getChestPlayerMap().get(c);
             }
         }
         return null;
     }
     
     public CustomChest getChest(Location location){
         for( CustomChest c : getChestPlayerMap().keySet() ){
             if(c.getChestBlock().getLocation().equals(location)){
                 return c;
             }
         }
         return null;
     }
 
     @Override
     public void onEnable() 
     {        
         ChestPlayerMap = new HashMap<CustomChest, Player>();
         
         this.reloadConfig();
         
         getServer().getPluginManager().registerEvents(new ChestListener(this), this);
         
         logger.info("[" + this.getDescription().getName() +  "] enabled.");
         
         
         getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
 
             public void run() {
                 for( CustomChest cc : ChestPlayerMap.keySet()){
                     if(cc.hasTimeoutExpired()){
                         cc.tearDown();
                         ChestPlayerMap.remove(cc);
                     }
                 }
             }
          }, 0L, 100L);
         
 
         loadObjects();
     }
     
     @Override
     public void onDisable() 
     {
         logger.info("[" + this.getDescription().getName() +  "] disabled.");  
         this.saveConfig();
         
         saveObjects();
     }
 
     public Chest CreateChest(Player player, Location location, ArrayList<ItemStack> items, float timeout)
     {
         // if there are no items, why bother
         if(items.size() == 0)
             return null;
         
         // check for nearby chests, no double chests allowed
         
         Location locX1 = location.clone();
         Location locX2 = location.clone();
         Location locY1 = location.clone();
         Location locY2 = location.clone();
         
         locX1.setX(locX1.getX() - 1);
         locX2.setX(locX2.getX() + 1);
         locY1.setY(locY1.getY() - 1);
         locY2.setY(locY2.getY() + 1);
         
         ArrayList<Location> locList = new ArrayList<Location>(Arrays.asList(locX1,locX2,locY1,locY2));
         ArrayList<Chest> chestList = new ArrayList<Chest>(); 
         
         for( Location loc : locList ){
             if (loc.getBlock().getType() == Material.CHEST){
                 chestList.add((Chest) loc.getBlock().getState());
             }
         }
         Block b = null; 
         if( chestList.size() > 0 ){
             // UH OH SPAGHETTIOS
             // if this player owns one of the chests, add the items to that one instead
             // of spawning a new one.
             for( Chest c : chestList ){
                 if( getChestOwner(c.getLocation()) == player ){
                     b = c.getBlock();
                 }
             }
             // if this player does not own any of the chests, spawn a new one 1 block above
             if( b == null ){
                 while( b == null ){
                     location.setY(location.getY() + 1);
                     Block b2 = location.getBlock();
                     if( b2.getType() == Material.AIR ){
                         b = b2;
                     }
                 }
                 
             }
             
         }
         else {
             b = location.getWorld().getBlockAt(location);
         }
         
         if( b == null ){
             // no block, wtf??
             return null;
         }
         
         if( b.getType() != Material.AIR && b.getType() != Material.CHEST ){
             // not air, we can't create a block here
             return null;
         }
         
         // otherwise change block type from air to chest
         b.setType(Material.CHEST);
         
         // insert items
         Chest chest = (Chest) b.getState();
         for( ItemStack item : items ){
             chest.getBlockInventory().addItem(item);
         }
         
         // insert in our map
         if(!this.hasChest(b.getLocation())){
             ChestPlayerMap.put(new CustomChest(chest,timeout), player);
         }
         else{
             // reset chest cooldown
             getChest(b.getLocation()).SetTimeout(timeout);
         }
         return chest;
     }
     
     @SuppressWarnings("static-access")
     public void saveObjects(){
         
         // convert into serializable objects
         HashMap<SerializableLocation,String> newMap = new HashMap<SerializableLocation,String>();
         for( CustomChest chest : this.getChestPlayerMap().keySet() ){
             newMap.put(new SerializableLocation(chest.getChestBlock().getLocation()),getChestPlayerMap().get(chest).getName());
         }
         
         
         try {
            this.save(newMap, getDataFolder() + File.separator + "ChestPlayerMap.dat");
         } catch (Exception e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
     }
     
     @SuppressWarnings({ "static-access", "unchecked" })
     public void loadObjects(){
         HashMap<SerializableLocation,String> newMap = new HashMap<SerializableLocation,String>();
         try {
             newMap = (HashMap<SerializableLocation,String>) this.load(getDataFolder() + File.separator + "ChestPlayerMap.dat");
         } catch (Exception e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
         
         
         for( SerializableLocation sloc : newMap.keySet() ){
             String playername = newMap.get(sloc);
             Player player = getServer().getPlayer(playername);
             Block b = SerializableLocation.returnLocation(sloc).getBlock();
             CustomChest c = new CustomChest((Chest) b.getState(), 5);
             
             getChestPlayerMap().put(c, player);
         }
     }
     
 
     public static void save(Object obj,String path) throws Exception
     {
         ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(path));
         oos.writeObject(obj);
         oos.flush();
         oos.close();
     
     }
     public static Object load(String path) throws Exception
     {
         ObjectInputStream ois = new ObjectInputStream(new FileInputStream(path));
         Object result = ois.readObject();
         ois.close();
         return result;
     }
     }
 
 /* 
  * Requirements:
 *   -CreateChest
 *   -Remove spawned chests when empty
 *   -Remove spawned non-empty chests when timeout expires
 *   -Prevent unauthorized players from opening chests
 */
 
