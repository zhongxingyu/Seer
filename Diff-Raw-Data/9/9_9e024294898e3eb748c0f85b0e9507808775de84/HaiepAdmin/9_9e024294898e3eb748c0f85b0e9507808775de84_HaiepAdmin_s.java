 package cn.iie.haiep.hbase.admin;
 
 import java.io.IOException;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.apache.hadoop.hbase.HBaseConfiguration;
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.hbase.HTableDescriptor;
 import org.apache.hadoop.hbase.MasterNotRunningException;
 import org.apache.hadoop.hbase.ZooKeeperConnectionException;
 import org.apache.hadoop.hbase.client.HBaseAdmin;
 import org.apache.hadoop.hbase.client.HTable;
 import org.apache.hadoop.hbase.client.HTablePool;
 import org.apache.hadoop.hbase.client.Put;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import cn.iie.haiep.hbase.key.HKey;
 import cn.iie.haiep.hbase.store.HBaseTableInfo;
 import cn.iie.haiep.hbase.value.HColumn;
 import cn.iie.haiep.hbase.value.HValue;
 import cn.iie.haiep.rdbms.export.SQLExporter;
 import cn.iie.haiep.rdbms.metadata.Database;
import cn.iie.haiep.rdbms.metadata.RowSchema;
 import cn.iie.haiep.rdbms.metadata.Table;
 
 public class HaiepAdmin {
 
 	/**
 	 * TODO replace FAMILY with your desired string.
 	 */
 	public static final String FAMILY = "COMMONS";
 	
 	/**
 	 * TODO replace REGION with your desired string.
 	 */
 	public static final String REGION = "HAIEP";
 	
 	private HBaseAdmin admin;
 	
 	private HTablePool pool;
 
 	private HTable table;
 
 	private Configuration conf;
 
 	private boolean autoCreateSchema = true;
 
 	private HBaseTableInfo tableInfo;
 	
 	private String username = null;
 	
 	private String password = null;
 	
 	private String url = null;
 	
 	private String catalog = null;
 	
 	/**
 	 * @return the username
 	 */
 	public String getUsername() {
 		return username;
 	}
 	/**
 	 * @param username the username to set
 	 */
 	public void setUsername(String username) {
 		this.username = username;
 	}
 	/**
 	 * @return the password
 	 */
 	public String getPassword() {
 		return password;
 	}
 	/**
 	 * @param password the password to set
 	 */
 	public void setPassword(String password) {
 		this.password = password;
 	}
 	/**
 	 * @return the url
 	 */
 	public String getUrl() {
 		return url;
 	}
 	/**
 	 * @param url the url to set
 	 */
 	public void setUrl(String url) {
 		this.url = url;
 	}
 	/**
 	 * @return the catalog
 	 */
 	public String getCatalog() {
 		return catalog;
 	}
 	/**
 	 * @param catalog the catalog to set
 	 */
 	public void setCatalog(String catalog) {
 		this.catalog = catalog;
 	}
 	
 	
 	public HaiepAdmin(String username, String password, String url,
 			String catalog) {
 		super();
 		this.username = username;
 		this.password = password;
 		this.url = url;
 		this.catalog = catalog;
 	}
 	
 	public HaiepAdmin() {
 	}
 	
 	public void initialize() {
 		this.conf = HBaseConfiguration.create();
 		try {
 			this.admin = new HBaseAdmin(conf);
 			tableInfo = new HBaseTableInfo();
 			initTableInfo();
 		} catch (MasterNotRunningException e) {
 			logger.error(e.getMessage());
 			e.printStackTrace();
 		} catch (ZooKeeperConnectionException e) {
 			logger.error(e.getMessage());
 			e.printStackTrace();
 		}
 		if (autoCreateSchema) {
 			try {
 				createSchemas();
 			} catch (IOException e) {
 				logger.error(e.getMessage());
 				e.printStackTrace();
 			}
 		}
 	}
 	
 	
 	/**
 	 * Add table name.
 	 * @param tableName table name to be set.
 	 */
 	public void addTableName(String tableName) {
 		if (tableInfo == null) {
 			logger.warn("MetaTable object has to be constructed at first.");
 			return;
 		}
 		tableInfo.addTableName(tableName);
 	}
 	
 	/**
 	 * add table.
 	 * @param tableName
 	 */
 	public void addTable(String tableName) {
 		tableInfo.addTable(tableName);
 	}
 	
 	/**
 	 * add family name.
 	 * @param familyName family name.
 	 */
 	public void addColumnFamily(String tableName, String familyName) {
 		if (familyName != null) {
 			tableInfo.addColumnFamily(tableInfo.getTableName(tableName), 
 					familyName, null, null, null, null, null, null, null);
 		} else {
 			tableInfo.addColumnFamily(tableInfo.getTableName(tableName), FAMILY,
 					null, null, null, null, null, null, null);
 		}
 	}
 	
 	public void createSchemas() throws IOException {
 		if (tableInfo != null) {
 			while (tableInfo.hasNextTableName()) {
 				String tableName = tableInfo.nextTableName();
 				createSchema(tableName);
 			}
 		}
 	}
 
 	
 	public void createSchema(String tableName) throws IOException {
 		if (admin.tableExists(tableInfo.getTableName(tableName))) {
 			return;
 		}
 		HTableDescriptor tableDesc = tableInfo.getTable(tableName);
 
 		admin.createTable(tableDesc);
 	}
 
 	public void deleteSchema(String tableName) throws IOException {
 		if (!admin.tableExists(tableInfo.getTableName(tableName))) {
 			if (table != null) {
 				table.getWriteBuffer().clear();
 			}
 			return;
 		}
 		admin.disableTable(tableInfo.getTableName(tableName));
 		admin.deleteTable(tableInfo.getTableName(tableName));
 	}
 
 	public boolean schemaExists(String tableName) throws IOException {
 		return admin.tableExists(tableInfo.getTableName(tableName));
 	}
 	
 	public void put(String tableName, HKey key, HValue value) {
		HTablePool pool = new HTablePool(conf, 1024);
 		HTable table = (HTable) pool.getTable(tableName);
 		
 		Put put = new Put(key.getRow());
 		while (value.hasNext()) {
 			HColumn column = value.next();
 			byte[] familyBytes = column.getFamily();
 			byte[] qualifierBytes = column.getQualifier();
 			byte[] valueBytes = column.getValue();
 			put.add(familyBytes, qualifierBytes, valueBytes);
 		}
 		try {
 			table.put(put);
 		} catch (IOException e) {
 			logger.error(e.getMessage());
 			e.printStackTrace();
 		}
 	}
 	
 	public void migrateData() {
		HTablePool pool = new HTablePool(conf, 1024);
 		SQLExporter sqlExporter = 
 			new SQLExporter(url, username, password, catalog);
 		while(sqlExporter.hasNextDataTable()) {
 			Entry entry = sqlExporter.next();
 			String tableName = REGION + "." + (String) entry.getKey();
 			List<Map<String, Object>> list = (List<Map<String, Object>>) entry.getValue();
 			/**
 			 * table to migrate data.
 			 */
 			HTable table = (HTable) pool.getTable(tableName);
 			
 			
 			for (int i = 0; i < list.size(); i++) {
 				
 				Put put = new Put((new Integer(i)).toString().getBytes());
 				
 				Map<String, Object> map = list.get(i);
 				for (Map.Entry<String, Object> m : map.entrySet()) {
 					
 					put.add(FAMILY.getBytes(), m.getKey().getBytes(), m.getValue().toString().getBytes());
 					
 //					System.out.println("Key: " + m.getKey());
 //					System.out.println("Value: " + m.getValue());
 					
 				}
 				try {
 					table.put(put);
 				} catch (IOException e) {
 					logger.error(e.getMessage());
 					e.printStackTrace();
 				}
 			}
 			
 			
 		}
 		
 		
 	}
 
 	public void flush() throws IOException {
 		table.flushCommits();
 	}
 
 	public void close() throws IOException {
 		flush();
 		if (table != null)
 			table.close();
 	}
 
 	public Configuration getConf() {
 		return conf;
 	}
 
 	public void setConf(Configuration conf) {
 		this.conf = conf;
 	}
 
 	public static final Logger logger = LoggerFactory.getLogger(HaiepAdmin.class);
 	
 	/**
 	 * Initializing tableInfo
 	 */
 	private void initTableInfo() {
 		Database db = new Database(url, username, password, catalog);
 		db.fillTableLists();
 		while (db.hasNextTable()) {
 			Table tbl = db.next();
 			
 			/**
 			 * Get table name.
 			 */
 			String tableName = REGION + "." + tbl.getTableName();
 			addTableName(tableName);
 			addTable(tableName);
 			addColumnFamily(tableName, null);
 		}
 	}
 }
