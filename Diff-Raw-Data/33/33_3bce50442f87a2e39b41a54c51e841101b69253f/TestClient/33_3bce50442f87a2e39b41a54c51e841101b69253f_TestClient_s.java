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
 
 import it.sayservice.platform.smartplanner.data.message.Itinerary;
 import it.sayservice.platform.smartplanner.data.message.Leg;
 import it.sayservice.platform.smartplanner.data.message.Position;
 import it.sayservice.platform.smartplanner.data.message.RType;
 import it.sayservice.platform.smartplanner.data.message.RoadElement;
 import it.sayservice.platform.smartplanner.data.message.StopId;
 import it.sayservice.platform.smartplanner.data.message.TType;
 import it.sayservice.platform.smartplanner.data.message.Transport;
 import it.sayservice.platform.smartplanner.data.message.alerts.Alert;
 import it.sayservice.platform.smartplanner.data.message.alerts.AlertAccident;
 import it.sayservice.platform.smartplanner.data.message.alerts.AlertDelay;
 import it.sayservice.platform.smartplanner.data.message.alerts.AlertParking;
 import it.sayservice.platform.smartplanner.data.message.alerts.AlertRoad;
 import it.sayservice.platform.smartplanner.data.message.alerts.AlertRoadType;
 import it.sayservice.platform.smartplanner.data.message.alerts.AlertStrike;
 import it.sayservice.platform.smartplanner.data.message.alerts.CreatorType;
 import it.sayservice.platform.smartplanner.data.message.cache.CacheUpdateResponse;
 import it.sayservice.platform.smartplanner.data.message.journey.RecurrentJourney;
 import it.sayservice.platform.smartplanner.data.message.journey.RecurrentJourneyParameters;
 import it.sayservice.platform.smartplanner.data.message.journey.SingleJourney;
 import it.sayservice.platform.smartplanner.data.message.otpbeans.CompressedTransitTimeTable;
 import it.sayservice.platform.smartplanner.data.message.otpbeans.Parking;
 import it.sayservice.platform.smartplanner.data.message.otpbeans.Route;
 import it.sayservice.platform.smartplanner.data.message.otpbeans.Stop;
 import it.sayservice.platform.smartplanner.data.message.otpbeans.StopTime;
 
 import java.text.SimpleDateFormat;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.UUID;
 
 import junit.framework.Assert;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import eu.trentorise.smartcampus.communicator.CommunicatorConnector;
 import eu.trentorise.smartcampus.communicator.model.Notifications;
 import eu.trentorise.smartcampus.mobilityservice.model.BasicItinerary;
 import eu.trentorise.smartcampus.mobilityservice.model.BasicRecurrentJourney;
 import eu.trentorise.smartcampus.mobilityservice.model.Delay;
 import eu.trentorise.smartcampus.mobilityservice.model.TimeTable;
 import eu.trentorise.smartcampus.mobilityservice.model.TripData;
 import eu.trentorise.smartcampus.network.RemoteConnector;
 import eu.trentorise.smartcampus.network.RemoteConnector.CLIENT_TYPE;
 import eu.trentorise.smartcampus.network.RemoteException;
 
 public class TestClient {
 
 	private MobilityDataService dataService;
 	private MobilityPlannerService plannerService;
 	private MobilityAlertService alertService;
 	private MobilityUserService userService;
 
 	@Before
 	public void init() {
 		dataService = new MobilityDataService(Constants.SERVER_URL);
 		plannerService = new MobilityPlannerService(Constants.SERVER_URL);
 		alertService = new MobilityAlertService(Constants.SERVER_URL);
 		userService = new MobilityUserService(Constants.SERVER_URL);
 		RemoteConnector.setClientType(CLIENT_TYPE.CLIENT_ACCEPTALL);
 	}
 
 	@Test
 	public void parkings() throws SecurityException, MobilityServiceException {
 		// get parkings
 		List<Parking> parkings = dataService.getParkings("COMUNE_DI_TRENTO",Constants.USER_AUTH_TOKEN);
 		Assert.assertNotNull(parkings);
 		Assert.assertTrue(parkings.size() > 0);
 		System.err.println(parkings);
 	}
 	
 	@Test
 	public void roadData() throws SecurityException, MobilityServiceException {
 		// get road information
 		List<AlertRoad> roadInfos = dataService.getRoadInfo("COMUNE_DI_ROVERETO", System.currentTimeMillis(), System.currentTimeMillis()+100*60*60*24*3,Constants.USER_AUTH_TOKEN);
 		Assert.assertNotNull(roadInfos);
 		Assert.assertTrue(roadInfos.size() > 0);
 		System.err.println(roadInfos);
 	}
 
 	@Test
 	public void trips() throws SecurityException, MobilityServiceException {
 		// get road information
 		List<Route> routes = dataService.getRoutes("12",Constants.USER_AUTH_TOKEN);
 		Assert.assertNotNull(routes);
 		Assert.assertTrue(routes.size() > 0);
 		System.err.println(routes);
 		
 		//get stops information
 		List<Stop> stops = dataService.getStops("12",routes.get(0).getId().getId(), Constants.USER_AUTH_TOKEN);
 		Assert.assertNotNull(stops);
 		Assert.assertTrue(stops.size() > 0);
 		System.err.println(stops);
 	}
 
 	@Test
 	public void timetables() throws SecurityException, MobilityServiceException {
 		List<Route> routes = dataService.getRoutes("12",Constants.USER_AUTH_TOKEN);
 		List<Stop> stops = dataService.getStops("12",routes.get(0).getId().getId(), Constants.USER_AUTH_TOKEN);
 
 		// stop times on the stop and route
 		List<StopTime> stopTimes = dataService.getStopTimes("12",routes.get(0).getId().getId(),stops.get(0).getId(), Constants.USER_AUTH_TOKEN);
 		Assert.assertNotNull(stopTimes);
 //		Assert.assertTrue(stopTimes.size() > 0);
 		System.err.println(stopTimes);
 
 		// next trips at the stop
 		List<TripData> stopTrips = dataService.getNextTrips("12",stops.get(0).getId(), 3, Constants.USER_AUTH_TOKEN);
 		Assert.assertNotNull(stopTrips);
 		Assert.assertTrue(stopTrips.size() > 0);
 		System.err.println(stopTrips);
 
 		// timetable for the route
 		TimeTable tt = dataService.getTimeTable(routes.get(0).getId().getId(), System.currentTimeMillis(), Constants.USER_AUTH_TOKEN);
 		Assert.assertNotNull(tt);
 		System.err.println(tt);
 
 		// delays for route
 		List<Delay> delays = dataService.getDelays(routes.get(0).getId().getId(), Constants.USER_AUTH_TOKEN);
 		Assert.assertNotNull(delays);
 		System.err.println(delays);
 	}
 	
 	@Test
 	public void cache() throws SecurityException, RemoteException {
 		Map<String,String> in = new HashMap<String, String>();
 		in.put("10", "0");
 		in.put("5", "0");
 		
 		Map<String, CacheUpdateResponse> res = dataService.getCacheStatus(in, Constants.USER_AUTH_TOKEN);
 		Assert.assertNotNull(res);
 		Assert.assertTrue(res.size() > 0);
 		for (String agency : res.keySet()) {
 			System.err.println(res.get(agency));
 			for (String s : res.get(agency).getAdded()) {
 				CompressedTransitTimeTable ctt = dataService.getCachedTimetable(agency, s, Constants.USER_AUTH_TOKEN);
 				Assert.assertNotNull(ctt);
 				System.err.println(ctt);
 			}
 		}
 		
 		
 	}
 
 	@Test
 	public void planning() throws SecurityException, MobilityServiceException {
 		// single
 		SingleJourney request = new SingleJourney();
 //		request.setDate(new SimpleDateFormat("MM/dd/yyyy").format(new Date()));
 		request.setDate("08/30/2013");
 		request.setDepartureTime("04:26PM");//new SimpleDateFormat("hh:mmaa").format(new Date()));
 		Position from = new Position();
 		from.setLat("46.0699898");
 		from.setLon("11.150353");
 		request.setFrom(from);
 		Position to = new Position("46.0746659,11.1216972");
 		request.setTo(to);
 		request.setResultsNumber(1);
 		request.setRouteType(RType.fastest);
 		request.setTransportTypes(new TType[]{TType.TRANSIT, TType.CAR, TType.WALK});
 		List<Itinerary> list = plannerService.planSingleJourney(request, Constants.USER_AUTH_TOKEN);
 		Assert.assertNotNull(list);
 		Assert.assertTrue(list.size() > 0);
 		System.err.println(list);
 		
 		// recurrent
 		RecurrentJourneyParameters recRequest = new RecurrentJourneyParameters();
 		recRequest.setFrom(from);
 		recRequest.setTo(to);
 		recRequest.setFromDate(System.currentTimeMillis());
 		recRequest.setInterval(1000*60*60);
 		recRequest.setRecurrence(Arrays.asList(new Integer[]{1,2,3,4,5,6,7}));
 		recRequest.setResultsNumber(3);
 		recRequest.setRouteType(RType.fastest);
 		recRequest.setTime(new SimpleDateFormat("hh:mmaa").format(new Date()));
 		recRequest.setToDate(System.currentTimeMillis()+1000*60*60*24*7);
 		recRequest.setTransportTypes(new TType[]{TType.TRANSIT, TType.BICYCLE});
 		RecurrentJourney res = plannerService.planRecurrentJourney(recRequest, Constants.USER_AUTH_TOKEN);
 		Assert.assertNotNull(res);
 		Assert.assertTrue(res.getLegs().size() > 0);
 		System.err.println(res);
 	}
 
 	@Test
 	public void alerts() throws MobilityServiceException {
 		// delay
 		AlertDelay ac = new AlertDelay();
 		commonAttrs(ac);
 		List<Route> routes = dataService.getRoutes("12",Constants.USER_AUTH_TOKEN);
 		List<Stop> stops = dataService.getStops("12",routes.get(0).getId().getId(), Constants.USER_AUTH_TOKEN);
 		List<TripData> stopTrips = dataService.getNextTrips("12",stops.get(0).getId(), 3, Constants.USER_AUTH_TOKEN);
 		Stop s = stops.get(0);
 		ac.setPosition(new Position(s.getName(), new StopId("12", s.getId()), s.getId(), ""+s.getLongitude(), ""+s.getLatitude()));
 		ac.setDelay(60*1000*5);
 		ac.setTransport(new Transport(TType.BUS, "12", stopTrips.get(0).getRouteId(), stopTrips.get(0).getTripId()));
 		alertService.sendUserAlert(ac, Constants.USER_AUTH_TOKEN);
 		// check delay is reflected
 		stopTrips = dataService.getNextTrips("12",stops.get(0).getId(), 3, Constants.USER_AUTH_TOKEN);
 		Assert.assertNotNull(stopTrips.get(0).getDelay().getValues().get(CreatorType.USER));
 		
 		// strike
 		AlertStrike as = new AlertStrike();
 		commonAttrs(as);
 		as.setStop(new StopId("12", s.getId()));
 		as.setTransport(new Transport(TType.BUS, "12", stopTrips.get(0).getRouteId(), stopTrips.get(0).getTripId()));
 		alertService.sendUserAlert(as, Constants.USER_AUTH_TOKEN);
 		
 		// road
 		AlertRoad ar = new AlertRoad();
 		commonAttrs(ar);
 		ar.setAgencyId("COMUNE_DI_TRENTO");
 		RoadElement re = new RoadElement();
 		re.setLat("46.066695");
 		re.setLon("11.11889");
 		re.setStreet("street");
 		ar.setRoad(re);
 		ar.setChangeTypes(new AlertRoadType[]{AlertRoadType.PARKING_BLOCK});
 		alertService.sendUserAlert(ar, Constants.USER_AUTH_TOKEN);
 		List<AlertRoad> roadInfos = dataService.getRoadInfo("COMUNE_DI_TRENTO", System.currentTimeMillis(), System.currentTimeMillis()+100*60*60*24*3,Constants.USER_AUTH_TOKEN);
 		Assert.assertNotNull(roadInfos);
 		Assert.assertTrue(roadInfos.size() > 0);
 
 		AlertParking ap = new AlertParking();
 		commonAttrs(ap);
 		List<Parking> parkings = dataService.getParkings("COMUNE_DI_TRENTO",Constants.USER_AUTH_TOKEN);
 		ap.setPlace(new StopId("COMUNE_DI_TRENTO", "P1"));
 		ap.setPlacesAvailable(10);
 		alertService.sendUserAlert(ap, Constants.USER_AUTH_TOKEN);
 		parkings = dataService.getParkings("COMUNE_DI_TRENTO",Constants.USER_AUTH_TOKEN);
 		boolean found = false;
 		for (Parking aap : parkings) {
 			if (aap.getName().equals("P1")) {
 				Assert.assertEquals(10,aap.getSlotsAvailable());
 				found = true;
 			}
 		}
 		Assert.assertTrue(found);
 		
 		AlertAccident aa = new AlertAccident();
 		commonAttrs(aa);
 		aa.setSeverity("severe");
 		aa.setPosition(new Position("46.066695,11.11889"));
 		alertService.sendUserAlert(aa, Constants.USER_AUTH_TOKEN);
 
 	}
 
 	/**
 	 * @param a
 	 */
 	private void commonAttrs(Alert a) {
 		a.setCreatorId("1");
 		a.setCreatorType(CreatorType.USER);
 		a.setDescription("description");
 		a.setEffect("effect");
 		a.setEntity(null);
 		a.setFrom(System.currentTimeMillis());
 		a.setTo(System.currentTimeMillis()+1000*60*5);
 		a.setId(UUID.randomUUID().toString());
 		a.setNote("note");
 	}
 	
 	@Test
 	public void userSingleJourneys() throws MobilityServiceException {
 		SingleJourney request = new SingleJourney();
 		request.setDate(new SimpleDateFormat("MM/dd/yyyy").format(new Date()));
 		request.setDepartureTime(new SimpleDateFormat("hh:mmaa").format(new Date()));
 		Position from = new Position();
 		from.setLat("46.066799");
 		from.setLon("11.151796");
 		request.setFrom(from);
 		Position to = new Position("46.066695,11.11889");
 		request.setTo(to);
 		request.setResultsNumber(1);
 		request.setRouteType(RType.fastest);
 		request.setTransportTypes(new TType[]{TType.TRANSIT, TType.BICYCLE});
 		List<Itinerary> list = plannerService.planSingleJourney(request, Constants.USER_AUTH_TOKEN);
 		Assert.assertNotNull(list);
 		Assert.assertTrue(list.size() > 0);
 		
 		// save
 		BasicItinerary basic = new BasicItinerary();
 		basic.setData(list.get(0));
 		basic.setMonitor(true);
 		basic.setName("test");
 		basic.setOriginalFrom(from);
 		basic.setOriginalTo(to);
 		basic = userService.saveSingleJourney(basic, Constants.USER_AUTH_TOKEN);
 		Assert.assertNotNull(basic);
 		// get single
 		basic = userService.getSingleJourney(basic.getClientId(),Constants.USER_AUTH_TOKEN);
 		Assert.assertNotNull(basic);
 		Assert.assertTrue(basic.isMonitor());
 		// monitor
 		boolean res = userService.monitorSingleJourney(basic.getClientId(), false, Constants.USER_AUTH_TOKEN);
 		Assert.assertFalse(res);
 		basic = userService.getSingleJourney(basic.getClientId(),Constants.USER_AUTH_TOKEN);
 		Assert.assertFalse(basic.isMonitor());
 		// all user trips
 		List<BasicItinerary> all = userService.getSingleJourneys(Constants.USER_AUTH_TOKEN);
 		Assert.assertNotNull(all);
 		Assert.assertTrue(all.size() > 0);
 		// delete
 		for (BasicItinerary bi : all) {
 			Assert.assertTrue(userService.deleteSingleJourney(bi.getClientId(), Constants.USER_AUTH_TOKEN));
 		}
 	}
 	@Test
 	public void userRecurrJourneys() throws MobilityServiceException {
 		// recurrent
 		RecurrentJourneyParameters recRequest = new RecurrentJourneyParameters();
 		Position from = new Position();
 		from.setLat("46.0699898");
 		from.setLon("11.150353");
 		Position to = new Position("46.0746659,11.1216972");
 		recRequest.setFrom(from);
 		recRequest.setTo(to);
 		recRequest.setFromDate(System.currentTimeMillis());
 		recRequest.setInterval(1000*60*60);
 		recRequest.setRecurrence(Arrays.asList(new Integer[]{1,2,3,4,5,6,7}));
 		recRequest.setResultsNumber(3);
 		recRequest.setRouteType(RType.fastest);
 		recRequest.setTime(new SimpleDateFormat("hh:mmaa").format(new Date()));
 		recRequest.setToDate(System.currentTimeMillis()+1000*60*60*24*7);
 		recRequest.setTransportTypes(new TType[]{TType.TRANSIT, TType.BICYCLE});
 		RecurrentJourney res = plannerService.planRecurrentJourney(recRequest, Constants.USER_AUTH_TOKEN);
 		Assert.assertNotNull(res);
 		Assert.assertTrue(res.getLegs().size() > 0);
 		
 		// save
 		BasicRecurrentJourney basic = new BasicRecurrentJourney();
 		basic.setData(res);
 		basic.setMonitor(true);
 		basic.setName("test");
 		basic = userService.saveRecurrentJourney(basic, Constants.USER_AUTH_TOKEN);
 		Assert.assertNotNull(basic);
 		// get single
 		basic = userService.getRecurrentJourney(basic.getClientId(),Constants.USER_AUTH_TOKEN);
 		Assert.assertNotNull(basic);
 		Assert.assertTrue(basic.isMonitor());
 		// monitor
 		boolean b = userService.monitorRecurrentJourney(basic.getClientId(), false, Constants.USER_AUTH_TOKEN);
 		Assert.assertFalse(b);
 		basic = userService.getRecurrentJourney(basic.getClientId(),Constants.USER_AUTH_TOKEN);
 		Assert.assertFalse(basic.isMonitor());
 		// all user trips
 		List<BasicRecurrentJourney> all = userService.getRecurrentJourneys(Constants.USER_AUTH_TOKEN);
 		Assert.assertNotNull(all);
 		Assert.assertTrue(all.size() > 0);
 		// delete
 		for (BasicRecurrentJourney br : all) {
 			Assert.assertTrue(userService.deleteRecurrentJourney(br.getClientId(), Constants.USER_AUTH_TOKEN));
 		}
 	}
 
 	@Test
 	public void testAlerts() throws Exception {
 		CommunicatorConnector communicatorConnector = new CommunicatorConnector(Constants.COMMUNICATOR_SRV_URL, Constants.APPID);
 		long since = System.currentTimeMillis();
 		Notifications nots = communicatorConnector.getNotificationsByApp(since, 0, -1, Constants.USER_AUTH_TOKEN);
 		int size = nots.getNotifications().size();
 		Assert.assertNotNull(nots);
 
 		// all user trips
 		List<BasicItinerary> all = userService.getSingleJourneys(Constants.USER_AUTH_TOKEN);
 		if (all != null) {
 			for (BasicItinerary it : all) {
 				userService.deleteSingleJourney(it.getClientId(), Constants.USER_AUTH_TOKEN);
 			}
 		}
 		// all recur trips
 		List<BasicRecurrentJourney> recur = userService.getRecurrentJourneys(Constants.USER_AUTH_TOKEN);
 		if (recur != null) {
 			for (BasicRecurrentJourney it : recur) {
 				userService.deleteRecurrentJourney(it.getClientId(), Constants.USER_AUTH_TOKEN);
 			}
 		}
 
 		
 		SingleJourney request = new SingleJourney();
 		request.setDate(new SimpleDateFormat("MM/dd/yyyy").format(new Date()));
 		request.setDepartureTime(new SimpleDateFormat("hh:mmaa").format(new Date()));
 		Position from = new Position();
 		from.setLat("46.066799");
 		from.setLon("11.151796");
 		request.setFrom(from);
 		Position to = new Position("46.066695,11.11889");
 		request.setTo(to);
 		request.setResultsNumber(1);
 		request.setRouteType(RType.fastest);
 		request.setTransportTypes(new TType[]{TType.TRANSIT});
 		List<Itinerary> list = plannerService.planSingleJourney(request, Constants.USER_AUTH_TOKEN);
 		Assert.assertNotNull(list);
 		Assert.assertTrue(list.size() > 0);
 		
 		Itinerary itinerary = list.get(0);
 		// save
 		BasicItinerary basic = new BasicItinerary();
 		basic.setData(itinerary);
 		basic.setMonitor(true);
 		basic.setName("test");
 		basic.setOriginalFrom(from);
 		basic.setOriginalTo(to);
 		basic = userService.saveSingleJourney(basic, Constants.USER_AUTH_TOKEN);
 		Assert.assertNotNull(basic);
 		// get single
 		basic = userService.getSingleJourney(basic.getClientId(),Constants.USER_AUTH_TOKEN);
 		Assert.assertNotNull(basic);
 		Assert.assertTrue(basic.isMonitor());
 		
 		// alert
 		int legIdx = -1;
 		for (int j = 0; j < itinerary.getLeg().size(); j++) {
 			Leg leg = itinerary.getLeg().get(j);
 			if (leg.getTransport().getType().equals(TType.BUS)) {
 				// delay
 				AlertDelay ac = new AlertDelay();
 				commonAttrs(ac);
 				List<Stop> stops = dataService.getStops(leg.getTransport().getAgencyId(),leg.getTransport().getRouteId(), Constants.USER_AUTH_TOKEN);
 				Stop s = stops.get(0);
 				ac.setPosition(new Position(s.getName(), new StopId(leg.getTransport().getAgencyId(), s.getId()), s.getId(), ""+s.getLongitude(), ""+s.getLatitude()));
 				ac.setTransport(leg.getTransport());
 				ac.setDelay(60*1000*10);
 				alertService.sendUserAlert(ac, Constants.USER_AUTH_TOKEN);
 				legIdx = j;
 				break;
 			}
 		}
 		Assert.assertTrue(legIdx >= 0);
 
 		// get single
 		basic = userService.getSingleJourney(basic.getClientId(),Constants.USER_AUTH_TOKEN);
 		Leg leg = basic.getData().getLeg().get(legIdx);
 		Assert.assertTrue(leg.getAlertDelayList() != null && !leg.getAlertDelayList().isEmpty());
 
 		Thread.sleep(2000);
 		nots = communicatorConnector.getNotificationsByApp(since, 0, -1, Constants.USER_AUTH_TOKEN);
 		Assert.assertEquals(1, nots.getNotifications().size()-size);
 		size = nots.getNotifications().size();
 	}
	
 }
