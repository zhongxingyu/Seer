 package littlegruz.autoruncommands;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.StringTokenizer;
 import java.util.Map.Entry;
 import java.util.UUID;
 import java.util.logging.Logger;
 
 import littlegruz.autoruncommands.commands.Blocks;
 import littlegruz.autoruncommands.commands.Commands;
 import littlegruz.autoruncommands.commands.Death;
 import littlegruz.autoruncommands.commands.Repeat;
 import littlegruz.autoruncommands.commands.Respawn;
 import littlegruz.autoruncommands.commands.RightClick;
 import littlegruz.autoruncommands.commands.Startup;
 import littlegruz.autoruncommands.listeners.CommandBlockListener;
 import littlegruz.autoruncommands.listeners.CommandEntityListener;
 import littlegruz.autoruncommands.listeners.CommandPlayerListener;
 import littlegruz.autoruncommands.listeners.CommandServerListener;
 
 import org.bukkit.Location;
 import org.bukkit.command.CommandSender;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class CommandMain extends JavaPlugin{
    Logger log = Logger.getLogger("This is MINECRAFT!");
    private HashMap<String, String> playerCommandMap;
    private HashMap<Location, String> blockCommandMap;
    private HashMap<String, String> commandMap;
    private HashMap<String, Location> playerPosMap;
    private HashMap<String, String> deathCommandMap;
    private HashMap<String, String> respawnCommandMap;
    private HashMap<String, Integer> repeatCommandMap;
    private HashMap<String, Integer> runningRepeatCommandMap;
    private File playerFile;
    private File commandFile;
    private File blockFile;
    private File deathFile;
    private File respawnFile;
    private File startupFile;
    private File repeatFile;
    private boolean placeBlock;
    private boolean startupDone;
    private String blockCommand;
    private String startupCommands;
 
    public void onDisable(){
       //Save player data
       try{
          BufferedWriter bw = new BufferedWriter(new FileWriter(playerFile));
          Iterator<Map.Entry<String, String>> it = playerCommandMap.entrySet().iterator();
          
          //Save the players and corresponding commands
          bw.write("<Player> <Command>\n");
          while(it.hasNext()){
             Entry<String, String> mp = it.next();
             bw.write(mp.getKey() + " " + mp.getValue() + "\n");
          }
          bw.close();
       }catch(IOException e){
          log.info("Error saving player command file");
       }
       
       //Save block data
       try{
           BufferedWriter bw = new BufferedWriter(new FileWriter(blockFile));
           Iterator<Map.Entry<Location, String>> it = blockCommandMap.entrySet().iterator();
           
           //Save the blocks and corresponding commands
           bw.write("<Block Location> <Command>\n");
           while(it.hasNext()){
              Entry<Location, String> mp = it.next();
              bw.write(mp.getKey().getWorld().getUID().toString() + " "
                      + Double.toString(mp.getKey().getX()) + " "
                      + Double.toString(mp.getKey().getY()) + " "
                      + Double.toString(mp.getKey().getZ()) + " "
                      + mp.getValue() + "\n");
           }
           bw.close();
        }catch(IOException e){
           log.info("Error saving block command file");
        }
       
       //Save player death data
       try{
          BufferedWriter bw = new BufferedWriter(new FileWriter(deathFile));
          Iterator<Map.Entry<String, String>> it = deathCommandMap.entrySet().iterator();
          
          //Save the players and corresponding commands
          bw.write("<Player> <Command>\n");
          while(it.hasNext()){
             Entry<String, String> mp = it.next();
             bw.write(mp.getKey() + " " + mp.getValue() + "\n");
          }
          bw.close();
       }catch(IOException e){
          log.info("Error saving player death command file");
       }
       
       //Save player respawn data
       try{
          BufferedWriter bw = new BufferedWriter(new FileWriter(respawnFile));
          Iterator<Map.Entry<String, String>> it = respawnCommandMap.entrySet().iterator();
          
          //Save the players and corresponding commands
          bw.write("<Player> <Command>\n");
          while(it.hasNext()){
             Entry<String, String> mp = it.next();
             bw.write(mp.getKey() + " " + mp.getValue() + "\n");
          }
          bw.close();
       }catch(IOException e){
          log.info("Error saving player respawn command file");
       }
       
       //Save server start up data
       try{
          BufferedWriter bw = new BufferedWriter(new FileWriter(startupFile));
          StringTokenizer st = new StringTokenizer(startupCommands, ":");
          
          while(st.countTokens() > 0){
             bw.write(st.nextToken() + "\n");
          }
          bw.close();
       }catch(IOException e){
          log.info("Error saving server start up command file");
       }
       
       //Save repeat command data
       try{
          BufferedWriter bw = new BufferedWriter(new FileWriter(repeatFile));
          Iterator<Map.Entry<String, Integer>> it = repeatCommandMap.entrySet().iterator();
          
          //Save the players and corresponding commands
          bw.write("<Command> <Interval>\n");
          while(it.hasNext()){
             Entry<String, Integer> mp = it.next();
             bw.write(mp.getKey() + " " + mp.getValue().toString() + "\n");
          }
          bw.close();
       }catch(IOException e){
          log.info("Error saving player repeat command file");
       }
       
       //Save command data
       try{
           BufferedWriter bw = new BufferedWriter(new FileWriter(commandFile));
           Iterator<Map.Entry<String, String>> it = commandMap.entrySet().iterator();
           
           //Save the blocks and corresponding commands
           bw.write("<Identifing name> <Command>\n");
           while(it.hasNext()){
              Entry<String, String> mp = it.next();
              bw.write(mp.getKey() + " " + mp.getValue() + "\n");
           }
           bw.close();
        }catch(IOException e){
           log.info("Error saving command file");
        }
      log.info("Autorun Commands v2.7.1 disabled");
    }
 
    public void onEnable(){
       //Create the directory and files if needed
       new File(getDataFolder().toString()).mkdir();
       playerFile = new File(getDataFolder().toString() + "/playerList.txt");
       commandFile = new File(getDataFolder().toString() + "/commands.txt");
       blockFile = new File(getDataFolder().toString() + "/blockList.txt");
       deathFile = new File(getDataFolder().toString() + "/deathList.txt");
       respawnFile = new File(getDataFolder().toString() + "/respawnList.txt");
       startupFile = new File(getDataFolder().toString() + "/startupCommands.txt");
       repeatFile = new File(getDataFolder().toString() + "/repeatList.txt");
       
       //Load the player file data
       playerCommandMap = new HashMap<String, String>();
       try{
          BufferedReader br = new BufferedReader(new FileReader(playerFile));
          StringTokenizer st;
          String input;
          String name;
          String command;
          while((input = br.readLine()) != null){
             if(input.compareToIgnoreCase("<Player> <Command>") == 0){
                continue;
             }
             st = new StringTokenizer(input, " ");
             name = st.nextToken();
             command = st.nextToken();
             playerCommandMap.put(name, command);
          }
          
       }catch(FileNotFoundException e){
          log.info("No original player command file, creating new one.");
       }catch(IOException e){
          log.info("Error reading player command file");
       }catch(Exception e){
          log.info("Incorrectly formatted player command file");
       }
       
       //Load the block file data
       blockCommandMap = new HashMap<Location, String>();
       try{
          BufferedReader br = new BufferedReader(new FileReader(blockFile));
          StringTokenizer st;
          String input;
          String command;
          Location loc = null;
          
          while((input = br.readLine()) != null){
             if(input.compareToIgnoreCase("<Block Location> <Command>") == 0){
                continue;
             }
             st = new StringTokenizer(input, " ");
             loc = new Location(getServer().getWorld(UUID.fromString(st.nextToken())), Double.parseDouble(st.nextToken()), Double.parseDouble(st.nextToken()), Double.parseDouble(st.nextToken()));
             command = st.nextToken();
             blockCommandMap.put(loc, command);
          }
          
       }catch(FileNotFoundException e){
          log.info("No original block command file, creating new one.");
       }catch(IOException e){
          log.info("Error reading block command file");
       }catch(Exception e){
          log.info("Incorrectly formatted block command file");
       }
       
       //Load the player death file data
       deathCommandMap = new HashMap<String, String>();
       try{
          BufferedReader br = new BufferedReader(new FileReader(deathFile));
          StringTokenizer st;
          String input;
          String name;
          String command;
          while((input = br.readLine()) != null){
             if(input.compareToIgnoreCase("<Player> <Command>") == 0){
                continue;
             }
             st = new StringTokenizer(input, " ");
             name = st.nextToken();
             command = st.nextToken();
             deathCommandMap.put(name, command);
          }
          
       }catch(FileNotFoundException e){
          log.info("No original player death command file, creating new one.");
       }catch(IOException e){
          log.info("Error reading player death command file");
       }catch(Exception e){
          log.info("Incorrectly formatted player death command file");
       }
       
       //Load the player respawn file data
       respawnCommandMap = new HashMap<String, String>();
       try{
          BufferedReader br = new BufferedReader(new FileReader(respawnFile));
          StringTokenizer st;
          String input;
          String name;
          String command;
          while((input = br.readLine()) != null){
             if(input.compareToIgnoreCase("<Player> <Command>") == 0){
                continue;
             }
             st = new StringTokenizer(input, " ");
             name = st.nextToken();
             command = st.nextToken();
             respawnCommandMap.put(name, command);
          }
          
       }catch(FileNotFoundException e){
          log.info("No original player respawn command file, creating new one.");
       }catch(IOException e){
          log.info("Error reading player respawn command file");
       }catch(Exception e){
          log.info("Incorrectly formatted player respawn command file");
       }
       
       //Load the start up data
       startupCommands = "";
       try{
          BufferedReader br = new BufferedReader(new FileReader(startupFile));
          String input;
          while((input = br.readLine()) != null){
             if(input.compareToIgnoreCase("<Command>") == 0){
                continue;
             }
             startupCommands += ":" + input;
          }
          
       }catch(FileNotFoundException e){
          log.info("No original start up command file, creating new one.");
       }catch(IOException e){
          log.info("Error reading start up command file");
       }catch(Exception e){
          log.info("Incorrectly formatted start up command file");
       }
       
       //Load the repeat command file data
       repeatCommandMap = new HashMap<String, Integer>();
       try{
          BufferedReader br = new BufferedReader(new FileReader(repeatFile));
          StringTokenizer st;
          String input;
          
          //Assumes that the name is only one token long
          while((input = br.readLine()) != null){
             if(input.compareToIgnoreCase("<Command> <Interval>") == 0){
                continue;
             }
             st = new StringTokenizer(input, " ");
             repeatCommandMap.put(st.nextToken(), Integer.parseInt(st.nextToken()));
          }
          
       }catch(FileNotFoundException e){
          log.info("No original repeat command file, creating new one.");
       }catch(IOException e){
          log.info("Error reading repeat command file");
       }catch(Exception e){
          log.info("Incorrectly formatted repeat command file");
       }
       
       //Load the command file data
       commandMap = new HashMap<String, String>();
       try{
          BufferedReader br = new BufferedReader(new FileReader(commandFile));
          StringTokenizer st;
          String input;
          String args;
          String name;
          
          //Assumes that the name is only one token long
          while((input = br.readLine()) != null){
             if(input.compareToIgnoreCase("<Identifing name> <Command>") == 0){
                continue;
             }
             st = new StringTokenizer(input, " ");
             name = st.nextToken();
             args = st.nextToken();
             while(st.hasMoreTokens()){
                args += " " + st.nextToken();
             }
             commandMap.put(name, args);
          }
          
       }catch(FileNotFoundException e){
          log.info("No original command file, creating new one.");
       }catch(IOException e){
          log.info("Error reading command file");
       }catch(Exception e){
          log.info("Incorrectly formatted command file");
       }
       
       //Start running the repeating tasks
       
       placeBlock = false;
       startupDone = false;
       blockCommand = "";
       playerPosMap = new HashMap<String, Location>();
       runningRepeatCommandMap = new HashMap<String, Integer>();
       
       //Set up the listeners
       getServer().getPluginManager().registerEvents(new CommandPlayerListener(this), this);
       getServer().getPluginManager().registerEvents(new CommandBlockListener(this), this);
       getServer().getPluginManager().registerEvents(new CommandEntityListener(this), this);
       getServer().getPluginManager().registerEvents(new CommandServerListener(this), this);
 
       //Set up the commands
       getCommand("setclickcommand").setExecutor(new RightClick(this));
       getCommand("removeclickcommand").setExecutor(new RightClick(this));
       getCommand("displayclickcommand").setExecutor(new RightClick(this));
 
       getCommand("setdeathcommand").setExecutor(new Death(this));
       getCommand("removedeathcommand").setExecutor(new Death(this));
       getCommand("displaydeathcommand").setExecutor(new Death(this));
 
       getCommand("addrepeatcommand").setExecutor(new Repeat(this));
       getCommand("removerepeatcommand").setExecutor(new Repeat(this));
 
       getCommand("addstartupcommand").setExecutor(new Startup(this));
       getCommand("removestartupcommand").setExecutor(new Startup(this));
       getCommand("displaystartupcommands").setExecutor(new Startup(this));
 
       getCommand("setrespawncommand").setExecutor(new Respawn(this));
       getCommand("removerespawncommand").setExecutor(new Respawn(this));
       getCommand("displayrespawncommand").setExecutor(new Respawn(this));
 
       getCommand("setcommandblock").setExecutor(new Blocks(this));
       
       getCommand("addacommand").setExecutor(new Commands(this));
       getCommand("addopcommand").setExecutor(new Commands(this));
       getCommand("removeacommand").setExecutor(new Commands(this));
       
      log.info("Autorun Commands v2.7.1 is enabled");
    }
    
    public void removeStartupCommand(CommandSender sender, String command){
       if(startupCommands.contains(":" + command)){
          startupCommands = startupCommands.replace(":" + command, "");
          sender.sendMessage("Command removal successful");
       }
       else{
          sender.sendMessage("No command set with that identifier");
          sender.sendMessage("Check with \'/displaystartupcommands' first");
       }
    }
    
    public void addStartupCommand(CommandSender sender, String command){
       if(startupCommands.contains(command))
          sender.sendMessage("That command is already present");
       else{
          startupCommands += ":" + command;
          sender.sendMessage("Command association successful");
       }
    }
 
    public HashMap<String, String> getPlayerClickMap(){
       return playerCommandMap;
    }
 
    public HashMap<String, String> getPlayerDeathMap(){
       return deathCommandMap;
    }
 
    public HashMap<String, String> getPlayerRespawnMap(){
       return respawnCommandMap;
    }
 
    public HashMap<Location, String> getBlockCommandMap(){
       return blockCommandMap;
    }
 
    public HashMap<String, Integer> getRepeatMap(){
       return repeatCommandMap;
    }
 
    public HashMap<String, Integer> getRunningRepeatMap(){
       return runningRepeatCommandMap;
    }
 
    public HashMap<String, String> getCommandMap(){
       return commandMap;
    }
    
    public boolean isPlaceBlock(){
       return placeBlock;
    }
    
    public void setPlaceBlock(boolean placeBlock){
       this.placeBlock = placeBlock;
    }
    
    public String getBlockCommand(){
       return blockCommand;
    }
 
    public void setBlockCommand(String blockCommand){
       this.blockCommand = blockCommand;
    }
 
    public HashMap<String, Location> getPlayerPosMap() {
       return playerPosMap;
    }
 
    public String getStartupCommands() {
       return startupCommands;
    }
 
    public void setStartupCommands(String sc) {
       this.startupCommands = sc;
    }
 
    public boolean isStartupDone() {
       return startupDone;
    }
 
    public void setStartupDone(boolean startupDone) {
       this.startupDone = startupDone;
    }
 }
