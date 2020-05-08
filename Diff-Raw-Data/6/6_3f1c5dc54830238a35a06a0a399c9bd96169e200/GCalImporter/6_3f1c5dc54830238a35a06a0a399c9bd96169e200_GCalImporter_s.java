 package calendar_importers;
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.net.MalformedURLException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.net.URLEncoder;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.swing.JOptionPane;
 
 import calendar.CalendarGroup;
 import calendar.CalendarResponses;
 import calendar.GoogleCalendars;
 import calendar.Owner;
 import calendar.Response;
 
 import com.google.gdata.client.calendar.CalendarQuery;
 import com.google.gdata.client.calendar.CalendarService;
 import com.google.gdata.data.calendar.CalendarEntry;
 import com.google.gdata.data.calendar.CalendarEventEntry;
 import com.google.gdata.data.calendar.CalendarEventFeed;
 import com.google.gdata.data.calendar.CalendarFeed;
 import com.google.gdata.data.extensions.When;
 import com.google.gdata.util.ServiceException;
 
 //import org.apache.commons.httpclient.util.URIUtil.encodeQuery;
 
 
 
 //auth stuff:
 	//http://code.google.com/apis/gdata/docs/auth/clientlogin.html
 	//http://www.java-samples.com/java/POST-toHTTPS-url-free-java-sample-program.htm
 	//http://www.java-samples.com/java/POST-toHTTPS-url-free-java-sample-program.htm
 	//http://code.google.com/apis/gdata/docs/auth/overview.html#ClientLogin
 
 	//https://developers.google.com/accounts/docs/OAuth2
 	//https://developers.google.com/accounts/docs/OAuth2InstalledApp
 	//http://code.google.com/p/google-api-java-client/wiki/OAuth2
 	//https://code.google.com/apis/console/#project:1034117539945:stats:calendar
 	//https://developers.google.com/google-apps/calendar/
 //edit recurring events
 
 //edit all day
 
 /*
  * Function: GoogleCalendars importMyGCal(org.joda.time.DateTime startTime, org.joda.time.DateTime endTime)
  * Function: GoogleCalendars refresh(org.joda.time.DateTime startTime, org.joda.time.DateTime endTime)
  */
 
 public class GCalImporter implements CalendarsImporter<CalendarResponses> {
 	private CalendarService _client;
 	int MAX_RESPONSES = Integer.MAX_VALUE;
 	private Owner _owner;
 	private String CLIENT_ID;
 	private String CLIENT_SECRET;
 	private String _refreshToken;
 	
 	public GCalImporter() {
 		//connect to client
 		_client = new CalendarService("yourCompany-yourAppName-v1");
 	}
 	
 	public CalendarGroup<CalendarResponses> importMyGCal(org.joda.time.DateTime startTime, org.joda.time.DateTime endTime) throws IOException, ServiceException, com.google.gdata.util.ServiceException {
 		//authenticate user
 		this.setAuth();
 		//import calendars -- make calendar group
 		return this.importCalendarGroup(startTime, endTime);
 	}
 	
 //	public void setAuth() throws AuthenticationException, MalformedURLException, com.google.gdata.util.AuthenticationException {
 //		//Get username and password from GUI
 //		String username = (String) new JOptionPane().showInputDialog("Please type Google Calendar username:");
 //		_owner = new OwnerImpl(username);
 //		JOptionPane pwordPane = new JOptionPane();
 //		JPasswordField jpf = new JPasswordField();
 //		pwordPane.showConfirmDialog(null, jpf, "Please type Google Calendar password:", pwordPane.OK_CANCEL_OPTION);
 //		char[] passwordArray = jpf.getPassword();
 //		StringBuffer passwordString = new StringBuffer();
 //		for (int i = 0; i < passwordArray.length; i++) {
 //			passwordString.append(passwordArray[i]);
 //		}
 //		String password = passwordString.toString();
 //		
 //		//TODO: set up cookie/token stuff			
 //		//URL feedURL = new URL("https://www.google.com/accounts/ClientLogin");
 //		//HttpsURLConnection connection = (HttpsURLConnection) URL.openConnection();
 //		//connection.setDoInput(true); 
 //		//connection.setDoOutput(true);
 //		//connection.setRequestMethod("POST"); 
 //		//connection.setFollowRedirects(true); 
 //		
 //		_client.setUserCredentials(username, password);
 //	}
 //	
 //	private synchronized void timeOut() {
 //		notifyAll();
 //	}
 	
 //	public synchronized Socket serverReady(InetAddress ad, int port) throws IOException {  
 //        Socket listener = null;  
 //        while (true) {  
 //            try {  
 //                listener = new Socket(ad, port);  
 //            } 
 //            catch (ConnectException ignore) {}  
 //            if (listener == null) {  
 //                new Timer(true).schedule(new TimerTask() {  
 //                                    public void run() {  
 //                                        timeOut();  
 //                                    }  
 //                                },  
 //                                1000);  
 //                 try {  
 //                     wait();  
 //                 } 
 //                 catch (InterruptedException ignore) {}  
 //            } 
 //            else {  
 //                break;  
 //            }  
 //        }  
 //        return listener;  
 //    }  
 	
 //	private String get(String urlString) {
 //		URL url;
 //		StringBuffer resp = new StringBuffer();
 //		try {
 //			url = new URL(urlString);
 //			URLConnection conn = url.openConnection();
 //			conn.setDoOutput(true);
 //		    
 //		    // Get the response
 //		    BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
 //		    
 //		    String line;
 //		    while ((line = rd.readLine()) != null) 
 //		        resp = resp.append(line);
 //		    
 //		} catch (Exception e) {
 //
 //		}
 //		// TODO implement properly
 //		return resp.toString();
 //	}
 	
 	public void setAuth() {
 		//the info
 		CLIENT_ID = "1034117539945.apps.googleusercontent.com";
 		String redirect_uri = "urn:ietf:wg:oauth:2.0:oob";
 		String redirect_uri_local = "http://localhost:8080";
 		String scope = "http://www.google.com/calendar/feeds/";
 		CLIENT_SECRET = "ygIy2-y40S1Fer0B3oU_coVn"; 
 		String code = null;
 		String response_type = "code";
 		//encoded
 		String redirect_uri_local_en = null;
 		String scope_en = null;
 		String redirect_uri_en = null;
 		try {
 			redirect_uri_en = URLEncoder.encode(redirect_uri, "UTF-8");
 			redirect_uri_local_en = URLEncoder.encode(redirect_uri_local, "UTF-8");		
 			scope_en = URLEncoder.encode(scope, "UTF-8");
 		}
 		catch (UnsupportedEncodingException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
 		
 		//STEP ONE: getting the code
 		try {
 		    String query = "response_type="+response_type+"&"+"client_id="+CLIENT_ID+"&"+"redirect_uri="+redirect_uri_en+"&"+"scope="+scope_en;		    
 			String url = "https://accounts.google.com/o/oauth2/auth?"+query;
 		    URI uri = new URI(url);
 		    
 		    //display webpage
 		    java.awt.Desktop.getDesktop().browse(uri);
 		    //get code
 		    code = (String) new JOptionPane().showInputDialog("Paste code:");		    
 		    
 //		    //SOCKET
 //		    //Socket listener = new Socket(InetAddress.getLocalHost(), 80);
 //		    //InetSocketAddress sa = new InetSocketAddress(InetAddress.getLocalHost(), 80);
 //		    //listener.bind(sa);
 //		    //local
 		    //Socket listener = this.serverReady(InetAddress.getLocalHost(), 80); 
 //		    //uri based
 ////		    System.out.println(r_uri.getHost());
 ////	    	Socket listener = this.serverReady(InetAddress., 80); 
 	    	//System.out.println("connected");
 		    
 //		    //HTTPURLConnection
 //		    HttpURLConnection listener = (HttpURLConnection) uri.toURL().openConnection();
 //		    listener.setDoInput(true); 
 //			listener.setDoOutput(true);
 //			listener.setFollowRedirects(true);
 //		    while (true) {
 ////		    	listener = (HttpURLConnection) uri.toURL().openConnection();
 ////		    	listener.setDoInput(true); 
 ////				listener.setDoOutput(true);
 ////				listener.setFollowRedirects(true);
 //		    	int response = listener.getResponseCode();
 //				System.out.println("resp = "+response);
 //				System.out.println("url = "+listener.getURL());
 //				if (response == 302) {
 //					break;
 //				}
 //		    }
 			
 			//int response = listener.getResponseCode();
 			//System.out.println("resp = "+response);
 		    
 	    	//READ INPUT
 //		    BufferedReader rd = new BufferedReader(new InputStreamReader(listener.getInputStream()));		    
 //		    String line;
 //			while ((line = rd.readLine()) != null) {
 //				System.out.println(line);
 //			}
 //			rd.close();
 		    
 			//STEP TWO: GET ACCESS TOKEN
 
 		    //getting the access token
 		    GoogleAuthorizationCodeTokenRequest toke = new GoogleAuthorizationCodeTokenRequest(new NetHttpTransport(), new JacksonFactory(), CLIENT_ID, CLIENT_SECRET, code, redirect_uri);
 		    GoogleTokenResponse request = toke.execute();
 		    request.setExpiresInSeconds((long) 9000);
 		    _refreshToken = request.getRefreshToken();
 		    
 		    //set token for client
 			_client.setAuthSubToken(request.getAccessToken());
 		} 
 		catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		catch (URISyntaxException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
 	
 	public CalendarGroup<CalendarResponses> importCalendarGroup(org.joda.time.DateTime st, org.joda.time.DateTime et) throws IOException, ServiceException, com.google.gdata.util.ServiceException {
 		//calendar group
 		GoogleCalendars allCalendars = new GoogleCalendars(st, et, _owner);
 		//set URL to get calendars
 		URL feedUrl = new URL("https://www.google.com/calendar/feeds/default/");
 		//write query
 		CalendarQuery myQuery = new CalendarQuery(feedUrl);
 		myQuery.setMinimumStartTime(new com.google.gdata.data.DateTime(st.getMillis()));
 		myQuery.setMaximumStartTime(new com.google.gdata.data.DateTime(et.getMillis()));
 		//send request and receive feed
 		CalendarFeed resultFeed = _client.query(myQuery, CalendarFeed.class);
 		
 		//NOTE:
 		//calendarentry = calendar in a list of calendars
 		//calendar event entry = event in single calendar
 		
 		//go through feed results and make calendars
         for (int i = 0; i < resultFeed.getEntries().size(); i++) {
             CalendarEntry calendar = resultFeed.getEntries().get(i);            
             
             //NOTE:
             //for java.util.date month zero indexed, year is something + 1900
             
             //make new calendar
             CalendarResponses currCal = new CalendarResponses(st, et, calendar.getTitle().getPlainText());          
            
             //get events for calendar
             ArrayList<Response> calResponses = this.getEvents(st, et, calendar.getId());
             currCal.setResponses(calResponses);            
             
             //add calendar to group of calendars
             allCalendars.addCalendar(currCal);
             
             //TEST
              currCal.print();
           }
         return allCalendars;
 	}
 	
 	public ArrayList<Response> getEvents(org.joda.time.DateTime st, org.joda.time.DateTime et, String calID) throws IOException, ServiceException, com.google.gdata.util.ServiceException {
 		ArrayList<Response> responseList = new ArrayList<Response>();
 		//get URL
 		String sub1 = calID.substring(0,37);
 		String sub2 = calID.substring(55,calID.length());
 		calID = sub1+sub2;
 		URL feedUrl = new URL(calID+"/private/full");
 		
 		//make query
 		CalendarQuery myQuery = new CalendarQuery(feedUrl);
 		myQuery.setMinimumStartTime(new com.google.gdata.data.DateTime(st.getMillis()));
 		myQuery.setMaximumStartTime(new com.google.gdata.data.DateTime(et.getMillis()));
 		myQuery.setStringCustomParameter("orderby", "starttime");
 		myQuery.setStringCustomParameter("sortorder", "ascending");
 		myQuery.setStringCustomParameter("singleevents", "true");
 		myQuery.setMaxResults(MAX_RESPONSES);
 		//send request and get result feed
 		CalendarEventFeed resultFeed = null;
 		try {
 			resultFeed = _client.query(myQuery, CalendarEventFeed.class);
 			//go through feed and get events to make responses
 			for (int i = 0; i < resultFeed.getEntries().size(); i++) {
 				CalendarEventEntry event = resultFeed.getEntries().get(i);
 				List<When> times = event.getTimes();
 				long startTime = times.get(0).getStartTime().getValue();
 				long endTime = times.get(0).getEndTime().getValue();
 				if (endTime - startTime < 86400000) {
 					Response eventResponse = new Response(new org.joda.time.DateTime(startTime), new org.joda.time.DateTime(endTime), event.getTitle().getPlainText());
 					responseList.add(eventResponse);
 				}
 			}
 		} 
 		catch (ServiceException e) {
 			System.out.println("Calendar query failed: service exception");
 		}
 		
 		return responseList;
 	}
 	
     public static void main(String[] args) throws IOException, ServiceException, com.google.gdata.util.ServiceException {
     	GCalImporter myImporter = new GCalImporter();
     	org.joda.time.DateTime startTime = new org.joda.time.DateTime(2012, 4, 20, 8, 0);
 		org.joda.time.DateTime endTime = new org.joda.time.DateTime(2012, 4, 29, 23, 0);
     	myImporter.importMyGCal(startTime, endTime);
     	myImporter.refresh(startTime, endTime);
     	
     }
 	
 	public CalendarGroup<CalendarResponses> refresh(org.joda.time.DateTime st, org.joda.time.DateTime et) {
 		try {
 			TokenResponse toke = new GoogleRefreshTokenRequest(new NetHttpTransport(), new JacksonFactory(), _refreshToken,CLIENT_ID, CLIENT_SECRET).execute();
 			_client.setAuthSubToken(toke.getAccessToken());
 		} catch (IOException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
 		try {
 			return this.importCalendarGroup(st, et);
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (ServiceException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return null;
 	}
 
 	//@Override
 	public CalendarGroup<CalendarResponses> importCalendarGroup(String url)
 			throws MalformedURLException, IOException {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public CalendarGroup<CalendarResponses> importNewEvent(String url)
 			throws MalformedURLException, IOException {
 		// TODO Auto-generated method stub
 		return null;
 	}
 }
 
