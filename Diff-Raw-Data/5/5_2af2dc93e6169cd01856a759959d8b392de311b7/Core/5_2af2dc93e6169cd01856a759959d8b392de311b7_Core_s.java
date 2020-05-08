 /*
  * Copyright (C) 2011 MineStar.de 
  * 
  * This file is part of TheRock.
  * 
  * TheRock is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, version 3 of the License.
  * 
  * TheRock is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with TheRock.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package de.minestar.therock;
 
 import org.bukkit.plugin.PluginManager;
 
 import de.minestar.minestarlibrary.AbstractCore;
 import de.minestar.minestarlibrary.commands.CommandList;
 import de.minestar.therock.commands.RollbackCommand;
 import de.minestar.therock.commands.SelectionCommand;
 import de.minestar.therock.commands.TheRockCommand;
 import de.minestar.therock.data.CacheHolder;
 import de.minestar.therock.database.DatabaseHandler;
 import de.minestar.therock.listener.BlockChangeListener;
 import de.minestar.therock.listener.ChatAndCommandListener;
 import de.minestar.therock.listener.SQLListener;
 import de.minestar.therock.listener.ToolListener;
 import de.minestar.therock.manager.MainConsumer;
 import de.minestar.therock.manager.MainManager;
 
 public class Core extends AbstractCore {
 
     public static Core INSTANCE;
 
     public static final String NAME = "TheRock";
 
     /** LISTENER */
     public static BlockChangeListener blockListener;
     public static ChatAndCommandListener playerListener;
     public static ToolListener toolListener;
     public static SQLListener sqlListener;
 
     /** MANAGER */
     public static DatabaseHandler databaseHandler;
     public static MainManager mainManager;
 
     /** CONSUMER */
     public static MainConsumer mainConsumer;
 
     /** CACHE */
     public static CacheHolder cacheHolder;
 
     @Override
     protected boolean createManager() {
         INSTANCE = this;
 
         databaseHandler = new DatabaseHandler(NAME, getDataFolder());
         if (!databaseHandler.hasConnection()) {
             return false;
         }
 
         // ToolManager
         toolListener = new ToolListener();
 
         // Queues
         mainConsumer = new MainConsumer();
 
         // WorldManager
         mainManager = new MainManager();
 
         // CacheHolder
         cacheHolder = new CacheHolder();
 
         return true;
     }
 
     @Override
     protected boolean createListener() {
         blockListener = new BlockChangeListener();
         playerListener = new ChatAndCommandListener();
         sqlListener = new SQLListener();
         return true;
     }
 
     @Override
     protected boolean commonDisable() {
         if (databaseHandler.hasConnection()) {
             mainConsumer.flushWithoutThread();
             databaseHandler.closeConnection();
         }
         return true;
     }
 
     @Override
     protected boolean registerEvents(PluginManager pm) {
         pm.registerEvents(blockListener, this);
         pm.registerEvents(playerListener, this);
         pm.registerEvents(toolListener, this);
         pm.registerEvents(sqlListener, this);
         return true;
     }
 
     @Override
     protected boolean createCommands() {
         //@formatter:off;
         this.cmdList = new CommandList(
                 new TheRockCommand    ("/tr", "", "",
                             new SelectionCommand ("selection",    "[ Player ] [ since ]",    "therock.tools.selection"),
                             new RollbackCommand ("rollback",      "",                        "therock.tools.rollback")
                           )
          );
         // @formatter: on;
         return true;
     }
 }
