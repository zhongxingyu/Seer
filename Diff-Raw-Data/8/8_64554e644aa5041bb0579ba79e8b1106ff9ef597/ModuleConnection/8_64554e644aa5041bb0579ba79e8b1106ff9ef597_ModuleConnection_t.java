 package com.dropoutdesign.ddf.module;
 
 import java.lang.Thread;
 import java.util.concurrent.*;
 import java.io.*;
 import javax.comm.*;
 
 public class ModuleConnection extends Thread {
 	
 	private final String address;
 	private final OutputStream outStream;
 	private final InputStream inStream;
 	
 	public static final int MAX_QUEUE_SIZE = 10;
 	public static final int IO_TIMEOUT = 50;
 	
 	public static final int SERIAL_BAUD = 57600;
 	private SerialPort serialPort;
 	
 	private BlockingQueue<byte[]> cmdQueue;
 	private BlockingQueue<byte[]> respQueue;
 	
 	public ModuleConnection(String address, InputStream in, OutputStream out) {
 		super("ModuleConnection: " + address);
 		this.address = address;
 		inStream = in;
 		outStream = out;
 		
 		cmdQueue = new ArrayBlockingQueue<byte[]>(MAX_QUEUE_SIZE);
 		respQueue = new ArrayBlockingQueue<byte[]>(MAX_QUEUE_SIZE);
 	}
 	
 	/**
 	 * Open a connection given a string such as "/dev/DDF0" that
 	 * specifies the location of a serial-port connected module.
 	 * The connection must be closed using the close()
 	 * method when it is no longer needed.
 	 */
 	public static ModuleConnection open(String address) throws ModuleIOException{
 		return open(address, 2000);
 	}
 	
 	public static ModuleConnection open(String address, long timeout) throws ModuleIOException {
 			
 		CommPortIdentifier portID;
 		OutputStream outputStream;
 		InputStream inputStream;
 		SerialPort serialPort;
 
 		try {
 			portID = CommPortIdentifier.getPortIdentifier(address);
 			serialPort = (SerialPort)portID.open("Disco Dance Floor", (int)timeout);
 			
 			System.out.print("");	// Sketchy fix, makes params work
 			serialPort.setSerialPortParams(SERIAL_BAUD,
 						   SerialPort.DATABITS_8,
 						   SerialPort.STOPBITS_1,
 						   SerialPort.PARITY_NONE);
 						
 			System.out.print("");
 			serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
 
 			outputStream = serialPort.getOutputStream();
 			inputStream = serialPort.getInputStream();		
 
 		} catch (NoSuchPortException e) {
 			throw new ModuleIOException("No serial port at " + address, e);
 
 		} catch (PortInUseException e) {
 			throw new ModuleIOException("Serial port " + address + " in use.", e);
 
 		} catch (UnsupportedCommOperationException e) {
 			throw new ModuleIOException("Could not initialize port " + address, e);
 
 		} catch (IOException e) {
 			e.printStackTrace();
 			throw new ModuleIOException("I/O error on serial port " + address, e);
 		}
 
 		ModuleConnection mc = new ModuleConnection(address, inputStream, outputStream);
 		mc.serialPort = serialPort;
 		mc.start();
 		return mc;
 	}
 	
 	public void close() {
 		try {
 			interrupt();
 			inStream.close();
 			outStream.close();
 			serialPort.close();
 		} catch (IOException io) {
 			// ignore exception when closing
 		}
 	}
 	
 	public void run() {
 		while (true) {
 			try {
 				if (interrupted()) {
 					System.err.println("Thread module " + address + " interrupted, dying.");
 					return;
 				}
 			
 				byte[] cmd = null;
 				cmd = cmdQueue.take();
 			
 				try {
 					outStream.write(cmd);
 				} catch (IOException e) {
 					System.err.println("Write error, module " + address);
 					e.printStackTrace();
 				}
 			
 				int respLen = getNumBytesExpected(cmd[0]);
 				byte[] resp = new byte[respLen];
 			
 				int bytesRead = 0;
 				long readStart = System.currentTimeMillis();
 
 				while (bytesRead < respLen) {
 					int r = 0;
 					try {
 						r = inStream.read(resp, bytesRead, respLen-bytesRead);
 					} catch (IOException e) {
 						System.err.println("Read error, module " + address);
 						e.printStackTrace();
 						break;
 					}
 				
 					if (r == -1) {
 						System.err.println("Connection terminated, module " + address);
 						break;
 					} else {
 						bytesRead += r;
 					}
 				
 					long t = System.currentTimeMillis();
 					if ((t - readStart) > IO_TIMEOUT) {
 						System.err.println("Read timeout, module " + address);
 						break;
 					}
 				
 					sleep(5);
 				}
 			
 				respQueue.put(resp);
			
			} catch (InterruptedException e) {
				System.err.println("Thread module " + address + "interrupted, dying.");
				return;
 			}
 		}
 	}
 	
 	public void sendCommand(byte cmd[]) {
 		try {
 			cmdQueue.put(cmd);
 		} catch (InterruptedException e) {
 			System.err.println("Interrupted while enqueueing command.");
 		}
 	}
 
 	public void sendCommand(int cmd) throws ModuleIOException {
 		byte[] c = new byte[1];
 		c[0] = (byte)cmd;
 		sendCommand(c);
 	}
 	
 	public byte[] receiveResponse() {
 		byte[] resp = null;
 		try { resp = respQueue.take(); } catch (InterruptedException e) {
 			System.err.println("Interrupted while dequeueing response.");
 		}
 		return resp;
 	}
 	
 	public byte receiveResponseByte() {
 		byte[] resp = receiveResponse();
 		return resp[0];
 	}
 
 	public byte ping() throws ModuleIOException {
 		sendCommand(0x50);
 		return receiveResponseByte();
 	}
 
 	public byte blackout() throws ModuleIOException {
 		sendCommand(0x40);
 		return receiveResponseByte();
 	}
 
 	public byte reset() throws ModuleIOException {
 		sendCommand(0x60);
 		return receiveResponseByte();
 	}
 
 	public byte selfTest() throws ModuleIOException {
 		sendCommand(0x80);
 		return receiveResponseByte();
 	}
 
 	public byte firmwareVersion() throws ModuleIOException {
 		sendCommand(0x70);
 		byte[] resp = receiveResponse();
 		if (resp[0] != 0) { // for old firmware
 			return resp[0];
 		}
 		else {
 			return resp[1];	
 		}
 	}
 
 	public int checkI2C() throws ModuleIOException {
 		sendCommand(0x61);
 		byte[] resp = receiveResponse();
 		if (resp[0] != 0) { // for old firmware
 			return -1;
 		}
 		else {
 			return ((((int)resp[1])&0xFF)<<8) | (((int)resp[2])&0xFF);
 		}
 	}
 	
 	private int getNumBytesExpected(int cmd) {
 		switch (cmd) {
 		case 0x20: return 9;
 		case 0x30: return 9;
 		case 0x70: return 2;
 		case 0x61: return 3;
 		default: return 1;
 		}
 	}
 	
 	private boolean getUsesStatusByte(int cmd) {
 		//return (cmd & 0xF0) != 0x70;
 		return true;
 	}
 }
