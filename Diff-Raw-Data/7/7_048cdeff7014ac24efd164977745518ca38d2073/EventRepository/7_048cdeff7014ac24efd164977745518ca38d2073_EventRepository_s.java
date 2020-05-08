 package fi.dratini.keikkalista.core.dataaccess;
 
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import com.google.inject.Inject;
 import com.j256.ormlite.dao.GenericRawResults;
 import com.j256.ormlite.dao.RuntimeExceptionDao;
 import com.j256.ormlite.stmt.QueryBuilder;
 import com.j256.ormlite.support.ConnectionSource;
 import com.j256.ormlite.table.TableUtils;
 
 import fi.dratini.keikkalista.core.model.Event;
 import fi.dratini.keikkalista.core.model.EventArtist;
 import fi.dratini.keikkalista.core.model.Preference;
 
 public class EventRepository {
     private RuntimeExceptionDao<Event, Integer> eventDao;
     private RuntimeExceptionDao<EventArtist, Integer> eventArtistDao;
     
     @Inject
     public EventRepository(RuntimeExceptionDao<Event, Integer> eventDao, RuntimeExceptionDao<EventArtist, Integer> eventArtistDao) {
         this.eventDao = eventDao;
         this.eventArtistDao = eventArtistDao;
     }
     
     public List<Event> getEvents() {
         String sql = 
                 "select events.event_id, events.title, events.date, events.url, events.cancelled, events.city, events.headliner, event_artist.artist " +
                 "from events inner join event_artist on events.event_id = event_artist.event_id " +
                 "order by events.date, event_artist.artist";
         try {
             GenericRawResults<String[]> queryResult = eventDao.queryRaw(sql);
             return mapEvents(queryResult);
             
         } catch (Exception e) {
             throw new RuntimeException(e);
         }
     }
     
     public List<Event> findEvents(Preference preference) {
         String sql = 
                 "select events.event_id, events.title, events.date, events.url, events.cancelled, events.city, events.headliner, event_artist.artist " +
                 "from events inner join event_artist on events.event_id = event_artist.event_id " +
                 "where event_artist.artist in (%s)" +
                 "order by events.date, event_artist.artist";
         
         try {
             StringBuilder sb = new StringBuilder();
             for (int i = 0; i < preference.getArtists().size(); i++) {
                 sb.append("?");
                 if (i + 1 < preference.getArtists().size()) {
                     sb.append(", ");
                 }
             }
             
             String query = String.format(sql, sb.toString());
             GenericRawResults<String[]> queryResult = eventDao.queryRaw(query, escapeArtistNames(preference.getArtists()));
             return mapEvents(queryResult);
             
         } catch (Exception e) {
             throw new RuntimeException(e);
         }
     }
     
     private String[] escapeArtistNames(Collection<String> names) {
         Set<String> escapedNames = new HashSet<String>();
         for (String name : names) {
             escapedNames.add(name.replace("\'", "\'\'"));
         }
         return escapedNames.toArray(new String[0]);
     }
 
     public List<Event> mapEvents(GenericRawResults<String[]> queryResult) throws ParseException {
         final DateFormat h2df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
         List<Event> events = new ArrayList<Event>();
         
         Event previousRowEvent = null;
         for (String[] row : queryResult) {
             int id = Integer.parseInt(row[0]);
             
             Event event;
             if (previousRowEvent != null && previousRowEvent.getId() == id) {
                 event = previousRowEvent;
             } else {
                 String title = row[1];
                 Date date = h2df.parse(row[2]);
                 String url = row[3];
                 boolean cancelled = Integer.parseInt(row[4]) == 1;
                 String city = row[5];
                 String headliner = row[6];
                 
                 event = new Event();
                 event.setId(id);
                 event.setTitle(title);
                 event.setDate(date);
                 event.setUrl(url);
                 event.setCancelled(cancelled);
                 event.setCity(city);
                 event.setHeadliner(headliner);
                 
                 events.add(event);
             }
             
            String artist = row[6];
             event.AddArtist(artist);
             previousRowEvent = event;
         }
         return events;
     }
 
     public void SaveEvents(List<Event> events) {
         try {
             for (Event event : events) {
                 eventDao.create(event);
                 for (String artist : event.getArtists()) {
                     EventArtist eventArtist = new EventArtist();
                     eventArtist.setEvent(event);
                     eventArtist.setArtist(artist);
                     eventArtistDao.create(eventArtist);
                 }
             }
             
         } catch (Exception e) {
             throw new RuntimeException(e);
         }
     }
     
     public void DeleteEvents() {
         try {
             ConnectionSource cs = eventDao.getConnectionSource();
             TableUtils.clearTable(cs, Event.class);
             TableUtils.clearTable(cs, EventArtist.class);
             
         } catch (Exception e) {
             throw new RuntimeException(e);
         }    
     }
     
 }
