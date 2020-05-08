 package com.psyco.tplmc.CustomMessages.configuration;
 
 import com.psyco.tplmc.CustomMessages.CustomMessages;
 import com.psyco.tplmc.CustomMessages.MessageTypes;
 import com.psyco.tplmc.CustomMessages.Util;
 import com.psyco.tplmc.CustomMessages.configuration.defaultvariables.*;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.entity.Player;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 
 public class Configuration {
 
     private YamlConfiguration config;
     private final File configFile;
     private static final String colorString = "&0&&00&1&&11&2&&22&3&&33&4&&44&5&&55&6&&66&7&&77&8&&88&9&&99&a&&aa&b&&bb&c&&cc&d&&dd&e&&ee&f&&ff".replaceAll("(&([a-f0-9]))", "\u00A7$2");
     private final HashMap<String, MessageVariable> variables = new HashMap<String, MessageVariable>();
     private final HashMap<String, MessageVariable> defaultVariables = new HashMap<String, MessageVariable>();
 
     public Configuration(CustomMessages instance) {
         configFile = new File(instance.getDataFolder(), "config.yml");
         defaultVariables.put("/name", new VariableName());
         defaultVariables.put("/nname", new VariableNickName());
         defaultVariables.put("/count", new VariableCount());
         defaultVariables.put("/online", new VariableOnline());
         defaultVariables.put("/maxonline", new VariableMaxOnline());
         defaultVariables.put("/prefix", new VariablePrefix());
         defaultVariables.put("/suffix", new VariableSuffix());
         defaultVariables.put("/group", new VariableGroup());
         defaultVariables.put("/world", new VariableWorld());
     }
 
     //Basic Configuration loading and saving
     public boolean loadConfig() {
         try {
             config = YamlConfiguration.loadConfiguration(configFile);
             saveConfig();
             return true;
         } catch (Exception e) {
             e.printStackTrace();
             return false;
         }
     }
 
     public void setDefaults() {
         String defQuit = "&e/name &eleft the game.";
         if (config.isString("Config.Global-Quit-Message")) {
             defQuit = config.getString("Config.Global-Quit-Message");
             config.set("Config.Global-Quit-Message", null);
         }
         String defJoin = "&e/name &ejoined the game.";
         if (config.isString("Config.Global-Join-Message")) {
             defJoin = config.getString("Config.Global-Join-Message");
             config.set("Config.Global-Join-Message", null);
         }
         config.addDefault("config.must-have-permissions-to-show-current-messages", false);
         config.addDefault("config.send-messages-to-console", true);
         config.addDefault("config.debug", false);
         config.addDefault("config.global-join-message", defJoin);
         config.addDefault("config.global-join-message-enabled", true);
         config.addDefault("config.global-quit-message", defQuit);
         config.addDefault("config.global-quit-message-enabled", true);
         config.addDefault("config.global-firstjoin-message", "&dWelcome /name to the server!");
         config.addDefault("config.global-firstjoin-message-enabled", false);
         config.addDefault("config.global-kick-message", "&e/name &ewas kicked from the server.");
         config.addDefault("config.global-kick-message-enabled", false);
         config.addDefault("config.message-prefix", "[&aCM&f] ");
         config.addDefault("users.example.join", "&4Example joined the server");
         config.addDefault("users.example.join-enabled", true);
         config.addDefault("users.example.quit", "&5Example is gone now");
         config.addDefault("users.example.quit-enabled", true);
         config.addDefault("groups.examplegroup.join", "/prefix&4/name joined the game");
         config.addDefault("groups.examplegroup.join-enabled", true);
         config.addDefault("groups.examplegroup.quit", "/prefix&4/name left the game");
         config.addDefault("groups.examplegroup.quit-enabled", true);
         config.options().copyDefaults(true);
     }
 
     public void saveConfig() {
         try {
             setDefaults();
             config.save(configFile);
         } catch (IOException e) {
             e.printStackTrace();
         }
     }
 
     //Generic method to get message, in decreasing precision
     public String getColoredMessage(Player p, MessageTypes action) {
         String name = p.getName();
         String message;
         if (!isPlayerMessageEnabled(p, action))
             return null;
         if (config.contains("users." + name + "." + action.getConfig())) {
             message = config.getString("users." + name + "." + action.getConfig());
             if (!p.hasPermission("CustomMessages.noprefix"))
                 message = getMessagePrefix() + message;
             return Util.translateColor(replaceVars(message, p, action));
         }
         String group = CustomMessages.getVaultCompat().getGroup(p);
         if (group != null) {
             if (!isGroupMessageEnabled(group.toLowerCase(), action))
                 return null;
             if (config.contains("groups." + group.toLowerCase() + "." + action.getConfig())) {
                 message = config.getString("groups." + group.toLowerCase() + "." + action.getConfig());
                 return Util.translateColor(replaceVars(message, p, action));
             }
         }
         message = getGlobalMessage(action);
         if (!isGlobalMessageEnabled(action))
             return null;
         return Util.translateColor(replaceVars(message, p, action));
     }
 
     //Player message stuff
     public String getPlayerMessage(Player p, MessageTypes action) {
         if (config.contains("users." + p.getName() + "." + action.getConfig()))
             return config.getString("users." + p.getName() + "." + action.getConfig());
         if (CustomMessages.getVaultCompat().getGroup(p) != null)
             return getGroupMessage(CustomMessages.getVaultCompat().getGroup(p), action);
         return getGlobalMessage(action);
     }
 
     public String setPlayerMessage(Player p, MessageTypes action, String message) {
         String name = p.getName();
         config.set("users." + name + "." + action.getConfig(), message);
         saveConfig();
         return Util.translateColor(message);
     }
 
     public boolean isPlayerMessageSet(Player p, MessageTypes action) {
         return config.contains("users." + p.getName() + "." + action.getConfig());
     }
 
     public void resetPlayerMessage(Player p, MessageTypes action) {
         config.set("users." + p.getName() + "." + action.getConfig(), null);
         saveConfig();
     }
 
     public boolean isPlayerMessageEnabled(Player p, MessageTypes action) {
         return config.getBoolean("users." + p.getName() + "." + action.getConfig() + "-enabled", true);
     }
 
     public boolean setPlayerMessageEnabled(Player p, MessageTypes action, boolean enabled) {
         if (isPlayerMessageEnabled(p, action) == enabled)
             return false;
         config.set("users." + p.getName() + "." + action.getConfig() + "-enabled", enabled);
         saveConfig();
         return true;
     }
 
     //Global message stuff
     public String getGlobalMessage(MessageTypes action) {
         return config.getString("config.global-" + action.getConfig() + "-message");
     }
 
     public String setGlobalMessage(MessageTypes action, String message) {
         config.set("config.global-" + action.getConfig() + "-message", message);
         saveConfig();
         return Util.translateColor(message);
     }
 
     public void resetGlobalMessage(MessageTypes action) {
         config.set("config.global-" + action.getConfig() + "-message", null);
         saveConfig();
     }
 
     //It is impossible to not have a global message set, but this method is for consistency
     @SuppressWarnings({"SameReturnValue", "UnusedParameters"})
     public boolean isGlobalMessageSet(MessageTypes action) {
         return true;
     }
 
     public boolean isGlobalMessageEnabled(MessageTypes action) {
         return config.getBoolean("config.global-" + action.getConfig() + "-message-enabled", true);
     }
 
     public boolean setGlobalMessageEnabled(MessageTypes action, boolean enabled) {
         if (isGlobalMessageEnabled(action) == enabled)
             return false;
         config.set("config.global-" + action.getConfig() + "-message-enabled", enabled);
         saveConfig();
         return true;
     }
 
     //Group message stuff
     public String getGroupMessage(String group, MessageTypes action) {
         group = group.toLowerCase();
         if (config.contains("groups." + group + "." + action.getConfig()))
             return config.getString("groups." + group + "." + action.getConfig());
         return getGlobalMessage(action);
     }
 
     public String setGroupMessage(String group, MessageTypes action, String message) {
         group = group.toLowerCase();
         config.set("groups." + group + "." + action.getConfig(), message);
         saveConfig();
         return Util.translateColor(message);
     }
 
     public boolean isGroupMessageSet(String group, MessageTypes action) {
         return config.contains("groups." + group.toLowerCase() + "." + action.getConfig());
     }
 
     public void resetGroupMessage(String group, MessageTypes action) {
         group = group.toLowerCase();
         config.set("groups." + group + "." + action.getConfig(), null);
         saveConfig();
     }
 
     public boolean isGroupMessageEnabled(String group, MessageTypes action) {
         group = group.toLowerCase();
         return config.getBoolean("groups." + group + "." + action.getConfig() + "-enabled", true);
     }
 
     public boolean setGroupMessageEnabled(String group, MessageTypes action, boolean enabled) {
         group = group.toLowerCase();
         if (isGroupMessageEnabled(group, action) == enabled)
             return false;
         config.set("groups." + group + "." + action.getConfig() + "-enabled", enabled);
         saveConfig();
         return true;
     }
 
     //Other configuration options
     public boolean permsRequired() {
         return config.getBoolean("config.must-have-permissions-to-show-current-messages");
     }
 
     public boolean logToConsole() {
         return config.getBoolean("config.send-messages-to-console");
     }
 
     public boolean isDebugging() {
         return config.getBoolean("config.debug");
     }
 
     public String getColorsString() {
         return colorString;
     }
 
     public String getMessagePrefix() {
         return config.getString("config.message-prefix");
     }
 
     public String replaceVars(String str, Player p, MessageTypes type) {
         for (Map.Entry<String, MessageVariable> entry : defaultVariables.entrySet()) {
             str = str.replaceAll(entry.getKey(), entry.getValue().getReplacement(p, type));
         }
         for (Map.Entry<String, MessageVariable> entry : variables.entrySet()) {
             try {
                 str = str.replaceAll(entry.getKey(), entry.getValue().getReplacement(p, type));
             } catch (Exception ex) {
                 CustomMessages.p.getLogger().severe("Encountered exception with the variable " + entry.getKey() + ": " + ex.toString() + ": " + ex.getMessage());
                 if (isDebugging()) {
                     ex.printStackTrace();
                 }
                 str = str.replaceAll(entry.getKey(), "");
             }
         }
         return str;
     }
 
     public boolean registerVariable(String variable, MessageVariable messageVariable) {
         if (!variables.containsKey(variable)) {
             variables.put(variable, messageVariable);
             if (isDebugging()) {
                 CustomMessages.p.getLogger().info("Variable " + variable + " registered by " + messageVariable.getClass());
             }
             return true;
         }
         return false;
     }
 
     public boolean unregisterVariable(String variable) {
         if (variables.containsKey(variable)) {
             variables.remove(variable);
             if (isDebugging()) {
                 CustomMessages.p.getLogger().info("Variable " + variable + " unregistered");
             }
             return true;
         }
         return false;
     }
 }
 
