 package functional;
 
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 
 import models.User;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import play.Logger;
 import play.cache.Cache;
 import play.mvc.Http;
 import play.test.Fixtures;
 import play.test.FunctionalTest;
 import utils.GsonFactory;
 import DTO.MeetingDTO;
 import assemblers.MeetingAssembler;
 
 public class LocationsTest extends FunctionalTest {
 	
 	private Http.Request request;
 	private Http.Response response;
 
 	private static final String BASE_CONTROLLER_PATH = "/locations";
 	private static final String MEETINGS_CONTROLLER_PATH = "/meetings";
 	private String userBaseQuery = "?oauth_token=";
 	private String user1Query = "";
 	private String user2Query = "";
 	private String user3Query = "";
 	
 	private User user1;
 	private User user2;
 	private User user3;
 	
 	@Before
 	public void setUp() {
		Fixtures.deleteAll();
		Fixtures.load("data.yml");
 		Cache.clear();
 		
 		request = new Http.Request();
 		request.headers.put("accept", new Http.Header("Accept", "application/json"));
 		
 		user1 = User.find("byEmail", "bob@gmail.com").first();
 		user1Query += userBaseQuery + user1.accessToken;
 		
 		user2 = User.find("byEmail", "bob2@gmail.com").first();
 		user2Query += userBaseQuery + user2.accessToken;
 		
 		user3 = User.find("byEmail", "alex@jarvis.com").first();
 		user3Query += userBaseQuery + user3.accessToken;
 	}
 	
 	@After
 	public void log() {
 		if (response != null) {
 			Logger.debug("Response Status: " + response.status.toString());
 			Logger.debug("Response: " + (response.out.toString().isEmpty() ? "" : "\n" + response.out.toString()) );
 		}
 	}
 
 	@Test
 	public void testRecent() {
 		// Create a meeting fifteen minutes from now
 		Date now = new Date();
 		Calendar fifteenMinsFromNow = new GregorianCalendar();
 		fifteenMinsFromNow.setTime(now);
 		fifteenMinsFromNow.add(Calendar.MINUTE, 15);
 		DateFormat dateFormat = new SimpleDateFormat(GsonFactory.ISO8601_DATE_FORMAT);
 		String fifteenMinsFromNowString = dateFormat.format(fifteenMinsFromNow.getTime());
 		
 		String body = "{\"time\":\"" + fifteenMinsFromNowString + "\","
 			+ "\"place\":{\"latitude\":52.416117,\"longitude\":-4.083803},"
 			+ "\"attendees\":[{\"id\":" + user1.id + "},{\"id\":" + user2.id + "}],"
 			+ "\"title\":\"Meeting title\","
 			+ "\"description\":\"Meeting description\","
 			+ "\"type\":\"Meeting type\"}";
 		
 		Http.Request request = newRequest();
 		request.params.put("body", body);
 		
 		response = POST(request, MEETINGS_CONTROLLER_PATH + user1Query, "application/json; charset=UTF-8", body);
 		assertStatus(201, response);
 		
 		// Get user2 to accept the meeting
 		MeetingDTO meetingDTO = MeetingAssembler.meetingDTOWithJsonString(response.out.toString());
 		response = POST(MEETINGS_CONTROLLER_PATH + "/" + meetingDTO.id + "/accept" + user2Query);
 		assertIsOk(response);
 		
 		// Update user2's location
 		testUpdateUser2();
 		
 		// Get the Recent locations
 		response = GET(BASE_CONTROLLER_PATH + user1Query);
 		assertIsOk(response);
 		assertTrue(response.out.size() > 0);
 	}
 
 	@Test
 	public void testUpdate() {
 		DateFormat dateFormat = new SimpleDateFormat(GsonFactory.ISO8601_DATE_FORMAT);
 		String dateNow = dateFormat.format(new Date());
 		String body = "[{\"time\":\"" + dateNow + "\","
 			+ "\"coordinate\":{\"latitude\":52.416117,\"longitude\":-4.083803},"
 			+ "\"speed\":0,"
 			+ "\"altitude\":0,"
 			+ "\"trueHeading\":0,"
 			+ "\"verticalAccuracy\":0,"
 			+ "\"horizontalAccuracy\":0}]";
 		
 		Http.Request request = newRequest();
 		request.params.put("body", body);
 		
 		response = POST(request, BASE_CONTROLLER_PATH + user1Query, "application/json; charset=UTF-8", body);
 		
 		assertStatus(200, response);
 	}
 	
 	@Test
 	public void testUpdateUser2() {
 		DateFormat dateFormat = new SimpleDateFormat(GsonFactory.ISO8601_DATE_FORMAT);
 		String dateNow = dateFormat.format(new Date());
 		String body = "[{\"time\":\"" + dateNow + "\","
 			+ "\"coordinate\":{\"latitude\":52.416117,\"longitude\":-4.083803},"
 			+ "\"speed\":0,"
 			+ "\"altitude\":0,"
 			+ "\"trueHeading\":0,"
 			+ "\"verticalAccuracy\":0,"
 			+ "\"horizontalAccuracy\":0}]";
 		
 		Http.Request request = newRequest();
 		request.params.put("body", body);
 		
 		response = POST(request, BASE_CONTROLLER_PATH + user2Query, "application/json; charset=UTF-8", body);
 		
 		assertStatus(200, response);
 	}
 	
 }
