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
 package com.buglabs.bug.module.vonhippel;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 
 import javax.microedition.io.CommConnection;
 import javax.microedition.io.Connector;
 
 import com.buglabs.bug.jni.vonhippel.VonHippel;
 import com.buglabs.bug.module.vonhippel.pub.IVonHippelModuleControl;
 
public class VonHippelModuleControl implements IVonHippelModuleControl {
 
 
 	private VonHippel vhDevice;
 	private int slotId;
 	private CommConnection cc;
 
 	public VonHippelModuleControl(VonHippel vh, int slotId) {
 		vhDevice = vh;
 		try {
 			cc = (CommConnection) Connector.open("comm:/dev/ttymxc/" + slotId + ";baudrate=9600;bitsperchar=8;stopbits=1;parity=none;autocts=off;autorts=off;blocking=off",
 					Connector.READ_WRITE, true);
 		} catch (IOException e) {
 			System.err.println("VonHippelModuleControl: Error opening serial connection...");
 			e.printStackTrace();
 		}
 	}
 
 	public int LEDGreenOff() throws IOException {
 		if (vhDevice != null) {
 			return vhDevice.ioctl_BMI_VH_GLEDOFF();
 		}
 		return -1;
 	}
 
 	public int LEDGreenOn() throws IOException {
 		if (vhDevice != null) {
 			return vhDevice.ioctl_BMI_VH_GLEDON();
 		}
 		return -1;
 	}
 
 	public int LEDRedOff() throws IOException {
 		if (vhDevice != null) {
 			return vhDevice.ioctl_BMI_VH_RLEDOFF();
 		}
 		return -1;
 	}
 
 	public int LEDRedOn() throws IOException {
 		if (vhDevice != null) {
 			return vhDevice.ioctl_BMI_VH_RLEDON();
 		}
 		return -1;
 	}
 
 	public void clearGPIO(int pin) throws IOException {
 		if (vhDevice != null) {
 			vhDevice.ioctl_BMI_VH_CLRGPIO(pin);
 		}
 
 	}
 
 	public void clearIOX(int pin) throws IOException {
 		if (vhDevice != null) {
 			vhDevice.ioctl_BMI_VH_CLRIOX(pin);
 		}
 
 	}
 
 	public void doADC() throws IOException {
 		//
 		throw new IOException("VonHippelModlet.doADC() is not yet implemented");
 
 	}
 
 	public void doDAC() throws IOException {
 		throw new IOException("VonHippelModlet.doDAC() is not yet implemented");
 
 	}
 
 	public int getRDACResistance() throws IOException {
 		throw new IOException("VonHippelModlet.getRDACResistance() is not yet implemented");
 	}
 
 	public int getStatus() throws IOException {
 		if (vhDevice != null) {
 			return vhDevice.ioctl_BMI_VH_GETSTAT();
 		}
 		return -1;
 	}
 
 	public void makeGPIOIn(int pin) throws IOException {
 		if (vhDevice != null) {
 			vhDevice.ioctl_BMI_VH_MKGPIO_IN(pin);
 		}
 	}
 
 	public void makeGPIOOut(int pin) throws IOException {
 		if (vhDevice != null) {
 			vhDevice.ioctl_BMI_VH_MKGPIO_OUT(pin);
 		}
 
 	}
 
 	public void makeIOXIn(int pin) throws IOException {
 		if (vhDevice != null) {
 			vhDevice.ioctl_BMI_VH_MKIOX_IN(pin);
 		}
 
 	}
 
 	public void makeIOXOut(int pin) throws IOException {
 		if (vhDevice != null) {
 			vhDevice.ioctl_BMI_VH_MKIOX_OUT(pin);
 		}
 
 	}
 
 	public int readADC() throws IOException {
 		throw new IOException("VonHippelModlet.readADC() is not yet implemented");
 	}
 
 	public void readDAC() throws IOException {
 		throw new IOException("VonHippelModlet.readDAC() is not yet implemented");
 
 	}
 
 	public void setGPIO(int pin) throws IOException {
 		if (vhDevice != null) {
 			vhDevice.ioctl_BMI_VH_SETGPIO(pin);
 		}
 
 	}
 
 	public void setIOX(int pin) throws IOException {
 		if (vhDevice != null) {
 			vhDevice.ioctl_BMI_VH_SETIOX(pin);
 		}
 
 	}
 
 	public void setRDACResistance(int resistance) throws IOException {
 		throw new IOException("VonHippelModlet.setRDACResistance(int resistance) is not yet implemented");
 	}
 
 	public int setLEDGreen(boolean state) throws IOException {
 		if (state) {
 			return LEDGreenOn();
 		} else
 			return LEDGreenOff();
 	}
 
 	public int setLEDRed(boolean state) throws IOException {
 		if (state) {
 			return LEDRedOn();
 		} else
 			return LEDRedOff();
 
 	}
 
 	public InputStream getRS232InputStream() {
 		try {
 			return (cc.openInputStream());
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
 		return null;
 	}
 
 	public OutputStream getRS232OutputStream() {
 		try {
 			return (cc.openOutputStream());
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
 		return null;
 	}
 
 }
