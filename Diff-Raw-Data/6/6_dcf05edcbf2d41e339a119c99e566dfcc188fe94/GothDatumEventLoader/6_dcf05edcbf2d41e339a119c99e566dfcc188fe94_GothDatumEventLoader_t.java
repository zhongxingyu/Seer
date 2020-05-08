 package com.alvarosantisteban.pathos.loader;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import com.alvarosantisteban.pathos.Event;
 import com.alvarosantisteban.pathos.R;
 import com.alvarosantisteban.pathos.utils.WebUtils;
 
 import android.content.Context;
 import android.util.Log;
 
 /**
  * Receives the html from the Goth Datum website and parses the information to create a list of events.
  * 
  * @author Alvaro Santisteban 2013 - alvarosantisteban@gmail.com
  *
  */
 public class GothDatumEventLoader implements EventLoader {
 	
 	public final static String WEBSITE_URL = "http://www.goth-city-radio.com/dsb/dates.php";
 	public final static String WEBSITE_URL_HTML = "<a href=\"http://www.goth-city-radio.com/dsb/dates.php\">Goth Datum</a>";
 	public final static String WEBSITE_NAME = "Goth Datum";
 	
 	/**
 	 * Used for logging purposes
 	 */
 	private static final String TAG = "GothDatumEventLoader";
 
 
 	//Event's topic tags
 	//private String ART_TOPIC_TAG;
 	//private String POLITICAL_TOPIC_TAG;
 	private String GOING_OUT_TOPIC_TAG;
 			
 	// Event's type tags
 	private String CONCERT_TYPE_TAG;
 	private String PARTY_TYPE_TAG;
 	//private String EXHIBITION_TYPE_TAG;
 	private String TALK_TYPE_TAG;
 	//private String SCREENING_TYPE_TAG;
 	//private String OTHER_TYPE_TAG;
 	
 	/**
 	 * The Database Helper that helps dealing with the db easily
 	 */
 	//private DatabaseHelper databaseHelper = null;
 	
 	/**
 	 * Map with names of clubs and its corresponding website url
 	 */
 	public static final Map<String,String> clubs;
 	static {
         clubs = new HashMap<String, String>();
         clubs.put("Berlin - KitKatClub", "http://www.kitkatclub.org/Home/Club/Termine/Index.html");
         clubs.put("Berlin - K17", "http://www.k17-berlin.de/content.php");
         clubs.put("Berlin - Red Club","http://www.redclub-berlin.de/RC/");
         clubs.put("Berlin - Area 61", "https://www.facebook.com/pages/AREA-61/499812456748536");
         clubs.put("Berlin - Lovelite", "www.lovelite.de");
         clubs.put("Berlin - Duncker Club", "http://www.dunckerclub.de");
         clubs.put("Berlin - Sophienclub", "http://www.sophienclub.com");
         clubs.put("Berlin - SUBVERSIV e.V.", "http://subversiv.squat.net");
         clubs.put("Berlin - Naherholung Sternchen","http://www.naherholung-sternchen.de/programm");
         clubs.put("Berlin - What?! Club", "http://www.what-club.de");
         clubs.put("Berlin - Kulturfabrik (Factory)","http://www.kulturfabrik-moabit.de/kufa");
         clubs.put("Berlin - White Trash Fast Food", "http://www.whitetrashfastfood.com/events/");
         clubs.put("Berlin - theARTer", "http://www.thearter.de");
         clubs.put("Berlin - Periplaneta Kreativzentrum","http://www.periplaneta.com");
         clubs.put("Berlin - Urban Spree","http://urbanspree.com/blog/events");
         clubs.put("Berlin - Comet Club","https://www.facebook.com/pages/Comet-Club/257275101014325");
         clubs.put("Berlin - MS Berlin (Reederrei Bethke)","http://www.reederei-bethke.de/");
         clubs.put("Berlin - Bi Nuu (Ex-Kato)","https://www.facebook.com/BiNuuBerlin");
     }
 
 	@Override
 	public List<Event> load(Context context) {
 		String html = WebUtils.downloadHtml(WEBSITE_URL, context);
 		initializeTags(context);
 		if(html.equals("Exception")){
 			Log.w(TAG, "The html equals to Exception");
 			return null;
 		}
 		try{
 			return extractEventsFromGothDatum(html);
 		}catch(ArrayIndexOutOfBoundsException e){
 			Log.e(TAG, "ArrayIndexOutOfBoundsException" +e);
 			return null;
 		}
 	}
 
 	/**
 	 * Initialize the tags used to categorize the topic and the type of each event
 	 * 
 	 * @param context
 	 */
 	private void initializeTags(Context context) {
 		//ART_TOPIC_TAG = context.getResources().getString(R.string.art_topic_tag);
 		GOING_OUT_TOPIC_TAG = context.getResources().getString(R.string.goingout_topic_tag);
 		CONCERT_TYPE_TAG = context.getResources().getString(R.string.concert_type_tag);
 		PARTY_TYPE_TAG = context.getResources().getString(R.string.party_type_tag);
 		TALK_TYPE_TAG = context.getResources().getString(R.string.talk_type_tag);
 	}
 
 	/**
 	 * Creates a set of events from the html of the Goth Datum website. 
 	 * Each Event has name, day, time, a sometimes a description and a link.
 	 * 
 	 * @param theHtml the String containing the html from the Goth Datum website
 	 * @return a List of Event with the name, day, time, description and link
 	 */
 	private List<Event> extractEventsFromGothDatum(String theHtml) throws ArrayIndexOutOfBoundsException{
 		String myPattern = "<TD ALIGN=\"LEFT\" VALIGN=\"TOP\"><hr /><b><i>";
 		String[] result = theHtml.split(myPattern);
 		
 		// Use an ArrayList because the number of events is unknown
 		List<Event> events = new ArrayList<Event>(); 
 		for (int i=1; i<result.length; i++){
 			// Separate up to the first "</b>"
 			String[] dateAndRest = result[i].split("</b>", 2);
 			// Get the date
 			String[] dayAndDate = dateAndRest[0].split("  ", 2);
 			String[] eventsOfADay = dateAndRest[1].split("<TR>");
 			for (int j=0; j<eventsOfADay.length; j++){
 				Event event = new Event();
 				// Format the date and set it
 				event.setDay(dayAndDate[1].replace('.', '/').trim());
 				String[] nothingTimeAndRest = eventsOfADay[j].split("<b>",2);
 				String[] timeAndRest = nothingTimeAndRest[1].split(" ",2);	
 				// Set the time
 				event.setHour(timeAndRest[0].trim());
 				String[] placeAndRest = timeAndRest[1].split("<",2);
 				// Get the place for the description
 				String place = "The event will take place in " +placeAndRest[0].trim() +"<br>"; // In many cases includes the word "Berlin"	
 				//event.setLocation("<a href=\"https://maps.google.es/maps?q="+placeAndRest[0].trim().replace(' ', '+')+"\">"+placeAndRest[0].trim()+"</a>");
				String location = placeAndRest[0].trim();
				if(location.equals("Berlin - K17")){
					location = "Pettenkoferstrae 17a, 10247 Berlin";
				}
				event.setLocation(location);
 				String[] nothingNameAndRest = placeAndRest[1].split("<i>",2);
 				String[] nameAndRest = nothingNameAndRest[1].split("<",2);
 				// Set the name
 				event.setName(nameAndRest[0].trim());	
 				// For each <BR> there is a part of the description
 				String[] nothingAndDescription = nameAndRest[1].split("<BR>",2); // Si quito el 2, lo puedo tener dividido en parrafos e iterar con for
 				String[] description = nothingAndDescription[1].split("</TD>");
 				event.setDescription(place +description[0]);
 				// Add a extra link if possible
 				String link = addExtraLink(placeAndRest[0].trim());
 				if (!link.equals("no link")){
 					event.setLink(link);
 				}
 				// Set the origin
 				event.setEventsOrigin(WEBSITE_NAME);
 				// Set the origin's website
 				event.setOriginsWebsite(WEBSITE_URL);
 				// Set the thema tag
 				event.setThemaTag(GOING_OUT_TOPIC_TAG);
 				// Set the type tag
 				event.setTypeTag(extractTag(nameAndRest[0].trim(), link));
 				events.add(event);
 			}
 		}
 		return events;		
 	}
 
 	/**
 	 * Extracts the tag from the event's name. To do so, searches for several key words that indicate that the event is a concert.
 	 * Warning: The last keyword, "*", is very weak.
 	 * 
 	 * @param eventsName
 	 * @return CONCERTS_TAG if one of the key words is found or PARTIES_TAG otherwise. The only exception is if there is a link
 	 * to a bookstore
 	 */
 	private String extractTag(String eventsName, String link) {
 		if(link.equals("Berlin - Periplaneta Kreativzentrum")){
 			return TALK_TYPE_TAG;
 		}
 		if(eventsName.contains("live") || eventsName.contains("Festival") || eventsName.contains("+ Support") || (eventsName.contains("*") && !eventsName.contains("Party"))){
 			return CONCERT_TYPE_TAG;
 		}
 		return PARTY_TYPE_TAG;
 			
 	}
 
 	/**
 	 * Checks if there is a corresponding link for the clubName parameter
 	 * 
 	 * @param clubName the name of the club. It can include other words
 	 * @return the corresponding link, if applies. If not, returns "no link"
 	 */
 	private String addExtraLink(String clubName) {
 		//System.out.println("clubName="+clubName +".");
 		if (clubs.containsKey(clubName)){
 			return clubs.get(clubName);
 		}
 		return "no link";
 	}
 }
