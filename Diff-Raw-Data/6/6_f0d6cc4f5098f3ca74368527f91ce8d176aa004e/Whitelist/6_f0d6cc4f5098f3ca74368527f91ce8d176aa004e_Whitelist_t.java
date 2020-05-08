 /*
  * Copyright (C) 2011 <silence@immortal-forces.net>
  *
  * This file is part of the Bukkit plugin Whitelist.
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston,
  * MA 02111-1307, USA.
  */
 
 package com.bukkit.silence.whitelist;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Properties;
 import java.util.Timer;
 import org.bukkit.ChatColor;
 import org.bukkit.Server;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.event.Event;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.PluginLoader;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.plugin.PluginManager;
 
 public class Whitelist extends JavaPlugin
 {
   //Constants
   private final String PROP_KICKMESSAGE = "kick-message";
   private final String PROP_WHITELIST_ADMINS = "whitelist-admins";
   private final String PROP_DISABLE_LIST = "disable-list-command";
   private final String FILE_WHITELIST = "whitelist.txt";
   private final String FILE_CONFIG = "whitelist.properties";
 
   //Attributes
   private final WLPlayerListener m_PlayerListner = new WLPlayerListener(this);
   private FileWatcher m_Watcher;
   private Timer m_Timer;
   private File m_Folder;
   private ArrayList<String> m_WhitelistAdmins;
   private ArrayList<String> m_WhitelistAllow;
   private String m_KickMessage;
   private boolean m_IsWhitelistActive;
   private boolean m_IsListCommandDisabled;
 
   public Whitelist(PluginLoader pluginLoader, Server instance, PluginDescriptionFile desc, File folder, File plugin, ClassLoader cLoader)
   {
     super(pluginLoader, instance, desc, folder, plugin, cLoader);
 
     m_Folder = folder;
     m_KickMessage = "";
     m_WhitelistAdmins = new ArrayList<String>();
     m_WhitelistAllow = new ArrayList<String>();
     m_IsWhitelistActive = true;
     m_IsListCommandDisabled = false;
   }
 
   public void onEnable()
   {
     // Register our events
     PluginManager pm = getServer().getPluginManager();
 
     pm.registerEvent(Event.Type.PLAYER_LOGIN, m_PlayerListner, Priority.Low, this);
     //pm.registerEvent(Event.Type.PLAYER_COMMAND, m_PlayerListner, Priority.Monitor, this);
 
     //Create folders and files
     if (!m_Folder.exists())
     {
       System.out.print("Whitelist: Config folder missing, creating...");
       m_Folder.mkdir();
       System.out.println("done.");
     }
     File fWhitelist = new File(m_Folder.getAbsolutePath() + File.separator + FILE_WHITELIST);
     if (!fWhitelist.exists())
     {
       System.out.print("Whitelist: Whitelist is missing, creating...");
       try
       {
         fWhitelist.createNewFile();
         System.out.println("done.");
       } catch (IOException ex)
       {
         System.out.println("failed.");
       }
     }
     //Start file watcher
     m_Watcher = new FileWatcher(fWhitelist);
     m_Timer = new Timer(true);
     m_Timer.schedule(m_Watcher, 0, 1000);
     
     File fConfig = new File(m_Folder.getAbsolutePath() + File.separator + FILE_CONFIG);
     if (!fConfig.exists())
     {
       System.out.print("Whitelist: Config is missing, creating...");
       try
       {
         fConfig.createNewFile();
         Properties propConfig = new Properties();
         propConfig.setProperty(PROP_KICKMESSAGE, "Sorry, you are not on the whitelist!");
         propConfig.setProperty(PROP_WHITELIST_ADMINS, "Name1,Name2,Name3");
         propConfig.setProperty(PROP_DISABLE_LIST, "false");
         BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(fConfig.getAbsolutePath()));
         propConfig.store(stream, "Auto generated config file, please modify");
         System.out.println("done.");
       } catch (IOException ex)
       {
         System.out.println("failed.");
       }
     }
     loadWhitelistSettings();
 
     PluginDescriptionFile pdfFile = this.getDescription();
     System.out.println(pdfFile.getName() + " version " + pdfFile.getVersion() + " is enabled!");
   }
 
   public void onDisable()
   {
     m_Timer.cancel();
     m_Timer.purge();
     m_Timer = null;
     System.out.println("Goodbye world!");
   }
 
   @Override
   public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args)
   {
     Player player = null;
     try
     {
       player = (Player)sender;
     }
     catch (Exception e)
     {
     }
 
     if ( player != null )
     {
       if ( !isAdmin(player.getName()) )
         return true;
     }
    
     if ( args.length < 1 )
     {
       return false;
     }
     if ( args[0].compareToIgnoreCase("help") == 0 )
     {
       sender.sendMessage(ChatColor.YELLOW + "Commands:");
       sender.sendMessage(ChatColor.YELLOW + "/whitelist reload  (reloads the whitelist and settings)");
       sender.sendMessage(ChatColor.YELLOW + "/whitelist add [player]  (adds a player to the whitelist)");
       sender.sendMessage(ChatColor.YELLOW + "/whitelist remove [player]  (removes a player from the whitelist)");
       sender.sendMessage(ChatColor.YELLOW + "/whitelist on|off  (actives/deactivates whitelist)");
       sender.sendMessage(ChatColor.YELLOW + "/whitelist list  (list whitelist entries)");
       return true;
     }
     if ( args[0].compareToIgnoreCase("reload") == 0 )
     {
       if ( reloadSettings() )
         sender.sendMessage(ChatColor.GREEN + "Settings and whitelist reloaded");
       else
         sender.sendMessage(ChatColor.RED + "Could not reload whitelist...");
       return true;
     }
     if(args[0].compareToIgnoreCase("add") == 0)
     {
       if ( args.length < 2 )
       {
         sender.sendMessage(ChatColor.RED + "Parameter missing: Player name");
       }
       else
       {
         if ( addPlayerToWhitelist( args[1] ) )
           sender.sendMessage(ChatColor.GREEN + "Player \"" + args[1] + "\" added");
         else
           sender.sendMessage(ChatColor.RED + "Could not add player \"" + args[1] + "\"");
       }
       return true;
     }
     if(args[0].compareToIgnoreCase("remove") == 0)
     {
       if ( args.length < 2 )
       {
         sender.sendMessage(ChatColor.RED + "Parameter missing: Player name");
       }
       else
       {
         if ( removePlayerFromWhitelist( args[1] ))
           sender.sendMessage(ChatColor.GREEN + "Player \"" + args[1] + "\" removed");
         else
           sender.sendMessage(ChatColor.RED + "Could not remove player \"" + args[1] + "\"");
       }
       return true;
     }
     if (args[0].compareToIgnoreCase("on") ==0)
     {
       setWhitelistActive(true);
       sender.sendMessage(ChatColor.GREEN + "Whitelist activated!");
       return true;
     }
     if (args[0].compareToIgnoreCase("off") ==0)
     {
       setWhitelistActive(false);
       sender.sendMessage(ChatColor.RED + "Whitelist deactivated!");
       return true;
     }
     if (args[0].compareToIgnoreCase("list") == 0 && !isListCommandDisabled())
     {
       sender.sendMessage(ChatColor.YELLOW + "Players on whitelist: " + ChatColor.GRAY + getFormatedAllowList());
       return true;
     }
     return false;
   }
 
   public boolean loadWhitelistSettings()
   {
     System.out.print("Whitelist: Trying to load whitelist and settings...");
     try
     {
       //1. Load whitelist.txt
       m_WhitelistAllow.clear();
       BufferedReader reader = new BufferedReader(new FileReader((m_Folder.getAbsolutePath() + File.separator + FILE_WHITELIST)));
       String line = reader.readLine();
       while (line != null)
       {
         m_WhitelistAllow.add(line);
         line = reader.readLine();
       }
       reader.close();
 
       //2. Load fWhitelist.properties
       Properties propConfig = new Properties();
       BufferedInputStream stream = new BufferedInputStream(new FileInputStream(m_Folder.getAbsolutePath() + File.separator + FILE_CONFIG));
       propConfig.load(stream);
       m_KickMessage = propConfig.getProperty(PROP_KICKMESSAGE);
       if (m_KickMessage == null)
       {
         m_KickMessage = "";
       }
       m_WhitelistAdmins.clear();
       String rawAdminList = propConfig.getProperty(PROP_WHITELIST_ADMINS);
       if (rawAdminList != null)
       {
         String[] admins = rawAdminList.split(",");
         if (admins != null)
         {
           m_WhitelistAdmins.addAll(Arrays.asList(admins));
         }
       }
       String rawDisableListCommand = propConfig.getProperty(PROP_DISABLE_LIST);
       if (rawDisableListCommand != null)
       {
         m_IsListCommandDisabled = Boolean.parseBoolean(rawDisableListCommand);
       }
       System.out.println("done.");
     } catch (Exception ex)
     {
       System.out.println("failed.");
       return false;
     }
     return true;
   }
 
   public boolean saveWhitelist()
   {
     try
     {
       BufferedWriter writer = new BufferedWriter(new FileWriter((m_Folder.getAbsolutePath() + File.separator + FILE_WHITELIST)));
       for (String player : m_WhitelistAllow)
       {
         writer.write(player);
         writer.newLine();
       }
       writer.close();
     } catch (Exception ex)
     {
       System.out.println(ex);
       return false;
     }
     return true;
   }
 
   public boolean isAdmin(String playerName)
   {
     for (String admin : m_WhitelistAdmins)
     {
       if (admin.compareToIgnoreCase(playerName) == 0)
       {
         return true;
       }
     }
     return false;
   }
 
   public boolean isOnWhitelist(String playerName)
   {
     for (String player : m_WhitelistAllow)
     {
       if (player.compareToIgnoreCase(playerName) == 0)
       {
         return true;
       }
     }
     return false;
   }
 
   public boolean addPlayerToWhitelist(String playerName)
   {
     if (!isOnWhitelist(playerName))
     {
       m_WhitelistAllow.add(playerName);
       return saveWhitelist();
     }
     return false;
   }
 
   public boolean removePlayerFromWhitelist(String playerName)
   {
     for (int i = 0; i < m_WhitelistAllow.size(); i++)
     {
       if (playerName.compareToIgnoreCase(m_WhitelistAllow.get(i)) == 0)
       {
         m_WhitelistAllow.remove(i);
         return saveWhitelist();
       }
     }
     return false;
   }
 
   public boolean reloadSettings()
   {
     return loadWhitelistSettings();
   }
 
   public String getKickMessage()
   {
     return m_KickMessage;
   }
 
   public String getFormatedAllowList()
   {
     String result = "";
     for (String player : m_WhitelistAllow)
     {
       if (result.length() > 0)
       {
         result += ", ";
       }
       result += player;
     }
     return result;
   }
 
   public boolean isWhitelistActive()
   {
     return m_IsWhitelistActive;
   }
 
   public void setWhitelistActive(boolean isWhitelistActive)
   {
     m_IsWhitelistActive = isWhitelistActive;
   }
 
   public boolean isListCommandDisabled()
   {
     return m_IsListCommandDisabled;
   }
 
   public boolean needReloadWhitelist()
   {
     if ( m_Watcher != null )
       return m_Watcher.wasFileModified();
     return false;
   }
 
   public void resetNeedReloadWhitelist()
   {
     if ( m_Watcher != null )
       m_Watcher.resetFileModifiedState();
   }
 }
