 package kimononet.geo;
 
 import kimononet.time.SystemTimeProvider;
 import kimononet.time.TimeProvider;
 
 /**
 * GPS enabled device emulator that provided an intial location and velocity,
  * acts according to the the  
  * <a href="http://en.wikipedia.org/wiki/Random_waypoint_model">Random Waypoint 
  * Model </a>. 
  * 
  * Accordingly, each time {@link #getVelocity()} is called, the bearing of the 
  * velocity will shift according to probability specified by 
  * {@link #turnProbability} by maximum of {@link #maxTurn} either in clockwise 
  * or counterclockwise direction.
  *  
  * @author Zorayr Khalapyan
  * @since 3/14/2012
  * @version 3/14/2012
  *
  */
 public class RandomWaypointGeoDevice implements GeoDevice {
 
 	/**
 	 * Current location of the traveling object.
 	 */
 	private GeoLocation currentLocation;
 	
 	/**
 	 * Current velocity of the traveling object.
 	 */
 	private GeoVelocity velocity;
 	
 	/**
 	 * Time provider for the current GPS device. This time provider will be used
 	 * to fetch the current time of the system for calculating change in time.
 	 */
 	private TimeProvider timeProvider;
 
 	/**
 	 * Probability that the direction of the velocity will change each time
 	 * {@link #getLocation()} is called. Probability of 1 indicates that every
 	 * single time the method is invoked, the direction of the velocity will
 	 * change.  
 	 */
 	private double turnProbability;
 	
 	/**
 	 * By default, {@link #turnProbability} is set to 20%.
 	 */
 	private static final double DEFAULT_TURN_PROBABILITY = 0.2;
 	
 	/**
 	 * With a probability of {@link #turnProbability}, GPS direction will change
 	 * by a maximum value either clockwise or counterclockwise specified by this
 	 * variable in radians. The direction may change by [-maxTurn, +maxTurn].
 	 */
 	private double maxTurn;
 	
 	/**
 	 * By default, {@link #maxTurn} is set to about 45 degrees.
 	 */
 	private static final double DEFAULT_MAX_TURN = Math.PI / 4;
 
 	
 	/**
 	 * Creates a new random waypoint model using {@link #DEFAULT_MAX_TURN} and
 	 * {@link #DEFAULT_TURN_PROBABILITY}. By defaul, the time provider will be 
 	 * set to the {@link SystemTimeProvider}.
 	 * 
 	 * 
 	 * @param currentLocation The starting location.
 	 * 
 	 * @param velocity        The starting velocity.
 	 */
 	public RandomWaypointGeoDevice(GeoLocation currentLocation,
 								   GeoVelocity velocity){
 		this(currentLocation, 
 			velocity, 
 			new SystemTimeProvider(), 
 			DEFAULT_MAX_TURN, 
 			DEFAULT_TURN_PROBABILITY);
 	}
 	
 	
 	/**
 	 * Creates a new GPS enabled device emulator based on the 
 	 * <a href="http://en.wikipedia.org/wiki/Random_waypoint_model">Random 
 	 * Waypoint Model </a>. 
 	 * 
 	 * @param currentLocation The starting location.
 	 * 
 	 * @param velocity        The starting velocity.
 	 * 
 	 * @param timeProvider	  Time provider for this device. Might either be 
 	 * 						  system time or a specific test-oriented time 
 	 * 						  provider. 
 	 * 
 	 * @param maxTurn         Maximum allowed turn at each hop.
 	 * 
 	 * @param turnProbability Probability that the direction will change at the 
 	 *                        next hop.
 	 */
 	public RandomWaypointGeoDevice(GeoLocation currentLocation, 
 							   	   GeoVelocity velocity,
 							   	   TimeProvider timeProvider,
 							   	   double maxTurn,
 							   	   double turnProbability){
 		
 		this.currentLocation = currentLocation;
 		this.velocity        = velocity;
 		this.timeProvider    = timeProvider;
 		this.maxTurn         = maxTurn;
 		this.turnProbability = turnProbability;
 		
 		
 	}
 	
 	/**
 	 * Retrieves the current velocity using the method {@link #getVelocity()} 
 	 * and updates the current location according to elapsed time. 
 	 * 
 	 * @see #getVelocity()
 	 */
 	@Override
 	public GeoLocation getLocation() {
 		return currentLocation.move(getVelocity(), timeProvider.getTime());
 	}
 
 
 	/**
 	 * With probability specified by {@link #turnProbability}, updates the 
 	 * bearing direction by up to {@link #maxTurn} and returns the current
 	 * velocity.
 	 * 
 	 * @return Current velocity.
 	 */
 	@Override
 	public GeoVelocity getVelocity() {
 		
 		//Waypoint algorithm.
		if(turnProbability < Math.random()){
 			velocity.setBearing(velocity.getBearing() + (float)(-maxTurn + 2 * maxTurn * Math.random()));
 		}
 		
 		return velocity;
 	}
 
 	/**
 	 * Sets the current time provider.
 	 * @param timeProvider New time provider to set.
 	 */
 	@Override
 	public void setTimeProvider(TimeProvider timeProvider) {
 		this.timeProvider = timeProvider;
 		
 	}
 }
