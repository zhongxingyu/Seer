 package dk.frv.eavdam.utils;
 
 import com.bbn.openmap.gui.OpenMapFrame;
 import dk.frv.eavdam.data.AISBaseAndReceiverStationFATDMAChannel;
 import dk.frv.eavdam.data.AISDatalinkCheckIssue;
 import dk.frv.eavdam.data.AISDatalinkCheckResult;
 import dk.frv.eavdam.data.AISFixedStationData;
 import dk.frv.eavdam.data.AISSlotMap;
 import dk.frv.eavdam.data.ActiveStation;
 import dk.frv.eavdam.data.EAVDAMData;
 import dk.frv.eavdam.data.EAVDAMUser;
 import dk.frv.eavdam.data.FATDMAReservation;
 import dk.frv.eavdam.data.FTP;
 import dk.frv.eavdam.data.Options;
 import dk.frv.eavdam.data.Simulation;
 import dk.frv.eavdam.io.FTPHandler;
 import dk.frv.eavdam.io.XMLExporter;
 import dk.frv.eavdam.io.XMLImporter;
 import dk.frv.eavdam.io.derby.DerbyDBInterface;
 import dk.frv.eavdam.layers.StationLayer;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.util.Calendar;
 import java.util.ArrayList;
 import java.util.GregorianCalendar;
 import java.util.List;
 import javax.swing.JOptionPane;
 import org.apache.commons.net.ftp.FTPClient;
 
 public class DBHandler {
         
     private static EAVDAMData data = null;  // for testing before the database works        
     private static boolean initialized = false;    
     private static boolean updatedXML = false;
 	public static boolean changes = false;
     
 	public static boolean importDataUpdated = false;
     public static DerbyDBInterface derby = null;
 
     public static EAVDAMData getData() {    
 		return getData(null);
     }
 	
     public static EAVDAMData getData(OpenMapFrame openMapFrame) {        
 		if(derby == null) derby = new DerbyDBInterface();
 		
 //    	System.out.println("Getting data from database!");
 //    	if(data != null && !changes) return data;
     	
     	EAVDAMData dat = new EAVDAMData();
     	if(!initialized){
     		//Check if the database exists. It does the check in the constructor.
 
 			if (openMapFrame != null) {
 				new LoadXMLsFromFTPsThread(openMapFrame).start();
 			}
 	
 	
 //    		d.closeConnection();
     		
     		dat = XMLHandler.importData();  // needed because initially the data is read from local xmls if it is not in the database !!
     		initialized = true;
     	}else{
     	
 	    	try {
 			
 				if (importDataUpdated && StationLayer.windowReady) {
 				    dat = XMLHandler.importData();
 					importDataUpdated = false;
 					
 				} else {
 
 					EAVDAMUser user = derby.retrieveDefaultUser();  
 					if(user != null){
 //						System.out.println("Retrieved default user: "+user.getOrganizationName());
 					
 					}
 					
 					dat = derby.retrieveAllEAVDAMData(user);
 				
 				}
 	        	
 	            //Test prints
 //	            for(ActiveStation a : dat.getActiveStations()){
 //	            	
 //	            	for(AISFixedStationData f : a.getStations()){
 //	            		System.out.println("Station "+(f.getStatus().getStatusID() == DerbyDBInterface.STATUS_PLANNED ? "Planned" : "Operative"));
 //	            		if(f.getFATDMAChannelA() != null)
 //	            			System.out.println("A "+((AISBaseAndReceiverStationFATDMAChannel)f.getFATDMAChannelA()).getFATDMAScheme().get(0).getOwnership());
 //	            		if(f.getFATDMAChannelB() != null)
 //	            			System.out.println("B "+((AISBaseAndReceiverStationFATDMAChannel)f.getFATDMAChannelB()).getFATDMAScheme().size());
 //	            	}
 //	            }
 	                    
 	                
 	
 	    	} catch (Exception e) {
 	    		e.printStackTrace();
 	//    		System.out.println(e.getMessage());
 	    	}
 
     	}
             
 //    	try{
 //	    	HealthCheckHandler hch = new HealthCheckHandler(dat);
 //	    	
 //	    	double[] point = {60,24.15};
 //	    	AISSlotMap res = hch.slotMapAtPoint(point[0], point[1]);
 //	    	
 //	    	if(res != null){
 //	    		try{
 //	    			System.out.println("Stations found with coverage at (60;24.15)!");
 //	    			if(res.getAIS1Timeslots() != null && res.getAIS1Timeslots().size() > 0){
 //	    				System.out.println("\tReservation: "+res.getBandwidthReservation());
 //	    				System.out.println("\t"+res.getAIS1Timeslots().get(101).toString());
 //	    			}
 //	    				
 //	    			
 //	    		}catch(Exception e){
 //	    			e.printStackTrace();
 //	    		}
 //	    	}else{
 //	    		System.out.println("\tNo stations found with coverage at (60;24.15)");
 //	    	}
 //    	}catch(Exception e){
 //    		e.printStackTrace();
 //    	}
     		
     	data = dat;
     	changes = false;
     	
         return dat;
 
     }
 
     public static EAVDAMData getData(double topLeftLat, double topLeftLon, double lowRightLat, double lowRightLon){
     	EAVDAMData dat = null;
     	
     	try {
     		if(derby == null) derby = new DerbyDBInterface();
     		
             EAVDAMUser user = derby.retrieveDefaultUser();  
             if(user != null){
             	System.out.println("Retrieved default user: "+user.getOrganizationName());
             
             }
             
         	dat = derby.retrieveAllEAVDAMData(user, topLeftLat, topLeftLon, lowRightLat, lowRightLon);
         	
                   
                 
 
     	} catch (Exception e) {
     		e.printStackTrace();
     	}
 
     	
     	
     	return dat;
     }
     
     public static void saveData(EAVDAMData data) {
         DBHandler.data = data;   // for testing before the database works
 		if(derby == null) derby = new DerbyDBInterface();
         //d.createDatabase(null);        
         
 //		System.out.println("Storing data to database...");
         
 		derby.insertEAVDAMData(data);
 
 		changes = true;
     }
     
     public static void saveUserData(EAVDAMUser user, boolean defaultUser){
 		if(derby == null) derby = new DerbyDBInterface();
         //d.createDatabase(null);
     	try{
     		int id = derby.insertEAVDAMUser(user, defaultUser);
 //    		System.out.println("Added user under id "+id+" (default: "+defaultUser+")");
     		changes = true;
     	}catch(Exception e){
     		e.printStackTrace();
     	}
     }
     
     /**
      * Retrieves the options from the database..
      * 
      * @return
      */
     public static Options getOptions() {
     	Options op = null;
     	
     	try{
     		op = new Options();
     	
 			if(derby == null) derby = new DerbyDBInterface();
 		
     		op = derby.getOptions();
     	}catch(Exception e){
     		e.printStackTrace();
     	}
 		
 		return op;
 	}
     		
     public static void saveOptions(Options options){
 		if(derby == null) derby = new DerbyDBInterface();
     	derby.insertOptions(options);
     	changes = true;
     }
     
     public static void deleteSimulation(String simulationName){
     	try{
     		if(derby == null) derby = new DerbyDBInterface();
     		derby.deleteSimulation(simulationName);
     		changes = true;
     	}catch (Exception e) {
 			e.printStackTrace();
 		}
     }
     
     public static void deleteStation(int stationID){
     	try{
     		if(derby == null) derby = new DerbyDBInterface();
     		derby.deleteStation(stationID);
     		changes = true;
     	}catch (Exception e) {
 			e.printStackTrace();
 		}
     }
     
     public static void acknowledgeIssues(List<AISDatalinkCheckIssue> issues){
     	
     	try{
     		if(derby == null) derby = new DerbyDBInterface();
     		derby.acknowledgeIssues(issues);
     		changes = true;
     	}catch(Exception e){
     		e.printStackTrace();
     	}
     	
     }
     
     
     public static void deleteIssues(List<AISDatalinkCheckIssue> issues){
     	return;
     	
 //    	try{
 //    		DerbyDBInterface db = new DerbyDBInterface();
 //    		db.deleteIssues(issues);
 //    		changes = true;
 //    	}catch(Exception e){
 //    		e.printStackTrace();
 //    	}
     	
     }
 }
 
 
 // loads other users xmls from ftps
 class LoadXMLsFromFTPsThread extends Thread {
 		
 	OpenMapFrame openMapFrame;
 		
 	LoadXMLsFromFTPsThread(OpenMapFrame openMapFrame) {
 		this.openMapFrame = openMapFrame;
 	}
 	
 	public void run() {
 		
 		if (openMapFrame == null) {
 			return;
 		}
 		
 		boolean updated = false;
 		
 		EAVDAMData exportData = XMLHandler.exportData();		
 		
 		Options options = DBHandler.getOptions();
 		String ownFileName = XMLHandler.getLatestDataFileName();
 		if (ownFileName != null && ownFileName.indexOf("/") != -1) {
 			ownFileName = ownFileName.substring(ownFileName.lastIndexOf("/")+1);
 		}
 		List<FTP> ftps = options.getFTPs();
 		String errors = "";
 		if (ftps != null && !ftps.isEmpty()) {
 			for (FTP ftp : ftps) {
 				try {
 					FTPClient ftpClient = FTPHandler.connect(ftp);
					if (exportData.getActiveStations() == null || exportData.getActiveStations().isEmpty()) {
 						FTPHandler.deleteDataFromFTP(ftpClient, ownFileName);
 					} else {
 						FTPHandler.sendDataToFTP(ftpClient, XMLHandler.getLatestDataFileName());
 					}
 					if (FTPHandler.importDataFromFTP(ftpClient, XMLHandler.importDataFolder, ownFileName)) {
 						updated = true;
 					}					
 					FTPHandler.disconnect(ftpClient);
 				} catch (IOException ex) {
 					System.out.println(ex.getMessage());
 					ex.printStackTrace();
 					 errors += "- " + ex.getMessage() + "\n";
 				} catch (Exception ex) {
 					System.out.println(ex.getMessage());
 					ex.printStackTrace();
 					errors += "- " + ex.getMessage() + "\n";
 				}
 			}
 		}
 		if (!errors.isEmpty()) {
 			JOptionPane.showMessageDialog(openMapFrame, "The following ftp sites had problems when exchanging data:\n" + errors, "Error", JOptionPane.ERROR_MESSAGE); 
 		}	
 
 		DBHandler.importDataUpdated = updated;		
 		
 		DBHandler.changes = true;
 	}
 
 }
         
