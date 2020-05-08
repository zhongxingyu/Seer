 
 package edu.common.dynamicextensions.entitymanager;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.Stack;
 
 import org.hibernate.HibernateException;
 
 import edu.common.dynamicextensions.domain.AbstractAttribute;
 import edu.common.dynamicextensions.domain.Association;
 import edu.common.dynamicextensions.domain.Attribute;
 import edu.common.dynamicextensions.domain.AttributeRecord;
 import edu.common.dynamicextensions.domain.DateAttributeTypeInformation;
 import edu.common.dynamicextensions.domain.Entity;
 import edu.common.dynamicextensions.domain.FileAttributeRecordValue;
 import edu.common.dynamicextensions.domain.FileAttributeTypeInformation;
 import edu.common.dynamicextensions.domain.ObjectAttributeTypeInformation;
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
 import edu.common.dynamicextensions.domaininterface.databaseproperties.TablePropertiesInterface;
 import edu.common.dynamicextensions.domaininterface.userinterface.AssociationControlInterface;
 import edu.common.dynamicextensions.domaininterface.userinterface.ContainerInterface;
 import edu.common.dynamicextensions.exception.DynamicExtensionsApplicationException;
 import edu.common.dynamicextensions.exception.DynamicExtensionsSystemException;
 import edu.common.dynamicextensions.util.DynamicExtensionsUtility;
 import edu.common.dynamicextensions.util.global.Constants;
 import edu.common.dynamicextensions.util.global.Constants.AssociationType;
 import edu.wustl.common.beans.NameValueBean;
 import edu.wustl.common.beans.SessionDataBean;
 import edu.wustl.common.dao.AbstractDAO;
 import edu.wustl.common.dao.DAOFactory;
 import edu.wustl.common.dao.HibernateDAO;
 import edu.wustl.common.dao.JDBCDAO;
 import edu.wustl.common.dao.JDBCDAOImpl;
 import edu.wustl.common.exception.BizLogicException;
 import edu.wustl.common.security.exceptions.UserNotAuthorizedException;
 import edu.wustl.common.util.dbManager.DAOException;
 import edu.wustl.common.util.dbManager.DBUtil;
 import edu.wustl.common.util.logger.Logger;
 
 /**
  *
  * @author mandar_shidhore
  *
  */
 public class NewEntityManager extends AbstractMetadataManager implements NewEntityManagerInterface
 {
 
 	/**
 	 * Static instance of the entity manager.
 	 */
 	private static NewEntityManagerInterface entityManager = null;
 
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
 	protected NewEntityManager()
 	{
 	}
 
 	/**
 	 * Returns the instance of the Entity Manager.
 	 * @return entityManager singleton instance of the Entity Manager.
 	 */
 	public static synchronized NewEntityManagerInterface getInstance()
 	{
 		if (entityManager == null)
 		{
 			entityManager = new NewEntityManager();
 			DynamicExtensionsUtility.initialiseApplicationVariables();
 			queryBuilder = QueryBuilderFactory.getQueryBuilder();
 		}
 
 		return entityManager;
 	}
 
 	/**
 	 * Mock entity manager can be placed in the entity manager using this method.
 	 * @param entityManager
 	 */
 	public static void setInstance(NewEntityManagerInterface entityManagerInterface)
 	{
 		NewEntityManager.entityManager = entityManagerInterface;
 
 	}
 
 	/**
 	 * Saves the entity into the database.Also prepares the dynamic tables and associations
 	 * between those tables using the metadata information in the entity object.
 	 * EntityInterface can be obtained from DomainObjectFactory.
 	 * @param entityInterface
 	 * @return
 	 * @throws DynamicExtensionsSystemException
 	 * @throws DynamicExtensionsApplicationException
 	 */
 	public EntityInterface persistEntity(EntityInterface entity)
 			throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException
 	{
 		List reverseQueryList = new LinkedList();
 		List queryList = new ArrayList();
 		Stack rollbackQueryStack = new Stack();
 		HibernateDAO hibernateDAO = (HibernateDAO) DAOFactory.getInstance().getDAO(
 				Constants.HIBERNATE_DAO);
 		try
 		{
 			hibernateDAO.openSession(null);
 
 			//preProcess(entity, queryList, hibernateDAO, reverseQueryList);
             preProcess(entity, reverseQueryList, hibernateDAO, queryList);
 
 			if (entity.getId() == null)
 			{
 				hibernateDAO.insert(entity, null, false, false);
 			}
 			else
 			{
 				hibernateDAO.update(entity, null, false, false, false);
 			}
 
 			postProcess(queryList, reverseQueryList, rollbackQueryStack);
 
 			hibernateDAO.commit();
 		}
 		catch (DAOException e)
 		{
 			rollbackQueries(rollbackQueryStack, entity, e, hibernateDAO);
 			throw new DynamicExtensionsSystemException(
 					"DAOException occured while opening a session to save the container.", e);
 		}
 		catch (UserNotAuthorizedException e)
 		{
 			rollbackQueries(rollbackQueryStack, entity, e, hibernateDAO);
 			e.printStackTrace();
 			throw new DynamicExtensionsSystemException(
 					"DAOException occured while opening a session to save the container.", e);
 		}
 		finally
 		{
 			try
 			{
 				hibernateDAO.closeSession();
 			}
 			catch (DAOException e)
 			{
 				throw new DynamicExtensionsSystemException(
 						"DAOException occured while opening a session to save the container.", e);
 			}
 		}
 		return entity;
 	}
 
 	/**
 	 * @see edu.common.dynamicextensions.entitymanager.EntityManagerInterface#persistEntityMetadata(edu.common.dynamicextensions.domaininterface.EntityInterface)
 	 */
 	public EntityInterface persistEntityMetadata(EntityInterface entity)
 			throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException
 	{
 		Stack rollbackQueryStack = new Stack();
 		HibernateDAO hibernateDAO = (HibernateDAO) DAOFactory.getInstance().getDAO(
 				Constants.HIBERNATE_DAO);
 		try
 		{
 			hibernateDAO.openSession(null);
 
 			if (entity.getId() == null)
 			{
 				hibernateDAO.insert(entity, null, false, false);
 			}
 			else
 			{
 				hibernateDAO.update(entity, null, false, false, false);
 			}
 
 			hibernateDAO.commit();
 		}
 		catch (DAOException e)
 		{
 			rollbackQueries(rollbackQueryStack, entity, e, hibernateDAO);
 			throw new DynamicExtensionsSystemException(
 					"DAOException occured while opening a session to save the container.", e);
 		}
 		catch (UserNotAuthorizedException e)
 		{
 			rollbackQueries(rollbackQueryStack, entity, e, hibernateDAO);
 			throw new DynamicExtensionsSystemException(
 					"DAOException occured while opening a session to save the container.", e);
 		}
 		finally
 		{
 			try
 			{
 				hibernateDAO.closeSession();
 			}
 			catch (DAOException e)
 			{
 				throw new DynamicExtensionsSystemException(
 						"DAOException occured while opening a session to save the container.", e);
 			}
 		}
 		return entity;
 	}
 
 	/**
 	 * This method creates dynamic table queries for the entities within a group.
 	 * @param group EntityGroup
 	 * @param reverseQueryList List of queries to be executed in case any problem occurs at DB level.
 	 * @param hibernateDAO
 	 * @param queryList List of queries to be executed to created dynamicn tables.
 	 * @throws DynamicExtensionsSystemException
 	 * @throws DynamicExtensionsApplicationException
 	 */
 	protected void preProcess(
 			DynamicExtensionBaseDomainObjectInterface dynamicExtensionBaseDomainObject,
 			List reverseQueryList, HibernateDAO hibernateDAO, List queryList)
 			throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException
 	{
 			EntityInterface entityObject = (EntityInterface) dynamicExtensionBaseDomainObject;
 			createDynamicQueries(entityObject, reverseQueryList, hibernateDAO, queryList);
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
 
 	/**
 	 * This method executes dynamic table queries created for all the entities within a group.
 	 * @param queryList List of queries to be executed to created dynamicn tables.
 	 * @param reverseQueryList List of queries to be executed in case any problem occurs at DB level.
 	 * @param rollbackQueryStack Stack to undo any changes done beforehand at DB level.
 	 * @throws DynamicExtensionsSystemException
 	 */
 	protected void postProcess(List queryList, List reverseQueryList, Stack rollbackQueryStack)
 			throws DynamicExtensionsSystemException
 	{
 		queryBuilder.executeQueries(queryList, reverseQueryList, rollbackQueryStack);
 	}
 
 	/**
 	 * createDynamicQueries.
 	 * @param entityInterface
 	 * @param reverseQueryList
 	 * @param hibernateDAO
 	 * @param queryList
 	 * @return
 	 * @throws DynamicExtensionsSystemException
 	 * @throws DynamicExtensionsApplicationException
 	 */
 	private List createDynamicQueries(EntityInterface entityInterface, List reverseQueryList,
 			HibernateDAO hibernateDAO, List queryList) throws DynamicExtensionsSystemException,
 			DynamicExtensionsApplicationException
 	{
 		return getDynamicQueryList(entityInterface.getEntityGroup(), reverseQueryList, hibernateDAO, queryList);
 	}
 	/**
 	 * getDynamicQueryList.
 	 */
 	public List getDynamicQueryList(EntityGroupInterface entityGroupInterface, List reverseQueryList,
 			HibernateDAO hibernateDAO, List queryList) throws DynamicExtensionsSystemException,
 			DynamicExtensionsApplicationException
 	{
         List<EntityInterface> entityList = DynamicExtensionsUtility
 				.getUnsavedEntities(entityGroupInterface);
 
 		for (EntityInterface entityObject : entityList)
 		{
 			List createQueryList = queryBuilder.getCreateEntityQueryList((Entity) entityObject,
 					reverseQueryList, hibernateDAO);
 
 			if (createQueryList != null && !createQueryList.isEmpty())
 			{
 				queryList.addAll(createQueryList);
 			}
 		}
 		for (EntityInterface entityObject : entityList)
 		{
 			List createQueryList = queryBuilder.getUpdateEntityQueryList((Entity) entityObject,
 					reverseQueryList, hibernateDAO);
 
 			if (createQueryList != null && !createQueryList.isEmpty())
 			{
 				queryList.addAll(createQueryList);
 			}
 		}
         List<EntityInterface> savedEntityList = DynamicExtensionsUtility
 		.getSavedEntities(entityGroupInterface);
 
 		for (EntityInterface savedEntity : savedEntityList)
 		{
 			Entity databaseCopy = (Entity) DBUtil.loadCleanObj(Entity.class, savedEntity
 					.getId());
 
 			List updateQueryList = queryBuilder.getUpdateEntityQueryList((Entity) savedEntity,
 					(Entity) databaseCopy, reverseQueryList);
 
 			if (updateQueryList != null && !updateQueryList.isEmpty())
 			{
 				queryList.addAll(updateQueryList);
 			}
 		}
 		return queryList;
 	}
 
 	/**
 	 * This method is called when there any exception occurs while generating the data table queries
 	 * for the entity. Valid scenario is that if we need to fire Q1 Q2 and Q3 in order to create the
 	 * data tables and Q1 Q2 get fired successfully and exception occurs while executing query Q3 then
 	 * this method receives the query list which holds the set of queries which negate the effect of the
 	 * queries which were generated successfully so that the metadata information and database are in
 	 * synchronisation.
 	 * @param reverseQueryList List of queries to be executed in case any problem occurs at DB level.
 	 * @param entity Entity
 	 * @param e Exception encountered
 	 * @param dao AbstractDAO
 	 * @throws DynamicExtensionsSystemException
 	 */
 	private void rollbackQueries(Stack reverseQueryStack, EntityInterface entity, Exception e,
 			AbstractDAO dao) throws DynamicExtensionsSystemException
 	{
 		String message = "";
 		try
 		{
 			dao.rollback();
 		}
 		catch (DAOException e2)
 		{
 			logDebug("rollbackQueries", DynamicExtensionsUtility.getStackTrace(e));
 			DynamicExtensionsSystemException ex = new DynamicExtensionsSystemException(message, e);
 			ex.setErrorCode(DYEXTN_S_000);
 			throw ex;
 		}
 
 		if (reverseQueryStack != null && !reverseQueryStack.isEmpty())
 		{
 			Connection conn;
 			try
 			{
 				conn = DBUtil.getConnection();
 				while (!reverseQueryStack.empty())
 				{
 					String query = (String) reverseQueryStack.pop();
 					PreparedStatement statement = null;
 					statement = conn.prepareStatement(query);
 					statement.executeUpdate();
 				}
 			}
 			catch (HibernateException e1)
 			{
 				message = e1.getMessage();
 			}
 			catch (SQLException exc)
 			{
 				message = exc.getMessage();
 				LogFatalError(exc, entity);
 			}
 			finally
 			{
 				logDebug("rollbackQueries", DynamicExtensionsUtility.getStackTrace(e));
 				DynamicExtensionsSystemException ex = new DynamicExtensionsSystemException(message,
 						e);
 				ex.setErrorCode(DYEXTN_S_000);
 				throw ex;
 			}
 		}
 	}
 
 	/**
 	 * This method is called when exception occurs while executing the rollback queries
 	 * or reverse queries. When this method is called , it signifies that the database state
 	 * and the metadata state for the entity are not in synchronisation and administrator
 	 * needs some database correction.
 	 * @param e The exception that took place.
 	 * @param entity Entity for which data tables are out of sync.
 	 */
 	protected void LogFatalError(Exception e, AbstractMetadataInterface abstractMetadata)
 	{
 		String table = "";
 		String name = "";
 		if (abstractMetadata != null)
 		{
 			EntityInterface entity = (EntityInterface) abstractMetadata;
 			entity.getTableProperties().getName();
 			name = entity.getName();
 		}
 		Logger.out
 				.error("***Fatal Error.. Incosistent data table and metadata information for the entity -"
 						+ name + "***");
 		Logger.out.error("Please check the table -" + table);
 		Logger.out.error("The cause of the exception is - " + e.getMessage());
 		Logger.out.error("The detailed log is : ");
 		e.printStackTrace();
 	}
 
 	/**
 	 * @see edu.common.dynamicextensions.entitymanager.EntityManagerInterface#getAssociations(java.lang.Long, java.lang.Long)
 	 */
 	public Collection<AssociationInterface> getAssociations(Long sourceEntityId, Long targetEntityId)
 			throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException
 	{
 		Map substitutionParameterMap = new HashMap();
 		substitutionParameterMap.put("0", new HQLPlaceHolderObject("long", sourceEntityId));
 		substitutionParameterMap.put("1", new HQLPlaceHolderObject("long", targetEntityId));
 		//Following method is called to execute the stored HQL , the name of which is given as the first parameter.
 		//The second parameter is the map which contains the actual values that are replaced for the placeholders.
 		Collection associationCollection = executeHQL("getAssociations", substitutionParameterMap);
 		return associationCollection;
 	}
 
 
 	/**
 	 * This method returns the EntityInterface given the entity name.
 	 * @param entityGroupShortName
 	 * @return
 	 */
 	public EntityInterface getEntityByName(String entityName)
 			throws DynamicExtensionsSystemException
 	{
 		EntityInterface entityInterface = (EntityInterface) getObjectByName(Entity.class.getName(),
 				entityName);
 		return entityInterface;
 	}
 
 	/**
 	 * Returns an association object given the entity name and source role name.
 	 * @param entityName
 	 * @param sourceRoleName
 	 * @return
 	 * @throws DynamicExtensionsSystemException
 	 * @throws DynamicExtensionsApplicationException
 	 */
 
 	public Collection<AssociationInterface> getAssociation(String entityName, String sourceRoleName)
 			throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException
 	{
 		Map substitutionParameterMap = new HashMap();
 		substitutionParameterMap.put("0", new HQLPlaceHolderObject("string", entityName));
 		substitutionParameterMap.put("1", new HQLPlaceHolderObject("string", sourceRoleName));
 		//Following method is called to execute the stored HQL , the name of which is given as the first parameter.
 		//The second parameter is the map which contains the actual values that are replaced for the placeholders.
 
 		Collection<AssociationInterface> associationCollection = executeHQL("getAssociation",
 				substitutionParameterMap);
 
 		return associationCollection;
 	}
 
 	/**
 	 * This method returns the collection of entities given the concept code for the entity.
 	 * @param entityConceptCode concept code for the entity
 	 * @return entityCollection a collection of entities.
 	 * @throws DynamicExtensionsSystemException
 	 * @throws DynamicExtensionsApplicationException
 	 */
 	public Collection<EntityInterface> getEntitiesByConceptCode(String entityConceptCode)
 			throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException
 	{
 		Map substitutionParameterMap = new HashMap();
 		substitutionParameterMap.put("0", new HQLPlaceHolderObject("string", entityConceptCode));
 		//Following method is called to execute the stored HQL , the name of which is given as the first parameter.
 		//The second parameter is the map which contains the actual values that are replaced for the placeholders.
 		Collection entityCollection = executeHQL("getEntitiesByConceptCode",
 				substitutionParameterMap);
 		return entityCollection;
 	}
 
 	/**
 	 * Returns all entities in the whole system
 	 * @return Collection Entity Collection
 	 * @throws DynamicExtensionsSystemException
 	 * @throws DynamicExtensionsApplicationException
 	 */
 	public Collection<EntityInterface> getAllEntities() throws DynamicExtensionsSystemException,
 			DynamicExtensionsApplicationException
 	{
 		//CAlling generic method to return all stored instances of the object, the class name of which is passed as
 		//the parameter.
 		return getAllObjects(EntityInterface.class.getName());
 	}
 
 	/**
 	 * @see edu.common.dynamicextensions.entitymanager.EntityManagerInterface#getEntityByIdentifier(java.lang.Long)
 	 */
 	public EntityInterface getEntityByIdentifier(Long id) throws DynamicExtensionsSystemException,
 			DynamicExtensionsApplicationException
 	{
 		//      CAlling generic method to return all stored instances of the object, the identifier of which is passed as
 		//the parameter.
 		return (EntityInterface) getObjectByIdentifier(EntityInterface.class.getName(), id
 				.toString());
 	}
 
 	/**
 	 * @see edu.common.dynamicextensions.entitymanager.EntityManagerInterface#getEntityByIdentifier(java.lang.String)
 	 */
 	public EntityInterface getEntityByIdentifier(String identifier)
 			throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException
 	{
 		//      CAlling generic method to return all stored instances of the object, the identifier of which is passed as
 		//the parameter.
 		return (EntityInterface) getObjectByIdentifier(EntityInterface.class.getName(), identifier);
 	}
 
 	/**
 	 * @see edu.common.dynamicextensions.entitymanager.EntityManagerInterface#getAllContainers()
 	 */
 	public Collection<ContainerInterface> getAllContainers()
 			throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException
 	{
 		//CAlling generic method to return all stored instances of the object, the class name of which is passed as
 		//the parameter.
 		return getAllObjects(ContainerInterface.class.getName());
 	}
 
 	/**
 	 * @see edu.common.dynamicextensions.entitymanager.EntityManagerInterface#insertData(edu.common.dynamicextensions.domaininterface.EntityInterface, java.util.Map)
 	 */
 	public Long insertData(EntityInterface entity, Map<AbstractAttributeInterface, ?> dataValue)
 			throws DynamicExtensionsApplicationException, DynamicExtensionsSystemException
 	{
 		List<Map<AbstractAttributeInterface, ?>> dataValueMapList = new ArrayList<Map<AbstractAttributeInterface, ?>>();
 		dataValueMapList.add(dataValue);
 		List<Long> recordIdList = insertData(entity, dataValueMapList);
 		return recordIdList.get(0);
 	}
 
 	/**
 	 * @see edu.common.dynamicextensions.entitymanager.EntityManagerInterface#insertData(edu.common.dynamicextensions.domaininterface.EntityInterface, java.util.Map)
 	 */
 	public List<Long> insertData(EntityInterface entity,
 			List<Map<AbstractAttributeInterface, ?>> dataValueMapList)
 			throws DynamicExtensionsApplicationException, DynamicExtensionsSystemException
 	{
 
 		List<Long> recordIdList = new ArrayList<Long>();
 		HibernateDAO hibernateDAO = null;
 		try
 		{
 			DAOFactory factory = DAOFactory.getInstance();
 			hibernateDAO = (HibernateDAO) factory.getDAO(Constants.HIBERNATE_DAO);
 			hibernateDAO.openSession(null);
 
 			for (Map<AbstractAttributeInterface, ?> dataValue : dataValueMapList)
 			{
 				Long recordId = insertDataForHeirarchy(entity, dataValue, hibernateDAO);
 				recordIdList.add(recordId);
 			}
 
 			hibernateDAO.commit();
 		}
 		catch (DynamicExtensionsApplicationException e)
 		{
 			throw (DynamicExtensionsApplicationException) handleRollback(e,
 					"Error while inserting data", hibernateDAO, false);
 		}
 		catch (Exception e)
 		{
 			throw (DynamicExtensionsSystemException) handleRollback(e,
 					"Error while inserting data", hibernateDAO, true);
 		}
 		finally
 		{
 			try
 			{
 				hibernateDAO.closeSession();
 			}
 			catch (DAOException e)
 			{
 				throw (DynamicExtensionsSystemException) handleRollback(e, "Error while closing",
 						hibernateDAO, true);
 			}
 		}
 
 		return recordIdList;
 	}
 
 	/**
 	 * @param entity
 	 * @param dataValue
 	 * @param hibernateDAO
 	 * @return
 	 * @throws DynamicExtensionsSystemException
 	 * @throws DynamicExtensionsApplicationException
 	 * @throws HibernateException
 	 * @throws SQLException
 	 * @throws DAOException
 	 * @throws UserNotAuthorizedException
 	 */
 	private Long insertDataForHeirarchy(EntityInterface entity,
 			Map<AbstractAttributeInterface, ?> dataValue, HibernateDAO hibernateDAO)
 			throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException,
 			HibernateException, SQLException, DAOException, UserNotAuthorizedException
 	{
 		List<EntityInterface> entityList = getParentEntityList(entity);
 		Map<EntityInterface, Map> entityValueMap = initialiseEntityValueMap(entity, dataValue);
 		Long parentRecordId = null;
 		for (EntityInterface entityInterface : entityList)
 		{
 			Map valueMap = entityValueMap.get(entityInterface);
 			parentRecordId = insertDataForSingleEntity(entityInterface, valueMap, hibernateDAO,
 					parentRecordId);
 		}
 
 		return parentRecordId;
 	}
 
 	/**
 	 * @param entity
 	 * @return
 	 */
 	private List<EntityInterface> getParentEntityList(EntityInterface entity)
 	{
 		List<EntityInterface> entityList = new ArrayList<EntityInterface>();
 		entityList.add(entity);
 		while (entity.getParentEntity() != null)
 		{
 			entityList.add(0, entity.getParentEntity());
 			entity = entity.getParentEntity();
 		}
 		return entityList;
 	}
 
 	/**
 	 * @param e
 	 * @param string
 	 * @param hibernateDAO
 	 * @throws DynamicExtensionsSystemException
 	 */
 	private Exception handleRollback(Exception e, String exceptionMessage, AbstractDAO dao,
 			boolean isExceptionToBeWrapped)
 	{
 		try
 		{
 			dao.rollback();
 		}
 		catch (DAOException e1)
 		{
 			return new DynamicExtensionsSystemException("error while rollback", e);
 		}
 
 		if (isExceptionToBeWrapped)
 		{
 			return new DynamicExtensionsSystemException(exceptionMessage, e);
 		}
 		else
 		{
 			return e;
 		}
 	}
 
 	/**
 	 * @param entity
 	 * @param dataValue
 	 * @return
 	 */
 	private Map<EntityInterface, Map> initialiseEntityValueMap(EntityInterface entity,
 			Map<AbstractAttributeInterface, ?> dataValue)
 	{
 		Map<EntityInterface, Map> entityMap = new HashMap<EntityInterface, Map>();
 
 		for (AbstractAttributeInterface abstractAttributeInterface : dataValue.keySet())
 		{
 			EntityInterface attributeEntity = abstractAttributeInterface.getEntity();
 			Object value = dataValue.get(abstractAttributeInterface);
 
 			Map<AbstractAttributeInterface, Object> entityDataValueMap = (Map) entityMap
 					.get(attributeEntity);
 			if (entityDataValueMap == null)
 			{
 				entityDataValueMap = new HashMap<AbstractAttributeInterface, Object>();
 				entityMap.put(attributeEntity, entityDataValueMap);
 			}
 			entityDataValueMap.put(abstractAttributeInterface, value);
 		}
 		return entityMap;
 	}
 
 	/**
 	 * @param entity
 	 * @param dataValue
 	 * @param hibernateDAO
 	 * @return
 	 * @throws DynamicExtensionsSystemException
 	 * @throws DynamicExtensionsApplicationException
 	 * @throws HibernateException
 	 * @throws SQLException
 	 * @throws DAOException
 	 * @throws UserNotAuthorizedException
 	 */
 	private Long insertDataForSingleEntity(EntityInterface entity, Map dataValue,
 			HibernateDAO hibernateDAO, Long parentRecordId)
 			throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException,
 			HibernateException, SQLException, DAOException, UserNotAuthorizedException
 	{
 		if (entity == null)
 		{
 			throw new DynamicExtensionsSystemException("Input to insert data is null");
 		}
 
 		// if empty, insert row with only identifer column value.
 		if (dataValue == null)
 		{
 			dataValue = new HashMap();
 		}
 
 		List<Object> columnValues = new ArrayList<Object>();
 		List<String> columnNames = new ArrayList<String>();
 
 
 		Long identifier = null;
 		if (parentRecordId != null)
 		{
 			identifier = parentRecordId;
 		}
 		else
 		{
 			identifier = entityManagerUtil.getNextIdentifier(entity.getTableProperties().getName());
 		}
 		columnNames.add("IDENTIFIER");
 		columnValues.add(identifier);
 
 		columnNames.add(Constants.ACTIVITY_STATUS_COLUMN);
 		columnValues.add(Constants.ACTIVITY_STATUS_ACTIVE);
 
 
 		String tableName = entity.getTableProperties().getName();
 
 		List<AttributeRecord> attributeRecords = new ArrayList<AttributeRecord>();
 
 		Set uiColumnSet = dataValue.keySet();
 		Iterator uiColumnSetIter = uiColumnSet.iterator();
 		List<String> queryList = new ArrayList<String>();
 		Object value = null;
 		while (uiColumnSetIter.hasNext())
 		{
 			AbstractAttribute attribute = (AbstractAttribute) uiColumnSetIter.next();
 			value = dataValue.get(attribute);
 
 			if (value == null)
 			{
 				continue;
 			}
 
 			if (attribute instanceof AttributeInterface)
 			{
 				AttributeInterface primitiveAttribute = (AttributeInterface) attribute;
 
 				// populate FileAttributeRecordValue HO
 				if (primitiveAttribute.getAttributeTypeInformation() instanceof FileAttributeTypeInformation)
 				{
 					populateFileAttribute(columnNames,columnValues,(FileAttributeRecordValue)value, primitiveAttribute);
 				}
 
 				else if (primitiveAttribute.getIsCollection())
 				{
 					//                    AttributeRecord collectionRecord = populateCollectionAttributeRecord(null,
 					//                            entity, primitiveAttribute, identifier, (List<String>) value);
 					//                    attributeRecords.add(collectionRecord);
 				}
 				else
 				{
 					columnNames.add(primitiveAttribute.getColumnProperties().getName());
 					columnValues.add(value);
 				}
 			}
 			else
 			{
 				//In case of association separate queries need to fire depending on the cardinalities
 				AssociationInterface association = (AssociationInterface) attribute;
 				List<Long> recordIdList = null;
 
 				if (association.getSourceRole().getAssociationsType().equals(
 						AssociationType.CONTAINTMENT))
 				{
 					List<Map> listOfMapsForContainedEntity = (List) value;
 					recordIdList = new ArrayList<Long>();
 
 					//Map valueMapForContainedEntity = (Map) value;
 					for (Map valueMapForContainedEntity : listOfMapsForContainedEntity)
 					{
 						//                      Long recordIdForContainedEntity = insertDataForSingleEntity(association
 						//                              .getTargetEntity(), valueMapForContainedEntity, hibernateDAO, null);
 
 						Long recordIdForContainedEntity = insertDataForHeirarchy(association
 								.getTargetEntity(), valueMapForContainedEntity, hibernateDAO);
 						recordIdList.add(recordIdForContainedEntity);
 					}
 
 				}
 				else
 				{
 					recordIdList = (List<Long>) value;
 				}
 
 				queryList.addAll(queryBuilder.getAssociationInsertDataQuery(association,
 						recordIdList, identifier));
 
 			}
 		}
 
 		JDBCDAOImpl jdbcDao = new JDBCDAOImpl();
 		jdbcDao.openSession(null);
 		jdbcDao.insert(tableName, columnValues, columnNames);
 		jdbcDao.commit();
 
 /*	ToDo: Correct this logger statement
  * 	logDebug("insertData", "Query is: " + query.toString());
  * */
 
 		Connection conn = DBUtil.getConnection();
 
 		for (String queryString : queryList)
 		{
 			logDebug("insertData", "Query for insert data is : " + queryString);
 			PreparedStatement statement = conn.prepareStatement(queryString);
 			statement.executeUpdate();
 		}
 
 		for (AttributeRecord collectionAttributeRecord : attributeRecords)
 		{
 			hibernateDAO.insert(collectionAttributeRecord, null, false, false);
 		}
 
 		return identifier;
 
 	}
 
 	/**
 	 * This method adds the extra columns information
 	 * that needs to be maintained while adding the file data
 	 * @param columnNames list of column names
 	 * @param columnValues list of column values
 	 * @param value file attribute value
 	 * @param attribute
 	 */
 	private void populateFileAttribute(List<String> columnNames, List<Object> columnValues, FileAttributeRecordValue value, AttributeInterface attribute)
 	{
 		columnNames.add(attribute.getName()+UNDERSCORE+FILE_NAME);
 		columnValues.add(value.getFileName());
 
 		columnNames.add(attribute.getName()+UNDERSCORE+CONTENT_TYPE);
 		columnValues.add(value.getContentType());
 
 		columnNames.add(attribute.getColumnProperties().getName());
 		columnValues.add(value.getFileContent());
		
 	}
 
 	/**
 	 * @see edu.common.dynamicextensions.entitymanager.EntityManagerInterface#editData(edu.common.dynamicextensions.domaininterface.EntityInterface, java.util.Map, java.lang.Long)
 	 */
 	public boolean editData(EntityInterface entity, Map<AbstractAttributeInterface, ?> dataValue,
 			Long recordId) throws DynamicExtensionsApplicationException,
 			DynamicExtensionsSystemException
 	{
 
 		boolean isSuccess = false;
 
 		HibernateDAO hibernateDAO = null;
 		try
 		{
 
 			DAOFactory factory = DAOFactory.getInstance();
 			hibernateDAO = (HibernateDAO) factory.getDAO(Constants.HIBERNATE_DAO);
 
 			hibernateDAO.openSession(null);
 			List<EntityInterface> entityList = getParentEntityList(entity);
 			Map<EntityInterface, Map> entityValueMap = initialiseEntityValueMap(entity, dataValue);
 			for (EntityInterface entityInterface : entityList)
 			{
 				Map valueMap = entityValueMap.get(entityInterface);
 				isSuccess = editDataForSingleEntity(entityInterface, valueMap, recordId,
 						hibernateDAO);
 			}
 			hibernateDAO.commit();
 		}
 		catch (DynamicExtensionsApplicationException e)
 		{
 			throw (DynamicExtensionsApplicationException) handleRollback(e,
 					"Error while inserting data", hibernateDAO, false);
 		}
 		catch (Exception e)
 		{
 			throw (DynamicExtensionsSystemException) handleRollback(e, "Error while updating",
 					hibernateDAO, true);
 		}
 		finally
 		{
 			try
 			{
 				hibernateDAO.closeSession();
 			}
 			catch (DAOException e)
 			{
 				throw (DynamicExtensionsSystemException) handleRollback(e, "Error while closing",
 						hibernateDAO, true);
 			}
 
 		}
 
 		return isSuccess;
 	}
 
 	/**
 	 * @param entity
 	 * @param dataValue
 	 * @param recordId
 	 * @return
 	 * @throws DynamicExtensionsApplicationException
 	 * @throws DynamicExtensionsSystemException
 	 * @throws BizLogicException
 	 * @throws BizLogicException
 	 */
 	private boolean editDataForSingleEntity(EntityInterface entity, Map dataValue, Long recordId,
 			HibernateDAO hibernateDAO) throws DynamicExtensionsSystemException,
 			DynamicExtensionsApplicationException, HibernateException, SQLException, DAOException,
 			UserNotAuthorizedException, BizLogicException
 	{
 
 		if (entity == null || dataValue == null || dataValue.isEmpty())
 		{
 			return true;
 		}
 		List<String> columnNames = new ArrayList<String>();
 		List<Object> columnValues = new ArrayList<Object>();
 
 		String tableName = entity.getTableProperties().getName();
 
 		Set uiColumnSet = dataValue.keySet();
 		Iterator uiColumnSetIter = uiColumnSet.iterator();
 		List associationRemoveDataQueryList = new ArrayList();
 		List associationInsertDataQueryList = new ArrayList();
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
 				AttributeInterface primitiveAttribute = (AttributeInterface) attribute;
 
 				if (primitiveAttribute.getIsCollection())
 				{
 					//                    // get previous values for multi select attributes
 					//                    AttributeRecord collectionRecord = getAttributeRecord(entity.getId(),
 					//                            primitiveAttribute.getId(), recordId, hibernateDAO);
 					//                    List<String> listOfValues = (List<String>) value;
 					//
 					//                    if (!listOfValues.isEmpty())
 					//                    { //if some values are provided,set these values clearing previous ones.
 					//                        collectionRecord = populateCollectionAttributeRecord(collectionRecord,
 					//                                entity, primitiveAttribute, recordId, (List<String>) value);
 					//                        collectionRecords.add(collectionRecord);
 					//                    }
 					//
 					//                    if (collectionRecord != null && listOfValues.isEmpty())
 					//                    {
 					//                        //if updated value is empty list, then delete previously saved value if any.
 					//                        deleteCollectionRecords.add(collectionRecord);
 					//                    }
 
 				}
 				else if (primitiveAttribute.getAttributeTypeInformation() instanceof FileAttributeTypeInformation)
 				{
 					populateFileAttribute(columnNames, columnValues, (FileAttributeRecordValue)value, primitiveAttribute);
 				}
 //				else if (primitiveAttribute.getAttributeTypeInformation() instanceof ObjectAttributeTypeInformation)
 //				{
 //					//For object type attribute,ObjectAttributeRecordValue needs to be updated for that record.
 //
 //					//                    ObjectAttributeRecordValue objectRecordValue = (ObjectAttributeRecordValue) value;
 //					//                    AttributeRecord objectRecord = getAttributeRecord(entity.getId(),
 //					//                            primitiveAttribute.getId(), recordId, hibernateDAO);
 //					//                    objectRecord.getObjectRecord().copyValues(objectRecordValue);
 //					//                    objectRecords.add(objectRecord);
 //				}
 				else
 				{
 					//for other attributes, create the udpate query.
 					columnNames.add(primitiveAttribute.getColumnProperties().getName());
 					columnValues.add(value);
 				}
 			}
 			else
 			{
 				AssociationInterface association = (AssociationInterface) attribute;
 				List<Long> recordIdList = new ArrayList<Long>();
 
 				if (association.getSourceRole().getAssociationsType().equals(
 						AssociationType.CONTAINTMENT))
 				{
 					List<String> removeContainmentRecordQuery = new ArrayList<String>();
 					recordIdList.add(recordId);
 
 					queryBuilder.getContenmentAssociationRemoveDataQueryList(
 							((Association) attribute), recordIdList, removeContainmentRecordQuery,
 							false);
 
 					entityManagerUtil.executeDML(removeContainmentRecordQuery);
 
 					List<Map> listOfMapsForContainedEntity = (List<Map>) value;
 					recordIdList.clear();
 					for (Map valueMapForContainedEntity : listOfMapsForContainedEntity)
 					{
 						//Long childRecordId = insertDataForSingleEntity(association
 						//.getTargetEntity(), valueMapForContainedEntity, hibernateDAO, null);
 						Long childRecordId = insertDataForHeirarchy(association.getTargetEntity(),
 								valueMapForContainedEntity, hibernateDAO);
 						recordIdList.add(childRecordId);
 					}
 
 				}
 				else
 				{
 					// for association need to remove previously associated target reocrd first.
 					String removeQuery = queryBuilder.getAssociationRemoveDataQuery(
 							((Association) attribute), recordId);
 
 					if (removeQuery != null && removeQuery.trim().length() != 0)
 					{
 						associationRemoveDataQueryList.add(removeQuery);
 					}
 
 					recordIdList = (List<Long>) value;
 				}
 
 				//then add new associated target records.
 				List insertQuery = queryBuilder.getAssociationInsertDataQuery(
 						((Association) attribute), recordIdList, recordId);
 				if (insertQuery != null && insertQuery.size() != 0)
 				{
 					associationInsertDataQueryList.addAll(insertQuery);
 				}
 
 			}
 		}
 
 		List<String> editDataQueryList = new ArrayList<String>();
 		editDataQueryList.addAll(associationRemoveDataQueryList);
 		editDataQueryList.addAll(associationInsertDataQueryList);
 
 		Connection conn = DBUtil.getConnection();
 		if (columnNames.size()!= 0)
 		{
 			StringBuffer query = new StringBuffer("UPDATE " + tableName + " SET ");
 			Iterator<String> iterator = columnNames.iterator();
 			while(iterator.hasNext()){
 				query.append(iterator.next() + EQUAL + "?");
 				if(iterator.hasNext()){
 					query.append(COMMA + WHITESPACE);
 				}
 
 			}
 			query.append(WHERE_KEYWORD);
 
 			query.append(IDENTIFIER);
 			query.append(WHITESPACE + EQUAL + WHITESPACE);
 			query.append(recordId);
 
 			PreparedStatement preparedStatement = conn.prepareStatement(query.toString());
 			int i = 1;
 			for(Object columnValue : columnValues){
 				preparedStatement.setObject(i++, columnValue);
 			}
 
 			preparedStatement.executeUpdate();
 
 		}
 
 
 
 
 
 		for (String queryString : editDataQueryList)
 		{
 			logDebug("editData", "Query is: " + queryString.toString());
 			PreparedStatement statement = conn.prepareStatement(queryString);
 			statement.executeUpdate();
 		}
 
 
 		return true;
 	}
 
 	/**
 	 * @see edu.common.dynamicextensions.entitymanager.EntityManagerInterface#getRecordById(edu.common.dynamicextensions.domaininterface.EntityInterface, java.lang.Long)
 	 * Value in the map depends on the type of the attribute as explaned below.<br>
 	 * Map
 	 *    key    - Attribute Name
 	 *    Value  - List<String> --           multiselect attribute.
 	 *             FileAttributeRecordValue  File attribute.
 	 *             List<Long>                Association
 	 *                  if One-One   |____   List will contain only 1 record id that is of target entity's record
 	 *                     Many-One  |
 	 *                  otherwise it will contains one or more reocrd ids.
 	 *
 	 *             String                    Other attribute type.
 	 */
 	private Map<AbstractAttributeInterface, Object> getEntityRecordById(EntityInterface entity,
 			Long recordId) throws DynamicExtensionsSystemException,
 			DynamicExtensionsApplicationException
 	{
 		Map<AbstractAttributeInterface, Object> recordValues = new HashMap<AbstractAttributeInterface, Object>();
 
 		Collection attributesCollection = entity.getAttributeCollection();
 		attributesCollection = entityManagerUtil.filterSystemAttributes(attributesCollection);
 
 
 		String tableName = entity.getTableProperties().getName();
 		List<String> selectColumnNameList = new ArrayList<String>();
 
 		Iterator attriIterator = attributesCollection.iterator();
 		Map columnNameMap = new HashMap();
 		while (attriIterator.hasNext())
 		{
 			AttributeInterface attribute = (AttributeInterface) attriIterator.next();
 
 			if (attribute.getIsCollection())
 			{ // need to fetch AttributeRecord object for the multi select type attribute.
 				//collectionAttributes.add(attribute);
 			}
 			else if (attribute.getAttributeTypeInformation() instanceof FileAttributeTypeInformation)
 			{
 				selectColumnNameList.add(attribute.getName()+UNDERSCORE+FILE_NAME);
 				columnNameMap.put(attribute.getName()+UNDERSCORE+FILE_NAME, attribute);
 			}
 			/*else if (attribute.getAttributeTypeInformation() instanceof ObjectAttributeTypeInformation)
 			{
 				// need to fetch AttributeRecord object for the File type attribute.
 				//objectAttributes.add(attribute);
 			}*/
 			else
 			{
 				//for the other attributes, create select query.
 
 				String dbColumnName = attribute.getColumnProperties().getName();
 				selectColumnNameList.add(dbColumnName);
 				columnNameMap.put(dbColumnName, attribute);
 			}
 		}
 
 		//get association values.
 		recordValues.putAll(queryBuilder.getAssociationGetRecordQueryList(entity, recordId));
 
 		try
 		{
 
 			if (!selectColumnNameList.isEmpty())
 			{
 
 				StringBuffer query = new StringBuffer();
 				query.append(SELECT_KEYWORD).append(WHITESPACE);
 
 				for (int i = 0; i < selectColumnNameList.size(); i++)
 				{
 					if (i != 0)
 					{
 						query.append(" , ");
 					}
 					query.append(selectColumnNameList.get(i));
 				}
 
 				query.append(WHITESPACE).append(FROM_KEYWORD).append(WHITESPACE).append(tableName)
 						.append(WHITESPACE).append(WHERE_KEYWORD).append(WHITESPACE).append(
 								IDENTIFIER).append(EQUAL).append(recordId);
 				/*get values for simple attributes*/
 
 				recordValues.putAll(getAttributeValues(selectColumnNameList, query.toString(),
 						columnNameMap));
 			}
 
 
 		}
 		catch (SQLException e)
 		{
 			throw new DynamicExtensionsSystemException("Error while retrieving the data", e);
 		}
 
 		return recordValues;
 	}
 
 	/**
 	 * @param selectColumnNameList
 	 * @param query
 	 * @param columnNameMap
 	 * @param multipleRows
 	 * @return
 	 * @throws DynamicExtensionsSystemException
 	 * @throws SQLException
 	 */
 	private Map<AbstractAttributeInterface, Object> getAttributeValues(
 			List<String> selectColumnNameList, String query, Map columnNameMap)
 			throws DynamicExtensionsSystemException, SQLException
 	{
 		Map<AbstractAttributeInterface, Object> recordValues = new HashMap<AbstractAttributeInterface, Object>();
 		ResultSet resultSet = entityManagerUtil.executeQuery(query);
 
 		if (resultSet.next())
 		{
 			for (int i = 0; i < selectColumnNameList.size(); i++)
 			{
 
 				String dbColumnName = selectColumnNameList.get(i);
 				Object value = getValueFromResultSet(resultSet, columnNameMap, dbColumnName, i);
 				Attribute attribute = (Attribute) columnNameMap.get(dbColumnName);
 				recordValues.put(attribute, value);
 			}
 		}
 		resultSet.close();
 		return recordValues;
 	}
 
 	private Object getValueFromResultSet(ResultSet resultSet, Map columnNameMap,
 			String dbColumnName, int index) throws SQLException
 	{
 		Attribute attribute = (Attribute) columnNameMap.get(dbColumnName);
 
 		Object valueObj = resultSet.getObject(index + 1);
 		Object value = "";
 
 		if (valueObj != null)
 		{
 			if (valueObj instanceof java.util.Date)
 			{
 
 				DateAttributeTypeInformation dateAttributeTypeInf = (DateAttributeTypeInformation) attribute
 						.getAttributeTypeInformation();
 
 				String format = dateAttributeTypeInf.getFormat();
 				if (format == null)
 				{
 					format = Constants.DATE_ONLY_FORMAT;
 				}
 
 				valueObj = resultSet.getTimestamp(index + 1);
 
 				SimpleDateFormat formatter = new SimpleDateFormat(format);
 				value = formatter.format((java.util.Date) valueObj);
 			}
 			else
 			{
 				value = valueObj;
 			}
 		}
 		return value;
 	}
 
 	/**
 	 * The method returns the entity records for the given entity, attribute and records.
 	 * @param entity Entity whose records are to be shown
 	 * @param abstractAttributeCollection The set of attributes for which values are to be shown
 	 * @param recordIds Record ids whose values are to be shown
 	 * @return EntityRecordResultInterface Object containing the result.
 	 * @throws DynamicExtensionsSystemException
 	 * @throws DynamicExtensionsApplicationException
 	 */
 	public EntityRecordResultInterface getEntityRecords(EntityInterface entity,
 			List<? extends AbstractAttributeInterface> abstractAttributeCollection,
 			List<Long> recordIds) throws DynamicExtensionsSystemException
 	{
 		if (abstractAttributeCollection == null || abstractAttributeCollection.isEmpty())
 		{
 			return null;
 		}
 		//      List<AbstractAttributeInterface> tempAbstractAttributeCollection = new ArrayList(
 		//              abstractAttributeCollection);
 		//      tempAbstractAttributeCollection = EntityManagerUtil
 		//              .filterSystemAttributes(tempAbstractAttributeCollection);
 
 		//Initialising entityRecord and entityRecordMetadata
 
 		EntityRecordResultInterface entityRecordResult = new EntityRecordResult();
 		EntityRecordMetadata recordMetadata = new EntityRecordMetadata();
 		recordMetadata.setAttributeList(abstractAttributeCollection);
 		entityRecordResult.setEntityRecordMetadata(recordMetadata);
 
 		recordMetadata.setAttributeList(abstractAttributeCollection);
 
 		//Filtering abstract attributes into attribtute and association
 		List<AssociationInterface> associationCollection = new ArrayList<AssociationInterface>();
 		List<AttributeInterface> attributesCollection = new ArrayList<AttributeInterface>();
 		filterAttributes(abstractAttributeCollection, attributesCollection, associationCollection);
 		//      attributesCollection = EntityManagerUtil.filterSystemAttributes(attributesCollection);
 
 		//Initialising collection for file attributes and collection attributes.
 		//        List<AttributeInterface> collectionAttributes = new ArrayList<AttributeInterface>();
 		//        List<AttributeInterface> fileAttributes = new ArrayList<AttributeInterface>();
 		//        List<AttributeInterface> objectAttributes = new ArrayList<AttributeInterface>();
 
 		String tableName = entity.getTableProperties().getName();
 		List<String> selectColumnNameList = new ArrayList<String>();
 
 		Iterator attriIterator = attributesCollection.iterator();
 		Map columnNameMap = new HashMap();
 
 		while (attriIterator.hasNext())
 		{
 			AttributeInterface attribute = (AttributeInterface) attriIterator.next();
 			//Filtering attributes into primitive attributes and collection attributes and file attributes
 			if (attribute.getIsCollection())
 			{ // need to fetch AttributeRecord object for the multi select type attribute.
 				//collectionAttributes.add(attribute);
 			}
 			else if (attribute.getAttributeTypeInformation() instanceof FileAttributeTypeInformation)
 			{
 				// need to fetch AttributeRecord object for the File type attribute.
 				//fileAttributes.add(attribute);
 			}
 			else if (attribute.getAttributeTypeInformation() instanceof ObjectAttributeTypeInformation)
 			{
 				// need to fetch AttributeRecord object for the File type attribute.
 				//objectAttributes.add(attribute);
 			}
 			else
 			{
 				//for the other attributes, create select query.
 				String dbColumnName = attribute.getColumnProperties().getName();
 				selectColumnNameList.add(dbColumnName);
 				columnNameMap.put(dbColumnName, attribute);
 			}
 		}
 		try
 		{
 			//Processing primitive attributes
 			StringBuffer query = new StringBuffer();
 			query.append(SELECT_KEYWORD).append(IDENTIFIER).append(WHITESPACE);
 
 			for (int i = 0; i < selectColumnNameList.size(); i++)
 			{
 				query.append(" , ");
 				query.append(selectColumnNameList.get(i));
 			}
 
 			query.append(WHITESPACE).append(FROM_KEYWORD).append(tableName);
 
 			if (recordIds != null && !recordIds.isEmpty())
 			{
 				query.append(WHERE_KEYWORD).append(IDENTIFIER).append(IN_KEYWORD).append(
 						EntityManagerUtil.getListToString(recordIds));
 			}
 
 			/*get values for simple attributes*/
 
 			List<EntityRecordInterface> entityRecordList = getEntityRecordList(
 					selectColumnNameList, query.toString(), columnNameMap, recordMetadata);
 
 			entityRecordResult.setEntityRecordList(entityRecordList);
 			/*
 			 * process any multi select attributes
 			 */
 			//
 			//            for (AttributeInterface attribute : collectionAttributes)
 			//            {
 			//                for (EntityRecordInterface entityRecord : entityRecordList)
 			//                {
 			//                    Long recordId = entityRecord.getRecordId();
 			//                    List<String> valueList = getCollectionAttributeRecordValues(entity.getId(),
 			//                            attribute.getId(), recordId);
 			//                    int index = abstractAttributeCollection.indexOf(attribute);
 			//                    entityRecord.getRecordValueList().set(index, valueList);
 			//                }
 			//
 			//            }
 			//            for (AttributeInterface attribute : fileAttributes)
 			//            {
 			//
 			//                for (EntityRecordInterface entityRecord : entityRecordList)
 			//                {
 			//                    Long recordId = entityRecord.getRecordId();
 			//                    FileAttributeRecordValue fileRecordValue = getFileAttributeRecordValue(entity
 			//                            .getId(), attribute.getId(), recordId);
 			//                    int index = abstractAttributeCollection.indexOf(attribute);
 			//                    entityRecord.getRecordValueList().set(index, fileRecordValue);
 			//                }
 			//
 			//            }
 			//
 			//            for (AttributeInterface attribute : objectAttributes)
 			//            {
 			//
 			//                for (EntityRecordInterface entityRecord : entityRecordList)
 			//                {
 			//                    Long recordId = entityRecord.getRecordId();
 			//                    ObjectAttributeRecordValueInterface objectRecordValue = getObjectAttributeRecordValue(
 			//                            entity.getId(), attribute.getId(), recordId);
 			//                    int index = abstractAttributeCollection.indexOf(attribute);
 			//                    entityRecord.getRecordValueList().set(index, objectRecordValue);
 			//                }
 			//
 			//            }
 			for (EntityRecordInterface entityRecord : entityRecordList)
 			{
 				Long recordId = entityRecord.getRecordId();
 				queryBuilder.putAssociationValues(associationCollection, entityRecordResult,
 						entityRecord, recordId);
 			}
 
 		}
 		catch (SQLException e)
 		{
 			throw new DynamicExtensionsSystemException("Error while retrieving the data", e);
 		}
 
 		return entityRecordResult;
 	}
 
 	/**
 	 * @param selectColumnNameList
 	 * @param query
 	 * @param columnNameMap
 	 * @param recordMetadata
 	 * @param multipleRows
 	 * @return
 	 * @throws DynamicExtensionsSystemException
 	 * @throws SQLException
 	 */
 	private List<EntityRecordInterface> getEntityRecordList(List<String> selectColumnNameList,
 			String query, Map columnNameMap, EntityRecordMetadata recordMetadata)
 			throws DynamicExtensionsSystemException, SQLException
 	{
 		ResultSet resultSet = entityManagerUtil.executeQuery(query);
 		List<EntityRecordInterface> entityRecordList = new ArrayList<EntityRecordInterface>();
 
 		while (resultSet.next())
 		{
 			EntityRecordInterface entityRecord = new EntityRecord();
 			Long id = resultSet.getLong(1);
 			entityRecord.setRecordId(id);
 			Object[] values = new Object[recordMetadata.getAttributeList().size()];
 			for (int i = 1; i <= selectColumnNameList.size(); i++)
 			{
 				String dbColumnName = selectColumnNameList.get(i - 1);
 				Object value = getValueFromResultSet(resultSet, columnNameMap, dbColumnName, i);
 				AttributeInterface attribute = (AttributeInterface) columnNameMap.get(dbColumnName);
 				int indexOfAttribute = recordMetadata.getAttributeList().indexOf(attribute);
 				values[indexOfAttribute] = value;
 			}
 			entityRecord.setRecordValueList(Arrays.asList(values));
 			entityRecordList.add(entityRecord);
 		}
 		resultSet.close();
 		return entityRecordList;
 	}
 
 	/**
 	 * filers abstractAttributes into attributes and associations
 	 * @param abstractAttributeCollection
 	 * @param attributesCollection
 	 * @param associationCollection
 	 */
 	private void filterAttributes(
 			List<? extends AbstractAttributeInterface> abstractAttributeCollection,
 			Collection<AttributeInterface> attributesCollection,
 			Collection<AssociationInterface> associationCollection)
 	{
 		for (AbstractAttributeInterface abstractAttributeInterface : abstractAttributeCollection)
 		{
 			if (abstractAttributeInterface instanceof AssociationInterface)
 			{
 				associationCollection.add((AssociationInterface) abstractAttributeInterface);
 			}
 			else
 			{
 				attributesCollection.add((AttributeInterface) abstractAttributeInterface);
 			}
 		}
 
 	}
 
 	/**
 	 * This method retrives the data for given entity for given record. It also returns the values of
 	 * any inherited attributes.
 	 *
 	 * @see edu.common.dynamicextensions.entitymanager.EntityManagerInterface#getRecordById(edu.common.dynamicextensions.domaininterface.EntityInterface, java.lang.Long)
 	 */
 	public Map<AbstractAttributeInterface, Object> getRecordById(EntityInterface entity,
 			Long recordId) throws DynamicExtensionsSystemException,
 			DynamicExtensionsApplicationException
 	{
 
 		if (entity == null || entity.getId() == null || recordId == null)
 		{
 			throw new DynamicExtensionsSystemException("Invalid Input");
 		}
 
 		Map<AbstractAttributeInterface, Object> recordValues = new HashMap<AbstractAttributeInterface, Object>();
 
 		do
 		{
 			Map<AbstractAttributeInterface, Object> recordValuesForSingleEntity = getEntityRecordById(
 					entity, recordId);
 			recordValues.putAll(recordValuesForSingleEntity);
 			entity = entity.getParentEntity();
 		}
 		while (entity != null);
 
 		return recordValues;
 	}
 
 	/**
 	 * @see edu.common.dynamicextensions.entitymanager.EntityManagerInterface#deleteRecord(edu.common.dynamicextensions.domaininterface.EntityInterface, java.lang.Long)
 	 */
 	public boolean deleteRecord(EntityInterface entity, Long recordId)
 			throws DynamicExtensionsApplicationException, DynamicExtensionsSystemException
 	{
 		boolean isRecordDeleted = false;
 
 		queryBuilder.validateForDeleteRecord(entity, recordId, null);
 
 		Collection attributeCollection = entity.getAttributeCollection();
 		//attributeCollection = entityManagerUtil.filterSystemAttributes(attributeCollection);
 		Collection associationCollection = entity.getAssociationCollection();
 		HibernateDAO hibernateDAO = null;
 		DAOFactory factory = DAOFactory.getInstance();
 		hibernateDAO = (HibernateDAO) factory.getDAO(Constants.HIBERNATE_DAO);
 		List associationRemoveQueryList = new ArrayList();
 
 		try
 		{
 
 			hibernateDAO.openSession(null);
 			if (attributeCollection != null && !attributeCollection.isEmpty())
 			{
 				Iterator iterator = attributeCollection.iterator();
 				while (iterator.hasNext())
 				{
 					AttributeInterface attribute = (AttributeInterface) iterator.next();
 					AttributeTypeInformationInterface typeInfo = attribute
 							.getAttributeTypeInformation();
 					// remove AttributeRecord objects for multi select and file type attributes
 					if (attribute.getIsCollection()
 							|| typeInfo instanceof FileAttributeTypeInformation
 							|| typeInfo instanceof ObjectAttributeTypeInformation)
 					{
 						//                        AttributeRecord collectionAttributeRecord = getAttributeRecord(entity
 						//                                .getId(), attribute.getId(), recordId, hibernateDAO);
 						//                        hibernateDAO.delete(collectionAttributeRecord);
 					}
 				}
 			}
 			if (associationCollection != null && !associationCollection.isEmpty())
 			{
 				Iterator iterator = associationCollection.iterator();
 				while (iterator.hasNext())
 				{
 					Association association = (Association) iterator.next();
 
 					if (association.getSourceRole().getAssociationsType().equals(
 							AssociationType.CONTAINTMENT))
 					{
 						List<Long> recordIdList = new ArrayList<Long>();
 						recordIdList.add(recordId);
 						QueryBuilderFactory.getQueryBuilder()
 								.getContenmentAssociationRemoveDataQueryList(association,
 										recordIdList, associationRemoveQueryList, true);
 
 					}
 					//                  else
 					//                  {
 					//                      String associationRemoveQuery = QueryBuilderFactory.getQueryBuilder()
 					//                              .getAssociationRemoveDataQuery(association, recordId);
 					//
 					//                      associationRemoveQueryList.add(associationRemoveQuery);
 					//
 					//                  }
 
 				}
 			}
 			Connection conn = DBUtil.getConnection();
 			StringBuffer query = new StringBuffer();
 			//          query.append(DELETE_KEYWORD + WHITESPACE + entity.getTableProperties().getName()
 			//                  + WHITESPACE + WHERE_KEYWORD + WHITESPACE + IDENTIFIER + WHITESPACE + EQUAL
 			//                  + WHITESPACE + recordId.toString());
 
 			query.append(UPDATE_KEYWORD + WHITESPACE + entity.getTableProperties().getName());
 			query.append(SET_KEYWORD + Constants.ACTIVITY_STATUS_COLUMN + EQUAL + " '"
 					+ Constants.ACTIVITY_STATUS_DISABLED + "' ");
 			query.append(WHERE_KEYWORD + WHITESPACE + IDENTIFIER + WHITESPACE + EQUAL + WHITESPACE
 					+ recordId.toString());
 
 			List<String> deleteRecordQueryList = new ArrayList<String>(associationRemoveQueryList);
 			deleteRecordQueryList.add(0, query.toString());
 			for (String queryString : deleteRecordQueryList)
 			{
 				logDebug("deleteRecord", "QUERY for delete record is : " + queryString.toString());
 				if (queryString != null && queryString.trim().length() != 0)
 				{
 					PreparedStatement statement = conn.prepareStatement(queryString.toString());
 					statement.executeUpdate();
 				}
 			}
 			hibernateDAO.commit();
 			isRecordDeleted = true;
 		}
 		catch (DynamicExtensionsApplicationException e)
 		{
 			try
 			{
 				hibernateDAO.rollback();
 			}
 			catch (DAOException e1)
 			{
 				throw new DynamicExtensionsSystemException(e.getMessage(), e, DYEXTN_S_001);
 			}
 
 			throw e;
 		}
 		catch (Exception e)
 		{
 			try
 			{
 				hibernateDAO.rollback();
 			}
 			catch (DAOException e1)
 			{
 				throw new DynamicExtensionsSystemException(e.getMessage(), e, DYEXTN_S_001);
 			}
 			throw new DynamicExtensionsSystemException(e.getMessage(), e, DYEXTN_S_001);
 		}
 		finally
 		{
 			try
 			{
 				hibernateDAO.closeSession();
 			}
 			catch (DAOException e1)
 			{
 				throw new DynamicExtensionsSystemException(e1.getMessage(), e1, DYEXTN_S_001);
 			}
 		}
 
 		return isRecordDeleted;
 	}
 
 	public void deleteRecords(Long containerId, List<Long> recordIdList)
 			throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException
 	{
 		ContainerInterface container = DynamicExtensionsUtility
 				.getContainerByIdentifier(containerId.toString());
 
 		EntityInterface entityInterface = container.getEntity();
 		for (Long recordId : recordIdList)
 		{
 			deleteRecord(entityInterface, recordId);
 		}
 
 	}
 
 	/**
 	 * This method retreives records by executing query of the form
 	 *
 	 select childTable.identifier, childTable.attribute1, parentTable.attribute5
 	 from childTable join parentTable
 	 on childTable.identifier = parentTable.identifier
 	 where childTable.activity_status = "active"
 
 	 * @see edu.common.dynamicextensions.entitymanager.EntityManagerInterface#getRecordsForAssociationControl(edu.common.dynamicextensions.domaininterface.userinterface.AssociationControlInterface)
 	 */
 	public Map<Long, List<String>> getRecordsForAssociationControl(
 			AssociationControlInterface associationControl) throws DynamicExtensionsSystemException
 	{
 		Map<Long, List<String>> outputMap = new HashMap<Long, List<String>>();
 		List<String> tableNames = new ArrayList<String>();
 		String tableName;
 		String targetEntityTable = "";
 		String columnName;
 		String onClause = ON_KEYWORD;
 
 		int counter = 0;
 		boolean containsMultipleAttributes = false;
 
 		Collection associationAttributesCollection = associationControl
 				.getAssociationDisplayAttributeCollection();
 
 		if (associationControl instanceof SelectControl)
 			targetEntityTable = ((AssociationInterface) ((SelectControl) associationControl)
 					.getAbstractAttribute()).getTargetEntity().getTableProperties().getName();
 
 		String selectClause = SELECT_KEYWORD + targetEntityTable + "." + IDENTIFIER;
 		String fromClause = FROM_KEYWORD + targetEntityTable + ", ";
 		String whereClause = WHERE_KEYWORD;
 		String multipleColumnsClause = SELECT_KEYWORD + targetEntityTable + "." + IDENTIFIER + ", ";
 
 		List associationAttributesList = new ArrayList(associationAttributesCollection);
 		Collections.sort(associationAttributesList);
 
 		Iterator attributeIterator = associationAttributesCollection.iterator();
 		AssociationDisplayAttributeInterface displayAttribute = null;
 
 		while (attributeIterator.hasNext())
 		{
 			displayAttribute = (AssociationDisplayAttributeInterface) attributeIterator.next();
 			columnName = displayAttribute.getAttribute().getColumnProperties().getName();
 			tableName = displayAttribute.getAttribute().getEntity().getTableProperties().getName();
 
 			if (associationControl instanceof SelectControl
 					&& ((AssociationInterface) ((SelectControl) associationControl)
 							.getAbstractAttribute()).getTargetEntity().getParentEntity() != null)
 			{
 				selectClause = selectClause + ", " + tableName + "." + columnName;
 
 				if (!(fromClause.contains(tableName)))
 					fromClause = fromClause + tableName + ", ";
 
 				if (counter == 0 && associationAttributesCollection.size() > 1)
 				{
 					whereClause = whereClause + tableName + ".ACTIVITY_STATUS <> 'Disabled' AND ";
 					whereClause = whereClause + tableName + "." + IDENTIFIER + " = ";
 				}
 				else if (counter > 0 && associationAttributesCollection.size() > 1)
 				{
 					whereClause = whereClause + tableName + "." + IDENTIFIER + " AND " + tableName
 							+ ".ACTIVITY_STATUS <> 'Disabled' AND " + tableName + "." + IDENTIFIER
 							+ " = ";
 				}
 				else if (associationAttributesCollection.size() == 1)
 				{
 					if (!(fromClause.contains(targetEntityTable)))
 						fromClause = fromClause + targetEntityTable + ", ";
 
 					whereClause = whereClause + targetEntityTable
 							+ ".ACTIVITY_STATUS <> 'Disabled' AND ";
 					whereClause = whereClause + tableName + "." + IDENTIFIER + " = "
 							+ targetEntityTable + "." + IDENTIFIER + " AND " + targetEntityTable
 							+ "." + IDENTIFIER + " = ";
 				}
 
 				counter++;
 
 				tableNames.add(tableName);
 			}
 			else
 			{
 				containsMultipleAttributes = true;
 				multipleColumnsClause += columnName + ", ";
 				tableNames.add(tableName);
 			}
 
 			if (tableNames.size() == 0 && !(associationControl instanceof SelectControl))
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
 
 		if (!containsMultipleAttributes)
 		{
 			int lastIndexOfAND = whereClause.lastIndexOf("AND");
 			whereClause = whereClause.substring(0, lastIndexOfAND);
 			fromClause = fromClause.substring(0, fromClause.length() - 2);
 		}
 
 		if (((AssociationInterface) ((SelectControl) associationControl).getAbstractAttribute())
 				.getTargetEntity().getParentEntity() == null)
 			multipleColumnsClause = multipleColumnsClause.substring(0, multipleColumnsClause
 					.length() - 2)
 					+ FROM_KEYWORD + targetEntityTable;
 
 		StringBuffer query = new StringBuffer();
 
 		if (!containsMultipleAttributes)
 		{
 			query.append(selectClause + fromClause + whereClause);
 		}
 		else
 		{
 			query.append(multipleColumnsClause);
 			query.append(WHERE_KEYWORD
 					+ queryBuilder.getRemoveDisbledRecordsQuery(tableNames.get(0)));
 		}
 
 		JDBCDAO jdbcDao = null;
 		try
 		{
 			jdbcDao = (JDBCDAO) DAOFactory.getInstance().getDAO(Constants.JDBC_DAO);
 			jdbcDao.openSession(null);
 			List result = new ArrayList();
 			result = jdbcDao.executeQuery(query.toString(), new SessionDataBean(), false, null);
 
 			if (result != null)
 			{
 				if (!containsMultipleAttributes)
 				{
 					for (int i = 0; i < result.size(); i++)
 					{
 						List innerList = (List) result.get(i);
 						Long recordId = Long.parseLong((String) innerList.get(0));
 						innerList.remove(0);
 						outputMap.put(recordId, innerList);
 					}
 				}
 				else
 				{
 					for (int i = 0; i < result.size(); i++)
 					{
 						List innerList = (List) result.get(i);
 						Long recordId = Long.parseLong((String) innerList.get(0));
 
 						if (outputMap.containsKey(recordId))
 						{
 							List<String> tempStringList = new ArrayList<String>();
 
 							String existingString = outputMap.get(recordId).toString().replace("[",
 									" ");
 							existingString = existingString.replace("]", " ");
 
 							tempStringList
 									.add(existingString.trim() + associationControl.getSeparator()
 											+ (String) innerList.get(1));
 							outputMap.put(recordId, tempStringList);
 						}
 						else
 						{
 							innerList.remove(0);
 							outputMap.put(recordId, innerList);
 						}
 					}
 				}
 			}
 		}
 
 		catch (Exception e)
 		{
 			throw new DynamicExtensionsSystemException("Error while retrieving the data", e);
 		}
 		finally
 		{
 			try
 			{
 				jdbcDao.closeSession();
 			}
 			catch (DAOException e)
 			{
 				throw new DynamicExtensionsSystemException("Error while retrieving the data", e);
 			}
 		}
 
 		return outputMap;
 	}
 
 
     /**
      * @see edu.common.dynamicextensions.entitymanager.EntityManagerInterface#getAllContainerBeans()
      */
     public List<NameValueBean> getAllContainerBeans() throws DynamicExtensionsSystemException,
             DynamicExtensionsApplicationException
     {
         Map<String, HQLPlaceHolderObject> substitutionParameterMap = new HashMap<String, HQLPlaceHolderObject>();
         Collection containersBeansCollection = executeHQL("getAllContainerBeans",
                 substitutionParameterMap);
         Iterator containerBeansIterator = containersBeansCollection.iterator();
         Object[] objectArrayForContainerBeans;
         List<NameValueBean> list = new ArrayList<NameValueBean>();
         while (containerBeansIterator.hasNext())
         {
             objectArrayForContainerBeans = (Object[]) containerBeansIterator.next();
             list.add(new NameValueBean((String) objectArrayForContainerBeans[1],
                     (Long) objectArrayForContainerBeans[0]));
         }
         return list;
     }
 
     /**
      * @see edu.common.dynamicextensions.entitymanager.EntityManagerInterface#getAllContainerBeans()
      */
     public List<ContainerInformationObject> getAllContainerInformationObjects()
             throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException
     {
         Map<String, HQLPlaceHolderObject> substitutionParameterMap = new HashMap<String, HQLPlaceHolderObject>();
         Collection containerInformationObjectCollection = executeHQL(
                 "getAllContainerInformationObjects", substitutionParameterMap);
         Iterator containerInformationObjectIterator = containerInformationObjectCollection
                 .iterator();
         Object[] objectArrayForContainerInformationObject;
         List<ContainerInformationObject> list = new ArrayList<ContainerInformationObject>();
         while (containerInformationObjectIterator.hasNext())
         {
             objectArrayForContainerInformationObject = (Object[]) containerInformationObjectIterator
                     .next();
             list.add(new ContainerInformationObject(
                     (String) objectArrayForContainerInformationObject[1],
                     ((Long) objectArrayForContainerInformationObject[0]).toString(),
                     (String) objectArrayForContainerInformationObject[2]));
         }
         return list;
     }
 
     /**
      *
      * @return
      * @throws DynamicExtensionsSystemException
      * @throws DynamicExtensionsApplicationException
      */
     public Map<String, String> getAllContainerBeansMap() throws DynamicExtensionsSystemException,
             DynamicExtensionsApplicationException
     {
         Map<String, HQLPlaceHolderObject> substitutionParameterMap = new HashMap<String, HQLPlaceHolderObject>();
         Collection containersBeansCollection = executeHQL("getAllContainerBeans",
                 substitutionParameterMap);
         Iterator containerBeansIterator = containersBeansCollection.iterator();
         Object[] objectArrayForContainerBeans;
         //List<NameValueBean> list = new ArrayList<NameValueBean>();
         Map<String, String> containerBeansMap = new HashMap<String, String>();
         String containerId;
         String containerCaption;
         while (containerBeansIterator.hasNext())
         {
             objectArrayForContainerBeans = (Object[]) containerBeansIterator.next();
             containerCaption = (String) objectArrayForContainerBeans[1];
             containerId = ((Long) objectArrayForContainerBeans[0]).toString();
             //          list.add(new NameValueBean((String) objectArrayForContainerBeans[1],
             //                  (Long) objectArrayForContainerBeans[0]));
             containerBeansMap.put(containerId, containerCaption);
         }
         return containerBeansMap;
     }
     /**
      * @see edu.common.dynamicextensions.entitymanager.EntityManagerInterface#getAllRecords(edu.common.dynamicextensions.domaininterface.EntityInterface)
      */
     public List<EntityRecord> getAllRecords(EntityInterface entity)
             throws DynamicExtensionsSystemException
     {
         List<EntityRecord> recordList = new ArrayList<EntityRecord>();
         JDBCDAO jdbcDao = null;
         List<List> result;
         try
         {
             jdbcDao = (JDBCDAO) DAOFactory.getInstance().getDAO(Constants.JDBC_DAO);
             jdbcDao.openSession(null);
             TablePropertiesInterface tablePropertiesInterface = entity.getTableProperties();
             String tableName = tablePropertiesInterface.getName();
             String[] selectColumnName = {IDENTIFIER};
             String[] whereColumnName = {Constants.ACTIVITY_STATUS_COLUMN};
             String[] whereColumnCondition = {EQUAL};
             Object[] whereColumnValue = {"'" + Constants.ACTIVITY_STATUS_ACTIVE + "'"};
             result = jdbcDao.retrieve(tableName, selectColumnName, whereColumnName,
                     whereColumnCondition, whereColumnValue, null);
             recordList = getRecordList(result);
 
         }
         catch (DAOException e)
         {
             throw new DynamicExtensionsSystemException("Error while retrieving the data", e);
         }
         finally
         {
             try
             {
                 jdbcDao.closeSession();
             }
             catch (DAOException e)
             {
                 throw new DynamicExtensionsSystemException("Error while retrieving the data", e);
             }
         }
         return recordList;
     }
     /**
     *
     * @param result
     * @return
     */
    private List<EntityRecord> getRecordList(List<List> result)
    {
        List<EntityRecord> recordList = new ArrayList<EntityRecord>();
        EntityRecord entityRecord;
        String id;
        for (List innnerList : result)
        {
            if (innnerList != null && !innnerList.isEmpty())
            {
                id = (String) innnerList.get(0);
                if (id != null)
                {
                    entityRecord = new EntityRecord(new Long(id));
                    recordList.add(entityRecord);
                }
            }
        }
        return recordList;
    }
    /**
     * @throws DynamicExtensionsSystemException
     * @see edu.common.dynamicextensions.entitymanager.EntityManagerInterface#getChildrenEntities(edu.common.dynamicextensions.domaininterface.EntityInterface)
     */
    public Collection<EntityInterface> getChildrenEntities(EntityInterface entity)
            throws DynamicExtensionsSystemException
    {
        Map<String, HQLPlaceHolderObject> substitutionParameterMap = new HashMap<String, HQLPlaceHolderObject>();
        substitutionParameterMap.put("0", new HQLPlaceHolderObject("long", entity.getId()));
 
        return executeHQL("getChildrenEntities", substitutionParameterMap);
    }
 
    /**
     * @see edu.common.dynamicextensions.entitymanager.EntityManagerInterface#getAssociationByIdentifier(java.lang.Long)
     */
    public AssociationInterface getAssociationByIdentifier(Long associationId)
            throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException
    {
        Map<String, HQLPlaceHolderObject> substitutionParameterMap = new HashMap<String, HQLPlaceHolderObject>();
        substitutionParameterMap.put("0", new HQLPlaceHolderObject("long", associationId));
        Collection assocationCollection = executeHQL("getAssociationByIdentifier",
                substitutionParameterMap);
        if (assocationCollection.isEmpty())
        {
            throw new DynamicExtensionsApplicationException(
                    "Object Not Found : id" + associationId, null, DYEXTN_A_008);
        }
        return (AssociationInterface) assocationCollection.iterator().next();
    }
 
    /**
     * @see edu.common.dynamicextensions.entitymanager.EntityManagerInterface#getAssociationsForTargetEntity(edu.common.dynamicextensions.domaininterface.EntityInterface)
     */
    public Collection<AssociationInterface> getIncomingAssociations(EntityInterface entity)
            throws DynamicExtensionsSystemException
    {
        Map<String, HQLPlaceHolderObject> substitutionParameterMap = new HashMap<String, HQLPlaceHolderObject>();
        substitutionParameterMap.put("0", new HQLPlaceHolderObject("long", entity.getId()));
        Collection<AssociationInterface> assocationCollection = executeHQL(null,
                "getAssociationsForTargetEntity", substitutionParameterMap);
        return assocationCollection;
    }
 
    /**
     * @see edu.common.dynamicextensions.entitymanager.EntityManagerInterface#getAssociationsForTargetEntity(edu.common.dynamicextensions.domaininterface.EntityInterface)
     */
    public Collection<Long> getIncomingAssociationIds(EntityInterface entity)
            throws DynamicExtensionsSystemException
    {
        Map<String, HQLPlaceHolderObject> substitutionParameterMap = new HashMap<String, HQLPlaceHolderObject>();
        substitutionParameterMap.put("0", new HQLPlaceHolderObject("long", entity.getId()));
        Collection<Long> assocationCollection = executeHQL(null,
                "getAssociationIdsForTargetEntity", substitutionParameterMap);
        return assocationCollection;
    }
 
    /**
     * @param containerId
     * @return
     * @throws DynamicExtensionsSystemException
     */
    public String getContainerCaption(Long containerId) throws DynamicExtensionsSystemException
    {
        Map<String, HQLPlaceHolderObject> substitutionParameterMap = new HashMap<String, HQLPlaceHolderObject>();
        substitutionParameterMap.put("0", new HQLPlaceHolderObject("long", containerId));
        Collection containerCaption = executeHQL("getContainerCaption", substitutionParameterMap);
        return containerCaption.iterator().next().toString();
 
    }
 
    /** (non-Javadoc)
     * @see edu.common.dynamicextensions.entitymanager.EntityManagerInterface#addAssociationColumn(edu.common.dynamicextensions.domaininterface.AssociationInterface)
     */
    public void addAssociationColumn(AssociationInterface association)
            throws DynamicExtensionsSystemException
    {
        List list = new ArrayList();
        String query;
        Stack stack = new Stack();
        try
        {
            query = queryBuilder.getQueryPartForAssociation(association, list, true);
 
            List queryList = new ArrayList();
            queryList.add(query);
            stack = queryBuilder.executeQueries(queryList, list, stack);
        }
        catch (DynamicExtensionsSystemException e)
        {
            if (!stack.isEmpty())
            {
                rollbackQueries(stack, (Entity) association.getEntity(), e, DAOFactory
                        .getInstance().getDAO(Constants.HIBERNATE_DAO));
            }
        }
    }
 
    /**
     * @see edu.common.dynamicextensions.entitymanager.EntityManagerInterface#associateEntityRecords(edu.common.dynamicextensions.domaininterface.AssociationInterface, java.lang.Long, java.lang.Long)
     */
    public void associateEntityRecords(AssociationInterface associationInterface,
            Long sourceEntityRecordId, Long TargetEntityRecordId)
            throws DynamicExtensionsSystemException
    {
        queryBuilder.associateRecords(associationInterface, sourceEntityRecordId,
                TargetEntityRecordId);
    }
 
    /**
     * @param containerId
     * @throws DynamicExtensionsSystemException
     */
    public Long getEntityIdByContainerId(Long containerId) throws DynamicExtensionsSystemException
    {
        Map substitutionParameterMap = new HashMap();
        substitutionParameterMap.put("0", new HQLPlaceHolderObject("long", containerId));
        Collection recordCollection = null;
        //Required HQL is stored in the hbm file. The following method takes the name of the query and
        // the actual values for the placeholders as the parameters.
        recordCollection = executeHQL("getEntityIdForContainerId", substitutionParameterMap);
        if (recordCollection != null && !recordCollection.isEmpty())
        {
            return (Long) recordCollection.iterator().next();
        }
        return null;
    }
 
    /**
     * @return
     * @throws DynamicExtensionsSystemException
     */
    public Map<Long, Date> getEntityCreatedDateByContainerId()
            throws DynamicExtensionsSystemException
    {
        Map<String, HQLPlaceHolderObject> substitutionParameterMap = new HashMap<String, HQLPlaceHolderObject>();
        Map<Long, Date> map = new HashMap<Long, Date>();
        Collection containersBeansCollection;
        containersBeansCollection = executeHQL("getAllEntityCreatedDateByContainerId",
                substitutionParameterMap);
 
        if (containersBeansCollection != null && !containersBeansCollection.isEmpty())
        {
            Iterator iter = containersBeansCollection.iterator();
 
            while (iter.hasNext())
            {
                Object[] objectArray = (Object[]) iter.next();
                map.put((Long) objectArray[0], (Date) objectArray[1]);
            }
        }
        return map;
 
    }
    /**
    *
    * @param isAbstarct
    * @param entityIdentifier
    * @return
    * @throws DynamicExtensionsSystemException
    */
    public Long checkContainerForAbstractEntity(Long entityIdentifier, boolean isAbstarct)
    throws DynamicExtensionsSystemException
    {
        Map<String, HQLPlaceHolderObject> substitutionParameterMap = new HashMap<String, HQLPlaceHolderObject>();
        substitutionParameterMap.put("0", new HQLPlaceHolderObject("long",entityIdentifier));
        substitutionParameterMap.put("1", new HQLPlaceHolderObject("boolean",isAbstarct));
 
        Collection containerCollection = executeHQL("checkContainerForAbstractEntity",substitutionParameterMap);
 
        Long contId = null;
 
        if (containerCollection != null && containerCollection.size() > 0)
        {
            contId = (Long) containerCollection.iterator().next();
 
        }
        return contId;
    }
    public  Long getEntityId(String entityName) throws DynamicExtensionsSystemException
    {
        ResultSet rsltSet = null;
        String entityTableName = "dyextn_abstract_metadata";
        String NAME = "name";
        StringBuffer query = new StringBuffer();
        query.append(SELECT_KEYWORD + WHITESPACE + IDENTIFIER);
        query.append(WHITESPACE + FROM_KEYWORD + WHITESPACE + entityTableName + WHITESPACE);
        query.append(WHERE_KEYWORD  + WHITESPACE + NAME +  WHITESPACE + EQUAL + "'"+entityName+"'");
        System.out.println("Query = "  +query.toString());
        try {
            rsltSet = EntityManagerUtil.executeQuery(query.toString());
            rsltSet.next();
            Long identifier = rsltSet.getLong(IDENTIFIER);
            return identifier;
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            if (rsltSet != null)
            {
                try
                {
                    rsltSet.close();
                }
                catch (SQLException e)
                {
                    throw new DynamicExtensionsSystemException(e.getMessage(), e);
                }
            }
        }
        return null;
    }
 
    /**
     * Get the container Id for the specified entity Id
     * This method fires direct JDBC SQL queries without using hibernate for performance purposes
     * @param entityId : Id for the entity whose container id is to be fetched
     * @return : container Id for specified entity
     * @throws DynamicExtensionsSystemException
     */
    public  Long getContainerIdForEntity(Long entityId) throws DynamicExtensionsSystemException
    {
        ResultSet rsltSet = null;
        String tableName = "dyextn_container";
        String ENTITY_ID_FIELD_NAME = "ENTITY_ID";
        StringBuffer query = new StringBuffer();
        query.append(SELECT_KEYWORD + WHITESPACE + IDENTIFIER);
        query.append(WHITESPACE + FROM_KEYWORD + WHITESPACE + tableName + WHITESPACE);
        query.append(WHERE_KEYWORD  + WHITESPACE + ENTITY_ID_FIELD_NAME +  WHITESPACE + EQUAL + "'"+entityId+"'");
        System.out.println("Query = "  +query.toString());
        try {
            rsltSet = EntityManagerUtil.executeQuery(query.toString());
            if(rsltSet!=null)
            {
                rsltSet.next();
                Long identifier = rsltSet.getLong(IDENTIFIER);
                return identifier;
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            if (rsltSet != null)
            {
                try
                {
 
                    rsltSet.close();
 
                }
                catch (SQLException e)
                {
                    throw new DynamicExtensionsSystemException(e.getMessage(), e);
                }
            }
        }
        return null;
    }
    /**
     * Get next identifier for an entity from entity table when a record is to be inserted to the entity table.
     * @param entityName :  Name of the entity
     * @return :  Next identifier that can be assigned to a entity record
     * @throws DynamicExtensionsSystemException
     */
    public  Long getNextIdentifierForEntity(String entityName) throws DynamicExtensionsSystemException
    {
        ResultSet rsltSet = null;
        String tableName = "dyextn_database_properties";
        String NAME = "NAME";
        StringBuffer query = new StringBuffer();
        query.append(SELECT_KEYWORD + WHITESPACE + NAME);
        query.append(WHITESPACE + FROM_KEYWORD + WHITESPACE + tableName + WHITESPACE);
        query.append(WHERE_KEYWORD  + WHITESPACE +  IDENTIFIER +  WHITESPACE + EQUAL );
        query.append(OPENING_BRACKET);
            query.append(SELECT_KEYWORD + WHITESPACE + IDENTIFIER);
            query.append(WHITESPACE + FROM_KEYWORD + WHITESPACE + "dyextn_table_properties" + WHITESPACE);
            query.append(WHERE_KEYWORD  + WHITESPACE +  "ENTITY_ID" +  WHITESPACE + EQUAL );
            query.append(OPENING_BRACKET);
                query.append(SELECT_KEYWORD + WHITESPACE + IDENTIFIER);
                query.append(WHITESPACE + FROM_KEYWORD + WHITESPACE + "dyextn_abstract_metadata" + WHITESPACE);
                query.append(WHERE_KEYWORD  + WHITESPACE +  "NAME" +  WHITESPACE + EQUAL +"'"+ entityName+"'");
            query.append(CLOSING_BRACKET);
        query.append(CLOSING_BRACKET);
        System.out.println("Query = "  +query.toString());
        try {
             rsltSet = EntityManagerUtil.executeQuery(query.toString());
            if(rsltSet!=null)
            {
                rsltSet.next();
                String entityTableName = rsltSet.getString(NAME);
                if(entityTableName!=null)
                {
                    EntityManagerUtil entityManagerUtil = new EntityManagerUtil();
                    return entityManagerUtil.getNextIdentifier(entityTableName);
                }
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            if (rsltSet != null)
            {
                try
                {
 
                    rsltSet.close();
 
                }
                catch (SQLException e)
                {
                    throw new DynamicExtensionsSystemException(e.getMessage(), e);
                }
            }
        }
        return null;
    }
 
    /**
     * Returns an attribute given the entity name and attribute name.
     * @param entityName name of the entity.
     * @param attributeName name of the attribute.
     * @return AttributeInterface attribute interface
     * @throws DynamicExtensionsSystemException
     * @throws DynamicExtensionsApplicationException
     */
    public AttributeInterface getAttribute(String entityName, String attributeName)
            throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException
    {
        AttributeInterface attributeInterface = null;
        AbstractAttributeInterface abstractAttributeInterface;
        String name;
        if (entityName == null || entityName.equals("") || attributeName == null
                || attributeName.equals(""))
        {
            return attributeInterface;
        }
 
        EntityInterface entityInterface = getEntityByName(entityName);
        if (entityInterface != null)
        {
            Collection abstractAttributeCollection = entityInterface
                    .getAbstractAttributeCollection();
            if (abstractAttributeCollection != null)
            {
                Iterator abstractAttributeIterator = abstractAttributeCollection.iterator();
 
                while (abstractAttributeIterator.hasNext())
                {
                    abstractAttributeInterface = (AbstractAttributeInterface) abstractAttributeIterator
                            .next();
                    if (abstractAttributeInterface instanceof AttributeInterface)
                    {
                        attributeInterface = (AttributeInterface) abstractAttributeInterface;
                        name = attributeInterface.getName();
                        if (name != null && name.equals(attributeName))
                        {
                            return attributeInterface;
                        }
                    }
                }
            }
        }
 
        return attributeInterface;
    }
 
    /**
     * Returns all entitiy groups in the whole system
     * @return Collection Entity group Beans Collection
     * @throws DynamicExtensionsSystemException
     */
    public Collection<NameValueBean> getAllEntityGroupBeans() throws DynamicExtensionsSystemException
    {
        Collection<NameValueBean> entityGroupBeansCollection = new ArrayList<NameValueBean>();
        Collection groupBeansCollection = executeHQL("getAllGroupBeans", new HashMap());
        Iterator groupBeansIterator = groupBeansCollection.iterator();
        Object[] objectArray;
 
        while (groupBeansIterator.hasNext())
        {
            objectArray = (Object[]) groupBeansIterator.next();
            NameValueBean entityGroupNameValue = new NameValueBean();
            entityGroupNameValue.setName(objectArray[0]);
            entityGroupNameValue.setValue(objectArray[1]);
            entityGroupBeansCollection.add(entityGroupNameValue);
        }
 
        return entityGroupBeansCollection;
    }
 
 	/**
 	 * validateEntity.
 	 */
 	public boolean validateEntity(EntityInterface entityInterface)
 			throws DynamicExtensionsApplicationException, DynamicExtensionsSystemException
 	{
 		Collection<EntityInterface> entityCollection = entityInterface.getEntityGroup()
 				.getEntityCollection();
 		for (EntityInterface entity : entityCollection)
 		{
 			Entity entityObject = (Entity) entityInterface;
 			if (entity.getId() == null)
 			{
 				DynamicExtensionsUtility.validateEntity(entity);
 			}
 			else
 			{
 				Entity databaseCopy = (Entity) DBUtil.loadCleanObj(Entity.class, entity.getId());
 				if (queryBuilder.isParentChanged((Entity) entity, databaseCopy))
 				{
 					checkParentChangeAllowed(entityObject);
 				}
 			}
 		}
 		return true;
 	}
 
 }
