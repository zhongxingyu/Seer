 /*
  *	File: @(#)RuleMngr.java 	Package: com.pace.base.rules 	Project: PafServer
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
 package com.pace.server;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.SortedMap;
 
 import org.apache.log4j.Logger;
 
 import com.pace.base.PafBaseConstants;
 import com.pace.base.PafErrSeverity;
 import com.pace.base.PafException;
 import com.pace.base.app.MeasureFunctionFactory;
 import com.pace.base.app.PafApplicationDef;
 import com.pace.base.app.VersionDef;
 import com.pace.base.comm.PafPlannerConfig;
 import com.pace.base.data.EvalUtil;
 import com.pace.base.data.Intersection;
 import com.pace.base.data.MemberTreeSet;
 import com.pace.base.data.PafMemberList;
 import com.pace.base.mdb.PafDimMember;
 import com.pace.base.mdb.PafDimTree;
 import com.pace.base.project.ProjectElementId;
 import com.pace.base.rules.Rule;
 import com.pace.base.rules.RuleGroup;
 import com.pace.base.rules.RuleSet;
 import com.pace.base.state.EvalState;
 import com.pace.base.state.PafClientState;
 import com.pace.base.utility.TimeBalance;
 
 /**
  * Class_description_goes_here
  *
  * @version	x.xx
  * @author JWatkins
  *
  */
 /**
  * Class_description_goes_here
  *
  * @version	x.xx
  * @author jim
  *
  */
 public class RuleMngr {
     private static RuleMngr _instance = null;
     private HashMap<String, List<RuleSet>> ruleSets = new HashMap<String, List<RuleSet>>();
     private HashMap<RuleGroup, HashMap<String, ArrayList<String>>> ruleGroupPriorityLists = new HashMap<RuleGroup, HashMap<String, ArrayList<String>>>();
     private Map<String, Map<String, List<String>>> dependencyMaps = new HashMap<String, Map<String, List<String>>>();
     private static Logger logger = Logger.getLogger(RuleMngr.class);
 
     private RuleMngr() {} // a singleton
     
     public static RuleMngr getInstance() {
         if (_instance == null) {
             _instance = new RuleMngr();
         }
         
         return _instance;
     }
     
 //    public RuleSet[] getRuleSets(String appId) {
 //        if (!ruleSets.containsKey(appId) || PafMetaData.debugMode )
 //            loadRuleSets(appId);
 //        
 //        return ruleSets.get(appId).toArray(new RuleSet[0]);
 //    }
     
     public RuleSet parseRuleSet(RuleSet rs, MeasureFunctionFactory functionFactory) throws PafException {
     	for (RuleGroup rg : rs.getRuleGroups()) {
     		for (Rule r : rg.getRules()) {
     			r.getFormula().parse(functionFactory);
     		}
     	}
     	
     	
     	return rs;
     }
     
     
     public RuleSet getRuleSet(String appId, String dim) {
         if (!ruleSets.containsKey(appId)) throw new IllegalArgumentException("No rulesets defined for application. (" + appId + ")");
         List<RuleSet> appRuleSets = ruleSets.get(appId);
         for (RuleSet rs : appRuleSets) {
             if (rs.getDimension().equals("dim")) return rs;
         }
         throw new IllegalArgumentException("No rulesets defined for specified dimension (" + dim + ")" + " in application (" + appId + ")");
         }
 
     private List<RuleSet> filterUserRuleSets(PafPlannerConfig plannerConfig, List<RuleSet> allRuleSets) throws PafException {
         // if no filters specified just return all rulesets as being available to that users
         if (plannerConfig.getRuleSetNames() == null || plannerConfig.getRuleSetNames().length < 1) 
             return allRuleSets;
         
         List<RuleSet> filteredList = new ArrayList<RuleSet>();
         boolean bFound = false;
         for (String rsName : plannerConfig.getRuleSetNames()) {
         	bFound = false;
             for (RuleSet rsTemp : allRuleSets) {
                 if (rsTemp.getName().trim().equals(rsName.trim())) {
                     filteredList.add(rsTemp);
                     bFound = true;
                     break;
                 }
             }
             // if we made it this far we never found a match, throw exception
             if (!bFound)
             	throw new PafException("No ruleset defined with the specified name. [" + rsName + "]", PafErrSeverity.Error );
         }
         
         return filteredList;
     }
     
     public RuleSet[] calculateRuleSets(MemberTreeSet treeSet, PafApplicationDef app, PafPlannerConfig plannerConfig) throws PafException {
         
     	RuleSet rs;
     	PafDimTree tree;
         
         // get measure based rule sets and filter for users current configuration
         List<RuleSet> ruleSetList = getMsrRuleSetsForConfig(plannerConfig, app);
         
     
         // add time dimension rule sets. also include time horizon dim for multi-year protection
         // rules (TTN-1956).
//        String[] timeDims = new String[]{app.getMdbDef().getTimeDim(), PafBaseConstants.TIME_HORIZON_DIM};
        String[] timeDims = new String[]{app.getMdbDef().getTimeDim()};
         for (String timeDim : timeDims) {
         	tree = treeSet.getTree(timeDim);   
         	rs = RuleMngr.createHierarchyRuleSet(tree, TimeBalance.First, timeDim );
         	if (rs != null)
         		ruleSetList.add(rs); 
         	rs = RuleMngr.createHierarchyRuleSet(tree, TimeBalance.Last, timeDim );
         	if (rs != null) 
         		ruleSetList.add(rs);
         }
     
 
         
         // *** We stopped sending down the hierarchical dim rule sets after attribute dimension logic was added
         // *** to the server, because there was too much overhead to process all those dimensions.
         // add hierarchical rule sets
        /* for (String dimName : mdbDef.getHierDims()) {
             rs = createHierarchyRuleSet(treeSet.getTree(dimName), TimeBalance.None, dimName);
             if (rs != null) {
                 ruleSetList.add(rs);
             }
         }*/
         
         // add version rulesets
         rs = new RuleSet();
         rs.setDimension(app.getMdbDef().getVersionDim());
         rs.setRuleGroups(createVersionRuleGroups(app.getVersionDefs().values()));
         if (PafMetaData.isDebugMode()) {
             logger.info("RuleSet calculated for version dimension"); 
             logger.info(rs.toString());
         }   
         // parse any function references in version rule set list and add to rule set list
         rs = parseRuleSet(rs, app.getMeasureFunctionFactory());
         ruleSetList.add(rs);
   
         return ruleSetList.toArray(new RuleSet[0]);
     }
     
     private RuleGroup[] createVersionRuleGroups(Collection<VersionDef> versionList) {
         
     	RuleGroup rg; 
         ArrayList<RuleGroup> ruleGroups = new ArrayList<RuleGroup>(versionList.size());
         ArrayList<Rule> rules;
         
         // Create rule groups for derived versions
         for (VersionDef vd : versionList) {
             if (PafBaseConstants.DERIVED_VERSION_TYPE_LIST.contains(vd.getType())) {
                 rg = new RuleGroup();
                 rules = new ArrayList<Rule>();                
                 rules.add(new Rule(vd.getBaseFormulaString()));
                 rules.add(new Rule(vd.getVersionFormulaString()));
                 rg.setRules(rules.toArray(new Rule[0]));
                 ruleGroups.add(rg);
             }
         }
         return ruleGroups.toArray(new RuleGroup[0]);
 
     }
     
 
     public ArrayList<String> getRuleGroupChngPriority(RuleGroup rg, String coordinate) {
         coordinate = coordinate.trim();
         ArrayList<String> recalcPriorityList;
         HashMap<String, ArrayList<String>> recalcPriorityMap;
         
         if (ruleGroupPriorityLists.containsKey(rg))
             recalcPriorityList = ruleGroupPriorityLists.get(rg).get(coordinate);
         else {
             recalcPriorityMap = calculatePriorityMap(rg);
             ruleGroupPriorityLists.put(rg, recalcPriorityMap);
             recalcPriorityList = recalcPriorityMap.get(coordinate);
         }
         
         return recalcPriorityList;
     }
 
     private HashMap<String, ArrayList<String>> calculatePriorityMap(RuleGroup rg) {
 
         ArrayList<String> rgList;
         HashMap<String, ArrayList<String>> priorityMap = new HashMap<String, ArrayList<String>>(rg.getRules().length);
         String[] terms;
         for (Rule rule : rg.getRules() ) {
             terms = rule.getFormula().getExpression().split("[+\\-*/()]");
             for (String term : terms) {
                 term = term.trim();
                 if (priorityMap.containsKey(term))
                     priorityMap.get(term).add(rule.getFormula().getResultTerm().trim());
                 else {
                     rgList = new ArrayList<String>();
                     rgList.add(rule.getFormula().getResultTerm().trim());
                     priorityMap.put(term.trim(), rgList);
                 }
             }
         }
 
         return priorityMap;
     }
 
     public Map<String, List<String>> getDependencyMap(String appId) throws PafException {
         
         Map<String, List<String>> dependencyMap = dependencyMaps.get(appId);
         
         if (dependencyMap == null || dependencyMap.size() == 0) { 
             dependencyMap = new HashMap<String, List<String>>();
             List <String>calcs;
             RuleSet rs = getRuleSet(appId, PafAppService.getInstance().getApplication(appId).getMdbDef().getMeasureDim());
             for (RuleGroup rg : rs.getRuleGroups()) {
                 for (Rule rule : rg.getRules()) {
                     String[] compTerms = rule.getFormula().getExpressionTerms();
                     for (String term : compTerms) {
                         if (dependencyMap.containsKey(term))
                             calcs = dependencyMap.get(term);
                         else
                             calcs = new ArrayList<String>();
                         if (!calcs.contains(rule.getFormula().getResultTerm()))
                             calcs.add(rule.getFormula().getResultTerm());
                         dependencyMap.put(term, calcs);
                         }
                     }
                 }
             }
         
         dependencyMaps.put(appId, dependencyMap);
         return dependencyMap;
     }
 
     
     /**
      *	For a given intersection, the current rulegroup is examined to determine the appropriate
      *  rule to use to calculate it. The intersection passed in is a component change on the right 
      *  side of an equation, so the rule used will be the highest priority rule in the group, whose
      *  expression result is not locked.
      *
      * @param ruleGroup the rulegroup currently being evaluated
      * @param evalState 
      * @param is the intersection that changed and is being considered for this evaluation.
      * @return Rule
      */
     public static Rule findLeadingRule(RuleGroup ruleGroup, EvalState evalState, Intersection is) {
 
         Rule leadingRule = null;
         Intersection testIs;
         Intersection firstInx = null;
         String measureName;
         String msrDim = evalState.getAppDef().getMdbDef().getMeasureDim();
         Intersection firstUnroundedLockedMeasure = null;
         Rule firstUnroundedLockedMeasureRule = null;
         PafClientState clientState = evalState.getClientState();
         final String LR_FIX_OPTION = "@EVAL_METHOD_B@";
         boolean isLrFixSelected = false;
 
         // singleton rule exit, if only 1 rule in rule group this has to be the leading rule
         // but this whole operation really needs a re-think
         if (ruleGroup.getRules().length == 1) 
         	return ruleGroup.getRules()[0];
         
         // check for leading rule fix in rule set comment (TTN-1761)
         String rsComment = clientState.getMsrRulsetByName(clientState.getCurrentMsrRulesetName()).getComment();
         if (rsComment != null && rsComment.toUpperCase().contains(LR_FIX_OPTION)) {
         	isLrFixSelected = true;
         }
         
         // find leading rule
         for (Rule r : ruleGroup.getRules()) {    
             measureName = r.getFormula().getResultTerm();
             if (evalState.getAppDef().isFunction(measureName)) {
             	// Measure functions don't need the same lead rule logic. Just return the rule
             	// corresponding to the currently processed measure.
             	testIs = EvalUtil.translocateIntersection(is, r.getFormula().extractResultFunction(), evalState);
             	if (testIs == null) continue;
             	testIs.setCoordinate(msrDim, measureName);
             } else {
             	testIs = is.clone(); 
             	testIs.setCoordinate(msrDim, measureName );
             }
             
             if (!evalState.isRoundingResourcePass()){
             	// Regular pass (no rounding) - select the current rule if the result term is not locked
             	if (!evalState.getCurrentLockedCells().contains(testIs)) {
         			// If LR FIX selected, also ensure that this is a pending change that triggers the rule. (TTN-1761)
             		if (!isLrFixSelected || EvalUtil.changeTriggersFormula(is, r, evalState)) {
             			leadingRule = r;
 
             			break;
             		}
             	}
             }
             else if (evalState.getAppDef().getAppSettings() != null && evalState.getAppDef().getAppSettings().isEnableRounding()){
             	// This is the rounding resource pass so take into consideration locks on allocated intersections
             	
             	// Select the current rule if the current intersection is locked or contains a locked allocation
 		        if (!evalState.getCurrentLockedCells().contains(testIs) && !evalState.getAllocatedLockedCells().contains(testIs)) {
 		        	// If LR FIX selected, also ensure that the current rule can be triggered by a pending change. (TTN-1761)
             		if (!isLrFixSelected || EvalUtil.changeTriggersFormula(is, r, evalState)) {
 						leadingRule = r;
 						break;
 					}
 		        }  
 		        else  //Intersection is locked
 		        //If no unlocked intersection exists for an unrounded measure, then unlock the first unrounded measure
 		        //First capture the first unrounded, locked measure
 		        {	
 		        	if ( firstUnroundedLockedMeasure == null)
 		        	{
 		        		//Unrounded measure check
 			        	if (!evalState.getRoundingRules().containsKey(measureName))
 			        	{
 			        		firstUnroundedLockedMeasure = testIs.clone();	
 			        		firstUnroundedLockedMeasureRule = r;
 			        	}
 		        	}
 		        }
             }
             if(firstInx == null){
             	firstInx = testIs.clone();
             }
         }
         
         //if the leadingRule is null, set it to be the first rule in the RuleGroup.
         //It also checks to see if there is an original locked cell that needs to be
         //unlocked so that evaluation can happen.
         if(leadingRule == null){
         	
         	if (!evalState.isRoundingResourcePass()){
         		//get the first rule.
         		Rule firstRule = ruleGroup.getRules()[0];
         		//create a temporary intersection.
         		Intersection temp = null;
         		//loop thur the rules.
         		for (Rule r : ruleGroup.getRules()) {
         			//clone the intersection
         			temp = firstInx.clone();
         			//set the intersection to have the left hand side of the formula
         			temp.setCoordinate(msrDim, r.getFormula().getResultTerm());
         			//see if the intersection was originally locked by the user, 
         			//if so then remove the lock so evaluation can flow into that cell -
         			// unless this is an attribute evaluation
         			//TODO Determine if check on attribute eval is still needed
         			if(evalState.getOrigLockedCells().contains(temp) && !evalState.isAttributeEval()){
         				evalState.getCurrentLockedCells().remove(firstInx);
         				break;
         			}
         		}
         		leadingRule = firstRule;
         	}
         	else
         	{
         		if (evalState.getAppDef().getAppSettings().isEnableRounding() == true)
         		{
         	        if ( firstUnroundedLockedMeasure != null)
         	        {
         	        	if (evalState.getCurrentLockedCells().contains(firstUnroundedLockedMeasure) && !evalState.isAttributeEval())
         	        	{
         	        		evalState.getCurrentLockedCells().remove(firstUnroundedLockedMeasure);
         	        	}
         	        	else if (evalState.getAllocatedLockedCells().contains(firstUnroundedLockedMeasure))
         	        	{
         	        		evalState.getAllocatedLockedCells().remove(firstUnroundedLockedMeasure);
         	        	}
         	        	
         	        	//recursive call
         	        	leadingRule = firstUnroundedLockedMeasureRule;
         	        }
         		}	
         	}
         }
         
         return leadingRule;
     }
     
     public static RuleSet createHierarchyRuleSet(PafDimTree tree, TimeBalance tb, String dimension) {
     	
     	SortedMap<Integer, List<PafDimMember>> treeMap = tree.getMembersByGen();   	
         if (treeMap.keySet().size() < 2) return null;
 
         RuleSet rs = new RuleSet();       
         ArrayList<RuleGroup> ruleGroups = new ArrayList<RuleGroup>();
         rs.setDimension(dimension);
         
         // set type
         // TODO get time balance time into constants or something external
         if (tb == TimeBalance.None)
         	rs.setType(PafBaseConstants.RULESET_TYPE_HIERARCHY);            
         else if (tb == TimeBalance.First)
         	rs.setType(PafBaseConstants.RULESET_TYPE_TIME_BAL_FIRST);
         else if (tb == TimeBalance.Last)
         	rs.setType(PafBaseConstants.RULESET_TYPE_TIME_BAL_LAST);
         
     	// calculate rulegroups at each parent child level
         for (int genKey = treeMap.firstKey() ; genKey < treeMap.lastKey() ; genKey++) {
             ruleGroups.addAll(calculateHierarchicalRuleGroups(treeMap.get(genKey), tb));
         }
     	
         rs.setRuleGroups(ruleGroups.toArray(new RuleGroup[0]));
         
         if (PafMetaData.isDebugMode()) {
             logger.info("\nRuleSet calculated for hierarchical dimension: " + tree.getRootNode().getKey() + "\n\n" + rs.toString()); 
         }       	
     	return rs;
     }
     
     private static List<RuleGroup> calculateHierarchicalRuleGroups(List<PafDimMember> upperLevelMembers, TimeBalance tb) {
         RuleGroup rg;
         ArrayList<RuleGroup> ruleGroups = new ArrayList<RuleGroup>();
         ArrayList<Rule> rules;
         String formula;
         
         for (PafDimMember member : upperLevelMembers) {
             if (member.getChildren().size() > 0) {
                 
             rg = new RuleGroup();
             rules = new ArrayList<Rule>();
                 switch (tb) {
                 case None:
                     // 1st a rule for parent = sum of children
                     formula = member.getKey() + " = " + additiveFormula(member.getChildren());
                     rules.add(new Rule(formula));
                     // now a rule for each child = Parent - (siblings)
                     for (PafDimMember child : member.getChildren() ) {
                         // copy children arraylist into new list and remove current child
                         ArrayList<PafDimMember> siblings = new ArrayList<PafDimMember>(member.getChildren().size());
                         siblings.addAll(member.getChildren());
                         siblings.remove(child);
                         if (siblings.size() > 0) 
                             formula = child.getKey() + " = " + member.getKey() + " - " + additiveFormula(siblings);
                         else
                             formula = child.getKey() + " = " + member.getKey();
                         
                         rules.add(new Rule(formula));
                     }
                     break;
                     
                 case First:
                     // 1st a rule for parent = first child
                     formula = member.getKey() + " = " + member.getChildren().get(0).getKey();
                     rules.add(new Rule(formula));
                     // now a rule for 1st child = Parent
                     formula = member.getChildren().get(0).getKey() + " = " + member.getKey() ;
                     rules.add(new Rule(formula));
                     break;
                     
                 case Last:
                     // 1st a rule for parent = last child
                 	int lastIndex = member.getChildren().size() - 1;
                     formula = member.getKey() + " = " + member.getChildren().get(lastIndex).getKey();
                     rules.add(new Rule(formula));
                     // now a rule for last child = Parent
                     formula = member.getChildren().get(lastIndex).getKey() + " = " + member.getKey() ;
                     rules.add(new Rule(formula));            	
                     break;             
                 }
                 rg.setRules(rules.toArray(new Rule[0]));
                 ruleGroups.add(rg);
             }
         }
         
         return ruleGroups;
     }
     
     public List<RuleSet> getMsrRuleSetsForConfig(PafPlannerConfig plannerConfig, PafApplicationDef app) throws PafException {
     	
  
     	// import rule sets (TTN-1456)
 		PafMetaData.getPaceProject().loadData(ProjectElementId.RuleSets);
 
     	// get measure based rule sets and filter for users current configuration
         List<RuleSet> ruleSetList = filterUserRuleSets(plannerConfig, 
         		new ArrayList<RuleSet>(PafMetaData.getPaceProject().getRuleSets().values()));
         
         //TODO check for ruleSetList to be null.  this could happen if xml has invalid tag and pafxstream blows up.
         
         // initialize attributes appropriate for a measure rule set
         for (RuleSet rsTemp : ruleSetList) {
             rsTemp.setDimension(app.getMdbDef().getMeasureDim());
             rsTemp.setType(PafBaseConstants.RULESET_TYPE_MEASURE);
             
             // post process rule set formulas using function factory
             this.parseRuleSet(rsTemp, app.getMeasureFunctionFactory());
             
             // expand rule set measure list (TTN-1698)
             rsTemp.setMeasureList(expandMeasureList(rsTemp.getMeasureList(), rsTemp, app));
             
             // expand lift allocation rule set measure lists (TTM-1793)
             rsTemp.setLiftAllMeasureList(expandMeasureList(rsTemp.getLiftAllMeasureList(), rsTemp, app));
             rsTemp.setLiftExistingMeasureList(expandMeasureList(rsTemp.getLiftExistingMeasureList(), rsTemp, app));
                         
         }
                
         return ruleSetList;
     }
     
     /**
      * Expand rule set measure list specification
      * 
      * @param measureList Measure list to be expanded
      * @param ruleSet Rule set
      * @param app Application definition
      * 
      * @return Expanded measures
      * @throws PafException 
      */
     private static String[] expandMeasureList(String[] measureList, RuleSet ruleSet, PafApplicationDef app) throws PafException {
     	
 		String measureDim = app.getMdbDef().getMeasureDim();
 		List<String> updatedMeasureList = new ArrayList<String>();
 		List<String> measureTerms = new ArrayList<String>();
 		
 		
 		// This method expands all of the measure list terms. This is
 		// needs to be done whenever a rule set is read from disk,
 		// as the loose measure evaluation process ignores any measures 
 		// indirectly reference in a member expression 
 		// ex. "@IDESC(MDTTL_DRL)".  (TTN-1698)
 		
 		// no measure list - exit method
 		if (measureList == null || measureList.length == 0) {
 			return measureList;
 		}
 		      
 		// Expand each measure list term
 		PafDataService dataService = PafDataService.getInstance();
 		for (String measureTerm : measureList) {
 			
 			// Check for member list token
 			if (measureTerm.startsWith(PafBaseConstants.MEMBERLIST_TOKEN)) {
 				
 				// Parse out member list name
 				String umlKey = measureTerm.substring(measureTerm.indexOf("(")+1, measureTerm.lastIndexOf(")"));
 				PafMemberList memberList = PafDataService.getInstance().getUserMemberList(umlKey);
 				
 				// Add each member term to temp list
 				if (memberList.getDimName().equals(measureDim)) {
 					for (String memberTerm : memberList.getMemberNames()) {
 						measureTerms.add(memberTerm);
 					}
 				} else {
 					String s = String.format("Only memberlists from the measures dimension can be used in rulsets. Memberlist [%s] found in ruleset [%s]",
 							measureTerm, ruleSet.getName());
 					throw new IllegalArgumentException(s);
 				}
 			} else {
 				// No member list - just add measure term to temp list
 				measureTerms.add(measureTerm);
 			}
 		}
 		
 		// Expand all found measure terms and return expanded list
 		updatedMeasureList.addAll(dataService.expandExpressionList(measureDim, measureTerms, null));
 		return updatedMeasureList.toArray(new String[0]);
 		
 
 	}
 
 	private static String additiveFormula(List<PafDimMember> members) {
         if (members.size() < 1) return "";
         StringBuilder sb = new StringBuilder("(");
         for (PafDimMember member : members) {
             sb.append(member.getKey() + " + ");
         }
         sb.setCharAt(sb.lastIndexOf("+"), ')');
         
         return sb.toString();
     }
 
 	public static boolean isLeadRule(Intersection is, Rule rule, EvalState evalState) {
 		return (rule.equals(findLeadingRule(evalState.getRuleGroup(), evalState, is)));	
 	}
 
 }
