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
 package com.buglabs.bug.module.camera.pub;
 
 import java.awt.Rectangle;
 import java.io.InputStream;
 
 /**
  * An interface for a device that can return images.
  * 
  * @author kgilmer
  * 
  */
 public interface ICameraDevice {
	/**
	 * @return byte array representing image from camera in JPG format.
	 */
 	public byte[] getImage();
 
	/**
	 * @param sizeX preferred X size of image from camera.
	 * @param sizeY preferred Y size of image from camera.
	 * @param format preferred format of image from camera.
	 * @param highQuality quality setting for camera device.
	 * @return byte array of image from camera.
	 */
 	public byte[] getImage(int sizeX, int sizeY, int format, boolean highQuality);
 
	/**
	 * Initialize overlay from camera device on LCD with specific bounds.
	 * @param pbounds
	 * @return
	 */
 	public boolean initOverlay(Rectangle pbounds);
 
	/**
	 * Start overlay of image from camera to LCD screen.
	 * @return
	 */
 	public boolean startOverlay();
 
	/**
	 * Stop overlay.
	 * @return
	 */
 	public boolean stopOverlay();
 
	/**
	 * @return inputstream of bytes from Camera device.
	 */
 	public InputStream getImageInputStream();
 
	/**
	 * @return The default format images are presented in.
	 */
 	public String getFormat();
 }
