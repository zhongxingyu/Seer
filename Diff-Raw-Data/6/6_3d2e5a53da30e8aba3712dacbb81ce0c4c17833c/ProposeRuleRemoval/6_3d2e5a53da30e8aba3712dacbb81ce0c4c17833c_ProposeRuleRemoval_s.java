 package actions;
 
 import agents.NomicAgent;
 
public class ProposeRuleRemoval extends TimeStampedAction {
 	NomicAgent agent;
 	
 	String oldRuleName, oldRulePackage;
 	
 	public ProposeRuleRemoval(NomicAgent agent, String oldRuleName, String oldRulePackage) {
 		this.agent = agent;
 		this.oldRuleName = oldRuleName;
 		this.oldRulePackage = oldRulePackage;
 	}
 
 	public NomicAgent getAgent() {
 		return agent;
 	}
 
 	public String getOldRuleName() {
 		return oldRuleName;
 	}
 
 	public String getOldRulePackage() {
 		return oldRulePackage;
 	}
 }
