 
 package edu.wustl.cab2b.common.cache;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 
 import org.apache.log4j.Logger;
 
 import edu.common.dynamicextensions.domaininterface.AbstractEntityInterface;
 import edu.common.dynamicextensions.domaininterface.AssociationInterface;
 import edu.common.dynamicextensions.domaininterface.AttributeInterface;
 import edu.common.dynamicextensions.domaininterface.AttributeTypeInformationInterface;
 import edu.common.dynamicextensions.domaininterface.BaseAbstractAttributeInterface;
 import edu.common.dynamicextensions.domaininterface.CategoryAssociationInterface;
 import edu.common.dynamicextensions.domaininterface.CategoryAttributeInterface;
 import edu.common.dynamicextensions.domaininterface.CategoryEntityInterface;
 import edu.common.dynamicextensions.domaininterface.CategoryInterface;
 import edu.common.dynamicextensions.domaininterface.EntityGroupInterface;
 import edu.common.dynamicextensions.domaininterface.EntityInterface;
 import edu.common.dynamicextensions.domaininterface.PermissibleValueInterface;
 import edu.common.dynamicextensions.domaininterface.UserDefinedDEInterface;
 import edu.common.dynamicextensions.domaininterface.userinterface.ContainerInterface;
 import edu.common.dynamicextensions.domaininterface.userinterface.ControlInterface;
 import edu.common.dynamicextensions.entitymanager.AbstractBaseMetadataManager;
 import edu.common.dynamicextensions.entitymanager.AbstractMetadataManager;
 import edu.common.dynamicextensions.entitymanager.CategoryManager;
 import edu.common.dynamicextensions.entitymanager.CategoryManagerInterface;
 import edu.common.dynamicextensions.entitymanager.EntityManager;
 import edu.common.dynamicextensions.exception.DynamicExtensionsApplicationException;
 import edu.common.dynamicextensions.exception.DynamicExtensionsCacheException;
 import edu.common.dynamicextensions.exception.DynamicExtensionsSystemException;
 import edu.common.dynamicextensions.skiplogic.SkipLogic;
 import edu.common.dynamicextensions.ui.util.Constants;
 import edu.common.dynamicextensions.util.DynamicExtensionsUtility;
 import edu.wustl.cab2b.common.beans.MatchedClass;
 import edu.wustl.cab2b.common.beans.MatchedClassEntry;
 import edu.wustl.cab2b.common.exception.RuntimeException;
 import edu.wustl.cab2b.common.util.Utility;
 import edu.wustl.cab2b.server.util.DynamicExtensionUtility;
 import edu.wustl.common.beans.NameValueBean;
 import edu.wustl.common.util.global.ApplicationProperties;
 import edu.wustl.dao.HibernateDAO;
 import edu.wustl.dao.exception.DAOException;
 
 /**
  * This is an abstract class for caching metadata.
  * It holds the data needed structures, methods to populate those and public methods to access cache.
  *
  * @author Chandrakant Talele
  * @author gautam_shetty
  * @author Rahul Ner
  * @author pavan_kalantri
  */
 public abstract class AbstractEntityCache implements IEntityCache
 {
 
 	private static final String DATA_ENTRY_CONTAINER_IN_USE = "data.entry.container.in.use";
 	private static final String CONTAINER_NOT_FOUND = "container.not.found";
 
 	private static final long serialVersionUID = 1234567890L;
 
 	public static boolean isCacheReady = true;
 
 	/**
 	 * Error Constants
 	 */
 	public static final String EXCEPTION_CREATING_CACHE = "Exception encountered while creating EntityCache!!";
 
 	/**
 	 * Error Constants
 	 */
 	public static final String ERROR_CREATING_CACHE = "Error while Creating EntityCache. Error: ";
 
 	private static final Logger LOGGER = edu.wustl.common.util.logger.Logger
 			.getLogger(AbstractEntityCache.class);
 
 	/**
 	 * Set of all the DyanamicExtensions categories loaded in the database.
 	 */
 	protected List<CategoryInterface> deCategories = new ArrayList<CategoryInterface>();
 
 	/**
 	 * Set of all the entity groups loaded as metadata in caB2B.
 	 */
 	private final Set<EntityGroupInterface> cab2bEntityGroups = new HashSet<EntityGroupInterface>();
 
 	/**
 	 * The EntityCache object. Needed for singleton
 	 */
 	protected static AbstractEntityCache entityCache = null;
 
 	/**
 	 * Map with KEY as dynamic extension Entity's identifier and Value as Entity object
 	 */
 	protected Map<Long, EntityInterface> idVsEntity = new HashMap<Long, EntityInterface>();
 
 	/**
 	 * Map with KEY as dynamic extension Association's identifier and Value as Association object
 	 */
 	protected Map<Long, AssociationInterface> idVsAssociation = new HashMap<Long, AssociationInterface>();
 
 	/**
 	 * Map with KEY as dynamic extension Attribute's identifier and Value as Attribute object
 	 */
 	protected Map<Long, AttributeInterface> idVsAttribute = new HashMap<Long, AttributeInterface>();
 
 	/** The container id vs skip logic. */
 	protected Map<Long, SkipLogic> containerIdVsSkipLogic = new HashMap<Long, SkipLogic>();
 
 	/**
 	 * This map holds all the original association. Associations which are
 	 * replicated by cab2b are not present in this map Key : String to identify
 	 * a parent association uniquely.Generated by
 	 * {InheritanceUtil#generateUniqueId(AssociationInterface)} Value : Original
 	 * association for given string identifier
 	 */
 	protected Map<String, AssociationInterface> originalAssociations = new HashMap<String, AssociationInterface>();
 
 	/**
 	 * Map with KEY as a permissible value (PV) and VALUE as its Entity. This is
 	 * needed because there is no back pointer from PV to Entity
 	 */
 	protected Map<PermissibleValueInterface, EntityInterface> permissibleValueVsEntity = new HashMap<PermissibleValueInterface, EntityInterface>();
 
 	/**
 	 *  Map with KEY as dynamic extension Containers identifier and Value as Container object.
 	 */
 	protected Map<Long, ContainerInterface> idVscontainers = new HashMap<Long, ContainerInterface>();
 
 	/**
 	 * Map with KEY as dynamic extension Controls's identifier and Value as Control object
 	 */
 	protected Map<Long, ControlInterface> idVsControl = new HashMap<Long, ControlInterface>();
 
 	/**
 	 * This set contains all the categories which are in opened at this instance in Edit
 	 * mode by any user.
 	 */
 	protected Set<CategoryInterface> categoriesInUse = new HashSet<CategoryInterface>();
 
 	/** This counter is used for creating the temporary directories in case of create category task by more than one user at a time. */
 	protected long catFileNameCounter = 1L;
 
 	/** The category cache. */
 	private static Map<String, CategoryInterface> categoryCache = new LinkedHashMap<String, CategoryInterface>();
 
 	/** This set contains all the categories which are in opened at this instance in Edit mode by any user. */
 	protected Set<Long> containerInUse = new HashSet<Long>();
 
 	/** This set contains all the entity id which are in use. */
 	protected Set<Long> entitiesInUse = new HashSet<Long>();
 
 	/**
 	 * This method gives the singleton cache object. If cache is not present then it
 	 * throws {@link UnsupportedOperationException}
 	 * @return The singleton cache object.
 	 */
 	public static AbstractEntityCache getCache()
 	{
 		if (entityCache == null)
 		{
 			throw new UnsupportedOperationException("Cache not present.");
 		}
 		return entityCache;
 	}
 
 	/**
 	 * Private default constructor. To restrict the user from instantiating
 	 * explicitly.
 	 */
 	protected AbstractEntityCache()
 	{
 		refreshCache();
 	}
 
 	public final synchronized void cacheSkipLogic()
 	{
 		HibernateDAO hibernateDAO = null;
 		try
 		{
 			hibernateDAO = DynamicExtensionsUtility.getHibernateDAO();
 			Collection<SkipLogic> allSkipLogics = DynamicExtensionUtility
 					.getSkipLogicsDefinedForCategories(hibernateDAO);
 			for (SkipLogic skipLogic : allSkipLogics)
 			{
 				containerIdVsSkipLogic.put(skipLogic.getContainerIdentifier(), skipLogic);
 			}
 		}
 		catch (final DAOException e)
 		{
 			LOGGER.error(ERROR_CREATING_CACHE + e.getMessage());
 			throw new RuntimeException(EXCEPTION_CREATING_CACHE, e);
 		}
 		catch (DynamicExtensionsSystemException e)
 		{
 			LOGGER.error(ERROR_CREATING_CACHE + e.getMessage());
 			throw new RuntimeException(EXCEPTION_CREATING_CACHE, e);
 		}
 		finally
 		{
 			try
 			{
 				DynamicExtensionsUtility.closeDAO(hibernateDAO);
 			}
 			catch (final DynamicExtensionsSystemException e)
 			{
 				LOGGER.error("Exception encountered while closing session In EntityCache."
 						+ e.getMessage());
 				throw new RuntimeException(
 						"Exception encountered while closing session In EntityCache.", e);
 			}
 
 		}
 	}
 
 	public AbstractEntityCache(EntityGroupInterface entityGroupInterface)
 	{
 		refreshCache(entityGroupInterface);
 	}
 
 	/**
 	 * Refresh the entity cache.
 	 */
 	public final synchronized void refreshCache()
 	{
 
 		LOGGER.info("Initializing cache, this may take few minutes...");
 		clearCache();
 
 		HibernateDAO hibernateDAO = null;
 		Collection<EntityGroupInterface> entityGroups = null;
 		try
 		{
 			hibernateDAO = DynamicExtensionsUtility.getHibernateDAO();
 			entityGroups = DynamicExtensionUtility.getSystemGeneratedEntityGroups(hibernateDAO);
 			createCache(entityGroups);
 		}
 		catch (final DAOException e)
 		{
 			LOGGER.error("Error while Creating EntityCache. Error: " + e.getMessage());
 			throw new RuntimeException("Exception encountered while creating EntityCache!!", e);
 		}
 		catch (DynamicExtensionsSystemException e)
 		{
 			LOGGER.error("Error while Creating EntityCache. Error: " + e.getMessage());
 			throw new RuntimeException("Exception encountered while creating EntityCache!!", e);
 		}
 		finally
 		{
 			try
 			{
 				DynamicExtensionsUtility.closeDAO(hibernateDAO);
 			}
 			catch (final DynamicExtensionsSystemException e)
 			{
 				LOGGER.error("Exception encountered while closing session In EntityCache."
 						+ e.getMessage());
 				throw new RuntimeException(
 						"Exception encountered while closing session In EntityCache.", e);
 			}
 
 		}
 		LOGGER.info("Initializing cache DONE");
 	}
 
 	public final synchronized void refreshCache(EntityGroupInterface entityGroupInterface)
 	{
 		LOGGER.info("Initializing cache, this may take few minutes...");
 		clearCache();
 
 		Collection entityGroups = new ArrayList();
 		entityGroups.add(entityGroupInterface);
 		createCache(entityGroups);
 		LOGGER.info("Initializing cache DONE");
 	}
 
 	/**
 	 * Loads cacheable categories to cache
 	 */
 	public final synchronized void loadCategories()
 	{
 		try
 		{
 			HibernateDAO hibernateDAO = DynamicExtensionsUtility.getHibernateDAO();
 			deCategories = DynamicExtensionUtility.getAllCacheableCategories(hibernateDAO);
 			addCategoriesToCache(deCategories);
 		}
 		catch (final DAOException e)
 		{
 			LOGGER.error("Error while Creating EntityCache. Error: " + e.getMessage());
 			throw new RuntimeException("Exception encountered while creating EntityCache!!", e);
 		}
 		catch (DynamicExtensionsSystemException e)
 		{
 			LOGGER.error("Error while Creating EntityCache. Error: " + e.getMessage());
 			throw new RuntimeException("Exception encountered while creating EntityCache!!", e);
 		}
 	}
 
 	/**
 	 * Initializes the data structures by processing container & entity group one by one at a time.
 	 * @param categoryList list of containers to be cached.
 	 * @param entityGroups list of system generated entity groups to be cached.
 	 */
 	private void addCategoriesToCache(final Collection<CategoryInterface> categoryList)
 	{
 		for (final CategoryInterface category : categoryList)
 		{
 			createCategoryEntityCache(category.getRootCategoryElement());
 		}
 	}
 
 	/**
 	 * It will add the categoryEntity & there containers to the cache.
 	 * It will then recursively call the same method for the child category Entities.
 	 * @param categoryEntity
 	 */
 	private void createCategoryEntityCache(final CategoryEntityInterface categoryEntity)
 	{
 		for (final Object container : categoryEntity.getContainerCollection())
 		{
 			final ContainerInterface containerInterface = (ContainerInterface) container;
 			addContainerToCache(containerInterface);
 		}
 		for (final CategoryAssociationInterface categoryAssociation : categoryEntity
 				.getCategoryAssociationCollection())
 		{
 			final CategoryEntityInterface targetCategoryEntity = categoryAssociation
 					.getTargetCategoryEntity();
 			createCategoryEntityCache(targetCategoryEntity);
 
 		}
 	}
 
 	/**
 	 * Initializes the data structures by processing container & entity group one by one at a time.
 	 * @param categoryList list of containers to be cached.
 	 * @param entityGroups list of system generated entity groups to be cached.
 	 */
 	public void createCache(final Collection<EntityGroupInterface> entityGroups)
 	{
 		for (final EntityGroupInterface entityGroup : entityGroups)
 		{
 			cab2bEntityGroups.remove(entityGroup);
 			cab2bEntityGroups.add(entityGroup);
 
 			for (final EntityInterface entity : entityGroup.getEntityCollection())
 			{
//				if(!Constants.DISABLED.equals(entity.getActivityStatus()))
				//{
 					addEntityToCache(entity);	
				//}
 				
 			}
 		}
 	}
 
 	/**
 	 * @param entityGroup
 	 */
 	public void updateEntityGroup(final EntityGroupInterface entityGroup) {
 
 		Collection<EntityGroupInterface> entityGroups = getAllEntityGroupToUpdate(entityGroup);
 		for (EntityGroupInterface group : entityGroups) {
 			if (cab2bEntityGroups.contains(group)) {
 				cab2bEntityGroups.remove(group);
 			}
 			cab2bEntityGroups.add(group);
 			for (final EntityInterface entity : group.getEntityCollection()) {
 				addEntityToCache(entity);
 			}
 		}
 	}
 
 	/**
 	 * @param entityGroup
 	 * @return
 	 */
 	private Collection<EntityGroupInterface> getAllEntityGroupToUpdate(
 			final EntityGroupInterface entityGroup) {
 		Collection<EntityGroupInterface> entityGroups = new HashSet<EntityGroupInterface>();
 		entityGroups.add(entityGroup);
 		for (EntityInterface hookEntity : entityGroup.getEntityCollection()) {
 			for (AssociationInterface association : hookEntity
 					.getAssociationCollection()) {
 				if (!association.getTargetEntity().getEntityGroup().equals(
 						entityGroup)) {
 					entityGroups.add(association.getTargetEntity()
 							.getEntityGroup());
 				}
 			}
 		}
 		return entityGroups;
 	}
 	/**
 	 * It will clear all the in memory maps
 	 */
 	private void clearCache()
 	{
 		cab2bEntityGroups.clear();
 		idVscontainers.clear();
 		idVsAssociation.clear();
 		idVsAttribute.clear();
 		idVsControl.clear();
 		idVsEntity.clear();
 	}
 
 	/**
 	 * @param entityGroups
 	 */
 	public synchronized void createEntityGroupCache(final EntityGroupInterface entityGroup)
 	{
 		cab2bEntityGroups.add(entityGroup);
 		if (entityGroup.getEntityCollection() != null)
 		{
 			for (final EntityInterface entity : entityGroup.getEntityCollection())
 			{
 				addEntityToCache(entity);
 			}
 		}
 	}
 
 	/**
 	 * This method verifies weather the current category is in use by some user in
 	 * edit category mode.
 	 * @param category category which is to be checked for in use state.
 	 * @return true if category is in use else will return false.
 	 */
 	public synchronized boolean isCategoryInUse(final CategoryInterface category)
 	{
 		boolean isInUse = false;
 		if (categoriesInUse.contains(category))
 		{
 			isInUse = true;
 		}
 		return isInUse;
 
 	}
 
 	/**
 	 * This method will remove the given category from in Use set.
 	 * so that it will be available for other users to Edit.
 	 * @param category which is to be released.
 	 */
 	public synchronized void releaseCategoryFromUse(final CategoryInterface category)
 	{
 		categoriesInUse.remove(category);
 	}
 
 	/**
 	 * This method will add the given category in use.
 	 * @param category which is to be marked as in use.
 	 */
 	public synchronized void markCategoryInUse(final CategoryInterface category)
 	{
 		categoriesInUse.add(category);
 	}
 
 	/**
 	 * This method will return the next Counter for generating the temporary directory
 	 * for category creation.
 	 * @return category file name counter.
 	 */
 	public synchronized long getNextIdForCategoryFileGeneration()
 	{
 		return catFileNameCounter++;
 	}
 
 	/**
 	 * It will add the given container to the cache & also update the cache
 	 * for its controls and AbstractEntity
 	 * @param container
 	 */
 	private void addContainerToCache(final ContainerInterface container)
 	{
 
 		LOGGER.info("Add Container to Cache.............." + container);
 		idVscontainers.put(container.getId(), container);
 		createControlCache(container.getControlCollection());
 		addAbstractEntityToCache(container.getAbstractEntity());
 	}
 
 	/**
 	 * Adds all controls into cache.
 	 * @param controlCollection collection of control objects which are  to be cached.
 	 */
 	private void createControlCache(final Collection<ControlInterface> controlCollection)
 	{
 		if (controlCollection != null)
 		{
 			for (final ControlInterface control : controlCollection)
 			{
 				idVsControl.put(control.getId(), control);
 			}
 		}
 	}
 
 	/**
 	 * Adds abstract Entity (which can be 'CategoryEnity' or 'Entity') into cache.
 	 * @param abstractEntity which should be cached.
 	 * @param entityGroupsSet in which the entityGroup of the abstractEntity is cached.
 	 * @param categorySet in which the category of the abstractEntity is cached.
 	 */
 	private void addAbstractEntityToCache(final AbstractEntityInterface abstractEntity)
 	{
 		if (abstractEntity instanceof EntityInterface)
 		{
 			final EntityInterface entity = (EntityInterface) abstractEntity;
 			createEntityCache(entity);
 
 		}
 	}
 
 	/**
 	 * Adds all attribute of given entity into cache
 	 * @param entity Entity to process
 	 */
 	private void createAttributeCache(final EntityInterface entity)
 	{
 
 		LOGGER.info("Create Attribute Cache for Entity:" + entity);
 		for (final AttributeInterface attribute : entity
 				.getAttributeCollectionWithInheritedAttributes())
 		{
 			if(!Constants.DISABLED.equals(attribute.getActivityStatus()))
 			{
 				idVsAttribute.put(attribute.getId(), attribute);	
 			}else
 			{
 				entity.removeAttribute(attribute);
 			}
 			
 		}
 	}
 
 	/**
 	 * Adds all associations of given entity into cache
 	 * @param entity Entity to process
 	 */
 	public void createAssociationCache(final EntityInterface entity)
 	{
 
 		LOGGER.info("Create Association Cache for Entity:" + entity);
 
 		for (final AssociationInterface association : entity.getAssociationCollection())
 		{
 			LOGGER.info("Association......................" + association);
 			idVsAssociation.put(association.getId(), association);
 			if (!Utility.isInherited(association))
 			{
 				originalAssociations.put(Utility.generateUniqueId(association), association);
 			}
 		}
 	}
 
 	/**
 	 * Adds permissible values of all the attributes of given entity into cache
 	 * @param entity Entity whose permissible values are to be processed
 	 */
 	private void createPermissibleValueCache(final EntityInterface entity)
 	{
 		LOGGER.info("Create PV Cache for Entity:" + entity);
 		for (final AttributeInterface attribute : entity.getAttributeCollection())
 		{
 			for (final PermissibleValueInterface value : Utility.getPermissibleValues(attribute))
 			{
 				permissibleValueVsEntity.put(value, entity);
 			}
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.wustl.cab2b.common.cache.IEntityCache#getEntityOnEntityParameters(java.util.Collection)
 	 */
 	public MatchedClass getEntityOnEntityParameters(
 			final Collection<EntityInterface> patternEntityCollection)
 	{
 		final MatchedClass matchedClass = new MatchedClass();
 		for (final EntityInterface cachedEntity : idVsEntity.values())
 		{
 			for (final EntityInterface patternEntity : patternEntityCollection)
 			{
 				final MatchedClassEntry matchedClassEntry = CompareUtil.compare(cachedEntity,
 						patternEntity);
 				if (matchedClassEntry != null)
 				{
 					matchedClass.addEntity(cachedEntity);
 					matchedClass.addMatchedClassEntry(matchedClassEntry);
 				}
 			}
 		}
 		return matchedClass;
 	}
 
 	/**
 	 * Returns the Entity objects whose Attribute fields match with the
 	 * respective not null fields in the passed Attribute object.
 	 *
 	 * @param entity The entity object.
 	 * @return the Entity objects whose Attribute fields match with the
 	 *         respective not null fields in the passed Attribute object.
 	 */
 	public MatchedClass getEntityOnAttributeParameters(
 			final Collection<AttributeInterface> patternAttributeCollection)
 	{
 		final MatchedClass matchedClass = new MatchedClass();
 		for (final EntityInterface entity : idVsEntity.values())
 		{
 			for (final AttributeInterface cachedAttribute : entity.getAttributeCollection())
 			{
 				for (final AttributeInterface patternAttribute : patternAttributeCollection)
 				{
 					final MatchedClassEntry matchedClassEntry = CompareUtil.compare(
 							cachedAttribute, patternAttribute);
 					if (matchedClassEntry != null)
 					{
 						matchedClass.addMatchedClassEntry(matchedClassEntry);
 						matchedClass.addAttribute(cachedAttribute);
 						matchedClass.addEntity(cachedAttribute.getEntity());
 					}
 				}
 			}
 		}
 		return matchedClass;
 	}
 
 	/**
 	 * Returns the Entity objects whose Permissible value fields match with the
 	 * respective not null fields in the passed Permissible value object.
 	 *
 	 * @param entity The entity object.
 	 * @return the Entity objects whose Permissible value fields match with the
 	 *         respective not null fields in the passed Permissible value
 	 *         object.
 	 */
 	public MatchedClass getEntityOnPermissibleValueParameters(
 			final Collection<PermissibleValueInterface> patternPermValueColl)
 	{
 		final MatchedClass matchedClass = new MatchedClass();
 		for (final PermissibleValueInterface cachedPermissibleValue : permissibleValueVsEntity
 				.keySet())
 		{
 			for (final PermissibleValueInterface patternPermissibleValue : patternPermValueColl)
 			{
 				final EntityInterface cachedEntity = permissibleValueVsEntity
 						.get(cachedPermissibleValue);
 				final MatchedClassEntry matchedClassEntry = CompareUtil.compare(
 						cachedPermissibleValue, patternPermissibleValue, cachedEntity);
 				if (matchedClassEntry != null)
 				{
 					matchedClass.addEntity(cachedEntity);
 					matchedClass.addMatchedClassEntry(matchedClassEntry);
 				}
 			}
 		}
 		return matchedClass;
 
 	}
 
 	/**
 	 * It will return the EntityGroup With the given id if it present in cache, else will throw the
 	 * exception.
 	 * @param identifier
 	 * @return
 	 */
 	public EntityGroupInterface getEntityGroupById(final Long identifier)
 	{
 		EntityGroupInterface entityGroup = null;
 		for (final EntityGroupInterface group : cab2bEntityGroups)
 		{
 			if (group.getId().equals(identifier))
 			{
 				entityGroup = group;
 				break;
 			}
 		}
 		if (entityGroup == null)
 		{
 			throw new RuntimeException("Entity Group with given id is not present in cache : "
 					+ identifier);
 		}
 		return entityGroup;
 
 	}
 
 	/**
 	 * Returns the Entity for given Identifier
 	 *
 	 * @param identifier Id of the entity
 	 * @return Actual Entity for given id.
 	 * @throws DynamicExtensionsCacheException
 	 */
 	public EntityInterface getEntityById(final Long identifier)
 			throws DynamicExtensionsCacheException
 	{
 		if (identifier == 0)
 		{
 			LOGGER.error("Error while retriving entity for identifier 0.");
 			throw new DynamicExtensionsCacheException(
 					"Error while retriving entity for identifier 0.An error has occurred in the database. Please contact system administrator.");
 		}
 		if (!entitiesInUse.contains(identifier))
 		{
 			return getEntityByIdForCacheUpdate(identifier);
 		}
 		throw new DynamicExtensionsCacheException("Entity With Identifier :" + identifier
 				+ " is in use.");
 	}
 
 	/**
 	 * Gets the entity by id for cache update.
 	 * @param identifier the identifier
 	 * @return the entity by id for cache update
 	 */
 	public EntityInterface getEntityByIdForCacheUpdate(final Long identifier)
 	{
 		EntityInterface entity = idVsEntity.get(identifier);
 		if (entity == null)
 		{
 			try
 			{
 				entity = EntityManager.getInstance().getEntityByIdentifier(identifier);
 				if (entity == null)
 				{
 					throw new RuntimeException("Entity with given id is not present in cache : "
 							+ identifier);
 				}
 			}
 			catch (DynamicExtensionsSystemException e)
 			{
 				throw new RuntimeException("Entity with given id is not present in cache : "
 						+ identifier, e);
 			}
 			catch (DynamicExtensionsApplicationException e)
 			{
 				throw new RuntimeException("Entity with given id is not present in cache : "
 						+ identifier, e);
 			}
 
 		}
 		return entity;
 	}
 
 	/**
 	 * Checks if entity with given id is present in cache.
 	 *
 	 * @param identifier the entity id
 	 * @return <code>true</code> - if entity with given id is present in
 	 *         cache; <code>false</code> otherwise.
 	 */
 	public boolean isEntityPresent(final Long identifier)
 	{
 		return idVsEntity.containsKey(identifier);
 	}
 
 	/**
 	 * Returns the Attribute for given Identifier
 	 *
 	 * @param identifier Id of the Attribute
 	 * @return Actual Attribute for given id.
 	 */
 	public AttributeInterface getAttributeById(final Long identifier)
 	{
 		final AttributeInterface attribute = idVsAttribute.get(identifier);
 		if (attribute == null)
 		{
 			throw new RuntimeException("Attribute with given id is not present in cache : "
 					+ identifier);
 		}
 		return attribute;
 	}
 
 	/**
 	 * Returns the Association for given Identifier
 	 *
 	 * @param identifier Id of the Association
 	 * @return Actual Association for given id.
 	 * @throws DynamicExtensionsCacheException 
 	 */
 	public AssociationInterface getAssociationById(final Long identifier) throws DynamicExtensionsCacheException
 	{
 		final AssociationInterface association = idVsAssociation.get(identifier);
 		if (association == null)
 		{
 			throw new DynamicExtensionsCacheException("Association with given id is not present in cache : "
 					+ identifier);
 		}
 		return association;
 	}
 
 	/**
 	 * Returns the Association for given string.
 	 * Passed string MUST be of format specified in {@link Utility#generateUniqueId(AssociationInterface)}
 	 * @param uniqueStringIdentifier unique String Identifier
 	 * @return Actual Association for given string identifier.
 	 */
 	public AssociationInterface getAssociationByUniqueStringIdentifier(
 			final String uniqueStringIdentifier)
 	{
 		final AssociationInterface association = originalAssociations.get(uniqueStringIdentifier);
 		if (association == null)
 		{
 			throw new RuntimeException(
 					"Association with given uniqueStringIdentifier is not present in cache : "
 							+ uniqueStringIdentifier);
 		}
 		return association;
 	}
 
 	/**
 	 * It will add the Given Entity in the cache .
 	 * @param entity
 	 */
 	private void createEntityCache(final EntityInterface entity)
 	{
 
 		LOGGER.info("create Entity Cache for............." + entity);
 		if(!Constants.DISABLED.equals(entity.getActivityStatus()))
 		{
 			idVsEntity.put(entity.getId(), entity);	
 		}
 		
 		createAttributeCache(entity);
 		createAssociationCache(entity);
 		createPermissibleValueCache(entity);
 	}
 
 	/**
 	 * It will add the given Entity to the cache & will also update the corresponding
 	 * controls containers & attributes.
 	 * @param entity
 	 */
 	public void addEntityToCache(final EntityInterface entity)
 	{
 
 		LOGGER.info("Add Entity................" + entity);
 
 		if ((entity.getContainerCollection() == null) || entity.getContainerCollection().isEmpty())
 		{
 			LOGGER.info("Create EntityCache........");
 			createEntityCache(entity);
 		}
 		else
 		{
 
 			LOGGER.info("Add Entity Container to Cache............");
 			for (final Object container : entity.getContainerCollection())
 			{
 				final ContainerInterface containerInterface = (ContainerInterface) container;
 				addContainerToCache(containerInterface);
 			}
 		}
 	}
 
 	/**
 	 * Returns all the entity groups registered with the instance of cache.
 	 * @return Returns all the registered entity groups
 	 */
 	public Collection<EntityGroupInterface> getEntityGroups()
 	{
 		return cab2bEntityGroups;
 	}
 
 	/**
 	 * This method returns the entity group of given name from cache.
 	 * @param name name of the entity group
 	 * @return entity group
 	 */
 	public EntityGroupInterface getEntityGroupByName(final String name)
 	{
 		EntityGroupInterface entityGroup = null;
 		for (final EntityGroupInterface group : cab2bEntityGroups)
 		{
 			if ((group.getName() != null) && group.getName().equals(name))
 			{
 				entityGroup = group;
 			}
 		}
 		return entityGroup;
 	}
 
 	/**
 	 * It will return BaseAbstractAttribute with the id as given identifier in the parameter.
 	 * @param identifier
 	 * @return categoryAttribute with given identifier
 	 */
 	public BaseAbstractAttributeInterface getBaseAbstractAttributeById(final Long identifier)
 	{
 		BaseAbstractAttributeInterface baseAbstractAttribute = null;
 		baseAbstractAttribute = idVsAttribute.get(identifier);
 		if (baseAbstractAttribute == null)
 		{
 			baseAbstractAttribute = idVsAssociation.get(identifier);
 		}
 		if (baseAbstractAttribute == null)
 		{
 			baseAbstractAttribute = getCategoryAttributeById(identifier);
 		}
 
 		if (baseAbstractAttribute == null)
 		{
 			throw new RuntimeException(
 					"BaseAbstractAttribute with given id is not present in cache : " + identifier);
 		}
 		return baseAbstractAttribute;
 	}
 
 	private CategoryAttributeInterface getCategoryAttributeById(Long identifier)
 	{
 		CategoryAttributeInterface catAttribute = null;
 		try
 		{
 			catAttribute = (CategoryAttributeInterface) AbstractBaseMetadataManager
 					.getObjectByIdentifier(CategoryAttributeInterface.class.getName(), identifier
 							.toString());
 		}
 		catch (DynamicExtensionsSystemException e)
 		{
 			LOGGER.error("Exception encountered while fetching the category attribute with id "
 					+ identifier, e);
 		}
 		return catAttribute;
 	}
 
 	/**
 	* It will return the Container with the id as given identifier in the parameter.
 	* @param identifier
 	* @return Container with given identifier
 	 * @throws DynamicExtensionsCacheException
 	*/
 
 	public ContainerInterface getContainerById(final Long identifier)
 			throws DynamicExtensionsCacheException
 	{
 		ContainerInterface container = idVscontainers.get(identifier);
 		try
 		{
 			if (container == null)
 			{
 				container = (ContainerInterface) CategoryManager.getObjectByIdentifier(
 						ContainerInterface.class.getName(), identifier.toString());
 			}
 		}
 		catch (DynamicExtensionsSystemException e)
 		{
 			throw new DynamicExtensionsCacheException(ApplicationProperties.getValue(
 					CONTAINER_NOT_FOUND, String.valueOf(identifier)), e);
 		}
 		if (container == null)
 		{
 			throw new DynamicExtensionsCacheException(ApplicationProperties.getValue(
 					CONTAINER_NOT_FOUND, String.valueOf(identifier)));
 		}
 		if (containerInUse.contains(container.getId()))
 		{
 			throw new DynamicExtensionsCacheException(ApplicationProperties
 					.getValue(DATA_ENTRY_CONTAINER_IN_USE));
 		}
 		return container;
 	}
 
 	/**
 	 * It will return the Control with the id as given identifier in the parameter.
 	 * @param identifier
 	 * @return Control with given identifier
 	 */
 	public ControlInterface getControlById(final Long identifier)
 	{
 		ControlInterface control = idVsControl.get(identifier);
 		try
 		{
 			if (control == null)
 			{
 				control = (ControlInterface) ((AbstractMetadataManager) CategoryManager
 						.getInstance()).getObjectByIdentifier(ControlInterface.class.getName(),
 						identifier.toString());
 			}
 		}
 		catch (DynamicExtensionsSystemException e)
 		{
 			throw new RuntimeException("Exception encounter while fetching the Control with id +"
 					+ identifier, e);
 		}
 		if (control == null)
 		{
 			throw new RuntimeException("control with given id is not present in cache : "
 					+ identifier);
 		}
 		return control;
 	}
 
 	public synchronized void updatePermissibleValues(EntityInterface entity, Long attributeId,
 			AttributeTypeInformationInterface attrTypeInfo)
 	{
 		AttributeInterface cachedattribute = entityCache.getAttributeById(attributeId);
 		cachedattribute.setAttributeTypeInformation(attrTypeInfo);
 
 		Set<Entry<PermissibleValueInterface, EntityInterface>> pvVsEntity = permissibleValueVsEntity
 				.entrySet();
 		List<PermissibleValueInterface> toBeRemovedPVs = new ArrayList<PermissibleValueInterface>();
 		for (Entry<PermissibleValueInterface, EntityInterface> entry : pvVsEntity)
 		{
 			if (entry.getValue().getName().equalsIgnoreCase(entity.getName()))
 			{
 				toBeRemovedPVs.add(entry.getKey());
 			}
 		}
 		for (PermissibleValueInterface pvList : toBeRemovedPVs)
 		{
 			permissibleValueVsEntity.remove(pvList);
 		}
 		updatePermissibleValueMap(entity);
 
 	}
 
 	private void updatePermissibleValueMap(EntityInterface entity)
 	{
 		Collection<AttributeInterface> allAttributes = entity.getAllAttributes();
 		Collection<PermissibleValueInterface> allPVs;
 		for (AttributeInterface attribute : allAttributes)
 		{
 			UserDefinedDEInterface userDefinedDE = (UserDefinedDEInterface) attribute
 					.getAttributeTypeInformation().getDataElement();
 			if (userDefinedDE != null)
 			{
 				allPVs = userDefinedDE.getPermissibleValues();
 				for (PermissibleValueInterface permissibleValue : allPVs)
 				{
 					permissibleValueVsEntity.put(permissibleValue, entity);
 				}
 			}
 		}
 	}
 
 	/**
 	 * This method will add the given category to cache.
 	 * @param category category to be added.
 	 */
 	public synchronized void addCategoryToCache(final CategoryInterface category)
 	{
 		if (deCategories.contains(category))
 		{
 			LOGGER.info("adding category to cache" + category);
 			deCategories.remove(category);
 			deCategories.add(category);
 			createCategoryEntityCache(category.getRootCategoryElement());
 			LOGGER.info("adding category to cache done");
 		}
 
 	}
 
 	/**
 	 * This method will add the given container id list to in use/under maintenance containers.
 	 * @param container ids which are to be marked as in use.
 	 */
 	public synchronized void lockAllContainer(Set<Long> caontainreIdList)
 	{
 		containerInUse.addAll(caontainreIdList);
 	}
 
 	/**
 	 * This method will release all the container ids.
 	 * @param conatiner ids to be released.
 	 */
 	public synchronized void releaseAllContainer(Set<Long> containreIdList)
 	{
 		containerInUse.removeAll(containreIdList);
 	}
 
 	/**
 	 * This method will add the given container id to in use/under maintenance containers.
 	 * @param category which is to be marked as in use.
 	 */
 	public synchronized void lockContainer(Long containerId)
 	{
 		containerInUse.add(containerId);
 	}
 
 	/**
 	 * This method will release the given container.
 	 * @param container id which is to be released.
 	 */
 	public synchronized void releaseContainer(Long containerId)
 	{
 		containerInUse.remove(containerId);
 	}
 
 	public boolean isFormAvailable(Long containerId)
 	{
 		return !containerInUse.contains(containerId);
 	}
 
 	/**
 	 * Lock all entities. This method will lock all the entities as Entities in Use.
 	 * @param allEntities the all entities
 	 */
 	public synchronized void lockAllEntities(Collection<EntityInterface> allEntities)
 	{
 		for (EntityInterface entityInterface : allEntities)
 		{
 			entitiesInUse.add(entityInterface.getId());
 		}
 	}
 
 	/**
 	 * Release all entities. This method will release all the entities as Entities in Use.
 	 * @param allEntities the all entities that are to release
 	 */
 	public synchronized void releaseAllEntities(Collection<EntityInterface> allEntities)
 	{
 		for (EntityInterface entityInterface : allEntities)
 		{
 			entitiesInUse.remove(entityInterface.getId());
 		}
 	}
 
 	/**
 	 * Gets the skip logic by container identifier.
 	 * @param containerIdentifier the container identifier
 	 * @return the skip logic by container identifier
 	 */
 	public SkipLogic getSkipLogicByContainerIdentifier(Long containerIdentifier)
 	{
 		return containerIdVsSkipLogic.get(containerIdentifier);
 	}
 
 	/**
 	 * Delete skip logic from cache.
 	 * @param containerIdentifier the container identifier
 	 */
 	public synchronized void deleteSkipLogicFromCache(Long containerIdentifier)
 	{
 		containerIdVsSkipLogic.remove(containerIdentifier);
 	}
 
 	/**
 	 * Update skip logic in cache.
 	 * @param skipLogic the skip logic
 	 */
 	public synchronized void updateSkipLogicInCache(SkipLogic skipLogic)
 	{
 		containerIdVsSkipLogic.put(skipLogic.getContainerIdentifier(), skipLogic);
 	}
 
 	/**
 	 * This method will add the given category to cache.
 	 * @param category category to be added.
 	 */
 	public synchronized void addCategoryToTempCache(final CategoryInterface category)
 	{
 		categoryCache.remove(category);
 		categoryCache.put(category.getName(), category);
 	}
 
 	/**
 	 * @param categoryName
 	 * @return
 	 * @throws DynamicExtensionsSystemException
 	 */
 	public CategoryInterface getCategoryByName(String categoryName)
 			throws DynamicExtensionsSystemException
 	{
 		CategoryManagerInterface categoryManager = CategoryManager.getInstance();
 		CategoryInterface categoryInterface = categoryCache.get(categoryName);
 		if (categoryInterface == null)
 		{
 			categoryInterface = categoryManager.getCategoryByName(categoryName);
 			if (categoryInterface != null)
 			{
 				addCategoryToTempCache(categoryInterface);
 			}
 
 		}
 		return categoryInterface;
 	}
 	
 	public boolean isCaCoreGenerated(Long id) throws DynamicExtensionsCacheException
 	{
 		ContainerInterface container= getContainerById(id);
 		return container.getAbstractEntity().getEntityGroup().getIscaCOREGenerated();
 	}
 	
 	public Collection<NameValueBean> getEntityGroupsByEntity(EntityInterface hookEntity)
 	{
 		Collection<NameValueBean> entityGroups = new HashSet<NameValueBean>();
 		
 		for (AssociationInterface association : hookEntity.getAssociationCollection())
 		{
 			if (!association.getTargetEntity().getEntityGroup().getIsSystemGenerated())
 			{
 				NameValueBean nvb = new NameValueBean();
 				nvb.setName(association.getTargetEntity().getEntityGroup().getName());
 				nvb.setValue(association.getTargetEntity().getEntityGroup().getId());
 				entityGroups.add(nvb);
 			}
 		}
 		return entityGroups;
 	}
 }
