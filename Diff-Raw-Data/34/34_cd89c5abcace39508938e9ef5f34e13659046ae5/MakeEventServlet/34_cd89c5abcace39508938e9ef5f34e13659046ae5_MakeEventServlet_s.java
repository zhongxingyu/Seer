 

 package com.VolunteerCoordinatorApp;
 
 import java.io.IOException;
 import javax.servlet.http.*;
 import com.google.gdata.client.calendar.*;
 import com.google.gdata.data.*;
 import com.google.gdata.data.calendar.*;
 import com.google.gdata.data.extensions.*;
 import com.google.gdata.util.AuthenticationException;
 import com.google.gdata.util.ServiceException;
 import javax.jdo.PersistenceManager;
 import com.VolunteerCoordinatorApp.PMF;
 import com.VolunteerCoordinatorApp.Volunteer;
 import com.google.appengine.api.datastore.Key;
 import com.google.appengine.api.datastore.KeyFactory;
 
 import java.net.URL;
 import java.util.TimeZone;
 
 @SuppressWarnings("serial")
 public class MakeEventServlet extends HttpServlet {
 	public void doPost(HttpServletRequest req, HttpServletResponse resp)
 	throws IOException {
 
 		String name = req.getParameter("name");
 
 		//If no user in query string, prompt to log in.
 		if (name == null || name.equalsIgnoreCase("null") || name.equals(""))
 		{
 			resp.sendRedirect("/loginredirect?url=/add.jsp");
 		} 
 		//Otherwise proceed normally.
 		else
 		{
 
 			CalendarService cService = new CalendarService("Volunteer-Coordinator-Calendar");
 			try {
 				cService.setUserCredentials("rockcreekvolunteercoordinator@gmail.com", "G0covenant");
 			} catch (AuthenticationException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 
 			URL postUrl =
 				new URL("https://www.google.com/calendar/feeds/default/private/full");
 			CalendarEventEntry entry = new CalendarEventEntry();
 			//CalendarQuery q = new CalendarQuery(new URL("hello")); 
 
			String title = req.getParameter( "title" );
 			entry.setTitle(new PlainTextConstruct(title));
 
 			String description = req.getParameter( "what" );
 			String forWho = req.getParameter( "for" );
 			String who = req.getParameter( "who" );
 			String why = req.getParameter( "why" );
 			String cat = req.getParameter( "cat" );
 			if( cat == null )
 			{
 				cat = "None";
 			}
 
 			entry.setContent(new PlainTextConstruct("<description> "
 					+ description + " </description> "
 					+ "<for> " + forWho + " </for> "
 					+ "<who> " + who + " </who> "
 					+ "<why> " + why + " </why> "));
 
 			// set category property
 			ExtendedProperty category = new ExtendedProperty();
 			category.setName("category");
 			category.setValue(cat);
 			entry.addExtendedProperty(category);
 
 			//	int day = Integer.parseInt(req.getParameter("day"));
 			//	int month = Integer.parseInt(req.getParameter("month"));
 			//	int year = Integer.parseInt(req.getParameter("year"));
 
 			// TODO validate input
 			int fromHrs = Integer.parseInt(req.getParameter("fromHrs"));
 			String fromMins = req.getParameter("fromMins");
 			int tillHrs = Integer.parseInt(req.getParameter("tillHrs"));
 			String tillMins = req.getParameter("tillMins");
 
 			if(req.getParameter("fromAMPM").equals("PM")) 
 			{ 
 				fromHrs += 12;
 			}
 			if(req.getParameter("toAMPM").equals("PM")) 
 			{ 
 				tillHrs += 12;
 			}
 			String fromHrsStr = "";
 			if( fromHrs / 10 > 0 )
 				fromHrsStr = String.valueOf( fromHrs );
 			else
 				fromHrsStr = "0" + String.valueOf( fromHrs );
 
 			String tillHrsStr = "";
 			if( tillHrs / 10 > 0 )
 				tillHrsStr = String.valueOf( tillHrs );
 			else
 				tillHrsStr = "0" + String.valueOf( tillHrs );
 
 			String date = req.getParameter("when");
			if (date == null || date.equals("")) { //Handle bug where user didn't input a date 
				resp.sendRedirect("/add.jsp?name=" + name + "&title=" + title + "&errordate=true&desc=" +
						description + "&for=" + forWho + "&who=" + who + "&why=" + why + "&cat=" +
						cat + "&fromHrs=" + fromHrs + "&fromMins=" + fromMins + "&tillHrs=" + tillHrs +
						"&tillMins=" + tillMins + "&fromAMPM=" + req.getParameter("fromAMPM") +
						"&toAMPM=" + req.getParameter("toAMPM") + "&when=" + date + "&recur=" + 
						req.getParameter("recur"));
			}

 			String month = date.substring(0, date.indexOf("/")); 
 			String day = date.substring(date.indexOf("/") + 1, date.indexOf("/") + 3); 
 			String year = date.substring(date.indexOf("/") + 4, date.length()); 
 			String formattedDate = year + "-" + month + "-" + day; 
			
			if (title == null || title.equals("")) { //Handle bug where user didn't input a title 
				resp.sendRedirect("/add.jsp?name=" + name + "&title=" + title + "&errortitle=true&desc=" +
						description + "&for=" + forWho + "&who=" + who + "&why=" + why + "&cat=" +
						cat + "&fromHrs=" + fromHrs + "&fromMins=" + fromMins + "&tillHrs=" + tillHrs +
						"&tillMins=" + tillMins + "&fromAMPM=" + req.getParameter("fromAMPM") +
						"&toAMPM=" + req.getParameter("toAMPM") + "&when=" + date + "&recur=" + 
						req.getParameter("recur"));
			}
 
 			PersistenceManager pManager = PMF.get().getPersistenceManager(); 
 
 			Key k = KeyFactory.createKey(Volunteer.class.getSimpleName(), name);
 			Volunteer vol = pManager.getObjectById(Volunteer.class, k);
 
 			String timeZone = vol.getTimeZone();
 			if( timeZone == null )
 			{
 				timeZone = "America/New_York";
 			}
 
 			TimeZone TZ =  TimeZone.getTimeZone( timeZone );
 
 			int offset = TZ.getOffset( DateTime.parseDate( formattedDate ).getValue() );
 
 			//Converts milisecond offset to positive hour offset.
 			offset = Math.abs( ( ( ( offset / 60 ) / 60 ) / 1000 ) );
 			String offsetString;
 			//It's a 2-digit offset; no leading zero needed
 			if( offset > 9 )
 			{
 				offsetString = "-" + offset + ":00";
 			}
 			//Single-digit offset; leading zero req'd.  Or we've royally screwed up input somehow,
 			//but how likely is *that*?
 			else
 			{
 				offsetString = "-0" + offset + ":00";
 			}
 
 			String fromTime = formattedDate + "T" + fromHrsStr
 			+ ":" + fromMins + ":00" + offsetString; //Should adjust to the user's TZ
 			String tillTime = formattedDate + "T" + tillHrsStr
 			+ ":" + tillMins + ":00" + offsetString; //Should adjust to the user's TZ			
 
 			DateTime startTime = DateTime.parseDateTime(fromTime);
 			DateTime endTime = DateTime.parseDateTime(tillTime);

			if (startTime.compareTo(endTime) > 0) { // handle bug where endtime is before the starttime
				resp.sendRedirect("/add.jsp?name=" + name + "&title=" + title + "&errortime=true&desc=" +
						description + "&for=" + forWho + "&who=" + who + "&why=" + why + "&cat=" +
						cat + "&fromHrs=" + fromHrs + "&fromMins=" + fromMins + "&tillHrs=" + tillHrs +
						"&tillMins=" + tillMins + "&fromAMPM=" + req.getParameter("fromAMPM") +
						"&toAMPM=" + req.getParameter("toAMPM") + "&when=" + date + "&recur=" + 
						req.getParameter("recur"));
			}
 			
 			When eventTimes = new When();
 			eventTimes.setStartTime(startTime);
 			eventTimes.setEndTime(endTime);
 
 			String recurStr = req.getParameter("recur");
 			ExtendedProperty recurrence = new ExtendedProperty();
 			recurrence.setName("recurrence");
 			recurrence.setValue(recurStr);
 			entry.addExtendedProperty(recurrence);
 
 			if (recurStr.equals("none")) { //If no recurrence, add the date/times
 				entry.addTime(eventTimes);
 			} else { //If recurrence selected, apply it
 				String recurData = "DTSTART;TZID=" + timeZone + ":" + year + month + day + "T" + fromHrsStr
 				+ fromMins + "00\r\n"
 				+ "DTEND;TZID=" + timeZone + ":" + year + month + day + "T" + tillHrsStr
 				+ tillMins + "00\r\n";
 				if (recurStr.equals("week")) {
 					recurData += "RRULE:FREQ=WEEKLY\r\n";
 				}
 				else if (recurStr.equals("biweek")) {
 					recurData += "RRULE:FREQ=WEEKLY;INTERVAL=2\r\n";
 				}
 				else if (recurStr.equals("month")) {
 					recurData += "RRULE:FREQ=MONTHLY\r\n";
 				}
 				Recurrence recur = new Recurrence();
 				recur.setValue(recurData);
 				entry.setRecurrence(recur);
 			}
 
 
 			try {
 				cService.insert(postUrl, entry);
 			} catch (ServiceException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 
 			resp.sendRedirect("/index.jsp?name=" + name);
 		}
 	}
 }
