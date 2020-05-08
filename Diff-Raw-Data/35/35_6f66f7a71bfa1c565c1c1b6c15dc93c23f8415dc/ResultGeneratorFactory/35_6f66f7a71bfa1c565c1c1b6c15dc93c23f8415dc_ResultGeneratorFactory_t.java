 package data.database.resultGenerators;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 
 import database.connector.Field;
 import database.connector.ResultGenerator;
 import database.connector.Row;
 
 public class ResultGeneratorFactory {
 	private static ResultGeneratorFactory instance;
 	private VerifyLogInResultGenerator verifyLogInGenerator;
 	private BidsByUserResultGenerator bidsByUserGenerator;
 	private AllItemsResultGenerator allItemsResultGenerator;
 	
 	private ResultGeneratorFactory(){
 		
 	}
 	
 	public static ResultGeneratorFactory getInstance() {
		if(instance == null) {
			instance = new ResultGeneratorFactory();
		}
		return instance;
 	}
 	
 	public VerifyLogInResultGenerator getVerifyLogInGenerator() {
 		return verifyLogInGenerator == null ? new VerifyLogInResultGenerator() : verifyLogInGenerator;
 	}
 	
 	public BidsByUserResultGenerator getBidsByUserGenerator() {
 		return bidsByUserGenerator == null ? new BidsByUserResultGenerator() : bidsByUserGenerator;
 	}
 	
 	public AllItemsResultGenerator getAllItemsGenerator() {
 		return allItemsResultGenerator == null ? new AllItemsResultGenerator() : allItemsResultGenerator;
 	}
 	
 	private class VerifyLogInResultGenerator implements ResultGenerator {
 		@Override
 		public ArrayList<Row> getResultRows(ResultSet res) {
 			ArrayList<Row> rows = new ArrayList<>();
 			Row row = null;
 			Field userID = null, username = null, firstname = null, lastname = null, adress = null;
 			
 			try {
 				while(res.next()){
 					row = new Row();
 					userID = new Field("UserID", res.getObject("UserID"));
 					username = new Field("Username", res.getObject("Username"));
 					firstname = new Field("Firstname", res.getObject("Firstname"));
 					lastname = new Field("Lastname", res.getObject("Lastname"));
 					adress = new Field("Adress", res.getObject("Adress"));
 					row.addField(userID); row.addField(username); row.addField(firstname); row.addField(lastname); row.addField(adress);
 					rows.add(row);
 				}
 			} catch (SQLException e) {
 				System.out.println("An sql error occured");
 				e.printStackTrace();
 				return null;
 			}
 			
 			return rows;
 		}
 	}
 	
 	private class BidsByUserResultGenerator implements ResultGenerator {
 
 		@Override
 		public ArrayList<Row> getResultRows(ResultSet res) {
 			ArrayList<Row> rows = new ArrayList<>();
 			Row row = null;
 			Field itemno = null, value = null, name = null;
 			
 			try {
 				while(res.next()) {
 					row = new Row();
 					itemno = new Field("itemno", res.getObject("itemno"));
 					value = new Field("value", res.getObject("value"));
 					name = new Field("name", res.getObject("name"));
 					row.addField(itemno); row.addField(value); row.addField(name);
 					rows.add(row);
 				}
 			} catch (SQLException e) {
 				System.out.println("An sql error occured");
 				e.printStackTrace();
 				return null;
 			}
 			
 			return rows;
 		}
 		
 	}
 	
 	private class AllItemsResultGenerator implements ResultGenerator {
 
 		@Override
 		public ArrayList<Row> getResultRows(ResultSet res) {
 			ArrayList<Row> rows = new ArrayList<>();
 			Row row = null;
 			Field highestbidder = null, itemno = null, name = null, price = null,
 				  expiredate = null, description = null, bid = null, addedByID = null;
 			
 			try {
 				while(res.next()) {
 					row = new Row();
 					highestbidder = new Field("highestbidder", res.getObject("highestbidder"));
 					itemno = new Field("itemno", res.getObject("itemno"));
 					name = new Field("name", res.getObject("name"));
 					price = new Field("price", res.getObject("price"));
 					expiredate = new Field("expiredate", res.getObject("expiredate"));
 					description = new Field("description", res.getObject("description"));
 					bid = new Field("bid", res.getObject("bid"));
 					addedByID = new Field("addedByID", res.getObject("addedByID"));
 					row.addField(highestbidder); row.addField(itemno); row.addField(name); row.addField(price);
 					row.addField(expiredate); row.addField(description); row.addField(bid); row.addField(addedByID);
 					rows.add(row);
 				}
 			} catch (SQLException e) {
 				System.out.println("An sql error occured");
 				e.printStackTrace();
 				return null;
 			}
 			
 			return rows;
 		}		
 	}
 }
