 package com.appspot.iclifeplanning;
 
 import java.io.IOException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import com.appspot.analyser.Analyzer;
 import com.appspot.analyser.DeleteSuggestion;
 import com.appspot.analyser.IEvent;
 import com.appspot.analyser.Suggestion;
 import com.appspot.datastore.SphereName;
 import com.appspot.datastore.TokenStore;
 import com.appspot.iclifeplanning.authentication.CalendarUtils;
 import com.appspot.iclifeplanning.events.Event;
 import com.appspot.iclifeplanning.events.EventStore;
 import com.google.appengine.repackaged.org.json.JSONArray;
 import com.google.appengine.repackaged.org.json.JSONException;
 import com.google.appengine.repackaged.org.json.JSONObject;
 
 /**
  * Suggestion servlet. responsible for managing the "optimise button".
  * Initialises the EventStore and runs the analyser to create suggestions.
  * 
  * @author Agnieszka Magda Madurska (amm208@doc.ic.ac.uk)
  * 
  */
 @SuppressWarnings("serial")
 public class SuggestionServlet extends HttpServlet {
 	// This should NOT be stored like this. Reimplement to use memcache at some point.
	private static Map<String, List<List<Suggestion>>> suggestionMap = new HashMap<String, List<List<Suggestion>>>();
 	
 	public void doGet(HttpServletRequest request, HttpServletResponse response)
 			throws IOException {
 
 		System.out.println("Map size: " + suggestionMap.size());
 		String token = TokenStore.getToken(CalendarUtils.getCurrentUserId());
 		CalendarUtils.client.setAuthSubToken(token);
 
 		EventStore eventStore = EventStore.getInstance();
 		eventStore.initizalize();
 		List<Event> events = eventStore.getEvents();
 		// ------------------- Dummy data
 		Analyzer analyser = new Analyzer();
		List<List<Suggestion>> suggestions = analyser.getSuggestions(events, CalendarUtils.getCurrentUserId(), true);
 		
 		suggestionMap.put(CalendarUtils.getCurrentUserId(), suggestions);
 
 		/*
 		IEvent event1 = (IEvent)events.get(0);
 		IEvent event2 = (IEvent)events.get(1);
 		//IEvent event3 = (IEvent)events.get(2);
 
 		Suggestion sug = new DeleteSuggestion(event1);
 		suggestions.add(sug);
 		sug = new DeleteSuggestion(event2);
 		suggestions.add(sug);
 		//sug = new DeleteSuggestion(event3);
 		suggestions.add(sug);*/
 		// ------------------- Dummy data
 		JSONArray suggestionArray = new JSONArray();
 		Suggestion s;
 		for (int i = 0; i < suggestions.size(); i++) {
			// TEMPORARY
			s = suggestions.get(0).get(i);
 			suggestionArray.put(suggestionToJSONOBject(s, i));
 		}
 		
 		response.getWriter().print(suggestionArray);
 	}
 
 	private JSONObject suggestionToJSONOBject(Suggestion s, int i) {
 		JSONObject suggestionObject = new JSONObject();
 		try {
 			suggestionObject.put("title", s.getTitle());
 			suggestionObject.put("description", s.getDescription());
 			suggestionObject.put("repeating", "");
 			
 			SimpleDateFormat date = new SimpleDateFormat("EEE, d MMM yyyy HH:mm");
 			suggestionObject.put("startDateTime", date.format(s.getStartDate().getTime()));
 
 			suggestionObject.put("endDateTime", date.format(s.getEndDate().getTime()));
 			suggestionObject.put("type", s.getType());
 			suggestionObject.put("id", i);
 			suggestionObject.put("userID", CalendarUtils.getCurrentUserId());
 
 			List<String> spheres = new ArrayList<String>();
 			for (SphereName sphere : s.getSpheres().keySet()) {
 				if (s.getSpheres().get(sphere) > 0) {
 					spheres.add(sphere.name());
 				}
 			}
 			suggestionObject.put("spheres", spheres);
 		} catch (JSONException e) {
 			e.printStackTrace();
 		}
 		return suggestionObject;
 	}
 
 	public void doPost(HttpServletRequest request, HttpServletResponse response)
 		throws IOException {
 		System.out.println(suggestionMap.size());
 	}
 }
