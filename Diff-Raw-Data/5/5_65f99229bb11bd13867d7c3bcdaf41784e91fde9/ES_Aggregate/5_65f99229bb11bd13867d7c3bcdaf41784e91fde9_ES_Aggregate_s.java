 /*
  *	File: @(#)ES_Aggregate.java 	Package: com.pace.base.eval 	Project: PafServer
  *	Created: Sep 14, 2005  		By: JWatkins
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
 package com.pace.server.eval;
 
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.log4j.Logger;
 
 import com.pace.base.PafBaseConstants;
 import com.pace.base.PafException;
 import com.pace.base.app.MeasureDef;
 import com.pace.base.app.MeasureType;
 import com.pace.base.comm.PafPlannerConfig;
 import com.pace.base.data.Intersection;
 import com.pace.base.data.MemberTreeSet;
 import com.pace.base.data.TimeSlice;
 import com.pace.base.mdb.DcTrackChangeOpt;
 import com.pace.base.mdb.PafDataCache;
 import com.pace.base.mdb.PafDataCacheCalc;
 import com.pace.base.mdb.PafDimTree;
 import com.pace.base.rules.Rule;
 import com.pace.base.state.EvalState;
 import com.pace.base.state.PafClientState;
 import com.pace.base.utility.LogUtil;
 import com.pace.base.utility.StringUtils;
 import com.pace.server.Messages;
 import com.pace.server.PafAppService;
 import com.pace.server.PafDataService;
 
 /**
  * Class_description_goes_here
  *
  * @version	x.xx
  * @author JWatkins
  *
  */
 public class ES_Aggregate extends ES_EvalBase implements IEvalStep {
 	
 	PafDataService dataService = PafDataService.getInstance();
     PafAppService appService = PafAppService.getInstance();
 	private static Logger logger = Logger.getLogger(ES_Aggregate.class);
 	private static Logger evalPerLogger = Logger.getLogger(PafBaseConstants.PERFORMANCE_LOGGER_EVAL);
 
 	public void performEvaluation(EvalState evalState) throws PafException{
 		
 		long startTime = System.currentTimeMillis();
 		PafDataCache dataCache = evalState.getDataCache();
 		PafClientState clientState = evalState.getClientState();
 		PafPlannerConfig plannerConfig = clientState.getPlannerConfig();
 		String measureDim = dataCache.getMeasureDim();
 		String timeDim = dataCache.getTimeDim();
 		String versionDim = dataCache.getVersionDim();
 		String yearDim = dataCache.getYearDim();
 		Rule rule = evalState.getRule();
 
         // opt out if flag set for this rule
         if (rule.isSkipAggregation()) return;
         
         // don't bother if not a default evaluation and no changes exist for the current measure (TTN-1927)
         if ( !evalState.isDefaultEvalStep() ) {
         	Set<Intersection> chngSet = evalState.getChangedCellsByMsr().get(evalState.getMeasureName());
         	if (chngSet == null || chngSet.size() == 0) return;
         }
         
 		String measure = evalState.getMeasureName();
 		
 
 		Map<String, MeasureDef> msrCat = evalState.getAppDef().getMeasureDefs();        
 		MeasureDef msrDef = msrCat.get(measure);
         
 		if ((msrDef == null) || (msrDef.getType() != MeasureType.Recalc)) {       
             // get dimension to aggregate
             String[] hierDims = evalState.getAppDef().getMdbDef().getHierDims();
             
             // build measure dimension/member filter object.
             Map<String, List<String>> aggFilter = new HashMap<String, List<String>>(1);
             List<String> mbrs = new ArrayList<String>(1);
 			mbrs.add(measure);
 
			// if measure allocation, include current measure's floor descendants
 			if (rule.isMeasureAllocation()) {
 				PafDimTree measureTree = evalState.getEvaluationTree(measureDim);
				mbrs.addAll(measureTree.getLowestMemberNames(measure));
 			}
 
 			//total hack at this point. Allows additional measures to be signaled for 
             // aggregation other than the primary measure of the rule. Used by certain custom functions
             // and needs cleared after this operation.
             mbrs.addAll( evalState.getTriggeredAggMsrs() ); 
             evalState.getTriggeredAggMsrs().clear();
             
             aggFilter.put(measureDim, mbrs);
             
             //
             // TTN-1595 - Force aggregation to use the time horizon members. Add time horizon 
             // open periods to time filter, but remove top node since it is virtual. Also set year 
             // to time horizon default, since year is embedded in each time horizon period ().
             //
             List<String> openTimeHorizonPeriods = dataCache.getOpenTimeHorizonPeriods();
 //            PafDimTree timeHorizonTree = dataCache.getDimTrees().getTree(timeHorizonDim);
 //            PafDimMember timeHorizonRoot = timeHorizonTree.getRootNode();
 //            if (timeHorizonRoot.getMemberProps().isSynthetic()) {
 //                openTimeHorizonPeriods.remove(timeHorizonRoot.getKey());            	
 //            }
             aggFilter.put(timeDim, openTimeHorizonPeriods);
             aggFilter.put(yearDim, Arrays.asList(TimeSlice.getTimeHorizonYear()));
 //            if (dataCache.hasLockedPeriods() ) {              
 //            	//get current year members
 //            	String[] yearMembers = dataCache.getDimMembers(yearDim);            
 //            	//if current year members exists
 //            	if ( yearMembers != null && yearMembers.length > 0) {           
 //            		//get 1st year member
 //            		String yearMember = yearMembers[0];
 //            		//get current open time periods
 //            		List<String> openPeriods = dataCache.getOpenPeriods(clientState.getPlanningVersion().getName(), yearMember);   
 //            		//if open time periods exists, add time dim to filter list
 //            		if ( openPeriods != null ) 
 //            			aggFilter.put(timeDim, openPeriods);     	
 //            	}
 //            }
 
              
             
             // During a default evaluation process, a version filter is created
             // to reflect options set on the planner paf config. The version
             // filter forces specific versions to be aggregated and potentially
             // generate a bunch of changes at upper level intersections.
              if (evalState.isDefaultEvalStep()) {
 
             	 List<String> filteredVersion = new ArrayList<String>();
             	 String workingVersion = clientState.getPlanningVersion().getName();
            	 
             	 // Process default eval on working version
              	 if (plannerConfig.isDefaultEvalEnabledWorkingVersion()) {
             		 filteredVersion.add(workingVersion);
              	 }
 
              	 // Process default eval on reference versions
              	 String[] evalRefVersions = plannerConfig.getDefaultEvalRefVersions();
              	 if (evalRefVersions != null && evalRefVersions.length > 0) {
 
              		 // Weed out any invalid reference versions
              		 List<String> validRefVersions = new ArrayList<String>(Arrays.asList(evalRefVersions));
              		 List<String> invalidVersions = new ArrayList<String>();
              		 List<String> uowVersions = dataCache.getBaseVersions();
              		 for (String refVersion:evalRefVersions) {
              			 // Check for the working version or any versions on in the current uow
              			 if (!uowVersions.contains(refVersion) || refVersion.equalsIgnoreCase(workingVersion)) {
              				 invalidVersions.add(refVersion);
              			 }
              		 }
 
              		 // If any invalid versions were specified, remove the invalid 
              		 // versions and log a warning message.
              		 if (invalidVersions.size() > 0) {
              			 validRefVersions.removeAll(invalidVersions);
              			 String errMsg = "Aggregation warning - the following invalid reference versions were removed from the list "
              				 + "of default evaluation reference versions: " + StringUtils.arrayToString(invalidVersions.toArray(new String[0]));
              			 logger.warn(errMsg);
              		 }
 
              		 // Add valid reference versions to list of filtered versions
              		 filteredVersion.addAll(validRefVersions);
              	 }
 
              	 // Add any filtered versions to aggregation filter, exit if no filtered versions were specified.
              	 if (filteredVersion.size() > 0) {
              		 aggFilter.put(versionDim, filteredVersion);
              	 } else {
          			 String errMsg = "Aggregation warning - default evaluation aggregation was skipped as there no valid reference versions specified";
           			 logger.warn(errMsg);  
           			 return;
              	 }
              }
 
 
              
              // Perform aggregation of base intersections
              //-- Aggregate all hierarchical dimensions
              for (String s : hierDims) {
             	 aggregateDimension(evalState, dataCache, s, aggFilter, DcTrackChangeOpt.APPEND);
              }
              //-- Aggregate time dimension
              aggregateDimension(evalState, dataCache, timeDim, aggFilter, DcTrackChangeOpt.APPEND);
              //-- Aggregate measure dimension (if measure allocation enabled) (TTN-1927)
              if (rule.isMeasureAllocation()) {
                  aggregateDimension(evalState, dataCache, measureDim, aggFilter, DcTrackChangeOpt.APPEND);
              }
              
              
              
              // Store changed cells as intersections, keyed by measure
              Set<Intersection> dcChangedIsSet = dataCache.getChangedIntersections();
              evalState.addAllChangedCells(dcChangedIsSet);
              dataCache.initChangedCells();
              
          
 			// Re-calculate potentially impacted attribute intersections supporting the current
             // view. 
 			if (evalState.isAttributeEval() && dataCache.isDirty()) {
 				logger.info("Starting attribute recalc step..."); 	
 				long attribRecalcStartTime = System.currentTimeMillis();
 				PafDataCacheCalc.calcAttributeIntersections(dataCache, clientState, dataCache.getPafMVS().getDataSliceParms(),
 						aggFilter, DcTrackChangeOpt.NONE);
 				dataCache.setDirty(false);
 				this.evalPerLogger.info(LogUtil.timedStep("Attribute recalc step", attribRecalcStartTime));
 			}
 
 		}
 		
 		logEvalDetail(this, evalState, dataCache);
 		logger.debug(Messages.getString("ES_Aggregate.0") + (System.currentTimeMillis() - startTime) + Messages.getString("ES_Aggregate.1")); //$NON-NLS-1$ //$NON-NLS-2$
 	}
 
 	private void aggregateDimension(EvalState evalState, PafDataCache dataCache, String dim, Map<String, List<String>> aggFilter, 
 			DcTrackChangeOpt trackChanges) throws PafException {
 
 		long calcStart  = System.currentTimeMillis();
 
 		// If time aggregation, aggregate along the time horizon hierarchy
 		PafDimTree aggTree;
 		MemberTreeSet treeSet = evalState.getDataCacheTrees();
 		if (!dim.equals(dataCache.getTimeDim())) {
 			aggTree = treeSet.getTree(dim);
 		} else {
 			aggTree = treeSet.getTree(dataCache.getTimeHorizonDim());
 		}
 		
 		logger.info(String.format("Aggregating dimension [%s]", dim)); 	
 		PafDataCacheCalc.aggDimension(dim, dataCache, aggTree, aggFilter, trackChanges);
 		String stepDesc = String.format("[%s] dimension aggregation ", dim);
 		this.evalPerLogger.info(LogUtil.timedStep(stepDesc, calcStart));
 
 	}
 
 }
