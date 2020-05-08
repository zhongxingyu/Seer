 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.mahn42.anhalter42.quest;
 
 import com.mahn42.anhalter42.quest.action.Action;
 import com.mahn42.anhalter42.quest.action.GenerateBlocks;
 import com.mahn42.anhalter42.quest.trigger.Trigger;
 import com.mahn42.framework.BlockPosition;
 import com.mahn42.framework.Framework;
 import com.mahn42.framework.WorldDBList;
 import java.util.ArrayList;
 import org.bukkit.Location;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.plugin.java.JavaPlugin;
 
 /**
  *
  * @author andre
  */
 public class QuestPlugin extends JavaPlugin {
 
     public int configQuestTaskTicks = 1;
     
     public static QuestPlugin plugin;
     
     public ArrayList<QuestTask> tasks = new ArrayList<QuestTask>();
     public WorldDBList<QuestBuildingDB> DBs;
     
     public static void main(String[] args) {
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
     }
 
     @Override
     public void onDisable() {
         getServer().getScheduler().cancelTasks(this);
     }
     
     private void readQuestConfig() {
         FileConfiguration lConfig = getConfig();
         configQuestTaskTicks = lConfig.getInt("QuestTask.Ticks");
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
     }
 
     public void stopQuest(QuestTask aTask) {
         aTask.finish();
         tasks.remove(aTask);
         getServer().getScheduler().cancelTask(aTask.taskId);
     }
 }
