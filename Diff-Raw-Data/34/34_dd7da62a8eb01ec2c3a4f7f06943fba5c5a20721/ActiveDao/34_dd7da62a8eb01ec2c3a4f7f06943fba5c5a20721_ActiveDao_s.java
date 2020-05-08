 package org.teamrubiconusa.teamrubicon.dao;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.List;
 
 import org.teamrubiconusa.teamrubicon.model.Active;
 import org.teamrubiconusa.teamrubicon.model.TeamRubiconDb;
 
 import android.content.Context;
 
 public class ActiveDao {
 
 	private static volatile ActiveDao instance = null;
 	private Context context;
 	private TeamRubiconDb db;
 	private List<Active> actives = new ArrayList<Active>();
 	private final static String TIME_SEPARATOR = ":";
 	
 	private ActiveDao() {
 	}
 
 	public static ActiveDao getInstance() {
 		if (instance == null) {
 			synchronized (ActiveDao.class) {
 				if (instance == null) {
 					instance = new ActiveDao();
 				}
 			}
 		}
 		return instance;
 	}
 
 	public void setContext(Context context) {
 		db = new TeamRubiconDb(context);
 		populateMap();
 	}
 	
 	private void populateMap() {
 		TeamRubiconDb.ActivesCursor activeCursor = db.getActives();
 		for (int rowNum = 0; rowNum < activeCursor.getCount(); rowNum++) {
 			activeCursor.moveToPosition(rowNum);
 			
			Active temp = new Active(WarehouseDao.getWarehouseById(activeCursor.getColWarehouseId()), 
									 ItemDao.getItemById(activeCursor.getColItemId()), 
 									activeCursor.getColAmount() ,stringToCalendar(activeCursor.getColTime()));
 			actives.add(temp);
 		}
 	}
 	
 	private static Calendar stringToCalendar(String time) {
 		String[] hourAndMinute = time.split(TIME_SEPARATOR);
 		Calendar calendar = Calendar.getInstance();
 		calendar.setTimeInMillis(0);
 		calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(hourAndMinute[0]));
 		calendar.set(Calendar.MINUTE, Integer.parseInt(hourAndMinute[1]));
 		return calendar;
 	}
 	
 	private List<Active> getAllActives() {
 		return actives;
 	}
 }
