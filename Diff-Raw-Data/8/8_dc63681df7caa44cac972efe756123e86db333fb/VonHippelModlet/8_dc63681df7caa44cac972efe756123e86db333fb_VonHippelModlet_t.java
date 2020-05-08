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
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Properties;
 
 import javax.microedition.io.CommConnection;
 
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.ServiceRegistration;
 import org.osgi.util.tracker.ServiceTracker;
 
 import com.buglabs.bug.jni.common.CharDeviceUtils;
 import com.buglabs.bug.jni.vonhippel.VonHippel;
 import com.buglabs.bug.menu.pub.StatusBarUtils;
 import com.buglabs.bug.module.pub.IModlet;
 import com.buglabs.bug.module.vonhippel.pub.IVonHippelModuleControl;
 import com.buglabs.bug.module.vonhippel.pub.IVonHippelSerialPort;
 import com.buglabs.bug.module.vonhippel.pub.VonHippelWS;
 import com.buglabs.module.IModuleControl;
 import com.buglabs.module.IModuleLEDController;
 import com.buglabs.module.IModuleProperty;
 import com.buglabs.module.ModuleProperty;
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
 
 	private String regionKey;
 
 	private ServiceRegistration vhModuleRef;
 
 	private CommConnection cc;
 
 	private VonHippelModuleControl vhc;
 
 	private ServiceRegistration vhSerialRef;
 
 	private ServiceRegistration vhLedRef;
 
 	private static boolean icon[][] = { { false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false },
 			{ false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false },
 			{ false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false },
 			{ false, false, true, true, true, true, true, true, true, true, true, true, true, true, false, false },
 			{ false, true, false, false, false, false, false, false, false, false, false, false, false, false, true, false },
 			{ false, true, false, true, false, true, false, true, false, true, false, true, false, true, true, false },
 			{ false, true, true, false, true, false, true, false, true, false, true, false, true, false, true, false },
 			{ false, true, false, false, false, false, false, false, false, false, false, false, false, false, true, false },
 			{ false, true, false, true, false, false, false, true, false, true, false, false, true, false, true, false },
 			{ false, true, false, true, false, false, false, true, false, true, false, false, true, false, true, false },
 			{ false, true, false, true, false, false, false, true, false, true, false, false, true, false, true, false },
 			{ false, true, false, true, false, false, false, true, false, true, true, true, true, false, true, false },
 			{ false, true, false, false, true, false, true, false, false, true, false, false, true, false, true, false },
 			{ false, true, false, false, false, true, false, false, false, true, false, false, true, false, true, false },
 			{ false, true, false, false, false, false, false, false, false, false, false, false, false, false, true, false },
 			{ false, true, true, true, true, true, true, true, true, true, true, true, true, true, true, false },
 			{ false, true, true, true, true, true, true, true, true, true, true, true, true, true, true, false },
 			{ false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false },
 			{ false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false },
 			{ false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false } };
 
 	public VonHippelModlet(BundleContext context, int slotId, String moduleId, String moduleName) {
 		this.context = context;
 		this.slotId = slotId;
 		this.moduleName = moduleName;
 		this.moduleId = moduleId;
 
 	}
 
 	public void start() throws Exception {
 		moduleRef = context.registerService(IModuleControl.class.getName(), this, createBasicServiceProperties());
 		vhModuleRef = context.registerService(IVonHippelModuleControl.class.getName(), vhc, createBasicServiceProperties());
 		vhSerialRef = context.registerService(IVonHippelSerialPort.class.getName(), vhc, createBasicServiceProperties());
 		vhLedRef =context.registerService(IModuleLEDController.class.getName(), vhc, createBasicServiceProperties());
 		VonHippelWS vhWS = new VonHippelWS(vhc);
 		wsMotionTracker = PublicWSAdminTracker.createTracker(context, vhWS);
 		regionKey = StatusBarUtils.displayImage(context, icon, this.getModuleName());
 	}
 
 	public void stop() throws Exception {
 		StatusBarUtils.releaseRegion(context, regionKey);
 		
 		//close any open resources
 		if (vhc != null) {
 			vhc.dispose();
 		}
 		
 		if (wsMotionTracker != null) {
 			wsMotionTracker.close();
 		}
 
 		if (vhLedRef != null) {
 			vhLedRef.unregister();
 		}
 		
 		if (wsAccTracker != null) {
 			wsAccTracker.close();
 		}
 		// TODO: Throw exception at some point if we encounter a failure
 		if (moduleRef != null) {
 			moduleRef.unregister();
 		}
 
 		if (vhModuleRef != null) {
 			vhModuleRef.unregister();
 		}
 		
 		if (vhSerialRef != null) {
 			vhSerialRef.unregister();
 		}
 		
 		if (cc != null) {
 			cc.close();
 		}
 	}
 
 	private Properties createBasicServiceProperties() {
 		Properties p = new Properties();
 		p.put("Provider", this.getClass().getName());
 		p.put("Slot", Integer.toString(slotId));
 		// p.put(RemoteOSGiServiceConstants.R_OSGi_REGISTRATION, "true");
 		return p;
 	}
 
 	public List getModuleProperties() {
 		List properties = new ArrayList();
 
 		properties.add(new ModuleProperty(PROPERTY_MODULE_NAME, getModuleName()));
 		properties.add(new ModuleProperty("Slot", "" + slotId));
 		properties.add(new ModuleProperty("State", Boolean.toString(deviceOn), "Boolean", true));
 
 		return properties;
 	}
 
 	public boolean setModuleProperty(IModuleProperty property) {
 		if (!property.isMutable()) {
 			return false;
 		}
 		if (property.getName().equals("State")) {
 			deviceOn = Boolean.valueOf((String) property.getValue()).booleanValue();
 			return true;
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
 
 	public void setup() throws Exception {
 		int slot = slotId + 1;
 		String devnode_vh = "/dev/bmi_vh_control_m" + slot;
 		vhDevice = new VonHippel();
 		CharDeviceUtils.openDeviceWithRetry(vhDevice, devnode_vh, 2);
 		vhc = new VonHippelModuleControl(vhDevice, slotId);
 	}
 
 }
