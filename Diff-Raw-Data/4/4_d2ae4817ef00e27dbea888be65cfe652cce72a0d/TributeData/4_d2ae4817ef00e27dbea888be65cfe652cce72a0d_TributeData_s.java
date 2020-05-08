 package com.censoredsoftware.demigods.engine.data;
 
 import com.google.common.base.Objects;
 import com.google.common.base.Predicate;
 import com.google.common.collect.Collections2;
 import com.google.common.collect.Sets;
 import org.bukkit.Material;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.configuration.serialization.ConfigurationSerializable;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Set;
 import java.util.UUID;
 
 public class TributeData implements ConfigurationSerializable
 {
 	private UUID id;
 	private String category;
 	private Material material;
 	private int amount;
 
 	public TributeData()
 	{
 		id = UUID.randomUUID();
 	}
 
 	public TributeData(UUID id, ConfigurationSection conf)
 	{
 		this.id = id;
 		category = conf.getString("category");
		material = Material.getMaterial(conf.getString("material"));
		amount = Integer.parseInt(conf.getString("amount"));
 	}
 
 	@Override
 	public Map<String, Object> serialize()
 	{
 		Map<String, Object> map = new HashMap<String, Object>();
 		map.put("category", category);
 		map.put("material", material.name());
 		map.put("amount", amount);
 		return map;
 	}
 
 	public UUID getId()
 	{
 		return this.id;
 	}
 
 	public void setCategory(String category)
 	{
 		this.category = category;
 	}
 
 	public void setMaterial(Material material)
 	{
 		this.material = material;
 	}
 
 	public void setAmount(int amount)
 	{
 		this.amount = amount;
 	}
 
 	public String getCategory()
 	{
 		return this.category;
 	}
 
 	public Material getMaterial()
 	{
 		return material;
 	}
 
 	public int getAmount()
 	{
 		return amount;
 	}
 
 	public void delete()
 	{
 		DataManager.timedData.remove(id);
 	}
 
 	@Override
 	public int hashCode()
 	{
 		return Objects.hashCode(this.id, this.category, this.material, this.amount);
 	}
 
 	@Override
 	public Object clone() throws CloneNotSupportedException
 	{
 		throw new CloneNotSupportedException();
 	}
 
 	public static class Util
 	{
 		public static void save(String category, Material material, int amount)
 		{
 			// Remove the data if it exists already
 			remove(category, material);
 
 			// Create and save the timed data
 			TributeData tributeData = new TributeData();
 			tributeData.setCategory(category);
 			tributeData.setMaterial(material);
 			tributeData.setAmount(amount);
 
 			// Put it in the map
 			DataManager.tributeData.put(tributeData.getId(), tributeData);
 		}
 
 		public static void remove(String category, Material material)
 		{
 			if(find(category, material) != null) find(category, material).delete();
 		}
 
 		public static TributeData get(UUID id)
 		{
 			return DataManager.tributeData.get(id);
 		}
 
 		public static Set<TributeData> getAll()
 		{
 			return Sets.newHashSet(DataManager.tributeData.values());
 		}
 
 		public static TributeData find(String category, Material material)
 		{
 			if(findByCategory(category) == null) return null;
 
 			for(TributeData data : findByCategory(category))
 				if(data.getMaterial().name().equalsIgnoreCase(material.name())) return data;
 
 			return null;
 		}
 
 		public static Set<TributeData> findByCategory(final String category)
 		{
 			return Sets.newHashSet(Collections2.filter(getAll(), new Predicate<TributeData>()
 			{
 				@Override
 				public boolean apply(TributeData tributeData)
 				{
 					return tributeData.getCategory().equalsIgnoreCase(category);
 				}
 			}));
 		}
 	}
 }
