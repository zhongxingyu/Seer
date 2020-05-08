 package hunternif.mc.dota2items.core;
 
 import hunternif.mc.dota2items.core.buff.BuffInstance;
 import hunternif.mc.dota2items.effect.ContinuousEffect;
 import hunternif.mc.dota2items.event.UseItemEvent;
 import hunternif.mc.dota2items.network.BuffPacket;
 import hunternif.mc.dota2items.network.EntityHurtPacket;
 import hunternif.mc.dota2items.util.MCConstants;
 import hunternif.mc.dota2items.util.NetworkUtil;
 
 import java.util.Map;
 import java.util.UUID;
 import java.util.concurrent.ConcurrentHashMap;
 
 import net.minecraft.entity.Entity;
 import net.minecraft.entity.EntityLivingBase;
 import net.minecraft.entity.SharedMonsterAttributes;
 import net.minecraft.entity.ai.attributes.AttributeInstance;
 import net.minecraft.entity.ai.attributes.AttributeModifier;
 import net.minecraft.entity.monster.EntityCreeper;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraftforge.common.IExtendedEntityProperties;
 import net.minecraftforge.event.ForgeSubscribe;
 import net.minecraftforge.event.entity.EntityEvent.EntityConstructing;
 import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
 import cpw.mods.fml.common.IPlayerTracker;
 import cpw.mods.fml.common.network.PacketDispatcher;
 import cpw.mods.fml.relauncher.ReflectionHelper;
 import cpw.mods.fml.relauncher.Side;
 
 public class StatsTracker implements IPlayerTracker {
 	/** I have no idea how to generate these properly. */
 	private static final UUID uuid = UUID.fromString("92f7a640-0ac7-11e3-8ffd-0800200c9a66");
 	
 	private static final String[] timeSinceIgnitedObfFields = {"timeSinceIgnited", "d", "field_70833_d"};
 	
 	private static final String EXT_PROP_STATS = "Dota2ItemsEntityStats";
 	
 	public static final int FOOD_THRESHOLD_FOR_HEAL = 0;
 	public static final float STR_PER_LEVEL = 2;
 	public static final float AGI_PER_LEVEL = 2;
 	public static final float INT_PER_LEVEL = 2;
 	
 	/** In seconds. */
 	public static final float SYNC_STATS_INTERVAL = 10;
 	
 	private Map<EntityLivingBase, EntityStats> clientEntityStats = new ConcurrentHashMap<EntityLivingBase, EntityStats>();
 	private Map<EntityLivingBase, EntityStats> serverEntityStats = new ConcurrentHashMap<EntityLivingBase, EntityStats>();
 	private Map<EntityLivingBase, EntityStats> getEntityStatsMap(Side side) {
 		return side.isClient() ? clientEntityStats : serverEntityStats;
 	}
 	
 	/** Guaranteed to be non-null for non-null entities. */
 	public EntityStats getOrCreateEntityStats(EntityLivingBase entity) {
 		if (entity == null) return null;
 		Map<EntityLivingBase, EntityStats> entityStats = getEntityStatsMap(getSide(entity));
 		EntityStats stats = entityStats.get(entity);
 		if (stats == null) {
 			stats = new EntityStats(entity);
 			entityStats.put(entity, stats);
		} else if (stats.entity != entity) {
			// Happens when the entity is copied, e.g. when changing dimension.
			stats.entity = entity;
 		}
 		return stats;
 	}
 	
 	/** Can be null. Accepts Entity, although stats only exist for EntityLivingBase. */
 	public EntityStats getEntityStats(Entity entity) {
 		if (entity == null) return null;
 		Map<EntityLivingBase, EntityStats> entityStats = getEntityStatsMap(getSide(entity));
		EntityStats stats = entityStats.get(entity);
		if (stats != null && stats.entity != entity) {
			// Happens when the entity is copied, e.g. when changing dimension.
			stats.entity = (EntityLivingBase)entity;
		}
		return stats;
 	}
 	
 	public void removeEntityStats(Entity entity) {
 		Map<EntityLivingBase, EntityStats> entityStats = getEntityStatsMap(getSide(entity));
 		entityStats.remove(entity);
 	}
 	
 	private void updateMoveSpeed(EntityLivingBase entity, EntityStats stats) {
 		AttributeInstance moveSpeedAttribute = entity.getEntityAttribute(SharedMonsterAttributes.movementSpeed);
 		double newMoveSpeed = stats.getMovementSpeed();
 		double oldMoveSpeed = moveSpeedAttribute.getAttributeValue();
 		if (newMoveSpeed != oldMoveSpeed) {
 			double baseMoveSpeed = moveSpeedAttribute.getBaseValue();
 			// Get the modifier:
 			AttributeModifier speedModifier = moveSpeedAttribute.getModifier(uuid);
 			if (speedModifier != null) {
 				// Remove the old modifier
 				moveSpeedAttribute.removeModifier(speedModifier);
 			}
 			// I think the argument "2" stands for operation "add percentage":
 			speedModifier = new AttributeModifier(uuid, "Speed bonus from Dota 2 Items", newMoveSpeed / baseMoveSpeed - 1.0, 2)
 				.setSaved(false); // I think this makes it non-persistent
 			moveSpeedAttribute.applyModifier(speedModifier);
 		}
 	}
 	
 	@ForgeSubscribe
 	public void onLivingUpdate(LivingUpdateEvent event) {
 		EntityStats stats = getEntityStats(event.entityLiving);
 		if (stats == null) {
 			return;
 		}
 		if (event.entityLiving instanceof EntityPlayer) {
 			// Regenerate health and mana every second:
 			regenHealthAndMana(stats);
 			// Add base attributes per level:
 			updateBaseAttributes((EntityPlayer)event.entityLiving, stats);
 			// Synchronize stats with all clients every SYNC_STATS_INTERVAL seconds:
 			int time = event.entityLiving.ticksExisted;
 			if (!event.entityLiving.worldObj.isRemote && time - stats.lastSyncTime >=
 					(long) (MCConstants.TICKS_PER_SECOND * SYNC_STATS_INTERVAL)) {
 				stats.sendSyncPacketToClient((EntityPlayer)event.entityLiving);
 			}
 		}
 		stats.clampMana();
 		for (BuffInstance buffInst : stats.getAppliedBuffs()) {
 			if (!buffInst.isPermanent() && event.entity.worldObj.getTotalWorldTime() > buffInst.endTime) {
 				stats.removeBuff(buffInst);
 			}
 		}
 		updateMoveSpeed(event.entityLiving, stats);
 		if (!stats.canMove()) {
 			event.setCanceled(true);
 			// Update items in inventory so that cooldown keeps on ticking:
 			if (event.entityLiving instanceof EntityPlayer) {
 				((EntityPlayer)event.entityLiving).inventory.decrementAnimations();
 			}
 		}
 		// Workaround for creepers still exploding while having their attack disabled:
 		if (!stats.canAttack() && event.entityLiving instanceof EntityCreeper) {
 			EntityCreeper creeper = (EntityCreeper) event.entityLiving;
 			Integer timeSinceIgnited = ReflectionHelper.getPrivateValue(EntityCreeper.class, creeper, timeSinceIgnitedObfFields);
 			ReflectionHelper.setPrivateValue(EntityCreeper.class, creeper, timeSinceIgnited.intValue()-1, timeSinceIgnitedObfFields);
 		}
 	}
 	
 	private static Side getSide(Entity entity) {
 		return entity.worldObj.isRemote ? Side.CLIENT : Side.SERVER;
 	}
 	
 	private static void regenHealthAndMana(EntityStats stats) {
 		if (shouldHeal(stats)) {
 			stats.heal(stats.getHealthRegen() / MCConstants.TICKS_PER_SECOND);
 		}
 		if ((stats.entity instanceof EntityPlayer) && ((EntityPlayer)stats.entity).capabilities.isCreativeMode) {
 			stats.setMana(stats.getMaxMana());
 		} else {
 			if (stats.entity.getHealth() > 0 && stats.getMana() < stats.getMaxMana()) {
 				stats.addMana(stats.getManaRegen() / MCConstants.TICKS_PER_SECOND);
 			}
 		}
 	}
 	
 	public static boolean shouldHeal(EntityStats stats) {
 		int health = stats.getHealth(stats.entity);
 		int maxHealth = stats.getMaxHealth();
 		boolean shouldHeal = health > 0 && health < maxHealth;
 		if (stats.entity instanceof EntityPlayer) {
 			shouldHeal &= ((EntityPlayer)stats.entity).getFoodStats().getFoodLevel() >= FOOD_THRESHOLD_FOR_HEAL;
 		}
 		return shouldHeal;
 	}
 	
 	private static void updateBaseAttributes(EntityPlayer player, EntityStats stats) {
 		stats.setBaseStrength(EntityStats.BASE_PLAYER_STR + player.experienceLevel * STR_PER_LEVEL);
 		stats.setBaseAgility(EntityStats.BASE_PLAYER_AGI + player.experienceLevel * AGI_PER_LEVEL);
 		stats.setBaseIntelligence(EntityStats.BASE_PLAYER_INT + player.experienceLevel * INT_PER_LEVEL);
 	}
 	
 	@ForgeSubscribe
 	public void onEntityConstructing(EntityConstructing event) {
 		if (event.entity instanceof EntityPlayer && !event.entity.worldObj.isRemote) {
 			event.entity.registerExtendedProperties(EXT_PROP_STATS, getOrCreateEntityStats((EntityLivingBase)event.entity));
 		}
 	}
 	
 	@ForgeSubscribe
 	public void onUseDota2Item(UseItemEvent event) {
 		EntityStats stats = getOrCreateEntityStats(event.entityPlayer);
 		for (BuffInstance buffInst : stats.getAppliedBuffs()) {
 			if (buffInst.buff.isRemovedOnAction) {
 				stats.removeBuff(buffInst);
 			}
 		}
 	}
 	
 	public void removeBuffsOnHurt(EntityLivingBase entity) {
 		boolean shouldSync = false;
 		EntityStats stats = getOrCreateEntityStats(entity);
 		for (BuffInstance buffInst : stats.getAppliedBuffs()) {
 			if (buffInst.buff.isRemovedOnHurt) {
 				stats.removeBuff(buffInst);
 				shouldSync = true;
 			}
 		}
 		if (!entity.worldObj.isRemote && shouldSync) {
 			NetworkUtil.sendToAllAround(new EntityHurtPacket(entity).makePacket(), entity);
 		}
 	}
 	
 	@Override
 	public void onPlayerLogin(EntityPlayer player) {
 		EntityStats stats = getOrCreateEntityStats(player);
 		for (BuffInstance buffInst : stats.getAppliedBuffs()) {
 			if (!buffInst.isItemPassiveBuff()) {
 				PacketDispatcher.sendPacketToAllPlayers(new BuffPacket(buffInst).makePacket());
 			}
 			if (ContinuousEffect.buffHasEffect(buffInst.buff)) {
 				ContinuousEffect effect = ContinuousEffect.construct(buffInst.buff, player);
 				player.worldObj.spawnEntityInWorld(effect);
 			}
 		}
 		stats.sendSyncPacketToClient(player);
 	}
 
 	@Override
 	public void onPlayerLogout(EntityPlayer player) {}
 
 	@Override
 	public void onPlayerChangedDimension(EntityPlayer player) {}
 
 	/**
 	 * Upon respawn the EntityPlayer is constructed anew, however, with a wrong
 	 * entityID at the moment of EntityConstructing event dispatch. That entityID
 	 * is changed later on, but the ExtendedProperties have already been written
 	 * and cannot be removed. So let us manually copy the required values into
 	 * the existing ExtendedProperties.  
 	 */
 	@Override
 	public void onPlayerRespawn(EntityPlayer player) {
 		IExtendedEntityProperties props = (player.getExtendedProperties(EXT_PROP_STATS));
 		if (props != null) {
 			EntityStats oldStats = getOrCreateEntityStats(player);
 			EntityStats newStats = (EntityStats)props;
 			newStats.entity = player;
 			newStats.setGold(oldStats.getReliableGold(), oldStats.getUnreliableGold());
 			Map<EntityLivingBase, EntityStats> entityStats = getEntityStatsMap(getSide(player));
 			entityStats.put(player, newStats);
 			newStats.setMana(newStats.getMaxMana());
 			
 			newStats.sendSyncPacketToClient(player);
 		}
 	}
 }
