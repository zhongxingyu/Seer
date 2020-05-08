 /**
  * TerraCraftTools(SuperSoapTools) - GuardPay.java
  * Copyright (c) 2013 Jeremy Koletar (jjkoletar), <http://jj.koletar.com>
  * Copyright (c) 2013 computerdude5000,<computerdude5000@gmail.com>
  *
  * TerraCraftTools is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * TerraCraftTools is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with TerraCraftTools.  If not, see <http://www.gnu.org/licenses/>.
  */
 
package main.java.terracrafttools.modules;
 
import main.java.terracrafttools.TerraCraftTools;
 import net.milkbowl.vault.economy.Economy;
 import net.milkbowl.vault.economy.EconomyResponse;
 import org.bukkit.ChatColor;
 import org.bukkit.World;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.HandlerList;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerMoveEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.logging.Logger;
 
 /**
  * GuardPay Module
  * <p/>
  * Module that pays online guards who have moved a configurable amount of money every 15 minutes (configurable).
  *
  * @author jjkoletar
  */
 public class GuardPay implements SuperSoapToolsModule, Listener {
     private Map<String, Payee> payees;
     private Map<String, Object> paidGroups;
     private TerraCraftTools plugin;
     private int taskId;
 
     /**
      * initModule enables the module called by {@link TerraCraftTools}
      */
     public void initModule(TerraCraftTools sst) {
         plugin = sst;
         payees = new HashMap<String, Payee>();
         paidGroups = plugin.getModuleConfig("GuardPay").getConfigurationSection("groups").getValues(false);
         for (World w : plugin.getServer().getWorlds()) {
             for (Player player : w.getPlayers()) {
                 evaluatePlayerForPay(player);
             }
         }
         plugin.commandRegistrar.registerCommand("checkpayroll", this);
         plugin.commandRegistrar.registerCommand("forcepayroll", this);
         plugin.getServer().getPluginManager().registerEvents(this, plugin);
         long interval = plugin.getModuleConfig("GuardPay").getLong("interval");
         taskId = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new GuardPayTask(this), interval * 60 * 20, interval * 60 * 20);
         plugin.logger.info("[TerraCraftTools][GuardPay] GuardPay Module Enabled!");
     }
 
     /**
      * deinitModule disables the module called by {@link TerraCraftTools}
      */
     public void deinitModule() {
         HandlerList.unregisterAll(this);
         plugin.commandRegistrar.unregisterCommand("checkpayroll", this);
         plugin.commandRegistrar.unregisterCommand("forcepayroll", this);
         plugin.getServer().getScheduler().cancelTask(taskId);
         payees = null;
         paidGroups = null;
         plugin.logger.info("[TerraCraftTools][GuardPay] GuardPay Module Disabled!");
         plugin = null;
     }
 
     /**
      * Run a payment cycle
      */
     public void callPayees() {
         for (Map.Entry<String, Payee> entry : payees.entrySet()) {
             entry.getValue().executePlayerPayment(plugin.getEconomyApi());
         }
     }
 
     protected boolean wouldGroupBePaid(String group) {
         return paidGroups.containsKey(group);
     }
 
     @EventHandler
     public void onPlayerJoin(PlayerJoinEvent event) {
         evaluatePlayerForPay(event.getPlayer());
     }
 
     @EventHandler
     public void onPlayerMove(PlayerMoveEvent event) {
         if (payees.containsKey(event.getPlayer().getName())) {
             payees.get(event.getPlayer().getName()).setActiveState(true);
         }
     }
 
     @EventHandler
     public void onPlayerQuit(PlayerQuitEvent event) {
         if (payees.containsKey(event.getPlayer().getName())) {
             payees.remove(event.getPlayer().getName());
         }
     }
 
     private void evaluatePlayerForPay(Player p) {
         //Build payment options as a map
         Map<String, Double> options = new HashMap<String, Double>();
         for (Map.Entry<String, Object> entry : paidGroups.entrySet()) {
             if (plugin.getPermissionsApi().playerInGroup(p, entry.getKey())) {
                 options.put(entry.getKey(), (Double) entry.getValue());
             }
         }
         //Player matched at least 1 paid group, right?
         if (options.size() == 0) {
             return;
         }
         //Pick highest pay
         double pay = Collections.max(options.values());
         //Find the group it actually was, if dupe, just pick what iterates first
         String group = "";
         for (Map.Entry<String, Object> entry : paidGroups.entrySet()) {
             if ((Double) entry.getValue() == pay) {
                 group = entry.getKey();
             }
         }
         payees.put(p.getName(), new Payee(p, group, pay));
     }
 
     /**
      * Returns true or false when a player types a command
      *
      * @param sender  sender who sent the message
      * @param command command that was sent
      * @param label   idk what this does.
      * @param args    arguments that were sent along with the command
      * @return true or false
      */
     public boolean callCommand(CommandSender sender, Command command, String label, String[] args) {
         if (command.getName().equalsIgnoreCase("checkpayroll")) {
             sender.sendMessage(ChatColor.RED + "Payees:");
             for (Map.Entry<String, Payee> entry : payees.entrySet()) {
                 sender.sendMessage(ChatColor.RED + entry.getKey() + " - $" + entry.getValue().getPay() + " for " + entry.getValue().getGroup());
             }
             return true;
         } else if (command.getName().equalsIgnoreCase("forcepayroll")) {
             if (!sender.isOp()) {
                 sender.sendMessage(ChatColor.RED + "No Permission!");
                 return false;
             }
             callPayees();
             return true;
         }
         return false;
     }
 
     private static class GuardPayTask implements Runnable {
         private GuardPay module;
 
         public GuardPayTask(GuardPay gp) {
             module = gp;
         }
 
         public void run() {
             module.plugin.logger.info("[TerraCraftTools][GuardPay] Executing Payment!");
             module.callPayees();
         }
     }
 
     private static class Payee {
         private Player p;
         private String group;
         private double pay;
         private boolean active = false;
         private Logger logger = Logger.getLogger("Minecraft");
 
         public Payee(Player pl, String g, double pm) {
             p = pl;
             group = g;
             pay = pm;
         }
 
         public void setActiveState(boolean s) {
             active = s;
         }
 
         public double getPay() {
             return pay;
         }
 
         public String getGroup() {
             return group;
         }
 
         public void executePlayerPayment(Economy econ) {
             if (!active) {
                 return;
             }
             EconomyResponse er = econ.depositPlayer(p.getName(), pay);
             if (!er.transactionSuccess()) {
                 logger.severe("[TerraCraftTools][GuardPay] Unable to pay " + p.getName() + "! Error message: " + er.errorMessage);
             } else {
                 p.sendMessage(ChatColor.GREEN + "You have been paid $" + pay + " as part of your role as a " + ChatColor.LIGHT_PURPLE + group);
                 active = false;
             }
         }
     }
 }
