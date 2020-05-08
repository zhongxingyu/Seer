 package kea.kme.pullpit.server.podio;
 
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.logging.Logger;
 
 import kea.kme.pullpit.server.persistence.PodioObjectHandler;
 
 import com.podio.APITransportException;
 import com.podio.filter.StandardSortBy;
 import com.podio.item.ItemAPI;
 import com.podio.item.ItemsResponse;
 
 public class PodioImporter {
 	private static PodioImporter podioImporter;
 	private static final Logger log = Logger.getLogger(PodioImporter.class.getName());
 	private static final int ITEM_LIMIT = 100;
 	
 	private PodioImporter() {
 	}
 	
 	public static PodioImporter getInstance() {
 		if (podioImporter == null)
 			podioImporter = new PodioImporter();
 		return podioImporter;
 	}
 	
 	public int[] pullAll() throws SQLException {
 		try {
 		log.info("TRUNCATING");
 		PodioObjectHandler.getInstance().truncateAllPodioTables();
 		int[] pulls = new int[7];
 		log.info("Pulling venues");
 		pulls[0] = pullVenues(0, ITEM_LIMIT);
 		log.info("Pulling bands");
 		pulls[1] = pullBands(0, ITEM_LIMIT);
 		log.info("Pulling shows");
 		pulls[2] = pullShows(0, ITEM_LIMIT);
 		log.info("Pulling contacts");
 		pulls[3] = writeContacts(StaticImporter.getInstance().getContacts());
 		log.info("Pulling agents");
 		pulls[4] = writeAgents(StaticImporter.getInstance().getAgents());
 		log.info("Pulling bookers");
 		pulls[5] = writeBookers(StaticImporter.getInstance().getBookers());
 		log.info("Pulling showVenues");
 		pulls[6] = writeShowVenues(StaticImporter.getInstance().getShowVenues());
 		StaticImporter.getInstance().clearLists();
 		PodioObjectHandler.getInstance().deleteNullShows();
 		return pulls;
 		} catch (APITransportException e) {
 			log.info("Podio timeout");
 			return pullAll();
 		}
 	}
 	
 
 	public int pullShows(int counter, int limit) throws SQLException {
 		ItemsResponse ir = pullFromApp(App.SHOWS, counter, limit);
 		ArrayList<PodioShow> result = ArrayParser.getInstance().parseShows(ir);
 		PodioObjectHandler.getInstance().writeShows(result.toArray(new PodioShow[result.size()]));
 		if (result.size() == ITEM_LIMIT)
 			pullShows(++counter, ITEM_LIMIT);
 		int resultSize = counter*ITEM_LIMIT + result.size();
 		return resultSize;
 	}
 
 	public int pullBands(int counter, int limit) throws SQLException {
 		ItemsResponse ir = pullFromApp(App.BANDS, counter, limit);
 		ArrayList<PodioBand> result = ArrayParser.getInstance().parseBands(ir);
 		PodioObjectHandler.getInstance().writeBands(result.toArray(new PodioBand[result.size()]));
 		if (result.size() == ITEM_LIMIT)
			pullBands(++counter, ITEM_LIMIT);
 		int resultSize = counter*ITEM_LIMIT + result.size();
 		return resultSize;
 	}
 	
 	public int pullVenues(int counter, int limit) throws SQLException {
 		ItemsResponse ir = pullFromApp(App.VENUES, counter, limit);
 		ArrayList<PodioVenue> result = ArrayParser.getInstance().parseVenues(ir);
 		PodioObjectHandler.getInstance().writeVenues(result.toArray(new PodioVenue[result.size()]));
 		if (result.size() == ITEM_LIMIT)
			pullVenues(++counter, ITEM_LIMIT);
 		int resultSize = counter*ITEM_LIMIT + result.size();
 		return resultSize;
 	}
 
 	private ItemsResponse pullFromApp(App app, int counter, int limit) {
 		AuthFactory authFactory = AuthFactory.getInstance();
 		ItemAPI items = authFactory.getItemAPI(app);
 		ItemsResponse ir = items.getItems(app.appID, limit, counter*limit, StandardSortBy.LAST_EDIT_ON, true);
 		return ir;
 	}
 	
 	public void handleSingleItem(int itemID, App app, int operationType) {
 		if (operationType == 3) {
 			try {
 			PodioObjectHandler.getInstance().deleteRow(app.appName, itemID);
 			} catch (SQLException e) {
 				log.info("Error while deleting row, " + e.getMessage());
 			}
 		} else {
 		try {
 			switch (app) {
 			case BANDS:
 				pullBands(0,1);
 				PodioObjectHandler.getInstance().writeAgents(StaticImporter.getInstance().getAgents());
 				break;
 			case SHOWS:
 				pullShows(0,1);
 		PodioObjectHandler.getInstance().writeShowVenues(StaticImporter.getInstance().getShowVenues());
 				break;
 			case VENUES:
 				pullVenues(0,1);
 		PodioObjectHandler.getInstance().writeBookers(StaticImporter.getInstance().getBookers());
 				break;
 			}
 			StaticImporter.getInstance().clearLists();
 		} catch (SQLException e) {
 			log.warning(e.getMessage());
 		}
 		}
 	}
 	
 //	private ItemBadge pullSingleItem(ItemBadge ib, App app) {
 //		AuthFactory authFactory = AuthFactory.getInstance();
 //		ItemAPI items = authFactory.getItemAPI(app);
 //		Item i = items.getItem(ib.getId());
 //		ib.setFields(i.getFields());
 //		ib.setCurrentRevision(i.getCurrentRevision());
 //		return ib;
 //	}
 
 	private int writeContacts(PodioContact[] contacts) throws SQLException {
 		PodioObjectHandler.getInstance().writeContacts(contacts);
 		return contacts.length;
 	}
 	
 	private int writeShowVenues(PodioShowVenue[] showVenues) throws SQLException {
 		PodioObjectHandler.getInstance().writeShowVenues(showVenues);
 		return showVenues.length;
 	}
 	
 	private int writeBookers(PodioBooker[] bookers) throws SQLException {
 		PodioObjectHandler.getInstance().writeBookers(bookers);
 		return bookers.length;
 	}
 	
 	private int writeAgents(PodioAgent[] agents) throws SQLException {
 		PodioObjectHandler.getInstance().writeAgents(agents);
 		return agents.length;
 	}
 }
