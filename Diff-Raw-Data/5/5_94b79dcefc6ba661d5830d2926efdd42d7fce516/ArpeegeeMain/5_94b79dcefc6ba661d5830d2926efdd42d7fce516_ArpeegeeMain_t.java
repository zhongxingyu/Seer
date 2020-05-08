 package littlegruz.arpeegee;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Random;
 import java.util.StringTokenizer;
 import java.util.Map.Entry;
 import java.util.UUID;
 
 import littlegruz.arpeegee.commands.Begin;
 import littlegruz.arpeegee.commands.Join;
 import littlegruz.arpeegee.commands.Party;
 import littlegruz.arpeegee.commands.Quests;
 import littlegruz.arpeegee.commands.Worlds;
 import littlegruz.arpeegee.entities.RPGMagicPlayer;
 import littlegruz.arpeegee.entities.RPGMeleePlayer;
 import littlegruz.arpeegee.entities.RPGParty;
 import littlegruz.arpeegee.entities.RPGPlayer;
 import littlegruz.arpeegee.entities.RPGQuest;
 import littlegruz.arpeegee.entities.RPGRangedPlayer;
 import littlegruz.arpeegee.gui.LittleGUI;
 import littlegruz.arpeegee.listeners.ButtonListener;
 import littlegruz.arpeegee.listeners.EnemyDeaths;
 import littlegruz.arpeegee.listeners.EntityDamageEntity;
 import littlegruz.arpeegee.listeners.PlayerChat;
 import littlegruz.arpeegee.listeners.PlayerInteract;
 import littlegruz.arpeegee.listeners.PlayerItemHeld;
 import littlegruz.arpeegee.listeners.PlayerJoin;
 import littlegruz.arpeegee.listeners.PlayerLevel;
 import littlegruz.arpeegee.listeners.PlayerPickupItem;
 import littlegruz.arpeegee.listeners.PlayerProjectile;
 import littlegruz.arpeegee.listeners.PlayerRespawn;
 import littlegruz.arpeegee.listeners.PlayerSpeed;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.entity.Animals;
 import org.bukkit.entity.EnderDragon;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Monster;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Squid;
 import org.bukkit.event.Event;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class ArpeegeeMain extends JavaPlugin {
    private LittleGUI gui;
    private File meleePlayerFile;
    private File rangedPlayerFile;
    private File magicPlayerFile;
    private File questStartFile;
    private File partyFile;
    private File worldsFile;
    private File dialogueFile;
    private HashMap<String, RPGMeleePlayer> meleePlayerMap;
    private HashMap<String, RPGRangedPlayer> rangedPlayerMap;
    private HashMap<String, RPGMagicPlayer> magicPlayerMap;
    private HashMap<String, Integer> bideMap;
    private HashMap<UUID, String> confMap;
    private HashMap<Entity, Integer> blindMap;
    private HashMap<Integer, RPGQuest> questMap;
    private HashMap<Location, Integer> questStartMap;
    private HashMap<String, RPGParty> partyMap;
    private HashMap<String, String> berserkMap;
    private HashMap<String, String> buildUpMap;
    private HashMap<Entity, String> projMap;
    private HashMap<String, String> worldsMap;
    private HashMap<String, String> dialogueMap;
    private HashMap<Integer, Integer> expLevelMap;
    private int questNumberToSet;
    private boolean questCanSet;
    private boolean questCanUnset;
    private boolean spoutEnabled;
    
    public final int MAX_LEVEL = 20;
    
    public void onEnable(){
       // Create the directory if needed
       new File(getDataFolder().toString()).mkdir();
       meleePlayerFile = new File(getDataFolder().toString() + "/meleePlayer.txt");
       rangedPlayerFile = new File(getDataFolder().toString() + "/rangedPlayer.txt");
       magicPlayerFile = new File(getDataFolder().toString() + "/magicPlayer.txt");
       questStartFile = new File(getDataFolder().toString() + "/questStarters.txt");
       partyFile = new File(getDataFolder().toString() + "/party.yml");
       worldsFile = new File(getDataFolder().toString() + "/worlds.txt");
       dialogueFile = new File(getDataFolder().toString() + "/dialogues.yml");
       
       spoutEnabled = getServer().getPluginManager().isPluginEnabled("Spout");
       
       getLogger().info("Loading LittleRPG data...");
 
       meleePlayerMap = new HashMap<String, RPGMeleePlayer>();
       loadMeleeFile();
 
       rangedPlayerMap = new HashMap<String, RPGRangedPlayer>();
       loadRangedFile();
 
       magicPlayerMap = new HashMap<String, RPGMagicPlayer>();
       loadMagicFile();
       
       worldsMap = new HashMap<String, String>();
       loadWorldsFile();
       
       dialogueMap = new HashMap<String, String>();
       loadDialoguesFile();
       
       questStartMap = new HashMap<Location, Integer>();
       loadQuestStartsFile();
       
       partyMap = new HashMap<String, RPGParty>();
       loadPartiesFile();
 
       // Load up the exp limits
       expLevelMap = new HashMap<Integer, Integer>();
       expLevelMap.put(1, 45);
       expLevelMap.put(2, 100);
       expLevelMap.put(3, 160);
       expLevelMap.put(4, 230);
       expLevelMap.put(5, 310);
       expLevelMap.put(6, 400);
       expLevelMap.put(7, 500);
       expLevelMap.put(8, 600);
       expLevelMap.put(9, 710);
       expLevelMap.put(10, 830);
       expLevelMap.put(11, 960);
       expLevelMap.put(12, 1100);
       expLevelMap.put(13, 1260);
       expLevelMap.put(14, 1440);
       expLevelMap.put(15, 1640);
       expLevelMap.put(16, 1860);
       expLevelMap.put(17, 2100);
       expLevelMap.put(18, 2380);
       expLevelMap.put(19, 2720);
       expLevelMap.put(20, 3140);
       
       //Get data from config.yml
       questMap = new HashMap<Integer, RPGQuest>();
       loadQuests();
 
       //Set up listeners
       getServer().getPluginManager().registerEvents(new EnemyDeaths(this), this);
       getServer().getPluginManager().registerEvents(new EntityDamageEntity(this), this);
       getServer().getPluginManager().registerEvents(new PlayerChat(this), this);
       getServer().getPluginManager().registerEvents(new PlayerInteract(this), this);
       getServer().getPluginManager().registerEvents(new PlayerItemHeld(this), this);
       getServer().getPluginManager().registerEvents(new PlayerJoin(this), this);
       getServer().getPluginManager().registerEvents(new PlayerLevel(this), this);
       getServer().getPluginManager().registerEvents(new PlayerPickupItem(this), this);
       getServer().getPluginManager().registerEvents(new PlayerProjectile(this), this);
       getServer().getPluginManager().registerEvents(new PlayerRespawn(this), this);
       getServer().getPluginManager().registerEvents(new PlayerSpeed(this), this);
       if(spoutEnabled)
          getServer().getPluginManager().registerEvents(new ButtonListener(this), this);
 
       getCommand("ichoosemelee").setExecutor(new Begin(this));
       getCommand("ichooseranged").setExecutor(new Begin(this));
       getCommand("ichoosemagic").setExecutor(new Begin(this));
       
       getCommand("addrpgworld").setExecutor(new Worlds(this));
       getCommand("removerpgworld").setExecutor(new Worlds(this));
       
       getCommand("setquest").setExecutor(new Quests(this));
       getCommand("unsetquest").setExecutor(new Quests(this));
       getCommand("displayquests").setExecutor(new Quests(this));
       
       getCommand("setrpgintro").setExecutor(new Join(this));
       getCommand("setrpgreturn").setExecutor(new Join(this));
       
       getCommand("createparty").setExecutor(new Party(this));
       getCommand("joinparty").setExecutor(new Party(this));
       getCommand("leaveparty").setExecutor(new Party(this));
       getCommand("sendpartyinvite").setExecutor(new Party(this));
       getCommand("removepartyinvite").setExecutor(new Party(this));
       getCommand("party").setExecutor(new Party(this));
 
       berserkMap = new HashMap<String, String>();
       buildUpMap = new HashMap<String, String>();
       projMap = new HashMap<Entity, String>();
       bideMap = new HashMap<String, Integer>();
       blindMap = new HashMap<Entity, Integer>();
       confMap = new HashMap<UUID, String>();
       
       questNumberToSet = -1;
       questCanSet = false;
       questCanUnset = false;
       
       if(spoutEnabled)
          gui = new LittleGUI(this);
       else
          getLogger().info("Spout not found. Some effects will be disabled");
 
       getLogger().info("Enabled " + this.toString());
    }
    
    public void onDisable(){
       // Save ALL the data!
       getLogger().info("Saving LittleRPG data...");
       
       saveToMeleeFile();
       saveToRangedFile();
       saveToMagicFile();
       saveToWorldsFile();
       saveToDialoguesFile();
       saveToQuestStartsFile();
       saveToPartiesFile();
       
       getLogger().info("Disabled " + this.toString());
    }
 
    public HashMap<String, RPGMeleePlayer> getMeleePlayerMap(){
       return meleePlayerMap;
    }
 
    public HashMap<String, RPGRangedPlayer> getRangedPlayerMap(){
       return rangedPlayerMap;
    }
 
    public HashMap<String, RPGMagicPlayer> getMagicPlayerMap(){
       return magicPlayerMap;
    }
    
    public HashMap<String, Integer> getBideMap(){
       return bideMap;
    }
    
    public HashMap<UUID, String> getConfMap(){
       return confMap;
    }
 
    public HashMap<Integer, RPGQuest> getQuestMap(){
       return questMap;
    }
 
    public HashMap<String, RPGParty> getPartyMap(){
       return partyMap;
    }
 
    public HashMap<String, String> getBerserkMap(){
       return berserkMap;
    }
 
    public HashMap<String, String> getBuildUpMap(){
       return buildUpMap;
    }
    
    public HashMap<Entity, String> getProjMap(){
       return projMap;
    }
 
    public HashMap<String, String> getWorldsMap(){
       return worldsMap;
    }
 
    public HashMap<Location, Integer> getQuestStartMap(){
       return questStartMap;
    }
 
    public HashMap<Entity, Integer> getBlindMap(){
       return blindMap;
    }
 
    public int getQuestNumberToSet(){
       return questNumberToSet;
    }
 
    public void setQuestNumberToSet(int questNumberToSet){
       this.questNumberToSet = questNumberToSet;
    }
 
    public boolean isQuestCanSet(){
       return questCanSet;
    }
 
    public void setQuestCanSet(boolean questCanSet){
       this.questCanSet = questCanSet;
    }
 
    public boolean isQuestCanUnset(){
       return questCanUnset;
    }
 
    public void setQuestCanUnset(boolean questCanUnset){
       this.questCanUnset = questCanUnset;
    }
    
    public boolean isSpoutEnabled(){
       return spoutEnabled;
    }
 
    public HashMap<String, String> getDialogueMap(){
       return dialogueMap;
    }
 
    public LittleGUI getGUI(){
       return gui;
    }
 
    /* Returns true if the RNG smiles upon the user*/
    public boolean probabilityRoll(int percent){
       Random rand = new Random();
       
       if(rand.nextInt(100) <= percent)
          return true;
       else
          return false;
    }
    
    /* Checks if the given entity is an enemy*/
    public boolean isEnemy(Entity ent, String party){
       if(ent instanceof Animals)
          return true;
       else if(ent instanceof Squid)
          return true;
       else if(ent instanceof Monster)
          return true;
       else if(ent instanceof EnderDragon)
          return true;
       else if(ent instanceof Player){
          RPGPlayer rpgPlaya;
 
          /* Find the RPG player*/
          if((rpgPlaya = getRPGPlayer(((Player) ent).getName())) == null)
             return true;
          
          if(rpgPlaya.getParty().compareToIgnoreCase(party) == 0)
             return false;
          
          return true;
       }
       return false;
    }
 
    public void giveCooldown(final Player playa, final String type, String classType, double delay){
       if(classType.compareTo("default") == 0){
          getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable(){
             public void run() {
                if(meleePlayerMap.get(playa.getName()) != null)
                   meleePlayerMap.get(playa.getName()).setBaseAttackReadiness(true);
                else if(magicPlayerMap.get(playa.getName()) != null)
                   magicPlayerMap.get(playa.getName()).setBaseAttackReadiness(true);
                else if(rangedPlayerMap.get(playa.getName()) != null)
                   rangedPlayerMap.get(playa.getName()).setBaseAttackReadiness(true);
             }
         }, (long) (delay * 20));
       }
       /* Sets a task to turn off a magical ability's cooldown*/
       else if(classType.compareTo("magic") == 0){
          getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable(){
             public void run() {
                RPGMagicPlayer rpgPlaya = magicPlayerMap.get(playa.getName());
                ItemStack is = new ItemStack(351,1);
                
                if(type.compareTo("heal") == 0){
                   rpgPlaya.setHealReadiness(true);
                   is.setDurability((short)15);
                   playa.getInventory().setItem(3, is);
                }
                else if(type.compareTo("light") == 0){
                   rpgPlaya.setLightningReadiness(true);
                   is.setDurability((short)11);
                   playa.getInventory().setItem(0, is);
                }
                else if(type.compareTo("fire") == 0){
                   rpgPlaya.setFireReadiness(true);
                   is.setDurability((short)1);
                   playa.getInventory().setItem(2, is);
                }
                else if(type.compareTo("baaa") == 0){
                   rpgPlaya.setSheepReadiness(true);
                   is.setType(Material.WHEAT);
                   playa.getInventory().setItem(4, is);
                }
                else if(type.compareTo("conf") == 0){
                   rpgPlaya.setConfusionReadiness(true);
                   is.setDurability((short)14);
                   playa.getInventory().setItem(1, is);
                }
             }
         }, (long) (delay * 20));
       }
       /* Sets a task to turn off a ranged ability's cooldown*/
       else if(classType.compareTo("ranged") == 0){
          getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable(){
             public void run() {
                RPGRangedPlayer rpgPlaya = rangedPlayerMap.get(playa.getName());
                ItemStack is = new ItemStack(351,1);
                playa.getInventory().setItem(0, new ItemStack(Material.BOW,1));
                
                if(type.compareTo("arrow") == 0){
                   rpgPlaya.setArrowReadiness(true);
                   playa.getInventory().setItem(0, new ItemStack(Material.BOW,1));
                }
                else if(type.compareTo("egg") == 0){
                   rpgPlaya.setEggReadiness(true);
                   playa.getInventory().setItem(3, new ItemStack(Material.EGG,1));
                }
                else if(type.compareTo("slow") == 0){
                   rpgPlaya.setSlowBowReadiness(true);
                   is.setDurability((short)12);
                   playa.getInventory().setItem(1, is);
                }
                else if(type.compareTo("ICANTSEE") == 0){
                   rpgPlaya.setBlindBowReadiness(true);
                   is.setDurability((short)0);
                   playa.getInventory().setItem(2, is);
                }
                else if(type.compareTo("woof") == 0){
                   rpgPlaya.setSheepBowReadiness(true);
                   is.setDurability((short)15);
                   playa.getInventory().setItem(4, is);
                }
             }
         }, (long) (delay * 20)); // Multiplied by 20 to turn the delay time into seconds
       }
       /* Sets a task to turn off a ranged ability's cooldown*/
       else if(classType.compareTo("melee") == 0){
          getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable(){
             public void run() {
                RPGMeleePlayer rpgPlaya = meleePlayerMap.get(playa.getName());
                
                if(type.compareTo("imob") == 0){
                   rpgPlaya.setImobiliseReadiness(true);
                }
                else if(type.compareTo("flash") == 0){
                   rpgPlaya.setFlashReadiness(true);
                   playa.getInventory().setItem(1, new ItemStack(Material.RAW_FISH,1));
                }
                else if(type.compareTo("bide") == 0){
                   rpgPlaya.setBideAmt(0);
                   rpgPlaya.setBideReadiness(true);
                   playa.getInventory().setItem(4, new ItemStack(Material.POTATO_ITEM,1));
                }
                else if(type.compareTo("sssh") == 0){
                   rpgPlaya.setSilenceReadiness(true);
                }
                else if(type.compareTo("slash") == 0){
                   rpgPlaya.setSwordReadiness(true);
                }
             }
         }, (long) (delay * 20));
       }
    }
 
    /* Example quests from config.yml
     * No prereq quest, must be at least level 5 and player must have at
     * least 5 dirt blocks. Finishing condition is 25 dirt blocks with a
     * reward of a sand block and 20xp.
     * Quest 2 has a finishing condition of 1 coal and 1 stick, reward is 30xp.
     * quest0:
     * - Level:5
     * - Items:3|5
     * - Text:Greetings traveller, I need more dirt to make my first home.
     * - Text:Could you please fetch me 25 dirt blocks?
     * - FC:3|25
     * - Pass:Thank you, I can now make my lovely 3x4x3 dirt home.
     * - Fail:You do not have enough dirt.
     * - Reward:12|1:XP|20
     * quest1:
     * - PQ:0
     * - Text:I now must decorate it, fetch me 1 piece of coal and 1 stick.
     * - FC:263|1:280|1
     * - Pass:Decorations! Yay!
     * - Fail:That is not right.
     * - Reward:XP|30*/
    
    // Loads all the quests from config.yml
    public void loadQuests(){
       int i, quest, level, preReqQuest;
       String type, item, finishers, reward;
       ArrayList<String> text;
       List<String> questData;
       StringTokenizer st;
       
       // Get data and create each quest
       for(quest = 0; getConfig().isList("quest" + quest); quest++){
          questData = getConfig().getStringList("quest" + quest);
          text = new ArrayList<String>();
          level = -1;
          preReqQuest = -1;
          item = "";
          finishers = "";
          reward = "";
          
          for(i = 0; i < questData.size(); i++){
             st = new StringTokenizer(questData.get(i), ":");
             type = st.nextToken();
             
             if(type.compareToIgnoreCase("pq") == 0){
                preReqQuest = Integer.parseInt(st.nextToken());
             }
             else if(type.compareToIgnoreCase("level") == 0){
                level = Integer.parseInt(st.nextToken());
             }
             else if(type.compareToIgnoreCase("items") == 0){
                item = st.nextToken();
                while(st.hasMoreTokens()){
                   item += "|" + st.nextToken();
                }
             }
             else if(type.compareToIgnoreCase("text") == 0
                   || type.compareToIgnoreCase("pass") == 0
                   || type.compareToIgnoreCase("fail") == 0){
                text.add(type.toLowerCase() + "|" + st.nextToken());
             }
             else if(type.compareToIgnoreCase("fc") == 0){
                finishers = st.nextToken();
                while(st.hasMoreTokens()){
                   finishers += "|" + st.nextToken();
                }
             }
             else if(type.compareToIgnoreCase("reward") == 0){
                reward = st.nextToken();
                while(st.hasMoreTokens()){
                   reward += "|" + st.nextToken();
                }
             }
          }
          
          // Create the new quest and store it in the quest hashmap
          questMap.put(quest, new RPGQuest(quest, preReqQuest, level, item, text, finishers, reward));
       }
    }
 
    /* Gets the percentage of the exp gained */
    public void giveExp(Player playa, int exp){
       float amount;
       
       amount = (float) exp / expLevelMap.get(playa.getLevel());
       if(playa.getExp() + amount > 1){
          amount = (amount + playa.getExp()) - 1;
          playa.setLevel(playa.getLevel() + 1);
          playa.setExp(amount);
       }
       else
          playa.setExp(playa.getExp() + amount);
    }
    
    /* Sets how effective damage is against certain creatures and abilities*/
    public void ohTheDamage(Event event, Entity entity, float dmg){
       boolean otherMother;
       
       otherMother = true;
       if(event instanceof EntityDamageByEntityEvent){
          if(entity instanceof Animals || entity instanceof Squid)
             ((EntityDamageByEntityEvent) event).setDamage((int)(dmg * 2));
          else{
             if(entity instanceof Player){
                if(meleePlayerMap.get(((Player) entity).getName()) != null){
                   meleeBide((Player) entity, (int) dmg);
                   otherMother = false;
                }
             }
             
             if(otherMother)
                ((EntityDamageByEntityEvent) event).setDamage((int)dmg);
          }
       }
       /* If null then it is from the PlayerInteract event */
       else if(event == null){
          if(entity instanceof Animals || entity instanceof Squid)
             ((LivingEntity)entity).damage((int)(dmg * 2));
          else{
             if(entity instanceof Player){
                if(meleePlayerMap.get(((Player) entity).getName()) != null){
                   meleeBide((Player) entity, (int)dmg);
                   otherMother = false;
                }
             }
 
             if(otherMother)
                ((LivingEntity)entity).damage((int)dmg);
          }
       }
    }
    
    public RPGPlayer getRPGPlayer(String name){
       if(meleePlayerMap.get(name) != null)
          return meleePlayerMap.get(name);
       else if(rangedPlayerMap.get(name) != null)
          return rangedPlayerMap.get(name);
       else if(magicPlayerMap.get(name) != null)
          return magicPlayerMap.get(name);
       else
          return null;
    }
    
    public void meleeBide(Player playa, int damage){
       RPGMeleePlayer rpgmp = meleePlayerMap.get(playa.getName());
       
       this.getServer().broadcastMessage("Biding");
       if(!rpgmp.addBideAmt(damage)){
          playa.getWorld().createExplosion(playa.getLocation(), damage);
          this.getServer().getScheduler().cancelTask(bideMap.get(rpgmp.getName()));
          bideMap.remove(rpgmp.getName());
          this.getServer().broadcastMessage("Canceled *explode*");
       }
    }
 
    // Load up the melee players from file
    public void loadMeleeFile(){
       BufferedReader br;
       String input;
       StringTokenizer st;
       
       try{
          br = new BufferedReader(new FileReader(meleePlayerFile));
          
          while((input = br.readLine()) != null){
             String name;
             int level, rage;
             float gear;
             
             st = new StringTokenizer(input, " ");
             name = st.nextToken();
 
             level = Integer.parseInt(st.nextToken());
             gear = Float.parseFloat(st.nextToken());
             rage = Integer.parseInt(st.nextToken());
             meleePlayerMap.put(name, new RPGMeleePlayer(name, level, gear, rage, st.nextToken(), st.nextToken(), st.nextToken()));
          }
          br.close();
          
       }catch(FileNotFoundException e){
          getLogger().info("No original LittleRPG melee player file found. One will be created for you");
       }catch(IOException e){
          getLogger().info("Error reading LittleRPG melee player file");
       }catch(Exception e){
          getLogger().info("Incorrectly formatted LittleRPG melee player file");
       }
    }
 
    // Load up the ranged players from file
    public void loadRangedFile(){
       BufferedReader br;
       String input;
       StringTokenizer st;
 
       try{
          br = new BufferedReader(new FileReader(rangedPlayerFile));
          
          while((input = br.readLine()) != null){
             String name;
             int level;
             float gear;
             
             st = new StringTokenizer(input, " ");
             name = st.nextToken();
 
             level = Integer.parseInt(st.nextToken());
             gear = Float.parseFloat(st.nextToken());
             rangedPlayerMap.put(name, new RPGRangedPlayer(name, level, gear, st.nextToken(), st.nextToken(), st.nextToken()));
          }
          br.close();
          
       }catch(FileNotFoundException e){
          getLogger().info("No original LittleRPG ranged player file found. One will be created for you");
       }catch(IOException e){
          getLogger().info("Error reading LittleRPG ranged player file");
       }catch(Exception e){
          getLogger().info("Incorrectly formatted LittleRPG ranged player file");
       }
    }
 
    // Load up the magic players from file
    public void loadMagicFile(){
       BufferedReader br;
       String input;
       StringTokenizer st;
 
       try{
          br = new BufferedReader(new FileReader(magicPlayerFile));
          
          while((input = br.readLine()) != null){
             String name;
             int level, buildUp;
             float gear;
             
             st = new StringTokenizer(input, " ");
             name = st.nextToken();
             
             level = Integer.parseInt(st.nextToken());
             gear = Float.parseFloat(st.nextToken());
             buildUp = Integer.parseInt(st.nextToken());
             magicPlayerMap.put(name, new RPGMagicPlayer(name, level, gear, buildUp, st.nextToken(), st.nextToken(), st.nextToken()));
          }
          br.close();
          
       }catch(FileNotFoundException e){
          getLogger().info("No original LittleRPG magic player file found. One will be created for you");
       }catch(IOException e){
          getLogger().info("Error reading LittleRPG magic player file");
       }catch(Exception e){
          getLogger().info("Incorrectly formatted LittleRPG magic player file");
       }
    }
 
    // Load up the worlds from file
    public void loadWorldsFile(){
       BufferedReader br;
       String input;
 
       try{
          br = new BufferedReader(new FileReader(worldsFile));
          
          while((input = br.readLine()) != null){
             worldsMap.put(input, input);
          }
          br.close();
          
       }catch(FileNotFoundException e){
          getLogger().info("No original LittleRPG world file found. One will be created for you");
       }catch(IOException e){
          getLogger().info("Error reading LittleRPG world file");
       }catch(Exception e){
          getLogger().info("Incorrectly formatted LittleRPG world file");
       }
    }
 
    // Load up the worlds from file
    public void loadDialoguesFile(){
       try{
          FileConfiguration dialogueConfig;
          
          dialogueConfig = YamlConfiguration.loadConfiguration(dialogueFile);
          
          if(!dialogueFile.exists()){
             getLogger().info("No original LittleRPG dialogue file found. One will be created for you");
             
             if(!spoutEnabled){
             dialogueMap.put("intro", ChatColor.RED + " Welcome to LittleRPG world. Please choose a class to begin. "
                   + ChatColor.YELLOW + "Melee class: type /ichoosemelee                                "
                   + ChatColor.GREEN + "Ranged class: type /ichooseranged                              "
                   + ChatColor.BLUE + "Magic class: type /ichoosemagic                                ");
             }
             else
                dialogueMap.put("intro", ChatColor.RED + "Welcome to LittleRPG world. You want to be the very best, like no one ever was.");
             
             dialogueMap.put("return", ChatColor.RED + "Welcome back, brave adventurer.");
             
             dialogueFile.createNewFile();
            saveToDialoguesFile();
          }
          else{
             dialogueMap.put("intro", dialogueConfig.getString("intro"));
             dialogueMap.put("return", dialogueConfig.getString("return"));
          }
          
       }catch(IllegalArgumentException e){
          getLogger().info("Error loading LittleRPG dialogue config file");
       } catch(IOException e){
          getLogger().info("Error creating LittleRPG dialogue file");
       }
    }
 
    // Load up the quest starting points from file
    public void loadQuestStartsFile(){
       BufferedReader br;
       String input;
       StringTokenizer st;
 
       try{
          Location loc;
          br = new BufferedReader(new FileReader(questStartFile));
          
          while((input = br.readLine()) != null){
             st = new StringTokenizer(input, " ");
             loc = new Location(getServer().getWorld(st.nextToken()), Integer.parseInt(st.nextToken()), Integer.parseInt(st.nextToken()), Integer.parseInt(st.nextToken()));
             
             questStartMap.put(loc, Integer.parseInt(st.nextToken()));
          }
          br.close();
          
       }catch(FileNotFoundException e){
          getLogger().info("No original LittleRPG quest start file found. One will be created for you");
       }catch(IOException e){
          getLogger().info("Error reading LittleRPG quest start file");
       }catch(Exception e){
          getLogger().info("Incorrectly formatted LittleRPG quest start file");
       }
    }
 
    // Load all the parties from file
    public void loadPartiesFile(){
       try{
          int i, j;
          List<String> partyNameList, partyMembers, partyInvites;
          FileConfiguration partyConfig;
          RPGParty partay;
          
          partyFile.createNewFile();
          
          partyConfig = YamlConfiguration.loadConfiguration(partyFile);
          
          partyNameList = partyConfig.getStringList("names");
          for(i = 0; i < partyNameList.size(); i++){
             partay = new RPGParty(partyNameList.get(i));
             
             partyMembers = partyConfig.getStringList(partyNameList.get(i) + ".members");
             for(j = 0; j < partyMembers.size(); j++){
                partay.addMember(partyMembers.get(j));
             }
             
             partyInvites = partyConfig.getStringList(partyNameList.get(i) + ".invites");
             for(j = 0; j < partyInvites.size(); j++){
                partay.addInvitation(partyInvites.get(j));
             }
             
             partyMap.put(partay.getName(), partay);
          }
          
          if(!partyFile.exists())
             partyFile.createNewFile();
          
       }catch(IllegalArgumentException e){
          getLogger().info("Error loading LittleRPG party config file");
       }catch(IOException e){
          getLogger().info("Error creating LittleRPG party file");
       }
    }
 
    // Save all melee players to file
    public void saveToMeleeFile(){
       BufferedWriter bw;
 
       try{
          bw = new BufferedWriter(new FileWriter(meleePlayerFile));
          
          Iterator<Map.Entry<String, RPGMeleePlayer>> it = meleePlayerMap.entrySet().iterator();
          while(it.hasNext()){
             Entry<String, RPGMeleePlayer> player = it.next();
             bw.write(player.getKey() + " "
                   + Integer.toString(player.getValue().getLevel()) + " "
                   + Float.toString(player.getValue().getGearLevel()) + " "
                   + Integer.toString(player.getValue().getRage()) + " "
                   + player.getValue().getIncomplete() + " "
                   + player.getValue().getComplete() + " "
                   + player.getValue().getParty() + "\n");
          }
          bw.close();
       }catch(IOException e){
          getLogger().info("Error saving LittleRPG melee players");
       }
    }
 
    // Save all ranged players to file
    public void saveToRangedFile(){
       BufferedWriter bw;
       
       try{
          bw = new BufferedWriter(new FileWriter(rangedPlayerFile));
          
          Iterator<Map.Entry<String, RPGRangedPlayer>> it = rangedPlayerMap.entrySet().iterator();
          while(it.hasNext()){
             Entry<String, RPGRangedPlayer> player = it.next();
             bw.write(player.getKey() + " "
                   + Integer.toString(player.getValue().getLevel()) + " "
                   + Float.toString(player.getValue().getGearLevel()) + " "
                   + player.getValue().getIncomplete() + " "
                   + player.getValue().getComplete() + " "
                   + player.getValue().getParty() + "\n");
          }
          bw.close();
       }catch(IOException e){
          getLogger().info("Error saving LittleRPG ranged players");
       }
       
    }
 
    // Save all magic players to file
    public void saveToMagicFile(){
       BufferedWriter bw;
 
       try{
          bw = new BufferedWriter(new FileWriter(magicPlayerFile));
          
          Iterator<Map.Entry<String, RPGMagicPlayer>> it = magicPlayerMap.entrySet().iterator();
          while(it.hasNext()){
             Entry<String, RPGMagicPlayer> player = it.next();
             bw.write(player.getKey() + " "
                   + Integer.toString(player.getValue().getLevel()) + " "
                   + Float.toString(player.getValue().getGearLevel()) + " "
                   + Integer.toString(player.getValue().getBuildUp()) + " "
                   + player.getValue().getIncomplete() + " "
                   + player.getValue().getComplete() + " "
                   + player.getValue().getParty() + "\n");
          }
          bw.close();
       }catch(IOException e){
          getLogger().info("Error saving LittleRPG magic players");
       }
    }
    
    public void saveToWorldsFile(){
       BufferedWriter bw;
 
       // Save all world names to file
       try{
          bw = new BufferedWriter(new FileWriter(worldsFile));
          Iterator<Map.Entry<String, String>> it = worldsMap.entrySet().iterator();
          
          while(it.hasNext()){
             Entry<String, String> world = it.next();
             bw.write(world.getValue() + "\n");
          }
          bw.close();
       }catch(IOException e){
          getLogger().info("Error saving LittleRPG worlds");
       }
    }
 
    // Save all dialogues to file
    public void saveToDialoguesFile(){
       try{
          FileConfiguration dialogueConfig;
          
          dialogueConfig = YamlConfiguration.loadConfiguration(dialogueFile);
          Iterator<Map.Entry<String, String>> it = dialogueMap.entrySet().iterator();
 
          dialogueConfig.set("intro", it.next().getValue());
          dialogueConfig.set("return", it.next().getValue());
          
         dialogueConfig.save(dialogueFile);
      }catch(Exception e){
          getLogger().info("Error saving LittleRPG dialogue file");
       }
    }
 
    // Save all quest starting points to file
    public void saveToQuestStartsFile(){
       BufferedWriter bw;
 
       try{
          bw = new BufferedWriter(new FileWriter(questStartFile));
          Iterator<Map.Entry<Location, Integer>> it = questStartMap.entrySet().iterator();
          
          while(it.hasNext()){
             Entry<Location, Integer> quest = it.next();
             bw.write(quest.getKey().getWorld().getName() + " "
                   + Integer.toString(quest.getKey().getBlockX()) + " "
                   + Integer.toString(quest.getKey().getBlockY()) + " "
                   + Integer.toString(quest.getKey().getBlockZ()) + " "
                   + Integer.toString(quest.getValue()) + "\n");
          }
          bw.close();
       }catch(IOException e){
          getLogger().info("Error saving LittleRPG quest starting points");
       }
    }
    
    // Save all party data to file
    public void saveToPartiesFile(){
       try{
          List<String> partyNameList, partyMembers, partyInvites;
          FileConfiguration partyConfig;
          partyNameList = new ArrayList<String>();
          
          partyConfig = YamlConfiguration.loadConfiguration(partyFile);
          
          Iterator<Map.Entry<String, RPGParty>> it = partyMap.entrySet().iterator();
          while(it.hasNext()){
             Entry<String, RPGParty> party = it.next();
             partyNameList.add(party.getKey());
             
             Iterator<Map.Entry<String, String>> it2 = party.getValue().getMembers().entrySet().iterator();
             partyMembers = new ArrayList<String>();
             while(it2.hasNext()){
                Entry<String, String> partyMem = it2.next();
                partyMembers.add(partyMem.getValue());
             }
             
             it2 = party.getValue().getInvitations().entrySet().iterator();
             partyInvites = new ArrayList<String>();
             while(it2.hasNext()){
                Entry<String, String> partyInv = it2.next();
                partyInvites.add(partyInv.getValue());
             }
 
             partyConfig.set(party.getKey() + ".members", partyMembers);
             partyConfig.set(party.getKey() + ".invites", partyInvites);
          }
          
          partyConfig.set("names", partyNameList);
          partyConfig.save(partyFile);
          
       }catch(Exception e){
          getLogger().info("Error saving LittleRPG parties");
       }
    }
 }
