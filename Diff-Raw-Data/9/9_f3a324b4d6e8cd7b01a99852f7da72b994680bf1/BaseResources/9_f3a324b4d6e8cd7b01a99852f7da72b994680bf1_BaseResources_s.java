 package org.gdgankara.app.resources;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.ws.rs.GET;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.MediaType;
 
 import org.gdgankara.app.AnnouncementWrapper;
 import org.gdgankara.app.model.Announcement;
 import org.gdgankara.app.model.Session;
 import org.gdgankara.app.model.SessionWrapper;
 import org.gdgankara.app.model.Speaker;
 import org.gdgankara.app.model.SpeakerWrapper;
 import org.gdgankara.app.model.Version;
 import org.gdgankara.app.utils.Util;
 
 import com.google.appengine.api.datastore.DatastoreService;
 import com.google.appengine.api.datastore.DatastoreServiceFactory;
 import com.google.appengine.api.datastore.Entity;
 import com.google.appengine.api.datastore.FetchOptions;
 import com.google.appengine.api.datastore.PreparedQuery;
 import com.google.appengine.api.datastore.Query;
 import com.google.appengine.api.datastore.Query.FilterOperator;
 import com.google.appengine.api.datastore.Query.FilterPredicate;
 
 @Path("/")
 public class BaseResources {
 
 	@GET
 	@Path("sessions/{lang}")
 	@Produces(MediaType.APPLICATION_JSON)
 	public SessionWrapper getSessions(@PathParam("lang") String lang) {
 		DatastoreService dataStore = DatastoreServiceFactory
 				.getDatastoreService();
 
 		Query query = new Query(Session.KIND).setFilter(new FilterPredicate(
 				Session.LANG, FilterOperator.EQUAL, lang));
 		PreparedQuery preparedQuery = dataStore.prepare(query);
 		List<Entity> eSessionList = preparedQuery.asList(FetchOptions.Builder
 				.withDefaults());
 		List<Session> sessionsList = new ArrayList<Session>();
 
 		query = new Query(Speaker.KIND);
 		preparedQuery = dataStore.prepare(query);
 		List<Entity> eSpeakerList = preparedQuery.asList(FetchOptions.Builder
 				.withDefaults());
 		List<Speaker> speakerList = new ArrayList<Speaker>();
 
 		for (Entity eSpeaker : eSpeakerList) {
 			Speaker speaker = Util.getSpeakerFromEntity(eSpeaker);
 			speakerList.add(speaker);
 		}
 
 		for (Entity entity : eSessionList) {
 			Session session = Util.getSessionFromEntity(entity);
 			List<Long> speakerIDList = session.getSpeakerIDList();
 			for (Speaker speaker : speakerList) {
 				if (speakerIDList.get(0) == speaker.getId()) {
 					session.setSpeaker1(speaker);
 				}else if (speakerIDList.get(1) == speaker.getId()) {
 					session.setSpeaker2(speaker);
 				}else if (speakerIDList.get(2) == speaker.getId()) {
 					session.setSpeaker3(speaker);
 				}
 			}
 			sessionsList.add(session);
 		}
 
 		Version version = Version.getVersion();
 		return new SessionWrapper(version, sessionsList);
 	}
 
 	@GET
 	@Path("speakers")
 	@Produces(MediaType.APPLICATION_JSON)
 	public SpeakerWrapper getSpeakers() {
 		DatastoreService dataStore = DatastoreServiceFactory
 				.getDatastoreService();
 		Query spekaerQuery = new Query(Speaker.KIND);
 		PreparedQuery preparedQuery = dataStore.prepare(spekaerQuery);
 
 		List<Entity> eSpeakerList = preparedQuery.asList(FetchOptions.Builder
 				.withDefaults());
 		List<Speaker> speakerList = new ArrayList<Speaker>();
 
 		for (Entity eSpeaker : eSpeakerList) {
 			Speaker speaker = Util.getSpeakerFromEntity(eSpeaker);
 			speakerList.add(speaker);
 		}
 
 		Version version = Version.getVersion();
 		return new SpeakerWrapper(version, speakerList);
 	}
 	
 	@GET
	@Path("announcements")
 	@Produces(MediaType.APPLICATION_JSON)
	public AnnouncementWrapper getAnnouncements(){
 		DatastoreService dataStore = DatastoreServiceFactory.getDatastoreService();
		Query query = new Query(Announcement.KIND);
 		PreparedQuery preparedQuery = dataStore.prepare(query);
 		List<Entity> eAnnouncementList = preparedQuery.asList(FetchOptions.Builder.withDefaults());
 		List<Announcement> announcementList = new ArrayList<Announcement>();
 		for (Entity eAnnouncement : eAnnouncementList) {
 			Announcement announcement = Util.getAnnouncementFromEntity(eAnnouncement);
 			announcementList.add(announcement);
 		}
 		
 		return new AnnouncementWrapper(Version.getVersion(), announcementList);
 	}
 	@GET
 	@Path("version/get")
 	@Produces(MediaType.APPLICATION_JSON)
 	public Version getVersion() {
 		return Version.getVersion();
 	}
 
 	@GET
 	@Path("version/set")
 	@Produces(MediaType.APPLICATION_JSON)
 	public Version setVersion() {
 		return Version.setVersion();
 	}
 
 	@GET
 	@Path("godkiller")
 	@Produces(MediaType.TEXT_PLAIN)
 	public String godKiller() {
 		DatastoreService dataStore = DatastoreServiceFactory
 				.getDatastoreService();
 		Query godKillerQuery = new Query();
 		PreparedQuery preparedGodKillerQuery = dataStore
 				.prepare(godKillerQuery);
 		for (Entity entity : preparedGodKillerQuery.asIterable()) {
 			dataStore.delete(entity.getKey());
 		}
 		return "hayırlısı be gülüm";
 	}
 
 	String[] hourArray = { "09:00", "09:30", "10:00", "10:30", "11:00",
 			"11:30", "12:00", "12:30", "13:00", "13:30", "14:00", "14:30",
 			"15:00", "15:30", "16:00", "16:30", "17:00", "17:30", "18:00",
 			"18:30", "19:00", "19:30", "20:00" };
 
 	// @GET
 	// @Path("samples")
 	// @Produces(MediaType.TEXT_PLAIN)
 	// public String samples() {
 	// DatastoreService datastore = DatastoreServiceFactory
 	// .getDatastoreService();
 	// Session session;
 	// Entity eSession;
 	//
 	// try {
 	// for (int i = 0; i < 4; i++) {
 	// session = new Session("tr", "14 Haziran 2013 Cuma",
 	// hourArray[i], hourArray[i + 1], "A", "Baslik_" + i
 	// + "_A_14", "Aciklama_" + i, "Konusmaci_" + i);
 	// eSession = new Entity(Session.KIND);
 	// eSession.setProperty(Session.LANG, session.getLang());
 	// eSession.setProperty(Session.DAY, session.getDay());
 	// eSession.setProperty(Session.START_HOUR, session.getStartHour());
 	// eSession.setProperty(Session.END_HOUR, session.getEndHour());
 	// eSession.setProperty(Session.HALL, session.getHall());
 	// eSession.setProperty(Session.SPEAKER, session.getSpeaker());
 	// eSession.setProperty(Session.TITLE, session.getTitle());
 	// eSession.setProperty(Session.DESCRIPTION,
 	// session.getDescription());
 	// datastore.put(eSession);
 	//
 	// session = new Session("en", "15 June 2013 Friday",
 	// hourArray[i], hourArray[i + 1], "A", "Title_" + i
 	// + "_A_14", "Description_" + i, "Speaker_" + i);
 	// eSession = new Entity(Session.KIND);
 	// eSession.setProperty(Session.LANG, session.getLang());
 	// eSession.setProperty(Session.DAY, session.getDay());
 	// eSession.setProperty(Session.START_HOUR, session.getStartHour());
 	// eSession.setProperty(Session.END_HOUR, session.getEndHour());
 	// eSession.setProperty(Session.HALL, session.getHall());
 	// eSession.setProperty(Session.SPEAKER, session.getSpeaker());
 	// eSession.setProperty(Session.TITLE, session.getTitle());
 	// eSession.setProperty(Session.DESCRIPTION,
 	// session.getDescription());
 	// datastore.put(eSession);
 	// }
 	//
 	// } catch (Exception e) {
 	// return e.toString();
 	// }
 	//
 	// return "well done";
 	// }
 }
