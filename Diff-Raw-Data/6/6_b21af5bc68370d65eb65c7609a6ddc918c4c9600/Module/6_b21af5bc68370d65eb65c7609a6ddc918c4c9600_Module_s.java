 package com.dropoutdesign.ddf;
 
 import com.dropoutdesign.ddf.config.*;
 import com.dropoutdesign.ddf.module.*;
 import java.util.*;
 import java.awt.Rectangle;
 
 /**
  * Represents a DDF module, attached to this computer.
  */
 public class Module {
 
 	protected String address;
 	protected Rectangle bounds;
 	
 	protected ModuleConnection currentConnection = null;
 	
 	/**
 	 * Initialize a module from the supplied ModuleConfig descriptor.
 	 */
 	public Module(ModuleConfig config) {
 		address = config.getAddress();
 		bounds = config.getBounds();
 		if (bounds.width != 16 || bounds.height != 4) {
 			throw new IllegalArgumentException("Invalid module size: " + bounds.width 
 										+ "x" + bounds.height);
 		}
 	}
 	
 	/**
 	 * Return the address of this module.
 	 */
 	public String getAddress() {
 		return address;
 	}
 	
 	/**
 	 * Return the bounds of this module, in the dance floor coordinate space.
 	 */
 	public Rectangle getBounds() {
 		return bounds;
 	}
 	
 	/**
 	 * Return the connection status of this module.
 	 */
 	public boolean isConnected() {
 		return (currentConnection != null);
 	}
 	
 	/**
 	 * Return the ModuleConnection used to communicated with this module's hardware.
 	 */
 	public ModuleConnection getConnection() {
 		return currentConnection;
 	}
 	
 	/**
 	 * Connect to this module.
 	 * @throws UnkownConnectionTypeException the address of the module (specified in
 	 * the ModuleConfig) specifies an unkown protocol.
 	 * @throws ModuleIOException the connection could not be established.
 	 */
 	public void connect() throws UnknownConnectionTypeException, ModuleIOException {
 		currentConnection = ModuleConnection.open(address);
 		
 		System.out.println("Connected to module at " + address);
 		System.out.println("\tfirmware: "
 				+ Integer.toString(currentConnection.firmwareVersion(), 16));
 		System.out.println("\ti2c: "
 				+ Integer.toString(currentConnection.checkI2C(), 16));
 		
 		currentConnection.reset(); // send soft reset command to module
 	}
 
 	/**
 	 * Disconnect from this module.
 	 */
 	public void disconnect() {
		currentConnection.close();
		currentConnection = null;
 	}
 	
 	/**
 	 * Write a frame to this module.
 	 * This method takes a full DanceFloor frame as input, and uses this module's
 	 * <code>bounds</code> field to determine what pixels to send across.
 	 * @param frame a full DanceFloor frame.
 	 * @throws ModuleIOException the frame could not be sent.
 	 * @see com.dropoutdesign.ddf.DanceFloor#drawFrame
 	 */
 	public void writeFrame(byte[] frame) throws ModuleIOException {
 		
 		byte[] cmd = new byte[97];
 		cmd[0] = 0x10;
 		int curNib = 2;
 
 		int xm = bounds.x + bounds.width;
 		int ym = bounds.y + bounds.height;
 		
 		// Note the odd writing pattern...
 		for (int y = bounds.y; y < ym; y++) {
 			for (int c = 0; c < 3; c++) {	// Component
 				for (int x = bounds.x; x < xm; x++) {
 				
 					
 					// FIXME: KLUDGE KLUDGE KLUDGE
 					int whichByte = (y * 16 + x) * 3 + c;
 					int nib = ((frame[whichByte] >> 4) & 0xF) ^ 0xF; 
 						// flip because 0xF is off					
 					
 					if ((curNib % 2) == 0) {
 						cmd[(int)(curNib/2)] = (byte)(nib);
 					} else {
 						cmd[(int)(curNib/2)] |= (byte)(nib << 4);
 					}
 					curNib++;
 				}
 			}
 		}
 		currentConnection.sendCommand(cmd);
 	}
 }
