 /*
  *	File: @(#)ES_AllocateRatios.java 	Package: com.pace.base.eval 	Project: PafServer
  *	Created: Sep 6, 2005  				By: JWatkins
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
  --------		--------		-------			--------
  06/25/07		AFarkas							Removed locked period intersections from set
  												of ratio allocation targets
  * 
  */
 package com.pace.server.eval;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.SortedMap;
 
 import org.apache.log4j.Logger;
 
 import com.pace.base.PafException;
 import com.pace.base.SortOrder;
 import com.pace.base.app.*;
 import com.pace.base.data.EvalUtil;
 import com.pace.base.data.Intersection;
 import com.pace.base.data.MemberTreeSet;
 import com.pace.base.data.TimeSlice;
 import com.pace.base.mdb.AttributeUtil;
 import com.pace.base.mdb.PafDataCache;
 import com.pace.base.mdb.PafDimMember;
 import com.pace.base.mdb.PafDimTree;
 import com.pace.base.state.EvalState;
 import com.pace.base.view.PafMVS;
 import com.pace.base.view.PafViewSection;
 
 /**
  * Performs one step in an evaluation strategy.
  * Responsible for converting a user change into a ratio change
  * in specific change/lock scnearios.
  * 
  * Basically if a user changes and locks all intersections on 2 or more 
  * different levels, the change needs to get translated from the highest completely
  * locked level to the next highest completely locked level. In effect a user can create
  * a top down allocation by directly changing all members at a level
  * 
  *
  * @version	x.xx
  * @author JWatkins
  *
  */
 public class ES_AllocateRatios extends ES_AllocateBase implements IEvalStep {
 
 	@SuppressWarnings("unused")
 	private static final Logger logger = Logger.getLogger(ES_AllocateRatios.class);
 	private Set<Intersection> tempAllocLocks;
 
 
 	public void performEvaluation(EvalState evalState) throws PafException {
 
 //		long startTime = System.currentTimeMillis();
 //		long stepTime;
 		String currMemberName = null;
 		
 		String[] axisAllocSeq = evalState.getAxisAllocPriority();
 		String[] axisSortSeq = evalState.getAxisSortPriority();
		String[] dimSequence = evalState.getDimSequence();
 //		MemberTreeSet uowTrees = evalState.getClientState().getUowTrees();
 		PafDataCache dataCache = evalState.getDataCache();
 		PafDimMember currMember = null;
 		tempAllocLocks = new HashSet<Intersection>(evalState.getLoadFactor());
 		String msrDim = evalState.getMsrDim();
 //		String timeDim = evalState.getTimeDim();
 
 
 		// Sort locked intersections resulting from user locks and cell changes
 		Intersection[] lockList = EvalUtil.sortIntersectionsByAxis(evalState.getUserLocksAndChangedCells().toArray(new Intersection[0]), 
 				evalState.getClientState().getMemberIndexLists(), axisSortSeq, SortOrder.Ascending); 
 
 		// remove all intersections that are non aggregate or exist in elapsed periods
 		List<Intersection> tempList = new ArrayList<Intersection>();
 		String msrName;
 		MeasureDef msrDef;
 		String timeName;
 		
 		
 //        Set<String> lockedTimePeriods = null;
 //
 //        if (evalState.getPlanVersion().getType() == VersionType.ForwardPlannable) {
 //            lockedTimePeriods = evalState.getClientState().getLockedPeriods();
 //        }
 //        if (lockedTimePeriods == null)
 //            lockedTimePeriods = new HashSet<String>(0);  
 		
 		
 		
 		for (Intersection i: lockList) {
 			// check measure type for non aggregate or recalcs, they don't allocate
 			msrName = i.getCoordinate(msrDim);
 			msrDef = evalState.getAppDef().getMeasureDef(msrName);
 			if ( msrDef.getType().equals(MeasureType.NonAggregate) )
 				continue;
 
 //			if ( msrDef.getType().equals(MeasureType.Recalc) )
 //				continue;			
 			
 			// also eliminate if currently in an elapsed period
 //			timeName = i.getCoordinate(timeDim);
 //			if (lockedTimePeriods.contains(timeName))
 			if (EvalUtil.isElapsedIs(i, evalState))
 				continue;
 			
 			// good enough, add to finalist list
 			tempList.add(i);
 		}
 		
 		// Reassign pruned list to original collection
 		lockList = tempList.toArray(new Intersection[0]);
 		
 
 		// process in dimension priority sequence
 		for (String dim : axisAllocSeq ) {        	
 
 
 			// convenience variable to hold client tree for current dimension
 			PafDimTree dimTree = evalState.getEvaluationTree(dim);
 
 			// temporary allocation locks are dimensionally specific, and can be cleared 
 			// during each dimensional pass.
 			tempAllocLocks.clear();
 			
 			// check each locked intersection for ratio allocation scenario
 			// list must be processed in top down order in order to waterfall down the ratio allocations
 			lockList = EvalUtil.sortIntersectionsByAxis(tempList.toArray(new Intersection[0]), evalState.getClientState().getMemberIndexLists(), evalState.getAxisSortPriority(), SortOrder.Ascending);
 			List<String> targetNames = new ArrayList<String>();
 			
 			for (Intersection is : lockList ) {

				// Ignore any intersections that don't match the dimensionality of the underlying view section. 
				// These are most likely due to session lock processing. (TTN-1893)
				if (!Arrays.deepEquals(is.getDimensions(), dimSequence)) 
					continue;

 				// Get the member coordinate corresponding to the allocated dimension. Time
 				// dimension allocation requires special logic (TTN-1595).
 				currMemberName = EvalUtil.getIsCoord(is, dim, evalState);
 //				currMemberName = is.getCoordinate(dim);
				
 				currMember = dimTree.getMember(currMemberName);
 				
 				// if no children along this dimension, no possibility for a ratio allocation
 				targetNames.clear();
 				if (currMember.hasChildren()) {
 					
 					// traverse children, returning true if all legs of traversal result in a locked intersection.
 					if ( isRatioLock(dim, is, evalState, targetNames) )
 						performRatioAllocation(dim, is, targetNames, evalState, dataCache);
 					
 
 					// --------------------
 					// old way 
 					
 					// build generationTree for all the current member descendants					
 //					genTree = EvalUtil.buildGenTreeByName(dimTree.getDescendants(currMemberName), dimTree);
 //					checkTreeForRatioAllocation(dim, is, genTree, evalState, dataCache);
 					// --------------------
 
 				}
 			}
 						
 		}						
 
 		logEvalDetail(this, evalState, dataCache);
 
 //		if (logger.isDebugEnabled()) {
 //		logger.debug(LogUtil.timedStep("Allocating " + allocCells.length + " intersections", stepTime));   
 //		logger.debug(LogUtil.timedStep("Allocation step", startTime));
 //		}
 
 	}
 
 	
 	
 	/**
 	 * @param dim
 	 * @param is
 	 * @param evalState
 	 * @param targetsToAllocate
 	 * @return
 	 */
 	private boolean isRatioLock(String dim, Intersection is, EvalState evalState,
 			List<String> targetsToAllocate) {
 		// get subtree from client state rooted at test intersection
 //		String rootMbrName = is.getCoordinate(dim);
 		String rootMbrName = EvalUtil.getIsCoord(is, dim, evalState); // TTN-1595
 //		PafDimTree subTree = evalState.getClientState().getUowTrees().getTree(dim);
 		PafDimTree subTree = evalState.getEvaluationTree(dim);
 		PafDimMember rootMbr = subTree.getMember(rootMbrName);
 		List<String> targets = new ArrayList<String>();
 //		targetsToAllocate = new ArrayList<String>();
 		
 		// if all children of the target intersection represent locked paths than this is a ratio allocation
 		// assert: must have children or wouldn't even be here
 
 		// loop children looking for an open path
 		for (PafDimMember child: rootMbr.getChildren() ) {
 			if (! isLockedPath(child.getKey(), dim, is, evalState, targets ) )
 				return false;
 		}
 		
 		// made it through all children, all representing locked paths.
 		// append newTarget list and return true
 		targetsToAllocate.addAll(targets);
 		return true;
 
 	}
 	
 	
 	
 	/**
 	 * @param memberName
 	 * @param is
 	 * @param evalState
 	 * @param targets
 	 * @return
 	 */
 	private boolean isLockedPath(String memberName, String dim, Intersection rootIs, EvalState evalState, List<String> targets) {
 		
 		// requires
 		// member tree
 		PafDimTree dimTree = evalState.getClientState().getUowTrees().getTree(dim);
 		PafDimMember dimMbr = dimTree.getMember(memberName);
 		
 		// construct test intersection
 		Intersection testIs = rootIs.clone();
 		//testIs.setCoordinate(dim, memberName);
 		EvalUtil.setIsCoord(testIs, dim, memberName, evalState); 	// TTN-1595
 		
 		if ( evalState.getOrigLockedCells().contains(testIs) ) {
 			//this node is locked and represents a potential allocation target
 			// add to target lets, and return true, indicating it's locked
 			targets.add(memberName);
 			return true;
 		}
 		else if (dimMbr.hasChildren() ) {
 			// this node isn't locked, however check descendant paths
 			// requires a new temporary collection. Append if valid lock else discard
 			List<String> newTargets = new ArrayList<String>();
 
 			// loop children looking for an open path
 			for (PafDimMember child: dimMbr.getChildren() ) {
 				if (! isLockedPath(child.getKey(), dim, rootIs, evalState, newTargets ) )
 					return false;
 			}
 			// made it through all children, all representing locked paths.
 			// append newTarget list and return true
 			targets.addAll(newTargets);
 			return true;
 		}
 		else {
 			// no children and not locked.
 			// assert: not a ratio allocation if an open leaf descendant is reached
 			// return false
 			return false;
 		}
 	}
 	
 	
 	
 
 
 
 
 	/**
 	 *  Performs a ratio allocation over a specific set of targets. Generates a list of valid target intersections
 	 *  and then calls helper method to do the math.
 	 *
 	 * @param dim	dimension being operated on
 	 * @param intersection
 	 * @param evalState 
 	 * @param dataCache 
 	 * @param genTree 
 	 * @param tb 
 	 * 
 	 * @return PafUowCache
 	 * @throws PafException 
 	 */
 	public boolean checkTreeForRatioAllocation(String dim, Intersection is, Map<Integer, List<String>> genlvlTree,  EvalState evalState, PafDataCache dataCache ) throws PafException {
 
 		// convenience variable to hold client tree for current dimension
 		MemberTreeSet uowTrees = evalState.getClientState().getUowTrees();
 		PafDimTree dimTree = uowTrees.getTree(dim);
 		String baseDim = null;
 		String[] assocAttrDims = null;
 		Set<String> assocAttrDimSet = null, baseDims = dataService.getBaseDimNames();
 		boolean isBaseDim = false;
 		Set<String>viewAttributes = new HashSet<String>();
 
 		// set dimensional convenience variables 
 		if (evalState.isAttributeEval()) {
 			// attribute view
 			if (baseDims.contains(dim)) {
 				// base dimension
 				baseDim = dim;
 				isBaseDim = true;
 			} else {
 				// attribute dimension
 				baseDim = dataService.getAttributeTree(dim).getBaseDimName();
 				isBaseDim = false;
 			}
 
 			// Set associated attribute dimensions for current base dimension
 			assocAttrDimSet = new HashSet<String>(dataService.getBaseTree(baseDim).getAttributeDimNames());
 			assocAttrDimSet.retainAll(viewAttributes);
 			assocAttrDims = assocAttrDimSet.toArray(new String[0]);
 		} else {
 			// regular view / base dimension
 			isBaseDim = true;
 		}		
 
 
 
 		boolean isRatioAllocation = false;		
 		Intersection isTest = is.clone();
 		// examine each generation starting below the current intersection looking for an entire generation that is locked.
 		// all dimensionality is considered identical to the parent change, except for the dimension currently examined.
 		for ( int genlvl : genlvlTree.keySet() ) {
 
 			// inspect each member at the current generation
 			isRatioAllocation = true;
 			List<String> mbrNames = genlvlTree.get(genlvl);
 			for (String mbrName:mbrNames) {
 
 				// create test intersection
 				isTest.setCoordinate(dim, mbrName);
 
 				// attribute view special processing
 				if (evalState.isAttributeEval()) {
 
 					// if current dimension is either an attribute dimension or a base dimension with at least
 					// one attribute dimension on the view, skip any invalid attribute member intersections for 
 					// the current dimension, since they aren't updatable. 
 					if (!assocAttrDimSet.isEmpty()) {
 
 						// determine if the intersection containing the current generation member is a valid
 						// attribute intersection. (TTN-1851)
 						String baseMember = isTest.getCoordinate(baseDim);
 						String[] attrCombo = new String[assocAttrDims.length];
 						for (int i = 0; i < assocAttrDims.length; i++) {
 							attrCombo[i] = isTest.getCoordinate(assocAttrDims[i]);
 						}
 						boolean isValidAttrIs = AttributeUtil.isValidAttributeCombo(baseDim, baseMember, assocAttrDims, attrCombo, uowTrees);
 
 						// skip ratio allocation if member being tested doesn't exist in data cache
 						// but represents a valid attribute intersection on the selected generation.
 						if (!dataCache.isMember(dim, mbrName) && isValidAttrIs) {	// (TTN-1851)
 							isRatioAllocation = false;
 							break;
 						}        					
 
 						// if current gen member intersection is invalid then skip lock test  
 						if (!isValidAttrIs) {  	// (TTN-1851)
 							continue;
 						}
 					}
 				}
 
 				// as soon as an intersection at this generation is found that ISN'T 
 				// either changed or a locked period - exit loop
 				if (!evalState.getOrigLockedCells().contains(isTest)) {
 					isRatioAllocation = false;
 					break;
 				}
 			}
 			if (isRatioAllocation) {
 				performRatioAllocation(dim, is, mbrNames, evalState, dataCache );
 			}		
 		}		
 
 		return isRatioAllocation;
 
 	}
 
 
 
 
 	
 	/**
 	 *  Performs a ratio allocation over a specific set of targets. Generates a list of valid target intersections
 	 *  and then calls helper method to do the math.
 	 *
 	 * @param dim	dimension being operated on
 	 * @param intersection
 	 * @param evalState 
 	 * @param dataCache 
 	 * @param tb 
 	 * 
 	 * @return PafUowCache
 	 * @throws PafException 
 	 */
 	public PafDataCache performRatioAllocation(String dim, Intersection is, List<String> targetNames,  EvalState evalState, PafDataCache dataCache) throws PafException {
 
 		// This recreates a lot of convenience variables, unfortunately
 		// However I didn't want to increase the signature by 4 variables and this step occurs rarely
 		// Ideally these could be moved to the class level (but that has multi-threading implications) or
 		// into the evalState
 		
 		Set<String> assocAttrDims = null,  viewAttributes = new HashSet<String>();
 		MemberTreeSet uowTrees = evalState.getDataCacheTrees();
 		List<String> baseDims = uowTrees.getBaseDimensions();
 		String baseDim = null;
 
 		// set dimensional convenience variables 
 		if (evalState.isAttributeEval()) {
 			// attribute view
 			if (baseDims.contains(dim)) {
 				// base dimension
 				baseDim = dim;
 			} else {
 				// attribute dimension
 				baseDim = uowTrees.getAttributeTree(dim).getBaseDimName();
 			}
 
 			// Set associated attribute dimensions for current base dimension
 			assocAttrDims = new HashSet<String>(uowTrees.getBaseTree(baseDim).getAttributeDimNames());
 			assocAttrDims.retainAll(viewAttributes);
 		} else {
 		}
 
 
 		// generate target is list - exclude any invalid attribute intersections
 		List<Intersection> allocTargets = new ArrayList<Intersection>();
 		Intersection isTarget;
 		for (String mbrName : targetNames) {
 			isTarget = is.clone();
 			//isTarget.setCoordinate(dim, mbrName);
 			EvalUtil.setIsCoord(isTarget, dim, mbrName, evalState);		// TTN-1595
 			// add target to list if regular view or allocation dimension has not associated attributes on view
 			//	 or if target is a valid attribute intersection
 			if (!evalState.isAttributeEval() || assocAttrDims.isEmpty()) {
 				// regular view or allocation across base dimension without associated attributes - add target to list
 				allocTargets.add(isTarget);     							
 			} else {
 				// create intersection containing just the associated attribute values
 				// of the current target intersection
 				Intersection attrIs = new Intersection(assocAttrDims.toArray(new String[0]));
 				for (String attrDim:attrIs.getDimensions()) {
 					attrIs.setCoordinate(attrDim, isTarget.getCoordinate(attrDim));
 				}
 
 				// if attribute intersection is valid - add target to list
 //				String baseMember = isTarget.getCoordinate(baseDim);
 				String baseMember = EvalUtil.getIsCoord(isTarget, baseDim, evalState);		// TTN-1595
 
 				Set<Intersection> validAttrIntersections = AttributeUtil.getValidAttributeCombos(baseDim, baseMember, assocAttrDims.toArray(new String[0]), uowTrees);									
 				if (validAttrIntersections.contains(attrIs)) {
 					allocTargets.add(isTarget);
 				}
 			}
 		}
 		return allocateRatioChange( is, allocTargets, evalState, dataCache );
 	}
 
 
 
 	/**
 	 *  Special purpose method to allocate a change in the case of a "ratio allocation". All targets are locked
 	 *  and basically performs a shape allocation.
 	 *
 	 * @param intersection
 	 * @param evalState 
 	 * @param dataCache 
 	 * @param tb 
 	 * 
 	 * @return PafUowCache
 	 * @throws PafException 
 	 */
 	public PafDataCache allocateRatioChange(Intersection intersection, List<Intersection> targets,  EvalState evalState, PafDataCache dataCache) throws PafException {
 
 
 		String timeDim = evalState.getAppDef().getMdbDef().getTimeDim();
 		long stepTime = System.currentTimeMillis();
 
 
 		// allocation amount starts as full value of intersection to be allocated
 		double allocAvailable = dataCache.getCellValue(intersection);
         double absTargetSum = 0, allocDiff = 0;
 
 
 //       Set<String> lockedTimePeriods = null;
 
 //        if (evalState.getPlanVersion().getType() == VersionType.ForwardPlannable) {
 //            lockedTimePeriods = evalState.getClientState().getLockedPeriods();
 //       }
 //        if (lockedTimePeriods == null)
 //           lockedTimePeriods = new HashSet<String>(0);  
 		
 		
 		// total up locked and unlocked targets
 		// the only locks considered by this process are previous allocations within this pass, and elapsed periods
 		// which are not allowed to be changed under any circumstances
 		double lockedTargetTotal = 0; 
 		double origTargetSum = 0;
 		List<Intersection> elapsedLocks = new ArrayList<Intersection>();
 		for (Intersection target : targets ) {
 			if (tempAllocLocks.contains(target)) {
 				lockedTargetTotal += dataCache.getCellValue(target);
 			}
 //			else if (lockedTimePeriods.contains(target.getCoordinate(timeDim))) {
 			else if (EvalUtil.isElapsedIs(target, evalState)) {		// TTN-1595
 				lockedTargetTotal += dataCache.getCellValue(target);
 				elapsedLocks.add(target);
 			}
 			else {
 				origTargetSum += dataCache.getCellValue(target);
 			}
 		}    
 
 		// remove targets that are locked and their total from the amount to be allocated
 		targets.removeAll(this.tempAllocLocks);
 		targets.removeAll(elapsedLocks);		
 		allocAvailable -= lockedTargetTotal;
 
 
         // Allocation type pre-processing (TTN-1792)
         AllocType allocType = evalState.getClientState().getCurrentMsrRuleset().getAllocType();
         if (allocType == AllocType.AbsAlloc) {
         	// Get sum of target absolute values
         	for (Intersection target : targets) {
         		absTargetSum += Math.abs(dataCache.getCellValue(target));
         	}
     		allocDiff = allocAvailable - origTargetSum;
         }
         
 		// allocate balance of total by shape into remaining targets
 		double origValue = 0;
 		double allocValue = 0;
 		for (Intersection target : targets ) {
 
 			origValue = dataCache.getCellValue(target);
 
 			if (origTargetSum == 0) {                
 				allocValue = allocAvailable / targets.size();
 			}
 			else {
             	// Allocation type specific processing (TTN-1792)
             	switch(allocType) {
             		case SignedAlloc: allocValue = ((origValue / origTargetSum) * (allocAvailable));
             						break;
             		case AbsAlloc: allocValue = origValue + ((Math.abs(origValue) / absTargetSum) * (allocDiff));
             						break;
             		default:	String errMsg = "Unhandled Allocation Type: " + allocType.toString();
             					logger.error(errMsg);
             					throw new IllegalArgumentException(errMsg);
             	}
 			}
 
 			dataCache.setCellValue(target, allocValue);
 		}
 
 		// add these new allocations to the temporary bucket
 		tempAllocLocks.addAll(targets);
 
 		return dataCache;
 	} 
 
 
 }
