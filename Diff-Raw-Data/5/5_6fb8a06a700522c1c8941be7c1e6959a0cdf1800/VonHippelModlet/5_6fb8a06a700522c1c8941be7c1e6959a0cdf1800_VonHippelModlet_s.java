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
 
 import gnu.io.CommPort;
 import gnu.io.CommPortIdentifier;
 import gnu.io.RXTXPort;
 import gnu.io.SerialPort;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Properties;
 
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.ServiceRegistration;
 import org.osgi.service.log.LogService;
 import org.osgi.util.tracker.ServiceTracker;
 
 import com.buglabs.bug.jni.common.CharDeviceUtils;
 import com.buglabs.bug.jni.vonhippel.VonHippel;
 import com.buglabs.bug.module.pub.BMIModuleProperties;
 import com.buglabs.bug.module.pub.IModlet;
 import com.buglabs.bug.module.vonhippel.pub.IVonHippelModuleControl;
 import com.buglabs.bug.module.vonhippel.pub.VonHippelWS;
 import com.buglabs.module.IModuleControl;
 import com.buglabs.module.IModuleLEDController;
 import com.buglabs.module.IModuleProperty;
 import com.buglabs.module.ModuleProperty;
 import com.buglabs.util.LogServiceUtil;
 import com.buglabs.util.trackers.PublicWSAdminTracker;
 
 public class VonHippelModlet implements IModlet, IModuleControl {
 
 	private BundleContext context;
 
 	private boolean deviceOn = true;
 
 	private int slotId;
 
 	private final String moduleId;
 
 	private ServiceRegistration moduleRef;
 	private ServiceTracker wsMotionTracker, wsAccTracker;
 
 	protected static final String PROPERTY_MODULE_NAME = "moduleName";
 
 	public static final String MODULE_ID = "0007";
 
 	private final String moduleName;
 
 	private VonHippel vhDevice;
 
 	private ServiceRegistration vhModuleRef;
 
 	private VonHippelModuleControl vhc;
 
 	private ServiceRegistration vhSerialRef;
 
 	private ServiceRegistration vhLedRef;
 
 	private boolean suspended;
 
 	private LogService logService;
 
 	private final BMIModuleProperties properties;
 
 	private RXTXPort serialPort;
 
 	private String devNode;
 
 	public VonHippelModlet(BundleContext context, int slotId, String moduleId, String moduleName) {
 		this.context = context;
 		this.slotId = slotId;
 		this.moduleName = moduleName;
 		this.moduleId = moduleId;
 		this.logService = LogServiceUtil.getLogService(context);
 		this.properties = null;
 	}
 
 	public VonHippelModlet(BundleContext context, int slotId, String moduleId, String moduleName, BMIModuleProperties properties) {
 		this.context = context;
 		this.slotId = slotId;
 		this.moduleName = moduleName;
 		this.moduleId = moduleId;
 		this.properties = properties;
 		this.logService = LogServiceUtil.getLogService(context);
 	}
 
 	public void start() throws Exception {
 		Properties modProperties = createBasicServiceProperties();
 		modProperties.put("Power State", suspended ? "Suspended": "Active");
 		moduleRef = context.registerService(IModuleControl.class.getName(), this, modProperties);
 		vhModuleRef = context.registerService(IVonHippelModuleControl.class.getName(), vhc, createBasicServiceProperties());
 		vhSerialRef = context.registerService(RXTXPort.class.getName(),serialPort , createBasicServiceProperties());
 		vhLedRef =context.registerService(IModuleLEDController.class.getName(), vhc, createBasicServiceProperties());
 		VonHippelWS vhWS = new VonHippelWS(vhc);
 		wsMotionTracker = PublicWSAdminTracker.createTracker(context, vhWS);
 	}
 
 	public void stop() throws Exception {
 		//close any open resources
 
 		
 		if (wsMotionTracker != null) {
 			wsMotionTracker.close();
 		}
 
 		if (vhLedRef != null) {
 			vhLedRef.unregister();
 		}
 		
 		if (wsAccTracker != null) {
 			wsAccTracker.close();
 		}
 		if (moduleRef != null) {
 			moduleRef.unregister();
 		}
 
 		if (vhModuleRef != null) {
 			vhModuleRef.unregister();
 		}
 		
 		if (vhSerialRef != null) {
 			vhSerialRef.unregister();
 		}
 		
 	}
 
 	private Properties createBasicServiceProperties() {
 		Properties p = new Properties();
 		p.put("Provider", this.getClass().getName());
 		p.put("Slot", Integer.toString(slotId));
 
 		if (properties != null) {
 			if (properties.getDescription() != null) {
 				p.put("ModuleDescription", properties.getDescription());
 			}
 			if (properties.getSerial_num() != null) {
 				p.put("ModuleSN", properties.getSerial_num());
 			}
 
 			p.put("ModuleVendorID", "" + properties.getVendor());
 
 			p.put("ModuleRevision", "" + properties.getRevision());
 
 		}
 		
 		return p;
 	}
 
 	private void updateIModuleControlProperties(){
 		if (moduleRef!=null){
 			Properties modProperties = createBasicServiceProperties();
 			modProperties.put("Power State", suspended ? "Suspended": "Active");
 			moduleRef.setProperties(modProperties);
 		}
 	}
 
 	public List getModuleProperties() {
 		List mprops = new ArrayList();
 
 		mprops.add(new ModuleProperty(PROPERTY_MODULE_NAME, getModuleName()));
 		mprops.add(new ModuleProperty("Slot", "" + slotId));
 		mprops.add(new ModuleProperty("State", Boolean.toString(deviceOn), "Boolean", true));
 		mprops.add(new ModuleProperty("Power State", suspended ? "Suspended": "Active", "String", true));
 		
 		if (properties != null) {
 			mprops.add(new ModuleProperty("Module Description", properties.getDescription()));
 			mprops.add(new ModuleProperty("Module SN", properties.getSerial_num()));
 			mprops.add(new ModuleProperty("Module Vendor ID", "" + properties.getVendor()));
 			mprops.add(new ModuleProperty("Module Revision", "" + properties.getRevision()));
 		}
 		
 		return mprops;
 	}
 
 	public boolean setModuleProperty(IModuleProperty property) {
 		if (!property.isMutable()) {
 			return false;
 		}
 		if (property.getName().equals("State")) {
 			deviceOn = Boolean.valueOf((String) property.getValue()).booleanValue();
 			return true;
 		}
 		if (property.getName().equals("Power State")) {
 			if (((String) property.getValue()).equals("Suspend")){
 				try{
 				suspend();
 				}
 			 catch (IOException e) {
 				 LogServiceUtil.logBundleException(logService, e.getMessage(), e);
 			}
 			}
 			else if (((String) property.getValue()).equals("Resume")){
 				
 				try {
 					resume();
 				} catch (IOException e) {
 					this.logService = LogServiceUtil.getLogService(context);
 				}
 			}
 			
 				
 			
 		}
 		
 		return false;
 	}
 
 	public String getModuleName() {
 		return moduleName;
 	}
 
 	public String getModuleId() {
 		return moduleId;
 	}
 
 	public int getSlotId() {
 		return slotId;
 	}
 
 	public int resume() throws IOException {
 		int result = -1;
 
 		result = vhDevice.ioctl_BMI_VH_RESUME();
 
 		if (result < 0) {
 			throw new IOException("ioctl BMI_VH_RESUME failed");
 		}
 		suspended = false;
 		updateIModuleControlProperties();
 		return result;
 	}
 	
 
 	public int suspend() throws IOException {
 		int result = -1;
 
 		result = vhDevice.ioctl_BMI_VH_SUSPEND();
 
 		if (result < 0) {
 			throw new IOException("ioctl BMI_VH_SUSPEND failed");
 		}
 		suspended = true;
 		updateIModuleControlProperties();
 		return result;
 	}
 
 	public void setup() throws Exception {
 		int slot = slotId + 1;
 		String devnode_vh = "/dev/bmi_vh_control_m" + slot;
 		vhDevice = new VonHippel();
 		CharDeviceUtils.openDeviceWithRetry(vhDevice, devnode_vh, 2);
 		vhc = new VonHippelModuleControl(vhDevice, slotId);
 		
         
         
 		try{
 		//this may have to eventually change if we want to use other names (which i think is a good idea)
 		this.devNode = "/dev/ttyBMI" + slotId;
 		
 		// initialize the serial port
 		CommPortIdentifier portIdentifier = CommPortIdentifier
 				.getPortIdentifier(devNode);
 		CommPort commPort = portIdentifier
				.open(this.getClass().getName(), 2000);
 		serialPort = (RXTXPort) commPort;
 		serialPort.setSerialPortParams(38400, SerialPort.DATABITS_8,
 				SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
 		}
 		catch(Exception e){
 			e.printStackTrace();
 		}
 
 	}
 
 }
