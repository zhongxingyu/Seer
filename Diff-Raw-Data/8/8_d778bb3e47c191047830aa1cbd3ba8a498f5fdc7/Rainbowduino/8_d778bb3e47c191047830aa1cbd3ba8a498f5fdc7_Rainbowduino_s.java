 /*
 A nice wrapper class to control the Rainbowduino 
 
 (c) copyright 2009 by rngtng - Tobias Bielohlawek
 (c) copyright 2010 by Michael Vogt/neophob.com 
 http://code.google.com/p/rainbowduino-firmware/wiki/FirmwareFunctionsReference
 
 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.
 
 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.
 
 You should have received a copy of the GNU Lesser General
 Public License along with this library; if not, write to the
 Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 Boston, MA  02111-1307  USA
  */
 
 package com.neophob.lib.rainbowduino;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import processing.core.PApplet;
 import processing.serial.Serial;
 
 /**
  * library to communicate with an arduino via serial port<br>
  * the arduino control up to n rainbowduinos using the i2c protocol
  * <br><br>
  * part of the neorainbowduino library
  * 
  * TODO: add blacklist for serial port detection!
  * 
  * @author Michael Vogt / neophob.com
  *
  */
 public class Rainbowduino {
 
 	static Logger log = Logger.getLogger(Rainbowduino.class.getName());
 
 	/**
 	 * number of leds horizontal<br>
 	 * TODO: should be dynamic, someday
 	 */
 	public static int NR_OF_LED_HORIZONTAL = 8;
 
 	/**
 	 * number of leds vertical<br>
 	 * TODO: should be dynamic, someday
 	 */
 	public static int NR_OF_LED_VERTICAL = NR_OF_LED_HORIZONTAL;
 
 	/** 
 	 * internal lib version
 	 */
 	public static final String VERSION = "1.4";
 
 	private static final byte START_OF_CMD = 0x01;
 	private static final byte CMD_SENDFRAME = 0x03;
 	private static final byte CMD_PING = 0x04;
 	private static final byte CMD_INIT_RAINBOWDUINO = 0x05;
 	private static final byte CMD_SCAN_I2C_BUS = 0x06;
 
 	private static final byte START_OF_DATA = 0x10;
 	private static final byte END_OF_DATA = 0x20;
 
 	private PApplet app;
 
 	private int baud = 115200;
 	private Serial port;
 	
 	private long arduinoHeartbeat;
 	private int arduinoBufferSize;
 	
 	//logical errors reported by arduino, TODO: rename to lastErrorCode
 	private int arduinoErrorCounter;
 	
 	//connection errors to arduino
 	private int connectionErrorCounter;
 	
 	/**
 	 * result of i2c bus scan
 	 */
 	private List<Integer> scannedI2cDevices;
 	
 	/**
 	 * map to store checksum of image
 	 */
 	private Map<Byte, String> lastDataMap;
 	
 	//the home made gamma table - please note:
 	//the rainbowduino has a color resoution if 4096 colors (12bit)
 	private static int[] gammaTab = {       
 		0,      0,      0,      0,      0,      0,      0,      0,
 		0,      0,      0,      0,      0,      0,      0,      0,
 		0,      0,      0,      0,      0,      0,      0,      0,
 		0,      0,      0,      0,      0,      0,      0,      0,
 		0,      0,      0,      0,      0,      0,      0,      0,
 		0,      0,      0,      0,      16,     16,     16,     16,
         16,     16,     16,     16,     16,     16,     16,     16, 
         16,     16,     16,     16,     16,     16,     16,     16, 
         16,     16,     16,     16,     16,     16,     16,     16,
         16,     16,     16,     16,     16,     16,     16,     16,
         32,     32,     32,     32,     32,     32,     32,     32, 
         32,     32,     32,     32,     32,     32,     32,     32, 
         32,     32,     32,     32,     32,     32,     32,     32, 
         32,     32,     32,     32,     32,     32,     32,     32, 
         32,     32,     32,     32,     48,     48,     48,     48, 
         48,     48,     48,     48,     48,     48,     48,     48, 
         48,     48,     48,     48,     48,     48,     48,     48, 
         48,     48,     48,     48,     64,     64,     64,     64, 
         64,     64,     64,     64,     64,     64,     64,     64, 
         64,     64,     64,     64,     64,     64,     64,     64, 
         64,     64,     64,     64,     64,     64,     64,     64, 
         80,     80,     80,     80,     80,     80,     80,     80, 
         80,     80,     80,     80,     80,     80,     80,     80, 
         96,     96,     96,     96,     96,     96,     96,     96, 
         96,     96,     96,     96,     96,     96,     96,     96, 
         112,    112,    112,    112,    112,    112,    112,    112, 
         128,    128,    128,    128,    128,    128,    128,    128, 
         144,    144,    144,    144,    144,    144,    144,    144, 
         160,    160,    160,    160,    160,    160,    160,    160, 
         176,    176,    176,    176,    176,    176,    176,    176, 
         192,    192,    192,    192,    192,    192,    192,    192, 
         208,    208,    208,    208,    224,    224,    224,    224, 
         240,    240,    240,    240,    240,    255,    255,    255 
     };
 
 	
 	/**
 	 * Create a new instance to communicate with the rainbowduino.
 	 * 
 	 * @param _app
 	 * @param rainbowduinoAddr
 	 * @throws NoSerialPortFoundException
 	 */
 	public Rainbowduino(PApplet _app, List<Integer> rainbowduinoAddr) 
 		throws NoSerialPortFoundException {
 		this(_app, null, 0, rainbowduinoAddr);
 	}
 
 	/**
 	 * Create a new instance to communicate with the rainbowduino.
 	 * 
 	 * @param _app
 	 * @param rainbowduinoAddr
 	 * @param baud
 	 * @throws NoSerialPortFoundException
 	 */
 	public Rainbowduino(PApplet _app, List<Integer> rainbowduinoAddr, int baud) 
 		throws NoSerialPortFoundException {
 		this(_app, null, baud, rainbowduinoAddr);
 	}
 
 	/**
 	 * Create a new instance to communicate with the rainbowduino.
 	 * 
 	 * @param _app
 	 * @param rainbowduinoAddr
 	 * @param portName
 	 * @throws NoSerialPortFoundException
 	 */
 	public Rainbowduino(PApplet _app, List<Integer> rainbowduinoAddr, String portName) 
 		throws NoSerialPortFoundException {
 		this(_app, portName, 0, rainbowduinoAddr);
 	}
 
 
 	/**
 	 * Create a new instance to communicate with the rainbowduino.
 	 * 
 	 * @param _app
 	 * @param portName
 	 * @param baud
 	 * @param rainbowduinoAddr
 	 * @throws NoSerialPortFoundException
 	 */
 	public Rainbowduino(PApplet _app, String portName, int baud, List<Integer> rainbowduinoAddr) 
 		throws NoSerialPortFoundException {
 		
 		this.app = _app;
 		app.registerDispose(this);
 		
 		scannedI2cDevices = new ArrayList<Integer>();
 		lastDataMap = new HashMap<Byte, String>();
 		
 		String serialPortName="";
 		if(baud > 0) {
 			this.baud = baud;
 		}
 		
 		if (portName!=null && !portName.trim().isEmpty()) {
 			//open specific port
 			log.log(Level.INFO,	"open port: {0}", portName);
 			serialPortName = portName;
 			openPort(portName, rainbowduinoAddr);
 		} else {
 			//try to find the port
 			String[] ports = Serial.list();
 			for (int i=0; port==null && i<ports.length; i++) {
 				log.log(Level.INFO,	"open port: {0}", ports[i]);
 				try {
 					serialPortName = ports[i];
 					openPort(ports[i], rainbowduinoAddr);
 				//catch all, there are multiple exception to catch (NoSerialPortFoundException, PortInUseException...)
 				} catch (Exception e) {
 					// search next port...
 				}				
 			}
 		}
 		
 		if (port==null) {
 			throw new NoSerialPortFoundException("Error: no serial port found!");
 		}
 		
 		log.log(Level.INFO,	"found serial port: "+serialPortName);
 	}
 
 
 	/**
 	 * clean up library
 	 */
 	public void dispose() {
 		if(connected()) port.stop();
 	}
 
 
 
 	/**
 	 * return the version of the library.
 	 *
 	 * @return String version number
 	 */
 	public String version() {
 		return VERSION;
 	}
 
 	/**
 	 * return connection state of lib 
 	 * 
 	 * @return wheter rainbowudino is connected
 	 */
 	public boolean connected() {
 		return (port != null);
 	}	
 
 
 	
 
 	/**
  	 * 
  	 * Open serial port with given name. Send ping to check if port is working.
 	 * If not port is closed and set back to null
 	 * 
 	 * @param portName
 	 */
 	private void openPort(String portName, List<Integer> rainbowduinoAddr) throws NoSerialPortFoundException {
 		if (portName == null) {
 			return;
 		}
 		
 		try {
 			port = new Serial(app, portName, this.baud);
 			sleep(1500); //give it time to initialize		
 			if (ping()) {
 
 				//send initial image to rainbowduinos
 				for (int i: rainbowduinoAddr) {
 					this.initRainbowduino((byte)i);					
 				}
 				
 				return;
 			}
 			log.log(Level.WARNING, "No response from port {0}", portName);
 			if (port != null) {
 				port.stop();        					
 			}
 			port = null;
 			throw new NoSerialPortFoundException("No response from port "+portName);
 		} catch (Exception e) {	
 			log.log(Level.WARNING, "Failed to open port {0}: {1}", new Object[] {portName, e});
 			if (port != null) {
 				port.stop();        					
 			}
 			port = null;
 			throw new NoSerialPortFoundException("Failed to open port "+portName+": "+e);
 		}	
 	}
 
 
 
 	/**
 	 * send a serial ping command to the arduino board.
 	 * 
 	 * @return wheter ping was successfull (arduino reachable) or not
 	 */
 	public boolean ping() {		
 		/*
 		 *  0   <startbyte>
 		 *  1   <i2c_addr>
 		 *  2   <num_bytes_to_send>
 		 *  3   command type, was <num_bytes_to_receive>
 		 *  4   data marker
 		 *  5   ... data
 		 *  n   end of data
 		 */
 		byte cmdfull[] = new byte[7];
 		cmdfull[0] = START_OF_CMD;
 		cmdfull[1] = 0; //unused here!
 		cmdfull[2] = 0x01;
 		cmdfull[3] = CMD_PING;
 		cmdfull[4] = START_OF_DATA;
 		cmdfull[5] = 0x02;
 		cmdfull[6] = END_OF_DATA;
 
 		try {
 			writeSerialData(cmdfull);
 			return waitForAck();			
 		} catch (Exception e) {
 			return false;
 		}
 	}
 
 	/**
 	 * Initiate a I2C bus scan<br>
 	 * The result will be stored in the scannedI2cDevices list.<br>
 	 * Hint: it takes some time for the scan to finish - wait 1-2s before you
 	 *       check the result.
 	 */
 	private boolean i2cBusScan() {		
 		byte cmdfull[] = new byte[7];
 		cmdfull[0] = START_OF_CMD;
 		cmdfull[1] = 0;
 		cmdfull[2] = 1;
 		cmdfull[3] = CMD_SCAN_I2C_BUS;
 		cmdfull[4] = START_OF_DATA;
 		cmdfull[5] = 0;
 		cmdfull[6] = END_OF_DATA;
 		
 		try {
 			writeSerialData(cmdfull);
 			return waitForI2cResultAndAck();			
 		} catch (Exception e) {
 			return false;
 		}
 	}
 	
 	
 	/**
 	 * wrapper class to send a RGB image to the rainbowduino.
 	 * the rgb image gets converted to the rainbowduino compatible
 	 * "image format"
 	 * 
 	 * @param addr the i2c address of the device
 	 * @param data rgb data (int[64], each int contains one RGB pixel)
 	 */
 	public boolean sendRgbFrame(byte addr, int[] data) {
 		return sendFrame(addr, convertRgbToRainbowduino(data));
 	}
 
 	
 	/**
 	 * get md5 hash out of an image. used to check if the image changed
 	 * @param addr
 	 * @param data
 	 * @return
 	 */
 	private boolean didFrameChange(byte addr, byte data[]) {
 		String s = HelperUtils.getMD5(data);
 		
 		if (!lastDataMap.containsKey(addr)) {
 			//first run
 			lastDataMap.put(addr, s);
 			return true;
 		}
 		
 		//log.log(Level.INFO, "{0} // {1}",new Object [] {s, lastDataMap.get(addr)});
 		
 		if (lastDataMap.get(addr).equals(s)) {
 			//last frame was equal current frame, do not send it!
 			//log.log(Level.INFO, "do not send frame to {0}", addr);
 			return false;
 		}
 		//update new hash
 		lastDataMap.put(addr, s);
 		return true;
 	}
 	
 	/**
 	 * send a frame to the active rainbowduino the data needs to be in this format:
 	 * buffer[3][8][4], The array to be sent formatted as [color][row][dots]   
 	 * 
 	 * @param addr the i2c address of the device
 	 * @param data byte[3*8*4]
 	 * @param check wheter to perform sensity check
 	 */
 	public boolean sendFrame(byte addr, byte data[]) {
 		//TODO stop if connection counter > n
 		//if (connectionErrorCounter>10000) {}
 		
 		if (!didFrameChange(addr, data)) {
 			return false;
 		}
 		
 		//log.log(Level.INFO, "Send data to device {0}", addr);
 		
 		byte cmdfull[] = new byte[6+data.length];
 		cmdfull[0] = START_OF_CMD;
 		cmdfull[1] = addr;
 		cmdfull[2] = (byte)data.length;
 		cmdfull[3] = CMD_SENDFRAME;
 		cmdfull[4] = START_OF_DATA;		
 		for (int i=0; i<data.length; i++) {
 			cmdfull[5+i] = data[i];
 		}
 		cmdfull[data.length+5] = END_OF_DATA;
 		
 		try {
 			writeSerialData(cmdfull);
 			return waitForAck();			
 		} catch (Exception e) {
 			return false;
 		}
 	}
 
 	/**
 	 * initialize an rainbowduino device - send the initial image to
 	 * the rainbowduino. check arduinoErrorCounter for any errors.
 	 * 
 	 * @param addr the i2c slave address of the rainbowduino
 	 */
 	public boolean initRainbowduino(byte addr) {
 		//TODO stop if connection counter > n
 		//if (connectionErrorCounter>10000) {}
 		
 		byte cmdfull[] = new byte[7];
 		cmdfull[0] = START_OF_CMD;
 		cmdfull[1] = addr;
 		cmdfull[2] = 1;
 		cmdfull[3] = CMD_INIT_RAINBOWDUINO;
 		cmdfull[4] = START_OF_DATA;
 		cmdfull[5] = 0;
 		cmdfull[6] = END_OF_DATA;
 		
 		try {
 			writeSerialData(cmdfull);
 			return waitForAck();			
 		} catch (Exception e) {
 			return false;
 		}
 	}
 	
 	/**
 	 * get last error code from arduino
 	 * if the errorcode is between 100..109 - serial connection issue (pc-arduino issue)
 	 * if the errorcode is < 100 it's a i2c lib error code (arduino-rainbowduino error)
 	 *    check http://arduino.cc/en/Reference/WireEndTransmission for more information
 	 *   
 	 * @return last error code from arduino
 	 */
 	public int getArduinoErrorCounter() {
 		return arduinoErrorCounter;
 	}
 
 	/**
 	 * return the serial buffer size of the arduino
 	 * 
 	 * the buffer is by default 128 bytes - if the buffer is most of the
 	 * time almost full (>110 bytes) you probabely send too much serial data 
 	 * 
 	 * @return arduino filled serial buffer size 
 	 */
 	public int getArduinoBufferSize() {
 		return arduinoBufferSize;
 	}
 
 	/**
 	 * per default arduino update this library each 3s with statistic information
 	 * this value save the timestamp of the last message.
 	 * 
 	 * @return timestamp when the last heartbeat receieved. should be updated each 3s.
 	 */
 	public long getArduinoHeartbeat() {
 		return arduinoHeartbeat;
 	}
 	
 	/**
 	 * send the data to the serial port
 	 * @param cmdfull
 	 */
 	private synchronized void writeSerialData(byte[] cmdfull) throws SerialPortException {
 		//TODO handle the 128 byte buffer limit!
 		if (port==null) {
 			throw new SerialPortException("port is null");
 		}
 		
 		try {
 			port.output.write(cmdfull);
 			//TODO flush or not?? -> do NOT!
 			//port.output.flush();
 		} catch (Exception e) {
 			log.log(Level.INFO, "Error sending serial data!", e);
 			connectionErrorCounter++;
 			throw new SerialPortException("cannot send serial data: "+e);
 		}		
 	}
 	
 	/**
 	 * read data from serial port, wait for ACK
 	 * @return true if ack received, false if not
 	 */
 	private synchronized boolean waitForAck() {
 		//wait for ack/nack
 		//TODO make this configurabe
 		int timeout=10; //wait up to 100ms
 		while (timeout > 0 && port.available() < 3) {
 			sleep(10); //in ms
 			timeout--;
 		}
 
 		byte[] msg = port.readBytes();
 		if (timeout < 1) {
 			log.log(Level.INFO, "Invalid serial data {0}", Arrays.toString(msg));
 			return false;
 		}
 
 		//INFO: MEEE [0, 0, 65, 67, 75, 0, 0]
		for (int i=0; i<msg.length-3; i++) {
 			if (msg[i]== 'A' && msg[i+1]== 'C' && msg[i+2]== 'K') {
 				try {
 					this.arduinoBufferSize = msg[i+3];
 					this.arduinoErrorCounter = msg[i+4];					
 				} catch (Exception e) {
 					// TODO: handle exception
 				}
 				this.arduinoHeartbeat = System.currentTimeMillis();
 				return true;
 			}			
 		}
 		
		if (msg.length==3 && msg[0]== 'A' && msg[1]== 'C' && msg[2]== 'K') {
			this.arduinoHeartbeat = System.currentTimeMillis();
			return true;			
		}
		
 		log.log(Level.INFO, "Invalid serial data {0}", Arrays.toString(msg));
 		return false;		
 	}
 	
 	
 	/**
 	 * read data from serial port, wait for ACK
 	 * @return true if ack received, false if not
 	 */
 	private synchronized boolean waitForI2cResultAndAck() {
 		//wait for ack/nack
 		//TODO make this configurabe
 		int timeout=10; //wait up to 100ms
 		while (timeout > 0 && port.available() < 3) {
 			sleep(10); //in ms
 			timeout--;
 		}
 
 		byte[] msg = port.readBytes();
 		if (timeout < 1) {
 			log.log(Level.INFO, "Invalid serial data {0}", Arrays.toString(msg));
 			return false;
 		}
 
 		
 		for (int i=0; i<msg.length-3; i++) {
 			if (msg[i]==START_OF_CMD && msg[i+1]==CMD_SCAN_I2C_BUS) {
 				//process i2c scanning result
 				for (int x=2; x<msg.length; x++) {                                                              
 					int n;
 					try {
 						n = Integer.parseInt(""+msg[x]);
 						if (n==255 || n<0) {
 							break;
 						}
 						log.log(Level.INFO, "Reply from I2C device: #{0}", n);
 						scannedI2cDevices.add(n);
 					} catch (Exception e) {}
 				}
 			}
 
 			if (msg[i]== 'A' && msg[i+1]== 'C' && msg[i+2]== 'K') {
 				try {
 					this.arduinoBufferSize = msg[i+3];
 					this.arduinoErrorCounter = msg[i+4];					
 				} catch (Exception e) {
 					// TODO: handle exception
 				}
 				this.arduinoHeartbeat = System.currentTimeMillis();
 				return true;
 			}			
 		}
 		
 		if (msg.length==3 && msg[0]== 'A' && msg[1]== 'C' && msg[2]== 'K') {
 			this.arduinoHeartbeat = System.currentTimeMillis();
 			return true;			
 		}
 		
 		log.log(Level.INFO, "Invalid serial data {0}", Arrays.toString(msg));
 		return false;		
 	}
 
 
 	/**
 	 * convert rgb image data to rainbowduino compatible format
 	 * format 8x8x4
 	 * 
 	 * @param data the rgb image as int[64]
 	 * @return rainbowduino compatible format as byte[3*8*4] 
 	 */
 	private static byte[] convertRgbToRainbowduino(int[] data) {
 		byte[] converted = new byte[3*8*4];
 		int[] r = new int[NR_OF_LED_HORIZONTAL*NR_OF_LED_VERTICAL];
 		int[] g = new int[NR_OF_LED_HORIZONTAL*NR_OF_LED_VERTICAL];
 		int[] b = new int[NR_OF_LED_HORIZONTAL*NR_OF_LED_VERTICAL];
 		int tmp;
 		int ofs=0;
 		int dst=0;
 
 		//step#1: split up r/g/b and apply gammatab
 		for (int y=0; y<NR_OF_LED_VERTICAL; y++) {
 			for (int x=0; x<NR_OF_LED_HORIZONTAL; x++) {
 				//one int contains the rgb color
 				tmp = data[ofs++];
 				
 				//the buffer on the rainbowduino takes GRB, not RGB				
 				g[dst] = gammaTab[(int) ((tmp>>16) & 255)];  //r
 				r[dst] = gammaTab[(int) ((tmp>>8)  & 255)];  //g
 				b[dst] = gammaTab[(int) ( tmp      & 255)];	 //b		
 				dst++;
 			}
 		}
 		//step#2: convert 8bit to 4bit
 		//Each color byte, aka two pixels side by side, gives you 4 bit brightness control, 
 		//first 4 bits for the left pixel and the last 4 for the right pixel. 
 		//-> this means a value from 0 (min) to 15 (max) is possible for each pixel 		
 		ofs=0;
 		dst=0;
 		for (int i=0; i<32;i++) {
 			//240 = 11110000 - delete the lower 4 bits, then add the (shr-ed) 2nd color
 			converted[00+dst] = (byte)(((r[ofs]&240) + (r[ofs+1]>>4))& 255); //r
 			converted[32+dst] = (byte)(((g[ofs]&240) + (g[ofs+1]>>4))& 255); //g
 			converted[64+dst] = (byte)(((b[ofs]&240) + (b[ofs+1]>>4))& 255); //b
 
 			ofs+=2;
 			dst++;
 		}
 
 		return converted;
 	}
 
 	/**
 	 * Sleep wrapper
 	 * @param ms
 	 */
 	private void sleep(int ms) {
 		try {
 			Thread.sleep(ms);
 		}
 		catch(InterruptedException e) {
 		}
 	}
 
 	/**
 	 * Scan I2C bus
 	 * 
 	 * @param _app
 	 * @return List of found i2c devices
 	 */
 	public static List<Integer> scanI2cBus(PApplet _app) {
 		Rainbowduino r=null;
 		
 		try {
 			r = new Rainbowduino(_app, new ArrayList<Integer>());
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		
 		r.i2cBusScan();
 		return r.scannedI2cDevices;
 	}
 }
