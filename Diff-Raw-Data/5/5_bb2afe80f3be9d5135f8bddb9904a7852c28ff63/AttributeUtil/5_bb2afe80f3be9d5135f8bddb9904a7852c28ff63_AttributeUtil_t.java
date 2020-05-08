 /**
  * 
  */
 package com.pace.base.mdb;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.log4j.Logger;
 
 import com.pace.base.PafException;
 import com.pace.base.app.PafDimSpec;
 import com.pace.base.data.Coordinates;
 import com.pace.base.data.Intersection;
 import com.pace.base.data.IntersectionUtil;
 import com.pace.base.data.MemberTreeSet;
 import com.pace.base.data.TimeSlice;
 import com.pace.base.utility.StringOdometer;
 
 /**
  * Attribute Utilities
  * 
  * @author Alan Farkas
  *
  */
 public abstract class AttributeUtil {
 
 	private static Logger logger = Logger.getLogger(AttributeUtil.class);
 	
 	/**
 	 *	Determine if the specified attribute member combination is invalid
 	 *	for the specified base member and attribute dimensions
 	 *
 	 *  This is a convenience method for isInvalidAttributeCombo(String baseDimName, 
 	 *  String baseMemberName, String[] attrDimNames, String[] attrCombo, 
 	 *	MemberTreeSet uowTrees) where the member tree map is converted into a 
 	 *	MemberTreeSet
 	 *  
 	 * @param baseDimName Base dimension name
 	 * @param baseMemberName Base member name
 	 * @param attrDimNames Attribute dimension names
 	 * @param attrCombo Attribute member combination
 	 * @param memberTreeMap Collection of member trees keyed by dimension name
 	 * 
 	 * @return True if the attribute combination is invalid
 	 */
 	public static boolean isInvalidAttributeCombo(String baseDimName, String baseMemberName, String[] attrDimNames, String[] attrCombo, 
 			Map<String, PafDimTree> memberTreeMap) {
 		
 		return isInvalidAttributeCombo(baseDimName, baseMemberName, attrDimNames, attrCombo, new MemberTreeSet(memberTreeMap));
 	}
 
 	/**
 	 *	Determine if the specified attribute member combination is invalid
 	 *	for the specified base member and attribute dimensions
 	 *
 	 * @param baseDimName Base dimension name
 	 * @param baseMemberName Base member name
 	 * @param attrDimNames Attribute dimension names
 	 * @param attrCombo Attribute member combination
 	 * @param uowTrees Collection of uow cache trees
 	 * 
 	 * @return True if the attribute combination is invalid
 	 */
 	public static boolean isInvalidAttributeCombo(String baseDimName, String baseMemberName, String[] attrDimNames, String[] attrCombo, 
 			MemberTreeSet uowTrees) {
 		
 		return !isValidAttributeCombo(baseDimName, baseMemberName, attrDimNames, attrCombo, uowTrees);
 	}
 
 //	/**
 //	 *	Determine if the specified attribute member combination is valid
 //	 *	for the specified base member, attribute dimensions, and uow
 //	 *  trees.
 //	 *
 //	 * @param baseDimName Base dimension name
 //	 * @param baseMemberName Base member name
 //	 * @param attrDimNames Attribute dimension names
 //	 * @param attrCombo Attribute member intersection
 //	 * @param uowTrees Collection of uow cache trees
 //	 * 
 //	 * @return True if the attribute combination is valid
 //	 */
 //	public static boolean isValidAttributeCombo(String baseDimName, String baseMemberName, String[] attrDimNames, 
 //			String[] attrCombo, MemberTreeSet uowTrees) {
 //	
 //		boolean isValid = false;
 //	
 //		// Create custom intersection object
 //		Intersection intersection = new Intersection(attrDimNames, attrCombo);
 //	
 //		// Get set of valid intersection objects
 //		Set<Intersection> intersections = getValidAttributeCombos(baseDimName, baseMemberName, attrDimNames, uowTrees);
 //	
 //		// Validate intersection
 //		if (intersections.contains(intersection)) {
 //			isValid = true;
 //		}
 //		return isValid;
 //	}
 
 	/**
 	 *	Determine if the specified attribute member combination is valid
 	 *	for the specified base member, attribute dimensions, and uow
 	 *  trees.
 	 *
 	 * @param baseDimName Base dimension name
 	 * @param baseMemberName Base member name
 	 * @param attrDimNames Attribute dimension names
 	 * @param attrCombo Combination of one or more attribute members, each from a different attribute dimension
 	 * @param uowTrees Collection of uow cache trees
 	 * 
 	 * @return True if the attribute combination is valid
 	 */
 	public static boolean isValidAttributeCombo(String baseDimName, String baseMemberName, String[] attrDimNames, 
 			String[] attrCombo, MemberTreeSet uowTrees) {
 	
 
 		PafBaseTree baseTree = uowTrees.getBaseTree(baseDimName);
 		boolean isValid = true;
 		
 //		// Check if all attributes are mapped to the same base tree level
 //		Integer mappingLevel = null;
 //		for (String attrDimName:attrDimNames) {
 //			int level = baseTree.getAttributeMappingLevel(attrDimName);	
 //			if (mappingLevel !=null) {
 //				if (level != mappingLevel) {
 //					// Mapping levels aren't consistent - attribute combination is not valid
 //					return false;
 //				} 
 //			} else {
 //				// First attribute dimension - initialize mapppingLevel
 //				mappingLevel = level;
 //			}
 //		}
 //		
 		
 		// Validate the attribute member combination. The attribute member combination is only valid
 		// if all of its members pass validation. 
 		int attrDimCount = attrDimNames.length;
 		for (int i = 0; i < attrDimCount && isValid; i++) {
 			
 			String attrDimName = attrDimNames[i], attrMemberName = attrCombo[i];
 			PafAttributeTree attrTree = uowTrees.getAttributeTree(attrDimName);
 			
 			// Validate attribute member name
 			if (!attrTree.hasMember(attrMemberName)) {
 				String errMsg = "getAttributeIntersections error - attribute dim names are null or empty";
 				logger.error(errMsg);
 				throw new IllegalArgumentException(errMsg);				
 			}
 			
 			// Validate the corresponding attribute member. The attribute member is valid if it or 
 			// one of its level 0 descendants is mapped to the specified base member or one of 
 			// its descendants.
 			Set<String> lev0AttrMembers = new HashSet<String>(attrTree.getLowestMemberNames(attrMemberName));
 			Set<String> validAttrValues = baseTree.getAttributeMembers(baseMemberName, attrDimName);
 			lev0AttrMembers.retainAll(validAttrValues);
 			if (lev0AttrMembers.isEmpty()) {
 				isValid = false;
 			}
 		}
 		
 		// Return status
 		return isValid;
 	}
 
 
 	/**
 	 *	Return the valid attribute member combinations for the specified
 	 *  base dimension, base member, attribute dimension(s), and member
 	 *  tree map. 
 	 *  
 	 *  This is a convenience method for getValidAttributeCombos (baseDimName,
 	 *  baseMemberName, attrDimNames, memberTrees) where the member tree
 	 *  map is converted into a MemberTreeSet
 	 *  
 	 *  If all attributes aren't mapped to the same base member level, then an 
 	 *  empty set is returned.
 	 * 
 	 * @param baseDimName Base dimension name
 	 * @param baseMemberName Base member name
 	 * @param attrDimNames Array of attribute dimension name(s)
 	 * @param memberTreeMap Collection of member trees keyed by dimension name
 	 *
 	 * @return Set<Intersection>
 	 */
 	public static Set<Intersection> getValidAttributeCombos(final String baseDimName, final String baseMemberName, 
 			final String[] attrDimNames, final Map<String, PafDimTree> memberTreeMap) {
 		
 		MemberTreeSet treeSet = new MemberTreeSet(memberTreeMap);		
 		return getValidAttributeCombos(baseDimName, baseMemberName,  attrDimNames, treeSet);
 	}
 
 	/**
 	 *	Return the valid attribute member combinations for the specified
 	 *  base dimension, base member, attribute dimension(s), and uow trees. 
 	 *  
 	 *  If all attributes aren't mapped to the same base member level, then an 
 	 *  empty set is returned.
 	 * 
 	 * @param baseDimName Base dimension name
 	 * @param baseMemberName Base member name
 	 * @param attrDimNames Array of attribute dimension name(s)
 	 * @param uowTrees Collection of uow trees
 	 *
 	 * @return Set<Intersection>
 	 */
 	@SuppressWarnings("unchecked")
 	public static Set<Intersection> getValidAttributeCombos(final String baseDimName, final String baseMemberName, 
 			final String[] attrDimNames, MemberTreeSet uowTrees)  {
 	
 		Set<Intersection> attrCombos = new HashSet<Intersection>();
 		PafBaseTree baseTree = null;
 	
 		// Throw exception, if attribute dim names is null or the array is empty
 		if ( attrDimNames == null || attrDimNames.length == 0 ) {
 			String errMsg = "getAttributeIntersections error - attribute dim names are null or empty";
 			logger.error(errMsg);
 			throw new IllegalArgumentException(errMsg);
 		}
 		
 		int attrDimCount = attrDimNames.length;
 		 
 		Set<Intersection> level0AttrCombinations = new HashSet<Intersection>();
 	
 		// Get the set of level 0 attribute member intersections for the selected base member
 		baseTree = uowTrees.getBaseTree(baseDimName);
 		level0AttrCombinations = baseTree.getAttributeCombinations(baseMemberName, attrDimNames);
 	
 		// Cycle through each level 0 attribute intersection and generate all valid member 
 		// combinations of these level 0 attributes and their ancestor members.
 		for (Intersection attrIs:level0AttrCombinations) {
 	
 			// Generate an array of member lists containing each attribute dimension's
 			// valid level 0 attributes along with their ancestors
 			String[] attrMemberCombo = attrIs.getCoordinates();
 			List<String>[] memberLists = new List[attrDimCount];
 			for (int i = 0; i < attrDimCount; i++) {
 				String attrDimName = attrDimNames[i];
 				String attrMemberName = attrMemberCombo[i];
 				memberLists[i] = new ArrayList<String>();
 				memberLists[i].add(attrMemberCombo[i]);	
 				List<PafDimMember> ancestors = uowTrees.getAttributeTree(attrDimName).getAncestors(attrMemberName);
 				for (PafDimMember attrMember:ancestors) {
 					memberLists[i].add(attrMember.getKey());	    			
 				}
 			}
 	
 			// Use the odometer to generate all the possible attribute member combinations and
 			// add them to the intersection collection.
 			StringOdometer isIterator = new StringOdometer(memberLists);
 			while (isIterator.hasNext()) {
 				String[] coords = isIterator.nextValue();		// TTN-1851
 				Intersection is = new Intersection(attrDimNames, coords);	// TTN-1851
 				attrCombos.add(is);
 			}
 	
 		}
 	
 		// Return the set of valid attribute combinations
 		return attrCombos;
 	}
 
 	/**
 	 *	Return the valid list of attribute members and rollups
 	 *  on the requested attribute dimension, in light of selections 
 	 *  on the related base dimension and selections on any 
 	 *  related attribute members
 	 *  
 	 * @param attrRequest Valid attribute request object
 	 * @return PafValidAttrResponse Valid attribute response object
 	 * 
 	 * @param requestedAttrDim - Requested attribute dimension
 	 * @param selBaseDim - Selected base dimension
 	 * @param selBaseMember - Selected base member
 	 * @param selAttrSpecs - Selected attribute dimension (can be null)
 	 * @param memberTrees - Member tree map keyed by dimension name 
 	 * 
 	 * @return String[]
 	 */
 	public static String[] getValidAttributeMembers(String requestedAttrDim, String selBaseDim, String selBaseMember, PafDimSpec[] selAttrSpecs, Map<String, PafDimTree> memberTrees) {
 	
 		// Validate parameters
 		if (requestedAttrDim == null || requestedAttrDim.equals("")) {
 			String errMsg = "Unable to get valid attribute members - reqAttriDim is null or blank";
 			logger.info(errMsg);
 			throw new IllegalArgumentException(errMsg);
 		}
 		if (selBaseDim == null || selBaseDim.equals("")) {
 			String errMsg = "Unable to get valid attribute members - selBaseDim is null or blank";
 			logger.info(errMsg);
 			throw new IllegalArgumentException(errMsg);
 		}
 		if (selBaseMember == null || selBaseMember.equals("")) {
 			String errMsg = "Unable to get valid attribute members - selBaseMember is null or blank";
 			logger.info(errMsg);
 			throw new IllegalArgumentException(errMsg);
 		}
 		
 		// Execute simplified logic if there are no attribute member selections
 		if (selAttrSpecs == null || selAttrSpecs.length == 0) {
 			
 			// Get set of  all valid attribute intersections
 			String allAttrDims[] = new String[1];
 			allAttrDims[0] = requestedAttrDim;
 			Set<Intersection> validAttrIntersections = getValidAttributeCombos(selBaseDim, selBaseMember, allAttrDims, memberTrees);
 	
 			// Return set of valid attributes
 			Set<String> validAttrSet = new HashSet<String>();
 			for (Intersection validAttrIS:validAttrIntersections) {
 				validAttrSet.add(validAttrIS.getCoordinate(requestedAttrDim));
 			}
 			return validAttrSet.toArray(new String[0]);
 			
 		}
 		
 		// Combine requested attribute dimension and selected attribute dimension
 		// array into a single array
 		String[] allAttrDims = new String[selAttrSpecs.length + 1];
 		allAttrDims[0] = requestedAttrDim;
 		int i = 1;
 		for (PafDimSpec dimSpec : selAttrSpecs) {
 			allAttrDims[i++] = dimSpec.getDimension();	
 		}
 		
 		// Create map of attribute selections
 		Map<String, Set<String>> selAttrMap = new HashMap<String, Set<String>>();
 		for (PafDimSpec dimSpec : selAttrSpecs) {
 			Set<String> selAttributes = new HashSet<String>(Arrays.asList(dimSpec.getExpressionList()));
 			selAttrMap.put(dimSpec.getDimension(), selAttributes);
 		}
 		
 		// Get the set of valid attribute member intersections for the selected base member
 		Set<Intersection> validAttrIntersections = getValidAttributeCombos(selBaseDim, selBaseMember, allAttrDims, memberTrees);
 	
 		// Remove any intersections for unselected attributes
 		Set<Intersection> invalidLevel0AttrIntersections = new HashSet<Intersection>();
 		for (String selAttrDim:selAttrMap.keySet()) {
 			Set<String> selAttributes = selAttrMap.get(selAttrDim);
 			for (Intersection attributeIS:validAttrIntersections) {
 				if (!selAttributes.contains(attributeIS.getCoordinate(selAttrDim))) {
 					invalidLevel0AttrIntersections.add(attributeIS);
 				}
 			}
 		}
 		validAttrIntersections.removeAll(invalidLevel0AttrIntersections);
 		
 		// Get the unique list of valid attributes
 		Set<String> validAttributeSet = new HashSet<String>();
 		for (Intersection attrIs:validAttrIntersections) {
 			validAttributeSet.add(attrIs.getCoordinate(requestedAttrDim));
 		}
 				
 		// Add in ancestors of each unique attribute
 		Set<String> validAncestors = new HashSet<String>();
 		for (String validAttributeMember:validAttributeSet) {
 	
 			List<PafDimMember> ancestors = memberTrees.get(requestedAttrDim).getAncestors(validAttributeMember);
 			for (PafDimMember attrMember:ancestors) {
 				validAncestors.add(attrMember.getKey());	    			
 			}
 	
 		}
 		validAttributeSet.addAll(validAncestors);
 		
 		// Return valid attribute members
 		String[] validAttributeMembers = validAttributeSet.toArray(new String[0]);
 		return validAttributeMembers;
 	}
 
 	/**
 	 *	Return the list of base members that will aggregate to the specified attribute 
 	 *  intersection for the specified base dimension. Component base member lists are 
 	 *  added to a collection so that they can be quickly recalled for future processing.
 	 *
 	 * @param dataCache Data cache
 	 * @param baseDimension Base dimension
 	 * @param attrDimensions Associated attribute dimensions
 	 * @param attrIs Attribute intersection
 	 * @param memberTrees Set of attribute and base member trees
 	 * 
 	 * @return List<String>
 	 */
 	public static List<String> getComponentBaseMembers(PafDataCache dataCache, final String baseDimension, final Set<String> attrDimensions, 
 			final Intersection attrIs, final MemberTreeSet memberTrees) {
 	
 	
 		// Initialization
 		List<String> componentMembers = null;
 		Set<String> validBaseMembers = new HashSet<String>();
 		PafBaseTree baseTree = (PafBaseTree) memberTrees.getTree(baseDimension);
 		String baseMember = attrIs.getCoordinate(baseDimension);		
 	
 		// Create an intersection containing the base member and it's associated attributes
 		// in the view section
 		int memberIsDimCount = attrDimensions.size() + 1;
 		String[] baseMemberDims = new String[memberIsDimCount];
 		String[] baseMemberCoords = new String[memberIsDimCount];
 		int i = 0;
 		for (String dsDimension:attrIs.getDimensions()) {
 			if (baseDimension.equalsIgnoreCase(dsDimension) || attrDimensions.contains(dsDimension)) {
 				baseMemberDims[i] = dsDimension;
 				baseMemberCoords[i] = attrIs.getCoordinate(dsDimension);
 				i++;
 			}
 		}
 		Intersection baseMemberIs = new Intersection(baseMemberDims, baseMemberCoords);
 	
		// Check for any unmapped attribute members (TTN-1893)
		if (i < memberIsDimCount) {
			return new ArrayList<String>();
		}
		
 		// Return pre-tabulated component member list, if it exists
 		componentMembers = dataCache.getComponentBaseMembers(baseMemberIs);
 		if (!componentMembers.isEmpty()) {
 			return componentMembers;
 		}
 	
 		// Find the intersection of associated base members for each attribute dimension
 		// in the data slice cache intersection
 		for (String attrDimension:attrDimensions) {
 	
 			// Get associated base member names of current attribute
 			String attrMember = attrIs.getCoordinate(attrDimension);
 			PafAttributeTree attrTree = (PafAttributeTree) memberTrees.getTree(attrDimension);
 			Set<String> associatedBaseMembers =  attrTree.getBaseMemberNames(attrMember);
 	
 			// If there are no base members then return empty set since this must be
 			// an invalid intersection of a base member with one or more attributes
 			if (associatedBaseMembers.isEmpty()) {
 				return new ArrayList<String>();
 			}
 	
 			// If 1st time through loop then initialize existing base members set
 			if (validBaseMembers.isEmpty()) {
 				validBaseMembers.addAll(associatedBaseMembers);
 			}
 	
 			// Get intersection of base members associated with each processed attribute
 			validBaseMembers.retainAll(associatedBaseMembers);
 	
 		}
 	
 		// Get base member descendants at attribute mapping level. It is assumed that
 		// all attribute dimensions on the view are mapped to the same level within
 		// a given base dimension.
 		int mappingLevel = baseTree.getAttributeMappingLevel((String)attrDimensions.toArray()[0]);
 		List<PafDimMember> dimMembers = baseTree.getMembersAtLevel(baseMember, (short) mappingLevel);
 		Set<String> intersectionDescendants = new HashSet<String>();
 		for (PafDimMember dimMember:dimMembers) {
 			intersectionDescendants.add(dimMember.getKey());
 		}
 	
 		// Filter list of potential valid base members against relevant base members for intersection
 		validBaseMembers.retainAll(intersectionDescendants);
 		componentMembers.addAll(validBaseMembers);
 	
 		// Add component base members to collection for future use
 		dataCache.addComponentBaseMembers(baseMemberIs, componentMembers);
 		
 	
 		// Return component base members
 		return componentMembers;
 	}
 
 
 	/**
 	 *	Return the list of base members along the specified base dimension, that aggregate to 
 	 *  the specified attribute intersection. All valid ancestor members are included as well. 
 	 *
 	 * @param dataCache Data cache
 	 * @param baseDimension Base dimension
 	 * @param attrDimensions Associated attribute dimensions
 	 * @param attrIs Attribute intersection
 	 * @param memberTrees Set of attribute and base member trees
 	 * 
 	 * @return List<String>
 	 */
 	public static List<String> getAllComponentBaseMembers(PafDataCache dataCache, final String baseDimension, final Set<String> attrDimensions, 
 			final Intersection attrIs, final MemberTreeSet memberTrees) {
 		
 		return getAllComponentBaseMembers(dataCache, baseDimension, attrDimensions, attrIs, memberTrees, false);
 	}
 	
 		
 	/**
 	 *	Return the list of base members along the specified base dimension, that aggregate to 
 	 *  the specified attribute intersection. All valid ancestor members are included as well. 
 	 *
 	 * @param dataCache Data cache
 	 * @param baseDimension Base dimension
 	 * @param attrDimensions Associated attribute dimensions
 	 * @param attrIs Attribute intersection
 	 * @param memberTrees Set of attribute and base member trees
 	 * @param bOmitPartialRollups Indicates that only ancestors whose descendants are all valid components should be returned
 	 * 
 	 * @return List<String>
 	 */
 	public static List<String> getAllComponentBaseMembers(PafDataCache dataCache, final String baseDimension, final Set<String> attrDimensions, 
 			final Intersection attrIs, final MemberTreeSet memberTrees, boolean bOmitPartialRollups) {
 	
 		
 		// Initialization
 		PafBaseTree baseTree = memberTrees.getBaseTree(baseDimension);
 		String baseMember = attrIs.getCoordinate(baseDimension);	
 		int mappingLevel = baseTree.getAttributeMappingLevel((String)attrDimensions.toArray()[0]);
 		Set<String> validAncestors = new HashSet<String>();
 		List<String> allDescendants = PafDimTree.getMemberNames(baseTree.getIDescendants(baseMember));
 
 		// Get component base members
 		List<String> allComponentMembers = new ArrayList<String>(getComponentBaseMembers(dataCache, baseDimension, attrDimensions, attrIs, memberTrees));
 		
 		// Merge in the all ancestors of each component member
 		for (String componentMember : allComponentMembers) {
 			List<String> ancestors = PafDimTree.getMemberNames(baseTree.getAncestors(componentMember));
 			if (!bOmitPartialRollups) {
 				// No filtering - include all ancestors
 				validAncestors.addAll(ancestors);
 			} else {
 				// Filtering - only include ancestors whose descendants are all valid components.
 				for (String ancestor : ancestors) {
 					// Get ancestor's descendants at the attribute mapping level. It is assumed
 					// that all attribute dimensions on the view are mapped to the same level
 					// within a given base dimension.
 					List<String> potentialComponents = PafDimTree.getMemberNames(baseTree.getMembersAtLevel(ancestor, (short) mappingLevel));
 					if (allComponentMembers.containsAll(potentialComponents)) {
 						validAncestors.add(ancestor);
 					}
 				}
 			}
 		}
 		
 		// Remove ancestors that are above current base member
 		validAncestors.retainAll(allDescendants);
 		
 		// Merge in valid ancestors
 		allComponentMembers.addAll(validAncestors);
 		
 		// Return full list of component members
 		return allComponentMembers;
 
 	}
 
 
 	/**
 	 * Return the coordinates of specified attribute intersection's descendant floor intersections
 	 * 
 	 * @param is Attribute intersection
 	 * @param dataCache Data Cache
 	 * 
 	 * @return List<Coordinates>
 	 */
 	public static List<Coordinates> buildAttrFloorCoordinates(Intersection is, PafDataCache dataCache) {
 
 		// Create the list of base dimensions that will be exploded as part of an attribute
 		// allocation. Currently, allocations are not performed over the measures dimension.		
 		StringOdometer isIterator = AttributeUtil.explodeAttributeIntersection(dataCache, is); 
 
 		// Build floor intersection coordinate list
 		List<Coordinates> floorCoords =  IntersectionUtil.buildCoordinates(isIterator, dataCache.getBaseDimensions());
 		
 	    // Translate time horizon coordinates back into regular time & year coordinates
 	    floorCoords = IntersectionUtil.translateTimeHorizonCoordinates(floorCoords, dataCache);
 
 		// Return the floor intersections
 		return floorCoords;
 	}
 
 
 	/**
 	 * Return the coordinates of specified attribute intersection's descendant floor intersections
 	 * 
 	 * @param is Attribute intersection
 	 * @param dataCache Data Cache
 	 * 
 	 * @return List of floor intersection coordinates
 	 */
 	public static List<String[]> buildAttrFloorIsCoords(Intersection is, PafDataCache dataCache) {
 
 		// Create the list of base dimensions that will be exploded as part of an attribute
 		// allocation. Currently, allocations are not performed over the measures dimension.		
 		StringOdometer isIterator = AttributeUtil.explodeAttributeIntersection(dataCache, is); 
 
 		// Build floor intersection coordinate list
 		List<String[]> floorIsCoordList =  IntersectionUtil.buildIsCoordList(isIterator, dataCache.getBaseDimensions());
 		
 	    // Translate time horizon coordinates back into regular time & year coordinates
 	    floorIsCoordList = IntersectionUtil.translateTimeHorizonCoords(floorIsCoordList, dataCache);
 
 		// Return the floor intersections
 		return floorIsCoordList;
 	}
 
 
 	/**
 	 * Return the specified attribute intersection's descendant floor intersections
 	 * 
 	 * @param is Attribute intersection
 	 * @param dataCache Data Cache
 	 * 
 	 * @return List of floor intersections
 	 */
 	public static List<Intersection> buildAttrFloorIntersections(Intersection is, PafDataCache dataCache) {
 
 
 		// Create the list of base dimensions that will be exploded as part of an attribute
 		// allocation. Currently, allocations are not performed over the measures dimension.		
 		StringOdometer isIterator = AttributeUtil.explodeAttributeIntersection(dataCache, is); 
 
 		// Build floor intersections
 		List<Intersection> floorIntersections =  IntersectionUtil.buildIntersections(isIterator, dataCache.getBaseDimensions());
 		
 	    // Translate time horizon coordinates back into regular time & year coordinates
 	    floorIntersections = IntersectionUtil.translateTimeHorizonIntersections(floorIntersections, dataCache);
 
 		// Return the floor intersections
 		return floorIntersections;
 	}
 
 
 	/**
 	 *	Return an iterator that will generate the corresponding descendant floor 
 	 *  base intersections for the specified attribute intersection.
 	 *  
 	 *  If there are no corresponding base intersections, then null is returned.
 	 *  
 	 *  NOTES: 
 	 *  
 	 *  1) Although similarly named, this method serves a different purpose 
 	 *  than the 'explodeAttributeInterection' method belonging to the 'EvalUtil'
 	 *  class. This current method is meant to do a 'generic' explosion of an
 	 *  attribute intersection, while its evaluation counterpart applies logic 
 	 *  specific to evaluation.
 	 *  
 	 *  2) Due to complexities with the potentially asymmetric relationship between
 	 *  the time and year hierarchies in a multi-year UOW, the returned iterator
 	 *  contains internal time horizon coordinates (ex. 'FY2006.Jan') instead of
 	 *  the time & year coordinates (ex. 'FY2006', 'Jan') that are used primarily 
 	 *  throughout this application. In most cases, the intersections generated by
 	 *  this iterator will need to be translated back to the time & year format.
 	 *
 	 * @param dataCache Data cache
 	 * @param attrIs Attribute intersection to explode
 	 * 
 	 * @return StringOdometer
 	 * @throws PafException 
 	 */
 	private static StringOdometer explodeAttributeIntersection(PafDataCache dataCache, final Intersection attrIs) {	
 		return explodeAttributeIntersection(dataCache, attrIs, AttrExplosionType.floorOnly);	
 	}
 
 	/**
 	 *	Return an iterator that will generate the corresponding descendant floor 
 	 *  base intersections for the specified attribute intersection.
 	 *  
 	 *  If there are no corresponding base intersections, then null is returned.
 	 *  
 	 *  NOTES: 
 	 *  
 	 *  1) Although similarly named, this method serves a different purpose 
 	 *  than the 'explodeAttributeInterection' method belonging to the 'EvalUtil'
 	 *  class. This current method is meant to do a 'generic' explosion of an
 	 *  attribute intersection, while its evaluation counterpart applies logic 
 	 *  specific to evaluation.
 	 *  
 	 *  2) Due to complexities with the potentially asymmetric relationship between
 	 *  the time and year hierarchies in a multi-year UOW, the returned iterator
 	 *  contains internal time horizon coordinates (ex. 'FY2006.Jan') instead of
 	 *  the time & year coordinates (ex. 'FY2006', 'Jan') that are used primarily 
 	 *  throughout this application. In most cases, the intersections generated by
 	 *  this iterator will need to be translated back to the time & year format.
 	 *
 	 * @param dataCache Data cache
 	 * @param attrIs Attribute intersection to explode
 	 * @param AttrExplosionType Indicates the type of intersection explosion being performed 
 	 * 
 	 * @return StringOdometer
 	 * @throws PafException 
 	 */
 	private static StringOdometer explodeAttributeIntersection(PafDataCache dataCache, final Intersection attrIs, 
 			final AttrExplosionType explosionType) {
 
 		Set<String> allBaseDims = new HashSet<String>(Arrays.asList(dataCache.getBaseDimensions()));
 		return explodeAttributeIntersection(dataCache, attrIs, allBaseDims, explosionType);
 
 	}
 		
 	/**
 	 *	Return an iterator that will generate the corresponding descendant base 
 	 *  intersections for the specified attribute intersection.
 	 *  
 	 *  If there are no corresponding base intersections, then null is returned.
 	 *  
 	 *  NOTES: 
 	 *  
 	 *  1) Although similarly named, this method serves a different purpose 
 	 *  than the 'explodeAttributeInterection' method belonging to the 'EvalUtil'
 	 *  class. This current method is meant to do a 'generic' explosion of an
 	 *  attribute intersection, while its evaluation counterpart applies logic 
 	 *  specific to evaluation.
 	 *  
 	 *  2) Due to complexities with the potentially asymmetric relationship between
 	 *  the time and year hierarchies in a multi-year UOW, the returned iterator
 	 *  contains internal time horizon coordinates (ex. 'FY2006.Jan') instead of
 	 *  the time & year coordinates (ex. 'FY2006', 'Jan') that are used primarily 
 	 *  throughout this application. In most cases, the intersections generated by
 	 *  this iterator will need to be translated back to the time & year format.
 	 *
 	 * @param dataCache Data cache
 	 * @param attrIs Attribute intersection to explode
 	 * @param explodedBaseDims Lists the base dimensions that should be exploded
 	 * @param AttrExplosionType Indicates the type of intersection explosion being performed 
 	 * 
 	 * @return StringOdometer
 	 * @throws PafException 
 	 */
 	private static StringOdometer explodeAttributeIntersection(PafDataCache dataCache, final Intersection attrIs, final Set<String> explodedBaseDims, final AttrExplosionType explosionType) {
 	
 		Map <String, List<String>> memberFilters = new HashMap<String, List<String>>();
 		final MemberTreeSet clientTrees = dataCache.getDimTrees();
 	    String timeDim = dataCache.getTimeDim(); 
 	    String yearDim = dataCache.getYearDim(); 
 		PafDimTree timeHorizonTree = clientTrees.getTree(dataCache.getTimeHorizonDim());			
 	
 		// Iterate through each base dimension in each attribute intersection and create
 		// the list of corresponding base members in the uow cache. For base dimensions
 		// without any corresponding attribute dimensions the current member is returned,
 		// unless the base dimension is included in the explodedBaseDims parameter. In 
 		// which case, the floor base members are returned.
 		final String[] baseDimensions = dataCache.getBaseDimensions();
 		int baseDimCount = baseDimensions.length;
 		
 		Set<String> isDims = new HashSet<String>(Arrays.asList(attrIs.getDimensions()));
 		for (int axisInx = 0; axisInx < baseDimCount; axisInx++) {   	
 
 			// Get current base member and tree
 			String baseDimension = baseDimensions[axisInx];
 			PafBaseTree pafBaseTree = clientTrees.getBaseTree(baseDimension);
 			String baseMember = attrIs.getCoordinate(baseDimension);
 			
 	       	// Time dimension - use time horizon tree
 	    	if (baseDimension.equals(timeDim)) {
 	    		String timeHorizCoord = TimeSlice.buildTimeHorizonCoord(baseMember, attrIs.getCoordinate(yearDim));
 	    		List<String> memberList = timeHorizonTree.getLowestMemberNames(timeHorizCoord);
 	    		memberFilters.put(baseDimension, memberList);
 	    		continue;
 	    	}
 	
 	    	// Year dimension - use time horizon default year member
 		    if (baseDimension.equals(yearDim)) {
 	    		List<String> memberList = Arrays.asList(new String[]{TimeSlice.getTimeHorizonYear()});
 	    		memberFilters.put(baseDimension, memberList);
 	    		continue;
 	    	}
 
 			// Get associated attribute dim names
 			Set<String> assocAttributes = new HashSet<String>();
 			assocAttributes.addAll(pafBaseTree.getAttributeDimNames());
 	
 			// Does this intersection contain any associated attributes for this base dimension
 			assocAttributes.retainAll(isDims);
 			if (assocAttributes.size() > 0) {
 				
 				// Yes - Add list of component base members to member filter
 				List<String> memberList;
 				if (explosionType == AttrExplosionType.floorOnly) {
 					memberList = AttributeUtil.getComponentBaseMembers(dataCache, baseDimension, assocAttributes, attrIs,
 							clientTrees);
 				} else {
 					boolean bOmitPartialRollups = false;
 					if (explosionType == AttrExplosionType.filteredDescendants) {
 						bOmitPartialRollups = true;
 					}
 					memberList = AttributeUtil.getAllComponentBaseMembers(dataCache, baseDimension, assocAttributes, attrIs,
 							clientTrees, bOmitPartialRollups);					
 				}
 				if (memberList.size() == 0) {
 					// No members were returned - this must be an invalid intersection - just return null
 					return null;
 				}
 				// Convert set of component base members to a list and add to member filter
 				// hash map.
 				memberFilters.put(baseDimension, memberList);
 				
 			} else {
 	
 				// No attribute dimensions
 				List<String> memberList = null;
 				if (explodedBaseDims != null && explodedBaseDims.contains(baseDimension)) {
 					// Base dimension explosion 
 					if (explosionType == AttrExplosionType.floorOnly) {
 						// Just pick lowest level descendants under member
 						memberList = pafBaseTree.getLowestMemberNames(baseMember);
 					} else {
 						// Get all descendants
 						memberList = PafDimTree.getMemberNames(pafBaseTree.getIDescendants(baseMember));
 					}
 				} else {
 					// No base dimension explosion - just add current base member to filter
 					memberList = new ArrayList<String>();
 					memberList.add(baseMember);
 				}	
 				
 				// Add selected base members to member filter
 				memberFilters.put(baseDimension, memberList);
 			}	
 		}
 	
 		// Return iterator
 		StringOdometer cacheIterator = new StringOdometer(memberFilters, baseDimensions);
 		return cacheIterator;
 	}
 
 }
