 
 package edu.common.dynamicextensions.entitymanager;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import edu.common.dynamicextensions.domain.CategoryEntity;
 import edu.common.dynamicextensions.domain.DomainObjectFactory;
 import edu.common.dynamicextensions.domain.Entity;
 import edu.common.dynamicextensions.domaininterface.AbstractAttributeInterface;
 import edu.common.dynamicextensions.domaininterface.AssociationInterface;
 import edu.common.dynamicextensions.domaininterface.AttributeInterface;
 import edu.common.dynamicextensions.domaininterface.EntityGroupInterface;
 import edu.common.dynamicextensions.domaininterface.EntityInterface;
 import edu.common.dynamicextensions.domaininterface.RoleInterface;
 import edu.common.dynamicextensions.domaininterface.databaseproperties.ColumnPropertiesInterface;
 import edu.common.dynamicextensions.exception.DynamicExtensionsApplicationException;
 import edu.common.dynamicextensions.exception.DynamicExtensionsSystemException;
 import edu.common.dynamicextensions.util.DynamicExtensionsUtility;
 import edu.common.dynamicextensions.util.global.DEConstants.AssociationType;
 import edu.common.dynamicextensions.util.global.DEConstants.Cardinality;
 import edu.wustl.common.util.logger.Logger;
 import edu.wustl.dao.HibernateDAO;
 import edu.wustl.dao.JDBCDAO;
 import edu.wustl.dao.exception.DAOException;
 
 public class EntityManagerUtil implements DynamicExtensionsQueryBuilderConstantsInterface
 {
 
 	private static Map<String, Long> idMap = new HashMap<String, Long>();
 
 	/**
 	 * @param query
 	 * @param useClnSession
 	 * @return
 	 * @throws DynamicExtensionsSystemException
 	 */
 	/*public static ResultSet executeQuery(String query,DAO dao)
 			throws DynamicExtensionsSystemException
 	{
 		Connection conn = null;
 		try
 		{
 			conn = dao.getCleanConnection();
 			conn.setAutoCommit(false);
 			Statement statement = null;
 			statement = conn.createStatement();
 			ResultSet resultSet = statement.executeQuery(query);
 			return resultSet;
 		}
 		catch (Exception e)
 		{
 			try
 			{
 				conn.rollback();
 			}
 			catch (SQLException e1)
 			{
 				throw new DynamicExtensionsSystemException(e.getMessage(), e);
 			}
 			throw new DynamicExtensionsSystemException(e.getMessage(), e);
 		}
 	}*/
 
 	/**
 	 * @param inputs
 	 * @return
 	 */
 	public static String getListToString(List inputs)
 	{
 		String query = inputs.toString();
 		query = query.replace("[", OPENING_BRACKET);
 		query = query.replace("]", CLOSING_BRACKET);
 
 		return query;
 	}
 
 	/**
 	 * @param query
 	 * @return
 	 * @throws DynamicExtensionsSystemException
 	 */
 	public static int getNoOfRecord(String query) throws DynamicExtensionsSystemException
 	{
 		ResultSet resultSet = null;
 		JDBCDAO jdbcDao = null;
 		try
 		{
 			jdbcDao = DynamicExtensionsUtility.getJDBCDAO();
 			resultSet = jdbcDao.getQueryResultSet(query);
 			resultSet.next();
 			return resultSet.getInt(1);
 		}
 		catch (DAOException e)
 		{
 			throw new DynamicExtensionsSystemException(e.getMessage(), e);
 		}
 		catch (SQLException e)
 		{
 			throw new DynamicExtensionsSystemException(e.getMessage(), e);
 		}
 		finally
 		{
 			if (resultSet != null)
 			{
 				try
 				{
 					DynamicExtensionsUtility.closeJDBCDAO(jdbcDao);
 				}
 				catch (DAOException e)
 				{
 					throw new DynamicExtensionsSystemException(e.getMessage(), e);
 				}
 			}
 		}
 	}
 
 	/**
 	 * @param jdbcDAO
 	 * @param query query to be executed
 	 * @return
 	 * @throws DynamicExtensionsSystemException
 	 */
 	public int executeDML(JDBCDAO jdbcDao, String query) throws DynamicExtensionsSystemException
 	{
 		Logger.out.info(query);
 		try
 		{
 			return jdbcDao.executeUpdate(query);
 		}
 		catch (DAOException e)
 		{
 			throw new DynamicExtensionsSystemException(e.getMessage(), e);
 		}
 	}
 
 	/**
 	 * @param jdbcDAO
 	 * @param queries
 	 * @return
 	 * @throws DynamicExtensionsSystemException
 	 */
 	public int executeDMLQueryList(JDBCDAO jdbcDao, List<String> queries)
 			throws DynamicExtensionsSystemException
 	{
 		int result = -1;
 
 		for (String query : queries)
 		{
 			result = executeDML(jdbcDao, query);
 		}
 
 		return result;
 	}
 
 	/**
 	 * Method generates the next identifier for the table that stores the value of the passes entity.
 	 * @param tableName
 	 * @return
 	 * @throws DynamicExtensionsSystemException
 	 */
 	synchronized public static Long getNextIdentifier(String tableName)
 			throws DynamicExtensionsSystemException
 	{
 		// Query to get next identifier.
 		StringBuffer query = new StringBuffer("SELECT MAX(IDENTIFIER) FROM " + tableName);
 		JDBCDAO jdbcDao = null;
 		try
 		{
 			Long identifier = null;
 			if (idMap.containsKey(tableName))
 			{
 				Long newIdentifier = (Long) idMap.get(tableName);
 				identifier = newIdentifier + 1;
 			}
 			else
 			{
 				ResultSet resultSet = null;
 				try
 				{
 					jdbcDao = DynamicExtensionsUtility.getJDBCDAO();
 					resultSet = jdbcDao.getQueryResultSet(query.toString());
 					resultSet.next();
 					identifier = resultSet.getLong(1);
 					identifier = identifier + 1;
 				}
 				finally
 				{
 					if (resultSet != null)
 					{
 						try
 						{
 							DynamicExtensionsUtility.closeJDBCDAO(jdbcDao);
 						}
 						catch (DAOException e)
 						{
 							throw new DynamicExtensionsSystemException(e.getMessage(), e);
 						}
 					}
 				}
 			}
 			idMap.put(tableName, identifier);
 
 			return identifier;
 		}
 		catch (DAOException e)
 		{
 			throw new DynamicExtensionsSystemException(
 					"Could not fetch the next identifier for table " + tableName, e);
 		}
 		catch (SQLException e)
 		{
 			throw new DynamicExtensionsSystemException(
 					"Could not fetch the next identifier for table " + tableName, e);
 		}
 	}
 
 	/**
 	 * This method is used in case result of the query is multiple records.
 	 * @param query
 	 * @return
 	 * @throws DynamicExtensionsSystemException
 	 */
 	public List<Long> getResultInList(String query) throws DynamicExtensionsSystemException
 	{
 		List<Long> results = new ArrayList<Long>();
 
 		ResultSet resultSet = null;
 		JDBCDAO jdbcDao = null;
 		try
 		{
 			jdbcDao = DynamicExtensionsUtility.getJDBCDAO();
 			resultSet = jdbcDao.getQueryResultSet(query);
 			while (resultSet.next())
 			{
 				Long identifier = resultSet.getLong(1);
 				results.add(identifier);
 			}
 		}
 		catch (DAOException e)
 		{
 			throw new DynamicExtensionsSystemException(e.getMessage(), e);
 		}
 		catch (SQLException e)
 		{
 			throw new DynamicExtensionsSystemException(e.getMessage(), e);
 		}
 		finally
 		{
 			try
 			{
 				DynamicExtensionsUtility.closeJDBCDAO(jdbcDao);
 			}
 			catch (DAOException e)
 			{
 				Logger.out.debug(e.getMessage());
 				throw new DynamicExtensionsSystemException(e.getMessage(), e);
 			}
 		}
 
 		return results;
 	}
 
 	/**
 	 * This method returns all the entity groups reachable from given entity.
 	 *
 	 * @param entity
 	 * @param prcesdEntities
 	 * @param prcesdEntGroup
 	 */
 	public static void getAllEntityGroups(EntityInterface entity,
 			Set<EntityInterface> prcesdEntities, Set<EntityGroupInterface> prcesdEntGroup)
 	{
 		if (prcesdEntities.contains(entity))
 		{
 			return;
 		}
 
 		prcesdEntities.add(entity);
 	}
 
 	/**
 	 * @param attributes
 	 * @return
 	 */
 	public static List<AbstractAttributeInterface> filterSystemAttributes(
 			List<AbstractAttributeInterface> attributes)
 	{
 		List<AbstractAttributeInterface> attributeList = new ArrayList<AbstractAttributeInterface>();
 		for (AbstractAttributeInterface attribute : attributes)
 		{
 			if (!attribute.getName().equalsIgnoreCase(ID_ATTRIBUTE_NAME))
 			{
 				attributeList.add(attribute);
 			}
 		}
 
 		return attributeList;
 	}
 
 	/**
 	 * @param attributes
 	 * @return
 	 */
 	public static Collection<AbstractAttributeInterface> filterSystemAttributes(
 			Collection<AbstractAttributeInterface> attributes)
 	{
 		return filterSystemAttributes(new ArrayList(attributes));
 	}
 
 	/**
 	 * This method adds a system generated attribute to the entity.
 	 * @param entity
 	 */
 	public static void addIdAttribute(EntityInterface entity)
 	{
 		if (!isIdAttributePresent(entity))
 		{
 			DomainObjectFactory factory = DomainObjectFactory.getInstance();
 			AttributeInterface idAttribute = factory.createLongAttribute();
 			idAttribute.setName(ID_ATTRIBUTE_NAME);
 			idAttribute.setIsPrimaryKey(Boolean.TRUE);
 			idAttribute.setIsNullable(Boolean.FALSE);
 			ColumnPropertiesInterface column = factory.createColumnProperties();
 			column.setName(IDENTIFIER);
 			idAttribute.setColumnProperties(column);
 			entity.addPrimaryKeyAttribute(idAttribute);
 			entity.addAttribute(idAttribute);
 			idAttribute.setEntity(entity);
 		}
 	}
 
 	/**
 	 * This method returns boolean whether the id attribute is present or not.
 	 * @param entity
 	 * @return boolean
 	 */
 	public static boolean isIdAttributePresent(EntityInterface entity)
 	{
 		boolean isAttrPresent = false;
 
 		Collection<AbstractAttributeInterface> attributes = entity.getAbstractAttributeCollection();
 		if (attributes != null && !attributes.isEmpty())
 		{
 			for (AbstractAttributeInterface attribute : attributes)
 			{
 				if (ID_ATTRIBUTE_NAME.equalsIgnoreCase(attribute.getName()))
 				{
 					isAttrPresent = true;
 					break;
 				}
 			}
 		}
 
 		return isAttrPresent;
 	}
 
 	/**
 	 * It will check weather the primary key of the entity is changed
 	 * @param entity to be checked 
 	 * @return 
 	 */
 	public static boolean isPrimaryKeyChanged(EntityInterface entity)
 	{
 		boolean isChanged = false;
 		if (entity != null)
 		{
 			Long entityId = entity.getId();
 			if (entityId != null)
 			{
 				EntityInterface dbaseCopy = null;
 				try
 				{
 					dbaseCopy = (Entity) DynamicExtensionsUtility.getCleanObject(Entity.class
 							.getCanonicalName(), entityId);
 				}
 				catch (DAOException e)
 				{
 					Logger.out.debug(e.getMessage());
 				}
 				isChanged = isPrimaryKeyChanged(entity, dbaseCopy);
 			}
 		}
 		return isChanged;
 	}
 
 	/**
 	 * It will check weather the primary key of the entity is changed
 	 * @param entity to be checked 
 	 * @return 
 	 */
 	public static boolean isPrimaryKeyChanged(EntityInterface entity, EntityInterface dbaseCopy)
 	{
 		boolean isChanged = false;
 		Collection<AttributeInterface> entityPrmKeyColl = entity.getPrimaryKeyAttributeCollection();
 		Collection<AttributeInterface> dbasePrmKeyColl = dbaseCopy
 				.getPrimaryKeyAttributeCollection();
 		for (AttributeInterface entityAttribute : entityPrmKeyColl)
 		{
 			if (!dbasePrmKeyColl.contains(entityAttribute))
 			{
 				isChanged = true;
 				break;
 			}
 		}
 		for (AttributeInterface dbaseAttribute : dbasePrmKeyColl)
 		{
 			if (!entityPrmKeyColl.contains(dbaseAttribute))
 			{
 				isChanged = true;
 				break;
 			}
 		}
 
 		return isChanged;
 	}
 
 	/**
 	 * It will check weather the cardinality of the association is changed
 	 * @param association
 	 * @param dbaseCopy
 	 * @return
 	 */
 	public static boolean isCardinalityChanged(AssociationInterface association,
 			AssociationInterface dbaseCopy)
 	{
 		boolean isChanged = false;
 		Cardinality srcMaxCard = association.getSourceRole().getMaximumCardinality();
 		Cardinality tgtMaxCard = association.getTargetRole().getMaximumCardinality();
 
 		Cardinality srcMaxCardDbCpy = dbaseCopy.getSourceRole().getMaximumCardinality();
 		Cardinality tgtMaxCardDbCpy = dbaseCopy.getTargetRole().getMaximumCardinality();
 
 		if (!srcMaxCard.equals(srcMaxCardDbCpy) || !tgtMaxCard.equals(tgtMaxCardDbCpy))
 		{
 			isChanged = true;
 		}
 
 		return isChanged;
 	}
 
 	/**
 	 * It will check weather the parent of the entity is changed
 	 * @param catEntity
 	 * @param dbaseCopy
 	 * @return
 	 */
 	public static boolean isParentChanged(CategoryEntity catEntity, CategoryEntity dbaseCopy)
 	{
 		boolean isParentChanged = false;
 		if (catEntity.getParentCategoryEntity() != null
 				&& !catEntity.getParentCategoryEntity().equals(dbaseCopy.getParentCategoryEntity()))
 		{
 			isParentChanged = true;
 		}
 		else if (catEntity.getParentCategoryEntity() == null
 				&& dbaseCopy.getParentCategoryEntity() != null)
 		{
 			isParentChanged = true;
 		}
 
 		return isParentChanged;
 	}
 
 	/**
 	 * @param entity
 	 * @param dbaseCopy
 	 * @return
 	 */
 	public static boolean isParentChanged(Entity entity, Entity dbaseCopy)
 	{
 		boolean isParentChanged = false;
 		if (entity.getParentEntity() != null
 				&& !entity.getParentEntity().equals(dbaseCopy.getParentEntity()))
 		{
 			isParentChanged = true;
 		}
 		else if (entity.getParentEntity() == null && dbaseCopy.getParentEntity() != null)
 		{
 			isParentChanged = true;
 		}
 
 		return isParentChanged;
 	}
 
 	/**
 	 * @param entityGroup
 	 * @param revQueries
 	 * @param hibernateDAO
 	 * @param queries
 	 * @return
 	 * @throws DynamicExtensionsSystemException
 	 * @throws DynamicExtensionsApplicationException
 	 */
 	public List<String> getDynamicQueryList(EntityGroupInterface entityGroup,
 			List<String> revQueries, HibernateDAO hibernateDAO, List<String> queries)
 			throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException
 	{
 		DynamicExtensionBaseQueryBuilder queryBuilder = QueryBuilderFactory.getQueryBuilder();
 
 		List<EntityInterface> entities = DynamicExtensionsUtility.getUnsavedEntities(entityGroup);
 		for (EntityInterface entity : entities)
 		{
 			List<String> createQueries = queryBuilder.getCreateEntityQueryList((Entity) entity,
 					revQueries);
 			if (createQueries != null && !createQueries.isEmpty())
 			{
 				queries.addAll(createQueries);
 			}
 		}
 
 		List<EntityInterface> savedEntities = DynamicExtensionsUtility
 				.getSavedEntities(entityGroup);
 
 		try
 		{
 			for (EntityInterface savedEntity : savedEntities)
 			{
 				Entity dbaseCopy = (Entity) hibernateDAO.retrieveById(Entity.class
 						.getCanonicalName(), savedEntity.getId());
 
 				List<String> updateQueries = queryBuilder.getUpdateEntityQueryList(
 						(Entity) savedEntity, (Entity) dbaseCopy, revQueries);
 				if (updateQueries != null && !updateQueries.isEmpty())
 				{
 					queries.addAll(updateQueries);
 				}
 			}
 		}
 		catch (DAOException exception)
 		{
 			throw new DynamicExtensionsSystemException(exception.getMessage(), exception);
 		}
 
 		return queries;
 	}
 
 	/**
 	 * This method checks the data in the form.
 	 * @param tableName
 	 * @return
 	 * @throws DynamicExtensionsSystemException
 	 */
 	public boolean isDataPresent(String tableName) throws DynamicExtensionsSystemException
 	{
 		DynamicExtensionBaseQueryBuilder queryBuilder = QueryBuilderFactory.getQueryBuilder();
 		return queryBuilder.isDataPresent(tableName);
 	}
 
 	/**
 	 * getRole.
 	 * @param assoType
 	 * @param name
 	 * @param minCard
 	 * @param maxCard
 	 * @return
 	 */
 	public static RoleInterface getRole(AssociationType assoType, String name, Cardinality minCard,
 			Cardinality maxCard)
 	{
 		RoleInterface role = DomainObjectFactory.getInstance().createRole();
 		role.setAssociationsType(assoType);
 		role.setName(name);
 		role.setMinimumCardinality(minCard);
 		role.setMaximumCardinality(maxCard);
 
 		return role;
 	}
 
 	public static int getNoOfRecordInTable(String tableName)
 			throws DynamicExtensionsSystemException
 	{
 		String query = "select count(*) from " + tableName;
 		return getNoOfRecord(query);
 	}
 
 	/**
 	 * @param jdbcDAO
 	 * @param query query to be executed
 	 * @return
 	 * @throws DynamicExtensionsSystemException
 	 */
 	public static void executeDML(List<String> queryList) throws DynamicExtensionsSystemException
 	{
 		JDBCDAO jdbcDao = null;
 
 		System.out.println(queryList);
 		try
 		{
 			jdbcDao = DynamicExtensionsUtility.getJDBCDAO();
 			for (String query : queryList)
 			{
 				jdbcDao.executeUpdate(query);
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
 				jdbcDao.commit();
 				DynamicExtensionsUtility.closeJDBCDAO(jdbcDao);
 			}
 			catch (DAOException e)
 			{
 				throw new DynamicExtensionsSystemException("Error while retrieving the data", e);
 			}
 		}
 	}
 
 	/**
 	 * @param entity
 	 * @param association
 	 * @param attribute
 	 * @param originalAttribute
 	 */
 	public static String getSqlScriptToMigrateOldDataForMultiselectAttribute(
 			EntityInterface entity, AssociationInterface association, AttributeInterface attribute,
 			AttributeInterface originalAttribute)
 	{
 		return "insert into "
 				+ association.getTargetEntity().getTableProperties().getName()
 				+ "(activity_status,identifier,"
 				+ attribute.getColumnProperties().getName()
 				+ ","
 				+ association.getConstraintProperties().getTgtEntityConstraintKeyProperties()
 						.getTgtForiegnKeyColumnProperties().getName() + ") "
 				+ "(select activity_status,identifier,"
 				+ originalAttribute.getColumnProperties().getName() + " ,identifier from "
 				+ entity.getTableProperties().getName() + ")";
 	}
 
 }
