 
 package edu.wustl.cab2b.server.path;
 
 import java.sql.Connection;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.log4j.Logger;
 
 import edu.common.dynamicextensions.domaininterface.AssociationInterface;
 import edu.common.dynamicextensions.domaininterface.AttributeInterface;
 import edu.common.dynamicextensions.domaininterface.EntityGroupInterface;
 import edu.common.dynamicextensions.domaininterface.EntityInterface;
 import edu.wustl.cab2b.common.exception.RuntimeException;
 import edu.wustl.cab2b.common.util.Constants;
 import edu.wustl.cab2b.common.util.Utility;
 import edu.wustl.cab2b.server.cache.EntityCache;
 import edu.wustl.cab2b.server.util.SQLQueryUtil;
 import edu.wustl.common.querysuite.metadata.associations.IAssociation;
 import edu.wustl.common.querysuite.metadata.associations.IInterModelAssociation;
 import edu.wustl.common.querysuite.metadata.associations.IIntraModelAssociation;
 import edu.wustl.common.querysuite.metadata.associations.impl.InterModelAssociation;
 import edu.wustl.common.querysuite.metadata.associations.impl.IntraModelAssociation;
 import edu.wustl.common.querysuite.metadata.path.CuratedPath;
 import edu.wustl.common.querysuite.metadata.path.ICuratedPath;
 import edu.wustl.common.querysuite.metadata.path.IPath;
 import edu.wustl.common.querysuite.metadata.path.Path;
 
 /**
  * This class provides methods to find all possible paths in which a collection of classes can be connected to a
  * destination. It finds inter model as well as intra model paths. A path is defined as ordered list of associations.
  * This class internally refers to tables PATH,INTER_MODEL_ASSOCIATION, INTRA_MODEL_ASSOCIATION,ASSOCIATION.
  * 
  * @author Chandrakant Talele
  */
 public class PathFinder
 {
 
 	private static final Logger logger = edu.wustl.common.util.logger.Logger
 			.getLogger(PathFinder.class);
 
 	private static PathFinder pathFinder;
 
 	private Set<InterModelConnection> interModelConnections;
 
 	private Map<String, List<PathRecord>> pathRecordCache;
 
 	private Map<Long, IAssociation> idVsAssociation;
 
 	private Map<Long, PathRecord> idVsPathRecord;
 
 	private Map<String, Set<ICuratedPath>> entitySetVsCuratedPath;
 
 	private Map<Long, List<IInterModelAssociation>> leftEntityVsInterModelAssociation;
 
 	// this is temp fix for query module for method IPath getPathForAssociations(List<IIntraModelAssociation>
 	// associations)
 	private Map<String, PathRecord> intermediatePathVsPathRecord;
 
 	// this is temp fix for query module for method IPath getPathForAssociations(List<IIntraModelAssociation>
 	// associations)
 	private Map<Long, Long> deIdVsAssociationId;
 
 	/**
 	 * This gives the singleton instance of the pathFinder class. If not it creates it. For one run, this should be
 	 * called at least once. The instance created can then be accessed by {@link PathFinder#getInstance()}
 	 * @param con Database connection to use
 	 * @return Returns the singleton instance of the pathFinder class.
 	 */
 	public static synchronized PathFinder getInstance(Connection con)
 	{
 		if (pathFinder == null)
 		{
 			logger.info("PathFinder Called first Time.Loading cache...");
 			EntityCache.getInstance();
 			pathFinder = new PathFinder();
 			refreshCache(con, false);
 		}
 		return pathFinder;
 	}
 
 	/**
 	 * Drops all the static data structures and creates them with latest database state
 	 * 
 	 * @param con Database connection to use
 	 * @param updateMetadataCache true if pathfinder is expected to update metadata cache.
 	 */
 	public static synchronized void refreshCache(Connection con, boolean updateMetadataCache)
 	{
 		if (updateMetadataCache)
 		{
 
 		}
 		pathFinder.populateCache(con);
 	}
 
 	/**
 	 * Drops all the static data structures and creates them with latest database state.
 	 * Note: This won't refresh the metadata cache
 	 * @param con Database connection to use
 	 * @deprecated Use {@link PathFinder#refreshCache(Connection, boolean)}
 	 */
 	@Deprecated
 	public static synchronized void refreshCache(Connection con)
 	{
 		refreshCache(con, false);
 	}
 
 	/**
 	 * Populates all the data structures needed for path finding.
 	 * @param con Database connection to use
 	 */
 	private void populateCache(Connection con)
 	{
 		setInterModelConnections(cacheInterModelConnections(con));
 		pathRecordCache = cachePathRecords(con);
 		idVsAssociation = cacheAssociationTypes(con);
 		setEntitySetVsCuratedPath(cacheCuratedPaths());
 	}
 
 	/**
 	 * @return Returns the singleton instance of the pathFinder class.
 	 */
 	public static synchronized PathFinder getInstance()
 	{
 		if (pathFinder == null)
 		{
 			throw new IllegalStateException(
 					"to get PathFinder with this method, it must be initialized using a connection before this call");
 		}
 		return pathFinder;
 	}
 
 	/**
 	 * Finds all curated paths defined for given source and destination entity. If no curated path is present, a empty
 	 * set will be returned.
 	 * 
 	 * @param source The source entity
 	 * @param destination The destination entity
 	 * @return Set of curated paths
 	 */
 	public Set<ICuratedPath> getCuratedPaths(EntityInterface source, EntityInterface destination)
 	{
 		HashSet<EntityInterface> entitySet = new HashSet<EntityInterface>(2);
 		entitySet.add(source);
 		entitySet.add(destination);
 		Set<ICuratedPath> curatedPathSet = autoConnect(entitySet);
 		// Filtering the paths. As paths in a curated paths does not form cycles.So checking only first path
 		// in the pathList of each curated path will be sufficient
 		Set<ICuratedPath> curatedPaths = new HashSet<ICuratedPath>();
 		for (ICuratedPath curatedPath : curatedPathSet)
 		{
 			Set<IPath> paths = curatedPath.getPaths();
 			// a curated path will have atleast one path present in it. no need to check it.
 			IPath path = paths.iterator().next();
 			if (source.getId().equals(path.getSourceEntity().getId()))
 			{
 				// this is a matching path.
 				curatedPaths.add(curatedPath);
 			}
 		}
 		return curatedPaths;
 	}
 
 	/**
 	 * Finds all curated paths defined for given set of entities.
 	 * 
 	 * @param entitySet Set of entities
 	 * @return Set of curated paths
 	 */
 	public Set<ICuratedPath> autoConnect(Set<EntityInterface> entitySet)
 	{
 		String entityIdSet = CuratedPath.getStringRepresentation(entitySet);
 		Set<ICuratedPath> curatedPathSet = entitySetVsCuratedPath.get(entityIdSet);
 
 		if (curatedPathSet == null)
 		{
 			curatedPathSet = new HashSet<ICuratedPath>(0);
 		}
 		return curatedPathSet;
 	}
 
 	/**
 	 * Finds all possible paths in which a collection of classes (sourceList) can be connected to a destination.
 	 * Returns map with key : EntityInterface and value as "List of IPath". VALUE in Map is list of all possible paths
 	 * from Entity which is its KEY and passed destination.<br>
 	 * KeySet of the map contains only those classes from which there are one or more paths present to destination.
 	 * That is KeySet will be subset of collection of classes passed as "sourceList". This method can be used when a
 	 * new class is added in DAG view.
 	 * 
 	 * @param sourceList collection of classes each of which will be treated as source.
 	 * @param destination End of the path
 	 * @return Returns the Map<EntityInterface,List<IPath>>
 	 */
 	public Map<EntityInterface, List<IPath>> getAllPossiblePaths(List<EntityInterface> sourceList,
 			EntityInterface destination)
 	{
 		logger.debug("Entering in method getAllPossiblePaths()");
 		Map<EntityInterface, List<IPath>> mapToReturn = new HashMap<EntityInterface, List<IPath>>(
 				sourceList.size());
 
 		for (EntityInterface srcEntity : sourceList)
 		{
 			List<IPath> pathList = getAllPossiblePaths(srcEntity, destination);
 			if (!pathList.isEmpty())
 			{
 				mapToReturn.put(srcEntity, pathList);
 			}
 		}
 		return mapToReturn;
 	}
 
 	/**
 	 * Finds all possible paths present between given "source" to "destination". 
 	 * Returns list of all possible paths.<br>
 	 * If no path found it returns a empty list. This method can be used when a new class is added in DAG view.
 	 * 
 	 * @param source Start of the path.
 	 * @param destination End of the path
 	 * @return Returns the List<IPath>
 	 */
 	public List<IPath> getAllPossiblePaths(EntityInterface source, EntityInterface destination)
 	{
 		logger.debug("Entering in method getAllPossiblePaths()");
 		Long desEntityId = destination.getId();
 		String desName = destination.getName();
 		EntityGroupInterface desEntityGroup = Utility.getEntityGroup(destination);
 
 		Long srcEntityId = source.getId();
 		String srcName = source.getName();
 		EntityGroupInterface srcEntityGroup = Utility.getEntityGroup(source);
 		// Are they belong to same entity group ??
 		if (desEntityGroup.equals(srcEntityGroup))
 		{
 			logger.info("Finding intramodel paths between : " + srcName + " and " + desName);
 			List<PathRecord> pathRecords = getPathRecords(srcEntityId, desEntityId);
			List<IPath> pathList = new ArrayList<IPath>(getPathList(pathRecords));
			return pathList;
 		}
 		else
 		{
 			// they are from different models
 			logger.info("Finding intermodel paths between : " + srcName + " and " + desName);
 			for (InterModelConnection interModelConnection : interModelConnections)
 			{
 
 				Long leftEntityId = interModelConnection.getLeftEntityId();
 				EntityGroupInterface leftEntityGroup = Utility.getEntityGroup(getCache()
 						.getEntityById(leftEntityId));
 				if (srcEntityGroup.equals(leftEntityGroup))
 				{
 
 					Long rightEntityId = interModelConnection.getRightEntityId();
 					EntityGroupInterface rightEntityGroup = Utility.getEntityGroup(getCache()
 							.getEntityById(rightEntityId));
 
 					if (rightEntityGroup.equals(desEntityGroup))
 					{
 						List<Path> pathsSrcToLeftEntity = new ArrayList<Path>();
 						if (!srcEntityId.equals(leftEntityId))
 						{
 							List<PathRecord> pathRecordsSrcToLeftEntity = getPathRecords(
 									srcEntityId, leftEntityId);
 							if (pathRecordsSrcToLeftEntity.isEmpty())
 							{
 								continue;
 							}
 							pathsSrcToLeftEntity = getPathList(pathRecordsSrcToLeftEntity);
 						}
 						List<Path> pathsRightEntityToDes = new ArrayList<Path>();
 						if (!rightEntityId.equals(desEntityId))
 						{
 							List<PathRecord> pathRecordsRightEntityToDes = getPathRecords(
 									rightEntityId, desEntityId);
 							if (pathRecordsRightEntityToDes.isEmpty())
 							{
 								continue;
 							}
 							pathsRightEntityToDes = getPathList(pathRecordsRightEntityToDes);
 						}
 						InterModelAssociation association = getInterModelAssociation(interModelConnection);
 						List<IPath> pathList = connectPaths(pathsSrcToLeftEntity, association,
 								pathsRightEntityToDes);
 						return pathList;
 					}
 				}
 			}
 		}
 		return new ArrayList<IPath>(0);
 	}
 
 	/**
 	 * Connect each path of passes "prePaths" to each path of passed "postPaths" by association. If "prePaths" is
 	 * empty then "association" will be prefixed to all paths in "postPaths". If "postPaths" is empty then
 	 * "association" will be postfixed to all paths in "prePaths". else the size of the returned list will be =
 	 * prePaths.size() * postPaths.size().
 	 * 
 	 * @param prePaths list of paths which will be prefixed in the returned path list.
 	 * @param association The association which serves as connection between those.
 	 * @param postPaths list of paths which will be postfixed in the returned path list.
 	 * @return Path list generated after concatination.
 	 */
 	List<IPath> connectPaths(List<Path> prePaths, InterModelAssociation association,
 			List<Path> postPaths)
 	{
 		List<IPath> list = new ArrayList<IPath>(prePaths.size() * postPaths.size());
 		if (prePaths.isEmpty() && postPaths.isEmpty())
 		{
 			List<IAssociation> allAssociation = new ArrayList<IAssociation>(1);
 			allAssociation.add(association.clone());
 			list.add(new Path(association.getSourceEntity(), association.getTargetEntity(),
 					allAssociation));
 
 		}
 		else if (prePaths.isEmpty())
 		{
 			for (Path postPath : postPaths)
 			{
 				List<IAssociation> allAssociation = new ArrayList<IAssociation>();
 				allAssociation.add(association.clone());
 				allAssociation.addAll(postPath.getIntermediateAssociations());
 				list.add(new Path(association.getSourceEntity(), postPath.getTargetEntity(),
 						allAssociation));
 			}
 		}
 		else if (postPaths.isEmpty())
 		{
 			for (Path prePath : prePaths)
 			{
 				List<IAssociation> allAssociation = new ArrayList<IAssociation>(prePath
 						.getIntermediateAssociations());
 				allAssociation.add(association.clone());
 				list.add(new Path(prePath.getSourceEntity(), association.getTargetEntity(),
 						allAssociation));
 			}
 		}
 		else
 		{
 			for (Path prePath : prePaths)
 			{
 				for (Path postPath : postPaths)
 				{
 					List<IAssociation> allAssociation = new ArrayList<IAssociation>(prePath
 							.getIntermediateAssociations());
 					allAssociation.add(association.clone());
 					allAssociation.addAll(postPath.getIntermediateAssociations());
 					list.add(new Path(prePath.getSourceEntity(), postPath.getTargetEntity(),
 							allAssociation));
 				}
 			}
 		}
 		return list;
 	}
 
 	/**
 	 * Converts given interModelConnection to InterModelAssociation and returns it.
 	 * 
 	 * @param interModelConnection InterModelConnection to transform
 	 * @return Returns the InterModelAssociation
 	 */
 	InterModelAssociation getInterModelAssociation(InterModelConnection interModelConnection)
 	{
 		Long leftEntityId = interModelConnection.getLeftEntityId();
 		Long leftAttrId = interModelConnection.getLeftAttributeId();
 
 		AttributeInterface leftAttr = getAttribute(leftEntityId, leftAttrId);
 
 		Long rightEntityId = interModelConnection.getRightEntityId();
 		Long rightAttrId = interModelConnection.getRightAttributeId();
 		AttributeInterface rightAttr = getAttribute(rightEntityId, rightAttrId);
 
 		return new InterModelAssociation(leftAttr, rightAttr);
 	}
 
 	/**
 	 * Returns all the intermodel connections available in the system.
 	 * 
 	 * @param con Database connection to use for fetching the intermodel connections
 	 * @return List of all InterModelConnection.
 	 */
 	private Set<InterModelConnection> cacheInterModelConnections(Connection con)
 	{
 		String sql = "select LEFT_ENTITY_ID,LEFT_ATTRIBUTE_ID,RIGHT_ENTITY_ID,RIGHT_ATTRIBUTE_ID from INTER_MODEL_ASSOCIATION";
 		String[][] result = executeQuery(sql, con);
 		Set<InterModelConnection> interModelConnections = new HashSet<InterModelConnection>(
 				result.length);
 		for (int i = 0; i < result.length; i++)
 		{
 			Long leftEntityId = Long.parseLong(result[i][0]);
 			Long leftAttrId = Long.parseLong(result[i][1]);
 			Long rightEntityId = Long.parseLong(result[i][2]);
 			Long rightAttrId = Long.parseLong(result[i][3]);
 
 			AttributeInterface leftAttr = getAttribute(leftEntityId, leftAttrId);
 			AttributeInterface rightAttr = getAttribute(rightEntityId, rightAttrId);
 
 			interModelConnections.add(new InterModelConnection(leftAttr, rightAttr));
 		}
 
 		return interModelConnections;
 	}
 
 	/**
 	 * Generates a Path for each PathRecord and returns it.
 	 * 
 	 * @param pathRecords Array of PathRecords to process
 	 * @param con Database connection to use.
 	 * @return Returns the List of Generated Paths.
 	 */
 	List<Path> getPathList(List<PathRecord> pathRecords)
 	{
 		List<Path> pathList = new ArrayList<Path>(pathRecords.size());
 		for (PathRecord pathRecord : pathRecords)
 		{
 			pathList.add(getPath(pathRecord));
 		}
 		return pathList;
 	}
 
 	/**
 	 * Generates a Path for given PathRecord and returns it.
 	 * 
 	 * @param pathRecord PathRecords to convert.
 	 * @param con Database connection to use.
 	 * @return Returns the generated Path.
 	 */
 	Path getPath(PathRecord pathRecord)
 	{
 		List<IAssociation> associations = new ArrayList<IAssociation>();
 		for (Long associationId : pathRecord.getAssociationSequence())
 		{
 			associations.add(idVsAssociation.get(associationId));
 		}
 		Long firstEntityId = pathRecord.getFirstEntityId();
 		Long lastEntityId = pathRecord.getLastEntityId();
 
 		EntityInterface firstEntity = getCache().getEntityById(firstEntityId);
 		EntityInterface lastEntity = getCache().getEntityById(lastEntityId);
 		return new Path(pathRecord.getPathId(), firstEntity, lastEntity, associations);
 	}
 
 	/**
 	 * @param id Path id for which a path is expected.
 	 * @return IPath object for given pathId
 	 */
 	public IPath getPathById(Long id)
 	{
 		PathRecord pathRecord = idVsPathRecord.get(id);
		Path path = getPath(pathRecord);
		return path;
 	}
 
 	/**
 	 * This method returns all the paths present between given "srcId" and "desId".
 	 * 
 	 * @param srcId Source entity Id
 	 * @param desId Target Entity Id
 	 * @return List of PathRecord
 	 */
 	private List<PathRecord> getPathRecords(long srcId, long desId)
 	{
 		String key = srcId + Constants.CONNECTOR + desId;
 		if (pathRecordCache.containsKey(key))
 		{
 			return pathRecordCache.get(key);
 		}
 		return new ArrayList<PathRecord>(0);
 	}
 
 	/**
 	 * This method caches all the paths table PATH in path records form. It also populates Map of path id V/s its
 	 * PathRecord
 	 * 
 	 * @param connection Database connection to use to fire queries.
 	 * @return Map with String as KEY and VALUE is List<PathRecord> KEY string will be formed using concatenating
 	 *         first entity id to last entity id by {@link PathConstants#ID_CONNECTOR}
 	 */
 	private Map<String, List<PathRecord>> cachePathRecords(Connection connection)
 	{
 		logger.debug("Entering in method cachePathRecords");
 		String sql = "select PATH_ID,FIRST_ENTITY_ID,INTERMEDIATE_PATH,LAST_ENTITY_ID from PATH";
 
 		String[][] resultSet = executeQuery(sql, connection);
 		Map<String, List<PathRecord>> pathRecordCache = new HashMap<String, List<PathRecord>>(
 				resultSet.length);
 		idVsPathRecord = new HashMap<Long, PathRecord>(resultSet.length);
 		intermediatePathVsPathRecord = new HashMap<String, PathRecord>(resultSet.length);
 		for (int i = 0; i < resultSet.length; i++)
 		{
 			PathRecord pathRecord = getPathRecord(resultSet[i]);
 			String key = pathRecord.getFirstEntityId() + Constants.CONNECTOR
 					+ pathRecord.getLastEntityId();
 			idVsPathRecord.put(pathRecord.getPathId(), pathRecord);
 			intermediatePathVsPathRecord.put(pathRecord.getIntermediateAssociations(), pathRecord);
 			if (pathRecordCache.containsKey(key))
 			{
 				pathRecordCache.get(key).add(pathRecord);
 			}
 			else
 			{
 				ArrayList<PathRecord> list = new ArrayList<PathRecord>();
 				list.add(pathRecord);
 				pathRecordCache.put(key, list);
 			}
 		}
 		logger.debug("Leaving method cachePathRecords");
 		return pathRecordCache;
 	}
 
 	/**
 	 * @param record String[] returned by SQL
 	 * @return Path record for that record
 	 */
 	PathRecord getPathRecord(String[] record)
 	{
 		Long pathId = Long.parseLong(record[0]);
 		Long firstId = Long.parseLong(record[1]);
 		String path = record[2];
 		Long lastId = Long.parseLong(record[3]);
 		return new PathRecord(pathId, firstId, path, lastId);
 	}
 
 	/**
 	 * Returns the AttributeInterface whose id is passed "attributeId" and parent is entity of id "entityId".
 	 * 
 	 * @param entityId Id of parent Entity.
 	 * @param attributeId Id of the required attribiute.
 	 * @return Attribute of passed entity with given id.
 	 */
 	AttributeInterface getAttribute(Long entityId, Long attributeId)
 	{
 		return getCache().getEntityById(entityId).getAttributeByIdentifier(attributeId);
 	}
 
 	/**
 	 * Returns all the InterModelAssociations where given entity is present.
 	 * 
 	 * @param sourceEntityId id of the source Entity
 	 * @return The list of IInterModelAssociation
 	 */
 	public List<IInterModelAssociation> getInterModelAssociations(Long sourceEntityId)
 	{
 		List<IInterModelAssociation> list = leftEntityVsInterModelAssociation.get(sourceEntityId);
 		if (list == null)
 		{
 			list = new ArrayList<IInterModelAssociation>(0);
 		}
 		return list;
 	}
 
 	/**
 	 * @param sql SELECT SQL to execute
 	 * @param connection Database connection to use.
 	 * @param params Parameters for SQL.
 	 * @return 2-D array of results with rows as records and columns as COLUMNS in select.
 	 */
 	// this method is package scoped for testing purpose
 	String[][] executeQuery(String sql, Connection connection, Object... params)
 	{
 		return SQLQueryUtil.executeQuery(sql, connection, params);
 	}
 
 	/**
 	 * Splits string representation into list of IDs, gets entity for each ID and returns ste of entities.
 	 * 
 	 * @param stringRepresentationOfEntitySet The string representation of entity set generated by
 	 *            {@link PathFinder#getStringRepresentation(Set)}
 	 * @return Set of entities whose ids are present in given string representation of entity set.
 	 */
 	Set<EntityInterface> getEntitySet(String stringRepresentationOfEntitySet)
 	{
 		EntityCache cache = (EntityCache) EntityCache.getInstance();
 		HashSet<EntityInterface> entitySet = new HashSet<EntityInterface>();
 		String[] ids = stringRepresentationOfEntitySet.split(Constants.CONNECTOR);
 		for (String str : ids)
 		{
 			Long entityId = Long.parseLong(str);
 			entitySet.add(cache.getEntityById(entityId));
 		}
 		return entitySet;
 	}
 
 	/**
 	 * Caches all the curated paths present in the system.
 	 * 
 	 * @param con Database connection to use
 	 */
 	private Map<String, Set<ICuratedPath>> cacheCuratedPaths()
 	{
 		List<ICuratedPath> list = new CuratedPathOperations().getAllCuratedPath();
 		Map<String, Set<ICuratedPath>> entitySetVsCuratedPath = new HashMap<String, Set<ICuratedPath>>(
 				list.size());
 		for (ICuratedPath curatedPath : list)
 		{
 			postProcessCuratedPath(curatedPath);
 			String entitySetStr = CuratedPath.getStringRepresentation(curatedPath.getEntitySet());
 			Set<ICuratedPath> paths = entitySetVsCuratedPath.get(entitySetStr);
 			if (paths == null)
 			{
 				paths = new HashSet<ICuratedPath>();
 				entitySetVsCuratedPath.put(entitySetStr, paths);
 			}
 			paths.add(curatedPath);
 		}
 		return entitySetVsCuratedPath;
 	}
 
 	/**
 	 * @param con Database connection to be used
 	 * @return Map with KEY as caB2B-Association ID v/s IAssociation
 	 */
 	private Map<Long, IAssociation> cacheAssociationTypes(Connection con)
 	{
 		String[][] intraModelRecords = executeQuery(
 				"select ASSOCIATION_ID,DE_ASSOCIATION_ID from INTRA_MODEL_ASSOCIATION", con);
 		String[][] interModelRecords = executeQuery(
 				"select LEFT_ENTITY_ID,LEFT_ATTRIBUTE_ID,RIGHT_ENTITY_ID,RIGHT_ATTRIBUTE_ID,ASSOCIATION_ID from INTER_MODEL_ASSOCIATION",
 				con);
 		Map<Long, IAssociation> idVsIassociation = new HashMap<Long, IAssociation>(
 				intraModelRecords.length + interModelRecords.length);
 		deIdVsAssociationId = new HashMap<Long, Long>(intraModelRecords.length);
 		for (String[] intraModelRecord : intraModelRecords)
 		{
 			Long id = Long.parseLong(intraModelRecord[0]);
 			Long deAssociationId = Long.parseLong(intraModelRecord[1]);
 			AssociationInterface association = getCache().getAssociationById(deAssociationId);
 			idVsIassociation.put(id, new IntraModelAssociation(association));
 			deIdVsAssociationId.put(deAssociationId, id);
 		}
 		leftEntityVsInterModelAssociation = new HashMap<Long, List<IInterModelAssociation>>(
 				interModelRecords.length);
 		for (String[] interModelRecord : interModelRecords)
 		{
 			Long sourceEntityId = Long.parseLong(interModelRecord[0]);
 			Long sourceAttributeId = Long.parseLong(interModelRecord[1]);
 			Long targetEntityId = Long.parseLong(interModelRecord[2]);
 			Long targetAttributeId = Long.parseLong(interModelRecord[3]);
 
 			Long id = Long.parseLong(interModelRecord[4]);
 
 			AttributeInterface sourceAttribute = getAttribute(sourceEntityId, sourceAttributeId);
 			AttributeInterface targetAttribute = getAttribute(targetEntityId, targetAttributeId);
 			InterModelAssociation association = new InterModelAssociation(sourceAttribute,
 					targetAttribute);
 			idVsIassociation.put(id, association);
 
 			List<IInterModelAssociation> list = leftEntityVsInterModelAssociation
 					.get(sourceEntityId);
 			if (list == null)
 			{
 				list = new ArrayList<IInterModelAssociation>();
 				leftEntityVsInterModelAssociation.put(sourceEntityId, list);
 			}
 			list.add(association);
 		}
 
 		return idVsIassociation;
 	}
 
 	private EntityCache getCache()
 	{
 		return (EntityCache) EntityCache.getInstance();
 	}
 
 	/**
 	 * Returns path for associations
 	 * 
 	 * @param associations
 	 * @return
 	 */
 	public IPath getPathForAssociations(List<IIntraModelAssociation> associations)
 	{
 		StringBuffer buff = new StringBuffer();
 
 		if (associations.size() < 1)
 		{
 			throw new RuntimeException("Association list is empty");
 		}
 		buff.append(getId(associations.get(0)));
 		for (int i = 1; i < associations.size(); i++)
 		{
 			buff.append(Constants.CONNECTOR);
 			buff.append(getId(associations.get(i)));
 		}
 		String intermediatePath = buff.toString();
 		PathRecord pathRecord = intermediatePathVsPathRecord.get(intermediatePath);
 		if (pathRecord == null)
 		{
 			logger.error("No Path found for associations : " + intermediatePath);
 			throw new RuntimeException("No Path found for associations : " + intermediatePath);
 		}
 		return getPath(pathRecord);
 	}
 
 	private Long getId(IIntraModelAssociation association)
 	{
 		// IntraModelAssociation association = (IntraModelAssociation) intraModelAssociation;
 		// This class knows the underline class so casting it.
 		Long deAssociationId = association.getDynamicExtensionsAssociation().getId();
 		Long id = deIdVsAssociationId.get(deAssociationId);
 		if (id == null)
 		{
 			logger.error("No IAssociation found for DE association : " + deAssociationId);
 			throw new RuntimeException("No IAssociation found for DE association : "
 					+ deAssociationId);
 		}
 		return id;
 	}
 
 	void addInterModelConnection(InterModelConnection imc)
 	{
 		interModelConnections.add(imc);
 	}
 
 	/**
 	 * Checks whether inter-model connection exists or not
 	 * @param imc InterModelConnection
 	 * @return true if inter-model connection exists otherwise false
 	 */
 	public boolean isInterModelConnectionExist(InterModelConnection imc)
 	{
 		return interModelConnections.contains(imc);
 	}
 
 	/**
 	 * Checks whether inter-model connection exists or not
 	 * @param attr1
 	 * @param attr2
 	 * @return true if inter-model connection exists otherwise false
 	 */
 	public boolean isInterModelConnectionExist(AttributeInterface attr1, AttributeInterface attr2)
 	{
 		return isInterModelConnectionExist(new InterModelConnection(attr1, attr2));
 	}
 
 	/**
 	 * This method adds a new curated path to the cache. 
 	 * This method is to be used immediately after saving a Curated path.
 	 * 
 	 * @param curatedPath
 	 * @return
 	 */
 	public boolean addCuratedPath(CuratedPath curatedPath)
 	{
 		boolean added = false;
 		String entitySetString = CuratedPath.getStringRepresentation(curatedPath.getEntitySet());
 		if (entitySetString != null && !"".equals(entitySetString))
 		{
 			Set<ICuratedPath> paths = entitySetVsCuratedPath.get(entitySetString);
 			if (paths == null)
 			{
 				paths = new HashSet<ICuratedPath>();
 				entitySetVsCuratedPath.put(entitySetString, paths);
 			}
 			paths.add(curatedPath);
 
 			added = true;
 		}
 		return added;
 	}
 
 	/**
 	 * This method process the given curated path object before saving it.
 	 * 
 	 * @param curatedPath
 	 */
 	private void postProcessCuratedPath(ICuratedPath cPath)
 	{
 		CuratedPath curatedPath = (CuratedPath) cPath;
 		// TODO this casting will be removed once we have hibernate layer for path
 		Set<IPath> paths = new HashSet<IPath>();
 		// TODO need to take care of this similar to Curated path, Path should also be populated
 		for (IPath path : curatedPath.getPaths())
 		{
 			Path p = (Path) path;
 			// TODO this casting will be removed once we have hibernate layer for path
 			PathRecord pathRec = new PathRecord(p.getPathId(), p.getSourceEntityId(), p
 					.getIntermediatePaths(), p.getTargetEntityId());
 			paths.add(getPath(pathRec));
 		}
 		curatedPath.setPaths(paths);
 	}
 
 	// ---------------------------------
 	// these methods are package scoped for testing purpose
 	PathFinder()
 	{
 		// TODO Auto-generated constructor stub
 	}
 
 	void setInstance(PathFinder pathFinder)
 	{
 		PathFinder.pathFinder = pathFinder;
 	}
 
 	void setEntitySetVsCuratedPath(Map<String, Set<ICuratedPath>> entitySetVsCuratedPath)
 	{
 		this.entitySetVsCuratedPath = entitySetVsCuratedPath;
 	}
 
 	void setInterModelConnections(Set<InterModelConnection> set)
 	{
 		interModelConnections = set;
 	}
 }
