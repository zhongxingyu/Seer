 package com.lebelw.Tickets;
 
 import java.io.File;
 import java.util.List;
 
 import org.bukkit.util.config.Configuration;
 
 public class TConfig {
     private static Tickets plugin;
     public static String hostname;
     public static String type;
     public static String username;
     public static String password;
     public static String database;
     public TConfig(Tickets instance) {
         plugin = instance; 
     }
    public String directory = "plugins" + File.separator + plugin.getDescription().getName();
    File file = new File(directory + File.separator + "config.yml");
    
 
        
 
 
     public void configCheck(){
         
         new File(directory).mkdir();
         if(!file.exists()){
             try {
                 file.createNewFile();
                 addDefaults();
 
             } catch (Exception ex) {
                 ex.printStackTrace();
             }
         } else {
 
             loadkeys();
         }
     }
     private void write(String root, Object x){
         Configuration config = load();
         config.setProperty(root, x);
         config.save();
     }
     private Boolean readBoolean(String root){
         Configuration config = load();
         return config.getBoolean(root, true);
     }
 
     private Double readDouble(String root){
         Configuration config = load();
         return config.getDouble(root, 0);
     }
     private List<String> readStringList(String root){
         Configuration config = load();
         return config.getKeys(root);
     }
     private String readString(String root){
         Configuration config = load();
         return config.getString(root);
     }
     private int readInteger(String root,int def){
     	Configuration config = load();
     	return config.getInt(root, def);
     }
     private Configuration load(){
 
         try {
             Configuration config = new Configuration(file);
             config.load();
             return config;
 
         } catch (Exception e) {
             e.printStackTrace();
         }
         return null;
     }
     private void addDefaults(){
         TLogger.info("Generating Config File...");
     write("Database.type", "sqlite");
     write("Database.hostname", "localhost");
     write("Database.username", "root");
     write("Database.password", "");
     write("Database.database", "tickets");
      loadkeys();
     }
     private void loadkeys(){
         TLogger.info("Loading Config File...");
         type = readString("Database.type");
         hostname = readString("Database.hostname");
         username = readString("Database.username");
         password = readString("Database.password");
         database = readString("Database.database");
 
         }
 }
