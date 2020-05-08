 package com.evervoid.state.player;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Set;
 
 import com.evervoid.json.Json;
 import com.evervoid.json.Jsonable;
 import com.evervoid.state.data.GameData;
 import com.evervoid.state.data.PlanetData;
 import com.evervoid.state.data.RaceData;
 
 public class ResourceAmount implements Jsonable
 {
 	private final Map<String, Integer> aResourceMap;
 
 	public ResourceAmount(final GameData data, final RaceData race)
 	{
 		aResourceMap = new HashMap<String, Integer>();
 		final ResourceAmount initial = race.getStartResources();
 		for (final String resource : data.getResources()) {
 			aResourceMap.put(resource, initial.getValue(resource));
 		}
 	}
 
 	public ResourceAmount(final Json j)
 	{
 		aResourceMap = new HashMap<String, Integer>();
 		for (final String resource : j.getAttributes()) {
 			aResourceMap.put(resource, j.getIntAttribute(resource));
 		}
 	}
 
 	public ResourceAmount(final PlanetData data)
 	{
 		aResourceMap = new HashMap<String, Integer>();
 	}
 
 	public boolean add(final ResourceAmount other)
 	{
 		if (!canAdd(other)) {
 			return false;
 		}
 		for (final String resName : other.getNames()) {
 			add(resName, other.getValue(resName));
 		}
 		return true;
 	}
 
 	public boolean add(final String resource, final int amount)
 	{
 		if (!aResourceMap.containsKey(resource)) {
 			return false;
 		}
 		aResourceMap.put(resource, Math.max(0, aResourceMap.get(resource) + amount));
 		return true;
 	}
 
 	/**
 	 * Checks whether all keys in the provided ResourceAmount instance are contained in this one. Not transitive!
 	 * 
 	 * @param other
 	 *            The other ResourceAmount
 	 * @return Whether it matches up or not
 	 */
 	public boolean canAdd(final ResourceAmount other)
 	{
 		for (final String resName : other.getNames()) {
 			if (!hasResource(resName)) {
 				return false;
 			}
 		}
 		return true;
 	}
 
 	@Override
 	public ResourceAmount clone()
 	{
 		return new ResourceAmount(toJson());
 	}
 
 	public Set<String> getNames()
 	{
 		return aResourceMap.keySet();
 	}
 
 	public int getValue(final String resourceName)
 	{
 		if (!hasResource(resourceName)) {
 			return 0;
 		}
 		return aResourceMap.get(resourceName);
 	}
 
 	public boolean hasResource(final String resource)
 	{
 		return aResourceMap.containsKey(resource);
 	}
 
 	public boolean remove(final String resource, final int amount)
 	{
 		return add(resource, -amount);
 	}
 
 	@Override
 	public Json toJson()
 	{
 		final Json map = new Json();
 		for (final String resource : aResourceMap.keySet()) {
 			map.setIntAttribute(resource, aResourceMap.get(resource));
 		}
		return map;
 	}
 }
