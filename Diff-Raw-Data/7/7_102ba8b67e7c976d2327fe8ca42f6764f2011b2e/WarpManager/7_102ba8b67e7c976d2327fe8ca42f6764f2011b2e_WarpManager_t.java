 /*
  * WarpManager.java
  * 
  * Copyright (c) 2012 Lolmewn <info@lolmewn.nl>. 
  * 
  * Sortal is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * Sortal is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with Sortal.  If not, see <http ://www.gnu.org/licenses/>.
  */
 
 package nl.lolmewn.sortal;
 
 import com.google.common.io.Files;
 import java.io.*;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Set;
 import java.util.logging.Level;
 import org.bukkit.configuration.file.YamlConfiguration;
 
 /**
  *
  * @author Lolmewn <info@lolmewn.nl>
  */
 
 public class WarpManager {
 
     private Main plugin;
     private HashMap<String, Warp> warps = new HashMap<String, Warp>();
     private HashMap<String, SignInfo> signs = new HashMap<String, SignInfo>();
     private HashMap<String, UserInfo> users = new HashMap<String, UserInfo>();
 
     private Main getPlugin() {
         return this.plugin;
     }
 
     public WarpManager(Main m) {
         this.plugin = m;
         try {
             this.checkOldVersion();
             this.loadWarps();
             this.loadSigns();
             this.loadUsers();
         } catch (Exception e) {
            m.getLogger().warning("Exception caught during loading of data: " + e.getMessage());
             //Be ready to do stuff, even if loading fails
         }
     }
 
     private void loadWarps() {
         if (this.getPlugin().getSettings().useMySQL()) {
             ResultSet set = this.getPlugin().getMySQL().executeQuery("SELECT * FROM "
                     + this.getPlugin().getWarpTable());
             if (set == null) {
                 this.getPlugin().getLogger().severe("Something is wrong with your MySQL database!");
                 this.getPlugin().getLogger().severe("Plugin is disabling!");
                 this.getPlugin().getServer().getPluginManager().disablePlugin(this.getPlugin());
                 return;
             }
             try {
                 while (set.next()) {
                     Warp w = this.addWarp(set.getString("name"), set.getString("world"),
                             set.getDouble("x"), set.getDouble("y"), set.getDouble("z"),
                             set.getFloat("yaw"), set.getFloat("pitch"));
                     if (set.getInt("uses") != -1) {
                         w.setUses(set.getInt("uses"));
                         w.setUsed(set.getInt("used"));
                         w.setUsedTotalBased(set.getBoolean("usedTotalBased"));
                     }
                     if (set.getInt("price") != -1) {
                         w.setPrice(set.getInt("price"));
                         w.setHasPrice(true);
                     }
                     if (set.getString("owner") != null && !set.getString("owner").equals("")) {
                         w.setOwner(set.getString("owner"));
                     }
                     if (this.getPlugin().getSettings().isDebug()) {
                         this.getPlugin().getLogger().log(Level.INFO, String.format("Warp loaded: %s", set.getString("name")));
                     }
                 }
             } catch (SQLException ex) {
                 this.getPlugin().getLogger().log(Level.SEVERE, null, ex);
             }
             this.getPlugin().getLogger().log(Level.INFO, String.format("Warps loaded: %s", this.warps.size()));
             return;
         }
         File warpFile = new File(plugin.getDataFolder(), "warps.yml");
         if (!warpFile.exists()) {
             try {
                 warpFile.createNewFile();
             } catch (IOException ex) {
                 this.getPlugin().getLogger().log(Level.SEVERE, null, ex);
             } finally {
                 return;
             }
         }
         YamlConfiguration c = YamlConfiguration.loadConfiguration(warpFile);
         for (String key : c.getConfigurationSection("").getKeys(false)) {
             Warp w = this.addWarp(key, c.getString(key + ".world"), c.getDouble(key + ".x"), c.getDouble(key + ".y"), c.getDouble(key + ".z"),
                     (float) c.getDouble(key + ".yaw"), (float) c.getDouble(key + ".pitch"));
             if (c.getInt(key + ".uses", -1) != -1) {
                 w.setUses(c.getInt(key + ".uses"));
                 w.setUsed(c.getInt(key + ".used"));
                 w.setUsedTotalBased(c.getBoolean(key + ".usedTotalBased"));
             }
             if (c.contains(key + ".owner")) {
                 w.setOwner(key + ".owner");
             }
             if (c.getInt(key + ".price", -1) != -1) {
                 w.setPrice(c.getInt(key + ".price"));
                 w.setHasPrice(true);
             }
         }
         this.getPlugin().getLogger().log(Level.INFO, String.format("Warps loaded: %s", this.warps.size()));
     }
 
     private void loadSigns() {
         if (this.getPlugin().getSettings().useMySQL()) {
             ResultSet set = this.getPlugin().getMySQL().executeQuery("SELECT * FROM "
                     + this.getPlugin().getSignTable());
             if (set == null) {
                 this.getPlugin().getLogger().severe("Something is wrong with your MySQL database!");
                 this.getPlugin().getLogger().severe("Plugin is disabling!");
                 this.getPlugin().getServer().getPluginManager().disablePlugin(this.getPlugin());
                 return;
             }
             try {
                 while (set.next()) {
                     String loc = set.getString("world") + 
                             set.getInt("x") + 
                             set.getInt("y") + 
                             set.getInt("z");
                     SignInfo added = this.addSign(loc);
                     added.setWarp(set.getString("warp"));
                     if (set.getInt("price") != -1) {
                         added.setPrice(set.getInt("price"));
                     }
                     if (set.getInt("uses") != -1) {
                         this.getSign(loc).setUses(set.getInt("uses"));
                         this.getSign(loc).setUsedTotalBased(set.getBoolean("usedTotalBased"));
                         this.getSign(loc).setUsed(set.getInt("used"));
                     }
                     if(set.getBoolean("isPrivate")){
                         added.setIsPrivate(true);
                         String[] userNames = set.getString("privateUsers").split(",");
                         for(String user : userNames){
                             added.addPrivateUser(user);
                         }
                     }
                     this.signs.put(loc, added);
                 }
             } catch (SQLException ex) {
                 this.getPlugin().getLogger().log(Level.SEVERE, null, ex);
             }
             this.getPlugin().getLogger().log(Level.INFO, String.format("Signs loaded: %s", this.signs.size()));
             return;
         }
         File signFile = new File(plugin.getDataFolder(), "signs.yml");
         if (!signFile.exists()) {
             try {
                 signFile.createNewFile();
             } catch (IOException ex) {
                 this.getPlugin().getLogger().log(Level.SEVERE, null, ex);
             } finally {
                 return;
             }
         }
         YamlConfiguration c = YamlConfiguration.loadConfiguration(signFile);
         for (String key : c.getConfigurationSection("").getKeys(false)) {
             //Key is location
             if (!key.contains(",")) {
                 //dafuq?
                this.getPlugin().debug("Key didn't contain comma: " + key);
                 continue;
             }
             SignInfo s = this.addSign(key);
             if(c.contains(key + ".warp")){
                 s.setWarp(c.getString(key+".warp"));
             }
             if(c.contains(key + ".price")){
                 s.setPrice(c.getInt(key+".price"));
             }
             if(c.contains(key+".uses")){
                 s.setUses(c.getInt(key+".uses"));
                 s.setUsed(c.getInt(key+".used"));
                 s.setUsedTotalBased(c.getBoolean(key+".usedTotalBased"));
             }
             if(c.contains(key + ".owner")){
                 s.setOwner(c.getString(key+".owner"));
             }
             if(c.getBoolean(key + ".private", false)){
                 s.setIsPrivate(true);
                 Set<String> userNames = c.getConfigurationSection(key + ".privateUsers").getKeys(false);
                 for(String user : userNames){
                     s.addPrivateUser(user);
                 }
             }
         }
         this.getPlugin().getLogger().log(Level.INFO, String.format("Signs loaded: %s", this.signs.size()));
     }
 
     private void loadUsers() {
         if (this.getPlugin().getSettings().useMySQL()) {
             ResultSet set = this.getPlugin().getMySQL().executeQuery("SELECT * FROM " + this.getPlugin().getUserTable());
             if (set == null) {
                 this.getPlugin().getLogger().severe("Something is wrong with your MySQL database!");
                 this.getPlugin().getLogger().severe("Plugin is disabling!");
                 this.getPlugin().getServer().getPluginManager().disablePlugin(this.getPlugin());
                 return;
             }
             try {
                 while (set.next()) {
                     String player = set.getString("player");
                     UserInfo info = this.getUserInfo(player);
                     if (set.getString("warp") == null) {
                         //location
                         info.addtoUsedLocation(set.getString("world") + "," + set.getInt("x") + "," + set.getInt("y") + "," + set.getInt("z"), set.getInt("used"));
                         continue;
                     }
                     info.addtoUsedWarp(set.getString("warp"), set.getInt("used"));
                 }
             } catch (SQLException ex) {
                 this.getPlugin().getLogger().log(Level.SEVERE, null, ex);
             }
             return;
         }
         File userFile = new File(plugin.getDataFolder(), "users.yml");
         if (!userFile.exists()) {
             try {
                 userFile.createNewFile();
             } catch (IOException ex) {
                 this.getPlugin().getLogger().log(Level.SEVERE, null, ex);
             } finally {
                 return;
             }
         }
         YamlConfiguration c = YamlConfiguration.loadConfiguration(userFile);
         for (String player : c.getConfigurationSection("").getKeys(false)) {
             UserInfo info = this.getUserInfo(player);
             for (String key : c.getConfigurationSection(player).getKeys(false)) {
                 if (!key.contains(",")) {
                     
                     //warp
                     info.addtoUsedWarp(key, c.getInt(player + "." + key));
                     continue;
                 }
                 info.addtoUsedLocation(key, c.getInt(player + "." + key, 0));
             }
         }
         this.getPlugin().getLogger().log(Level.INFO, String.format("Users loaded: %s", this.users.size()));
     }
 
     protected Warp addWarp(String name, String world, double x, double y, double z, float yaw, float pitch) {
         this.warps.put(name, new Warp(name, world, x, y, z, yaw, pitch));
         Warp w = this.getWarp(name);
         if (this.getPlugin().getSettings().useMySQL()) {
             w.save(this.getPlugin().getMySQL(), this.getPlugin().getWarpTable());
         } else {
             w.save(new File(plugin.getDataFolder(), "warps.yml"));
         }
         return w;
     }
 
     protected Warp addWarp(String name, String world, double x, double y, double z, float yaw, float pitch, int price) {
         Warp w = this.addWarp(name, world, x, y, z, yaw, pitch);
         w.setPrice(price);
         return w;
     }
 
     protected SignInfo addSign(String world, int x, int y, int z) {
         return this.addSign(world + "," + x + "," + y + "," + z);
     }
     
     protected SignInfo addSign(String key) {
         String[] s = key.split(",");
         this.signs.put(key, new SignInfo(s[0], Integer.parseInt(s[1]), Integer.parseInt(s[2]), Integer.parseInt(s[3])));
        this.getPlugin().debug("New sign created at " + key);
         return this.getSign(key);
     }
 
     protected UserInfo addUserInfo(String name) {
         this.users.put(name, new UserInfo(name));
         return this.getUserInfo(name);
     }
 
     protected UserInfo getUserInfo(String name) {
         if (this.hasUserInfo(name)) {
             return this.users.get(name);
         }
         return this.addUserInfo(name);
     }
 
     protected SignInfo getSign(String world, int x, int y, int z) {
         return this.getSign(world + "," + x + "," + y + "," + z);
     }
     
     protected SignInfo getSign(String locationString){
         return this.signs.get(locationString);
     }
 
     protected Warp removeWarp(String name) {
         if (!this.hasWarp(name)) {
             return null;
         }
         if (this.getPlugin().getSettings().useMySQL()) {
             this.warps.get(name).delete(this.getPlugin().getMySQL(), this.getPlugin().getWarpTable());
         } else {
             this.warps.get(name).delete(new File(plugin.getDataFolder(), "warps.yml"));
         }
         return this.warps.remove(name);
     }
 
     protected void removeSign(String world, int x, int y, int z) {
         SignInfo s = this.getSign(world + "," + x + "," + y + "," + z);
         if (s == null) {
             return;
         }
         if (this.getPlugin().getSettings().useMySQL()) {
             s.delete(this.getPlugin().getMySQL(), this.getPlugin().getSignTable());
         } else {
             s.delete(new File(plugin.getDataFolder(), "signs.yml"));
         }
     }
     
     protected boolean hasSignInfo(String world, double x, double y, double z){
         return this.hasSignInfo(world + "," + x + "," + y + "," + z);
     }
     
     protected boolean hasSignInfo(String locationInts) {
         return this.signs.containsKey(locationInts);
     }
 
     protected boolean hasWarp(String name) {
         if(this.warps.containsKey(name)){
             return true;
         }
         Set<String> warpList = this.warps.keySet();
         for(String compare: warpList){
             if(compare.equalsIgnoreCase(name)){
                 return true;
             }
         }
         for(String compare : warpList){
             if(compare.startsWith(name)){
                 return true;
             }
         }
         return false;
     }
 
     protected boolean hasUserInfo(String user) {
         return this.users.containsKey(user);
     }
 
     public Warp getWarp(String name) {
         if(this.warps.containsKey(name)){
             return this.warps.get(name);
         }
         Set<String> warpList = this.warps.keySet();
         for(String compare: warpList){
             if(compare.equalsIgnoreCase(name)){
                 return this.warps.get(compare);
             }
         }
         for(String compare : warpList){
             if(compare.startsWith(name)){
                 return this.warps.get(compare);
             }
         }
         return null;
     }
 
     public void saveData() {
         for (String name : this.warps.keySet()) {
             if (this.getPlugin().getSettings().isDebug()) {
                 this.getPlugin().getLogger().log(Level.INFO, String.format("[Debug] Saving warp %s", name));
             }
             if (this.getPlugin().getSettings().useMySQL()) {
                 this.warps.get(name).save(this.getPlugin().getMySQL(), this.getPlugin().getWarpTable());
             } else {
                 this.warps.get(name).save(new File(plugin.getDataFolder(), "warps.yml"));
             }
         }
         for (String loc : this.signs.keySet()) {
             if (this.getPlugin().getSettings().isDebug()) {
                 this.getPlugin().getLogger().log(Level.INFO, String.format("[Debug] Saving sign at %s", loc));
             }
             if (this.getPlugin().getSettings().useMySQL()) {
                 this.signs.get(loc).save(this.getPlugin().getMySQL(), this.getPlugin().getSignTable());
             } else {
                 this.signs.get(loc).save(new File(plugin.getDataFolder(), "signs.yml"));
             }
         }
         for (String user : this.users.keySet()) {
             if (this.getPlugin().getSettings().isDebug()) {
                 this.getPlugin().getLogger().log(Level.INFO, String.format("[Debug] Saving user %s", user));
             }
             if (this.getPlugin().getSettings().useMySQL()) {
                 this.users.get(user).save(this.getPlugin().getMySQL(), this.getPlugin().getUserTable());
             } else {
                 this.users.get(user).save(new File(plugin.getDataFolder(), "users.yml"));
             }
         }
     }
 
     public Set<Warp> getWarps() {
         Set<Warp> warpSet = new HashSet<Warp>();
         for (String warp : this.warps.keySet()) {
             warpSet.add(this.warps.get(warp));
         }
         return warpSet;
     }
 
     public Set<SignInfo> getSigns() {
         Set<SignInfo> warpSet = new HashSet<SignInfo>();
         for (String warp : this.signs.keySet()) {
             warpSet.add(this.signs.get(warp));
         }
         return warpSet;
     }
 
     private void checkOldVersion() {
         File old = new File("plugins" + File.separator + "Sortal" + File.separator + "warps.txt");
         if (old.exists()) {
             BufferedReader in1 = null;
             try {
                 //Old version!
                 this.getPlugin().getLogger().log(Level.INFO, "Old Sortal saving system found, converting!");
                 in1 = new BufferedReader(new FileReader(old));
                 String str;
                 while ((str = in1.readLine()) != null) {
                     if (str.startsWith("#")) {
                         continue;
                     }
                     String warp = "unknownWarp";
                     try {
                         String[] split = str.split("=");
                         warp = split[0];
                         String[] rest = split[1].split(",");
                         Warp w = this.addWarp(warp, rest[0], Double.parseDouble(rest[1]),Double.parseDouble(rest[2]), Double.parseDouble(rest[3]), 0,0);
                         if (rest.length == 5) {
                             //Also has a price
                             w.setPrice(Integer.parseInt(rest[4]));
                         }
                         this.getPlugin().getLogger().log(Level.INFO, String.format("Converted warp %s", warp));
                     } catch (ArrayIndexOutOfBoundsException e) {
                         this.getPlugin().debug(String.format("Warp %s couldn't be converted : Too little arguments!", warp));
                     }
                 }
             } catch (IOException ex) {
                 this.getPlugin().getLogger().log(Level.SEVERE, null, ex);
             } finally {
                 try {
                     in1.close();
                 } catch (IOException ex) {
                     this.getPlugin().getLogger().log(Level.WARNING, null, ex);
                 }
             }
             try {
                 Files.move(old, new File(old.getParentFile(), "warps_old.txt"));
             } catch (IOException ex) {
                 this.getPlugin().getLogger().log(Level.WARNING, null, ex);
             }
         }
         File oldSigns = new File(old.getParentFile(), "signs.txt");
         if (oldSigns.exists()) {
             try {
                 BufferedReader in1 = new BufferedReader(new FileReader(oldSigns));
                 String str;
                 while ((str = in1.readLine()) != null) {
                     if (str.startsWith("#")) {
                         continue;
                     }
                     if (!str.contains("=")) {
                         continue;
                     }
                     String[] split = str.split("=");
                     String warp = split[1];
                     String[] rest = split[0].split(",");
                     if (rest.length == 3) {
                         if (isInt(rest[0]) && isInt(rest[1]) && isInt(rest[2])) {
                             SignInfo s = this.addSign(this.getPlugin().getServer().getWorlds().get(0).getName(), Integer.parseInt(rest[0]), Integer.parseInt(rest[1]), Integer.parseInt(rest[2]));
                             s.setWarp(warp);
                             continue;
                         }
                         continue;
                     }
                     if (rest.length == 4) {
                         if (isInt(rest[0]) && isInt(rest[1]) && isInt(rest[2])) {
                             SignInfo s = this.addSign(rest[3], Integer.parseInt(rest[0]), Integer.parseInt(rest[1]), Integer.parseInt(rest[2]));
                             s.setWarp(warp);
                             continue;
                         } else if (isInt(rest[3]) && isInt(rest[1]) && isInt(rest[2])) {
                             SignInfo s = this.addSign(rest[0], Integer.parseInt(rest[1]), Integer.parseInt(rest[2]), Integer.parseInt(rest[3]));
                             s.setWarp(warp);
                             continue;
                         } else {
                             continue;
                         }
                     }
                 }
                 in1.close();
                 this.getPlugin().getLogger().log(Level.INFO, String.format("Managed to save %s signs!", this.warps.size()));
             } catch (FileNotFoundException e) {
                 this.getPlugin().getLogger().log(Level.WARNING, null, e);
             } catch (IOException e) {
                 this.getPlugin().getLogger().log(Level.WARNING, null, e);
             }
             try {
                 Files.move(oldSigns, new File(oldSigns.getParentFile(), "signs_old.txt"));
             } catch (IOException ex) {
                 this.getPlugin().getLogger().log(Level.WARNING, null, ex);
             }
             this.saveData();
         }
     }
 
     public boolean isInt(String str) {
         try {
             Integer.parseInt(str);
             return true;
         } catch (NumberFormatException e) {
             return false;
         }
     }
 
     
 
     
 }
