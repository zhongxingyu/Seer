 package kabbage.customentitylibrary;
 
 import java.lang.reflect.Field;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import net.minecraft.server.v1_5_R1.EntityLiving;
 import net.minecraft.server.v1_5_R1.ItemStack;
 import net.minecraft.server.v1_5_R1.PathfinderGoal;
 import net.minecraft.server.v1_5_R1.PathfinderGoalSelector;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.craftbukkit.v1_5_R1.CraftWorld;
 import org.bukkit.craftbukkit.v1_5_R1.entity.CraftEntity;
 import org.bukkit.craftbukkit.v1_5_R1.inventory.CraftItemStack;
 import org.bukkit.craftbukkit.v1_5_R1.util.UnsafeList;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Player;
 
 public class CustomEntityWrapper
 {
 	static Map<EntityLiving, CustomEntityWrapper> customEntities = new HashMap<EntityLiving, CustomEntityWrapper>();
 	
 	private EntityLiving entity;
 	private String name;
 	private int health;
 	private int maxHealth;
 	private EntityType type;
 	Map<String, Integer> damagers = new LinkedHashMap<String, Integer>();
 	
 	public boolean immune;
 	
 	@SuppressWarnings("rawtypes")
 	public CustomEntityWrapper(final EntityLiving entity, World world, final double x, final double y, final double z, EntityType type)
 	{
 		this.entity = entity;
 		this.type = type;
 		entity.world = ((CraftWorld) world).getHandle();
 		this.name = type.toString();
 		immune = true;
 		entity.setPosition(x, y-5, z);
 		
 		//The arduous process of changing the entities speed without disrupting the pathfinders in any other way
 		try
 		{
 			float initialSpeed = 0;
			Field speed = EntityLiving.class.getDeclaredField("bG");
 
 			speed.setAccessible(true);
 			initialSpeed = speed.getFloat(entity);
 			speed.setFloat(entity, type.getSpeed());
 			
 			UnsafeList goalSelectorList = null;
 			UnsafeList targetSelectorList = null;
 			PathfinderGoalSelector goalSelector;
 			PathfinderGoalSelector targetSelector;
 			
 			Field gsa = PathfinderGoalSelector.class.getDeclaredField("a");
 			Field goalSelectorField = EntityLiving.class.getDeclaredField("goalSelector");
 			Field targetSelectorField = EntityLiving.class.getDeclaredField("targetSelector");
 			
 			gsa.setAccessible(true);
 			goalSelectorField.setAccessible(true);
 			targetSelectorField.setAccessible(true);
 
 			goalSelector = (PathfinderGoalSelector) goalSelectorField.get(entity);
 			targetSelector = (PathfinderGoalSelector) targetSelectorField.get(entity);
 			goalSelectorList = (UnsafeList) gsa.get(goalSelector);
 			targetSelectorList = (UnsafeList) gsa.get(targetSelector);
 			
 			for(Object goalObject : goalSelectorList)
 			{
 				Field goalField = goalObject.getClass().getDeclaredField("a");
 				goalField.setAccessible(true);
 				PathfinderGoal goal = (PathfinderGoal) goalField.get(goalObject);
 				for(Field f : goal.getClass().getDeclaredFields())
 				{
 					if(f.getType().equals(Float.TYPE))
 					{
 						f.setAccessible(true);
 						float fl = f.getFloat(goal);
 						if(fl == initialSpeed)
 							f.setFloat(goal, type.getSpeed());
 					}
 				}
 			}
 			for(Object goalObject : targetSelectorList)
 			{
 				Field goalField = goalObject.getClass().getDeclaredField("a");
 				goalField.setAccessible(true);
 				PathfinderGoal goal = (PathfinderGoal) goalField.get(goalObject);
 				for(Field f : goal.getClass().getDeclaredFields())
 				{
 					if(f.getType().equals(Float.TYPE))
 					{
 						f.setAccessible(true);
 						float fl = f.getFloat(goal);
 						if(fl == initialSpeed)
 							f.setFloat(goal, type.getSpeed());
 					}
 				}
 			}
 		} catch (Exception e)
 		{
 			e.printStackTrace();
 		}
 
 		org.bukkit.inventory.ItemStack[] items = type.getItems();
 		if(items != null)
 		{
 			for(int i = 0; i <= 4; i++)
 			{
 				if(items[i] != null)
 				{
 					ItemStack item = CraftItemStack.asNMSCopy(items[i]);
 					if(item != null)
 						entity.setEquipment(i, item);
 				}
 			}
 		}
 
 		maxHealth = type.getHealth();
 		health = type.getHealth();
 		
 		customEntities.put(entity, this);
 		//Reload visibility
 		Bukkit.getScheduler().scheduleSyncDelayedTask(CustomEntityLibrary.plugin, new Runnable()
 		{
 			@Override
 			public void run()
 			{
 				if(entity.getHealth() > 0)
 				{
 					entity.setPosition(x, y, z);
 					immune = false;
 				}
 			}
 		},1L);
 	}
 	
 	public void setHealth(int health)
 	{
 		this.health = health;
 	}
 	
 	public EntityLiving getEntity()
 	{
 		return entity;
 	}
 	
 	public int getHealth()
 	{
 		return health;
 	}
 	
 	public void setMaxHealth(int maxHealth)
 	{
 		this.maxHealth = maxHealth;
 		if(health > maxHealth)
 			health = maxHealth;
 	}
 	
 	public int getMaxHealth()
 	{
 		return maxHealth;
 	}
 	
 	public void restoreHealth()
 	{
 		health = maxHealth;
 	}
 	
 	public void modifySpeed(double modifier)
 	{
 		Field f;
 		try
 		{
			f = EntityLiving.class.getDeclaredField("bG");
 
 			f.setAccessible(true);
 			float newSpeed = (float) (f.getFloat(entity) * modifier);
 			f.setFloat(entity, newSpeed);
 		} catch (Exception e)
 		{
 			e.printStackTrace();
 		}
 	}
 	
 	public EntityType getType()
 	{
 		return type;
 	}
 	
 	public void addAttack(Player p, int damage)
     {
     	int damagex = 0;
     	if(damagers.get(p.getName()) != null)
     		damagex = damagers.get(p.getName());
     	damagers.put(p.getName(), damage + damagex);
     }
 	
 	public Player getBestAttacker()
     {
     	String p = null;
     	int damage = 0;
     	for(Entry<String, Integer> e: damagers.entrySet())
     	{
     		if(e.getValue() > damage)
     		{
     			p = e.getKey();
     			damage = e.getValue();
     		}
     	}
     	if(p == null)
     		return null;
     	return Bukkit.getPlayer(p);
     }
 	
     public Player getAssistAttacker()
     {
     	String p = null;
     	String p2 = null;
     	int damage = 0;
     	for(Entry<String, Integer> e: damagers.entrySet())
     	{
     		if(e.getValue() > damage)
     		{
     			p2 = p;
     			p = e.getKey();
     			damage = e.getValue();
     		}
     	}
     	if(p2 == null)
     		return null;
     	return Bukkit.getPlayer(p2);
     }
 
 	public String getName()
 	{
 		return name;
 	}
 	
 	/**
 	 * Allows for a simpler way of checking if an Entity is an instance of a CustomEntityWrapper
 	 * @param entity the entity being checked
 	 * @return whether or not an Entity is an instanceof a CustomEntityWrapper
 	 */
 	public static boolean instanceOf(Entity entity)
 	{
 		if(customEntities.containsKey(((CraftEntity) entity).getHandle()))
 			return true;
 		return false;
 	}
 	
 	/**
 	 * Allows for a simpler way of converting an Entity to a CustomEntity
 	 * @param entity being converted to a CustomEntityWrapper
 	 * @return a CustomEntityWrapper instance of the entity, or null if none exists
 	 */
 	public static CustomEntityWrapper getCustomEntity(Entity entity)
 	{
 		if(customEntities.containsKey(((CraftEntity) entity).getHandle()))
 			return customEntities.get(((CraftEntity) entity).getHandle());
 		return null;
 	}
 	
 	public static CustomEntityWrapper spawnCustomEntity(EntityLiving entity, World world, double x, double y, double z, EntityType type)
 	{
 		CustomEntityWrapper customEnt = new CustomEntityWrapper(entity, world, x, y, z, type);
 		CustomEntitySpawnEvent event = new CustomEntitySpawnEvent(customEnt, new Location(world, x, y, z));
 		Bukkit.getPluginManager().callEvent(event);
 		if(event.isCancelled())
 		{
 			customEnt.getEntity().setHealth(0);
 			return null;
 		}
 		return customEnt;
 	}
 	
 	public static CustomEntityWrapper spawnCustomEntity(EntityLiving entity, Location location, EntityType type)
 	{
 		return spawnCustomEntity(entity, location.getWorld(), location.getX(), location.getY(), location.getZ(), type);
 	}
 }
