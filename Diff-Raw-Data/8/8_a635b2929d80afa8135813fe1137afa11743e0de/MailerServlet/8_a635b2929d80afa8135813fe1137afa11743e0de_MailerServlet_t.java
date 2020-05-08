 package com.VolunteerCoordinatorApp;
 
 // When run, this servlet emails a reminder to all users who have
 // volunteered for an event taking place tomorrow
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.net.URL;
 import javax.servlet.http.*;
 import java.text.SimpleDateFormat;
 import java.util.*;
 
 import javax.jdo.PersistenceManager;
 import javax.mail.*;
 import javax.mail.internet.*;
 import com.google.appengine.api.datastore.Key;
 import com.google.appengine.api.datastore.KeyFactory;
 import com.google.gdata.client.calendar.CalendarQuery;
 import com.google.gdata.client.calendar.CalendarService;
 import com.google.gdata.data.DateTime;
 import com.google.gdata.data.calendar.CalendarEventEntry;
 import com.google.gdata.data.calendar.CalendarEventFeed;
 import com.google.gdata.data.extensions.ExtendedProperty;
 import com.google.gdata.data.extensions.When;
 import com.google.gdata.util.AuthenticationException;
 import com.google.gdata.util.ServiceException;
 
 @SuppressWarnings("serial")
 public class MailerServlet extends HttpServlet {
 	public void doGet(HttpServletRequest req, HttpServletResponse resp)
 	throws IOException {	
 		CalendarService myService = new CalendarService("Volunteer-Coordinator-Calendar"); 
 		try {
 			myService.setUserCredentials("rockcreekvolunteercoordinator@gmail.com", "G0covenant");
 		} catch (AuthenticationException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
 
 		// Run once for each possible reminder setting
 		mail(req, resp, myService, 1);
 		mail(req, resp, myService, 2);
 		mail(req, resp, myService, 3);
 	}
 
 	//Send email reminders for volunteers with reminders set to n days in advance
 	public void mail(HttpServletRequest req, HttpServletResponse resp, CalendarService myService, int n) throws IOException {
 
 		URL feedUrl = new URL("https://www.google.com/calendar/feeds/default/private/full");
 		CalendarQuery myQuery = new CalendarQuery(feedUrl);
 
 		SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy");
 		Date today = new Date();
 		Date tomorrow = new Date();
 		DateTime startDT = new DateTime(today); 
 		DateTime endDT = new DateTime(tomorrow); 
 
 		// Shift start date forward one day
 		long startL = startDT.getValue();
 		startL += (86400000 * n);
 		startDT.setValue(startL);
 
 		// Shift end date to the next day (86400000 milliseconds = 1 day), then
 		// shift end date to midnight at end of day instead of beginning of day.
 		// This assumes the MailerServlet is being run at midnight
 		long endL = endDT.getValue();
 		endL += (86400000 * n) + 86399999;
 		endDT.setValue(endL);
 
 		myQuery.setMinimumStartTime(startDT); 
 		myQuery.setMaximumStartTime(endDT); 
		myQuery.setStringCustomParameter("singleevents", "true");
 
 		// Send the request and get the list of events between today and tommorrow
 		CalendarEventFeed resultFeed = null;
 		try {
 			resultFeed = myService.query(myQuery, CalendarEventFeed.class);
 		} catch (ServiceException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// Retry
 			try {
 				resultFeed = myService.query(myQuery, CalendarEventFeed.class);
 			} catch (ServiceException e1) {
 				// TODO Auto-generated catch block
 				e1.printStackTrace();
 			}
 		}
 		List<CalendarEventEntry> results = (List<CalendarEventEntry>)resultFeed.getEntries();
 
 		if (!results.isEmpty()) {
 			for (CalendarEventEntry entry : results) { // For each event...
 				// Get event details
 				
 				// Get the start and end times for the event 
 				When time = entry.getTimes().get(0); 
 				DateTime start = time.getStartTime(); 
 				//DateTime end = time.getEndTime();
 				Date startDate = new Date(start.getValue());
 
 				String title = entry.getTitle().getPlainText();
 
 		           // Access the description field of the calendar 
 		           // event, where the event description and a list 
 		           // of volunteers is stored.
 		           String description = entry.getPlainTextContent();
 		           String forWho = "";
 		           String who = "";
 		           String why = "";
 		           String category = "";
 		           
 		      		List<ExtendedProperty> propList = entry.getExtendedProperty();
 		      		String acceptedBy = "nobody";
 		      		for (ExtendedProperty prop : propList) {
 		      			/*if (prop.getName().equals("category")) 
 		      			{
 		      				category = prop.getValue();
 		      			}
 		      			if (prop.getName().equals("for")) 
 		      			{
 		      				forWho = prop.getValue();
 		      			}
 		      			if (prop.getName().equals("who")) 
 		      			{
 		      				who = prop.getValue();
 		      			}
 		      			if (prop.getName().equals("why")) 
 		      			{
 		      				why = prop.getValue();
 		      			}*/
 		      			if( prop.getName().equals( "acceptedBy" ) )
 		      			{
 		      			    acceptedBy = prop.getValue();
 		      			}
 		      		}
 
 				PersistenceManager pm = PMF.get().getPersistenceManager(); 
 				if (!acceptedBy.equals("nobody")) {
 						Properties props = new Properties();
 						Session session = Session.getDefaultInstance(props, null);
 
 						// Get volunteer
 						Key k = KeyFactory.createKey(Volunteer.class.getSimpleName(), acceptedBy);
 						Volunteer v = pm.getObjectById(Volunteer.class, k);
 
 						// Only send email if it's the right number of days, as set in their preferences
 						if (v.getReminder().equals(Integer.toString(n))) {
 
 							// Get volunteer's email address
 							String recip = v.getEmail();
 							
 							//Adjust starting time according to volunteer's timezone setting
 							String timeZone = v.getTimeZone();
 							if( timeZone == null )
 							{
 								timeZone = "America/New_York";
 							}
 							TimeZone TZ =  TimeZone.getTimeZone( timeZone );
 							
 							int offset = TZ.getOffset( startDate.getTime() ); //returns in milliseconds
 							start.setTzShift( offset / 1000 / 60 ); //takes argument in minutes
 							
 							// Get a date object, which can be formatted easier.
 							startDate = new Date( start.getValue() + 1000 * (start.getTzShift() * 60) );  
 
 							String hourPattern = "hh:mma"; 
 							SimpleDateFormat timeFormat = new SimpleDateFormat(hourPattern); 
 							String startDay = format.format(startDate); 
 							String startTime = timeFormat.format(startDate);
 							//String endTime = timeFormat.format(endDate);
 							System.out.println(title+" "+startTime+" "+startDay);
 
 							// Set email subject and body
 							String msgSubj = new String("Coordinator Reminder");
 							String msgBody = new String("Dear " + acceptedBy + ",\n" + 
 									"This is a friendly reminder that you volunteered to help " +
 									"out with coordinating \"" + title + "\" on " + startDay + " at " +
 									startTime + ". Don't forget!\n" +
 									"Thank you,\n" +
 							"Rock Creek Fellowship");
 
 							//must be "address of a registered developer for the application" (Google documentation)
 							String senderAddress = new String("serpentine.cougar@gmail.com");
 							String senderName = new String("Rock Creek Fellowship"); //optional
 
 							try {
 								InternetAddress[] recipEmail = InternetAddress.parse(recip);
 
 								Message msg = new MimeMessage(session);
 								msg.setFrom(new InternetAddress(senderAddress, senderName));
 								msg.addRecipients(Message.RecipientType.TO, recipEmail); //other possible types: cc, bcc
 								msg.setSubject(msgSubj);
 								msg.setText(msgBody);
 
 								//make program wait 8 seconds before emailing, to avoid overstepping quota limits
 								pause(8);
 								Transport.send(msg);
 
 							} catch (AddressException e) {
 								System.err.println("Address Exception");
 							} catch (MessagingException e) {
 								System.err.println("Messaging Exception");
 							} 
 						}
 					
 				}
 			}
 		}
 	}
 
 	public void pause(int x) { // pauses for x minutes
 		long ms2, ms1 = System.currentTimeMillis();
 		do {
 			ms2 = System.currentTimeMillis();
 		} while ((ms2 - ms1) < (x * 1000)); //convert seconds to miliseconds and check if the specified amount of time has passed 
 	}
 }
