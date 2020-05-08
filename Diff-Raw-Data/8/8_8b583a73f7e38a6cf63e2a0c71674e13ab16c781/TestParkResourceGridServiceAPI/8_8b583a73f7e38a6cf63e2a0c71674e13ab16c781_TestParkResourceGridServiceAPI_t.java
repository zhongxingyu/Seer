 package parkservice.gridservice;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import javax.xml.bind.JAXBElement;
 import javax.xml.namespace.QName;
 
 import com.parq.server.dao.MiscellaneousDao;
 import com.parq.server.dao.ParkingSpaceDao;
 import com.parq.server.dao.ParkingStatusDao;
 import com.parq.server.dao.UserDao;
 import com.parq.server.dao.model.object.ParkingInstance;
 import com.parq.server.dao.model.object.ParkingSpace;
 import com.parq.server.dao.model.object.Payment;
 import com.parq.server.dao.model.object.User;
 import com.parq.server.dao.model.object.UserSelfReporting;
 import com.parq.server.dao.model.object.Payment.PaymentType;
 import com.parq.server.dao.support.SupportScriptForDaoTesting;
 import com.parq.server.grid.GridManagementService;
 
 import parkservice.gridservice.model.FindGridsByGPSCoordinateRequest;
 import parkservice.gridservice.model.FindGridsByGPSCoordinateResponse;
 import parkservice.gridservice.model.GetSpotLevelInfoRequest;
 import parkservice.gridservice.model.GetSpotLevelInfoResponse;
 import parkservice.gridservice.model.GetUpdatedSpotLevelInfoRequest;
 import parkservice.gridservice.model.GetUpdatedSpotLevelInfoResponse;
 import parkservice.gridservice.model.GetUpdatedStreetInfoRequest;
 import parkservice.gridservice.model.GetUpdatedStreetInfoResponse;
 import parkservice.gridservice.model.GpsCoordinate;
 import parkservice.gridservice.model.SearchArea;
 import parkservice.gridservice.model.SearchForStreetsRequest;
 import parkservice.gridservice.model.SearchForStreetsResponse;
 import parkservice.gridservice.model.UserLoginResponse;
 import parkservice.model.AuthRequest;
 import parkservice.resources.ParkResource;
 import parkservice.userscore.model.AddUserReportingRequest;
 import parkservice.userscore.model.AddUserReportingResponse;
 import parkservice.userscore.model.GetCountRequest;
 import parkservice.userscore.model.GetCountResponse;
 import parkservice.userscore.model.GetUserReportingHistoryRequest;
 import parkservice.userscore.model.GetUserReportingHistoryResponse;
 import parkservice.userscore.model.GetUserScoresRequest;
 import parkservice.userscore.model.GetUserScoreResponse;
 import parkservice.userscore.model.Score;
 import parkservice.userscore.model.UpdateUserScoreRequest;
 import parkservice.userscore.model.UpdateUserScoreResponse;
 import parkservice.userscore.model.UserParkingStatusReport;
 import junit.framework.TestCase;
 
 public class TestParkResourceGridServiceAPI extends TestCase {
 	
 	private long testUserId;
 	
 	public TestParkResourceGridServiceAPI() {
 		UserDao userDao = new UserDao();
 		User user = userDao.getUserByEmail(SupportScriptForDaoTesting.userEmail);
 		testUserId = user.getUserID();
 	}
 	
 	public void testBasicFindGridByGPSCoor() {
 		ParkResource parkResource = new ParkResource();
 		
 		FindGridsByGPSCoordinateRequest findGridByGPSCoordinateRequest = new FindGridsByGPSCoordinateRequest();
 		
 		GpsCoordinate northEast = new GpsCoordinate();
 		northEast.setLatitude(180);
 		northEast.setLongitude(180);
 		GpsCoordinate southWest = new GpsCoordinate();
 		southWest.setLatitude(-180);
 		southWest.setLongitude(-180);
 		
 		findGridByGPSCoordinateRequest.setLastUpdateTime(0);
 		List<SearchArea> testSearchArea = new ArrayList<SearchArea>();
 		testSearchArea.add(new SearchArea());
 		testSearchArea.get(0).setNorthEastCorner(northEast);
 		testSearchArea.get(0).setSouthWestCorner(southWest);
 		findGridByGPSCoordinateRequest.setSearchArea(testSearchArea);
 		
 		JAXBElement<FindGridsByGPSCoordinateRequest> testRequest = new JAXBElement<FindGridsByGPSCoordinateRequest>(
 				new QName("Test"), FindGridsByGPSCoordinateRequest.class, findGridByGPSCoordinateRequest);
 		
 		FindGridsByGPSCoordinateResponse[] response = parkResource.findGridByGPSCoor(testRequest);
 		
 		assertNotNull(response);
 		assertTrue(response.length > 0);	
 		
 		// test for search with no result based on time stamp
 		findGridByGPSCoordinateRequest.setLastUpdateTime(System.currentTimeMillis() + 10000);
 		FindGridsByGPSCoordinateResponse[] response1 = parkResource.findGridByGPSCoor(testRequest);
 		assertNotNull(response1);
 		assertEquals(response1.length, 0);
 		
 		// test for search with no result from small search area
 		northEast.setLatitude(0.0);
 		northEast.setLongitude(0.0);
 		southWest.setLatitude(0.001);
 		southWest.setLongitude(0.001);
 		findGridByGPSCoordinateRequest.setLastUpdateTime(0);
 		FindGridsByGPSCoordinateResponse[] response2 = parkResource.findGridByGPSCoor(testRequest);
 		assertNotNull(response2);
 		assertEquals(response2.length, 0);
 	}
 	
 	public void testBasicSearchForStreets() {
 		ParkResource parkResource = new ParkResource();
 		
 		SearchForStreetsRequest searchForStreetsRequest = new SearchForStreetsRequest();
 		GpsCoordinate northEast = new GpsCoordinate();
 		northEast.setLatitude(180);
 		northEast.setLongitude(180);
 		GpsCoordinate southWest = new GpsCoordinate();
 		southWest.setLatitude(-180);
 		southWest.setLongitude(-180);
 		
 		List<SearchArea> testSearchArea = new ArrayList<SearchArea>();
 		testSearchArea.add(new SearchArea());
 		testSearchArea.get(0).setNorthEastCorner(northEast);
 		testSearchArea.get(0).setSouthWestCorner(southWest);
 		searchForStreetsRequest.setSearchArea(testSearchArea);
 		
 		JAXBElement<SearchForStreetsRequest> jaxbTestRequest = new JAXBElement<SearchForStreetsRequest>(
 				new QName("Test"), SearchForStreetsRequest.class, searchForStreetsRequest);
 		
 		SearchForStreetsResponse[] response = parkResource.searchForStreets(jaxbTestRequest);
 		assertNotNull(response);
 		assertTrue(response.length > 0);
 		
 		// test for search with no result
 		southWest.setLatitude(0.0);
 		southWest.setLongitude(0.0);
 		northEast.setLatitude(0.001);
 		northEast.setLongitude(0.001);
 		SearchForStreetsResponse[] response1 = parkResource.searchForStreets(jaxbTestRequest);
 		assertNotNull(response1);
 		assertEquals(response1.length, 0);
 	}
 	
 	public void testBasicGetUpdatedStreetInfo() {
 		ParkResource parkResource = new ParkResource();
 		
 		GetUpdatedStreetInfoRequest getUpdatedStreetInfoRequest = new GetUpdatedStreetInfoRequest();
 		GpsCoordinate northEast = new GpsCoordinate();
 		northEast.setLatitude(180);
 		northEast.setLongitude(180);
 		GpsCoordinate southWest = new GpsCoordinate();
 		southWest.setLatitude(-180);
 		southWest.setLongitude(-180);
 		
 		List<SearchArea> testSearchArea = new ArrayList<SearchArea>();
 		testSearchArea.add(new SearchArea());
 		testSearchArea.get(0).setNorthEastCorner(northEast);
 		testSearchArea.get(0).setSouthWestCorner(southWest);
 		getUpdatedStreetInfoRequest.setSearchArea(testSearchArea);
 		getUpdatedStreetInfoRequest.setLastUpdateTime(0);
 		JAXBElement<GetUpdatedStreetInfoRequest> jaxbTestRequest = new JAXBElement<GetUpdatedStreetInfoRequest>(
 				new QName("Test"), GetUpdatedStreetInfoRequest.class, getUpdatedStreetInfoRequest);
 		
 		GetUpdatedStreetInfoResponse[] response = parkResource.getUpdatedStreetInfo(jaxbTestRequest);
 		assertNotNull(response);
 		assertTrue(response.length > 0);
 		
 		// test for search with no result based on time stamp
 		getUpdatedStreetInfoRequest.setLastUpdateTime(System.currentTimeMillis() + 10000);
 		GetUpdatedStreetInfoResponse[] response1 = parkResource.getUpdatedStreetInfo(jaxbTestRequest);
 		assertNotNull(response1);
 		assertEquals(response1.length, 0);
 		
 		// test for search with no result from small search area
 		southWest.setLatitude(0.0);
 		southWest.setLongitude(0.0);
 		northEast.setLatitude(0.001);
 		northEast.setLongitude(0.001);
 		getUpdatedStreetInfoRequest.setLastUpdateTime(0);
 		GetUpdatedStreetInfoResponse[] response2 = parkResource.getUpdatedStreetInfo(jaxbTestRequest);
 		assertNotNull(response2);
 		assertEquals(response2.length, 0);
 	}
 	
 	public void testBasicGetStreetInfo() {
 		ParkResource parkResource = new ParkResource();
 		
 		GetSpotLevelInfoRequest getStreetInfoRequest = new GetSpotLevelInfoRequest();
 		GpsCoordinate northEast = new GpsCoordinate();
 		northEast.setLatitude(180);
 		northEast.setLongitude(180);
 		GpsCoordinate southWest = new GpsCoordinate();
 		southWest.setLatitude(-180);
 		southWest.setLongitude(-180);
 		
 		List<SearchArea> testSearchArea = new ArrayList<SearchArea>();
 		testSearchArea.add(new SearchArea());
 		testSearchArea.get(0).setNorthEastCorner(northEast);
 		testSearchArea.get(0).setSouthWestCorner(southWest);
 		getStreetInfoRequest.setSearchArea(testSearchArea);
 		JAXBElement<GetSpotLevelInfoRequest> jaxbTestRequest = new JAXBElement<GetSpotLevelInfoRequest>(
 				new QName("Test"), GetSpotLevelInfoRequest.class, getStreetInfoRequest);
 		
 		GetSpotLevelInfoResponse[] response = parkResource.getStreetInfo(jaxbTestRequest);
 		assertNotNull(response);
 		assertTrue(response.length > 0);
 		assertTrue(response[0].getParkingSpace().get(0).getSegment() > 0);
 		assertTrue(response[0].getParkingSpace().get(0).getSegment() < 100);
 		assertNotNull(response[0].getParkingSpace().get(0).getSpaceName());
 		assertNotNull(response[0].getParkingSpace().get(0).getSpaceName().length() > 0);
 		
 		// test for search with no result
 		southWest.setLatitude(0.0);
 		southWest.setLongitude(0.0);
 		northEast.setLatitude(0.001);
 		northEast.setLongitude(0.001);
 		GetSpotLevelInfoResponse[] response1 = parkResource.getStreetInfo(jaxbTestRequest);
 		assertNotNull(response1);
 		assertEquals(response1.length, 0);
 	}
 	
 	public void testConfinedSearchForGridAndStreet() {
 		// get the test grid
 		ParkResource parkResource = new ParkResource();
 		FindGridsByGPSCoordinateRequest findGridByGPSCoordinateRequest = new FindGridsByGPSCoordinateRequest();
 		GpsCoordinate northEast = new GpsCoordinate();
 		northEast.setLatitude(180);
 		northEast.setLongitude(180);
 		GpsCoordinate southWest = new GpsCoordinate();
 		southWest.setLatitude(-180);
 		southWest.setLongitude(-180);
 		
 		List<SearchArea> testSearchArea = new ArrayList<SearchArea>();
 		testSearchArea.add(new SearchArea());
 		testSearchArea.get(0).setNorthEastCorner(northEast);
 		testSearchArea.get(0).setSouthWestCorner(southWest);
 		findGridByGPSCoordinateRequest.setSearchArea(testSearchArea);
 		findGridByGPSCoordinateRequest.setLastUpdateTime(0);
 		JAXBElement<FindGridsByGPSCoordinateRequest> testRequest = new JAXBElement<FindGridsByGPSCoordinateRequest>(
 				new QName("Test"), FindGridsByGPSCoordinateRequest.class, findGridByGPSCoordinateRequest);
 		
 		FindGridsByGPSCoordinateResponse[] responseGrid = parkResource.findGridByGPSCoor(testRequest);
 		
 		
 		long testGridId = responseGrid[0].getGridId();
 		double gridLat = responseGrid[0].getLatitude();
 		double gridLong = responseGrid[0].getLongitude();
 		double curFillRate = responseGrid[0].getFillRate();
 		
 		// test that the initial value is correct
 		assertEquals(0.0, curFillRate);
 		
 		// do a confined grid search
 		southWest.setLatitude(gridLat - 0.001);
 		southWest.setLongitude(gridLong - 0.001);
 		northEast.setLatitude(gridLat + 0.001);
 		northEast.setLongitude(gridLong + 0.001);
 		
 		responseGrid = parkResource.findGridByGPSCoor(testRequest);
 		assertNotNull(responseGrid);
 		assertEquals(testGridId, responseGrid[0].getGridId());
 		
 		// search for streets in gps coor
 		southWest.setLatitude(-180);
 		southWest.setLongitude(-180);
 		northEast.setLatitude(180);
 		northEast.setLongitude(180);
 		SearchForStreetsRequest searchForStreetsRequest = new SearchForStreetsRequest();
 		searchForStreetsRequest.setSearchArea(testSearchArea);
 	
 		JAXBElement<SearchForStreetsRequest> jaxbTestRequest = new JAXBElement<SearchForStreetsRequest>(
 				new QName("Test"), SearchForStreetsRequest.class, searchForStreetsRequest);
 		SearchForStreetsResponse[] responseStreet = parkResource.searchForStreets(jaxbTestRequest);
 		assertNotNull(responseStreet);
 		assertTrue(responseStreet.length > 0);
 		
 		long testStreetId = responseStreet[0].getStreetId();
 		//double streetLat = responseStreet[0].getGpsCoor().get(0).getLatitude();
 		// double streetLong = responseStreet[0].getGpsCoor().get(0).getLongitude();
 		double streetFillRate = responseStreet[0].getFillRate();
 		
 		// test that the initial value is correct
 		assertEquals(0.0, streetFillRate);
 		
 		// do a confined street search
 		southWest.setLatitude(0);
 		southWest.setLongitude(0);
 		northEast.setLatitude(6);
 		northEast.setLongitude(6);
 		responseStreet = parkResource.searchForStreets(jaxbTestRequest);
 		assertNotNull(responseStreet);
 		assertTrue(responseStreet.length >= 1);
 		assertEquals(testStreetId, responseStreet[0].getStreetId());
 	}
 
 	public void testGetUpdatedSpotLevelInfoResponse() {
 		// search for streets in gps coor
 		ParkResource parkResource = new ParkResource();
 		GpsCoordinate northEast = new GpsCoordinate();
 		northEast.setLatitude(180);
 		northEast.setLongitude(180);
 		GpsCoordinate southWest = new GpsCoordinate();
 		southWest.setLatitude(-180);
 		southWest.setLongitude(-180);
 		
 		List<SearchArea> testSearchArea = new ArrayList<SearchArea>();
 		testSearchArea.add(new SearchArea());
 		testSearchArea.get(0).setNorthEastCorner(northEast);
 		testSearchArea.get(0).setSouthWestCorner(southWest);
 		
 		GetUpdatedSpotLevelInfoRequest getStreetInfoRequest = new GetUpdatedSpotLevelInfoRequest();
 		getStreetInfoRequest.setSearchArea(testSearchArea);
 		
 		JAXBElement<GetUpdatedSpotLevelInfoRequest> jaxbTestStreetRequest = new JAXBElement<GetUpdatedSpotLevelInfoRequest>(
 				new QName("Test"), GetUpdatedSpotLevelInfoRequest.class, getStreetInfoRequest);
 		
 		GetUpdatedSpotLevelInfoResponse[] responseStreet = parkResource.getUpdatedSpotLevelInfo(jaxbTestStreetRequest);
 		assertNotNull(responseStreet);
 		assertFalse(responseStreet.length == 0);
 		assertNotNull(responseStreet[0].getParkingSpace());
 		assertFalse(responseStreet[0].getParkingSpace().size() == 0);
 	}
 	
 	public void testFillRateUpdate() {
 		// search for streets in gps coor
 		ParkResource parkResource = new ParkResource();
 		
 		GetSpotLevelInfoRequest getStreetInfoRequest = new GetSpotLevelInfoRequest();
 		GpsCoordinate northEast = new GpsCoordinate();
 		northEast.setLatitude(180);
 		northEast.setLongitude(180);
 		GpsCoordinate southWest = new GpsCoordinate();
 		southWest.setLatitude(-180);
 		southWest.setLongitude(-180);
 		
 		List<SearchArea> testSearchArea = new ArrayList<SearchArea>();
 		testSearchArea.add(new SearchArea());
 		testSearchArea.get(0).setNorthEastCorner(northEast);
 		testSearchArea.get(0).setSouthWestCorner(southWest);
 		getStreetInfoRequest.setSearchArea(testSearchArea);
 	
 		JAXBElement<GetSpotLevelInfoRequest> jaxbTestRequest = new JAXBElement<GetSpotLevelInfoRequest>(
 				new QName("Test"), GetSpotLevelInfoRequest.class, getStreetInfoRequest);
 		GetSpotLevelInfoResponse[] responseStreet = parkResource.getStreetInfo(jaxbTestRequest);
 		assertNotNull(responseStreet);
 		
 		// park at the first street's first space for 5 second
 		long spaceToParkAtId = responseStreet[0].getParkingSpace().get(0).getSpaceId();
 		GridManagementService gms = GridManagementService.getInstance();
 		gms.park(spaceToParkAtId, new Date(System.currentTimeMillis() + 5000));
 		
 		responseStreet = parkResource.getStreetInfo(jaxbTestRequest);
 		assertTrue(responseStreet[0].getFillRate() > 0.01);
 		assertTrue(responseStreet[0].getParkingSpace().get(0).getSegment() > 0);
 		assertNotNull(responseStreet[0].getParkingSpace().get(0).getSpaceName());
 		
 		// make sure the grid data also is updated
 		FindGridsByGPSCoordinateRequest findGridByGPSCoordinateRequest = new FindGridsByGPSCoordinateRequest();
 		findGridByGPSCoordinateRequest.setLastUpdateTime(0);
 		findGridByGPSCoordinateRequest.setSearchArea(testSearchArea);
 		JAXBElement<FindGridsByGPSCoordinateRequest> testRequest = new JAXBElement<FindGridsByGPSCoordinateRequest>(
 				new QName("Test"), FindGridsByGPSCoordinateRequest.class, findGridByGPSCoordinateRequest);
 		
 		FindGridsByGPSCoordinateResponse[] responseGrid = parkResource.findGridByGPSCoor(testRequest);
 		assertTrue(responseGrid[0].getFillRate() > 0.01);
 		
 		// wait 32 second to see if the status are updated correctly
 		try {
 			Thread.sleep(32000);
 		} catch (InterruptedException ie) {}
 		
 		responseStreet = parkResource.getStreetInfo(jaxbTestRequest);
 		assertEquals(responseStreet[0].getFillRate(), 0.0);
 		
 		responseGrid = parkResource.findGridByGPSCoor(testRequest);
 		assertEquals(responseGrid[0].getFillRate(), 0.0);
 	}
 	
 	public void testFullParkingScenrio() {
 		// search for streets in gps coor
 		ParkResource parkResource = new ParkResource();
 		
 		GetSpotLevelInfoRequest getStreetInfoRequest = new GetSpotLevelInfoRequest();
 		GpsCoordinate northEast = new GpsCoordinate();
 		northEast.setLatitude(180);
 		northEast.setLongitude(180);
 		GpsCoordinate southWest = new GpsCoordinate();
 		southWest.setLatitude(-180);
 		southWest.setLongitude(-180);
 		
 		List<SearchArea> testSearchArea = new ArrayList<SearchArea>();
 		testSearchArea.add(new SearchArea());
 		testSearchArea.get(0).setNorthEastCorner(northEast);
 		testSearchArea.get(0).setSouthWestCorner(southWest);
 		getStreetInfoRequest.setSearchArea(testSearchArea);
 		
 		JAXBElement<GetSpotLevelInfoRequest> jaxbTestStreetRequest = new JAXBElement<GetSpotLevelInfoRequest>(
 				new QName("Test"), GetSpotLevelInfoRequest.class, getStreetInfoRequest);
 		
 		FindGridsByGPSCoordinateRequest findGridByGPSCoordinateRequest = new FindGridsByGPSCoordinateRequest();
 		findGridByGPSCoordinateRequest.setLastUpdateTime(0);
 		findGridByGPSCoordinateRequest.setSearchArea(testSearchArea);
 		JAXBElement<FindGridsByGPSCoordinateRequest> jaxbTestGridRequest = new JAXBElement<FindGridsByGPSCoordinateRequest>(
 				new QName("Test"), FindGridsByGPSCoordinateRequest.class, findGridByGPSCoordinateRequest);
 		
 		FindGridsByGPSCoordinateResponse[] responseGrid = parkResource.findGridByGPSCoor(jaxbTestGridRequest);
 		GetSpotLevelInfoResponse[] responseStreet = parkResource.getStreetInfo(jaxbTestStreetRequest);
 		long spaceToParkAtId = responseStreet[0].getParkingSpace().get(0).getSpaceId();
 		//long streetToParkAtId = responseStreet[0].getStreetId();
 		//long gridToParkAtId = responseGrid[0].getGridId();
 		
 		responseStreet = parkResource.getStreetInfo(jaxbTestStreetRequest);
 		assertEquals(responseStreet[0].getFillRate(), 0.0);
 		assertEquals(responseGrid[0].getFillRate(), 0.0);
 		
 		// park for 5 seconds
 		ParkingInstance pi = new ParkingInstance();
 		pi.setPaidParking(true);
 		pi.setParkingBeganTime(new Date(System.currentTimeMillis()));
 		pi.setParkingEndTime(new Date(System.currentTimeMillis() + 5000));
 		pi.setSpaceId(spaceToParkAtId);
 		pi.setUserId(testUserId);
 		// set the payment type;
 		Payment payment = new Payment();
 		payment.setAmountPaidCents(15);
 		payment.setPaymentRefNumber("Test");
 		payment.setPaymentDateTime(new Date(System.currentTimeMillis()));
 		payment.setPaymentType(PaymentType.PrePaid);
 		payment.setAccountId(-1);
 		pi.setPaymentInfo(payment);
 		
 		// park with the parking status dao
 		ParkingStatusDao psDao = new ParkingStatusDao();
 		psDao.addNewParkingAndPayment(pi);
 		
 		// make sure the grid and street data are updated
 		responseGrid = parkResource.findGridByGPSCoor(jaxbTestGridRequest);
 		assertTrue(responseGrid[0].getFillRate() > 0.01);
 		responseStreet = parkResource.getStreetInfo(jaxbTestStreetRequest);
 		assertTrue(responseStreet[0].getFillRate() > 0.01);
 		
 		// wait 32 second to see if the status are updated correctly
 		try {
 			Thread.sleep(32000);
 		} catch (InterruptedException ie) {}
 		responseStreet = parkResource.getStreetInfo(jaxbTestStreetRequest);
 		assertEquals(responseStreet[0].getFillRate(), 0.0);
 		responseGrid = parkResource.findGridByGPSCoor(jaxbTestGridRequest);
 		assertEquals(responseGrid[0].getFillRate(), 0.0);
 		
 		// park for 500 second
 		pi.setParkingEndTime(new Date(System.currentTimeMillis() + 500000));
 		psDao.addNewParkingAndPayment(pi);
 		
 		// make sure the grid and street data are updated
 		responseGrid = parkResource.findGridByGPSCoor(jaxbTestGridRequest);
 		assertTrue(responseGrid[0].getFillRate() > 0.01);
 		responseStreet = parkResource.getStreetInfo(jaxbTestStreetRequest);
 		assertTrue(responseStreet[0].getFillRate() > 0.01);
 		
 		// wait 2 second then unpark
 		try {
 			Thread.sleep(2000);
 		} catch (InterruptedException ie) {}
 		
 		// unpark
 		List<ParkingInstance> piLists = psDao.getParkingStatusBySpaceIds(new long[]{spaceToParkAtId});
 		psDao.unparkBySpaceIdAndParkingRefNum(spaceToParkAtId, 
 				piLists.get(0).getParkingRefNumber(), new Date(System.currentTimeMillis()));
 		
 		responseStreet = parkResource.getStreetInfo(jaxbTestStreetRequest);
 		assertEquals(responseStreet[0].getFillRate(), 0.0);
 		responseGrid = parkResource.findGridByGPSCoor(jaxbTestGridRequest);
 		assertEquals(responseGrid[0].getFillRate(), 0.0);
 		
 	}
 	
 	public void testGridServiceUpdateThreadIsRunning() {
 		try {
 			Thread.sleep(61000);
 		} catch (InterruptedException ie) {}
 		// expecting to see 2 system.out.println messages
 	}
 	
 	public void testGetUserScore() {
 		ParkResource parkResource = new ParkResource();
 		
 		// create request object
 		GetUserScoresRequest request = new GetUserScoresRequest();
 		request.setUserId(testUserId);
 		
 		// call the ParkResource
 		JAXBElement<GetUserScoresRequest> jaxbRequest = new JAXBElement<GetUserScoresRequest>(
 				new QName("Test"), GetUserScoresRequest.class, request);
 		GetUserScoreResponse response = parkResource.getUserScore(jaxbRequest);
 		
 		// validate response
 		List<Score> scores = response.getScores();
 		for (Score score: scores) {
 			assertEquals(testUserId, score.getUserId());
 			assertTrue(score.getScoreId() > 0);
 			assertTrue(score.getScore1() >= 0);
 			assertTrue(score.getScore2() >= 0);
 			assertTrue(score.getScore3() >= 0);
 		}
 	}
 	
 	public void testUpdateUserScore() {
 		ParkResource parkResource = new ParkResource();
 		
 		// create request object
 		UpdateUserScoreRequest request = new UpdateUserScoreRequest();
 		Score uScore = new Score();
 		uScore.setUserId(testUserId);
 		uScore.setScore1(5);
 		uScore.setScore2(10);
 		uScore.setScore3(15);
 		request.setScore(uScore);
 		
 		// call the ParkResource
 		JAXBElement<UpdateUserScoreRequest> jaxbRequest = new JAXBElement<UpdateUserScoreRequest>(
 				new QName("Test"), UpdateUserScoreRequest.class, request);
 		UpdateUserScoreResponse response = parkResource.updateUserScore(jaxbRequest);
 		
 		// check the response
 		assertTrue(response.isUpdateSuccessful());
 		
 		// create request object
 		GetUserScoresRequest request2 = new GetUserScoresRequest();
 		request2.setUserId(testUserId);
 		
 		// call the ParkResource
 		JAXBElement<GetUserScoresRequest> jaxbRequest2 = new JAXBElement<GetUserScoresRequest>(
 				new QName("Test"), GetUserScoresRequest.class, request2);
 		GetUserScoreResponse response2 = parkResource.getUserScore(jaxbRequest2);
 		
 		// validate response
 		List<Score> scores = response2.getScores();
 		Score score = scores.get(0);
 		assertEquals(testUserId, score.getUserId());
 		assertTrue(score.getScoreId() > 0);
 		assertEquals(5, score.getScore1());
 		assertEquals(10, score.getScore2());
 		assertEquals(15, score.getScore3());
 	}
 	
 	public void testGetCount() {
 		ParkResource parkResource = new ParkResource();
 		GetCountRequest request = new GetCountRequest();
 		
 		// call the ParkResource
 		JAXBElement<GetCountRequest> jaxbRequest = new JAXBElement<GetCountRequest>(
 				new QName("Test"), GetCountRequest.class, request);
 		
 		int pre = -1;
 		int cur = 0;
 		
 		int[] intArray = new int[5];
 		
 		for (int i = 0; i < 100; i++) {
 			GetCountResponse response = parkResource.getCount(jaxbRequest);
 			cur = response.getCount();
 			assertTrue(pre != cur);
 			assertTrue(cur >= 0);
 			assertTrue(cur <= 3);
 			pre = cur;
 			intArray[cur] = intArray[cur] + 1;
 		}
 		
 		assertTrue(intArray[0] > 10);
 		assertTrue(intArray[1] > 10);
 		assertTrue(intArray[2] > 10);
 		assertTrue(intArray[3] > 10);
 		assertEquals(0, intArray[4]);
 	}
 	
 	public void testAddUserReporting() {
 		ParkResource parkResource = new ParkResource();
 		AddUserReportingRequest addRequest = new AddUserReportingRequest();
 		
 		User user = new UserDao()
 				.getUserByEmail(SupportScriptForDaoTesting.userEmail);
 		ParkingSpace pSpace = new ParkingSpaceDao()
 			.getParkingSpaceBySpaceIdentifier(
 				SupportScriptForDaoTesting.parkingLocationNameMain, SupportScriptForDaoTesting.spaceNameMain);
 		
 		addRequest.setUserId(user.getUserID());
 		addRequest.setSpaceId(pSpace.getSpaceId());
 		addRequest.setScore1((int)(Math.random() * 1000));
 		addRequest.setScore2((int)(Math.random() * 1000));
 		addRequest.setScore3((int)(Math.random() * 1000));
 		addRequest.setScore4((int)(Math.random() * 1000));
 		addRequest.setScore5((int)(Math.random() * 1000));
 		addRequest.setScore6((int)(Math.random() * 1000));
 		
 		// call the ParkResource
 		JAXBElement<AddUserReportingRequest> jaxbRequest = new JAXBElement<AddUserReportingRequest>(
 				new QName("Test"), AddUserReportingRequest.class, addRequest);
 		AddUserReportingResponse response = 
 			parkResource.addUserReporting(jaxbRequest);
 		
 		// check the add result
 		assertTrue(response.isUpdateSuccessful());
 		
 		MiscellaneousDao miscDao = new MiscellaneousDao();
 		List<UserSelfReporting> reports = miscDao.getUserSelfReportingHistoryForUser(user.getUserID());
 		assertNotNull(reports);
 		assertTrue(reports.size() > 0);
 		UserSelfReporting userReport2 = reports.get(0);
 		
 		assertTrue(userReport2.getReportId() > 0);
 		assertNotNull(userReport2.getReportDateTime());
 		System.out.println(userReport2.getReportDateTime());
 		assertEquals(addRequest.getUserId(), userReport2.getUserId());
 		assertEquals(addRequest.getSpaceId(), userReport2.getSpaceId());
 		assertEquals(addRequest.getScore1(), userReport2.getScore1());
 		assertEquals(addRequest.getScore2(), userReport2.getScore2());
 		assertEquals(addRequest.getScore3(), userReport2.getScore3());
 		assertEquals(addRequest.getScore4(), userReport2.getScore4());
 		assertEquals(addRequest.getScore5(), userReport2.getScore5());
 		assertEquals(addRequest.getScore6(), userReport2.getScore6());
 	}
 	
 	public void testGetUserReportingHistory() {
 		ParkResource parkResource = new ParkResource();
 		
 		GetUserReportingHistoryRequest getHistoryRequest
 			= new GetUserReportingHistoryRequest();
 		User user = new UserDao()
 			.getUserByEmail(SupportScriptForDaoTesting.userEmail);
 		getHistoryRequest.setUserId(user.getUserID());
 		
 		// call the ParkResource
 		JAXBElement<GetUserReportingHistoryRequest> jaxbRequest = 
 			new JAXBElement<GetUserReportingHistoryRequest>(
 				new QName("Test"), GetUserReportingHistoryRequest.class, getHistoryRequest);
 		GetUserReportingHistoryResponse response = 
 			parkResource.getUserReportingHistory(jaxbRequest);
 		
 		List<UserParkingStatusReport> reportsHistory = response.getReports();
 		assertTrue(reportsHistory.size() > 0);
 		assertEquals(user.getUserID(), reportsHistory.get(0).getUserId());
 		assertTrue(reportsHistory.get(0).getReportId() > 0);
 		// TEST parking time to the nearest minute
 		assertEquals(new Date(System.currentTimeMillis()).getTime() / 60000,
 				reportsHistory.get(0).getReportDateTime().getTime() / 60000);
 		assertTrue(reportsHistory.get(0).getScore1() > 0);
 	}
 	
 	public void testLogin() {
 		ParkResource parkResource = new ParkResource();
 		
 		AuthRequest authRequest = new AuthRequest();
 		authRequest.setEmail(SupportScriptForDaoTesting.userEmail);
 		authRequest.setPassword(SupportScriptForDaoTesting.userPassWord);
 		JAXBElement<AuthRequest> jaxbRequest = 
 			new JAXBElement<AuthRequest>(
 				new QName("Test"), AuthRequest.class, authRequest);
 		
 		UserLoginResponse response = parkResource.login(jaxbRequest);
 		assertTrue(response.getUid() > 0);
 		assertEquals(SupportScriptForDaoTesting.userEmail, response.getEmail());
 //		assertTrue(response.getLicense() != null);
 //		assertTrue(response.getLicense().length() > 0);
 //		System.out.println(response.getLicense());
 		assertTrue(response.getBalance() > 50);
 	}
 }
