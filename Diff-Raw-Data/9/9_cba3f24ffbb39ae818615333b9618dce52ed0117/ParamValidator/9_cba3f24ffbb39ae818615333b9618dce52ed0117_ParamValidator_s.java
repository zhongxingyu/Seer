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
  * Name: ParamValidator.java
  * Project: SensorAct-VPDS
  * Version: 1.0
  * Date: 2012-04-14
  * Author: Pandarasamy Arjunan
  */
 package edu.pc3.sensoract.vpds.util;
 
 import play.data.validation.Error;
 import edu.pc3.sensoract.vpds.api.SensorActAPI;
 import edu.pc3.sensoract.vpds.constants.Const;
 
 /**
  * API helper class to validate various request parameters. This is a wrapper
  * for play's internal <validation> object.
  * 
  * @author Pandarasamy Arjunan
  * @version 1.0
  */
 public class ParamValidator extends SensorActAPI {
 
 	/**
 	 * Validates a string parameter and sets the corresponding error messages on
 	 * failure.
 	 * 
 	 * @param string
 	 *            String parameter value
 	 * @param name
 	 *            Name of the parameter
 	 * @param minSize
 	 *            Parameter's valid minimum length
 	 * @param maxSize
 	 *            Parameter's valid maximum length
 	 */
 	protected void validateString(final String string, final String name,
 			final int minSize, final int maxSize) {
 		validation.required(string).message(name + Const.MSG_REQUIRED);
 		validation.minSize(string, minSize).message(
 				name + Const.MSG_MIN_LENGTH + minSize);
 		validation.maxSize(string, maxSize).message(
 				name + Const.MSG_MAX_LENGTH + maxSize);
 	}
 
 	/**
 	 * Validates a numeric parameter and sets the corresponding error messages
 	 * on failure.
 	 * 
 	 * @param number
 	 *            Numeric parameter value
 	 * @param name
 	 *            Name of the parameter
 	 * @param minValue
 	 *            Parameter's valid minimum value
 	 * @param maxValue
 	 *            Parameter's vaid maximum value
 	 */
 	private void validateNumber(final double number, final String name,
 			final double minValue, final double maxValue) {
 		validation.min(number, minValue).message(
 				name + Const.MSG_MIN_VALUE + minValue);
 		validation.max(number, maxValue).message(
 				name + Const.MSG_MAX_VALUE + maxValue);
 	}
 
 	public boolean hasErrors() {
 		return validation.hasErrors();
 	}
 
 	/**
 	 * Returns validation error messages, if any previous validation has failed.
 	 * Otherwise returns null.
 	 * 
 	 * @return Validation error messages separated by ;
 	 */
 	public String getErrorMessages() {
 
 		if (!validation.hasErrors()) {
 			return null;
 		}
 
 		StringBuffer errMsg = new StringBuffer();
 		for (Error error : validation.errors()) {
 			errMsg.append(error.message()).append(";");
 		}
 		return errMsg.toString();
 	}
 
 	/**
 	 * Adds an error message to the validation object
 	 * 
 	 * @param field
 	 *            Name of the attribute caused the error
 	 * @param message
 	 *            Error message
 	 */
 	public void addError(final String field, final String message) {
 		validation.addError(field, message);
 	}
 
 	/**
 	 * Validates username parameter and sets corresponding error message, if
 	 * validation fails.
 	 * 
 	 * @param username
 	 *            User name to validate
 	 */
 	public void validateUserName(final String username) {
 		validateString(username, Const.PARAM_USERNAME,
 				Const.USERNAME_MIN_LENGTH, Const.USERNAME_MAX_LENGTH);
 	}
 
 	/**
 	 * Validates password parameter and sets the corresponding error message, if
 	 * validation fails.
 	 * 
 	 * @param password
 	 *            Password Password to validate
 	 */
 	public void validatePassword(final String password) {
 		validateString(password, Const.PARAM_PASSWORD,
 				Const.PASSWORD_MIN_LENGTH, Const.PASSWORD_MAX_LENGTH);
 	}
 
 	/**
 	 * Validates email parameter and sets the corresponding error message, if
 	 * validation fails.
 	 * 
 	 * @param email
 	 *            E-mail address to validate
 	 */
 	public void validateEmail(final String email) {
 		validation.required(email).message(
 				Const.PARAM_EMAIL + Const.MSG_REQUIRED);
 		validation.email(email).message(Const.PARAM_EMAIL + Const.MSG_INVALID);
 		validation.minSize(email, Const.EMAIL_MIN_LENGTH).message(
 				Const.PARAM_EMAIL + Const.MSG_MIN_LENGTH
 						+ Const.EMAIL_MIN_LENGTH);
 		validation.maxSize(email, Const.EMAIL_MAX_LENGTH).message(
 				Const.PARAM_EMAIL + Const.MSG_MAX_LENGTH
 						+ Const.EMAIL_MAX_LENGTH);
 	}
 
 	/**
 	 * Validates secretkey parameter and sets the corresponding error message,
 	 * if validation fails.
 	 * 
 	 * @param secretkey
 	 *            Secret key to validate
 	 */
 	public void validateSecretKey(final String secretkey) {
 		validateString(secretkey, Const.PARAM_SECRETKEY,
 				Const.SECRETKEY_MIN_LENGTH, Const.SECRETKEY_MAX_LENGTH);
 	}
 
 	/**
 	 * Validates ke parameter and sets the corresponding error message, if
 	 * validation fails.
 	 * 
 	 * @param key
 	 *            Key to validate
 	 */
 	public void validateKey(final String key) {
 		validateString(key, Const.PARAM_KEY, Const.SECRETKEY_MIN_LENGTH,
 				Const.SECRETKEY_MAX_LENGTH);
 	}
 
 	/**
 	 * Validates templatename parameter and sets the corresponding error message,
 	 * if validation fails.
 	 * 
 	 * @param templatename
 	 *            Template name to validate
 	 */
 	public void validateTemplateName(final String templatename) {
 		validateString(templatename, Const.PARAM_TEMPLATENAME,
 				Const.TEMPLATENAME_MIN_LENGTH, Const.TEMPLATENAME_MAX_LENGTH);
 	}
 
 	/**
 	 * Validates devicename parameter and sets the corresponding error message,
 	 * if validation fails.
 	 * 
 	 * @param devicename
 	 *            Device name to validate
 	 */
 
 	public void validateDeviceName(final String devicename) {
 		validateString(devicename, Const.PARAM_DEVICENAME,
 				Const.DEVICENAME_MIN_LENGTH, Const.DEVICENAME_MAX_LENGTH);
 	}
 
 	/**
 	 * Validates deviceprofile.name parameter and sets the corresponding error
 	 * message, validation if fails.
 	 * 
 	 * @param templatename
 	 *            Device name to validate
 	 */
 	public void validateDeviceProfileDeviceName(final String name) {
 		validateString(name,
 				Const.PARAM_DEVICEPROFILE + "." + Const.PARAM_DEVICENAME,
 				Const.DEVICEPROFILENAME_MIN_LENGTH,
 				Const.DEVICEPROFILENAME_MAX_LENGTH);
 	}
 
 	/**
 	 * Validates deviceprofile.name parameter and sets the corresponding error
 	 * message, validation if fails.
 	 * 
 	 * @param templatename
 	 *            Device name to validate
 	 */
 	public void validateDeviceProfileTemplateName(final String name) {
 		validateString(name,
				Const.PARAM_DEVICEPROFILE + "." + Const.PARAM_DEVICENAME,
 				Const.DEVICEPROFILENAME_MIN_LENGTH,
 				Const.DEVICEPROFILENAME_MAX_LENGTH);
 	}
 
 	/**
 	 * Validates actuator IP parameter and sets the corresponding error message,
 	 * if validation fails.
 	 * 
 	 * @param actuatorIP
 	 *            Actuator IP address to validate
 	 */
 	public void validateDeviceProfileIP(final String IP) {
 		validation.required(IP).message(
 				Const.PARAM_DEVICEPROFILE + "." + Const.PARAM_IP
 						+ Const.MSG_REQUIRED);
 		// TODO: validation of IP address is weired.
 		validation.ipv4Address(IP).message(
 				Const.PARAM_DEVICEPROFILE + "." + Const.PARAM_IP
 						+ Const.MSG_INVALID);
 	}
 
 	/**
 	 * Validates location parameter and sets the corresponding error message, if
 	 * validation fails.
 	 * 
 	 * @param location
 	 *            Location name to validate
 	 */
 	public void validateDeviceProfileLocation(final String location) {
 		validateString(location, Const.PARAM_DEVICEPROFILE + "."
 				+ Const.PARAM_LOCATION, Const.LOCATION_MIN_LENGTH,
 				Const.LOCATION_MAX_LENGTH);
 	}
 
 	/**
 	 * Validates tags parameter and sets the corresponding error message, if
 	 * validation fails.
 	 * 
 	 * @param tags
 	 *            Tags to validate
 	 */
 	public void validateDeviceProfileTags(final String tags) {
 		validateString(tags,
 				Const.PARAM_DEVICEPROFILE + "." + Const.PARAM_TAGS,
 				Const.TAGS_MIN_LENGTH, Const.TAGS_MAX_LENGTH);
 	}
 
 	/**
 	 * Validates latitude parameter and sets the corresponding error message, if
 	 * validation fails.
 	 * 
 	 * @param latitude
 	 *            Latitude value to validate
 	 */
 	public void validateDeviceProfileLatitude(final Double latitude) {
 
 		if (null == latitude) {
 			validator.addError(Const.PARAM_LATITUDE, Const.PARAM_DEVICEPROFILE
 					+ "." + Const.PARAM_LATITUDE + Const.MSG_REQUIRED);
 		} else {
 			validateNumber(latitude.doubleValue(), Const.PARAM_DEVICEPROFILE
 					+ "." + Const.PARAM_LATITUDE, Const.LATITUDE_MIN_VALUE,
 					Const.LATITUDE_MAX_VALUE);
 		}
 	}
 
 	/**
 	 * Validates longitude parameter and sets the corresponding error message,
 	 * if validation fails.
 	 * 
 	 * @param longitude
 	 *            Longitude value to validate
 	 */
 	public void validateDeviceProfileLongitude(final Double longitude) {
 
 		if (null == longitude) {
 			validator.addError(Const.PARAM_LONGITUDE, Const.PARAM_DEVICEPROFILE
 					+ "." + Const.PARAM_LONGITUDE + Const.MSG_REQUIRED);
 		} else {
 			validateNumber(longitude.doubleValue(), Const.PARAM_DEVICEPROFILE
 					+ "." + Const.PARAM_LONGITUDE, Const.LONGITUDE_MIN_VALUE,
 					Const.LONGITUDE_MAX_VALUE);
 		}
 	}
 
 	/**
 	 * Validates sensor name parameter and sets the corresponding error message,
 	 * if validation fails.
 	 * 
 	 * @param sensorName
 	 *            Sensor name value of device profile to validate
 	 * @param index
 	 *            Index of the sensor name in the device profile
 	 */
 	public void validateDeviceProfileSensorName(final String sensorName,
 			final int sIndex) {
 		validateString(sensorName, Const.PARAM_DEVICEPROFILE + "."
 				+ Const.PARAM_SENSORS + "[" + sIndex + "]." + Const.PARAM_NAME,
 				Const.SENSORNAME_MIN_LENGTH, Const.SENSORNAME_MAX_LENGTH);
 	}
 
 	/**
 	 * Validates sensor id parameter and sets the corresponding error message,
 	 * if validation fails.
 	 * 
 	 * @param sensorName
 	 *            Sensor name value of device profile to validate
 	 * @param index
 	 *            Index of the sensor name in the device profile
 	 */
 	public void validateDeviceProfileSensorId(final Integer sensorId,
 			final int sIndex) {
 
 		if (null == sensorId) {
 			validator.addError(Const.PARAM_SID, Const.PARAM_DEVICEPROFILE + "."
 					+ Const.PARAM_SENSORS + "[" + sIndex + "]."
 					+ Const.PARAM_SID + Const.MSG_REQUIRED);
 		} else {
 			validateNumber(sensorId.intValue(), Const.PARAM_DEVICEPROFILE + "."
 					+ Const.PARAM_SENSORS + "[" + sIndex + "]."
 					+ Const.PARAM_SID, Const.SENSORID_MIN_VALUE,
 					Const.SENSORID_MAX_VALUE);
 		}
 	}
 
 	/**
 	 * Validates channel's name parameter and sets the corresponding error
 	 * message, if validation fails.
 	 * 
 	 * @param channelName
 	 *            Sensor name value of device profile to validate
 	 * @param sIndex
 	 *            Index of the sensor inside the device profile
 	 * @param cIndex
 	 *            Index of the channel inside the device profile
 	 */
 	public void validateDeviceProfileChannelName(final String channelName,
 			final int sIndex, final int cIndex) {
 		validateString(channelName,
 				Const.PARAM_DEVICEPROFILE + "." + Const.PARAM_SENSORS + "["
 						+ sIndex + "]" + "." + Const.PARAM_CHANNELS + "["
 						+ cIndex + "]." + Const.PARAM_NAME,
 				Const.CHANNELNAME_MIN_LENGTH, Const.CHANNELNAME_MAX_LENGTH);
 	}
 
 	/**
 	 * Validates channel's type parameter and sets the corresponding error
 	 * message, if validation fails.
 	 * 
 	 * @param channelName
 	 *            Sensor name value of device profile to validate
 	 * @param sIndex
 	 *            Index of the sensor inside the device profile
 	 * @param cIndex
 	 *            Index of the channel inside the device profile
 	 */
 	public void validateDeviceProfileChannelType(final String channelType,
 			final int sIndex, final int cIndex) {
 		validateString(channelType,
 				Const.PARAM_DEVICEPROFILE + "." + Const.PARAM_SENSORS + "["
 						+ sIndex + "]" + "." + Const.PARAM_CHANNELS + "["
 						+ cIndex + "]." + Const.PARAM_TYPE,
 				Const.CHANNELTYPE_MIN_LENGTH, Const.CHANNELTYPE_MAX_LENGTH);
 	}
 
 	/**
 	 * Validates channel's unit parameter and sets the corresponding error
 	 * message, if validation fails.
 	 * 
 	 * @param channelName
 	 *            Sensor name value of device profile to validate
 	 * @param sIndex
 	 *            Index of the sensor inside the device profile
 	 * @param cIndex
 	 *            Index of the channel inside the device profile
 	 */
 	public void validateDeviceProfileChannelUnit(final String channelUnit,
 			final int sIndex, final int cIndex) {
 		validateString(channelUnit,
 				Const.PARAM_DEVICEPROFILE + "." + Const.PARAM_SENSORS + "["
 						+ sIndex + "]" + "." + Const.PARAM_CHANNELS + "["
 						+ cIndex + "]." + Const.PARAM_UNIT,
 				Const.CHANNELUNIT_MIN_LENGTH, Const.CHANNELUNIT_MAX_LENGTH);
 	}
 
 	/**
 	 * Validates channel's sampling period parameter and sets the corresponding
 	 * error message, if validation fails.
 	 * 
 	 * @param samplingRate
 	 *            Sensor name value of device profile to validate
 	 * @param sIndex
 	 *            Index of the sensor inside the device profile
 	 * @param cIndex
 	 *            Index of the channel inside the device profile
 	 */
 	public void validateDeviceProfileChannelSamplingPeriod(
 			final Integer samplingRate, final int sIndex, final int cIndex) {
 
 		if (null == samplingRate) {
 			validator.addError(Const.PARAM_SAMPLING_PERIOD,
 					Const.PARAM_DEVICEPROFILE + "." + Const.PARAM_SENSORS + "["
 							+ sIndex + "]" + "." + Const.PARAM_CHANNELS + "["
 							+ cIndex + "]." + Const.PARAM_SAMPLING_PERIOD
 							+ Const.MSG_REQUIRED);
 		} else {
 			validateNumber(samplingRate.intValue(), Const.PARAM_DEVICEPROFILE
 					+ "." + Const.PARAM_SENSORS + "[" + sIndex + "]" + "."
 					+ Const.PARAM_CHANNELS + "[" + cIndex + "]."
 					+ Const.PARAM_SAMPLING_PERIOD,
 					Const.SAMPLING_PERIOD_MIN_VALUE,
 					Const.SAMPLING_PERIOD_MAX_VALUE);
 		}
 
 	}
 
 	/**
 	 * Validates actuator name parameter and sets the corresponding error
 	 * message, if validation fails.
 	 * 
 	 * @param actuatorName
 	 *            Actuator name value of device profile to validate
 	 * @param aIndex
 	 *            Index of the actuator name in the device profile
 	 */
 	public void validateDeviceProfileActuatorName(final String actuatorName,
 			final int aIndex) {
 		validateString(actuatorName, Const.PARAM_DEVICEPROFILE + "."
 				+ Const.PARAM_ACTUATORS + "[" + aIndex + "]."
 				+ Const.PARAM_NAME, Const.ACTUATORNAME_MIN_LENGTH,
 				Const.ACTUATORNAME_MAX_LENGTH);
 	}
 
 }
