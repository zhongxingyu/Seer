 /*
  *	File: @(#)IntersectionUtil.java 	Package: com.pace.base.data 	Project: pace-base
  *	Created: Dec 18, 2012  				By: Alan Farkas
  *	Version: x.xx
  *
  * 	Copyright (c) 2005-2012 Alvarez and Marsal Software, LLC. All rights reserved.
  *
  *	This software is the confidential and proprietary information of A&M Software, LLC.
  *	("Confidential Information"). You shall not disclose such Confidential Information and 
  * 	should use it only in accordance with the terms of the license agreement you entered into
  *	with A&M Software, LLC.
  *
  *
  *
  Date			Author			Version			Changes
  xx/xx/xx		xxxxxxxx		x.xx			..............
  * 
  */
 package com.pace.base.data;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import com.pace.base.app.MdbDef;
 import com.pace.base.comm.SimpleCoordList;
 import com.pace.base.mdb.AttributeUtil;
 import com.pace.base.mdb.PafDataCache;
 import com.pace.base.mdb.PafDimMember;
 import com.pace.base.mdb.PafDimTree;
 import com.pace.base.state.IPafEvalState;
 import com.pace.base.utility.CollectionsUtil;
 import com.pace.base.utility.IOdometer;
 import com.pace.base.utility.StringOdometer;
 import com.pace.base.utility.StringUtils;
 
 /**
  * General purpose intersection-based utilities. 
  * 
  * If specific evaluation logic is required, the EvalUtil class should be used instead.
  *
  */
 public class IntersectionUtil {
 
 
 	/**
 	 * Return the coordinates of the ancestor intersections above the specified intersection
 	 * coordinates. 
 	 * 
 	 * (Attribute intersections are not supported at this time)
 	 * 
 	 * @param cords Intersection coordinates
 	 * @param dataCache Data cache
 	 * 
 	 * @return ArrayList of coordinates for each ancestor intersection
 	 */
 	public static List<String[]> buildAncestorIsCoords(String[] coords, PafDataCache dataCache) {
 
 		List<String[]> ancestorIsCoords = null;
 		if (dataCache.isBaseIntersection(coords)) {
 			ancestorIsCoords = buildBaseAncestorIsCoords(coords, dataCache);
 		} else {
 			//TODO Create AttributeUtil.buildAncestorIsCoords
 //			ancestorIsCoords = AttributeUtil.buildAncestorIsCoords(is, dataCache);
 			String errMsg = "Attribute intersections are not currently supported by 'buildAncestorIsCoords()'";
 			throw new IllegalArgumentException(errMsg);
 		}
 		return ancestorIsCoords;
 	}
 
 
 	/**
 	 * Return the coordinates of the ancestor intersections above the specified base intersection
 	 * coordinates. 
 	 * 
 	 * (Attribute intersections are not supported at this time)
 	 * 
 	 * @param cords Intersection coordinates
 	 * @param dataCache Data cache
 	 * 
 	 * @return ArrayList of coordinates for each ancestor intersection
 	 */
 	public static List<String[]> buildBaseAncestorIsCoords(String[] coords, PafDataCache dataCache) {
 
 		List<String[]> ancestorIsCoords = new ArrayList<String[]>();
 		Map<String, List<String>> memberListMap = new HashMap<String, List<String>>();
 	    final String timeDim = dataCache.getTimeDim(); 
 	    final String yearDim = dataCache.getYearDim(); 
 	    final int timeAxis = dataCache.getTimeAxis(), yearAxis = dataCache.getYearAxis();
 		final MemberTreeSet clientTrees = dataCache.getDimTrees();
 		final PafDimTree timeHorizonTree = clientTrees.getTree(dataCache.getTimeHorizonDim());
 		final String[] baseDims = dataCache.getBaseDimensions();
 		
 		// Collate the list of ancestors in each dimension. The original coordinate
 		// needs to be included as well, in order to get all ancestor intersections.
 		for (String dim : baseDims) {
 			
 			List<String> ancestorList = new ArrayList<String>();
 			
 	       	// Time dimension - use time horizon tree
 	    	if (dim.equals(timeDim)) {
 	    		String timeHorizCoord = TimeSlice.buildTimeHorizonCoord(coords, dataCache);
 	    		ancestorList.add(timeHorizCoord);
 	    		ancestorList.addAll(PafDimTree.getMemberNames(timeHorizonTree.getAncestors(timeHorizCoord)));
 	    		memberListMap.put(dim, ancestorList);
 	    		continue;
 	    	}
 	
 	    	// Year dimension - use time horizon default year member
 		    if (dim.equals(yearDim)) {
 		    	ancestorList.add(TimeSlice.getTimeHorizonYear());
 	    		memberListMap.put(dim, ancestorList);
 	    		continue;
 	    	}
 	    	
 	    	// All other dimensions - use corresponding tree to get the coordinate's ancestors
 			String coord = coords[dataCache.getAxisIndex(dim)];
 			PafDimTree dimTree = clientTrees.getTree(dim);
 			ancestorList.add(coord);
 			ancestorList.addAll(PafDimTree.getMemberNames(dimTree.getAncestors(coord)));
 			memberListMap.put(dim, ancestorList);
 		}
 		
 		// Build ancestor intersections
 		ancestorIsCoords = IntersectionUtil.buildIsCoordList(memberListMap, baseDims);
 		
 	    // Translate time horizon coordinates back into regular time & year coordinates
 	    ancestorIsCoords = translateTimeHorizonCoords(ancestorIsCoords, dataCache);
 	    
 	    // Remove original intersection from set of ancestors
 		ancestorIsCoords.remove(coords);
 		
 		// Return ancestor intersections
 		return ancestorIsCoords;
 	}
 
 
 	/**
 	 * Return the ancestor intersections above the specified intersection. 
 	 * 
 	 * (Attribute intersections are not supported at this time)
 	 * 
 	 * @param is Intersection
 	 * @param dataCache Data cache
 	 * 
 	 * @return ArrayList of ancestor intersections
 	 */
 	public static List<Intersection> buildAncestorIntersections(Intersection is, PafDataCache dataCache) {
 
 		List<Intersection> ancestorIntersections = null;
 		if (dataCache.isBaseIntersection(is)) {
 			ancestorIntersections = buildBaseAncestorIntersections(is, dataCache);
 		} else {
 			//TODO Create AttributeUtil.buildAncestorIntersections
 //			ancestorIntersections = AttributeUtil.buildAncestorIntersections(is, dataCache);
 			String errMsg = "Attribute intersections are not currently supported by 'buildAncestorIntersections()'";
 			throw new IllegalArgumentException(errMsg);
 		}
 		return ancestorIntersections;
 	}
 
 	
 	/**
 	 * Return the specified base intersection's ancestor intersections. 
 	 * 
 	 * @param is Base (non-attribute) intersection
 	 * @param dataCache Data Cache
 	 * 
 	 * @return List of ancestor intersections
 	 */
 	public static List<Intersection> buildBaseAncestorIntersections(Intersection is, PafDataCache dataCache) {
 		
 		List<Intersection> ancestorIntersections = new ArrayList<Intersection>();
 	    String timeDim = dataCache.getTimeDim(); 
 	    String yearDim = dataCache.getYearDim(); 
 		MemberTreeSet clientTrees = dataCache.getDimTrees();
 		PafDimTree timeHorizonTree = clientTrees.getTree(dataCache.getTimeHorizonDim());			
 		Map<String, List<String>> memberListMap = new HashMap<String, List<String>>();
 		
 		// Collate the list of ancestors in each dimension. The original coordinate
 		// needs to be included as well, in order to get all ancestor intersections.
 		String[] dimensions = is.getDimensions();
 		for (String dim : dimensions) {
 			
 			List<String> ancestorList = new ArrayList<String>();
 			
 	       	// Time dimension - use time horizon tree
 	    	if (dim.equals(timeDim)) {
 	    		String timeHorizCoord = TimeSlice.buildTimeHorizonCoord(is.getCoordinate(timeDim), is.getCoordinate(yearDim));
 	    		ancestorList.add(timeHorizCoord);
 	    		ancestorList.addAll(PafDimTree.getMemberNames(timeHorizonTree.getAncestors(timeHorizCoord)));
 	    		memberListMap.put(dim, ancestorList);
 	    		continue;
 	    	}
 	
 	    	// Year dimension - use time horizon default year member
 		    if (dim.equals(yearDim)) {
 		    	ancestorList.add(TimeSlice.getTimeHorizonYear());
 	    		memberListMap.put(dim, ancestorList);
 	    		continue;
 	    	}
 	    	
 	    	// All other dimensions - use corresponding tree to get the coordinate's ancestors
 			String coord = is.getCoordinate(dim);
 			PafDimTree dimTree = clientTrees.getTree(dim);
 			ancestorList.add(coord);
 			ancestorList.addAll(PafDimTree.getMemberNames(dimTree.getAncestors(coord)));
 			memberListMap.put(dim, ancestorList);
 		}
 		
 		// Build ancestor intersections
 		ancestorIntersections = IntersectionUtil.buildIntersections(memberListMap, dimensions);
 		
 	    // Translate time horizon coordinates back into regular time & year coordinates
 	    ancestorIntersections = translateTimeHorizonIntersections(ancestorIntersections, dataCache);
 	    
 	    // Remove original intersection from set of ancestors
 		ancestorIntersections.remove(is);
 		
 		// Return ancestor intersections
 		return ancestorIntersections;
 	}
 
 
 
 
 	/**
 	 * Return the coordinates of the floor intersections beneath the specified intersection
 	 * 
 	 * @param is Intersection
 	 * @param dataCache Data cache
 	 * 
 	 * @return ArrayList<Coordinates>
 	 */
 	public static List<Coordinates> buildFloorCoordinates(Intersection is, PafDataCache dataCache) {
 
 		List<Coordinates> floorCoords = null;
 		if (dataCache.isBaseIntersection(is)) {
 			floorCoords = buildBaseFloorCoordinates(is, dataCache);
 		} else {
 			floorCoords = AttributeUtil.buildAttrFloorCoordinates(is, dataCache);
 		}
 		return floorCoords;
 	}
 
 
 	/**
 	 * Return the coordinates of the floor intersections beneath the specified base intersection
 	 * 
 	 * @param is Base (non-attribute) intersection
 	 * @param dataCache Data Cache
 	 * 
 	 * @return List of floor intersections
 	 */
 	public static List<Coordinates> buildBaseFloorCoordinates(Intersection is, PafDataCache dataCache) {
 	
 	    // Build floor intersection coordinate list
 	    Map<String, List<String>> memberListMap = buildBaseFloorMemberMap(is, dataCache);
 	    List<Coordinates> floorCoords =  buildCoordinates(memberListMap, is.getDimensions());
 	    
 	    // Translate time horizon coordinates back into regular time & year coordinates
 	    floorCoords = translateTimeHorizonCoordinates(floorCoords, dataCache);
 	    
 	    // Return the floor intersections
 	    return floorCoords;
 	}
 
 
 	/**
 	 * Return the coordinates of the floor intersections beneath the specified intersection
 	 * 
 	 * @param is Intersection
 	 * @param dataCache Data cache
 	 * 
 	 * @return ArrayList of floor intersections coordinates
 	 */
 	public static List<String[]> buildFloorIsCoords(Intersection is, PafDataCache dataCache) {
 
 		List<String[]> floorIsCoords = null;
 		if (dataCache.isBaseIntersection(is)) {
 			floorIsCoords = buildBaseFloorIsCoords(is, dataCache);
 		} else {
 			floorIsCoords = AttributeUtil.buildAttrFloorIsCoords(is, dataCache);
 		}
 		return floorIsCoords;
 	}
 
 
 	/**
 	 * Return the coordinates of the floor intersections beneath the specified base intersection
 	 * 
 	 * @param is Base (non-attribute) intersection
 	 * @param dataCache Data Cache
 	 * 
 	 * @return List of floor intersections
 	 */
 	public static List<String[]> buildBaseFloorIsCoords(Intersection is, PafDataCache dataCache) {
 	
 	    // Build floor intersection coordinate list
 	    Map<String, List<String>> memberListMap = buildBaseFloorMemberMap(is, dataCache);
 	    List<String[]> floorIsCoords =  buildIsCoordList(memberListMap, is.getDimensions());
 	    
 	    // Translate time horizon coordinates back into regular time & year coordinates
 	    floorIsCoords = translateTimeHorizonCoords(floorIsCoords, dataCache);
 	    
 	    // Return the floor intersections
 	    return floorIsCoords;
 	}
 
 	/**
 	 * Return the coordinates of the floor intersections beneath the specified base intersection
 	 * 
 	 * @param coords Base (non-attribute) intersection coordinates
 	 * @param dataCache Data Cache
 	 * 
 	 * @return List of floor intersections
 	 */
 	public static List<String[]> buildBaseFloorIsCoords(String[] coords, PafDataCache dataCache) {
 	
 	    // Build floor intersection coordinate list
 	    Map<String, List<String>> memberListMap = buildBaseFloorMemberMap(coords, dataCache);
 	    List<String[]> floorIsCoords =  buildIsCoordList(memberListMap, dataCache.getBaseDimensions());
 	    
 	    // Translate time horizon coordinates back into regular time & year coordinates
 	    floorIsCoords = translateTimeHorizonCoords(floorIsCoords, dataCache);
 	    
 	    // Return the floor intersections
 	    return floorIsCoords;
 	}
 
 
 	/**
 	 * Return the floor intersections beneath the specified intersection
 	 * 
 	 * @param is Intersection
 	 * @param dataCache Data cache
 	 * 
 	 * @return ArrayList of floor intersections
 	 */
 	public static List<Intersection> buildFloorIntersections(Intersection is, PafDataCache dataCache) {
 		
 		List<Intersection> floorIntersections = null;
 		if (dataCache.isBaseIntersection(is)) {
 			floorIntersections = buildBaseFloorIntersections(is, dataCache);
 		} else {
 			floorIntersections = AttributeUtil.buildAttrFloorIntersections(is, dataCache);
 		}
 		return floorIntersections;
 		
 	}
 
 
 	/**
 	 * Return the specified base intersection's descendant floor intersections
 	 * 
 	 * @param is Base (non-attribute) intersection
 	 * @param dataCache Data Cache
 	 * 
 	 * @return List of floor intersections
 	 */
 	public static List<Intersection> buildBaseFloorIntersections(Intersection is, PafDataCache dataCache) {
 
 	    // Build floor intersections
 	    Map<String, List<String>> memberListMap = buildBaseFloorMemberMap(is, dataCache);
 	    List<Intersection> floorIntersections = buildIntersections(memberListMap, is.getDimensions());
 	    
 	    // Translate time horizon coordinates back into regular time & year coordinates
 	    floorIntersections = translateTimeHorizonIntersections(floorIntersections, dataCache);
 	    
 	    // Return the floor intersections
 	    return floorIntersections;
 	}
 
 
 	/**
 	 * Build the member map that represents the union of all coordinates representing the specified 
 	 * base intersection's floor intersections
 	 * 
 	 * @param is Base (non-attribute) intersection
 	 * @param dataCache Data Cache
 	 * 
 	 * @return Map of members by dimension
 	 */
 	private static Map<String, List<String>> buildBaseFloorMemberMap(Intersection is, PafDataCache dataCache) {
 		return buildBaseFloorMemberMap(is.getCoordinates(), dataCache);
 	}
 
 	/**
 	 * Build the member map that represents the union of all coordinates representing the specified 
 	 * base intersection's floor intersections
 	 * 
 	 * @param coords Base (non-attribute) intersection coordinates
 	 * @param dataCache Data Cache
 	 * 
 	 * @return Map of members by dimension
 	 */
 	private static Map<String, List<String>> buildBaseFloorMemberMap(String[] coords, PafDataCache dataCache) {
 
 		final MemberTreeSet clientTrees = dataCache.getDimTrees();
 	    final String timeDim = dataCache.getTimeDim(); 
 	    final String yearDim = dataCache.getYearDim(); 
 		final PafDimTree timeHorizonTree = clientTrees.getTree(dataCache.getTimeHorizonDim());	
 		final String[] baseDims = dataCache.getBaseDimensions();
 	    Map<String, List<String>> memberListMap = new HashMap<String, List<String>>();
 	    List<String> memberList;
 	    
 	     
 	    // Get floor members of each dimension. Use time horizon tree for time/year explosion.
 	    for (String dim : baseDims) {
 	
 	       	// Time dimension - use time horizon tree
 	    	if (dim.equals(timeDim)) {
 	    		String timeHorizCoord = TimeSlice.buildTimeHorizonCoord(coords, dataCache);
 	    		memberList = timeHorizonTree.getLowestMemberNames(timeHorizCoord);
 	    		memberListMap.put(dim, memberList);
 	    		continue;
 	    	}
 	
 	    	// Year dimension - use time horizon default year member
 		    if (dim.equals(yearDim)) {
 	    		memberList = Arrays.asList(new String[]{TimeSlice.getTimeHorizonYear()});
 	    		memberListMap.put(dim, memberList);
 	    		continue;
 	    	}
 	    	
 	    	// All other dimensions - just add the lowest members under branch. This 
 		    // tree method will return the member itself if it has no children.
 	    	PafDimTree tree = clientTrees.getTree(dim);
 	    	memberList = tree.getLowestMemberNames(coords[dataCache.getAxisIndex(dim)]);
 	     	memberListMap.put(dim, memberList);
 	    }
 	
 		return memberListMap;
 	}
 
 
 	/**
 	 *	Return an iterator that will generate the coordinates of the corresponding 
 	 *  descendant floor intersections for the specified base intersection.
 	 *  	 
 	 *  NOTE: The returned iterator will generate time horizon coordinates. These 
 	 *  must be converted back to time and year coordinates.
 	 * 
 	 * 
 	 * @param is Base (non-attribute) intersection
 	 * @param dataCache Data Cache
 	 * 
 	 * @return IOdometer
 	 */
 	public static IOdometer explodeBaseIntersection(Intersection is, PafDataCache dataCache) {
 	
 	    // Build floor intersections
 	    Map<String, List<String>> memberListMap = buildBaseFloorMemberMap(is, dataCache);
 	    IOdometer floorCoordIterator = new StringOdometer(memberListMap, is.getDimensions());
 	    return floorCoordIterator;
 	}
 
 
 	/**
 	 * Build a list of intersection coordinates that represents all possible combinations of the supplied member lists
 	 * 
 	 * @param memberLists Member lists by dimension
 	 * @param axisSequence Sorted list of dimensions
 	 * 
 	 * @return List<Coordinates>
 	 */
 	public static List<Coordinates> buildCoordinates(Map<String, List<String>> memberLists, String[] axisSequence) {
 
 		@SuppressWarnings("unchecked")
 		ArrayList<String>[] memberArrays = new ArrayList[memberLists.size()];
 		int i = 0;
 
 		for (String axis : axisSequence) {         
 			memberArrays[i++] = new ArrayList<String>(memberLists.get(axis));
 		}
 
 		StringOdometer odom = new StringOdometer(memberArrays);
 		List<Coordinates> coordinates = buildCoordinates(odom, axisSequence);
 
 		return coordinates;
 	}
 	
 	/**
 	 * Build a list of intersection coordinates using the supplied odometer to generate each intersection's coordinates
 	 * 
 	 * @param coordOdometer Odometer that will iterate through each set of intersection coordinates
 	 * @param dimensions The dimensions that will be used to construct each intersection
 	 * 
 	 * @return List of intersection coordinates
 	 */
 	public static List<Coordinates> buildCoordinates(StringOdometer coordOdometer, String[] dimensions) {
 		
 		List<Coordinates> isCoordList = new ArrayList<Coordinates>(coordOdometer.getCount());
 		while (coordOdometer.hasNext()) {
 			isCoordList.add(new Coordinates(coordOdometer.nextValue()));
 		}
 		
 		return isCoordList;
 	}
 
 	
 	/**
 	 * Build a list of intersection coordinates that represents all possible combinations of the supplied member lists
 	 * 
 	 * @param memberLists Member lists by dimension
 	 * @param axisSequence Sorted list of dimensions
 	 * 
 	 * @return List of intersection coordinates
 	 */
 	public static List<String[]> buildIsCoordList(Map<String, List<String>> memberLists, String[] axisSequence) {
 
 		@SuppressWarnings("unchecked")
 		ArrayList<String>[] memberArrays = new ArrayList[memberLists.size()];
 		int i = 0;
 
 		for (String axis : axisSequence) {         
 			memberArrays[i++] = new ArrayList<String>(memberLists.get(axis));
 		}
 
 		StringOdometer odom = new StringOdometer(memberArrays);
 		List<String[]> isCoordList = buildIsCoordList(odom, axisSequence);
 
 		return isCoordList;
 	}
 	
 
 	/**
 	 * Build a list of intersection coordinates using the supplied odometer to generate each intersection's coordinates
 	 * 
 	 * @param coordOdometer Odometer that will iterate through each set of intersection coordinates
 	 * @param dimensions The dimensions that will be used to construct each intersection
 	 * 
 	 * @return List of intersection coordinates
 	 */
 	public static List<String[]> buildIsCoordList(StringOdometer coordOdometer, String[] dimensions) {
 		
 		List<String[]> isCoordList = new ArrayList<String[]>(coordOdometer.getCount());
 		while (coordOdometer.hasNext()) {
 			isCoordList.add(coordOdometer.nextValue());
 		}
 		
 		return isCoordList;
 	}
 
 
 	/**
 	 * Build a list of intersections that represents all possible combinations of the supplied member lists
 	 * 
 	 * @param memberLists Member lists by dimension
 	 * @param axisSequence Sorted list of dimensions
 	 * 
 	 * @return List of intersections
 	 */
 	public static List<Intersection> buildIntersections(Map<String, List<String>> memberLists, String[] axisSequence) {
 
 		@SuppressWarnings("unchecked")
 		ArrayList<String>[] memberArrays = new ArrayList[memberLists.size()];
 		int i = 0;
 
 		for (String axis : axisSequence) {         
 			memberArrays[i++] = new ArrayList<String>(memberLists.get(axis));
 		}
 
 		StringOdometer odom = new StringOdometer(memberArrays);
 		List<Intersection> intersections = buildIntersections(odom, axisSequence);
 
 		return intersections;
 	}
 	
 	
 	/**
 	 * Build a list of intersections using the supplied odometer to generate each intersection's coordinates
 	 * 
 	 * @param coordOdometer Odometer that will iterate through each set of intersection coordinates
 	 * @param dimensions The dimensions that will be used to construct each intersection
 	 * 
 	 * @return List of intersections
 	 */
 	public static List<Intersection> buildIntersections(StringOdometer coordOdometer, String[] dimensions) {
 		
 		List<Intersection> intersections = new ArrayList<Intersection>(coordOdometer.getCount());
 		while (coordOdometer.hasNext()) {
 			Intersection is = new Intersection(dimensions, coordOdometer.nextValue());
 			intersections.add(is);
 		}
 		
 		return intersections;
 	}
 
 
 	/**
 	 * Return the intersection coordinate for the specified dimension that is appropriate
 	 * for traversal across the combined time/year time horizon. In the case of the time
 	 * dimension, the time horizon coordinate will be returned. In the case of the year 
 	 * dimension, the default time horizon year will be returned.
 	 * 
 	 * @param cellIs Cell intersection
 	 * @param dim Dimension name
 	 * @param dataCache Data cache
 	 * 
 	 * @return Intersection coordinate
 	 */
 	public static String getIsCoord(Intersection cellIs, String dim, PafDataCache dataCache) {	
 		return getIsCoord(cellIs.getCoordinates(), dim, dataCache);
 	}
 
 	/**
 	 * Return the intersection coordinate for the specified dimension that is appropriate
 	 * for traversal across the combined time/year time horizon. In the case of the time
 	 * dimension, the time horizon coordinate will be returned. In the case of the year 
 	 * dimension, the default time horizon year will be returned.
 	 * 
 	 * @param coords Cell intersection coordinates
 	 * @param dim Dimension name
 	 * @param dataCache Data cache
 	 * 
 	 * @return Intersection coordinate
 	 */
 	public static String getIsCoord(String[] coords, String dim, PafDataCache dataCache) {
 		
 		MdbDef mdbDef = dataCache.getMdbDef();
 		String timeHorizonDim = dataCache.getTimeHorizonDim();
 		String timeDim = dataCache.getTimeDim(), yearDim = mdbDef.getYearDim();
 		String coord = null;
 
 		if (dim.equals(timeDim) || dim.equals(timeHorizonDim)) {
 			coord = TimeSlice.buildTimeHorizonCoord(coords, dataCache);
 		} else if (dim.equals(yearDim)) {
 			coord = TimeSlice.getTimeHorizonYear();
 		} else {
 			coord = coords[dataCache.getAxisIndex(dim)]; 
 		}
 		return coord;
 	}
 
 
 	/**
 	 * Set the intersection coordinate for the specified dimension. In the case of the time
 	 * dimension, this method assumes that the time horizon coordinate is being set.
 	 *
 	 * @param coords Cell intersection coordinates
 	 * @param dim Dimension name
 	 * @param coord Intersection coordinate being set
 	 * @param dataCache Data cache
 	 * 
 	 */
 	public static void setIsCoord(String[] coords, String dim, String coord, PafDataCache dataCache) {
 
 		String timeHorizonDim = dataCache.getTimeHorizonDim();
 		String timeDim = dataCache.getTimeDim();
 
 		if (!dim.equals(timeDim) && !dim.equalsIgnoreCase(timeHorizonDim)) {
 			coords[dataCache.getAxisIndex(dim)] = coord; 
 		} else {
 			TimeSlice.applyTimeHorizonCoord(coords, coord, dataCache);
 		}
 	
 	}
 
 	/**
 	 * Set the intersection coordinate for the specified dimension. In the case of the time
 	 * dimension, this method assumes that the time horizon coordinate is being set.
 	 *
 	 * @param coords Cell intersection coordinates
 	 * @param dim Dimension name
 	 * @param coord Intersection coordinate being set
 	 * @param dataCache Data cache
 	 * 
 	 */
 	public static void setIsCoord(Coordinates coords, String dim, String coord, PafDataCache dataCache) {
 
 		String timeHorizonDim = dataCache.getTimeHorizonDim();
 		String timeDim = dataCache.getTimeDim();
 
 		if (!dim.equals(timeDim) && !dim.equalsIgnoreCase(timeHorizonDim)) {
 			coords.setCoordinate(dataCache.getAxisIndex(dim), coord); 
 		} else {
 			TimeSlice.applyTimeHorizonCoord(coords, coord, dataCache);
 		}
 	
 	}
 
 	/**
 	 * Set the intersection coordinate for the specified dimension. In the case of the time
 	 * dimension, this method assumes that the time horizon coordinate is being set.
 	 *
 	 * @param cellIs Cell intersection
 	 * @param dim Dimension name
 	 * @param coord Intersection coordinate
 	 * @param evalState Evaluation state
 	 * 
 	 */
 	public static void setIsCoord(Intersection cellIs, String dim, String coord, IPafEvalState evalState) {
 
 		String timeHorizonDim = evalState.getTimeHorizonDim();
 		String timeDim = evalState.getTimeDim();
 		MdbDef mdbDef = evalState.getAppDef().getMdbDef();
 
 		if (!dim.equals(timeDim) && !dim.equalsIgnoreCase(timeHorizonDim)) {
 			cellIs.setCoordinate(dim, coord); 
 		} else {
 			TimeSlice.applyTimeHorizonCoord(cellIs, coord, mdbDef);
 		}
 	
 	}
 
 
 	/**
 	 * Return the uow tree for the specified dimension that is appropriate for
 	 * traversal across the combined time/year time horizon. In the case of the time
 	 * dimension, the time horizon tree will be used. 
 	 * 
 	 * @param dim Dimension name
 	 * @param dataCache DataCache
 	 * 
 	 * @return Dim Tree
 	 */
 	public static PafDimTree getIsDimTree(String dim, PafDataCache dataCache) {
 
 		final String timeDim = dataCache.getTimeDim();
 		final String timeHorizDim = dataCache.getTimeHorizonDim();
 		final MemberTreeSet uowTrees = dataCache.getDimTrees();
 		PafDimTree dimTree = null;
 		
 		// Substitute the time horizon tree when the time dimension
 		// is specified, else return the specified dimension tree
 		if (!dim.equalsIgnoreCase(timeDim)) {
 			dimTree = uowTrees.getTree(dim);
 		} else {
 			dimTree =  uowTrees.getTree(timeHorizDim);
 		}
 
 		return dimTree;
 	}
 	
 
 	/**
 	 * Return the coordinates of any base intersections who should be locked as result each of their
 	 * children contained in the supplied set of locked intersections coordinates.
 	 * 
 	 * @param childCoordsSet Set of locked base intersection coordinates
 	 * @param dataCache Data cache
 	 * @param lockedCoordsSet Set of locked base intersection coordinates
 	 * 
 	 * @return Set of coordinates for each locked parent 
 	 */
 	public static Set<Coordinates> getLockedBaseParentCoords(final Set<Coordinates> childCoordsSet, final PafDataCache dataCache, Set<Coordinates> lockedCoordsSet) {
 		
 		final String yearDim = dataCache.getYearDim();
 		final String[] baseDims = dataCache.getBaseDimensions();
 		final int PARENT_COLLECTION_SIZE = childCoordsSet.size() / 10;
 		Set<Coordinates> lockedParentCoords = new HashSet<Coordinates>(PARENT_COLLECTION_SIZE);
 
 		// Check for potential parent locks along each base dimension
 		for (String dim : baseDims) {
 
 			// Skip year dimension as it will be referenced when processing the time dimension
 			if (dim.equals(yearDim))
 				continue;
 			
 			// Compile a set of unique parent intersections along this dimension
 			PafDimTree dimTree = getIsDimTree(dim, dataCache);
 			Set<Coordinates> uniqueParentCoords = new HashSet<Coordinates>(PARENT_COLLECTION_SIZE);
 			for (Coordinates childCoords : childCoordsSet) {
 
 				// Get the member coordinate information corresponding to the current dimension
 				String currMemberName = getIsCoord(childCoords.getCoordinates(), dim, dataCache);
 				PafDimMember currMember = dimTree.getMember(currMemberName);
 
 				// If parent intersection exists, add it to collection for subsequent processing
 				if (currMember.hasParent()) {
 					PafDimMember parentMember = currMember.getParent();
 					Coordinates parentCoords = childCoords.clone();
 					IntersectionUtil.setIsCoord(parentCoords.getCoordinates(), dim, parentMember.getKey(), dataCache);
 					uniqueParentCoords.add(parentCoords);
 				}
 			}
 			
 			// Check each parent intersection to see if it should be locked
 			for (Coordinates parentCoords : uniqueParentCoords) {
 
 				if (isLockedPath(parentCoords, dim, dimTree, dataCache, lockedCoordsSet)) {
 					lockedParentCoords.add(parentCoords);
 					lockedCoordsSet.add(parentCoords);
 				}
 			}
 		}
 		
 		
 		// Process the new parents
 		if (!lockedParentCoords.isEmpty()) {
 			Set<Coordinates> foundParents = getLockedBaseParentCoords(lockedParentCoords, dataCache, lockedCoordsSet);
 			lockedParentCoords.addAll(foundParents);		
 		}
 		
 		return lockedParentCoords;
 	}
 
 
 
 	/**
 	 * Determine if all children intersections against selected dimension are locked
 	 * 
 	 * @param parentCoords Parent intersection coordinates
 	 * @param dim Dimension
 	 * @param dimTree Dimension tree
 	 * @param dataCache Data cache
 	 * @param lockedCoordsSet Set of coordinates for existing locked floor intersections 
 	 * @return
 	 */
 	private static boolean isLockedPath(Coordinates parentCoords, String dim, PafDimTree dimTree, PafDataCache dataCache, Set<Coordinates> lockedCoordsSet) {
 
 		// Check if parent intersection is already locked
 		if (lockedCoordsSet.contains(parentCoords)) {
 			return true;
 		}
 				
 		// Perform a recursive traversal through all children to check if floor descendants along
 		// this dimension are locked.
 		String parent = getIsCoord(parentCoords.getCoordinates(), dim, dataCache);
 		PafDimMember parentMember = dimTree.getMember(parent);
 		if (parentMember.hasChildren()) {
 			Coordinates childCoords = parentCoords.clone();
 			for (PafDimMember child : parentMember.getChildren()) {
 				IntersectionUtil.setIsCoord(childCoords, dim, child.getKey(), dataCache);
 				if (!isLockedPath(childCoords, dim, dimTree, dataCache, lockedCoordsSet)) 
 					return false;
 			}
 			return true;
 		}
 		
 		// Intersection has no children and doesn't appear in locked set, therefore it should not
 		// be locked.
 		return false;
 	}
 
 
 	/**
 	 * Translate the time horizon coordinates, in the supplied list of intersection coordinates, into time/year coordinates
 	 * 
 	 * @param isCoordList List of data cache intersection coordinates
 	 * @param dataCache Data cache 
 	 * 
 	 * @return List of translated intersection coordinates
 	 */
 	public static List<String[]> translateTimeHorizonCoords(List<String[]> isCoordList, PafDataCache dataCache) {
 		
 	    // Convert time horizon intersections back to time/year intersections 
 	    int timeAxis = dataCache.getTimeAxis(), yearAxis = dataCache.getYearAxis();
 	    for (String[] coords : isCoordList) {
 	    	TimeSlice.translateTimeHorizonCoords(coords, timeAxis, yearAxis);
 	    }
 	    
 	    // Return translated intersections
 	    return isCoordList;
 	}
 
 
 	/**
 	 * Translate the time horizon coordinates, in the supplied list of intersection coordinates, into time/year coordinates
 	 * 
 	 * @param coordinatesList List of data cache intersection coordinates
 	 * @param dataCache Data cache 
 	 * 
 	 * @return List of translated intersection coordinates
 	 */
 	public static List<Coordinates> translateTimeHorizonCoordinates(List<Coordinates> coordinatesList, PafDataCache dataCache) {
 		
 	    // Convert time horizon intersections back to time/year intersections 
 	    int timeAxis = dataCache.getTimeAxis(), yearAxis = dataCache.getYearAxis();
 	    for (Coordinates coordinates : coordinatesList) {
 	    	TimeSlice.translateTimeHorizonCoords(coordinates, timeAxis, yearAxis);
 	    }
 	    
 	    // Return translated intersections
 	    return coordinatesList;
 	}
 
 	/**
 	 * Translate the time horizon coordinates, in the supplied list of intersections, into time/year coordinates
 	 * 
 	 * @param intersections List of data cache intersections
 	 * @param dataCache Data cache 
 	 * 
 	 * @return List of translated intersections
 	 */
 	public static List<Intersection> translateTimeHorizonIntersections(List<Intersection> intersections, PafDataCache dataCache) {
 		
 	    // Convert time horizon intersections back to time/year intersections 
 	    String timeDim = dataCache.getTimeDim(), yearDim = dataCache.getYearDim();
 	    for (Intersection is : intersections) {
 	    	TimeSlice.translateTimeHorizonCoords(is, timeDim, yearDim);
 	    }
 	    
 	    // Return translated intersections
 	    return intersections;
 	}
 
 
 	/**
 	 * Convert a collection of "like" intersections to a simple coordinate list
 	 * 
 	 * @param intersections List of intersections
 	 * @return SimpleCoordList
 	 */
 	public static SimpleCoordList convertIntersectionsToSimpleCoordList(Collection<Intersection> intersections) {
 		
 		boolean isFirstIs = true;
 		String[] dimensions = null;
 		List<String> coordList = new ArrayList<String>();
 		
 
 		// Iterate through all intersections and assemble all coordinates into a single list
 		for (Intersection intersection : intersections) {
 			
 			// Get list of dimensions (assume that all intersections have the same dimensionality)
 			if (isFirstIs) {
 				dimensions = intersection.getDimensions();
 				isFirstIs = false;
 			}
 			
 			// Get intersections coordinates
 			coordList.addAll(Arrays.asList(intersection.getCoordinates()));		
 		}
 
 		// Construct and return the SimpleCoordList
 		SimpleCoordList simpleCoordList = new SimpleCoordList(dimensions, coordList.toArray(new String[0]));
 		return simpleCoordList;
 	}
 
 
 	/**
 	 * Convert a list of "like" intersection coordinates to a simple coordinate list
 	 * 
 	 * @param dimensions Intersection dimensions
 	 * @param coordinatesList List of each intersection's coordinates
 	 * 
 	 * @return SimpleCoordList
 	 */
 	public static SimpleCoordList convertCoordinatesToSimpleCoordList(String[] dimensions, Collection<Coordinates> coordinatesList) {
 
 		List<String> coordList = new ArrayList<String>();
 		
		// Check for null or empty values
		if (dimensions == null || dimensions.length == 0) return null;
		if (coordinatesList == null || coordinatesList.isEmpty()) return null;
		
 		// Iterate through all intersections and assemble all coordinates into a single list
 		for (Coordinates coordinates : coordinatesList) {
 			coordList.addAll(Arrays.asList(coordinates.getCoordinates()));		
 		}
 
 		// Construct and return the SimpleCoordList
 		SimpleCoordList simpleCoordList = new SimpleCoordList(dimensions, coordList.toArray(new String[0]));
 		return simpleCoordList;
 	}
 
 
 	/**
 	 * Convert a list of "like" intersection coordinates to a simple coordinate list
 	 * 
 	 * @param dimensions Intersection dimensions
 	 * @param isCoordList List of each intersection's coordinates
 	 * 
 	 * @return SimpleCoordList
 	 */
 	public static SimpleCoordList convertIsCoordListToSimpleCoordList(String[] dimensions, Collection<String[]> isCoordsList) {
 
 		List<String> coordList = new ArrayList<String>();
 		
 		// Iterate through all intersections and assemble all coordinates into a single list
 		for (String[] coords : isCoordsList) {
 			coordList.addAll(Arrays.asList(coords));		
 		}
 
 		// Construct and return the SimpleCoordList
 		SimpleCoordList simpleCoordList = new SimpleCoordList(dimensions, coordList.toArray(new String[0]));
 		return simpleCoordList;
 	}
 
 
 	/**
 	 * Convert a simple coordinate list into a set of intersections
 	 * 
 	 * @param simpleCoordList Simple coordinate list
 	 * @return Intersection set
 	 */
 	public static Set<Intersection> convertSimpleCoordListToIntersectionSet(SimpleCoordList simpleCoordList) {
 
 		Set<Intersection> isSet = null;
 		
 		// Check for null coordinate list or components
 		if (simpleCoordList == null) return new HashSet<Intersection>();
         if (simpleCoordList.getCoordinates() == null) return new HashSet<Intersection>();
         if (simpleCoordList.getAxis() == null) return new HashSet<Intersection>();
 				
 		// Convert simple coordinate list
 		String[] dims = simpleCoordList.getAxis();
         int dimCount = simpleCoordList.getAxis().length, coordCount = simpleCoordList.getCoordCount();
 		int isCount = coordCount / dimCount;
         isSet = new HashSet<Intersection>(isCount);
         for (int i = 0; i < isCount; i++) {
             String[] coords = new String[dimCount];
             for (int j = 0; j < dimCount; j++) {
                 coords[j] = simpleCoordList.getCoordinates()[(i*dimCount)+j];
             }
             isSet.add(new Intersection(dims, coords));
          }
 		
 		// Return converted intersections
 		return isSet;
 	}
 
 	/**
 	 * Convert an array of simple coordinate list into a set of intersections
 	 * 
 	 * @param simpleCoordListAr Array of simple coordinate lists
 	 * @return Intersection set
 	 */
 	public static Set<Intersection> convertSimpleCoordListToIntersectionSet(SimpleCoordList[] simpleCoordListAr) {
 		
 		Set<Intersection> isSet = null;
 		int coordCount = 0;
 		
 		// Check for null array
 		if (simpleCoordListAr == null) return new HashSet<Intersection>();
 		
 		// Get a count of all coordinates and initialize return set
 		for (SimpleCoordList simpleCoordList : simpleCoordListAr) {
 			if (simpleCoordList != null && simpleCoordList.getAxis() !=null  && simpleCoordList.getCoordinates() != null) 
 				coordCount += simpleCoordList.getCoordCount() / simpleCoordList.getAxis().length;
 		}
 		isSet = new HashSet<Intersection>(coordCount);
 		
 		// Convert each simple coordinate list
 		for (SimpleCoordList simpleCoordList : simpleCoordListAr) {
 			isSet.addAll(convertSimpleCoordListToIntersectionSet(simpleCoordList));
 		}
 		
 		// Return converted intersections
 		return isSet;
 	}
 
 
 	
 }
