 package dk.frv.eavdam.io.derby;
 
 
 import java.net.URL;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.Set;
 
 import dk.frv.eavdam.data.AISAtonStationFATDMAChannel;
 import dk.frv.eavdam.data.AISBaseAndReceiverStationFATDMAChannel;
 import dk.frv.eavdam.data.AISFixedStationCoverage;
 import dk.frv.eavdam.data.AISFixedStationData;
 import dk.frv.eavdam.data.AISFixedStationStatus;
 import dk.frv.eavdam.data.AISFixedStationType;
 import dk.frv.eavdam.data.ActiveStation;
 import dk.frv.eavdam.data.Address;
 import dk.frv.eavdam.data.Antenna;
 import dk.frv.eavdam.data.AntennaType;
 import dk.frv.eavdam.data.AtonMessageBroadcastRate;
 import dk.frv.eavdam.data.EAVDAMData;
 import dk.frv.eavdam.data.EAVDAMUser;
 import dk.frv.eavdam.data.FATDMAChannel;
 import dk.frv.eavdam.data.FATDMAReservation;
 import dk.frv.eavdam.data.FATDMASlotAllocation;
 import dk.frv.eavdam.data.FTP;
 import dk.frv.eavdam.data.Options;
 import dk.frv.eavdam.data.OtherUserStations;
 import dk.frv.eavdam.data.Person;
 import dk.frv.eavdam.data.Simulation;
 
 
 	/**
 	 */
 	public class DerbyDBInterface {
 	    /* the default framework is embedded*/
 	    private String framework = "embedded";
 	    private String driver = "org.apache.derby.jdbc.EmbeddedDriver";
 	    private String protocol = "jdbc:derby:";
 	    private String defaultDB = "eavdamDB";
 	    
 	    public static final int STATUS_ACTIVE = 1;
 	    public static final int STATUS_OLD = 2;
 	    public static final int STATUS_SIMULATED = 3;
 	    public static final int STATUS_PROPOSED = 4;
 	    public static final int STATUS_PLANNED = 5;
 	    
 	    public static final int ANTENNA_DIRECTIONAL = 1;
 	    public static final int ANTENNA_OMNIDIR = 2;
 	    
 	    public static final int STATION_BASE = 1;
 	    public static final int STATION_REPEATER = 2;
 	    public static final int STATION_RECEIVER = 3;
 	    public static final int STATION_ATON = 4;
 	    
 	    public static final int COVERAGE_TRANSMIT= 1;
 	    public static final int COVERAGE_RECEIVE = 2;
 	    public static final int COVERAGE_INTERFERENCE = 3;
 	    
 	    public static final int FATDMA_CHANNEL_A = 1;
 	    public static final int FATDMA_CHANNEL_B = 2;
 	    
 	    Connection conn = null;
 	    
 	    public DerbyDBInterface(String driver, String protocol){
 	    	this.driver = driver;
 	    	this.protocol = protocol;
 	    }
 	    
 	    public DerbyDBInterface(){
 	    	try{
 	    		if(this.conn == null)
 	    			this.conn = this.getDBConnection(null, false);
 	    	}catch(Exception e){
 	    		e.printStackTrace();
 	    	}
 	    }
 	    
 	    public static void main(String[] args) {
 	    	
 	    	if(args == null || (args.length > 0 && args[0].equalsIgnoreCase("test"))){
 		    	System.out.println("Testing the database. Printing the base stations...");
 		    	dbTester();
 	    	}else{
 	    		System.out.println("Destroying the old database and creating a new one.");
 	    	
 	    		DerbyDBInterface dba =  new DerbyDBInterface();
 	    		dba.createDatabase(null);
 	    	}
 	       
 	    }
 
 	    /**
 	     * Creates the connection to the database.
 	     * 
 	     * Checks if the database exists and if it does not, it creates it (using this.createDatabase(dbName))
 	     * 
 	     * @param dbName Name of the database. If null, default(eavdam) will be used.
 	     * @param creatingDatabase Indicates if we are in the process of creating the database. If true, the check "if databases exists" is not made.
 	     * @return The connection.
 	     * @throws SQLException
 	     */
 	    private Connection getDBConnection(String dbName, boolean creatingDatabase) throws SQLException{
 	        /* load the desired JDBC driver */
 	        loadDriver();
 	        
 	        
 	        
 	        if(dbName == null) dbName = defaultDB;
 	        
             Properties props = new Properties(); // connection properties
             // providing a user name and password is optional in the embedded
             // and derbyclient frameworks
             props.put("user", "eavdam");
             props.put("password", "DaMSA");
 	        
             this.conn = DriverManager.getConnection(protocol + dbName+ ";create=true", props);
             
             //Test if the database exists:
             if(!creatingDatabase){
             	boolean createdDB = false;
 	            try{
 	            	PreparedStatement ps = conn.prepareStatement("SELECT id, name FROM STATIONTYPE WHERE id=1");
 	            	ResultSet rs = ps.executeQuery();
 	            	
 	            	rs.close();
 	            	ps.close();
 	            	
 	            }catch(SQLException e){
 //	            	e.printStackTrace();
 	            	System.out.println("Database does not exist, creating it...");
 	            	this.createDatabase(dbName);
 	            	createdDB = true;
 	            }
 
 	            if(!createdDB) this.updateDatabaseVersion();	            
 
 	    	}	
             
             
             return this.conn;
 	    }
 	
 	    public void closeConnection(){
 	    	try{
 		    	if(this.conn != null)
 		    		this.conn.close();
 	    	}catch(Exception e){
 	    		e.printStackTrace();
 	    	}
 	    }
 	    
 	    /**
 	     * Inserts the data to the database. Depending on the data 
 	     * 
 	     * @param data 
 	     * @return Boolean value that indicates if the insert was successful
 	     */
 	    public boolean insertEAVDAMData(ArrayList<EAVDAMData> data){
 	    	boolean success = true;
 	    	for(EAVDAMData d : data){
 	    		if(!this.insertEAVDAMData(d)){
 	    			success = false;
 	    		}
 	    	}
 	    	
 	    	return success;
 	    }
 	    
 	    public boolean insertEAVDAMData(EAVDAMData data){
  	
 	        boolean success = true;
 	        
 	        if(data.getUser() == null){
 	        	System.out.println("No user given...");
 	        	return false;
 	        }
 	        
 	        try{
 	        	
 	        	if(conn == null) conn = getDBConnection(defaultDB, false);
 	    	
 
 	        	int orgID = this.getOrganizationID(data.getUser(), false);
 	        	
 	        	
 	        	if(data.getActiveStations() != null && data.getActiveStations().size() > 0){
 	        		for(ActiveStation a : data.getActiveStations()){
 	        			this.insertActiveStations(a, orgID);
 	        		}
 	        		
 	        		data.setActiveStations(this.retrieveActiveStations(orgID));
 	        	}
 	        	
 	        	if(data.getOldStations() != null && data.getOldStations().size() > 0){
 	        		for(AISFixedStationData ais : data.getOldStations()){
 //	        			System.out.println("Inserting old station to the database...");
 	        			this.insertStation(ais, -1, orgID, 0);
 	        		}
 	        	}
 	        	
 	        	if(data.getSimulatedStations() != null && data.getSimulatedStations().size() > 0){
 	        		for(Simulation sim : data.getSimulatedStations()){
 	        			this.insertSimulationDataset(sim, orgID);
 	        		}
 	        	}
 	        	
 	        	if(data.getOtherUsersStations() != null && data.getOtherUsersStations().size() > 0){
 	        		for(OtherUserStations other : data.getOtherUsersStations()){
 	        			int otherID = this.getOrganizationID(other.getUser(), false);
 	        			for(ActiveStation active : other.getStations()){
 		        			for(AISFixedStationData ais : active.getStations()){
 //		        				System.out.println("Inserting active station of other users to the database...");
 		        				this.insertStation(ais, -1, otherID, 0);
 		        			}
 	        			}
 	        		}
 	        	}
 	        	
 	        	//Insert the data from XML import...
 	        	if(data.getStations() != null && data.getStations().length > 0){
 //	        		System.out.println("XML IMPORT!");
 	        		for(AISFixedStationData s : data.getStations()){
 	        			this.insertStation(s, -1, orgID, 0);
 	        		}
 	        	}
 	        	
             	
 	        }catch(Exception e){
 	        	e.printStackTrace();
 	        	success = false;
 	        }
 	        
 //	        System.out.println("Insert "+(success ? "was a success" : "was a failure"));
 	        
 	        return success;
 	    }
 	    
 
 		/**
 	     * Finds the id for the address. Returns -x if the address is new.
 	     * 
 	     * -x is the negative value for the new id. So if the result is -28, the first available id is 28.
 	     * 
 	     * 
 	     * @param address 
 	     * @return
 	     * @throws Exception
 	     */
 	    private int getAddressID(Address address) throws Exception{
 	    	if(address == null) return 0; 
 	    	
 	    	PreparedStatement ps = conn.prepareStatement("select id from ADDRESS where addressline1 = ? AND zipcode = ? AND city = ? AND country = ?");
     		ps.setString(1, address.getAddressline1());
     		ps.setString(2, address.getZip());
     		ps.setString(3, address.getCity());
     		ps.setString(4, address.getCountry());
     		
     		ResultSet res = ps.executeQuery();
     		int addressID = -1;
     		if(!res.next()){
     			//No address found. Add new one. First, get the highest id...
     			res.close();
         		ps = conn.prepareStatement("select max(id) from ADDRESS");
         		res = ps.executeQuery();
     			if(!res.next()){
     				addressID = 0+1;
     			}else{
     				addressID = res.getInt(1)+1;
     			}
     			
     			this.insertAddress(address, addressID);
     		}else{
     			//TODO Call update if necessary!
     			
     			return res.getInt(1);
     		}
     		
     		ps.close();
     		res.close();
     		return addressID;
 	    }
 	    
 	    private int insertAddress(Address address, int id) throws Exception{
 			if(address == null) return 0;
 	    	
     		PreparedStatement ps = conn.prepareStatement("insert into ADDRESS values (?,?,?,?,?,?)");
     		ps.setInt(1, id);
     		ps.setString(2, address.getAddressline1());
     		ps.setString(3, address.getAddressline2());
     		ps.setString(4, address.getZip());
     		ps.setString(5, address.getCity());
     		ps.setString(6, address.getCountry());
     		
     		ps.executeUpdate();
     		ps.close();
     		
     		return id;
 	    }
 	    
 	    private int getPersonID(Person person) throws Exception{
     		if(person == null){
     			return 0;
     		}
 	    	
 	    	PreparedStatement ps = conn.prepareStatement("select id from PERSON where email = ?");
     		
     		ps.setString(1, person.getEmail());
     		ResultSet res = ps.executeQuery();
     		int contactID = -1;
     		if(!res.next()){ //No such person, add new one...
     			res.close();
         		ps = conn.prepareStatement("select max(id) from PERSON");
         		res = ps.executeQuery();
     			if(!res.next()){
     				contactID = 1;
     			}else{
     				contactID = res.getInt(1)+1;
     			}
     			
     			this.insertPerson(person, contactID);
     			
     		}else{
     			//TODO Call update if necessary!
     			contactID = res.getInt(1);
 
     			System.out.println("Person found! Updating information...");
     			this.updatePerson(person);
     			
     			return contactID;
     		}
     		
     		ps.close();
     		res.close();
     		
     		return contactID;
 	    }
 	    
 	    private void insertPerson(Person person, int id) throws Exception{
 	    	//Find if the addresses exists.
 			int personPostalAddress = this.getAddressID(person.getPostalAddress());
 			int personVisitingAddress = this.getAddressID(person.getVisitingAddress());
 
 			
 						
     		PreparedStatement ps = conn.prepareStatement("insert into PERSON values (?,?,?,?,?,?,?,?)");
     		ps.setInt(1, id);
     		ps.setString(2, person.getName());
     		ps.setString(3, person.getEmail());
     		ps.setString(4, person.getPhone());
     		ps.setString(5, person.getFax());
     		ps.setString(6, person.getDescription());
     		ps.setInt(7, personVisitingAddress);
     		ps.setInt(8, personPostalAddress);
     		
     		
     		ps.executeUpdate();
     		ps.close();
 
 	    }
 	    
 	    private int getOrganizationID(EAVDAMUser user, boolean defaultUser) throws Exception{
 	        
 	    	if(this.conn == null) this.getDBConnection(null, false);
 	    			
 	    	if(user == null) return 0;
 	    	
 	    	
         	PreparedStatement ps = conn.prepareStatement("select id from ORGANIZATION where ORGANIZATION.organizationname = ? OR ORGANIZATION.id = ?");
         	ps.setString(1, user.getOrganizationName());
         	ps.setInt(2, user.getUserDBID());
         	ResultSet res = ps.executeQuery();
         	
         	int orgID = -1;
         	//Find out if the organization exists in the database.
         	if(!res.next()){
         		System.out.println("Organization "+user.getOrganizationName()+" does not exist! Inserting new organization...");
         		
         		//Add the organization.
         		ps = conn.prepareStatement("select max(id) from ORGANIZATION");
         		ResultSet rs = ps.executeQuery();
         		if(!rs.next()){
         			orgID = 1;
         		}else{
         			orgID = rs.getInt(1)+1;
         		}
         		rs.close();
         		ps.close();
         		this.insertOrganization(user, orgID, defaultUser);
         	}else{
         		
         		orgID = res.getInt(1);
         		System.out.println("Organization found (id: "+orgID+")! Updating its information...");
         		
         		user.setUserDBID(orgID);
         		this.updateOrganization(user);
         		
         		
         	}
         	
         	ps.close();
         	res.close();
         	
         	user.setUserDBID(orgID);
         	
         	return orgID;
 	    }
 	    
 	    /**
 	     * Insert a new user = organization to the database.
 	     * 
 	     * @param user The user to be inserted to the database
 	     * @param defaultUser Indicates if the user is the default one = the one who is loaded at initialization and who controls the stations inserted from this instance of the application.
 	     * @return Database id for the given organization.
 	     */
 	    public int insertEAVDAMUser(EAVDAMUser user, boolean defaultUser) throws Exception{
 	    	return this.getOrganizationID(user, defaultUser);
 	    }
 	    
 	    private void insertOrganization(EAVDAMUser user, int id, boolean defaultUser) throws Exception{
     		//Start with the POSTAL address
     		int postalID = 0;
     		if(user.getPostalAddress() != null)
     			postalID = this.getAddressID(user.getPostalAddress()); //Insert a new entry if the address is not found.
     		
     		int visitingID = 0;
     		if(user.getVisitingAddress() != null)
     			visitingID = this.getAddressID(user.getVisitingAddress());
     		
     		//Check the CONTACT person.
     		int contactPersonID = 0;
     		if(user.getContact() != null)
     			contactPersonID = this.getPersonID(user.getContact());
     	
     		//Check the TECHNICAL person.
     		int technicalPersonID = 0;
     		if(user.getTechnicalContact() != null)
     			technicalPersonID = this.getPersonID(user.getTechnicalContact());
     		
     		PreparedStatement ps = conn.prepareStatement("insert into ORGANIZATION values (?,?,?,?,?,?,?,?,?,?,?,?)");
     		ps.setInt(1,id);
     		ps.setString(2, user.getOrganizationName());
     		ps.setString(3, user.getCountryID());
     		ps.setString(4, user.getPhone());
     		ps.setString(5, user.getFax());
     		ps.setString(6, (user.getWww() != null ? user.getWww().toString() : null));
     		ps.setString(7, user.getDescription());
     		ps.setInt(8, contactPersonID);
     		ps.setInt(9, technicalPersonID);
     		ps.setInt(10, visitingID);
     		ps.setInt(11, postalID);
     		ps.setInt(12, (defaultUser ? 1 : 0));
     		
     		ps.executeUpdate();
     		ps.close();
     		
 	    }
 	    
 	    /**
 	     * Inserts the active station to the database.
 	     * 
 	     * @param station
 	     */
 	   private void insertActiveStations(ActiveStation station, int organizationID) throws Exception{
 		   if(station == null) return;
 	    	
 		   AISFixedStationData active = null;
 		   AISFixedStationData planned = null;
 		   
 		   System.out.println("There are "+station.getStations().size()+" active station...");
 		   
 		   for(AISFixedStationData d : station.getStations()){
 
 			   if(d.getStatus().getStatusID() == STATUS_ACTIVE) active = d;
 			   else if(d.getStatus().getStatusID() == STATUS_PLANNED) planned = d;
 		   }
 	
 		   //Update to change the status...
 		   if(active == null && planned == null){
 			   if(station.getStations().size() > 0){
 				   AISFixedStationData s = station.getStations().get(0);
 				   
 				   
 				   System.out.println("Updating status...");
 				   this.updateAISStation(s, organizationID, -1);
 				   
 			   }
 		   }else if((active != null && planned == null)){
 			   
 			   int stationID = this.insertStation(active, -1, organizationID, 0);
 			   
 			   
 		   }else if((active == null && planned != null)){
 			   
 			   int stationID = this.insertStation(planned, -1, organizationID, 0);
 			   
 			   
 		   }else{
 			   System.out.println("Making status changes...");
 			   
 			   int stationID = -1;
 
 			   if(active != null){
 				   stationID = this.insertStation(active, -1, organizationID, 0);
 			   }
 			   
 			   if(planned != null){
 				   stationID = this.insertStation(planned, stationID, organizationID, 0);
 			   }
 		    	
 		   }
 		   
 		   if(station.getProposals() != null && station.getProposals().size() > 0){
 			   for(EAVDAMUser proposal : station.getProposals().keySet()){
 				   this.insertStation(station.getProposals().get(proposal), -1, organizationID, this.getOrganizationID(proposal, false));
 			   }
 		   }
 	   }
 	    
 	    /**
 	     * Inserts the new AIS Station to the databse. IF a station with the same MMSI and same location can be found, the station is updated instead!  
 	     * 
 	     * That is, if latitude, longitude or MMSI changes, a new station will be created!
 	     * 
 	     * @param as Station data
 	     * @param refstation To which station this status refers to. For example, planned station always refers to an old/active station.
 	     * @param status The status of the station
 	     * @param organizationID Owner of the station
 	     * @param proposee Who proposed this station
 	     * @return Id of the station
 	     * @throws Exception SQLException will be thrown if there are some problems.
 	     */
 	    private int insertStation(AISFixedStationData as, int refstation, int organizationID, int proposee) throws Exception{
 	    	if(as == null) return -1;
 	    	
 	    	if(this.conn == null) this.getDBConnection(null, false);
 	    	
 	    	//Check if the station exists.
 	    	
 
 	    	
 	    	
 	    	String update_sql = "select id " +
 	    			"from FIXEDSTATION, STATUS " +
 	    			"where FIXEDSTATION.id = STATUS.station AND STATUS.statustype = ? AND " +
 	    			"(FIXEDSTATION.id = ? OR (mmsi = ? AND lat = ? AND lon = ?))";
 	    	PreparedStatement update = conn.prepareStatement(update_sql);
 	    	update.setInt(1, as.getStatus().getStatusID());
 	    	update.setInt(2, as.getStationDBID());
 	    	update.setString(3, as.getMmsi());
 	    	update.setDouble(4, as.getLat());
 	    	update.setDouble(5, as.getLon());
 	    	
 	    	ResultSet res = update.executeQuery();
 	    	if(res.next()){
 	    		int id = res.getInt(1);
 	    		if(id >= 0){
 	    			
 	    			System.out.println("Station exists. Updating its information! (id: "+as.getStationDBID()+" vs. dbid: "+id+", status: "+as.getStatus().getStatusID()+", owner: "+organizationID+")");
 	    			as.setStationDBID(id);
 	    			this.updateAISStation(as, organizationID, refstation);
 	    			
 	    			res.close();	    			
 	    			update.close();
 	    			return id;
 	    		}
 	    	}
 	    	
 	    	System.out.println("Inserting new AIS Station...");
 	    	
 	    	int stationType;
 	    	//Get the station type.
 	    	switch(as.getStationType()){
 	    		case BASESTATION: stationType = STATION_BASE; break;
 	    		case ATON: stationType = STATION_ATON; break;
 	    		case RECEIVER: stationType = STATION_RECEIVER; break;
 	    		case REPEATER: stationType = STATION_REPEATER; break;
 	    	
 	    		default: stationType = STATION_BASE;
 	    	}
 	    	
 	    	PreparedStatement psc = conn.prepareStatement("select max(id) from FIXEDSTATION");
 	    	ResultSet rs = psc.executeQuery();
 	    	int stationID = 0;
 	    	if(!rs.next()){
 	    		stationID = 1;
 	    	}else{
 	    		stationID = rs.getInt(1)+1;
 	    	}
 	    	
 	    	rs.close();
 	    	psc.close();
 	    	
 	    	//Insert station
 	    	PreparedStatement ps = conn.prepareStatement("insert into FIXEDSTATION values (?,?,?,?,?,?,?,?,?,?,?)");
 	    	ps.setInt(1, stationID);
 	    	ps.setString(2, as.getStationName());
 	    	ps.setInt(3,organizationID);
 	    	ps.setString(4, as.getMmsi());
 	    	ps.setDouble(5, as.getLat());
 	    	ps.setDouble(6, as.getLon());
 	    	ps.setDouble(7, (as.getTransmissionPower() != null ? as.getTransmissionPower() : 0));
 	    	ps.setString(8, as.getDescription());
 	    	ps.setInt(9, stationType);
 	    	ps.setString(10, as.getAnythingString());
 	    	ps.setInt(11, proposee);
 	    	
 	    	ps.executeUpdate();
 	    	ps.close();
 	    	
 	    	//Insert Antenna
 	    	int antennaType;
 	    	//Get the antenna type.
 	    	if(as.getAntenna() == null){
 	    		antennaType = 0;
 	    	}else{
 		    	switch(as.getAntenna().getAntennaType()){
 		    		case DIRECTIONAL: antennaType = ANTENNA_DIRECTIONAL; break;
 		    		case OMNIDIRECTIONAL: antennaType = ANTENNA_OMNIDIR; break;
 		    	
 		    		default: antennaType = ANTENNA_DIRECTIONAL;
 		    	}
 		    	
 		    	this.insertAntenna(as.getAntenna(), stationID, antennaType);
 	    	}
 	    	
 	    	//Insert the different coverage types.
 	    	this.insertCoverage(as.getTransmissionCoverage(), stationID, COVERAGE_TRANSMIT);
 	    	this.insertCoverage(as.getReceiveCoverage(), stationID, COVERAGE_RECEIVE);
 	    	this.insertCoverage(as.getInterferenceCoverage(), stationID, COVERAGE_INTERFERENCE);
 	    	
 	    	//Insert Status
 	    	this.insertStatus(as.getStatus(), refstation, as.getStatus().getStatusID(), stationID);
 	    	
 	    	
 	    	//Insert FATDMA Allocations
 	    	if(as.getFATDMAChannelA() != null){
 	    		as.getFATDMAChannelA().setDBID(-1);
 	    		
 //	    		System.out.println("FATDMA ALLOCATIONS FOUND!");
 	    		this.insertFATDMAAllocations(as.getFATDMAChannelA(), stationID, FATDMA_CHANNEL_A);
 	    	}
 
 	    	if(as.getFATDMAChannelB() != null){
 	    		as.getFATDMAChannelB().setDBID(-1);
 //	    		System.out.println("FATDMA ALLOCATIONS FOUND!");
 	    		this.insertFATDMAAllocations(as.getFATDMAChannelB(), stationID, FATDMA_CHANNEL_B);
 	    	}
 	    	
 	    	as.setStationDBID(stationID);
 	    	
 	    	return stationID;
 	    }
 	    
 	    public void insertAntenna(Antenna antenna, int stationID, int antennaType) throws Exception{
 	    	if(antenna == null) return;
 	    	
 	    	PreparedStatement psc = conn.prepareStatement("insert into ANTENNA values (?,?,?,?,?,?,?)");
 	    	
 	    	psc.setInt(1, stationID);
 	    	psc.setDouble(2, antenna.getAntennaHeight());
 	    	psc.setDouble(3, antenna.getTerrainHeight());
 	    	psc.setInt(4, (antenna.getHeading() != null ? antenna.getHeading() : 0));
 	    	psc.setDouble(5, (antenna.getFieldOfViewAngle() != null ? antenna.getFieldOfViewAngle() : 0));
 	    	psc.setDouble(6, (antenna.getGain() != null ? antenna.getGain() : 0));
 	    	psc.setInt(7, antennaType);
 	    	psc.executeUpdate();
 	    	
 	    	psc.close();
 	    	
 	    }
 	    
 	    public void insertStatus(AISFixedStationStatus status, int refstation, int statusID, int stationID) throws Exception{
 	    	PreparedStatement psc = conn.prepareStatement("insert into STATUS values (?,?,?,?,?)");
 	    	
 	    	psc.setInt(1, stationID);
 	    	psc.setInt(2, refstation);
 	    	psc.setInt(3, statusID);
 	    	psc.setDate(4, (status.getStartDate() != null ? status.getStartDate() : null));
 	    	psc.setDate(5, status.getEndDate());
 	    	psc.executeUpdate();
 	    	
 	    	psc.close();
 	    }
 	    
 	    
 	   /**
 	    * Inserts the FATDMA allocations to the database for the given station.
 	    *  
 	    * @param fatdma The FATDMA allocations.
 	    * @param stationID Station that has the given allocations.
 	    */
 	    public void insertFATDMAAllocations(FATDMAChannel f, int stationID, int channelType) throws Exception{
 	    	if(f == null) return;
 	    	
 	    	System.out.println("Inserting FATDMA Allocations");
 	    	
 	    	if(this.conn == null) this.conn = getDBConnection(null,false);
 	    	
 	    		try{
 	    	    	//Check if the old channel exists...
 	    			PreparedStatement eps = conn.prepareStatement("select id from FATDMACHANNEL where station = ? AND type = ?");
 	    			eps.setInt(1, stationID);
 	    			eps.setInt(2, channelType);
 	    			ResultSet ers = eps.executeQuery();
 	    			if(ers.next()){
 	    				f.setDBID(ers.getInt(1));
 	    			}
 	    			
 	    			//Insert the channel
 	    			boolean insertNew = false;
 	    			if(f.getDBID() <= 0){
 	    				PreparedStatement fp = conn.prepareStatement("select max(id) from FATDMACHANNEL");
 	    				ResultSet rs = fp.executeQuery();
 	    				if(rs.next()){
 	    					int id = rs.getInt(1)+1;
 	    					f.setDBID(id);
 	    				}else{
 	    					f.setDBID(1);
 	    				}
 	    				
 	    				fp = conn.prepareStatement("insert into FATDMACHANNEL values (?,?,?,?)");
 	    				fp.setInt(1, f.getDBID());
 	    				fp.setInt(2, stationID);
 	    				fp.setString(3, f.getChannelName());
 	    				fp.setInt(4, channelType);
 	    				fp.executeUpdate();
 	    				
 	    				rs.close();
 	    				fp.close();
 	    				
 	    				insertNew = true;
 	    				
 		    			System.out.println("Inserted new channel "+f.getDBID());
 	    			}
 	    			
 	    			if(f instanceof AISAtonStationFATDMAChannel){
 	    				AISAtonStationFATDMAChannel aton = (AISAtonStationFATDMAChannel)f;
 
 	    	    		//Delete the old ones
 	    		    	PreparedStatement delete = conn.prepareStatement("delete from FATDMAATON where channel = "+f.getDBID());
 	    		    	delete.executeUpdate();
 	    				
 	    				
 	    				PreparedStatement p = conn.prepareStatement("select max(id) from FATDMAATON");
 	    				ResultSet rs = p.executeQuery();
 	    				int maxID = 0;
 	    				if(rs.next()){
 	    					maxID = rs.getInt(1);
 	    				}
 
 	    				rs.close();
 	    				p.close();
 	    				for(AtonMessageBroadcastRate r : aton.getAtonMessageBroadcastList()){
 	    					if((r.getDbID() == null || r.getDbID().intValue() <= 0) || insertNew){
 		    		    		maxID = maxID + 1;
 		    			    	r.setDbID(new Integer(maxID));
 		    			    	this.insertAtonMessageBroadcastRate(r, f.getDBID());
 	    					}else{
 	    						//UPDATE
 	    						this.updateAtonMessageBroadcastRate(r);
 	    					}
 	    				}
 	    			}else{
 	    				AISBaseAndReceiverStationFATDMAChannel base = (AISBaseAndReceiverStationFATDMAChannel)f;
 
 	    	    		//Delete the old ones
 	    		    	PreparedStatement delete = conn.prepareStatement("delete from FATDMABASE where channel = "+f.getDBID());
 	    		    	delete.executeUpdate();
 	    				
 	    				PreparedStatement p = conn.prepareStatement("select max(id) from FATDMABASE");
 	    				ResultSet rs = p.executeQuery();
 	    				int maxID = 0;
 	    				if(rs.next()){
 	    					maxID = rs.getInt(1);
 	    				}
 	    				
 	    				rs.close();
 	    				p.close();
 	    				
 	    				for(FATDMAReservation r : base.getFATDMAScheme()){
 	    					if(r.getDbID() == null || r.getDbID().intValue() <= 0 || insertNew){
 			    				maxID = maxID + 1;
 		    			    	r.setDbID(new Integer(maxID));
 		    			    	this.insertFATDMAReservation(r, f.getDBID());
 	    					}else{
 	    						//UPDATE
 	    						this.updateFATDMAReservation(r);
 	    					}
 	    				}
 	    				
 	    			}
 
 	    		}catch(Exception e){
 	    			e.printStackTrace();
 	    		}
 	    	
 	    }
 	    
 	    private int insertAtonMessageBroadcastRate(AtonMessageBroadcastRate r, int channelID) throws Exception{
 
 	    	
 	    	if(r.getDbID() == null || r.getDbID() <= 0){
 				PreparedStatement p = conn.prepareStatement("select max(id) from FATDMAATON");
 				ResultSet rs = p.executeQuery();
 				int maxID = 0;
 				if(rs.next()){
 					maxID = rs.getInt(1);
 				}
 				
 				r.setDbID(new Integer(maxID + 1));
     		}
 	    	
 	    	
 	    	PreparedStatement psc = conn.prepareStatement("insert into FATDMAATON values (?,?,?,?,?,?,?,?,?,?)");
 	    	
     		psc.setInt(1, r.getDbID());
 	    	psc.setInt(2, channelID);
 	    	psc.setString(3, r.getAccessScheme());
 	    	psc.setInt(4, r.getMessageID());
 	    	psc.setInt(5, r.getUTCHour());
 	    	psc.setInt(6, r.getUTCMinute());
 	    	psc.setInt(7, r.getStartslot());
 	    	psc.setInt(8, r.getBlockSize());
 	    	psc.setInt(9, r.getIncrement());
 	    	psc.setString(10, r.getUsage());
 	    	
 	    	
 	    	psc.executeUpdate();
 	    	
 	    	psc.close();
 	    	
 	    	return r.getDbID();
 	    }
 	    
 	    private int insertFATDMAReservation(FATDMAReservation r, int channelID) throws Exception{
 
 	    	if(r.getDbID() == null || r.getDbID() <= 0){
 				PreparedStatement p = conn.prepareStatement("select max(id) from FATDMABASE");
 				ResultSet rs = p.executeQuery();
 				int maxID = 0;
 				if(rs.next()){
 					maxID = rs.getInt(1);
 				}
 				
 				r.setDbID(new Integer(maxID + 1));
     		}
 	    	
 	    	PreparedStatement psc = conn.prepareStatement("insert into FATDMABASE values (?,?,?,?,?,?,?)");
     		
 	    	psc.setInt(1, r.getDbID());
 	    	psc.setInt(2, channelID);
 	    	psc.setInt(3, r.getStartslot());
 	    	psc.setInt(4, r.getBlockSize());
 	    	psc.setInt(5, r.getIncrement());
 	    	psc.setString(6, r.getOwnership());
 	    	psc.setString(7, r.getUsage());
 	    	psc.executeUpdate();
 	    	
 	    	psc.close();
 
 	    	return r.getDbID();
 	    }
 	    
 	    public void insertCoverage(AISFixedStationCoverage coverage, int stationID, int coverageType) throws Exception{
 	    	if(coverage == null || coverage.getCoveragePoints() == null) return;
 	    	
 	    	int ith = 1;
 	    	for(double[] c : coverage.getCoveragePoints()){
 	    		try{
 		    		PreparedStatement psc = conn.prepareStatement("insert into COVERAGEPOINTS values (?,?,?,?,?)");
 			    	
 //		    		System.out.println("Inserting: "+stationID+", "+c[0]+", "+c[1]);
 		    		
 			    	psc.setInt(1, stationID);
 			    	psc.setDouble(2, c[0]); //lat
 			    	psc.setDouble(3, c[1]); //lon
 			    	psc.setDouble(4, ith); 
 			    	psc.setDouble(5, coverageType); 
 			    	psc.executeUpdate();
 			    
 			    	++ith;
 			    	
 			    	psc.close();
 	    		}catch(Exception e){
 	    			e.printStackTrace();
 	    		}
 	    		
 	    		
 	    	}
 	    	
 	    	
 	    }
 	    
 	    private FATDMAChannel retrieveFATDMAAllocations(int stationID, int channelType) throws Exception{
 	    	System.out.print("Retrieving FATDMA Channel "+(channelType == FATDMA_CHANNEL_A ? "A" : "B"));
 	    	
 	    	//GET THE Channel
 	    	PreparedStatement psc = conn.prepareStatement("select id, name from FATDMACHANNEL where station = ? AND type = ?");
 	    	psc.setInt(1, stationID);
 	    	psc.setInt(2, channelType);
 	    	
 	    	FATDMAChannel tempChannels = new FATDMAChannel();
 	    	
 	    	ResultSet rs = psc.executeQuery();
 	    	while(rs.next()){
 	    		FATDMAChannel c = new FATDMAChannel();
 	    		c.setDBID(rs.getInt(1));
 	    		c.setChannelName(rs.getString(2));
 	    		
 	    		tempChannels = c;
 	    	}
 	    	
 	    	psc.close();
 	    	rs.close();
 	    	
 	    	
 	    	
 	    	//Get ATON
     		PreparedStatement ps = conn.prepareStatement("select id, blocksize,increment,messageid,startslot,accessscheme,utchour,utcminute,usage from FATDMAATON where channel = ?");
 	    	ps.setInt(1, tempChannels.getDBID());
 		    	
 	    	ResultSet r = ps.executeQuery();
 		    	
 		    List<AtonMessageBroadcastRate> rates = new ArrayList<AtonMessageBroadcastRate>();
 		    while(r.next()){
 		    		
 		    	AtonMessageBroadcastRate aton = new AtonMessageBroadcastRate();
 		    	aton.setDbID(new Integer(r.getInt(1)));
 		    	aton.setBlockSize(new Integer(r.getInt(2)));
 		    	aton.setIncrement(new Integer(r.getInt(3)));
 		    	aton.setMessageID(new Integer(r.getInt(4)));
 		    	aton.setStartslot(new Integer(r.getInt(5)));
 		    	aton.setAccessScheme(r.getString(6));
 		    	aton.setUTCHour(new Integer(r.getInt(7)));
 		    	aton.setUTCMinute(new Integer(r.getInt(8)));
 		    	aton.setUsage(r.getString(9));
 		    	
 		    	rates.add(aton);
 		    }
 		    
    
 		    ps.close();
 
 		    if(rates.size() > 0){ //The channel was an Aton channel
 		    	AISAtonStationFATDMAChannel channel = new AISAtonStationFATDMAChannel(tempChannels.getChannelName());
 			    channel.setDBID(tempChannels.getDBID());
 			    channel.setAtonMessageBroadcastList(rates);
 			    
 	    		System.out.print("... Found "+rates.size()+" ATON allocations...\n");
 			    
 			    return channel;	
 			    
 		    }else //GET BASE STATION FATDMA
 		    	ps = conn.prepareStatement("select id, blocksize,increment,startslot,ownership,usage from FATDMABASE where channel = ?");
 			    ps.setInt(1, tempChannels.getDBID());
 			    	
 			    r = ps.executeQuery();
 			    List<FATDMAReservation> bases = new ArrayList<FATDMAReservation>();
 			    while(r.next()){
 			    	FATDMAReservation res = new FATDMAReservation();
 			    	res.setDbID(new Integer(r.getInt(1)));
 			    	res.setBlockSize(new Integer(r.getInt(2)));
 			    	res.setIncrement(new Integer(r.getInt(3)));
 			    	res.setStartslot(new Integer(r.getInt(4)));
 			    	res.setOwnership(r.getString(5));
 			    	res.setUsage(r.getString(6));
 			    	
 			    	bases.add(res);
 			    }
 		    		
 		    	if(bases.size() > 0){
 		    		AISBaseAndReceiverStationFATDMAChannel channel = new AISBaseAndReceiverStationFATDMAChannel(tempChannels.getChannelName());
 		    		channel.setDBID(tempChannels.getDBID());
 		    		channel.setFatdmaScheme(bases);
 		    			
 		    		System.out.print("... Found "+bases.size()+" BASE allocations (Channel: "+channel.getDBID()+" | "+channel.getFATDMAScheme().get(0).toString()+")...\n");
 		    		
 		    		
 		    		return channel;
 		    	}
 			    	
 		    	System.out.println();
 	    	
 		    	return null;
 	    }
 	    
 	    /**
 	     * Deletes the given allocations from the database. 
 	     * 
 	     * @param stationID Station from which the allocations are deleted.
 	     * @param allocations The deleted allocations.
 	     * @throws Exception SQLException
 	     */
 	    public void deleteFATDMAAllocations(int stationID, List<FATDMAChannel> allocations) throws Exception{
 	    	if(allocations == null || allocations.size() <= 0) return;
 	    	
 	    	for(FATDMAChannel f : allocations){
 	    		if(f instanceof AISAtonStationFATDMAChannel){
 	    			AISAtonStationFATDMAChannel aton = (AISAtonStationFATDMAChannel)f;
 	    			
 	    			for(AtonMessageBroadcastRate r : aton.getAtonMessageBroadcastList()){
 		    			//Update
 				    	String sql = "delete from FATDMAATON" 
 				    			+ " where station = ? and id = ?";
 				    	PreparedStatement ps = conn.prepareStatement(sql);
 				    	ps.setInt(1, stationID);
 				    	ps.setInt(2, r.getDbID());
 				    	ps.executeUpdate();
 				    
 				    	ps.close();
 	    			}
 	    		}else{
 	    			AISBaseAndReceiverStationFATDMAChannel base = (AISBaseAndReceiverStationFATDMAChannel)f;
 	    			
 	    			for(FATDMAReservation r : base.getFATDMAScheme()){
 		    			//Update
 				    	String sql = "delete from FATDMABASE" 
 				    			+ " where station = ? and id = ?";
 				    	PreparedStatement ps = conn.prepareStatement(sql);
 				    	ps.setInt(1, stationID);
 				    	ps.setInt(2, r.getDbID());
 				    	ps.executeUpdate();
 				    	
 				    	ps.close();
 	    			}
 	    		}
 	    		
 	    		
 	    		PreparedStatement p = conn.prepareStatement("select * from FATDMAATON, FATDMABASE where FATDMAATON.channel = ? OR FATDMABASE.channel = ?");
 	    		ResultSet rs = p.executeQuery();
 	    		if(!rs.next()){
 	    			System.out.println("No FATDMA allocation remaining for channel "+f.getChannelName()+"! Deleting the channel!");
 	    			
 	    			p = conn.prepareStatement("delete from FATDMACHANNEL where id = ?");
 	    			p.setInt(1, f.getDBID());
 	    			p.executeUpdate();
 	    		}
 	    		
 	    		rs.close();
 	    		p.close();
 	    		
 	    	}
 
 	    }
 	    
 	    private void insertSimulationDataset(Simulation sim, int organizationID) throws Exception{
 	    	if(this.conn == null) this.getDBConnection(null, false);
 	    	
 	    	int simID = 0;
 	    	PreparedStatement ps = conn.prepareStatement("select ID from SIMULATION where name = ?");
 	    	ps.setString(1, sim.getName());
 	    	ResultSet rs = ps.executeQuery();
 	    	if(rs.next()){
 	    		simID = rs.getInt(1);
 	    	}else{
 	    		ps = conn.prepareStatement("select max(ID) from SIMULATION");
 	    		rs = ps.executeQuery();
 	    		if(rs.next()){
 	    			simID = rs.getInt(1)+1;
 	    		}else{
 	    			simID = 1;
 	    		}
 
 	    		ps = conn.prepareStatement("insert into SIMULATION values (?,?)");
 	    		ps.setInt(simID, 1);
 	    		ps.setString(2, sim.getName());
 	    		ps.executeUpdate();
 	    	}
 	    	
 	    	for(AISFixedStationData s : sim.getStations()){
 	    		this.insertSimulatedStation(s, simID, organizationID);
 	    	}
 	    	
 	    	rs.close();
 	    	ps.close();
 	    	
 	    }
 	    
 	    private void insertSimulatedStation(AISFixedStationData station, int simulationID, int orgID) throws Exception{
 	    	int stationID = this.insertStation(station, -1, orgID, 0);
 	    	
 	    	PreparedStatement check = conn.prepareStatement("select * from SIMULATIONSTATION where STATIONID = ? AND SIMULATIONID = ?");
 	    	check.setInt(1, stationID);
 	    	check.setInt(2, simulationID);
 	    	ResultSet rs = check.executeQuery();
 	    	if(!rs.next()){
 		    	PreparedStatement ps = conn.prepareStatement("insert into SIMULATIONSTATION values (?,?)");
 		    	ps.setInt(1, stationID);
 		    	ps.setInt(2, simulationID);
 		    	ps.executeUpdate();
 		    	ps.close();
 	    	}
 	    	
 	    	rs.close();
 	    	check.close();
 	    }
 	    
 	    /**
 	     * Deletes the given simulation.
 	     * 
 	     * @param simulation This should be the name of the simulation
 	     */
 	    public void deleteSimulation(String simulation) throws Exception{
 	    	if(this.conn == null) this.getDBConnection(null, false);
 	    	
 	    	
 	    	int simID = 0;
 	    	
 	    	PreparedStatement ps = conn.prepareStatement("select id from SIMULATION where name = ?");
 	    	ps.setString(1, simulation);
 	    	ResultSet rs = ps.executeQuery();
 	    	if(rs.next()){
 	    		simID = rs.getInt(1);
 	    	}
 	    	
 	    	rs.close();
 	    	
 	    	if(simID <= 0) return;
 	    	
 	    	ps = conn.prepareStatement("delete from SIMULATIONSTATION where simulationid = ?");
 	    	ps.setInt(1, simID);
 	    	ps.executeUpdate();
 	    	
 	    	ps = conn.prepareStatement("delete from SIMULATION where id = ?");
 	    	ps.setInt(1, simID);
 	    	ps.executeUpdate();
 	    	
 	    	ps.close();
 	    	
 	    }
 	    
 	    public void deleteUser(EAVDAMUser user) throws Exception{
 	    	if(user == null) return;
 	    	
 	    	System.out.println("Deleting user "+user.getOrganizationName());
 	    	
 	    	PreparedStatement ps = null;
 	    	
 	    	ps = conn.prepareStatement("select ORGANIZATION.id from ORGANIZATION, ADDRESS where (ORGANIZATION.visitingaddress = ADDRESS.id OR ORGANIZATION.postaladdress = ADDRESS.id) AND (organizationname = ? OR addressline1 = ?)");
 	    	ps.setString(1, user.getOrganizationName());
 	    	ps.setString(2, user.getPostalAddress() != null ? user.getPostalAddress().getAddressline1() : "NO ADDRESS GIVEN FOR DELETION");
 	    	ResultSet rs = ps.executeQuery();
 	    	int id = -1;
 	    	if(rs.next()){
 	    		id = rs.getInt(1);
 	    	}
 	    	
 	    	if(id <= 0) return;
 	    	
	    	  
 	    	List<AISFixedStationData> stations = this.retrieveAISStations(-1, id);
 	    	for(AISFixedStationData s : stations){
 	    		this.deleteStation(s.getStationDBID());
 	    	}
	    	 
 	    	ps = conn.prepareStatement("delete from ORGANIZATION where id = ?");
 	    	ps.setInt(1, id);
 	    	int n = ps.executeUpdate();
 	    	if(n <= 0){
 	    		System.err.println("Delete organization "+user.getOrganizationName()+" ("+id+") FAILED!");
 	    	}
 	    	
 	    }
 	    
 	    /**
 	     * Deletes the give station from the database.
 	     * 
 	     * @param stationID
 	     */
 		public void deleteStation(int stationID) throws Exception{
 	    	if(this.conn == null) this.getDBConnection(null, false);
 	    	
 	    	PreparedStatement ps = null;
 	    	
 	    	if(stationID <= 0) return;
 	    	
 	    	ps = conn.prepareStatement("delete from COVERAGEPOINTS where station = ?");
 	    	ps.setInt(1, stationID);
 	    	ps.executeUpdate();
 	    	
 	    	ps = conn.prepareStatement("delete from SIMULATIONSTATION where stationid = ?");
 	    	ps.setInt(1, stationID);
 	    	ps.executeUpdate();
 
 
 	    	ps = conn.prepareStatement("select id from FATDMACHANNEL where station = ?");
 	    	ps.setInt(1, stationID);
 	    	ResultSet rs = ps.executeQuery();
 	    	while(rs.next()){
 	    		int channelID = rs.getInt(1);
 	    		
 //	    		System.out.println("Found "+channelID+" id for channel (station: "+stationID+")");
 	    		
 		    	PreparedStatement p = conn.prepareStatement("delete from FATDMAATON where channel = ?");
 		    	p.setInt(1, channelID);
 		    	int n = p.executeUpdate();
 		    	
 //		    	System.out.println(n+" broadcasts deleted...");
 		    	
 		    	p = conn.prepareStatement("delete from FATDMABASE where channel = ?");
 		    	p.setInt(1, channelID);
 		    	n = p.executeUpdate();
 	    		
 //		    	System.out.println(n+" base broadcasts deleted. Deleting channels for station "+stationID);
 //		    	
 //		    	p = conn.prepareStatement("select id, channel from FATDMABASE");
 //		    	ResultSet r = p.executeQuery();
 //		    	while(r.next()){
 //		    		System.out.println(r.getInt(1)+" | "+r.getInt(2));
 //		    	}
 		    	
 	    	}
 
 	    	ps = conn.prepareStatement("delete from FATDMACHANNEL where station = ?");
 	    	ps.setInt(1, stationID);
 	    	ps.executeUpdate();
 
 	    	ps = conn.prepareStatement("delete from STATUS where station = ?");
 	    	ps.setInt(1, stationID);
 	    	ps.executeUpdate();
 	    	
 
 	    	ps = conn.prepareStatement("delete from ANTENNA where station = ?");
 	    	ps.setInt(1, stationID);
 	    	ps.executeUpdate();
 	    	
 	    	ps = conn.prepareStatement("delete from FIXEDSTATION where id = ?");
 	    	ps.setInt(1, stationID);
 	    	ps.executeUpdate();
 	    	
 	    	ps.close();
 	    	
 			
 		}
 	    
 	    /**
 	     * Updates the organization database. Also updates the person database if necessary and insert a new address, if necessary.
 	     * 
 	     * @param user
 	     * @throws Exception
 	     */
 	    private void updateOrganization(EAVDAMUser user) throws Exception{
 	    	if(this.conn == null) this.getDBConnection(null, false);
 	    	
 	    	int cp = this.getPersonID(user.getContact());
 	    	int tp = this.getPersonID(user.getTechnicalContact());
 	    	int va = this.getAddressID(user.getVisitingAddress());
 	    	int pa = this.getAddressID(user.getPostalAddress());
 	    	
 	    	String sql = "update ORGANIZATION set " +
 	    			"ORGANIZATIONNAME = ?, " + //1.
 	    			"COUNTRYID = ?," +  //2.
 	    			"PHONE = ?," +  //3.
 	    			"FAX = ?," +  //4.
 	    			"WWW = ?," +  //5.
 	    			"DESCRIPTION = ?," +  //6.
 	    			"CONTACTPERSON = ?," +  //7.
 	    			"TECHNICALPERSON = ?," +  //8.
 	    			"VISITINGADDRESS = ?," +  //9.
 	    			"POSTALADDRESS = ?" +  //10.
 //	    			", DEFAULTUSER = ?" +  //11.
 	    			"where id = ?";  //12.
 	    	PreparedStatement ps = conn.prepareStatement(sql);
 	    	ps.setString(1, user.getOrganizationName());
 	    	ps.setString(2, user.getCountryID());
 	    	ps.setString(3, user.getPhone());
 	    	ps.setString(4, user.getFax());
 	    	ps.setString(5, (user.getWww() != null ? user.getWww().toString() : ""));
 	    	ps.setString(6, user.getDescription());
 	    	ps.setInt(7, cp);
 	    	ps.setInt(8,tp);
 	    	ps.setInt(9, va);
 	    	ps.setInt(10, pa);
 	    	ps.setInt(11,user.getUserDBID());
 	    		
 	    	ps.executeUpdate();
 
 	    	
 	    }
 	    
 	    /**
 	     * Updates the person table. Note that email-address is the only indicator of a person! If the email address changes, new person will be inserted! 
 	     * 
 	     * @param person
 	     * @throws Exception
 	     */
 	    public void updatePerson(Person person) throws Exception{
 	    	if(this.conn == null) this.getDBConnection(null, false);
 	    	
 	    	int va = this.getAddressID(person.getVisitingAddress());
 	    	int pa = this.getAddressID(person.getVisitingAddress());
 	    	
 	    	String sql = "update PERSON set " +
 	    			"NAME = ?, " + //1.
 	    			"PHONE = ?," +  //2.
 	    			"FAX = ?," +  //3.
 	    			"DESCRIPTION = ?," +  //4.
 	    			"VISITINGADDRESS = ?," +  //5.
 	    			"POSTALADDRESS = ?" +  //6.
 	    			"where EMAIL = ?";  //7.
 	    	PreparedStatement ps = conn.prepareStatement(sql);
 	    	ps.setString(1, person.getName());
 	    	ps.setString(2, person.getPhone());
 	    	ps.setString(3, person.getFax());
 	    	ps.setString(4, person.getDescription());
 	    	ps.setInt(5, va);
 	    	ps.setInt(6, pa);
 	    	ps.setString(7,person.getEmail());
 	    		
 	    	ps.executeUpdate();
 	    	
 	    	
 	    }
 	    
 	    /**
 	     * Updates the information about the given AISFixedStationData to the database. 
 	     * 
 	     * @param ais
 	     */
 	    public void updateAISStation(AISFixedStationData ais, int orgID, int refstation) throws Exception{
 	    	if(this.conn == null) this.getDBConnection(null, false);
 	    	
 	    	int owner = orgID; 
 	    	
 	    	int stationType = 1;
 	    	switch(ais.getStationType()){
 	    		case ATON: stationType = STATION_ATON; break;
 	    		case BASESTATION: stationType = STATION_BASE; break;
 	    		case RECEIVER: stationType = STATION_RECEIVER; break;
 	    		case REPEATER: stationType = STATION_REPEATER; break;
 	    	}
 	    	
 	    	System.out.println("UPDATING: orgID="+orgID+" stationID: "+ais.getStationDBID());
 	    	
 	    	String sql = "update FIXEDSTATION set " +
 	    			"NAME = ?, " + //1.
 	    			"OWNER = ?," +  //2.
 	    			"MMSI = ?," +  //3.
 	    			"LAT = ?," +  //4.
 	    			"LON = ?," +  //5.
 	    			"TRANSMISSIONPOWER = ?," +  //6.
 	    			"DESCRIPTION = ?," +  //7.
 	    			"STATIONTYPE = ?," +  //8.
 	    			"ANYVALUE = ?," +  //9.
 	    			"PROPOSEE = ?" +  //10.
 	    			" where ID = ?";  //11.
 	    	PreparedStatement ps = conn.prepareStatement(sql);
 	    	ps.setString(1, ais.getStationName());
 	    	ps.setInt(2, owner);
 	    	ps.setString(3, ais.getMmsi());
 	    	ps.setDouble(4, ais.getLat());
 	    	ps.setDouble(5, ais.getLon());
 	    	ps.setDouble(6, (ais.getTransmissionPower() != null ? ais.getTransmissionPower() : 0));
 	    	ps.setString(7, ais.getDescription());
 	    	ps.setInt(8, stationType);
 	    	ps.setString(9, ais.getAnythingString());
 	    	ps.setInt(10, ais.getProposee());
 	    	ps.setInt(11, ais.getStationDBID());
 	    	
 	    	ps.executeUpdate();
 	    	ps.close();
 	    	
 	    	if(ais.getAntenna() != null) this.updateAntenna(ais.getAntenna(), ais.getStationDBID());
 	    	
 	    	if(ais.getStatus() != null) this.updateStatus(ais.getStatus(), ais.getStationDBID(), refstation);
 	    	
 	    	if(ais.getTransmissionCoverage() != null){
 	    		this.updateCoverage(ais.getTransmissionCoverage(), ais.getStationDBID(), COVERAGE_TRANSMIT);
 	    	}
 	    	
 	    	if(ais.getReceiveCoverage() != null){
 	    		this.updateCoverage(ais.getReceiveCoverage(), ais.getStationDBID(), COVERAGE_RECEIVE);
 	    	}
 	    	
 	    	if(ais.getInterferenceCoverage() != null){
 	    		this.updateCoverage(ais.getInterferenceCoverage(), ais.getStationDBID(), COVERAGE_INTERFERENCE);
 	    	}
 	    	
 	    	if(ais.getFATDMAChannelA() != null){
 	    		this.updateFATDMAChannel(ais.getFATDMAChannelA(), ais.getStationDBID(), FATDMA_CHANNEL_A);
 	    	}
 	    	if(ais.getFATDMAChannelB() != null){
 	    		this.updateFATDMAChannel(ais.getFATDMAChannelB(), ais.getStationDBID(), FATDMA_CHANNEL_B);
 	    	}
 	    } 
 	    
 	    private void updateCoverage(AISFixedStationCoverage coverage, int stationDBID, int coverageType) throws Exception{
 
 	    	//Check if the coverage can be found
 	    	String sql = "delete from COVERAGEPOINTS where station = ? and coverageType = ?";
 	    	PreparedStatement ps = conn.prepareStatement(sql);
 	    	ps.setInt(1, stationDBID);
 	    	ps.setInt(2, coverageType);
 	    	ps.executeUpdate();
 
     		this.insertCoverage(coverage, stationDBID, coverageType);
 			
 		}
 
 	    private void updateFATDMAChannel(FATDMAChannel f, int stationDBID, int channelType) throws Exception{
 
 	    	System.out.println("Updating FATDMA Allocations");
 	    	
 	    		if(f.getDBID() <= 0){
 	    			this.insertFATDMAAllocations(f, stationDBID, channelType);
 	    		}else{
 	    		
 			    	PreparedStatement p = conn.prepareStatement("update FATDMACHANNEL set name = ? where id = ?");
 			    	p.setString(1, f.getChannelName());
 			    	p.setInt(2, f.getDBID());
 			    	p.executeUpdate();
 		    		
 		    		
 		    		if(f instanceof AISAtonStationFATDMAChannel){
 		    			AISAtonStationFATDMAChannel aton = (AISAtonStationFATDMAChannel)f;
 
 	    	    		//Delete the old ones
 	    		    	PreparedStatement delete = conn.prepareStatement("delete from FATDMAATON where channel = "+f.getDBID());
 	    		    	delete.executeUpdate();
 		    			
 		    			for(AtonMessageBroadcastRate r : aton.getAtonMessageBroadcastList()){
 		    				if(r.getDbID() != null && r.getDbID().intValue() > 0 && false){
 		    					//Update
 		    					this.updateAtonMessageBroadcastRate(r);
 		    				}else{
 			    				
 		    					this.insertAtonMessageBroadcastRate(r, f.getDBID());
 		    				}
 		    			}
 		    		}else{
 		    			AISBaseAndReceiverStationFATDMAChannel base = (AISBaseAndReceiverStationFATDMAChannel)f;
 
 	    	    		//Delete the old ones
 	    		    	PreparedStatement delete = conn.prepareStatement("delete from FATDMABASE where channel = "+f.getDBID());
 	    		    	delete.executeUpdate();
 		    			
 		    			for(FATDMAReservation r : base.getFATDMAScheme()){
 		    				if(r.getDbID() != null && r.getDbID().intValue() > 0 && false){
 		    					//Update
 		    					this.updateFATDMAReservation(r);
 		    				}else{
 		    					
 		    					//Insert
 		    					this.insertFATDMAReservation(r, f.getDBID());
 		    				}
 		    			}
 		    		}
 		    	}
 	    	
 			
 		}
 	    
 	    private void updateAtonMessageBroadcastRate(AtonMessageBroadcastRate r) throws Exception{
 	    	//Update
 	    	String sql = "update FATDMAATON set " 
 	    			+ "MESSAGEID = ?,"
 					+ "ACCESSSCHEME = ?,"
 					+ "UTCHOUR = ?,"
 					+ "UTCMINUTE = ?,"
 					+ "STARTSLOT = ?,"
 					+ "BLOCKSIZE = ?,"
 					+ "INCREMENT = ?,"
 					+ "USAGE = ?"
 	    			+ " where id = ?";
 	    	PreparedStatement ps = conn.prepareStatement(sql);
 	    	ps.setInt(1, r.getMessageID());
 	    	ps.setString(2, r.getAccessScheme());
 	    	ps.setInt(3, r.getUTCHour());
 	    	ps.setInt(4, r.getUTCMinute());
 	    	ps.setInt(5, r.getStartslot());
 	    	ps.setInt(6, r.getBlockSize());
 	    	ps.setInt(7, r.getIncrement());
 	    	ps.setString(8, r.getUsage());
 	    	ps.setInt(9, r.getDbID());
 	    	ps.executeUpdate();
 	    }
 	    
 	    private void updateFATDMAReservation(FATDMAReservation r) throws Exception{
 	    	String sql = "update FATDMABASE set " 
 					+ "STARTSLOT = ?,"
 					+ "BLOCKSIZE = ?,"
 					+ "INCREMENT = ?,"
 					+ "OWNERSHIP = ?,"
 					+ "USAGE = ?"
 	    			+ " where id = ?";
 	    	PreparedStatement ps = conn.prepareStatement(sql);
 	    	ps.setInt(1, r.getStartslot());
 	    	ps.setInt(2, r.getBlockSize());
 	    	ps.setInt(3, r.getIncrement());
 	    	ps.setString(4, r.getOwnership());
 	    	ps.setString(5, r.getUsage());
 	    	ps.setInt(6, r.getDbID());
 	    	ps.executeUpdate();
 	    }
 	    
 		private void updateAntenna(Antenna antenna, int stationId) throws Exception{
 	    	
 	    	
 	    	int antennaType = 1;
 	    	switch(antenna.getAntennaType()){
 	    		case DIRECTIONAL: antennaType = ANTENNA_DIRECTIONAL; break;
 	    		case OMNIDIRECTIONAL: antennaType = ANTENNA_OMNIDIR; break;
 	    	
 	    	}
 	    	
 	    	//Check if the antenna exists
 	    	String find = "select * from ANTENNA where station = ?";
 
 	    	PreparedStatement update = conn.prepareStatement(find);
 	    	update.setInt(1, stationId);
 	    	
 	    	ResultSet res = update.executeQuery();
 	    	if(!res.next()){
 	    		this.insertAntenna(antenna, stationId, antennaType);
 	    		return;
 	    	}
 
 	    	
 	    	String sql = "update ANTENNA set " +
 	    			"ANTENNAHEIGHT = ?, " + //1.
 	    			"TERRAINHEIGHT = ?," +  //2.
 	    			"ANTENNAHEADING = ?," +  //3.
 	    			"FIELDOFVIEWANGLE = ?," +  //4.
 	    			"GAIN = ?," +  //5.
 	    			"ANTENNATYPE = ?" +  //6.
 	    			" where STATION = ?";  //7.
 	    	PreparedStatement ps = conn.prepareStatement(sql);
 	    	ps.setDouble(1, antenna.getAntennaHeight());
 	    	ps.setDouble(2, antenna.getTerrainHeight());
 	    	ps.setInt(3, (antenna.getHeading() != null ? antenna.getHeading() : 0));
 	    	ps.setInt(4, (antenna.getFieldOfViewAngle() != null ? antenna.getFieldOfViewAngle() : 0));
 	    	ps.setDouble(5, (antenna.getGain() != null ? antenna.getGain() : 0));
 	    	ps.setInt(6, antennaType);
 	    	ps.setInt(7, stationId);
 	    	
 	    	ps.executeUpdate();
 	    	ps.close();
 	    	
 	    	
 	    }
 	    
 	    private void updateStatus(AISFixedStationStatus status, int stationId, int refstation) throws Exception{
 	    	
 	    	String sql = "update STATUS set " +
 	    			"STATUSTYPE = ?, " + //1.
 	    			"STARTDATE = ?," +  //2.
 	    			"ENDDATE = ?," +  //3.
 	    			"REFSTATION = ?" +  //4.
 	    			" where STATION = ?";  //5.
 	    	PreparedStatement ps = conn.prepareStatement(sql);
 	    	ps.setInt(1, status.getStatusID());
 	    	ps.setDate(2, status.getStartDate());
 	    	ps.setDate(3, status.getEndDate());
 	    	ps.setInt(4, refstation);
 	    	ps.setInt(5, stationId);
 	    	
 	    	int n = ps.executeUpdate();
 	    	System.out.println("Updated the status: "+status.getStatusID()+" (n:"+n+", id: "+stationId+")");
 	    	
 	    	ps.close();
 	    	
 	    }
 	    
 	    /**
 	     * Retrieves all the data from the database.
 	     * 
 	     * This includes ...
 	     * 
 	     * @return
 	     */
 	    public EAVDAMData retrieveAllEAVDAMData(EAVDAMUser defaultUser) throws Exception{
 	    	
 	    	
 	    	EAVDAMData data = new EAVDAMData();
 	    	
 	    	System.out.println("Retrieving all EAVDAMData");
 	    	
 	    	if(this.conn == null) this.conn = this.getDBConnection(null, false);
 	    	
 	    	//Get user information
 	    	////Includes Address and Person 
 	    	
 	    	List<EAVDAMUser> users = this.retrieveAllEAVDAMUsers();
 
 	    	if(users != null && users.size() > 0 && defaultUser != null)
 	    	for(EAVDAMUser u : users){
 	    		if(u == null) continue;
 
 	    		if(u.getUserDBID() == defaultUser.getUserDBID()){
 	    			System.out.println("Retrieving data for default user "+u.getOrganizationName());
 	    			
 	    			data.setUser(u);
 	    			
 	    			//Gets both active, proposed (to this user) and planned stations...
 	    			data.setActiveStations(this.retrieveEAVDAMData(u.getUserDBID(),STATUS_ACTIVE).getActiveStations());
 	    			data.setOldStations(this.retrieveEAVDAMData(u.getUserDBID(), STATUS_OLD).getOldStations());
 	    			
 	    			//TODO Simulation
 	    			data.setSimulatedStations(this.retrieveEAVDAMData(u.getUserDBID(), STATUS_SIMULATED).getSimulatedStations());
 	    			
 	    			
 	    			
 	    		}else{
 	    			System.out.println("Retrieving data for \"other user\" "+u.getOrganizationName());
 	    			//Get "other user data"
 	    			OtherUserStations o = new OtherUserStations();
 	    			EAVDAMData others = this.retrieveEAVDAMData(u.getUserDBID(), STATUS_ACTIVE);
 	    			o.setUser(u);
 	    			o.setStations(others.getActiveStations());
 	    			
 //	    			System.out.println("There are "+o.getStations().size()+" stations: "+(o.getStations().size() > 0 ? o.getStations().get(0).getStations().get(0).getStationName() : " "));
 	    			
 	    			//TODO What about proposals to this user?
 	    			EAVDAMData prop = this.retrieveEAVDAMData(u.getUserDBID(), STATUS_PROPOSED);
 	    			if(prop != null)
 	    				o.getStations().addAll(prop.getActiveStations());
 	    			
 	    			List<OtherUserStations> lo = data.getOtherUsersStations();
 	    			if(lo == null) lo = new ArrayList<OtherUserStations>();
 	    			
 	    			if(o != null && o.getStations() != null && o.getStations().size() > 0){
 	    				lo.add(o);
 	    			}
 	    			
 	    			data.setOtherUsersStations(lo);
 	    		}
 	    		
 	    	}
 	    	
 	    	
 	    	//Get station information
 	    	
 	    	//Get antenna information
 	    	
 	    	//Get status information
 	    	
 	    	//
 	    	
 	    	
 	    	return data;
 	    }
 	    
 	    
 	    /**
 	     * Gets all the users (Organizations) from the database.
 	     * 
 	     * @return List of all organizations.
 	     * 
 	     * @throws Exception
 	     */
 	    public List<EAVDAMUser> retrieveAllEAVDAMUsers() throws Exception{
 	    	ArrayList<EAVDAMUser> users = new ArrayList<EAVDAMUser>();
 	    	PreparedStatement ps = conn.prepareStatement("select id from ORGANIZATION");
 	    	
 	    	ResultSet rs = ps.executeQuery(); 
 	    	ArrayList<Integer> ids = new ArrayList<Integer>();
 	    	while(rs.next()){
 	    		ids.add(new Integer(rs.getInt(1)));
 	    	}
 	    	rs.close();
 	    	ps.close();
 	    	
 	    	for(Integer id : ids){
 	    		users.add(this.retrieveEAVDAMUser(id.intValue()+""));
 	    	}
 	    	
 	    	return users;
 	    	
 	    }
 	    
 	    /**
 	     * Retrieves all the relevant information about the user (= organization). This includes Contact and Technical Persons and Addresses.
 	     * 
 	     * Note: If several organization of the same name/id can be found, the first one is returned.
 	     * 
 	     * @param user ID or the name of the <b>Organization</b>.
 	     * @return Returns the EAVDAMUser object that contains all the information found.
 	     * @throws Exception
 	     */
 	    public EAVDAMUser retrieveEAVDAMUser(String user) throws Exception{
 	    	if(this.conn == null) this.createDatabase(null);
 	    	
 	    	EAVDAMUser u = new EAVDAMUser();
 	    	PreparedStatement ps = conn.prepareStatement("select * from ORGANIZATION where id=? OR organizationname=?");
 	    	ps.setString(1, user);
 	    	ps.setString(2, user);
 	    	
 	    	ResultSet res = ps.executeQuery();
 	    	
 	    	int postalAddressId = -1, visitingAddressId = -1, technicalPersonId = -1, contactPersonId = -1;
 	    	int ith = 0;
 	    	while(res.next()){
 	    		++ith;
 	    		if(ith > 1){
 	    			System.out.println("There are more than one user with the id/name "+user+". Returning only the first one...");
 	    			break;
 	    		}
 	    		u.setUserDBID(res.getInt(1));
 	    		u.setOrganizationName(res.getString(2));
 	    		u.setCountryID(res.getString(3));
 	    		u.setPhone(res.getString(4));
 	    		u.setFax(res.getString(5));
 	    		
 	    		u.setWww((res.getString(6) != null && res.getString(6).length() > 0 ? new URL((res.getString(6).startsWith("http://") ? res.getString(6) : "http://"+res.getString(6))) : null));
 	    		
 	    		
 	    		u.setDescription(res.getString(7));
 	    		
 	    		
 	    		postalAddressId = res.getInt(11);
 	    		visitingAddressId = res.getInt(10);
 	    		technicalPersonId = res.getInt(9);
 	    		contactPersonId = res.getInt(8);
 	    		
 	    		
 	    	}
 	    	
 	    	res.close();
 	    	ps.close();
 	    	
 	    	//Get the address(es)	    	
 	    	if(postalAddressId > 0){
 	    		u.setPostalAddress(this.retrieveAddress(postalAddressId));
 	    	}
 	    	
 	    	if(postalAddressId != visitingAddressId){
 	    		if(visitingAddressId > 0)
 	    			u.setVisitingAddress(this.retrieveAddress(visitingAddressId));
 
 	    	}else{
 	    		u.setVisitingAddress(u.getPostalAddress());
 	    	}
 	    	
 	    	
 	    	//Get the person information.
 	    	if(technicalPersonId > 0){
 	    		u.setTechnicalContact(this.retrievePerson(technicalPersonId));
 	    	}
 	    	
 	    	if(technicalPersonId != contactPersonId){
 	    		if(contactPersonId > 0)
 	    			u.setContact(this.retrievePerson(contactPersonId));
 	    	}else{
 	    		u.setContact(u.getTechnicalContact());
 	    	}
     	
 	    	return u;
 	    }
 	    
 	    /**
 	     * Gets the default user from the database. Default user is the organization where the application is installed. If no default user exist, null is returned.
 	     * 
 	     * In that case, the GUI should ask for the default user and it should be added as such. This should only be relevant in the first run of the application.
 	     * 
 	     * @return The default user.
 	     * @throws Exception
 	     */
 	    public EAVDAMUser retrieveDefaultUser() throws Exception{
 	    	int userID = 0;
 	    	
 	    	if(this.conn == null) this.getDBConnection(defaultDB, false);
 	    	
 	    	
 	    	PreparedStatement ps = conn.prepareStatement("select id from ORGANIZATION where defaultuser = 1");
 	    	ResultSet rs = ps.executeQuery();
 	    	if(rs.next()){
 	    		userID = rs.getInt(1);
 	    	}
 	    	if(userID <= 0) return null;
 	    	
 	    	return this.retrieveEAVDAMUser(userID+"");
 	    }
 	    
 	    /**
 	     * Retrieves the EAVDAMData only for the given organization.
 	     * 
 	     * @param user Either database userID (int) OR organization name. 
 	     * @return
 	     */
 	    public EAVDAMData retrieveEAVDAMData(int user, int status) throws Exception{
 //	    	System.out.println("Retrieving EAVDAMData for user "+user+" with status "+status);
 	    	EAVDAMData data = new EAVDAMData();
 	    	EAVDAMUser u = this.retrieveEAVDAMUser(user+"");
 	    	
 	    	if(u == null) return null;
 	    	
 	    	data.setUser(u);
 
 	    	
 	    	if(status <= 0){
 	    	
 		    	//Get the active stations.
 		    	List<AISFixedStationData> activeStations = this.retrieveAISStations(STATUS_ACTIVE, u.getUserDBID());
 		    	//Proposed stations...
 		    	List<AISFixedStationData> proposedStations = this.retrieveAISStations(STATUS_PROPOSED, u.getUserDBID());
 	
 		    	for(AISFixedStationData prop : proposedStations){
 			    	//Transform this list to ActiveStation list...
 			    	ActiveStation as = new ActiveStation();
 			    	as.setStations(activeStations);
 			    	HashMap<EAVDAMUser, AISFixedStationData> proposal = new HashMap<EAVDAMUser, AISFixedStationData>();
 			    	proposal.put(u, prop);
 			    	as.setProposals(proposal);
 			    	
 			    	List<ActiveStation> a = data.getActiveStations();
 			    	if(a == null) a = new ArrayList<ActiveStation>();
 			    	
 			    	a.add(as);
 			    	data.setActiveStations(a);
 		    	}
 		    	
 		    	List<AISFixedStationData> oldStations = this.retrieveAISStations(STATUS_OLD, u.getUserDBID());
 		    	data.setOldStations(oldStations);
 		    	
 		    	
 		    	List<AISFixedStationData> otherUserStations = this.retrieveAISStations(STATUS_ACTIVE, -1);
 		    	List<OtherUserStations> other = new ArrayList<OtherUserStations>();
 		    	OtherUserStations o = new OtherUserStations();
 		    	
 		    	
 		    	data.setOtherUsersStations(other);
 
 		    	
 		    	data.setSimulatedStations(null);
 		    	
 	    	}else{
 	    		if(status == STATUS_ACTIVE){
 
 	    			data.setActiveStations(this.retrieveActiveStations(user));
 	    		}else if(status == STATUS_OLD){
 	    			List<AISFixedStationData> oldStations = this.retrieveAISStations(STATUS_OLD, u.getUserDBID());
 	    			data.setOldStations(oldStations);
 	    			
 	    		}else if(status == STATUS_PROPOSED){
 	    			
 	    		}else if(status == STATUS_PLANNED){
 	    		
 	    			List<AISFixedStationData> plannedStations = this.retrieveAISStations(STATUS_PLANNED, u.getUserDBID());
 	    			ActiveStation as = new ActiveStation();
 	    			as.setStations(plannedStations);
 	    			
 	    			List<ActiveStation> al = new ArrayList<ActiveStation>();
 	    			al.add(as);
 	    			data.setActiveStations(al);
 	    			
 	    		}else if(status == STATUS_SIMULATED){
 	    			List<Simulation> sims = this.retrieveSimulations(u);
 	    			data.setSimulatedStations(sims);
 	    		}
 	    		
 	    	}
 	    	
 	    	
 	    	return data;
 	    }
 	    
 	    private List<ActiveStation> retrieveActiveStations(int user) throws Exception{
 			List<ActiveStation> activeStations = new ArrayList<ActiveStation>();
 	    	if(user < 0) return null;
 	    	
 	    	Set<String> plannedIDs = new HashSet<String>();
 	    	
 	    	List<AISFixedStationData> active = this.retrieveAISStations(STATUS_ACTIVE, user);
 	    	if(active != null && active.size() > 0){
 				for(AISFixedStationData a : active){
 					ActiveStation act = new ActiveStation();
 					List<AISFixedStationData> list = new ArrayList<AISFixedStationData>();
 					list.add(a);
 					
 					List<AISFixedStationData> planned = this.retrieveAISStation(a.getStationDBID(), STATUS_PLANNED, user);
 					
 					//Store the ids of the planned stations linked to an active station
 					if(planned != null && planned.size() > 0){
 						for(AISFixedStationData p : planned){
 							plannedIDs.add(p.getStationDBID()+"");
 						}
 						
 						list.addAll(planned);
 					}
 					
 					
 					act.setStations(list);
 					activeStations.add(act);
 				}	
 	    	}
 	    	
 	    	//Retrieve planned stations that have no link to an active station 
 	    	List<AISFixedStationData> planned = this.retrieveAISStations(STATUS_PLANNED, user);
 	    	for(AISFixedStationData d : planned){
 	    		if(!plannedIDs.contains(d.getStationDBID()+"")){
 	    			ActiveStation a = new ActiveStation();
 	    			List<AISFixedStationData> list = new ArrayList<AISFixedStationData>();
 	    			list.add(d);
 	    			a.setStations(list);
 	    			activeStations.add(a);
 	    		}
 	    			
 	    	}
 	    	
 	    	
 	    	return activeStations;
 	    }
 	    
 	    /**
 	     * Retrieves all the AISFixedStations of the given status (DerbyDBInterface.STATUS_) and user. 
 	     * 
 	     * If statusID is < 0, all of the stations are retrieved. If userID is < 0, stations are retrieved for all users. 
 	     * 
 	     * @param statusID
 	     * @param userID 
 	     * @return
 	     */
 	    public List<AISFixedStationData> retrieveAISStations(int statusID, int userID) throws Exception{
 	    	ArrayList<AISFixedStationData> data = new ArrayList<AISFixedStationData>();
 	    	
 	    	String sql = "select " +
 	    			"FIXEDSTATION.id, " +  //1.
 	    			"FIXEDSTATION.name, " +  //2.
 	    			"FIXEDSTATION.owner, " +  //3.
 	    			"FIXEDSTATION.mmsi, " + //4.
 	    			"FIXEDSTATION.lat, " +  //5.
 	    			"FIXEDSTATION.lon, " +  //6.
 	    			"FIXEDSTATION.transmissionpower, " +  //7.
 	    			"FIXEDSTATION.description, " + //8.
 	    			"FIXEDSTATION.stationtype, "+  //9.
 	    			"FIXEDSTATION.anyvalue, "+  //10.
 	    			"FIXEDSTATION.proposee, "+  //11.
 	    			"STATUS.statustype, "+   //12.
 	    			"STATUS.startdate, "+   //13.
 	    			"STATUS.enddate, "+  //14.
 	    			"STATUS.refstation,"+ //15
 	    			"FIXEDSTATION.stationtype " +  //16.
 	    			"from FIXEDSTATION, STATUS " +
 	    			"where FIXEDSTATION.ID = STATUS.STATION";
 	    	String whereClause = "";
 	    	if(statusID > 0){
 	    		whereClause += " AND STATUS.statustype="+statusID;
 	    	}
 	    	
 	    	HashMap<String, EAVDAMUser> users = new HashMap<String, EAVDAMUser>();
 	    	if(userID >= 0){
 	    		whereClause += " AND FIXEDSTATION.owner="+userID;
 	    		
 	    		users.put(userID+"", this.retrieveEAVDAMUser(userID+""));
 	    		
 	    	}else{
 	    		for(EAVDAMUser u : this.retrieveAllEAVDAMUsers()){
 	    			users.put(u.getUserDBID()+"", u);
 	    		}
 	    	}
 	    	
 	    	PreparedStatement ps = conn.prepareStatement(sql+whereClause);
 	    	
 	    	ResultSet rs = ps.executeQuery();
 
 	    	while(rs.next()){
 	    		AISFixedStationData ais = new AISFixedStationData();
 	    		ais.setStationDBID(rs.getInt(1));
 	    		ais.setStationName(rs.getString(2));
 	    		ais.setMmsi(rs.getString(4));
 	    		ais.setLat(rs.getDouble(5));
 	    		ais.setLon(rs.getDouble(6));
 	    		ais.setTransmissionPower(rs.getDouble(7));
 	    		ais.setDescription(rs.getString(8));
 	    		ais.setProposee(rs.getInt(11));
 	    		
 	    		//Get the owner?
 	    		ais.setOperator(users.get(rs.getInt(3)+""));
 	    		
 	    		ais.setRefStationID(rs.getInt(15));
 
 	    		
 	    		AISFixedStationStatus status = new AISFixedStationStatus();
 	    		status.setStartDate(rs.getDate(13));
 	    		status.setEndDate(rs.getDate(14));
 	    		status.setStatusID(rs.getInt(12));
 	    		ais.setStatus(status);
 	    		
 	    		int stationType = rs.getInt(16);
 	    		if(stationType == STATION_ATON){
 	    			ais.setStationType(AISFixedStationType.ATON);
 	    		}else if(stationType == STATION_BASE){
 	    			ais.setStationType(AISFixedStationType.BASESTATION);
 	    		} else if(stationType == STATION_RECEIVER){
 	    			ais.setStationType(AISFixedStationType.RECEIVER);
 	    		} else if(stationType == STATION_REPEATER){
 	    			ais.setStationType(AISFixedStationType.REPEATER);
 	    		} else{
 	    			ais.setStationType(null);
 	    		}
 	    		
 	    		ais.setFATDMAChannelA(this.retrieveFATDMAAllocations(ais.getStationDBID(), FATDMA_CHANNEL_A));
 	    		ais.setFATDMAChannelB(this.retrieveFATDMAAllocations(ais.getStationDBID(), FATDMA_CHANNEL_B));
 	    		
 //	    		System.out.println(ais.getFATDMAChannels().size()+" --> "+(ais.getFATDMAChannels().size() > 0 ? ais.getFATDMAChannels().get(0).getChannelName()+"" : "") );
 	    		
 	    		//TODO Get this data also...
 	    		int stationID = ais.getStationDBID();
 	    		ais.setTransmissionCoverage(this.retrieveCoverageArea(stationID,COVERAGE_TRANSMIT));
 	    		ais.setReceiveCoverage(this.retrieveCoverageArea(stationID, COVERAGE_RECEIVE));
 	    		ais.setInterferenceCoverage(this.retrieveCoverageArea(stationID, COVERAGE_INTERFERENCE));
 	    		
 	    		
 	    		
 	    		ais.setAnything(null);
 	    		
 	    		data.add(ais);
 	    	}
 	    	
 	    	//Get the antenna information
 	    	for(AISFixedStationData ais : data){
 		    	sql = "select " +
 		    			"ANTENNA.antennaheight, " +  //1.
 		    			"ANTENNA.terrainheight, " +  //2.
 		    			"ANTENNA.antennaheading, " +  //3.
 		    			"ANTENNA.fieldofviewangle, " +  //4.
 		    			"ANTENNA.gain, " +  //5.
 		    			"ANTENNA.antennatype " +  //6.
 		    			"from ANTENNA " +
 		    			"where ANTENNA.station = ?";
 		    	
 		    	
 		    	ps = conn.prepareStatement(sql);
 		    	ps.setInt(1, ais.getStationDBID());
 		    	
 		    	rs.close();
 		    	rs = ps.executeQuery();
 		    	while(rs.next()){
 		    		Antenna antenna = new Antenna();
 		    		antenna.setAntennaHeight(rs.getDouble(1));
 		    		if(rs.getInt(6) == ANTENNA_DIRECTIONAL){
 		    			antenna.setAntennaType(AntennaType.DIRECTIONAL);
 		    		}else{
 		    			antenna.setAntennaType(AntennaType.OMNIDIRECTIONAL);
 		    		}
 		    		antenna.setFieldOfViewAngle(rs.getInt(4));
 		    		antenna.setGain(rs.getDouble(5));
 		    		antenna.setHeading(3);
 		    		antenna.setTerrainHeight(rs.getDouble(2));
 		    		ais.setAntenna(antenna);
 		    	}
 	    	}
 	    	
 	    	rs.close();
 	    	ps.close();
 	    	
 	    	return data;
 	    }
 	    
 
 	    /**
 	     * Retrieves a specific station.
 	     * 
 	     * NOTE! If status == STATUS_PLANNED, the stationID should indicate the reference station to which the planned station maps to!
 	     * 
 	     * @param stationID
 	     * @param statusID
 	     * @param userID
 	     * @return
 	     * @throws Exception
 	     */
 	    private List<AISFixedStationData> retrieveAISStation(int stationID, int statusID, int userID) throws Exception{
 	    	ArrayList<AISFixedStationData> data = new ArrayList<AISFixedStationData>();
 	    	
 	    	System.out.println("Retrieving planned station linked to station "+stationID);
 	    	
 	    	String sql = "select " +
 	    			"FIXEDSTATION.id, " +  //1.
 	    			"FIXEDSTATION.name, " +  //2.
 	    			"FIXEDSTATION.owner, " +  //3.
 	    			"FIXEDSTATION.mmsi, " + //4.
 	    			"FIXEDSTATION.lat, " +  //5.
 	    			"FIXEDSTATION.lon, " +  //6.
 	    			"FIXEDSTATION.transmissionpower, " +  //7.
 	    			"FIXEDSTATION.description, " + //8.
 	    			"FIXEDSTATION.stationtype, "+  //9.
 	    			"FIXEDSTATION.anyvalue, "+  //10.
 	    			"FIXEDSTATION.proposee, "+  //11.
 	    			"STATUS.statustype, "+   //12.
 	    			"STATUS.startdate, "+   //13.
 	    			"STATUS.enddate, "+  //14.
 	    			"FIXEDSTATION.stationtype " +  //15.
 	    			"from FIXEDSTATION, STATUS " +
 	    			"where FIXEDSTATION.ID = STATUS.STATION";
 	    	String whereClause = "";
 	    	if(statusID > 0){
 	    		whereClause += " AND STATUS.statustype="+statusID;
 	    		
 	    	}
 	    	
 	    	HashMap<String, EAVDAMUser> users = new HashMap<String, EAVDAMUser>();
 	    	if(userID >= 0){
 	    		whereClause += " AND FIXEDSTATION.owner="+userID;
 	    		
 	    		users.put(userID+"", this.retrieveEAVDAMUser(userID+""));
 	    		
 	    	}else{
 	    		for(EAVDAMUser u : this.retrieveAllEAVDAMUsers()){
 	    			users.put(u.getUserDBID()+"", u);
 	    		}
 	    	}
 	    	
 	    	if(stationID >= 0){
 	    		if(statusID == STATUS_PLANNED){
 	    			whereClause += " AND STATUS.REFSTATION = "+stationID;
 	    		}else{
 	    			whereClause += " AND FIXEDSTATION.ID = "+stationID;
 	    		}
 	    	}
 	    	
 	    	PreparedStatement ps = conn.prepareStatement(sql+whereClause);
 	    	
 	    	ResultSet rs = ps.executeQuery();
 
 	    	while(rs.next()){
 	    		AISFixedStationData ais = new AISFixedStationData();
 	    		ais.setStationDBID(rs.getInt(1));
 	    		ais.setStationName(rs.getString(2));
 	    		ais.setMmsi(rs.getString(4));
 	    		ais.setLat(rs.getDouble(5));
 	    		ais.setLon(rs.getDouble(6));
 	    		ais.setTransmissionPower(rs.getDouble(7));
 	    		ais.setDescription(rs.getString(8));
 	    		ais.setProposee(rs.getInt(11));
 	    		
 	    		//Get the owner?
 	    		ais.setOperator(users.get(rs.getInt(3)+""));
 
 
 	    		
 	    		AISFixedStationStatus status = new AISFixedStationStatus();
 	    		status.setStartDate(rs.getDate(13));
 	    		status.setEndDate(rs.getDate(14));
 	    		status.setStatusID(rs.getInt(12));
 	    		ais.setStatus(status);
 	    		
 	    		int stationType = rs.getInt(15);
 	    		if(stationType == STATION_ATON){
 	    			ais.setStationType(AISFixedStationType.ATON);
 	    		}else if(stationType == STATION_BASE){
 	    			ais.setStationType(AISFixedStationType.BASESTATION);
 	    		} else if(stationType == STATION_RECEIVER){
 	    			ais.setStationType(AISFixedStationType.RECEIVER);
 	    		} else if(stationType == STATION_REPEATER){
 	    			ais.setStationType(AISFixedStationType.REPEATER);
 	    		} else{
 	    			ais.setStationType(null);
 	    		}
 	    		
 	    		ais.setTransmissionCoverage(this.retrieveCoverageArea(stationID,COVERAGE_TRANSMIT));
 	    		ais.setReceiveCoverage(this.retrieveCoverageArea(stationID, COVERAGE_RECEIVE));
 	    		ais.setInterferenceCoverage(this.retrieveCoverageArea(stationID, COVERAGE_INTERFERENCE));
 	    		
 	    		ais.setFATDMAChannelA(this.retrieveFATDMAAllocations(ais.getStationDBID(), FATDMA_CHANNEL_A));
 	    		ais.setFATDMAChannelB(this.retrieveFATDMAAllocations(ais.getStationDBID(), FATDMA_CHANNEL_B));
 	    		
 	    		ais.setAnything(null);
 	    		
 	    		data.add(ais);
 	    	}
 	    	
 	    	//Get the antenna information
 	    	for(AISFixedStationData ais : data){
 		    	sql = "select " +
 		    			"ANTENNA.antennaheight, " +  //1.
 		    			"ANTENNA.terrainheight, " +  //2.
 		    			"ANTENNA.antennaheading, " +  //3.
 		    			"ANTENNA.fieldofviewangle, " +  //4.
 		    			"ANTENNA.gain, " +  //5.
 		    			"ANTENNA.antennatype " +  //6.
 		    			"from ANTENNA " +
 		    			"where ANTENNA.station = ?";
 		    	
 		    	
 		    	ps = conn.prepareStatement(sql);
 		    	ps.setInt(1, ais.getStationDBID());
 		    	
 		    	rs.close();
 		    	rs = ps.executeQuery();
 		    	while(rs.next()){
 		    		Antenna antenna = new Antenna();
 		    		antenna.setAntennaHeight(rs.getDouble(1));
 		    		if(rs.getInt(6) == ANTENNA_DIRECTIONAL){
 		    			antenna.setAntennaType(AntennaType.DIRECTIONAL);
 		    		}else{
 		    			antenna.setAntennaType(AntennaType.OMNIDIRECTIONAL);
 		    		}
 		    		antenna.setFieldOfViewAngle(rs.getInt(4));
 		    		antenna.setGain(rs.getDouble(5));
 		    		antenna.setHeading(3);
 		    		antenna.setTerrainHeight(rs.getDouble(2));
 		    		ais.setAntenna(antenna);
 		    	}
 	    	}
 	    	
 	    	rs.close();
 	    	ps.close();
 	    	
 	    	return data;
 	    }
 	    
 	    private AISFixedStationCoverage retrieveCoverageArea(int stationID, int coverageType) throws Exception{
 	    	AISFixedStationCoverage c = new AISFixedStationCoverage();
 	    	
 	    	String sql = "select lat, lon, orderline from COVERAGEPOINTS where station = ? AND coverageType = ? order by orderline ASC";
 	    	PreparedStatement ps = conn.prepareStatement(sql);
 	    	ps.setInt(1, stationID);
 	     	ps.setInt(2, coverageType);
 	     	
 	    	ResultSet rs = ps.executeQuery();
 	    	while(rs.next()){
 	    		double[] point = new double[2];
 	    		point[0] = rs.getDouble(1);
 	    		point[1] = rs.getDouble(2);
 	    		
 	    		c.addCoveragePoint(point[0], point[1]);
 	    	}
 	    	
 	    	
 	    	return c;
 		}
 
 		/**
 	     * Retrieves all the simulations found from the database.
 	     * 
 	     * 
 	     * @return
 	     * @throws Exception
 	     */
 	    public List<Simulation> retrieveSimulations(EAVDAMUser user) throws Exception{
 	    	List<Simulation> simulations = new ArrayList<Simulation>();
 	    	
 	    	
 	    	if(this.conn == null) this.getDBConnection(null, false);
 	    	 
 	    	PreparedStatement ps = conn.prepareStatement("select name from SIMULATION");
 	    	ResultSet rs = ps.executeQuery();
 	    	while(rs.next()){
 	    		Simulation sim = new Simulation();
 	    		sim.setName(rs.getString(1));
 	    		
 	    		simulations.add(sim);
 	    	}
 	    		
 	    	ps.close();
 	    	rs.close();
 //	    	if(simulations.size() == 0) return null;
 	    	
 	    	for(Simulation sim : simulations){
 	    		ps = conn.prepareStatement("select SIMULATIONSTATION.stationid FROM SIMULATIONSTATION, SIMULATION where SIMULATION.id = SIMULATIONSTATION.simulationid AND SIMULATION.name = ?");
 	    		ps.setString(1, sim.getName());
 	    		rs = ps.executeQuery();
 	    		List<Integer> stationIDs = new ArrayList<Integer>();
 	    		while(rs.next()){
 	    			stationIDs.add(new Integer(rs.getInt(1)));
 	    		}
 	    		
 	    		rs.close();
 	    		ps.close();
 	    		
 	    		List<AISFixedStationData> stations = new ArrayList<AISFixedStationData>();
 	    		for(Integer id : stationIDs){
 	    			stations.addAll(this.retrieveAISStation(id.intValue(), STATUS_SIMULATED, user.getUserDBID())); 
 	    		}
 	    		sim.setStations(stations);
 	    	
 	    	}
 	    	
 	    	
 	    	return simulations;
 	    }
 	    
 	    
 	    /**
 	     * Transforms the list of proposed stations into Map of proposed stations
 	     * 
 	     * @return
 	     */
 	    public Map<EAVDAMUser, List<AISFixedStationData>> transformToProposals(List<AISFixedStationData> proposals) throws Exception{
 	    	Map<EAVDAMUser, List<AISFixedStationData>> map = new HashMap<EAVDAMUser, List<AISFixedStationData>>();
 	    	
 	    	Map<String, EAVDAMUser> usrBuffer = new HashMap<String, EAVDAMUser>();
 	    	for(AISFixedStationData ais : proposals){
 	    		
 	    		EAVDAMUser user = usrBuffer.get(ais.getProposee()+"");
 	    		if(user == null){
 	    			user = this.retrieveEAVDAMUser(ais.getProposee()+"");
 	    			usrBuffer.put(ais.getProposee()+"", user);
 	    		}
 	    		
 	    		List<AISFixedStationData> list = map.get(user);
 	    		if(list == null) list = new ArrayList<AISFixedStationData>();
 	    		
 	    		list.add(ais);
 	    		
 	    		map.put(user, list);
 	    		
 	    	}
 	    	
 	    	
 	    	return map;
 	    }
 	    
 	    public Address retrieveAddress(int id) throws Exception{
 	    	Address a = new Address();
 	    	PreparedStatement ps = conn.prepareStatement("select * from ADDRESS where id=?");
 	    	ps.setInt(1, id);
 	    	
 	    	ResultSet rs = ps.executeQuery();
 	    	while(rs.next()){
 	    		
 	    		a.setAddressline1(rs.getString(2));
 	    		a.setAddressline2(rs.getString(3));
 	    		a.setZip(rs.getString(4));
 	    		a.setCity(rs.getString(5));
 	    		a.setCountry(rs.getString(6));
 
 	    		break;
 	    	}
 	    	
 	    	rs.close();
 	    	ps.close();
 	    	
 	    	return a;
 	    }
 	    
 	    public Person retrievePerson(int id) throws Exception{
 	    	Person p = new Person();
 	    	PreparedStatement ps = conn.prepareStatement("select * from Person where id=?");
 	    	ps.setInt(1, id);
 	    	
 	    	ResultSet rs = ps.executeQuery();
 	    	int postalAddressID = -1, visitingAddressID = -1;
 	    	while(rs.next()){
 	    		
 	    		p.setName(rs.getString(2));
 	    		p.setEmail(rs.getString(3));
 	    		p.setPhone(rs.getString(4));
 	    		p.setFax(rs.getString(5));
 	    		p.setDescription(rs.getString(6));
 	    		
 	    		postalAddressID = rs.getInt(8);
 	    		visitingAddressID = rs.getInt(7);
 
 	    		break;
 	    	}
 	    	
 	    	rs.close();
 	    	ps.close();
 	    	
 	    	if(postalAddressID >= 0){
 	    		p.setPostalAddress(this.retrieveAddress(postalAddressID));
 	    	}
 	    	
 	    	if(postalAddressID != visitingAddressID){
 	    		if(visitingAddressID >= 0){
 	    			p.setVisitingAddress(this.retrieveAddress(visitingAddressID));
 	    		}
 	    	}else{
 	    		p.setVisitingAddress(p.getPostalAddress());
 	    	}
 	    	
 	    	return p;
 	    }
 	    
 	    /**
 	     * Retrieves the data that will be stored in an xml-file. 
 	     * 
 	     * This includes the active stations (STATUS_ACTIVE), and proposed stations (STATUS_PROPOSED).
 	     * The proposed stations are the stations that this user has proposed to another user.
 	     * 
 	     * @return
 	     */
 	    public EAVDAMData retrieveEAVDAMDataForXML(){
 	    	try{
 	    		EAVDAMData data = new EAVDAMData();
 	    		
 	    		EAVDAMUser defaultUser = this.retrieveDefaultUser();
 	    		data.setUser(defaultUser);
 	    		
 	    		List<AISFixedStationData> activeStations = this.retrieveAISStations(STATUS_ACTIVE, defaultUser.getUserDBID());
 	    		
 	    		List<AISFixedStationData> proposals = this.retrieveAISStations(STATUS_PROPOSED, defaultUser.getUserDBID());
 	    		activeStations.addAll(proposals);
 	    		data.setStations(activeStations);
 	    		
 	    		
 	    		return data;
 	    	}catch(Exception e){
 	    		e.printStackTrace();
 	    	}
 	    	
 	    	return null;
 	    }
 	    
 	    /**
 	     * Loads the appropriate JDBC driver for this environment/framework. For
 	     * example, if we are in an embedded environment, we load Derby's
 	     * embedded Driver, <code>org.apache.derby.jdbc.EmbeddedDriver</code>.
 	     */
 	    private void loadDriver() {
 	        /*
 	         *  The JDBC driver is loaded by loading its class.
 	         *  If you are using JDBC 4.0 (Java SE 6) or newer, JDBC drivers may
 	         *  be automatically loaded, making this code optional.
 	         *
 	         *  In an embedded environment, this will also start up the Derby
 	         *  engine (though not any databases), since it is not already
 	         *  running. In a client environment, the Derby engine is being run
 	         *  by the network server framework.
 	         *
 	         *  In an embedded environment, any static Derby system properties
 	         *  must be set before loading the driver to take effect.
 	         */
 	        try {
 	            Class.forName(driver).newInstance();
 	            System.out.println("Loaded the appropriate driver");
 	        } catch (ClassNotFoundException cnfe) {
 	            System.err.println("\nUnable to load the JDBC driver " + driver);
 	            System.err.println("Please check your CLASSPATH.");
 	            cnfe.printStackTrace(System.err);
 	        } catch (InstantiationException ie) {
 	            System.err.println(
 	                        "\nUnable to instantiate the JDBC driver " + driver);
 	            ie.printStackTrace(System.err);
 	        } catch (IllegalAccessException iae) {
 	            System.err.println(
 	                        "\nNot allowed to access the JDBC driver " + driver);
 	            iae.printStackTrace(System.err);
 	        }
 	    }
 
 	    /**
 	     * Reports a data verification failure to System.err with the given message.
 	     *
 	     * @param message A message describing what failed.
 	     */
 	    private void reportFailure(String message) {
 	        System.err.println("\nData verification failed:");
 	        System.err.println('\t' + message);
 	    }
 
 	    /**
 	     * Prints details of an SQLException chain to <code>System.err</code>.
 	     * Details included are SQL State, Error code, Exception message.
 	     *
 	     * @param e the SQLException from which to print details.
 	     */
 	    public static void printSQLException(SQLException e)
 	    {
 	        // Unwraps the entire exception chain to unveil the real cause of the
 	        // Exception.
 	        while (e != null)
 	        {
 	            System.err.println("\n----- SQLException -----");
 	            System.err.println("  SQL State:  " + e.getSQLState());
 	            System.err.println("  Error Code: " + e.getErrorCode());
 	            System.err.println("  Message:    " + e.getMessage());
 	            // for stack traces, refer to derby.log or uncomment this:
 	            //e.printStackTrace(System.err);
 	            e = e.getNextException();
 	        }
 	    }
 
 	    /**
 	     * Parses the arguments given and sets the values of this class' instance
 	     * variables accordingly - that is which framework to use, the name of the
 	     * JDBC driver class, and which connection protocol protocol to use. The
 	     * protocol should be used as part of the JDBC URL when connecting to Derby.
 	     * <p>
 	     * If the argument is "embedded" or invalid, this method will not change
 	     * anything, meaning that the default values will be used.</p>
 	     * <p>
 	     * @param args JDBC connection framework, either "embedded", "derbyclient".
 	     * Only the first argument will be considered, the rest will be ignored.
 	     */
 	    private void parseArguments(String[] args)
 	    {
 	        if (args.length > 0) {
 	            if (args[0].equalsIgnoreCase("derbyclient"))
 	            {
 	                framework = "derbyclient";
 	                driver = "org.apache.derby.jdbc.ClientDriver";
 	                protocol = "jdbc:derby://localhost:1527/";
 	            }
 	        }
 	    }
 	    
 	    public void createDatabase(String dbName){
 	    	boolean log = false;
 	    	
 	    	try{
 	    		if(this.conn == null) this.getDBConnection(null, true);
 	    	
 	    		Statement s = conn.createStatement();
 
 	    		
 	    		//First, drop the tables (if they exist)
 				
 				
 				try {
 					s.execute("DROP TABLE ANTENNA");
 				} catch (Exception e) {
 					if(log)
 						e.printStackTrace();
 				}
 	    		
 				try {
 					s.execute("DROP TABLE FATDMAATON");
 				} catch (Exception e) {
 					if(log)
 						e.printStackTrace();
 				}
 				
 				try {
 					s.execute("DROP TABLE FATDMABASE");
 				} catch (Exception e) {
 					if(log)
 						e.printStackTrace();
 				}
 				
 	    		try {
 					s.execute("DROP TABLE COVERAGEPOINTS");
 				} catch (Exception e) {
 					if(log)
 						e.printStackTrace();
 				}
 								
 				try {
 					s.execute("DROP TABLE STATUS");
 				} catch (Exception e) {
 					if(log)
 						e.printStackTrace();
 				}
 	    		
 				
 				
 				try {
 					s.execute("DROP TABLE SIMULATIONSTATION");
 				} catch (Exception e) {
 					if(log)
 						e.printStackTrace();
 				}
 				
 				try {
 					s.execute("DROP TABLE FIXEDSTATION");
 				} catch (Exception e) {
 					if(log)
 						e.printStackTrace();
 				}
 				
 	    		try{
 	    			s.execute("DROP TABLE ORGANIZATION");
 	    		}catch(Exception e){
 	    			if(log)
 						e.printStackTrace();
 	    		}
 								
 				try {
 					s.execute("DROP TABLE PERSON");
 				} catch (Exception e) {
 					if(log)
 						e.printStackTrace();
 				}
 				
 				
 	    		try {
 					s.execute("DROP TABLE ADDRESS");
 				} catch (Exception e) {
 					if(log)
 						e.printStackTrace();
 				}
 
 
 				
 	    		try {
 					s.execute("DROP TABLE ANTENNATYPE");
 				} catch (Exception e) {
 					if(log)
 						e.printStackTrace();
 				}
 				
 				try {
 					s.execute("DROP TABLE STATIONTYPE");
 				} catch (Exception e) {
 					if(log)
 						e.printStackTrace();
 				}
 				
 				try {
 					s.execute("DROP TABLE STATUSTYPE");
 				} catch (Exception e) {
 					if(log)
 						e.printStackTrace();
 				}
 				
 				try {
 					s.execute("DROP TABLE SENDTOFTP");
 				} catch (Exception e) {
 					if(log)
 						e.printStackTrace();
 				}
 				
 				try {
 					s.execute("DROP TABLE SENDTOEMAIL");
 				} catch (Exception e) {
 					if(log)
 						e.printStackTrace();
 				}
 
 				
 				try {
 					s.execute("DROP TABLE SIMULATION");
 				} catch (Exception e) {
 					if(log)
 						e.printStackTrace();
 				}
 
 
 				//Then create the tables.
 				try {
 					s.execute("CREATE TABLE ADDRESS" 
 							+ "(ID INT PRIMARY KEY,"
 							+ "ADDRESSLINE1 VARCHAR(200),"
 							+ "ADDRESSLINE2 VARCHAR(200),"
 							+ "ZIPCODE VARCHAR(10)," + "CITY VARCHAR(50),"
 							+ "COUNTRY VARCHAR(50))");
 				} catch (Exception e) {
 					// TODO: handle exception
 					if(log)
 						e.printStackTrace();
 				}
 				
 			
 				try {
 					s.execute("CREATE TABLE PERSON"
 							+ "(ID INT PRIMARY KEY,"
 							+ "NAME VARCHAR(150) NOT NULL,"
 							+ "EMAIL VARCHAR(150) NOT NULL,"
 							+ "PHONE VARCHAR(25),"
 							+ "FAX VARCHAR(25),"
 							+ "DESCRIPTION VARCHAR(1000),"
 							+ "VISITINGADDRESS INT,"
 							+ "POSTALADDRESS INT,"
 							+ "CONSTRAINT fk_va_p FOREIGN KEY (VISITINGADDRESS) references ADDRESS(ID),"
 							+ "CONSTRAINT fk_pa_p FOREIGN KEY (POSTALADDRESS) references ADDRESS(ID))");
 				} catch (Exception e) {
 					if(log)
 						e.printStackTrace();
 				}
 	    		
 	    		try {
 					s.execute("CREATE TABLE ORGANIZATION"
 							+ "(ID INT PRIMARY KEY,"
 							+ "ORGANIZATIONNAME VARCHAR(100) NOT NULL,"
 							+ "COUNTRYID VARCHAR(3) NOT NULL,"
 							+ "PHONE VARCHAR(25),"
 							+ "FAX VARCHAR(25),"
 							+ "WWW VARCHAR(50),"
 							+ "DESCRIPTION VARCHAR(1000),"
 							+ "CONTACTPERSON INT,"
 							+ "TECHNICALPERSON INT,"
 							+ "VISITINGADDRESS INT,"
 							+ "POSTALADDRESS INT,"
 							+ "DEFAULTUSER INT,"
 							+ "CONSTRAINT fk_cp_o FOREIGN KEY (CONTACTPERSON) references PERSON(ID),"
 							+ "CONSTRAINT fk_tp_o FOREIGN KEY (TECHNICALPERSON) references PERSON(ID),"
 							+ "CONSTRAINT fk_va_o FOREIGN KEY (VISITINGADDRESS) references ADDRESS(ID),"
 							+ "CONSTRAINT fk_pa_o FOREIGN KEY (POSTALADDRESS) references ADDRESS(ID))");
 				} catch (Exception e) {
 					if(log)
 						e.printStackTrace();
 				}
 				
 				
 				try {
 					s.execute("CREATE TABLE ANTENNATYPE"
 							+ "(ID INT PRIMARY KEY," + "NAME VARCHAR(50))");
 				} catch (Exception e) {
 					if(log)
 						e.printStackTrace();
 				}
 				
 
 
 				try {
 					s.execute("CREATE TABLE STATIONTYPE"
 							+ "(ID INT PRIMARY KEY,"
 							+ "NAME VARCHAR(50) NOT NULL)");
 				} catch (Exception e) {
 					if(log)
 						e.printStackTrace();
 				}
 				
 
 				
 				try {
 					s.execute("CREATE TABLE STATUSTYPE" 
 							+ "(ID INT PRIMARY KEY,"
 							+ "NAME VARCHAR(50))");
 				} catch (Exception e) {
 					if(log)
 						e.printStackTrace();
 				}
 
 
 				
 				try {
 					s.execute("CREATE TABLE FIXEDSTATION"
 							+ "(ID INT PRIMARY KEY,"
 							+ "NAME VARCHAR(120),"
 							+ "OWNER INT,"
 							+ "MMSI VARCHAR(10),"
 							+ "LAT DECIMAL (10,5),"
 							+ "LON DECIMAL (10,5),"
 							+ "TRANSMISSIONPOWER DECIMAL (10,5),"
 							+ "DESCRIPTION VARCHAR(1000),"
 							+ "STATIONTYPE INT,"
 							+ "ANYVALUE VARCHAR(2000),"
 							+ "PROPOSEE INT,"
 							+ "CONSTRAINT fk_prop_fs FOREIGN KEY (PROPOSEE) references ORGANIZATION(ID),"
 							+ "CONSTRAINT fk_o_fs FOREIGN KEY (OWNER) references ORGANIZATION(ID),"
 							+ "CONSTRAINT fk_st_fs FOREIGN KEY (STATIONTYPE) references STATIONTYPE(ID))");
 				} catch (Exception e) {
 					if(log)
 						e.printStackTrace();
 					
 				}
 				
 				try {
 					s.execute("CREATE TABLE STATUS"
 							+ "(STATION INT,"
 							+ "REFSTATION INT,"
 							+ "STATUSTYPE INT,"
 							+ "STARTDATE DATE,"
 							+ "ENDDATE DATE,"
 							+ "CONSTRAINT fk_fs_s FOREIGN KEY (STATION) references FIXEDSTATION(ID),"
 							+ "CONSTRAINT fk_st_s FOREIGN KEY (STATUSTYPE) references STATUSTYPE(ID))");
 				} catch (Exception e) {
 					if(log)
 						e.printStackTrace();
 				}
 				
 				try {
 					s.execute("CREATE TABLE ANTENNA"
 							+ "(STATION INT,"
 							+ "ANTENNAHEIGHT DECIMAL (10,5),"
 							+ "TERRAINHEIGHT DECIMAL (10,5),"
 							+ "ANTENNAHEADING INT,"
 							+ "FIELDOFVIEWANGLE DECIMAL (10,5),"
 							+ "GAIN DECIMAL (7,4),"
 							+ "ANTENNATYPE INT,"
 							+ "CONSTRAINT fk_s_a FOREIGN KEY (STATION) references FIXEDSTATION(ID))");
 				} catch (Exception e) {
 					if(log)
 						e.printStackTrace();
 				}
 				
 				try {
 					s.execute("CREATE TABLE COVERAGEPOINTS"
 							+ "(STATION INT,"
 							+ "LAT DECIMAL (10,5),"
 							+ "LON DECIMAL (10,5),"
 							+ "ORDERLINE INT,"
 							+ "COVERAGETYPE INT,"
 //							+ "CONSTRAINT pk_cp PRIMARY KEY (STATION, LAT, LON),"
 							+ "CONSTRAINT fk_s_cp FOREIGN KEY (STATION) references FIXEDSTATION(ID))");
 				} catch (Exception e) {
 					if(log)
 						e.printStackTrace();
 				}
 
 				
 				try {
 					s.execute("CREATE TABLE FATDMACHANNEL"
 							+ "(ID INT," 
 							+ "STATION INT,"
 							+ "NAME VARCHAR(50),"
 							+ "TYPE INT,"
 							+ "CONSTRAINT pk_fatdmac_a PRIMARY KEY (ID), "
 							+ "CONSTRAINT fk_fatdma FOREIGN KEY (STATION) references FIXEDSTATION(ID))");
 				} catch (Exception e) {
 					if(log)
 						e.printStackTrace();
 				}
 				
 				try {
 					s.execute("CREATE TABLE FATDMAATON"
 							+ "(ID INT," 
 //							+ "STATION INT,"
 //							+ "NAME VARCHAR(50),"
 							+ "CHANNEL INT,"							
 							+ "ACCESSSCHEME VARCHAR(6),"
 							+ "MESSAGEID INT,"
 							+ "UTCHOUR INT,"
 							+ "UTCMINUTE INT,"
 							+ "STARTSLOT INT,"
 							+ "BLOCKSIZE INT,"
 							+ "INCREMENT INT,"
 							+ "USAGE VARCHAR(80),"
 							+ "CONSTRAINT pk_fatdma_a PRIMARY KEY (ID), "
 							+ "CONSTRAINT fk_a_fatdma FOREIGN KEY (CHANNEL) references FATDMACHANNEL(ID))");
 				} catch (Exception e) {
 					if(log)
 						e.printStackTrace();
 				}
 				
 				try {
 					s.execute("CREATE TABLE FATDMABASE"
 							+ "(ID INT,"
 //							+ "STATION INT,"
 //							+ "NAME VARCHAR(50),"
 							+ "CHANNEL INT,"
 							+ "STARTSLOT INT,"
 							+ "BLOCKSIZE INT,"
 							+ "INCREMENT INT,"
 							+ "OWNERSHIP VARCHAR(1),"
 							+ "USAGE VARCHAR(80),"
 							+ "CONSTRAINT pk_fatdma_b PRIMARY KEY (ID), "
 							+ "CONSTRAINT fk_b_fatdmab FOREIGN KEY (CHANNEL) references FATDMACHANNEL(ID))");
 				} catch (Exception e) {
 					if(log)
 						e.printStackTrace();
 				}
 				
 				try {
 					s.execute("CREATE TABLE SENDTOFTP"
 							+ "(SERVER VARCHAR(100),"
 							+ "DIRECTORY VARCHAR(150),"
 							+ "USERNAME VARCHAR(50),"
 							+ "PASSWORD VARCHAR(25))");
 				} catch (Exception e) {
 					if(log)
 						e.printStackTrace();
 				}
 
 				try {
 					s.execute("CREATE TABLE SENDTOEMAIL"
 							+ "(EMAILTO VARCHAR(100),"
 							+ "EMAILFROM VARCHAR(100),"
 							+ "SUBJECT VARCHAR(500),"
 							+ "SMTPSERVER VARCHAR(75)," 
 							+ "AUTHENTICATION INT," 
 							+ "USERNAME VARCHAR(50)," 
 							+ "PASSWORD VARCHAR(25))");
 				} catch (Exception e) {
 					if(log)
 						e.printStackTrace();
 				}
 				
 				try {
 					s.execute("CREATE TABLE SETTINGS"
 							+ "(ICONSIZE INT)");
 				} catch (Exception e) {
 					if(log)
 						e.printStackTrace();
 				}
 				
 				try {
 					s.execute("CREATE TABLE SIMULATION"
 							+ "(ID INT PRIMARY KEY, " 
 							+ "NAME VARCHAR(100))");
 				} catch (Exception e) {
 					if(log)
 						e.printStackTrace();
 				}
 				
 				try {
 					s.execute("CREATE TABLE SIMULATIONSTATION"
 							+ "(STATIONID INT, " 
 							+ "SIMULATIONID INT,"
 							+ "CONSTRAINT fk_sim_station FOREIGN KEY (STATIONID) references FIXEDSTATION(ID)," 
 							+ "CONSTRAINT fk_sim FOREIGN KEY (SIMULATIONID) references SIMULATION(ID))");
 				} catch (Exception e) {
 					if(log)
 						e.printStackTrace();
 				}
 				
 				//Insert the predefined data
 	    		s.execute("INSERT INTO ANTENNATYPE VALUES("+ANTENNA_DIRECTIONAL+", 'Directional Antenna')");
 	    		s.execute("INSERT INTO ANTENNATYPE VALUES("+ANTENNA_OMNIDIR+", 'Omnidirectional Antenna')");
 	    		
 	    		s.execute("INSERT INTO STATIONTYPE VALUES("+STATION_BASE+", 'Base station')");
 	    		s.execute("INSERT INTO STATIONTYPE VALUES("+STATION_REPEATER+", 'Repeater')");
 	    		s.execute("INSERT INTO STATIONTYPE VALUES("+STATION_RECEIVER+", 'Receiver')");
 	    		s.execute("INSERT INTO STATIONTYPE VALUES("+STATION_ATON+", 'ATON')");
 	    		
 	    		s.execute("INSERT INTO STATUSTYPE VALUES("+STATUS_ACTIVE+", 'Active')");
 	    		s.execute("INSERT INTO STATUSTYPE VALUES("+STATUS_OLD+", 'Old')");
 	    		s.execute("INSERT INTO STATUSTYPE VALUES("+STATUS_SIMULATED+", 'Simulated')");
 	    		s.execute("INSERT INTO STATUSTYPE VALUES("+STATUS_PROPOSED+", 'Proposed')");
 	    		s.execute("INSERT INTO STATUSTYPE VALUES("+STATUS_PLANNED+", 'Planned')");
 	    		
 	    		//Insert "unknown" values.
 	    		s.execute("INSERT INTO ANTENNATYPE VALUES(0, 'No antenna')");
 	    		s.execute("INSERT INTO ADDRESS VALUES(0,'','',null,null,null)");
 	    		s.execute("INSERT INTO PERSON VALUES(0,'Unknown','','','','',0,0)");
 	    		s.execute("INSERT INTO ORGANIZATION VALUES(0,'Unknown','NO','','','','',0,0,0,0,0)");
 	    		
 //	    		System.out.println("Inserting default user to the database for demonstration purposis. Default user name: DaMSA");
 //	    		s.execute("INSERT INTO ORGANIZATION VALUES(1,'DaMSA','DK','+45 3268 9677','+45 3257 4341','http://WWW.FRV.DK','Danish Maritime Safety Administration',0,0,0,0,1)");
 	    		conn.commit();
 	    		
 	    	 	 //test
 	    		 ResultSet rs = s.executeQuery("SELECT id, name FROM STATIONTYPE WHERE id=1");
 	    		 boolean failure = false;
 	             if (!rs.next())
 	             {
 	                 failure = true;
 	                 reportFailure("No rows in ResultSet");
 	             }
 
 
 	             int id;
 	             if ((id = rs.getInt(1)) != 1){
 	                 failure = true;
 	                 System.out.println("FAILURE IN DATABASE CREATION. ID TEST FAILED! Should be "+1+", is "+id);
 	             }
 
 	             String type;
 	             if(!(type = rs.getString(2)).equals("Base station")){
 	            	 failure = true;
 	                 System.out.println("FAILURE IN DATABASE CREATION. ID TEST FAILED! Should be 'Base station', is "+type);
 	             }
 	    		
 	    		if(!failure){
 	    			System.out.println("Database created successfully! Test result: id="+id+", type="+type+" (should be: id=1, type=Base station)");
 	    		}
 				
 	    		rs.close();
 	    		conn.close();
 	    	}catch(Exception e){
 	    		e.printStackTrace();
 	    	}
 	    }
 	    
 		private void updateDatabaseVersion(){
 			
 			//Version alpha: update table if the ftp-directory is too short...
 			try{
 				if(this.conn == null) this.conn = this.getDBConnection(null, true);
 				
 				PreparedStatement ps = this.conn.prepareStatement("insert into SENDTOFTP values ('DELETE','TOO LONG DIRECTORY !!!!!!!','none','none')");
 				ps.executeUpdate();
 				
 				ps = this.conn.prepareStatement("delete from SENDTOFTP where server = 'DELETE'");
 				ps.executeUpdate();
 				ps.close();
 			}catch(Exception e){
 				try{
 					System.out.print("Old version of the SENDTOFTP table. Updating it to alpha...");
 					PreparedStatement ps = this.conn.prepareStatement("alter table SENDTOFTP alter server set data type varchar(150)");
 					ps.executeUpdate();
 					
 					ps = this.conn.prepareStatement("alter table SENDTOFTP alter directory set data type varchar(100)");
 					ps.executeUpdate();
 					System.out.print(" ...Update complete.\n");
 					
 					ps.close();
 				}catch(Exception e2){
 					e2.printStackTrace();
 				}
 				
 			}
 			
 		}
 
 	    /**
 	     * Retrieves the options for FTP and email xml sending.
 	     * 
 	     * @return
 	     */
 		public Options getOptions() {
 			Options op = new Options();
 			
 			op.setFTPs(this.retrieveFTPSettings());
 			
 			try{
 				PreparedStatement ps = conn.prepareStatement("select EMAILTO, EMAILFROM, SUBJECT, SMTPSERVER, AUTHENTICATION, USERNAME, PASSWORD from SENDTOEMAIL");
 				ResultSet rs = ps.executeQuery();
 				if(rs.next()){
 					
 					op.setEmailAuth((rs.getInt(5) == 1 ? true : false));
 					op.setEmailFrom(rs.getString(2));
 					op.setEmailHost(rs.getString(4));
 					op.setEmailPassword(rs.getString(6));
 					op.setEmailSubject(rs.getString(3));
 					op.setEmailTo(rs.getString(1));
 					op.setEmailUsername(rs.getString(7));
 					
 				}
 				
 				rs.close();
 				ps.close();
 
 				ps = conn.prepareStatement("select iconsize from SETTINGS");
 				rs = ps.executeQuery();
 				if(rs.next()){
 					op.setIconsSize(rs.getInt(1));
 				}
 				
 				ps.close();
 				rs.close();
 				
 			}catch(Exception e){
 				e.printStackTrace();
 			}
 			
 			return op;
 		}
 
 		/**
 		 * Retrieves the FTP settings from the database.
 		 * 
 		 * @return
 		 */
 		private List<FTP> retrieveFTPSettings() {
 			try{
 				if(this.conn == null) this.getDBConnection(null, false);
 				
 				List<FTP> ftps = new ArrayList<FTP>();
 
 				PreparedStatement ps = conn.prepareStatement("select SERVER, DIRECTORY, USERNAME, PASSWORD from SENDTOFTP");
 				ResultSet rs = ps.executeQuery();
 				while(rs.next()){
 					FTP ftp = new FTP();
 					
 					ftp.setServer(rs.getString(1));
 					ftp.setDirectory(rs.getString(2));
 					ftp.setUsername(rs.getString(3));
 					ftp.setPassword(rs.getString(4));
 					
 					ftps.add(ftp);
 				}
 				
 				rs.close();
 				ps.close();
 				
 				return ftps;
 			}catch(Exception e){
 				
 			}
 			return null;
 		}
 	    
 		/**
 		 * Inserts the options to the database.
 		 * 
 		 * @param options
 		 */
 		public void insertOptions(Options options){
 			try{
 				if(options == null) return;
 				
 				if(this.conn == null) this.getDBConnection(null, false);
 				
 				PreparedStatement ps = conn.prepareStatement("delete from SENDTOEMAIL"); //Delete the old values...
 				ps.executeUpdate();
 				if(options.getEmailHost() != null && options.getEmailHost().length() > 0){
 
 					
 					ps = conn.prepareStatement("insert into SENDTOEMAIL values (?,?,?,?,?,?,?)");
 					ps.setString(1, options.getEmailTo());
 					ps.setString(2, options.getEmailFrom());
 					ps.setString(3, options.getEmailSubject());
 					ps.setString(4, options.getEmailHost());
 					ps.setInt(5, (options.isEmailAuth() ? 1 : 0));
 					ps.setString(6, options.getEmailUsername());
 					ps.setString(7, options.getEmailPassword());
 					
 					ps.executeUpdate();
 					
 					ps.close();
 				}
 				ps = conn.prepareStatement("delete from SETTINGS");
 				ps.executeUpdate();
 				
 				ps = conn.prepareStatement("insert into SETTINGS values (?)");
 				ps.setInt(1, options.getIconsSize());
 				ps.executeUpdate();
 				ps.close();
 				
 				this.insertFTPSettings(options.getFTPs());
 				
 			}catch(Exception e){
 				e.printStackTrace();
 			}
 			
 		}
 		
 		private void insertFTPSettings(List<FTP> ftps) throws Exception{
 			PreparedStatement del = conn.prepareStatement("delete from SENDTOFTP");
 			del.executeUpdate();
 			del.close();
 			
 			for(FTP f : ftps){
 				PreparedStatement ps = conn.prepareStatement("insert into SENDTOFTP values (?,?,?,?)");
 				ps.setString(1, f.getServer());
 				ps.setString(2, f.getDirectory());
 				ps.setString(3, f.getUsername());
 				ps.setString(4, f.getPassword());
 				ps.executeUpdate();
 				ps.close();
 			}
 		}
 		
 		
 		public static void dbTester(){
 			try{
 
 		    		
 		    		DerbyDBInterface db = new DerbyDBInterface();
 		    		Connection conn = db.getDBConnection(null, false);
 		    		EAVDAMUser def = db.retrieveDefaultUser();
 		    		System.out.println("Default user = "+def.getOrganizationName());
 		    		
 		    		String sql = "";
 
 		    		sql = "select FIXEDSTATION.id, FIXEDSTATION.name, STATUS.statustype, FIXEDSTATION.owner, FIXEDSTATION.lat, FIXEDSTATION.lon from FIXEDSTATION, STATUS where STATUS.station = FIXEDSTATION.id";
 //		    		sql = "select id, name, email from PERSON";
 		    		
 		    		
 		    		PreparedStatement ps = conn.prepareStatement(sql);
 		    		ResultSet rs = ps.executeQuery();
 		    		
 		    		System.out.println("Table holds:\nID\tName\tStatus\tOwner");
 		    		while(rs.next()){
 		    			System.out.println(rs.getInt(1)+"\t"+rs.getString(2)+"\t"+rs.getInt(3)+"\t"+rs.getInt(4)+"\t"+rs.getDouble(5)+"\t"+rs.getDouble(6));
 		    		}
 		    		rs.close();
 		    		ps.close();
 		    		System.out.println("\nOrganization table holds:");
 		    		sql = "select id, organizationname from ORGANIZATION";
 		    		ps = conn.prepareStatement(sql);
 		    		rs = ps.executeQuery();
 		    		
 		    		System.out.println("ID\tName");
 		    		while(rs.next()){
 		    			System.out.println(rs.getInt(1)+"\t"+rs.getString(2));
 		    		}
 		    		
 		    		sql = "select iconsize from SETTINGS";
 		    		ps = conn.prepareStatement(sql);
 		    		rs = ps.executeQuery();
 		    		
 		    		System.out.println("\nICONSIZE");
 		    		while(rs.next()){
 		    			System.out.println(rs.getInt(1));
 		    		}
 		    		
 		    		sql = "select id, name from SIMULATION";
 		    		ps = conn.prepareStatement(sql);
 		    		rs = ps.executeQuery();
 		    		
 		    		System.out.println("\nSIMULATION\nId\tName");
 		    		while(rs.next()){
 		    			System.out.println(rs.getInt(1)+"\t"+rs.getString(2));
 		    		}
 		    		
 		    		
 		    	}catch(Exception e){
 		    		e.printStackTrace();
 		    	}
 		}
 
 }
