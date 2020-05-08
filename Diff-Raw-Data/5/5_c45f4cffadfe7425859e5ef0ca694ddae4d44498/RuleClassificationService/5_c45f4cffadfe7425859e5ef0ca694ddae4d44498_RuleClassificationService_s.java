 package services;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.apache.log4j.Logger;
 import org.drools.runtime.StatefulKnowledgeSession;
 
 import uk.ac.imperial.presage2.core.environment.EnvironmentService;
 import uk.ac.imperial.presage2.core.environment.EnvironmentSharedStateAccess;
 
 import com.google.inject.Inject;
 
 import enums.RuleFlavor;
 import facts.RuleDefinition;
 
 public class RuleClassificationService extends EnvironmentService {
 	private final Logger logger = Logger.getLogger(this.getClass());
 	
 	private final StatefulKnowledgeSession session;
 	
 	private Map<String, RuleDefinition> RulePool = new HashMap<String, RuleDefinition>();
 	
 	@Inject
 	public RuleClassificationService(EnvironmentSharedStateAccess ss,
 			StatefulKnowledgeSession session) {
 		super(ss);
 		this.session = session;
 		
 		RuleStringRepository ruleRepo = new RuleStringRepository();
 		for (RuleDefinition definition : ruleRepo.getRules()) {
 			RulePool.put(definition.getName(), definition);
 		}
 	}
 	
 	public void LoadRuleDefinitions(Collection<RuleDefinition> collection) {
 		for (RuleDefinition definition : collection) {
 			RuleDefinition localDef = RulePool.get(definition.getName());
 			
 			localDef.setActive(definition.isActive());
 		}
 	}
 	
 	public RuleFlavor getFlavor(String ruleName) {
 		return RulePool.get(ruleName).getPrevailingFlavor();
 	}
 	
 	public Map<RuleFlavor, Integer> getFlavors(String ruleName) {
 		return RulePool.get(ruleName).getFlavors();
 	}
 	
 	public Collection<RuleDefinition> getAllRules() {
 		return RulePool.values();
 	}
 	
 	public RuleDefinition getRule(String ruleName) {
 		return RulePool.get(ruleName);
 	}
 	
 	public RuleDefinition getAnyRuleWithHighestFlavor(RuleFlavor flavorType) {
 		RuleDefinition bestRuleDef = null;
 		
 		for (String name : RulePool.keySet()) {
 			RuleDefinition currentRuleDef = RulePool.get(name);
 			if (bestRuleDef == null || currentRuleDef.getFlavorAmount(flavorType) > bestRuleDef.getFlavorAmount(flavorType)) {
 				bestRuleDef = currentRuleDef;
 			}
 		}
 		
 		return bestRuleDef;
 	}
 	
 	public RuleDefinition getActiveRuleWithHighestFlavor(RuleFlavor flavorType) {
 		RuleDefinition bestRuleDef = null;
 		
 		for (String name : RulePool.keySet()) {
 			RuleDefinition currentRuleDef = RulePool.get(name);
 			if (currentRuleDef.isActive() && (bestRuleDef == null || currentRuleDef.getFlavorAmount(flavorType) > bestRuleDef.getFlavorAmount(flavorType))) {
 				bestRuleDef = currentRuleDef;
 			}
 		}
 		
 		return bestRuleDef;
 	}
 	
 	public RuleDefinition getInActiveRuleWithHighestFlavor(RuleFlavor flavorType) {
 		RuleDefinition bestRuleDef = null;
 		
 		for (String name : RulePool.keySet()) {
 			RuleDefinition currentRuleDef = RulePool.get(name);
 			if (!currentRuleDef.isActive() && (bestRuleDef == null || currentRuleDef.getFlavorAmount(flavorType) > bestRuleDef.getFlavorAmount(flavorType))) {
 				bestRuleDef = currentRuleDef;
 			}
 		}
 		
 		return bestRuleDef;
 	}
 	
 	public ArrayList<RuleDefinition> getAllRulesWithFlavor(RuleFlavor flavorType) {
 		ArrayList<RuleDefinition> rules = new ArrayList<RuleDefinition>();
 		for (String name : RulePool.keySet()) {
 			RuleDefinition currentRule = RulePool.get(name);
 			if (currentRule.getFlavorAmount(flavorType) > 50) {
 				rules.add(currentRule);
 			}
 		}
 		
 		return rules;
 	}
 	
 	public ArrayList<RuleDefinition> getAllActiveRulesWithLowFlavor(RuleFlavor flavor) {
 		ArrayList<RuleDefinition> rules = new ArrayList<RuleDefinition>();
 		
 		for (String name : RulePool.keySet()) {
 			RuleDefinition current = RulePool.get(name);
 			if (current.isActive() && current.isNot(flavor))
 				rules.add(current);
 		}
 		
 		return rules;
 	}
 	
 	public ArrayList<RuleDefinition> getAllActiveRulesWithFlavor(RuleFlavor flavor) {
 		ArrayList<RuleDefinition> rules = new ArrayList<RuleDefinition>();
 		
 		for (String name : RulePool.keySet()) {
 			RuleDefinition current = RulePool.get(name);
 			if (current.isActive() && current.getFlavorAmount(flavor) > 50)
 				rules.add(current);
 		}
 		
 		return rules;
 	}
 	
 	public ArrayList<RuleDefinition> getAllActiveRulesWithFlavors(ArrayList<RuleFlavor> flavors) {
 		ArrayList<RuleDefinition> rules = new ArrayList<RuleDefinition>();
 		
 		for (RuleFlavor flavor : flavors) {
 			rules.addAll(getAllActiveRulesWithFlavor(flavor));
 		}
 		
 		return rules;
 	}
 	
 	public ArrayList<RuleDefinition> getAllInActiveRulesWithFlavor(RuleFlavor flavor) {
 		ArrayList<RuleDefinition> rules = new ArrayList<RuleDefinition>();
 		
 		for (String name : RulePool.keySet()) {
 			RuleDefinition current = RulePool.get(name);
 			if (!current.isActive() && current.getFlavorAmount(flavor) > 50)
 				rules.add(current);
 		}
 		
 		return rules;
 	}
 	
 	public ArrayList<RuleDefinition> getAllInActiveRulesWithFlavors(Collection<RuleFlavor> flavors) {
 		ArrayList<RuleDefinition> rules = new ArrayList<RuleDefinition>();
 		
 		for (RuleFlavor flavor : flavors) {
 			rules.addAll(getAllInActiveRulesWithFlavor(flavor));
 		}
 		
 		return rules;
 	}
 	
 	public ArrayList<RuleDefinition> getRulesThatModify(String oldRuleName) {
 		ArrayList<RuleDefinition> rules = new ArrayList<RuleDefinition>();
 		
 		for (String name : RulePool.keySet()) {
 			RuleDefinition definition = RulePool.get(name);
 			if (definition.isReplaces(oldRuleName))
 				rules.add(definition);
 		}
 		
 		return rules;
 	}
 	
 	public ArrayList<RuleDefinition> getAllInActiveRules() {
 		ArrayList<RuleDefinition> rules = new ArrayList<RuleDefinition>();
 		
 		for (String name : RulePool.keySet()) {
 			RuleDefinition definition = RulePool.get(name);
 			if (!definition.isActive())
 				rules.add(definition);
 		}
 		
 		return rules;
 	}
 	
 	public ArrayList<RuleDefinition> getAllActiveRulesWithoutFlavors(ArrayList<RuleFlavor> flavors) {
 		ArrayList<RuleDefinition> rules = new ArrayList<RuleDefinition>();
 		
 		for (String name : RulePool.keySet()) {
 			RuleDefinition definition = RulePool.get(name);
			if (definition.isHasPositiveForFlavors(flavors))
 				rules.add(definition);
 		}
 		
 		return rules;
 	}
 	
 	public ArrayList<RuleDefinition> getInActiveRulesThatReplaceActiveRulesWithFlavor(RuleFlavor flavor) {
 		ArrayList<RuleDefinition> rulesWeWantToReplace = getAllActiveRulesWithFlavor(flavor);
 		
 		ArrayList<RuleDefinition> rules = new ArrayList<RuleDefinition>();
 		
 		for (String name : RulePool.keySet()) {
 			RuleDefinition definition = RulePool.get(name);
 			
			if (definition.isReplacesAny(rulesWeWantToReplace))
 				rules.add(definition);
 		}
 		
 		return rules;
 	}
 	
 	public ArrayList<RuleDefinition> getInActiveRulesThatReplaceActiveRulesWithFlavors(ArrayList<RuleFlavor> flavors) {
 		ArrayList<RuleDefinition> rules = new ArrayList<RuleDefinition>();
 		
 		for (RuleFlavor flavor : flavors) {
 			rules.addAll(getInActiveRulesThatReplaceActiveRulesWithFlavor(flavor));
 		}
 		
 		return rules;
 	}
 	
 	public String getRuleBody(String ruleName) {
 		return RulePool.get(ruleName).getRuleContent();
 	}
 	
 	public boolean isActive(String ruleName) {
 		return RulePool.get(ruleName).isActive();
 	}
 	
 	public void setActive(String ruleName, boolean active) {
 		logger.info("Setting rule " + ruleName + " activity to " + active);
 		
 		RulePool.get(ruleName).setActive(active);
 	}
 }
