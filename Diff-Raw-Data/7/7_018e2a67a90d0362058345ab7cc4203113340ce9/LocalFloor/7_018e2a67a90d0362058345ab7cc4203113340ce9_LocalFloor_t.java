 package com.dropoutdesign.ddf;
 
 import com.dropoutdesign.ddf.config.*;
 import com.dropoutdesign.ddf.module.*;
 
 import java.util.*;
 import java.io.*;
 import java.awt.Rectangle;
 
 /**
  * A locally connected floor.
  * Allows inspection and control of a dance floor attached to this
  * physical machine, generally via usb-serial.
  */
 public class LocalFloor extends DanceFloor {
 	
 	private List<Module> modules;
 	private int numConnectedModules;
 	
 	private int width;
 	private int height;
 	
 	private int frameRate;
 		
 	/**
 	 * Create a floor described by the configuration xml file of the given name.
 	 * @param confFile the name of the file containing configuration data.
 	 * @throws IOException the file could not be read or is malformed.
 	 */
 	public LocalFloor(String confFile) throws IOException {
 		
 		this(DanceFloorConfig.readAll(confFile));
 	}
 	
 	/**
 	 * Create a floor described by the given configuration object.
 	 * The configuration data is used to specify the size, layout, and connection
 	 * of the floor's component modules.
 	 */
 	public LocalFloor(DanceFloorConfig config) {
 		
 		frameRate = config.framerate;
 		
 		modules = new ArrayList<Module>(config.modules.size());
 		for (ModuleConfig mc : config.modules) {
 			Module m = new Module(mc);
 		
 			Rectangle bounds = m.getBounds();
 			int x = bounds.x + bounds.width;
 			if (x > width)
 				width = x;
 			int y = bounds.y + bounds.height;
 			if (y > height)
 				height = y;
 			
 			modules.add(m);
 		}
 		System.out.println("Initialized floor: " + width + "x" + height);
 	}
 	
 	/**
 	 * Connect to the floor.  Will return sucessfully if at least one module is connected.
 	 * @throws ModuleIOException no modules could be connected.
 	 */
 	public void connect() throws ModuleIOException {
 		if (isConnected()) {
 			return;
 		}
 		for (Module m : modules) {
 			try {
 				m.connect();
 				numConnectedModules++;
 			} catch (Exception e) {
 				System.out.println("Failed to connect module: " + e);
 			}
 		}
 		if (numConnectedModules == 0) {
 			throw new ModuleIOException("Unable to connect any modules.");
 		}
 	}
 	
 	/**
 	 * Disconnect from the floor.
 	 */
 	public void disconnect() {
 		for (Module m : modules) {
 			m.disconnect();
 		}
 		numConnectedModules = 0;
 	}
 	
 	/**
 	 * Return the connection status of the floor.
 	 * @return true if at least one module is connected.
 	 */
 	public boolean isConnected() {
 		return (numConnectedModules > 0);
 	}
 
 	/**
 	 * Return the width of this floor, in pixels.
 	 */
 	public int getWidth() {
 		return width;
 	}
 	
 	/**
 	 * Return the height of this floor, in pixels.
 	 */
 	public int getHeight() {
 		return height;
 	}
 	
 	/**
 	 * Return the component modules of this floor.
 	 */
 	public List getModules() {
 		return modules;
 	}
 
 	/**
 	 * Return the native framerate of this floor.
 	 */
 	public int getFramerate() {
 		return frameRate;
 	}
 	
 	/**
 	 * Draw a frame onto the floor.
 	 * @param frame the frame to draw.
 	 * @see com.dropoutdesign.ddf.DanceFloor#drawFrame
 	 */
 	public void drawFrame(byte frame[]) throws ModuleIOException {
 		for (int i = 0; i < modules.size(); i++) {
 			Module m = modules.get(i);
 			if (m.isConnected()) {
 				m.writeFrame(frame);
				byte response = m.getConnection().readResponseByte();
				System.out.println("Module " + i + " reponse: " + response);
			} else {
				System.out.println("Module " + i + " not connected.");
 			}
 		}
 	}
 }
