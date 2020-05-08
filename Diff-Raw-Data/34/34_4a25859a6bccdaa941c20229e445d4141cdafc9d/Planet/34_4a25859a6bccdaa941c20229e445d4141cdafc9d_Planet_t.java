 package com.evervoid.state.prop;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import com.evervoid.json.Json;
 import com.evervoid.state.EVGameState;
 import com.evervoid.state.building.Building;
 import com.evervoid.state.data.BuildingData;
 import com.evervoid.state.data.PlanetData;
 import com.evervoid.state.data.SpriteData;
 import com.evervoid.state.geometry.GridLocation;
 import com.evervoid.state.observers.PlanetObserver;
 import com.evervoid.state.player.Player;
 import com.evervoid.state.player.ResourceAmount;
 import com.evervoid.utils.MathUtils;
 
 public class Planet extends Prop
 {
 	private final Map<Integer, Building> aBuildings = new HashMap<Integer, Building>();
 	private int aCurrentHealth;
 	private int aCurrentShields;
 	private final PlanetData aData;
 	private final Set<PlanetObserver> aObserverSet;
 
 	public Planet(final int id, final Player player, final GridLocation location, final String type, final EVGameState state)
 	{
 		super(id, player, location, "planet", state);
 		aData = state.getPlanetData(type);
 		aLocation.dimension = aData.getDimension();
 		aObserverSet = new HashSet<PlanetObserver>();
 		// Initialize map to all-null buildings
 		for (int b = 0; b < aData.getNumOfBuildingSlots(); b++) {
 			aBuildings.put(b, null);
 		}
 		aCurrentHealth = aData.getBaseHealth();
 	}
 
 	public Planet(final Json j, final PlanetData data, final EVGameState state)
 	{
 		super(j, "planet", state);
 		aData = data;
 		aObserverSet = new HashSet<PlanetObserver>();
 		aState.registerProp(this, aContainer);
 		aCurrentHealth = j.getIntAttribute("health");
 		final Json buildingsJson = j.getAttribute("buildings");
 		String slot;
 		for (int b = 0; b < aData.getNumOfBuildingSlots(); b++) {
 			slot = String.valueOf(b);
 			if (buildingsJson.hasAttribute(slot) && !buildingsJson.getAttribute(slot).isNull()) {
 				aBuildings.put(b, new Building(buildingsJson.getAttribute(slot), state));
 			}
 			else {
 				aBuildings.put(b, null);
 			}
 		}
 	}
 
 	public void addBuilding(final int slot, final Building building)
 	{
 		aBuildings.put(slot, building);
 	}
 
 	public void addHealth(final int amount)
 	{
 		aCurrentHealth = MathUtils.clampInt(0, aCurrentHealth + amount, getMaxHealth());
 		for (final PlanetObserver obs : aObserverSet) {
 			obs.healthChanged(this);
 		}
 		if (aCurrentHealth == 0) {
 			// planet destroyed, make it neutral
 			changeOwner(aState.getNullPlayer());
 		}
 	}
 
 	public void addShields(final int amount)
 	{
 		aCurrentShields = MathUtils.clampInt(0, aCurrentShields + amount, getMaxShields());
 		for (final PlanetObserver obs : aObserverSet) {
 			obs.shieldsChanged(this);
 		}
 	}
 
 	public void changeOwner(final Player player)
 	{
 		if (player == null || player.equals(aPlayer)) {
 			return; // No change
 		}
 		// change the player
 		aPlayer = player;
 		// Reset buildings
 		deleteBuildings();
 		// warn all observers
 		for (final PlanetObserver observer : aObserverSet) {
 			observer.planetCaptured(this, aPlayer);
 		}
 	}
 
 	public void deleteBuilding(final int slot)
 	{
 		if (!hasSlot(slot)) {
 			return;
 		}
 		final Building toDelete = getBuildingAt(slot);
 		if (toDelete != null) {
 			toDelete.deregister();
 		}
 		aBuildings.put(slot, null);
 	}
 
 	public void deleteBuildings()
 	{
 		// This will initialize the map to all-null buildings
 		for (int b = 0; b < aData.getNumOfBuildingSlots(); b++) {
 			deleteBuilding(b);
 		}
 	}
 
 	public void deregisterObserver(final PlanetObserver pObserver)
 	{
 		aObserverSet.remove(pObserver);
 	}
 
 	public Building getBuildingAt(final int slot)
 	{
 		if (!hasSlot(slot)) {
 			return null;
 		}
 		return aBuildings.get(slot);
 	}
 
 	public Integer getBuildingProgress(final int slot)
 	{
 		if (!hasSlot(slot)) {
 			return null;
 		}
 		return aBuildings.get(slot).getBuildingProgress();
 	}
 
 	public Map<Integer, Building> getBuildings()
 	{
 		return aBuildings;
 	}
 
 	public int getCurrentHealth()
 	{
 		return aCurrentHealth;
 	}
 
 	public int getCurrentShields()
 	{
 		return aCurrentShields;
 	}
 
 	public float getCurrentShieldsFloat()
 	{
		if (getMaxShields() <= 0) {
			return 0; // Prevent silent division by 0
		}
 		return (float) aCurrentShields / (float) getMaxShields();
 	}
 
 	public PlanetData getData()
 	{
 		return aData;
 	}
 
 	public int getHealthRegenRate()
 	{
 		if (aPlayer.isNullPlayer()) {
 			return 0;
 		}
 		return aData.getHealthRegenRate(aPlayer.getResearch());
 	}
 
 	public int getMaxHealth()
 	{
 		// TODO deal with research
 		return aData.getBaseHealth();
 	}
 
 	public int getMaxShields()
 	{
 		int maxShields = 0;
 		for (final Building b : aBuildings.values()) {
 			if (b != null && b.isBuildingComplete()) {
 				maxShields += b.getExtraShields();
 			}
 		}
 		return maxShields;
 	}
 
 	public String getPlanetType()
 	{
 		return aData.getTitle();
 	}
 
 	public ResourceAmount getResourceRate()
 	{
 		ResourceAmount income = aData.getResourceRate().populateWith(getState());
 		for (final Building b : aBuildings.values()) {
 			if (b != null && b.isBuildingComplete()) {
 				final ResourceAmount bIncome = b.getIncome();
 				if (bIncome != null) {
 					income = income.add(bIncome);
 				}
 			}
 		}
 		return income;
 	}
 
 	public int getShieldRegenRate()
 	{
 		if (aPlayer.isNullPlayer()) {
 			return 0;
 		}
 		int shieldsRegen = 0;
 		for (final Building b : aBuildings.values()) {
 			if (b != null && b.isBuildingComplete()) {
 				shieldsRegen += b.getShieldRegen();
 			}
 		}
 		return shieldsRegen;
 	}
 
 	public SpriteData getShieldSprite()
 	{
 		return getPlayer().getRaceData().getShieldSprite(getPlayer().getResearch(), getDimension());
 	}
 
 	public Integer getSlotForBuilding(final Building building)
 	{
 		if (building == null) {
 			return null;
 		}
 		for (int slot = 0; slot < aData.getNumOfBuildingSlots(); slot++) {
 			if (building.equals(getBuildingAt(slot))) {
 				return slot;
 			}
 		}
 		return null;
 	}
 
 	public boolean hasSlot(final int slot)
 	{
 		return slot >= 0 && slot < aData.getNumOfBuildingSlots();
 	}
 
 	/**
 	 * Starts or increments a building construction
 	 * 
 	 * @param slot
 	 *            The building slot
 	 * @param building
 	 *            The type of building to build
 	 */
 	public void incrementBuilding(final int slot, final BuildingData building)
 	{
 		if (!hasSlot(slot) || (isBuildingComplete(slot) && getBuildingAt(slot).getData().equals(building))) {
 			// Invalid slot or building already in place
 			return;
 		}
 		Building b = getBuildingAt(slot);
 		if (b == null || !b.getData().equals(building)) {
 			// There was no building in construction there, or there was another type of building in construction
 			if (b != null) {
 				b.deregister();
 			}
 			b = new Building(getState(), this, building, false);
 			addBuilding(slot, b);
 		}
 		b.incrementBuildingProgress();
 		for (final PlanetObserver obs : aObserverSet) {
 			obs.buildingsChanged(this);
 		}
 	}
 
 	public boolean isAtMaxHealth()
 	{
 		return aCurrentHealth == getMaxHealth();
 	}
 
 	public boolean isAtMaxShields()
 	{
 		return aCurrentShields == getMaxShields();
 	}
 
 	public boolean isBuildingComplete(final int slot)
 	{
 		return getBuildingAt(slot) != null && getBuildingAt(slot).isBuildingComplete();
 	}
 
 	public boolean isSlotFree(final int slot)
 	{
 		return hasSlot(slot) && aBuildings.get(slot) == null;
 	}
 
 	/**
 	 * Populates this planet's buildings with the player race's initial buildings. Only called when generating a random game
 	 * state, not when capturing.
 	 */
 	public void populateInitialBuildings()
 	{
 		final List<BuildingData> buildings = aPlayer.getRaceData().getInitialBuildingData();
 		int slot = 0;
 		for (final BuildingData data : buildings) {
 			if (!hasSlot(slot)) {
 				break;
 			}
 			addBuilding(slot, new Building(getState(), this, data, true));
 			slot++;
 		}
 	}
 
 	public void registerObserver(final PlanetObserver pObserver)
 	{
 		aObserverSet.add(pObserver);
 	}
 
 	public void removeHealth(final int amount)
 	{
 		addHealth(-amount);
 	}
 
 	private void removeShields(final int amount)
 	{
 		addShields(-amount);
 	}
 
 	public void takeDamange(final int damage)
 	{
 		removeHealth(Math.max(0, damage - aCurrentShields));
 		removeShields(Math.min(aCurrentShields, damage));
 	}
 
 	@Override
 	public Json toJson()
 	{
 		final Json j = super.toJson();
 		j.setStringAttribute("planettype", aData.getType());
 		j.setIntMapAttribute("buildings", aBuildings);
 		j.setIntAttribute("health", aCurrentHealth);
 		return j;
 	}
 }
