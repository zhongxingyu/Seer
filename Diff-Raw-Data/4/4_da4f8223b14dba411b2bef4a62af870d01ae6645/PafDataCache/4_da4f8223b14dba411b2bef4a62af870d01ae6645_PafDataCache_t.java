 /*
  *	File: @(#)PafUowCache.java 	Package: com.pace.base.mdb 	Project: Paf Base Libraries
  *	Created: Aug 27, 2005  			By: Alan Farkas
  *	Version: x.xx
  *
  * 	Copyright (c) 2005-2006 Palladium Group, Inc. All rights reserved.
  *
  *	This software is the confidential and proprietary information of Palladium Group, Inc.
  *	("Confidential Information"). You shall not disclose such Confidential Information and 
  * 	should use it only in accordance with the terms of the license agreement you entered into
  *	with Palladium Group, Inc.
  *
  *
  *
 	Date			Author			Version			Changes
 	xx/xx/xx		xxxxxxxx		x.xx			..............
  * 
  */
 package com.pace.base.mdb;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.log4j.Logger;
 
 import com.pace.base.PafBaseConstants;
 import com.pace.base.PafErrSeverity;
 import com.pace.base.PafException;
 import com.pace.base.app.MeasureDef;
 import com.pace.base.app.MeasureType;
 import com.pace.base.app.PafApplicationDef;
 import com.pace.base.app.PafDimSpec;
 import com.pace.base.app.UnitOfWork;
 import com.pace.base.app.VersionDef;
 import com.pace.base.app.VersionFormula;
 import com.pace.base.app.VersionType;
 import com.pace.base.data.IPafDataCache;
 import com.pace.base.data.Intersection;
 import com.pace.base.data.MemberTreeSet;
 import com.pace.base.data.PafDataSlice;
 import com.pace.base.state.PafClientState;
 import com.pace.base.utility.LogUtil;
 import com.pace.base.utility.Odometer;
 import com.pace.base.utility.StringUtils;
 import com.pace.base.view.PafMVS;
 
 /**
  * Container for a subset of multidimensional data cells corresponding
  * to a "unit of work"
  *
  * @version	x.xx
  * @author Alan Farkas
  *
  */
 public class PafDataCache implements IPafDataCache {
 
 	private Map<Intersection, Integer> dataBlockIndexMap = null; // Maps data block key to an item in the data block pool
 	private List<double[][]> dataBlockPool = null;				// [Measure][Time]
 	private LinkedList<Integer> deletedBlockIndexes = null;		// Surrogate keys of deleted blocks (Linked list for performance reasons)
 	private Map<String, Set<Intersection>> dataBlocksByVersion = null;	// Data block keys organized by version
 	private Map<Intersection, Set<Intersection>> aliasKeyLookup = null; // Identifies any alias keys for a given data block
 //	private Map<Intersection, Intersection> baseKeyLookup = null; 		// Identifies the primary key for any alias key
 	private int axisCount = 0;						// Total number of defined dimensions (size of allDimensions)
 	private int blockSize = 0;						// Number of cells in a data block
 	private int dataBlockCount = 0;					// Number of existing data blocks
 	private int[] axisSizes = null;					// # of members in each axis
 	private int[] coreKeyAxes = null; 				// The core dimension axes that comprise that data block key
 	private int[][] keyIndexesByIsSize = null;		// Optimization tool - an array that will be used to isolate
 													//		the intersection coordinates that comprise the data 
 													//		block key for each potential intersection size
 	private String[] coreKeyDims = null;			// The core dimensions that comprise that data block key
 													//		(all dimensions except Measures and Time)
 	private String[] attributeDims = null;			// Valid attribute dimensions
 	private String[] baseDimensions = null; 		// Valid base dimensions
 	private List<String> allDimensions = null; 		// Valid intersection dimensions
 	private Set<String> coreDimensions = null;		// The required cell intersection dimensions (base dimensions)
 	private Set<String> nonCoreDims = null;			// The optional cell intersection dimensions (attribute dimensions)
 	private String[][] memberCatalog = null;		// Defined members by dimension axis
 	private Map<String, Integer> axisIndexMap = null;			// Resolves dimension name to an axis
 	private Map<String, Integer>[] memberIndexMap = null;		// Resolves member name to an index (currently only
 																// 		used to lookup Measures and Time dimension members)
 	private String[] planVersions = null; 			// Plan version 
 	private Set<String> lockedPeriods = null;		// Locked planning periods
 	private PafDataCacheCells changedCells = new PafDataCacheCells();		// Optionally tracks changed cells
 	private PafApplicationDef appDef = null;
 	private PafMVS pafMVS = null;
 	private MemberTreeSet dimTrees = null;
 	private boolean isDirty = true;					// Indicates that the data cache has been updated
 	private final static int NON_KEY_DIM_COUNT = 2;	// Measure and Time Dimension
 
 	// Lazy-loaded collection of component base member names, indexed by base member intersection
 	private Map<Intersection, List<String>> componentBaseMemberMap = new HashMap<Intersection, List<String>>();
 
 	// Loggers
 	private static Logger logger = Logger.getLogger(PafDataCache.class);
 	private static Logger performanceLogger = Logger.getLogger(PafBaseConstants.PERFORMANCE_LOGGER_DC);
 	
 	
 
 	/**
 	 * @param clientState Client State
 	 * @param lockedPeriods Set of locked periods
 	 */
 	public PafDataCache(PafClientState clientState, Set<String> lockedPeriods) {
 		this.lockedPeriods = lockedPeriods;
 		initDataCache(clientState);		
 	}
 
 			
 	/**
 	 * Convenience constructor for testing purposes
 	 * 
 	 * @param clientState Client State
 	 */
 	public PafDataCache(PafClientState clientState) {
 		this(clientState, new HashSet<String>());
 	}
 	
 	
 	/**
 	 *	Initialize data cache
 	 *
 	 * @param clientState Client state
 	 *
 	 */
 	private void initDataCache(PafClientState clientState) {
 
 		logger.debug("Creating instance of PafDataCache");
 		long startTime = System.currentTimeMillis();
 		
 		// Get needed client state properties
 		appDef = clientState.getApp();
 		String versionDim = getVersionDim();
 		planVersions = new String[]{clientState.getPlanningVersion().getName()};
 		UnitOfWork expandedUowSpec = clientState.getUnitOfWork();
 		dimTrees = clientState.getUowTrees();
 
 		// Set dimension properties
 		String[] uowDims = expandedUowSpec.getDimensions();
 		attributeDims = dimTrees.getAttributeDimensions().toArray(new String[0]);
 		baseDimensions = uowDims;
 		coreDimensions = new HashSet<String>(Arrays.asList(uowDims));
 		nonCoreDims = new HashSet<String>(Arrays.asList(attributeDims));
 		allDimensions = new ArrayList<String>(Arrays.asList(baseDimensions));
 		allDimensions.addAll(Arrays.asList(attributeDims));
 		axisCount = allDimensions.size();
 	
 		// Add all dimensions members to member catalog. For proper member aggregation,
 		// the members will be added in POST ORDER so that all children are calculated
 		// before their parents.
 		axisSizes = new int[axisCount];
 		memberCatalog = new String[(axisCount)][];
 		for (int axis = 0; axis < axisCount; axis++) {
 			String dim = allDimensions.get(axis);
 			PafDimTree dimTree = dimTrees.getTree(dim);
 			String[]members;
 			if (!dim.equals(versionDim)) {
 				// Pull all dimensions, except Version, from dim trees
 				members = dimTree.getMemberNames(TreeTraversalOrder.POST_ORDER).toArray(new String[0]);
 			} else {
 				// Version dim is flat, so just pull members from uow definition
 				members = expandedUowSpec.getDimMembers(versionDim);
 			}
 			memberCatalog[axis] = members;
 			axisSizes[axis] = members.length;
 		}
 
 		// Create axisIndexMap
 		axisIndexMap = createAxisIndexMap();
 
 		// Create memberIndexMap
 		memberIndexMap = createMemberIndexMap();
 
 		// Create keyIndexes Array
 		createKeyIndexArrays();
 		
 		// Initialize data block index and data block pool. To minimize the
 		// the time it takes to initially populate the data cache, the index
 		// and pool are pre-allocated to hold all initially defined blocks.
 		blockSize = getMeasureSize() * getTimeSize();
 		int initialBlockCount = getMaxCoreDataBlockCount();
 		dataBlockIndexMap = new HashMap<Intersection, Integer>(initialBlockCount);
 		dataBlockPool = new ArrayList<double[][]>(initialBlockCount);
 		
 		// Initialize various data block housekeeping objects
 		dataBlocksByVersion = new HashMap<String, Set<Intersection>>(getVersionSize());
 		aliasKeyLookup = new HashMap<Intersection, Set<Intersection>>();
 		deletedBlockIndexes = new LinkedList<Integer>();
 
 		// Initialize data block list
 		long maxCellCount = initialBlockCount * getBlockSize();
 		String stepDesc = "Data Cache intialized with a potential base intersection count of : [" 
 			+ StringUtils.commaFormat(maxCellCount) + "] ";
 		logger.info(stepDesc);
 		String logMsg = LogUtil.timedStep(stepDesc, startTime);
 		performanceLogger.info(logMsg);
 
 	}
 
 
 	/**
 	 *	Return the number of columns in the data cache
 	 *
 	 * @return Returns the number of columns in the data cache.
 	 */
 	public int getColumnCount() {
 		return getAxisSizes()[0];
 	}
 
 
 	/**
 	 * @return the dimTrees
 	 */
 	public MemberTreeSet getDimTrees() {
 		return dimTrees;
 	}
 
 
 	/**
 	 *	Return the PafMVS
 	 *
 	 * @return Returns the PafMVS
 	 */
 	public PafMVS getPafMVS() {
 		return pafMVS;
 	}
 
 	/**
 	 * @param pafMVS the pafMVS to set
 	 */
 	public void setPafMVS(PafMVS pafMVS) {
 		this.pafMVS = pafMVS;
 	}
 
 
 
 	/**
 	 *	Valid that array is not empty or null
 	 *
 	 * @param array Array to validate
 	 * @param arrayName Array name
 	 * @throws IllegalArgumentException 
 	 */
 	private void validateArrayNotEmpty(Object[] array, String arrayName) throws IllegalArgumentException {
 
 		// Validate that the array contains data and is the proper size
 		if (array == null || array.length == 0) {
 			// Empty or null array
 			String errMsg = "Parms error - empty or null [" + arrayName + "] array";
 			logger.error(errMsg);
 			IllegalArgumentException iae = new IllegalArgumentException(errMsg);    
 			throw iae; 
 		}
 	}
 
 
 	/**
 	 *	Create PafUowCache axis index map
 	 *
 	 * @return HashMap used for resolving dimension names to a specific axis index value.
 	 */
 	private Map<String, Integer> createAxisIndexMap() {
 
 		Map <String, Integer> axisIndexMap = null;
 
 		// Fill the axisIndexMap, which is used to resolve dimension
 		// names to a specific axis index.
 		logger.debug("Creating axisIndexMap ...");
 		axisIndexMap = new HashMap<String, Integer>();
 		for (int i = 0; i < axisCount; i++) {
 			axisIndexMap.put(allDimensions.get(i), i);
 		}
 		return axisIndexMap;
 	}
 
 
 	/**
 	 *	Create indexed intersection elements array
 	 *
 	 * @return An array containing the indexed intersection elements by intersection section size
 	 */
 	private void createKeyIndexArrays() {
 
 		int measureAxis = getMeasureAxis(), timeAxis = getTimeAxis();
 
 		//TODO FIX C O M M E N T S !!!
 		// Fill the indexedIsElements array, which contains the list of all
 		// UOW dimensions axis that comprise the data block index. This would be 
 		// all dimensions except Measure and Time, which are not included as 
 		// they each are represented within the data block itself.
 		logger.debug("Creating indexedAxis array...");
 		int minIsSize = coreDimensions.size();
 		int maxIsSize = allDimensions.size();
 		keyIndexesByIsSize = new int[maxIsSize + 1][];
 
 		coreKeyAxes = new int[minIsSize - NON_KEY_DIM_COUNT];
 		coreKeyDims = new String[coreKeyAxes.length];
 		int i = 0;
 		for (int axisIndex = 0; axisIndex < minIsSize; axisIndex++) {
 			if (axisIndex != measureAxis && axisIndex != timeAxis) {
 				coreKeyAxes[i] = axisIndex;
 				coreKeyDims[i] = getDimension(axisIndex); 
 				i++;
 			}
 		}
 
 		// Create an array that will be used to isolate the intersection coordinates
 		// that comprise the data block key for each intersection size
 		//
 		// 	Example:  	
 		//	Intersection Size		indexedAxes
 		//			  		7		[1][3][4][5][6]
 		//			  		8		[1][3][4][5][6][7]
 		//			  		9		[1][3][4][5][6][7][8]
 		//
 		keyIndexesByIsSize[minIsSize] = coreKeyAxes;
 		for (int element = minIsSize + 1; element <= maxIsSize; element++) {
 			int[] prevIndexedAxes = keyIndexesByIsSize[element - 1];
 			int[] indexedAxes = new int[element - NON_KEY_DIM_COUNT];
 			System.arraycopy(prevIndexedAxes, 0, indexedAxes, 0, prevIndexedAxes.length);
 			indexedAxes[indexedAxes.length - 1] = element - 1;
 			keyIndexesByIsSize[element] = indexedAxes;
 		}
 	}
 
 	/**
 	 *	Create member index map
 	 *
 	 */
 	@SuppressWarnings("unchecked")
 	private Map<String, Integer>[] createMemberIndexMap() {
 
 		Map<String, Integer>[] memberIndexMap = null;
 
 		// Fill the memberIndexMap array, which is used to resolve member
 		// names to a specific index.
 		logger.debug("Filling memberIndexMap array...");
 		memberIndexMap = new Map[axisCount];
 
 		for (int i = 0; i < axisCount; i++) {
 			HashMap <String, Integer> memberIndex = new HashMap <String, Integer> (memberCatalog[i].length);
 			for (int j = 0; j < axisSizes[i]; j++) {
 				memberIndex.put(memberCatalog[i][j], j);
 			}
 			memberIndexMap[i] = memberIndex;
 			logger.debug("Member index for Axis [" + i + "] populated");
 		}
 		return memberIndexMap;
 	}
 
 
 
 	/**
 	 *	Returns the existing number of data blocks in the data cache
 	 *
 	 * @return The existing number of data blocks in the data cache
 	 */
 	public int getDataBlockCount() {
 
 		int dataBlockCount = dataBlockPool.size();
 		return dataBlockCount;
 	}
 
 	/**
 	 *	Returns the maximum number of addressable data blocks across the core
 	 *	dimensions
 	 *
 	 * @return The maximum number of addressable data blocks across the core dimensions
 	 */
 	public int getMaxCoreDataBlockCount() {
 
 		int memberCombinations = 1;
 
 		// Multiply the number of members in each core dimension by
 		// each other.
 		for (int axisIndex = 0; axisIndex < coreKeyAxes.length; axisIndex++) {
 			memberCombinations = memberCombinations * getAxisSize(axisIndex);
 		}
 
 		return memberCombinations;
 	}
 
 
 	/**
 	 * @return the blockSize
 	 */
 	public int getBlockSize() {
 		return blockSize;
 	}
 
 
 	/**
 	 *	Return a list of versions that are components for any of the
 	 *  specified derived versions
 	 *  
 	 * @param derivedVersions List of derived versions to inspect
 	 * @return List of component versions
 	 */
 	public List<String> getComponentVersions(List<String> derivedVersions) {
 
 		Set<String> componentVersions = new HashSet<String>();
 
 
 		// Cycle through each derived version and look for component members
 		for (String derivedVersion : derivedVersions) {
 			
 			VersionDef versionDef = getVersionDef(derivedVersion);
 			
 				// Get base version
 				VersionFormula versionFormula = versionDef.getVersionFormula();
 				String baseVersion = versionFormula.getBaseVersion();
 				componentVersions.add(baseVersion);
 				
 				// Determine the comparison version
 				String compareVersion = null;
 				if (versionDef.getType() == VersionType.Variance) {
 					// Variance version - use compare version property
 					compareVersion = versionFormula.getCompareVersion(); 
 				} else if (versionDef.getType() == VersionType.ContribPct) {
 					// Contribution percent - search for possible compare version
 					for (PafDimSpec compareMemberSpec:versionFormula.getCompareIsSpec()) {
 						String dim = compareMemberSpec.getDimension();
 						if (dim.equalsIgnoreCase(getVersionDim())) {
 							compareVersion = compareMemberSpec.getExpressionList()[0];
 						}
 					}
 				}
				if (compareVersion != null) {
					componentVersions.add(compareVersion);
				}
 			}
 
 	
 		return new ArrayList<String>(componentVersions);
 	}
 
 	/**
 	 *	Return a list of versions for those data cache versions whose formulas 
 	 *  are dependent on any of the base versions passed to this method 
 	 *  
 	 * @param versions List of versions whose dependencies will be checked
 	 * @return List of dependent versions
 	 */
 	public List<String> getDependentVersions(List<String> versions) {
 
 		List<String> dependentVersions = new ArrayList<String>();
 
 		// Get list of versionDef objects for all derived versions
 		List<VersionDef>derivedVersionDefs = getDerivedVersionDefs();
 		
 		// Cycle through each version and check for dependent formulas
 		for (String version : versions) {
 			for (VersionDef versionDef : derivedVersionDefs) {
 
 				// Get formula components
 				String derivedVersion = versionDef.getName();
 				VersionFormula versionFormula = versionDef.getVersionFormula();
 				String baseVersion = versionFormula.getBaseVersion();
 				String compareVersion = null;
 				
 				// Determine the comparison version
 				if (versionDef.getType() == VersionType.Variance) {
 					// Variance version - use compare version property
 					compareVersion = versionFormula.getCompareVersion(); 
 				} else if (versionDef.getType() == VersionType.ContribPct) {
 					// Contribution percent - search for possible compare version
 					for (PafDimSpec compareMemberSpec:versionFormula.getCompareIsSpec()) {
 						String dim = compareMemberSpec.getDimension();
 						if (dim.equalsIgnoreCase(getVersionDim())) {
 							compareVersion = compareMemberSpec.getExpressionList()[0];
 						}
 					}
 				}
 				
 				// Add dependent version if it is a dependent of one of the selected versions 
 				if (version.equalsIgnoreCase(baseVersion) || 
 						(compareVersion != null && version.equalsIgnoreCase(compareVersion))) {
 					dependentVersions.add(derivedVersion);
 				}
 			}
 
 		}
 		return dependentVersions;
 	}
 
 
 	/**
 	 *	Determine if the Version and Year combination are locked
 	 *
 	 * @param version Selected member of version dimension
 	 * @param year Selected member of year dimension
 	 *
 	 * @return true if Version and Year combination contains locked periods
 	 */
 	public boolean hasLockedPeriods(String version, String year) {	
 
 		boolean hasLockedPeriods = false;
 		VersionType versionType = getVersionType(version);
 
 		// A Version and Year combination contains locked periods if the following conditions are met:
 		// 	1. The year matches the "current year"
 		//	2. The Version is Forward Plannable 
 		//			OR The Version is a Variance Version whose Base Version is Forward Plannable
 		if (year.equalsIgnoreCase(getCurrentYear())) {
 			if (versionType == VersionType.ForwardPlannable) {
 				hasLockedPeriods = true;
 			} else if (PafBaseConstants.DERIVED_VERSION_TYPE_LIST.contains(versionType)) {
 				String baseVersion = getVersionDef(version).getVersionFormula().getBaseVersion();
 				hasLockedPeriods = hasLockedPeriods(baseVersion, year);
 			}
 		}
 		return hasLockedPeriods;
 	}
 
 
 	/**
 	 *	Get the entire list of Forward Plannable periods in this data cache
 	 *
 	 * @return List of Forward Plannable periods
 	 */
 	public List<String> getForwardPlannablePeriods() {
 		return getForwardPlannablePeriods(getDimMembers(getTimeDim()), getLockedPeriods()) ;
 	}
 
 
 	/**
 	 *	Get the list of Forward Plannable members from the supplied list of time members
 	 *
 	 * @param allPeriods All valid members of the time dimension
 	 * @param lockedPeriods List of current locked periods
 	 * 
 	 * @return List of Forward Plannable periods
 	 */
 	public List<String> getForwardPlannablePeriods(String[] allPeriods, Set<String> lockedPeriods) {
 
 		List<String> fpPeriods = new ArrayList<String>();
 
 		// Return any periods from the Time dimension that are not contained in lockedPeriods
 		for (String period:allPeriods) {
 			if (!lockedPeriods.contains(period)) {
 				fpPeriods.add(period);
 			}
 		}
 		return fpPeriods;
 	}
 
 
 	/**
 	 *	Get the list open periods in this data cache based on the selected version and year
 	 *	 
 	 * @param version Selected member of version dimension
 	 * @param year Selected member of year dimension
 	 * 
 	 * @return List of open periods
 	 */
 	public List<String> getOpenPeriods(String version, String year) {
 		return getOpenPeriods(version, year, getDimMembers(getTimeDim()), getLockedPeriods()) ;
 	}
 
 
 	/**
 	 *	Get the list open periods based on the version and year
 	 *
 	 * @param version Selected member of version dimension
 	 * @param year Selected member of year dimension
 	 * @param timeMembers A valid members of the time dimension
 	 * @param lockedPeriods List of current locked periods
 	 * 
 	 * @return List of open periods
 	 */
 	public List<String> getOpenPeriods(String version, String year, String[] timeMembers, Set<String> lockedPeriods) {
 
 		List<String> openPeriods = null;
 
 		// Does the selected Version Type and Year combination have locked periods?
 		if (hasLockedPeriods(version, year)) {
 			// Yes - Just return list of Forward Plannable members
 			openPeriods = getForwardPlannablePeriods(timeMembers, lockedPeriods);
 		} else {
 			// No locked periods - Return entire list of time members
 			openPeriods = Arrays.asList(timeMembers);
 		}
 		return openPeriods;
 	}
 
 
 	/**
 	 *	Returns the planning versions
 	 *
 	 * @return An array of planning versions
 	 */
 	public String[] getPlanVersions() {
 		return planVersions;
 	}
 
 	/**
 	 * Return an array containing the dimensions that are represented in the data cache
 	 * 
 	 * @return Returns the dimension array.
 	 */
 	public String[] getAllDimensions() {
 		return allDimensions.toArray(new String[0]);
 	}
 
 	/**
 	 * @return Returns the appDef.
 	 */
 	public PafApplicationDef getAppDef() {
 		return appDef;
 	}
 
 	/**
 	 *	Return the number of axes contained in the data cache
 	 *
 	 * @return Returns the number of axes contained in the data cache.
 	 */
 	public int getAxisCount() {
 		return axisCount;
 	}
 
 	/**
 	 *	Return the axis number that corresponds to the 
 	 *  the supplied dimension name
 	 *
 	 * @param dimName Dimension name
 	 * @return Returns the axis number of the supplied dimension name.
 	 */
 	public int getAxisIndex(String dimName) {
 
 		int index = 0;	
 
 		if (axisIndexMap.containsKey(dimName)) {
 			index = axisIndexMap.get(dimName);
 		} else {
 			// Dimension name not found
 			String errMsg = "Unable to get index for dimension ["
 				+ dimName + "] - this dimension does not exist in the paf data cache";
 			logger.error(errMsg);
 			IllegalArgumentException iae = new IllegalArgumentException(errMsg);    
 			throw iae;          
 		}
 
 		return index;
 	}
 
 
 	/**
 	 *	Return an array containing the members in the specified axis
 	 *
 	 * @param axis Index axis
 	 * @return Returns an array containing the members in the specified axis.
 	 */
 	public String[] getAxisMembers(int axis) {
 		return memberCatalog[axis];
 	}
 
 	/**
 	 *	Return the number of members in specified axis.
 	 *
 	 * @param axis Axis index
 	 * @return Returns the number of members in the specified axis.
 	 */
 	public int getAxisSize(int axis) {
 
 		// Validate axis
 		if (axis < 0 || axis > axisCount - 1) {
 			// Invalid axis value
 			String errMsg = "Unable to get size of axis ["
 				+ axis + "] - axis value is out of bounds";
 			logger.error(errMsg);
 			IllegalArgumentException iae = new IllegalArgumentException(errMsg);    
 			throw iae;          			
 		}
 
 		return axisSizes[axis];
 	}
 
 	/**
 	 *	Return the array containing the number of members in each axis.
 	 *
 	 * @return Returns the array containing the number of members in each axis.
 	 */
 	public int[] getAxisSizes() {
 		return axisSizes;
 	}
 
 	/**
 	 * @return the attributeDims
 	 */
 	public String[] getAttributeDims() {
 		return attributeDims;
 	}
 
 
 	/**
 	 * @return the baseDimensions
 	 */
 	public String[] getBaseDimensions() {
 		return baseDimensions;
 	}
 
 	/**
 	 * @return the base dimension count
 	 */
 	public int getBaseDimCount() {
 		return baseDimensions.length;
 	}
 
 
 	/**
 	 *	Return set of names of any base dimensions that have been
 	 *  assigned one or more attribute dimensions
 	 *
 	 * @return Set<String>
 	 */
 	public Set<String> getBaseDimNamesWithAttributes() {
 
 		Set<String> baseDimNames = new HashSet<String>();
 
 		// Iterate through all attribute dimensions and append all associated
 		// base dimensions to base dimension name set
 		for (String attrDim : this.attributeDims) {
 			PafAttributeTree attrTree = (PafAttributeTree) dimTrees.getTree(attrDim);
 			String baseDimName = attrTree.getBaseDimName();
 			if (!baseDimNames.contains(baseDimName)) {
 				baseDimNames.add(baseDimName);
 			}			
 		}
 
 		// Return all base dimensions that have been assigned one or more attribute dimensions
 		return baseDimNames;
 	}
 
 	
 	/**
 	 *	Return the data cache cell count
 	 *
 	 * @return Returns the data cache cell count.
 	 */
 	public int getCellCount() {
 		return getDataBlockCount() * blockSize;
 	}
 
 
 	/**
 	 * 	Returns an iterator that will iterate through all valid
 	 * 	intersections comprising the specified dimensions. The 
 	 * 	order of the supplied dimensions will determine the 
 	 *  dimension order of the intersection coordinates generated 
 	 *  by the iterator.
 	 *  
 	 * @param dimensions Dimensions to iterate through. 
 	 * @return Odometer
 	 */
 	public Odometer getCellIterator(String[] dimensions) {
 		return getCellIterator(dimensions, null);
 	}
 	
 	/**
 	 * 	Returns an iterator that will iterate through all valid
 	 * 	intersections comprising the specified dimensions. The 
 	 * 	order of the supplied dimensions will determine the 
 	 *  dimension order of the intersection coordinates generated 
 	 *  by the iterator.
 	 *  
 	 *  An optional member filter can be used to select specific members
 	 *  to iterate through.
 	 * 
 	 * @param dimensions Dimensions to iterate through. 
 	 * @param memberFilter Map containing a list for each filtered dimension
 	 * 
 	 * @return Odometer
 	 */
 	public Odometer getCellIterator(String[] dimensions, Map<String, List<String>> memberFilter) {
 		
 		// The Odometer requires member lists for each iterated dimension. So, a 
 		// member filter will be created if it's not already supplied and any
 		// missing dimensions will be added.
 		if (memberFilter == null) {
 			memberFilter = new HashMap<String, List<String>>();
 		}
 		memberFilter = addMissingDimsToMemberFilter(memberFilter, dimensions);
 		
 		Odometer cellIterator = new Odometer(memberFilter, dimensions);
 		return cellIterator;
 	}
 	
 	/**
 	 * 	Add any missing dimensions to member filter. All dimension members defined 
 	 *  in the data cache will be added for any missing dimensions. 
 	 * 
 	 * @param memberFilter Map containing member lists by dimension
 	 * @param dimensions List of dimensions that the map should contain. represented in filter
 	 */
 	private Map<String, List<String>> addMissingDimsToMemberFilter(Map<String, List<String>> memberFilter, String[] dimensions) {
 		
 		Map<String, List<String>> updatedMemberFilter =  new HashMap<String, List<String>>(memberFilter);
 		for (String dimension : dimensions) {
 			if (!updatedMemberFilter.containsKey(dimension)) {
 				updatedMemberFilter.put(dimension, new ArrayList<String>(Arrays.asList(getDimMembers(dimension))));
 			}
 		} 
 		
 		return updatedMemberFilter;
 	}
 
 	/**
 	 *	Return the cell value for the specified non-attribute intersection coordinates
 	 *
 	 * @param coords Array of dimension members that define a non-attribute intersection
 	 * @return Returns the cell value.
 	 */
 	public double getBaseCellValue(String[] coords) {
 
 		Intersection intersection = new Intersection(getBaseDimensions(), coords);
 		return getCellValue(intersection);
 	}
 
 	/**
 	 *	Return the intersection cell value for the specified intersection coordinates
 	 *
 	 * @param dimensions Array of dimension names that define the cell intersection dimensionality
 	 * @param coords Array of dimension members that define the cell intersection
 	 * 
 	 * @return Returns the cell value.
 	 */
 	public double getCellValue(String[] dimensions, String[] coords) {
 
 		Intersection intersection = new Intersection(dimensions, coords);
 		return getCellValue(intersection);
 	}
 
 	/**
 	 *	Return the cell value for the specified intersection. If the requested intersection
 	 *  is valid, but does not exist, then a zero value is returned.
 	 *
 	 * @param intersection Member intersection object that corresponds to data cache cell
 	 * 
 	 * @return Returns the cell value
 	 */
 	public double getCellValue(Intersection intersection)  {
 
 		// Get the cell address of the specified intersection
 		DataCacheCellAddress cellAddress = generateCellAddress(intersection);
 
 		// Return cell value
 		double[][] dataBlock = getDataBlock(cellAddress.getDataBlockKey()).getDataBlock();
 		if (dataBlock != null) {
 			// Intersection exists - return cell value
 			return dataBlock[cellAddress.getCoordX()][cellAddress.getCoordY()];
 		} else {
 			// Intersection does not exist
 			if (isValidIntersection(intersection)) {
 				// Valid intersection, but does not exist - return 0
 				return 0;
 			} else {
 				// Invalid intersection - throw error
 				String errMsg = "Data Cache error - Unable to get data cache cell value for invalid intersection: "
 					+ StringUtils.arrayToString(intersection.getCoordinates());
 				throw new IllegalArgumentException(errMsg);
 			}
 		}
 
 	}
 
 	
 	/**
 	 *	Set the value for a specific base intersection cell
 	 *
 	 * @param coords Array of dimension members that define a single base intersection
 	 * @param value Value to put into cell
 	 */
 	public void setBaseCellValue(String[] coords, double value) {
 
 		Intersection intersection = new Intersection(getBaseDimensions(), coords);
 		setCellValue(intersection, value);
 	}
 
 	/**
 	 *	Return the intersection cell value for the specified intersection coordinates
 	 *
 	 * @param dimensions Array of dimension names that define the cell intersection dimensionality
 	 * @param coords Array of dimension members that define the cell intersection
 	 * @param value Value to put into cell
 	 * 
 	 * @return Returns the cell value.
 	 */
 	public void setCellValue(String[] dimensions, String[] coords, double value) {
 
 		Intersection intersection = new Intersection(dimensions, coords);
 		setCellValue(intersection, value);
 	}
 
 
 	
 	/**
 	 *	Set the value for a specific data cache cell and optionally track changes
 	 *
 	 * @param intersection Cell intersection
 	 * @param value Value to put into cell
 	 * @param trackChangeOpt Data cache change tracking option
 	 */
 	public void setCellValue(Intersection intersection, double value, DcTrackChangeOpt trackChangeOpt) {
 
         // Update cell value
         if (trackChangeOpt == DcTrackChangeOpt.NONE) {
               setCellValue(intersection, value);
         } else {
               setCellValueAndTrackChanges(intersection, value);
         }     
 
 	}
 		
 	/**
 	 *	Set the value for a specific data cache cell and track any changed cells
 	 *
 	 * @param intersection Cell intersection
 	 * @param value Value to put into cell
 	 */
 	public void setCellValueAndTrackChanges(Intersection intersection, double value) {
 
 		double oldCellValue = getCellValue(intersection);
 		setCellValue(intersection, value);
 		if (Math.abs(value - oldCellValue) > PafBaseConstants.DC_TRACK_CHANGES_THRESHHOLD) {
 			// Clone intersection before adding to collection as the calling method
 			// may reuse this intersection object to point to a different intersection
 			addChangedCell(intersection.clone(), value);
 		}
 	}
 
 	/**
 	 *	Set the value for a specific data cache cell
 	 *
 	 * @param intersection Cell intersection
 	 * @param value Value to put into cell
 	 */
 	public void setCellValue(Intersection intersection, double value) {
 		
 		// Add intersection if it doesn't already exist
 		DataCacheCellAddress cellAddress = addCell(intersection);
 		
 		// Round updated cell value to a predefined number of digits to mask any potential precision errors
 		long longValue = (long) value;
 		double signedMantissa = value - longValue;
 		double roundedMantissa = Math.round(signedMantissa * PafBaseConstants.DC_ROUNDING_CONSTANT)
 								/ PafBaseConstants.DC_ROUNDING_CONSTANT;
 		double roundedValue = longValue + roundedMantissa;
 		
 		// Update cell value
 		double[][] dataBlock = getDataBlock(cellAddress.getDataBlockKey()).getDataBlock();
 		dataBlock[cellAddress.getCoordX()][cellAddress.getCoordY()] = roundedValue;
 
 		// Set dirty flag to indicate that data cache was updated
 		setDirty(true);
 	}
 
 
 	/**
 	 *	Add a new cell intersection to the data cache
 	 *
 	 * @param intersection Cell intersection
 	 * @return DataCacheCellAddress Cell's internal address
 	 */
 	public DataCacheCellAddress addCell(Intersection intersection) {
 		
 		// Validate intersection
 		if (!isValidIntersection(intersection)) {
 			// Invalid intersection - throw error
 			String errMsg = "Data Cache error - Unable to set data cache cell value for invalid intersection ["
 				+ StringUtils.arrayToString(intersection.getCoordinates()) + "]";
 			throw new IllegalArgumentException(errMsg);
 		}
 		
 		// Get the cell address of the specified intersection
 		DataCacheCellAddress cellAddress = generateCellAddress(intersection);
 		
 		// Add data block if it doesn't already exist
 		addDataBlock(cellAddress.getDataBlockKey());
 		
 		// Return cell address
 		return cellAddress;
 	}
 	
 	
 	/**
 	 *	Add a new data block to the data cache.
 	 *
 	 * @param key Data block key
 	 * @return DataBlockResponse Data block response
 	 */
 	private DataBlockResponse addDataBlock(Intersection key) {
 
 		int surrogateKey = 0;
 		double[][] dataBlock = null;
 		DataBlockResponse dataBlockResp = null;
 		
 		
 		// If the data block already exists, jut return its
 		// information
 		dataBlockResp = getDataBlock(key);
 		dataBlock = dataBlockResp.getDataBlock();
 		if (dataBlock != null) {
 			return dataBlockResp;
 		}
 		
 		
 		// Ensure that data block being added is valid
 		if (!isValidDataBlock(key)) {
 			String msg = "An attempt was made to add the invalid data block: "
 				+ key.toString() + " to the data cache.";
 			throw new IllegalArgumentException(msg);
 		}
 		
 		// If alias data block key, add key to index. Also check for
 		// corresponding primary data block. If the primary data
 		// block doesn't already exists, then create it.  
 		if (isAliasDataBlockKey(key)) {
 			Intersection primaryKey = generatePrimaryKey(key);
 			DataBlockResponse aliasDataBlockResp =  getDataBlock(primaryKey);
 			if (aliasDataBlockResp.getDataBlock() == null) {
 				aliasDataBlockResp = addDataBlock(primaryKey);
 				surrogateKey = addDataBlock(primaryKey).getSurrogateKey();
 			}
 			surrogateKey = aliasDataBlockResp.getSurrogateKey();
 			dataBlockIndexMap.put(key, surrogateKey);
 			addAliasKey(key, primaryKey); 
 			return aliasDataBlockResp;
 		}
 		
 		// Add index entry for new data block. Attempt to reuse any
 		// previously deleted blocks.
 		if (deletedBlockIndexes.size() == 0) {
 			// Add index entry for new data block (index is auto-incremented)
 			surrogateKey = dataBlockCount;
 		} else {
 			// Reuse index of a deleted block
 			surrogateKey = deletedBlockIndexes.removeLast();
 		}
 		dataBlockIndexMap.put(key, surrogateKey);
 		dataBlockCount++;
 		
 		// Create new data block and add it to data block pool
 		dataBlock = new double[getMeasureSize()][getTimeSize()];
 		dataBlockPool.add(dataBlock);
 		
 		// Add data block key to lookup collections
 		//TODO only primary block key should be added
 		addDataBlockKey(key);
 		
 		// Return data block response
 		dataBlockResp = new DataBlockResponse();
 		dataBlockResp.setSurrogateKey(surrogateKey);
 		dataBlockResp.setDataBlock(dataBlock);
 		return dataBlockResp;
 	}
 
 
 	/**
 	 * 	Add alias data block key to alias key lookup
 	 * 
 	 * @param aliasKey Alias data block key
 	 * @param primaryKey Corresponding primary data block key
 	 */
 	private void addAliasKey(Intersection aliasKey, Intersection primaryKey) {
 		
 		Set<Intersection> aliasKeys = aliasKeyLookup.get(primaryKey);
 		if (aliasKeys == null) {
 			aliasKeys = new HashSet<Intersection>();
 		}
 		aliasKeys.add(aliasKey);
 		aliasKeyLookup.put(primaryKey, aliasKeys);
 
 	}
 
 
 	/**
 	 * Convert an alias data block key to its corresponding primary 
 	 * data block key
 	 *  
 	 * @param aliasKey Alias data block key
 	 * @return Primary data block key.
 	 */
 	private Intersection generatePrimaryKey(Intersection aliasKey) {
 		
 		// Strip off non-core dimension elements. The non-core
 		// dimension elements will always appear after the core
 		// dimension elements.
 		Intersection primaryKey = aliasKey.createSubIntersection(coreKeyDims.length);
 		return primaryKey;
 	}
 
 
 	/**
 	 * Builds the primary intersection corresponding to the specified alias 
 	 * intersection
 	 * 
 	 * @param aliasIs Alias intersection
 	 * @return Corresponding primary intersection
 	 */
 	public Intersection generatePrimaryIntersection(Intersection aliasIs) {
 		
 		// Convert the alias intersection into its corresponding primary
 		// intersection by striping off the non-core dimension elements. 
 		// The non-core dimension elements will always appear after the 
 		// core dimension elements.
 		Intersection primaryIs = aliasIs.createSubIntersection(coreDimensions.size());
 		return primaryIs;
 	}
 
 	
 	/**
 	 *  Add data block key to lookup collections
 	 *  
 	 * @param key
 	 */
 	private void addDataBlockKey(Intersection key) {
 	
 		// Add data block key to version collection
 		//TODO only primary block key should be added
 		String version = key.getCoordinate(this.getVersionDim());
 		Set<Intersection> versionBlocks = dataBlocksByVersion.get(version);
 		if (versionBlocks != null) {
 			versionBlocks.add(key);
 		} else {
 			versionBlocks = new HashSet<Intersection>();
 			versionBlocks.add(key);
 			dataBlocksByVersion.put(version, versionBlocks);
 		}	
 	}
 
 
 	/**
 	 *	Delete a data block from the data cache.
 	 *
 	 * @param key Data block key	
 	 */
 	private void deleteDataBlock(Intersection key) {
 		
 		// Retrieve the data block being deleted
 		DataBlockResponse dataBlockResp = getDataBlock(key);
 		double[][] dataBlock = dataBlockResp.getDataBlock();
 		
 		if (dataBlock == null) {
 			String errMsg = "Data Cache Error - attempt to delete block deletion error - a data block with a key of [" 
 				+ key + "] does not exist";
 			logger.error(errMsg);
 			throw new IllegalArgumentException(errMsg);
 		}
 		
 
 		// If this is an alias data block key, remove key entry
 		// from data block index and exit.
 		if (isAliasDataBlockKey(key)) {
 			dataBlockIndexMap.remove(key);
 			return;
 		}
 		
 		// Remove data block key from collections
 		deleteDataBlockKey(key);
 		
 		// Delete data block
 		dataBlock = null;
 		dataBlockIndexMap.remove(key);
 		dataBlockCount--;
 
 		// Add index of deleted block to collection so that the 
 		// index entry can be reused for a future data
 		// block addition. 
 		int surrogateKey = dataBlockResp.getSurrogateKey();
 		deletedBlockIndexes.add(surrogateKey);
 			
 	}
 
 
 	/**
 	 *	Remove data block key from lookup collections
 	 *
 	 * @param key Data block key
 	 */
 	private void deleteDataBlockKey(Intersection key) {
 
 		// This method is only called on the primary data
 		// block key.
 		
 		// Remove key from version collection
 		Set<Intersection> versionBlocks = dataBlocksByVersion.get(key.getCoordinate(getVersionDim()));
 		if (versionBlocks != null) {
 			versionBlocks.remove(key);
 		}
 
 		// Remove any associated alias key entries
 		Set<Intersection> aliasKeys = aliasKeyLookup.remove(key);
 		if (aliasKeys != null) {
 			for (Intersection aliasKey : aliasKeys) {
 				dataBlockIndexMap.remove(aliasKey);
 			}
 		}
 		
 	}
 
 
 	/**
 	 * Returns true if the data block key is not the primary
 	 * key
 	 * 
 	 * @param Returns true if the data block key is not the
 	 * @return
 	 */
 	private boolean isAliasDataBlockKey(Intersection dataBlockKey) {
 		return !isPrimaryDataBlockKey(dataBlockKey);
 	}
 
 	/**
 	 * Returns true if the intersection in an alias of a base intersection
 	 * 
 	 * @param intersection Cell intersection
 	 * @return True if the intersection is an alias of a base  intersection 
 	 */
 	public boolean isAliasIntersection(Intersection intersection) {
 		return !isPrimaryIntersection(intersection);
 	}
 
 	/**
 	 * Returns true if the intersection in an attribute intersection
 	 * 
 	 * @param intersection Cell intersection
 	 * @return True if the intersection is an attribute intersection 
 	 */
 	public boolean isAttributeIntersection(Intersection intersection) {
 		
 		boolean isAttributeIs = false;
 		
 		// Simple check - an intersection is an attribute intersection,
 		// if it contains any non-core dimensions
 		if (intersection.getSize() > this.coreDimensions.size()) {
 			isAttributeIs = true;
 		}
 		return isAttributeIs;
 	}
 
 
 	/**
 	 * Returns true if the data block key maps directly
 	 * to a unique data block in the data cache. 
 	 * 
 	 * A key is considered to be a primary key if it meets one
 	 * of the following two conditions:
 	 * 
 	 * 1) Comprised of only core key dimensions
 	 * 2) Does not map directly to a base member intersection -
 	 *    Specifically, the key contains at least one non-level 0
 	 *    attribute member or an attribute member with an 
 	 *    associated non-level 0 base member.
 	 * 
 	 * @param key Data block key
 	 * @return boolean
 	 */
 	private boolean isPrimaryDataBlockKey(Intersection key) {
 		
 		final int coreKeyDimCount = coreKeyDims.length;
 		final int dataBlockKeySize = key.getSize();
 		boolean isPrimaryKey = true;
 
 		// Check if the key contains any attribute dimension
 		// coordinates.
 		if (dataBlockKeySize != coreKeyDimCount) {
 
 			// The key does contain one or more attribute dimension coordinates, 
 			// which need to be analyzed. These attribute dimension coordinates, 
 			// when they exist, always appear after all the core key dimension 
 			// coordinates.
 			isPrimaryKey = false;
 			validation:
 				for (int index = coreKeyDimCount; index < dataBlockKeySize; index++) {
 
 					String attrDim = key.getDimensions()[index];
 					String attrMember = key.getCoordinate(attrDim);
 
 					// Check level of attribute coordinate member - if not at level 0,
 					// then this is a primary key
 					PafAttributeTree attrDimTree = (PafAttributeTree) dimTrees.getTree(attrDim);
 					int attrMbrLevel = attrDimTree.getMember(attrMember).getMemberProps().getLevelNumber();
 					if (attrMbrLevel > 0) {
 						isPrimaryKey = true;
 						break validation;
 					}
 
 					// Check level of associated base member - if above attribute mapping level,
 					// then this is a primary key
 					String assocBaseDim = attrDimTree.getBaseDimName();
 					PafBaseTree baseDimTree = (PafBaseTree) dimTrees.getTree(assocBaseDim);
 					String baseMember = key.getCoordinate(assocBaseDim);
 					int baseMbrLevel = baseDimTree.getMember(baseMember).getMemberProps().getLevelNumber();
 					if (baseMbrLevel > baseDimTree.getAttributeMappingLevel(attrDim)) {
 						isPrimaryKey = true;
 						break validation;
 					}
 				}
 		}
 
 
 		// Return status
 		return isPrimaryKey;
 	}
 
 
 	/**
 	 * Returns true if the intersection is not alias of a base intersection
 	 * 
 	 * @param intersection Cell intersection
 	 * @return True if the intersection is not an alias of a base  intersection 
 	 */
 	public boolean isPrimaryIntersection(Intersection intersection) {
 	
 		// Get the data block key of the specified intersection
 		Intersection dataBlockKey = generateCellAddress(intersection).getDataBlockKey();
 		
 		// The intersection is a primary intersection if its corresponding
 		// data block key is a primary key
 		return isPrimaryDataBlockKey(dataBlockKey);
 	}
 
 	
 
 	/**
 	 *  Remove data blocks for the specified versions. If the version
 	 *  filter is empty or null then no data will be removed.
 	 *  
 	 * @param versionFilter Specifies the versions to clear
 	 */
 	public int clearVersionData(List<String> versionFilter) {
 
 		long startTime = System.currentTimeMillis();
 		int deletedBlockCount = 0;
 		if (versionFilter != null && !versionFilter.isEmpty()) {
 			for (String version : versionFilter) {
 				List<Intersection> dataBlockKeys = null;
 				if (dataBlocksByVersion.get(version) !=null ) {
 					dataBlockKeys = new ArrayList<Intersection>(dataBlocksByVersion.get(version));
 					for (Intersection key : dataBlockKeys) {
 						deleteDataBlock(key);
 						deletedBlockCount++;
 					}
 				}
 			}
 		}
 		
 		String stepDesc = StringUtils.commaFormat(deletedBlockCount) 
 				+ " data block(s) removed from the data cache while initializing the following version(s): " 
 				+  StringUtils.arrayListToString(versionFilter);
 		logger.info(stepDesc);
 		stepDesc = "Removal of " + StringUtils.commaFormat(deletedBlockCount) + " data block(s)";
 		performanceLogger.info(LogUtil.timedStep(stepDesc, startTime));
 		return deletedBlockCount;
 		
 	}
 
 	/**
 	 *	Return the data block corresponding to the specified intersection.
 	 *  Additional information is also returned.
 	 *  
 	 *  If the data block is not found, then the returned data block will
 	 *  be a null value.
 	 *   
 	 * @param intersection Data block key
 	 * 
 	 * @return DataBlockResponse
 	 */
 	private DataBlockResponse getDataBlock(Intersection dataBlockKey) {
 		
 		double[][] dataBlock = null;
 		
 		// Find data block index entry
 		Integer  surrogateKey = dataBlockIndexMap.get(dataBlockKey);
 		
 		// Get data block (if an index entry exists)
 		if (surrogateKey != null) {
 			dataBlock = dataBlockPool.get(surrogateKey);
 			if (dataBlock == null) {
 				// Index entry was found
 				String errMsg = "Data Cache Error - Non-existent data block at intersection: " 
 					+ StringUtils.arrayToString(dataBlockKey.getCoordinates());
 				throw new IllegalArgumentException(errMsg);
 			}
 		}
 		
 		// Return data block response
 		DataBlockResponse dataBlockResp = new DataBlockResponse();
 		dataBlockResp.setSurrogateKey(surrogateKey);
 		dataBlockResp.setDataBlock(dataBlock);
 		return dataBlockResp;
 	}
 
 
 	/**
 	 * 	Update the contents of the specified data block
 	 * 
 	 * @param dataBlockResp Data Block Response Object
 	 */
 	private void updateDataBlock(DataBlockResponse dataBlockResp) {
 		dataBlockPool.set(dataBlockResp.getSurrogateKey(), dataBlockResp.getDataBlock());
 	}
 
 
 	/**
 	 * 	Update the contents of the specified data block
 	 * 
 	 * @param dataBlockKey Data Block Key
 	 * @param dataBlock Data Block
 	 */
 	private void updateDataBlock(Intersection dataBlockKey, double[][] dataBlock) {
 		
 		Integer  surrogateKey = dataBlockIndexMap.get(dataBlockKey);	
 		dataBlockPool.set(surrogateKey, dataBlock);
 		
 	}
 
 
 	/**
 	 *	Return the cell address for the specified intersection
 	 *
 	 * @param intersection Cell intersection 
 	 * @return DataCacheCellAddress
 	 */
 	private DataCacheCellAddress generateCellAddress(Intersection intersection) {
 		
 		int measureAxis = getMeasureAxis();
 		int timeAxis = getTimeAxis();
 		DataCacheCellAddress cellAddress = new DataCacheCellAddress();
 
 		
 		// Convert intersection to data block key
 		Intersection dataBlockKey = generateDataBlockKey(intersection);
 		cellAddress.setDataBlockKey(dataBlockKey);
 		
 		// Set x and y coordinates (measure, time)
 		int measureIndex = getMemberIndex(intersection.getCoordinates()[measureAxis], measureAxis);
 		cellAddress.setCoordX(measureIndex);
 		int timeIndex = getMemberIndex(intersection.getCoordinates()[timeAxis], timeAxis);
 		cellAddress.setCoordY(timeIndex);
 
 		// Return cell address
 		return cellAddress;
 	}
 
 
 	/**
 	 * 	Calculate the data block key for the specified cell intersection
 	 * 
 	 * @param intersection Cell Intersection
 	 * @return Intersection
 	 */
 	private Intersection generateDataBlockKey(Intersection intersection) {
 		
 		Intersection dataBlockKey = null;
 		
 		// The data block key is calculated by taking the intersection
 		// and removing the elements that correspond to the non-key
 		// dimensions. 
 		//
 		// To reduce overhead, this method accesses a pre-built array 
 		// that contains the desired intersection elements for each
 		// given intersection size.  
 		int[] keyDimIndexes = keyIndexesByIsSize[intersection.getSize()];
 		dataBlockKey = intersection.createSubIntersection(keyDimIndexes);
 		return dataBlockKey;
 	}
 
 
 	/**
 	 * 	Filter out loaded reference data intersections from data specification
 	 *  map
 	 *  
 	 * @param memberSpecByAxis Defines a subset of reference data in the data cache 
 	 * 
 	 * @return
 	 */
 	public Map<Integer, List<String>> getFilteredRefDataSpec(Map<Integer, List<String>> memberSpecByAxis) {
 		
 		long filterStartTime = System.currentTimeMillis();
 		String logMsg = null;
 		Map<Integer, List<String>> filteredRefDataSpec = new HashMap<Integer, List<String>>();
 		
 		
 		// Generate all the data block keys represented by the member specifications. Since
 		// reference data is loaded an entire block at a time, we only need to filter at the
 		// data block level instead of the individual intersection level
 		logMsg = "Filtering data cache reference data specification....";
 		logger.debug(logMsg);
 		
 		// Generate all the data block keys represented by the member specifications. Since
 		// reference data is loaded an entire block at a time, we only need to filter at the
 		// data block level instead of the individual intersection level
 		int indexedAxisCount = coreKeyAxes.length;
 		@SuppressWarnings("unchecked")
 		List<String>[] memberLists = new ArrayList[indexedAxisCount]; 
 		for (int i = 0; i < indexedAxisCount; i++) {
 			
 			// Get the next indexed core dimension axis
 			int axis = coreKeyAxes[i];
 			
 			// Get the list of members for the current axis. If there's
 			// no entry in the member spec then select all defined axis
 			// members.
 			List<String> memberList = memberSpecByAxis.get(axis);
 			if (memberList == null) {
 				memberList = new ArrayList<String>(Arrays.asList(getAxisMembers(axis)));
 			}
 			memberLists[i] = memberList;
 		}
 		Odometer dataBlockIterator = new Odometer(memberLists);
 		//List<Intersection> representedDataBlockKeys = IntersectionUtil.buildIntersections(memberLists, indexedCoreDims);
 
 		// Get list of keys for any requested data blocks that don't yet exist
 		List<Intersection> requiredKeys = new ArrayList<Intersection>();
 		while (dataBlockIterator.hasNext()) { 
 			@SuppressWarnings("unchecked")
 			Intersection dataBlockKey = new Intersection(coreKeyDims, (String[])dataBlockIterator.nextValue().toArray(new String[0]));
 			if (!isExistingDataBlock(dataBlockKey)) {
 				requiredKeys.add(dataBlockKey);
 			}
 		}
 		
 		
 		// Build a data spec that defines a superset of all non-existing data blocks
 		// that need to be loaded.
 		if (!requiredKeys.isEmpty()) {
 			
 			// Iterate through the required data block keys and compile a
 			// unique list of required members by dimension. 
 			Map<String, Set<String>> memberSets = new HashMap<String, Set<String>>();
 			for (String dim : coreKeyDims) {
 				memberSets.put(dim, new HashSet<String>());
 			}
 			for (Intersection dataBlockKey : requiredKeys) {
 				for (String dim : coreKeyDims) {
 					memberSets.get(dim).add(dataBlockKey.getCoordinate(dim));
 				}
 			}
 			// Build a revised member specification map that represents the
 			// minimal superset of all the required data block keys 
 			for (String dim : coreKeyDims) {
 				filteredRefDataSpec.put(this.getAxisIndex(dim),
 						new ArrayList<String>(memberSets.get(dim)));
 			}
 			// Add in all defined measure and time members
 			filteredRefDataSpec.put(getMeasureAxis(),
 					Arrays.asList(getAxisMembers(getMeasureAxis())));
 			filteredRefDataSpec.put(getTimeAxis(),
 					Arrays.asList(getAxisMembers(getTimeAxis())));
 		}
 		
 		// Return filtered data spec
 		logMsg = "Filtering of mdb data specification";
 		logger.debug(logMsg);
 		performanceLogger.info(LogUtil.timedStep(logMsg, filterStartTime));
 		return filteredRefDataSpec;
 	}
 
 
 	/**
 	 * Generate a fully expanded unit of work specification
 	 * 
 	 * @return UnitOfWork
 	 */
 	public UnitOfWork getUowSpec() {
 		
 		int baseDimCount = baseDimensions.length;
 		String[][] members = new String[baseDimCount][];
 		for (int i = 0; i < baseDimCount; i++) {
 			members[i] = memberCatalog[i];
 		}
 		UnitOfWork unitOfWork = new UnitOfWork(baseDimensions, members);
 		
 		return unitOfWork;
 	}
 	
 	/**
 	 * @return Returns the current year.
 	 */
 	public String getCurrentYear() {
 		return appDef.getCurrentYear();
 	}
 
 	
 	/**
 	 * @return Returns the lockedPeriods.
 	 */
 	public Set<String> getLockedPeriods() {
 		return lockedPeriods;
 	}
 
 	/**
 	 *	Returns the axis number corresponding to the Measure dimension
 	 *
 	 * @return Axis number corresponding to the Measure dimension
 	 */
 	public int getMeasureAxis() {
 		return getAxisIndex(getMeasureDim());
 	}
 
 	/**
 	 *	Returns the measureDef object for the specified measure
 	 *
 	 * @param measure measure dimension member
 	 *
 	 * @return the measureDef object for the specified measure
 	 */
 	public MeasureDef getMeasureDef(String measure) {
 
 		MeasureDef measureDef = getAppDef().getMeasureDef(measure);
 
 		// Check if versionDef was found
 		if (measureDef == null) {
 			// No matching versionDef found - throw IllegalArgumentException
 			String errMsg = "getMeasureDef() error - no matching measureDef for member: ["
 				+ measure + "]";
 			logger.error(errMsg);
 			IllegalArgumentException iae = new IllegalArgumentException(errMsg);	
 			throw iae;
 		}
 
 		return measureDef;
 	}
 
 	/**
 	 *	Returns the name of the Measure dimension
 	 *
 	 * @return Name of the Measure dimension
 	 */
 	public String getMeasureDim() {
 		return appDef.getMdbDef().getMeasureDim();
 	}
 
 	/**
 	 *	Return the number of members in the Measure dimension
 	 *
 	 * @return The number of members in the Measure dimension
 	 */
 	public int getMeasureSize() {
 		return getAxisSize(getMeasureAxis());
 	}
 
 	/**
 	 *	Returns the measure type for the specified measure
 	 *
 	 * @param measure Measure dimension member
 	 *
 	 * @return the measure type for the specified measure
 	 */
 	public MeasureType getMeasureType(String measure) {
 		return getMeasureDef(measure).getType();	
 	}
 
 
 	/**
 	 *	Returns the axis number corresponding to the PlanType dimension
 	 *
 	 * @return Axis number corresponding to the PlanType dimension
 	 */
 	public int getPlanTypeAxis() {
 		return getAxisIndex(getPlanTypeDim());
 	}
 
 	/**
 	 *	Returns the name of the PlanType dimension
 	 *
 	 * @return Name of the PlanType dimension
 	 */
 	public String getPlanTypeDim() {
 		return appDef.getMdbDef().getPlanTypeDim();
 	}
 
 	/**
 	 *	Returns the axis number corresponding to the Time dimension
 	 *
 	 * @return Axis number corresponding to the Time dimension
 	 */
 	public int getTimeAxis() {
 		return getAxisIndex(getTimeDim());
 	}
 
 	/**
 	 *	Returns the name of the Time dimension
 	 *
 	 * @return Name of the Time dimension
 	 */
 	public String getTimeDim() {
 		return appDef.getMdbDef().getTimeDim();
 	}
 
 	/**
 	 *	Return the number of members in the Time dimension
 	 *
 	 * @return The number of members in the Time dimension
 	 */
 	public int getTimeSize() {
 		return getAxisSize(getTimeAxis());
 	}
 
 	/**
 	 *	Returns the axis number corresponding to the Version dimension
 	 *
 	 * @return Axis number corresponding to the Version dimension
 	 */
 	public int getVersionAxis() {
 		return getAxisIndex(getVersionDim());
 	}
 
 	/**
 	 *	Returns the versionDef object for the specified version
 	 *
 	 * @param version Version dimension member
 	 *
 	 * @return the versionDef object for the specified version
 	 */
 	public VersionDef getVersionDef(String version) {
 
 		VersionDef versionDef = getAppDef().getVersionDef(version);
 
 		// Check if versionDef was found
 		if (versionDef == null) {
 			// No matching versionDef found - throw IllegalArgumentException
 			String errMsg = "getVersionDef() error - no matching versionDef for member: ["
 				+ version + "]";
 			logger.error(errMsg);
 			IllegalArgumentException iae = new IllegalArgumentException(errMsg);	
 			throw iae;
 		}
 
 		return versionDef;
 	}
 
 	/**
 	 *	Returns the versionDefs catalog from the application definition object
 	 *  for those versions that exist in this data cache
 	 *
 	 * @return the versionDefs catalog from the application definition object
 	 */
 	public Map<String, VersionDef> getVersionDefs() {
 		
 		Map<String, VersionDef> validVersionDefs = new HashMap<String, VersionDef>();
 		String[] validVersions = getVersions();
 		for (String version:validVersions) {
 			validVersionDefs.put(version, getAppDef().getVersionDef(version));
 		}
 
 		return validVersionDefs;
 	}
 
 	/**
 	 *	Returns the list of versionDef objects for the derived versions that exist 
 	 *  in this data cache
 	 *
 	 * @return List of versionDef objects
 	 */
 	public List<VersionDef> getDerivedVersionDefs() {
 		
 		List<VersionDef> allDerivedVersionDefs = getAppDef().getDerivedVersionDefs();		
 		List<VersionDef> derivedVersionDefs = new ArrayList<VersionDef>();
 		for (VersionDef versionDef:allDerivedVersionDefs) {
 			String version = versionDef.getName();
 			if (isMember(getVersionDim(), versionDef.getName())) {
 				derivedVersionDefs.add(getAppDef().getVersionDef(version));
 			}
 		}
 
 		return derivedVersionDefs;
 	}
 
 	/**
 	 *	Returns the list of versionDef objects for the variance versions that exist 
 	 *  in this data cache
 	 *
 	 * @return List of versionDef objects
 	 */
 	public List<VersionDef> getVarianceVersionDefs() {
 		
 		List<VersionDef> allVarianceVersionDefs = getAppDef().getVarianceVersionDefs();		
 		List<VersionDef> varianceVersionDefs = new ArrayList<VersionDef>();
 		for (VersionDef versionDef:allVarianceVersionDefs) {
 			String version = versionDef.getName();
 			if (isMember(getVersionDim(), versionDef.getName())) {
 				varianceVersionDefs.add(getAppDef().getVersionDef(version));
 			}
 		}
 
 		return varianceVersionDefs;
 	}
 
 	/**
 	 *	Returns the name of the Version dimension
 	 *
 	 * @return Name of the Version dimension
 	 */
 	public String getVersionDim() {
 		return appDef.getMdbDef().getVersionDim();
 	}
 
 	/**
 	 *	Returns the entire list of versions in the PafDataCache 
 	 *
 	 * @return The entire list of versions with the PafDataCache
 	 */
 	public String[] getVersions() {
 		return getAxisMembers(getVersionAxis());
 	}
 
 	/**
 	 *	Returns the list of Versions with the specified VersionType
 	 *
 	 * @param versionType The Version Type to match
 	 * 
 	 * @return The list of Versions with the specified VersionType
 	 */
 	public List<String> getVersionsByType(VersionType versionType) {
 
 		List<String> matchedVersions = new ArrayList<String>();
 		String[] allVersions = getVersions();
 
 		// Cycle through all defined versions and return the ones that match "VersionType"
 		for(String version:allVersions)  {
 			if (getVersionType(version) == versionType) {
 				matchedVersions.add(version);
 			}
 		}
 		return matchedVersions;
 	}
 
 	/**
 	 *	Return list of base versions
 	 *
 	 * @return List<String>
 	 */
 	public List<String> getBaseVersions() {
 
 		List<String> baseVersions = new ArrayList<String>();
 
 		// Cycle through all defined versions and return the ones that are 
 		// base versions (ForwardPlannable, NonPlannable, or Plannable)
 		for(String version:getVersions())  {
 			VersionType versionType = getVersionType(version);
 			if (PafBaseConstants.BASE_VERSION_TYPE_LIST.contains(versionType)) {
 				baseVersions.add(version);
 			}
 		}
 		return baseVersions;
 	}
 
 	/**
 	 *	Return list of derived versions
 	 *
 	 * @return List<String>
 	 */
 	public List<String> getDerivedVersions() {
 
 		// Get the list of derived versions that are defined to the data cache
 		List<String> derivedVersions = new ArrayList<String>(appDef.getDerivedVersions());
 		List<String> dataCacheVersions = new ArrayList<String>(Arrays.asList(getVersions()));
 		derivedVersions.retainAll(dataCacheVersions);
 
 		return derivedVersions;
 	}
 
 	/**
 	 *	Return list of reference versions - all non-derived versions, except the 
 	 *  active planning versions
 	 *
 	 * @return List<String>
 	 */
 	public List<String> getReferenceVersions() {
 
 		List<String> referenceVersions = getBaseVersions();
 		List<String> activePlanVersions = Arrays.asList(getPlanVersions());
 		
 		referenceVersions.removeAll(activePlanVersions);
 		return referenceVersions;
 	}
 
 	/**
 	 *	Returns the number of members in the Version dimension
 	 *
 	 * @return The number of members in the Version dimension
 	 */
 	public int getVersionSize() {
 		return getAxisSize(getVersionAxis());
 	}
 
 	/**
 	 *	Returns the version type for the specified version
 	 *
 	 * @param version Version dimension member
 	 *
 	 * @return the version type for the specified version
 	 */
 	public VersionType getVersionType(String version) {
 		return getVersionDef(version).getType();	
 	}
 
 	/**
 	 *	Returns the axis number corresponding to the Year dimension
 	 *
 	 * @return Axis number corresponding to the Year dimension
 	 */
 	public int getYearAxis() {
 		return getAxisIndex(getYearDim());
 	}
 
 	/**
 	 *	Returns the name of the Year dimension
 	 *
 	 * @return Name of the Year dimension
 	 */
 	public String getYearDim() {
 		return appDef.getMdbDef().getYearDim();
 	}
 
 	/**
 	 *	Return the number of members in the Year dimension
 	 *
 	 * @return The number of members in the Year dimension
 	 */
 	public int getYearSize() {
 		return getAxisSize(getYearAxis());
 	}
 
 	/**
 	 *	Add data cache cell to the list of changed cells
 	 *
 	 * @param cellIntersection Data cache cell intersection
 	 * @param cellValue Changed cell value
 	 */
 	public void addChangedCell(Intersection cellIntersection, double cellValue) {
 		changedCells.add(cellIntersection, cellValue);		
 	}
 
 	/**
 	 * @return Returns the list of changed data cache cells.
 	 */
 	public List<PafDataCacheCell> getChangedCells() {
 		return changedCells.getCells();
 	}
 
 	/**
 	 * @return Returns the set of changed data cache intersections.
 	 */
 	public Set<Intersection> getChangedIntersections() {
 		return changedCells.getCellIntersections();
 	}
 
 	/**
 	 *	Initialize the list of changed cells
 	 *
 	 */
 	public void initChangedCells() {
 		logger.debug("Initializing changed cells list");
 		this.changedCells = new PafDataCacheCells();
 	}	
 
 	/**
 	 *	Load in cells from another data cache. This is a convenience method for
 	 *  loadCacheCells(sourceCache, memberFilter) where memberFilter is set to null.
 	 *
 	 * @param sourceCache Source data cache
 	 * 
 	 * @return PafDataCache
 	 * @throws PafException 
 	 */
 	public void loadCacheCells(final PafDataCache sourceCache) throws PafException  {
 		loadCacheCells(sourceCache, null);
 	}
 
 	/**
 	 *	Load in cells from another data cache using the supplied member filter
 	 *
 	 * @param sourceCache Source data cache
 	 * @param memberFilter Member filter by dimension
 	 * 
 	 * @return PafDataCache
 	 * @throws PafException 
 	 */
 	public void loadCacheCells(final PafDataCache sourceCache, final Map<String, List<String>> memberFilter) throws PafException  {
 
 		String[] dimensions = getAllDimensions();
 		Map<String, List<String>> generatedMemberFilter = new HashMap<String, List<String>>();
 
 		// By default a filter will be created for each dimension, consisting of the common members
 		// between both data caches. However, any supplied filters for a given dimension will take 
 		// precedence.
 		for (String dimension : dimensions) {
 			// Use dimensionality of all dsCache dimensions, except the version dimension
 			if (memberFilter != null && memberFilter.containsKey(dimension)) {
 				
 				// Validate filter members
 				List<String> memberList = memberFilter.get(dimension);
 				List<String> missingMembers = new ArrayList<String>();
 				for (String member : memberList) {
 					if (!isMember(dimension, member) || !sourceCache.isMember(dimension, member)) {
 						missingMembers.add(member);
 					}
 				}
 				memberList.removeAll(missingMembers);
 				
 				// Exit if there are no valid members for current filtered dimension
 				if (memberList.isEmpty()) {
 					return;
 				}
 				
 				// Add dimension filter
 				generatedMemberFilter.put(dimension, memberList);
 				
 			} else {
 				
 				List<String> destCacheMembers = Arrays.asList((getDimMembers(dimension)));
 				Set<String> commonMembers = new HashSet<String>(destCacheMembers);
 				List<String> sourceCacheMembers = Arrays.asList((sourceCache.getDimMembers(dimension)));
 				commonMembers.retainAll(new HashSet<String>(sourceCacheMembers));
 				//FIX TTN-838 (KRM)
 				if(commonMembers != null && commonMembers.size() == 0){
 					String members = StringUtils.arrayToString(destCacheMembers.toArray(new String[0]), "", "", "", "", ",");
 					String errMsg = "The following dimension: [" + dimension + "], has member(s): [" + members + "] that are defined on the view, but don't exist in the unit of work.";
 					logger.error(errMsg);
 					throw new PafException(errMsg, PafErrSeverity.Error);
 				}
 				generatedMemberFilter.put(dimension, Arrays.asList(commonMembers.toArray(new String[0])));
 			}
 		}
 
 		// Iterate through all cell intersections represented by the member filter
 		Odometer cacheIterator = new Odometer(generatedMemberFilter, dimensions);
 		while(cacheIterator.hasNext()) {
 
 			// Copy source intersection to this data cache
 			@SuppressWarnings("unchecked")
 			ArrayList<String> coords = cacheIterator.nextValue();
 			Intersection intersection = new Intersection(dimensions, coords);
 //			try {
 				setCellValue(intersection, sourceCache.getCellValue(intersection));
 //			} catch (PafException pfe) {
 //				// Problem encountered with source cache index
 //				String errMsg = "Unable to load data into DESTINATION data cache - requested member don't exist in SOURCE data cache.";
 //				logger.error(errMsg);
 //				errMsg = pfe.getMessage() + "  --- " + errMsg;
 //				throw new PafException(errMsg, PafErrSeverity.Error);
 //			}
 		}
 	}
 
 	/**
 	 * 	Get "data slice" using dimensional and member specifications
 	 *  defined in supplied parameter object. The "data slice"
 	 *  contains all of the cell values that are needed to populate
 	 *  a client view section. Meta-data is passed down to the client 
 	 *  via other objects and processes outside of this method.
 	 *
 	 * @param parms Object containing required PafDataSlice parameters
 	 * @param dimSequence Dimension sequence for data cache intersections associated with the corresponding view section
 	 * 
 	 * @return Returns "Data Slice" - a subset of cells in the UowCache
 	 * @throws PafException
 	 */
 	public PafDataSlice getDataSlice(PafDataSliceParms parms, String[] dimSequence) throws PafException {
 
 		boolean hasPageDimensions = false;
 		int cellCount = 0, cols = 0, rows = 0, sliceIndex = 0;
 		double[] dataSlice = null;
 		String[] rowDims = parms.getRowDimensions();
 		String[] colDims = parms.getColDimensions();
 		String[][] rowTuples = parms.getRowTuples();
 		String[][] colTuples = parms.getColTuples();
 		PafDataSlice pafDataSlice = null;
 
 		try {
 			// Validate data slice parms
 			logger.debug("Validating PafDataSlice parameters...");
 			hasPageDimensions = validateDataSliceParms(parms);
 
 			// Creating new data slice array 
 			logger.debug("Creating new data slice array");
 			cols = colTuples.length;
 			rows = rowTuples.length;
 			cellCount = rows * cols;
 			dataSlice = new double[cellCount];
 
 			// Create reusable cell intersection that will to access
 			// data cache data. This intersection will get updated as
 			// we iterate through all the tuple members
 			Intersection cellIs = new Intersection(dimSequence);
 			
 			// Enter page headers into appropriate elements of the data 
 			// cache cell intersection
 			if (hasPageDimensions) {
 				logger.debug("Entering page headers into cell intersection");
 				for (int i = 0; i < parms.getPageDimensions().length; i++) {
 					cellIs.setCoordinate(parms.getPageDimensions()[i], parms.getPageMembers()[i]);
 				}
 			}
 
 			// Load data slice. Start by cycling through row tuples
 			logger.info("Getting data slice - [" + StringUtils.commaFormat(rows)  
 					+ "] rows  X  [" + cols + "] columns  =  [" 
 					+ StringUtils.commaFormat(cellCount) + "] total cells");  
 			for (String[] rowTuple:rowTuples) {
 	
 				// Updated the cell intersection with the current row header members
 				for (int i = 0; i < rowDims.length; i++) {
 					cellIs.setCoordinate(rowDims[i], rowTuple[i]);
 				}
 
 				// Cycle through column tuples
 				for (String[] colTuple:colTuples) {
 
 					// Update the cell intersection with the current column header
 					// members 
 					for (int i = 0; i < colDims.length; i++) {
 						cellIs.setCoordinate(colDims[i], colTuple[i]);
 					}
 	
 					// Copy selected cell to data slice
 					try {
 						dataSlice[sliceIndex++] = getCellValue(cellIs);
 					} catch (IllegalArgumentException iae) {
 						String errMsg = "Data retrieval error - the view references an intersection: "
 							+ StringUtils.arrayToString(cellIs.getCoordinates()) 
 							+ " that is outside the current unit of work";
 						throw new IllegalArgumentException(errMsg);
 					}
 				}
 			}
 
 			// Transfer data slice to PafDataSlice object
 			pafDataSlice = new PafDataSlice(dataSlice, cols);
 
 		} catch (PafException pfe) {
 			// throw Paf Exception
 			throw pfe;
 		} catch (Exception ex) {
 			// throw Paf Exception
 			String errMsg = ex.getMessage();
 			logger.error(errMsg);
 			PafException pfe = new PafException(errMsg, PafErrSeverity.Error, ex);	
 			throw pfe;
 		}
 
 		logger.info("Returning pafDataSlice");
 		return pafDataSlice;
 	}
 
 
 	/**
 	 *	Validate data slice parameters
 	 *
 	 * @param parms PafDataSlice parameter object
 	 * 
 	 * @return True if page dimensions are found among data slice parameters
 	 * @throws PafException 
 	 */
 	public boolean validateDataSliceParms(PafDataSliceParms parms) throws PafException {
 
 		boolean hasPageDimensions;
 		boolean[] dimensionFlags = null;
 		String[] viewDims = pafMVS.getViewSection().getDimensionsPriority();
 		int coreDimCount = coreDimensions.size(), viewDimCount = viewDims.length;
 		String[] pageDimensions = parms.getPageDimensions();
 		String[] colDimensions = parms.getColDimensions();
 		String[] rowDimensions = parms.getRowDimensions();
 		String[][] colMembers = parms.getColTuples();
 		String[] pageMembers = parms.getPageMembers();
 		String[][] rowMembers = parms.getRowTuples();
 
 
 		// Checking for existence of page dimensions
 		if (pageDimensions != null && pageDimensions.length > 0) {
 			hasPageDimensions = true;
 		} else {
 			hasPageDimensions = false;
 		}
 
 		// Check that all arrays are not empty (page dimensions are optional)
 		logger.debug("Validating that arrays are not empty....");
 		//validateArrayNotEmpty(pageDimensions, "pageDimensions");
 		validateArrayNotEmpty(colDimensions, "colDimensions");
 		validateArrayNotEmpty(rowDimensions, "rowDimensions");
 		if (hasPageDimensions) validateArrayNotEmpty(pageMembers, "pageMembers");
 		validateArrayNotEmpty(colMembers, "colMembers");
 		validateArrayNotEmpty(rowMembers, "rowMembers");
 
 		// Validate that the correct number of dimensions have been specified
 		int parmsDimCount = 0;
 		if (hasPageDimensions) {
 			parmsDimCount = pageDimensions.length + colDimensions.length + rowDimensions.length;
 		} else {
 			parmsDimCount = colDimensions.length + rowDimensions.length;
 		}
 		
 		if (parmsDimCount != viewDimCount) {
 			// Incorrect number of dimensions specified
 			String errMsg;
 			if (parmsDimCount >= viewDimCount) {
 				String missingparmDims = " ";
 
 				List<String> viewDimList = Arrays.asList(viewDims);
 				for(String dim : parms.getAllDimensions()){
 					if (! viewDimList.contains(dim)){
 						missingparmDims += dim + ", ";
 					}
 				}
 				
 				errMsg = "The view definition has more dimensions than the server expects.  The extra view dimensions are: " + missingparmDims;
 			}else{
 				errMsg = parmsDimCount + " dimensions specified in data slice parms, but data cache has "
 				+ viewDimCount + " dimensions specified"; 
 			}
 			logger.error(errMsg);
 			PafException pfe = new PafException(errMsg, PafErrSeverity.Error);	
 			throw pfe; 				
 		}
 
 		// Check that all dimensions are valid and all core dimensions are accounted for 
 		logger.debug("Validating that all dimensions are valid and all core dimensions are accounted for....");
 		dimensionFlags = new boolean[coreDimCount];
 		for (int i = 0; i < coreDimCount; i++) {
 			dimensionFlags[i] = false;
 		}
 		if (hasPageDimensions) {
 			for (String dimension:pageDimensions) {
 				if (axisIndexMap.containsKey(dimension)) {
 					// Indicate that dimension was found (if it's a core dimension)
 					if (coreDimensions.contains(dimension)) {
 						dimensionFlags[axisIndexMap.get(dimension)] = true;
 					}
 				} else {
 					// Unknown page dimension
 					String errMsg = "Unknown page dimension [" + dimension 
 					+ "] found in paf data slice parms";
 					logger.error(errMsg);
 					PafException pfe = new PafException(errMsg, PafErrSeverity.Error);	
 					throw pfe; 				
 				}
 			}
 		}
 		for (String dimension:colDimensions) {
 			if (axisIndexMap.containsKey(dimension)) {
 				// Indicate that dimension was found (if it's a core dimension)
 				if (coreDimensions.contains(dimension)) {
 					dimensionFlags[axisIndexMap.get(dimension)] = true;
 				}
 			} else {
 				// Unknown column dimension
 				String errMsg = "Unknown column dimension [" + dimension 
 				+ "] found in paf data slice parms";
 				logger.error(errMsg);
 				PafException pfe = new PafException(errMsg, PafErrSeverity.Error);	
 				throw pfe; 				
 			}
 		}
 		for (String dimension:rowDimensions) {
 			if (axisIndexMap.containsKey(dimension)) {
 				// Indicate that dimension was found (if it's a core dimension)
 				if (coreDimensions.contains(dimension)) {
 					dimensionFlags[axisIndexMap.get(dimension)] = true;
 				}
 			} else {
 				// Unknown row dimension
 				String errMsg = "Unknown row dimension [" + dimension 
 						+ "] found in paf data slice parms";
 				logger.error(errMsg);
 				PafException pfe = new PafException(errMsg, PafErrSeverity.Error);	
 				throw pfe; 				
 			}
 		}
 		for (int i = 0; i < coreDimCount; i++) {
 			if (!dimensionFlags[i]) {
 				// Unknown row dimension
 				String errMsg = "Dimension [" + allDimensions.get(i) 
 						+ "] was not specified in the data slice parms";
 				logger.error(errMsg);
 				PafException pfe = new PafException(errMsg, PafErrSeverity.Error);	
 				throw pfe; 								
 			}
 		}
 
 		// Check that corresponding dimension and tuple arrays match
 		if (hasPageDimensions) {
 			if (pageDimensions.length != pageMembers.length) {
 				// Row dimensions array has different number of dimensions than row tuples array
 				String errMsg = "Page dimensions array has different number of dimensions than page tuples array";
 				logger.error(errMsg);
 				PafException pfe = new PafException(errMsg, PafErrSeverity.Error);	
 				throw pfe; 	
 			}
 		}
 		if (rowDimensions.length != rowMembers[0].length) {
 			// Row dimensions array has different number of dimensions than row tuples array
 			String errMsg = "Row dimensions array has different number of dimensions than row tuples array";
 			logger.error(errMsg);
 			PafException pfe = new PafException(errMsg, PafErrSeverity.Error);	
 			throw pfe; 											
 		}
 		if (colDimensions.length != colMembers[0].length) {
 			// Row dimensions array has different number of dimensions than row tuples array
 			String errMsg = "Column dimensions array has different number of dimensions than column tuples array";
 			logger.error(errMsg);
 			PafException pfe = new PafException(errMsg, PafErrSeverity.Error);	
 			throw pfe; 											
 		}
 
 		// Indicate presence of page dimensions
 		return hasPageDimensions;
 	}
 
 
 	/**
 	 * Return the dimension for the specified axis
 	 * 
 	 * @param axis Axis index
 	 * @return Returns the dimension name.
 	 */
 	public String getDimension(int axis) {
 
 		// Validate axis
 		if (axis < 0 || axis > axisCount - 1) {
 			// Invalid axis value
 			String errMsg = "Unable to get dimension for axis ["
 				+ axis + "] - axis value is out of bounds";
 			logger.error(errMsg);
 			IllegalArgumentException iae = new IllegalArgumentException(errMsg);    
 			throw iae;          			
 		}
 		return allDimensions.get(axis);
 	}
 
 
 	/**
 	 *	Return corresponding data cache axis indexes 
 	 *	for supplied array of dimension names
 	 *
 	 * @param dimNames Array of dimension names
 	 * @return Returns array of data cache axis indexes
 	 */
 	public int[] getDimIndexes(String[] dimNames) {
 
 		int[] dimIndexes = new int[dimNames.length];
 		for (int i = 0; i < dimNames.length; i++) {
 			dimIndexes[i] = getAxisIndex(dimNames[i]);
 		}
 		return dimIndexes;
 	}
 
 
 	/**
 	 *	Return the dimension member for the corresponding axis and member index
 	 *
 	 * @param axis Axis index
 	 * @param memberIndex Dimension member index
 	 * @return Returns the dimension member for the corresponding axis and member index.
 	 */
 	public String getDimMember(int axis, int memberIndex) {
 		return getAxisMembers(axis)[memberIndex];
 	}    
 
 
 	/**
 	 *	Return an array containing the members in the specified dimension name
 	 *
 	 * @param dimName Dimension name
 	 * @return Returns an array containing the members in the specified axis.
 	 */
 	public String[] getDimMembers(String dimName) {
 		return getAxisMembers(getAxisIndex(dimName));
 	}    
 
 
 	/**
 	 *	Return number of data cache rows
 	 *
 	 * @return Returns the number of data cache rows.
 	 */
 	public int getRowCount() {
 		return getCellCount() / getColumnCount();
 	}
 
 
 	/**
 	 *	Convert a member name to its corresponding index value
 	 *
 	 * @param axis Axis index
 	 * @param member Dimension member
 	 * 
 	 * @return Returns a dimension member index.
 	 */
 	public int getMemberIndex(String member, int axis) {
 
 		int memberIndex = 0;
 
 		// Validate axis
 		if (axis < 0 || axis > axisCount - 1) {
 			// Invalid axis value
 			String errMsg = "Unable to convert member [" 
 				+ member + "] to an index - axis value of [" + axis + "] - axis is out of bounds";
 			logger.error(errMsg);
 			IllegalArgumentException iae = new IllegalArgumentException(errMsg);    
 			throw iae;          			
 		}
 
 		// Compute member index
 		if (memberIndexMap[axis].containsKey(member)) {
 			memberIndex = memberIndexMap[axis].get(member);
 		} else { 
 			// Member not found
 			String errMsg = "Member to index conversion error - member ["
 				+ member + "] not found in axis [" + axis + "] (dimension [" + allDimensions.get(axis)  + "])";
 			logger.error(errMsg);
 			IllegalArgumentException iae = new IllegalArgumentException(errMsg);    
 			throw iae;          			
 		}
 
 		return memberIndex;
 	}
 
 
 
 	/**
 	 *	Determines if the specified member exists in the specified dimension
 	 *
 	 * @param dimension Dimension name
 	 * @param member Dimension member name
 	 * 
 	 * @return True if the member exists in the specified dimension
 	 */
 	public boolean isMember(String dimension, String member) {
 
 		boolean isFound = false;
 
 		// Get axis for specified dimension
 		int axis = getAxisIndex(dimension);
 
 		// Look for member
 		Map<String, Integer> memberMap = memberIndexMap[axis];
 		if (memberMap.containsKey(member)) {
 			isFound = true;
 		}
 
 		// Return status
 		return isFound;
 	}
 
 	/**
 	 *	Determines if the specified member exists in the specified dimension,
 	 *  ignoring case.
 	 *
 	 * @param dimension Dimension name
 	 * @param searchMember Dimension member name to search for
 	 * @param foundMember Dimension member name found (could differ in case)
 	 * 
 	 * @return True if the member exists in the specified dimension
 	 */
 	public boolean isMemberIgnoresCase(String dimension, String searchMember, StringBuffer foundMember) {
 
 	
 		// Initialize found member string buffer
 		foundMember.setLength(0);
 		foundMember.trimToSize();
 		
 		// Look for member - ignoring case
 		if (isMember(dimension, searchMember)) {
 			foundMember.append(searchMember);
 			return true;
 		}
 			
 		// Check for alternate case match
 		String[] members = getDimMembers(dimension);
 		for (String member : members) {
 			if (searchMember.equalsIgnoreCase(member)) {
 				foundMember.append(member);
 				return true;
 			}
 		}
 
 		// Member not found
 		return false;
 	}
 
 
 	/**
 	 *	Determines if the specified data block exists in the data cache
 	 *
 	 * @param key Data block key
 	 * @return True if the specified data block exists in the data cache
 	 */
 	public boolean isExistingDataBlock(Intersection key) {
 
 		boolean isFound = false;
 
 		// Look for data block
 		if (dataBlockIndexMap.containsKey(key)) {
 			isFound = true;
 		}
 
 		// Return status
 		return isFound;
 	}
 
 	/**
 	 *	Determines if the specified intersection exists in the data cache
 	 *
 	 * @param intersection Cell intersection
 	 * @return True if the specified intersection exists in the data cache
 	 */
 	public boolean isExistingIntersection(Intersection intersection) {
 
 		boolean isFound = false;
 
 		// An intersection exists if its coordinates are valid and
 		// its corresponding data block exists.
 		
 		// Get the data block key (this step also validates the measure and time members)
 		DataCacheCellAddress cellAddress = generateCellAddress(intersection);
 		
 		// Look for corresponding data block
 		if (dataBlockIndexMap.containsKey(cellAddress.getDataBlockKey())) {
 			isFound = true;
 		}
 
 		// Return status
 		return isFound;
 	}
 
 
 	/**
 	 *	Determines if the specified key represents a valid data block. This 
 	 *  does not necessarily mean that the data block exists in the data cache.
 	 *
 	 * @param key Data block key
 	 * @return True if the specified intersection is a valid data cache intersection
 	 */
 	public boolean isValidDataBlock(Intersection key) {
 
 		int minKeyDims = coreDimensions.size() - NON_KEY_DIM_COUNT;
 		int maxKeyDims = allDimensions.size() - NON_KEY_DIM_COUNT;
 
 		
 		// Key can't be null
 		if (key == null) {
 			return false;
 		}
 		
 		// Check data block key size. The data block key must be big enough
 		// all core data cache key dimensions, but can't be bigger than the 
 		// number allowable key dimensions.
 		int keyDimCount = key.getDimensions().length;
 		if (keyDimCount < minKeyDims || keyDimCount > maxKeyDims ) {
 			return false;
 		}
 		
 		// Core key validation (check the required key dimensions and
 		// coordinates)
 		for (int i = 0; i < minKeyDims; i++) {
 			
 			// Lookup the axis number
 			int axis = coreKeyAxes[i];
 			
 			// Validate dimension name and dimension order
 			String dim = key.getDimensions()[i];
 			if (!dim.equals(this.getDimension(axis))) {
 				return false;
 			}
 			
 			// Validate member name
 			String member = key.getCoordinate(dim);
 			if (!this.isMember(dim, member)) {
 				return false;
 			}
 		}
 
 		
 		// Check any remaining key dimensions to ensure that they are valid
 		// dimensions and that each corresponding coordinate is mapped to a
 		// valid member
 		for (int i = minKeyDims; i < keyDimCount; i++) {
 
 			// Validate dimension name and dimension order
 			String dim = key.getDimensions()[i];
 			if (!nonCoreDims.contains(dim)) {
 				return false;
 			}
 			
 			// Validate member name
 			String member = key.getCoordinate(dim);
 			if (!this.isMember(dim, member)) {
 				return false;
 			}
 						
 		}
 
 		// Return status
 		return true;
 	}
 
 	/**
 	 *	Determines if the specified intersection is a valid data cache intersection. This
 	 *  does not necessarily mean that the intersection exists in the data cache.
 	 *  
 	 *  This method does not check for attribute intersection validity
 	 *
 	 * @param intersection Cell intersection
 	 * @return True if the specified intersection is a valid data cache intersection
 	 */
 	public boolean isValidIntersection(Intersection intersection) {
 
 		// Assume that intersection is not null
 		
 		// Check intersection size. The intersection must be big enough to fit 
 		// all core data cache dimensions, but can't be bigger than the number
 		// of allowable dimensions.
 		int isDimCount = intersection.getDimensions().length;
 		if (isDimCount < coreDimensions.size() || isDimCount > allDimensions.size()) {
 			return false;
 		}
 		
 		// Check core dimensions in sequence by position
 		for (int axis = 0; axis < coreDimensions.size(); axis++) {
 			
 			// Validate dimension name and dimension order
 			String dim = intersection.getDimensions()[axis];
 			if (!dim.equals(this.getDimension(axis))) {
 				return false;
 			}
 			
 			//Validate member name
 			String member = intersection.getCoordinate(dim);
 			if (!this.isMember(dim, member)) {
 				return false;
 			}
 		}
 
 		// Check remaining dimensions to ensure that they are valid 
 		// and that each corresponding coordinate is mapped to a valid
 		// member
 		for (int i = coreDimensions.size(); i < isDimCount; i++) {
 
 			// Validate dimension name and dimension order
 			String dim = intersection.getDimensions()[i];
 			if (!nonCoreDims.contains(dim)) {
 				return false;
 			}
 			
 			// Validate member name
 			String member = intersection.getCoordinate(dim);
 			if (!this.isMember(dim, member)) {
 				return false;
 			}
 						
 		}
 
 
 		// Return status
 		return true;
 	}
 
 	
 	/**
 	 *	Validate member filter used for retrieving filtered data cache data
 	 *
 	 * @param memberFilter Map comprised of member lists for each filtered dimension
 	 * @throws IllegalArgumentException
 	 */
 	public void validateMemberFilter(Map<String, List<String>> memberFilter) {
 		for (String dimension:memberFilter.keySet()) {
 			List<String> members = memberFilter.get(dimension);
 			if (members == null || members.size() == 0) {
 				// Empty member list - throw IllegalArgumentException
 				String errMsg = "Member filter error - null or empty member list specifed on filtered dimension: ["
 					+ dimension + "]";
 				logger.error(errMsg);
 				throw (new IllegalArgumentException(errMsg));
 			}
 		}		
 	}
 
 
 	/**
 	 * @return the isDirty
 	 */
 	public boolean isDirty() {
 		return isDirty;
 	}
 
 
 	/**
 	 * @param isDirty the isDirty to set
 	 */
 	public void setDirty(boolean isDirty) {
 		this.isDirty = isDirty;
 	}
 
 
 	/**
 	 * @return the componentBaseMemberMap
 	 */
 	public Map<Intersection, List<String>> getComponentBaseMemberMap() {
 		return componentBaseMemberMap;
 	}
 
 	/**
 	 *	Get component base members for the specified intersection
 	 *
 	 * @param baseMemberIs Base member intersection
 	 * @return List<String>
 	 */
 	public List<String> getComponentBaseMembers(Intersection baseMemberIs) {
 
 		List<String> componentMembers = null;
 		if (componentBaseMemberMap.containsKey(baseMemberIs)) {
 			componentMembers = componentBaseMemberMap.get(baseMemberIs);
 		} else {
 			componentMembers = new ArrayList<String>();
 		}
 		return componentMembers;
 	}
 
 
 	/**
 	 *	Add component members for the specified base member intersection
 	 *
 	 * @param baseMemberIs Base member intersection
 	 * @param componentMembers Component base members
 	 */
 	public void addComponentBaseMembers(Intersection baseMemberIs, List<String> componentMembers) {
 
 		// Add new collection
 		componentBaseMemberMap.put(baseMemberIs, componentMembers);
 
 	}
 
 	
 	/*
 	 *	Represent the DataCache as a 2-dimensional array of data cells
 	 *
 	 * @see java.lang.Object#toString()
 	 */
 	public String toString() {
 
 		int measureSize = getMeasureSize();
 		int timeSize = getTimeSize();
 		StringBuffer stringBuffer = new StringBuffer("\n");	
 		String format = "%#11.2f\t";
 
 		for (double[][] dataBlock : dataBlockPool) {
 			for (int measureIndex = 0; measureIndex < measureSize; measureIndex++ ) {
 				for (int timeIndex = 0; timeIndex < timeSize; timeIndex++ ) {
 					stringBuffer.append(String.format(format, dataBlock[measureIndex][timeIndex]));
 				}
 				stringBuffer.append("\n");
 			}
 		}
 		return stringBuffer.toString();
 	}
 
 
 	/**
 	 * @param attrIs Attribute intersection
 	 * @param attrDimNames Attribute dimension names
 	 * 
 	 * @return True if the the attribute intersection is invalid
 	 */
 	public boolean isInvalidAttributeIntersection(Intersection attrIs, String[] attrDimNames) {
 		return !isValidAttributeIntersection(attrIs, attrDimNames);
 	}
 
 
 	/**
 	 * Determines if the specified attribute intersection is valid
 	 * 
 	 * @param attrIs Attribute intersection
 	 * @param attrDimNames Attribute dimension names
 	 * 
 	 * @return True if the intersection represents a valid attribute intersection
 	 */
 	public boolean isValidAttributeIntersection(Intersection attrIs, String[] attrDimNames) {
 		
 		boolean isValid = true;
 		
 		// Select each base dimension with attributes and verify each 
 		// corresponding attribute combination
 		final List<String> attrDimList = Arrays.asList(attrDimNames);
 		final Set<String> baseDimNames = getBaseDimNamesWithAttributes();
 		validation:
 			for (String baseDimName : baseDimNames) {
 	
 				PafBaseTree baseTree = (PafBaseTree) dimTrees.getTree(baseDimName);
 				Set<String> assocAttrDimSet =  new HashSet<String>(baseTree.getAttributeDimNames());
 				assocAttrDimSet.retainAll(attrDimList);
 				if (!assocAttrDimSet.isEmpty()) {
 					String baseMemberName = attrIs.getCoordinate(baseDimName);
 					String[] assocAttrDims = assocAttrDimSet.toArray(new String[0]);
 					String[] attrCombo = attrIs.getCoordinates(assocAttrDims);
 					if (!AttributeUtil.isValidAttributeCombo(baseDimName, baseMemberName,
 							assocAttrDims, attrCombo, dimTrees)) {
 						isValid = false;
 						break validation;
 					}
 				}
 			}
 	
 		return isValid;
 	}
 
 
 }
