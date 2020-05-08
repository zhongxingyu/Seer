 package nl.sense_os.service;
 
 import java.io.BufferedReader;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URI;
 import java.net.URL;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.zip.GZIPInputStream;
 import java.util.zip.GZIPOutputStream;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.os.Build;
 import android.telephony.TelephonyManager;
 import android.util.Log;
 
 public class SenseApi {
 
     private static final String TAG = "SenseApi";
     private static final long CACHE_REFRESH = 1000l * 60 * 60; // 1 hour
 
     /**
      * Gets the current device ID for use with CommonSense. The device ID is cached in the
      * preferences if it was fetched earlier.
      * 
      * @param context
      *            Context for getting preferences
      * @return the device ID, or -1 if the device is not registered yet, or -2 if an error occurred.
      */
     public static int getDeviceId(Context context) {
 
         final SharedPreferences authPrefs = context.getSharedPreferences(Constants.AUTH_PREFS,
                 Context.MODE_PRIVATE);
 
         // try to get the device ID from the cache
         try {
             int cachedId = authPrefs.getInt(Constants.PREF_DEVICE_ID, -1);
             long cacheTime = authPrefs.getLong(Constants.PREF_DEVICE_ID_TIME, 0);
             boolean isOutdated = System.currentTimeMillis() - cacheTime > CACHE_REFRESH;
 
             // return cached ID of it is still valid
             if (cachedId != -1 && false == isOutdated) {
                 return cachedId;
             }
 
         } catch (Exception e) {
             Log.e(TAG, "Failed to get device ID! Exception while checking cache: ", e);
             return -2; // error return -2
         }
 
         // if we make it here, the device ID was not in the cache
         Log.v(TAG, "Device ID is missing or outdated, refreshing...");
 
         // get phone IMEI, this is used as the device UUID at CommonSense
         final String imei = ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE))
                 .getDeviceId();
 
         try {
             // get list of devices that are already registered at CommonSense for this user
             boolean devMode = authPrefs.getBoolean(Constants.PREF_DEV_MODE, false);
             final URI uri = new URI(devMode ? Constants.URL_DEV_DEVICES : Constants.URL_DEVICES);
             String cookie = authPrefs.getString(Constants.PREF_LOGIN_COOKIE, "NO_COOKIE");
             JSONObject response = SenseApi.getJsonObject(context, uri, cookie);
 
             // check if this device is in the list
             if (response != null) {
                 JSONArray deviceList = response.getJSONArray("devices");
 
                 String uuid = "";
                 for (int x = 0; x < deviceList.length(); x++) {
 
                     JSONObject device = deviceList.getJSONObject(x);
                     uuid = device.getString("uuid");
 
                     // pad the UUID with leading zeros, CommonSense API removes them
                     while (uuid.length() < imei.length()) {
                         uuid = "0" + uuid;
                     }
 
                     // Found the right device if UUID matches IMEI
                     if (uuid.equalsIgnoreCase(imei)) {
 
                         // cache device ID in preferences
                         int deviceId = Integer.parseInt(device.getString("id"));
                         final Editor editor = authPrefs.edit();
                         editor.putString(Constants.PREF_DEVICE_TYPE, device.getString("type"));
                         editor.putInt(Constants.PREF_DEVICE_ID, deviceId);
                         editor.putLong(Constants.PREF_DEVICE_ID_TIME, System.currentTimeMillis());
                         editor.remove(Constants.PREF_SENSOR_LIST);
                         editor.commit();
                         return deviceId;
                     }
                 }
 
                 // if we make it here, the device was not registered yet: return -1
                 Log.w(TAG, "This device is not registered at CommonSense yet");
                 return -1;
 
             } else {
                 // invalid response
                 Log.e(TAG, "Failed to get device ID: invalid device list from CommonSense.");
                 return -2;
             }
 
         } catch (Exception e) {
             Log.e(TAG, "Failed to get device ID: exception communicating wih CommonSense!", e);
             return -2;
 
         }
     }
 
     /**
      * Gets a list of all registered sensors for this device at the CommonSense API. Uses caching
      * for increased performance.
      * 
      * @param context
      *            Application context, used for getting preferences.
      * @return The list of sensors (can be empty), or <code>null</code> if an error occurred and the
      *         list could not be retrieved.
      */
     public static JSONArray getRegisteredSensors(Context context) {
 
         final SharedPreferences authPrefs = context.getSharedPreferences(Constants.AUTH_PREFS,
                 Context.MODE_PRIVATE);
 
         // try to get list of sensors from the cache
         try {
             String cachedSensors = authPrefs.getString(Constants.PREF_SENSOR_LIST, null);
             long cacheTime = authPrefs.getLong(Constants.PREF_SENSOR_LIST_TIME, 0);
             boolean isOutdated = System.currentTimeMillis() - cacheTime > CACHE_REFRESH;
 
             // return cached list of it is still valid
             if (false == isOutdated && null != cachedSensors) {
                 return new JSONArray(cachedSensors);
             }
 
         } catch (Exception e) {
             // should not happen, we are only using stuff that was previously cached
             Log.e(TAG, "Failed to get list of sensors! Exception while checking cache: ", e);
             return null;
         }
 
         // if we make it here, the list was not in the cache
         Log.v(TAG, "List of sensor IDs is missing or outdated, refreshing...");
 
         try {
 
             // get device ID to use in communication with CommonSense
             int deviceId = getDeviceId(context);
             if (deviceId == -1) {
                 // device is not yet registered, so the sensor list is empty
                 Log.w(TAG, "The list of sensors is empty: device is not registered yet.");
                 return new JSONArray("[]");
 
             } else if (deviceId == -2) {
                 // there was an error retrieving info from CommonSense: give up
                 Log.e(TAG, "Problem getting sensor list: failed to get device ID from CommonSense");
                 return null;
             }
 
             // get fresh list of sensors for this device from CommonSense
             String cookie = authPrefs.getString(Constants.PREF_LOGIN_COOKIE, "NO_COOKIE");
             boolean devMode = authPrefs.getBoolean(Constants.PREF_DEV_MODE, false);
             String rawUrl = devMode ? Constants.URL_DEV_SENSORS : Constants.URL_SENSORS;
             URI uri = new URI(rawUrl.replaceAll("<id>", "" + deviceId));
             JSONObject response = SenseApi.getJsonObject(context, uri, cookie);
 
             // parse response and store the list
             if (response != null) {
                 JSONArray sensorList = response.getJSONArray("sensors");
 
                 // store the new sensor list
                 Editor authEditor = authPrefs.edit();
                 authEditor.putString(Constants.PREF_SENSOR_LIST, sensorList.toString());
                 authEditor.putLong(Constants.PREF_SENSOR_LIST_TIME, System.currentTimeMillis());
                 authEditor.commit();
 
                 return sensorList;
 
             } else {
                 Log.e(TAG, "Problem getting list of sensors: invalid response from CommonSense");
                 return null;
             }
 
         } catch (Exception e) {
             Log.e(TAG, "Exception in retrieving registered sensors: ", e);
             return null;
 
         }
     }
 
     /**
      * This method returns the URL to which the data must be send, it does this based on the sensor
      * name and device_type. If the sensor cannot be found, then it will be created.
      */
     public static String getSensorUrl(Context context, String sensorName, String sensorValue,
             String dataType, String deviceType) {
         try {
 
             final SharedPreferences authPrefs = context.getSharedPreferences(Constants.AUTH_PREFS,
                     Context.MODE_PRIVATE);
             final boolean devMode = authPrefs.getBoolean(Constants.PREF_DEV_MODE, false);
 
             // get list of all registered sensors for this device
             JSONArray sensors = getRegisteredSensors(context);
 
             if (null != sensors) {
 
                 // check all the sensors in the list
                 for (int x = 0; x < sensors.length(); x++) {
                     JSONObject sensor = (JSONObject) sensors.get(x);
 
                     if (sensor.getString("device_type").equalsIgnoreCase(deviceType)
                             && sensor.getString("name").equalsIgnoreCase(sensorName)
                             && sensor.getString("data_type").equalsIgnoreCase(dataType)) {
 
                         // found the right sensor
                         if (dataType.equals(Constants.SENSOR_DATA_TYPE_FILE)) {
                             String url = devMode ? Constants.URL_DEV_SENSOR_FILE
                                     : Constants.URL_SENSOR_FILE;
                             return url.replaceFirst("<id>", sensor.getString("id"));
                         } else {
                             String url = devMode ? Constants.URL_DEV_SENSOR_DATA
                                     : Constants.URL_SENSOR_DATA;
                             return url.replaceFirst("<id>", sensor.getString("id"));
                         }
                     }
                 }
             } else {
                 // couldn't get the list of sensors, probably a connection problem: give up
                 Log.w(TAG, "Failed to get URL for sensor '" + sensorName
                         + "': there was an error getting the list of sensors");
                 return null;
             }
 
             /* Sensor not found in current list of sensors, create it at CommonSense */
             String id = registerSensor(context, sensorName, deviceType, dataType, sensorValue);
 
             // create URL with the new sensor ID
             if (dataType.equals(Constants.SENSOR_DATA_TYPE_FILE)) {
                 String url = devMode ? Constants.URL_DEV_SENSOR_FILE : Constants.URL_SENSOR_FILE;
                 return url.replaceFirst("<id>", id);
             } else {
                 String url = devMode ? Constants.URL_DEV_SENSOR_DATA : Constants.URL_SENSOR_DATA;
                 return url.replaceFirst("<id>", id);
             }
 
         } catch (Exception e) {
             Log.e(TAG, "Failed to get URL for sensor '" + sensorName + "': Exception occurred:", e);
             return null;
         }
     }
 
     /**
      * @param hashMe
      *            "clear" password String to be hashed before sending it to CommonSense
      * @return hashed String
      */
     public static String hashPassword(String hashMe) {
         final byte[] unhashedBytes = hashMe.getBytes();
         try {
             final MessageDigest algorithm = MessageDigest.getInstance("MD5");
             algorithm.reset();
             algorithm.update(unhashedBytes);
             final byte[] hashedBytes = algorithm.digest();
 
             final StringBuffer hexString = new StringBuffer();
             for (final byte element : hashedBytes) {
                 final String hex = Integer.toHexString(0xFF & element);
                 if (hex.length() == 1) {
                     hexString.append(0);
                 }
                 hexString.append(hex);
             }
             return hexString.toString();
         } catch (final NoSuchAlgorithmException e) {
             e.printStackTrace();
             return null;
         }
     }
 
     /**
      * Tries to log in at CommonSense using the supplied username and password. After login, the
      * cookie containing the session ID is stored in the preferences.
      * 
      * @param context
      *            Context for getting preferences
      * @param username
      *            username for login
      * @param pass
      *            hashed password for login
      * @return 0 if login completed successfully, -2 if login was forbidden, and -1 for any other
      *         errors.
      */
     public static int login(Context context, String username, String pass) {
         try {
             final SharedPreferences authPrefs = context.getSharedPreferences(Constants.AUTH_PREFS,
                     Context.MODE_PRIVATE);
             final boolean devMode = authPrefs.getBoolean(Constants.PREF_DEV_MODE, false);
 
             final URL url = new URL(devMode ? Constants.URL_DEV_LOGIN : Constants.URL_LOGIN);
             final JSONObject user = new JSONObject();
             user.put("username", username);
             user.put("password", pass);
             final HashMap<String, String> response = sendJson(context, url, user, "POST", "");
             if (response == null) {
                 // request failed
                 return -1;
             }
 
             final Editor authEditor = authPrefs.edit();
 
             // if response code is not 200 (OK), the login was incorrect
             String responseCode = response.get("http response code");
             if ("403".equalsIgnoreCase(responseCode)) {
                 Log.e(TAG, "CommonSense login refused! Response: forbidden!");
                 authEditor.remove(Constants.PREF_LOGIN_COOKIE);
                 authEditor.commit();
                 return -2;
             } else if (!"200".equalsIgnoreCase(responseCode)) {
                 Log.e(TAG, "CommonSense login failed! Response: " + responseCode);
                 authEditor.remove(Constants.PREF_LOGIN_COOKIE);
                 authEditor.commit();
                 return -1;
             }
 
             // if no cookie was returned, something went horribly wrong
             if (response.get("set-cookie") == null) {
                 // incorrect login
                 Log.e(TAG, "CommonSense login failed: no cookie received.");
                 authEditor.remove(Constants.PREF_LOGIN_COOKIE);
                 authEditor.commit();
                 return -1;
             }
 
             // store cookie in the preferences
             String cookie = response.get("set-cookie");
             Log.v(TAG, "CommonSense login OK!");
             authEditor.putString(Constants.PREF_LOGIN_COOKIE, cookie);
             authEditor.commit();
 
             return 0;
 
         } catch (Exception e) {
             Log.e(TAG, "Exception during login: " + e.getMessage());
 
             final SharedPreferences authPrefs = context.getSharedPreferences(Constants.AUTH_PREFS,
                     Context.MODE_PRIVATE);
             final Editor editor = authPrefs.edit();
             editor.remove(Constants.PREF_LOGIN_COOKIE);
             editor.commit();
             return -1;
         }
     }
 
     /**
      * Registers a new sensor for this device at CommonSense. Also connects the sensor to this
      * device.
      * 
      * @param context
      *            The application context, used to retrieve preferences.
      * @param sensorName
      *            The name of the sensor.
      * @param deviceType
      *            The sensor device type.
      * @param dataType
      *            The sensor data type.
      * @param sensorValue
      *            An example sensor value, used to determine the data structure for JSON type
      *            sensors.
      * @return The new sensor ID at CommonSense, or <code>null</code> if the registration failed.
      */
     public static String registerSensor(Context context, String sensorName, String deviceType,
             String dataType, String sensorValue) {
 
         final SharedPreferences authPrefs = context.getSharedPreferences(Constants.AUTH_PREFS,
                 Context.MODE_PRIVATE);
         final boolean devMode = authPrefs.getBoolean(Constants.PREF_DEV_MODE, false);
         String cookie = authPrefs.getString(Constants.PREF_LOGIN_COOKIE, "");
 
         try {
             // prepare request to create new sensor
             URL url = new URL(devMode ? Constants.URL_DEV_CREATE_SENSOR
                     : Constants.URL_CREATE_SENSOR);
             JSONObject postData = new JSONObject();
             JSONObject sensor = new JSONObject();
             sensor.put("name", sensorName);
             sensor.put("device_type", deviceType);
             sensor.put("pager_type", "");
             sensor.put("data_type", dataType);
             if (dataType.compareToIgnoreCase("json") == 0) {
                 JSONObject dataStructJSon = new JSONObject(sensorValue);
                 JSONArray names = dataStructJSon.names();
                 for (int x = 0; x < names.length(); x++) {
                     String name = names.getString(x);
                     int start = dataStructJSon.get(name).getClass().getName().lastIndexOf(".");
                     dataStructJSon.put(name, dataStructJSon.get(name).getClass().getName()
                             .substring(start + 1));
                 }
                 sensor.put("data_structure", dataStructJSon.toString().replaceAll("\"", "\\\""));
             }
             postData.put("sensor", sensor);
 
             // check if sensor was created successfully
             HashMap<String, String> response = sendJson(context, url, postData, "POST", cookie);
             if (response == null) {
                 // failed to create the sensor
                 Log.e(TAG, "Error creating sensor. response=null");
                 return null;
             }
             if (response.get("http response code").compareToIgnoreCase("201") != 0) {
                 String code = response.get("http response code");
                 Log.e(TAG, "Error creating sensor. Got response code: " + code);
                 return null;
             }
 
             // retrieve the newly created sensor ID
             String content = response.get("content");
             JSONObject responseJson = new JSONObject(content);
             JSONObject JSONSensor = responseJson.getJSONObject("sensor");
             final String id = (String) JSONSensor.get("id");
 
             // store the new sensor in the preferences
             JSONArray sensors = getRegisteredSensors(context);
             sensors.put(JSONSensor);
             Editor authEditor = authPrefs.edit();
             authEditor.putString(Constants.PREF_SENSOR_LIST, sensors.toString());
             authEditor.commit();
 
             Log.v(TAG, "-------> Created sensor: \'" + sensorName + "\'");
 
            // get device properties from preferences, so it matches the properties in CommonSense
             final String imei = ((TelephonyManager) context
                     .getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
            final String phoneType = authPrefs.getString(Constants.PREF_PHONE_TYPE, Build.MODEL);
 
             // Add sensor to this device at CommonSense
             String rawUrl = devMode ? Constants.URL_DEV_ADD_SENSOR_TO_DEVICE
                     : Constants.URL_ADD_SENSOR_TO_DEVICE;
             url = new URL(rawUrl.replaceFirst("<id>", id));
             postData = new JSONObject();
             JSONObject device = new JSONObject();
             device.put("type", phoneType);
             device.put("uuid", imei);
             postData.put("device", device);
 
             response = sendJson(context, url, postData, "POST", cookie);
             if (response == null) {
                 // failed to add the sensor to the device
                 Log.e(TAG, "Error adding sensor to device. response=null");
                 return null;
             }
             if (response.get("http response code").compareToIgnoreCase("201") != 0) {
                 String code = response.get("http response code");
                 Log.e(TAG, "Error adding sensor to device. Got response code: " + code);
                 return null;
             }
 
             // return the new sensor ID
             return id;
 
         } catch (MalformedURLException e) {
             Log.e(TAG, "MalformedURLException registering new sensor '" + sensorName + "':", e);
             return null;
         } catch (JSONException e) {
             Log.e(TAG, "JSONException registering new sensor '" + sensorName + "':", e);
             return null;
         } catch (Exception e) {
             Log.e(TAG, "Exception registering new sensor '" + sensorName + "':", e);
             return null;
         }
     }
 
     /**
      * Tries to register a new user at CommonSense. Discards private data of any previous users.
      * 
      * @param context
      *            Context for getting preferences
      * @param username
      *            username to register
      * @param pass
      *            hashed password for the new user
      * @return 0 if registration completed successfully, -2 if the user already exists, and -1
      *         otherwise.
      */
     public static int registerUser(Context context, String username, String pass) {
 
         // clear cached settings of the previous user
         final SharedPreferences authPrefs = context.getSharedPreferences(Constants.AUTH_PREFS,
                 Context.MODE_PRIVATE);
         final Editor authEditor = authPrefs.edit();
         authEditor.remove(Constants.PREF_DEVICE_ID);
         authEditor.remove(Constants.PREF_DEVICE_TYPE);
         authEditor.remove(Constants.PREF_LOGIN_COOKIE);
         authEditor.remove(Constants.PREF_SENSOR_LIST);
         authEditor.commit();
 
         try {
             final boolean devMode = authPrefs.getBoolean(Constants.PREF_DEV_MODE, false);
 
             final URL url = new URL(devMode ? Constants.URL_DEV_REG : Constants.URL_REG);
             final JSONObject data = new JSONObject();
             final JSONObject user = new JSONObject();
             user.put("username", username);
             user.put("password", pass);
             user.put("email", username);
             data.put("user", user);
             final HashMap<String, String> response = SenseApi.sendJson(context, url, data, "POST",
                     "");
             if (response == null) {
                 Log.e(TAG, "Error registering new user. response=null");
                 return -1;
             }
             String responseCode = response.get("http response code");
             if ("201".equalsIgnoreCase(responseCode)) {
                 Log.v(TAG, "CommonSense registration successful");
             } else if ("409".equalsIgnoreCase(responseCode)) {
                 Log.e(TAG, "Error registering new user! User already exists");
                 return -2;
             } else {
                 Log.e(TAG, "Error registering new user! Response code: " + responseCode);
                 return -1;
             }
         } catch (final IOException e) {
             Log.e(TAG, "IOException during registration!", e);
             return -1;
         } catch (final IllegalAccessError e) {
             Log.e(TAG, "IllegalAccessError during registration!", e);
             return -1;
         } catch (JSONException e) {
             Log.e(TAG, "JSONException during registration!", e);
             return -1;
         } catch (Exception e) {
             Log.e(TAG, "Exception during registration!", e);
             return -1;
         }
         return 0;
     }
 
     /**
      * @return a JSONObject from the requested URI
      */
     public static JSONObject getJsonObject(Context context, URI uri, String cookie) {
         try {
 
             final SharedPreferences mainPrefs = context.getSharedPreferences(Constants.MAIN_PREFS,
                     Context.MODE_PRIVATE);
             final boolean compress = mainPrefs.getBoolean(Constants.PREF_COMPRESSION, true);
 
             final HttpGet get = new HttpGet(uri);
             get.setHeader("Cookie", cookie);
             if (compress)
                 get.setHeader("Accept-Encoding", "gzip");
             final HttpClient client = new DefaultHttpClient();
 
             // client.getConnectionManager().closeIdleConnections(2, TimeUnit.SECONDS);
             final HttpResponse response = client.execute(get);
             if (response == null) {
                 return null;
             }
             if (response.getStatusLine().getStatusCode() != 200) {
                 Log.e(TAG, "Error receiving content for " + uri.toString() + ". Status code: "
                         + response.getStatusLine().getStatusCode());
                 return null;
             }
 
             HttpEntity entity = response.getEntity();
             InputStream is = entity.getContent();
             if (compress)
                 is = new GZIPInputStream(is);
 
             BufferedReader rd = new BufferedReader(new InputStreamReader(is), 1024);
             String line;
             StringBuffer responseString = new StringBuffer();
             while ((line = rd.readLine()) != null) {
                 responseString.append(line);
                 responseString.append('\r');
             }
             rd.close();
             return new JSONObject(responseString.toString());
         } catch (Exception e) {
             Log.e(TAG, "Error receiving content for " + uri.toString() + ": " + e.getMessage());
             return null;
         }
     }
 
     /**
      * This method sends a JSON object to update or create an item it returns the HTTP-response code
      */
     public static HashMap<String, String> sendJson(Context context, URL url, JSONObject json,
             String method, String cookie) {
         HttpURLConnection urlConn = null;
         try {
             // Log.d(TAG, "Sending:" + url.toString());
 
             // Open New URL connection channel.
             urlConn = (HttpURLConnection) url.openConnection();
 
             // set post request
             urlConn.setRequestMethod(method);
 
             // Let the run-time system (RTS) know that we want input.
             urlConn.setDoInput(true);
 
             // we want to do output.
             urlConn.setDoOutput(true);
 
             // We want no caching
             urlConn.setUseCaches(false);
 
             // Set content type
             urlConn.setRequestProperty("Content-Type", "application/json");
             urlConn.setInstanceFollowRedirects(false);
 
             // Set cookie
             urlConn.setRequestProperty("Cookie", cookie);
 
             // Send POST output.
             DataOutputStream printout;
 
             // Set compression
             final SharedPreferences mainPrefs = context.getSharedPreferences(Constants.MAIN_PREFS,
                     Context.MODE_PRIVATE);
             final boolean compress = mainPrefs.getBoolean(Constants.PREF_COMPRESSION, true);
             if (compress) {
                 // Don't Set content size
                 urlConn.setRequestProperty("Transfer-Encoding", "chunked");
                 urlConn.setRequestProperty("Content-Encoding", "gzip");
                 GZIPOutputStream zipStream = new GZIPOutputStream(urlConn.getOutputStream());
                 printout = new DataOutputStream(zipStream);
             } else {
                 // Set content size
                 urlConn.setFixedLengthStreamingMode(json.toString().length());
                 urlConn.setRequestProperty("Content-Length", "" + json.toString().length());
                 printout = new DataOutputStream(urlConn.getOutputStream());
             }
 
             printout.writeBytes(json.toString());
             printout.flush();
             printout.close();
 
             // Get Response
             HashMap<String, String> response = new HashMap<String, String>();
             int responseCode = urlConn.getResponseCode();
             response.put("http response code", "" + urlConn.getResponseCode());
 
             // content is only available for 2xx requests
             if (200 <= responseCode && 300 > responseCode) {
                 InputStream is = urlConn.getInputStream();
                 BufferedReader rd = new BufferedReader(new InputStreamReader(is), 1024);
                 String line;
                 StringBuffer responseString = new StringBuffer();
                 while ((line = rd.readLine()) != null) {
                     responseString.append(line);
                     responseString.append('\r');
                 }
                 rd.close();
                 response.put("content", responseString.toString());
             }
 
             // read header fields
             Map<String, List<String>> headerFields = urlConn.getHeaderFields();
             for (Entry<String, List<String>> entry : headerFields.entrySet()) {
                 String key = entry.getKey();
                 List<String> value = entry.getValue();
                 if (null != key && null != value) {
                     key = key.toLowerCase();
                     String valueString = value.toString();
                     valueString = valueString.substring(1, valueString.length() - 1);
                     // Log.d(TAG, "Header field '" + key + "': '" + valueString + "'");
                     response.put(key, valueString);
                 } else {
                     // Log.d(TAG, "Skipped header field '" + key + "': '" + value + "'");
                 }
             }
             return response;
 
         } catch (Exception e) {
             if (null == e.getMessage()) {
                 Log.e(TAG, "Error in posting JSON: " + json.toString(), e);
             } else {
                 // less verbose output
                 Log.e(TAG, "Error in posting JSON: " + json.toString(), e);
             }
             return null;
         } finally {
 
             if (urlConn != null) {
                 urlConn.disconnect();
             }
         }
     }
 }
