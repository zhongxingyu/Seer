 /*
  * Copyright (c) 2012, Indraprastha Institute of Information Technology,
  * Delhi (IIIT-D) and The Regents of the University of California.
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions
  * are met:
  * 1. Redistributions of source code must retain the above copyright
  *    notice, this list of conditions and the following disclaimer.
  * 2. Redistributions in binary form must reproduce the above
  *    copyright notice, this list of conditions and the following
  *    disclaimer in the documentation and/or other materials provided
  *    with the distribution.
  * 3. Neither the names of the Indraprastha Institute of Information
  *    Technology, Delhi and the University of California nor the names
  *    of their contributors may be used to endorse or promote products
  *    derived from this software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE IIIT-D, THE REGENTS, AND CONTRIBUTORS
  * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
  * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
  * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE IIITD-D, THE REGENTS
  * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
  * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
  * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
  * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
  * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
  * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
  * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
  * SUCH DAMAGE.
  *
  */
 /*
  * Name: DeviceProfile.java
  * Project: SensorAct-VPDS
  * Version: 1.0
  * Date: 2012-04-14
  * Author: Pandarasamy Arjunan
  */
 package edu.pc3.sensoract.vpds.profile.mongo;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import play.modules.morphia.Model.MorphiaQuery;
 import edu.pc3.sensoract.vpds.api.request.DeviceAddFormat;
 import edu.pc3.sensoract.vpds.api.response.DeviceListResponseFormat;
 import edu.pc3.sensoract.vpds.api.response.DeviceProfileFormat;
 import edu.pc3.sensoract.vpds.model.DeviceModel;
 import edu.pc3.sensoract.vpds.model.DeviceProfileModel;
 import edu.pc3.sensoract.vpds.model.DeviceTemplateModel;
 import edu.pc3.sensoract.vpds.profile.DeviceProfile;
 
 /**
  * Device profile management, provides methods for managing devices and device
  * templates
  * 
  * @author Pandarasamy Arjunan
  * @version 1.0
  */
 public class DeviceProfileImpl implements DeviceProfile {
 
 	/**
 	 * Adds a new device to the repository.
 	 * 
 	 * @param newDevice
 	 *            Device object to persist to the repository.
 	 * @return True, if device profile added successfully, otherwise false.
 	 */
 	@Override
 	public boolean addDevice(final DeviceAddFormat newDevice) {
 		DeviceModel device = new DeviceModel(newDevice);
 		device.save();
 		return true;
 	}
 
 	/**
 	 * Adds a new device template to the repository.
 	 * 
 	 * @param newTemplate
 	 *            Device profile object to persist to the repository.
 	 * @return True, if device template added successfully, otherwise false.
 	 */
 	@Override
 	public boolean addDeviceTemplate(final DeviceAddFormat newTemplate) {
 		DeviceTemplateModel template = new DeviceTemplateModel(newTemplate);
 		template.save();
 		return true;
 	}
 
 	/**
 	 * Removes a device profile corresponding to the user's secretkey and
 	 * devicename from the repository.
 	 * 
 	 * @param secretkey
 	 *            Secret key of the registered user associated with the device.
 	 * @param devicename
 	 *            Name of the registered device.
 	 * @return True, if device removed successfully, otherwise false.
 	 */
 
 	@Override
 	public boolean deleteDevice(final String secretkey, final String devicename) {
 
 		// TODO: Include other params to uniquely identify device profile
 		// TODO: Inconsistent way with play's jpa Model
 		MorphiaQuery mq = DeviceModel.find("bySecretkeyAndDevicename",
 				secretkey, devicename);
 		if (0 == mq.count()) {
 			return false;
 		}
 		// DeviceProfileModel.find("bySecretkeyAndName", secretkey,
 		// devicename).delete();
 		mq.delete();
 		return true;
 	}
 
 	/**
 	 * Removes a device template corresponding to the user's secretkey and
 	 * templatename from the repository.
 	 * 
 	 * @param secretkey
 	 *            Secret key of the registered user associated with the device
 	 *            template.
 	 * @param templatename
 	 *            Name of the registered device template.
 	 * @return True, if device profile removed successfully, otherwise false.
 	 */
 	@Override
 	public boolean deleteDeviceTemplate(final String secretkey,
 			final String templatename) {
 
 		// TODO: Include other params to uniquely identify device profile
 		// TODO: Inconsistent way with play's jpa Model
 		MorphiaQuery mq = DeviceTemplateModel.find(
				"bySecretkeyAndTemplatename", secretkey, templatename);
 		if (0 == mq.count()) {
 			return false;
 		}
 		// DeviceProfileModel.find("bySecretkeyAndName", secretkey,
 		// devicename).delete();
 		mq.delete();
 		return true;
 	}
 
 	/**
 	 * Retrieves a device from the data repository corresponding to the
 	 * devicename.
 	 * 
 	 * @param secretkey
 	 *            Secret key of the registered user associated with the device.
 	 * @param devicename
 	 *            Name of the registered device profile.
 	 * @return Device object in DeviceModel format.
 	 * @see DeviceModel
 	 */
 	@Override
 	public DeviceProfileFormat getDevice(final String secretkey,
 			final String devicename) {
 
 		// TODO: Inconsistent way with play's jpa Model
 		List<DeviceModel> deviceList = DeviceModel.find(
 				"bySecretkeyAndDevicename", secretkey, devicename).fetchAll();
 		if (null == deviceList || 0 == deviceList.size()) {
 			return null;
 		}
 		return new DeviceProfileFormat(deviceList.get(0));
 	}
 
 	/**
 	 * Retrieves a device template from the data repository corresponding to the
 	 * templatename.
 	 * 
 	 * @param secretkey
 	 *            Secret key of the registered user associated with the device.
 	 * @param templatename
 	 *            Name of the registered device template.
 	 * @return Device template object in DeviceTemplateModel format.
 	 * @see DeviceTemplateModel
 	 */
 	@Override
 	public DeviceProfileFormat getDeviceTemplate(final String secretkey,
 			final String templatename) {
 
 		// TODO: Inconsistent way with play's jpa Model
 		List<DeviceTemplateModel> templateList = DeviceTemplateModel.find(
 				"bySecretkeyAndTemplatename", secretkey, templatename)
 				.fetchAll();
 		if (null == templateList || 0 == templateList.size()) {
 			return null;
 		}
 
 		return new DeviceProfileFormat(templateList.get(0));
 		// return null;
 
 	}
 
 	/**
 	 * Retrieves all devices from the data repository corresponding to the
 	 * secretkey.
 	 * 
 	 * @param secretkey
 	 *            Secret key of the registered user associated with the devices.
 	 * @return List of devices in DeviceModel object.
 	 * @see DeviceModel
 	 */
 	@Override
 	public List<DeviceProfileFormat> getDeviceList(final String secretkey) {
 
 		// TODO: Inconsistent way with play's jpa Model
 		List<DeviceModel> deviceList = DeviceModel.find("bySecretkey",
 				secretkey).fetchAll();
 		if (null == deviceList || 0 == deviceList.size()) {
 			return null;
 		}
 		// TODO: filter only devices
 		// Iterator<DeviceProfileModel> devicesListIterator = allDevicesList
 		// .iterator();
 		// while (devicesListIterator.hasNext()) {
 		// devicesListIterator.next().templatename = null;
 		// }
 
 		List<DeviceProfileFormat> list = new ArrayList<DeviceProfileFormat>();
 		for (DeviceModel dm : deviceList) {
 			list.add(new DeviceProfileFormat(dm));
 		}
 		return list;
 	}
 
 	/**
 	 * Retrieves all device templates from the data repository corresponding to
 	 * the secretkey.
 	 * 
 	 * @param secretkey
 	 *            Secret key of the registered user associated with the devices.
 	 * @return List of device templates in DeviceTemplateModel object.
 	 * @see DeviceTemplateModel
 	 */
 	@Override
 	public List<DeviceProfileFormat> getDeviceTemplateList(
 			final String secretkey) {
 
 		// TODO: Inconsistent way with play's jpa Model
 		List<DeviceTemplateModel> templateList = DeviceTemplateModel.find(
 				"bySecretkey", secretkey).fetchAll();
 		if (null == templateList || 0 == templateList.size()) {
 			return null;
 		}
 
 		List<DeviceProfileFormat> tlist = new ArrayList<DeviceProfileFormat>();
 		for (DeviceTemplateModel dm : templateList) {
 			tlist.add(new DeviceProfileFormat(dm));
 		}
 		
 		return tlist;
 	}
 
 	/**
 	 * Retrieves all device templates from the data repository corresponding to
 	 * the secretkey.
 	 * 
 	 * @param secretkey
 	 *            Secret key of the registered user associated with the devices.
 	 * @return List of device templates in DeviceTemplateModel object.
 	 * @see DeviceTemplateModel
 	 */
 	@Override
 	public List<DeviceProfileFormat> getGlobalDeviceTemplateList() {
 
 		// TODO: Inconsistent way with play's jpa Model
 		List<DeviceTemplateModel> templateList = DeviceTemplateModel.find(
 				"isglobal", true).fetchAll();
 		if (null == templateList || 0 == templateList.size()) {
 			return null;
 		}
 
 		List<DeviceProfileFormat> tlist = new ArrayList<DeviceProfileFormat>();
 		for (DeviceTemplateModel dm : templateList) {
 			tlist.add(new DeviceProfileFormat(dm));
 		}
 		
 		return tlist;
 	}
 
 	/**
 	 * Checks for duplicate devices. If device already exists in the repository,
 	 * sends corresponding failure message to the caller.
 	 * 
 	 * @param newDevice
 	 *            Device object to check for duplicates.
 	 * @return True, if device profile exists in the repository, otherwise
 	 *         false.
 	 */
 	@Override
 	public boolean isDeviceExists(final DeviceAddFormat newDevice) {
 
 		// TODO: Check the uniqueness against id, ip, etc also
 		return 0 == DeviceModel.count("bySecretkeyAndDevicename",
 				newDevice.secretkey, newDevice.deviceprofile.devicename) ? false
 				: true;
 	}
 
 	/**
 	 * Checks for duplicate device templates. If device template already exists
 	 * in the repository, sends corresponding failure message to the caller.
 	 * 
 	 * @param newTemplate
 	 *            Device template object to check for duplicates.
 	 * @return True, if device template exists in the repository, otherwise
 	 *         false.
 	 */
 
 	@Override
 	public boolean isDeviceTemplateExists(final DeviceAddFormat newTemplate) {
 
 		// TODO: Check the uniqueness against id, ip, etc also
 		return 0 == DeviceTemplateModel.count("bySecretkeyAndTemplatename",
 				newTemplate.secretkey, newTemplate.deviceprofile.templatename) ? false
 				: true;
 	}
 
 }
