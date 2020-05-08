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
  * An instance of this class is used to control one analog sensor on the specified sensor port.
  * 
  * @author Markus Klein
  * @version 1.0.0
  */
 
 public class AnalogSensor extends Sensor {
 	
 	/**
 	 * Creates a new analog sensor on the specified port.
 	 * The invocation {@code AnalogSensor sensor = new AnalogSensor(0)} is equivalent to:
 	 * {@code AnalogSensor sensor = new AnalogSensor();
 	 * sensor.setPort(0);}
 	 * 
 	 * @param sensorPort the port of the analog sensor
 	 */
 	public AnalogSensor(int sensorPort) {
 		super(sensorPort);
 	}
 	
 	/**
	 * Returns the sensor value as 10 bit number(from 0 to 1023).
 	 * 
 	 * @return the current sensor value
 	 */
 	public int getValue() {
 		return LinkJVM.analog10(sensorPort);
 	}
 	
 	/**
	 * Returns the sensor value as 8 bit number(from 0 to 255).
 	 * 
 	 * @return the current sensor value
 	 */
	public short getValue8(){
 		return LinkJVM.analog(sensorPort);
 	}
 	
 	/**
 	 * The analog sensor ports on the KIPR Link provides a pullup sensor 
 	 * for sensor that dont have an integrated pullup resistor.
 	 * For example, {@code sensorInstance.setPullup(1)} enables the pullup and 
 	 * {@code sensorInstance.setPullup(0)} diables the pullup.
 	 * 
 	 * @param pullup pullup
 	 */
 	public void setPullup(int pullup){
 		LinkJVM.set_analog_pullup(sensorPort, pullup);
 	}
 }
