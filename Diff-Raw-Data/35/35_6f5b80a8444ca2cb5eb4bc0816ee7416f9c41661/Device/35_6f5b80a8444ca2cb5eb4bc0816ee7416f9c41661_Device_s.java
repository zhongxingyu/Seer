 /*
  * This file is part of CBCJVM.
  * CBCJVM is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * CBCJVM is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with CBCJVM.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package cbccore;
 
 import cbccore.low.*;
 import cbccore.low.CBCSimulator;
 
 /**
  * A static class with static access to instantiations cbccore.low classes.
  * <p>
  * It is recommended that you use the higher level libraries in
  * cbccore.movement, cbccore.motors, and cbccore.sensors
  * <p>
  * The cbccore.low classes provide a link between the CBC APIs and Java. They
  * are native and contain no Java implementation (except the simulator). They
  * can be useful for quick porting of C code, although, it is highly encouraged
  * to do a clean port atop the CBCJVM API.
  *
  * @author Braden McDorman / Benjamin Woodruff
  *
  */
 
 public class Device {
 	private static CBCSimulator simulator;
 	private static Motor lowMotors;
 	private static Display lowDisplay;
 	private static Create lowCreate;
 	private static Servo lowServos;
 	private static Camera lowCamera;
 	private static cbccore.low.Device lowDevice;
 	private static Input lowInput;
 	private static Sound lowSound;
 	private static Sensor lowSensors;
 	private static boolean onCBC = true;
 
 	static {
 		//we could probably shorten this, some duplication
    	try {
    		if(System.getProperty("CBC") == null) throw new Exception();
    		System.load("/mnt/user/jvm/cbc/CBC.so");
    	}
    	catch(Exception e) {
    		onCBC = false;
    	}
 		try {
 			if(onCBC) {
 				System.out.println("On CBC!");
 				lowSound = new Sound();
 				lowSensors = new Sensor();
 				lowDevice = new cbccore.low.Device();
 				lowDisplay = new Display();
 				lowInput = new Input();
 				lowServos = new Servo();
 				lowMotors = new Motor();
 				lowCamera = new Camera();
 				lowCreate = new Create();
 			} else {
 				simulator = new CBCSimulator();
 				lowSound = simulator.sound;
 				lowSensors = simulator.sensor;
 				lowDevice = simulator.device;
 				lowDisplay = simulator.display;
 				lowInput = simulator.input;
 				lowServos = simulator.servo;
 				lowMotors = simulator.motor;
 				lowCamera = simulator.camera;
 				lowCreate = simulator.create;
 			}
 		}
 		catch(UnsatisfiedLinkError e) {
 			e.printStackTrace();
 		}
 	}
	
	
	
 	/**
	 * Getter for the CBCSimulator object, you probably don't want this. It
	 * should, in theory remain invisible. Checking this for null could be handy
	 * to see if you should activate a simulator work-around.
 	 *
 	 * @return  returns null if not in simulator mode, otherwise the simulator controller
 	 * @see     cbccore.low.CBCSimulator
 	 */
 	public static CBCSimulator getSimulatorController() {
 		return simulator;
 	}
 
 	/**
 	 * Getter for the low level Motor object. It is recommended you use a higher
 	 * level library.
 	 *
 	 * @return  an instance of Motor
 	 * @see     cbccore.low.Motor
 	 */
 	public static Motor getLowMotorController() {
 		return lowMotors;
 	}
 
 	/**
 	 * Getter for the low level Create object. It is recommended you use a
 	 * higher level library.
 	 *
 	 * @return  an instance of Create
 	 * @see     cbccore.low.Create
 	 */
 	public static Create getLowCreateController() {
 		return lowCreate;
 	}
 
 	/**
 	 * Getter for the low level Display object. It is recommended you use a
 	 * higher level library.
 	 *
 	 * @return  an instance of Display
 	 * @see     cbccore.low.Display
 	 */
 	public static Display getLowDisplayController() {
 		return lowDisplay;
 	}
 
 	/**
 	 * Getter for the low level Servo object. It is recommended you use a
 	 * higher level library.
 	 *
 	 * @return  an instance of Servo
 	 * @see     cbccore.low.Servo
 	 */
 	public static Servo getLowServoController() {
 		return lowServos;
 	}
 
 	/**
 	 * Getter for the low level Camera object. It is recommended you use a
 	 * higher level library.
 	 *
 	 * @return  an instance of Camera
 	 * @see     cbccore.low.Camera
 	 */
 	public static Camera getLowCameraController() {
 		return lowCamera;
 	}
 
    /**
 	 * Getter for the low level Device object. It is recommended you use a
 	 * higher level library. (Not to be confused with this "Device" class)
 	 *
 	 * @return  an instance of Device
 	 * @see     cbccore.low.Device
 	 */
 	public static cbccore.low.Device getLowDeviceController() {
 		return lowDevice;
 	}
 
 	/**
 	 * Getter for the low level Input object. It is recommended you use a
 	 * higher level library.
 	 *
 	 * @return  an instance of Input
 	 * @see     cbccore.low.Input
 	 */
 	public static Input getLowInputController() {
 		return lowInput;
 	}
 
 	/**
 	 * Getter for the low level Sound object. It is recommended you use a
 	 * higher level library.
 	 *
 	 * @return  an instance of Sound
 	 * @see     cbccore.low.Sound
 	 */
 	public static Sound getLowSoundController() {
 		return lowSound;
 	}
 
 	/**
 	 * Getter for the low level Sensor object. It is recommended you use a
 	 * higher level library.
 	 *
 	 * @return  an instance of Sensor
 	 * @see     cbccore.low.Sensor
 	 */
 	public static Sensor getLowSensorController() {
 		return lowSensors;
 	}
 	
 	public static boolean isOnCBC() {
 		return onCBC;
 	}
 }
