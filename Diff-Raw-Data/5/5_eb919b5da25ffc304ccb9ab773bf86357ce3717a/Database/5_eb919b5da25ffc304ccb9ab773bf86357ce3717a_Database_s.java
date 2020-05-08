 package net.krinsoft.jobsuite.db;
 
 import net.krinsoft.jobsuite.Job;
 import net.krinsoft.jobsuite.JobCore;
 import net.krinsoft.jobsuite.JobItem;
 import org.bukkit.Material;
 import org.bukkit.enchantments.Enchantment;
 import org.bukkit.inventory.ItemStack;
 
 import java.sql.*;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Properties;
 
 /**
  * @author krinsdeath
  */
 public class Database {
     public enum Type {
         MySQL("com.mysql.jdbc.Driver"),
         SQLite("org.sqlite.JDBC");
 
         private String className;
 
         Type(String clazz) {
             this.className = clazz;
         }
 
         String getDriver() {
             return this.className;
         }
 
         public static Type getType(String type) {
             for (Type t : Type.values()) {
                 if (t.name().equalsIgnoreCase(type)) {
                     return t;
                 }
             }
             return SQLite;
         }
     }
     private final JobCore plugin;
     private Type type;
 
     private Connection connection;
 
     private Map<String, PreparedStatement> statements = new HashMap<String, PreparedStatement>();
 
     public Database(JobCore instance) {
         this.plugin = instance;
         this.type = Type.getType(this.plugin.getConfig().getString("database.type", "SQLite"));
         connect();
         makeDatabase();
         load();
         try {
             Statement state = connection.createStatement();
             String rowid = "SELECT next_id FROM jobsuite_schema ;";
             ResultSet result = state.executeQuery(rowid);
             int id = 0;
             while (result.next()) {
                 id = result.getInt(1);
                 plugin.getConfig().set("jobs.total", id);
             }
             plugin.getLogger().info("Total jobs: " + id);
             result.close();
             state.close();
         } catch (SQLException e) {
             plugin.getLogger().warning("An SQLException occurred: " + e.getMessage());
         }
     }
 
     public boolean connect() {
         try {
             if (connection != null && !connection.isClosed()) {
                 return true;
             }
             Properties props = new Properties();
             if (type == Type.MySQL) {
                 props.put("user", plugin.getConfig().getString("database.user", "root"));
                 props.put("password", plugin.getConfig().getString("database.password", "root"));
             }
             Class.forName(type.getDriver());
             String connURL = getDatabasePath();
             plugin.getLogger().info("Connection URL: " + connURL);
             connection = DriverManager.getConnection(connURL, props);
         } catch (ClassNotFoundException e) {
             plugin.getLogger().warning("Couldn't find database driver: " + e.getMessage());
             return false;
         } catch (SQLException e) {
             plugin.getLogger().warning("Couldn't connect to the database: " + e.getMessage());
             return false;
         }
         return true;
     }
 
     public void close() {
         if (connection == null) {
             return;
         }
         try {
             for (PreparedStatement state : statements.values()) {
                 if (state == null) { continue; }
                 state.close();
             }
             connection.close();
         } catch (SQLException e) {
             plugin.getLogger().warning("Error while closing connections!");
         } finally {
             plugin.getLogger().info("JobSuite database closed.");
         }
     }
 
     public void makeDatabase() {
         if (connection == null) {
             return;
         }
         try {
             if (!connect()) {
                 return;
             }
             Statement state = connection.createStatement();
             state.executeUpdate("CREATE TABLE IF NOT EXISTS jobsuite_schema (" +
                     "id INTEGER AUTO_INCREMENT, " +
                     "next_id INTEGER, " +
                     "PRIMARY KEY (id, next_id)" +
                     ");"
             );
             state.executeUpdate("CREATE TABLE IF NOT EXISTS jobsuite_base (" +
                     "id INTEGER AUTO_INCREMENT, " +
                    "job_id INTEGER NOT NULL, " +
                     "owner VARCHAR(32) NOT NULL, " +
                     "name TEXT, " +
                     "description TEXT, " +
                     "reward INTEGER, " +
                     "expiry BIGINT, " +
                     "locked_by VARCHAR(32) DEFAULT NULL, " +
                     "finished BOOLEAN DEFAULT false, " +
                     "claimed BOOLEAN DEFAULT false, " +
                    "PRIMARY KEY (id, job_id, owner) " +
                     ");"
             );
             state.executeUpdate("CREATE TABLE IF NOT EXISTS jobsuite_items (" +
                     "item_id INTEGER AUTO_INCREMENT, " +
                     "job_id INTEGER NOT NULL, " +
                     "item_entry INTEGER NOT NULL, " +
                     "enchantment_entry INTEGER NOT NULL, " +
                     "type TEXT, " +
                     "amount INTEGER, " +
                     "PRIMARY KEY (item_id, item_entry), " +
                     "FOREIGN KEY (job_id) REFERENCES jobsuite_base(job_id)" +
                     ");"
             );
             state.executeUpdate("CREATE TABLE IF NOT EXISTS jobsuite_enchantments (" +
                     "enchantment_id INTEGER AUTO_INCREMENT, " +
                     "job_id INTEGER NOT NULL, " +
                     "enchantment_entry INTEGER NOT NULL, " +
                     "enchantment INTEGER, " +
                     "power INTEGER," +
                     "PRIMARY KEY (enchantment_id), " +
                     "FOREIGN KEY (job_id) REFERENCES jobsuite_base(job_id)," +
                     "FOREIGN KEY (enchantment_entry) REFERENCES jobsuite_items(enchantment_entry)" +
                     ");"
             );
         } catch (SQLException e) {
             plugin.getLogger().warning("An SQLException occurred: " + e.getMessage());
         }
     }
 
     public void load() {
         try {
             if (!connect()) {
                 return;
             }
             Statement state = connection.createStatement();
             ResultSet base = state.executeQuery("SELECT * FROM jobsuite_base WHERE expiry > " + System.currentTimeMillis() + " AND claimed = 'false' ;");
             PreparedStatement itemState = prepare("SELECT * FROM jobsuite_items WHERE job_id = ? ORDER BY item_entry ASC ;");
             PreparedStatement enchState = prepare("SELECT * FROM jobsuite_enchantments WHERE enchantment_entry = ? ;");
             while (base.next()) {
                 Job job = new Job(base.getString("owner"), base.getString("name"), base.getInt("job_id"), base.getLong("expiry"));
                 job.setDescription(base.getString("description"));
                 job.setReward(base.getDouble("reward"));
                 job.lock(base.getString("locked_by"));
                 itemState.setInt(1, job.getId());
                 ResultSet items = itemState.executeQuery();
                 while (items.next()) {
                     Material type = Material.matchMaterial(items.getString("type"));
                     int amount = items.getInt("amount");
                     JobItem jItem = job.getItem(job.addItem(items.getInt("item_entry"), new ItemStack(type, amount)));
                     enchState.setInt(1, items.getInt("enchantment_entry"));
                     ResultSet ench = enchState.executeQuery();
                     while (ench.next()) {
                         Enchantment enchantment = Enchantment.getById(ench.getInt("enchantment"));
                         int level = ench.getInt("power");
                         jItem.addEnchant(enchantment, level);
                     }
                 }
                 plugin.getJobManager().addJob(job);
                 if (base.getBoolean("finished")) {
                     plugin.getJobManager().moveToClaims(job);
                 }
             }
         } catch (SQLException e) {
             plugin.getLogger().warning("An SQLException occurred: " + e.getMessage());
         }
     }
 
     public String getDatabasePath() {
         String path;
         if (type == Type.SQLite) {
             path = "jdbc:sqlite:" + plugin.getDataFolder().toString() + "/";
             return path += plugin.getConfig().getString("database.name", "jobsuite") + ".db";
         } else {
             path = "jdbc:mysql://" + plugin.getConfig().getString("database.host", "localhost");
             path += ":" + plugin.getConfig().getInt("database.port", 3306);
             return path += "/" + plugin.getConfig().getString("database.name", "jobsuite");
         }
     }
 
     public PreparedStatement prepare(String query) {
         if (!connect()) {
             return null;
         }
         PreparedStatement state = statements.get(query);
         if (state == null) {
             try {
                 state = connection.prepareStatement(query);
                 statements.put(query, state);
             } catch (SQLException e) {
                 plugin.getLogger().warning("Unable to prepare statement: " + e.getMessage());
                 return null;
             }
         }
         return state;
     }
 
 }
