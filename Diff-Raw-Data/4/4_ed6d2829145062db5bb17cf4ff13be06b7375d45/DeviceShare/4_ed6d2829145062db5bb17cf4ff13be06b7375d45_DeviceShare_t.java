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
  * Name: DeviceShare.java
  * Project: SensorAct-VPDS
  * Version: 1.0
  * Date: 2012-05-13
  * Author: Pandarasamy Arjunan
  */
 package edu.pc3.sensoract.vpds.api;
 
 import java.util.Date;
 import java.util.List;
 
 import play.Play;
 
 import edu.pc3.sensoract.vpds.api.request.DeviceShareFormat;
 import edu.pc3.sensoract.vpds.api.request.GuardRuleAddFormat;
 import edu.pc3.sensoract.vpds.api.request.GuardRuleAssociationAddFormat;
 import edu.pc3.sensoract.vpds.api.request.GuardRuleAssociationGetFormat;
 import edu.pc3.sensoract.vpds.api.request.GuardRuleDeleteFormat;
 import edu.pc3.sensoract.vpds.api.response.DeviceProfileFormat;
 import edu.pc3.sensoract.vpds.constants.Const;
 import edu.pc3.sensoract.vpds.enums.ErrorType;
 import edu.pc3.sensoract.vpds.exceptions.InvalidJsonException;
 import edu.pc3.sensoract.vpds.guardrule.GuardRuleManager;
 import edu.pc3.sensoract.vpds.model.GuardRuleAssociationModel;
 import edu.pc3.sensoract.vpds.model.ShareAccessModel;
 
 /**
  * device/share API: Share device profile with others
  * 
  * @author Pandarasamy Arjunan
  * @version 1.0
  */
 public class DeviceShare extends SensorActAPI {
 
 	/**
 	 * Validates the device share request format attributes. If validation
 	 * fails, sends corresponding failure message to the caller.
 	 * 
 	 * @param deviceShareRequest
 	 *            Device share request format object
 	 */
 	private void validateRequest(final DeviceShareFormat deviceShareRequest) {
 
 		validator.validateSecretKey(deviceShareRequest.secretkey);
 		// TODO: add validation for other parameters
 
 		if (validator.hasErrors()) {
 			response.sendFailure(Const.API_DEVICE_SHARE,
 					ErrorType.VALIDATION_FAILED, validator.getErrorMessages());
 		}
 	}
 
 	private void deleteExistingShare(final DeviceShareFormat req,
 			final GuardRuleAddFormat guardRule,
 			final GuardRuleAssociationAddFormat association) {
 
 		/*
 		 * GuardRuleAssociationGetFormat aGet = new
 		 * GuardRuleAssociationGetFormat(); aGet.secretkey =
 		 * association.secretkey; aGet.devicename = association.devicename;
 		 * aGet.sensorname = association.sensorname; aGet.sensorid =
 		 * association.sensorid; aGet.actuatorname = association.actuatorname;
 		 * aGet.actuatorid = association.actuatorid;
 		 */
 
 		List<ShareAccessModel> sharedList = ShareAccessModel.getSharedAccess(
 				req.brokername, req.username, req.email);
 
 		// if no shared device found, just add them
 		if (null == sharedList || sharedList.isEmpty()) {
 			return;
 		}
 
 		for (ShareAccessModel.SharedDevice sDevice : sharedList.get(0).shared) {
 
 			System.out.println("shared device " + json.toJson(sDevice));
 
 			if (sDevice.devicename.equalsIgnoreCase(req.share.devicename)
 					&& sDevice.sensorname
 							.equalsIgnoreCase(req.share.sensorname)
 					&& sDevice.sensorid.equalsIgnoreCase(req.share.sensorid)
 					&& sDevice.actuatorname
 							.equalsIgnoreCase(req.share.actuatorname)
 					&& sDevice.actuatorid
 							.equalsIgnoreCase(req.share.actuatorid)) {
 
 				// delete the existing guard rule and the corresponding
 				// association
 
 				GuardRuleDeleteFormat gDel = new GuardRuleDeleteFormat();
 
 				gDel.secretkey = Play.configuration
 						.getProperty(Const.OWNER_OWNERKEY);
 				gDel.name = sDevice.guardrulename;
 
 				GuardRuleManager.deleteGuardRule(gDel);
 				GuardRuleManager.deleteRuleAssociations(gDel.secretkey,
 						gDel.name);
 			}
 		}
 
 	}
 
 	private void updateGuardRule(final DeviceShareFormat req) throws Exception {
 
 		GuardRuleAddFormat guardRule = new GuardRuleAddFormat();
 		GuardRuleAssociationAddFormat association = new GuardRuleAssociationAddFormat();
 
 		String accesskey = userProfile.getHashCode(req.brokername
 				+ req.username + req.email);
 		System.out.println("\n Access key created!!!  " + accesskey + "\n");
 
 		guardRule.secretkey = req.secretkey;
 		// TODO: what is the default priority?
 		guardRule.rule.priority = 0;
 		guardRule.rule.condition = "USER.email=='" + req.email + "'";
 		guardRule.rule.action = Const.PARAM_ALLOW;
 
 		// TODO: include broker name also to uniquely identify the rule name
 		String guardRuleName = req.share.devicename + ":" + req.username + ":";
 
 		association.secretkey = req.secretkey;
 		association.devicename = req.share.devicename;
 		association.sensorname = req.share.sensorname;
 		association.sensorid = req.share.sensorid;
 		association.actuatorname = req.share.actuatorname;
 		association.actuatorid = req.share.actuatorid;
 
 		if (req.share.read) {
 			guardRule.rule.name = guardRuleName + Const.PARAM_READ
 					+ new Date().getTime();
 			guardRule.rule.description = guardRule.rule.name;
 			guardRule.rule.targetOperation = Const.PARAM_READ;
 			association.rulename = guardRule.rule.name;
 
 			deleteExistingShare(req, guardRule, association);
 			GuardRuleManager.addGuardRule(guardRule);
 			GuardRuleManager.addAssociation(association);
			
			req.secretkey = accesskey;
 			ShareAccessModel share = new ShareAccessModel(req,
 					guardRule.rule.name);
 			share.save();
 		}
 
 		if (req.share.write) {
 			guardRule.rule.name = guardRuleName + Const.PARAM_WRITE
 					+ (new Date().getTime() + 1); // Just to make the rules
 													// unique
 			guardRule.rule.description = guardRule.rule.name;
 			guardRule.rule.targetOperation = Const.PARAM_WRITE;
 
 			association.rulename = guardRule.rule.name;
 
 			deleteExistingShare(req, guardRule, association);
 			GuardRuleManager.addGuardRule(guardRule);
 			GuardRuleManager.addAssociation(association);
			req.secretkey = accesskey;
 			ShareAccessModel share = new ShareAccessModel(req,
 					guardRule.rule.name);
 			share.save();
 
 		}
 	}
 
 	private void sharedevice(DeviceShareFormat req) throws Exception {
 
 		// Step 1: Verify the device exists
 		// TODO: verify sensor/actuator also
 		DeviceProfileFormat oneDevice = deviceProfile.getDevice(req.secretkey,
 				req.share.devicename);
 		if (null == oneDevice) {
 			response.sendFailure(Const.API_DEVICE_GET,
 					ErrorType.DEVICE_NOTFOUND, req.share.devicename);
 		}
 
 		// Step 2 : Create a guard rule
 		// Step 3 : Update the table
 		updateGuardRule(req);
 	}
 
 	/**
 	 * Services the device/share API.
 	 * 
 	 * @param deviceShareJson
 	 *            Device share request attributes in Json string
 	 */
 	public void doProcess(final String deviceShareJson) {
 
 		try {
 			DeviceShareFormat deviceShareRequest = convertToRequestFormat(
 					deviceShareJson, DeviceShareFormat.class);
 			validateRequest(deviceShareRequest);
 
 			if (!userProfile
 					.isRegisteredSecretkey(deviceShareRequest.secretkey)) {
 				response.sendFailure(Const.API_DEVICE_SHARE,
 						ErrorType.UNREGISTERED_SECRETKEY,
 						deviceShareRequest.secretkey);
 			}
 
 			sharedevice(deviceShareRequest);
 
 			// TODO: share device
 			response.SendSuccess(Const.API_DEVICE_SHARE, Const.DEVICE_SHARED);
 
 		} catch (InvalidJsonException e) {
 			response.sendFailure(Const.API_DEVICE_SHARE,
 					ErrorType.INVALID_JSON, e.getMessage());
 		} catch (Exception e) {
 			response.sendFailure(Const.API_DEVICE_SHARE,
 					ErrorType.SYSTEM_ERROR, e.getMessage());
 		}
 	}
 
 }
