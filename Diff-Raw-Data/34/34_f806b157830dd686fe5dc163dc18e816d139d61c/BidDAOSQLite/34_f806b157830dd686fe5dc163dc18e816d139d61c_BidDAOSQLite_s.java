 package ru.spbstu.students.dao;
 
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.util.List;
 
 import ru.spbstu.students.dao.querysupport.QuerySupport;
 import ru.spbstu.students.dto.BidInfo;
 import ru.spbstu.students.dto.ItemInfo;
 
public class BidDAOSQLite extends QuerySupport implements BidDAO {
 
 	private static final DateFormat df = DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.FULL);
	
	public String addBid(BidInfo bid) {		
		ItemInfo item = (new ItemDAOSQLite()).getItem(bid.getItemID());
 		double step = item.getStartBid() * 0.05; // 5% shall be the minimal step
 		
 		if(item.getType().equals("English")){
 			Query q = new Query("SELECT amount FROM bids ")
 				.append(where(eq("item", bid.getItemID())))
 				.append(" ORDER BY amount DESC LIMIT 1");
 			List<Double> lMax = q.list(new Fetcher<Double>(){
 				@Override
 				protected Double fetch(){
 					return getDouble("amount");
 				}
 			});
 			
 			if(!lMax.isEmpty()){						
 				double max = lMax.get(0);
 			
 				if(bid.getAmount() < max + step){
 					return "lessThanStep";
 				}
 			}
 		}
 		else if(item.getType().equals("Dutch")){
 			Query q = new Query("SELECT amount FROM bids ")
 				.append(where(eq("item", bid.getItemID())))
 				.append(" ORDER BY amount ASC LIMIT 1");
 			List<Double> lMin = q.list(new Fetcher<Double>(){
 				@Override
 				protected Double fetch(){
 					return getDouble("amount");
 				}
 			});
 		
 			if(!lMin.isEmpty()){						
 				double min = lMin.get(0);
 			
 				if(bid.getAmount() > min - step){
 					return "lessThanStep";
 				}
 			}			
 		}
 		else{
 			return "unkType";
 		}
 		
 		Query q = new Query("INSERT INTO bids(item, user, time, amount) VALUES(")
 			.append(bid.getItemID() + ",")
 			.append(bid.getUserID() + ",")
 			.append("'" + df.format(bid.getTime()) + "',")
 			.append(bid.getAmount() + ")");
 		q.execute();
 		
 		return "success";
 	}
 
 	public List<BidInfo> getBids(int itemID) {
 		Query q = new Query("SELECT * FROM bids ")
 			.append(where(eq("item", itemID)));
 		
 		return q.list(new Fetcher<BidInfo>(){
 			@Override
 			protected BidInfo fetch() { 				
 				try {
 					return new BidInfo(getInt("item"), getInt("user"), getDouble("amount"), df.parse(getString("time")));
 				} catch (ParseException e) {
 					return new BidInfo(getInt("item"), getInt("user"), getDouble("amount"), null);
 				}
 			}
 		});
 	}
 
 	public void removeBid(int bidID) {
 		Query q = new Query("DELETE FROM bids ").append(where(eq("id", bidID)));
 		q.execute();
 	}
 
 }
