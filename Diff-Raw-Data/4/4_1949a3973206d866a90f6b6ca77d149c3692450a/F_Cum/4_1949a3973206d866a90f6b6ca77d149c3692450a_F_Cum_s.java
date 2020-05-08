 /*
  *	File: @(#)F_Next.java 	Package: com.pace.base.funcs 	Project: Paf Base Libraries
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
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Properties;
 import java.util.Set;
 
 import org.apache.log4j.Logger;
 
 import com.pace.base.PafErrSeverity;
 import com.pace.base.PafException;
 import com.pace.base.app.PafApplicationDef;
 import com.pace.base.data.EvalUtil;
 import com.pace.base.data.IPafDataCache;
 import com.pace.base.data.Intersection;
 import com.pace.base.mdb.PafDimTree;
 import com.pace.base.state.IPafEvalState;
 
 /**
  * Implements a cumulative function. It totals up all the intersections along a particular
  * dimensional axis. The default axis is the time dimension (ie. @CUM(SLS_DLR, Time))
  *
 * Function Signature: @CUM(MEASURE, TIME DIMENSION, GEN/LEVEL, YEAR)
 * Example: @CUM(SLS_DLR, Time, G3, FY2007)
  * 
  * @version	x.xx
  * @author jim
  *
  */
 
 
 public class F_Cum extends AbstractFunction {
 	Map<String, Set<String>> filterMap = new HashMap<String, Set<String>>(); 
 	private static Logger logger = Logger.getLogger(F_Cum.class);	
 	
     public double calculate(Intersection sourceIs, IPafDataCache dataCache, IPafEvalState evalState) throws PafException {
     	double result = 0;
     	PafApplicationDef app = evalState.getAppDef();
 		String yearDim = app.getMdbDef().getYearDim();
 		PafDimTree yearTree = evalState.getEvaluationTree(yearDim);
 		String timeDim, levelGenParm = null, yearParm = null, yearMbr = null;
     	String errMsg = "Error in [" + this.getClass().getName() + "] - ";
     	ParsedLevelGen parsedLG = null;
     	Intersection dataIs = sourceIs.clone();
     	
         // usual use case is to provide a measure parameter this operation will apply to
     	if ( parms.length > 0 )
     		dataIs.setCoordinate(app.getMdbDef().getMeasureDim(), parms[0]);
     	
         // by default this accumulates values along the time dimension, however the dimension
         // of accumulation can be altered.
     	if (parms.length > 1 ) 
 			timeDim = parms[1];
 		else
 			timeDim = app.getMdbDef().getTimeDim();
 
 		// Gen/Level parm
 		if (parms.length <= 2) {
 			// No Gen/Level parm
 		    result  = dataCache.getCumTotal(dataIs, timeDim);
 			return result;
 		} else {
 			levelGenParm = parms[2];
 			try {
 				parsedLG = parseLevelGenParm(levelGenParm);
 			} catch (IllegalArgumentException e) {
 				errMsg += "[" + levelGenParm + "] is not a valid level/gen specification";
 				logger.error(errMsg);
 				throw new PafException(errMsg, PafErrSeverity.Error);
 			}
 		}
 		
 		// Year member parm
 		if (parms.length > 3) {
 			yearParm = parms[3];
 			Properties tokenCatalog = evalState.getClientState().generateTokenCatalog(new Properties());
 			try {
 				yearMbr = parseYearParm(yearParm, yearTree, tokenCatalog, true);
 			} catch (IllegalArgumentException e) {
 				errMsg += "[" + yearParm + "] is not a valid year specification";
 				logger.error(errMsg);
 				throw new PafException(errMsg, PafErrSeverity.Error);
 			}
 		}
 
 		result = dataCache.getCumTotal(dataIs,  timeDim, 0, parsedLG.getLevelGenType(), parsedLG.getLevelGen(), yearMbr);
 		return result;
     }
     
 
 	/**
 	 *	This method augments the normal dependency logic in a formula. In this case a dependency being changed
 	 *  causes all intersections corresponding to the result term in the rule rolling forward to also be
 	 *  recalculated.
 	 * @param evalState
 	 *
 	 * @return Set of intersection objects
 	 */
 	public Set<Intersection> getTriggerIntersections(IPafEvalState evalState) {	
 		
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
 			iSet = new HashSet<Intersection>( chngBaseMsrs.size() * 2);
 			for (Intersection is : chngBaseMsrs) {
 				currentChange = is.clone();
 //				currentChange.setCoordinate(timeDim, evalState.getCurrentTimeSlice());
 				EvalUtil.setIsCoord(currentChange, timeDim, evalState.getCurrentTimeSlice(), evalState);		// TTN-1595
 				iSet.add(currentChange);
 			}
 		}
 		else {
 			iSet = new HashSet<Intersection>(0);
 		}
 
 		return iSet;                           
 		
 	}
 
 }
