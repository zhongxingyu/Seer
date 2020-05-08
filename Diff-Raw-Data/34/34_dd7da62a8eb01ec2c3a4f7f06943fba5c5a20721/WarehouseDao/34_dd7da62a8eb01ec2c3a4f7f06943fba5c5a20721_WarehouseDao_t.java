 package org.teamrubiconusa.teamrubicon.dao;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.teamrubiconusa.teamrubicon.model.TeamRubiconDb;
 import org.teamrubiconusa.teamrubicon.model.Warehouse;
 
 import android.content.Context;
 
 public class WarehouseDao {
 
 	private static volatile WarehouseDao instance = null;
 	private Context context;
 	private TeamRubiconDb db;
	private Map<Integer, Warehouse> warehouses = new HashMap<Integer, Warehouse>();
 	
 	private WarehouseDao() {
 	}
 
 	public static WarehouseDao getInstance() {
 		if (instance == null) {
 			synchronized (WarehouseDao.class) {
 				if (instance == null) {
 					instance = new WarehouseDao();
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
 		TeamRubiconDb.WarehousesCursor warehouseCursor = db.getWarehouses();
 		for (int rowNum = 0; rowNum < warehouseCursor.getCount(); rowNum++) {
 			warehouseCursor.moveToPosition(rowNum);
 			Warehouse temp = new Warehouse(warehouseCursor.getColId(), warehouseCursor.getColName(), warehouseCursor.getColLocation());
 			warehouses.put(warehouseCursor.getColId(), temp);
 		}
 	}
 	
	public Warehouse getWarehouseById(int id) {
 		return warehouses.get(Integer.toString(id));
 	}
 }
