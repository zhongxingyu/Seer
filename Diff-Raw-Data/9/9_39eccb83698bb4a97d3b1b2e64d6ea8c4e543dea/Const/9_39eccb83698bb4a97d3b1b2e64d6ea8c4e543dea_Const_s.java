 /*
  * Name: Const.java
  * Project: SensorAct, MUC@IIIT-Delhi
  * Version: 1.0
  * Date: 2012-04-14
  * Author: Pandarasamy Arjunan
  */
 package edu.iiitd.muc.sensoract.constants;
 
 /**
  * Defines various constants. 
  * 
  * @author Pandarasamy Arjunan
  * @version 1.0
  */
 
 public class Const {
 
 	/*
 	 * General 
 	 */
 	public static final String SENSORACT = "SensorAct";
 	public static final String APPLICATION_NAME = SENSORACT;
 
 	public static final String TODO = "Yet to implement. Please stay tuned!";
 
 	public static final String MSG_SUCCESS = "Success";
 	public static final String MSG_FAILED = "Failed";
 	public static final String MSG_ERROR = "Error ";
 	public static final String DELIM = ": ";
 
 	public static final int SUCCESS = 0;
 
 	/*
 	 * API names
 	 */
 	public static final String API_USER_LOGIN = "user/login";
 	public static final String API_USER_REGISTER = "user/register";
 
 	public static final String API_DEVICE_ADD = "device/add";
 	public static final String API_DEVICE_DELETE = "device/delete";
 	public static final String API_DEVICE_GET = "device/get";
 	public static final String API_DEVICE_LIST = "device/list";
 	public static final String API_DEVICE_SEARCH = "device/search";
 	public static final String API_DEVICE_SHARE = "device/share";
 	
 	public static final String API_GUARDRULE_ADD = "guardrule/add";
 	public static final String API_GUARDRULE_DELETE = "guardrule/delete";
 	public static final String API_GUARDRULE_GET = "guardrule/get";
 	public static final String API_GUARDRULE_LIST = "guardrule/list";
 	
 	public static final String API_TASK_ADD = "task/add";
 	public static final String API_TASK_DELETE = "task/delete";
 	public static final String API_TASK_GET = "task/get";
 	public static final String API_TASK_LIST = "task/list";
 	
 	public static final String API_DATA_UPLOAD_WAVESEGMENT = "data/upload/wavesegment";
 	public static final String API_DATA_QUERY = "data/query";
 
 	/*
 	 * API parameter names
 	 */
 	public static final String PARAM_USERNAME = "username";
 	public static final String PARAM_PASSWORD = "password";
 	public static final String PARAM_EMAIL = "email";
 	public static final String PARAM_SECRETKEY = "secretkey";
 
 	public static final String PARAM_DEVICEPROFILE = "deviceprofile";
 	public static final String PARAM_NAME = "name";
 	public static final String PARAM_IP = "IP";
 	public static final String PARAM_LOCATION = "location";
 	public static final String PARAM_TAGS = "tags";
 	public static final String PARAM_LATITUDE = "latitude";
 	public static final String PARAM_LONGITUDE = "longitude";
 	public static final String PARAM_SENSORS = "sensors";
 	public static final String PARAM_ACTUATORS = "actuators";	
 	public static final String PARAM_ID = "id";
 	public static final String PARAM_CHANNELS = "channels";
 	public static final String PARAM_TYPE = "type";
 	public static final String PARAM_UNIT = "unit";
 	public static final String PARAM_SAMPLING_PERIOD = "samplingperiod";
 
 	public static final String PARAM_DATA = "data";
 	public static final String PARAM_CHANNEL = "channel";
 	public static final String PARAM_EPOCTIME = "epoctime";
 	public static final String PARAM_READINGS = "readings";
 	public static final String PARAM_DEVICENAME = "devicename";
 
 	/*
 	 * API parameter validation limits
 	 */
 	public static final int USERNAME_MIN_LENGTH = 8;
 	public static final int USERNAME_MAX_LENGTH = 20;
 
 	public static final int PASSWORD_MIN_LENGTH = 8;
 	public static final int PASSWORD_MAX_LENGTH = 20;
 
 	public static final int EMAIL_MIN_LENGTH = 8;
 	public static final int EMAIL_MAX_LENGTH = 40;
 
 	public static final int SECRETKEY_MIN_LENGTH = 32;  // UUID length
 	public static final int SECRETKEY_MAX_LENGTH = 32;
 
 	public static final int DEVICENAME_MIN_LENGTH = 2;
 	public static final int DEVICENAME_MAX_LENGTH = 20;
 
 	public static final int LOCATION_MIN_LENGTH = 2;
 	public static final int LOCATION_MAX_LENGTH = 50;
 
 	public static final int TAGS_MIN_LENGTH = 2;
 	public static final int TAGS_MAX_LENGTH = 100;
 
 	public static final double LATITUDE_MIN_VALUE = -90.0;
 	public static final double LATITUDE_MAX_VALUE = 90.0;
 
 	public static final double LONGITUDE_MIN_VALUE = -180.0;
 	public static final double LONGITUDE_MAX_VALUE = 180.0;
 
 	public static final int SAMPLING_PERIOD_MIN_VALUE = 1;
 	public static final int SAMPLING_PERIOD_MAX_VALUE = 60;
 
 	public static final int DEVICEPROFILENAME_MIN_LENGTH = 2;
 	public static final int DEVICEPROFILENAME_MAX_LENGTH = 20;
 
 	public static final int SENSORNAME_MIN_LENGTH = 2;
 	public static final int SENSORNAME_MAX_LENGTH = 20;
 
 	public static final int SENSORID_MIN_VALUE = 1;
 	public static final int SENSORID_MAX_VALUE = 1000;
 
 	public static final int CHANNELNAME_MIN_LENGTH = 1;
 	public static final int CHANNELNAME_MAX_LENGTH = 20;
 
 	public static final int CHANNELTYPE_MIN_LENGTH = 2;
 	public static final int CHANNELTYPE_MAX_LENGTH = 20;
 
 	public static final int CHANNELUNIT_MIN_LENGTH = 2;
 	public static final int CHANNELUNIT_MAX_LENGTH = 20;
 
 	public static final int ACTUATORNAME_MIN_LENGTH = 2;
 	public static final int ACTUATORNAME_MAX_LENGTH = 20;
 
 	/*
 	 * API parameter validation error messages
 	 */
 	public static final String MSG_REQUIRED = " is required";
 	public static final String MSG_LENGTH = " length must be ";
 	public static final String MSG_MIN_LENGTH = " minimum length is ";
 	public static final String MSG_MAX_LENGTH = " maximum length is ";
 	public static final String MSG_MIN_VALUE = " minimum value is ";
 	public static final String MSG_MAX_VALUE = " maximum value is ";
 	public static final String MSG_INVALID = " is invalid";
 	public static final String MSG_EMPTY = " is empty";
 
 	/*
 	 * API Error messages
 	 */
 	public static final String SYSTEM_ERROR = "System error";
 	public static final String VALIDATION_FAILED = "Validation failed";
 	public static final String INVALID_JSON = "Invalid JSON object";
 	public static final String EMPTY_JSON = "Empty JSON object";
 
 
 	/*
 	 * API success/failure messages
 	 */	
 	public static final String LOGIN_FAILED = "Login failed";
 	public static final String USER_REGISTERED = "New Userprofile registered";
 	public static final String USER_ALREADYEXISTS = "Userprofile already exists";
 
 	public static final String DEVICE_ADDED = "New device added";
 	public static final String DEVICE_ALREADYEXISTS = "Device already exists";
 	public static final String DEVICE_DELETED = "Device successfully deleted";
 	public static final String DEVICE_NOTFOUND = "Device not found";	
 	public static final String DEVICE_NODEVICE_FOUND = "No device found";
 
 	public static final String UNREGISTERED_SECRETKEY = "Unregistered secretkey";
 	public static final String UNREGISTERED_USERNAME = "Unregistered username";
 
 	public static final String UPLOAD_WAVESEGMENT_SUCCESS = "Wavesegment stored successfully";
 
 	private Const() {
 	}
 }
