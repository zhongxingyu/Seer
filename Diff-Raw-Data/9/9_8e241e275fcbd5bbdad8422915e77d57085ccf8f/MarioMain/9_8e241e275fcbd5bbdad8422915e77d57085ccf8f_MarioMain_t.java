 package littlegruz.marioworld;
 /* IMPORTANT: The Mario sound clips are not of my own creation, they are
  * instead made by the lovely folks at http://themushroomkingdom.net/ */
 
 /* I would like to thank WingedSpear from the Bukkit forums for some very
  * valuable feedback, suggestions as well as providing the translations to
  * Spanish. */
 
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Locale;
 import java.util.Map;
 import java.util.ResourceBundle;
 import java.util.Map.Entry;
 import java.util.StringTokenizer;
 import java.util.logging.Logger;
 
 import littlegruz.marioworld.commands.CheckpointStuff;
 import littlegruz.marioworld.commands.GameplayStuff;
 import littlegruz.marioworld.commands.LanguageStuff;
 import littlegruz.marioworld.commands.WarpStuff;
 import littlegruz.marioworld.commands.WorldStuff;
 import littlegruz.marioworld.entities.MarioBlock;
 import littlegruz.marioworld.entities.MarioPlayer;
 import littlegruz.marioworld.gui.MarioGUI;
 import littlegruz.marioworld.listeners.MarioBlockListener;
 import littlegruz.marioworld.listeners.MarioEntityListener;
 import littlegruz.marioworld.listeners.MarioPlayerListener;
 import littlegruz.marioworld.listeners.MarioScreenListener;
 import littlegruz.marioworld.listeners.MarioSpoutListener;
 
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.getspout.spoutapi.SpoutManager;
 
 public class MarioMain extends JavaPlugin{
    private Logger log = Logger.getLogger("This is MINECRAFT!");
    private HashMap<Location, MarioBlock> blockMap;
    private HashMap<String, MarioPlayer> playerMap;
    private HashMap<String, String> worldMap;
    private HashMap<String, String> lavaDeathMap;
    //private HashMap<String, String> invMap;
    private File blockFile;
    private File playerFile;
    private File worldFile;
    private File warpFile;
    private MarioGUI gui;
    private ResourceBundle currentRB;
    private Locale spanishLocale;
    private Locale aussieLocale;
    private Locale romanianLocale;
    private Locale nederlandsLocale;
    private Location firstWarp;
    private boolean marioDamage;
    private boolean spoutEnabled;
    private boolean coinPersistence;
    private int defaultLives;
    private int warpPlacement;
 
    public void onEnable(){
       // Create the directory and files if needed
       new File(getDataFolder().toString()).mkdir();
       blockFile = new File(getDataFolder().toString() + "/marioblocks.txt");
       playerFile = new File(getDataFolder().toString() + "/marioplayers.txt");
       worldFile = new File(getDataFolder().toString() + "/marioworlds.txt");
       warpFile = new File(getDataFolder().toString() + "/mariowarps.txt");
       
       spoutEnabled = getServer().getPluginManager().isPluginEnabled("Spout");
       
       log.info("Populating Mario HashMaps...");
       BufferedReader br;
       blockMap = new HashMap<Location, MarioBlock>();
       // Load up the blocks from file
       try{
          br = new BufferedReader(new FileReader(blockFile));
          String input;
          Location loc;
          MarioBlock mb;
          StringTokenizer st;
          
          // Load block file data into the block HashMap
          while((input = br.readLine()) != null){
             st = new StringTokenizer(input, " ");
             loc = new Location(getServer().getWorld(st.nextToken()), Double.parseDouble(st.nextToken()), Double.parseDouble(st.nextToken()), Double.parseDouble(st.nextToken()));
             mb = new MarioBlock(loc, Material.getMaterial(st.nextToken()), st.nextToken());
             if(Integer.parseInt(st.nextToken()) == 1)
                mb.setHit(true);
             blockMap.put(loc, mb);
             if(mb.getBlockType().compareTo("respawn") == 0){
                loc.getWorld().setSpawnLocation(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
                log.info("Custom respawn found");
             }
          }
          br.close();
          
       }catch(FileNotFoundException e){
          log.info("No original Mario block file, but that's okay! DON'T PANIC!");
       }catch(IOException e){
          log.info("Error reading Mario block file");
       }catch(Exception e){
          log.info("Incorrectly formatted Mario block file");
       }
       
       // Load up the warp blocks from file
       try{
          br = new BufferedReader(new FileReader(warpFile));
          String input;
          Location locEntry, locExit;
          MarioBlock mb;
          StringTokenizer st;
          
          // Load warp block file data into the block HashMap
          while((input = br.readLine()) != null){
             st = new StringTokenizer(input, " ");
             locEntry = new Location(getServer().getWorld(st.nextToken()), Double.parseDouble(st.nextToken()), Double.parseDouble(st.nextToken()), Double.parseDouble(st.nextToken()));
             locExit = new Location(getServer().getWorld(st.nextToken()), Double.parseDouble(st.nextToken()), Double.parseDouble(st.nextToken()), Double.parseDouble(st.nextToken()));
             mb = new MarioBlock(locExit, Material.getMaterial(st.nextToken()), st.nextToken());
             blockMap.put(locEntry, mb);
          }
          br.close();
          
       }catch(FileNotFoundException e){
          log.info("No original Mario warp block file, but that's okay! DON'T PANIC!");
       }catch(IOException e){
          log.info("Error reading Mario warp block file");
       }catch(Exception e){
          log.info("Incorrectly formatted Mario warp block file");
       }
 
       playerMap = new HashMap<String, MarioPlayer>();
       // Load up the players from file
       try{
          br = new BufferedReader(new FileReader(playerFile));
          String input, state;
          StringTokenizer st;
          Location loc;
          int coins, lives;
          
          // Load player file data into the player HashMap
          while((input = br.readLine()) != null){
             String name;
             st = new StringTokenizer(input, " ");
             name = st.nextToken();
             state = st.nextToken();
             coins = Integer.parseInt(st.nextToken());
             lives = Integer.parseInt(st.nextToken());
             loc = new Location(getServer().getWorld(st.nextToken()), Double.parseDouble(st.nextToken()), Double.parseDouble(st.nextToken()), Double.parseDouble(st.nextToken()));
             playerMap.put(name, new MarioPlayer(name, state, loc, coins, lives));
          }
          br.close();
          
       }catch(FileNotFoundException e){
          log.info("No original Mario player file found, but that's okay! DON'T PANIC!");
       }catch(IOException e){
          log.info("Error reading Mario player file");
       }catch(Exception e){
          log.info("Incorrectly formatted Mario player file");
       }
       
       worldMap = new HashMap<String, String>();
       // Load up the worlds from file
       try{
          br = new BufferedReader(new FileReader(worldFile));
          String input;
          
          // Load world file data into the world HashMap
          while((input = br.readLine()) != null){
             worldMap.put(input, input);
          }
          br.close();
          
       }catch(FileNotFoundException e){
          log.info("No original Mario world file found, but that's okay! DON'T PANIC!");
       }catch(IOException e){
          log.info("Error reading Mario world file");
       }catch(Exception e){
          log.info("Incorrectly formatted Mario world file");
       }
 
       // Set up the event listeners
       getServer().getPluginManager().registerEvents(new MarioPlayerListener(this), this);
       getServer().getPluginManager().registerEvents(new MarioBlockListener(this), this);
       getServer().getPluginManager().registerEvents(new MarioEntityListener(this), this);
       if(spoutEnabled){
          getServer().getPluginManager().registerEvents(new MarioScreenListener(this), this);
          getServer().getPluginManager().registerEvents(new MarioSpoutListener(this), this);
       }
 
       //Set up listeners
       getCommand("clearmariocheckpoint").setExecutor(new CheckpointStuff(this));
       getCommand("mariorestart").setExecutor(new GameplayStuff(this));
       getCommand("mariodamage").setExecutor(new GameplayStuff(this));
       getCommand("marioscore").setExecutor(new GameplayStuff(this));
       getCommand("keepmariocoins").setExecutor(new GameplayStuff(this));
       getCommand("changelanguage").setExecutor(new LanguageStuff(this));
       getCommand("addmarioworld").setExecutor(new WorldStuff(this));
       getCommand("removemarioworld").setExecutor(new WorldStuff(this));
       getCommand("displaywarppipes").setExecutor(new WarpStuff(this));
       getCommand("cancelwarppipe").setExecutor(new WarpStuff(this));
 
       spanishLocale = new Locale("spa", "ES");
       aussieLocale = new Locale("aus", "AU");
       romanianLocale = new Locale("ro", "ROU");
       nederlandsLocale = new Locale("nl", "NLD");
       
       warpPlacement = 0;
       firstWarp = null;
       lavaDeathMap = new HashMap<String, String>();
       
       // Pulling data from config.yml
       if(getConfig().isBoolean("damage"))
          marioDamage = getConfig().getBoolean("damage");
       else
          marioDamage = false;
       if(getConfig().isBoolean("coin_persistence"))
          coinPersistence = getConfig().getBoolean("coin_persistence");
       else
          coinPersistence = false;
       if(getConfig().isInt("lives"))
          defaultLives = getConfig().getInt("lives");
       else
          defaultLives = 3;
       if(getConfig().isString("language")){
          if(getConfig().getString("language").compareTo("english") == 0)
             currentRB = ResourceBundle.getBundle("littlegruz/marioworld/languages/language", Locale.ENGLISH);
          else if(getConfig().getString("language").compareTo("spanish") == 0)
             currentRB = ResourceBundle.getBundle("littlegruz/marioworld/languages/language", spanishLocale);
          else if(getConfig().getString("language").compareTo("aussie") == 0)
             currentRB = ResourceBundle.getBundle("littlegruz/marioworld/languages/language", aussieLocale);
          else if(getConfig().getString("language").compareTo("romanian") == 0)
             currentRB = ResourceBundle.getBundle("littlegruz/marioworld/languages/language", romanianLocale);
          else if(getConfig().getString("language").compareTo("german") == 0)
             currentRB = ResourceBundle.getBundle("littlegruz/marioworld/languages/language", Locale.GERMAN);
          else if(getConfig().getString("language").compareTo("dutch") == 0)
             currentRB = ResourceBundle.getBundle("littlegruz/marioworld/languages/language", nederlandsLocale);
       }
       else
          currentRB = ResourceBundle.getBundle("littlegruz/marioworld/languages/language", Locale.ENGLISH);
       
       if(spoutEnabled)
          gui = new MarioGUI(this);
       log.info(this.toString() + " enabled");
    }
 
    public void onDisable(){
       BufferedWriter bw;
       log.info("Saving Mario data...");
       try{
          int hit = 0;
          bw = new BufferedWriter(new FileWriter(blockFile));
          
          // Save all MarioBlocks to file
          Iterator<Map.Entry<Location, MarioBlock>> it = blockMap.entrySet().iterator();
          while(it.hasNext()){
             Entry<Location, MarioBlock> mb = it.next();
             if(mb.getValue().getBlockType().compareTo("warp") != 0){
                if(mb.getValue().isHit())
                   hit = 1;
                bw.write(mb.getValue().getLocation().getWorld().getName() + " "
                      + Double.toString(mb.getValue().getLocation().getX()) + " "
                      + Double.toString(mb.getValue().getLocation().getY()) + " "
                      + Double.toString(mb.getValue().getLocation().getZ()) + " "
                      + mb.getValue().getType().toString() + " "
                      + mb.getValue().getBlockType() + " " + hit + "\n");
             }
          }
          bw.close();
       }catch(IOException e){
          log.info("Error saving Mario blocks");
       }
       
       try{
          int hit = 0;
          bw = new BufferedWriter(new FileWriter(warpFile));
          
          // Save all warp blocks to file
          Iterator<Map.Entry<Location, MarioBlock>> it = blockMap.entrySet().iterator();
          while(it.hasNext()){
             Entry<Location, MarioBlock> mb = it.next();
             if(mb.getValue().getBlockType().compareTo("warp") == 0){
                if(mb.getValue().isHit())
                   hit = 1;
                bw.write(mb.getKey().getWorld().getName() + " "
                      + Double.toString(mb.getKey().getX()) + " "
                      + Double.toString(mb.getKey().getY()) + " "
                      + Double.toString(mb.getKey().getZ()) + " "
                      + mb.getValue().getLocation().getWorld().getName() + " "
                      + Double.toString(mb.getValue().getLocation().getX()) + " "
                      + Double.toString(mb.getValue().getLocation().getY()) + " "
                      + Double.toString(mb.getValue().getLocation().getZ()) + " "
                      + mb.getValue().getType().toString() + " "
                      + mb.getValue().getBlockType() + " " + hit + "\n");
             }
          }
          bw.close();
       }catch(IOException e){
          log.info("Error saving Mario warp blocks");
       }
       
       try{
          bw = new BufferedWriter(new FileWriter(playerFile));
          Iterator<Map.Entry<String, MarioPlayer>> it = playerMap.entrySet().iterator();
          
          // Save all MarioPlayers to file
          while(it.hasNext()){
             Entry<String, MarioPlayer> mp = it.next();
             bw.write(mp.getValue().getPlayaName() + " "
                   + mp.getValue().getState() + " "
                   + Integer.toString(mp.getValue().getCoins()) + " "
                   + Integer.toString(mp.getValue().getLives()) + " "
                   + mp.getValue().getCheckpoint().getWorld().getName() + " "
                   + Double.toString(mp.getValue().getCheckpoint().getX()) + " "
                   + Double.toString(mp.getValue().getCheckpoint().getY()) + " "
                   + Double.toString(mp.getValue().getCheckpoint().getZ()) + "\n");
          }
          bw.close();
       }catch(IOException e){
          log.info("Error saving Mario players");
       }
       
       try{
          bw = new BufferedWriter(new FileWriter(worldFile));
          Iterator<Map.Entry<String, String>> it = worldMap.entrySet().iterator();
          
          // Save all world names to file
          while(it.hasNext()){
             Entry<String, String> mp = it.next();
             bw.write(mp.getValue() + "\n");
          }
          bw.close();
       }catch(IOException e){
          log.info("Error saving Mario worlds");
       }
       
       this.getConfig().set("lives", defaultLives);
       saveConfig();
       log.info(this.toString() + " disabled");
    }
    
    public String clearCheckpoint(String name, String worldName){
       if(playerMap.get(name) != null){
          playerMap.get(name).setCheckpoint(getServer().getWorld(worldName).getSpawnLocation());
          return name + " " + currentRB.getString("PlayerCheckpointResetP2");
       } else
          return name + " " + currentRB.getString("PlayerCheckpointNotResetP2");
    }
    
    public void deathSequence(Player playa){
       MarioPlayer mp = playerMap.get(playa.getName());
       mp.setLives(mp.getLives() - 1);
       
       if(mp.getLives() == 0){
          //Play gameover music and display a large "Game Over" label
          //plugin.getServer().getPlayer(mp.getPlayaName()).sendMessage("Game Over");
          //plugin.getGui().placeGameOver(event.getPlayer());
          clearCheckpoint(mp.getPlayaName(), playa.getWorld().getName());
         if(!coinPersistence)
             mp.setCoins(0);
          // File size 164KB
          if(spoutEnabled)
             SpoutManager.getSoundManager().playCustomMusic(this, SpoutManager.getPlayer(playa), "http://sites.google.com/site/littlegruzsplace/download/smb_gameover.wav", true);
       }else{
          // File size 118KB
          if(spoutEnabled)
             SpoutManager.getSoundManager().playCustomMusic(this, SpoutManager.getPlayer(playa), "http://sites.google.com/site/littlegruzsplace/download/smb_mariodie.wav", true);
       }
       lavaDeathMap.put(playa.getName(), "badger");
    }
    
    public HashMap<Location, MarioBlock> getBlockMap(){
       return blockMap;
    }
 
    public HashMap<String, MarioPlayer> getPlayerMap(){
       return playerMap;
    }
 
    public HashMap<String, String> getWorldMap(){
       return worldMap;
    }
 
    public boolean isMarioDamage(){
       return marioDamage;
    }
 
    public void setMarioDamage(boolean marioDamage){
       this.marioDamage = marioDamage;
    }
    
    public String getPlayerWorld(String name){
       return getServer().getPlayer(name).getWorld().getName();
    }
 
    public MarioGUI getGui(){
       return gui;
    }
 
    public boolean isSpoutEnabled(){
       return spoutEnabled;
    }
    
    public int getDefaultLives(){
       return defaultLives;
    }
    
    public void setCurrentRB(ResourceBundle bundle) {
       currentRB = bundle;
    }
 
     public ResourceBundle getCurrentRB() {
       return currentRB;
    }
 
    public Locale getSpanishLocale(){
       return spanishLocale;
    }
 
    public Locale getAussieLocale(){
       return aussieLocale;
    }
 
    public Locale getRomanianLocale(){
       return romanianLocale;
    }
 
    public Locale getNederlandsLocale(){
       return nederlandsLocale;
    }
 
    public int getWarpPlacement(){
       return warpPlacement;
    }
 
    public void setWarpPlacement(int warpPlacement){
       this.warpPlacement = warpPlacement;
    }
 
    public Location getFirstWarp(){
       return firstWarp;
    }
 
    public void setFirstWarp(Location firstWarp){
       this.firstWarp = firstWarp;
    }
 
    public HashMap<String, String> getLavaDeathMap(){
       return lavaDeathMap;
    }
 
    public boolean isCoinPersistence(){
       return coinPersistence;
    }
 
    public void setCoinPersistence(boolean coinPersistence){
       this.coinPersistence = coinPersistence;
    }
 }
