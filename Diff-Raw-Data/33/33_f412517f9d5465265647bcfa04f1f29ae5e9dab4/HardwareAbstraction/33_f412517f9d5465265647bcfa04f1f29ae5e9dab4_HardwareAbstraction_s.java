 /**
  * 
  */
 package moses.client.abstraction;
 
 import java.util.LinkedList;
 import java.util.List;
 
 import moses.client.com.ConnectionParam;
 import moses.client.com.NetworkJSON.BackgroundException;
 import moses.client.com.ReqTaskExecutor;
 import moses.client.com.requests.RequestGetFilter;
 import moses.client.com.requests.RequestGetHardwareParameters;
 import moses.client.com.requests.RequestLogin;
 import moses.client.com.requests.RequestSetFilter;
 import moses.client.com.requests.RequestSetHardwareParameters;
 import moses.client.service.MosesService;
 import moses.client.service.MosesService.LocalBinder;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.AlertDialog;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.ServiceConnection;
 import android.hardware.Sensor;
 import android.hardware.SensorManager;
 import android.os.Build;
 import android.os.IBinder;
 import android.util.Log;
 
 /**
  * This class provides basic support for hardware sync with server
  * 
  * @author Jaco Hofmann
  * 
  */
 public class HardwareAbstraction {
 	
 	public static class HardwareInfo {
 		private String deviceID;
 		private String sdkbuildversion;
 		private List<Integer> sensors;
 		public HardwareInfo(String deviceID, String sdkbuildversion, List<Integer> sensors) {
 			super();
 			this.sdkbuildversion = sdkbuildversion;
 			this.sensors = sensors;
 		}
 		public String getDeviceID() {
 			return deviceID;
 		}
 		public String getSdkbuildversion() {
 			return sdkbuildversion;
 		}
 		public List<Integer> getSensors() {
 			return sensors;
 		}
 	}
 	
 	private class ReqClassGetFilter implements ReqTaskExecutor {
 
 		@Override
 		public void handleException(Exception e) {
 			Log.d("MoSeS.HARDWARE_ABSTRACTION", "FAILURE: " + e.getMessage());
 		}
 
 		@Override
 		public void postExecution(String s) {
 			JSONObject j = null;
 			try {
 				j = new JSONObject(s);
 				if (RequestGetFilter.parameterAcquiredFromServer(j)) {
 					JSONArray filter = j.getJSONArray("FILTER");
 					if (mBound) {
 						mService.setFilter(filter);
 						appContext.unbindService(mConnection);
 					}
 				} else {
 					Log.d("MoSeS.HARDWARE_ABSTRACTION",
 							"Parameters NOT retrived successfully! Server returned negative response");
 				}
 			} catch (JSONException e) {
 				this.handleException(e);
 			}
 		}
 
 		@Override
 		public void updateExecution(BackgroundException c) {
 			if (c.c != ConnectionParam.EXCEPTION) {
 				Log.d("MoSeS.HARDWARE_ABSTRACTION", c.c.toString());
 			} else {
 				handleException(c.e);
 			}
 		}
 	}
 
 	private class ReqClassGetHWParams implements ReqTaskExecutor {
 
 		@Override
 		public void handleException(Exception e) {
 			Log.d("MoSeS.HARDWARE_ABSTRACTION", "FAILURE: " + e.getMessage());
 		}
 
 		@Override
 		public void postExecution(String s) {
 			JSONObject j = null;
 			try {
 				j = new JSONObject(s);
 				// TODO handling
 				if (RequestGetHardwareParameters.parameterAcquiredFromServer(j)) {
 					StringBuffer sb = new StringBuffer(256);
 					sb.append("Parameters retrived successfully from server");
 					sb.append("\n").append("Device id:")
 							.append(j.get("DEVICEID"));
 					sb.append("\n").append("Android version:")
 							.append(j.get("ANDVER"));
 					JSONArray sensors = j.getJSONArray("SENSORS");
 					sb.append("\n").append("SENSORS:").append("\n");
 					for (int i = 0; i < sensors.length(); i++) {
 						sb.append("\n");
 						sb.append(ESensor.values()[sensors.getInt(i)]);
 					}
 					Log.d("MoSeS.HARDWARE_ABSTRACTION", sb.toString());
 				} else {
 					Log.d("MoSeS.HARDWARE_ABSTRACTION",
 							"Parameters NOT retrived successfully from server! :(");
 				}
 			} catch (JSONException e) {
 				this.handleException(e);
 			}
 		}
 
 		@Override
 		public void updateExecution(BackgroundException c) {
 			if (c.c != ConnectionParam.EXCEPTION) {
 				Log.d("MoSeS.HARDWARE_ABSTRACTION", c.c.toString());
 			} else {
 				handleException(c.e);
 			}
 		}
 	}
 
 	private class ReqClassSetFilter implements ReqTaskExecutor {
 
 		@Override
 		public void handleException(Exception e) {
 			Log.d("MoSeS.HARDWARE_ABSTRACTION", "FAILURE SETTING FILTER: " + e.getMessage());
 		}
 
 		@Override
 		public void postExecution(String s) {
 			JSONObject j = null;
 			try {
 				j = new JSONObject(s);
 				// TODO handling
 				if (RequestSetFilter.filterSetOnServer(j)) {
 					Log.d("MoSeS.HARDWARE_ABSTRACTION",
 							"Filter set successfully, server returned positive response");
 				} else {
 					Log.d("MoSeS.HARDWARE_ABSTRACTION",
 							"Filter NOT set successfully! Server returned negative response");
 				}
 			} catch (JSONException e) {
 				this.handleException(e);
 			}
 		}
 
 		@Override
 		public void updateExecution(BackgroundException c) {
 			if (c.c != ConnectionParam.EXCEPTION) {
 				Log.d("MoSeS.HARDWARE_ABSTRACTION", c.c.toString());
 			} else {
 				handleException(c.e);
 			}
 		}
 	}
 
 	private class ReqClassSetHWParams implements ReqTaskExecutor {
 
 		@Override
 		public void handleException(Exception e) {
 			Log.d("MoSeS.HARDWARE_ABSTRACTION", "FAILURE: " + e.getMessage());
 		}
 
 		@Override
 		public void postExecution(String s) {
 			JSONObject j = null;
 			try {
 				j = new JSONObject(s);
 				if (RequestSetHardwareParameters.parameterSetOnServer(j)) {
 					Log.d("MoSeS.HARDWARE_ABSTRACTION", "Parameters set successfully, server returned positive response");
 				} else {
 					Log.d("MoSeS.HARDWARE_ABSTRACTION", "Parameters NOT set successfully! Server returned negative response");
 				}
 			} catch (JSONException e) {
 				this.handleException(e);
 			}
 		}
 
 		@Override
 		public void updateExecution(BackgroundException c) {
 			if (c.c != ConnectionParam.EXCEPTION) {
 				Log.d("MoSeS.HARDWARE_ABSTRACTION", c.c.toString());
 			} else {
 				handleException(c.e);
 			}
 		}
 	}
 
 	private Context appContext;
 
 	public HardwareAbstraction(Context c) {
 		appContext = c;
 	}
 
 	/** The m service. */
 	public MosesService mService;
 
 	/** The m bound. */
 	public static boolean mBound = false;
 
 	/** The m connection. */
 	private ServiceConnection mConnection = new ServiceConnection() {
 
 		@Override
 		public void onServiceConnected(ComponentName className, IBinder service) {
 			// We've bound to LocalService, cast the IBinder and get
 			// LocalService instance
 			LocalBinder binder = (LocalBinder) service;
 			mService = binder.getService();
 			mBound = true;
 		}
 
 		@Override
 		public void onServiceDisconnected(ComponentName arg0) {
 			mBound = false;
 		}
 	};
 
 	/**
 	 * This method sends a Request to the website for obtainint the filter
 	 * stored for this device
 	 */
 	public void getFilter() {
 		String sessionID = RequestLogin.getSessionID(); // obtain the session id
 
 		// Connect to the service
 		Intent intent = new Intent(appContext, MosesService.class);
 		appContext.bindService(intent, mConnection, 0);
 
 		RequestGetFilter rGetFilter = new RequestGetFilter(
 				new ReqClassGetFilter(), sessionID, extractDeviceId());
 		rGetFilter.send();
 
 	}
 
 	/**
 	 * This method reads the sensor list stored for the device on the server
 	 */
 	public void getHardwareParameters() {
 		// *** SENDING GET_HARDWARE_PARAMETERS REQUEST TO SERVER ***//
 		String sessionID = RequestLogin.getSessionID(); // obtain the session id
 
 		RequestGetHardwareParameters rGetHWParams = new RequestGetHardwareParameters(
 				new ReqClassGetHWParams(), sessionID, extractDeviceId());
 		rGetHWParams.send();
 	}
 
 	/**
 	 * This method sends a set_filter Request to the website
 	 */
 	public void setFilter(List<Integer> filter) {
 		// *** SENDING GET_HARDWARE_PARAMETERS REQUEST TO SERVER ***//
 		String sessionID = RequestLogin.getSessionID(); // obtain the session id
 
 		RequestSetFilter rSetFilter = new RequestSetFilter(
 				new ReqClassSetFilter(), sessionID, extractDeviceId(), filter);
 		rSetFilter.send();
 	}
 
 	/**
 	 * This method reads the sensors currently chosen by the user and returns them
 	 */
 	private HardwareInfo retrieveHardwareParameters() {
 		// *** SENDING SET_HARDWARE_PARAMETERS REQUEST TO SERVER ***//
 
 		LinkedList<Integer> sensors = new LinkedList<Integer>();
 		SensorManager s = (SensorManager) appContext
 				.getSystemService(Context.SENSOR_SERVICE);
 		for (Sensor sen : s.getSensorList(Sensor.TYPE_ALL)) {
 			sensors.add(sen.getType());
 		}
 
 		String deviceID = extractDeviceId();
 		return new HardwareInfo(deviceID, Build.VERSION.SDK, sensors);
 	}
 	
 	public void sendDeviceInformationToMosesServer(HardwareInfo hardware, String c2dmRegistrationId, String sessionId) {
 	}
 
 	public static String extractDeviceId() {
 		return Build.MANUFACTURER + " " + Build.MODEL + " " + Build.FINGERPRINT;
 	}
 
 	/**
 	 * Device informations like hardware parameters, and cloud notification (c2dm) identification/connection tokens are sent to the moses server
 	 * 
 	 * @param c2dmRegistrationId
 	 * @param sessionID
 	 */
 	public void syncDeviceInformation(String sessionID) {
 		HardwareInfo hwInfo = retrieveHardwareParameters();
 		
 		RequestSetHardwareParameters rSetHWParams = new RequestSetHardwareParameters(new ReqClassSetHWParams(), hwInfo, sessionID);
 		rSetHWParams.send();
 	}
 }
