 /*
  *	File: @(#)F_CrossDim.java 		Package: com.pace.base.funcs 	Project: Paf Base Libraries
  *	Created: Jul 10, 2007  			By: AFarkas
  *	Version: x.xx
  *
  * 	Copyright (c) 2005-2007 Palladium Group, Inc. All rights reserved.
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
 package com.pace.base.funcs;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.Set;
 
 import org.apache.log4j.Logger;
 
 import com.pace.base.PafBaseConstants;
 import com.pace.base.PafException;
 import com.pace.base.app.MeasureDef;
 import com.pace.base.app.MeasureType;
 import com.pace.base.data.EvalUtil;
 import com.pace.base.data.IPafDataCache;
 import com.pace.base.data.Intersection;
 import com.pace.base.data.MemberTreeSet;
 import com.pace.base.mdb.PafDimMember;
 import com.pace.base.mdb.PafDimTree;
 import com.pace.base.state.IPafEvalState;
 import com.pace.base.state.PafClientState;
 import com.pace.base.utility.StringOdometer;
 
 /**
  * "CrossDim" Custom Function - Returns the value of the intersection represented by 
  * overriding one or more dimension members of the current data cache intersection with
  * the specfied dimension member override values.
  * 
  * The calling signature of this function is '@CrossDim(String Dim1, String Member1, String Dim2, 
  * 		String Member2, ......, String DimN, String MemberN)'.
  *
  * @version	x.xx
  * @author AFarkas
  *
  */
 public class F_CrossDim extends AbstractFunction {
 
 	private static Logger logger = Logger.getLogger(F_CrossDim.class);
 	
 	// These variables will hold pre-parsed information, for performance reasons
 	private Intersection overrideParms = null;
 	private List<String> functionDims = null;
 	
 	
 	public double calculate(Intersection sourceIs, IPafDataCache dataCache, IPafEvalState evalState) throws PafException {
 
 		// Get required arguments if not already parsed
 //		if (overrideParms == null) {
 		// Generate token catalog
 			Properties tokenCatalog = evalState.getClientState().generateTokenCatalog(new Properties());
 			overrideParms = parseCrossDimParms(sourceIs, evalState.getClientState(), tokenCatalog);
 //		}
 
 		// Clone current intersection
 		Intersection crossDimIs = sourceIs.clone();
 		
 		// Update coordinates with the dimension member override values
 		for (int i = 0; i < overrideParms.getDimensions().length; i++) {
 			crossDimIs.setCoordinate(overrideParms.getDimensions()[i], overrideParms.getCoordinates()[i]);
 		}
 		
 //		// Lock cross dim intersection so that any direct changes are preserved
 //		evalState.getOrigLockedCells().add(crossDimIs);
 //
 //		// Interesting things to try...
 //		evalState.getCurrentLockedCells().add(crossDimIs);
 //	    Collection<Intersection> cellsToLock = new HashSet<Intersection>(1);
 //	    cellsToLock.add(crossDimIs);
 //	    evalState.addAllAllocations(cellsToLock);
 		
 		
 		lockRecalcComps(sourceIs, crossDimIs, evalState);
 
 		// Return value of cross dimensional intersection
 		double result;
 		try {
 			result = dataCache.getCellValue(crossDimIs);
 		} catch (IllegalArgumentException iae) {
 			// Ignore invalid intersection error when attempting to pull an intersection
			// invalid combination of synthetic year root and time member coordinate.
			// (TTN-2025)
 			String yearCoord = crossDimIs.getCoordinate(dataCache.getYearAxis());
 			PafDimTree yearTree = evalState.getEvaluationTree(dataCache.getYearDim());
 			if (yearTree.getSyntheticMemberNames().contains(yearCoord)
 					&& !dataCache.hasValidTimeHorizonCoord(crossDimIs)) {
 				result = 0;
 			} else {
 				// Else, throw error
 				throw iae;
 			}
 		}
 		return result;
 	}
 
 
 	/**
 	 *	Parse function parameters
 	 *
 	 *	If sourceIs parameter is null then any set of parameters that use a member token
 	 *	will be skipped. This is necessary for trigger intersection processing.
 	 *
 	 * @param sourceIs Source intersection
 	 * @param clientState ClientState
 	 * @param tokenCatalog Token catalog
 	 *
 	 * @return Intersection
 	 */
 	private Intersection parseCrossDimParms(Intersection sourceIs, PafClientState clientState, Properties tokenCatalog) {
 		
 		try {
 			int dimCount = parms.length / 2;
 			functionDims = new ArrayList<String>();
 			List<String> dims = new ArrayList<String>(), members = new ArrayList<String>();
 			for (int parmInx = 0; parmInx < dimCount; parmInx++) {
 				
 				// Parse dimension and member specification string
 				String dim = parms[parmInx*2];
 				String memberSpec = parms[parmInx*2+1];
 				
 				// Skip this set of parameters if no source intersection was supplied and a 
 				// member token is used
 				if (sourceIs == null && memberSpec.startsWith(PafBaseConstants.FUNC_TOKEN_START_CHAR)) {
 					continue;
 				}
 				
 				// Add override parms
 				dims.add(dim);
 				String currMember = null;
 				if (sourceIs != null) {
 					currMember = sourceIs.getCoordinate(dim);
 				}
 				members.add(resolveMemberSpec(dim, memberSpec, clientState, currMember, tokenCatalog));
 			}
 			overrideParms = new Intersection(dims.toArray(new String[0]), members.toArray(new String[0]));
 			return overrideParms;
 			
 		} catch (RuntimeException e) {
 			// Check for an invalid number of parameters
 			String errMsg = null;
 			if (parms.length == 0 || parms.length % 2 != 0) {
 				errMsg = "@CrossDim signature error - an invalid number of parameters were specified";
 				logger.error(errMsg);
 				throw new IllegalArgumentException(errMsg);
 			} else {
 				throw e;
 			}
 
 		}
 	}
 
 
 	/**
 	 *  Resolve member specification
 	 *
 	 * @param dimension Dimension name
 	 * @param memberSpec Member specification
 	 * @param clientState ClientState
 	 * @param currMbrName Current intersection member
 	 * @param tokenCatalog Token catalog
 	 * 
 	 * @return Member name
 	 */
 	private String resolveMemberSpec(String dimension, String memberSpec, PafClientState clientState, String currMbrName, Properties tokenCatalog) {
 		
 		// If not a token, then just return member name (original membSpec value)
 		if (!memberSpec.startsWith(PafBaseConstants.FUNC_TOKEN_START_CHAR)) {
 			return memberSpec;
 		}
 		
 		String resolvedMemberSpec = null, key = null, validDim = null;
 		String timeDim = clientState.getMdbDef().getTimeDim();
 		String yearDim = clientState.getMdbDef().getYearDim();
 		
 
 		// Get dimension tree and current member
 		PafDimTree dimTree = clientState.getUowTrees().getTree(dimension);
 		PafDimMember currMember = dimTree.getMember(currMbrName);
 		
 		// Check for PARENT token
 		if (memberSpec.equalsIgnoreCase(PafBaseConstants.FUNC_TOKEN_PARENT)) {
 			if (currMember == dimTree.getRootNode()) {
 				// Return current member name if current member is root of tree
 				resolvedMemberSpec = currMember.getKey();
 			} else {
 				// Else return name of parent
 				resolvedMemberSpec = currMember.getParent().getKey();				
 			}
 			functionDims.add(dimension);
 			return resolvedMemberSpec;
 		}
 			
 		// Check for UOWROOT token
 		if (memberSpec.equalsIgnoreCase(PafBaseConstants.FUNC_TOKEN_UOWROOT)) {
 			// Return name of root node
 			resolvedMemberSpec = dimTree.getRootNode().getKey();
 			functionDims.add(dimension);
 			return resolvedMemberSpec;
 		}
 		
 		// Check for FIRST_PLAN_YEAR token (TTN-1597)
 		key = PafBaseConstants.FUNC_TOKEN_FIRST_PLAN_YEAR; 
 		validDim = yearDim;
 		if (memberSpec.equalsIgnoreCase(key)) {
 			if (dimension.equals(validDim)) {
 				// Return token value
 				resolvedMemberSpec = tokenCatalog.getProperty(key);
 				functionDims.add(dimension);
 				return resolvedMemberSpec;
 			} else {
 				// Return error - token only valid on year dimension
 				String errMsg = "@CrossDim signature error - the token: " + key
 						+ " is only valid on the [" + validDim + "] dimension";
 				logger.error(errMsg);
 				throw new IllegalArgumentException(errMsg);
 			}
 		}
 		
 		// Check for FIRST_NONPLAN_YEAR token (TTN-1597)
 		key = PafBaseConstants.FUNC_TOKEN_FIRST_NONPLAN_YEAR; 
 		validDim = yearDim;
 		if (memberSpec.equalsIgnoreCase(key)) {
 			if (dimension.equals(validDim)) {
 				// Return token value
 				resolvedMemberSpec = tokenCatalog.getProperty(key);
 				functionDims.add(dimension);
 				return resolvedMemberSpec;
 			} else {
 				// Return error - token only valid on year dimension
 				String errMsg = "@CrossDim signature error - the token: " + key
 						+ " is only valid on the [" + validDim + "] dimension";
 				logger.error(errMsg);
 				throw new IllegalArgumentException(errMsg);
 			}
 		}
 		
 		// Check for FIRST_PLAN_PERIOD token (TTN-1597)
 		key = PafBaseConstants.FUNC_TOKEN_FIRST_PLAN_PERIOD; 
 		validDim = timeDim;
 		if (memberSpec.equalsIgnoreCase(key)) {
 			if (dimension.equals(validDim)) {
 				// Return token value
 				resolvedMemberSpec = tokenCatalog.getProperty(key);
 				functionDims.add(dimension);
 				return resolvedMemberSpec;
 			} else {
 				// Return error - token only valid on year dimension
 				String errMsg = "@CrossDim signature error - the token: " + key
 						+ " is only valid on the [" + validDim + "] dimension";
 				logger.error(errMsg);
 				throw new IllegalArgumentException(errMsg);
 			}
 		}
 		
 		// Invalid token	
 		String errMsg = "@CrossDim signature error - invalid token specified";
 		logger.error(errMsg);
 		throw new IllegalArgumentException(errMsg);
 		
 	}
 
 
 	/* 
 	 * Return the set of intersections that might cause this function to need to re-evaulate
 	 * TODO Re-arrange parsing logic and trigger logic so that this function can be called without a current member
 	 */
 	public Set<Intersection> getTriggerIntersections(IPafEvalState evalState) throws PafException {
 
 		// Get required arguments if not already parsed
 //		if (overrideParms == null) {
 		// Generate token catalog
 			overrideParms = parseCrossDimParms(null, null, null);
 //		}
 		
 		Map<String, Set<String>> filterMap = new HashMap<String, Set<String>>(); 
 		List<String> floorDims = new ArrayList<String>();
 		String measureDim = evalState.getAppDef().getMdbDef().getMeasureDim();
 		String versionDim = evalState.getAppDef().getMdbDef().getVersionDim();
 
 		// Create dimension driven data items
 		for (int i = 0; i < overrideParms.getDimensions().length; i++) {
 			
 			// Create a member filter on each cross dimension			
 			String dim = overrideParms.getDimensions()[i];
 			Set<String>members = new HashSet<String>();
 			members.add(overrideParms.getCoordinates()[i]);
 			filterMap.put(dim, members);
 			
 			// Find the floor dimensions (ignore measures and version)
 			if (!dim.equalsIgnoreCase(measureDim) && !dim.equalsIgnoreCase(versionDim)) {
 				floorDims.add(dim);
 			}
 			
 		}
 
 		// Find the intersections that match the overridden dimensions passed to this function
 		Set<Intersection> matchingChangedCells = findIntersections(filterMap, evalState.getCurrentChangedCells(), evalState); 
 
 		
 		// Get the potential impact list for each matching intersection and add to trigger set
 		Set<Intersection> triggerSet = new HashSet<Intersection>(matchingChangedCells.size() * 2);
 		if (floorDims.isEmpty()) {
 			// No floor dimensions found, just send back matching changed cells
 			triggerSet.addAll(matchingChangedCells);
 		} else {
 			// Clone each matching changed intersection across the floor of each floor dimension
 			for (Intersection matchingChangedCell : matchingChangedCells) {
 				triggerSet.addAll(cloneIntersections(matchingChangedCell, floorDims, evalState));
 			}
 		}
 		
 		// Return set of trigger intersections
 		return triggerSet;
 	}
 
 	public boolean changeTriggersFormula(Intersection is, IPafEvalState evalState) {
 		return true;
 	}
 	
     /* (non-Javadoc)
      * @see com.pace.base.funcs.AbstractFunction#getMemberDependencyMap(com.pace.base.state.IPafEvalState)
      */
     public Map<String, Set<String>> getMemberDependencyMap(IPafEvalState evalState) throws PafException {
     	
     	Map<String, Set<String>> dependencyMap = new HashMap<String, Set<String>>();
     	
 		overrideParms = parseCrossDimParms(null, null, null);
 		
 		// Check each set of function parms
 		int dimCount = parms.length / 2;
 		functionDims = new ArrayList<String>();
 		for (int parmInx = 0; parmInx < dimCount; parmInx++) {
 
 			// Parse dimension and member specification string
 			String dim = parms[parmInx*2];
 			String memberSpec = parms[parmInx*2+1];
 
 			// If not a token, then just return member name (original membSpec value)
 			Set<String> members = null;
 			if (!memberSpec.startsWith(PafBaseConstants.FUNC_TOKEN_START_CHAR)) {
 				// If member is hard-coded, just add it to member map
 				members = new HashSet<String>();
 				members.add(memberSpec);
 			} else {
 				// Token is specified, add all dimension members
 				PafDimTree dimTree = evalState.getEvaluationTree(dim);
 				members = new HashSet<String>();
 				members.addAll(Arrays.asList(dimTree.getMemberKeys()));
 			}
 			dependencyMap.put(dim, members);
 		}
 
 		// Return member dependency map
 		return dependencyMap;
     	
     }
 
  	/**
 	 *  Clone the specified intersection across the floor of the specified dimensions
 	 *
 	 * @param intersection Intersection
 	 * @param floorDims Floor dimensions
 	 * @param evalState Evaluation state
 	 * 
 	 * @return ArrayList<Intersection>
 	 */
 	private ArrayList<Intersection> cloneIntersections(Intersection intersection, List<String> floorDims, IPafEvalState evalState) {
 
 		String[] allDims = intersection.getDimensions();
 		@SuppressWarnings("unchecked")
 		ArrayList<String>[] memberArrays = new ArrayList[allDims.length];
 		MemberTreeSet memberTrees = evalState.getClientState().getUowTrees();		// TTN-1595
 		
 
 		// Build member lists for clone process. 
 		// 
 		// Use original time tree and year trees, versus time horizon tree
 		// for intersection cloning. This is done to support the scenario 
 		// where a specified cross dimension is the time dimension or 
 		// the year dimension, but not necessarily both (TTN-1595)
 		int i = 0;
 		for (String dim:allDims) { 
 			ArrayList<String> memberList = new ArrayList<String>();
 			if (floorDims.contains(dim)) {
 				// Floor dimension - Member list equals dimension floor
 				PafDimTree dimTree = memberTrees.getTree(dim);						// TTN-1595
 				List<PafDimMember> floorMembers = dimTree.getLowestLevelMembers();
 				for (PafDimMember floorMember:floorMembers) {
 					memberList.add(floorMember.getKey());
 				}
 			} else {
 				// Else member list equals current coordinate
 				memberList.add(intersection.getCoordinate(dim));					
 			}
 			memberArrays[i++] = memberList;
 		}
 
 
 		// Clone Intersections
 		ArrayList<Intersection> clonedIntersections = new ArrayList<Intersection>();
 		StringOdometer odom = new StringOdometer(memberArrays);
 		Intersection clonedIntersection;
 		while (odom.hasNext()) {
 			clonedIntersection = new Intersection(allDims, odom.nextValue());		// TTN-1851
 			// Filter out any intersections whose time coordinates are not valid in the
 			// time horizon tree (TTN-1595).
 			if (EvalUtil.hasValidTimeCoord(clonedIntersection, evalState)) {
 				clonedIntersections.add(clonedIntersection);
 			}
 		}
 
 		return clonedIntersections;
 	}
 
 	private void lockRecalcComps(Intersection targetIs, Intersection crossDimIs, IPafEvalState evalState) {
 		
 	    Collection<Intersection> cellsToLock = new HashSet<Intersection>(1);
 	    int tCount = 0;
 	    String msrDim = evalState.getAppDef().getMdbDef().getMeasureDim();
 	   	Map<String, MeasureDef> measureCat = evalState.getAppDef().getMeasureDefs();
 	    MeasureDef msrDef = measureCat.get(crossDimIs.getCoordinate(msrDim));
 	    if (msrDef != null && msrDef.getType() == MeasureType.Recalc && evalState.getOrigLockedCells().contains(crossDimIs)) {
             cellsToLock.add(targetIs);
     	    evalState.getCurrentLockedCells().addAll(cellsToLock); 
     	    evalState.addAllAllocations(cellsToLock);
 	    }
 	}
 	
 }
