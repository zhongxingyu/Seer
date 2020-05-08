 package hunternif.mc.dota2items.core;
 
 import hunternif.mc.dota2items.Dota2Items;
 import hunternif.mc.dota2items.core.buff.Buff;
 import hunternif.mc.dota2items.core.buff.BuffInstance;
 import hunternif.mc.dota2items.entity.IInvulnerableEntity;
 import hunternif.mc.dota2items.entity.IMagicImmuneEntity;
 import hunternif.mc.dota2items.event.BuffEvent.BuffAddEvent;
 import hunternif.mc.dota2items.event.BuffEvent.BuffRemoveEvent;
 import hunternif.mc.dota2items.network.EntityStatsSyncPacket;
 import hunternif.mc.dota2items.util.MCConstants;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import java.util.concurrent.CopyOnWriteArrayList;
 
 import net.minecraft.entity.Entity;
 import net.minecraft.entity.EntityLivingBase;
 import net.minecraft.entity.SharedMonsterAttributes;
 import net.minecraft.entity.ai.attributes.AttributeInstance;
 import net.minecraft.entity.boss.EntityDragon;
 import net.minecraft.entity.boss.EntityWither;
 import net.minecraft.entity.monster.EntityGolem;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.nbt.NBTTagCompound;
 import net.minecraft.nbt.NBTTagInt;
 import net.minecraft.nbt.NBTTagList;
 import net.minecraft.util.MathHelper;
 import net.minecraft.world.World;
 import net.minecraftforge.common.IExtendedEntityProperties;
 import net.minecraftforge.common.MinecraftForge;
 import cpw.mods.fml.common.network.PacketDispatcher;
 import cpw.mods.fml.common.network.Player;
 
 public class EntityStats implements IExtendedEntityProperties {
 	public static final int MAX_MOVE_SPEED = 522;
 	public static final int MAX_HP_PER_STR = 19;
 	public static final float HP_REGEN_PER_STR = 0.03f;
 	public static final int MAX_MANA_PER_INT = 13;
 	public static final float MANA_REGEN_PER_INT = 0.04f;
 	public static final float ARMOR_PER_AGI = 1f/7f;
 	public static final int MIN_ASPD = -80;
 	public static final int MAX_ASPD = 400;
 	
 	public static final int BASE_PLAYER_HP = 150;
 	public static final int BASE_PLAYER_STR = 0;
 	public static final int BASE_PLAYER_AGI = 0;
 	public static final int BASE_PLAYER_INT = 0;
 	public static final int BASE_PLAYER_TOTAL_HP = BASE_PLAYER_HP + BASE_PLAYER_STR*MAX_HP_PER_STR;
 	public static final int BASE_PLAYER_MOVE_SPEED = 300;
 	
 	private static final String TAG_MANA = "D2IMana";
 	private static final String TAG_PARTIAL_HP = "D2IPartialHp";
 	private static final String TAG_RELIABLE_GOLD = "D2IrelGold";
 	private static final String TAG_UNRELIABLE_GOLD = "D2IunrelGold";
 	private static final String TAG_BUFFS = "D2IBuffs";
 	private static final String TAG_ATTACKER_ID = "D2IAttackerID";
 	private static final String TAG_ATTACKERS = "D2IAttackers";
 	
 	
 	public EntityLivingBase entity;
 	/** Measured in ticks the Entity has existed. */
 	public long lastSyncTime;
 	
 	public int baseHealth;
 	public float baseHealthRegen;
 	public int baseMana = 0;
 	public float baseManaRegen;
 	public int baseMovementSpeed;
 	public float baseAttackTime;
 	public int baseArmor = 0;
 	public int baseDamage = 0;
 	public int baseSpellResistance = 0;
 	
 	public boolean baseInvulnerable = false;
 	public boolean baseMagicImmune = false;
 	
 	private float baseStrength = BASE_PLAYER_STR;
 	private float baseAgility = BASE_PLAYER_AGI;
 	private float baseIntelligence = BASE_PLAYER_INT;
 	
 	// Health is deferred to Minecraft.
 	private float curMana = 0;
 	private float reliableGold = 0;
 	private float unreliableGold = 0;
 	/** Used to restrict attack interval by AttackTime. */
 	public long lastAttackTime = 0;
 	/** For assist gold. This is only updated on the server. */
 	private Set<Integer> playerAttackersIDs = Collections.synchronizedSet(new HashSet<Integer>());
 	
 	/** When applying damage or heal directly to the entity, the amount of health
 	 * will be truncated due to entity's health being a fixed small integer
 	 * (20 half-hearts for the player). In order to apply the damage and heal
 	 * amount properly according to Dota mechanics, this partial health will be
 	 * "carried over" and applied, when sufficient, at the next heal/damage
 	 * instance. */
 	public float partialHalfHeart = 0;
 	
 	private List<BuffInstance> appliedBuffs = new CopyOnWriteArrayList<BuffInstance>();
 	
 	
 	public EntityStats(EntityLivingBase entity) {
 		this.entity = entity;
 		AttributeInstance attrMaxHealth = entity.func_110148_a(SharedMonsterAttributes.field_111267_a);
 		// When EntityPlayer is Constructing, all his attributes are null
 		float maxHealth = attrMaxHealth == null ? MCConstants.MINECRAFT_PLAYER_HP : (float)attrMaxHealth.func_111126_e();
 		baseHealth = MathHelper.floor_float(maxHealth * (float)BASE_PLAYER_HP / MCConstants.MINECRAFT_PLAYER_HP);
 		if (entity instanceof EntityPlayer) {
 			baseManaRegen = 0.01f;
 			baseHealthRegen = 0.25f;
 			baseAttackTime = 1.3f;
 			baseMovementSpeed = BASE_PLAYER_MOVE_SPEED;
 			//baseSpellResistance = 25; Cancelled so that normal Minecraft magic hurts as much as before.
 		} else {
 			baseManaRegen = 0.75f;
 			baseHealthRegen = 0.5f;
 			baseAttackTime = 0.8f;
 			AttributeInstance attrMoveSpeed = entity.func_110148_a(SharedMonsterAttributes.field_111263_d);
 			double baseMoveSpeed = attrMoveSpeed.func_111125_b();
 			baseMovementSpeed = MathHelper.floor_double((double)BASE_PLAYER_MOVE_SPEED * baseMoveSpeed / MCConstants.MINECRAFT_PLAYER_MOVE_SPEED);
 		}
 		if (entity instanceof EntityGolem || entity instanceof EntityDragon || entity instanceof EntityWither) {
 			baseMagicImmune = true;
 		}
 		if (entity instanceof IMagicImmuneEntity) {
 			baseMagicImmune = true;
 		}
 		if (entity instanceof IInvulnerableEntity) {
 			baseInvulnerable = true;
 		}
 	}
 	
 	public List<BuffInstance> getAppliedBuffs() {
 		return appliedBuffs;
 	}
 	public void addBuff(BuffInstance buffInst) {
 		if (buffInst == null) { return; }
 		if (!buffInst.buff.stacks) {
 			for (BuffInstance curBuffInst : appliedBuffs) {
 				if (curBuffInst.buff.id == buffInst.buff.id) {
 					if (buffInst.endTime > curBuffInst.endTime) {
 						curBuffInst.startTime = buffInst.startTime;
 						curBuffInst.endTime = buffInst.endTime;
 					}
 					return;
 				}
 			}
 		}
 		if (appliedBuffs.add(buffInst)) {
 			MinecraftForge.EVENT_BUS.post(new BuffAddEvent(buffInst, entity));
 		}
 	}
 	public void removeBuff(BuffInstance buffInst) {
 		if (buffInst == null) { return; }
 		if (appliedBuffs.remove(buffInst)) {
 			MinecraftForge.EVENT_BUS.post(new BuffRemoveEvent(buffInst, entity));
 		}
 	}
 	public boolean hasBuff(Buff buff) {
 		return getBuffInstance(buff) != null;
 	}
 	public BuffInstance getBuffInstance(Buff buff) {
 		for (BuffInstance buffInst : appliedBuffs) {
 			if (buffInst.buff == buff) {
 				return buffInst;
 			}
 		}
 		return null;
 	}
 	
 	/** Returns a copy. */
 	public Set<Integer> getPlayerAttackersIDs() {
 		return new HashSet<Integer>(playerAttackersIDs);
 	}
 	public void addPlayerAttackerID(int id) {
 		playerAttackersIDs.add(Integer.valueOf(id));
 	}
 	public void removePlayerAttackerID(int id) {
 		playerAttackersIDs.remove(Integer.valueOf(id));
 	}
 	
 	public int getMaxHealth() {
 		int health = baseHealth;
 		for (BuffInstance buffInst : appliedBuffs) {
 			health += buffInst.buff.health;
 		}
 		return health + MAX_HP_PER_STR*getStrength();
 	}
 	public int getHealth(EntityLivingBase entity) {
 		float mcHPProportion = (float)entity.func_110143_aJ() / (float)entity.func_110138_aP();
 		float halfHeartEquivalent = (float)getMaxHealth() / (float)entity.func_110138_aP();
 		return MathHelper.floor_float((float)getMaxHealth()*mcHPProportion + halfHeartEquivalent*partialHalfHeart);
 	}
 	public float getHealthRegen() {
 		float regen = baseHealthRegen;
 		for (BuffInstance buffInst : appliedBuffs) {
 			regen += buffInst.buff.healthRegen;
 		}
 		return regen + HP_REGEN_PER_STR*(float)getStrength();
 	}
 	
 	public void heal(float dotaHealthAmount) {
 		// func_110138_aP = "getMaxHealth"
 		float halfHeartEquivalent = (float)getMaxHealth() / (float)entity.func_110138_aP();
 		float partialHealth = partialHalfHeart + dotaHealthAmount / halfHeartEquivalent;
 		if (partialHealth >= 1) {
 			int floor = MathHelper.floor_float(partialHealth);
 			entity.heal(floor);
 			partialHealth -= (float) floor;
		}
		if (entity.func_110143_aJ() == entity.func_110138_aP()) {
			// At full health
			partialHealth = 0;
 		}
 		partialHalfHeart = partialHealth;
 	}
 	
 	/** Store or apply the partial damage, that doesn't constitute enough to deplete 1 half-heart. */
 	public int getDamageFloor(float floatMCDamage) {
 		int intMCDamage = MathHelper.floor_float(floatMCDamage);
 		float partialDamage = floatMCDamage - (float)intMCDamage - partialHalfHeart;
 		int partialDamageFloor = MathHelper.floor_float(partialDamage);
 		intMCDamage += partialDamageFloor;
 		partialDamage -= (float)partialDamageFloor;
 		partialHalfHeart = -partialDamage;
 		if (partialDamageFloor > 0) {
 			Dota2Items.logger.info(String.format("Applied carry-over damage: %d", partialDamageFloor));
 		}
 		return intMCDamage;
 	}
 	
 	public int getMaxMana() {
 		int mana = baseMana;
 		for (BuffInstance buffInst : appliedBuffs) {
 			mana += buffInst.buff.mana;
 		}
 		return mana + MAX_MANA_PER_INT*getIntelligence();
 	}
 	public int getMana() {
 		return MathHelper.floor_float(curMana); //Math.min(getMaxMana(), MathHelper.floor_float(curMana));
 	}
 	/** Only for "technical" use like writing to NBT and syncing over the network. */
 	public float getFloatMana() {
 		return curMana;
 	}
 	public void setMana(float amount) {
 		curMana = amount;
 	}
 	public void addMana(float amount) {
 		setMana(curMana + amount);
 		clampMana();
 	}
 	public void removeMana(float amount) {
 		setMana(curMana - amount);
 		clampMana();
 	}
 	
 	public float getManaRegen() {
 		float regen = baseManaRegen;
 		for (BuffInstance buffInst : appliedBuffs) {
 			regen += buffInst.buff.manaRegen;
 		}
 		regen += MANA_REGEN_PER_INT*(float)getIntelligence();
 		for (BuffInstance buffInst : appliedBuffs) {
 			regen *= 1f + buffInst.buff.manaRegenPercent/100f;
 		}
 		return regen;
 	}
 	
 	/** A percentage. */
 	public int getIncreasedAttackSpeed() {
 		int aspd = 0;
 		for (BuffInstance buffInst : appliedBuffs) {
 			aspd += buffInst.buff.attackSpeed;
 		}
 		aspd += getAgility();
 		if (aspd < MIN_ASPD) aspd = MIN_ASPD;
 		if (aspd > MAX_ASPD) aspd = MAX_ASPD;
 		return aspd;
 	}
 	
 	public float getAttackTime() {
 		float aspd = ((float)getIncreasedAttackSpeed())/100f;
 		float attackTime = baseAttackTime / (1f + aspd);
 		//FMLLog.log(Dota2Items.ID, Level.FINE, "Attack time = %.2f", attackTime);
 		return attackTime;
 	}
 	
 	public float getDamage(float weaponDamage, boolean melee) {
 		float damage = weaponDamage + this.baseDamage;
 		for (BuffInstance buffInst : appliedBuffs) {
 			damage += buffInst.buff.damage;
 		}
 		for (BuffInstance buffInst : appliedBuffs) {
 			damage += (float)damage * ((float)(melee ? buffInst.buff.damagePercentMelee : buffInst.buff.damagePercentRanged)) / 100f;
 			break; // Because percentage-based movement speed from multiple items does not stack.
 		}
 		return damage;
 	}
 	
 	/** Result is random! */
 	public float getBonusDamage() {
 		float damage = 0;
 		for (BuffInstance buffInst : appliedBuffs) {
 			if (Math.random()*100 <= buffInst.buff.bonusDamageChance) {
 				damage += buffInst.buff.bonusDamage;
 			}
 		}
 		if (damage > 0) {
 			Dota2Items.logger.info(String.format("Bonus damage: %.1f", damage));
 		}
 		return damage;
 	}
 	
 	public int getArmor() {
 		int armor = this.baseArmor;
 		for (BuffInstance buffInst : appliedBuffs) {
 			armor += buffInst.buff.armor;
 		}
 		armor += (float)getAgility() * ARMOR_PER_AGI;
 		return armor;
 	}
 	
 	/** @return movement speed calculated for Minecraft. */
 	public float getMovementSpeed() {
 		return (float)getDotaMovementSpeed() * MCConstants.MINECRAFT_PLAYER_MOVE_SPEED / (float) BASE_PLAYER_MOVE_SPEED;
 	}
 	public int getDotaMovementSpeed() {
 		int movementSpeed = baseMovementSpeed;
 		List<Buff> appliedMSBuffs = new ArrayList<Buff>();
 		boolean bootsApplied = false;
 		for (BuffInstance buffInst : appliedBuffs) {
 			if (!appliedMSBuffs.contains(buffInst.buff) && buffInst.buff.movementSpeed > 0) {
 				movementSpeed += buffInst.buff.movementSpeed;
 				// Movement speed from the same type of Buff doesn't stack
 				appliedMSBuffs.add(buffInst.buff);
 			}
 			// Flat movement speed bonuses from multiple pairs of boots do not stack.
 			if (buffInst.buff.bootsMovementSpeed > 0 && !bootsApplied) {
 				movementSpeed += buffInst.buff.bootsMovementSpeed;
 				bootsApplied = true;
 			}
 		}
 		for (BuffInstance buffInst : appliedBuffs) {
 			if (!appliedMSBuffs.contains(buffInst.buff) && buffInst.buff.movementSpeedPercent > 0) {
 				movementSpeed += MathHelper.floor_float((float)movementSpeed * (float)buffInst.buff.movementSpeedPercent / 100f);
 				// Movement speed from the same type of Buff doesn't stack
 				appliedMSBuffs.add(buffInst.buff);
 			}
 		}
 		/*if (movementSpeed > MAX_MOVE_SPEED) {
 			movementSpeed = MAX_MOVE_SPEED;
 		}*/
 		return movementSpeed;
 	}
 	
 	public boolean canAttack() {
 		for (BuffInstance buffInst : appliedBuffs) {
 			if (buffInst.buff.disableAttack) {
 				return false;
 			}
 		}
 		return true;
 	}
 	
 	public boolean canMove() {
 		for (BuffInstance buffInst : appliedBuffs) {
 			if (buffInst.buff.disableMove) {
 				return false;
 			}
 		}
 		return true;
 	}
 	
 	public boolean canUseItems() {
 		for (BuffInstance buffInst : appliedBuffs) {
 			if (buffInst.buff.disableItems) {
 				return false;
 			}
 		}
 		return true;
 	}
 	
 	public boolean isMagicImmune() {
 		if (baseMagicImmune) {
 			return true;
 		}
 		for (BuffInstance buffInst : appliedBuffs) {
 			if (buffInst.buff.magicImmune) {
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	public boolean isInvulnerable() {
 		if (baseInvulnerable) {
 			return true;
 		}
 		for (BuffInstance buffInst : appliedBuffs) {
 			if (buffInst.buff.invulnerable) {
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	public int getGold() {
 		return MathHelper.floor_float(reliableGold + unreliableGold);
 	}
 	public float getReliableGold() {
 		return reliableGold;
 	}
 	public float getUnreliableGold() {
 		return unreliableGold;
 	}
 	public void setGold(float reliable, float unreliable) {
 		if (reliable < 0) reliable = 0;
 		reliableGold = reliable;
 		if (unreliable < 0) unreliable = 0;
 		unreliableGold = unreliable;
 	}
 	public void addGold(float reliable, float unreliable) {
 		setGold(reliableGold + reliable, unreliableGold + unreliable);
 	}
 	/** Returns the remainder that reliable gold couldn't cover. */
 	public float deductReliableGold(float amount) {
 		if (reliableGold >= amount) {
 			reliableGold -= amount;
 			return 0;
 		} else {
 			float remainder = amount - reliableGold;
 			reliableGold = 0;
 			return remainder;
 		}
 	}
 	/** Returns the remainder that unreliable gold couldn't cover. */
 	public float deductUnreliableGold(float amount) {
 		if (unreliableGold >= amount) {
 			unreliableGold -= amount;
 			return 0;
 		} else {
 			float remainder = amount - unreliableGold;
 			unreliableGold = 0;
 			return remainder;
 		}
 	}
 	
 	public int getBaseStrength() {
 		return MathHelper.floor_float(baseStrength);
 	}
 	public float getFloatBaseStrength() {
 		return baseStrength;
 	}
 	public void setBaseStrength(float value) {
 		if (value < 0) value = 0;
 		baseStrength = value;
 	}
 	public int getStrength() {
 		float str = baseStrength;
 		for (BuffInstance buffInst : appliedBuffs) {
 			str += buffInst.buff.strength;
 		}
 		return MathHelper.floor_float(str);
 	}
 	
 	public int getBaseAgility() {
 		return MathHelper.floor_float(baseAgility);
 	}
 	public float getFloatBaseAgility() {
 		return baseAgility;
 	}
 	public void setBaseAgility(float value) {
 		if (value < 0) value = 0;
 		baseAgility = value;
 	}
 	public int getAgility() {
 		float agi = baseAgility;
 		for (BuffInstance buffInst : appliedBuffs) {
 			agi += buffInst.buff.agility;
 		}
 		return MathHelper.floor_float(agi);
 	}
 	
 	public int getBaseIntelligence() {
 		return MathHelper.floor_float(baseIntelligence);
 	}
 	public float getFloatBaseIntelligence() {
 		return baseIntelligence;
 	}
 	public void setBaseIntelligence(float value) {
 		if (value < 0) value = 0;
 		baseIntelligence = value;
 	}
 	public int getIntelligence() {
 		float intel = baseIntelligence;
 		for (BuffInstance buffInst : appliedBuffs) {
 			intel += buffInst.buff.intelligence;
 		}
 		return MathHelper.floor_float(intel);
 	}
 	
 	public boolean canEvade() {
 		for (BuffInstance buffInst : appliedBuffs) {
 			if (buffInst.buff.evasionPercent > 0 && Math.random()*100 <= buffInst.buff.evasionPercent) {
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	/** Result is random! */
 	public float getCriticalMultiplier() {
 		List<BuffInstance> critBuffs = new ArrayList<BuffInstance>();
 		for (BuffInstance buffInst : appliedBuffs) {
 			if (buffInst.buff.critChancePercent > 0) {
 				critBuffs.add(buffInst);
 			}
 		}
 		Collections.sort(critBuffs, critDamageComparator);
 		for (BuffInstance buffInst : critBuffs) {
 			if (Math.random()*100 <= buffInst.buff.critChancePercent) {
 				return buffInst.buff.critDamagePercent * 0.01f;
 			}
 		}
 		return 1f;
 	}
 	/** Provides sorting by Critical Damage in descending order. */
 	private static class CritDamageComparator implements Comparator<BuffInstance> {
 		@Override
 		public int compare(BuffInstance o1, BuffInstance o2) {
 			return o1.buff.critDamagePercent > o2.buff.critDamagePercent ? -1 :
 				o1.buff.critDamagePercent < o2.buff.critDamagePercent ? 1 : 0;
 		}
 	}
 	private static CritDamageComparator critDamageComparator = new CritDamageComparator();
 	
 	public float getSpellResistance() {
 		float resistance = baseSpellResistance;
 		List<Buff> appliedItems = new ArrayList<Buff>();
 		for (BuffInstance buffInst : appliedBuffs) {
 			if (buffInst.buff.spellResistance != 0 && !appliedItems.contains(buffInst.buff)) {
 				if (buffInst.isItemPassiveBuff()) {
 					appliedItems.add(buffInst.buff);
 				}
 				resistance += buffInst.buff.spellResistance;
 			}
 		}
 		return resistance * 0.01f;
 	}
 	
 	public float getAmplifyDamage(boolean isMagic) {
 		float result = 1f;
 		for (BuffInstance buffInst : appliedBuffs) {
 			if (isMagic && buffInst.buff.amplifyMagic != 0) {
 				result += result * (float)buffInst.buff.amplifyMagic * 0.01f;
 			} else if (!isMagic && buffInst.buff.amplifyDamage != 0) {
 				result += result * (float)buffInst.buff.amplifyDamage * 0.01f;
 			}
 		}
 		return result;
 	}
 	
 	public int getDamageBlock(boolean melee, boolean isHero) {
 		int block = 0;
 		for (BuffInstance buffInst : appliedBuffs) {
 			int curBlock = buffInst.buff.getDamageBlock(melee, isHero);
 			if (curBlock > block) block = curBlock;
 		}
 		if (block > 0) {
 			Dota2Items.logger.info(String.format("Blocked %d damage", block));
 		}
 		return block;
 	}
 	
 	public boolean isTrueStrike() {
 		for (BuffInstance buffInst : appliedBuffs) {
 			if (buffInst.buff.trueStrike) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	public float getLifestealMultiplier() {
 		float percent = 0;
 		for (BuffInstance buffInst : appliedBuffs) {
 			percent += buffInst.buff.lifesteal;
 		}
 		return percent / 100f;
 	}
 	
 	@Override
 	public void saveNBTData(NBTTagCompound compound) {
 		compound.setFloat(TAG_MANA, getFloatMana());
 		compound.setFloat(TAG_PARTIAL_HP, partialHalfHeart);
 		compound.setFloat(TAG_RELIABLE_GOLD, getReliableGold());
 		compound.setFloat(TAG_UNRELIABLE_GOLD, getUnreliableGold());
 		NBTTagList buffsList = new NBTTagList();
 		for (BuffInstance buffInst : appliedBuffs) {
 			buffsList.appendTag(buffInst.toNBT());
 		}
 		compound.setTag(TAG_BUFFS, buffsList);
 		NBTTagList attackersList = new NBTTagList();
 		synchronized (playerAttackersIDs) {
 			for (Integer playerID : playerAttackersIDs) {
 				attackersList.appendTag(new NBTTagInt(TAG_ATTACKER_ID, playerID.intValue()));
 			}
 		}
 		compound.setTag(TAG_ATTACKERS, attackersList);
 	}
 
 	@Override
 	public void loadNBTData(NBTTagCompound compound) {
 		this.curMana = compound.getFloat(TAG_MANA);
 		this.partialHalfHeart = compound.getFloat(TAG_PARTIAL_HP);
 		this.reliableGold = compound.getFloat(TAG_RELIABLE_GOLD);
 		this.unreliableGold = compound.getFloat(TAG_UNRELIABLE_GOLD);
 		NBTTagList buffsList = compound.getTagList(TAG_BUFFS);
 		for (int i = 0; i < buffsList.tagCount(); i++) {
 			NBTTagCompound buffTag = (NBTTagCompound) buffsList.tagAt(i);
 			BuffInstance buffInst = BuffInstance.fromNBT(buffTag, entity.entityId);
 			addBuff(buffInst);
 		}
 		NBTTagList attackersList = compound.getTagList(TAG_ATTACKERS);
 		for (int i = 0; i < attackersList.tagCount(); i++) {
 			NBTTagInt playerID = (NBTTagInt) attackersList.tagAt(i);
 			addPlayerAttackerID(playerID.data);
 		}
 	}
 
 	@Override
 	public void init(Entity entity, World world) {
 	}
 	
 	public void clampMana() {
 		int maxMana = getMaxMana();
 		if (curMana < 0) curMana = 0;
 		if (curMana > maxMana) curMana = maxMana;
 	}
 	
 	public void sendSyncPacketToClient(EntityPlayer player) {
 		this.lastSyncTime = player.ticksExisted;
 		PacketDispatcher.sendPacketToPlayer(new EntityStatsSyncPacket(this).makePacket(), (Player)player);
 	}
 }
