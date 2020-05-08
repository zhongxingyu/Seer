 /*******************************************************************************
  * Copyright 2012-2013 Trento RISE
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *        http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either   express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  ******************************************************************************/
 package eu.trentorise.smartcampus.jp.helper;
 
 import it.sayservice.platform.smartplanner.data.message.Itinerary;
 import it.sayservice.platform.smartplanner.data.message.alerts.CreatorType;
 import it.sayservice.platform.smartplanner.data.message.journey.RecurrentJourney;
 import it.sayservice.platform.smartplanner.data.message.journey.SingleJourney;
 import it.sayservice.platform.smartplanner.data.message.otpbeans.Route;
 import it.sayservice.platform.smartplanner.data.message.otpbeans.Stop;
 import it.sayservice.platform.smartplanner.data.message.otpbeans.StopTime;
 
 import java.io.IOException;
 import java.text.ParseException;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.codehaus.jackson.JsonParseException;
 import org.codehaus.jackson.map.JsonMappingException;
 import org.codehaus.jackson.type.TypeReference;
 import org.json.JSONException;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.content.res.Resources;
 import android.content.res.TypedArray;
 import android.location.Location;
 import android.location.LocationListener;
 import android.os.Bundle;
 import android.widget.Toast;
 import eu.trentorise.smartcampus.ac.SCAccessProvider;
 import eu.trentorise.smartcampus.ac.authenticator.AMSCAccessProvider;
 import eu.trentorise.smartcampus.android.common.GlobalConfig;
 import eu.trentorise.smartcampus.android.common.LocationHelper;
 import eu.trentorise.smartcampus.jp.Config;
 import eu.trentorise.smartcampus.jp.R;
 import eu.trentorise.smartcampus.jp.custom.data.BasicAlert;
 import eu.trentorise.smartcampus.jp.custom.data.BasicItinerary;
 import eu.trentorise.smartcampus.jp.custom.data.BasicRecurrentJourney;
 import eu.trentorise.smartcampus.jp.custom.data.BasicRecurrentJourneyParameters;
 import eu.trentorise.smartcampus.jp.custom.data.SmartLine;
 import eu.trentorise.smartcampus.jp.custom.data.TimeTable;
 import eu.trentorise.smartcampus.jp.custom.map.MapManager;
 import eu.trentorise.smartcampus.jp.model.ObjectFilter;
 import eu.trentorise.smartcampus.jp.model.ParkingSerial;
 import eu.trentorise.smartcampus.jp.model.SmartCheckRoute;
 import eu.trentorise.smartcampus.jp.model.SmartCheckStop;
 import eu.trentorise.smartcampus.jp.model.SmartCheckTime;
 import eu.trentorise.smartcampus.jp.model.TripData;
 import eu.trentorise.smartcampus.jp.timetable.TTHelper;
 import eu.trentorise.smartcampus.protocolcarrier.ProtocolCarrier;
 import eu.trentorise.smartcampus.protocolcarrier.common.Constants.Method;
 import eu.trentorise.smartcampus.protocolcarrier.custom.MessageRequest;
 import eu.trentorise.smartcampus.protocolcarrier.custom.MessageResponse;
 import eu.trentorise.smartcampus.protocolcarrier.exceptions.ConnectionException;
 import eu.trentorise.smartcampus.protocolcarrier.exceptions.ProtocolException;
 import eu.trentorise.smartcampus.protocolcarrier.exceptions.SecurityException;
 import eu.trentorise.smartcampus.storage.DataException;
 import eu.trentorise.smartcampus.storage.sync.SyncStorage;
 import eu.trentorise.smartcampus.storage.sync.SyncStorageWithPaging;
 
 public class JPHelper {
 
 	private static JPHelper instance = null;
 
 	private static SCAccessProvider accessProvider = new AMSCAccessProvider();
 
 	private static Context mContext;
 
 	private ProtocolCarrier protocolCarrier = null;
 
 	private static LocationHelper mLocationHelper;
 
 	private SyncStorageWithPaging storage = null;
 	
 	//tutorial's stuff
 	
 	private static final String TUT_PREFS= "jp_tut_prefs";
 	private static final String TOUR_PREFS= "jp_wantTour";
 	private static final String FIRST_LAUNCH_PREFS= "jp_firstLaunch";
 	
 	public static enum Tutorial {
 	    PLAN("planTut"),
 	    MONITOR("monitorTut"),
 	    WATCH("watchTut"),
 	    NOTIF("notifTut"),
 	    SEND("sendTut"),
 	    INFO("infoTut"),
 	    PREFST("prefsTut")
 	    ;
 	    /**
 	     * @param text
 	     */
 	    private Tutorial(final String text) {
 	        this.text = text;
 	    }
 
 	    private final String text;
 
 	    /* (non-Javadoc)
 	     * @see java.lang.Enum#toString()
 	     */
 	    @Override
 	    public String toString() {
 	        return text;
 	    }
 	}
 
 	protected JPHelper(Context mContext) {
 		super();
 		JPHelper.mContext = mContext;
 
 		JPParamsHelper.init(mContext);
 		TTHelper.init(mContext);
 		MapManager.initWithParams();
 
 		setProtocolCarrier(new ProtocolCarrier(mContext, JPParamsHelper.getAppToken()));
 
 		// LocationManager locationManager = (LocationManager)
 		// mContext.getSystemService(Context.LOCATION_SERVICE);
 		// locationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER,
 		// 0, 0, new JPLocationListener());
 		// locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
 		// 0, 0, new JPLocationListener());
 		// locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
 		// 0, 0, new JPLocationListener());
 		setLocationHelper(new LocationHelper(mContext));
 	}
 
 	public static void init(Context mContext) {
 		JPParamsHelper.init(mContext);
 		instance = new JPHelper(mContext);
 	}
 
 	public static boolean isInitialized() {
 		return instance != null;
 	}
 
 	public static List<Itinerary> planSingleJourney(SingleJourney sj) throws JsonParseException, JsonMappingException,
 			IOException, ConnectionException, ProtocolException, SecurityException, ParseException {
 		List<Itinerary> list = new ArrayList<Itinerary>();
 
 		if (sj != null) {
 			String json = JSONUtils.convertToJSON(sj);
 			MessageRequest req = new MessageRequest(GlobalConfig.getAppUrl(JPHelper.mContext), Config.TARGET_ADDRESS
 					+ Config.CALL_PLANSINGLEJOURNEY);
 			req.setMethod(Method.POST);
 			req.setBody(json);
 
 			MessageResponse res = JPHelper.instance.getProtocolCarrier().invokeSync(req, JPParamsHelper.getAppToken(),
 					getAuthToken());
 
 			List<?> its = JSONUtils.getFullMapper().readValue(res.getBody(), List.class);
 			for (Object it : its) {
 				Itinerary itinerary = JSONUtils.getFullMapper().convertValue(it, Itinerary.class);
 				list.add(itinerary);
 			}
 
 		}
 
 		return list;
 	}
 
 	public static void saveItinerary(BasicItinerary bi) throws ConnectionException, ProtocolException,
 			SecurityException {
 		if (bi != null) {
 			String json = JSONUtils.convertToJSON(bi);
 			MessageRequest req = new MessageRequest(GlobalConfig.getAppUrl(JPHelper.mContext), Config.TARGET_ADDRESS
 					+ Config.CALL_ITINERARY);
 			req.setMethod(Method.POST);
 			req.setBody(json);
 
 			JPHelper.instance.getProtocolCarrier().invokeSync(req, JPParamsHelper.getAppToken(), getAuthToken());
 		}
 	}
 
 	public static List<BasicItinerary> getMyItineraries() throws ConnectionException, ProtocolException,
 			SecurityException, JSONException, JsonParseException, JsonMappingException, IOException {
 		MessageRequest req = new MessageRequest(GlobalConfig.getAppUrl(JPHelper.mContext), Config.TARGET_ADDRESS
 				+ Config.CALL_ITINERARY);
 		req.setMethod(Method.GET);
 
 		MessageResponse res = JPHelper.instance.getProtocolCarrier().invokeSync(req, JPParamsHelper.getAppToken(),
 				getAuthToken());
 		return eu.trentorise.smartcampus.android.common.Utils.convertJSONToObjects(res.getBody(), BasicItinerary.class);
 
 	}
 
 	public static List<List<Map<String, String>>> getDelay(String routeId, long from_time,long to_time) throws ConnectionException, ProtocolException, SecurityException, JSONException, JsonParseException,
 	JsonMappingException, IOException {
 		String url = Config.TARGET_ADDRESS + Config.CALL_GET_DELAY_TIME_BY_ROUTE + "/" + routeId + "/" + from_time
 		+ "/" + to_time;
 
 		MessageRequest req = new MessageRequest(GlobalConfig.getAppUrl(JPHelper.mContext), url);
 		req.setMethod(Method.GET);
 		req.setQuery("complex=true");
 		
 		MessageResponse res = JPHelper.instance.getProtocolCarrier().invokeSync(req, JPParamsHelper.getAppToken(),
 				getAuthToken());
 
 	return eu.trentorise.smartcampus.android.common.Utils.convertJSONToObject(res.getBody(), TimeTable.class).getDelays();
 	}
 
 	public static void deleteMyItinerary(String id) throws ConnectionException, ProtocolException, SecurityException {
 		if (id != null && id.length() > 0) {
 			MessageRequest req = new MessageRequest(GlobalConfig.getAppUrl(JPHelper.mContext), Config.TARGET_ADDRESS
 					+ Config.CALL_ITINERARY + "/" + id);
 			req.setMethod(Method.DELETE);
 
 			JPHelper.instance.getProtocolCarrier().invokeSync(req, JPParamsHelper.getAppToken(), getAuthToken());
 		}
 	}
 
 	public static boolean monitorMyItinerary(boolean monitor, String id) throws ConnectionException, ProtocolException,
 			SecurityException {
 		MessageResponse res = null;
 
 		if (id != null && id.length() > 0) {
 			MessageRequest req = new MessageRequest(GlobalConfig.getAppUrl(JPHelper.mContext), Config.TARGET_ADDRESS
 					+ Config.CALL_MONITOR + "/" + id + "/" + Boolean.toString(monitor));
 			req.setMethod(Method.GET);
 			req.setBody("");
 
 			res = JPHelper.instance.getProtocolCarrier().invokeSync(req, JPParamsHelper.getAppToken(), getAuthToken());
 
 		}
 		// se cambiato restituisce il valore del monitor
 		return eu.trentorise.smartcampus.android.common.Utils.convertJSONToObject(res.getBody(), Boolean.class);
 	}
 
 	public static boolean monitorMyRecItinerary(boolean monitor, String id) throws ConnectionException,
 			ProtocolException, SecurityException {
 		MessageResponse res = null;
 		if (id != null && id.length() > 0) {
 			MessageRequest req = new MessageRequest(GlobalConfig.getAppUrl(JPHelper.mContext), Config.TARGET_ADDRESS
 					+ Config.CALL_REC_MONITOR + "/" + id + "/" + Boolean.toString(monitor));
 			req.setMethod(Method.GET);
 			req.setBody("");
 
 			res = JPHelper.instance.getProtocolCarrier().invokeSync(req, JPParamsHelper.getAppToken(), getAuthToken());
 
 		}
 		return eu.trentorise.smartcampus.android.common.Utils.convertJSONToObject(res.getBody(), Boolean.class);
 
 	}
 
 	/*
 	 * BUS
 	 */
 	public static List<Route> getRoutesByAgencyId(String agencyId) throws ConnectionException, ProtocolException,
 			SecurityException, JsonParseException, JsonMappingException, IOException {
 		List<Route> list = new ArrayList<Route>();
 
 		MessageRequest req = new MessageRequest(GlobalConfig.getAppUrl(JPHelper.mContext), Config.TARGET_ADDRESS
 				+ Config.CALL_BUS_ROUTES + "/" + agencyId);
 		req.setMethod(Method.GET);
 
 		MessageResponse res = JPHelper.instance.getProtocolCarrier().invokeSync(req, JPParamsHelper.getAppToken(),
 				getAuthToken());
 
 		List<?> routes = JSONUtils.getFullMapper().readValue(res.getBody(), List.class);
 		for (Object r : routes) {
 			Route route = JSONUtils.getFullMapper().convertValue(r, Route.class);
 			list.add(route);
 		}
 
 		return list;
 	}
 
 	public static List<SmartLine> getSmartLinesByAgencyId(String agencyId) throws ConnectionException,
 			ProtocolException, SecurityException, JsonParseException, JsonMappingException, IOException {
 
 		List<Route> list = new ArrayList<Route>();
 		Resources resources = mContext.getResources();
 		String[] lines = resources.getStringArray(R.array.smart_check_12_numbers);
 		TypedArray icons = resources.obtainTypedArray(R.array.smart_check_12_icons);
 		TypedArray colors = resources.obtainTypedArray(R.array.smart_check_12_colors);
 
 		// get info from result (busRoutes)
 		Map<String, List<String>> singleRoutesShorts = new HashMap<String, List<String>>();
 		Map<String, List<String>> singleRoutesLong = new HashMap<String, List<String>>();
 		Map<String, List<String>> singleRoutesId = new HashMap<String, List<String>>();
 		ArrayList<SmartLine> busLines = new ArrayList<SmartLine>();
 		// //prepare the request
 		// MessageRequest req = new MessageRequest(
 		// GlobalConfig.getAppUrl(instance.mContext),
 		// Config.TARGET_ADDRESS + Config.CALL_BUS_ROUTES + "/" + agencyId);
 		// req.setMethod(Method.GET);
 		//
 		// MessageResponse res =
 		// JPHelper.instance.getProtocolCarrier().invokeSync(req,
 		// JPParamsHelper.getAppToken(), getAuthToken());
 		//
 		// //get alle the routes and order them
 		// List<?> routes =
 		// JSONUtils.getFullMapper().readValue(res.getBody(),List.class);
 		// for (Object r : routes) {
 		// Route route = JSONUtils.getFullMapper().convertValue(r, Route.class);
 		// list.add(route);
 		// }
 		list = RoutesHelper.getRoutesList(mContext, new String[] { agencyId });
 		Collections.sort(list, Utils.getRouteComparator());
 
 		// get all-the-routes for a smartline
 		for (int index = 0; index < lines.length; index++) {
 			// put them in the array
 			for (Route route : list) {
 				//
 				if ((route.getId().getId().toUpperCase().compareTo(lines[index].toUpperCase()) == 0)
 						|| route.getId().getId().toUpperCase().compareTo(lines[index].toUpperCase() + "R") == 0
 						|| route.getId().getId().toUpperCase().compareTo(lines[index].toUpperCase() + "A") == 0) {
 					if (singleRoutesShorts.get(lines[index]) == null) {
 						singleRoutesShorts.put(lines[index], new ArrayList<String>());
 						singleRoutesLong.put(lines[index], new ArrayList<String>());
 						singleRoutesId.put(lines[index], new ArrayList<String>());
 
 					}
 					singleRoutesShorts.get(lines[index]).add(route.getRouteShortName());
 					singleRoutesLong.get(lines[index]).add(route.getRouteLongName());
 					singleRoutesId.get(lines[index]).add(route.getId().getId());
 
 				}
 			}
 			SmartLine singleLine = new SmartLine(icons.getDrawable(index), lines[index], colors.getColor(index, 0),
 					singleRoutesShorts.get(lines[index]), singleRoutesLong.get(lines[index]),
 					singleRoutesId.get(lines[index]));
 			busLines.add(singleLine);
 		}
 		return busLines;
 	}
 
 	public static List<Stop> getStopsByAgencyIdRouteId(String agencyId, String routeId) throws ConnectionException,
 			ProtocolException, SecurityException, JsonParseException, JsonMappingException, IOException {
 		List<Stop> list = new ArrayList<Stop>();
 
 		MessageRequest req = new MessageRequest(GlobalConfig.getAppUrl(JPHelper.mContext), Config.TARGET_ADDRESS
 				+ Config.CALL_BUS_STOPS + "/" + agencyId + "/" + routeId);
 		req.setMethod(Method.GET);
 
 		MessageResponse res = JPHelper.instance.getProtocolCarrier().invokeSync(req, JPParamsHelper.getAppToken(),
 				getAuthToken());
 
 		List<?> stops = JSONUtils.getFullMapper().readValue(res.getBody(), List.class);
 		for (Object r : stops) {
 			Stop stop = JSONUtils.getFullMapper().convertValue(r, Stop.class);
 			list.add(stop);
 		}
 
 		return list;
 	}
 
 	public static List<StopTime> getStoptimesByAgencyIdRouteIdStopId(String agencyId, String routeId, String stopId)
 			throws ConnectionException, ProtocolException, SecurityException, JsonParseException, JsonMappingException,
 			IOException {
 		List<StopTime> list = new ArrayList<StopTime>();
 
 		MessageRequest req = new MessageRequest(GlobalConfig.getAppUrl(JPHelper.mContext), Config.TARGET_ADDRESS
 				+ Config.CALL_BUS_STOPTIMES + "/" + agencyId + "/" + routeId + "/" + stopId);
 		req.setMethod(Method.GET);
 
 		MessageResponse res = JPHelper.instance.getProtocolCarrier().invokeSync(req, JPParamsHelper.getAppToken(),
 				getAuthToken());
 
 		List<?> stoptimes = JSONUtils.getFullMapper().readValue(res.getBody(), List.class);
 		for (Object r : stoptimes) {
 			StopTime stoptime = JSONUtils.getFullMapper().convertValue(r, StopTime.class);
 			list.add(stoptime);
 		}
 
 		long now = Calendar.getInstance().getTimeInMillis();
 		List<StopTime> newlist = new ArrayList<StopTime>();
 		for (StopTime st : list) {
 			long newTime = st.getTime() * 1000;
 			if (newTime < now) {
 				st.setTime(newTime);
 				newlist.add(st);
 			}
 		}
 		list = newlist;
 
 		return list;
 	}
 
 	/*
 	 * Alerts
 	 */
 	public static void submitAlert(BasicAlert ba) throws ConnectionException, ProtocolException, SecurityException {
 		if (ba != null) {
 			String json = JSONUtils.convertToJSON(ba);
 			MessageRequest req = new MessageRequest(GlobalConfig.getAppUrl(JPHelper.mContext), Config.TARGET_ADDRESS
 					+ Config.CALL_ALERT_SUBMIT);
 			req.setMethod(Method.POST);
 			req.setBody(json);
 
 			JPHelper.instance.getProtocolCarrier().invokeSync(req, JPParamsHelper.getAppToken(), getAuthToken());
 		}
 	}
 
 	public static String getAuthToken() {
 		return getAccessProvider().readToken(JPHelper.mContext, null);
 	}
 
 	public static JPHelper getInstance() throws DataException {
 		if (instance == null)
 			throw new DataException("JPHelper is not initialized");
 		return instance;
 	}
 
 	public static SCAccessProvider getAccessProvider() {
 		return accessProvider;
 	}
 
 	public static void endAppFailure(Activity activity, int id) {
 		Toast.makeText(activity, activity.getResources().getString(id), Toast.LENGTH_LONG).show();
 		activity.finish();
 	}
 
 	public static void showFailure(Activity activity, int id) {
 		Toast.makeText(activity, activity.getResources().getString(id), Toast.LENGTH_LONG).show();
 	}
 
 	public ProtocolCarrier getProtocolCarrier() {
 		return protocolCarrier;
 	}
 
 	public void setProtocolCarrier(ProtocolCarrier protocolCarrier) {
 		this.protocolCarrier = protocolCarrier;
 	}
 
 	public static LocationHelper getLocationHelper() {
 		if (JPHelper.mLocationHelper == null) {
 			setLocationHelper(new LocationHelper(mContext));
 		}
 		return JPHelper.mLocationHelper;
 	}
 
 	public static void setLocationHelper(LocationHelper mLocationHelper) {
 		JPHelper.mLocationHelper = mLocationHelper;
 	}
 
 	public static Boolean saveRecurrentJourney(BasicRecurrentJourneyParameters brj) throws ConnectionException,
 			ProtocolException, SecurityException {
 		if (brj != null) {
 			String json = JSONUtils.convertToJSON(brj);
 			MessageRequest req = null;
 			if (brj.getClientId() != null) {
 				req = new MessageRequest(GlobalConfig.getAppUrl(JPHelper.mContext), Config.TARGET_ADDRESS
 						+ Config.CALL_RECUR + "/" + brj.getClientId());
 				req.setMethod(Method.PUT);
 			} else {
 				req = new MessageRequest(GlobalConfig.getAppUrl(JPHelper.mContext), Config.TARGET_ADDRESS
 						+ Config.CALL_RECUR);
 				req.setMethod(Method.POST);
 			}
 			req.setBody(json);
 
 			JPHelper.instance.getProtocolCarrier().invokeSync(req, JPParamsHelper.getAppToken(), getAuthToken());
 			return true;
 		}
 		return false;
 	}
 
 	public static void deleteMyRecurItinerary(String id) throws ConnectionException, ProtocolException,
 			SecurityException {
 		if (id != null && id.length() > 0) {
 			MessageRequest req = new MessageRequest(GlobalConfig.getAppUrl(JPHelper.mContext), Config.TARGET_ADDRESS
 					+ Config.CALL_RECUR + "/" + id);
 			req.setMethod(Method.DELETE);
 
 			JPHelper.instance.getProtocolCarrier().invokeSync(req, JPParamsHelper.getAppToken(), getAuthToken());
 		}
 	}
 
 	public static Object getItineraryObject(String objectId) throws ConnectionException, ProtocolException,
 			SecurityException {
 		MessageRequest req = new MessageRequest(GlobalConfig.getAppUrl(JPHelper.mContext), Config.TARGET_ADDRESS
 				+ Config.CALL_ITINERARY + "/" + objectId);
 		req.setMethod(Method.GET);
 
 		MessageResponse res = JPHelper.instance.getProtocolCarrier().invokeSync(req, JPParamsHelper.getAppToken(),
 				getAuthToken());
 		if (res.getBody() != null && res.getBody().length() != 0) {
 			return eu.trentorise.smartcampus.android.common.Utils.convertJSONToObject(res.getBody(),
 					BasicItinerary.class);
 		} else {
 			req = new MessageRequest(GlobalConfig.getAppUrl(JPHelper.mContext), Config.TARGET_ADDRESS
 					+ Config.CALL_GET_RECUR + "/" + objectId);
 			req.setMethod(Method.GET);
 
 			res = instance.getProtocolCarrier().invokeSync(req, JPParamsHelper.getAppToken(), getAuthToken());
 			req.setTargetAddress(Config.TARGET_ADDRESS + Config.CALL_GET_RECUR + "/" + objectId);
 			return eu.trentorise.smartcampus.android.common.Utils.convertJSONToObject(res.getBody(),
 					BasicRecurrentJourney.class);
 		}
 	}
 
 	public class JPLocationListener implements LocationListener {
 		@Override
 		public void onLocationChanged(Location location) {
 		}
 
 		@Override
 		public void onProviderDisabled(String provider) {
 		}
 
 		@Override
 		public void onProviderEnabled(String provider) {
 		}
 
 		@Override
 		public void onStatusChanged(String provider, int status, Bundle extras) {
 		}
 	}
 
 	public static RecurrentJourney planRecurItinerary(BasicRecurrentJourneyParameters brj) throws ConnectionException,
 			ProtocolException, SecurityException {
 
 		// if (brj.getClientId() != null) {
 		String json = JSONUtils.convertToJSON(brj.getData());
 		MessageRequest req = null;
 		if (brj.getClientId() != null) {
 			req = new MessageRequest(GlobalConfig.getAppUrl(JPHelper.mContext), Config.TARGET_ADDRESS
 					+ Config.CALL_PLAN_RECUR + "/" + brj.getClientId());
 			req.setMethod(Method.POST);
 		} else {
 			req = new MessageRequest(GlobalConfig.getAppUrl(JPHelper.mContext), Config.TARGET_ADDRESS
 					+ Config.CALL_PLAN_RECUR);
 			req.setMethod(Method.POST);
 		}
 		req.setBody(json);
 
 		MessageResponse res = JPHelper.instance.getProtocolCarrier().invokeSync(req, JPParamsHelper.getAppToken(),
 				getAuthToken());
 		return eu.trentorise.smartcampus.android.common.Utils
 				.convertJSONToObject(res.getBody(), RecurrentJourney.class);
 		// return
 		// eu.trentorise.smartcampus.android.common.Utils.convertJSONToObject(exammpleRouteString,
 		// RecurrentJourney.class);
 		// }
 		// return
 		// eu.trentorise.smartcampus.android.common.Utils.convertJSONToObject(exammpleRouteString,
 		// RecurrentJourney.class);
 
 	}
 
 	public static Boolean saveMyRecurrentJourney(BasicRecurrentJourney brj) throws ConnectionException,
 			ProtocolException, SecurityException {
 
 		if (brj != null) {
 			String json = JSONUtils.convertToJSON(brj);
 			MessageRequest req = null;
 			if (brj.getClientId() != null) {
 				req = new MessageRequest(GlobalConfig.getAppUrl(JPHelper.mContext), Config.TARGET_ADDRESS
 						+ Config.CALL_SAVE_RECUR + "/" + brj.getClientId());
 				req.setMethod(Method.PUT);
 			} else {
 				req = new MessageRequest(GlobalConfig.getAppUrl(JPHelper.mContext), Config.TARGET_ADDRESS
 						+ Config.CALL_SAVE_RECUR);
 				req.setMethod(Method.POST);
 			}
 			req.setBody(json);
 
 			JPHelper.instance.getProtocolCarrier().invokeSync(req, JPParamsHelper.getAppToken(), getAuthToken());
 			return true;
 		}
 		return false;
 	}
 
 	public static List<BasicRecurrentJourney> getMyRecurItineraries() throws ConnectionException, ProtocolException,
 			SecurityException, JSONException, JsonParseException, JsonMappingException, IOException {
 		MessageRequest req = new MessageRequest(GlobalConfig.getAppUrl(JPHelper.mContext), Config.TARGET_ADDRESS
 				+ Config.CALL_GET_ALL_RECUR);
 		req.setMethod(Method.GET);
 
 		MessageResponse res = JPHelper.instance.getProtocolCarrier().invokeSync(req, JPParamsHelper.getAppToken(),
 				getAuthToken());
 		// return
 		// eu.trentorise.smartcampus.android.common.Utils.convertJSONToObjects(myJourneysString,
 		// BasicRecurrentJourney.class);
 		return eu.trentorise.smartcampus.android.common.Utils.convertJSONToObjects(res.getBody(),
 				BasicRecurrentJourney.class);
 
 	}
 
 	// private static String busTTreturn = "{"+
 	// "\"stops\" : [ \"piazza\", \"via\", \"viale\", \"corso\", \"via\", \"via\", \"via\", \"via\", \"via\", \"via\", \"via\", \"via\", \"via\"],"+
 	// "\"stopsId\" : [ \"axcd\", \"fefrfrg\",\"defr\", \"fefrfrg\",\"defr\", \"fefrfrg\",\"defr\", \"fefrfrg\",\"defr\", \"fefrfrg\",\"defr\", \"fefrfrg\",\"defr\"],"+
 	// "\"times\" : [[[\"12:00\", \"12:05\",\"13:00\", \"12:05\",\"13:00\", \"12:05\",\"13:00\", \"12:05\",\"13:00\", \"12:05\",\"13:00\", \"12:05\",\"13:00\"], [\"12:10\", \"12:05\",\"13:00\", \"12:05\",\"13:00\", \"12:05\",\"13:00\", \"12:05\",\"13:00\", \"12:05\",\"13:00\", \"12:05\",\"13:00\"],[\"12:00\", \"12:05\",\"13:00\", \"12:05\",\"13:00\", \"12:05\",\"13:00\", \"12:05\",\"13:00\", \"12:05\",\"13:00\", \"12:05\",\"13:00\"],[\"12:90\", \"12:05\",\"13:00\", \"12:05\",\"13:00\", \"12:05\",\"13:00\", \"12:05\",\"13:00\", \"12:05\",\"13:00\", \"12:05\",\"13:00\"],[\"12:00\", \"12:05\",\"13:00\", \"12:05\",\"13:00\", \"12:05\",\"13:00\", \"12:05\",\"13:00\", \"12:05\",\"13:00\", \"12:05\",\"13:00\"],[\"12:00\", \"12:05\",\"13:00\", \"12:05\",\"13:00\", \"12:05\",\"13:00\", \"12:05\",\"13:00\", \"12:05\",\"13:00\", \"12:05\",\"13:00\"]],[[\"12:00\", \"12:05\",\"13:00\", \"12:05\",\"13:00\", \"12:05\",\"13:00\", \"12:05\",\"13:00\", \"12:05\",\"13:00\", \"12:05\",\"13:00\"], [\"12:00\", \"12:05\",\"13:00\", \"12:05\",\"13:00\", \"12:05\",\"13:00\", \"12:05\",\"13:00\", \"12:05\",\"13:00\", \"12:05\",\"13:00\"],[\"12:00\", \"12:05\",\"13:00\", \"12:05\",\"13:00\", \"12:05\",\"13:00\", \"12:05\",\"13:00\", \"12:05\",\"13:00\", \"12:05\",\"13:00\"],[\"12:00\", \"12:05\",\"13:00\", \"12:05\",\"13:00\", \"12:05\",\"13:00\", \"12:05\",\"13:00\", \"12:05\",\"13:00\", \"12:05\",\"13:00\"],[\"12:00\", \"12:05\",\"13:00\", \"12:05\",\"13:00\", \"12:05\",\"13:00\", \"12:05\",\"13:00\", \"12:05\",\"13:00\", \"12:05\",\"13:00\"],[\"12:00\", \"12:05\",\"13:00\", \"12:05\",\"13:00\", \"12:05\",\"13:00\", \"12:05\",\"13:00\", \"12:05\",\"13:00\", \"12:05\",\"13:00\"]],[[\"12:00\", \"12:05\",\"13:00\", \"12:05\",\"13:00\", \"12:05\",\"13:00\", \"12:05\",\"13:00\", \"12:05\",\"13:00\", \"12:05\",\"13:00\"], [\"12:00\", \"12:05\",\"13:00\", \"12:05\",\"13:00\", \"12:05\",\"13:00\", \"12:05\",\"13:00\", \"12:05\",\"13:00\", \"12:05\",\"13:00\"],[\"12:00\", \"12:05\",\"13:00\", \"12:05\",\"13:00\", \"12:05\",\"13:00\", \"12:05\",\"13:00\", \"12:05\",\"13:00\", \"12:05\",\"13:00\"],[\"12:00\", \"12:05\",\"13:00\", \"12:05\",\"13:00\", \"12:05\",\"13:00\", \"12:05\",\"13:00\", \"12:05\",\"13:00\", \"12:05\",\"13:00\"],[\"12:00\", \"12:05\",\"13:00\", \"12:05\",\"13:00\", \"12:05\",\"13:00\", \"12:05\",\"13:00\", \"12:05\",\"13:00\", \"12:05\",\"13:00\"],[\"12:00\", \"12:05\",\"13:00\", \"12:05\",\"13:00\", \"12:05\",\"13:00\", \"12:05\",\"13:00\", \"12:05\",\"13:00\", \"12:05\",\"13:00\"]]],"+
 	// "\"delays\" : [[0,5,0,5,7,8],[11,5,1,5,7,8],[22,5,0,5,7,8]]"+
 	// "}";
 	// private static String busTTreturn
 	// ="{\"times\":[[[\"\",\"07:19:00\",\"\",\"06:28:00\",\"06:29:00\",\"06:30:00\",\"06:32:00\",\"06:34:00\",\"06:37:00\",\"06:39:00\",\"06:40:00\",\"06:42:00\",\"\",\"06:44:00\",\"06:45:00\",\"06:47:00\",\"06:49:00\",\"06:52:00\",\"06:53:00\",\"06:55:00\",\"06:56:00\",\"06:57:00\",\"07:01:00\",\"07:04:00\",\"07:06:00\",\"07:10:00\",\"07:12:00\",\"07:11:00\",\"07:16:00\",\"\",\"\",\"07:15:00\",\"\",\"07:18:00\",\"\"],[\"\",\"06:50:00\",\"\",\"\",\"\",\"\",\"\",\"\",\"06:41:00\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"06:35:00\",\"06:37:00\",\"\",\"06:43:00\",\"06:42:00\",\"06:47:00\",\"\",\"\",\"06:46:00\",\"\",\"06:49:00\",\"\"],[\"\",\"07:33:00\",\"\",\"06:42:00\",\"06:43:00\",\"06:44:00\",\"06:46:00\",\"06:48:00\",\"06:51:00\",\"06:53:00\",\"06:54:00\",\"06:56:00\",\"\",\"06:58:00\",\"06:59:00\",\"07:01:00\",\"07:03:00\",\"07:06:00\",\"07:07:00\",\"07:09:00\",\"07:10:00\",\"07:11:00\",\"07:15:00\",\"07:18:00\",\"07:20:00\",\"07:24:00\",\"07:26:00\",\"07:25:00\",\"07:30:00\",\"\",\"\",\"07:29:00\",\"\",\"07:32:00\",\"\"],[\"\",\"06:56:00\",\"\",\"06:56:00\",\"06:57:00\",\"06:58:00\",\"07:00:00\",\"07:02:00\",\"07:05:00\",\"07:07:00\",\"07:08:00\",\"07:10:00\",\"\",\"07:12:00\",\"07:13:00\",\"07:15:00\",\"07:17:00\",\"07:20:00\",\"07:21:00\",\"07:23:00\",\"07:24:00\",\"07:25:00\",\"07:29:00\",\"07:32:00\",\"07:34:00\",\"07:38:00\",\"07:40:00\",\"07:39:00\",\"07:44:00\",\"\",\"\",\"07:43:00\",\"\",\"07:46:00\",\"07:47:00\"],[\"\",\"08:01:00\",\"\",\"07:10:00\",\"07:11:00\",\"07:12:00\",\"07:14:00\",\"07:16:00\",\"07:19:00\",\"07:21:00\",\"07:22:00\",\"07:24:00\",\"\",\"07:26:00\",\"07:27:00\",\"07:29:00\",\"07:31:00\",\"07:34:00\",\"07:35:00\",\"07:37:00\",\"07:38:00\",\"07:39:00\",\"07:43:00\",\"07:46:00\",\"07:48:00\",\"07:52:00\",\"07:54:00\",\"07:53:00\",\"07:58:00\",\"\",\"\",\"07:57:00\",\"\",\"08:00:00\",\"\"],[\"\",\"\",\"\",\"07:11:00\",\"07:12:00\",\"07:13:00\",\"07:15:00\",\"07:17:00\",\"07:20:00\",\"07:22:00\",\"07:23:00\",\"07:25:00\",\"07:30:00\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\"],[\"\",\"07:24:00\",\"\",\"07:24:00\",\"07:25:00\",\"07:26:00\",\"07:28:00\",\"07:30:00\",\"07:33:00\",\"07:35:00\",\"07:36:00\",\"07:38:00\",\"\",\"07:40:00\",\"07:41:00\",\"07:43:00\",\"07:45:00\",\"07:48:00\",\"07:49:00\",\"07:51:00\",\"07:52:00\",\"07:53:00\",\"07:57:00\",\"08:00:00\",\"08:02:00\",\"08:06:00\",\"08:08:00\",\"08:07:00\",\"08:12:00\",\"\",\"\",\"08:11:00\",\"\",\"08:14:00\",\"08:15:00\"],[\"\",\"07:40:00\",\"\",\"07:40:00\",\"07:41:00\",\"07:42:00\",\"07:44:00\",\"07:46:00\",\"07:49:00\",\"07:51:00\",\"07:52:00\",\"07:54:00\",\"\",\"07:56:00\",\"07:57:00\",\"07:59:00\",\"08:01:00\",\"08:04:00\",\"08:05:00\",\"08:07:00\",\"08:08:00\",\"08:09:00\",\"08:13:00\",\"08:16:00\",\"08:18:00\",\"08:22:00\",\"08:24:00\",\"08:23:00\",\"08:28:00\",\"\",\"\",\"08:27:00\",\"\",\"08:30:00\",\"08:31:00\"],[\"\",\"07:58:00\",\"\",\"07:58:00\",\"07:59:00\",\"08:00:00\",\"08:02:00\",\"08:04:00\",\"08:07:00\",\"08:09:00\",\"08:10:00\",\"08:12:00\",\"\",\"08:14:00\",\"08:15:00\",\"08:17:00\",\"08:19:00\",\"08:22:00\",\"08:23:00\",\"08:25:00\",\"08:26:00\",\"08:27:00\",\"08:31:00\",\"08:34:00\",\"08:36:00\",\"08:40:00\",\"08:42:00\",\"08:41:00\",\"08:46:00\",\"\",\"\",\"08:45:00\",\"\",\"08:48:00\",\"08:49:00\"],[\"\",\"08:16:00\",\"\",\"08:16:00\",\"08:17:00\",\"08:18:00\",\"08:20:00\",\"08:22:00\",\"08:25:00\",\"08:27:00\",\"08:28:00\",\"08:30:00\",\"\",\"08:32:00\",\"08:33:00\",\"08:35:00\",\"08:37:00\",\"08:40:00\",\"08:41:00\",\"08:43:00\",\"08:44:00\",\"08:45:00\",\"08:49:00\",\"08:52:00\",\"08:54:00\",\"08:58:00\",\"09:00:00\",\"08:59:00\",\"09:04:00\",\"\",\"\",\"09:03:00\",\"\",\"09:06:00\",\"09:07:00\"],[\"\",\"08:34:00\",\"\",\"08:34:00\",\"08:35:00\",\"08:36:00\",\"08:38:00\",\"08:40:00\",\"08:43:00\",\"08:45:00\",\"08:46:00\",\"08:48:00\",\"\",\"08:50:00\",\"08:51:00\",\"08:53:00\",\"08:55:00\",\"08:58:00\",\"08:59:00\",\"09:01:00\",\"09:02:00\",\"09:03:00\",\"09:07:00\",\"09:10:00\",\"09:12:00\",\"09:16:00\",\"09:18:00\",\"09:17:00\",\"09:22:00\",\"\",\"\",\"09:21:00\",\"\",\"09:24:00\",\"09:25:00\"],[\"\",\"08:52:00\",\"\",\"08:52:00\",\"08:53:00\",\"08:54:00\",\"08:56:00\",\"08:58:00\",\"09:01:00\",\"09:03:00\",\"09:04:00\",\"09:06:00\",\"\",\"09:08:00\",\"09:09:00\",\"09:11:00\",\"09:13:00\",\"09:16:00\",\"09:17:00\",\"09:19:00\",\"09:20:00\",\"09:21:00\",\"09:25:00\",\"09:28:00\",\"09:30:00\",\"09:34:00\",\"09:36:00\",\"09:35:00\",\"09:40:00\",\"\",\"\",\"09:39:00\",\"\",\"09:42:00\",\"09:43:00\"],[\"\",\"09:10:00\",\"\",\"09:10:00\",\"09:11:00\",\"09:12:00\",\"09:14:00\",\"09:16:00\",\"09:19:00\",\"09:21:00\",\"09:22:00\",\"09:24:00\",\"\",\"09:26:00\",\"09:27:00\",\"09:29:00\",\"09:31:00\",\"09:34:00\",\"09:35:00\",\"09:37:00\",\"09:38:00\",\"09:39:00\",\"09:43:00\",\"09:46:00\",\"09:48:00\",\"09:52:00\",\"09:54:00\",\"09:53:00\",\"09:58:00\",\"\",\"\",\"09:57:00\",\"\",\"10:00:00\",\"10:01:00\"],[\"\",\"09:28:00\",\"\",\"09:28:00\",\"09:29:00\",\"09:30:00\",\"09:32:00\",\"09:34:00\",\"09:37:00\",\"09:39:00\",\"09:40:00\",\"09:42:00\",\"\",\"09:44:00\",\"09:45:00\",\"09:47:00\",\"09:49:00\",\"09:52:00\",\"09:53:00\",\"09:55:00\",\"09:56:00\",\"09:57:00\",\"10:01:00\",\"10:04:00\",\"10:06:00\",\"10:10:00\",\"10:12:00\",\"10:11:00\",\"10:16:00\",\"\",\"\",\"10:15:00\",\"\",\"10:18:00\",\"10:19:00\"],[\"\",\"09:46:00\",\"\",\"09:46:00\",\"09:47:00\",\"09:48:00\",\"09:50:00\",\"09:52:00\",\"09:55:00\",\"09:57:00\",\"09:58:00\",\"10:00:00\",\"\",\"10:02:00\",\"10:03:00\",\"10:05:00\",\"10:07:00\",\"10:10:00\",\"10:11:00\",\"10:13:00\",\"10:14:00\",\"10:15:00\",\"10:19:00\",\"10:22:00\",\"10:24:00\",\"10:28:00\",\"10:30:00\",\"10:29:00\",\"10:34:00\",\"\",\"\",\"10:33:00\",\"\",\"10:36:00\",\"10:37:00\"],[\"\",\"10:04:00\",\"\",\"10:04:00\",\"10:05:00\",\"10:06:00\",\"10:08:00\",\"10:10:00\",\"10:13:00\",\"10:15:00\",\"10:16:00\",\"10:18:00\",\"\",\"10:20:00\",\"10:21:00\",\"10:23:00\",\"10:25:00\",\"10:28:00\",\"10:29:00\",\"10:31:00\",\"10:32:00\",\"10:33:00\",\"10:37:00\",\"10:40:00\",\"10:42:00\",\"10:46:00\",\"10:48:00\",\"10:47:00\",\"10:52:00\",\"\",\"\",\"10:51:00\",\"\",\"10:54:00\",\"10:55:00\"],[\"\",\"10:22:00\",\"\",\"10:22:00\",\"10:23:00\",\"10:24:00\",\"10:26:00\",\"10:28:00\",\"10:31:00\",\"10:33:00\",\"10:34:00\",\"10:36:00\",\"\",\"10:38:00\",\"10:39:00\",\"10:41:00\",\"10:43:00\",\"10:46:00\",\"10:47:00\",\"10:49:00\",\"10:50:00\",\"10:51:00\",\"10:55:00\",\"10:58:00\",\"11:00:00\",\"11:04:00\",\"11:06:00\",\"11:05:00\",\"11:10:00\",\"\",\"\",\"11:09:00\",\"\",\"11:12:00\",\"11:13:00\"],[\"\",\"10:40:00\",\"\",\"10:40:00\",\"10:41:00\",\"10:42:00\",\"10:44:00\",\"10:46:00\",\"10:49:00\",\"10:51:00\",\"10:52:00\",\"10:54:00\",\"\",\"10:56:00\",\"10:57:00\",\"10:59:00\",\"11:01:00\",\"11:04:00\",\"11:05:00\",\"11:07:00\",\"11:08:00\",\"11:09:00\",\"11:13:00\",\"11:16:00\",\"11:18:00\",\"11:22:00\",\"11:24:00\",\"11:23:00\",\"11:28:00\",\"\",\"\",\"11:27:00\",\"\",\"11:30:00\",\"11:31:00\"],[\"\",\"10:58:00\",\"\",\"10:58:00\",\"10:59:00\",\"11:00:00\",\"11:02:00\",\"11:04:00\",\"11:07:00\",\"11:09:00\",\"11:10:00\",\"11:12:00\",\"\",\"11:14:00\",\"11:15:00\",\"11:17:00\",\"11:19:00\",\"11:22:00\",\"11:23:00\",\"11:25:00\",\"11:26:00\",\"11:27:00\",\"11:31:00\",\"11:34:00\",\"11:36:00\",\"11:40:00\",\"11:42:00\",\"11:41:00\",\"11:46:00\",\"\",\"\",\"11:45:00\",\"\",\"11:48:00\",\"11:49:00\"],[\"\",\"11:16:00\",\"\",\"11:16:00\",\"11:17:00\",\"11:18:00\",\"11:20:00\",\"11:22:00\",\"11:25:00\",\"11:27:00\",\"11:28:00\",\"11:30:00\",\"\",\"11:32:00\",\"11:33:00\",\"11:35:00\",\"11:37:00\",\"11:40:00\",\"11:41:00\",\"11:43:00\",\"11:44:00\",\"11:45:00\",\"11:49:00\",\"11:52:00\",\"11:54:00\",\"11:58:00\",\"12:00:00\",\"11:59:00\",\"12:04:00\",\"\",\"\",\"12:03:00\",\"\",\"12:06:00\",\"12:07:00\"],[\"\",\"11:34:00\",\"\",\"11:34:00\",\"11:35:00\",\"11:36:00\",\"11:38:00\",\"11:40:00\",\"11:43:00\",\"11:45:00\",\"11:46:00\",\"11:48:00\",\"\",\"11:50:00\",\"11:51:00\",\"11:53:00\",\"11:55:00\",\"11:58:00\",\"11:59:00\",\"12:01:00\",\"12:02:00\",\"12:03:00\",\"12:07:00\",\"12:10:00\",\"12:12:00\",\"12:16:00\",\"12:18:00\",\"12:17:00\",\"12:22:00\",\"\",\"\",\"12:21:00\",\"\",\"12:24:00\",\"12:25:00\"],[\"\",\"11:52:00\",\"\",\"11:52:00\",\"11:53:00\",\"11:54:00\",\"11:56:00\",\"11:58:00\",\"12:01:00\",\"12:03:00\",\"12:04:00\",\"12:06:00\",\"\",\"12:08:00\",\"12:09:00\",\"12:11:00\",\"12:13:00\",\"12:16:00\",\"12:17:00\",\"12:19:00\",\"12:20:00\",\"12:21:00\",\"12:25:00\",\"12:28:00\",\"12:30:00\",\"12:34:00\",\"12:36:00\",\"12:35:00\",\"12:40:00\",\"\",\"\",\"12:39:00\",\"\",\"12:42:00\",\"12:43:00\"],[\"\",\"12:10:00\",\"\",\"12:10:00\",\"12:11:00\",\"12:12:00\",\"12:14:00\",\"12:16:00\",\"12:19:00\",\"12:21:00\",\"12:22:00\",\"12:24:00\",\"\",\"12:26:00\",\"12:27:00\",\"12:29:00\",\"12:31:00\",\"12:34:00\",\"12:35:00\",\"12:37:00\",\"12:38:00\",\"12:39:00\",\"12:43:00\",\"12:46:00\",\"12:48:00\",\"12:52:00\",\"12:54:00\",\"12:53:00\",\"12:58:00\",\"\",\"\",\"12:57:00\",\"\",\"13:00:00\",\"13:01:00\"],[\"\",\"12:28:00\",\"\",\"12:28:00\",\"12:29:00\",\"12:30:00\",\"12:32:00\",\"12:34:00\",\"12:37:00\",\"12:39:00\",\"12:40:00\",\"12:42:00\",\"\",\"12:44:00\",\"12:45:00\",\"12:47:00\",\"12:49:00\",\"12:52:00\",\"12:53:00\",\"12:55:00\",\"12:56:00\",\"12:57:00\",\"13:01:00\",\"13:04:00\",\"13:06:00\",\"13:10:00\",\"13:12:00\",\"13:11:00\",\"13:16:00\",\"\",\"\",\"13:15:00\",\"\",\"13:18:00\",\"13:19:00\"],[\"\",\"12:46:00\",\"\",\"12:46:00\",\"12:47:00\",\"12:48:00\",\"12:50:00\",\"12:52:00\",\"12:55:00\",\"12:57:00\",\"12:58:00\",\"13:00:00\",\"\",\"13:02:00\",\"13:03:00\",\"13:05:00\",\"13:07:00\",\"13:10:00\",\"13:11:00\",\"13:13:00\",\"13:14:00\",\"13:15:00\",\"13:19:00\",\"13:22:00\",\"13:24:00\",\"13:28:00\",\"13:30:00\",\"13:29:00\",\"13:34:00\",\"\",\"\",\"13:33:00\",\"\",\"13:36:00\",\"13:37:00\"],[\"\",\"13:04:00\",\"\",\"13:04:00\",\"13:05:00\",\"13:06:00\",\"13:08:00\",\"13:10:00\",\"13:13:00\",\"13:15:00\",\"13:16:00\",\"13:18:00\",\"\",\"13:20:00\",\"13:21:00\",\"13:23:00\",\"13:25:00\",\"13:28:00\",\"13:29:00\",\"13:31:00\",\"13:32:00\",\"13:33:00\",\"13:37:00\",\"13:40:00\",\"13:42:00\",\"13:46:00\",\"13:48:00\",\"13:47:00\",\"13:52:00\",\"\",\"\",\"13:51:00\",\"\",\"13:54:00\",\"13:55:00\"],[\"\",\"13:22:00\",\"\",\"13:22:00\",\"13:23:00\",\"13:24:00\",\"13:26:00\",\"13:28:00\",\"13:31:00\",\"13:33:00\",\"13:34:00\",\"13:36:00\",\"\",\"13:38:00\",\"13:39:00\",\"13:41:00\",\"13:43:00\",\"13:46:00\",\"13:47:00\",\"13:49:00\",\"13:50:00\",\"13:51:00\",\"13:55:00\",\"13:58:00\",\"14:00:00\",\"14:04:00\",\"14:06:00\",\"14:05:00\",\"14:10:00\",\"\",\"\",\"14:09:00\",\"\",\"14:12:00\",\"14:13:00\"],[\"\",\"13:40:00\",\"\",\"13:40:00\",\"13:41:00\",\"13:42:00\",\"13:44:00\",\"13:46:00\",\"13:49:00\",\"13:51:00\",\"13:52:00\",\"13:54:00\",\"\",\"13:56:00\",\"13:57:00\",\"13:59:00\",\"14:01:00\",\"14:04:00\",\"14:05:00\",\"14:07:00\",\"14:08:00\",\"14:09:00\",\"14:13:00\",\"14:16:00\",\"14:18:00\",\"14:22:00\",\"14:24:00\",\"14:23:00\",\"14:28:00\",\"\",\"\",\"14:27:00\",\"\",\"14:30:00\",\"14:31:00\"],[\"\",\"13:58:00\",\"\",\"13:58:00\",\"13:59:00\",\"14:00:00\",\"14:02:00\",\"14:04:00\",\"14:07:00\",\"14:09:00\",\"14:10:00\",\"14:12:00\",\"\",\"14:14:00\",\"14:15:00\",\"14:17:00\",\"14:19:00\",\"14:22:00\",\"14:23:00\",\"14:25:00\",\"14:26:00\",\"14:27:00\",\"14:31:00\",\"14:34:00\",\"14:36:00\",\"14:40:00\",\"14:42:00\",\"14:41:00\",\"14:46:00\",\"\",\"\",\"14:45:00\",\"\",\"14:48:00\",\"14:49:00\"],[\"\",\"14:16:00\",\"\",\"14:16:00\",\"14:17:00\",\"14:18:00\",\"14:20:00\",\"14:22:00\",\"14:25:00\",\"14:27:00\",\"14:28:00\",\"14:30:00\",\"\",\"14:32:00\",\"14:33:00\",\"14:35:00\",\"14:37:00\",\"14:40:00\",\"14:41:00\",\"14:43:00\",\"14:44:00\",\"14:45:00\",\"14:49:00\",\"14:52:00\",\"14:54:00\",\"14:58:00\",\"15:00:00\",\"14:59:00\",\"15:04:00\",\"\",\"\",\"15:03:00\",\"\",\"15:06:00\",\"15:07:00\"],[\"\",\"14:34:00\",\"\",\"14:34:00\",\"14:35:00\",\"14:36:00\",\"14:38:00\",\"14:40:00\",\"14:43:00\",\"14:45:00\",\"14:46:00\",\"14:48:00\",\"\",\"14:50:00\",\"14:51:00\",\"14:53:00\",\"14:55:00\",\"14:58:00\",\"14:59:00\",\"15:01:00\",\"15:02:00\",\"15:03:00\",\"15:07:00\",\"15:10:00\",\"15:12:00\",\"15:16:00\",\"15:18:00\",\"15:17:00\",\"15:22:00\",\"\",\"\",\"15:21:00\",\"\",\"15:24:00\",\"15:25:00\"],[\"\",\"14:52:00\",\"\",\"14:52:00\",\"14:53:00\",\"14:54:00\",\"14:56:00\",\"14:58:00\",\"15:01:00\",\"15:03:00\",\"15:04:00\",\"15:06:00\",\"\",\"15:08:00\",\"15:09:00\",\"15:11:00\",\"15:13:00\",\"15:16:00\",\"15:17:00\",\"15:19:00\",\"15:20:00\",\"15:21:00\",\"15:25:00\",\"15:28:00\",\"15:30:00\",\"15:34:00\",\"15:36:00\",\"15:35:00\",\"15:40:00\",\"\",\"\",\"15:39:00\",\"\",\"15:42:00\",\"15:43:00\"],[\"\",\"15:10:00\",\"\",\"15:10:00\",\"15:11:00\",\"15:12:00\",\"15:14:00\",\"15:16:00\",\"15:19:00\",\"15:21:00\",\"15:22:00\",\"15:24:00\",\"\",\"15:26:00\",\"15:27:00\",\"15:29:00\",\"15:31:00\",\"15:34:00\",\"15:35:00\",\"15:37:00\",\"15:38:00\",\"15:39:00\",\"15:43:00\",\"15:46:00\",\"15:48:00\",\"15:52:00\",\"15:54:00\",\"15:53:00\",\"15:58:00\",\"\",\"\",\"15:57:00\",\"\",\"16:00:00\",\"16:01:00\"],[\"\",\"15:28:00\",\"\",\"15:28:00\",\"15:29:00\",\"15:30:00\",\"15:32:00\",\"15:34:00\",\"15:37:00\",\"15:39:00\",\"15:40:00\",\"15:42:00\",\"\",\"15:44:00\",\"15:45:00\",\"15:47:00\",\"15:49:00\",\"15:52:00\",\"15:53:00\",\"15:55:00\",\"15:56:00\",\"15:57:00\",\"16:01:00\",\"16:04:00\",\"16:06:00\",\"16:10:00\",\"16:12:00\",\"16:11:00\",\"16:16:00\",\"\",\"\",\"16:15:00\",\"\",\"16:18:00\",\"16:19:00\"],[\"\",\"15:46:00\",\"\",\"15:46:00\",\"15:47:00\",\"15:48:00\",\"15:50:00\",\"15:52:00\",\"15:55:00\",\"15:57:00\",\"15:58:00\",\"16:00:00\",\"\",\"16:02:00\",\"16:03:00\",\"16:05:00\",\"16:07:00\",\"16:10:00\",\"16:11:00\",\"16:13:00\",\"16:14:00\",\"16:15:00\",\"16:19:00\",\"16:22:00\",\"16:24:00\",\"16:28:00\",\"16:30:00\",\"16:29:00\",\"16:34:00\",\"\",\"\",\"16:33:00\",\"\",\"16:36:00\",\"16:37:00\"],[\"\",\"16:04:00\",\"\",\"16:04:00\",\"16:05:00\",\"16:06:00\",\"16:08:00\",\"16:10:00\",\"16:13:00\",\"16:15:00\",\"16:16:00\",\"16:18:00\",\"\",\"16:20:00\",\"16:21:00\",\"16:23:00\",\"16:25:00\",\"16:28:00\",\"16:29:00\",\"16:31:00\",\"16:32:00\",\"16:33:00\",\"16:37:00\",\"16:40:00\",\"16:42:00\",\"16:46:00\",\"16:48:00\",\"16:47:00\",\"16:52:00\",\"\",\"\",\"16:51:00\",\"\",\"16:54:00\",\"16:55:00\"],[\"\",\"16:22:00\",\"\",\"16:22:00\",\"16:23:00\",\"16:24:00\",\"16:26:00\",\"16:28:00\",\"16:31:00\",\"16:33:00\",\"16:34:00\",\"16:36:00\",\"\",\"16:38:00\",\"16:39:00\",\"16:41:00\",\"16:43:00\",\"16:46:00\",\"16:47:00\",\"16:49:00\",\"16:50:00\",\"16:51:00\",\"16:55:00\",\"16:58:00\",\"17:00:00\",\"17:04:00\",\"17:06:00\",\"17:05:00\",\"17:10:00\",\"\",\"\",\"17:09:00\",\"\",\"17:12:00\",\"17:13:00\"],[\"\",\"16:40:00\",\"\",\"16:40:00\",\"16:41:00\",\"16:42:00\",\"16:44:00\",\"16:46:00\",\"16:49:00\",\"16:51:00\",\"16:52:00\",\"16:54:00\",\"\",\"16:56:00\",\"16:57:00\",\"16:59:00\",\"17:01:00\",\"17:04:00\",\"17:05:00\",\"17:07:00\",\"17:08:00\",\"17:09:00\",\"17:13:00\",\"17:16:00\",\"17:18:00\",\"17:22:00\",\"17:24:00\",\"17:23:00\",\"17:28:00\",\"\",\"\",\"17:27:00\",\"\",\"17:30:00\",\"17:31:00\"],[\"\",\"16:58:00\",\"\",\"16:58:00\",\"16:59:00\",\"17:00:00\",\"17:02:00\",\"17:04:00\",\"17:07:00\",\"17:09:00\",\"17:10:00\",\"17:12:00\",\"\",\"17:14:00\",\"17:15:00\",\"17:17:00\",\"17:19:00\",\"17:22:00\",\"17:23:00\",\"17:25:00\",\"17:26:00\",\"17:27:00\",\"17:31:00\",\"17:34:00\",\"17:36:00\",\"17:40:00\",\"17:42:00\",\"17:41:00\",\"17:46:00\",\"\",\"\",\"17:45:00\",\"\",\"17:48:00\",\"17:49:00\"],[\"\",\"17:16:00\",\"\",\"17:16:00\",\"17:17:00\",\"17:18:00\",\"17:20:00\",\"17:22:00\",\"17:25:00\",\"17:27:00\",\"17:28:00\",\"17:30:00\",\"\",\"17:32:00\",\"17:33:00\",\"17:35:00\",\"17:37:00\",\"17:40:00\",\"17:41:00\",\"17:43:00\",\"17:44:00\",\"17:45:00\",\"17:49:00\",\"17:52:00\",\"17:54:00\",\"17:58:00\",\"18:00:00\",\"17:59:00\",\"18:04:00\",\"\",\"\",\"18:03:00\",\"\",\"18:06:00\",\"18:07:00\"],[\"\",\"17:34:00\",\"\",\"17:34:00\",\"17:35:00\",\"17:36:00\",\"17:38:00\",\"17:40:00\",\"17:43:00\",\"17:45:00\",\"17:46:00\",\"17:48:00\",\"\",\"17:50:00\",\"17:51:00\",\"17:53:00\",\"17:55:00\",\"17:58:00\",\"17:59:00\",\"18:01:00\",\"18:02:00\",\"18:03:00\",\"18:07:00\",\"18:10:00\",\"18:12:00\",\"18:16:00\",\"18:18:00\",\"18:17:00\",\"18:22:00\",\"\",\"\",\"18:21:00\",\"\",\"18:24:00\",\"18:25:00\"],[\"\",\"17:52:00\",\"\",\"17:52:00\",\"17:53:00\",\"17:54:00\",\"17:56:00\",\"17:58:00\",\"18:01:00\",\"18:03:00\",\"18:04:00\",\"18:06:00\",\"\",\"18:08:00\",\"18:09:00\",\"18:11:00\",\"18:13:00\",\"18:16:00\",\"18:17:00\",\"18:19:00\",\"18:20:00\",\"18:21:00\",\"18:25:00\",\"18:28:00\",\"18:30:00\",\"18:34:00\",\"18:36:00\",\"18:35:00\",\"18:40:00\",\"\",\"\",\"18:39:00\",\"\",\"18:42:00\",\"18:43:00\"],[\"\",\"18:10:00\",\"\",\"18:10:00\",\"18:11:00\",\"18:12:00\",\"18:14:00\",\"18:16:00\",\"18:19:00\",\"18:21:00\",\"18:22:00\",\"18:24:00\",\"\",\"18:26:00\",\"18:27:00\",\"18:29:00\",\"18:31:00\",\"18:34:00\",\"18:35:00\",\"18:37:00\",\"18:38:00\",\"18:39:00\",\"18:43:00\",\"18:46:00\",\"18:48:00\",\"18:52:00\",\"18:54:00\",\"18:53:00\",\"18:58:00\",\"\",\"\",\"18:57:00\",\"\",\"19:00:00\",\"19:01:00\"],[\"\",\"18:28:00\",\"\",\"18:28:00\",\"18:29:00\",\"18:30:00\",\"18:32:00\",\"18:34:00\",\"18:37:00\",\"18:39:00\",\"18:40:00\",\"18:42:00\",\"\",\"18:44:00\",\"18:45:00\",\"18:47:00\",\"18:49:00\",\"18:52:00\",\"18:53:00\",\"18:55:00\",\"18:56:00\",\"18:57:00\",\"19:01:00\",\"19:04:00\",\"19:06:00\",\"19:10:00\",\"19:12:00\",\"19:11:00\",\"19:16:00\",\"\",\"\",\"19:15:00\",\"\",\"19:18:00\",\"19:19:00\"],[\"\",\"18:46:00\",\"\",\"18:46:00\",\"18:47:00\",\"18:48:00\",\"18:50:00\",\"18:52:00\",\"18:55:00\",\"18:57:00\",\"18:58:00\",\"19:00:00\",\"\",\"19:02:00\",\"19:03:00\",\"19:05:00\",\"19:07:00\",\"19:10:00\",\"19:11:00\",\"19:13:00\",\"19:14:00\",\"19:15:00\",\"19:19:00\",\"19:22:00\",\"19:24:00\",\"19:28:00\",\"19:30:00\",\"19:29:00\",\"19:34:00\",\"\",\"\",\"19:33:00\",\"\",\"19:36:00\",\"19:37:00\"],[\"\",\"19:04:00\",\"\",\"19:04:00\",\"19:05:00\",\"19:06:00\",\"19:08:00\",\"19:10:00\",\"19:13:00\",\"19:15:00\",\"19:16:00\",\"19:18:00\",\"\",\"19:20:00\",\"19:21:00\",\"19:23:00\",\"19:25:00\",\"19:28:00\",\"19:29:00\",\"19:31:00\",\"19:32:00\",\"19:33:00\",\"19:37:00\",\"19:40:00\",\"19:42:00\",\"19:46:00\",\"19:48:00\",\"19:47:00\",\"19:52:00\",\"\",\"\",\"19:51:00\",\"\",\"19:54:00\",\"19:55:00\"],[\"\",\"19:22:00\",\"\",\"19:22:00\",\"19:23:00\",\"19:24:00\",\"19:26:00\",\"19:28:00\",\"19:31:00\",\"19:33:00\",\"19:34:00\",\"19:36:00\",\"\",\"19:38:00\",\"19:39:00\",\"19:41:00\",\"19:43:00\",\"19:46:00\",\"19:47:00\",\"19:49:00\",\"19:50:00\",\"19:51:00\",\"19:55:00\",\"19:58:00\",\"20:00:00\",\"20:04:00\",\"20:06:00\",\"20:05:00\",\"20:10:00\",\"\",\"\",\"20:09:00\",\"\",\"20:12:00\",\"20:13:00\"],[\"\",\"19:40:00\",\"\",\"19:40:00\",\"19:41:00\",\"19:42:00\",\"19:44:00\",\"19:46:00\",\"19:49:00\",\"19:51:00\",\"19:52:00\",\"19:54:00\",\"\",\"19:56:00\",\"19:57:00\",\"19:59:00\",\"20:01:00\",\"20:04:00\",\"20:05:00\",\"20:07:00\",\"20:08:00\",\"20:09:00\",\"20:13:00\",\"20:16:00\",\"20:18:00\",\"20:22:00\",\"20:24:00\",\"20:23:00\",\"20:28:00\",\"\",\"\",\"20:27:00\",\"\",\"20:30:00\",\"20:31:00\"],[\"\",\"20:04:00\",\"\",\"20:04:00\",\"20:05:00\",\"20:06:00\",\"20:08:00\",\"20:10:00\",\"20:13:00\",\"20:15:00\",\"20:16:00\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\"],[\"\",\"20:22:00\",\"\",\"20:22:00\",\"20:23:00\",\"20:24:00\",\"20:26:00\",\"20:28:00\",\"20:31:00\",\"20:33:00\",\"20:34:00\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\"]],[[\"\",\"07:19:00\",\"\",\"06:28:00\",\"06:29:00\",\"06:30:00\",\"06:32:00\",\"06:34:00\",\"06:37:00\",\"06:39:00\",\"06:40:00\",\"06:42:00\",\"\",\"06:44:00\",\"06:45:00\",\"06:47:00\",\"06:49:00\",\"06:52:00\",\"06:53:00\",\"06:55:00\",\"06:56:00\",\"06:57:00\",\"07:01:00\",\"07:04:00\",\"07:06:00\",\"07:10:00\",\"07:12:00\",\"07:11:00\",\"07:16:00\",\"\",\"\",\"07:15:00\",\"\",\"07:18:00\",\"\"],[\"\",\"06:50:00\",\"\",\"\",\"\",\"\",\"\",\"\",\"06:41:00\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"06:35:00\",\"06:37:00\",\"\",\"06:43:00\",\"06:42:00\",\"06:47:00\",\"\",\"\",\"06:46:00\",\"\",\"06:49:00\",\"\"],[\"\",\"07:33:00\",\"\",\"06:42:00\",\"06:43:00\",\"06:44:00\",\"06:46:00\",\"06:48:00\",\"06:51:00\",\"06:53:00\",\"06:54:00\",\"06:56:00\",\"\",\"06:58:00\",\"06:59:00\",\"07:01:00\",\"07:03:00\",\"07:06:00\",\"07:07:00\",\"07:09:00\",\"07:10:00\",\"07:11:00\",\"07:15:00\",\"07:18:00\",\"07:20:00\",\"07:24:00\",\"07:26:00\",\"07:25:00\",\"07:30:00\",\"\",\"\",\"07:29:00\",\"\",\"07:32:00\",\"\"],[\"\",\"06:56:00\",\"\",\"06:56:00\",\"06:57:00\",\"06:58:00\",\"07:00:00\",\"07:02:00\",\"07:05:00\",\"07:07:00\",\"07:08:00\",\"07:10:00\",\"\",\"07:12:00\",\"07:13:00\",\"07:15:00\",\"07:17:00\",\"07:20:00\",\"07:21:00\",\"07:23:00\",\"07:24:00\",\"07:25:00\",\"07:29:00\",\"07:32:00\",\"07:34:00\",\"07:38:00\",\"07:40:00\",\"07:39:00\",\"07:44:00\",\"\",\"\",\"07:43:00\",\"\",\"07:46:00\",\"07:47:00\"],[\"\",\"08:01:00\",\"\",\"07:10:00\",\"07:11:00\",\"07:12:00\",\"07:14:00\",\"07:16:00\",\"07:19:00\",\"07:21:00\",\"07:22:00\",\"07:24:00\",\"\",\"07:26:00\",\"07:27:00\",\"07:29:00\",\"07:31:00\",\"07:34:00\",\"07:35:00\",\"07:37:00\",\"07:38:00\",\"07:39:00\",\"07:43:00\",\"07:46:00\",\"07:48:00\",\"07:52:00\",\"07:54:00\",\"07:53:00\",\"07:58:00\",\"\",\"\",\"07:57:00\",\"\",\"08:00:00\",\"\"],[\"\",\"\",\"\",\"07:11:00\",\"07:12:00\",\"07:13:00\",\"07:15:00\",\"07:17:00\",\"07:20:00\",\"07:22:00\",\"07:23:00\",\"07:25:00\",\"07:30:00\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\"],[\"\",\"07:24:00\",\"\",\"07:24:00\",\"07:25:00\",\"07:26:00\",\"07:28:00\",\"07:30:00\",\"07:33:00\",\"07:35:00\",\"07:36:00\",\"07:38:00\",\"\",\"07:40:00\",\"07:41:00\",\"07:43:00\",\"07:45:00\",\"07:48:00\",\"07:49:00\",\"07:51:00\",\"07:52:00\",\"07:53:00\",\"07:57:00\",\"08:00:00\",\"08:02:00\",\"08:06:00\",\"08:08:00\",\"08:07:00\",\"08:12:00\",\"\",\"\",\"08:11:00\",\"\",\"08:14:00\",\"08:15:00\"],[\"\",\"07:40:00\",\"\",\"07:40:00\",\"07:41:00\",\"07:42:00\",\"07:44:00\",\"07:46:00\",\"07:49:00\",\"07:51:00\",\"07:52:00\",\"07:54:00\",\"\",\"07:56:00\",\"07:57:00\",\"07:59:00\",\"08:01:00\",\"08:04:00\",\"08:05:00\",\"08:07:00\",\"08:08:00\",\"08:09:00\",\"08:13:00\",\"08:16:00\",\"08:18:00\",\"08:22:00\",\"08:24:00\",\"08:23:00\",\"08:28:00\",\"\",\"\",\"08:27:00\",\"\",\"08:30:00\",\"08:31:00\"],[\"\",\"07:58:00\",\"\",\"07:58:00\",\"07:59:00\",\"08:00:00\",\"08:02:00\",\"08:04:00\",\"08:07:00\",\"08:09:00\",\"08:10:00\",\"08:12:00\",\"\",\"08:14:00\",\"08:15:00\",\"08:17:00\",\"08:19:00\",\"08:22:00\",\"08:23:00\",\"08:25:00\",\"08:26:00\",\"08:27:00\",\"08:31:00\",\"08:34:00\",\"08:36:00\",\"08:40:00\",\"08:42:00\",\"08:41:00\",\"08:46:00\",\"\",\"\",\"08:45:00\",\"\",\"08:48:00\",\"08:49:00\"],[\"\",\"08:16:00\",\"\",\"08:16:00\",\"08:17:00\",\"08:18:00\",\"08:20:00\",\"08:22:00\",\"08:25:00\",\"08:27:00\",\"08:28:00\",\"08:30:00\",\"\",\"08:32:00\",\"08:33:00\",\"08:35:00\",\"08:37:00\",\"08:40:00\",\"08:41:00\",\"08:43:00\",\"08:44:00\",\"08:45:00\",\"08:49:00\",\"08:52:00\",\"08:54:00\",\"08:58:00\",\"09:00:00\",\"08:59:00\",\"09:04:00\",\"\",\"\",\"09:03:00\",\"\",\"09:06:00\",\"09:07:00\"],[\"\",\"08:34:00\",\"\",\"08:34:00\",\"08:35:00\",\"08:36:00\",\"08:38:00\",\"08:40:00\",\"08:43:00\",\"08:45:00\",\"08:46:00\",\"08:48:00\",\"\",\"08:50:00\",\"08:51:00\",\"08:53:00\",\"08:55:00\",\"08:58:00\",\"08:59:00\",\"09:01:00\",\"09:02:00\",\"09:03:00\",\"09:07:00\",\"09:10:00\",\"09:12:00\",\"09:16:00\",\"09:18:00\",\"09:17:00\",\"09:22:00\",\"\",\"\",\"09:21:00\",\"\",\"09:24:00\",\"09:25:00\"],[\"\",\"08:52:00\",\"\",\"08:52:00\",\"08:53:00\",\"08:54:00\",\"08:56:00\",\"08:58:00\",\"09:01:00\",\"09:03:00\",\"09:04:00\",\"09:06:00\",\"\",\"09:08:00\",\"09:09:00\",\"09:11:00\",\"09:13:00\",\"09:16:00\",\"09:17:00\",\"09:19:00\",\"09:20:00\",\"09:21:00\",\"09:25:00\",\"09:28:00\",\"09:30:00\",\"09:34:00\",\"09:36:00\",\"09:35:00\",\"09:40:00\",\"\",\"\",\"09:39:00\",\"\",\"09:42:00\",\"09:43:00\"],[\"\",\"09:10:00\",\"\",\"09:10:00\",\"09:11:00\",\"09:12:00\",\"09:14:00\",\"09:16:00\",\"09:19:00\",\"09:21:00\",\"09:22:00\",\"09:24:00\",\"\",\"09:26:00\",\"09:27:00\",\"09:29:00\",\"09:31:00\",\"09:34:00\",\"09:35:00\",\"09:37:00\",\"09:38:00\",\"09:39:00\",\"09:43:00\",\"09:46:00\",\"09:48:00\",\"09:52:00\",\"09:54:00\",\"09:53:00\",\"09:58:00\",\"\",\"\",\"09:57:00\",\"\",\"10:00:00\",\"10:01:00\"],[\"\",\"09:28:00\",\"\",\"09:28:00\",\"09:29:00\",\"09:30:00\",\"09:32:00\",\"09:34:00\",\"09:37:00\",\"09:39:00\",\"09:40:00\",\"09:42:00\",\"\",\"09:44:00\",\"09:45:00\",\"09:47:00\",\"09:49:00\",\"09:52:00\",\"09:53:00\",\"09:55:00\",\"09:56:00\",\"09:57:00\",\"10:01:00\",\"10:04:00\",\"10:06:00\",\"10:10:00\",\"10:12:00\",\"10:11:00\",\"10:16:00\",\"\",\"\",\"10:15:00\",\"\",\"10:18:00\",\"10:19:00\"],[\"\",\"09:46:00\",\"\",\"09:46:00\",\"09:47:00\",\"09:48:00\",\"09:50:00\",\"09:52:00\",\"09:55:00\",\"09:57:00\",\"09:58:00\",\"10:00:00\",\"\",\"10:02:00\",\"10:03:00\",\"10:05:00\",\"10:07:00\",\"10:10:00\",\"10:11:00\",\"10:13:00\",\"10:14:00\",\"10:15:00\",\"10:19:00\",\"10:22:00\",\"10:24:00\",\"10:28:00\",\"10:30:00\",\"10:29:00\",\"10:34:00\",\"\",\"\",\"10:33:00\",\"\",\"10:36:00\",\"10:37:00\"],[\"\",\"10:04:00\",\"\",\"10:04:00\",\"10:05:00\",\"10:06:00\",\"10:08:00\",\"10:10:00\",\"10:13:00\",\"10:15:00\",\"10:16:00\",\"10:18:00\",\"\",\"10:20:00\",\"10:21:00\",\"10:23:00\",\"10:25:00\",\"10:28:00\",\"10:29:00\",\"10:31:00\",\"10:32:00\",\"10:33:00\",\"10:37:00\",\"10:40:00\",\"10:42:00\",\"10:46:00\",\"10:48:00\",\"10:47:00\",\"10:52:00\",\"\",\"\",\"10:51:00\",\"\",\"10:54:00\",\"10:55:00\"],[\"\",\"10:22:00\",\"\",\"10:22:00\",\"10:23:00\",\"10:24:00\",\"10:26:00\",\"10:28:00\",\"10:31:00\",\"10:33:00\",\"10:34:00\",\"10:36:00\",\"\",\"10:38:00\",\"10:39:00\",\"10:41:00\",\"10:43:00\",\"10:46:00\",\"10:47:00\",\"10:49:00\",\"10:50:00\",\"10:51:00\",\"10:55:00\",\"10:58:00\",\"11:00:00\",\"11:04:00\",\"11:06:00\",\"11:05:00\",\"11:10:00\",\"\",\"\",\"11:09:00\",\"\",\"11:12:00\",\"11:13:00\"],[\"\",\"10:40:00\",\"\",\"10:40:00\",\"10:41:00\",\"10:42:00\",\"10:44:00\",\"10:46:00\",\"10:49:00\",\"10:51:00\",\"10:52:00\",\"10:54:00\",\"\",\"10:56:00\",\"10:57:00\",\"10:59:00\",\"11:01:00\",\"11:04:00\",\"11:05:00\",\"11:07:00\",\"11:08:00\",\"11:09:00\",\"11:13:00\",\"11:16:00\",\"11:18:00\",\"11:22:00\",\"11:24:00\",\"11:23:00\",\"11:28:00\",\"\",\"\",\"11:27:00\",\"\",\"11:30:00\",\"11:31:00\"],[\"\",\"10:58:00\",\"\",\"10:58:00\",\"10:59:00\",\"11:00:00\",\"11:02:00\",\"11:04:00\",\"11:07:00\",\"11:09:00\",\"11:10:00\",\"11:12:00\",\"\",\"11:14:00\",\"11:15:00\",\"11:17:00\",\"11:19:00\",\"11:22:00\",\"11:23:00\",\"11:25:00\",\"11:26:00\",\"11:27:00\",\"11:31:00\",\"11:34:00\",\"11:36:00\",\"11:40:00\",\"11:42:00\",\"11:41:00\",\"11:46:00\",\"\",\"\",\"11:45:00\",\"\",\"11:48:00\",\"11:49:00\"],[\"\",\"11:16:00\",\"\",\"11:16:00\",\"11:17:00\",\"11:18:00\",\"11:20:00\",\"11:22:00\",\"11:25:00\",\"11:27:00\",\"11:28:00\",\"11:30:00\",\"\",\"11:32:00\",\"11:33:00\",\"11:35:00\",\"11:37:00\",\"11:40:00\",\"11:41:00\",\"11:43:00\",\"11:44:00\",\"11:45:00\",\"11:49:00\",\"11:52:00\",\"11:54:00\",\"11:58:00\",\"12:00:00\",\"11:59:00\",\"12:04:00\",\"\",\"\",\"12:03:00\",\"\",\"12:06:00\",\"12:07:00\"],[\"\",\"11:34:00\",\"\",\"11:34:00\",\"11:35:00\",\"11:36:00\",\"11:38:00\",\"11:40:00\",\"11:43:00\",\"11:45:00\",\"11:46:00\",\"11:48:00\",\"\",\"11:50:00\",\"11:51:00\",\"11:53:00\",\"11:55:00\",\"11:58:00\",\"11:59:00\",\"12:01:00\",\"12:02:00\",\"12:03:00\",\"12:07:00\",\"12:10:00\",\"12:12:00\",\"12:16:00\",\"12:18:00\",\"12:17:00\",\"12:22:00\",\"\",\"\",\"12:21:00\",\"\",\"12:24:00\",\"12:25:00\"],[\"\",\"11:52:00\",\"\",\"11:52:00\",\"11:53:00\",\"11:54:00\",\"11:56:00\",\"11:58:00\",\"12:01:00\",\"12:03:00\",\"12:04:00\",\"12:06:00\",\"\",\"12:08:00\",\"12:09:00\",\"12:11:00\",\"12:13:00\",\"12:16:00\",\"12:17:00\",\"12:19:00\",\"12:20:00\",\"12:21:00\",\"12:25:00\",\"12:28:00\",\"12:30:00\",\"12:34:00\",\"12:36:00\",\"12:35:00\",\"12:40:00\",\"\",\"\",\"12:39:00\",\"\",\"12:42:00\",\"12:43:00\"],[\"\",\"12:10:00\",\"\",\"12:10:00\",\"12:11:00\",\"12:12:00\",\"12:14:00\",\"12:16:00\",\"12:19:00\",\"12:21:00\",\"12:22:00\",\"12:24:00\",\"\",\"12:26:00\",\"12:27:00\",\"12:29:00\",\"12:31:00\",\"12:34:00\",\"12:35:00\",\"12:37:00\",\"12:38:00\",\"12:39:00\",\"12:43:00\",\"12:46:00\",\"12:48:00\",\"12:52:00\",\"12:54:00\",\"12:53:00\",\"12:58:00\",\"\",\"\",\"12:57:00\",\"\",\"13:00:00\",\"13:01:00\"],[\"\",\"12:28:00\",\"\",\"12:28:00\",\"12:29:00\",\"12:30:00\",\"12:32:00\",\"12:34:00\",\"12:37:00\",\"12:39:00\",\"12:40:00\",\"12:42:00\",\"\",\"12:44:00\",\"12:45:00\",\"12:47:00\",\"12:49:00\",\"12:52:00\",\"12:53:00\",\"12:55:00\",\"12:56:00\",\"12:57:00\",\"13:01:00\",\"13:04:00\",\"13:06:00\",\"13:10:00\",\"13:12:00\",\"13:11:00\",\"13:16:00\",\"\",\"\",\"13:15:00\",\"\",\"13:18:00\",\"13:19:00\"],[\"\",\"12:46:00\",\"\",\"12:46:00\",\"12:47:00\",\"12:48:00\",\"12:50:00\",\"12:52:00\",\"12:55:00\",\"12:57:00\",\"12:58:00\",\"13:00:00\",\"\",\"13:02:00\",\"13:03:00\",\"13:05:00\",\"13:07:00\",\"13:10:00\",\"13:11:00\",\"13:13:00\",\"13:14:00\",\"13:15:00\",\"13:19:00\",\"13:22:00\",\"13:24:00\",\"13:28:00\",\"13:30:00\",\"13:29:00\",\"13:34:00\",\"\",\"\",\"13:33:00\",\"\",\"13:36:00\",\"13:37:00\"],[\"\",\"13:04:00\",\"\",\"13:04:00\",\"13:05:00\",\"13:06:00\",\"13:08:00\",\"13:10:00\",\"13:13:00\",\"13:15:00\",\"13:16:00\",\"13:18:00\",\"\",\"13:20:00\",\"13:21:00\",\"13:23:00\",\"13:25:00\",\"13:28:00\",\"13:29:00\",\"13:31:00\",\"13:32:00\",\"13:33:00\",\"13:37:00\",\"13:40:00\",\"13:42:00\",\"13:46:00\",\"13:48:00\",\"13:47:00\",\"13:52:00\",\"\",\"\",\"13:51:00\",\"\",\"13:54:00\",\"13:55:00\"],[\"\",\"13:22:00\",\"\",\"13:22:00\",\"13:23:00\",\"13:24:00\",\"13:26:00\",\"13:28:00\",\"13:31:00\",\"13:33:00\",\"13:34:00\",\"13:36:00\",\"\",\"13:38:00\",\"13:39:00\",\"13:41:00\",\"13:43:00\",\"13:46:00\",\"13:47:00\",\"13:49:00\",\"13:50:00\",\"13:51:00\",\"13:55:00\",\"13:58:00\",\"14:00:00\",\"14:04:00\",\"14:06:00\",\"14:05:00\",\"14:10:00\",\"\",\"\",\"14:09:00\",\"\",\"14:12:00\",\"14:13:00\"],[\"\",\"13:40:00\",\"\",\"13:40:00\",\"13:41:00\",\"13:42:00\",\"13:44:00\",\"13:46:00\",\"13:49:00\",\"13:51:00\",\"13:52:00\",\"13:54:00\",\"\",\"13:56:00\",\"13:57:00\",\"13:59:00\",\"14:01:00\",\"14:04:00\",\"14:05:00\",\"14:07:00\",\"14:08:00\",\"14:09:00\",\"14:13:00\",\"14:16:00\",\"14:18:00\",\"14:22:00\",\"14:24:00\",\"14:23:00\",\"14:28:00\",\"\",\"\",\"14:27:00\",\"\",\"14:30:00\",\"14:31:00\"],[\"\",\"13:58:00\",\"\",\"13:58:00\",\"13:59:00\",\"14:00:00\",\"14:02:00\",\"14:04:00\",\"14:07:00\",\"14:09:00\",\"14:10:00\",\"14:12:00\",\"\",\"14:14:00\",\"14:15:00\",\"14:17:00\",\"14:19:00\",\"14:22:00\",\"14:23:00\",\"14:25:00\",\"14:26:00\",\"14:27:00\",\"14:31:00\",\"14:34:00\",\"14:36:00\",\"14:40:00\",\"14:42:00\",\"14:41:00\",\"14:46:00\",\"\",\"\",\"14:45:00\",\"\",\"14:48:00\",\"14:49:00\"],[\"\",\"14:16:00\",\"\",\"14:16:00\",\"14:17:00\",\"14:18:00\",\"14:20:00\",\"14:22:00\",\"14:25:00\",\"14:27:00\",\"14:28:00\",\"14:30:00\",\"\",\"14:32:00\",\"14:33:00\",\"14:35:00\",\"14:37:00\",\"14:40:00\",\"14:41:00\",\"14:43:00\",\"14:44:00\",\"14:45:00\",\"14:49:00\",\"14:52:00\",\"14:54:00\",\"14:58:00\",\"15:00:00\",\"14:59:00\",\"15:04:00\",\"\",\"\",\"15:03:00\",\"\",\"15:06:00\",\"15:07:00\"],[\"\",\"14:34:00\",\"\",\"14:34:00\",\"14:35:00\",\"14:36:00\",\"14:38:00\",\"14:40:00\",\"14:43:00\",\"14:45:00\",\"14:46:00\",\"14:48:00\",\"\",\"14:50:00\",\"14:51:00\",\"14:53:00\",\"14:55:00\",\"14:58:00\",\"14:59:00\",\"15:01:00\",\"15:02:00\",\"15:03:00\",\"15:07:00\",\"15:10:00\",\"15:12:00\",\"15:16:00\",\"15:18:00\",\"15:17:00\",\"15:22:00\",\"\",\"\",\"15:21:00\",\"\",\"15:24:00\",\"15:25:00\"],[\"\",\"14:52:00\",\"\",\"14:52:00\",\"14:53:00\",\"14:54:00\",\"14:56:00\",\"14:58:00\",\"15:01:00\",\"15:03:00\",\"15:04:00\",\"15:06:00\",\"\",\"15:08:00\",\"15:09:00\",\"15:11:00\",\"15:13:00\",\"15:16:00\",\"15:17:00\",\"15:19:00\",\"15:20:00\",\"15:21:00\",\"15:25:00\",\"15:28:00\",\"15:30:00\",\"15:34:00\",\"15:36:00\",\"15:35:00\",\"15:40:00\",\"\",\"\",\"15:39:00\",\"\",\"15:42:00\",\"15:43:00\"],[\"\",\"15:10:00\",\"\",\"15:10:00\",\"15:11:00\",\"15:12:00\",\"15:14:00\",\"15:16:00\",\"15:19:00\",\"15:21:00\",\"15:22:00\",\"15:24:00\",\"\",\"15:26:00\",\"15:27:00\",\"15:29:00\",\"15:31:00\",\"15:34:00\",\"15:35:00\",\"15:37:00\",\"15:38:00\",\"15:39:00\",\"15:43:00\",\"15:46:00\",\"15:48:00\",\"15:52:00\",\"15:54:00\",\"15:53:00\",\"15:58:00\",\"\",\"\",\"15:57:00\",\"\",\"16:00:00\",\"16:01:00\"],[\"\",\"15:28:00\",\"\",\"15:28:00\",\"15:29:00\",\"15:30:00\",\"15:32:00\",\"15:34:00\",\"15:37:00\",\"15:39:00\",\"15:40:00\",\"15:42:00\",\"\",\"15:44:00\",\"15:45:00\",\"15:47:00\",\"15:49:00\",\"15:52:00\",\"15:53:00\",\"15:55:00\",\"15:56:00\",\"15:57:00\",\"16:01:00\",\"16:04:00\",\"16:06:00\",\"16:10:00\",\"16:12:00\",\"16:11:00\",\"16:16:00\",\"\",\"\",\"16:15:00\",\"\",\"16:18:00\",\"16:19:00\"],[\"\",\"15:46:00\",\"\",\"15:46:00\",\"15:47:00\",\"15:48:00\",\"15:50:00\",\"15:52:00\",\"15:55:00\",\"15:57:00\",\"15:58:00\",\"16:00:00\",\"\",\"16:02:00\",\"16:03:00\",\"16:05:00\",\"16:07:00\",\"16:10:00\",\"16:11:00\",\"16:13:00\",\"16:14:00\",\"16:15:00\",\"16:19:00\",\"16:22:00\",\"16:24:00\",\"16:28:00\",\"16:30:00\",\"16:29:00\",\"16:34:00\",\"\",\"\",\"16:33:00\",\"\",\"16:36:00\",\"16:37:00\"],[\"\",\"16:04:00\",\"\",\"16:04:00\",\"16:05:00\",\"16:06:00\",\"16:08:00\",\"16:10:00\",\"16:13:00\",\"16:15:00\",\"16:16:00\",\"16:18:00\",\"\",\"16:20:00\",\"16:21:00\",\"16:23:00\",\"16:25:00\",\"16:28:00\",\"16:29:00\",\"16:31:00\",\"16:32:00\",\"16:33:00\",\"16:37:00\",\"16:40:00\",\"16:42:00\",\"16:46:00\",\"16:48:00\",\"16:47:00\",\"16:52:00\",\"\",\"\",\"16:51:00\",\"\",\"16:54:00\",\"16:55:00\"],[\"\",\"16:22:00\",\"\",\"16:22:00\",\"16:23:00\",\"16:24:00\",\"16:26:00\",\"16:28:00\",\"16:31:00\",\"16:33:00\",\"16:34:00\",\"16:36:00\",\"\",\"16:38:00\",\"16:39:00\",\"16:41:00\",\"16:43:00\",\"16:46:00\",\"16:47:00\",\"16:49:00\",\"16:50:00\",\"16:51:00\",\"16:55:00\",\"16:58:00\",\"17:00:00\",\"17:04:00\",\"17:06:00\",\"17:05:00\",\"17:10:00\",\"\",\"\",\"17:09:00\",\"\",\"17:12:00\",\"17:13:00\"],[\"\",\"16:40:00\",\"\",\"16:40:00\",\"16:41:00\",\"16:42:00\",\"16:44:00\",\"16:46:00\",\"16:49:00\",\"16:51:00\",\"16:52:00\",\"16:54:00\",\"\",\"16:56:00\",\"16:57:00\",\"16:59:00\",\"17:01:00\",\"17:04:00\",\"17:05:00\",\"17:07:00\",\"17:08:00\",\"17:09:00\",\"17:13:00\",\"17:16:00\",\"17:18:00\",\"17:22:00\",\"17:24:00\",\"17:23:00\",\"17:28:00\",\"\",\"\",\"17:27:00\",\"\",\"17:30:00\",\"17:31:00\"],[\"\",\"16:58:00\",\"\",\"16:58:00\",\"16:59:00\",\"17:00:00\",\"17:02:00\",\"17:04:00\",\"17:07:00\",\"17:09:00\",\"17:10:00\",\"17:12:00\",\"\",\"17:14:00\",\"17:15:00\",\"17:17:00\",\"17:19:00\",\"17:22:00\",\"17:23:00\",\"17:25:00\",\"17:26:00\",\"17:27:00\",\"17:31:00\",\"17:34:00\",\"17:36:00\",\"17:40:00\",\"17:42:00\",\"17:41:00\",\"17:46:00\",\"\",\"\",\"17:45:00\",\"\",\"17:48:00\",\"17:49:00\"],[\"\",\"17:16:00\",\"\",\"17:16:00\",\"17:17:00\",\"17:18:00\",\"17:20:00\",\"17:22:00\",\"17:25:00\",\"17:27:00\",\"17:28:00\",\"17:30:00\",\"\",\"17:32:00\",\"17:33:00\",\"17:35:00\",\"17:37:00\",\"17:40:00\",\"17:41:00\",\"17:43:00\",\"17:44:00\",\"17:45:00\",\"17:49:00\",\"17:52:00\",\"17:54:00\",\"17:58:00\",\"18:00:00\",\"17:59:00\",\"18:04:00\",\"\",\"\",\"18:03:00\",\"\",\"18:06:00\",\"18:07:00\"],[\"\",\"17:34:00\",\"\",\"17:34:00\",\"17:35:00\",\"17:36:00\",\"17:38:00\",\"17:40:00\",\"17:43:00\",\"17:45:00\",\"17:46:00\",\"17:48:00\",\"\",\"17:50:00\",\"17:51:00\",\"17:53:00\",\"17:55:00\",\"17:58:00\",\"17:59:00\",\"18:01:00\",\"18:02:00\",\"18:03:00\",\"18:07:00\",\"18:10:00\",\"18:12:00\",\"18:16:00\",\"18:18:00\",\"18:17:00\",\"18:22:00\",\"\",\"\",\"18:21:00\",\"\",\"18:24:00\",\"18:25:00\"],[\"\",\"17:52:00\",\"\",\"17:52:00\",\"17:53:00\",\"17:54:00\",\"17:56:00\",\"17:58:00\",\"18:01:00\",\"18:03:00\",\"18:04:00\",\"18:06:00\",\"\",\"18:08:00\",\"18:09:00\",\"18:11:00\",\"18:13:00\",\"18:16:00\",\"18:17:00\",\"18:19:00\",\"18:20:00\",\"18:21:00\",\"18:25:00\",\"18:28:00\",\"18:30:00\",\"18:34:00\",\"18:36:00\",\"18:35:00\",\"18:40:00\",\"\",\"\",\"18:39:00\",\"\",\"18:42:00\",\"18:43:00\"],[\"\",\"18:10:00\",\"\",\"18:10:00\",\"18:11:00\",\"18:12:00\",\"18:14:00\",\"18:16:00\",\"18:19:00\",\"18:21:00\",\"18:22:00\",\"18:24:00\",\"\",\"18:26:00\",\"18:27:00\",\"18:29:00\",\"18:31:00\",\"18:34:00\",\"18:35:00\",\"18:37:00\",\"18:38:00\",\"18:39:00\",\"18:43:00\",\"18:46:00\",\"18:48:00\",\"18:52:00\",\"18:54:00\",\"18:53:00\",\"18:58:00\",\"\",\"\",\"18:57:00\",\"\",\"19:00:00\",\"19:01:00\"],[\"\",\"18:28:00\",\"\",\"18:28:00\",\"18:29:00\",\"18:30:00\",\"18:32:00\",\"18:34:00\",\"18:37:00\",\"18:39:00\",\"18:40:00\",\"18:42:00\",\"\",\"18:44:00\",\"18:45:00\",\"18:47:00\",\"18:49:00\",\"18:52:00\",\"18:53:00\",\"18:55:00\",\"18:56:00\",\"18:57:00\",\"19:01:00\",\"19:04:00\",\"19:06:00\",\"19:10:00\",\"19:12:00\",\"19:11:00\",\"19:16:00\",\"\",\"\",\"19:15:00\",\"\",\"19:18:00\",\"19:19:00\"],[\"\",\"18:46:00\",\"\",\"18:46:00\",\"18:47:00\",\"18:48:00\",\"18:50:00\",\"18:52:00\",\"18:55:00\",\"18:57:00\",\"18:58:00\",\"19:00:00\",\"\",\"19:02:00\",\"19:03:00\",\"19:05:00\",\"19:07:00\",\"19:10:00\",\"19:11:00\",\"19:13:00\",\"19:14:00\",\"19:15:00\",\"19:19:00\",\"19:22:00\",\"19:24:00\",\"19:28:00\",\"19:30:00\",\"19:29:00\",\"19:34:00\",\"\",\"\",\"19:33:00\",\"\",\"19:36:00\",\"19:37:00\"],[\"\",\"19:04:00\",\"\",\"19:04:00\",\"19:05:00\",\"19:06:00\",\"19:08:00\",\"19:10:00\",\"19:13:00\",\"19:15:00\",\"19:16:00\",\"19:18:00\",\"\",\"19:20:00\",\"19:21:00\",\"19:23:00\",\"19:25:00\",\"19:28:00\",\"19:29:00\",\"19:31:00\",\"19:32:00\",\"19:33:00\",\"19:37:00\",\"19:40:00\",\"19:42:00\",\"19:46:00\",\"19:48:00\",\"19:47:00\",\"19:52:00\",\"\",\"\",\"19:51:00\",\"\",\"19:54:00\",\"19:55:00\"],[\"\",\"19:22:00\",\"\",\"19:22:00\",\"19:23:00\",\"19:24:00\",\"19:26:00\",\"19:28:00\",\"19:31:00\",\"19:33:00\",\"19:34:00\",\"19:36:00\",\"\",\"19:38:00\",\"19:39:00\",\"19:41:00\",\"19:43:00\",\"19:46:00\",\"19:47:00\",\"19:49:00\",\"19:50:00\",\"19:51:00\",\"19:55:00\",\"19:58:00\",\"20:00:00\",\"20:04:00\",\"20:06:00\",\"20:05:00\",\"20:10:00\",\"\",\"\",\"20:09:00\",\"\",\"20:12:00\",\"20:13:00\"],[\"\",\"19:40:00\",\"\",\"19:40:00\",\"19:41:00\",\"19:42:00\",\"19:44:00\",\"19:46:00\",\"19:49:00\",\"19:51:00\",\"19:52:00\",\"19:54:00\",\"\",\"19:56:00\",\"19:57:00\",\"19:59:00\",\"20:01:00\",\"20:04:00\",\"20:05:00\",\"20:07:00\",\"20:08:00\",\"20:09:00\",\"20:13:00\",\"20:16:00\",\"20:18:00\",\"20:22:00\",\"20:24:00\",\"20:23:00\",\"20:28:00\",\"\",\"\",\"20:27:00\",\"\",\"20:30:00\",\"20:31:00\"],[\"\",\"20:04:00\",\"\",\"20:04:00\",\"20:05:00\",\"20:06:00\",\"20:08:00\",\"20:10:00\",\"20:13:00\",\"20:15:00\",\"20:16:00\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\"],[\"\",\"20:22:00\",\"\",\"20:22:00\",\"20:23:00\",\"20:24:00\",\"20:26:00\",\"20:28:00\",\"20:31:00\",\"20:33:00\",\"20:34:00\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\"]]],\"stopsId\":[\"22055n\",\"22500c\",\"22005z\",\"22505c\",\"22520x\",\"22510x\",\"21455x\",\"21440x\",\"21300x\",\"21650x\",\"20125p\",\"21590z\",\"21595x\",\"21585z\",\"21360z\",\"21790z\",\"21495z\",\"21130z\",\"21335x\",\"21560x\",\"21350x\",\"21390x\",\"21645x\",\"20145-\",\"21420z\",\"21300x\",\"21440z\",\"21270z\",\"21040z\",\"22305s\",\"22005x\",\"21455z\",\"22055n\",\"22505z\",\"22500c\"],\"stops\":[\"GARDOLO"+
 	// "P.le Neufahrn\",\"RONCAFORT nord\",\"GARDOLO \\\"campo"+
 	// "sportivo\\\"\",\"RONCAFORT\",\"RONCAFORT  Caproni\",\"RONCAFORT"+
 	// "Bettine\",\"Maccani  Commercio\",\"Maccani \\\"rotatoria\\\"\",\"F.lli Fontana"+
 	// "Gen. Cantore\",\"Segantini  Centa\",\"Piazza Dante \\\"Stazione"+
 	// "FS\\\"\",\"Rosmini  S.Maria Maggiore\",\"S.Francesco  Porta Nuova\",\"Rosmini"+
 	// "\\\"Cimitero\\\"\",\"Giusti  Pascoli\",\"Vittorio Veneto"+
 	// "\\\"S.Giuseppe\\\"\",\"Milano  a Prato\",\"Bolghera \\\"S.Antonio\\\"\",\"Gerola"+
 	// "\\\"Ospedale S.Chiara\\\"\",\"Piazza Vicenza\",\"Giovanelli \\\"Osp."+
 	// "S.Camillo\\\"\",\"Grazioli  Brigata Acqui\",\"Sanzio"+
 	// "\\\"Castello\\\"\",\"Gazzoletti  Piazza Dante\",\"Largo Sauro\",\"F.lli Fontana"+
 	// "Gen. Cantore\",\"Maccani \\\"rotatoria\\\"\",\"degli Alpini"+
 	// "Oberziner\",\"Caneppele Goio\",\"M. ROSSI \\\"Stella d.Mattino\\\"\",\"GARDOLO"+
 	// "\\\"campo sportivo\\\"\",\"Maccani  Commercio\",\"GARDOLO  P.le"+
 	// "Neufahrn\",\"RONCAFORT\",\"RONCAFORT nord\"],\"delays\":[[0,5,0,5,7,8,5,0,5,7,8,5,0,5,7,8,5,0,5,7,8,5,0,5,7,8,5,0,5,7,8,5,0,5,7,8,5,0,5,7,8,5,0,5,7,8,5,0,5,7],[0,5,0,5,7,8,5,0,5,7,8,5,0,5,7,8,5,0,5,7,8,5,0,5,7,8,5,0,5,7,8,5,0,5,7,8,5,0,5,7,8,5,0,5,7,8,5,0,5,7]]}";
 	public static TimeTable getTransitTimeTableById(long from_day, long to_day, String routeId)
 			throws ConnectionException, ProtocolException, SecurityException, JSONException, JsonParseException,
 			JsonMappingException, IOException {
 		String url = Config.TARGET_ADDRESS + Config.CALL_GET_TRANSIT_TIME_BY_ROUTE + "/" + routeId + "/" + from_day
 				+ "/" + to_day;
 
 		MessageRequest req = new MessageRequest(GlobalConfig.getAppUrl(JPHelper.mContext), url);
 		req.setMethod(Method.GET);
 		req.setQuery("complex=true");
 
 		MessageResponse res = JPHelper.instance.getProtocolCarrier().invokeSync(req, JPParamsHelper.getAppToken(),
 				getAuthToken());
 
 		return eu.trentorise.smartcampus.android.common.Utils.convertJSONToObject(res.getBody(), TimeTable.class);
 	}
 
 	public static TimeTable getLocalTransitTimeTableById(long from_day, long to_day, String routeId)
 			throws ConnectionException, ProtocolException, SecurityException, JSONException, JsonParseException,
 			JsonMappingException, IOException {
 		if (!TTHelper.isInitialized())
 			TTHelper.init(mContext);
 		return TTHelper.getTTwithRouteIdAndTime(routeId, from_day, to_day);
 	}
 
 	public static List<SmartCheckStop> getStops(String agencyId, double[] location, double radius) throws Exception {
 		getInstance();
 		MessageRequest request = new MessageRequest(GlobalConfig.getAppUrl(getInstance().mContext),
 				JPHelper.mContext.getString(R.string.api_dt_objects));
 		request.setMethod(Method.GET);
 		ObjectFilter filter = new ObjectFilter();
 
 		filter.setClassName("eu.trentorise.smartcampus.dt.model.POIObject");
 
 		filter.setSkip(0);
 		filter.setLimit(-1);
 		filter.setTypes(Collections.singletonList("Mobility"));
 
 		Map<String, Object> criteria = new HashMap<String, Object>();
 		criteria.put("source", "smartplanner-transitstops");
 
 		if (agencyId != null) {
 			criteria.put("customData.agencyId", agencyId);
 		}
 
 		filter.setCriteria(criteria);
 
 		// filter by near me
 		if (location != null) {
 			filter.setCenter(location);
 			filter.setRadius(radius);
 		}
 
 		String queryStrObject = eu.trentorise.smartcampus.android.common.Utils.convertToJSON(filter);
 		String queryString = "filter=" + queryStrObject;
 		request.setQuery(queryString);
 
 		MessageResponse response = getInstance().protocolCarrier.invokeSync(request, JPParamsHelper.getAppToken(),
 				getAuthToken());
 		String body = response.getBody();
 		if (body == null || body.trim().length() == 0) {
 			return Collections.emptyList();
 		}
 
 		Map<String, List<Map<String, Object>>> map = eu.trentorise.smartcampus.android.common.Utils.convertJSON(body,
 				new TypeReference<Map<String, List<Map<String, Object>>>>() {
 				});
 		ArrayList<SmartCheckStop> objects = new ArrayList<SmartCheckStop>();
 		if (map != null) {
 			for (String key : map.keySet()) {
 				// if (types != null && !types.contains(key)) continue;
 				List<Map<String, Object>> protos = map.get(key);
 				if (protos != null) {
 					for (Map<String, Object> proto : protos) {
 						objects.add(eu.trentorise.smartcampus.android.common.Utils.convertObjectToData(
 								SmartCheckStop.class, proto));
 					}
 				}
 			}
 		}
 
 		return objects;
 	}
 
 	public static List<TripData> getTrips(SmartCheckStop stop) throws Exception {
 
 		getInstance();
 		String url = Config.TARGET_ADDRESS + Config.CALL_GET_LIMITED_TIMETABLE;
 		url += "/";
 		url += stop.getCustomData().get("agencyId"); // agencyId
 		url += "/";
 		url += stop.getCustomData().get("id"); // stopId
 		url += "/";
 		url += "3"; // max results for route
 
 		MessageRequest request = new MessageRequest(GlobalConfig.getAppUrl(getInstance().mContext), url);
 		request.setMethod(Method.GET);
 		request.setQuery("complex=true");
 
 		MessageResponse response = getInstance().protocolCarrier.invokeSync(request, JPParamsHelper.getAppToken(),
 				getAuthToken());
 		String body = response.getBody();
 		if (body == null || body.trim().length() == 0) {
 			return Collections.emptyList();
 		}
 
 		Map<String, SmartCheckRoute> map = eu.trentorise.smartcampus.android.common.Utils.convertJSON(body,
 				new TypeReference<Map<String, SmartCheckRoute>>() {
 				});
 
 		ArrayList<TripData> objects = new ArrayList<TripData>();
 
 		if (map != null) {
 			for (Entry<String, SmartCheckRoute> entry : map.entrySet()) {
 				// if (types != null && !types.contains(key)) continue;
 				String routeId = entry.getKey();
 				SmartCheckRoute routeData = entry.getValue();
 
 				for (SmartCheckTime timeData : routeData.getTimes()) {
 					TripData tripData = new TripData();
 					tripData.setRouteId(routeId);
 					tripData.setRouteName(routeData.getName());
 					tripData.setRouteShortName(routeData.getRoute());
 					tripData.setTime(timeData.getTime());
 					tripData.setTripId(timeData.getTrip().getId());
 					tripData.setAgencyId(timeData.getTrip().getAgency());
 
 					// delays
 					Map<CreatorType, String> delays = null;
 					if (routeData.getDelays().containsKey(timeData.getTrip().getId())) {
 						delays = new HashMap<CreatorType, String>();
 						Map<String, String> rdDelays = routeData.getDelays().get(timeData.getTrip().getId());
 						for (Entry<String, String> delay : rdDelays.entrySet()) {
 							delays.put(CreatorType.getAlertType(delay.getKey()), delay.getValue());
 						}
 					}
 					tripData.setDelays(delays);
 
 					objects.add(tripData);
 				}
 			}
 		}
 
 		return objects;
 	}
 
 	public static List<ParkingSerial> getParkings(String parkingAgencyId) throws Exception {
 
 		getInstance();
 		String url = Config.TARGET_ADDRESS;
 
 		if (parkingAgencyId != null) {
 			url += Config.CALL_GET_PARKINGS_BY_AGENCY + "/" + parkingAgencyId;
 		} else {
 			url += Config.CALL_GET_PARKINGS;
 		}
 
 		MessageRequest request = new MessageRequest(GlobalConfig.getAppUrl(getInstance().mContext), url);
 		request.setMethod(Method.GET);
 
 		MessageResponse response = getInstance().protocolCarrier.invokeSync(request, JPParamsHelper.getAppToken(),
 				getAuthToken());
 		String body = response.getBody();
 		if (body == null || body.trim().length() == 0) {
 			return Collections.emptyList();
 		}
 
 		List<ParkingSerial> objects = eu.trentorise.smartcampus.android.common.Utils.convertJSON(body,
 				new TypeReference<List<ParkingSerial>>() {
 				});
 
 		return objects;
 	}
 
 	public static SyncStorage getSyncStorage() throws DataException {
 		return getInstance().storage;
 	}
 	
 	public static SharedPreferences getTutorialPreferences(Context ctx){
 		SharedPreferences out = ctx.getSharedPreferences(TUT_PREFS, Context.MODE_PRIVATE);
 		return out;
 	}
 	
 	public static boolean isFirstLaunch(Context ctx){
 		return getTutorialPreferences(ctx).getBoolean(FIRST_LAUNCH_PREFS, true);
 	}
 	public static void disableFirstLaunch(Context ctx){
 		Editor edit = getTutorialPreferences(ctx).edit();
 		edit.putBoolean(FIRST_LAUNCH_PREFS, false);
 		edit.commit();
 	}
 	
 	public static boolean wantTour(Context ctx){
 		return getTutorialPreferences(ctx).getBoolean(TOUR_PREFS, false);
 	}
 	
 	public static void setWantTour(Context ctx,boolean want){
 		Editor edit = getTutorialPreferences(ctx).edit();
 		edit.putBoolean(TOUR_PREFS, want);
 		edit.commit();
 	}
 	
 	public static boolean isTutorialShowed(Context ctx,Tutorial t){
 		return getTutorialPreferences(ctx).getBoolean(t.toString(), false);
 	}
 	
 	
 	public static void setTutorialVisibility(Context ctx,Tutorial t,boolean visibility){
 		Editor edit = getTutorialPreferences(ctx).edit();
 		edit.putBoolean(t.toString(), visibility);
 		edit.commit();
 	}
 	
 	public static void resetTutorialPreferences(Context ctx){
 		for(Tutorial t : Tutorial.values() )
 		{
 			setTutorialVisibility(mContext, t,false);
 		}
 	}
 	
 	/**
 	 * With this method you can get the last tutorial that was not showed
 	 * @param ctx the activity 
 	 * @return the last Tutorial not showed to the user otherwise null
 	 */
 	public static Tutorial getLastTutorialNotShowed(Context ctx){
 		for(Tutorial t : Tutorial.values() )
 		{
 			if(!isTutorialShowed(ctx, t))
 				return t;
 		}
 		return null;
 	}
 
 }
