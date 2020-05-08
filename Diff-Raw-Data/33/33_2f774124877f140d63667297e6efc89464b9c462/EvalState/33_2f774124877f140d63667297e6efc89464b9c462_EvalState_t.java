 /*
  *	File: @(#)EvalState.java 	Package: com.pace.base.state 	Project: Paf Base Libraries
  *	Created: Sep 13, 2005  		By: JWatkins
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
  *	05/24/06		AFarkas			x.xx			Moved from com.pace.base.eval (PafServer)
  * 
  */
 package com.pace.base.state;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import com.pace.base.PafBaseConstants;
 import com.pace.base.app.MeasureType;
 import com.pace.base.app.PafApplicationDef;
 import com.pace.base.app.VersionDef;
 import com.pace.base.data.Intersection;
 import com.pace.base.data.MemberTreeSet;
 import com.pace.base.data.TimeSlice;
 import com.pace.base.mdb.PafDataCache;
 import com.pace.base.mdb.PafDimMember;
 import com.pace.base.mdb.PafDimTree;
 import com.pace.base.mdb.TreeTraversalOrder;
 import com.pace.base.rules.Formula;
 import com.pace.base.rules.RoundingRule;
 import com.pace.base.rules.Rule;
 import com.pace.base.rules.RuleGroup;
 import com.pace.base.rules.RuleSet;
 
 
 /**
  * This class holds the state of an evaluation sequence. It is initialized with all
  * the starting information relevant to evaluation, and then mutates as the various
  * steps update the collections within.
  *
  * @version	0.01
  * @author JWatkins
  *
  */
 
 
 
 public class EvalState implements IPafEvalState, Cloneable {
 
 	private int loadFactor = 10000;
 	private static final int LG_CHNG_LOAD = 10000;
 	private static final int SM_CHNG_LOAD = LG_CHNG_LOAD / 100;
 
 	private SliceState sliceState;
 
 	// this collection not currently used
 	private Set <Intersection> currentProtectedCells = new HashSet<Intersection>();
 
 	private Set <Intersection> currentLockedCells = new HashSet<Intersection>(LG_CHNG_LOAD);    
 	private Set <Intersection> currentChangedCells = new HashSet<Intersection>(LG_CHNG_LOAD);
 	
 	private Set <Intersection> userLocksAndChangedCells = new HashSet<Intersection>();
 
 	private boolean roundingResourcePass = false;
 	private boolean skipRounding = false;
 
 	private Map<String, RoundingRule> roundingRules = new HashMap<String, RoundingRule>();
 
 	private Set <Intersection> origLockedCells = new HashSet<Intersection>();
 	private Set <Intersection> origChangedCells = new HashSet<Intersection>();
 
 	
 	private Map <Intersection, Formula> protectedCellFormulas = new HashMap<Intersection, Formula>();
 	private RuleGroup ruleGroup;
 	private Rule rule;
 
 	private Set <Intersection> consumedByRulegroup = new HashSet<Intersection>();
 	
 	//	private HashSet <Intersection> allocations = new HashSet<Intersection>();
 	private HashMap<String, Set<Intersection>> allocationsByMsr = new HashMap<String, Set<Intersection>>(SM_CHNG_LOAD);
 	private HashSet <Intersection> allocatedLockedCells = new HashSet<Intersection>();
 
 	private HashSet <Intersection> forcedCalcs = new HashSet<Intersection>();    
 	private HashSet <Intersection> requiredCalcs = new HashSet<Intersection>();
 	private HashMap<String, Set<Intersection>> changedCellsByMsr = new HashMap<String, Set<Intersection>>(SM_CHNG_LOAD);
 	private HashMap<String, Set<Intersection>> changedCellsByTime = new HashMap<String, Set<Intersection>>(SM_CHNG_LOAD);
 	private HashMap<String, Set<Intersection>> origLockedCellsByTime = new HashMap<String, Set<Intersection>>(SM_CHNG_LOAD);
 	
 	private Map<Intersection, Double> varianceReplicationValues = new HashMap<Intersection, Double>();
 	private List<String> varianceVersionNames = new ArrayList<String>();
 	
 	// Contribution percent change collections
 	private Map<Intersection, Double> changedContribPctValueMap = new HashMap<Intersection, Double>();
 	private Map <String, Set<Intersection>> changedContribPctCellsByMsr = new HashMap <String, Set<Intersection>>();
 	private Map <String, Set<Intersection>> changedContribPctCellsByTime = new HashMap <String, Set<Intersection>>();
 	private Set <Intersection> replicateAllContribPctCells = new HashSet<Intersection>();
 	private Set <Intersection> replicateExistingContribPctCells = new HashSet<Intersection>();
 	
 	private List<String> triggeredAggMsrs = new ArrayList<String>();
 	
 	
 	private PafApplicationDef appDef;
 
 	private Long startTime;
 	private MemberTreeSet dataCacheTrees;
 	private RuleSet measureRuleSet;
 	private boolean stateChanged;
 	private PafClientState clientState;
 	private PafDataCache dataCache;
 	private boolean isAttributeEval = false;
 	private boolean isDefaultEvalStep = false;
 	private boolean hasContribPctFormulas = false;
 
 	// time slice mode variables
 	private String currentTimeSlice;
 	private String previousTimeSlice;
 	private boolean timeSliceMode;
 	private List<String> timePeriodList;
 	private PafDimTree timeSubTree;
 
 	// convenience variables;
 	private String msrDim;
 	private String timeDim;
 	private String versionDim;
 	private String[] axisAllocPriority;		// The dimensions to use for allocation
 	private String[] axisSortPriority;		// The dimensions to use for intersection sorting
 	private String[] dimSequence;
 	
 
 	/**
 	 * @param sliceState Slice state
 	 * @param clientState Client state
 	 * @param dataCache Data cache
 	 */
 	public EvalState(SliceState sliceState, PafClientState clientState, PafDataCache dataCache) {
 		this.sliceState = sliceState;
 		this.clientState = clientState;
 		this.appDef = clientState.getApp();
 		this.dataCache = dataCache;
 		dataCache.setEvalState(this);
 		init();
 	}
 
 
 	/**
 	 * @param clientState Client state
 	 * @param dataCache Data cache
 	 */
 	public EvalState(PafClientState clientState, PafDataCache dataCache) {
 		this.clientState = clientState;
 		this.appDef = clientState.getApp();
 		this.dataCache = dataCache;
 		dataCache.setEvalState(this);
 		init();
 	}
 
 	/**
 	 * @param clientState Client state
 	 */
 	public EvalState(PafClientState clientState) {
 		this.clientState = clientState;
 		this.appDef = clientState.getApp();
 		init();
 	}
 
 	private void init() {
 		this.msrDim = appDef.getMdbDef().getMeasureDim();
 		this.timeDim = appDef.getMdbDef().getTimeDim();
 		this.versionDim = appDef.getMdbDef().getVersionDim();
 		
 		// slice state is null in the case of default evaluations
 		if (sliceState != null) {
 			this.currentLockedCells = buildLists(sliceState.getLockedCells());
 			this.userLocksAndChangedCells.addAll(currentLockedCells);
 			this.currentProtectedCells = buildLists(sliceState.getProtectedCells());
 			this.currentChangedCells = buildLists(sliceState.getChangedCells());
 
 
 			// append locked periods
 			this.currentLockedCells.addAll(clientState.getCurrentLockedIntersections(sliceState.getViewName()));
 
 		}
 
 		// initialize default axis priority
 		this.axisSortPriority = appDef.getMdbDef().getAxisPriority();
 		
 		// process protected cell formulas into formula objects
 		// each protected cell maps to it's formula object
 //		int i = 0;
 //		Formula f = null;
 //		String[] formulaTerms;
 
 //		for (Intersection is : this.currentProtectedCells) {          
 //		formulaTerms = this.sliceState.getProtectedCellFormulas()[i++].split("[=]");
 //		f = new Formula(formulaTerms[0],formulaTerms[1]);           
 //		protectedCellFormulas.put(is, f);
 //		}
 
 		// process changed cells into changed by measure stack        
 		for (Intersection is : currentChangedCells) {
 			this.addChangedCellByMsr(is);
 			this.addChangedCellByTime(is);
 		}
 		
 		//TODO (AF) filter out elapsed periods
 		this.addAllAllocations(this.currentLockedCells);
 		this.addAllAllocations(this.currentChangedCells);
 
 		this.forcedCalcs.addAll(this.currentProtectedCells);        
 
 		origLockedCells.addAll(currentLockedCells);
 		origChangedCells.addAll(currentChangedCells);
 
 		// initialize original locks by time collection
 		for (Intersection is : origLockedCells) {
 			this.addOrigLockedCellByTime(is);
 		}
 
 		// Initialize data cache trees from clientState
 		this.dataCacheTrees = clientState.getUowTrees();
 
 
 		// initialize time structures.
 		// Get a post order version of the time dimension for the data cache
 		// and filter out any locked periods
 		this.timeSubTree = dataCacheTrees.getTree(this.getTimeHorizonDim());	// TTN-1595
 		List<String> timePeriods = timeSubTree.getMemberNames(TreeTraversalOrder.POST_ORDER);
		Set<String> lockedPeriods = clientState.getLockedTimeHorizonPeriods();
		if (lockedPeriods != null) {
			timePeriods.removeAll(lockedPeriods);
 		}
 		
 		// Set useful objects in eval state object
 		this.setTimePeriodList(timePeriods);  
 
 		//build a list of variance version names
 		if(appDef != null && appDef.getVarianceVersionDefs() != null)
 		{
 			varianceVersionNames.clear();
 			for (VersionDef vd : appDef.getVarianceVersionDefs()) {
 				varianceVersionNames.add(vd.getName());
 	        }
 		}
 		
 	}
 
 	public List<String> getTimePeriodList() {
 		return timePeriodList;
 	}
 
 	public void setTimePeriodList(List<String> timePeriodList) {
 		this.timePeriodList = timePeriodList;
 	}
 
 	public PafDimTree getTimeSubTree() {
 		return timeSubTree;
 	}
 
 
 	public SliceState getSliceState() {
 		return sliceState;
 	}
 
 	public Set <Intersection>getCurrentLockedCells() {
 		return currentLockedCells;
 	}
 
 	//pmack
 	public Set <Intersection>getAllocatedLockedCells() {
 		return allocatedLockedCells;
 	}
 
 
 	public Set <Intersection> getCurrentProtectedCells() {
 		return currentProtectedCells;
 	}
 
 	private Set<Intersection> buildLists(Intersection[] is) {
 		if (sliceState.getLockedCells() == null || is == null) return new HashSet<Intersection>();
 
 		Set<Intersection> list = new HashSet<Intersection>(sliceState.getLockedCells().length);
 		for (Intersection i : is) {
 			list.add(i);
 		}
 		return list;
 	}
 
 	public Set<Intersection> getCurrentChangedCells() {
 		return currentChangedCells;
 	}
 
 	/**
 	 *	Remove changed cell for all related collections
 	 *
 	 * @param is Intersection
 	 */
 	public void removeCurrentChangedCell(Intersection is) {
 		currentChangedCells.remove(is);
 		removeChangedCellByMsr(is);
 		removeChangedCellByTime(is);
 	}
 
 	public void removeOrigLockedCell(Intersection is) {
 		origLockedCells.remove(is);
 		removeOrigLockedCellByTime(is);
 	}
 	
 	public void addOrigLockedCell(Intersection is) {
 		origLockedCells.add(is);
 		addOrigLockedCellByTime(is);
 	}
 	
 	
 
 	/**
 	 *	Remove collection of changed cells from all related collections
 	 *
 	 * @param isCollection Collection of intersections
 	 */
 	public void removeAllCurrentChangedCells(Collection<Intersection> isCollection) {		
 		Intersection[] changedCells = isCollection.toArray(new Intersection[0]);
 		for (Intersection changedCell:changedCells) {
 			removeCurrentChangedCell(changedCell);
 		}
 	}
 	
 	public Formula getProtectedCellFormula(Intersection is) {
 		return protectedCellFormulas.get(is);
 	}
 
 	public void removeProtectedCell(Intersection is) {
 		currentProtectedCells.remove(is);
 		protectedCellFormulas.remove(is);
 	}
 	
 	
 	/**
 	 *	Remove collection of protected cells from all related collections
 	 *
 	 * @param isCollection Collection of intersections
 	 */
 	public void removeAllProtectedCells(Collection<Intersection> protectedCells) {		
 
 		for (Intersection protectedCell:protectedCells) {
 			removeProtectedCell(protectedCell);
 		}
 	}
 	
 
 
 	public void setProtectedCellFormulas(Intersection is, Formula f) {
 		protectedCellFormulas.put(is, f);
 	}
 
 	public RuleGroup getRuleGroup() {
 		return ruleGroup;
 	}
 
 	public void setRuleGroup(RuleGroup ruleGroup) {
 		this.ruleGroup = ruleGroup;
 	}
 
 	public Rule getRule() {
 		return rule;
 	}
 
 	public String getMeasureName() {
 		if ( rule.getFormula().isResultFunction() ) {
 			return rule.getFormula().extractResultFunction().getMeasureName();
 		}
 		return rule.getFormula().getResultTerm();   
 	}
 
 	public MeasureType getMeasureType() {
 		return appDef.getMeasureDef(this.getMeasureName()).getType();
 	}
 
 	public void setRule(Rule rule) {
 		this.rule = rule;
 	}
 
 	
 //	public HashSet<Intersection> getAllocations() {
 //	return allocations;
 //	}
 
 	public Set<Intersection> getAllocationsByMsr(String msrName) {
 		if (allocationsByMsr.containsKey(msrName)) 
 			return allocationsByMsr.get(msrName);
 		return new HashSet<Intersection>(0);
 	}
 
 	public void addAllocation(Intersection is) {
 		if (!allocationsByMsr.containsKey(is.getCoordinate(msrDim)))
 			allocationsByMsr.put(is.getCoordinate(msrDim), new HashSet<Intersection>(LG_CHNG_LOAD));
 		allocationsByMsr.get(is.getCoordinate(msrDim)).add(is);
 	}
 
 	/**
 	 *	Remove intersection from allocation by measure collection
 	 *
 	 * @param is
 	 */
 	public void removeAllocation(Intersection is) {
 		String msrName = is.getCoordinate(getMsrDim());
 		if (allocationsByMsr.containsKey(msrName)) {
 			allocationsByMsr.get(msrName).remove(is);
 		}
 	}
 
 	public void addAllAllocations(Collection<Intersection> isList) {
 		for (Intersection is : isList) addAllocation(is);
 	}
 
 
 
 	public Long getStartTime() {
 		return startTime;
 	}
 
 	public void setStartTime(Long startTime) {
 		this.startTime = startTime;
 	}
 
 
 	public HashSet<Intersection> getForcedCalcs() {
 		return forcedCalcs;
 	}
 
 	public void setForcedCalcs(HashSet<Intersection> forcedCalcs) {
 		this.forcedCalcs = forcedCalcs;
 	}
 
 	public HashSet<Intersection> getRequiredCalcs() {
 		return requiredCalcs;
 	}
 
 	public void setRequiredCalcs(HashSet<Intersection> requiredCalcs) {
 		this.requiredCalcs = requiredCalcs;
 	}
 
 	public PafApplicationDef getAppDef() {
 		return appDef;
 	}
 
 	/**
 	 * @return Returns the dataCacheTrees.
 	 */
 	public MemberTreeSet getDataCacheTrees() {
 		return dataCacheTrees;
 	}
 
 	/**
 	 * @return Returns the measureRuleSet.
 	 */
 	public RuleSet getMeasureRuleSet() {
 		return measureRuleSet;
 	}
 
 	/**
 	 * @param measureRuleSet The measureRuleSet to set.
 	 */
 	public void setMeasureRuleSet(RuleSet measureRuleSet) {
 		this.measureRuleSet = measureRuleSet;
 	}
 
 	public void setStateChanged(boolean b) {
 		this.stateChanged = b;
 	}
 
 	public boolean isStateChanged() {
 		return this.stateChanged;
 	}
 
 	public PafClientState getClientState() {
 		return clientState;
 	}
 
 	public void setClientState(PafClientState clientState) {
 		this.clientState = clientState;
 	}
 
 	public void setCurrentTimeSlice(String timeSlice) {
 		currentTimeSlice = timeSlice;
 
 		PafDimMember prevMember = timeSubTree.getPrevSibling(getCurrentTimeMember(), false);
 		if (prevMember != null)
 			previousTimeSlice = prevMember.getKey();
 		else
 			previousTimeSlice = "";
 	}
 
 	public String getCurrentTimeSlice() {
 		return currentTimeSlice;
 	}
 
 	public String getPreviousTimeSlice() {
 		return previousTimeSlice;
 	}	
 
 	public Set<Intersection> getCurrentTimeBasedChanges() {
 		if (changedCellsByTime.get(currentTimeSlice) == null)
 			return new HashSet<Intersection>();
 		return changedCellsByTime.get(currentTimeSlice);
 	}
 	
 	public Set<Intersection> getCurrentTimeBasedLocks() {
 		if (origLockedCellsByTime.get(currentTimeSlice) == null)
 			return new HashSet<Intersection>();
 		return origLockedCellsByTime.get(currentTimeSlice);
 	}
 
 	public Set<Intersection> getPreviousTimeBasedChanges() {
 		if (changedCellsByTime.containsKey(previousTimeSlice))
 			return changedCellsByTime.get(previousTimeSlice);
 		return new HashSet<Intersection>(0);
 	}	
 
 	public PafDimMember getCurrentTimeMember() {
 		return timeSubTree.getMember(getCurrentTimeSlice());
 	}
 
 	public VersionDef getPlanVersion() {
 		return clientState.getPlanningVersion();
 	}
 
 	public void addChangedCell(Intersection is) {
 		addChangedCellByMsr(is);
 		addChangedCellByTime(is);
 		currentChangedCells.add(is);
 	}
 
 
 
 	public void addAllChangedCells(Set<Intersection> iss) {
 		for (Intersection is : iss) {
 			addChangedCell(is);
 		}
 	}    
 
 	private void addChangedCellByMsr(Intersection is) {
 		String msrName = is.getCoordinate(getMsrDim());
 		if (!changedCellsByMsr.containsKey(msrName)) {
 			changedCellsByMsr.put(msrName, new HashSet<Intersection>(LG_CHNG_LOAD));
 		}
 		changedCellsByMsr.get(msrName).add(is);
 	}
 
 	private void removeChangedCellByMsr(Intersection is) {
 		String msrName = is.getCoordinate(getMsrDim());
 		if (changedCellsByMsr.containsKey(msrName)) {
 			changedCellsByMsr.get(msrName).remove(is);
 		}
 	}
 
 	private void addChangedCellByTime(Intersection is) {
 		String timeName = TimeSlice.buildTimeHorizonCoord(is, appDef.getMdbDef());	// TTN-1595
 		if (!changedCellsByTime.containsKey(timeName)) {
 			changedCellsByTime.put(timeName, new HashSet<Intersection>(LG_CHNG_LOAD));
 		}
 		changedCellsByTime.get(timeName).add(is);
 
 	}
 	
 	private void removeChangedCellByTime(Intersection is) {
 		String timeName = TimeSlice.buildTimeHorizonCoord(is, appDef.getMdbDef());	// TTN-1595
 		if (changedCellsByTime.containsKey(timeName)) {
 			changedCellsByTime.get(timeName).remove(is);
 		}
 	}
 	
 	private void addOrigLockedCellByTime(Intersection is) {
 		String timeName = TimeSlice.buildTimeHorizonCoord(is, appDef.getMdbDef());	// TTN-1595
 		if (!origLockedCellsByTime.containsKey(timeName)) {
 			origLockedCellsByTime.put(timeName, new HashSet<Intersection>(LG_CHNG_LOAD));
 		}
 		origLockedCellsByTime.get(timeName).add(is);
 	}
 	
 	private void removeOrigLockedCellByTime(Intersection is) {
 		String timeName = TimeSlice.buildTimeHorizonCoord(is, appDef.getMdbDef());	// TTN-1595
 		if (origLockedCellsByTime.containsKey(timeName)) {
 			origLockedCellsByTime.get(timeName).remove(is);
 		}
 	}
 
 
 	/**
 	 *  Add changed cells to all changed, locked, and allocation intersection collections
 	 *
 	 * @param changedCells Changed Intersections
 	 */
 	public void addAllUserChanges(Collection<Intersection> changedCells) {
 
         for (Intersection is : changedCells) {
         	addUserChanges(is);
         }
     		
 	}
 
 	/**
 	 *  Add changed cell to all changed, locked, and allocation intersection collections
 	 *
 	 * @param is Changed Intersection
 	 */
 	public void addUserChanges(Intersection is) {
 
 		// Add intersection to collections
 		getOrigChangedCells().add(is);
 		addChangedCell(is);    
 		addOrigLockedCell(is);           
 		getCurrentLockedCells().add(is);         
 		addAllocation(is);          
 	}
 
 	
 
 	/**
 	 *  Remove changed cells from all changed, locked, and allocation intersection collections
 	 *
 	 * @param changedCells Changed Intersections
 	 */
 	public void removeAllUserChanges(Collection<Intersection> changedCells) {
 		
         for (Intersection is : changedCells) {
         	removeUserChanges(is);
         }
         		
 	}
 
 	/**
 	 *  Remove changed cell from all changed, locked, and allocation intersection collections
 	 *
 	 * @param changedCells Changed Intersections
 	 */
 	public void removeUserChanges(Intersection is) {
 		        	
 		// Remove intersection from collections
 		getOrigChangedCells().remove(is);
 		removeCurrentChangedCell(is);        
 		removeOrigLockedCell(is);        
 		getCurrentLockedCells().remove(is);           
 		removeAllocation(is);    
 	}
 
 
 	/**
 	 * @return Returns the changedCellsByMsr.
 	 */
 	public Map<String, Set<Intersection>> getChangedCellsByMsr() {
 		return changedCellsByMsr;
 	}
 
 	public Map<String, Set<Intersection>> getChangedCellsByTime() {  	
 		return changedCellsByTime;        
 	}    
 
 
 
 	/**
 	 *  Add changed contribution percent cells to all related collections
 	 *
 	 * @param changedCellValueMap
 	 */
 	public void addAllChangedContribPctCells(Map<Intersection, Double> changedCellValueMap) {
 		
 		for (Intersection is : changedCellValueMap.keySet()) {
 			addChangedContribPctCell(is, changedCellValueMap.get(is));
 		}
 	}
 
 
 	/**
 	 *  Add changed contribution percent cells to all related collections
 	 *
 	 * @param is Intersection
 	 * @param value 
 	 */
 	public void addChangedContribPctCell(Intersection is, Double value) {
 		addChangedContribPctCellByMsr(is);
 		addChangedContribPctCellByTime(is);
 		changedContribPctValueMap.put(is, value);
 	}
 
 	
 	/**
 	 *  Remove intersection collection from changed contribution percent cell collections
 	 *
 	 * @param isCollection Collection of intersections
 	 */
 	public void removeAllChangedContribPctCells(Collection<Intersection> isCollection) {
 		
 		Intersection[] changedCells = isCollection.toArray(new Intersection[0]);
 		for (Intersection is : changedCells) {
 			removeChangedContribPctCell(is);
 		}
 		
 	}
 
 	/**
 	 *  Remove intersection from changed contribution percent cell collections
 	 *
 	 * @param is Intersection
 	 */
 	public void removeChangedContribPctCell(Intersection is) {
 		removeChangedContribPctCellByMsr(is);
 		removeChangedContribPctCellByTime(is);
 		changedContribPctValueMap.remove(is);
 	}
 
 
 	/**
 	 *  Add intersection to collection changed contribution cells by measure
 	 *
 	 * @param is Intersection
 	 */
 	private void addChangedContribPctCellByMsr(Intersection is) {
 		
 		// Initialize measure entries
 		String measureName = is.getCoordinate(getMsrDim());
 		if (!changedContribPctCellsByMsr.containsKey(measureName)) {
 			changedContribPctCellsByMsr.put(measureName, new HashSet<Intersection>());
 		}
 		
 		// Add intersection to collection
 		changedContribPctCellsByMsr.get(measureName).add(is);
 		
 	}
 
 	/**
 	 *  Removed intersection from changed contribution percent by measure collection
 	 *
 	 * @param is
 	 */
 	private void removeChangedContribPctCellByMsr(Intersection is) {
 
 		// Get measure name
 		String measureName = is.getCoordinate(getMsrDim());
 		
 		// Remove intersection
 		if (changedContribPctCellsByMsr.containsKey(measureName)) {
 			changedContribPctCellsByMsr.get(measureName).remove(is);
 		}
 	}
 
 
 	/**
 	 *  Add intersection to collection of changed contribution cells by time
 	 *
 	 * @param is Intersection
 	 */
 	private void addChangedContribPctCellByTime(Intersection is) {
 		
 		// Initialize time entries
 		String periodName = TimeSlice.buildTimeHorizonCoord(is, appDef.getMdbDef());	// TTN-1595
 		if (!changedContribPctCellsByTime.containsKey(periodName)) {
 			changedContribPctCellsByTime.put(periodName, new HashSet<Intersection>());
 		}
 		
 		// Add intersection to collection
 		changedContribPctCellsByTime.get(periodName).add(is);
 		
 	}
 
 	/**
 	 *  Removed intersection from changed contribution percent by time period collection
 	 *
 	 * @param is
 	 */
 	private void removeChangedContribPctCellByTime(Intersection is) {
 		
 		// Get periods name
 		String periodName = TimeSlice.buildTimeHorizonCoord(is, appDef.getMdbDef());	// TTN-1595
 		
 		// Remove intersection
 		if (changedContribPctCellsByTime.containsKey(periodName)) {
 			changedContribPctCellsByTime.get(periodName).remove(is);
 		}
 	}
 
 
 
 	public String getPlanningVersionName() {
 		return clientState.getPlanningVersion().getName();
 	}
 
 
 	public String getFirstTimeSlice() {
 		if (timePeriodList != null && timePeriodList.size() > 0)
 			return timePeriodList.get(0);
 
 		return "";
 	}
 
 
 	/**
 	 * @return Returns the origChangedCells.
 	 */
 	public Set<Intersection> getOrigChangedCells() {
 		return origChangedCells;
 	}
 
 
 	/**
 	 * @return Returns the origLockedCells.
 	 */
 	public Set<Intersection> getOrigLockedCells() {
 		return origLockedCells;
 	}
 
 
 	/**
 	 * @return Returns the timeSliceMode.
 	 */
 	public boolean isTimeSliceMode() {
 		return timeSliceMode;
 	}
 
 
 	/**
 	 * @param timeSliceMode The timeSliceMode to set.
 	 */
 	public void setTimeSliceMode(boolean timeSliceMode) {
 		this.timeSliceMode = timeSliceMode;
 	}
 
 
 	public String getMsrDim() {
 		return msrDim;
 	}
 
 
 	public String getTimeDim() {
 		return timeDim;
 	}
 
 	public String getTimeHorizonDim() {
 		return PafBaseConstants.TIME_HORIZON_DIM;
 	}
 
 	public void setRoundingRules(Map<String, RoundingRule> rRules) {
 		roundingRules = rRules;
 	}
 
 	public Map<String, RoundingRule> getRoundingRules() {
 		return roundingRules;
 	}
 
 	public void setRoundingResourcePass(boolean roundingResourcePass) {
 		this.roundingResourcePass = roundingResourcePass;
 	}
 
 	public boolean isRoundingResourcePass() {
 		return roundingResourcePass;
 	}
 
 	public void setSkipRounding(boolean skipRounding) {
 		this.skipRounding = skipRounding;
 	}
 
 	public boolean isSkipRounding() {
 		return skipRounding;
 	}
 
 	public int getLoadFactor() {
 		return loadFactor;
 	}
 
 
 	public void setLoadFactor(int loadFactor) {
 		this.loadFactor = loadFactor;
 	}
 
 
 	public Map<Intersection, Double> getVarianceReplicationValues() {
 		return varianceReplicationValues;
 	}
 
 
 	public void setVarianceReplicationValues(
 			Map<Intersection, Double> varianceReplicationValues) {
 		this.varianceReplicationValues = varianceReplicationValues;
 	}
 
 
 	public PafDataCache getDataCache() {
 		return dataCache;
 	}
 
 
 	public void setDataCache(PafDataCache dataCache) {
 		this.dataCache = dataCache;
 	}
 
 
 
 
 	/**
 	 * @return the isAttributeEval
 	 */
 	public boolean isAttributeEval() {
 		return isAttributeEval;
 	}
 	/**
 	 * @param isAttriubteEval the isAttriubteEval to set
 	 */
 	public void setAttributeEval(boolean isAttributeEval) {
 		this.isAttributeEval = isAttributeEval;
 	}
 
 
 	/**
 	 * @return the isDefaultEvalStep
 	 */
 	public boolean isDefaultEvalStep() {
 		return isDefaultEvalStep;
 	}
 
 
 	/**
 	 * @param isDefaultEvalStep the isDefaultEvalStep to set
 	 */
 	public void setDefaultEvalStep(boolean isDefaultEvalStep) {
 		this.isDefaultEvalStep = isDefaultEvalStep;
 	}
 
 
 	/***
 	 * Gets the name of the version dimension. 
 	 * @return name of the version dimension
 	 */
 	public String getVersionDim() {
 		return versionDim;
 	}
 
 	/***
 	 * Gets a set of variance version names.
 	 * @return a set of variance version names.
 	 */
 	public List<String> getVarVersNames() {
 		return varianceVersionNames;
 	}
 
 
 	/**
 	 * @return the userLockedCells
 	 */
 	public Set<Intersection> getUserLocksAndChangedCells() {
 		return userLocksAndChangedCells;
 	}
 
 
 	/**
 	 * @return the hasContribPctFormulas
 	 */
 	public boolean hasContribPctFormulas() {
 		return hasContribPctFormulas;
 	}
 
 	/**
 	 * @param hasContribPctFormulas the hasContribPctFormulas to set
 	 */
 	public void setContribPctFormulas(boolean hasContribPctFormulas) {
 		this.hasContribPctFormulas = hasContribPctFormulas;
 	}
 
 
 	/**
 	 * @return the changedCcontribPctCells
 	 */
 	public Map<Intersection, Double> getChangedContribPctCells() {
 		return changedContribPctValueMap;
 	}
 
 	/**
 	 * @param changedContribPctCells the contribPctChangedCells to set
 	 */
 	public void setChangedContribPctCells(Map<Intersection, Double> changedContribPctCells) {
 		this.changedContribPctValueMap = changedContribPctCells;
 	}
 
 
 	/**
 	 * @return the changedContribPctCellsByMsr
 	 */
 	public Map<String, Set<Intersection>> getChangedContribPctCellsByMsr() {
 		return changedContribPctCellsByMsr;
 	}
 
 	/**
 	 * @param changedContribChangedCellsByMsr the changedContribPctCellsByMsr to set
 	 */
 	public void setChangedContribPctCellsByMsr(Map<String, Set<Intersection>> changedContribPctCellsByMsr) {
 		this.changedContribPctCellsByMsr = changedContribPctCellsByMsr;
 	}
 
 
 	/**
 	 * @return the changedContribPctCellsByTime
 	 */
 	public Map<String, Set<Intersection>> getChangedContribPctCellsByTime() {
 		return changedContribPctCellsByTime;
 	}
 
 	/**
 	 * @param changedContribChangedCellsByTime the changedContribPctCellsByTime to set
 	 */
 	public void setChangedContribPctCellsByTime(Map<String, Set<Intersection>> changedContribPctCellsByTime) {
 		this.changedContribPctCellsByTime = changedContribPctCellsByTime;
 	}
 
 
 	/**
 	 * @return the replicateAllContribPctCells
 	 */
 	public Set<Intersection> getReplicateAllContribPctCells() {
 		return replicateAllContribPctCells;
 	}
 
 	/**
 	 * @param replicateAllContribPctCells the replicateAllContribPctCells to set
 	 */
 	public void setReplicateAllContribPctCells(Set<Intersection> replicateAllContribPctCells) {
 		this.replicateAllContribPctCells = replicateAllContribPctCells;
 	}
 
 
 	/**
 	 * @return the replicateExistingContribPctCells
 	 */
 	public Set<Intersection> getReplicateExistingContribPctCells() {
 		return replicateExistingContribPctCells;
 	}
 
 	/**
 	 * @param replicateExistingContribPctCells the replicateExistingContribPctCells to set
 	 */
 	public void setReplicateExistingContribPctCells(Set<Intersection> replicateExistingContribPctCells) {
 		this.replicateExistingContribPctCells = replicateExistingContribPctCells;
 	}
 
 
 	/**
 	 * @return the axisAllocPriority
 	 */
 	public String[] getAxisAllocPriority() {
 		return axisAllocPriority;
 	}
 
 
 	/**
 	 * @param axisAllocPriority the axisAllocPriority to set
 	 */
 	public void setAxisAllocPriority(String[] axisAllocPriority) {
 		this.axisAllocPriority = axisAllocPriority;
 	}
 
 
 	/**
 	 * @return the axisSortPriority
 	 */
 	public String[] getAxisSortPriority() {
 		return axisSortPriority;
 	}
 
 	/**
 	 * @param axisSortPriority the axisSortPriority to set
 	 */
 	public void setAxisSortPriority(String[] axisSortPriority) {
 		this.axisSortPriority = axisSortPriority;
 	}
 
 
 	/**
 	 * @param dimSequence the dimSequence to set
 	 */
 	public void setDimSequence(String[] dimSequence) {
 		this.dimSequence = dimSequence;
 	}
 
 
 	/**
 	 * @return the dimSequence
 	 */
 	public String[] getDimSequence() {
 		return dimSequence;
 	}
 
 
 	/* (non-Javadoc)
 	 * @see java.lang.Object#clone()
 	 */
 	@Override
 	public EvalState clone() throws CloneNotSupportedException {
 
 		// Only a simple clone is currently being performed. The underlying objects are not getting cloned.
 		EvalState evalState = (EvalState) super.clone();
 
 
 		return evalState;
 	}
 
 	public Set<Intersection> getConsumedByRulegroup() {
 		return consumedByRulegroup;
 	}
 	public void addConsumedByRulegroup(Intersection is) {
 		consumedByRulegroup.add(is);
 	}
 	public void addConsumedByRulegroup(Collection<Intersection> c) {
 		consumedByRulegroup.addAll(c);
 	}
 	public void clearConsumedByRulegroup() {
 		consumedByRulegroup.clear();
 	}
 
 	public List<String> getTriggeredAggMsrs() {
 		return this.triggeredAggMsrs;
 	}
 
 
 	
 	/**
 	 * Returns the proper evaluation tree to use for the specified
 	 * uow dimension. 
 	 * 
 	 * @param dim Dimension name
 	 * @return Dim Tree
 	 */
 	public PafDimTree getEvaluationTree(String dim) {
 
 		PafDimTree evalTree = null;
 		
 		// Substitute the time horizon tree when the time dimension
 		// is specified, else return the specified dimension tree
 		if (!dim.equalsIgnoreCase(timeDim)) {
 			evalTree = this.getClientState().getUowTrees().getTree(dim);
 		} else {
 			evalTree =  this.getTimeSubTree();
 		}
 
 		return evalTree;
 	}
 	
 
 
 
 }
