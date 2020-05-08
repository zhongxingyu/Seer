 package thesmith.eventhorizon.service.impl;
 
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.net.URL;
 import java.util.Date;
 import java.util.List;
 
 import org.json.JSONArray;
 import org.json.JSONObject;
 import org.springframework.stereotype.Service;
 
 import thesmith.eventhorizon.model.Account;
 import thesmith.eventhorizon.model.Event;
 import thesmith.eventhorizon.service.EventService;
 
 import com.google.appengine.repackaged.com.google.common.collect.Lists;
 import com.google.appengine.repackaged.org.joda.time.format.DateTimeFormat;
 import com.google.appengine.repackaged.org.joda.time.format.DateTimeFormatter;
 
 @Service
 public class LastfmEventServiceImpl implements EventService {
   private static final String API_KEY = "b25b959554ed76058ac220b7b2e0a026";
   private static final String DOMAIN_URL = "http://last.fm";
   private static final String API_URL = "http://ws.audioscrobbler.com/2.0/?method=user.getrecenttracks&user=%s&api_key=%s&format=json&page=%s&limit=10";
   private static final DateTimeFormatter formater = DateTimeFormat.forPattern("dd MMM yyyy, HH:mm");
 
   public List<Event> events(Account account, int page) {
     if (!"lastfm".equals(account.getDomain()))
       throw new RuntimeException("You can only get events for the lastfm domain");
 
     try {
       List<Event> events = Lists.newArrayList();
       URL url = new URL(String.format(API_URL, account.getPersonId(), API_KEY, String.valueOf(page)));
       BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
       StringBuffer json = new StringBuffer();
       String line;
       while ((line = reader.readLine()) != null) {
         json.append(line);
       }
       reader.close();
 
      JSONObject doc = new JSONObject(json.toString());
      if (doc.has("recenttracks")) {
        JSONObject recentTracks = (new JSONObject(json.toString())).getJSONObject("recenttracks");
        if (recentTracks.has("track")) {
          JSONArray tracks = recentTracks.getJSONArray("track");
          for (int i = 0; i < tracks.length(); i++) {
            JSONObject track = tracks.getJSONObject(i);
            Event event = new Event();
            event.setTitle(track.getJSONObject("artist").getString("#text") + " - " + track.getString("name"));
            event.setTitleUrl(track.getString("url"));
            if (track.has("date")) {
              event.setCreated(formater.parseDateTime(track.getJSONObject("date").getString("#text")).toDate());
            } else {
              event.setCreated(new Date());
            }
            event.setDomainUrl(DOMAIN_URL);
            event.setUserUrl(DOMAIN_URL + "/user/" + account.getUserId());
            events.add(event);
           }
         }
       }
 
       return events;
     } catch (Exception e) {
       throw new RuntimeException(e);
     }
   }
 }
