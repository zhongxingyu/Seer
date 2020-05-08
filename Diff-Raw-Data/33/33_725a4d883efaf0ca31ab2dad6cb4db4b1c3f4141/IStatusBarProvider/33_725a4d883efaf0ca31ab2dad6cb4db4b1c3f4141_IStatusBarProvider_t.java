 /*******************************************************************************
  * Copyright (c) 2008, 2009 Bug Labs, Inc.
  * All rights reserved.
  * 
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  *    - Redistributions of source code must retain the above copyright notice,
  *      this list of conditions and the following disclaimer.
  *    - Redistributions in binary form must reproduce the above copyright
  *      notice, this list of conditions and the following disclaimer in the
  *      documentation and/or other materials provided with the distribution.
  *    - Neither the name of Bug Labs, Inc. nor the names of its contributors may be
  *      used to endorse or promote products derived from this software without
  *      specific prior written permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
  * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
  * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
  * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
  * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
  * POSSIBILITY OF SUCH DAMAGE.
  *******************************************************************************/
 package com.buglabs.status;
 
import com.buglabs.device.IFramebufferDevice;

 /**
  * A Status Bar is a bitmap display that can show the user simple status
  * messages and icons. Clients may request resources from the Status Bar
  * Provider in either bitmap form or text (String) form. The position and scale
  * of the region are not controlled by the client. The client should not assume
  * that it's region is always visible at all times.
  * 
  * @author ken
  * 
  */
 public interface IStatusBarProvider {
 	/**
 	 * Get bitmap resource from Status Bar provider.
 	 * 
 	 * @param clientId
 	 *            ID of client
 	 * @param height
 	 *            required height for status bitmap
 	 * @param width
 	 *            required width for status bitmap
 	 * @return A key associated with requested region or <code>null</code> if
 	 *         no region can be allocated.
 	 */
 	public String acquireRegion(int height, int width);
 
 	/**
 	 * Get text resource from Status Bar provider.
 	 * 
 	 * @param clientId
 	 * @param length
 	 * @return A key associated with requested region or <code>null</code> if
 	 *         no region can be allocated.
 	 */
 	public String acquireRegion(int length);
 
 	/**
	 * This method is used to get access to the entire LCD device.  Only
	 * one client per JVM session may have access to display at one time.
	 * Client will have entire display until releaseDisplay() is called.
	 * During time of allocation no statusbar information will reach the display.
	 * 
	 * @return Key to display.
	 */
	public String acquireDisplay();
	
	/**
	 * Release screen from SB and menu use.
	 * @param key
	 */
	public void releaseDisplay(String key);
	
	/**
	 * @param key
	 * @return The framebuffer device for a client that has allocated entire display.
	 */
	public IFramebufferDevice getDisplay(String key);
	
	/**
 	 * Write a bitmap to the status screen. Must successfully aquire region
 	 * before calling this method.
 	 * 
 	 * @param clientId
 	 * @param bitmap
 	 */
 	public void write(String key, boolean[][] bitmap);
 
 	/**
 	 * Write a text message to the status screen. Must successfully aquire
 	 * region before calling this method.
 	 * 
 	 * @param clientId
 	 * @param message
 	 */
 	public void write(String key, String message);
 
	/**
	 * Deallocate a SB region
	 * @param key
	 */
 	public void releaseRegion(String key);
	
	
 }
