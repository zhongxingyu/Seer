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
 
 
 	public final static String VERSION = "v0.1.1";
 
 
 	private final static int CTRL_SERIAL_WAIT =						1000; //milliseconds to wait for initial connect
 	private final static int CTRL_PACKET_TIMEOUT =					50; //milliseconds to wait before re-sending un-ACKed packet
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
 
 	//port and pin defns 
 	private final static char PORT_0 =								0x00; //port 0/A definition 
 	private final static char PORT_1 =								0x10; //port 1/B definition 
 	private final static char PORT_2 =								0x20; //port 2/C definition 
 	private final static char PORT_3 =								0x30; //port 3/D definition 
 	private final static char PORT_4 =								0x40; //port 4/E definition 
 	private final static char PORT_5 =								0x50; //port 5/F definition 
 
 	private final static char PIN_0 =								0x00; //pin 0 definition 
 	private final static char PIN_1 =								0x00; //pin 1 definition 
 	private final static char PIN_2 =								0x00; //pin 2 definition 
 	private final static char PIN_3 =								0x00; //pin 3 definition 
 	private final static char PIN_4 =								0x00; //pin 4 definition 
 	private final static char PIN_5 =								0x00; //pin 5 definition 
 	private final static char PIN_6 =								0x00; //pin 6 definition 
 	private final static char PIN_7 =								0x00; //pin 7 definition 
 
 	//in pakcet defns
 	private final static char PKIN_NULL =							0x00; //IN-bound packet NULL 
 	private final static char PKIN_START =							0x2B; //IN-bound start of packet char "+"
 	private final static char PKIN_ACK=								0x01; //IN-bound ACK
 	private final static char PKIN_STAT_ERR=						0x01; //IN-bound packet type "STATUS / ERROR"
 	private final static char PKIN_STAT_ERR_LEN=					0x05; //IN-bound packet type "STATUS / ERROR" length
 
 
 	private final static char[] PKIN_PKET_ACK = {PKIN_START, PKIN_STAT_ERR_LEN, PKIN_STAT_ERR, PKIN_ACK, 0x07};	
 
 
 
 
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
 	 * Constant of pin name P1.0
 	 */
 	public final static char P10 = PORT_1 | PIN_0;
 	/**
 	 * Constant of pin name P1.1
 	 */
 	public final static char P11 = PORT_1 | PIN_1;
 	/**
 	 * Constant of pin name P1.2
 	 */
 	public final static char P12 = PORT_1 | PIN_2;
 	/**
 	 * Constant of pin name P1.3
 	 */
 	public final static char P13 = PORT_1 | PIN_3;
 	/**
 	 * Constant of pin name P1.4
 	 */
 	public final static char P14 = PORT_1 | PIN_4;
 	/**
 	 * Constant of pin name P1.5
 	 */
 	public final static char P15 = PORT_1 | PIN_5;
 	/**
 	 * Constant of pin name P1.6
 	 */
 	public final static char P16 = PORT_1 | PIN_6;
 	/**
 	 * Constant of pin name P1.7
 	 */
 	public final static char P17 = PORT_1 | PIN_7;
 	/**
 	 * Constant of pin name P2.0
 	 */
 	public final static char P20 = PORT_2 | PIN_0;
 	/**
 	 * Constant of pin name P2.1
 	 */
 	public final static char P21 = PORT_2 | PIN_1;
 	/**
 	 * Constant of pin name P2.2
 	 */
 	public final static char P22 = PORT_2 | PIN_2;
 	/**
 	 * Constant of pin name P2.3
 	 */
 	public final static char P23 = PORT_2 | PIN_3;
 	/**
 	 * Constant of pin name P2.4
 	 */
 	public final static char P24 = PORT_2 | PIN_4;
 	/**
 	 * Constant of pin name P2.5
 	 */
 	public final static char P25 = PORT_2 | PIN_5;
 	/**
 	 * Constant of pin name P2.6
 	 */
 	public final static char P26 = PORT_2 | PIN_6;
 	/**
 	 * Constant of pin name P2.7
 	 */
 	public final static char P27 = PORT_2 | PIN_7;
 
 
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
 	public ROCKETuC(PApplet myParent, String iname) {
 		this(myParent, iname, 9600);
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
 	public ROCKETuC (PApplet myParent, String iname, int irate){
 		this.myParent = myParent;
 		this.serialProxy = new SerialProxy();
 		this.serial = new Serial(serialProxy, iname, irate);
 
 		try {
 			System.out.println("ROCKETuC > Connecting ...");
 			Thread.sleep(CTRL_SERIAL_WAIT); //time for serial init?
 		} catch (InterruptedException e) 
 		{
 			System.err.println("ROCKETuC > ERR: Serial connect problem, check your connection");
 		}
 
 		//do stuff on first connect
 		System.out.println("ROCKETuC > Connected");
 		myParent.registerDispose(this);
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
 		try {
 			return serialSendPacket(packet)[0];
 		} catch (myException e) {
 			System.err.println("ROCKETuC > ERR: No ACK on digitalRead, check connection");
 		}	
 		return 2; //should never get here
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
 
 	private char packetCrcCalc(char[] packetIn) {
 
 		char crc = 0;
 
		for(int i = 1; i < packetIn.length; i++) {
 			crc += packetIn[i];
 		} 
 
 		return crc;
 	}	
 
 	private char[] serialSendPacket(char[] packetIn) throws myException
 	{
 		char crc = packetCrcCalc(packetIn);//calculate crc
 		char tries = 0;
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
 				System.err.println("ROCKETuC > WARNING: No ACK, retry");
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
 			System.out.println((int)inChars[0]);
 			System.out.println((int)inChars[1]);
 			System.out.println((int)inChars[2]);
 			System.out.println((int)inChars[3]);
 			char dataLength = (char) (inChars[1] - 4); //calculate length of data segment
 			char dataOut[] = new char[dataLength];
 			//TODO: verify packet is good with CRC and return type
 			for(char i = 3; i < dataLength + 3; i++){ //copy out the data portion of the packet
 				dataOut[i-3] = inChars[i];
 			}
 			return dataOut;
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
 
