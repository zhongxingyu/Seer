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
 * The Sensor class is a static class. It provides all basic functions for a sensor instance.
  * In addition it offers a static function for reading the button values.
  * 
  * @author Markus Klein
  * @version 1.0.0
  */
 
 public abstract class Sensor {
 	
 	protected int sensorPort;
 	/**
 	 * Constant for the A button 
 	 */
 	public static final int A_BUTTON = 0;
 	public static final int B_BUTTON = 1;
 	public static final int SIDE_BUTTON = 2;
 	
 	/**
 	 * Create a new sensor instance on the specified sensor port.
 	 * @param sensorPort
 	 */
 	public Sensor(int sensorPort){
 		this.sensorPort = sensorPort;
 	}
 	
 	/**
 	 * This method is used to request if one of the buttons on the KIPR Link is pressed.
 	 * For example, {@code Sesnor.getButtonPressed(A_BUTTON)} returns if the A button is pressed.
 	 * 
 	 * @param btn button constant
 	 * @return button value (1 for pressed and 0 for not)
 	 */
 	public static int getButtonPressed(int btn){
 		switch(btn){
 		case 0:
 			return LinkJVM.a_button();
 		case 1:
 			return LinkJVM.b_button();
 		case 2:
 			return LinkJVM.side_button();
 		default:
 			return 0;
 		}
 	}
 	
 	/**
 	 * Returns the port of the sensor instance.
 	 * 
 	 * @return port
 	 */
 	public int getPort(){
 		return sensorPort;
 	}
 	
 	/**
 	 * Sets the port of the sensor instance.
 	 * 
 	 * @param sensorPort sensor port
 	 */
 	public void setPort(int sensorPort){
 		this.sensorPort = sensorPort;
 	}
 	
 	/**
 	 * Returns the current value of the sensor.
 	 * 
 	 * @return sensor value
 	 */
 	public abstract int getValue();
 }
