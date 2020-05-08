 /**
  * 
  */
 package de.da_sense.moses.client.abstraction;
 
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 
 import de.da_sense.moses.client.R;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import de.da_sense.moses.client.com.ConnectionParam;
 import de.da_sense.moses.client.com.ReqTaskExecutor;
 import de.da_sense.moses.client.com.NetworkJSON.BackgroundException;
 import de.da_sense.moses.client.com.requests.RequestChangeDeviceIDParameters;
 import de.da_sense.moses.client.com.requests.RequestGetFilter;
 import de.da_sense.moses.client.com.requests.RequestGetHardwareParameters;
 import de.da_sense.moses.client.com.requests.RequestLogin;
 import de.da_sense.moses.client.com.requests.RequestSetFilter;
 import de.da_sense.moses.client.com.requests.RequestSetHardwareParameters;
 import de.da_sense.moses.client.service.MosesService;
 import de.da_sense.moses.client.service.helpers.C2DMManager;
 import de.da_sense.moses.client.service.helpers.EHookTypes;
 import de.da_sense.moses.client.service.helpers.EMessageTypes;
 import de.da_sense.moses.client.service.helpers.Executor;
 
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.hardware.Sensor;
 import android.hardware.SensorManager;
 import android.os.Build;
 import android.os.Handler;
 import android.preference.PreferenceManager;
 import de.da_sense.moses.client.util.Log;
 
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
 		private String vendor;
 		private String model;
 		private List<Integer> sensors;
 
 		public HardwareInfo(String deviceID, String vendor, String model, String sdkbuildversion, List<Integer> sensors) {
 			super();
 			this.deviceID = deviceID;
 			this.sdkbuildversion = sdkbuildversion;
 			this.sensors = sensors;
 			this.vendor = vendor;
 			this.model = model;
 		}
 
 		public String getDeviceVendor() {
 			return vendor;
 		}
 
 		public String getDeviceModel() {
 			return model;
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
 					if (MosesService.getInstance() != null)
 						MosesService.getInstance().setFilter(filter);
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
 			if (c.c == ConnectionParam.EXCEPTION) {
 				handleException(c.e);
 			}
 		}
 	}
 
 	/**
 	 * Get all available sensors from the operating system.
 	 * 
 	 * @return All available sensors on this device
 	 */
 	public static List<Sensor> getSensors() {
 		if (MosesService.getInstance() != null) {
 			List<Sensor> sensors = new ArrayList<Sensor>();
 			SensorManager s = (SensorManager) MosesService.getInstance().getSystemService(Context.SENSOR_SERVICE);
 			for (Sensor sen : s.getSensorList(Sensor.TYPE_ALL))
 				sensors.add(sen);
 
 			return sensors;
 		} else {
 			return null;
 		}
 	}
 
 	private ProgressDialog gethwprogressdialog = null;
 	private Handler handler = new Handler();
 
 	private class ReqClassGetHWParams implements ReqTaskExecutor {
 
 		@Override
 		public void handleException(Exception e) {
 			Log.d("MoSeS.HARDWARE_ABSTRACTION", "FAILURE: " + e.getMessage());
 			gethwprogressdialog.setMessage("Error while retrieving Hardware Informations.");
 			handler.postDelayed(new Runnable() {
 
 				@Override
 				public void run() {
 					gethwprogressdialog.dismiss();
 				}
 			}, 2000);
 		}
 
 		@Override
 		public void postExecution(String s) {
 			JSONObject j = null;
 			try {
 				j = new JSONObject(s);
 				if (RequestGetHardwareParameters.parameterAcquiredFromServer(j)) {
 					StringBuffer sb = new StringBuffer(256);
 					sb.append("Parameters retrived successfully from server");
 					sb.append("\n").append("Device id:").append(j.get("DEVICEID"));
 					sb.append("\n").append("Android version:").append(j.get("ANDVER"));
 					JSONArray sensors = j.getJSONArray("SENSORS");
 					sb.append("\n").append("SENSORS:").append("\n");
 					for (int i = 0; i < sensors.length(); i++) {
 						sb.append("\n");
 						sb.append(ESensor.values()[sensors.getInt(i)]);
 					}
 					Log.d("MoSeS.HARDWARE_ABSTRACTION", sb.toString());
 					AlertDialog ad = new AlertDialog.Builder(appContext).create();
 					ad.setCancelable(false); // This blocks the 'BACK' button
 					ad.setIcon(R.drawable.ic_launcher);
 					ad.setMessage(sb.toString());
 					ad.setButton("OK", new DialogInterface.OnClickListener() {
 						@Override
 						public void onClick(DialogInterface dialog, int which) {
 							dialog.dismiss();
 						}
 					});
 					gethwprogressdialog.dismiss();
 					ad.show();
 				} else {
 					Log.d("MoSeS.HARDWARE_ABSTRACTION", "Parameters NOT retrived successfully from server! :(");
 				}
 			} catch (JSONException e) {
 				this.handleException(e);
 			}
 		}
 
 		@Override
 		public void updateExecution(BackgroundException c) {
 			if (c.c == ConnectionParam.EXCEPTION) {
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
 				if (RequestSetFilter.filterSetOnServer(j)) {
 					Log.d("MoSeS.HARDWARE_ABSTRACTION", "Filter set successfully, server returned positive response");
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
 			if (c.c == ConnectionParam.EXCEPTION) {
 				handleException(c.e);
 			}
 		}
 	}
 
 	private class ReqClassUpdateHWParams implements ReqTaskExecutor {
 
 		@Override
 		public void handleException(Exception e) {
 			Log.d("MoSeS.HARDWARE_ABSTRACTION", "FAILURE: " + e.getMessage());
 			MosesService.getInstance().noOnSharedPreferenceChanged(true);
 			PreferenceManager
 					.getDefaultSharedPreferences(appContext)
 					.edit()
 					.putString(
 							"deviceid_pref",
 							PreferenceManager.getDefaultSharedPreferences(MosesService.getInstance()).getString(
 									"lastdeviceid", "")).commit();
 			MosesService.getInstance().noOnSharedPreferenceChanged(false);
 		}
 
 		@Override
 		public void postExecution(String s) {
 			JSONObject j = null;
 			try {
 				Log.d("MoSeS.HARDWARE_ABSTRACTION", "Received: " + s);
 				j = new JSONObject(s);
 				if (j.getString("STATUS").equals("SUCCESS")) {
 					Log.d("MoSeS.HARDWARE_ABSTRACTION",
 							"Updated device id successfully, server returned positive response");
 					MosesService.getInstance().noOnSharedPreferenceChanged(true);
 					PreferenceManager
 							.getDefaultSharedPreferences(appContext)
 							.edit()
 							.putString(
 									"lastdeviceid",
 									PreferenceManager.getDefaultSharedPreferences(MosesService.getInstance())
 											.getString("deviceid_pref", "")).commit();
 					MosesService.getInstance().noOnSharedPreferenceChanged(false);
 					MosesService.getInstance().executeLoggedIn(EHookTypes.POSTLOGINSUCCESSPRIORITY,
 							EMessageTypes.REQUESTSETHARDWAREPARAMETERS, new Executor() {
 
 								@Override
 								public void execute() {
									syncDeviceInformation(true);
 								}
 							});
 				} else if (j.getString("STATUS").equals("FAILURE_DEVICEID_DUPLICATED")) {
 					showForceDialog(j.getString("VENDOR_NAME"), j.getString("MODEL_NAME"), j.getString("ANDVER"),
 							MosesService.getInstance().getActivityContext(), true);
 				} else if (j.getString("STATUS").equals("FAILURE_DEVICEID_NOT_SET")) {
 					MosesService.getInstance().executeLoggedIn(EHookTypes.POSTLOGINSUCCESSPRIORITY,
 							EMessageTypes.REQUESTSETHARDWAREPARAMETERS, new Executor() {
 
 								@Override
 								public void execute() {
 									syncDeviceInformation(false);
 								}
 							});
 				} else {
 					Log.d("MoSeS.HARDWARE_ABSTRACTION", "Update device id FAILED! Invalid session id.");
 					MosesService.getInstance().noOnSharedPreferenceChanged(true);
 					PreferenceManager
 							.getDefaultSharedPreferences(appContext)
 							.edit()
 							.putString(
 									"deviceid_pref",
 									PreferenceManager.getDefaultSharedPreferences(MosesService.getInstance())
 											.getString("lastdeviceid", "")).commit();
 					MosesService.getInstance().noOnSharedPreferenceChanged(false);
 				}
 			} catch (JSONException e) {
 				this.handleException(e);
 			}
 		}
 
 		@Override
 		public void updateExecution(BackgroundException c) {
 			if (c.c == ConnectionParam.EXCEPTION) {
 				handleException(c.e);
 			}
 		}
 	}
 
 	private class ReqClassSetHWParams implements ReqTaskExecutor {
 
 		@Override
 		public void handleException(Exception e) {
 			Log.d("MoSeS.HARDWARE_ABSTRACTION", "FAILURE: " + e.getMessage());
 			MosesService.getInstance().noOnSharedPreferenceChanged(true);
 			PreferenceManager
 					.getDefaultSharedPreferences(appContext)
 					.edit()
 					.putString(
 							"deviceid_pref",
 							PreferenceManager.getDefaultSharedPreferences(MosesService.getInstance()).getString(
 									"lastdeviceid", "")).commit();
 			MosesService.getInstance().noOnSharedPreferenceChanged(false);
 		}
 
 		@Override
 		public void postExecution(String s) {
 			JSONObject j = null;
 			try {
 				Log.d("MoSeS.HARDWARE", "SetHWParams request response: " + s);
 				j = new JSONObject(s);
 				if (RequestSetHardwareParameters.parameterSetOnServer(j)) {
 					Log.d("MoSeS.HARDWARE_ABSTRACTION",
 							"Parameters set successfully, server returned positive response");
 					MosesService.getInstance().noOnSharedPreferenceChanged(true);
 					PreferenceManager
 							.getDefaultSharedPreferences(appContext)
 							.edit()
 							.putBoolean("deviceidsetsuccessfully", true)
 							.putString(
 									"lastdeviceid",
 									PreferenceManager.getDefaultSharedPreferences(MosesService.getInstance())
 											.getString("deviceid_pref", "")).commit();
 					MosesService.getInstance().noOnSharedPreferenceChanged(false);
 					C2DMManager.sendCurrentC2DM();
 					MosesService.getInstance().uploadFilter();
 				} else if (j.getString("STATUS").equals("FAILURE_DEVICEID_DUPLICATED")) {
 					showForceDialog(j.getString("VENDOR_NAME"), j.getString("MODEL_NAME"), j.getString("ANDVER"),
 							MosesService.getInstance().getActivityContext(), false);
 				} else {
 					Log.d("MoSeS.HARDWARE_ABSTRACTION", "Parameters NOT set successfully! Invalid session id.");
 				}
 			} catch (JSONException e) {
 				this.handleException(e);
 			}
 		}
 
 		@Override
 		public void updateExecution(BackgroundException c) {
 			if (c.c == ConnectionParam.EXCEPTION) {
 				handleException(c.e);
 			}
 		}
 	}
 
 	public void showForceDialog(String vendor, String model, String andver, Context c, boolean update) {
 		AlertDialog a = new AlertDialog.Builder(c).create();
 		a.setIcon(R.drawable.ic_launcher);
 		a.setTitle(R.string.dialog_duplicated_devid_title);
 		a.setMessage(MosesService.getInstance().getString(R.string.dialog_duplicated_devid_text) + "\nVendor: "
 				+ vendor + "\nModel: " + model + "\nSDK Version: " + andver + "\n"
 				+ MosesService.getInstance().getString(R.string.dialog_duplicated_devid_text2));
 		a.setIcon(R.drawable.ic_launcher);
 		if (update) {
 			a.setButton(MosesService.getInstance().getString(R.string.dialog_duplicated_devid_update),
 					new DialogInterface.OnClickListener() {
 
 						@Override
 						public void onClick(DialogInterface arg0, int arg1) {
 							MosesService.getInstance().executeLoggedIn(EHookTypes.POSTLOGINSUCCESSPRIORITY,
 									EMessageTypes.REQUESTUPDATEHARDWAREPARAMETERS, new Executor() {
 
 										@Override
 										public void execute() {
 											HardwareAbstraction.this.changeDeviceID(true);
 										}
 									});
 							arg0.dismiss();
 						}
 					});
 			a.setButton2(MosesService.getInstance().getString(R.string.dialog_duplicated_devid_cancel),
 					new DialogInterface.OnClickListener() {
 
 						@Override
 						public void onClick(DialogInterface arg0, int arg1) {
 							MosesService.getInstance().noOnSharedPreferenceChanged(true);
 							PreferenceManager.getDefaultSharedPreferences(MosesService.getInstance()).edit()
 									.putString("deviceid_pref", "").commit();
 							MosesService.getInstance().noOnSharedPreferenceChanged(false);
 							MosesService.getInstance().notSetDeviceID();
 							arg0.dismiss();
 						}
 
 					});
 		} else {
 			a.setButton(MosesService.getInstance().getString(R.string.dialog_duplicated_devid_update),
 					new DialogInterface.OnClickListener() {
 
 						@Override
 						public void onClick(DialogInterface arg0, int arg1) {
 							MosesService.getInstance().executeLoggedIn(EHookTypes.POSTLOGINSUCCESSPRIORITY,
 									EMessageTypes.REQUESTSETHARDWAREPARAMETERS, new Executor() {
 
 										@Override
 										public void execute() {
 											HardwareAbstraction.this.syncDeviceInformation(true);
 										}
 									});
 							arg0.dismiss();
 						}
 					});
 			a.setButton2(MosesService.getInstance().getString(R.string.dialog_duplicated_devid_cancel),
 					new DialogInterface.OnClickListener() {
 
 						@Override
 						public void onClick(DialogInterface arg0, int arg1) {
 							MosesService.getInstance().noOnSharedPreferenceChanged(true);
 							PreferenceManager.getDefaultSharedPreferences(MosesService.getInstance()).edit()
 									.putString("deviceid_pref", "").commit();
 							MosesService.getInstance().noOnSharedPreferenceChanged(false);
 							MosesService.getInstance().notSetDeviceID();
 							arg0.dismiss();
 						}
 
 					});
 		}
 		a.setIcon(R.drawable.ic_launcher);
 		a.show();
 	}
 
 	private Context appContext;
 
 	public HardwareAbstraction(Context c) {
 		appContext = c;
 	}
 
 	/**
 	 * This method sends a Request to the website for obtainint the filter
 	 * stored for this device
 	 */
 	public void getFilter() {
 
 		if (MosesService.getInstance() != null)
 			MosesService.getInstance().executeLoggedIn(EHookTypes.POSTLOGINSUCCESS, EMessageTypes.REQUESTGETFILTER,
 					new Executor() {
 
 						@Override
 						public void execute() {
 							final RequestGetFilter rGetFilter = new RequestGetFilter(new ReqClassGetFilter(),
 									RequestLogin.getSessionID(), extractDeviceId());
 							rGetFilter.send();
 						}
 					});
 	}
 
 	/**
 	 * This method reads the sensor list stored for the device on the server
 	 */
 	public void getHardwareParameters() {
 		// *** SENDING GET_HARDWARE_PARAMETERS REQUEST TO SERVER ***//
 		if (MosesService.getInstance() != null)
 			MosesService.getInstance().executeLoggedIn(EHookTypes.POSTLOGINSUCCESS,
 					EMessageTypes.REQUESTGETHARDWAREPARAMETERS, new Executor() {
 
 						@Override
 						public void execute() {
 							gethwprogressdialog = new ProgressDialog(appContext);
 							gethwprogressdialog.setTitle("Hardware Informations");
 							gethwprogressdialog.setMessage("Retrieving...");
 							gethwprogressdialog.show();
 							new RequestGetHardwareParameters(new ReqClassGetHWParams(), RequestLogin.getSessionID(),
 									extractDeviceId()).send();
 						}
 					});
 	}
 
 	/**
 	 * This method sends a set_filter Request to the website
 	 */
 	public void setFilter(final String filter) {
 		// *** SENDING GET_HARDWARE_PARAMETERS REQUEST TO SERVER ***//
 
 		if (MosesService.getInstance() != null)
 			MosesService.getInstance().executeLoggedIn(EHookTypes.POSTLOGINSUCCESS, EMessageTypes.REQUESTSETFILTER,
 					new Executor() {
 
 						@Override
 						public void execute() {
 							RequestSetFilter rSetFilter = new RequestSetFilter(new ReqClassSetFilter(), RequestLogin
 									.getSessionID(), extractDeviceId(), filter);
 							rSetFilter.send();
 						}
 					});
 	}
 
 	/**
 	 * This method reads the sensors currently chosen by the user and returns
 	 * them
 	 */
 	private HardwareInfo retrieveHardwareParameters() {
 		// *** SENDING SET_HARDWARE_PARAMETERS REQUEST TO SERVER ***//
 
 		LinkedList<Integer> sensors = new LinkedList<Integer>();
 		SensorManager s = (SensorManager) appContext.getSystemService(Context.SENSOR_SERVICE);
 		for (Sensor sen : s.getSensorList(Sensor.TYPE_ALL)) {
 			sensors.add(sen.getType());
 		}
 
 		return new HardwareInfo(extractDeviceId(), Build.MANUFACTURER, Build.MODEL, Build.VERSION.SDK, sensors);
 	}
 
 	public void sendDeviceInformationToMosesServer(HardwareInfo hardware, String c2dmRegistrationId, String sessionId) {
 	}
 
 	public static String extractDeviceId() {
 		String deviceid = "";
 		if (MosesService.getInstance() != null)
 			deviceid = PreferenceManager.getDefaultSharedPreferences(MosesService.getInstance()).getString(
 					"deviceid_pref", "");
 		return deviceid;
 	}
 
 	public void changeDeviceID(final boolean force) {
 		if (MosesService.getInstance() != null) {
 			if (!PreferenceManager
 					.getDefaultSharedPreferences(MosesService.getInstance())
 					.getString("deviceid_pref", "")
 					.equals(PreferenceManager.getDefaultSharedPreferences(MosesService.getInstance()).getString(
 							"lastdeviceid", ""))) {
 				MosesService.getInstance().executeLoggedIn(EHookTypes.POSTLOGINSUCCESSPRIORITY,
 						EMessageTypes.REQUESTUPDATEHARDWAREPARAMETERS, new Executor() {
 
 							@Override
 							public void execute() {
 								new RequestChangeDeviceIDParameters(new ReqClassUpdateHWParams(), force,
 										PreferenceManager.getDefaultSharedPreferences(MosesService.getInstance())
 												.getString("deviceid_pref", ""), RequestLogin.getSessionID()).send();
 							}
 						});
 			}
 		}
 	}
 
 	/**
 	 * Device informations like hardware parameters, and cloud notification
 	 * (c2dm) identification/connection tokens are sent to the moses server
 	 * 
 	 * @param c2dmRegistrationId
 	 * @param sessionID
 	 */
 	public void syncDeviceInformation(final boolean force) {
 		if (MosesService.getInstance() != null) {
 			MosesService.getInstance().executeLoggedIn(EHookTypes.POSTLOGINSUCCESSPRIORITY,
 					EMessageTypes.REQUESTSETHARDWAREPARAMETERS, new Executor() {
 
 						@Override
 						public void execute() {
 							HardwareInfo hwInfo = retrieveHardwareParameters();
 							new RequestSetHardwareParameters(new ReqClassSetHWParams(), hwInfo, force, RequestLogin
 									.getSessionID()).send();
 						}
 					});
 		}
 	}
 }
