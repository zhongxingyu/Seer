 
 package edu.common.dynamicextensions.util;
 
 import java.text.ParseException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import edu.common.dynamicextensions.domain.CategoryEntity;
 import edu.common.dynamicextensions.domain.DateAttributeTypeInformation;
 import edu.common.dynamicextensions.domain.NumericAttributeTypeInformation;
 import edu.common.dynamicextensions.domain.PathAssociationRelationInterface;
 import edu.common.dynamicextensions.domaininterface.AssociationInterface;
 import edu.common.dynamicextensions.domaininterface.AttributeMetadataInterface;
 import edu.common.dynamicextensions.domaininterface.CategoryAssociationInterface;
 import edu.common.dynamicextensions.domaininterface.CategoryAttributeInterface;
 import edu.common.dynamicextensions.domaininterface.CategoryEntityInterface;
 import edu.common.dynamicextensions.domaininterface.CategoryInterface;
 import edu.common.dynamicextensions.domaininterface.EntityGroupInterface;
 import edu.common.dynamicextensions.domaininterface.EntityInterface;
 import edu.common.dynamicextensions.domaininterface.userinterface.ContainerInterface;
 import edu.common.dynamicextensions.entitymanager.EntityManager;
 import edu.common.dynamicextensions.entitymanager.EntityManagerInterface;
 import edu.common.dynamicextensions.exception.DynamicExtensionsApplicationException;
 import edu.common.dynamicextensions.exception.DynamicExtensionsSystemException;
 import edu.common.dynamicextensions.util.global.CategoryConstants;
 import edu.common.dynamicextensions.util.parser.CategoryCSVConstants;
 import edu.common.dynamicextensions.util.parser.FormulaParser;
 import edu.common.dynamicextensions.validation.category.CategoryValidator;
 import edu.wustl.common.util.global.ApplicationProperties;
 
 /**
  * 
  * @author kunal_kamble
  *
  */
 public class CategoryGenerationUtil
 {
 
 	/**
 	 * This method returns the entity from the entity group.
 	 * @param entityName
 	 * @param entityGroup
 	 * @return
 	 */
 	public static EntityInterface getEntity(String entityName, EntityGroupInterface entityGroup)
 	{
 		return entityGroup.getEntityByName(entityName);
 	}
 
 	/**
 	 * Returns the multiplicity in number for the give string
 	 * @param multiplicity
 	 * @return
 	 * @throws DynamicExtensionsSystemException 
 	 */
 	public static int getMultiplicityInNumbers(String multiplicity)
 			throws DynamicExtensionsSystemException
 	{
 		int multiplicityI = 1;
 		if (multiplicity.equalsIgnoreCase(CategoryCSVConstants.MULTILINE))
 		{
 			multiplicityI = -1;
 		}
 		else if (multiplicity.equalsIgnoreCase(CategoryCSVConstants.SINGLE))
 		{
 			multiplicityI = 1;
 		}
 		else
 		{
 			throw new DynamicExtensionsSystemException(
 					"ERROR: WRONG KEYWORD USED FOR MULTIPLICITY " + multiplicity
 							+ ". VALID KEY WORDS ARE: 1- single 2-multiline");
 		}
 		return multiplicityI;
 	}
 
 	/**
 	 * Sets the root category entity for the category.
 	 * @param category
 	 * @param rootContainerObject
 	 * @param containerCollection
 	 * @param paths
 	 * @param absolutePath
 	 * @param containerNameInstanceMap
 	 * @throws DynamicExtensionsSystemException
 	 * @throws DynamicExtensionsApplicationException
 	 */
 	public static void setRootContainer(CategoryInterface category,
 			ContainerInterface rootContainerObject, List<ContainerInterface> containerCollection,
 			Map<String, List<AssociationInterface>> paths, Map<String, List<String>> absolutePath,
 			Map<String, String> containerNameInstanceMap) throws DynamicExtensionsSystemException,
 			DynamicExtensionsApplicationException
 	{
 		ContainerInterface rootContainer = rootContainerObject;
 		//		ContainerInterface rootContainer = null;
 		//		for (ContainerInterface containerInterface : containerCollection)
 		//		{
 		//			CategoryEntityInterface categoryEntityInterface = (CategoryEntityInterface) containerInterface.getAbstractEntity();
 		//			if (categoryEntityInterface.getTreeParentCategoryEntity() == null)
 		//			{
 		//				rootContainer = containerInterface;
 		//				break;
 		//			}
 		//		}
 
 		CategoryHelperInterface categoryHelper = new CategoryHelper();
 
 		for (String entityName : absolutePath.keySet())
 		{
 
 			if (absolutePath.get(entityName).size() == 1 && rootContainer != null)
 			{
 				CategoryEntityInterface categoryEntityInterface = (CategoryEntityInterface) rootContainer
 						.getAbstractEntity();
 				String entityNameForAssociationMap = CategoryGenerationUtil
 						.getEntityNameForAssociationMap(containerNameInstanceMap
 								.get(categoryEntityInterface.getName()));
 				if (!entityName.equals(entityNameForAssociationMap))
 				{
 					String entName = entityName;
 					EntityInterface entity = CategoryGenerationUtil
 							.getEntityFromEntityAssociationMap(paths.get(entityName));
 					if (entity != null)
 					{
 						entName = entity.getName();
 					}
 					ContainerInterface containerInterface = getContainerWithEntityName(
 							containerCollection, entName);
 					if (containerInterface == null)
 					{
 						EntityGroupInterface entityGroup = categoryEntityInterface.getEntity()
 								.getEntityGroup();
 						entity = entityGroup.getEntityByName(entName);
 
 						ContainerInterface newRootContainer = categoryHelper
 								.createOrUpdateCategoryEntityAndContainer(entity, null, category,
 										entName + "[1]" + entName + "[1]");
 						newRootContainer.setAddCaption(false);
 
 						categoryHelper.associateCategoryContainers(category, entityGroup,
 								newRootContainer, rootContainer, paths
 										.get(entityNameForAssociationMap), 1,
 								containerNameInstanceMap.get(rootContainer.getAbstractEntity()
 										.getName()));
 
 						rootContainer = newRootContainer;
 
 					}
 					else
 					{
 						rootContainer = containerInterface;
 					}
 
 				}
 			}
 		}
 
 		for (ContainerInterface containerInterface : containerCollection)
 		{
 			CategoryEntityInterface categoryEntity = ((CategoryEntityInterface) containerInterface
 					.getAbstractEntity());
 			boolean isTableCreated = ((CategoryEntity) categoryEntity).isCreateTable();
 			if (rootContainer != containerInterface
 					&& categoryEntity.getTreeParentCategoryEntity() == null && isTableCreated)
 			{
 				categoryHelper.associateCategoryContainers(category, categoryEntity.getEntity()
 						.getEntityGroup(), rootContainer, containerInterface, paths
 						.get(CategoryGenerationUtil
 								.getEntityNameForAssociationMap(containerNameInstanceMap
 										.get(categoryEntity.getName()))), 1,
 						containerNameInstanceMap.get(containerInterface.getAbstractEntity()
 								.getName()));
 			}
 		}
 		//If category is edited and no attributes from the main form of the model are not selected
 		//rootContainer will be null
 		//keep the root container unchanged in this case. Just use the root entity of the category
 		if (rootContainer == null)
 		{
 			EntityManagerInterface entityManagerInterface = EntityManager.getInstance();
 			rootContainer = entityManagerInterface.getContainerByEntityIdentifier(category
 					.getRootCategoryElement().getId());
 		}
 		categoryHelper.setRootCategoryEntity(rootContainer, category);
 
 		categoryHelper.setRootCategoryEntity(rootContainer, category);
 	}
 
 	/**
 	 * Returns the container having container caption as one passed to this method.
 	 * @param containerCollection
 	 * @param containerCaption
 	 * @return
 	 */
 	public static ContainerInterface getContainer(List<ContainerInterface> containerCollection,
 			String containerCaption)
 	{
 		ContainerInterface container = null;
 		for (ContainerInterface containerInterface : containerCollection)
 		{
 			if (containerCaption.equals(containerInterface.getCaption()))
 			{
 				container = containerInterface;
 				break;
 			}
 
 		}
 		return container;
 	}
 
 	/**
 	 * @param containerCollection
 	 * @param entityName
 	 * @return
 	 */
 	public static ContainerInterface getContainerWithEntityName(
 			List<ContainerInterface> containerCollection, String entityName)
 	{
 		ContainerInterface container = null;
 		for (ContainerInterface containerInterface : containerCollection)
 		{
 			CategoryEntityInterface categoryEntity = (CategoryEntityInterface) containerInterface
 					.getAbstractEntity();
 			if (entityName.equals(categoryEntity.getEntity().getName()))
 			{
 				container = containerInterface;
 				break;
 			}
 
 		}
 		return container;
 	}
 
 	/**
 	 * Returns the container with given category entity name.
 	 * @param containerCollection
 	 * @param categoryEntityName
 	 * @return
 	 */
 	public static ContainerInterface getContainerWithCategoryEntityName(
 			List<ContainerInterface> containerCollection, String categoryEntityName)
 	{
 		ContainerInterface container = null;
 
 		for (ContainerInterface containerInterface : containerCollection)
 		{
 			if (categoryEntityName.equals(containerInterface.getAbstractEntity().getName()))
 			{
 				container = containerInterface;
 				break;
 			}
 
 		}
 		return container;
 	}
 
 	/**
 	 * @param paths
 	 * @param entityGroup
 	 * @return
 	 * @throws DynamicExtensionsSystemException 
 	 */
 	public static Map<String, List<AssociationInterface>> getAssociationList(
 			Map<String, List<String>> paths, EntityGroupInterface entityGroup)
 			throws DynamicExtensionsSystemException
 	{
 		Map<String, List<AssociationInterface>> entityPaths = new HashMap<String, List<AssociationInterface>>();
 
 		Set<String> entitiesNames = paths.keySet();
 		for (String entityName : entitiesNames)
 		{
 			// Path stored is from the root. 
 			List<String> pathsForEntity = paths.get(entityName);
 
 			List<AssociationInterface> associations = new ArrayList<AssociationInterface>();
 			Iterator<String> entNamesIter = pathsForEntity.iterator();
 
 			String srcEntityName = entNamesIter.next();
 			String associationRoleName = getAssociationRoleName(srcEntityName);
 
 			EntityInterface sourceEntity = entityGroup
 					.getEntityByName(getEntityNameExcludingAssociationRoleName(srcEntityName));
 			CategoryValidator.checkForNullRefernce(sourceEntity,
 					"ERROR IN DEFINING PATH FOR THE ENTITY " + entityName + ": ENTITY WITH NAME "
 							+ srcEntityName + " DOES NOT EXIST");
 
 			while (entNamesIter.hasNext())
 			{
 				String targetEntityName = entNamesIter.next();
 
 				EntityInterface targetEntity = entityGroup
 						.getEntityByName(getEntityNameExcludingAssociationRoleName(targetEntityName));
 				addAssociation(sourceEntity, targetEntity, associationRoleName, associations);
 
 				// Add all parent entity associations also to the list.
 				EntityInterface parentEntity = sourceEntity.getParentEntity();
 				while (parentEntity != null)
 				{
 					addAssociation(parentEntity, targetEntity, associationRoleName, associations);
 
 					parentEntity = parentEntity.getParentEntity();
 				}
 
 				// Source entity should now be target entity.
 				sourceEntity = targetEntity;
 				associationRoleName = getAssociationRoleName(targetEntityName);
 			}
 
 			entityPaths.put(entityName, associations);
 
 			if (pathsForEntity.size() > 1 && associations.isEmpty())
 			{
 				CategoryValidator.checkForNullRefernce(null, "ERROR: PATH DEFINED FOR THE ENTITY "
 						+ entityName + " IS NOT CORRECT");
 			}
 		}
 
 		return entityPaths;
 	}
 
 	/**
 	 * @throws DynamicExtensionsSystemException 
 	 * 
 	 */
 	private static void addAssociation(EntityInterface sourceEntity, EntityInterface targetEntity,
 			String associationRoleName, List<AssociationInterface> associations)
 	{
 		for (AssociationInterface association : sourceEntity.getAssociationCollection())
 		{
 			if (association.getTargetEntity().equals(targetEntity))
 			{
 				if (associationRoleName != null && associationRoleName.length() > 0
 						&& associationRoleName.equals(association.getTargetRole().getName()))
 				{
 					associations.add(association);
 					break;
 				}
 				else if (associationRoleName != null && associationRoleName.length() == 0)
 				{
 					associations.add(association);
 					break;
 				}
 			}
 		}
 	}
 
 	/**
 	 * 
 	 * @return
 	 */
 	public static String getEntityNameExcludingAssociationRoleName(String entityName)
 	{
 		String newEntityName = entityName;
 		String associationRoleName = getFullAssociationRoleName(entityName);
 		if (associationRoleName != null && associationRoleName.length() > 0)
 		{
 			newEntityName = entityName.replace(associationRoleName, "");
 		}
 		return newEntityName;
 	}
 
 	/**
 	 * 
 	 * @return
 	 */
 	public static String getAssociationRoleName(String entityName)
 	{
 		String associationRoleName = "";
 		if (entityName != null && entityName.indexOf('(') != -1 && entityName.indexOf(')') != -1)
 		{
 			associationRoleName = entityName.substring(entityName.indexOf('(') + 1, entityName
 					.indexOf(')'));
 		}
 		return associationRoleName;
 	}
 
 	/**
 	 * 
 	 * @return
 	 */
 	public static String getFullAssociationRoleName(String entityName)
 	{
 		String associationRoleName = "";
 		if (entityName != null && entityName.indexOf('(') != -1 && entityName.indexOf(')') != -1)
 		{
 			associationRoleName = entityName.substring(entityName.indexOf('('), entityName
 					.indexOf(')') + 1);
 		}
 		return associationRoleName;
 	}
 
 	/**
 	 * This method gets the relative path.
 	 * @param entityNameList ordered entities names in the path
 	 * @param pathMap 
 	 * @return
 	 */
 	public static List<String> getRelativePath(List<String> entityNameList,
 			Map<String, List<String>> pathMap)
 	{
 		List<String> newEntityNameList = new ArrayList<String>();
 		String lastEntityName = null;
 		for (String entityName : entityNameList)
 		{
 			if (pathMap.get(entityName) == null)
 			{
 				newEntityNameList.add(entityName);
 			}
 			else
 			{
 				lastEntityName = entityName;
 			}
 		}
 		if (lastEntityName != null && !newEntityNameList.contains(lastEntityName))
 		{
 			newEntityNameList.add(0, lastEntityName);
 		}
 
 		return newEntityNameList;
 	}
 
 	/**
 	 * Returns the entity group used for careting this category 
 	 * @param category
 	 * @param entityGroupName
 	 * @return
 	 */
 	public static EntityGroupInterface getEntityGroup(CategoryInterface category,
 			String entityGroupName)
 	{
 		if (category.getRootCategoryElement() != null)
 		{
 			return category.getRootCategoryElement().getEntity().getEntityGroup();
 		}
 
 		return DynamicExtensionsUtility.retrieveEntityGroup(entityGroupName);
 
 	}
 
 	/**
 	 * This method finds the main category entity.
 	 * @param categoryPaths
 	 * @return
 	 */
 	public static String getMainCategoryEntityName(String[] categoryPaths)
 	{
 		int minNumOfCategoryEntityNames = categoryPaths[0].split("->").length;
 		for (String string : categoryPaths)
 		{
 			if (minNumOfCategoryEntityNames > string.split("->").length)
 			{
 				minNumOfCategoryEntityNames = string.split("->").length;
 			}
 		}
 		String categoryEntityName = categoryPaths[0].split("->")[0];
 
 		a : for (int i = 0; i < minNumOfCategoryEntityNames; i++)
 		{
 			String temp = categoryPaths[0].split("->")[i];
 			for (String string : categoryPaths)
 			{
 				if (!string.split("->")[i].equals(temp))
 				{
 					break a;
 				}
 			}
 			categoryEntityName = categoryPaths[0].split("->")[i];
 		}
 
 		return categoryEntityName;
 	}
 
 	/**
 	 * category names in CSV are of format <entity_name>[instance_Number]
 	 * @param categoryNameInCSV
 	 * @return
 	 */
 	public static String getEntityName(String categoryNameInCSV)
 	{
 		return getEntityNameExcludingAssociationRoleName(categoryNameInCSV.substring(0,
 				categoryNameInCSV.indexOf('[')));
 	}
 
 	/**
 	 * category names in CSV are of format <entity_name>[instance_Number]
 	 * @param categoryNameInCSV
 	 * @return
 	 */
 	public static String getEntityNameForAssociationMap(String categoryEntityInstancePath)
 	{
 		StringBuffer entityNameForAssociationMap = new StringBuffer();
 		String[] categoryEntityPathArray = categoryEntityInstancePath.split("->");
 
 		for (String instancePath : categoryEntityPathArray)
 		{
 			entityNameForAssociationMap
 					.append(instancePath.substring(0, instancePath.indexOf("[")));
 		}
 		return entityNameForAssociationMap.toString();
 	}
 
 	/**
 	 * category names in CSV are of format <entity_name>[instance_Number]
 	 * @param categoryNameInCSV
 	 * @return
 	 */
 	public static String getCategoryEntityName(String categoryEntityInstancePath)
 	{
 		StringBuffer entityNameForAssociationMap = new StringBuffer();
 		String[] categoryEntityPathArray = categoryEntityInstancePath.split("->");
 
 		for (String instancePath : categoryEntityPathArray)
 		{
 			entityNameForAssociationMap.append(instancePath);
 		}
 		if (categoryEntityPathArray.length == 1)
 		{
 			entityNameForAssociationMap.append(categoryEntityPathArray[0]);
 		}
 		return entityNameForAssociationMap.toString();
 	}
 
 	/**
 	 * category names in CSV are of format <entity_name>[instance_Number]
 	 * @param categoryNameInCSV
 	 * @return
 	 */
 	public static String getEntityNameFromCategoryEntityInstancePath(
 			String categoryEntityInstancePath)
 	{
 		String entityName = "";
 		if (categoryEntityInstancePath != null)
 		{
 			String[] categoryEntitiesInPath = categoryEntityInstancePath.split("->");
 			String newCategoryEntityName = categoryEntitiesInPath[categoryEntitiesInPath.length - 1];
 			entityName = getEntityName(newCategoryEntityName);
 		}
 		return entityName;
 	}
 
 	/**
 	 * category names in CSV are of format <entity_name>[instance_Number]
 	 * @param categoryNameInCSV
 	 * @return
 	 */
 	public static EntityInterface getEntityFromEntityAssociationMap(
 			List<AssociationInterface> assoList)
 	{
 		EntityInterface entityInterface = null;
 		if (assoList != null && !assoList.isEmpty())
 		{
 			AssociationInterface associationInterface = assoList.get(assoList.size() - 1);
 			entityInterface = associationInterface.getTargetEntity();
 		}
 		return entityInterface;
 	}
 	/**
 	 * @throws ParseException 
 	 * @throws DynamicExtensionsApplicationException 
 	 * @throws DynamicExtensionsSystemException 
 	 * @throws DynamicExtensionsSystemException 
 	 * 
 	 */
	public static void setDefaultValueForCalculatedAttributes(CategoryEntityInterface rootCategoryEntity,Long lineNumber) throws DynamicExtensionsApplicationException, DynamicExtensionsSystemException
 	{
 		for (CategoryAssociationInterface categoryAssociationInterface : rootCategoryEntity
 				.getCategoryAssociationCollection())
 		{
 			for (CategoryAttributeInterface categoryAttributeInterface : categoryAssociationInterface
 					.getTargetCategoryEntity().getAllCategoryAttributes())
 			{
 				Boolean isCalculatedAttribute = categoryAttributeInterface
 						.getIsCalculated();
 				if (isCalculatedAttribute != null && isCalculatedAttribute) 
 				{
					setDefaultValue(categoryAttributeInterface,rootCategoryEntity
							.getCategory());
 					FormulaCalculator formulaCalculator = new FormulaCalculator();
 					String message = formulaCalculator.setDefaultValueForCalculatedAttributes(
 							categoryAttributeInterface, rootCategoryEntity
 									.getCategory());
 					if (message != null && message.length() > 0)
 					{
 						throw new DynamicExtensionsSystemException(ApplicationProperties
 								.getValue(CategoryConstants.CREATE_CAT_FAILS)
 								+ ApplicationProperties.getValue(CategoryConstants.LINE_NUMBER)
 								+ lineNumber
 								+ " "
 								+ message);
 					}
 				}
 			}
			setDefaultValueForCalculatedAttributes(categoryAssociationInterface
 					.getTargetCategoryEntity(),lineNumber);
 		}
 	}
 	/**
 	 * 
 	 * @return
 	 */
 	public static void getCategoryAttribute(String entityName,Long instanceNumber,String attributeName,CategoryEntityInterface rootCategoryEntity,List <CategoryAttributeInterface> attributes)
 	{
 		if (rootCategoryEntity != null)
 		{
 			for (CategoryAssociationInterface categoryAssociationInterface : rootCategoryEntity
 					.getCategoryAssociationCollection())
 			{
 				CategoryEntityInterface categoryEntityInterface = categoryAssociationInterface
 						.getTargetCategoryEntity();
 				List<PathAssociationRelationInterface> pathAssociationCollection = categoryEntityInterface
 						.getPath().getSortedPathAssociationRelationCollection();
 				PathAssociationRelationInterface pathAssociationRelationInterface = pathAssociationCollection
 						.get(pathAssociationCollection.size() - 1);
 				if (categoryEntityInterface.getEntity().getName().equals(
 						entityName)
 						&& pathAssociationRelationInterface
 								.getTargetInstanceId().equals(instanceNumber))
 				{
 					for (CategoryAttributeInterface categoryAttributeInterface : categoryAssociationInterface.getTargetCategoryEntity().getCategoryAttributeCollection())
 					{
 						if (categoryAttributeInterface.getAbstractAttribute().getName().equals(attributeName))
 						{
 							attributes.add(categoryAttributeInterface);
 							return;
 						}
 					}
 				}
 				else
 				{
 					getCategoryAttribute(entityName,instanceNumber,attributeName,categoryAssociationInterface.getTargetCategoryEntity(),attributes);
 				}
 			}
 		}
 	}
 	/**
 	 * setDefaultValueforCalculatedAttributes.
 	 * @param defaultValue
 	 * @param control
 	 * @param categoryAttribute
 	 * @throws DynamicExtensionsApplicationException
 	 * @throws DynamicExtensionsSystemException
 	 */
 	private static void setDefaultValue(CategoryAttributeInterface categoryAttribute,CategoryInterface categoryInterface) throws DynamicExtensionsApplicationException, DynamicExtensionsSystemException
 	{
 		FormulaParser formulaParser = new FormulaParser();
 		if (formulaParser.validateExpression(
 				categoryAttribute.getFormula().getExpression()))
 		{
 			categoryAttribute.removeAllCalculatedCategoryAttributes();
 			List<String> symbols = formulaParser.getSymobols();
 			List <CategoryAttributeInterface> attributes = new ArrayList<CategoryAttributeInterface>();
 			for (String symbol : symbols)
 			{
 				String [] names = symbol.split("_");
 				if (names.length == 3)
 				{
 					attributes.clear();
 					try
 					{
 						Long.parseLong(names[1]);
 					}
 					catch (NumberFormatException e) 
 					{
 						throw new DynamicExtensionsSystemException(
 								ApplicationProperties
 										.getValue(CategoryConstants.CREATE_CAT_FAILS)
 										+ " "
 										+ names
 										+ ApplicationProperties
 												.getValue("incorrectFormulaSyntaxCalculatedAttribute")
 										+ categoryAttribute.getFormula()
 												.getExpression());
 						
 					}
 					CategoryGenerationUtil.getCategoryAttribute(names[0], Long.valueOf(names[1]), names[2],
 							categoryInterface.getRootCategoryElement(), attributes);
 					if (attributes.isEmpty())
 					{
 						throw new DynamicExtensionsSystemException(
 								ApplicationProperties
 										.getValue(CategoryConstants.CREATE_CAT_FAILS)
 										+ " "
 										+ names
 										+ ApplicationProperties
 												.getValue("incorrectFormulaSyntaxCalculatedAttribute")
 										+ categoryAttribute.getFormula()
 												.getExpression());
 					}
 					else
 					{
 						if (((AttributeMetadataInterface) attributes.get(0)).getAttributeTypeInformation() instanceof NumericAttributeTypeInformation
 								|| ((AttributeMetadataInterface) attributes.get(0)).getAttributeTypeInformation() instanceof DateAttributeTypeInformation)
 						{
 							categoryAttribute.addCalculatedCategoryAttribute(attributes.get(0));
 						}
 						else
 						{
 							throw new DynamicExtensionsSystemException(
 									ApplicationProperties
 											.getValue(CategoryConstants.CREATE_CAT_FAILS)
 											+ " "
 											+ names
 											+ ApplicationProperties
 													.getValue("incorrectDataTypeForCalculatedAttribute")
 											+ categoryAttribute.getFormula()
 													.getExpression());
 						}
 					}
 				}
 				else
 				{
 					throw new DynamicExtensionsSystemException(
 							ApplicationProperties
 									.getValue(CategoryConstants.CREATE_CAT_FAILS)
 									+ " "
 									+ names
 									+ ApplicationProperties
 											.getValue("incorrectFormulaSyntaxCalculatedAttribute")
 									+ categoryAttribute.getFormula()
 											.getExpression());
 				}
 			}
 		}
 	}
 }
