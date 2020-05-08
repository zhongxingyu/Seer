 package nl.lolmewn.sortal;
 
 import java.io.*;
 import java.util.HashMap;
 import java.util.logging.Level;
 import org.bukkit.Bukkit;
 import org.bukkit.configuration.file.YamlConfiguration;
 
 /**
  *
  * @author Lolmewn
  */
 public class Localisation {
     
     private File localisation = new File("plugins" + File.separator + "Sortal"
             + File.separator + "localisation.yml");
     
     private String noPlayer;
     private String noPerms;
     private String createNameForgot;
     private String deleteNameForgot;
     private String nameInUse;
     private String warpCreated;
     private String warpDeleted;
     private String warpNotFound;
     private String paymentComplete;
     private String noMoney;
     private String noWarpsFound;
     private String errorInSign;
     private String playerTeleported;
 
     public Localisation() {
         this.checkFile();
         this.loadFile();
     }
 
     private void checkFile() {
         if(!this.localisation.exists()){
             Bukkit.getLogger().info("[Sortal] Trying to create default language file...");
             try {
                this.localisation.getParentFile().mkdirs();
                 InputStream in = this.getClass().
                         getClassLoader().getResourceAsStream("localisation.yml");
                 OutputStream out = new BufferedOutputStream(
                         new FileOutputStream(this.localisation));
                 int c;
                 while ((c = in.read()) != -1) {
                     out.write(c);
                 }
                 out.flush();
                 out.close();
                 in.close();
                 Bukkit.getLogger().info("[Sortal] Default language file created succesfully!");
             } catch (Exception e) {
                 e.printStackTrace();
                 Bukkit.getLogger().warning("[Sortal] Error creating language file! Using default settings!");
             }
         }
     }
 
     private void loadFile() {
         YamlConfiguration c = YamlConfiguration.loadConfiguration(this.localisation);
         this.createNameForgot = c.getString("commands.createNameForgotten");
         this.deleteNameForgot = c.getString("commands.deleteNameForgotten");
         this.nameInUse = c.getString("commands.nameInUse");
         this.noMoney = c.getString("noMoney");
         this.noPerms = c.getString("noPermissions");
         this.paymentComplete = c.getString("paymentComplete");
         this.warpCreated = c.getString("commands.warpCreated");
         this.warpDeleted = c.getString("commands.warpDeleted");
         this.warpNotFound = c.getString("commands.warpNotFound");
         this.noWarpsFound = c.getString("commands.noWarpsFound");
         this.errorInSign = c.getString("signError");
         this.playerTeleported = c.getString("playerTeleported");
         Bukkit.getLogger().info("[Sortal] Localisation loaded!");
     }
 
     public String getCreateNameForgot() {
         return createNameForgot;
     }
 
     public String getDeleteNameForgot() {
         return deleteNameForgot;
     }
 
     public String getNameInUse(String warp) {
         if(this.nameInUse.contains("$WARP") && warp != null && !warp.equals("")){
             return nameInUse.replace("$WARP", warp);
         }
         return nameInUse;
     }
 
     public String getNoMoney(String money) {
         if(this.noMoney.contains("$WARP") && money != null && !money.equals("")){
             return noMoney.replace("$MONEY", money);
         }
         return noMoney;
     }
 
     public String getNoPerms() {
         return noPerms;
     }
 
     public String getNoPlayer() {
         return noPlayer;
     }
 
     public String getPaymentComplete(String money) {
         if(this.paymentComplete.contains("$WARP") && money != null && !money.equals("")){
             return paymentComplete.replace("$WARP", money);
         }
         return paymentComplete;
     }
 
     public String getWarpCreated(String warp) {
         if(this.warpCreated.contains("$WARP") && warp != null && !warp.equals("")){
             return warpCreated.replace("$WARP", warp);
         }
         return warpCreated;
     }
 
     public String getWarpDeleted(String warp) {
         if(this.warpDeleted.contains("$WARP") && warp != null && !warp.equals("")){
             return warpDeleted.replace("$WARP", warp);
         }
         return warpDeleted;
     }
 
     public String getWarpNotFound(String warp) {
         if(this.warpNotFound.contains("$WARP") && warp != null && !warp.equals("")){
             return warpNotFound.replace("$WARP", warp);
         }
         return warpNotFound;
     }
 
     public String getNoWarpsFound() {
         return noWarpsFound;
     }
 
     public String getErrorInSign() {
         return errorInSign;
     }
 
     public String getPlayerTeleported(String dest) {
         if(this.playerTeleported.contains("$DEST") && dest != null && !dest.equals("")){
             return playerTeleported.replace("$DEST", dest);
         }
         return playerTeleported;
     }
 
     void addOld(HashMap<String, String> local) {
         try {
             YamlConfiguration c = YamlConfiguration.loadConfiguration(this.localisation);
             for(String path : local.keySet()){
                 c.set(path, local.get(path));
             }
             c.save(localisation);
         } catch (IOException ex) {
             Bukkit.getLogger().log(Level.SEVERE, null, ex);
         }
         this.loadFile();
     }
     
 }
