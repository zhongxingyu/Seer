 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package at.rcraft.minecoupon;
 
 import java.sql.*;
 import java.util.ArrayList;
 import java.util.Random;
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.java.JavaPlugin;
 
 /**
  *
  * @author Thomas Raith
  */
 public class MineCoupon extends JavaPlugin {
     
     Connection con = null;
     Statement stmt;
     ResultSet res;
  
     @Override
     public void onEnable(){
 
         loadConfig();
         
         System.out.println("[MineCoupon] Plugin by "+this.getDescription().getAuthors());
         
         System.out.println("[MineCoupon] Connecting to MySQL Database..."); 
          try{
              con = DriverManager.getConnection("jdbc:mysql://"+this.getConfig().getString("config.mysql.host")+":"+this.getConfig().getInt("config.mysql.port")+"/"+this.getConfig().getString("config.mysql.database"), this.getConfig().getString("config.mysql.username"), this.getConfig().getString("config.mysql.password"));
              
              stmt = (Statement) con.createStatement();
                           
              stmt.execute("CREATE TABLE IF NOT EXISTS `minecoupon` (`ID` int(11) NOT NULL AUTO_INCREMENT,`voucher_code` varchar(200) NOT NULL,`usage_left` int(11) NOT NULL,`valid_throug` bigint(20) NOT NULL,`command` varchar(500) NOT NULL,`used_by` varchar(1000) NOT NULL DEFAULT ';',PRIMARY KEY (`ID`)) ENGINE=InnoDB  DEFAULT CHARSET=latin1 AUTO_INCREMENT=3 ;");
              
              System.out.println("[MineCoupon] Connected to MySQL Database.");
          }
          catch (Exception e){
              System.out.println("[MineCoupon] Failed to connect to MySQL Database.");
              System.out.println("[MineCoupon] Disable MineCoupon ... ");
              
          }
          
         
         getServer().getPluginManager().registerEvents(new MineCouponListener(this), this);  
         
         // Metrics Plugin
         if (getConfig().getBoolean("config.allowpluginmetrics")){
             try {
                Metrics metrics = new Metrics(this);
                metrics.start();
                System.out.println("[MineCoupon] PluginMetrics enabled.");
             } catch (Exception e) {
                 System.out.println("[MineCoupon] Failed to activate PluginMetrics.");
             }
         }
         else {
             System.out.println("[MineCoupon] PluginMetrics disabled.");
         }
         //Metrics Plugin
     }
     
     @Override
     public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
         
         if(cmd.getName().equalsIgnoreCase("mc")){
             
             if (args.length == 0){
                 sender.sendMessage(ChatColor.GREEN+"-----------------------------------------------------");
                 sender.sendMessage(ChatColor.GREEN+this.getDescription().getFullName() +" by "+this.getDescription().getAuthors());
                 sender.sendMessage(ChatColor.GREEN+ "Type /mc help for help");
                 sender.sendMessage(ChatColor.GREEN+ "Type /mc perms for permissions");
                 sender.sendMessage(ChatColor.GREEN+"-----------------------------------------------------");
                 if (con == null)
                     sender.sendMessage(ChatColor.RED+"MySQL STATUS: Connection failed");
                 else    
                     sender.sendMessage(ChatColor.GREEN+"MySQL STATUS: Connection ready");
                 sender.sendMessage(ChatColor.GREEN+"-----------------------------------------------------");
             }
             else{
                 
                         
                 if (con == null){
                     sender.sendMessage(ChatColor.RED+ this.getConfig().getString("config.errormessages.mysqlconnection"));
                     return true;
                 }
                 
                 if (args[0].equalsIgnoreCase("create")){
                     
                     
                     if (args.length == 4)
                     {
                     
                     if(sender instanceof Player){
                         if(!permCheck((Player)sender, "minecoupon.create")){
                          sender.sendMessage(ChatColor.RED+ this.getConfig().getString("config.errormessages.nopermission"));
                             return true;
                           }
                     }
                 
                      try {
                          
                          long time_expire = System.currentTimeMillis() / 1000;
                          String[ ] zahl;
                          String code = generateCouponCode(this.getConfig().getInt("config.cuponcode.length"), this.getConfig().getString("config.cuponcode.allowedchars"));
                          if (args[2].endsWith("seconds") || args[2].endsWith("second") || args[2].endsWith("sec")){
                             zahl = args[2].split("sec");
                             time_expire = time_expire + Integer.parseInt(zahl[0]); 
                          }
                          
                          if (args[2].endsWith("minutes") || args[2].endsWith("minute") || args[2].endsWith("min")){
                             zahl = args[2].split("min");
                             time_expire = time_expire + Integer.parseInt(zahl[0])*60; 
                          }
                          
                          if (args[2].endsWith("hours") || args[2].endsWith("hour") || args[2].endsWith("h")){
                             zahl = args[2].split("h");
                             time_expire = time_expire + Integer.parseInt(zahl[0])*3600; 
                          }
                          
                          if (args[2].endsWith("days") || args[2].endsWith("day") || args[2].endsWith("d")){
                             zahl = args[2].split("d");
                             time_expire = time_expire + Integer.parseInt(zahl[0])*86400; 
                          }
                          
                          if (args[2].endsWith("weeks") || args[2].endsWith("week") || args[2].endsWith("w")){
                             zahl = args[2].split("w");
                             time_expire = time_expire + Integer.parseInt(zahl[0])*604800; 
                          }
                          
                          if (args[2].endsWith("months") || args[2].endsWith("month") || args[2].endsWith("mon")){
                             zahl = args[2].split("mon");
                             time_expire = time_expire + Integer.parseInt(zahl[0])*18144000; 
                          }
                         stmt.execute("INSERT INTO `minecoupon` (`ID`, `voucher_code`, `usage_left`, `valid_throug`, `command`) VALUES (NULL, '"+code+"', '"+args[1]+"', '"+time_expire+"', '"+args[3].replace('?', ' ')+"');");
                         sender.sendMessage(ChatColor.GREEN+ this.getConfig().getString("config.messages.coupongenerated") +" "+code);
                      } catch (SQLException ex) {
                         sender.sendMessage(ChatColor.RED+ this.getConfig().getString("config.errormessages.coupongenerateerror"));
                         sender.sendMessage("FAIL: "+ex);
                     }        
                 
                      return true;
                     }
                     else{
                         sender.sendMessage("Use: /mc create <usage> <valid trough> <command>");
                         return true;
                     }
                 }
                 
                 
                 if (args[0].equalsIgnoreCase("remove")){
                     
                     if(sender instanceof Player){
                         if(!permCheck((Player)sender, "minecoupon.remove")){
                          sender.sendMessage(ChatColor.RED+ this.getConfig().getString("config.errormessages.nopermission"));
                             return true;
                           }
                     }
                 
                      try {
                         stmt.execute("DELETE FROM `minecoupon` WHERE `voucher_code` = '" + args[1]+"'");
                         sender.sendMessage(ChatColor.GREEN+ this.getConfig().getString("config.messages.couponremoved"));
                      } catch (SQLException ex) {
                         sender.sendMessage(ChatColor.RED+ this.getConfig().getString("config.errormessages.coupongenerateerror"));
                     } 
                      
                      return true;
                 }
                 
                 if (args[0].equalsIgnoreCase("list")){
                     
                     if(sender instanceof Player){
                         if(!permCheck((Player)sender, "minecoupon.list")){
                          sender.sendMessage(ChatColor.RED+ this.getConfig().getString("config.errormessages.nopermission"));
                             return true;
                           }
                     }
                 
                      try {
                         res = stmt.executeQuery("SELECT * FROM `minecoupon`");
                         int counter = 1;
                         sender.sendMessage(ChatColor.GREEN+"COUPONS LIST");
                         sender.sendMessage(ChatColor.GREEN+"-----------------------------------------------------");
                         while (res.next() && !res.isClosed()){
                             if (res.getLong("valid_throug") > System.currentTimeMillis() / 1000L){
                                 sender.sendMessage(ChatColor.GREEN+""+counter+" "+res.getString("voucher_code")+" - "+res.getInt("usage_left")+" - "+res.getString("command"));
                             }
                             else {
                                 sender.sendMessage(ChatColor.GREEN+""+counter+" "+res.getString("voucher_code")+" - "+res.getInt("usage_left")+" - "+res.getString("command")+ChatColor.RED+" (Expired)");
                             }
                             counter++;
                         }
                      } catch (SQLException ex) {
                     } 
                      
                      return true;
                 }
                 
                 if (args[0].equalsIgnoreCase("help")){
                     sender.sendMessage(ChatColor.GREEN+"Apply Coupon: /mc <couponcode>");
                     sender.sendMessage(ChatColor.GREEN+"Create Coupon: /mc create <applications> <valid_trough> <command>");
                     sender.sendMessage(ChatColor.GREEN+"Remove Coupon: /mc remove <couponcode>");
                     return true;
                 }
                 
                 if (args[0].equalsIgnoreCase("perms")){
                     sender.sendMessage(ChatColor.GREEN+"Use MineCoupon: minecoupon.use");
                     sender.sendMessage(ChatColor.GREEN+"List Coupons: minecoupon.list");
                     sender.sendMessage(ChatColor.GREEN+"Create Coupons: minecoupon.create");
                     sender.sendMessage(ChatColor.GREEN+"Remove Coupons: minecoupon.remove");
                     sender.sendMessage(ChatColor.GREEN+"Check for updates at join: minecoupon.checkupdate");
                     return true;
                 }
                 
                 
                 //Ab hier wird Coupon bearbeitet!
                 if(sender instanceof Player){
                    if(!permCheck((Player)sender, "minecoupon.use")){
                          sender.sendMessage(ChatColor.RED+ this.getConfig().getString("config.errormessages.nopermission"));
                          return true;
                    }
                    
                    try {
                     res = stmt.executeQuery("SELECT * FROM `minecoupon`");
                     String[] users_used = null;
                     int i=1;
                     while (res.next() && !res.isClosed()){
                         if (args[0].equals(res.getString(2))){
                             users_used = res.getString("used_by").split(";");
                             for (i=1;i<users_used.length;i++){
                                 if(users_used[i].equals(sender.getName())){
                                     sender.sendMessage(ChatColor.RED+this.getConfig().getString("config.errormessages.couponused"));
                                     return true;
                                 }
                             }
                             if (res.getInt(3) != 0){
                                 if (res.getInt(4) > System.currentTimeMillis() / 1000L){
                                     String[] commands = res.getString("command").split(";");
                                     for (i=0; i<commands.length; i++){
                                            getServer().dispatchCommand(sender, commands[i].replaceAll("%player%", sender.getName()));
                                     }
                                     String used = res.getString(6)+sender.getName()+";";
                                     int id = res.getInt(1);
                                     stmt.execute("UPDATE `minecoupon` SET `usage_left` = '"+(res.getInt(3)-1)+"' WHERE `ID` = "+id);
                                     stmt.execute("UPDATE `minecoupon` SET `used_by` = '"+used+"' WHERE `ID` = "+id);
                                     sender.sendMessage(ChatColor.GREEN+ this.getConfig().getString("config.messages.couponused"));
                                     res.close();
                                     return true;
                                 }
                                 else{
                                     sender.sendMessage(ChatColor.RED+ this.getConfig().getString("config.errormessages.couponexpired"));
                                 }
                                 
                             }
                             else{
                                 sender.sendMessage(ChatColor.RED+ this.getConfig().getString("config.errormessages.couponnoapplications"));
                             }
                             
                         }
                     }
                     
                 } catch (SQLException ex) {
                     
                 }
                    sender.sendMessage(ChatColor.RED+ this.getConfig().getString("config.errormessages.couponnotfound"));
                 }                
             }
         }
         
         return true;
     }
     
     
     private void loadConfig(){
       //String rundruf = this.getConfig().getString(rundruf);
        this.getConfig().options().header("MINECOUPON CONFIGURATION");
        this.getConfig().addDefault("config.mysql.host", "localhost");
        this.getConfig().addDefault("config.mysql.port", 3306);
        this.getConfig().addDefault("config.mysql.database", "minecraft");
        this.getConfig().addDefault("config.mysql.username", "root");
        this.getConfig().addDefault("config.mysql.password", "");
        this.getConfig().addDefault("config.cuponcode.length", 5);
        this.getConfig().addDefault("config.cuponcode.allowedchars", "0123456789abcdefghijklmnopqrstuvwxyz");
        this.getConfig().addDefault("config.messages.coupongenerated", "The coupon was generated successfully. Code:");
        this.getConfig().addDefault("config.messages.couponremoved", "The coupon was successfully removed.");
        this.getConfig().addDefault("config.messages.couponused", "The coupon has been applied successfully.");
        this.getConfig().addDefault("config.errormessages.coupongenerateerror", "There was an erroy while edit the coupon.");
        this.getConfig().addDefault("config.errormessages.couponexpired", "Coupon has expired.");
        this.getConfig().addDefault("config.errormessages.couponnoapplications", "The maximum number of applications has been reached.");
        this.getConfig().addDefault("config.errormessages.couponnotfound", "This coupon code is invalid.");
        this.getConfig().addDefault("config.errormessages.couponused", "You have already used this coupon code.");
        this.getConfig().addDefault("config.errormessages.nopermission", "You don't have the required permissons.");
        this.getConfig().addDefault("config.errormessages.mysqlconnection", "There is no MySQL connection.");
        this.getConfig().addDefault("config.update.message.check", "Check for updates ... ");
        this.getConfig().addDefault("config.update.message.newupdate", "A newer Version is available.");
        this.getConfig().addDefault("config.update.message.noupdate", "MineCoupon is up to date.");
        this.getConfig().addDefault("config.update.message.developementbuild", "You are using a developementbuild.");
        this.getConfig().addDefault("config.update.message.error", "Check for updates failed.");
        this.getConfig().addDefault("config.allowpluginmetrics", true);
        
        this.getConfig().options().copyDefaults(true);
        this.saveConfig();
     }
 
     public boolean permCheck(Player player, String permission){
         if(player.isOp() || player.hasPermission(permission)){
             return true;
         }
         return false;
     }
     
     private static String generateCouponCode(int length, String allowedChars) {
     Random random = new Random();
     int max = allowedChars.length();
     StringBuffer buffer = new StringBuffer();
     for (int i=0; i<length; i++) {
         int value = random.nextInt(max);
         buffer.append(allowedChars.charAt(value));
     }
     return buffer.toString();
     } 
     
     
 }
