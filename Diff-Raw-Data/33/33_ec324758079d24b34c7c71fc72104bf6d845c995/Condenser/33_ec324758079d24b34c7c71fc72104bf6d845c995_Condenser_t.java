 package simulator;
 
class Condenser extends PlantComponent {
 	private static final long serialVersionUID = 4348915919668272156L;

 	private final static int DEFAULT_TEMPERATURE = 50;
 	private final static int DEFAULT_PRESSURE = 0;
 	private final static int DEFAULT_WATER_VOLUME = 2000;
 	private final static int DEFAULT_STEAM_VOLUME = 0;
 	
 	private final static int MAX_TEMPERATURE = 2000;
 	private final static int MAX_PRESSURE = 2000;
 	private final static int MAX_HEALTH = 100;
 	private final static int HEALTH_CHANGE_WHEN_DAMAGING = 10;
 	private final static int COOLANT_TEMP = 20; // temperature of the coolant coming in
 	private final static int MAX_COOLDOWN_PER_STEP = 250; // Maximum amount to cool the condenser per step. 
 	private final static int WATER_STEAM_RATIO = 2; // water to steam ratio.
 	private final static double COND_MULTIPLIER = 0.2; // temperature to steam condensed multiplier.
 	private final static double VOL_TO_PRESSURE_MULTIPLIER = 0.15;
 	
 	private int temperature;
 	private int pressure;
 	private int health;
 	private int waterVolume;
 	private int steamVolume;
 	private int steamIn;
 	private Pump coolantPump;
	
	
 	/**
 	 * Constructor for Condenser.
 	 * 
 	 * The created Condenser never fails randomly and is pressured.
 	 */
	public Condenser(Pump coolantPump) {
 		super(0,0,true,true); // Never randomly fails, is operational and is pressurised. 
 		this.health = MAX_HEALTH;
 		this.temperature = DEFAULT_TEMPERATURE;
 		this.pressure = DEFAULT_PRESSURE;
 		this.waterVolume = DEFAULT_WATER_VOLUME;
 		this.steamVolume = DEFAULT_STEAM_VOLUME;
 		this.coolantPump = coolantPump;
 	}
 
 	// ----------- Getters & Setters ---------------
 	
 	/**
 	 * Return  the temperature of the condenser.
 	 * @return the temperature of the condenser.
 	 */
 	public int getTemperature() {
 		return temperature;
 	}
 	
 	/**
 	 * Return  the max temperature of the condenser.
 	 * @return the max temperature of the condenser.
 	 */
 	public int getMaxTemperature() {
 		return MAX_TEMPERATURE;
 	}
 	
 	/**
 	 * Return  the pressure of the condenser.
 	 * @return the pressure of the condenser.
 	 */
 	public int getPressure() {
 		return pressure;
 	}
 	
 	/**
 	 * Return  the max pressure of the condenser.
 	 * @return the max pressure of the condenser.
 	 */
 	public int getMaxPressure() {
 		return MAX_PRESSURE;
 	}
 	
 	/**
 	 * Return  the current water volume inside the condenser.
 	 * @return the current water volume inside the condenser.
 	 */
 	public int getWaterVolume() {
 		return waterVolume;
 	}
 	
 	/**
 	 * Updates the amount of water in the condenser.
 	 * 
 	 * @param amount amount of water to add to the total in the condenser.
 	 */
 	public void updateWaterVolume(int amount) {
 		this.waterVolume += amount;
 	}
 	
 	/**
 	 * Return  the current steam volume inside the condenser.
 	 * @return the current steam volume inside the condenser.
 	 */
 	public int getSteamVolume()
 	{
 		return steamVolume;
 	}
 
 	/**
 	 * Updates the amount of steam in the condenser.
 	 * Also stores the amount of steam into the condenser for this step and 
 	 * as such should not be called more than once per step.
 	 * 
 	 * amount can be negative and will be when steam is condensed into water.
 	 *  
 	 * @param amount the amount of steam to add to the volume.
 	 */
 	public void updateSteamVolume(int amount)
 	{
 		this.steamIn = amount;
 		this.steamVolume += amount;
 	}
 
 	/**
 	 * Return  the current health of the condenser.
 	 * @return the current health of the condenser.
 	 */
 	public int getHealth() {
 		return health;
	}
 
 	public Pump getCoolantPump() {
 		return coolantPump;
	}
 	
 	/**
 	 * Updates the state of the condenser.
 	 * 
 	 * Updates the temperature, condense some steam to water,
 	 * updates the pressure and damage the condenser if it has to.
 	 */

 	public void updateState() {
 		updateTemperature();
 		condenseSteam();
 		updatePressure();
 		checkIfDamaging();
 	}
 	
 	
 	/**
 	 * Checks if the condenser fails.
 	 * 
 	 * If the health is below 0 and the method returns true,
 	 * the PlantController will detect a game over state.
 	 * 
 	 * @return true if health is below 0
 	 */
 	@Override
 	public boolean checkFailure() {
 		if (health <= 0)
 			return true;
 		else
 			return false;
 	}
 
 	/**
 	 * Updates the temperature.
 	 * 
 	 * Calculates a new value for the temperature based on
 	 * heating because of steam flowing in the condenser and
 	 * cooling.
 	 */
 	private void updateTemperature() {
 		int changeInTemp;
 		Flow flowIn = this.getInput().getFlowOut();
 		int steamTemperature = flowIn.getTemperature();
 		
 		changeInTemp = heating(steamTemperature, this.steamIn) - cooldown();
 		this.temperature += changeInTemp;
 	}
 	
 	/**
 	 * Updates the pressure.
 	 * 
 	 * Calculates a new value for the pressure.
 	 */
 	private void updatePressure() {
 		int currentPressure;
 		currentPressure = (int) Math.round(new Double(this.steamVolume) * VOL_TO_PRESSURE_MULTIPLIER);
 		this.pressure = currentPressure;
 	}
 	
 	/**
 	 * Calculates the increase in temperature based upon the temperature and volume
 	 * of steam coming into the condenser.
 	 * 
 	 * @param steamTemperature temperature of steam coming into the condenser
 	 * @param steamVolumeIn amount of steam that has come into the condenser in the last step.
 	 * @return amount of temperature increase for this step.
 	 */
 	private int heating(int steamTemperature, int steamVolumeIn) {
 		int tempDiff = steamTemperature - this.temperature;
 		if (this.steamVolume < 1) return 0; // stops a potential divide by 0.
 		if (steamVolumeIn == 0) return 0; // No steam flowing in => no heating.
 		return tempDiff * (1 - ((this.steamVolume - steamVolumeIn)/this.steamVolume));
 	}
 	
 	/**
 	 * Returns COOLDOWN_PER_STEP constant as the pump pumping coolant
 	 * into the condenser is always on full.
 	 * Will obviously not try to cool the condenser past the temperature of 
 	 * the coolant.
 	 * 
 	 * @return amount of temperature decrease for this step.
 	 */
 	private int cooldown() {
 		int cooldownAmount = cooldownPerStep();
 		int potentialNewTemp = this.temperature - cooldownAmount;
 		if (potentialNewTemp > COOLANT_TEMP) {
 			return cooldownAmount;
 		} else {
 			return this.temperature - COOLANT_TEMP;
 		}
 	}
 	
 	private int cooldownPerStep() {
 		int maxRpm = coolantPump.getMaxRpm();
 		int currRpm = coolantPump.getRpm();
 		return (int) Math.round(MAX_COOLDOWN_PER_STEP * new Double(currRpm)/maxRpm);
 	}
 
 	/**
 	 * Not very physics accurate, but it provides a reasonable model of 
 	 * the behaviour of steam condensing.
 	 */
 	private void condenseSteam() {
 		int steamCondensed;
 		int waterCreated;
 		if (this.temperature < MAX_TEMPERATURE) {
 			steamCondensed = (int) Math.ceil((MAX_TEMPERATURE - this.temperature) * COND_MULTIPLIER);
 		} else {
 			steamCondensed = 0;
 		}
 		
 		if (steamCondensed > this.steamVolume) steamCondensed = this.steamVolume;
 		waterCreated = (int) Math.ceil(steamCondensed * (1 / new Double(WATER_STEAM_RATIO)));
 		/*
 		 * Since we do a dodgy division above, to make sure we aren't losing / creating
 		 * water we remultiply out the waterCreated.
 		 */
 		steamCondensed = waterCreated * WATER_STEAM_RATIO;	
 		this.steamVolume -= steamCondensed; // made negative as the water is removed.
 		this.waterVolume += waterCreated;
 	}
 
 	/**
 	 * Damages the condenser if appropriate.
 	 * 
 	 * Damages the condenser if the current temperature is above
 	 * the max temperature and if the current pressure is
 	 * above the max pressure.
 	 */
 	private void checkIfDamaging() {
 		if(this.temperature >= MAX_TEMPERATURE) {
 			damageCondenser();
 		}
 		if(this.pressure >= MAX_PRESSURE) {
 			damageCondenser();
 		}
 	}
 	
 	/**
 	 * Damages the condenser.
 	 * 
 	 * Lowers the health by HEALTH_CHANGE_WHEN_DAMAGING.
 	 */
 	private void damageCondenser() {
 		health -= HEALTH_CHANGE_WHEN_DAMAGING;
 	}
 }
