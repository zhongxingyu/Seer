 package edu.common.dynamicextensions.util;
 
 import java.util.List;
 import java.util.Map;
 
 import edu.common.dynamicextensions.domain.DomainObjectFactory;
 import edu.common.dynamicextensions.domain.Entity;
 import edu.common.dynamicextensions.domain.EntityGroup;
 import edu.common.dynamicextensions.domain.databaseproperties.TableProperties;
 import edu.common.dynamicextensions.domaininterface.EntityInterface;
 import edu.common.dynamicextensions.exception.DynamicExtensionsSystemException;
 import edu.common.dynamicextensions.util.global.Constants;
 import edu.wustl.common.beans.SessionDataBean;
 import edu.wustl.common.dao.DAOFactory;
 import edu.wustl.common.dao.HibernateDAO;
 import edu.wustl.common.dao.JDBCDAO;
 import edu.wustl.common.security.exceptions.UserNotAuthorizedException;
 import edu.wustl.common.util.dbManager.DAOException;
 import edu.wustl.common.util.logger.Logger;
 
 /**
  * This is a singleton class that manages operations related to dynamic entity creation,attributes creation,
  * adding data into them and retrieving data from them.
  * @author geetika_bangard
  */
 public class EntityManager {
 	
 	/**
 	 * Static instance of the entity manager.
 	 */
 	private static EntityManager entityManager = null;
 	
 	/**
 	 * Empty Constructor.
 	 */
 	protected EntityManager(){
 	}
 	
 	/**
 	 * Returns the instance of the Entity Manager.
 	 * @return entityManager singleton instance of the Entity Manager.
 	 */
 	public static synchronized EntityManager getInstance(){
 		if(entityManager == null){
 			entityManager = new EntityManager();
 		}
 		return entityManager;
 	}
 	
 	//------------------ Methods related to Entity---------------------------
 	/**
 	 * Creates an Entity with the given entity information.Entity is registered in the metadata and a table is created
 	 * to store the records.
 	 * @param entity the entity to be created.
 	 * @throws DAOException
 	 */
	public EntityInterface createEntity(Entity entity) throws DynamicExtensionsSystemException{
 		HibernateDAO hibernateDAO = (HibernateDAO)DAOFactory.getDAO(Constants.HIBERNATE_DAO);
 		try {
 			if(entityInterface == null ) {
 				throw new DAOException("while createEntity : Entity is Null");
 			}
 			hibernateDAO.openSession(null);
 			hibernateDAO.insert(entity,null,false,false);
 			DomainObjectFactory domainObjectFactory = DomainObjectFactory.getInstance();
 			TableProperties tablePropertiesToSave = (TableProperties) domainObjectFactory.createTableProperties();
 			String tableName = getEntityTableName(entity);
 			tablePropertiesToSave.setName(tableName);
 			entity.setTableProperties(tablePropertiesToSave);
 			
 			hibernateDAO.update(entity,null,false,false,false);
 			hibernateDAO.commit();
 			String entityName = entity.getName();
 			
 			TableProperties tableProperties = entity.getTableProperties();
 			String entityTableName = tableProperties.getName();
 			
 			//Query to create the table with the identifier
 			StringBuffer query = new  StringBuffer("CREATE TABLE "+ entityTableName + "( IDENTIFIER number(19,0) not null, "); 
 			query = query.append("primary key (IDENTIFIER))");
 			Logger.out.debug("[createEntity]Query formed is: "+query.toString());
 			
 			QueryInterfaceManager.fireQuery(query.toString(),"while creating an entity table for "+entityName);
 			//Creating sequence for the table formed.
 			
 			StringBuffer queryToGenerateSeq = new StringBuffer("CREATE SEQUENCE EAV_ENTITY" + entity.getId().toString()+"_SEQ START WITH 1 INCREMENT BY 1 MINVALUE 1");
 			Logger.out.debug("[createEntity -- sequence query ]Query formed is: "+queryToGenerateSeq.toString());
 			QueryInterfaceManager.fireQuery(queryToGenerateSeq.toString(),"while creating a sequence for entity "+entityName);
 			
 			//Entering metadata in tables related to entity that will be used for search
 			
 			//TODO UNCOMMENT LATER
 			//            QueryInterfaceManager.insertMetdataOnCreateEntity(entity);
 			
 			 Entity savedEntity = null;
 			Long entityId = entity.getId();
 			if(entityId!= null){
 			     savedEntity =  (Entity) hibernateDAO.retrieve(Entity.class.getName(), entityId);
 			}
 			
 			return savedEntity;
 		} catch (DAOException daoException){
 			daoException.printStackTrace();
 			
 			try
 			{  
 				hibernateDAO.rollback();
 			}
 			catch(DAOException daoEx)
 			{
 				throw new DynamicExtensionsSystemException("Exception while hibernate rollback: "+daoEx.getMessage(), daoEx);
 			}
 			
 			throw new DynamicExtensionsSystemException("Exception while creating Entity: "+daoException.getMessage(),daoException);
 		} catch (UserNotAuthorizedException userNotAuthorizedException) {
 			userNotAuthorizedException.printStackTrace();
 			throw new DynamicExtensionsSystemException("userNotAuthorizedException",userNotAuthorizedException);
 		}finally
 		{
 			try
 			{
 				hibernateDAO.closeSession();
 			}
 			catch(DAOException daoEx)
 			{
 				throw new DynamicExtensionsSystemException("Exception while closing the session");
 			}
 		}
 		
 	}
 	
 	/**
 	 * Create multiple entities.Their metadata is registered andcorresponding tables are created
 	 * to store the records.
 	 * @param entities
 	 */
 	public void createEntities(List entities){
 		
 	}
 	/**
 	 * Edits the given entity.
 	 * @param entity the entity to be edited.
 	 */
 	public void editEntity(Entity entity){
 		
 	}
 	/**
 	 * Delets the entity from metadata and drops the related table that stores the entity records.
 	 * @param entity the entity to be deleted.
 	 */
 	public void deleteEntity(Entity entity){
 		
 	}
 	/**
 	 * Delets the entity from metadata and drops the related table that stores the entity records.
 	 * @param entityIdentifier the identifier of the entity that is to be deleted.
 	 */
 	public void deleteEntity(Long entityIdentifier){
 		
 	}
 	/**
 	 * Retrieves the entity with the given identifier.
 	 * @param entityIdentifier the identifier of the entity that is to be retrieved.
 	 * @return the entity with the given identifier.
 	 */
 	public Entity getEntity(Long entityIdentifier){
 		Entity entity = null;
 		return entity;
 	}
 	/**
 	 * Retrieves the entity with the given name.
 	 * @param entityName name of the entity that is to be retrieved.
 	 * @return the entity with the given name.
 	 */
 	public Entity getEntity(String entityName){
 		Entity entity = null;
 		return entity;
 	}
 	
 	/**
 	 * Return list of all entities in the system.
 	 * @return list of all entities in the system.
 	 */
 	public List getAllEntities(){
 		List entityList = null;
 		return entityList;
 	}
 	
 	/**
 	 * Return list of entities corresponding to the list of identifiers passed.
 	 * @param entityIdentifiers list of entity identifiers.
 	 * @return list of entities corresponding to the list of identifiers passed.
 	 */
 	public List getEntitiesById(List entityIdentifiers){
 		List entityList = null;
 		return entityList;
 	}
 	
 	//------------------------End of methods related to Entity
 	
 	//--------- Methods related to data manipulation ----------------------
 	/**
 	 * Add a record for the given entity with the data provided in the map.
 	 * @param entity entity for which record is to be added.
 	 * @param dataMap map containing the actual data to be inserted
 	 * with Key - attribute and Value - data for that attribute.
 	 */
 	public void  addRecord (Entity entity, Map dataMap){
 		
 	}
 	
 	/**
 	 * Add multiple records for the given entity with the data provided in the list.
 	 * @param entity entity for which records are to be added.
 	 * @param dataMapList list of maps containing the datato be inserted
 	 * with Key - attribute and Value - data for that attribute.
 	 */
 	public void  addMultipleRecords (Entity entity, List dataMapList){
 		
 	}
 	/**
 	 * Retrieves record for the given entity and with the given identifier.
 	 * @param entity entity for which record is to be retrieved.
 	 * @param recordIdentifier identifier that uniquely determines the record.
 	 * @return Map having record for the given entity and with the given identifier 
 	 * with Key - attribute and Value - data for that attribute.
 	 */
 	public Map getRecord (Entity entity, Long recordIdentifier){
 		Map recordMap = null;
 		return recordMap;
 	}
 	
 	/**
 	 * Retrieves the record for the given entity and for the given attributes with the given record identifier.
 	 * @param entity entity for which record is to be retrieved.
 	 * @param attributeList list of attributes whose value is to be retrieved.
 	 * @param recordIdentifier identifier that uniquely determines the record.
 	 * @return Map having record for the given entity,attributes and with the given identifier 
 	 * with Key - attribute and Value - data for that attribute.
 	 */
 	public Map getRecord (Entity entity, List attributeList, Long recordIdentifier){
 		Map recordMap = null;                             
 		return recordMap;
 	}
 	/**
 	 * Retrieves all records for the given entity.
 	 * @param entity entity for which records are to be retrieved.
 	 * @return list of map having record for the given entity 
 	 * with Key - attribute and Value - data for that attribute.
 	 */
 	public List getAllRecords (Entity entity){
 		List recordsList = null;
 		return recordsList; 
 	}
 	/**
 	 * Retrieves all records for the given entity for the given attributes.
 	 * @param entity entity for which records are to be retrieved.
 	 * @param attributeList list of attributes whose value is to be retrieved.
 	 * @return list of map having record for the given entity and given attributes
 	 * with Key - attribute and Value - data for that attribute.
 	 */
 	public List getAllRecords (Entity entity,List attributeList){
 		List recordList = null;
 		return recordList; 
 	}
 	
 	/**
 	 * Edits the record for the given entity and the record identifier with the data in the dataMap.
 	 * @param entity entity for which record is to be updated.
 	 * @param recordIdentifier identifier that uniquely determines the record.
 	 * @param dataMap Map having record for the given entity and with the given identifier 
 	 * with Key - attribute and Value - data for that attribute.
 	 */
 	public void editRecord (Entity entity, Long recordIdentifier, Map dataMap){
 		
 	}
 	
 	/**
 	 * Edits the records for the given entity with the data in the map.
 	 * @param entity entity for which records are to be updated.
 	 * @param mapOfDataMap map containing key as record identifier and value as dataMap conatining key as Attibute
 	 * and value as value of that attribute.
 	 */
 	public void editMultipleRecords (Entity entity, Map mapOfDataMap){
 		
 	}
 	
 	/**
 	 * Deletes record of the given entity and with the given record identifier.
 	 * @param entity entity for which record is to be deleted.
 	 * @param recordIdentifier identifier that uniquely determines the record.
 	 */
 	public void deleteRecord (Entity entity , Long recordIdentifier){
 		
 	}
 	/**
 	 * Deletes records of the given entity and with the given record identifiers list.
 	 * @param entity entity for which record is to be deleted.
 	 * @param recordIdentifierList list of record identifiers that need to be deleted from the entity.
 	 */
 	public void deleteMultipleRecords (Entity entity, List recordIdentifierList){
 		
 	}
 	
 	
 	//---------End of methods related to data manipulation -----------------
 	
 	//------------------------Methods related to entity group ------------------
 	/**
 	 * This method creates Entity Group.
 	 * @param entityGroup the entity group to be created.
 	 */
 	public void createEntityGroup(EntityGroup entityGroup){
 		
 	}
 	/**
 	 * This method edits the given Entity Group. 
 	 * @param entityGroup the entity group to be edited.
 	 */
 	public void editEntityGroup(EntityGroup entityGroup){
 		
 	}
 	/**
 	 * Retrieves Entity Group with the given identifier.
 	 * @param entityGroupIdentifier entity group identifier. 
 	 * @return Entity Group with the given identifier.
 	 */
 	public EntityGroup getEntityGroup(Long entityGroupIdentifier){
 		EntityGroup entityGroup = null;
 		return entityGroup; 
 	}
 	/**
 	 * Retrieves Entity Group with the given name.
 	 * @param entityGroupName name of the Entity Group.
 	 * @return Entity Group with the given name.
 	 */
 	public EntityGroup getEntityGroup(String entityGroupName){
 		EntityGroup entityGroup = null;
 		return entityGroup; 
 	}
 	
 	/**
 	 * Deletes the given Entity Group.
 	 * @param entityGroup Entity Group to be deleted.
 	 */
 	public void deleteEntityGroup(EntityGroup entityGroup){
 		
 	}
 	/**
 	 * Deletes the Entity Group with the given identifier.
 	 * @param entityGroupIdentifier entity group identifier. 
 	 */
 	public void deleteEntityGroup(Long entityGroupIdentifier){
 		
 	}
 	/**
 	 * Deletes the Entity Group with the given name.
 	 * @param entityGroupName name of the Entity Group.
 	 */
 	public void deleteEntityGroup(String entityGroupName){
 		
 	}
 	
 	/**
 	 * 
 	 * @param entityGroup
 	 * @param entity
 	 */
 	public void addEntityToEntityGroup(EntityGroup entityGroup,Entity entity){
 		
 	}
 	/**
 	 * 
 	 * @param entityGroup
 	 * @param entity
 	 */
 	public void deleteEntityFromEntityGroup(EntityGroup entityGroup,Entity entity){
 		
 	}
 	
 	/**
 	 * Executes a query and return result set.
 	 * @param queryToGetNextIdentifier
 	 * @param sessionDataBean
 	 * @param isSecureExecute
 	 * @param hasConditionOnIdentifiedField
 	 * @param queryResultObjectDataMap
 	 * @return
 	 * @throws DAOException
 	 * @throws ClassNotFoundException
 	 */
 	List getResultInList(String queryToGetNextIdentifier, SessionDataBean sessionDataBean, boolean isSecureExecute, boolean hasConditionOnIdentifiedField, Map queryResultObjectDataMap) throws DAOException, ClassNotFoundException{
 		List resultList = null;
 		JDBCDAO jdbcDAO = (JDBCDAO)DAOFactory.getDAO(Constants.JDBC_DAO);
 		try {
 			jdbcDAO.openSession(null);
 			resultList = jdbcDAO.executeQuery(queryToGetNextIdentifier,sessionDataBean,isSecureExecute,hasConditionOnIdentifiedField,queryResultObjectDataMap);
 		} catch (DAOException daoException) {
 			daoException.printStackTrace();
 			throw new DAOException("Exception while retrieving the query result",daoException);
 		} finally{
 			try
 			{
 				jdbcDAO.closeSession();
 			}
 			catch(DAOException daoException)
 			{
 				throw new DAOException("Exception while closing the jdbc session",daoException);
 			}
 		}
 		return resultList;
 	}
 	
 	
 	/**
 	 * Returns the name of the values tabnle related to the entity.
 	 * @param entity the entity.
 	 * @return the name of the values tabnle related to the entity.
 	 */
 	String getEntityTableName(Entity entity){
 		return("EAV_ENTITY"+entity.getId());
 	}
 	
 	/**
 	 * Method generates the next identifier for the table that stores the value of the passes entity.
 	 * @param entity
 	 * @return
 	 * @throws DAOException
 	 * @throws ClassNotFoundException
 	 */
 	private Long getNextIdentifier(Entity entity)throws DAOException, ClassNotFoundException{
 		TableProperties tableProperties = entity.getTableProperties();
 		String entityTableName = tableProperties.getName();
 		StringBuffer queryToGetNextIdentifier = new StringBuffer("SELECT MAX(IDENTIFIER) FROM "+entityTableName);
 		List resultList = QueryInterfaceManager.getResultInList(queryToGetNextIdentifier.toString(),new SessionDataBean(),false,false,null);
 		if(resultList == null){
 			throw new DAOException("Could not fetch the next identifier for table "+entityTableName);
 		}
 		List internalList = (List)resultList.get(0);
 		if(internalList == null || internalList.isEmpty()){
 			throw new DAOException("Could not fetch the next identifier for table "+entityTableName); 
 		}
 		String idString = (String)(internalList.get(0));
 		Long identifier = null;
 		if(idString == null || idString.trim().equals("")){
 			identifier = new Long(0); 
 		} else{
 			identifier = new Long(idString);
 			if(identifier == null){
 				identifier = new Long(0);
 			}
 		}
 		long id = identifier.longValue();
 		id++;
 		identifier = new Long(id);
 		return identifier;
 	}
 	
 }
