 package com.pagesociety.bdb;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.nio.channels.FileChannel;
 import java.text.Format;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.Random;
 import java.util.Set;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipOutputStream;
 
 import org.apache.log4j.Logger;
 
 import com.pagesociety.bdb.binding.EntityBinding;
 import com.pagesociety.bdb.binding.EntityDefinitionBinding;
 import com.pagesociety.bdb.binding.EntityRelationshipBinding;
 import com.pagesociety.bdb.binding.EntitySecondaryIndexBinding;
 import com.pagesociety.bdb.binding.FieldBinding;
 import com.pagesociety.bdb.index.EntityIndexDefinition;
 import com.pagesociety.bdb.index.query.QueryManager;
 import com.pagesociety.bdb.index.query.QueryManagerConfig;
 import com.pagesociety.bdb.locker.AdminLocker;
 import com.pagesociety.bdb.locker.Locker;
 import com.pagesociety.bdb.queue.PersistentQueueManager;
 import com.pagesociety.persistence.Entity;
 import com.pagesociety.persistence.EntityDefinition;
 import com.pagesociety.persistence.EntityIndex;
 import com.pagesociety.persistence.EntityRelationshipDefinition;
 import com.pagesociety.persistence.FieldDefinition;
 import com.pagesociety.persistence.PersistenceException;
 import com.pagesociety.persistence.PersistentStore;
 import com.pagesociety.persistence.Query;
 import com.pagesociety.persistence.QueryResult;
 import com.pagesociety.persistence.Types;
 import com.sleepycat.bind.tuple.LongBinding;
 import com.sleepycat.bind.tuple.TupleOutput;
 import com.sleepycat.db.CacheFileStats;
 import com.sleepycat.db.Cursor;
 import com.sleepycat.db.Database;
 import com.sleepycat.db.DatabaseConfig;
 import com.sleepycat.db.DatabaseEntry;
 import com.sleepycat.db.DatabaseException;
 import com.sleepycat.db.DatabaseType;
 import com.sleepycat.db.DeadlockException;
 import com.sleepycat.db.Environment;
 import com.sleepycat.db.EnvironmentConfig;
 import com.sleepycat.db.LockDetectMode;
 import com.sleepycat.db.LockMode;
 import com.sleepycat.db.OperationStatus;
 import com.sleepycat.db.Transaction;
 
 public class BDBStore implements PersistentStore, BDBEntityDefinitionProvider
 {
 	private final int store_major_version = 1;
 	private final int store_minor_version = 0;
 	
 	private static final Logger logger = Logger.getLogger(BDBStore.class);
 	//environment
 
 	private Environment 				environment;
 	// entity def db and binding
 	private Database 					version_db;
 	private Database 					entity_def_db;
 	private Database 					entity_index_db;
 	private Database 					entity_relationship_db; 
 	private EntityBinding 				entity_binding;
 	private EntityDefinitionBinding 	entity_def_binding;
 	private EntitySecondaryIndexBinding entity_index_binding;
 	private EntityRelationshipBinding 	entity_relationship_binding;
 	private BDBEntityDefinitionProvider entity_definition_provider;
 
 	// modules
 	protected BDBEntityIndexDefinitionManager _entity_index_definition_manager;
 	
 	/*deadlocking stuff */
 	public static final int MAX_DEADLOCK_RETRIES 		= 10;
 	private volatile boolean _deadlock_monitor_running  = false;
 	//private int _deadlocking_scheme;
 	Thread _deadlock_monitor;
 	private static final int DEFAULT_MONITOR_INTERVAL_FOR_MONITORING_SCHEME = 3000;
 	
 	//backup stuff//
 	private File backup_root_directory;
 	
 	// primary and secondary databases handles
 	private Map<String,BDBPrimaryIndex> 		  			entity_primary_indexes_as_map;
 	private List<BDBPrimaryIndex> 		  					entity_primary_indexes_as_list;
 	private Map<String, Map<String, BDBSecondaryIndex>> 	entity_secondary_indexes_as_map;
 	private Map<String, List<BDBSecondaryIndex>> 		  	entity_secondary_indexes_as_list;
 
 	private Map<String, Map<String,EntityRelationshipDefinition>> entity_relationship_map;
 
 
 	/* Database operations, used by resolve relationship */
 
 	public static final int INSERT = 0;
 	public static final int UPDATE = 1;
 	public static final int DELETE = 2;
 
 	private Locker _store_locker = null;
 	private CheckpointPolicy checkpoint_policy;
 	private Properties _db_env_props;
 	private File _db_env_props_file;
 	private Map<String, Object> _config;
 	
 	/* BEGIN INTERFACE ***************************************************************************/
 	public void init(Map<String, Object> config) throws PersistenceException
 	{
 		logger.info("Initializing BDBStore V"+store_major_version+"."+store_minor_version);
 		_config = config; 
 
 		entity_primary_indexes_as_map 	 		 = new HashMap<String, BDBPrimaryIndex>();
 		entity_primary_indexes_as_list 	 		 = new ArrayList<BDBPrimaryIndex>();
 		entity_secondary_indexes_as_map 		 = new HashMap<String, Map<String, BDBSecondaryIndex>>();
 		entity_secondary_indexes_as_list 		 = new HashMap<String,List<BDBSecondaryIndex>>();
 		entity_relationship_map 		 	 	 = new HashMap<String, Map<String,EntityRelationshipDefinition>>();
 		entity_binding					 		 = new EntityBinding(); 
 
 		/* order is important */
 		/* check the version of data vs version of code*/
 		init_shutdown_hook();
 		init_locker(config);
 		init_checkpoint_policy(config);
 		init_environment(config);
 		
 		setup_version_file(config);
 		verify_version(config);
 
 		init_entity_definition_db(config);
 		init_entity_definition_provider(config);
 
 		init_entity_secondary_index_db(config);
 		init_entity_relationship_db(config);
 		init_entity_index_definition_manager();
 		init_deadlock_resolution_scheme(config);
 
 
 		init_query_manager();
 		init_queue_manager(config);
 		
 		
 		bootstrap_existing_entity_definitions();
 		bootstrap_existing_indices();
 		bootstrap_existing_entity_relationships();
 		init_field_binding();
 		init_backup_subsystem(config);
 		
 		//start_deadlock_detector();
 		start_deadlock_monitor(1000 * 60 * 3);//3 minutes
 		logger.debug("Init - Complete");
 		
 		
 	}
 	
 	private boolean _shutdown_hook_is_added = false;
 	private void init_shutdown_hook()
 	{
 		if(_shutdown_hook_is_added)
 			return;
 		
 		/*
 		Runtime.getRuntime().addShutdownHook(new Thread()
 		{
 			public void run()
 			{
 				try{
 					System.out.println("SHUTDOWN HOOK IS RUNNING");
 					do_close();
 					
 				}catch(Exception e)
 				{
 					e.printStackTrace();
 				}
 			}
 		});
 		*/
 		_shutdown_hook_is_added = true;
 	}
 	
 	private boolean _locker_is_inited = false;
 	private void init_locker(Map<String, Object> config)
 	{
 		if(_locker_is_inited)
 			return;
 		try {
 			_store_locker = (Locker)Class.forName((String)config.get(BDBStoreConfigKeyValues.KEY_STORE_LOCKER_CLASS)).newInstance();
 		} catch (Exception e) {
 			//e.printStackTrace();
 			_store_locker = new AdminLocker();
 		}
 		logger.debug("init_store_locker(HashMap<Object,Object>) - SETTING STORE LOCKER TO INSTANCE OF " + _store_locker.getClass().getName());
 		_store_locker.init(config);
 		_locker_is_inited = true;
 	}
 	
 	private void init_checkpoint_policy(Map<String,Object> config)
 	{
 		try
 		{
 			checkpoint_policy = (CheckpointPolicy) Class.forName((String) config.get(BDBStoreConfigKeyValues.KEY_STORE_CHECKPOINT_POLICY_CLASS)).newInstance();
 		}
 		catch (Exception e)
 		{
 			checkpoint_policy = new DefaultCheckpointPolicyWithInterval();
 		}
 
 		logger.debug("init_checkpoint_policy(HashMap<Object,Object>) - SETTING CHECKPOINT POLICY TO INSTANCE OF " + checkpoint_policy.getClass().getName());
 		checkpoint_policy.init(this,config);
 	}
 	
 
 	private void init_entity_index_definition_manager() throws PersistenceException
 	{
 		_entity_index_definition_manager = new BDBEntityIndexDefinitionManager();			
 		_entity_index_definition_manager.loadDefinitions();
 	}
 	
 	public void addEntityDefinition(EntityDefinition entity_def) throws PersistenceException
 	{
 		_store_locker.enterLockerThread();
 		try{
 			logger.debug("addEntityDefinition(EntityDefinition) - ADDING ENTITY DEF " + entity_def.getName());
 			if(entity_primary_indexes_as_map.get(entity_def.getName()) != null)
 				throw new PersistenceException("Entity Definition already exists for: "+entity_def.getName()+".Delete existing entity definition first.");			
 			add_entity_definition_to_db(entity_def);
 			BDBPrimaryIndex pidx = new BDBPrimaryIndex(entity_binding);
 			pidx.setup(environment, entity_def);
 			
 			/*do runtime cacheing*/
 			entity_primary_indexes_as_map.put(entity_def.getName(), pidx);
 			entity_primary_indexes_as_list.add(pidx);
 			
 			/* initialize maps for holding indexes bound to entity*/
 			List<BDBSecondaryIndex> sec_indexes_list 	  = new ArrayList<BDBSecondaryIndex>();
 			Map<String,BDBSecondaryIndex> sec_indexes_map = new HashMap<String,BDBSecondaryIndex>();
 			entity_secondary_indexes_as_list.put(entity_def.getName(),sec_indexes_list);
 			entity_secondary_indexes_as_map.put(entity_def.getName(),sec_indexes_map);
 			calculate_query_cache_dependencies(entity_def);
 
 			//TODO talk to registry instead of directly to field binding
 			// or resolve definitions here...
 			FieldBinding.addToPrimaryIndexMap(entity_def.getName(),pidx);
 			
 		}catch(PersistenceException pe)
 		{
 			throw pe;
 		}
 		finally
 		{
 			_store_locker.exitLockerThread();	
 		}
 	}
 
 	/* this just does the insert on the entity def table. the rest of the initialization of other
 	 * entity handles etc happens in init_entity_db. see above.
 	 */
 	private void add_entity_definition_to_db(EntityDefinition entity_def) throws PersistenceException
 	{
 		String ename 	  = entity_def.getName();	
 	
 		Transaction txn = null;		
 		OperationStatus op_status = null;
 		try
 		{			
 			txn = environment.beginTransaction(null, null);
 			DatabaseEntry key = new DatabaseEntry();
 			DatabaseEntry data = new DatabaseEntry();
 
 			FieldBinding.valueToEntry(Types.TYPE_STRING, ename, key);
 			entity_def_binding.objectToEntry(entity_def, data);		
 			op_status = entity_def_db.put(txn, key, data);
 			
 			if(op_status != OperationStatus.SUCCESS)
 			{
 				abortTxn(txn);
 				throw new PersistenceException("Failed Saving Entity Definition: "+ename);
 			}
 			txn.commit();
 
 		}
 		catch (Exception e)
 		{
 			abortTxn(txn);
 			logger.error("add_entity_definition_to_db(EntityDefinition)", e);
 			throw new PersistenceException("Unable to add entity definition " + entity_def + " "
 					+ e.getMessage());
 		}
 	}
 	
 	public void deleteEntityDefinition(String entity_def_name) throws PersistenceException
 	{
 		_store_locker.enterLockerThread();
 		try{
 			do_delete_entity_definition(entity_def_name);
 		
 		}catch(PersistenceException p)
 		{
 			throw p;
 		}
 		finally
 		{
 			_store_locker.exitLockerThread();	
 		}
 	}
 	
 	private void do_delete_entity_definition(String name) throws PersistenceException
 	{
 		BDBPrimaryIndex pidx = entity_primary_indexes_as_map.get(name);
 		if(pidx == null)
 			throw new PersistenceException("DELETE ENTITY DEF: no entity definition for "+name);
 		
 		for(BDBPrimaryIndex p:entity_primary_indexes_as_list)
 		{
 			EntityDefinition d = p.getEntityDefinition();
 			if(d.getName().equals(name))
 				continue;
 			for(FieldDefinition f:d.getFields())
 			{
 				if(f.getBaseType() == Types.TYPE_REFERENCE)
 				{
 					if(f.getReferenceType().equals(name))
 						throw new PersistenceException("ENTITY FIELD "+f.getName()+" IN "+d.getName()+
 								" IS A REFERENCE TO TYPE "+name+". REMOVE THIS FIELD IN ORDER TO DELETE "+name);
 				}
 			}
 		}
 		
 		EntityDefinition def = pidx.getEntityDefinition();
 		String ename = def.getName();
 		try{
 			logger.debug("do_delete_entity_definition(String) - DELETEING ENTITY DEF " + ename);
 			delete_entity_definition_from_db(ename);
 			entity_primary_indexes_as_map.remove(ename);
 			entity_primary_indexes_as_list.remove(pidx);
 			remove_query_cache_dependencies(def);
 			pidx.delete();
 			
 			/* right now entityindexes are always attached to primaryindexes so we
 			 * blast all of them away too
 			 */
 			List<BDBSecondaryIndex> sec_indexes = entity_secondary_indexes_as_list.get(ename);
 			for(int i = 0; i < sec_indexes.size();i++)
 			{
 				BDBSecondaryIndex s_idx = sec_indexes.get(i);
 				s_idx.delete();
 				/*remove it from the entity_index table */
 				delete_entity_index_from_db(ename, s_idx.getEntityIndex());
 			}
 			entity_secondary_indexes_as_list.remove(ename);
 			entity_secondary_indexes_as_map.remove(ename);
 		}catch(DatabaseException de)
 		{
 			logger.error("do_delete_entity_definition(String)", de);
 			throw new PersistenceException("UNABLE TO DELETE ENTITY DEF FOR "+ename+".INTERNAL ERROR. SEE LOGS");
 		}
 	}
 	
 	public void renameEntityDefinition(String ename,String new_name) throws PersistenceException
 	{	
 		_store_locker.enterLockerThread();
 		try{
 			do_rename_entity_definition(ename, new_name);
 		}
 		finally
 		{	
 			_store_locker.exitLockerThread();	
 		}
 	}
 
 	private void do_rename_entity_definition(String ename,String new_name) throws PersistenceException
 	{
 		
 		BDBPrimaryIndex pidx = entity_primary_indexes_as_map.get(ename);
 		if(pidx == null)
 			throw new PersistenceException("NO ENTITY DEF FOR "+ename);
 		BDBPrimaryIndex pidx2 = entity_primary_indexes_as_map.get(new_name);
 		if(pidx2 != null)
 			throw new PersistenceException("CANT RENAME TO  "+new_name+". ENTITY "+new_name+" ALREADY EXISTS");
 
 		
 		
 		EntityDefinition olddef = pidx.getEntityDefinition();
 		EntityDefinition newdef = olddef.clone();
 		newdef.setName(new_name);
 		try{
 			/* this will move the indexes too */
 			redefine_entity_definition(olddef,newdef);	
 		}catch(PersistenceException pe)
 		{
 			logger.error("do_rename_entity_definition(String, String)", pe);
 			throw new PersistenceException("UNABLE TO RENAME ENTITY DEF FOR "+ename+".INTERNAL ERROR. SEE LOGS");
 		}
 	
 	}
 		
 	private void redefine_entity_definition(EntityDefinition olddef,EntityDefinition redef) throws PersistenceException
 	{
 		logger.debug("redefine_entity_definition(EntityDefinition, EntityDefinition) - tREDEFINITING ENTITY DEF FOR " + olddef.getName() + " " + redef.getName());
 		
 
 		BDBPrimaryIndex pidx = null;
 		String old_ename = olddef.getName();
 		String new_ename = redef.getName();
 		
 		/*update entity def in db */
 		delete_entity_definition_from_db(old_ename);
 		/* TODO: this is due to the way we are looking up entity defs */
 		/* we look them up through the primary index map. if we */
 		/* dont remove the key it wont lets us add it because it
 		 * will think that it exists.*/
 		
 		pidx = entity_primary_indexes_as_map.remove(old_ename);
 		add_entity_definition_to_db(redef);
 		entity_primary_indexes_as_map.put(new_ename,pidx);
 
 		//this method is called by addfield and rename field and delete field//
 		//so this should be fine //
 		calculate_query_cache_dependencies(olddef);
 		remove_query_cache_dependencies(redef);
 		
 		if(!olddef.getName().equals(redef.getName()))
 		{		
 			pidx.renameDb(olddef, redef);
 			/*attach the indexes to the new entity name*/
 			move_entity_indexes(olddef, redef);
 			
 			/*update the other entity definitions reftypes to the new type*/
 			for(BDBPrimaryIndex p:entity_primary_indexes_as_list)
 			{
 				EntityDefinition d = p.getEntityDefinition();
 				if(d.getName().equals(old_ename))
 					continue;
 				for(FieldDefinition f:d.getFields())
 				{
 					if(f.getBaseType() == Types.TYPE_REFERENCE)
 					{
 						if(f.getReferenceType().equals(old_ename))
 						{
 							f.setReferenceType(new_ename);
 							redefine_entity_definition(d, d);//make sure the ref type is saved in the new def too//
 						}
 					}
 				}
 			}
 
 		}
 		
 		pidx.setEntityDefinition(redef);		
 	}
 	
 	/*this just deletes the entity definition in the entity def table. not exposed publicly.*/	
 	protected void delete_entity_definition_from_db(String ename) throws PersistenceException
 	{
 		if(entity_primary_indexes_as_map.get(ename) == null)
 		{
 			logger.error("delete_entity_definition_from_db(String) - DELETING ENTITY WHICH DOESNT EXIST!!!", null);
 			throw new PersistenceException("Entity Definition does not exist for: "+ename+".Delete failed.");
 		}
 		Transaction txn = null;		
 		OperationStatus op_status = null;
 		try
 		{			
 			txn = environment.beginTransaction(null, null);
 			DatabaseEntry key = new DatabaseEntry();
 			FieldBinding.valueToEntry(Types.TYPE_STRING, ename, key);
 
 			op_status = entity_def_db.delete(txn, key);
 			if(op_status != OperationStatus.SUCCESS)
 			{
 				abortTxn(txn);
 				throw new PersistenceException("Failed Deleting Entity Definition: "+ename);
 			}
 			txn.commit();
 		}
 		catch (Exception e)
 		{
 			abortTxn(txn);
 			logger.error("delete_entity_definition_from_db(String)", e);
 			throw new PersistenceException("Unable to delete definition " + ename + " "
 					+ e.getMessage());
 		}
 	}
 	
 	
 	
 	public void insertEntities(List<Entity> entities) throws PersistenceException
 	{
 		_store_locker.enterAppThread();
 		try
 		{
 			for(int i = 0;i < entities.size();i++)
 			{
 				Entity e = entities.get(i);
 				String entity_type 		  = e.getType();
 				BDBPrimaryIndex pi = entity_primary_indexes_as_map.get(entity_type);
 				if(pi == null)
 					throw new PersistenceException("ENTITY OF TYPE "+entity_type+" DOES NOT EXIST");
 				validate_entity(e);
 				if(e.getId() == Entity.UNDEFINED)
 					throw new PersistenceException("CANNOT USE insertEntity TO INSERT AN ENTITY WITH AN ID OF "+Entity.UNDEFINED);
 				do_insert_entity(null,pi,e,true,false);
 			}
 			do_checkpoint();
 		}
 		catch(PersistenceException pe)
 		{
 			logger.error(pe);
 			throw pe;
 		}
 		catch(DatabaseException dbe)
 		{
 			logger.error(dbe);
 			throw new PersistenceException("SAVE OF ENTITY FAILED. TRY AGAIN.\n");	
 		}
 		finally
 		{
 			_store_locker.exitAppThread();
 		}	
 	}
 	
 	public void insertEntity(Entity e) throws PersistenceException
 	{
 		_store_locker.enterAppThread();
 		try
 		{
 			String entity_type 		  = e.getType();
 			BDBPrimaryIndex pi = entity_primary_indexes_as_map.get(entity_type);
 			if(pi == null)
 				throw new PersistenceException("ENTITY OF TYPE "+entity_type+" DOES NOT EXIST");
 			validate_entity(e);
 			if(e.getId() == Entity.UNDEFINED)
 				throw new PersistenceException("CANNOT USE insertEntity TO INSERT AN ENTITY WITH AN ID OF "+Entity.UNDEFINED);
 			do_insert_entity(null,pi,e,true,true);
 		}
 		catch(PersistenceException pe)
 		{
 			logger.error(pe);
 			throw pe;
 		}
 		catch(DatabaseException dbe)
 		{
 			logger.error(dbe);
 			throw new PersistenceException("SAVE OF ENTITY FAILED. TRY AGAIN.\n"+e);	
 		}
 		finally
 		{
 			_store_locker.exitAppThread();
 		}	
 
 	}
 		
 	/*----------------- TRANSACTION INTERFACE-----------------------------------------------*/
 	public int startTransaction() throws PersistenceException
 	{
 		Transaction txn = null;
 		try{
 			
 			txn = environment.beginTransaction(null, null);
 			int tid = txn.getId();
 			//hashCode();
 			//System.out.println(Thread.currentThread().getName()+" ISSUING "+tid);
 			Transaction r = transaction_map.put(tid, txn);
 			if(r != null)
 			{
 				logger.debug("!!!!! DUDES!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
 				logger.debug("!!!!!! WE STARTED A TRANSACTION AND IT HAD THE SAME HASHCODE AS AN EXISTING TRANSACTION. THIS COULD BE DUE TO GARBAGE COLLECTION. HASHCODE COULD CHANGE AND SOMEHOW BE THE SAME AS AN EXISITNG TRANSACTION");
 				logger.debug("!!!!! DUDES!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
 			}
 			return tid;
 		}catch(DatabaseException e)
 		{
 			logger.error("GETTING TRANSACTION ID FAILED"+e);
 			throw new PersistenceException("startTransaction(): UNABLE TO GET NEW TRANSACTION ID",PersistenceException.UNABLE_TO_START_TRANSACTION);
 		}
 	}
 	
 	public int startTransaction(int parent_transaction_id) throws PersistenceException
 	{
 		Transaction txn = null;
 		try{
 			
 			Transaction parent = get_transaction_by_transaction_id(parent_transaction_id);
 			txn = environment.beginTransaction(parent, null);
 			int tid = txn.getId();
 			//hashCode();
 			System.out.println("ISSUING CHILD TXN: "+tid+" PARENT IS:"+parent_transaction_id);
 			Transaction r = transaction_map.put(tid, txn);
 			if(r != null)
 			{
 				logger.debug("!!!!! DUDES!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
 				logger.debug("!!!!!! WE STARTED A CHILD TRANSACTION AND IT HAD THE SAME HASHCODE AS AN EXISTING TRANSACTION. THIS COULD BE DUE TO GARBAGE COLLECTION. HASHCODE COULD CHANGE AND SOMEHOW BE THE SAME AS AN EXISITNG TRANSACTION");
 				logger.debug("!!!!! DUDES!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
 			}
 			return tid;
 		}catch(DatabaseException e)
 		{
 			logger.error("GETTING TRANSACTION ID FAILED"+e);
 			throw new PersistenceException("startTransaction(): UNABLE TO GET NEW TRANSACTION ID");
 		}
 	}
 	
 	public void commitTransaction(int transaction_id) throws PersistenceException
 	{
 
 		try{
 			Transaction txn = get_transaction_by_transaction_id(transaction_id);
 			txn.commitNoSync();
 			//System.out.println(Thread.currentThread().getName()+" COMMITED "+transaction_id);
 			clear_transaction_id(transaction_id);
 		}catch(DatabaseException e)
 		{
 			logger.error("GETTING TRANSACTION ID FAILED"+e);
 			throw new PersistenceException("startTransaction(): UNABLE TO GET NEW TRANSACTION ID");
 		}
 	}
 	
 	public void rollbackTransaction(int transaction_id) throws PersistenceException
 	{
 
 		try{
 			Transaction txn = get_transaction_by_transaction_id(transaction_id);
 			txn.abort();
 			System.out.println(Thread.currentThread().getName()+" ROLLED BACK "+transaction_id);
 			clear_transaction_id(transaction_id);
 		}catch(DatabaseException e)
 		{
 			logger.error("ROLLBACK OF TRANSACTION FAILED"+e);
 			throw new PersistenceException("startTransaction(): UNABLE TO GET NEW TRANSACTION ID");
 		}
 	}
 	
 	//TODO: probably should reap them after some period of time//
 	private ConcurrentHashMap<Integer,Transaction> transaction_map = new ConcurrentHashMap<Integer, Transaction>(128,0.75f,128);
 	private Transaction get_transaction_by_transaction_id(int transaction_id) throws PersistenceException
 	{
 		Transaction t =  transaction_map.get(transaction_id);
 		if(t == null)
 			throw new PersistenceException("UNABLE TO FIND ACTIVE TRANSACTION FOR TRANSACTION ID "+transaction_id);
 		return t;
 	}
 	
 	private void clear_transaction_id(int transaction_id) throws PersistenceException
 	{
 		transaction_map.remove(transaction_id);
 	}
 	
 	private void start_deadlock_detector()
 	{
 		Thread t = new Thread()
 		{	
 			public void run()
 			{
 				while(true)
 				{
 
 					//Iterator i = transaction_map.keySet().iterator();
 					//System.out.println("--TRANSACTION REPORT--");
 					//while(i.hasNext())
 					//{
 					//	System.out.println("\t--TRANSACTION REPORT-- ACTIVE TRANSACTION "+i.next());
 						
 					//}
 					//System.out.println("--TRANSACTION REPORT OVER--");
 					
 					System.out.println("--RUNNING DEADLOCK DETECTOR");
 					try{
 						environment.detectDeadlocks(LockDetectMode.DEFAULT);
 					}catch(DatabaseException de)
 					{
 						de.printStackTrace();
 					}
 					System.out.println("--DEADLOCK DETECTOR DONE");
 
 					try {
 						Thread.sleep(1000 * 60 * 3);//sleep three minutes
 					} catch (InterruptedException e) {
 						
 					}					
 				}
 			}
 		};
 		t.setDaemon(true);
 		t.start();
 		
 	}
 	
 	
 	/*------------------------------END TRANSACTION INTERFACE------------------------*/
 	public Entity saveEntity(Entity e) throws PersistenceException
 	{	
 		_store_locker.enterAppThread();
 		try
 		{
 			String entity_type 		  = e.getType();
 			BDBPrimaryIndex pi = entity_primary_indexes_as_map.get(entity_type);
 			if(pi == null)
 				throw new PersistenceException("ENTITY OF TYPE "+entity_type+" DOES NOT EXIST");
 
 			validate_entity(e);
 			do_save_entity(null,pi,e,true);
 		}
 		catch(PersistenceException pe)
 		{
 			logger.error(pe);
 			throw pe;
 		}
 		finally
 		{
 			_store_locker.exitAppThread();
 		}	
 		return e;
 	}
 
 	public Entity saveEntity(int transaction_id,Entity e) throws PersistenceException
 	{	
 		_store_locker.enterAppThread();
 		try
 		{
 			String entity_type 		  = e.getType();
 			BDBPrimaryIndex pi = entity_primary_indexes_as_map.get(entity_type);
 			if(pi == null)
 				throw new PersistenceException("ENTITY OF TYPE "+entity_type+" DOES NOT EXIST");
 
 			validate_entity(e);
 			Transaction txn = get_transaction_by_transaction_id(transaction_id);
 			do_save_entity(txn,pi,e,true);
 		}
 		catch(PersistenceException pe)
 		{
 			logger.error(pe);
 			throw pe;
 		}
 		finally
 		{
 			_store_locker.exitAppThread();
 		}	
 		return e;
 	}
 
 	
 	private void validate_entity(Entity e) throws PersistenceException
 	{
 		EntityDefinition def = do_get_entity_definition(e.getType());
 		List<FieldDefinition> fields = def.getFields();
 		int s = fields.size();
 		for (int i=0; i<s; i++)
 		{
 			FieldDefinition field = fields.get(i);
 			Object o = e.getAttribute(field.getName());
 			if (!field.isValidValue(o))
 			{
 				//this could either be more robust coercion or this little fix.
 				//this is dealing with the inabily of the admin cms to send a 0 as
 				//a double
 				if(o.getClass() == Integer.class && field.getType() == Types.TYPE_DOUBLE)
 				{
 					o = new Double((Integer)o);
 					e.setAttribute(field.getName(), o);
					continue;
 				}//also coerce doubles to ints
 				else if(o.getClass() == Double.class && field.getType() == Types.TYPE_INT)
 				{
 					o = new Integer((int)((Double)o).doubleValue());
 					e.setAttribute(field.getName(), o);
					continue;
 				}
 				throw new PersistenceException("Field "+field.getName()+" requires a value of type ["+FieldDefinition.typeAsString(field.getType())+"]. Not "+o.getClass());
 			}
 		}
 		//		
 	}
 
 	//NOT SURE ABOUT THIS YET SO IT IS UNFINISHED. TALK TO DAVID ABOUT VALUE COERCION.
 	private Object get_coerced_value(FieldDefinition f,Object value) throws Exception
 	{
 		int type = f.getType();
 		switch (type)
 		{
 			case Types.TYPE_UNDEFINED:
 				return "Undefined";
 			case Types.TYPE_BOOLEAN:
 				if (value.getClass() == Integer.class)
 				{
 					int val = (Integer)value;
 					if(val == 1)
 						return true;
 					return false;
 				}
 				if (value.getClass() == Long.class)
 				{
 					long val = (Long) value;
 					if(val == 1)
 						return true;
 					return false;
 				}
 				if (value.getClass() == String.class)
 				{
 					String val = (String) value;
 					if(val.equalsIgnoreCase("true"))
 						return true;
 					else if(val.equalsIgnoreCase("false"))
 						return false;
 				}
 			case Types.TYPE_LONG:
 				if (value.getClass() == Boolean.class)
 					return true;
 				if (value.getClass() == Integer.class)
 					return true;
 				if (value.getClass() == Long.class)
 					return true;
 				if (value.getClass() == Double.class)
 					return true;
 				if (value.getClass() == Float.class)
 					return true;
 				if (value.getClass() == String.class)
 					return true;
 				if (value.getClass() == Date.class)
 					return true;
 			case Types.TYPE_INT:
 				if (value.getClass() == Boolean.class)
 					return true;
 				if (value.getClass() == Integer.class)
 					return true;
 				if (value.getClass() == Long.class)
 					return true;
 				if (value.getClass() == Double.class)
 					return true;
 				if (value.getClass() == Float.class)
 					return true;
 				if (value.getClass() == String.class)
 					return true;
 				if (value.getClass() == Date.class)
 					return true;
 			case Types.TYPE_DOUBLE:
 				if (value.getClass() == Boolean.class)
 					return true;
 				if (value.getClass() == Integer.class)
 					return true;
 				if (value.getClass() == Long.class)
 					return true;
 				if (value.getClass() == Double.class)
 					return true;
 				if (value.getClass() == Float.class)
 					return true;
 				if (value.getClass() == String.class)
 					return true;
 				if (value.getClass() == Date.class)
 					return true;
 			case Types.TYPE_FLOAT:
 				if (value.getClass() == Boolean.class)
 					return true;
 				if (value.getClass() == Integer.class)
 					return true;
 				if (value.getClass() == Long.class)
 					return true;
 				if (value.getClass() == Double.class)
 					return true;
 				if (value.getClass() == Float.class)
 					return true;
 				if (value.getClass() == String.class)
 					return true;
 				if (value.getClass() == Date.class)
 					return true;
 			case Types.TYPE_STRING:
 				if (value.getClass() == Boolean.class)
 					return true;
 				if (value.getClass() == Integer.class)
 					return true;
 				if (value.getClass() == Long.class)
 					return true;
 				if (value.getClass() == Double.class)
 					return true;
 				if (value.getClass() == Float.class)
 					return true;
 				if (value.getClass() == String.class)
 					return true;
 				if (value.getClass() == Date.class)
 					return true;
 			case Types.TYPE_TEXT:
 				if (value.getClass() == Boolean.class)
 					return true;
 				if (value.getClass() == Integer.class)
 					return true;
 				if (value.getClass() == Long.class)
 					return true;
 				if (value.getClass() == Double.class)
 					return true;
 				if (value.getClass() == Float.class)
 					return true;
 				if (value.getClass() == String.class)
 					return true;
 				if (value.getClass() == Date.class)
 					return true;
 			case Types.TYPE_DATE:
 			case Types.TYPE_BLOB:
 			case Types.TYPE_REFERENCE:
 			default:
 				
 		}
 		throw new Exception("CANT COERCE");
 	}
 	
 	protected Entity do_save_entity(Transaction parent_txn,Entity e,boolean resolve_relations) throws PersistenceException
 	{
 		BDBPrimaryIndex pi = entity_primary_indexes_as_map.get(e.getType());
 		return do_save_entity(parent_txn, pi, e,resolve_relations);
 	}
 
 	
 	protected Entity do_save_entity(Transaction parent_txn,BDBPrimaryIndex pi,Entity e, boolean resolve_relations) throws PersistenceException
 	{
 
 		boolean update 		= true;
 		DatabaseEntry pkey 	= null;
 		int retry_count = 0;
 		if(e.getId() == Entity.UNDEFINED)
 			update = false;
 		Transaction txn = null;	
 		while(true)
 		{
 			try{
 				txn = environment.beginTransaction(parent_txn, null);
 				if (update)
 				{
 					/* resolve side effects first so we still have handle to old value */
 					if(resolve_relations)
 						resolve_relationship_sidefx(txn,e, UPDATE);
 
 					//NOTE: the update is canonical. it gets the definitive version
 					//of the object from the store and just updates the fields you
 					//specify. this avoids problems when relationships side effect
 					//an entity but the local version you have in the function 
 					//still has the old value and it has not been dirtied. this
 					//results in that old value getting inserted back into the primary
 					//table even though relationships may have side effected it.
 					//in essence this causes what ever the relationship did to
 					//your object is undone since the relationship has no way
 					//of updating your local copy of the object. it can just
 					//do it in the db.
 
 					Entity ee = pi.getById(txn, e.getId());
 					List<String> dirty_attributes = e.getDirtyAttributes();
 					int s = dirty_attributes.size();
 					for(int i = 0;i < s;i++)
 					{
 						String dirty_attribute = dirty_attributes.get(i);
 						ee.setAttribute(dirty_attribute, e.getAttribute(dirty_attribute));
 					}
 					pkey = pi.saveEntity(txn,ee);
 				} 
 				else 
 				{					
 					set_default_values(txn, e);
 					pkey = pi.saveEntity(txn,e);
 					if(resolve_relations)
 						resolve_relationship_sidefx(txn,e,INSERT);
 
 				}
 
 				save_to_secondary_indexes(txn, pkey, e, update);						
 				//save_to_deep_indexes(parent_txn, pkey, e, update);
 				e.undirty();
 				txn.commitNoSync();
 				checkpoint_policy.handleCheckpoint();
 				break;
 			}catch(DatabaseException dbe)
 			{
 				retry_count++;
 				//System.out.println(Thread.currentThread().getName()+" CAUGHT EXCEPTION FROM PRIMARY IDX RETRY COUNT IS "+retry_count);
 				abortTxn(txn);
 				//REMEMBER one_to_many entity clone properties
 				//if(retry_count >= BDBStore.MAX_DEADLOCK_RETRIES)
 				//{
 				//	logger.error(Thread.currentThread().getId()+" FAILING HORRIBLY HERE!!!!!!!!!");
 					throw new PersistenceException("SAVE OF ENTITY "+e+" FAILED",dbe);
 				//}
 			}catch(Exception ee)
 			{
 				abortTxn(txn);
 				ee.printStackTrace();
 				throw new PersistenceException("FAILED SAVING ENTITY DUE TO STRANGE EXCEPTION. SEE LOG.",ee);
 			}
 		}//end while
 
 
 //			do_checkpoint();
 	
 		//we might want a more elaborate policy here//
 		clean_query_cache(e.getType());
 	
 		return e;
 		
 	}
 	
 	
 	protected Entity do_insert_entity(Transaction parent_txn,BDBPrimaryIndex pi,Entity e,boolean blow_cache,boolean checkpoint) throws DatabaseException
 	{
 		DatabaseEntry pkey 	= new DatabaseEntry();
 		long eid = e.getId();	
 		LongBinding.longToEntry(eid, pkey);
 		Transaction txn = environment.beginTransaction(parent_txn, null);
 		pi.insertEntity(txn,pkey,e);
 		save_to_secondary_indexes(txn, pkey, e, false);
 		save_to_deep_indexes(parent_txn, pkey, e, false);
 		txn.commitNoSync();
 		e.undirty();
 		//we might want a more elaborate policy here//
 		if(blow_cache)
 			clean_query_cache(e.getType());
 		if(checkpoint)
 			do_checkpoint();
 		return e;	
 	}
 	
 	private void set_default_values(Transaction txn,Entity e) throws PersistenceException
 	{
 		List<String> dirty_fields = e.getDirtyAttributes();
 		List<FieldDefinition> all_fields = getEntityDefinitionProvider().provideEntityDefinition(e).getFields();
 		int s = all_fields.size();
 		for(int i = 0;i < s;i++)
 		{
 			FieldDefinition f = all_fields.get(i);
 			/* don't default the ones the user has intentionally set */
 			if(dirty_fields.contains(f.getName()))
 				continue;
 			set_default_value(txn, e, f);
 		}		
 	}
 	
 	private void set_default_value(Transaction txn,Entity e,FieldDefinition field) throws PersistenceException
 	{
 		Object val = field.getDefaultValue();
 		if(val == null)
 		{
 			e.setAttribute(field.getName(), null);
 			return;
 		}
 		
 		if(field.getBaseType() == Types.TYPE_REFERENCE)
 		{
 			if(field.isArray())
 				set_default_reference_array_value(txn,e,val,field);
 			else
 				set_default_reference_value(txn,e,val,field);
 
 			return;
 		}
 		
 		e.setAttribute(field.getName(), val);
 	}
 	
 	private void set_default_reference_value(Transaction txn,Entity e,Object ev,FieldDefinition field) throws PersistenceException
 	{
 		Entity val = (Entity)ev;
 		if(val.getId() == Entity.UNDEFINED)
 			e.setAttribute(field.getName(), do_save_entity(txn,val.cloneShallow(),true));	
 		else
 			e.setAttribute(field.getName(),val);	
 
 	}
 	
 	private void set_default_reference_array_value(Transaction txn,Entity e,Object elv,FieldDefinition field) throws PersistenceException
 	{
 		List<Entity> ee = ((List<Entity>)elv);
 		List<Entity> instance_of_default = new ArrayList<Entity>(ee.size());
 		for(int i = 0;i < ee.size();i++)
 		{
 			Entity eee = ee.get(i);
 			if(eee.getId() == Entity.UNDEFINED)
 			{
 				eee = do_save_entity(txn,eee.cloneShallow(),true);	
 				instance_of_default.add(eee);
 			}
 			else
 			{
 				instance_of_default.add(eee);
 			}
 		}
 		e.setAttribute(field.getName(),instance_of_default);	
 	}
 	
 	private void save_to_secondary_indexes(Transaction parent_txn,DatabaseEntry pkey,Entity e,boolean update) throws DatabaseException
 	{
 		List<BDBSecondaryIndex>sec_indexes = entity_secondary_indexes_as_list.get(e.getType());
 		List<String> dirty_fields;
 		if(update)
 			dirty_fields = e.getDirtyAttributes();
 		else
 		{
 			//get all the fields on insert//
 			dirty_fields = getEntityDefinitionProvider().provideEntityDefinition(e).getFieldNames();
 		}
 		
 		int ss 					  = dirty_fields.size();
 		int s 					  = sec_indexes.size();
 		BDBSecondaryIndex sidx = null;
 		for(int i=0;i < s;i++)
 		{					
 			sidx = sec_indexes.get(i);
 			//cant use this method to check if a deep index
 			//indexes a particular field//
 			//but it still needs to be in list for query manager testing existing etc...sigh
 			if(sidx.isDeepIndex())
 				continue;
 
 			//System.out.println("\nSEC INDEX NAME IS "+sidx.getName());
 			//System.out.println("\nSEC INDEX ATTRIBUTES IS "+sidx.getAttributes());
 			//System.out.println("DIRTY FIELDS ARE "+dirty_fields);
 			for(int ii = 0; ii < ss;ii++ )
 			{
 				if(sidx.indexesField(dirty_fields.get(ii)))
 				{
 					//System.out.println("DETECTING DIRTY FIELD "+dirty_fields.get(ii));
 					//System.out.println("E IS "+e);
 					save_to_secondary_index(parent_txn,pkey, sidx, e, update);
 					break;/*we break here because we only want to update an index
 							//	once if it is a multifield index*/
 				}
 			}
 		}
 	}
 	
 	private void save_to_secondary_index(Transaction parent_txn,DatabaseEntry pkey,BDBSecondaryIndex sidx,Entity e,boolean update) throws DatabaseException
 	{	
 		//need to update index
 		if(update)
 		{
 			//System.out.println(">>>>DELETEING "+LongBinding.entryToLong(pkey)+" FROM "+sidx.getName());
 			sidx.deleteIndexEntry(parent_txn,pkey);
 		}				
 		//System.out.println(">>>>INSERTING "+LongBinding.entryToLong(pkey)+" TO "+sidx.getName());
 		sidx.insertIndexEntry(parent_txn,e,pkey);						
 
 	}
 
 	///BEGIN DEEP INDEX CRAP//
 	private void save_to_deep_indexes(Transaction parent_txn,DatabaseEntry pkey,Entity e,boolean update) throws DatabaseException
 	{
 		String entity_type = e.getType();
 		Map<String,List<BDBSecondaryIndex>> deep_indexes_by_entity = deep_index_meta_map.get(entity_type);
 		if(deep_indexes_by_entity == null)
 			return;
 		
 		List<String> dirty_fields;
 		if(update)
 			dirty_fields = e.getDirtyAttributes();
 		else
 		{
 			//get all the fields on insert//
 			dirty_fields = getEntityDefinitionProvider().provideEntityDefinition(e).getFieldNames();
 		}
 
 		//need to get all possible indexes for dirty fields of initiaing entity
 		List<BDBSecondaryIndex> deep_indexes_by_field = new ArrayList<BDBSecondaryIndex>();
 		int s = dirty_fields.size();
 		for(int i = 0;i < s;i++)
 		{
 			String dirty_field 		= dirty_fields.get(i);
 			List<BDBSecondaryIndex> l_deep_indexes_by_field 	= deep_indexes_by_entity.get(dirty_field);
 			//System.out.println("DIRTY FIELD IS "+dirty_field);
 			//System.out.println("DEEP INDEXES BY FIELD IS "+deep_indexes_by_field);
 			if(l_deep_indexes_by_field != null)
 				deep_indexes_by_field.addAll(l_deep_indexes_by_field);
 		}
 	
 		s = deep_indexes_by_field.size();
 		Set<BDBSecondaryIndex> applied_idxs = new HashSet<BDBSecondaryIndex>();
 		for(int i = 0;i < s;i++)
 		{
 			BDBSecondaryIndex didx = deep_indexes_by_field.get(i);
 			if(applied_idxs.contains(didx))
 				continue;
 			//System.out.println("SAVING TO DEEP INDEX "+didx.getName()+" "+i);
 			save_to_deep_index(parent_txn,pkey,didx, e, update);
 			applied_idxs.add(didx);
 		}
 	}
 	
 	private void save_to_deep_index(Transaction parent_txn,DatabaseEntry pkey,BDBSecondaryIndex didx,Entity e,boolean update) throws DatabaseException
 	{	
 		String entity_type = e.getType();
 		String return_type 		   			   = didx.getPrimaryIndex().getEntityDefinition().getName();		
 		List<FieldDefinition > fields 	= didx.getFields();
 		List<Entity> modified_list 		= new ArrayList<Entity>();
 		Map<Integer,Entity> seen				= new HashMap<Integer,Entity>();
 
 
 		if(return_type.equals(e.getType()))
 		{
 			try{
 				expand_all_complex_fields(didx, e);
 				modified_list.add(e);
 			}catch(PersistenceException pe)
 			{
 				pe.printStackTrace();
 				throw new DatabaseException("FAILED EXPAND ALL COMPLEX FIELDS");
 			}
 		}
 		else
 		{
 			int s = fields.size();
 			for(int p = 0;p<s;p++)
 			{			
 				if(fields.get(p).getName().indexOf('.') == - 1)
 					continue;
 			
 				String[] ref_path 					   = (String[])didx.getAttribute(BDBSecondaryIndex.ATTRIBUTE_DEEP_INDEX_PATH_LOCATOR_PREFIX+p);
 				List<FieldDefinition>[] ref_path_types = (List<FieldDefinition>[])didx.getAttribute(BDBSecondaryIndex.ATTRIBUTE_DEEP_INDEX_PATH_TYPE_LOCATOR_PREFIX+p);		
 				int				initiating_path_index 	= -1;			
 			
 				//System.out.println("INITIATOR IS "+e);
 				//System.out.println("FIELD IS "+fields.get(p));
 				//System.out.println("REF PATH IS "+Arrays.asList(ref_path));
 
 				//find where i am //
 				//System.out.println("HI TOPH "+e+" IS "+e.getType()+" "+e.getId());
 				int ss = ref_path_types.length;
 				outer:for(int i = 0;i < ss;i++)
 				{
 					List<FieldDefinition> ref_path_type_members = ref_path_types[i];
 					for(int ii=0;ii < ref_path_type_members.size();ii++)
 					{
 						FieldDefinition ref_candidate = ref_path_type_members.get(ii);
 					//System.out.println("REF CANDIDATE IS "+ref_candidate+" "+ref_candidate.getReferenceType());
 						if(entity_type.equals(ref_path_type_members.get(ii).getReferenceType()))
 						{
 							initiating_path_index = i;
 							//System.out.println("INITIATOR IS "+e);
 							//EXPAND FROM INITIATOR DOWNWARD
 							try{
 								//System.out.println("SEEN IS "+seen);
 								expand_ref_path(e, ref_path, ref_path_types, initiating_path_index,seen);
 							}catch(PersistenceException ee)
 							{
 								ee.printStackTrace();
 								throw new DatabaseException("COULDNT EXPAND REF PATH IN DEEP INDEX "+didx.getName()+" FOR PATH "+Arrays.asList(ref_path)+" WITH INITIATOR "+e);
 							}
 
 							//System.out.println("ENTITY IS "+e.getType()+" "+e.getId());
 							expand_ref_path_upwards(e, ref_path, ref_path_types,initiating_path_index,return_type, modified_list,seen);
 							//System.out.println(">>>>INSERTING "+LongBinding.entryToLong(pkey)+" TO "+sidx.getName());
 							//System.out.println("MODIFIED LIST IS NOW. "+modified_list);
 
 							break outer;//break the loop labelled outer://
 						}
 					}
 				}			
 	
 			
 				if(initiating_path_index == -1)
 				{
 					if(p == fields.size()-1)
 						throw new DatabaseException("1: UNABLE TO RESOLVE INITIATING INSTANCE TYPE FOR DEEP INDEX. "+didx.getName()+" INITIATOR WAS "+e);
 					continue;
 				}
 			
 				//fill out the rest of the ref fields//
 				for(int pp = 0;pp < fields.size();pp++)
 				{
 					if(pp == p)
 						continue;
 					if(fields.get(pp).getName().indexOf('.') == -1)
 						continue;
 				
 					String[] l_ref_path 					   = (String[])didx.getAttribute(BDBSecondaryIndex.ATTRIBUTE_DEEP_INDEX_PATH_LOCATOR_PREFIX+pp);
 					List<FieldDefinition>[] l_ref_path_types = (List<FieldDefinition>[])didx.getAttribute(BDBSecondaryIndex.ATTRIBUTE_DEEP_INDEX_PATH_TYPE_LOCATOR_PREFIX+pp);		
 					//System.out.println("I AM HERE "+fields.get(pp));
 					//System.out.println("REF PATH "+Arrays.asList(l_ref_path));
 					//System.out.println("REF TYPES "+Arrays.asList(l_ref_path_types));
 					int S = modified_list.size();
 					for(int i = 0;i < S;i++)
 					{
 						Entity dawg = modified_list.get(i);
 						try{	
 							expand_ref_path(dawg, l_ref_path, l_ref_path_types, 0, new HashMap<Integer,Entity>());
 						}catch(PersistenceException ee)
 						{
 							ee.printStackTrace();
 							throw new DatabaseException("COULDNT EXPAND REF PATH IN DEEP INDEX "+didx.getName()+" FOR PATH "+Arrays.asList(l_ref_path)+" WITH INITIATOR "+e);
 						}
 					}
 				}
 			break;
 			}
 		}
 		
 		int sss = modified_list.size();
 		for(int i = 0;i < sss;i++)
 		{
 			Entity top_dog = modified_list.get(i);//top dog is an instance of what the index returns//
 			//System.out.println("TOP DAWG IS "+top_dog);
 			TupleOutput to = new TupleOutput();
 			to.writeLong(top_dog.getId());
 			pkey = new DatabaseEntry(to.toByteArray());
 
 			if(update)
 				didx.deleteIndexEntry(parent_txn,pkey);
 
 			didx.insertIndexEntry(parent_txn,top_dog,pkey);						
 		}
 
 	
 		clean_query_cache(return_type);
 	}
 
 
 	private List<Entity> get_modified_list_from_initiator(BDBSecondaryIndex didx,Entity e) throws DatabaseException
 	{
 		List<FieldDefinition> fields = didx.getFields();
 		List<Entity> modified_list = new ArrayList<Entity>();
 		int ss = fields.size();
 		for(int p=0;p < ss;p++)
 		{
 			if(fields.get(p).getName().indexOf('.')==-1)
 				continue;
 			String entity_type = e.getType();
 			String[] ref_path 					   = (String[])didx.getAttribute(BDBSecondaryIndex.ATTRIBUTE_DEEP_INDEX_PATH_LOCATOR_PREFIX+p);
 			List<FieldDefinition>[] ref_path_types = (List<FieldDefinition>[])didx.getAttribute(BDBSecondaryIndex.ATTRIBUTE_DEEP_INDEX_PATH_TYPE_LOCATOR_PREFIX+p);
 			String return_type 		   			   = didx.getEntityDefinition().getName();		
 		
 			int				initiating_path_index 	= -1;
 		
 			if(return_type.equals(e.getType()))
 				initiating_path_index = 0;
 			else
 			{
 				//find where i am //
 				//System.out.println("HI TOPH "+e+" IS "+e.getType()+" "+e.getId());
 				int s = ref_path_types.length;
 				outer:for(int i = 0;i < s-1;i++)
 				{
 					List<FieldDefinition> ref_path_type_members = ref_path_types[i];
 					for(int ii=0;ii < ref_path_type_members.size();ii++)
 					{
 						FieldDefinition ref_candidate = ref_path_type_members.get(ii);
 						//System.out.println("REF CANDIDATE IS "+ref_candidate+" "+ref_candidate.getReferenceType());
 						if(entity_type.equals(ref_candidate.getReferenceType()))
 						{
 							initiating_path_index = i;
 							break outer;//break the loop labelled outer://
 						}
 					}
 				}			
 			}
 			
 			if(initiating_path_index == -1)
 				if(p == fields.size()-1)
 					throw new DatabaseException("2: UNABLE TO RESOLVE INITIATING INSTANCE TYPE FOR DEEP INDEX. "+didx.getName()+" INITIATOR WAS "+e);
 				else
 					continue;
 			expand_ref_path_upwards(e, ref_path, ref_path_types,initiating_path_index,return_type, modified_list,new HashMap<Integer,Entity>());
 		}
 		return modified_list;
 	}
 
 	
 	private void expand_ref_path_upwards(Entity e, String[] ref_path, List<FieldDefinition>[] ref_path_types,int offset,String top_return_type, List<Entity> modified_list,Map<Integer,Entity> seen) throws DatabaseException 
 	{
 		
 		if(e.getType().equals(top_return_type))
 		{
 			if(!seen.containsKey(e.hashCode()))
 			{
 				seen.put(e.hashCode(),e);
 				modified_list.add(e);
 			}
 			return;
 		}
 		//System.out.println("offset is "+offset);
 		if(offset == 0)
 		{
 			String 			index_name = "deep_aux_by"+intercaps(ref_path[offset]);
 			//System.out.println("TOP EXPANDING UPWARD FOR:\n"+e.getType()+" "+e.getId());
 			//System.out.println("select "+top_return_type+" from "+index_name+" where eq "+e);
 			EntityDefinition d = getEntityDefinition(top_return_type);
 			FieldDefinition r_type = d.getField(ref_path[offset]);
 			Query q = new Query(top_return_type);
 			q.idx(index_name);
 			if(r_type.isArray())
 				q.setContainsAny(q.list(e));
 			else
 				q.eq(e);
 			
 			QueryResult r = null;
 			try {
 				r = _query_manager.executeQuery(null,q);
 			} catch (PersistenceException e1) 
 			{
 				e1.printStackTrace();
 				throw new DatabaseException("FAILED EXCUTING QUERY "+q+" FOR EXPAND UPWARDS IN DEEP INDEX RESOLUTION.");
 			}
 			List<Entity> parents = r.getEntities();
 			//System.out.println("TOP INDEX "+top_return_type+" "+index_name);
 			//System.out.println("\t1 found -> "+parents.size()+" "+parents);
 			for(int ii = 0;ii < parents.size();ii++)
 			{
 				Entity parent = parents.get(ii);
 				boolean has_seen = false;
 				if(seen.containsKey(parent.hashCode()))
 				{
 					has_seen = true;
 					parent = seen.get(parent.hashCode());
 				}	
 				//System.out.println("SETTING PARENT ATTRIBUTE OF "+parent.getType()+" "+parent.getId()+" "+ref_path[offset]+" TO  "+e.getType()+" "+e.getId());
 				if(ref_path_types[offset].get(0).isArray())
 				{
 					List<Entity> ll = (List<Entity>)parent.getAttribute(ref_path[offset]);
 					if(ll == null)
 						ll = new ArrayList<Entity>();
 					//List<Entity> ll = new ArrayList<Entity>();
 					ll.add(e);
 					parent.setAttribute(ref_path[offset], ll);
 				}
 				else
 					parent.setAttribute(ref_path[offset], e);
 				
 				if(!has_seen)
 				{
 					seen.put(parent.hashCode(),parent);
 					modified_list.add(parent);
 				}	
 			}
 			return;
 		}
 		
 		List<FieldDefinition> upwards_ref_types = ref_path_types[offset-1];
 		int s = upwards_ref_types.size();
 		for(int i = 0;i < s;i++)
 		{
 			FieldDefinition index_type =  upwards_ref_types.get(i);
 			String 			index_name = "deep_aux_by"+intercaps(ref_path[offset]);
 			Query q 				   = new Query(index_type.getReferenceType());
 			EntityDefinition d 		   = do_get_entity_definition(index_type.getReferenceType());
 			FieldDefinition right_type = d.getField(ref_path[offset]);
 			
 			q.idx(index_name);
 			if(right_type.isArray())
 				q.setContainsAny(q.list(e));
 			else
 				q.eq(e);
 			
 			//System.out.println("select "+index_type.getReferenceType()+" from "+index_name+" where eq "+e);
 			QueryResult r = null;
 			try{
 				r = _query_manager.executeQuery(null,q);
 			} catch (PersistenceException e1) 
 			{
 				e1.printStackTrace();
 				throw new DatabaseException("FAILED EXCUTING QUERY "+q+" FOR EXPAND UPWARDS IN DEEP INDEX RESOLUTION.");
 			}
 			List<Entity> parents = r.getEntities();
 			//System.out.println("EXPANDING UPWARD FOR:\n"+e.getType()+" "+e.getId());
 			//System.out.println("select "+index_type.getReferenceType()+" from "+index_name+" where eq "+e);
 			//System.out.println("\tfound -> "+parents.size()+" "+parents);
 			for(int ii = 0;ii < parents.size();ii++)
 			{
 				Entity parent = parents.get(ii);
 				boolean has_seen = false;
 				if(seen.containsKey(parent.hashCode()))
 				{//avoid circular reference//
 					has_seen = true;
 					parent = seen.get(parent.hashCode());
 				}
 				
 				//System.out.println("SETTING PARENT ATTRIBUTE "+ref_path[offset]+" TO  "+e.getType()+" "+e.getId());
 				if(ref_path_types[offset].get(0).isArray())
 				{
 					//TODO: I THINK THIS MUST BE BACK IN EFFECT//
 					List<Entity> ll = (List<Entity>)parent.getAttribute(ref_path[offset]);
 					if(ll == null)
 						ll = new ArrayList<Entity>();
 					ll.add(e);
 					parent.setAttribute(ref_path[offset], ll);
 				}
 				else
 					parent.setAttribute(ref_path[offset], e);
 				if(!has_seen)
 				{
 					seen.put(parent.hashCode(),parent);
 					expand_ref_path_upwards(parent, ref_path, ref_path_types, offset-1, top_return_type, modified_list,seen);
 				}
 			}
 
 		}	
 	}
 
 	/* BEGIN resolve relationship side effects */
 	/**
 	 * Takes an entity that has been updated and finds relationships for dirty fields.
 	 * If deleting or inserting, all fields are used in the search for relationships.
 	 * If any relationships are found, work is passed on to resolve_relationship_sidefekt.
 	 * @param parent_txn
 	 * @param e The entity that has just be operated on.
 	 * @param operation The entity operation (INSERT, UPDATE, DELETE)
 	 * @throws DatabaseException
 	 */
 	private void resolve_relationship_sidefx(Transaction parent_txn,Entity e,int operation) throws PersistenceException
 	{
 		// TODO: get references only
 		List<String> dirty_fields = (operation == DELETE )? getEntityDefinitionProvider().provideEntityDefinition(e).getFieldNames() : e.getDirtyAttributes();
 		int s = dirty_fields.size();
 		
 		String dirty_fieldname = null;
 		EntityRelationshipDefinition r = null;
 		String entity_type = e.getType();
 		Map<String,EntityRelationshipDefinition> ofmap = entity_relationship_map.get(entity_type);
 		if(ofmap == null)
 			return;
 		for(int i = 0; i < s;i++)
 		{
 			dirty_fieldname = dirty_fields.get(i);
 			//System.out.println("\tDIRTY FIELD "+i+" IS "+dirty_fieldname);
 			r = ofmap.get(dirty_fieldname);
 			
 			if(r == null)
 				continue;
 			resolve_relationship_sidefekt(parent_txn,e,dirty_fieldname,operation,r);
 		}
 	}
 	
 
 	/**
 	 * Delegates work by relationship type: 1 to 1, 1 to many, many to 1, many to many
 	 * @param ptxn
 	 * @param e The entity that has just be operated on.
 	 * @param dirty_field The name of a field in the entity that has already been modified (but not saved yet if the operation is UPDATE).
 	 * @param operation INSERT, UPDATE, or DELETE
 	 * @param relationship The dirty field is a part of this relationship.
 	 * @throws DatabaseException
 	 */
 	public void resolve_relationship_sidefekt(Transaction ptxn,Entity e,String dirty_field,int operation,EntityRelationshipDefinition relationship) throws PersistenceException
 	{
 		
 		if (e.getType().equals(relationship.getTargetEntity()) 
 			&& dirty_field.equals(relationship.getTargetEntityField())) 
 		{
 			//System.out.println("\tFLIPPING RELATIONSHIP");
 			relationship = relationship.flip();
 		}
 
 		String other_side_type 		   = relationship.getTargetEntity();
 		String other_side_fieldname = relationship.getTargetEntityField();
 				
 		switch(relationship.getType())
 		{
 			case EntityRelationshipDefinition.TYPE_ONE_TO_ONE:
 				resolve_one_to_one(ptxn, operation, e, dirty_field, other_side_type, other_side_fieldname);
 				break;
 			case EntityRelationshipDefinition.TYPE_ONE_TO_MANY:
 				resolve_one_to_many(ptxn, operation, e, dirty_field, other_side_type, other_side_fieldname);
 				break;
 			case EntityRelationshipDefinition.TYPE_MANY_TO_ONE:
 				resolve_many_to_one(ptxn, operation, e, dirty_field, other_side_type, other_side_fieldname);
 				break;
 			case EntityRelationshipDefinition.TYPE_MANY_TO_MANY:
 				resolve_many_to_many(ptxn, operation, e, dirty_field, other_side_type, other_side_fieldname);
 				break;
 			default:
 				throw new PersistenceException("UNIMPLEMENTED ");
 		}	
 	}
 	
 	
 	private void resolve_one_to_one(Transaction ptxn, int operation, Entity e, String dirty_field, String other_side_type, String other_side_fieldname) throws PersistenceException
 	{
 		validate_entity_for_relationship((Entity)e.getAttribute(dirty_field));
 		
 		BDBPrimaryIndex pidx = entity_primary_indexes_as_map.get(e.getType());
 		BDBPrimaryIndex rel_pidx = entity_primary_indexes_as_map.get(other_side_type);
 		
 		if (operation==UPDATE || operation==DELETE)
 		{
 			Entity old_rel = (Entity)pidx.getById(ptxn,e.getId()).getAttribute(dirty_field);
 			if(old_rel != null)
 			{
 				old_rel.setAttribute(other_side_fieldname, null);
 				do_save_entity(ptxn, rel_pidx,old_rel,false);
 
 			}
 		}
 		if (operation==INSERT || operation==UPDATE)
 		{
 			Entity new_rel   = (Entity)e.getAttribute(dirty_field);
 			if(new_rel != null)
 			{
 				//new_rel.getAttributes().put(relation_field_to_e, e);
 				new_rel.setAttribute(other_side_fieldname, null);
 				do_save_entity(ptxn,rel_pidx, new_rel,false);
 			}
 		}
 	}
 
 
 	@SuppressWarnings("unchecked")
 	private void resolve_one_to_many(Transaction ptxn, int operation, Entity e, String dirty_field, String other_side_field, String relation_field_to_e) throws PersistenceException
 	{
 		//System.out.println("ONE TO MANY E IS "+e.getType()+" "+e.getId()+"\nDIRTY FIELD IS "+dirty_field+" RELATION FIELD "+relation_field_to_e);
 		validate_entity_for_relationship((Entity)e.getAttribute(dirty_field));
 		
 		BDBPrimaryIndex my_pidx = entity_primary_indexes_as_map.get(e.getType());
 		BDBPrimaryIndex other_pidx = entity_primary_indexes_as_map.get(other_side_field);
 		
 		if (operation==UPDATE || operation==DELETE)
 		{
 			Entity old_child_record = /* fill e */ (Entity)my_pidx.getById(ptxn,e.getId());
 			Entity old_father = (Entity)old_child_record.getAttribute(dirty_field);
 			
 			//System.out.println(Thread.currentThread().getName()+" OLD CHILD RECORD IS "+old_child_record.getType()+" "+old_child_record.getId());
 		
 			if(old_father != null)
 			{
 				//System.out.println(Thread.currentThread().getName()+" OLD FATHER IS "+old_father.getType()+" "+old_father.getId());
 				/* remove e from the old father */
 				old_father = /* fill */ other_pidx.getById(ptxn, old_father.getId());	
 				List<Entity> old_fathers_children = (List<Entity>)old_father.getAttribute(relation_field_to_e);
 				if (old_fathers_children==null)
 					throw new PersistenceException("RESOLVE RELATIONSHIP INTEGRITY ERROR resolve_one_to_many - father of child must have children");
 				
 				//System.out.println("\t"+Thread.currentThread().getName()+" REMOVING "+old_child_record.getType()+" "+old_child_record.getId()+" FROM "+old_father.getType()+" "+old_father.getId()+" "+relation_field_to_e);
 				old_fathers_children.remove(old_child_record);
 				old_father.setAttribute(relation_field_to_e, old_fathers_children);//same list dirtying idea here..see below.without setting it wont pick up on indexes//
 				do_save_entity(ptxn,other_pidx, old_father,false);
 			}
 		}
 		if (operation==INSERT || operation==UPDATE)
 		{
 			Entity new_father  = (Entity)e.getAttribute(dirty_field);
 			if(new_father != null)
 			{
 				expand_entity(ptxn, other_pidx,new_father);
 				List<Entity> new_fathers_children = (List<Entity>)new_father.getAttribute(relation_field_to_e);
 				if(new_fathers_children == null)
 				{
 					new_fathers_children = new ArrayList<Entity>();
 				}
 				/* add e to the new relation */
 				if(new_fathers_children.contains(e))
 				{
 					try {
 						System.err.println("!!!!!!!!!!!!!!!ADDING A CHILD THAT ALREADY EXISTS!!!!!!!!!!!!!!!!!!!!");
 						System.err.println(Thread.currentThread().getName()+" CHILD "+e);
 						System.err.println(Thread.currentThread().getName()+" NEW FATHER "+new_father);
 						System.err.println(Thread.currentThread().getName()+" NEW FATHERS CHILDREN "+new_fathers_children);
 						throw new Exception();
 					} catch (Exception e1) {
 						
 						e1.printStackTrace();
 						System.exit(0);
 					}
 				}
 				new_fathers_children.add(e);
 				new_father.setAttribute(relation_field_to_e, new_fathers_children);//TODO: same list dirtying logic
 				do_save_entity(ptxn,other_pidx, new_father,false);
 
 			}
 		}
 	}
 	
 
 	@SuppressWarnings("unchecked")
 	private void resolve_many_to_one(Transaction ptxn, int operation, Entity e, String dirty_field, String other_side_type, String other_side_fieldname) throws PersistenceException
 	{
 		//System.out.println("M TO ONE E IS "+e.getType()+" "+e.getId()+"\nDIRTY FIELD IS "+dirty_field+" RELATION FIELD "+other_side_fieldname);
 		validate_entities_for_relationship((List<Entity>)e.getAttribute(dirty_field));
 		
 		BDBPrimaryIndex my_pidx = entity_primary_indexes_as_map.get(e.getType());
 		BDBPrimaryIndex other_pidx = entity_primary_indexes_as_map.get(other_side_type);
 
 		List<Entity> removed_children = new ArrayList<Entity>();
 		List<Entity> added_children   = new ArrayList<Entity>();
 		calc_added_and_removed(ptxn, e, dirty_field, my_pidx, other_pidx, operation, added_children, removed_children);
 		//System.out.println("ADDED AND REMOVED LIST FOR "+e.getType()+" "+e.getId());
 		//System.out.println("ADDED "+added_children);
 		//System.out.println("REMOVED "+removed_children);
 		if (operation==DELETE || operation==UPDATE)
 		{
 			//DEAL WITH DELETE LIST
 			int s = removed_children.size();
 			for(int i = 0;i < s;i++)
 			{
 				Entity c = removed_children.get(i);
 				//System.out.println("\tSETTING "+c.getType()+" "+c.getId()+" "+other_side_fieldname+" TO "+null);
 				c.setAttribute(other_side_fieldname, null);
 				do_save_entity(ptxn,other_pidx, c,false);				
 			}
 			
 		
 			s = added_children.size();
 			for(int j = 0; j < s;j++)
 			{
 				Entity c = added_children.get(j);
 				Entity old_father = (Entity)c.getAttribute(other_side_fieldname);
 				//System.out.println("OLD FATHER IS "+old_father);
 				if (old_father!=null) {
 					expand_entity(ptxn, my_pidx, old_father);
 					List<Entity> old_fathers_children = (List<Entity>) old_father.getAttribute(dirty_field);
 					//System.out.println("REMOVING"+c.getType()+" "+c.getId()+" FROM OLD FATHERS CHILDREN "+old_fathers_children);
 					boolean b = old_fathers_children.remove(c);
 					old_father.setAttribute(dirty_field, old_fathers_children);
 					//System.out.println("OLD FATHER IS NOW "+old_father);
 					//System.out.println("OLD FATHER DIRTY ATTS "+old_father.getDirtyAttributes());
 					do_save_entity(ptxn,my_pidx, old_father,false);
 				}
 			}
 			
 		
 		}
 	
 		if (operation==INSERT || operation==UPDATE)
 		{
 			//DEAL WITH ADD LIST
 			int s = added_children.size();
 			for(int i = 0; i < s;i++)
 			{
 				Entity c = added_children.get(i);
 				c.setAttribute(other_side_fieldname, e);
 				do_save_entity(ptxn,other_pidx, c,false);
 			}
 		}
 		
 	}
 
 	@SuppressWarnings("unchecked")
 	private void resolve_many_to_many(Transaction ptxn, int operation, Entity e, String dirty_field, String other_side_type, String other_side_fieldname) throws PersistenceException
 	{
 		validate_entities_for_relationship((List<Entity>)e.getAttribute(dirty_field));
 		
 		BDBPrimaryIndex my_pidx = entity_primary_indexes_as_map.get(e.getType());
 		BDBPrimaryIndex other_pidx = entity_primary_indexes_as_map.get(other_side_type);
 		
 		List<Entity> removed_children = new ArrayList<Entity>();
 		List<Entity> added_children   = new ArrayList<Entity>();
 		calc_added_and_removed(ptxn, e, dirty_field, my_pidx, other_pidx, operation, added_children, removed_children);
 		
 		if (operation == DELETE || operation==UPDATE )
 		{
 			int s = removed_children.size();
 			for(int j = 0; j < s;j++)
 			{
 				Entity c = removed_children.get(j);
 				List<Entity> old_fathers = (List<Entity>)c.getAttribute(other_side_fieldname);
 				old_fathers.remove(e);
 				c.setAttribute(other_side_fieldname ,old_fathers);//TODO: e.setListElement(),e.addListElement(),e.removeListElement(),need to set it so it is marked dirty//
 				do_save_entity(ptxn,other_pidx, c,false);	
 			}
 		}
 
 		if (operation==INSERT || operation==UPDATE)
 		{
 			int s = added_children.size();
 			for(int i = 0; i < s;i++)
 			{
 				Entity t = added_children.get(i);
 				List<Entity> tc = (List<Entity>)t.getAttribute(other_side_fieldname);
 				if (tc == null)
 				{
 					tc = new ArrayList<Entity>();
 				
 				}
 				tc.add(e);
 				t.setAttribute(other_side_fieldname, tc);//TODO: e.setListElement(),e.addListElement(),e.removeListElement(),need to set it so it is marked dirty//
 				do_save_entity(ptxn,other_pidx, t,false);			
 			}
 		}
 		
 	}
 	
 
 
 	/*
 	 *  utilities for relationship resolution
 	 */
 	private void validate_entity_for_relationship(Entity e) throws PersistenceException
 	{
 		if(e != null)
 		{
 			if(e.getId() == Entity.UNDEFINED)
 				throw new PersistenceException("SAVE DEEP IS NOT SUPPORTED. SAVE CHILD REFERENCES BEFORE SAVING THEM IN A RELATIONSHIP CONTEXT");		
 			if(e.isDirty())
 				throw new PersistenceException("CANT SAVE DIRTY MEMBER REFERENCE: "+e);
 		}
 	}
 	
 	private void validate_entities_for_relationship(List<Entity> new_children) throws PersistenceException
 	{
 		if(new_children != null) 
 		{	
 			int s = new_children.size();
 			for(int i = 0; i < s;i++)
 			{
 				validate_entity_for_relationship( new_children.get(i) );
 			}
 		}		
 	}
 	
 	@SuppressWarnings("unchecked")
 	private void calc_added_and_removed(Transaction ptxn, Entity e, String dirty_field, BDBPrimaryIndex father_pidx, BDBPrimaryIndex child_pidx, int op, List<Entity> added_children, List<Entity> removed_children) throws PersistenceException
 	{
 		//System.out.println("ORIG IS "+e);
 		// e is originating
 		Entity old_orig = (Entity)father_pidx.getById(ptxn, e.getId());
 		//System.out.println("....OLD ORIG IS "+old_orig);
 		
 		List<Entity> old_children = (List<Entity>)old_orig.getAttribute(dirty_field);
 		List<Entity> new_children = (List<Entity>)e.getAttribute(dirty_field);//must already filled
 		
 		//System.out.println("........OLD CHILDREN IS "+old_children);
 		//System.out.println("........NEW CHILDREN IS "+new_children);
 		if(op == INSERT)
 		{
 			if(new_children != null)
 				added_children.addAll(new_children);
 		}
 		else if (op==DELETE)
 		{
 			if (old_children!=null)
 				removed_children.addAll(old_children);
 		}
 		else if (op==UPDATE)
 		{
 			Map<Long,Entity> removed_children_map = new HashMap<Long, Entity>();
 			// compare the old & new lists to determine what has been added & removed
 			get_map_from_list(old_children,removed_children_map);
 			if (new_children!=null)
 			{
 				for (int i=0; i<new_children.size(); i++)
 				{
 					Entity c = new_children.get(i);
 					if (removed_children_map.containsKey(c.getId()))
 						removed_children_map.remove(c.getId());
 					else 
 						added_children.add(c);
 				}
 
 			}
 			Iterator<Entity> i = removed_children_map.values().iterator();
 			while(i.hasNext())
 				removed_children.add(i.next());
 		}
 
 		// fill the attributes of relevant info
 		//TODO: why do we do this???//
 		//System.out.println("....ADDED CHILDREN IS "+added_children);
 		for (int i=0; i<added_children.size(); i++)
 		{
 			expand_entity(ptxn, child_pidx, added_children.get(i));
 		}
 		
 		for (int i=0; i < removed_children.size(); i++)
 		{
 			expand_entity(ptxn, child_pidx, removed_children.get(i));
 		}
 
 	}
 
 	private void expand_entity(Transaction ptxn, BDBPrimaryIndex pidx,Entity entity) throws PersistenceException
 	{
 		if (!entity.isLightReference()) // THERE IS ALWAYS 1 attribute ("id") in lightly filled references
 			return;
 		if (entity.getId()==Entity.UNDEFINED)
 			throw new PersistenceException("INTEGRITY PROBLEM - all remove & added children is relationships have to be weakly filled at least");
 		Entity e = pidx.getById(ptxn, entity.getId());
 		entity.copyAttributes(e);
 	}
 
 	private Map<Long, Entity> get_map_from_list(List<Entity> entities, Map<Long, Entity> x)
 	{
 		if (entities==null || x==null)
 			return null;
 		int s = entities.size();
 		for (int i=0; i<s; i++)
 		{
 			Entity e = entities.get(i);
 			x.put(e.getId(),e);
 		}
 		return x;
 	}
 	
 
 	
 	/*
 	 * END resolve relationship side effects
 	 */
 
 	
 
 	public void deleteEntity(Entity e) throws PersistenceException
 	{	
 		_store_locker.enterAppThread();
 
 		try{
 			do_delete_entity(null, e);
 		}catch(PersistenceException pe)
 		{
 			logger.error("deleteEntity(Entity)", pe);
 			throw new PersistenceException("DELETE FAILED FOR ENTITY "+e+"\n",pe);
 		}
 		finally
 		{
 			_store_locker.exitAppThread();
 		}	
 
 	}
 	public void deleteEntity(int transaction_id,Entity e) throws PersistenceException
 	{	
 		_store_locker.enterAppThread();
 
 		try{
 			Transaction txn = get_transaction_by_transaction_id(transaction_id);
 			do_delete_entity(txn, e);
 		}catch(PersistenceException pe)
 		{
 			logger.error("deleteEntity(Entity)", pe);
 			throw new PersistenceException("DELETE FAILED FOR ENTITY IN TXN DELETE"+e+"\n",pe);
 		}
 		finally
 		{
 			_store_locker.exitAppThread();
 		}	
 
 	}
 	
 	public Entity do_delete_entity(Transaction parent_txn,Entity e) throws PersistenceException
 	{
 		Transaction txn = null;
 		DatabaseEntry pkey;
 		try{
 			String entity_type 		  = e.getType();
 			BDBPrimaryIndex pi = entity_primary_indexes_as_map.get(entity_type);
 			if(pi == null)
 				throw new PersistenceException("ENTITY OF TYPE "+entity_type+" DOES NOT EXIST");
 			
 			txn = environment.beginTransaction(parent_txn, null);	
 			resolve_relationship_sidefx(txn,e, DELETE);
 			pkey = pi.deleteEntity(txn,e);
 			if(pkey == null)
 			{
 				txn.commitNoSync();
 				throw new PersistenceException("ENTITY "+e.getType()+" "+e.getId()+" DOES NOT EXIST." +
 												"YOU CANNOT DELETE THAT WHICH DOES NOT EXIST.",PersistenceException.ENTITY_DOES_NOT_EXIST);
 			}
 			delete_from_secondary_indexes(txn, pkey, e);
 			//delete_from_deep_indexes(txn, pkey, e);
 			txn.commitNoSync();
 			/* have a more robust cache expiration policy at some point...probably
 			 * just blow away complex cache */
 			clean_query_cache(entity_type);
 			checkpoint_policy.handleCheckpoint();
 			return e;
 		}catch(DatabaseException dbe)
 		{
 			abortTxn(txn);
 			throw new PersistenceException("DELETE OF "+e+" FAILED: ",dbe);
 		}
 	}
 	
 	private void delete_from_secondary_indexes(Transaction parent_txn,DatabaseEntry pkey,Entity e) throws DatabaseException
 	{
 		List<BDBSecondaryIndex>sec_indexes = entity_secondary_indexes_as_list.get(e.getType());
 		int s 					  = sec_indexes.size();
 		BDBSecondaryIndex sidx = null;
 		for(int i=0;i < s;i++)
 		{	
 			sidx = sec_indexes.get(i);
 			delete_from_secondary_index(parent_txn,pkey, sidx, e);
 		}		
 	}
 	
 	private void delete_from_secondary_index(Transaction parent_txn,DatabaseEntry pkey,BDBSecondaryIndex sidx,Entity e) throws DatabaseException
 	{	
 			//System.out.println(">>>>DELETEING "+seqnum+" FROM "+sidx.getName());
 		sidx.deleteIndexEntry(parent_txn,pkey);
 	}
 	
 	private void delete_from_deep_indexes(Transaction parent_txn,DatabaseEntry pkey,Entity e) throws DatabaseException
 	{
 		String entity_type = e.getType();
 		Map<String,List<BDBSecondaryIndex>> deep_indexes_by_entity = deep_index_meta_map.get(entity_type);
 		if(deep_indexes_by_entity == null)
 			return;
 		
 		List<String> all_possible_index_fields;
 		//get all the fields on delete and look for indexes for all of them//
 		all_possible_index_fields = do_get_entity_definition(entity_type).getFieldNames();
 		List<BDBSecondaryIndex> deep_indexes_by_field = new ArrayList<BDBSecondaryIndex>();
 		int s = all_possible_index_fields.size();
 		for(int i = 0;i < s;i++)
 		{
 			String index_field 		= all_possible_index_fields.get(i);
 			List<BDBSecondaryIndex> l_deep_indexes_by_field 	= deep_indexes_by_entity.get(index_field);
 			if(l_deep_indexes_by_field != null)
 				deep_indexes_by_field.addAll(l_deep_indexes_by_field);
 		}
 		s = deep_indexes_by_field.size();
 		Set<BDBSecondaryIndex> applied_idxs = new HashSet<BDBSecondaryIndex>();
 		for(int i = 0;i < s;i++)
 		{
 			BDBSecondaryIndex didx = deep_indexes_by_field.get(i);
 			if(applied_idxs.contains(didx))
 				continue;
 			delete_from_deep_index(parent_txn,pkey,deep_indexes_by_field.get(i), e);
 			applied_idxs.add(didx);
 		}
 	}
 	
 	private void delete_from_deep_index(Transaction parent_txn,DatabaseEntry pkey,BDBSecondaryIndex didx,Entity e) throws DatabaseException
 	{	
 		String return_type = didx.getPrimaryIndex().getEntityDefinition().getName();		
 		if(return_type.equals(e.getType()))
 		{
 			didx.deleteIndexEntry(parent_txn, pkey);
 		}
 		else
 		{
 			List<Entity> modified_list = get_modified_list_from_initiator(didx, e);
 			//System.out.println("!!!MODIFIED LIST IS "+modified_list);
 			int s = modified_list.size();
 			for(int i = 0;i < s;i++)
 			{
 				Entity top_dog = modified_list.get(i);//top dog is an instance of what the index returns//
 				//System.out.println("TOP DAWG IS "+top_dog);
 				TupleOutput to = new TupleOutput();
 				to.writeLong(top_dog.getId());
 				pkey = new DatabaseEntry(to.toByteArray());
 				didx.deleteIndexEntry(parent_txn,pkey);
 			}
 		}
 		clean_query_cache(return_type);
 	}
 	
 	/* implements BDBEntityDefProvider... */
 	public EntityDefinition provideEntityDefinition(Entity e)
 	{
 		return do_get_entity_definition(e.getType());
 	}
 	
 	/* implements BDBEntityDefProvider... */
 	public EntityDefinition provideEntityDefinition(String entity_type)
 	{
 		return do_get_entity_definition(entity_type);
 	}
 	
 	/* implements BDBEntityDefProvider... */
 	public List<EntityDefinition> provideEntityDefinitions()
 	{
 		return do_get_entity_definitions();
 	}
 	
 	
 	public BDBEntityDefinitionProvider getEntityDefinitionProvider()
 	{
 		return entity_definition_provider;
 	}
 	
 	public EntityDefinition getEntityDefinition(String entity_type)
 	{
 		_store_locker.enterAppThread();	
 		EntityDefinition def;
 		def = do_get_entity_definition(entity_type);
 		_store_locker.exitAppThread();	
 		
 		if(def == null)
 			return null;
 		else
 			return def;	
 	}
 
 	public EntityDefinition do_get_entity_definition(String entity_name)
 	{
 		BDBPrimaryIndex pidx;
 		if((pidx = entity_primary_indexes_as_map.get(entity_name)) != null)
 			return pidx.getEntityDefinition();	
 		else
 			return null;
 	}
 
 	public List<EntityDefinition> getEntityDefinitions()throws PersistenceException
 	{
 		_store_locker.enterAppThread();	
 		try {
 			return do_get_entity_definitions();
 		}catch(Exception e)
 		{
 			throw new PersistenceException("FAILED GETTING ENTITY DEFINITIONS FROM MAP.");
 		}
 		finally
 		{
 			_store_locker.exitAppThread();
 		}
 	}
 	
 	private List<EntityDefinition> do_get_entity_definitions()
 	{
 		List<EntityDefinition> ret = new ArrayList<EntityDefinition>();
 		int s = entity_primary_indexes_as_list.size();
 		for(int i = 0; i < s;i++)
 			ret.add(entity_primary_indexes_as_list.get(i).getEntityDefinition());
 
 		return ret;
 	}
 
 	/* get the entity defs from the db and bootstrap the entity_def cache */
 	protected List<EntityDefinition> get_entity_definitions_from_db() throws PersistenceException
 	{
 		List<EntityDefinition> defs = new ArrayList<EntityDefinition>();
 		Cursor cursor = null;
 		try
 		{
 			cursor = entity_def_db.openCursor(null, null);
 			DatabaseEntry key = new DatabaseEntry();
 			DatabaseEntry data = new DatabaseEntry();
 			while (cursor.getNext(key, data, LockMode.DEFAULT) == OperationStatus.SUCCESS)
 			{
 				EntityDefinition e = (EntityDefinition) entity_def_binding.entryToObject(data);
 				defs.add(e);
 			}
 			close_cursors(cursor);
 			return defs;
 		}
 		catch (Exception de)
 		{
 			logger.error("get_entity_definitions_from_db()", de);
 			close_cursors(cursor);
 			throw new PersistenceException("ERROR READING ENTITY DEFINITIONS FROM DATABASE.");
 		}
 	}
 	
 	/* get the entity defs from the db and bootstrap the entity_def cache */
 	protected List<EntityIndex> get_entity_indices_from_db(BDBPrimaryIndex pidx) throws PersistenceException
 	{
 		List<EntityIndex> indices = new ArrayList<EntityIndex>();
 		Cursor cursor = null;
 		try
 		{
 			
 			cursor = entity_index_db.openCursor(null, null);
 			DatabaseEntry key  = new DatabaseEntry();
 			DatabaseEntry data = new DatabaseEntry();
 
 			FieldBinding.valueToEntry(Types.TYPE_STRING, pidx.getEntityDefinition().getName(), key);				
 			OperationStatus op_stat = cursor.getSearchKey(key, data, LockMode.DEFAULT);
 			while (op_stat == OperationStatus.SUCCESS)
 			{
 				EntityIndex e = (EntityIndex) entity_index_binding.entryToObject(data);
 				indices.add(e);
 				op_stat = cursor.getNextDup(key, data, LockMode.DEFAULT);
 			}
 			close_cursors(cursor);
 			return indices;
 		}
 		catch (Exception de)
 		{
 			logger.error("get_entity_indices_from_db(String)", de);
 			close_cursors(cursor);
 			throw new PersistenceException("ERROR READING ENTITY INDICES FROM DATABASE.");
 		}
 	}
 
 	/* get the entity defs from the db and bootstrap the entity_def cache */
 	protected List<EntityRelationshipDefinition> get_entity_relationships_from_db() throws PersistenceException
 	{
 		List<EntityRelationshipDefinition> rels = new ArrayList<EntityRelationshipDefinition>();
 		Cursor cursor = null;
 		try
 		{
 			cursor = entity_relationship_db.openCursor(null, null);
 			DatabaseEntry key = new DatabaseEntry();
 			DatabaseEntry data = new DatabaseEntry();
 			while (cursor.getNext(key, data, LockMode.DEFAULT) == OperationStatus.SUCCESS)
 			{
 				EntityRelationshipDefinition r = (EntityRelationshipDefinition) entity_relationship_binding.entryToObject(data);
 				rels.add(r);
 			}
 			close_cursors(cursor);
 			return rels;
 		}
 		catch (Exception de)
 		{
 			logger.error("get_entity_relationships_from_db()", de);
 			close_cursors(cursor);
 			throw new PersistenceException("ERROR READING ENTITY DEFINITIONS FROM DATABASE.");
 		}		
 	}
 
 	//!!! NOT PART OF PERSISTENCE INTERFACE ANYMORE
 	public List<EntityIndexDefinition> getEntityIndexDefinitions() throws PersistenceException
 	{
 		return _entity_index_definition_manager.getDefinitions();
 	}
 	
 	//!!! NOT PART OF PERSISTENCE INTERFACE ANYMORE	
 	public EntityIndexDefinition getEntityIndexDefinition(String name)
 	{
 		return _entity_index_definition_manager.getDefinition(name);
 	}
 	
 	public List<EntityIndex> getEntityIndices(String entity) throws PersistenceException
 	{
 		_store_locker.enterAppThread();	
 		
 		try{
 			BDBPrimaryIndex pi = entity_primary_indexes_as_map.get(entity);
 			if(pi == null)
 				throw new PersistenceException("ENTITY OF TYPE "+entity+" DOES NOT EXIST");
 		
 			List<EntityIndex> indices = new ArrayList<EntityIndex>();
 			List<BDBSecondaryIndex> bdb_idx = entity_secondary_indexes_as_list.get(entity);
 			for (int i=0; i<bdb_idx.size(); i++)
 				indices.add(bdb_idx.get(i).getEntityIndex());
 			return indices;
 		}catch(Exception e)
 		{
 			e.printStackTrace();
 			throw new PersistenceException("PROBLEM GETTING INDICES FOR "+entity);
 		}
 		finally
 		{
 			_store_locker.exitAppThread();
 		}
 	}
 	
 	public EntityIndex getEntityIndex(String entity,String index_name) throws PersistenceException
 	{
 		_store_locker.enterAppThread();	
 		
 		try{
 			return do_get_entity_index(entity, index_name);
 		}catch(Exception e)
 		{
 			e.printStackTrace();
 			throw new PersistenceException("PROBLEM GETTING INDICES FOR "+entity);
 		}
 		finally
 		{
 			_store_locker.exitAppThread();
 		}
 	}
 	
 	//TODO: this should be added to interface at some point//
 	public EntityIndex do_get_entity_index(String entity,String index_name) 
 	{
 		BDBPrimaryIndex pidx = entity_primary_indexes_as_map.get(entity);
 		if(pidx == null)
 			return null;
 		BDBSecondaryIndex idx;
 		idx = entity_secondary_indexes_as_map.get(entity).get(index_name);
 		if(idx == null)
 			return null;
 		return idx.getEntityIndex();
 	}
 	//entity_secondary_indexes_as_map.get(entity).get(index_name)
 
 	public void addEntityRelationship(EntityRelationshipDefinition r) throws PersistenceException
 	{
 		_store_locker.enterLockerThread();
 		try{
 			logger.debug("addEntityRelationship(EntityRelationshipDefinition) - ADD ENTITY RELATIONSHIP " + r);
 			do_add_entity_relationship(r);	
 		}catch(PersistenceException pe)
 		{
 			throw pe;
 		}
 		finally
 		{
 			logger.debug("addEntityRelationship(EntityRelationshipDefinition) - LOCKER THREAD IS EXITING");
 			_store_locker.exitLockerThread();	
 		}	
 	}
 	
 
 
 	public void do_add_entity_relationship(EntityRelationshipDefinition r) throws PersistenceException
 	{
 		String oe  = r.getOriginatingEntity();
 		String oef = r.getOriginatingEntityField();
 		String te  = r.getTargetEntity();
 		String tef = r.getTargetEntityField();
 		
 		EntityDefinition od = do_get_entity_definition(oe);
 		EntityDefinition td = do_get_entity_definition(te);
 		FieldDefinition of;
 		FieldDefinition tf;
 		if(entity_primary_indexes_as_map.get(oe) == null)
 			throw new PersistenceException("BAD ENTITY RELATIONSHIP. NO SUCH ORIGINATING ENTITY TYPE "+oe);
 		if(entity_primary_indexes_as_map.get(te) == null)	
 			throw new PersistenceException("BAD ENTITY RELATIONSHIP. NO SUCH TARGET ENTITY TYPE "+te);
 
 
 		if((of = od.getField(oef)) == null || of.getBaseType() != Types.TYPE_REFERENCE)
 			throw new PersistenceException("BAD ENTITY RELATIONSHIP. NO REFERENCE FIELD NAMED "+oef+" IN "+oe);
 		if((tf = td.getField(tef)) == null || tf.getBaseType() != Types.TYPE_REFERENCE)
 			throw new PersistenceException("BAD ENTITY RELATIONSHIP. NO REFERENCE FIELD NAMED "+tef+" IN "+te);
 
 		switch(r.getType())
 		{
 			case EntityRelationshipDefinition.TYPE_ONE_TO_ONE:
 				if(of.isArray())
 					throw new PersistenceException("BAD ENTITY RELATIONSHIP. ONE TO ONE RELATIONSHIP CANNOT EXIST" +
 													" WITH ORIGINATING FIELD "+oe+"."+oef+" OF TYPE ARRAY");
 				if(tf.isArray())
 					throw new PersistenceException("BAD ENTITY RELATIONSHIP. ONE TO ONE RELATIONSHIP CANNOT EXIST" +
 													" WITH TARGET FIELD "+te+"."+tef+" OF TYPE ARRAY");
 				break;
 			case EntityRelationshipDefinition.TYPE_MANY_TO_ONE:
 				if(!of.isArray())
 					throw new PersistenceException("BAD ENTITY RELATIONSHIP. MANY TO ONE RELATIONSHIP CANNOT EXIST" +
 													" WITH ORIGINATING FIELD "+oe+"."+oef+" NOT OF TYPE ARRAY");
 				if(tf.isArray())
 					throw new PersistenceException("BAD ENTITY RELATIONSHIP. MANY TO ONE RELATIONSHIP CANNOT EXIST" +
 													" WITH TARGET FIELD "+te+"."+tef+" OF TYPE ARRAY");
 				break;
 			case EntityRelationshipDefinition.TYPE_ONE_TO_MANY:
 				if(of.isArray())
 					throw new PersistenceException("BAD ENTITY RELATIONSHIP. ONE TO MANY RELATIONSHIP CANNOT EXIST" +
 													" WITH ORIGINATING FIELD "+oe+"."+oef+" OF TYPE ARRAY");
 				if(!tf.isArray())
 					throw new PersistenceException("BAD ENTITY RELATIONSHIP. ONE TO MANY RELATIONSHIP CANNOT EXIST" +
 													" WITH TARGET FIELD "+te+"."+tef+" NOT OF TYPE ARRAY");
 				break;
 			case EntityRelationshipDefinition.TYPE_MANY_TO_MANY:
 				if(!of.isArray())
 					throw new PersistenceException("BAD ENTITY RELATIONSHIP. MANY TO MANY RELATIONSHIP CANNOT EXIST" +
 													" WITH ORIGINATING FIELD "+oe+"."+oef+" NOT OF TYPE ARRAY");
 				if(!tf.isArray())
 					throw new PersistenceException("BAD ENTITY RELATIONSHIP. MANY TO ONE RELATIONSHIP CANNOT EXIST" +
 													" WITH TARGET FIELD "+te+"."+tef+" NOT OF TYPE ARRAY");
 				break;
 		}
 		
 		/* different views of entity relationship map */
 		Map<String,EntityRelationshipDefinition> ofmap; 
 		Map<String,EntityRelationshipDefinition> tfmap; 
 		
 		/* create them lazily */
 		ofmap = entity_relationship_map.get(oe);
 		if(ofmap == null)
 		{
 			ofmap = new HashMap<String,EntityRelationshipDefinition>();
 			entity_relationship_map.put(oe, ofmap);
 		}
 		if(ofmap.get(oef) != null)
 			throw new PersistenceException("ENTITY RELATIONSHIP ALREADY DEFINED FOR REF FIELD NAMED "+oef+" IN "+oe);
 
 		tfmap = entity_relationship_map.get(te);
 		if(tfmap == null)
 		{
 			tfmap = new HashMap<String,EntityRelationshipDefinition>();
 			entity_relationship_map.put(te, tfmap);
 		}
 		if(tfmap.get(tef) != null)
 			throw new PersistenceException("ENTITY RELATIONSHIP ALREADY DEFINED FOR REF FIELD NAMED "+tef+" IN "+te);
 		
 		try{
 			add_entity_relationship_to_db(r);
 			/*put in cache */
 			ofmap.put(of.getName(),r);
 			tfmap.put(tf.getName(),r);
 			logger.debug("do_add_entity_relationship(EntityRelationshipDefinition) - ADDED ENTITY RELATIONSHIP " + r);
 		}catch(DatabaseException de)
 		{
 			logger.error("do_add_entity_relationship(EntityRelationshipDefinition)", de);
 			throw new PersistenceException("UNABLE TO ADD ENTITY RELATIONSHIP.INTERNAL ERROR. SEE LOGS");
 		}
 
 	}
 
 	private  void add_entity_relationship_to_db(EntityRelationshipDefinition r)throws DatabaseException
 	{
 		StringBuffer buf = new StringBuffer();
 		buf.append(r.getOriginatingEntity());
 		buf.append(r.getOriginatingEntityField());
 		buf.append(r.getTargetEntity());
 		buf.append(r.getTargetEntityField());
 		
 		DatabaseEntry key = new DatabaseEntry();
 		DatabaseEntry data = new DatabaseEntry(new byte[256]);
 		FieldBinding.valueToEntry(Types.TYPE_STRING, buf.toString(), key);
 
 		entity_relationship_binding.objectToEntry(r,data);		
 		entity_relationship_db.put(null, key, data); 
 
 	}
 	
 	protected EntityRelationshipDefinition get_entity_relationship(String entity_name,String fieldname)
 	{
 		try{
 			return entity_relationship_map.get(entity_name).get(fieldname);
 		}catch(Exception e)
 		{
 			logger.error("get_entity_relationship(String, String)", e);
 			return null;
 		}
 	}
 
 	public List<EntityRelationshipDefinition> getEntityRelationships() throws PersistenceException
 	{
 		_store_locker.enterAppThread();	
 		try{
 			List<EntityRelationshipDefinition> rels = new ArrayList<EntityRelationshipDefinition>();
 			Iterator<String> ei = entity_relationship_map.keySet().iterator();
 			HashMap<EntityRelationshipDefinition,Object> already_added = new HashMap<EntityRelationshipDefinition,Object>();
 			
 			while(ei.hasNext())
 			{
 				String entity_name = ei.next();
 				Map<String,EntityRelationshipDefinition> frmap = entity_relationship_map.get(entity_name);
 				Iterator<String> fi = frmap.keySet().iterator();
 				while(fi.hasNext())
 				{
 					EntityRelationshipDefinition r = frmap.get(fi.next());
 					if(already_added.get(r) == null)
 					{
 						rels.add(r);
 						already_added.put(r,null);	
 					}
 					else
 						continue;
 				}
 			}
 			return rels;
 		}catch(Exception e)
 		{
 			throw new PersistenceException("FAILED GETTING ENTITY RELATIONSHIPS FROM MAP.");
 		}
 		finally
 		{
 			_store_locker.exitAppThread();
 		}
 	}
 	
 
 	protected List<EntityRelationshipDefinition> do_get_entity_relationships() throws DatabaseException
 	{
 		List<EntityRelationshipDefinition> rels = new ArrayList<EntityRelationshipDefinition>();
 		Cursor cursor = null;
 		try
 		{
 			cursor = entity_relationship_db.openCursor(null, null);
 			DatabaseEntry key = new DatabaseEntry();
 			DatabaseEntry data = new DatabaseEntry();
 			while (cursor.getNext(key, data, LockMode.DEFAULT) == OperationStatus.SUCCESS)
 			{
 				EntityRelationshipDefinition e = (EntityRelationshipDefinition) entity_relationship_binding.entryToObject(data);
 				rels.add(e);
 			}
 			close_cursors(cursor);
 		}
 		catch (Exception de)
 		{
 			close_cursors(cursor);
 			logger.error("getEntityDefinitions() - Error accessing database." + de, de);
 		}
 		return rels;
 	}
 
 	
 	/**
 	 * The safe way to checkpoint the database
 	 * @throws PersistenceException
 	 */
 	public void checkpoint() throws PersistenceException
 	{
 		_store_locker.enterLockerThread();
 		try
 		{
 			do_checkpoint();
 		}
 		catch (DatabaseException e)
 		{
 			e.printStackTrace();
 			throw new PersistenceException("Can't checkpoint database", e);
 		}
 		finally
 		{
 			_store_locker.exitLockerThread();
 		}
 	}
 	
 	/**
 	 * Forces a checkpoint without a lock on the store. This should be done with caution. 
 	 * 
 	 * @throws PersistenceException
 	 */
 	protected void do_checkpoint() throws DatabaseException
 	{
 			environment.checkpoint(null);
 			logger.debug("C H E C K P O I N T");
 	}
 	
 	/**
 	 * ALERT! this will allow all remaining transactions to complete. then it will refuse
 	 * all incoming requests. remember to unlock!
 	 */
 	public void lock()
 	{
 		_store_locker.enterLockerThread();
 	}
 
 	/**
 	 * thanks for unlocking the store.
 	 */
 	public void unlock()
 	{
 		_store_locker.exitLockerThread();
 	}
 	
 	
 	public void close() throws PersistenceException
 	{
 		_store_locker.enterLockerThread();	
 		try{
 			do_close();
 		}catch(PersistenceException pe)
 		{
 			throw pe;
 		}
 		finally
 		{
 			_store_locker.exitLockerThread();
 		}	
 	}
 	
 	private boolean _closed = false;
 	private synchronized void do_close() throws PersistenceException
 	{
 		
 		if(_closed)
 		{
 			System.out.println("STORE HAS ALREADY BEEN CLOSED");
 			return;
 		}
 		_closed = true;
 		System.out.println("ENTER CLOSE");
 		try{
 			System.out.println("ABOUT TO CHECKPOINT");
 			do_checkpoint();
 			System.out.println("CHECKPOINT COMPLETE");
 		}catch(DatabaseException dbe)
 		{
 			System.out.println("FAILED CHECKPOINTING THE DB ON SHUTDOWN ");
 			dbe.printStackTrace();
 		}
 			
 		checkpoint_policy.destroy();
 		try{
 			System.out.println("LOCK STATS "+environment.getLockStats(null));
 		}catch(Exception e)
 		{
 			e.printStackTrace();
 		}
 		try{
 			System.out.println("TRANSACTION STATS "+environment.getTransactionStats(null));
 		}catch(Exception e)
 		{
 			e.printStackTrace();
 		}
 
 		
 
 		long t = System.currentTimeMillis();
 		try
 		{			
 			for(int i = 0; i < entity_primary_indexes_as_list.size();i++)
 			{
 				BDBPrimaryIndex pidx = entity_primary_indexes_as_list.get(i);
 				String ename 		 = pidx.getEntityDefinition().getName();
 				pidx.close(entity_secondary_indexes_as_list.get(ename));
 			}
 			
 			if (entity_index_db != null)
 			{
 				entity_index_db.sync();		
 				entity_index_db.close();		
 			}
 			if (entity_def_db != null)
 			{
 				entity_def_db.sync();		
 				entity_def_db.close();		
 			}
 			if(entity_relationship_db != null)
 			{
 				entity_relationship_db.sync();
 				entity_relationship_db.close();
 			}
 			
 			_queue_manager.shutdown();
 
 			//System.out.println("LOCK STATS "+environment.getLockStats(null));
 			//System.out.println();
 			//System.out.println("CONFIG STATS "+environment.getLockStats(null));
 			if (environment != null)
 				environment.close();	
 
 		}
 		catch (Exception e)
 		{
 			System.err.println("do_close()");
 			e.printStackTrace();
 			throw new PersistenceException("FIALED TO CLOSE PERSISTENT STORE");
 		}
 		if(_deadlock_monitor_running)
 			stop_deadlock_monitor();		
 
 
 		
 
 		System.out.println("BDBStore - do_close() - CLOSED EVERYTHING IN " + (System.currentTimeMillis() - t) + " MS");		
 	}
 
 	/******************SUPPORT FUNCTIONS**************************************************/
 
 	private static SimpleDateFormat timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss");
 	public static String timestamp()
 	{
 		return timestamp.format(new Date());
 	}
 	
 	private void init_environment(Map<String, Object> config) throws PersistenceException
 	{
 		String path = (String)config.get(BDBStoreConfigKeyValues.KEY_STORE_ROOT_DIRECTORY);
 		if(path == null)
 			throw new PersistenceException("PLEASE SPECIFY BASE_DB_ENV IN CONFIG");		
 
 		File db_env_home = null;
 		File db_env_root = new File(path);
 		_db_env_props_file = new File(db_env_root, BDBConstants.ENVIRONMENT_PROPERTIES_FILE_NAME);
 		_db_env_props = new Properties();
 		try 
 		{
 			_db_env_props.load(new FileInputStream(_db_env_props_file));
             db_env_home = new File(db_env_root, _db_env_props.getProperty(BDBConstants.KEY_ACTIVE_ENVIRONMENT));
         } 
 		catch (Exception e) 
 		{
         	db_env_home = new File(db_env_root, timestamp());
         	db_env_home.mkdir();
         	set_active_env_prop(db_env_home.getName());
         }
         if (db_env_home == null || !db_env_home.exists())
         	throw new PersistenceException("INVALID BASE_DB_ENV. "+db_env_home+" DOES NOT EXIST");
 		if(!db_env_home.isDirectory())
 			throw new PersistenceException("INVALID BASE_DB_ENV. "+db_env_home+" NOT A DIRECTORY");
 		
 		logger.debug("init_environment(HashMap<Object,Object>) - INITIALIZING ENVIRONMENTntPATH=" + db_env_home + "n");		
 		try
 		{
 			EnvironmentConfig env_cfg = get_tds_default_config();
 			
 			Integer val = (Integer)config.get(BDBStoreConfigKeyValues.KEY_DEADLOCK_RESOLUTION_SCHEME);
 			if(val == null || val  == BDBStoreConfigKeyValues.VALUE_DEADLOCK_RESOLUTION_SCHEME_ALWAYS_CRAWL_LOCKTABLE) 
 				env_cfg.setLockDetectMode(LockDetectMode.OLDEST);
 	
 			environment = new Environment(db_env_home, env_cfg);
 			logger.info("BDB VERSION IS: "+ environment.getVersionString());
 			_closed 	= false;
 		}
 		catch (Exception e)
 		{
 			logger.error("init_environment(HashMap<Object,Object>)", e);
 			throw new PersistenceException("COULD NOT INITIALIZE BDB ENVIRONMENT AT "+db_env_home);
 		}		
 	}
 
 	//TODO: need to compare versions and rewrite file//
 	private void setup_version_file(Map<String,Object> config) throws PersistenceException
 	{
 		String path = (String)config.get(BDBStoreConfigKeyValues.KEY_STORE_ROOT_DIRECTORY);
 		File f = new File(path+File.separator+BDBConstants.STORE_VERSION_FILENAME);
 		if(!f.exists())
 		{
 			String active_env_path =  _db_env_props.getProperty(BDBConstants.KEY_ACTIVE_ENVIRONMENT);
 			File def_db = new File(path+File.separator+active_env_path+File.separator+BDBConstants.ENTITY_DEFINITION_DB_NAME);
 			if(def_db.exists())
 				throw new PersistenceException("PLEASE MIGRATE STORE FROM VERSION 0 TO VERSION "+store_major_version);
 
 			try{
 				FileOutputStream fos = new FileOutputStream(f);
 				fos.write(store_major_version);
 				fos.write(store_minor_version);
 				fos.close();
 			}catch(IOException e)
 			{
 				logger.error(e);
 				throw new PersistenceException("UNABLE TO SETUP VERSION FILE FOR DB DUE TO IO EXCEPTION.");
 			}
 			f.setReadOnly();
 		}
 	}
 		
 	private void verify_version(Map<String,Object> config) throws PersistenceException
 	{
 		String path = (String)config.get(BDBStoreConfigKeyValues.KEY_STORE_ROOT_DIRECTORY);
 		File f = new File(path+File.separator+BDBConstants.STORE_VERSION_FILENAME);
 		try{
 			FileInputStream fis = new FileInputStream(f);
 			int disk_major_version = fis.read();
 			int disk_minor_version = fis.read();
 			fis.close();
 			if(disk_major_version != store_major_version)
 				throw new PersistenceException("VERSION OF DATA ON DISK DOES NOT MATCH STORE VERSION. STORE VERSION IS "+store_major_version+"."+store_minor_version+" AND DATA IS IN FORMAT FOR VERSION "+disk_major_version+"."+disk_minor_version+". PLEASE RUN MIGRATION PROGRAM.");
 			logger.info("VERSION OF DATA ON DISK IS VERSION "+disk_major_version+"."+disk_minor_version);
 		}catch(IOException e)
 		{
 			logger.error(e);
 			throw new PersistenceException("UNABLE TO VERIFY VERSION FOR DB DUE TO IO EXCEPTION.");
 		}
 	}
 	
 	
 	private void init_backup_subsystem(Map<String, Object> config) throws PersistenceException
 	{
 		
 		String backup_path = (String)config.get(BDBStoreConfigKeyValues.KEY_STORE_BACKUP_DIRECTORY);
 		if(backup_path == null)
 			return;
 		backup_root_directory = new File(backup_path);
 		if(!backup_root_directory.exists())
 			throw new PersistenceException("BACKUP DIRECTORY "+backup_path+" DOES NOT EXIST");
 		if(!backup_root_directory.isDirectory())
 			throw new PersistenceException("BACKUP DIRECTORY "+backup_path+" IS NOT A DIRECTORY");
 	}
 	
 	private void init_entity_definition_db(Map<String,Object> config) throws PersistenceException
 	{
 
 		DatabaseConfig cfg = get_default_primary_db_config();
 		try{
 			entity_def_db = environment.openDatabase(null, BDBConstants.ENTITY_DEFINITION_DB_NAME, null, cfg);
 		}catch (Exception e){	
 
 			logger.error("init_entity_definition_db(HashMap<Object,Object>)", e);
 			throw new PersistenceException("UNABLE TO OPEN ENTITY DEFINITION DB "+BDBConstants.ENTITY_DEFINITION_DB_NAME);
 		}		
 
 		entity_def_binding = new EntityDefinitionBinding();
 		logger.debug("init_entity_definition_db(HashMap<Object,Object>) - OPENED ENTITY DEFINITION DATABASE ");
 		
 	}
 	
 	private void init_entity_definition_provider(Map<String,Object> config) throws PersistenceException
 	{
 
 		entity_definition_provider = this;
 		entity_binding.setEntityDefinitionProvider(getEntityDefinitionProvider());
 		
 	}
 
 	
 	private void init_entity_secondary_index_db(Map<String,Object> config) throws PersistenceException
 	{
 
 		DatabaseConfig cfg = get_entity_index_db_config();
 		try{
 			entity_index_db = environment.openDatabase(null, BDBConstants.ENTITY_INDEX_DB_NAME, null, cfg);
 		}catch (Exception e){	
 
 			logger.error("init_entity_secondary_index_db(HashMap<Object,Object>)", e);
 			throw new PersistenceException("UNABLE TO OPEN ENTITY INDEX DB "+BDBConstants.ENTITY_INDEX_DB_NAME);
 		}		
 
 		entity_index_binding = new EntitySecondaryIndexBinding();
 		logger.debug("init_entity_secondary_index_db(HashMap<Object,Object>) - OPENED ENTITY INDEX DATABASE ");
 		
 	}
 	
 	private void init_entity_relationship_db(Map<String,Object> config) throws PersistenceException
 	{
 		DatabaseConfig cfg = get_default_primary_db_config();
 		try{	
 			entity_relationship_db = environment.openDatabase(null, BDBConstants.ENTITY_RELATIONSHIP_DB_NAME, null, cfg);
 		}catch(Exception e){
 			logger.error("init_entity_relationship_db(HashMap<Object,Object>)", e);
 			throw new PersistenceException("UNABLE TO OPEN ENTITY RELATIONSHIPS DB "+BDBConstants.ENTITY_RELATIONSHIP_DB_NAME);
 		}
 		entity_relationship_binding = new EntityRelationshipBinding();	
 
 		logger.info("init_entity_relationship_db(HashMap<Object,Object>) - OPENED ENTITY RELATIONSHIP DATABASE ");
 	}
 	
 	
 	public DatabaseConfig getDefaultBTreeConfig()
 	{
 		return get_default_primary_db_config();
 	}
 	
 	private DatabaseConfig get_default_primary_db_config()
 	{
 		DatabaseConfig cfg = get_primary_db_config_btree();
 		return cfg;
 	}
 	
 	private DatabaseConfig get_entity_index_db_config()
 	{
 		DatabaseConfig cfg = new DatabaseConfig();
 		cfg.setErrorStream(System.err);
 		cfg.setErrorPrefix("DB FOR ENTITY INDEXES");
 		cfg.setType(DatabaseType.BTREE);
 		cfg.setAllowCreate(true);
 		cfg.setSortedDuplicates(true);
 		cfg.setTransactional(true);
 		return cfg;
 	}
 	
 	@SuppressWarnings("unused")
 	private DatabaseConfig get_primary_db_config_hash()
 	{
 		DatabaseConfig cfg = new DatabaseConfig();
 		cfg.setType(DatabaseType.HASH);
 		cfg.setAllowCreate(true);
 		cfg.setTransactional(true);
 		//cfg.setReadUncommitted(true);
 		return cfg;
 	}
 	
 	private DatabaseConfig get_primary_db_config_btree()
 	{
 		
 		DatabaseConfig cfg = new DatabaseConfig();
 		cfg.setType(DatabaseType.BTREE);
 		cfg.setAllowCreate(true);
 		cfg.setTransactional(true);
 		//cfg.setReadUncommitted(true);
 		return cfg;
 	}	
 	
 	private void bootstrap_existing_entity_definitions() throws PersistenceException
 	{
 		List<EntityDefinition> defs = get_entity_definitions_from_db();
 		BDBPrimaryIndex pidx;
 		for (int i = 0; i < defs.size(); i++)
 		{
 			EntityDefinition def = defs.get(i); 
 			pidx = new BDBPrimaryIndex(entity_binding);
 			pidx.setup(environment,def);
 			String entity_name = def.getName();
 			entity_primary_indexes_as_map.put(entity_name, pidx);
 			entity_primary_indexes_as_list.add(pidx);
 			calculate_query_cache_dependencies(def);
 		}
 	}
 	
 	private void bootstrap_existing_indices() throws PersistenceException
 	{
 		Set<String> entity_types = entity_primary_indexes_as_map.keySet();
 		for (String entity_name : entity_types){
 			List<BDBSecondaryIndex> 		sec_indexes_list = new ArrayList<BDBSecondaryIndex>();
 			Map<String, BDBSecondaryIndex> 	sec_indexes_map  = new HashMap<String, BDBSecondaryIndex>();
 
 			BDBPrimaryIndex pidx 		= entity_primary_indexes_as_map.get(entity_name);
 			List<EntityIndex> indicies 	= get_entity_indices_from_db(pidx);
 
 			EntityIndex eii;
 			for (int ii = 0; ii < indicies.size(); ii++)
 			{
 				eii = indicies.get(ii);
 				Class<?> c = null;
 				BDBSecondaryIndex index = null;
 				
 				int index_type = eii.getType();
 				String classname = null;
 				switch(index_type)
 				{
 					case EntityIndex.TYPE_SIMPLE_SINGLE_FIELD_INDEX:
 						classname = "com.pagesociety.bdb.index.SimpleSingleFieldIndex";
 						break;
 					case EntityIndex.TYPE_SIMPLE_MULTI_FIELD_INDEX:
 						classname = "com.pagesociety.bdb.index.SimpleMultiFieldIndex";
 						break;
 					case EntityIndex.TYPE_ARRAY_MEMBERSHIP_INDEX:
 						classname = "com.pagesociety.bdb.index.ArrayMembershipIndex";
 						break;
 					case EntityIndex.TYPE_MULTIFIELD_ARRAY_MEMBERSHIP_INDEX:
 						classname = "com.pagesociety.bdb.index.MultiFieldArrayMembershipIndex";
 						break;
 					case EntityIndex.TYPE_FREETEXT_INDEX:
 						classname = "com.pagesociety.bdb.index.SingleFieldFreeTextIndex";
 						break;	
 					case EntityIndex.TYPE_MULTI_FIELD_FREETEXT_INDEX:
 						classname = "com.pagesociety.bdb.index.MultiFieldFreeTextIndex";
 						break;						
 					default:
 						throw new PersistenceException("UNKNOWN INDEX TYPE 0x"+Integer.toHexString(index_type));
 				}
 				
 				try {
 					c = Class.forName(classname);
 					index = (BDBSecondaryIndex)c.newInstance();
 				} catch (Exception e) {
 					logger.error("bootstrap_existing_indices()", e);
 				}
 				System.out.println("\tSETTING UP "+eii.getEntity()+" "+eii.getName()+" FIELDS: "+eii.getFields());
 				index.setup(pidx, eii);
 				
 				/*BEGIN PART OF CRAZINESS*/
 				if(index.isDeepIndex())
 				{
 					update_deep_index_meta_info(index);
 					deep_index_list.add(index);
 				}
 				/*END PART OF CRAZINESS */
 				sec_indexes_list.add(index);
 				sec_indexes_map.put(eii.getName(),index);
 			}
 
 			entity_secondary_indexes_as_list.put(entity_name,sec_indexes_list);
 			entity_secondary_indexes_as_map.put(entity_name,sec_indexes_map);
 
 		}//end for each entity
 
 	}
 	
 	private void bootstrap_existing_entity_relationships() throws PersistenceException
 	{
 		List<EntityRelationshipDefinition> rels = get_entity_relationships_from_db();
 
 		for (int i = 0; i < rels.size(); i++)
 		{
 			EntityRelationshipDefinition r = rels.get(i);
 			String oe = r.getOriginatingEntity();
 			String of = r.getOriginatingEntityField();
 			String te = r.getTargetEntity();
 			String tf = r.getTargetEntityField();
 			/* put it in going both ways */
 			if(entity_relationship_map.get(oe) == null)
 				entity_relationship_map.put(oe, new HashMap<String,EntityRelationshipDefinition>());
 			if(entity_relationship_map.get(te) == null)
 				entity_relationship_map.put(te, new HashMap<String,EntityRelationshipDefinition>());
 			
 			entity_relationship_map.get(oe).put(of, r);
 			entity_relationship_map.get(te).put(tf, r);
 		}
 
 	}
 
 	private QueryManager 		_query_manager;
 	private QueryManagerConfig 	_query_manager_config;
 	private void init_query_manager()
 	{
 
 		_query_manager_config = new QueryManagerConfig();
 		_query_manager_config.setContext(this);
 		_query_manager_config.setPrimaryIndexMap(entity_primary_indexes_as_map);
 		_query_manager_config.setSecondaryIndexMap(entity_secondary_indexes_as_map);
 		_query_manager_config.setEntityCacheInitialSize(64);
 		_query_manager_config.setEntityCacheLoadFactor(0.75f);
 		_query_manager_config.setEntityCacheMaxSize(128);
 		_query_manager = new QueryManager(_query_manager_config);
 		_query_manager.init();
 
 	}
 
 	private PersistentQueueManager		_queue_manager;
 	private void init_queue_manager(Map<String,Object> config) throws PersistenceException
 	{
 
 		_queue_manager = new PersistentQueueManager();
 		_queue_manager.init(this, config);
 	}
 
 	private void init_field_binding() throws PersistenceException
 	{
 		FieldBinding.initWithPrimaryIndexMap(entity_primary_indexes_as_map);
 	}
 	
 	private void init_deadlock_resolution_scheme(Map<String,Object> config) throws PersistenceException
 	{
 		Integer val = (Integer)config.get(BDBStoreConfigKeyValues.KEY_DEADLOCK_RESOLUTION_SCHEME); 
 		if(val != null && val == BDBStoreConfigKeyValues.VALUE_DEADLOCK_RESOLUTION_SCHEME_MONITOR_DEADLOCKS)
 		{
 			//_deadlocking_scheme = BDBStoreConfigKeyValues.VALUE_DEADLOCK_RESOLUTION_SCHEME_MONITOR_DEADLOCKS;
 			int interval = (Integer)config.get(BDBStoreConfigKeyValues.KEY_DEADLOCK_RESOLUTION_SCHEME_MONITOR_DEADLOCKS_INTERVAL);
 			if(interval <= 0)
 				interval = DEFAULT_MONITOR_INTERVAL_FOR_MONITORING_SCHEME;
 			
 			start_deadlock_monitor(interval);
 			logger.debug("init_deadlock_resolution_scheme(HashMap<Object,Object>) - DEADLOCK RESOLUTION SCHEME IS MONITOR WITH AND INTERVAL OF " + interval);
 		}
 		else
 		{
 			//_deadlocking_scheme = BDBStoreConfigKeyValues.VALUE_DEADLOCK_RESOLUTION_SCHEME_ALWAYS_CRAWL_LOCKTABLE;
 			_deadlock_monitor_running = false;
 			logger.debug("init_deadlock_resolution_scheme(HashMap<Object,Object>) - DEADLOCK RESOLUTION SCHEME IS ALWAYS CRAWL");
 		}
 	}
 	
 	private int     _total_num_deadlocks = 0;
 	private void start_deadlock_monitor(final int interval)
 	{
 		
 		if(_deadlock_monitor_running)
 			return;
 		logger.debug("start_deadlock_monitor(int) - Starting deadlock monitor.");
 		_deadlock_monitor_running = true;
 		_deadlock_monitor = new Thread()
 		{
 			public void run()
 			{
 				while(_deadlock_monitor_running)
 				{
 					try{
 						logger.debug("run() - STARTING LOCK DETECTION");
 						int num_deadlocks = environment.detectDeadlocks(LockDetectMode.MINWRITE);
 						_total_num_deadlocks += num_deadlocks;
 						logger.debug("run() - NUM KILLED DEADLOCKS IS " + num_deadlocks);
 						logger.debug("run() - ENDING LOCK DETECTION");
 						Thread.sleep(interval);
 						logger.debug("run() - WAKING UP");
 					}catch(Exception e)
 					{
 						//logger.error("run()", e);
 					}	
 				}
 			}
 		};
 		_deadlock_monitor.start();
 	}
 	
 	private void stop_deadlock_monitor()
 	{
 		if(_deadlock_monitor_running == false)
 			return;
 		
 		_deadlock_monitor_running = false;
 		try {
 			_deadlock_monitor.interrupt();
 			_deadlock_monitor.join();
 			logger.debug("stop_deadlock_monitor() - STOPPING DEADLOCK MONITOR ");
 			logger.debug("stop_deadlock_monitor() - tTOTAL DEADLOCKS " + _total_num_deadlocks);
 		} catch (InterruptedException e) {
 			logger.error("stop_deadlock_monitor()", e);
 		}
 	}
 	
 
 	/* TDS CONFIG */
 	private EnvironmentConfig get_tds_default_config()
 	{
 		EnvironmentConfig env_cfg = new EnvironmentConfig();
 		env_cfg.setAllowCreate(true);
 		env_cfg.setInitializeCache(true);
 		env_cfg.setInitializeLocking(true);
 		env_cfg.setInitializeLogging(true);
 		env_cfg.setRunRecovery(true);
 		env_cfg.setTransactional(true);
 		env_cfg.setErrorStream(System.err);
 		env_cfg.setTxnTimeout(1000 * 1000 * 30);//in microseconds... 30 secconds//
 		//1 megabytes = 1 048 576 bytes
 		env_cfg.setCacheSize(1048576 * 500);
 		// we need enough transactions for the number of 
 		// simultaneous threads per environment
 		env_cfg.setTxnMaxActive(1684);
 		// locks
 
 		env_cfg.setMaxLockers(10000);
 		env_cfg.setMaxLockObjects(10000);
 		env_cfg.setMaxLocks(10000);		
 
 		//env_cfg.setLockDetectMode(LockDetectMode.MINWRITE);
 		//env_cfg.setVerbose(VerboseConfig.FILEOPS_ALL, true);
 		return env_cfg;
 	}
 
 	private EnvironmentConfig get_run_recovery_config(boolean fatal_recovery)
 	{
 		EnvironmentConfig env_cfg = new EnvironmentConfig();
 		env_cfg.setAllowCreate(true);
 		env_cfg.setInitializeCache(true);
 		env_cfg.setInitializeLocking(true);
 		env_cfg.setInitializeLogging(true);
 		if(fatal_recovery)
 			env_cfg.setRunFatalRecovery(true);
 		else
 			env_cfg.setRunRecovery(true);
 		env_cfg.setTransactional(true);
 		env_cfg.setErrorStream(System.err);		
 		//1 megabytes = 1 048 576 bytes
 		env_cfg.setCacheSize(1048576);
 		// we need enough transactions for the number of 
 		// simultaneous threads per environment
 		env_cfg.setTxnMaxActive(1000);
 		// locks
 		env_cfg.setMaxLocks(2000);
 		env_cfg.setMaxLockObjects(2000);
 		env_cfg.setMaxLockers(2000);
 
 		//env_cfg.setLockDetectMode(LockDetectMode.MINWRITE);
 		//env_cfg.setVerbose(VerboseConfig.FILEOPS_ALL, true);
 		return env_cfg;		
 	}
 
 	public int addEntityField(String entity, FieldDefinition entity_field_def)throws PersistenceException
 	{
 		_store_locker.enterLockerThread();
 		try{
 			logger.debug("addEntityField(String, FieldDefinition, Object) - ADD ENTITY FIELD " + entity_field_def);
 			return do_add_entity_field(entity,entity_field_def);	
 		}catch(PersistenceException pe)
 		{
 			throw pe;
 		}
 		finally
 		{
 			_store_locker.exitLockerThread();	
 		}
 	}
 
 
 	protected int do_add_entity_field(String entity,FieldDefinition field) throws PersistenceException
 	{
 		BDBPrimaryIndex pidx = entity_primary_indexes_as_map.get(entity);
 		EntityDefinition old_def = pidx.getEntityDefinition();
 		if(pidx == null)
 			throw new PersistenceException("ADD ENTITY FIELD: ENTITY "+entity+" DOES NOT EXIST!");
 		
 		String fieldname = field.getName();		
 		if(old_def.getField(field.getName())!= null)
 			throw new PersistenceException("ADD ENTITY FIELD: FIELD "+fieldname+" ALREADY EXISTS IN ENTITY");
 		
 		EntityDefinition new_def = old_def.clone();
 		/*add the field to the new def */
 		new_def.addField(field);
 
 		Cursor cursor = null;
 		Transaction txn = null;
 		int count = 0;
 		try
 		{	
 			txn = environment.beginTransaction(null, null);
 			cursor = pidx.getDbh().openCursor(txn, null);			
 			DatabaseEntry foundKey = new DatabaseEntry();
 			DatabaseEntry data     = new DatabaseEntry();
 			
 			/* we dont need to update the indexes here since we are just adding a field*/
 			/* when we delete a field we need to delete asociated indexes if the index only
 			 * has one field and it is the field being deleted
 			 */
 			while (cursor.getNext(foundKey, data, LockMode.DEFAULT) == OperationStatus.SUCCESS)
 			{	
 				Entity e = entity_binding.entryToEntity(old_def,data);
 				cursor.delete();
 				set_default_value(txn,e,field);
 				entity_binding.entityToEntry(new_def,e,data);
 				cursor.put(foundKey,data);
 				count++;	
 			}
 			
 			cursor.close();			
 			txn.commit();
 			
 			/*update entity definition record */
 			/*update entity def */
 			redefine_entity_definition(old_def,new_def);
 		}
 		catch (DatabaseException de)
 		{
 			abortTxn(txn);
 			logger.error("do_add_entity_field(String, FieldDefinition, Object) - Error accessing database." + de, de);
 		}
 		/*duh...need to do this */
 		clean_query_cache(entity);
 		return count;
 	}
 		
 
 	
 	public int deleteEntityField(String entity, String fieldname)throws PersistenceException 
 	{
 		_store_locker.enterLockerThread();
 		try{
 			logger.debug("deleteEntityField(String, String) - DELETE ENTITY FIELD " + entity + "." + fieldname);
 			return do_delete_entity_field(entity,fieldname);
 	
 			}catch(PersistenceException pe)
 			{
 				throw pe;
 			}
 			finally
 			{
 				_store_locker.exitLockerThread();	
 			}
 	}
 			
 	protected int do_delete_entity_field(String entity,String field_name) throws PersistenceException
 	{
 		BDBPrimaryIndex pidx = entity_primary_indexes_as_map.get(entity);
 		EntityDefinition old_def = pidx.getEntityDefinition();
 		FieldDefinition f = old_def.getField(field_name);
 		if(pidx == null)
 			throw new PersistenceException("DELETE ENTITY FIELD: ENTITY "+entity+" DOES NOT EXIST!");
 		if(f == null)
 			throw new PersistenceException("DELETE ENTITY FIELD: FIELD "+field_name+" DOES NOT EXIST IN ENTITY");
 		
 		List<BDBSecondaryIndex> sec_indexes = entity_secondary_indexes_as_list.get(entity);
 		for(int i = 0; i < sec_indexes.size();i++)
 		{
 			BDBSecondaryIndex sec_idx = sec_indexes.get(i);
 			if(sec_idx.isDeepIndex())
 			{
 				System.out.println("WARNING STILL NEEDS TO BE DEALT WITH. SEE RENAME ENTITY FIELD.");
 				try
 				{
 					throw new Exception();
 				}catch(Exception e)
 				{
 					e.printStackTrace();
 				}
 				
 				continue;
 			}
 			if(sec_idx.invalidatedByFieldDelete(f))
 				throw new PersistenceException("DELETE ENTITY FIELD: "+field_name+" has a dependent index( "+sec_idx.getName()+" ). Delete the index first.");
 		}
 		
 		EntityDefinition new_def = old_def.clone();
 		/* remove field from cloned def */
 		List<FieldDefinition> fields =  new_def.getFields();
 		f = null;
 		int s = fields.size();
 		for(int i = 0; i < s;i++)
 		{
 			f = fields.get(i);
 			if(f.getName().equals(field_name))
 			{
 				fields.remove(i);
 				logger.debug("do_delete_entity_field(String, String) - !!! REMOVING " + f.getName());
 				break;
 			}
 		}
 
 		/*rewrite the primary table */
 		Cursor cursor 	= null;
 		Transaction txn = null;
 		int count 		= 0;
 		try
 		{			
 			txn = environment.beginTransaction(null, null);
 			cursor = pidx.getDbh().openCursor(txn, null);			
 			DatabaseEntry foundKey = new DatabaseEntry();
 			DatabaseEntry data     = new DatabaseEntry();
 			
 			while (cursor.getNext(foundKey, data, LockMode.RMW) == OperationStatus.SUCCESS)
 			{			
 				Entity e = entity_binding.entryToEntity(old_def,data);
 				cursor.delete();
 				entity_binding.entityToEntry(new_def,e, data);
 				cursor.put(foundKey,data);
 				count++;
 			}
 			cursor.close();			
 			txn.commit();
 			/*update entity def */
 			redefine_entity_definition(old_def,new_def);
 			
 			/* delete any dependent secondary indexes and free resources associated with it */
 			/* however this will never be called since we are enforcing a guard on the index above.
 			 * an exception is thrown if you try to delete a field which has dependent indexes */
 			List<BDBSecondaryIndex> all_entity_sec_indexes = entity_secondary_indexes_as_list.get(entity);
 			for(int i = 0; i < all_entity_sec_indexes.size();i++)
 			{
 				BDBSecondaryIndex s_idx = all_entity_sec_indexes.get(i);
 				if(s_idx.invalidatedByFieldDelete(f))
 				{
 					/*remove from caches */
 					all_entity_sec_indexes.remove(i);
 					entity_secondary_indexes_as_map.get(entity).remove(s_idx.getName());	
 					
 					/*delete from disk*/
 					s_idx.delete();
 					delete_entity_index_from_db(entity, s_idx.getEntityIndex());
 				}
 			}
 		}
 		catch (DatabaseException de)
 		{
 			abortTxn(txn);
 			logger.error("do_delete_entity_field(String, String) - Error accessing database." + de, de);
 
 		}
 
 		return count;
 	}
 	
 
 	public FieldDefinition renameEntityField(String entity, String old_field_name,String new_field_name) throws PersistenceException
 	{
 		_store_locker.enterLockerThread();
 		try{
 			logger.debug("renameEntityField(String, String, String) - RENAME " + entity + " ENTITY FIELD " + old_field_name + " to " + new_field_name);
 			return do_rename_entity_field(entity, old_field_name, new_field_name);
 			
 		}catch(PersistenceException pe)
 		{
 			throw pe;
 		}
 		finally
 		{
 			_store_locker.exitLockerThread();	
 		}
 		
 	}
 
 	protected FieldDefinition do_rename_entity_field(String entity, String old_field_name,String new_field_name) throws PersistenceException
 	{
 		if(deep_index_list.size() != 0)
 		{
 			System.out.println("WARNING: DEEP IDXS ARE NOT YET DEALT WITH FOR FIELD RENAMES.\n PLEASE REVIEW YOUR DEEP IDXS BASED ON YOUR FIELD RENAME AND DROP IT AND ADD IT IF NECESSARY.");
 			try{
 				throw new Exception();
 			}catch(Exception e)
 			{
 				e.printStackTrace();
 			}
 			
 		}
 		
 		BDBPrimaryIndex pidx = entity_primary_indexes_as_map.get(entity);
 		if(pidx == null)
 			throw new PersistenceException("RENAME ENTITY FIELD: ENTITY "+entity+" DOES NOT EXIST!");
 		
 		EntityDefinition old_def = pidx.getEntityDefinition();
 		if(old_def.getField(old_field_name)== null)
 			throw new PersistenceException("RENAME ENTITY FIELD: FIELD "+old_field_name+" DOES NOT EXIST IN ENTITY "+entity);
 		if(old_def.getField(new_field_name)!= null)
 			throw new PersistenceException("RENAME ENTITY FIELD: FIELD "+new_field_name+" ALREADY EXISTS IN ENTITY");
 		
 		EntityDefinition new_def 			= old_def.clone();
 		List<FieldDefinition> fields 	= new_def.getFields();
 		int s 				= fields.size();
 		FieldDefinition f 	= null;
 		for(int i = 0;i < s;i++)
 		{
 			f = fields.get(i);
 			if(f.getName().equals(old_field_name))
 			{
 				f.setName(new_field_name);
 				break;
 			}
 		}
 		
 
 		/*update entity def */
 		redefine_entity_definition(old_def,new_def);	 
 
 		List<BDBSecondaryIndex> all_indexes_for_entity = entity_secondary_indexes_as_list.get(entity);
 		for(int i = 0; i < all_indexes_for_entity.size();i++)
 		{
 			BDBSecondaryIndex index 		= all_indexes_for_entity.get(i);
 			if(index.indexesField(old_field_name))
 				update_index_field_definition_for_rename(index, old_field_name, new_field_name);
 		}
 		
 		//deal with deep indexes
 
 		//for(int i = 0;i < deep_index_list.size();i++)
 		//{
 			//BDBSecondaryIndex index 		= deep_index_list.get(i);
 			//for(int p = 0;p< index.getFields().size();p++)
 			//{
 			////////////////////////////////////////////////	
 				//FieldDefinition ff = index.getFields().get(p);
 				//if(ff.getName().indexOf('.')== -1)
 				//{
 					//TODO: might have to deal with this seperately//
 					//also this same routine can be used to check if a deep index indexes a field//
 					//if(index.getEntityDefinition().getName().equals(old_def.getName())&& old_field_name.equals(ff.getName()))
 						//ff.setName(new_field_name);
 
 					//continue;
 				//}
 				
 				//String[] ref_path = (String[])index.getAttribute(BDBSecondaryIndex.ATTRIBUTE_DEEP_INDEX_PATH_LOCATOR_PREFIX+p);
 				//List<FieldDefinition>[] ref_path_types = (List<FieldDefinition>[])index.getAttribute(BDBSecondaryIndex.ATTRIBUTE_DEEP_INDEX_PATH_TYPE_LOCATOR_PREFIX+p);
 
 				//if(index.getEntityDefinition().getName().equals(old_def.getName()))
 				//{
 		
 					//if(ref_path[0].equals(old_field_name))
 					//{
 					//	throw new PersistenceException("DEEP INDEX "+index.getEntityIndex().getName()+" DEPENDS ON THIS FIELD."+old_field_name+"DROP THE INDEX FIRST");
 						//ref_path[0] = new_field_name;
 						//index.getAttributes().put(BDBSecondaryIndex.ATTRIBUTE_DEEP_INDEX_PATH_LOCATOR_PREFIX+p,ref_path);
 					//}
 				//}
 				//for(int ii = 0;ii < ref_path_types.length-1;ii++)
 				//{
 					//List<FieldDefinition> dd = ref_path_types[ii];
 					//for(int j = 0;j < dd.size();j++)
 					//{
 						//FieldDefinition type = dd.get(j);
 						//if(type.getReferenceType().equals(old_def.getName()) || 
 							//	type.getReferenceType().equals(FieldDefinition.REF_TYPE_UNTYPED_ENTITY))
 						//{
 							//if(ref_path[ii+1].equals(old_field_name))
 							//{
 								//throw new PersistenceException("DEEP INDEX "+index.getEntityIndex().getName()+" DEPENDS ON THIS FIELD."+old_field_name+"DROP THE INDEX FIRST");
 								//update this index//
 					//			ref_path[ii+1] = new_field_name;
 					//			List<FieldDefinition> types = ref_path_types[ii+1];
 					//			for(int k=0;k< types.size();k++)
 					//			{
 					//				types.get(k).setName(new_field_name);
 					//			}
 								
 								//update aux index names
 								//jeez...sigh sigh sigh
 				//				types = ref_path_types[ii];
 				//				for(int k=0;k< types.size();k++)
 				//				{
 				//					String aux_index_entity   = ref_path_types[ii].get(k).getReferenceType();
 				//					String old_aux_index_name = "deep_aux_by"+intercaps(old_field_name);
 				//					String new_aux_index_name = "deep_aux_by"+intercaps(new_field_name);
 				//					if(do_get_entity_index(aux_index_entity, new_aux_index_name)==null)										
 				//						do_rename_entity_index(aux_index_entity, old_aux_index_name, new_aux_index_name);
 				//				}
 							//}
 						//}
 				//	}
 				//}
 				//try{
 				//	System.out.println("INDEX NAME IS "+index.getName()+" INDEX "+index.getEntityIndex());
 				//	delete_entity_index_from_db(index.getEntityDefinition().getName(), index.getEntityIndex());
 				//	add_entity_index_to_db(index.getEntityDefinition().getName(), index.getEntityIndex());
 				//	Map<String,List<BDBSecondaryIndex>> meta = deep_index_meta_map.get(old_def.getName());
 				//	meta.put(new_field_name,meta.remove(old_field_name));			
 				//}//catch(DatabaseException dbe)
 				//{
 					//throw new PersistenceException("FAILED UPDATING DEEP INDEX DEF ON FIELD RENAME.");
 				//}
 			//}
 		//}
 		///end dealing with deep indexes//
 		return f; 
 	}
 
 	/*this deals with updateing the entity index instance in the event that you change the name of one of the fields it is indexing */
 	private void update_index_field_definition_for_rename(BDBSecondaryIndex index,String old_field_name,String new_field_name) throws PersistenceException
 	{	
 		EntityIndex   idx 				= index.getEntityIndex();
 		List<FieldDefinition> ifields	= index.getFields();
 		for(FieldDefinition fd:ifields)
 		{
 			if(fd.getName().equals(old_field_name))
 			{
 				try{
 					
 					/*persist change..we need to do the delete with the idx still referring to the old
 					 * fieldname. this is because we use getSearchBoth to delete it. see delete_entity_index_from_db
 					 * for details.*/
 					delete_entity_index_from_db(index.getEntityDefinition().getName(), idx);
 					fd.setName(new_field_name);
 					add_entity_index_to_db(index.getEntityDefinition().getName(), idx);
 					/* this is in case it is using the name of the field for some reason 
 					 * in the filename of its db
 					 */
 					//TODO: get rid of this crap..ha ha
 					index.fieldChangedName(old_field_name, new_field_name);	
 					break;
 				}catch(DatabaseException e)
 				{
 					logger.error("do_rename_entity_field(String, String, String)", e);
 					throw new PersistenceException("UNABLE TO COMMIT FIELD RENAME TO DEPENDEDNT INDEX");
 				}
 			}
 		}
 
 	}
 
 	public EntityIndex addEntityIndex(String entity,String field_name,int index_type,String index_name, Map<String,Object> attributes) throws PersistenceException
 	{
 		_store_locker.enterLockerThread();
 		try{
 			logger.debug("addEntityIndex(String, String, String, String, Map<String,Object>) - ADD ENTITY INDEX " + index_name + " ON " + entity + " OF TYPE " + index_type);
 			return do_add_entity_index(entity,new String[]{field_name},index_type,index_name,attributes);
 	
 		}catch(PersistenceException pe)
 		{
 			throw pe;
 		}
 		finally
 		{
 			_store_locker.exitLockerThread();	
 		}		
 	}
 	
 	public EntityIndex addEntityIndex(String entity,String[] field_names,int index_type,String index_name, Map<String,Object> attributes) throws PersistenceException
 	{
 		_store_locker.enterLockerThread();
 		try{
 			logger.debug("addEntityIndex(String, String[], String, String, Map<String,Object>) - ADD ENTITY INDEX " + index_name + " ON " + entity + " OF TYPE " + index_type);
 			return do_add_entity_index(entity,field_names,index_type,index_name,attributes);	
 		}catch(PersistenceException pe)
 		{
 			throw pe;
 		}
 		finally
 		{
 			_store_locker.exitLockerThread();	
 		}		
 	}
 	
 	
 	protected EntityIndex do_add_entity_index(String entity,String[] field_names,int index_type,String index_name,Map<String,Object>attributes) throws PersistenceException
 	{
 		BDBPrimaryIndex pidx = entity_primary_indexes_as_map.get(entity);
 		if(pidx == null)
 			throw new PersistenceException("ADD ENTITY INDEX: ENTITY "+entity+" DOES NOT EXIST!");
 
 		if (entity_secondary_indexes_as_map.get(entity).get(index_name)!= null)
 			throw new PersistenceException("ADD ENTITY INDEX: INDEX "+index_name+" ALREADY EXISTS IN ENTITY");
 
 		
 		EntityDefinition def 	= pidx.getEntityDefinition();
 		EntityIndex   eii 		= new EntityIndex(index_name,index_type);
 		eii.setEntity(entity);
 		for(int i = 0; i < field_names.length;i++)
 		{
 			String fieldname = field_names[i];
 			if(fieldname.indexOf('.') == -1)
 			{
 				FieldDefinition field 	= def.getField(field_names[i]);
 				if (field==null)
 					throw new PersistenceException("ADD ENTITY INDEX: FIELD "+field_names[i]+" DOES NOT EXIST IN ENTITY "+entity);
 				eii.addField(field);
 			}
 			else
 			{
 				//BEGIN CRAZINESS
 				//this fieldname has a dot in it so they must be dereferencing some
 				//complex field  i.e. "daughter.father.name//
 				String[] parts 		  		= fieldname.split("\\.");
 				List<FieldDefinition>[] path_ref_types = new List[parts.length];
 				resolve_ref_path_ref_types(def,def,parts,path_ref_types,i,0,attributes);
 				
 				/*now we have parts with all their types matched */
 				for(int ii = 0;ii < path_ref_types.length-1;ii++)
 				{
 					System.out.println("PARTS "+ii+" "+parts[ii]);
 					List<FieldDefinition> l = path_ref_types[ii];
 					System.out.print("REF TYPES "+ii+" ");
 					for(int iii = 0;iii < l.size();iii++)
 					{
 						System.out.print(l.get(iii).getReferenceType()+" ");
 					}
 					System.out.println();
 				}
 				
 				FieldDefinition terminal_field = path_ref_types[path_ref_types.length-1].get(0);
 				System.out.println("TERMINAL FIELD IS "+path_ref_types[path_ref_types.length-1].get(0));
 				//set the field for the index//
 				eii.addField(terminal_field);
 				
 				//DEAL WITH ADDING THE INDEX CHAIN											  //
 				//ADD UPWARD INDEXES IN THE CHAIN IGNORING THE TERMINAL FIELD(parts.length -1)//
 				//START AT THE TOP
 				
 				String aux_index = def.getName()+" IDX_BY_"+parts[0];
 				//System.out.println("IF "+aux_index+" DOES NOT EXIST ADD "+aux_index);
 				String aux_index_name = "deep_aux_by"+intercaps(parts[0]);
 				//if(path_ref_types[0].get(0).getReferenceType() == FieldDefinition.REF_TYPE_UNTYPED_ENTITY)
 				//	aux_index_name = 
 				
 				if(do_get_entity_index(def.getName(),aux_index_name) == null )
 				{
 					FieldDefinition type = def.getField(parts[0]);
 					logger.info("ADDING "+aux_index_name+" TO "+def.getName()+" IN THE SERVICE OF "+eii.getName());
 					if(type.isArray())
 					{
 						logger.info("ADDING ARRAY INDEX "+aux_index_name+" TO "+type.getReferenceType()+" IN THE SERVICE OF "+eii.getName());
 						do_add_entity_index(def.getName(), new String[]{parts[0]},EntityIndex.TYPE_ARRAY_MEMBERSHIP_INDEX , aux_index_name,null);
 					}
 					else
 					{
 						logger.info("ADDING SIMPLE INDEX "+aux_index_name+" TO "+type.getReferenceType()+" IN THE SERVICE OF "+eii.getName());
 						do_add_entity_index(def.getName(), new String[]{parts[0]},EntityIndex.TYPE_SIMPLE_SINGLE_FIELD_INDEX , aux_index_name,null);
 					}
 				}
 				//AND MOVE THROUGH PATH//
 				for(int j = 0;j < parts.length-1;j++)
 				{
 					System.out.println("AT PART "+parts[j]);
 					List<FieldDefinition> types = path_ref_types[j];
 					System.out.println("TYPES IS "+types);
 					for(int k = 0; k < types.size();k++)
 					{
 						FieldDefinition left_type = types.get(k);
 						System.out.println("LEFT TYPE IS "+left_type);
 						EntityDefinition d = do_get_entity_definition(left_type.getReferenceType());
 						if(d == null)
 							throw new PersistenceException("BAD REF PATH "+left_type.getReferenceType()+" DOES NOT EXIST IN STORE. "+eii.getName());
 						System.out.println("ENTITY DEF IS "+d);
 						System.out.println("PARTS J+1 "+parts[j+1]);
 						FieldDefinition right_type = d.getField(parts[j+1]);
 						System.out.println("R TYPE IS "+right_type);
 						aux_index = left_type.getReferenceType()+" IDX_BY_"+parts[j+1];
 						aux_index_name = "deep_aux_by"+intercaps(parts[j+1]);					
 						if(do_get_entity_index(left_type.getReferenceType(),aux_index_name) == null )
 						{
 							if(right_type.isArray())
 							{
 								logger.info("ADDING ARRAY INDEX "+aux_index_name+" TO "+left_type.getReferenceType()+" IN THE SERVICE OF "+eii.getName());
 								do_add_entity_index(left_type.getReferenceType(), new String[]{parts[j+1]},EntityIndex.TYPE_ARRAY_MEMBERSHIP_INDEX , aux_index_name,null);		
 							}
 							else
 							{
 								logger.info("ADDING SIMPLE INDEX "+aux_index_name+" TO "+left_type.getReferenceType()+" IN THE SERVICE OF "+eii.getName());
 								do_add_entity_index(left_type.getReferenceType(), new String[]{parts[j+1]},EntityIndex.TYPE_SIMPLE_SINGLE_FIELD_INDEX , aux_index_name,null);		
 							}
 						}
 					}
 				}
 				
 				attributes.put(BDBSecondaryIndex.ATTRIBUTE_IS_DEEP_INDEX, BDBSecondaryIndex.ATTRIBUTE_IS_DEEP_INDEX_YES);
 				//TODO: this is only for single field indexes//
 				attributes.put(BDBSecondaryIndex.ATTRIBUTE_DEEP_INDEX_PATH_LOCATOR_PREFIX+i,parts);
 				attributes.put(BDBSecondaryIndex.ATTRIBUTE_DEEP_INDEX_PATH_TYPE_LOCATOR_PREFIX+i,path_ref_types);
 
 				//END CRAZINESS
 			}
 		}
 		
 		if (attributes!=null)
 		{
 			Iterator<String> keys 	 = attributes.keySet().iterator();
 			while(keys.hasNext())
 			{
 				String att_name = keys.next();
 				Object att_value = attributes.get(att_name);
 				eii.setAttribute(att_name, att_value);
 			}
 		}
 		Class<?> c = null;
 		
 		BDBSecondaryIndex index = null;
 		String classname = null;		
 		switch(index_type)
 		{
 			case EntityIndex.TYPE_SIMPLE_SINGLE_FIELD_INDEX:
 				classname = "com.pagesociety.bdb.index.SimpleSingleFieldIndex";
 				break;
 			case EntityIndex.TYPE_SIMPLE_MULTI_FIELD_INDEX:
 				classname = "com.pagesociety.bdb.index.SimpleMultiFieldIndex";
 				break;
 			case EntityIndex.TYPE_ARRAY_MEMBERSHIP_INDEX:
 				classname = "com.pagesociety.bdb.index.ArrayMembershipIndex";
 				break;
 			case EntityIndex.TYPE_MULTIFIELD_ARRAY_MEMBERSHIP_INDEX:
 				classname = "com.pagesociety.bdb.index.MultiFieldArrayMembershipIndex";
 				break;
 			case EntityIndex.TYPE_FREETEXT_INDEX:
 				classname = "com.pagesociety.bdb.index.SingleFieldFreeTextIndex";
 				break;	
 			case EntityIndex.TYPE_MULTI_FIELD_FREETEXT_INDEX:
 				classname = "com.pagesociety.bdb.index.MultiFieldFreeTextIndex";
 				break;					
 			default:
 				throw new PersistenceException("UNKNOWN INDEX TYPE 0x"+Integer.toHexString(index_type));
 		}
 		
 		
 		try {
 			c = Class.forName(classname);
 			index = (BDBSecondaryIndex)c.newInstance();
 			try{
 				index.validateFields(eii.getFields());
 			}catch(PersistenceException pe)
 			{
 				throw new PersistenceException(pe.getMessage());
 			}
 
 		} catch (Exception ee) {
 			logger.error("do_add_entity_index(String, String[], String, String, Map<String,Object>)", ee);
 			throw new PersistenceException("FAILED INSTANTIATING INSTANCE OF INDEX "+eii.getType());
 		}
 		index.setup(pidx, eii);
 		
 
 		
 		/*** AUTO POPULATE ON INDEX CREATION*******************************/
 		/*** when the index is created fill it up with data from ptable ***/
 		/*** maybe make this optional for the case where you just want to index ***/
 		/*** data from now ***/
 		long t1 = System.currentTimeMillis();
 		Transaction txn = null;
 		Cursor cursor   = null;
 		try{
 			txn = environment.beginTransaction(null, null);
 			DatabaseEntry pkey = new DatabaseEntry();
 			DatabaseEntry data = new DatabaseEntry();
 			cursor = pidx.getDbh().openCursor(null, null);
 			Entity e = null;
 			int i = 0;
 			while(cursor.getNext(pkey, data, LockMode.DEFAULT) == OperationStatus.SUCCESS)
 			{	
 				i++;
 				try{
 					e = entity_binding.entryToEntity(def, data);
 					//System.out.println(index.getName()+">>>> AUTO POPULATE WITH"+e.getType()+" "+e.getId());
 					//if index is complex
 					if(index.getAttribute(BDBSecondaryIndex.ATTRIBUTE_IS_DEEP_INDEX) == BDBSecondaryIndex.ATTRIBUTE_IS_DEEP_INDEX_YES)
 					{
 						/*
 						System.out.println("BEFORE COMPLEX FILL:");
 						Entity ddata 	= null;
 						Entity subdata  = null;
 						String city 	= null;
 						ddata = (Entity)e.getAttribute("Data");
 						System.out.println("\tData is "+ddata);
 						if(ddata != null)
 						{
 							subdata = (Entity)e.getAttribute("SubData");
 							System.out.println("\tSubData is "+subdata);
 
 							if(subdata != null)
 							{
 								city = (String)subdata.getAttribute("City");
 								System.out.println("\tCity is "+city);
 								System.out.println();
 							}
 						}
 						*/
 						expand_all_complex_fields(index, e);
 						/*		
 						System.out.println("AFTER COMPLEX FILL:");
 						ddata = (Entity)e.getAttribute("Data");
 						System.out.println("\tData is "+ddata);
 						if(ddata != null)
 						{
 							subdata = (Entity)ddata.getAttribute("SubData");
 							System.out.println("\tSubData is "+subdata);
 
 							if(subdata != null)
 							{
 								city = (String)subdata.getAttribute("City");
 								System.out.println("\tCity is "+city);
 								System.out.println();
 							}
 						}
 						 */
 					}
 				
 					index.insertIndexEntry(txn, e, pkey);
 				}catch(Exception ee)
 				{
 					System.out.println("FAILED UPDATING ENTITY AFTER ADDING INDEX");
 					System.out.println("DEF WAS :\n"+def+"\n");
 					System.out.println("INDEX WAS :\n"+index+"\n");
 					System.out.println("DATA WAS :\n"+new String(data.getData())+"\n");
 					ee.printStackTrace();
 					throw new PersistenceException("DAVID SHOW THIS TO TOPH.");
 				}
 			}
 			cursor.close();
 			txn.commit();
 			
 		}catch(DatabaseException de)
 		{
 			close_cursors(cursor);
 			abortTxn(txn);
 			logger.error("do_add_entity_index(String, String[], String, String, Map<String,Object>)", de);
 			throw new PersistenceException("ADD INDEX POPULATE FAILED FOR "+index.getName());
 		}
 		long t2 = System.currentTimeMillis();
 		logger.debug("do_add_entity_index(String, String[], String, String, Map<String,Object>) - INITIAL POPULATE OF INDEX TOOK " + (t2 - t1) + " (ms)");
 		/****END AUTO POPULATE**********************************************************/
 		
 		/* maintain runtime cache */
 		/*BEGIN PART OF CRAZINESS*/
 		if(index.getAttribute(BDBSecondaryIndex.ATTRIBUTE_IS_DEEP_INDEX) == BDBSecondaryIndex.ATTRIBUTE_IS_DEEP_INDEX_YES)
 		{
 			System.out.println("UPDATING META INFO FOR DEEP INDEX "+index.getName()+" ADDING TO DEEP INDEX META MAP");
 			//add it to the deep index meta map//			
 			update_deep_index_meta_info(index);
 			deep_index_list.add(index);
 		}
 		
 		entity_secondary_indexes_as_list.get(entity).add(index);
 		entity_secondary_indexes_as_map.get(entity).put(eii.getName(),index);
 		/*END PART OF CRAZINESS*/
 		
 		try
 		{
 			add_entity_index_to_db(entity, eii);
 		}
 		catch (DatabaseException e)
 		{
 			logger.error("do_add_entity_index(String, String[], String, String, Map<String,Object>)", e);
 			throw new PersistenceException("Couldn't add index "+eii.getName()+" for entity "+entity);
 		}		
 		return eii;
 	}
 	
 /////BEGIN CRAZY DEEP INDEXING SUPPORT STUFF//////////////////////////////////////////////////////
 	
 	private void expand_all_complex_fields(BDBSecondaryIndex complex_index,Entity e) throws PersistenceException
 	{
 		List<FieldDefinition> fields = complex_index.getFields();
 
 		for(int i=0;i < fields.size();i++)
 		{
 			String[] ref_path = (String[])complex_index.getAttribute(BDBSecondaryIndex.ATTRIBUTE_DEEP_INDEX_PATH_LOCATOR_PREFIX+i);
 			List<FieldDefinition>[] ref_path_types = (List<FieldDefinition>[])complex_index.getAttribute(BDBSecondaryIndex.ATTRIBUTE_DEEP_INDEX_PATH_TYPE_LOCATOR_PREFIX+i);
 			if(ref_path != null)
 				expand_ref_path(e,ref_path,ref_path_types,0,new HashMap<Integer,Entity>());
 
 		}
 	}
 	
 	private void expand_ref_path(Entity e,String[] ref_path,List<FieldDefinition>[] ref_path_types ,int offset,Map<Integer,Entity> seen) throws PersistenceException
 	{
 		if(offset == ref_path.length-1)
 			return;
 		
 		String fieldname = ref_path[offset];
 		Object ref 		 = e.getAttribute(fieldname);	
 		if(ref == null || ref != null && seen.containsKey(ref.hashCode()))//guard against circular references			
 			return;
 		
 		FieldDefinition fill_field 		= do_get_entity_definition(e.getType()).getField(fieldname);
 		
 		if(fill_field.isArray())
 		{
 			List<Entity> aref = (List<Entity>)ref;
 			if(aref.size() == 0)
 				return;
 			else
 			{
 				Entity blah = aref.get(0);
 				if(blah.isLightReference())
 					do_fill_reference_field(null,e,fill_field);
 			}
 		}
 		else
 		{
 			Entity asref = (Entity)ref;
 			if(asref.isLightReference())
 				do_fill_reference_field(null,e,fill_field);				
 		}
 		
 		ref = e.getAttribute(fieldname);
 		if(ref == null)
 			return;
 		if(fill_field.isArray())
 		{
 			List<Entity> vals = (List<Entity>)ref;
 			if(vals == null)
 				return;
 			int s = vals.size();
 			
 			for(int i = 0;i < s;i++ )
 			{
 				Entity lref = vals.get(i);
 				seen.put(lref.hashCode(),lref);	
 				expand_ref_path(lref, ref_path,ref_path_types, offset+1,seen);
 			}
 		}
 		else
 		{
 			if( ref == null)
 				return;
 			seen.put(ref.hashCode(),(Entity)ref);	
 			expand_ref_path((Entity)ref, ref_path,ref_path_types, offset+1,seen);
 		}
 
 
 
 	}
 	
 	
 	protected Map<String,Map<String,List<BDBSecondaryIndex>>> deep_index_meta_map = new HashMap<String,Map<String,List<BDBSecondaryIndex>>>(); 
 	protected List<BDBSecondaryIndex> deep_index_list 	  						  = new ArrayList<BDBSecondaryIndex>(); 
 	private void update_deep_index_meta_info(BDBSecondaryIndex index) throws PersistenceException
 	{
 		add_to_deep_index_metainfo(index);
 	}
 	
 	private void add_to_deep_index_metainfo(BDBSecondaryIndex index) throws PersistenceException
 	{
 		for(int i = 0;i < index.getFields().size();i++)
 		{
 			FieldDefinition d = index.getFields().get(i);
 			if(d.getName().indexOf('.') == -1)
 				continue;
 			String[] parts = (String[])index.getAttribute(BDBSecondaryIndex.ATTRIBUTE_DEEP_INDEX_PATH_LOCATOR_PREFIX+i);
 			List<FieldDefinition>[] ref_path_ref_types = new List[parts.length];
 			resolve_ref_path_ref_types(index.getEntityDefinition(),index.getEntityDefinition(),parts,ref_path_ref_types,i,0,index.getAttributes());
 			add_item_to_deep_index_meta_map(index.getEntityDefinition().getName(), parts[0], index);
 			
 			for(int j = 0;j < parts.length-1;j++)
 			{
 				String part 				= parts[j];
 				List<FieldDefinition> types = ref_path_ref_types[j];
 				for(FieldDefinition type:types)
 					add_item_to_deep_index_meta_map(type.getReferenceType(), parts[j+1], index);
 			}
 		}
 	}
 
 	private void add_item_to_deep_index_meta_map(String entity_name,String fieldname,BDBSecondaryIndex index)
 	{
 		Map<String,List<BDBSecondaryIndex>> deep_per_entity = deep_index_meta_map.get(entity_name);
 		if(deep_per_entity == null)
 		{
 			deep_per_entity 	= new HashMap<String,List<BDBSecondaryIndex>>();
 			deep_index_meta_map.put(entity_name,deep_per_entity); 
 		}
 		
 		List<BDBSecondaryIndex> deep_per_field = deep_per_entity.get(fieldname);
 		if(deep_per_field == null)
 		{
 			deep_per_field = new ArrayList<BDBSecondaryIndex>();
 			deep_per_entity.put(fieldname,deep_per_field);
 		}
 
 		deep_per_field.add(index);
 		//we add it to the list here too
 		
 		//System.out.println("ADD "+entity_name+"->"+fieldname+"->"+index.getName());
 		//System.out.println("MAP IS NOW:\n "+index_meta_map_to_string());
 	}
 	
 	private String index_meta_map_to_string()
 	{
 		StringBuilder buf = new StringBuilder();
 		Iterator<String> i = deep_index_meta_map.keySet().iterator();
 		while(i.hasNext())
 		{
 			String entity = i.next();
 			buf.append(entity+"\n");
 			Iterator<String> ii = deep_index_meta_map.get(entity).keySet().iterator();
 			while(ii.hasNext())
 			{
 				String field = ii.next();
 				buf.append("\t"+field+"\n");
 				List<BDBSecondaryIndex> idxs = deep_index_meta_map.get(entity).get(field);
 				for(int iii = 0;iii< idxs.size();iii++)
 					buf.append("\t\t"+idxs.get(iii).getName()+"\n");
 			}
 		}		
 		return buf.toString();
 	}
 	
 	private void resolve_ref_path_ref_types(EntityDefinition top_def,EntityDefinition def,String[] parts,List<FieldDefinition>[] ref_part_types,int field_offset,int parts_offset,Map<String,Object> idx_attributes) throws PersistenceException
 	{
 		FieldDefinition f = def.getField(parts[parts_offset]);
 		if(f == null)
 			throw new PersistenceException("BAD REFERENCE PATH "+ref_path_as_string(top_def, parts)+". CRAPPED OUT ON FIELD "+parts[parts_offset]+". IT DOES NOT EXIST.");
 		
 		List<FieldDefinition> ff = new ArrayList<FieldDefinition>();
 		ref_part_types[parts_offset] = ff;
 		if(parts.length-1 == parts_offset)
 		{
 			//clone the field def.addField(this is the terminal one)
 			FieldDefinition terminal_field_def = new FieldDefinition(ref_path_as_string(parts),f.getType(),f.getReferenceType());
 			ff.add(terminal_field_def);
 			return;
 		}
 		else
 		{
 			if(f.getBaseType()!= Types.TYPE_REFERENCE)
 				throw new PersistenceException("BAD REFERENCE PATH "+ref_path_as_string(top_def, parts)+". CRAPPED OUT ON FIELD "+parts[parts_offset]+" IT IS NOT A REFERENCE TYPE FIELD.");
 			
 			String ref_type 		 = f.getReferenceType();
 			EntityDefinition d		 = null;
 			if(ref_type.equals(FieldDefinition.REF_TYPE_UNTYPED_ENTITY))
 			{
 				String[] union = (String[])idx_attributes.get(field_offset+"_"+parts_offset+"_can_be");
 				if(union == null)
 					throw new PersistenceException(ref_path_as_string(top_def, parts)+" PLEASE DEFINE INDEX ATTRIBUTE "+field_offset+"_"+parts_offset+"_can_be TO DECLARE THE UNION OF TYPES FOR UNTYPED REFERENCE "+f.getName());
 				for(int i = 0;i < union.length;i++)
 				{
 					d = do_get_entity_definition(union[i]);
 					//TODO: SHOULD CHECK TO MAKE SURE THAT parts[offeset+1] i.e. the untyped field name is of the same type
 					//in all the unions 
 					//shit could get weird here with chains of untyped references but maybe not. we will see
 					if(d == null)
 						throw new PersistenceException("UNTYPED REFERENCE IN "+ref_path_as_string(top_def, parts)+" INDEX ATTRIBUTE "+field_offset+"_"+parts_offset+"_can_be CONTAINS INVALID UNION MEMBER "+union[i]);
 					
 					FieldDefinition fff = new FieldDefinition(f.getName(),Types.TYPE_REFERENCE,d.getName());
 					ff.add(fff);
 				}
 			}
 			else//dont think we need to clone this//
 			{
 				d = do_get_entity_definition(f.getReferenceType());
 				ff.add(f);
 			}
 			resolve_ref_path_ref_types(top_def, d, parts,ref_part_types,field_offset,++parts_offset,idx_attributes);
 		}
 	}
 	
 	private static String ref_path_as_string(EntityDefinition top_def,String[] parts)
 	{
 		StringBuilder path = new StringBuilder();
 		path.append(top_def.getName());
 		for(int i = 0;i < parts.length;i++)
 		{
 			path.append('.');
 			path.append(parts[i]);
 		}
 		return path.toString();
 	}
 	
 	private static String ref_path_as_string(String[] parts)
 	{
 		StringBuilder path = new StringBuilder();
 		for(int i = 0;i < parts.length;i++)
 		{
 			path.append(parts[i]);
 			path.append('.');
 		}
 		path.setLength(path.length()-1);
 		return path.toString();
 	}
 
 	private String intercaps(String string) {
 		char[] cc = string.toCharArray();
 		cc[0] = Character.toUpperCase(cc[0]);
 		for(int i = 1;i < cc.length;i++)
 			cc[i] = Character.toLowerCase(cc[i]);
 		return new String(cc);
 	}
 /////////////////////////END CRAZY DEEP INDEXING STUFF//////////////////////////////////////////
 	public void deleteEntityIndex(String entity,String index_name) throws PersistenceException
 	{
 		_store_locker.enterLockerThread();
 		try{
 			logger.debug("deleteEntityIndex(String, String) - DELETE ENTITY INDEX ON " + entity + " OF TYPE " + index_name);
 			do_delete_entity_index(entity,index_name);
 		}catch(PersistenceException pe)
 		{
 			throw pe;
 		}
 		finally
 		{
 			_store_locker.exitLockerThread();	
 		}		
 	}
 	
 	protected void do_delete_entity_index(String entity,String index_name) throws PersistenceException
 	{
 		BDBPrimaryIndex pidx = entity_primary_indexes_as_map.get(entity);
 		if(pidx == null)
 			throw new PersistenceException("DELETE ENTITY INDEX: entity "+entity+" does not exist");
 		if(entity_secondary_indexes_as_map.get(entity).get(index_name)== null)
 			throw new PersistenceException("DELETE ENTITY INDEX: index "+index_name+" does not exists in "+entity);
 		
 		List<BDBSecondaryIndex> sec_idxs = entity_secondary_indexes_as_list.get(entity);
 		for(int i = 0; i < sec_idxs.size();i++)
 		{
 			BDBSecondaryIndex s_idx = sec_idxs.get(i);
 			if(s_idx.getName().equals(index_name))
 			{
 				try{
 					s_idx.delete();
 					delete_entity_index_from_db(entity, s_idx.getEntityIndex());
 				}catch(DatabaseException e)
 				{
 					logger.error("do_delete_entity_index(String, String)", e);
 					throw new PersistenceException("UNABLE TO DELETE INDEX "+index_name);
 				}
 				
 				/* maintain runtime cache */
 				entity_secondary_indexes_as_list.get(entity).remove(i);
 				entity_secondary_indexes_as_map.get(entity).remove(s_idx.getName());
 				break;
 			}
 		}
 		
 	}
 	
 	public void renameEntityIndex(String entity,String old_name,String new_name) throws PersistenceException
 	{
 		_store_locker.enterLockerThread();
 		try{
 			logger.debug("renameEntityIndex(String, String, String) - RENAME ENTITY INDEX ON " + entity + " FROM " + old_name + " TO " + new_name);
 			do_rename_entity_index(entity,old_name,new_name);
 		}catch(PersistenceException pe)
 		{
 			throw pe;
 		}
 		finally
 		{
 			_store_locker.exitLockerThread();	
 		}		
 	}
 	
 	protected void do_rename_entity_index(String entity,String old_name,String new_name) throws PersistenceException
 	{
 		BDBPrimaryIndex pidx = entity_primary_indexes_as_map.get(entity);
 		if(pidx == null)
 			throw new PersistenceException("RENAME ENTITY INDEX: entity "+entity+" does not exist");
 		if(entity_secondary_indexes_as_map.get(entity).get(old_name)== null)
 			throw new PersistenceException("RENAME ENTITY INDEX: index "+old_name+" does not exists in "+entity);
 		if(entity_secondary_indexes_as_map.get(entity).get(new_name)!= null)
 			throw new PersistenceException("RENAME ENTITY INDEX: index "+new_name+" already exists in "+entity);
 		
 		/*update runtime caches */
 		List<BDBSecondaryIndex> sec_idxs = entity_secondary_indexes_as_list.get(entity);
 		BDBSecondaryIndex s_idx;
 		for(int i = 0; i < sec_idxs.size();i++)
 		{
 			s_idx = sec_idxs.get(i);
 			if(s_idx.getName().equals(old_name))
 			{
 				EntityIndex idx = s_idx.getEntityIndex();
 				try{
 					delete_entity_index_from_db(entity, idx);
 					s_idx = entity_secondary_indexes_as_map.get(entity).remove(old_name);
 					idx.setName(new_name);
 					add_entity_index_to_db(entity, idx);
 					entity_secondary_indexes_as_map.get(entity).put(new_name,s_idx);
 				}catch(DatabaseException de)
 				{
 					logger.error("do_rename_entity_index(String, String, String)", de);
 				}
 				break;
 			}
 		}
 	}
 
 
 	private EntityIndex delete_entity_index_from_db(String entity,EntityIndex idx) throws DatabaseException
 	{
 		System.out.println("DELETING "+entity+" "+idx.getName()+" FROM DB");
 		DatabaseEntry key  = new DatabaseEntry();
 		FieldBinding.valueToEntry(Types.TYPE_STRING, entity, key);
 		
 		Transaction txn = null;
 		try{
 
 			txn = environment.beginTransaction(null, null);
 			Cursor cursor 	   = entity_index_db.openCursor(txn, null);
 			DatabaseEntry data = new DatabaseEntry();
 			OperationStatus op_stat = cursor.getSearchKey(key, data, LockMode.DEFAULT);
 			if(op_stat == OperationStatus.NOTFOUND)
 				throw new DatabaseException("NOTFOUND: COULDNT DELETE IDX "+entity+" "+idx.getName()+" BECAUSE THERE ARE NO INDEXES FOR "+entity);			
 
 			while((op_stat = cursor.getNextDup(key, data, LockMode.DEFAULT)) == OperationStatus.SUCCESS);
 			{
 				EntityIndex ei = (EntityIndex)entity_index_binding.entryToObject(data);
 				System.out.println("EI IS "+ei.getName()+" "+idx.getName());
 				if(ei.getName().equals(idx.getName()))
 				{
 					op_stat = cursor.delete();
 					cursor.close();
 					txn.commit();
 					if(op_stat != OperationStatus.SUCCESS)
 						throw new DatabaseException("DELETE FAILED: COULDNT DELETE IDX "+entity+" "+idx.getName());
 					return ei;
 				}
 			}
 			if(op_stat == OperationStatus.NOTFOUND)
 				throw new DatabaseException("NOTFOUND: COULDNT DELETE IDX "+entity+" "+idx.getName()+" DIDNT FIND IT.");			
 			
 		}catch(DatabaseException dbe)
 		{
 			try{
 				txn.abort();
 			}catch(Exception e)
 			{
 				e.printStackTrace();
 			}
 			dbe.printStackTrace();
 			throw new DatabaseException("DATABASE FAILURE FOR DELETE ENTITY INDEX");
 		}
 		return null;
 	}
 	
 	private void add_entity_index_to_db(String entity,EntityIndex idx) throws DatabaseException
 	{
 		logger.info("ADDING "+entity+" "+idx.getName()+" TO DB");
 		DatabaseEntry key = new DatabaseEntry();
 		FieldBinding.valueToEntry(Types.TYPE_STRING, entity,key);
 		OperationStatus op_stat = entity_index_db.put(null, key, entity_index_binding.objectToEntry(idx));
 		if(op_stat != OperationStatus.SUCCESS)
 				throw new DatabaseException("");
 	}
 	
 	
 	/* set count to true if you want the number of records deleted returned */
 	public int truncate(String entity_type,boolean count) throws PersistenceException
 	{
 		_store_locker.enterLockerThread();
 		try{
 			logger.debug("truncate(String, boolean) - TUNCATE ENTITY " + entity_type);
 			return do_truncate_entity(entity_type,count);
 		}catch(PersistenceException pe)
 		{
 			throw pe;
 		}
 		finally
 		{
 			_store_locker.exitLockerThread();	
 		}		
 		
 	}
 	
 	private int do_truncate_entity(String entity,boolean count_records) throws PersistenceException
 	{
 		BDBPrimaryIndex pidx = entity_primary_indexes_as_map.get(entity);
 		if(pidx == null)
 			throw new PersistenceException("TRUNCATE ENTITY: entity "+entity+" does not exist");
 			
 			int n = -1;
 			try {
 				n = pidx.truncate(null,count_records);
 				List<BDBSecondaryIndex> lsec_indexes = entity_secondary_indexes_as_list.get(entity);
 				if(lsec_indexes != null)
 				{
 					BDBSecondaryIndex sidx = null;
 					for(int i = 0; i < lsec_indexes.size();i++)
 					{
 						sidx = lsec_indexes.get(i);
 						sidx.truncate(null);
 					}
 						
 				}
 				return n;
 			} catch (DatabaseException e) {
 				throw new PersistenceException("TRUNCATE FAILED FOR "+entity);
 			}
 			
 	}
 	
 	private void move_entity_indexes(EntityDefinition old_entity_def,EntityDefinition new_entity_def) throws PersistenceException
 	{
 		String old_entity_name = old_entity_def.getName();
 		String new_entity_name = new_entity_def.getName();
 		
 		List<BDBSecondaryIndex> lsec_indexes = entity_secondary_indexes_as_list.remove(old_entity_name);
 		if(lsec_indexes != null)
 		{
 			for(int i = 0; i < lsec_indexes.size();i++)
 			{
 				try{
 					BDBSecondaryIndex s_idx = lsec_indexes.get(i);
 					s_idx.primaryIndexNameChanged(old_entity_name,new_entity_name);
 					
 					EntityIndex idx = s_idx.getEntityIndex();
 					delete_entity_index_from_db(old_entity_name, idx);
 					idx.setEntity(new_entity_name);
 					add_entity_index_to_db(new_entity_name, idx);
 				
 				}catch(DatabaseException de)
 				{
 					logger.error("move_entity_indexes(EntityDefinition, EntityDefinition)", de);
 					throw new PersistenceException("FAILED MOVING ENTITY INDEXES FROM "+old_entity_name+" TO "+new_entity_name);
 				}
 			}
 			entity_secondary_indexes_as_list.put(new_entity_name,lsec_indexes);
 		}
 		
 		Map<String,BDBSecondaryIndex> sec_indexes = entity_secondary_indexes_as_map.remove(old_entity_name);
 		if(sec_indexes != null)
 			entity_secondary_indexes_as_map.put(new_entity_name,sec_indexes);
 		else
 		{
 			throw new PersistenceException("UNABLE TO GET INDEXES FROM MAP FOR ENTITY NAME "+old_entity_name);
 		}
 
 	}
 		
 	/****************QUERY***********************************/
 	
 	public Entity getEntityById(String type, long id) throws PersistenceException
 	{
 		_store_locker.enterAppThread();	
 		Entity e;
 		try{
 			BDBPrimaryIndex pi = entity_primary_indexes_as_map.get(type);
 			if(pi == null)
 				throw new PersistenceException("ENTITY OF TYPE "+type+" DOES NOT EXIST");
 			
 			e =  pi.getById(null,id);
 		}
 		finally
 		{
 			_store_locker.exitAppThread();
 		}
 		return e;
 	}
 	
 	public Entity getEntityById(int transaction_id,String type, long id) throws PersistenceException
 	{
 		_store_locker.enterAppThread();	
 		Entity e;
 		try{
 			BDBPrimaryIndex pi = entity_primary_indexes_as_map.get(type);
 			if(pi == null)
 				throw new PersistenceException("ENTITY OF TYPE "+type+" DOES NOT EXIST");
 			Transaction txn = get_transaction_by_transaction_id(transaction_id);
 			e =  pi.getById(txn,id);
 		}
 		finally
 		{
 			_store_locker.exitAppThread();
 		}
 		return e;
 	}
 	
 
 	public QueryResult getEntitiesOrderedById(String type, int start, long number_of_records) throws PersistenceException
 	{ 
 		_store_locker.enterAppThread();
 		try{
 			return do_get_entities_ordered_by_id(type,start,number_of_records);
 		}catch(PersistenceException pe)
 		{
 			throw pe;
 		}
 		finally
 		{
 			_store_locker.exitAppThread();
 		}
 	}
 	
 	protected QueryResult do_get_entities_ordered_by_id(String type, int start, long number_of_records) throws PersistenceException
 	{
 		BDBPrimaryIndex pidx = entity_primary_indexes_as_map.get(type);
 		if(pidx == null)
 			throw new PersistenceException("GET ENTITIES BY ID: no such entity "+type);
 		return pidx.getEntitiesOrderedById(type, start, number_of_records);
 	}
 
 	public QueryResult executeQuery(Query q) throws PersistenceException
 	{ 
 		_store_locker.enterAppThread();
 		try{
 			//return do_query_index(q);
 			com.pagesociety.persistence.Query qq = (com.pagesociety.persistence.Query)q;
 			return _query_manager.executeQuery(null,qq);
 		}catch(PersistenceException pe)
 		{
 			throw pe;
 		}
 		catch(ClassCastException cce)
 		{
 			cce.printStackTrace();
 			throw new PersistenceException("Query "+q+" is not a bdb query");
 		}
 		finally
 		{
 			_store_locker.exitAppThread();
 		}
 	}
 	
 	public QueryResult executeQuery(int transaction_id,Query q) throws PersistenceException
 	{ 
 		_store_locker.enterAppThread();
 		try{
 			//return do_query_index(q);
 			com.pagesociety.persistence.Query qq = (com.pagesociety.persistence.Query)q;
 			Transaction txn = get_transaction_by_transaction_id(transaction_id);
 			return _query_manager.executeQuery(txn,qq);
 		}catch(PersistenceException pe)
 		{
 			throw pe;
 		}
 		catch(ClassCastException cce)
 		{
 			cce.printStackTrace();
 			throw new PersistenceException("Query "+q+" is not a bdb query");
 		}
 		finally
 		{
 			_store_locker.exitAppThread();
 		}
 	}
 	
 	public QueryResult executePSSqlQuery(String pssql) throws PersistenceException
 	{ 
 		_store_locker.enterAppThread();
 		try{
 			return _query_manager.executePSSqlQuery(null,pssql);
 		}catch(PersistenceException pe)
 		{
 			throw pe;
 		}
 		finally
 		{
 			_store_locker.exitAppThread();
 		}
 	}
 	
 
 	public List<Object> getDistinctKeys(String entityname,String indexname) throws PersistenceException
 	{
 		_store_locker.enterAppThread();
 		try{
 			return do_get_distinct_keys(entityname,indexname);
 		}catch(PersistenceException pe)
 		{
 			throw pe;
 		}
 		finally
 		{
 			_store_locker.exitAppThread();
 		}	
 	}
 	
 	
 	private List<Object> do_get_distinct_keys(String entityname,String indexname) throws PersistenceException
 	{
 		BDBPrimaryIndex pidx = entity_primary_indexes_as_map.get(entityname);
 		if(pidx == null)
 			throw new PersistenceException("GET DISTINCT KEYS: no such entity "+entityname);
 		
 		BDBSecondaryIndex ei = entity_secondary_indexes_as_map.get(entityname).get(indexname);
 		if(ei == null)
 			throw new PersistenceException("GET DISTINCT KEYS: no such index "+indexname+" on entity "+entityname);
 			
 		return ei.getDistinctKeys();
 	}
 	
 	public int count(Query q) throws PersistenceException
 	{
 		_store_locker.enterAppThread();
 		try{
 			//return do_query_index(q);
 			com.pagesociety.persistence.Query qq = (com.pagesociety.persistence.Query)q;
 			return _query_manager.executeCount(null,qq);
 		}catch(PersistenceException pe)
 		{
 			throw pe;
 		}
 		catch(ClassCastException cce)
 		{
 			cce.printStackTrace();
 			throw new PersistenceException("Query "+q+" is not a bdb query");
 		}
 		finally
 		{
 			_store_locker.exitAppThread();
 		}
 	}
 	
 	public int count(int transaction_id,Query q) throws PersistenceException
 	{
 		_store_locker.enterAppThread();
 		try{
 			//return do_query_index(q);
 			com.pagesociety.persistence.Query qq = (com.pagesociety.persistence.Query)q;
 			Transaction txn = get_transaction_by_transaction_id(transaction_id);
 			return _query_manager.executeCount(txn,qq);
 		}catch(PersistenceException pe)
 		{
 			throw pe;
 		}
 		catch(ClassCastException cce)
 		{
 			cce.printStackTrace();
 			throw new PersistenceException("Query "+q+" is not a bdb query");
 		}
 		finally
 		{
 			_store_locker.exitAppThread();
 		}
 	}
 	
 
 
 	public void fillReferenceFields(List<Entity> es) throws PersistenceException
 	{
 		_store_locker.enterAppThread();
 		try{	
 			if(es.size() == 0)
 				return;
 			Entity e = es.get(0);
 			EntityDefinition ed = getEntityDefinitionProvider().provideEntityDefinition(e);
 			int s = es.size();
 
 			for(FieldDefinition fd:ed.getFields())
 			{
 				if(fd.getBaseType() != Types.TYPE_REFERENCE)
 					continue;
 
 				for (int i = 0;i < s;i++)
 					do_fill_reference_field(null,es.get(i),fd);    
 
 			}
 		}catch(PersistenceException pe)
 		{
 			throw pe;
 		}
 		finally
 		{
 			_store_locker.exitAppThread();
 		}		
 	}
 //assume homogenous list of references you want filled//
 	public void fillReferenceFields(int transaction_id,List<Entity> es) throws PersistenceException
 	{
 		_store_locker.enterAppThread();
 		try{
 			int s = es.size();
 			if(s == 0)
 				return;
 			Transaction txn = get_transaction_by_transaction_id(transaction_id);
 			Entity e = es.get(0);
 			EntityDefinition ed = getEntityDefinitionProvider().provideEntityDefinition(e);
 		
 			for(FieldDefinition fd:ed.getFields())
 			{
 				if(fd.getBaseType() != Types.TYPE_REFERENCE)
 					continue;
 
 				for (int i = 0;i < s;i++)
 					do_fill_reference_field(txn,es.get(i),fd);    
 
 			}
 		}catch(PersistenceException pe)
 		{
 			throw pe;
 		}
 		finally
 		{
 			_store_locker.exitAppThread();
 		}		
 	}
 	
 	public void fillReferenceField(List<Entity> es,String fieldname) throws PersistenceException
 	{
 		_store_locker.enterAppThread();
 		try{
 			int s = es.size();			
 			if(s == 0)
 				return;
 			Entity e = es.get(0);
 			EntityDefinition ed = getEntityDefinitionProvider().provideEntityDefinition(e);
 			FieldDefinition fd = ed.getField(fieldname);
 
 			for (int i = 0;i < s;i++)
 				do_fill_reference_field(null,es.get(i),fd);    
 		}catch(PersistenceException pe)
 		{
 			throw pe;
 		}
 		finally
 		{
 			_store_locker.exitAppThread();
 		}	
 	}
 
 	public void fillReferenceField(int transaction_id,List<Entity> es,String fieldname) throws PersistenceException
 	{
 		_store_locker.enterAppThread();
 		try{
 			int s = es.size();			
 			if(s == 0)
 				return;
 			Transaction txn = get_transaction_by_transaction_id(transaction_id);
 			Entity e = es.get(0);
 			EntityDefinition ed = getEntityDefinitionProvider().provideEntityDefinition(e);
 			FieldDefinition fd = ed.getField(fieldname);
 	
 	
 				for (int i = 0;i < s;i++)
 					do_fill_reference_field(txn,es.get(i),fd);    
 			}catch(PersistenceException pe)
 			{
 				throw pe;
 			}
 		finally
 		{
 			_store_locker.exitAppThread();
 		}	
 	}
 
 	
 
 	public void fillReferenceFields(Entity e) throws PersistenceException
 	{
 		_store_locker.enterAppThread();
 
 	    try{
 			EntityDefinition ed = getEntityDefinitionProvider().provideEntityDefinition(e);
 	    	for (FieldDefinition f : ed.getFields())
 		    {
 		    	if(f.getBaseType() != Types.TYPE_REFERENCE)
 		    		continue;
 		    	do_fill_reference_field(null,e, f);
 		    }  
 		}catch(PersistenceException pe)
 		{
 			throw pe;
 		}
 		finally
 		{
 			_store_locker.exitAppThread();
 		}		
 	}
 
 	public void fillReferenceFields(int transaction_id,Entity e) throws PersistenceException
 	{
 		_store_locker.enterAppThread();
 
 	    try{
 	    	Transaction txn = get_transaction_by_transaction_id(transaction_id);
 			EntityDefinition ed = getEntityDefinitionProvider().provideEntityDefinition(e);
 	    	for (FieldDefinition f : ed.getFields())
 		    {
 		    	if(f.getBaseType() != Types.TYPE_REFERENCE)
 		    		continue;
 		    	do_fill_reference_field(txn,e, f);
 		    }  
 		}catch(PersistenceException pe)
 		{
 			throw pe;
 		}
 		finally
 		{
 			_store_locker.exitAppThread();
 		}		
 	}
 
 	public void fillReferenceField(Entity e,String fieldname) throws PersistenceException
 	{
 		_store_locker.enterAppThread();
 
 	    try{
 			FieldDefinition fd = getEntityDefinitionProvider().provideEntityDefinition(e).getField(fieldname);
 			do_fill_reference_field(null,e, fd);   
 		}catch(PersistenceException pe)
 		{
 			throw pe;
 		}
 		finally
 		{
 			_store_locker.exitAppThread();
 		}	
 	}
 
 	public void fillReferenceField(int transaction_id,Entity e,String fieldname) throws PersistenceException
 	{
 		_store_locker.enterAppThread();
 
 	    try{
 	    	Transaction txn = get_transaction_by_transaction_id(transaction_id);
 	    	FieldDefinition fd = getEntityDefinitionProvider().provideEntityDefinition(e).getField(fieldname);
 			do_fill_reference_field(txn,e, fd);   
 		}catch(PersistenceException pe)
 		{
 			throw pe;
 		}
 		finally
 		{
 			_store_locker.exitAppThread();
 		}	
 	}
 
 	@SuppressWarnings("unchecked")
 	private void do_fill_reference_field(Transaction parent_txn,Entity e, FieldDefinition f) throws PersistenceException
 	{
 		if (f == null || f.getBaseType() != Types.TYPE_REFERENCE)
            throw new PersistenceException("REF FIELD CANT BE FILLED.IT IS EITHER DOES NOT EXIST IN ENTITY OR IS NOT A REFERNCE TYPE");
 
        String type = f.getReferenceType();
        if(type.equals(FieldDefinition.REF_TYPE_UNTYPED_ENTITY))
     	   do_fill_untyped_reference_field(parent_txn,e, f);
        else
     	   do_fill_typed_reference_field(parent_txn,e, f);
 
    }
 	
 	private void do_fill_typed_reference_field(Transaction parent_txn,Entity e, FieldDefinition f) throws PersistenceException
 	{
        BDBPrimaryIndex ref_pidx = entity_primary_indexes_as_map.get(f.getReferenceType());
        if (ref_pidx==null)
     	   throw new PersistenceException("STORE DOES NOT RECOGNIZE REFERENCE TYPE "+f.getReferenceType());
 
 	       if (f.isArray())
 	       {
 	           List<Entity> refs = (List<Entity>) e.getAttribute(f.getName());
 	           if (refs == null)
 	               return;
 	           
 	           List<Entity> filled_refs = new ArrayList<Entity>();
 	           for (int i=0; i<refs.size(); i++)
 	           {
 	              Entity r = refs.get(i);
 	              if(r == null)
 	              {
 	            	  filled_refs.add(null);
 	              }
 	              else
 	              {
 	            	  Entity r1 = ref_pidx.getById(parent_txn,r.getId());
 	            	  if (r1 != null) // if a list is refering to a reference that doesnt exist, we add the lightweight version only
 	            		  filled_refs.add(r1);
 	            	  else
 	            	  {
 	            		  filled_refs.add(r);
 	            		  logger.error("Data Integrity error: "+r.getType()+" "+r.getId()+
 	            			  " does not exist in db (parent="+e.getType()+" "+e.getId()+" field="+f.getName());
 	            	  }
 	              }
 	            }
 	           e.getAttributes().put(f.getName(), filled_refs);
 	       }
 	       else
 	       {
 	           Entity ref = (Entity) e.getAttribute(f.getName());
 	           if (ref == null)
 	               return;
 	           e.getAttributes().put(f.getName(),ref_pidx.getById(parent_txn,ref.getId()));
 	       }
    }
 
 	private void do_fill_untyped_reference_field(Transaction parent_txn,Entity e, FieldDefinition f) throws PersistenceException
 	{
 		BDBPrimaryIndex ref_pidx = null;
 
        if (f.isArray())
        {
            List<Entity> refs = (List<Entity>) e.getAttribute(f.getName());
            if (refs == null || refs.size() == 0)
                return;
            
            List<Entity> filled_refs = new ArrayList<Entity>();
            Entity r = null;
            for (int i=0; i<refs.size(); i++)
            {
               r = refs.get(i);
               if(r == null)
             	  filled_refs.add(null);
               else
               {
             	  /* here we need to look up this things per entity since this will probably
             	   * be a non homgenous list of entities */
                   ref_pidx = entity_primary_indexes_as_map.get(r.getType());
                   if (ref_pidx==null)
                	   throw new PersistenceException("STORE DOES NOT RECOGNIZE REFERENCE TYPE "+f.getReferenceType());
             	  filled_refs.add(ref_pidx.getById(parent_txn,r.getId()));
               }
              }
             e.getAttributes().put(f.getName(), filled_refs);
        }
        else
        {
     	   Entity ref = (Entity) e.getAttribute(f.getName());
            if (ref == null)
                return;
            ref_pidx = entity_primary_indexes_as_map.get(ref.getType());
            if (ref_pidx==null)
         	   throw new PersistenceException("STORE DOES NOT RECOGNIZE REFERENCE TYPE "+ref.getType()+" "+f.getReferenceType());
            e.getAttributes().put(f.getName(),ref_pidx.getById(parent_txn,ref.getId()));
        }
 
    }
 
 	
 	
 	private void clean_query_cache(String entity_name)
 	{
 		_query_manager.cleanCache(entity_name);
 	}
 	
 	private void calculate_query_cache_dependencies(EntityDefinition def)
 	{
 		_query_manager.calculateCacheDependencies(def);
 	}
 	
 	private void remove_query_cache_dependencies(EntityDefinition def)
 	{
 		_query_manager.removeCacheDependencies(def);
 	}
 	
 	//////////////////////////////
 	//QUEUE FUNCTIONS
 	//////////////////////////////
 	
 	
 	public String createQueue(String name,int record_size,int num_records_in_extent) throws PersistenceException
 	{
 		_store_locker.enterLockerThread();
 		try{
 			
 			return _queue_manager.createQueue(name, record_size, num_records_in_extent);
 	
 		}catch(PersistenceException pe)
 		{
 			throw pe;
 		}
 		finally
 		{
 			_store_locker.exitLockerThread();	
 		}
 	}
 
 	public void deleteQueue(String name) throws PersistenceException
 	{
 		_store_locker.enterLockerThread();
 		try{
 			_queue_manager.deleteQueue(name);
 		}catch(PersistenceException pe)
 		{
 			throw pe;
 		}
 		finally
 		{
 			_store_locker.exitLockerThread();
 		}
 	}
 	
 	public void enqueue(String queue_name,byte[] queue_item,boolean durable_commit) throws PersistenceException
 	{
 		_store_locker.enterAppThread();
 		try
 		{
 			_queue_manager.enqueue(null, queue_name, queue_item,durable_commit);
 		}catch(PersistenceException pe)
 		{
 			throw pe;
 		}
 		finally
 		{
 			_store_locker.exitAppThread();
 		}
 	}
 	
 	public void enqueue(int transaction_id,String queue_name,byte[] queue_item,boolean durable_commit) throws PersistenceException
 	{
 		_store_locker.enterAppThread();
 		try
 		{
 			Transaction txn = get_transaction_by_transaction_id(transaction_id);
 			_queue_manager.enqueue(txn, queue_name, queue_item,durable_commit);
 		}catch(PersistenceException pe)
 		{
 			throw pe;
 		}
 		finally
 		{
 			_store_locker.exitAppThread();
 		}
 	}
 	
 	
 	// TODO even though we add variable length byte arrays in enqueue,
 	// dequeue returns fixed length (128) padded with 32
 	public byte[] dequeue(String queue_name,boolean durable_commit,boolean block) throws PersistenceException
 	{
 		_store_locker.enterAppThread();
 		try{
 			return _queue_manager.dequeue(null, queue_name,durable_commit,block);
 		}catch(PersistenceException pe)
 		{
 			throw pe;
 		}
 		finally
 		{
 			_store_locker.exitAppThread();
 		}
 	}
 
 	// TODO even though we add variable length byte arrays in enqueue,
 	// dequeue returns fixed length (128) padded with 32
 	public byte[] dequeue(int transaction_id,String queue_name,boolean durable_commit,boolean block) throws PersistenceException
 	{
 		_store_locker.enterAppThread();
 		try{
 			Transaction txn = get_transaction_by_transaction_id(transaction_id);
 			return _queue_manager.dequeue(txn, queue_name,durable_commit,block);
 		}catch(PersistenceException pe)
 		{
 			throw pe;
 		}
 		finally
 		{
 			_store_locker.exitAppThread();
 		}
 	}
 
 	public List<String> listQueues() throws PersistenceException
 	{
 		_store_locker.enterAppThread();
 		try{
 			return _queue_manager.listQueues();
 		}catch(PersistenceException pe)
 		{
 			throw pe;
 		}
 		finally
 		{
 			_store_locker.exitAppThread();
 		}
 		
 	}
 	
 	//////
 	// END QUEUE FUNCTIONS
 	//
 	
 	private void close_cursors(Cursor... cursors)
 	{
 		try
 		{
 			for (Cursor c : cursors)
 			{
 				if (c != null)
 				{
 					c.close();
 				}
 			}
 		}
 		catch (DatabaseException e)
 		{
 			logger.error("close_cursors(Cursor)", e);
 		}
 	}
 
 
 	private void abortTxn(Transaction txn)
 	{
 		if (txn != null)
 		{
 			try
 			{
 				txn.abort();
 			}
 			catch (DatabaseException e)
 			{
 				e.printStackTrace();
 				logger.error("abortTxn(Transaction)", e);
 			}
 			txn = null;
 		}
 	}
 	public Environment getEnvironment()
 	{
 		return environment;
 	}
 	////////////////////////////
 	///	STATS
 	///////////////////////////	
 	
 	public String getStatistics()
 	{
 		StringBuilder buf = new StringBuilder();
 		buf.append(getTransactionStatistics()+"\n\n");
 		buf.append(getLockStatistics()+"\n\n");
 		buf.append(getMutexStatistics()+"\n\n");
 		buf.append(getLogStatistics()+"\n\n");
 		buf.append(getCacheStatistics()+"\n\n");
 		buf.append(getCacheFileStatistics()+"\n\n");
 		return buf.toString();
 	}
 
 	public String getTransactionStatistics()
 	{
 		try{
 			return (environment.getTransactionStats((null)).toString());
 		}catch(DatabaseException dbe)
 		{
 			logger.error(dbe);
 			return "DB Exception Getting Transaction Statistics";
 		}
 	}
 
 	public String getCacheStatistics()
 	{
 		try{
 			return (environment.getCacheStats((null)).toString());
 		}catch(DatabaseException dbe)
 		{
 			logger.error(dbe);
 			return "DB Exception Getting Cache Statistics";
 		}
 	}
 
 	public String getCacheFileStatistics()
 	{
 		StringBuilder b = new StringBuilder();
 		try{
 			CacheFileStats[] ff = environment.getCacheFileStats(null);
 			for(int i= 0;i < ff.length;i++)
 			{
 				b.append(ff[i].toString()+"\n");
 			}
 			return b.toString();
 		}catch(DatabaseException dbe)
 		{
 			logger.error(dbe);
 			return "DB Exception Getting Cache Statistics";
 		}
 	}
 
 	public String getLockStatistics()
 	{
 		try{
 		return (environment.getLockStats(null)).toString();
 		}catch(DatabaseException dbe)
 		{
 			logger.error(dbe);
 			return "DB Exception Getting Lock Statistics";
 		}
 	}
 	
 	public String getLogStatistics()
 	{
 		try{
 		return (environment.getLogStats(null)).toString();
 		}catch(DatabaseException dbe)
 		{
 			logger.error(dbe);
 			return "DB Exception Getting Log Statistics";
 		}
 	}
 	
 	public String getMutexStatistics()
 	{
 		try{
 			return (environment.getMutexStats(null)).toString();
 		}catch(DatabaseException dbe)
 		{
 			logger.error(dbe);
 			return "DB Exception Getting Log Statistics";
 		}
 	}
 	
 	
 
 	////BACKUP SUBSYSTEM/////////////////////////////////////////////////////////////
 	//
 	//
 	//
 	/////////////////////////////////////////////////////////////////////////////////////
 	
 	
 	public boolean supportsFullBackup() throws PersistenceException
 	{
 		return backup_root_directory != null;
 	}
 	
 	public boolean supportsIncrementalBackup() throws PersistenceException
 	{
 		return backup_root_directory != null;
 	}
 
 	public String doFullBackup() throws PersistenceException
 	{
 		if(!supportsFullBackup())
 			throw new PersistenceException("FULLBACKUP NOT SETUP.SET store-backup-dir PARAM.");
 		try{
 			String backup_filename = get_backup_filename();
 			logger.debug("F U L L   B A C K U P  TO "+backup_root_directory.getAbsolutePath()+File.separator+get_backup_filename() );
 			return do_full_backup(backup_filename);	
 		}catch(PersistenceException pe)
 		{
 			throw pe;
 		}
 	}
 	
 	public int removeUnusedLogFiles() throws PersistenceException
 	{
 		try{
 			logger.debug("R E M O V E   U N U S E D   L O G   F I L E S " );
 			return delete_unused_log_files();	
 		}catch(PersistenceException pe)
 		{
 			throw pe;
 		}
 	}
 	
 	
 	private File[] get_archive_databases() throws PersistenceException
 	{
 		//TODO:
 		//this should be in a method or a local variable
 		String path = (String)_config.get(BDBStoreConfigKeyValues.KEY_STORE_ROOT_DIRECTORY);
 		File db_env_home = null;
 		File db_env_root = new File(path);
 		_db_env_props_file = new File(db_env_root, BDBConstants.ENVIRONMENT_PROPERTIES_FILE_NAME);
 		_db_env_props = new Properties();
 		try 
 		{
 			_db_env_props.load(new FileInputStream(_db_env_props_file));
             db_env_home = new File(db_env_root, _db_env_props.getProperty(BDBConstants.KEY_ACTIVE_ENVIRONMENT));
         } 
 		catch (Exception e) 
 		{
 			throw new PersistenceException("FAILED FINDING ACTIVE ENVIRONMENT IN BACKUP", e);
         }
 		
 		File[] ff 			= db_env_home.listFiles();
 		List<File> f_ret 	= new ArrayList<File>(); 
 		for(int i = 0;i < ff.length;i++)
 		{
 			File f = ff[i];
 			if(f.getAbsolutePath().endsWith(".db"))
 				f_ret.add(f);
 		}
 		File[] fff = new File[f_ret.size()];
 		for(int i = 0;i < fff.length;i++)
 		{
 			File bf = f_ret.get(i);
 			//System.out.println("\tADDING DB "+bf.getAbsolutePath()+" TO BACKUP.");
 			fff[i] = bf; 
 		}
 		
 		return fff;
 	}
 	
 	
 	public String do_full_backup(String backup_filename) throws PersistenceException
 	{
 		lock();
 		try{
 			File backup_dir = new File(backup_root_directory,backup_filename);	
 			backup_dir.mkdir();
 			do_checkpoint();
 			//environment.resetLogSequenceNumber(filename, encrypted)
 
 			//there is a bug in bdb where this is failing in postera. i
 			//am almost certain it relates to the queue db type used
 			//in the s3 module
 			//File[] archive_dbs = environment.getArchiveDatabases();
 			File[] archive_dbs = get_archive_databases();
 			for (int i=0; i<archive_dbs.length; i++)
 				copy(archive_dbs[i], backup_dir);
 
 			File[] unneeded_archive_logs = environment.getArchiveLogFiles(false);
 			for (int i=0; i<unneeded_archive_logs.length; i++)
 				unneeded_archive_logs[i].delete();
 			
 			File[] archive_logs = environment.getArchiveLogFiles(true);
 			
 			for (int i=0; i<archive_logs.length; i++)
 				copy(archive_logs[i], backup_dir);
 
 			//TODO: dont know if we need to do this here//
 			logger.debug("\tRUNNING CATASTROPHIC RECOVERY ON "+backup_dir);
 			EnvironmentConfig env_cfg = get_run_recovery_config(true);
 			Environment recovered_env = new Environment(backup_dir,env_cfg);
 			recovered_env.close();
 			File[] ff = backup_dir.listFiles();
 			for(int i = 0;i < ff.length;i++)
 			{
 				File f = ff[i];
 				if(f.getName().startsWith("__db"))
 					f.delete();
 			}
 			
 			return backup_dir.getAbsolutePath();
 			
 		}catch(Exception e)
 		{
 			e.printStackTrace();
 			throw new PersistenceException("FAILED DURING FULL BACKUP");		
 		}
 		finally
 		{
 			unlock();
 		}
 	}
 	
 	
 	public String get_backup_filename()
 	{
 	    Format formatter = new SimpleDateFormat("yyyyMMdd_HHmmss");
 	    Date now = new Date();
 	    return formatter.format(now);
 	}
 	
 
 	public String doIncrementalBackup(String full_backup_token) throws PersistenceException
 	{
 		if(!supportsFullBackup())
 			throw new PersistenceException("INCREMENTAL BACKUP NOT SETUP.SET store-backup-dir PARAM.");
 		//dont think we need to do a lock here
 		File backup_dir = new File((String)full_backup_token);
 		try{
 			logger.debug("I N C R E M E N T A L   B A C K U P   TO "+backup_dir);
 			do_checkpoint();
 			File[] unneeded_archive_logs = environment.getArchiveLogFiles(false);
 			for (int i=0; i<unneeded_archive_logs.length; i++)
 				unneeded_archive_logs[i].delete();
 			
 			File[] archive_logs = environment.getArchiveLogFiles(true);
 			for (int i=0; i<archive_logs.length; i++)
 				copy(archive_logs[i], backup_dir);
 	
 
 			//RUN CATASTROPHIC RECOVERY ON DATABASE ENVIRONMENT//
 			//TODO: pretty sure we need to do this here//
 			//logger.debug("\tRUNNING CATASTROPHIC RECOVERY ON "+backup_dir);
 			//EnvironmentConfig env_cfg = get_run_recovery_config(true);
 			//Environment recovered_env = new Environment(backup_dir,env_cfg);
 			//recovered_env.close();
 			return full_backup_token;
 		}catch(Exception e)
 		{
 			e.printStackTrace();
 			throw new PersistenceException("FAILED DURING INCREMENTAL BACKUP");		
 		}
 	
 	}
 	
 
 	public String[] getBackupIdentifiers() throws PersistenceException
 	{
 		File[] backups = backup_root_directory.listFiles();	
 		String[] ss = new String[backups.length];
 		for(int i = 0;i< ss.length;i++)
 			ss[i]=backups[i].getAbsolutePath();
 		return ss;
 	}
 
 	
 
 	public void restoreFromBackup(String backup_token) throws PersistenceException
 	{
 		_store_locker.enterLockerThread();
 		try{
 			logger.debug("R E S T O R E  FROM "+backup_token);
 			File backup_dir 	   = new File((String)backup_token);
 			String backup_dir_name = backup_dir.getName(); 
 			String store_root = (String)_config.get(BDBStoreConfigKeyValues.KEY_STORE_ROOT_DIRECTORY);
 			File dest_dir = new File(store_root,backup_dir_name);
 			logger.debug("\tCOPYING "+backup_dir+" TO "+dest_dir.getAbsolutePath());
 			
 			copyDirectory(backup_dir,dest_dir );
 			System.out.println("SWITCHING ENVIRONMENT TO "+backup_dir_name);
 			use_environment(backup_dir_name);
 		}catch(Exception e)
 		{
 			e.printStackTrace();
 			throw new PersistenceException("RESTORE FROM BACKUP FAILED ");
 		}
 		finally
 		{
 			logger.debug("RESTORE FROM BACKUP  - LOCKER THREAD IS EXITING");
 			_store_locker.exitLockerThread();	
 		}	
 		logger.debug("RESTORE SUCCESSFUL");
 	}
 	
 	public File getBackupAsZipFile(String backup_identifier) throws PersistenceException
 	{
 		try{
 			String shortname = backup_identifier.substring(backup_identifier.lastIndexOf(File.separatorChar)+1)+new Random().nextInt();
 			File f = new File(System.getProperty("java.io.tmpdir"),shortname+".zip");
 			return zip_dir(f,backup_identifier);
 		}catch(Exception e)
 		{
 			throw new PersistenceException(e.getMessage());
 		}
 	}
 	
 	 private static File zip_dir(File zipFile, String dir) throws PersistenceException
 	    {
 	        File dirObj = new File(dir);
 	        if(!dirObj.isDirectory())
 	           throw new PersistenceException(dir + " is not a directory");
 
 	        try
 	        {
 	            ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipFile));
 	            System.out.println("Creating : " + zipFile.getAbsolutePath());
 	            add_zip_dir(dirObj, out);
 	            // Complete the ZIP file
 	            out.close();
 	        }
 	        catch (IOException e)
 	        {
 	            e.printStackTrace();
 	            System.exit(1);
 	        }
 	        return zipFile;
 	    }
 
 	    private static void add_zip_dir(File dirObj, ZipOutputStream out) throws IOException
 	    {
 	        File[] files = dirObj.listFiles();
 	        byte[] tmpBuf = new byte[1024];
 
 	        for (int i=0; i<files.length; i++)
 	        {
 	            if(files[i].isDirectory())
 	            {
 	                add_zip_dir(files[i], out);
 	                continue;
 	            }
 
 	            FileInputStream in = new FileInputStream(files[i].getAbsolutePath());
 	            System.out.println(" Adding: " + files[i].getAbsolutePath());
 
 	            out.putNextEntry(new ZipEntry(files[i].getAbsolutePath()));
 
 	            // Transfer from the file to the ZIP file
 	            int len;
 	            while((len = in.read(tmpBuf)) > 0)
 	            {
 	                out.write(tmpBuf, 0, len);
 	            }
 
 	            // Complete the entry
 	            out.closeEntry();
 	            in.close();
 	        }
 	    }
 
 	public void deleteBackup(String backup_token) throws PersistenceException
 	{
 
 		try{
 			logger.debug("D E L E T I N G   B A C K U P "+backup_token);
 			File b = new File((String)backup_token);
 			delete_dir(b);
 		}catch(Exception e)
 		{
 			e.printStackTrace();
 			throw new PersistenceException("DELETE BACKUP "+backup_token+" FAILED");
 		}
 		finally
 		{
 			logger.debug("deleteBackup(String backup_token) - LOCKER THREAD IS EXITING");
 		}	
 	}
 	
 	// Deletes all files and subdirectories under dir.
 	// Returns true if all deletions were successful.
 	// If a deletion fails, the method stops attempting to delete and returns false.
 	private static boolean delete_dir(File dir) 
 	{
 		if (dir.isDirectory())
 		{
 			String[] children = dir.list();
 				for (int i=0; i<children.length; i++)
 				{
 					boolean success = delete_dir(new File(dir, children[i]));
 					if (!success) 
 					{
 						return false;
 					}
 				}
 		}
 		// The directory is now empty so delete it
 		return dir.delete();
 	} 
 	
 	
 	public int delete_unused_log_files() throws PersistenceException
 	{
 		lock();
 		try{
 			do_checkpoint();
 
 			File[] unneeded_archive_logs = environment.getArchiveLogFiles(false);
 			for (int i=0; i<unneeded_archive_logs.length; i++)
 				unneeded_archive_logs[i].delete();
 			return unneeded_archive_logs.length;
 			
 		}catch(Exception e)
 		{
 			e.printStackTrace();
 			throw new PersistenceException("FAILED DURING FULL BACKUP");		
 		}
 		finally
 		{
 			unlock();
 		}
 	}
 	
 
 	//TODO: WE MIGHT INTRODUCE A TYPE PARAM HERE AT SOME POINT  TYPE_BASIC_FILE,TYPE_ZIP_FILE,TYPE_S3,TYPE_REMOTE etc
 	//at which point the backup subsystem becomes some interface that you can set instances of for the store IBDBBackupManager
 	//and all this crap can move there
 	public void setBackupRootIdentifier(String s) throws PersistenceException
 	{
 		File f = new File(s);
 		if(!f.exists())
 			throw new PersistenceException("BACKUP DIRECTORY "+f.getAbsolutePath()+" DOES NOT EXIST");
 		if(!f.isDirectory())
 			throw new PersistenceException("BACKUP DIRECTORY "+f.getAbsolutePath()+" IS NOT A DIRECTORY");
 		backup_root_directory = f;
 	}
 
 
 	private void use_environment(String environment_name) throws PersistenceException
 	{
 		
 		try{
 			do_close();
 			set_active_env_prop(environment_name);
 			init(_config);
 		}catch(PersistenceException pe)
 		{
 			pe.printStackTrace();
 			throw pe;
 		}
 	}	
 	
 	private void set_active_env_prop(String relative_path) throws PersistenceException
 	{
 		System.out.println("SETTING ACTIVE ENV TO "+relative_path);
 		_db_env_props.put(BDBConstants.KEY_ACTIVE_ENVIRONMENT, relative_path);
     	try
 		{
     		_db_env_props.store(new FileOutputStream(_db_env_props_file), "PS PERSISTENCE BERKELEY DB ENVIRONMENT PROPERTIES");
 		}
 		catch (Exception e1)
 		{
 			e1.printStackTrace();
 			throw new PersistenceException("SELECT ENVIRONMENT ERROR - CANNOT INITIALIZE PROPERTIES FILE IN ENV ROOT",e1);
 		}
 	}
 
 
 		
 	//http://www.javalobby.org/java/forums/t17036.html
 	private void copy(File file, File destination_directory) throws PersistenceException
 	{
 		FileChannel ic = null;
 		FileChannel oc =  null;
 		try
 		{
 			ic = new FileInputStream(file).getChannel();
 			oc = new FileOutputStream(new File(destination_directory, file.getName())).getChannel();
 			ic.transferTo(0, ic.size(), oc);
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 			throw new PersistenceException("Cant make archive copy!",e);
 		}
 		finally
 		{
 			try
 			{
 				if (ic!=null)
 					ic.close();
 				if (oc!=null)
 					oc.close();
 			} 
 			catch (Exception e)
 			{
 				e.printStackTrace();
 				throw new PersistenceException("Cant make archive copy!",e);
 			}
 		}
 
 	}
 	
     public void copyDirectory(File sourceLocation , File targetLocation)throws IOException {
         
         if (sourceLocation.isDirectory()) {
             if (!targetLocation.exists()) {
                 targetLocation.mkdir();
             }
             
             String[] children = sourceLocation.list();
             for (int i=0; i<children.length; i++) {
                 copyDirectory(new File(sourceLocation, children[i]),
                         new File(targetLocation, children[i]));
             }
         } else {
             
             InputStream in = new FileInputStream(sourceLocation);
             OutputStream out = new FileOutputStream(targetLocation);
             
             // Copy the bits from instream to outstream
             byte[] buf = new byte[1024];
             int len;
             while ((len = in.read(buf)) > 0) {
                 out.write(buf, 0, len);
             }
             in.close();
             out.close();
         }
     }
 
 }
