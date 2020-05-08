 
 package edu.common.dynamicextensions.entitymanager;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Stack;
 
 import org.hibernate.HibernateException;
 import org.hibernate.Session;
 import org.hibernate.Transaction;
 
 import edu.common.dynamicextensions.domain.AbstractAttribute;
 import edu.common.dynamicextensions.domain.Association;
 import edu.common.dynamicextensions.domain.Attribute;
 import edu.common.dynamicextensions.domain.BooleanAttributeTypeInformation;
 import edu.common.dynamicextensions.domain.CategoryAssociation;
 import edu.common.dynamicextensions.domain.CategoryAttribute;
 import edu.common.dynamicextensions.domain.CategoryEntity;
 import edu.common.dynamicextensions.domain.DateAttributeTypeInformation;
 import edu.common.dynamicextensions.domain.DoubleAttributeTypeInformation;
 import edu.common.dynamicextensions.domain.Entity;
 import edu.common.dynamicextensions.domain.FileAttributeTypeInformation;
 import edu.common.dynamicextensions.domain.FloatAttributeTypeInformation;
 import edu.common.dynamicextensions.domain.IntegerAttributeTypeInformation;
 import edu.common.dynamicextensions.domain.LongAttributeTypeInformation;
 import edu.common.dynamicextensions.domain.ObjectAttributeTypeInformation;
 import edu.common.dynamicextensions.domain.ShortAttributeTypeInformation;
 import edu.common.dynamicextensions.domain.StringAttributeTypeInformation;
 import edu.common.dynamicextensions.domaininterface.AbstractAttributeInterface;
 import edu.common.dynamicextensions.domaininterface.AbstractEntityInterface;
 import edu.common.dynamicextensions.domaininterface.AssociationInterface;
 import edu.common.dynamicextensions.domaininterface.AttributeInterface;
 import edu.common.dynamicextensions.domaininterface.AttributeMetadataInterface;
 import edu.common.dynamicextensions.domaininterface.AttributeTypeInformationInterface;
 import edu.common.dynamicextensions.domaininterface.BaseAbstractAttributeInterface;
 import edu.common.dynamicextensions.domaininterface.CategoryAssociationInterface;
 import edu.common.dynamicextensions.domaininterface.CategoryAttributeInterface;
 import edu.common.dynamicextensions.domaininterface.CategoryEntityInterface;
 import edu.common.dynamicextensions.domaininterface.EntityInterface;
 import edu.common.dynamicextensions.domaininterface.RoleInterface;
 import edu.common.dynamicextensions.domaininterface.databaseproperties.ConstraintPropertiesInterface;
 import edu.common.dynamicextensions.exception.DataTypeFactoryInitializationException;
 import edu.common.dynamicextensions.exception.DynamicExtensionsApplicationException;
 import edu.common.dynamicextensions.exception.DynamicExtensionsSystemException;
 import edu.common.dynamicextensions.processor.ProcessorConstants;
 import edu.common.dynamicextensions.util.DynamicExtensionsUtility;
 import edu.common.dynamicextensions.util.global.Constants;
 import edu.common.dynamicextensions.util.global.Variables;
 import edu.common.dynamicextensions.util.global.Constants.AssociationType;
 import edu.common.dynamicextensions.util.global.Constants.Cardinality;
 import edu.wustl.common.dao.HibernateDAO;
 import edu.wustl.common.exception.BizLogicException;
 import edu.wustl.common.util.Utility;
 import edu.wustl.common.util.dbManager.DBUtil;
 import edu.wustl.common.util.logger.Logger;
 
 /**
  * This class provides the methods that builds the queries that are required for
  * creation and updation of the tables of the entities.These queries are as per SQL-99 standard.
  * Theses methods can be over-ridden  in the database specific query builder class to
  * provide any database-specific implemention.
  *
  * @author Rahul Ner
  */
 class DynamicExtensionBaseQueryBuilder
 		implements
 			EntityManagerConstantsInterface,
 			EntityManagerExceptionConstantsInterface,
 			DynamicExtensionsQueryBuilderConstantsInterface
 {
 
 	EntityManagerUtil entityManagerUtil = new EntityManagerUtil();
 
 	/**
 	 * This method builds the list of all the queries that need to be executed in order to
 	 * create the data table for the entity and its associations.
 	 *
 	 * @param entity Entity for which to get the queries.
 	 * @param reverseQueryList For every data table query the method builds one more query
 	 * which negates the effect of that data table query. All such reverse queries are added in this list.
 	 * @param rollbackQueryStack
 	 * @param hibernateDAO
 	 * @param addIdAttribute
 	 *
 	 * @return List of all the data table queries
 	 *
 	 * @throws DynamicExtensionsSystemException
 	 * @throws DynamicExtensionsApplicationException
 	 */
 	public List getCreateEntityQueryList(Entity entity, List reverseQueryList, HibernateDAO hibernateDAO) throws DynamicExtensionsSystemException,
 			DynamicExtensionsApplicationException
 	{
 		List queryList = new ArrayList();
 
 		//get query to create main table with primitive attributes.
 		queryList.addAll(getCreateMainTableQuery(entity, reverseQueryList));
 
 		return queryList;
 	}
 
 	/**
 	 * This method builds the list of all the queries that need to be executed in order to
 	 * create the data table for the entity and its associations.
 	 *
 	 * @param entity Entity for which to get the queries.
 	 * @param reverseQueryList For every data table query the method builds one more query
 	 * which negates the effect of that data table query. All such reverse queries are added in this list.
 	 * @param rollbackQueryStack
 	 * @param hibernateDAO
 	 * @param addIdAttribute
 	 *
 	 * @return List of all the data table queries
 	 *
 	 * @throws DynamicExtensionsSystemException
 	 * @throws DynamicExtensionsApplicationException
 	 */
 	public List getUpdateEntityQueryList(Entity entity, List reverseQueryList, HibernateDAO hibernateDAO) throws DynamicExtensionsSystemException,
 			DynamicExtensionsApplicationException
 	{
 		List queryList = new ArrayList();
 
 		queryList.addAll(getForeignKeyConstraintQuery(entity, reverseQueryList));
 
 		// get query to create associations ,it invloves altering source/taget table or creating
 		//middle table depending upon the cardinalities.
 
 		queryList.addAll(getCreateAssociationsQueryList(entity, reverseQueryList, hibernateDAO));
 		return queryList;
 	}
 
 	/**
 	 * This method builds the list of all the queries that need to be executed in order to
 	 * create the data table for the entity and its associations.
 	 *
 	 * @param entity Entity for which to get the queries.
 	 * @param reverseQueryList For every data table query the method builds one more query
 	 * which negates the effect of that data table query. All such reverse queries are added in this list.
 	 * @param rollbackQueryStack
 	 * @param hibernateDAO
 	 * @param addIdAttribute
 	 *
 	 * @return List of all the data table queries
 	 *
 	 * @throws DynamicExtensionsSystemException
 	 * @throws DynamicExtensionsApplicationException
 	 */
 	public List<String> getUpdateCategoryEntityQueryList(CategoryEntityInterface categoryEntity, List<String> reverseQueryList,
 			HibernateDAO hibernateDAO) throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException
 	{
 		List<String> queryList = new ArrayList<String>();
 
 		// Get queries for foreign key constraint for inheritance and to create associations,
 		// it involves altering source/target table or creating middle table depending upon the
 		// cardinalities.
 		queryList.addAll(getForeignKeyConstraintQuery(categoryEntity, reverseQueryList));
 		queryList.addAll(getCreateAssociationsQueryList(categoryEntity, reverseQueryList, hibernateDAO));
 
 		return queryList;
 	}
 
 	/**
 	 * This method is used to execute the data table queries for entity in case of editing the entity.
 	 * This method takes each attribute of the entity and then scans for any changes and builds the alter query
 	 * for each attribute for the entity.
 	 *
 	 * @param entity Entity for which to generate and execute the alter queries.
 	 * @param databaseCopy Old database copy of the entity.
 	 * @param attributeRollbackQueryList rollback query list.
 	 * @return Stack Stack holding the rollback queries in case of any exception
 	 *
 	 * @throws DynamicExtensionsSystemException System exception in case of any fatal error
 	 * @throws DynamicExtensionsApplicationException Thrown in case of authentication failure or duplicate name.
 	 */
 	public List getUpdateEntityQueryList(Entity entity, Entity databaseCopy, List attributeRollbackQueryList)
 			throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException
 	{
 		Logger.out.debug("getUpdateEntityQueryList : Entering method");
 		List updateQueryList = new ArrayList();
 
 		List entityInheritanceQueryList = getInheritanceQueryList(entity, databaseCopy, attributeRollbackQueryList);
 		//get the query for any attribute that is modified.
 		List updateAttributeQueryList = getUpdateAttributeQueryList(entity, databaseCopy, attributeRollbackQueryList);
 
 		//get the query for any association that is modified.
 		List updateassociationsQueryList = getUpdateAssociationsQueryList(entity, databaseCopy, attributeRollbackQueryList);
 
 		updateQueryList.addAll(entityInheritanceQueryList);
 		updateQueryList.addAll(updateAttributeQueryList);
 		updateQueryList.addAll(updateassociationsQueryList);
 
 		Logger.out.debug("getUpdateEntityQueryList Exiting method");
 		return updateQueryList;
 	}
 
 	/**
 	 * This method is used to execute the data table queries for entity in case of editing the entity.
 	 * This method takes each attribute of the entity and then scans for any changes and builds the alter query
 	 * for each attribute for the entity.
 	 *
 	 * @param entity Entity for which to generate and execute the alter queries.
 	 * @param databaseCopy Old database copy of the entity.
 	 * @param attributeRollbackQueryList rollback query list.
 	 * @return Stack Stack holding the rollback queries in case of any exception
 	 *
 	 * @throws DynamicExtensionsSystemException System exception in case of any fatal error
 	 * @throws DynamicExtensionsApplicationException Thrown in case of authentication failure or duplicate name.
 	 */
 	public List<String> getUpdateEntityQueryList(CategoryEntity categoryEntity, CategoryEntity databaseCopy, List<String> attributeRollbackQueryList)
 			throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException
 	{
 		Logger.out.debug("getUpdateEntityQueryList : Entering method");
 		List<String> updateQueryList = new ArrayList<String>();
 
 		List<String> entityInheritanceQueryList = getInheritanceQueryList(categoryEntity, databaseCopy, attributeRollbackQueryList);
 		//get the query for any attribute that is modified.
 		List<String> updateAttributeQueryList = getUpdateAttributeQueryList(categoryEntity, databaseCopy, attributeRollbackQueryList);
 
 		//get the query for any association that is modified.
 		List<String> updateassociationsQueryList = getUpdateAssociationsQueryList(categoryEntity, databaseCopy, attributeRollbackQueryList);
 
 		updateQueryList.addAll(entityInheritanceQueryList);
 		updateQueryList.addAll(updateAttributeQueryList);
 		updateQueryList.addAll(updateassociationsQueryList);
 
 		Logger.out.debug("getUpdateEntityQueryList Exiting method");
 		return updateQueryList;
 	}
 
 	/**
 	 *
 	 * @param entity
 	 * @param databaseCopy
 	 * @param attributeRollbackQueryList
 	 * @return
 	 */
 	private List getInheritanceQueryList(Entity entity, Entity databaseCopy, List attributeRollbackQueryList)
 	{
 		List queryList = new ArrayList();
 		if (entity.getTableProperties() != null)
 		{
 			String tableName = entity.getTableProperties().getName();
 
 			if (isParentChanged(entity, databaseCopy))
 			{
 				String foreignConstraintRollbackQuery = "";
 				if (databaseCopy.getParentEntity() != null)
 				{
 					String foreignConstraintRemoveQuery = getForeignKeyRemoveConstraintQueryForInheritance(databaseCopy, databaseCopy
 							.getParentEntity());
 					foreignConstraintRollbackQuery = getForeignKeyConstraintQueryForInheritance(databaseCopy);
 					queryList.add(foreignConstraintRemoveQuery);
 					attributeRollbackQueryList.add(foreignConstraintRollbackQuery);
 				}
 
 				if (entity.getParentEntity() != null)
 				{
 					String foreignConstraintAddQuery = getForeignKeyConstraintQueryForInheritance(entity);
 
 					foreignConstraintRollbackQuery = getForeignKeyRemoveConstraintQueryForInheritance(entity, entity.getParentEntity());
 					queryList.add(foreignConstraintAddQuery);
 					attributeRollbackQueryList.add(foreignConstraintRollbackQuery);
 				}
 			}
 		}
 
 		return queryList;
 	}
 
 	/**
 	 *
 	 * @param categoryEntity
 	 * @param databaseCopy
 	 * @param attributeRollbackQueryList
 	 * @return
 	 */
 	private List<String> getInheritanceQueryList(CategoryEntity categoryEntity, CategoryEntity databaseCopy, List<String> attributeRollbackQueryList)
 	{
 		List<String> queryList = new ArrayList<String>();
 		if (categoryEntity.getTableProperties() != null)
 		{
 			if (isParentChanged(categoryEntity, databaseCopy))
 			{
 				String foreignConstraintRollbackQuery = "";
 				if (databaseCopy.getParentCategoryEntity() != null)
 				{
 					String foreignConstraintRemoveQuery = getForeignKeyRemoveConstraintQueryForInheritance(databaseCopy, databaseCopy
 							.getParentCategoryEntity());
 					foreignConstraintRollbackQuery = getForeignKeyConstraintQueryForInheritance(databaseCopy, databaseCopy.getParentCategoryEntity());
 					queryList.add(foreignConstraintRemoveQuery);
 					attributeRollbackQueryList.add(foreignConstraintRollbackQuery);
 				}
 
 				if (categoryEntity.getParentCategoryEntity() != null)
 				{
 					String foreignConstraintAddQuery = getForeignKeyConstraintQueryForInheritance(databaseCopy, databaseCopy
 							.getParentCategoryEntity());
 
 					foreignConstraintRollbackQuery = getForeignKeyRemoveConstraintQueryForInheritance(categoryEntity, categoryEntity
 							.getParentCategoryEntity());
 					queryList.add(foreignConstraintAddQuery);
 					attributeRollbackQueryList.add(foreignConstraintRollbackQuery);
 				}
 			}
 		}
 		return queryList;
 	}
 
 	/**
 	 * This method returns association value for the entity's given record.
 	 * e.g if user1 is associated with study1 and study2. The method returns the
 	 * list of record ids of study1 and study2 as the return value for the association bet'n user and study
 	 *
 	 * @param entity entity
 	 * @param recordId recordId
 	 * @return
 	 * @throws DynamicExtensionsSystemException
 	 * @throws DynamicExtensionsApplicationException
 	 */
 	public Map<Association, List> getAssociationGetRecordQueryList(EntityInterface entity, Long recordId) throws DynamicExtensionsSystemException,
 			DynamicExtensionsApplicationException
 	{
 
 		Collection associationCollection = entity.getAssociationCollection();
 		Iterator associationIterator = associationCollection.iterator();
 		StringBuffer manyToOneAssociationsGetReocrdQuery = new StringBuffer();
 		manyToOneAssociationsGetReocrdQuery.append(SELECT_KEYWORD + WHITESPACE);
 		List<Association> manyToOneAssociationList = new ArrayList<Association>();
 		String comma = "";
 
 		Map<Association, List> associationValuesMap = new HashMap<Association, List>();
 
 		while (associationIterator.hasNext())
 		{
 			Association association = (Association) associationIterator.next();
 
 			String tableName = DynamicExtensionsUtility.getTableName(association);
 			String sourceKey = association.getConstraintProperties().getSourceEntityKey();
 			String targetKey = association.getConstraintProperties().getTargetEntityKey();
 			StringBuffer query = new StringBuffer();
 
 			RoleInterface sourceRole = association.getSourceRole();
 			RoleInterface targetRole = association.getTargetRole();
 			Cardinality sourceMaxCardinality = sourceRole.getMaximumCardinality();
 			Cardinality targetMaxCardinality = targetRole.getMaximumCardinality();
 			if (sourceMaxCardinality == Cardinality.MANY && targetMaxCardinality == Cardinality.MANY)
 			{/* for Many to many get values from the middle table*/
 				query.append(SELECT_KEYWORD + WHITESPACE + targetKey);
 				query.append(WHITESPACE + FROM_KEYWORD + WHITESPACE + tableName + WHITESPACE);
 				query.append(WHITESPACE + WHERE_KEYWORD + WHITESPACE + sourceKey + EQUAL + recordId);
 				associationValuesMap.put(association, getAssociationRecordValues(query.toString()));
 			}
 			else if (sourceMaxCardinality == Cardinality.MANY && targetMaxCardinality == Cardinality.ONE)
 			{
 				/* for all Many to one associations of a single entity create a single query to get values for the target
 				 * records.
 				 *  */
 				if (manyToOneAssociationList.size() != 0)
 				{
 					manyToOneAssociationsGetReocrdQuery.append(COMMA);
 				}
 				manyToOneAssociationsGetReocrdQuery.append(WHITESPACE + sourceKey + WHITESPACE);
 				manyToOneAssociationList.add(association);
 			}
 			else
 			{
 				/* for one to many or one to one association, get taget reocrd values from the target entity table.*/
 				query.append(SELECT_KEYWORD + WHITESPACE + IDENTIFIER);
 				query.append(WHITESPACE + FROM_KEYWORD + WHITESPACE + tableName + WHITESPACE);
 				query.append(WHITESPACE + WHERE_KEYWORD + WHITESPACE + targetKey + EQUAL + recordId);
 				//  query.append(" and " + getRemoveDisbledRecordsQuery(""));
 
 				List<Long> reocordIdList = getAssociationRecordValues(query.toString());
 
 				if (association.getSourceRole().getAssociationsType().equals(AssociationType.CONTAINTMENT))
 				{
 					List<Map> containmentRecordMapList = new ArrayList<Map>();
 
 					for (Long containmentRecordId : reocordIdList)
 					{
 						Map recordMap = EntityManager.getInstance().getRecordById(association.getTargetEntity(), containmentRecordId);
 						containmentRecordMapList.add(recordMap);
 					}
 					associationValuesMap.put(association, containmentRecordMapList);
 				}
 				else
 				{
 					associationValuesMap.put(association, reocordIdList);
 				}
 			}
 		}
 
 		manyToOneAssociationsGetReocrdQuery.append(WHITESPACE + FROM_KEYWORD + WHITESPACE + entity.getTableProperties().getName() + WHITESPACE);
 		manyToOneAssociationsGetReocrdQuery.append(WHITESPACE + WHERE_KEYWORD + WHITESPACE + IDENTIFIER + EQUAL + recordId);
 
 		int noOfMany2OneAsso = manyToOneAssociationList.size();
 		if (noOfMany2OneAsso != 0)
 		{
 			Statement statement = null;
 			ResultSet resultSet = null;
 			try
 			{
 				Connection conn = DBUtil.getConnection();
 				statement = conn.createStatement();
 				resultSet = statement.executeQuery(manyToOneAssociationsGetReocrdQuery.toString());
 
 				//ResultSet resultSet = entityManagerUtil.executeQuery(manyToOneAssociationsGetReocrdQuery.toString());
 				resultSet.next();
 				for (int i = 0; i < noOfMany2OneAsso; i++)
 				{
 					Long targetRecordId = resultSet.getLong(i + 1);
 					List<Long> valueList = new ArrayList<Long>();
 					valueList.add(targetRecordId);
 					associationValuesMap.put(manyToOneAssociationList.get(i), valueList);
 				}
 			}
 			catch (SQLException e)
 			{
 				throw new DynamicExtensionsSystemException("Exception in query execution", e);
 			}
 			finally
 			{
 				try
 				{
 					resultSet.close();
 					statement.close();
 				}
 				catch (SQLException e)
 				{
 					throw new DynamicExtensionsApplicationException(e.getMessage());
 				}
 			}
 		}
 		return associationValuesMap;
 	}
 
 	/**
 	 * This method returns association value for the entity's given record.
 	 * e.g if user1 is associated with study1 and study2. The method returns the
 	 * list of record ids of study1 and study2 as the return value for the association bet'n user and study
 	 * @param entityRecord
 	 *
 	 * @param entity entity
 	 * @param recordId recordId
 	 * @return
 	 * @throws DynamicExtensionsSystemException
 	 * @throws DynamicExtensionsApplicationException
 	 */
 	public void putAssociationValues(List<AssociationInterface> associationCollection, EntityRecordResultInterface entityRecordResult,
 			EntityRecordInterface entityRecord, Long recordId) throws DynamicExtensionsSystemException
 
 	{
 		Iterator associationIterator = associationCollection.iterator();
 		StringBuffer manyToOneAssociationsGetReocrdQuery = new StringBuffer();
 		manyToOneAssociationsGetReocrdQuery.append(SELECT_KEYWORD + WHITESPACE);
 		List<Association> manyToOneAssociationList = new ArrayList<Association>();
 		String comma = "";
 
 		while (associationIterator.hasNext())
 		{
 			Association association = (Association) associationIterator.next();
 
 			int index = entityRecordResult.getEntityRecordMetadata().getAttributeList().indexOf(association);
 
 			String tableName = DynamicExtensionsUtility.getTableName(association);
 			String sourceKey = association.getConstraintProperties().getSourceEntityKey();
 			String targetKey = association.getConstraintProperties().getTargetEntityKey();
 			StringBuffer query = new StringBuffer();
 
 			RoleInterface sourceRole = association.getSourceRole();
 			RoleInterface targetRole = association.getTargetRole();
 			Cardinality sourceMaxCardinality = sourceRole.getMaximumCardinality();
 			Cardinality targetMaxCardinality = targetRole.getMaximumCardinality();
 			if (sourceMaxCardinality == Cardinality.MANY && targetMaxCardinality == Cardinality.MANY)
 			{ /* for Many to many get values from the middle table*/
 				query.append(SELECT_KEYWORD + WHITESPACE + targetKey);
 				query.append(WHITESPACE + FROM_KEYWORD + WHITESPACE + tableName + WHITESPACE);
 				query.append(WHITESPACE + WHERE_KEYWORD + WHITESPACE + sourceKey + EQUAL + recordId);
 				List<Long> manyToManyRecordIdList = getAssociationRecordValues(query.toString());
 				entityRecord.getRecordValueList().set(index, manyToManyRecordIdList);
 			}
 			else if (sourceMaxCardinality == Cardinality.MANY && targetMaxCardinality == Cardinality.ONE)
 			{
 				/* for all Many to one associations of a single entity create a single query to get values for the target
 				 * records.
 				 *  */
 				if (manyToOneAssociationList.size() != 0)
 				{
 					manyToOneAssociationsGetReocrdQuery.append(COMMA);
 				}
 				manyToOneAssociationsGetReocrdQuery.append(WHITESPACE + sourceKey + WHITESPACE);
 				manyToOneAssociationList.add(association);
 			}
 			else
 			{
 				/* for one to many or one to one association, get taget reocrd values from the target entity table.*/
 				query.append(SELECT_KEYWORD + WHITESPACE + IDENTIFIER);
 				query.append(WHITESPACE + FROM_KEYWORD + WHITESPACE + tableName + WHITESPACE);
 				query.append(WHITESPACE + WHERE_KEYWORD + WHITESPACE + targetKey + EQUAL + recordId);
 				query.append(" and " + getRemoveDisbledRecordsQuery(""));
 
 				List<Long> recordIdList = getAssociationRecordValues(query.toString());
 
 				if (association.getSourceRole().getAssociationsType().equals(AssociationType.CONTAINTMENT))
 				{
 					List<AbstractAttributeInterface> targetAttributes = new ArrayList(association.getTargetEntity().getAbstractAttributeCollection());
 					EntityRecordResultInterface containmentEntityRecordResult = EntityManager.getInstance().getEntityRecords(
 							association.getTargetEntity(), targetAttributes, recordIdList);
 					entityRecord.getRecordValueList().set(index, containmentEntityRecordResult);
 				}
 				else
 				{
 					entityRecord.getRecordValueList().set(index, recordIdList);
 				}
 			}
 		}
 
 		if (manyToOneAssociationList.size() != 0)
 		{
 			String srcEntityName = manyToOneAssociationList.get(0).getEntity().getTableProperties().getName();
 			manyToOneAssociationsGetReocrdQuery.append(WHITESPACE + FROM_KEYWORD + WHITESPACE + srcEntityName + WHITESPACE);
 			manyToOneAssociationsGetReocrdQuery.append(WHITESPACE + WHERE_KEYWORD + WHITESPACE + IDENTIFIER + EQUAL + recordId);
 			ResultSet resultSet = null;
 			try
 			{
 				resultSet = EntityManagerUtil.executeQuery(manyToOneAssociationsGetReocrdQuery.toString());
 				resultSet.next();
 				for (int i = 0; i < manyToOneAssociationList.size(); i++)
 				{
 					Long targetRecordId = resultSet.getLong(i + 1);
 					List<Long> valueList = new ArrayList<Long>();
 					valueList.add(targetRecordId);
 					AssociationInterface association = manyToOneAssociationList.get(i);
 					int index = entityRecordResult.getEntityRecordMetadata().getAttributeList().indexOf(association);
 					entityRecord.getRecordValueList().set(index, valueList);
 				}
 			}
 			catch (SQLException e)
 			{
 				throw new DynamicExtensionsSystemException("Exception in query execution", e);
 			}
 			finally
 			{
 				if (resultSet != null)
 				{
 					try
 					{
 						resultSet.close();
 					}
 					catch (SQLException e)
 					{
 						throw new DynamicExtensionsSystemException(e.getMessage(), e);
 					}
 				}
 			}
 		}
 	}
 
 	/**
 	 * returns the queries to insert data for the association.
 	 *
 	 * @param associationInterface
 	 * @param recordIdList
 	 * @param sourceRecordId
 	 * @return
 	 * @throws DynamicExtensionsApplicationException
 	 * @throws DynamicExtensionsSystemException
 	 */
 	public List<String> getAssociationInsertDataQuery(AssociationInterface associationInterface, List<Long> recordIdList, Long sourceRecordId)
 			throws DynamicExtensionsApplicationException, DynamicExtensionsSystemException
 	{
 		List<String> queryList = new ArrayList<String>();
 
 		if (recordIdList.isEmpty())
 		{
 			return queryList;
 		}
 		Association association = (Association) associationInterface;
 		if (association.getSourceRole().getAssociationsType().equals(AssociationType.CONTAINTMENT))
 		{
 			verifyCardinalityConstraints(associationInterface, sourceRecordId);
 		}
 		String sourceKey = association.getConstraintProperties().getSourceEntityKey();
 		String targetKey = association.getConstraintProperties().getTargetEntityKey();
 		StringBuffer query = new StringBuffer();
 		RoleInterface sourceRole = association.getSourceRole();
 		RoleInterface targetRole = association.getTargetRole();
 		Cardinality sourceMaxCardinality = sourceRole.getMaximumCardinality();
 		Cardinality targetMaxCardinality = targetRole.getMaximumCardinality();
 		if (sourceMaxCardinality == Cardinality.MANY && targetMaxCardinality == Cardinality.MANY)
 		{
 			Long id = entityManagerUtil.getNextIdentifier(associationInterface.getConstraintProperties().getName());
 			//for many to many insert into middle table
 			for (int i = 0; i < recordIdList.size(); i++)
 			{
 				query = new StringBuffer();
 				query.append("INSERT INTO " + association.getConstraintProperties().getName() + " ( ");
 				query.append(IDENTIFIER + "," + sourceKey + "," + targetKey);
 				query.append(" ) VALUES ( ");
 				query.append(id.toString());
 				query.append(COMMA);
 				query.append(sourceRecordId.toString());
 				query.append(COMMA);
 				query.append(recordIdList.get(i));
 				query.append(CLOSING_BRACKET);
 				id++; //TODO this is not thread safe ,so needs to find a another solution.
 
 				queryList.add(query.toString());
 			}
 
 		}
 		else if (sourceMaxCardinality == Cardinality.MANY && targetMaxCardinality == Cardinality.ONE)
 		{
 			//many to one : update source entity table
 			query.append(UPDATE_KEYWORD);
 			query.append(WHITESPACE + association.getEntity().getTableProperties().getName());
 			query.append(WHITESPACE + SET_KEYWORD + WHITESPACE + sourceKey + EQUAL + recordIdList.get(0) + WHITESPACE);
 			query.append(WHERE_KEYWORD + WHITESPACE + IDENTIFIER + EQUAL + sourceRecordId);
 			queryList.add(query.toString());
 
 		}
 		else
 		{ //one to one && onr to many : update target entity table
 			String recordIdString = recordIdList.toString();
 			recordIdString = recordIdString.replace("[", OPENING_BRACKET);
 			recordIdString = recordIdString.replace("]", CLOSING_BRACKET);
 
 			query.append(UPDATE_KEYWORD);
 			query.append(WHITESPACE + associationInterface.getTargetEntity().getTableProperties().getName());
 			query.append(WHITESPACE + SET_KEYWORD + WHITESPACE + targetKey + EQUAL + sourceRecordId + WHITESPACE);
 			query.append(WHERE_KEYWORD + WHITESPACE + IDENTIFIER + WHITESPACE + IN_KEYWORD + WHITESPACE + recordIdString);
 			queryList.add(query.toString());
 		}
 
 		return queryList;
 	}
 
 	/**
 	 * returns the queries to insert data for the association.
 	 *
 	 * @param associationInterface
 	 * @param recordIdList
 	 * @param sourceRecordId
 	 * @return
 	 * @throws DynamicExtensionsApplicationException
 	 * @throws DynamicExtensionsSystemException
 	 */
 	public List<String> getAssociationInsertDataQuery(CategoryAssociationInterface categoryAssociationInterface, List<Long> recordIdList,
 			Long sourceRecordId) throws DynamicExtensionsApplicationException, DynamicExtensionsSystemException
 	{
 		List<String> queryList = new ArrayList<String>();
 
 		if (recordIdList.isEmpty())
 		{
 			return queryList;
 		}
 		CategoryAssociation categoryAssociation = (CategoryAssociation) categoryAssociationInterface;
 		verifyCardinalityConstraints(categoryAssociation, sourceRecordId);
 		String targetKey = categoryAssociation.getConstraintProperties().getTargetEntityKey();
 		StringBuffer query = new StringBuffer();
 		//one to one && onr to many : update target entity table
 		String recordIdString = recordIdList.toString();
 		recordIdString = recordIdString.replace("[", OPENING_BRACKET);
 		recordIdString = recordIdString.replace("]", CLOSING_BRACKET);
 
 		query.append(UPDATE_KEYWORD);
 		query.append(WHITESPACE + categoryAssociation.getTargetCategoryEntity().getTableProperties().getName());
 		query.append(WHITESPACE + SET_KEYWORD + WHITESPACE + targetKey + EQUAL + sourceRecordId + WHITESPACE);
 		query.append(WHERE_KEYWORD + WHITESPACE + IDENTIFIER + WHITESPACE + IN_KEYWORD + WHITESPACE + recordIdString);
 		queryList.add(query.toString());
 		return queryList;
 	}
 
 	/**
 	 * This method creats the queries to remove records for the containtment association.
 	 *
 	 * @param association association for which records to be deleted
 	 * @param recordIdList list of record ids
 	 * @param queryList list of queries added by this method.
 	 * @return
 	 * @throws DynamicExtensionsApplicationException
 	 */
 	public void getContenmentAssociationRemoveDataQueryList(AssociationInterface association, List<Long> recordIdList, List<String> queryList,
 			boolean isLogicalDeletion) throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException
 	{
 		if (recordIdList == null || recordIdList.isEmpty())
 		{
 			return;
 		}
 
 		List<Long> childrenRecordIdList = getRecordIdListForContainment(association, recordIdList);
 		if (childrenRecordIdList == null || childrenRecordIdList.isEmpty())
 		{
 			return;
 		}
 
 		EntityInterface targetEntity = association.getTargetEntity();
 
 		/*now chk if these records are referred by some other incoming association , if so this should not be disabled*/
 		Collection<AssociationInterface> incomingAssociations = EntityManager.getInstance().getIncomingAssociations(targetEntity);
 		incomingAssociations.remove(association);
 		validateForDeleteRecord(targetEntity, childrenRecordIdList, incomingAssociations);
 
 		Collection<AssociationInterface> associationCollection = targetEntity.getAssociationCollection();
 		for (AssociationInterface targetEntityAssociation : associationCollection)
 		{
 			if (targetEntityAssociation.getSourceRole().getAssociationsType().equals(AssociationType.CONTAINTMENT))
 			{
 				getContenmentAssociationRemoveDataQueryList(targetEntityAssociation, childrenRecordIdList, queryList, isLogicalDeletion);
 			}
 		}
 
 		String tableName = DynamicExtensionsUtility.getTableName(association);
 
 		StringBuffer query = new StringBuffer();
 		if (isLogicalDeletion)
 		{
 			query.append(UPDATE_KEYWORD);
 			query.append(WHITESPACE + tableName + WHITESPACE);
 			query.append(SET_KEYWORD + Constants.ACTIVITY_STATUS_COLUMN + EQUAL + "'" + Constants.ACTIVITY_STATUS_DISABLED + "'");
 			query.append(WHERE_KEYWORD + WHITESPACE + IDENTIFIER + WHITESPACE + IN_KEYWORD);
 			query.append(WHITESPACE + getListToString(childrenRecordIdList) + WHITESPACE);
 
 		}
 		else
 		{
 			query.append(DELETE_KEYWORD);
 			query.append(WHITESPACE + tableName + WHITESPACE);
 			query.append(WHERE_KEYWORD + WHITESPACE + IDENTIFIER + WHITESPACE + IN_KEYWORD);
 			query.append(WHITESPACE + getListToString(childrenRecordIdList) + WHITESPACE);
 		}
 
 		queryList.add(query.toString());
 	}
 
 	/**
 	 * @param entity
 	 * @param recordId
 	 * @param incomingAssociations
 	 * @throws DynamicExtensionsSystemException
 	 * @throws DynamicExtensionsApplicationException
 	 */
 	public void validateForDeleteRecord(EntityInterface entity, Long recordId, Collection<AssociationInterface> incomingAssociations)
 			throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException
 	{
 		List<Long> recordIdList = new ArrayList<Long>();
 		recordIdList.add(recordId);
 		validateForDeleteRecord(entity, recordIdList, incomingAssociations);
 	}
 
 	/**
 	 * This method chkecks if the record id of given entity is referred by
 	 * some other entity in some association
 	 * @param entity
 	 * @param recordId
 	 * @throws DynamicExtensionsSystemException
 	 * @throws DynamicExtensionsApplicationException
 	 */
 	public void validateForDeleteRecord(EntityInterface entity, List<Long> recordIdList, Collection<AssociationInterface> incomingAssociations)
 			throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException
 	{
 		if (incomingAssociations == null)
 		{
 			incomingAssociations = EntityManager.getInstance().getIncomingAssociations(entity);
 		}
 
 		String tableName = "";
 		String columnName = "";
 		String sourceKey = "";
 		String targetKey = "";
 		for (AssociationInterface association : incomingAssociations)
 		{
 
 			StringBuffer query = new StringBuffer();
 			tableName = DynamicExtensionsUtility.getTableName(association);
 			sourceKey = association.getConstraintProperties().getSourceEntityKey();
 			targetKey = association.getConstraintProperties().getTargetEntityKey();
 
 			query.append(SELECT_KEYWORD + COUNT_KEYWORD + "(*)");
 			query.append(FROM_KEYWORD + tableName);
 
 			RoleInterface sourceRole = association.getSourceRole();
 			RoleInterface targetRole = association.getTargetRole();
 			Cardinality sourceMaxCardinality = sourceRole.getMaximumCardinality();
 			Cardinality targetMaxCardinality = targetRole.getMaximumCardinality();
 			if (sourceMaxCardinality == Cardinality.MANY && targetMaxCardinality == Cardinality.MANY)
 			{
 				//for many to many check into middle table
 				String srcTable = association.getEntity().getTableProperties().getName();
 				query.append(" AS m_table join " + srcTable);
 				query.append(" AS s_table on m_table." + sourceKey + "= s_table." + IDENTIFIER);
 				query.append(WHERE_KEYWORD + targetKey + WHITESPACE + IN_KEYWORD + WHITESPACE + getListToString(recordIdList));
 				query.append(" and " + DynamicExtensionBaseQueryBuilder.getRemoveDisbledRecordsQuery("s_table"));
 			}
 			else if (sourceMaxCardinality == Cardinality.MANY && targetMaxCardinality == Cardinality.ONE)
 			{
 				query.append(WHERE_KEYWORD + sourceKey + WHITESPACE + IN_KEYWORD + WHITESPACE + getListToString(recordIdList));
 				query.append(" and " + DynamicExtensionBaseQueryBuilder.getRemoveDisbledRecordsQuery(""));
 			}
 			else
 			{ //one to one && onr to many : check target entity table
 				query.append(WHERE_KEYWORD + IDENTIFIER + WHITESPACE + IN_KEYWORD + WHITESPACE + getListToString(recordIdList));
 				query.append(" and " + targetKey);
 				query.append(" and " + DynamicExtensionBaseQueryBuilder.getRemoveDisbledRecordsQuery(""));
 			}
 
 			if (entityManagerUtil.getNoOfRecord(query.toString()) != 0)
 			{
 				List<String> placeHolders = new ArrayList<String>();
 				placeHolders.add(association.getEntity().getName());
 				throw new DynamicExtensionsApplicationException("This record is refered by some record of [" + association.getEntity().getName()
 						+ "] ", null, DYEXTN_A_014, placeHolders);
 			}
 
 		}
 
 	}
 
 	/**
 	 * This method retuns contenment record id list for a given parent record id list
 	 * @param association association
 	 * @param recordIdList list of record ids for the parent
 	 * @return recordIdList list of record ids for the content child
 	 * @throws DynamicExtensionsSystemException
 	 */
 	public List<Long> getRecordIdListForContainment(AssociationInterface association, List<Long> recordIdList)
 			throws DynamicExtensionsSystemException
 	{
 		String tableName = DynamicExtensionsUtility.getTableName(association);
 
 		String targetKey = association.getConstraintProperties().getTargetEntityKey();
 
 		StringBuffer containmentRecordIdQuery = new StringBuffer();
 		containmentRecordIdQuery.append(SELECT_KEYWORD + WHITESPACE + IDENTIFIER);
 		containmentRecordIdQuery.append(WHITESPACE + FROM_KEYWORD + WHITESPACE + tableName + WHITESPACE);
 		containmentRecordIdQuery.append(WHERE_KEYWORD + WHITESPACE + targetKey + WHITESPACE + IN_KEYWORD);
 		containmentRecordIdQuery.append(WHITESPACE + getListToString(recordIdList) + WHITESPACE);
 
 		List<Long> tempList = entityManagerUtil.getResultInList(containmentRecordIdQuery.toString());
 
 		return tempList;
 	}
 
 	/**
 	 *  returns the queries to remove the the association
 	 * @param association
 	 * @param recordId
 	 * @return
 	 */
 	public String getAssociationRemoveDataQuery(Association association, Long recordId)
 	{
 		String tableName = DynamicExtensionsUtility.getTableName(association);
 		String sourceKey = association.getConstraintProperties().getSourceEntityKey();
 		String targetKey = association.getConstraintProperties().getTargetEntityKey();
 		StringBuffer query = new StringBuffer();
 
 		RoleInterface sourceRole = association.getSourceRole();
 		RoleInterface targetRole = association.getTargetRole();
 		Cardinality sourceMaxCardinality = sourceRole.getMaximumCardinality();
 		Cardinality targetMaxCardinality = targetRole.getMaximumCardinality();
 		if (sourceMaxCardinality == Cardinality.MANY && targetMaxCardinality == Cardinality.MANY)
 		{
 			//for many to many delete all the records having reffered by this recordId
 			query.append(DELETE_KEYWORD + WHITESPACE + tableName + WHITESPACE + WHERE_KEYWORD + WHITESPACE + sourceKey);
 			query.append(WHITESPACE + EQUAL);
 			query.append(recordId.toString());
 		}
 		else if (sourceMaxCardinality == Cardinality.MANY && targetMaxCardinality == Cardinality.ONE)
 		{
 			query.append(UPDATE_KEYWORD);
 			query.append(WHITESPACE + tableName);
 			query.append(WHITESPACE + SET_KEYWORD + WHITESPACE + sourceKey + EQUAL + WHITESPACE + "null" + WHITESPACE);
 			query.append(WHERE_KEYWORD + WHITESPACE + sourceKey + EQUAL + recordId);
 		}
 		else
 		{
 			//for one to many and one to one: update  target entities records(set value in target column key = null)
 			//that are reffering to  this redord by setting it to null.
 			query.append(UPDATE_KEYWORD);
 			query.append(WHITESPACE + tableName);
 			query.append(WHITESPACE + SET_KEYWORD + WHITESPACE + targetKey + EQUAL + WHITESPACE + "null" + WHITESPACE);
 			query.append(WHERE_KEYWORD + WHITESPACE + targetKey + EQUAL + recordId);
 		}
 
 		return query.toString();
 	}
 
 	/**
 	 * This method returns the main data table CREATE query that is associated with the entity.
 	 *
 	 * @param entity Entity for which to create the data table query.
 	 * @param reverseQueryList Reverse query list which holds the query to negate the data table query.
 	 * @param addIdAttribute
 	 *
 	 * @return String The method returns the "CREATE TABLE" query for the data table query for the entity passed.
 	 *
 	 * @throws DynamicExtensionsSystemException
 	 */
 	protected List<String> getCreateMainTableQuery(Entity entity, List<String> reverseQueryList) throws DynamicExtensionsSystemException
 	{
 		List<String> queryList = new ArrayList<String>();
 		if (entity.getTableProperties() != null)
 		{
 			String activityStatusString = Constants.ACTIVITY_STATUS_COLUMN + WHITESPACE + getDataTypeForStatus();
 
 			String tableName = entity.getTableProperties().getName();
 			StringBuffer query = new StringBuffer(CREATE_TABLE + " " + tableName + " " + OPENING_BRACKET + " " + activityStatusString + COMMA);
 			if (!EntityManagerUtil.isIdAttributePresent(entity))
 			{
 				query.append(IDENTIFIER).append(WHITESPACE).append(getDataTypeForIdentifier()).append(COMMA);
 			}
 			Collection<AttributeInterface> attributeCollection = entity.getAttributeCollection();
 
 			if (attributeCollection != null && !attributeCollection.isEmpty())
 			{
 				Iterator attributeIterator = attributeCollection.iterator();
 				while (attributeIterator.hasNext())
 				{
 					Attribute attribute = (Attribute) attributeIterator.next();
 
 					if (isAttributeColumnToBeExcluded(attribute))
 					{
 						//column is not created if it is multi select,file type etc.
 						continue;
 					}
 
 					String type = "";
 					//get column info for attribute
 					String attributeQueryPart = getQueryPartForAttribute(attribute, type, true);
 					query = query.append(attributeQueryPart);
 					query = query.append(COMMA);
 				}
 			}
 			if (attributeCollection != null && !attributeCollection.isEmpty())
 			{
 				Iterator attributeIterator = attributeCollection.iterator();
 				while (attributeIterator.hasNext())
 				{
 					Attribute attribute = (Attribute) attributeIterator.next();
 					if (attribute.getIsPrimaryKey() && attribute.getColumnProperties().getName() != null
 							&& !attribute.getColumnProperties().getName().equalsIgnoreCase(IDENTIFIER))
 					{
 						query = query.append(CONSTRAINT_KEYWORD + WHITESPACE + attribute.getColumnProperties().getName() + entity.getId()
 								+ WHITESPACE + UNIQUE_KEYWORD + OPENING_BRACKET + attribute.getColumnProperties().getName() + CLOSING_BRACKET);
 						query = query.append(COMMA);
 					}
 				}
 			}
 
 			query = query.append(PRIMARY_KEY_CONSTRAINT_FOR_ENTITY_DATA_TABLE + ")"); //identifier set as primary key
 
 			// add create query
 			queryList.add(query.toString());
 			//		// add foerfign key query for inheritance
 			//		if (parentEntity != null) {
 			//			String foreignKeyConstraintQueryForInheritance = getForeignKeyConstraintQueryForInheritance(entity);
 			//			queryList.add(foreignKeyConstraintQueryForInheritance);
 			//		}
 
 			String reverseQuery = getReverseQueryForAbstractEntityTable(entity.getTableProperties().getName());
 			reverseQueryList.add(reverseQuery);
 		}
 		return queryList;
 	}
 
 	/**
 	 * This method builds the list of all the queries that need to be executed in order to
 	 * create the data table for the entity and its associations.
 	 *
 	 * @param category Category for which to get the queries.
 	 * @param reverseQueryList For every data table query the method builds one more query
 	 * which negates the effect of that data table query. All such reverse queries are added in this list.
 	 * @param rollbackQueryStack
 	 * @param hibernateDAO
 	 * @param addIdAttribute
 	 *
 	 * @return List of all the data table queries
 	 *
 	 * @throws DynamicExtensionsSystemException
 	 * @throws DynamicExtensionsApplicationException
 	 */
 	public List<String> getCreateCategoryQueryList(CategoryEntityInterface categoryEntity, List<String> reverseQueryList, HibernateDAO hibernateDAO)
 			throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException
 	{
 		List<String> queryList = getCreateCategoryEntityTableQuery(categoryEntity, reverseQueryList);
 		return queryList;
 	}
 
 	/**
 	 * This method is used to create a table for each category entity.
 	 * @param category
 	 * @param reverseQueryList
 	 * @return list of category table creation queries.
 	 * @throws DynamicExtensionsSystemException
 	 */
 	protected List<String> getCreateCategoryEntityTableQuery(CategoryEntityInterface categoryEntity, List<String> reverseQueryList)
 			throws DynamicExtensionsSystemException
 	{
 		List<String> queryList = new ArrayList<String>();
 		if (categoryEntity.getTableProperties() != null)
 		{
 			String activityStatusString = Constants.ACTIVITY_STATUS_COLUMN + WHITESPACE + getDataTypeForStatus();
 
 			String tableName = categoryEntity.getTableProperties().getName();
 			StringBuffer query = new StringBuffer(CREATE_TABLE + " " + tableName + " " + OPENING_BRACKET + " " + activityStatusString + COMMA);
 			query.append(IDENTIFIER).append(WHITESPACE).append(getDataTypeForIdentifier()).append(COMMA);
 			query = query.append("record_Id" + WHITESPACE + getDataTypeForIdentifier() + WHITESPACE + "NOT NULL" + COMMA);
 			query = query.append(PRIMARY_KEY_CONSTRAINT_FOR_ENTITY_DATA_TABLE + ")"); //identifier set as primary key
 			queryList.add(query.toString());
 
 			String reverseQuery = getReverseQueryForAbstractEntityTable(categoryEntity.getTableProperties().getName());
 			reverseQueryList.add(reverseQuery);
 		}
 		return queryList;
 	}
 
 	/**
 	 * getForeignKeyConstraintQuery.
 	 * @param entity
 	 * @param reverseQueryList
 	 * @return
 	 */
 	private List<String> getForeignKeyConstraintQuery(Entity entity, List<String> reverseQueryList)
 	{
 		List<String> queryList = new ArrayList<String>();
 		EntityInterface parentEntity = entity.getParentEntity();
 		//    	 add foerfign key query for inheritance
 		if (parentEntity != null)
 		{
 			String foreignKeyConstraintQueryForInheritance = getForeignKeyConstraintQueryForInheritance(entity);
 			queryList.add(foreignKeyConstraintQueryForInheritance);
 			String foreignKeyRemoveConstraintQueryForInheritance = getForeignKeyRemoveConstraintQueryForInheritance(entity, parentEntity);
 			reverseQueryList.add(foreignKeyRemoveConstraintQueryForInheritance);
 		}
 		return queryList;
 	}
 
 	/**
 	 * getForeignKeyConstraintQuery.
 	 * @param entity
 	 * @param reverseQueryList
 	 * @return
 	 */
 	private List<String> getForeignKeyConstraintQuery(CategoryEntityInterface categoryEntity, List<String> reverseQueryList)
 	{
 		List<String> queryList = new ArrayList<String>();
 		CategoryEntity parentCategoryEntity = (CategoryEntity) categoryEntity.getParentCategoryEntity();
 		//    	 add foerfign key query for inheritance
 		if (parentCategoryEntity != null && parentCategoryEntity.isCreateTable())
 		{
 			categoryEntity.getAttributeByName(IDENTIFIER);
 			String foreignKeyConstraintQueryForInheritance = getForeignKeyConstraintQueryForInheritance(categoryEntity, parentCategoryEntity);
 			queryList.add(foreignKeyConstraintQueryForInheritance);
 			String foreignKeyRemoveConstraintQueryForInheritance = getForeignKeyRemoveConstraintQueryForInheritance(categoryEntity,
 					parentCategoryEntity);
 			reverseQueryList.add(foreignKeyRemoveConstraintQueryForInheritance);
 		}
 		return queryList;
 	}
 
 	/**
 	 * This method returns the query to add foreign key constraint in the given child entity
 	 * that refers to identifier column of the parent.
 	 * @param entity
 	 * @return
 	 */
 	protected String getForeignKeyConstraintQueryForInheritance(EntityInterface entity)
 	{
 		EntityInterface parentEntity = entity.getParentEntity();
 		return getForeignKeyConstraintQueryForInheritance(entity, parentEntity);
 	}
 
 	/**
 	 *
 	 * @param entity
 	 * @param parentEntity
 	 * @return
 	 */
 	protected String getForeignKeyConstraintQueryForInheritance(AbstractEntityInterface entity, AbstractEntityInterface parentEntity)
 	{
 		StringBuffer foreignKeyConstraint = new StringBuffer();
 		String foreignConstraintName = entity.getTableProperties().getConstraintName() + UNDERSCORE
 				+ parentEntity.getTableProperties().getConstraintName();
 
 		foreignKeyConstraint.append(ALTER_TABLE).append(WHITESPACE).append(entity.getTableProperties().getName()).append(WHITESPACE).append(
 				ADD_KEYWORD).append(WHITESPACE).append(CONSTRAINT_KEYWORD).append(WHITESPACE).append(foreignConstraintName).append(
 				FOREIGN_KEY_KEYWORD).append(OPENING_BRACKET).append(IDENTIFIER).append(CLOSING_BRACKET).append(WHITESPACE).append(REFERENCES_KEYWORD)
 				.append(WHITESPACE).append(parentEntity.getTableProperties().getName()).append(OPENING_BRACKET).append(IDENTIFIER).append(
 						CLOSING_BRACKET);
 		return foreignKeyConstraint.toString();
 	}
 
 	/**
 	 * This method returns the query to add foreign key constraint in the given child entity
 	 * that refers to identifier column of the parent.
 	 * @param entity
 	 * @return
 	 */
 	protected String getForeignKeyRemoveConstraintQueryForInheritance(AbstractEntityInterface entity, AbstractEntityInterface parentEntity)
 	{
 		StringBuffer foreignKeyConstraint = new StringBuffer();
 		String foreignConstraintName = entity.getTableProperties().getConstraintName() + UNDERSCORE
 				+ parentEntity.getTableProperties().getConstraintName();
 
 		foreignKeyConstraint.append(ALTER_TABLE).append(WHITESPACE).append(entity.getTableProperties().getName()).append(WHITESPACE).append(
 				DROP_KEYWORD).append(WHITESPACE).append(CONSTRAINT_KEYWORD).append(WHITESPACE).append(foreignConstraintName);
 
 		return foreignKeyConstraint.toString();
 	}
 
 	/**
 	 * This method returns the dabase type for idenitifier.
 	 * @return String database type for the identifier.
 	 * @throws DynamicExtensionsSystemException exception is thrown if factory is not instanciated properly.
 	 */
 	protected String getDataTypeForIdentifier() throws DynamicExtensionsSystemException
 	{
 		DataTypeFactory dataTypeFactory = DataTypeFactory.getInstance();
 		return dataTypeFactory.getDatabaseDataType("Integer");
 	}
 
 	/**
 	 * This method returns the dabase type for idenitifier.
 	 * @return String database type for the identifier.
 	 * @throws DynamicExtensionsSystemException exception is thrown if factory is not instanciated properly.
 	 */
 	protected String getDataTypeForStatus() throws DynamicExtensionsSystemException
 	{
 		DataTypeFactory dataTypeFactory = DataTypeFactory.getInstance();
 		return dataTypeFactory.getDatabaseDataType("String");
 	}
 
 	/**
 	 * This method returns true if a column in not to be created for the attribute.
 	 * @return
 	 */
 	protected boolean isAttributeColumnToBeExcluded(AttributeInterface attribute)
 	{
 		boolean isExclude = false;
 
 		if (attribute.getIsCollection() != null && attribute.getIsCollection())
 		{
 			isExclude = true;
 		}
 		else
 		{
 			AttributeTypeInformationInterface typeInfo = attribute.getAttributeTypeInformation();
 
 			if (typeInfo instanceof FileAttributeTypeInformation || typeInfo instanceof ObjectAttributeTypeInformation)
 			{
 				isExclude = true;
 			}
 		}
 
 		return isExclude;
 	}
 
 	/**
 	 * This method builds the query part for the primitive attribute
 	 * @param attribute primitive attribute for which to build the query.
 	 * @return String query part of the primitive attribute.
 	 * @throws DataTypeFactoryInitializationException
 	 */
 	protected String getQueryPartForAttribute(Attribute attribute, String type, boolean processConstraints) throws DynamicExtensionsSystemException
 	{
 
 		String attributeQuery = null;
 		if (attribute != null)
 		{
 			String columnName = attribute.getColumnProperties().getName();
 			//String isUnique = "";
 			String nullConstraint = "";
 			//			String defaultConstraint = "";
 			if (processConstraints)
 			{
 				//                if (attribute.getIsPrimaryKey()) {
 				//                    isUnique = CONSTRAINT_KEYWORD + WHITESPACE + attribute.getColumnProperties().getName()
 				//                            + UNDERSCORE + UNIQUE_CONSTRAINT_SUFFIX + WHITESPACE + UNIQUE_KEYWORD;
 				//                }
 				nullConstraint = "NULL";
 
 				if (!attribute.getIsNullable())
 				{
 					nullConstraint = "NOT NULL";
 				}
 				// dont need to specify this deflaut value
 				//				if (attribute.getAttributeTypeInformation().getDefaultValue() != null
 				//						&& attribute.getAttributeTypeInformation().getDefaultValue()
 				//								.getValueAsObject() != null)
 				//				{
 				//					defaultConstraint = DEFAULT_KEYWORD
 				//							+ WHITESPACE
 				//							+ EntityManagerUtil.getFormattedValue(attribute, attribute
 				//									.getAttributeTypeInformation().getDefaultValue()
 				//									.getValueAsObject());
 				//				}
 
 			}
 
 			attributeQuery = columnName + WHITESPACE + type + WHITESPACE + getDatabaseTypeAndSize(attribute) //+ WHITESPACE + defaultConstraint
 					+ WHITESPACE + nullConstraint;
 		}
 		return attributeQuery;
 	}
 
 	/**
 	 * This method builds the query part for the primitive attribute
 	 * @param attribute primitive attribute for which to build the query.
 	 * @return String query part of the primitive attribute.
 	 * @throws DataTypeFactoryInitializationException
 	 */
 	protected String getQueryPartForCategoryAttribute(CategoryAttributeInterface attribute, String type, boolean processConstraints)
 			throws DynamicExtensionsSystemException
 	{
 		String attributeQuery = null;
 		if (attribute != null)
 		{
 			String columnName = attribute.getColumnProperties().getName();
 			String nullConstraint = "";
 			if (processConstraints)
 			{
 				nullConstraint = "NULL";
 			}
 			attributeQuery = columnName + WHITESPACE + type + WHITESPACE + getDataTypeForIdentifier() + WHITESPACE + nullConstraint;
 		}
 		return attributeQuery;
 	}
 
 	/**
 	 * This method returns the database type and size of the attribute passed to it which becomes the part of the query for that attribute.
 	 * @param attribute Attribute object for which to get the database type and size.
 	 * @return String that specifies the data base type and size.
 	 * @throws DynamicExtensionsSystemException
 	 * @throws DataTypeFactoryInitializationException
 	 */
 	protected String getDatabaseTypeAndSize(AttributeMetadataInterface attribute) throws DynamicExtensionsSystemException
 
 	{
 		try
 		{
 			DataTypeFactory dataTypeFactory = DataTypeFactory.getInstance();
 			AttributeTypeInformationInterface attributeInformation = attribute.getAttributeTypeInformation();
 			if (attributeInformation instanceof StringAttributeTypeInformation)
 			{
 				return dataTypeFactory.getDatabaseDataType("String");
 			}
 			else if (attributeInformation instanceof IntegerAttributeTypeInformation)
 			{
 				return dataTypeFactory.getDatabaseDataType("Integer");
 			}
 			else if (attributeInformation instanceof DateAttributeTypeInformation)
 			{
 				DateAttributeTypeInformation dateAttributeInformation = (DateAttributeTypeInformation) attributeInformation;
 				String format = dateAttributeInformation.getFormat();
 				if (format != null && format.equalsIgnoreCase(ProcessorConstants.DATE_TIME_FORMAT))
 				{
 					return dataTypeFactory.getDatabaseDataType("DateTime");
 				}
 				else
 				{
 					return dataTypeFactory.getDatabaseDataType("Date");
 				}
 			}
 			else if (attributeInformation instanceof FloatAttributeTypeInformation)
 			{
 				return dataTypeFactory.getDatabaseDataType("Float");
 			}
 			else if (attributeInformation instanceof BooleanAttributeTypeInformation)
 			{
 				return dataTypeFactory.getDatabaseDataType("Boolean");
 			}
 			else if (attributeInformation instanceof DoubleAttributeTypeInformation)
 			{
 				return dataTypeFactory.getDatabaseDataType("Double");
 			}
 			else if (attributeInformation instanceof LongAttributeTypeInformation)
 			{
 				return dataTypeFactory.getDatabaseDataType("Long");
 			}
 			else if (attributeInformation instanceof ShortAttributeTypeInformation)
 			{
 				return dataTypeFactory.getDatabaseDataType("Short");
 			}
 
 		}
 		catch (DataTypeFactoryInitializationException e)
 		{
 			throw new DynamicExtensionsSystemException("Could Not get data type attribute", e);
 		}
 
 		return null;
 	}
 
 	/**
 	 * This method gives the opposite query to negate the effect of "CREATE TABLE" query for the data table for the entity.
 	 * @param entity Entity for which query generation is done.
 	 * @return String query that basically holds the "DROP TABLE" query.
 	 */
 	protected String getReverseQueryForAbstractEntityTable(String tableName)
 	{
 		String query = null;
 		if (tableName != null && tableName.length() > 0)
 		{
 			query = "Drop table" + " " + tableName;
 		}
 		return query;
 	}
 
 	/**
 	 * This method returns all the CREATE table entries for associations present in the entity.
 	 * @param entity Entity object from which to get the associations.
 	 * @param reverseQueryList Reverse query list that holds the reverse queries.
 	 * @param rollbackQueryStack
 	 * @param hibernateDAO
 	 * @return List of queries
 	 * @throws DynamicExtensionsSystemException
 	 * @throws DynamicExtensionsApplicationException
 	 */
 	protected List getCreateAssociationsQueryList(Entity entity, List reverseQueryList, HibernateDAO hibernateDAO)
 			throws DynamicExtensionsApplicationException, DynamicExtensionsSystemException
 	{
 		Collection associationCollection = entity.getAssociationCollection();
 		List associationQueryList = new ArrayList();
 		if (associationCollection != null && !associationCollection.isEmpty())
 		{
 			Iterator associationIterator = associationCollection.iterator();
 			while (associationIterator.hasNext())
 			{
 				AssociationInterface association = (AssociationInterface) associationIterator.next();
 				if (((Association) association).getIsSystemGenerated())
 				{ //no need to process system generated association
 					continue;
 				}
 				boolean isAddAssociationQuery = true;
 				String associationQuery = getQueryPartForAssociation(association, reverseQueryList, isAddAssociationQuery);
 				associationQueryList.add(associationQuery);
 			}
 		}
 		return associationQueryList;
 	}
 
 	/**
 	 *
 	 * @param associationCollection
 	 * @param reverseQueryList
 	 * @param hibernateDAO
 	 * @return
 	 */
 	protected List<String> getCreateAssociationsQueryList(CategoryEntityInterface entity, List<String> reverseQueryList, HibernateDAO hibernateDAO)
 			throws DynamicExtensionsApplicationException, DynamicExtensionsSystemException
 	{
 		Collection<CategoryAssociationInterface> associationCollection = entity.getCategoryAssociationCollection();
 		List<String> associationQueryList = new ArrayList<String>();
 		if (associationCollection != null && !associationCollection.isEmpty())
 		{
 			for (CategoryAssociationInterface categoryAssociationInterface : associationCollection)
 			{
 				boolean isAddAssociationQuery = true;
 				String associationQuery = getQueryPartForAssociation(categoryAssociationInterface, reverseQueryList, isAddAssociationQuery);
 				associationQueryList.add(associationQuery);
 			}
 		}
 		return associationQueryList;
 	}
 
 	/**
 	 * This method builds the query part for the association.
 	 *
 	 * @param association Association object for which to build the query.
 	 * @param reverseQueryList rollback query list
 	 * @param isAddAssociationQuery boolean indicating whether to create query for
 	 *        add association or remove association.
 	 * @return
 	 * @throws DynamicExtensionsSystemException
 	 */
 	public String getQueryPartForAssociation(AssociationInterface association, List reverseQueryList, boolean isAddAssociationQuery)
 			throws DynamicExtensionsSystemException
 	{
 		Logger.out.debug("getQueryPartForAssociation Entering method");
 
 		StringBuffer query = new StringBuffer();
 		EntityInterface sourceEntity = association.getEntity();
 		EntityInterface targetEntity = association.getTargetEntity();
 		RoleInterface sourceRole = association.getSourceRole();
 		RoleInterface targetRole = association.getTargetRole();
 		Cardinality sourceMaxCardinality = sourceRole.getMaximumCardinality();
 		Cardinality targetMaxCardinality = targetRole.getMaximumCardinality();
 		ConstraintPropertiesInterface constraintProperties = association.getConstraintProperties();
 		String tableName = "";
 
 		String dataType = getDataTypeForIdentifier();
 		if (sourceMaxCardinality == Cardinality.MANY && targetMaxCardinality == Cardinality.MANY)
 		{
 			//for many-many a middle table is created.
 			tableName = constraintProperties.getName();
 
 			query.append(CREATE_TABLE + WHITESPACE + tableName + WHITESPACE + OPENING_BRACKET + WHITESPACE + IDENTIFIER + WHITESPACE + dataType
 					+ COMMA);
 			query.append(constraintProperties.getSourceEntityKey() + WHITESPACE + dataType + COMMA);
 			query.append(constraintProperties.getTargetEntityKey() + WHITESPACE + dataType + COMMA + WHITESPACE);
 			query.append(PRIMARY_KEY_CONSTRAINT_FOR_ENTITY_DATA_TABLE + CLOSING_BRACKET);
 			String rollbackQuery = DROP_KEYWORD + WHITESPACE + TABLE_KEYWORD + WHITESPACE + tableName;
 
 			if (isAddAssociationQuery)
 			{
 				reverseQueryList.add(rollbackQuery);
 			}
 			else
 			{
 				reverseQueryList.add(query.toString());
 				query = new StringBuffer(rollbackQuery);
 			}
 		}
 		else if (sourceMaxCardinality == Cardinality.MANY && targetMaxCardinality == Cardinality.ONE)
 		{
 			//for many to one, a column is added into source entity table.
 			tableName = sourceEntity.getTableProperties().getName();
 			String columnName = constraintProperties.getSourceEntityKey();
 			query.append(getAddAttributeQuery(tableName, columnName, dataType, reverseQueryList, isAddAssociationQuery));
 		}
 		else
 		{
 			//for one to one and one to many, a column is added into target entity table.
 			tableName = targetEntity.getTableProperties().getName();
 			String columnName = constraintProperties.getTargetEntityKey();
 			query.append(getAddAttributeQuery(tableName, columnName, dataType, reverseQueryList, isAddAssociationQuery));
 
 		}
 
 		Logger.out.debug("getQueryPartForAssociation exiting method");
 		return query.toString();
 	}
 
 	/**
 	 * This method builds the query part for the association.
 	 *
 	 * @param association Association object for which to build the query.
 	 * @param reverseQueryList rollback query list
 	 * @param isAddAssociationQuery boolean indicating whether to create query for
 	 *        add association or remove association.
 	 * @return
 	 * @throws DynamicExtensionsSystemException
 	 */
 	public String getQueryPartForAssociation(CategoryAssociationInterface association, List reverseQueryList, boolean isAddAssociationQuery)
 			throws DynamicExtensionsSystemException
 	{
 		StringBuffer query = new StringBuffer();
 		Logger.out.debug("getQueryPartForAssociation Entering method");
 		CategoryEntityInterface targetEntity = association.getTargetCategoryEntity();
 		//for one to one and one to many, a column is added into target entity table.
 		String tableName = targetEntity.getTableProperties().getName();
 		String columnName = association.getConstraintProperties().getTargetEntityKey();
 		query.append(getAddAttributeQuery(tableName, columnName, getDataTypeForIdentifier(), reverseQueryList, isAddAssociationQuery));
 		Logger.out.debug("getQueryPartForAssociation exiting method");
 		return query.toString();
 	}
 
 	protected String getAddAttributeQuery(String tableName, String columnName, String dataType, List reverseQueryList, boolean isAddAssociationQuery)
 	{
 		StringBuffer query = new StringBuffer();
 		query.append(ALTER_TABLE + WHITESPACE + tableName + WHITESPACE + ADD_KEYWORD + WHITESPACE);
 		query.append(columnName + WHITESPACE + dataType + WHITESPACE);
 		String rollbackQuery = ALTER_TABLE + WHITESPACE + tableName + WHITESPACE + DROP_KEYWORD + WHITESPACE + COLUMN_KEYWORD + WHITESPACE
 				+ columnName;
 
 		//		query.append(REFERENCES_KEYWORD + WHITESPACE + sourceEntity.getTableProperties().getName() + OPENING_BRACKET + IDENTIFIER + CLOSING_BRACKET + COMMA);
 
 		if (isAddAssociationQuery)
 		{
 			reverseQueryList.add(rollbackQuery);
 			return query.toString();
 		}
 		else
 		{
 			reverseQueryList.add(query.toString());
 			return rollbackQuery;
 		}
 	}
 
 	/**
 	 * returns queries for any attribute that is modified.
 	 * @param entity entity
 	 * @param databaseCopy its database copy to compare with
 	 * @param attributeRollbackQueryList rollback query list
 	 * @return query list
 	 *
 	 * @throws DynamicExtensionsSystemException
 	 * @throws DynamicExtensionsApplicationException
 	 */
 	protected List getUpdateAttributeQueryList(Entity entity, Entity databaseCopy, List attributeRollbackQueryList)
 			throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException
 	{
 		Logger.out.debug("getUpdateAttributeQueryList Entering method");
 		Collection attributeCollection = entity.getAttributeCollection();
 		List attributeQueryList = new ArrayList();
 
 		if (attributeCollection != null && !attributeCollection.isEmpty())
 		{
 			Iterator attributeIterator = attributeCollection.iterator();
 
 			while (attributeIterator.hasNext())
 			{
 				Attribute attribute = (Attribute) attributeIterator.next();
 				Attribute savedAttribute = (Attribute) databaseCopy.getAttributeByIdentifier(attribute.getId());
 
 				if (isAttributeColumnToBeAdded(attribute, savedAttribute))
 				{
 					//either attribute is newly added or previously excluded(file type/multiselect) attribute
 					//modified sh that now its column needs to add.
 					String attributeQuery = processAddAttribute(attribute, attributeRollbackQueryList);
 					attributeQueryList.add(attributeQuery);
 				}
 				else
 				{
 					//check for other modification in the attributes such a unique constriant change.
 					List modifiedAttributeQueryList = processModifyAttribute(attribute, savedAttribute, attributeRollbackQueryList);
 					attributeQueryList.addAll(modifiedAttributeQueryList);
 				}
 
 			}
 
 		}
 
 		processRemovedAttributes(entity, databaseCopy, attributeQueryList, attributeRollbackQueryList);
 
 		Logger.out.debug("getUpdateAttributeQueryList Exiting method");
 		return attributeQueryList;
 	}
 
 	/**
 	 * returns queries for any attribute that is modified.
 	 * @param entity entity
 	 * @param databaseCopy its database copy to compare with
 	 * @param attributeRollbackQueryList rollback query list
 	 * @return query list
 	 *
 	 * @throws DynamicExtensionsSystemException
 	 * @throws DynamicExtensionsApplicationException
 	 */
 	protected List<String> getUpdateAttributeQueryList(CategoryEntity categoryEntity, CategoryEntity databaseCopy,
 			List<String> attributeRollbackQueryList) throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException
 	{
 		Logger.out.debug("getUpdateAttributeQueryList Entering method");
 		Collection<CategoryAttributeInterface> attributeCollection = categoryEntity.getCategoryAttributeCollection();
 		List<String> attributeQueryList = new ArrayList<String>();
 
 		if (attributeCollection != null)
 		{
 			for (CategoryAttributeInterface categoryAttributeInterface : attributeCollection)
 			{
 				CategoryAttribute attribute = (CategoryAttribute) categoryAttributeInterface;
 				CategoryAttribute savedAttribute = (CategoryAttribute) databaseCopy.getAttributeByIdentifier(attribute.getId());
 
 				if (isAbstarctAttributeColumnToBeAdded(attribute, savedAttribute))
 				{
 					//either attribute is newly added or previously excluded(file type/multiselect) attribute
 					//modified sh that now its column needs to add.
 					String attributeQuery = processAddAttribute(attribute, attributeRollbackQueryList);
 					attributeQueryList.add(attributeQuery);
 				}
 			}
 		}
 
 		processRemovedAttributes(categoryEntity, databaseCopy, attributeQueryList, attributeRollbackQueryList);
 
 		Logger.out.debug("getUpdateAttributeQueryList Exiting method");
 		return attributeQueryList;
 	}
 
 	/**
 	 * @param entity
 	 * @param databaseCopy
 	 * @param attributeRollbackQueryList
 	 * @return
 	 */
 	protected List getUpdateAssociationsQueryList(Entity entity, Entity databaseCopy, List attributeRollbackQueryList)
 			throws DynamicExtensionsSystemException
 	{
 		Logger.out.debug("getUpdateAssociationsQueryList Entering method");
 		List associationsQueryList = new ArrayList();
 		boolean isAddAssociationQuery = true;
 
 		Collection associationCollection = entity.getAssociationCollection();
 
 		if (associationCollection != null && !associationCollection.isEmpty())
 		{
 			Iterator associationIterator = associationCollection.iterator();
 
 			while (associationIterator.hasNext())
 			{
 				Association association = (Association) associationIterator.next();
 				Association associationDatabaseCopy = (Association) databaseCopy.getAssociationByIdentifier(association.getId());
 
 				if (association.getIsSystemGenerated())
 				{
 					continue;
 				}
 				if (associationDatabaseCopy == null)
 				{
 					isAddAssociationQuery = true;
 					String newAssociationQuery = getQueryPartForAssociation(association, attributeRollbackQueryList, isAddAssociationQuery);
 					associationsQueryList.add(newAssociationQuery);
 				}
 				else
 				{
 					if (isCardinalityChanged(association, associationDatabaseCopy))
 					{
 						isAddAssociationQuery = false;
 						String savedAssociationRemoveQuery = getQueryPartForAssociation(associationDatabaseCopy, attributeRollbackQueryList,
 								isAddAssociationQuery);
 						associationsQueryList.add(savedAssociationRemoveQuery);
 
 						isAddAssociationQuery = true;
 						String newAssociationAddQuery = getQueryPartForAssociation(association, attributeRollbackQueryList, isAddAssociationQuery);
 						associationsQueryList.add(newAssociationAddQuery);
 					}
 				}
 			}
 		}
 		processRemovedAssociation(entity, databaseCopy, associationsQueryList, attributeRollbackQueryList);
 
 		Logger.out.debug("getUpdateAssociationsQueryList Exiting method");
 		return associationsQueryList;
 	}
 
 	/**
 	 * @param entity
 	 * @param databaseCopy
 	 * @param attributeRollbackQueryList
 	 * @return
 	 */
 	protected List<String> getUpdateAssociationsQueryList(CategoryEntity categoryEntity, CategoryEntity databaseCopy,
 			List<String> attributeRollbackQueryList) throws DynamicExtensionsSystemException
 	{
 		Logger.out.debug("getUpdateAssociationsQueryList Entering method");
 		List<String> associationsQueryList = new ArrayList<String>();
 		boolean isAddAssociationQuery = true;
 
 		Collection<CategoryAssociationInterface> associationCollection = categoryEntity.getCategoryAssociationCollection();
 
 		if (associationCollection != null)
 		{
 			for (CategoryAssociationInterface categoryAssociationInterface : associationCollection)
 			{
 				CategoryAssociation associationDatabaseCopy = (CategoryAssociation) databaseCopy
 						.getAssociationByIdentifier(categoryAssociationInterface.getId());
 
 				if (isAbstarctAttributeColumnToBeAdded(categoryAssociationInterface, associationDatabaseCopy))
 				{
 					isAddAssociationQuery = true;
 					String newAssociationQuery = getQueryPartForAssociation(categoryAssociationInterface, attributeRollbackQueryList,
 							isAddAssociationQuery);
 					associationsQueryList.add(newAssociationQuery);
 				}
 			}
 		}
 		processRemovedAssociation(categoryEntity, databaseCopy, associationsQueryList, attributeRollbackQueryList);
 
 		Logger.out.debug("getUpdateAssociationsQueryList Exiting method");
 		return associationsQueryList;
 	}
 
 	/**
 	 * This method processes all the attributes that previoulsy saved but removed by editing.
 	 * @param entity entity
 	 * @param databaseCopy databaseCopy
 	 * @param attributeQueryList attributeQueryList
 	 * @param attributeRollbackQueryList attributeRollbackQueryList
 	 * @throws DynamicExtensionsSystemException
 	 * @throws DynamicExtensionsApplicationException
 	 */
 	protected void processRemovedAttributes(Entity entity, Entity databaseCopy, List attributeQueryList, List attributeRollbackQueryList)
 			throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException
 	{
 		Collection savedAttributeCollection = databaseCopy.getAttributeCollection();
 		//savedAttributeCollection = entityManagerUtil.filterSystemAttributes(savedAttributeCollection);
 		if (entity.getTableProperties() != null)
 		{
 			String tableName = entity.getTableProperties().getName();
 
 			if (savedAttributeCollection != null && !savedAttributeCollection.isEmpty())
 			{
 				Iterator savedAttributeIterator = savedAttributeCollection.iterator();
 				while (savedAttributeIterator.hasNext())
 				{
 
 					Attribute savedAttribute = (Attribute) savedAttributeIterator.next();
 					Attribute attribute = (Attribute) entity.getAttributeByIdentifier(savedAttribute.getId());
 
 					if (attribute == null && isDataPresent(tableName, savedAttribute))
 					{
 						throw new DynamicExtensionsApplicationException("data is present ,attribute can not be deleted", null, DYEXTN_A_013);
 					}
 
 					//attribute is removed or modified such that its column need to be removed
 					if (isAttributeColumnToBeRemoved(attribute, savedAttribute))
 					{
 						String columnName = savedAttribute.getColumnProperties().getName();
 
 						String removeAttributeQuery = ALTER_TABLE + WHITESPACE + tableName + WHITESPACE + DROP_KEYWORD + WHITESPACE + COLUMN_KEYWORD
 								+ WHITESPACE + columnName;
 						String type = "";
 
 						String removeAttributeQueryRollBackQuery = ALTER_TABLE + WHITESPACE + tableName + WHITESPACE + ADD_KEYWORD + WHITESPACE
 								+ getQueryPartForAttribute(savedAttribute, type, true);
 
 						attributeQueryList.add(removeAttributeQuery);
 						attributeRollbackQueryList.add(removeAttributeQueryRollBackQuery);
 					}
 				}
 			}
 		}
 	}
 
 	/**
 	 * This method processes all the attributes that previoulsy saved but removed by editing.
 	 * @param entity entity
 	 * @param databaseCopy databaseCopy
 	 * @param attributeQueryList attributeQueryList
 	 * @param attributeRollbackQueryList attributeRollbackQueryList
 	 * @throws DynamicExtensionsSystemException
 	 * @throws DynamicExtensionsApplicationException
 	 */
 	protected void processRemovedAttributes(CategoryEntity categoryEntity, CategoryEntity databaseCopy, List<String> attributeQueryList,
 			List<String> attributeRollbackQueryList) throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException
 	{
 		Collection<CategoryAttributeInterface> savedAttributeCollection = databaseCopy.getCategoryAttributeCollection();
 
 		if (categoryEntity.getTableProperties() != null)
 		{
 			String tableName = categoryEntity.getTableProperties().getName();
 
 			if (savedAttributeCollection != null)
 			{
 				for (CategoryAttributeInterface savedCategoryAttribute : savedAttributeCollection)
 				{
 					CategoryAttribute categoryAttribute = (CategoryAttribute) categoryEntity.getAttributeByIdentifier(savedCategoryAttribute.getId());
 
 					if (categoryAttribute == null && isDataPresent(tableName, savedCategoryAttribute))
 					{
 						throw new DynamicExtensionsApplicationException("data is present ,attribute can not be deleted", null, DYEXTN_A_013);
 					}
 
 					//attribute is removed or modified such that its column need to be removed
 					if (isAttributeColumnToBeRemoved(categoryAttribute, savedCategoryAttribute))
 					{
 						String columnName = savedCategoryAttribute.getColumnProperties().getName();
 
 						String removeAttributeQuery = ALTER_TABLE + WHITESPACE + tableName + WHITESPACE + DROP_KEYWORD + WHITESPACE + COLUMN_KEYWORD
 								+ WHITESPACE + columnName;
 						String type = "";
 
 						String removeAttributeQueryRollBackQuery = ALTER_TABLE + WHITESPACE + tableName + WHITESPACE + ADD_KEYWORD + WHITESPACE
 								+ getQueryPartForCategoryAttribute(savedCategoryAttribute, type, true);
 
 						attributeQueryList.add(removeAttributeQuery);
 						attributeRollbackQueryList.add(removeAttributeQueryRollBackQuery);
 					}
 				}
 			}
 		}
 	}
 
 	/**
 	 * This method returns true if a attribute is changed such that its column needs to be removed.
 	 * @param attribute attribute
 	 * @param dataBaseCopy dataBaseCopy of the  attribute
 	 * @return true if its column to be removed
 	 */
 	protected boolean isAttributeColumnToBeRemoved(AttributeInterface attribute, AttributeInterface dataBaseCopy)
 	{
 		boolean columnRemoved = false;
 
 		if (attribute == null)
 		{ /* removed now*/
 			columnRemoved = true;
 
 			if (isAttributeColumnToBeExcluded(dataBaseCopy))
 			{
 				columnRemoved = false;
 			}
 		}
 		else
 		{ /* previously not bexcluded now needs to excluded*/
 			if (!attribute.getColumnProperties().getName().equalsIgnoreCase(IDENTIFIER))
 			{
 				if (!isAttributeColumnToBeExcluded(dataBaseCopy) && isAttributeColumnToBeExcluded(attribute))
 				{
 					columnRemoved = true;
 				}
 			}
 		}
 
 		return columnRemoved;
 	}
 
 	/**
 	 * This method returns true if a attribute is changed such that its column needs to be removed.
 	 * @param attribute attribute
 	 * @param dataBaseCopy dataBaseCopy of the  attribute
 	 * @return true if its column to be removed
 	 */
 	protected boolean isAttributeColumnToBeRemoved(CategoryAttributeInterface attribute, CategoryAttributeInterface dataBaseCopy)
 	{
 		boolean columnRemoved = false;
 
 		if (attribute == null && dataBaseCopy != null)
 		{ /* removed now*/
 			columnRemoved = true;
 		}
 		return columnRemoved;
 	}
 
 	/**
 	 * @param association
 	 * @param associationDatabaseCopy
 	 * @return
 	 */
 	protected boolean isCardinalityChanged(Association association, Association associationDatabaseCopy)
 	{
 		Cardinality sourceMaxCardinality = association.getSourceRole().getMaximumCardinality();
 		Cardinality targetMaxCardinality = association.getTargetRole().getMaximumCardinality();
 
 		Cardinality sourceMaxCardinalityDatabaseCopy = associationDatabaseCopy.getSourceRole().getMaximumCardinality();
 		Cardinality targetMaxCardinalityDatabaseCopy = associationDatabaseCopy.getTargetRole().getMaximumCardinality();
 
 		if (!sourceMaxCardinality.equals(sourceMaxCardinalityDatabaseCopy) || !targetMaxCardinality.equals(targetMaxCardinalityDatabaseCopy))
 		{
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * This method processes any associations that are deleted from the entity.
 	 * @param entity
 	 * @param databaseCopy
 	 * @param associationsQueryList
 	 * @param attributeRollbackQueryList
 	 * @throws DynamicExtensionsSystemException
 	 */
 	protected void processRemovedAssociation(Entity entity, Entity databaseCopy, List associationsQueryList, List attributeRollbackQueryList)
 			throws DynamicExtensionsSystemException
 	{
 		Logger.out.debug("processRemovedAssociation Entering method");
 
 		Collection savedAssociationCollection = databaseCopy.getAssociationCollection();
 		if (entity.getTableProperties() != null)
 		{
 			String tableName = entity.getTableProperties().getName();
 
 			if (savedAssociationCollection != null && !savedAssociationCollection.isEmpty())
 			{
 				Iterator savedAssociationIterator = savedAssociationCollection.iterator();
 				while (savedAssociationIterator.hasNext())
 				{
 					Association savedAssociation = (Association) savedAssociationIterator.next();
 					Association association = (Association) entity.getAssociationByIdentifier(savedAssociation.getId());
 
 					// removed ??
 					if (association == null)
 					{
 						boolean isAddAssociationQuery = false;
 						String removeAssociationQuery = getQueryPartForAssociation(savedAssociation, attributeRollbackQueryList,
 								isAddAssociationQuery);
 						associationsQueryList.add(removeAssociationQuery);
 					}
 				}
 			}
 		}
 		Logger.out.debug("processRemovedAssociation Exiting method");
 	}
 
 	/**
 	 * This method processes any associations that are deleted from the entity.
 	 * @param entity
 	 * @param databaseCopy
 	 * @param associationsQueryList
 	 * @param attributeRollbackQueryList
 	 * @throws DynamicExtensionsSystemException
 	 */
 	protected void processRemovedAssociation(CategoryEntity categoryEntity, CategoryEntity databaseCopy, List<String> associationsQueryList,
 			List attributeRollbackQueryList) throws DynamicExtensionsSystemException
 	{
 		Logger.out.debug("processRemovedAssociation Entering method");
 
 		Collection<CategoryAssociationInterface> savedAssociationCollection = databaseCopy.getCategoryAssociationCollection();
 		if (categoryEntity.getTableProperties() != null)
 		{
 			if (savedAssociationCollection != null)
 			{
 				for (CategoryAssociationInterface savedcategoryAssociation : savedAssociationCollection)
 				{
 					CategoryAssociation association = (CategoryAssociation) categoryEntity.getAssociationByIdentifier(savedcategoryAssociation
 							.getId());
 
 					// removed ??
 					if (association == null)
 					{
 						boolean isAddAssociationQuery = false;
 						String removeAssociationQuery = getQueryPartForAssociation(savedcategoryAssociation, attributeRollbackQueryList,
 								isAddAssociationQuery);
 						associationsQueryList.add(removeAssociationQuery);
 					}
 				}
 			}
 		}
 		Logger.out.debug("processRemovedAssociation Exiting method");
 	}
 
 	/**
 	 * This method returns true if a attribute is changed such that its column needs to be added.
 	 *
 	 * @param attribute attribute
 	 * @param dataBaseCopy dataBaseCopy
 	 * @return true is column needs to be added.
 	 */
 	protected boolean isAttributeColumnToBeAdded(AttributeInterface attribute, AttributeInterface dataBaseCopy)
 	{
 		boolean columnAdd = false;
 
 		if (dataBaseCopy == null)
 		{ /*newly added*/
 			if (!isAttributeColumnToBeExcluded(attribute))
 			{
 				columnAdd = true;
 			}
 		}
 		else
 		{ /* previously excluded now need to add*/
 			if (isAttributeColumnToBeExcluded(dataBaseCopy) && !isAttributeColumnToBeExcluded(attribute))
 			{
 				columnAdd = true;
 			}
 		}
 		return columnAdd;
 	}
 
 	/**
 	 * This method returns true if a attribute is changed such that its column needs to be added.
 	 *
 	 * @param attribute attribute
 	 * @param dataBaseCopy dataBaseCopy
 	 * @return true is column needs to be added.
 	 */
 	protected boolean isAbstarctAttributeColumnToBeAdded(BaseAbstractAttributeInterface attribute, BaseAbstractAttributeInterface dataBaseCopy)
 	{
 		boolean columnAdd = false;
 
 		if (dataBaseCopy == null && attribute != null)
 		{ /*newly added*/
 			columnAdd = true;
 		}
 		return columnAdd;
 	}
 
 	/**
 	 * This method takes the edited attribtue and its database copy and then looks for any change
 	 * Changes that are tracked in terms of data table query are
 	 * Change in the constraint NOT NULL AND UNIQUE
 	 * <BR> Change in the database type of the column.
 	 * @param attribute edited Attribute
 	 * @param savedAttribute original database copy of the edited attribute.
 	 * @param attributeRollbackQueryList This list is updated with the roll back queries for the actual queries.
 	 * @return List list of strings which hold the queries for the changed attribute.
 	 * @throws DynamicExtensionsSystemException
 	 * @throws DynamicExtensionsApplicationException
 	 */
 	protected List processModifyAttribute(Attribute attribute, Attribute savedAttribute, List attributeRollbackQueryList)
 			throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException
 	{
 		List modifyAttributeQueryList = new ArrayList();
 
 		if (isAttributeColumnToBeExcluded(attribute))
 		{
 			return modifyAttributeQueryList;
 		}
 
 		String tableName = attribute.getEntity().getTableProperties().getName();
 		String columnName = attribute.getColumnProperties().getName();
 
 		String newTypeClass = attribute.getAttributeTypeInformation().getClass().getName();
 		String oldTypeClass = savedAttribute.getAttributeTypeInformation().getClass().getName();
 
 		if (!newTypeClass.equals(oldTypeClass))
 		{
 			checkIfDataTypeChangeAllowable(attribute);
 			modifyAttributeQueryList = getAttributeDataTypeChangedQuery(attribute, savedAttribute, attributeRollbackQueryList);
 			//modifyAttributeQueryList.addAll(modifyAttributeQueryList);
 		}
 
 		if (attribute.getIsPrimaryKey() && !savedAttribute.getIsPrimaryKey())
 		{
 
 			String uniqueConstraintQuery = ALTER_TABLE + WHITESPACE + tableName + WHITESPACE + ADD_KEYWORD + WHITESPACE + CONSTRAINT_KEYWORD
 					+ WHITESPACE + columnName + UNDERSCORE + UNIQUE_CONSTRAINT_SUFFIX + WHITESPACE + UNIQUE_KEYWORD + WHITESPACE + OPENING_BRACKET
 					+ columnName + CLOSING_BRACKET;
 			String uniqueConstraintRollbackQuery = ALTER_TABLE + WHITESPACE + tableName + WHITESPACE + DROP_KEYWORD + WHITESPACE + CONSTRAINT_KEYWORD
 					+ WHITESPACE + columnName + UNDERSCORE + UNIQUE_CONSTRAINT_SUFFIX;
 
 			modifyAttributeQueryList.add(uniqueConstraintQuery);
 			attributeRollbackQueryList.add(uniqueConstraintRollbackQuery);
 
 		}
 		else if (!attribute.getIsPrimaryKey() && savedAttribute.getIsPrimaryKey())
 		{
 			String uniqueConstraintQuery = ALTER_TABLE + WHITESPACE + tableName + WHITESPACE + DROP_KEYWORD + WHITESPACE + CONSTRAINT_KEYWORD
 					+ WHITESPACE + columnName + UNDERSCORE + UNIQUE_CONSTRAINT_SUFFIX;
 			String uniqueConstraintRollbackQuery = ALTER_TABLE + WHITESPACE + tableName + WHITESPACE + ADD_KEYWORD + WHITESPACE + CONSTRAINT_KEYWORD
 					+ WHITESPACE + columnName + UNDERSCORE + UNIQUE_CONSTRAINT_SUFFIX + WHITESPACE + UNIQUE_KEYWORD + WHITESPACE + OPENING_BRACKET
 					+ columnName + CLOSING_BRACKET;
 
 			modifyAttributeQueryList.add(uniqueConstraintQuery);
 			attributeRollbackQueryList.add(uniqueConstraintRollbackQuery);
 		}
 
 		return modifyAttributeQueryList;
 	}
 
 	private void checkIfDataTypeChangeAllowable(Attribute attribute) throws DynamicExtensionsApplicationException, DynamicExtensionsSystemException
 	{
 		EntityInterface entityInterface = attribute.getEntity();
 		String tableName = entityInterface.getTableProperties().getName();
 		if (isDataPresent(tableName))
 		{
 			throw new DynamicExtensionsApplicationException("Can not change the data type of the attribute", null, DYEXTN_A_009);
 		}
 	}
 
 	/**
 	 * @param tableName
 	 * @param columnName
 	 * @return
 	 * @throws DynamicExtensionsSystemException
 	 */
 	public boolean isDataPresent(String tableName, Attribute savedAttribute) throws DynamicExtensionsSystemException
 	{
 		boolean dataPresent = false;
 		StringBuffer queryBuffer = new StringBuffer();
 		if (!isAttributeColumnToBeExcluded(savedAttribute))
 		{
 			queryBuffer.append(SELECT_KEYWORD).append(WHITESPACE).append("COUNT").append(OPENING_BRACKET).append("*").append(CLOSING_BRACKET).append(
 					WHITESPACE).append(FROM_KEYWORD).append(WHITESPACE).append(tableName).append(WHITESPACE).append(WHERE_KEYWORD).append(WHITESPACE)
 					.append(savedAttribute.getColumnProperties().getName()).append(WHITESPACE).append("IS").append(WHITESPACE).append(NOT_KEYWORD)
 					.append(WHITESPACE).append(NULL_KEYWORD).append(WHITESPACE).append(AND_KEYWORD).append(WHITESPACE).append(
 							savedAttribute.getColumnProperties().getName()).append(WHITESPACE).append(NOT_KEYWORD).append(WHITESPACE).append(
 							LIKE_KEYWORD).append(WHITESPACE).append("''");
 
 			ResultSet resultSet = null;
 			try
 			{
 				resultSet = entityManagerUtil.executeQuery(queryBuffer.toString());
 				resultSet.next();
 				Long count = resultSet.getLong(1);
 				if (count > 0)
 				{
 					dataPresent = true;
 				}
 			}
 			catch (SQLException e)
 			{
 				throw new DynamicExtensionsSystemException("Can not check the availability of data", e);
 			}
 			finally
 			{
 				if (resultSet != null)
 				{
 					try
 					{
 						resultSet.close();
 					}
 					catch (SQLException e)
 					{
 						throw new DynamicExtensionsSystemException(e.getMessage(), e);
 					}
 				}
 			}
 		}
 		else
 		{
 			Collection<Integer> recordCollection = EntityManager.getInstance().getAttributeRecordsCount(savedAttribute.getEntity().getId(),
 					savedAttribute.getId());
 			if (recordCollection != null && !recordCollection.isEmpty())
 			{
 				Integer count = (Integer) recordCollection.iterator().next();
 				if (count > 0)
 				{
 					dataPresent = true;
 				}
 			}
 		}
 		return dataPresent;
 	}
 
 	/**
 	 * @param tableName
 	 * @param columnName
 	 * @return
 	 * @throws DynamicExtensionsSystemException
 	 */
 	public boolean isDataPresent(String tableName, CategoryAttributeInterface savedAttribute) throws DynamicExtensionsSystemException
 	{
 		boolean dataPresent = false;
 		StringBuffer queryBuffer = new StringBuffer();
 		if (savedAttribute != null)
 		{
 			queryBuffer.append(SELECT_KEYWORD).append(WHITESPACE).append("COUNT").append(OPENING_BRACKET).append("*").append(CLOSING_BRACKET).append(
 					WHITESPACE).append(FROM_KEYWORD).append(WHITESPACE).append(tableName).append(WHITESPACE).append(WHERE_KEYWORD).append(WHITESPACE)
 					.append(savedAttribute.getColumnProperties().getName()).append(WHITESPACE).append("IS").append(WHITESPACE).append(NOT_KEYWORD)
 					.append(WHITESPACE).append(NULL_KEYWORD).append(WHITESPACE).append(AND_KEYWORD).append(WHITESPACE).append(
 							savedAttribute.getColumnProperties().getName()).append(WHITESPACE).append(NOT_KEYWORD).append(WHITESPACE).append(
 							LIKE_KEYWORD).append(WHITESPACE).append("''");
 
 			ResultSet resultSet = null;
 			try
 			{
 				resultSet = entityManagerUtil.executeQuery(queryBuffer.toString());
 				resultSet.next();
 				Long count = resultSet.getLong(1);
 				if (count > 0)
 				{
 					dataPresent = true;
 				}
 
 			}
 			catch (SQLException e)
 			{
 				throw new DynamicExtensionsSystemException("Can not check the availability of data", e);
 			}
 			finally
 			{
 				if (resultSet != null)
 				{
 					try
 					{
 						resultSet.close();
 					}
 					catch (SQLException e)
 					{
 						throw new DynamicExtensionsSystemException(e.getMessage(), e);
 					}
 				}
 			}
 		}
 		return dataPresent;
 	}
 
 	public boolean isDataPresent(String tableName) throws DynamicExtensionsSystemException
 	{
 		StringBuffer queryBuffer = new StringBuffer();
 		queryBuffer.append(SELECT_KEYWORD).append(WHITESPACE).append("COUNT").append(OPENING_BRACKET).append("*").append(CLOSING_BRACKET).append(
 				WHITESPACE).append(FROM_KEYWORD).append(WHITESPACE).append(tableName);
 
 		ResultSet resultSet = null;
 		try
 		{
 			resultSet = entityManagerUtil.executeQuery(queryBuffer.toString());
 			resultSet.next();
 			Long count = resultSet.getLong(1);
 			if (count > 0)
 			{
 				return true;
 			}
 			else
 			{
 				return false;
 			}
 		}
 		catch (SQLException e)
 		{
 			throw new DynamicExtensionsSystemException("Can not check the availability of data", e);
 		}
 		finally
 		{
 			if (resultSet != null)
 			{
 				try
 				{
 					resultSet.close();
 				}
 				catch (SQLException e)
 				{
 					throw new DynamicExtensionsSystemException(e.getMessage(), e);
 				}
 			}
 		}
 	}
 
 	/**
 	 * This method returns the query for the attribute to modify its data type.
 	 * @param attribute
 	 * @param savedAttribute
 	 * @param modifyAttributeRollbackQuery
 	 * @param tableName
 	 * @return
 	 * @throws DynamicExtensionsSystemException
 	 */
 	protected List getAttributeDataTypeChangedQuery(Attribute attribute, Attribute savedAttribute, List modifyAttributeRollbackQueryList)
 			throws DynamicExtensionsSystemException
 	{
 		String tableName = attribute.getEntity().getTableProperties().getName();
 		String type = "";
 		String modifyAttributeRollbackQuery = "";
 
 		String modifyAttributeQuery = getQueryPartForAttribute(attribute, type, false);
 		modifyAttributeQuery = ALTER_TABLE + WHITESPACE + tableName + WHITESPACE + MODIFY_KEYWORD + WHITESPACE + modifyAttributeQuery;
 
 		modifyAttributeRollbackQuery = getQueryPartForAttribute(savedAttribute, type, false);
 		modifyAttributeRollbackQuery = ALTER_TABLE + WHITESPACE + tableName + WHITESPACE + MODIFY_KEYWORD + WHITESPACE + modifyAttributeRollbackQuery;
 
 		String nullQueryKeyword = "";
 		String nullQueryRollbackKeyword = "";
 
 		if (attribute.getIsNullable() && !savedAttribute.getIsNullable())
 		{
 			nullQueryKeyword = WHITESPACE + NULL_KEYWORD + WHITESPACE;
 			nullQueryRollbackKeyword = WHITESPACE + NOT_KEYWORD + WHITESPACE + NULL_KEYWORD + WHITESPACE;
 		}
 		else if (!attribute.getIsNullable() && savedAttribute.getIsNullable())
 		{
 			nullQueryKeyword = WHITESPACE + NOT_KEYWORD + WHITESPACE + NULL_KEYWORD + WHITESPACE;
 			nullQueryRollbackKeyword = WHITESPACE + NULL_KEYWORD + WHITESPACE;
 
 		}
 
 		modifyAttributeQuery = modifyAttributeQuery + nullQueryKeyword;
 		modifyAttributeRollbackQuery = modifyAttributeRollbackQuery + nullQueryRollbackKeyword;
 		modifyAttributeRollbackQueryList.add(modifyAttributeRollbackQuery);
 
 		List modifyAttributeQueryList = new ArrayList();
 		modifyAttributeQueryList.add(modifyAttributeQuery);
 
 		return modifyAttributeQueryList;
 	}
 
 	/**
 	 * This method builds the query part for the newly added attribute.
 	 * @param attribute Newly added attribute in the entity.
 	 * @param attributeRollbackQueryList This list is updated with the rollback queries for the actual queries.
 	 * @return Srting The actual query part for the new attribute.
 	 * @throws DynamicExtensionsSystemException
 	 * @throws DynamicExtensionsApplicationException
 	 */
 	protected String processAddAttribute(Attribute attribute, List attributeRollbackQueryList) throws DynamicExtensionsSystemException,
 			DynamicExtensionsApplicationException
 	{
 
 		String columnName = attribute.getColumnProperties().getName();
 		String tableName = attribute.getEntity().getTableProperties().getName();
 		String type = "";
 		String newAttributeQuery = ALTER_TABLE + WHITESPACE + tableName + WHITESPACE + ADD_KEYWORD + WHITESPACE
 				+ getQueryPartForAttribute(attribute, type, true);
 
 		String newAttributeRollbackQuery = ALTER_TABLE + WHITESPACE + tableName + WHITESPACE + DROP_KEYWORD + WHITESPACE + COLUMN_KEYWORD
 				+ WHITESPACE + columnName;
 
 		attributeRollbackQueryList.add(newAttributeRollbackQuery);
 
 		return newAttributeQuery;
 	}
 
 	/**
 	 * This method builds the query part for the newly added attribute.
 	 * @param attribute Newly added attribute in the entity.
 	 * @param attributeRollbackQueryList This list is updated with the rollback queries for the actual queries.
 	 * @return Srting The actual query part for the new attribute.
 	 * @throws DynamicExtensionsSystemException
 	 * @throws DynamicExtensionsApplicationException
 	 */
 	protected String processAddAttribute(CategoryAttribute attribute, List<String> attributeRollbackQueryList)
 			throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException
 	{
 
 		String columnName = attribute.getColumnProperties().getName();
 		String tableName = attribute.getCategoryEntity().getTableProperties().getName();
 		String type = "";
 		String newAttributeQuery = ALTER_TABLE + WHITESPACE + tableName + WHITESPACE + ADD_KEYWORD + WHITESPACE
 				+ getQueryPartForCategoryAttribute(attribute, type, true);
 
 		String newAttributeRollbackQuery = ALTER_TABLE + WHITESPACE + tableName + WHITESPACE + DROP_KEYWORD + WHITESPACE + COLUMN_KEYWORD
 				+ WHITESPACE + columnName;
 
 		attributeRollbackQueryList.add(newAttributeRollbackQuery);
 
 		return newAttributeQuery;
 	}
 
 	/**
 	 * This method executes the queries which generate and or manipulate the data table associated with the entity.
 	 * @param entity Entity for which the data table queries are to be executed.
 	 * @param rollbackQueryStack
 	 * @param reverseQueryList2
 	 * @param queryList2
 	 * @param hibernateDAO
 	 * @param session Hibernate Session through which connection is obtained to fire the queries.
 	 * @throws DynamicExtensionsSystemException Whenever there is any exception , this exception is thrown with proper message and the exception is
 	 * wrapped inside this exception.
 	 */
 	public Stack executeQueries(List queryList, List reverseQueryList, Stack rollbackQueryStack, HibernateDAO hibernateDAO)
 			throws DynamicExtensionsSystemException
 	{
 		Session session = null;
 		Transaction transaction = null;
 		Connection connection = null;
 
 		try
 		{
 			session = DBUtil.getCleanSession();
 			transaction = session.beginTransaction();
 		}
 		catch (BizLogicException e)
 		{
 			throw new DynamicExtensionsSystemException("Exception occured while getting the new session", e, DYEXTN_S_002);
 		}
 		catch (HibernateException e)
 		{
 			throw new DynamicExtensionsSystemException("Exception occured while getting the new trasaction", e, DYEXTN_S_002);
 		}
 
 		Iterator reverseQueryListIterator = reverseQueryList.iterator();
 
 		try
 		{
 			connection = session.connection();
 			//            System.out.print("Autocommit flag ********"+ connection.getAutoCommit());
 			if (queryList != null && !queryList.isEmpty())
 			{
 				Iterator queryListIterator = queryList.iterator();
 				while (queryListIterator.hasNext())
 				{
 					String query = (String) queryListIterator.next();
 					System.out.println("Query: " + query);
 
 					PreparedStatement statement = null;
 					try
 					{
 						statement = connection.prepareStatement(query);
 					}
 					catch (SQLException e)
 					{
 						throw new DynamicExtensionsSystemException("Exception occured while executing the data table query", e);
 					}
 					try
 					{
 						statement.executeUpdate();
 						if (reverseQueryListIterator.hasNext())
 						{
 							rollbackQueryStack.push(reverseQueryListIterator.next());
 						}
 					}
 					catch (SQLException e)
 					{
 						throw new DynamicExtensionsSystemException("Exception occured while forming the data tables for entity", e, DYEXTN_S_002);
 					}
 				}
 			}
 		}
 		catch (HibernateException e)
 		{
 			throw new DynamicExtensionsSystemException("Cannot obtain connection to execute the data query", e, DYEXTN_S_001);
 		}
 		finally
 		{
 			try
 			{
 				transaction.commit();
 				session.close();
 			}
 			catch (HibernateException e)
 			{
 				throw new DynamicExtensionsSystemException("Exception occured while commiting trasaction", e, DYEXTN_S_002);
 			}
 		}
 		return rollbackQueryStack;
 	}
 
 	public Object[] executeDMLQuery(String query) throws DynamicExtensionsSystemException
 	{
 		Session session = null;
 
 		try
 		{
 			session = DBUtil.currentSession();
 		}
 		catch (HibernateException e1)
 		{
 			throw new DynamicExtensionsSystemException("Unable to exectute the queries .....Cannot access connection from sesesion", e1, DYEXTN_S_002);
 		}
 
 		try
 		{
 			Connection conn = session.connection();
 
 			System.out.println("Query: " + query);
 			Statement statement = null;
 			try
 			{
 				statement = conn.createStatement();
 				ResultSet rs = statement.executeQuery(query);
 				System.out.println(rs.getMetaData());
 				//Object[]obj = new Object[rs.getMetaData().getColumnCount()];
 				List list = new ArrayList();
 				int i = 1;
 				while (rs.next())
 				{
 					list.add(rs.getObject(i));
 
 				}
 				System.out.println(list);
 				return list.toArray();
 			}
 			catch (SQLException e)
 			{
 				throw new DynamicExtensionsSystemException("Exception occured while forming the data tables for entity", e, DYEXTN_S_002);
 			}
 		}
 		catch (HibernateException e)
 		{
 			throw new DynamicExtensionsSystemException("Cannot obtain connection to execute the data query", e, DYEXTN_S_001);
 		}
 	}
 
 	/**
 	 * This method excute the query that selects record ids of the target entity that are associated
 	 * to the source entity for a given association.
 	 * @param query
 	 * @return List of reocrd ids of the target entity .
 	 * @throws DynamicExtensionsSystemException
 	 */
 	protected List<Long> getAssociationRecordValues(String query) throws DynamicExtensionsSystemException
 	{
 		List<Long> associationRecordValues = new ArrayList();
 
 		Statement statement = null;
 		ResultSet resultSet = null;
 		try
 		{
 			Connection conn = DBUtil.getConnection();
 			statement = conn.createStatement();
 			resultSet = statement.executeQuery(query);
 
 			while (resultSet.next())
 			{
 				Long recordId = resultSet.getLong(1);
 				associationRecordValues.add(recordId);
 			}
 		}
 		catch (Exception e)
 		{
 			throw new DynamicExtensionsSystemException("Exception in query execution", e);
 		}
 		finally
 		{
 			try
 			{
 				resultSet.close();
 				statement.close();
 			}
 			catch (SQLException e)
 			{
 				throw new DynamicExtensionsSystemException("can not close result set", e);
 			}
 		}
 
 		return associationRecordValues;
 	}
 
 	/**
 	 * This method make sure the cardinality constaints are properly
 	 * followed.
 	 * e.g
 	 * 1. For One to One association,it checks if target entity's record id is not associated to any other
 	 * source entity.
 	 *
 	 * @param association for which cardinality to be tested.
 	 * @param recordIdList recordIdList (for one to one, it will contain only one entry).
 	 *
 	 * @throws DynamicExtensionsApplicationException
 	 * @throws DynamicExtensionsSystemException
 	 */
 	protected void verifyCardinalityConstraints(AssociationInterface association, Long sourceRecordId) throws DynamicExtensionsApplicationException,
 			DynamicExtensionsSystemException
 	{
 		EntityInterface targetEntity = association.getTargetEntity();
 		Cardinality sourceMaxCardinality = association.getSourceRole().getMaximumCardinality();
 		Cardinality targetMaxCardinality = association.getTargetRole().getMaximumCardinality();
 
 		String columnName = "";
 		String tableName = "";
 
 		if (targetMaxCardinality == Cardinality.ONE && sourceMaxCardinality == Cardinality.ONE)
 		{
 
 			tableName = targetEntity.getTableProperties().getName();
 			columnName = association.getConstraintProperties().getTargetEntityKey();
 
 			String query = SELECT_KEYWORD + WHITESPACE + COUNT_KEYWORD + OPENING_BRACKET + "*" + CLOSING_BRACKET + WHITESPACE + FROM_KEYWORD
 					+ WHITESPACE + tableName + WHITESPACE + WHERE_KEYWORD + WHITESPACE + columnName + WHITESPACE + EQUAL + WHITESPACE
 					+ sourceRecordId;
 			ResultSet resultSet = null;
 			try
 			{
 				resultSet = entityManagerUtil.executeQuery(query);
 				resultSet.next();
 				// if another source record is already using target record , throw exception.
 				if (resultSet.getInt(1) != 0)
 				{
 					throw new DynamicExtensionsApplicationException("Cardinality constraint violated", null, DYEXTN_A_005);
 				}
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
 						resultSet.close();
 					}
 					catch (SQLException e)
 					{
 						throw new DynamicExtensionsSystemException(e.getMessage(), e);
 					}
 				}
 			}
 		}
 	}
 
 	/**
 	 * This method make sure the cardinality constaints are properly
 	 * followed.
 	 * e.g
 	 * 1. For One to One association,it checks if target entity's record id is not associated to any other
 	 * source entity.
 	 *
 	 * @param association for which cardinality to be tested.
 	 * @param recordIdList recordIdList (for one to one, it will contain only one entry).
 	 *
 	 * @throws DynamicExtensionsApplicationException
 	 * @throws DynamicExtensionsSystemException
 	 */
 	protected void verifyCardinalityConstraints(CategoryAssociationInterface categoryAssociationInterface, Long sourceRecordId)
 			throws DynamicExtensionsApplicationException, DynamicExtensionsSystemException
 	{
 		CategoryEntityInterface categoryEntityInterface = categoryAssociationInterface.getTargetCategoryEntity();
 		String columnName = "";
 		String tableName = "";
 
 		if (categoryEntityInterface.getNumberOfEntries() == 1)
 		{
 
 			tableName = categoryEntityInterface.getTableProperties().getName();
 			columnName = categoryAssociationInterface.getConstraintProperties().getTargetEntityKey();
 
 			String query = SELECT_KEYWORD + WHITESPACE + COUNT_KEYWORD + OPENING_BRACKET + "*" + CLOSING_BRACKET + WHITESPACE + FROM_KEYWORD
 					+ WHITESPACE + tableName + WHITESPACE + WHERE_KEYWORD + WHITESPACE + columnName + WHITESPACE + EQUAL + WHITESPACE
 					+ sourceRecordId;
 			ResultSet resultSet = null;
 			try
 			{
 				entityManagerUtil.executeQuery(query);
 				resultSet.next();
 				// if another source record is already using target record , throw exception.
 				if (resultSet.getInt(1) != 0)
 				{
 					throw new DynamicExtensionsApplicationException("Cardinality constraint violated", null, DYEXTN_A_005);
 				}
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
 						resultSet.close();
 					}
 					catch (SQLException e)
 					{
 						throw new DynamicExtensionsSystemException(e.getMessage(), e);
 					}
 				}
 			}
 		}
 	}
 
 	/**
 	 * @param inputList
 	 * @return
 	 */
 	private String getListToString(List inputList)
 	{
 
 		String queryString = inputList.toString();
 		queryString = queryString.replace("[", OPENING_BRACKET);
 		queryString = queryString.replace("]", CLOSING_BRACKET);
 
 		return queryString;
 	}
 
 	/**
 	 * @param entity
 	 * @param databaseCopy
 	 * @return
 	 */
 	public boolean isParentChanged(Entity entity, Entity databaseCopy)
 	{
 		boolean isParentChanged = false;
 		if (entity.getParentEntity() != null && !entity.getParentEntity().equals(databaseCopy.getParentEntity()))
 		{
 			isParentChanged = true;
 		}
 		else if (entity.getParentEntity() == null && databaseCopy.getParentEntity() != null)
 		{
 			isParentChanged = true;
 		}
 		return isParentChanged;
 
 	}
 
 	public boolean isParentChanged(Entity entity, Long databaseParentId)
 	{
 		boolean isParentChanged = false;
 		if (entity.getParentEntity() != null && !entity.getParentEntity().getId().equals(databaseParentId))
 		{
 			isParentChanged = true;
 		}
 		else if (entity.getParentEntity() == null && databaseParentId != null)
 		{
 			isParentChanged = true;
 		}
 		return isParentChanged;
 
 	}
 
 	/**
 	 * @param entity
 	 * @param databaseCopy
 	 * @return
 	 */
 	public boolean isParentChanged(CategoryEntity categoryEntity, CategoryEntity databaseCopy)
 	{
 		boolean isParentChanged = false;
 		if (categoryEntity.getParentCategoryEntity() != null
 				&& !categoryEntity.getParentCategoryEntity().equals(databaseCopy.getParentCategoryEntity()))
 		{
 			isParentChanged = true;
 		}
 		else if (categoryEntity.getParentCategoryEntity() == null && databaseCopy.getParentCategoryEntity() != null)
 		{
 			isParentChanged = true;
 		}
 		return isParentChanged;
 
 	}
 
 	/**
 	 *
 	 * @param attribute
 	 * @param value
 	 * @return
 	 */
 	public String getFormattedValue(AbstractAttribute attribute, Object value)
 	{
 		String formattedvalue = null;
 		AttributeTypeInformationInterface attributeInformation = ((Attribute) attribute).getAttributeTypeInformation();
 		if (attribute == null)
 		{
 			formattedvalue = null;
 		}
 
 		else if (attributeInformation instanceof StringAttributeTypeInformation)
 		{
 			// quick fix.
 			if (value instanceof List)
 			{
 				if (((List) value).size() > 0)
 				{
 					formattedvalue = "'" + getEscapedStringValue((String) ((List) value).get(0)) + "'";
 				}
 			}
 			else
 				formattedvalue = "'" + getEscapedStringValue((String) value) + "'";
 		}
 		else if (attributeInformation instanceof DateAttributeTypeInformation)
 		{
 			String dateFormat = ((DateAttributeTypeInformation) attributeInformation).getFormat();
 			if (dateFormat == null)
 			{
 				dateFormat = Constants.DATE_PATTERN_MM_DD_YYYY;
 			}
 
 			String str = null;
 			if (value instanceof Date)
 			{
 				str = Utility.parseDateToString(((Date) value), dateFormat);
 			}
 			else
 			{
 				str = (String) value;
 			}
 
 			if (dateFormat.equals(ProcessorConstants.MONTH_YEAR_FORMAT))
 			{
 				if (str.length() != 0)
 				{
 					str = DynamicExtensionsUtility.formatMonthAndYearDate(str);
 					if (Variables.databaseName.equals(Constants.ORACLE_DATABASE))
 					{
 						str = str.substring(0, str.length() - 4);
 					}
 				}
 			}
 
 			if (dateFormat.equals(ProcessorConstants.YEAR_ONLY_FORMAT))
 			{
 				if (str.length() != 0)
 				{
 					str = DynamicExtensionsUtility.formatYearDate(str);
 					if (Variables.databaseName.equals(Constants.ORACLE_DATABASE))
 					{
 						str = str.substring(0, str.length() - 4);
 					}
 				}
 			}
 			//for mysql5 if user not enter any value for date field its getting saved as 00-00-0000 ,which is throwing exception
 			//So to avoid it store null value in database
 			if (Variables.databaseName.equals(Constants.MYSQL_DATABASE) && str.trim().length() == 0)
 			{
 				formattedvalue = null;
 
 			}
 			else
 			{
 				formattedvalue = Variables.strTodateFunction + "('" + str.trim() + "','" + DynamicExtensionsUtility.getSQLDateFormat(dateFormat)
 						+ "')";
 			}
 		}
 		else
 		{
 			// quick fix.
 			if (value instanceof List)
 			{
 				if (((List) value).size() > 0)
 				{
 					formattedvalue = ((List) value).get(0).toString();
 				}
 			}
 			else
 				formattedvalue = value.toString();
 
 			//In case of Mysql 5 ,if the column datatype double ,float ,integer then its not possible to pass '' as  in insert-update query
 			//so instead pass null as value.
 			if (Variables.databaseName.equals(Constants.MYSQL_DATABASE) && attributeInformation instanceof DoubleAttributeTypeInformation)
 			{
 				if (formattedvalue.trim().length() == 0)
 				{
 					formattedvalue = null;
 				}
 			}
 			else if (Variables.databaseName.equals(Constants.MYSQL_DATABASE) && attributeInformation instanceof IntegerAttributeTypeInformation)
 			{
 
 				if (formattedvalue.trim().length() == 0)
 				{
 					formattedvalue = null;
 				}
 			}
 			else if (Variables.databaseName.equals(Constants.MYSQL_DATABASE) && attributeInformation instanceof FloatAttributeTypeInformation)
 			{
 
 				if (formattedvalue.trim().length() == 0)
 				{
 					formattedvalue = null;
 				}
 			}
 			else if (Variables.databaseName.equals(Constants.MYSQL_DATABASE) && attributeInformation instanceof ShortAttributeTypeInformation)
 			{
 
 				if (formattedvalue.trim().length() == 0)
 				{
 					formattedvalue = null;
 				}
 			}
 			else if (Variables.databaseName.equals(Constants.MYSQL_DATABASE) && attributeInformation instanceof LongAttributeTypeInformation)
 			{
 
 				if (formattedvalue.trim().length() == 0)
 				{
 					formattedvalue = null;
 				}
 			}
 			else if (Variables.databaseName.equals(Constants.MYSQL_DATABASE) && attributeInformation instanceof BooleanAttributeTypeInformation)
 			{
 				if (formattedvalue.equals("false"))
 					formattedvalue = "0";
 				else
 					formattedvalue = "1";
 			}
 
 			if (formattedvalue != null)
 			{
 				formattedvalue = "'" + formattedvalue + "'";
 			}
 		}
 		Logger.out.debug("getFormattedValue The formatted value for attribute " + attribute.getName() + "is " + formattedvalue);
 		return formattedvalue;
 	}
 
 	/**
	 * Replace any single and double quotes value with proper escape character	
 	 * @param value
 	 * @return
 	 */
 	protected String getEscapedStringValue(String value)
 	{

		value = DynamicExtensionsUtility.replaceUtil(value, "'", "\\\'");
		value = DynamicExtensionsUtility.replaceUtil(value, "\"", "\\\"");
 		return value.trim();
 	}
 
 	/**
 	 * @return
 	 */
 	public boolean isValuePresent(AttributeInterface attribute, Object value) throws DynamicExtensionsSystemException
 	{
 		boolean present = false;
 
 		String tableName = attribute.getEntity().getTableProperties().getName();
 		String columnName = attribute.getColumnProperties().getName();
 		Object formattedValue = getFormattedValue((AbstractAttribute) attribute, value);
 
 		StringBuffer queryBuffer = new StringBuffer();
 		queryBuffer.append(SELECT_KEYWORD).append(WHITESPACE).append(COUNT_KEYWORD).append(OPENING_BRACKET).append("*").append(CLOSING_BRACKET)
 				.append(WHITESPACE).append(FROM_KEYWORD).append(WHITESPACE).append(tableName).append(WHITESPACE).append(WHERE_KEYWORD).append(
 						WHITESPACE).append(columnName).append(EQUAL).append(formattedValue).append(" and " + getRemoveDisbledRecordsQuery(""));
 
 		ResultSet resultSet = null;
 		try
 		{
 			resultSet = EntityManagerUtil.executeQuery(queryBuffer.toString());
 			resultSet.next();
 			Long count = resultSet.getLong(1);
 			if (count > 0)
 			{
 				present = true;
 			}
 		}
 		catch (SQLException e)
 		{
 			throw new DynamicExtensionsSystemException("Can not check the availability of value", e);
 		}
 		finally
 		{
 			if (resultSet != null)
 			{
 				try
 				{
 					resultSet.close();
 				}
 				catch (SQLException e)
 				{
 					throw new DynamicExtensionsSystemException(e.getMessage(), e);
 				}
 			}
 		}
 		return present;
 	}
 
 	/**
 	 * @return
 	 */
 	public static String getRemoveDisbledRecordsQuery(String tableName)
 	{
 		String prefix = "";
 		if (tableName != null && !tableName.equals(""))
 		{
 			prefix = tableName + ".";
 		}
 		return " " + prefix + Constants.ACTIVITY_STATUS_COLUMN + " <> '" + Constants.ACTIVITY_STATUS_DISABLED + "' ";
 	}
 
 	/**
 	 * @param association
 	 * @param sourceEntityRecordId
 	 * @param targetEntityRecordId
 	 * @throws DynamicExtensionsSystemException
 	 */
 	public static void associateRecords(AssociationInterface association, Long sourceEntityRecordId, Long targetEntityRecordId)
 			throws DynamicExtensionsSystemException
 	{
 		EntityInterface sourceEntity = association.getEntity();
 		EntityInterface targetEntity = association.getTargetEntity();
 		RoleInterface sourceRole = association.getSourceRole();
 		RoleInterface targetRole = association.getTargetRole();
 		Cardinality sourceMaxCardinality = sourceRole.getMaximumCardinality();
 		Cardinality targetMaxCardinality = targetRole.getMaximumCardinality();
 		ConstraintPropertiesInterface constraint = association.getConstraintProperties();
 		String tableName = DynamicExtensionsUtility.getTableName(association);
 		StringBuffer query = new StringBuffer();
 		query.append(UPDATE_KEYWORD).append(tableName).append(SET_KEYWORD);
 		StringBuffer partialQuery = new StringBuffer();
 		if (sourceMaxCardinality == Cardinality.MANY && targetMaxCardinality == Cardinality.MANY)
 		{
 			query = new StringBuffer();
 			query.append(INSERT_INTO_KEYWORD).append(tableName).append(OPENING_BRACKET).append(constraint.getSourceEntityKey()).append(COMMA).append(
 					constraint.getTargetEntityKey()).append(CLOSING_BRACKET).append("values").append(OPENING_BRACKET).append(sourceEntityRecordId)
 					.append(COMMA).append(targetEntityRecordId).append(CLOSING_BRACKET);
 		}
 		else if (sourceMaxCardinality == Cardinality.MANY && targetMaxCardinality == Cardinality.ONE)
 		{
 			query.append(constraint.getSourceEntityKey()).append(EQUAL).append(targetEntityRecordId).append(WHERE_KEYWORD).append(IDENTIFIER).append(
 					EQUAL).append(sourceEntityRecordId);
 		}
 		else
 		{
 			query.append(constraint.getTargetEntityKey()).append(EQUAL).append(sourceEntityRecordId).append(WHERE_KEYWORD).append(IDENTIFIER).append(
 					EQUAL).append(targetEntityRecordId);
 		}
 		Connection conn = null;
 		try
 		{
 			conn = DBUtil.getConnection();
 			Statement stmt = conn.createStatement();
 			stmt.execute(query.toString());
 			conn.commit();
 		}
 		catch (HibernateException e)
 		{
 			connectionRollBack(conn);
 		}
 		catch (SQLException e)
 		{
 			connectionRollBack(conn);
 		}
 		finally
 		{
 			DBUtil.closeConnection();
 		}
 	}
 
 	/**
 
 	 * This method rollbacks the conncetion
 
 	 * @param connection
 
 	 * @throws DynamicExtensionsSystemException
 
 	 */
 
 	private static void connectionRollBack(Connection connection) throws DynamicExtensionsSystemException
 	{
 		try
 		{
 			connection.rollback();
 		}
 		catch (SQLException e)
 		{
 			e.printStackTrace();
 		}
 		throw new DynamicExtensionsSystemException("Can not execute query");
 	}
 
 }
