 package com.buglabs.bug.module.lcd.pub;
 
 import java.io.IOException;
 
 import com.buglabs.module.IModuleControl;
 import com.buglabs.module.IModuleLEDController;
 
 public interface ILCDModuleControl extends IModuleControl, IModuleLEDController {
 	
 	/**
 	 * @param val Set's the intensity of the backlight 0-7.
 	 * 
 	 */
 	public int setBlackLight(int val) throws IOException;		// Set IOX backlight bits [2:0]
 	
 	/**
 	 * 
 	 * @return The stat of IOX. The 3 LSBs reprersent the state of the backlight. 
 	 */
 	public int getStatus() throws IOException;		// Get IOX state
 	
 	public int disable() throws IOException;	// Power down module
 	public int enable() throws IOException;	// Power up module	
 }
