 package net.craftminecraft.bungee.bungeeyaml.supereasyconfig;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.Writer;
 
 import net.craftminecraft.bungee.bungeeyaml.bukkitapi.InvalidConfigurationException;
 import net.craftminecraft.bungee.bungeeyaml.bukkitapi.file.YamlConfiguration;
 
 /*
  * Bungee's SuperEasyConfig - Config
  * 
  * Based off of MrFigg's SuperEasyConfig v1.2
  * which was inspired by codename_Bs
  * which was inspired by md_5
  * which was inspired by... oh no, that's it.
  * 
  * Super Lazy Configuration for BungeeCord.
  * Includes support for Guava Library's collection types.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  * 
  * @author MrFigg
  * @author roblabla
  * 
  * @version 1.2
  */
 public abstract class Config extends ConfigObject {
 
     protected transient File CONFIG_FILE = null;
     protected transient String CONFIG_HEADER = null;
 
     public Config() {
         CONFIG_HEADER = null;
     }
 
     public Config load(File file) throws InvalidConfigurationException {
         if (file == null) {
             throw new InvalidConfigurationException(new NullPointerException());
         }
         if (!file.exists()) {
             throw new InvalidConfigurationException(new IOException("File doesn't exist"));
         }
         CONFIG_FILE = file;
         return reload();
     }
 
     public Config reload() throws InvalidConfigurationException {
         if (CONFIG_FILE == null) {
             throw new InvalidConfigurationException(new NullPointerException());
         }
         if (!CONFIG_FILE.exists()) {
             throw new InvalidConfigurationException(new IOException("File doesn't exist"));
         }
         YamlConfiguration yamlConfig = YamlConfiguration.loadConfiguration(CONFIG_FILE);
         try {
             onLoad(yamlConfig);
             yamlConfig.save(CONFIG_FILE);
         } catch (Exception ex) {
             throw new InvalidConfigurationException(ex);
         }
         return this;
     }
 
     public Config save(File file) throws InvalidConfigurationException {
         if (file == null) {
             throw new InvalidConfigurationException(new NullPointerException());
         }
         CONFIG_FILE = file;
         return save();
     }
 
     public Config save() throws InvalidConfigurationException {
         if (CONFIG_FILE == null) {
             throw new InvalidConfigurationException(new NullPointerException());
         }
         if (!CONFIG_FILE.exists()) {
             try {
                 if (CONFIG_FILE.getParentFile() != null) {
                     CONFIG_FILE.getParentFile().mkdirs();
                 }
                 CONFIG_FILE.createNewFile();
                 if (CONFIG_HEADER != null) {
                     Writer newConfig = new BufferedWriter(new FileWriter(CONFIG_FILE));
                    boolean firstLine = true;
                     for (String line : CONFIG_HEADER.split("\n")) {
                        if (!firstLine)
                            newConfig.write("\n");
                        else
                            firstLine = false;
                        newConfig.write("# " + line);
                     }
                     newConfig.close();
                 }
             } catch (Exception ex) {
                 throw new InvalidConfigurationException(ex);
             }
         }
         YamlConfiguration yamlConfig = YamlConfiguration.loadConfiguration(CONFIG_FILE);
         try {
             onSave(yamlConfig);
             yamlConfig.save(CONFIG_FILE);
         } catch (Exception ex) {
             throw new InvalidConfigurationException(ex);
         }
         return this;
     }
 
     public Config init(File file) throws InvalidConfigurationException {
         if (file == null) {
             throw new InvalidConfigurationException(new NullPointerException());
         }
         CONFIG_FILE = file;
         return init();
     }
 
     public Config init() throws InvalidConfigurationException {
         if (CONFIG_FILE == null) {
             throw new InvalidConfigurationException(new NullPointerException());
         }
         if (CONFIG_FILE.exists()) {
             return reload();
         } else {
             return save();
         }
     }
 }
