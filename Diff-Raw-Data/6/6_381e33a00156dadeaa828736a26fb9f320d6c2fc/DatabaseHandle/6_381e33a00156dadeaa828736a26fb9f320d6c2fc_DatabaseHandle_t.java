 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.LinkedList;
 
 
 public class DatabaseHandle {
 	
 	private Connection con;
 	
 	private PreparedStatement vbc;
 	private PreparedStatement fa;
 	private PreparedStatement sf;
 	private PreparedStatement gac;
 	private PreparedStatement gan;
 	private PreparedStatement bookFlightChair;
 	private PreparedStatement createBooking;
 	private PreparedStatement getBookingID;
 	private PreparedStatement createAccount;
 	private PreparedStatement getAccountID;
 	private PreparedStatement getAvailableCars;
 	private PreparedStatement bookCar;
 	private PreparedStatement getDepots;
 	private PreparedStatement getAvailableHotels;
 	private PreparedStatement getRooms;
 	private PreparedStatement bookRoom;
 	private PreparedStatement getEventsByLocation;
 	private PreparedStatement getEventSpotsLeft;
 	private PreparedStatement getTotalSpots;
 	private PreparedStatement bookEvent;
 	private PreparedStatement logIn;
 	private PreparedStatement searchWithFilters;
 	
 	public DatabaseHandle(){
 		try
         {
             Class.forName("org.postgresql.Driver");
             con = DriverManager.getConnection (LoginData.URL, LoginData.USER, LoginData.PASSWORD);
             //Stöder full sökning av vaccinationsinformation samt landsinfo baserat på landsnamn.
             vbc = con.prepareStatement("SELECT Vaccination.VaccBeskrivning, Landsinfo FROM Vaccination NATURAL INNER JOIN VacciLand NATURAL INNER JOIN Landsinfo WHERE Landsinfo.Land = ?");
             fa = con.prepareStatement("SELECT Avgangsdatum, MIN(Kostnad) AS Pris FROM Flight NATURAL INNER JOIN Stol NATURAL INNER JOIN Ort WHERE Avgangsort = ? AND Ankomstort = ? AND Avgangsdatum BETWEEN ? AND ? GROUP BY Avgangsdatum");
             //Har stöd för Sökning av flyg enligt bollen. man söker på Utresedatum, Avgangsort och ankomstort. För att söka hemresa byter man plats på dessa fält.
             sf = con.prepareStatement("SELECT FlightID, Avgangstid, Ankomsttid, MIN(Kostnad) AS MinKostnad FROM Flight NATURAL INNER JOIN Flygplan NATURAL INNER JOIN Flygplanstyp WHERE Avgangsdatum = ? AND Avgangsort = ? AND Ankomstort = ? AND Platser > (SELECT COUNT(*) FROM Flightbokning WHERE Flightbokning.FlightID = Flight.FlightID) GROUP BY FlightID");
             //Variant med filter. Har atm stöd för sållning på pris.
             searchWithFilters = con.prepareStatement("SELECT FlightID, Avgangstid, Ankomsttid, MIN(Kostnad) AS MinKostnad FROM Flight NATURAL INNER JOIN Flygplan NATURAL INNER JOIN Flygplanstyp WHERE Avgangsdatum = ? AND Avgangsort = ? AND Ankomstort = ? AND Platser > (SELECT COUNT(*) FROM Flightbokning WHERE Flightbokning.FlightID = Flight.FlightID) AND MinPris > ? AND MinPris < ? GROUP BY FlightID");
             gac = con.prepareStatement("SELECT Typ, COUNT(*) AS Tillg FROM Stol NATURAL INNER JOIN Flight WHERE FlightID = ? AND NOT EXISTS (SELECT FlightID, Nummer FROM Flightbokning WHERE Flight.FlightID = Flightbokning.FlightID AND Stol.Nummer = Flightbokning.Nummer) GROUP BY Typ");
             gan = con.prepareStatement("SELECT Nummer FROM Stol NATURAL INNER JOIN Flight WHERE FlightID = ? AND Typ = ? AND NOT EXISTS (SELECT * FROM Flightbokning WHERE Flightbokning.Nummer = Stol.Nummer)");
             createBooking = con.prepareStatement("INSERT INTO TABLE Bokning VALUES (?)");
             getBookingID = con.prepareStatement("SELECT MAX(BokningsID) AS Max FROM Bokning WHERE KontoID = ?");
             bookFlightChair = con.prepareStatement("INSERT INTO TABLE Flightbokning VALUES (?, ?, ?)");
             createAccount = con.prepareStatement("INSERT INTO Konto VALUES (?, decode(md5(?), 'hex'), ?, ?, ?, ?, ?, ?, ?, ?)");
             getAccountID = con.prepareStatement("SELECT KontoID FROM Konto WHERE Login = ?");
             //Stöder sökning av bilar baserat på utlämningsort och de datum bilen skall bokas. Returnerar all relevant information för klassning.
             getAvailableCars = con.prepareStatement("SELECT BilID, Modell, Tillverkare, Drift, Vaxel, DepotID FROM Bil NATURAL INNER JOIN Bilmodell WHERE NOT EXISTS (SELECT * FROM Bilschema WHERE Bilschema.BilID = Bil.BilID AND (? > Hamtdatum AND ? < Lamningsdatum) AND (? > Hamtdatum AND ? < Lamningsdatum)) AND (SELECT OrtID FROM Ort WHERE Ort = ?) = (SELECT Lamningsplats FROM Bilschema WHERE Lamningsdatum = (SELECT MAX(Lamningsdatum) FROM Bilschema AS a WHERE a.BilID = Bilschema.BilID))");
             bookCar = con.prepareStatement("INSERT INTO Bilschema VALUES (?, ?, ?, ?, ?, ?)");
             getDepots = con.prepareStatement("SELECT DepotID FROm Depot NATURAL INNER JOIN Ort WHERE Ort = ?");
             //Hämtar alla hotell som har minst ett rum ledigt.
             getAvailableHotels = con.prepareStatement("SELECT HotellID, Namn, Ort, Stjarnor, Beskrivning, MIN(Kostnad) FROM Hotell NATURAL INNER JOIN Rum NATURAL INNER JOIN Ort WHERE Ort = ? AND EXISTS (SELECT RumsID FROM Rum WHERE Rum.HotellID = Hotell.HotellID MINUS SELECT RumsID FROM Rumsschema WHERE (? > Frandatum AND ? < Tilldatum) AND (? > Frandatum AND ? < Tilldatum))");
             //Hämtar alla rum vid ett givet hotell som är lediga under den angivna tiden. Kan kombineras med föregående query för att få stöd för bokning av flera rum.
             getRooms = con.prepareStatement("SELECT RumsID FROM Rum WHERE HotellID = ? EXCEPT SELECT RumsID FROM Rumsschema WHERE (? > Frandatum AND ? < Tilldatum) AND (? > Frandatum AND ? < Tilldatum)");
             bookRoom = con.prepareStatement("INSERT INTO Rumsschema VALUES (?,?,?,?)");
             //Stöder sökning av events baserat på ort samt en datumrange.
             getEventsByLocation = con.prepareStatement("SELECT EvenemangsID, Namn, Startdatum, Slutdatum, Starttid, Sluttid, Beskrivning, PlatsNamn, Kostnad FROM Evenemang NATURAL INNER JOIN Platser NATURAL INNER JOIN Ort WHERE Ort = ? AND Startdatum BETWEEN ? AND ? AND AntalPlatser > (SELECT SUM(Antal) FROM Evenemangschema WHERE Evenemang.EvenemangsID=Evenemangsschema.EvenemangsID)");
             getEventSpotsLeft = con.prepareStatement("SELECT SUM(Antal) AS Sum FROM Evenemangschema WHERE EvenemangsID = ?");
             getTotalSpots = con.prepareStatement("Select Antalplatser FROM Evenemang WHERE EvenemangsID = ?");
             bookEvent = con.prepareStatement("INSERT INTO Evenemangsschema VALUES (?, ?, ?)");
             logIn = con.prepareStatement("SELECT KontoID, BokningsID FROM Konto NATURAL INNER JOIN Bokning WHERE Login=? AND Password=decode(md5(?), 'hex');");
             
         }
         catch (Exception e)
         {
             e.printStackTrace();
         }
 	}
 	
	public ResultSet logIn(String user, String pass) throws SQLException{
 		logIn.setString(1, user);
 		logIn.setString(1, pass);
 		ResultSet rs = logIn.executeQuery();
 		if(!rs.next()) return null;
		rs.previous();
		return rs;
 	}
 	
 	public void bookEvent(int evenemangsID, long bokningsID, int antal) throws SQLException{
 		bookEvent.setInt(1, evenemangsID);
 		bookEvent.setLong(2, bokningsID);
 		bookEvent.setInt(3, antal);
 		bookEvent.executeUpdate();
 		
 	}
 	
 	public int spotsLeft(int eventID) throws SQLException{
 		getEventSpotsLeft.setInt(1, eventID);
 		ResultSet rs = getEventSpotsLeft.executeQuery();
 		rs.next();
 		int spotsTaken = rs.getInt("Sum");
 		getTotalSpots.setInt(1, eventID);
 		rs = getTotalSpots.executeQuery();
 		rs.next();
 		return rs.getInt("Antalplatser") - spotsTaken;
 		
 	}
 	
 	public ResultSet getEventsByLocation(String ort, Date startdatum, Date slutdatum) throws SQLException{
 		getEventsByLocation.setString(1, ort);
 		getEventsByLocation.setDate(2, new java.sql.Date(startdatum.getTime()));
 		getEventsByLocation.setDate(2, new java.sql.Date(slutdatum.getTime()));
 		return getEventsByLocation.executeQuery();
 	}
 	
 	public void bookRoom(long bokningsID, int hotellID, Date fran, Date till) throws SQLException{
 		getRooms.setInt(1, hotellID);
 		getRooms.setDate(2, new java.sql.Date(fran.getTime()));
 		getRooms.setDate(3, new java.sql.Date(fran.getTime()));
 		getRooms.setDate(4, new java.sql.Date(till.getTime()));
 		getRooms.setDate(5, new java.sql.Date(till.getTime()));
 		ResultSet rs = getRooms.executeQuery();
 		rs.next();
 		bookRoom.setLong(1, bokningsID);
 		bookRoom.setInt(2, rs.getInt("RumsID"));
 		bookRoom.setDate(3, new java.sql.Date(fran.getTime()));
 		bookRoom.setDate(4, new java.sql.Date(till.getTime()));
 		bookRoom.executeUpdate();
 	}
 	
 	public ResultSet getAvailableHotels(String ort, Date fran, Date till) throws SQLException{
 		getAvailableHotels.setString(1, ort);
 		getAvailableHotels.setDate(2, new java.sql.Date(fran.getTime()));
 		getAvailableHotels.setDate(3, new java.sql.Date(fran.getTime()));
 		getAvailableHotels.setDate(4, new java.sql.Date(till.getTime()));
 		getAvailableHotels.setDate(5, new java.sql.Date(till.getTime()));
 		return getAvailableHotels.executeQuery();
 	}
 	
 	public void bookCar(int bilID, long bokningsID, Date hamtdatum, Date lamningsdatum, int hamtort, String lamningsort) throws SQLException{
 		getDepots.setString(1, lamningsort);
 		ResultSet rs = getDepots.executeQuery();
 		rs.next();
 		int depotID = rs.getInt("DepotID");
 		bookCar.setInt(1, bilID);
 		bookCar.setLong(2, bokningsID);
 		bookCar.setDate(3, new java.sql.Date(hamtdatum.getTime()));
 		bookCar.setDate(4, new java.sql.Date(lamningsdatum.getTime()));
 		bookCar.setInt(5, hamtort);
 		bookCar.setInt(6, depotID);
 		bookCar.executeUpdate();
 		
 	}
 	
 	
 	public ResultSet getAvailableCars(String ort, Date dat1, Date dat2) throws SQLException{
 		getAvailableCars.setDate(1, new java.sql.Date(dat1.getTime()));
 		getAvailableCars.setDate(2, new java.sql.Date(dat1.getTime()));
 		getAvailableCars.setDate(3, new java.sql.Date(dat2.getTime()));
 		getAvailableCars.setDate(4, new java.sql.Date(dat2.getTime()));
 		getAvailableCars.setString(5, ort);
 		
 		return getAvailableCars.executeQuery();
 		
 	}
 	
 	
 	//TODO: Change all bigint values to setLong
 	public int createAccount(String user, String password, String fornamn, String efternamn, String postort, int postnr, String email, String adress, long telefon, Date tabort) throws SQLException{
 		createAccount.setString(1, user);
 		createAccount.setString(2, password);
 		createAccount.setString(3, fornamn);
 		createAccount.setString(4, efternamn);
 		createAccount.setString(5, postort);
 		createAccount.setInt(6, postnr);
 		createAccount.setString(7, email);
 		createAccount.setString(8, adress);
 		createAccount.setLong(9, telefon);
 		createAccount.setDate(10, new java.sql.Date(tabort.getTime()));
 		createAccount.executeUpdate();
 		
 		getAccountID.setString(1, user);
 		ResultSet rs = getAccountID.executeQuery();
 		rs.next();
 		return rs.getInt("KontoID");
 		
 	}
 	
 	public int createBooking(int kontoID) throws SQLException{
 		createBooking.setInt(1, kontoID);
 		createBooking.executeUpdate();
 		getBookingID.setInt(1, kontoID);
 		ResultSet rs = getBookingID.executeQuery();
 		rs.next();
 		return rs.getInt("Max");
 		
 	}
 	
 	public void bookChair(int nummer, int bokningsID, int flightID) throws SQLException{
 		bookFlightChair.setInt(1, flightID);
 		bookFlightChair.setInt(2, bokningsID);
 		bookFlightChair.setInt(3, nummer);
 		bookFlightChair.executeUpdate();
 		
 	}
 	
 	public LinkedList<Integer> getAvailableNumbers(int flightID, String type) throws SQLException{
 		gan.setInt(1, flightID);
 		gan.setString(2, type);
 		ResultSet rs = gan.executeQuery();
 		LinkedList<Integer> res = new LinkedList<Integer>();
 		while(rs.next()){
 			res.add(rs.getInt("Nummer"));
 		}
 		return res;
 	}
 	
 	public HashMap<String, Integer> getAvailableChairs(int flightID) throws SQLException{
 		gac.setInt(1, flightID);
 		ResultSet rs = gac.executeQuery();
 		HashMap<String, Integer> res = new HashMap<String, Integer>();
 		while(rs.next()){
 			res.put(rs.getString("Typ"), rs.getInt("Tillg"));
 		}
 		return res;
 	}
 	
 	public ResultSet searchFlight(Date avgangsdatumRaw, String avgangsort, String ankomstort) throws SQLException{
 		java.sql.Date avgangsdatum = new java.sql.Date(avgangsdatumRaw.getTime());
 		sf.setDate(1, avgangsdatum);
 		sf.setString(2, avgangsort);
 		sf.setString(3, ankomstort);
 		return sf.executeQuery();
 		
 	}
 	
 	public ResultSet searchFlightFilters(Date avgangsdatumRaw, String avgangsort, String ankomstort, int minpris, int maxpris) throws SQLException{
 		java.sql.Date avgangsdatum = new java.sql.Date(avgangsdatumRaw.getTime());
 		searchWithFilters.setDate(1, avgangsdatum);
 		searchWithFilters.setString(2, avgangsort);
 		searchWithFilters.setString(3, ankomstort);
 		searchWithFilters.setInt(4, minpris);
 		searchWithFilters.setInt(5, maxpris);
 		
 		return sf.executeQuery();
 		
 	}
 	
 	public LinkedList<DatePrice> fillAlmanack(Date date, String avgang, String ankomst) throws SQLException{
 		
 		fa.setString(1, avgang);
 		fa.setString(2, ankomst);
 		
 		//17 days before and after
 		fa.setDate(3, new java.sql.Date(date.getTime()-1468800000));
 		fa.setDate(4, new java.sql.Date(date.getTime()+1468800000));
 		ResultSet rs = fa.executeQuery();
 		LinkedList<DatePrice> list = new LinkedList<DatePrice>();
 		while(rs.next()){
 			list.add(new DatePrice(rs.getDate("Avgangsdatum"), rs.getInt("Pris")));
 		}
 		return list;
 		
 	}
 	
 	public String getVaccinationByCountry(String country) throws SQLException{
 		vbc.setString(1, country);
 		ResultSet rs = vbc.executeQuery();
 		rs.next();
 		return rs.getString("VaccBeskrivning");
 	}
 	
 	
 
 }
 
