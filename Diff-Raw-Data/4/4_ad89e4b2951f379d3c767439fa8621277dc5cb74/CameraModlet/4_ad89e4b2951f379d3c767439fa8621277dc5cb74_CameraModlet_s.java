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
 package com.buglabs.bug.module.camera;
 
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Properties;
 
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.ServiceRegistration;
 import org.osgi.service.log.LogService;
 
 import com.buglabs.bug.jni.camera.Camera;
 import com.buglabs.bug.jni.camera.CameraControl;
 import com.buglabs.bug.module.camera.pub.ICameraDevice;
 import com.buglabs.bug.module.camera.pub.ICameraModuleControl;
 import com.buglabs.bug.module.pub.BMIModuleProperties;
 import com.buglabs.bug.module.pub.IModlet;
 import com.buglabs.module.IModuleControl;
 import com.buglabs.module.IModuleLEDController;
 import com.buglabs.module.IModuleProperty;
 import com.buglabs.module.ModuleProperty;
 import com.buglabs.services.ws.IWSResponse;
 import com.buglabs.services.ws.PublicWSDefinition;
 import com.buglabs.services.ws.PublicWSProvider;
 import com.buglabs.services.ws.PublicWSProvider2;
 import com.buglabs.services.ws.WSResponse;
 import com.buglabs.util.LogServiceUtil;
 
 /**
  * 
  * @author kgilmer
  * 
  */
 public class CameraModlet implements IModlet, ICameraDevice, PublicWSProvider2, IModuleControl {
 	private static final String JPEG_MIME_TYPE = "image/jpg";
 	private static boolean suspended = false;
 
 	private List modProps;
 
 	private final BundleContext context;
 
 	private final int slotId;
 
 	private final String moduleName;
 
 	private ServiceRegistration moduleRef;
 
 	private ServiceRegistration cameraService;
 
 	private LogService logService;
 
 	private Camera camera;
 	
 	protected static final String PROPERTY_MODULE_NAME = "moduleName";
 
 	private String moduleId;
 
 	private CameraModuleControl cameraControl;
 
 	private ServiceRegistration cameraControlRef;
 
 	private CameraControl cc;
 
 	private ServiceRegistration ledRef;
 	private String serviceName = "Picture";
 	private BMIModuleProperties properties;
 	private ServiceRegistration wsReg;
 
 	public CameraModlet(BundleContext context, int slotId, String moduleId) {
 		this.context = context;
 		this.slotId = slotId;
 		this.moduleId = moduleId;
 		this.moduleName = "Camera";
 		this.properties = null;
 	}
 
 	public CameraModlet(BundleContext context, int slotId, String moduleId, BMIModuleProperties properties) {
 		this(context, slotId, moduleId);
 		this.properties = properties;
 	}
 
 	public String getModuleId() {
 		return moduleId;
 	}
 
 	public int getSlotId() {
 		return slotId;
 	}
 
 	public void setup() throws Exception {
 		logService = LogServiceUtil.getLogService(context);
 	}
 
 	public void start() throws Exception {
 		modProps = new ArrayList();
 
 		camera = new Camera();
 		cc = new CameraControl();
 		cameraControl = new CameraModuleControl(cc);
 		cameraControlRef = context.registerService(ICameraModuleControl.class.getName(), cameraControl, createBasicServiceProperties());
 		Properties modProperties = createBasicServiceProperties();
 		modProperties.put("Power State", suspended ? "Suspended": "Active");
 		moduleRef = context.registerService(IModuleControl.class.getName(), this, modProperties);
 		cameraService = context.registerService(ICameraDevice.class.getName(), this, createBasicServiceProperties());
 		ledRef = context.registerService(IModuleLEDController.class.getName(), cameraControl, createBasicServiceProperties());
 
 		wsReg = context.registerService(PublicWSProvider.class.getName(), this, null);
 	}
 
 	private Properties createBasicServiceProperties() {
 		Properties p = new Properties();
 		p.put("Provider", this.getClass().getName());
 		p.put("Slot", Integer.toString(slotId));
 
 		if (properties != null) {
 			if (properties.getDescription() != null)
 				p.put("ModuleDescription", properties.getDescription());
 			
 			if (properties.getSerial_num() != null)
 				p.put("ModuleSN", properties.getSerial_num());
 			
 			// these are ints so don't need a null check
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
 
 	public void stop() throws Exception {
 		cameraControlRef.unregister();
 		cameraService.unregister();
 		moduleRef.unregister();
 		ledRef.unregister();
 		wsReg.unregister();
 		camera.close();
 	}
 
 	public PublicWSDefinition discover(int operation) {
 		if (operation == PublicWSProvider2.GET) {
 			return new PublicWSDefinition() {
 
 				public List getParameters() {
 					return null;
 				}
 
 				public String getReturnType() {
 					return JPEG_MIME_TYPE;
 				}
 			};
 		}
 
 		return null;
 	}
 
 	public IWSResponse execute(int operation, String input) {
 		System.out.println("New Picture GET");
 		if (operation == PublicWSProvider2.GET) {
			/*
 			bug_camera_open(ICameraDevice.DEFAULT_MEDIA_NODE,
 					-1,
 					2048,
 					1536,
 					320,
 					240);
 			bug_camera_start();
 			
 			// throw away 3 previews to get the exposure etc right
 			bug_camera_grab_preview();
 			bug_camera_grab_preview();
 			bug_camera_grab_preview();
 			
 			IWSResponse response =  new WSResponse(new ByteArrayInputStream(bug_camera_grab_full()), JPEG_MIME_TYPE);
 			
 			bug_camera_stop();
 			bug_camera_close();
 			return response;
			*/
			return new WSResponse(new ByteArrayInputStream(bug_camera_grab_full()), JPEG_MIME_TYPE);
 		}
 		return null;
 	}
 
 	public String getPublicName() {
 		return serviceName;
 	}
 
 	public List getModuleProperties() {
 		modProps.clear();
 
 		modProps.add(new ModuleProperty(PROPERTY_MODULE_NAME, getModuleName()));
 		modProps.add(new ModuleProperty("Slot", "" + slotId));
 		modProps.add(new ModuleProperty("Power State", suspended ? "Suspended": "Active", "String", true));
 		
 		if (properties != null) {
 			modProps.add(new ModuleProperty("Module Description", properties.getDescription()));
 			modProps.add(new ModuleProperty("Module SN", properties.getSerial_num()));
 			modProps.add(new ModuleProperty("Module Vendor ID", "" + properties.getVendor()));
 			modProps.add(new ModuleProperty("Module Revision", "" + properties.getRevision()));
 		}
 		
 		return modProps;
 	}
 
 	public String getModuleName() {
 		return moduleName;
 	}
 
 	public boolean setModuleProperty(IModuleProperty property) {
 		if (!property.isMutable()) {
 			return false;
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
 					LogServiceUtil.logBundleException(logService, e.getMessage(), e);
 				}
 			}
 			
 				
 			
 		}
 		
 		return false;
 	}
 	
 	public int resume() throws IOException {
 		int result = -1;
 
 		result = cc.ioctl_BMI_CAM_RESUME();
 		suspended = false;
 		if (result < 0) {
 			throw new IOException("ioctl BMI_CAM_RESUME failed");
 		}
 		suspended = false;
 		updateIModuleControlProperties();
 		return result;
 	}
 	
 
 	public int suspend() throws IOException {
 		int result = -1;
 
 		result = cc.ioctl_BMI_CAM_SUSPEND();
 		if (result < 0) {
 			throw new IOException("ioctl BMI_CAM_SUSPEND failed");
 		}
 		suspended = true;
 		updateIModuleControlProperties();
 		return result;
 	}
 
 	public String getFormat() {
 
 		return JPEG_MIME_TYPE;
 	}
 
 	public String getDescription() {
 		return "This service can return image data from a hardware camera.";
 	}
 
 	public void setPublicName(String name) {
 		serviceName = name;
 	}
 	
 	public int bug_camera_open(
 			final String media_node,
 			int slot_num,
 			int full_height,
 			int full_width,
 			int preview_height,
 			int preview_width)
 	{
 		return camera.bug_camera_open(media_node, slot_num, full_height, full_width, preview_height, preview_width);
 	}
 
 	public int bug_camera_close()
 	{
 		return camera.bug_camera_close();
 	}
 
 	public int bug_camera_start()
 	{
 		return camera.bug_camera_start();
 	}
 	
 	public int bug_camera_stop()
 	{
 		return camera.bug_camera_stop();
 	}
 
 	public byte[] bug_camera_grab_preview()
 	{
 		return camera.bug_camera_grab_preview();
 	}
 
 	public byte[] bug_camera_grab_full()
 	{
 		return camera.bug_camera_grab_raw();
 	}
 }
