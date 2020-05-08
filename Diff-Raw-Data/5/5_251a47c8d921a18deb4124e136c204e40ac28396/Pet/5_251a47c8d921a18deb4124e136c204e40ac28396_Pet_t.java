 package com.censoredsoftware.demigods.player;
 
 import com.censoredsoftware.demigods.Demigods;
 import com.censoredsoftware.demigods.battle.Participant;
 import com.censoredsoftware.demigods.data.DataManager;
 import com.censoredsoftware.demigods.deity.Deity;
 import com.censoredsoftware.demigods.helper.ConfigFile;
import com.censoredsoftware.demigods.util.Errors;
 import com.google.common.base.Predicate;
 import com.google.common.collect.Collections2;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Sets;
 import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.serialization.ConfigurationSerializable;
 import org.bukkit.entity.*;
 
 import javax.annotation.Nullable;
 import java.util.*;
 import java.util.concurrent.ConcurrentHashMap;
 
 public class Pet implements Participant, ConfigurationSerializable
 {
 	private UUID id;
 	private String entityType;
 	private String animalTamer;
 	private Boolean PvP;
 	private UUID entityUUID;
 	private UUID owner;
 
 	public Pet()
 	{}
 
 	public Pet(UUID id, ConfigurationSection conf)
 	{
 		this.id = id;
 		entityType = conf.getString("entityType");
 		if(conf.getString("animalTamer") != null) animalTamer = conf.getString("animalTamer");
 		PvP = conf.getBoolean("PvP");
 		entityUUID = UUID.fromString(conf.getString("entityUUID"));
 		if(conf.getString("owner") != null) owner = UUID.fromString(conf.getString("owner"));
 	}
 
 	@Override
 	public Map<String, Object> serialize()
 	{
 		return new HashMap<String, Object>()
 		{
 			{
 				put("entityType", entityType);
 				if(animalTamer != null) put("animalTamer", animalTamer);
 				put("PvP", PvP);
 				put("entityUUID", entityUUID.toString());
 				if(owner != null) put("owner", owner.toString());
 			}
 		};
 	}
 
 	public void generateId()
 	{
 		id = UUID.randomUUID();
 	}
 
 	public void remove()
 	{
 		getEntity().remove();
 		delete();
 	}
 
 	public void delete()
 	{
 		DataManager.pets.remove(getId());
 	}
 
 	public void setTamable(LivingEntity tameable)
 	{
 		if(!(tameable instanceof Tameable)) throw new IllegalArgumentException("LivingEntity not tamable.");
 		this.entityType = tameable.getType().getName();
 		this.entityUUID = tameable.getUniqueId();
 	}
 
 	public void setOwner(DCharacter owner)
 	{
 		this.animalTamer = owner.getName();
 		this.owner = owner.getId();
 		Util.save(this);
 	}
 
 	public void setCanPvp(boolean PvP)
 	{
 		this.PvP = PvP;
 		Util.save(this);
 	}
 
 	public Boolean canPvp()
 	{
 		return this.PvP;
 	}
 
 	public String getEntityType()
 	{
 		return entityType;
 	}
 
 	public String getAnimalTamer()
 	{
 		return animalTamer;
 	}
 
 	public UUID getEntityUUID()
 	{
 		return entityUUID;
 	}
 
 	public LivingEntity getEntity()
 	{
 		for(World world : Bukkit.getServer().getWorlds())
 		{
 			for(Entity pet : world.getLivingEntities())
 			{
 				if(!(pet instanceof Tameable)) continue;
 				if(pet.getUniqueId().equals(this.entityUUID)) return (LivingEntity) pet;
 			}
 		}
		Errors.triggerError("Demigods", ChatColor.RED + "Could not find pet.");
 		return null;
 	}
 
 	public DCharacter getOwner()
 	{
 		DCharacter owner = DCharacter.Util.load(this.owner);
 		if(owner == null)
 		{
 			disownPet();
 			delete();
 			return null;
 		}
 		else if(!owner.isUsable()) return null;
 		return owner;
 	}
 
 	public Deity getDeity()
 	{
 		if(getOwner() == null)
 		{
 			disownPet();
 			delete();
 			return null;
 		}
 		else if(!getOwner().isUsable()) return null;
 		return getOwner().getDeity();
 	}
 
 	@Override
 	public UUID getId()
 	{
 		return this.id;
 	}
 
 	@Override
 	public Location getCurrentLocation()
 	{
 		try
 		{
 			return getEntity().getLocation();
 		}
 		catch(Exception ignored)
 		{}
 		return null;
 	}
 
 	@Override
 	public DCharacter getRelatedCharacter()
 	{
 		return getOwner();
 	}
 
 	public void disownPet()
 	{
 		if(this.getEntity() == null) return;
 		((Tameable) this.getEntity()).setOwner(new AnimalTamer()
 		{
 			@Override
 			public String getName()
 			{
 				return "Disowned";
 			}
 		});
 	}
 
 	public static class File extends ConfigFile
 	{
 		private static String SAVE_PATH;
 		private static final String SAVE_FILE = "pets.yml";
 
 		public File()
 		{
 			super(Demigods.plugin);
 			SAVE_PATH = Demigods.plugin.getDataFolder() + "/data/";
 		}
 
 		@Override
 		public ConcurrentHashMap<UUID, Pet> loadFromFile()
 		{
 			final FileConfiguration data = getData(SAVE_PATH, SAVE_FILE);
 			return new ConcurrentHashMap<UUID, Pet>()
 			{
 				{
 					for(String stringId : data.getKeys(false))
 						put(UUID.fromString(stringId), new Pet(UUID.fromString(stringId), data.getConfigurationSection(stringId)));
 				}
 			};
 		}
 
 		@Override
 		public boolean saveToFile()
 		{
 			FileConfiguration saveFile = getData(SAVE_PATH, SAVE_FILE);
 			Map<UUID, Pet> currentFile = loadFromFile();
 
 			for(UUID id : DataManager.pets.keySet())
 				if(!currentFile.keySet().contains(id) || !currentFile.get(id).equals(DataManager.pets.get(id))) saveFile.createSection(id.toString(), Util.load(id).serialize());
 
 			for(UUID id : currentFile.keySet())
 				if(!DataManager.pets.keySet().contains(id)) saveFile.set(id.toString(), null);
 
 			return saveFile(SAVE_PATH, SAVE_FILE, saveFile);
 		}
 	}
 
 	public static class Util
 	{
 		public static Pet load(UUID id)
 		{
 			return DataManager.pets.get(id);
 		}
 
 		public static void save(Pet pet)
 		{
 			DataManager.pets.put(pet.getId(), pet);
 		}
 
 		public static Pet create(LivingEntity tameable, DCharacter owner)
 		{
 			if(owner == null) throw new IllegalArgumentException("Owner cannot be null.");
 			if(!(tameable instanceof Tameable)) throw new IllegalArgumentException("LivingEntity not tamable.");
 			Pet wrapper = new Pet();
 			wrapper.generateId();
 			wrapper.setTamable(tameable);
 			wrapper.setOwner(owner);
 			save(wrapper);
 			return wrapper;
 		}
 
 		public static Set<Pet> findByType(final EntityType type)
 		{
 			return Sets.newHashSet(Collections2.filter(DataManager.pets.values(), new Predicate<Pet>()
 			{
 				@Override
 				public boolean apply(@Nullable Pet pet)
 				{
 					return pet.getEntityType().equals(type.getName());
 				}
 			}));
 		}
 
 		public static Set<Pet> findByTamer(final String animalTamer)
 		{
 			return Sets.newHashSet(Collections2.filter(DataManager.pets.values(), new Predicate<Pet>()
 			{
 				@Override
 				public boolean apply(@Nullable Pet pet)
 				{
 					return pet.getAnimalTamer().equals(animalTamer);
 				}
 			}));
 		}
 
 		public static List<Pet> findByUUID(final UUID uniqueId)
 		{
 			return Lists.newArrayList(Collections2.filter(DataManager.pets.values(), new Predicate<Pet>()
 			{
 				@Override
 				public boolean apply(@Nullable Pet pet)
 				{
 					return pet.getEntityUUID().equals(uniqueId);
 				}
 			}));
 		}
 
 		public static Pet getTameable(LivingEntity tameable)
 		{
 			if(!(tameable instanceof Tameable)) throw new IllegalArgumentException("LivingEntity not tamable.");
 			try
 			{
 				return findByUUID(tameable.getUniqueId()).get(0);
 			}
 			catch(Exception ignored)
 			{}
 			return null;
 		}
 
 		public static void disownPets(String animalTamer)
 		{
 			for(Pet wrapper : findByTamer(animalTamer))
 			{
 				if(wrapper.getEntity() == null) continue;
 				((Tameable) wrapper.getEntity()).setOwner(new AnimalTamer()
 				{
 					@Override
 					public String getName()
 					{
 						return "Disowned";
 					}
 				});
 			}
 		}
 
 		public static void reownPets(AnimalTamer tamer, DCharacter character)
 		{
 			for(Pet wrapper : findByTamer(character.getName()))
 			{
 				((Tameable) wrapper.getEntity()).setOwner(tamer);
 			}
 		}
 	}
 }
