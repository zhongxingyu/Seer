 /**
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.apache.hadoop.hive.metastore;
 
 import static org.apache.commons.lang.StringUtils.join;
 
 import java.math.BigDecimal;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Properties;
 import java.util.Set;
 import java.util.concurrent.locks.Lock;
 import java.util.concurrent.locks.ReentrantLock;
 
 import javax.jdo.JDOHelper;
 import javax.jdo.JDOObjectNotFoundException;
 import javax.jdo.PersistenceManager;
 import javax.jdo.PersistenceManagerFactory;
 import javax.jdo.Query;
 import javax.jdo.Transaction;
 import javax.jdo.datastore.DataStoreCache;
 
 import org.antlr.runtime.CharStream;
 import org.antlr.runtime.CommonTokenStream;
 import org.antlr.runtime.RecognitionException;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.hadoop.conf.Configurable;
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.hive.common.classification.InterfaceAudience;
 import org.apache.hadoop.hive.common.classification.InterfaceStability;
 import org.apache.hadoop.hive.conf.HiveConf;
 import org.apache.hadoop.hive.conf.HiveConf.ConfVars;
 import org.apache.hadoop.hive.metastore.DiskManager.DeviceInfo;
 import org.apache.hadoop.hive.metastore.api.AlreadyExistsException;
 import org.apache.hadoop.hive.metastore.api.BinaryColumnStatsData;
 import org.apache.hadoop.hive.metastore.api.BooleanColumnStatsData;
 import org.apache.hadoop.hive.metastore.api.BusiTypeColumn;
 import org.apache.hadoop.hive.metastore.api.BusiTypeDatacenter;
 import org.apache.hadoop.hive.metastore.api.Busitype;
 import org.apache.hadoop.hive.metastore.api.ColumnStatistics;
 import org.apache.hadoop.hive.metastore.api.ColumnStatisticsData;
 import org.apache.hadoop.hive.metastore.api.ColumnStatisticsDesc;
 import org.apache.hadoop.hive.metastore.api.ColumnStatisticsObj;
 import org.apache.hadoop.hive.metastore.api.Constants;
 import org.apache.hadoop.hive.metastore.api.Database;
 import org.apache.hadoop.hive.metastore.api.Device;
 import org.apache.hadoop.hive.metastore.api.DoubleColumnStatsData;
 import org.apache.hadoop.hive.metastore.api.EquipRoom;
 import org.apache.hadoop.hive.metastore.api.FieldSchema;
 import org.apache.hadoop.hive.metastore.api.GeoLocation;
 import org.apache.hadoop.hive.metastore.api.GlobalSchema;
 import org.apache.hadoop.hive.metastore.api.HiveObjectPrivilege;
 import org.apache.hadoop.hive.metastore.api.HiveObjectRef;
 import org.apache.hadoop.hive.metastore.api.HiveObjectType;
 import org.apache.hadoop.hive.metastore.api.Index;
 import org.apache.hadoop.hive.metastore.api.InvalidInputException;
 import org.apache.hadoop.hive.metastore.api.InvalidObjectException;
 import org.apache.hadoop.hive.metastore.api.InvalidPartitionException;
 import org.apache.hadoop.hive.metastore.api.LongColumnStatsData;
 import org.apache.hadoop.hive.metastore.api.MetaException;
 import org.apache.hadoop.hive.metastore.api.NoSuchObjectException;
 import org.apache.hadoop.hive.metastore.api.Node;
 import org.apache.hadoop.hive.metastore.api.NodeGroup;
 import org.apache.hadoop.hive.metastore.api.Order;
 import org.apache.hadoop.hive.metastore.api.Partition;
 import org.apache.hadoop.hive.metastore.api.PartitionEventType;
 import org.apache.hadoop.hive.metastore.api.PrincipalPrivilegeSet;
 import org.apache.hadoop.hive.metastore.api.PrincipalType;
 import org.apache.hadoop.hive.metastore.api.PrivilegeBag;
 import org.apache.hadoop.hive.metastore.api.PrivilegeGrantInfo;
 import org.apache.hadoop.hive.metastore.api.Role;
 import org.apache.hadoop.hive.metastore.api.SFile;
 import org.apache.hadoop.hive.metastore.api.SFileLocation;
 import org.apache.hadoop.hive.metastore.api.SFileRef;
 import org.apache.hadoop.hive.metastore.api.SerDeInfo;
 import org.apache.hadoop.hive.metastore.api.SkewedInfo;
 import org.apache.hadoop.hive.metastore.api.SplitValue;
 import org.apache.hadoop.hive.metastore.api.StorageDescriptor;
 import org.apache.hadoop.hive.metastore.api.StringColumnStatsData;
 import org.apache.hadoop.hive.metastore.api.Subpartition;
 import org.apache.hadoop.hive.metastore.api.Table;
 import org.apache.hadoop.hive.metastore.api.Type;
 import org.apache.hadoop.hive.metastore.api.UnknownDBException;
 import org.apache.hadoop.hive.metastore.api.UnknownPartitionException;
 import org.apache.hadoop.hive.metastore.api.UnknownTableException;
 import org.apache.hadoop.hive.metastore.api.User;
 import org.apache.hadoop.hive.metastore.api.statfs;
 import org.apache.hadoop.hive.metastore.model.MBusiTypeColumn;
 import org.apache.hadoop.hive.metastore.model.MBusiTypeDatacenter;
 import org.apache.hadoop.hive.metastore.model.MBusitype;
 import org.apache.hadoop.hive.metastore.model.MColumnDescriptor;
 import org.apache.hadoop.hive.metastore.model.MDBPrivilege;
 import org.apache.hadoop.hive.metastore.model.MDatabase;
 import org.apache.hadoop.hive.metastore.model.MDevice;
 import org.apache.hadoop.hive.metastore.model.MDirectDDL;
 import org.apache.hadoop.hive.metastore.model.MEquipRoom;
 import org.apache.hadoop.hive.metastore.model.MFieldSchema;
 import org.apache.hadoop.hive.metastore.model.MFile;
 import org.apache.hadoop.hive.metastore.model.MFileLocation;
 import org.apache.hadoop.hive.metastore.model.MGeoLocation;
 import org.apache.hadoop.hive.metastore.model.MGlobalPrivilege;
 import org.apache.hadoop.hive.metastore.model.MIndex;
 import org.apache.hadoop.hive.metastore.model.MNode;
 import org.apache.hadoop.hive.metastore.model.MNodeGroup;
 import org.apache.hadoop.hive.metastore.model.MOrder;
 import org.apache.hadoop.hive.metastore.model.MPartition;
 import org.apache.hadoop.hive.metastore.model.MPartitionColumnPrivilege;
 import org.apache.hadoop.hive.metastore.model.MPartitionColumnStatistics;
 import org.apache.hadoop.hive.metastore.model.MPartitionEvent;
 import org.apache.hadoop.hive.metastore.model.MPartitionIndex;
 import org.apache.hadoop.hive.metastore.model.MPartitionIndexStore;
 import org.apache.hadoop.hive.metastore.model.MPartitionPrivilege;
 import org.apache.hadoop.hive.metastore.model.MRole;
 import org.apache.hadoop.hive.metastore.model.MRoleMap;
 import org.apache.hadoop.hive.metastore.model.MSchema;
 import org.apache.hadoop.hive.metastore.model.MSchemaPrivilege;
 import org.apache.hadoop.hive.metastore.model.MSerDeInfo;
 import org.apache.hadoop.hive.metastore.model.MSplitValue;
 import org.apache.hadoop.hive.metastore.model.MStorageDescriptor;
 import org.apache.hadoop.hive.metastore.model.MStringList;
 import org.apache.hadoop.hive.metastore.model.MTable;
 import org.apache.hadoop.hive.metastore.model.MTableColumnPrivilege;
 import org.apache.hadoop.hive.metastore.model.MTableColumnStatistics;
 import org.apache.hadoop.hive.metastore.model.MTablePrivilege;
 import org.apache.hadoop.hive.metastore.model.MType;
 import org.apache.hadoop.hive.metastore.model.MUser;
 import org.apache.hadoop.hive.metastore.model.MetaStoreConst;
 import org.apache.hadoop.hive.metastore.msg.MSGFactory;
 import org.apache.hadoop.hive.metastore.msg.MSGFactory.DDLMsg;
 import org.apache.hadoop.hive.metastore.msg.MSGType;
 import org.apache.hadoop.hive.metastore.msg.MetaMsgServer;
 import org.apache.hadoop.hive.metastore.parser.ExpressionTree.ANTLRNoCaseStringStream;
 import org.apache.hadoop.hive.metastore.parser.FilterLexer;
 import org.apache.hadoop.hive.metastore.parser.FilterParser;
 import org.apache.hadoop.hive.metastore.tools.MetaUtil;
 import org.apache.hadoop.hive.metastore.tools.PartitionFactory.PartitionInfo;
 import org.apache.hadoop.util.StringUtils;
 import org.apache.thrift.TException;
 import org.datanucleus.FetchPlan;
 
 import com.taobao.metamorphosis.exception.MetaClientException;
 /**
  * This class is the interface between the application logic and the database
  * store that contains the objects. Refrain putting any logic in mode.M* objects
  * or in this file as former could be auto generated and this class would need
  * to be made into a interface that can read both from a database and a
  * filestore.
  */
 public class ObjectStore implements RawStore, Configurable {
   private static final Long g_fid_syncer = new Long(0);
   private static long g_fid = 0;
   private static boolean g_fid_inited = false;
   private static Properties prop = null;
   private static PersistenceManagerFactory pmf = null;
 
   private static Lock pmfPropLock = new ReentrantLock();
   private static final Log LOG = LogFactory.getLog(ObjectStore.class.getName());
 
   private static enum TXN_STATUS {
     NO_STATE, OPEN, COMMITED, ROLLBACK
   }
 
   private void restoreFID() {
     boolean commited = false;
 
     try {
       openTransaction();
       Query query = pm.newQuery("javax.jdo.query.SQL", "SELECT max(fid) FROM FILES");
       List results = (List) query.execute();
       BigDecimal maxfid = (BigDecimal) results.iterator().next();
       if (maxfid != null) {
         g_fid = maxfid.longValue() + 10;
       }
       commited = commitTransaction();
       LOG.info("restore FID to " + g_fid);
     } catch (javax.jdo.JDODataStoreException e) {
       LOG.info(e, e);
     }catch (Exception e) {
       LOG.info(e, e);
     } finally {
       if (!commited) {
         rollbackTransaction();
       }
     }
   }
 
   public long getNextFID() {
     synchronized (g_fid_syncer) {
       return g_fid++;
     }
   }
 
   @Override
   public long getCurrentFID() {
     return g_fid;
   }
 
   private static final Map<String, Class> PINCLASSMAP;
   static {
     Map<String, Class> map = new HashMap();
     map.put("schema", MSchema.class);
     map.put("table", MTable.class);
     map.put("storagedescriptor", MStorageDescriptor.class);
     map.put("serdeinfo", MSerDeInfo.class);
     map.put("partition", MPartition.class);
     map.put("database", MDatabase.class);
     map.put("type", MType.class);
     map.put("fieldschema", MFieldSchema.class);
     map.put("order", MOrder.class);
     map.put("files", MFile.class);
     map.put("nodes", MNode.class);
     map.put("device", MDevice.class);
     map.put("direct_ddl", MDirectDDL.class);
     map.put("busi_column", MBusiTypeColumn.class);
     map.put("index", MIndex.class);
     map.put("partindex", MPartitionIndex.class);
     map.put("partindexstore", MPartitionIndexStore.class);
     map.put("filelocation", MFileLocation.class);
     PINCLASSMAP = Collections.unmodifiableMap(map);
   }
 
   private boolean isInitialized = false;
   private PersistenceManager pm = null;
   private Configuration hiveConf;
   int openTrasactionCalls = 0;
   private Transaction currentTransaction = null;
   private TXN_STATUS transactionStatus = TXN_STATUS.NO_STATE;
 
   public ObjectStore() {
   }
 
   public Configuration getConf() {
     return hiveConf;
   }
 
   /**
    * Called whenever this object is instantiated using ReflectionUils, and also
    * on connection retries. In cases of connection retries, conf will usually
    * contain modified values.
    */
   @SuppressWarnings("nls")
   public void setConf(Configuration conf) {
     // Although an instance of ObjectStore is accessed by one thread, there may
     // be many threads with ObjectStore instances. So the static variables
     // pmf and prop need to be protected with locks.
     pmfPropLock.lock();
     try {
       isInitialized = false;
       hiveConf = conf;
       Properties propsFromConf = getDataSourceProps(conf);
       boolean propsChanged = !propsFromConf.equals(prop);
 
       if (propsChanged) {
         pmf = null;
         prop = null;
       }
 
       assert(!isActiveTransaction());
       shutdown();
       // Always want to re-create pm as we don't know if it were created by the
       // most recent instance of the pmf
       pm = null;
       openTrasactionCalls = 0;
       currentTransaction = null;
       transactionStatus = TXN_STATUS.NO_STATE;
 
       initialize(propsFromConf);
 
       if (!isInitialized) {
         throw new RuntimeException(
         "Unable to create persistence manager. Check dss.log for details");
       } else {
         LOG.info("Initialized ObjectStore");
       }
     } finally {
       pmfPropLock.unlock();
     }
   }
 
   private ClassLoader classLoader;
   {
     classLoader = Thread.currentThread().getContextClassLoader();
     if (classLoader == null) {
       classLoader = ObjectStore.class.getClassLoader();
     }
   }
 
   @SuppressWarnings("nls")
   private void initialize(Properties dsProps) {
     LOG.info("ObjectStore, initialize called");
     prop = dsProps;
     pm = getPersistenceManager();
     isInitialized = pm != null;
     if (isInitialized) {
       if (!g_fid_inited) {
         g_fid_inited = true;
         restoreFID();
       }
     }
 
     //add by zjw for messge queue
     String zkAddr = prop.getProperty(Constants.META_JDO_ZOOKER_ADDR);
     MetaMsgServer.setZkAddr(zkAddr);
     try {
       MetaMsgServer.start();
     } catch (MetaClientException e) {
       LOG.error(e+"---start-metaQ--error",e);
     } catch (Exception e) {
       LOG.error(e, e);
     }
 
     return;
   }
 
   /**
    * Properties specified in hive-default.xml override the properties specified
    * in jpox.properties.
    */
   @SuppressWarnings("nls")
   private static Properties getDataSourceProps(Configuration conf) {
     Properties prop = new Properties();
 
     Iterator<Map.Entry<String, String>> iter = conf.iterator();
     while (iter.hasNext()) {
       Map.Entry<String, String> e = iter.next();
       if (e.getKey().contains("datanucleus") || e.getKey().contains("jdo")) {
         Object prevVal = prop.setProperty(e.getKey(), conf.get(e.getKey()));
         if (LOG.isDebugEnabled()
             && !e.getKey().equals(HiveConf.ConfVars.METASTOREPWD.varname)) {
           LOG.debug("Overriding " + e.getKey() + " value " + prevVal
               + " from  jpox.properties with " + e.getValue());
         }
       }
     }
 
     if (LOG.isDebugEnabled()) {
       for (Entry<Object, Object> e : prop.entrySet()) {
         if (!e.getKey().equals(HiveConf.ConfVars.METASTOREPWD.varname)) {
           LOG.debug(e.getKey() + " = " + e.getValue());
         }
       }
     }
     return prop;
   }
 
   private static PersistenceManagerFactory getPMF() {
     if (pmf == null) {
       pmf = JDOHelper.getPersistenceManagerFactory(prop);
       DataStoreCache dsc = pmf.getDataStoreCache();
       if (dsc != null) {
         HiveConf conf = new HiveConf(ObjectStore.class);
         String objTypes = HiveConf.getVar(conf, HiveConf.ConfVars.METASTORE_CACHE_PINOBJTYPES);
         LOG.info("Setting MetaStore object pin classes with hive.metastore.cache.pinobjtypes=\"" + objTypes + "\"");
         if (objTypes != null && objTypes.length() > 0) {
           objTypes = objTypes.toLowerCase();
           String[] typeTokens = objTypes.split(",");
           for (String type : typeTokens) {
             type = type.trim();
             if (PINCLASSMAP.containsKey(type)) {
               dsc.pinAll(true, PINCLASSMAP.get(type));
             }
             else {
               LOG.warn(type + " is not one of the pinnable object types: " + org.apache.commons.lang.StringUtils.join(PINCLASSMAP.keySet(), " "));
             }
           }
         }
       } else {
         LOG.warn("PersistenceManagerFactory returned null DataStoreCache object. Unable to initialize object pin types defined by hive.metastore.cache.pinobjtypes");
       }
     }
     return pmf;
   }
 
   @InterfaceAudience.LimitedPrivate({"HCATALOG"})
   @InterfaceStability.Evolving
   public PersistenceManager getPersistenceManager() {
     return getPMF().getPersistenceManager();
   }
 
   public void shutdown() {
     if (pm != null) {
       pm.close();
     }
   }
 
   /**
    * Opens a new one or the one already created Every call of this function must
    * have corresponding commit or rollback function call
    *
    * @return an active transaction
    */
 
   public boolean openTransaction() {
     openTrasactionCalls++;
     //LOG.debug("---" + Thread.currentThread().getId() + "--openTransaction:"+openTrasactionCalls);
     if (openTrasactionCalls == 1) {
       currentTransaction = pm.currentTransaction();
       currentTransaction.begin();
       transactionStatus = TXN_STATUS.OPEN;
     } else {
       // something is wrong since openTransactionCalls is greater than 1 but
       // currentTransaction is not active
       assert ((currentTransaction != null) && (currentTransaction.isActive()));
     }
     return currentTransaction.isActive();
   }
 
   /**
    * if this is the commit of the first open call then an actual commit is
    * called.
    *
    * @return Always returns true
    */
   @SuppressWarnings("nls")
   public boolean commitTransaction() {
     if (TXN_STATUS.ROLLBACK == transactionStatus) {
       return false;
     }
     /*String str = "THIS " + Thread.currentThread().getId() + " tx = " + openTrasactionCalls + " .\n";
     for (StackTraceElement ste : Thread.currentThread().getStackTrace()) {
       str += "\t" + ste.toString() + "\n";
     }
     LOG.error(str);*/
     if (openTrasactionCalls <= 0) {
       throw new RuntimeException("commitTransaction was called but openTransactionCalls = "
           + openTrasactionCalls + ". This probably indicates that there are unbalanced " +
               "calls to openTransaction/commitTransaction");
     }
     if (!currentTransaction.isActive()) {
       throw new RuntimeException(
           "Commit is called, but transaction is not active. Either there are"
               + " mismatching open and close calls or rollback was called in the same trasaction");
     }
     openTrasactionCalls--;
     //LOG.debug("---" + Thread.currentThread().getId() + "--commitTransaction:"+openTrasactionCalls);
     if ((openTrasactionCalls == 0) && currentTransaction.isActive()) {
       transactionStatus = TXN_STATUS.COMMITED;
       currentTransaction.commit();
     }
     return true;
   }
 
   /**
    * @return true if there is an active transaction. If the current transaction
    *         is either committed or rolled back it returns false
    */
   public boolean isActiveTransaction() {
     if (currentTransaction == null) {
       return false;
     }
     return currentTransaction.isActive();
   }
 
   /**
    * Rolls back the current transaction if it is active
    */
   public void rollbackTransaction() {
     //LOG.debug("---" + Thread.currentThread().getId() + "--rollbackTransaction:"+openTrasactionCalls);
     if (openTrasactionCalls < 1) {
       return;
     }
     openTrasactionCalls = 0;
     if (currentTransaction.isActive()
         && transactionStatus != TXN_STATUS.ROLLBACK) {
       transactionStatus = TXN_STATUS.ROLLBACK;
       // could already be rolled back
       currentTransaction.rollback();
     }
   }
 
   public void createDatabase(Database db) throws InvalidObjectException, MetaException {
     boolean commited = false;
     MDatabase mdb = new MDatabase();
     mdb.setName(db.getName().toLowerCase());
     mdb.setLocationUri(db.getLocationUri());
     mdb.setDescription(db.getDescription());
     mdb.setParameters(db.getParameters());
 
     try {
       openTransaction();
       pm.makePersistent(mdb);
       commited = commitTransaction();
     } finally {
       if (!commited) {
         rollbackTransaction();
       }
     }
     LOG.warn("---zjw---in createdatabase");
     long db_id = Long.parseLong(MSGFactory.getIDFromJdoObjectId(pm.getObjectId(mdb).toString()));
     DDLMsg msg = MSGFactory.generateDDLMsg(org.apache.hadoop.hive.metastore.msg.MSGType.MSG_NEW_DATABESE,db_id,-1,
         pm,mdb,null);
     if(commited) {
       MetaMsgServer.sendMsg(msg);
     }
   }
 
   @SuppressWarnings("nls")
   private MDatabase getMDatabase(String name) throws NoSuchObjectException {
     MDatabase mdb = null;
     boolean commited = false;
     try {
       openTransaction();
       String dbname = name.toLowerCase().trim();
       Query query = pm.newQuery(MDatabase.class, "name == dbname");
       query.declareParameters("java.lang.String dbname");
       query.setUnique(true);
       mdb = (MDatabase) query.execute(dbname);
       pm.retrieve(mdb);
       commited = commitTransaction();
     } finally {
       if (!commited) {
         rollbackTransaction();
       }
     }
     if (mdb == null) {
       throw new NoSuchObjectException("There is no database named " + name);
     }
     return mdb;
   }
   @SuppressWarnings("nls")
   private MSchema getMSchema(String schema_name) throws NoSuchObjectException {
     MSchema mSchema = null;
     boolean commited = false;
     try {
       openTransaction();
       schema_name = schema_name.toLowerCase().trim();
       Query query = pm.newQuery(MSchema.class, "schemaName == schema_name");
       query.declareParameters("java.lang.String schema_name");
       query.setUnique(true);
       mSchema = (MSchema) query.execute(schema_name);
       if(mSchema != null){
         pm.retrieve(mSchema);
         pm.retrieve(mSchema.getSd());
         pm.retrieve(mSchema.getSd().getCD());
         pm.retrieve(mSchema.getSd().getSerDeInfo());
       }
       commited = commitTransaction();
     } finally {
       if (!commited) {
         rollbackTransaction();
       }
     }
     if (mSchema == null) {
       throw new NoSuchObjectException("There is no schema named " + schema_name);
     }
     return mSchema;
   }
 
   public Database getDatabase(String name) throws NoSuchObjectException {
     MDatabase mdb = null;
     boolean commited = false;
     try {
       openTransaction();
       mdb = getMDatabase(name);
       commited = commitTransaction();
     } finally {
       if (!commited) {
         rollbackTransaction();
       }
     }
     Database db = new Database();
     db.setName(mdb.getName());
     db.setDescription(mdb.getDescription());
     db.setLocationUri(mdb.getLocationUri());
     db.setParameters(mdb.getParameters());
     return db;
   }
 
   /**
    * Alter the database object in metastore. Currently only the parameters
    * of the database can be changed.
    * @param dbName the database name
    * @param db the Hive Database object
    * @throws MetaException
    * @throws NoSuchObjectException
    */
   public boolean alterDatabase(String dbName, Database db)
     throws MetaException, NoSuchObjectException {
 
     MDatabase mdb = null;
     boolean committed = false;
     try {
       openTransaction();
       mdb = getMDatabase(dbName);
       // currently only allow changing database parameters
       mdb.setParameters(db.getParameters());
       long db_id = Long.parseLong(MSGFactory.getIDFromJdoObjectId(pm.getObjectId(mdb).toString()));
       pm.makePersistent(mdb);
       committed = commitTransaction();
 
       HashMap<String, Object> params = new HashMap<String, Object>();
       ArrayList<String>  ps = new ArrayList<String>();
       ps.addAll(mdb.getParameters().keySet());
       params.put("param_name",ps);
       if(committed) {
         MetaMsgServer.sendMsg(MSGFactory.generateDDLMsg(MSGType.MSG_ALTER_DATABESE_PARAM,db_id,-1, pm, mdb,params));
       }
     } finally {
       if (!committed) {
         rollbackTransaction();
         return false;
       }
     }
     return true;
   }
 
   public boolean dropDatabase(String dbname) throws NoSuchObjectException, MetaException {
     boolean success = false;
     LOG.info("Dropping database " + dbname + " along with all tables");
     dbname = dbname.toLowerCase();
     try {
       openTransaction();
 
       // then drop the database
       MDatabase db = getMDatabase(dbname);
       long db_id = Long.parseLong(MSGFactory.getIDFromJdoObjectId(pm.getObjectId(db).toString()));
       pm.retrieve(db);
       if (db != null) {
         List<MDBPrivilege> dbGrants = this.listDatabaseGrants(dbname);
         if (dbGrants != null && dbGrants.size() > 0) {
           pm.deletePersistentAll(dbGrants);
         }
         pm.deletePersistent(db);
       }
       String db_name = db.getName();
 
 
       success = commitTransaction();
       HashMap<String,Object> old_params= new HashMap<String,Object>();
 
       old_params.put("db_name", db_name);
       if(success) {
         MetaMsgServer.sendMsg(MSGFactory.generateDDLMsg(MSGType.MSG_DROP_DATABESE,db_id,-1, pm, db,old_params));
       }
     } finally {
       if (!success) {
         rollbackTransaction();
       }
     }
     return success;
   }
 
 
   public List<String> getDatabases(String pattern) throws MetaException {
     boolean commited = false;
     List<String> databases = null;
     try {
       openTransaction();
       // Take the pattern and split it on the | to get all the composing
       // patterns
       String[] subpatterns = pattern.trim().split("\\|");
       String query = "select name from org.apache.hadoop.hive.metastore.model.MDatabase where (";
       boolean first = true;
       for (String subpattern : subpatterns) {
         subpattern = "(?i)" + subpattern.replaceAll("\\*", ".*");
         if (!first) {
           query = query + " || ";
         }
         query = query + " name.matches(\"" + subpattern + "\")";
         first = false;
       }
       query = query + ")";
 
       Query q = pm.newQuery(query);
       q.setResult("name");
       q.setOrdering("name ascending");
       Collection names = (Collection) q.execute();
       databases = new ArrayList<String>();
       for (Iterator i = names.iterator(); i.hasNext();) {
         databases.add((String) i.next());
       }
       commited = commitTransaction();
     } finally {
       if (!commited) {
         rollbackTransaction();
       }
     }
     return databases;
   }
 
   public List<String> getAllDatabases() throws MetaException {
     return getDatabases(".*");
   }
 
   private MType getMType(Type type) {
     List<MFieldSchema> fields = new ArrayList<MFieldSchema>();
     if (type.getFields() != null) {
       for (FieldSchema field : type.getFields()) {
         fields.add(new MFieldSchema(field.getName(), field.getType(), field
             .getComment(), field.getVersion()));
       }
     }
     return new MType(type.getName(), type.getType1(), type.getType2(), fields);
   }
 
   private Type getType(MType mtype) {
     List<FieldSchema> fields = new ArrayList<FieldSchema>();
     if (mtype.getFields() != null) {
       for (MFieldSchema field : mtype.getFields()) {
         fields.add(new FieldSchema(field.getName(), field.getType(), field
             .getComment()));
       }
     }
     Type ret = new Type();
     ret.setName(mtype.getName());
     ret.setType1(mtype.getType1());
     ret.setType2(mtype.getType2());
     ret.setFields(fields);
     return ret;
   }
 
   public boolean createType(Type type) {
     boolean success = false;
     MType mtype = getMType(type);
     boolean commited = false;
     try {
       openTransaction();
       pm.makePersistent(mtype);
       commited = commitTransaction();
       success = true;
     } finally {
       if (!commited) {
         rollbackTransaction();
       }
     }
     return success;
   }
 
   public Type getType(String typeName) {
     Type type = null;
     boolean commited = false;
     try {
       openTransaction();
       Query query = pm.newQuery(MType.class, "name == typeName");
       query.declareParameters("java.lang.String typeName");
       query.setUnique(true);
       MType mtype = (MType) query.execute(typeName.trim());
       pm.retrieve(type);
       if (mtype != null) {
         type = getType(mtype);
       }
       commited = commitTransaction();
     } finally {
       if (!commited) {
         rollbackTransaction();
       }
     }
     return type;
   }
 
   public boolean dropType(String typeName) {
     boolean success = false;
     try {
       openTransaction();
       Query query = pm.newQuery(MType.class, "name == typeName");
       query.declareParameters("java.lang.String typeName");
       query.setUnique(true);
       MType type = (MType) query.execute(typeName.trim());
       pm.retrieve(type);
       if (type != null) {
         pm.deletePersistent(type);
       }
       success = commitTransaction();
     } catch (JDOObjectNotFoundException e) {
       success = commitTransaction();
       LOG.debug("type not found " + typeName, e);
     } finally {
       if (!success) {
         rollbackTransaction();
       }
     }
     return success;
   }
 
   @Override
   public List<SFile> filterTableFiles(String dbName, String tableName, List<SplitValue> values) throws MetaException {
     List<SFile> rls = new ArrayList<SFile>();
     String filter = "", parameters = "java.lang.String tableName, java.lang.String dbName";
     Map<String, Object> params = new HashMap<String, Object>();
 
     if (values == null || values.size() == 0) {
       return rls;
     }
     try {
       openTransaction();
       Query q = pm.newQuery(MFile.class, "this.table.tableName == tableName && this.table.database.name == dbName");
       for (int i = 0; i < values.size(); i++) {
         if (i != 0) {
           filter += "&&";
         }
         filter += "(this.values.contains(v" + i + ") && (v"
             + i + ".pkname == v" + i + "pkname && v"
             + i + ".level == v" + i + "level && v"
             + i + ".value == v" + i + "value && v"
             + i + ".version == v" + i + "version" + "))";
         params.put("v" + i + "pkname", values.get(i).getSplitKeyName());
         params.put("v" + i + "level", values.get(i).getLevel());
         params.put("v" + i + "value", values.get(i).getValue());
         params.put("v" + i + "version", values.get(i).getVerison());
         parameters += ", java.lang.String v" + i + "pkname";
         parameters += ", java.lang.Integer v" + i + "level";
         parameters += ", java.lang.String v" + i + "value";
         parameters += ", java.lang.Long v" + i + "version";
         //filter += "values.get(" + i + ").splitKeyName == this.values.get(" + i + ").splitKeyName";
       }
 
       params.put("tableName", tableName);
       params.put("dbName", dbName);
 
       LOG.debug("Got filter: " + filter);
       LOG.debug("Got parameter: " + parameters);
       LOG.debug("Got kvs: " + params.toString());
 
       q.setFilter(filter);
       q.declareParameters(parameters);
       Collection files = (Collection)q.executeWithMap(params);
       Iterator iter = files.iterator();
       while (iter.hasNext()) {
         MFile mf = (MFile)iter.next();
 
         if (mf == null) {
           continue;
         }
         List<MFileLocation> lmf = getMFileLocations(mf.getFid());
         List<SFileLocation> l = new ArrayList<SFileLocation>();
 
         if (lmf != null) {
           l = convertToSFileLocation(lmf);
         }
 
         SFile sf = convertToSFile(mf);
         sf.setLocations(l);
         rls.add(sf);
       }
     } finally {
       commitTransaction();
     }
 
     return rls;
   }
 
   @Override
   public List<Long> listTableFiles(String dbName, String tableName, int begin, int end) throws MetaException {
     List<Long> rls = new ArrayList<Long>();
 
     try {
       Query q0 = pm.newQuery(MFile.class, "this.table.tableName == tableName && this.table.database.name == dbName");
       q0.setResult("count(fid)");
       q0.declareParameters("java.lang.String tableName, java.lang.String dbName");
       Long fnr = (Long)q0.execute(tableName, dbName);
       LOG.info("Total hit " + fnr + " files in DB '" + dbName + "' TABLE '" + tableName + "'.");
 
       Query q = pm.newQuery(MFile.class, "this.table.tableName == tableName && this.table.database.name == dbName");
       if (begin < 0) {
         begin = 0;
       }
       if (end < 0) {
         end = 0;
       }
       if (end < begin) {
         end = begin;
       }
       // FIXME: do NOT ordering on large data set!
       if (fnr <= 1000) {
         q.setOrdering("fid ascending");
       }
       q.setRange(begin, end);
       q.declareParameters("java.lang.String tableName, java.lang.String dbName");
       Collection files = (Collection)q.execute(tableName, dbName);
       Iterator iter = files.iterator();
       while (iter.hasNext()) {
         MFile mf = (MFile)iter.next();
 
         if (mf == null) {
           continue;
         }
         rls.add(mf.getFid());
       }
     } finally {
     }
 
     return rls;
   }
 
   @Override
   public void truncTableFiles(String dbName, String tableName) throws MetaException,
     NoSuchObjectException {
     boolean success = false;
     try {
       openTransaction();
 
       for (int i = 0, step = 1000; i < Integer.MAX_VALUE; i+=step) {
         List<Long> files = listTableFiles(dbName, tableName, i, i + step);
         for (int j = 0; j < files.size(); j++) {
           SFile f = this.getSFile(files.get(j));
           if (f != null) {
             f.setStore_status(MetaStoreConst.MFileStoreStatus.RM_PHYSICAL);
             updateSFile(f);
           }
         }
         if (files.size() < step) {
           break;
         }
       }
 
       success = commitTransaction();
     } finally {
       if (!success) {
         rollbackTransaction();
       }
     }
 
   }
 
   public List<Long> findSpecificDigestFiles(String digest) throws MetaException {
     boolean commited = false;
     List<Long> r = new ArrayList<Long>();
 
     try {
       openTransaction();
       Query q = pm.newQuery(MFile.class, "this.digest == digest");
       q.declareParameters("java.lang.String digest");
       Collection files = (Collection)q.execute(digest);
       Iterator iter = files.iterator();
       while (iter.hasNext()) {
         MFile f = (MFile)iter.next();
         if (f == null) {
           continue;
         }
         r.add(f.getFid());
       }
       commited = commitTransaction();
     } finally {
       if (!commited) {
         rollbackTransaction();
       }
     }
     return r;
   }
   public void findVoidFiles(List<SFile> voidFiles) throws MetaException {
     boolean commited = false;
 
     try {
       openTransaction();
       Query q = pm.newQuery(MFile.class, "this.store_status == status && this.create_time <= t1");
       q.declareParameters("int status, long t1");
       Collection files = (Collection)q.execute(MetaStoreConst.MFileStoreStatus.INCREATE, System.currentTimeMillis() - (600 * 1000));
       Iterator iter = files.iterator();
       while (iter.hasNext()) {
         MFile m = (MFile)iter.next();
         if (m == null) {
           continue;
         }
         List<SFileLocation> l = getSFileLocations(m.getFid());
 
         // check if it contains valid locations
         boolean ok = false;
         for (SFileLocation fl : l) {
           if (fl.getVisit_status() == MetaStoreConst.MFileLocationVisitStatus.ONLINE) {
             ok = true;
             break;
           }
         }
         if (!ok) {
           try {
             SFile s = convertToSFile(m);
             s.setLocations(l);
             voidFiles.add(s);
           } catch (javax.jdo.JDOObjectNotFoundException e) {
               // it means the file slips ...
               LOG.error(e, e);
           }
         }
       }
 
       commited = commitTransaction();
     } finally {
       if (!commited) {
         rollbackTransaction();
       }
     }
   }
 
   public void findVoidFilesByPartition(List<SFile> voidFiles) throws MetaException {
     boolean commited = false;
     long beginTs = System.currentTimeMillis();
 
     //FIXME: try to use non-transaction
     try {
       openTransaction();
       SFileLocation fl;
 
       // iterate all partitions to find valid files.
       Query q = pm.newQuery(MPartition.class);
       Collection allParts = (Collection)q.execute();
       Set<Long> allValidFids = new HashSet<Long>();
       Iterator iter = allParts.iterator();
       while (iter.hasNext()) {
         MPartition mp = (MPartition)iter.next();
         if (mp == null) {
           continue;
         }
         if (mp.getFiles() != null && mp.getFiles().size() > 0) {
           for (Long id : mp.getFiles()) {
             allValidFids.add(id);
           }
         }
       }
       // iterate all files to find void files
       q = pm.newQuery(MFile.class);
       Collection allFiles = (Collection)q.execute();
       iter = allFiles.iterator();
       while (iter.hasNext()) {
         MFile mf = (MFile)iter.next();
         if (mf == null) {
           continue;
         }
         if (allValidFids.contains(mf.getFid())) {
           // double check the update timestamp
           List<MFileLocation> lmf = getMFileLocations(mf.getFid());
 
           if (lmf != null && lmf.size() > 0) {
             boolean beforeBeginTs = true;
             for (MFileLocation m : lmf) {
               if (m.getUpdate_time() >= beginTs) {
                 // this is new file
                 beforeBeginTs = false;
                 break;
               }
             }
             if (beforeBeginTs) {
               // ok, add this file to result list
               List<SFileLocation> l = convertToSFileLocation(lmf);
               SFile f = convertToSFile(mf);
               f.setLocations(l);
               voidFiles.add(f);
             }
           }
         }
       }
       commited = commitTransaction();
     } finally {
       if (!commited) {
         rollbackTransaction();
       }
     }
   }
 
   public void findFiles(List<SFile> underReplicated, List<SFile> overReplicated, List<SFile> lingering,
       long from, long to) throws MetaException {
     long node_nr = countNode();
     boolean commited = false;
 
     if (underReplicated == null || overReplicated == null || lingering == null) {
       throw new MetaException("Invalid input List<SFile> collection. IS NULL");
     }
 
     try {
       openTransaction();
       Query q = pm.newQuery(MFile.class, "this.store_status != increate");
       q.declareParameters("int increate");
       q.setRange(from, to);
       Collection allFiles = (Collection)q.execute(MetaStoreConst.MFileStoreStatus.INCREATE);
       Iterator iter = allFiles.iterator();
       while (iter.hasNext()) {
         MFile m = (MFile)iter.next();
         if (m == null) {
           continue;
         }
         List<SFileLocation> l = getSFileLocations(m.getFid());
 
         // find under replicated files
         if (m.getStore_status() == MetaStoreConst.MFileStoreStatus.CLOSED ||
             m.getStore_status() == MetaStoreConst.MFileStoreStatus.REPLICATED) {
           int nr = 0;
 
           for (SFileLocation fl : l) {
             if (fl.getVisit_status() == MetaStoreConst.MFileLocationVisitStatus.ONLINE) {
               nr++;
             }
           }
           if (m.getRep_nr() > nr) {
             try {
               SFile s = convertToSFile(m);
               s.setLocations(l);
               underReplicated.add(s);
             } catch (javax.jdo.JDOObjectNotFoundException e) {
               // it means the file slips ...
               LOG.error(e, e);
             }
           }
         }
         // find over  replicated files
         if (m.getStore_status() != MetaStoreConst.MFileStoreStatus.INCREATE) {
           int nr = 0;
 
           for (SFileLocation fl : l) {
             if (fl.getVisit_status() == MetaStoreConst.MFileLocationVisitStatus.ONLINE) {
               nr++;
             }
           }
           if (m.getRep_nr() < nr) {
             try {
               SFile s = convertToSFile(m);
               s.setLocations(l);
               overReplicated.add(s);
             } catch (javax.jdo.JDOObjectNotFoundException e) {
               // it means the file slips ...
               LOG.error(e, e);
             }
           }
         }
         // find lingering files
         if (m.getStore_status() == MetaStoreConst.MFileStoreStatus.RM_PHYSICAL) {
           SFile s = convertToSFile(m);
           s.setLocations(l);
           lingering.add(s);
         }
         if (m.getStore_status() != MetaStoreConst.MFileStoreStatus.INCREATE) {
           int offnr = 0, onnr = 0;
 
           for (SFileLocation fl : l) {
             if (fl.getVisit_status() == MetaStoreConst.MFileLocationVisitStatus.ONLINE) {
               onnr++;
             } else if (fl.getVisit_status() == MetaStoreConst.MFileLocationVisitStatus.OFFLINE) {
               offnr++;
             }
           }
           if ((m.getRep_nr() <= onnr && offnr > 0) ||
               (onnr + offnr >= node_nr && offnr > 0)) {
             try {
               SFile s = convertToSFile(m);
               s.setLocations(l);
               lingering.add(s);
             } catch (javax.jdo.JDOObjectNotFoundException e) {
               // it means the file slips ...
               LOG.error(e, e);
             }
           }
         }
       }
       commited = commitTransaction();
     } finally {
       if (!commited) {
         rollbackTransaction();
       }
     }
   }
 
   public List<SFile> findUnderReplicatedFiles() throws MetaException {
     List<SFile> r = new ArrayList<SFile>();
     boolean commited = false;
 
     try {
       openTransaction();
       Query q = pm.newQuery(MFile.class, "this.store_status == closed || this.store_status == replicated");
       q.declareParameters("int closed, int replicated");
       Collection allFiles = (Collection)q.execute(MetaStoreConst.MFileStoreStatus.CLOSED, MetaStoreConst.MFileStoreStatus.REPLICATED);
       Iterator iter = allFiles.iterator();
       while (iter.hasNext()) {
         MFile m = (MFile)iter.next();
         if (m != null &&
             (m.getStore_status() == MetaStoreConst.MFileStoreStatus.CLOSED ||
              m.getStore_status() == MetaStoreConst.MFileStoreStatus.REPLICATED)) {
           List<SFileLocation> l = getSFileLocations(m.getFid());
           int nr = 0;
 
           for (SFileLocation fl : l) {
             if (fl.getVisit_status() == MetaStoreConst.MFileLocationVisitStatus.ONLINE) {
               nr++;
             }
           }
           if (m.getRep_nr() > nr) {
             SFile s = convertToSFile(m);
             s.setLocations(l);
             r.add(s);
           }
         }
       }
       commited = commitTransaction();
     } finally {
       if (!commited) {
         rollbackTransaction();
       }
     }
 
     return r;
   }
 
   public List<SFile> findOverReplicatedFiles() throws MetaException {
     List<SFile> r = new ArrayList<SFile>();
     boolean commited = false;
 
     try {
       openTransaction();
       Query q = pm.newQuery(MFile.class, "this.store_status != increate");
       q.declareParameters("int increate");
       Collection allFiles = (Collection)q.execute(MetaStoreConst.MFileStoreStatus.INCREATE);
       Iterator iter = allFiles.iterator();
       while (iter.hasNext()) {
         MFile m = (MFile)iter.next();
         if (m != null &&
             (m.getStore_status() != MetaStoreConst.MFileStoreStatus.INCREATE)) {
           List<SFileLocation> l = getSFileLocations(m.getFid());
           int nr = 0;
 
           for (SFileLocation fl : l) {
             if (fl.getVisit_status() == MetaStoreConst.MFileLocationVisitStatus.ONLINE) {
               nr++;
             }
           }
           if (m.getRep_nr() < nr) {
             SFile s = convertToSFile(m);
             s.setLocations(l);
             r.add(s);
           }
         }
       }
       commited = commitTransaction();
     } finally {
       if (!commited) {
         rollbackTransaction();
       }
     }
 
     return r;
   }
 
   public List<SFile> findLingeringFiles(long node_nr) throws MetaException {
     List<SFile> r = new ArrayList<SFile>();
     boolean commited = false;
 
     try {
       openTransaction();
       Query q = pm.newQuery(MFile.class, "this.store_status != increate");
       q.declareParameters("int increate");
       Collection allFiles = (Collection)q.execute(MetaStoreConst.MFileStoreStatus.INCREATE);
       Iterator iter = allFiles.iterator();
       while (iter.hasNext()) {
         MFile m = (MFile)iter.next();
         if (m != null && m.getStore_status() == MetaStoreConst.MFileStoreStatus.RM_PHYSICAL) {
           List<SFileLocation> l = getSFileLocations(m.getFid());
           SFile s = convertToSFile(m);
           s.setLocations(l);
           r.add(s);
         } else if (m != null && (m.getStore_status() != MetaStoreConst.MFileStoreStatus.INCREATE)) {
           List<SFileLocation> l = getSFileLocations(m.getFid());
           int offnr = 0, onnr = 0;
 
           for (SFileLocation fl : l) {
             if (fl.getVisit_status() == MetaStoreConst.MFileLocationVisitStatus.ONLINE) {
               onnr++;
             } else if (fl.getVisit_status() == MetaStoreConst.MFileLocationVisitStatus.OFFLINE) {
               offnr++;
             }
           }
           if ((m.getRep_nr() <= onnr && offnr > 0) ||
               (onnr + offnr >= node_nr && offnr > 0)) {
             SFile s = convertToSFile(m);
             s.setLocations(l);
             r.add(s);
           }
         }
       }
       commited = commitTransaction();
     } finally {
       if (!commited) {
         rollbackTransaction();
       }
     }
 
     return r;
   }
 
   /**
    *
    * @param from: begin time (sec)
    * @param to: end time (sec)
    * @return
    * @throws MetaException
    */
   @Override
   public statfs statFileSystem(long from, long to) throws MetaException {
     statfs s = new statfs();
     boolean commited = false;
 
     // select the files in the time range
     try {
       openTransaction();
       Query q = pm.newQuery(MFile.class, "this.create_time >= begin && this.create_time < end");
       q.declareParameters("long begin, long end");
       Collection files = (Collection)q.execute(from * 1000, to * 1000);
       LOG.info("Total Hit " + files.size() + " files in time range [" + from + "," + to + ").");
       Iterator iter = files.iterator();
       while (iter.hasNext()) {
         MFile m = (MFile)iter.next();
         if (m == null) {
           continue;
         }
         s.setRecordnr(s.getRecordnr() + m.getRecord_nr());
         s.setLength(s.getLength() + m.getLength());
         switch (m.getStore_status()) {
         case MetaStoreConst.MFileStoreStatus.INCREATE:
           s.setIncreate(s.getIncreate() + 1);
           break;
         case MetaStoreConst.MFileStoreStatus.CLOSED:
           s.setClose(s.getClose() + 1);
           s.addToClos(m.getFid());
           break;
         case MetaStoreConst.MFileStoreStatus.REPLICATED:
           s.setReplicated(s.getReplicated() + 1);
           break;
         case MetaStoreConst.MFileStoreStatus.RM_LOGICAL:
           s.setRm_logical(s.getRm_logical() + 1);
           break;
         case MetaStoreConst.MFileStoreStatus.RM_PHYSICAL:
           s.setRm_physical(s.getRm_physical() + 1);
           break;
         }
         if (m.getTable() != null && m.getTable().getTableName() != null) {
           if (s.getFnrs() == null) {
             s.setFnrs(new HashMap<String, Long>());
           }
           Long fnr = s.getFnrs().get(m.getTable().getTableName());
           if (fnr == null) {
             fnr = 0L;
           }
           s.putToFnrs(m.getTable().getTableName(), ++fnr);
         }
 
         // get under/over/linger files
         List<SFileLocation> l = getSFileLocations(m.getFid());
         int on = 0, off = 0, susp = 0;
 
         for (SFileLocation fl : l) {
           switch (fl.getVisit_status()) {
           case MetaStoreConst.MFileLocationVisitStatus.ONLINE:
             on++;
             break;
           case MetaStoreConst.MFileLocationVisitStatus.OFFLINE:
             off++;
             break;
           case MetaStoreConst.MFileLocationVisitStatus.SUSPECT:
             susp++;
             break;
           }
         }
         if (on > m.getRep_nr()) {
           s.setOverrep(s.getOverrep() + 1);
         }
         if (on < m.getRep_nr()) {
           s.setUnderrep(s.getUnderrep() + 1);
         }
         if (on >= m.getRep_nr() && off > 0) {
           s.setLinger(s.getLinger() + 1);
         }
         if (susp > 0) {
           s.setSuspect(s.getSuspect() + 1);
         }
         if (on == 1 && m.getStore_status() == MetaStoreConst.MFileStoreStatus.INCREATE) {
           s.setInc_ons(s.getInc_ons() + 1);
         }
         if (on > 1 && m.getStore_status() == MetaStoreConst.MFileStoreStatus.INCREATE) {
           s.setInc_ons2(s.getInc_ons2() + 1);
           s.addToIncs(m.getFid());
         }
         if (on == 0 && m.getStore_status() == MetaStoreConst.MFileStoreStatus.CLOSED) {
           s.setCls_offs(s.getCls_offs() + 1);
         }
       }
       commited = commitTransaction();
     } finally {
       if (!commited) {
         rollbackTransaction();
       }
     }
 
     return s;
   }
 
   public void createDevice(MDevice md) throws InvalidObjectException, MetaException {
     boolean commited = false;
 
     try {
       openTransaction();
       pm.makePersistent(md);
       commited = commitTransaction();
     } finally {
       if (!commited) {
         rollbackTransaction();
       }
     }
   }
 
   private void createPartitionIndexStore(Index index, Partition part, SFile store, long originFid) throws InvalidObjectException, MetaException {
     MPartition mp;
     MIndex mi;
 
     mp = getMPartition(part.getDbName(), part.getTableName(), part.getPartitionName());
     mi = getMIndex(index.getDbName(), index.getOrigTableName(), index.getIndexName());
     if (mi == null || mp == null) {
       throw new InvalidObjectException("Invalid Partition or Index provided!");
     }
     MPartitionIndex mpi = getMPartitionIndex(mi, mp);
     if (mpi == null) {
       throw new InvalidObjectException("Invalid PartitionIndex!");
     }
     MFile mf = getMFile(store.getFid());
     if (mf == null || getMFile(originFid) == null) {
       throw new InvalidObjectException("Invalid PartitionIndex file or origin file");
     }
     MPartitionIndexStore mpis = new MPartitionIndexStore(mpi, mf, originFid);
     boolean commited = false;
 
     try {
       openTransaction();
       pm.makePersistent(mpis);
       commited = commitTransaction();
     } finally {
       if (!commited) {
         rollbackTransaction();
       }
     }
   }
 
   private void createSubPartitionIndexStore(Index index, Subpartition subpart, SFile store, long originFid) throws InvalidObjectException, MetaException {
     MPartition mp;
     MIndex mi;
 
     mp = getMPartition(subpart.getDbName(), subpart.getTableName(), subpart.getPartitionName());
     mi = getMIndex(index.getDbName(), index.getOrigTableName(), index.getIndexName());
     if (mi == null || mp == null) {
       throw new InvalidObjectException("Invalid SubPartition or Index provided!");
     }
     MPartitionIndex mpi = getMPartitionIndex(mi, mp);
     if (mpi == null) {
       throw new InvalidObjectException("Invalid PartitionIndex!");
     }
     MFile mf = getMFile(store.getFid());
     if (mf == null || getMFile(originFid) == null) {
       throw new InvalidObjectException("Invalid PartitionIndex file or origin file");
     }
     MPartitionIndexStore mpis = new MPartitionIndexStore(mpi, mf, originFid);
     boolean commited = false;
 
     try {
       openTransaction();
       pm.makePersistent(mpis);
       commited = commitTransaction();
     } finally {
       if (!commited) {
         rollbackTransaction();
       }
     }
   }
 
   public void createPartitionIndex(Index index, Partition part) throws InvalidObjectException, MetaException, AlreadyExistsException {
     MPartition mp;
     MIndex mi;
 
     mp = getMPartition(part.getDbName(), part.getTableName(), part.getPartitionName());
     mi = getMIndex(index.getDbName(), index.getOrigTableName(), index.getIndexName());
     if (mi == null || mp == null) {
       throw new InvalidObjectException("Invalid Partition or Index provided!");
     }
     MPartitionIndex mpi = getMPartitionIndex(mi, mp);
     if (mpi != null) {
       throw new AlreadyExistsException("This Index " + index.getIndexName() + " and part " + part.getPartitionName() + " has already exist.");
     } else {
       mpi = new MPartitionIndex(mi, mp);
     }
     boolean commited = false;
 
     try {
       openTransaction();
       pm.makePersistent(mpi);
       commited = commitTransaction();
 
       long db_id = Long.parseLong(MSGFactory.getIDFromJdoObjectId(pm.getObjectId(mp.getTable().getDatabase()).toString()));
       if(commited) {
         MetaMsgServer.sendMsg(MSGFactory.generateDDLMsg(MSGType.MSG_NEW_PARTITION_INDEX,db_id,-1, pm, mpi,null));
       }
     } finally {
       if (!commited) {
         rollbackTransaction();
       }
     }
   }
 
   public void createPartitionIndex(Index index, Subpartition part) throws InvalidObjectException, MetaException, AlreadyExistsException {
     MPartition mp;
     MIndex mi;
 
     mp = getMPartition(part.getDbName(), part.getTableName(), part.getPartitionName());
     mi = getMIndex(index.getDbName(), index.getOrigTableName(), index.getIndexName());
     if (mi == null || mp == null) {
       throw new InvalidObjectException("Invalid Partition or Index provided!");
     }
     MPartitionIndex mpi = getMPartitionIndex(mi, mp);
     if (mpi != null) {
       throw new AlreadyExistsException("This Index " + index.getIndexName() + " and subpart " + part.getPartitionName() + " has already exist.");
     } else {
       mpi = new MPartitionIndex(mi, mp);
     }
     boolean commited = false;
 
     try {
       openTransaction();
       pm.makePersistent(mpi);
       commited = commitTransaction();
 
       long db_id = Long.parseLong(MSGFactory.getIDFromJdoObjectId(pm.getObjectId(mp.getTable().getDatabase()).toString()));
       if(commited) {
         MetaMsgServer.sendMsg(MSGFactory.generateDDLMsg(MSGType.MSG_NEW_PARTITION_INDEX,db_id,-1, pm, mpi,null));
       }
     } finally {
       if (!commited) {
         rollbackTransaction();
       }
     }
   }
 
   public void createNode(Node node) throws InvalidObjectException, MetaException {
     boolean commited = false;
 
     for (String ip : node.getIps()) {
       Node other = findNode(ip);
       if (other != null) {
         throw new MetaException("Duplicate IP address for node '" + node.getNode_name() +
             "' vs '" + other.getNode_name() + "' on IP(" + ip + ")");
       }
     }
     if (getNode(node.getNode_name()) != null) {
       throw new MetaException("Duplicate node name '" + node.getNode_name() +"'");
     }
 
     try {
       openTransaction();
       MNode mnode = convertToMNode(node);
       pm.makePersistent(mnode);
       commited = commitTransaction();
       if(commited) {
         MetaMsgServer.sendMsg( MSGFactory.generateDDLMsg(MSGType.MSG_NEW_NODE,-1,-1,pm,mnode,null));
       }
     } finally {
       if (!commited) {
         rollbackTransaction();
       }
     }
   }
 
   public void createFileLocaiton(SFileLocation fl) throws InvalidObjectException, MetaException {
     boolean commited = false;
     try {
       openTransaction();
       MFileLocation mfl = convertToMFileLocation(fl);
       pm.makePersistent(mfl);
       commited = commitTransaction();
     } finally {
       if (!commited) {
         rollbackTransaction();
       }
     }
   }
 
   public SFile createFile(SFile file) throws InvalidObjectException, MetaException {
     SFile newf = null;
     boolean commited = false;
 
     do {
       file.setFid(getNextFID());
       // query on this fid to check if it is a valid fid
       MFile oldf = getMFile(file.getFid());
       if (oldf != null) {
         continue;
       }
       break;
     } while (true);
 
     try {
       openTransaction();
       MFile mfile = convertToMFile(file);
       pm.makePersistent(mfile);
 
 //      LOG.info("---zy-- in createfile: table:"+mfile.getTable() == null);
 //      if(mfile.getTable() != null) {
 //        tableName = mfile.getTable().getTableName();
 //      }
 //      if(mfile.getTable() != null && mfile.getTable().getDatabase() != null) {
 //        dbName = mfile.getTable().getDatabase().getName();
 //      }
 
       newf = convertToSFile(mfile);
       commited = commitTransaction();
 
       HashMap<String, Object> old_params = new HashMap<String, Object>();
       old_params.put("f_id", mfile.getFid());
       old_params.put("db_name", file.getDbName());
       old_params.put("table_name", file.getTableName());
 //      long db_id = Long.parseLong(MSGFactory.getIDFromJdoObjectId(pm.getObjectId(mfile.getTable().getDatabase()).toString()));
 
       if(commited) {
         MetaMsgServer.sendMsg(MSGFactory.generateDDLMsg(MSGType.MSG_CREATE_FILE, -1l, -1l, pm, mfile, old_params));
       }
     } finally {
       if (!commited) {
         rollbackTransaction();
       }
     }
 
     if (commited) {
       return newf;
     } else {
       return null;
     }
   }
 
   public boolean createFileLocation(SFileLocation location) throws InvalidObjectException, MetaException {
     boolean r = true;
     boolean commited = false;
     SFileLocation old = getSFileLocation(location.getDevid(), location.getLocation());
     if (old != null) {
       return false;
     }
     MFileLocation mfloc;
 
     String dbName = null,tableName=null;
     try {
       openTransaction();
       mfloc = convertToMFileLocation(location);
       if (mfloc != null) {
         pm.makePersistent(mfloc);
       }
 //      dbName = mfloc.getFile().getTable().getDatabase().getName();
 //      tableName = mfloc.getFile().getTable().getTableName();
       if(mfloc.getFile().getTable() != null) {
         tableName = mfloc.getFile().getTable().getTableName();
       }
       if(mfloc.getFile().getTable() != null && mfloc.getFile().getTable().getDatabase() != null) {
         dbName = mfloc.getFile().getTable().getDatabase().getName();
       }
       commited = commitTransaction();
     } finally {
       if (!commited) {
         rollbackTransaction();
       }
     }
     if (r && mfloc != null && commited) {
       // send the sfile rep change message
       HashMap<String, Object> old_params = new HashMap<String, Object>();
       old_params.put("f_id", new Long(location.getFid()));
       old_params.put("devid", location.getDevid());
       old_params.put("location", location.getLocation());
       old_params.put("db_name", dbName);
       old_params.put("table_name", tableName);
       MetaMsgServer.sendMsg(MSGFactory.generateDDLMsg(MSGType.MSG_REP_FILE_CHANGE, -1l, -1l, pm, mfloc.getFile(), old_params));
     }
     return r && commited;
   }
 
   public List<Device> convertToDevices(List<MDevice> mds) {
     List<Device> ds = new ArrayList<Device>();
 
     if (mds != null) {
       ds = new ArrayList<Device>();
       for (MDevice md : mds) {
         Device d = new Device(md.getDev_name(), md.getProp(), md.getNode().getNode_name(), md.getStatus());
         ds.add(d);
       }
     }
 
     return ds;
   }
 
   @Override
   public List<Device> listDevice() throws MetaException {
     List<Device> ds = new ArrayList<Device>();
     boolean success = false;
     try {
       openTransaction();
       Query query = pm.newQuery(MDevice.class);
       List<MDevice> mds = (List<MDevice>) query.execute();
       pm.retrieveAll(mds);
 
       success = commitTransaction();
       ds = this.convertToDevices(mds);
     } finally {
       if (!success) {
         rollbackTransaction();
       }
     }
     return ds;
   }
 
   public Device modifyDevice(Device dev, Node node) throws MetaException, NoSuchObjectException, InvalidObjectException {
     MDevice md = getMDevice(dev.getDevid());
     MNode mn;
 
     if (md == null) {
       throw new NoSuchObjectException("Invalid device '" + dev.getDevid() + "'!");
     }
     if (node != null && !node.getNode_name().equals(md.getNode().getNode_name())) {
       mn = getMNode(node.getNode_name());
       if (mn == null) {
         throw new NoSuchObjectException("Invalid node '" + node.getNode_name() + "'!");
       }
       md.setNode(mn);
     }
     md.setProp(dev.getProp());
     md.setStatus(dev.getStatus());
 
     createDevice(md);
     return convertToDevice(md);
   }
 
   public void createOrUpdateDevice(DeviceInfo di, Node node) throws MetaException, InvalidObjectException {
     MDevice md = getMDevice(di.dev.trim());
     boolean doCreate = false;
 
     if (md == null) {
       // create it now
       MNode mn = getMNode(node.getNode_name());
       if (mn == null) {
         throw new InvalidObjectException("Invalid Node name '" + node.getNode_name() + "'!");
       }
       if (di.mp == null) {
         md = new MDevice(mn, di.dev.trim(), di.prop);
       } else {
         md = new MDevice(mn, di.dev.trim(), di.prop, MetaStoreConst.MDeviceStatus.ONLINE);
       }
       doCreate = true;
     } else {
       // update it now?
       if (di.mp != null && md.getStatus() == MetaStoreConst.MDeviceStatus.SUSPECT) {
         // if the Device is in SUSPECT state, update it to ONLINE
         md.setStatus(MetaStoreConst.MDeviceStatus.ONLINE);
       }
       if (!md.getNode().getNode_name().equals(node.getNode_name()) &&
           md.getProp() == MetaStoreConst.MDeviceProp.ALONE) {
         LOG.info("Saved " + md.getNode().getNode_name() + ", this " + node.getNode_name());
         // should update it.
         MNode mn = getMNode(node.getNode_name());
         if (mn == null) {
           throw new InvalidObjectException("Invalid Node name '" + node.getNode_name() + "'!");
         }
         md.setNode(mn);
         doCreate = true;
       }
     }
     if (doCreate) {
       createDevice(md);
     }
   }
 
   public void onlineDevice(String devid) throws MetaException, InvalidObjectException {
     MDevice md = getMDevice(devid);
 
     if (md == null) {
       throw new InvalidObjectException("Invalid devid: " + devid);
     }
     md.setStatus(MetaStoreConst.MDeviceStatus.ONLINE);
 
     createDevice(md);
   }
 
   public void offlineDevice(String devid) throws MetaException, InvalidObjectException {
     MDevice md = getMDevice(devid);
 
     if (md == null) {
       throw new InvalidObjectException("Invalid devid: " + devid);
     }
     md.setStatus(MetaStoreConst.MDeviceStatus.OFFLINE);
 
     createDevice(md);
   }
 
   private void testHook() throws InvalidObjectException, MetaException {
     List<String> ips = Arrays.asList("192.168.11.7", "127.0.0.1");
     //createNode(new Node("test_node", ips, 100));
     //createFile(new SFile(10, 10, 3, 4, "abc", 1, 2, null));
     //createFile(new SFile(20, 10, 5, 6, "xyz", 1, 2, null));
     Node n = new Node("macan", ips, MetaStoreConst.MNodeStatus.SUSPECT);
     SFile sf = new SFile(0, "db", "table", 5, 6, "xyzadfads", 1, 2, null, 100, null, null, 0);
     createNode(n);
     MDevice md1 = new MDevice(getMNode("macan"), "dev-hello", 0, 0);
     MDevice md2 = new MDevice(getMNode("macan"), "xyz1", 0, 0);
     createDevice(md1);
     createDevice(md2);
 
     createFile(sf);
     createFileLocation(new SFileLocation(n.getNode_name(), sf.getFid(), "ffffffff", "xxxxxxxxx", 100, 1000, 2, "yyyyy"));
     Node y = findNode("127.0.0.1");
     if (y == null) {
       LOG.info("BAD++++");
     } else {
       LOG.info("findNode => " + y.getNode_name());
     }
 
     long fid = 0;
     MFile mf = getMFile(fid);
     if (mf == null) {
       LOG.info("Can't get File w/ fid = " + fid);
     } else {
       LOG.info("Read fid from PM: " + mf.getFid());
     }
   }
 
   public void createTable(Table tbl) throws InvalidObjectException, MetaException {
     boolean commited = false;
     try {
       openTransaction();
       MTable mtbl = convertToMTable(tbl);
       boolean make_table = false;
       if(mtbl.getSd().getCD().getCols() != null){//增加业务类型查询支持
         List<MBusiTypeColumn> bcs = new ArrayList<MBusiTypeColumn>();
 
         createBusiTypeCol(mtbl, bcs);
 
         if(!bcs.isEmpty()){
 
           LOG.info("--zjw--getPartitions is not null,size:"+bcs.size());
           pm.makePersistentAll(bcs);
         }else{
           pm.makePersistent(mtbl);
           make_table =true;
           LOG.info("--zjw--getPartitions is null view:"+tbl.getViewExpandedText()+"--"+tbl.getViewOriginalText());
         }
       }
       if(!make_table){
         pm.makePersistent(mtbl);
       }
       if(mtbl.getGroupDistribute() != null && !mtbl.getGroupDistribute().isEmpty()){
         for(MNodeGroup mng : mtbl.getGroupDistribute()){
           mng.getAttachedtables().add(mtbl);
         }
         pm.makePersistentAll(mtbl.getGroupDistribute());
       }
 
       if(tbl.getPartitions() != null && !tbl.getPartitions().isEmpty()){//存储分区
         LOG.info("--zjw--getPartitions is not null,size:"+tbl.getPartitionsSize());
         List<MPartition> mparts = convertToMParts(tbl.getPartitions(),false,tbl.getDbName());
         pm.makePersistentAll(mparts);
       }else{
         LOG.warn("--zjw--getPartitions is null ");
       }
 
       LOG.info("createTable w/ ID=" + JDOHelper.getObjectId(mtbl));
       PrincipalPrivilegeSet principalPrivs = tbl.getPrivileges();
       List<Object> toPersistPrivObjs = new ArrayList<Object>();
       if (principalPrivs != null) {
         int now = (int)(System.currentTimeMillis()/1000);
 
         Map<String, List<PrivilegeGrantInfo>> userPrivs = principalPrivs.getUserPrivileges();
         putPersistentPrivObjects(mtbl, toPersistPrivObjs, now, userPrivs, PrincipalType.USER);
 
         Map<String, List<PrivilegeGrantInfo>> groupPrivs = principalPrivs.getGroupPrivileges();
         putPersistentPrivObjects(mtbl, toPersistPrivObjs, now, groupPrivs, PrincipalType.GROUP);
 
         Map<String, List<PrivilegeGrantInfo>> rolePrivs = principalPrivs.getRolePrivileges();
         putPersistentPrivObjects(mtbl, toPersistPrivObjs, now, rolePrivs, PrincipalType.ROLE);
       }
       pm.makePersistentAll(toPersistPrivObjs);
 
       pm.retrieve(mtbl.getDatabase());
 
       commited = commitTransaction();
 
       long db_id = Long.parseLong(MSGFactory.getIDFromJdoObjectId(pm.getObjectId(mtbl.getDatabase()).toString()));
       if(commited) {
         MetaMsgServer.sendMsg(MSGFactory.generateDDLMsg(MSGType.MSG_NEW_TALBE,db_id,-1, pm, mtbl,null));
       }
     } finally {
       if (!commited) {
         rollbackTransaction();
       }
     }
 
 
   }
 
   private void deleteBusiTypeCol(MTable mtbl) {
     boolean commited = false;
     try {
       openTransaction();
       Query query = pm.newQuery(MBusiTypeColumn.class, "table.tableName == tabName && table.database.name == dbName");
       query.declareParameters("java.lang.String tabName, java.lang.String dbName");
       Collection cols = (Collection)query.execute(mtbl.getTableName(), mtbl.getDatabase().getName());
       Iterator iter = cols.iterator();
       while (iter.hasNext()) {
         MBusiTypeColumn col = (MBusiTypeColumn)iter.next();
         LOG.debug("--> DEL BusiType " + col.getBusiType() + " on col " + col.getColumn() + " for db " +
             mtbl.getDatabase().getName() + " table " + mtbl.getTableName());
         pm.deletePersistent(col);
       }
       commited = commitTransaction();
     } finally {
       if (!commited) {
         rollbackTransaction();
       }
     }
   }
 
   private void createBusiTypeCol(MTable mtbl,List<MBusiTypeColumn> bcs){
     for(MFieldSchema f : mtbl.getSd().getCD().getCols()){
       String cmet = f.getComment();
       if(cmet != null && cmet.indexOf(MetaStoreUtils.BUSI_TYPES_PREFIX)>=0){
         int pos = cmet.indexOf(MetaStoreUtils.BUSI_TYPES_PREFIX);// ip/tel/time/content
         for(String type : MetaStoreUtils.BUSI_TYPES){
           if( cmet.length() - pos >= type.length()
               && type.equals(cmet.substring(pos,type.length()).toLowerCase())){
             MBusiTypeColumn bc = new MBusiTypeColumn(type,mtbl,f.getName());
             bcs.add(bc);
           }
         }
       }
     }
   }
 
   /**
    * Convert PrivilegeGrantInfo from privMap to MTablePrivilege, and add all of
    * them to the toPersistPrivObjs. These privilege objects will be persisted as
    * part of createTable.
    *
    * @param mtbl
    * @param toPersistPrivObjs
    * @param now
    * @param privMap
    * @param type
    */
   private void putPersistentPrivObjects(MTable mtbl, List<Object> toPersistPrivObjs,
       int now, Map<String, List<PrivilegeGrantInfo>> privMap, PrincipalType type) {
     if (privMap != null) {
       for (Map.Entry<String, List<PrivilegeGrantInfo>> entry : privMap
           .entrySet()) {
         String principalName = entry.getKey();
         List<PrivilegeGrantInfo> privs = entry.getValue();
         for (int i = 0; i < privs.size(); i++) {
           PrivilegeGrantInfo priv = privs.get(i);
           if (priv == null) {
             continue;
           }
           MTablePrivilege mTblSec = new MTablePrivilege(
               principalName, type.toString(), mtbl, priv.getPrivilege(),
               now, priv.getGrantor(), priv.getGrantorType().toString(), priv
                   .isGrantOption());
           toPersistPrivObjs.add(mTblSec);
         }
       }
     }
   }
 
   public boolean dropTable(String dbName, String tableName) throws MetaException,
     NoSuchObjectException, InvalidObjectException, InvalidInputException {
     boolean success = false;
     try {
       openTransaction();
       MTable tbl = getMTable(dbName, tableName);
       long db_id = Long.parseLong(MSGFactory.getIDFromJdoObjectId(pm.getObjectId(tbl.getDatabase()).toString()));     //在表被删除之前先获得id
       pm.retrieve(tbl);
       if (tbl != null) {
         // first remove all the grants
         List<MTablePrivilege> tabGrants = listAllTableGrants(dbName, tableName);
         if (tabGrants != null && tabGrants.size() > 0) {
           pm.deletePersistentAll(tabGrants);
         }
         List<MTableColumnPrivilege> tblColGrants = listTableAllColumnGrants(dbName,
             tableName);
         if (tblColGrants != null && tblColGrants.size() > 0) {
           pm.deletePersistentAll(tblColGrants);
         }
 
         List<MPartitionPrivilege> partGrants = this.listTableAllPartitionGrants(dbName, tableName);
         if (partGrants != null && partGrants.size() > 0) {
           pm.deletePersistentAll(partGrants);
         }
 
         List<MPartitionColumnPrivilege> partColGrants = listTableAllPartitionColumnGrants(dbName,
             tableName);
         if (partColGrants != null && partColGrants.size() > 0) {
           pm.deletePersistentAll(partColGrants);
         }
         // delete column statistics if present
         try {
           deleteTableColumnStatistics(dbName, tableName, null);
         } catch (NoSuchObjectException e) {
           LOG.info("Found no table level column statistics associated with db " + dbName +
           " table " + tableName + " record to delete");
         }
 
         preDropStorageDescriptor(tbl.getSd());
         // FIXME: reset the schema here? and delete files? and delete busitypecols
         deleteBusiTypeCol(tbl);
 
         tbl.setSchema(null);
         tbl.setDatabase(null);
 
 
         // then remove the table
         pm.deletePersistentAll(tbl);
 
 
       }
       success = commitTransaction();
 
       HashMap<String, Object> old_params = new HashMap<String, Object>();
       old_params.put("table_name", tableName);
       old_params.put("db_name", dbName);
       if(success) {
         MetaMsgServer.sendMsg(MSGFactory.generateDDLMsg(MSGType.MSG_DROP_TABLE, db_id, -1l, pm, tbl, old_params));
       }
     } finally {
       if (!success) {
         rollbackTransaction();
       }
     }
     return success;
   }
 
   public boolean updateNode(Node node) throws MetaException {
     boolean success = false;
     boolean commited = false;
     boolean changed = false;
 
     MNode mn = getMNode(node.getNode_name());
     if (mn != null) {
       if (mn.getStatus() != node.getStatus()) {
         changed = true;
       }
       mn.setStatus(node.getStatus());
       mn.setIpList(node.getIps());
     } else {
       return success;
     }
 
     try {
       openTransaction();
       pm.makePersistent(mn);
       commited = commitTransaction();
     } finally {
       if (!commited) {
         rollbackTransaction();
       } else {
         success = true;
       }
     }
     if (success && changed) {
       HashMap<String, Object> old_params = new HashMap<String, Object>();
       long event = 0;
 
       old_params.put("node_name", node.getNode_name());
       old_params.put("status", node.getStatus());
       if (node.getStatus() == MetaStoreConst.MNodeStatus.SUSPECT ||
           node.getStatus() == MetaStoreConst.MNodeStatus.OFFLINE) {
         event = MSGType.MSG_FAIL_NODE;
       } else if (node.getStatus() == MetaStoreConst.MNodeStatus.ONLINE) {
         event = MSGType.MSG_BACK_NODE;
       }
       if (event != 0) {
         MetaMsgServer.sendMsg(MSGFactory.generateDDLMsg(event, -1l, -1l, pm, mn, old_params));
       }
     }
 
     return success;
   }
 
   public long countNode() throws MetaException {
     boolean commited = false;
     long r = 0;
 
     try {
       openTransaction();
       Query query = pm.newQuery("javax.jdo.query.SQL", "SELECT count(*) FROM NODES");
       List results = (List) query.execute();
       BigDecimal tableSize = (BigDecimal) results.iterator().next();
       r = tableSize.longValue();
       commited = commitTransaction();
     } finally {
       if (!commited) {
         rollbackTransaction();
       }
     }
     return r;
   }
 
   @Override
   public long countDevice() throws MetaException {
     boolean commited = false;
     long r = 0;
 
     try {
       openTransaction();
       Query query = pm.newQuery("javax.jdo.query.SQL", "SELECT count(*) FROM DEVICES");
       List results = (List) query.execute();
       BigDecimal tableSize = (BigDecimal) results.iterator().next();
       r = tableSize.longValue();
       commited = commitTransaction();
     } finally {
       if (!commited) {
         rollbackTransaction();
       }
     }
     return r;
   }
 
   public List<Node> getAllNodes() throws MetaException {
     List<Node> ln = new ArrayList<Node>();
     boolean commited = false;
 
     try {
       openTransaction();
       Query q = pm.newQuery(MNode.class);
       Collection allNodes = (Collection)q.execute();
       Iterator iter = allNodes.iterator();
       while (iter.hasNext()) {
         Node n = convertToNode((MNode)iter.next());
         ln.add(n);
       }
       commited = commitTransaction();
     } finally {
       if (!commited) {
         rollbackTransaction();
       }
     }
     return ln;
   }
 
   public Device getDevice(String devid) throws MetaException, NoSuchObjectException {
     boolean commited = false;
     Device d = null;
 
     try {
       openTransaction();
       d = convertToDevice(getMDevice(devid));
       commited = commitTransaction();
     } finally {
       if (!commited) {
         rollbackTransaction();
       }
     }
     if (d == null) {
       throw new NoSuchObjectException("Can not find Device " + devid + ", please check it!");
     }
     return d;
   }
 
   public Node getNode(String node_name) throws MetaException {
     boolean commited = false;
     Node n = null;
     if (node_name == null) {
       throw new MetaException("Null node name provided!");
     }
     try {
       openTransaction();
       // if node_name contains("."), then it is a remote node, caller should getNode from other DC
       if (node_name.contains(".")) {
         String[] ns = node_name.split(".");
         if (ns.length != 2) {
           throw new MetaException("Node name " + node_name + " contains too many '.'!");
         }
         node_name = ns[1];
       }
       n = convertToNode(getMNode(node_name));
       commited = commitTransaction();
     } finally {
       if (!commited) {
         rollbackTransaction();
       }
     }
     return n;
   }
 
   public MPartitionIndex getPartitionIndex(Index index, Partition part) throws InvalidObjectException, MetaException, NoSuchObjectException {
     boolean commited = false;
     MPartitionIndex mpi = null;
     try {
       MPartition mp;
       MIndex mi;
       openTransaction();
       mp = getMPartition(part.getDbName(), part.getTableName(), part.getPartitionName());
       mi = getMIndex(index.getDbName(), index.getOrigTableName(), index.getIndexName());
       if (mp == null || mi == null) {
         throw new InvalidObjectException("Invalid Index or Partition provided!");
       }
       mpi = getMPartitionIndex(mi, mp);
       commited = commitTransaction();
     } finally {
       if (!commited) {
         rollbackTransaction();
       }
     }
     if (mpi == null) {
       throw new NoSuchObjectException("Can not find the PartitionIndex, please check it!");
     }
     return mpi;
   }
 
   public MPartitionIndex getSubpartitionIndex(Index index, Subpartition subpart) throws InvalidObjectException, NoSuchObjectException, MetaException {
     boolean commited = false;
     MPartitionIndex mpi = null;
     try {
       MPartition mp;
       MIndex mi;
       openTransaction();
       mp = getMPartition(subpart.getDbName(), subpart.getTableName(), subpart.getPartitionName());
       mi = getMIndex(index.getDbName(), index.getOrigTableName(), index.getIndexName());
       if (mp == null || mi == null) {
         throw new InvalidObjectException("Invalid Index or subPartition provided!");
       }
       mpi = getMPartitionIndex(mi, mp);
       commited = commitTransaction();
     } finally {
       if (!commited) {
         rollbackTransaction();
       }
     }
     if (mpi == null) {
       throw new NoSuchObjectException("Can not find the PartitionIndex, please check it!");
     }
     return mpi;
   }
 
   public SFile getSFile(long fid) throws MetaException {
     boolean commited = false;
     SFile f = null;
     try {
       openTransaction();
       f = convertToSFile(getMFile(fid));
       commited = commitTransaction();
     } finally {
       if (!commited) {
         rollbackTransaction();
       }
     }
 
     return f;
   }
 
   public SFile getSFile(String devid, String location) throws MetaException {
     boolean commited = false;
     SFile f = null;
     try {
       openTransaction();
       f = convertToSFile(getMFile(devid, location));
       commited = commitTransaction();
     } finally {
       if (!commited) {
         rollbackTransaction();
       }
     }
 
     return f;
   }
 
   public List<SFileLocation> getSFileLocations(long fid) throws MetaException {
     boolean commited = false;
     List<SFileLocation> sfl = new ArrayList<SFileLocation>();
     try {
       openTransaction();
       sfl = convertToSFileLocation(getMFileLocations(fid));
       commited = commitTransaction();
     } finally {
       if (!commited) {
         rollbackTransaction();
       }
     }
     return sfl;
   }
 
   public List<SFileLocation> getSFileLocations(String devid, long curts, long timeout) throws MetaException {
     boolean commited = false;
     List<SFileLocation> sfl = new ArrayList<SFileLocation>();
     try {
       openTransaction();
       sfl = convertToSFileLocation(getMFileLocations(devid, curts, timeout));
       commited = commitTransaction();
     } finally {
       if (!commited) {
         rollbackTransaction();
       }
     }
     return sfl;
   }
 
   public List<SFileLocation> getSFileLocations(int status) throws MetaException {
     boolean commited = false;
     List<SFileLocation> sfl = new ArrayList<SFileLocation>();
     try {
       openTransaction();
       sfl = convertToSFileLocation(getMFileLocations(status));
       commited = commitTransaction();
     } finally {
       if (!commited) {
         rollbackTransaction();
       }
     }
     return sfl;
   }
 
   public SFileLocation getSFileLocation(String devid, String location) throws MetaException {
     boolean commited = false;
     SFileLocation sfl = null;
     try {
       openTransaction();
       sfl = convertToSFileLocation(getMFileLocation(devid, location));
       commited = commitTransaction();
     } finally {
       if (!commited) {
         rollbackTransaction();
       }
     }
     return sfl;
   }
 
   @Override
   public boolean reopenSFile(SFile file) throws MetaException {
     boolean commited = false;
     MFile mf = null;
     SFile f = null;
     boolean changed = false;
 
     try {
       List<MFileLocation> toOffline = new ArrayList<MFileLocation>();
 
       openTransaction();
       mf = getMFile(file.getFid());
       if (mf.getStore_status() == MetaStoreConst.MFileStoreStatus.REPLICATED) {
         mf.setStore_status(MetaStoreConst.MFileStoreStatus.INCREATE);
         pm.makePersistent(mf);
 
         List<MFileLocation> mfl = getMFileLocations(file.getFid());
         boolean selected = false;
         int idx = 0;
 
         if (mfl.size() > 0) {
           for (int i = 0; i < mfl.size(); i++) {
             MFileLocation x = mfl.get(i);
 
             if (x.getVisit_status() == MetaStoreConst.MFileLocationVisitStatus.ONLINE) {
               selected = true;
               idx = i;
               break;
             }
           }
           if (selected) {
             // it is ok to reopen, and close other locations
             for (int i = 0; i < mfl.size(); i++) {
               if (i != idx) {
                 MFileLocation x = mfl.get(i);
                 // mark it as OFFLINE
                 x.setVisit_status(MetaStoreConst.MFileLocationVisitStatus.OFFLINE);
                 pm.makePersistent(x);
                 toOffline.add(x);
               }
             }
           }
         }
         if (selected) {
           changed = true;
         }
       }
       commited = commitTransaction();
       if (commited && changed) {
         // ok, send msgs
         HashMap<String, Object> old_params = new HashMap<String, Object>();
         old_params.put("f_id", file.getFid());
         old_params.put("new_status", MetaStoreConst.MFileStoreStatus.INCREATE);
         MetaMsgServer.sendMsg(MSGFactory.generateDDLMsg(MSGType.MSG_STA_FILE_CHANGE, -1l, -1l, pm, mf, old_params));
         if (toOffline.size() > 0) {
           for (MFileLocation y : toOffline) {
             HashMap<String, Object> params = new HashMap<String, Object>();
             old_params.put("f_id", file.getFid());
             old_params.put("new_status", MetaStoreConst.MFileLocationVisitStatus.OFFLINE);
             MetaMsgServer.sendMsg(MSGFactory.generateDDLMsg(MSGType.MSG_REP_FILE_ONOFF, -1l, -1l, pm, y, params));
           }
         }
       }
     } finally {
       if (!commited) {
         rollbackTransaction();
       }
     }
     return changed && commited;
   }
 
   public SFile updateSFile(SFile newfile) throws MetaException {
     boolean commited = false;
     boolean repnr_changed = false;
     boolean stat_changed = false;
     MFile mf = null;
     SFile f = null;
 
     String dbName = null,tableName=null;
     try {
       openTransaction();
       mf = getMFile(newfile.getFid());
       if (mf.getRep_nr() != newfile.getRep_nr()) {
         repnr_changed = true;
       }
       if (mf.getStore_status() != newfile.getStore_status()) {
         stat_changed = true;
       }
       mf.setRep_nr(newfile.getRep_nr());
       mf.setDigest(newfile.getDigest());
       mf.setRecord_nr(newfile.getRecord_nr());
       mf.setAll_record_nr(newfile.getAll_record_nr());
       mf.setStore_status(newfile.getStore_status());
       mf.setLoad_status(newfile.getLoad_status());
       mf.setLength(newfile.getLength());
 
       tableName = newfile.getTableName();
       dbName = newfile.getDbName();
 
       pm.makePersistent(mf);
       f = convertToSFile(mf);
 
 //      tableName = mf.getTable().getTableName();
 //      dbName = mf.getTable().getDatabase().getName();
       commited = commitTransaction();
 
     } finally {
       if (!commited) {
         rollbackTransaction();
       }
     }
     if (f == null || mf == null) {
       throw new MetaException("Invalid SFile object provided!");
     }
 
     if (stat_changed) {
       // send the SFile state change message
       HashMap<String, Object> old_params = new HashMap<String, Object>();
       old_params.put("f_id", newfile.getFid());
       old_params.put("new_status", newfile.getStore_status());
       old_params.put("db_name", dbName);
       old_params.put("table_name", tableName);
       MetaMsgServer.sendMsg(MSGFactory.generateDDLMsg(MSGType.MSG_STA_FILE_CHANGE, -1l, -1l, pm, mf, old_params));
     }
     if (repnr_changed) {
       // send the SFile state change message
       HashMap<String, Object> old_params = new HashMap<String, Object>();
       old_params.put("f_id", newfile.getFid());
       old_params.put("new_repnr", newfile.getRep_nr());
       old_params.put("db_name", dbName);
       old_params.put("table_name", tableName);
       MetaMsgServer.sendMsg(MSGFactory.generateDDLMsg(MSGType.MSG_FILE_USER_SET_REP_CHANGE, -1l, -1l, pm, mf, old_params));
     }
     return f;
   }
 
   public SFileLocation updateSFileLocation(SFileLocation newsfl) throws MetaException {
     boolean commited = false;
     boolean changed = false;
     SFileLocation sfl = null;
     MFileLocation mfl = null;
 
     String dbName=null,tableName=null;
     try {
       openTransaction();
       mfl = getMFileLocation(newsfl.getDevid(), newsfl.getLocation());
       if (mfl == null) {
         throw new MetaException("Invalid SFileLocation provided or JDO Commit failed!");
       }
       pm.retrieveAll(mfl);
       if(mfl.getFile().getTable() != null) {
         tableName = mfl.getFile().getTable().getTableName();
       }
       if(mfl.getFile().getTable() != null && mfl.getFile().getTable().getDatabase() != null) {
         dbName = mfl.getFile().getTable().getDatabase().getName();
       }
       mfl.setUpdate_time(System.currentTimeMillis());
       if (mfl.getVisit_status() != newsfl.getVisit_status()) {
         changed = true;
         mfl.setVisit_status(newsfl.getVisit_status());
       }
       mfl.setRep_id(newsfl.getRep_id());
       mfl.setDigest(newsfl.getDigest());
 
       pm.makePersistent(mfl);
       sfl = convertToSFileLocation(mfl);
 
 
       commited = commitTransaction();
     } finally {
       if (!commited) {
         rollbackTransaction();
         sfl = null;
       }
     }
     if (sfl == null || mfl == null) {
       throw new MetaException("Invalid SFileLocation provided or JDO Commit failed!");
     }
 
     if (changed) {
       // send the SFL state change message
       HashMap<String, Object> old_params = new HashMap<String, Object>();
       old_params.put("f_id", newsfl.getFid());
       old_params.put("new_status", newsfl.getVisit_status());
       old_params.put("db_name", dbName);
       old_params.put("table_name", tableName);
       MetaMsgServer.sendMsg(MSGFactory.generateDDLMsg(MSGType.MSG_REP_FILE_ONOFF, -1l, -1l, pm, mfl, old_params));
     }
 
     return sfl;
   }
 
   public Table getTableByID(long id) throws MetaException {
     boolean commited = false;
     Table tbl = null;
     try {
       openTransaction();
       String oidStr = Long.toString(id) + "[OID]" + MTable.class.getName();
       MTable mtbl = (MTable)pm.getObjectById(MTable.class, oidStr);
       tbl = convertToTable(mtbl);
       commited = commitTransaction();
     } finally {
       if (!commited) {
         rollbackTransaction();
       }
     }
     if (tbl == null) {
       throw new MetaException("Invalid Table ID " + id + " provided!");
     }
     return tbl;
   }
 
   public Table getTable(String dbName, String tableName) throws MetaException {
     boolean commited = false;
     Table tbl = null;
     try {
       openTransaction();
       tbl = convertToTable(getMTable(dbName, tableName));
       commited = commitTransaction();
     } finally {
       if (!commited) {
         rollbackTransaction();
       }
     }
     return tbl;
   }
 
   public long getTableOID(String dbName, String tableName) throws MetaException {
     boolean commited = false;
     long oid = -1;
 
     try {
       openTransaction();
       MTable mtbl = getMTable(dbName, tableName);
       commited = commitTransaction();
       oid = Long.parseLong(JDOHelper.getObjectId(mtbl).toString().split("\\[OID\\]")[0]);
     } finally {
       if (!commited) {
         rollbackTransaction();
       }
     }
     if (oid == -1) {
       throw new MetaException("Invalid table: " + dbName + " " + tableName);
     }
     return oid;
   }
 
   public List<String> getTables(String dbName, String pattern)
       throws MetaException {
     boolean commited = false;
     List<String> tbls = null;
     try {
       openTransaction();
       dbName = dbName.toLowerCase().trim();
       // Take the pattern and split it on the | to get all the composing
       // patterns
       String[] subpatterns = pattern.trim().split("\\|");
       String query =
         "select tableName from org.apache.hadoop.hive.metastore.model.MTable "
         + "where database.name == dbName && (";
       boolean first = true;
       for (String subpattern : subpatterns) {
         subpattern = "(?i)" + subpattern.replaceAll("\\*", ".*");
         if (!first) {
           query = query + " || ";
         }
         query = query + " tableName.matches(\"" + subpattern + "\")";
         first = false;
       }
       query = query + ")";
 
       Query q = pm.newQuery(query);
       q.declareParameters("java.lang.String dbName");
       q.setResult("tableName");
       q.setOrdering("tableName ascending");
       Collection names = (Collection) q.execute(dbName);
       tbls = new ArrayList<String>();
       for (Iterator i = names.iterator(); i.hasNext();) {
         tbls.add((String) i.next());
       }
       commited = commitTransaction();
     } finally {
       if (!commited) {
         rollbackTransaction();
       }
     }
     return tbls;
   }
 
   public List<String> getAllTables(String dbName) throws MetaException {
     return getTables(dbName, ".*");
   }
 
   private MDevice getMDevice(String dev_name) {
     MDevice md = null;
     boolean commited = false;
     try {
       openTransaction();
       dev_name = dev_name.trim();
       Query query = pm.newQuery(MDevice.class, "this.dev_name == dev_name");
       query.declareParameters("java.lang.String dev_name");
       query.setUnique(true);
       md = (MDevice)query.execute(dev_name);
       pm.retrieve(md);
       commited = commitTransaction();
     } finally {
       if (!commited) {
         rollbackTransaction();
       }
     }
     return md;
   }
 
   private MPartitionIndex getMPartitionIndex(MIndex index, MPartition part) {
     MPartitionIndex mpi = null;
     boolean commited = false;
     try {
       openTransaction();
       Query query = pm.newQuery(MPartitionIndex.class, "this.index.indexName == indexName && this.partition.partitionName == partName");
       query.declareParameters("java.lang.String indexName, java.lang.String partName");
       query.setUnique(true);
       mpi = (MPartitionIndex)query.execute(index.getIndexName(), part.getPartitionName());
       if (mpi != null) {
         pm.retrieve(mpi);
       }
       commited = commitTransaction();
     } finally {
       if (!commited) {
         rollbackTransaction();
       }
     }
     return mpi;
   }
 
   private MPartitionIndexStore getMPartitionIndexStore(MPartitionIndex mpi, MFile mf) {
     MPartitionIndexStore mpis = null;
     boolean commited = false;
     try {
       openTransaction();
       Query query = pm.newQuery(MPartitionIndexStore.class, "this.pi.index.indexName == indexName && this.pi.partition.partitionName == partName && this.indexFile.fid == fid");
       query.declareParameters("java.lang.String indexName, java.lang.String partName, long fid");
       query.setUnique(true);
       mpis = (MPartitionIndexStore)query.execute(mpi.getIndex().getIndexName(), mpi.getPartition().getPartitionName(), mf.getFid());
       pm.retrieve(mpis);
       commited = commitTransaction();
     } finally {
       if (!commited) {
         rollbackTransaction();
       }
     }
     return mpis;
   }
 
   private List<MPartitionIndex> getMPartitionIndexByIndex(MIndex index) {
     List<MPartitionIndex> mpil = new ArrayList<MPartitionIndex>();
     boolean commited = false;
     try {
       openTransaction();
       Query query = pm.newQuery(MPartitionIndex.class, "this.index.indexName == indexName");
       query.declareParameters("java.lang.String indexName");
       Collection allpis = (Collection)query.execute();
       Iterator iter = allpis.iterator();
       while (iter.hasNext()) {
         MPartitionIndex p = (MPartitionIndex)iter.next();
         mpil.add(p);
       }
       commited = commitTransaction();
     } finally {
       if (!commited) {
         rollbackTransaction();
       }
     }
     return mpil;
   }
 
   private List<MPartitionIndex> getMPartitionIndexByPartition(MPartition part) {
     List<MPartitionIndex> mpil = new ArrayList<MPartitionIndex>();
     boolean commited = false;
     try {
       openTransaction();
       Query query = pm.newQuery(MPartitionIndex.class, "this.partition.partitionName == partName");
       query.declareParameters("java.lang.String partName");
       Collection allpis = (Collection)query.execute();
       Iterator iter = allpis.iterator();
       while (iter.hasNext()) {
         MPartitionIndex p = (MPartitionIndex)iter.next();
         mpil.add(p);
       }
       commited = commitTransaction();
     } finally {
       if (!commited) {
         rollbackTransaction();
       }
     }
     return mpil;
   }
 
   private MNode getMNode(String node_name) {
     MNode mn = null;
     boolean commited = false;
 
     try {
       openTransaction();
       if (!node_name.contains(".")) {
         node_name = node_name.trim();
         Query query = pm.newQuery(MNode.class, "this.node_name == node_name");
         query.declareParameters("java.lang.String node_name");
         query.setUnique(true);
         mn = (MNode)query.execute(node_name);
         pm.retrieve(mn);
       }
       commited = commitTransaction();
     } finally {
       if (!commited) {
         rollbackTransaction();
       }
     }
     return mn;
   }
 
   private MFile getMFile(long fid) {
     MFile mf = null;
     boolean commited = false;
     try {
       openTransaction();
       Query query = pm.newQuery(MFile.class, "this.fid == fid");
       query.declareParameters("long fid");
       query.setUnique(true);
       mf = (MFile)query.execute(fid);
       pm.retrieve(mf);
       commited = commitTransaction();
     } finally {
       if (!commited) {
         rollbackTransaction();
       }
     }
     return mf;
   }
 
   private MFile getMFile(String devid, String location) {
     MFile mf = null;
     MFileLocation mfl = null;
     boolean commited = false;
     try {
       openTransaction();
       mfl = getMFileLocation(devid, location);
       if (mfl != null) {
         mf = mfl.getFile();
       }
       commited = commitTransaction();
     } finally {
       if (!commited) {
         rollbackTransaction();
       }
     }
     return mf;
   }
 
   private MFileLocation getMFileLocation(String devid, String location) {
     MFileLocation mfl = null;
     boolean commited = false;
     try {
       openTransaction();
       Query query = pm.newQuery(MFileLocation.class,
           "this.dev.dev_name == devid && this.location == location");
       query.declareParameters("java.lang.String devid, java.lang.String location");
       query.setUnique(true);
       query.getFetchPlan().setMaxFetchDepth(2);
       query.getFetchPlan().setFetchSize(FetchPlan.FETCH_SIZE_GREEDY);
       mfl = (MFileLocation)query.execute(devid, location);
       if (mfl != null) {
         pm.retrieve(mfl);
       }
       commited = commitTransaction();
     } finally {
       if (!commited) {
         rollbackTransaction();
       }
     }
     return mfl;
   }
 
   private List<MFileLocation> getMFileLocations(long fid) {
     List<MFileLocation> mfl = new ArrayList<MFileLocation>();
     boolean commited = false;
 
     try {
       openTransaction();
       Query query = pm.newQuery(MFileLocation.class, "this.file.fid == fid");
       query.declareParameters("long fid");
       query.getFetchPlan().setMaxFetchDepth(3);
       query.getFetchPlan().setFetchSize(FetchPlan.FETCH_SIZE_GREEDY);
       List l = (List)query.execute(fid);
       Iterator iter = l.iterator();
       while (iter.hasNext()) {
         MFileLocation mf = (MFileLocation)iter.next();
         mfl.add(mf);
       }
       commited = commitTransaction();
     } finally {
       if (!commited) {
         rollbackTransaction();
       }
     }
     return mfl;
   }
 
   private List<MFileLocation> getMFileLocations(int status) {
     List<MFileLocation> mfl = new ArrayList<MFileLocation>();
     boolean commited = false;
 
     try {
       openTransaction();
       Query query = pm.newQuery(MFileLocation.class, "this.visit_status == status");
       query.declareParameters("int status");
       query.getFetchPlan().setMaxFetchDepth(2);
       List l = (List)query.execute(status);
       Iterator iter = l.iterator();
       while (iter.hasNext()) {
         MFileLocation mf = (MFileLocation)iter.next();
         mfl.add(mf);
       }
       commited = commitTransaction();
     } finally {
       if (!commited) {
         rollbackTransaction();
       }
     }
     return mfl;
   }
 
   private List<MFileLocation> getMFileLocations(String devid, long curts, long timeout) {
     List<MFileLocation> mfl = new ArrayList<MFileLocation>();
     boolean commited = false;
 
     try {
       openTransaction();
       Query query = pm.newQuery(MFileLocation.class, "this.dev.dev_name == devid && this.update_time + timeout < curts");
       query.declareParameters("java.lang.String devid, long timeout, long curts");
       query.getFetchPlan().setMaxFetchDepth(2);
       List l = (List)query.execute(devid, timeout, curts);
       Iterator iter = l.iterator();
       while (iter.hasNext()) {
         MFileLocation mf = (MFileLocation)iter.next();
         mfl.add(mf);
       }
       commited = commitTransaction();
     } finally {
       if (!commited) {
         rollbackTransaction();
       }
     }
     return mfl;
   }
 
   private MTable getMTable(String db, String table) {
     MTable mtbl = null;
     boolean commited = false;
     try {
       openTransaction();
       db = db.toLowerCase().trim();
       table = table.toLowerCase().trim();
       Query query = pm.newQuery(MTable.class, "tableName == table && database.name == db");
       query.declareParameters("java.lang.String table, java.lang.String db");
       query.setUnique(true);
       mtbl = (MTable) query.execute(table, db);
       pm.retrieve(mtbl);
       commited = commitTransaction();
     } finally {
       if (!commited) {
         rollbackTransaction();
       }
     }
     return mtbl;
   }
 
   public List<Table> getTableObjectsByName(String db, List<String> tbl_names)
       throws MetaException, UnknownDBException {
     List<Table> tables = new ArrayList<Table>();
     boolean committed = false;
     try {
       openTransaction();
 
       db = db.toLowerCase().trim();
       Query dbExistsQuery = pm.newQuery(MDatabase.class, "name == db");
       dbExistsQuery.declareParameters("java.lang.String db");
       dbExistsQuery.setUnique(true);
       dbExistsQuery.setResult("name");
       String dbNameIfExists = (String) dbExistsQuery.execute(db);
       if (dbNameIfExists == null || dbNameIfExists.isEmpty()) {
         throw new UnknownDBException("Could not find database " + db);
       }
 
       List<String> lowered_tbl_names = new ArrayList<String>();
       for (String t : tbl_names) {
         lowered_tbl_names.add(t.toLowerCase().trim());
       }
       Query query = pm.newQuery(MTable.class);
       query.setFilter("database.name == db && tbl_names.contains(tableName)");
       query.declareParameters("java.lang.String db, java.util.Collection tbl_names");
       Collection mtables = (Collection) query.execute(db, lowered_tbl_names);
       for (Iterator iter = mtables.iterator(); iter.hasNext();) {
         tables.add(convertToTable((MTable) iter.next()));
       }
       committed = commitTransaction();
     } finally {
       if (!committed) {
         rollbackTransaction();
       }
     }
     return tables;
   }
 
   private Node convertToNode(MNode mn) {
     if (mn == null) {
       return null;
     }
     return new Node(mn.getNode_name(), mn.getIPList(), mn.getStatus());
   }
 
   private Set<Node> convertToNodeSet(Set<MNode> mnSet)  {
     if (mnSet == null) {
       return null;
     }
     Set<Node> nodes = new HashSet<Node>();
     for(MNode mn : mnSet){
       nodes.add(convertToNode(mn));
     }
     return nodes;
   }
 
   private Set<MNode> convertToMNodeSet(Set<Node> mnSet)  {
     if (mnSet == null) {
       return null;
     }
     Set<MNode> nodes = new HashSet<MNode>();
     for(Node mn : mnSet){
       nodes.add(convertToMNode(mn));
     }
     return nodes;
   }
 
   private Set<MNode> getMNodeSet(Set<Node> mnSet)  {
     if (mnSet == null) {
       return null;
     }
     Set<MNode> nodes = new HashSet<MNode>();
     for(Node mn : mnSet){
       nodes.add(getMNode(mn.getNode_name()));
     }
     return nodes;
   }
 
   private Device convertToDevice(MDevice md) throws MetaException {
     if (md == null) {
       return null;
     }
     return new Device(md.getDev_name(), md.getProp(), md.getNode() != null ? md.getNode().getNode_name() : null,
         md.getStatus());
   }
 
   private SFile convertToSFile(MFile mf) throws MetaException {
     if (mf == null) {
       return null;
     }
     String dbName = null;
     String tableName = null;
     if (mf.getTable() != null) {
       tableName = mf.getTable().getTableName();
       if (mf.getTable().getDatabase() != null) {
         dbName = mf.getTable().getDatabase().getName();
       }
     }
     List<SplitValue> values = new ArrayList<SplitValue>();
     if (mf.getValues() != null) {
       for (MSplitValue msv : mf.getValues()) {
         values.add(new SplitValue(msv.getPkname(), msv.getLevel(), msv.getValue(), msv.getVersion()));
       }
     }
     return new SFile(mf.getFid(), dbName, tableName, mf.getStore_status(), mf.getRep_nr(),
         mf.getDigest(), mf.getRecord_nr(), mf.getAll_record_nr(), null, mf.getLength(), mf.getRef_files(), values, mf.getLoad_status());
   }
 
   private List<SFileLocation> convertToSFileLocation(List<MFileLocation> mfl) throws MetaException {
     if (mfl == null) {
       return null;
     }
     List<SFileLocation> r = new ArrayList<SFileLocation>();
     for (MFileLocation mf : mfl) {
       r.add(new SFileLocation(mf.getDev().getNode().getNode_name(), mf.getFile().getFid(), mf.getDev().getDev_name(),
           mf.getLocation(), mf.getRep_id(), mf.getUpdate_time(), mf.getVisit_status(), mf.getDigest()));
     }
     return r;
   }
 
   private SFileLocation convertToSFileLocation(MFileLocation mfl) throws MetaException {
     if (mfl == null) {
       return null;
     }
     pm.retrieve(mfl);
     return new SFileLocation(mfl.getDev().getNode().getNode_name(), mfl.getFile().getFid(), mfl.getDev().getDev_name(),
         mfl.getLocation(), mfl.getRep_id(), mfl.getUpdate_time(), mfl.getVisit_status(), mfl.getDigest());
   }
 
   private MDatabase convertToMDatabase(Database db) throws MetaException {
     if (db == null) {
       return null;
     }
 
     return new MDatabase(db.getName(), db.getDescription(), db.getLocationUri(), db.getParameters());
   }
 
   private Database convertToDatabase(MDatabase mdb) throws MetaException {
     if (mdb == null) {
       return null;
     }
 
     return new Database(mdb.getName(), mdb.getDescription(), mdb.getLocationUri(), mdb.getParameters());
   }
 
   private Table convertToTable(MTable mtbl) throws MetaException {
     if (mtbl == null) {
       return null;
     }
     String tableType = mtbl.getTableType();
     if (tableType == null) {
       // for backwards compatibility with old metastore persistence
       if (mtbl.getViewOriginalText() != null) {
         tableType = TableType.VIRTUAL_VIEW.toString();
       } else if ("TRUE".equals(mtbl.getParameters().get("EXTERNAL"))) {
         tableType = TableType.EXTERNAL_TABLE.toString();
       } else {
         tableType = TableType.MANAGED_TABLE.toString();
       }
     }
     return new Table(mtbl.getTableName(), mtbl.getDatabase().getName(), mtbl.getSchema().getSchemaName(),mtbl
         .getOwner(), mtbl.getCreateTime(), mtbl.getLastAccessTime(), mtbl
         .getRetention(), convertToStorageDescriptor(mtbl.getSd()),
         convertToFieldSchemas(mtbl.getPartitionKeys()), mtbl.getParameters(),
         mtbl.getViewOriginalText(), mtbl.getViewExpandedText(),
         tableType, convertToNodeGroups(mtbl.getGroupDistribute()), convertToFieldSchemas(mtbl.getFileSplitKeys()));
   }
 
   private MNode convertToMNode(Node node) {
     if (node == null) {
       return null;
     }
 
     return new MNode(node.getNode_name().trim(), node.getIps(), node.getStatus());
   }
 
   private MDevice convertToMDevice(Device device) {
     if (device == null) {
       return null;
     }
     MNode mn = this.getMNode(device.getNode_name());
 
     return new MDevice(mn, device.getDevid(), device.getProp(), device.getStatus());
   }
 
   private MFile convertToMFile(SFile file) throws InvalidObjectException {
     MTable mt = null;
 
     if (file == null) {
       return null;
     }
     if (file.getDbName() != null && file.getTableName() != null) {
       mt = getMTable(file.getDbName(), file.getTableName());
       if (mt == null) {
         throw new InvalidObjectException("Invalid db or table name: db=" + file.getDbName() + ", table=" + file.getTableName());
       }
     }
     List<MSplitValue> values = new ArrayList<MSplitValue>();
     if (file.getValuesSize() > 0) {
       long version = -1;
 
       if (mt == null || mt.getFileSplitKeys() == null || mt.getFileSplitKeys().size() == 0) {
         throw new InvalidObjectException("This file does not belong to any TABLE or table contains NONE file split keys, you should not set SplitValues!");
       }
       for (SplitValue sv : file.getValues()) {
         values.add(new MSplitValue(sv.getSplitKeyName().toLowerCase(), sv.getLevel(), sv.getValue(), sv.getVerison()));
         if (version == -1) {
           version = sv.getVerison();
         }
       }
       //  we must check whether the values are valid.
       boolean ok = false;
 
       if (version != -1) {
         for (MFieldSchema mfs : mt.getFileSplitKeys()) {
           if (mfs.getVersion() == version) {
             ok = true;
           }
         }
       }
       if (!ok) {
         throw new InvalidObjectException("This file contains invalid SPLIT KEYS version: " + version);
       }
     }
 
     return new MFile(file.getFid(), mt, file.getStore_status(), file.getRep_nr(),
         file.getDigest(), file.getRecord_nr(), file.getAll_record_nr(), file.getLength(), file.getRef_files(), values);
   }
 
   private MFileLocation convertToMFileLocation(SFileLocation location) {
     if (location == null) {
       return null;
     }
 
     // get mfile
     MFile mf = getMFile(location.getFid());
     if (mf == null) {
       return null;
     }
 
     MDevice md = getMDevice(location.getDevid());
     if (md == null) {
       return null;
     }
 
     return new MFileLocation(mf, md, location.getLocation(),
         location.getRep_id(), System.currentTimeMillis(), location.getVisit_status(), location.getDigest());
   }
 
   private MTable convertToMTable(Table tbl) throws InvalidObjectException,
       MetaException {
     if (tbl == null) {
       return null;
     }
     MDatabase mdb = null;
     MSchema mSchema = null;
     try {
       mdb = getMDatabase(tbl.getDbName());
       mSchema = getMSchema(tbl.getSchemaName());
     } catch (NoSuchObjectException e) {
       LOG.error(StringUtils.stringifyException(e));
       throw new InvalidObjectException("Database " + tbl.getDbName()
           + " doesn't exist.");
     }
 
     // If the table has property EXTERNAL set, update table type
     // accordingly
     String tableType = tbl.getTableType();
     boolean isExternal = "TRUE".equals(tbl.getParameters().get("EXTERNAL"));
     if (TableType.MANAGED_TABLE.toString().equals(tableType)) {
       if (isExternal) {
         tableType = TableType.EXTERNAL_TABLE.toString();
       }
     }
     if (TableType.EXTERNAL_TABLE.toString().equals(tableType)) {
       if (!isExternal) {
         tableType = TableType.MANAGED_TABLE.toString();
       }
     }
 
     MTable mtbl =new MTable(tbl.getTableName().toLowerCase(), mdb,mSchema,
         convertToMStorageDescriptor(tbl.getSd()), tbl.getOwner(), tbl
             .getCreateTime(), tbl.getLastAccessTime(), tbl.getRetention(),
         convertToMFieldSchemas(tbl.getFileSplitKeys()),
         convertToMFieldSchemas(tbl.getPartitionKeys()), tbl.getParameters(),
         tbl.getViewOriginalText(), tbl.getViewExpandedText(),
         tableType);
 
     if(tbl.getFileSplitKeys() != null && !tbl.getFileSplitKeys().isEmpty()){
       mtbl.setFileSplitKeys(convertToMFieldSchemas(tbl.getFileSplitKeys()));
     }
 
     if(tbl.getNodeGroups() != null && !tbl.getNodeGroups().isEmpty()){
       HashSet<MNodeGroup> mngs = new HashSet<MNodeGroup>();
       for( NodeGroup ngGroup : tbl.getNodeGroups()){
         MNodeGroup mng;
         try {
           mng = getMNodeGroup(ngGroup.getNode_group_name());
         } catch (NoSuchObjectException e) {
           throw new InvalidObjectException("Node group ["+ngGroup.getNode_group_name()+"] is not valid.");
         }
         mngs.add(mng);
       }
       mtbl.setGroupDistribute(mngs);
     }
 
     // A new table is always created with a new column descriptor
     return mtbl;
   }
 
   private MSchema convertToMSchema(GlobalSchema schema) throws InvalidObjectException,
   MetaException {
     if (schema == null) {
       return null;
     }
 
     // If the table has property EXTERNAL set, update table type
     // accordingly
     String schemaType = schema.getSchemaType();
     boolean isExternal = "TRUE".equals(schema.getParameters().get("EXTERNAL"));
     if (TableType.MANAGED_TABLE.toString().equals(schemaType)) {
       if (isExternal) {
         schemaType = TableType.EXTERNAL_TABLE.toString();
       }
     }
     if (TableType.EXTERNAL_TABLE.toString().equals(schemaType)) {
       if (!isExternal) {
         schemaType = TableType.MANAGED_TABLE.toString();
       }
     }
 
     // A new table is always created with a new column descriptor
     return new MSchema(schema.getSchemaName().toLowerCase(),
         convertToMStorageDescriptor(schema.getSd()), schema.getCreateTime(),
             schema.getLastAccessTime(), schema.getRetention(), schema.getParameters(),
         schema.getViewOriginalText(), schema.getViewExpandedText(),
         schemaType);
   }
 
   private List<MFieldSchema> convertToMFieldSchemas(List<FieldSchema> keys) {
     List<MFieldSchema> mkeys = null;
     if (keys != null) {
       mkeys = new ArrayList<MFieldSchema>(keys.size());
       for (FieldSchema part : keys) {
         // FIXME: set version to ZERO initially
         mkeys.add(new MFieldSchema(part.getName().toLowerCase(),
             part.getType(), part.getComment(), 0));
       }
     }
     return mkeys;
   }
 
   private FieldSchema convertToFieldSchema(MFieldSchema mkey) {
     FieldSchema key = null;
     if (key != null) {
       key = new FieldSchema(mkey.getName().toLowerCase(),
           mkey.getType(), mkey.getComment());
       key.setVersion(mkey.getVersion());
     }
     return key;
   }
 
   private List<FieldSchema> convertToFieldSchemas(List<MFieldSchema> mkeys) {
     List<FieldSchema> keys = null;
     if (mkeys != null) {
       keys = new ArrayList<FieldSchema>(mkeys.size());
       for (MFieldSchema part : mkeys) {
         FieldSchema fs = new FieldSchema(part.getName(), part.getType(), part
             .getComment());
         fs.setVersion(part.getVersion());
         keys.add(fs);
       }
     }
     return keys;
   }
 
   private List<MOrder> convertToMOrders(List<Order> keys) {
     List<MOrder> mkeys = null;
     if (keys != null) {
       mkeys = new ArrayList<MOrder>(keys.size());
       for (Order part : keys) {
         mkeys.add(new MOrder(part.getCol().toLowerCase(), part.getOrder()));
       }
     }
     return mkeys;
   }
 
   private List<Order> convertToOrders(List<MOrder> mkeys) {
     List<Order> keys = null;
     if (mkeys != null) {
       keys = new ArrayList<Order>(mkeys.size());
       for (MOrder part : mkeys) {
         keys.add(new Order(part.getCol(), part.getOrder()));
       }
     }
     return keys;
   }
 
   private SerDeInfo converToSerDeInfo(MSerDeInfo ms) throws MetaException {
     if (ms == null) {
       throw new MetaException("Invalid SerDeInfo object");
     }
     return new SerDeInfo(ms.getName(), ms.getSerializationLib(), ms
         .getParameters());
   }
 
   private MSerDeInfo converToMSerDeInfo(SerDeInfo ms) throws MetaException {
     if (ms == null) {
       throw new MetaException("Invalid SerDeInfo object");
     }
     return new MSerDeInfo(ms.getName(), ms.getSerializationLib(), ms
         .getParameters());
   }
 
   /**
    * Given a list of model field schemas, create a new model column descriptor.
    * @param cols the columns the column descriptor contains
    * @return a new column descriptor db-backed object
    */
   private MColumnDescriptor createNewMColumnDescriptor(List<MFieldSchema> cols) {
     if (cols == null) {
       return null;
     }
     return new MColumnDescriptor(cols);
   }
 
   // MSD and SD should be same objects. Not sure how to make then same right now
   // MSerdeInfo *& SerdeInfo should be same as well
   private StorageDescriptor convertToStorageDescriptor(MStorageDescriptor msd,
       boolean noFS)
       throws MetaException {
     if (msd == null) {
       return null;
     }
     List<MFieldSchema> mFieldSchemas = msd.getCD() == null ? null : msd.getCD().getCols();
 
     LOG.debug("mFieldSchemas.size():"+mFieldSchemas.size());
     StorageDescriptor sd = new StorageDescriptor(noFS ? null : convertToFieldSchemas(mFieldSchemas),
         msd.getLocation(), msd.getInputFormat(), msd.getOutputFormat(), msd
         .isCompressed(), msd.getNumBuckets(), converToSerDeInfo(msd
         .getSerDeInfo()), msd.getBucketCols(), convertToOrders(msd
         .getSortCols()), msd.getParameters());
     SkewedInfo skewedInfo = new SkewedInfo(msd.getSkewedColNames(),
         convertToSkewedValues(msd.getSkewedColValues()),
         covertToSkewedMap(msd.getSkewedColValueLocationMaps()));
     sd.setSkewedInfo(skewedInfo);
     sd.setStoredAsSubDirectories(msd.isStoredAsSubDirectories());
     return sd;
   }
 
   private StorageDescriptor convertToStorageDescriptor(MStorageDescriptor msd)
       throws MetaException {
     return convertToStorageDescriptor(msd, false);
   }
 
   /**
    * Convert a list of MStringList to a list of list string
    *
    * @param mLists
    * @return
    */
   private List<List<String>> convertToSkewedValues(List<MStringList> mLists) {
     List<List<String>> lists = null;
     if (mLists != null) {
       lists = new ArrayList<List<String>>(mLists.size());
       for (MStringList element : mLists) {
         lists.add(new ArrayList<String>(element.getInternalList()));
       }
     }
     return lists;
   }
 
   private List<MStringList> convertToMStringLists(List<List<String>> mLists) {
     List<MStringList> lists = null ;
     if (null != mLists) {
       lists = new ArrayList<MStringList>();
       for (List<String> mList : mLists) {
         lists.add(new MStringList(mList));
       }
     }
     return lists;
   }
 
   /**
    * Convert a MStringList Map to a Map
    * @param mMap
    * @return
    */
   private Map<List<String>, String> covertToSkewedMap(Map<MStringList, String> mMap) {
     Map<List<String>, String> map = null;
     if (mMap != null) {
       map = new HashMap<List<String>, String>(mMap.size());
       Set<MStringList> keys = mMap.keySet();
       for (MStringList key : keys) {
         map.put(new ArrayList<String>(key.getInternalList()), mMap.get(key));
       }
     }
     return map;
   }
 
   /**
    * Covert a Map to a MStringList Map
    * @param mMap
    * @return
    */
   private Map<MStringList, String> covertToMapMStringList(Map<List<String>, String> mMap) {
     Map<MStringList, String> map = null;
     if (mMap != null) {
       map = new HashMap<MStringList, String>(mMap.size());
       Set<List<String>> keys = mMap.keySet();
       for (List<String> key : keys) {
         map.put(new MStringList(key), mMap.get(key));
       }
     }
     return map;
   }
 
 
 
   /**
    * Converts a storage descriptor to a db-backed storage descriptor.  Creates a
    *   new db-backed column descriptor object for this SD.
    * @param sd the storage descriptor to wrap in a db-backed object
    * @return the storage descriptor db-backed object
    * @throws MetaException
    */
   private MStorageDescriptor convertToMStorageDescriptor(StorageDescriptor sd)
       throws MetaException {
     if (sd == null) {
       return null;
     }
     MColumnDescriptor mcd = createNewMColumnDescriptor(convertToMFieldSchemas(sd.getCols()));
     return convertToMStorageDescriptor(sd, mcd);
   }
 
   /**
    * Converts a storage descriptor to a db-backed storage descriptor.  It points the
    * storage descriptor's column descriptor to the one passed as an argument,
    * so it does not create a new mcolumn descriptor object.
    * @param sd the storage descriptor to wrap in a db-backed object
    * @param mcd the db-backed column descriptor
    * @return the db-backed storage descriptor object
    * @throws MetaException
    */
   private MStorageDescriptor convertToMStorageDescriptor(StorageDescriptor sd,
       MColumnDescriptor mcd) throws MetaException {
     if (sd == null) {
       return null;
     }
     return new MStorageDescriptor(mcd, sd
         .getLocation(), sd.getInputFormat(), sd.getOutputFormat(), sd
         .isCompressed(), sd.getNumBuckets(), converToMSerDeInfo(sd
         .getSerdeInfo()), sd.getBucketCols(),
         convertToMOrders(sd.getSortCols()), sd.getParameters(),
         (null == sd.getSkewedInfo()) ? null
             : sd.getSkewedInfo().getSkewedColNames(),
         convertToMStringLists((null == sd.getSkewedInfo()) ? null : sd.getSkewedInfo()
             .getSkewedColValues()),
         covertToMapMStringList((null == sd.getSkewedInfo()) ? null : sd.getSkewedInfo()
             .getSkewedColValueLocationMaps()), sd.isStoredAsSubDirectories());
   }
 
   public boolean addPartition(Partition part) throws InvalidObjectException,
       MetaException {
     boolean success = false;
     boolean commited = false;
     try {
       MTable table = this.getMTable(part.getDbName(), part.getTableName());
       List<MTablePrivilege> tabGrants = null;
       List<MTableColumnPrivilege> tabColumnGrants = null;
       if ("TRUE".equalsIgnoreCase(table.getParameters().get("PARTITION_LEVEL_PRIVILEGE"))) {
         tabGrants = this.listAllTableGrants(part
             .getDbName(), part.getTableName());
         tabColumnGrants = this.listTableAllColumnGrants(
             part.getDbName(), part.getTableName());
       }
       openTransaction();
       MPartition mpart = convertToMPart(part, true);
       LOG.info("---zjw--in add partition:"+mpart.getPartitionName()+"--sub:"+mpart.getSubPartitions().size());
 
 //      for (MTableColumnPrivilege col : tabColumnGrants) {
 //        pm.makePersistentAll(mpart.getSubPartitions());
 //      }
 
       pm.makePersistent(mpart);
 //      pm.makePersistentAll(mpart.getSubPartitions());
 
       int now = (int)(System.currentTimeMillis()/1000);
       List<Object> toPersist = new ArrayList<Object>();
       if (tabGrants != null) {
         for (MTablePrivilege tab: tabGrants) {
           MPartitionPrivilege partGrant = new MPartitionPrivilege(tab
               .getPrincipalName(), tab.getPrincipalType(),
               mpart, tab.getPrivilege(), now, tab.getGrantor(), tab
                   .getGrantorType(), tab.getGrantOption());
           toPersist.add(partGrant);
         }
       }
 
       if (tabColumnGrants != null) {
         for (MTableColumnPrivilege col : tabColumnGrants) {
           MPartitionColumnPrivilege partColumn = new MPartitionColumnPrivilege(col
               .getPrincipalName(), col.getPrincipalType(), mpart, col
               .getColumnName(), col.getPrivilege(), now, col.getGrantor(), col
               .getGrantorType(), col.getGrantOption());
           toPersist.add(partColumn);
         }
 
         if (toPersist.size() > 0) {
           pm.makePersistentAll(toPersist);
         }
       }
 
       pm.retrieve(mpart.getTable());
       commited = commitTransaction();
       long db_id = Long.parseLong(MSGFactory.getIDFromJdoObjectId(pm.getObjectId(table.getDatabase()).toString()));
       HashMap<String, Object> params = new HashMap<String, Object>();
       params.put("db_name", mpart.getTable().getDatabase().getName());
       params.put("table_name", mpart.getTable().getTableName());
       params.put("partition_name", mpart.getPartitionName());
       if(commited) {
         MetaMsgServer.sendMsg(MSGFactory.generateDDLMsg(MSGType.MSG_NEW_PARTITION,db_id,-1,
             pm,mpart,params));
       }
 
       /*****************NOTE oracle does not commit here.*****************/
 //      pm.flush();//
       success = true;
     } finally {
       if (!commited) {
         LOG.info("---zjw--in add partition,roll back");
         rollbackTransaction();
       }else{
         LOG.info("---zjw--in add partition,commit success");
       }
     }
     return success;
   }
 
   public Partition getPartition(String dbName, String tableName,
       String part_name) throws NoSuchObjectException, MetaException {
     openTransaction();
     Partition part = convertToPart(getMPartition(dbName, tableName, part_name));
     commitTransaction();
     if(part == null) {
       throw new NoSuchObjectException("partition values=" + part_name);
     }
     return part;
   }
 
   public Partition getParentPartition(String dbName, String tableName, String subpart_name) throws NoSuchObjectException, MetaException {
     Partition pp = null;
     openTransaction();
     MPartition subp = getMPartition(dbName, tableName, subpart_name);
     if (subp == null) {
       throw new NoSuchObjectException("partition values=" + subpart_name);
     }
     pp = convertToPart(subp.getParent());
     commitTransaction();
 
     return pp;
   }
 
   public Subpartition getSubpartition(String dbName, String tableName,
       String part_name) throws NoSuchObjectException, MetaException {
     openTransaction();
     Subpartition part = convertToSubpart(getMPartition(dbName, tableName, part_name));
     commitTransaction();
     if(part == null) {
       throw new NoSuchObjectException("partition values=" + part_name);
     }
     return part;
   }
 
   public Partition getPartition(String dbName, String tableName,
       List<String> part_vals) throws NoSuchObjectException, MetaException {
     openTransaction();
     Partition part = convertToPart(getMPartition(dbName, tableName, part_vals));
     commitTransaction();
     if(part == null) {
       throw new NoSuchObjectException("partition values="
           + part_vals.toString());
     }
     part.setValues(part_vals);
     return part;
   }
 
   private MPartition getMPartition(String dbName, String tableName, String partName) throws MetaException {
     MPartition mpart = null;
     boolean commited = false;
     try {
       openTransaction();
       dbName = dbName.toLowerCase().trim();
       tableName = tableName.toLowerCase().trim();
       MTable mtbl = getMTable(dbName, tableName);
       if (mtbl == null) {
         commited = commitTransaction();
         return null;
       }
       // Change the query to use part_vals instead of the name which is
       // redundant
       Query query = pm.newQuery(MPartition.class,
           "table.tableName == t1 && table.database.name == t2 && partitionName == t3");
       query.declareParameters("java.lang.String t1, java.lang.String t2, java.lang.String t3");
       query.setUnique(true);
       mpart = (MPartition) query.execute(tableName, dbName, partName);
       pm.retrieve(mpart);
       commited = commitTransaction();
     } finally {
       if (!commited) {
         rollbackTransaction();
       }
     }
     return mpart;
   }
 
   /**
    * Convert a Partition object into an MPartition, which is an object backed by the db
    * If the Partition's set of columns is the same as the parent table's AND useTableCD
    * is true, then this partition's storage descriptor's column descriptor will point
    * to the same one as the table's storage descriptor.
    * @param part the partition to convert
    * @param useTableCD whether to try to use the parent table's column descriptor.
    * @return the model partition object
    * @throws InvalidObjectException
    * @throws MetaException
    */
   private MPartition convertToMPart(Partition part, boolean useTableCD)
       throws InvalidObjectException, MetaException {
     if (part == null) {
       return null;
     }
     MTable mt = getMTable(part.getDbName(), part.getTableName());
     if (mt == null) {
       throw new InvalidObjectException(
           "Partition doesn't have a valid table or database name");
     }
 
     // If this partition's set of columns is the same as the parent table's,
     // use the parent table's, so we do not create a duplicate column descriptor,
     // thereby saving space
     MStorageDescriptor msd;
     if (useTableCD &&
         mt.getSd() != null && mt.getSd().getCD() != null &&
         mt.getSd().getCD().getCols() != null &&
         part.getSd() != null &&
         convertToFieldSchemas(mt.getSd().getCD().getCols()).
         equals(part.getSd().getCols())) {
       msd = convertToMStorageDescriptor(part.getSd(), mt.getSd().getCD());
     } else {
       msd = convertToMStorageDescriptor(part.getSd());
     }
 
 //    return new MPartition(Warehouse.makePartName(convertToFieldSchemas(mt
 //        .getPartitionKeys()), part.getValues()), mt, part.getValues(), part
 //        .getCreateTime(), part.getLastAccessTime(),
 //        msd, part.getParameters());
     MPartition mpart = new MPartition(part.getPartitionName(), mt, null,part.getValues(), part.getFiles(), part
         .getCreateTime(), part.getLastAccessTime(),
         msd, part.getParameters());
     mpart.setPartition_level(1);
 
     if(part.getSubpartitions() != null){
       LOG.warn("--zjw--getSubPartitions is not null,size"+part.getSubpartitions().size());
       for(Subpartition sub : part.getSubpartitions()){
         sub.setDbName(mt.getDatabase().getName());
         MPartition sub_part = convertToMPartFromSubpartition(sub,useTableCD);
         sub_part.setParent(mpart);
         mpart.getSubPartitions().add(sub_part);
       }
     }else{
       LOG.warn("--zjw--getSubPartitions is  null");
     }
 
     return mpart;
   }
 
   private MPartition convertToMPartFromSubpartition(Subpartition part, boolean useTableCD)
       throws InvalidObjectException, MetaException {
     if (part == null) {
       return null;
     }
     MTable mt = getMTable(part.getDbName(), part.getTableName());
     if (mt == null) {
       throw new InvalidObjectException(
           "Partition doesn't have a valid table or database name");
     }
 
     // If this partition's set of columns is the same as the parent table's,
     // use the parent table's, so we do not create a duplicate column descriptor,
     // thereby saving space
     MStorageDescriptor msd;
     if (useTableCD &&
         mt.getSd() != null && mt.getSd().getCD() != null &&
         mt.getSd().getCD().getCols() != null &&
         part.getSd() != null &&
         convertToFieldSchemas(mt.getSd().getCD().getCols()).
         equals(part.getSd().getCols())) {
       msd = convertToMStorageDescriptor(part.getSd(), mt.getSd().getCD());
     } else {
       msd = convertToMStorageDescriptor(part.getSd());
     }
 
 //    return new MPartition(Warehouse.makePartName(convertToFieldSchemas(mt
 //        .getPartitionKeys()), part.getValues()), mt, part.getValues(), part
 //        .getCreateTime(), part.getLastAccessTime(),
 //        msd, part.getParameters());
     MPartition mpart = new MPartition(part.getPartitionName(), mt,null, part.getValues(), part.getFiles(), part
         .getCreateTime(), part.getLastAccessTime(),
         msd, part.getParameters());
     mpart.setPartition_level(2);
     return mpart;
   }
 
   private List<MPartition> convertToMParts(List<Partition> parts, boolean useTableCD, String dbName)
       throws InvalidObjectException, MetaException {
     if(parts == null) {
       return null;
     }
     List<MPartition> mparts = new ArrayList<MPartition>();
     for(Partition p :parts){
       p.setDbName(dbName);
       MPartition part = this.convertToMPart(p,useTableCD);
       mparts.add(part);
       if(p.getSubpartitions() != null){
         LOG.warn("--zjw--getSubPartitions is not null,size"+p.getSubpartitions().size());
         for(Subpartition sub : p.getSubpartitions()){
           sub.setDbName(dbName);
           MPartition sub_part = convertToMPartFromSubpartition(sub,useTableCD);
           sub_part.setParent(part);
           part.getSubPartitions().add(sub_part);
         }
       }else{
         LOG.warn("--zjw--getSubPartitions is  null");
       }
 
     }
     return mparts;
   }
 
   private Partition convertToPart(MPartition mpart) throws MetaException {
     List<Subpartition> sub_parts = null;
     if (mpart == null) {
       return null;
     }else{
       if(mpart.getSubPartitions() != null){
         sub_parts = new ArrayList<Subpartition>();
         LOG.warn("--zjw--getMSubPartitions is not null,size"+mpart.getSubPartitions().size());
         for(MPartition msub : mpart.getSubPartitions()){
 
           Subpartition sub_part = convertToSubpart(msub);
           //FIX this ,thrift do not support recursive definition,neither inter-reference,so parent partition cannot be defined.
 //          sub_part.setParent(part);
           sub_parts.add(sub_part);
         }
       }
     }
     Partition p = new Partition(mpart.getValues(), mpart.getTable().getDatabase()
         .getName(), mpart.getTable().getTableName(), mpart.getCreateTime(),
         mpart.getLastAccessTime(), convertToStorageDescriptor(mpart.getSd()),
         mpart.getParameters(), mpart.getFiles());
     p.setPartitionName(mpart.getPartitionName());
     p.setSubpartitions(sub_parts);
     return p;
   }
 
   private Subpartition convertToSubpart(MPartition mpart) throws MetaException {
     if (mpart == null) {
       return null;
     }
     Subpartition p = new Subpartition(mpart.getValues(), mpart.getTable().getDatabase()
         .getName(), mpart.getTable().getTableName(), mpart.getCreateTime(),
         mpart.getLastAccessTime(), convertToStorageDescriptor(mpart.getSd()),
         mpart.getParameters(), mpart.getFiles());
     p.setPartitionName(mpart.getPartitionName());
     return p;
   }
 
   private Partition convertToPart(String dbName, String tblName, MPartition mpart)
       throws MetaException {
     if (mpart == null) {
       return null;
     }
 
     Partition part = new Partition(mpart.getValues(), dbName, tblName, mpart.getCreateTime(),
         mpart.getLastAccessTime(), convertToStorageDescriptor(mpart.getSd(), true),
         mpart.getParameters(), mpart.getFiles());
 
     part.setPartitionName(mpart.getPartitionName());
 
     List<Subpartition> sub_parts = null;
     if(mpart.getSubPartitions() != null){
       sub_parts = new ArrayList<Subpartition>();
       LOG.warn("--zjw--getMSubPartitions is not null,size"+mpart.getSubPartitions().size());
       for(MPartition msub : mpart.getSubPartitions()){
 
         Subpartition sub_part = convertToSubpart(msub);
         //FIX this ,thrift do not support recursive definition,neither inter-reference,so parent partition cannot be defined.
 //        sub_part.setParent(part);
         sub_parts.add(sub_part);
       }
     }else{
       LOG.warn("--zjw--getSubPartitions is  null");
     }
 
     part.setSubpartitions(sub_parts);
 
     return part;
   }
 
   @Override
   public boolean dropPartition(String dbName, String tableName,
     List<String> part_vals) throws MetaException, NoSuchObjectException, InvalidObjectException,
     InvalidInputException {
     boolean success = false;
     try {
       LOG.debug("--zjw--before 4  ");
       openTransaction();
       // TODO: fix it
       MPartition part = getMPartition(dbName, tableName, part_vals);
       dropPartitionCommon(part);
       LOG.debug("--zjw--after 4  ");
       success = commitTransaction();
     } finally {
       if (!success) {
         rollbackTransaction();
       }
     }
     return success;
   }
 
   @Override
   public boolean dropPartition(String dbName, String tableName,
     String part_name) throws MetaException, NoSuchObjectException, InvalidObjectException,
     InvalidInputException {
     boolean success = false;
     try {
       LOG.debug("--zjw--before dropPartition open ");
       openTransaction();
       // TODO: fix it
       MPartition part = getMPartition(dbName, tableName, part_name);
       dropPartitionCommon(part);
       LOG.debug("--zjw--before dropPartition cmt ");
       success = commitTransaction();
       LOG.debug("--zjw--after dropPartition cmt ");
     } finally {
       if (!success) {
         rollbackTransaction();
       }
     }
     return success;
   }
 
   /**
    * Drop an MPartition and cascade deletes (e.g., delete partition privilege grants,
    *   drop the storage descriptor cleanly, etc.)
    * @param part - the MPartition to drop
    * @return whether the transaction committed successfully
    * @throws InvalidInputException
    * @throws InvalidObjectException
    * @throws MetaException
    * @throws NoSuchObjectException
    */
   private boolean dropPartitionCommon(MPartition part) throws NoSuchObjectException, MetaException,
     InvalidObjectException, InvalidInputException {
     boolean success = false;
     try {
       if(part == null) {
         return true;
       }
       openTransaction();
       if (part != null) {
 //        List<MFieldSchema> schemas = part.getTable().getPartitionKeys();
 //        List<String> colNames = new ArrayList<String>();
 //        for (MFieldSchema col: schemas) {
 //          colNames.add(col.getName());
 //        }
 //        String partName = FileUtils.makePartName(colNames, part.getValues());
 
         String partName = part.getPartitionName();
         LOG.debug("--zjw--getPartitionName is  "+part.getPartitionName());
         LOG.debug("--zjw--getSd is  "+part.getSd());
         LOG.debug("--zjw--getTableName is  "+part.getTable().getTableName());
         List<MPartitionPrivilege> partGrants = listPartitionGrants(
             part.getTable().getDatabase().getName(),
             part.getTable().getTableName(),
             partName);
 
         if (partGrants != null && partGrants.size() > 0) {
           pm.deletePersistentAll(partGrants);
         }
 
         List<MPartitionColumnPrivilege> partColumnGrants = listPartitionAllColumnGrants(
             part.getTable().getDatabase().getName(),
             part.getTable().getTableName(),
             partName);
         LOG.debug("--zjw--getTableName 111  ");
         if (partColumnGrants != null && partColumnGrants.size() > 0) {
           pm.deletePersistentAll(partColumnGrants);
         }
 
         /**
          * WARNING: check weather subpartitions need to delete grant tables recursivly !!!
          */
         if(part.getSubPartitions() != null){
           LOG.debug("--zjw--getSubPartitions not null  ");
           pm.deletePersistentAll(part.getSubPartitions());
         }else{
           LOG.debug("--zjw--getTableName null  ");
         }
 
         String dbName = part.getTable().getDatabase().getName();
         String tableName = part.getTable().getTableName();
 
         // delete partition level column stats if it exists
 
         /**
          *  FATAL ERROR HERE which can cause JDO ERROR,delete by zjw
          */
 //       try {
 //          deletePartitionColumnStatistics(dbName, tableName, partName, part.getValues(), null);
 //        } catch (NoSuchObjectException e) {
 //          LOG.info("No column statistics records found to delete");
 //        } catch (Exception e){
 //          LOG.error(e,e);
 //        }
 
         preDropStorageDescriptor(part.getSd());
         pm.deletePersistent(part);
         LOG.debug("--zjw--after dropPartitioncommon cmt ");
       }
 
       long db_id = Long.parseLong(MSGFactory.getIDFromJdoObjectId(pm.getObjectId(part.getTable().getDatabase()).toString()));
       if(success) {
         MetaMsgServer.sendMsg(MSGFactory.generateDDLMsg(MSGType.MSG_DEL_PARTITION,db_id,-1,pm,part,null));
       }
       success = commitTransaction();
     } finally {
       if (!success) {
         rollbackTransaction();
       }
     }
     return success;
   }
 
   public List<Partition> getPartitions(String dbName, String tableName, int max)
       throws MetaException {
     openTransaction();
     List<Partition> parts = convertToParts(listMPartitions(dbName, tableName, max));
     commitTransaction();
     return parts;
   }
 
   @Override
   public List<Partition> getPartitionsWithAuth(String dbName, String tblName,
       short max, String userName, List<String> groupNames)
       throws MetaException, NoSuchObjectException, InvalidObjectException {
     boolean success = false;
     try {
       openTransaction();
       List<MPartition> mparts = listMPartitions(dbName, tblName, max);
       List<Partition> parts = new ArrayList<Partition>(mparts.size());
       if (mparts != null && mparts.size()>0) {
         for (MPartition mpart : mparts) {
           MTable mtbl = mpart.getTable();
           Partition part = convertToPart(mpart);
           parts.add(part);
 
           if ("TRUE".equalsIgnoreCase(mtbl.getParameters().get("PARTITION_LEVEL_PRIVILEGE"))) {
             String partName = Warehouse.makePartName(this.convertToFieldSchemas(mtbl
                 .getPartitionKeys()), part.getValues());
             PrincipalPrivilegeSet partAuth = this.getPartitionPrivilegeSet(dbName,
                 tblName, partName, userName, groupNames);
             part.setPrivileges(partAuth);
           }
         }
       }
       success =  commitTransaction();
       return parts;
     } finally {
       if (!success) {
         rollbackTransaction();
       }
     }
   }
 
   /**
    * added by zjw ,for partition value specification getpartition
    * @param dbName
    * @param tableName
    * @param partVals
    * @return
    * @throws MetaException
    */
   private MPartition getMPartition(String dbName, String tableName, List<String> partVals) throws MetaException {
     boolean success = false;
     List<MPartition> mparts = null;
     try {
       openTransaction();
       LOG.debug("Executing listMPartitions");
       dbName = dbName.toLowerCase().trim();
       tableName = tableName.toLowerCase().trim();
 
       StringBuilder sb = new StringBuilder(
           "table.tableName == t1 && table.database.name == t2 && (");
       int n = 0;
       Map<String, String> params = new HashMap<String, String>();
       for (Iterator<String> itr = partVals.iterator(); itr.hasNext();) {
         String vn = "v" + n;
         n++;
         String part = itr.next();
         params.put(vn, part);
         sb.append("values.contains(").append(vn).append(")");
         sb.append(" || ");
       }
       sb.setLength(sb.length() - 4); // remove the last " || "
       sb.append(')');
 
       Query query = pm.newQuery(MPartition.class, sb.toString());
 
       LOG.debug(" JDOQL filter is " + sb.toString());
 
       params.put("t1", tableName.trim());
       params.put("t2", dbName.trim());
 
       String parameterDeclaration = makeParameterDeclarationString(params);
       query.declareParameters(parameterDeclaration);
       query.setOrdering("partitionName ascending");
 
 
       mparts = (List<MPartition>) query.executeWithMap(params);
       LOG.debug("Done executing query for listMPartitions");
       pm.retrieveAll(mparts);
       success = commitTransaction();
       LOG.debug("Done retrieving all objects for listMPartitions");
     } finally {
       if (!success) {
         rollbackTransaction();
       }
     }
     return mparts.get(0);
   }
 
   @Override
   public Partition getPartitionWithAuth(String dbName, String tblName,
       List<String> partVals, String user_name, List<String> group_names)
       throws NoSuchObjectException, MetaException, InvalidObjectException {
     boolean success = false;
     try {
       openTransaction();
       // TODO: fix it
       MPartition mpart = getMPartition(dbName, tblName, partVals);//modified by zjw
       if (mpart == null) {
         commitTransaction();
         throw new NoSuchObjectException("partition values="
             + partVals.toString());
       }
       Partition part = null;
       MTable mtbl = mpart.getTable();
       part = convertToPart(mpart);
       if ("TRUE".equalsIgnoreCase(mtbl.getParameters().get("PARTITION_LEVEL_PRIVILEGE"))) {
         String partName = Warehouse.makePartName(this.convertToFieldSchemas(mtbl
             .getPartitionKeys()), partVals);
         PrincipalPrivilegeSet partAuth = this.getPartitionPrivilegeSet(dbName,
             tblName, partName, user_name, group_names);
         part.setPrivileges(partAuth);
       }
 
       success = commitTransaction();
       return part;
     }catch(Exception e){
       LOG.error(e,e);
       throw new MetaException(e.getMessage());
     } finally {
       if (!success) {
         rollbackTransaction();
       }
     }
   }
 
   /**
    * @deprecated
    * @param mparts
    * @throws MetaException
    */
   @Deprecated
   private void loadSubpartitions(List<MPartition> mparts)
       throws MetaException {
 
     openTransaction();
     for (MPartition mp : mparts) {
       if(mp.getTable().getPartitionKeys().size() != 2){
         continue;
       }
       StringBuilder sb = new StringBuilder(
           "table.tableName == t1 && table.database.name == t2 && parent.partitionName == t3");
       int n = 0;
 
       Query query = pm.newQuery(MPartition.class, sb.toString());
       query.declareParameters("java.lang.String t1, java.lang.String t2, java.lang.String t3");
       query.setOrdering("partitionName ascending");
       LOG.debug(" JDOQL filter is " + sb.toString());
 
       Collection names = (Collection) query.execute(mp.getTable().getTableName(),mp.getTable().getDatabase().getName(),mp.getPartitionName());
       for (Iterator i = names.iterator(); i.hasNext();) {
         MPartition sub = (MPartition) i.next();
         LOG.debug("---zjw-- getParent is"+sub.getParent().getPartitionName()+"--"+sub.getParent().toString()+"--"+sub.getParent().getSubPartitions().size());
         mp.getSubPartitions().add(sub);
       }
       LOG.debug("---zjw-- getSubPartitions size  is " + mp.getSubPartitions().size());
     }
     commitTransaction();
   }
 
 
   private List<Partition> convertToParts(List<MPartition> mparts)
       throws MetaException {
     List<Partition> parts = new ArrayList<Partition>(mparts.size());
     for (MPartition mp : mparts) {
       parts.add(convertToPart(mp));
     }
     return parts;
   }
 
   private List<Partition> convertToParts(String dbName, String tblName, List<MPartition> mparts)
       throws MetaException {
     List<Partition> parts = new ArrayList<Partition>(mparts.size());
     for (MPartition mp : mparts) {
       parts.add(convertToPart(dbName, tblName, mp));
     }
     return parts;
   }
 
   // TODO:pc implement max
   public List<String> listPartitionNames(String dbName, String tableName,
       short max) throws MetaException {
     List<String> pns = new ArrayList<String>();
     boolean success = false;
     try {
       openTransaction();
       LOG.debug("Executing getPartitionNames");
       dbName = dbName.toLowerCase().trim();
       tableName = tableName.toLowerCase().trim();
       Query q = pm.newQuery(
           "select partitionName from org.apache.hadoop.hive.metastore.model.MPartition "
           + "where table.database.name == t1 && table.tableName == t2  && parent == null "
           + "order by partitionName asc");
       q.declareParameters("java.lang.String t1, java.lang.String t2");
       q.setResult("partitionName");
 
       if(max > 0) {
         q.setRange(0, max);
       }
       Collection names = (Collection) q.execute(dbName, tableName);
       for (Iterator i = names.iterator(); i.hasNext();) {
         pns.add((String) i.next());
       }
       success = commitTransaction();
     } finally {
       if (!success) {
         rollbackTransaction();
       }
     }
     return pns;
   }
 
   /**
    * Retrieves a Collection of partition-related results from the database that match
    *  the partial specification given for a specific table.
    * @param dbName the name of the database
    * @param tableName the name of the table
    * @param part_vals the partial specification values
    * @param max_parts the maximum number of partitions to return
    * @param resultsCol the metadata column of the data to return, e.g. partitionName, etc.
    *        if resultsCol is empty or null, a collection of MPartition objects is returned
    * @throws NoSuchObjectException
    * @results A Collection of partition-related items from the db that match the partial spec
    *          for a table.  The type of each item in the collection corresponds to the column
    *          you want results for.  E.g., if resultsCol is partitionName, the Collection
    *          has types of String, and if resultsCol is null, the types are MPartition.
    */
   private Collection getPartitionPsQueryResults(String dbName, String tableName,
       List<String> part_vals, short max_parts, String resultsCol)
       throws MetaException, NoSuchObjectException {
     dbName = dbName.toLowerCase().trim();
     tableName = tableName.toLowerCase().trim();
     Table table = getTable(dbName, tableName);
 
     if (table == null) {
       throw new NoSuchObjectException(dbName + "." + tableName + " table not found");
     }
 
     List<FieldSchema> partCols = table.getPartitionKeys();
     int numPartKeys = partCols.size();
     if (part_vals.size() > numPartKeys) {
       throw new MetaException("Incorrect number of partition values");
     }
 
     partCols = partCols.subList(0, part_vals.size());
     //Construct a pattern of the form: partKey=partVal/partKey2=partVal2/...
     // where partVal is either the escaped partition value given as input,
     // or a regex of the form ".*"
     //This works because the "=" and "/" separating key names and partition key/values
     // are not escaped.
     String partNameMatcher = Warehouse.makePartName(partCols, part_vals, ".*");
     //add ".*" to the regex to match anything else afterwards the partial spec.
     if (part_vals.size() < numPartKeys) {
       partNameMatcher += ".*";
     }
 
     Query q = pm.newQuery(MPartition.class);
     StringBuilder queryFilter = new StringBuilder("table.database.name == dbName");
     queryFilter.append(" && table.tableName == tableName");
     queryFilter.append(" && partitionName.matches(partialRegex)");
     q.setFilter(queryFilter.toString());
     q.declareParameters("java.lang.String dbName, " +
         "java.lang.String tableName, java.lang.String partialRegex");
 
     if( max_parts >= 0 ) {
       //User specified a row limit, set it on the Query
       q.setRange(0, max_parts);
     }
     if (resultsCol != null && !resultsCol.isEmpty()) {
       q.setResult(resultsCol);
     }
 
     return (Collection) q.execute(dbName, tableName, partNameMatcher);
   }
 
   @Override
   public List<Partition> listPartitionsPsWithAuth(String db_name, String tbl_name,
       List<String> part_vals, short max_parts, String userName, List<String> groupNames)
       throws MetaException, InvalidObjectException, NoSuchObjectException {
     List<Partition> partitions = new ArrayList<Partition>();
     boolean success = false;
     try {
       openTransaction();
       LOG.debug("executing listPartitionNamesPsWithAuth");
       Collection parts = getPartitionPsQueryResults(db_name, tbl_name,
           part_vals, max_parts, null);
       MTable mtbl = getMTable(db_name, tbl_name);
       for (Object o : parts) {
         Partition part = convertToPart((MPartition) o);
         //set auth privileges
         if (null != userName && null != groupNames &&
             "TRUE".equalsIgnoreCase(mtbl.getParameters().get("PARTITION_LEVEL_PRIVILEGE"))) {
           String partName = Warehouse.makePartName(this.convertToFieldSchemas(mtbl
               .getPartitionKeys()), part.getValues());
           PrincipalPrivilegeSet partAuth = getPartitionPrivilegeSet(db_name,
               tbl_name, partName, userName, groupNames);
           part.setPrivileges(partAuth);
         }
         partitions.add(part);
       }
       success = commitTransaction();
     } finally {
       if (!success) {
         rollbackTransaction();
       }
     }
     return partitions;
   }
 
   @Override
   public List<String> listPartitionNamesPs(String dbName, String tableName,
       List<String> part_vals, short max_parts) throws MetaException, NoSuchObjectException {
     List<String> partitionNames = new ArrayList<String>();
     boolean success = false;
     try {
       openTransaction();
       LOG.debug("Executing listPartitionNamesPs");
       Collection names = getPartitionPsQueryResults(dbName, tableName,
           part_vals, max_parts, "partitionName");
       for (Object o : names) {
         partitionNames.add((String) o);
       }
       success = commitTransaction();
     } finally {
       if (!success) {
         rollbackTransaction();
       }
     }
     return partitionNames;
   }
 
   // TODO:pc implement max
   private List<MPartition> listMPartitions(String dbName, String tableName,
       int max) {
 
     boolean success = false;
     List<MPartition> mparts = null;
     try {
       openTransaction();
       LOG.debug("Executing listMPartitions");
       dbName = dbName.toLowerCase().trim();
       tableName = tableName.toLowerCase().trim();
       Query query = pm.newQuery(MPartition.class,
           "table.tableName == t1 && table.database.name == t2 && parent == null");
       query.declareParameters("java.lang.String t1, java.lang.String t2");
       query.setOrdering("partitionName ascending");
       if(max > 0) {
         query.setRange(0, max);
       }
       mparts = (List<MPartition>) query.execute(tableName, dbName);
       LOG.debug("Done executing query for listMPartitions");
       pm.retrieveAll(mparts);
       success = commitTransaction();
       LOG.debug("Done retrieving all objects for listMPartitions");
     } finally {
       if (!success) {
         rollbackTransaction();
       }
     }
     return mparts;
   }
 
   @Override
   public List<Partition> getPartitionsByNames(String dbName, String tblName,
       List<String> partNames) throws MetaException, NoSuchObjectException {
 
     boolean success = false;
     List<Partition> results = new ArrayList<Partition>();
 
     try {
       openTransaction();
 
       StringBuilder sb = new StringBuilder(
           "table.tableName == t1 && table.database.name == t2 && (");
       int n = 0;
       Map<String, String> params = new HashMap<String, String>();
       for (Iterator<String> itr = partNames.iterator(); itr.hasNext();) {
         String pn = "p" + n;
         n++;
         String part = itr.next();
         params.put(pn, part);
         sb.append("partitionName == ").append(pn);
         sb.append(" || ");
       }
       sb.setLength(sb.length() - 4); // remove the last " || "
       sb.append(')');
 
       Query query = pm.newQuery(MPartition.class, sb.toString());
 
       LOG.debug(" JDOQL filter is " + sb.toString());
 
       params.put("t1", tblName.trim());
       params.put("t2", dbName.trim());
 
       String parameterDeclaration = makeParameterDeclarationString(params);
       query.declareParameters(parameterDeclaration);
       query.setOrdering("partitionName ascending");
 
       List<MPartition> mparts = (List<MPartition>) query.executeWithMap(params);
 
       // can be load recursivly in oracle
 //      this.loadSubpartitions(mparts);
 
       // pm.retrieveAll(mparts); // retrieveAll is pessimistic. some fields may not be needed
       results = convertToParts(dbName, tblName, mparts);
       // pm.makeTransientAll(mparts); // makeTransient will prohibit future access of unfetched fields
       query.closeAll();
       success = commitTransaction();
     } finally {
       if (!success) {
         rollbackTransaction();
       }
     }
 
     return results;
   }
 
   @Override
   public List<Partition> getPartitionsByFilter(String dbName, String tblName,
       String filter, short maxParts) throws MetaException, NoSuchObjectException {
     openTransaction();
     List<Partition> parts = convertToParts(listMPartitionsByFilter(dbName,
         tblName, filter, maxParts));
     LOG.info("# parts after pruning = " + parts.size());
     commitTransaction();
     return parts;
   }
 
   private FilterParser getFilterParser(String filter) throws MetaException {
     CharStream cs = new ANTLRNoCaseStringStream(filter);
     FilterLexer lexer = new FilterLexer(cs);
 
     CommonTokenStream tokens = new CommonTokenStream();
     tokens.setTokenSource (lexer);
 
     FilterParser parser = new FilterParser(tokens);
 
     try {
       parser.filter();
     } catch(RecognitionException re) {
       throw new MetaException("Error parsing partition filter : " + re);
     }
 
     if (lexer.errorMsg != null) {
       throw new MetaException("Error parsing partition filter : " + lexer.errorMsg);
     }
 
     return parser;
   }
 
   /**
    * Makes a JDO query filter string
    * if mtable is not null, generates the query to filter over partitions in a table.
    * if mtable is null, generates the query to filter over tables in a database
    */
   private String makeQueryFilterString(MTable mtable, String filter,
       Map<String, Object> params)
       throws MetaException {
 
     StringBuilder queryBuilder = new StringBuilder();
     if (mtable != null) {
       queryBuilder.append("table.tableName == t1 && table.database.name == t2");
     } else {
       queryBuilder.append("database.name == dbName");
     }
 
     if (filter != null && filter.length() > 0) {
       FilterParser parser = getFilterParser(filter);
       String jdoFilter;
 
       if (mtable != null) {
         Table table = convertToTable(mtable);
         jdoFilter = parser.tree.generateJDOFilter(table, params);
       } else {
         jdoFilter = parser.tree.generateJDOFilter(null, params);
       }
       LOG.debug("jdoFilter = " + jdoFilter);
 
       if( jdoFilter.trim().length() > 0 ) {
         queryBuilder.append(" && ( ");
         queryBuilder.append(jdoFilter.trim());
         queryBuilder.append(" )");
       }
     }
     return queryBuilder.toString();
   }
 
   private String makeTableQueryFilterString(String filter,
       Map<String, Object> params)
       throws MetaException {
     return makeQueryFilterString(null, filter, params);
   }
 
   private String makeParameterDeclarationString(Map<String, String> params) {
     //Create the parameter declaration string
     StringBuilder paramDecl = new StringBuilder();
     for (String key : params.keySet()) {
       paramDecl.append(", java.lang.String " + key);
     }
     return paramDecl.toString();
   }
 
   private String makeParameterDeclarationStringObj(Map<String, Object> params) {
     //Create the parameter declaration string
     StringBuilder paramDecl = new StringBuilder();
     for (Entry<String, Object> entry : params.entrySet()) {
       paramDecl.append(", ");
       paramDecl.append(entry.getValue().getClass().getName());
       paramDecl.append(" ");
       paramDecl.append(entry.getKey());
     }
     return paramDecl.toString();
   }
 
   private List<MPartition> listMPartitionsByFilter(String dbName, String tableName,
       String filter, short maxParts) throws MetaException, NoSuchObjectException{
     boolean success = false;
     List<MPartition> mparts = null;
     try {
       openTransaction();
       LOG.debug("Executing listMPartitionsByFilter");
       dbName = dbName.toLowerCase();
       tableName = tableName.toLowerCase();
 
       MTable mtable = getMTable(dbName, tableName);
       if( mtable == null ) {
         throw new NoSuchObjectException("Specified database/table does not exist : "
             + dbName + "." + tableName);
       }
       Map<String, Object> params = new HashMap<String, Object>();
       String queryFilterString =
         makeQueryFilterString(mtable, filter, params);
 
       Query query = pm.newQuery(MPartition.class,
           queryFilterString);
 
       if( maxParts >= 0 ) {
         //User specified a row limit, set it on the Query
         query.setRange(0, maxParts);
       }
 
       LOG.debug("Filter specified is " + filter + "," +
              " JDOQL filter is " + queryFilterString);
 
       params.put("t1", tableName.trim());
       params.put("t2", dbName.trim());
 
       String parameterDeclaration = makeParameterDeclarationStringObj(params);
       query.declareParameters(parameterDeclaration);
       query.setOrdering("partitionName ascending");
 
       mparts = (List<MPartition>) query.executeWithMap(params);
 
       LOG.debug("Done executing query for listMPartitionsByFilter");
       pm.retrieveAll(mparts);
       success = commitTransaction();
       LOG.debug("Done retrieving all objects for listMPartitionsByFilter");
     } finally {
       if (!success) {
         rollbackTransaction();
       }
     }
     return mparts;
   }
 
   @Override
   public List<String> listTableNamesByFilter(String dbName, String filter, short maxTables)
       throws MetaException {
     boolean success = false;
     List<String> tableNames = new ArrayList<String>();
     try {
       openTransaction();
       LOG.debug("Executing listTableNamesByFilter");
       dbName = dbName.toLowerCase().trim();
       Map<String, Object> params = new HashMap<String, Object>();
       String queryFilterString = makeTableQueryFilterString(filter, params);
       Query query = pm.newQuery(MTable.class);
       query.declareImports("import java.lang.String");
       query.setResult("tableName");
       query.setResultClass(java.lang.String.class);
       if (maxTables >= 0) {
         query.setRange(0, maxTables);
       }
       LOG.debug("filter specified is " + filter + "," + " JDOQL filter is " + queryFilterString);
       params.put("dbName", dbName);
       for (Entry<String, Object> entry : params.entrySet()) {
         LOG.debug("key: " + entry.getKey() + " value: " + entry.getValue() +
             " class: " + entry.getValue().getClass().getName());
       }
       String parameterDeclaration = makeParameterDeclarationStringObj(params);
       query.declareParameters(parameterDeclaration);
       query.setFilter(queryFilterString);
       Collection names = (Collection) query.executeWithMap(params);
       //have to emulate "distinct", otherwise tables with the same name may be returned
       Set<String> tableNamesSet = new HashSet<String>();
       for (Iterator i = names.iterator(); i.hasNext();) {
         tableNamesSet.add((String) i.next());
       }
       tableNames = new ArrayList<String>(tableNamesSet);
       LOG.debug("Done executing query for listTableNamesByFilter");
       success = commitTransaction();
       LOG.debug("Done retrieving all objects for listTableNamesByFilter");
 
     } finally {
       if (!success) {
         rollbackTransaction();
       }
     }
     return tableNames;
   }
 
   @Override
   public List<String> listPartitionNamesByFilter(String dbName, String tableName,
       String filter, short maxParts) throws MetaException {
     boolean success = false;
     List<String> partNames = new ArrayList<String>();
     try {
       openTransaction();
       LOG.debug("Executing listMPartitionNamesByFilter");
       dbName = dbName.toLowerCase();
       tableName = tableName.toLowerCase();
 
       MTable mtable = getMTable(dbName, tableName);
       if( mtable == null ) {
         // To be consistent with the behavior of listPartitionNames, if the
         // table or db does not exist, we return an empty list
         return partNames;
       }
       Map<String, Object> params = new HashMap<String, Object>();
       String queryFilterString =
         makeQueryFilterString(mtable, filter, params);
       Query query = pm.newQuery(
           "select partitionName from org.apache.hadoop.hive.metastore.model.MPartition "
           + "where " + queryFilterString);
 
       if( maxParts >= 0 ) {
         //User specified a row limit, set it on the Query
         query.setRange(0, maxParts);
       }
 
       LOG.debug("Filter specified is " + filter + "," +
           " JDOQL filter is " + queryFilterString);
       LOG.debug("Parms is " + params);
 
       params.put("t1", tableName.trim());
       params.put("t2", dbName.trim());
 
       String parameterDeclaration = makeParameterDeclarationStringObj(params);
       query.declareParameters(parameterDeclaration);
       query.setOrdering("partitionName ascending");
       query.setResult("partitionName");
 
       Collection names = (Collection) query.executeWithMap(params);
       partNames = new ArrayList<String>();
       for (Iterator i = names.iterator(); i.hasNext();) {
         partNames.add((String) i.next());
       }
 
       LOG.debug("Done executing query for listMPartitionNamesByFilter");
       success = commitTransaction();
       LOG.debug("Done retrieving all objects for listMPartitionNamesByFilter");
     } finally {
       if (!success) {
         rollbackTransaction();
       }
     }
     return partNames;
   }
 
   public void alterTable(String dbname, String name, Table newTable)
       throws InvalidObjectException, MetaException {
     boolean success = false;
     try {
       openTransaction();
       name = name.toLowerCase();
       dbname = dbname.toLowerCase();
       MTable newt = convertToMTable(newTable);
       if (newt == null) {
         throw new InvalidObjectException("new table is invalid");
       }
 
       MTable oldt = getMTable(dbname, name);
       if (oldt == null) {
         throw new MetaException("table " + name + " doesn't exist");
       }
 
       long db_id = Long.parseLong(MSGFactory.getIDFromJdoObjectId(pm.getObjectId(newt.getDatabase()).toString()));
       ArrayList<MSGFactory.DDLMsg> msgs = new ArrayList<MSGFactory.DDLMsg>();     //先把消息存储起来，修改执行成功了再发
 
 
       //alt table name
       if(!oldt.getTableName().toLowerCase().equals(newt.getTableName().toLowerCase()))
       {
         HashMap<String, Object> params = new HashMap<String, Object>();
         params.put("table_name", newt.getTableName());
         params.put("old_table_name", oldt.getTableName());
 //        MetaMsgServer.sendMsg(MSGFactory.generateDDLMsg(MSGType.MSG_ALT_TALBE_NAME,db_id,-1, pm, oldt,params));
         msgs.add(MSGFactory.generateDDLMsg(MSGType.MSG_ALT_TALBE_NAME,db_id,-1, pm, oldt,params));
       }
       //del col   可以删除多个列,删除多个时,发送多次消息
       if(oldt.getSd().getCD().getCols().size() > newt.getSd().getCD().getCols().size())
       {
         List<MFieldSchema> oldCols = new ArrayList<MFieldSchema>();
         oldCols.addAll( oldt.getSd().getCD().getCols());
         List<MFieldSchema> newCols = new ArrayList<MFieldSchema>();
         newCols.addAll( newt.getSd().getCD().getCols());
 
         oldCols.removeAll(newCols);
         for(MFieldSchema omfs : oldCols)
         {
           HashMap<String, Object> ps = new HashMap<String, Object>();
           ps.put("db_name", oldt.getDatabase().getName());
           ps.put("table_name", oldt.getTableName());
           LOG.info("---zy--in alterTable delCol,colname:"+omfs.getName());
           ps.put("column_name",omfs.getName());
 //          MetaMsgServer.sendMsg(MSGFactory.generateDDLMsg(MSGType.MSG_ALT_TALBE_DEL_COL,db_id,-1, pm, oldt.getSd().getCD(),ps));
           msgs.add(MSGFactory.generateDDLMsg(MSGType.MSG_ALT_TALBE_DEL_COL,db_id,-1, pm, newt.getSd().getCD(),ps));
         }
       }
       //add col
       if(oldt.getSd().getCD().getCols().size() < newt.getSd().getCD().getCols().size())
       {
         List<MFieldSchema> oldCols = new ArrayList<MFieldSchema>();
         oldCols.addAll( oldt.getSd().getCD().getCols());
         List<MFieldSchema> newCols = new ArrayList<MFieldSchema>();
         newCols.addAll( newt.getSd().getCD().getCols());
 //        LOG.info("---zy--in alterTable addCol, before removeall");
         newCols.removeAll(oldCols);
 //        LOG.info("---zy--in alterTable addCol, after removeall");
         for(MFieldSchema nmfs : newCols)
         {
           HashMap<String, Object> ps = new HashMap<String, Object>();
           ps.put("db_name", oldt.getDatabase().getName());
           ps.put("table_name", oldt.getTableName());
           LOG.info("---zy--in alterTable addCol,colname:"+nmfs.getName());
           ps.put("column_name",nmfs.getName());
           ps.put("column_type", nmfs.getType());
 //            MetaMsgServer.sendMsg(MSGFactory.generateDDLMsg(MSGType.MSG_ALT_TALBE_ADD_COL,db_id,-1, pm, oldt.getSd().getCD(),ps));
           msgs.add(MSGFactory.generateDDLMsg(MSGType.MSG_ALT_TALBE_ADD_COL,db_id,-1, pm, newt.getSd().getCD(),ps));
         }
       }
       //修改列名，列类型  一次只能修改一个
       if(oldt.getSd().getCD().getCols().size() == newt.getSd().getCD().getCols().size())
       {
         List<MFieldSchema> oldCols = new ArrayList<MFieldSchema>();
         oldCols.addAll( oldt.getSd().getCD().getCols());
         List<MFieldSchema> newCols = new ArrayList<MFieldSchema>();
         newCols.addAll( newt.getSd().getCD().getCols());
 
         oldCols.removeAll(newt.getSd().getCD().getCols());
         newCols.removeAll(oldt.getSd().getCD().getCols());
         if(oldCols.size() == 1 && newCols.size() == 1 && !oldCols.get(0).getName().equals(newCols.get(0).getName()))    //说明只有一列是不同的,且是名字不同
         {
           HashMap<String, Object> params = new HashMap<String, Object>();
           params.put("db_name", oldt.getDatabase().getName());
           params.put("table_name", oldt.getTableName());
           params.put("column_name",newCols.get(0).getName());
           params.put("old_column_name", oldCols.get(0).getName());
 //          MetaMsgServer.sendMsg(MSGFactory.generateDDLMsg(MSGType.MSG_ALT_TALBE_ALT_COL_NAME,db_id,-1, pm, oldt.getSd().getCD(),params));
           msgs.add(MSGFactory.generateDDLMsg(MSGType.MSG_ALT_TALBE_ALT_COL_NAME,db_id,-1, pm, newt.getSd().getCD(),params));
         }
         else if(oldCols.size() == 1 && newCols.size() == 1 && !oldCols.get(0).getType().equals(newCols.get(0).getType()))         //修改了列类型
         {
           HashMap<String, Object> params = new HashMap<String, Object>();
           params.put("db_name", oldt.getDatabase().getName());
           params.put("table_name", oldt.getTableName());
           params.put("column_name", oldCols.get(0).getName());
           params.put("column_type",newCols.get(0).getType());
           params.put("old_column_type", oldCols.get(0).getType());
 //          MetaMsgServer.sendMsg(MSGFactory.generateDDLMsg(MSGType.MSG_ALT_TALBE_ALT_COL_TYPE,db_id,-1, pm, oldt.getSd().getCD(),params));
           msgs.add(MSGFactory.generateDDLMsg(MSGType.MSG_ALT_TALBE_ALT_COL_TYPE,db_id,-1, pm, newt.getSd().getCD(),params));
         }
       }
       //alt table param
       if(!tableParamEquals(oldt.getParameters(), newt.getParameters()) )
       {
         HashMap<String, Object> params = new HashMap<String, Object>();
         LOG.debug("---zy--in ObjectStore alterTable: alt table param");
         params.put("db_name", oldt.getDatabase().getName());
         params.put("table_name", oldt.getTableName());
         ArrayList<String>  ps = new ArrayList<String>();
         ps.addAll(newt.getParameters().keySet());
         params.put("tbl_param_keys", ps);
 
 //        MetaMsgServer.sendMsg(MSGFactory.generateDDLMsg(MSGType.MSG_ALT_TABLE_PARAM,db_id,-1, pm, oldt,params));
         msgs.add(MSGFactory.generateDDLMsg(MSGType.MSG_ALT_TABLE_PARAM,db_id,-1, pm, oldt,params));
       }
     //MSG_ALT_TALBE_PARTITIONING
       HashMap<String, Object> altPartitioningParams = new HashMap<String, Object>();
       LOG.warn("---zy-- in alter table,old partition keys:"+oldt.getPartitionKeys()+",new partition keys"+newt.getPartitionKeys());
       if(newt.getPartitionKeys().size() > oldt.getPartitionKeys().size())      //要传什么参数呢．．
       {
         altPartitioningParams.put("table_name", oldt.getTableName());
         altPartitioningParams.put("db_name", oldt.getDatabase().getName());
 //        msgs.add(MSGFactory.generateDDLMsg(MSGType.MSG_ALT_TALBE_PARTITIONING,db_id,-1, pm, oldt,params));
       }
 
       //ALT_TABLE_SPLITKEYS
       HashMap<String, Object> altSplitKeyParams = new HashMap<String, Object>();
       LOG.warn("---zy-- in alter table,old split keys:"+oldt.getFileSplitKeys()+",new split keys"+newt.getFileSplitKeys());
       if(newt.getFileSplitKeys().size() > 0)
       {
         altSplitKeyParams.put("table_name", oldt.getTableName());
         altSplitKeyParams.put("db_name", oldt.getDatabase().getName());
 //        MetaMsgServer.sendMsg(MSGFactory.generateDDLMsg(MSGType.MSG_ALT_TALBE_PARTITIONING,db_id,-1, pm, oldt,params));
       }
       //MSG_ALT_TALBE_DISTRIBUTE        似乎没有修改过..
 
 
       // For now only alter name, owner, paramters, cols, bucketcols are allowed
       oldt.setTableName(newt.getTableName().toLowerCase());
       oldt.setParameters(newt.getParameters());
       oldt.setOwner(newt.getOwner());
 
       // Fully copy over the contents of the new SD into the old SD,
       // so we don't create an extra SD in the metastore db that has no references.
       copyMSD(newt.getSd(), oldt.getSd());
       oldt.setDatabase(newt.getDatabase());
       oldt.setRetention(newt.getRetention());
       // !NOTE: append new partition keys to old partition key list with new version
       List<MFieldSchema> newFS = new ArrayList<MFieldSchema>();
       long cur_version = -1;
 
       if (oldt.getPartitionKeys() != null) {
         for (MFieldSchema mfs : oldt.getPartitionKeys()) {
           if (mfs.getVersion() > cur_version) {
             cur_version = mfs.getVersion();
             newFS.add(mfs);
           }
         }
       }
       cur_version++;
       if (newt.getPartitionKeys() != null) {
         for (MFieldSchema mfs : newt.getPartitionKeys()) {
           mfs.setVersion(cur_version);
           newFS.add(mfs);
         }
       }
       oldt.setPartitionKeys(newFS);
       if(!altPartitioningParams.isEmpty())    //说明该事件被触发了
       {
         altPartitioningParams.put("version", cur_version);
         msgs.add(MSGFactory.generateDDLMsg(MSGType.MSG_ALT_TALBE_PARTITIONING,db_id,-1, pm, oldt,altPartitioningParams));
       }
       // !NOTE: append new file split keys to old file split key list with new version
       newFS.clear();
       cur_version = -1;
       if (oldt.getFileSplitKeys() != null) {
         for (MFieldSchema mfs : oldt.getFileSplitKeys()) {
           if (mfs.getVersion() > cur_version) {
             cur_version = mfs.getVersion();
           }
           newFS.add(mfs);
         }
       }
       cur_version++;
       List<PartitionInfo> newPis =  PartitionInfo.getPartitionInfo(convertToFieldSchemas(newt.getFileSplitKeys()));
       if (newPis != null) {
         int i=0;
         for (PartitionInfo pif : newPis) {
           pif.setP_version((int)cur_version);
           newt.getFileSplitKeys().get(i).setVersion(cur_version);
           newt.getFileSplitKeys().get(i).setComment(pif.toJson());
           newFS.add(newt.getFileSplitKeys().get(i));
           i++;
         }
       }
 //      if (newt.getFileSplitKeys() != null) {
 //        for (MFieldSchema mfs : newt.getFileSplitKeys()) {
 //          mfs.setVersion(cur_version);
 //          newFS.add(mfs);
 //        }
 //      }
       oldt.setFileSplitKeys(newFS);
       if(!altSplitKeyParams.isEmpty())
       {
         altSplitKeyParams.put("version", cur_version);
         msgs.add(MSGFactory.generateDDLMsg(MSGType.MSG_ALT_TABLE_SPLITKEYS,db_id,-1, pm, oldt,altSplitKeyParams));
       }
 
       oldt.setTableType(newt.getTableType());
       oldt.setLastAccessTime(newt.getLastAccessTime());
       oldt.setViewOriginalText(newt.getViewOriginalText());
       oldt.setViewExpandedText(newt.getViewExpandedText());
 
       // commit the changes
       success = commitTransaction();
 
       if(success) {
         for(MSGFactory.DDLMsg msg : msgs) {
           Object eventObject = msg.getEventObject();
           long id = -1;
           if(eventObject instanceof Long){
             id = (Long)eventObject;
           }else{
             Object objectId = pm.getObjectId(eventObject);
             LOG.info("Sending DDL message:"+msg.getEvent_id()+"---"+objectId.toString());
             try{
               id = Long.parseLong(MSGFactory.getIDFromJdoObjectId(objectId.toString()));
             }catch(Exception e){
               LOG.error(e, e);
             }
           }
           msg.setObject_id(id);
           MetaMsgServer.sendMsg(msg);
         }
       }
 
 
     } finally {
       if (!success) {
         rollbackTransaction();
       }
     }
   }
 
   //add by zy
   private boolean tableParamEquals(Map<String,String> m1,Map<String,String> m2)
   {
     LOG.info("---zy--in tableParamEquals old:"+m1.keySet()+", new:"+m2.keySet());
     //忽略下面三个键
     String[] keys = {"transient_lastDdlTime","last_modified_time","last_modified_by"};
     Map<String,String> mc1 = new HashMap<String,String>();
     Map<String,String> mc2 = new HashMap<String,String>();
     mc1.putAll(m1);
     mc2.putAll(m2);
     for(String k:keys)
     {
       mc1.remove(k);
       mc2.remove(k);
     }
 //    if(m1.size() != m2.size()) {
 //      LOG.info("---zy--in tableParamEquals size not equal old:"+m1.keySet()+", new:"+m2.keySet());
 //      return false;
 //    }
 //    boolean re = true;
 //    while(m1.keySet().iterator().hasNext())
 //    {
 //      String key = m1.keySet().iterator().next();
 //      if(!(m2.containsKey(key) && m1.get(key).equals(m2.get(key)))) {
 //        LOG.debug("---zy--in tableParamEquals "+key+" "+m1.get(key)+" "+m2.get(key));
 //        re = false;
 //      }
 
 //    }
     return mc1.equals(mc2);     //map的equals方法应该是重载过,可以直接拿来判断的
   }
 
   public void alterIndex(String dbname, String baseTblName, String name, Index newIndex)
       throws InvalidObjectException, MetaException {
     boolean success = false;
     try {
       openTransaction();
       name = name.toLowerCase();
       baseTblName = baseTblName.toLowerCase();
       dbname = dbname.toLowerCase();
       MIndex newi = convertToMIndex(newIndex);
       if (newi == null) {
         throw new InvalidObjectException("new index is invalid");
       }
 
       MIndex oldi = getMIndex(dbname, baseTblName, name);
       if (oldi == null) {
         throw new MetaException("index " + name + " doesn't exist");
       }
 
       // For now only alter paramters are allowed
       oldi.setParameters(newi.getParameters());
 
       // commit the changes
       success = commitTransaction();
 
       long db_id = Long.parseLong(MSGFactory.getIDFromJdoObjectId(pm.getObjectId(oldi.getOrigTable().getDatabase()).toString()));
       HashMap<String,Object> params = new HashMap<String,Object>();
       ArrayList<String>  ps = new ArrayList<String>();
       ps.addAll(oldi.getParameters().keySet());
       params.put("param_name", ps);
       if(success) {
         MetaMsgServer.sendMsg(MSGFactory.generateDDLMsg(MSGType.MSG_ALT_INDEX_PARAM, db_id, -1, pm, oldi, params));
       }
     } finally {
       if (!success) {
         rollbackTransaction();
       }
     }
   }
 
   private void alterPartitionNoTxn(String dbname, String name, String partName, List<String> part_vals,
       Partition newPart) throws InvalidObjectException, MetaException {
     name = name.toLowerCase();
     dbname = dbname.toLowerCase();
     // TODO: fix it
     MPartition oldp = getMPartition(dbname, name, partName);
     MPartition newp = convertToMPart(newPart, false);
     if (oldp == null || newp == null) {
       throw new InvalidObjectException("partition does not exist.");
     }
 
     long db_id = Long.parseLong(MSGFactory.getIDFromJdoObjectId(pm.getObjectId(oldp.getTable().getDatabase()).toString()));
     HashMap<String, Object> params = new HashMap<String, Object>();
     params.put("old_partition_name", oldp.getPartitionName());
     MetaMsgServer.sendMsg(MSGFactory.generateDDLMsg(MSGType.MSG_ALT_PARTITION,db_id,-1, pm, oldp,params));
 
     oldp.setValues(newp.getValues());
     oldp.setPartitionName(newp.getPartitionName());
     LOG.info("-----> Set partition name to: " + newp.getPartitionName());
     oldp.setParameters(newPart.getParameters());
     copyMSD(newp.getSd(), oldp.getSd());
     if (newp.getCreateTime() != oldp.getCreateTime()) {
       oldp.setCreateTime(newp.getCreateTime());
     }
     if (newp.getLastAccessTime() != oldp.getLastAccessTime()) {
       oldp.setLastAccessTime(newp.getLastAccessTime());
     }
   }
 
   public void alterPartition(String dbname, String name, String partName, List<String> part_vals, Partition newPart)
       throws InvalidObjectException, MetaException {
     boolean success = false;
     try {
       openTransaction();
       alterPartitionNoTxn(dbname, name, partName, part_vals, newPart);
       // commit the changes
       success = commitTransaction();
     } finally {
       if (!success) {
         rollbackTransaction();
         throw new MetaException(
             "The transaction for alter partition did not commit successfully.");
       }
     }
   }
 
   public void updatePartition(Partition newPart) throws InvalidObjectException, MetaException {
     boolean success = false;
     try {
       openTransaction();
       MPartition oldp = getMPartition(newPart.getDbName(), newPart.getTableName(), newPart.getPartitionName());
       // update the files list!
       oldp.setFiles(newPart.getFiles());
       pm.makePersistent(oldp);
       success = commitTransaction();
     } finally {
       if (!success) {
         rollbackTransaction();
         throw new MetaException("Update partition did not commit successfully.");
       }
     }
   }
 
   public void updateSubpartition(Subpartition newPart) throws InvalidObjectException, MetaException {
     boolean success = false;
     try {
       openTransaction();
       MPartition oldp = getMPartition(newPart.getDbName(), newPart.getTableName(), newPart.getPartitionName());
       // update the files list!
       oldp.setFiles(newPart.getFiles());
       pm.makePersistent(oldp);
       pm.retrieve(oldp.getTable().getDatabase());
       long db_id = Long.parseLong(MSGFactory.getIDFromJdoObjectId(pm.getObjectId(oldp.getTable().getDatabase()).toString()));
       success = commitTransaction();
 
       //added by zjw for msg queue
       HashSet<Long> new_set = new HashSet<Long>();
       if(newPart.getFiles() != null){
         new_set.addAll(newPart.getFiles());
       }
       HashSet<Long> old_set = new HashSet<Long>();
       if(oldp.getFiles() != null){
         old_set.addAll(oldp.getFiles());
       }
 
       /**
        * 注意，如果分区文件数量没有变化，消息不会推送到后端
        */
       HashMap<String,Object> old_params= new HashMap<String,Object>();
 
 
       old_params.put("partition_name", newPart.getPartitionName());
       old_params.put("partition_level", 2);
       old_params.put("db_name", newPart.getDbName());
       old_params.put("table_name", newPart.getTableName());
 
       if(new_set.size() > old_set.size()){
         new_set.removeAll(old_set);
         old_params.put("f_id", new_set);
         MetaMsgServer.sendMsg(MSGFactory.generateDDLMsg(MSGType.MSG_ADD_PARTITION_FILE,db_id,-1,
             pm,new_set.toArray(new Long[0]),old_params));
       }else if(new_set.size() < old_set.size()){
         old_set.removeAll(new_set);
         old_params.put("f_id", old_set);
         MetaMsgServer.sendMsg(MSGFactory.generateDDLMsg(MSGType.MSG_DEL_PARTITION_FILE,db_id,-1,
             pm,old_set.toArray(new Long[0]),old_params));
       }
 
 
     } finally {
       if (!success) {
         rollbackTransaction();
         throw new MetaException("Update partition did not commit successfully.");
       }
     }
   }
 
   public void alterPartitions(String dbname, String name, List<String> partNames, List<List<String>> part_vals,
       List<Partition> newParts) throws InvalidObjectException, MetaException {
     boolean success = false;
     try {
       openTransaction();
       Iterator<List<String>> part_val_itr = part_vals.iterator();
       for (int i = 0; i < newParts.size(); i++) {
         Partition tmpPart = newParts.get(i);
         List<String> tmpPartVals = part_val_itr.next();
         alterPartitionNoTxn(dbname, name, partNames.get(i), tmpPartVals, tmpPart);
       }
       // commit the changes
       success = commitTransaction();
     } finally {
       if (!success) {
         rollbackTransaction();
         throw new MetaException(
             "The transaction for alter partition did not commit successfully.");
       }
     }
   }
 
   private void copyMSD(MStorageDescriptor newSd, MStorageDescriptor oldSd) {
     oldSd.setLocation(newSd.getLocation());
     MColumnDescriptor oldCD = oldSd.getCD();
     // If the columns of the old column descriptor != the columns of the new one,
     // then change the old storage descriptor's column descriptor.
     // Convert the MFieldSchema's to their thrift object counterparts, because we maintain
     // datastore identity (i.e., identity of the model objects are managed by JDO,
     // not the application).
     if (!(oldSd != null && oldSd.getCD() != null &&
          oldSd.getCD().getCols() != null &&
          newSd != null && newSd.getCD() != null &&
          newSd.getCD().getCols() != null &&
          convertToFieldSchemas(newSd.getCD().getCols()).
          equals(convertToFieldSchemas(oldSd.getCD().getCols()))
        )) {
         oldSd.setCD(newSd.getCD());
     }
 
     //If oldCd does not have any more references, then we should delete it
     // from the backend db
     removeUnusedColumnDescriptor(oldCD);
     oldSd.setBucketCols(newSd.getBucketCols());
     oldSd.setCompressed(newSd.isCompressed());
     oldSd.setInputFormat(newSd.getInputFormat());
     oldSd.setOutputFormat(newSd.getOutputFormat());
     oldSd.setNumBuckets(newSd.getNumBuckets());
     oldSd.getSerDeInfo().setName(newSd.getSerDeInfo().getName());
     oldSd.getSerDeInfo().setSerializationLib(
         newSd.getSerDeInfo().getSerializationLib());
     oldSd.getSerDeInfo().setParameters(newSd.getSerDeInfo().getParameters());
     oldSd.setSkewedColNames(newSd.getSkewedColNames());
     oldSd.setSkewedColValues(newSd.getSkewedColValues());
     oldSd.setSkewedColValueLocationMaps(newSd.getSkewedColValueLocationMaps());
     oldSd.setSortCols(newSd.getSortCols());
     oldSd.setParameters(newSd.getParameters());
     oldSd.setStoredAsSubDirectories(newSd.isStoredAsSubDirectories());
   }
 
   /**
    * Checks if a column descriptor has any remaining references by storage descriptors
    * in the db.  If it does not, then delete the CD.  If it does, then do nothing.
    * @param oldCD the column descriptor to delete if it is no longer referenced anywhere
    */
   private void removeUnusedColumnDescriptor(MColumnDescriptor oldCD) {
     if (oldCD == null) {
       return;
     }
 
     boolean success = false;
     try {
       openTransaction();
       LOG.debug("execute removeUnusedColumnDescriptor");
       List<MStorageDescriptor> referencedSDs = listStorageDescriptorsWithCD(oldCD, 1);
       //if no other SD references this CD, we can throw it out.
       if (referencedSDs != null && referencedSDs.isEmpty()) {
         pm.retrieve(oldCD);
         pm.deletePersistent(oldCD);
       }
       success = commitTransaction();
       LOG.debug("successfully deleted a CD in removeUnusedColumnDescriptor");
     } finally {
       if (!success) {
         rollbackTransaction();
       }
     }
   }
 
   /**
    * Called right before an action that would drop a storage descriptor.
    * This function makes the SD's reference to a CD null, and then deletes the CD
    * if it no longer is referenced in the table.
    * @param msd the storage descriptor to drop
    */
   private void preDropStorageDescriptor(MStorageDescriptor msd) {
     if (msd == null || msd.getCD() == null) {
       return;
     }
 
     MColumnDescriptor mcd = msd.getCD();
     // Because there is a 1-N relationship between CDs and SDs,
     // we must set the SD's CD to null first before dropping the storage descriptor
     // to satisfy foriegn key constraints.
     msd.setCD(null);
     removeUnusedColumnDescriptor(mcd);
   }
 
   /**
    * Get a list of storage descriptors that reference a particular Column Descriptor
    * @param oldCD the column descriptor to get storage descriptors for
    * @param maxSDs the maximum number of SDs to return
    * @return a list of storage descriptors
    */
   private List<MStorageDescriptor> listStorageDescriptorsWithCD(MColumnDescriptor oldCD,
       long maxSDs) {
     boolean success = false;
     List<MStorageDescriptor> sds = null;
     try {
       openTransaction();
       LOG.debug("Executing listStorageDescriptorsWithCD");
       Query query = pm.newQuery(MStorageDescriptor.class,
           "this.cd == inCD");
       query.declareParameters("MColumnDescriptor inCD");
       if(maxSDs >= 0) {
         //User specified a row limit, set it on the Query
         query.setRange(0, maxSDs);
       }
       sds = (List<MStorageDescriptor>) query.execute(oldCD);
       LOG.debug("Done executing query for listStorageDescriptorsWithCD");
       pm.retrieveAll(sds);
       success = commitTransaction();
       LOG.debug("Done retrieving all objects for listStorageDescriptorsWithCD");
     } finally {
       if (!success) {
         rollbackTransaction();
       }
     }
     return sds;
   }
 
   @Override
   public boolean addIndex(Index index) throws InvalidObjectException,
       MetaException {
     boolean commited = false;
     try {
       openTransaction();
       MIndex idx = convertToMIndex(index);
       pm.makePersistent(idx);
       long db_id = Long.parseLong(MSGFactory.getIDFromJdoObjectId(pm.getObjectId(idx.getOrigTable().getDatabase()).toString()));
       commited = commitTransaction();
 
       if(commited) {
         MetaMsgServer.sendMsg(MSGFactory.generateDDLMsg(MSGType.MSG_NEW_INDEX,db_id,-1,
             pm,idx,null));
       }
       return true;
     } finally {
       if (!commited) {
         rollbackTransaction();
         return false;
       }
     }
   }
 
   private MIndex convertToMIndex(Index index) throws InvalidObjectException,
       MetaException {
 
     StorageDescriptor sd = index.getSd();
     if (sd == null) {
       throw new InvalidObjectException("Storage descriptor is not defined for index.");
     }
 
     MStorageDescriptor msd = this.convertToMStorageDescriptor(sd);
     MTable origTable = getMTable(index.getDbName(), index.getOrigTableName());
     if (origTable == null) {
       throw new InvalidObjectException(
           "Original table does not exist for the given index.");
     }
 
     MTable indexTable = null;
 //    MTable indexTable = getMTable(index.getDbName(), index.getIndexTableName());
 //    if (indexTable == null) {
 //      throw new InvalidObjectException(
 //          "Underlying index table does not exist for the given index.");
 //    }
 
     return new MIndex(index.getIndexName(), origTable, index.getCreateTime(),
         index.getLastAccessTime(), index.getParameters(), indexTable, msd,
         index.getIndexHandlerClass(), index.isDeferredRebuild());
   }
 
   @Override
   public boolean dropIndex(String dbName, String origTableName, String indexName)
       throws MetaException {
     boolean success = false;
     try {
       openTransaction();
       MIndex index = getMIndex(dbName, origTableName, indexName);
       MTable tmpOrigTable = null;
 
       if (index != null) {
         // FIXME: set sd to null?
         LOG.error("--> DROP INDEX " + index.getIndexName());
         preDropStorageDescriptor(index.getSd());
         tmpOrigTable = index.getOrigTable();
 
         index.setOrigTable(null);
         index.setIndexTable(null);
 
         pm.deletePersistent(index);
       }
       success = commitTransaction();
       if (tmpOrigTable != null) {
         long db_id = Long.parseLong(MSGFactory.getIDFromJdoObjectId(pm.getObjectId(tmpOrigTable.getDatabase()).toString()));
         HashMap<String,Object> params = new HashMap<String,Object>();
         params.put("index_name", indexName);
         MetaMsgServer.sendMsg(MSGFactory.generateDDLMsg(MSGType.MSG_DEL_INDEX, db_id, -1, pm, index, params));
       }
     } finally {
       if (!success) {
         rollbackTransaction();
       }
     }
     return success;
   }
 
   private MIndex getMIndex(String dbName, String originalTblName, String indexName) throws MetaException {
     MIndex midx = null;
     boolean commited = false;
     try {
       openTransaction();
       dbName = dbName.toLowerCase().trim();
       originalTblName = originalTblName.toLowerCase().trim();
       MTable mtbl = getMTable(dbName, originalTblName);
       if (mtbl == null) {
         commited = commitTransaction();
         return null;
       }
 
       Query query = pm.newQuery(MIndex.class,
         "origTable.tableName == t1 && origTable.database.name == t2 && indexName == t3");
       query.declareParameters("java.lang.String t1, java.lang.String t2, java.lang.String t3");
       query.setUnique(true);
       midx = (MIndex) query.execute(originalTblName, dbName, indexName);
       pm.retrieve(midx);
       commited = commitTransaction();
     } finally {
       if (!commited) {
         rollbackTransaction();
       }
     }
     return midx;
   }
 
   @Override
   public Index getIndex(String dbName, String origTableName, String indexName)
       throws MetaException {
     openTransaction();
     MIndex mIndex = this.getMIndex(dbName, origTableName, indexName);
     Index ret = convertToIndex(mIndex);
     commitTransaction();
     return ret;
   }
 
   private Index convertToIndex(MIndex mIndex) throws MetaException {
     if(mIndex == null) {
       return null;
     }
 
     return new Index(
     mIndex.getIndexName(),
     mIndex.getIndexHandlerClass(),
     mIndex.getOrigTable().getDatabase().getName(),
     mIndex.getOrigTable().getTableName(),
     mIndex.getCreateTime(),
     mIndex.getLastAccessTime(),
     mIndex.getIndexTable() == null ? null : mIndex.getIndexTable().getTableName(),
     this.convertToStorageDescriptor(mIndex.getSd()),
     mIndex.getParameters(),
     mIndex.getDeferredRebuild());
 
   }
 
   @Override
   public List<Index> getIndexes(String dbName, String origTableName, int max)
       throws MetaException {
     boolean success = false;
     try {
       openTransaction();
       List<MIndex> mIndexList = listMIndexes(dbName, origTableName, max);
       List<Index> indexes = new ArrayList<Index>(mIndexList.size());
       for (MIndex midx : mIndexList) {
         indexes.add(this.convertToIndex(midx));
       }
       success = commitTransaction();
       return indexes;
     } finally {
       if (!success) {
         rollbackTransaction();
       }
     }
   }
 
   private List<MIndex> listMIndexes(String dbName, String origTableName,
       int max) {
     boolean success = false;
     List<MIndex> mindexes = null;
     try {
       openTransaction();
       LOG.debug("Executing listMIndexes");
       dbName = dbName.toLowerCase().trim();
       origTableName = origTableName.toLowerCase().trim();
       Query query = pm.newQuery(MIndex.class,
           "origTable.tableName == t1 && origTable.database.name == t2");
       query.declareParameters("java.lang.String t1, java.lang.String t2");
       mindexes = (List<MIndex>) query.execute(origTableName, dbName);
       LOG.debug("Done executing query for listMIndexes");
       pm.retrieveAll(mindexes);
       success = commitTransaction();
       LOG.debug("Done retrieving all objects for listMIndexes");
     } finally {
       if (!success) {
         rollbackTransaction();
       }
     }
     return mindexes;
   }
 
   @Override
   public List<String> listIndexNames(String dbName, String origTableName,
       short max) throws MetaException {
     List<String> pns = new ArrayList<String>();
     boolean success = false;
     try {
       openTransaction();
       LOG.debug("Executing listIndexNames");
       dbName = dbName.toLowerCase().trim();
       origTableName = origTableName.toLowerCase().trim();
       Query q = pm.newQuery(
           "select indexName from org.apache.hadoop.hive.metastore.model.MIndex "
           + "where origTable.database.name == t1 && origTable.tableName == t2 "
           + "order by indexName asc");
       q.declareParameters("java.lang.String t1, java.lang.String t2");
       q.setResult("indexName");
       Collection names = (Collection) q.execute(dbName, origTableName);
       for (Iterator i = names.iterator(); i.hasNext();) {
         pns.add((String) i.next());
       }
       success = commitTransaction();
     } finally {
       if (!success) {
         rollbackTransaction();
       }
     }
     return pns;
   }
 
   @Override
   public boolean addRole(String roleName, String ownerName)
       throws InvalidObjectException, MetaException, NoSuchObjectException {
     boolean success = false;
     boolean commited = false;
     try {
       openTransaction();
       MRole nameCheck = this.getMRole(roleName);
       if (nameCheck != null) {
         throw new InvalidObjectException("Role " + roleName + " already exists");
       }
       int now = (int)(System.currentTimeMillis()/1000);
       MRole mRole = new MRole(roleName, now,
           ownerName);
       pm.makePersistent(mRole);
       commited = commitTransaction();
       success = true;
     } finally {
       if (!commited) {
         rollbackTransaction();
       }
     }
     return success;
   }
 
   @Override
   public boolean grantRole(Role role, String userName,
       PrincipalType principalType, String grantor, PrincipalType grantorType,
       boolean grantOption) throws MetaException, NoSuchObjectException,InvalidObjectException {
     boolean success = false;
     boolean commited = false;
     try {
       MRoleMap roleMap = null;
       try {
         roleMap = this.getMSecurityUserRoleMap(userName, principalType, role
             .getRoleName());
       } catch (Exception e) {
       }
       if (roleMap != null) {
         throw new InvalidObjectException("Principal " + userName
             + " already has the role " + role.getRoleName());
       }
       openTransaction();
       MRole mRole = getMRole(role.getRoleName());
       long now = System.currentTimeMillis()/1000;
       MRoleMap roleMember = new MRoleMap(userName, principalType.toString(),
           mRole, (int) now, grantor, grantorType.toString(), grantOption);
       pm.makePersistent(roleMember);
       commited = commitTransaction();
       success = true;
     } finally {
       if (!commited) {
         rollbackTransaction();
       }
     }
     return success;
   }
 
   @Override
   public boolean revokeRole(Role role, String userName, PrincipalType principalType) throws MetaException, NoSuchObjectException {
     boolean success = false;
     try {
       openTransaction();
       MRoleMap roleMember = getMSecurityUserRoleMap(userName, principalType,
           role.getRoleName());
       pm.deletePersistent(roleMember);
       success = commitTransaction();
     } finally {
       if (!success) {
         rollbackTransaction();
       }
     }
     return success;
   }
 
   //authentication and authorization with user by liulichao, begin
   @Override
   public boolean addUser(String userName, String passwd, String ownerName)
       throws InvalidObjectException, MetaException {
     boolean success = false;
     boolean commited = false;
 
     try {
       openTransaction();
       MUser nameCheck = this.getMUser(userName);
       if (nameCheck != null) {
         LOG.info("User "+ userName +" already exists");
         return false;
       }
         int now = (int)(System.currentTimeMillis()/1000);
       MUser mUser = new MUser(userName, passwd, now, ownerName);
       pm.makePersistent(mUser);
       commited = commitTransaction();
       success = true;
     } finally {
       if (!commited) {
         rollbackTransaction();
       }
     }
     return success;
   }
 
 @Override
   public boolean removeUser(String userName) throws MetaException,
       NoSuchObjectException {
     boolean success = false;
 
     try {
       openTransaction();
       MUser mUser = getMUser(userName);
       pm.retrieve(mUser);
 
       LOG.debug("remove.getusername"+mUser.getUserName());
       LOG.debug("remove.getusername:boolean"+(mUser.getUserName().equals(null)));
 
       if (!mUser.getUserName().equals(null)) {
         // first remove all then remove all the grants
         List<MRoleMap> uRoleMember =listMSecurityPrincipalMembershipRole(
             userName, PrincipalType.USER);
         if(uRoleMember.size() > 0){
           pm.deletePersistentAll(uRoleMember);
         }
 
         List<MGlobalPrivilege> userGrants = listPrincipalGlobalGrants(
             mUser.getUserName(), PrincipalType.USER);
         if (userGrants.size() > 0) {
           pm.deletePersistentAll(userGrants);
         }
         List<MDBPrivilege> dbGrants = listPrincipalAllDBGrant(
             mUser.getUserName(), PrincipalType.USER);
         if (dbGrants.size() > 0) {
           pm.deletePersistentAll(dbGrants);
         }
         List<MTablePrivilege> tabPartGrants = listPrincipalAllTableGrants(
             mUser.getUserName(), PrincipalType.USER);
         if (tabPartGrants.size() > 0) {
           pm.deletePersistentAll(tabPartGrants);
         }
         List<MPartitionPrivilege> partGrants = listPrincipalAllPartitionGrants(
             mUser.getUserName(), PrincipalType.USER);
         if (partGrants.size() > 0) {
           pm.deletePersistentAll(partGrants);
         }
         List<MTableColumnPrivilege> tblColumnGrants = listPrincipalAllTableColumnGrants(
             mUser.getUserName(), PrincipalType.USER);
         if (tblColumnGrants.size() > 0) {
           pm.deletePersistentAll(tblColumnGrants);
         }
         List<MPartitionColumnPrivilege> partColumnGrants = listPrincipalAllPartitionColumnGrants(
             mUser.getUserName(), PrincipalType.USER);
         if (tblColumnGrants.size() > 0) {
           pm.deletePersistentAll(partColumnGrants);
         }
         // finally remove the role
         pm.deletePersistent(mUser);
       } else {
         //LOG.debug("用户" + userName + "不存在！");
         LOG.debug("User " + userName + " doesnt exist!");
         return false;
       }
       success = commitTransaction();
     } finally {
       if (!success) {
         rollbackTransaction();
       }
     }
     return success;
   }
 
 @Override
 public boolean modifyUser(User user) throws MetaException,
     NoSuchObjectException {
   boolean commited = false;
 
   try {
     openTransaction();
     MUser nameCheck = this.getMUser(user.getUserName());
     if (nameCheck == null) {
       LOG.debug("User " + user.getUserName() + " doesnt exist!");
       return false;
     }
     nameCheck.setPasswd(user.getPassword());
     pm.makePersistent(nameCheck);
     commited = commitTransaction();
   } finally {
     if (!commited) {
       rollbackTransaction();
     }
   }
   return commited;
 }
 
 @Override
 public List<String> listUsersNames(String dbName) throws MetaException {
    List<String> userNames  = new ArrayList<String>();
    boolean success = false;
     try {
       openTransaction();
       Query query = pm.newQuery(MUser.class);
       Collection names = (Collection) query.execute();
       Iterator iter = names.iterator();
       while (iter.hasNext()) {
         MUser mu = (MUser)iter.next();
         if (mu.getDbs() != null) {
           for (MDatabase md : mu.getDbs()) {
             if (md.getName().equals(dbName)) {
               userNames.add(mu.getUserName());
               break;
             }
           }
         }
       }
       success = commitTransaction();
       return userNames;
     } finally {
       if (!success) {
         rollbackTransaction();
       }
     }
 }
 
 @Override
 public List<String> listUsersNames() throws MetaException {
     boolean success = false;
     try {
       openTransaction();
       LOG.debug("Executing listAllUserNames");
       Query query = pm.newQuery("select userName from org.apache.hadoop.hive.metastore.model.MUser");
       query.setResult("userName");
 
       Collection names = (Collection) query.execute();
 
       List<String> userNames  = new ArrayList<String>();
       for (Iterator i = names.iterator(); i.hasNext();) {
         userNames.add((String) i.next());
       }
       success = commitTransaction();
 
       LOG.debug("sizeofuserNames"+userNames.size());
       return userNames;
     } finally {
       if (!success) {
         rollbackTransaction();
       }
     }
 }
 
 @Override
 public boolean authentication(String userName, String passwd)
     throws MetaException, NoSuchObjectException {
   boolean auth = false;
 
   try {
     openTransaction();
     MUser nameCheck = this.getMUser(userName);
     if (nameCheck == null) {
       //LOG.debug("用户 " + userName + " 不存在！");
       LOG.debug("User " + userName + " doesnt exist");
       auth = false;
     } else if (nameCheck.getPasswd().equals(passwd)){
       auth = true;
     } else {
       //LOG.debug("用户名或密码错误！");
       LOG.debug("User or password error!"+nameCheck.getPasswd()+", "+passwd);
       auth = false;
     }
   } finally {
     commitTransaction();
   }
   return auth;
 }
 
 //utilities for authentication and authorization, liulichao
 public MUser getMUser(String userName) {
   MUser muser = null;
   boolean commited = false;
   try {
     openTransaction();
     Query query = pm.newQuery(MUser.class, "userName == t1");
     query.declareParameters("java.lang.String t1");
     query.setUnique(true);
     muser = (MUser) query.execute(userName);
     pm.retrieve(muser);
     commited = commitTransaction();
   } finally {
     if (!commited) {
       rollbackTransaction();
     }
   }
   return muser;
 }
 
   private MRoleMap getMSecurityUserRoleMap(String userName,
       PrincipalType principalType, String roleName) {
     MRoleMap mRoleMember = null;
     boolean commited = false;
     try {
       openTransaction();
       Query query = pm.newQuery(MRoleMap.class, "principalName == t1 && principalType == t2 && role.roleName == t3");
       query.declareParameters("java.lang.String t1, java.lang.String t2, java.lang.String t3");
       query.setUnique(true);
       mRoleMember = (MRoleMap) query.executeWithArray(userName, principalType.toString(), roleName);
       pm.retrieve(mRoleMember);
       commited = commitTransaction();
     } finally {
       if (!commited) {
         rollbackTransaction();
       }
     }
     return mRoleMember;
   }
 
   @Override
   public boolean removeRole(String roleName) throws MetaException,
       NoSuchObjectException {
     boolean success = false;
     try {
       openTransaction();
       MRole mRol = getMRole(roleName);
       pm.retrieve(mRol);
       if (mRol != null) {
         // first remove all the membership, the membership that this role has
         // been granted
         List<MRoleMap> roleMap = listRoleMembers(mRol);
         if (roleMap.size() > 0) {
           pm.deletePersistentAll(roleMap);
         }
         List<MRoleMap> roleMember = listMSecurityPrincipalMembershipRole(mRol
             .getRoleName(), PrincipalType.ROLE);
         if (roleMember.size() > 0) {
           pm.deletePersistentAll(roleMember);
         }
         // then remove all the grants
         List<MGlobalPrivilege> userGrants = listPrincipalGlobalGrants(
             mRol.getRoleName(), PrincipalType.ROLE);
         if (userGrants.size() > 0) {
           pm.deletePersistentAll(userGrants);
         }
         List<MDBPrivilege> dbGrants = listPrincipalAllDBGrant(mRol
             .getRoleName(), PrincipalType.ROLE);
         if (dbGrants.size() > 0) {
           pm.deletePersistentAll(dbGrants);
         }
         List<MTablePrivilege> tabPartGrants = listPrincipalAllTableGrants(
             mRol.getRoleName(), PrincipalType.ROLE);
         if (tabPartGrants.size() > 0) {
           pm.deletePersistentAll(tabPartGrants);
         }
         List<MPartitionPrivilege> partGrants = listPrincipalAllPartitionGrants(
             mRol.getRoleName(), PrincipalType.ROLE);
         if (partGrants.size() > 0) {
           pm.deletePersistentAll(partGrants);
         }
         List<MTableColumnPrivilege> tblColumnGrants = listPrincipalAllTableColumnGrants(
             mRol.getRoleName(), PrincipalType.ROLE);
         if (tblColumnGrants.size() > 0) {
           pm.deletePersistentAll(tblColumnGrants);
         }
         List<MPartitionColumnPrivilege> partColumnGrants = listPrincipalAllPartitionColumnGrants(
             mRol.getRoleName(), PrincipalType.ROLE);
         if (tblColumnGrants.size() > 0) {
           pm.deletePersistentAll(partColumnGrants);
         }
         // finally remove the role
         pm.deletePersistent(mRol);
       }
       success = commitTransaction();
     } finally {
       if (!success) {
         rollbackTransaction();
       }
     }
     return success;
   }
 
   private List<MRoleMap> listRoles(String userName,
       List<String> groupNames) {
     List<MRoleMap> ret = new ArrayList<MRoleMap>();
     if(userName != null) {
       ret.addAll(listRoles(userName, PrincipalType.USER));
     }
     if (groupNames != null) {
       for (String groupName: groupNames) {
         ret.addAll(listRoles(groupName, PrincipalType.GROUP));
       }
     }
     return ret;
   }
 
   @SuppressWarnings("unchecked")
   @Override
   public List<MRoleMap> listRoles(String principalName,
       PrincipalType principalType) {
     boolean success = false;
     List<MRoleMap> mRoleMember = null;
     try {
       openTransaction();
       LOG.debug("Executing listRoles");
       Query query = pm
           .newQuery(
               MRoleMap.class,
               "principalName == t1 && principalType == t2");
       query
           .declareParameters("java.lang.String t1, java.lang.String t2");
       query.setUnique(false);
       mRoleMember = (List<MRoleMap>) query.executeWithArray(
           principalName, principalType.toString());
       LOG.debug("Done executing query for listMSecurityUserRoleMap");
       pm.retrieveAll(mRoleMember);
       success = commitTransaction();
       LOG.debug("Done retrieving all objects for listMSecurityUserRoleMap");
     } finally {
       if (!success) {
         rollbackTransaction();
       }
     }
     return mRoleMember;
   }
 
   @SuppressWarnings("unchecked")
   private List<MRoleMap> listMSecurityPrincipalMembershipRole(final String roleName,
       final PrincipalType principalType) {
     boolean success = false;
     List<MRoleMap> mRoleMemebership = null;
     try {
       openTransaction();
       LOG.debug("Executing listMSecurityPrincipalMembershipRole");
       Query query = pm.newQuery(MRoleMap.class,
           "principalName == t1 && principalType == t2");
       query
           .declareParameters("java.lang.String t1, java.lang.String t2");
       mRoleMemebership = (List<MRoleMap>) query.execute(roleName, principalType.toString());
       LOG
           .debug("Done executing query for listMSecurityPrincipalMembershipRole");
       pm.retrieveAll(mRoleMemebership);
       success = commitTransaction();
       LOG
           .debug("Done retrieving all objects for listMSecurityPrincipalMembershipRole");
     } finally {
       if (!success) {
         rollbackTransaction();
       }
     }
     return mRoleMemebership;
   }
 
   public Role getRole(String roleName) throws NoSuchObjectException {
     MRole mRole = this.getMRole(roleName);
     if (mRole == null) {
       throw new NoSuchObjectException(roleName + " role can not be found.");
     }
     Role ret = new Role(mRole.getRoleName(), mRole.getCreateTime(), mRole
         .getOwnerName());
     return ret;
   }
 
   private MRole getMRole(String roleName) {
     MRole mrole = null;
     boolean commited = false;
     try {
       openTransaction();
       Query query = pm.newQuery(MRole.class, "roleName == t1");
       query.declareParameters("java.lang.String t1");
       query.setUnique(true);
       mrole = (MRole) query.execute(roleName);
       pm.retrieve(mrole);
       commited = commitTransaction();
     } finally {
       if (!commited) {
         rollbackTransaction();
       }
     }
     return mrole;
   }
 
   public List<String> listRoleNames() {
     boolean success = false;
     try {
       openTransaction();
       LOG.debug("Executing listAllRoleNames");
       Query query = pm.newQuery("select roleName from org.apache.hadoop.hive.metastore.model.MRole");
       query.setResult("roleName");
       Collection names = (Collection) query.execute();
       List<String> roleNames  = new ArrayList<String>();
       for (Iterator i = names.iterator(); i.hasNext();) {
         roleNames.add((String) i.next());
       }
       success = commitTransaction();
       return roleNames;
     } finally {
       if (!success) {
         rollbackTransaction();
       }
     }
   }
 
   @Override
   public PrincipalPrivilegeSet getUserPrivilegeSet(String userName,
       List<String> groupNames) throws InvalidObjectException, MetaException {
     boolean commited = false;
     PrincipalPrivilegeSet ret = new PrincipalPrivilegeSet();
     try {
       openTransaction();
       if (userName != null) {
         List<MGlobalPrivilege> user = this.listPrincipalGlobalGrants(userName, PrincipalType.USER);
         if(user.size()>0) {
           Map<String, List<PrivilegeGrantInfo>> userPriv = new HashMap<String, List<PrivilegeGrantInfo>>();
           List<PrivilegeGrantInfo> grantInfos = new ArrayList<PrivilegeGrantInfo>(user.size());
           for (int i = 0; i < user.size(); i++) {
             MGlobalPrivilege item = user.get(i);
             grantInfos.add(new PrivilegeGrantInfo(item.getPrivilege(), item
                 .getCreateTime(), item.getGrantor(), getPrincipalTypeFromStr(item
                 .getGrantorType()), item.getGrantOption()));
           }
           userPriv.put(userName, grantInfos);
           ret.setUserPrivileges(userPriv);
         }
       }
       if (groupNames != null && groupNames.size() > 0) {
         Map<String, List<PrivilegeGrantInfo>> groupPriv = new HashMap<String, List<PrivilegeGrantInfo>>();
         for(String groupName: groupNames) {
           List<MGlobalPrivilege> group = this.listPrincipalGlobalGrants(groupName, PrincipalType.GROUP);
           if(group.size()>0) {
             List<PrivilegeGrantInfo> grantInfos = new ArrayList<PrivilegeGrantInfo>(group.size());
             for (int i = 0; i < group.size(); i++) {
               MGlobalPrivilege item = group.get(i);
               grantInfos.add(new PrivilegeGrantInfo(item.getPrivilege(), item
                   .getCreateTime(), item.getGrantor(), getPrincipalTypeFromStr(item
                   .getGrantorType()), item.getGrantOption()));
             }
             groupPriv.put(groupName, grantInfos);
           }
         }
         ret.setGroupPrivileges(groupPriv);
       }
       commited = commitTransaction();
     } finally {
       if (!commited) {
         rollbackTransaction();
       }
     }
     return ret;
   }
 
   public List<PrivilegeGrantInfo> getDBPrivilege(String dbName,
       String principalName, PrincipalType principalType)
       throws InvalidObjectException, MetaException {
     dbName = dbName.toLowerCase().trim();
 
     if (principalName != null) {
       List<MDBPrivilege> userNameDbPriv = this.listPrincipalDBGrants(
           principalName, principalType, dbName);
       if (userNameDbPriv != null && userNameDbPriv.size() > 0) {
         List<PrivilegeGrantInfo> grantInfos = new ArrayList<PrivilegeGrantInfo>(
             userNameDbPriv.size());
         for (int i = 0; i < userNameDbPriv.size(); i++) {
           MDBPrivilege item = userNameDbPriv.get(i);
           grantInfos.add(new PrivilegeGrantInfo(item.getPrivilege(), item
               .getCreateTime(), item.getGrantor(), getPrincipalTypeFromStr(item
               .getGrantorType()), item.getGrantOption()));
         }
         return grantInfos;
       }
     }
     return new ArrayList<PrivilegeGrantInfo>(0);
   }
 
 
   @Override
   public PrincipalPrivilegeSet getDBPrivilegeSet(String dbName,
       String userName, List<String> groupNames) throws InvalidObjectException,
       MetaException {
     boolean commited = false;
     dbName = dbName.toLowerCase().trim();
 
     PrincipalPrivilegeSet ret = new PrincipalPrivilegeSet();
     try {
       openTransaction();
       if (userName != null) {
         Map<String, List<PrivilegeGrantInfo>> dbUserPriv = new HashMap<String, List<PrivilegeGrantInfo>>();
         dbUserPriv.put(userName, getDBPrivilege(dbName, userName,
             PrincipalType.USER));
         ret.setUserPrivileges(dbUserPriv);
       }
       if (groupNames != null && groupNames.size() > 0) {
         Map<String, List<PrivilegeGrantInfo>> dbGroupPriv = new HashMap<String, List<PrivilegeGrantInfo>>();
         for (String groupName : groupNames) {
           dbGroupPriv.put(groupName, getDBPrivilege(dbName, groupName,
               PrincipalType.GROUP));
         }
         ret.setGroupPrivileges(dbGroupPriv);
       }
       List<MRoleMap> roles = listRoles(userName, groupNames);
       if (roles != null && roles.size() > 0) {
         Map<String, List<PrivilegeGrantInfo>> dbRolePriv = new HashMap<String, List<PrivilegeGrantInfo>>();
         for (MRoleMap role : roles) {
           String name = role.getRole().getRoleName();
           dbRolePriv
               .put(name, getDBPrivilege(dbName, name, PrincipalType.ROLE));
         }
         ret.setRolePrivileges(dbRolePriv);
       }
       commited = commitTransaction();
     } finally {
       if (!commited) {
         rollbackTransaction();
       }
     }
     return ret;
   }
 
   @Override
   public PrincipalPrivilegeSet getPartitionPrivilegeSet(String dbName,
       String tableName, String partition, String userName,
       List<String> groupNames) throws InvalidObjectException, MetaException {
     boolean commited = false;
     PrincipalPrivilegeSet ret = new PrincipalPrivilegeSet();
     tableName = tableName.toLowerCase().trim();
     dbName = dbName.toLowerCase().trim();
 
     try {
       openTransaction();
       if (userName != null) {
         Map<String, List<PrivilegeGrantInfo>> partUserPriv = new HashMap<String, List<PrivilegeGrantInfo>>();
         partUserPriv.put(userName, getPartitionPrivilege(dbName,
             tableName, partition, userName, PrincipalType.USER));
         ret.setUserPrivileges(partUserPriv);
       }
       if (groupNames != null && groupNames.size() > 0) {
         Map<String, List<PrivilegeGrantInfo>> partGroupPriv = new HashMap<String, List<PrivilegeGrantInfo>>();
         for (String groupName : groupNames) {
           partGroupPriv.put(groupName, getPartitionPrivilege(dbName, tableName,
               partition, groupName, PrincipalType.GROUP));
         }
         ret.setGroupPrivileges(partGroupPriv);
       }
       List<MRoleMap> roles = listRoles(userName, groupNames);
       if (roles != null && roles.size() > 0) {
         Map<String, List<PrivilegeGrantInfo>> partRolePriv = new HashMap<String, List<PrivilegeGrantInfo>>();
         for (MRoleMap role : roles) {
           String roleName = role.getRole().getRoleName();
           partRolePriv.put(roleName, getPartitionPrivilege(dbName, tableName,
               partition, roleName, PrincipalType.ROLE));
         }
         ret.setRolePrivileges(partRolePriv);
       }
       commited = commitTransaction();
     } finally {
       if (!commited) {
         rollbackTransaction();
       }
     }
     return ret;
   }
 
   @Override
   public PrincipalPrivilegeSet getTablePrivilegeSet(String dbName,
       String tableName, String userName, List<String> groupNames)
       throws InvalidObjectException, MetaException {
     boolean commited = false;
     PrincipalPrivilegeSet ret = new PrincipalPrivilegeSet();
     tableName = tableName.toLowerCase().trim();
     dbName = dbName.toLowerCase().trim();
 
     try {
       openTransaction();
       if (userName != null) {
         Map<String, List<PrivilegeGrantInfo>> tableUserPriv = new HashMap<String, List<PrivilegeGrantInfo>>();
         tableUserPriv.put(userName, getTablePrivilege(dbName,
             tableName, userName, PrincipalType.USER));
         ret.setUserPrivileges(tableUserPriv);
       }
       if (groupNames != null && groupNames.size() > 0) {
         Map<String, List<PrivilegeGrantInfo>> tableGroupPriv = new HashMap<String, List<PrivilegeGrantInfo>>();
         for (String groupName : groupNames) {
           tableGroupPriv.put(groupName, getTablePrivilege(dbName, tableName,
               groupName, PrincipalType.GROUP));
         }
         ret.setGroupPrivileges(tableGroupPriv);
       }
       List<MRoleMap> roles = listRoles(userName, groupNames);
       if (roles != null && roles.size() > 0) {
         Map<String, List<PrivilegeGrantInfo>> tableRolePriv = new HashMap<String, List<PrivilegeGrantInfo>>();
         for (MRoleMap role : roles) {
           String roleName = role.getRole().getRoleName();
           tableRolePriv.put(roleName, getTablePrivilege(dbName, tableName,
               roleName, PrincipalType.ROLE));
         }
         ret.setRolePrivileges(tableRolePriv);
       }
       commited = commitTransaction();
     } finally {
       if (!commited) {
         rollbackTransaction();
       }
     }
     return ret;
   }
 
   @Override
   public PrincipalPrivilegeSet getColumnPrivilegeSet(String dbName,
       String tableName, String partitionName, String columnName,
       String userName, List<String> groupNames) throws InvalidObjectException,
       MetaException {
     tableName = tableName.toLowerCase().trim();
     dbName = dbName.toLowerCase().trim();
     columnName = columnName.toLowerCase().trim();
 
     boolean commited = false;
     PrincipalPrivilegeSet ret = new PrincipalPrivilegeSet();
     try {
       openTransaction();
       if (userName != null) {
         Map<String, List<PrivilegeGrantInfo>> columnUserPriv = new HashMap<String, List<PrivilegeGrantInfo>>();
         columnUserPriv.put(userName, getColumnPrivilege(dbName, tableName,
             columnName, partitionName, userName, PrincipalType.USER));
         ret.setUserPrivileges(columnUserPriv);
       }
       if (groupNames != null && groupNames.size() > 0) {
         Map<String, List<PrivilegeGrantInfo>> columnGroupPriv = new HashMap<String, List<PrivilegeGrantInfo>>();
         for (String groupName : groupNames) {
           columnGroupPriv.put(groupName, getColumnPrivilege(dbName, tableName,
               columnName, partitionName, groupName, PrincipalType.GROUP));
         }
         ret.setGroupPrivileges(columnGroupPriv);
       }
       List<MRoleMap> roles = listRoles(userName, groupNames);
       if (roles != null && roles.size() > 0) {
         Map<String, List<PrivilegeGrantInfo>> columnRolePriv = new HashMap<String, List<PrivilegeGrantInfo>>();
         for (MRoleMap role : roles) {
           String roleName = role.getRole().getRoleName();
           columnRolePriv.put(roleName, getColumnPrivilege(dbName, tableName,
               columnName, partitionName, roleName, PrincipalType.ROLE));
         }
         ret.setRolePrivileges(columnRolePriv);
       }
       commited = commitTransaction();
     } finally {
       if (!commited) {
         rollbackTransaction();
       }
     }
     return ret;
   }
 
   private List<PrivilegeGrantInfo> getPartitionPrivilege(String dbName,
       String tableName, String partName, String principalName,
       PrincipalType principalType) {
 
     tableName = tableName.toLowerCase().trim();
     dbName = dbName.toLowerCase().trim();
 
     if (principalName != null) {
       List<MPartitionPrivilege> userNameTabPartPriv = this
           .listPrincipalPartitionGrants(principalName, principalType,
               dbName, tableName, partName);
       if (userNameTabPartPriv != null && userNameTabPartPriv.size() > 0) {
         List<PrivilegeGrantInfo> grantInfos = new ArrayList<PrivilegeGrantInfo>(
             userNameTabPartPriv.size());
         for (int i = 0; i < userNameTabPartPriv.size(); i++) {
           MPartitionPrivilege item = userNameTabPartPriv.get(i);
           grantInfos.add(new PrivilegeGrantInfo(item.getPrivilege(), item
               .getCreateTime(), item.getGrantor(),
               getPrincipalTypeFromStr(item.getGrantorType()), item.getGrantOption()));
 
         }
         return grantInfos;
       }
     }
     return new ArrayList<PrivilegeGrantInfo>(0);
   }
 
   private PrincipalType getPrincipalTypeFromStr(String str) {
     return str == null ? null : PrincipalType.valueOf(str);
   }
 
   private List<PrivilegeGrantInfo> getTablePrivilege(String dbName,
       String tableName, String principalName, PrincipalType principalType) {
     tableName = tableName.toLowerCase().trim();
     dbName = dbName.toLowerCase().trim();
 
     if (principalName != null) {
       List<MTablePrivilege> userNameTabPartPriv = this
           .listAllTableGrants(principalName, principalType,
               dbName, tableName);
       if (userNameTabPartPriv != null && userNameTabPartPriv.size() > 0) {
         List<PrivilegeGrantInfo> grantInfos = new ArrayList<PrivilegeGrantInfo>(
             userNameTabPartPriv.size());
         for (int i = 0; i < userNameTabPartPriv.size(); i++) {
           MTablePrivilege item = userNameTabPartPriv.get(i);
           grantInfos.add(new PrivilegeGrantInfo(item.getPrivilege(), item
               .getCreateTime(), item.getGrantor(), getPrincipalTypeFromStr(item
               .getGrantorType()), item.getGrantOption()));
         }
         return grantInfos;
       }
     }
     return new ArrayList<PrivilegeGrantInfo>(0);
   }
 
   private List<PrivilegeGrantInfo> getColumnPrivilege(String dbName,
       String tableName, String columnName, String partitionName,
       String principalName, PrincipalType principalType) {
 
     tableName = tableName.toLowerCase().trim();
     dbName = dbName.toLowerCase().trim();
     columnName = columnName.toLowerCase().trim();
 
     if (partitionName == null) {
       List<MTableColumnPrivilege> userNameColumnPriv = this
           .listPrincipalTableColumnGrants(principalName, principalType,
               dbName, tableName, columnName);
       if (userNameColumnPriv != null && userNameColumnPriv.size() > 0) {
         List<PrivilegeGrantInfo> grantInfos = new ArrayList<PrivilegeGrantInfo>(
             userNameColumnPriv.size());
         for (int i = 0; i < userNameColumnPriv.size(); i++) {
           MTableColumnPrivilege item = userNameColumnPriv.get(i);
           grantInfos.add(new PrivilegeGrantInfo(item.getPrivilege(), item
               .getCreateTime(), item.getGrantor(), getPrincipalTypeFromStr(item
               .getGrantorType()), item.getGrantOption()));
         }
         return grantInfos;
       }
     } else {
       List<MPartitionColumnPrivilege> userNameColumnPriv = this
           .listPrincipalPartitionColumnGrants(principalName,
               principalType, dbName, tableName, partitionName, columnName);
       if (userNameColumnPriv != null && userNameColumnPriv.size() > 0) {
         List<PrivilegeGrantInfo> grantInfos = new ArrayList<PrivilegeGrantInfo>(
             userNameColumnPriv.size());
         for (int i = 0; i < userNameColumnPriv.size(); i++) {
           MPartitionColumnPrivilege item = userNameColumnPriv.get(i);
           grantInfos.add(new PrivilegeGrantInfo(item.getPrivilege(), item
               .getCreateTime(), item.getGrantor(), getPrincipalTypeFromStr(item
               .getGrantorType()), item.getGrantOption()));
         }
         return grantInfos;
       }
     }
     return new ArrayList<PrivilegeGrantInfo>(0);
   }
 
   @Override
   public boolean grantPrivileges(PrivilegeBag privileges) throws InvalidObjectException,
       MetaException, NoSuchObjectException {
     boolean committed = false;
     int now = (int) (System.currentTimeMillis() / 1000);
     try {
       openTransaction();
       List<Object> persistentObjs = new ArrayList<Object>();
 
       List<HiveObjectPrivilege> privilegeList = privileges.getPrivileges();
 
       if (privilegeList != null && privilegeList.size() > 0) {
         Iterator<HiveObjectPrivilege> privIter = privilegeList.iterator();
         Set<String> privSet = new HashSet<String>();
         while (privIter.hasNext()) {
           HiveObjectPrivilege privDef = privIter.next();
           HiveObjectRef hiveObject = privDef.getHiveObject();
           String privilegeStr = privDef.getGrantInfo().getPrivilege();
           String[] privs = privilegeStr.split(",");
           String userName = privDef.getPrincipalName();
           PrincipalType principalType = privDef.getPrincipalType();
           String grantor = privDef.getGrantInfo().getGrantor();
           String grantorType = privDef.getGrantInfo().getGrantorType().toString();
           boolean grantOption = privDef.getGrantInfo().isGrantOption();
           privSet.clear();
 
           if (hiveObject.getObjectType() == HiveObjectType.GLOBAL) {
             List<MGlobalPrivilege> globalPrivs = this
                 .listPrincipalGlobalGrants(userName, principalType);
             if (globalPrivs != null) {
               for (MGlobalPrivilege priv : globalPrivs) {
                 if (priv.getGrantor().equalsIgnoreCase(grantor)) {
                   privSet.add(priv.getPrivilege());
                 }
               }
             }
             for (String privilege : privs) {
               if (privSet.contains(privilege)) {
                 throw new InvalidObjectException(privilege
                     + " is already granted by " + grantor);
               }
               MGlobalPrivilege mGlobalPrivs = new MGlobalPrivilege(userName,
                   principalType.toString(), privilege, now, grantor, grantorType, grantOption);
               persistentObjs.add(mGlobalPrivs);
             }
           } else if (hiveObject.getObjectType() == HiveObjectType.DATABASE) {
             MDatabase dbObj = getMDatabase(hiveObject.getDbName());
             if (dbObj != null) {
               List<MDBPrivilege> dbPrivs = this.listPrincipalDBGrants(
                   userName, principalType, hiveObject.getDbName());
               if (dbPrivs != null) {
                 for (MDBPrivilege priv : dbPrivs) {
                   if (priv.getGrantor().equalsIgnoreCase(grantor)) {
                     privSet.add(priv.getPrivilege());
                   }
                 }
               }
               for (String privilege : privs) {
                 if (privSet.contains(privilege)) {
                   throw new InvalidObjectException(privilege
                       + " is already granted on database "
                       + hiveObject.getDbName() + " by " + grantor);
                 }
                 MDBPrivilege mDb = new MDBPrivilege(userName, principalType
                     .toString(), dbObj, privilege, now, grantor, grantorType, grantOption);
                 persistentObjs.add(mDb);
               }
             }
           } else if (hiveObject.getObjectType() == HiveObjectType.TABLE) {
             MTable tblObj = getMTable(hiveObject.getDbName(), hiveObject
                 .getObjectName());
             if (tblObj != null) {
               List<MTablePrivilege> tablePrivs = this
                   .listAllTableGrants(userName, principalType,
                       hiveObject.getDbName(), hiveObject.getObjectName());
               if (tablePrivs != null) {
                 for (MTablePrivilege priv : tablePrivs) {
                   if (priv.getGrantor() != null
                       && priv.getGrantor().equalsIgnoreCase(grantor)) {
                     privSet.add(priv.getPrivilege());
                   }
                 }
               }
               for (String privilege : privs) {
                 if (privSet.contains(privilege)) {
                   throw new InvalidObjectException(privilege
                       + " is already granted on table ["
                       + hiveObject.getDbName() + ","
                       + hiveObject.getObjectName() + "] by " + grantor);
                 }
                 MTablePrivilege mTab = new MTablePrivilege(
                     userName, principalType.toString(), tblObj,
                     privilege, now, grantor, grantorType, grantOption);
                 persistentObjs.add(mTab);
               }
             }
           }  else if (hiveObject.getObjectType() == HiveObjectType.SCHEMA) {
             MSchema schemaObj = getMSchema(hiveObject.getObjectName());
             if (schemaObj != null) {
               List<MSchemaPrivilege> schemaPrivs = this
                   .listAllSchemaGrants(userName, principalType, hiveObject.getObjectName());
               if (schemaPrivs != null) {
                 for (MSchemaPrivilege priv : schemaPrivs) {
                   if (priv.getGrantor() != null
                       && priv.getGrantor().equalsIgnoreCase(grantor)) {
                     privSet.add(priv.getPrivilege());
                   }
                 }
               }
               for (String privilege : privs) {
                 if (privSet.contains(privilege)) {
                   throw new InvalidObjectException(privilege
                       + " is already granted on schema ["
                       + hiveObject.getObjectName() + "] by " + grantor);
                 }
                 MSchemaPrivilege mSch = new MSchemaPrivilege(
                     userName, principalType.toString(), schemaObj,
                     privilege, now, grantor, grantorType, grantOption);
                 persistentObjs.add(mSch);
               }
             }
           } else if (hiveObject.getObjectType() == HiveObjectType.PARTITION) {
             // TODO: fix it
             MPartition partObj = this.getMPartition(hiveObject.getDbName(),
                 hiveObject.getObjectName(), hiveObject.getPartValues().toString());
             String partName = null;
             if (partObj != null) {
               partName = partObj.getPartitionName();
               List<MPartitionPrivilege> partPrivs = this
                   .listPrincipalPartitionGrants(userName,
                       principalType, hiveObject.getDbName(), hiveObject
                           .getObjectName(), partObj.getPartitionName());
               if (partPrivs != null) {
                 for (MPartitionPrivilege priv : partPrivs) {
                   if (priv.getGrantor().equalsIgnoreCase(grantor)) {
                     privSet.add(priv.getPrivilege());
                   }
                 }
               }
               for (String privilege : privs) {
                 if (privSet.contains(privilege)) {
                   throw new InvalidObjectException(privilege
                       + " is already granted on partition ["
                       + hiveObject.getDbName() + ","
                       + hiveObject.getObjectName() + ","
                       + partName + "] by " + grantor);
                 }
                 MPartitionPrivilege mTab = new MPartitionPrivilege(userName,
                     principalType.toString(), partObj, privilege, now, grantor,
                     grantorType, grantOption);
                 persistentObjs.add(mTab);
               }
             }
           } else if (hiveObject.getObjectType() == HiveObjectType.COLUMN) {
             MTable tblObj = getMTable(hiveObject.getDbName(), hiveObject
                 .getObjectName());
             if (tblObj != null) {
               if (hiveObject.getPartValues() != null) {
                 MPartition partObj = null;
                 List<MPartitionColumnPrivilege> colPrivs = null;
                 // TODO: fix it
                 partObj = this.getMPartition(hiveObject.getDbName(), hiveObject
                     .getObjectName(), hiveObject.getPartValues().toString());
                 if (partObj == null) {
                   continue;
                 }
                 colPrivs = this.listPrincipalPartitionColumnGrants(
                     userName, principalType, hiveObject.getDbName(), hiveObject
                         .getObjectName(), partObj.getPartitionName(),
                     hiveObject.getColumnName());
 
                 if (colPrivs != null) {
                   for (MPartitionColumnPrivilege priv : colPrivs) {
                     if (priv.getGrantor().equalsIgnoreCase(grantor)) {
                       privSet.add(priv.getPrivilege());
                     }
                   }
                 }
                 for (String privilege : privs) {
                   if (privSet.contains(privilege)) {
                     throw new InvalidObjectException(privilege
                         + " is already granted on column "
                         + hiveObject.getColumnName() + " ["
                         + hiveObject.getDbName() + ","
                         + hiveObject.getObjectName() + ","
                         + partObj.getPartitionName() + "] by " + grantor);
                   }
                   MPartitionColumnPrivilege mCol = new MPartitionColumnPrivilege(userName,
                       principalType.toString(), partObj, hiveObject
                           .getColumnName(), privilege, now, grantor, grantorType,
                       grantOption);
                   persistentObjs.add(mCol);
                 }
 
               } else {
                 List<MTableColumnPrivilege> colPrivs = null;
                 colPrivs = this.listPrincipalTableColumnGrants(
                     userName, principalType, hiveObject.getDbName(), hiveObject
                         .getObjectName(), hiveObject.getColumnName());
 
                 if (colPrivs != null) {
                   for (MTableColumnPrivilege priv : colPrivs) {
                     if (priv.getGrantor().equalsIgnoreCase(grantor)) {
                       privSet.add(priv.getPrivilege());
                     }
                   }
                 }
                 for (String privilege : privs) {
                   if (privSet.contains(privilege)) {
                     throw new InvalidObjectException(privilege
                         + " is already granted on column "
                         + hiveObject.getColumnName() + " ["
                         + hiveObject.getDbName() + ","
                         + hiveObject.getObjectName() + "] by " + grantor);
                   }
                   MTableColumnPrivilege mCol = new MTableColumnPrivilege(userName,
                       principalType.toString(), tblObj, hiveObject
                           .getColumnName(), privilege, now, grantor, grantorType,
                       grantOption);
                   persistentObjs.add(mCol);
                 }
               }
             }
           }
         }
       }
       if (persistentObjs.size() > 0) {
         pm.makePersistentAll(persistentObjs);
       }
       committed = commitTransaction();
 
       //add by zy for msg queue
       if(committed)
       {
         for(Object obj : persistentObjs)
         {
           if(obj instanceof MGlobalPrivilege)
           {
             MGlobalPrivilege mgp = (MGlobalPrivilege)obj;
             HashMap<String,Object> params = new HashMap<String,Object>();
             params.put("grantor", mgp.getGrantor());
             params.put("grantor_type", mgp.getGrantorType());
             params.put("principalName", mgp.getPrincipalName());
             params.put("principal_type", mgp.getPrincipalType());
             params.put("privilege", mgp.getPrivilege());
             params.put("create_time",mgp.getCreateTime());
             params.put("grant_option",mgp.getGrantOption());
 
             MetaMsgServer.sendMsg(MSGFactory.generateDDLMsg(MSGType.MSG_GRANT_GLOBAL,-1l,-1l, pm, mgp,params));
           }
           else if(obj instanceof MDBPrivilege)
           {
             MDBPrivilege mdbp = (MDBPrivilege)obj;
             HashMap<String,Object> params = new HashMap<String,Object>();
             params.put("grantor", mdbp.getGrantor());
             params.put("grantor_type", mdbp.getGrantorType());
             params.put("principalName", mdbp.getPrincipalName());
             params.put("principal_type", mdbp.getPrincipalType());
             params.put("privilege", mdbp.getPrivilege());
             params.put("create_time",mdbp.getCreateTime());
             params.put("grant_option",mdbp.getGrantOption());
             params.put("db_name", mdbp.getDatabase().getName());
             long db_id = Long.parseLong(MSGFactory.getIDFromJdoObjectId(pm.getObjectId(mdbp.getDatabase()).toString()));
             MetaMsgServer.sendMsg(MSGFactory.generateDDLMsg(MSGType.MSG_GRANT_DB,db_id,-1l, pm, mdbp.getDatabase(),params));
           }
 
           else if(obj instanceof MTablePrivilege)
           {
             MTablePrivilege mtp = (MTablePrivilege)obj;
             HashMap<String,Object> params = new HashMap<String,Object>();
             params.put("grantor", mtp.getGrantor());
             params.put("grantor_type", mtp.getGrantorType());
             params.put("principalName", mtp.getPrincipalName());
             params.put("principal_type", mtp.getPrincipalType());
             params.put("privilege", mtp.getPrivilege());
             params.put("create_time",mtp.getCreateTime());
             params.put("grant_option",mtp.getGrantOption());
             params.put("table_name", mtp.getTable().getTableName());
             params.put("db_name", mtp.getTable().getDatabase().getName());
             long db_id = Long.parseLong(MSGFactory.getIDFromJdoObjectId(pm.getObjectId(mtp.getTable().getDatabase()).toString()));
             MetaMsgServer.sendMsg(MSGFactory.generateDDLMsg(MSGType.MSG_GRANT_TABLE,db_id,-1l, pm, mtp.getTable(),params));
           }
           else if(obj instanceof MSchemaPrivilege)
           {
             MSchemaPrivilege msp = (MSchemaPrivilege)obj;
             HashMap<String,Object> params = new HashMap<String,Object>();
             params.put("grantor", msp.getGrantor());
             params.put("grantor_type", msp.getGrantorType());
             params.put("principalName", msp.getPrincipalName());
             params.put("principal_type", msp.getPrincipalType());
             params.put("privilege", msp.getPrivilege());
             params.put("create_time",msp.getCreateTime());
             params.put("grant_option",msp.getGrantOption());
             params.put("schema_name", msp.getSchema().getSchemaName());
             MetaMsgServer.sendMsg(MSGFactory.generateDDLMsg(MSGType.MSG_GRANT_SCHEMA,-1l,-1l, pm, msp.getSchema(),params));
           }
           else if(obj instanceof MPartitionPrivilege)
           {
             MPartitionPrivilege mpp = (MPartitionPrivilege)obj;
             HashMap<String,Object> params = new HashMap<String,Object>();
             params.put("grantor", mpp.getGrantor());
             params.put("grantor_type", mpp.getGrantorType());
             params.put("principalName", mpp.getPrincipalName());
             params.put("principal_type", mpp.getPrincipalType());
             params.put("privilege", mpp.getPrivilege());
             params.put("create_time",mpp.getCreateTime());
             params.put("grant_option",mpp.getGrantOption());
             params.put("partition_name", mpp.getPartition().getPartitionName());
             params.put("table_name", mpp.getPartition().getTable().getTableName());
             params.put("db_name", mpp.getPartition().getTable().getDatabase().getName());
             long db_id = Long.parseLong(MSGFactory.getIDFromJdoObjectId(pm.getObjectId(mpp.getPartition().getTable().getDatabase()).toString()));
             MetaMsgServer.sendMsg(MSGFactory.generateDDLMsg(MSGType.MSG_GRANT_PARTITION,db_id,-1l, pm, mpp.getPartition(),params));
           }
           else if(obj instanceof MPartitionColumnPrivilege)
           {
             MPartitionColumnPrivilege mpcp = (MPartitionColumnPrivilege)obj;
             HashMap<String,Object> params = new HashMap<String,Object>();
             params.put("grantor", mpcp.getGrantor());
             params.put("grantor_type", mpcp.getGrantorType());
             params.put("principalName", mpcp.getPrincipalName());
             params.put("principal_type", mpcp.getPrincipalType());
             params.put("privilege", mpcp.getPrivilege());
             params.put("create_time",mpcp.getCreateTime());
             params.put("grant_option",mpcp.getGrantOption());
             params.put("column_name", mpcp.getColumnName());
             params.put("partition_name", mpcp.getPartition().getPartitionName());
             params.put("table_name", mpcp.getPartition().getTable().getTableName());
             params.put("db_name", mpcp.getPartition().getTable().getDatabase().getName());
             long db_id = Long.parseLong(MSGFactory.getIDFromJdoObjectId(pm.getObjectId(mpcp.getPartition().getTable().getDatabase()).toString()));
             MetaMsgServer.sendMsg(MSGFactory.generateDDLMsg(MSGType.MSG_GRANT_PARTITION_COLUMN,db_id,-1l, pm, mpcp.getPartition().getTable().getSd().getCD(),params));
           }
           else if(obj instanceof MTableColumnPrivilege)
           {
             MTableColumnPrivilege mtcp = (MTableColumnPrivilege)obj;
             HashMap<String,Object> params = new HashMap<String,Object>();
             params.put("grantor", mtcp.getGrantor());
             params.put("grantor_type", mtcp.getGrantorType());
             params.put("principalName", mtcp.getPrincipalName());
             params.put("principal_type", mtcp.getPrincipalType());
             params.put("privilege", mtcp.getPrivilege());
             params.put("create_time",mtcp.getCreateTime());
             params.put("grant_option",mtcp.getGrantOption());
             params.put("column_name", mtcp.getColumnName());
             params.put("table_name", mtcp.getTable().getTableName());
             params.put("db_name", mtcp.getTable().getDatabase().getName());
             long db_id = Long.parseLong(MSGFactory.getIDFromJdoObjectId(pm.getObjectId(mtcp.getTable().getDatabase()).toString()));
             MetaMsgServer.sendMsg(MSGFactory.generateDDLMsg(MSGType.MSG_GRANT_TABLE_COLUMN,db_id,-1l, pm, mtcp.getTable().getSd().getCD(),params));
           }
         }
       }
     } finally {
       if (!committed) {
         rollbackTransaction();
       }
     }
     return committed;
   }
 
   @Override
   public boolean revokePrivileges(PrivilegeBag privileges)
       throws InvalidObjectException, MetaException, NoSuchObjectException {
     boolean committed = false;
     try {
       openTransaction();
       List<Object> persistentObjs = new ArrayList<Object>();
 
       List<HiveObjectPrivilege> privilegeList = privileges.getPrivileges();
 
 
       if (privilegeList != null && privilegeList.size() > 0) {
         Iterator<HiveObjectPrivilege> privIter = privilegeList.iterator();
 
         while (privIter.hasNext()) {
           HiveObjectPrivilege privDef = privIter.next();
           HiveObjectRef hiveObject = privDef.getHiveObject();
           String privilegeStr = privDef.getGrantInfo().getPrivilege();
           if (privilegeStr == null || privilegeStr.trim().equals("")) {
             continue;
           }
           String[] privs = privilegeStr.split(",");
           String userName = privDef.getPrincipalName();
           PrincipalType principalType = privDef.getPrincipalType();
 
           if (hiveObject.getObjectType() == HiveObjectType.GLOBAL) {
             List<MGlobalPrivilege> mSecUser = this.listPrincipalGlobalGrants(
                 userName, principalType);
             boolean found = false;
             if (mSecUser != null) {
               for (String privilege : privs) {
                 for (MGlobalPrivilege userGrant : mSecUser) {
                   String userGrantPrivs = userGrant.getPrivilege();
                   if (privilege.equals(userGrantPrivs)) {
                     found = true;
                     persistentObjs.add(userGrant);
                     break;
                   }
                 }
                 if (!found) {
                   throw new InvalidObjectException(
                       "No user grant found for privileges " + privilege);
                 }
               }
             }
 
           } else if (hiveObject.getObjectType() == HiveObjectType.DATABASE) {
             MDatabase dbObj = getMDatabase(hiveObject.getDbName());
             if (dbObj != null) {
               String db = hiveObject.getDbName();
               boolean found = false;
               List<MDBPrivilege> dbGrants = this.listPrincipalDBGrants(
                   userName, principalType, db);
               for (String privilege : privs) {
                 for (MDBPrivilege dbGrant : dbGrants) {
                   String dbGrantPriv = dbGrant.getPrivilege();
                   if (privilege.equals(dbGrantPriv)) {
                     found = true;
                     persistentObjs.add(dbGrant);
                     break;
                   }
                 }
                 if (!found) {
                   throw new InvalidObjectException(
                       "No database grant found for privileges " + privilege
                           + " on database " + db);
                 }
               }
             }
           } else if (hiveObject.getObjectType() == HiveObjectType.TABLE) {
             boolean found = false;
             List<MTablePrivilege> tableGrants = this
                 .listAllTableGrants(userName, principalType,
                     hiveObject.getDbName(), hiveObject.getObjectName());
             for (String privilege : privs) {
               for (MTablePrivilege tabGrant : tableGrants) {
                 String tableGrantPriv = tabGrant.getPrivilege();
                 if (privilege.equalsIgnoreCase(tableGrantPriv)) {
                   found = true;
                   persistentObjs.add(tabGrant);
                   break;
                 }
               }
               if (!found) {
                 throw new InvalidObjectException("No grant (" + privilege
                     + ") found " + " on table " + hiveObject.getObjectName()
                     + ", database is " + hiveObject.getDbName());
               }
             }
           } else if (hiveObject.getObjectType() == HiveObjectType.PARTITION) {
 
             boolean found = false;
             Table tabObj = this.getTable(hiveObject.getDbName(), hiveObject.getObjectName());
             String partName = null;
             if (hiveObject.getPartValues() != null) {
               partName = Warehouse.makePartName(tabObj.getPartitionKeys(), hiveObject.getPartValues());
             }
             List<MPartitionPrivilege> partitionGrants = this
                 .listPrincipalPartitionGrants(userName, principalType,
                     hiveObject.getDbName(), hiveObject.getObjectName(), partName);
             for (String privilege : privs) {
               for (MPartitionPrivilege partGrant : partitionGrants) {
                 String partPriv = partGrant.getPrivilege();
                 if (partPriv.equalsIgnoreCase(privilege)) {
                   found = true;
                   persistentObjs.add(partGrant);
                   break;
                 }
               }
               if (!found) {
                 throw new InvalidObjectException("No grant (" + privilege
                     + ") found " + " on table " + tabObj.getTableName()
                     + ", partition is " + partName + ", database is " + tabObj.getDbName());
               }
             }
           } else if (hiveObject.getObjectType() == HiveObjectType.COLUMN) {
 
             Table tabObj = this.getTable(hiveObject.getDbName(), hiveObject
                 .getObjectName());
             String partName = null;
             if (hiveObject.getPartValues() != null) {
               partName = Warehouse.makePartName(tabObj.getPartitionKeys(),
                   hiveObject.getPartValues());
             }
 
             if (partName != null) {
               List<MPartitionColumnPrivilege> mSecCol = listPrincipalPartitionColumnGrants(
                   userName, principalType, hiveObject.getDbName(), hiveObject
                       .getObjectName(), partName, hiveObject.getColumnName());
               boolean found = false;
               if (mSecCol != null) {
                 for (String privilege : privs) {
                   for (MPartitionColumnPrivilege col : mSecCol) {
                     String colPriv = col.getPrivilege();
                     if (colPriv.equalsIgnoreCase(privilege)) {
                       found = true;
                       persistentObjs.add(col);
                       break;
                     }
                   }
                   if (!found) {
                     throw new InvalidObjectException("No grant (" + privilege
                         + ") found " + " on table " + tabObj.getTableName()
                         + ", partition is " + partName + ", column name = "
                         + hiveObject.getColumnName() + ", database is "
                         + tabObj.getDbName());
                   }
                 }
               }
             } else {
               List<MTableColumnPrivilege> mSecCol = listPrincipalTableColumnGrants(
                   userName, principalType, hiveObject.getDbName(), hiveObject
                       .getObjectName(), hiveObject.getColumnName());
               boolean found = false;
               if (mSecCol != null) {
                 for (String privilege : privs) {
                   for (MTableColumnPrivilege col : mSecCol) {
                     String colPriv = col.getPrivilege();
                     if (colPriv.equalsIgnoreCase(privilege)) {
                       found = true;
                       persistentObjs.add(col);
                       break;
                     }
                   }
                   if (!found) {
                     throw new InvalidObjectException("No grant (" + privilege
                         + ") found " + " on table " + tabObj.getTableName()
                         + ", column name = "
                         + hiveObject.getColumnName() + ", database is "
                         + tabObj.getDbName());
                   }
                 }
               }
             }
 
           }
         }
       }
 
       if (persistentObjs.size() > 0) {
         pm.deletePersistentAll(persistentObjs);
       }
       committed = commitTransaction();
 
     //add by zy for msg queue
       if(committed)
       {
         for(Object obj : persistentObjs)
         {
           if(obj instanceof MGlobalPrivilege)
           {
             MGlobalPrivilege mgp = (MGlobalPrivilege)obj;
             HashMap<String,Object> params = new HashMap<String,Object>();
             params.put("grantor", mgp.getGrantor());
             params.put("grantor_type", mgp.getGrantorType());
             params.put("principalName", mgp.getPrincipalName());
             params.put("principal_type", mgp.getPrincipalType());
             params.put("privilege", mgp.getPrivilege());
             params.put("create_time",mgp.getCreateTime());
             params.put("grant_option",mgp.getGrantOption());
 
             MetaMsgServer.sendMsg(MSGFactory.generateDDLMsg(MSGType.MSG_REVOKE_GLOBAL,-1l,-1l, pm, mgp,params));
           }
           else if(obj instanceof MDBPrivilege)
           {
             MDBPrivilege mdbp = (MDBPrivilege)obj;
             HashMap<String,Object> params = new HashMap<String,Object>();
             params.put("grantor", mdbp.getGrantor());
             params.put("grantor_type", mdbp.getGrantorType());
             params.put("principalName", mdbp.getPrincipalName());
             params.put("principal_type", mdbp.getPrincipalType());
             params.put("privilege", mdbp.getPrivilege());
             params.put("create_time",mdbp.getCreateTime());
             params.put("grant_option",mdbp.getGrantOption());
             params.put("db_name", mdbp.getDatabase().getName());
             long db_id = Long.parseLong(MSGFactory.getIDFromJdoObjectId(pm.getObjectId(mdbp.getDatabase()).toString()));
             MetaMsgServer.sendMsg(MSGFactory.generateDDLMsg(MSGType.MSG_REVOKE_DB,db_id,-1l, pm, mdbp.getDatabase(),params));
           }
 
           else if(obj instanceof MTablePrivilege)
           {
             MTablePrivilege mtp = (MTablePrivilege)obj;
             HashMap<String,Object> params = new HashMap<String,Object>();
             params.put("grantor", mtp.getGrantor());
             params.put("grantor_type", mtp.getGrantorType());
             params.put("principalName", mtp.getPrincipalName());
             params.put("principal_type", mtp.getPrincipalType());
             params.put("privilege", mtp.getPrivilege());
             params.put("create_time",mtp.getCreateTime());
             params.put("grant_option",mtp.getGrantOption());
             params.put("table_name", mtp.getTable().getTableName());
             params.put("db_name", mtp.getTable().getDatabase().getName());
             long db_id = Long.parseLong(MSGFactory.getIDFromJdoObjectId(pm.getObjectId(mtp.getTable().getDatabase()).toString()));
             MetaMsgServer.sendMsg(MSGFactory.generateDDLMsg(MSGType.MSG_REVOKE_TABLE,db_id,-1l, pm, mtp.getTable(),params));
           }
           else if(obj instanceof MSchemaPrivilege)
           {
             MSchemaPrivilege msp = (MSchemaPrivilege)obj;
             HashMap<String,Object> params = new HashMap<String,Object>();
             params.put("grantor", msp.getGrantor());
             params.put("grantor_type", msp.getGrantorType());
             params.put("principalName", msp.getPrincipalName());
             params.put("principal_type", msp.getPrincipalType());
             params.put("privilege", msp.getPrivilege());
             params.put("create_time",msp.getCreateTime());
             params.put("grant_option",msp.getGrantOption());
             params.put("schema_name", msp.getSchema().getSchemaName());
             MetaMsgServer.sendMsg(MSGFactory.generateDDLMsg(MSGType.MSG_REVOKE_SCHEMA,-1l,-1l, pm, msp.getSchema(),params));
           }
           else if(obj instanceof MPartitionPrivilege)
           {
             MPartitionPrivilege mpp = (MPartitionPrivilege)obj;
             HashMap<String,Object> params = new HashMap<String,Object>();
             params.put("grantor", mpp.getGrantor());
             params.put("grantor_type", mpp.getGrantorType());
             params.put("principalName", mpp.getPrincipalName());
             params.put("principal_type", mpp.getPrincipalType());
             params.put("privilege", mpp.getPrivilege());
             params.put("create_time",mpp.getCreateTime());
             params.put("grant_option",mpp.getGrantOption());
             params.put("partition_name", mpp.getPartition().getPartitionName());
             params.put("table_name", mpp.getPartition().getTable().getTableName());
             params.put("db_name", mpp.getPartition().getTable().getDatabase().getName());
             long db_id = Long.parseLong(MSGFactory.getIDFromJdoObjectId(pm.getObjectId(mpp.getPartition().getTable().getDatabase()).toString()));
             MetaMsgServer.sendMsg(MSGFactory.generateDDLMsg(MSGType.MSG_REVOKE_PARTITION,db_id,-1l, pm, mpp.getPartition(),params));
           }
           else if(obj instanceof MPartitionColumnPrivilege)
           {
             MPartitionColumnPrivilege mpcp = (MPartitionColumnPrivilege)obj;
             HashMap<String,Object> params = new HashMap<String,Object>();
             params.put("grantor", mpcp.getGrantor());
             params.put("grantor_type", mpcp.getGrantorType());
             params.put("principalName", mpcp.getPrincipalName());
             params.put("principal_type", mpcp.getPrincipalType());
             params.put("privilege", mpcp.getPrivilege());
             params.put("create_time",mpcp.getCreateTime());
             params.put("grant_option",mpcp.getGrantOption());
             params.put("column_name", mpcp.getColumnName());
             params.put("partition_name", mpcp.getPartition().getPartitionName());
             params.put("table_name", mpcp.getPartition().getTable().getTableName());
             params.put("db_name", mpcp.getPartition().getTable().getDatabase().getName());
             long db_id = Long.parseLong(MSGFactory.getIDFromJdoObjectId(pm.getObjectId(mpcp.getPartition().getTable().getDatabase()).toString()));
             MetaMsgServer.sendMsg(MSGFactory.generateDDLMsg(MSGType.MSG_REVOKE_PARTITION_COLUMN,db_id,-1l, pm, mpcp.getPartition().getTable().getSd().getCD(),params));
           }
           else if(obj instanceof MTableColumnPrivilege)
           {
             MTableColumnPrivilege mtcp = (MTableColumnPrivilege)obj;
             HashMap<String,Object> params = new HashMap<String,Object>();
             params.put("grantor", mtcp.getGrantor());
             params.put("grantor_type", mtcp.getGrantorType());
             params.put("principalName", mtcp.getPrincipalName());
             params.put("principal_type", mtcp.getPrincipalType());
             params.put("privilege", mtcp.getPrivilege());
             params.put("create_time",mtcp.getCreateTime());
             params.put("grant_option",mtcp.getGrantOption());
             params.put("column_name", mtcp.getColumnName());
             params.put("table_name", mtcp.getTable().getTableName());
             params.put("db_name", mtcp.getTable().getDatabase().getName());
             long db_id = Long.parseLong(MSGFactory.getIDFromJdoObjectId(pm.getObjectId(mtcp.getTable().getDatabase()).toString()));
             MetaMsgServer.sendMsg(MSGFactory.generateDDLMsg(MSGType.MSG_REVOKE_TABLE_COLUMN,db_id,-1l, pm, mtcp.getTable().getSd().getCD(),params));
           }
         }
       }
     } finally {
       if (!committed) {
         rollbackTransaction();
       }
     }
     return committed;
   }
 
   @SuppressWarnings("unchecked")
   private List<MRoleMap> listRoleMembers(
       MRole mRol) {
     boolean success = false;
     List<MRoleMap> mRoleMemeberList = null;
     try {
       openTransaction();
       LOG.debug("Executing listMSecurityUserRoleMember");
       Query query = pm.newQuery(MRoleMap.class,
           "role.roleName == t1");
       query.declareParameters("java.lang.String t1");
       query.setUnique(false);
       mRoleMemeberList = (List<MRoleMap>) query.execute(
           mRol.getRoleName());
       LOG.debug("Done executing query for listMSecurityUserRoleMember");
       pm.retrieveAll(mRoleMemeberList);
       success = commitTransaction();
       LOG.debug("Done retrieving all objects for listMSecurityUserRoleMember");
     } finally {
       if (!success) {
         rollbackTransaction();
       }
     }
     return mRoleMemeberList;
   }
 
   @SuppressWarnings("unchecked")
   @Override
   public List<MGlobalPrivilege> listPrincipalGlobalGrants(String principalName, PrincipalType principalType) {
     boolean commited = false;
     List<MGlobalPrivilege> userNameDbPriv = null;
     try {
       openTransaction();
       if (principalName != null) {
         Query query = pm.newQuery(MGlobalPrivilege.class,
             "principalName == t1 && principalType == t2 ");
         query.declareParameters(
             "java.lang.String t1, java.lang.String t2");
         userNameDbPriv = (List<MGlobalPrivilege>) query
             .executeWithArray(principalName, principalType.toString());
         pm.retrieveAll(userNameDbPriv);
       }
       commited = commitTransaction();
     } finally {
       if (!commited) {
         rollbackTransaction();
       }
     }
     return userNameDbPriv;
   }
 
   @SuppressWarnings("unchecked")
   @Override
   public List<MDBPrivilege> listPrincipalDBGrants(String principalName,
       PrincipalType principalType, String dbName) {
     boolean success = false;
     List<MDBPrivilege> mSecurityDBList = null;
     dbName = dbName.toLowerCase().trim();
 
     try {
       openTransaction();
       LOG.debug("Executing listPrincipalDBGrants");
       Query query = pm.newQuery(MDBPrivilege.class,
           "principalName == t1 && principalType == t2 && database.name == t3");
       query
           .declareParameters("java.lang.String t1, java.lang.String t2, java.lang.String t3");
       mSecurityDBList = (List<MDBPrivilege>) query.executeWithArray(principalName, principalType.toString(), dbName);
       LOG.debug("Done executing query for listPrincipalDBGrants");
       pm.retrieveAll(mSecurityDBList);
       success = commitTransaction();
       LOG.debug("Done retrieving all objects for listPrincipalDBGrants");
     } finally {
       if (!success) {
         rollbackTransaction();
       }
     }
     return mSecurityDBList;
   }
 
   @SuppressWarnings("unchecked")
   private List<MDBPrivilege> listPrincipalAllDBGrant(
       String principalName, PrincipalType principalType) {
     boolean success = false;
     List<MDBPrivilege> mSecurityDBList = null;
     try {
       openTransaction();
       LOG.debug("Executing listPrincipalAllDBGrant");
       Query query = pm.newQuery(MDBPrivilege.class,
           "principalName == t1 && principalType == t2");
       query
           .declareParameters("java.lang.String t1, java.lang.String t2");
       mSecurityDBList = (List<MDBPrivilege>) query.execute(principalName, principalType.toString());
       LOG.debug("Done executing query for listPrincipalAllDBGrant");
       pm.retrieveAll(mSecurityDBList);
       success = commitTransaction();
       LOG.debug("Done retrieving all objects for listPrincipalAllDBGrant");
     } finally {
       if (!success) {
         rollbackTransaction();
       }
     }
     return mSecurityDBList;
   }
 
   @SuppressWarnings("unchecked")
   public List<MTablePrivilege> listAllTableGrants(String dbName,
       String tableName) {
     boolean success = false;
     tableName = tableName.toLowerCase().trim();
     dbName = dbName.toLowerCase().trim();
     List<MTablePrivilege> mSecurityTabList = null;
     tableName = tableName.toLowerCase().trim();
     dbName = dbName.toLowerCase().trim();
     try {
       openTransaction();
       LOG.debug("Executing listAllTableGrants");
       String queryStr = "table.tableName == t1 && table.database.name == t2";
       Query query = pm.newQuery(
           MTablePrivilege.class, queryStr);
       query.declareParameters(
           "java.lang.String t1, java.lang.String t2");
       mSecurityTabList = (List<MTablePrivilege>) query
           .executeWithArray(tableName, dbName);
       LOG.debug("Done executing query for listAllTableGrants");
       pm.retrieveAll(mSecurityTabList);
       success = commitTransaction();
       LOG
           .debug("Done retrieving all objects for listAllTableGrants");
     } finally {
       if (!success) {
         rollbackTransaction();
       }
     }
     return mSecurityTabList;
   }
 
   @SuppressWarnings("unchecked")
   public List<MPartitionPrivilege> listTableAllPartitionGrants(String dbName,
       String tableName) {
     tableName = tableName.toLowerCase().trim();
     dbName = dbName.toLowerCase().trim();
     boolean success = false;
     List<MPartitionPrivilege> mSecurityTabPartList = null;
     try {
       openTransaction();
       LOG.debug("Executing listTableAllPartitionGrants");
       String queryStr = "partition.table.tableName == t1 && partition.table.database.name == t2";
       Query query = pm.newQuery(
           MPartitionPrivilege.class, queryStr);
       query.declareParameters(
           "java.lang.String t1, java.lang.String t2");
       mSecurityTabPartList = (List<MPartitionPrivilege>) query
           .executeWithArray(tableName, dbName);
       LOG.debug("Done executing query for listTableAllPartitionGrants");
       pm.retrieveAll(mSecurityTabPartList);
       success = commitTransaction();
       LOG
           .debug("Done retrieving all objects for listTableAllPartitionGrants");
     } finally {
       if (!success) {
         rollbackTransaction();
       }
     }
     return mSecurityTabPartList;
   }
 
   @SuppressWarnings("unchecked")
   public List<MTableColumnPrivilege> listTableAllColumnGrants(String dbName,
       String tableName) {
     boolean success = false;
     List<MTableColumnPrivilege> mTblColPrivilegeList = null;
     tableName = tableName.toLowerCase().trim();
     dbName = dbName.toLowerCase().trim();
 
     try {
       openTransaction();
       LOG.debug("Executing listTableAllColumnGrants");
       String queryStr = "table.tableName == t1 && table.database.name == t2";
       Query query = pm.newQuery(MTableColumnPrivilege.class, queryStr);
       query.declareParameters("java.lang.String t1, java.lang.String t2");
       mTblColPrivilegeList = (List<MTableColumnPrivilege>) query
           .executeWithArray(tableName, dbName);
       LOG.debug("Done executing query for listTableAllColumnGrants");
       pm.retrieveAll(mTblColPrivilegeList);
       success = commitTransaction();
       LOG.debug("Done retrieving all objects for listTableAllColumnGrants");
     } finally {
       if (!success) {
         rollbackTransaction();
       }
     }
     return mTblColPrivilegeList;
   }
 
   @SuppressWarnings("unchecked")
   public List<MPartitionColumnPrivilege> listTableAllPartitionColumnGrants(String dbName,
       String tableName) {
     boolean success = false;
     tableName = tableName.toLowerCase().trim();
     dbName = dbName.toLowerCase().trim();
 
     List<MPartitionColumnPrivilege> mSecurityColList = null;
     try {
       openTransaction();
       LOG.debug("Executing listTableAllPartitionColumnGrants");
       String queryStr = "partition.table.tableName == t1 && partition.table.database.name == t2";
       Query query = pm.newQuery(MPartitionColumnPrivilege.class, queryStr);
       query.declareParameters("java.lang.String t1, java.lang.String t2");
       mSecurityColList = (List<MPartitionColumnPrivilege>) query
           .executeWithArray(tableName, dbName);
       LOG.debug("Done executing query for listTableAllPartitionColumnGrants");
       pm.retrieveAll(mSecurityColList);
       success = commitTransaction();
       LOG.debug("Done retrieving all objects for listTableAllPartitionColumnGrants");
     } finally {
       if (!success) {
         rollbackTransaction();
       }
     }
     return mSecurityColList;
   }
 
   @SuppressWarnings("unchecked")
   public List<MPartitionColumnPrivilege> listPartitionAllColumnGrants(String dbName,
       String tableName, String partName) {
     boolean success = false;
     tableName = tableName.toLowerCase().trim();
     dbName = dbName.toLowerCase().trim();
 
     List<MPartitionColumnPrivilege> mSecurityColList = null;
     try {
       openTransaction();
       LOG.debug("Executing listPartitionAllColumnGrants");
       String queryStr = "partition.table.tableName == t1 && partition.table.database.name == t2 && partition.partitionName == t3";
       Query query = pm.newQuery(MPartitionColumnPrivilege.class, queryStr);
       query.declareParameters(
           "java.lang.String t1, java.lang.String t2, java.lang.String t3");
       mSecurityColList = (List<MPartitionColumnPrivilege>) query
           .executeWithArray(tableName, dbName, partName);
       LOG.debug("Done executing query for listPartitionAllColumnGrants");
       pm.retrieveAll(mSecurityColList);
       success = commitTransaction();
       LOG.debug("Done retrieving all objects for listPartitionAllColumnGrants");
     } finally {
       if (!success) {
         rollbackTransaction();
       }
     }
     return mSecurityColList;
   }
 
   @SuppressWarnings("unchecked")
   private List<MDBPrivilege> listDatabaseGrants(String dbName) {
     dbName = dbName.toLowerCase().trim();
 
     boolean success = false;
     try {
       openTransaction();
       LOG.debug("Executing listDatabaseGrants");
       Query query = pm.newQuery(MDBPrivilege.class,
           "database.name == t1");
       query.declareParameters("java.lang.String t1");
       List<MDBPrivilege> mSecurityDBList = (List<MDBPrivilege>) query
           .executeWithArray(dbName);
       LOG.debug("Done executing query for listDatabaseGrants");
       pm.retrieveAll(mSecurityDBList);
       success = commitTransaction();
       LOG.debug("Done retrieving all objects for listDatabaseGrants");
       return mSecurityDBList;
     } finally {
       if (!success) {
         rollbackTransaction();
       }
     }
   }
 
   @SuppressWarnings("unchecked")
   private List<MPartitionPrivilege> listPartitionGrants(String dbName, String tableName,
       String partName) {
     tableName = tableName.toLowerCase().trim();
     dbName = dbName.toLowerCase().trim();
 
     boolean success = false;
     List<MPartitionPrivilege> mSecurityTabPartList = null;
     try {
       openTransaction();
       LOG.debug("Executing listPartitionGrants");
       Query query = pm.newQuery(MPartitionPrivilege.class,
           "partition.table.tableName == t1 && partition.table.database.name == t2 && partition.partitionName == t3");
       query.declareParameters(
           "java.lang.String t1, java.lang.String t2, java.lang.String t3");
       mSecurityTabPartList = (List<MPartitionPrivilege>) query
           .executeWithArray(tableName, dbName, partName);
       LOG.debug("Done executing query for listPartitionGrants");
       pm.retrieveAll(mSecurityTabPartList);
       success = commitTransaction();
       LOG.debug("Done retrieving all objects for listPartitionGrants");
     } finally {
       if (!success) {
         rollbackTransaction();
       }
     }
     return mSecurityTabPartList;
   }
 
   @SuppressWarnings("unchecked")
   public List<MTablePrivilege> listAllTableGrants(
       String principalName, PrincipalType principalType, String dbName,
       String tableName) {
     tableName = tableName.toLowerCase().trim();
     dbName = dbName.toLowerCase().trim();
 
     boolean success = false;
     List<MTablePrivilege> mSecurityTabPartList = null;
     try {
       openTransaction();
       LOG.debug("Executing listAllTableGrants");
       Query query = pm.newQuery(
           MTablePrivilege.class,
               "principalName == t1 && principalType == t2 && table.tableName == t3 && table.database.name == t4");
       query.declareParameters(
           "java.lang.String t1, java.lang.String t2, java.lang.String t3, java.lang.String t4");
       mSecurityTabPartList = (List<MTablePrivilege>) query
           .executeWithArray(principalName, principalType.toString(), tableName, dbName);
       LOG.debug("Done executing query for listAllTableGrants");
       pm.retrieveAll(mSecurityTabPartList);
       success = commitTransaction();
       LOG
           .debug("Done retrieving all objects for listAllTableGrants");
     } finally {
       if (!success) {
         rollbackTransaction();
       }
     }
     return mSecurityTabPartList;
   }
 
   @SuppressWarnings("unchecked")
   public List<MSchemaPrivilege> listAllSchemaGrants(
       String principalName, PrincipalType principalType, String schemaName) {
     schemaName = schemaName.toLowerCase().trim();
 
     boolean success = false;
     List<MSchemaPrivilege> mSecuritySchPartList = null;
     try {
       openTransaction();
       LOG.debug("Executing listAllSchemaGrants");
       Query query = pm.newQuery(
           MSchemaPrivilege.class,
               "principalName == t1 && principalType == t2 && schema.schemaName == t3");
       query.declareParameters(
           "java.lang.String t1, java.lang.String t2, java.lang.String t3");
       mSecuritySchPartList = (List<MSchemaPrivilege>) query
           .executeWithArray(principalName, principalType.toString(), schemaName);
       LOG.debug("Done executing query for listAllSchemaGrants");
       pm.retrieveAll(mSecuritySchPartList);
       success = commitTransaction();
       LOG
           .debug("Done retrieving all objects for listAllSchemaGrants");
     } finally {
       if (!success) {
         rollbackTransaction();
       }
     }
     return mSecuritySchPartList;
   }
 
 
   @SuppressWarnings("unchecked")
   @Override
   public List<MPartitionPrivilege> listPrincipalPartitionGrants(
       String principalName, PrincipalType principalType, String dbName,
       String tableName, String partName) {
     boolean success = false;
     tableName = tableName.toLowerCase().trim();
     dbName = dbName.toLowerCase().trim();
 
     List<MPartitionPrivilege> mSecurityTabPartList = null;
     try {
       openTransaction();
       LOG.debug("Executing listMSecurityPrincipalPartitionGrant");
       Query query = pm.newQuery(
           MPartitionPrivilege.class,
               "principalName == t1 && principalType == t2 && partition.table.tableName == t3 " +
               "&& partition.table.database.name == t4 && partition.partitionName == t5");
       query.declareParameters(
           "java.lang.String t1, java.lang.String t2, java.lang.String t3, java.lang.String t4, " +
           "java.lang.String t5");
       mSecurityTabPartList = (List<MPartitionPrivilege>) query
           .executeWithArray(principalName, principalType.toString(), tableName, dbName, partName);
       LOG.debug("Done executing query for listMSecurityPrincipalPartitionGrant");
 
       pm.retrieveAll(mSecurityTabPartList);
       success = commitTransaction();
       LOG.debug("Done retrieving all objects for listMSecurityPrincipalPartitionGrant");
     } finally {
       if (!success) {
         rollbackTransaction();
       }
     }
     return mSecurityTabPartList;
   }
 
   @SuppressWarnings("unchecked")
   @Override
   public List<MTableColumnPrivilege> listPrincipalTableColumnGrants(
       String principalName, PrincipalType principalType, String dbName,
       String tableName, String columnName) {
     boolean success = false;
     tableName = tableName.toLowerCase().trim();
     dbName = dbName.toLowerCase().trim();
     columnName = columnName.toLowerCase().trim();
     List<MTableColumnPrivilege> mSecurityColList = null;
     try {
       openTransaction();
       LOG.debug("Executing listPrincipalTableColumnGrants");
       String queryStr = "principalName == t1 && principalType == t2 && " +
           "table.tableName == t3 && table.database.name == t4 &&  columnName == t5 ";
       Query query = pm.newQuery(MTableColumnPrivilege.class, queryStr);
       query
           .declareParameters("java.lang.String t1, java.lang.String t2, java.lang.String t3, " +
               "java.lang.String t4, java.lang.String t5");
       mSecurityColList = (List<MTableColumnPrivilege>) query.executeWithArray(
           principalName, principalType.toString(), tableName, dbName, columnName);
       LOG.debug("Done executing query for listPrincipalTableColumnGrants");
       pm.retrieveAll(mSecurityColList);
       success = commitTransaction();
       LOG
           .debug("Done retrieving all objects for listPrincipalTableColumnGrants");
     } finally {
       if (!success) {
         rollbackTransaction();
       }
     }
     return mSecurityColList;
   }
 
   @SuppressWarnings("unchecked")
   public List<MPartitionColumnPrivilege> listPrincipalPartitionColumnGrants(
       String principalName, PrincipalType principalType, String dbName,
       String tableName, String partitionName, String columnName) {
     boolean success = false;
     tableName = tableName.toLowerCase().trim();
     dbName = dbName.toLowerCase().trim();
     columnName = columnName.toLowerCase().trim();
 
     List<MPartitionColumnPrivilege> mSecurityColList = null;
     try {
       openTransaction();
       LOG.debug("Executing listPrincipalPartitionColumnGrants");
       Query query = pm
           .newQuery(
               MPartitionColumnPrivilege.class,
               "principalName == t1 && principalType == t2 && partition.table.tableName == t3 " +
               "&& partition.table.database.name == t4 && partition.partitionName == t5 && columnName == t6");
       query
           .declareParameters("java.lang.String t1, java.lang.String t2, java.lang.String t3, " +
               "java.lang.String t4, java.lang.String t5, java.lang.String t6");
 
       mSecurityColList = (List<MPartitionColumnPrivilege>) query
           .executeWithArray(principalName, principalType.toString(), tableName,
               dbName, partitionName, columnName);
       LOG.debug("Done executing query for listPrincipalPartitionColumnGrants");
       pm.retrieveAll(mSecurityColList);
 
       success = commitTransaction();
       LOG
           .debug("Done retrieving all objects for listPrincipalPartitionColumnGrants");
     } finally {
       if (!success) {
         rollbackTransaction();
       }
     }
     return mSecurityColList;
   }
 
   @SuppressWarnings("unchecked")
   private List<MTablePrivilege> listPrincipalAllTableGrants(
       String principalName, PrincipalType principalType) {
     boolean success = false;
     List<MTablePrivilege> mSecurityTabPartList = null;
     try {
       openTransaction();
       LOG.debug("Executing listPrincipalAllTableGrants");
       Query query = pm.newQuery(MTablePrivilege.class,
           "principalName == t1 && principalType == t2");
       query.declareParameters("java.lang.String t1, java.lang.String t2");
       mSecurityTabPartList = (List<MTablePrivilege>) query.execute(
           principalName, principalType.toString());
       LOG
           .debug("Done executing query for listPrincipalAllTableGrants");
       pm.retrieveAll(mSecurityTabPartList);
       success = commitTransaction();
       LOG
           .debug("Done retrieving all objects for listPrincipalAllTableGrants");
     } finally {
       if (!success) {
         rollbackTransaction();
       }
     }
     return mSecurityTabPartList;
   }
 
   @SuppressWarnings("unchecked")
   private List<MPartitionPrivilege> listPrincipalAllPartitionGrants(
       String principalName, PrincipalType principalType) {
     boolean success = false;
     List<MPartitionPrivilege> mSecurityTabPartList = null;
     try {
       openTransaction();
       LOG.debug("Executing listPrincipalAllPartitionGrants");
       Query query = pm.newQuery(MPartitionPrivilege.class,
           "principalName == t1 && principalType == t2");
       query.declareParameters("java.lang.String t1, java.lang.String t2");
       mSecurityTabPartList = (List<MPartitionPrivilege>) query.execute(
           principalName, principalType.toString());
       LOG
           .debug("Done executing query for listPrincipalAllPartitionGrants");
       pm.retrieveAll(mSecurityTabPartList);
       success = commitTransaction();
       LOG
           .debug("Done retrieving all objects for listPrincipalAllPartitionGrants");
     } finally {
       if (!success) {
         rollbackTransaction();
       }
     }
     return mSecurityTabPartList;
   }
 
   @SuppressWarnings("unchecked")
   private List<MTableColumnPrivilege> listPrincipalAllTableColumnGrants(
       String principalName, PrincipalType principalType) {
     boolean success = false;
     List<MTableColumnPrivilege> mSecurityColumnList = null;
     try {
       openTransaction();
       LOG.debug("Executing listPrincipalAllTableColumnGrants");
       Query query = pm.newQuery(MTableColumnPrivilege.class,
           "principalName == t1 && principalType == t2");
       query
           .declareParameters("java.lang.String t1, java.lang.String t2");
       mSecurityColumnList = (List<MTableColumnPrivilege>) query.execute(
           principalName, principalType.toString());
       LOG.debug("Done executing query for listPrincipalAllTableColumnGrants");
       pm.retrieveAll(mSecurityColumnList);
       success = commitTransaction();
       LOG.debug("Done retrieving all objects for listPrincipalAllTableColumnGrants");
     } finally {
       if (!success) {
         rollbackTransaction();
       }
     }
     return mSecurityColumnList;
   }
 
   @SuppressWarnings("unchecked")
   private List<MPartitionColumnPrivilege> listPrincipalAllPartitionColumnGrants(
       String principalName, PrincipalType principalType) {
     boolean success = false;
     List<MPartitionColumnPrivilege> mSecurityColumnList = null;
     try {
       openTransaction();
       LOG.debug("Executing listPrincipalAllTableColumnGrants");
       Query query = pm.newQuery(MPartitionColumnPrivilege.class,
           "principalName == t1 && principalType == t2");
       query
           .declareParameters("java.lang.String t1, java.lang.String t2");
       mSecurityColumnList = (List<MPartitionColumnPrivilege>) query.execute(
           principalName, principalType.toString());
       LOG.debug("Done executing query for listPrincipalAllTableColumnGrants");
       pm.retrieveAll(mSecurityColumnList);
       success = commitTransaction();
       LOG.debug("Done retrieving all objects for listPrincipalAllTableColumnGrants");
     } finally {
       if (!success) {
         rollbackTransaction();
       }
     }
     return mSecurityColumnList;
   }
 
   @Override
   public boolean isPartitionMarkedForEvent(String dbName, String tblName,
       Map<String, String> partName, PartitionEventType evtType) throws UnknownTableException,
       MetaException, InvalidPartitionException, UnknownPartitionException {
 
     Collection<MPartitionEvent> partEvents;
     boolean success = false;
     LOG.debug("Begin Executing isPartitionMarkedForEvent");
     try{
     openTransaction();
     Query query = pm.newQuery(MPartitionEvent.class, "dbName == t1 && tblName == t2 && partName == t3 && eventType == t4");
     query.declareParameters("java.lang.String t1, java.lang.String t2, java.lang.String t3, int t4");
     Table tbl = getTable(dbName, tblName); // Make sure dbName and tblName are valid.
     if(null == tbl) {
       throw new UnknownTableException("Table: "+ tblName + " is not found.");
     }
     partEvents = (Collection<MPartitionEvent>) query.executeWithArray(dbName, tblName, getPartitionStr(tbl, partName), evtType.getValue());
     pm.retrieveAll(partEvents);
     success = commitTransaction();
     LOG.debug("Done executing isPartitionMarkedForEvent");
     } finally{
       if (!success) {
         rollbackTransaction();
       }
     }
     return (partEvents != null  && !partEvents.isEmpty()) ? true : false;
 
   }
 
   @Override
   public Table markPartitionForEvent(String dbName, String tblName, Map<String,String> partName,
       PartitionEventType evtType) throws MetaException, UnknownTableException, InvalidPartitionException, UnknownPartitionException {
 
     LOG.debug("Begin executing markPartitionForEvent");
     boolean success = false;
     Table tbl = null;
     try{
     openTransaction();
     tbl = getTable(dbName, tblName); // Make sure dbName and tblName are valid.
     if(null == tbl) {
       throw new UnknownTableException("Table: "+ tblName + " is not found.");
     }
     pm.makePersistent(new MPartitionEvent(dbName,tblName,getPartitionStr(tbl, partName), evtType.getValue()));
     success = commitTransaction();
     LOG.debug("Done executing markPartitionForEvent");
     } finally {
       if(!success) {
         rollbackTransaction();
       }
     }
     return tbl;
   }
 
   private String getPartitionStr(Table tbl, Map<String,String> partName) throws InvalidPartitionException{
     if(tbl.getPartitionKeysSize() != partName.size()){
       throw new InvalidPartitionException("Number of partition columns in table: "+ tbl.getPartitionKeysSize() +
           " doesn't match with number of supplied partition values: "+partName.size());
     }
     final List<String> storedVals = new ArrayList<String>(tbl.getPartitionKeysSize());
     for(FieldSchema partKey : tbl.getPartitionKeys()){
       String partVal = partName.get(partKey.getName());
       if(null == partVal) {
         throw new InvalidPartitionException("No value found for partition column: "+partKey.getName());
       }
       storedVals.add(partVal);
     }
     return join(storedVals,',');
   }
 
   /** The following API
    *
    *  - executeJDOQLSelect
    *
    * is used by HiveMetaTool. This API **shouldn't** be exposed via Thrift.
    *
    */
   public Collection<?> executeJDOQLSelect(String query) {
     boolean committed = false;
     Collection<?> result = null;
 
     try {
       openTransaction();
       Query q = pm.newQuery(query);
       result = (Collection<?>) q.execute();
       committed = commitTransaction();
       if (committed) {
         return result;
       } else {
         return null;
       }
     } finally {
       if (!committed) {
         rollbackTransaction();
       }
     }
   }
 
   /** The following API
   *
   *  - executeJDOQLUpdate
   *
   * is used by HiveMetaTool. This API **shouldn't** be exposed via Thrift.
   *
   */
   public long executeJDOQLUpdate(String query) {
     boolean committed = false;
     long numUpdated = 0;
 
     try {
       openTransaction();
       Query q = pm.newQuery(query);
       numUpdated = (Long) q.execute();
       committed = commitTransaction();
       if (committed) {
         return numUpdated;
       } else {
         return -1;
       }
     } finally {
       if (!committed) {
         rollbackTransaction();
       }
     }
   }
 
   /** The following API
   *
   *  - listFSRoots
   *
   * is used by HiveMetaTool. This API **shouldn't** be exposed via Thrift.
   *
   */
   public Set<String> listFSRoots() {
     boolean committed = false;
     Set<String> fsRoots = new HashSet<String>();
 
     try {
       openTransaction();
       Query query = pm.newQuery(MDatabase.class);
       List<MDatabase> mDBs = (List<MDatabase>) query.execute();
       pm.retrieveAll(mDBs);
 
       for (MDatabase mDB:mDBs) {
         fsRoots.add(mDB.getLocationUri());
       }
       committed = commitTransaction();
       if (committed) {
         return fsRoots;
       } else {
         return null;
       }
     } finally {
       if (!committed) {
         rollbackTransaction();
       }
     }
   }
 
   private boolean shouldUpdateURI(URI onDiskUri, URI inputUri) {
     String onDiskHost = onDiskUri.getHost();
     String inputHost = inputUri.getHost();
 
     int onDiskPort = onDiskUri.getPort();
     int inputPort = inputUri.getPort();
 
     String onDiskScheme = onDiskUri.getScheme();
     String inputScheme = inputUri.getScheme();
 
     //compare ports
     if (inputPort != -1) {
       if (inputPort != onDiskPort) {
         return false;
       }
     }
     //compare schemes
     if (inputScheme != null) {
       if (onDiskScheme == null) {
         return false;
       }
       if (!inputScheme.equalsIgnoreCase(onDiskScheme)) {
         return false;
       }
     }
     //compare hosts
     if (onDiskHost != null) {
       if (!inputHost.equalsIgnoreCase(onDiskHost)) {
         return false;
       }
     } else {
       return false;
     }
     return true;
   }
 
   public class UpdateMDatabaseURIRetVal {
     private List<String> badRecords;
     private Map<String, String> updateLocations;
 
     UpdateMDatabaseURIRetVal(List<String> badRecords, Map<String, String> updateLocations) {
       this.badRecords = badRecords;
       this.updateLocations = updateLocations;
     }
 
     public List<String> getBadRecords() {
       return badRecords;
     }
 
     public void setBadRecords(List<String> badRecords) {
       this.badRecords = badRecords;
     }
 
     public Map<String, String> getUpdateLocations() {
       return updateLocations;
     }
 
     public void setUpdateLocations(Map<String, String> updateLocations) {
       this.updateLocations = updateLocations;
     }
   }
 
   /** The following APIs
   *
   *  - updateMDatabaseURI
   *
   * is used by HiveMetaTool. This API **shouldn't** be exposed via Thrift.
   *
   */
   public UpdateMDatabaseURIRetVal updateMDatabaseURI(URI oldLoc, URI newLoc, boolean dryRun) {
     boolean committed = false;
     Map<String, String> updateLocations = new HashMap<String, String>();
     List<String> badRecords = new ArrayList<String>();
     UpdateMDatabaseURIRetVal retVal = null;
 
     try {
       openTransaction();
       Query query = pm.newQuery(MDatabase.class);
       List<MDatabase> mDBs = (List<MDatabase>) query.execute();
       pm.retrieveAll(mDBs);
 
       for(MDatabase mDB:mDBs) {
         URI locationURI = null;
         String location = mDB.getLocationUri();
         try {
           locationURI = new URI(location);
         } catch(URISyntaxException e) {
           badRecords.add(location);
         } catch (NullPointerException e) {
           badRecords.add(location);
         }
         if (locationURI == null) {
           badRecords.add(location);
         } else {
           if (shouldUpdateURI(locationURI, oldLoc)) {
             String dbLoc = mDB.getLocationUri().replaceAll(oldLoc.toString(), newLoc.toString());
             updateLocations.put(locationURI.toString(), dbLoc);
             if (!dryRun) {
               mDB.setLocationUri(dbLoc);
             }
           }
         }
       }
       committed = commitTransaction();
       if (committed) {
         retVal = new UpdateMDatabaseURIRetVal(badRecords, updateLocations);
       }
       return retVal;
     } finally {
       if (!committed) {
         rollbackTransaction();
       }
     }
   }
 
   public class UpdateMStorageDescriptorTblPropURIRetVal {
     private List<String> badRecords;
     private Map<String, String> updateLocations;
 
     UpdateMStorageDescriptorTblPropURIRetVal(List<String> badRecords,
       Map<String, String> updateLocations) {
       this.badRecords = badRecords;
       this.updateLocations = updateLocations;
     }
 
     public List<String> getBadRecords() {
       return badRecords;
     }
 
     public void setBadRecords(List<String> badRecords) {
       this.badRecords = badRecords;
     }
 
     public Map<String, String> getUpdateLocations() {
       return updateLocations;
     }
 
     public void setUpdateLocations(Map<String, String> updateLocations) {
       this.updateLocations = updateLocations;
     }
   }
 
   /** The following APIs
   *
   *  - updateMStorageDescriptorTblPropURI
   *
   * is used by HiveMetaTool. This API **shouldn't** be exposed via Thrift.
   *
   */
   public UpdateMStorageDescriptorTblPropURIRetVal updateMStorageDescriptorTblPropURI(URI oldLoc,
       URI newLoc, String tblPropKey, boolean isDryRun) {
     boolean committed = false;
     Map<String, String> updateLocations = new HashMap<String, String>();
     List<String> badRecords = new ArrayList<String>();
     UpdateMStorageDescriptorTblPropURIRetVal retVal = null;
 
     try {
       openTransaction();
       Query query = pm.newQuery(MStorageDescriptor.class);
       List<MStorageDescriptor> mSDSs = (List<MStorageDescriptor>) query.execute();
       pm.retrieveAll(mSDSs);
 
       for(MStorageDescriptor mSDS:mSDSs) {
         URI tablePropLocationURI = null;
         if (mSDS.getParameters().containsKey(tblPropKey)) {
           String tablePropLocation = mSDS.getParameters().get(tblPropKey);
           try {
               tablePropLocationURI = new URI(tablePropLocation);
             } catch (URISyntaxException e) {
               badRecords.add(tablePropLocation);
             } catch (NullPointerException e) {
               badRecords.add(tablePropLocation);
             }
             // if tablePropKey that was passed in lead to a valid URI resolution, update it if
             //parts of it match the old-NN-loc, else add to badRecords
             if (tablePropLocationURI == null) {
               badRecords.add(tablePropLocation);
             } else {
               if (shouldUpdateURI(tablePropLocationURI, oldLoc)) {
                 String tblPropLoc = mSDS.getParameters().get(tblPropKey).replaceAll(oldLoc.toString(),
                     newLoc.toString());
                 updateLocations.put(tablePropLocationURI.toString(), tblPropLoc);
                 if (!isDryRun) {
                   mSDS.getParameters().put(tblPropKey, tblPropLoc);
                 }
              }
            }
          }
       }
       committed = commitTransaction();
       if (committed) {
         retVal = new UpdateMStorageDescriptorTblPropURIRetVal(badRecords, updateLocations);
       }
       return retVal;
      } finally {
         if (!committed) {
           rollbackTransaction();
         }
      }
   }
 
   public class UpdateMStorageDescriptorTblURIRetVal {
     private List<String> badRecords;
     private Map<String, String> updateLocations;
 
     UpdateMStorageDescriptorTblURIRetVal(List<String> badRecords,
       Map<String, String> updateLocations) {
       this.badRecords = badRecords;
       this.updateLocations = updateLocations;
     }
 
     public List<String> getBadRecords() {
       return badRecords;
     }
 
     public void setBadRecords(List<String> badRecords) {
       this.badRecords = badRecords;
     }
 
     public Map<String, String> getUpdateLocations() {
       return updateLocations;
     }
 
     public void setUpdateLocations(Map<String, String> updateLocations) {
       this.updateLocations = updateLocations;
     }
   }
 
   /** The following APIs
   *
   *  - updateMStorageDescriptorTblURI
   *
   * is used by HiveMetaTool. This API **shouldn't** be exposed via Thrift.
   *
   */
   public UpdateMStorageDescriptorTblURIRetVal updateMStorageDescriptorTblURI(URI oldLoc, URI newLoc,
     boolean isDryRun) {
     boolean committed = false;
     Map<String, String> updateLocations = new HashMap<String, String>();
     List<String> badRecords = new ArrayList<String>();
     UpdateMStorageDescriptorTblURIRetVal retVal = null;
 
     try {
       openTransaction();
       Query query = pm.newQuery(MStorageDescriptor.class);
       List<MStorageDescriptor> mSDSs = (List<MStorageDescriptor>) query.execute();
       pm.retrieveAll(mSDSs);
 
       for(MStorageDescriptor mSDS:mSDSs) {
         URI locationURI = null;
         String location = mSDS.getLocation();
         try {
           locationURI = new URI(location);
         } catch (URISyntaxException e) {
           badRecords.add(location);
         } catch (NullPointerException e) {
           badRecords.add(location);
         }
         if (locationURI == null) {
           badRecords.add(location);
         } else {
           if (shouldUpdateURI(locationURI, oldLoc)) {
             String tblLoc = mSDS.getLocation().replaceAll(oldLoc.toString(), newLoc.toString());
             updateLocations.put(locationURI.toString(), tblLoc);
             if (!isDryRun) {
               mSDS.setLocation(tblLoc);
             }
           }
         }
       }
       committed = commitTransaction();
       if (committed) {
         retVal = new UpdateMStorageDescriptorTblURIRetVal(badRecords, updateLocations);
       }
       return retVal;
     } finally {
         if (!committed) {
           rollbackTransaction();
         }
      }
   }
 
   public class UpdateSerdeURIRetVal {
     private List<String> badRecords;
     private Map<String, String> updateLocations;
 
     UpdateSerdeURIRetVal(List<String> badRecords, Map<String, String> updateLocations) {
       this.badRecords = badRecords;
       this.updateLocations = updateLocations;
     }
 
     public List<String> getBadRecords() {
       return badRecords;
     }
 
     public void setBadRecords(List<String> badRecords) {
       this.badRecords = badRecords;
     }
 
     public Map<String, String> getUpdateLocations() {
       return updateLocations;
     }
 
     public void setUpdateLocations(Map<String, String> updateLocations) {
       this.updateLocations = updateLocations;
     }
   }
 
   /** The following APIs
   *
   *  - updateSerdeURI
   *
   * is used by HiveMetaTool. This API **shouldn't** be exposed via Thrift.
   *
   */
   public UpdateSerdeURIRetVal updateSerdeURI(URI oldLoc, URI newLoc, String serdeProp,
     boolean isDryRun) {
     boolean committed = false;
     Map<String, String> updateLocations = new HashMap<String, String>();
     List<String> badRecords = new ArrayList<String>();
     UpdateSerdeURIRetVal retVal = null;
 
     try {
       openTransaction();
       Query query = pm.newQuery(MSerDeInfo.class);
       List<MSerDeInfo> mSerdes = (List<MSerDeInfo>) query.execute();
       pm.retrieveAll(mSerdes);
 
       for(MSerDeInfo mSerde:mSerdes) {
         if (mSerde.getParameters().containsKey(serdeProp)) {
           String schemaLoc = mSerde.getParameters().get(serdeProp);
           URI schemaLocURI = null;
           try {
             schemaLocURI = new URI(schemaLoc);
           } catch (URISyntaxException e) {
             badRecords.add(schemaLoc);
           } catch (NullPointerException e) {
             badRecords.add(schemaLoc);
           }
           if (schemaLocURI == null) {
             badRecords.add(schemaLoc);
           } else {
             if (shouldUpdateURI(schemaLocURI, oldLoc)) {
               String newSchemaLoc = schemaLoc.replaceAll(oldLoc.toString(), newLoc.toString());
               updateLocations.put(schemaLocURI.toString(), newSchemaLoc);
               if (!isDryRun) {
                 mSerde.getParameters().put(serdeProp, newSchemaLoc);
               }
             }
           }
         }
       }
       committed = commitTransaction();
       if (committed) {
         retVal = new UpdateSerdeURIRetVal(badRecords, updateLocations);
       }
       return retVal;
     } finally {
       if (!committed) {
         rollbackTransaction();
       }
     }
   }
 
   // Methods to persist, maintain and retrieve Column Statistics
   private MTableColumnStatistics convertToMTableColumnStatistics(ColumnStatisticsDesc statsDesc,
       ColumnStatisticsObj statsObj) throws NoSuchObjectException,
       MetaException, InvalidObjectException
   {
      if (statsObj == null || statsDesc == null) {
        throw new InvalidObjectException("Invalid column stats object");
      }
 
      String dbName = statsDesc.getDbName();
      String tableName = statsDesc.getTableName();
      MTable table = getMTable(dbName, tableName);
 
      if (table == null) {
        throw new NoSuchObjectException("Table " + tableName +
        " for which stats is gathered doesn't exist.");
      }
 
      MTableColumnStatistics mColStats = new MTableColumnStatistics();
      mColStats.setTable(table);
      mColStats.setDbName(statsDesc.getDbName());
      mColStats.setTableName(statsDesc.getTableName());
      mColStats.setLastAnalyzed(statsDesc.getLastAnalyzed());
      mColStats.setColName(statsObj.getColName());
      mColStats.setColType(statsObj.getColType());
 
      if (statsObj.getStatsData().isSetBooleanStats()) {
        BooleanColumnStatsData boolStats = statsObj.getStatsData().getBooleanStats();
        mColStats.setBooleanStats(boolStats.getNumTrues(), boolStats.getNumFalses(),
            boolStats.getNumNulls());
      } else if (statsObj.getStatsData().isSetLongStats()) {
        LongColumnStatsData longStats = statsObj.getStatsData().getLongStats();
        mColStats.setLongStats(longStats.getNumNulls(), longStats.getNumDVs(),
            longStats.getLowValue(), longStats.getHighValue());
      } else if (statsObj.getStatsData().isSetDoubleStats()) {
        DoubleColumnStatsData doubleStats = statsObj.getStatsData().getDoubleStats();
        mColStats.setDoubleStats(doubleStats.getNumNulls(), doubleStats.getNumDVs(),
            doubleStats.getLowValue(), doubleStats.getHighValue());
      } else if (statsObj.getStatsData().isSetStringStats()) {
        StringColumnStatsData stringStats = statsObj.getStatsData().getStringStats();
        mColStats.setStringStats(stringStats.getNumNulls(), stringStats.getNumDVs(),
          stringStats.getMaxColLen(), stringStats.getAvgColLen());
      } else if (statsObj.getStatsData().isSetBinaryStats()) {
        BinaryColumnStatsData binaryStats = statsObj.getStatsData().getBinaryStats();
        mColStats.setBinaryStats(binaryStats.getNumNulls(), binaryStats.getMaxColLen(),
          binaryStats.getAvgColLen());
      }
      return mColStats;
   }
 
   private ColumnStatisticsObj getTableColumnStatisticsObj(MTableColumnStatistics mStatsObj) {
     ColumnStatisticsObj statsObj = new ColumnStatisticsObj();
     statsObj.setColType(mStatsObj.getColType());
     statsObj.setColName(mStatsObj.getColName());
     String colType = mStatsObj.getColType();
     ColumnStatisticsData colStatsData = new ColumnStatisticsData();
 
     if (colType.equalsIgnoreCase("boolean")) {
       BooleanColumnStatsData boolStats = new BooleanColumnStatsData();
       boolStats.setNumFalses(mStatsObj.getNumFalses());
       boolStats.setNumTrues(mStatsObj.getNumTrues());
       boolStats.setNumNulls(mStatsObj.getNumNulls());
       colStatsData.setBooleanStats(boolStats);
     } else if (colType.equalsIgnoreCase("string")) {
       StringColumnStatsData stringStats = new StringColumnStatsData();
       stringStats.setNumNulls(mStatsObj.getNumNulls());
       stringStats.setAvgColLen(mStatsObj.getAvgColLen());
       stringStats.setMaxColLen(mStatsObj.getMaxColLen());
       stringStats.setNumDVs(mStatsObj.getNumDVs());
       colStatsData.setStringStats(stringStats);
     } else if (colType.equalsIgnoreCase("binary")) {
       BinaryColumnStatsData binaryStats = new BinaryColumnStatsData();
       binaryStats.setNumNulls(mStatsObj.getNumNulls());
       binaryStats.setAvgColLen(mStatsObj.getAvgColLen());
       binaryStats.setMaxColLen(mStatsObj.getMaxColLen());
       colStatsData.setBinaryStats(binaryStats);
     } else if (colType.equalsIgnoreCase("bigint") || colType.equalsIgnoreCase("int") ||
         colType.equalsIgnoreCase("smallint") || colType.equalsIgnoreCase("tinyint") ||
         colType.equalsIgnoreCase("timestamp")) {
       LongColumnStatsData longStats = new LongColumnStatsData();
       longStats.setNumNulls(mStatsObj.getNumNulls());
       longStats.setHighValue(mStatsObj.getLongHighValue());
       longStats.setLowValue(mStatsObj.getLongLowValue());
       longStats.setNumDVs(mStatsObj.getNumDVs());
       colStatsData.setLongStats(longStats);
    } else if (colType.equalsIgnoreCase("double") || colType.equalsIgnoreCase("float")) {
      DoubleColumnStatsData doubleStats = new DoubleColumnStatsData();
      doubleStats.setNumNulls(mStatsObj.getNumNulls());
      doubleStats.setHighValue(mStatsObj.getDoubleHighValue());
      doubleStats.setLowValue(mStatsObj.getDoubleLowValue());
      doubleStats.setNumDVs(mStatsObj.getNumDVs());
      colStatsData.setDoubleStats(doubleStats);
    }
    statsObj.setStatsData(colStatsData);
    return statsObj;
   }
 
   private ColumnStatisticsDesc getTableColumnStatisticsDesc(MTableColumnStatistics mStatsObj) {
     ColumnStatisticsDesc statsDesc = new ColumnStatisticsDesc();
     statsDesc.setIsTblLevel(true);
     statsDesc.setDbName(mStatsObj.getDbName());
     statsDesc.setTableName(mStatsObj.getTableName());
     statsDesc.setLastAnalyzed(mStatsObj.getLastAnalyzed());
     return statsDesc;
   }
 
   private ColumnStatistics convertToTableColumnStatistics(MTableColumnStatistics mStatsObj)
     throws MetaException
   {
     if (mStatsObj == null) {
       return null;
     }
 
     ColumnStatisticsDesc statsDesc = getTableColumnStatisticsDesc(mStatsObj);
     ColumnStatisticsObj statsObj = getTableColumnStatisticsObj(mStatsObj);
     List<ColumnStatisticsObj> statsObjs = new ArrayList<ColumnStatisticsObj>();
     statsObjs.add(statsObj);
 
     ColumnStatistics colStats = new ColumnStatistics();
     colStats.setStatsDesc(statsDesc);
     colStats.setStatsObj(statsObjs);
     return colStats;
   }
 
   private MPartitionColumnStatistics convertToMPartitionColumnStatistics(ColumnStatisticsDesc
     statsDesc, ColumnStatisticsObj statsObj, List<String> partVal)
     throws MetaException, NoSuchObjectException
   {
     if (statsDesc == null || statsObj == null || partVal == null) {
       return null;
     }
 
     // TODO: fix it
     MPartition partition  = getMPartition(statsDesc.getDbName(), statsDesc.getTableName(), partVal.toString());
 
     if (partition == null) {
       throw new NoSuchObjectException("Partition for which stats is gathered doesn't exist.");
     }
 
     MPartitionColumnStatistics mColStats = new MPartitionColumnStatistics();
     mColStats.setPartition(partition);
     mColStats.setDbName(statsDesc.getDbName());
     mColStats.setTableName(statsDesc.getTableName());
     mColStats.setPartitionName(statsDesc.getPartName());
     mColStats.setLastAnalyzed(statsDesc.getLastAnalyzed());
     mColStats.setColName(statsObj.getColName());
     mColStats.setColType(statsObj.getColType());
 
     if (statsObj.getStatsData().isSetBooleanStats()) {
       BooleanColumnStatsData boolStats = statsObj.getStatsData().getBooleanStats();
       mColStats.setBooleanStats(boolStats.getNumTrues(), boolStats.getNumFalses(),
           boolStats.getNumNulls());
     } else if (statsObj.getStatsData().isSetLongStats()) {
       LongColumnStatsData longStats = statsObj.getStatsData().getLongStats();
       mColStats.setLongStats(longStats.getNumNulls(), longStats.getNumDVs(),
           longStats.getLowValue(), longStats.getHighValue());
     } else if (statsObj.getStatsData().isSetDoubleStats()) {
       DoubleColumnStatsData doubleStats = statsObj.getStatsData().getDoubleStats();
       mColStats.setDoubleStats(doubleStats.getNumNulls(), doubleStats.getNumDVs(),
           doubleStats.getLowValue(), doubleStats.getHighValue());
     } else if (statsObj.getStatsData().isSetStringStats()) {
       StringColumnStatsData stringStats = statsObj.getStatsData().getStringStats();
       mColStats.setStringStats(stringStats.getNumNulls(), stringStats.getNumDVs(),
         stringStats.getMaxColLen(), stringStats.getAvgColLen());
     } else if (statsObj.getStatsData().isSetBinaryStats()) {
       BinaryColumnStatsData binaryStats = statsObj.getStatsData().getBinaryStats();
       mColStats.setBinaryStats(binaryStats.getNumNulls(), binaryStats.getMaxColLen(),
         binaryStats.getAvgColLen());
     }
     return mColStats;
   }
 
   private void writeMTableColumnStatistics(MTableColumnStatistics mStatsObj)
     throws NoSuchObjectException, MetaException, InvalidObjectException, InvalidInputException
   {
      String dbName = mStatsObj.getDbName();
      String tableName = mStatsObj.getTableName();
      String colName = mStatsObj.getColName();
 
      LOG.info("Updating table level column statistics for db=" + dbName + " tableName=" + tableName
        + " colName=" + colName);
 
      MTable mTable = getMTable(mStatsObj.getDbName(), mStatsObj.getTableName());
      boolean foundCol = false;
 
      if (mTable == null) {
         throw new
           NoSuchObjectException("Table " + tableName +
           " for which stats gathering is requested doesn't exist.");
       }
 
       MStorageDescriptor mSDS = mTable.getSd();
       List<MFieldSchema> colList = mSDS.getCD().getCols();
 
       for(MFieldSchema mCol:colList) {
         if (mCol.getName().equals(mStatsObj.getColName().trim())) {
           foundCol = true;
           break;
         }
       }
 
       if (!foundCol) {
         throw new
           NoSuchObjectException("Column " + colName +
           " for which stats gathering is requested doesn't exist.");
       }
 
       MTableColumnStatistics oldStatsObj = getMTableColumnStatistics(dbName, tableName, colName);
 
       if (oldStatsObj != null) {
        oldStatsObj.setAvgColLen(mStatsObj.getAvgColLen());
        oldStatsObj.setLongHighValue(mStatsObj.getLongHighValue());
        oldStatsObj.setDoubleHighValue(mStatsObj.getDoubleHighValue());
        oldStatsObj.setLastAnalyzed(mStatsObj.getLastAnalyzed());
        oldStatsObj.setLongLowValue(mStatsObj.getLongLowValue());
        oldStatsObj.setDoubleLowValue(mStatsObj.getDoubleLowValue());
        oldStatsObj.setMaxColLen(mStatsObj.getMaxColLen());
        oldStatsObj.setNumDVs(mStatsObj.getNumDVs());
        oldStatsObj.setNumFalses(mStatsObj.getNumFalses());
        oldStatsObj.setNumTrues(mStatsObj.getNumTrues());
        oldStatsObj.setNumNulls(mStatsObj.getNumNulls());
       } else {
         pm.makePersistent(mStatsObj);
       }
    }
 
   private ColumnStatisticsObj getPartitionColumnStatisticsObj(MPartitionColumnStatistics mStatsObj)
   {
     ColumnStatisticsObj statsObj = new ColumnStatisticsObj();
     statsObj.setColType(mStatsObj.getColType());
     statsObj.setColName(mStatsObj.getColName());
     String colType = mStatsObj.getColType();
     ColumnStatisticsData colStatsData = new ColumnStatisticsData();
 
     if (colType.equalsIgnoreCase("boolean")) {
       BooleanColumnStatsData boolStats = new BooleanColumnStatsData();
       boolStats.setNumFalses(mStatsObj.getNumFalses());
       boolStats.setNumTrues(mStatsObj.getNumTrues());
       boolStats.setNumNulls(mStatsObj.getNumNulls());
       colStatsData.setBooleanStats(boolStats);
     } else if (colType.equalsIgnoreCase("string")) {
       StringColumnStatsData stringStats = new StringColumnStatsData();
       stringStats.setNumNulls(mStatsObj.getNumNulls());
       stringStats.setAvgColLen(mStatsObj.getAvgColLen());
       stringStats.setMaxColLen(mStatsObj.getMaxColLen());
       stringStats.setNumDVs(mStatsObj.getNumDVs());
       colStatsData.setStringStats(stringStats);
     } else if (colType.equalsIgnoreCase("binary")) {
       BinaryColumnStatsData binaryStats = new BinaryColumnStatsData();
       binaryStats.setNumNulls(mStatsObj.getNumNulls());
       binaryStats.setAvgColLen(mStatsObj.getAvgColLen());
       binaryStats.setMaxColLen(mStatsObj.getMaxColLen());
       colStatsData.setBinaryStats(binaryStats);
     } else if (colType.equalsIgnoreCase("tinyint") || colType.equalsIgnoreCase("smallint") ||
         colType.equalsIgnoreCase("int") || colType.equalsIgnoreCase("bigint") ||
         colType.equalsIgnoreCase("timestamp")) {
       LongColumnStatsData longStats = new LongColumnStatsData();
       longStats.setNumNulls(mStatsObj.getNumNulls());
       longStats.setHighValue(mStatsObj.getLongHighValue());
       longStats.setLowValue(mStatsObj.getLongLowValue());
       longStats.setNumDVs(mStatsObj.getNumDVs());
       colStatsData.setLongStats(longStats);
    } else if (colType.equalsIgnoreCase("double") || colType.equalsIgnoreCase("float")) {
      DoubleColumnStatsData doubleStats = new DoubleColumnStatsData();
      doubleStats.setNumNulls(mStatsObj.getNumNulls());
      doubleStats.setHighValue(mStatsObj.getDoubleHighValue());
      doubleStats.setLowValue(mStatsObj.getDoubleLowValue());
      doubleStats.setNumDVs(mStatsObj.getNumDVs());
      colStatsData.setDoubleStats(doubleStats);
    }
    statsObj.setStatsData(colStatsData);
    return statsObj;
   }
 
   private ColumnStatisticsDesc getPartitionColumnStatisticsDesc(
     MPartitionColumnStatistics mStatsObj) {
     ColumnStatisticsDesc statsDesc = new ColumnStatisticsDesc();
     statsDesc.setIsTblLevel(false);
     statsDesc.setDbName(mStatsObj.getDbName());
     statsDesc.setTableName(mStatsObj.getTableName());
     statsDesc.setPartName(mStatsObj.getPartitionName());
     statsDesc.setLastAnalyzed(mStatsObj.getLastAnalyzed());
     return statsDesc;
   }
 
   private void writeMPartitionColumnStatistics(MPartitionColumnStatistics mStatsObj,
     List<String> partVal) throws NoSuchObjectException, MetaException, InvalidObjectException,
     InvalidInputException
   {
     String dbName = mStatsObj.getDbName();
     String tableName = mStatsObj.getTableName();
     String partName = mStatsObj.getPartitionName();
     String colName = mStatsObj.getColName();
 
     LOG.info("Updating partition level column statistics for db=" + dbName + " tableName=" +
       tableName + " partName=" + partName + " colName=" + colName);
 
     MTable mTable = getMTable(mStatsObj.getDbName(), mStatsObj.getTableName());
     boolean foundCol = false;
 
     if (mTable == null) {
       throw new
         NoSuchObjectException("Table " + tableName +
         " for which stats gathering is requested doesn't exist.");
     }
 
     // TODO: fix it
     MPartition mPartition =
                  getMPartition(mStatsObj.getDbName(), mStatsObj.getTableName(), partVal.toString());
 
     if (mPartition == null) {
       throw new
         NoSuchObjectException("Partition " + partName +
         " for which stats gathering is requested doesn't exist");
     }
 
     MStorageDescriptor mSDS = mPartition.getSd();
     List<MFieldSchema> colList = mSDS.getCD().getCols();
 
     for(MFieldSchema mCol:colList) {
       if (mCol.getName().equals(mStatsObj.getColName().trim())) {
         foundCol = true;
         break;
       }
     }
 
     if (!foundCol) {
       throw new
         NoSuchObjectException("Column " + colName +
         " for which stats gathering is requested doesn't exist.");
     }
 
     MPartitionColumnStatistics oldStatsObj = getMPartitionColumnStatistics(dbName, tableName,
                                                                partName, partVal, colName);
     if (oldStatsObj != null) {
       oldStatsObj.setAvgColLen(mStatsObj.getAvgColLen());
       oldStatsObj.setLongHighValue(mStatsObj.getLongHighValue());
       oldStatsObj.setDoubleHighValue(mStatsObj.getDoubleHighValue());
       oldStatsObj.setLastAnalyzed(mStatsObj.getLastAnalyzed());
       oldStatsObj.setLongLowValue(mStatsObj.getLongLowValue());
       oldStatsObj.setDoubleLowValue(mStatsObj.getDoubleLowValue());
       oldStatsObj.setMaxColLen(mStatsObj.getMaxColLen());
       oldStatsObj.setNumDVs(mStatsObj.getNumDVs());
       oldStatsObj.setNumFalses(mStatsObj.getNumFalses());
       oldStatsObj.setNumTrues(mStatsObj.getNumTrues());
       oldStatsObj.setNumNulls(mStatsObj.getNumNulls());
     } else {
       pm.makePersistent(mStatsObj);
     }
  }
 
   public boolean updateTableColumnStatistics(ColumnStatistics colStats)
     throws NoSuchObjectException, MetaException, InvalidObjectException, InvalidInputException
   {
     boolean committed = false;
 
     try {
       openTransaction();
       List<ColumnStatisticsObj> statsObjs = colStats.getStatsObj();
       ColumnStatisticsDesc statsDesc = colStats.getStatsDesc();
 
       for (ColumnStatisticsObj statsObj:statsObjs) {
           MTableColumnStatistics mStatsObj = convertToMTableColumnStatistics(statsDesc, statsObj);
           writeMTableColumnStatistics(mStatsObj);
       }
       committed = commitTransaction();
       return committed;
     } finally {
       if (!committed) {
         rollbackTransaction();
       }
     }
  }
 
   public boolean updatePartitionColumnStatistics(ColumnStatistics colStats, List<String> partVals)
     throws NoSuchObjectException, MetaException, InvalidObjectException, InvalidInputException
   {
     boolean committed = false;
 
     try {
     openTransaction();
     List<ColumnStatisticsObj> statsObjs = colStats.getStatsObj();
     ColumnStatisticsDesc statsDesc = colStats.getStatsDesc();
 
     for (ColumnStatisticsObj statsObj:statsObjs) {
         MPartitionColumnStatistics mStatsObj =
             convertToMPartitionColumnStatistics(statsDesc, statsObj, partVals);
         writeMPartitionColumnStatistics(mStatsObj, partVals);
     }
     committed = commitTransaction();
     return committed;
     } finally {
       if (!committed) {
         rollbackTransaction();
       }
     }
   }
 
   private MTableColumnStatistics getMTableColumnStatistics(String dbName, String tableName,
     String colName) throws NoSuchObjectException, InvalidInputException, MetaException
   {
     boolean committed = false;
 
     if (dbName == null) {
       dbName = MetaStoreUtils.DEFAULT_DATABASE_NAME;
     }
 
     if (tableName == null || colName == null) {
       throw new InvalidInputException("TableName/ColName passed to get_table_column_statistics " +
       "is null");
     }
 
     try {
       openTransaction();
       MTableColumnStatistics mStatsObj = null;
       MTable mTable = getMTable(dbName.trim(), tableName.trim());
       boolean foundCol = false;
 
       if (mTable == null) {
         throw new NoSuchObjectException("Table " + tableName +
         " for which stats is requested doesn't exist.");
       }
 
       MStorageDescriptor mSDS = mTable.getSd();
       List<MFieldSchema> colList = mSDS.getCD().getCols();
 
       for(MFieldSchema mCol:colList) {
         if (mCol.getName().equals(colName.trim())) {
           foundCol = true;
           break;
         }
       }
 
       if (!foundCol) {
         throw new NoSuchObjectException("Column " + colName +
         " for which stats is requested doesn't exist.");
       }
 
       Query query = pm.newQuery(MTableColumnStatistics.class);
       query.setFilter("table.tableName == t1 && " +
         "dbName == t2 && " + "colName == t3");
       query
       .declareParameters("java.lang.String t1, java.lang.String t2, java.lang.String t3");
       query.setUnique(true);
 
       mStatsObj = (MTableColumnStatistics) query.execute(tableName.trim(),
                                                         dbName.trim(), colName.trim());
       pm.retrieve(mStatsObj);
       committed = commitTransaction();
       return mStatsObj;
     } finally {
       if (!committed) {
         rollbackTransaction();
         return null;
       }
     }
   }
 
  public ColumnStatistics getTableColumnStatistics(String dbName, String tableName, String colName)
    throws MetaException, NoSuchObjectException, InvalidInputException
   {
     ColumnStatistics statsObj;
     MTableColumnStatistics mStatsObj = getMTableColumnStatistics(dbName, tableName, colName);
 
     if (mStatsObj == null) {
       throw new NoSuchObjectException("Statistics for dbName=" + dbName + " tableName=" + tableName
         + " columnName=" + colName + " doesn't exist.");
     }
 
     statsObj = convertToTableColumnStatistics(mStatsObj);
     return statsObj;
   }
 
   public ColumnStatistics getPartitionColumnStatistics(String dbName, String tableName,
     String partName, List<String> partVal, String colName)
     throws MetaException, NoSuchObjectException, InvalidInputException
   {
     ColumnStatistics statsObj;
     MPartitionColumnStatistics mStatsObj =
           getMPartitionColumnStatistics(dbName, tableName, partName, partVal, colName);
 
     if (mStatsObj == null) {
       throw new NoSuchObjectException("Statistics for dbName=" + dbName + " tableName=" + tableName
           + " partName= " + partName + " columnName=" + colName + " doesn't exist.");
     }
     statsObj = convertToPartColumnStatistics(mStatsObj);
     return statsObj;
   }
 
   private ColumnStatistics convertToPartColumnStatistics(MPartitionColumnStatistics mStatsObj)
   {
     if (mStatsObj == null) {
       return null;
     }
 
     ColumnStatisticsDesc statsDesc = getPartitionColumnStatisticsDesc(mStatsObj);
     ColumnStatisticsObj statsObj = getPartitionColumnStatisticsObj(mStatsObj);
     List<ColumnStatisticsObj> statsObjs = new ArrayList<ColumnStatisticsObj>();
     statsObjs.add(statsObj);
 
     ColumnStatistics colStats = new ColumnStatistics();
     colStats.setStatsDesc(statsDesc);
     colStats.setStatsObj(statsObjs);
     return colStats;
   }
 
   private MPartitionColumnStatistics getMPartitionColumnStatistics(String dbName, String tableName,
     String partName, List<String> partVal, String colName) throws NoSuchObjectException,
     InvalidInputException, MetaException
   {
     boolean committed = false;
     MPartitionColumnStatistics mStatsObj = null;
 
     if (dbName == null) {
       dbName = MetaStoreUtils.DEFAULT_DATABASE_NAME;
     }
 
     if (tableName == null || partVal == null || colName == null) {
       throw new InvalidInputException("TableName/PartName/ColName passed to " +
         " get_partition_column_statistics is null");
     }
 
     try {
       openTransaction();
       MTable mTable = getMTable(dbName.trim(), tableName.trim());
       boolean foundCol = false;
 
       if (mTable == null) {
         throw new NoSuchObjectException("Table "  + tableName +
           " for which stats is requested doesn't exist.");
       }
 
       // TODO: fix it
       MPartition mPartition =
                   getMPartition(dbName, tableName, partVal.toString());
 
       if (mPartition == null) {
         throw new
           NoSuchObjectException("Partition " + partName +
           " for which stats is requested doesn't exist");
       }
 
       MStorageDescriptor mSDS = mPartition.getSd();
       List<MFieldSchema> colList = mSDS.getCD().getCols();
 
       for(MFieldSchema mCol:colList) {
         if (mCol.getName().equals(colName.trim())) {
           foundCol = true;
           break;
         }
       }
 
       if (!foundCol) {
         throw new NoSuchObjectException("Column " + colName +
         " for which stats is requested doesn't exist.");
       }
 
       Query query = pm.newQuery(MPartitionColumnStatistics.class);
       query.setFilter("partition.partitionName == t1 && " +
         "dbName == t2 && " + "tableName == t3 && " + "colName == t4");
       query
       .declareParameters("java.lang.String t1, java.lang.String t2, " +
          "java.lang.String t3, java.lang.String t4");
       query.setUnique(true);
 
       mStatsObj = (MPartitionColumnStatistics) query.executeWithArray(partName.trim(),
                                                         dbName.trim(), tableName.trim(),
                                                         colName.trim());
       pm.retrieve(mStatsObj);
       committed = commitTransaction();
       return mStatsObj;
 
     } finally {
       if (!committed) {
         rollbackTransaction();
        }
     }
   }
 
   public boolean deletePartitionColumnStatistics(String dbName, String tableName,
     String partName, List<String> partVals,String colName)
     throws NoSuchObjectException, MetaException, InvalidObjectException, InvalidInputException
   {
     boolean ret = false;
 
     if (dbName == null) {
       dbName = MetaStoreUtils.DEFAULT_DATABASE_NAME;
     }
 
     if (tableName == null) {
       throw new InvalidInputException("Table name is null.");
     }
 
     try {
       openTransaction();
       MTable mTable = getMTable(dbName, tableName);
       MPartitionColumnStatistics mStatsObj;
       List<MPartitionColumnStatistics> mStatsObjColl;
 
       if (mTable == null) {
         throw new
           NoSuchObjectException("Table " + tableName +
           "  for which stats deletion is requested doesn't exist");
       }
 
       // TODO: fix it
       MPartition mPartition =
           getMPartition(dbName, tableName, partVals);
 
       if (mPartition == null) {
         throw new
           NoSuchObjectException("Partition " + partName +
           " for which stats deletion is requested doesn't exist");
       }
 
       Query query = pm.newQuery(MPartitionColumnStatistics.class);
       String filter;
       String parameters;
 
       if (colName != null) {
         filter = "partition.partitionName == t1 && dbName == t2 && tableName == t3 && " +
                     "colName == t4";
         parameters = "java.lang.String t1, java.lang.String t2, " +
                         "java.lang.String t3, java.lang.String t4";
       } else {
         filter = "partition.partitionName == t1 && dbName == t2 && tableName == t3";
         parameters = "java.lang.String t1, java.lang.String t2, java.lang.String t3";
       }
 
       query.setFilter(filter);
       query
         .declareParameters(parameters);
 
       if (colName != null) {
         query.setUnique(true);
         mStatsObj = (MPartitionColumnStatistics)query.executeWithArray(partName.trim(),
                                                 dbName.trim(), tableName.trim(), colName.trim());
         pm.retrieve(mStatsObj);
 
         if (mStatsObj != null) {
           pm.deletePersistent(mStatsObj);
         } else {
           throw new NoSuchObjectException("Column stats doesn't exist for db=" +dbName + " table="
               + tableName + " partition=" + partName + " col=" + colName);
         }
       } else {
         mStatsObjColl= (List<MPartitionColumnStatistics>)query.execute(partName.trim(),
                                   dbName.trim(), tableName.trim());
         pm.retrieveAll(mStatsObjColl);
 
         if (mStatsObjColl != null) {
           pm.deletePersistentAll(mStatsObjColl);
         } else {
           throw new NoSuchObjectException("Column stats doesn't exist for db=" + dbName +
             " table=" + tableName + " partition" + partName);
         }
       }
       ret = commitTransaction();
     } catch(NoSuchObjectException e) {
        rollbackTransaction();
        throw e;
     } finally {
       if (!ret) {
         rollbackTransaction();
       }
     }
     return ret;
   }
 
   public boolean deleteTableColumnStatistics(String dbName, String tableName, String colName)
     throws NoSuchObjectException, MetaException, InvalidObjectException, InvalidInputException
   {
     boolean ret = false;
 
     if (dbName == null) {
       dbName = MetaStoreUtils.DEFAULT_DATABASE_NAME;
     }
 
     if (tableName == null) {
       throw new InvalidInputException("Table name is null.");
     }
 
     try {
       openTransaction();
       MTable mTable = getMTable(dbName, tableName);
       MTableColumnStatistics mStatsObj;
         List<MTableColumnStatistics> mStatsObjColl;
 
       if (mTable == null) {
         throw new
           NoSuchObjectException("Table " + tableName +
           "  for which stats deletion is requested doesn't exist");
       }
 
       Query query = pm.newQuery(MTableColumnStatistics.class);
       String filter;
       String parameters;
 
       if (colName != null) {
         filter = "table.tableName == t1 && dbName == t2 && colName == t3";
         parameters = "java.lang.String t1, java.lang.String t2, java.lang.String t3";
       } else {
         filter = "table.tableName == t1 && dbName == t2";
         parameters = "java.lang.String t1, java.lang.String t2";
       }
 
       query.setFilter(filter);
       query
         .declareParameters(parameters);
 
       if (colName != null) {
         query.setUnique(true);
         mStatsObj = (MTableColumnStatistics)query.execute(tableName.trim(),
                                                     dbName.trim(), colName.trim());
         pm.retrieve(mStatsObj);
 
         if (mStatsObj != null) {
           pm.deletePersistent(mStatsObj);
         } else {
           throw new NoSuchObjectException("Column stats doesn't exist for db=" +dbName + " table="
               + tableName + " col=" + colName);
         }
       } else {
         mStatsObjColl= (List<MTableColumnStatistics>)query.execute(tableName.trim(), dbName.trim());
         pm.retrieveAll(mStatsObjColl);
 
         if (mStatsObjColl != null) {
           pm.deletePersistentAll(mStatsObjColl);
         } else {
           throw new NoSuchObjectException("Column stats doesn't exist for db=" + dbName +
             " table=" + tableName);
         }
       }
       ret = commitTransaction();
     } catch(NoSuchObjectException e) {
        rollbackTransaction();
        throw e;
     } finally {
       if (!ret) {
         rollbackTransaction();
       }
     }
     return ret;
   }
 
   @Override
   public long cleanupEvents() {
     boolean commited = false;
     long delCnt;
     LOG.debug("Begin executing cleanupEvents");
     Long expiryTime = HiveConf.getLongVar(getConf(), ConfVars.METASTORE_EVENT_EXPIRY_DURATION) * 1000L;
     Long curTime = System.currentTimeMillis();
     try {
       openTransaction();
       Query query = pm.newQuery(MPartitionEvent.class,"curTime - eventTime > expiryTime");
       query.declareParameters("java.lang.Long curTime, java.lang.Long expiryTime");
       delCnt = query.deletePersistentAll(curTime, expiryTime);
       commited = commitTransaction();
     }
     finally {
       if (!commited) {
         rollbackTransaction();
       }
       LOG.debug("Done executing cleanupEvents");
     }
     return delCnt;
   }
 
   @Override
   public Node findNode(String ip) throws MetaException {
     MNode mn = null;
     boolean commited = false;
     try {
       openTransaction();
       Query query = pm.newQuery(MNode.class, "this.ips.matches(\".*\" + ip + \".*\")");
       query.declareParameters("java.lang.String ip");
       query.setUnique(true);
       mn = (MNode)query.execute(ip);
       if (mn != null) {
         pm.retrieve(mn);
       }
       commited = commitTransaction();
     } finally {
       if (!commited) {
         rollbackTransaction();
       }
     }
 
     if (mn == null) {
       return null;
     } else {
       return convertToNode(mn);
     }
   }
 
   @Override
   public boolean delNode(String node_name) throws MetaException {
     boolean success = false;
     try {
       openTransaction();
       MNode mnode = getMNode(node_name);
       if (mnode != null) {
         pm.deletePersistent(mnode);
       }
       success = commitTransaction();
       if(success) {
         MetaMsgServer.sendMsg( MSGFactory.generateDDLMsg(MSGType.MSG_DEL_NODE,-1,-1,pm,mnode,null));
       }
     } finally {
       if (!success) {
         rollbackTransaction();
       }
     }
     return success;
   }
 
   @Override
   public boolean delSFile(long fid) throws MetaException {
     boolean success = false;
     try {
       openTransaction();
       MFile mf = getMFile(fid);
 //      long db_id = -1;
       HashMap<String, Object> old_params = new HashMap<String, Object>();
 
 
       if (mf != null) {
         pm.retrieveAll(mf);
         String dbName = null,tableName = null;
         if(mf.getTable() != null) {
           tableName = mf.getTable().getTableName();
         }
         if(mf.getTable() != null && mf.getTable().getDatabase() != null) {
           dbName = mf.getTable().getDatabase().getName();
         }
 //        db_id = Long.parseLong(MSGFactory.getIDFromJdoObjectId(pm.getObjectId(mf.getTable().getDatabase()).toString()));
         old_params.put("f_id", mf.getFid());
         old_params.put("db_name", dbName);
         old_params.put("table_name", tableName );
         pm.deletePersistent(mf);
       }
       success = commitTransaction();
 
       if(success) {
         MetaMsgServer.sendMsg(MSGFactory.generateDDLMsg(MSGType.MSG_DEL_FILE, -1l, -1l, pm, mf, old_params));
       }
     } finally {
       if (!success) {
         rollbackTransaction();
       }
     }
     return success;
   }
 
   public boolean delDevice(String devid) throws MetaException {
     boolean success = false;
     try {
       openTransaction();
       MDevice md = getMDevice(devid);
       if (md != null) {
         pm.deletePersistent(md);
       }
       success = commitTransaction();
     } finally {
       if (!success) {
         rollbackTransaction();
       }
     }
     return success;
   }
 
   public boolean delSFileLocation(String devid, String location) throws MetaException {
     boolean success = false;
     try {
       openTransaction();
       MFileLocation mfl = getMFileLocation(devid, location);
       if (mfl != null) {
         pm.deletePersistent(mfl);
       }
       success = commitTransaction();
     } finally {
       if (!success) {
         rollbackTransaction();
       }
     }
     return success;
   }
 
   @Override
   public boolean dropPartitionIndex(Index index, Partition part) throws InvalidObjectException,
       NoSuchObjectException, MetaException {
     boolean success = false;
     try {
       openTransaction();
       MIndex mi = getMIndex(index.getDbName(), index.getOrigTableName(), index.getIndexName());
       MPartition mp = getMPartition(part.getDbName(), part.getTableName(), part.getPartitionName());
       MPartitionIndex mpi = getMPartitionIndex(mi, mp);
       if (mpi != null) {
         pm.deletePersistent(mpi);
       }
       success = commitTransaction();
 
       long db_id = Long.parseLong(MSGFactory.getIDFromJdoObjectId(pm.getObjectId(mpi).toString()));
       if(success) {
         MetaMsgServer.sendMsg( MSGFactory.generateDDLMsg(MSGType.MSG_DEL_PARTITION_INDEX,db_id,-1,pm,mpi,null));
       }
     } finally {
       if (!success) {
         rollbackTransaction();
       }
     }
     return success;
   }
 
   public boolean dropPartitionIndex(Index index, Subpartition part) throws InvalidObjectException,
       NoSuchObjectException, MetaException {
     boolean success = false;
     try {
       openTransaction();
       MIndex mi = getMIndex(index.getDbName(), index.getOrigTableName(), index.getIndexName());
       MPartition mp = getMPartition(part.getDbName(), part.getTableName(), part.getPartitionName());
       MPartitionIndex mpi = getMPartitionIndex(mi, mp);
       if (mpi != null) {
         pm.deletePersistent(mpi);
       }
       success = commitTransaction();
 
       long db_id = Long.parseLong(MSGFactory.getIDFromJdoObjectId(pm.getObjectId(mpi).toString()));
       if(success) {
         MetaMsgServer.sendMsg( MSGFactory.generateDDLMsg(MSGType.MSG_DEL_PARTITION_INDEX,db_id,-1,pm,mpi,null));
       }
     } finally {
       if (!success) {
         rollbackTransaction();
       }
     }
     return success;
   }
 
   private boolean dropPartitionIndexStore(Index index, Partition part, SFile store) throws InvalidObjectException,
       NoSuchObjectException, MetaException {
     boolean success = false;
     try {
       openTransaction();
       MIndex mi = getMIndex(index.getDbName(), index.getOrigTableName(), index.getIndexName());
       MPartition mp = getMPartition(part.getDbName(), part.getTableName(), part.getPartitionName());
       if (mi == null || mp == null) {
         throw new NoSuchObjectException("Partition or Index doesn't exist!");
       }
       MPartitionIndex mpi = getMPartitionIndex(mi, mp);
       MFile mf = getMFile(store.getFid());
       if (mpi == null || mf == null) {
         throw new NoSuchObjectException("PartitionIndex or SFile doesn't exist!");
       }
       MPartitionIndexStore mpis = getMPartitionIndexStore(mpi, mf);
       if (mpis != null) {
         pm.deletePersistent(mpis);
       }
       success = commitTransaction();
     } finally {
       if (!success) {
         rollbackTransaction();
       }
     }
     return success;
   }
 
   private boolean dropSubPartitionIndexStore(Index index, Subpartition subpart, SFile store) throws InvalidObjectException,
       NoSuchObjectException, MetaException {
     boolean success = false;
     try {
       openTransaction();
       MIndex mi = getMIndex(index.getDbName(), index.getOrigTableName(), index.getIndexName());
       MPartition mp = getMPartition(subpart.getDbName(), subpart.getTableName(), subpart.getPartitionName());
       if (mi == null || mp == null) {
         throw new NoSuchObjectException("Subpartition or Index doesn't exist!");
       }
       MPartitionIndex mpi = getMPartitionIndex(mi, mp);
       MFile mf = getMFile(store.getFid());
       if (mpi == null || mf == null) {
         throw new NoSuchObjectException("PartitionIndex or SFile doesn't exist!");
       }
       MPartitionIndexStore mpis = getMPartitionIndexStore(mpi, mf);
       if (mpis != null) {
         pm.deletePersistent(mpis);
       }
       success = commitTransaction();
     } finally {
       if (!success) {
         rollbackTransaction();
       }
     }
     return success;
   }
 
   @Override
   public void createPartitionIndexStores(Index index, Partition part, List<SFile> store,
       List<Long> originFid) throws InvalidObjectException, MetaException {
     boolean success = false;
     try {
       openTransaction();
       for (int i = 0; i < store.size(); i++) {
         createPartitionIndexStore(index, part, store.get(i), originFid.get(i));
       }
       success = commitTransaction();
     } finally {
       if (!success) {
         rollbackTransaction();
       }
     }
   }
 
   @Override
   public void createPartitionIndexStores(Index index, Subpartition part, List<SFile> store,
       List<Long> originFid) throws InvalidObjectException, MetaException {
     boolean success = false;
     try {
       openTransaction();
       for (int i = 0; i < store.size(); i++) {
         createSubPartitionIndexStore(index, part, store.get(i), originFid.get(i));
       }
       success = commitTransaction();
     } finally {
       if (!success) {
         rollbackTransaction();
       }
     }
   }
 
   @Override
   public boolean dropPartitionIndexStores(Index index, Partition part, List<SFile> store)
       throws InvalidObjectException, NoSuchObjectException, MetaException {
     boolean success = false;
     try {
       openTransaction();
       for (int i = 0; i < store.size(); i++) {
         dropPartitionIndexStore(index, part, store.get(i));
       }
       success = commitTransaction();
     } finally {
       if (!success) {
         rollbackTransaction();
       }
     }
     return success;
   }
 
   @Override
   public boolean dropPartitionIndexStores(Index index, Subpartition subpart, List<SFile> store)
       throws InvalidObjectException, NoSuchObjectException, MetaException {
     boolean success = false;
     try {
       openTransaction();
       for (int i = 0; i < store.size(); i++) {
         dropSubPartitionIndexStore(index, subpart, store.get(i));
       }
       success = commitTransaction();
     } finally {
       if (!success) {
         rollbackTransaction();
       }
     }
     return success;
   }
 
   @Override
   public boolean add_datawarehouse_sql(int dwNum, String sql) throws InvalidObjectException,
       MetaException {
     boolean success = false;
     int now = (int)(System.currentTimeMillis()/1000);
     try {
       openTransaction();
       MDirectDDL mdd = new MDirectDDL(dwNum,sql,now,now);
       pm.makePersistent(mdd);
       success = commitTransaction();
       if(success){
         long event_id = 0;
         if(mdd.getDwNum() == 1) {
           event_id = MSGType.MSG_DDL_DIRECT_DW1;
         }
         if(mdd.getDwNum() == 2) {
           event_id = MSGType.MSG_DDL_DIRECT_DW2;
         }
         MetaMsgServer.sendMsg(MSGFactory.generateDDLMsg(event_id, -1l, -1l, pm, mdd, null));
       }
     } finally {
       if (!success) {
         rollbackTransaction();
       }
     }
     return success;
    }
 
   private List<SFileRef> getMPartitionIndexFiles(MPartitionIndex mpi) throws MetaException {
     boolean success = false;
     List<SFileRef> r = new ArrayList<SFileRef>();
 
     try {
       openTransaction();
       Query query = pm.newQuery(MPartitionIndexStore.class, "this.pi.index.indexName == indexName && this.pi.partition.partitionName == partName");
       query.declareParameters("java.lang.String indexName, java.lang.String partName");
       Collection files = (Collection) query.execute(mpi.getIndex().getIndexName(), mpi.getPartition().getPartitionName());
       for (Iterator i = files.iterator(); i.hasNext();) {
         MPartitionIndexStore mpis = (MPartitionIndexStore)i.next();
         SFileRef fr = new SFileRef(convertToSFile(mpis.getIndexFile()), mpis.getOriginFid());
         r.add(fr);
       }
       success = commitTransaction();
     } finally {
       if (!success) {
         rollbackTransaction();
       }
     }
 
     return r;
   }
 
 
   public List<SFileRef> getPartitionIndexFiles(Index index, Partition part)
       throws InvalidObjectException, NoSuchObjectException, MetaException {
     try {
       MPartitionIndex mpi = getPartitionIndex(index, part);
       List<SFileRef> sfr = getMPartitionIndexFiles(mpi);
 
       return sfr;
     } catch (NoSuchObjectException e) {
       return new ArrayList<SFileRef>();
     }
   }
 
 //added by zjw for subparition index files operations
 
 
   @Override
   public List<SFileRef> getSubpartitionIndexFiles(Index index, Subpartition subpart)  throws InvalidObjectException, MetaException{
     try {
       MPartitionIndex mpi = getSubpartitionIndex(index, subpart);
       List<SFileRef> sfr = getMPartitionIndexFiles(mpi);
 
       return sfr;
     } catch (NoSuchObjectException e) {
       return new ArrayList<SFileRef>();
     }
   }
 
   @Override
   public List<Subpartition> getSubpartitions(String dbname, String tbl_name, Partition part)
       throws InvalidObjectException, NoSuchObjectException, MetaException {
     List<Subpartition> subparts = new ArrayList<Subpartition>();
     boolean success = false;
     try {
       openTransaction();
       LOG.debug("Executing getSubpartitions");
       dbname = dbname.toLowerCase().trim();
       tbl_name = tbl_name.toLowerCase().trim();
 //      Query q = pm.newQuery(
 //          "select partitionName from org.apache.hadoop.hive.metastore.model.MPartition "
 //          + "where table.database.name == t1 && table.tableName == t2  && parent == null "
 //          + "order by partitionName asc");
 //      q.declareParameters("java.lang.String t1, java.lang.String t2");
 //      q.setResult("partitionName");
 
       Query q = pm.newQuery(MPartition.class,
           "table.database.name == t1 && table.tableName == t2  && parent != null && parent.partitionName == t3 "
 //          + "order by this.partitionName asc"
               );
       q.declareParameters("java.lang.String t1, java.lang.String t2, java.lang.String t3");
 
       List<MPartition> mparts = (List<MPartition>)q.execute(dbname, tbl_name,part.getPartitionName());
       pm.retrieveAll(mparts);
       for (MPartition mp :  mparts) {
         subparts.add(convertToSubpart(mp));
       }
 
 
       success = commitTransaction();
     } finally {
       if (!success) {
         rollbackTransaction();
       }
     }
     return subparts;
   }
 
   @Override
   public List<BusiTypeColumn> getAllBusiTypeCols() throws MetaException {
 
     List<BusiTypeColumn> btcols = new ArrayList<BusiTypeColumn>();
 
     boolean success = false;
     try {
       openTransaction();
       Query query = pm.newQuery(MBusiTypeColumn.class);
       List<MBusiTypeColumn> mbtcs = (List<MBusiTypeColumn>) query.execute();
       pm.retrieveAll(mbtcs);
       for (Iterator i = mbtcs.iterator(); i.hasNext();) {
         MBusiTypeColumn col = (MBusiTypeColumn)i.next();
         BusiTypeColumn btc = new BusiTypeColumn(col.getBusiType(),
            convertToTable(col.getTable()),col.getColumn());
         btcols.add(btc);
       }
       success = commitTransaction();
     } finally {
       if (!success) {
         rollbackTransaction();
       }
     }
 
     return btcols;
   }
 
   @Override
   public List<BusiTypeDatacenter> get_all_busi_type_datacenters() throws MetaException, TException {
 
     List<BusiTypeDatacenter> bdcs = new ArrayList<BusiTypeDatacenter>();
     boolean success = false;
     try {
       openTransaction();
       Query query = pm.newQuery(MBusiTypeDatacenter.class);
       List<MBusiTypeDatacenter> mbdcs = (List<MBusiTypeDatacenter>) query.execute();
       pm.retrieveAll(mbdcs);
       for (Iterator i = mbdcs.iterator(); i.hasNext();) {
         MBusiTypeDatacenter mdatacenter = (MBusiTypeDatacenter)i.next();
         BusiTypeDatacenter bdc = new BusiTypeDatacenter(mdatacenter.getBusiType(),
             convertToDatabase(mdatacenter.getDb()));
         bdcs.add(bdc);
       }
       success = commitTransaction();
     } finally {
       if (!success) {
         rollbackTransaction();
       }
     }
 
     return bdcs;
   }
 
   @Override
   public void append_busi_type_datacenter(BusiTypeDatacenter busiTypeDatacenter)
       throws InvalidObjectException, MetaException, TException {
     boolean success = false;
     MDatabase mdb = null;
     int now = (int)(System.currentTimeMillis()/1000);
     try {
       openTransaction();
       try{
         mdb = getMDatabase(busiTypeDatacenter.getDb().getName());
       }catch(NoSuchObjectException e){
         LOG.warn("No database:"+busiTypeDatacenter.getDb().getName());
       }
       MBusiTypeDatacenter mtdc = null;
       if(mdb !=null){
         mtdc = new MBusiTypeDatacenter(busiTypeDatacenter.getBusiType(), mdb);
         pm.makePersistent(mtdc);
       }else{
         mtdc = new MBusiTypeDatacenter(busiTypeDatacenter.getBusiType(),
             convertToMDatabase(busiTypeDatacenter.getDb()));
         pm.makePersistent(mtdc);
       }
       success = commitTransaction();
     } finally {
       if (!success) {
         rollbackTransaction();
       }
     }
     return ;
   }
 
   @Override
   public List<Busitype> showBusitypes() throws MetaException, TException {
 
     List<Busitype> bts = new ArrayList<Busitype>();
     boolean success = false;
     try {
       openTransaction();
       Query query = pm.newQuery(MBusitype.class);
       List<MBusitype> mbts = (List<MBusitype>) query.execute();
       pm.retrieveAll(mbts);
       for (Iterator i = mbts.iterator(); i.hasNext();) {
         MBusitype mbt = (MBusitype)i.next();
         Busitype bt = new Busitype(mbt.getBusiname(),mbt.getComment());
         bts.add(bt);
       }
       success = commitTransaction();
     } finally {
       if (!success) {
         rollbackTransaction();
       }
     }
 
     return bts;
   }
 
   @Override
   public int createBusitype(Busitype busitype) throws InvalidObjectException, MetaException,
       TException {
     MBusitype mbt = new MBusitype();
     mbt.setBusiname(busitype.getName());
     mbt.setComment(busitype.getComment());
     boolean success = false;
     int now = (int)(System.currentTimeMillis()/1000);
     try {
       openTransaction();
       pm.makePersistent(mbt);
       success = commitTransaction();
     } finally {
       if (!success) {
         rollbackTransaction();
       }
     }
     if(success){
       return 0 ;
     }else{
       return -1;
     }
   }
 
 /**
  * Cry
  */
 
   @Override
   public boolean addEquipRoom(EquipRoom er) throws MetaException {
     MEquipRoom mer = new MEquipRoom();
     mer.setEqRoomName(er.getEqRoomName());
     mer.setStatus(er.getStatus());
     mer.setComment(er.getComment());
     mer.setGeolocation(convertToMGeoLocation(er.getGeolocation()));
     boolean success = false;
     int now = (int)(System.currentTimeMillis()/1000);
     try {
       openTransaction();
       pm.makePersistent(mer);
       success = commitTransaction();
     } finally {
       if (!success) {
         rollbackTransaction();
       }
     }
     if(success){
       return true ;
     }else{
       return false;
     }
   }
   private MGeoLocation convertToMGeoLocation(GeoLocation gl) {
     if (gl == null) {
       return null;
     }
     return new MGeoLocation(gl.getGeoLocName(),gl.getNation(),gl.getProvince(),gl.getCity(),gl.getDist());
   }
 
   @Override
   public boolean modifyEquipRoom(EquipRoom er) throws MetaException {
     boolean success = false;
     boolean committed = false;
     if (er != null) {
       er.setEqRoomName(er.getEqRoomName());
       er.setStatus(er.getStatus());
       er.setComment(er.getComment());
       er.setGeolocation(er.getGeolocation());
     } else {
       return success;
     }
     try {
       openTransaction();
       pm.makePersistent(er);
       committed = commitTransaction();
     } finally {
       if (!committed) {
         rollbackTransaction();
       } else {
         success = true;
       }
     }
     return true;
   }
   @Override
   public boolean deleteEquipRoom(EquipRoom er) throws MetaException {
     boolean success = false;
     try {
       openTransaction();
       MEquipRoom mer = new MEquipRoom();
       if (mer != null) {
         pm.deletePersistent(mer);
       }
       success = commitTransaction();
     } finally {
       if (!success) {
         rollbackTransaction();
       }
     }
     return success;
   }
 
   @Override
   public List<EquipRoom> listEquipRoom() throws MetaException {
     List<EquipRoom> ers = new ArrayList<EquipRoom>();
     boolean success = false;
     try {
       openTransaction();
       Query query = pm.newQuery(MEquipRoom.class);
       List<MEquipRoom> mers = (List<MEquipRoom>) query.execute();
       pm.retrieveAll(mers);
       for (Iterator i = mers.iterator(); i.hasNext();) {
         MEquipRoom mer = (MEquipRoom)i.next();
         EquipRoom er = new EquipRoom(mer.getEqRoomName(),mer.getStatus());
         if(mer.getGeolocation() !=  null){
           er.setGeolocation(er.getGeolocation());
         }
         ers.add(er);
       }
       success = commitTransaction();
     } finally {
       if (!success) {
         rollbackTransaction();
       }
     }
     return ers;
   }
 
   @Override
   public boolean addGeoLocation(GeoLocation gl) throws MetaException {
     MGeoLocation mgl = new MGeoLocation();
     mgl.setGeoLocName(gl.getGeoLocName());
     mgl.setNation(gl.getNation());
     mgl.setProvince(gl.getProvince());
     mgl.setCity(gl.getCity());
     mgl.setDist(gl.getDist());
     boolean success = false;
     int now = (int)(System.currentTimeMillis()/1000);
     try {
       openTransaction();
       pm.makePersistent(mgl);
       success = commitTransaction();
     } finally {
       if (!success) {
         rollbackTransaction();
       }
     }
     if(success){
       return true ;
     }else{
       return false;
     }
   }
   @Override
   public boolean modifyGeoLocation(GeoLocation gl) throws MetaException {
     boolean success = false;
     boolean committed = false;
     try {
       openTransaction();
       MGeoLocation mgl = convertToMGeoLocation(gl);
       pm.makePersistent(mgl);
       committed = commitTransaction();
     } finally {
       if (!committed) {
         rollbackTransaction();
       } else {
         success = true;
       }
     }
     return true;
   }
   @Override
   public boolean deleteGeoLocation(GeoLocation gl) throws MetaException {
     boolean success = false;
     try {
       openTransaction();
       MGeoLocation mgl = convertToMGeoLocation(gl);
       if (mgl != null) {
         pm.deletePersistent(mgl);
       }
       success = commitTransaction();
     } finally {
       if (!success) {
         rollbackTransaction();
       }
     }
     return success;
   }
 
   @Override
   public List<GeoLocation> listGeoLocation() throws MetaException {
     List<GeoLocation> gls = new ArrayList<GeoLocation>();
     boolean success = false;
     try {
       openTransaction();
       Query query = pm.newQuery(MGeoLocation.class);
       List<MGeoLocation> mgls = (List<MGeoLocation>) query.execute();
       pm.retrieveAll(mgls);
       for (Iterator i = mgls.iterator(); i.hasNext();) {
         MGeoLocation mgl = (MGeoLocation)i.next();
         GeoLocation gl = new GeoLocation(mgl.getGeoLocName(),mgl.getNation(),mgl.getProvince(),mgl.getCity(),mgl.getDist());
         gls.add(gl);
       }
       success = commitTransaction();
     } finally {
       if (!success) {
         rollbackTransaction();
       }
     }
     return gls;
   }
 
   @Override
   public GlobalSchema getSchema(String schema_name) throws NoSuchObjectException, MetaException {
 
     MSchema mSchema = null;
     GlobalSchema schema = null;
     boolean commited = false;
     try {
       openTransaction();
       schema_name = schema_name.toLowerCase().trim();
       Query query = pm.newQuery(MSchema.class, "schemaName == schema_name");
       query.declareParameters("java.lang.String schema_name");
       query.setUnique(true);
       mSchema = (MSchema) query.execute(schema_name);
       if(mSchema != null){
         pm.retrieve(mSchema);
         pm.retrieve(mSchema.getSd());
         pm.retrieve(mSchema.getSd().getCD());
         pm.retrieve(mSchema.getSd().getSerDeInfo());
         schema = convertToSchema(mSchema);
       }
 
       commited = commitTransaction();
     } finally {
       if (!commited) {
         rollbackTransaction();
       }
     }
     if (schema == null) {
       throw new NoSuchObjectException("There is no schema named " + schema_name);
     }
 
 
     return schema;
   }
 
   @Override
   public void createSchema(GlobalSchema schema) throws InvalidObjectException, MetaException {
 
     boolean commited = false;
 
     try {
       LOG.info("in obj createSchema");
       openTransaction();
       MSchema mSchema = convertToMSchema(schema);
       boolean make_schema = false;
       if(mSchema.getSd().getCD().getCols() != null){//增加业务类型查询支持
         List<MBusiTypeColumn> bcs = new ArrayList<MBusiTypeColumn>();
 
         //FIXME 修改业务视图
 //        createBusiTypeCol(mSchema, bcs);
 
         if(!bcs.isEmpty()){
 
           LOG.info("--zjw--MBusiTypeColumn,size:"+bcs.size());
 //          pm.makePersistentAll(bcs);
         }else{
           pm.makePersistent(mSchema);
           make_schema =true;
           LOG.info("--zjw-- view:"+schema.getViewExpandedText()+"--"+schema.getViewOriginalText());
         }
       }
       if(!make_schema){
         pm.makePersistent(mSchema);
       }
 
 
 
       LOG.info("createSchema w/ ID=" + JDOHelper.getObjectId(mSchema));
 
       //FIXME 修改权限
 //      PrincipalPrivilegeSet principalPrivs = schema.getPrivileges();
 //      List<Object> toPersistPrivObjs = new ArrayList<Object>();
 //      if (principalPrivs != null) {
 //        int now = (int)(System.currentTimeMillis()/1000);
 //
 
 //        Map<String, List<PrivilegeGrantInfo>> userPrivs = principalPrivs.getUserPrivileges();
 //        putPersistentPrivObjects(mSchema, toPersistPrivObjs, now, userPrivs, PrincipalType.USER);
 //
 //        Map<String, List<PrivilegeGrantInfo>> groupPrivs = principalPrivs.getGroupPrivileges();
 //        putPersistentPrivObjects(mSchema, toPersistPrivObjs, now, groupPrivs, PrincipalType.GROUP);
 //
 //        Map<String, List<PrivilegeGrantInfo>> rolePrivs = principalPrivs.getRolePrivileges();
 //        putPersistentPrivObjects(mSchema, toPersistPrivObjs, now, rolePrivs, PrincipalType.ROLE);
 //      }
 //      pm.makePersistentAll(toPersistPrivObjs);
 
       commited = commitTransaction();
 

       long db_id = -1;
       if(commited) {
         MetaMsgServer.sendMsg(MSGFactory.generateDDLMsg(MSGType.MSG_CREATE_SCHEMA,db_id,-1, pm, mSchema,null));
       }
     } finally {
       if (!commited) {
         rollbackTransaction();
       }
     }
 
 
   }
 
 
 
 
   /**
    * 修改模式结构的同时，需要修改表和视图
    * @throws NoSuchObjectException
    */
   @Override
   public boolean assiginSchematoDB(String dbName, String schemaName,List<FieldSchema> fileSplitKeys,
       List<FieldSchema> part_keys,List<NodeGroup> ngs) throws InvalidObjectException, NoSuchObjectException, MetaException {
     boolean success = false;
     try {
       openTransaction();
       schemaName = schemaName.toLowerCase();
       dbName = dbName.toLowerCase();
       MSchema mSchema = getMSchema(schemaName);
       if (mSchema == null) {
         throw new InvalidObjectException("schema "+schemaName+" is invalid");
       }
 
       MDatabase mdb = getMDatabase(dbName);
       if (mdb == null) {
         throw new InvalidObjectException("database "+dbName+" is invalid");
       }
 
       MTable mtbl = getMTable(dbName, schemaName);
       if (mSchema == null) {
         throw new MetaException(" schema "+ schemaName +" in db"+ dbName +" is already exist.");
       }
 
       /***
        * 注意！！！！！！！！！
        * 此处新建表，表和schema共用同一个MStorageDescriptor对象！！！
        */
       MTable oldmt = new MTable();
       // For now only alter name, owner, paramters, cols, bucketcols are allowed
       oldmt.setTableName(mSchema.getSchemaName().toLowerCase());
       oldmt.setParameters(mSchema.getParameters());
       oldmt.setOwner(mSchema.getOwner());
       // Fully copy over the contents of the new SD into the old SD,
       // so we don't create an extra SD in the metastore db that has no references.
       copyMSD(mSchema.getSd(), oldmt.getSd());//Schema的修改不涉及修改归属地/分区方法和类型
       oldmt.setDatabase(mdb);
       oldmt.setRetention(mSchema.getRetention());
       oldmt.setPartitionKeys(convertToMFieldSchemas(part_keys));
       oldmt.setPartitionKeys(convertToMFieldSchemas(fileSplitKeys));
       oldmt.setTableType(mSchema.getSchemaType());
       oldmt.setLastAccessTime(mSchema.getLastAccessTime());
       oldmt.setViewOriginalText(mSchema.getViewOriginalText());
       oldmt.setViewExpandedText(mSchema.getViewExpandedText());
       oldmt.setGroupDistribute(MetaUtil.CollectionToHashSet(convertToMNodeGroups(ngs)));
 
       // commit the changes
       success = commitTransaction();
     } finally {
       if (!success) {
         rollbackTransaction();
       }
     }
 
     return success;
   }
 
   /**
    * 修改模式结构的同时，需要修改表和视图
    */
   @Override
   public boolean modifySchema(String schemaName, GlobalSchema newSchema) throws InvalidObjectException, MetaException {
     boolean success = false;
     try {
       openTransaction();
       schemaName = schemaName.toLowerCase();
       MSchema mSchema = convertToMSchema(newSchema);
       List<MTable> mtbls = getMTablesBySchemaName(schemaName);
       if (mSchema == null) {
         throw new InvalidObjectException("new table is invalid");
       }
 
       MSchema oldSchema = null;
       try{
         oldSchema = getMSchema(schemaName);
       }catch(NoSuchObjectException e){
         throw new MetaException("schema " + schemaName + " doesn't exist");
       }
       if (oldSchema == null) {
         throw new MetaException("schema " + schemaName + " doesn't exist");
       }
 
       long db_id = -1;
       ArrayList<MSGFactory.DDLMsg> msgs = new ArrayList<MSGFactory.DDLMsg>();     //先把消息存储起来，修改执行成功了再发
       HashMap<String, Object> params = new HashMap<String, Object>();
 
       //alt schema name
       if(!mSchema.getSchemaName().equals(schemaName))
       {
         params.put("schema_name", mSchema.getSchemaName());
         params.put("old_schema_name", schemaName);
         msgs.add(MSGFactory.generateDDLMsg(MSGType.MSG_MODIFY_SCHEMA_NAME,db_id,-1, pm, oldSchema,params));
       }
       //del col   可以删除多个列,删除多个时,发送多次消息
       if(oldSchema.getSd().getCD().getCols().size() > mSchema.getSd().getCD().getCols().size())
       {
         List<MFieldSchema> oldCols = new ArrayList<MFieldSchema>();
         oldCols.addAll( oldSchema.getSd().getCD().getCols());
         List<MFieldSchema> newCols = new ArrayList<MFieldSchema>();
         newCols.addAll( mSchema.getSd().getCD().getCols());
 
         oldCols.removeAll(newCols);
         for(MFieldSchema omfs : oldCols)
         {
           HashMap<String, Object> ps = new HashMap<String, Object>();
           ps.put("schema_name", schemaName);
           ps.put("column_name",omfs.getName());
           msgs.add(MSGFactory.generateDDLMsg(MSGType.MSG_MODIFY_SCHEMA_DEL_COL,db_id,-1, pm, mSchema.getSd().getCD(),ps));
         }
       }
       //add col
       if(oldSchema.getSd().getCD().getCols().size() < mSchema.getSd().getCD().getCols().size())
       {
         List<MFieldSchema> oldCols = new ArrayList<MFieldSchema>();
         oldCols.addAll( oldSchema.getSd().getCD().getCols());
         List<MFieldSchema> newCols = new ArrayList<MFieldSchema>();
         newCols.addAll( mSchema.getSd().getCD().getCols());
         newCols.removeAll(oldCols);
         for(MFieldSchema nmfs : newCols)
         {
           HashMap<String, Object> ps = new HashMap<String, Object>();
           ps.put("schema_name", schemaName);
           ps.put("column_name",nmfs.getName());
           msgs.add(MSGFactory.generateDDLMsg(MSGType.MSG_MODIFY_SCHEMA_ADD_COL,db_id,-1, pm, mSchema.getSd().getCD(),ps));
         }
       }
       //修改列名，列类型  一次只能修改一个
       if(oldSchema.getSd().getCD().getCols().size() == mSchema.getSd().getCD().getCols().size())
       {
         List<MFieldSchema> oldCols = new ArrayList<MFieldSchema>();
         oldCols.addAll( oldSchema.getSd().getCD().getCols());
         List<MFieldSchema> newCols = new ArrayList<MFieldSchema>();
         newCols.addAll( mSchema.getSd().getCD().getCols());
 
         oldCols.removeAll(mSchema.getSd().getCD().getCols());
         newCols.removeAll(oldSchema.getSd().getCD().getCols());
         if(oldCols.size() == 1 && newCols.size() == 1 && !oldCols.get(0).getName().equals(newCols.get(0).getName()))    //说明只有一列是不同的,且是名字不同
         {
           params.put("schema_name", schemaName);
           params.put("column_name",newCols.get(0).getName());
           params.put("old_column_name", oldCols.get(0).getName());
           msgs.add(MSGFactory.generateDDLMsg(MSGType.MSG_MODIFY_SCHEMA_ALT_COL_NAME,db_id,-1, pm, mSchema.getSd().getCD(),params));
         }
         else if(oldCols.size() == 1 && newCols.size() == 1 && !oldCols.get(0).getType().equals(newCols.get(0).getType()))         //修改了列类型
         {
           params.put("schema_name", schemaName);
           params.put("column_name", oldCols.get(0).getName());
           params.put("column_type",newCols.get(0).getType());
           params.put("old_column_type", oldCols.get(0).getType());
           msgs.add(MSGFactory.generateDDLMsg(MSGType.MSG_MODIFY_SCHEMA_ALT_COL_TYPE,db_id,-1, pm, mSchema.getSd().getCD(),params));
         }
       }
       //alt schema param    不知道判断schema的参数有没有和判断table有不一样的地方
       if(!tableParamEquals(oldSchema.getParameters(), mSchema.getParameters()) )
       {
         params.put("schema_name", schemaName);
         ArrayList<String>  ps = new ArrayList<String>();
         ps.addAll(mSchema.getParameters().keySet());
         params.put("tbl_param_keys",ps);
         msgs.add(MSGFactory.generateDDLMsg(MSGType.MSG_MODIFY_SCHEMA_PARAM,db_id,-1, pm, oldSchema,params));
       }
 
       oldSchema.setSchemaName(mSchema.getSchemaName().toLowerCase());
       oldSchema.setParameters(mSchema.getParameters());
       oldSchema.setOwner(mSchema.getOwner());
       // Fully copy over the contents of the new SD into the old SD,
       // so we don't create an extra SD in the metastore db that has no references.
       copyMSD(mSchema.getSd(), oldSchema.getSd());
       oldSchema.setRetention(mSchema.getRetention());
       oldSchema.setSchemaType(mSchema.getSchemaType());
       oldSchema.setLastAccessTime(mSchema.getLastAccessTime());
       oldSchema.setViewOriginalText(mSchema.getViewOriginalText());
       oldSchema.setViewExpandedText(mSchema.getViewExpandedText());
 
       for(MTable oldmt : mtbls){
         // For now only alter name, owner, paramters, cols, bucketcols are allowed
         oldmt.setTableName(mSchema.getSchemaName().toLowerCase());
         oldmt.setParameters(mSchema.getParameters());
         oldmt.setOwner(mSchema.getOwner());
         // Fully copy over the contents of the new SD into the old SD,
         // so we don't create an extra SD in the metastore db that has no references.
         copyMSD(mSchema.getSd(), oldmt.getSd());//Schema的修改不涉及修改归属地/分区方法和类型
 //        oldmt.setDatabase(mSchema.getDatabase());
         oldmt.setRetention(mSchema.getRetention());
 //        oldmt.setPartitionKeys(mSchema.getPartitionKeys());
 //        oldmt.setTableType(mSchema.getTableType());
         oldmt.setLastAccessTime(mSchema.getLastAccessTime());
         oldmt.setViewOriginalText(mSchema.getViewOriginalText());
         oldmt.setViewExpandedText(mSchema.getViewExpandedText());
       }
 
       // commit the changes
       success = commitTransaction();
 
       if(success) {
         for(MSGFactory.DDLMsg msg : msgs) {
           Object eventObject = msg.getEventObject();
           long id = -1;
           if(eventObject instanceof Long){
             id = (Long)eventObject;
           }else{
             Object objectId = pm.getObjectId(eventObject);
             LOG.info("Sending DDL message:"+msg.getEvent_id()+"---"+objectId.toString());
             try{
               id = Long.parseLong(MSGFactory.getIDFromJdoObjectId(objectId.toString()));
             }catch(Exception e){
               LOG.error(e, e);
             }
           }
           msg.setObject_id(id);
           MetaMsgServer.sendMsg(msg);
         }
       }
     } finally {
       if (!success) {
         rollbackTransaction();
       }
     }
 
     return success;
   }
 
   private List<MTable> getMTablesBySchemaName(String schemaName) {
     List<MTable> mtbls = null;
     boolean commited = false;
     try {
       openTransaction();
       schemaName = schemaName.toLowerCase().trim();
       Query query = pm.newQuery(MTable.class, "schema.schemaName == schemaName");
       query.declareParameters("java.lang.String schemaName");
       mtbls = (List<MTable>) query.execute(schemaName);
       pm.retrieveAll(mtbls);
       commited = commitTransaction();
     } finally {
       if (!commited) {
         rollbackTransaction();
       }
     }
     return mtbls;
   }
 
   /**
    * 删除schema需要完成的工作非常多，包括：
    * 1.删除所有schema相关的业务类型
    * 2.删除列上的所有视图（递归删除视图上的权限）
    * 3.删除所有归属地上的同名table
    * @throws InvalidInputException
    * @throws InvalidObjectException
    * @throws NoSuchObjectExceptionMetaException,
    *
    */
   @Override
   public boolean deleteSchema(String schemaName) throws InvalidObjectException, InvalidInputException,NoSuchObjectException, MetaException {
     boolean success = false;
     try {
       openTransaction();
       MSchema schema = getMSchema(schemaName);
       pm.retrieve(schema);
 
       HashMap<String, Object> old_params = new HashMap<String, Object>();
       old_params.put("schema_name", schemaName);
       LOG.debug("---zy-- in deleteSchema:"+schema == null);
       MSGFactory.DDLMsg msg = MSGFactory.generateDDLMsg(MSGType.MSG_DEL_SCHEMA, -1l, -1l, pm, schema, old_params);
 
       if (schema != null) {
         // first remove all the grants
 //        List<MTablePrivilege> tabGrants = listAllTableGrants(schemaName);
 //        if (tabGrants != null && tabGrants.size() > 0) {
 //          pm.deletePersistentAll(tabGrants);
 //        }
 //        List<MTableColumnPrivilege> tblColGrants = listTableAllColumnGrants(schemaName);
 //        if (tblColGrants != null && tblColGrants.size() > 0) {
 //          pm.deletePersistentAll(tblColGrants);
 //        }
 //
 //        List<MPartitionPrivilege> partGrants = this.listTableAllPartitionGrants(schemaName);
 //        if (partGrants != null && partGrants.size() > 0) {
 //          pm.deletePersistentAll(partGrants);
 //        }
 //
 //        List<MPartitionColumnPrivilege> partColGrants = listTableAllPartitionColumnGrants(schemaName);
 //        if (partColGrants != null && partColGrants.size() > 0) {
 //          pm.deletePersistentAll(partColGrants);
 //        }
 
         //FIXME 删除业务列和相关视图
 
         preDropStorageDescriptor(schema.getSd());
         // then remove the table
         pm.deletePersistentAll(schema);
       }
 
       //@todo test!!!!
       List<MTable> mtbls = getMTablesBySchemaName(schemaName);
       pm.retrieveAll(mtbls);
       if(mtbls != null && !mtbls.isEmpty()){
         for(MTable mtbl : mtbls){
           this.dropTable(mtbl.getDatabase().getName(), mtbl.getTableName());
         }
       }
 
       success = commitTransaction();
 
       //在删除之前先获得各种参数,如果删除成功再发送消息
       if(success) {
         MetaMsgServer.sendMsg(msg);
       }
     } finally {
       if (!success) {
         rollbackTransaction();
       }
     }
     return success;
   }
 
   @Override
   public List<GlobalSchema> listSchemas() throws  MetaException {
     List<GlobalSchema> schemas = null;
     boolean success = false;
     try {
       openTransaction();
       Query query = pm.newQuery(MSchema.class);
       List<MSchema> mschemas = (List<MSchema>) query.execute();
       pm.retrieveAll(mschemas);
       for(MSchema ms : mschemas){
         pm.retrieve(ms.getSd());
         pm.retrieve(ms.getSd().getCD());
       }
       schemas = convertToSchemas(mschemas);
       success = commitTransaction();
 
     } finally {
       if (!success) {
         rollbackTransaction();
       }
     }
     return schemas;
   }
 
   private GlobalSchema convertToSchema(MSchema mschema) throws MetaException {
     GlobalSchema schema = null;
     if(mschema != null) {
         schema = new GlobalSchema(mschema.getSchemaName(), mschema.getOwner(),
             mschema.getCreateTime(), mschema.getLastAccessTime(), mschema.getRetention(),
             convertToStorageDescriptor(mschema.getSd()), mschema.getParameters(),
             mschema.getViewOriginalText(), mschema.getViewExpandedText(), mschema.getSchemaType());
         LOG.debug("---zjw--getColsSize():"+schema.getSd().getColsSize());
     }
     return schema;
   }
 
   private List<GlobalSchema> convertToSchemas(List<MSchema> mschemas) throws MetaException {
     List<GlobalSchema> schemas = null;
     if(mschemas != null) {
       schemas = new ArrayList<GlobalSchema>();
       for(MSchema mschema : mschemas){
         GlobalSchema schema = new GlobalSchema(mschema.getSchemaName(), mschema.getOwner(),
             mschema.getCreateTime(), mschema.getLastAccessTime(), mschema.getRetention(),
             convertToStorageDescriptor(mschema.getSd()), mschema.getParameters(),
             mschema.getViewOriginalText(), mschema.getViewExpandedText(), mschema.getSchemaType());
         schemas.add(schema);
       }
     }
     return schemas;
   }
 
   private List<MSchema> convertToMSchemas(List<GlobalSchema> schemas) throws MetaException {
     List<MSchema> mschemas = null;
     if(mschemas != null) {
       mschemas = new ArrayList<MSchema>();
       for(GlobalSchema schema : schemas){
 
         MSchema mschema = new MSchema(schema.getSchemaName(), convertToMStorageDescriptor(schema.getSd()),
             schema.getCreateTime(), schema.getLastAccessTime(), schema.getRetention(),
              schema.getParameters(),
             schema.getViewOriginalText(), schema.getViewExpandedText(), schema.getSchemaType());
         mschemas.add(mschema);
       }
     }
     return mschemas;
   }
 
   @Override
   public boolean addNodeGroup(NodeGroup ng) throws InvalidObjectException, MetaException {
 
     boolean success = false;
     boolean commited = false;
 
     LOG.info("---zjw--in addNodeGroup");
     try {
       openTransaction();
       MNodeGroup mng = convertToMNodeGroup(ng);
 
       pm.makePersistent(mng);
       pm.makePersistentAll(mng.getNodes());
       commited = commitTransaction();
       if(commited) {
         MetaMsgServer.sendMsg( MSGFactory.generateDDLMsg(MSGType.MSG_NEW_NODEGROUP,-1,-1,pm,mng,null));
       }
       success = true;
     } finally {
       if (!commited) {
         rollbackTransaction();
       }
     }
     return success;
   }
 
   @Override
   public boolean modifyNodeGroup(String ngName,NodeGroup ng) throws InvalidObjectException, MetaException {
     boolean success = false;
     boolean commited = false;
     try {
 
       MNodeGroup mng = null;
       try {
         mng = getMNodeGroup(ngName);
       } catch (NoSuchObjectException e) {
         throw new MetaException("NodeGroup :" + ngName+" does not exist.");
       }
       if (mng == null) {
         throw new MetaException("NodeGroup " + ngName+" does not exist.");
       }
       openTransaction();
       MNodeGroup new_mng = convertToMNodeGroup(ng);
 
 
       mng.setMNodeGroup(new_mng);
 
       pm.makePersistent(mng);
       commited = commitTransaction();
       success = true;
 
       HashMap<String,Object> params = new HashMap<String,Object>();
       params.put("old_nodegroup_name", ngName);
       params.put("nodegroup_name", mng.getNode_group_name());
       if(commited) {
         MetaMsgServer.sendMsg(MSGFactory.generateDDLMsg(MSGType.MSG_MODIFY_NODEGROUP, -1, -1, pm, mng, params));
       }
     } finally {
       if (!commited) {
         rollbackTransaction();
       }
     }
     return success;
   }
 
   @Override
   public boolean deleteNodeGroup(NodeGroup ng) throws MetaException {
     boolean success = false;
     boolean commited = false;
     try {
 
       MNodeGroup mng = null;
       try {
         mng = getMNodeGroup(ng.getNode_group_name());
       } catch (NoSuchObjectException e) {
         throw new MetaException("NodeGroup [" + ng.getNode_group_name()+"] does not exist.");
       }
       if (mng == null) {
         throw new MetaException("NodeGroup " + ng.getNode_group_name()+" does not exist.");
       }
       openTransaction();
 
       pm.deletePersistentAll(mng);// watch here
       commited = commitTransaction();
       success = true;
 
       HashMap<String,Object> params = new HashMap<String,Object>();
       params.put("nodegroup_name", ng.getNode_group_name());
       if(commited) {
         MetaMsgServer.sendMsg(MSGFactory.generateDDLMsg(MSGType.MSG_DEL_NODEGROUP, -1, -1, pm, mng, params));
       }
     } finally {
       if (!commited) {
         rollbackTransaction();
       }
     }
     return success;
   }
 
   @Override
   public List<NodeGroup> listNodeGroups() throws MetaException {
     List<NodeGroup> ngs = null;
     boolean success = false;
     try {
       openTransaction();
       Query query = pm.newQuery(MNodeGroup.class);
       List<MNodeGroup> mngs = (List<MNodeGroup>) query.execute();
       pm.retrieveAll(mngs);
 
       success = commitTransaction();
       ngs = this.convertToNodeGroups(mngs);
     } finally {
       if (!success) {
         rollbackTransaction();
       }
     }
     return ngs;
   }
 
   @Override
   public List<NodeGroup> listDBNodeGroups(String dbName) throws MetaException {
     MDatabase mdb;
     try {
       mdb = this.getMDatabase(dbName);
     } catch (NoSuchObjectException e) {
       LOG.error("No db :"+ dbName);
       return null;
     }
     List<NodeGroup> ngs = new ArrayList<NodeGroup>();
     ngs.addAll(this.convertToNodeGroups(mdb.getNodeGroups()));
     return ngs;
   }
 
   @Override
   public boolean addTableNodeDist(String db, String tab, List<String> ng) throws MetaException {
     boolean success = false;
     boolean commited = false;
     Set<String> ngNameMap = new HashSet<String>();
     if(ng == null || ng.isEmpty()){
       throw new MetaException("No node group defined.");
     }else{
       ngNameMap.addAll(ng);
     }
     try {
       openTransaction();
       MTable mtbl = this.getMTable(db, tab);
       if (mtbl == null) {
         throw new MetaException("tab " + tab + " in db "+db+" does not exist.");
       }
       int now = (int)(System.currentTimeMillis()/1000);
 
 //      List<MNodeGroup> mngs = this.getMNodeGroupByNames(ng);//new ArrayList<MNodeGroup>();
       List<NodeGroup> ngs = this.getNodeGroupByNames(ng);//new ArrayList<MNodeGroup>();
       MNodeGroup mng =  null;
       HashMap<String , MNodeGroup> str2Mng = new HashMap<String, MNodeGroup>();
       for(MNodeGroup mNodeGroup :mtbl.getGroupDistribute()){
         str2Mng.put(mNodeGroup.getNode_group_name(), mNodeGroup);
       }
 
       for(NodeGroup nodeGroup : ngs){
           if(!str2Mng.keySet().contains(nodeGroup.getNode_group_name().toLowerCase())){//always in lowercase,else ask zhuqihan
             try {
               mng = getMNodeGroup(nodeGroup.getNode_group_name());
             } catch (NoSuchObjectException e) {
               LOG.error(e,e);
               throw new MetaException(e.getMessage());
             }
             mtbl.getGroupDistribute().add(mng);//many-to-many delete
             mng.getAttachedtables().add(mtbl);
           }
        }
 
       pm.makePersistent(mtbl);
       pm.makePersistentAll(ngs);// watch here
       commited = commitTransaction();
       success = true;
     } finally {
       if (!commited) {
         rollbackTransaction();
       }
     }
     return success;
   }
 
   @SuppressWarnings("nls")
   private MNodeGroup getMNodeGroup(String nodegroupName) throws NoSuchObjectException {
     MNodeGroup mng = null;
     boolean commited = false;
     if(nodegroupName == null ) {
       return null;
     }
     LOG.info("getMnodeGroup groupName:["+nodegroupName+"]");
     try {
       openTransaction();
       nodegroupName = nodegroupName.toLowerCase().trim();
       Query query = pm.newQuery(MNodeGroup.class, "node_group_name == nodegroupName");
       query.declareParameters("java.lang.String nodegroupName");
       query.setUnique(true);
       mng = (MNodeGroup) query.execute(nodegroupName);
       pm.retrieve(mng);
       commited = commitTransaction();
     } finally {
       if (!commited) {
         rollbackTransaction();
       }
     }
     if (mng == null) {
       throw new NoSuchObjectException("There is no schema named " + nodegroupName);
     }
     return mng;
   }
 
   private List<NodeGroup> getNodeGroupByNames(List<String> ngs)throws MetaException {
     boolean success = false;
 
     if(ngs == null || ngs.isEmpty()){
       return null;
     }
 
     LOG.debug(" JDOQL filter is " + ngs.toString());
     List<NodeGroup> results = new ArrayList<NodeGroup>();
 
     try {
       openTransaction();
 
       StringBuilder sb = new StringBuilder(
           "(");
       int n = 0;
       Map<String, String> params = new HashMap<String, String>();
       for (Iterator<String> itr = ngs.iterator(); itr.hasNext();) {
         String pn = "p" + n;
         n++;
         String part = itr.next();
         params.put(pn, part);
         sb.append("node_group_name == ").append(pn);
         sb.append(" || ");
       }
       sb.setLength(sb.length() - 4); // remove the last " || "
       sb.append(')');
 
       Query query = pm.newQuery(MNodeGroup.class, sb.toString());
 
       LOG.debug(" JDOQL filter is " + sb.toString());
 
       String parameterDeclaration = makeParameterDeclarationString(params);
       query.declareParameters(parameterDeclaration);
       query.setOrdering("node_group_name ascending");
 
       List<MNodeGroup> mngs = (List<MNodeGroup>) query.executeWithMap(params);
       results = convertToNodeGroups(mngs);
 
       query.closeAll();
       success = commitTransaction();
     } finally {
       if (!success) {
         rollbackTransaction();
       }
     }
 
     return results;
   }
 
   private List<NodeGroup> convertToNodeGroups(Collection<MNodeGroup> mngs) {
     List<NodeGroup> ngs = null;
     if(mngs != null) {
       ngs = new ArrayList<NodeGroup>();
       for(MNodeGroup mng : mngs){
         NodeGroup ng = new NodeGroup(mng.getNode_group_name(),
             mng.getComment(),mng.getStatus(),convertToNodeSet(mng.getNodes()));
         ngs.add(ng);
       }
     }
     return ngs;
   }
 
   private List<MNodeGroup> convertToMNodeGroups(List<NodeGroup> ngs) {
     List<MNodeGroup> mngs = null;
     if(mngs != null) {
       mngs = new ArrayList<MNodeGroup>();
       for(NodeGroup ng : ngs){
         MNodeGroup mng = new MNodeGroup(ng.getNode_group_name(),
             ng.getComment(),ng.getStatus(),convertToMNodeSet(ng.getNodes()));
         mngs.add(mng);
       }
     }
     return mngs;
   }
 
   private MNodeGroup convertToMNodeGroup(NodeGroup ng) {
     if(ng.getNodes() != null && !ng.getNodes().isEmpty()){
       for(Node node : ng.getNodes()){
         LOG.info("---zjw--" + node.getNode_name());
       }
     }else{
       LOG.info("---zjw--nodes is null");
     }
 
     MNodeGroup mng = new MNodeGroup(ng.getNode_group_name(),
         ng.getComment(),ng.getStatus(),getMNodeSet(ng.getNodes()));
     for(MNode mNode : mng.getNodes()){
       if(mNode.getNodeGroups() == null){
         mNode.setNodeGroups(new HashSet<MNodeGroup>());
       }
       mNode.getNodeGroups().add(mng);
     }
     return mng;
   }
 
   @Override
   public boolean deleteTableNodeDist(String db, String tab, List<String> ng) throws MetaException {
     boolean success = false;
     boolean commited = false;
     try {
       openTransaction();
       MTable mtbl = this.getMTable(db, tab);
       if (mtbl == null) {
         throw new MetaException("tab " + tab + " in db "+db+" does not exist.");
       }
       int now = (int)(System.currentTimeMillis()/1000);
 
       List<MNodeGroup> mngs = new ArrayList<MNodeGroup>();
       for(MNodeGroup mng : mtbl.getGroupDistribute()){
           for(String ngName:ng){
             if(mng.equals(ngName.toLowerCase())){
               mtbl.getGroupDistribute().remove(mng);//many-to-many delete
               mng.getAttachedtables().remove(mtbl);
               mngs.add(mng);
               break;
             }
           }
 
       }
 
       pm.makePersistent(mtbl);
       pm.makePersistentAll(mngs);// watch here
       commited = commitTransaction();
       success = true;
     } finally {
       if (!commited) {
         rollbackTransaction();
       }
     }
     return success;
   }
 
   @Override
   public List<NodeGroup> listTableNodeDists(String dbName, String tabName) throws MetaException {
     Table tbl = this.getTable(dbName, tabName);
     return tbl.getNodeGroups();
   }
 
   @Override
   public List<NodeGroup> listNodeGroupByNames(List<String> ngNames) throws MetaException {
     return  getNodeGroupByNames(ngNames);
   }
 
 /**
  * Cry ------ NodeAssignment,UserAssignment,RoleAssignment
  */
 
   @Override
   public boolean addNodeAssignment(String nodeName, String dbName) throws MetaException,
       NoSuchObjectException {
     boolean success = false;
     boolean commited = false;
     try {
       openTransaction();
       MDatabase mdb = this.getMDatabase(dbName);
       MNode mnd = this.getMNode(nodeName);
       Set<MNode> nodes = mdb.getNodes();
 //      if (mdb.getNodes() != null) {
 //        throw new MetaException("this " + nodeName + " already exists");
 //      }
       int now = (int) (System.currentTimeMillis() / 1000);
       nodes = new HashSet<MNode>();
       nodes.add(mnd);
       mnd.getDbs().add(mdb);
       mdb.getNodes().add(mnd);
       pm.makePersistent(mnd);
       pm.makePersistent(mdb);
       commited = commitTransaction();
       success = true;
     } finally {
       if (!commited) {
         rollbackTransaction();
       }
     }
     return success;
   }
 
   @Override
   public boolean deleteNodeAssignment(String nodeName, String dbName) throws MetaException,
       NoSuchObjectException {
     boolean success = false;
     boolean commited = false;
     try {
       openTransaction();
       MDatabase mdb = this.getMDatabase(dbName);
       MNode mnd = this.getMNode(nodeName);
       Set<MNode> nodes = mdb.getNodes();
 //      if (mdb.getNodes() != null) {
 //        throw new MetaException("this " + nodeName + " already exists");
 //      }
       int now = (int) (System.currentTimeMillis() / 1000);
       nodes = new HashSet<MNode>();
       nodes.add(mnd);
       mnd.getDbs().remove(mdb);
       mdb.getNodes().remove(mnd);
       pm.makePersistent(mnd);
       pm.makePersistent(mdb);
 //      pm.deletePersistentAll(nodes);
       commited = commitTransaction();
       success = true;
     } finally {
       if (!commited) {
         rollbackTransaction();
       }
     }
     return success;
   }
 
   @Override
   public GeoLocation getGeoLocationByName(String geoLocName) throws MetaException {
 //    List<GeoLocation> gls = new ArrayList<GeoLocation>();
     GeoLocation gl = null;
     boolean committed = false;
     try {
       openTransaction();//创建并开始一个事务
       Query query = pm.newQuery(MGeoLocation.class);//设置这个query作用的范围，即查询的是那个表或记录集
       query.setFilter("geoLocName == \"geoLocName\"");
       query.declareParameters("java.lang.String geoLocName");
 //      gl =  (GeoLocation) query.execute(geoLocName);
 //      query.setUnique(true);//设置返回的结果是唯一的
       MGeoLocation mgl=(MGeoLocation)query.execute(geoLocName);
       LOG.info("++++++++++++++++++++++++++++++MGeoLocation" + mgl.getGeoLocName());
       gl = convertToGeoLocation(mgl);
       committed = commitTransaction();
     } finally {
       if (!committed) {
         rollbackTransaction();
       }
     }
     return gl;
   }
 
   private GeoLocation convertToGeoLocation(MGeoLocation mgl) {
     if (mgl == null) {
       return null;
     }
     return new GeoLocation(mgl.getGeoLocName(),mgl.getNation(),mgl.getProvince(),mgl.getCity(),mgl.getDist());
   }
 
   @Override
   public List<GeoLocation> getGeoLocationByNames(List<String> geoLocNames) throws MetaException {
     List<MGeoLocation> mgls = getMGeoLocationByNames(geoLocNames);
     return convertToGeoLocations(mgls);
   }
 
   private List<MGeoLocation> getMGeoLocationByNames(List<String> geoLocNames) throws MetaException {
     boolean success = false;
     List<MGeoLocation> results = new ArrayList<MGeoLocation>();
     try {
       openTransaction();
       StringBuilder sb = new StringBuilder(
           "(");
       int n = 0;
       Map<String, String> params = new HashMap<String, String>();
       for (Iterator<String> itr = geoLocNames.iterator(); itr.hasNext();) {
         String pn = "p" + n;
         n++;
         String part = itr.next();
         params.put(pn, part);
         sb.append("geoLocName == ").append(pn);
         sb.append(" || ");
       }
       sb.setLength(sb.length() - 4);
       sb.append(')');
       Query query = pm.newQuery(MGeoLocation.class, sb.toString());
       String parameterDeclaration = makeParameterDeclarationString(params);
       query.declareParameters(parameterDeclaration);
       query.setOrdering("geoLocName ascending");
       results = (List<MGeoLocation>) query.executeWithMap(params);
       query.closeAll();
       success = commitTransaction();
     } finally {
       if (!success) {
         rollbackTransaction();
       }
     }
     return results;
   }
 
   private List<GeoLocation> convertToGeoLocations(Collection<MGeoLocation> mgls) {
     List<GeoLocation> gls = null;
     if(mgls != null) {
       gls = new ArrayList<GeoLocation>();
       for(MGeoLocation mgl : mgls){
         GeoLocation gl = new GeoLocation(mgl.getGeoLocName(),mgl.getNation(),mgl.getProvince(),mgl.getCity(),mgl.getDist());
         gls.add(gl);
       }
     }
     return gls;
   }
 
   @Override
   public List<Node> listNodes() throws MetaException {
     List<Node> nds = null;
     boolean success = false;
     try {
       openTransaction();
       Query query = pm.newQuery(MNode.class);
       List<MNode> mnds = (List<MNode>) query.execute();
       pm.retrieveAll(mnds);
 
       success = commitTransaction();
       nds = this.convertToNodes(mnds);
     } finally {
       if (!success) {
         rollbackTransaction();
       }
     }
     return nds;
   }
 
   private List<Node> convertToNodes(List<MNode> mnds) {
     List<Node> nds = null;
     if(mnds != null) {
       nds = new ArrayList<Node>();
       for(MNode mnd : mnds){
         Node nd = new Node(mnd.getNode_name(), mnd.getIPList(), mnd.getStatus());
         nds.add(nd);
       }
     }
     return nds;
   }
 
   @Override
   public boolean addUserAssignment(String userName, String dbName) throws MetaException,
       NoSuchObjectException {
     boolean success = false;
     boolean commited = false;
     try {
       openTransaction();
       MDatabase mdb = this.getMDatabase(dbName);
       MUser muser = this.getMUser(userName);
 //      if (mdb.getUsers() != null) {
 //        throw new MetaException("this" + userName + "already exists");
 //      }
       int now = (int)(System.currentTimeMillis()/1000);
 //      List<User> users = this.getUserByName(userName);
 //      List<MUser> musers  = new ArrayList<MUser>();
 //      musers.add(muser);
       muser.getDbs().add(mdb);
       mdb.getUsers().add(muser);
       pm.makePersistent(mdb);
       pm.makePersistent(muser);
       commited = commitTransaction();
       success = true;
     } finally {
       if (!commited) {
         rollbackTransaction();
       }
     }
     return success;
   }
 
   @Override
   public boolean deleteUserAssignment(String userName, String dbName) throws MetaException,
       NoSuchObjectException {
     boolean success = false;
     boolean commited = false;
     try {
       openTransaction();
       MDatabase mdb = this.getMDatabase(dbName);
       MUser muser = this.getMUser(userName);
 //      if (mdb.getUsers() == null) {
 //        throw new MetaException("this " + userName + " does not exist");
 //      }
       int now = (int)(System.currentTimeMillis()/1000);
 //      List<MUser> musers  = new ArrayList<MUser>();
 //      muser.getDbs().remove(mdb);
 //      mdb.getUsers().remove(muser);
 //      musers.add(muser);
       muser.getDbs().remove(mdb);
       mdb.getUsers().remove(muser);
       pm.makePersistent(mdb);
       pm.makePersistent(muser);
 //      pm.deletePersistentAll(musers);
       commited = commitTransaction();
       success = true;
     } finally {
       if (!commited) {
         rollbackTransaction();
       }
     }
     return success;
   }
 
   @Override
   public List<User> listUsers() throws MetaException {
     List<User> users = null;
     boolean success = false;
     try {
       openTransaction();
       Query query = pm.newQuery(MUser.class);
       List<MUser> musers = (List<MUser>) query.execute();
       pm.retrieveAll(musers);
 
       success = commitTransaction();
       users = this.convertToUsers(musers);
     } finally {
       if (!success) {
         rollbackTransaction();
       }
     }
     return users;
   }
 
   private List<User> convertToUsers(List<MUser> musers) {
     List<User> users = null;
     if(musers != null) {
       users = new ArrayList<User>();
       for(MUser muser : musers){
         User user = new User(muser.getUserName(),muser.getPasswd(),muser.getCreateTime(),muser.getOwnerName());
         users.add(user);
       }
     }
     return users;
   }
 
   @Override
   public boolean addRoleAssignment(String roleName, String dbName) throws MetaException,
       NoSuchObjectException {
     boolean success = false;
     boolean commited = false;
     try {
       openTransaction();
       MDatabase mdb = this.getMDatabase(dbName);
       MRole mrole = this.getMRole(roleName);
 //      if (mdb.getRoles() != null) {
 //        throw new MetaException("this " + roleName + " already exists");
 //      }
       int now = (int)(System.currentTimeMillis()/1000);
 //      List<MRole> mroles  = new ArrayList<MRole>();
 //      mroles.add(mrole);
       mrole.getDbs().add(mdb);
       mdb.getRoles().add(mrole);
       pm.makePersistent(mdb);
       pm.makePersistent(mrole);
       commited = commitTransaction();
       success = true;
     } finally {
       if (!commited) {
         rollbackTransaction();
       }
     }
     return success;
   }
 
   @Override
   public boolean deleteRoleAssignment(String roleName, String dbName) throws MetaException,
       NoSuchObjectException {
     boolean success = false;
     boolean commited = false;
     try {
       openTransaction();
       MDatabase mdb = this.getMDatabase(dbName);
       MRole mrole = this.getMRole(roleName);
 //      if (mdb.getRoles() != null) {
 //        throw new MetaException("this " + roleName + " already exists");
 //      }
       int now = (int)(System.currentTimeMillis()/1000);
 //      List<MRole> mroles  = new ArrayList<MRole>();
 //      mroles.add(mrole);
       mrole.getDbs().remove(mdb);
       mdb.getRoles().remove(mrole);
       pm.makePersistent(mdb);
       pm.makePersistent(mrole);
 //      pm.deletePersistentAll(mroles);
       commited = commitTransaction();
       success = true;
     } finally {
       if (!commited) {
         rollbackTransaction();
       }
     }
     return success;
   }
 
   @Override
   public List<Role> listRoles() throws MetaException {
     List<Role> roles = null;
     boolean success = false;
     try {
       openTransaction();
       Query query = pm.newQuery(MRole.class);
       List<MRole> mroles = (List<MRole>) query.execute();
       pm.retrieveAll(mroles);
 
       success = commitTransaction();
       roles = this.convertToRoles(mroles);
     } finally {
       if (!success) {
         rollbackTransaction();
       }
     }
     return roles;
   }
 
   private List<Role> convertToRoles(List<MRole> mroles) {
     List<Role> roles = null;
     if(mroles != null) {
       roles = new ArrayList<Role>();
       for(MRole mrole : mroles){
         Role role = new Role(mrole.getRoleName(),mrole.getCreateTime(),mrole.getOwnerName());
         roles.add(role);
       }
     }
     return roles;
   }
 
   @Override
   public boolean addNodeGroupAssignment(NodeGroup ng, String dbName) throws MetaException,
       NoSuchObjectException {
     // TODO Auto-generated method stub
     return false;
   }
 
   @Override
   public boolean deleteNodeGroupAssignment(NodeGroup ng, String dbName) throws MetaException,
       NoSuchObjectException {
     // TODO Auto-generated method stub
     return false;
 
   }
 
   public class MsgHandler
   {
     public void refresh(MSGFactory.DDLMsg msg)
     {
       int eventId = (int) msg.getEvent_id();
       switch(eventId)
       {
         case MSGType.MSG_NEW_TALBE:
           String d1 = (String) msg.getMsg_data().get("db_name");
           String t1 = (String)msg.getMsg_data().get("table_name");
           getMTable(d1,t1);
           break;
         //所有修改表和列的事件，都做同一处理，都是把整个表的缓存更新
         case MSGType.MSG_ALT_TALBE_NAME:
         case MSGType.MSG_ALT_TALBE_DISTRIBUTE:
         case MSGType.MSG_ALT_TALBE_PARTITIONING:
         case MSGType.MSG_ALT_TABLE_SPLITKEYS:
         case MSGType.MSG_ALT_TALBE_DEL_COL:
         case MSGType.MSG_ALT_TALBE_ADD_COL:
         case MSGType.MSG_ALT_TALBE_ALT_COL_NAME:
         case MSGType.MSG_ALT_TALBE_ALT_COL_TYPE:
         case MSGType.MSG_ALT_TABLE_PARAM:
           String dbName = (String) msg.getMsg_data().get("db_name");
           String tableName = (String)msg.getMsg_data().get("table_name");
           MTable mt = getMTable(dbName, tableName);
           //javax.jdo.JDOUserException: Object of type "org.apache.hadoop.hive.metastore.model.MTable"
           //is detached. Detached objects cannot be used with this operation.
           //evict，refresh都报这个错
 //          pm.evict(mt);
 //          pm.refresh(mt);
 //          Object obj = pm.getObjectById(msg.getObject_id() );
 //          pm.refresh(obj);
 //          mt = getMTable(dbName, tableName);
 
 
           break;
 
         //删除表
         case MSGType.MSG_DROP_TABLE:
           String d = (String) msg.getMsg_data().get("db_name");
           String t = (String)msg.getMsg_data().get("table_name");
           pm.evict(getMTable(d,t));
           break;
 
         case MSGType.MSG_ADD_PARTITION_FILE:
         case MSGType.MSG_DEL_PARTITION_FILE:
           break;
         case MSGType.MSG_ALT_PARTITION_FILE:
           //未实现
           break;
         case MSGType.MSG_REP_FILE_CHANGE:
         case MSGType.MSG_STA_FILE_CHANGE:
         case MSGType.MSG_REP_FILE_ONOFF:
         case MSGType.MSG_FILE_USER_SET_REP_CHANGE:
 
           break;
         case MSGType.MSG_NEW_PARTITION_INDEX_FILE:
         case MSGType.MSG_ALT_PARTITION_INDEX_FILE:
         case MSGType.MSG_DEL_PARTITION_INDEX_FILE:
           //未实现
           break;
 
 
       }
 
     }
   }
 
 }
