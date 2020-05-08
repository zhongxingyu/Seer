 /*
  *	File: @(#)PafDataSliceCacheCalc.java 	Package: com.pace.base.eval 	Project: PafServer
  *	Created: Feb 1, 2007  					By: AFarkas
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
 package com.pace.base.mdb;
 
 import java.text.DecimalFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.log4j.Logger;
 
 import com.pace.base.PafBaseConstants;
 import com.pace.base.PafErrSeverity;
 import com.pace.base.PafException;
 import com.pace.base.app.MdbDef;
 import com.pace.base.app.MeasureDef;
 import com.pace.base.app.MeasureType;
 import com.pace.base.app.PafApplicationDef;
 import com.pace.base.app.VarRptgFlag;
 import com.pace.base.app.VersionDef;
 import com.pace.base.app.VersionFormula;
 import com.pace.base.app.VersionType;
 import com.pace.base.app.VersionVarianceType;
 import com.pace.base.data.EvalUtil;
 import com.pace.base.data.Intersection;
 import com.pace.base.data.IntersectionUtil;
 import com.pace.base.data.MemberTreeSet;
 import com.pace.base.data.TimeSlice;
 import com.pace.base.funcs.IPafFunction;
 import com.pace.base.rules.Formula;
 import com.pace.base.rules.Rule;
 import com.pace.base.rules.RuleGroup;
 import com.pace.base.rules.RuleSet;
 import com.pace.base.state.EvalState;
 import com.pace.base.state.PafClientState;
 import com.pace.base.utility.LogUtil;
 import com.pace.base.utility.StringOdometer;
 import com.pace.base.utility.StringUtils;
 import com.pace.base.view.PafViewSection;
 
 
 
 
 /**
  * Manages all calculation operations against the PafDataSliceCache
  *
  * @version x.xx
  * @author AFarkas
  *
  */
 public abstract class PafDataCacheCalc {
 
 	private static Logger logger = Logger.getLogger(PafDataCacheCalc.class);
 	private static Logger performanceLogger = Logger.getLogger(PafBaseConstants.PERFORMANCE_LOGGER_EVAL);
 	private static Logger evalPerfLogger = Logger.getLogger(PafBaseConstants.PERFORMANCE_LOGGER_EVAL);
 
 
 	/**
 	 *	Aggregate data across the selected dimension. This is a convenience method
 	 * 	that calls aggDimension(aggDimension, dataCache, memberTree, memberFilters,
 	 * 	trackChanges) with the 'trackChanges' parameter set to 'DcChangeTrackOpt.APPEND'.
 	 *
 	 * @param aggDimension Name of dimension to aggregate
 	 * @param dataCache Paf data cache object
 	 * @param memberTree Hierarchy for aggregated dimension
 	 * 
 	 * @return Paf data cache object
 	 * @throws PafException 
 	 */
 	public static PafDataCache aggDimension(String aggDimension, PafDataCache dataCache, PafBaseTree memberTree) throws PafException {  	
 		return aggDimension(aggDimension, dataCache, memberTree, DcTrackChangeOpt.APPEND);
 	}
 
 
 	/**
 	 *	Aggregate data across the selected base dimension. This is a convenience method
 	 * 	that calls aggDimension(aggDimension, dataCache, memberTree, memberFilters, 
 	 * 	appendChangedCells) with the 'memberFilters' parameter set to null.
 	 *
 	 * @param aggDimension Name of dimension to aggregate
 	 * @param dataCache Paf data cache object
 	 * @param memberTree Hierarchy for aggregated dimension
 	 * @param trackChanges Data cache change tracking method
 	 * 
 	 * @return Paf data cache object
 	 * @throws PafException 
 	 */
 	public static PafDataCache aggDimension(String aggDimension, PafDataCache dataCache, PafBaseTree memberTree, DcTrackChangeOpt trackChanges) throws PafException {
 		return aggDimension(aggDimension, dataCache, memberTree, null, trackChanges);
 	}
 
 	/**
 	 *	Aggregate data across the selected base dimension. This is a convenience method
 	 * 	that calls aggDimension(aggDimension, dataCache, memberTree, memberFilters, 
 	 * 	trackChanges) with the 'trackChanges' parameter set to 'DcChangeTrackOpt.APPEND'.
 	 *
 	 * @param aggDimension Name of dimension to aggregate
 	 * @param dataCache Paf data cache object
 	 * @param memberTree Hierarchy for aggregated dimension
 	 * @param memberFilters Map of member lists, by dimension, that can be used to narrow the focus of the calculation process
 	 * @param trackChanges Data cache change tracking method
 	 * 
 	 * @return Paf data cache object
 	 * @throws PafException 
 	 */
 	public static PafDataCache aggDimension(String aggDimension, PafDataCache dataCache, PafBaseTree memberTree, Map<String, List<String>> memberFilters) throws PafException {
 		return aggDimension(aggDimension, dataCache, memberTree, memberFilters, DcTrackChangeOpt.APPEND);
 	}
 
 
 	/**
 	 *	Aggregate data across the selected base dimension, for the selected dimension member.
 	 *
 	 * @param aggDimension Name of dimension to aggregate
 	 * @param dataCache Paf data cache object
 	 * @param memberTree Hierarchy for aggregated dimension
 	 * @param memberFilter Map of member lists, by dimension, that can be used to narrow the focus of the calculation process
 	 * @param trackChanges Data cache change tracking method
 	 * 
 	 * @return Paf data cache object
 	 * @throws PafException 
 	 */
 	public static PafDataCache aggDimension(String aggDimension, PafDataCache dataCache, PafDimTree memberTree, Map<String, List<String>> memberFilter, DcTrackChangeOpt trackChanges) throws PafException {
 
 		boolean isTimeAggregation = false;
 		String measureDim = dataCache.getMeasureDim(), versionDim = dataCache.getVersionDim();
 		String timeDim = dataCache.getTimeDim(), yearDim = dataCache.getYearDim();
 		int timeAxis = dataCache.getAxisIndex(timeDim), yearAxis = dataCache.getAxisIndex(yearDim);
 		String[] dimensions = dataCache.getBaseDimensions();
 		String[] planVersions = dataCache.getPlanVersions();
 		List<String> aggMemberNames = null;
 		List<String> timeOpenPeriods = new ArrayList<String>();
 		Set<MeasureType> aggMeasureTypes = new HashSet<MeasureType>(Arrays.asList(new MeasureType[]{MeasureType.Aggregate, MeasureType.TimeBalFirst, MeasureType.TimeBalLast}));
 		Map<String, List<String>> aggFilter = new HashMap<String, List<String>>();
 		MdbDef mdbDef = dataCache.getAppDef().getMdbDef();
 		StringOdometer cellIterator = null;
 
 
 		// The fun starts now
 		logger.debug(String.format("Starting aggregation process for dimension [%s]", aggDimension)); 	
 
 		
 		// Clone member filter so that it can be updated and used to drive the aggregation
 		// process
 		if (memberFilter != null) {
 			aggFilter = new HashMap<String, List<String>>(memberFilter);
 		} else {
 			aggFilter = new HashMap<String, List<String>>();
 		}
 		
 		
 		// Initialize changed cells list
 		if (trackChanges == DcTrackChangeOpt.NONE) {
 			dataCache.initChangedCells();
 		}
 
 		// Check if this is a time dimension
 		if (aggDimension.equals(timeDim)) {
 			logger.debug("Aggregation of Time Dimension is detected. Time Balance processing will be applied to Time Balance members.");
 			isTimeAggregation = true;
 		}
 
 		// Add filter for the Version dimension if it's not the aggregated dimension
 		// and it doesn't already have a filter on it
 		if (!aggDimension.equalsIgnoreCase(versionDim)) {
 			if (!aggFilter.containsKey(versionDim)) {
 				aggFilter.put(versionDim, Arrays.asList(planVersions));
 			}
 		}
 
 
 		//BEGIN(1) - TTN-584
 		//if time aggregation and member filter contains time filter
 		if ( isTimeAggregation && aggFilter.containsKey(timeDim)) {
 			//get time open periods
 			if (aggFilter.get(timeDim) != null ) {
 				timeOpenPeriods = aggFilter.get(timeDim);
 			}
 			//remove time filter from map
 			aggFilter.remove(timeDim);
 
 		}
 		//END(1) - TTN-584
 
 		// Enter a list of filtered dimensions into the log
 		String[] filteredDims = new ArrayList<String>(aggFilter.keySet()).toArray(new String[0]);
 		logger.debug("Aggregation filters have been specified on the following dimensions: " + StringUtils.arrayToString(filteredDims));
 
 		// Validate aggregation filters
 		dataCache.validateMemberFilter(aggFilter);
 
 
 
 		// Main aggregation process - Aggregate the selected dimension across all 
 		// remaining dimensions. Only aggregate the following measure types: 
 		// Aggregate, TimeBalanceFirst, and TimeBalanceLast
 		logger.debug("Starting main aggregation process for dimension [" + aggDimension + "]....");
 
 
 		// Get list of members to aggregate - level 1  and above in Post Order, 
 		// so that all children are calculated before their parents.
 		logger.debug("Getting list of members to aggregate");
 		aggMemberNames = memberTree.getMemberNames(TreeTraversalOrder.POST_ORDER, 1);
 		
 		// Apply member filter to list of members to aggregate (TTN-1644).
 		List<String> filteredMembers = aggFilter.get(aggDimension);
 		if (filteredMembers != null && !filteredMembers.isEmpty()) {
 			aggMemberNames.retainAll(filteredMembers);
 		}
 		
 		// Modify the aggregation filter so that it can be used to generate all
 		// the cell intersections that need to be included in the aggregation
 		// process. 
 		//
 		// Since the aggregation dimension is being iterated in the outer
 		// loop, the filter on the aggregation dimension will be set to a
 		// single "dummy" member.
 		Map<String, List<String>> iteratorFilter = new HashMap<String, List<String>>(aggFilter);
 		iteratorFilter.put(aggDimension, Arrays.asList(new String[]{"[MEMBER]"}));
 			
 		// Initialize the intersection iterator. To reduce overhead, the iterator
 		// only gets created once, but will be reused for each aggregation member.
 		cellIterator = dataCache.getCellIterator(dimensions, iteratorFilter);
 
 		// Cycle through aggregation members
 		for (String aggMemberName : aggMemberNames) {
 
 			//BEGIN(2) - TTN-584 - Skip aggregation on elapsed periods
 			if ( isTimeAggregation && timeOpenPeriods.size() > 0 && !timeOpenPeriods.contains(aggMemberName)) {
 				logger.debug("Skipping aggregation on time member [" + aggMemberName + "]");
 				continue;
 			} else {
 				logger.debug("Aggregating member [" + aggMemberName + "]");
 			}
 			//END(2) - TTN-584
 
 
 			// Process the aggregation member only if it has children
 			PafDimMember aggMember = memberTree.getMember(aggMemberName);
 			List<PafDimMember> children = aggMember.getChildren();
 			if (children.size() > 0) {
 
 				// Cycle through all selected member intersections in the data cache
 				// across the member being aggregated. 
 				// 
 				// Each iterated intersection is a time horizon-based intersection, and
 				// with the exception of a time dimension aggregation, must be initially
 				// converted to a time/year-based intersection. 
 				//
 				// Time dimension aggregation, on the other hand, is handled a bit 
 				// differently. Since time aggregation occurs along the time horizon
 				// tree, each child intersection being aggregated is first converted 
 				// to a time horizon intersection. After the aggregation, the 
 				// iterated intersection is then converted back to a time/year
 				// based intersection, before being written back to the data cache.
 				//
 				// To reduce overhead, intersections are "translated" from  a time/year
 				// format to a time horizon format, and vice versa, instead of being 
 				// cloned (TTN-1595).
 				//
 				while (cellIterator.hasNext()) {
 					
 					// Get next member intersection
 					String[] coords = cellIterator.nextValue();		// TTN-1851
 					Intersection intersection = new Intersection(dimensions, coords);
 					intersection.setCoordinate(aggDimension, aggMemberName); 
 					
 					// Initialize aggregation total
 					double aggAmount = 0;
 
 					// Only process measures with valid aggregation types
 					String measure = intersection.getCoordinate(measureDim);
 					MeasureType measureType = dataCache.getMeasureType(measure);
 					if (aggMeasureTypes.contains(measureType) || (aggMember.isSynthetic() && measureType == MeasureType.NonAggregate)) {
 
 						// Aggregate children across selected member intersection. When aggregating 
 						// across the "Time" dimension, the aggregation process must properly aggregate 
 						// any measures set with the "Time Balance First" or "Time Balance Last" property.
 						if (!isTimeAggregation){
 
 							// Non-Time Aggregation - translate time horizon coordinate in aggregated 
 							// intersection to time year coordinates. 
 							TimeSlice.translateTimeHorizonCoords(coords, timeAxis, yearAxis);
 							
 							// Standard aggregation process - sum up children of selected member
 							for (PafDimMember child:children) {
 
 								intersection.setCoordinate(aggDimension, child.getKey());
 								double cellValue = dataCache.getCellValue(intersection);
 								aggAmount = aggAmount + cellValue;								
 							}
 							
 							// If aggregation member is synthetic and measure is non-aggregate then
 							// set the value to be the average of the children. (TTN-1644)
 							if (measureType == MeasureType.NonAggregate) {
 								aggAmount /= children.size();
 							}
 							
 							// Update aggregated member value (non-time aggregation)
 							intersection.setCoordinate(aggDimension, aggMemberName);
 							dataCache.setCellValue(intersection, aggAmount, trackChanges);
 
 						} else { 
 							// Time aggregation
 							if (measureType == MeasureType.Aggregate || (aggMember.isSynthetic() && measureType == MeasureType.NonAggregate)) {
 								// Standard aggregation process along time hierarchy (TTN-1595)
 								for (PafDimMember child:children) {
 									TimeSlice.applyTimeHorizonCoord(intersection, child.getKey(), mdbDef); 
 									double cellValue = dataCache.getCellValue(intersection);
 									aggAmount = aggAmount + cellValue;
 								}
 								// If aggregation member is synthetic and measure is non-aggregate then
 								// set the value to be the average of the children. (TTN-1644)
 								if (measureType == MeasureType.NonAggregate) {
 									aggAmount /= children.size();
 								}
 								
 							} else if (measureType == MeasureType.TimeBalFirst)  {
 								// Time Balance First - selected time dimension member equals it's first child
 								PafDimMember child = children.get(0);
 								TimeSlice.applyTimeHorizonCoord(intersection, child.getKey(), mdbDef); // TTN-1595
 								aggAmount = dataCache.getCellValue(intersection);												
 							} else if (measureType == MeasureType.TimeBalLast) {
 								// Time Balance Last - selected time dimension member equals it's last child
 								PafDimMember child = children.get(children.size() - 1);
 								TimeSlice.applyTimeHorizonCoord(intersection, child.getKey(), mdbDef); // TTN-1595
 								aggAmount = dataCache.getCellValue(intersection);	
 							} else {
 								// Invalid Measure Type - Throw IllegalArgumentException (should never get here)
 								String errMsg = "Agg Dimension error - invalid Measure Type of [" + measureType.toString() + "] encountered.";
 								logger.error(errMsg);
 								IllegalArgumentException iae = new IllegalArgumentException(errMsg);
 								throw iae;
 							}
 
 							// Update aggregated time member value
 							TimeSlice.applyTimeHorizonCoord(intersection, aggMemberName, mdbDef); 
 							dataCache.setCellValue(intersection, aggAmount, trackChanges);
 
 						} 
 					}
 						
 				}  // Next intersection
 			}	
 			cellIterator.reset();
 		} // Next aggregation member
 
 
 		// Return aggregated data cache
 		return dataCache;
 	}
 
 
 
 
 
 
 	/**
 	 *	Compute all attribute intersections. Any invalid intersections or intersections 
 	 *  containing derived calculations or non aggregate measures, are skipped.
 	 *
 	 *  This is a convenience method for calcAllAttributeIntersections(dataCache, evalState, clientState, memberFilters)
 	 *  where evalState has been set to null.
 	 *  
 	 * @param dataCache Data cache
 	 * @param clientState Client state object
 	 * @param memberMap Map of member lists by dimension that specify the intersections to calculate
 	 * @param trackChanges Data cache change tracking method
 	 * 
 	 * @return PafDatCache
 	 * @throws PafException 
 	 */
 
 	public static PafDataCache calcAttributeIntersections(PafDataCache dataCache, PafClientState clientState, 
 			Map<String, List<String>> memberMap, DcTrackChangeOpt trackChanges) throws PafException {
 		return calcAttributeIntersections(dataCache, null, clientState, memberMap, trackChanges);
 	}
 
 	/**
 	 *	Compute all attribute intersections required to support the data slice. Any invalid 
 	 *	intersections, or intersections containing derived versions, are skipped.
 	 *
 	 *  An optional member filter can be supplied, to narrow the intersections being calculated
 	 *  
 	 * @param dataCache Data cache
 	 * @param clientState Client state
 	 * @param sliceParms Data slice parms
 	 * @param memberFilter Optional Map of member lists by dimension that can narrow the focus of the calculations
 	 * @param trackChanges Data cache change tracking method
 	 *
 	 * @return PafDatCache
 	 * @throws PafException 
 	 */
 	public static PafDataCache calcAttributeIntersections(PafDataCache dataCache, PafClientState clientState, PafDataSliceParms sliceParms, 
 			Map<String, List<String>> memberFilter, DcTrackChangeOpt trackChanges) throws PafException {
 
 		PafViewSection viewSection = dataCache.getPafMVS().getViewSection();
 		String versionDim = dataCache.getVersionDim();
 		String[] dimensionOrder = viewSection.getDimensionsPriority();
 		Map<String, List<String>> memberListMap = sliceParms.buildUowSpec(dimensionOrder).buildMemberFilter();
 		List<String> dataSliceVersions = memberListMap.get(versionDim);
 		
 		// Copy in optional member filter lists
 		if (memberFilter != null) {
 			for (String dim : memberFilter.keySet()) {
 				memberListMap.put(dim, new ArrayList<String>(memberFilter.get(dim)));
 			}
 		}
 		
 		// Remove any derived versions and reference versions from member map
 		HashSet<String> versionsToCalc = new HashSet<String>(dataSliceVersions);
 		versionsToCalc.retainAll(dataCache.getBaseVersions());
 		
 		// Add in any off-screen base versions, that are components to
 		// any derived versions on the view.
 		List<String> dsDerivedVersions = new ArrayList<String>(dataSliceVersions);
 		dsDerivedVersions.retainAll(dataCache.getDerivedVersions());
 		versionsToCalc.addAll(dataCache.getComponentVersions(dsDerivedVersions));
 
 //		// Just calculate the current plan version
 //		HashSet<String> versionsToCalc = new HashSet<String>(Arrays.asList(dataCache.getPlanVersions()));
 		
 		// Calculate attributes
 		memberListMap.put(versionDim,  new ArrayList<String>(versionsToCalc));
 		return PafDataCacheCalc.calcAttributeIntersections(dataCache, clientState, memberListMap, trackChanges);
 	}
 
 	
 	/**
 	 *	Compute attribute intersections specified in member map. Any invalid intersections
 	 *  or intersections containing derived version calculations are skipped.
 	 *
 	 * @param dataCache Data cache
 	 * @param evalState Evaluation state object
 	 * @param clientState Client state object
 	 * @param memberMap Map of member lists by dimension that specify the intersections to calculate
 	 * @param trackChanges Data cache change tracking method
 	 * 
 	 * @return PafDataCache
 	 * @throws PafException 
 	 */
 	public static PafDataCache calcAttributeIntersections(PafDataCache dataCache, EvalState evalState, PafClientState clientState, 
 			Map<String, List<String>> memberMap, DcTrackChangeOpt trackChanges) throws PafException {
 
 		PafViewSection viewSection = dataCache.getPafMVS().getViewSection();
 		final String measureDim = dataCache.getMeasureDim();
 		final String[] attributeDims = viewSection.getAttributeDims();
 		final String[] viewDims = viewSection.getDimensionsPriority();
 		final int timeIndex = dataCache.getAxisIndex(dataCache.getTimeDim()), yearIndex = dataCache.getAxisIndex(dataCache.getYearDim());
 		final Set<Intersection> invalidViewIs = viewSection.invalidAttrIntersections();
 		Map<String, Set<Intersection>> recalcIsByMsr = new HashMap<String, Set<Intersection>>();
 		final MemberTreeSet memberTrees = clientState.getUowTrees();
 		final RuleSet ruleSet = clientState.getCurrentMsrRuleset();
 
 
 		// Initialize data cache change tracking collection
 		if (trackChanges == DcTrackChangeOpt.OVERWRITE) {
 			dataCache.initChangedCells();
 		}
 		
 		// Iterate through all attribute intersections that have been selected for calculation
 		StringOdometer cacheIterator = new StringOdometer(memberMap, viewDims);
 		while(cacheIterator.hasNext()) {
 
 			// Get next intersection and convert to a time/year based intersection
 			String[] coordinates = cacheIterator.nextValue();		// TTN-1851
 			TimeSlice.translateTimeHorizonCoords(coordinates, timeIndex, yearIndex);		
 			Intersection attrIs = new Intersection(viewDims, coordinates);
 
 			// Skip any invalid intersections. The view section's invalid intersection
 			// collection only contains invalid intersections that are visible on the 
 			// view. Therefore, it may be necessary to perform an additional validation
 			// to catch any attribute intersections that were originally in the view,
 			// but were located in rows or columns that were subsequently suppressed
 			// and removed from the view entirely.
 			if (invalidViewIs.contains(attrIs) || dataCache.isInvalidAttributeIntersection(attrIs, attributeDims)) {
 				continue;
 			}
 
 			// Skip any locked intersections
 			if (evalState != null && evalState.getCurrentLockedCells().contains(attrIs)) {
 				continue;
 			}
 
 			// Skip derived versions in case they're included in member filters
 			String version = attrIs.getCoordinate(dataCache.getVersionDim());
 			VersionDef versionDef = dataCache.getVersionDef(version);
 			if (PafBaseConstants.DERIVED_VERSION_TYPE_LIST.contains(versionDef.getType())) {
 				continue;
 			}
 
 			// Build recalc intersections into map based upon measure, for later processing
 			String measure = attrIs.getCoordinate(dataCache.getMeasureDim());
 			MeasureDef measureDef = dataCache.getMeasureDef(measure);
 			if (measureDef.getType() == MeasureType.Recalc ) {
 				if (!recalcIsByMsr.containsKey(measure)) recalcIsByMsr.put(measure, new HashSet<Intersection>());
 				recalcIsByMsr.get(measure).add(attrIs);
 				continue;
 			}
 
 			// Calculate Attribute Intersection
 			calcAttributeIntersection (dataCache, attrIs, measureDef.getType(), memberTrees, trackChanges);			
 		}
 
 
 		// 2nd pass to do recalcs. Dependent on aggregates being calculated, and processing 
 		// recalc measures in rule group sequence, so that dependent recalcs are also processed
 		if (!recalcIsByMsr.isEmpty()) {
 			if (evalState == null) {
 				evalState = new EvalState(clientState, dataCache);
 			}
 			for (RuleGroup rg : ruleSet.getRuleGroups()) {
 				for (Rule r : rg.getRules()) {
 					// if rule set measure is in list, then it's by definition a recalc
 					// and should be processed at this point in time.
 					String msrName = r.getFormula().getResultMeasure();
 					if (recalcIsByMsr.containsKey(msrName)) {
 						// iterate over intersections, calculating them
 						for (Intersection is : recalcIsByMsr.get(msrName)) {
 							//evalFormula(r.getFormula(), measureDim, is, dataCache, new EvalState(null, clientState, dataCache));
 							evalFormula(r.getFormula(), measureDim, is, dataCache, evalState);
 						}
 					}
 				}
 			}
 		}
 
 		// Return updated data slice cache
 		return dataCache;
 	}
 
 
 	// I'm dumping a copy of this method here for now...
 	// arguably this much business logic should be up in the server
 	private static void evalFormula(Formula formula, String axis,  Intersection srcIs, Intersection targetIs,  PafDataCache dataCache, EvalState evalState) throws PafException {
 
 		// get formula terms
 		String[] terms = formula.getExpressionTerms();
 		boolean[] funcFlags = formula.getFunctionTermFlags();
 		double[] values = new double[terms.length];
 		IPafFunction function = null;
 
 		//lookup each term
 		for (int i = 0; i < terms.length; i++) {
 			// funcflags indicate a complex function that must be evaluated differently
 			if (funcFlags[i]) {
 				function = formula.extractFunctionTerms()[i];
 				values[i] = function.calculate(srcIs, dataCache, evalState );
 			}
 			else {
 				Intersection isTerm = srcIs.clone();
 				isTerm.setCoordinate(axis, terms[i].trim());
 				values[i] = dataCache.getCellValue(isTerm);
 			}
 		}
 
 		double result = formula.evaluate(values);
 
 		// check for division by 0, which returns positive infinity, set to 0
 		if ( Double.isInfinite(result) || Double.isNaN(result) )
 			result = 0;
 
 		// update value in dataCache
 		dataCache.setCellValue(targetIs, result);
 
 	}
 
 	// convenience method for above function
 	private static void evalFormula(Formula formula, String axis, Intersection calcIs, PafDataCache dataCache, EvalState evalState) throws PafException {
 		evalFormula(formula, axis, calcIs, calcIs, dataCache, evalState);        
 	}	
 
 
 	/**
 	 *	Calculate a specific attribute intersection
 	 *
 	 * @param dataCache Data cache
 	 * @param attrIs Attribute intersection
 	 * @param measureType Measure type of intersection being calculated
 	 * @param memberTrees Collection of attribute and base trees corresponding to uow
 	 * 
 	 * @throws PafException 
 	 */
 	public static void calcAttributeIntersection(final PafDataCache dataCache, final Intersection attrIs, final MeasureType measureType, final MemberTreeSet memberTrees, DcTrackChangeOpt trackChanges) throws PafException {
 
 		// Some attribute intersections map directly to a base intersection. If this
 		// attribute intersection is an alias of a base intersection then, just 
 		// add the intersection to the data cache since no calculation is needed, 
 		// and exit method. 
 		List<AliasIntersectionType> aliasTypeProps = new ArrayList<AliasIntersectionType>();
 		if (dataCache.isAliasIntersection(attrIs, aliasTypeProps) 
 				&& aliasTypeProps.contains(AliasIntersectionType.BASE_IS)) {
 			Intersection baseIs = dataCache.generatePrimaryIntersection(attrIs, aliasTypeProps);
 			// To avoid created unneeded data blocks, only add attribute 
 			// intersection to data cache if its corresponding base
 			// intersection exists.
 			if (dataCache.isExistingIntersection(baseIs)) {
 				dataCache.addCell(attrIs);
 			}
 			return;
 		}
 		
 		
 		// Explode attribute intersection into corresponding base intersections. Iterator
 		// coordinates are in Time Horizon format (TTN-1597).
 		StringOdometer cacheIterator = EvalUtil.explodeAttributeIntersection(dataCache, attrIs, memberTrees);
 
 		// Exit if no intersections were found
 		if (cacheIterator == null) {
 			return;
 		}
 
 		// Aggregate descendant base intersection values located in data cache.
 		//
 		// When the intersection being calculated is a non-aggregate measure, 
 		// a cell count will be maintained that allows the aggregate total
 		// to be averaged over the number of existing descendants intersections.
 		double total = 0;
 		int cellCount = 0, timeAxis = dataCache.getTimeAxis(), yearAxis = dataCache.getYearAxis();
 		while(cacheIterator.hasNext()) {
 			
 			// Get next intersection coordinates
 			String[] coords = cacheIterator.nextValue();		// TTN-1851
 			
 		    // Translate time horizon coordinates back into regular time & year coordinates (TTN-1597)
 			TimeSlice.translateTimeHorizonCoords(coords, timeAxis, yearAxis);
 			
 			// Get base intersection's cell value add it to total. 
 			double cellValue = dataCache.getBaseCellValue(coords);
 			total += cellValue;
 
 			// Non-Aggregate measure - increment cell count only if intersections exists.
			if (measureType != MeasureType.NonAggregate) {
 				Intersection intersection = new Intersection(dataCache.getBaseDimensions(), coords);
 				if (dataCache.isExistingIntersection(intersection)) {
					total += cellValue;
 					cellCount++;
 				}
 			}
 		}
 
 		// Calculate the appropriate results based on the intersection measure type
 		double result = 0;
 		if (measureType != MeasureType.NonAggregate) {
 			//For aggregate members, simply return total
 			result = total;
 		} else {
 			// Non-aggregate measure - return average
 			result = total / cellCount;
 		}
 		
 		// Store results
 		dataCache.setCellValue(attrIs, result, trackChanges);
 
 	}
 
 	
 	/**
 	 *	Return the valid floor member combinations for the specified
 	 *  intersection, base dimensions and associated attributes. 
 	 *  
 	 *  If all the attributes for a given base dimension aren't mapped 
 	 *  to the same base member level, then an empty set is returned.
 	 * 
 	 * @param dataCache Data cache
 	 * @param attrIs Attribute intersection
 	 * @param baseDimNames Base dimension names
 	 * @param assocAttrMap Map of associated attribute dimensions by base dimension
 	 * @param uowTrees Collection of uow cache trees
 	 *
 	 * @return Set<Intersection>
 	 */
 	public static List<Intersection> getValidFloorMemberCombos(PafDataCache dataCache, Intersection attrIs, List<String> baseDimNames,
 			Map<String, List<String>> assocAttrMap, MemberTreeSet uowTrees) {
 
 		// Generate the valid base member combinations, across each base dimension,
 		// for the specified attribute values
 		List<String> memberComboDims = new ArrayList<String>(baseDimNames);
 		Map<String, List<String>> baseMemberMap = new HashMap<String, List<String>>();
 		for (String baseDim : baseDimNames) {
 			// Get list of valid base members for the current base dimension
 			Set<String> assocAttributes = new HashSet<String>(assocAttrMap.get(baseDim));
 			memberComboDims.addAll(assocAttributes);
 			List<String> baseMembers = AttributeUtil.getComponentBaseMembers(dataCache, baseDim, assocAttributes, attrIs, uowTrees);
 			baseMemberMap.put(baseDim, baseMembers);
 		}
 
 		// Generate the valid member combinations
 		StringOdometer odometer = new StringOdometer(baseMemberMap, baseDimNames.toArray(new String[0]));
 		List<Intersection> floorMemberCombos = new ArrayList<Intersection>();
 		while(odometer.hasNext()) {
 			String[] baseMembers = odometer.nextValue();		// TTN-1851
 			Intersection memberCombo = new Intersection(memberComboDims.toArray(new String[0]));
 			for (int i = 0; i < baseDimNames.size(); i++) {
 
 				// Set base member coordinate
 				String baseDim = baseDimNames.get(i); 
 				String baseMember = baseMembers[i];
 				memberCombo.setCoordinate(baseDim, baseMember);
 	
 				// Set associated attribute coordinates
 				PafBaseTree baseTree = (PafBaseTree) uowTrees.getTree(baseDim);
 				for (String attrDim : assocAttrMap.get(baseDim)) {
 					// getAttributeMembers will only return a single value in this case, 
 					// since baseMember is at the attribute mapping level.
 					String attrValue = baseTree.getAttributeMembers(baseMember, attrDim).toArray(new String[0])[0];
 					memberCombo.setCoordinate(attrDim, attrValue);
 				}
 			}
 			floorMemberCombos.add(memberCombo);
 		}
 		
 		return floorMemberCombos;
 	}
 
 	/**
 	 *	Calculate version dimension across all data intersections comprising derived
 	 *	versions. 
 	 *
 	 *	This is a convenience method that calls calcVersionDim(dataCache, null).
 	 *
 	 * @param dataCache Paf data cache object
 	 * @param memberTrees Collection of member trees
 	 * 
 	 * @return Paf data cache object
 	 * @throws PafException 
 	 */
 	public static PafDataCache calcVersionDim(PafDataCache dataCache, MemberTreeSet memberTrees) throws PafException {
 		return calcVersionDim(dataCache, new HashMap<String, List<String>>(), memberTrees); 	
 	}
 
 	/**
 	 *	Calculate the "version" dimension across the data set represented in a 
 	 *  view section, as defined by the view sections corresponding data slice parms
 	 *
 	 * @param dataCache Paf Data Cache
 	 * @param dataSliceParms Data Slice definition corresponding to a selected view section
 	 * @param memberTrees Collection of member trees
 	 *
 	 * @return Updated Paf Data Cache
 	 * @throws PafException 
 	 */
 	public static PafDataCache calcVersionDim(PafDataCache dataCache, PafDataSliceParms dataSliceParms, MemberTreeSet memberTrees) throws PafException {
 		return calcVersionDim(dataCache, dataSliceParms.buildUowSpec().buildMemberFilter(), memberTrees);					
 	}
 
 
 	/**
 	 *	Calculate version dimension
 	 *
 	 * @param dataCache Paf data cache object
 	 * @param memberFilter Map of member lists, by dimension, that can be used to narrow the focus of the calculation process
 	 * @param memberTrees Collection of member trees
 	 * 
 	 * @return Paf data cache object
 	 * @throws PafException 
 	 */
 	public static PafDataCache calcVersionDim(PafDataCache dataCache, final Map<String, List<String>> memberFilter, MemberTreeSet memberTrees) throws PafException {
 
 		long calcStart = 0, calcEnd = 0;
 		String versionDim = dataCache.getVersionDim();
 		String yearDim = dataCache.getYearDim();
 		String[] dimensions = dataCache.getPafMVS().getViewSection().getDimensionsPriority();
 		List<String> years = null;
 		Map<String, List<String>> updatedMemberFilter = new HashMap<String, List<String>>();
 		StringOdometer cellIterator = null;
 
 
 		logger.info(String.format("Calculating version dimension: [%s]", versionDim));
 		calcStart =  System.currentTimeMillis();
 
 
 		// Inspect member filter
 		if (memberFilter != null && memberFilter.size() > 0){
 			List<String> filteredDims = new ArrayList<String>(memberFilter.keySet());
 			logger.debug("Calculation filters have been specified on the following dimensions: "
 					+ StringUtils.arrayListToString(filteredDims)); 		
 
 			// Validate calculation filters
 			dataCache.validateMemberFilter(memberFilter);
 			
 			// Clone member filter
 			updatedMemberFilter = new HashMap<String, List<String>>(memberFilter);
 		}
 
 
 		// Modify the member filter so that it can be used to generate all
 		// the cell intersections that need to be included in the calculation
 		// process. This entails adding entries for any missing intersection
 		// dimensions.
 		//
 		// Since the year and version dimensions each have their own explicit
 		// loops, the filter on each of those dimensions will be set to a
 		// single "dummy" member.
 		updatedMemberFilter.put(versionDim, Arrays.asList(new String[]{"[MEMBER]"}));
 		updatedMemberFilter.put(yearDim, Arrays.asList(new String[]{"[MEMBER]"}));
 			
 		// Get the list of derived versions to calculate
 		List<String> derivedVersions = new ArrayList<String>();
 		List<String> versionFilter = memberFilter.get(versionDim);
 		if (versionFilter == null) {
 			// No versions specified - add all derived versions in data cache
 			derivedVersions.addAll(dataCache.getDerivedVersions());	
 		} else {
 			// Version filter specified - pull out all the derived ones
 			derivedVersions.addAll(versionFilter);
 			derivedVersions.retainAll(dataCache.getDerivedVersions());
 		}
 
 		// Exit if no derived versions were found
 		if (derivedVersions.size() == 0) {
 			logger.warn("Version calculation cancelled - no derived versions were found");
 			return dataCache;
 		} else {
 			logger.info("The following version members will be calculated: " + StringUtils.arrayListToString(derivedVersions));
 		}
 
 		
 		// Create the list of years to iterate
 		List<String> yearFilter = memberFilter.get(yearDim);
 		if (yearFilter == null) {
 			// No filter, use all year members in data cache
 			years = Arrays.asList(dataCache.getDimMembers(yearDim));
 		} else {
 			// Use supplied filter
 			years = yearFilter;
 		}
 
 
 		// Main calculation process - Calculate the Version dimension across all 
 		// other dimensions
 		logger.info("Starting main calcuation process for dimension [" + versionDim + "]....");
 		cellIterator = dataCache.getCellIterator(dimensions, updatedMemberFilter);
 		try {
 			// Cycle through years
 			for (String year : years){
 
 				// Cycle through each derived version
 				for (String version : derivedVersions) {
 					VersionType versionType = dataCache.getVersionType(version);
 					VersionFormula formula = dataCache.getVersionDef(version).getVersionFormula();
 					logger.debug("Calculating: Year [ " + year + "] - Version [" + version + "]"); 
 									
 					// Calculate version - cycle through each dimension intersection for current Year and Version
 					while (cellIterator.hasNext()) {
 
 						// Get next cell intersection
 						String[] coords = cellIterator.nextValue();		// TTN-1851
 						Intersection intersection = new Intersection(dimensions, coords);
 						intersection.setCoordinate(versionDim, version);
 						intersection.setCoordinate(yearDim, year);
 	
 						// Skip any invalid time intersections
 						if (!dataCache.hasValidTimeHorizonCoord(intersection)) {
 							continue;
 						}
 						
 						// Calculate version formula on current cell
 						double cellValue = 0;
 						switch (versionType) {
 						case ContribPct:
 							cellValue = calcContribPct(dataCache, intersection, formula, memberTrees);
 							break;
 						case Variance:
 							cellValue = calcVariance(dataCache, intersection, formula);
 							break;
 						default:
 							// Invalid versionType - throw IllegalArgumentException
 							String errMsg = "calcVersion error - invalid version type of ["
 								+ versionType.toString() + "] found on version [" + version + "]";
 						logger.error(errMsg);
 						IllegalArgumentException iae = new IllegalArgumentException(errMsg);
 						throw iae;
 						}
 
 						// Store results in data cache
 						dataCache.setCellValue(intersection, cellValue);	
 					}
 
 					// Reset the dimension intersection iterator
 					cellIterator.reset();
 				}     			
 			}
 
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
 
 		// Return calculated data cache
 		calcEnd = System.currentTimeMillis();
 		float calcElapsed = (float)(calcEnd - calcStart) / 1000;
 		DecimalFormat decimalFormat = new DecimalFormat("[#,##0.00]");
 		String formattedTime = decimalFormat.format(calcElapsed);
 		logger.info("Variance calculations completed in: " + formattedTime + " seconds") ;
 		return dataCache;
 	}
 
 
 	/**
 	 *	Calculate variance version
 	 *
 	 * @param dataCache Data cache
 	 * @param intersection Intersection being calculated
 	 * @param formula Version formula
 	 * 
 	 * @return Calculated cell value
 	 * @throws PafException 
 	 */
 	private static double calcVariance(PafDataCache dataCache, Intersection intersection, VersionFormula formula) throws PafException {
 
 		double calcedVariance = 0;
 		String measureDim = dataCache.getMeasureDim();
 		String versionDim = dataCache.getVersionDim();
 		VarRptgFlag varRptgFlag = null;
 
 
 		// Get value of base version intersection
 		Intersection baseIs = intersection.clone();
 		baseIs.setCoordinate(versionDim, formula.getBaseVersion());
 		double baseValue = dataCache.getCellValue(baseIs);
 
 		// Get value of compare version intersection
 		Intersection compareIs = intersection.clone();
 		compareIs.setCoordinate(versionDim, formula.getCompareVersion());
 		double compareValue = dataCache.getCellValue(compareIs);
 
 		// Get Variance Reporting Flag for selected measure
 		String measure = intersection.getCoordinate(measureDim);
 		try {
 			varRptgFlag = dataCache.getMeasureDef(measure).getVarRptgFlag();
 		} catch (RuntimeException e) {
 			// No Measure Def found for selected measure - use default value of Revenue Reporting Flag
 			varRptgFlag = VarRptgFlag.RevenueReporting;
 		}
 
 		
 		// Calculate simple variance based on measure's variance reporting flag
 		double simpleVariance;
 		if (varRptgFlag == VarRptgFlag.RevenueReporting) {
 			simpleVariance = baseValue - compareValue;
 		} else {
 			simpleVariance = compareValue - baseValue;
 		}
 
 		// Return a simple variance or percent variance based on variance type
 		if (formula.getVarianceType() == VersionVarianceType.SimpleVariance) {
 			calcedVariance = simpleVariance;
 		} else {
 			// Return percent variance
 			if (baseValue == 0 && compareValue == 0) {
 				// If both base and compare value are zero, then set the pct variance to zero
 				calcedVariance = 0;
 			} else if (compareValue == 0) {
 				// If just the compare value is zero, then pct variance = base value / abs(base value)	
 				calcedVariance = baseValue / Math.abs(baseValue);
 			} else {
 				// Else, pct variance = simple variance / abs(compare value)
 				calcedVariance = (simpleVariance / Math.abs(compareValue));
 			}
 		}
 
 		// Return calculated variance
 		return calcedVariance;
 	}
 
 
 	/**
 	 *	Calculate contribution percent version
 	 *
 	 * @param version Version dimension member
 	 * @param cellIndex Cell index corresponding to value being calculated
 	 * @param dataCache Paf data cache object
 	 * 
 	 * @return Calculated cell value
 	 * @throws PafException 
 	 */
 	private static double calcContribPct(PafDataCache dataCache, Intersection intersection, VersionFormula formula, MemberTreeSet memberTrees) throws PafException {
 
 		double cellValue = 0;
 		String versionDim = dataCache.getVersionDim();
 
 		
 		// Get value of base version intersection by cloning current intersection
 		// and setting the version to be the formula's base version
 		Intersection baseIs = intersection.clone();
 		baseIs.setCoordinate(versionDim, formula.getBaseVersion());
 		double baseValue = dataCache.getCellValue(baseIs);
 		
 		// Get value of comparison intersection - Start by cloning the base 
 		// intersection and updates the values of the dimension members 
 		// represented in the compare specification.
 		String[] compareDims = formula.getCompareIsDims();
 		String[] compareMemberSpecs = formula.getCompareIsMembers();
 		Intersection compareIs = baseIs.clone();
 		for (int i = 0; i < compareDims.length; i++) {
 			String compareDim = compareDims[i];
 			String currMember = intersection.getCoordinate(compareDim);
 			String compareMember = resolveMemberSpec(compareMemberSpecs[i], memberTrees.getTree(compareDim), currMember);
 			compareIs.setCoordinate(compareDim, compareMember);
 		}
 		double compareValue = dataCache.getCellValue(compareIs);
 
 		
 		// Compute cell value - return 0 if compare value is zero
 		if (compareValue != 0) {
 			cellValue = baseValue / compareValue;
 		}
 
 		return cellValue;
 
 	}
 
 
 	/**
 	 * 	Resolve member specification
 	 * 
 	 * @param memberSpec Member specification string
 	 * @param dimTree Dimension tree
 	 * @param currMbrName Current intersection member name
 	 * 
 	 * @return member name
 	 */
 	public static String resolveMemberSpec(String memberSpec, PafDimTree dimTree, String currMbrName) {
 
 		String resolvedMemberSpec = null;
 		
 		// If not a token, then just return member name (original membSpec value)
 		if (!memberSpec.startsWith("@")) {
 			return memberSpec;
 		}
 		
 		// Get current member
 		PafDimMember currMember = dimTree.getMember(currMbrName);
 		
 		// Check for PARENT token
 		if (memberSpec.equalsIgnoreCase(PafBaseConstants.VF_TOKEN_PARENT)) {
 			if (currMember == dimTree.getRootNode()) {
 				// Return current member name if current member is root of tree
 				resolvedMemberSpec = currMember.getKey();
 			} else {
 				// Else return name of parent
 				resolvedMemberSpec = currMember.getParent().getKey();				
 			}
 			return resolvedMemberSpec;
 		}
 			
 		// Check for TOTAL token
 		if (memberSpec.equalsIgnoreCase(PafBaseConstants.VF_TOKEN_UOWROOT)) {
 			// Return name of root node
 			resolvedMemberSpec = dimTree.getRootNode().getKey();
 			return resolvedMemberSpec;
 		}
 		
 		// Invalid member spec token
 		String errMsg = "Invalid member token in version formula";
 		logger.error(errMsg);
 		throw new IllegalArgumentException(errMsg);
 		
 	}
 
 	/**
 	 *  Build contribution percent formula basis intersection
 	 *
 	 * @param is Changed intersection
 	 * @param evalState Evaluation state
 	 * 
 	 * @return Changed intersection
 	 */
 	public static Intersection buildContribPctBasisIs (Intersection is, EvalState evalState) {
 
 		String versionDim = evalState.getVersionDim();
 		PafApplicationDef appDef = evalState.getAppDef();
 		MemberTreeSet uowTrees = evalState.getClientState().getUowTrees();
 
 
 		// Get version info
 		String version = is.getCoordinate(versionDim);
 		VersionDef vd = appDef.getVersionDef(version);
 		VersionFormula formula = vd.getVersionFormula();
 
 		// Build basis intersection. Start with clone of changed intersection, overriding
 		// the version member with the formula base version. Next override any of the
 		// dimension member specifications that are contained in the comparison definition.
 		Intersection basisIs = is.clone();
 		basisIs.setCoordinate(versionDim, formula.getBaseVersion());
 		String[] basisDims = formula.getCompareIsDims();
 		String[] basisMembers = formula.getCompareIsMembers();
 		for (int i = 0; i < basisDims.length; i++) {
 			String dim = basisDims[i];
 			String currMember = basisIs.getCoordinate(dim);
 			PafDimTree dimTree = uowTrees.getTree(dim);
 			String basisMember = PafDataCacheCalc.resolveMemberSpec(basisMembers[i], dimTree, currMember);
 			basisIs.setCoordinate(dim, basisMember);
 		}
 
 		// Return basis intersection
 		return basisIs;
 
 	}
 
 
 	/**
 	 *	Calculate synthetic member intersections
 	 *
 	 * @param clientState Client state object
 	 * @param dataCache Data cache
 	 * @param dataSpecByVersion Specifies the intersections to calculate over, by version
 	 * 
 	 * @throws PafException
 	 */
 	public static void calculateSyntheticMembers(PafClientState clientState, PafDataCache dataCache, Map<String, Map<Integer, List<String>>> dataSpecByVersion) throws PafException {
 
 		long startTime = System.currentTimeMillis();
 		int timeAxis = dataCache.getTimeAxis(), yearAxis = dataCache.getYearAxis();
 		String activePlanVersion = clientState.getPlanningVersion().getName();
 		String measureDim = dataCache.getMeasureDim(), timeDim = dataCache.getTimeDim();
 		String timeHorizonDim = dataCache.getTimeHorizonDim();
 		String versionDim = dataCache.getVersionDim(), yearDim = dataCache.getYearDim();
 		String logMsg = null;
 		String[] hierDims = clientState.getApp().getMdbDef().getHierDims();
 		String[] baseDims = dataCache.getBaseDimensions();
 		List<String> recalcMeasures = dataCache.getRecalcMeasures();
 		Set<String> versions = new HashSet<String>();
 		Map<String, List<String>> timeFilter = new HashMap<String, List<String>>(), baseDimCalcFilter = null;
 		MemberTreeSet uowTrees = clientState.getUowTrees();
 		PafDimTree timeHorizonTree = uowTrees.getTree(timeHorizonDim);
 		List<String> defEvalVersions = new ArrayList<String>();
 
 		//TODO TTN-1644 Refactor this into PafDataCacheCalc or Evaluation Package
 
 
 		// Exit if no synthetic members to process
 		List<String> dimsToCalc = new ArrayList<String>();
 		for (String dim : hierDims) {
 			PafDimTree dimTree = uowTrees.getTree(dim);
 			if (dimTree.hasSyntheticMembers()) {
 				dimsToCalc.add(dim);
 			}
 		}
 		if (timeHorizonTree.hasSyntheticMembers()) dimsToCalc.add(timeDim);
 		if (dimsToCalc.isEmpty()) return;
 
 		// Exit if no data to calculate
 		if (dataSpecByVersion.isEmpty()) return;
 
 
 		//		// Get a list of any versions that are being calculated in a default evaluation
 		//		// as these versions will be skipped.
 		//		PafPlannerConfig plannerConfig = clientState.getPlannerConfig();
 		//		if (plannerConfig.isDefaultEvalEnabledWorkingVersion()) {
 		//			defEvalVersions.add(activePlanVersion);
 		//		}
 		//		String[] evalRefVersions = plannerConfig.getDefaultEvalRefVersions();
 		//    	if (evalRefVersions != null && evalRefVersions.length> 0) {
 		//    		List<String> evalRefVersionList = Arrays.asList(plannerConfig.getDefaultEvalRefVersions());
 		//     		defEvalVersions.addAll(evalRefVersionList);
 		//    	}
 
 		// Apply version filter. If no version filter is supplied than use the active
 		// planning version.
 		if (dataSpecByVersion != null && !dataSpecByVersion.isEmpty()) {
 			versions = dataSpecByVersion.keySet();
 		} else {
 			versions.add(activePlanVersion);
 
 		}
 
 		// Create a time/year filter. Select all uow time horizon periods, as synthetic members
 		// need to be populated across elapsed and no-elapsed periods.
 		timeFilter.put(yearDim, Arrays.asList(TimeSlice.getTimeHorizonYear()));
 		List<String> openTimeHorizonPeriods = timeHorizonTree.getMemberNames(TreeTraversalOrder.POST_ORDER);
 		timeFilter.put(timeDim, openTimeHorizonPeriods);
 
 
 		// Process each version separately as they each may be populated across varying sets
 		// of intersections;
 		for (String version : versions) {
 
 			// Skip processing, if default eval is enabled on this version, since any synthetic
 			// members will also get calculated during default eval.
 			if (defEvalVersions.contains(version)) {
 				continue;
 			}
 
 			// Set version specific filters. Skip year & time dimensions as these will be 
 			// the same values for all versions.
 			baseDimCalcFilter = new HashMap<String, List<String>>(timeFilter);
 			baseDimCalcFilter.put(versionDim, new ArrayList<String>(Arrays.asList(new String[]{version})));
 			Map<Integer, List<String>> axisDataSpecs = dataSpecByVersion.get(version);
 			for (int axis : axisDataSpecs.keySet()) {
 				if (axis != timeAxis && axis!= yearAxis) {
 					String dim = dataCache.getDimension(axis);
 					List<String> members = new ArrayList<String>(axisDataSpecs.get(axis));
 					// Filter out any recalc measures
 					if (dim.equals(measureDim)) {
 						members.removeAll(recalcMeasures);
 					}
 					if (!members.isEmpty()) {
 						baseDimCalcFilter.put(dim, members);
 					}					
 				}
 			}
 
 
 			// Cycle through each hierarchical dimension and perform the necessary 
 			// calculations.
 			//
 			// Each dimension aggregation pass is grouped together with a recalc
 			// measure pass. This is necessary to ensure that the required 
 			// synthetic member intersections are calculated and only synthetic 
 			// member intersections are calculated.
 			for (String dim : dimsToCalc) {
 
 				logMsg = String.format("Calculating synthetic member(s) on dimension: [%s]", dim); 
 				logger.info(logMsg);
 
 				// Aggregate hierarchical or time horizon dimension
 				PafDimTree dimTree;
 				if (!dim.equals(timeDim)) {
 					dimTree = uowTrees.getTree(dim);
 				} else {
 					dimTree = timeHorizonTree;
 				}
 				Map<String,List<String>> dimFilter = new HashMap<String, List<String>>(baseDimCalcFilter);
 				dimFilter.put(dim, new ArrayList<String>(dimTree.getSyntheticMemberNames()));
 				PafDataCacheCalc.aggDimension(dim, dataCache, dimTree, dimFilter, DcTrackChangeOpt.NONE);
 
 				// Calculate re-calc measure intersections using the default rule set
 				if (!recalcMeasures.isEmpty()) {
 
 					// Initialization
 					long recalcStartTime = System.currentTimeMillis();
 					EvalState evalState = new EvalState(null, clientState, dataCache);
 					RuleSet ruleSet = clientState.getDefaultMsrRuleset();
 					Intersection is = new Intersection(baseDims);
 
 					// Create an intersection iterator. To reduce overhead, this iterator is 
 					// reused for each calculated measure. So, it must be initialized with a 
 					// dummy measure.
 					dimFilter.put(measureDim, new ArrayList<String>(Arrays.asList(new String[]{"[DUMMY]"})));
 					StringOdometer cellIterator = dataCache.getCellIterator(baseDims, dimFilter);
 
 					// Process each recalc measure formula in the each rule group
 					for (RuleGroup rg : ruleSet.getRuleGroups()) {
 						for (Rule r : rg.getRules()) {
 
 							// Evaluation any formula whose result term is a recalc measure across
 							// the required synthetic intersections
 							String msrName = r.getFormula().getResultMeasure();						
 							if (recalcMeasures.contains(msrName)) {							
 
 								// Iterate over required synthetic intersections and calculate them.
 								while (cellIterator.hasNext()) {
 									String[] coords = cellIterator.nextValue();		// TTN-1851
 									TimeSlice.translateTimeHorizonCoords(coords, timeAxis, yearAxis);
 									is.setCoordinates(coords);
 									is.setCoordinate(measureDim, msrName);
 									EvalUtil.evalFormula(r.getFormula(), measureDim, is, dataCache, evalState);
 								}
 								cellIterator.reset();
 							}
 						}
 					}
 					logMsg = LogUtil.timedStep("Synthetic Member - Recalc Measure Calculation Pass", recalcStartTime);
 					evalPerfLogger.info(logMsg);
 				}
 
 			}  // Next dim
 
 		}  // Next version
 
 
 		dataCache.clearDirty();
 		logMsg = LogUtil.timedStep("Synthetic Member Calculation", startTime);
 		evalPerfLogger.info(logMsg);
 	}
 
 
 }
