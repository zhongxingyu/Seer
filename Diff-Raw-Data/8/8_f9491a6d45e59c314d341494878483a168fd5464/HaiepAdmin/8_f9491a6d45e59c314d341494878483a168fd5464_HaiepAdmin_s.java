 package cn.iie.haiep.hbase.admin;
 
 import java.io.IOException;
 
 import org.apache.hadoop.hbase.HBaseConfiguration;
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.hbase.HTableDescriptor;
 import org.apache.hadoop.hbase.MasterNotRunningException;
 import org.apache.hadoop.hbase.ZooKeeperConnectionException;
 import org.apache.hadoop.hbase.client.HBaseAdmin;
 import org.apache.hadoop.hbase.client.HTable;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import cn.iie.haiep.hbase.store.HBaseTableInfo;
 
 public class HaiepAdmin {
 
 	private HBaseAdmin admin;
 
 	private HTable table;
 
 	private Configuration conf;
 
 	private boolean autoCreateSchema = true;
 
 	private HBaseTableInfo tableInfo;
 
 	public HaiepAdmin() {
 	}
 	
 	public void initialize() {
 		this.conf = HBaseConfiguration.create();
 		try {
 			this.admin = new HBaseAdmin(conf);
 			//TODO initialize tableInfo.
 		} catch (MasterNotRunningException e) {
 			logger.error(e.getMessage());
 			e.printStackTrace();
 		} catch (ZooKeeperConnectionException e) {
 			logger.error(e.getMessage());
 			e.printStackTrace();
 		}
 		if (autoCreateSchema) {
 			try {
 				createSchema();
 			} catch (IOException e) {
 				logger.error(e.getMessage());
 				e.printStackTrace();
 			}
 		}
 	}
 
 	
 	public void createSchema() throws IOException {
 		if (admin.tableExists(tableInfo.getTableName())) {
 			return;
 		}
 		HTableDescriptor tableDesc = tableInfo.getTable();
 
 		admin.createTable(tableDesc);
 	}
 
 	public void deleteSchema() throws IOException {
 		if (!admin.tableExists(tableInfo.getTableName())) {
 			if (table != null) {
 				table.getWriteBuffer().clear();
 			}
 			return;
 		}
 		admin.disableTable(tableInfo.getTableName());
 		admin.deleteTable(tableInfo.getTableName());
 	}
 
 	public boolean schemaExists() throws IOException {
 		return admin.tableExists(tableInfo.getTableName());
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
 }
