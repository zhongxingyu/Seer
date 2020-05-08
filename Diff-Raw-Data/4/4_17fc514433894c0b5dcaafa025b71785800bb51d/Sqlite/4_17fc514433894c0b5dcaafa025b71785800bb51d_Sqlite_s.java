 package eu.play_project.dcep.distributedetalis.persistence;
 
 import java.io.File;
 import java.io.Serializable;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.HashSet;
 import java.util.Set;
 
 public class Sqlite implements Persistence {
 
 	private Connection sqliteConn;
 	final String sqliteTable = "subscriptionsEc";
 
 	
 	
 	public Sqlite() throws PersistenceException {
 		this(
				new File(System.getProperty("java.io.tmpdir") + File.pathSeparator + "play-dcep"
						+ File.pathSeparator + "dcep.db"));
 		// TODO stuehmer: this should be unique for more than one Detalis instance per machine
 	}
 	
 	public Sqlite(File dbFile) throws PersistenceException {
 
 		try {
 			Class.forName(org.sqlite.JDBC.class.getName());
 			sqliteConn = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getPath());
 			sqliteConn.setAutoCommit(true);
 	        Statement stat = sqliteConn.createStatement();
 	        stat.executeUpdate(String.format("CREATE TABLE IF NOT EXISTS %s (cloudId, subscriptionId);", sqliteTable)); // if not exists
 	        
 			
 		} catch (ClassNotFoundException e) {
 			throw new PersistenceException("Error retrieving old subscriptions from database.", e);
 		} catch (SQLException e) {
 			throw new PersistenceException("Error retrieving old subscriptions from database.", e);
 		}
 	}
 	
 	/* (non-Javadoc)
 	 * @see eu.play_project.dcep.distributedetalis.persistence.Persistence#storeSubscription(java.lang.String, java.lang.String)
 	 */
 	@Override
 	public void storeSubscription(String cloudId, String subscriptionId) {
 		try {
 			sqliteConn.createStatement().execute(
 							String.format("INSERT INTO %s VALUES ('%s', '%s');", sqliteTable,
 									cloudId, subscriptionId));
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see eu.play_project.dcep.distributedetalis.persistence.Persistence#deleteAllSubscriptions()
 	 */
 	@Override
 	public void deleteAllSubscriptions() {
 		try {
 			sqliteConn.createStatement().execute(String.format("DELETE FROM %s;", sqliteTable));
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see eu.play_project.dcep.distributedetalis.persistence.Persistence#getSubscriptions()
 	 */
 	@Override
 	public Set<SubscriptionPerCloud> getSubscriptions() throws PersistenceException {
 
 		Set<SubscriptionPerCloud> persistedSubscriptions = new HashSet<SubscriptionPerCloud>();
 		
 		try {
 			ResultSet rs = sqliteConn.createStatement().executeQuery(
 					String.format("SELECT cloudId, subscriptionId FROM %s;", sqliteTable));
 			while (rs.next()) {
 				final String cloudId = rs.getString("cloudId");
 				final String subscriptionId = rs.getString("subscriptionId");
 				
 				persistedSubscriptions.add(new SubscriptionPerCloud(cloudId, subscriptionId));
 			}
 			rs.close();
 
 		} catch (SQLException e) {
 			throw new PersistenceException(
 					"Error retrieving old subscriptions from database.", e);
 		}
 		
 		return persistedSubscriptions;
 	}
 	
 	public class SubscriptionPerCloud implements Serializable {
 
 		private static final long serialVersionUID = 100L;
 
 		public SubscriptionPerCloud(String cloudId, String subscriptionId) {
 			this.subscriptionId = subscriptionId;
 			this.cloudId = cloudId;
 		}
 
 		public String subscriptionId;
 		public String cloudId;
 	}
 
 }
