 package com.github.thebiologist13;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Random;
 
 import org.bukkit.DyeColor;
 import org.bukkit.Location;
 import org.bukkit.Server;
 import org.bukkit.entity.Animals;
 import org.bukkit.entity.Creeper;
 import org.bukkit.entity.Enderman;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.Golem;
 import org.bukkit.entity.IronGolem;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.MagmaCube;
 import org.bukkit.entity.Monster;
 import org.bukkit.entity.Ocelot;
 import org.bukkit.entity.Pig;
 import org.bukkit.entity.PigZombie;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Sheep;
 import org.bukkit.entity.Slime;
 import org.bukkit.entity.Spider;
 import org.bukkit.entity.Villager;
 import org.bukkit.entity.Wolf;
 import org.bukkit.potion.PotionEffect;
 
 public class Spawner {
 	//Identification variables
 	private String name = "";
 	private int id = -1; //Identification # for spawner
 	
 	//TODO Make sure this is compatible with all files
 	private HashMap<Integer, SpawnableEntity> typeData = new HashMap<Integer, SpawnableEntity>(); //SpawnableEntities in format SpawnableEntity ID #, SpawnableEntity
 	
 	//Spawner Properties
 	private boolean active = false; //Is this spawner active?
 	private boolean hidden = false; //Is the spawner hidden?
 	private double radius = 0; //Radius that it can spawn in
 	private boolean useSpawnArea = false; //Should it use a separate spawn area?
 	private boolean redstoneTriggered = false; //Does it need to be redstone triggered to spawn?
 	private int maxPlayerDistance = -1; //Maximum distance a player can be from it to spawn mobs
 	private int minPlayerDistance = -1; //Minimum distance
 	private byte maxLightLevel = -1; //Maximum light level the spawner can spawn entities at
 	private byte minLightLevel = -1; //Minimum light level
 	private int mobsPerSpawn = 0; //Amount of mobs to spawn per time
 	private int maxMobs = -1; //Maximum amount of mobs it will spawn
 	
 	//Location variables
 	private Location loc = null; //Location of the spawner
 	private Location[] areaPoints = new Location[2]; //Points for a spawn area.
 	
 	//Spawning rates/timing
 	private int ticksLeft = -1; //Ticks left before next spawn
 	private int rate = -1; //Rate in how many ticks to spawn at
 	
 	//List of mobs
 	private ArrayList<Integer> mobs = new ArrayList<Integer>(); //Integer is mob ID. This holds the entities that have been spawned so when one dies, it can be removed from maxMobs.
 	
 	//Other
 	private Server server = null; //Used to get online players
 	
 	/*
 	 * Constructors
 	 */
 	
 	public Spawner(SpawnableEntity type, Location loc, int id, Server server) {
 		this.id = id;
 		this.loc = loc;
 		this.server = server;
 		typeData.put(type.getId(), type);
 		
 		areaPoints[0] = loc;
 		areaPoints[1] = loc;
 	}
 	
 	public Spawner(SpawnableEntity type, Location loc, String name, int id, Server server) {
 		this.id = id;
 		this.name = name;
 		this.loc = loc;
 		this.server = server;
 		typeData.put(type.getId(), type);
 		
 		areaPoints[0] = loc;
 		areaPoints[1] = loc;
 	}
 	
 	public int getId() {
 		return id;
 	}
 	
 	public String getName() {
 		return name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	public HashMap<Integer, SpawnableEntity> getTypeData() {
 		return typeData;
 	}
 
 	public void setTypeData(HashMap<Integer, SpawnableEntity> typeData) {
 		
 		if(typeData == null) {
 			return;
 		}
 		
 		this.typeData = typeData;
 	}
 	
 	public void addTypeData(SpawnableEntity data) {
 		typeData.put(data.getId(), data);
 	}
 	
 	public boolean isActive() {
 		return active;
 	}
 
 	public void setActive(boolean active) {
 		this.active = active;
 	}
 
 	public boolean isHidden() {
 		return hidden;
 	}
 
 	public void setHidden(boolean hidden) {
 		this.hidden = hidden;
 	}
 
 	public double getRadius() {
 		return radius;
 	}
 
 	public void setRadius(double radius) {
 		this.radius = radius;
 	}
 
 	public boolean isUsingSpawnArea() {
 		return useSpawnArea;
 	}
 
 	public void setUseSpawnArea(boolean useSpawnArea) {
 		this.useSpawnArea = useSpawnArea;
 	}
 
 	public boolean isRedstoneTriggered() {
 		return redstoneTriggered;
 	}
 
 	public void setRedstoneTriggered(boolean redstoneTriggered) {
 		this.redstoneTriggered = redstoneTriggered;
 	}
 
 	public int getMaxPlayerDistance() {
 		return maxPlayerDistance;
 	}
 
 	public void setMaxPlayerDistance(int maxPlayerDistance) {
 		this.maxPlayerDistance = maxPlayerDistance;
 	}
 
 	public int getMinPlayerDistance() {
 		return minPlayerDistance;
 	}
 
 	public void setMinPlayerDistance(int minPlayerDistance) {
 		this.minPlayerDistance = minPlayerDistance;
 	}
 
 	public byte getMaxLightLevel() {
 		return maxLightLevel;
 	}
 
 	public void setMaxLightLevel(byte maxLightLevel) {
 		this.maxLightLevel = maxLightLevel;
 	}
 
 	public byte getMinLightLevel() {
 		return minLightLevel;
 	}
 
 	public void setMinLightLevel(byte minLightLevel) {
 		this.minLightLevel = minLightLevel;
 	}
 
 	public int getMobsPerSpawn() {
 		return mobsPerSpawn;
 	}
 
 	public void setMobsPerSpawn(int mobsPerSpawn) {
 		this.mobsPerSpawn = mobsPerSpawn;
 	}
 
 	public int getMaxMobs() {
 		return maxMobs;
 	}
 
 	public void setMaxMobs(int maxMobs) {
 		this.maxMobs = maxMobs;
 	}
 
 	public Location getLoc() {
 		return loc;
 	}
 
 	public void setLoc(Location loc) {
 		this.loc = loc;
 	}
 
 	public Location[] getAreaPoints() {
 		return areaPoints;
 	}
 
 	public void setAreaPoints(Location[] areaPoints) {
 		this.areaPoints = areaPoints;
 	}
 	
 	public void changeAreaPoint(int index, Location value) {
 		if(index != 0 || index != 1) {
 			return;
 		}
 		
 		areaPoints[index] = value;
 	}
 
 	public int getRate() {
 		return rate;
 	}
 
 	public void setRate(int rate) {
 		this.rate = rate;
 		this.ticksLeft = rate;
 	}
 
 	public ArrayList<Integer> getMobs() {
 		return mobs;
 	}
 	
 	public void setMobs(ArrayList<Integer> mobs) {
 		this.mobs = mobs;
 	}
 	
 	public void setMobsFromIDs(ArrayList<Integer> mobs) {
 		this.mobs = mobs;
 	}
 
 	//Change the name
 	public void changeName(String newName) {
 		this.name = newName;
 	}
 	
 	/*
 	 * Methods for spawning, timing, etc.
 	 */
 	
 	//Tick the spawn rate down and spawn mobs if it is time to spawn. Return the ticks left.
 	public int tick() {
 		if(active && !(rate <= 0)) {
 			ticksLeft--;
 			if(ticksLeft == 0) {
 				ticksLeft = rate;
 				spawn();
 				return 0;
 			}
 		}
 		
 		return ticksLeft;
 	}
 	
 	//Remove the spawner
 	public void remove() {
 		/*
 		 * If the ID is -1, it should be removed next tick by not 
 		 * calling these functions and removing it's file.
 		 * 
 		 * Won't happen initially because when the spawner is initialized, it is given an ID
 		 */
 		this.id = -1;
 		this.active = false;
 	}
 	
 	//Spawn the mobs
 	public void spawn() {
 		//Are conditions valid for spawning?
 		boolean canSpawn = true;
 		//Random locations
 		double randX = 0;
 		double randY = 0;
 		double randZ = 0;
 		
 		//If the spawner is not active, return
 		if(!active) {
 			return;
 		}
 		
 		/*
 		 * This block checks if the conditions are met to spawn mobs
 		 */
 		if(redstoneTriggered && !loc.getBlock().isBlockPowered()) {
 			canSpawn = false;
 		} else if(!isPlayerNearby()) {
 			canSpawn = false;
 		} else if(!(loc.getBlock().getLightLevel() <= maxLightLevel) && !(loc.getBlock().getLightLevel() >= minLightLevel)) {
 			canSpawn = false;
 		} else if(mobs.size() >= maxMobs) {
 			canSpawn = false;
 		}
 		
 		//If we can spawn
 		if(canSpawn) {
 			//Loop to spawn until the mobs per spawn is reached
 			for(int i = 0; i < mobsPerSpawn; i++) {
 				//Break the loop if the spawn limit is reached
 				if(mobs.size() == maxMobs) {
 					break;
 				}
 				
 				//Has the mob been spawned?
 				boolean spawned = false;
 				
 				//Amount of times tried
 				int tries = 0;
 				
 				//As long as the mob has not been spawned, keep trying
 				while(!spawned && tries <= 512) {
 					
 					Location spawnLoc = null;
 					Location blockBelow = null;
 					Location blockAbove = null;
 					boolean acceptableLight = false;
 					byte light = 0;
 					
 					if(!useSpawnArea) {
 						//Generates a random location using randomGenRad(), a location for the block above, and a location for the block below.
 						randX = randomGenRad()  + loc.getBlockX();
 						randY = Math.round(randomGenRad()) + loc.getBlockY();
 						randZ = randomGenRad() + loc.getBlockZ();
 						
 					} else {
 						//Generates a random location in the spawn area using randomGenRange().
 						randX = randomGenRange(areaPoints[0].getX(), areaPoints[1].getX());
 						randY = randomGenRange(areaPoints[0].getY(), areaPoints[1].getY());
 						randZ = randomGenRange(areaPoints[0].getZ(), areaPoints[1].getZ());
 					}
 					
 					spawnLoc = new Location(loc.getWorld(), randX, randY, randZ);
 					blockBelow = new Location(loc.getWorld(), randX, randY - 1, randZ);
 					blockAbove = new Location(loc.getWorld(), randX, randY + 1, randZ);
 					
 					light = spawnLoc.getBlock().getLightLevel();
 					if(light <= maxLightLevel && light >= minLightLevel) {
 						acceptableLight = true;
 					}
 					
 					/*
 					 * If the blocks are empty, it has something to stand on, and the light level is acceptable: 
 					 * spawn the creature and add it to the list of mobs, then set this mob spawned
 					 */
 					if(spawnLoc.getBlock().isEmpty() && blockAbove.getBlock().isEmpty() && !blockBelow.getBlock().isEmpty() && acceptableLight) {
 						SpawnableEntity type = randType();
 						LivingEntity le = loc.getWorld().spawnCreature(spawnLoc, type.getType());
 						if(le != null) {
 							assignMobProps(le, type);
 							if(type.isJockey()) {
 								makeJockey(le);
 							}
 							mobs.add(le.getEntityId());
 							spawned = true;
 						}
 					}
 					
 					tries++;
 				}
 			}
 		}
 	}
 	
 	//Spawn the mobs
 	public void forceSpawn() {
 		//Random locations
 		double randX = 0;
 		double randY = 0;
 		double randZ = 0;
 
 		//Loop to spawn until the mobs per spawn is reached
 		for(int i = 0; i < mobsPerSpawn; i++) {
 			
 			//Has the mob been spawned?
 			boolean spawned = false;
 			
 			//Amount of times tried
 			int tries = 0;
 			
 			//As long as the mob has not been spawned, keep trying
 			while(!spawned && tries <= 512) {
 				
 				Location spawnLoc = null;
 				Location blockBelow = null;
 				Location blockAbove = null;
 				
 				if(!useSpawnArea) {
 					//Generates a random location using randomGenRad(), a location for the block above, and a location for the block below.
 					randX = randomGenRad()  + loc.getBlockX();
 					randY = Math.round(randomGenRad()) + loc.getBlockY();
 					randZ = randomGenRad() + loc.getBlockZ();
 					
 				} else {
 					//Generates a random location in the spawn area using randomGenRange().
 					randX = randomGenRange(areaPoints[0].getX(), areaPoints[1].getX());
 					randY = randomGenRange(areaPoints[0].getY(), areaPoints[1].getY());
 					randZ = randomGenRange(areaPoints[0].getZ(), areaPoints[1].getZ());
 				}
 				
 				spawnLoc = new Location(loc.getWorld(), randX, randY, randZ);
 				blockBelow = new Location(loc.getWorld(), randX, randY - 1, randZ);
 				blockAbove = new Location(loc.getWorld(), randX, randY + 1, randZ);
 				
 				/*
 				 * If the blocks are empty, it has something to stand on, and the light level is acceptable: 
 				 * spawn the creature and add it to the list of mobs, then set this mob spawned
 				 */
 				if(spawnLoc.getBlock().isEmpty() && blockAbove.getBlock().isEmpty() && !blockBelow.getBlock().isEmpty()) {
 					SpawnableEntity type = randType();
 					LivingEntity le = loc.getWorld().spawnCreature(spawnLoc, type.getType());
 					if(le != null) {
 						assignMobProps(le, type);
 						if(type.isJockey()) {
 							makeJockey(le);
 						}
 						mobs.add(le.getEntityId());
 						spawned = true;
 					}
 				}
 				
 				tries++;
 			}
 		}
 	}
 	
 	//Spawn the mobs
 	public void forceSpawnType(SpawnableEntity entity) {
 		//Random locations
 		double randX = 0;
 		double randY = 0;
 		double randZ = 0;
 
 		//Loop to spawn until the mobs per spawn is reached
 		for(int i = 0; i < mobsPerSpawn; i++) {
 			
 			//Has the mob been spawned?
 			boolean spawned = false;
 			
 			//Amount of times tried
 			int tries = 0;
 			
 			//As long as the mob has not been spawned, keep trying
 			while(!spawned && tries <= 512) {
 				
 				Location spawnLoc = null;
 				Location blockBelow = null;
 				Location blockAbove = null;
 				
 				if(!useSpawnArea) {
 					//Generates a random location using randomGenRad(), a location for the block above, and a location for the block below.
 					randX = randomGenRad()  + loc.getBlockX();
 					randY = Math.round(randomGenRad()) + loc.getBlockY();
 					randZ = randomGenRad() + loc.getBlockZ();
 					
 				} else {
 					//Generates a random location in the spawn area using randomGenRange().
 					randX = randomGenRange(areaPoints[0].getX(), areaPoints[1].getX());
 					randY = randomGenRange(areaPoints[0].getY(), areaPoints[1].getY());
 					randZ = randomGenRange(areaPoints[0].getZ(), areaPoints[1].getZ());
 				}
 				
 				spawnLoc = new Location(loc.getWorld(), randX, randY, randZ);
 				blockBelow = new Location(loc.getWorld(), randX, randY - 1, randZ);
 				blockAbove = new Location(loc.getWorld(), randX, randY + 1, randZ);
 				
 				/*
 				 * If the blocks are empty, it has something to stand on, and the light level is acceptable: 
 				 * spawn the creature and add it to the list of mobs, then set this mob spawned
 				 */
 				if(spawnLoc.getBlock().isEmpty() && blockAbove.getBlock().isEmpty() && !blockBelow.getBlock().isEmpty()) {
 					LivingEntity le = loc.getWorld().spawnCreature(spawnLoc, entity.getType());
 					if(le != null) {
 						assignMobProps(le, entity);
 						if(entity.isJockey()) {
 							makeJockey(le);
 						}
 						mobs.add(le.getEntityId());
 						spawned = true;
 					}
 				}
 				
 				tries++;
 			}
 		}
 	}
 	
 	/*
 	 * Methods for choosing locations, checking things, etc.
 	 */
 	
 	//Check if players are nearby
 	private boolean isPlayerNearby() {
 		for(Player p : server.getOnlinePlayers()) {
 			//Finds distance between spawner and player in 3D space.
 			double distance = Math.sqrt(
 					Math.pow((p.getLocation().getX() - loc.getX()), 2) + // x coordinate
 					Math.pow((p.getLocation().getY() - loc.getY()), 2) + // y coordinate
 					Math.pow((p.getLocation().getZ() - loc.getZ()), 2)   // z coordinate
 					);
 			if(distance <= maxPlayerDistance && distance >= minPlayerDistance) {
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	//Check if players are nearby
 	private Player[] getNearbyPlayers(Location source, double max) {
 		ArrayList<Player> players = new ArrayList<Player>();
 		for(Player p : server.getOnlinePlayers()) {
 			//Finds distance between spawner and player is 3D space.
 			double distance = Math.sqrt(
 					Math.pow((p.getLocation().getX() - source.getX()), 2) + // x coordinate
 					Math.pow((p.getLocation().getY() - source.getY()), 2) + // y coordinate
 					Math.pow((p.getLocation().getZ() - source.getZ()), 2)   // z coordinate
 					);
 			if(distance <= max) {
 				players.add(p);
 			}
 		}
 		return (Player[]) players.toArray();
 	}
 	
 	//Generate random double for location's parts within radius
 	private double randomGenRad() {
 		Random rand = new Random();
 		if(rand.nextFloat() < 0.5) {
 			return (rand.nextDouble() * radius) * -1;
 		} else {
 			return (rand.nextDouble() * radius) * 1;
 		}
 	}
 	
 	private double randomGenRange(double arg0, double arg1) {
 		Random rand = new Random();
 		double difference = Math.abs(arg0 - arg1);
 		if(arg0 < arg1) {
 			return arg0 + (difference * rand.nextDouble());
 		} else if(arg1 < arg0) {
 			return arg1 + (difference * rand.nextDouble());
 		} else {
 			return arg0;
 		}
 	}
 	
 	private SpawnableEntity randType() {
 		Random rand = new Random();
 		int index = rand.nextInt(typeData.size());
 		int count = 0;
		
 		for(SpawnableEntity e : typeData.values()) {
 			if(count == index) {
 				return e;
			} else {
				count++;
 			}
 		}
		
 		return null;
 	}
 	
 	//Assigns properties to a LivingEntity
 	private void assignMobProps(LivingEntity entity, SpawnableEntity data) {
 		for(EntityPotionEffect p : data.getEffects()) {
 			PotionEffect ef = new PotionEffect(p.getType(), p.getDuration(), p.getAmplifier());
 			entity.addPotionEffect(ef, true);
 		}
 		entity.setVelocity(data.getVelocity());
 		
 		//Health handling
 		if(data.getHealth() == -2) {
 			entity.setHealth(1);
 		} else if(data.getHealth() == -1) {
 			entity.setHealth(entity.getMaxHealth());
 		} else {
 			if(data.getHealth() > entity.getMaxHealth()) {
 				entity.setHealth(entity.getMaxHealth());
 			} else if(data.getHealth() < 0) {
 				entity.setHealth(0);
 			} else {
 				entity.setHealth(data.getHealth());
 			}
 		}
 		
 		//Air handling
 		if(data.getAir() == -2) {
 			entity.setRemainingAir(0);
 		} else if(data.getAir() == -1) {
 			entity.setRemainingAir(entity.getMaximumAir());
 		} else {
 			entity.setRemainingAir(data.getAir());
 		}
 		
 		if(entity instanceof Animals) {
 			Animals animal = (Animals) entity;
 			
 			//Age handling
 			if(data.getAir() == -2) {
 				animal.setBaby();
 			} else if(data.getAir() == -1) {
 				animal.setAdult();
 			} else {
 				animal.setAge(data.getAge());
 			}
 			
 			//Setting animal specific properties
 			if(animal instanceof Pig) {
 				Pig p = (Pig) animal;
 				p.setSaddle(data.isSaddled());
 			} else if(animal instanceof Sheep) {
 				Sheep s = (Sheep) animal;
 				DyeColor color = DyeColor.valueOf(data.getColor());
 				s.setColor(color);
 			} else if(animal instanceof Wolf) {
 				Wolf w = (Wolf) animal;
 				w.setAngry(data.isAngry());
 				w.setTamed(data.isTamed());
 				if(data.isTamed()) {
 					w.setSitting(data.isSitting());
 				}
 			} else if(animal instanceof Ocelot) {
 				Ocelot o = (Ocelot) animal;
 				o.setTamed(data.isTamed());
 				if(data.isTamed()) {
 					Ocelot.Type catType = Ocelot.Type.valueOf(data.getCatType());
 					o.setCatType(catType);
 					o.setSitting(data.isSitting());
 				}
 			}
 		} else if(entity instanceof Villager) {
 			Villager v = (Villager) entity;
 			v.setAge(data.getAge());
 			v.setProfession(data.getProfession());
 		} else if(entity instanceof Monster) {
 			Monster monster = (Monster) entity;
 			
 			//Setting monster specific properties.
 			if(monster instanceof Enderman) {
 				Enderman e = (Enderman) monster;
 				e.setCarriedMaterial(data.getEndermanBlock());
 			} else if(monster instanceof Creeper) {
 				Creeper c = (Creeper) monster;
 				c.setPowered(data.isCharged());
 			} else if(monster instanceof Slime) {
 				Slime s = (Slime) monster;
 				s.setSize(data.getSlimeSize());
 			} else if(monster instanceof MagmaCube) {
 				MagmaCube m = (MagmaCube) monster;
 				m.setSize(data.getSlimeSize());
 			} else if(monster instanceof PigZombie) {
 				PigZombie p = (PigZombie) monster;
 				if(data.isAngry()) {
 					p.setAngry(true);
 				}
 			}
 		} else if(entity instanceof Golem) {
 			Golem golem = (Golem) entity;
 			
 			if(golem instanceof IronGolem) { //TODO may or may not work
 				IronGolem i = (IronGolem) golem;
 				if(data.isAngry()) {
 					Player[] players = server.getOnlinePlayers();
 					int index = (int) Math.round(Math.rint(players.length - 1));
 					Player[] nearPlayers = getNearbyPlayers(i.getLocation(), 16);
 					if(nearPlayers != null) {
 						i.setPlayerCreated(false);
 						i.damage(0, nearPlayers[index]);
 					}
 				}
 			}
 		}
 	}
 	
 	//Makes a spider jockey
 	private void makeJockey(LivingEntity spider) {
 		if(spider instanceof Spider) {
 			Spider s = (Spider) spider;
 			Location spiderLoc = s.getLocation();
 			LivingEntity skele = spiderLoc.getWorld().spawnCreature(spiderLoc, EntityType.SKELETON);
 			s.setPassenger(skele);
 		} else {
 			return;
 		}
 	}
 }
