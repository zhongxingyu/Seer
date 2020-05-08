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
 package com.buglabs.bug.module.lcd.accelerometer;
 
 import java.io.InputStream;
 
 import com.buglabs.bug.accelerometer.pub.AccelerometerSampleStream;
 import com.buglabs.bug.accelerometer.pub.IAccelerometerRawFeed;
 import com.buglabs.bug.accelerometer.pub.IAccelerometerSampleFeed;
 import com.buglabs.bug.module.lcd.Activator;
 import com.buglabs.util.LogServiceUtil;
 import com.buglabs.util.StreamMultiplexer;
 
 public class LCDAccelerometerInputStreamProvider extends StreamMultiplexer implements IAccelerometerSampleFeed, IAccelerometerRawFeed {
	private static final int BUFFER_SIZE = 6;
	private static final int PROCESS_DELAY = 50;
	private static final int READ_DELAY = 250;
	
 	public LCDAccelerometerInputStreamProvider(InputStream is) {
		super(is, BUFFER_SIZE, PROCESS_DELAY);
 		setName("LCDAccelerometer");
 		setLogService(LogServiceUtil.getLogService(Activator.getInstance().getBundleContext()));
 	}
 
 	public AccelerometerSampleStream getSampleInputStream() {
 		return new LCDAccelerometerSampleInputStream(getInputStream());
 	}
 }
