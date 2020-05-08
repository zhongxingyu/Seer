 package nl.giantit.minecraft.GiantShop.core.Tools.dbInit;
 
 import nl.giantit.minecraft.GiantShop.GiantShop;
 import nl.giantit.minecraft.GiantShop.core.Database.drivers.iDriver;
 import nl.giantit.minecraft.GiantShop.core.Tools.dbInit.Updates.*;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.logging.Level;
 
 public class dbInit {
 	
 	private iDriver dbDriver;
 	private double curS = 1.0, curI = 1.1, curD = 1.2;
 
 	private void init() {
 		if(!this.dbDriver.tableExists("#__versions")) {
 			HashMap<String, HashMap<String, String>> fields = new HashMap<String, HashMap<String, String>>();
 			HashMap<String, String> data = new HashMap<String, String>();
 			data.put("TYPE", "VARCHAR");
 			data.put("LENGTH", "100");
 			data.put("NULL", "false");
 			fields.put("tableName", data);
 			data = new HashMap<String, String>();
 			data.put("TYPE", "DOUBLE");
 			data.put("LENGTH", null);
 			data.put("NULL", "false");
 			data.put("DEFAULT", "1.0");
 			fields.put("version", data);
 			
 			this.dbDriver.create("#__versions").fields(fields).Finalize();
 			this.dbDriver.updateQuery();
 			
 			GiantShop.log.log(Level.INFO, "Revisions table successfully created!");
 		}
 		
 		if(!this.dbDriver.tableExists("#__log")){
 			ArrayList<String> field = new ArrayList<String>();
 			field.add("tablename");
 			field.add("version");
 			
 			HashMap<Integer, HashMap<String, String>> d = new HashMap<Integer, HashMap<String, String>>();
 			HashMap<String, String> data = new HashMap<String, String>();
 			data.put("data", "log");
 			d.put(0, data);
 			
 			data = new HashMap<String, String>();
 			data.put("data", "1.0");
 			d.put(1, data);
 			
 			this.dbDriver.insert("#__versions", field, d).Finalize();
 			this.dbDriver.updateQuery();
 			
 			HashMap<String, HashMap<String, String>> fields = new HashMap<String, HashMap<String, String>>();
 			data = new HashMap<String, String>();
 			data.put("TYPE", "INT");
 			data.put("LENGTH", "3");
			data.put("NULL", "false");
 			data.put("A_INCR", "true");
 			data.put("P_KEY", "true");
 			fields.put("id", data);
 			
 			data = new HashMap<String, String>();
 			data.put("TYPE", "INT");
 			data.put("LENGTH", "3");
 			data.put("NULL", "true");
 			fields.put("type", data);
 			
 			data = new HashMap<String, String>();
 			data.put("TYPE", "VARCHAR");
 			data.put("LENGTH", "100");
 			data.put("NULL", "true");
 			fields.put("user", data);
 			
 			data = new HashMap<String, String>();
 			data.put("TYPE", "VARCHAR");
 			data.put("LENGTH", "100");
 			data.put("NULL", "true");
 			fields.put("data", data);
 			
 			data = new HashMap<String, String>();
 			data.put("TYPE", "INT");
 			data.put("LENGTH", "50");
 			data.put("NULL", "false");
 			data.put("DEFAULT", "0");
 			fields.put("date", data);
 			
 			this.dbDriver.create("#__log").fields(fields).Finalize();
 			this.dbDriver.updateQuery();
 			
 			GiantShop.log.log(Level.INFO, "Logging table successfully created!");
 		}
 		
 		if(!this.dbDriver.tableExists("#__shops")) {
 			ArrayList<String> field = new ArrayList<String>();
 			field.add("tablename");
 			field.add("version");
 			
 			HashMap<Integer, HashMap<String, String>> d = new HashMap<Integer, HashMap<String, String>>();
 			HashMap<String, String> data = new HashMap<String, String>();
 			data.put("data", "shops");
 			d.put(0, data);
 			
 			data = new HashMap<String, String>();
 			data.put("data", "1.0");
 			d.put(1, data);
 			
 			this.dbDriver.insert("#__versions", field, d).Finalize();
 			this.dbDriver.updateQuery();
 			
 			HashMap<String, HashMap<String, String>> fields = new HashMap<String, HashMap<String, String>>();
 			data = new HashMap<String, String>();
 			data.put("TYPE", "INT");
 			data.put("LENGTH", "3");
 			data.put("NULL", "false");
 			data.put("A_INCR", "true");
 			data.put("P_KEY", "true");
 			fields.put("id", data);
 			
 			data = new HashMap<String, String>();
 			data.put("TYPE", "VARCHAR");
 			data.put("LENGTH", "100");
 			data.put("NULL", "false");
 			fields.put("name", data);
 			
 			data = new HashMap<String, String>();
 			data.put("TYPE", "VARCHAR");
 			data.put("LENGTH", "100");
 			data.put("NULL", "true");
 			fields.put("world", data);
 			
 			data = new HashMap<String, String>();
 			data.put("TYPE", "DOUBLE");
 			data.put("LENGTH", null);
 			data.put("NULL", "false");
 			fields.put("locMinX", data);
 			
 			data = new HashMap<String, String>();
 			data.put("TYPE", "DOUBLE");
 			data.put("LENGTH", null);
 			data.put("NULL", "false");
 			fields.put("locMinY", data);
 			
 			data = new HashMap<String, String>();
 			data.put("TYPE", "DOUBLE");
 			data.put("LENGTH", null);
 			data.put("NULL", "false");
 			fields.put("locMinZ", data);
 			
 			data = new HashMap<String, String>();
 			data.put("TYPE", "DOUBLE");
 			data.put("LENGTH", null);
 			data.put("NULL", "false");
 			fields.put("locMaxX", data);
 			
 			data = new HashMap<String, String>();
 			data.put("TYPE", "DOUBLE");
 			data.put("LENGTH", null);
 			data.put("NULL", "false");
 			fields.put("locMaxY", data);
 			
 			data = new HashMap<String, String>();
 			data.put("TYPE", "DOUBLE");
 			data.put("LENGTH", null);
 			data.put("NULL", "false");
 			fields.put("locMaxZ", data);
 			
 			this.dbDriver.create("#__shops").fields(fields).Finalize();
 			this.dbDriver.updateQuery();
 			
 			GiantShop.log.log(Level.INFO, "Shops table successfully created!");
 		}
 		
 		if(!this.dbDriver.tableExists("#__items")) {
 			ArrayList<String> field = new ArrayList<String>();
 			field.add("tablename");
 			field.add("version");
 			
 			HashMap<Integer, HashMap<String, String>> d = new HashMap<Integer, HashMap<String, String>>();
 			HashMap<String, String> data = new HashMap<String, String>();
 			data.put("data", "items");
 			d.put(0, data);
 			
 			data = new HashMap<String, String>();
 			data.put("data", "1.0");
 			d.put(1, data);
 			
 			this.dbDriver.insert("#__versions", field, d).Finalize();
 			this.dbDriver.updateQuery();
 			
 			HashMap<String, HashMap<String, String>> fields = new HashMap<String, HashMap<String, String>>();
 			data = new HashMap<String, String>();
 			data.put("TYPE", "INT");
 			data.put("LENGTH", "3");
			data.put("NULL", "false");
 			data.put("A_INCR", "true");
 			data.put("P_KEY", "true");
 			fields.put("id", data);
 			
 			data = new HashMap<String, String>();
 			data.put("TYPE", "INT");
 			data.put("LENGTH", "3");
 			data.put("NULL", "false");
 			fields.put("itemID", data);
 			
 			data = new HashMap<String, String>();
 			data.put("TYPE", "INT");
 			data.put("LENGTH", "3");
 			data.put("DEFAULT", "-1");
 			fields.put("type", data);
 			
 			data = new HashMap<String, String>();
 			data.put("TYPE", "DOUBLE");
 			data.put("LENGTH", null);
 			data.put("DEFAULT", "-1");
 			fields.put("sellFor", data);
 			
 			data = new HashMap<String, String>();
 			data.put("TYPE", "DOUBLE");
 			data.put("LENGTH", null);
 			data.put("DEFAULT", "-1");
 			fields.put("buyFor", data);
 			
 			data = new HashMap<String, String>();
			data.put("TYPE", "DOUBLE");
 			data.put("LENGTH", null);
 			data.put("DEFAULT", "-1");
 			fields.put("stock", data);
 			
 			data = new HashMap<String, String>();
 			data.put("TYPE", "INT");
 			data.put("LENGTH", "3");
 			data.put("DEFAULT", "1");
 			fields.put("perStack", data);
 			
 			data = new HashMap<String, String>();
 			data.put("TYPE", "VARCHAR");
 			data.put("LENGTH", "100");
 			data.put("NULL", "true");
 			fields.put("shops", data);
 			
 			this.dbDriver.create("#__items").fields(fields).Finalize();
 			this.dbDriver.updateQuery();
 			
 			GiantShop.log.log(Level.INFO, "Items table successfully created!");
 		}
 		
 		if(!this.dbDriver.tableExists("#__discounts")) {
 			ArrayList<String> field = new ArrayList<String>();
 			field.add("tablename");
 			field.add("version");
 			
 			HashMap<Integer, HashMap<String, String>> d = new HashMap<Integer, HashMap<String, String>>();
 			HashMap<String, String> data = new HashMap<String, String>();
 			data.put("data", "discounts");
 			d.put(0, data);
 			
 			data = new HashMap<String, String>();
 			data.put("data", "1.0");
 			d.put(1, data);
 			
 			this.dbDriver.insert("#__versions", field, d).Finalize();
 			this.dbDriver.updateQuery();
 			
 			HashMap<String, HashMap<String, String>> fields = new HashMap<String, HashMap<String, String>>();
 			data = new HashMap<String, String>();
 			data.put("TYPE", "INT");
 			data.put("LENGTH", "3");
 			data.put("NULL", "false");
 			data.put("A_INCR", "true");
 			data.put("P_KEY", "true");
 			fields.put("id", data);
 			
 			data = new HashMap<String, String>();
 			data.put("TYPE", "INT");
 			data.put("LENGTH", "3");
 			data.put("NULL", "false");
 			fields.put("itemID", data);
 			
 			data = new HashMap<String, String>();
 			data.put("TYPE", "INT");
 			data.put("LENGTH", "3");
 			data.put("DEFAULT", "10");
 			fields.put("discount", data);
 			
 			data = new HashMap<String, String>();
 			data.put("TYPE", "VARCHAR");
 			data.put("LENGTH", "100");
 			data.put("NULL", "true");
 			fields.put("user", data);
 			
 			data = new HashMap<String, String>();
 			data.put("TYPE", "VARCHAR");
 			data.put("LENGTH", "100");
 			data.put("NULL", "true");
 			fields.put("`group`", data);
 			
 			this.dbDriver.create("#__discounts").fields(fields).Finalize();
 			this.dbDriver.updateQuery();
 			
 			GiantShop.log.log(Level.INFO, "Discounts type table successfully created!");
 		}
 	}
 	
 	private void checkUpdate() {
 		ArrayList<HashMap<String, String>> resSet = this.dbDriver.select("tablename", "version").from("#__versions").execQuery();
 		
 		for(int i = 0; i < resSet.size(); i++) {
 			HashMap<String, String> res = resSet.get(i);
 			String table = res.get("tableName");
 			Double version = Double.parseDouble(res.get("version"));
 			
 			if(table.equalsIgnoreCase("shops") && version < curS) {
 				Shops.run(version);
 			}else if(table.equalsIgnoreCase("items") && version < curI) {
 				Items.run(version);
 			}else if(table.equalsIgnoreCase("discounts") && version < curD) {
 				Discounts.run(version);
 			}	
 		}
 	}
 	
 	public dbInit(GiantShop plugin) {
 		this.dbDriver = plugin.getDB().getEngine();
 
 		this.init();
 		this.checkUpdate();
 	}
 	
 }
