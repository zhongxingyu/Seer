 
 package edu.common.dynamicextensions.entitymanager;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.ObjectOutputStream;
 import java.sql.Blob;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Timestamp;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.Stack;
 
 import edu.common.dynamicextensions.domain.AbstractAttribute;
 import edu.common.dynamicextensions.domain.Association;
 import edu.common.dynamicextensions.domain.Attribute;
 import edu.common.dynamicextensions.domain.AttributeRecord;
 import edu.common.dynamicextensions.domain.DateAttributeTypeInformation;
 import edu.common.dynamicextensions.domain.Entity;
 import edu.common.dynamicextensions.domain.EntityGroup;
 import edu.common.dynamicextensions.domain.FileAttributeRecordValue;
 import edu.common.dynamicextensions.domain.FileAttributeTypeInformation;
 import edu.common.dynamicextensions.domain.NumericAttributeTypeInformation;
 import edu.common.dynamicextensions.domain.ObjectAttributeTypeInformation;
 import edu.common.dynamicextensions.domain.StringAttributeTypeInformation;
 import edu.common.dynamicextensions.domain.integration.EntityMapCondition;
 import edu.common.dynamicextensions.domain.userinterface.SelectControl;
 import edu.common.dynamicextensions.domaininterface.AbstractAttributeInterface;
 import edu.common.dynamicextensions.domaininterface.AbstractMetadataInterface;
 import edu.common.dynamicextensions.domaininterface.AssociationDisplayAttributeInterface;
 import edu.common.dynamicextensions.domaininterface.AssociationInterface;
 import edu.common.dynamicextensions.domaininterface.AttributeInterface;
 import edu.common.dynamicextensions.domaininterface.AttributeTypeInformationInterface;
 import edu.common.dynamicextensions.domaininterface.DynamicExtensionBaseDomainObjectInterface;
 import edu.common.dynamicextensions.domaininterface.EntityGroupInterface;
 import edu.common.dynamicextensions.domaininterface.EntityInterface;
 import edu.common.dynamicextensions.domaininterface.userinterface.AssociationControlInterface;
 import edu.common.dynamicextensions.domaininterface.userinterface.ContainerInterface;
 import edu.common.dynamicextensions.domaininterface.userinterface.ControlInterface;
 import edu.common.dynamicextensions.exception.DynamicExtensionsApplicationException;
 import edu.common.dynamicextensions.exception.DynamicExtensionsSystemException;
 import edu.common.dynamicextensions.util.AssociationTreeObject;
 import edu.common.dynamicextensions.util.DynamicExtensionsUtility;
 import edu.common.dynamicextensions.util.global.DEConstants;
 import edu.common.dynamicextensions.util.global.DEConstants.AssociationType;
 import edu.wustl.common.beans.NameValueBean;
 import edu.wustl.common.util.global.CommonServiceLocator;
 import edu.wustl.common.util.global.Constants;
 import edu.wustl.common.util.global.Status;
 import edu.wustl.common.util.logger.Logger;
 import edu.wustl.dao.DAO;
 import edu.wustl.dao.HashedDataHandler;
 import edu.wustl.dao.HibernateDAO;
 import edu.wustl.dao.JDBCDAO;
 import edu.wustl.dao.exception.DAOException;
 import edu.wustl.dao.query.generator.DBTypes;
 import edu.wustl.dao.util.NamedQueryParam;
 
 /**
  *
  * @author mandar_shidhore
  * @author kunal_kamble
  * @author rajesh_patil
  */
 public class EntityManager extends AbstractMetadataManager implements EntityManagerInterface
 {
 
 	/**
 	 * Static instance of the entity manager.
 	 */
 	private static EntityManagerInterface entityManager = null;
 
 	/**
 	 * Static instance of the queryBuilder.
 	 */
 	private static DynamicExtensionBaseQueryBuilder queryBuilder = null;
 
 	/**
 	 * Instance of entity manager util class
 	 */
 	EntityManagerUtil entityManagerUtil = new EntityManagerUtil();
 
 	/**
 	 * Empty Constructor.
 	 */
 	protected EntityManager()
 	{
 		super();
 	}
 
 	/**
 	 * Returns the instance of the Entity Manager.
 	 * @return entityManager singleton instance of the Entity Manager.
 	 */
 	public static synchronized EntityManagerInterface getInstance()
 	{
 		if (entityManager == null)
 		{
 			entityManager = new EntityManager();
 			DynamicExtensionsUtility.initialiseApplicationVariables();
 			queryBuilder = QueryBuilderFactory.getQueryBuilder();
 		}
 
 		return entityManager;
 	}
 
 	/**
 	 * Mock entity manager can be placed in the entity manager using this method.
 	 * @param entityManager
 	 */
 	public static void setInstance(EntityManagerInterface entManager)
 	{
 		EntityManager.entityManager = entManager;
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.common.dynamicextensions.entitymanager.AbstractMetadataManager#getQueryBuilderInstance()
 	 */
 	protected DynamicExtensionBaseQueryBuilder getQueryBuilderInstance()
 	{
 		return queryBuilder;
 	}
 
 	/**
 	 * Persists the entity to the database. Also creates the dynamic tables and associations
 	 * between those tables using the meta data information in the entity object.
 	 * @param entity entity to be persisted
 	 * @return entity persisted entity
 	 * @throws DynamicExtensionsSystemException
 	 * @throws DynamicExtensionsApplicationException
 	 */
 	public EntityInterface persistEntity(EntityInterface entity)
 			throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException
 	{
 		List<String> revQueries = new LinkedList<String>();
 		List<String> queries = new ArrayList<String>();
 		Stack<String> rlbkQryStack = new Stack<String>();
 		HibernateDAO hibernateDAO = null;
 
 		try
 		{
 			hibernateDAO = DynamicExtensionsUtility.getHibernateDAO();
 			preProcess(entity, revQueries, queries);
 			if (entity.getId() == null)
 			{
 				hibernateDAO.insert(entity, false);
 			}
 			else
 			{
 				hibernateDAO.update(entity);
 			}
 
 			postProcess(queries, revQueries, rlbkQryStack);
 
 			hibernateDAO.commit();
 
 			// Update the dynamic extension cache for all containers within the entity group.
 			EntityGroupInterface entityGroup = entity.getEntityGroup();
 			DynamicExtensionsUtility.updateDynamicExtensionsCache(entityGroup.getId());
 		}
 		catch (DAOException e)
 		{
 			rollbackQueries(rlbkQryStack, entity, e, hibernateDAO);
 			throw new DynamicExtensionsSystemException(e.getMessage(), e, DYEXTN_S_003);
 		}
 		finally
 		{
 			try
 			{
 				DynamicExtensionsUtility.closeHibernateDAO(hibernateDAO);
 			}
 			catch (DAOException e)
 			{
 				throw new DynamicExtensionsSystemException(e.getMessage(), e, DYEXTN_S_003);
 			}
 		}
 
 		return entity;
 	}
 
 	/**
 	 * This method is used to save the meta data information  
 	 * of the given entity without creating its data table.
 	 * @param entityInterface entity to be persisted
 	 * @return entity persisted entity
 	 */
 	public EntityInterface persistEntityMetadata(EntityInterface entity)
 			throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException
 	{
 		Stack<String> rlbkQryStack = new Stack<String>();
 		HibernateDAO hibernateDAO = null;
 		try
 		{
 			hibernateDAO = DynamicExtensionsUtility.getHibernateDAO();
 			if (entity.getId() == null)
 			{
 				hibernateDAO.insert(entity, false);
 			}
 			else
 			{
 				hibernateDAO.update(entity);
 			}
 
 			hibernateDAO.commit();
 
 			// Update the dynamic extension cache for all containers within the entity group.
 			EntityGroupInterface entityGroup = entity.getEntityGroup();
 			DynamicExtensionsUtility.updateDynamicExtensionsCache(entityGroup.getId());
 		}
 		catch (DAOException e)
 		{
 			rollbackQueries(rlbkQryStack, entity, e, hibernateDAO);
 			throw new DynamicExtensionsSystemException(e.getMessage(), e, DYEXTN_S_003);
 		}
 		finally
 		{
 			try
 			{
 				DynamicExtensionsUtility.closeHibernateDAO(hibernateDAO);
 			}
 			catch (DAOException e)
 			{
 				throw new DynamicExtensionsSystemException(e.getMessage(), e, DYEXTN_S_003);
 			}
 		}
 
 		return entity;
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.common.dynamicextensions.entitymanager.AbstractMetadataManager#preProcess(edu.common.dynamicextensions.domaininterface.DynamicExtensionBaseDomainObjectInterface, java.util.List, java.util.List)
 	 */
 	protected void preProcess(DynamicExtensionBaseDomainObjectInterface dyExtBsDmnObj,
 			List<String> revQueries, List<String> queries) throws DynamicExtensionsSystemException,
 			DynamicExtensionsApplicationException
 	{
 		EntityInterface entityObj = (EntityInterface) dyExtBsDmnObj;
 		createDynamicQueries(entityObj, revQueries, queries);
 	}
 
 	/**
 	 * @param entity
 	 * @throws DynamicExtensionsSystemException
 	 * @throws DynamicExtensionsApplicationException
 	 */
 	private void checkParentChangeAllowed(Entity entity) throws DynamicExtensionsSystemException,
 			DynamicExtensionsApplicationException
 	{
 		String tableName = entity.getTableProperties().getName();
 		if (queryBuilder.isDataPresent(tableName))
 		{
 			throw new DynamicExtensionsApplicationException(
 					"Can not change the data type of the attribute", null, DYEXTN_A_010);
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.common.dynamicextensions.entitymanager.AbstractMetadataManager#postProcess(java.util.List, java.util.List, java.util.Stack)
 	 */
 	protected void postProcess(List<String> queries, List<String> revQueries,
 			Stack<String> rlbkQryStack) throws DynamicExtensionsSystemException
 	{
 		queryBuilder.executeQueries(queries, revQueries, rlbkQryStack);
 	}
 
 	/**
 	 * This method creates dynamic queries.
 	 * @param entity
 	 * @param revQueries
 	 * @param queries
 	 * @return
 	 * @throws DynamicExtensionsSystemException
 	 * @throws DynamicExtensionsApplicationException
 	 */
 	private List<String> createDynamicQueries(EntityInterface entity, List<String> revQueries,
 			List<String> queries) throws DynamicExtensionsSystemException,
 			DynamicExtensionsApplicationException
 	{
 		return getDynamicQueryList(entity.getEntityGroup(), revQueries, queries);
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.common.dynamicextensions.entitymanager.AbstractMetadataManager#LogFatalError(java.lang.Exception, edu.common.dynamicextensions.domaininterface.AbstractMetadataInterface)
 	 */
 	protected void logFatalError(Exception exception, AbstractMetadataInterface abstrMetadata)
 	{
 		String table = "";
 		String name = "";
 		if (abstrMetadata != null)
 		{
 			EntityInterface entity = (EntityInterface) abstrMetadata;
 			entity.getTableProperties().getName();
 			name = entity.getName();
 		}
 
 		Logger.out
 				.error("***Fatal Error.. Inconsistent data table and metadata information for the entity -"
 						+ name + "***");
 		Logger.out.error("Please check the table -" + table);
 		Logger.out.error("The cause of the exception is - " + exception.getMessage());
 		Logger.out.error("The detailed log is : ");
 
 	}
 
 	/**
 	 * Returns a collection of association objects given the source entity id and
 	 * target entity id.
 	 * @param srcEntityId source entity Id
 	 * @param tgtEntityId target entity Id
 	 * @return associations collection of association
 	 * @throws DynamicExtensionsSystemException
 	 * @throws DynamicExtensionsApplicationException
 	 */
 	public Collection<AssociationInterface> getAssociations(Long srcEntityId, Long tgtEntityId)
 			throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException
 	{
 		// Create a map of substitution parameters.
 		Map<String, NamedQueryParam> substParams = new HashMap<String, NamedQueryParam>();
 		substParams.put("0", new NamedQueryParam(DBTypes.LONG, srcEntityId));
 		substParams.put("1", new NamedQueryParam(DBTypes.LONG, tgtEntityId));
 
 		// Following method is called to execute the stored HQL, the name of which is given as 
 		// the first parameter. The second parameter is the map which contains the actual values 
 		// that are replaced for the place holders.
 		Collection<AssociationInterface> associations = executeHQL("getAssociations", substParams);
 
 		return associations;
 	}
 
 	/**
 	 * Returns a collection of association identifiers given the source entity id and
 	 * target entity id.
 	 * @param srcEntityId source entity Id
 	 * @param tgtEntityId target entity Id
 	 * @return associations collection of association
 	 * @throws DynamicExtensionsSystemException
 	 * @throws DynamicExtensionsApplicationException
 	 */
 	public Collection<Long> getAssociationIds(Long srcEntityId, Long tgtEntityId)
 			throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException
 	{
 		// Create a map of substitution parameters.
 		Map<String, NamedQueryParam> substParams = new HashMap<String, NamedQueryParam>();
 		substParams.put("0", new NamedQueryParam(DBTypes.LONG, srcEntityId));
 		substParams.put("1", new NamedQueryParam(DBTypes.LONG, tgtEntityId));
 
 		// Following method is called to execute the stored HQL, the name of which is given as 
 		// the first parameter. The second parameter is the map which contains the actual values 
 		// that are replaced for the place holders.
 		Collection<Long> associationIds = executeHQL("getAssociationIds", substParams);
 
 		return associationIds;
 	}
 
 	/**
 	 * Returns an entity object given the entity name;
 	 * @param entityName entity name
 	 * @return entity
 	 * @throws DynamicExtensionsSystemException
 	 * @throws DynamicExtensionsApplicationException
 	 */
 	public EntityInterface getEntityByName(String entityName)
 			throws DynamicExtensionsSystemException
 	{
 		EntityInterface entity = (EntityInterface) getObjectByName(Entity.class.getName(),
 				entityName);
 
 		return entity;
 	}
 
 	/**
 	 * Returns an association object given the entity name and source role name.
 	 * @param entityName
 	 * @param srcRoleName
 	 * @return associations collection of association
 	 * @throws DynamicExtensionsSystemException
 	 * @throws DynamicExtensionsApplicationException
 	 */
 	public Collection<AssociationInterface> getAssociation(String entityName, String srcRoleName)
 			throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException
 	{
 		// Create a map of substitution parameters.
 		Map<String, NamedQueryParam> substParams = new HashMap<String, NamedQueryParam>();
 		substParams.put("0", new NamedQueryParam(DBTypes.STRING, entityName));
 		substParams.put("1", new NamedQueryParam(DBTypes.STRING, srcRoleName));
 
 		// Following method is called to execute the stored HQL, the name of which is given as 
 		// the first parameter. The second parameter is the map which contains the actual values
 		// that are replaced for the place holders.
 		Collection<AssociationInterface> associations = executeHQL("getAssociation", substParams);
 
 		return associations;
 	}
 
 	/**
 	 * Returns an association object given the association name.
 	 * @param assoName name of association
 	 * @return association
 	 * @throws DynamicExtensionsSystemException
 	 * @throws DynamicExtensionsApplicationException
 	 */
 	public AssociationInterface getAssociationByName(String assoName)
 			throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException
 	{
 		// Create a map of substitution parameters.
 		Map<String, NamedQueryParam> substParams = new HashMap<String, NamedQueryParam>();
 		substParams.put("0", new NamedQueryParam(DBTypes.STRING, assoName));
 
 		// Following method is called to execute the stored HQL, the name of which is given as 
 		// the first parameter. The second parameter is the map which contains the actual values
 		// that are replaced for the place holders.
 		Collection<AssociationInterface> associations = executeHQL("getAssociationByName",
 				substParams);
 		AssociationInterface association = null;
 		if (associations != null && !associations.isEmpty())
 		{
 			association = associations.iterator().next();
 		}
 
 		return association;
 	}
 
 	/**
 	 * @param srcEntName source entity name
 	 * @param assoName association name
 	 * @param tgtEntName target entity name
 	 * @return association
 	 * @throws DynamicExtensionsSystemException
 	 * @throws DynamicExtensionsApplicationException
 	 */
 	public AssociationInterface getAssociation(String srcEntName, String assoName, String tgtEntName)
 			throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException
 	{
 		// Create a map of substitution parameters.
 		Map<String, NamedQueryParam> substParams = new HashMap<String, NamedQueryParam>();
 		substParams.put("0", new NamedQueryParam(DBTypes.STRING, srcEntName));
 		substParams.put("1", new NamedQueryParam(DBTypes.STRING, assoName));
 		substParams.put("2", new NamedQueryParam(DBTypes.STRING, tgtEntName));
 
 		// Following method is called to execute the stored HQL, the name of which is given as 
 		// the first parameter. The second parameter is the map which contains the actual values
 		// that are replaced for the place holders.
 		Collection<AssociationInterface> associations = executeHQL(
 				"getAssociationBySourceTargetEntity", substParams);
 
 		AssociationInterface association = null;
 		if (associations != null && !associations.isEmpty())
 		{
 			association = associations.iterator().next();
 		}
 
 		return association;
 	}
 
 	/**
 	 * Returns a collection of entities given the entity concept code.
 	 * @param conceptCode
 	 * @return entities collection of entities
 	 * @throws DynamicExtensionsSystemException
 	 * @throws DynamicExtensionsApplicationException
 	 */
 	public Collection<EntityInterface> getEntitiesByConceptCode(String conceptCode)
 			throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException
 	{
 		// Create a map of substitution parameters.
 		Map<String, NamedQueryParam> substParams = new HashMap<String, NamedQueryParam>();
 		substParams.put("0", new NamedQueryParam(DBTypes.STRING, conceptCode));
 
 		// Following method is called to execute the stored HQL, the name of which is given as 
 		// the first parameter. The second parameter is the map which contains the actual values
 		// that are replaced for the place holders.
 		Collection<EntityInterface> entities = executeHQL("getEntitiesByConceptCode", substParams);
 
 		return entities;
 	}
 
 	/**
 	 * Returns all entities in the whole system
 	 * @return collection of entities
 	 * @throws DynamicExtensionsSystemException
 	 * @throws DynamicExtensionsApplicationException
 	 */
 	public Collection<EntityInterface> getAllEntities() throws DynamicExtensionsSystemException,
 			DynamicExtensionsApplicationException
 	{
 		// Calling generic method to return all entities.
 		return getAllObjects(EntityInterface.class.getName());
 	}
 
 	/**
 	 * Returns a single  entity for given identifier
 	 * @param identifier id of entity
 	 * @return entity
 	 * @throws DynamicExtensionsSystemException
 	 * @throws DynamicExtensionsApplicationException
 	 */
 	public EntityInterface getEntityByIdentifier(Long identifier)
 			throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException
 	{
 		// Calling generic method to return entity with a particular identifier.
 		return (EntityInterface) getObjectByIdentifier(EntityInterface.class.getName(), identifier
 				.toString());
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.common.dynamicextensions.entitymanager.EntityManagerInterface#getEntityByIdentifier(java.lang.String)
 	 */
 	public EntityInterface getEntityByIdentifier(String identifier)
 			throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException
 	{
 		// Calling generic method to return entity with a particular identifier.
 		return (EntityInterface) getObjectByIdentifier(EntityInterface.class.getName(), identifier);
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.common.dynamicextensions.entitymanager.EntityManagerInterface#getAllContainers()
 	 */
 	public Collection<ContainerInterface> getAllContainers()
 			throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException
 	{
 		// Calling generic method to return all containers.
 		return getAllObjects(ContainerInterface.class.getName());
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.common.dynamicextensions.entitymanager.EntityManagerInterface#getAllContainersByEntityGroupId(java.lang.Long)
 	 */
 	public Collection<ContainerInterface> getAllContainersByEntityGroupId(Long entGroupId)
 			throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException
 	{
 		Map<String, NamedQueryParam> substParams = new HashMap<String, NamedQueryParam>();
 		substParams.put("0", new NamedQueryParam(DBTypes.LONG, entGroupId));
 
 		return executeHQL("getAllContainersByEntityGroupId", substParams);
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.common.dynamicextensions.entitymanager.EntityManagerInterface#insertData(edu.common.dynamicextensions.domaininterface.EntityInterface, java.util.Map, java.lang.Long[])
 	 */
 	public Long insertData(EntityInterface entity,
 			Map<AbstractAttributeInterface, Object> dataValue, Long... userId)
 			throws DynamicExtensionsApplicationException, DynamicExtensionsSystemException
 	{
 		List<Map<AbstractAttributeInterface, Object>> dataValMaps = new ArrayList<Map<AbstractAttributeInterface, Object>>();
 		dataValMaps.add(dataValue);
 
 		Long usrId = ((userId != null && userId.length != 0) ? userId[0] : null);
 
 		List<Long> recordIds = insertData(entity, dataValMaps, usrId);
 
 		return recordIds.get(0);
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.common.dynamicextensions.entitymanager.EntityManagerInterface#insertData(edu.common.dynamicextensions.domaininterface.EntityInterface, java.util.List, java.lang.Long[])
 	 */
 	public List<Long> insertData(EntityInterface entity,
 			List<Map<AbstractAttributeInterface, Object>> dataValMaps, Long... userId)
 			throws DynamicExtensionsApplicationException, DynamicExtensionsSystemException
 	{
 		List<Long> recordIds = new ArrayList<Long>();
 		Long usrId = ((userId != null && userId.length != 0) ? userId[0] : null);
 		JDBCDAO jdbcDAo = null;
 		try
 		{
 			jdbcDAo = DynamicExtensionsUtility.getJDBCDAO();
 			for (Map<AbstractAttributeInterface, ?> dataValue : dataValMaps)
 			{
 				Long recordId = insertDataForHeirarchy(entity, dataValue, jdbcDAo, usrId);
 				recordIds.add(recordId);
 			}
 			jdbcDAo.commit();
 		}
 		catch (DynamicExtensionsApplicationException e)
 		{
 			throw (DynamicExtensionsApplicationException) handleRollback(e,
 					"Error while inserting data", jdbcDAo, false);
 		}
 		catch (DAOException e)
 		{
 			throw (DynamicExtensionsSystemException) handleRollback(e,
 					"Error while inserting data", jdbcDAo, true);
 		}
 		/*finally
 		{
 			*/try
 		{
 			DynamicExtensionsUtility.closeJDBCDAO(jdbcDAo);
 		}
 		catch (DAOException e)
 		{
 			throw (DynamicExtensionsSystemException) handleRollback(e, "Error while closing",
 					jdbcDAo, true);
 		}
 		//}
 
 		return recordIds;
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.common.dynamicextensions.entitymanager.EntityManagerInterface#insertDataForHeirarchy(edu.common.dynamicextensions.domaininterface.EntityInterface, java.util.Map, edu.wustl.common.dao.JDBCDAO, java.lang.Long[])
 	 */
 	public Long insertDataForHeirarchy(EntityInterface entity,
 			Map<AbstractAttributeInterface, ?> dataValue, JDBCDAO jdbcDao, Long... userId)
 			throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException
 	{
 		Long parentRecId = null;
 		Long usrId = ((userId != null || userId.length > 0) ? userId[0] : null);
 
 		Map<EntityInterface, Map<?, ?>> entityData = initialiseEntityValueMap(entity, dataValue);
 
 		List<EntityInterface> entities = getParentEntityList(entity);
 		for (EntityInterface ent : entities)
 		{
 			Map<?, ?> valuesMap = entityData.get(ent);
 			parentRecId = insertDataForSingleEntity(ent, valuesMap, jdbcDao, parentRecId, usrId);
 		}
 
 		return parentRecId;
 	}
 
 	/**
 	 * @param entity
 	 * @return
 	 */
 	private List<EntityInterface> getParentEntityList(EntityInterface entity)
 	{
 		EntityInterface thisEntity = entity;
 		List<EntityInterface> entities = new ArrayList<EntityInterface>();
 		entities.add(thisEntity);
 
 		while (thisEntity.getParentEntity() != null)
 		{
 			entities.add(0, thisEntity.getParentEntity());
 			thisEntity = thisEntity.getParentEntity();
 		}
 
 		return entities;
 	}
 
 	/**
 	 * @param entity
 	 * @param dataValue
 	 * @return
 	 */
 	private Map<EntityInterface, Map<?, ?>> initialiseEntityValueMap(EntityInterface entity,
 			Map<AbstractAttributeInterface, ?> dataValue)
 	{
 		Map<EntityInterface, Map<?, ?>> entityMap = new HashMap<EntityInterface, Map<?, ?>>();
 		Map<AbstractAttributeInterface, ?> dataValueMap = dataValue;
 		// Ensuring null check in case of category inheritance.
 		if (dataValueMap == null)
 		{
 			dataValueMap = new HashMap();
 		}
 
 		for (AbstractAttributeInterface abstrAttribute : dataValueMap.keySet())
 		{
 			EntityInterface attrEntity = abstrAttribute.getEntity();
 			Object value = dataValueMap.get(abstrAttribute);
 
 			Map<AbstractAttributeInterface, Object> entDataValMap = (Map) entityMap.get(attrEntity);
 			if (entDataValMap == null)
 			{
 				entDataValMap = new HashMap<AbstractAttributeInterface, Object>();
 				entityMap.put(attrEntity, entDataValMap);
 			}
 
 			entDataValMap.put(abstrAttribute, value);
 		}
 
 		return entityMap;
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.common.dynamicextensions.entitymanager.EntityManagerInterface#insertDataForSingleEntity(edu.common.dynamicextensions.domaininterface.EntityInterface, java.util.Map, edu.wustl.common.dao.JDBCDAO, java.lang.Long, java.lang.Long[])
 	 */
 	public Long insertDataForSingleEntity(EntityInterface entity, Map<?, ?> dataValue,
 			JDBCDAO jdbcDao, Long parentRecId, Long... userId)
 			throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException
 	{
 		Map<?, ?> dataValueMap = dataValue;
 		Long usrId = ((userId != null && userId.length != 0) ? userId[0] : null);
 
 		if (entity == null)
 		{
 			throw new DynamicExtensionsSystemException("Input to insert data is null");
 		}
 
 		// If empty, insert row with only identifier column value.
 		if (dataValueMap == null)
 		{
 			dataValueMap = new HashMap();
 		}
 
 		Long identifier = null;
 		if (parentRecId == null)
 		{
 			identifier = entityManagerUtil.getNextIdentifier(entity.getTableProperties().getName());
 		}
 		else
 		{
 			identifier = parentRecId;
 		}
 
 		StringBuffer queryValues = new StringBuffer();
 		List<Object> columnValues = new ArrayList<Object>();
 		List<String> columnNames = new ArrayList<String>();
 		List<Object> columnNullValues = new ArrayList<Object>();
 
 		StringBuffer insertQuery = new StringBuffer(INSERT_INTO_KEYWORD);
 		insertQuery.append(entity.getTableProperties().getName() + WHITESPACE);
 		insertQuery.append(OPENING_BRACKET);
 		insertQuery.append("IDENTIFIER,");
 		insertQuery.append(Constants.ACTIVITY_STATUS_COLUMN);
 
 		columnNames.add("IDENTIFIER ");
 		columnValues.add(identifier);
 		queryValues.append(VALUES_KEYWORD);
 		queryValues.append(OPENING_BRACKET);
 		queryValues.append(identifier);
 		queryValues.append(COMMA);
 
 		columnNames.add(Constants.ACTIVITY_STATUS_COLUMN);
 		columnValues.add(Status.ACTIVITY_STATUS_ACTIVE.toString());
 		queryValues.append(Status.ACTIVITY_STATUS_ACTIVE.toString());
 
 		String tableName = entity.getTableProperties().getName();
 
 		Object value = null;
 		List<String> queries = new ArrayList<String>();
 
 		Set<?> uiColumns = dataValueMap.keySet();
 		Iterator<?> uiColIter = uiColumns.iterator();
 		while (uiColIter.hasNext())
 		{
 			AbstractAttribute attribute = (AbstractAttribute) uiColIter.next();
 
 			value = dataValueMap.get(attribute);
 			if (value == null || value.toString().length() == 0)
 			{
 				continue;
 			}
 
 			if (attribute instanceof AttributeInterface)
 			{
 				updateColumnNamesAndColumnValues(attribute, value, columnNames, columnValues,
 						columnNullValues);
 				if (((AttributeInterface) attribute).getAttributeTypeInformation() instanceof FileAttributeTypeInformation
 						&& !(value instanceof String))
 				{
 					insertQuery.append(COMMA);
 					queryValues.append(COMMA);
 					insertQuery.append(attribute.getName() + UNDERSCORE + FILE_NAME);
 					queryValues.append(((FileAttributeRecordValue) value).getFileName());
 					queryValues.append(COMMA);
 					insertQuery.append(COMMA);
 					insertQuery.append(attribute.getName() + UNDERSCORE + CONTENT_TYPE);
 					queryValues.append(((FileAttributeRecordValue) value).getContentType());
 					insertQuery.append(COMMA);
 					insertQuery.append(((AttributeInterface) attribute).getColumnProperties()
 							.getName());
 					queryValues.append(COMMA);
 					queryValues.append("FILE CONTENTS ARE NOT AUDITED!");
 				}
 				else
 				{
 					queryValues.append(COMMA);
 					queryValues.append(queryBuilder.getFormattedValue(attribute, value));
 					insertQuery.append(COMMA);
 					insertQuery.append(((AttributeInterface) attribute).getColumnProperties()
 							.getName());
 				}
 			}
 			else
 			{
 				List<Long> recordIds = null;
 
 				// In case of associations, separate queries need to be fired 
 				// depending on the cardinalities.
 				AssociationInterface association = (AssociationInterface) attribute;
 				if (association.getSourceRole().getAssociationsType().equals(
 						AssociationType.CONTAINTMENT)
 						|| (association.getSourceRole().getAssociationsType().equals(
 								AssociationType.ASSOCIATION) && association.getIsCollection()))
 				{
 					recordIds = new ArrayList<Long>();
 
 					// Get the maps for contained entity.
 					List<Map> mapsOfCntaindEnt = (List) value;
 					for (Map valueMap : mapsOfCntaindEnt)
 					{
 						// Get the record identifier for contained entity.
 						Long recordId = insertDataForHeirarchy(association.getTargetEntity(),
 								valueMap, jdbcDao, usrId);
 						recordIds.add(recordId);
 					}
 				}
 				else
 				{
 					recordIds = (List<Long>) value;
 				}
 
 				queries.addAll(queryBuilder.getAssociationInsertDataQuery(jdbcDao, association,
 						recordIds, identifier));
 			}
 		}
 
 		try
 		{
 			HashedDataHandler hashedDataHandler = new HashedDataHandler();
 			hashedDataHandler.insertHashedValues(tableName, columnValues, columnNames, jdbcDao);
 			insertQuery.append(CLOSING_BRACKET);
 			queryValues.append(CLOSING_BRACKET);
 			insertQuery.append(queryValues);
 
 			queryBuilder.auditQuery(jdbcDao, insertQuery.toString(), usrId);
 
 			for (String query : queries)
 			{
 				logDebug("insertData", "Query for insert data is : " + insertQuery);
 				try
 				{
 					jdbcDao.executeUpdate(query);
 				}
 				catch (DAOException e)
 				{
 					throw new DynamicExtensionsSystemException("Exception in query execution", e);
 				}
 				queryBuilder.auditQuery(jdbcDao, query, usrId);
 			}
 
 		}
 		catch (DAOException e)
 		{
 			throw new DynamicExtensionsApplicationException("Exception in query execution", e);
 		}
 
 		return identifier;
 	}
 
 	/**
 	 * @param attribute
 	 * @param value
 	 * @param columnNames
 	 * @param columnValues
 	 * @throws ParseException
 	 * @throws ParseException
 	 * @throws IOException
 	 * @throws IOException
 	 */
 	private void updateColumnNamesAndColumnValues(AbstractAttribute attribute, Object value,
 			List<String> columnNames, List<Object> columnValues, List<Object> columnNullValues)
 			throws DynamicExtensionsApplicationException
 	{
 		Object object = value;
 		// Get the primitive attribute.
 		AttributeInterface primitiveAttr = (AttributeInterface) attribute;
 
 		// Populate FileAttributeRecordValue hibernate object.
 		if (primitiveAttr.getAttributeTypeInformation() instanceof FileAttributeTypeInformation)
 		{
 			//DO NOT change this if and combined it with above if  ,it will change logic flow in case of FILE type attribute
 			if (!(object instanceof String))
 			{
 				populateFileAttribute(columnNames, columnValues, (FileAttributeRecordValue) object,
 						primitiveAttr);
 				columnNullValues.add(null);
 				columnNullValues.add(null);
 				columnNullValues.add(null);
 			}
 		}
 		else
 		{
 			try
 			{
 				columnNames.add(primitiveAttr.getColumnProperties().getName());
 				if (primitiveAttr.getAttributeTypeInformation() instanceof DateAttributeTypeInformation)
 				{
 					if (object != null && object.toString().length() != 0)
 					{
 						String dateFormat = ((DateAttributeTypeInformation) primitiveAttr
 								.getAttributeTypeInformation()).getFormat();
 						object = new Timestamp(new SimpleDateFormat(dateFormat,
 								CommonServiceLocator.getInstance().getDefaultLocale()).parse(
 								object.toString()).getTime());
 					}
 				}
 				else if (primitiveAttr.getAttributeTypeInformation() instanceof ObjectAttributeTypeInformation)
 				{
 					ByteArrayOutputStream bStream = new ByteArrayOutputStream();
 					ObjectOutputStream oStream = new ObjectOutputStream(bStream);
 					oStream.writeObject(object);
 
 					object = bStream.toByteArray();
 				}
 				else if (primitiveAttr.getAttributeTypeInformation() instanceof StringAttributeTypeInformation)
 				{
 					object = DynamicExtensionsUtility.getEscapedStringValue(object.toString());
 				}
 				if (object == null || object.toString().length() == 0)
 				{
 					columnValues.add(null);
 					if (primitiveAttr.getAttributeTypeInformation() instanceof DateAttributeTypeInformation)
 					{
 						columnNullValues.add(java.sql.Types.DATE);
 					}
 					else if (primitiveAttr.getAttributeTypeInformation() instanceof StringAttributeTypeInformation)
 					{
 						columnNullValues.add(java.sql.Types.VARCHAR);
 					}
 					else if (primitiveAttr.getAttributeTypeInformation() instanceof NumericAttributeTypeInformation)
 					{
 						columnNullValues.add(java.sql.Types.NUMERIC);
 					}
 					else if (primitiveAttr.getAttributeTypeInformation() instanceof ObjectAttributeTypeInformation)
 					{
 						columnNullValues.add(java.sql.Types.BLOB);
 					}
 					else
 					{
 						columnNullValues.add(null);
 					}
 				}
 				else
 				{
 					columnValues.add(object);
 					columnNullValues.add(null);
 				}
 			}
 			catch (ParseException e)
 			{
 				throw new DynamicExtensionsApplicationException(
 						"Exception in parsing date for attribute " + attribute.getName(), e);
 			}
 			catch (IOException e)
 			{
 				throw new DynamicExtensionsApplicationException(
 						"Exception while creating blob for object type attribute "
 								+ attribute.getName(), e);
 			}
 		}
 	}
 
 	/**
 	 * This method adds the extra columns information
 	 * that needs to be maintained while adding the file data
 	 * @param columnNames list of column names
 	 * @param columnValues list of column values
 	 * @param value file attribute value
 	 * @param attribute
 	 */
 	private void populateFileAttribute(List<String> columnNames, List<Object> columnValues,
 			FileAttributeRecordValue value, AttributeInterface attribute)
 	{
 		columnNames.add(attribute.getColumnProperties().getName() + UNDERSCORE + FILE_NAME);
 		columnValues.add(value.getFileName());
 
 		columnNames.add(attribute.getColumnProperties().getName() + UNDERSCORE + CONTENT_TYPE);
 		columnValues.add(value.getContentType());
 
 		columnNames.add(attribute.getColumnProperties().getName());
 		columnValues.add(value.getFileContent());
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.common.dynamicextensions.entitymanager.EntityManagerInterface#editData(edu.common.dynamicextensions.domaininterface.EntityInterface, java.util.Map, java.lang.Long, java.lang.Long[])
 	 */
 	public boolean editData(EntityInterface entity, Map<AbstractAttributeInterface, ?> dataValue,
 			Long recordId, Long... userId) throws DynamicExtensionsApplicationException,
 			DynamicExtensionsSystemException
 	{
 		boolean isSuccess = false;
 		Long usrId = ((userId != null && userId.length != 0) ? userId[0] : null);
 
 		JDBCDAO jdbcDao = null;
 		try
 		{
 			jdbcDao = DynamicExtensionsUtility.getJDBCDAO();
 
 			Map<EntityInterface, Map<?, ?>> entityValueMap = initialiseEntityValueMap(entity,
 					dataValue);
 
 			List<EntityInterface> entities = getParentEntityList(entity);
 			for (EntityInterface ent : entities)
 			{
 				Map valueMap = entityValueMap.get(ent);
 				isSuccess = editDataForSingleEntity(ent, valueMap, recordId, jdbcDao, usrId);
 			}
 			jdbcDao.commit();
 		}
 		/*catch (DynamicExtensionsApplicationException e)
 		{
 			throw (DynamicExtensionsApplicationException) handleRollback(e,
 					"Error while inserting data", jdbcDAO, false);
 		}*/
 		catch (DAOException e)
 		{
 			throw new DynamicExtensionsSystemException("Error while updating", e);
 			/*throw (DynamicExtensionsSystemException) handleRollback(e, "Error while updating",
 					jdbcDAO, true);*/
 		}
 		finally
 		{
 			try
 			{
 				DynamicExtensionsUtility.closeJDBCDAO(jdbcDao);
 			}
 			catch (DAOException e)
 			{
 				throw (DynamicExtensionsSystemException) handleRollback(e, "Error while closing",
 						jdbcDao, true);
 			}
 
 		}
 
 		return isSuccess;
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.common.dynamicextensions.entitymanager.EntityManagerInterface#editDataForHeirarchy(edu.common.dynamicextensions.domaininterface.EntityInterface, java.util.Map, java.lang.Long, edu.wustl.common.dao.JDBCDAO, java.lang.Long[])
 	 */
 	public boolean editDataForHeirarchy(EntityInterface entity,
 			Map<AbstractAttributeInterface, ?> dataValue, Long recordId, JDBCDAO jdbcDao,
 			Long... userId) throws DynamicExtensionsApplicationException,
 			DynamicExtensionsSystemException
 	{
 		boolean isSuccess = false;
 
 		Long usrId = ((userId != null && userId.length != 0) ? userId[0] : null);
 
 		Map<EntityInterface, Map<?, ?>> entityValueMap = initialiseEntityValueMap(entity, dataValue);
 
 		List<EntityInterface> entities = getParentEntityList(entity);
 		try
 		{
 			for (EntityInterface ent : entities)
 			{
 				Map valueMap = entityValueMap.get(ent);
 				isSuccess = editDataForSingleEntity(ent, valueMap, recordId, jdbcDao, usrId);
 			}
 		}
 		catch (DynamicExtensionsApplicationException e)
 		{
 			throw (DynamicExtensionsApplicationException) handleRollback(e,
 					"Error while inserting data", jdbcDao, false);
 		}
 		catch (DynamicExtensionsSystemException e)
 		{
 			throw (DynamicExtensionsSystemException) handleRollback(e, "Error while updating",
 					jdbcDao, true);
 		}
 
 		return isSuccess;
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.common.dynamicextensions.entitymanager.EntityManagerInterface#editDataForSingleEntity(edu.common.dynamicextensions.domaininterface.EntityInterface, java.util.Map, java.lang.Long, edu.wustl.common.dao.JDBCDAO, java.lang.Long[])
 	 */
 	public boolean editDataForSingleEntity(EntityInterface entity, Map<?, ?> dataValue,
 			Long recordId, JDBCDAO jdbcDao, Long... userId)
 			throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException
 	{
 		Long usrId = ((userId != null && userId.length != 0) ? userId[0] : null);
 
 		if (entity == null || dataValue == null || dataValue.isEmpty())
 		{
 			return true;
 		}
 
 		List<String> columnNames = new ArrayList<String>();
 		List<Object> columnValues = new ArrayList<Object>();
 		List<Object> columnNullValues = new ArrayList<Object>();
 		String tableName = entity.getTableProperties().getName();
 
 		Set<?> uiColumnSet = dataValue.keySet();
 		Iterator<?> uiColumnSetIter = uiColumnSet.iterator();
 
 		List<String> assoRemDataQries = new ArrayList<String>();
 		List<String> assoInsDataQries = new ArrayList<String>();
 
 		while (uiColumnSetIter.hasNext())
 		{
 			AbstractAttribute attribute = (AbstractAttribute) uiColumnSetIter.next();
 
 			Object value = dataValue.get(attribute);
 			if (value == null)
 			{
 				continue;
 			}
 			if (attribute instanceof AttributeInterface)
 			{
 				updateColumnNamesAndColumnValues(attribute, value, columnNames, columnValues,
 						columnNullValues);
 			}
 			else
 			{
 				List<Long> recordIds = new ArrayList<Long>();
 
 				AssociationInterface association = (AssociationInterface) attribute;
 				if (association.getSourceRole().getAssociationsType().equals(
 						AssociationType.CONTAINTMENT)
 						|| (association.getSourceRole().getAssociationsType().equals(
 								AssociationType.ASSOCIATION) && association.getIsCollection()))
 				{
 					List<String> remCntmntRecQries = new ArrayList<String>();
 					recordIds.add(recordId);
 
 					queryBuilder.getContenmentAssociationRemoveDataQueryList(
 							((Association) attribute), recordIds, remCntmntRecQries, false);
 
 					entityManagerUtil.executeDMLQueryList(jdbcDao, remCntmntRecQries);
 
 					List<Map> mapsOfCntaindEnt = (List<Map>) value;
 					recordIds.clear();
 					for (Map valueMap : mapsOfCntaindEnt)
 					{
 						Long childRecId = insertDataForHeirarchy(association.getTargetEntity(),
 								valueMap, jdbcDao, usrId);
 						recordIds.add(childRecId);
 					}
 
 				}
 				else
 				{
 					// For associations, we need to remove previously associated target records first.
 					String removeQuery = queryBuilder.getAssociationRemoveDataQuery(
 							((Association) attribute), recordId);
 
 					if (removeQuery != null && removeQuery.trim().length() != 0)
 					{
 						assoRemDataQries.add(removeQuery);
 					}
 
 					recordIds = (List<Long>) value;
 				}
 
 				// Now add new associated target records.
 				List<String> insertQueries = queryBuilder.getAssociationInsertDataQuery(jdbcDao,
 						((Association) attribute), recordIds, recordId);
 				if (insertQueries != null && !insertQueries.isEmpty())
 				{
 					assoInsDataQries.addAll(insertQueries);
 				}
 			}
 		}
 
 		List<String> editDataQueries = new ArrayList<String>();
 		editDataQueries.addAll(assoRemDataQries);
 		editDataQueries.addAll(assoInsDataQries);
 
 		try
 		{
 			// Shift the below code into the jdbcdao			
 			if (!columnNames.isEmpty())
 			{
 				StringBuffer query = new StringBuffer("UPDATE " + tableName + " SET ");
 				StringBuffer auditQuery = new StringBuffer("UPDATE " + tableName + " SET ");
 
 				Iterator<String> colNameIter = columnNames.iterator();
 				String columnName = null;
 				while (colNameIter.hasNext())
 				{
 					columnName = colNameIter.next();
 					query.append(columnName + EQUAL + "?");
 					auditQuery.append(columnName + EQUAL + "?");
 					if (colNameIter.hasNext())
 					{
 						query.append(COMMA + WHITESPACE);
 						auditQuery.append(COMMA + WHITESPACE);
 					}
 				}
 
 				query.append(WHERE_KEYWORD);
 				query.append(IDENTIFIER);
 				query.append(WHITESPACE + EQUAL + WHITESPACE);
 				query.append(recordId);
 				PreparedStatement preparedStatement = jdbcDao
 						.getPreparedStatement(query.toString());
 				for (int index = 0; index < columnValues.size(); index++)
 				{
 					Object columnValue = columnValues.get(index);
 					Object columnNullValue = columnNullValues.get(index);
 					if (columnNullValue != null)
 					{
 						preparedStatement.setNull(index + 1, Integer.valueOf(columnNullValue
 								.toString()));
 					}
 					else
 					{
 						preparedStatement.setObject(index + 1, columnValue);
 						auditQuery.replace(auditQuery.indexOf("?"), auditQuery.indexOf("?") + 1,
 								columnValue == null ? "" : columnValue.toString());
 					}
 
 				}
 
 				preparedStatement.setMaxFieldSize(1);
 				try
 				{
 					preparedStatement.executeUpdate();
 				}
 				catch (SQLException e)
 				{
 					throw new DynamicExtensionsSystemException("Exception in query execution", e);
 				}
 				finally
 				{
 					try
 					{
 						preparedStatement.close();
 					}
 					catch (SQLException e)
 					{
 						throw new DynamicExtensionsSystemException(
 								"Exception occured while closing statement", e);
 					}
 				}
 
 				/*Commenting this insert because this method is empty currently.
 				 * jdbcDAO.insert(DomainObjectFactory.getInstance().createDESQLAudit(usrId,
 						auditQuery.toString()), null, false, false);*/
 			}
 
 			for (String query : editDataQueries)
 			{
 				logDebug("editData", "Query is: " + query.toString());
 				try
 				{
 					jdbcDao.executeUpdate(query);
 				}
 				catch (DAOException e)
 				{
 					throw new DynamicExtensionsSystemException("Exception in query execution", e);
 				}
 			}
 		}
 		catch (DAOException e)
 		{
 			throw new DynamicExtensionsApplicationException("Exception in editing data", e);
 		}
 		catch (SQLException e)
 		{
 			throw new DynamicExtensionsApplicationException("Exception in editing data", e);
 		}
 
 		return true;
 	}
 
 	/**
 	 * @see edu.common.dynamicextensions.entitymanager.EntityManagerInterface#getRecordById(edu.common.dynamicextensions.domaininterface.EntityInterface, java.lang.Long)
 	 * Value in the map depends on the type of the attribute as explained below.<br>
 	 * Map
 	 *    key    - Attribute Name
 	 *    Value  - List<String> --           multi select attribute.
 	 *             FileAttributeRecordValue  File attribute.
 	 *             List<Long>                Association
 	 *                  if One-One   |____   List will contain only 1 record id that is of target entity's record
 	 *                     Many-One  |
 	 *                  otherwise it will contains one or more record identifiers.
 	 *
 	 *             String                    Other attribute type.
 	 */
 	public Map<AbstractAttributeInterface, Object> getEntityRecordById(EntityInterface entity,
 			Long recordId) throws DynamicExtensionsSystemException,
 			DynamicExtensionsApplicationException
 	{
 		Map<AbstractAttributeInterface, Object> recordValues = new HashMap<AbstractAttributeInterface, Object>();
 
 		Collection attributes = entity.getAttributeCollection();
 		attributes = entityManagerUtil.filterSystemAttributes(attributes);
 
 		String tableName = entity.getTableProperties().getName();
 
 		List<String> selColNames = new ArrayList<String>();
 
 		Map<String, AttributeInterface> colNames = new HashMap<String, AttributeInterface>();
 
 		Iterator attrIter = attributes.iterator();
 		while (attrIter.hasNext())
 		{
 			AttributeInterface attribute = (AttributeInterface) attrIter.next();
 			String dbColumnName = null;
 
 			// For other attributes, create select query.
 			if (attribute.getAttributeTypeInformation() instanceof FileAttributeTypeInformation)
 			{
 				dbColumnName = attribute.getColumnProperties().getName() + UNDERSCORE + FILE_NAME;
 			}
 			else
 			{
 				dbColumnName = attribute.getColumnProperties().getName();
 			}
 
 			selColNames.add(dbColumnName);
 			colNames.put(dbColumnName, attribute);
 		}
 
 		// Get association values.
 		recordValues.putAll(queryBuilder.getAssociationGetRecordQueryList(entity, recordId));
 
 		try
 		{
 			if (!selColNames.isEmpty())
 			{
 				StringBuffer query = new StringBuffer();
 				query.append(SELECT_KEYWORD).append(WHITESPACE);
 
 				for (int i = 0; i < selColNames.size(); i++)
 				{
 					if (i != 0)
 					{
 						query.append(" , ");
 					}
 
 					query.append(selColNames.get(i));
 				}
 
 				query.append(WHITESPACE).append(FROM_KEYWORD).append(WHITESPACE).append(tableName)
 						.append(WHITESPACE).append(WHERE_KEYWORD).append(WHITESPACE).append(
 								IDENTIFIER).append(EQUAL).append(recordId);
 
 				recordValues.putAll(getAttributeValues(selColNames, query.toString(), colNames));
 			}
 		}
 		catch (DAOException e)
 		{
 			throw new DynamicExtensionsSystemException("Error while retrieving the data", e);
 		}
 
 		return recordValues;
 	}
 
 	/** 
 	 * The actual values of the multi select attribute are not stored in the entity's data table because there can
 	 * be more than one values associated with a particular multi select attribute. For this reason, these values
 	 * are stored in a different table. AttributeRecord is the hibernate object that maps to this table.
 	 * So this method is used to get the AttributeRecord for the given combination of entity attribute and the particular
 	 * record of the entity.
 	 * @param entityId
 	 * @param attributeId
 	 * @param recordId
 	 * @return
 	 */
 	private AttributeRecord getAttributeRecord(Long entityId, Long attributeId, Long recordId,
 			HibernateDAO hibernateDao) throws DynamicExtensionsSystemException
 	{
 		AttributeRecord attrRecords = null;
 
 		Map<String, NamedQueryParam> substParams = new HashMap<String, NamedQueryParam>();
 		substParams.put("0", new NamedQueryParam(DBTypes.LONG, entityId));
 		substParams.put("1", new NamedQueryParam(DBTypes.LONG, attributeId));
 		substParams.put("2", new NamedQueryParam(DBTypes.LONG, recordId));
 
 		Collection records = null;
 		if (hibernateDao == null)
 		{
 			// The following method takes the name of the query and the actual values 
 			// for the place holders as the parameters.
 			records = executeHQL("getCollectionAttributeRecord", substParams);
 		}
 		else
 		{
 			// The following method takes the name of the query and the actual values 
 			// for the place holders as the parameters.
 			records = executeHQL(hibernateDao, "getCollectionAttributeRecord", substParams);
 		}
 		if (records != null && !records.isEmpty())
 		{
 			attrRecords = (AttributeRecord) records.iterator().next();
 		}
 
 		return attrRecords;
 	}
 
 	/**
 	 * @param selColNames
 	 * @param query
 	 * @param columnNames
 	 * @return
 	 * @throws DynamicExtensionsSystemException
 	 * @throws SQLException
 	 * @throws DAOException 
 	 */
 	private Map<AbstractAttributeInterface, Object> getAttributeValues(List<String> selColNames,
 			String query, Map<String, AttributeInterface> columnNames)
 			throws DynamicExtensionsSystemException, DAOException
 	{
 		Map<AbstractAttributeInterface, Object> records = new HashMap<AbstractAttributeInterface, Object>();
 
 		ResultSet resultSet = null;
 		JDBCDAO jdbcDao = null;
 		try
 		{
 			jdbcDao = DynamicExtensionsUtility.getJDBCDAO();
 			resultSet = jdbcDao.getQueryResultSet(query);
 
 			if (resultSet.next())
 			{
 				for (int i = 0; i < selColNames.size(); i++)
 				{
 					String dbColumnName = selColNames.get(i);
 					Object value = getValueFromResultSet(resultSet, columnNames, dbColumnName, i);
 					Attribute attribute = (Attribute) columnNames.get(dbColumnName);
 					records.put(attribute, value);
 				}
 			}
 		}
 		catch (SQLException e)
 		{
 			throw new DynamicExtensionsSystemException(e.getMessage(), e);
 		}
 		catch (IOException e)
 		{
 			throw new DynamicExtensionsSystemException(e.getMessage(), e);
 		}
 		catch (ClassNotFoundException e)
 		{
 			throw new DynamicExtensionsSystemException(e.getMessage(), e);
 		}
 		finally
 		{
 			jdbcDao.closeStatement(resultSet);
 			DynamicExtensionsUtility.closeJDBCDAO(jdbcDao);
 		}
 
 		return records;
 	}
 
 	/**
 	 * @param resultSet
 	 * @param columnNames
 	 * @param dbColumnName
 	 * @param index
 	 * @return
 	 * @throws SQLException
 	 * @throws IOException
 	 * @throws ClassNotFoundException
 	 */
 	private Object getValueFromResultSet(ResultSet resultSet,
 			Map<String, AttributeInterface> columnNames, String dbColumnName, int index)
 			throws SQLException, IOException, ClassNotFoundException,
 			DynamicExtensionsSystemException
 	{
 		Attribute attribute = (Attribute) columnNames.get(dbColumnName);
 
 		Object valueObj = resultSet.getObject(index + 1);
 		Object value = "";
 
 		if (valueObj != null)
 		{
 			if (valueObj instanceof java.util.Date)
 			{
 				DateAttributeTypeInformation dateAttrTypInfo = (DateAttributeTypeInformation) attribute
 						.getAttributeTypeInformation();
 
 				String format = dateAttrTypInfo.getFormat();
 				if (format == null)
 				{
 					format = CommonServiceLocator.getInstance().getDatePattern();
 				}
 
 				valueObj = resultSet.getTimestamp(index + 1);
 
 				SimpleDateFormat sdFormatter = new SimpleDateFormat(format, CommonServiceLocator
 						.getInstance().getDefaultLocale());
 				value = sdFormatter.format((java.util.Date) valueObj);
 			}
 			else
 			{
 				if (attribute.getAttributeTypeInformation() instanceof ObjectAttributeTypeInformation)
 				{
 					value = queryBuilder.convertValueToObject(valueObj);
 				}
 				else
 				{
 					value = valueObj;
 				}
 			}
 		}
 
 		// All objects on the UI are handled as String, so string values  
 		// of objects need to be stored in the map.
 		if (!(((AttributeInterface) attribute).getAttributeTypeInformation() instanceof FileAttributeTypeInformation)
 				&& !(((AttributeInterface) attribute).getAttributeTypeInformation() instanceof ObjectAttributeTypeInformation))
 		{
 			value = value.toString();
 		}
 
 		return value;
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.common.dynamicextensions.entitymanager.EntityManagerInterface#getEntityRecords(edu.common.dynamicextensions.domaininterface.EntityInterface, java.util.List, java.util.List)
 	 */
 	public EntityRecordResultInterface getEntityRecords(EntityInterface entity,
 			List<? extends AbstractAttributeInterface> abstrAttributes, List<Long> recordIds)
 			throws DynamicExtensionsSystemException
 	{
 		if (abstrAttributes == null || abstrAttributes.isEmpty())
 		{
 			return null;
 		}
 
 		EntityRecordResultInterface entityRec = new EntityRecordResult();
 
 		EntityRecordMetadata recMetadata = new EntityRecordMetadata();
 		recMetadata.setAttributeList(abstrAttributes);
 		entityRec.setEntityRecordMetadata(recMetadata);
 		recMetadata.setAttributeList(abstrAttributes);
 
 		// Filtering abstract attributes into attribute and association.
 		List<AssociationInterface> associations = new ArrayList<AssociationInterface>();
 		List<AttributeInterface> attributes = new ArrayList<AttributeInterface>();
 		filterAttributes(abstrAttributes, attributes, associations);
 
 		String tableName = entity.getTableProperties().getName();
 		List<String> selColNames = new ArrayList<String>();
 
 		Map<String, AttributeInterface> columnNames = new HashMap<String, AttributeInterface>();
 
 		Iterator<AttributeInterface> attributeIter = attributes.iterator();
 		while (attributeIter.hasNext())
 		{
 			AttributeInterface attribute = (AttributeInterface) attributeIter.next();
 
 			// For the other attributes, create select query.
 			String dbColumnName = attribute.getColumnProperties().getName();
 			selColNames.add(dbColumnName);
 			columnNames.put(dbColumnName, attribute);
 		}
 		try
 		{
 			// Processing primitive attributes.
 			StringBuffer query = new StringBuffer();
 			query.append(SELECT_KEYWORD).append(IDENTIFIER).append(WHITESPACE);
 
 			for (int i = 0; i < selColNames.size(); i++)
 			{
 				query.append(" , ");
 				query.append(selColNames.get(i));
 			}
 
 			query.append(WHITESPACE).append(FROM_KEYWORD).append(tableName);
 
 			if (recordIds != null && !recordIds.isEmpty())
 			{
 				query.append(WHERE_KEYWORD).append(IDENTIFIER).append(IN_KEYWORD).append(
 						EntityManagerUtil.getListToString(recordIds));
 			}
 
 			List<EntityRecordInterface> entityRecords = getEntityRecordList(selColNames, query
 					.toString(), columnNames, recMetadata);
 
 			entityRec.setEntityRecordList(entityRecords);
 			for (EntityRecordInterface entityRecord : entityRecords)
 			{
 				Long recordId = entityRecord.getRecordId();
 				queryBuilder.putAssociationValues(associations, entityRec, entityRecord, recordId);
 			}
 		}
 		catch (Exception e)
 		{
 			throw new DynamicExtensionsSystemException("Error while retrieving the data", e);
 		}
 
 		return entityRec;
 	}
 
 	/**
 	 * @param selColNames
 	 * @param query
 	 * @param columnNames
 	 * @param recMetadata
 	 * @return
 	 * @throws DynamicExtensionsSystemException
 	 * @throws SQLException
 	 * @throws IOException
 	 * @throws ClassNotFoundException
 	 * @throws DAOException 
 	 */
 	private List<EntityRecordInterface> getEntityRecordList(List<String> selColNames, String query,
 			Map<String, AttributeInterface> columnNames, EntityRecordMetadata recMetadata)
 			throws DynamicExtensionsSystemException, SQLException, IOException,
 			ClassNotFoundException, DAOException
 	{
 		List<EntityRecordInterface> entityRecords = new ArrayList<EntityRecordInterface>();
 
 		ResultSet resultSet = null;
 		JDBCDAO jdbcDao = null;
 		try
 		{
 			jdbcDao = DynamicExtensionsUtility.getJDBCDAO();
 			resultSet = jdbcDao.getQueryResultSet(query);
 			while (resultSet.next())
 			{
 				EntityRecordInterface entityRecord = new EntityRecord();
 				Long identifier = resultSet.getLong(1);
 				entityRecord.setRecordId(identifier);
 
 				Object[] values = new Object[recMetadata.getAttributeList().size()];
 
 				for (int i = 1; i <= selColNames.size(); i++)
 				{
 					String dbColumnName = selColNames.get(i - 1);
 					Object value = getValueFromResultSet(resultSet, columnNames, dbColumnName, i);
 					AttributeInterface attribute = (AttributeInterface) columnNames
 							.get(dbColumnName);
 					int indexOfAttribute = recMetadata.getAttributeList().indexOf(attribute);
 					values[indexOfAttribute] = value;
 				}
 
 				entityRecord.setRecordValueList(Arrays.asList(values));
 				entityRecords.add(entityRecord);
 			}
 		}
 		finally
 		{
 			try
 			{
 				jdbcDao.closeStatement(resultSet);
 				DynamicExtensionsUtility.closeJDBCDAO(jdbcDao);
 			}
 			catch (DAOException e)
 			{
 				throw new DynamicExtensionsSystemException(e.getMessage(), e);
 			}
 
 		}
 
 		return entityRecords;
 	}
 
 	/**
 	 * This method filers abstractAttributes into attributes and associations.
 	 * @param abstrAttributes
 	 * @param attributes
 	 * @param associations
 	 */
 	private void filterAttributes(List<? extends AbstractAttributeInterface> abstrAttributes,
 			Collection<AttributeInterface> attributes, Collection<AssociationInterface> associations)
 	{
 		for (AbstractAttributeInterface abstractAttr : abstrAttributes)
 		{
 			if (abstractAttr instanceof AssociationInterface)
 			{
 				associations.add((AssociationInterface) abstractAttr);
 			}
 			else
 			{
 				attributes.add((AttributeInterface) abstractAttr);
 			}
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.common.dynamicextensions.entitymanager.EntityManagerInterface#getRecordById(edu.common.dynamicextensions.domaininterface.EntityInterface, java.lang.Long)
 	 */
 	public Map<AbstractAttributeInterface, Object> getRecordById(EntityInterface entity,
 			Long recordId) throws DynamicExtensionsSystemException,
 			DynamicExtensionsApplicationException
 	{
 		Map<AbstractAttributeInterface, Object> recordValues = new HashMap<AbstractAttributeInterface, Object>();
 		HibernateDAO hibernateDAO = null;
 		try
 		{
 			hibernateDAO = DynamicExtensionsUtility.getHibernateDAO();
 			if (entity == null || entity.getId() == null || recordId == null)
 			{
 				throw new DynamicExtensionsSystemException("Invalid Input");
 			}
 			recordValues = getRecordForSingleEntity(entity, recordId);
 		}
 		catch (DynamicExtensionsApplicationException e)
 		{
 			throw (DynamicExtensionsApplicationException) handleRollback(e,
 					"Error while Retrieving  data", hibernateDAO, false);
 		}
 		catch (DAOException e)
 		{
 			throw (DynamicExtensionsSystemException) handleRollback(e,
 					"Error while Retrieving  data", hibernateDAO, true);
 		}
 		finally
 		{
 			try
 			{
 				DynamicExtensionsUtility.closeHibernateDAO(hibernateDAO);
 			}
 			catch (DAOException e)
 			{
 				throw (DynamicExtensionsSystemException) handleRollback(e, "Error while closing",
 						hibernateDAO, true);
 			}
 		}
 
 		return recordValues;
 	}
 
 	/**
 	 * This method return all the records based on entity
 	 * @param entity Identifier of entity
 	 * @param recordId recordId of entity
 	 * @return recordValues for the entity
 	 * @throws DynamicExtensionsSystemException fails to get entity record
 	 * @throws DynamicExtensionsApplicationException fails to get entity record
 	 */
 	public Map<AbstractAttributeInterface, Object> getRecordForSingleEntity(EntityInterface entity,
 			Long recordId) throws DynamicExtensionsSystemException,
 			DynamicExtensionsApplicationException
 	{
 		Map<AbstractAttributeInterface, Object> recordValues = new HashMap<AbstractAttributeInterface, Object>();
 		EntityInterface parentEntity = entity;
 		do
 		{
 			Map<AbstractAttributeInterface, Object> recForSingleEnt = getEntityRecordById(
 					parentEntity, recordId);
 			recordValues.putAll(recForSingleEnt);
 			parentEntity = parentEntity.getParentEntity();
 		}
 		while (parentEntity != null);
 
 		return recordValues;
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.common.dynamicextensions.entitymanager.EntityManagerInterface#deleteRecord(edu.common.dynamicextensions.domaininterface.EntityInterface, java.lang.Long)
 	 */
 	public boolean deleteRecord(EntityInterface entity, Long recordId)
 			throws DynamicExtensionsApplicationException, DynamicExtensionsSystemException
 	{
 		boolean isRecDeleted = false;
 
 		queryBuilder.validateForDeleteRecord(entity, recordId, null);
 
 		Collection<AttributeInterface> attributes = entity.getAttributeCollection();
 		Collection<AssociationInterface> associations = entity.getAssociationCollection();
 
 		HibernateDAO hibernateDAO = null;
 
 		List<String> assoRemQueries = new ArrayList<String>();
 		try
 		{
 			hibernateDAO = DynamicExtensionsUtility.getHibernateDAO();
 			if (attributes != null && !attributes.isEmpty())
 			{
 				Iterator<AttributeInterface> attrIter = attributes.iterator();
 				while (attrIter.hasNext())
 				{
 					AttributeInterface attribute = (AttributeInterface) attrIter.next();
 					AttributeTypeInformationInterface typeInfo = attribute
 							.getAttributeTypeInformation();
 
 					// Remove AttributeRecord objects for multi select and file type attributes.
 					if (typeInfo instanceof FileAttributeTypeInformation
 							|| typeInfo instanceof ObjectAttributeTypeInformation)
 					{
 						AttributeRecord attrRecords = getAttributeRecord(entity.getId(), attribute
 								.getId(), recordId, hibernateDAO);
 						hibernateDAO.delete(attrRecords);
 					}
 				}
 			}
 			hibernateDAO.commit();
 		}
 		catch (DAOException e)
 		{
 			rollback(hibernateDAO);
 			throw new DynamicExtensionsSystemException(e.getMessage(), e, DYEXTN_S_001);
 		}
 		finally
 		{
 			try
 			{
 				DynamicExtensionsUtility.closeHibernateDAO(hibernateDAO);
 
 			}
 			catch (DAOException e1)
 			{
 				throw new DynamicExtensionsSystemException(e1.getMessage(), e1, DYEXTN_S_001);
 			}
 		}
 
 		JDBCDAO jdbcDAO = null;
 		try
 		{
 			jdbcDAO = DynamicExtensionsUtility.getJDBCDAO();
 			if (associations != null && !associations.isEmpty())
 			{
 				Iterator<AssociationInterface> assoIter = associations.iterator();
 				while (assoIter.hasNext())
 				{
 					AssociationInterface association = assoIter.next();
 
 					if (association.getSourceRole().getAssociationsType().equals(
 							AssociationType.CONTAINTMENT))
 					{
 						List<Long> recordIds = new ArrayList<Long>();
 						recordIds.add(recordId);
 						QueryBuilderFactory.getQueryBuilder()
 								.getContenmentAssociationRemoveDataQueryList(association,
 										recordIds, assoRemQueries, true);
 					}
 				}
 			}
 
 			StringBuffer query = new StringBuffer();
 			query.append(UPDATE_KEYWORD + WHITESPACE + entity.getTableProperties().getName());
 			query.append(SET_KEYWORD + Constants.ACTIVITY_STATUS_COLUMN + EQUAL + " '"
 					+ Status.ACTIVITY_STATUS_DISABLED.toString() + "' ");
 			query.append(WHERE_KEYWORD + WHITESPACE + IDENTIFIER + WHITESPACE + EQUAL + WHITESPACE
 					+ recordId.toString());
 
 			List<String> delRecQueries = new ArrayList<String>(assoRemQueries);
 			delRecQueries.add(0, query.toString());
 
 			for (String delRecQuery : delRecQueries)
 			{
 				logDebug("deleteRecord", "QUERY for delete record is : " + delRecQuery.toString());
 
 				if (delRecQuery != null && delRecQuery.trim().length() != 0)
 				{
 					try
 					{
 						jdbcDAO.executeUpdate(delRecQuery.toString());
 					}
 					catch (DAOException e)
 					{
 						throw new DynamicExtensionsSystemException("Exception in query execution",
 								e);
 					}
 				}
 			}
 			isRecDeleted = true;
 			jdbcDAO.commit();
 		}
 		catch (DynamicExtensionsApplicationException e)
 		{
 			rollback(jdbcDAO, e);
 			throw e;
 		}
 		catch (DAOException e)
 		{
 			rollback(hibernateDAO);
 			throw new DynamicExtensionsSystemException(e.getMessage(), e, DYEXTN_S_001);
 		}
 		finally
 		{
 			try
 			{
 				DynamicExtensionsUtility.closeJDBCDAO(jdbcDAO);
 			}
 			catch (DAOException e1)
 			{
 				throw new DynamicExtensionsSystemException(e1.getMessage(), e1, DYEXTN_S_001);
 			}
 		}
 
 		return isRecDeleted;
 	}
 
 	/**
 	 * @param hibernateDAO
 	 */
 	private void rollback(DAO hibernateDAO)
 	{
 		try
 		{
 			hibernateDAO.rollback();
 		}
 		catch (DAOException e1)
 		{
 			Logger.out.debug(e1.getMessage(), e1);
 		}
 	}
 
 	/**
 	 * @param hibernateDAO
 	 * @param e
 	 * @throws DynamicExtensionsSystemException
 	 */
 	private void rollback(DAO hibernateDAO, DynamicExtensionsApplicationException exception)
 			throws DynamicExtensionsSystemException
 	{
 		try
 		{
 			hibernateDAO.rollback();
 		}
 		catch (DAOException e1)
 		{
 			throw new DynamicExtensionsSystemException(e1.getMessage(), e1, DYEXTN_S_001);
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.common.dynamicextensions.entitymanager.EntityManagerInterface#deleteRecords(java.lang.Long, java.util.List)
 	 */
 	public void deleteRecords(Long containerId, List<Long> recordIds)
 			throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException
 	{
 		ContainerInterface container = DynamicExtensionsUtility
 				.getContainerByIdentifier(containerId.toString());
 
 		EntityInterface entity = (EntityInterface) container.getAbstractEntity();
 		for (Long recordId : recordIds)
 		{
 			deleteRecord(entity, recordId);
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.common.dynamicextensions.entitymanager.EntityManagerInterface#getRecordsForAssociationControl(edu.common.dynamicextensions.domaininterface.userinterface.AssociationControlInterface)
 	 */
 	public Map<Long, List<String>> getRecordsForAssociationControl(
 			AssociationControlInterface assoControl) throws DynamicExtensionsSystemException
 	{
 		Map<Long, List<String>> assoRecords = new HashMap<Long, List<String>>();
 		List<String> tableNames = new ArrayList<String>();
 		String tableName;
 		String tgtEntityTable = "";
 		String columnName;
 		String onClause = ON_KEYWORD;
 
 		int counter = 0;
 		boolean areMultipleAttr = false;
 
 		Collection<AssociationDisplayAttributeInterface> assoAttributes = assoControl
 				.getAssociationDisplayAttributeCollection();

 		if (assoControl instanceof SelectControl)
 		{
 			tgtEntityTable = ((AssociationInterface) ((SelectControl) assoControl)
 					.getBaseAbstractAttribute()).getTargetEntity().getTableProperties().getName();
 		}
 		String selectClause = SELECT_KEYWORD + tgtEntityTable + "." + IDENTIFIER;
 		String fromClause = FROM_KEYWORD + tgtEntityTable + ", ";
 		String whereClause = WHERE_KEYWORD;
 		// Clause for multiple columns.
 		String multipleColClause = SELECT_KEYWORD + tgtEntityTable + "." + IDENTIFIER + ", ";
 
 		List associationAttr = new ArrayList(assoAttributes);
 		Collections.sort(associationAttr);
 
 		Iterator<AssociationDisplayAttributeInterface> attrIter = assoAttributes.iterator();
 		AssociationDisplayAttributeInterface displayAttribute = null;
 
 		while (attrIter.hasNext())
 		{
 			displayAttribute = attrIter.next();
 			columnName = displayAttribute.getAttribute().getColumnProperties().getName();
 			tableName = displayAttribute.getAttribute().getEntity().getTableProperties().getName();
 
 			if (assoControl instanceof SelectControl
 					&& ((AssociationInterface) ((SelectControl) assoControl)
 							.getBaseAbstractAttribute()).getTargetEntity().getParentEntity() != null)
 			{
 				selectClause = selectClause + ", " + tableName + "." + columnName;
 
 				if (!(fromClause.contains(tableName)))
 				{
 					fromClause = fromClause + tableName + ", ";
 				}
 				if (counter == 0 && assoAttributes.size() > 1)
 				{
 					whereClause = whereClause + tableName + ".ACTIVITY_STATUS <> 'Disabled' AND ";
 					whereClause = whereClause + tableName + "." + IDENTIFIER + " = ";
 				}
 				else if (counter > 0 && assoAttributes.size() > 1)
 				{
 					whereClause = whereClause + tableName + "." + IDENTIFIER + " AND " + tableName
 							+ ".ACTIVITY_STATUS <> 'Disabled' AND " + tableName + "." + IDENTIFIER
 							+ " = ";
 				}
 				else if (assoAttributes.size() == 1)
 				{
 					if (!(fromClause.contains(tgtEntityTable)))
 					{
 						fromClause = fromClause + tgtEntityTable + ", ";
 					}
 					whereClause = whereClause + tgtEntityTable
 							+ ".ACTIVITY_STATUS <> 'Disabled' AND ";
 					whereClause = whereClause + tableName + "." + IDENTIFIER + " = "
 							+ tgtEntityTable + "." + IDENTIFIER + " AND " + tgtEntityTable + "."
 							+ IDENTIFIER + " = ";
 				}
 
 				counter++;
 
 				tableNames.add(tableName);
 			}
 			else
 			{
 				areMultipleAttr = true;
 				multipleColClause += columnName + ", ";
 				tableNames.add(tableName);
 			}
 
 			if (tableNames.isEmpty() && !(assoControl instanceof SelectControl))
 			{
 				selectClause = selectClause + tableName + "." + IDENTIFIER;
 				fromClause = fromClause + tableName;
 				onClause = onClause + tableName + "." + IDENTIFIER;
 				tableNames.add(tableName);
 			}
 			else
 			{
 				if (tableNames.indexOf(tableName) == -1)
 				{
 					tableNames.add(tableName);
 					fromClause = fromClause + JOIN_KEYWORD + tableName;
 					onClause = onClause + EQUAL + tableName + "." + IDENTIFIER;
 				}
 			}
 		}
 
 		if (!areMultipleAttr)
 		{
 			int lastIndexOfAND = whereClause.lastIndexOf("AND");
 			whereClause = whereClause.substring(0, lastIndexOfAND);
 			fromClause = fromClause.substring(0, fromClause.length() - 2);
 		}
 
 		if (((AssociationInterface) ((SelectControl) assoControl).getBaseAbstractAttribute())
 				.getTargetEntity().getParentEntity() == null)
 		{
 			multipleColClause = multipleColClause.substring(0, multipleColClause.length() - 2)
 					+ FROM_KEYWORD + tgtEntityTable;
 		}
 
 		StringBuffer query = new StringBuffer();
 
 		if (!areMultipleAttr)
 		{
 			query.append(selectClause + fromClause + whereClause);
 		}
 		else
 		{
 			query.append(multipleColClause);
 			query.append(WHERE_KEYWORD
 					+ queryBuilder.getRemoveDisbledRecordsQuery(tableNames.get(0)));
 		}
 
 		JDBCDAO jdbcDao = null;
 		try
 		{
 			jdbcDao = DynamicExtensionsUtility.getJDBCDAO();
 			List results = new ArrayList();
 			results = jdbcDao.executeQuery(query.toString());
 
 			if (results != null)
 			{
 				if (!areMultipleAttr)
 				{
 					for (int i = 0; i < results.size(); i++)
 					{
 						List innerList = (List) results.get(i);
 						Long recordId = Long.parseLong((String) innerList.get(0));
 						innerList.remove(0);
 						assoRecords.put(recordId, innerList);
 					}
 				}
 				else
 				{
 					for (int i = 0; i < results.size(); i++)
 					{
 						List innerList = (List) results.get(i);
 						Long recordId = Long.parseLong((String) innerList.get(0));
 
 						if (assoRecords.containsKey(recordId))
 						{
 							List<String> tempStringList = new ArrayList<String>();
 
 							String existingString = assoRecords.get(recordId).toString().replace(
 									"[", " ");
 							existingString = existingString.replace("]", " ");
 
 							tempStringList.add(existingString.trim() + assoControl.getSeparator()
 									+ (String) innerList.get(1));
 							assoRecords.put(recordId, tempStringList);
 						}
 						else
 						{
 							innerList.remove(0);
 							assoRecords.put(recordId, innerList);
 						}
 					}
 				}
 			}
 		}
 
 		catch (DAOException e)
 		{
 			throw new DynamicExtensionsSystemException("Error while retrieving the data", e);
 		}
 		finally
 		{
 			try
 			{
 				DynamicExtensionsUtility.closeJDBCDAO(jdbcDao);
 			}
 			catch (DAOException e)
 			{
 				throw new DynamicExtensionsSystemException("Error while retrieving the data", e);
 			}
 		}
 
 		return assoRecords;
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.common.dynamicextensions.entitymanager.EntityManagerInterface#getAllContainerBeans()
 	 */
 	public List<NameValueBean> getAllContainerBeans() throws DynamicExtensionsSystemException,
 			DynamicExtensionsApplicationException
 	{
 		// Create a map of substitution parameters.
 		Map<String, NamedQueryParam> substParams = new HashMap<String, NamedQueryParam>();
 		Collection containerBeans = executeHQL("getAllContainerBeans", substParams);
 
 		Object[] contBeans;
 		List<NameValueBean> nameValueBeans = new ArrayList<NameValueBean>();
 
 		Iterator contBeansIter = containerBeans.iterator();
 		while (contBeansIter.hasNext())
 		{
 			contBeans = (Object[]) contBeansIter.next();
 
 			// In case of category creation form caption is optional.
 			if ((String) contBeans[1] != null)
 			{
 				nameValueBeans.add(new NameValueBean((String) contBeans[1], (Long) contBeans[0]));
 			}
 		}
 
 		return nameValueBeans;
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.common.dynamicextensions.entitymanager.EntityManagerInterface#getAllContainerBeansByEntityGroupId(java.lang.Long)
 	 */
 	public List<NameValueBean> getAllContainerBeansByEntityGroupId(Long entityGroupId)
 			throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException
 	{
 		// Create a map of substitution parameters.
 		Map<String, NamedQueryParam> substParams = new HashMap<String, NamedQueryParam>();
 		substParams.put("0", new NamedQueryParam(DBTypes.LONG, entityGroupId));
 
 		Collection containerBeans = executeHQL("getAllContainersBeansByEntityGroupId", substParams);
 
 		Object[] contBeans;
 		List<NameValueBean> nameValueBeans = new ArrayList<NameValueBean>();
 
 		Iterator contBeansIter = containerBeans.iterator();
 		while (contBeansIter.hasNext())
 		{
 			contBeans = (Object[]) contBeansIter.next();
 			// In case of category creation form caption is optional.
 			if ((String) contBeans[1] != null)
 			{
 				nameValueBeans.add(new NameValueBean((String) contBeans[1], (Long) contBeans[0]));
 			}
 		}
 
 		return nameValueBeans;
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.common.dynamicextensions.entitymanager.EntityManagerInterface#getAllContainerInformationObjects()
 	 */
 	public List<ContainerInformationObject> getAllContainerInformationObjects()
 			throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException
 	{
 		// Create a map of substitution parameters.
 		Map<String, NamedQueryParam> substParams = new HashMap<String, NamedQueryParam>();
 		Collection cntInfoObjects = executeHQL("getAllContainerInformationObjects", substParams);
 
 		Object[] contInfoObj;
 		List<ContainerInformationObject> contInfObjects = new ArrayList<ContainerInformationObject>();
 
 		Iterator cntInfoObjIter = cntInfoObjects.iterator();
 		while (cntInfoObjIter.hasNext())
 		{
 			contInfoObj = (Object[]) cntInfoObjIter.next();
 			contInfObjects.add(new ContainerInformationObject((String) contInfoObj[1],
 					((Long) contInfoObj[0]).toString(), (String) contInfoObj[2]));
 		}
 
 		return contInfObjects;
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.common.dynamicextensions.entitymanager.EntityManagerInterface#getAllContainerBeansMap()
 	 */
 	public Map<String, String> getAllContainerBeansMap() throws DynamicExtensionsSystemException,
 			DynamicExtensionsApplicationException
 	{
 		// Create a map of substitution parameters.
 		Map<String, NamedQueryParam> substParams = new HashMap<String, NamedQueryParam>();
 		Object[] cntBeans;
 		Map<String, String> contBeans = new HashMap<String, String>();
 		String containerId;
 		// Container caption.
 		String contCaption;
 
 		Collection containerBeans = executeHQL("getAllContainerBeans", substParams);
 		Iterator contBeansIter = containerBeans.iterator();
 		while (contBeansIter.hasNext())
 		{
 			cntBeans = (Object[]) contBeansIter.next();
 			contCaption = (String) cntBeans[1];
 			containerId = ((Long) cntBeans[0]).toString();
 			contBeans.put(containerId, contCaption);
 		}
 
 		return contBeans;
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.common.dynamicextensions.entitymanager.EntityManagerInterface#getChildrenEntities(edu.common.dynamicextensions.domaininterface.EntityInterface)
 	 */
 	public Collection<EntityInterface> getChildrenEntities(EntityInterface entity)
 			throws DynamicExtensionsSystemException
 	{
 		// Create a map of substitution parameters.
 		Map<String, NamedQueryParam> substParams = new HashMap<String, NamedQueryParam>();
 		substParams.put("0", new NamedQueryParam(DBTypes.LONG, entity.getId()));
 
 		return executeHQL("getChildrenEntities", substParams);
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.common.dynamicextensions.entitymanager.EntityManagerInterface#getAssociationByIdentifier(java.lang.Long)
 	 */
 	public AssociationInterface getAssociationByIdentifier(Long assoId)
 			throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException
 	{
 		// Create a map of substitution parameters.
 		Map<String, NamedQueryParam> substParams = new HashMap<String, NamedQueryParam>();
 		substParams.put("0", new NamedQueryParam(DBTypes.LONG, assoId));
 
 		Collection assocations = executeHQL("getAssociationByIdentifier", substParams);
 		if (assocations.isEmpty())
 		{
 			throw new DynamicExtensionsApplicationException("Object Not Found : id" + assoId, null,
 					DYEXTN_A_008);
 		}
 
 		return (AssociationInterface) assocations.iterator().next();
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.common.dynamicextensions.entitymanager.EntityManagerInterface#getIncomingAssociations(edu.common.dynamicextensions.domaininterface.EntityInterface)
 	 */
 	public Collection<AssociationInterface> getIncomingAssociations(EntityInterface entity)
 			throws DynamicExtensionsSystemException
 	{
 		// Create a map of substitution parameters.
 		Map<String, NamedQueryParam> substParams = new HashMap<String, NamedQueryParam>();
 		substParams.put("0", new NamedQueryParam(DBTypes.LONG, entity.getId()));
 
 		Collection<AssociationInterface> assocations = executeHQL("getAssociationsForTargetEntity",
 				substParams);
 
 		return assocations;
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.common.dynamicextensions.entitymanager.EntityManagerInterface#getIncomingAssociationIds(edu.common.dynamicextensions.domaininterface.EntityInterface)
 	 */
 	public Collection<Long> getIncomingAssociationIds(EntityInterface entity)
 			throws DynamicExtensionsSystemException
 	{
 		// Create a map of substitution parameters.
 		Map<String, NamedQueryParam> substParams = new HashMap<String, NamedQueryParam>();
 		substParams.put("0", new NamedQueryParam(DBTypes.LONG, entity.getId()));
 
 		Collection<Long> assocations = executeHQL("getAssociationIdsForTargetEntity", substParams);
 
 		return assocations;
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.common.dynamicextensions.entitymanager.EntityManagerInterface#getContainerCaption(java.lang.Long)
 	 */
 	public String getContainerCaption(Long containerId) throws DynamicExtensionsSystemException
 	{
 		// Create a map of substitution parameters.
 		Map<String, NamedQueryParam> substParams = new HashMap<String, NamedQueryParam>();
 		substParams.put("0", new NamedQueryParam(DBTypes.LONG, containerId));
 
 		Collection contCaptions = executeHQL("getContainerCaption", substParams);
 
 		return contCaptions.iterator().next().toString();
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.common.dynamicextensions.entitymanager.EntityManagerInterface#getCategoryCaption(java.lang.Long)
 	 */
 	public String getCategoryCaption(Long categoryId) throws DynamicExtensionsSystemException
 	{
 		// Create a map of substitution parameters.
 		Map<String, NamedQueryParam> substParams = new HashMap<String, NamedQueryParam>();
 		substParams.put("0", new NamedQueryParam(DBTypes.LONG, categoryId));
 
 		Collection catCaptions = executeHQL("getRootCategoryEntityCaptionById", substParams);
 
 		return catCaptions.iterator().next().toString();
 	}
 
 	public Long getRootCategoryEntityIdByCategoryName(String categoryName)
 			throws DynamicExtensionsSystemException
 	{
 		// Create a map of substitution parameters.
 		Map<String, NamedQueryParam> substParams = new HashMap<String, NamedQueryParam>();
 		substParams.put("0", new NamedQueryParam(DBTypes.STRING, categoryName));
 		Collection<Long> rootCategoryEntityId = null;
 		rootCategoryEntityId = executeHQL("getRootCategoryEntityId", substParams);
 		if (rootCategoryEntityId != null && !rootCategoryEntityId.isEmpty())
 		{
 			return (Long) rootCategoryEntityId.iterator().next();
 		}
 		return null;
 	}
 
 	public Long getContainerIdFromEntityId(Long entityId) throws DynamicExtensionsSystemException
 	{
 		// Create a map of substitution parameters.
 		Map<String, NamedQueryParam> substParams = new HashMap<String, NamedQueryParam>();
 		substParams.put("0", new NamedQueryParam(DBTypes.LONG, entityId));
 		Collection<Long> containerIds = null;
 		containerIds = executeHQL("getContainerIdFromEntityId", substParams);
 		if (containerIds != null && !containerIds.isEmpty())
 		{
 			return (Long) containerIds.iterator().next();
 		}
 		return null;
 	}
 
 	/**
 	 * @param entityId
 	 * @return
 	 * @throws DynamicExtensionsSystemException
 	 */
 	public String getContainerCaptionFromEntityId(Long entityId)
 			throws DynamicExtensionsSystemException
 	{
 		// Create a map of substitution parameters.
 		Map<String, NamedQueryParam> substParams = new HashMap<String, NamedQueryParam>();
 		substParams.put("0", new NamedQueryParam(DBTypes.STRING, entityId));
 		Collection<String> containerIds = null;
 		containerIds = executeHQL("getContainerCaptionFromEntityId", substParams);
 		if (containerIds != null && !containerIds.isEmpty())
 		{
 			return containerIds.iterator().next();
 		}
 		return null;
 	}
 
 	public Collection<Long> getAllEntityIdsForEntityGroup(Long entityGroupId)
 			throws DynamicExtensionsSystemException
 	{
 		// Create a map of substitution parameters.
 		Map<String, NamedQueryParam> substParams = new HashMap<String, NamedQueryParam>();
 		substParams.put("0", new NamedQueryParam(DBTypes.LONG, entityGroupId));
 
 		Collection<Long> entityIds = executeHQL("getAllEntityIdsForEntityGroup", substParams);
 
 		return entityIds;
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.common.dynamicextensions.entitymanager.EntityManagerInterface#addAssociationColumn(edu.common.dynamicextensions.domaininterface.AssociationInterface)
 	 */
 	public void addAssociationColumn(AssociationInterface association)
 			throws DynamicExtensionsSystemException
 	{
 		List<String> revQueries = new ArrayList<String>();
 		Stack<String> rlbkQryStack = new Stack<String>();
 
 		try
 		{
 			List<String> queries = new ArrayList<String>();
 			queries.addAll(queryBuilder.getQueryPartForAssociation(association, revQueries, true));
 			rlbkQryStack = queryBuilder.executeQueries(queries, revQueries, rlbkQryStack);
 		}
 		catch (DynamicExtensionsSystemException e)
 		{
 			if (!rlbkQryStack.isEmpty())
 			{
 				rollbackQueries(rlbkQryStack, (Entity) association.getEntity(), e, null);
 			}
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.common.dynamicextensions.entitymanager.EntityManagerInterface#associateEntityRecords(edu.common.dynamicextensions.domaininterface.AssociationInterface, java.lang.Long, java.lang.Long)
 	 */
 	public void associateEntityRecords(AssociationInterface association, Long srcEntRecId,
 			Long tgtEntRecId) throws DynamicExtensionsSystemException
 	{
 		queryBuilder.associateRecords(association, srcEntRecId, tgtEntRecId);
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.common.dynamicextensions.entitymanager.EntityManagerInterface#getEntityIdByContainerId(java.lang.Long)
 	 */
 	public Long getEntityIdByContainerId(Long containerId) throws DynamicExtensionsSystemException
 	{
 		// Create a map of substitution parameters.
 		Map<String, NamedQueryParam> substParams = new HashMap<String, NamedQueryParam>();
 		substParams.put("0", new NamedQueryParam(DBTypes.LONG, containerId));
 
 		// The following method takes the name of the query and
 		// the actual values for the place holders as the parameters.
 		Collection records = null;
 		records = executeHQL("getEntityIdForContainerId", substParams);
 		if (records != null && !records.isEmpty())
 		{
 			return (Long) records.iterator().next();
 		}
 
 		return null;
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.common.dynamicextensions.entitymanager.EntityManagerInterface#getEntityCreatedDateByContainerId()
 	 */
 	public Map<Long, Date> getEntityCreatedDateByContainerId()
 			throws DynamicExtensionsSystemException
 	{
 		// Create a map of substitution parameters.
 		Map<String, NamedQueryParam> substParams = new HashMap<String, NamedQueryParam>();
 		Map<Long, Date> records = new HashMap<Long, Date>();
 
 		Collection containers;
 		containers = executeHQL("getAllEntityCreatedDateByContainerId", substParams);
 
 		if (containers != null && !containers.isEmpty())
 		{
 			Iterator iter = containers.iterator();
 
 			while (iter.hasNext())
 			{
 				Object[] objects = (Object[]) iter.next();
 				records.put((Long) objects[0], (Date) objects[1]);
 			}
 		}
 
 		return records;
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.common.dynamicextensions.entitymanager.EntityManagerInterface#checkContainerForAbstractEntity(java.lang.Long, boolean)
 	 */
 	public Long checkContainerForAbstractEntity(Long entityId, boolean isAbstarct)
 			throws DynamicExtensionsSystemException
 	{
 		// Create a map of substitution parameters.
 		Map<String, NamedQueryParam> substParams = new HashMap<String, NamedQueryParam>();
 		substParams.put("0", new NamedQueryParam(DBTypes.LONG, entityId));
 		substParams.put("1", new NamedQueryParam(DBTypes.BOOLEAN, isAbstarct));
 
 		Collection containers = executeHQL("checkContainerForAbstractEntity", substParams);
 
 		Long contId = null;
 
 		if (containers != null && !containers.isEmpty())
 		{
 			contId = (Long) containers.iterator().next();
 
 		}
 
 		return contId;
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.common.dynamicextensions.entitymanager.EntityManagerInterface#checkContainerForAbstractCategoryEntity(java.lang.Long)
 	 */
 	public Long checkContainerForAbstractCategoryEntity(Long entityId)
 			throws DynamicExtensionsSystemException
 	{
 		// Create a map of substitution parameters.
 		Map<String, NamedQueryParam> substParams = new HashMap<String, NamedQueryParam>();
 		substParams.put("0", new NamedQueryParam(DBTypes.LONG, entityId));
 
 		Collection containers = executeHQL("checkContainerForAbstractCategoryEntity", substParams);
 
 		Long contId = null;
 
 		if (containers != null && !containers.isEmpty())
 		{
 			contId = (Long) containers.iterator().next();
 
 		}
 
 		return contId;
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.common.dynamicextensions.entitymanager.EntityManagerInterface#getEntityId(java.lang.String)
 	 */
 	public Long getEntityId(String entityName) throws DynamicExtensionsSystemException
 	{
 		Long entityId = null;
 
 		// Create a map of substitution parameters.
 		Map<String, NamedQueryParam> substParams = new HashMap<String, NamedQueryParam>();
 		substParams.put("0", new NamedQueryParam(DBTypes.STRING, entityName));
 
 		Collection entityIds = executeHQL("getEntityIdentifier", substParams);
 		if (entityIds != null && !entityIds.isEmpty())
 		{
 			entityId = (Long) entityIds.iterator().next();
 		}
 
 		return entityId;
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.common.dynamicextensions.entitymanager.EntityManagerInterface#getContainerIdForEntity(java.lang.Long)
 	 */
 	public Long getContainerIdForEntity(Long entityId) throws DynamicExtensionsSystemException
 	{
 		String tableName = "dyextn_container";
 
 		StringBuffer query = new StringBuffer();
 		query.append(SELECT_KEYWORD + WHITESPACE + IDENTIFIER);
 		query.append(WHITESPACE + FROM_KEYWORD + WHITESPACE + tableName + WHITESPACE);
 		query.append(WHERE_KEYWORD + WHITESPACE + "ENTITY_ID" + WHITESPACE + EQUAL + "'" + entityId
 				+ "'");
 		ResultSet resultSet = null;
 		JDBCDAO jdbcDao = null;
 		try
 		{
 			jdbcDao = DynamicExtensionsUtility.getJDBCDAO();
 			resultSet = jdbcDao.getQueryResultSet(query.toString());
 			if (resultSet != null)
 			{
 				resultSet.next();
 				return resultSet.getLong(IDENTIFIER);
 			}
 		}
 		catch (DAOException e)
 		{
 			Logger.out.debug(e.getMessage());
 		}
 		catch (SQLException e)
 		{
 			Logger.out.debug(e.getMessage());
 		}
 		finally
 		{
 			if (resultSet != null)
 			{
 				try
 				{
 					jdbcDao.closeStatement(resultSet);
 					DynamicExtensionsUtility.closeJDBCDAO(jdbcDao);
 				}
 				catch (DAOException e)
 				{
 					throw new DynamicExtensionsSystemException(e.getMessage(), e);
 				}
 			}
 		}
 
 		return null;
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.common.dynamicextensions.entitymanager.EntityManagerInterface#getNextIdentifierForEntity(java.lang.String)
 	 */
 	public Long getNextIdentifierForEntity(String entityName)
 			throws DynamicExtensionsSystemException
 	{
 		String tableName = "dyextn_database_properties";
 		String NAME = "NAME";
 
 		StringBuffer query = new StringBuffer();
 		query.append(SELECT_KEYWORD + WHITESPACE + NAME);
 		query.append(WHITESPACE + FROM_KEYWORD + WHITESPACE + tableName + WHITESPACE);
 		query.append(WHERE_KEYWORD + WHITESPACE + IDENTIFIER + WHITESPACE + EQUAL);
 		query.append(OPENING_BRACKET);
 		query.append(SELECT_KEYWORD + WHITESPACE + IDENTIFIER);
 		query.append(WHITESPACE + FROM_KEYWORD + WHITESPACE + "dyextn_table_properties"
 				+ WHITESPACE);
 		query.append(WHERE_KEYWORD + WHITESPACE + "ABSTRACT_ENTITY_ID" + WHITESPACE + EQUAL);
 		query.append(OPENING_BRACKET);
 		query.append(SELECT_KEYWORD + WHITESPACE + IDENTIFIER);
 		query.append(WHITESPACE + FROM_KEYWORD + WHITESPACE + "dyextn_abstract_metadata"
 				+ WHITESPACE);
 		query.append(WHERE_KEYWORD + WHITESPACE + "NAME" + WHITESPACE + EQUAL + "'" + entityName
 				+ "'");
 		query.append(CLOSING_BRACKET);
 		query.append(CLOSING_BRACKET);
 		Logger.out.info("Query = " + query.toString());
 
 		ResultSet resultSet = null;
 		JDBCDAO jdbcDao = null;
 		try
 		{
 			Logger.out.info("Query = " + query.toString());
 			jdbcDao = DynamicExtensionsUtility.getJDBCDAO();
 			resultSet = jdbcDao.getQueryResultSet(query.toString());
 			if (resultSet != null)
 			{
 				resultSet.next();
 				String entTableName = resultSet.getString(NAME);
 				if (entTableName != null)
 				{
 					EntityManagerUtil entityManagerUtil = new EntityManagerUtil();
 					return entityManagerUtil.getNextIdentifier(entTableName);
 				}
 			}
 		}
 		catch (DAOException e)
 		{
 			Logger.out.debug(e.getMessage());
 		}
 		catch (SQLException e)
 		{
 			Logger.out.debug(e.getMessage());
 		}
 		finally
 		{
 			if (resultSet != null)
 			{
 				try
 				{
 					jdbcDao.closeStatement(resultSet);
 					DynamicExtensionsUtility.closeJDBCDAO(jdbcDao);
 				}
 				catch (DAOException e)
 				{
 					throw new DynamicExtensionsSystemException(e.getMessage(), e);
 				}
 			}
 		}
 
 		return null;
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.common.dynamicextensions.entitymanager.EntityManagerInterface#getAttribute(java.lang.String, java.lang.String)
 	 */
 	public AttributeInterface getAttribute(String entityName, String attributeName)
 			throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException
 	{
 		AttributeInterface attribute = null;
 		AbstractAttributeInterface abstractAttribute;
 		String name;
 		if (entityName == null || entityName.equals("") || attributeName == null
 				|| attributeName.equals(""))
 		{
 			return attribute;
 		}
 
 		EntityInterface entity = getEntityByName(entityName);
 		if (entity != null)
 		{
 			Collection<AbstractAttributeInterface> abstrAttributes = entity
 					.getAbstractAttributeCollection();
 			if (abstrAttributes != null)
 			{
 				Iterator<AbstractAttributeInterface> abstrAttrIter = abstrAttributes.iterator();
 				while (abstrAttrIter.hasNext())
 				{
 					abstractAttribute = abstrAttrIter.next();
 					if (abstractAttribute instanceof AttributeInterface)
 					{
 						attribute = (AttributeInterface) abstractAttribute;
 						name = attribute.getName();
 						if (name != null && name.equals(attributeName))
 						{
 							return attribute;
 						}
 					}
 				}
 			}
 		}
 
 		return attribute;
 	}
 
 	/**
 	 * @param entGroupId
 	 * @return
 	 * @throws DynamicExtensionsSystemException
 	 */
 	public Collection<NameValueBean> getEntityGroupBeanById(Long entGroupId)
 			throws DynamicExtensionsSystemException
 	{
 		Collection<NameValueBean> entGroupBeans = new ArrayList<NameValueBean>();
 		Object[] objects;
 
 		// Create a map of substitution parameters.
 		Map<String, NamedQueryParam> substParams = new HashMap<String, NamedQueryParam>();
 		substParams.put("0", new NamedQueryParam(DBTypes.LONG, entGroupId));
 
 		Collection groupBeans = executeHQL("getEntityGroupBeanById", substParams);
 		Iterator grpBeansIter = groupBeans.iterator();
 		while (grpBeansIter.hasNext())
 		{
 			objects = (Object[]) grpBeansIter.next();
 
 			NameValueBean nameValueBean = new NameValueBean();
 			nameValueBean.setName(objects[0]);
 			nameValueBean.setValue(objects[1]);
 
 			entGroupBeans.add(nameValueBean);
 		}
 
 		return entGroupBeans;
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.common.dynamicextensions.entitymanager.EntityManagerInterface#getAllEntityGroupBeans()
 	 */
 	public Collection<NameValueBean> getAllEntityGroupBeans()
 			throws DynamicExtensionsSystemException
 	{
 		Collection<NameValueBean> entGroupBeans = new ArrayList<NameValueBean>();
 		Object[] objects;
 
 		Collection groupBeans = executeHQL("getAllGroupBeans", new HashMap());
 		Iterator grpBeansIter = groupBeans.iterator();
 		while (grpBeansIter.hasNext())
 		{
 			objects = (Object[]) grpBeansIter.next();
 
 			NameValueBean nameValueBean = new NameValueBean();
 			nameValueBean.setName(objects[0]);
 			nameValueBean.setValue(objects[1]);
 
 			entGroupBeans.add(nameValueBean);
 		}
 
 		return entGroupBeans;
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.common.dynamicextensions.entitymanager.EntityManagerInterface#validateEntity(edu.common.dynamicextensions.domaininterface.EntityInterface)
 	 */
 	public boolean validateEntity(EntityInterface ent)
 			throws DynamicExtensionsApplicationException, DynamicExtensionsSystemException
 	{
 		Collection<EntityInterface> entities = ent.getEntityGroup().getEntityCollection();
 		for (EntityInterface entity : entities)
 		{
 			Entity entityObject = (Entity) ent;
 			if (entity.getId() == null)
 			{
 				DynamicExtensionsUtility.validateEntity(entity);
 			}
 			else
 			{
 				Entity dbaseCopy = null;
 				try
 				{
 					dbaseCopy = (Entity) DynamicExtensionsUtility.getCleanObject(Entity.class
 							.getCanonicalName(), entity.getId());
 				}
 				catch (DAOException e)
 				{
 					throw new DynamicExtensionsSystemException(e.getMessage(), e);
 				}
 				if (EntityManagerUtil.isParentChanged((Entity) entity, dbaseCopy))
 				{
 					checkParentChangeAllowed(entityObject);
 				}
 			}
 		}
 
 		return true;
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.common.dynamicextensions.entitymanager.EntityManagerInterface#getAttributeRecordsCount(java.lang.Long, java.lang.Long)
 	 */
 	public Collection<Integer> getAttributeRecordsCount(Long entityId, Long attributeId)
 			throws DynamicExtensionsSystemException
 	{
 		// Create a map of substitution parameters.
 		Map<String, NamedQueryParam> substParams = new HashMap<String, NamedQueryParam>();
 		substParams.put("0", new NamedQueryParam(DBTypes.LONG, entityId));
 		substParams.put("1", new NamedQueryParam(DBTypes.LONG, attributeId));
 
 		// The following method takes the name of the query and
 		// the actual values for the place holders as the parameters.
 		Collection records = executeHQLWithCleanSession("getAttributeRecords", substParams);
 
 		return records;
 	}
 
 	/**
 	 * This method executes the HQL query in a clean session.
 	 * @param queryName
 	 * @param substParams
 	 * @return
 	 * @throws DynamicExtensionsSystemException
 	 */
 	private Collection executeHQLWithCleanSession(String queryName,
 			Map<String, NamedQueryParam> substParams) throws DynamicExtensionsSystemException
 	{
 		Collection objects = new HashSet();
 		HibernateDAO hibernateDAO = null;
 		try
 		{
 			hibernateDAO = DynamicExtensionsUtility.getHibernateDAO();
 			objects = hibernateDAO.executeNamedQuery(queryName, substParams);
 		}
 		catch (DAOException e)
 		{
 			throw new DynamicExtensionsSystemException("Error while rolling back the session", e);
 		}
 		finally
 		{
 			try
 			{
 				DynamicExtensionsUtility.closeHibernateDAO(hibernateDAO);
 			}
 			catch (DAOException e)
 			{
 				throw new DynamicExtensionsSystemException(
 						"Exception occured while closing the session", e, DYEXTN_S_001);
 			}
 		}
 
 		return objects;
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.common.dynamicextensions.entitymanager.EntityManagerInterface#getContainerByEntityIdentifier(java.lang.Long)
 	 */
 	public ContainerInterface getContainerByEntityIdentifier(Long entityId)
 			throws DynamicExtensionsSystemException
 	{
 		ContainerInterface container = null;
 
 		// Create a map of substitution parameters.
 		Map<String, NamedQueryParam> substParams = new HashMap<String, NamedQueryParam>();
 		substParams.put("0", new NamedQueryParam(DBTypes.LONG, entityId));
 
 		Collection containers = executeHQL("getContainerOfEntity", substParams);
 		if (containers != null && !containers.isEmpty())
 		{
 			container = (ContainerInterface) containers.iterator().next();
 		}
 
 		return container;
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.common.dynamicextensions.entitymanager.EntityManagerInterface#getAssociationTree(java.lang.Long)
 	 */
 	public Collection<AssociationTreeObject> getAssociationTree(Long entGroupId)
 			throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException
 	{
 		Collection<AssociationTreeObject> assoTreeObjs = new HashSet<AssociationTreeObject>();
 		AssociationTreeObject assoTreeObject;
 
 		Collection<NameValueBean> groupBeans = getEntityGroupBeanById(entGroupId);
 		Iterator<NameValueBean> grpBeansIter = groupBeans.iterator();
 		while (grpBeansIter.hasNext())
 		{
 			assoTreeObject = processGroupBean(grpBeansIter.next());
 			assoTreeObjs.add(assoTreeObject);
 		}
 
 		return assoTreeObjs;
 	}
 
 	/**
 	 * @param groupBean
 	 * @return
 	 * @throws DynamicExtensionsSystemException
 	 * @throws DynamicExtensionsApplicationException
 	 */
 	private AssociationTreeObject processGroupBean(NameValueBean groupBean)
 			throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException
 	{
 		AssociationTreeObject assoTreeObject = new AssociationTreeObject(Long.valueOf(groupBean
 				.getValue()), groupBean.getName());
 
 		// Create a map of substitution parameters.
 		Map<String, NamedQueryParam> substParams = new HashMap<String, NamedQueryParam>();
 		substParams.put("0", new NamedQueryParam(DBTypes.LONG, assoTreeObject.getId()));
 
 		Object[] containerBeans;
 		AssociationTreeObject asTreeObjForCont;
 
 		Collection contBeans = executeHQL("getAllContainersBeansByEntityGroupId", substParams);
 		Iterator contBeansIter = contBeans.iterator();
 		while (contBeansIter.hasNext())
 		{
 			containerBeans = (Object[]) contBeansIter.next();
 			asTreeObjForCont = new AssociationTreeObject((Long) containerBeans[0],
 					(String) containerBeans[1]);
 			assoTreeObject.addAssociationTreeObject(asTreeObjForCont);
 		}
 
 		return assoTreeObject;
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.common.dynamicextensions.entitymanager.EntityManagerInterface#getFileAttributeRecordValueByRecordId(edu.common.dynamicextensions.domaininterface.AttributeInterface, java.lang.Long)
 	 */
 	public FileAttributeRecordValue getFileAttributeRecordValueByRecordId(
 			AttributeInterface attribute, Long recordId) throws DynamicExtensionsSystemException,
 			DynamicExtensionsApplicationException, DAOException, SQLException, IOException
 	{
 		EntityInterface entity = attribute.getEntity();
 		FileAttributeRecordValue fileRecordValue = new FileAttributeRecordValue();
 
 		String query = SELECT_KEYWORD + attribute.getColumnProperties().getName() + UNDERSCORE
 				+ FILE_NAME + COMMA + attribute.getColumnProperties().getName() + UNDERSCORE
 				+ CONTENT_TYPE + COMMA + attribute.getColumnProperties().getName() + FROM_KEYWORD
 				+ entity.getTableProperties().getName() + WHITESPACE + WHERE_KEYWORD + IDENTIFIER
 				+ EQUAL + recordId;
 
 		ResultSet resultSet = null;
 		JDBCDAO jdbcDAO = DynamicExtensionsUtility.getJDBCDAO();
 		try
 		{
 			resultSet = jdbcDAO.getQueryResultSet(query);
 			while (resultSet.next())
 			{
 				fileRecordValue.setFileName(resultSet.getString(attribute.getColumnProperties()
 						.getName()
 						+ UNDERSCORE + FILE_NAME));
 				fileRecordValue.setContentType(resultSet.getString(attribute.getColumnProperties()
 						.getName()
 						+ UNDERSCORE + CONTENT_TYPE));
 
 				Blob blob = resultSet.getBlob(attribute.getColumnProperties().getName());
 				byte[] byteArray = blob.getBytes(1, (int) blob.length());
 
 				fileRecordValue.setFileContent(byteArray);
 			}
 		}
 		finally
 		{
 			jdbcDAO.closeStatement(resultSet);
 			DynamicExtensionsUtility.closeJDBCDAO(jdbcDAO);
 		}
 
 		return fileRecordValue;
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.common.dynamicextensions.entitymanager.EntityManagerInterface#getCategoriesContainerIdFromHookEntity(java.lang.Long)
 	 */
 	public Collection<ContainerInterface> getCategoriesContainerIdFromHookEntity(Long hookEntityId)
 			throws DynamicExtensionsSystemException
 	{
 		// Create a map of substitution parameters.
 		Map<String, NamedQueryParam> substParams = new HashMap<String, NamedQueryParam>();
 		substParams.put("0", new NamedQueryParam(DBTypes.LONG, hookEntityId));
 
 		Collection containers = executeHQL("getCategoryContainerIdFromHookEntiy", substParams);
 
 		return containers;
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.common.dynamicextensions.entitymanager.EntityManagerInterface#getDynamicTableName(java.lang.Long)
 	 */
 	public String getDynamicTableName(Long containerId) throws DynamicExtensionsSystemException
 	{
 		String tableName = "";
 
 		// Create a map of substitution parameters.
 		Map<String, NamedQueryParam> substParams = new HashMap<String, NamedQueryParam>();
 		substParams.put("0", new NamedQueryParam(DBTypes.LONG, containerId));
 
 		Collection containers = executeHQL("getDynamicTableName", substParams);
 		if (containers != null && !containers.isEmpty())
 		{
 			tableName = (String) containers.iterator().next();
 		}
 
 		return tableName;
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.common.dynamicextensions.entitymanager.EntityManagerInterface#getDynamicEntitiesContainerIdFromHookEntity(java.lang.Long)
 	 */
 	public Collection<ContainerInterface> getDynamicEntitiesContainerIdFromHookEntity(
 			Long hookEntityId) throws DynamicExtensionsSystemException
 	{
 		// Create a map of substitution parameters.
 		Map<String, NamedQueryParam> substParams = new HashMap<String, NamedQueryParam>();
 		substParams.put("0", new NamedQueryParam(DBTypes.LONG, hookEntityId));
 
 		Collection containers = executeHQL("getFormsContainerIdFromHookEntiy", substParams);
 
 		return containers;
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.common.dynamicextensions.entitymanager.EntityManagerInterface#isCategory(java.lang.Long)
 	 */
 	public Long isCategory(Long containerId) throws DynamicExtensionsSystemException
 	{
 		Long contIdentifier = null;
 
 		// Create a map of substitution parameters.
 		Map<String, NamedQueryParam> substParams = new HashMap<String, NamedQueryParam>();
 		substParams.put("0", new NamedQueryParam(DBTypes.LONG, containerId));
 
 		Collection containers = executeHQL("isCategory", substParams);
 		if (containers != null && !containers.isEmpty())
 		{
 			contIdentifier = (Long) containers.iterator().next();
 		}
 
 		return contIdentifier;
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.common.dynamicextensions.entitymanager.EntityManagerInterface#getCategoryRootContainerId(java.lang.Long)
 	 */
 	public Long getCategoryRootContainerId(Long containerId)
 			throws DynamicExtensionsSystemException
 	{
 		Long contIdentifier = null;
 
 		// Create a map of substitution parameters.
 		Map<String, NamedQueryParam> substParams = new HashMap<String, NamedQueryParam>();
 		substParams.put("0", new NamedQueryParam(DBTypes.LONG, containerId));
 
 		Collection containers = executeHQL("getCategoryRootContainerId", substParams);
 		if (containers != null && !containers.isEmpty())
 		{
 			contIdentifier = (Long) containers.iterator().next();
 		}
 
 		return contIdentifier;
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.common.dynamicextensions.entitymanager.EntityManagerInterface#getColumnNameForAssociation(java.lang.Long, java.lang.Long)
 	 */
 	public String getColumnNameForAssociation(Long hookEntityId, Long containerId)
 			throws DynamicExtensionsSystemException
 	{
 		String colName = null;
 
 		// Create a map of substitution parameters.
 		Map<String, NamedQueryParam> substParams = new HashMap<String, NamedQueryParam>();
 		substParams.put("0", new NamedQueryParam(DBTypes.LONG, hookEntityId));
 		substParams.put("1", new NamedQueryParam(DBTypes.LONG, containerId));
 
 		Collection colNames = executeHQL("getColumnNameForAssociation", substParams);
 
 		if (colNames != null && !colNames.isEmpty())
 		{
 			colName = (String) colNames.iterator().next();
 		}
 
 		return colName;
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.common.dynamicextensions.entitymanager.EntityManagerInterface#persistEntityMetadataForAnnotation(edu.common.dynamicextensions.domaininterface.EntityInterface, boolean, boolean, edu.common.dynamicextensions.domaininterface.AssociationInterface)
 	 */
 	public EntityInterface persistEntityMetadataForAnnotation(EntityInterface entityObj,
 			boolean isDataTblPresent, boolean cpyDataTblState, AssociationInterface association)
 			throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException
 	{
 
 		HibernateDAO hibernateDAO = null;
 		try
 		{
 			hibernateDAO = DynamicExtensionsUtility.getHibernateDAO();
 			//Use an overloaded method for update the object
 			this.persistEntityMetadataForAnnotation(entityObj, isDataTblPresent, cpyDataTblState,
 					association, hibernateDAO);
 
 			// Committing the changes done in the hibernate session to the database.
 			hibernateDAO.commit();
 		}
 		catch (Exception e)
 		{
 			// If there is any exception while storing the meta data,
 			// we need to roll back the queries that were fired. 
 			// So calling the following method to do that.
 			//rollbackQueries(stack, entity, e, hibernateDAO);
 
 			if (e instanceof DynamicExtensionsApplicationException)
 			{
 				throw (DynamicExtensionsApplicationException) e;
 			}
 			else
 			{
 				throw new DynamicExtensionsSystemException(e.getMessage(), e);
 			}
 		}
 		finally
 		{
 			try
 			{
 				// In any case, after all the operations, hibernate session needs to be closed. 
 				// So this call has been added in the finally clause.
 				DynamicExtensionsUtility.closeHibernateDAO(hibernateDAO);
 			}
 			catch (DAOException e)
 			{
 				// If there is any exception while storing the meta data, 
 				// we need to roll back the queries that were fired. So calling the 
 				// following method to do that.
 				//rollbackQueries(stack, entity, e, hibernateDAO);
 				Logger.out.error("The cause of the exception is - " + e.getMessage());
 			}
 		}
 
 		logDebug("persistEntity", "exiting the method");
 
 		return entityObj;
 	}
 
 	/**
 	 * This method is overloaded to take hibernatedao as argument ,it will be called from importxmi
 	 * @param entityObj
 	 * @param isDataTblPresent
 	 * @param cpyDataTblState
 	 * @param association
 	 * @param hibernateDAO
 	 * @return
 	 * @throws DynamicExtensionsSystemException
 	 * @throws DynamicExtensionsApplicationException
 	 */
 	public EntityInterface persistEntityMetadataForAnnotation(EntityInterface entityObj,
 			boolean isDataTblPresent, boolean cpyDataTblState, AssociationInterface association,
 			HibernateDAO hibernateDAO) throws DynamicExtensionsSystemException,
 			DynamicExtensionsApplicationException
 	{
 		Entity entity = (Entity) entityObj;
 		if (isDataTblPresent)
 		{
 			((Entity) entityObj).setDataTableState(DATA_TABLE_STATE_ALREADY_PRESENT);
 		}
 		else
 		{
 			((Entity) entityObj).setDataTableState(DATA_TABLE_STATE_NOT_CREATED);
 		}
 		try
 		{
 			hibernateDAO.update(entity);
 
 		}
 		catch (Exception e)
 		{
 			// If there is any exception while storing the meta data,
 			// we need to roll back the queries that were fired. 
 			// So calling the following method to do that.
 			//rollbackQueries(stack, entity, e, hibernateDAO);
 
 			if (e instanceof DynamicExtensionsApplicationException)
 			{
 				throw (DynamicExtensionsApplicationException) e;
 			}
 			else
 			{
 				throw new DynamicExtensionsSystemException(e.getMessage(), e);
 			}
 		}
 
 		logDebug("persistEntity", "exiting the method");
 
 		return entityObj;
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.common.dynamicextensions.entitymanager.EntityManagerInterface#getMainContainer(java.lang.Long)
 	 */
 	public Collection<NameValueBean> getMainContainer(Long entGroupId)
 			throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException
 	{
 		// Create a map of substitution parameters.
 		Map<String, NamedQueryParam> substParams = new HashMap<String, NamedQueryParam>();
 		substParams.put("0", new NamedQueryParam(DBTypes.LONG, entGroupId));
 
 		return executeHQL("getMainContainers", substParams);
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.common.dynamicextensions.entitymanager.EntityManagerInterface#getEntityGroupByName(java.lang.String)
 	 */
 	public EntityGroupInterface getEntityGroupByName(String entGroupName)
 			throws DynamicExtensionsSystemException
 	{
 		EntityGroupInterface entityGroup = (EntityGroupInterface) getObjectByName(EntityGroup.class
 				.getName(), entGroupName);
 
 		return entityGroup;
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.common.dynamicextensions.entitymanager.EntityManagerInterface#getAllEntitiyGroups()
 	 */
 	public Collection<EntityGroupInterface> getAllEntitiyGroups()
 			throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException
 	{
 		return getAllObjects(EntityGroupInterface.class.getName());
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.common.dynamicextensions.entitymanager.EntityManagerInterface#getControlByAbstractAttributeIdentifier(java.lang.Long)
 	 */
 	public ControlInterface getControlByAbstractAttributeIdentifier(Long abstrAttrId)
 			throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException
 	{
 		ControlInterface control = null;
 
 		// Create a map of substitution parameters.
 		Map<String, NamedQueryParam> substParams = new HashMap<String, NamedQueryParam>();
 		substParams.put("0", new NamedQueryParam(DBTypes.LONG, abstrAttrId));
 
 		Collection controls = executeHQL("getControlOfAbstractAttribute", substParams);
 		if (controls != null && !controls.isEmpty())
 		{
 			control = (ControlInterface) controls.iterator().next();
 		}
 
 		return control;
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.common.dynamicextensions.entitymanager.EntityManagerInterface#updateAttributeTypeInfo(edu.common.dynamicextensions.domaininterface.AttributeTypeInformationInterface)
 	 */
 	public AttributeTypeInformationInterface updateAttributeTypeInfo(
 			AttributeTypeInformationInterface attrTypeInfo)
 			throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException
 	{
 		HibernateDAO hibernateDAO = null;
 		try
 		{
 			hibernateDAO = DynamicExtensionsUtility.getHibernateDAO();
 
 			hibernateDAO.update(attrTypeInfo);
 
 			hibernateDAO.commit();
 		}
 		catch (DAOException e)
 		{
 			throw new DynamicExtensionsSystemException(e.getMessage(), e, DYEXTN_S_003);
 		}
 		finally
 		{
 			try
 			{
 				DynamicExtensionsUtility.closeHibernateDAO(hibernateDAO);
 			}
 			catch (DAOException e)
 			{
 				throw new DynamicExtensionsSystemException(e.getMessage(), e, DYEXTN_S_003);
 			}
 		}
 
 		return attrTypeInfo;
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.common.dynamicextensions.entitymanager.EntityManagerInterface#getEntityGroupId(java.lang.String)
 	 */
 	public Long getEntityGroupId(String entGroupName) throws DynamicExtensionsSystemException
 	{
 		Long entGroupId = null;
 
 		// Create a map of substitution parameters.
 		Map<String, NamedQueryParam> substParams = new HashMap<String, NamedQueryParam>();
 		substParams.put("0", new NamedQueryParam(DBTypes.STRING, entGroupName));
 
 		Collection entGrpIds = executeHQL("getEntityGroupId", substParams);
 		if (entGrpIds != null && !entGrpIds.isEmpty())
 		{
 			entGroupId = (Long) entGrpIds.iterator().next();
 		}
 
 		return entGroupId;
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.common.dynamicextensions.entitymanager.EntityManagerInterface#getEntityId(java.lang.String, java.lang.Long)
 	 */
 	public Long getEntityId(String entityName, Long entGroupId)
 			throws DynamicExtensionsSystemException
 	{
 		Long entityId = null;
 
 		// Create a map of substitution parameters.
 		Map<String, NamedQueryParam> substParams = new HashMap<String, NamedQueryParam>();
 		substParams.put("0", new NamedQueryParam(DBTypes.LONG, entGroupId));
 		substParams.put("1", new NamedQueryParam(DBTypes.STRING, entityName));
 
 		Collection entityIds = executeHQL("getEntityId", substParams);
 		if (entityIds != null && !entityIds.isEmpty())
 		{
 			entityId = (Long) entityIds.iterator().next();
 		}
 
 		return entityId;
 	}
 
 	/**
 	 * @param attrName name of attribute
 	 * @param entityId identifier of entity
 	 * @return attributeId form entity based on attribute name
 	 * @throws DynamicExtensionsSystemException fails to get attributeId form entity based on attribute name
 	 */
 	public Long getAttributeId(String attrName, Long entityId)
 			throws DynamicExtensionsSystemException
 	{
 		Long attrId = null;
 
 		// Create a map of substitution parameters.
 		Map<String, NamedQueryParam> substParams = new HashMap<String, NamedQueryParam>();
 		substParams.put("0", new NamedQueryParam(DBTypes.LONG, entityId));
 		substParams.put("1", new NamedQueryParam(DBTypes.STRING, attrName));
 
 		Collection attrIds = executeHQL("getAttributeId", substParams);
 		if (attrIds != null && !attrIds.isEmpty())
 		{
 			attrId = (Long) attrIds.iterator().next();
 		}
 
 		return attrId;
 	}
 
 	/**
 	 * @param attrId identifier of an attribute
 	 * @return AttributeTypeInformationInterface based on attribute id
 	 * @throws DynamicExtensionsSystemException fails to get AttributeTypeInformationInterface based on attribute Id
 	 */
 	public AttributeTypeInformationInterface getAttributeTypeInformation(Long attrId)
 			throws DynamicExtensionsSystemException
 	{
 		AttributeTypeInformationInterface attrTypeInfo = null;
 
 		// Create a map of substitution parameters.
 		Map<String, NamedQueryParam> substParams = new HashMap<String, NamedQueryParam>();
 		substParams.put("0", new NamedQueryParam(DBTypes.LONG, attrId));
 
 		Collection attrTypeInfos = executeHQL("getAttributeTypeObject", substParams);
 		if (attrTypeInfos != null && !attrTypeInfos.isEmpty())
 		{
 			attrTypeInfo = (AttributeTypeInformationInterface) attrTypeInfos.iterator().next();
 		}
 
 		return attrTypeInfo;
 	}
 
 	/**
 	 * @param contCaption Caption of container
 	 * @return containerId container id
 	 * @throws DynamicExtensionsSystemException fails to get containerId based on container caption
 	 */
 	public Long getContainerIdByCaption(String contCaption) throws DynamicExtensionsSystemException
 	{
 		Long containerId = null;
 
 		// Create a map of substitution parameters.
 		Map<String, NamedQueryParam> substParams = new HashMap<String, NamedQueryParam>();
 		substParams.put("0", new NamedQueryParam(DBTypes.STRING, contCaption));
 
 		Collection<Long> containerIds = executeHQL("getContainerIdByName", substParams);
 		if (containerIds != null && !containerIds.isEmpty())
 		{
 			containerId = (Long) containerIds.iterator().next();
 		}
 
 		return containerId;
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.common.dynamicextensions.entitymanager.EntityManagerInterface#getAssociationAttributeId(java.lang.Long)
 	 */
 	public Long getAssociationAttributeId(Long attrId) throws DynamicExtensionsSystemException
 	{
 		Long assoAttrId = null;
 
 		// Create a map of substitution parameters.
 		Map<String, NamedQueryParam> substParams = new HashMap<String, NamedQueryParam>();
 		substParams.put("0", new NamedQueryParam(DBTypes.LONG, attrId));
 		substParams.put("1", new NamedQueryParam(DBTypes.BOOLEAN, true));
 
 		Collection tgtEntityIds = executeHQL("getTargetEntityIdForCollAttribute", substParams);
 		if (tgtEntityIds != null && !tgtEntityIds.isEmpty())
 		{
 			Long targetEntityId = (Long) tgtEntityIds.iterator().next();
 			if (targetEntityId != null)
 			{
 				Map<String, NamedQueryParam> subParams = new HashMap<String, NamedQueryParam>();
 				subParams.put("0", new NamedQueryParam(DBTypes.LONG, targetEntityId));
 				subParams.put("1", new NamedQueryParam(DBTypes.STRING,
 						DEConstants.COLLECTIONATTRIBUTE + "%"));
 				subParams.put("2", new NamedQueryParam(DBTypes.STRING,
 						DEConstants.COLLECTIONATTRIBUTE_OLD + "%"));
 
 				Collection attributeIds = executeHQL("getMultiSelAttrId", subParams);
 				if (attributeIds != null && !attributeIds.isEmpty())
 				{
 					assoAttrId = (Long) attributeIds.iterator().next();
 				}
 			}
 		}
 
 		return assoAttrId;
 	}
 
 	/**
 	 * @param categoryEntityId
 	 * @return
 	 * @throws DynamicExtensionsSystemException
 	 */
 	public Long getEntityIdByCategorEntityId(Long categoryEntityId)
 			throws DynamicExtensionsSystemException
 	{
 		Long containerId = null;
 		Map<String, NamedQueryParam> substitutionParameterMap = new HashMap<String, NamedQueryParam>();
 		substitutionParameterMap.put("0", new NamedQueryParam(DBTypes.LONG, categoryEntityId));
 		Collection<Long> containerIdCollection = executeHQL("getEntityIdByCategoryEntityId",
 				substitutionParameterMap);
 		if (containerIdCollection != null && !containerIdCollection.isEmpty())
 		{
 			containerId = (Long) containerIdCollection.iterator().next();
 		}
 
 		return containerId;
 
 	}
 
 	/**
 	 * @return SystemGenerated EntityGroup beans
 	 * @throws DynamicExtensionsSystemException
 	 */
 	public Collection<NameValueBean> getAllSystemGenEntityGroupBeans()
 			throws DynamicExtensionsSystemException
 	{
 		Collection<NameValueBean> entGroupBeans = new ArrayList<NameValueBean>();
 		Object[] objects;
 
 		Collection groupBeans = executeHQL("getAllSystemGenGroupBeans", new HashMap());
 		Iterator grpBeansIter = groupBeans.iterator();
 		while (grpBeansIter.hasNext())
 		{
 			objects = (Object[]) grpBeansIter.next();
 
 			NameValueBean nameValueBean = new NameValueBean();
 			nameValueBean.setName(objects[0]);
 			nameValueBean.setValue(objects[1]);
 
 			entGroupBeans.add(nameValueBean);
 		}
 
 		return entGroupBeans;
 	}
 
 	/**
 	 * @param staticRecordId Collection Protocol Id
 	 * @return entityMapCondition for the given staticRecordId
 	 * @throws DynamicExtensionsSystemException
 	 */
 	public Collection<EntityMapCondition> getAllConditionsByStaticRecordId(Long staticRecordId)
 			throws DynamicExtensionsSystemException
 	{
 		// Create a map of substitution parameters.
 		Map<String, NamedQueryParam> substParams = new HashMap<String, NamedQueryParam>();
 		substParams.put("0", new NamedQueryParam(DBTypes.LONG, staticRecordId));
 
 		Collection<EntityMapCondition> entityMapCondition = executeHQL(
 				"getAllEntityMapConditionByStaticRecordId", substParams);
 
 		return entityMapCondition;
 	}
 
 	/**
 	 * @param entityName to get entityGroup
 	 * @return entityGroupName of a particular entity
 	 * @throws DynamicExtensionsSystemException
 	 */
 	public String getEntityGroupNameByEntityName(String entityName)
 			throws DynamicExtensionsSystemException
 	{
 		String entityGroupName = null;
 		// Create a map of substitution parameters.
 		Map<String, NamedQueryParam> substParams = new HashMap<String, NamedQueryParam>();
 		substParams.put("0", new NamedQueryParam(DBTypes.STRING, entityName));
 
 		// The following method takes the name of the query and
 		// the actual values for the place holders as the parameters.
 		Collection groupName = executeHQL("getEntityGroupNameByEntityName", substParams);
 		if (groupName != null && !groupName.isEmpty())
 		{
 			entityGroupName = groupName.iterator().next().toString();
 		}
 		return entityGroupName;
 	}
 
 	/**
 	 * @param pathAssociationRelationId  identifier of pathAssociationRelationId
 	 * @return association Id based on pathAssociationRelationId
 	 * @throws DynamicExtensionsSystemException
 	 */
 	public Long getAssociationIdFrmPathAssoRelationId(Long pathAssociationRelationId)
 			throws DynamicExtensionsSystemException
 	{
 		// Create a map of substitution parameters.
 		Map<String, NamedQueryParam> substParams = new HashMap<String, NamedQueryParam>();
 		substParams.put("0", new NamedQueryParam(DBTypes.LONG, pathAssociationRelationId));
 		Collection<Long> associationColl = null;
 		associationColl = executeHQL("getAssoIdFrmPathAssoRelationId", substParams);
 		if (associationColl != null && !associationColl.isEmpty())
 		{
 			return associationColl.iterator().next();
 		}
 		return null;
 	}
 
 	/**
 	 * @param pathId identifier of Path
 	 * @return collection of PathAssociationRelationIds
 	 * @throws DynamicExtensionsSystemException
 	 */
 	public Collection<Long> getPathAssociationRelationIds(Long pathId)
 			throws DynamicExtensionsSystemException
 	{
 		// Create a map of substitution parameters.
 		Map<String, NamedQueryParam> substParams = new HashMap<String, NamedQueryParam>();
 		substParams.put("0", new NamedQueryParam(DBTypes.LONG, pathId));
 		Collection<Long> pathAssoRelationIdCollection = null;
 		pathAssoRelationIdCollection = executeHQL("getPathAssociationRelationIdCollection",
 				substParams);
 		return pathAssoRelationIdCollection;
 	}
 
 	/**
 	 * @param associationId  identifier of association
 	 * @return source entity name
 	 * @throws DynamicExtensionsSystemException
 	 */
 	public String getSrcEntityNameFromAssociationId(Long associationId)
 			throws DynamicExtensionsSystemException
 	{
 		// Create a map of substitution parameters.
 		Map<String, NamedQueryParam> substParams = new HashMap<String, NamedQueryParam>();
 		substParams.put("0", new NamedQueryParam(DBTypes.LONG, associationId));
 		Collection<String> srcEntityNameColl = null;
 		srcEntityNameColl = executeHQL("getSrcEntityNameFromAssociationId", substParams);
 		if (srcEntityNameColl != null && !srcEntityNameColl.isEmpty())
 		{
 			return srcEntityNameColl.iterator().next();
 		}
 		return null;
 	}
 
 	/**
 	 * @param associationId  identifier of association
 	 * @return target entity name
 	 * @throws DynamicExtensionsSystemException
 	 */
 	public String getTgtEntityNameFromAssociationId(Long associationId)
 			throws DynamicExtensionsSystemException
 	{
 		// Create a map of substitution parameters.
 		Map<String, NamedQueryParam> substParams = new HashMap<String, NamedQueryParam>();
 		substParams.put("0", new NamedQueryParam(DBTypes.LONG, associationId));
 		Collection<String> tgtEntityNameColl = null;
 		tgtEntityNameColl = executeHQL("getTgtEntityNameFromAssociationId", substParams);
 		if (tgtEntityNameColl != null && !tgtEntityNameColl.isEmpty())
 		{
 			return tgtEntityNameColl.iterator().next();
 		}
 		return null;
 	}
 
 	/**
 	 * @param associationRelationId  PathAssociationRelationId
 	 * @return instanceId of the source categoryEntity
 	 * @throws DynamicExtensionsSystemException
 	 */
 	public Long getSrcInstanceIdFromAssociationRelationId(Long associationRelationId)
 			throws DynamicExtensionsSystemException
 	{
 		// Create a map of substitution parameters.
 		Map<String, NamedQueryParam> substParams = new HashMap<String, NamedQueryParam>();
 		substParams.put("0", new NamedQueryParam(DBTypes.LONG, associationRelationId));
 		Collection<Long> srcInstanceIdColl = null;
 		srcInstanceIdColl = executeHQL("getSrcInstanceIdFromAssociationRelationId", substParams);
 		if (srcInstanceIdColl != null && !srcInstanceIdColl.isEmpty())
 		{
 			return srcInstanceIdColl.iterator().next();
 		}
 		return null;
 	}
 
 	/**
 	 * @param associationRelationId PathAssociationRelationId
 	 * @return instanceId of the target categoryEntity
 	 * @throws DynamicExtensionsSystemException
 	 */
 	public Long getTgtInstanceIdFromAssociationRelationId(Long associationRelationId)
 			throws DynamicExtensionsSystemException
 	{
 		// Create a map of substitution parameters.
 		Map<String, NamedQueryParam> substParams = new HashMap<String, NamedQueryParam>();
 		substParams.put("0", new NamedQueryParam(DBTypes.LONG, associationRelationId));
 		Collection<Long> tgtInstanceIdColl = null;
 		tgtInstanceIdColl = executeHQL("getTgtInstanceIdFromAssociationRelationId", substParams);
 		if (tgtInstanceIdColl != null && !tgtInstanceIdColl.isEmpty())
 		{
 			return tgtInstanceIdColl.iterator().next();
 		}
 		return null;
 	}
 
 	/**
 	 * @return categoryEntityId collection
 	 * @throws DynamicExtensionsSystemException
 	 */
 	public Collection<Long> getAllCategoryEntityId() throws DynamicExtensionsSystemException
 	{
 		Collection<Long> caegoryEntityIdCollection = null;
 		caegoryEntityIdCollection = executeHQL("getAllCategoryEntityId", new HashMap());
 		return caegoryEntityIdCollection;
 	}
 
 	/**
 	 * @param categoryId identifier of category entity
 	 * @return name of category
 	 * @throws DynamicExtensionsSystemException
 	 */
 	public String getCategoryEntityNameByCategoryEntityId(Long categoryId)
 			throws DynamicExtensionsSystemException
 	{
 		// Create a map of substitution parameters.
 		Map<String, NamedQueryParam> substParams = new HashMap<String, NamedQueryParam>();
 		substParams.put("0", new NamedQueryParam(DBTypes.LONG, categoryId));
 		Collection<String> rootCategoryEntityName = null;
 		rootCategoryEntityName = executeHQL("getCategoryEntityNameByCategoryEntityId", substParams);
 		if (rootCategoryEntityName != null && !rootCategoryEntityName.isEmpty())
 		{
 			return (String) rootCategoryEntityName.iterator().next();
 		}
 		return null;
 	}
 
 }
