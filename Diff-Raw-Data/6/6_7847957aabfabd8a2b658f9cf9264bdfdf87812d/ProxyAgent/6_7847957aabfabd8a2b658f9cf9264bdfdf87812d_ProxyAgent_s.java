 package agents;
 
 import java.util.ArrayList;
 import java.util.UUID;
 
 import actions.ProposeRuleChange;
 import enums.RuleFlavor;
 import enums.VoteType;
 import facts.RuleDefinition;
 
 public class ProxyAgent extends NomicAgent {
 	
 	private NomicAgent owner;
 	
 	private boolean Winner = false;
 	
 	private Integer preference = 50;
 	
 	private boolean preferenceLocked = false;
 	
 	private boolean avatar;
 
 	public ProxyAgent(UUID id, String name) {
 		super(id, name);
 		// TODO Auto-generated constructor stub
 	}
 	
 	@Override
 	public void Win() {
 		Winner = true;
 	}
 	
 	public void setOwner(NomicAgent owner) {
 		this.owner = owner;
 	}
 	
 	public NomicAgent getOwner() {
 		return owner;
 	}
 	
 	public UUID GetOwnerID() {
 		return owner.getID();
 	}
 	
 	public boolean isWinner() {
 		return Winner;
 	}
 
 	public void setWinner(boolean winner) {
 		Winner = winner;
 	}
 	
 	@Override
 	public void incrementTime() {
 		logger.info("EPIC PROXY LOLZ");
 		
 		super.incrementTime();
 	}
 	
 	@Override
 	public String getProxyRulesFile() {
 		return owner.getProxyRulesFile();
 	}
 	
 	@Override
 	protected ProposeRuleChange chooseProposal() {
 		// Random proposal strategy for subsims, since we're actually testing the first rule change
 		// but we need the simulation to proceed, which requires proposals
 		if (rand.nextBoolean()) {
 			ArrayList<RuleDefinition> rules = ruleClassificationService.getAllInActiveRules();
 			
 			if (!rules.isEmpty()) {
 				RuleDefinition definition = rules.get(rand.nextInt(rules.size()));
 				
 				return definition.getRuleChange(this);
 			}
 		}
 		else {
 			ArrayList<RuleFlavor> flavors = new ArrayList<RuleFlavor>();
 			flavors.add(RuleFlavor.STABLE);
 			
 			ArrayList<RuleDefinition> rules = ruleClassificationService.getAllActiveRulesWithoutFlavors(flavors);
 			
 			if (!rules.isEmpty()) {
 				RuleDefinition definition = rules.get(rand.nextInt(rules.size()));
 				
				return definition.getRuleChange(this);
 			}
 		}
 		
 		return super.chooseProposal();
 	}
 	
 	@Override
 	public VoteType chooseVote(ProposeRuleChange ruleChange) {
 		return super.chooseVote(ruleChange);
 	}
 	
 	/**
 	 * Returns true if this agent is the representative of the agent that created
 	 * the subsimulation.
 	 * @return
 	 */
 	public boolean IsAvatar() {
 		return avatar;
 	}
 	
 	public void SetAvatar(boolean avatar) {
 		this.avatar = avatar;
 	}
 
 	public Integer getPreference() {
 		return preference;
 	}
 
 	public void setPreference(Integer preference) {
 		if (!preferenceLocked)
 			this.preference = preference;
 	}
 	
 	public void increasePreference(Integer amount) {
 		if (!preferenceLocked)
 			this.preference += amount;
 	}
 	
 	public void decreasePreference(Integer amount) {
 		if (!preferenceLocked)
 			this.preference -= amount;
 	}
 
 	public boolean isPreferenceLocked() {
 		return preferenceLocked;
 	}
 
 	public void setPreferenceLocked(boolean preferenceLocked) {
 		this.preferenceLocked = preferenceLocked;
 	}
 }
