 package com.jaxelson;
 
 import java.awt.Graphics2D;
 import java.io.Serializable;
 
 import navigation.ExtendedBot;
 import navigation.GravPoint;
 import robocode.AdvancedRobot;
 import robocode.ScannedRobotEvent;
 
 public class BotInfo implements Serializable,Comparable<BotInfo> {
 	private static final long serialVersionUID = -6633270333555835298L;
 	private static final double DEFAULT_STRENGTH = 50;
 	
 	// Bot Info
 	private String _name;
 	private double _energy;
 	private double _heading;
 	private double _headingRadians;
 	private double _oldHeading;
 	private double _oldHeadingRadians;
 	private double _velocity;
 	private long _time;
 	
 	/**
 	 * How much this robot is to be avoided, chiefly used in anti-gravity movement
 	 */
 	private double _strength = DEFAULT_STRENGTH; 
 	
 	/** Location of this BotInfo */
 	private ExtendedPoint2D _location;
 	
 	/** The robot that created this BotInfo */
 	private ExtendedBot _robot;
 	
 	// Misc Info
 	public int _numUpdates;
 
 	public BotInfo(ScannedRobotEvent e, ExtendedBot robot) {
 		this.setRobot(robot);
     	this.update(e);
 	}
 
 	/**
 	 * Update the information about this enemy
 	 * @param e ScannedRobotEvent information to use to update
 	 */
 	public void update(ScannedRobotEvent e) {
 		_numUpdates++;
 		savePreviousValues();
 		
 		String enemyName = BotUtility.fixName(e.getName());
 		
 		this.setName(enemyName);
     	this.setEnergy(e.getEnergy());
     	this.setHeading(e.getHeading());
     	this.setHeadingRadians(e.getHeadingRadians());
     	this.setVelocity(e.getVelocity());
     	this.setTime(e.getTime());
     	
 		double angle = _robot.getHeadingRadians() + e.getBearingRadians();
 		ExtendedPoint2D enemyLocation = new ExtendedPoint2D((_robot.getX() + Math.sin(angle) * e.getDistance()),
 				(_robot.getY() + Math.cos(angle) * e.getDistance()));
     	this.setLocation(enemyLocation);
 	}
 	
 	/**
 	 * Saves previous values from last scan
 	 */
 	private void savePreviousValues() {
 		setOldHeading(_heading);
 		setOldHeadingRadians(_headingRadians);
 	}
 
 	/**
 	 * Returns time since this robot was last seen
 	 * @param currentTime This is only required until I can figure out a workaround
 	 * @return the time since this robot was last seen
 	 */
 	public long timeSinceSeen() {
 		//Doesn't compile
 		//System.out.println(StatusEvent.getStatus().getTime());
 		return _robot.getTime() - this.getTime();
 	}
 	
 	public void printBot() {
 		System.out.println("Name: "+ this.getName());
 		System.out.println("Bearing: "+ this.getBearing());
 		System.out.println("Bearing (radians): "+ this.getBearingRadians());
 		System.out.println("Distance: "+ this.getDistance());
 		System.out.println("Energy: "+ this.getEnergy());
 		System.out.println("Heading: "+ this.getHeading());
 		System.out.println("Heading (radians)"+ this.getHeadingRadians());
 		System.out.println("Velocity: "+ this.getVelocity());
 		System.out.println("Time: "+ this.getTime());
 		System.out.println("Location: " + this.getLocation());
 	}
 
 	public String toString() {
 		StringBuilder string = new StringBuilder();
 		
 		string.append("Name: "+ this.getName());
 		string.append(" Bearing: "+ this.getBearing());
 		string.append(" Bearing (radians): "+ this.getBearingRadians());
 		string.append(" Distance: "+ this.getDistance());
 		string.append(" Energy: "+ this.getEnergy());
 		string.append(" Heading: "+ this.getHeading());
 		string.append(" Heading (radians)"+ this.getHeadingRadians());
 		string.append(" Velocity: "+ this.getVelocity());
 		string.append(" Time: "+ this.getTime());
 		string.append(" Location: " + this.getLocation());
 		
 		return string.toString();
 	}
 
 	public GravPoint getGravPoint() {
 		GravPoint p = new GravPoint(_location.x, _location.y, getStrength());
 		return p;
 	}
 	
 	/**
 	 * Returns the bearing in radians
 	 * @return bearing from robot to this enemy robot
 	 */
 	public double getBearingRadians() {
 		ExtendedPoint2D robotLoc = _robot.getLocation();
 		return _location.bearingTo(robotLoc, _robot);
 	}
 	
	/**
	 * Gives absolute angle to this bot
	 * @return absolute angle to this bot
	 */
 	public double getAngle() {
 		ExtendedPoint2D robotLoc = _robot.getLocation();
 		return _location.angleTo(robotLoc);
 	}
 	
 	/***************************/
 	/* Getters and Setters     */
 	/***************************/
 	
 	public double getX() {
 		return _location.getX();
 	}
 	
 	public double getY() {
 		return _location.getY();
 	}
 
 	public String getName() {
 		return _name;
 	}
 
 	public void setName(String name) {
 		this._name = BotUtility.fixName(name);
 	}
 
 	/**
 	 * @return the _oldHeading
 	 */
 	public double getOldHeading() {
 		return _oldHeading;
 	}
 
 	/**
 	 * @param oldHeading the _oldHeading to set
 	 */
 	public void setOldHeading(double oldHeading) {
 		_oldHeading = oldHeading;
 	}
 
 	public double getBearing() {
 		double radians = getBearingRadians();
 		return radians/Math.PI * 180;
 	}
 	
 	/**
 	 * @return the _oldHeadingRadians
 	 */
 	public double getOldHeadingRadians() {
 		return _oldHeadingRadians;
 	}
 
 	/**
 	 * @param oldHeadingRadians the _oldHeadingRadians to set
 	 */
 	public void setOldHeadingRadians(double oldHeadingRadians) {
 		_oldHeadingRadians = oldHeadingRadians;
 	}
 
 	public double getDistance() {
 		return _robot.getLocation().distance(_location);
 	}
 
 	public void setEnergy(double energy) {
 		this._energy = energy;
 	}
 
 	public double getEnergy() {
 		return _energy;
 	}
 
 	public void setHeading(double heading) {
 		this._heading = heading;
 	}
 
 	public double getHeading() {
 		return _heading;
 	}
 
 	public void setHeadingRadians(double headingRadians) {
 		this._headingRadians = headingRadians;
 	}
 
 	public double getHeadingRadians() {
 		return _headingRadians;
 	}
 
 	public void setVelocity(double velocity) {
 		this._velocity = velocity;
 	}
 
 	public double getVelocity() {
 		return _velocity;
 	}
 
 	public void setTime(long time) {
 		this._time = time;
 	}
 
 	public long getTime() {
 		return _time;
 	}
 
 	public void setLocation(ExtendedPoint2D location) {
 		this._location = location;
 	}
 	
 	public ExtendedPoint2D getLocation() {
 		return _location;
 	}
 
 	/**
 	 * @return the robot
 	 */
 	public AdvancedRobot getRobot() {
 		return _robot;
 	}
 
 	/**
 	 * @param robot the robot to set
 	 */
 	public void setRobot(ExtendedBot robot) {
 		this._robot = robot;
 	}
 	
 	/**
 	 * @param _strength the _strength to set
 	 */
 	public void setStrength(double _strength) {
 		this._strength = _strength;
 	}
 
 	/**
 	 * @return the _strength
 	 */
 	public double getStrength() {
 		return _strength;
 	}
 
 	public int getNumUpdates() {
 		return _numUpdates;
 	}
 
 	public void paintTrackingRectangle(AdvancedRobot robot, Graphics2D g) {
 	     int x = (int)getX();
 	     int y = (int)getY();
 	     
 	     // Draw a line from our robot to the scanned robot
 	     g.drawLine(x, y, (int)robot.getX(), (int)robot.getY());
 	 
 	     // Draw a filled square on top of the scanned robot that covers it
 	     g.fillRect(x - 20, y - 20, 40, 40);
 	}
 
 	/* (non-Javadoc)
 	 * @see java.lang.Comparable#compareTo(java.lang.Object)
 	 */
 	@Override
 	public int compareTo(BotInfo o) {
 		if(this.getEnergy() < o.getEnergy()) {
 			return -1;
 		} else if(this.getEnergy() > o.getEnergy()) {
 			return 1;
 		} else {
 			return 0;
 		}
 	}
 }
