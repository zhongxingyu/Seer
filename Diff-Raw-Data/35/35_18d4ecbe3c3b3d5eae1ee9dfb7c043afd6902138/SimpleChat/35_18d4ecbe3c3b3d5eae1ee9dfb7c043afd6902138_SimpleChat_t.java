 /*
  * Copyright (C) 2012 Calenria <https://github.com/Calenria/> and contributors
  *
  * This program is free software: you can redistribute it and/or modify it under
  * the terms of the GNU Lesser General Public License as published by the Free
  * Software Foundation; either version 3.0 of the License, or (at your option)
  * any later version.
  *
  * This program is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
  * details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with this program. If not, see
  * <http://www.gnu.org/licenses/lgpl-3.0.html>.
  */
 package com.github.calenria.simplechat;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.PropertyResourceBundle;
 import java.util.ResourceBundle;
 import java.util.Vector;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import lib.PatPeter.SQLibrary.MySQL;
 import me.zford.jobs.bukkit.JobsPlugin;
 import net.milkbowl.vault.chat.Chat;
 import net.milkbowl.vault.economy.Economy;
 import net.milkbowl.vault.permission.Permission;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.RegisteredServiceProvider;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import com.github.calenria.simplechat.commands.SimpleChatCommands;
 import com.github.calenria.simplechat.listener.SimpleChatListener;
 import com.github.calenria.simplechat.listener.SimpleChatPluginListener;
 import com.sk89q.bukkit.util.CommandsManagerRegistration;
 import com.sk89q.minecraft.util.commands.CommandException;
 import com.sk89q.minecraft.util.commands.CommandPermissionsException;
 import com.sk89q.minecraft.util.commands.CommandUsageException;
 import com.sk89q.minecraft.util.commands.CommandsManager;
 import com.sk89q.minecraft.util.commands.MissingNestedCommandException;
 import com.sk89q.minecraft.util.commands.SimpleInjector;
 import com.sk89q.minecraft.util.commands.WrappedCommandException;
 
 /**
  * SimpleChat ein BukkitPlugin zum verteilen von Vote Belohnungen.
  * 
  * @author Calenria
  */
 public class SimpleChat extends JavaPlugin {
    public static final String REPLACE_STM               = "REPLACE INTO %s (`player`, `player_ds`, `x`, `y`, `z`, `world`, `server`, `group`) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
    public static final String REMOVE_STM                = "DELETE FROM %s where `player` = ? and server = ?";
 
    public static final String SELECT_STM                = "SELECT * FROM %s";
    public static final String SELECT_SERVERS            = "SELECT server FROM %s GROUP BY `server`";
    public static final String SELECT_PLAYERS_PER_SERVER = "SELECT * FROM %s where `server` = ?";
     /**
      * Standart Bukkit Logger.
      */
     private static Logger      log                       = Logger.getLogger("Minecraft");
 
     /**
      * Vault Economy.
      * 
      * @return the economy
      */
     public Economy getEconomy() {
         return economy;
     }
 
     /**
      * Kommandos.
      */
     private CommandsManager<CommandSender> commands;
 
     /**
      * Vault Permissions.
      */
     private Permission                     permission = null;
     private MySQL                          mysql;
 
     private String                         table;
     /**
      * Vault Economy.
      */
     private Economy                        economy    = null;
     private Vector<String>                 currOnline = new Vector<String>();
 
     /**
      * @param currOnline
      *            the currOnline to set
      */
     public void setCurrOnline(Vector<String> currOnline) {
         this.currOnline = currOnline;
     }
 
     public void reConSql() {
         setMysql(new MySQL(log, config.getPraefix(), config.getHostname(), config.getPort(), config.getDatabase(), config.getUser(), config.getPassword()));
     }
 
     private void initSql() {
         setMysql(new MySQL(log, config.getPraefix(), config.getHostname(), config.getPort(), config.getDatabase(), config.getUser(), config.getPassword()));
         if (getMysql().open()) {
             log.log(Level.INFO, String.format("[%s] Database enabled %s", getDescription().getName(), getDescription().getVersion()));
             setTable(config.getPraefix() + "online_player");
             if (!getMysql().isTable(getTable())) {
                 log.log(Level.INFO, String.format("[%s] Installing Table %s", getDescription().getName(), getTable()));
                 try {
                     getMysql()
                             .query("CREATE TABLE `"
                                     + getTable()
                                     + "` (`player` VARCHAR(50) NULL DEFAULT NULL, `player_ds` VARCHAR(50) NULL DEFAULT NULL, `x` DOUBLE NULL DEFAULT NULL, `y` DOUBLE NULL DEFAULT NULL,    `z` DOUBLE NULL DEFAULT NULL,    `world` VARCHAR(50) NULL DEFAULT NULL,    `server` VARCHAR(50) NULL DEFAULT NULL,    `group` VARCHAR(50) NULL DEFAULT NULL,    PRIMARY KEY (`player`),    UNIQUE INDEX `UNIQUE_PLAYER` (`player`)) COMMENT='Tabelle mit Spielern die auf allen Servern Online sind.' COLLATE='utf8_general_ci' ENGINE=InnoDB");
                 } catch (SQLException e) {
                     // TODO Auto-generated catch block
                     e.printStackTrace();
                 }
             }
         }
     }
 
     public void setupSheduler() {
         getServer().getScheduler().runTaskTimer(this, new Runnable() {
             public void run() {
                 Player[] players = Bukkit.getOnlinePlayers();
                 try {
                     if (!getMysql().checkConnection()) {
                         getMysql().open();
                     }
                    PreparedStatement pstm = getMysql().prepare(String.format(REPLACE_STM, getTable()));
                     for (Player player : players) {
                         pstm.setString(1, player.getName());
 
                         pstm.setString(2, parseName(player));
                         pstm.setDouble(3, player.getLocation().getX());
                         pstm.setDouble(4, player.getLocation().getY());
                         pstm.setDouble(5, player.getLocation().getZ());
                         pstm.setString(6, player.getWorld().getName());
                         pstm.setString(7, config.getServer());
                         try {
                             pstm.setString(8, getPermission().getPrimaryGroup(player));
                         } catch (UnsupportedOperationException e) {
                             pstm.setString(8, "Undefiniert");
                         }
                         pstm.executeUpdate();
                     }
                 } catch (SQLException e) {
                     e.printStackTrace();
                 }
             }
         }, 6000L, 6000L);
     }
 
     public void online(CommandSender sender) {
         try {
             if (!getMysql().checkConnection()) {
                 getMysql().open();
             }
            PreparedStatement serverps = getMysql().prepare(String.format(SELECT_SERVERS, getTable()));
             ResultSet servers = serverps.executeQuery();
             while (servers.next()) {
                 sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&2Spieler auf &4[&6" + servers.getString("server") + "&4]"));
                PreparedStatement pstm = getMysql().prepare(String.format(SELECT_PLAYERS_PER_SERVER, getTable()));
                 pstm.setString(1, servers.getString("server"));
                 ResultSet rs = pstm.executeQuery();
                 String playerlist = "";
                 while (rs.next()) {
                     playerlist += rs.getString("player_ds") + ", ";
                 }
                 sender.sendMessage(ChatColor.translateAlternateColorCodes('&', playerlist.substring(0, playerlist.length() - 2)));
             }
         } catch (SQLException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
 
     }
 
     /**
      * @return the table
      */
     public String getTable() {
         return table;
     }
 
     /**
      * @param table
      *            the table to set
      */
     public void setTable(String table) {
         this.table = table;
     }
 
     /**
      * @return the mysql
      */
     public MySQL getMysql() {
         return mysql;
     }
 
     /**
      * @param mysql
      *            the mysql to set
      */
     public void setMysql(MySQL mysql) {
         this.mysql = mysql;
     }
 
     /**
      * @param currOnline
      *            the currOnline to set
      */
     public void updateCurrOnline() {
         try {
             try {
                 if (!getMysql().checkConnection()) {
                     getMysql().open();
                 }
             } catch (Exception e) {
                 reConSql();
                 getMysql().open();
             }
            PreparedStatement pstmPlayers = getMysql().prepare(String.format(SELECT_STM, getTable()));
             ResultSet rs = pstmPlayers.executeQuery();
             Vector<String> players = new Vector<String>();
             while (rs.next()) {
                 players.add(rs.getString("player"));
             }
             setCurrOnline(players);
         } catch (SQLException e) {
             e.printStackTrace();
         }
     }
 
     /**
      * @return the currOnline
      */
     public String getOnlinePlayer(String name) {
         if (currOnline.contains(name)) {
             return name;
         } else {
             for (String onlinePlayer : currOnline) {
                 if (onlinePlayer.toLowerCase().startsWith(name.toLowerCase())) {
                     return onlinePlayer;
                 }
             }
         }
 
         return null;
     }
 
     public volatile Map<String, Chatter> chatters = new HashMap<String, Chatter>();
 
     /**
      * @return the chatters
      */
     public Chatter getChatter(String name) {
         if (chatters.containsKey(name)) {
             return chatters.get(name);
         }
         return null;
     }
 
     /**
      * @param chatters
      *            the chatters to set
      */
     public void setChatter(Chatter chatter) {
         this.chatters.put(chatter.getName(), chatter);
     }
 
     /**
      * @param chatters
      *            the chatters to set
      */
     public void setConversionPartner(String name, String conversionPartner) {
         if (chatters.containsKey(name)) {
             Chatter c = chatters.get(name);
             c.setConversionPartner(conversionPartner);
             chatters.remove(name);
             chatters.put(name, c);
         }
     }
 
     /**
      * @param chatters
      *            the chatters to set
      */
     public void removeConversionPartner(String name) {
         if (chatters.containsKey(name)) {
             Chatter c = chatters.get(name);
             c.removeConversionPartner();
             chatters.remove(name);
             chatters.put(name, c);
         }
     }
 
     public void removeChatter(String chatter) {
         if (chatters.containsKey(chatter)) {
             chatters.remove(chatter);
         }
     }
 
     /**
      * Vault Permissions.
      * 
      * @return the permission
      */
     public Permission getPermission() {
         return permission;
     }
 
     /**
      * ResourceBundle der I18N Strings.
      */
     private ResourceBundle    messages = null;
 
     /**
      * String der gewählten Sprache.
      */
     private String            lang     = "de";
 
     public SimpleChatListener listener;
 
     public Chat               chat;
 
     public ConfigData         config;
 
     public static boolean     jobs     = false;
 
     public static JobsPlugin  jobsPlugin;
 
     /**
      * Die aktuell gewählte Sprache.
      * 
      * @return the lang
      */
     public final String getLang() {
         return lang;
     }
 
     /**
      * ResourceBundle der I18N Strings.
      * 
      * @return the messages
      */
     public final ResourceBundle getMessages() {
         return messages;
     }
 
     /**
      * Delegiert die registierten Befehle an die jeweiligen Klassen und prüft ob die Benutzung korrekt ist.
      * 
      * @see org.bukkit.plugin.java.JavaPlugin#onCommand(org.bukkit.command.CommandSender, org.bukkit.command.Command, java.lang.String, java.lang.String[])
      * @param sender
      *            Der Absender des Befehls
      * @param cmd
      *            Das Kommando
      * @param label
      *            Das Label
      * @param args
      *            String Array von Argumenten
      * @return <tt>true</tt> wenn der Befehl erfolgreich ausgeführt worden ist
      */
     @Override
     public final boolean onCommand(final CommandSender sender, final Command cmd, final String label, final String[] args) {
         try {
             commands.execute(cmd.getName(), args, sender, sender);
         } catch (CommandPermissionsException e) {
             sender.sendMessage(ChatColor.RED + "Du hast keinen zugriff auf diesen Befehl!");
         } catch (MissingNestedCommandException e) {
             sender.sendMessage(ChatColor.RED + e.getUsage());
         } catch (CommandUsageException e) {
             sender.sendMessage(ChatColor.RED + e.getLocalizedMessage());
             sender.sendMessage(ChatColor.RED + e.getUsage());
         } catch (WrappedCommandException e) {
             if (e.getCause() instanceof NumberFormatException) {
                 sender.sendMessage(ChatColor.RED + "Zahl erwartet, erhielt aber eine Zeichenfolge.");
             } else {
                 sender.sendMessage(ChatColor.RED + "Ein Fehler ist aufgetreten, genaueres findest du in der Konsole");
                 e.printStackTrace();
             }
         } catch (CommandException e) {
             sender.sendMessage(ChatColor.RED + e.getMessage());
         }
         return true;
     }
 
     /**
      * Wird beim auschalten des Plugins aufgerufen.
      * 
      * @see org.bukkit.plugin.java.JavaPlugin#onDisable()
      */
     @Override
     public final void onDisable() {
        String sql = "DELETE FROM "+getTable()+" where server = ?";
         try {
             if (!getMysql().checkConnection()) {
                 getMysql().open();
             }
             PreparedStatement pstm = getMysql().prepare(sql);
             pstm.setString(1, config.getServer());
             pstm.executeUpdate();
         } catch (SQLException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
         log.log(Level.INFO, String.format("[%s] Disabled Version %s", getDescription().getName(), getDescription().getVersion()));
     }
 
     /**
      * Initialisierung des Plugins.
      * 
      * @see org.bukkit.plugin.java.JavaPlugin#onEnable()
      */
     @Override
     public final void onEnable() {
 
         if (getServer().getPluginManager().getPlugin("Jobs") != null) {
             jobs = true;
             jobsPlugin = (JobsPlugin) Bukkit.getServer().getPluginManager().getPlugin("Jobs");
             log.log(Level.INFO, String.format("[%s] Jobs %s found", getDescription().getName(), getServer().getPluginManager().getPlugin("Jobs").getDescription().getVersion()));
         }
 
         setupPermissions();
         setupEconomy();
         setupChat();
         setupCommands();
         setupListeners();
         setupConfig();
         initSql();
 
        String sql = "DELETE FROM "+getTable()+" where server = ?";
         try {
             if (!getMysql().checkConnection()) {
                 getMysql().open();
             }
             PreparedStatement pstm = getMysql().prepare(sql);
             pstm.setString(1, config.getServer());
             pstm.executeUpdate();
         } catch (SQLException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
 
         Bukkit.getMessenger().registerOutgoingPluginChannel(this, "SimpleChat");
         Bukkit.getMessenger().registerIncomingPluginChannel(this, "SimpleChat", new SimpleChatPluginListener(this));
 
         log.log(Level.INFO, String.format("[%s] Enabled Version %s", getDescription().getName(), getDescription().getVersion()));
     }
 
     /**
      * Liest die Eigentlichen Sprachdateien ein.
      * 
      * @param type
      *            muss gesetzt sein und repräsentiert entweder die Items oder Nachrichten
      * @return <tt>PropertyResourceBundle</tt>
      */
     private PropertyResourceBundle readProperties(final String type) {
         PropertyResourceBundle bundle = null;
         File messagesFile = new File(this.getDataFolder(), type + lang + ".properties");
         if (!messagesFile.exists()) {
             try {
                 Utils.copy(getResource(type + lang + ".properties"), messagesFile);
             } catch (Exception e) {
                 log.info("Keine " + type + " für " + lang + " gefunden! Erstelle standard Datei!");
                 try {
                     Utils.copy(getResource(type + "de.properties"), messagesFile);
                 } catch (IOException e1) {
                     log.log(Level.SEVERE, e.getLocalizedMessage());
                 }
             }
         }
 
         try {
             bundle = new PropertyResourceBundle(new FileInputStream(messagesFile));
         } catch (FileNotFoundException e) {
             e.printStackTrace();
         } catch (IOException e) {
             e.printStackTrace();
         }
         return bundle;
     }
 
     /**
      * Setzt die verwendete Sprache.
      * 
      * @param language
      *            Sprache welche verwendet werden soll
      */
     public final void setLang(final String language) {
         this.lang = language;
     }
 
     private boolean setupChat() {
         RegisteredServiceProvider<Chat> chatProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.chat.Chat.class);
         if (chatProvider != null) {
             chat = chatProvider.getProvider();
         }
 
         return (chat != null);
     }
 
     /**
      * Initialisierung der Plugin Befehle.
      */
     private void setupCommands() {
         this.commands = new CommandsManager<CommandSender>() {
             @Override
             public boolean hasPermission(final CommandSender sender, final String perm) {
                 return permission.has(sender, perm);
             }
         };
 
         commands.setInjector(new SimpleInjector(this));
         CommandsManagerRegistration cmdRegister = new CommandsManagerRegistration(this, this.commands);
         cmdRegister.register(SimpleChatCommands.class);
     }
 
     /**
      * Liest die Konfiguration aus und erzeugt ein ConfigData Objekt.
      */
     public final void setupConfig() {
         if (!new File(this.getDataFolder(), "config.yml").exists()) {
             this.getConfig().options().copyDefaults(true);
             this.saveConfig();
         } else {
             this.reloadConfig();
         }
         this.config = new ConfigData(this);
 
     }
 
     /**
      * Überprüft ob Vault vorhanden und ein passender Economyhandler verfügbar ist.
      * 
      * @return <tt>true</tt> wenn ein Vault Economyhandler gefunden wird.
      */
     private boolean setupEconomy() {
         RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
         if (economyProvider != null) {
             economy = economyProvider.getProvider();
         }
         return (economy != null);
     }
 
     /**
      * Initialisiert die Sprachdateien.
      */
     public final void setupLang() {
         messages = readProperties("messages_");
     }
 
     public void setupListeners() {
         listener = new SimpleChatListener(this);
     }
 
     /**
      * Überprüft ob Vault vorhanden und ein passender Permissionhandler verfügbar ist.
      * 
      * @return <tt>true</tt> wenn ein Vault Permissionhandler gefunden wird.
      */
     private boolean setupPermissions() {
         RegisteredServiceProvider<Permission> permissionProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
         if (permissionProvider != null) {
             permission = permissionProvider.getProvider();
         }
         return (permission != null);
     }
 
     public void removePlayer(final Player player) {
         try {
             try {
                 if (!getMysql().checkConnection()) {
                     getMysql().open();
                 }
             } catch (Exception e) {
                 reConSql();
                 getMysql().open();
             }
            PreparedStatement pstm = getMysql().prepare(String.format(REMOVE_STM, getTable()));
             pstm.setString(1, player.getName());
             pstm.setString(2, config.getServer());
             pstm.executeUpdate();
 
             updateCurrOnline();
 
         } catch (SQLException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
     }
 
     public void addPlayer(final Player player) {
         try {
             try {
                 if (!getMysql().checkConnection()) {
                     getMysql().open();
                 }
             } catch (Exception e) {
                 reConSql();
                 getMysql().open();
             }
            PreparedStatement pstm = getMysql().prepare(String.format(REPLACE_STM, getTable()));
             pstm.setString(1, player.getName());
             pstm.setString(2, parseName(player));
             pstm.setDouble(3, player.getLocation().getX());
             pstm.setDouble(4, player.getLocation().getY());
             pstm.setDouble(5, player.getLocation().getZ());
             pstm.setString(6, player.getWorld().getName());
             pstm.setString(7, config.getServer());
             try {
                 pstm.setString(8, getPermission().getPrimaryGroup(player));
             } catch (UnsupportedOperationException e) {
                 pstm.setString(8, "Undefiniert");
             }
             pstm.executeUpdate();
             updateCurrOnline();
         } catch (SQLException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
     }
 
     public String parseName(Player player) {
         String name = config.getName();
         name = name.replace("<player>", player.getName());
         name = name.replace("<playerdn>", player.getDisplayName());
         name = name.replace("<server>", config.getServer());
         if (chat != null) {
             name = name.replace("<prefix>", chat.getPlayerPrefix(player));
             name = name.replace("<suffix>", chat.getPlayerSuffix(player));
             name = name.replace("<group>", chat.getPrimaryGroup(player));
         }
         return name;
     }
 }
