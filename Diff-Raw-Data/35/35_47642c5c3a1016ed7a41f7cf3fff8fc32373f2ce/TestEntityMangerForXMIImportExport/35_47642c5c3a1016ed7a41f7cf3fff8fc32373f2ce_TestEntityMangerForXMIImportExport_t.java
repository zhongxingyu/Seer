 /**
  *<p>Title: </p>
  *<p>Description:  </p>
  *<p>Copyright://TODO</p>
  *@author Ashish Gupta
  *@version 1.0
  */
 
 package edu.common.dynamicextensions.entitymanager;
 
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 
 import edu.common.dynamicextensions.domain.DomainObjectFactory;
 import edu.common.dynamicextensions.domain.EntityGroup;
 import edu.common.dynamicextensions.domain.userinterface.Container;
 import edu.common.dynamicextensions.domaininterface.AssociationInterface;
 import edu.common.dynamicextensions.domaininterface.AttributeInterface;
 import edu.common.dynamicextensions.domaininterface.EntityInterface;
 import edu.common.dynamicextensions.domaininterface.RoleInterface;
 import edu.common.dynamicextensions.exception.DynamicExtensionsApplicationException;
 import edu.common.dynamicextensions.util.DynamicExtensionsBaseTestCase;
 import edu.common.dynamicextensions.util.global.DEConstants;
 import edu.common.dynamicextensions.util.global.DEConstants.AssociationDirection;
 import edu.common.dynamicextensions.util.global.DEConstants.AssociationType;
 import edu.common.dynamicextensions.util.global.DEConstants.Cardinality;
 import edu.common.dynamicextensions.xmi.exporter.XMIExporter;
 import edu.common.dynamicextensions.xmi.importer.XMIImporter;
 import edu.wustl.common.util.logger.Logger;
 import gov.nih.nci.cagrid.metadata.common.UMLAttribute;
 import gov.nih.nci.cagrid.metadata.common.UMLClass;
 import gov.nih.nci.cagrid.metadata.dataservice.UMLAssociation;
 
 public class TestEntityMangerForXMIImportExport extends DynamicExtensionsBaseTestCase
 {
 
 	/**
 	 * Specify the name of the domain model xmi file to import. This file must be present at the path test/ModelXML under the project root directory
 	 */
 	private String XMIFileName = "./xmi/newsurgery.xmi";
	private String ClinicalAnnotationXMI = "./xmi/ClinicalAnnotations.xmi";
 
 	/**
 	 *
 	 */
 	public void testXMIImport()
 	{
 		try
 		{
 			String[] args = {XMIFileName, "Surgery", "./csv/surgery.csv"};
 			XMIImporter.main(args);
 			//			DomainModelParser parser = getParser(XMIFileName);
 			//			XMIImportProcessor processor = new XMIImportProcessor(
 			//					parser, "Application1");
 			System.out.println("--------------- Test Case to import XMI successful ------------");
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 			fail("Exception occured");
 		}
 	}
 
	/**
	 * 
	 */
 	public void testXMIImportEditCase()
 	{
 		testXMIImport();
 	}
	
	/**
	 * 
	 */
	public void testImportClinicalAnnotationsXMI()
	{
		try
		{
			String[] args = {ClinicalAnnotationXMI, "edu.wustl.catissuecore.domain.ClinicalAnnotations", "./csv/AnnotationsMainContainer.csv"};
			XMIImporter.main(args);

			System.out.println("--------------- Importing clinical annotations XMI successful ---------------");
		}
		catch (Exception e)
		{
			e.printStackTrace();
			fail("Could not import clinical annotations XMI.");
		}
	}
	
	/**
	 * 
	 */
	public void testEditClinicalAnnotationsXMI()
	{
		testImportClinicalAnnotationsXMI();
	}

 
 	public void testXMIExport()
 	{
 		try
 		{
 			//EntityGroupInterface entityGroup = EntityManager.getInstance().getEntityGroupByName("grp1");
 			XMIExporter xmiExporter = new XMIExporter();
 			//xmiExporter.exportXMI("D:\\DEXMI.xmi", entityGroup, null);
 			String args[] = {XMIFileName, "./xmi/exp_newsurgery.xmi", "1.1"};
 			xmiExporter.main(args);
 		}
 		catch (DynamicExtensionsApplicationException dynamicExtensionsApplicationException)
 		{
 			System.out.print(dynamicExtensionsApplicationException.getMessage());
 			fail("Exception occured");
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Test cases for XMI Import
 	 */
 	public void testPersistEntityGroupWithContainersForXMIImport()
 	{
 		//		DomainObjectFactory domainObjectFactory = DomainObjectFactory.getInstance();
 		//		EntityGroupInterface entityGroup = domainObjectFactory.createEntityGroup();
 		//		entityGroup.setName("test_" + new Double(Math.random()).toString());
 		//		try
 		//		{
 		//			Collection<ContainerInterface> processedContainerList = new HashSet<ContainerInterface>();
 		//
 		//			//Container 1
 		//			Container container1 = (Container) new MockEntityManager().getContainer("abc");
 		//			EntityInterface sourceEntity = (EntityInterface) container1.getAbstractEntity();
 		//			AssociationInterface association = domainObjectFactory.createAssociation();
 		//			association.setName("AssociationName_1");
 		//			association.setEntity(sourceEntity);
 		//			sourceEntity.addAssociation(association);
 		//			//			Adding association to entity
 		//			RoleInterface role = domainObjectFactory.createRole();
 		//			association.setSourceRole(getRole(role, 1, 1, "Role_1"));
 		//			association.setTargetRole(getRole(role, 1, 1, "Role_2"));
 		//			//	association.setAssociationDirection(Constants.AssociationDirection.BI_DIRECTIONAL);
 		//			association.setAssociationDirection(Constants.AssociationDirection.SRC_DESTINATION);
 		//
 		//			//Container 2
 		//			Container container2 = (Container) new MockEntityManager().getContainer("xyz");
 		//			EntityInterface targetEntity = (EntityInterface) new MockEntityManager().initializeEntity2();
 		//			container2.setAbstractEntity(targetEntity);
 		//			association.setTargetEntity(targetEntity);
 		//			//container1.setBaseContainer(container2);
 		//
 		//			//			Container 3
 		//
 		//			EntityInterface parentEntity = (EntityInterface) new MockEntityManager().initializeEntity(entityGroup);
 		//			Container container3 = (Container) new MockEntityManager().createContainerForGivenEntity("For Inheritance - Parent", parentEntity);
 		//
 		//			//container3.setEntity(parentEntity);
 		//
 		//			//			Container 4
 		//
 		//			EntityInterface childEntity = (EntityInterface) new MockEntityManager().initializeEntity(entityGroup);
 		//			childEntity.setName("abc");
 		//			childEntity.setParentEntity(parentEntity);
 		//
 		//			Container container4 = (Container) new MockEntityManager().createContainerForGivenEntity("For Inheritance - Source", childEntity);
 		//			//container4.setEntity(childEntity);
 		//			container4.setCaption("Child Container");
 		//
 		//			container4.setBaseContainer(container3);
 		//
 		//			processedContainerList.add(container1);
 		//			processedContainerList.add(container2);
 		//			processedContainerList.add(container3);
 		//			processedContainerList.add(container4);
 		//
 		//			EntityGroupInterface entityGroupInterface = domainObjectFactory.createEntityGroup();
 		//			//entityGroupInterface.setName("Test Group");
 		//			entityGroupInterface.setName("test_" + new Double(Math.random()).toString());
 		//			entityGroupInterface.setDescription("Test description1");
 		//			entityGroupInterface.setVersion("1");
 		//
 		//			entityGroupInterface.addEntity((EntityInterface) container1.getAbstractEntity());
 		//			entityGroupInterface.addEntity((EntityInterface) container2.getAbstractEntity());
 		//			entityGroupInterface.addEntity((EntityInterface) container3.getAbstractEntity());
 		//			entityGroupInterface.addEntity((EntityInterface) container4.getAbstractEntity());
 		//			//EntityManager.getInstance().persistEntityGroupWithAllContainers(entityGroupInterface, processedContainerList);
 		//		}
 		//		catch (Exception e)
 		//		{
 		//			e.printStackTrace();
 		//			Logger.out.debug(e.getMessage());
 		//			fail("Exception occured");
 		//		}
 	}
 
 	/**
 	 *
 	 */
 	public void testEntitesForDomain_Model_XMI()
 	{//TODO
 		//		try
 		//		{
 		//			testXMIImport();
 		//			DomainModelParser parser = getParser(XMIFileName);
 		//			XMIImportProcessor processor = new XMIImportProcessor(
 		//					parser, "Application1");
 		//
 		//			UMLClass[] umlClasses = parser.getUmlClasses();
 		//			EntityInterface entity = null;
 		//
 		//			Map<String,List<UMLAssociation>> umlClassIdVsUmlAssociationList = new HashMap<String,List<UMLAssociation>>();
 		//
 		//			for (UMLClass umlClass : umlClasses)
 		//			{
 		//				umlClassIdVsUmlAssociationList.put(umlClass.getId(), new ArrayList<UMLAssociation>());
 		//			}
 		//
 		//			for(UMLAssociation association : parser.getUmlAssociations())
 		//			{
 		//				String refId = association.getSourceUMLAssociationEdge().getUMLAssociationEdge().getUMLClassReference().getRefid();
 		//				List<UMLAssociation> umlAssociationList = umlClassIdVsUmlAssociationList.get(refId);
 		//				umlAssociationList.add(association);
 		//
 		//				String targetRefId = association.getTargetUMLAssociationEdge().getUMLAssociationEdge().getUMLClassReference().getRefid();
 		//				umlAssociationList = umlClassIdVsUmlAssociationList.get(targetRefId);
 		//				umlAssociationList.add(association);
 		//			}
 		//
 		//			for (UMLClass umlClass : umlClasses)
 		//			{
 		//				entity = EntityManager.getInstance().getEntityByName(
 		//						umlClass.getPackageName() + "." + umlClass.getClassName());
 		//
 		//				if (entity == null)
 		//				{
 		//					fail("Entity present in the XMI file has not been persisted");
 		//				}
 		//
 		//				testAttributesForEntity(entity.getAttributeCollection(), umlClass.getUmlAttributeCollection().getUMLAttribute());
 		//
 		////				testAssociationsForEntity(entity.getAssociationCollection(),umlClassIdVsUmlAssociationList.get(umlClass.getId()));
 		//
 		//				Map<String,List<String>> parentVsChildList = parser.getParentVsChildrenMap();
 		//				testGeneralizationForEntity(parentVsChildList,umlClasses,entity.getParentEntity(),umlClass);
 		//			}
 		//		}
 		//		catch (Exception e)
 		//		{
 		//			e.printStackTrace();
 		//			fail("Exception occured");
 		//		}
 	}
 
 	/**
 	 * @param parentVsChildList
 	 * @param umlClasses
 	 * @param parentEntity
 	 * @param umlClass
 	 */
 	public void testGeneralizationForEntity(Map<String, List<String>> parentVsChildList,
 			UMLClass[] umlClasses, EntityInterface parentEntity, UMLClass umlClass)
 	{
 
 		//		Set<String> parentIds = parentVsChildList.keySet();
 		//		for (String parentId : parentIds)
 		//		{
 		//			List<String> childrenList = parentVsChildList.get(parentId);
 		//			if (childrenList.contains(umlClass.getId()))
 		//			{
 		//				UMLClass parent = null;
 		//				for (UMLClass parentUmlClass : umlClasses)
 		//				{
 		//					if (parentUmlClass.getId().equalsIgnoreCase(parentId))
 		//					{
 		//						parent = parentUmlClass;
 		//					}
 		//				}
 		//
 		//				if (!(parentEntity.getName().equalsIgnoreCase(parent.getPackageName() + "." + parent.getClassName())))
 		//				{
 		//					fail("Parent Entity has not been persisted");
 		//				}
 		//			}
 		//		}
 	}
 
 	/**
 	 *
 	 */
 	public void testAssociationsForEntity(Collection<AssociationInterface> associationColl,
 			List<UMLAssociation> umlAssociations)
 	{
 		//		for (UMLAssociation umlAssociation : umlAssociations)
 		//		{
 		//			boolean isAttrPresent = false;
 		//			for (AssociationInterface association : associationColl)
 		//			{
 		//				if (umlAssociation.getSourceUMLAssociationEdge().getUMLAssociationEdge().getRoleName().equalsIgnoreCase(association.getName())
 		//						|| umlAssociation.getTargetUMLAssociationEdge().getUMLAssociationEdge().getRoleName().equalsIgnoreCase(association.getName()))//&& umlAttr.getDataTypeName().equalsIgnoreCase(attr.getAttributeTypeInformation().getDataType())
 		//				{
 		//					isAttrPresent = true;
 		//					break;
 		//				}
 		//			}
 		//			if (!isAttrPresent)
 		//			{
 		//				fail("Association present in the XMI file has not been persisted");
 		//			}
 		//		}
 	}
 
 	/**
 	 * @param attrColl
 	 * @param umlAttrColl
 	 */
 	public void testAttributesForEntity(Collection<AttributeInterface> attrColl,
 			UMLAttribute[] umlAttrColl)
 	{
 		//		for (UMLAttribute umlAttr : umlAttrColl)
 		//		{
 		//			boolean isAttrPresent = false;
 		//			for (AttributeInterface attr : attrColl)
 		//			{
 		//				if (umlAttr.getName().equalsIgnoreCase(attr.getName()))//&& umlAttr.getDataTypeName().equalsIgnoreCase(attr.getAttributeTypeInformation().getDataType())
 		//				{
 		//					isAttrPresent = true;
 		//					break;
 		//				}
 		//			}
 		//			if (!isAttrPresent)
 		//			{
 		//				fail("Attribute present in the XMI file has not been persisted");
 		//			}
 		//		}
 	}
 
 	/**
 	 *
 	 */
 	public void testUniqueCase1ForXMIImport()//E1 associated with E2 extends E3 extends E1
 	{
 		DomainObjectFactory domainObjectFactory = DomainObjectFactory.getInstance();
 		try
 		{
 			EntityGroup entityGroup = (EntityGroup) new MockEntityManager().initializeEntityGroup();
 			Collection<EntityInterface> entityColl = entityGroup.getEntityCollection();
 			//			Container1
 			EntityInterface entity1 = null;
 			for (EntityInterface e : entityColl)
 			{
 				entity1 = e;
 			}
 
 			Container container1 = (Container) new MockEntityManager()
 					.createContainerForGivenEntity("For Inheritance - Parent", entity1);
 			container1.setAbstractEntity(entity1);
 
 			EntityInterface entity2 = (EntityInterface) new MockEntityManager()
 					.initializeEntity(entityGroup);
 			entity2.setName("Entity2");
 			EntityInterface entity3 = (EntityInterface) new MockEntityManager()
 					.initializeEntity(entityGroup);
 			entity3.setName("Entity3");
 
 			AssociationInterface association1_2 = domainObjectFactory.createAssociation();
 			association1_2.setTargetEntity(entity2);
 			association1_2.setAssociationDirection(AssociationDirection.SRC_DESTINATION);
 			association1_2.setName("E1_E2");
 			association1_2.setSourceRole(getRole(AssociationType.ASSOCIATION, "Entity1",
 					Cardinality.ONE, Cardinality.ONE));
 			association1_2.setTargetRole(getRole(AssociationType.ASSOCIATION, "Entity2",
 					Cardinality.ONE, Cardinality.MANY));
 			entity1.addAssociation(association1_2);
 
 			entity2.setParentEntity(entity3);
 			entity3.setParentEntity(entity1);
 			entityGroup.addEntity(entity2);
 			entityGroup.addEntity(entity3);
 
 			Collection containerColl = new HashSet();
 			containerColl.add(container1);
 			//EntityManager.getInstance().persistEntityGroupWithAllContainers(entityGroup, containerColl);
 
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 			Logger.out.debug(e.getMessage());
 			fail("Exception occured");
 		}
 	}
 
 	/**
 	 * @param role
 	 * @param maxCardinality
 	 * @param minCardinality
 	 * @param roleName
 	 * @return
 	 */
 	private RoleInterface getRole(RoleInterface role, int maxCardinality, int minCardinality,
 			String roleName)
 	{
 		role.setAssociationsType(DEConstants.AssociationType.ASSOCIATION);
 		role.setName(roleName);
 		role.setMaximumCardinality(getCardinality(maxCardinality));
 		role.setMinimumCardinality(getCardinality(minCardinality));
 		return role;
 	}
 
 	/**
 	 * @param cardinality
 	 * @return
 	 */
 	private DEConstants.Cardinality getCardinality(int cardinality)
 	{
 		if (cardinality == 0)
 		{
 			return DEConstants.Cardinality.ZERO;
 		}
 		if (cardinality == 1)
 		{
 			return DEConstants.Cardinality.ONE;
 		}
 		return DEConstants.Cardinality.MANY;
 	}
 
 	/**
 	 * PURPOSE : To test the method persistEntityGroup
 	 * EXPECTED BEHAVIOR : The new entity group should be stored correctly and should be retrieved back correctly.
 	 * 1. Create entity group.
 	 * 2. Save entityGroup using entity manager.
 	 * 3. Check whether the saved entity group is retrieved back properly or not.
 	 */
 	public void testPersistEntityGroupWithAllContainers()
 	{
 		//		try
 		//		{
 		//			EntityManagerInterface entityManager = EntityManager.getInstance();
 		//			MockEntityManager mockEM = new MockEntityManager();
 		//			//Step 1
 		//			EntityGroupInterface entityGroup = (EntityGroupInterface) mockEM.initializeEntityGroup();
 		//
 		//			Collection<ContainerInterface> containerColl = new HashSet<ContainerInterface>();
 		//			EntityInterface entity = null;
 		//			Collection<EntityInterface> entityColl = entityGroup.getEntityCollection();
 		//			for (EntityInterface entityInterface : entityColl)
 		//			{
 		//				entity = entityInterface;
 		//			}
 		//			containerColl.add(mockEM.createContainerForGivenEntity("ABC", entity));
 		//
 		//			//Step 2
 		//			//entityGroup = (EntityGroupInterface) entityManager.persistEntityGroupWithAllContainers(entityGroup, containerColl);
 		//			//Step 3
 		//			DefaultBizLogic defaultBizLogic = BizLogicFactory.getDefaultBizLogic();
 		//			List entityGroupColl = defaultBizLogic.retrieve(EntityGroup.class.getName(), "name", entityGroup.getName());
 		//
 		//			assertTrue(entityGroupColl.contains(entityGroup));
 		//
 		//		}
 		//		catch (Exception e)
 		//		{
 		//			Logger.out.debug(e.getMessage());
 		//			e.printStackTrace();
 		//			fail("Exception occured");
 		//		}
 	}
 
 }
