 /*
  * Copyright (C) 2012 MineStar.de 
  * 
  * This file is part of FifthElement.
  * 
  * FifthElement is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, version 3 of the License.
  * 
  * FifthElement is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with FifthElement.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package de.minestar.FifthElement.core;
 
 import java.io.File;
 
 import de.minestar.FifthElement.commands.bank.cmdBank;
 import de.minestar.FifthElement.commands.bank.cmdBankInfo;
 import de.minestar.FifthElement.commands.bank.cmdSetBank;
 import de.minestar.FifthElement.commands.home.cmdHome;
 import de.minestar.FifthElement.commands.home.cmdHomeInfo;
 import de.minestar.FifthElement.commands.home.cmdSetHome;
 import de.minestar.FifthElement.commands.teleport.cmdTeleport;
 import de.minestar.FifthElement.commands.teleport.cmdTeleportHere;
 import de.minestar.FifthElement.commands.warp.cmdWarp;
 import de.minestar.FifthElement.commands.warp.cmdWarpCreate;
 import de.minestar.FifthElement.commands.warp.cmdWarpDelete;
 import de.minestar.FifthElement.commands.warp.cmdWarpInfo;
 import de.minestar.FifthElement.commands.warp.cmdWarpInvite;
 import de.minestar.FifthElement.commands.warp.cmdWarpMove;
 import de.minestar.FifthElement.commands.warp.cmdWarpPrivate;
 import de.minestar.FifthElement.commands.warp.cmdWarpPublic;
 import de.minestar.FifthElement.commands.warp.cmdWarpRandom;
 import de.minestar.FifthElement.commands.warp.cmdWarpRename;
 import de.minestar.FifthElement.commands.warp.cmdWarpUninvite;
 import de.minestar.FifthElement.database.DatabaseHandler;
 import de.minestar.FifthElement.manager.BankManager;
 import de.minestar.FifthElement.manager.HomeManager;
 import de.minestar.FifthElement.manager.WarpManager;
 import de.minestar.FifthElement.statistics.bank.BankInfoStat;
 import de.minestar.FifthElement.statistics.bank.BankStat;
 import de.minestar.FifthElement.statistics.bank.SetBankStat;
 import de.minestar.FifthElement.statistics.home.HomeInfoStat;
 import de.minestar.FifthElement.statistics.home.HomeStat;
 import de.minestar.FifthElement.statistics.home.SetHomeStat;
 import de.minestar.FifthElement.statistics.teleport.TeleportHereStat;
 import de.minestar.FifthElement.statistics.teleport.TeleportPlayerToStat;
 import de.minestar.FifthElement.statistics.teleport.TeleportToStat;
 import de.minestar.FifthElement.statistics.warp.PrivateWarpStat;
 import de.minestar.FifthElement.statistics.warp.PublicWarpStat;
 import de.minestar.FifthElement.statistics.warp.WarpCreateStat;
 import de.minestar.FifthElement.statistics.warp.WarpInviteStat;
 import de.minestar.FifthElement.statistics.warp.WarpMoveStat;
 import de.minestar.FifthElement.statistics.warp.WarpRenameStat;
 import de.minestar.FifthElement.statistics.warp.WarpToStat;
 import de.minestar.FifthElement.statistics.warp.WarpUninviteStat;
 import de.minestar.illuminati.IlluminatiCore;
 import de.minestar.minestarlibrary.AbstractCore;
 import de.minestar.minestarlibrary.annotations.UseStatistic;
 import de.minestar.minestarlibrary.commands.CommandList;
 
 @UseStatistic
 public class Core extends AbstractCore {
 
     public static final String NAME = "FifthElement";
 
     /* MANAGER */
     public static DatabaseHandler dbHandler;
     public static WarpManager warpManager;
     public static HomeManager homeManager;
     public static BankManager bankManager;
 
     public Core() {
         super(NAME);
     }
 
     @Override
     protected boolean loadingConfigs(File dataFolder) {
         return Settings.init(dataFolder, NAME, getDescription().getVersion());
     }
 
     @Override
     protected boolean createManager() {
         dbHandler = new DatabaseHandler(getDataFolder());
         if (!dbHandler.hasConnection())
             return false;
 
         warpManager = new WarpManager();
         homeManager = new HomeManager();
         bankManager = new BankManager();
 
         return true;
     }
 
     @Override
     protected boolean registerStatistics() {
 
         IlluminatiCore.registerStatistic(TeleportHereStat.class);
         IlluminatiCore.registerStatistic(TeleportToStat.class);
         IlluminatiCore.registerStatistic(TeleportPlayerToStat.class);
 
         IlluminatiCore.registerStatistic(PrivateWarpStat.class);
         IlluminatiCore.registerStatistic(PublicWarpStat.class);
         IlluminatiCore.registerStatistic(WarpCreateStat.class);
         IlluminatiCore.registerStatistic(WarpInviteStat.class);
         IlluminatiCore.registerStatistic(WarpMoveStat.class);
         IlluminatiCore.registerStatistic(WarpRenameStat.class);
         IlluminatiCore.registerStatistic(WarpToStat.class);
         IlluminatiCore.registerStatistic(WarpUninviteStat.class);
 
         IlluminatiCore.registerStatistic(BankInfoStat.class);
         IlluminatiCore.registerStatistic(BankStat.class);
         IlluminatiCore.registerStatistic(SetBankStat.class);
 
         IlluminatiCore.registerStatistic(HomeInfoStat.class);
         IlluminatiCore.registerStatistic(HomeStat.class);
         IlluminatiCore.registerStatistic(SetHomeStat.class);
 
         return true;
     }
 
     @Override
     protected boolean createCommands() {
 
         // @formatter:off
         cmdList = new CommandList(NAME,
 
                 // HOME COMMANDS
                 new cmdHome(        "/home",        "[HomeOwner]",          "fifthelement.command.home"),
                new cmdSetHome(     "/setHome",     "",                     "fifthelement.command.sethome"),
                new cmdHomeInfo(    "/homeInfo",    "[HomeOwner]",          "fifthelement.command.homeinfo"),
 
                 // BANK COMMANDS
                 new cmdBank(        "/bank",        "[BankOwner]",         "fifthelement.command.bank"),
                new cmdSetBank(     "/setBank",     "<BankOwner>",         "fifthelement.command.setbank"),
                new cmdBankInfo(    "/bankInfo",    "[BankOwner]",         "fifthelement.command.bankinfo"),
 
                 // TELEPORT COMMANDS
                 new cmdTeleport(    "/tp",          "<Target> [OtherTarget] | [x] [y] [z] [[world]]", ""),
                new cmdTeleportHere("/tpHere",      "<Target> ... [Target n]", "fifthelement.command.tphere"),
 
                 // WARP COMMANDS AND SUB COMMANDS
                 new cmdWarp(        "/warp",        "<Warp>",               "fifthelement.command.warp", 
                         
                         new cmdWarpCreate(  "create",       "<Warp>",                       "fifthelement.command.warpcreate"),
                         new cmdWarpCreate(  "pcreate",      "<Warp>",                       "fifthelement.command.warpcreate"),
                         new cmdWarpDelete(  "delete",       "<Warp>",                       "fifthelement.command.warpdelete"),
                         new cmdWarpInvite(  "invite",       "<Warp> <Player> ...[PlayerN]", "fifthelement.command.warpinvite"),
                         new cmdWarpUninvite("uninvite",     "<Warp> <Player> ...[PlayerN]", "fifthelement.command.warpuninvite"),
                         new cmdWarpMove(    "move",         "<Warp>",                       "fifthelement.command.warpmove"),
                         new cmdWarpRename(  "rename",       "<OldName> <NewName>",          "fifthelement.command.warprename"),
                         new cmdWarpRandom(  "random",       "",                             "fifthelement.command.warprandom"),
                         new cmdWarpPublic(  "public",       "<Warp>",                       "fifthelement.command.warppublic"),
                         new cmdWarpPrivate( "private",      "<Warp>",                       "fifthelement.command.warpprivate"),
                         new cmdWarpInfo(    "info",         "<Warp>",                       "fifthelement.command.warpinfo")
                 )
         );
         // @formatter:on
 
         return true;
     }
 
     @Override
     protected boolean commonDisable() {
         dbHandler.closeConnection();
 
         return !dbHandler.hasConnection();
     }
 }
