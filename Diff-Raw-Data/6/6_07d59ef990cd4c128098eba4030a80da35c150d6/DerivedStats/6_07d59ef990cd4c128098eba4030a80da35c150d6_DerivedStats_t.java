 package org.andfRa.mythr.player;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.Random;
 
 import org.andfRa.mythr.Mythr;
 import org.andfRa.mythr.MythrLogger;
 import org.andfRa.mythr.config.AttributeConfiguration;
 import org.andfRa.mythr.config.ResponseConfiguration;
 import org.andfRa.mythr.config.SkillConfiguration;
 import org.andfRa.mythr.config.VanillaConfiguration;
 import org.andfRa.mythr.items.ItemType;
 import org.andfRa.mythr.items.MythrItem;
 import org.andfRa.mythr.responses.Response;
 import org.andfRa.mythr.util.LinearFunction;
 import org.bukkit.Material;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.EntityEquipment;
 
 public class DerivedStats {
 
 
 	/** Random number generator. */
 	public static Random RANDOM = new Random();
 	
 	/** Derived stats for default creatures. */
 	public static DerivedStats DEFAULT_CREATURE_STATS = new DerivedStats(); // TODO: This must be private.
 	
 	
 	/** Minimum base damage. */
 	private int minBaseDmg = 0;
 
 	/** Maximum base damage. */
 	private int maxBaseDmg = 0;
 
 
 	/** Melee damage modifier. */
 	private int meleeDmgMod = 0;
 
 	/** Ranged damage modifier. */
 	private int rangedDmgMod = 0;
 
 	/** Magic damage modifier. */
 	private int magicDmgMod = 0;
 
 	/** Curse damage modifier. */
 	private int curseDmgMod = 0;
 
 	/** Blessing damage modifier. */
 	private int blessingDmgMod = 0;
 
 	
 	/** Melee attack rating. */
 	private int meleeAR = 0;
 
 	/** Ranged attack rating. */
 	private int rangedAR = 0;
 
 	/** Magic attack rating. */
 	private int magicAR = 0;
 
 	/** Curse attack rating. */
 	private int curseAR = 0;
 
 	/** Blessing attack rating. */
 	private int blessingAR = 0;
 
 
 	/** Armour induced damage multiplier. */
 	private double armour = 1.0;
 
 	
 	/** Armour defence rating. */
 	private int armourDR = 0;
 
 
 	/** Players raw attributes. */
 	private int[] attributes = new int[AttributeConfiguration.getAttribCount()];
 
 	/** Players raw attributes. */
 	private int[] skills = new int[SkillConfiguration.getSkillCount()];
 
 	
 	// CONSTRUCTION:
 	/**
 	 * Calculates the derived stats.
 	 * 
 	 * @param living living entity
 	 */
 	public DerivedStats() {
 		
 	}
 
 	/**
 	 * Updates derived stats.
 	 * 
 	 * @param attribScores attribute scores
 	 * @param skillScores skill scores
 	 * @param perks perks
 	 * @param equipment equipment
 	 */
 	public void update(Map<String, Integer> attribScores, Map<String, Integer> skillScores, List<String> perks, EntityEquipment equipment)
 	 {
 		// Reset:
 		reset();
 		
 		// Equipment:
 		MythrItem mweapon = null;
 		MythrItem mhelmet = null;
 		MythrItem mchestplate = null;
 		MythrItem mleggings = null;
 		MythrItem mboots = null;
 		
 		if(equipment.getItemInHand() != null) mweapon = MythrItem.fromBukkitItem(equipment.getItemInHand());
 		if(equipment.getHelmet() != null) mhelmet = MythrItem.fromBukkitItem(equipment.getHelmet());
 		if(equipment.getChestplate() != null) mchestplate = MythrItem.fromBukkitItem(equipment.getChestplate());
 		if(equipment.getLeggings() != null) mleggings = MythrItem.fromBukkitItem(equipment.getLeggings());
 		if(equipment.getBoots() != null) mboots = MythrItem.fromBukkitItem(equipment.getBoots());
 		
 		// Update:
 		updateStats(attribScores, skillScores, mweapon, mhelmet, mchestplate, mleggings, mboots);
 		updateReponses(perks, mweapon, mhelmet, mchestplate, mleggings, mboots);
 		updateWeapon(mweapon);
 		updateArmour(mhelmet, mchestplate, mleggings, mboots);
 	 }
 	
 	/**
 	 * Resets derived stats.
 	 * 
 	 */
 	private void reset()
 	 {
 		// Stats:
 		attributes = new int[AttributeConfiguration.getAttribCount()];
 		skills = new int[SkillConfiguration.getSkillCount()];
 		
 		// Passives:
 		
 		
 		// Weapon:
 		minBaseDmg = 0;
 		maxBaseDmg = 0;
 		
		meleeDmgMod = 0;
		rangedDmgMod = 0;
		magicDmgMod = 0;
		curseDmgMod = 0;
		blessingDmgMod = 0;
		
 		meleeAR = 1;
 		rangedAR = 1;
 		magicAR = 1;
 		curseAR = 1;
 		blessingAR = 1;
 		
 		// Armour:
 		armour = 1.0;
 		armourDR = 0;
 		
 	 }
 	
 	
 	/**
 	 * Updates player reponses.
 	 * 
 	 * @param perks perks
 	 * @param mweapon weapon Mythr item
 	 * @param mhelmet helmet Mythr item, null if none
 	 * @param mchestplate chestplate Mythr item, null if none
 	 * @param mleggings leggings Mythr item, null if none
 	 * @param mboots boots Mythr item, null if none
 	 */
 	private void updateReponses(List<String> perks, MythrItem mweapon, MythrItem mhelmet, MythrItem mchestplate, MythrItem mleggings, MythrItem mboots)
 	 {
 		
 		// Gather responses:
 		ArrayList<String> responses = new ArrayList<String>();
 		for (String perk : perks) {
 			responses.add(perk);
 		}
 		if(mweapon != null) responses.addAll(mweapon.getResponses());
 		if(mhelmet != null) responses.addAll(mhelmet.getResponses());
 		if(mchestplate != null) responses.addAll(mchestplate.getResponses());
 		if(mleggings != null) responses.addAll(mleggings.getResponses());
 		if(mboots != null) responses.addAll(mboots.getResponses());
 		
 		// Trigger passives:
 		Response response = null;
 		for (String respName : responses) {
 			response = ResponseConfiguration.getResponse(respName);
 			if(response == null) continue;
 			response.passiveTrigger(this);
 		}
 		
 	 }
 
 	/**
 	 * Updates player stats.
 	 * 
 	 * @param attribScores attribute scores
 	 * @param skillScores skill scores
 	 * @param mweapon weapon Mythr item
 	 * @param mhelmet helmet Mythr item, null if none
 	 * @param mchestplate chestplate Mythr item, null if none
 	 * @param mleggings leggings Mythr item, null if none
 	 * @param mboots boots Mythr item, null if none
 	 */
 	private void updateStats(Map<String, Integer> attribScores, Map<String, Integer> skillScores, MythrItem mweapon, MythrItem mhelmet, MythrItem mchestplate, MythrItem mleggings, MythrItem mboots)
 	 {
 		
 		// Attributes:
 		String[] attribNames = AttributeConfiguration.getAttrNames();
 		for (int i = 0; i < attribNames.length; i++) {
 			Integer score = attribScores.get(attribNames[i]);
 			if(score == null) continue;
 			attributes[i] = score;
 		}
 
 		// Skills:
 		String[] skillNames = SkillConfiguration.getSkillNames();
 		for (int i = 0; i < skillNames.length; i++) {
 			Integer score = skillScores.get(skillNames[i]);
 			if(score == null) continue;
 			skills[i] = score;
 		}
 	 }
 	
 	/**
 	 * Update all weapon stats.
 	 * 
 	 * @param mweapon weapon Mythr item, null if none
 	 */
 	private void updateWeapon(MythrItem mweapon)
 	 {
 		// Weapon item:
 		if(mweapon != null){
 			
 			switch (mweapon.getType()) {
 			case MELEE_WEAPON:
 				meleeAR+= mweapon.getAttackRating();
 				break;
 			
 			case RANGED_WEAPON:
 				rangedAR+= mweapon.getAttackRating();
 				break;
 			
 			case ARCANE_SPELL:
 				magicAR+= mweapon.getAttackRating();
 				break;
 			
 			case CURSE_SPELL:
 				curseAR+= mweapon.getAttackRating();
 				break;
 				
 			case BLESSING_SPELL:
 				blessingAR+= mweapon.getAttackRating();
 				break;
 
 			default:
 				break;
 			}
 			
 			minBaseDmg = mweapon.getDamageMin();
 			maxBaseDmg = mweapon.getDamageMax();
 		}
 		// No weapon item:
 		else{
 			minBaseDmg = VanillaConfiguration.DEFAULT_DAMAGE;
 			maxBaseDmg = VanillaConfiguration.DEFAULT_DAMAGE;
 		}
 
 		// Attributes:
 		Attribute[] attributes = AttributeConfiguration.getAttributes();
 		for (int i = 0; i < attributes.length; i++) {
 			int score = getAttribScore(i);
 			
 			meleeDmgMod+= attributes[i].getSpecifier(Specifier.MELEE_ATTACK_DAMAGE_MODIFIER, score);
 			rangedDmgMod+= attributes[i].getSpecifier(Specifier.RANGED_ATTACK_DAMAGE_MODIFIER, score);
 			magicDmgMod+= attributes[i].getSpecifier(Specifier.MAGIC_ATTACK_DAMAGE_MODIFIER, score);
 			curseDmgMod+= attributes[i].getSpecifier(Specifier.CURSE_ATTACK_DAMAGE_MODIFIER, score);
 			blessingDmgMod+= attributes[i].getSpecifier(Specifier.BLESSING_ATTACK_DAMAGE_MODIFIER, score);
 		}
 		
 		// Skills:
 		Skill[] skills = SkillConfiguration.getSkills();
 		for (int i = 0; i < skills.length; i++) {
 			int score = getSkillScore(i);
 			
 			meleeAR+= skills[i].getSpecifier(Specifier.MELEE_ATTACK_RATING_MODIFIER, score);
 			rangedAR+= skills[i].getSpecifier(Specifier.RANGED_ATTACK_RATING_MODIFIER, score);
 			magicAR+= skills[i].getSpecifier(Specifier.MAGIC_ATTACK_RATING_MODIFIER, score);
 			curseAR+= skills[i].getSpecifier(Specifier.CURSE_ATTACK_RATING_MODIFIER, score);
 			blessingAR+= skills[i].getSpecifier(Specifier.BLESSING_ATTACK_RATING_MODIFIER, score);
 		}
 	 }
 
 	/**
 	 * Updates all armour stats.
 	 * 
 	 * @param mhelmet helmet Mythr item, null if none
 	 * @param mchestplate chestplate Mythr item, null if none
 	 * @param mleggings leggings Mythr item, null if none
 	 * @param mboots boots Mythr item, null if none
 	 */
 	private void updateArmour(MythrItem mhelmet, MythrItem mchestplate, MythrItem mleggings, MythrItem mboots)
 	 {
 		Material material;
 		
 		int lightDR = 0;
 		int heavyDR = 0;
 		int exoticDR = 0;
 		
 		double light = 0.0;
 		double heavy = 0.0;
 		double exotic = 0.0;
 		
 		// Helmet:
 		if(mhelmet != null){
 			material = mhelmet.getMaterial();
 			
 			switch (mhelmet.getType()) {
 			case LIGHT_ARMOUR:
 				lightDR+= mhelmet.getDefenceRating();
 				light+= VanillaConfiguration.HELMET_RATIO;
 				break;
 			
 			case HEAVY_ARMOUR:
 				heavyDR+= mhelmet.getDefenceRating();
 				heavy+= VanillaConfiguration.HELMET_RATIO;
 				break;
 
 			case EXOTIC_ARMOUR:
 				exoticDR+= mhelmet.getDefenceRating();
 				exotic+= VanillaConfiguration.HELMET_RATIO;
 				break;
 				
 			default:
 				break;
 			}
 			
 			switch (material) {
 			case LEATHER_HELMET:
 				armour+= VanillaConfiguration.LEATHER_HELMET_MULTIPLIER;
 				break;
 			
 			case GOLD_HELMET:
 				armour+= VanillaConfiguration.GOLD_HELMET_MULTIPLIER;
 				break;
 
 			case CHAINMAIL_HELMET:
 				armour+= VanillaConfiguration.CHAINMAIL_HELMET_MULTIPLIER;
 				break;
 
 			case IRON_HELMET:
 				armour+= VanillaConfiguration.IRON_HELMET_MULTIPLIER;
 				break;
 			
 			case DIAMOND_HELMET:
 				armour+= VanillaConfiguration.DIAMOND_HELMET_MULTIPLIER;
 				break;
 				
 			default:
 				break;
 			}
 		}
 		
 		// Chestplate:
 		if(mchestplate != null){
 			material = mchestplate.getMaterial();
 
 			switch (mchestplate.getType()) {
 			case LIGHT_ARMOUR:
 				lightDR+= mchestplate.getDefenceRating();
 				light+= VanillaConfiguration.HELMET_RATIO;
 				break;
 			
 			case HEAVY_ARMOUR:
 				heavyDR+= mchestplate.getDefenceRating();
 				heavy+= VanillaConfiguration.HELMET_RATIO;
 				break;
 
 			case EXOTIC_ARMOUR:
 				exoticDR+= mchestplate.getDefenceRating();
 				exotic+= VanillaConfiguration.HELMET_RATIO;
 				break;
 				
 			default:
 				break;
 			}
 			
 			switch (material) {
 			case LEATHER_CHESTPLATE:
 				armour+= VanillaConfiguration.LEATHER_CHESTPLATE_MULTIPLIER;
 				break;
 			
 			case GOLD_CHESTPLATE:
 				armour+= VanillaConfiguration.GOLD_CHESTPLATE_MULTIPLIER;
 				break;
 
 			case CHAINMAIL_CHESTPLATE:
 				armour+= VanillaConfiguration.CHAINMAIL_CHESTPLATE_MULTIPLIER;
 				break;
 
 			case IRON_CHESTPLATE:
 				armour+= VanillaConfiguration.IRON_CHESTPLATE_MULTIPLIER;
 				break;
 			
 			case DIAMOND_CHESTPLATE:
 				armour+= VanillaConfiguration.DIAMOND_CHESTPLATE_MULTIPLIER;
 				break;
 				
 			default:
 				break;
 			}
 		}
 		
 		// Leggings:
 		if(mleggings != null){
 			material = mleggings.getMaterial();
 
 			switch (mleggings.getType()) {
 			case LIGHT_ARMOUR:
 				lightDR+= mleggings.getDefenceRating();
 				light+= VanillaConfiguration.HELMET_RATIO;
 				break;
 			
 			case HEAVY_ARMOUR:
 				heavyDR+= mleggings.getDefenceRating();
 				heavy+= VanillaConfiguration.HELMET_RATIO;
 				break;
 
 			case EXOTIC_ARMOUR:
 				exoticDR+= mleggings.getDefenceRating();
 				exotic+= VanillaConfiguration.HELMET_RATIO;
 				break;
 				
 			default:
 				break;
 			}
 			
 			switch (material) {
 			case LEATHER_LEGGINGS:
 				armour+= VanillaConfiguration.LEATHER_LEGGINGS_MULTIPLIER;
 				break;
 			
 			case GOLD_LEGGINGS:
 				armour+= VanillaConfiguration.GOLD_LEGGINGS_MULTIPLIER;
 				break;
 
 			case CHAINMAIL_LEGGINGS:
 				armour+= VanillaConfiguration.CHAINMAIL_LEGGINGS_MULTIPLIER;
 				break;
 
 			case IRON_LEGGINGS:
 				armour+= VanillaConfiguration.IRON_LEGGINGS_MULTIPLIER;
 				break;
 			
 			case DIAMOND_LEGGINGS:
 				armour+= VanillaConfiguration.DIAMOND_LEGGINGS_MULTIPLIER;
 				break;
 				
 			default:
 				break;
 			}
 		}
 		
 		// Boots:
 		if(mboots != null){
 			material = mboots.getMaterial();
 
 			switch (mboots.getType()) {
 			case LIGHT_ARMOUR:
 				lightDR+= mboots.getDefenceRating();
 				light+= VanillaConfiguration.HELMET_RATIO;
 				break;
 			
 			case HEAVY_ARMOUR:
 				heavyDR+= mboots.getDefenceRating();
 				heavy+= VanillaConfiguration.HELMET_RATIO;
 				break;
 
 			case EXOTIC_ARMOUR:
 				exoticDR+= mboots.getDefenceRating();
 				exotic+= VanillaConfiguration.HELMET_RATIO;
 				break;
 				
 			default:
 				break;
 			}
 			
 			switch (material) {
 			case LEATHER_BOOTS:
 				armour+= VanillaConfiguration.LEATHER_BOOTS_MULTIPLIER;
 				break;
 			
 			case GOLD_BOOTS:
 				armour+= VanillaConfiguration.GOLD_BOOTS_MULTIPLIER;
 				break;
 
 			case CHAINMAIL_BOOTS:
 				armour+= VanillaConfiguration.CHAINMAIL_BOOTS_MULTIPLIER;
 				break;
 
 			case IRON_BOOTS:
 				armour+= VanillaConfiguration.IRON_BOOTS_MULTIPLIER;
 				break;
 			
 			case DIAMOND_BOOTS:
 				armour+= VanillaConfiguration.DIAMOND_BOOTS_MULTIPLIER;
 				break;
 				
 			default:
 				break;
 			}
 		}
 
 		// Attributes:
 		Attribute[] attributes = AttributeConfiguration.getAttributes();
 		for (int i = 0; i < attributes.length; i++) {
 			int score = getAttribScore(i);
 			if(light > 0.0) lightDR+= light * attributes[i].getSpecifier(Specifier.LIGHT_ARMOUR_DEFENCE_RATING_MODIFIER, score);
 			if(heavy > 0.0) heavyDR+= heavy * attributes[i].getSpecifier(Specifier.HEAVY_ARMOUR_DEFENCE_RATING_MODIFIER, score);
 			if(exotic > 0.0) exoticDR+= exotic * attributes[i].getSpecifier(Specifier.EXOTIC_ARMOUR_DEFENCE_RATING_MODIFIER, score);
 		}
 		
 		// Skills:
 		Skill[] skills = SkillConfiguration.getSkills();
 		for (int i = 0; i < skills.length; i++) {
 			int score = getSkillScore(i);
 			if(heavy > 0.0) lightDR+= heavy * skills[i].getSpecifier(Specifier.LIGHT_ARMOUR_DEFENCE_RATING_MODIFIER, score);
 			if(heavy > 0.0) heavyDR+= heavy * skills[i].getSpecifier(Specifier.HEAVY_ARMOUR_DEFENCE_RATING_MODIFIER, score);
 			if(exotic > 0.0) exoticDR+= exotic * skills[i].getSpecifier(Specifier.EXOTIC_ARMOUR_DEFENCE_RATING_MODIFIER, score);
 		}
 		
 		// Armour defence rating:
 		armourDR = lightDR + heavyDR + exoticDR;
 		
 	 }
 	
 	
 	// SCORES:
 	/**
 	 * Gets the attribute score.
 	 * 
 	 * @param index attribute index
 	 * @return attribute score
 	 * @throws IndexOutOfBoundsException when the index is out of bounds
 	 */
 	public int getAttribScore(int index)
 	 {
 		return attributes[index];
 	 }
 	
 	/**
 	 * Gets the attribute score.
 	 * 
 	 * @param attributeName attribute name
 	 * @return attribute score
 	 */
 	public int getAttribScore(String atribName)
 	 {
 		int i = AttributeConfiguration.getAttribIndex(atribName);
 		if(i == -1) return 0;
 		
 		return getAttribScore(i);
 	 }
 	
 	/**
 	 * Modifies attribute score.
 	 * 
 	 * @param atribName attribute name
 	 * @param amount amount
 	 */
 	public void modAttribScore(String atribName, int amount)
 	 {
 		int i = AttributeConfiguration.getAttribIndex(atribName);
 		if(i == -1) return;
 		
 		attributes[i]+= amount;
 	 }
 	
 	/**
 	 * Gets the skill score.
 	 * 
 	 * @param index skill index
 	 * @return skill score
 	 * @throws IndexOutOfBoundsException when the index is out of bounds
 	 */
 	public int getSkillScore(int index)
 	 {
 		return skills[index];
 	 }
 	
 	/**
 	 * Gets the skill score.
 	 * 
 	 * @param skillName skill name
 	 * @return skill score
 	 */
 	public int getSkillScore(String skillName)
 	 {
 		int i = SkillConfiguration.getSkillIndex(skillName);
 		if(i == -1) return 0;
 		
 		return getSkillScore(i);
 	 }
 
 	/**
 	 * Modifies skill score.
 	 * 
 	 * @param skillName skill name
 	 * @param amount amount
 	 */
 	public void modSkillScore(String skillName, int amount)
 	 {
 		int i = SkillConfiguration.getSkillIndex(skillName);
 		if(i == -1) return;
 		
 		skills[i]+= amount;
 	 }
 	
 	
 	// DAMAGE:
 	/**
 	 * 
 	 * 
 	 * @param type
 	 * @param attacker
 	 * @return
 	 */
 	public int defend(ItemType type, DerivedStats attacker)
 	 {
 		// Damage and defence:
 		int damage = random(attacker.minBaseDmg, attacker.maxBaseDmg);
 		double armourDR = this.armourDR;
 		double armour = this.armour;
 		
 		// Attack:
 		double attackRating = 0;
 		
 		switch (type) {
 		case MELEE_WEAPON:
 			attackRating+= attacker.meleeAR;
 			damage+= attacker.meleeDmgMod;
 			break;
 			
 		case RANGED_WEAPON:
 			attackRating+= attacker.rangedAR;
 			damage+= attacker.rangedDmgMod;
 			break;
 			
 		case ARCANE_SPELL:
 			attackRating+= attacker.magicAR;
 			damage+= attacker.magicDmgMod;
 			break;
 
 		case CURSE_SPELL:
 			attackRating+= attacker.curseAR;
 			damage+= attacker.curseDmgMod;
 			break;
 
 		case BLESSING_SPELL:
 			attackRating+= attacker.blessingAR;
 			damage+= attacker.blessingDmgMod;
 			break;
 			
 		default:
 			break;
 		}
 		
 		// Hit chance:
 		double tohit = attackRating / (attackRating + armourDR);
 		boolean hit = tohit >= RANDOM.nextDouble();
 		
 		// Half armour on hit:
 		if(hit){
 			armour = armour + 0.70 * (1 - armour);
 		}else{
 			// Apply perks:
 		}
 		
 		// Calculate damage:
 		return LinearFunction.roundRand(damage*armour);
 	 }
 	
 	
 	// UTIL:
 	/**
 	 * Generates a random number between the given values.
 	 * 
 	 * @param min minimum value
 	 * @param max maximum value
 	 * @return random number
 	 */
 	public static int random(int min, int max) {
 		return min + (int)(RANDOM.nextDouble() * ((max - min) + 1));
 	}
 	
 	
 	// OTHER:
 	/* 
 	 * (non-Javadoc)
 	 * 
 	 * @see java.lang.Object#clone()
 	 */
 	@Override
 	public DerivedStats clone()
 	 {
 		DerivedStats dstats = new DerivedStats();
 		
 		dstats.minBaseDmg = minBaseDmg;
 		dstats.maxBaseDmg = maxBaseDmg;
 		
 		dstats.meleeDmgMod = meleeDmgMod;
 		dstats.rangedDmgMod = rangedDmgMod;
 		dstats.magicDmgMod = magicDmgMod;
 		dstats.curseDmgMod = curseDmgMod;
 		dstats.blessingDmgMod = blessingDmgMod;
 
 		dstats.meleeAR = meleeAR;
 		dstats.rangedAR = rangedAR;
 		dstats.magicAR = magicAR;
 		dstats.curseAR = curseAR;
 		dstats.blessingAR = blessingAR;
 
 		dstats.armour = armour;
 		dstats.armourDR = armourDR;
 
 		dstats.attributes = attributes;
 		dstats.skills = skills;
 		
 		return dstats;
 	}
 	
 	/**
 	 * Finds living entity derived stats.
 	 * 
 	 * @param lentity living entity
 	 * @return derived stats
 	 */
 	public static DerivedStats findDerived(LivingEntity lentity)
 	 {
 		
 		// Player:
 		if(lentity instanceof Player){
 			MythrPlayer mplayer = Mythr.plugin().getLoadedPlayer(((Player) lentity).getName());
 			if(mplayer == null){
 				MythrLogger.severe(DerivedStats.class, "Failed to retrieve Mythr player for " + ((Player) lentity).getName() + ".");
 				return DEFAULT_CREATURE_STATS;
 			}
 			
 			return mplayer.getDerived();
 		}
 		
 		// Creature:
 		else{
 			// TODO: Creature derived stats.
 			return DEFAULT_CREATURE_STATS;
 		}
 		
 	 }
 	
 }
