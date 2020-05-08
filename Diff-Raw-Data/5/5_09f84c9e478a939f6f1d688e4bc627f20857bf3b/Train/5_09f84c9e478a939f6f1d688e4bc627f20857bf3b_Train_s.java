 package trainmodel;
 
 public class Train
 {
 	private double length;
 	private int trainLine;
 	private double mass;
 	private int crewCount;
 	private int maxCapacity;
 	private int occupancy;
 	private double distTraveled;
 	private int maxTrainSpeed;
 	private int maxPower;
 	private boolean doors; // false = close, true = open
 	private boolean lights; // false = off, true = on
 	private String trainId;
 	//private boolean ;
 
 	public Train(int line, int crew, String id)
 	{
 		length = 32.2; //m
 		trainLine = line;
 		mass = 40.9; // tons, what do we want?
 		crewCount = crew;
 		maxCapacity =  222 + crewCount;
 		distTraveled = 0; // meters
 		lights = false;
 		maxTrainSpeed = 70; // km/h
 		//maxPower = // ???
		occupancy = crewCount// how many crew members?
 		trainId = id;
 	}
 
 	public double calcVelocity()
 	{
 		// get int power
 		// get track info
 		//
 	}
 
 	public void setLights()
 	{
 		lights = !lights;
 	}
 
 	public void openDoors()
 	{
 		doors = true;
 	}
 
 	public void closeDoors()
 	{
 		doors = false;
 	}
 
 	public int getOccupancy()
 	{
 		return occupancy;
 	}
 
 	public int getLine()
 	{
 		return trainLine;
 	}
 
 	public boolean getTransponder()
 	{
 		return transponder;
 	}
 
	public getTransponderInfo()
 	{
 
 	}
 
 	public void updateTrack()
 	{
 		// set occupied
 		// set unoccupied
 		// give reference to yourself
 		// get grade info getGrade()
 		// check for transponder and set var transponder
 		// get transponder info if it exists
 	}
 
 
 }
