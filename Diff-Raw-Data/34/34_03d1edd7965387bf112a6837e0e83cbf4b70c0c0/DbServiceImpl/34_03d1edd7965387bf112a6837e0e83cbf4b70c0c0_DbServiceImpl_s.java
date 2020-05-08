 package ch.cern.atlas.apvs.server;
 
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletException;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import ch.cern.atlas.apvs.client.event.ServerSettingsChangedEvent;
 import ch.cern.atlas.apvs.client.service.DbService;
 import ch.cern.atlas.apvs.client.settings.ServerSettings;
 import ch.cern.atlas.apvs.eventbus.shared.RemoteEventBus;
 
 /**
  * @author Mark Donszelmann
  */
 @SuppressWarnings("serial")
 public class DbServiceImpl extends ResponsePollService implements DbService {
 
 	// private static final int DEFAULT_DB_PORT = 1521;
 
 	private Logger log = LoggerFactory.getLogger(getClass().getName());
 
 	private String dbUrl;
 
 	protected static RemoteEventBus eventBus;
 	protected static DbHandler dbHandler;
 
 	public DbServiceImpl() {
 		if (eventBus != null)
 			return;
 		log.info("Creating DbService...");
 		eventBus = APVSServerFactory.getInstance().getEventBus();
 	}
 
 	@Override
 	public void init(ServletConfig config) throws ServletException {
 		super.init(config);
 
 		if (dbHandler != null)
 			return;
 
 		log.info("Starting DbService...");
 
 		ServerSettingsChangedEvent.subscribe(eventBus,
 				new ServerSettingsChangedEvent.Handler() {
 
 					@Override
 					public void onServerSettingsChanged(
 							ServerSettingsChangedEvent event) {
 						ServerSettings settings = event.getServerSettings();
 						if (settings != null) {
 							String url = settings
 									.get(ServerSettings.Entry.databaseUrl
 											.toString());
 							if ((url != null) && !url.equals(dbUrl)) {
 								dbUrl = url;
 
								dbHandler.connect("jdbc:log4jdbc:oracle:thin:"
										+ dbUrl);
 							}
 						}
 					}
 				});
 
 		dbHandler = new DbHandler(eventBus);
 	}
 	
 	@Override
 	public boolean isConnected() {
 		return dbHandler.isConnected();
 	}
 }
