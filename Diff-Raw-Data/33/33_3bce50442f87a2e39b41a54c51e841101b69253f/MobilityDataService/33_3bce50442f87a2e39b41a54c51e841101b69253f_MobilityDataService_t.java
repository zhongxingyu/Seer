 /*******************************************************************************
  * Copyright 2012-2013 Trento RISE
  * 
  *    Licensed under the Apache License, Version 2.0 (the "License");
  *    you may not use this file except in compliance with the License.
  *    You may obtain a copy of the License at
  * 
  *        http://www.apache.org/licenses/LICENSE-2.0
  * 
  *    Unless required by applicable law or agreed to in writing, software
  *    distributed under the License is distributed on an "AS IS" BASIS,
  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *    See the License for the specific language governing permissions and
  *    limitations under the License.
  ******************************************************************************/
 package eu.trentorise.smartcampus.mobilityservice;
 
 import it.sayservice.platform.smartplanner.data.message.alerts.AlertRoad;
 import it.sayservice.platform.smartplanner.data.message.alerts.CreatorType;
 import it.sayservice.platform.smartplanner.data.message.cache.CacheUpdateResponse;
 import it.sayservice.platform.smartplanner.data.message.otpbeans.CompressedTransitTimeTable;
 import it.sayservice.platform.smartplanner.data.message.otpbeans.Parking;
 import it.sayservice.platform.smartplanner.data.message.otpbeans.Route;
 import it.sayservice.platform.smartplanner.data.message.otpbeans.Stop;
 import it.sayservice.platform.smartplanner.data.message.otpbeans.StopTime;
 
 import java.net.URLEncoder;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import eu.trentorise.smartcampus.mobilityservice.model.Delay;
 import eu.trentorise.smartcampus.mobilityservice.model.TimeTable;
 import eu.trentorise.smartcampus.mobilityservice.model.TripData;
 import eu.trentorise.smartcampus.network.JsonUtils;
 import eu.trentorise.smartcampus.network.RemoteConnector;
 import eu.trentorise.smartcampus.network.RemoteException;
 
 
 /**
  * Mobility Service information API. Provides information of timetables, real time info, etc.
  * 
  * @author raman
  * 
  */
 public class MobilityDataService {
 
 	private static final String ROUTES = "getroutes/%s";
 	private static final String STOPS = "getstops/%s/%s";
 
 	private static final String TT = "gettimetable/%s/%s/%s";
 	private static final String LIMITED_TT = "getlimitedtimetable/%s/%s/%s";
 	private static final String TRANSIT_TIMES = "gettransittimes/%s/%s/%s";
 	private static final String TRANSIT_DELAYS = "gettransitdelays/%s/%s/%s";
 	private static final String PARKING = "getparkingsbyagency/%s";
 	private static final String ROADINFO = "getroadinfobyagency/%s/%s/%s";
 	private static final String CACHE_STATUS = "getcachestatus";
 	private static final String CACHE_UPDATE = "getcacheupdate/%s/%s";
 
 	private String serviceUrl;
 
 	/**
 	 * 
 	 * @param serviceUrl service address
 	 */
 	public MobilityDataService(String serviceUrl) {
 		this.serviceUrl = serviceUrl;
 		if (!serviceUrl.endsWith("/")) {
 			this.serviceUrl += '/';
 		}
 	}
 
 	/**
 	 * Provides (possibly real-time) info about parkings for the specified agency ID.
 	 * @param agencyId
 	 * @param token user or client access token 
 	 * @return List of {@link Parking} instances
 	 * @throws MobilityServiceException
 	 */
 	public List<Parking> getParkings(String agencyId, String token) throws MobilityServiceException {
 		if (agencyId == null)
 			throw new MobilityServiceException("Incomplete request parameters");
 		try {
 			agencyId = URLEncoder.encode(agencyId, "utf8");
 			String json = RemoteConnector.getJSON(serviceUrl, String.format(PARKING, agencyId), token);
 			return JsonUtils.toObjectList(json,Parking.class);
 		}catch (SecurityException e) {
 			throw e;
 		} catch (Exception e) {
 			throw new MobilityServiceException(e);
 		}
 
 	}
 
 	/**
 	 * Provides real time information about road works and deviations for 
 	 * the specified agency and period of time
 	 * @param agencyId
 	 * @param from start time of interval, defaults to the current time
 	 * @param to end time of the interval, defaults to the current time plus 1 day
 	 * @param token user or client access token 
 	 * @return list of {@link AlertRoad} instances
 	 * @throws MobilityServiceException
 	 */
 	public List<AlertRoad> getRoadInfo(String agencyId, Long from, Long to, String token) throws MobilityServiceException {
 		if (agencyId == null)
 			throw new MobilityServiceException("Incomplete request parameters");
 		if (from == null) from = System.currentTimeMillis();
 		if (to == null || to < from) to = from+1000*60*60*24;
 		try {
 			agencyId = URLEncoder.encode(agencyId, "utf8");
 			String json = RemoteConnector.getJSON(serviceUrl, String.format(ROADINFO, agencyId, from, to), token);
 			return JsonUtils.toObjectList(json, AlertRoad.class);
 		}catch (SecurityException e) {
 			throw e;
 		} catch (Exception e) {
 			throw new MobilityServiceException(e);
 		}
 	}
 
 	/**
 	 * Get transport agency routes
 	 * @param agencyId
 	 * @param token user or client access token 
 	 * @return List of {@link Route} instances
 	 * @throws MobilityServiceException
 	 */
 	public List<Route> getRoutes(String agencyId, String token) throws MobilityServiceException {
 		if (agencyId == null)
 			throw new MobilityServiceException("Incomplete request parameters");
 		try {
 			agencyId = URLEncoder.encode(agencyId, "utf8");
 			String json = RemoteConnector.getJSON(serviceUrl, String.format(ROUTES, agencyId), token);
 			return JsonUtils.toObjectList(json, Route.class);
 		}catch (SecurityException e) {
 			throw e;
 		} catch (Exception e) {
 			throw new MobilityServiceException(e);
 		}
 	}
 
 	/**
 	 * Provides information about route stops.
 	 * @param agencyId
 	 * @param routeId
 	 * @param token user or client access token 
 	 * @return List of {@link Stop} instances
 	 * @throws MobilityServiceException
 	 */
 	public List<Stop> getStops(String agencyId, String routeId, String token) throws MobilityServiceException {
 		if (agencyId == null || routeId == null)
 			throw new MobilityServiceException("Incomplete request parameters");
 		try {
 			agencyId = URLEncoder.encode(agencyId, "utf8");
 			routeId = URLEncoder.encode(routeId, "utf8");
 			String json = RemoteConnector.getJSON(serviceUrl, String.format(STOPS, agencyId, routeId), token);
 			return JsonUtils.toObjectList(json, Stop.class);
 		}catch (SecurityException e) {
 			throw e;
 		} catch (Exception e) {
 			throw new MobilityServiceException(e);
 		}
 	}
 
 	/**
 	 * Provides information about the public transport times at the specified
 	 * stop for the period of 1 hour before and 1 hour after.
 	 * @param agencyId
 	 * @param routeId
 	 * @param stopId
 	 * @param token user or client access token 
 	 * @return List of {@link StopTime} instances
 	 * @throws MobilityServiceException
 	 */
 	public List<StopTime> getStopTimes(String agencyId, String routeId, String stopId, String token) throws MobilityServiceException {
 		if (agencyId == null || routeId == null || stopId == null)
 			throw new MobilityServiceException("Incomplete request parameters");
 		try {
 			agencyId = URLEncoder.encode(agencyId, "utf8");
 			routeId = URLEncoder.encode(routeId, "utf8");
 			stopId = URLEncoder.encode(stopId, "utf8");
 			String json = RemoteConnector.getJSON(serviceUrl, String.format(TT, agencyId, routeId, stopId), token);
 			return JsonUtils.toObjectList(json, StopTime.class);
 		}catch (SecurityException e) {
 			throw e;
 		} catch (Exception e) {
 			throw new MobilityServiceException(e);
 		}
 	}
 
 	/**
 	 * Provides info about the next expected public transports passing the specified stop
 	 * @param agencyId
 	 * @param stopId
 	 * @param maxResults number of transports per route to return
 	 * @param token user or client access token 
 	 * @return List of {@link TripData} instances
 	 * @throws MobilityServiceException
 	 */
 	public List<TripData> getNextTrips(String agencyId, String stopId, Integer maxResults, String token) throws MobilityServiceException {
 		if (agencyId == null || stopId == null)
 			throw new MobilityServiceException("Incomplete request parameters");
 		try {
 			agencyId = URLEncoder.encode(agencyId, "utf8");
 			stopId = URLEncoder.encode(stopId, "utf8");
 			if (maxResults == null || maxResults < 0) maxResults = 3;
 			String json = RemoteConnector.getJSON(serviceUrl, String.format(LIMITED_TT, agencyId, stopId, maxResults), token);
 			return toTripDataList(json);
 		}catch (SecurityException e) {
 			throw e;
 		} catch (Exception e) {
 			throw new MobilityServiceException(e);
 		}
 	}
 
 	/**
 	 * @param json
 	 * @return
 	 */
 	@SuppressWarnings({ "rawtypes", "unchecked" })
 	private List<TripData> toTripDataList(String json) {
 		Map<String,Object> map = JsonUtils.toObject(json, Map.class);
 		List<TripData> result = new ArrayList<TripData>();
 		for(String routeId : map.keySet()) {
 			Map obj = (Map)map.get(routeId);
 			List<Map> times = (List)obj.get("times");
 			Map delays = (Map)obj.get("delays");
 			for (Map tMap : times) {
 				TripData t = new TripData();
 				t.setRouteId(routeId);
 				t.setRouteName((String)obj.get("name"));
 				t.setRouteShortName((String)obj.get("route"));
 				if (tMap.containsKey("time")) t.setTime(Long.parseLong(tMap.get("time").toString()));
 				Map tripObj = (Map)tMap.get("trip");
 				t.setTripId((String)tripObj.get("id"));
 				t.setAgencyId((String)tripObj.get("agency"));
 
 				Delay delay = null;
 				if (delays.containsKey(t.getTripId())) {
 					delay = new Delay();
 					Map delayMap = (Map)delays.get(t.getTripId());
 					for (Object type : delayMap.keySet()) {
 						delay.getValues().put(CreatorType.getAlertType(type.toString()),
 								(String)delayMap.get(type));
 					}
 				}
 				t.setDelay(delay);
 				result.add(t);
 			}
 		}
 
 		return result;
 	}
 
 	/**
 	 * Get public transport timetable (with real time delays if any) for
 	 * the specified route and date.
 	 * @param routeId
 	 * @param when
 	 * @param token user or client access token 
 	 * @return {@link TimeTable} instance
 	 * @throws MobilityServiceException
 	 */
 	public TimeTable getTimeTable(String routeId, long when, String token) throws MobilityServiceException {
 		if (routeId == null)
 			throw new MobilityServiceException("Incomplete request parameters");
 		try {
 			routeId = URLEncoder.encode(routeId, "utf8");
 			Calendar cal = Calendar.getInstance();
 			cal.setTimeInMillis(when);
 			cal.set(Calendar.HOUR_OF_DAY, 0);
 			cal.set(Calendar.MINUTE, 0);
 			cal.set(Calendar.SECOND, 0);
 			long from = cal.getTimeInMillis();
 			cal.set(Calendar.HOUR_OF_DAY, 23);
 			cal.set(Calendar.MINUTE, 59);
 			cal.set(Calendar.SECOND, 59);
 			cal.set(Calendar.MILLISECOND, 999);
 			long to = cal.getTimeInMillis();
 			String json = RemoteConnector.getJSON(serviceUrl, String.format(TRANSIT_TIMES, routeId, from, to), token);
 			return toTimetable(json);
 		}catch (SecurityException e) {
 			throw e;
 		} catch (Exception e) {
 			throw new MobilityServiceException(e);
 		}
 	}
 
 	/**
 	 * @param json
 	 * @return
 	 */
 	@SuppressWarnings({ "unchecked", "rawtypes" })
 	private TimeTable toTimetable(String json) {
 		TimeTable tt = new TimeTable();
 		Map map = JsonUtils.toObject(json, Map.class);
 		tt.setStops(JsonUtils.convert(map.get("stops"), List.class));
 		tt.setStopsId(JsonUtils.convert(map.get("stopsId"), List.class));
 		List list = (List)map.get("tripIds");
 		tt.setTripIds(JsonUtils.convert(list.get(0), List.class));
 		list = (List)map.get("times");
 		tt.setTimes(JsonUtils.convert(list.get(0), List.class));
 		list = (List)map.get("delays");
 		tt.setDelays(new ArrayList<Delay>());
 		for (Object o : (List)list.get(0)) {
			Map<String,String> om = JsonUtils.convert(o, Map.class);
			Delay d = new Delay();
			d.setValues(new HashMap<CreatorType, String>());
			if (om != null) {
				for (String key : om.keySet())
					d.getValues().put(CreatorType.getAlertType(key), om.get(key));
			}
 			tt.getDelays().add(d);
 		}
 		return tt;
 	}
 
 	/**
 	 * Retrieve the status of the cached timetables from the server 
 	 * @param versions current versions (of each agency of interest) to update
 	 * @param token user or client access token
 	 * @return map with agency and the updates of that agency.
 	 * @throws SecurityException
 	 * @throws RemoteException
 	 */
 	@SuppressWarnings("unchecked")
 	public Map<String, CacheUpdateResponse> getCacheStatus(Map<String,String> versions, String token) throws SecurityException, RemoteException {
 		Map<String, CacheUpdateResponse> map = new HashMap<String, CacheUpdateResponse>();
 		String body = versions == null || versions.isEmpty() ? "{}" : JsonUtils.toJSON(versions);
 		String json = RemoteConnector.postJSON(serviceUrl, CACHE_STATUS, body, token);
 		Map<String, Object> jsonMap = JsonUtils.toObject(json, Map.class);
 		if (jsonMap != null) {
 			for (String agency : jsonMap.keySet()) {
 				map.put(agency, JsonUtils.convert(jsonMap.get(agency), CacheUpdateResponse.class));
 			}
 		}
 		return map;
 	}
 
 	/**
 	 * Get the compressed timetable used by the timetable cache.
 	 * @param agencyId agency ID of interest
 	 * @param ttId timetable ID as returned by the status message
 	 * @param token user or client access token
 	 * @return {@link CompressedTransitTimeTable} instance
 	 * @throws SecurityException
 	 * @throws RemoteException
 	 */
 	public CompressedTransitTimeTable getCachedTimetable(String agencyId, String ttId, String token) throws SecurityException, RemoteException {
 		String json = RemoteConnector.getJSON(serviceUrl, String.format(CACHE_UPDATE, agencyId, ttId), token);
 		return JsonUtils.toObject(json, CompressedTransitTimeTable.class);
 	}
 	
 	/**
 	 * @param json
 	 * @return
 	 */
 	@SuppressWarnings({ "rawtypes" })
 	private List<Delay> toDelays(String json) {
 		Map map = JsonUtils.toObject(json, Map.class);
 		List list = (List)map.get("delays");
 		List<Delay> result = new ArrayList<Delay>();
 		for (Object o : (List)list.get(0)) {
			Map<String,String> om = JsonUtils.convert(o, Map.class);
			Delay d = new Delay();
			d.setValues(new HashMap<CreatorType, String>());
			if (om != null) {
				for (String key : om.keySet())
					d.getValues().put(CreatorType.getAlertType(key), om.get(key));
			}
 			result.add(d);
 		}
 		return result;
 	}
 
 	/**
 	 * Provides information about the current delays for the specified route
 	 * @param routeId
 	 * @param token
 	 * @return
 	 * @throws MobilityServiceException
 	 */
 	public List<Delay> getDelays(String routeId, String token) throws MobilityServiceException {
 		if (routeId == null)
 			throw new MobilityServiceException("Incomplete request parameters");
 		try {
 			routeId = URLEncoder.encode(routeId, "utf8");
 			Calendar cal = Calendar.getInstance();
 			cal.set(Calendar.HOUR_OF_DAY, 0);
 			cal.set(Calendar.MINUTE, 0);
 			cal.set(Calendar.SECOND, 0);
 			long from = cal.getTimeInMillis();
 			cal.set(Calendar.HOUR_OF_DAY, 23);
 			cal.set(Calendar.MINUTE, 59);
 			cal.set(Calendar.SECOND, 59);
 			cal.set(Calendar.MILLISECOND, 999);
 			long to = cal.getTimeInMillis();
 			String json = RemoteConnector.getJSON(serviceUrl, String.format(TRANSIT_DELAYS, routeId, from, to), token);
 			return toDelays(json);
 		}catch (SecurityException e) {
 			throw e;
 		} catch (Exception e) {
 			throw new MobilityServiceException(e);
 		}
 	}
 }
