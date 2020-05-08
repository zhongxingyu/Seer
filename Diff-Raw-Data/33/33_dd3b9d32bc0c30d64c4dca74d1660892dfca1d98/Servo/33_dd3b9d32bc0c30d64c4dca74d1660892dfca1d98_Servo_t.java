 /*
 * This file is part of LinkJVM.
 *
 * Java Framework for the KIPR Link
 * Copyright (C) 2013 Markus Klein<m@mklein.co.at>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
 
 package libkovan;
 
 import libkovan.jni.LinkJVM;
 
 
 /**
 * An instance of this class is used to control a servo on a specified servo port.
 * In addition it provides static methods to disable and enable all servos.
  * 
  * @author Markus Klein
  * @version 1.0.0
  */
 
 public class Servo {
 	
 	private int servoPort;
 	
 	/**
 	 * Creates a new servo on the specified port.
 	 * The invocation {@code Servo servo = new Servo(0)} is equivalent to:
 	 * {@code Servo servo = new Servo();
 	 * servo.setPort(0);}
 	 * 
 	 * @param sensorPort the port of the sensor
 	 */
 	public Servo(int servoPort){
 		this.servoPort = servoPort;
 	}
 	
 	/**
 	 * Sets the port of the servo.
 	 * 
 	 * @param port servo port
 	 */
 	public void setPort(int port){
 		servoPort = port;
 	}
 	
 	/**
 	 * Returns the port of the servo instance.
 	 * 
 	 * @return servo port
 	 */
 	public int getPort(){
 		return servoPort;
 	}
 	
 	/**
 	 * Disables the servo.
 	 */
 	public void disable(){
 		LinkJVM.disable_servo(servoPort);
 	}
 	
 	/**
 	 * Enables the servo.
 	 */
 	public void enable(){
 		LinkJVM.enable_servo(servoPort);
 	}
 	
 	/**
 	 * Moves the servo to the specified position.
 	 * 
 	 * @param pos position (range from 0 to 2047)
 	 */
 	public void setPosition(int pos){
 		LinkJVM.set_servo_position(servoPort, pos);
 	}
 	
 	/**
 	 * Returns the position of the servo.
 	 * 
 	 * @return servo position (range from 0 to 2047)
 	 */
 	public int getPosition(){
 		return LinkJVM.get_servo_position(servoPort);
 	}
 	
 	/**
 	 * Enables all servos.
 	 */
 	public static void enableServos(){
 		LinkJVM.enable_servos();
 	}
 	
 	/**
 	 * Disables all servos.
 	 */
 	public static void disableServos(){
 		LinkJVM.disable_servos();
 	}
 }
