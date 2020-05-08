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
 package com.buglabs.bug.input.pub;
 
 import java.util.ArrayList;
 import java.util.ConcurrentModificationException;
 import java.util.Iterator;
 
 import org.osgi.service.log.LogService;
 
 import com.buglabs.bug.jni.common.FCNTL_H;
 import com.buglabs.bug.jni.input.InputDevice;
 import com.buglabs.bug.jni.input.InputEvent;
 import com.buglabs.device.ButtonEvent;
 import com.buglabs.device.IButtonEventListener;
 import com.buglabs.device.IButtonEventProvider;
 
 public class InputEventProvider extends Thread implements IButtonEventProvider {
 
 	private ArrayList listeners;
 	private final LogService log;
 	private String inputDevice;
 
 	public InputEventProvider(String inputDevice, LogService log) {
 		this.log = log;
 		listeners = new ArrayList();
 		this.inputDevice = inputDevice;
 	}
 
 	public void addListener(IButtonEventListener listener) {
 		synchronized (listeners) {
 			if (!listeners.contains(listener)) {
 				listeners.add(listener);
 			}
 		}
 	}
 
 	public void removeListener(IButtonEventListener listener) {
 		synchronized (listeners) {
 			listeners.remove(listener);
 		}
 	}
 
 	public void run() {
 		InputDevice dev = new InputDevice();
 		if (dev.open(inputDevice, FCNTL_H.O_RDWR) < 0) {
 			log.log(LogService.LOG_ERROR, "Unable to open input device: " + inputDevice);
 		}
 
 		while (!isInterrupted()) {
 			try {
 				InputEvent[] inputEvents = dev.readEvents();
 
 				synchronized (listeners) {
 					Iterator iter = listeners.iterator();
 
 					for (int i = 0; i < inputEvents.length; ++i) {
 						ButtonEvent b = new ButtonEvent(inputEvents[i].code, 0, inputEvents[i].code, convertButtonAction(inputEvents[i].value), this.getClass().toString());

 						while (iter.hasNext()) {
 							IButtonEventListener l = (IButtonEventListener) iter.next();
 							try {
 								l.buttonEvent(b);
 							} catch (Exception e) {
 								log.log(LogService.LOG_ERROR, "Button event client threw an exception.", e);
 							}
 						}
 					}
 				}
 			} catch (ConcurrentModificationException e) {
 				log.log(LogService.LOG_ERROR, "Concurrency issue", e);
 			}
 		}
 
 		dev.close();
 	}
 
 	private long convertButtonAction(long value) {
 		if (value == 1) {
 			return ButtonEvent.KEY_DOWN;
 		}
 
 		if (value == 0) {
 			return ButtonEvent.KEY_UP;
 		}
 
 		return value;
 	}
 
 	public void tearDown() {
 		interrupt();
 	}
 }
