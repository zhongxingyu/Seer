 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package info.jeppes.ZoneCore;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.bukkit.Bukkit;
 import org.bukkit.configuration.InvalidConfigurationException;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.plugin.Plugin;
 
 /**
  *
  * @author Jeppe
  */
 public class ZoneConfig extends AsynchronizedYamlConfiguration{
     private final Plugin plugin;
     private File file;
     private String name;
     public ZoneConfig(Plugin plugin, File file){
         this(plugin,file,true);
     }
     public ZoneConfig(Plugin plugin, File file, boolean loadDefaults){
         this.plugin = plugin;
         this.file = file;
         this.name = file.getName();
         if(!file.exists()){
             try {
                 //Create directory if doesn't exist
                new File(file.getPath()).mkdirs();
                 //create file
                 file.createNewFile();
             } catch (IOException ex) {
                 Logger.getLogger(ZoneConfig.class.getName()).log(Level.SEVERE, null, ex);
             }
         }
         try {
             load(file);
         } catch (FileNotFoundException ex) {
             Logger.getLogger(ZoneConfig.class.getName()).log(Level.SEVERE, null, ex);
         } catch (IOException | InvalidConfigurationException ex) {
             Logger.getLogger(ZoneConfig.class.getName()).log(Level.SEVERE, null, ex);
         }
         if(loadDefaults){
             loadDefaults(plugin);
         }
     }
     
     public String getFileName(){
         return name;
     }
     
     @Override
     public String getName(){
         return getFileName();
     }
     public final boolean loadDefaults(Plugin plugin){
         return this.loadDefaults(plugin, file.getName());
     }
     public final boolean loadDefaults(Plugin plugin, String fileName){
         if(plugin == null){
             return false;
         }
         InputStream defConfigStream = plugin.getResource(fileName);
         if (defConfigStream != null) {
             YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
             loadDefaults(plugin,defConfig);
             return true;
         }
         return false;
     }
     public final boolean loadDefaults(Plugin plugin, YamlConfiguration config){
         setDefaults(config);
         options().copyDefaults(true);
         schedualSave();
         return true;
     }
     
     public void writeYML(String root, Object x){
         writeYML(this,file,root,x);
     }
     public static void writeYML(YamlConfiguration config, File configFile, String root, Object x){
         config.set(root, x);
         try {
             config.save(configFile);
         } catch (IOException ex) {
             Logger.getLogger(ZoneConfig.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
     public void deleteYML(String root){
         deleteYML(this,file,root);
     }
     public static void deleteYML(YamlConfiguration config, File configFile, String root){
         config.set(root, null);
         try {
             config.save(configFile);
         } catch (IOException ex) {
             Logger.getLogger(ZoneConfig.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
     public void writeYMLASync(String root, Object x){
         writeYMLASync(this,file,root,x);
     }
     public void writeYMLASync(final YamlConfiguration config, final File configFile, final String root, final Object x){
         Bukkit.getScheduler().scheduleAsyncDelayedTask(plugin, new Runnable(){
             @Override
             public void run() {
                 try {
                     config.set(root, x);
                     config.save(configFile);
                 } catch (IOException ex) {
                     Logger.getLogger(ZoneConfig.class.getName()).log(Level.SEVERE, null, ex);
                 }
             }
 
         });
     }
     public void deleteYMLASync(String root){
         deleteYMLASync(this,file,root);
     }
     public void deleteYMLASync(final YamlConfiguration config, final File configFile, final String root){
         Bukkit.getScheduler().scheduleAsyncDelayedTask(plugin, new Runnable(){
             @Override
             public void run() {
                 try {
                     config.set(root, null);
                     config.save(configFile);
                 } catch (IOException ex) {
                     Logger.getLogger(ZoneConfig.class.getName()).log(Level.SEVERE, null, ex);
                 }
             }
 
         });
     }
     
     @Override
     public boolean save(){
         try {
             save(file);
         } catch (IOException ex) {
             Logger.getLogger(ZoneConfig.class.getName()).log(Level.SEVERE, null, ex);
             return false;
         }
         return true;
     }
     
     @Override
     public String toString(){
         return "ZoneConfig[File name=\""+this.getName()+"\" path:\""+this.file.getPath()+"\"]";
     }
 }
