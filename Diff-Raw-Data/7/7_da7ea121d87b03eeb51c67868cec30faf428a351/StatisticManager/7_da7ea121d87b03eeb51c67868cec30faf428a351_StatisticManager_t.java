 /*
  * Copyright (C) 2011 MineStar.de 
  * 
  * This file is part of ContaoPlugin.
  * 
  * ContaoPlugin is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, version 3 of the License.
  * 
  * ContaoPlugin is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with ContaoPlugin.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package de.minestar.contao2.manager;
 
 import java.util.Map.Entry;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ConcurrentMap;
 
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 
 import de.minestar.contao2.core.Core;
 import de.minestar.contao2.units.MCWarning;
 import de.minestar.contao2.units.PlayerWarnings;
 import de.minestar.contao2.units.Statistic;
 import de.minestar.minestarlibrary.utils.ChatUtils;
 
 public class StatisticManager implements Runnable {
 
     private ConcurrentMap<String, Statistic> statistics = new ConcurrentHashMap<String, Statistic>();
     private ConcurrentMap<String, PlayerWarnings> warnings = new ConcurrentHashMap<String, PlayerWarnings>();
     private DatabaseManager databaseManager;
 
     public StatisticManager(DatabaseManager dbManager) {
         this.databaseManager = dbManager;
         this.loadAllStatistics();
         this.loadAllWarnings();
     }
 
     public Statistic getPlayersStatistic(String playerName) {
         playerName = playerName.toLowerCase();
        Statistic thisStatistic = statistics.get(playerName);
        if (thisStatistic == null) {
            thisStatistic = new Statistic(0, 0);
            this.statistics.put(playerName, thisStatistic);
        }
        return thisStatistic;
     }
 
     private void loadAllStatistics() {
         statistics.putAll(databaseManager.loadAllStatistics());
     }
 
     private void loadAllWarnings() {
         warnings.putAll(databaseManager.loadAllWarnings());
     }
 
     @Override
     public void run() {
         for (Entry<String, Statistic> entry : statistics.entrySet()) {
             Statistic stats = entry.getValue();
             if (stats.hasChanged()) {
                 databaseManager.saveStatistics(entry.getKey().toLowerCase(), stats.getTotalPlaced(), stats.getTotalBreak());
                 stats.setHasChanged(false);
             }
         }
     }
 
     public void saveAllStatistics() {
         for (Entry<String, Statistic> entry : statistics.entrySet()) {
             Statistic stats = entry.getValue();
             if (stats.hasChanged()) {
                 databaseManager.saveStatistics(entry.getKey().toLowerCase(), stats.getTotalPlaced(), stats.getTotalBreak());
                 stats.setHasChanged(false);
             }
         }
     }
 
     public void initPlayerStatistic(String playerName) {
         playerName = playerName.toLowerCase();
         if (!this.statistics.containsKey(playerName)) {
             this.statistics.put(playerName.toLowerCase(), new Statistic(0, 0));
         }
     }
 
     public PlayerWarnings getWarnings(String playerName) {
         playerName = playerName.toLowerCase();
         return warnings.get(playerName);
     }
 
     public void addWarning(String playerName, MCWarning warning) {
         playerName = playerName.toLowerCase();
         PlayerWarnings thisPlayer = warnings.get(playerName);
         if (thisPlayer == null) {
             thisPlayer = new PlayerWarnings();
             warnings.put(playerName, thisPlayer);
         }
         thisPlayer.addWarning(warning);
     }
 
     public void printWarnings(Player player) {
         PlayerWarnings thisWarnings = this.getWarnings(player.getName());
         if (thisWarnings != null && thisWarnings.getWarnings().size() > 0) {
             ChatUtils.writeMessage(player, "");
             ChatUtils.writeColoredMessage(player, Core.pluginName, ChatColor.RED, "Du hast " + thisWarnings.getWarnings().size() + " Verwarnung" + (thisWarnings.getWarnings().size() > 1 ? "en" : "") + "!");
             for (MCWarning warning : thisWarnings.getWarnings()) {
                 ChatUtils.writeMessage(player, warning.toString());
             }
         }
     }
 
     public void printStatistics(Player player) {
         Statistic stats = this.getPlayersStatistic(player.getName());
 
         ChatUtils.writeMessage(player, "");
         if (stats == null)
             ChatUtils.writeColoredMessage(player, ChatColor.RED, "Du hast keine Statistiken!");
         else {
             ChatUtils.writeColoredMessage(player, ChatColor.BLUE, "Blcke zerstrt: " + stats.getTotalBreak());
             ChatUtils.writeColoredMessage(player, ChatColor.BLUE, "Blcke gesetzt  : " + stats.getTotalPlaced());
         }
     }
 }
