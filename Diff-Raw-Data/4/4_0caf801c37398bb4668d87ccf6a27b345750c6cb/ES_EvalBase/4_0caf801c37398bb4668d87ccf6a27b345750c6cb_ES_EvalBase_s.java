 /*
  *	File: @(#)ES_EvalnvMeasures.java 	Package: com.pace.base.eval 	Project: PafServer
  *	Created: Sep 26, 2005  		By: JWatkins
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
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.log4j.Logger;
 
 import com.pace.base.PafException;
 import com.pace.base.data.Intersection;
 import com.pace.base.funcs.IPafFunction;
 import com.pace.base.mdb.PafDataCache;
 import com.pace.base.rules.Formula;
 import com.pace.base.rules.Rule;
 import com.pace.base.state.EvalState;
 import com.pace.base.utility.StringUtils;
 import com.pace.server.PafDataService;
 import com.pace.server.PafMetaData;
 
 /**
  * Class_description_goes_here
  *
  * @version	x.xx
  * @author JWatkins
  *
  */
 public abstract class ES_EvalBase implements IEvalStep {
 	
 	private static Logger logger = Logger.getLogger(ES_EvalBase.class);
 	PafDataService dataService = PafDataService.getInstance();
 	
     public abstract void performEvaluation(EvalState evalState) throws PafException; 
 
     protected Set<Intersection> impactingChangeList(Rule rule, EvalState evalState ) throws PafException {
         Set<Intersection> impactList = new HashSet<Intersection>(5000);
         Set<Intersection> changeSet = new HashSet<Intersection>(5000);
         String msrDim = evalState.getAppDef().getMdbDef().getMeasureDim();
         String timeDim = evalState.getAppDef().getMdbDef().getTimeDim();
         
         Map<String, Set<String>> filterMap = new HashMap<String, Set<String>>(); 
         filterMap.put(msrDim, new HashSet<String>());
         filterMap.put(timeDim, new HashSet<String>());        
         int termIndex = 0;
         
         if ( evalState.isTimeSliceMode() ) {
         	// if time slice mode
             if (rule.hasTriggerMeasures()) {
                 // easier scenario, must be current time period and correspond to trigger measures
                 // candidate list starts from current time period changes.
                 changeSet = evalState.getCurrentTimeBasedChanges();
                 changeSet.addAll(evalState.getCurrentTimeBasedLocks());
                 for (Intersection is : changeSet) {
                     for ( String trigger : rule.getTriggerMeasures() ) {
                         if (is.getCoordinate(msrDim).equals(trigger)) {
                             impactList.add(is);
                             break;
                         }
                     }
                 }
             }
             
             else {
                 // harder scenario, each term must be inspected and its potential time offset considered also          
                 
                 // start with the measure based terms from the expression. These have the function operators stripped
                 // off for quick disqualification.
 
                 for (String term : rule.getFormula().getTermMeasures() ) {
                     // not a function so straightforward comparison from current time period                
                     if ( ! rule.getFormula().getFunctionTermFlags()[termIndex] ) { 
                         changeSet = evalState.getCurrentTimeBasedChanges();
                         changeSet.addAll(evalState.getCurrentTimeBasedLocks());                
                         for (Intersection is : changeSet) {
                             if (is.getCoordinate(msrDim).equals(term)) {
                                 impactList.add(is);
                             }                        
                         }
                     }
                     // function, so delegate responsibility to declaring function
                     else {
                         IPafFunction func = rule.getFormula().extractFunctionTerms()[termIndex];
                         impactList.addAll(func.getTriggerIntersections(evalState));    
                     }
                     termIndex++;
                 }
             }
             return impactList;	
         }
         // non time sliced mode
         else {
             String[] termsToConsider;
             Formula formula = rule.getFormula();
     		List<String> periods = evalState.getTimePeriodList();
     		boolean isTriggerMsrDriven = false;
             
     		
     		
             // if no trigger measures, just parse the components of the expression
             if (rule.hasTriggerMeasures()) {  
                 termsToConsider = rule.getTriggerMeasures(); 
                 isTriggerMsrDriven = true;
             }
             else {
                 termsToConsider = formula.getTermMeasures();            	
             }
             
             termIndex = 0;
             for (String term : termsToConsider) {
             	// nothing fancy for trigger measures, just pull back measures changes that match.
             	if (isTriggerMsrDriven) {
             		if (evalState.getChangedCellsByMsr().get(term) != null) {
             			changeSet.addAll(evalState.getChangedCellsByMsr().get(term));
             		}
             		continue;
             	}
             	
             	if ( ! rule.getFormula().getFunctionTermFlags()[termIndex] ) {             	
             		Set<Intersection> changedCells = evalState.getChangedCellsByMsr().get(term);
             		if (changedCells != null) {
             			// Calc all periods?
             			if (rule.isCalcAllPeriods()) {
             				// Clone each changed cell intersection across each period to force all periods to be recalced
             				for (Intersection intersection:changedCells) {
             					for (String period:periods) {
             						Intersection periodIs = intersection.clone();
            						periodIs.setCoordinate(timeDim,period);
             						changeSet.add(periodIs);       				
             					}
             				}
             			} else {
             				// Else, just process the changed cells for the current period
             				changeSet.addAll(changedCells);	
             			}
             		}
             	}
             	else {
                     IPafFunction func = rule.getFormula().extractFunctionTerms()[termIndex];
                     Set<Intersection> triggerIntersections = func.getTriggerIntersections(evalState);            		
                     changeSet.addAll(triggerIntersections);            		
             	}
             	termIndex++;
             }
             return changeSet;                 
         }       
     }
     
     
     
     protected List<Intersection> findIntersections(Map <String, Set<String>> filterMap, Set<Intersection> set ) {
         List<Intersection> list = new ArrayList<Intersection>();
         boolean matchesFilter;
         for (Intersection is : set) {
             matchesFilter = true;
             for (String dim : filterMap.keySet()) {
                 if (!filterMap.get(dim).contains(is.getCoordinate(dim))) {
                     matchesFilter = false;
                     break;
                 }
             }
             if (matchesFilter) list.add(is);
         }
         
         return list;
     }
  
     /**
      *  Verbose logging method
      *
      * @param step
      * @param evalState
      * @param dataCache
      * @throws PafException
      */
     protected void logEvalDetail(IEvalStep step, EvalState evalState, PafDataCache dataCache) throws PafException {
         if (PafMetaData.getServerSettings().isEvaluationStepLogging()) {
 
             String logStmt;
             logStmt =  "Evaluating Rule: " + evalState.getRule().toString();
             logStmt += "\nCurrent changed cell list: " + StringUtils.arrayToString(evalState.getCurrentChangedCells().toArray(new Intersection[0]), 5);
             logStmt += "\nData slice after step: " + step.getClass().getCanonicalName() + "\n" 
             + dataCache.getDataSlice(evalState.getSliceState().getDataSliceParms());
             logger.debug(logStmt);
         }       
     }
     
 }
