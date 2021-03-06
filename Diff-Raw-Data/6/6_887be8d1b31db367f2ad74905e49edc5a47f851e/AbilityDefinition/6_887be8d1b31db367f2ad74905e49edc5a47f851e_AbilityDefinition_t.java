 package org.saga.abilities;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Hashtable;
 import java.util.Set;
 
 import org.bukkit.Material;
 import org.saga.SagaLogger;
 import org.saga.config.AbilityConfiguration;
 import org.saga.player.SagaPlayer;
 import org.saga.utility.TwoPointFunction;
 
 
 /**
  * Defines a profession.
  * 
  * @author andf
  *
  */
 public class AbilityDefinition{
 	
 	
 	/**
 	 * Ability class.
 	 */
 	private String className;
 	
 	/**
 	 * Ability name.
 	 */
 	private String name;
 
 	/**
 	 * Activation item.
 	 */
 	private HashSet<Material> activationItem;
 	
 	/**
 	 * Activation action.
 	 */
 	private ActivationAction activationAction;
 	
 	/**
 	 * Used item.
 	 */
 	private Material usedItem;
 	
 	/**
 	 * Used amount.
 	 */
 	private TwoPointFunction usedAmount;
 	
 	/**
 	 * Cooldown.
 	 */
 	private TwoPointFunction cooldown;
 	
 	/**
 	 * Active for.
 	 */
 	private TwoPointFunction active;
 
 	/**
 	 * Usage description.
 	 */
 	private String usage;
 	
 	/**
 	 * Description.
 	 */
 	private String description;
 
 	/**
 	 * Attribute requirements.
 	 */
 	private Hashtable<String, TwoPointFunction> attributeRequirements;
 
 	/**
 	 * Ability functions.
 	 */
 	private Hashtable<String, TwoPointFunction> functions;
 	
 	
 	
 	
 	// Initialisation:
 	/**
 	 * Used by gson.
 	 * 
 	 */
 	@SuppressWarnings("unused")
 	private AbilityDefinition() {
 	}
 
 	/**
 	 * Creates definition.
 	 * 
 	 * @param name name
 	 * @param materials materials
 	 * @param type type
 	 * @param abilities ability names
 	 */
 	public AbilityDefinition(String name, ArrayList<String> abilities) {
 		this.name = name;
 	}
 	
 	/**
 	 * Completes the definition.
 	 * 
 	 * @return integrity.
 	 */
 	public void complete() {
 
 		
 		if(className == null){
 			className = "invalid";
 			SagaLogger.nullField(this, "className");
 		}
 		
 		if(name == null){
 			name = "invalid";
 			SagaLogger.nullField(this, "name");
 		}
 		
 		if(activationItem == null){
 			activationItem = new HashSet<Material>();
 			SagaLogger.nullField(this, "activationItem");
 		}
 		
 		if(activationAction == null){
 			activationAction = ActivationAction.RIGHT_CLICK;
 			SagaLogger.nullField(this, "activationAction");
 		}
 		
 		if(usedItem == null){
 			usedItem = Material.AIR;
 			SagaLogger.nullField(this, "usedItem");
 		}
 		
 		if(usedAmount == null){
 			usedAmount = new TwoPointFunction(0.0);
 			SagaLogger.nullField(this, "usedAmount");
 		}
		usedAmount.complete();
 		
 		if(cooldown == null){
 			cooldown = new TwoPointFunction(0.0);
 			SagaLogger.nullField(this, "cooldown");
 		}
		cooldown.complete();
 		
 		if(active == null){
 			active = new TwoPointFunction(0.0);
 			SagaLogger.nullField(this, "active");
 		}
		active.complete();
		
 
 		if(usage == null){
 			usage = "";
 			SagaLogger.nullField(this, "usage");
 		}
 
 		if(description == null){
 			description = "";
 			SagaLogger.nullField(this, "description");
 		}
 		
 		if(attributeRequirements == null){
 			attributeRequirements = new Hashtable<String, TwoPointFunction>();
 			SagaLogger.nullField(this, "attributeRequirements");
 		}
 		Collection<TwoPointFunction> reqFunctions = attributeRequirements.values();
 		for (TwoPointFunction reqFunction : reqFunctions) {
 			reqFunction.complete();
 		}
 		
 		if(functions == null){
 			functions = new Hashtable<String, TwoPointFunction>();
 			SagaLogger.nullField(this, "functions");
 		}
 		Collection<TwoPointFunction> functionsElements = functions.values();
 		for (TwoPointFunction function : functionsElements) {
 			function.complete();
 		}
 		
 		
 	}
 
 	
 	
 	
 	// Interaction:
 	/**
 	 * Gets the class name.
 	 * 
 	 * @return class name
 	 */
 	public String getClassName() {
 		return className;
 	}
 	
 	/**
 	 * Gets ability name.
 	 * 
 	 * @return ability name.
 	 */
 	public String getName() {
 		return name;
 	}
 	
 	
 	/**
 	 * Gets the activation items.
 	 * 
 	 * @return activation item
 	 */
 	public HashSet<Material> getActivationItems() {
 		return activationItem;
 	}
 	
 	/**
 	 * Gets the activationAction.
 	 * 
 	 * @return the activationAction
 	 */
 	public ActivationAction getActivationAction() {
 		return activationAction;
 	}
 	
 
 	/**
 	 * Gets the used item.
 	 * 
 	 * @return used item, AIR if none
 	 */
 	public Material getUsedItem() {
 		return usedItem;
 	}
 
 	/**
 	 * Gets the amount of used material.
 	 * 
 	 * @param level level
 	 * @return amount of used material
 	 */
 	public Integer getUsedAmount(Integer level) {
 		return usedAmount.randomIntValue(level);
 	}
 	
 	/**
 	 * Gets the amount of used material.
 	 * 
 	 * @param level level
 	 * @return amount of used material
 	 */
 	public Integer getMaxAmount(Integer level) {
 		return (int)Math.ceil(usedAmount.value(level));
 	}
 	
 	/**
 	 * Gets the cooldown.
 	 * 
 	 * @return the cooldown
 	 */
 	public Integer getCooldown(Integer level) {
 		return cooldown.value(level).intValue();
 	}
 	
 	/**
 	 * Gets the active for.
 	 * 
 	 * @return the active for
 	 */
 	public Integer getActiveFor(Integer level) {
 		return active.value(level).intValue();
 	}
 
 	
 	/**
 	 * Checks ability attribute requirements.
 	 * 
 	 * @param sagaPlayer saga player
 	 * @param abilityScore ability score
 	 * @return true if requirements are met
 	 */
 	public boolean checkAttributes(SagaPlayer sagaPlayer, Integer abilityScore) {
 
 
 		Set<String> attributeNames = attributeRequirements.keySet();
 		
 		for (String attrName : attributeNames) {
 			
 			if(sagaPlayer.getAttributeScore(attrName) < attributeRequirements.get(attrName).intValue(abilityScore)){
 				return false;
 			}
 			
 		}
 		
 		return true;
 		
 
 	}
 
 	/**
 	 * Gets attribute requirement.
 	 * 
 	 * @param attribute attribute
 	 * @param abilityScore ability score
 	 * @return ability attribute requirement
 	 */
 	public Integer getAttrReq(String attribute, Integer abilityScore) {
 
 		
 		TwoPointFunction function = attributeRequirements.get(attribute);
 		if(function == null) return 0;
 		
 		return function.intValue(abilityScore);
 		
 		
 	}
 	
 	/**
 	 * Gets a function for the given key.
 	 * 
 	 * @param key key
 	 * @return function for the given key, 0 if none
 	 */
 	public TwoPointFunction getFunction(String key) {
 
 		TwoPointFunction function = functions.get(key);
 		
 		if(function == null){
 			SagaLogger.severe(this, "failed to retrive function for " + key + " key");
 			return new TwoPointFunction(0.0);
 		}
 		
 		return function;
 
 	}
 	
 	/**
 	 * Gets players ability score.
 	 * 
 	 * @param sagaPlayer saga player
 	 * @return ability score
 	 */
 	public Integer getScore(SagaPlayer sagaPlayer) {
 
 		
 		int prevScore = 0;
 		
 		for (int score = 1; score <= AbilityConfiguration.config().maxAbilityScore; score++) {
 			
 			if(!checkAttributes(sagaPlayer, score)) return prevScore;
 			prevScore = score;
 			
 		}
 		
 		return prevScore;
 		
 		
 	}
 	
 	
 	// Info:
 	/**
 	 * Gets the description.
 	 * 
 	 * @return the description
 	 */
 	public String getDescription() {
 		return description;
 	}
 
 	/**
 	 * Gets the usage description.
 	 * 
 	 * @return the usage description
 	 */
 	public String getUsage() {
 		return usage;
 	}
 
 	
 	// Other:
 	/* 
 	 * (non-Javadoc)
 	 * 
 	 * @see java.lang.Object#toString()
 	 */
 	@Override
 	public String toString() {
 		return name;
 	}
 	
 	
 	// Types:
 	public enum ActivationAction{
 		
 		LEFT_CLICK,
 		RIGHT_CLICK,
 		NONE;
 		
 		public String getShortName() {
 
 			return name().toLowerCase().replace("_", "").replace("left", "l").replace("right", "r");
 			
 		}
 		
 	}
 	
 }
