 /*
 A nice wrapper class to control the Rainbowduino 
 
 (c) copyright 2009 by rngtng - Tobias Bielohlawek
 (c) copyright 2010/2011/2012 by Michael Vogt/neophob.com 
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
 
 package com.neophob.lpd6803;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import processing.core.PApplet;
 import processing.serial.Serial;
 
 import com.neophob.lpd6803.misc.ColorFormat;
 import com.neophob.lpd6803.misc.MD5;
 
 /**
  * library to communicate with an LPD6803 stripes via serial port<br>
  * <br><br>
  * part of the neorainbowduino library.
  *
  * @author Michael Vogt / neophob.com
  */
 public class Lpd6803 {
 		
 	/** The log. */
 	private static final Logger LOG = Logger.getLogger(Lpd6803.class.getName());
 
 	/** internal lib version. */
 	public static final String VERSION = "1.0";
 
 	/** The Constant START_OF_CMD. */
 	private static final byte START_OF_CMD = 0x01;
 	
 	/** The Constant CMD_SENDFRAME. */
 	private static final byte CMD_SENDFRAME = 0x03;
 	
 	/** The Constant CMD_PING. */
 	private static final byte CMD_PING = 0x04;
 
 	/** The Constant START_OF_DATA. */
 	private static final byte START_OF_DATA = 0x10;
 	
 	/** The Constant END_OF_DATA. */
 	private static final byte END_OF_DATA = 0x20;
 
 	/** The app. */
 	private PApplet app;
 
 	/** The baud. */
 	private int baud = 115200;
 	
 	/** The port. */
 	private Serial port;
 	
 	/** The arduino heartbeat. */
 	private long arduinoHeartbeat;
 	
 	/** The ack errors. */
 	private long ackErrors = 0;
 	
 	/** The arduino buffer size. */
 	private int arduinoBufferSize;
 	
 	//logical errors reported by arduino, TODO: rename to lastErrorCode
 	/** The arduino last error. */
 	private int arduinoLastError;
 	
 	//connection errors to arduino, TODO: use it!
 	/** The connection error counter. */
 	private int connectionErrorCounter;
 		
 	/** map to store checksum of image. */
 	private Map<Byte, String> lastDataMap;
 	
 	/** internal buffer, how many pixels are controlled */
   	private int pixelBuffer;
     
 
 	/**
 	 * Create a new instance to communicate with the lpd6803 device.
 	 *
 	 * @param app the app
 	 * @throws NoSerialPortFoundException the no serial port found exception
 	 */
 	public Lpd6803(PApplet app, int pixelBuffer) throws NoSerialPortFoundException {
 		this(app, null, 0, pixelBuffer);
 	}
 
 	/**
 	 * Create a new instance to communicate with the lpd6803 device.
 	 *
 	 * @param app the app
 	 * @param baud the baud
 	 * @throws NoSerialPortFoundException the no serial port found exception
 	 */
 	public Lpd6803(PApplet app, int baud, int pixelBuffer) throws NoSerialPortFoundException {
 		this(app, null, baud, pixelBuffer);
 	}
 
 	/**
 	 * Create a new instance to communicate with the lpd6803 device.
 	 *
 	 * @param app the app
 	 * @param portName the port name
 	 * @throws NoSerialPortFoundException the no serial port found exception
 	 */
 	public Lpd6803(PApplet app, String portName, int pixelBuffer) throws NoSerialPortFoundException {
 		this(app, portName, 0, pixelBuffer);
 	}
 
 
 	/**
 	 * Create a new instance to communicate with the lpd6803 device.
 	 *
 	 * @param _app the _app
 	 * @param portName the port name
 	 * @param baud the baud
 	 * @throws NoSerialPortFoundException the no serial port found exception
 	 */
 	public Lpd6803(PApplet app, String portName, int baud, int pixelBuffer) throws NoSerialPortFoundException {
 		
 		LOG.log(Level.INFO,	"Initialize LPD6803 lib v{0}", VERSION);
 		
 		this.app = app;
 		app.registerDispose(this);
 		
 		this.pixelBuffer = pixelBuffer;
 		lastDataMap = new HashMap<Byte, String>();
 		
 		String serialPortName="";
 		if(baud > 0) {
 			this.baud = baud;
 		}
 		
 		if (portName!=null && !portName.trim().isEmpty()) {
 			//open specific port
 			LOG.log(Level.INFO,	"open port: {0}", portName);
 			serialPortName = portName;
 			openPort(portName);
 		} else {
 			//try to find the port
 			String[] ports = Serial.list();
 			for (int i=0; port==null && i<ports.length; i++) {
 				LOG.log(Level.INFO,	"open port: {0}", ports[i]);
 				try {
 					serialPortName = ports[i];
 					openPort(ports[i]);
 				//catch all, there are multiple exception to catch (NoSerialPortFoundException, PortInUseException...)
 				} catch (Exception e) {
 					// search next port...
 				}
 			}
 		}
 				
 		if (port==null) {
 			throw new NoSerialPortFoundException("Error: no serial port found!");
 		}
 		
 		LOG.log(Level.INFO,	"found serial port: "+serialPortName);
 	}
 
 
 	/**
 	 * clean up library.
 	 */
 	public void dispose() {
 		if (connected()) {
 			port.stop();
 		}
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
 	 * return connection state of lib.
 	 *
 	 * @return whether a lpd6803 device is connected
 	 */
 	public boolean connected() {
 		return (port != null);
 	}	
 
 	
 
 	/**
 	 * Open serial port with given name. Send ping to check if port is working.
 	 * If not port is closed and set back to null
 	 *
 	 * @param portName the port name
 	 * @throws NoSerialPortFoundException the no serial port found exception
 	 */
 	private void openPort(String portName) throws NoSerialPortFoundException {
 		if (portName == null) {
 			return;
 		}
 		try {
 			port = new Serial(app, portName, this.baud);
 			sleep(1500); //give it time to initialize
 			if (ping()) {
 				return;
 			}
 			LOG.log(Level.WARNING, "No response from port {0}", portName);
 			if (port != null) {
 				port.stop();        					
 			}
 			port = null;
 			throw new NoSerialPortFoundException("No response from port "+portName);
 		} catch (Exception e) {	
 			LOG.log(Level.WARNING, "Failed to open port {0}: {1}", new Object[] {portName, e});
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
 		 *  1   <i2c_addr>/<offset>
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
 	 * wrapper class to send a RGB image to the lpd6803 device.
 	 * the rgb image gets converted to the lpd6803 device compatible
 	 * "image format"
 	 *
 	 * @param ofs the image ofs
 	 * @param data rgb data (int[64], each int contains one RGB pixel)
 	 * @param colorFormat the color format
 	 * @return true if send was successful
 	 */
 	public boolean sendRgbFrame(byte ofs, int[] data, ColorFormat colorFormat) {
 		return sendFrame(ofs, convertBufferTo15bit(data, colorFormat));
 	}
 
 
 	
 	/**
 	 * get md5 hash out of an image. used to check if the image changed
 	 *
 	 * @param ofs the ofs
 	 * @param data the data
 	 * @return true if send was successful
 	 */
 	private boolean didFrameChange(byte ofs, byte data[]) {
 		String s = MD5.asHex(data);
 		
 		if (!lastDataMap.containsKey(ofs)) {
 			//first run
 			lastDataMap.put(ofs, s);
 			return true;
 		}
 		
 		if (lastDataMap.get(ofs).equals(s)) {
 			//last frame was equal current frame, do not send it!
 			//log.log(Level.INFO, "do not send frame to {0}", addr);
 			return false;
 		}
 		//update new hash
 		lastDataMap.put(ofs, s);
 		return true;
 	}
 	
 	/**
 	 * send a frame to the active lpd6803 device.
 	 *
 	 * @param ofs - the offset 
 	 * @param data byte[3*8*4]
 	 * @return true if send was successful
 	 * @throws IllegalArgumentException the illegal argument exception
 	 */
 	public boolean sendFrame(byte ofs, byte data[]) throws IllegalArgumentException {		
 //		if (data.length!=64) {
 //			throw new IllegalArgumentException("data lenght must be 128 bytes!");
 //		}
LOG.log(Level.WARNING, "send {0} bytes, announced: {1}", new Object[] {data.length, pixelBuffer});
 		boolean returnValue = false;
 		byte cmdfull[] = new byte[data.length+7];
 		
 		cmdfull[0] = START_OF_CMD;
 		cmdfull[1] = ofs;
 		cmdfull[2] = (byte)data.length;
 		cmdfull[3] = CMD_SENDFRAME;
 		cmdfull[4] = START_OF_DATA;		
 		cmdfull[data.length+5] = END_OF_DATA;
 
 	    System.arraycopy(data, 0, cmdfull, 5, data.length);
 	      
 		//send frame one
 		if (didFrameChange(ofs, cmdfull)) {
 			if (sendSerialData(cmdfull)) {
 				returnValue=true;
 			} else {
 				//in case of an error, make sure we send it the next time!
 				lastDataMap.put(ofs, "");
 			}
 		}
 		
 		return returnValue;
 	}
 	
 
 	/**
 	 * Send serial data.
 	 *
 	 * @param cmdfull the cmdfull
 	 * @return true, if successful
 	 */
 	private boolean sendSerialData(byte cmdfull[]) {
 		try {
 			writeSerialData(cmdfull);
 			if (waitForAck()) {
 				//frame was send successful
 				return true;
 			}
 		} catch (Exception e) {
 			LOG.log(Level.WARNING, "sending serial data failed: {0}", e);
 		}
 		return false;
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
 		return arduinoLastError;
 	}
 
 	/**
 	 * return the serial buffer size of the arduino
 	 * 
 	 * the buffer is by default 128 bytes - if the buffer is most of the
 	 * time almost full (>110 bytes) you probabely send too much serial data.
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
 	 * how may times the serial response was missing / invalid.
 	 *
 	 * @return the ack errors
 	 */
 	public long getAckErrors() {
 		return ackErrors;
 	}
 
 	/**
 	 * send the data to the serial port.
 	 *
 	 * @param cmdfull the cmdfull
 	 * @throws SerialPortException the serial port exception
 	 */
 	private synchronized void writeSerialData(byte[] cmdfull) throws SerialPortException {
 		//TODO handle the 128 byte buffer limit!
 		if (port==null) {
 			throw new SerialPortException("port is not ready!");
 		}
 		
 		//log.log(Level.INFO, "Serial Wire Size: {0}", cmdfull.length);
 
 		try {
 			port.output.write(cmdfull);
 			//port.output.flush();
 			//DO NOT flush the buffer... hmm not sure about this, processing flush also
 			//and i discovered strange "hangs"...
 		} catch (Exception e) {
 			LOG.log(Level.INFO, "Error sending serial data!", e);
 			connectionErrorCounter++;
 			throw new SerialPortException("cannot send serial data, errorNr: "+connectionErrorCounter+", Error: "+e);
 		}		
 	}
 	
 	/**
 	 * read data from serial port, wait for ACK.
 	 *
 	 * @return true if ack received, false if not
 	 */
 	private synchronized boolean waitForAck() {		
 		//TODO some more tuning is needed here.
 		long start = System.currentTimeMillis();
 		int timeout=20;//8; //wait up to 50ms
 		//log.log(Level.INFO, "wait for ack");
 		while (timeout > 0 && port.available() < 2) {
 			sleep(4); //in ms
 			timeout--;
 		}
 
 		if (timeout == 0 && port.available() < 2) {
 			LOG.log(Level.INFO, "#### No serial reply, duration: {0}ms ###", System.currentTimeMillis()-start);
 			ackErrors++;
 			return false;
 		}
 		//TODO: next method is not very speed/memory efficient!
 		byte[] msg = port.readBytes();
 /*		//LOG.log(Level.INFO, "got ACK! data length: {0}", msg.length);
 		for (byte b:msg)
 			System.out.print(Integer.toHexString(b)+' ');
 		System.out.println();
 		//INFO: MEEE [0, 0, 65, 67, 75, 0, 0]
 */		for (int i=0; i<msg.length-1; i++) {
 			if (msg[i]== 'A' && msg[i+1]== 'K') {
 				try {
 					this.arduinoBufferSize = msg[i+2];
 					this.arduinoLastError = msg[i+3];
 					if (this.arduinoLastError!=0) {
						LOG.log(Level.INFO, "Last Errorcode: {0}", this.arduinoLastError);
 					}
 				} catch (Exception e) {
 					// we failed to update statistics...
 				}
 				this.arduinoHeartbeat = System.currentTimeMillis();
 				if (this.arduinoLastError==0) {
 					return true;					
 				}
 				ackErrors++;
 				return false;
 				//TODO inconsisten logging!
 			}			
 		}
 		
 		String s="";
 		for (byte b: msg) {
 			s+=(char)b;
 		}
 		LOG.log(Level.INFO, "Invalid serial data <{0}>, duration: {1}ms", 
 				new String[] {s, ""+(System.currentTimeMillis()-start)});
 		ackErrors++;
 		return false;		
 	}
 
 
 
 
 	/**
 	 * Sleep wrapper.
 	 *
 	 * @param ms the ms
 	 */
 	private void sleep(int ms) {
 		try {
 			Thread.sleep(ms);
 		}
 		catch(InterruptedException e) {
 		}
 	}
 	
 	/**
 	 * Convert buffer to15bit.
 	 *
 	 * @param data the data
 	 * @param colorFormat the color format
 	 * @return the byte[]
 	 * @throws IllegalArgumentException the illegal argument exception
 	 */
 	public byte[] convertBufferTo15bit(int[] data, ColorFormat colorFormat) throws IllegalArgumentException {
 		if (data.length!=pixelBuffer) {
 			throw new IllegalArgumentException("data lenght must be "+pixelBuffer+" bytes!");
 		}
 
 		int[] r = new int[pixelBuffer];
 		int[] g = new int[pixelBuffer];
 		int[] b = new int[pixelBuffer];
 		int tmp;
 		int ofs=0;
 
 		//step#1: split up r/g/b 
 		for (int n=0; n<pixelBuffer; n++) {
 			//one int contains the rgb color
 			tmp = data[ofs];
 
 			switch (colorFormat) {
 			case RGB:
 				r[ofs] = (int) ((tmp>>16) & 255);
 				g[ofs] = (int) ((tmp>>8)  & 255);
 				b[ofs] = (int) ( tmp      & 255);		
 				
 				break;
 			case RBG:
 				r[ofs] = (int) ((tmp>>16) & 255);
 				b[ofs] = (int) ((tmp>>8)  & 255);
 				g[ofs] = (int) ( tmp      & 255);		
 				
 				break;
 			}
 			ofs++;
 		}
 
 		return convertTo15Bit(r,g,b);
 	}
 
 	/**
 	 * Convert to15 bit.
 	 *
 	 * @param r the r
 	 * @param g the g
 	 * @param b the b
 	 * @return the byte[]
 	 */
 	private static byte[] convertTo15Bit(int[] r, int[] g, int[] b) {
 		int dst=0;
 		//send one rgb color in two bytes
 		byte[] converted = new byte[r.length*2];
 		//convert to 24bpp to 15(16)bpp 
 		//output format: RRRRRGGG GGGBBBBB (64x)
 		for (int i=0; i<r.length;i++) {
 			byte b1 = (byte)(r[i]>>3);
 			byte b2 = (byte)(g[i]>>3);
 			byte b3 = (byte)(b[i]>>3);
 
 			converted[dst++] = (byte)((b1<<2) | (b2>>3));
 			converted[dst++] = (byte)(((b2&7)<<5) | b3);
 		}
 		
 		return converted;
 	}
 
 
 
 }
