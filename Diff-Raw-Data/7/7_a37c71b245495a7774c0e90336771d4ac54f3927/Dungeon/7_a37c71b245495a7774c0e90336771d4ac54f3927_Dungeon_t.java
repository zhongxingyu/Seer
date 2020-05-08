 /*
  * Copyright (C) 2012 MineStar.de 
  * 
  * This file is part of CastAway.
  * 
  * CastAway is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, version 3 of the License.
  * 
  * CastAway is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with CastAway.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package de.minestar.castaway.data;
 
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.TimeUnit;
 
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
 import org.bukkit.potion.PotionEffectType;
 
 import de.minestar.castaway.blocks.AbstractActionBlock;
 import de.minestar.castaway.core.CastAwayCore;
 import de.minestar.castaway.stats.BeginDungeonStat;
 import de.minestar.castaway.stats.DeathInDungeonStat;
 import de.minestar.castaway.stats.FinishDungeonStat;
 import de.minestar.minestarlibrary.stats.StatisticHandler;
 import de.minestar.minestarlibrary.utils.ConsoleUtils;
 import de.minestar.minestarlibrary.utils.PlayerUtils;
 
 public class Dungeon {
 
     private static SimpleDateFormat dateFormatter = new SimpleDateFormat();
 
     private final String creator;
     private final String name;
     private int ID;
 
     private Map<BlockVector, SingleSign> registeredSignsByVector;
     private List<SingleSign> registeredSigns;
     private Map<BlockVector, AbstractActionBlock> registeredBlocks;
     private Map<String, PlayerData> players;
 
     private final int hash;
 
     public Dungeon(int dungeonID, String name, String creator) {
         this.ID = dungeonID;
         this.name = name;
         this.creator = creator;
         this.registeredBlocks = new HashMap<BlockVector, AbstractActionBlock>();
         this.registeredSignsByVector = new HashMap<BlockVector, SingleSign>();
         this.registeredSigns = new ArrayList<SingleSign>();
         this.players = new HashMap<String, PlayerData>();
         this.hash = generateHash();
     }
 
     public Dungeon(String name, String creator) {
         this(0, name, creator);
     }
 
     // ///////////////////////////////////
     //
     // ACTIONBLOCKS
     //
     // ///////////////////////////////////
 
     public void registerBlocks(AbstractActionBlock... blocks) {
         for (AbstractActionBlock block : blocks)
             this.registeredBlocks.put(block.getVector(), block);
     }
 
     public void registerBlocks(Collection<AbstractActionBlock> blocks) {
         for (AbstractActionBlock block : blocks)
             this.registeredBlocks.put(block.getVector(), block);
     }
 
     public void unRegisterBlocks(BlockVector... blockPositions) {
         for (BlockVector blockPosition : blockPositions)
             this.registeredBlocks.remove(blockPosition);
     }
 
     public Map<BlockVector, AbstractActionBlock> getRegisteredBlocks() {
         return new HashMap<BlockVector, AbstractActionBlock>(this.registeredBlocks);
     }
 
     // ///////////////////////////////////
     //
     // SIGNS
     //
     // ///////////////////////////////////
 
     public void registerSigns(SingleSign... signs) {
         for (SingleSign sign : signs) {
             if (!this.registeredSignsByVector.containsKey(sign.getVector())) {
                 this.registeredSignsByVector.put(sign.getVector(), sign);
                 this.registeredSigns.add(sign);
             }
         }
     }
 
     public void registerSigns(Collection<SingleSign> signs) {
         for (SingleSign sign : signs) {
             if (!this.registeredSignsByVector.containsKey(sign.getVector())) {
                 this.registeredSignsByVector.put(sign.getVector(), sign);
                 this.registeredSigns.add(sign);
             }
         }
     }
 
     public void unRegisterSigns(BlockVector... blockPositions) {
         for (BlockVector blockPosition : blockPositions) {
             SingleSign sign = this.registeredSignsByVector.remove(blockPosition);
             if (sign != null) {
                 int index = 0;
                 INNER : for (SingleSign inList : this.registeredSigns) {
                     if (sign.equals(inList)) {
                         this.registeredSigns.remove(index);
                         break INNER;
                     }
                     ++index;
                 }
             }
         }
     }
 
     public Map<BlockVector, SingleSign> getRegisteredSignsByVector() {
         return new HashMap<BlockVector, SingleSign>(this.registeredSignsByVector);
     }
 
     public SingleSign getSignByVector(BlockVector vector) {
         return this.registeredSignsByVector.get(vector);
     }
 
     public int getSignCount() {
         return this.registeredSigns.size();
     }
 
     public int getPlaceForSign(SingleSign sign) {
         int count = 1;
         for (SingleSign inList : this.registeredSigns) {
             if (inList.equals(sign)) {
                 return count;
             }
             ++count;
         }
         return count;
     }
 
     public SingleSign getNextFreeSign() {
         for (SingleSign inList : this.registeredSigns) {
             if (!inList.isFilledWithPlayerData()) {
                 return inList;
             }
         }
         return null;
     }
 
     public boolean hasFreeSign() {
         return this.getNextFreeSign() != null;
     }
 
     // ///////////////////////////////////
     //
     // METHODS FOR PLAYERS
     //
     // ///////////////////////////////////
 
     public void playerJoin(PlayerData playerData) {
         playerData.joinDungeon(this);
 
         // get the player
         Player player = playerData.getPlayer();
 
         // regain health
         player.setHealth(20);
 
         // regain food
         player.setFoodLevel(20);
 
         // remove active potioneffects
        for (PotionEffect effect : player.getActivePotionEffects()) {
            PotionEffectType type = effect.getType();
             player.removePotionEffect(type);
         }
 
         // send info
         PlayerUtils.sendMessage(player, ChatColor.DARK_AQUA, "------------------------------");
         PlayerUtils.sendSuccess(player, "Herzlich Willkommen im Dungeon '" + this.getName() + "'.");
         PlayerUtils.sendInfo(player, "Ersteller: " + this.getAuthor());
         PlayerUtils.sendMessage(player, ChatColor.DARK_AQUA, "------------------------------");
 
         // HANDLE STATISTIC
         StatisticHandler.handleStatistic(new BeginDungeonStat(playerData.getPlayerName(), ID));
 
         this.players.put(playerData.getPlayerName(), playerData);
     }
 
     public void playerFinished(PlayerData playerData) {
         long time = System.currentTimeMillis() - playerData.getStartTime();
         // get the player
         Player player = playerData.getPlayer();
 
         // send info
         PlayerUtils.sendMessage(player, ChatColor.DARK_AQUA, "------------------------------");
         PlayerUtils.sendSuccess(player, "Herzlich Glckwunsch! Du hast den Dungeon '" + this.getName() + "' erfolgreich beendet!");
         PlayerUtils.sendSuccess(player, "Du hast dafr " + formatTime(time) + " bentigt");
        PlayerUtils.sendSuccess(player, "Schau dir deinen Platz in der Hall of Fame an! /warp HallOfFame");
         PlayerUtils.sendMessage(player, ChatColor.DARK_AQUA, "------------------------------");
 
         // SAVE STATS & update sign if successfull
         boolean hasAlreadyFinished = CastAwayCore.databaseManager.isWinner(this, playerData.getPlayerName());
         if (CastAwayCore.dungeonManager.addWinner(playerData.getDungeon(), playerData.getPlayerName(), time)) {
             if (!hasAlreadyFinished) {
                 // update sign
                 SingleSign sign = this.getNextFreeSign();
                 if (sign != null) {
                     String[] lines = new String[4];
                     // place
                     lines[0] = "---> " + this.getPlaceForSign(sign) + " <---";
                     // playername
                     lines[1] = playerData.getPlayerName();
                     // date
                     Date date = new Date();
                     dateFormatter.applyPattern("dd.MM.yyyy");
                     lines[2] = dateFormatter.format(date);
                     // time
                     dateFormatter.applyPattern("HH:mm:ss");
                     lines[3] = dateFormatter.format(date);
                     sign.fillWithInformation(lines);
                 }
             }
         } else {
             PlayerUtils.sendError(player, CastAwayCore.NAME, "Aufgrund eines Fehlers konnte deine Zeit nicht gespeichert werden :-(");
             PlayerUtils.sendInfo(player, "Bitte wende dich an einen Admin.");
         }
 
         // HANDLE STATISTIC
         StatisticHandler.handleStatistic(new FinishDungeonStat(playerData.getPlayerName(), ID));
 
         // update the player & the data
         this.playerQuit(playerData);
     }
 
     public void playerQuit(PlayerData playerData) {
         playerData.quitDungeon();
 
         // HANDLE STATISTIC
         StatisticHandler.handleStatistic(new DeathInDungeonStat(playerData.getPlayerName(), ID));
 
         this.players.remove(playerData.getPlayerName());
     }
 
     // ///////////////////////////////////
     //
     // MISC-METHODS
     //
     // ///////////////////////////////////
 
     private String formatTime(long millis) {
         long hours = TimeUnit.MILLISECONDS.toHours(millis);
         millis -= TimeUnit.HOURS.toMillis(hours);
         long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
         millis -= TimeUnit.MINUTES.toMillis(minutes);
         long seconds = TimeUnit.MILLISECONDS.toSeconds(millis);
         millis -= TimeUnit.SECONDS.toMillis(seconds);
 
         StringBuilder sb = new StringBuilder(50);
         sb.append(hours);
         if (hours == 1)
             sb.append(" Stunde ");
         else
             sb.append(" Stunden ");
 
         sb.append(minutes);
         if (minutes == 1)
             sb.append(" Minute ");
         else
             sb.append(" Minuten ");
 
         sb.append(seconds);
         if (seconds == 1)
             sb.append(" Sekunde ");
         else
             sb.append(" Sekunden ");
 
         sb.append(millis);
         if (millis == 1)
             sb.append(" Millisekunde");
         else
             sb.append(" Millisekunden");
 
         return sb.toString();
     }
 
     // ///////////////////////////////////
     //
     // FOR THE DUNGEON ITSELF
     //
     // ///////////////////////////////////
 
     public int getID() {
         return ID;
     }
 
     public String getName() {
         return name;
     }
 
     public String getAuthor() {
         return creator;
     }
 
     public void setID(int ID) {
         if (this.ID == 0)
             this.ID = ID;
         else
             ConsoleUtils.printError(CastAwayCore.NAME, "The dungeon " + this.toString() + " has already an database ID!");
     }
 
     public Map<String, PlayerData> getPlayers() {
         return new HashMap<String, PlayerData>(this.players);
     }
 
     @Override
     public String toString() {
         return "Dungeon = { Name=" + name + ", Creator=" + creator + ", ID=" + ID + " }";
     }
 
     @Override
     public boolean equals(Object that) {
         if (that == null)
             return false;
         if (!(that instanceof Dungeon))
             return false;
         if (that == this)
             return true;
 
         return this.ID == ((Dungeon) that).ID;
     }
 
     @Override
     public int hashCode() {
         return hash;
     }
 
     private int generateHash() {
         int tempHash = 17;
         tempHash *= this.name.hashCode();
         tempHash *= this.creator.hashCode();
 
         return (tempHash << 5) - tempHash;
     }
 }
