 package coupling.app.BL;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import coupling.app.Utils;
 
 
 public class BLFactory {
 
 	private static BLFactory blFactory;
 	
 	private Map<Long, BLShopList> shopLists;
 	private Map<Long, BLCalendarEvents> calendarEvents;
 	private BLShopListOverview blShopListOverview;
 	private BLGroceryList blGroceryList;
 	private BLCalendarEvents blCalendarEvents;
 	
 	private BLFactory(){
 		shopLists = new HashMap<Long, BLShopList>();
 		calendarEvents = new HashMap<Long, BLCalendarEvents>();
 	}
 	
 	public static BLFactory getInstance(){
 		if(blFactory == null)
 			blFactory = new BLFactory();
 		return blFactory;
 	}
 	
 	public BLShopList getShopList(Long listId){
 		BLShopList blShopList = shopLists.get(listId);
 		if(blShopList == null && listId != null){
 			Utils.Log("BLFACTORY", "getShopList", "Creating new BLShopList listID: " + listId);
 			blShopList = new BLShopList(listId);
 			shopLists.put(listId, blShopList);
 		}
 		Utils.Log("BLFactory", "getShopList", "ListId: " +listId+ " BLShopList Id: " + blShopList);
 		return blShopList;
 	}
 	
 	public BLCalendarEvents getCalendarEvents(Long eventId){
 		BLCalendarEvents blCalendarEvents = calendarEvents.get(eventId);
 		if (blCalendarEvents == null && eventId != null){
 			//Utils.Log("BLFACTORY", "blCalendarEvents", "Creating new calendarEvents listID: " + date);
 			blCalendarEvents = new BLCalendarEvents();
 			calendarEvents.put(eventId, blCalendarEvents);
 		}
 		return blCalendarEvents;
 	}
 	
 	public BLShopListOverview getShopListOverview(){
 		if(blShopListOverview == null)
 			blShopListOverview = new BLShopListOverview();
 		return blShopListOverview;
 	}
 	
 	public BLGroceryList getGroceryList(){
 		if (blGroceryList == null)
 			blGroceryList = new BLGroceryList();
 		return blGroceryList;
 	}
	
	public BLCalendarEvents getCalendarEvents(){
		if (blCalendarEvents == null)
			blCalendarEvents = new BLCalendarEvents();
		return blCalendarEvents;
	}
 
 }
