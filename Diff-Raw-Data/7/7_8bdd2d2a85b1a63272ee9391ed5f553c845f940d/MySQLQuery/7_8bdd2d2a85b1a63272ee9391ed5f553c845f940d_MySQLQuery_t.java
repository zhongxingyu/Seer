 package AndroidWebService;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.CommunityService.Services.DBConnection;
 import org.hibernate.HibernateException;
 import org.CommunityService.EntitiesMapped.Volunteer;
 import org.CommunityService.EntitiesMapped.EventVolunteer;
 import org.CommunityService.EntitiesMapped.Event;
 import org.CommunityService.EntitiesMapped.Interest;
 import org.CommunityService.EntitiesMapped.EventInterests;
 
 public class MySQLQuery {
 
 	String hql = null;
 
 	public String getResultString(int kind, String id) {
 
 		String parse = "``"; // used to separate data when parsing (which is
 								// done in main android app)
 		String parse1 = "~~"; // used to separate objects
 
 		String result = "" + kind + parse + id + parse + parse1;
 		
 		try {
 
 			if (kind == MainServlet.kindVolQuery)
 				hql = "from Volunteer where VolunteerName = ? ";
 			else if (kind == MainServlet.kindEventVolQuery)
 				hql = "from EventVolunteer where VolunteerD = ? ";
 			else if (kind == MainServlet.kindEventQuery)
 				hql = "from Event where eventID = ? ";
 			else if (kind == MainServlet.kindFindQuery)
 				hql = "from Event";			
 			else if (kind == MainServlet.kindInterestQuery)
 				hql = "from Interest";			
 			else if (kind == MainServlet.kindEventInterestQuery)
 				hql = "from EventInterests where eventID = ? ";
 			else
 				result += "error bad kind";
 			
 			//parameter list
 			List params = null;
 			if(kind != MainServlet.kindFindQuery && kind != MainServlet.kindInterestQuery) {
 				
 				params = new ArrayList();
 				params.add(id);
 			}
 			
 			//query
 			List list = null;
 			try {
 				list = (List)DBConnection.query(hql, params);				
 			}
 			catch(HibernateException e) {	
 				result += "HibernateException " + e.toString();
 				return result;
 			}
 			
 			//return only certain data as one string
 			for(int i = 0; i < list.size(); i++ ) {
 
 				if (kind == MainServlet.kindVolQuery) {
 				
 					Volunteer v = (Volunteer)list.get(i);
 					
 					result += v.getVolunteerId() + parse;
 					result += v.getVolunteerName() + parse;
 					result += v.getVolunteerPassword() + parse;
 					result += v.getPhoneNumber() + parse;
 					result += v.getEmailAddress() + parse;
 				}
 				else if (kind == MainServlet.kindEventVolQuery) {
 					
 					EventVolunteer v = (EventVolunteer)list.get(i);
 					
 					result += v.getEvent().getEventId() + parse;
 					result += v.getVolunteer().getVolunteerId() + parse;
 				}
 				else if (kind == MainServlet.kindEventQuery || kind == MainServlet.kindFindQuery) {
 					
 					Event v = (Event)list.get(i);				
 					
 					result += v.getEventId() + parse;
 					result += v.getEventName() + parse;
 					result += v.getDescription() + parse;
 					result += v.getLocation() + parse;
 					result += v.getBeginTime() + parse;
 					result += v.getEndTime() + parse;
 				}
 				else if (kind == MainServlet.kindInterestQuery) {
 					
 					Interest v = (Interest)list.get(i);
 					
 					result += v.getInterestId() + parse;
 					result += v.getName() + parse;
 					result += v.getDescription() + parse;
 				}		
 				else if (kind == MainServlet.kindEventInterestQuery) {
 					
 					EventInterests v = (EventInterests)list.get(i);
 					
 					result += v.getEventInterestsId() + parse;
 					result += v.getEvent().getEventId() + parse;
 					result += v.getInterest().getInterestId() + parse;
 				}			
 				
 				result += parse1;
 			}
 			
 		} catch (Exception e) {
 
 			System.out.println(e);
 
 			result += "exception " + e.toString();
 			result += e.getMessage();
 			result += e.getLocalizedMessage();
 		}
 
 		return result;
 	}
 }
