 package com.evervoid.state.player;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Set;
 
 import com.evervoid.json.Json;
 import com.evervoid.json.Jsonable;
 import com.evervoid.state.EVGameState;
 import com.evervoid.state.data.GameData;
 import com.evervoid.state.data.RaceData;
 
 public class ResourceAmount implements Jsonable
 {
 	public static String getFormattedAmount(final double amount)
 	{
 		return String.valueOf((int) amount);
 	}
 
 	private final Map<String, Double> aResourceMap = new HashMap<String, Double>();
 
 	/**
 	 * Private argument-less constructor; used for cloning. Use emptyClone to get an empty clone.
 	 */
 	private ResourceAmount()
 	{
 	}
 
 	public ResourceAmount(final GameData data, final RaceData race)
 	{
 		final ResourceAmount initial = race.getStartResources();
 		for (final String resource : data.getResources()) {
 			aResourceMap.put(resource, initial.getValue(resource));
 		}
 	}
 
 	public ResourceAmount(final Json j)
 	{
 		for (final String resource : j.getAttributes()) {
 			aResourceMap.put(resource, j.getDoubleAttribute(resource));
 		}
 	}
 
 	/**
 	 * Adds the specified ResourceAmount to this one and returns the result
 	 * 
 	 * @param other
 	 *            The ResourceAmount to add
 	 * @return The sum of this ResourceAmount and the one passed as argument
 	 */
 	public ResourceAmount add(final ResourceAmount other)
 	{
 		if (!isCompatibleWith(other)) {
 			return null;
 		}
 		final ResourceAmount added = clone();
 		for (final String resName : other.getNames()) {
 			added.aResourceMap.put(resName, Math.max(0, aResourceMap.get(resName) + other.getValue(resName)));
 		}
 		return added;
 	}
 
 	@Override
 	public ResourceAmount clone()
 	{
 		final ResourceAmount newObj = new ResourceAmount();
 		for (final String key : aResourceMap.keySet()) {
 			newObj.aResourceMap.put(key, aResourceMap.get(key));
 		}
 		return newObj;
 	}
 
 	/**
 	 * @param cost
 	 *            The cost to pay
 	 * @return Whether this ResourceAmount holds enough resources to pay the specified cost
 	 */
 	public boolean contains(final ResourceAmount cost)
 	{
 		if (!isCompatibleWith(cost)) {
 			return false;
 		}
 		for (final String resName : cost.getNames()) {
 			if (getValue(resName) < cost.getValue(resName)) {
 				return false;
 			}
 		}
 		return true;
 	}
 
 	/**
 	 * Divides the current ResourceAmount and returns the result
 	 * 
 	 * @param factor
 	 *            The factor to divide by
 	 * @return The result of the division of this ResourceAmount
 	 */
 	public ResourceAmount divide(final double factor)
 	{
 		final ResourceAmount product = clone();
 		for (final String resName : getNames()) {
 			product.aResourceMap.put(resName, Math.max(0, getValue(resName) / factor));
 		}
 		return product;
 	}
 
 	public ResourceAmount emptyClone()
 	{
 		final ResourceAmount clone = new ResourceAmount();
 		for (final String resource : aResourceMap.keySet()) {
 			clone.aResourceMap.put(resource, 0d);
 		}
 		return clone;
 	}
 
 	public String getFormattedValue(final String resourceName)
 	{
 		return getFormattedAmount(getValue(resourceName));
 	}
 
 	public Set<String> getNames()
 	{
 		return aResourceMap.keySet();
 	}
 
 	public double getValue(final String resourceName)
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
 
 	/**
 	 * Checks whether all keys in the provided ResourceAmount instance are contained in this one. Not transitive!
 	 * 
 	 * @param other
 	 *            The other ResourceAmount
 	 * @return Whether it matches up or not
 	 */
 	public boolean isCompatibleWith(final ResourceAmount other)
 	{
 		if (other == null) {
 			return false;
 		}
 		for (final String resName : other.getNames()) {
 			if (!hasResource(resName)) {
 				return false;
 			}
 		}
 		return true;
 	}
 
 	/**
 	 * @return Whether this ResourceAmount represents no resources at all or not
 	 */
 	public boolean isZero()
 	{
 		for (final double val : aResourceMap.values()) {
 			if (val != 0d) {
 				return false;
 			}
 		}
 		return true;
 	}
 
 	/**
 	 * Multiplies the specified ResourceAmount to this one and returns the result
 	 * 
 	 * @param other
 	 *            The ResourceAmount to multiply
 	 * @return The product of this ResourceAmount and the one passed as argument
 	 */
 	public ResourceAmount mult(final ResourceAmount other)
 	{
 		if (!isCompatibleWith(other)) {
 			return null;
 		}
 		final ResourceAmount product = clone();
 		for (final String resName : other.getNames()) {
 			product.aResourceMap.put(resName, Math.max(0, aResourceMap.get(resName) * other.getValue(resName)));
 		}
 		return product;
 	}
 
 	/**
 	 * Negates the current ResourceAmount and returns the result
 	 * 
 	 * @return The negation of this ResourceAmount
 	 */
 	public ResourceAmount negate()
 	{
 		final ResourceAmount newObj = clone();
 		for (final String resource : aResourceMap.keySet()) {
 			newObj.aResourceMap.put(resource, -aResourceMap.get(resource));
 		}
 		return newObj;
 	}
 
 	/**
 	 * Fills in all unset resources to 0 given a game state. Does modify this object, but does not change the effective amount
 	 * of resources that this object represents.
 	 * 
 	 * @param state
 	 *            The state to pull resource types from
 	 * @return this
 	 */
 	public ResourceAmount populateWith(final EVGameState state)
 	{
		final ResourceAmount copy = clone();
 		for (final String resName : state.getResourceNames()) {
			if (!copy.hasResource(resName)) {
				copy.aResourceMap.put(resName, 0d);
 			}
 		}
		return copy;
 	}
 
 	public void remove(final ResourceAmount amount)
 	{
 		add(amount.negate());
 	}
 
 	/**
 	 * Subtracts the specified ResourceAmount to this one and returns the result
 	 * 
 	 * @param other
 	 *            The ResourceAmount to subtract
 	 * @return The difference of this ResourceAmount and the one passed as argument
 	 */
 	public ResourceAmount subtract(final ResourceAmount amount)
 	{
 		return add(amount.negate());
 	}
 
 	@Override
 	public Json toJson()
 	{
 		final Json map = new Json();
 		for (final String resource : aResourceMap.keySet()) {
 			map.setDoubleAttribute(resource, aResourceMap.get(resource));
 		}
 		return map;
 	}
 
 	@Override
 	public String toString()
 	{
 		return toJson().toPrettyString();
 	}
 }
