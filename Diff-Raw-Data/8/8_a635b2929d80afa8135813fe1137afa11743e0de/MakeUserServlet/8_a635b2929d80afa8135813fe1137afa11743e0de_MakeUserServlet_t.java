 package com.VolunteerCoordinatorApp;
 
 import java.io.IOException;
 
 import java.net.URL;
 import java.util.Date;
 import java.util.List;
 import java.util.TimeZone;
 
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletException;
 import javax.servlet.http.*;
 import javax.jdo.PersistenceManager;
 import javax.jdo.Query;
 
 import com.google.appengine.api.datastore.Key;
 import com.google.appengine.api.datastore.KeyFactory;
 import com.google.gdata.client.calendar.*;
 import com.google.gdata.data.acl.*;
 import com.google.gdata.data.calendar.*;
 import com.google.gdata.data.extensions.When;
 import com.google.gdata.data.*;
 import com.google.gdata.util.AuthenticationException;
 import com.google.gdata.util.ServiceException;
 
 @SuppressWarnings("serial")
 public class MakeUserServlet extends HttpServlet {
 	public void doPost(HttpServletRequest req, HttpServletResponse resp)
     throws IOException {
 		doGet(req, resp); 
 	}
 	
 	public void doGet(HttpServletRequest req, HttpServletResponse resp)
 	        throws IOException {
 		String firstName = req.getParameter("firstName"); 
 		String lastName = req.getParameter("lastName");
 		String email = req.getParameter("email");  
 		String phone = req.getParameter("phone");  
 		String reminder = req.getParameter("reminder");
 		
 		String task = req.getQueryString().split("task=")[1];
 		
 		String name = firstName + " " + lastName;
 		
 		String timeZone = req.getParameter( "timezone" );
 	    
 	    //Translate the time zone code into something non-depreciated for Java
 	    if( timeZone.equalsIgnoreCase( "est" ) ) {
 	        timeZone = "America/New_York";
 	    }
 	    else if( timeZone.equalsIgnoreCase( "cst" ) ) {
             timeZone = "America/Chicago";
         }
 	    else if( timeZone.equalsIgnoreCase( "mst" ) ) {
             timeZone = "America/Denver";
         }
 	    else if( timeZone.equalsIgnoreCase( "pst" ) ) {
             timeZone = "America/Los_Angeles";
         }
 	    else {
 	        timeZone = "America/New_York";
 	    }
 		
 		//If the user fixed a typo in their name or something, we don't want to 
 		//create a new user.
 		if( userExists( name ) )
 		{
 		    resp.sendRedirect( "/volunteercoordinator?name=" + name + "&task=" + task );
 		}
 		//But if the name still doesn't match, we create a new user.
 		else
 		{
 		    //We make a new calendar for this user.
 		    CalendarEntry calendar = new CalendarEntry();
 		    calendar.setTitle(new PlainTextConstruct(name + "'s Jobs"));
 		    calendar.setSummary(new PlainTextConstruct("This calendar contains the jobs " + name + " has volunteered  to coordinate for."));
 		    calendar.setTimeZone(new TimeZoneProperty( timeZone ));
 		    calendar.setHidden(HiddenProperty.FALSE);
 
 		    CalendarService myService = new CalendarService("Volunteer-Coordinator-Calendar");
 		    try {
 		        myService.setUserCredentials("rockcreekvolunteercoordinator@gmail.com", "G0covenant");
 		    } catch (AuthenticationException e) {
 		        // TODO Auto-generated catch block
 		        e.printStackTrace();
 		    }
 
 		    // Insert the calendar
 		    URL postUrl = new URL("https://www.google.com/calendar/feeds/default/owncalendars/full");
 		    CalendarEntry newCalendar = null;
 		    try
 		    {
 		        newCalendar = myService.insert(postUrl, calendar);
 		    }
 		    catch ( ServiceException e2 )
 		    {
 		        // TODO Auto-generated catch block
 				System.err.println( "Failed at inserting new calendar." );
 		        e2.printStackTrace();
 		    }
 		    catch (IOException e) {
 		    	//Retry
 		    	try
 			    {
 			        newCalendar = myService.insert(postUrl, calendar);
 			    }
 			    catch ( ServiceException e2 )
 			    {
 			        // TODO Auto-generated catch block
 					System.err.println( "Failed at inserting new calendar." );
 			        e2.printStackTrace();
 			    }
 		    }
 		    // Get the calender's url
 		    String usrCalUrl = newCalendar.getId();
 		    int splitHere = usrCalUrl.lastIndexOf("/") + 1;
 		    usrCalUrl = usrCalUrl.substring(splitHere);
 
 		    //Find events on the calendar with this user's name attached
 		    URL feedUrl = new URL("https://www.google.com/calendar/feeds/default/private/full");
 		    CalendarQuery myQuery = new CalendarQuery(feedUrl);
 		    myQuery.setFullTextQuery(name);
 		    CalendarEventFeed resultFeed = null;
 		    try
 		    {
 		        resultFeed = myService.query(myQuery, CalendarEventFeed.class);
 		    }
 		    catch ( ServiceException e1 )
 		    {
 		        // TODO Auto-generated catch block
 		        e1.printStackTrace();
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
 
 		    //Add all of those events to the new calendar
 		    if (!results.isEmpty()) {
 		        for (CalendarEventEntry entry : results) {
 		            //Get the event's details
 		            TextConstruct title = entry.getTitle();
 		            String content = entry.getPlainTextContent();
 		            When time = entry.getTimes().get(0);
 		            DateTime start = time.getStartTime();
 		            DateTime end = time.getEndTime();
 
 	                TimeZone TZ =  TimeZone.getTimeZone( timeZone );
 	                Date startDate = new Date(start.getValue());
 	                Date endDate = new Date(end.getValue());
 	                //Determine timezone offset in minutes, depending on whether or not
 	                //Daylight Savings Time is in effect
 	                if (TZ.inDaylightTime(startDate)) { 
 	                    start.setTzShift(-240); 
 	                } else {
 	                   start.setTzShift(-300); 
 	                }
 	                if (TZ.inDaylightTime(endDate)) { 
 	                    end.setTzShift(-240);
 	                } else {
 	                    end.setTzShift(-300);
 	                }
 
 		            //Create a new entry and add it
 		            URL newUrl = new URL(
 		                    "http://www.google.com/calendar/feeds/" + usrCalUrl + "/private/full");
 		            CalendarEventEntry myEntry = new CalendarEventEntry();
 		            myEntry.setTitle(title);
 		            myEntry.setContent(new PlainTextConstruct(content));
 		            When eventTimes = new When();
 		            eventTimes.setStartTime(start);
 		            eventTimes.setEndTime(end);
 		            myEntry.addTime(eventTimes);
 
 		            // Send the request and receive the response:
 		            try
 		            {
 		                myService.insert(newUrl, myEntry);
 		            }
 		            catch ( ServiceException e )
 		            {
 		                // TODO Auto-generated catch block
 		                e.printStackTrace();
 		            }
 		            catch (IOException e) {
 		            	//Retry
 		            	try
 			            {
 			                myService.insert(newUrl, myEntry);
 			            }
 			            catch ( ServiceException e1 )
 			            {
 			                // TODO Auto-generated catch block
 			                e1.printStackTrace();
 			            }
 		            }
 		        }
 		    }
 		    // Access the Access Control List (ACL) for the calendar
 		    Link link = newCalendar.getLink(AclNamespace.LINK_REL_ACCESS_CONTROL_LIST,
 		            Link.Type.ATOM);
 		    URL aclUrl = new URL(link.getHref());
 		    AclFeed aclFeed = null;
 		    try
 		    {
 		        aclFeed = myService.getFeed(aclUrl, AclFeed.class);
 		    }
 		    catch ( ServiceException e1 )
 		    {
 		        // TODO Auto-generated catch block
 		        e1.printStackTrace();
 		    } catch (IOException e) {
 				// Retry
 				try {
 					aclFeed = myService.getFeed(aclUrl, AclFeed.class);
 				} catch (ServiceException e1) {
 					// TODO Auto-generated catch block
 					e1.printStackTrace();
 				}
 			}
 
 		    // Set the default to "read-only" for all users
 		    AclEntry aclEntry = aclFeed.createEntry();
 		    aclEntry.setScope(new AclScope(AclScope.Type.DEFAULT, null));
 		    aclEntry.setRole(CalendarAclRole.READ);
 		    // Add it to the ACL  
 		    try
 		    {
 		        myService.insert(aclUrl, aclEntry);
 		    }
 		    catch ( ServiceException e1 )
 		    {
 		        // TODO Auto-generated catch block
 		        e1.printStackTrace();
 		    }
 		    catch (IOException e) {
 		    	//Retry
 			    try
 			    {
 			        myService.insert(aclUrl, aclEntry);
 			    }
 			    catch ( ServiceException e1 )
 			    {
 			        // TODO Auto-generated catch block
 			        e1.printStackTrace();
 			    }
 		    }
 
 		    //Make a new volunteer and associate all the new information with it in
 		    //the datastore.
 		    PersistenceManager pm = PMF.get().getPersistenceManager();
 
 		    Volunteer v = new Volunteer(firstName, lastName, email, phone, reminder, usrCalUrl, timeZone);
 
 		    Key key = KeyFactory.createKey(Volunteer.class.getSimpleName(), (firstName + " " + lastName));
 		    v.setKey(key);
 
 		    try {
 		        pm.makePersistent(v);
 		    } catch (Exception e) {
 		    	e.printStackTrace();
 		    } finally {
 		        pm.close();
 		    } 
 
 			resp.sendRedirect("/volunteercoordinator" + "?name=" + name + "&task=" + task);
 		}
 		
 	}	
 	
 	private boolean userExists(String name) { 
         PersistenceManager pm = PMF.get().getPersistenceManager(); 
         Query query = pm.newQuery(Volunteer.class); 
         query.setFilter("name == nameParam"); 
         query.declareParameters("String nameParam"); 
         List<Volunteer> results = (List<Volunteer>)query.execute(name); 
         
         if(results.isEmpty()) {
             return false; 
         } else {
             return true; 
         }
     }
 	
 
 }
