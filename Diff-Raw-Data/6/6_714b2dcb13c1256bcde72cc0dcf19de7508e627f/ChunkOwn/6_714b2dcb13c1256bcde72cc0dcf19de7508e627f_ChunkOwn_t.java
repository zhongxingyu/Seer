 package com.codisimus.plugins.chunkown;
 
 import java.io.*;
 import java.util.*;
 import net.milkbowl.vault.economy.Economy;
 import net.milkbowl.vault.permission.Permission;
 import org.bukkit.Chunk;
 import org.bukkit.Server;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.RegisteredServiceProvider;
 import org.bukkit.plugin.java.JavaPlugin;
 
 /**
  * Loads Plugin and manages Data/Permissions
  * 
  * @author Codisimus
  */
 public class ChunkOwn extends JavaPlugin {
     static Server server;
     static Permission permission;
     private static PluginManager pm;
     private Properties p;
     static int lowerLimit;
     private static HashMap<Chunk, OwnedChunk> ownedChunks = new HashMap<Chunk, OwnedChunk>();
     public static HashMap<String, Integer> chunkCounter = new HashMap<String, Integer>();
     static int groupSize;
     private static int disownTime;
     private static boolean revertChunks;
     private boolean disablePistons;
     static Properties lastDaySeen;
     private static String dataFolder;
 
     @Override
     public void onDisable () {
     }
 
     /**
      * Calls methods to load this Plugin when it is enabled
      *
      */
     @Override
     public void onEnable () {
         server = getServer();
         pm = server.getPluginManager();
         
         File dir = this.getDataFolder();
         if (!dir.isDirectory())
             dir.mkdir();
         
         dataFolder = dir.getPath();
         
         dir = new File(dataFolder+"/OwnedChunks");
         if (!dir.isDirectory())
             dir.mkdir();
         
         dir = new File(dataFolder+"/Chunk Snapshots");
         if (!dir.isDirectory())
             dir.mkdir();
         
         loadSettings();
         
         //Find Permissions
         RegisteredServiceProvider<Permission> permissionProvider =
                 getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
         if (permissionProvider != null)
             permission = permissionProvider.getProvider();
         
         //Find Economy
         RegisteredServiceProvider<Economy> economyProvider =
                 getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
         if (economyProvider != null)
             Econ.economy = economyProvider.getProvider();
         
         loadAll();
         
         loadLastSeen();
         
         //Register Events
         pm.registerEvents(new ChunkOwnListener(), this);
         if (disablePistons)
             pm.registerEvents(new ChunkOwnPistonListener(), this);
         
         //Register the command found in the plugin.yml
         String commands = this.getDescription().getCommands().toString();
         ChunkOwnCommand.command = commands.substring(1, commands.indexOf("="));
         getCommand(ChunkOwnCommand.command).setExecutor(new ChunkOwnCommand());
         
         //Start the tickListener if there is an AutoDisownTimer
         if (disownTime > 0)
             tickListener();
         
         System.out.println("ChunkOwn "+this.getDescription().getVersion()+" is enabled!");
     }
     
     /**
      * Loads settings from the config.properties file
      * 
      */
     public void loadSettings() {
         try {
             //Copy the file from the jar if it is missing
             File file = new File(dataFolder+"/config.properties");
             if (!file.exists())
                 this.saveResource("config.properties", true);
             
             //Load config file
             p = new Properties();
             FileInputStream fis = new FileInputStream(file);
             p.load(fis);
             
             Econ.buyPrice = Double.parseDouble(loadValue("BuyPrice"));
             Econ.sellPrice = Double.parseDouble(loadValue("SellPrice"));
             Econ.buyMultiplier = Double.parseDouble(loadValue("BuyMultiplier"));
             Econ.sellMultiplier = Double.parseDouble(loadValue("SellMultiplier"));
             
             groupSize = Integer.parseInt(loadValue("MinimumGroupSize"));
             
             lowerLimit = Integer.parseInt(loadValue("OwnLowerLimit"));
             
             ChunkOwnCommand.cornerID = Integer.parseInt(loadValue("CornerBlockID"));
             
             ChunkOwnCommand.cooldown = Integer.parseInt(loadValue("PreviewCooldown"));
             
             disownTime = Integer.parseInt(loadValue("AutoDisownTimer"));
             
             revertChunks = Boolean.parseBoolean(loadValue("RevertChunks"));
             
             disablePistons = Boolean.parseBoolean(loadValue("DisableUseOfPistonsInOwnedChunks"));
             
             ChunkOwnMessages.permission = loadValue("PermissionMessage");
             ChunkOwnMessages.doNotOwn = loadValue("DoNotOwnMessage");
             ChunkOwnMessages.claimed = loadValue("AlreadyClaimedMessage");
             ChunkOwnMessages.limit = loadValue("LimitReachedMessage");
             ChunkOwnMessages.unclaimed = loadValue("UnclaimedMessage");
             ChunkOwnMessages.buyFree = loadValue("BuyFreeMessage");
             ChunkOwnMessages.insufficientFunds = loadValue("InsufficientFundsMessage");
             ChunkOwnMessages.buy = loadValue("BuyMessage");
             ChunkOwnMessages.sell = loadValue("SellMessage");
             ChunkOwnMessages.adminSell = loadValue("AdminSellMessage");
             ChunkOwnMessages.adminSold = loadValue("SoldByAdminMessage");
             ChunkOwnMessages.groupLand = loadValue("MustGroupLandMessage");
             ChunkOwnMessages.formatAll();
             
             fis.close();
         }
         catch (Exception e) {
         }
     }
 
     /**
      * Loads the given key and prints an error message if the key is missing
      *
      * @param key The key to be loaded
      * @return The String value of the loaded key
      */
     private String loadValue(String key) {
         //Print an error if the key is not found
         if (!p.containsKey(key)) {
             System.err.println("[ChunkOwn] Missing value for "+key+" in config file");
             System.err.println("[ChunkOwn] Please regenerate config file");
         }
         
         return p.getProperty(key);
     }
     
     /**
      * Returns true if the given Player has the specific permission
      * 
      * @param player The Player who is being checked for permission
      * @param type The String of the permission, ex. admin
      * @return True if the given Player has the specific permission
      */
     static boolean hasPermission(String player, String type) {
         return permission.has(server.getWorlds().get(0), player, "chunkown."+type);
     }
 
     /**
      * Returns true if the given Player has the specific permission
      * 
      * @param player The Player who is being checked for permission
      * @param type The String of the permission, ex. admin
      * @return True if the given Player has the specific permission
      */
     public static boolean hasPermission(Player player, String type) {
         return permission.has(player, "chunkown."+type);
     }
 
     /**
      * Returns the Integer value that is the limit of Chunks the given Player can own
      * Returns -1 if there is no limit
      *
      * @param player The Player who is being checked for a limit
      * @return The Integer value that is the limit of Chunks the given Player can own
      */
     public static int getOwnLimit(Player player) {
         //Check for the unlimited node first
         if (hasPermission(player, "limit.-1"))
             return -1;
 
         //Start at 100 and work down until a limit node is found
         for (int i = 100; i >= 0; i--)
             if (hasPermission(player, "limit."+i))
                 return i;
 
         //No limit if a limit node is not found
         return -1;
     }
     
     /**
      * Returns true if Player has permission to build*
      * *build also refers to many griefing events**
      * **all events can be found in Listener Classes
      * 
      * @param player The Player who is trying to build
      * @param block The Block the ChunkOwnListener is modifying
      * @return True if Player has permission to 'build'
      */
     public static boolean canBuild(Player player, Block block) {
         //Allow building if Player has admin node
         if (player != null && hasPermission(player, "admin"))
             return true;
         
         //Allow building if below the lower limit
         if (block.getY() < ChunkOwn.lowerLimit)
             return true;
         
         String world = block.getWorld().getName();
         Chunk chunk = block.getChunk();
         int x = chunk.getX();
         int z = chunk.getZ();
         
         //See if the area is owned by a Player
         OwnedChunk ownedChunk = findOwnedChunk(world, x, z);
         
         //If unowned, deny building if the Player cannot build on unclaimed land
         if (ownedChunk == null)
             if (player != null && hasPermission(player, "mustowntobuild")) {
                 player.sendMessage(ChunkOwnMessages.doNotOwn);
                 return false;
             }
             else
                 return true;
         
         //Deny building if no Player can be determined
         if (player == null)
             return false;
         
         //Allow building if the Player is an Owner or CoOwner of the land
         if (ownedChunk.owner.equals(player.getName()))
             return true;
         else if (ownedChunk.isCoOwner(player))
             return true;
         else {
             player.sendMessage(ChunkOwnMessages.doNotOwn);
             return false;
         }
     }
 
     /**
      * Reads save file to load ChunkOwn data for all Worlds
      *
      */
     private static void loadAll() {
         for (World world: server.getWorlds())
             loadData(world);
         
         if (ownedChunks.isEmpty())
             loadDataOld();
     }
     
     /**
      * Reads save file to load ChunkOwn data for given World
      *
      */
     private static void loadData(World world) {
         try {
             String worldName = world.getName();
             
             //Open save file in BufferedReader
             File file = new File(dataFolder+"/OwnedChunks/"+worldName+".coc");
             if (!file.exists())
                 return;
             BufferedReader bReader = new BufferedReader(new FileReader(file));
 
             //Convert each line into data until all lines are read
             String line;
             while ((line = bReader.readLine()) != null) {
                 try {
                     String[] data = line.split(";");
 
                     //Construct a new OwnedChunk using the World name and x/z coordinates
                     OwnedChunk ownedChunk = new OwnedChunk(worldName, Integer.parseInt(data[0]), Integer.parseInt(data[1]));
 
                     //Set the Owner and increment the count of how many Chunks the Player owns
                     ownedChunk.owner = data[2];
                     int owned = 0;
                     if (chunkCounter.containsKey(ownedChunk.owner))
                         owned = chunkCounter.get(ownedChunk.owner);
                     chunkCounter.put(ownedChunk.owner, owned + 1);
 
                     //Convert the coOwners data into a LinkedList for the OwnedChunk
                     if (!data[3].equals("none")) 
                         ownedChunk.coOwners = new LinkedList<String>(Arrays.asList(data[3].split(",")));
 
                     //Convert the groups data into a LinkedList for the OwnedChunk
                     if (!data[4].equals("none")) 
                         ownedChunk.groups = new LinkedList<String>(Arrays.asList(data[4].split(",")));
                     
                     addOwnedChunk(ownedChunk);
                 }
                 catch (Exception corruptedData) {
                     /* Do not load this line */
                 }
             }
             
             bReader.close();
         }
         catch (Exception loadFailed) {
             System.err.println("[ChunkOwn] Load Failed!");
             loadFailed.printStackTrace();
         }
     }
     
     /**
      * Reads save file to load ChunkOwn data
      *
      */
     private static void loadDataOld() {
         try {
             //Open save file in BufferedReader
             File file = new File(dataFolder+"/chunkown.save");
             if (!file.exists())
                 return;
             
             System.out.println("[ChunkOwn] Converting old save file");
             BufferedReader bReader = new BufferedReader(new FileReader(file));
 
             //Convert each line into data until all lines are read
             String line;
             while ((line = bReader.readLine()) != null) {
                 try {
                     String[] data = line.split(";");
                     
                     World world = server.getWorld(data[0]);
                     if (world == null)
                         continue;
 
                     //Construct a new OwnedChunk using the World name and x/z coordinates
                     OwnedChunk ownedChunk = new OwnedChunk(data[0], Integer.parseInt(data[1]), Integer.parseInt(data[2]));
 
                     //Set the Owner and increment the count of how many Chunks the Player owns
                     ownedChunk.owner = data[3];
                     int owned = 0;
                     if (chunkCounter.containsKey(ownedChunk.owner))
                         owned = chunkCounter.get(ownedChunk.owner);
                     chunkCounter.put(ownedChunk.owner, owned + 1);
 
                     //Convert the coOwners data into a LinkedList for the OwnedChunk
                     if (!data[4].equals("none")) 
                         ownedChunk.coOwners = new LinkedList<String>(Arrays.asList(data[4].split(",")));
 
                     //Convert the groups data into a LinkedList for the OwnedChunk
                     if (!data[5].equals("none")) 
                         ownedChunk.groups = new LinkedList<String>(Arrays.asList(data[5].split(",")));
                     
                     addOwnedChunk(ownedChunk);
                 }
                 catch (Exception corruptedData) {
                     /* Do not load this line */
                 }
             }
             
             bReader.close();
             
             saveAll();
         }
         catch (Exception loadFailed) {
             System.err.println("[ChunkOwn] Load Failed!");
             loadFailed.printStackTrace();
         }
     }
     
     /**
      * Loads last seen data from file
      * 
      */
     public static void loadLastSeen() {
         lastDaySeen = new Properties();
         try {
             //Create the file if it does not exist
             File file = new File(dataFolder+"/lastseen.properties");
             File oldFile = new File(dataFolder+"/lastseen.map");
             if (oldFile.exists())
                 oldFile.renameTo(file);
             
             FileInputStream fis = new FileInputStream(file);
             lastDaySeen.load(fis);
             fis.close();
         }
         catch (Exception ex) {
         }
     }
     
     /**
      * Saves data for all Worlds
      * 
      */
     public static void saveAll() {
         for (World world: server.getWorlds())
             save(world);
     }
     
     /**
      * Writes data for specified World to save file
      * Old file is overwritten
      */
     public static void save(World world) {
         try {
             String worldName = world.getName();
             LinkedList<OwnedChunk> toSave = new LinkedList<OwnedChunk>();
             for (OwnedChunk ownedChunk: ownedChunks.values())
                 if (ownedChunk.world.equals(worldName))
                     toSave.add(ownedChunk);
             
             if (toSave.isEmpty())
                 return;
             
             //Open save file for writing data
             File file = new File(dataFolder+"/OwnedChunks/"+world.getName()+".coc");
             if (!file.exists())
                 file.createNewFile();
             BufferedWriter bWriter = new BufferedWriter(new FileWriter(file));
             
             //Iterate through all OwnedChunks to write each to the file
             for (OwnedChunk ownedChunk: toSave) {
                 //Write data in format "x;z;owner;coOwner1,coOwner2,...;group1,group2,...;
                 bWriter.write(ownedChunk.x+";");
                 bWriter.write(ownedChunk.z+";");
                 bWriter.write(ownedChunk.owner.concat(";"));
 
                 if (ownedChunk.coOwners.isEmpty())
                     bWriter.write("none");
                 else
                     for (String coOwner: ownedChunk.coOwners)
                         bWriter.write(coOwner.concat(","));
                 bWriter.write(";");
 
                 if (ownedChunk.groups.isEmpty())
                     bWriter.write("none");
                 else
                     for (String group: ownedChunk.groups)
                         bWriter.write(group.concat(","));
                 bWriter.write(";");
 
                 //Write each OwnedChunk on its own line
                 bWriter.newLine();
             }
             
             bWriter.close();
         }
         catch (Exception saveFailed) {
             System.err.println("[ChunkOwn] Save Failed!");
             saveFailed.printStackTrace();
         }
     }
     
     /**
      * Writes the Map of last seen data to the save file
      * Old file is over written
      */
     public static void saveLastSeen() {
         try {
             lastDaySeen.store(new FileOutputStream(dataFolder.concat("/lastseen.properties")), null);
         }
         catch (Exception ex) {
         }
     }
     
     /**
      * Returns the OwnedChunk object for the given Chunk
      * returns null if the Chunk is not claimed
      * 
      * @param world The name of the World the OwnedChunk is in
      * @param x The x-coordinate of the OwnedChunk
      * @param z The z-coordinate of the OwnedChunk
      * @return The OwnedChunk object for the given Chunk
      */
     public static OwnedChunk findOwnedChunk(String world, int x, int z) {
         return findOwnedChunk(server.getWorld(world).getChunkAt(x, z));
     }
     
     /**
      * Returns the OwnedChunk object for the given Chunk
      * returns null if the Chunk is not claimed
      * 
      * @param chunk The Chunk that the OwnedChunk would represent
      */
     public static OwnedChunk findOwnedChunk(Chunk chunk) {
         return ownedChunks.get(chunk);
     }
     
     /**
      * Adds the OwnedChunk from the saved data
      * 
      * @param ownedChunk The OwnedChunk to add
      */
     public static void addOwnedChunk(OwnedChunk ownedChunk) {
         ownedChunk.saveSnapshot();
        
        if (chunkCounter.containsKey(ownedChunk.owner))
            chunkCounter.put(ownedChunk.owner, chunkCounter.get(ownedChunk.owner) + 1);
        else
            chunkCounter.put(ownedChunk.owner, 1);
         
         World world = server.getWorld(ownedChunk.world);
         Chunk chunk = world.getChunkAt(ownedChunk.x, ownedChunk.z);
         ownedChunks.put(chunk, ownedChunk);
         save(world);
     }
     
     /**
      * Removes the OwnedChunk from the saved data
      * 
      * @param ownedChunk The OwnedChunk to be removed
      */
     public static void removeOwnedChunk(OwnedChunk ownedChunk) {
         removeOwnedChunk(ownedChunk.world, ownedChunk.x, ownedChunk.z);
     }
     
     /**
      * Removes the OwnedChunk from the saved data
      * 
      * @param world The name of the World the OwnedChunk is in
      * @param x The x-coordinate of the OwnedChunk
      * @param z The z-coordinate of the OwnedChunk
      */
     public static void removeOwnedChunk(String world, int x, int z) {
         removeOwnedChunk(server.getWorld(world).getChunkAt(x, z));
     }
     
     /**
      * Removes the OwnedChunk from the saved data
      * 
      * @param chunk The Chunk that the OwnedChunk would represent
      */
     public static void removeOwnedChunk(Chunk chunk) {
         OwnedChunk ownedChunk = ownedChunks.get(chunk);
         ownedChunk.revert();
         chunkCounter.put(ownedChunk.owner, chunkCounter.get(ownedChunk.owner) - 1);
         
         ownedChunks.remove(chunk);
         save(chunk.getWorld());
     }
     
     /**
      * Retrieves a list of Chunks that the given Player owns
      * 
      * @param player The name of the given Player
      * @return The list of Chunks
      */
     public static LinkedList<Chunk> getOwnedChunks(String player) {
         //Retrieve the ChunkCounter value of the Player
         int owned = ChunkOwn.chunkCounter.get(player);
             
         LinkedList<Chunk> chunks = new LinkedList<Chunk>();
         
         if (owned == 0)
             return chunks;
         
         for (OwnedChunk chunk: ownedChunks.values())
             if (chunk.owner != null && chunk.owner.equals(player)) {
                 chunks.add(server.getWorld(chunk.world).getChunkAt(chunk.x, chunk.z));
                 owned--;
 
                 if (owned == 0)
                     return chunks;
             }
         
         return chunks;
     }
     
     /**
      * Returns a Collection of all OwnedChunks
      * 
      * @return A Collection of all OwnedChunks
      */
     public static Collection<OwnedChunk> getOwnedChunks() {
         return ownedChunks.values();
     }
     
     /**
      * Returns true if all connected Owned Chunks are grouped in sizes that equal or exceed the chunkSize
      * True is returned if specific group sizes are not required
      * True is returned if the ChunkList is empty
      * 
      * @return true if all Chunk groups are large enough
      */
     public static boolean canBuyLoner(LinkedList<Chunk> chunkList) {
         //Return true if specific group sizes are not required
         if (groupSize < 2)
             return true;
         
         while (!chunkList.isEmpty()) {
             //Return false if the group size is not large enough
             if (getSize(chunkList, chunkList.removeFirst()) < groupSize)
                 return false;
         }
         
         return true;
     }
     
     /**
      * Returns the total amount of connected Chunks that this Chunk is included in
      * A Chunk is connected if it is adjacent to one of the 4 sides and has the same owner
      * 
      * @return The Chunk Size
      */
     private static int getSize(LinkedList<Chunk> owned, Chunk chunk) {
         int size = 1;
         World world = chunk.getWorld();
         int x = chunk.getX();
         int z = chunk.getZ();
         
         //Compass directions may not be accurate but you get the idea
         //Check for connected Chunks to the North
         chunk = world.getChunkAt(x + 1, z);
         if (owned.remove(chunk))
             size = size + getSize(owned, chunk);
         
         //Check for connected Chunks to the East
         chunk = world.getChunkAt(x, z + 1);
         if (owned.remove(chunk))
             size = size + getSize(owned, chunk);
         
         //Check for connected Chunks to the South
         chunk = world.getChunkAt(x - 1, z);
         if (owned.remove(chunk))
             size = size + getSize(owned, chunk);
         
         //Check for connected Chunks to the West
         chunk = world.getChunkAt(x, z - 1);
         if (owned.remove(chunk))
             size = size + getSize(owned, chunk);
         
         return size;
     }
     
     /**
      * Returns the number of the current day in the AD time period
      * 
      * @return The number of the current day in the AD time period
      */
     public static int getDayAD() {
         Calendar calendar = Calendar.getInstance();
         int yearAD = calendar.get(Calendar.YEAR);
         int dayOfYear = calendar.get(Calendar.DAY_OF_YEAR);
         return (int)((yearAD - 1) * 365.4) + dayOfYear;
     }
     
     /**
      * Checks for Players who have not logged on within the given amount of time
      * These Players will have their OwnedChunks automatically disowned
      * Players that do not have any Owned Chunks are ignored
      */
     public void tickListener() {
         //Repeat every day
     	server.getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
             @Override
     	    public void run() {
                 int cutoffDay = getDayAD() - disownTime;
                 
                 for (String key: lastDaySeen.stringPropertyNames())
                     if (Integer.parseInt(lastDaySeen.getProperty(key)) < cutoffDay) {
                         System.out.println("[ChunkOwn] Clearing Chunks that are owned by "+key);
                         ChunkOwnCommand.clear(key);
                         lastDaySeen.remove(key);
                         saveLastSeen();
                     }
     	    }
     	}, 0L, 1728000L);
     }
     
     /**
      * Saves a snapshot of the given Chunk
      * 
      */
     public static void saveSnapshot(Chunk chunk) {
         saveSnapshot(chunk.getWorld().getName(), chunk.getX(), chunk.getZ());
     }
     
     /**
      * Saves a snapshot of the given chunk
      * 
      */
     public static void saveSnapshot(String chunkWorld, int chunkX, int chunkZ) {
         if (!revertChunks)
             return;
         
         try {
             File file = new File(dataFolder+"/Chunk Snapshots/"+chunkWorld);
             if (!file.isDirectory())
                 file.mkdirs();
             
             file = new File(dataFolder+"/Chunk Snapshots/"+chunkWorld+"/"+chunkX+"-"+chunkZ+".cota");
             if (file.exists())
                 return;
             else
                 file.createNewFile();
             
             World world = server.getWorld(chunkWorld);
             int maxHeight = world.getMaxHeight();
 
             int size = 256 * (maxHeight - lowerLimit);
 
             byte[] typeArray = new byte[size];
             byte[] dataArray = new byte[size];
             int index = 0;
 
             int x = 16 * chunkX;
             int z = 16 * chunkZ;
 
             for (int i = 0; i < 16; i++)
                 for (int j = 0; j < 16; j++) {
                     int y = world.getHighestBlockYAt(x, z);
                     if (y >= lowerLimit)
                         index = index + maxHeight - 1 - y;
 
                     while (y >= lowerLimit) {
                         byte type = (byte)world.getBlockAt(x + i, y, z + j).getTypeId();
                         typeArray[index] = type;
                         
                         byte data = world.getBlockAt(x + i, y, z + j).getData();
                         dataArray[index] = data;
                         
                         index++;
                         y--;
                     }
                 }
         
             FileOutputStream fos = new FileOutputStream(file);
             fos.write(typeArray);
             fos.close();
             
             file = new File(dataFolder+"/Chunk Snapshots/"+chunkWorld+"/"+chunkX+"-"+chunkZ+".coda");
             if (!file.exists())
                 file.createNewFile();
             
             fos = new FileOutputStream(file);
             fos.write(dataArray);
             fos.close();
         }
         catch (Exception ex) {
             System.err.println("[ChunkOwn] Error when saving Chunk Snapshot...");
             ex.printStackTrace();
         }
     }
     
     /**
      * Reverts given Chunk back to it's saved snapshot
      * 
      */
     public static void revertChunk(Chunk chunk) {
         revertChunk(chunk.getWorld().getName(), chunk.getX(), chunk.getZ());
     }
     
     /**
      * Reverts given Chunk back to it's saved snapshot
      * 
      */
     public static void revertChunk(String chunkWorld, int chunkX, int chunkZ) {
         if (!revertChunks)
             return;
         
         try {
             File file = new File(dataFolder+"/Chunk Snapshots/"+chunkWorld+"/"+chunkX+"-"+chunkZ+".cota");
             if (!file.exists()) {
                 System.out.println("[ChunkOwn] Unable to revert Chunk '"+chunkWorld+","+chunkX+","+chunkZ+" because no snapshot was found.");
                 return;
             }
             
             int length = (int)file.length();
             
             byte[] typeArray = new byte[length];
             byte[] dataArray = new byte[length];
             
             InputStream is = new FileInputStream(file);
             
             int offset = 0;
             int numRead = 0;
             while (offset < length && (numRead = is.read(typeArray, offset, length - offset)) >= 0)
                 offset += numRead;
 
             // Ensure all the bytes have been read in
             if (offset < length)
                 throw new IOException("Could not completely read file "+file.getName());
 
             // Close the input stream and return bytes
             is.close();
             
             file = new File(dataFolder+"/Chunk Snapshots/"+chunkWorld+"/"+chunkX+"-"+chunkZ+".coda");
             if (!file.exists()) {
                 System.out.println("[ChunkOwn] Unable to revert Chunk '"+chunkWorld+","+chunkX+","+chunkZ+" because no snapshot was found.");
                 return;
             }
             
             is = new FileInputStream(file);
             
             offset = 0;
             numRead = 0;
             while (offset < length && (numRead = is.read(dataArray, offset, length - offset)) >= 0)
                 offset += numRead;
 
             // Ensure all the bytes have been read in
             if (offset < length)
                 throw new IOException("Could not completely read file "+file.getName());
 
             // Close the input stream and return bytes
             is.close();
             
             World world = server.getWorld(chunkWorld);
             int maxHeight = world.getMaxHeight();
             
             int lowerLimit = maxHeight - (length / 256);
 
             int index = 0;
 
             int x = 16 * chunkX;
             int z = 16 * chunkZ;
 
             for (int i = 0; i < 16; i++)
                 for (int j = 0; j < 16; j++)
                     for (int y = maxHeight - 1; y >= lowerLimit; y--) {
                         Block block = world.getBlockAt(x + i, y, z + j);
                         block.setTypeIdAndData((int)typeArray[index], dataArray[index], true);
                         
                         index++;
                     }
             
             file.delete();
             new File(dataFolder+"/Chunk Snapshots/"+chunkWorld+"/"+chunkX+"-"+chunkZ+".cota").delete();
         }
         catch (Exception ex) {
             System.err.println("[ChunkOwn] Error when reverting Chunk from Snapshot...");
             ex.printStackTrace();
         }
     }
 }
