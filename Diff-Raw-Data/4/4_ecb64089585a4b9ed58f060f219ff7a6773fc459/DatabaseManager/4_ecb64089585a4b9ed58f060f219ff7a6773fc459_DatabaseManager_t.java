 package uk.codingbadgers.bstore.database;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Level;
 import uk.codingbadgers.bFundamentals.bFundamentals;
 
 import uk.thecodingbadgers.bDatabaseManager.Database.BukkitDatabase;
 import uk.thecodingbadgers.bDatabaseManager.DatabaseTable.DatabaseTable;
 
 public class DatabaseManager {
 
     /**
      * The database instance
      */
     BukkitDatabase database;
 
     /**
      * The purchase table
      */
     private DatabaseTable purchaseTable;
     
     /**
      * The investor table
      */
     private DatabaseTable investorTable;
     
     /**
      * The xp purchase table
      */
     private DatabaseTable xpTable;
 
     /**
      * Class constructor
      */
     public DatabaseManager() {
         setupDatabase();
     }
 
     /**
      * Setup the database and its tables
      */
     private void setupDatabase() {
         database = bFundamentals.getBukkitDatabase();
 
         purchaseTable = database.createTable("Store-Purchases", PurchaseData.class);
         if (purchaseTable == null) {
             bFundamentals.log(Level.SEVERE, "Failed to setup purchases table!");
         }
         
         investorTable = database.createTable("Store-Investors", InvestorData.class);
         if (investorTable == null) {
             bFundamentals.log(Level.SEVERE, "Failed to setup investors table!");
         }
         
         xpTable = database.createTable("Store-XP", XPPurchaseData.class);
         if (xpTable == null) {
             bFundamentals.log(Level.SEVERE, "Failed to setup xp table!");
         }
     }
 
     /**
      *
      * @param playerName
      * @param type
      * @param item
      */
     public void logPurchase(String playerName, String type, String item) {
 
         PurchaseData data = new PurchaseData();
         data.date = System.currentTimeMillis();
         data.item = item;
         data.type = type;
         data.playerName = playerName;
 
         this.purchaseTable.insert(data, PurchaseData.class, false);
 
     }
 
     /**
      *
      * @param playerName
      * @param from
      * @param to
      */
     public void logInvestor(String playerName, long from, long to, String oldRank, boolean update) {
 
         InvestorData data = new InvestorData();
         data.playerName = playerName;
         data.startTime = from;
         data.endTime = to;
         data.oldRank = oldRank;
 
         long lengthMS = to - from;
         long oneDay = 1000 * 60 * 60 * 24;
         long noofDays = lengthMS / oneDay;
 
         data.lengthDays = noofDays + " days";
 
         if (!update) {
             this.investorTable.insert(data, InvestorData.class, true);
         }
         else {
             String updateQuery = "UPDATE `Store-Investors` SET `endTime` = '" + 
                      data.endTime + "', `lengthDays` = '" + 
                      data.lengthDays + 
                      "' WHERE `playerName` = '" +
                      data.playerName + "'";
             database.query(updateQuery, true);
         }
 
     }
 
     /**
      *
      * @param playerName
      * @return
      */
     public InvestorData getInvestor(String playerName) {
 
         ResultSet result = database.queryResult("SELECT * FROM `Store-Investors` WHERE `playerName` = '" + playerName + "'");
         try {
             if (result == null) {
                 return null;
             }
 
             if (result.next()) {
 
                 InvestorData data = new InvestorData();
                 data.playerName = result.getString("playerName");
                 data.startTime = result.getLong("startTime");
                 data.endTime = result.getLong("endTime");
                 data.lengthDays = result.getString("lengthDays");
                 data.oldRank = result.getString("oldRank");
                 
                 result.close();
                 return data;
             } else {
                 result.close();
                 return null;
             }
         } catch (SQLException e) {
             e.printStackTrace();
             return null;
         }
 
     }
 
     /**
      * 
      * @param playerName 
      */
     public void removeInvestor(String playerName) {
         this.database.query("DELETE FROM `Store-Investors` WHERE `playerName`='" + playerName + "'");
     }
     
     /**
      * 
      * @param playerName
      * @param levels 
      */
     public void logXPPurchase(String playerName, int levels) {
         
         XPPurchaseData data = new XPPurchaseData();
         data.playerName = playerName;
         data.levels = levels;
         
         this.xpTable.insert(data, XPPurchaseData.class, true);
         
     }
     
     /**
      * 
      * @param playerName
      * @return 
      */
     public List<XPPurchaseData> getXPPurchases(String playerName) {
         
         List<XPPurchaseData> data = new ArrayList<XPPurchaseData>();
         
         ResultSet result = this.database.queryResult("SELECT * FROM `Store-XP` WHERE `playerName`='" + playerName + "'");
         if (result == null) {
             return data;
         }
         
         try {
             while(result.next()) {
                 XPPurchaseData newData = new XPPurchaseData();
                 newData.playerName = result.getString("playerName");
                 newData.levels = result.getInt("levels");
                 data.add(newData);
             }
         } catch(SQLException ex) {
             return data;
         }
             
         return data;
     }
     
     /**
      * 
      * @param playerName 
      */
     public void removeXPPurchases(String playerName) {
        this.database.query("DELETE FROM `Store-XP` WHERE `playerName`= '" + playerName + "'");
     }
 
 }
