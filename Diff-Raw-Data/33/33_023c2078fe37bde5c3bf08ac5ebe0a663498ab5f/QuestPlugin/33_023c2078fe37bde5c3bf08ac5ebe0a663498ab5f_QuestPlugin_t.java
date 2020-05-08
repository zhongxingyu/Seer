 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.mahn42.anhalter42.quest;
 
 import com.mahn42.anhalter42.quest.action.Action;
 import com.mahn42.anhalter42.quest.action.GenerateBlocks;
 import com.mahn42.anhalter42.quest.trigger.Trigger;
 import com.mahn42.framework.BlockPosition;
 import com.mahn42.framework.BuildingBlock;
 import com.mahn42.framework.BuildingDescription;
 import com.mahn42.framework.BuildingDetector;
 import com.mahn42.framework.Framework;
 import com.mahn42.framework.WorldClassification;
 import com.mahn42.framework.WorldDBList;
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 import org.bukkit.GameMode;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.block.BlockState;
 import org.bukkit.block.Sign;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.util.Vector;
 
 /**
  *
  * @author andre
  */
 public class QuestPlugin extends JavaPlugin {
 
     public int configQuestTaskTicks = 1;
     
     public static QuestPlugin plugin;
     
     public ArrayList<QuestTask> tasks = new ArrayList<QuestTask>();
     public WorldDBList<QuestBuildingDB> DBs;
 
     protected World fQuestWorld;
     protected QuestWorldAllocator fQWAllocator = new QuestWorldAllocator();
     
     public static void main(String[] args) {
     }
     
     public File getBuildingQuestFolder() {
         String lDataPath = getDataFolder().getPath();
         return new File(lDataPath + File.separatorChar + "BuildingQuests");
     }
     
     public File getTrapQuestFolder() {
         String lDataPath = getDataFolder().getPath();
         return new File(lDataPath + File.separatorChar + "TrapQuests");
     }
 
     public File getAdventureQuestFolder() {
         String lDataPath = getDataFolder().getPath();
         return new File(lDataPath + File.separatorChar + "AdventureQuests");
     }
 
     @Override
     public void onEnable() { 
         plugin = this;
         readQuestConfig();
         DBs = new WorldDBList<QuestBuildingDB>(QuestBuildingDB.class, plugin);
         Framework.plugin.registerSaver(DBs);
         Trigger.register();
         Action.register();
         GenerateBlocks.register();
         getCommand("q_start").setExecutor(new CommandQuestStart());
         getCommand("q_stop").setExecutor(new CommandQuestStop());
         getCommand("q_gentest").setExecutor(new CommandGeneratorTest());
         getServer().getPluginManager().registerEvents(new PlayerListener(), this);
         QuestBuildingHandler lHandler = new QuestBuildingHandler();
         BuildingDetector lDetector = Framework.plugin.getBuildingDetector();
         BuildingDescription lDesc;
         BuildingDescription.BlockDescription lBDesc;
         BuildingDescription.RelatedTo lRel;
         
         File lFolder = getDataFolder();
         if (!lFolder.exists()) {
             lFolder.mkdirs();
         }
         lFolder = getBuildingQuestFolder();
         if (!lFolder.exists()) {
             lFolder.mkdirs();
         }
         lFolder = getTrapQuestFolder();
         if (!lFolder.exists()) {
             lFolder.mkdirs();
         }
         lFolder = getAdventureQuestFolder();
         if (!lFolder.exists()) {
             lFolder.mkdirs();
         }
         
         lDesc = lDetector.newDescription("Quest.Starter");
         lDesc.typeName = "Quest Starter";
         lDesc.handler = lHandler;
         lBDesc = lDesc.newBlockDescription("base");
         lBDesc.materials.add(Material.SMOOTH_BRICK, (byte)3);
         lBDesc.detectSensible = true;
         lRel = lBDesc.newRelatedTo(new Vector(0, 2, 0), "base_top");
         lRel.materials.add(Material.SMOOTH_BRICK, (byte)3);
         lRel = lBDesc.newRelatedTo("sign", BuildingDescription.RelatedPosition.Nearby, 1);
         lRel = lBDesc.newRelatedTo("chest", BuildingDescription.RelatedPosition.Nearby, 1);
         lRel = lBDesc.newRelatedTo(new Vector(-4, 0, -4), "corner_1");
         lRel = lBDesc.newRelatedTo(new Vector(-4, 0,  4), "corner_2");
         lRel = lBDesc.newRelatedTo(new Vector( 4, 0, -4), "corner_3");
         lRel = lBDesc.newRelatedTo(new Vector( 4, 0,  4), "corner_4");
         lBDesc = lDesc.newBlockDescription("sign");
         lBDesc.materials.add(Material.SIGN);
         lBDesc.materials.add(Material.SIGN_POST);
         lBDesc.materials.add(Material.WALL_SIGN);
         lBDesc = lDesc.newBlockDescription("base_top");
         lBDesc.materials.add(Material.SMOOTH_BRICK, (byte)3);
         lRel = lBDesc.newRelatedTo("lever", BuildingDescription.RelatedPosition.Nearby, 1);
         lRel = lBDesc.newRelatedTo(new Vector(0, 1, 0), "base_lamp");
         lBDesc = lDesc.newBlockDescription("base_lamp");
         lBDesc.redstoneSensible = true;
         lBDesc.materials.add(Material.REDSTONE_LAMP_OFF);
         lBDesc.materials.add(Material.REDSTONE_LAMP_ON);
         lBDesc = lDesc.newBlockDescription("chest");
         lBDesc.materials.add(Material.CHEST);
         lBDesc = lDesc.newBlockDescription("lever");
         lBDesc.materials.add(Material.LEVER);
         lBDesc = lDesc.newBlockDescription("corner_1");
         lBDesc.materials.add(Material.BRICK);
         lRel = lBDesc.newRelatedTo(new Vector(0, 5, 0), "corner_1_top");
         lRel.materials.add(Material.BRICK);
         lBDesc = lDesc.newBlockDescription("corner_2");
         lBDesc.materials.add(Material.BRICK);
         lRel = lBDesc.newRelatedTo(new Vector(0, 5, 0), "corner_2_top");
         lRel.materials.add(Material.BRICK);
         lBDesc = lDesc.newBlockDescription("corner_3");
         lBDesc.materials.add(Material.BRICK);
         lRel = lBDesc.newRelatedTo(new Vector(0, 5, 0), "corner_3_top");
         lRel.materials.add(Material.BRICK);
         lBDesc = lDesc.newBlockDescription("corner_4");
         lBDesc.materials.add(Material.BRICK);
         lRel = lBDesc.newRelatedTo(new Vector(0, 5, 0), "corner_4_top");
         lRel.materials.add(Material.BRICK);
         lBDesc = lDesc.newBlockDescription("corner_1_top");
         lBDesc.materials.add(Material.BRICK);
         lBDesc = lDesc.newBlockDescription("corner_2_top");
         lBDesc.materials.add(Material.BRICK);
         lBDesc = lDesc.newBlockDescription("corner_3_top");
         lBDesc.materials.add(Material.BRICK);
         lBDesc = lDesc.newBlockDescription("corner_4_top");
         lBDesc.materials.add(Material.BRICK);
         lDesc.activate();
         //lDesc.createAndActivateXZ();
         fQuestWorld = Framework.plugin.requireWorld("world_quest", fWorldClassification);
     }
 
     @Override
     public void onDisable() {
         getServer().getScheduler().cancelTasks(this);
     }
     
     protected WorldClassification fWorldClassification;
     private void readQuestConfig() {
         FileConfiguration lConfig = getConfig();
         configQuestTaskTicks = lConfig.getInt("QuestTask.Ticks");
         fWorldClassification = new WorldClassification();
         fWorldClassification.fromSectionValue(lConfig.get("WorldClassification"));
         Framework.plugin.registerWorldClassification(fWorldClassification);
     }
     
 
     
     public void startQuest(Quest aQuest) {
         QuestTask lTask = new QuestTask();
         lTask.quest = aQuest;
         lTask.taskId = getServer().getScheduler().scheduleAsyncRepeatingTask(plugin, lTask, 10, configQuestTaskTicks);
         tasks.add(lTask);
     }
     
     public QuestTask getQuestTask(Quest aQuest) {
         for(QuestTask lTask : tasks) {
             if (lTask.quest == aQuest) {
                 return lTask;
             }
         }
         return null;
     }
     
     public QuestTask getQuestTask(String aQuestName) {
         for(QuestTask lTask : tasks) {
             if (lTask.quest.name.equalsIgnoreCase(aQuestName)) {
                 return lTask;
             }
         }
         return null;
     }
     
     public ArrayList<QuestTask> getQuestTasks(Location aLoc) {
         ArrayList<QuestTask> lResult = new ArrayList<QuestTask>();
         BlockPosition lPos = new BlockPosition(aLoc);
         for(QuestTask lTask : tasks) {
             if (lPos.isBetween(lTask.quest.edge1, lTask.quest.edge2)) {
                 lResult.add(lTask);
             }
         }
         return lResult;
     }
     
     public void stopQuest(Quest aQuest) {
         QuestTask lTask = getQuestTask(aQuest);
         if (lTask != null) {
             stopQuest(lTask);
         }
         if (aQuest.buildingQuest != null) {
             fQWAllocator.free(aQuest.buildingQuest);
         }
     }
 
     public void stopQuest(QuestTask aTask) {
         tasks.remove(aTask);
         getServer().getScheduler().cancelTask(aTask.taskId);
         aTask.finish();
     }
 
     public boolean startBuildingQuest(QuestBuilding lQB) {
         // get Quest
         BuildingBlock lSignBlock = lQB.getBlock("sign");
         Sign lSign = (Sign)lSignBlock.position.getBlock(lQB.world).getState();
         String lQuestName = lSign.getLine(0);
         Quest lQuest = loadBuildingQuest(lQuestName);
         BuildingQuest lBQ = new BuildingQuest();
         lBQ.building = lQB;
         lBQ.quest = lQuest;
         lQuest.buildingQuest = lBQ;
         if (lQuest.useQuestWorld) {
             lQuest.world = fQuestWorld;
             lBQ.questWorld = lQuest.world;
             QuestWorldAllocation lA = fQWAllocator.alloc(lBQ);
             lQuest.edge1 = lA.pos;
         } else {
             lQuest.world = lQB.world;
             lBQ.questWorld = lQuest.world;
             lQuest.edge1 = new BlockPosition(lQB.getBlock("base").position);
             lQuest.edge1.subtract(lQuest.startPos);
         }
         // get players
         List<Player> lPlayers = lQB.world.getPlayers();
         for(Player lPlayer : lPlayers) {
             BlockPosition lPlayerPos = new BlockPosition(lPlayer.getLocation());
             if (lPlayerPos.isBetween(lQB.edge1, lQB.edge2)) {
                 lQuest.players.add(lPlayer.getName());
                lQuest.log("Player " + lPlayer.getName() + " is now in quest " + lQuest.name);
             }
         }
         if (lQuest.players.size() > lQuest.maxPlayerCount) {
             lQuest.sendPlayerMessage("too many players for quest %s!", lQuest.name);
             return false;
         }
         if (lQuest.players.size() < lQuest.minPlayerCount) {
             lQuest.sendPlayerMessage("not enough players for quest %s!", lQuest.name);
             return false;
         }
         // start quest
         startQuest(lQuest);
         return true;
     }
 
     private Quest loadBuildingQuest(String lQuestName) {
         Quest lQuest = null;
         File lFolder = getBuildingQuestFolder();
         File lFile = new File(lFolder.getPath() + File.separatorChar + lQuestName);
         if (lFile.isDirectory()) {
             lFile = new File(lFile.getPath() + File.separatorChar + "start.yml");
         }
         lQuest = new Quest();
        lQuest.log("load quest file from " + lFile.toString());
         lQuest.load(lFile);
         return lQuest;
     }
     
     public String getText(String aText, Object... aObjects) {
         return getText((String)null, aText, aObjects);
     }
     
     public String getText(CommandSender aPlayer, String aText, Object... aObjects) {
         return getText(Framework.plugin.getPlayerLanguage(aPlayer.getName()), aText, aObjects);
     }
     
     public String getText(String aLanguage, String aText, Object... aObjects) {
         return Framework.plugin.getText(this, aLanguage, aText, aObjects);
     }
 }
