 package no.ntnu.idi.freerider.backend;
 
 
 import java.sql.SQLException;
 import java.util.Calendar;
 import java.util.List;
 
 import no.ntnu.idi.freerider.model.Journey;
 
 /**
  * Deletes old routes each time it is triggered
  * @author Thomas
  *
  */
 public class MaintenanceService implements Runnable {
 	public void run() {
 		//Routes
 		ServerLogger.write("Run service routine");
 		DBConnector db = new DBConnector();
 		try {
 			db.init();
 			List<SimpleRoute> list = db.getAllSimpleRoutes();
 			for(int i = 0; i<list.size();i++) {
 				
 				Calendar cal = Calendar.getInstance();  
 				Calendar now = Calendar.getInstance();
 				cal.setTime(list.get(i).getDateModified());
 				cal.add(Calendar.DAY_OF_MONTH, 14);
 				ServerLogger.write("Now:" + now.getTime().toString());
 				ServerLogger.write("Compare:" + cal.getTime().toString());
 				
 				if(now.after(cal)){
 					try{
 						db.deleteRouteBySerial(list.get(i).getSerial());
 					} catch(Exception e) {// Change to more specific exception
 						ServerLogger.write(e.getMessage());
 					}	
 				}
 			}
 		} catch (Exception e) {//Change to more specific exception
 			ServerLogger.write("Error: " + e.getMessage());
 		}
 		
 		//Journeys
 		try {
 			List<Journey> journeys = db.getAllJourneys();
 			Calendar cal = Calendar.getInstance();  
 			cal.add(Calendar.DAY_OF_MONTH, -1);
 			for(int i = 0; i < journeys.size(); i++){
 				if(journeys.get(i).getStart().before(cal)){
 					db.sendRating(journeys.get(i));
 					
 					db.deleteJourneyWithoutCheck(journeys.get(i));
 				}
 			}
 		} catch (SQLException e) {
			ServerLogger.write("Error: " + e.getMessage());
 		}
 		
 	}
 }
