 package civchat.manager;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.logging.Logger;
 
 import org.bukkit.Location;
 
 import civchat.CivChat;
 import civchat.model.Antenna;
 import civchat.model.Antenna.DirtyAntennaReason;
 import civchat.model.Network.DirtyNetworkReason;
 import civchat.model.Network;
 import civchat.storage.DB;
 import civchat.storage.MySQL;
 
 public class StorageManager 
 {
 	private DB db;
 	private CivChat plugin;
 	private ConfigManager configManager;
 	private Logger log;
 
 	public StorageManager()
 	{
 		plugin        = CivChat.getInstance();
 		configManager = plugin.getConfigManager();
 		log           = CivChat.getLog();
 
 		initiateDB();
 	}
 
 	private void initiateDB()
 	{
 		String host     = configManager.getHost();
 		String database = configManager.getDatabase();
 		String username = configManager.getUsername();
 		String password = configManager.getPassword();
 		int port        = configManager.getPort();
 
 		db = new MySQL(host, port, database, username, password);
 
 		if(db.checkConnection())
 		{
 			log.info("dbMysqlConnected");
 
 			if(!db.existsTable("cc_network"))
 			{
 				log.info("Creating table: cc_network");
 				String query = "CREATE TABLE IF NOT EXISTS `cc_network` (`id` integer NOT NULL auto_increment, `name` varchar(100), `owner` varchar(100), PRIMARY KEY(`id`));";
 				db.execute(query);
 			}
 
 			if(!db.existsTable("cc_antenna"))
 			{
 				log.info("Creating table: cc_antenna");
 				String query = "CREATE TABLE IF NOT EXISTS `cc_antenna` (`id` integer NOT NULL auto_increment, `x` integer NOT NULL, `y` integer NOT NULL, `z` integer NOT NULL, `owner` varchar(100) NOT NULL, `damaged` TINYINT(1) DEFAULT 0, `network_id` integer, PRIMARY KEY(`id`), FOREIGN KEY (`network_id`) REFERENCES cc_network (`id`));";
 				db.execute(query);
 			}
 		}
 	}
 
 	public void insertAntenna(Antenna antenna)
 	{
 		String query  = "INSERT INTO `cc_antenna` (`x`, `y`, `z`, `owner`)";
 		String values = "VALUES (" + antenna.getX() + ", " + antenna.getY() + ", " + antenna.getZ() + ", '" + antenna.getOwner() + "')";	
 
 		synchronized (this)
 		{
 			db.insert(query + values);
 		}
 	}
 
 	public void updateAntenna(Antenna antenna)
 	{
 		String subQuery = "";
 
 		if(antenna.isDirty(DirtyAntennaReason.OWNER))
 		{
 			subQuery += "owner = " + antenna.getOwner() + ", ";
 		}
 		if(antenna.isDirty(DirtyAntennaReason.NETWORK))
 		{
 			subQuery += "network_id = " + antenna.getNetworkId() + ", ";
 		}
 		if(antenna.isDirty(DirtyAntennaReason.DAMAGED))
 		{
 			subQuery += "damaged = " + antenna.isDamaged();
 		}
 
 		if (!subQuery.isEmpty())
 		{
 			String query = "UPDATE `cc_antenna` SET " + subQuery + " WHERE x = " + antenna.getX() + " AND y = " + antenna.getY() + " AND z = " + antenna.getZ() + " AND owner = '" + antenna.getOwner() + "';";
 			db.execute(query);
 		}
 
 		antenna.clearDirty();
 	}
 
 	public Antenna findAntenna(Location location)
 	{
 		Antenna antenna = null;
 
		String query  = "SELECT * FROM `cc_antenna` WHERE x = " + (int) location.getX() + ", y = " + (int) location.getY() + ", z = " + (int) location.getY();
 		ResultSet res = db.select(query);
 		if(res != null)
 		{
 			try
 			{
 				while(res.next())
 				{
 					try {
 						int id          = res.getInt("id");
 						int x           = res.getInt("x");
 						int y           = res.getInt("y");
 						int z           = res.getInt("z");
 						String owner    = res.getString("owner");
 						boolean damaged = res.getBoolean("damaged");
 						int networkId   = res.getInt("network_id");
 
 						antenna = new Antenna(id, x, y, z, owner, damaged);
 					}
 					catch (Exception ex)
 					{
 						log.info(ex.getMessage());
 					}
 				}
 			}
 			catch (SQLException ex)
 			{
 				log.severe(ex.getMessage());
 			}
 		}
 
 		return antenna;
 	}
 
 	public void insertNetwork(Network network)
 	{
 		String query  = "INSERT INTO `cc_network` (`name`, `owner`)";
 		String values = "VALUES ('" + network.getName() + "', '" + network.getOwner() + "')";
 
 		synchronized (this)
 		{
 			db.insert(query + values);
 		}
 	}
 
 	public void updateNetwork(Network network)
 	{
 		String subQuery = "";
 
 		if(network.isDirty(DirtyNetworkReason.OWNER))
 		{
 			subQuery += "owner = " + network.getOwner();
 		}
 
 		if(!subQuery.isEmpty())
 		{
 			String query = "UPDATE `cc_network` SET " + subQuery + " WHERE name = " + network.getName();
 			db.execute(query);
 		}
 
 		network.clearDirty();
 	}
 
 	public Network findNetwork(String n)
 	{
 		Network network = null;
 
 		String query  = "SELECT * FROM `cc_network` WHERE name = '" + n + "'";
 		ResultSet res = db.select(query);
 		if(res != null)
 		{
 			try
 			{
 				while(res.next())
 				{
 					try 
 					{
 						int id       = res.getInt("id");
 						String name  = res.getString("name");
 						String owner = res.getString("owner");
 
 						network = new Network(id, name, owner);
 					}
 					catch (Exception ex)
 					{
 						log.info(ex.getMessage());
 					}
 				}
 			}
 			catch (SQLException ex)
 			{
 				log.severe(ex.getMessage());
 			}
 		}
 
 		return network;
 	}
 }
