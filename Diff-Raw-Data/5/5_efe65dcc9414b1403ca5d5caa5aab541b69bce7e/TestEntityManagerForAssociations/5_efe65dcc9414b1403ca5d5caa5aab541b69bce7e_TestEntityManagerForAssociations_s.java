 /**
  *<p>Description:  </p>
  *<p>Copyright://TODO</p>
  *@author Vishvesh Mulay
  *@version 1.0
  */
 
 package edu.common.dynamicextensions.entitymanager;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 
 import edu.common.dynamicextensions.domain.Association;
 import edu.common.dynamicextensions.domain.Attribute;
 import edu.common.dynamicextensions.domain.DomainObjectFactory;
 import edu.common.dynamicextensions.domain.Entity;
 import edu.common.dynamicextensions.domain.userinterface.Container;
 import edu.common.dynamicextensions.domaininterface.AssociationInterface;
 import edu.common.dynamicextensions.domaininterface.AttributeInterface;
 import edu.common.dynamicextensions.domaininterface.EntityGroupInterface;
 import edu.common.dynamicextensions.domaininterface.EntityInterface;
 import edu.common.dynamicextensions.domaininterface.RoleInterface;
 import edu.common.dynamicextensions.domaininterface.userinterface.ContainerInterface;
 import edu.common.dynamicextensions.exception.DynamicExtensionsApplicationException;
 import edu.common.dynamicextensions.exception.DynamicExtensionsSystemException;
 import edu.common.dynamicextensions.util.DynamicExtensionsBaseTestCase;
 import edu.common.dynamicextensions.util.DynamicExtensionsUtility;
 import edu.common.dynamicextensions.util.global.DEConstants.AssociationDirection;
 import edu.common.dynamicextensions.util.global.DEConstants.AssociationType;
 import edu.common.dynamicextensions.util.global.DEConstants.Cardinality;
 import edu.wustl.common.util.global.Status;
 import edu.wustl.common.util.logger.Logger;
 
 public class TestEntityManagerForAssociations extends DynamicExtensionsBaseTestCase
 {
 
 	/**
 	 *
 	 */
 	public TestEntityManagerForAssociations()
 	{
 		super();
 		//TODO Auto-generated constructor stub
 	}
 
 	/**
 	 * @param arg0 name
 	 */
 	public TestEntityManagerForAssociations(String arg0)
 	{
 		super(arg0);
 		//TODO Auto-generated constructor stub
 	}
 
 	/**
 	 * @see edu.common.dynamicextensions.util.DynamicExtensionsBaseTestCaseUtility#setUp()
 	 */
 	protected void setUp()
 	{
 		super.setUp();
 	}
 
 	/**
 	 * @see edu.common.dynamicextensions.util.DynamicExtensionsBaseTestCaseUtility#tearDown()
 	 */
 	protected void tearDown()
 	{
 		super.tearDown();
 	}
 
 	/**
 	 * This test case test for associating two entities with one to many association
 	 *
 	 * for oracle it should throw exception.
 	 * for mysql  it works.
 	 */
 	public void testCreateEntityWithOneToManyAssociation()
 	{
 		EntityGroupManagerInterface entityGroupManager = EntityGroupManager.getInstance();
 		DomainObjectFactory factory = DomainObjectFactory.getInstance();
 
 		// create user
 		EntityGroupInterface entityGroup = DomainObjectFactory.getInstance().createEntityGroup();
 		entityGroup.setName("User" + new Double(Math.random()).toString());
 		EntityInterface user = createAndPopulateEntity();
 		AttributeInterface userNameAttribute = factory.createStringAttribute();
 		userNameAttribute.setName("user_name");
 		user.setName("user");
 		user.addAbstractAttribute(userNameAttribute);
 
 		// create study
 		EntityInterface study = createAndPopulateEntity();
 
 		Container container = new Container();
 		container.setCaption("testcontainer");
 		Collection<ContainerInterface> listOfContainers = new HashSet<ContainerInterface>();
 		listOfContainers.add(container);
 
 		study.setContainerCollection(listOfContainers);
 		try
 		{
 			AttributeInterface studyNameAttribute = factory.createStringAttribute();
 			studyNameAttribute.setName("study_name");
 			study.setName("study");
 			study.addAbstractAttribute(studyNameAttribute);
 
 			// Associate user (1)------ >(*)study
 			AssociationInterface association = factory.createAssociation();
 
 			association.setTargetEntity(study);
 			association.setAssociationDirection(AssociationDirection.SRC_DESTINATION);
 			association.setName("primaryInvestigator");
 			association.setSourceRole(getRole(AssociationType.ASSOCIATION, "primaryInvestigator",
 					Cardinality.ONE, Cardinality.ONE));
 			association.setTargetRole(getRole(AssociationType.ASSOCIATION, "study",
 					Cardinality.ZERO, Cardinality.MANY));
 
 			user.addAbstractAttribute(association);
 			DynamicExtensionsUtility.getConstraintPropertiesForAssociation(association);
 
 			//entityManager.createEntity(study);
 			entityGroup.addEntity(user);
 			user.setEntityGroup(entityGroup);
 			entityGroup.addEntity(study);
 			study.setEntityGroup(entityGroup);
 			EntityManager m1 = new EntityManager();
 
 			EntityGroupInterface savedUser = entityGroupManager.persistEntityGroup(entityGroup);
 
 			Collection<Container> coll = study.getContainerCollection();
 
 			long id = 0;
 			for (ContainerInterface cont : coll)
 			{
 				id = cont.getId();
 			}
 			assertEquals(getColumnCount("select * from " + study.getTableProperties().getName()),
 					noOfDefaultColumns + 2);
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
 
 	}
 
 	/**
 	 * This test case test for associating two entities with one to many association
 	 *
 	 * for oracle it should throw exception.
 	 * for mysql  it works.
 	 */
 	public void testCreateEntityWithManyToOneAssociation()
 	{
 		EntityGroupManagerInterface entityGroupManager = EntityGroupManager.getInstance();
 		DomainObjectFactory factory = DomainObjectFactory.getInstance();
 
 		// create user
 		EntityGroupInterface entityGroupInterface = factory.getInstance().createEntityGroup();
 		EntityInterface user = createAndPopulateEntity();
 		AttributeInterface userNameAttribute = factory.createStringAttribute();
 		userNameAttribute.setName("user name");
 		user.setName("user");
 		user.addAbstractAttribute(userNameAttribute);
 
 		// create study
 		EntityInterface study = createAndPopulateEntity();
 		AttributeInterface studyNameAttribute = factory.createStringAttribute();
 		studyNameAttribute.setName("study name");
 		study.setName("study");
 		study.addAbstractAttribute(studyNameAttribute);
 
 		// Associate user (1)------ >(*)study
 		AssociationInterface association = factory.createAssociation();
 
 		try
 		{
 			association.setTargetEntity(study);
 			association.setAssociationDirection(AssociationDirection.SRC_DESTINATION);
 			association.setName("primaryInvestigator");
 			association.setSourceRole(getRole(AssociationType.ASSOCIATION, "primaryInvestigator",
 					Cardinality.ZERO, Cardinality.MANY));
 			association.setTargetRole(getRole(AssociationType.ASSOCIATION, "study",
 					Cardinality.ZERO, Cardinality.ONE));
 
 			user.addAbstractAttribute(association);
 			DynamicExtensionsUtility.getConstraintPropertiesForAssociation(association);
 
 			//entityManager.createEntity(study);
 
 			entityGroupInterface.getEntityCollection().add(user);
 			EntityGroupInterface savedGroup = (EntityGroupInterface) entityGroupManager
 					.persistEntityGroup(entityGroupInterface);
 			assertEquals(getColumnCount("select * from " + user.getTableProperties().getName()),
 					noOfDefaultColumns + 2);
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
 
 	}
 
 	/**
 	 * This test case tries to modify data type of the attribute,when data is present for that column.
 	 * for oracle it should throw exception.
 	 * for mysql  it works.
 	 */
 	public void testCreateEntityWithAssociationWithUnsavedTargetEntity()
 	{
 		try
 		{
 			DomainObjectFactory factory = DomainObjectFactory.getInstance();
 			EntityGroupInterface entityGroup = factory.createEntityGroup();
 			entityGroup.setName("test_" + new Double(Math.random()).toString());
 
 			Entity srcEntity = (Entity) new MockEntityManager().initializeEntity(entityGroup);
 			srcEntity.setName("study");
 			//Entity savedSrcEntity = (Entity) EntityManager.getInstance().createEntity(srcEntity);
 			Entity targetEntity = (Entity) new MockEntityManager().initializeEntity(entityGroup);
 			targetEntity.setName("user");
 			//Entity savedTargetEntity = (Entity) EntityManager.getInstance().createEntity(targetEntity);
 			Association association = (Association) DomainObjectFactory.getInstance()
 					.createAssociation();
 			association.setEntity(srcEntity);
 			association.setTargetEntity(targetEntity);
 			association.setAssociationDirection(AssociationDirection.SRC_DESTINATION);
 			association.setName("primaryInvestigator");
 			association.setSourceRole(getRole(AssociationType.ASSOCIATION, "study",
 					Cardinality.ONE, Cardinality.ONE));
 			association.setTargetRole(getRole(AssociationType.ASSOCIATION, "primaryInvestigator",
 					Cardinality.ONE, Cardinality.ONE));
 			DynamicExtensionsUtility.getConstraintPropertiesForAssociation(association);
 			srcEntity.addAbstractAttribute(association);
 			// association.sets
 
 			EntityManager.getInstance().persistEntity(srcEntity);
 
 		}
 		catch (Exception e)
 		{
 			// TODO Auto-generated catch block
 			fail();
 			e.printStackTrace();
 		}
 
 	}
 
 	/**
 	 * This test case test for associating two entities with many to many association  and direction is src_destination.
 	 *
 	 * for oracle it should throw exception.
 	 * for mysql  it works.
 	 */
 	public void testCreateEntityWithManyToManyAssociation()
 	{
 		EntityManagerInterface entityManager = EntityManager.getInstance();
 		DomainObjectFactory factory = DomainObjectFactory.getInstance();
 		EntityGroupInterface entityGroup = factory.createEntityGroup();
 		entityGroup.setName("test_" + new Double(Math.random()).toString());
 
 		// create user
 		EntityInterface user = createAndPopulateEntity();
 		AttributeInterface userNameAttribute = factory.createStringAttribute();
 		userNameAttribute.setName("user name");
 		user.setName("user");
 		user.addAbstractAttribute(userNameAttribute);
 
 		// create study
 		EntityInterface study = createAndPopulateEntity();
 		AttributeInterface studyNameAttribute = factory.createStringAttribute();
 		studyNameAttribute.setName("study name");
 		study.setName("study");
 		study.addAbstractAttribute(studyNameAttribute);
 
 		// Associate user (1)------ >(*)study
 		AssociationInterface association = factory.createAssociation();
 		try
 		{
 			association.setTargetEntity(study);
 			association.setAssociationDirection(AssociationDirection.SRC_DESTINATION);
 			association.setName("primaryInvestigator");
 			association.setSourceRole(getRole(AssociationType.ASSOCIATION, "primaryInvestigator",
 					Cardinality.ONE, Cardinality.MANY));
 			association.setTargetRole(getRole(AssociationType.ASSOCIATION, "study",
 					Cardinality.ZERO, Cardinality.MANY));
 
 			user.addAbstractAttribute(association);
 			DynamicExtensionsUtility.getConstraintPropertiesForAssociation(association);
 			entityGroup.addEntity(user);
 			user.setEntityGroup(entityGroup);
 			entityGroup.addEntity(study);
 			study.setEntityGroup(entityGroup);
 
 			EntityInterface savedUser = entityManager.persistEntity(user);
 
 			String middleTableName = association.getConstraintProperties().getName();
 
 			assertNotNull(middleTableName);
 
 			assertEquals(getColumnCount("select * from " + middleTableName), noOfDefaultColumns + 1);
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
 
 	}
 
 	/**
 	 * Purpose is to test the self referencing of the entity.
 	 * Scenario - user(*)------>(1)User
 	 *                   creator
 	 */
 	public void testEditEntityWithSelfReferencingBiDirectionalManyToManyAssociation()
 	{
 		EntityManagerInterface entityManager = EntityManager.getInstance();
 		DomainObjectFactory factory = DomainObjectFactory.getInstance();
 		EntityGroupInterface entityGroup = factory.createEntityGroup();
 		entityGroup.setName("test_" + new Double(Math.random()).toString());
 
 		// create user
 		EntityInterface user = createAndPopulateEntity();
 		AttributeInterface userNameAttribute = factory.createStringAttribute();
 		userNameAttribute.setName("user name");
 		user.setName("user");
 		user.addAbstractAttribute(userNameAttribute);
 		entityGroup.addEntity(user);
 		user.setEntityGroup(entityGroup);
 		try
 		{
 			user = entityManager.persistEntity(user);
 
 			// Associate user (*)------ >(1)user
 			AssociationInterface association = factory.createAssociation();
 			association.setName("testassociation");
 			association.setEntity(user);
 			association.setTargetEntity(user);
 			association.setAssociationDirection(AssociationDirection.BI_DIRECTIONAL);
 			association.setName("primaryInvestigator");
 			association.setSourceRole(getRole(AssociationType.ASSOCIATION, "subpi",
 					Cardinality.ONE, Cardinality.ONE));
 			association.setTargetRole(getRole(AssociationType.ASSOCIATION, "pi", Cardinality.ONE,
 					Cardinality.MANY));
 
 			user.addAbstractAttribute(association);
 			DynamicExtensionsUtility.getConstraintPropertiesForAssociation(association);
 
 			EntityInterface savedUser = entityManager.persistEntity(user);
 
 			String tableName = user.getTableProperties().getName();
 
 			assertNotNull(tableName);
 
 			assertEquals(getColumnCount("select * from " + tableName), noOfDefaultColumns + 2);
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
 
 	}
 
 	/**
 	 * This method test for inserting data for a multi select attribute
 	 */
 	public void testInsertDataForAssociationMany2Many()
 	{
 
 		EntityManagerInterface entityManagerInterface = EntityManager.getInstance();
 		DomainObjectFactory factory = DomainObjectFactory.getInstance();
 		EntityGroupInterface entityGroup = factory.createEntityGroup();
 		entityGroup.setName("test_" + new Double(Math.random()).toString());
 		try
 		{
 
 			//          create user
 			EntityInterface user = createAndPopulateEntity();
 			AttributeInterface userNameAttribute = factory.createStringAttribute();
 			userNameAttribute.setName("user name");
 			user.setName("user");
 			user.addAbstractAttribute(userNameAttribute);
 
 			//          create study
 			EntityInterface study = createAndPopulateEntity();
 			AttributeInterface studyNameAttribute = factory.createStringAttribute();
 			studyNameAttribute.setName("study name");
 			study.setName("study");
 			study.addAbstractAttribute(studyNameAttribute);
 
 			//          Associate user (1)------ >(*)study
 			AssociationInterface association = factory.createAssociation();
 			association.setTargetEntity(study);
 			association.setAssociationDirection(AssociationDirection.SRC_DESTINATION);
 			association.setName("primaryInvestigator");
 			association.setSourceRole(getRole(AssociationType.ASSOCIATION, "primaryInvestigator",
 					Cardinality.ZERO, Cardinality.MANY));
 			association.setTargetRole(getRole(AssociationType.ASSOCIATION, "study",
 					Cardinality.ZERO, Cardinality.MANY));
 
 			user.addAbstractAttribute(association);
 			DynamicExtensionsUtility.getConstraintPropertiesForAssociation(association);
 			entityGroup.addEntity(user);
 			user.setEntityGroup(entityGroup);
 			entityGroup.addEntity(study);
 			study.setEntityGroup(entityGroup);
 
 			EntityInterface savedEntity = entityManagerInterface.persistEntity(user);
 
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
 
 			dataValue.clear();
 			dataValue.put(userNameAttribute, "vishvesh");
 			dataValue.put(association, targetIdList);
 			entityManagerInterface.insertData(savedEntity, dataValue);
 
 			int rowCount = (Integer) executeQuery("select count(*) from "
 					+ association.getConstraintProperties().getName(), INT_TYPE, 1);
 			assertEquals(4, rowCount);
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
 	 * This method test for inserting data for a multi select attribute
 	 */
 	public void testInsertDataForAssociationOne2Many()
 	{
 
 		EntityManagerInterface entityManagerInterface = EntityManager.getInstance();
 		DomainObjectFactory factory = DomainObjectFactory.getInstance();
 		EntityGroupInterface entityGroup = factory.createEntityGroup();
 		entityGroup.setName("test_" + new Double(Math.random()).toString());
 		try
 		{
 
 			//          create user
 			EntityInterface user = createAndPopulateEntity();
 			AttributeInterface userNameAttribute = factory.createStringAttribute();
 			userNameAttribute.setName("user name");
 			user.setName("user");
 			user.addAbstractAttribute(userNameAttribute);
 
 			//          create study
 			EntityInterface study = createAndPopulateEntity();
 			AttributeInterface studyNameAttribute = factory.createStringAttribute();
 			studyNameAttribute.setName("study name");
 			study.setName("study");
 			study.addAbstractAttribute(studyNameAttribute);
 
 			//          Associate user (1)------ >(*)study
 			AssociationInterface association = factory.createAssociation();
 			association.setTargetEntity(study);
 			association.setAssociationDirection(AssociationDirection.SRC_DESTINATION);
 			association.setName("primaryInvestigator");
 			association.setSourceRole(getRole(AssociationType.ASSOCIATION, "primaryInvestigator",
 					Cardinality.ZERO, Cardinality.ONE));
 			association.setTargetRole(getRole(AssociationType.ASSOCIATION, "study",
 					Cardinality.ZERO, Cardinality.MANY));
 
 			user.addAbstractAttribute(association);
 			DynamicExtensionsUtility.getConstraintPropertiesForAssociation(association);
 			entityGroup.addEntity(user);
 			user.setEntityGroup(entityGroup);
 			entityGroup.addEntity(study);
 			study.setEntityGroup(entityGroup);
 			EntityInterface savedEntity = entityManagerInterface.persistEntity(user);
 
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
 
 			int rowCount = (Integer) executeQuery("select count(*) from "
 					+ study.getTableProperties().getName(), INT_TYPE, 1);
 			assertEquals(2, rowCount);
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
 
 	//
 	/**
 	 * This method test for inserting data for a multi select attribute
 	 */
 	public void testInsertDataForAssociationMany2One()
 	{
 
 		EntityManagerInterface entityManagerInterface = EntityManager.getInstance();
 		DomainObjectFactory factory = DomainObjectFactory.getInstance();
 		EntityGroupInterface entityGroup = factory.createEntityGroup();
 		entityGroup.setName("test_" + new Double(Math.random()).toString());
 		try
 		{
 
 			//          create user
 			EntityInterface user = createAndPopulateEntity();
 			AttributeInterface userNameAttribute = factory.createStringAttribute();
 			userNameAttribute.setName("user name");
 			user.setName("user");
 			user.addAbstractAttribute(userNameAttribute);
 
 			//          create study
 			EntityInterface study = createAndPopulateEntity();
 			AttributeInterface studyNameAttribute = factory.createStringAttribute();
 			studyNameAttribute.setName("study name");
 			study.setName("study");
 			study.addAbstractAttribute(studyNameAttribute);
 
 			//          Associate user (1)------ >(*)study
 			AssociationInterface association = factory.createAssociation();
 			association.setTargetEntity(study);
 			association.setAssociationDirection(AssociationDirection.SRC_DESTINATION);
 			association.setName("primaryInvestigator");
 			association.setSourceRole(getRole(AssociationType.ASSOCIATION, "primaryInvestigator",
 					Cardinality.ZERO, Cardinality.MANY));
 			association.setTargetRole(getRole(AssociationType.ASSOCIATION, "study",
 					Cardinality.ZERO, Cardinality.ONE));
 
 			user.addAbstractAttribute(association);
 			DynamicExtensionsUtility.getConstraintPropertiesForAssociation(association);
 			entityGroup.addEntity(user);
 			user.setEntityGroup(entityGroup);
 			entityGroup.addEntity(study);
 			study.setEntityGroup(entityGroup);
 
 			EntityInterface savedEntity = entityManagerInterface.persistEntity(user);
 
 			Map dataValue = new HashMap();
 			List<Long> targetIdList = new ArrayList<Long>();
 			targetIdList.add(1L);
 			dataValue.put(userNameAttribute, "rahul");
 
 			dataValue.put(association, targetIdList);
 
 			entityManagerInterface.insertData(savedEntity, dataValue);
 
 			int rowCount = (Integer) executeQuery("select IDENTIFIER from "
 					+ savedEntity.getTableProperties().getName(), INT_TYPE, 1);
 			assertEquals(1, rowCount);
 
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
 	 * This method test for inserting data for a multi select attribute
 	 */
 	public void testEditDataForAssociationMany2One()
 	{
 
 		EntityManagerInterface entityManagerInterface = EntityManager.getInstance();
 		DomainObjectFactory factory = DomainObjectFactory.getInstance();
 		EntityGroupInterface entityGroup = factory.createEntityGroup();
 		entityGroup.setName("test_" + new Double(Math.random()).toString());
 		try
 		{
 
 			//          create user
 			EntityInterface user = createAndPopulateEntity();
 			AttributeInterface userNameAttribute = factory.createStringAttribute();
 			userNameAttribute.setName("user name");
 			user.setName("user");
 			user.addAbstractAttribute(userNameAttribute);
 
 			//          create study
 			EntityInterface study = createAndPopulateEntity();
 			AttributeInterface studyNameAttribute = factory.createStringAttribute();
 			studyNameAttribute.setName("study name");
 			study.setName("study");
 			study.addAbstractAttribute(studyNameAttribute);
 
 			//          Associate user (1)------ >(*)study
 			AssociationInterface association = factory.createAssociation();
 			association.setTargetEntity(study);
 			association.setAssociationDirection(AssociationDirection.SRC_DESTINATION);
 			association.setName("primaryInvestigator");
 			association.setSourceRole(getRole(AssociationType.ASSOCIATION, "primaryInvestigator",
 					Cardinality.ZERO, Cardinality.MANY));
 			association.setTargetRole(getRole(AssociationType.ASSOCIATION, "study",
 					Cardinality.ZERO, Cardinality.ONE));
 
 			user.addAbstractAttribute(association);
 			DynamicExtensionsUtility.getConstraintPropertiesForAssociation(association);
 			entityGroup.addEntity(user);
 			user.setEntityGroup(entityGroup);
 			entityGroup.addEntity(study);
 			study.setEntityGroup(entityGroup);
 
 			EntityInterface savedEntity = entityManagerInterface.persistEntity(user);
 
 			Map dataValue = new HashMap();
 			List<Long> targetIdList = new ArrayList<Long>();
 			targetIdList.add(1L);
 			dataValue.put(userNameAttribute, "rahul");
 
 			dataValue.put(association, targetIdList);
 
 			Long recordId = entityManagerInterface.insertData(savedEntity, dataValue);
 
 			dataValue.clear();
 			dataValue.put(userNameAttribute, "vishvesh");
 			dataValue.put(association, targetIdList);
 			Long recordId1 = entityManagerInterface.insertData(savedEntity, dataValue);
 
 			dataValue.clear();
 			dataValue = entityManagerInterface.getRecordById(savedEntity, recordId);
 			List targetRecordIdList = (List) dataValue.get(association);
 			System.out.println(dataValue);
 			assertEquals(1, targetRecordIdList.size());
 			assertEquals(1L, targetRecordIdList.get(0));
 
 			dataValue.clear();
 			dataValue = entityManagerInterface.getRecordById(savedEntity, recordId1);
 			targetRecordIdList = (List) dataValue.get(association);
 			System.out.println(dataValue);
 			assertEquals(1, targetRecordIdList.size());
 			assertEquals(1L, targetRecordIdList.get(0));
 
 		}
 		catch (DynamicExtensionsSystemException e)
 		{
 			e.printStackTrace();
 			fail();
 			Logger.out.debug(e.getStackTrace());
 		}
 		catch (DynamicExtensionsApplicationException e)
 		{
 			e.printStackTrace();
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
 	 * This method test for inserting data for a multi select attribute
 	 */
 	public void testGetRecordByIdAssociationMany2Many()
 	{
 
 		EntityManagerInterface entityManagerInterface = EntityManager.getInstance();
 		DomainObjectFactory factory = DomainObjectFactory.getInstance();
 		EntityGroupInterface entityGroup = factory.createEntityGroup();
 		entityGroup.setName("test_" + new Double(Math.random()).toString());
 		try
 		{
 
 			//          create user
 			EntityInterface user = createAndPopulateEntity();
 			AttributeInterface userNameAttribute = factory.createStringAttribute();
 			userNameAttribute.setName("user name");
 			user.setName("user");
 			user.addAbstractAttribute(userNameAttribute);
 
 			//          create study
 			EntityInterface study = createAndPopulateEntity();
 			AttributeInterface studyNameAttribute = factory.createStringAttribute();
 			studyNameAttribute.setName("study name");
 			study.setName("study");
 			study.addAbstractAttribute(studyNameAttribute);
 
 			//          Associate user (1)------ >(*)study
 			AssociationInterface association = factory.createAssociation();
 			association.setTargetEntity(study);
 			association.setAssociationDirection(AssociationDirection.SRC_DESTINATION);
 			association.setName("primaryInvestigator");
 			association.setSourceRole(getRole(AssociationType.ASSOCIATION, "primaryInvestigator",
 					Cardinality.ZERO, Cardinality.MANY));
 			association.setTargetRole(getRole(AssociationType.ASSOCIATION, "study",
 					Cardinality.ZERO, Cardinality.MANY));
 
 			user.addAbstractAttribute(association);
 			DynamicExtensionsUtility.getConstraintPropertiesForAssociation(association);
 			entityGroup.addEntity(user);
 			user.setEntityGroup(entityGroup);
 			entityGroup.addEntity(study);
 			study.setEntityGroup(entityGroup);
 
 			EntityInterface savedEntity = entityManagerInterface.persistEntity(user);
 
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
 
 			Long recordId = entityManagerInterface.insertData(savedEntity, dataValue);
 
 			dataValue.clear();
 			dataValue.put(userNameAttribute, "vishvesh");
 			dataValue.put(association, targetIdList);
 			entityManagerInterface.insertData(savedEntity, dataValue);
 
 			dataValue.clear();
 			dataValue = entityManagerInterface.getRecordById(savedEntity, recordId);
 
 			List targetRecordIdList = (List) dataValue.get(association);
 
 			System.out.println(dataValue);
 
 			assertEquals(2, targetRecordIdList.size());
 			assertEquals(1L, targetRecordIdList.get(0));
 			assertEquals(2L, targetRecordIdList.get(1));
 
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
 	 * This method test for inserting data for a multi select attribute
 	 */
 	public void testGetRecordByIdForOne2ManyAssociation()
 	{
 
 		EntityManagerInterface entityManagerInterface = EntityManager.getInstance();
 		DomainObjectFactory factory = DomainObjectFactory.getInstance();
 		EntityGroupInterface entityGroup = factory.createEntityGroup();
 		entityGroup.setName("test_" + new Double(Math.random()).toString());
 
 		try
 		{
 
 			//          create user
 			EntityInterface user = createAndPopulateEntity();
 			AttributeInterface userNameAttribute = factory.createStringAttribute();
 			userNameAttribute.setName("user name");
 			user.setName("user");
 			user.addAbstractAttribute(userNameAttribute);
 
 			//          create study
 			EntityInterface study = createAndPopulateEntity();
 			AttributeInterface studyNameAttribute = factory.createStringAttribute();
 			studyNameAttribute.setName("study name");
 			study.setName("study");
 			study.addAbstractAttribute(studyNameAttribute);
 
 			//          Associate user (1)------ >(*)study
 			AssociationInterface association = factory.createAssociation();
 			association.setTargetEntity(study);
 			association.setAssociationDirection(AssociationDirection.SRC_DESTINATION);
 			association.setName("primaryInvestigator");
 			association.setSourceRole(getRole(AssociationType.ASSOCIATION, "primaryInvestigator",
 					Cardinality.ZERO, Cardinality.ONE));
 			association.setTargetRole(getRole(AssociationType.ASSOCIATION, "study",
 					Cardinality.ZERO, Cardinality.MANY));
 
 			user.addAbstractAttribute(association);
 			DynamicExtensionsUtility.getConstraintPropertiesForAssociation(association);
 			entityGroup.addEntity(user);
 			user.setEntityGroup(entityGroup);
 			entityGroup.addEntity(study);
 			study.setEntityGroup(entityGroup);
 
 			EntityInterface savedEntity = entityManagerInterface.persistEntity(user);
 
 			Map dataValue = new HashMap();
 
 			dataValue.put(studyNameAttribute, "study");
 			entityManagerInterface.insertData(study, dataValue);
 
 			dataValue.clear();
 			dataValue.put(studyNameAttribute, "study1");
 			entityManagerInterface.insertData(study, dataValue);
 
 			dataValue.clear();
 			dataValue.put(userNameAttribute, "rahul");
 			List<Long> targetIdList = new ArrayList<Long>();
 			targetIdList.add(1L);
 			dataValue.put(association, targetIdList);
 
 			Long recordId = entityManagerInterface.insertData(savedEntity, dataValue);
 			dataValue.clear();
 
 			dataValue = entityManagerInterface.getRecordById(savedEntity, recordId);
 			List targetRecordIdList = (List) dataValue.get(association);
 
 			System.out.println(dataValue);
 
 			assertEquals(1, targetRecordIdList.size());
 			assertEquals(1L, targetRecordIdList.get(0));
 
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
 	 * This test case is to check the constraint violation in case when the  source cardinality for target is one && maximum cardinality for target is one.
 	 * So in this test case we try to insert data such that the same target entity record is associated with the
 	 * source entity record twice. After the first insertion is successful, when the second insertion takes place
 	 * at that time constraint violation should  fail
 	 */
 	public void testInsertDataForConstraintViolationForOneToOne()
 	{
 
 		/*EntityManagerInterface entityManagerInterface = EntityManager.getInstance();
 		DomainObjectFactory factory = DomainObjectFactory.getInstance();
 		EntityInterface savedEntity = null;
 		try
 		{
 
 			//          create user
 			EntityInterface user = createAndPopulateEntity();
 			AttributeInterface userNameAttribute = factory.createStringAttribute();
 			userNameAttribute.setName("user name");
 			user.setName("user");
 			user.addAbstractAttribute(userNameAttribute);
 
 			//          create study
 			EntityInterface study = createAndPopulateEntity();
 			AttributeInterface studyNameAttribute = factory.createStringAttribute();
 			studyNameAttribute.setName("study name");
 			study.setName("study");
 			study.addAbstractAttribute(studyNameAttribute);
 
 			//          Associate user (1)------ >(*)study
 			AssociationInterface association = factory.createAssociation();
 			association.setTargetEntity(study);
 			association.setAssociationDirection(AssociationDirection.SRC_DESTINATION);
 			association.setName("primaryInvestigator");
 			association.setSourceRole(getRole(AssociationType.ASSOCIATION, "primaryInvestigator",
 					Cardinality.ZERO, Cardinality.ONE));
 			association.setTargetRole(getRole(AssociationType.ASSOCIATION, "study",
 					Cardinality.ZERO, Cardinality.ONE));
 
 			user.addAbstractAttribute(association);
 
 			savedEntity = entityManagerInterface.persistEntity(user);
 
 			Map dataValue = new HashMap();
 			dataValue.clear();
 			dataValue.put(studyNameAttribute, "study1");
 			entityManagerInterface.insertData(study, dataValue);
 
 			dataValue.clear();
 			dataValue.put(userNameAttribute, "rahul");
 			List<Long> targetIdList = new ArrayList<Long>();
 			targetIdList.add(1L);
 			dataValue.put(association, targetIdList);
 
 			entityManagerInterface.insertData(savedEntity, dataValue);
 
 			ResultSet resultSet = executeQuery("select IDENTIFIER from "
 					+ savedEntity.getTableProperties().getName());
 			resultSet.next();
 			assertEquals(1, resultSet.getInt(1));
 
 			entityManagerInterface.insertData(savedEntity, dataValue);
 
 			fail();
 
 		}
 		catch (DynamicExtensionsSystemException e)
 		{
 			fail();
 			Logger.out.debug(e.getStackTrace());
 		}
 		catch (DynamicExtensionsApplicationException e)
 		{
 
 			ResultSet resultSet = executeQuery("select IDENTIFIER from "
 					+ savedEntity.getTableProperties().getName());
 			try
 			{
 				resultSet.next();
 				assertEquals(1, resultSet.getInt(1));
 			}
 			catch (SQLException e1)
 			{
 				e1.printStackTrace();
 				fail();
 
 			}
 
 			Logger.out
 					.debug("constraint validation should fail...because max target cardinality is one");
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 			fail();
 
 			Logger.out.debug(e.getStackTrace());
 		}*/
 
 	}
 
 	/**
 	 * This test case is to check the scenario when user adds maximum cardinality less than the minimum cardinality
 	 * In such case DE internally corrects these cardinalities by swapping the minimum and maximum cardinalities.
 	 */
 	public void testInsertDataForInvalidCardinalities()
 	{
 
 		EntityManagerInterface entityManagerInterface = EntityManager.getInstance();
 		DomainObjectFactory factory = DomainObjectFactory.getInstance();
 		EntityGroupInterface entityGroup = factory.createEntityGroup();
 		entityGroup.setName("test_" + new Double(Math.random()).toString());
 
 		EntityInterface savedEntity = null;
 		try
 		{
 
 			//          create user
 			EntityInterface user = createAndPopulateEntity();
 			AttributeInterface userNameAttribute = factory.createStringAttribute();
 			userNameAttribute.setName("user name");
 			user.setName("user");
 			user.addAbstractAttribute(userNameAttribute);
 
 			//          create study
 			EntityInterface study = createAndPopulateEntity();
 			AttributeInterface studyNameAttribute = factory.createStringAttribute();
 			studyNameAttribute.setName("study name");
 			study.setName("study");
 			study.addAbstractAttribute(studyNameAttribute);
 
 			//          Associate user (1)------ >(*)study
 			AssociationInterface association = factory.createAssociation();
 			association.setTargetEntity(study);
 			association.setAssociationDirection(AssociationDirection.SRC_DESTINATION);
 			association.setName("primaryInvestigator");
 			association.setSourceRole(getRole(AssociationType.ASSOCIATION, "primaryInvestigator",
 					Cardinality.MANY, Cardinality.ZERO));
 			association.setTargetRole(getRole(AssociationType.ASSOCIATION, "study",
 					Cardinality.ONE, Cardinality.ZERO));
 
 			user.addAbstractAttribute(association);
 			DynamicExtensionsUtility.getConstraintPropertiesForAssociation(association);
 			entityGroup.addEntity(user);
 			user.setEntityGroup(entityGroup);
 			entityGroup.addEntity(study);
 			study.setEntityGroup(entityGroup);
 
 			savedEntity = entityManagerInterface.persistEntity(user);
 
 			Map dataValue = new HashMap();
 			dataValue.put(userNameAttribute, "rahul");
 			List<Long> targetIdList = new ArrayList<Long>();
 			targetIdList.add(1L);
 
 			dataValue.put(association, targetIdList);
 
 			entityManagerInterface.insertData(savedEntity, dataValue);
 			int identifier = (Integer) executeQuery("select IDENTIFIER from "
 					+ savedEntity.getTableProperties().getName(), INT_TYPE, 1);
 			assertEquals(1, identifier);
 		}
 		catch (Exception e)
 		{
 			Logger.out.debug(e.getStackTrace());
 			e.printStackTrace();
 			fail();
 
 		}
 
 	}
 
 	/**
 	 *  Purpose: To test the deletion of record for an entity which has a one to many association with other entity.
 	 *  Expected behavior: the entry for this record in the target entity's data table should also be removed.
 	 *  Test case flow : 1. create user and study entity. 2. create association User 1--->* Study. 3.Persist entities.
 	 *  4.Insert one record 5. Check the record has been inserted or not. 6. Delete the inserted record.
 	 */
 
 	public void testDeleteRecordWithAssociationOneToMany()
 	{
 
 		try
 		{
 			DomainObjectFactory factory = DomainObjectFactory.getInstance();
 			EntityGroupInterface entityGroup = factory.createEntityGroup();
 			entityGroup.setName("test_" + new Double(Math.random()).toString());
 			MockEntityManager mock = new MockEntityManager();
 			Entity study = (Entity) createAndPopulateEntity();;
 			study.setName("study");
 
 			Attribute studyName = (Attribute) mock.initializeStringAttribute("studyName",
 					"new Study");
 			study.addAbstractAttribute(studyName);
 
 			Entity user = (Entity) createAndPopulateEntity();;
 			user.setName("user");
 			Attribute userName = (Attribute) mock.initializeStringAttribute("userName", "new User");
 			user.addAbstractAttribute(userName);
 
 			//Associate  User(1) <---->(*)Study
 			AssociationInterface association = DomainObjectFactory.getInstance()
 					.createAssociation();
 
 			association.setTargetEntity(study);
 			association.setAssociationDirection(AssociationDirection.SRC_DESTINATION);
 			association.setName("primaryInvestigator");
 			association.setSourceRole(getRole(AssociationType.ASSOCIATION, "primaryInvestigator",
 					Cardinality.ONE, Cardinality.ONE));
 			association.setTargetRole(getRole(AssociationType.ASSOCIATION, "study",
 					Cardinality.ZERO, Cardinality.MANY));
 
 			user.addAbstractAttribute(association);
 			DynamicExtensionsUtility.getConstraintPropertiesForAssociation(association);
 			EntityManagerUtil.addIdAttribute(user);
 			entityGroup.addEntity(user);
 			user.setEntityGroup(entityGroup);
 			entityGroup.addEntity(study);
 			study.setEntityGroup(entityGroup);
 
 			EntityManager.getInstance().persistEntity(user);
 
 			Map dataValue = new HashMap();
 			List list = new ArrayList();
 			list.add(1L);
 			dataValue.put(userName, "User1");
 			dataValue.put(association, list);
 
 			EntityManager.getInstance().insertData(user, dataValue);
 			int columnValue = (Integer) executeQuery("select * from "
 					+ user.getTableProperties().getName(), INT_TYPE, 2);
 			assertEquals(1, columnValue);
 			String value = (String) executeQuery("select * from "
 					+ user.getTableProperties().getName(), STRING_TYPE, 3);
 			assertEquals("User1", value);
 
 			assertEquals(getColumnCount("select * from " + user.getTableProperties().getName()),
 					noOfDefaultColumns + 1);
 
 			int rowCount = (Integer) executeQuery("select count(*) from "
 					+ user.getTableProperties().getName(), INT_TYPE, 1);
 			assertEquals(1, rowCount);
 
 			EntityManager.getInstance().deleteRecord(user, 1L);
 
 			assertEquals(Status.ACTIVITY_STATUS_DISABLED.toString(), getActivityStatus(user, 1L));
 
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
 	 *  Purpose: To test the deletion of record for an entity which has a many to many association with other entity.
 	 *  Expected behavior: the entry for this record in the target entity's data table should also be removed.
 	 *  Test case flow : 1. create user and study entity. 2. create association User *--->* Study. 3.Persist entities.
 	 *  4.Insert one record 5. Check the record has been inserted or not. 6. Delete the inserted record.
 	 *  7.Check if the record is deleted. 8. Check if there are no entries for the deleted record in the
 	 *  middle table of the many to many association.
 	 */
 
 	public void testDeleteRecordWithAssociationManyToMany()
 	{
 
 		try
 		{
 			DomainObjectFactory factory = DomainObjectFactory.getInstance();
 			EntityGroupInterface entityGroup = factory.createEntityGroup();
 			entityGroup.setName("test_" + new Double(Math.random()).toString());
 			MockEntityManager mock = new MockEntityManager();
 			Entity study = (Entity) createAndPopulateEntity();
 			study.setName("study");
 
 			Attribute studyName = (Attribute) mock.initializeStringAttribute("studyName",
 					"new Study");
 			study.addAbstractAttribute(studyName);
 
 			Entity user = (Entity) createAndPopulateEntity();;
 			user.setName("user");
 			Attribute userName = (Attribute) mock.initializeStringAttribute("userName", "new User");
 			user.addAbstractAttribute(userName);
 
 			//Associate  User(*) <---->(*)Study
 			AssociationInterface association = DomainObjectFactory.getInstance()
 					.createAssociation();
 
 			association.setTargetEntity(study);
 			association.setAssociationDirection(AssociationDirection.SRC_DESTINATION);
 			association.setName("primaryInvestigator");
 			association.setSourceRole(getRole(AssociationType.ASSOCIATION, "primaryInvestigator",
 					Cardinality.ONE, Cardinality.MANY));
 			association.setTargetRole(getRole(AssociationType.ASSOCIATION, "study",
 					Cardinality.ZERO, Cardinality.MANY));
 
 			user.addAbstractAttribute(association);
 			DynamicExtensionsUtility.getConstraintPropertiesForAssociation(association);
 			EntityManagerUtil.addIdAttribute(user);
 			entityGroup.addEntity(user);
 			user.setEntityGroup(entityGroup);
 			entityGroup.addEntity(study);
 			study.setEntityGroup(entityGroup);
 
 			EntityManager.getInstance().persistEntity(user);
 
 			Map dataValue = new HashMap();
 			List list = new ArrayList();
 			list.add(1L);
 			list.add(2L);
 			dataValue.put(userName, "User1");
 			dataValue.put(association, list);
 
 			Long recordId = EntityManager.getInstance().insertData(user, dataValue);
 
 			//Checking whether there is an entry added in the data table for user.
 			int columnValue = (Integer) executeQuery("select * from "
 					+ user.getTableProperties().getName(), INT_TYPE, 2);
 			assertEquals(1, columnValue);
 			//Checking whether the the value of the second column (i.e. the column for the user name.. first column is identifier).
 			//is having the expected value or not.
 			String value = (String) executeQuery("select * from "
 					+ user.getTableProperties().getName(), "string", 3);
 			assertEquals("User1", value);
 			assertEquals(getColumnCount("select * from " + user.getTableProperties().getName()),
 					noOfDefaultColumns + 1);
 
 			//Checking whether there are 2 entries added in the middle table for the many to many association.
 			int rowCount = (Integer) executeQuery("select count(*) from "
 					+ association.getConstraintProperties().getName(), INT_TYPE, 1);
 			assertEquals(2, rowCount);
 			//Deleting the record
 			EntityManager.getInstance().deleteRecord(user, 1L);
 			//Checking there is no entry for the deleted record in the middle table.
 			assertEquals(Status.ACTIVITY_STATUS_DISABLED.toString(), getActivityStatus(user, 1L));
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
 	 *  Purpose: To test the deletion of record for an entity which has a many to one association with other entity.
 	 *  Expected behavior: the entry for this record in the target entity's data table should also be removed.
 	 *  Test case flow : 1. create user and study entity. 2. create association User *--->1 Study. 3.Persist entities.
 	 *  4.Insert one record 5. Check the record has been inserted or not. 6. Delete the inserted record.
 	 *  7.Check if the record is properly deleted or not.
 	 */
 
 	public void testDeleteRecordWithAssociationManyToOne()
 	{
 
 		try
 		{
 			//Variables.databaseName = DEConstants.ORACLE_DATABASE;
 			DomainObjectFactory factory = DomainObjectFactory.getInstance();
 			EntityGroupInterface entityGroup = factory.createEntityGroup();
 			entityGroup.setName("test_" + new Double(Math.random()).toString());
 			MockEntityManager mock = new MockEntityManager();
 			Entity study = (Entity) createAndPopulateEntity();;
 			study.setName("study");
 
 			Attribute studyName = (Attribute) mock.initializeStringAttribute("studyName",
 					"new Study");
 			study.addAbstractAttribute(studyName);
 
 			Entity user = (Entity) createAndPopulateEntity();;
 			user.setName("user");
 			Attribute userName = (Attribute) mock.initializeStringAttribute("userName", "new User");
 			user.addAbstractAttribute(userName);
 
 			//Associate  User(*) <---->(1)Study
 			AssociationInterface association = DomainObjectFactory.getInstance()
 					.createAssociation();
 
 			association.setTargetEntity(study);
 			association.setAssociationDirection(AssociationDirection.SRC_DESTINATION);
 			association.setName("primaryInvestigator");
 			association.setSourceRole(getRole(AssociationType.ASSOCIATION, "primaryInvestigator",
 					Cardinality.ONE, Cardinality.MANY));
 			association.setTargetRole(getRole(AssociationType.ASSOCIATION, "study",
 					Cardinality.ZERO, Cardinality.ONE));
 
 			user.addAbstractAttribute(association);
 			DynamicExtensionsUtility.getConstraintPropertiesForAssociation(association);
 			entityGroup.addEntity(user);
 			user.setEntityGroup(entityGroup);
 			entityGroup.addEntity(study);
 			study.setEntityGroup(entityGroup);
 
 			EntityManager.getInstance().persistEntity(user);
 
 			Map dataValue = new HashMap();
 			List list = new ArrayList();
 			list.add(1L);
 			dataValue.put(userName, "User1");
 			dataValue.put(association, list);
 
 			Long recordId = EntityManager.getInstance().insertData(user, dataValue);
 
 			//Checking whether there is an entry added in the data table for user.
 			int identifier = (Integer) executeQuery("select IDENTIFIER from "
 					+ user.getTableProperties().getName(), INT_TYPE, 1);
 
 			assertEquals(1, identifier);
 			//Checking whether the the value of the second column (i.e. the column for the user name.. first column is identifier).
 			//is having the expected value or not.
 			//Checking whether there is an entry added in the data table for user.
 			String value = (String) executeQuery("select "
 					+ userName.getColumnProperties().getName() + " from "
 					+ user.getTableProperties().getName(), STRING_TYPE, 1);
 
 			assertEquals("User1", value);
 			//Checking if the extra column for many to one association is added or not
 			assertEquals(getColumnCount("select * from " + user.getTableProperties().getName()),
 					noOfDefaultColumns + 2);
 
 			//Deleting the record
 			EntityManager.getInstance().deleteRecord(user, 1L);
 
 			assertEquals(Status.ACTIVITY_STATUS_DISABLED.toString(), getActivityStatus(user, 1L));;
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
 	 * @param targetEntity
 	 * @param associationDirection
 	 * @param assoName
 	 * @param sourceRole
 	 * @param targetRole
 	 * @return
 	 * @throws DynamicExtensionsSystemException 
 	 */
 	private AssociationInterface getAssociation(EntityInterface targetEntity,
 			AssociationDirection associationDirection, String assoName, RoleInterface sourceRole,
 			RoleInterface targetRole) throws DynamicExtensionsSystemException
 	{
 		AssociationInterface association = DomainObjectFactory.getInstance().createAssociation();
 		association.setTargetEntity(targetEntity);
 		association.setAssociationDirection(associationDirection);
 		association.setName(assoName);
 		association.setSourceRole(sourceRole);
 		association.setTargetRole(targetRole);
 		return association;
 	}
 
 	/**
 	 * This test case test for associating three entities with  many to one to one
 	 *
 	 * User(*) ---- >(1)Study(1) ------>(1)institute
 	 */
 	public void testCreateEntityWithCascadeManyToOneAssociation()
 	{
 		EntityManagerInterface entityManager = EntityManager.getInstance();
 		DomainObjectFactory factory = DomainObjectFactory.getInstance();
 		EntityGroupInterface entityGroup = factory.createEntityGroup();
 		entityGroup.setName("test_" + new Double(Math.random()).toString());
 
 		// create user
 		EntityInterface user = createAndPopulateEntity();
 		AttributeInterface userNameAttribute = factory.createStringAttribute();
 		userNameAttribute.setName("user name");
 		user.setName("user");
 		user.addAbstractAttribute(userNameAttribute);
 
 		// create study
 		EntityInterface study = createAndPopulateEntity();
 		AttributeInterface studyNameAttribute = factory.createStringAttribute();
 		studyNameAttribute.setName("study name");
 		study.setName("study");
 		study.addAbstractAttribute(studyNameAttribute);
 
 		// create institution
 		EntityInterface institution = createAndPopulateEntity();
 		AttributeInterface institutionNameAttribute = factory.createStringAttribute();
 		institutionNameAttribute.setName("institution name");
 		institution.setName("institution");
 		institution.addAbstractAttribute(institutionNameAttribute);
 
 		// Associate user (*)------ >(1)study
 		AssociationInterface association = factory.createAssociation();
 		try
 		{
 			association.setTargetEntity(study);
 			association.setAssociationDirection(AssociationDirection.SRC_DESTINATION);
 			association.setName("primaryInvestigator");
 			association.setSourceRole(getRole(AssociationType.ASSOCIATION, "primaryInvestigator",
 					Cardinality.ZERO, Cardinality.MANY));
 			association.setTargetRole(getRole(AssociationType.ASSOCIATION, "study",
 					Cardinality.ZERO, Cardinality.ONE));
 
 			user.addAbstractAttribute(association);
 			DynamicExtensionsUtility.getConstraintPropertiesForAssociation(association);
 
 			// Associate study(1) ------> (1) institution
 			AssociationInterface studInstAssociation = factory.createAssociation();
 
 			studInstAssociation.setTargetEntity(institution);
 			studInstAssociation.setAssociationDirection(AssociationDirection.SRC_DESTINATION);
 			studInstAssociation.setName("studyLocation");
 			studInstAssociation.setSourceRole(getRole(AssociationType.ASSOCIATION,
 					"studyPerformed", Cardinality.ZERO, Cardinality.ONE));
 			studInstAssociation.setTargetRole(getRole(AssociationType.ASSOCIATION, "studyLocation",
 					Cardinality.ZERO, Cardinality.ONE));
 
 			study.addAbstractAttribute(studInstAssociation);
 			DynamicExtensionsUtility.getConstraintPropertiesForAssociation(studInstAssociation);
 			entityGroup.addEntity(user);
 			user.setEntityGroup(entityGroup);
 			entityGroup.addEntity(study);
 			study.setEntityGroup(entityGroup);
 			entityGroup.addEntity(institution);
 			institution.setEntityGroup(entityGroup);
 
 			//entityManager.createEntity(study);
 
 			EntityInterface savedUser = entityManager.persistEntity(user);
 
 			assertEquals(getColumnCount("select * from " + user.getTableProperties().getName()),
 					noOfDefaultColumns + 2);
 
 			assertEquals(getColumnCount("select * from " + study.getTableProperties().getName()),
 					noOfDefaultColumns + 1);
 
 			assertEquals(getColumnCount("select * from "
 					+ institution.getTableProperties().getName()), noOfDefaultColumns + 2);
 
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
 
 	}
 
 	/**
 	 * This test case test for associating three entities with  many to one to many to many
 	 *
 	 *        User(*) ---- >(1)Study(1) ------>(1)institute(*)-- (*)User
 	 *
 	 */
 	public void testCreateEntityWithCyclicCascadeManyToOneAssociation()
 	{
 		EntityManagerInterface entityManager = EntityManager.getInstance();
 		DomainObjectFactory factory = DomainObjectFactory.getInstance();
 		EntityGroupInterface entityGroup = factory.createEntityGroup();
 		entityGroup.setName("test_" + new Double(Math.random()).toString());
 
 		// create user
 		EntityInterface user = createAndPopulateEntity();
 		AttributeInterface userNameAttribute = factory.createStringAttribute();
 		userNameAttribute.setName("user name");
 		user.setName("user");
 		user.addAbstractAttribute(userNameAttribute);
 
 		// create study
 		EntityInterface study = createAndPopulateEntity();
 		AttributeInterface studyNameAttribute = factory.createStringAttribute();
 		studyNameAttribute.setName("study name");
 		study.setName("study");
 		study.addAbstractAttribute(studyNameAttribute);
 
 		// create institution
 		EntityInterface institution = createAndPopulateEntity();
 		AttributeInterface institutionNameAttribute = factory.createStringAttribute();
 		institutionNameAttribute.setName("institution name");
 		institution.setName("institution");
 		institution.addAbstractAttribute(institutionNameAttribute);
 
 		// Associate user (*)------ >(1)study
 		AssociationInterface association = factory.createAssociation();
 		try
 		{
 			association.setTargetEntity(study);
 			association.setAssociationDirection(AssociationDirection.SRC_DESTINATION);
 			association.setName("primaryInvestigator");
 			association.setSourceRole(getRole(AssociationType.ASSOCIATION, "primaryInvestigator",
 					Cardinality.ZERO, Cardinality.MANY));
 			association.setTargetRole(getRole(AssociationType.ASSOCIATION, "study",
 					Cardinality.ZERO, Cardinality.ONE));
 
 			user.addAbstractAttribute(association);
 			DynamicExtensionsUtility.getConstraintPropertiesForAssociation(association);
 			EntityManagerUtil.addIdAttribute(user);
 			entityGroup.addEntity(user);
 			user.setEntityGroup(entityGroup);
 			// Associate study(1) ------> (1) institution
 			AssociationInterface studInstAssociation = factory.createAssociation();
 
 			studInstAssociation.setTargetEntity(institution);
 			studInstAssociation.setAssociationDirection(AssociationDirection.SRC_DESTINATION);
 			studInstAssociation.setName("studyLocation");
 			studInstAssociation.setSourceRole(getRole(AssociationType.ASSOCIATION,
 					"studyPerformed", Cardinality.ZERO, Cardinality.ONE));
 			studInstAssociation.setTargetRole(getRole(AssociationType.ASSOCIATION, "studyLocation",
 					Cardinality.ZERO, Cardinality.ONE));
 
 			study.addAbstractAttribute(studInstAssociation);
 			DynamicExtensionsUtility.getConstraintPropertiesForAssociation(studInstAssociation);
 			EntityManagerUtil.addIdAttribute(study);
 			entityGroup.addEntity(study);
 			study.setEntityGroup(entityGroup);
 			// Associate institution (*)----->(*) user
 			AssociationInterface instUserAssociation = factory.createAssociation();
 
 			instUserAssociation.setTargetEntity(user);
 			instUserAssociation.setAssociationDirection(AssociationDirection.SRC_DESTINATION);
 			instUserAssociation.setName("lecturers");
 			instUserAssociation.setSourceRole(getRole(AssociationType.ASSOCIATION, "university",
 					Cardinality.ONE, Cardinality.MANY));
 			instUserAssociation.setTargetRole(getRole(AssociationType.ASSOCIATION, "lecturer",
 					Cardinality.ONE, Cardinality.MANY));
 
 			institution.addAbstractAttribute(instUserAssociation);
 			DynamicExtensionsUtility.getConstraintPropertiesForAssociation(instUserAssociation);
 			entityGroup.addEntity(institution);
 			institution.setEntityGroup(entityGroup);
 
 			EntityInterface savedUser = entityManager.persistEntity(user);
 
 			assertEquals(getColumnCount("select * from " + user.getTableProperties().getName()),
 					noOfDefaultColumns + 2);
 
 			assertEquals(getColumnCount("select * from " + study.getTableProperties().getName()),
 					noOfDefaultColumns + 1);
 
 			assertEquals(getColumnCount("select * from "
 					+ institution.getTableProperties().getName()), noOfDefaultColumns + 2);
 
 			assertNotNull(instUserAssociation.getConstraintProperties().getName());
 
 			assertEquals(getColumnCount("select * from "
 					+ instUserAssociation.getConstraintProperties().getName()),
 					noOfDefaultColumns + 1);
 
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
 	}
 
 	/**
 	 * Purpose is to test the self referencing of the entity.
 	 * Scenario - user(*)------>(1)User
 	 *                   creator
 	 */
 	public void testCreateEntityWithSelfReferencingManyToManyAssociation()
 	{
 		EntityManagerInterface entityManager = EntityManager.getInstance();
 		DomainObjectFactory factory = DomainObjectFactory.getInstance();
 		EntityGroupInterface entityGroup = factory.createEntityGroup();
 		entityGroup.setName("test_" + new Double(Math.random()).toString());
 		// create user
 		EntityInterface user = createAndPopulateEntity();
 		AttributeInterface userNameAttribute = factory.createStringAttribute();
 		userNameAttribute.setName("user name");
 		user.setName("user");
 		user.addAbstractAttribute(userNameAttribute);
 
 		// Associate user (*)------ >(1)user
 		AssociationInterface association = factory.createAssociation();
 
 		try
 		{
 			association.setTargetEntity(user);
 			association.setAssociationDirection(AssociationDirection.SRC_DESTINATION);
 			association.setName("primaryInvestigator");
 			association.setSourceRole(getRole(AssociationType.ASSOCIATION, "subpi",
 					Cardinality.ONE, Cardinality.MANY));
 			association.setTargetRole(getRole(AssociationType.ASSOCIATION, "pi", Cardinality.ONE,
 					Cardinality.MANY));
 
 			user.addAbstractAttribute(association);
 			DynamicExtensionsUtility.getConstraintPropertiesForAssociation(association);
 			entityGroup.addEntity(user);
 			user.setEntityGroup(entityGroup);
 
 			EntityInterface savedUser = entityManager.persistEntity(user);
 
 			String tableName = user.getTableProperties().getName();
 
 			assertNotNull(tableName);
 
 			assertEquals(getColumnCount("select * from " + tableName), noOfDefaultColumns + 1);
 
 			assertEquals(noOfDefaultColumns + 1, getColumnCount("select * from "
 					+ association.getConstraintProperties().getName()));
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
 
 	}
 
 	/**
 	 * Purpose is to test the multiple self referencing of the entity.
 	 * Scenario -
 	 * user(*)------>(*)User
 	 *       childUsers
 	 * user(*)------>(1)User
 	 *        creator
 	 */
 	public void testCreateEntityWithSelfReferencingMultipleAssociations()
 	{
 		EntityManagerInterface entityManager = EntityManager.getInstance();
 		DomainObjectFactory factory = DomainObjectFactory.getInstance();
 		EntityGroupInterface entityGroup = factory.createEntityGroup();
 		entityGroup.setName("test_" + new Double(Math.random()).toString());
 		// create user
 		EntityInterface user = createAndPopulateEntity();
 		AttributeInterface userNameAttribute = factory.createStringAttribute();
 		userNameAttribute.setName("user name");
 		user.setName("user");
 		user.addAbstractAttribute(userNameAttribute);
 
 		// Associate user (*)------ >(*)user
 		AssociationInterface association = factory.createAssociation();
 		try
 		{
 
 			association.setTargetEntity(user);
 			association.setAssociationDirection(AssociationDirection.SRC_DESTINATION);
 			association.setName("children");
 			association.setSourceRole(getRole(AssociationType.ASSOCIATION, "subpi",
 					Cardinality.ONE, Cardinality.MANY));
 			association.setTargetRole(getRole(AssociationType.ASSOCIATION, "pi", Cardinality.ONE,
 					Cardinality.MANY));
 
 			user.addAbstractAttribute(association);
 			DynamicExtensionsUtility.getConstraintPropertiesForAssociation(association);
 
 			// Associate user (*)------ >(1)user
 			AssociationInterface creatorAssociation = factory.createAssociation();
 
 			creatorAssociation.setTargetEntity(user);
 			creatorAssociation.setAssociationDirection(AssociationDirection.SRC_DESTINATION);
 			creatorAssociation.setName("creator");
 			creatorAssociation.setSourceRole(getRole(AssociationType.ASSOCIATION, "user",
 					Cardinality.ONE, Cardinality.MANY));
 			creatorAssociation.setTargetRole(getRole(AssociationType.ASSOCIATION, "creator",
 					Cardinality.ONE, Cardinality.ONE));
 
 			user.addAbstractAttribute(creatorAssociation);
 			DynamicExtensionsUtility.getConstraintPropertiesForAssociation(creatorAssociation);
 			entityGroup.addEntity(user);
 			user.setEntityGroup(entityGroup);
 
 			EntityInterface savedUser = entityManager.persistEntity(user);
 
 			String tableName = user.getTableProperties().getName();
 
 			assertNotNull(tableName);
 
 			assertEquals(noOfDefaultColumns + 2, getColumnCount("select * from " + tableName));
 
 			assertEquals(noOfDefaultColumns + 1, getColumnCount("select * from "
 					+ association.getConstraintProperties().getName()));
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
 
 	}
 
 	/**
 	 * Purpose is to test the multiple self referencing of the entity.
 	 * Scenario -
 	 * user(*)------>(*)User
 	 *       childUsers
 	 * user(*)------>(*)User
 	 *        creators
 	 */
 	public void testCreateEntityWithSelfReferencingMultipleManyToManyAssociations()
 	{
 		EntityManagerInterface entityManager = EntityManager.getInstance();
 		DomainObjectFactory factory = DomainObjectFactory.getInstance();
 		EntityGroupInterface entityGroup = factory.createEntityGroup();
 		entityGroup.setName("test_" + new Double(Math.random()).toString());
 		// create user
 		EntityInterface user = createAndPopulateEntity();
 		AttributeInterface userNameAttribute = factory.createStringAttribute();
 		userNameAttribute.setName("user name");
 		user.setName("user");
 		user.addAbstractAttribute(userNameAttribute);
 
 		// Associate user (*)------ >(*)user
 		AssociationInterface association = factory.createAssociation();
 		try
 		{
 			association.setTargetEntity(user);
 			association.setAssociationDirection(AssociationDirection.SRC_DESTINATION);
 			association.setName("children");
 			association.setSourceRole(getRole(AssociationType.ASSOCIATION, "subpi",
 					Cardinality.ONE, Cardinality.MANY));
 			association.setTargetRole(getRole(AssociationType.ASSOCIATION, "pi", Cardinality.ONE,
 					Cardinality.MANY));
 
 			user.addAbstractAttribute(association);
 			DynamicExtensionsUtility.getConstraintPropertiesForAssociation(association);
 
 			// Associate user (*)------ >(*)user
 			AssociationInterface creatorAssociation = factory.createAssociation();
 
 			creatorAssociation.setTargetEntity(user);
 			creatorAssociation.setAssociationDirection(AssociationDirection.SRC_DESTINATION);
 			creatorAssociation.setName("parentUSers");
 			creatorAssociation.setSourceRole(getRole(AssociationType.ASSOCIATION, "user",
 					Cardinality.ONE, Cardinality.MANY));
 			creatorAssociation.setTargetRole(getRole(AssociationType.ASSOCIATION, "creator",
 					Cardinality.ONE, Cardinality.MANY));
 
 			user.addAbstractAttribute(creatorAssociation);
 			DynamicExtensionsUtility.getConstraintPropertiesForAssociation(creatorAssociation);
 			entityGroup.addEntity(user);
 			user.setEntityGroup(entityGroup);
 
 			EntityInterface savedUser = entityManager.persistEntity(user);
 
 			String tableName = user.getTableProperties().getName();
 
 			assertNotNull(tableName);
 
 			assertEquals(noOfDefaultColumns + 1, getColumnCount("select * from " + tableName));
 
 			assertEquals(noOfDefaultColumns + 1, getColumnCount("select * from "
 					+ association.getConstraintProperties().getName()));
 
 			assertEquals(noOfDefaultColumns + 1, getColumnCount("select * from "
 					+ creatorAssociation.getConstraintProperties().getName()));
 
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
 
 	}
 
 	/**
 	 * This test case test for associating two entities with one to many association  and bidirectional
 	 * It tests for internal system generated association for the bidirectional.
 	 * User(1) <---->(*)Study
 	 */
 	public void testCreateEntityBidirectionalOneToManyAssociation()
 	{
 		EntityManagerInterface entityManager = EntityManager.getInstance();
 		DomainObjectFactory factory = DomainObjectFactory.getInstance();
 		EntityGroupInterface entityGroup = factory.createEntityGroup();
 		entityGroup.setName("test_" + new Double(Math.random()).toString());
 		// create user
 		EntityInterface user = createAndPopulateEntity();
 		AttributeInterface userNameAttribute = factory.createStringAttribute();
 		userNameAttribute.setName("user name");
 		user.setName("user");
 		user.addAbstractAttribute(userNameAttribute);
 		EntityManagerUtil.addIdAttribute(user);
 		// create study
 		EntityInterface study = createAndPopulateEntity();
 		study.setName("Study");
 
 		// Associate  User(1) <---->(*)Study
 		AssociationInterface association = factory.createAssociation();
 		association.setEntity(user);
 		try
 		{
 			association.setTargetEntity(study);
 			association.setAssociationDirection(AssociationDirection.BI_DIRECTIONAL);
 			association.setName("primaryInvestigator");
 			association.setSourceRole(getRole(AssociationType.ASSOCIATION, "primaryInvestigator",
 					Cardinality.ONE, Cardinality.ONE));
 			association.setTargetRole(getRole(AssociationType.ASSOCIATION, "study",
 					Cardinality.ZERO, Cardinality.MANY));
 			DynamicExtensionsUtility.getConstraintPropertiesForAssociation(association);
 
 			user.addAbstractAttribute(association);
 			entityGroup.addEntity(user);
 			entityGroup.addEntity(study);
 			user.setEntityGroup(entityGroup);
 			study.setEntityGroup(entityGroup);
 
 			EntityInterface savedUser = entityManager.persistEntity(user);
 
 			assertEquals(noOfDefaultColumns + 1, getColumnCount("select * from "
 					+ study.getTableProperties().getName()));
 			//1 user attribute + 1 system generated attribute
 			assertEquals(2, study.getAbstractAttributeCollection().size());
 
 			Association systemGeneratedAssociation = (Association) study
 					.getAbstractAttributeCollection().toArray()[0];
 
 			assertTrue(systemGeneratedAssociation.getIsSystemGenerated());
 
 			assertEquals(user, systemGeneratedAssociation.getTargetEntity());
 
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
 
 	}
 
 	/**
 	 *
 	 *
 	 */
 	public void testGetAssociation()
 	{
 		try
 		{
 			EntityManagerInterface entityManager = EntityManager.getInstance();
 			DomainObjectFactory factory = DomainObjectFactory.getInstance();
 			EntityGroupInterface entityGroup = factory.createEntityGroup();
 			entityGroup.setName("test_" + new Double(Math.random()).toString());
 
 			// create user
 			EntityInterface user = createAndPopulateEntity();
 			AttributeInterface userNameAttribute = factory.createStringAttribute();
 			userNameAttribute.setName("user name");
 			user.setName("user");
 			user.addAbstractAttribute(userNameAttribute);
 
 			// create study
 			EntityInterface study = createAndPopulateEntity();
 			AttributeInterface studyNameAttribute = factory.createStringAttribute();
 			studyNameAttribute.setName("study name");
 			study.setName("study");
 			study.addAbstractAttribute(studyNameAttribute);
 
 			// create institution
 			EntityInterface institution = createAndPopulateEntity();
 			AttributeInterface institutionNameAttribute = factory.createStringAttribute();
 			institutionNameAttribute.setName("institution name");
 			institution.setName("institution");
 			institution.addAbstractAttribute(institutionNameAttribute);
 
 			// Associate user (*)------ >(1)study
 			AssociationInterface association = factory.createAssociation();
 
 			association.setTargetEntity(study);
 			association.setAssociationDirection(AssociationDirection.SRC_DESTINATION);
 			association.setName("primaryInvestigator");
 			association.setSourceRole(getRole(AssociationType.ASSOCIATION, "primaryInvestigator",
 					Cardinality.ZERO, Cardinality.MANY));
 			association.setTargetRole(getRole(AssociationType.ASSOCIATION, "study",
 					Cardinality.ZERO, Cardinality.ONE));
 
 			user.addAbstractAttribute(association);
 			DynamicExtensionsUtility.getConstraintPropertiesForAssociation(association);
 
 			// Associate study(1) ------> (1) institution
 			AssociationInterface studInstAssociation = factory.createAssociation();
 
 			studInstAssociation.setTargetEntity(institution);
 			studInstAssociation.setAssociationDirection(AssociationDirection.SRC_DESTINATION);
 			studInstAssociation.setName("studyLocation");
 			studInstAssociation.setSourceRole(getRole(AssociationType.ASSOCIATION,
 					"studyPerformed", Cardinality.ZERO, Cardinality.ONE));
 			studInstAssociation.setTargetRole(getRole(AssociationType.ASSOCIATION, "studyLocation",
 					Cardinality.ZERO, Cardinality.ONE));
 
 			study.addAbstractAttribute(studInstAssociation);
 			DynamicExtensionsUtility.getConstraintPropertiesForAssociation(studInstAssociation);
 			entityGroup.addEntity(user);
 			entityGroup.addEntity(study);
 			user.setEntityGroup(entityGroup);
 			study.setEntityGroup(entityGroup);
 			entityGroup.addEntity(institution);
 			institution.setEntityGroup(entityGroup);
 			user = (Entity) entityManager.persistEntity(user);
 			Collection<AssociationInterface> associationInterface = entityManager.getAssociation(
 					"user", "primaryInvestigator");
 			assertNotNull(associationInterface);
 
 		}
 
 		catch (Exception e)
 		{
 			//TODO Auto-generated catch block
 			e.printStackTrace();
 			Logger.out.debug(e.getMessage());
 			fail("Exception occured");
 		}
 
 	}
 
 	public void testGetAssociations()
 	{
 		EntityManagerInterface entityManager = EntityManager.getInstance();
 		DomainObjectFactory factory = DomainObjectFactory.getInstance();
 		EntityGroupInterface entityGroup = factory.createEntityGroup();
 		entityGroup.setName("test_" + new Double(Math.random()).toString());
 
 		// create user
 		EntityInterface user = createAndPopulateEntity();
 		AttributeInterface userNameAttribute = factory.createStringAttribute();
 		userNameAttribute.setName("user name");
 		user.setName("user");
 		user.addAbstractAttribute(userNameAttribute);
 
 		// create study
 		EntityInterface study = createAndPopulateEntity();
 		study.setName("study name");
 
 		// Associate  User(1) <---->(*)Study
 		AssociationInterface association = factory.createAssociation();
 		association.setEntity(user);
 		try
 		{
 			association.setTargetEntity(study);
 			association.setAssociationDirection(AssociationDirection.BI_DIRECTIONAL);
 			association.setName("primaryInvestigator");
 			association.setSourceRole(getRole(AssociationType.ASSOCIATION, "primaryInvestigator",
 					Cardinality.ONE, Cardinality.ONE));
 			association.setTargetRole(getRole(AssociationType.ASSOCIATION, "study",
 					Cardinality.ZERO, Cardinality.MANY));
 
 			user.addAbstractAttribute(association);
 			DynamicExtensionsUtility.getConstraintPropertiesForAssociation(association);
 			entityGroup.addEntity(user);
 			user.setEntityGroup(entityGroup);
 			entityGroup.addEntity(study);
 			study.setEntityGroup(entityGroup);
 
 			EntityInterface savedUser = entityManager.persistEntity(user);
 			Collection associationCollection = entityManager.getAssociations(savedUser.getId(),
 					study.getId());
 			assertTrue(associationCollection != null && associationCollection.size() > 0);
 
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
 
 	}
 
 	/**
 	 * Purpose is to test the self referencing of the entity.
 	 * Scenario - user(*)------>(1)User
 	 *                   creator
 	 */
 	public void testCreateEntityWithSelfReferencingBidirectionManyToManyAssociation()
 	{
 		EntityManagerInterface entityManager = EntityManager.getInstance();
 		DomainObjectFactory factory = DomainObjectFactory.getInstance();
 		EntityGroupInterface entityGroup = factory.createEntityGroup();
 		entityGroup.setName("test_" + new Double(Math.random()).toString());
 
 		// create user
 		EntityInterface user = createAndPopulateEntity();
 		AttributeInterface userNameAttribute = factory.createStringAttribute();
 		userNameAttribute.setName("user name");
 		user.setName("user");
 		user.addAbstractAttribute(userNameAttribute);
 
 		// Associate user (*)------ >(1)user
 		AssociationInterface association = factory.createAssociation();
 		association.setEntity(user);
 		try
 		{
 			association.setTargetEntity(user);
 			association.setAssociationDirection(AssociationDirection.BI_DIRECTIONAL);
 			association.setName("primaryInvestigator");
 			association.setSourceRole(getRole(AssociationType.ASSOCIATION, "subpi",
 					Cardinality.ONE, Cardinality.MANY));
 			association.setTargetRole(getRole(AssociationType.ASSOCIATION, "pi", Cardinality.ONE,
 					Cardinality.MANY));
 
 			user.addAbstractAttribute(association);
 			DynamicExtensionsUtility.getConstraintPropertiesForAssociation(association);
 			entityGroup.addEntity(user);
 			user.setEntityGroup(entityGroup);
 
 			EntityInterface savedUser = entityManager.persistEntity(user);
 
 			String tableName = user.getTableProperties().getName();
 
 			assertNotNull(tableName);
 
 			assertEquals(noOfDefaultColumns + 1, getColumnCount("select * from " + tableName));
 
 			assertEquals(noOfDefaultColumns + 1, getColumnCount("select * from "
 					+ association.getConstraintProperties().getName()));
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
 
 	}
 
 	/**
 	 * This test case test for adding a new association bet 2 entities
 	 *
 	 * for oracle it should throw exception.
 	 * for mysql  it works.
 	 */
 	public void testAddAssociationAfterSave()
 	{
 		EntityManagerInterface entityManager = EntityManager.getInstance();
 		DomainObjectFactory factory = DomainObjectFactory.getInstance();
 		EntityGroupInterface entityGroup = factory.createEntityGroup();
 		entityGroup.setName("test_" + new Double(Math.random()).toString());
 
 		// create user
 		EntityInterface user = createAndPopulateEntity();
 		AttributeInterface userNameAttribute = factory.createStringAttribute();
 		userNameAttribute.setName("user name");
 		user.setName("user");
 		user.addAbstractAttribute(userNameAttribute);
 		entityGroup.addEntity(user);
 		user.setEntityGroup(entityGroup);
 		try
 		{
 			user = entityManager.persistEntity(user);
 
 			// create study
 			EntityInterface study = createAndPopulateEntity();
 			AttributeInterface studyNameAttribute = factory.createStringAttribute();
 			studyNameAttribute.setName("study name");
 			study.setName("study");
 			study.addAbstractAttribute(studyNameAttribute);
 			entityGroup.addEntity(study);
 			study.setEntityGroup(entityGroup);
 
 			// Associate user (1)------ >(*)study
 			AssociationInterface association = factory.createAssociation();
 			association.setEntity(user);
 			association.setTargetEntity(study);
 
 			association.setName("primaryInvestigator");
 			association.setSourceRole(getRole(AssociationType.ASSOCIATION, "primaryInvestigator",
 					Cardinality.ZERO, Cardinality.MANY));
 			association.setTargetRole(getRole(AssociationType.ASSOCIATION, "study",
 					Cardinality.ZERO, Cardinality.ONE));
 			association.setAssociationDirection(AssociationDirection.BI_DIRECTIONAL);
 			DynamicExtensionsUtility.getConstraintPropertiesForAssociation(association);
 			user.addAbstractAttribute(association);
 
 			EntityInterface savedUser = entityManager.persistEntity(user);
 
 			assertEquals(noOfDefaultColumns + 2, getColumnCount("select * from "
 					+ user.getTableProperties().getName()));
 
 			//			 Associate user (1) <------>(*)study
 			AssociationInterface association1 = factory.createAssociation();
 			association1.setEntity(user);
 			association1.setTargetEntity(study);
 
 			association1.setName("primaryInvestigator");
 			association1.setSourceRole(getRole(AssociationType.ASSOCIATION, "association1",
 					Cardinality.ZERO, Cardinality.ONE));
 			association1.setTargetRole(getRole(AssociationType.ASSOCIATION, "association1",
 					Cardinality.ZERO, Cardinality.MANY));
 			association1.setAssociationDirection(AssociationDirection.BI_DIRECTIONAL);
 			DynamicExtensionsUtility.getConstraintPropertiesForAssociation(association1);
 			savedUser.addAbstractAttribute(association1);
 
 			savedUser = entityManager.persistEntity(savedUser);
 
 			assertEquals(noOfDefaultColumns + 2, getColumnCount("select * from "
 					+ study.getTableProperties().getName()));
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
 
 	}
 
 	/**
 	 * This test case test for removing an association bet 2 existing entities
 	 */
 	public void testRemoveAssociationAfterSave()
 	{
 		EntityManagerInterface entityManager = EntityManager.getInstance();
 		DomainObjectFactory factory = DomainObjectFactory.getInstance();
 		EntityGroupInterface entityGroup = factory.createEntityGroup();
 		entityGroup.setName("test_" + new Double(Math.random()).toString());
 
 		// create user
 		EntityInterface user = createAndPopulateEntity();
 		AttributeInterface userNameAttribute = factory.createStringAttribute();
 		userNameAttribute.setName("user name");
 		user.setName("user");
 		user.addAbstractAttribute(userNameAttribute);
 		entityGroup.addEntity(user);
 		user.setEntityGroup(entityGroup);
 
 		try
 		{
 			user = entityManager.persistEntity(user);
 
 			// create study
 			EntityInterface study = createAndPopulateEntity();
 			AttributeInterface studyNameAttribute = factory.createStringAttribute();
 			studyNameAttribute.setName("study name");
 			study.setName("study");
 			study.addAbstractAttribute(studyNameAttribute);
 			entityGroup.addEntity(study);
 			study.setEntityGroup(entityGroup);
 			// Associate user (1)------ >(*)study
 			RoleInterface sourceRole = getRole(AssociationType.ASSOCIATION, "association1",
 					Cardinality.ZERO, Cardinality.MANY);
 			RoleInterface targetRole = getRole(AssociationType.ASSOCIATION, "association1",
 					Cardinality.ZERO, Cardinality.ONE);
 			AssociationInterface association = getAssociation(study, user,
 					AssociationDirection.SRC_DESTINATION, "prim", sourceRole, targetRole);
 
 			user.addAbstractAttribute(association);
 			user = entityManager.persistEntity(user);
 
 			assertEquals(noOfDefaultColumns + 2, getColumnCount("select * from "
 					+ user.getTableProperties().getName()));
 
 			user.removeAssociation(association);
 			user = entityManager.persistEntity(user);
 
 			assertEquals(noOfDefaultColumns + 1, getColumnCount("select * from "
 					+ user.getTableProperties().getName()));
 
 			sourceRole = getRole(AssociationType.ASSOCIATION, "association1", Cardinality.ZERO,
 					Cardinality.MANY);
 			targetRole = getRole(AssociationType.ASSOCIATION, "association1", Cardinality.ZERO,
 					Cardinality.MANY);
 			association = getAssociation(study, user, AssociationDirection.SRC_DESTINATION, "prim",
 					sourceRole, targetRole);
 
 			user.addAbstractAttribute(association);
 			user = entityManager.persistEntity(user);
 
 			assertTrue(isTablePresent(association.getConstraintProperties().getName()));
 			assertEquals(noOfDefaultColumns + 1, getColumnCount("select * from "
 					+ association.getConstraintProperties().getName()));
 
 			user.removeAssociation(association);
 			user = entityManager.persistEntity(user);
 
 			assertFalse(isTablePresent(association.getConstraintProperties().getName()));
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
 
 	}
 
 	/**
 	 * This test case test for removing an association bet 2 existing entities
 	 * Before- SRC-DESTINATION
 	 * After - BIDIRECTIONAL
 	 */
 	public void testEditAssociationDirection1AfterSave()
 	{
 		EntityManagerInterface entityManager = EntityManager.getInstance();
 		DomainObjectFactory factory = DomainObjectFactory.getInstance();
 		EntityGroupInterface entityGroup = factory.createEntityGroup();
 		entityGroup.setName("test_" + new Double(Math.random()).toString());
 
 		// create user
 		EntityInterface user = createAndPopulateEntity();
 		AttributeInterface userNameAttribute = factory.createStringAttribute();
 		userNameAttribute.setName("user name");
 		user.setName("user");
 		user.addAbstractAttribute(userNameAttribute);
 
 		// create study
 		EntityInterface study = createAndPopulateEntity();
 		AttributeInterface studyNameAttribute = factory.createStringAttribute();
 		studyNameAttribute.setName("study name");
 		study.setName("study");
 		study.addAbstractAttribute(studyNameAttribute);
 		try
 		{
 			// Associate user (1)------ >(*)study
 			RoleInterface sourceRole = getRole(AssociationType.ASSOCIATION, "association1",
 					Cardinality.ZERO, Cardinality.MANY);
 			RoleInterface targetRole = getRole(AssociationType.ASSOCIATION, "association1",
 					Cardinality.ZERO, Cardinality.ONE);
 			AssociationInterface association = getAssociation(user, study,
 					AssociationDirection.SRC_DESTINATION, "prim", sourceRole, targetRole);
 
 			user.addAbstractAttribute(association);
 			entityGroup.addEntity(user);
 			user.setEntityGroup(entityGroup);
 			entityGroup.addEntity(study);
 			study.setEntityGroup(entityGroup);
 
 			user = entityManager.persistEntity(user);
 
 			assertEquals(noOfDefaultColumns + 2, getColumnCount("select * from "
 					+ user.getTableProperties().getName()));
 
 			assertEquals(noOfDefaultColumns + 1, getColumnCount("select * from "
 					+ study.getTableProperties().getName()));
 
 			association.getSourceRole().setMaximumCardinality(Cardinality.ONE);
 			association.getTargetRole().setMaximumCardinality(Cardinality.MANY);
 			association.setAssociationDirection(AssociationDirection.BI_DIRECTIONAL);
 			DynamicExtensionsUtility.getConstraintPropertiesForAssociation(association);
 			user = entityManager.persistEntity(user);
 
			assertEquals(noOfDefaultColumns + 1, getColumnCount("select * from "
					+ user.getTableProperties().getName()));
 			assertEquals(noOfDefaultColumns + 2, getColumnCount("select * from "
 					+ study.getTableProperties().getName()));
 			assertEquals(noOfDefaultColumns + 2, getColumnCount("select * from "
 					+ study.getTableProperties().getName()));
 
 			EntityInterface savedUser = entityManager
 					.getEntityByIdentifier(user.getId().toString());
 			assertEquals(1, savedUser.getAssociationCollection().size());
 
 			EntityInterface savedStudy = entityManager.getEntityByIdentifier(study.getId()
 					.toString());
 			assertEquals(1, savedStudy.getAssociationCollection().size());
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
 
 	}
 
 	/**
 	 * This test case test for removing an association bet 2 existing entities
 	 * Before-  BIDIRECTIONAL
 	 * After -  SRC-DESTINATION
 	 * system generated association should get removed.
 	 */
 	public void testEditAssociationDirectionAfterSave()
 	{
 		EntityManagerInterface entityManager = EntityManager.getInstance();
 		DomainObjectFactory factory = DomainObjectFactory.getInstance();
 		EntityGroupInterface entityGroup = factory.createEntityGroup();
 		entityGroup.setName("test_" + new Double(Math.random()).toString());
 		// create user
 		EntityInterface user = createAndPopulateEntity();
 		AttributeInterface userNameAttribute = factory.createStringAttribute();
 		userNameAttribute.setName("user name");
 		user.setName("user");
 		user.addAbstractAttribute(userNameAttribute);
 
 		// create study
 		EntityInterface study = createAndPopulateEntity();
 		AttributeInterface studyNameAttribute = factory.createStringAttribute();
 		studyNameAttribute.setName("study name");
 		study.setName("study");
 		study.addAbstractAttribute(studyNameAttribute);
 
 		// Associate user (1)------ >(*)study
 		try
 		{
 			RoleInterface sourceRole = getRole(AssociationType.ASSOCIATION, "association1",
 					Cardinality.ZERO, Cardinality.MANY);
 			RoleInterface targetRole = getRole(AssociationType.ASSOCIATION, "association1",
 					Cardinality.ZERO, Cardinality.ONE);
 			AssociationInterface association = getAssociation(study, user,
 					AssociationDirection.BI_DIRECTIONAL, "prim", sourceRole, targetRole);
 
 			user.addAbstractAttribute(association);
 			entityGroup.addEntity(user);
 			user.setEntityGroup(entityGroup);
 			entityGroup.addEntity(study);
 			study.setEntityGroup(entityGroup);
 
 			user = entityManager.persistEntity(user);
 
 			assertEquals(noOfDefaultColumns + 2, getColumnCount("select * from "
 					+ user.getTableProperties().getName()));
 
 			assertEquals(noOfDefaultColumns + 1, getColumnCount("select * from "
 					+ study.getTableProperties().getName()));
 			association.getSourceRole().setMaximumCardinality(Cardinality.ONE);
 			association.getTargetRole().setMaximumCardinality(Cardinality.MANY);
 			association.setAssociationDirection(AssociationDirection.SRC_DESTINATION);
 			DynamicExtensionsUtility.getConstraintPropertiesForAssociation(association);
 
 			user = entityManager.persistEntity(user);
 
 			assertEquals(noOfDefaultColumns, getColumnCount("select * from "
 					+ user.getTableProperties().getName()));
 
 			assertEquals(noOfDefaultColumns + 2, getColumnCount("select * from "
 					+ study.getTableProperties().getName()));
 
 			EntityInterface savedUser = entityManager
 					.getEntityByIdentifier(user.getId().toString());
 			assertEquals(1, savedUser.getAssociationCollection().size());
 
 			EntityInterface savedStudy = entityManager.getEntityByIdentifier(study.getId()
 					.toString());
 			assertEquals(0, savedStudy.getAssociationCollection().size());
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
 
 	}
 
 	/**
 	 *  PURPOSE: This method test for inserting data for a containtment relationship between two entities
 	 *  EXPECTED BEHAVIOUR: Data should be persisted for the target entity in its own table and that record should
 	 *                      get associated with the source entity's record.
 	 *  TEST CASE FLOW: 1. create User
 	 *                  2. Create Address
 	 *                  3. Add Association with      User(1) ------->(1) Address containtment association
 	 *                  4. persist entities.
 	 *                  5. Insert Data
 	 *                  6. Check for it.
 	 */
 	public void testInsertDataForContaintmentOneToOne()
 	{
 
 		EntityManagerInterface entityManagerInterface = EntityManager.getInstance();
 		DomainObjectFactory factory = DomainObjectFactory.getInstance();
 		EntityGroupInterface entityGroup = factory.createEntityGroup();
 		entityGroup.setName("test_" + new Double(Math.random()).toString());
 		try
 		{
 
 			// Step 1
 			EntityInterface user = createAndPopulateEntity();
 			AttributeInterface userNameAttribute = factory.createStringAttribute();
 			userNameAttribute.setName("user name");
 			user.setName("user");
 			user.addAbstractAttribute(userNameAttribute);
 
 			// Step 2
 			EntityInterface address = createAndPopulateEntity();
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
 			DynamicExtensionsUtility.getConstraintPropertiesForAssociation(association);
 			entityGroup.addEntity(user);
 			user.setEntityGroup(entityGroup);
 			entityGroup.addEntity(address);
 			address.setEntityGroup(entityGroup);
 
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
 
 			// Step 6
 			int rowCount = (Integer) executeQuery("select count(*) from "
 					+ address.getTableProperties().getName(), INT_TYPE, 1);
 			assertEquals(1, rowCount);
 
 			rowCount = (Integer) executeQuery("select count(*) from "
 					+ user.getTableProperties().getName(), INT_TYPE, 1);
 			assertEquals(1, rowCount);
 		}
 		catch (Exception e)
 		{
 			Logger.out.debug(DynamicExtensionsUtility.getStackTrace(e));
 			fail();
 		}
 
 	}
 
 	/**
 	 *  PURPOSE: This method test for inserting data for a containtment relationship between two entities having one to many asso
 	 *  EXPECTED BEHAVIOUR: Data should be persisted for the target entity in its own table and that record should
 	 *                      get associated with the source entity's record.
 	 *  TEST CASE FLOW: 1. create User
 	 *                  2. Create Address
 	 *                  3. Add Association with      User(1) ------->(*) Address containment association
 	 *                  4. persist entities.
 	 *                  5. Insert Data
 	 *                  6. Data table for Address should have two records for the user.
 	 */
 	public void testInsertDataForContainmentOneToMany()
 	{
 
 		EntityManagerInterface entityManagerInterface = EntityManager.getInstance();
 		DomainObjectFactory factory = DomainObjectFactory.getInstance();
 		EntityGroupInterface entityGroup = factory.createEntityGroup();
 		entityGroup.setName("test_" + new Double(Math.random()).toString());
 		try
 		{
 
 			// Step 1
 			EntityInterface user = createAndPopulateEntity();
 			AttributeInterface userNameAttribute = factory.createStringAttribute();
 			userNameAttribute.setName("user name");
 			user.setName("user");
 			user.addAbstractAttribute(userNameAttribute);
 
 			// Step 2
 			EntityInterface address = createAndPopulateEntity();
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
 			DynamicExtensionsUtility.getConstraintPropertiesForAssociation(association);
 			entityGroup.addEntity(user);
 			user.setEntityGroup(entityGroup);
 			entityGroup.addEntity(address);
 			address.setEntityGroup(entityGroup);
 
 			// Step 4
 			EntityInterface savedEntity = entityManagerInterface.persistEntity(user);
 
 			Map dataValue = new HashMap();
 			Map addressDataValue1 = new HashMap();
 			addressDataValue1.put(streetAttribute, "Laxmi Road");
 			addressDataValue1.put(cityAttribute, "Pune");
 
 			Map addressDataValue2 = new HashMap();
 			addressDataValue2.put(streetAttribute, "MG Road");
 			addressDataValue2.put(cityAttribute, "Pune21");
 
 			List<Map> addressDataValueMapList = new ArrayList<Map>();
 			addressDataValueMapList.add(addressDataValue1);
 			addressDataValueMapList.add(addressDataValue2);
 
 			dataValue.put(userNameAttribute, "rahul");
 			dataValue.put(association, addressDataValueMapList);
 
 			// Step 5
 			entityManagerInterface.insertData(savedEntity, dataValue);
 
 			// Step 6
 			int rowCount = (Integer) executeQuery("select count(*) from "
 					+ address.getTableProperties().getName(), INT_TYPE, 1);
 			assertEquals(2, rowCount);
 
 			rowCount = (Integer) executeQuery("select count(*) from "
 					+ user.getTableProperties().getName(), INT_TYPE, 1);
 			assertEquals(1, rowCount);
 		}
 		catch (Exception e)
 		{
 			Logger.out.debug(DynamicExtensionsUtility.getStackTrace(e));
 			fail();
 		}
 
 	}
 
 	/**
 	 *  PURPOSE: This method test for editing data for a containtment relationship between two entities having one to one asso
 	 *  EXPECTED BEHAVIOUR: Data should be persisted for the target entity in its own table and that record should
 	 *                      get updated.
 	 *  TEST CASE FLOW: 1. create User
 	 *                  2. Create Address
 	 *                  3. Add Association with      User(1) ------->(1) Address containment association
 	 *                  4. persist entities.
 	 *                  5. Insert Data
 	 *                  6. Data table for Address should have the appropriate entry for the inserted data.
 	 *                  7. Change the data
 	 *                  8. Call editData method on entity manager.
 	 *                  9. Check if the data in the data table for address is updated or not for the selected record of user.
 	 */
 	public void testEditDataForContainmentOneToOne()
 	{
 
 		EntityManagerInterface entityManagerInterface = EntityManager.getInstance();
 		DomainObjectFactory factory = DomainObjectFactory.getInstance();
 		EntityGroupInterface entityGroup = factory.createEntityGroup();
 		entityGroup.setName("test_" + new Double(Math.random()).toString());
 		try
 		{
 
 			// Step 1
 			EntityInterface user = createAndPopulateEntity();
 			AttributeInterface userNameAttribute = factory.createStringAttribute();
 			userNameAttribute.setName("user name");
 			user.setName("user");
 			user.addAbstractAttribute(userNameAttribute);
 
 			// Step 2
 			EntityInterface address = createAndPopulateEntity();
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
 			DynamicExtensionsUtility.getConstraintPropertiesForAssociation(association);
 			entityGroup.addEntity(user);
 			user.setEntityGroup(entityGroup);
 			entityGroup.addEntity(address);
 			address.setEntityGroup(entityGroup);
 			// Step 4
 			EntityInterface savedEntity = entityManagerInterface.persistEntity(user);
 
 			Map dataValue = new HashMap();
 			Map addressDataValue1 = new HashMap();
 			addressDataValue1.put(streetAttribute, "Laxmi Road");
 			addressDataValue1.put(cityAttribute, "Pune");
 
 			List<Map> addressDataValueMapList = new ArrayList<Map>();
 			addressDataValueMapList.add(addressDataValue1);
 
 			dataValue.put(userNameAttribute, "rahul");
 			dataValue.put(association, addressDataValueMapList);
 
 			// Step 5
 			Long recordId = entityManagerInterface.insertData(savedEntity, dataValue);
 
 			// Step 6
 			int rowCount = (Integer) executeQuery("select count(*) from "
 					+ address.getTableProperties().getName(), INT_TYPE, 1);
 			assertEquals(1, rowCount);
 
 			rowCount = (Integer) executeQuery("select count(*) from "
 					+ user.getTableProperties().getName(), INT_TYPE, 1);
 			assertEquals(1, rowCount);
 
 			addressDataValue1.clear();
 			// Step 7
 			addressDataValue1.put(streetAttribute, "Swami Vivekanand Road");
 			addressDataValue1.put(cityAttribute, "Pune 37");
 
 			// Step 8
 			entityManagerInterface.editData(savedEntity, dataValue, recordId);
 
 			// Step 9
 			rowCount = (Integer) executeQuery("select count(*) from "
 					+ address.getTableProperties().getName(), INT_TYPE, 1);
 			assertEquals(1, rowCount);
 
 			rowCount = (Integer) executeQuery("select count(*) from "
 					+ user.getTableProperties().getName(), INT_TYPE, 1);
 			assertEquals(1, rowCount);
 
 		}
 		catch (Exception e)
 		{
 			Logger.out.debug(DynamicExtensionsUtility.getStackTrace(e));
 			fail();
 		}
 
 	}
 
 	/**
 	 *  PURPOSE: This method test for editing data for a containtment relationship between two entities
 	 *  having one to many association
 	 *  EXPECTED BEHAVIOUR: Data should be persisted for the target entity in its own table and that record should
 	 *                      get updated.
 	 *  TEST CASE FLOW: 1. create User
 	 *                  2. Create Address
 	 *                  3. Add Association with      User(1) ------->(*) Address containment association
 	 *                  4. persist entities.
 	 *                  5. Insert Data with multiple addresses for the user
 	 *                  6. Data table for Address should have the appropriate entries for the inserted data for user.
 	 *                  7. Change the data
 	 *                  8. Call editData method on entity manager.
 	 *                  9. Check if the data in the data table for address is updated or not for the selected record of user.
 	 */
 	public void testEditDataForContainmentOneToMany()
 	{
 
 		EntityManagerInterface entityManagerInterface = EntityManager.getInstance();
 		DomainObjectFactory factory = DomainObjectFactory.getInstance();
 		EntityGroupInterface entityGroup = factory.createEntityGroup();
 		entityGroup.setName("test_" + new Double(Math.random()).toString());
 		try
 		{
 
 			// Step 1
 			EntityInterface user = createAndPopulateEntity();
 			AttributeInterface userNameAttribute = factory.createStringAttribute();
 			userNameAttribute.setName("user name");
 			user.setName("user");
 			user.addAbstractAttribute(userNameAttribute);
 
 			// Step 2
 			EntityInterface address = createAndPopulateEntity();
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
 			DynamicExtensionsUtility.getConstraintPropertiesForAssociation(association);
 			entityGroup.addEntity(user);
 			user.setEntityGroup(entityGroup);
 			entityGroup.addEntity(address);
 			address.setEntityGroup(entityGroup);
 			// Step 4
 			EntityInterface savedEntity = entityManagerInterface.persistEntity(user);
 
 			Map dataValue = new HashMap();
 			Map addressDataValue1 = new HashMap();
 			addressDataValue1.put(streetAttribute, "Laxmi Road");
 			addressDataValue1.put(cityAttribute, "Pune");
 
 			Map addressDataValue2 = new HashMap();
 			addressDataValue2.put(streetAttribute, "Saraswati Road");
 			addressDataValue2.put(cityAttribute, "Pune");
 
 			List<Map> addressDataValueMapList = new ArrayList<Map>();
 			addressDataValueMapList.add(addressDataValue1);
 			addressDataValueMapList.add(addressDataValue2);
 			dataValue.put(userNameAttribute, "rahul");
 			dataValue.put(association, addressDataValueMapList);
 
 			// Step 5
 			Long recordId = entityManagerInterface.insertData(savedEntity, dataValue);
 
 			// Step 6
 			int rowCount = (Integer) executeQuery("select count(*) from "
 					+ address.getTableProperties().getName(), INT_TYPE, 1);
 
 			assertEquals(2, rowCount);
 
 			rowCount = (Integer) executeQuery("select count(*) from "
 					+ user.getTableProperties().getName(), INT_TYPE, 1);
 			assertEquals(1, rowCount);
 
 			addressDataValue1.clear();
 			addressDataValueMapList.clear();
 
 			// Step 7
 			addressDataValue1.put(streetAttribute, "Swami Vivekand Road");
 			addressDataValue1.put(cityAttribute, "Pune 37");
 			addressDataValueMapList.add(addressDataValue1);
 
 			// Step 8
 			entityManagerInterface.editData(savedEntity, dataValue, recordId);
 
 			// Step 9
 			rowCount = (Integer) executeQuery("select count(*) from "
 					+ address.getTableProperties().getName(), INT_TYPE, 1);
 			assertEquals(1, rowCount);
 
 			String value = (String) executeQuery("select * from "
 					+ address.getTableProperties().getName(), STRING_TYPE, 3);
 			assertEquals("Swami Vivekand Road", value);
 
 			value = (String) executeQuery(
 					"select * from " + address.getTableProperties().getName(), STRING_TYPE, 4);
 			assertEquals("Pune 37", value);
 
 			rowCount = (Integer) executeQuery("select count(*) from "
 					+ user.getTableProperties().getName(), INT_TYPE, 1);
 
 			assertEquals(1, rowCount);
 
 		}
 		catch (Exception e)
 		{
 			Logger.out.debug(DynamicExtensionsUtility.getStackTrace(e));
 			fail();
 		}
 
 	}
 
 	/**
 	 *  PURPOSE: This method test for deleting data for a containtment relationship between two entities
 	 *  having one to many association
 	 *  EXPECTED BEHAVIOUR: Data should be removed from the data tables of the source and target entity
 	 *  TEST CASE FLOW: 1. create User
 	 *                  2. Create Address
 	 *                  3. Add Association with      User(1) ------->(*) Address containment association
 	 *                  4. persist entities.
 	 *                  5. Insert Data with multiple addresses for the user
 	 *                  6. Data table for Address should have the appropriate entries for the inserted data for user.
 	 *                  7. Call deleteData method on entity manager for the inserted data.
 	 *                  8. Check if the data is removed from both the tables
 	 *                  */
 	public void testDeleteDataForContainmentOneToMany()
 	{
 
 		EntityManagerInterface entityManagerInterface = EntityManager.getInstance();
 		DomainObjectFactory factory = DomainObjectFactory.getInstance();
 		EntityGroupInterface entityGroup = factory.createEntityGroup();
 		entityGroup.setName("test_" + new Double(Math.random()).toString());
 		try
 		{
 
 			// Step 1
 			EntityInterface user = createAndPopulateEntity();
 			AttributeInterface userNameAttribute = factory.createStringAttribute();
 			userNameAttribute.setName("user name");
 			user.setName("user");
 			user.addAbstractAttribute(userNameAttribute);
 
 			// Step 2
 			EntityInterface address = createAndPopulateEntity();
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
 			DynamicExtensionsUtility.getConstraintPropertiesForAssociation(association);
 			entityGroup.addEntity(user);
 			user.setEntityGroup(entityGroup);
 			entityGroup.addEntity(address);
 			address.setEntityGroup(entityGroup);
 			// Step 4
 			EntityInterface savedEntity = entityManagerInterface.persistEntity(user);
 
 			Map dataValue = new HashMap();
 			Map addressDataValue1 = new HashMap();
 			addressDataValue1.put(streetAttribute, "Laxmi Road");
 			addressDataValue1.put(cityAttribute, "Pune");
 
 			Map addressDataValue2 = new HashMap();
 			addressDataValue2.put(streetAttribute, "Saraswati Road");
 			addressDataValue2.put(cityAttribute, "Pune");
 
 			List<Map> addressDataValueMapList = new ArrayList<Map>();
 			addressDataValueMapList.add(addressDataValue1);
 			addressDataValueMapList.add(addressDataValue2);
 			dataValue.put(userNameAttribute, "rahul");
 			dataValue.put(association, addressDataValueMapList);
 
 			// Step 5
 			Long recordId = entityManagerInterface.insertData(savedEntity, dataValue);
 
 			// Step 6
 			int rowCount = (Integer) executeQuery("select count(*) from "
 					+ address.getTableProperties().getName(), INT_TYPE, 1);
 			assertEquals(2, rowCount);
 
 			rowCount = (Integer) executeQuery("select count(*) from "
 					+ user.getTableProperties().getName(), INT_TYPE, 1);
 			assertEquals(1, rowCount);
 
 			// Step 7
 			entityManagerInterface.deleteRecord(savedEntity, recordId);
 
 			// Step 8
 			rowCount = (Integer) executeQuery("select count(*) from "
 					+ user.getTableProperties().getName(), INT_TYPE, 1);
 			assertEquals(1, rowCount);
 
 			rowCount = (Integer) executeQuery("select count(*) from "
 					+ address.getTableProperties().getName(), INT_TYPE, 1);
 			assertEquals(2, rowCount);
 
 		}
 		catch (Exception e)
 		{
 			Logger.out.debug(DynamicExtensionsUtility.getStackTrace(e));
 			fail();
 		}
 
 	}
 
 	//	/**
 	//	 *  PURPOSE: This method tests for creation of all the hierarchy for containment control
 	//	 *  having one to many association
 	//	 *  EXPECTED BEHAVIOUR: All the hierarchy should get saved
 	//	 *  TEST CASE FLOW: 1. create User
 	//	 *                  2. Create Address
 	//	 *                  3. Add Association with      User(1) ------->(1) Address containment association
 	//	 *                  4. Add containment control to user container with address container inside this control.
 	//	 *                  5. persist entities.
 	//	 *                  6. Check if the address container is saved or not.
 	//	 */
 	//	public void testCreateContainerFromContainmentAssociation()
 	//	{
 	//		try
 	//		{
 	//			//Step 1
 	//			Container userContainer = (Container) DomainObjectFactory.getInstance()
 	//					.createContainer();
 	//
 	//			EntityInterface user = DomainObjectFactory.getInstance().createEntity();
 	//			user.setName("USER");
 	//			AttributeInterface nameAttribute = new MockEntityManager().initializeStringAttribute(
 	//					"name", "new name");
 	//
 	//			ControlInterface textbox = DomainObjectFactory.getInstance().createTextField();
 	//			textbox.setAbstractAttribute(nameAttribute);
 	//			userContainer.addControl(textbox);
 	//			user.addAbstractAttribute(nameAttribute);
 	//
 	//			userContainer.setEntity(user);
 	//
 	//			//Step 2
 	//			EntityInterface address = DomainObjectFactory.getInstance().createEntity();
 	//			address.setName("ADDRESS");
 	//			AttributeInterface cityAttribute = new MockEntityManager().initializeStringAttribute(
 	//					"City", "Ahmednagar");
 	//
 	//			Container addressContainer = (Container) DomainObjectFactory.getInstance()
 	//					.createContainer();
 	//
 	//			ControlInterface cityTextbox = DomainObjectFactory.getInstance().createTextField();
 	//			cityTextbox.setAbstractAttribute(cityAttribute);
 	//			addressContainer.addControl(cityTextbox);
 	//			address.addAbstractAttribute(cityAttribute);
 	//
 	//			addressContainer.setEntity(address);
 	//
 	//			// Step 3 Associate user (1)------ >(1)address
 	//			RoleInterface sourceRole = getRole(AssociationType.CONTAINTMENT, "address",
 	//					Cardinality.ZERO, Cardinality.ONE);
 	//			RoleInterface targetRole = getRole(AssociationType.ASSOCIATION, "user",
 	//					Cardinality.ZERO, Cardinality.ONE);
 	//			AssociationInterface association = getAssociation(address,
 	//					AssociationDirection.SRC_DESTINATION, "userAddress", sourceRole, targetRole);
 	//
 	//			user.addAbstractAttribute(association);
 	//
 	//			//Step 4
 	//			ContainmentAssociationControlInterface containmentAssociationControlInterface = DomainObjectFactory
 	//					.getInstance().createContainmentAssociationControl();
 	//
 	//			containmentAssociationControlInterface.setContainer(addressContainer);
 	//
 	//			userContainer.addControl(containmentAssociationControlInterface);
 	//			//Step 5
 	//			EntityManager.getInstance().persistContainer(userContainer);
 	//			Collection list = EntityManager.getInstance().getAllContainers();
 	//			assertNotNull(list);
 	//			Iterator iter = list.iterator();
 	//			boolean flag = false;
 	//			//Step 6
 	//			while (iter.hasNext())
 	//			{
 	//				Container cont = (Container) iter.next();
 	//				if (cont.getId().equals(addressContainer.getId()))
 	//				{
 	//					flag = true;
 	//					break;
 	//				}
 	//			}
 	//			assertTrue(flag);
 	//		}
 	//		catch (Exception e)
 	//		{
 	//			//TODO Auto-generated catch block
 	//			Logger.out.debug(e.getMessage());
 	//			fail("Exception occured");
 	//		}
 	//
 	//	}
 
 	/**
 	 *
 	 */
 	public void testGetAssociationById()
 	{
 		EntityManagerInterface entityManager = EntityManager.getInstance();
 		DomainObjectFactory factory = DomainObjectFactory.getInstance();
 		EntityGroupInterface entityGroup = factory.createEntityGroup();
 		entityGroup.setName("test_" + new Double(Math.random()).toString());
 		// create user
 		EntityInterface user = createAndPopulateEntity();
 		AttributeInterface userNameAttribute = factory.createStringAttribute();
 		userNameAttribute.setName("user name");
 		user.setName("user");
 		user.addAbstractAttribute(userNameAttribute);
 
 		// create study
 		EntityInterface study = createAndPopulateEntity();
 		AttributeInterface studyNameAttribute = factory.createStringAttribute();
 		studyNameAttribute.setName("study name");
 		study.setName("study");
 		study.addAbstractAttribute(studyNameAttribute);
 
 		// Associate user (1)------ >(*)study
 		AssociationInterface association = factory.createAssociation();
 		try
 		{
 			association.setTargetEntity(study);
 			association.setAssociationDirection(AssociationDirection.SRC_DESTINATION);
 			association.setName("primaryInvestigator");
 			association.setSourceRole(getRole(AssociationType.ASSOCIATION, "primaryInvestigator",
 					Cardinality.ZERO, Cardinality.MANY));
 			association.setTargetRole(getRole(AssociationType.ASSOCIATION, "study",
 					Cardinality.ZERO, Cardinality.ONE));
 
 			user.addAbstractAttribute(association);
 			DynamicExtensionsUtility.getConstraintPropertiesForAssociation(association);
 			entityGroup.addEntity(user);
 			user.setEntityGroup(entityGroup);
 			entityGroup.addEntity(study);
 			study.setEntityGroup(entityGroup);
 
 			//entityManager.createEntity(study);
 
 			EntityInterface savedUser = entityManager.persistEntity(user);
 
 			System.out.println();
 
 			AssociationInterface saveAssociation = entityManager
 					.getAssociationByIdentifier(association.getId());
 
 			assertEquals(association.getName(), saveAssociation.getName());
 
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
 
 	}
 
 	/**
 	 *
 	 */
 	public void testGetAssociationByIdNotPresent()
 	{
 		EntityManagerInterface entityManager = EntityManager.getInstance();
 		DomainObjectFactory factory = DomainObjectFactory.getInstance();
 
 		try
 		{
 			AssociationInterface saveAssociation = entityManager
 					.getAssociationByIdentifier(123456L);
 		}
 		catch (DynamicExtensionsSystemException e)
 		{
 			e.printStackTrace();
 			fail();
 		}
 		catch (DynamicExtensionsApplicationException e)
 		{
 			e.printStackTrace();
 			assertTrue(true);
 		}
 	}
 
 	/**
 	 *  PURPOSE: This method tests for editing the data for the case when multiple level containment is
 	 *           present.    (fix for the bug : 3289)
 	 *
 	 *  EXPECTED BEHAVIOUR: The data should be properly edited..
 	 *  TEST CASE FLOW: 1. create User
 	 *                  2. Create Institute
 	 *                  3. Create address
 	 *                  4. Add Association with      User(1) ------->(*) Institutes with containment association
 	 *                  5. Add Association with      Institutes(1) ------->(*) Address containment association
 	 *                  6. Add data for user  rahul -> Verizon --> Pune
 	 *                  7. edit data for the user  rahul --> PSPL -->Pune
 	 *                  8. Address should have only one record.. previous one should get deleted and new one should get added
 	 */
 	public void testEditDataWithContainmentForMultipleLevel()
 	{
 
 		EntityManagerInterface entityManager = EntityManager.getInstance();
 		DomainObjectFactory factory = DomainObjectFactory.getInstance();
 		EntityGroupInterface entityGroup = factory.createEntityGroup();
 		entityGroup.setName("test_" + new Double(Math.random()).toString());
 		try
 		{
 			//Step 1
 			EntityInterface user = createAndPopulateEntity();
 			AttributeInterface userNameAttribute = factory.createStringAttribute();
 			userNameAttribute.setName("user name");
 			user.setName("user");
 			user.addAbstractAttribute(userNameAttribute);
 			entityGroup.addEntity(user);
 			user.setEntityGroup(entityGroup);
 
 			entityManager.persistEntity(user);
 
 			//Step 2 create institute
 			EntityInterface institution = createAndPopulateEntity();
 			AttributeInterface institutionName = factory.createStringAttribute();
 			institutionName.setName("institution Name");
 			institution.setName("institution");
 			institution.addAbstractAttribute(institutionName);
 			entityGroup.addEntity(institution);
 			institution.setEntityGroup(entityGroup);
 
 			entityManager.persistEntity(institution);
 
 			//Step 3 create address
 			EntityInterface address = createAndPopulateEntity();
 			AttributeInterface addressCity = factory.createStringAttribute();
 			addressCity.setName("City");
 			address.setName("address");
 			address.addAbstractAttribute(addressCity);
 			entityGroup.addEntity(address);
 			address.setEntityGroup(entityGroup);
 			entityManager.persistEntity(address);
 
 			//Step 4 user --- > institute
 			AssociationInterface userInstitute = factory.createAssociation();
 
 			userInstitute.setTargetEntity(institution);
 			userInstitute.setAssociationDirection(AssociationDirection.SRC_DESTINATION);
 			userInstitute.setName("userinstitution");
 			userInstitute.setSourceRole(getRole(AssociationType.CONTAINTMENT, "user",
 					Cardinality.ONE, Cardinality.ONE));
 			userInstitute.setTargetRole(getRole(AssociationType.CONTAINTMENT, "institution",
 					Cardinality.ONE, Cardinality.MANY));
 			user.addAbstractAttribute(userInstitute);
 			DynamicExtensionsUtility.getConstraintPropertiesForAssociation(userInstitute);
 
 			entityManager.persistEntity(user);
 
 			//Step 5 institute -->address
 			AssociationInterface instituteAddress = factory.createAssociation();
 
 			instituteAddress.setTargetEntity(address);
 			instituteAddress.setAssociationDirection(AssociationDirection.SRC_DESTINATION);
 			instituteAddress.setName("instituteAddress");
 			instituteAddress.setSourceRole(getRole(AssociationType.CONTAINTMENT,
 					"instituteAddress", Cardinality.ONE, Cardinality.ONE));
 			instituteAddress.setTargetRole(getRole(AssociationType.CONTAINTMENT, "address",
 					Cardinality.ONE, Cardinality.MANY));
 
 			institution.addAbstractAttribute(instituteAddress);
 			DynamicExtensionsUtility.getConstraintPropertiesForAssociation(instituteAddress);
 
 			entityManager.persistEntity(institution);
 
 			//Step 6
 			Map addressValueMap = new HashMap();
 			addressValueMap.put(addressCity, "Pune");
 
 			Map institutionValueMap = new HashMap();
 			List addressList = new ArrayList();
 			addressList.add(addressValueMap);
 
 			institutionValueMap.put(institutionName, "verizon");
 			institutionValueMap.put(instituteAddress, addressList);
 
 			//			Map institutionValueMap1 = new HashMap();
 			//			institutionValueMap1.put(institutionName,"pspl");
 
 			Map dataValue = new HashMap();
 			List institutionList = new ArrayList();
 			institutionList.add(institutionValueMap);
 			//instituionList.add(institutionValueMap1);
 
 			dataValue.put(userNameAttribute, "Rahul");
 			dataValue.put(userInstitute, institutionList);
 
 			Long recordId = entityManager.insertData(user, dataValue);
 
 			//Step 7
 			institutionValueMap.put(institutionName, "PSPL");
 			entityManager.editData(user, dataValue, recordId);
 
 			//step 8
 			int rowCount = (Integer) executeQuery("select count(*) from "
 					+ address.getTableProperties().getName(), INT_TYPE, 1);
 			assertEquals(1, rowCount);
 
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 			fail();
 		}
 
 	}
 
 	/**
 	 * This test case test for associating two entities with one to many association
 	 *
 	 * for oracle it should throw exception.
 	 * for mysql  it works.
 	 */
 	public void testGetIncomingAssociationsForEntity()
 	{
 		EntityManagerInterface entityManager = EntityManager.getInstance();
 		DomainObjectFactory factory = DomainObjectFactory.getInstance();
 		EntityGroupInterface entityGroup = factory.createEntityGroup();
 		entityGroup.setName("test_" + new Double(Math.random()).toString());
 		// create user
 		EntityInterface user = createAndPopulateEntity();
 		AttributeInterface userNameAttribute = factory.createStringAttribute();
 		userNameAttribute.setName("user name");
 		user.setName("user");
 		user.addAbstractAttribute(userNameAttribute);
 		entityGroup.addEntity(user);
 		user.setEntityGroup(entityGroup);
 		// create study
 		EntityInterface study = createAndPopulateEntity();
 		AttributeInterface studyNameAttribute = factory.createStringAttribute();
 		studyNameAttribute.setName("study name");
 		study.setName("study");
 		study.addAbstractAttribute(studyNameAttribute);
 		entityGroup.addEntity(study);
 		study.setEntityGroup(entityGroup);
 		// Associate user (1)------ >(*)study
 		AssociationInterface association = factory.createAssociation();
 		try
 		{
 			association.setTargetEntity(study);
 			association.setAssociationDirection(AssociationDirection.SRC_DESTINATION);
 			association.setName("primaryInvestigator");
 			association.setSourceRole(getRole(AssociationType.ASSOCIATION, "primaryInvestigator",
 					Cardinality.ONE, Cardinality.ONE));
 			association.setTargetRole(getRole(AssociationType.ASSOCIATION, "study",
 					Cardinality.ZERO, Cardinality.MANY));
 
 			user.addAbstractAttribute(association);
 			DynamicExtensionsUtility.getConstraintPropertiesForAssociation(association);
 
 			// create site
 			EntityInterface site = createAndPopulateEntity();
 			AttributeInterface siteNameAttribute = factory.createStringAttribute();
 			siteNameAttribute.setName("site name");
 			site.setName("site");
 			site.addAbstractAttribute(siteNameAttribute);
 			entityGroup.addEntity(site);
 			site.setEntityGroup(entityGroup);
 			// Associate site (1)------ >(*)study
 			AssociationInterface associationSite = factory.createAssociation();
 
 			associationSite.setTargetEntity(study);
 			associationSite.setAssociationDirection(AssociationDirection.SRC_DESTINATION);
 			associationSite.setName("site_study");
 			associationSite.setSourceRole(getRole(AssociationType.ASSOCIATION, "site",
 					Cardinality.ONE, Cardinality.ONE));
 			associationSite.setTargetRole(getRole(AssociationType.ASSOCIATION, "study",
 					Cardinality.ZERO, Cardinality.MANY));
 
 			site.addAbstractAttribute(associationSite);
 			DynamicExtensionsUtility.getConstraintPropertiesForAssociation(associationSite);
 
 			//entityManager.createEntity(study);
 
 			entityManager.persistEntity(user);
 			site = entityManager.persistEntity(site);
 
 			Collection<AssociationInterface> coll = entityManager.getIncomingAssociations(study);
 			assertEquals(2, coll.size());
 
 			coll = entityManager.getIncomingAssociations(user);
 			assertEquals(0, coll.size());
 
 			coll = entityManager.getIncomingAssociations(site);
 			assertEquals(0, coll.size());
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
 
 	}
 
 	/**
 	 * @param targetEntity
 	 * @param associationDirection
 	 * @param assoName
 	 * @param sourceRole
 	 * @param targetRole
 	 * @return
 	 * @throws DynamicExtensionsSystemException 
 	 */
 	private AssociationInterface getAssociation(EntityInterface sourceEntity,
 			EntityInterface targetEntity, AssociationDirection associationDirection,
 			String assoName, RoleInterface sourceRole, RoleInterface targetRole)
 			throws DynamicExtensionsSystemException
 	{
 		AssociationInterface association = DomainObjectFactory.getInstance().createAssociation();
 		association.setTargetEntity(targetEntity);
 		association.setEntity(sourceEntity);
 		association.setName(assoName);
 		association.setSourceRole(sourceRole);
 		association.setTargetRole(targetRole);
 		association.setAssociationDirection(associationDirection);
 		DynamicExtensionsUtility.getConstraintPropertiesForAssociation(association);
 		return association;
 	}
 
 }
