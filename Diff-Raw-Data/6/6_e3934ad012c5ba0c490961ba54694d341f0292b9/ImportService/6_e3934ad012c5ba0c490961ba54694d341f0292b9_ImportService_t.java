 package de.tum.in.tumcampus.services;
 
 import java.io.PrintWriter;
 import java.io.StringWriter;
 
 import android.app.IntentService;
 import android.content.Intent;
 import android.util.Log;
 import de.tum.in.tumcampus.models.Feed;
 import de.tum.in.tumcampus.models.FeedManager;
 import de.tum.in.tumcampus.models.Link;
 import de.tum.in.tumcampus.models.LinkManager;
 import de.tum.in.tumcampus.models.TransportManager;
 import de.tum.in.tumcampus.models.Utils;
 
 public class ImportService extends IntentService {
 
 	public ImportService() {
 		super("ImportService");
 	}
 
 	final static String db = "database.db";
 
 	@Override
 	protected void onHandleIntent(Intent intent) {
 
 		// TODO add locking
 
 		Log.d("TumCampus ImportService", "TumCampus service start");
 
 		try {
 			// check if sd card available
 			Utils.getCacheDir("");
 
 			importTransports();
 
 			importFeeds();
 
 			importLinks();
 
 		} catch (Exception e) {
 			StringWriter sw = new StringWriter();
 			e.printStackTrace(new PrintWriter(sw));
 
			System.out.println(e);
			System.out.println(sw);
 			// TODO implement
 		}
 	}
 
 	public void importTransports() throws Exception {
 		TransportManager tm = new TransportManager(this, db);
 
 		if (tm.empty()) {
 			tm.replaceIntoDb("Garching-Forschungszentrum");
 			tm.replaceIntoDb("Marienplatz");
 		}
 		tm.close();
 	}
 
 	public void importFeeds() throws Exception {
 		FeedManager nm = new FeedManager(this, db);
 
 		if (nm.empty()) {
 			nm.insertUpdateIntoDb(new Feed("Spiegel",
 					"http://www.spiegel.de/schlagzeilen/index.rss"));
 
 			nm.insertUpdateIntoDb(new Feed("N-tv", "http://www.n-tv.de/rss"));
 
 			nm.insertUpdateIntoDb(new Feed("Zeit",
 					"http://newsfeed.zeit.de/index"));
 
 			nm.insertUpdateIntoDb(new Feed("Golem",
 					"http://rss.golem.de/rss.php?feed=RSS1.0"));
 
 			nm.insertUpdateIntoDb(new Feed("Heise",
 					"http://www.heise.de/newsticker/heise.rdf"));
 		}
 		nm.importFromInternal();
 		nm.close();
 	}
 
 	public void importLinks() throws Exception {
 		LinkManager lm = new LinkManager(this, db);
 
 		if (lm.empty()) {
 			lm.insertUpdateIntoDb(new Link("Spiegel", "http://m.spiegel.de/"));
 
 			lm.insertUpdateIntoDb(new Link("N-tv", "http://mobil.n-tv.de/"));
 
 			lm.insertUpdateIntoDb(new Link("Zeit", "http://mobil.zeit.de/"));
 
 			lm.insertUpdateIntoDb(new Link("Golem", "http://golem.mobi/"));
 
 			lm.insertUpdateIntoDb(new Link("Heise", "http://heise-online.mobi/"));
 
 			lm.insertUpdateIntoDb(new Link(
 					"Informatik Infopoint",
 					"http://www.in.tum.de/fuer-studierende-der-tum/service-fuer-studierende/infopoint.html"));
 
 			lm.insertUpdateIntoDb(new Link("Informatik Studienberatung",
 					"http://www.in.tum.de/fuer-studierende-der-tum/beratung.html"));
 
 			lm.insertUpdateIntoDb(new Link("Stellenangebote",
 					"http://portal.mytum.de/jobs/index_html"));
 
 			lm.insertUpdateIntoDb(new Link("Fachschaft MPI",
 					"http://mpi.fs.tum.de/"));
 
 			lm.insertUpdateIntoDb(new Link("OPAC TU Mnchen",
 					"http://opac.ub.tum.de/InfoGuideClient.tumsis/start.do?Login=wotum01"));
 
 			lm.insertUpdateIntoDb(new Link(
 					"OPAC LMU",
 					"https://opacplus.ub.uni-muenchen.de/InfoGuideClient.ubmsis/start.do?Login=igubm"));
 
 			lm.insertUpdateIntoDb(new Link("Informatik Fakultt",
 					"http://www.in.tum.de/"));
 		}
 		lm.importFromInternal();
		lm.close();
 	}
 
 	@Override
 	public void onDestroy() {
 		super.onDestroy();
 
 		Log.d("TumCampus ImportService", "TumCampus service destroy");
 	}
 
 	@Override
 	public void onCreate() {
 		super.onCreate();
 
 		Log.d("TumCampus ImportService", "TumCampus service create");
 	}
 }
