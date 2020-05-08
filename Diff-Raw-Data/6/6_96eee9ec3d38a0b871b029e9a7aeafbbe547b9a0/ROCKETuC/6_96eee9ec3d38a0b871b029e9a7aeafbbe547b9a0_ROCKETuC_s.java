 /**
  * ROCKETuC
  * Library for fast prototyping of micro-controllers through serial protocols
  * http://www.rocketuc.com
  *
  * Copyright (C) 2012 Alexander Reben 
  *
  * Contains some code / ideas from:
  * Processing/Arduino library, Copyright 2006-08 David A. Mellis 
  * 
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License, or (at your option) any later version.
  * 
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  * 
  * You should have received a copy of the GNU Lesser General
  * Public License along with this library; if not, write to the
  * Free Software Foundation, Inc., 59 Temple Place, Suite 330,
  * Boston, MA  02111-1307  USA
  * 
  * @author      Alexander Reben
  * @modified    4/14/2012
  * @version     v0.1.1 (1)
  */
 
 package rocketuc;
 
 import java.io.File;
 import java.io.FileInputStream;
import java.io.UnsupportedEncodingException;
 import java.net.URISyntaxException;
 import java.net.URL;
import java.net.URLDecoder;
 import java.util.Properties;
 
 import processing.core.*;
 import processing.serial.*;
 
 /**
  * Main ROCKETuC class
  * 
  * @example Hello
  * 
  *       
  */
 
 public class ROCKETuC {
 
 	// mymyParent is a reference to the myParent sketch
 	PApplet myParent;
 	Serial serial;
 	SerialProxy serialProxy;
 	Properties properties = new Properties();
 
 	public final static String VERSION = "v0.1.1";
 
 	private static String DEVICE = null;
 
 	private final static int CTRL_SERIAL_WAIT =						1000; //milliseconds to wait for initial connect
 	private final static int CTRL_PACKET_TIMEOUT =					40; //milliseconds to wait before re-sending un-ACKed packet
 	private final static int CTRL_PACKET_RESEND_LIMIT =				10; //number of times to try re-sending un-ACKed packet
 
 	private final static char POUT_START = 							0x24; 
 
 	private final static char POUT_NULL = 							0x00; //OUT-bound packet type "NULL"
 	private final static char POUT_NULL_LEN = 						0x04; //OUT-bound packet type "NULL" length
 
 	//private final static char POUT_RESERVED = 					0x01; //OUT-bound packet type 
 
 	private final static char POUT_SYS_INFO =						0x02; //OUT-bound packet type "system info"
 	private final static char POUT_SYS_INFO_LEN =					0x02; //OUT-bound packet type "system info" length
 
 	private final static char POUT_DEV_CTRL = 						0x03; //OUT-bound packet type "device control"
 
 	private final static char POUT_PIN_FUNC = 						0x04; //OUT-bound packet type "pin function"
 	private final static char POUT_PIN_FUNC_LEN = 					0x06; //OUT-bound "pin function" length
 
 	private final static char POUT_PIN_CTRL = 						0x05; //OUT-bound packet type "pin control"
 	private final static char POUT_PIN_CTRL_LEN = 					0x02; //OUT-bound packet type "pin control" length
 
 	private final static char POUT_PWM_FUNC = 						0x06; //OUT-bound packet type "pwm function"
 	private final static char POUT_PWM_FUNC_LEN = 					0x03; //OUT-bound packet type "pwm function"
 
 	private final static char POUT_PWM_CTRL = 						0x07; //OUT-bound packet type "pwm control"
 	private final static char POUT_PWM_CTRL_LEN = 					0x03; //OUT-bound packet type "pwm control"
 
 	private final static char POUT_SERL_FUNC =						0x08; //OUT-bound packet type "serial function"
 
 	private final static char POUT_SERL_DATA = 						0x09; //OUT-bound packet type "serial data"
 
 	private final static char POUT_EXT_INTR_FUNC =					0x0A; //OUT-bound packet type "external interrupt function"
 
 	//pin function data commands
 	private final static char CMD_PIN_IN_FLOAT =					0x00; //CMD function for pin function "input float" 	
 	private final static char CMD_PIN_IN_PULL_UP =					0x01; //CMD function for pin function "input pull-up" 	
 	private final static char CMD_PIN_IN_PULL_DOWN =				0x02; //CMD function for pin function "input pull-down" 	
 	private final static char CMD_PIN_OUT =							0x03; //CMD function for pin function "output" 	
 	private final static char CMD_PIN_IN_ANAG =						0x04; //CMD function for pin function "input analog" 	
 	private final static char CMD_PIN_IN_PWM =						0x05; //CMD function for pin function "input PWM" 
 	private final static char CMD_PIN_SER_TX =						0x05; //CMD function for pin function "serial TX" 
 	private final static char CMD_PIN_SER_RX =						0x05; //CMD function for pin function "serial RX" 
 
 	//pin control data commands
 	private final static char CMD_PIN_CLR =							0x00; //CMD function for pin control "clear pin" 	
 	private final static char CMD_PIN_SET =							0x01; //CMD function for pin control "set pin" 	
 	private final static char CMD_PIN_TOGL =						0x02; //CMD function for pin control "toggle pin" 	
 	private final static char CMD_PIN_DIGI_READ =					0x03; //CMD function for pin control "digital read" 	
 	private final static char CMD_PIN_ANAG_READ =					0x04; //CMD function for pin control "analog read" 	
 	private final static char CMD_PIN_PWM_READ =					0x05; //CMD function for pin control "pwm read" 
 
 
 	//in pakcet defns
 	private final static char PKIN_NULL =							0x00; //IN-bound packet NULL 
 	private final static char PKIN_START =							0x2B; //IN-bound start of packet char "+"
 	private final static char PKIN_ACK=								0x01; //IN-bound ACK
 	private final static char PKIN_STAT_ERR=						0x01; //IN-bound packet type "STATUS / ERROR"
 	private final static char PKIN_STAT_ERR_LEN=					0x05; //IN-bound packet type "STATUS / ERROR" length
 
 
 
 
 	/**
 	 * Constant to write a low value to a pin (in a call to
 	 * digitalWrite()).
 	 */
 	public final static char LOW = CMD_PIN_CLR;
 	/**
 	 * Constant to write a high value to a pin (in a call to
 	 * digitalWrite()).
 	 */
 	public final static char HIGH = CMD_PIN_SET ;
 	/**
 	 * Constant to set a pin to output mode (in a call to pinMode()).
 	 */
 	public final static char OUTPUT = CMD_PIN_OUT;
 	/**
 	 * Constant to set a pin to input mode float(in a call to pinMode()).
 	 */
 	public final static char INPUT = CMD_PIN_IN_FLOAT;
 	/**
 	 * Constant to set a pin to input mode pull up(in a call to pinMode()).
 	 */
 	public final static char PULLUP = CMD_PIN_IN_PULL_UP;
 	/**
 	 * Constant to set a pin to input mode pull down(in a call to pinMode()).
 	 */
 	public final static char PULLDOWN = CMD_PIN_IN_PULL_DOWN;
 	/**
 	 * Constant to set a pin to input mode analog(in a call to pinMode()).
 	 */
 	public final static char ANALOG = CMD_PIN_ANAG_READ;
 
 
 	/**
 	 *
 	 * We need a class descended from PApplet so that we can override the
 	 * serialEvent() method to capture serial data.  We can't use the ROCKETuC
 	 * class itself, because PApplet defines a list() method that couldn't be
 	 * overridden by the static list() method we use to return the available
 	 * serial ports.  This class needs to be public so that the Serial class
 	 * can access its serialEvent() method.
 	 */
 	public class SerialProxy extends PApplet {
 		public SerialProxy() {
 			/**
 			 * Create the container for the registered dispose() methods, so that
 			 * our Serial instance can register its dispose() method (which it does
 			 * automatically).
 			 * 
 			 */
 
 			disposeMethods = new RegisteredMethods();
 
 		}
 
 		/*public void serialEvent(Serial which) {
 			// Notify the ROCKETuC class that there's serial data for it to process.
 			while (serial.available() > 0){
 				//processInput();
 			}
 		}*/
 	}
 
 	public void dispose() {
 		this.serial.dispose();
 	}
 
 	/**
 	 * Get a list of the available uC's; currently all serial devices
 	 * (i.e. the same as Serial.list()).  In theory, this should figure out
 	 * what's an uC and what's not.
 	 */
 	public static String[] list() {
 		return Serial.list();
 	}
 
 	/**
 	 * Create a proxy to a uC running the ROCKETuC firmware at the
 	 * default baud rate of 9600.
 	 *
 	 * @param myParent the Processing sketch creating this ROCKETuC board
 	 * (i.e. "this").
 	 * @param iname the name of the serial device associated with the ROCKETuC
 	 * board (e.g. one the elements of the array returned by ROCKETuC.list())
 	 */
 	public ROCKETuC(PApplet myParent, String iname, String idevice) {
 		this(myParent, iname, 9600, idevice);
 	}
 
 	/**
 	 * Create a proxy to uC board running the ROCKETuC firmware.
 	 *
 	 * @param myParent the Processing sketch creating this ROCKETuC board
 	 * (i.e. "this").
 	 * @param iname the name of the serial device associated with the uC
 	 * (e.g. one the elements of the array returned by ROCKETuC.list())
 	 * @param irate the baud rate to use to communicate with the ROCKETuC board
 	 * (the ROCKETuC library defaults to 9600, and the examples use this rate,
 	 * but other firmwares may override it) 
 	 */
 	public ROCKETuC (PApplet myParent, String iname, int irate, String idevice){
 		this.myParent = myParent;
 		this.serialProxy = new SerialProxy();
 		this.serial = new Serial(serialProxy, iname, irate);
 		DEVICE = idevice; //set device type
 		
 		try {
 			System.out.println("ROCKETuC > Connecting ...");
 			Thread.sleep(CTRL_SERIAL_WAIT); //time for serial init?
 		} catch (InterruptedException e) 
 		{
 			System.err.println("ROCKETuC > ERR: Serial connect problem, check your connection");
 		}
 		//load device properties file
 
 		URL root = getClass().getProtectionDomain().getCodeSource().getLocation();
 		String path = null;
 		try {
 			path = (new File(root.toURI())).getParentFile().getPath();
 		} catch (URISyntaxException e1) {
 			// TODO Auto-generated catch block
 		}
 		try {
 		    properties.load(new FileInputStream(path + "\\devices\\" + DEVICE + ".properties"));
 		} catch (Exception e) {
 			System.err.println("ROCKETuC > ERR: Properties file not found or unaccessable, check for " + path + "\\devices\\" + DEVICE + ".properties");
 		}
 
 		//do stuff on first connect
 		System.out.println("ROCKETuC > Connected");
 		myParent.registerDispose(this);
 	}
 	
 	/**
 	 * Return property from properties file
 	 *
 	 * @param string of key for property 
 	 *
 	 */
 	
 	public String getProperty(String property) {
 		return properties.getProperty(property);
 	}
 
 	/**
 	 * Return pin property
 	 *
 	 * @param pin name
 	 *
 	 */
 	
 	public char getPin(String pin) {
	return (char)Integer.parseInt(getProperty(pin)); ///convert string to int then to char
 	}
 	
 
 	/**
 	 * Send NULL packet 
 	 * 
 	 */
 	public void packetNull() {
 		char packet[] = {POUT_START, POUT_NULL_LEN, POUT_NULL};
 		try {
 			serialSendPacket(packet);
 		} catch (myException e) {
 			System.err.println("ROCKETuC > ERR: No ACK on NULL, check connection");
 		}
 	}
 
 
 	/**
 	 * Set a digital pin to input or output mode.
 	 * Also sets pull-ups if present
 	 *
 	 * @param pin the pin whose mode to set 
 	 * @param mode either input(with options) or output
 	 *
 	 */
 	public void pinMode(char pin, char mode) {
 
 		char packet[] = {POUT_START, POUT_PIN_FUNC_LEN, POUT_PIN_FUNC, pin, mode};
 		try {
 			serialSendPacket(packet);
 		} catch (myException e) {
 			System.err.println("ROCKETuC > ERR: No ACK on pinMode, check connection");
 		}
 	}
 
 	/**
 	 * Setup PWM period
 	 * Also sets pull-ups if present
 	 *
 	 * @param pin the pin whose mode to set 
 	 * @param PWM period in milliseconds
 	 *
 	 */
 	public void pwmPeriod(char pin, int period) {	
 		//split out int to chars
 		char lsb = (char)period; 
 		char msb = (char)(period >>> 8);
 		char packet[] = {POUT_START, POUT_PWM_FUNC_LEN, POUT_PWM_FUNC, pin, lsb, msb};
 		try {
 			serialSendPacket(packet);
 		} catch (myException e) {
 			System.err.println("ROCKETuC > ERR: No ACK on pwmSetup, check connection");
 		}
 	}
 
 	/**
 	 * Setup PWM duty cycle
 	 * Also sets pull-ups if present
 	 *
 	 * @param pin the pin whose mode to set 
 	 * @param PWM duty cycle 0-255 is 0-100%
 	 *
 	 */
 	public void pwmDuty(char pin, char duty) {	
 
 		char packet[] = {POUT_START, POUT_PWM_CTRL_LEN, POUT_PWM_CTRL, pin, duty};
 		try {
 			serialSendPacket(packet);
 		} catch (myException e) {
 			System.err.println("ROCKETuC > ERR: No ACK on pwmDuty, check connection");
 		}
 	}
 
 	/**
 	 * Write to a digital pin (the pin must have been put into output mode with
 	 * pinMode()).
 	 *
 	 * @param pin the pin to write to 
 	 * @param value the value to write: ROCKETuC.LOW  or ROCKETuC.HIGH 
 	 * 
 	 */
 	public void digitalWrite(char pin, char value) {
 		char packet[] = {POUT_START, POUT_PIN_FUNC_LEN, POUT_PIN_CTRL, pin, value};
 		try {
 			serialSendPacket(packet);
 		} catch (myException e) {
 			System.err.println("ROCKETuC > ERR: No ACK on digitalWrite, check connection");
 		}	
 	}
 
 	/**
 	 * Read from digital pin (the pin must have been put into input mode with
 	 * pinMode()).
 	 *
 	 * @param pin the pin to read from 
 	 * @return value of pin
 	 */
 	public char digitalRead(char pin) {
 		char packet[] = {POUT_START, POUT_PIN_FUNC_LEN, POUT_PIN_CTRL, pin, CMD_PIN_DIGI_READ};
 		char data[];
 		try {
 			data = serialSendPacket(packet);
 			//System.out.println(data);
 
 		} catch (myException e) {
 			System.err.println("ROCKETuC > ERR: No ACK on digitalRead, check connection");
 			return 2;
 		}	
 		return data[1];
 	}
 
 	/**
 	 * Read from analog pin (the pin must have been put into input mode with
 	 * pinMode()).
 	 *
 	 * @param pin the pin to read from 
 	 * @return value of pin
 	 */
 	public int analogRead(char pin) {
 		char data[];
 		char packet[] = {POUT_START, POUT_PIN_FUNC_LEN, POUT_PIN_CTRL, pin, CMD_PIN_ANAG_READ};
 		try {
 			data = serialSendPacket(packet);
 		} catch (myException e) {
 			System.err.println("ROCKETuC > ERR: No ACK on analogRead, check connection");
 			return -1;
 		}		
 		char lsb = data[1];
 		char msb = data[2];
 		int value =  msb << 8 | lsb;
 		return value;
 	}
 
 	/*private void processInput() {
 		byte[] inputData = serial.readBytes();
 		//char command;
 		System.out.println(inputData); 
 	}
 
 	private void serialSendPacketAck(char[] packetIn)
 	{
 		String out = new String(packetIn); //convert char array to string
 		//System.out.print(out);
 		serial.write(out);
 
 	}
 
 	/**
 	 * 
 	 * Calculate CRC for packet
 	 * Simple adding of all bytes
 	 * 
 	 */
 
 	private char packetCrcCalcOut(char[] packetIn) {
 
 		char crc = 0;
 
 		for(int i = 0; i < packetIn.length; i++) {
 			crc += packetIn[i];
 		} 
 
 		return crc;
 	}	
 	
 	private boolean packetCrcCheckIn(char[] packetIn) {
 
 		char crc = 0;
 		//don't calc last byte (contains
 		for(int i = 0; i < packetIn.length-1; i++) {
 			crc += packetIn[i];
 		} 
 		if(packetIn[packetIn.length-1] == crc){
 			return true;
 		}
 		else{
 			return false;
 		}
 	}	
 
 	private char[] serialSendPacket(char[] packetIn) throws myException
 	{
 		char crc = packetCrcCalcOut(packetIn);//calculate crc
 		int tries = 0;
 		char[] packetOut = new char[packetIn.length+1]; //packet to modify for crc add
 
 		for(int i = 0; i < packetIn.length; i++)
 		{
 			packetOut[i] = packetIn[i]; //copy packet in over
 		}
 		packetOut[packetIn.length] = crc; //slip in crc
 		String out = new String(packetOut); //convert char array to string
 		//System.out.print(out);
 		serial.write(out);
 
 		try {
 			Thread.sleep(CTRL_PACKET_TIMEOUT); //need to sleep to wait for data
 		} catch (InterruptedException e1) {
 		}
 		char [] dataIn = checkForReturn(packetOut); //check for data input and load it
 		if(dataIn != null) //if we got no return packet
 		{
 			return dataIn; //return data portion of in-packet
 		}
 		else
 		{
 			while(dataIn == null) //keep trying to comm
 			{
 				try {
 					Thread.sleep(CTRL_PACKET_TIMEOUT); //need to sleep to wait for data
 				} catch (InterruptedException e) {
 					e.printStackTrace();
 				}
 				if(tries > CTRL_PACKET_RESEND_LIMIT) //if we are below our try limit
 				{
 					throw new myException ("Packet error: no ACK"); //throw an exception if we exceed our retry limit
 				}
 				tries++;
 				System.err.println("ROCKETuC > WARNING: No ACK, retry " + tries + " of " + CTRL_PACKET_RESEND_LIMIT);
 				serial.write(out);
 				dataIn = checkForReturn(packetOut); //check again
 			}
 			return dataIn; //return data portion of in-packet
 		}
 	}
 
 
 	private char[] checkForReturn(char[] packetIn){
 
 		if(serial.available() > 0){
 			String inBuffer = serial.readString(); 
 			serial.clear();
 
 			char inChars[] = inBuffer.toCharArray();
 			//length check
 			if(inChars.length > 4 && inChars.length == inChars[1]){
 				//CRC
 				if(packetCrcCheckIn(inChars)){	
 					char dataLength = (char) (inChars[1] - 4); //calculate length of data segment
 					char dataOut[] = new char[dataLength];
 					//TODO: verify packet is good with return type
 					//copy out the data portion of the packet
 					for(char i = 3; i < dataLength + 3; i++){ 
 						dataOut[i-3] = inChars[i];
 					}
 					return dataOut;
 				}
 				else{
 					System.err.println("ROCKETuC > WARNING: Packet CRC error");
 					return null;
 				}
 			}
 			else{
 				System.err.println("ROCKETuC > WARNING: Packet length mismatch");
 				return null;
 			}
 		}
 		else{
 			System.err.println("ROCKETuC > WARNING: Empty buffer read");
 			return null;
 		}
 	}
 
 	/**
 	 * Checks to see if value is in a list
 	 * 
 	 * @param value
 	 * @param list
 	 * @return is in list
 	 */
 	private boolean isValueInList(char value, char[] list){
 		for(int i = 0; i < list.length; i++)
 		{
 			if(list[i] == value) //if value is in list
 			{
 				return true; //it is in
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * 
 	 * Exception handler
 	 * 
 	 */
 	class myException extends Exception {
 
 		private static final long serialVersionUID = 1L;
 
 		public myException(String msg){
 			super(msg);
 		}
 	}
 
 	//TEST STUFF
 
 
 }
 
