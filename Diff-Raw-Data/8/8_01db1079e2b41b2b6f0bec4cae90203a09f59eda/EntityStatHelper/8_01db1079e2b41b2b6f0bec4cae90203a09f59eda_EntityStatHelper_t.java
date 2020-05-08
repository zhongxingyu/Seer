 package assets.fyresmodjam;
 
 import java.io.ByteArrayOutputStream;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.lang.reflect.Constructor;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Random;
 
 import cpw.mods.fml.common.ICraftingHandler;
 import cpw.mods.fml.common.network.PacketDispatcher;
 import cpw.mods.fml.common.network.Player;
 import cpw.mods.fml.common.registry.GameRegistry;
 
 import assets.fyresmodjam.EntityStatHelper.EntityStat;
 import assets.fyresmodjam.ItemStatHelper.ItemStatTracker;
 
 import net.minecraft.entity.Entity;
 import net.minecraft.entity.EntityLiving;
 import net.minecraft.entity.monster.EntityMob;
 import net.minecraft.entity.EntityLivingBase;
 import net.minecraft.entity.boss.EntityDragon;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.entity.player.EntityPlayerMP;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraft.nbt.NBTTagCompound;
 import net.minecraft.nbt.NBTTagList;
 import net.minecraft.nbt.NBTTagString;
 import net.minecraft.network.packet.Packet;
 import net.minecraft.network.packet.Packet250CustomPayload;
 import net.minecraft.network.packet.Packet5PlayerInventory;
 import net.minecraft.potion.Potion;
 import net.minecraft.potion.PotionEffect;
 import net.minecraft.world.World;
 import net.minecraft.world.WorldServer;
 import net.minecraftforge.common.MinecraftForge;
 import net.minecraftforge.event.ForgeSubscribe;
 import net.minecraftforge.event.entity.EntityJoinWorldEvent;
 import net.minecraftforge.event.entity.living.LivingDeathEvent;
 import net.minecraftforge.event.entity.living.LivingHurtEvent;
 import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
 
 public class EntityStatHelper {
 	
 	//There's probably a better way of doing all of this. :P Oh well.
 	
 	public static class EntityStatTracker {
 		public Class[] classes;
 		
 		public boolean instanceAllowed = false;
 		
 		public EntityStatTracker(Class[] classes, boolean instancesAllowed) {this.classes = classes; this.instanceAllowed = instancesAllowed;}
 		public EntityStatTracker(Class c, boolean instancesAllowed) {this(new Class[] {c}, instancesAllowed);}
 
 		//public HashMap<String, String> stats = new HashMap<String, String>();
 		public ArrayList<EntityStat> stats = new ArrayList<EntityStat>();
 		//public StatTracker giveStat(String name, String value) {stats.put(name, value); return this;}
 
 		public void addStat(EntityStat stat) {
 			if(!stats.contains(stat)) {stats.add(stat);}
 		}
 	}
 	
 	public static class EntityStat {
 		public String name;
 		public String value;
 		
 		public EntityStat(String name, String value) {
 			this.name = name;
 			this.value = value;
 		}
 		
 		public Object getNewValue(Random r) {return value;}
 		public String getAlteredEntityName(EntityLiving entity) {return entity.getEntityName();}
 		public void modifyEntity(Entity entity) {}
 	}
 	
 	public static HashMap<Class, EntityStatTracker> statTrackersByClass = new HashMap<Class, EntityStatTracker>();
 	public static ArrayList<EntityStatTracker> genericTrackers = new ArrayList<EntityStatTracker>();
 	
 	public static void addStatTracker(EntityStatTracker statTracker) {
 		if(statTracker.classes != null) {
 			for(Class c : statTracker.classes) {statTrackersByClass.put(c, statTracker);}
 			if(statTracker.instanceAllowed) {genericTrackers.add(statTracker);}
 		}
 	}
 	
 	public static Entity giveStat(Entity entity, String name, Object value) {
 		if(entity != null && name != null && value != null) {entity.getEntityData().setString(name, value.toString());}
 		return entity;
 	}
 	
 	public static Entity setName(EntityLiving entity, String name) {
 		entity.setCustomNameTag(name);
 		//if(entity.getCustomNameTag().equals(entity.getEntityName())) {entity.setCustomNameTag("");}
 		return entity;
 	}
 	
 	public static String getStat(Entity entity, String name) {
 		String s = null;
 		if(entity.getEntityData() != null && entity.getEntityData().hasKey(name)) {s = entity.getEntityData().getString(name);}
 		return s;
 	}
 	
 	public static boolean hasStat(Entity entity, String name) {
 		if(entity.getEntityData() != null && entity.getEntityData().hasKey(name)) {return true;}
 		return false;
 	}
 	
 	@ForgeSubscribe
 	public void entityJoinWorld(EntityJoinWorldEvent event) {
 		if(!event.world.isRemote) {
 			processEntity(event.entity, ModjamMod.r);
 			
 			boolean isClone = true;
 			
 			try {
 				isClone = event.entity.getDataWatcher().getWatchableObjectByte(30) != 0;
 			} catch (Exception e) {isClone = false;}
 			
 			if(CommonTickHandler.worldData != null && CommonTickHandler.worldData.currentDisadvantage.equals("Increased Mob Spawn") && (event.entity instanceof EntityMob) && !(event.entity instanceof EntityDragon) && !isClone && ModjamMod.r.nextBoolean()) {
 				
 				event.entity.getDataWatcher().addObject(30, (byte) 1);
 				
 				Entity entityNew = null;
 				
 				try {
 					Constructor[] constructors = event.entity.getClass().getConstructors();
 					
 					for(int i = 0; i < constructors.length; i++) {
 						Class[] parameters = constructors[i].getParameterTypes();
 						if(parameters.length == 1 && parameters[0].equals(World.class)) {entityNew = (Entity) event.entity.getClass().getConstructors()[i].newInstance(event.world);}
 					}
 					
 				} catch (Exception e) {e.printStackTrace();}
 				
 				if(entityNew != null) {
					try {
						entityNew.setLocationAndAngles(event.entity.posX, event.entity.posY, event.entity.posZ, event.entity.rotationYaw, event.entity.rotationPitch);
						entityNew.getDataWatcher().addObject(30, (byte) 1);
						event.world.spawnEntityInWorld(entityNew);
					} catch (Exception e) {System.out.println("Error.");}
 				}
 			}
 		}
 	}
 	
 	public static ArrayList<EntityStatTracker> temp = new ArrayList<EntityStatTracker>();
 	
 	public static boolean b = false;
 	
 	public static void processEntity(Entity entity, Random r) {
 		
 		if(entity == null) {return;}
 		
 		temp.clear();
 		
 		if(statTrackersByClass.containsKey(entity.getClass())) {temp.add(statTrackersByClass.get(entity.getClass()));}
 		
 		for(EntityStatTracker e : genericTrackers) {
 			if(!temp.contains(e)) {
 				for(Class c : e.classes) {if(c.isAssignableFrom(entity.getClass())) {temp.add(e); break;}}
 			}
 		}
 		
 		if(!temp.isEmpty()) {
 			String processed = EntityStatHelper.getStat(entity, "processed");
 			
 			if(processed == null || processed.equals("false")) {
 				EntityStatHelper.giveStat(entity, "processed", "true");
 				
 				for(EntityStatTracker statTracker : temp) {
 					for(EntityStat s : statTracker.stats) {
 						giveStat(entity, s.name, s.getNewValue(r).toString());
 						if(entity instanceof EntityLiving) {setName((EntityLiving) entity, s.getAlteredEntityName((EntityLiving) entity));}
 						s.modifyEntity(entity);
 					}
 				}
 			}
 		}
 		
 		/*if(entity instanceof EntityLiving) {
 			String entityName = ((EntityLiving) entity).hasCustomNameTag() ? ((EntityLiving) entity).getCustomNameTag() : null;
 			
 			if(FyresWorldData.currentDisadvantage != null && FyresWorldData.currentDisadvantage.equals("Illiterate")) {
 				if((entityName == null || !entityName.startsWith("\u00A7k"))) {
 					setName((EntityLiving) entity, "\u00A7k" + (entityName == null ? entity.getEntityName() : entityName));
 				}
 			} else if(entityName != null && entityName.startsWith("\u00A7k")) {
 				setName((EntityLiving) entity, entityName.replace("\u00A7k", ""));
 			}
 		}*/
 	}
 	
 	public void register() {
 		MinecraftForge.EVENT_BUS.register(this);
 	}
 	
 	@ForgeSubscribe
 	public void livingDeath(LivingDeathEvent event) {
 		if(!event.entity.worldObj.isRemote) {
 			if(event.entity.worldObj.getGameRules().getGameRuleBooleanValue("doMobLoot")) {
 				
 				if(event.entity instanceof EntityLivingBase && event.source != null && event.source.getEntity() != null) {
 					if(event.source.getEntity().getEntityData().hasKey("Blessing")) {
 						String blessing = event.source.getEntity().getEntityData().getString("Blessing");
 						
 						if(blessing.equals("Thief") && ModjamMod.r.nextInt(10) == 0) {
 							if(!event.entity.worldObj.isRemote) {event.entity.dropItem(Item.goldNugget.itemID, 1);}
 							//event.entity.worldObj.playSoundAtEntity(event.entity, "fyresmodjam:coin", 1.0F, 1.0F);
 						}
 					}
 				}
 				
 				int level = 0;
 				if(event.entity.getEntityData().hasKey("Level")) {level = Integer.parseInt(event.entity.getEntityData().getString("Level"));}
 				if(ModjamMod.r.nextInt(10) == 0 || level == 5) {event.entity.entityDropItem(new ItemStack(ModjamMod.mysteryPotion.itemID, 1, ModjamMod.r.nextInt(13)), event.entity.height/2);}
 			
 			}
 			
 			if(CommonTickHandler.worldData.currentTask.equals("Kill") && CommonTickHandler.worldData.validMobs[CommonTickHandler.worldData.currentTaskID].isAssignableFrom(event.entity.getClass())) {
 				CommonTickHandler.worldData.progress++;
 				
 				if(CommonTickHandler.worldData.progress >= CommonTickHandler.worldData.currentTaskAmount) {
 					CommonTickHandler.worldData.progress = 0;
 					CommonTickHandler.worldData.tasksCompleted++;
 					
 					CommonTickHandler.worldData.giveNewTask();
 					
 					PacketDispatcher.sendPacketToAllPlayers(PacketHandler.newPacket(PacketHandler.SEND_MESSAGE, new Object[] {"\u00A7eA world goal has been completed!" + (!CommonTickHandler.worldData.currentDisadvantage.equals("None") ? " World disadvantage has been lifted!": "")}));
 					PacketDispatcher.sendPacketToAllPlayers(PacketHandler.newPacket(PacketHandler.SEND_MESSAGE, new Object[] {"\u00A7eA new world goal has been set: " + (CommonTickHandler.worldData.currentTask + " " + CommonTickHandler.worldData.currentTaskAmount + " " + (CommonTickHandler.worldData.currentTask.equals("Kill") ? FyresWorldData.validMobNames[CommonTickHandler.worldData.currentTaskID] : new ItemStack(Item.itemsList[CommonTickHandler.worldData.currentTaskID], 1).getDisplayName()) + "s. (" + CommonTickHandler.worldData.progress + " " + CommonTickHandler.worldData.currentTask + "ed)")}));
 					
 					CommonTickHandler.worldData.currentDisadvantage = "None";
 				}
 				
 				PacketDispatcher.sendPacketToAllPlayers(PacketHandler.newPacket(PacketHandler.UPDATE_WORLD_DATA, new Object[] {CommonTickHandler.worldData.potionValues, CommonTickHandler.worldData.potionDurations, CommonTickHandler.worldData.currentDisadvantage, CommonTickHandler.worldData.currentTask, CommonTickHandler.worldData.currentTaskID, CommonTickHandler.worldData.currentTaskAmount, CommonTickHandler.worldData.progress, CommonTickHandler.worldData.tasksCompleted, CommonTickHandler.worldData.enderDragonKilled}));
 			
 				CommonTickHandler.worldData.markDirty();
 			}
 			
 			if(!CommonTickHandler.worldData.enderDragonKilled && event.entity instanceof EntityDragon) {
 				CommonTickHandler.worldData.enderDragonKilled = true;
 				PacketDispatcher.sendPacketToAllPlayers(PacketHandler.newPacket(PacketHandler.UPDATE_WORLD_DATA, new Object[] {CommonTickHandler.worldData.potionValues, CommonTickHandler.worldData.potionDurations, CommonTickHandler.worldData.currentDisadvantage, CommonTickHandler.worldData.currentTask, CommonTickHandler.worldData.currentTaskID, CommonTickHandler.worldData.currentTaskAmount, CommonTickHandler.worldData.progress, CommonTickHandler.worldData.tasksCompleted, CommonTickHandler.worldData.enderDragonKilled}));
 				CommonTickHandler.worldData.markDirty();
 			}
 		}
 		
 		if(event.entity instanceof EntityPlayer) {
 			((EntityPlayer) event.entity).triggerAchievement(ModjamMod.losingIsFun);
 		}
 	}
 }
