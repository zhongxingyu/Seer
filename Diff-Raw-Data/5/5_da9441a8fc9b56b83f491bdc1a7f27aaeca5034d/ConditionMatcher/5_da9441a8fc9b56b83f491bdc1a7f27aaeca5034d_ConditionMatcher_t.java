 package org.rulemaker.engine.matcher;
 
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.commons.beanutils.PropertyUtils;
 import org.rulemaker.engine.EngineContext;
 import org.rulemaker.engine.matcher.exception.MatchingException;
 import org.rulemaker.model.Condition;
 import org.rulemaker.model.Term;
 
 
 class ConditionMatcher {
 	
 	private EngineContext engineContext; 
 	
 	public ConditionMatcher() {
 		super();
 	}
 	
 	protected EngineContext getEngineContext() {
 		return engineContext;
 	}
 
 	public void setEngineContext(EngineContext engineContext) {
 		this.engineContext = engineContext;
 	}
 
 	public boolean matches(Object object, Condition condition) throws MatchingException {
 		Map<String, Object> globalVariablesMap = getEngineContext().getGobalVariablesMap();
 		Map<String, Object> objectMembersMap = buildMapFromObjectMembers(object);
 		// Add current object members to global variables map to make them available
 		// from expressions
 		globalVariablesMap.putAll(objectMembersMap);
 		boolean matches = true;
 		List<Term> conditionTerms = condition.getTermsList();
 		Iterator<Term> iterator = conditionTerms.iterator();
 		while(matches && iterator.hasNext()) {
 			Term currentTerm = iterator.next();
 			TermMatcher termMatcher = TermMatcher.Factory.buildTermMatcher(engineContext , currentTerm);
 			matches = termMatcher.matches(object);
 		}
 		// Remove object members due they are no longer available
 		removeKeysFromMap(globalVariablesMap, objectMembersMap.keySet());
 		return matches;
 	}
 	
	private Map<String, Object> buildMapFromObjectMembers(Object object) throws MatchingException{
 		try {
 			@SuppressWarnings("unchecked")
 			Map<String, Object> map = PropertyUtils.describe(object);
 			map.remove("class");
 			return map;
 		} catch (Exception e) {
			throw new MatchingException(e);
 		}
 	}
 	
 	private void removeKeysFromMap(Map<String, Object> map, Set<String> keysToRemove) {
 		for (String aKey : keysToRemove) {
 			map.remove(aKey);
 		}
 	}
 }
