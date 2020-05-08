 /**
  *<p>Title: </p>
  *<p>Description:  </p>
  *<p>Copyright://TODO</p>
  *@author Vishvesh Mulay
  *@author Rahul Ner
  *@version 1.0
  */
 
 package edu.common.dynamicextensions.entitymanager;
 
 import java.sql.ResultSet;
 import java.sql.ResultSetMetaData;
 import java.sql.SQLException;
 import java.sql.Types;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import edu.common.dynamicextensions.domain.Attribute;
 import edu.common.dynamicextensions.domain.AttributeTypeInformation;
 import edu.common.dynamicextensions.domain.DateAttributeTypeInformation;
 import edu.common.dynamicextensions.domain.DomainObjectFactory;
 import edu.common.dynamicextensions.domain.Entity;
 import edu.common.dynamicextensions.domain.EntityGroup;
 import edu.common.dynamicextensions.domain.FileAttributeRecordValue;
 import edu.common.dynamicextensions.domain.FileAttributeTypeInformation;
 import edu.common.dynamicextensions.domain.FileExtension;
 import edu.common.dynamicextensions.domain.StringAttributeTypeInformation;
 import edu.common.dynamicextensions.domain.databaseproperties.TableProperties;
 import edu.common.dynamicextensions.domain.userinterface.Container;
 import edu.common.dynamicextensions.domaininterface.AssociationInterface;
 import edu.common.dynamicextensions.domaininterface.AttributeInterface;
 import edu.common.dynamicextensions.domaininterface.AttributeTypeInformationInterface;
 import edu.common.dynamicextensions.domaininterface.EntityGroupInterface;
 import edu.common.dynamicextensions.domaininterface.EntityInterface;
 import edu.common.dynamicextensions.domaininterface.SemanticPropertyInterface;
 import edu.common.dynamicextensions.domaininterface.StringValueInterface;
 import edu.common.dynamicextensions.domaininterface.TaggedValueInterface;
 import edu.common.dynamicextensions.domaininterface.userinterface.ContainerInterface;
 import edu.common.dynamicextensions.exception.DynamicExtensionsApplicationException;
 import edu.common.dynamicextensions.exception.DynamicExtensionsSystemException;
 import edu.common.dynamicextensions.processor.ProcessorConstants;
 import edu.common.dynamicextensions.util.DynamicExtensionsBaseTestCase;
 import edu.common.dynamicextensions.util.DynamicExtensionsUtility;
 import edu.common.dynamicextensions.util.global.Constants;
 import edu.common.dynamicextensions.util.global.Variables;
 import edu.common.dynamicextensions.util.global.Constants.AssociationDirection;
 import edu.common.dynamicextensions.util.global.Constants.AssociationType;
 import edu.common.dynamicextensions.util.global.Constants.Cardinality;
 import edu.wustl.common.beans.NameValueBean;
 import edu.wustl.common.util.dbManager.DBUtil;
 import edu.wustl.common.util.logger.Logger;
 
 public class TestEntityManager extends DynamicExtensionsBaseTestCase
 {
 
 	/**
 	 * 
 	 */
 	public TestEntityManager()
 	{
 		super();
 		//TODO Auto-generated constructor stub
 	}
 
 	/**
 	 * @param arg0 name
 	 */
 	public TestEntityManager(String arg0)
 	{
 		super(arg0);
 		//TODO Auto-generated constructor stub
 	}
 
 	/**
 	 * @see edu.common.dynamicextensions.util.DynamicExtensionsBaseTestCase#setUp()
 	 */
 	protected void setUp()
 	{
 		super.setUp();
 	}
 
 	/**
 	 * @see edu.common.dynamicextensions.util.DynamicExtensionsBaseTestCase#tearDown()
 	 */
 	protected void tearDown()
 	{
 		super.tearDown();
 	}
 
 	/**
 	 * PURPOSE : To test the whether editing of an existing entity works properly or not.
 	 * EXPECTED BEHAVIOR : Changes in the existing entity should be stored properly and the changes made to the 
 	 * attributes of the entity should get reflected properly in the data tables.
 	 * TEST CASE FLOW : 
 	 * 1. Create entity with some attributes
 	 * 2. Save entity using entity manager.
 	 * 3. Add an extra attribute to the entity
 	 * 4. Save the entity again.
 	 * 5. Check whether the added attribute is retrieved back properly from the database.
 	 * 6. Check whether a column is newly added or not  to the data table of the entity.
 	 */
 	public void testEditEntity()
 	{
 		//Step 1 
 		Entity entity = new Entity();
 		entity.setName("Stock Quote");
 		EntityManagerInterface entityManagerInterface = EntityManager.getInstance();
 
 		try
 		{
 			//Step 2
 			EntityInterface savedEntity = entityManagerInterface.persistEntity(entity);
 
 			//Step 3 Edit entity-- Add extra attribute
 			AttributeInterface floatAtribute = DomainObjectFactory.getInstance()
 					.createFloatAttribute();
 			floatAtribute.setName("Price");
 
 			savedEntity.addAbstractAttribute(floatAtribute);
 			//Step 4
 			EntityInterface editedEntity = entityManagerInterface.persistEntity(savedEntity);
 
 			Map dataValue = new HashMap();
 
 			//			dataValue.put(floatAtribute, "15.90");
 			//			entityManagerInterface.insertData(editedEntity, dataValue);
 			//			
 			//			dataValue.put(floatAtribute, "16.90");
 			//			entityManagerInterface.insertData(editedEntity, dataValue);
 			//			
 			//			Long id = new EntityManagerUtil().getNextIdentifier(editedEntity.getTableProperties().getName());
 			//			System.out.println(id);
 
 			//Edit entity
 			AttributeInterface floatAtribute1 = DomainObjectFactory.getInstance()
 					.createFloatAttribute();
 			floatAtribute.setName("NewPrice");
 			editedEntity.addAbstractAttribute(floatAtribute1);
 
 			java.sql.ResultSetMetaData metadata = executeQueryForMetadata("select * from "
 					+ editedEntity.getTableProperties().getName());
 			assertEquals(noOfDefaultColumns + 1, metadata.getColumnCount());
 
 			ResultSet resultSet = executeQuery("select count(*) from "
 					+ editedEntity.getTableProperties().getName());
 			resultSet.next();
 			assertEquals(0, resultSet.getInt(1));
 
 			//Step 5
 			EntityInterface newEditedEntity = entityManagerInterface.persistEntity(editedEntity);
 			dataValue.put(floatAtribute1, "21");
 			EntityManager.getInstance().insertData(newEditedEntity, dataValue);
 			//Step 6
 			metadata = executeQueryForMetadata("select * from "
 					+ editedEntity.getTableProperties().getName());
 			assertEquals(noOfDefaultColumns + 2, metadata.getColumnCount());
 
 			resultSet = executeQuery("select count(*) from "
 					+ editedEntity.getTableProperties().getName());
 			resultSet.next();
 			assertEquals(1, resultSet.getInt(1));
 
 			EntityManager.getInstance().insertData(newEditedEntity, dataValue);
 			resultSet = executeQuery("select count(*) from "
 					+ editedEntity.getTableProperties().getName());
 			resultSet.next();
 			assertEquals(2, resultSet.getInt(1));
 
 		}
 		catch (DynamicExtensionsSystemException e)
 		{
 
 			e.printStackTrace();
 			fail();
 		}
 		catch (DynamicExtensionsApplicationException e)
 		{
 			e.printStackTrace();
 			fail();
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 			fail();
 		}
 
 	}
 
 	/**
 	 * PURPOSE : To test the whether editing of an existing entity (removing an attribute) works properly or not.
 	 * EXPECTED BEHAVIOR : Changes in the existing entity should be stored properly and the changes made to the 
 	 * attributes of the entity should get reflected properly in the data tables.
 	 * TEST CASE FLOW : 
 	 * 1. Create entity with some attributes
 	 * 2. Save entity using entity manager.
 	 * 3. Remove an attribute from the entity
 	 * 4. Save the entity again.
 	 * 5. Check whether the removed attribute is not present in the entity
 	 * 6. Check whether a column is removed or not  from the data table of the entity.
 	 */
 	public void testEditEntityRemoveAttribute()
 	{
 		//Step 1 
 		Entity entity = new Entity();
 		entity.setName("Stock Quote");
 		EntityManagerInterface entityManagerInterface = EntityManager.getInstance();
 
 		try
 		{
 
 			AttributeInterface floatAtribute = DomainObjectFactory.getInstance()
 					.createFloatAttribute();
 			floatAtribute.setName("Price");
 			AttributeInterface commentsAttributes = DomainObjectFactory.getInstance()
 					.createStringAttribute();
 			commentsAttributes.setName("coomments");
 
 			entity.addAbstractAttribute(floatAtribute);
 			entity.addAbstractAttribute(commentsAttributes);
 			//Step 2 
 			EntityInterface savedEntity = entityManagerInterface.persistEntity(entity);
 
 //			Map dataValue = new HashMap();
 //			dataValue.put(floatAtribute, "15.90");
 //			entityManagerInterface.insertData(savedEntity, dataValue);
 
 			java.sql.ResultSetMetaData metadata = executeQueryForMetadata("select * from "
 					+ savedEntity.getTableProperties().getName());
 			assertEquals(metadata.getColumnCount(), noOfDefaultColumns + 2);
 
 			AttributeInterface savedFloatAtribute = null;
 			//Collection collection = savedEntity.getAttributeCollection();
 			//collection =  EntityManagerUtil.filterSystemAttributes(collection);
 			//Iterator itr = collection.iterator();
 			
 			
 				savedFloatAtribute = savedEntity.getAttributeByIdentifier(floatAtribute.getId());
 				System.out.println("id is: " + savedFloatAtribute.getId());
 
 
 			//remove attribute
 			//Step 3
 			savedEntity.removeAbstractAttribute(savedFloatAtribute);
 			//Step 4
 			EntityInterface editedEntity = entityManagerInterface.persistEntity(savedEntity);
 			//Step 6
 			metadata = executeQueryForMetadata("select * from "
 					+ editedEntity.getTableProperties().getName());
 			assertEquals(noOfDefaultColumns + 1, metadata.getColumnCount());
 		}
 		catch (DynamicExtensionsSystemException e)
 		{
 			fail();
 			Logger.out.debug(e.getStackTrace());
 		}
 		catch (DynamicExtensionsApplicationException e)
 		{
 			e.printStackTrace();
 			Logger.out.debug(e.getStackTrace());
 			fail();
 			
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 			fail();
 
 			Logger.out.debug(e.getStackTrace());
 		}
 
 	}
 
 	/**
 	 * PURPOSE : To test the behavior of entity manager when an entity with null attribute is tried for saving.
 	 * EXPECTED BEHAVIOR : Entity manager should throw an application exception with proper error code.
 	 * TEST CASE FLOW : 
 	 * 1. Create entity with null attribute
 	 * 2. Save entity using entity manager.
 	 * 3. Check whether the application exception is thrown or not.
 	 */
 	public void testCreateEnityWithNullAttribute()
 	{
 		try
 		{
 			//Step 1
 			Entity entity = new Entity();
 			entity.addAbstractAttribute(null);
 
 			EntityManagerInterface entityManagerInterface = EntityManager.getInstance();
 			//Step 2
 			EntityInterface savedEntity = entityManagerInterface.persistEntity(entity);
 			fail();
 		}
 		catch (DynamicExtensionsApplicationException e)
 		{
 			//Step 3
 			Logger.out
 					.debug("Exception should occur coz the attribute is null.. validation should fail");
 		}
 		catch (DynamicExtensionsSystemException e)
 		{
 			fail();
 			Logger.out.debug(e.getStackTrace());
 		}
 
 	}
 
 	/**
 	 * PURPOSE : To test whether the created date is set on saving of entity or not.
 	 * EXPECTED BEHAVIOR : Before saving the created date is null. After the entity is saved created date should be set.
 	 * TEST CASE FLOW : 
 	 * 1. Create entity with null created date
 	 * 2. Save entity using entity manager.
 	 * 3. Check whether the created date is set or not.
 	 */
 	public void testCreateEntityNull()
 	{
 		//Step 1  
 		EntityInterface entity = new Entity();
 		entity.setName("test");
 		EntityManagerInterface entityManagerInterface = EntityManager.getInstance();
 		entity.setCreatedDate(null);
 
 		try
 		{
 			//Step 2
 			EntityInterface savedEntity = entityManagerInterface.persistEntity(entity);
 			assertEquals(savedEntity.getName(), entity.getName());
 
 			String tableName = entity.getTableProperties().getName();
 			String query = "Select * from " + tableName;
 			executeQueryForMetadata(query);
 
 			//Step 3
 			assertNotNull(savedEntity.getCreatedDate());
 		}
 		catch (DynamicExtensionsSystemException e)
 		{
 			fail();
 			Logger.out.debug(e.getStackTrace());
 		}
 		catch (DynamicExtensionsApplicationException e)
 		{
 			fail();
 			Logger.out.debug(e.getStackTrace());
 		}
 
 	}
 
 	/**
 	 * PURPOSE : To test the behavior when attribute type is changed while editing the entity.
 	 * EXPECTED BEHAVIOR : Change in the attribute type should be stored properly. Also the changes in the data type
 	 * of the column due to the change in attribute type is reflected properly in the data table or not.
 	 * 1. Create entity with integer attribute.
 	 * 2. Save entity using entity manager.
 	 * 3. Check whether the associated column type is numeric or not.
 	 * 4. Change the attribute type to string.
 	 * 5. Save the entity again.
 	 * 6. Check whether the associated column type is changed or not.
 	 */
 	public void testEditAttributeTypeChange()
 	{
 		Entity entity;
 		try
 		{
 			//Step 1
 			entity = (Entity) new Entity();
 			AttributeInterface ssn = DomainObjectFactory.getInstance().createIntegerAttribute();
 			ssn.setName("SSN of participant");
 			entity.addAbstractAttribute(ssn);
 			entity.setName("test");
 			//Step 2
 			entity = (Entity) EntityManager.getInstance().persistEntity(entity);
 
 			ResultSetMetaData metaData = executeQueryForMetadata("select * from "
 					+ entity.getTableProperties().getName());
 			//Step 3
 			if (Variables.databaseName
 					.equals(edu.common.dynamicextensions.util.global.Constants.MYSQL_DATABASE))
 			{
 				assertEquals(metaData.getColumnType(2), Types.INTEGER);
 			}
 			else
 			{
 				assertEquals(metaData.getColumnType(2), Types.NUMERIC);
 			}
 
 			//Step 4
 			AttributeTypeInformationInterface stringAttributeType = new StringAttributeTypeInformation();
 			ssn.setAttributeTypeInformation(stringAttributeType);
 			//Step 5
 			entity = (Entity) EntityManager.getInstance().persistEntity(entity);
 
 			metaData = executeQueryForMetadata("select * from "
 					+ entity.getTableProperties().getName());
 			//Step 6
 			if (Variables.databaseName
 					.equals(edu.common.dynamicextensions.util.global.Constants.MYSQL_DATABASE))
 			{
 				assertEquals(metaData.getColumnTypeName(2), "TEXT");
 			}
 			else
 			{
 				assertEquals(metaData.getColumnType(2), Types.VARCHAR);
 
 			}
 
 		}
 		catch (DynamicExtensionsApplicationException e)
 		{
 			// TODO Auto-generated catch block
 			fail();
 			e.printStackTrace();
 		}
 		catch (DynamicExtensionsSystemException e)
 		{
 			// TODO Auto-generated catch block
 			fail();
 			e.printStackTrace();
 		}
 		catch (SQLException e)
 		{
 			// TODO Auto-generated catch block
 			fail();
 			e.printStackTrace();
 		}
 
 	}
 
 	/**
 	 * PURPOSE : To check the behavior when user tries modify data type of the attribute,
 	 * when data is present for that column.
 	 * EXPECTED BEHAVIOR : for oracle it should throw exception.for mysql  it works.  
 	 */
 	public void testEditAttributeTypeChangeDataExists()
 	{
 		Entity entity = null;
 		try
 		{
 			entity = (Entity) new Entity();
 			AttributeInterface ssn = DomainObjectFactory.getInstance().createIntegerAttribute();
 			ssn.setName("SSN of participant");
 			entity.addAbstractAttribute(ssn);
 			entity.setName("test");
 			entity = (Entity) EntityManager.getInstance().persistEntity(entity);
 
 			Map dataValue = new HashMap();
 			dataValue.put(ssn, 101202);
 			EntityManager.getInstance().insertData(entity, dataValue);
 
 			AttributeTypeInformationInterface dateAttributeType = new StringAttributeTypeInformation();
 			ssn.setAttributeTypeInformation(dateAttributeType);
 
 			entity = (Entity) EntityManager.getInstance().persistEntity(entity);
 
 		}
 		catch (DynamicExtensionsApplicationException e)
 		{
 			// TODO Auto-generated catch block
 
 			e.printStackTrace();
 		}
 		catch (DynamicExtensionsSystemException e)
 		{
 			fail();
 			e.printStackTrace();
 			/*   assertTrue("Data exists so can't modify its data type",true); 
 			 ResultSetMetaData metaData = executeQueryForMetadata("select * from "
 			 + entity.getTableProperties().getName());
 			 
 			 try
 			 {
 			 assertEquals(metaData.getColumnType(2),Types.NUMERIC);
 			 }
 			 catch (SQLException e1)
 			 {
 			 fail();
 			 e1.printStackTrace();
 			 }
 			 e.printStackTrace();*/
 		}
 
 	}
 
 	/**
 	 * PURPOSE : To test the method persistEntityGroup
 	 * EXPECTED BEHAVIOR : The new entity group should be stored correctly and should be retrieved back correctly.
 	 * 1. Create entity group.
 	 * 2. Save entityGroup using entity manager.
 	 * 3. Check whether the saved entity group is retrieved back properly or not.
 	 */
 	public void testCreateEntityGroup()
 	{
 		try
 		{
 			EntityManagerInterface entityManager = EntityManager.getInstance();
 			//Step 1
 			EntityGroup entityGroup = (EntityGroup) new MockEntityManager().initializeEntityGroup();
 			//Step 2 
 			entityGroup = (EntityGroup) entityManager.persistEntityGroup(entityGroup);
 			//Step 3			
 			Collection collection = EntityManager.getInstance().getAllEntitiyGroups();
 			assertTrue(collection.contains(entityGroup));
 
 		}
 		catch (Exception e)
 		{
 			//TODO Auto-generated catch block
 			Logger.out.debug(e.getMessage());
 			e.printStackTrace();
 			fail("Exception occured");
 		}
 	}
 
 	/**
 	 * PURPOSE : To test the method getEntityGroupByName
 	 * EXPECTED BEHAVIOR : The new entity group should be stored correctly and should be retrieved back correctly 
 	 * if the name of that entity group is given.
 	 * TEST CASE FLOW :
 	 * 1. Create entity group.
 	 * 2. Save entityGroup using entity manager.
 	 * 3. Check whether the saved entity group is retrieved back properly or not given it's name.
 	 */
 	public void testGetEntityGroupByName()
 	{
 		try
 		{
 			//Step 1
 			EntityManagerInterface entityManagerInterface = EntityManager.getInstance();
 			//Step 2 
 			EntityGroup entityGroup = (EntityGroup) new MockEntityManager().initializeEntityGroup();
 			entityManagerInterface.persistEntityGroup(entityGroup);
 			//Step 3
 			EntityGroupInterface entityGroupInterface = entityManagerInterface
 					.getEntityGroupByName(entityGroup.getName());
 			assertNotNull(entityGroupInterface);
 		}
 		catch (Exception e)
 		{
 			//TODO Auto-generated catch block
 			Logger.out.debug(e.getMessage());
 			e.printStackTrace();
 			fail("Exception occured");
 		}
 	}
 
 	/**
 	 * PURPOSE : To test the method getEntitiesByConceptCode
 	 * EXPECTED BEHAVIOR : All the entities that are associated with the given concept code should be retrieved back
 	 * correctly
 	 * TEST CASE FLOW :
 	 * 1. Create entity with some concept code
 	 * 2. Save entity using entity manager.
 	 * 3. Check whether the saved entity is retrieved back properly or not given it's concept code.
 	 */
 	public void testGetEntitiesByConceptCode()
 	{
 		EntityManagerInterface entityManagerInterface = EntityManager.getInstance();
 		try
 		{
 			//Step 1 
 			Entity entity = (Entity) new MockEntityManager().initializeEntity();
 			SemanticPropertyInterface semanticPropertyInterface = (SemanticPropertyInterface) new MockEntityManager()
 					.initializeSemanticProperty();
 			entity.addSemanticProperty(semanticPropertyInterface);
 			//Step 2 
 			entity = (Entity) entityManagerInterface.persistEntity(entity);
 
 			//Step 3
 			Collection entityCollection = (Collection) EntityManager.getInstance()
 					.getEntitiesByConceptCode(semanticPropertyInterface.getConceptCode());
 			assertTrue(entityCollection != null && entityCollection.size() > 0);
 		}
 		catch (Exception e)
 		{
 			//TODO Auto-generated catch block
 			Logger.out.debug(e.getMessage());
 			fail("Exception occured");
 		}
 	}
 
 	/**
 	 * PURPOSE : To test the method getEntityGroupByName
 	 * EXPECTED BEHAVIOR : The new entity group should be stored correctly and should be retrieved back correctly 
 	 * if the name of that entity group is given.
 	 * TEST CASE FLOW :
 	 * 1. Create entity group.
 	 * 2. Save entityGroup using entity manager.
 	 * 3. Check whether the saved entity group is retrieved back properly or not given it's name.
 	 */
 	public void testGetEntityByName()
 	{
 		EntityManagerInterface entityManagerInterface = EntityManager.getInstance();
 		try
 		{
 			//Step 1
 			Entity entity = (Entity) new MockEntityManager().initializeEntity();
 			//Step 2
 			entity = (Entity) entityManagerInterface.persistEntity(entity);
 			//Step 3
 			EntityInterface entityInterface = (EntityInterface) entityManagerInterface
 					.getEntityByName(entity.getName());
 			assertNotNull(entityInterface);
 		}
 		catch (Exception e)
 		{
 			//TODO Auto-generated catch block
 			Logger.out.debug(e.getMessage());
 			fail("Exception occured");
 		}
 	}
 
 	/**
 	 * This method tests GetAttribute method.
 	 *
 	 */
 	public void testGetAttribute()
 	{
 		EntityManagerInterface entityManagerInterface = EntityManager.getInstance();
 		try
 		{
 			EntityManagerInterface entityManager = EntityManager.getInstance();
 			DomainObjectFactory factory = DomainObjectFactory.getInstance();
 
 			// create user 
 			EntityInterface user = factory.createEntity();
 			AttributeInterface userNameAttribute = factory.createStringAttribute();
 			userNameAttribute.setName("user name");
 			user.setName("user");
 			user.addAbstractAttribute(userNameAttribute);
 
 			user = (Entity) entityManager.persistEntity(user);
 
 			AttributeInterface attributeInterface = (AttributeInterface) entityManagerInterface
 					.getAttribute("user", "user name");
 			assertNotNull(attributeInterface);
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 			//TODO Auto-generated catch block
 			Logger.out.debug(e.getMessage());
 			fail("Exception occured");
 		}
 
 	}
 
 	/**
 	 * Test case to check the behaviour when first entity is saved with a collection attribute and then the that 
 	 * attribute is made non-collection. Expected behavior is that after editing the attribute in
 	 * such a way the column for that attribute should get added to the data table. This column was not present in
 	 * earlier scenario when the attribue was a collection attribute. 
 	 */
 	public void testEditEntityWithCollectionAttribute()
 	{
 		try
 		{
 			EntityManagerInterface entityManager = EntityManager.getInstance();
 			DomainObjectFactory factory = DomainObjectFactory.getInstance();
 
 			// create user 
 			EntityInterface user = factory.createEntity();
 			AttributeInterface userNameAttribute = factory.createStringAttribute();
 			userNameAttribute.setName("user name");
 			userNameAttribute.setIsCollection(true);
 			user.setName("user");
 			user.addAbstractAttribute(userNameAttribute);
 
 			user = (Entity) EntityManager.getInstance().persistEntity(user);
 
 			Entity newEntity = (Entity) EntityManager.getInstance().getEntityByIdentifier(
 					user.getId().toString());
 			assertEquals(user.getName(), newEntity.getName());
 
 			String tableName = newEntity.getTableProperties().getName();
 			assertTrue(isTablePresent(tableName));
 			ResultSetMetaData metaData = executeQueryForMetadata("select * from " + tableName);
 			assertEquals(metaData.getColumnCount(), noOfDefaultColumns);
 
 			userNameAttribute = (AttributeInterface) newEntity
 					.getAttributeByIdentifier(userNameAttribute.getId());
 			userNameAttribute.setIsCollection(false);
 
 			newEntity = (Entity) EntityManager.getInstance().persistEntity(newEntity);
 
 			tableName = newEntity.getTableProperties().getName();
 			assertTrue(isTablePresent(tableName));
 			metaData = executeQueryForMetadata("select * from " + tableName);
 			assertEquals(metaData.getColumnCount(), noOfDefaultColumns + 1);
 
 			userNameAttribute = (AttributeInterface) newEntity
 					.getAttributeByIdentifier(userNameAttribute.getId());
 			userNameAttribute.setIsCollection(true);
 
 			newEntity = (Entity) EntityManager.getInstance().persistEntity(newEntity);
 
 			tableName = newEntity.getTableProperties().getName();
 			assertTrue(isTablePresent(tableName));
 			metaData = executeQueryForMetadata("select * from " + tableName);
 			assertEquals(metaData.getColumnCount(), noOfDefaultColumns);
 
 		}
 		catch (Exception e)
 		{
 			//TODO Auto-generated catch block
 			Logger.out.debug(e.getMessage());
 			e.printStackTrace();
 			fail("Exception occured");
 		}
 
 	}
 
 	/**
 	 * This method tests GetRecordById method for the condition where record and entity does exists
 	 */
 
 	public void testDeleteRecordById()
 	{
 
 		try
 		{
 			EntityManagerInterface entityManagerInterface = EntityManager.getInstance();
 
 			Entity entity = (Entity) new MockEntityManager().initializeEntity();
 			entity = (Entity) entityManagerInterface.persistEntity(entity);
 
 			Entity newEntity = (Entity) entityManagerInterface.getEntityByIdentifier(entity.getId()
 					.toString());
 			Map dataValue = new HashMap();
 			Collection collection = newEntity.getAttributeCollection();
 			collection = EntityManagerUtil.filterSystemAttributes(collection);
 			Iterator attrIterator = collection.iterator();
 			int i = 0;
 			while (attrIterator.hasNext())
 			{
 				AttributeInterface attribute = (AttributeInterface) attrIterator.next();
 				AttributeTypeInformationInterface typeInfo = attribute
 						.getAttributeTypeInformation();
 
 				if (typeInfo instanceof StringAttributeTypeInformation)
 				{
 					dataValue.put(attribute, "temp" + i);
 				}
 				else if (attribute instanceof DateAttributeTypeInformation)
 				{
 					dataValue.put(attribute, "11-01-2006");
 				}
 
 				i++;
 			}
 
 			entityManagerInterface.insertData(newEntity, dataValue);
 			ResultSet resultSet = executeQuery("select count(*) from "
 					+ newEntity.getTableProperties().getName());
 
 			resultSet.next();
 
 			assertEquals(1, resultSet.getInt(1));
 
 			assertEquals("Person", entity.getName());
 			boolean isRecordDeleted = EntityManager.getInstance().deleteRecord(entity, new Long(1));
 			assertTrue(isRecordDeleted);
 
 			assertEquals(Constants.ACTIVITY_STATUS_DISABLED, getActivityStatus(entity, 1L));
 
 		}
 		catch (DynamicExtensionsSystemException e)
 		{
 			//TODO Auto-generated catch block
 			Logger.out.debug(e.getMessage());
 			fail("Exception occured");
 		}
 		catch (DynamicExtensionsApplicationException e)
 		{
 			//TODO Auto-generated catch block
 			Logger.out.debug(e.getMessage());
 			fail("Exception occured");
 		}
 		catch (Exception e)
 		{
 			//          TODO Auto-generated catch block
 			Logger.out.debug(e.getMessage());
 			fail("Exception occured");
 		}
 
 	}
 
 	/**
 	 * This method test for inserting data for a multi select attribute
 	 */
 	public void testInsertDataWithMultiSelectAttribute()
 	{
 		Entity entity = new Entity();
 		entity.setName("Stock Quote");
 		EntityManagerInterface entityManagerInterface = EntityManager.getInstance();
 		DomainObjectFactory factory = DomainObjectFactory.getInstance();
 
 		try
 		{
 
 			//Edit entity
 			AttributeInterface floatAtribute = factory.createFloatAttribute();
 			floatAtribute.setName("Price");
 
 			AttributeInterface commentsAttributes = factory.createStringAttribute();
 			commentsAttributes.setName("comments");
 			commentsAttributes.setIsCollection(true);
 
 			entity.addAbstractAttribute(floatAtribute);
 			entity.addAbstractAttribute(commentsAttributes);
 
 			EntityInterface savedEntity = entityManagerInterface.persistEntity(entity);
 
 			Map dataValue = new HashMap();
 			dataValue.put(floatAtribute, "15.90");
 			List<String> commentsValues = new ArrayList<String>();
 			commentsValues.add("comments1");
 			commentsValues.add("comments2");
 			commentsValues.add("comments3");
 			commentsValues.add("comments4");
 
 			dataValue.put(commentsAttributes, commentsValues);
 
 			entityManagerInterface.insertData(savedEntity, dataValue);
 
 			ResultSet resultSet = executeQuery("select * from "
 					+ savedEntity.getTableProperties().getName());
 			resultSet.next();
 			assertEquals(1, resultSet.getInt(3));
 			ResultSetMetaData metadata = resultSet.getMetaData();
 			assertEquals(metadata.getColumnCount(), noOfDefaultColumns + 1);
 
 			boolean isRecordDeleted = entityManagerInterface.deleteRecord(savedEntity, new Long(1));
 			assertTrue(isRecordDeleted);
 
 			assertEquals(Constants.ACTIVITY_STATUS_DISABLED, getActivityStatus(entity, 1L));
 
 		}
 		catch (DynamicExtensionsSystemException e)
 		{
 			fail();
 			Logger.out.debug(e.getStackTrace());
 		}
 		catch (DynamicExtensionsApplicationException e)
 		{
 			fail();
 			Logger.out.debug(e.getStackTrace());
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 			fail();
 
 			Logger.out.debug(e.getStackTrace());
 		}
 
 	}
 
 	/**
 	 * This method test for updating record for an entity.
 	 */
 	public void testEditRecord()
 	{
 		Entity study = new Entity();
 		study.setName("Study");
 		EntityManagerInterface entityManagerInterface = EntityManager.getInstance();
 		DomainObjectFactory factory = DomainObjectFactory.getInstance();
 
 		try
 		{
 
 			AttributeInterface name = factory.createStringAttribute();
 			name.setName("Study name");
 
 			AttributeInterface userNames = factory.createStringAttribute();
 			userNames.setName("users");
 
 			AttributeInterface studyDate = factory.createDateAttribute();
 			studyDate.setName("Date");
 
 			study.addAbstractAttribute(name);
 			study.addAbstractAttribute(userNames);
 			study.addAbstractAttribute(studyDate);
 
 			EntityInterface savedStudy = entityManagerInterface.persistEntity(study);
 
 			Map dataValue = new HashMap();
 
 			dataValue.put(name, "Java Study");
 			dataValue.put(userNames, "a");
 			dataValue.put(studyDate, "11-20-2006");
 
 			Long recordId = entityManagerInterface.insertData(savedStudy, dataValue);
 
 			Map map = entityManagerInterface.getRecordById(savedStudy, recordId);
 
 			String userName = (String) map.get(userNames);
 			assertEquals("a", userName);
 
 			dataValue.put(userNames, "b");
 			dataValue.put(studyDate, "12-20-2006");
 
 			entityManagerInterface.editData(savedStudy, dataValue, recordId);
 
 			map = entityManagerInterface.getRecordById(savedStudy, recordId);
 			userName = (String) map.get(userNames);
 			assertEquals("b", userName);
 			//assertEquals("12-20-2006", (String) map.get(studyDate));
 
 		}
 		catch (DynamicExtensionsSystemException e)
 		{
 			fail();
 			Logger.out.debug(e.getStackTrace());
 		}
 		catch (DynamicExtensionsApplicationException e)
 		{
 			fail();
 			Logger.out.debug(e.getStackTrace());
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 			fail();
 
 			Logger.out.debug(e.getStackTrace());
 		}
 
 	}
 
 	/**
 	 * This method test for updating data for a multi select attribute
 	 */
 	public void testEditRecordWithMultiselectAttrubteUpdate()
 	{
 		Entity study = new Entity();
 		study.setName("Study");
 		EntityManagerInterface entityManagerInterface = EntityManager.getInstance();
 		DomainObjectFactory factory = DomainObjectFactory.getInstance();
 
 		try
 		{
 
 			AttributeInterface name = factory.createStringAttribute();
 			name.setName("Study name");
 
 			AttributeInterface studyDate = factory.createDateAttribute();
 			studyDate.setName("Date");
 
 			AttributeInterface userNames = factory.createStringAttribute();
 			userNames.setName("users");
 			userNames.setIsCollection(true);
 
 			study.addAttribute(name);
 			study.addAttribute(userNames);
 			study.addAttribute(studyDate);
 			EntityInterface savedStudy = entityManagerInterface.persistEntity(study);
 
 			Map dataValue = new HashMap();
 			List<String> userNameList = new ArrayList<String>();
 			userNameList.add("a");
 			userNameList.add("b");
 			userNameList.add("c");
 
 			dataValue.put(name, "Java Study");
 			dataValue.put(userNames, userNameList);
 			dataValue.put(studyDate, "11-20-2006");
 
 			Long recordId = entityManagerInterface.insertData(savedStudy, dataValue);
 
 			Map map = entityManagerInterface.getRecordById(savedStudy, recordId);
 
 			int noOfUsers = ((List) map.get(userNames)).size();
 			assertEquals(3, noOfUsers);
 			System.out.println(map);
 
 			dataValue.clear();
 			userNameList.clear();
 			userNameList.add("d");
 
 			dataValue.put(userNames, userNameList);
 			dataValue.put(studyDate, "12-20-2006");
 
 			entityManagerInterface.editData(savedStudy, dataValue, recordId);
 
 			map = entityManagerInterface.getRecordById(savedStudy, recordId);
 			noOfUsers = ((List) map.get(userNames)).size();
 			assertEquals(1, noOfUsers);
 			assertEquals("12-20-2006", (String) map.get(studyDate));
 
 			System.out.println(map);
 
 		}
 		catch (DynamicExtensionsSystemException e)
 		{
 			fail();
 			Logger.out.debug(e.getStackTrace());
 		}
 		catch (DynamicExtensionsApplicationException e)
 		{
 			fail();
 			Logger.out.debug(e.getStackTrace());
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 			fail();
 
 			Logger.out.debug(e.getStackTrace());
 		}
 
 	}
 
 	/**
 	 * This method edits an existing attribute to a file type attribute.
 	 */
 	public void testEditFileAttribute()
 	{
 		EntityManagerInterface entityManager = EntityManager.getInstance();
 		DomainObjectFactory factory = DomainObjectFactory.getInstance();
 
 		// create user 
 		EntityInterface user = factory.createEntity();
 		user.setName("User");
 		AttributeInterface resume = factory.createStringAttribute();
 		resume.setName("Resume");
 		user.addAbstractAttribute(resume);
 
 		try
 
 		{
 
 			ResultSet rs = executeQuery("select count(*) from dyextn_file_extensions");
 			rs.next();
 			int beforeCount = rs.getInt(1);
 			System.out.println(beforeCount);
 
 			// save the entity
 			user = entityManager.persistEntity(user);
 			ResultSetMetaData metaData = executeQueryForMetadata("select * from "
 					+ user.getTableProperties().getName());
 			assertEquals(metaData.getColumnCount(), noOfDefaultColumns + 1);
 
 			//Edit attribute: change attribute type to file attrbiute type.
 			Collection<FileExtension> allowedExtn = new HashSet<FileExtension>();
 
 			FileExtension txtExtn = new FileExtension();
 			txtExtn.setFileExtension("txt");
 			allowedExtn.add(txtExtn);
 
 			FileExtension pdfExtn = new FileExtension();
 			pdfExtn.setFileExtension("pdf");
 			allowedExtn.add(pdfExtn);
 
 			allowedExtn.add(txtExtn);
 			allowedExtn.add(pdfExtn);
 
 			AttributeTypeInformation fileTypeInformation = factory
 					.createFileAttributeTypeInformation();
 
 			((FileAttributeTypeInformation) fileTypeInformation).setMaxFileSize(20F);
 			((FileAttributeTypeInformation) fileTypeInformation)
 					.setFileExtensionCollection(allowedExtn);
 
 			resume.setAttributeTypeInformation(fileTypeInformation);
 
 			user = entityManager.persistEntity(user);
 
 			DBUtil.closeConnection();
 			//DBUtil.Connection();
 
 			metaData = executeQueryForMetadata("select * from "
 					+ user.getTableProperties().getName());
 			assertEquals(metaData.getColumnCount(), noOfDefaultColumns);
 
 			//executeQuery("select * from dyextn_file_extensions");
 
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 			fail();
 		}
 	}
 
 	//
 	/**
 	 * This method tests for creating a entity with file attribute.
 	 */
 	public void testCreateFileAttribute()
 	{
 		EntityManagerInterface entityManager = EntityManager.getInstance();
 		DomainObjectFactory factory = DomainObjectFactory.getInstance();
 
 		// create user 
 		EntityInterface user = factory.createEntity();
 		user.setName("User");
 		AttributeInterface resume = factory.createFileAttribute();
 		resume.setName("Resume");
 		user.addAbstractAttribute(resume);
 
 		Collection<FileExtension> allowedExtn = new HashSet<FileExtension>();
 
 		FileExtension txtExtn = new FileExtension();
 		txtExtn.setFileExtension("txt");
 		allowedExtn.add(txtExtn);
 
 		FileExtension pdfExtn = new FileExtension();
 		pdfExtn.setFileExtension("pdf");
 		allowedExtn.add(pdfExtn);
 
 		allowedExtn.add(txtExtn);
 		allowedExtn.add(pdfExtn);
 
 		((FileAttributeTypeInformation) resume.getAttributeTypeInformation()).setMaxFileSize(20F);
 		((FileAttributeTypeInformation) resume.getAttributeTypeInformation())
 				.setFileExtensionCollection(allowedExtn);
 
 		try
 		{
 			ResultSet rs = executeQuery("select count(*) from dyextn_file_extensions");
 			rs.next();
 			int beforeCount = rs.getInt(1);
 
 			user = entityManager.persistEntity(user);
 			ResultSetMetaData metaData = executeQueryForMetadata("select * from "
 					+ user.getTableProperties().getName());
 			assertEquals(metaData.getColumnCount(), noOfDefaultColumns);
 
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 			fail();
 		}
 	}
 
 	/**
 	 * 
 	 */
 	public void testCreateEntity()
 	{
 		try
 		{
 			Entity entity = (Entity) new MockEntityManager().initializeEntity();
 			TaggedValueInterface taggedValue = DomainObjectFactory.getInstance()
 					.createTaggedValue();
 			taggedValue.setKey("a");
 			taggedValue.setValue("b");
 			entity.addTaggedValue(taggedValue);
 			entity = (Entity) EntityManager.getInstance().persistEntity(entity);
 
 			Entity newEntity = (Entity) EntityManager.getInstance().getEntityByIdentifier(
 					entity.getId().toString());
 			assertEquals(entity.getName(), newEntity.getName());
 
 			String tableName = entity.getTableProperties().getName();
 			String query = "Select * from " + tableName;
 			executeQueryForMetadata(query);
 		}
 		catch (Exception e)
 		{
 			//TODO Auto-generated catch block
 			Logger.out.debug(e.getMessage());
 			e.printStackTrace();
 			fail("Exception occured");
 		}
 
 	}
 
 	/**
 	 * 
 	 */
 
 	public void testCreateEntityWithEntityGroup()
 	{
 		try
 		{
 			Entity entity = (Entity) new MockEntityManager().initializeEntity();
 			EntityGroup entityGroup = new EntityGroup();
 			entityGroup.setName("testEntityGroup");
 			entity.addEntityGroupInterface(entityGroup);
 			entityGroup.addEntity(entity);
 			entity = (Entity) EntityManager.getInstance().persistEntity(entity);
 			Entity newEntity = (Entity) EntityManager.getInstance().getEntityByIdentifier(
 					entity.getId().toString());
 			//  Checking whether metadata information is saved properly or not.
 			assertEquals(entity.getName(), newEntity.getName());
 			Collection collection = newEntity.getEntityGroupCollection();
 			Iterator iter = collection.iterator();
 			EntityGroup eg = (EntityGroup) iter.next();
 			assertEquals(eg.getId(), entityGroup.getId());
 
 		}
 		catch (Exception e)
 		{
 			//TODO Auto-generated catch block
 			Logger.out.debug(e.getMessage());
 			fail("Exception occured");
 		}
 
 	}
 
 	/**
 	 * This method tests GetRecordById method for the condition where record and entity does exists
 	 */
 
 	public void testGetRecordById()
 	{
 
 		try
 		{
 			EntityManagerInterface entityManagerInterface = EntityManager.getInstance();
 
 			Entity entity = (Entity) new MockEntityManager().initializeEntity();
 			entity = (Entity) entityManagerInterface.persistEntity(entity);
 
 			Entity newEntity = (Entity) entityManagerInterface.getEntityByIdentifier(entity.getId()
 					.toString());
 			Map dataValue = new HashMap();
 			Collection collection = newEntity.getAttributeCollection();
 			collection = EntityManagerUtil.filterSystemAttributes(collection);
 			Iterator attrIterator = collection.iterator();
 			int i = 0;
 			while (attrIterator.hasNext())
 			{
 				AttributeInterface attribute = (AttributeInterface) attrIterator.next();
 				AttributeTypeInformationInterface typeInfo = attribute
 						.getAttributeTypeInformation();
 
 				if (typeInfo instanceof StringAttributeTypeInformation)
 				{
 					dataValue.put(attribute, "temp" + i);
 				}
 				else if (attribute instanceof DateAttributeTypeInformation)
 				{
 					dataValue.put(attribute, "11-01-2006");
 				}
 
 				i++;
 			}
 
 			entityManagerInterface.insertData(newEntity, dataValue);
 
 			assertEquals("Person", entity.getName());
 			Map map = EntityManager.getInstance().getRecordById(entity, new Long(1));
 
 			System.out.println(map);
 		}
 		catch (DynamicExtensionsSystemException e)
 		{
 			//TODO Auto-generated catch block
 			Logger.out.debug(e.getMessage());
 			fail("Exception occured");
 		}
 		catch (DynamicExtensionsApplicationException e)
 		{
 			//TODO Auto-generated catch block
 			Logger.out.debug(e.getMessage());
 			fail("Exception occured");
 		}
 
 	}
 
 	/**
 	 * This method tests GetRecordById method for the condition wheer record does not exists for given id.
 	 */
 
 	public void testGetRecordByIdNoRecord()
 	{
 
 		try
 		{
 			EntityManagerInterface entityManagerInterface = EntityManager.getInstance();
 
 			Entity entity = (Entity) new MockEntityManager().initializeEntity();
 			entity = (Entity) entityManagerInterface.persistEntity(entity);
 
 			Map map = EntityManager.getInstance().getRecordById(entity, new Long(1));
 			assertEquals(0, map.size());
 		}
 		catch (DynamicExtensionsSystemException e)
 		{
 			//TODO Auto-generated catch block
 			Logger.out.debug(e.getMessage());
 			fail("Exception occured");
 		}
 		catch (DynamicExtensionsApplicationException e)
 		{
 			//TODO Auto-generated catch block
 			Logger.out.debug(e.getMessage());
 			fail("Exception occured");
 		}
 
 	}
 
 	/**
 	 * This method tests GetRecordById method for the condition where entity is not saved , entity  is null or record id id null
 	 */
 
 	public void testGetRecordByIdEntityNotSaved()
 	{
 
 		EntityManagerInterface entityManagerInterface = EntityManager.getInstance();
 		Entity entity = null;
 
 		try
 		{
 			entity = (Entity) new MockEntityManager().initializeEntity();
 
 			Map map = EntityManager.getInstance().getRecordById(entity, new Long(1));
 			fail("Exception should have be thrown since entity is not saved");
 
 		}
 		catch (DynamicExtensionsSystemException e)
 		{
 			assertTrue(true);
 		}
 		catch (DynamicExtensionsApplicationException e)
 		{
 			//TODO Auto-generated catch block
 			Logger.out.debug(e.getMessage());
 			fail("Unexpected Exception occured");
 		}
 
 		try
 		{
 			Map map = EntityManager.getInstance().getRecordById(null, new Long(1));
 			fail("Exception should have be thrown since entity is not saved");
 		}
 		catch (DynamicExtensionsSystemException e)
 		{
 			assertTrue(true);
 		}
 		catch (DynamicExtensionsApplicationException e)
 		{
 			//TODO Auto-generated catch block
 			Logger.out.debug(e.getMessage());
 		}
 
 		try
 		{
 			entity = (Entity) entityManagerInterface.persistEntity(entity);
 
 			Map map = EntityManager.getInstance().getRecordById(entity, null);
 			fail("Exception should have be thrown since entity is not saved");
 		}
 		catch (DynamicExtensionsSystemException e)
 		{
 			assertTrue(true);
 		}
 		catch (DynamicExtensionsApplicationException e)
 		{
 			//TODO Auto-generated catch block
 			Logger.out.debug(e.getMessage());
 		}
 
 	}
 
 	/**
 	 * 
 	 */
 
 	public void testCreateContainer()
 	{
 		try
 		{
 			Container container = (Container) new MockEntityManager().getContainer("abc");
 			EntityManager.getInstance().persistContainer(container);
 			Collection list = EntityManager.getInstance().getAllContainers();
 			assertNotNull(list);
 			Iterator iter = list.iterator();
 			boolean flag = false;
 			while (iter.hasNext())
 			{
 				Container cont = (Container) iter.next();
 				if (cont.getId().equals(container.getId()))
 				{
 					flag = true;
 					break;
 				}
 			}
 			assertTrue(flag);
 		}
 		catch (Exception e)
 		{
 			//TODO Auto-generated catch block
 			Logger.out.debug(e.getMessage());
 			fail("Exception occured");
 		}
 
 	}
 
 	/**
 	 * 
 	 */
 
 	public void testCreateContainerForContainerWithoutEntity()
 	{
 		try
 		{
 			Container container = new Container();
 			EntityManager.getInstance().persistContainer(container);
 			Collection list = EntityManager.getInstance().getAllContainers();
 			assertNotNull(list);
 			Iterator iter = list.iterator();
 			boolean flag = false;
 			while (iter.hasNext())
 			{
 				Container cont = (Container) iter.next();
 				if (cont.getId().equals(container.getId()))
 				{
 					flag = true;
 					break;
 				}
 			}
 			assertTrue(flag);
 		}
 		catch (Exception e)
 		{
 			// //TODO Auto-generated catch block
 			Logger.out.debug(e.getMessage());
 			fail("Exception occured");
 		}
 	}
 
 	/**
 	 * 
 	 */
 
 	public void testEditEntityForNewAddedAttribute()
 	{
 		try
 		{
 			Entity entity = (Entity) new MockEntityManager().initializeEntity();
 			entity = (Entity) EntityManager.getInstance().persistEntity(entity);
 			AttributeInterface attr = new MockEntityManager().initializeStringAttribute("na", "ab");
 			attr.setEntity(entity);
 			entity.addAbstractAttribute(attr);
 			entity = (Entity) EntityManager.getInstance().persistEntity(entity);
 			//   Checking whether metadata information is saved properly or not.
 			String tableName = entity.getTableProperties().getName();
 
 		}
 		catch (Exception e)
 		{
 			//     //TODO Auto-generated catch block
 			Logger.out.debug(e.getMessage());
 			fail("Exception occured");
 		}
 
 	}
 
 	/**
 	 * 
 	 */
 
 	public void testEditEntityForModifiedIsNullableAttribute()
 	{
 		try
 		{
 			Entity entity = (Entity) new MockEntityManager().initializeEntity();
 			AttributeInterface attr = new MockEntityManager().initializeStringAttribute("na", "ab");
 			attr.setEntity(entity);
 			entity.addAbstractAttribute(attr);
 			entity = (Entity) EntityManager.getInstance().persistEntity(entity);
 			attr = (AttributeInterface) entity.getAttributeByIdentifier(attr.getId());
 			attr.setIsNullable(new Boolean(false));
 			entity = (Entity) EntityManager.getInstance().persistEntity(entity);
 			Attribute savedAttribute = (Attribute) entity.getAttributeByIdentifier(attr.getId());
 			assertFalse(savedAttribute.getIsNullable());
 			//   Checking whether metadata information is saved properly or not.
 			String tableName = entity.getTableProperties().getName();
 			//   //TODO Check table query
 		}
 		catch (Exception e)
 		{
 			// //TODO Auto-generated catch block
 			Logger.out.debug(e.getMessage());
 			fail("Exception occured");
 		}
 
 	}
 
 	/**
 	 * This method tests for creating a entity with file attribute.
 	 */
 	public void testInsertRecordForFileAttribute1()
 	{
 		EntityManagerInterface entityManager = EntityManager.getInstance();
 		DomainObjectFactory factory = DomainObjectFactory.getInstance();
 
 		// create user 
 		EntityInterface user = factory.createEntity();
 		user.setName("User");
 
 		AttributeInterface age = factory.createIntegerAttribute();
 		age.setName("Age");
 		user.addAbstractAttribute(age);
 
 		AttributeInterface resume = factory.createFileAttribute();
 		resume.setName("Resume");
 		user.addAbstractAttribute(resume);
 
 		Collection<FileExtension> allowedExtn = new HashSet<FileExtension>();
 
 		FileExtension txtExtn = new FileExtension();
 		txtExtn.setFileExtension("txt");
 		allowedExtn.add(txtExtn);
 
 		FileExtension pdfExtn = new FileExtension();
 		pdfExtn.setFileExtension("pdf");
 		allowedExtn.add(pdfExtn);
 
 		allowedExtn.add(txtExtn);
 		allowedExtn.add(pdfExtn);
 
 		((FileAttributeTypeInformation) resume.getAttributeTypeInformation()).setMaxFileSize(20F);
 		((FileAttributeTypeInformation) resume.getAttributeTypeInformation())
 				.setFileExtensionCollection(allowedExtn);
 
 		try
 		{
 			user = entityManager.persistEntity(user);
 
 			FileAttributeRecordValue fileRecord = new FileAttributeRecordValue();
 			fileRecord.setContentType("PDF");
 			fileRecord.setFileName("tp.java");
 			String fileContent = "this is cntent of the file";
 			//File f = new File("C:\\BinaryBlobType.java");
 			fileRecord.setFileContent(fileContent.getBytes());
 
 			Map dataValue = new HashMap();
 			dataValue.put(age, "45");
 			dataValue.put(resume, fileRecord);
 
 			Long recordId = entityManager.insertData(user, dataValue);
 
 			ResultSet resultSet = executeQuery("select count(*) from "
 					+ user.getTableProperties().getName());
 			resultSet.next();
 			assertEquals(1, resultSet.getInt(1));
 
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 			fail();
 		}
 	}
 
 	/**
 	 * This method tests for creating a entity with file attribute.
 	 */
 	public void testInsertRecordForFileAttribute()
 	{
 		EntityManagerInterface entityManager = EntityManager.getInstance();
 		DomainObjectFactory factory = DomainObjectFactory.getInstance();
 
 		// create user 
 		EntityInterface user = factory.createEntity();
 		user.setName("User");
 
 		AttributeInterface age = factory.createIntegerAttribute();
 		age.setName("Age");
 		user.addAbstractAttribute(age);
 
 		AttributeInterface resume = factory.createFileAttribute();
 		resume.setName("Resume");
 		user.addAbstractAttribute(resume);
 
 		Collection<FileExtension> allowedExtn = new HashSet<FileExtension>();
 
 		FileExtension txtExtn = new FileExtension();
 		txtExtn.setFileExtension("txt");
 		allowedExtn.add(txtExtn);
 
 		FileExtension pdfExtn = new FileExtension();
 		pdfExtn.setFileExtension("pdf");
 		allowedExtn.add(pdfExtn);
 
 		allowedExtn.add(txtExtn);
 		allowedExtn.add(pdfExtn);
 
 		((FileAttributeTypeInformation) resume.getAttributeTypeInformation()).setMaxFileSize(20F);
 		((FileAttributeTypeInformation) resume.getAttributeTypeInformation())
 				.setFileExtensionCollection(allowedExtn);
 
 		try
 		{
 			user = entityManager.persistEntity(user);
 
 			FileAttributeRecordValue fileRecord = new FileAttributeRecordValue();
 			fileRecord.setContentType("PDF");
 			fileRecord.setFileName("tp.java");
 			String fileContent = "this is cntent of the file";
 			//File f = new File("C:\\BinaryBlobType.java");
 			fileRecord.setFileContent(fileContent.getBytes());
 
 			Map dataValue = new HashMap();
 			dataValue.put(age, "45");
 			dataValue.put(resume, fileRecord);
 
 			Long recordId = entityManager.insertData(user, dataValue);
 
 			ResultSet resultSet = executeQuery("select count(*) from "
 					+ user.getTableProperties().getName());
 			resultSet.next();
 			assertEquals(1, resultSet.getInt(1));
 
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 			fail();
 		}
 	}
 
 	/**
 	 * This method tests for creating a entity with file attribute.
 	 */
 	public void testGerRecordForFileAttribute()
 	{
 		EntityManagerInterface entityManager = EntityManager.getInstance();
 		DomainObjectFactory factory = DomainObjectFactory.getInstance();
 
 		// create user 
 		EntityInterface user = factory.createEntity();
 		user.setName("User");
 
 		AttributeInterface age = factory.createIntegerAttribute();
 		age.setName("Age");
 		user.addAbstractAttribute(age);
 
 		AttributeInterface resume = factory.createFileAttribute();
 		resume.setName("Resume");
 		user.addAbstractAttribute(resume);
 
 		Collection<FileExtension> allowedExtn = new HashSet<FileExtension>();
 
 		FileExtension txtExtn = new FileExtension();
 		txtExtn.setFileExtension("txt");
 		allowedExtn.add(txtExtn);
 
 		FileExtension pdfExtn = new FileExtension();
 		pdfExtn.setFileExtension("pdf");
 		allowedExtn.add(pdfExtn);
 
 		allowedExtn.add(txtExtn);
 		allowedExtn.add(pdfExtn);
 
 		((FileAttributeTypeInformation) resume.getAttributeTypeInformation()).setMaxFileSize(20F);
 		((FileAttributeTypeInformation) resume.getAttributeTypeInformation())
 				.setFileExtensionCollection(allowedExtn);
 
 		try
 		{
 			user = entityManager.persistEntity(user);
 
 			FileAttributeRecordValue fileRecord = new FileAttributeRecordValue();
 			fileRecord.setContentType("PDF");
 			fileRecord.setFileName("tp.java");
 			String fileContent = "this is content of the file";
 			fileRecord.setFileContent(fileContent.getBytes());
 
 			Map dataValue = new HashMap();
 			dataValue.put(age, "45");
 			dataValue.put(resume, fileRecord);
 
 			Long recordId = entityManager.insertData(user, dataValue);
 
 			dataValue = entityManager.getRecordById(user, recordId);
 
 			assertEquals("45", dataValue.get(age));
 			assertEquals("tp.java", ((FileAttributeRecordValue) dataValue.get(resume))
 					.getFileName());
 
 			System.out.println(dataValue);
 
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 			fail();
 		}
 	}
 
 	/**
 	 * This method tests for creating a entity with file attribute.
 	 */
 	public void testEditRecordForFileAttribute()
 	{
 		EntityManagerInterface entityManager = EntityManager.getInstance();
 		DomainObjectFactory factory = DomainObjectFactory.getInstance();
 
 		// create user 
 		EntityInterface user = factory.createEntity();
 		user.setName("User");
 
 		AttributeInterface age = factory.createIntegerAttribute();
 		age.setName("Age");
 		user.addAbstractAttribute(age);
 
 		AttributeInterface resume = factory.createFileAttribute();
 		resume.setName("Resume");
 		user.addAbstractAttribute(resume);
 
 		Collection<FileExtension> allowedExtn = new HashSet<FileExtension>();
 
 		FileExtension txtExtn = new FileExtension();
 		txtExtn.setFileExtension("txt");
 		allowedExtn.add(txtExtn);
 
 		FileExtension pdfExtn = new FileExtension();
 		pdfExtn.setFileExtension("pdf");
 		allowedExtn.add(pdfExtn);
 
 		allowedExtn.add(txtExtn);
 		allowedExtn.add(pdfExtn);
 
 		((FileAttributeTypeInformation) resume.getAttributeTypeInformation()).setMaxFileSize(20F);
 		((FileAttributeTypeInformation) resume.getAttributeTypeInformation())
 				.setFileExtensionCollection(allowedExtn);
 
 		try
 		{
 			user = entityManager.persistEntity(user);
 
 			FileAttributeRecordValue fileRecord = new FileAttributeRecordValue();
 			fileRecord.setContentType("PDF");
 			fileRecord.setFileName("tp.java");
 			String fileContent = "this is content of the file";
 			fileRecord.setFileContent(fileContent.getBytes());
 
 			Map dataValue = new HashMap();
 			dataValue.put(age, "45");
 			dataValue.put(resume, fileRecord);
 
 			Long recordId = entityManager.insertData(user, dataValue);
 			//recordId = entityManager.insertData(user, dataValue);
 
 			dataValue = entityManager.getRecordById(user, recordId);
 			dataValue.clear();
 			fileRecord.setFileName("new file name");
 			fileRecord.setFileContent("modified file contents".getBytes());
 			dataValue.put(resume, fileRecord);
 			entityManager.editData(user, dataValue, recordId);
 
 			dataValue = entityManager.getRecordById(user, recordId);
 
 			assertEquals("new file name", ((FileAttributeRecordValue) dataValue.get(resume))
 					.getFileName());
 
 			System.out.println(dataValue);
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 			fail();
 		}
 	}
 
 	/**
 	 * This method tests for creating a entity with file attribute.
 	 */
 	public void testDeleteRecordForFileAttribute()
 	{
 		EntityManagerInterface entityManager = EntityManager.getInstance();
 		DomainObjectFactory factory = DomainObjectFactory.getInstance();
 
 		EntityInterface user = factory.createEntity();
 		user.setName("User");
 
 		AttributeInterface age = factory.createIntegerAttribute();
 		age.setName("Age");
 		user.addAbstractAttribute(age);
 
 		AttributeInterface resume = factory.createFileAttribute();
 		resume.setName("Resume");
 		user.addAbstractAttribute(resume);
 
 		Collection<FileExtension> allowedExtn = new HashSet<FileExtension>();
 
 		FileExtension txtExtn = new FileExtension();
 		txtExtn.setFileExtension("txt");
 		allowedExtn.add(txtExtn);
 
 		FileExtension pdfExtn = new FileExtension();
 		pdfExtn.setFileExtension("pdf");
 		allowedExtn.add(pdfExtn);
 
 		allowedExtn.add(txtExtn);
 		allowedExtn.add(pdfExtn);
 
 		((FileAttributeTypeInformation) resume.getAttributeTypeInformation()).setMaxFileSize(20F);
 		((FileAttributeTypeInformation) resume.getAttributeTypeInformation())
 				.setFileExtensionCollection(allowedExtn);
 
 		try
 		{
 			user = entityManager.persistEntity(user);
 
 			FileAttributeRecordValue fileRecord = new FileAttributeRecordValue();
 			fileRecord.setContentType("PDF");
 			fileRecord.setFileName("tp.java");
 			String fileContent = "this is content of the file";
 			fileRecord.setFileContent(fileContent.getBytes());
 
 			Map dataValue = new HashMap();
 			dataValue.put(age, "45");
 			dataValue.put(resume, fileRecord);
 
 			Long recordId = entityManager.insertData(user, dataValue);
 
 			dataValue = entityManager.getRecordById(user, recordId);
 
 			ResultSet resultSet = executeQuery("select count(*) from dyextn_attribute_record");
 			resultSet.next();
 			int beforeCnt = resultSet.getInt(1);
 			resultSet.close();
 
 			entityManager.deleteRecord(user, recordId);
 
 			resultSet = executeQuery("select count(*) from dyextn_attribute_record");
 			resultSet.next();
 			int afterCnt = resultSet.getInt(1);
 
 			assertEquals(1, beforeCnt - afterCnt);
 
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 			fail();
 		}
 
 	}
 
 	/**
 	 * PURPOSE : to test the method persistEntityMetadata. 
 	 * EXPECTED BEHAVIOR : It should only save the metadata information of the entity and not create the data table for the entity.
 	 * TEST CASE FLOW : 
 	 * 1.Create an entity 
 	 * 2.Populate the entity. 
 	 * 3.Test that the metadata information is properly saved or not. 
 	 * 4.Check that the data table is not created.
 	 */
 	public void testPersistEntityMetadata()
 	{
 		try
 		{
 			//Step 1 
 			Entity entity = (Entity) new MockEntityManager().initializeEntity();
 			TaggedValueInterface taggedValue = DomainObjectFactory.getInstance()
 					.createTaggedValue();
 			//Step 2 
 			taggedValue.setKey("a");
 			taggedValue.setValue("b");
 			entity.addTaggedValue(taggedValue);
 			entity = (Entity) EntityManager.getInstance().persistEntityMetadata(entity, false);
 
 			//Step 3 
 			Entity newEntity = (Entity) EntityManager.getInstance().getEntityByIdentifier(
 					entity.getId().toString());
 			assertEquals(entity.getName(), newEntity.getName());
 			//Step 4
 			String tableName = entity.getTableProperties().getName();
 			assertFalse(isTablePresent(tableName));
 		}
 		catch (Exception e)
 		{
 			//TODO Auto-generated catch block
 			Logger.out.debug(e.getMessage());
 			e.printStackTrace();
 			fail("Exception occured");
 		}
 
 	}
 
 	/**
 	 * This method tests the creation of entity group
 	 */
 	public void testCreateEntityGroupForRollback()
 	{
 		EntityGroup entityGroup = null;
 		try
 		{
 			MockEntityManager mock = new MockEntityManager();
 			entityGroup = (EntityGroup) mock.initializeEntityGroup();
 			for (int i = 0; i <= 11; i++)
 			{
 				Entity entity = (Entity) mock.initializeEntity();
 				entityGroup.addEntity(entity);
 				if (i == 10)
 				{
 					entity = (Entity) mock.initializeEntity();
 					TableProperties tableProperties = new TableProperties();
 					tableProperties.setName("@#$%@$%$^");
 					entity.setTableProperties(tableProperties);
 					entityGroup.addEntity(entity);
 				}
 			}
 			entityGroup = (EntityGroup) EntityManager.getInstance().persistEntityGroup(entityGroup);
 			fail();
 		}
 		catch (Exception e)
 		{
 
 			boolean isTablePresent = isTablePresent(entityGroup.getEntityCollection().iterator()
 					.next().getTableProperties().getName());
 			assertFalse(isTablePresent);
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * This method tests the creation of entity group
 	 */
 	public void testCreateEntityGroupForMultipleEntities()
 	{
 		EntityGroup entityGroup = null;
 		try
 		{
 			MockEntityManager mock = new MockEntityManager();
 			entityGroup = (EntityGroup) mock.initializeEntityGroup();
 			for (int i = 0; i <= 11; i++)
 			{
 				Entity entity = (Entity) mock.initializeEntity();
 				entityGroup.addEntity(entity);
 			}
 			entityGroup = (EntityGroup) EntityManager.getInstance().persistEntityGroup(entityGroup);
 			Iterator iterator = entityGroup.getEntityCollection().iterator();
 			while (iterator.hasNext())
 			{
 				Entity entity = (Entity) iterator.next();
 				boolean isTablePresent = isTablePresent(entity.getTableProperties().getName());
 				assertTrue(isTablePresent);
 			}
 		}
 		catch (Exception e)
 		{
 			fail();
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * PURPOSE : To test the method persistEntityGroupMetadata
 	 * EXPECTED BEHAVIOR : Entity group metadata should be stored properly without creating the data tables 
 	 * for the associated entities.
 	 * TEST CASE FLOW : 
 	 * 1. Create entity group
 	 * 2. Create entities and add them to the entity group.
 	 * 3. Call the method persistEntityGroupMetadata to store the metdata information.
 	 * 4. Check whether the metadata is stored correctly.
 	 * 5. Check that the data tables are not created for the associated entities.
 	 */
 	public void testPersistEntityGroupMetadataForMultipleEntities()
 	{
 		EntityGroup entityGroup = null;
 		try
 		{
 			//Step 1 
 			MockEntityManager mock = new MockEntityManager();
 			entityGroup = (EntityGroup) mock.initializeEntityGroup();
 			entityGroup.setEntityCollection(new HashSet());
 			//Step 2 
 			for (int i = 0; i <= 9; i++)
 			{
 				Entity entity = (Entity) mock.initializeEntity();
 				entityGroup.addEntity(entity);
 			}
 			//Step 3
 			entityGroup = (EntityGroup) EntityManager.getInstance().persistEntityGroupMetadata(
 					entityGroup);
 			//Step 4
 			Collection entityCollection = entityGroup.getEntityCollection();
 			assertEquals(10, entityCollection.size());
 			Iterator iterator = entityCollection.iterator();
 			while (iterator.hasNext())
 			{
 				Entity entity = (Entity) iterator.next();
 				//Step 5
 				boolean isTablePresent = isTablePresent(entity.getTableProperties().getName());
 				assertFalse(isTablePresent);
 			}
 		}
 		catch (Exception e)
 		{
 			fail();
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * 
 	 *
 	 */
 
 	public void testCreateEntityForRollbackQuery()
 	{
 		Entity entity = null;
 		try
 		{
 			entity = (Entity) new MockEntityManager().initializeEntity();
 			TableProperties tableProperties = new TableProperties();
 			tableProperties.setName("Created_table");
 			entity.setTableProperties(tableProperties);
 			String query = "create table Created_table (id integer)";
 			executeQueryDDL(query);
 			EntityManager.getInstance().persistEntity(entity);
 			fail("Exception should have occured but did not");
 		}
 		catch (DynamicExtensionsSystemException e)
 		{
 			Logger.out.info("Exception because of wrong table name.");
 			Logger.out.info(e.getMessage());
 		}
 		catch (Exception e)
 		{
 			//TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		finally
 		{
 			executeQueryDDL("drop table Created_table");
 			try
 			{
 				Entity newEntity = (Entity) EntityManager.getInstance().getEntityByIdentifier(
 						entity.getId().toString());
 				fail();
 
 			}
 			catch (DynamicExtensionsApplicationException e)
 			{
 				Logger.out.info("Entity object not found in the database ....");
 			}
 			catch (Exception e1)
 			{
 				e1.printStackTrace();
 			}
 
 		}
 
 	}
 
 	/**
 	 * 
 	 *
 	 */
 
 	public void testCreateEntityForQueryException()
 	{
 		try
 		{
 			Entity entity = (Entity) new MockEntityManager().initializeEntity();
 			TableProperties tableProperties = new TableProperties();
 			tableProperties.setName("!##$$%");
 			entity.setTableProperties(tableProperties);
 			entity = (Entity) EntityManager.getInstance().persistEntity(entity);
 			fail("Exception should have occured but did not");
 		}
 		catch (DynamicExtensionsSystemException e)
 		{
 			Logger.out.info("Exception because of wrong table name.");
 			Logger.out.info(e.getMessage());
 		}
 		catch (Exception e)
 		{
 			//TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 	}
 
 	/**
 	 * 
 	 */
 	public void testgetAllContainersByEntityGroupId()
 	{
 		EntityManagerInterface entityManager = EntityManager.getInstance();
 		DomainObjectFactory factory = DomainObjectFactory.getInstance();
 
 		ContainerInterface userContainer = factory.createContainer();
 		userContainer.setCaption("userContainer");
 		EntityInterface user = factory.createEntity();
 		user.setName("User");
 		userContainer.setEntity(user);
 
 		ContainerInterface managerContainer = factory.createContainer();
 		managerContainer.setCaption("managerContainer");
 		EntityInterface manager = factory.createEntity();
 		manager.setName("Manager");
 		managerContainer.setEntity(manager);
 
 		EntityGroupInterface userGroup = factory.createEntityGroup();
 		userGroup.setName("test_" + new Double(Math.random()).toString());
 		userGroup.addEntity(user);
 		userGroup.addEntity(manager);
 		
 		user.addEntityGroupInterface(userGroup);
 		manager.addEntityGroupInterface(userGroup);
 
 		ContainerInterface studyContainer = factory.createContainer();
 		studyContainer.setCaption("newstudyContainer");
 		EntityInterface study = factory.createEntity();
 		study.setName("study");
 		studyContainer.setEntity(study);
 
 		ContainerInterface javaStudyContainer = factory.createContainer();
 		javaStudyContainer.setCaption("javaStudyContainer");
 		EntityInterface javaStudy = factory.createEntity();
 		javaStudy.setName("javaStudy");
 		javaStudyContainer.setEntity(javaStudy);
 
 		EntityGroupInterface studyGroup = factory.createEntityGroup();
 		studyGroup.setName("test_" + new Double(Math.random()).toString());
 		studyGroup.addEntity(study);
 		studyGroup.addEntity(javaStudy);
 
 		study.addEntityGroupInterface(studyGroup);
 		javaStudy.addEntityGroupInterface(studyGroup);
 
 		try
 		{
 
 			userContainer = entityManager.persistContainer(userContainer);
 			managerContainer = entityManager.persistContainer(managerContainer);
 			studyContainer = entityManager.persistContainer(studyContainer);
 			javaStudyContainer = entityManager.persistContainer(javaStudyContainer);
 
 			Long studyGroupId = studyGroup.getId();
 
 			Collection studyGroupContainerCollection = entityManager
 					.getAllContainersByEntityGroupId(studyGroupId);
 
 			assertEquals(2, studyGroupContainerCollection.size());
 
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 			fail();
 		}
 	}
 
 	public void testGetContainerByEntityIdentifier()
 	{
 
 		try
 		{
 			ContainerInterface containerInterface = (Container) new MockEntityManager()
 					.getContainer("abc");
 			EntityInterface entityInterface = containerInterface.getEntity();
 			EntityManagerInterface entityManager = EntityManager.getInstance();
 			entityManager.persistContainer(containerInterface);
 			assertNotNull(entityManager.getContainerByEntityIdentifier(entityInterface.getId()));
 
 		}
 		catch (DynamicExtensionsApplicationException e)
 		{
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		catch (DynamicExtensionsSystemException e)
 		{
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 	}
 
 	/**
 	 * PURPOSE : to test the method insertData for attribute having default value specified. 
 	 * EXPECTED BEHAVIOR : since default value is only for display purpose, it should not be persisted
 	 *                     if user has explicitly removed this value /not mentioned for the attribute. 
 	 * 
 	 * TEST CASE FLOW : 
 	 * 1.Create an entity 
 	 * 2.Persist the entity. 
 	 * 3.Insert data without specifying value for the attribute that has some default value.
 	 * 4.Check that the value for that attribute is null.
 	 */
 	public void testInsertDataWithdefaultValue()
 	{
 		Entity entity = new Entity();
 		entity.setName("Stock Quote");
 		EntityManagerInterface entityManagerInterface = EntityManager.getInstance();
 		DomainObjectFactory factory = DomainObjectFactory.getInstance();
 
 		try
 		{
 			//Step 1.
 			AttributeInterface commentsAttributes = factory.createStringAttribute();
 			commentsAttributes.setName("comments");
 			StringValueInterface defalutValue = factory.createStringValue();
 			defalutValue.setValue("this is default value");
 			commentsAttributes.getAttributeTypeInformation().setDefaultValue(defalutValue);
 
 			entity.addAbstractAttribute(commentsAttributes);
 
 			AttributeInterface tempAttributes = factory.createStringAttribute();
 			tempAttributes.setName("temp");
 			entity.addAbstractAttribute(tempAttributes);
 
 			//Step 2.
 			EntityInterface savedEntity = entityManagerInterface.persistEntity(entity);
 
 			//Step 3.
 			Map dataValue = new HashMap();
 			//dataValue.put(commentsAttributes, "this is not default comment");
 			dataValue.put(tempAttributes, "temp");
 
 			entityManagerInterface.insertData(savedEntity, dataValue);
 
 			//Step 4.
 			ResultSet resultSet = executeQuery("select * from "
 					+ savedEntity.getTableProperties().getName());
 			resultSet.next();
 			assertEquals(null, resultSet.getString(2));
 
 		}
 		catch (DynamicExtensionsSystemException e)
 		{
 			fail();
 			Logger.out.debug(e.getStackTrace());
 		}
 		catch (DynamicExtensionsApplicationException e)
 		{
 			fail();
 			Logger.out.debug(e.getStackTrace());
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 			fail();
 
 			Logger.out.debug(e.getStackTrace());
 		}
 
 	}
 
 	/**
 	 * PURPOSE : to test the fix for bug 3209. 
 	 *            
 	 *  
 	 * EXPECTED BEHAVIOR : If file attribute is added during edit,its column should not be getting added 
 	 * 
 	 * TEST CASE FLOW : 
 	 * 1.Create an entity 
 	 * 2.Persist the entity.
 	 * 3.edit entity adding a file attribute
 	 * 4. persist again 
 	 * 5. chk for no of column for that entity
 	 */
 	public void testEditEntityToAddFileAttribute()
 	{
 		EntityManagerInterface entityManagerInterface = EntityManager.getInstance();
 		DomainObjectFactory factory = DomainObjectFactory.getInstance();
 
 		try
 		{
 			//Step 1.
 			EntityInterface user = factory.createEntity();
 			user.setName("user");
 
 			AttributeInterface commentsAttributes = factory.createStringAttribute();
 			commentsAttributes.setName("comments");
 			user.addAbstractAttribute(commentsAttributes);
 
 			//Step 2.
 			EntityInterface savedEntity = entityManagerInterface.persistEntity(user);
 			java.sql.ResultSetMetaData metadata = executeQueryForMetadata("select * from "
 					+ savedEntity.getTableProperties().getName());
 			assertEquals(metadata.getColumnCount(), noOfDefaultColumns + 1);
 
 			//Step 3.
 			AttributeInterface resume = factory.createFileAttribute();
 			resume.setName("resume");
 			user.addAbstractAttribute(resume);
 
 			//Step 4.
 			savedEntity = entityManagerInterface.persistEntity(user);
 
 			//Step 5.
 			metadata = executeQueryForMetadata("select * from "
 					+ savedEntity.getTableProperties().getName());
 			assertEquals(metadata.getColumnCount(), noOfDefaultColumns + 1);
 
 		}
 		catch (DynamicExtensionsSystemException e)
 		{
 			fail();
 			Logger.out.debug(e.getStackTrace());
 		}
 		catch (DynamicExtensionsApplicationException e)
 		{
 			fail();
 			Logger.out.debug(e.getStackTrace());
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 			fail();
 
 			Logger.out.debug(e.getStackTrace());
 		}
 
 	}
 
 	/**
 	 * PURPOSE : to test the method GetMainContainer
 	 *            
 	 *  
 	 * EXPECTED BEHAVIOR : GetMainContainer method should return the main container given the entity group id 
 	 * 
 	 * TEST CASE FLOW : 
 	 * 1.Create an entity
 	 * 2.Create an entity Group
 	 * 3.Add entity in entity group
 	 * 4.Create an container
 	 * 5.set its entity to the created entity 
 	 * 6.Persist the container. 
 	 * 7. invoke getMainContainer
 	 * 8. test if retruned container is same or not
 	 */
 	public void testGetMainContainer()
 	{
 		EntityManagerInterface entityManagerInterface = EntityManager.getInstance();
 		DomainObjectFactory factory = DomainObjectFactory.getInstance();
 
 		try
 		{
 			//Step 1.
 			EntityInterface user = factory.createEntity();
 			user.setName("user");
 
 			//Step 2.
 			EntityGroupInterface userGroup = factory.createEntityGroup();
 			((EntityGroup) userGroup).setCurrent(true);
 
 			//Step 3.
 			userGroup.addEntity(user);
 			user.addEntityGroupInterface(userGroup);
 
 			//Step 4.
 			ContainerInterface userContainer = factory.createContainer();
 
 			userContainer.setCaption("User Container");
 
 			//Step 5.
 			userContainer.setEntity(user);
 
 			//Step 6.
 			entityManagerInterface.persistContainer(userContainer);
 
 			//Step 7.
 			Collection<NameValueBean> containerNameValueCollection = entityManagerInterface
 					.getMainContainer(userGroup.getId());
 
 			//Step 8.
 			assertEquals(containerNameValueCollection.size(), 1);
 			NameValueBean containerNameValue = containerNameValueCollection.iterator().next();
 
 			assertEquals(containerNameValue.getName(), userContainer.getCaption());
 			assertEquals(containerNameValue.getValue(), userContainer.getId().toString());
 
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 		}
 
 	}
 
 	/**
 	 * PURPOSE : to test the method GetMainContainer
 	 *            
 	 *  
 	 * EXPECTED BEHAVIOR : GetMainContainer method should return null when 
 	 *                     main entity within given is abstract 
 	 * 
 	 * TEST CASE FLOW : 
 	 * 1.Create an abstract entity
 	 * 2.Create an entity Group
 	 * 3.Add entity in entity group
 	 * 4.Create an container
 	 * 5.set its entity to the created entity 
 	 * 6.Persist the container. 
 	 * 7. invoke getMainContainer
 	 * 8. test if retruned container null or not
 	 */
 	public void testGetMainContainerForAbstractEntity()
 	{
 		EntityManagerInterface entityManagerInterface = EntityManager.getInstance();
 		DomainObjectFactory factory = DomainObjectFactory.getInstance();
 
 		try
 		{
 			//Step 1.
 			EntityInterface user = factory.createEntity();
 			user.setAbstract(true);
 			user.setName("user");
 
 			//Step 2.
 			EntityGroupInterface userGroup = factory.createEntityGroup();
 			((EntityGroup) userGroup).setCurrent(true);
 
 			//Step 3.
 			userGroup.addEntity(user);
 			user.addEntityGroupInterface(userGroup);
 
 			//Step 4.
 			ContainerInterface userContainer = factory.createContainer();
 			userContainer.setCaption("User Container");
 
 			//Step 5.
 			userContainer.setEntity(user);
 
 			//Step 6.
 			entityManagerInterface.persistContainer(userContainer);
 
 			//Step 7.
 			Collection<NameValueBean> containerNameValueCollection = entityManagerInterface
 					.getMainContainer(userGroup.getId());
 
 			//Step 8.
 			assertEquals(0, containerNameValueCollection.size());
 
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 		}
 
 	}
 
 	/**
 	 * 
 	 */
 	public void testInsertDataForDate()
 	{
 		//Step 1 
 		Entity entity = new Entity();
 		entity.setName("Stock Quote");
 		EntityManagerInterface entityManagerInterface = EntityManager.getInstance();
 
 		try
 		{
 
 			AttributeInterface floatAtribute = DomainObjectFactory.getInstance()
 					.createFloatAttribute();
 			floatAtribute.setName("Price");
 
 			AttributeInterface commentsAttributes = DomainObjectFactory.getInstance()
 					.createStringAttribute();
 			commentsAttributes.setName("coomments");
 
 			AttributeInterface startDate = DomainObjectFactory.getInstance().createDateAttribute();
 			startDate.setName("startDate");
 			((DateAttributeTypeInformation) startDate.getAttributeTypeInformation())
 					.setFormat(ProcessorConstants.DATE_TIME_FORMAT);
 
 			AttributeInterface endDate = DomainObjectFactory.getInstance().createDateAttribute();
 			endDate.setName("endDate");
 
 			entity.addAbstractAttribute(floatAtribute);
 			entity.addAbstractAttribute(commentsAttributes);
 			entity.addAbstractAttribute(startDate);
 			entity.addAbstractAttribute(endDate);
 
 			//Step 2 
 			EntityInterface savedEntity = entityManagerInterface.persistEntity(entity);
 
 			Map dataValue = new HashMap();
 			dataValue.put(floatAtribute, "15.90");
 			dataValue.put(startDate, "11-12-1982 10:11");
 			dataValue.put(endDate, "01-12-1982");
 
 			Long recordId = entityManagerInterface.insertData(savedEntity, dataValue);
 
 			dataValue = entityManagerInterface.getRecordById(entity, recordId);
 
 			assertEquals("11-12-1982 10:11", dataValue.get(startDate));
 			assertEquals("01-12-1982", dataValue.get(endDate));
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 			fail();
 		}
 
 	}
 
 	/**
 	 * PURPOSE : to test the basic delete functionality 
 	 *            
 	 *  
 	 * EXPECTED BEHAVIOR : delete method should disbale the record and not actually delete it.
 	 * 
 	 * TEST CASE FLOW : 
 	 * 1.Create an entity
 	 * 2.add two attributes
 	 * 3.persist entity
 	 * 4.add record.
 	 * 5. check the activity status - should be active
 	 * 6.delete that record 
 	 * 7. check the activity status - should be disable
 	 */
 	public void testDeleteRecordById1()
 	{
 
 		try
 		{
 			//step 1
 			EntityManagerInterface entityManagerInterface = EntityManager.getInstance();
 			DomainObjectFactory factory = DomainObjectFactory.getInstance();
 
 			Entity entity = (Entity) factory.createEntity();
 			entity.setName("Stock Quote");
 
 			AttributeInterface floatAtribute = factory.createFloatAttribute();
 			floatAtribute.setName("Price");
 
 			AttributeInterface commentsAttributes = factory.createStringAttribute();
 			commentsAttributes.setName("comments");
 
 			//step 2
 			entity.addAbstractAttribute(floatAtribute);
 			entity.addAbstractAttribute(commentsAttributes);
 			//step 3			
 			entity = (Entity) entityManagerInterface.persistEntity(entity);
 
 			//step 4
 			Map dataValue = new HashMap();
 			dataValue.put(floatAtribute, "122.34");
 			dataValue.put(commentsAttributes, "test1123");
 
 			entityManagerInterface.insertData(entity, dataValue);
 
 			//step 5
 			ResultSet resultSet = executeQuery("select count(*) from "
 					+ entity.getTableProperties().getName());
 			resultSet.next();
 			assertEquals(1, resultSet.getInt(1));
 
 			resultSet = executeQuery("select " + Constants.ACTIVITY_STATUS_COLUMN + " from "
 					+ entity.getTableProperties().getName());
 			resultSet.next();
 			assertEquals(Constants.ACTIVITY_STATUS_ACTIVE, resultSet.getString(1));
 
 			//step 6
 			boolean isRecordDeleted = EntityManager.getInstance().deleteRecord(entity, new Long(1));
 
 			//step 7
 			assertTrue(isRecordDeleted);
 			resultSet = executeQuery("select " + Constants.ACTIVITY_STATUS_COLUMN + " from "
 					+ entity.getTableProperties().getName());
 			resultSet.next();
 			assertEquals(Constants.ACTIVITY_STATUS_DISABLED, resultSet.getString(1));
 
 			resultSet = executeQuery("select count(*) from "
 					+ entity.getTableProperties().getName());
 			resultSet.next();
 			assertEquals(1, resultSet.getInt(1));
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 			Logger.out.debug(e.getMessage());
 			fail("Exception occured");
 		}
 
 	}
 
 	/**
 	 * PURPOSE : to test the behaviour of  GetAllRecords method for deleted records
 	 * EXPECTED BEHAVIOR : GetAllRecords method should filter the deleted records.
 	 * 
 	 * TEST CASE FLOW : 
 	 * 1.Create an entity
 	 * 2.add two attributes
 	 * 3.persist entity
 	 * 4.add 2 records.
 	 * 5. call  getAllRecords - should return 2 records
 	 * 6.delete 2nd  record 
 	 * 7. call  getAllRecords - should return 1 records
 	 */
 	public void testGetAllRecordsForDeletedRecords()
 	{
 
 		try
 		{
 			//step 1
 			EntityManagerInterface entityManagerInterface = EntityManager.getInstance();
 			DomainObjectFactory factory = DomainObjectFactory.getInstance();
 
 			Entity entity = (Entity) factory.createEntity();
 			entity.setName("Stock Quote");
 
 			AttributeInterface floatAtribute = factory.createFloatAttribute();
 			floatAtribute.setName("Price");
 
 			AttributeInterface commentsAttributes = factory.createStringAttribute();
 			commentsAttributes.setName("comments");
 
 			//step 2
 			entity.addAbstractAttribute(floatAtribute);
 			entity.addAbstractAttribute(commentsAttributes);
 			//step 3			
 			entity = (Entity) entityManagerInterface.persistEntity(entity);
 
 			//step 4
 			Map dataValue = new HashMap();
 			dataValue.put(floatAtribute, "1.1");
 			dataValue.put(commentsAttributes, "test1");
 
 			entityManagerInterface.insertData(entity, dataValue);
 
 			dataValue.put(floatAtribute, "1.2");
 			dataValue.put(commentsAttributes, "test2");
 			entityManagerInterface.insertData(entity, dataValue);
 
 			//step 5
 			List<EntityRecord> recordList = entityManagerInterface.getAllRecords(entity);
 			assertEquals(2, recordList.size());
 
 			//step 6
 			boolean isRecordDeleted = EntityManager.getInstance().deleteRecord(entity, new Long(2));
 
 			//step 7
 			assertTrue(isRecordDeleted);
 			recordList = entityManagerInterface.getAllRecords(entity);
 			assertEquals(1, recordList.size());
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 			Logger.out.debug(e.getMessage());
 			fail("Exception occured");
 		}
 
 	}
 
 	/**
 	 *  PURPOSE: This method test for deleting data for a containtment relationship between two entities
 	 *  EXPECTED BEHAVIOUR: Data of containted entity should also be disabled
 	 *   
 	 *  TEST CASE FLOW: 1. create User
 	 *                  2. Create Address                       
 	 *                  3. Add Association with      User(1) ------->(1) Address containtment association
 	 *                  4. persist entities.
 	 *                  5. Insert Data
 	 *                  6. Delete User record, address should also be deleted.
 	 */
 	public void testDeleteDataForContaintmentOneToOne()
 	{
 
 		EntityManagerInterface entityManagerInterface = EntityManager.getInstance();
 		DomainObjectFactory factory = DomainObjectFactory.getInstance();
 
 		try
 		{
 
 			// Step 1  
 			EntityInterface user = factory.createEntity();
 			AttributeInterface userNameAttribute = factory.createStringAttribute();
 			userNameAttribute.setName("user name");
 			user.setName("user");
 			user.addAbstractAttribute(userNameAttribute);
 
 			// Step 2  
 			EntityInterface address = factory.createEntity();
 			address.setName("address");
 
 			AttributeInterface streetAttribute = factory.createStringAttribute();
 			streetAttribute.setName("street name");
 			address.addAbstractAttribute(streetAttribute);
 
 			AttributeInterface cityAttribute = factory.createStringAttribute();
 			cityAttribute.setName("city name");
 			address.addAbstractAttribute(cityAttribute);
 
 			// Step 3
 			AssociationInterface association = factory.createAssociation();
 			association.setTargetEntity(address);
 			association.setAssociationDirection(AssociationDirection.SRC_DESTINATION);
 			association.setName("UserAddress");
 			association.setSourceRole(getRole(AssociationType.CONTAINTMENT, "User",
 					Cardinality.ZERO, Cardinality.ONE));
 			association.setTargetRole(getRole(AssociationType.ASSOCIATION, "address",
 					Cardinality.ZERO, Cardinality.ONE));
 
 			user.addAbstractAttribute(association);
 
 			// Step 4
 			EntityInterface savedEntity = entityManagerInterface.persistEntity(user);
 
 			Map dataValue = new HashMap();
 			Map addressDataValue = new HashMap();
 			addressDataValue.put(streetAttribute, "Laxmi Road");
 			addressDataValue.put(cityAttribute, "Pune");
 
 			List<Map> addressDataValueMapList = new ArrayList<Map>();
 			addressDataValueMapList.add(addressDataValue);
 
 			dataValue.put(userNameAttribute, "rahul");
 			dataValue.put(association, addressDataValueMapList);
 
 			// Step 5
 			entityManagerInterface.insertData(savedEntity, dataValue);
 
 			System.out.println("show sql: " + System.getProperty("show_sql"));
 
 			// Step 6
 
 			assertEquals(Constants.ACTIVITY_STATUS_ACTIVE, getActivityStatus(user, 1L));
 			assertEquals(Constants.ACTIVITY_STATUS_ACTIVE, getActivityStatus(address, 1L));
 
 			entityManagerInterface.deleteRecord(savedEntity, 1L);
 
 			assertEquals(Constants.ACTIVITY_STATUS_DISABLED, getActivityStatus(user, 1L));
 			assertEquals(Constants.ACTIVITY_STATUS_DISABLED, getActivityStatus(address, 1L));
 
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 			Logger.out.debug(DynamicExtensionsUtility.getStackTrace(e));
 			fail();
 		}
 
 	}
 
 	/**
 	 *  PURPOSE: This method test for deleting data for a containtment relationship between two entities having one to many asso.
 	 *  EXPECTED BEHAVIOUR: Data of containted entity should also be disabled
 	 *   
 	 *  TEST CASE FLOW: 1. create User
 	 *                  2. Create Address                       
 	 *                  3. Add Association with      User(1) ------->(*) Address containtment association
 	 *                  4. persist entities.
 	 *                  5. Insert Data
 	 *                  6. Delete User record, all associated addresses should also be deleted.
 	 */
 	public void testDeleteDataForContaintmentOneToMany()
 	{
 
 		EntityManagerInterface entityManagerInterface = EntityManager.getInstance();
 		DomainObjectFactory factory = DomainObjectFactory.getInstance();
 
 		try
 		{
 
 			// Step 1  
 			EntityInterface user = factory.createEntity();
 			AttributeInterface userNameAttribute = factory.createStringAttribute();
 			userNameAttribute.setName("user name");
 			user.setName("user");
 			user.addAbstractAttribute(userNameAttribute);
 
 			// Step 2  
 			EntityInterface address = factory.createEntity();
 			address.setName("address");
 
 			AttributeInterface streetAttribute = factory.createStringAttribute();
 			streetAttribute.setName("street name");
 			address.addAbstractAttribute(streetAttribute);
 
 			AttributeInterface cityAttribute = factory.createStringAttribute();
 			cityAttribute.setName("city name");
 			address.addAbstractAttribute(cityAttribute);
 
 			// Step 3
 			AssociationInterface association = factory.createAssociation();
 			association.setTargetEntity(address);
 			association.setAssociationDirection(AssociationDirection.SRC_DESTINATION);
 			association.setName("UserAddress");
 			association.setSourceRole(getRole(AssociationType.CONTAINTMENT, "User",
 					Cardinality.ZERO, Cardinality.ONE));
 			association.setTargetRole(getRole(AssociationType.ASSOCIATION, "address",
 					Cardinality.ZERO, Cardinality.MANY));
 
 			user.addAbstractAttribute(association);
 
 			// Step 4
 			EntityInterface savedEntity = entityManagerInterface.persistEntity(user);
 
 			Map dataValue = new HashMap();
 			Map officeAddress = new HashMap();
 			officeAddress.put(streetAttribute, "Laxmi Road");
 			officeAddress.put(cityAttribute, "Pune");
 
 			Map homeAddress = new HashMap();
 			homeAddress.put(streetAttribute, "Baner Road");
 			homeAddress.put(cityAttribute, "Pune");
 
 			List<Map> addressDataValueMapList = new ArrayList<Map>();
 			addressDataValueMapList.add(officeAddress);
 			addressDataValueMapList.add(homeAddress);
 
 			dataValue.put(userNameAttribute, "rahul");
 			dataValue.put(association, addressDataValueMapList);
 
 			// Step 5
 			entityManagerInterface.insertData(savedEntity, dataValue);
 
 			assertEquals(Constants.ACTIVITY_STATUS_ACTIVE, getActivityStatus(user, 1L));
 			assertEquals(Constants.ACTIVITY_STATUS_ACTIVE, getActivityStatus(address, 1L));
 			assertEquals(Constants.ACTIVITY_STATUS_ACTIVE, getActivityStatus(address, 2L));
 
 			// Step 6	
 			entityManagerInterface.deleteRecord(savedEntity, 1L);
 
 			assertEquals(Constants.ACTIVITY_STATUS_DISABLED, getActivityStatus(user, 1L));
 			assertEquals(Constants.ACTIVITY_STATUS_DISABLED, getActivityStatus(address, 1L));
 			assertEquals(Constants.ACTIVITY_STATUS_DISABLED, getActivityStatus(address, 2L));
 
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 			Logger.out.debug(DynamicExtensionsUtility.getStackTrace(e));
 			fail();
 		}
 
 	}
 
 	/** PURPOSE: This method test for deleting data for a one to many lookup association relationship between two entities.
 	 *  EXPECTED BEHAVIOUR: Data of looked up entity should not be disabled
 	 *   
 	 *  TEST CASE FLOW: 1. create User
 	 *                  2. Create Study                       
 	 *                  3. Add Association with      User(1) ------->(*) Study lookup association
 	 *                  4. persist entities.
 	 *                  5. Insert Data
 	 *                  6. Delete User record,  associated study should NOT be deleted.
 	 */
 
 	public void testDeleteDataForAssociationOne2Many()
 	{
 
 		EntityManagerInterface entityManagerInterface = EntityManager.getInstance();
 		DomainObjectFactory factory = DomainObjectFactory.getInstance();
 
 		try
 		{
 
 			//create user 
 			EntityInterface user = factory.createEntity();
 			AttributeInterface userNameAttribute = factory.createStringAttribute();
 			userNameAttribute.setName("user name");
 			user.setName("user");
 			user.addAbstractAttribute(userNameAttribute);
 
 			//create study 
 			EntityInterface study = factory.createEntity();
 			AttributeInterface studyNameAttribute = factory.createStringAttribute();
 			studyNameAttribute.setName("study name");
 			study.setName("study");
 			study.addAbstractAttribute(studyNameAttribute);
 
 			//Associate user (1)------ >(*)study       
 			AssociationInterface association = factory.createAssociation();
 			association.setTargetEntity(study);
 			association.setAssociationDirection(AssociationDirection.SRC_DESTINATION);
 			association.setName("primaryInvestigator");
 			association.setSourceRole(getRole(AssociationType.ASSOCIATION, "primaryInvestigator",
 					Cardinality.ZERO, Cardinality.ONE));
 			association.setTargetRole(getRole(AssociationType.ASSOCIATION, "study",
 					Cardinality.ZERO, Cardinality.MANY));
 
 			user.addAbstractAttribute(association);
 
 			//persist entity
 			EntityInterface savedEntity = entityManagerInterface.persistEntity(user);
 
 			//Insert Data
 			Map dataValue = new HashMap();
 			dataValue.put(studyNameAttribute, "study");
 			entityManagerInterface.insertData(study, dataValue);
 
 			dataValue.clear();
 			dataValue.put(studyNameAttribute, "study1");
 			entityManagerInterface.insertData(study, dataValue);
 
 			dataValue.clear();
 			List<Long> targetIdList = new ArrayList<Long>();
 			targetIdList.add(1L);
 			targetIdList.add(2L);
 
 			dataValue.put(userNameAttribute, "rahul");
 			dataValue.put(association, targetIdList);
 
 			entityManagerInterface.insertData(savedEntity, dataValue);
 
 			assertEquals(Constants.ACTIVITY_STATUS_ACTIVE, getActivityStatus(user, 1L));
 			assertEquals(Constants.ACTIVITY_STATUS_ACTIVE, getActivityStatus(study, 1L));
 			assertEquals(Constants.ACTIVITY_STATUS_ACTIVE, getActivityStatus(study, 2L));
 
 			//Step 6:
 			entityManagerInterface.deleteRecord(savedEntity, 1L);
 
 			assertEquals(Constants.ACTIVITY_STATUS_DISABLED, getActivityStatus(user, 1L));
 
 			assertEquals(Constants.ACTIVITY_STATUS_ACTIVE, getActivityStatus(study, 1L));
 			assertEquals(Constants.ACTIVITY_STATUS_ACTIVE, getActivityStatus(study, 2L));
 		}
 		catch (Exception e)
 		{
 			Logger.out.debug(DynamicExtensionsUtility.getStackTrace(e));
 			fail();
 		}
 	}
 
 	/** PURPOSE: This method test for deleting data of one entity which is refered by a record of some 
 	 *           other entity.
 	 *  EXPECTED BEHAVIOUR: In such a cases, record is not allowed to be deleted, approprate applicationException should be thrown.
 	 *   
 	 *  TEST CASE FLOW: 1. create User
 	 *                  2. Create Study                       
 	 *                  3. Add Association with      User(1) ------->(*) Study lookup association
 	 *                  4. persist entities.
 	 *                  5. Insert Data for study as "study"
 	 *                  6. Insert Data for study as  "study1"
 	 *                  7. Delete "study1" shoud be succesful. 
 	 *                  8. Insert Data for user  as "user" that refers to "study"
 	 *                  9. Delete "Study" : should throw exception as it is refered by "user"
 	 * @throws Exception 
 	 */
 
 	public void testDeleteReferedData() throws Exception
 	{
 
 		EntityManagerInterface entityManagerInterface = EntityManager.getInstance();
 		DomainObjectFactory factory = DomainObjectFactory.getInstance();
 		EntityInterface study = null;
 		try
 		{
 
 			//Step 1
 			EntityInterface user = factory.createEntity();
 			AttributeInterface userNameAttribute = factory.createStringAttribute();
 			userNameAttribute.setName("user name");
 			user.setName("user");
 			user.addAbstractAttribute(userNameAttribute);
 
 			//Step 2
 			study = factory.createEntity();
 			AttributeInterface studyNameAttribute = factory.createStringAttribute();
 			studyNameAttribute.setName("study name");
 			study.setName("study");
 			study.addAbstractAttribute(studyNameAttribute);
 
 			//Step 3 
 			AssociationInterface association = factory.createAssociation();
 			association.setTargetEntity(study);
 			association.setAssociationDirection(AssociationDirection.SRC_DESTINATION);
 			association.setName("primaryInvestigator");
 			association.setSourceRole(getRole(AssociationType.ASSOCIATION, "primaryInvestigator",
 					Cardinality.ZERO, Cardinality.MANY));
 			association.setTargetRole(getRole(AssociationType.ASSOCIATION, "study",
 					Cardinality.ZERO, Cardinality.ONE));
 
 			user.addAbstractAttribute(association);
 
 			//Step 4 
 			EntityInterface savedEntity = entityManagerInterface.persistEntity(user);
 
 			//Step 5 
 			Map dataValue = new HashMap();
 			dataValue.put(studyNameAttribute, "study");
 			entityManagerInterface.insertData(study, dataValue);
 
 			//Step 6			
 			dataValue.clear();
 			dataValue.put(studyNameAttribute, "study1");
 			entityManagerInterface.insertData(study, dataValue);
 
 			//Step 7
 			entityManagerInterface.deleteRecord(study, 1L);
 			assertTrue(true);
 
 			assertEquals(Constants.ACTIVITY_STATUS_ACTIVE, getActivityStatus(study, 2L));
 			assertEquals(Constants.ACTIVITY_STATUS_DISABLED, getActivityStatus(study, 1L));
 
 			//Step 8
 			dataValue.clear();
 			List<Long> targetIdList = new ArrayList<Long>();
 			targetIdList.add(2L);
 
 			dataValue.put(userNameAttribute, "rahul");
 			dataValue.put(association, targetIdList);
 
 			entityManagerInterface.insertData(savedEntity, dataValue);
 
 			//Step 9
 			entityManagerInterface.deleteRecord(study, 2L);
 
 			fail();
 		}
 		catch (DynamicExtensionsApplicationException e)
 		{
 			assertTrue(true);
 			assertEquals(Constants.ACTIVITY_STATUS_ACTIVE, getActivityStatus(study, 2L));
 			Logger.out.debug(DynamicExtensionsUtility.getStackTrace(e));
 		}
 		catch (Exception e)
 		{
 			Logger.out.debug(DynamicExtensionsUtility.getStackTrace(e));
 			fail();
 		}
 	}
 
 }
