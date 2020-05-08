 package edu.first3729.frc2012;
 
import edu.wpi.first.wpilibj.Joystick;
 
 public class Input
 {
 	
 	private Joystick _joy0;
 	private Joystick _joy1;
 	private Joystick _controller;
 	protected int mode = 0;
 	private int booleanInputs = 0;
 	
         public static final int mecanum = 0;
         public static final int arcade_joy = 1;
         public static final int arcade_controller = 2;
         public static final int tank = 3;
         public static final int locked = 4;
         
 	public Input()
 	{
 		this._joy0 = new Joystick(1);
 		this._joy1 = new Joystick(2);
 		this._controller = new Joystick(3);
 	}
 	
 	public void setMode(int m)
 	{
 		this.mode = m;
 	}
 	
 	public boolean checkButton(int joystick, int button_id)
 	{ 
 		switch (joystick) {
 		case 0:
 			return this._joy0.getRawButton(button_id);
 		case 1:
 			return this._joy1.getRawButton(button_id);
 		case 2:
 			return this._controller.getRawButton(button_id);
 		default:
 			return checkButton(2, button_id);
 		}
 	}
 	
 	public int getMode() { return mode; } 
 	
 	/*
 	 * Modes:
 	 * 0 = mecanum, 3 joysticks
 	 * 1 = arcade, 2 joysticks
 	 * 2 = arcade, 1 joystick 1 controller
 	 * 3 = tank, 3 joysticks
 	 * 4 = locked, no input
 	 */
 	
 	public double getX()
 	{
 		switch(mode) {
 		case mecanum:
 			return normalize(this._joy1.getRawAxis(1), -1.0, 1.0);
 		case arcade_joy:
 			return expo(normalize(this._joy1.getX(), -1.0, 1.0), Params.JOYEXPO);
 		case arcade_controller:
 			return expo(normalize(this._controller.getX(), Params.XMIN, Params.XMAX), Params.XEXPO);
 		case tank:
 			return expo(normalize(this._joy0.getY(), -1.0, 1.0) * -1.0, Params.JOYEXPO);
 		case locked:
 			return 0;
 		default:
 			mode = 2;
 			return getX();
 		}
 	}
 	
 	public double getY()
 	{
 		switch(mode) {
 		case mecanum:
 			return normalize(-this._joy0.getRawAxis(2), -1.0, 1.0);
 		case arcade_joy:
 			return expo(normalize(this._joy1.getY(), -1.0, 1.0) * -1.0, Params.JOYEXPO);
 		case arcade_controller:
 			return expo(normalize(this._controller.getY(),Params.YMIN, Params.YMAX) * -1.0, Params.YEXPO);
 		case tank:
 			return expo(normalize(this._joy1.getY(), -1.0, 1.0) * -1.0, Params.JOYEXPO);
 		case locked:
 			return 0;
 		default:
 			mode = 2;
 			return getY();
 		}
 	}
 	
 	public double getZ()
 	{
 		switch(mode) {
 		case mecanum:
 			return normalize(this._joy0.getRawAxis(1), -1.0, 1.0);
 		case arcade_joy:
 			return expo(normalize(this._joy0.getX(), -1.0, 1.0), Params.JOYEXPO);
 		case arcade_controller:
 			return expo(normalize(this._controller.getZ(),Params.ZMIN, Params.ZMAX), Params.YEXPO);
 		case tank:
 			return expo(normalize(this._joy0.getX(), -1.0, 1.0), Params.JOYEXPO);
 		case locked:
 			return 0;
 		default:
 			mode = 2;
 			return getZ();
 		}
 	}
 	
 	public double getW()
 	{
 		switch (mode) {
 		case mecanum:
 			return normalize(this._joy1.getRawAxis(2), -1.0, 1.0);
 		case arcade_joy:
 			return expo(normalize(this._joy0.getY(), -1.0, 1.0) * -1.0, Params.JOYEXPO);
 		case arcade_controller:
 			return expo(normalize(this._controller.getTwist(),Params.ROTMIN, Params.ROTMAX) * -1.0, Params.ROTEXPO);
 		case tank:
 			return expo(normalize(this._joy1.getX(), -1.0, 1.0), Params.JOYEXPO);
 		case locked:
 			return 0;
 		default:
 			mode = 2;
 			return getW();
 		}
 	}
 	
 	public double getThrottle(int joy)
 	{
 		switch(mode){
 		case mecanum:
 		case arcade_joy:
 			switch(joy) {
 			case 0:
 				return this._joy0.getThrottle();
 			case 1:
 				return this._joy1.getThrottle();
 			default:
 				joy = 0;
 				return getThrottle(joy);
 			}
 		case arcade_controller:
 			return this._joy0.getThrottle();
 		case tank:
 			switch(joy) {
 			case 0:
 				return this._joy0.getThrottle();
 			case 1:
 				return this._joy1.getThrottle();
 			case 2:
 				return this._controller.getThrottle();  // 'Controller' is actually 3rd joystick
 			default:
 				joy = 0;
 				return getThrottle(joy);
 			}
 		case locked:
 			return 0;
 		default:
 			mode = 2;
 			return getThrottle(0);
 		}
 	}
 	
 	public double getThrottle()
 	{
 		return getThrottle(0);
 	}
         
         public double getTwist(int joy)
 	{
 		switch(mode){
 		case mecanum:
 		case arcade_joy:
 			switch(joy) {
 			case 0:
 				return this._joy0.getTwist();
 			case 1:
 				return this._joy1.getTwist();
 			default:
 				joy = 0;
 				return getTwist(joy);
 			}
 		case arcade_controller:
 			return this._joy0.getTwist();
 		case tank:
 			switch(joy) {
 			case 0:
 				return this._joy0.getTwist();
 			case 1:
 				return this._joy1.getTwist();
 			case 2:
 				return this._controller.getTwist();  // 'Controller' is actually 3rd joystick
 			default:
 				joy = 0;
 				return getTwist(joy);
 			}
 		case locked:
 			return 0;
 		default:
 			mode = 2;
 			return getTwist(0);
 		}
 	}
 	
 	public double getTwist()
 	{
 		return getTwist(0);
 	}
 	
 	public int getBooleanButtonInputs(int side)
 	{
 		int i;
 		switch (side) {
 		case 0:
 			for (i = 1; i <= 11; i++) {
 				this.booleanInputs ^= toInt(this._joy0.getRawButton(i)) << (i - 1);
 			}
 			break;
 		case 1:
 			for (i = 1; i <= 11; i++) {
 				this.booleanInputs ^= toInt(this._joy1.getRawButton(i)) << (i - 1);
 			}
 			break;
 		case 2:
 			for (i = 1; i <= 11; i++) {
 				this.booleanInputs ^= toInt(this._controller.getRawButton(i)) << (i - 1);
 			}
 			break;
 		default:
 			return getBooleanButtonInputs(0);
 		}
 		return this.booleanInputs;
 	}
 	
 	private int toInt(boolean thingummy)
 	{
 		if (thingummy)
 			return 1;
 		else
 			return 0;
 	}
 	
 	/**
 	 * @brief Exponential function that makes the controller more sensitive toward the center and less so toward the outside
 	 * 
 	 * @author Adam Bryant
 	 * @param x Input to be expo-ed
 	 * @param a Pre-defined exponentiation factor
 	 * @return The expo-ed value
 	 */
 	private double expo(double x, double a)
 	{
 		return (a * (x * x * x) + (1 - a) * x);
 	}
 	
 	/**
 	 * @brief Normalize input between -1.0 and 1.0.  Also take into account min and max values given by controller.
 	 *
 	 * @author Adam Bryant
 	 * @param joyVal Input value to be normalized
 	 * @param min Minimum controller value.  If using a joystick, supply -1.0.
 	 * @param max Maximum controller value.  If using a joystick, supply 1.0.
 	 * @return The normalized value
 	 */
 	private double normalize(double joyVal, double min, double max)
 	{
 		double retVal = 0.0;
 		if (joyVal < 0.0)
 			retVal = Math.abs(joyVal) / min;
 		else if (joyVal > 0.0)
 			retVal = Math.abs(joyVal) / max;
 		if (retVal < -1.0)
 			retVal = -1.0;
 		else if (retVal > 1.0)
 			retVal = 1.0;
 		return retVal;
 	}
 }
