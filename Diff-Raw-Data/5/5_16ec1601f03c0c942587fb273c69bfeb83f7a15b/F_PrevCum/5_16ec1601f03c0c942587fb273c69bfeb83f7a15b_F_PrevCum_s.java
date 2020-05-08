 /*
  *	File: @(#)F_PrevCum.java 	Package: com.pace.base.funcs 	Project: Paf Base Libraries
  *	Created: Mar 21, 2006  		By: jim
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
 package com.pace.base.funcs;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.log4j.Logger;
 
 import com.pace.base.PafErrSeverity;
 import com.pace.base.PafException;
 import com.pace.base.app.PafApplicationDef;
 import com.pace.base.data.EvalUtil;
 import com.pace.base.data.IPafDataCache;
 import com.pace.base.data.Intersection;
 import com.pace.base.mdb.PafDimMember;
 import com.pace.base.mdb.PafDimTree;
 import com.pace.base.state.IPafEvalState;
 
 
 /**
  * Combines the behavior of a previous function and a cumulative function into one function
  * so as to avoid the "nesting of custom functions". It returns the running cumulative value
  * of a measure, if added up along a particular dimension (default is time). However, it 
  * stops and the "previous" point in time to the current position in the time hierarchy.
  * Signature = @PrevCum(MsrName, Dimension, Offset)
  *
  * @version	2.8.2.0
  * @author jwatkins
  *
  */
 
 
 // TODO add support for multiple offset increments in next function
 public class F_PrevCum extends AbstractFunction {
 
    	protected static int MEASURE_ARGS = 1, REQUIRED_ARGS = 1, MAX_ARGS = 3;
 
    	// parameter variables
 	private String offsetDim;
 	private int offset;
 	
 	PafDimTree offsetTree;
 	Map<String, Set<String>> filterMap = new HashMap<String, Set<String>>();    
 	
 	private static Logger logger = Logger.getLogger(F_PrevCum.class);	
 	
 	
     public double calculate(Intersection sourceIs, IPafDataCache dataCache, IPafEvalState evalState) throws PafException {
 
     	
     	// convenience strings
     	String msrDim = evalState.getMsrDim();
 
     	validateParms(evalState);
     	
     	// create testing intersection and assign measure from parameter string
     	Intersection dataIs = sourceIs.clone();
 		dataIs.setCoordinate( msrDim, this.getMeasureName() );
 		        
     	// get all members at the current level of the current intersection
 
     	PafDimMember curMbr = offsetTree.getMember(dataIs.getCoordinate(offsetDim));
         List<PafDimMember> peers = offsetTree.getMembersAtLevel(offsetTree.getRootNode().getKey(), (short) curMbr.getMemberProps().getLevelNumber());
         
         // calculate which member to stop accumulation on, by default -1 and contained on the offset variable
         // peers need to be in order
         
         // accumulator
     	double result = 0;        
         
     	// fastest approach and default case presumes offset 1
         if (offset == 1) {
         	// add all previous values until offset member is reached
         	for (PafDimMember peer : peers) {
         		if (peer.getKey().equals(curMbr.getKey())) 
         			break;
         		dataIs.setCoordinate(offsetDim, peer.getKey());
         		result += sumLevel0Desc(dataIs, evalState, dataCache);
         	}
         } else {
         	// incur overhead for more complex case, being a list this search is unfortunately O(n).
         	// find index of current member in list
     		int i = peers.indexOf(curMbr);
     		int stopIndex = i - offset;
     		int index = 0;
         	while  (index <= stopIndex) {
         		dataIs.setCoordinate(offsetDim, peers.get(index).getKey());
         		result += sumLevel0Desc(dataIs, evalState, dataCache);        		
         	}
         }
 	
     	return result;
     }
     
     private double sumLevel0Desc(Intersection dataIs, IPafEvalState evalState, IPafDataCache dataCache) throws PafException {
 		List<Intersection>floorIs = EvalUtil.buildFloorIntersections(dataIs, evalState);
 		double sum = 0;
 		for (Intersection is : floorIs) 
 			sum+= dataCache.getCellValue(is);
 		return sum;
 	}
     
     /**
      *  Parse and validate function parameters 
      *
      * @param evalState Evaluation state object
      * @throws PafException
      */
     protected void validateParms(IPafEvalState evalState) throws PafException {
 
     	int parmIndex = 0;
     	// quick check to get out if it looks like these have been validated already
     	if (this.isValidated) return;
     	
     	String errMsg = "Error in [" + this.getClass().getName() + "] - ";
     	String measureDim = evalState.getAppDef().getMdbDef().getMeasureDim();
     	
     	
      	// Check for existence of arguments
     	if (parms == null) {
     		errMsg += "[" + REQUIRED_ARGS + "] arguments are required, but none were provided.";
     		logger.error(errMsg);
     		throw new PafException(errMsg, PafErrSeverity.Error);
     	}
     	
     	// Check for the correct number of arguments
     	if (parms.length < REQUIRED_ARGS) {
     		errMsg += "[" + REQUIRED_ARGS + "] arguments are required, but [" + parms.length + "] were provided.";
     		logger.error(errMsg);
     		throw new PafException(errMsg, PafErrSeverity.Error);
     	}
    	
     	
     	// Check validity of measure arguments for existence in measures dimension
     	PafDimTree measureTree = evalState.getDataCacheTrees().getTree(measureDim);
     	if (!measureTree.hasMember(this.measureName)){
     		errMsg += "[" + this.measureName + "] is not a valid member of the [" + measureDim + "] dimension.";
     		logger.error(errMsg);
     		throw new PafException(errMsg, PafErrSeverity.Error);
 
     	}
     	
     	
 		// by default this accumulates values along the time dimension, however
 		// the dimension of accumulation can be altered by passing a 2nd parameter
 		if (parms.length > 1) {
 			offsetDim = parms[1];
 			// Check validity of dimension argument for existing dimension
 			offsetTree = evalState.getDataCacheTrees().getTree(offsetDim);
 			if (offsetTree == null) {
 				errMsg += "[" + this.measureName
 						+ "] is not a valid member of the [" + measureDim
 						+ "] dimension.";
 				logger.error(errMsg);
 				throw new PafException(errMsg, PafErrSeverity.Error);
 			}
		} else
 			offsetDim = evalState.getTimeDim();
 		
 		// be default the offset amount is 1, however if a 3rd parameter is
 		// specified it may by overridden
 		if (parms.length > 2) {
 			try {
 				offset = Integer.parseInt(parms[2]);
 			}
 			catch (NumberFormatException ex) {
 				errMsg += "[" + this.parms[2] + "] is not a valid integer";
 				logger.error(errMsg);
 				throw new PafException(errMsg, PafErrSeverity.Error);
 			}
 		}
 		else { 
 			offset = 1;
 		}
     	
     	this.isValidated = true;
 	}
     
 
 	@Override
 	public Set<Intersection> getTriggerIntersections(IPafEvalState evalState) throws PafException {
         // this function forces the recalculation of all subsequent intersections involving
         // the measure on the left hand side of this rule and time periods subsequent to the 
         // current time period
         
         String measureName = this.getMeasureName(); 
         String timeDim = evalState.getAppDef().getMdbDef().getTimeDim();
 
 
         
         // if their has been a receipt change prior to this time slice, also evaluate
         // the left hand measure in the intersection = (curent msr, cur ts, rcptchg1, rcptchg2...)
         // .or pretend their is a change in the current time period for the measure so that the
         // cumulative recalcs in this period
         Set<Intersection> chngBaseMsrs = evalState.getChangedCellsByMsr().get(measureName);
         Set<Intersection> iSet = null;      
         Intersection currentChange;
         if (chngBaseMsrs != null) {
         	iSet = new HashSet<Intersection>(chngBaseMsrs.size() * 2);   
             for (Intersection is : chngBaseMsrs) {
                 currentChange = is.clone();
                 currentChange.setCoordinate(timeDim, evalState.getCurrentTimeSlice());
                 iSet.add(currentChange);
             }
         }
         else {
         	 iSet = new HashSet<Intersection>(0);        
         }
 
         return iSet;            
 	}
 }
