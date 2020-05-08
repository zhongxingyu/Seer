 package java_backend;
 
 import java.sql.*;
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  * @author Daniel
  */
 public class DbConnect {
 
     //Initializeer connection, statement en result.
     private Connection con;
     private Statement st;
     private ResultSet rs;
     private String persoontabel = "LocatieID, Voornaam, Tussenvoegsel, Achternaam, Emailadres, Wachtwoord, Geboortedatum, Mobielnummer, Profielfoto, IBAN";
     private String locatie = "Latitude, Longitude, Plaatsnaam, Straatnaam, Huisnummer, Toevoeging, Postcode, Telefoonnummer, TZTPoint";
     private String tabel;
     //Server url
     String url = "jdbc:mysql://server48.firstfind.nl/vanderbe-2";
     //Server login naam
     String user = "vanderbe";
     //Server wachtwoord
     String pasw = "Daniel26061990";
     //Query holder
     String query = "";
 
     public DbConnect() {
         //Probeer mysql driver te laden
         try {
             Class.forName("com.mysql.jdbc.Driver");
             con = DriverManager.getConnection(url, user, pasw);
             st = con.createStatement();
             //Afvangen fouten voor database connectie    
         } catch (SQLException ex) {
             System.out.println("Problemen met verbinding.");
         } catch (Exception ex) {
             System.out.println("Onbekende error");
         }
         System.out.println(st);
     }
 
     /**
      * @author Jelle
      * @description Controleerd of verbinding goed is of niet adhv dbconnect.
      * @return Boolean
      */
     public boolean checkConnection() {
         //als st niet null is is connection actief.
         if (st != null) {
             return true;
         } else {
             //bij null stuur false, omdat connectie niet actief is.
             return false;
         }
     }
 
     public void getData() {
         //Query voor uitlezen!!!!----->
         try {
 
             //Select query
             query = "SELECT * FROM test";
 
             //Select collum
             String collum = "YOLO";
 
             //Query uitvoeren
             rs = st.executeQuery(query);
 
             //Loop door de query data heen
             while (rs.next()) {
                 String content = rs.getString(collum);
                 System.out.println(content);
             }
             //Afvangen fouten voor getdata    
         } catch (Exception ea) {
             System.out.println("Query lees ERROR: " + ea);
         }
 
     }
 
     public Object[][] getUsers() {
         try {
             //get aantal personen.
             rs = st.executeQuery("Select Count(*) from Persoon");
             int aantal = 0;
             while (rs.next()) {
                 aantal = rs.getInt("Count(*)");
             }
             //haal alles op.
             Object[][] returnval = new Object[aantal][11];
             query = "SELECT * from Persoon";
             rs = st.executeQuery(query);
             int i = 0;
             while (rs.next()) {
                 returnval[i][0] = rs.getString("PersoonID");
                 returnval[i][1] = rs.getString("Voornaam");
                 returnval[i][2] = rs.getString("Tussenvoegsel");
                 returnval[i][3] = rs.getString("Achternaam");
                 returnval[i][4] = rs.getString("Emailadres");
                 returnval[i][5] = rs.getString("Wachtwoord");
                 returnval[i][6] = rs.getString("Geboortedatum");
                 returnval[i][7] = rs.getString("Mobielnummer");
                 returnval[i][8] = rs.getString("Profielfoto");
                 returnval[i][9] = rs.getString("IBAN");
                 returnval[i][10] = rs.getString("Rechten");
                 i++;
             }
             return returnval;
         } catch (Exception e) {
             System.out.println("error : " + e.getClass());
 
         }
         return null;
     }
 
     /**
      * Haal de personen op die een locatie hebben met coordinaten
      *
      * @param Achternaam Filter op achternaam
      * @return Object met personen <UL> <LI>PersoonID</LI> <LI>Voornaam</LI>
      * <LI>Tussenvoegel</LI> <LI>Achternaam</LI> <LI>Postcode</LI>
      * <LI>Huisnummer</LI> <LI>IBAN</LI> </UL>
      */
     public Object[][] getPersonenWithCoordinates(String Achternaam) {
         try {
             //get aantal personen.
             String query = "SELECT COUNT(*) from Persoon p "
                      + "JOIN Locatie l ON p.LocatieID = l.LocatieID "
                      + "WHERE l.Latitude IS NOT NULL "
                      + "AND l.Longitude IS NOT NULL ";
             if(!Achternaam.equals(""))
                 query += "AND p.Achternaam = '" + Achternaam + "'";
             rs = st.executeQuery(query);
             int aantal = 0;
             while (rs.next()) {
                 aantal = rs.getInt("COUNT(*)");
             }
             //haal alles op.
             Object[][] returnval = new Object[aantal][8];
             query = "SELECT p.PersoonID, p.Voornaam, p.Tussenvoegsel, p.Achternaam, l.Postcode, l.Huisnummer, l.Toevoeging, p.IBAN "
                      + "FROM Persoon p "
                      + "JOIN Locatie l ON p.LocatieID = l.LocatieID "
                      + "WHERE l.Latitude IS NOT NULL "
                      + "AND l.Longitude IS NOT NULL ";
             if(!Achternaam.equals(""))
                 query += "AND p.Achternaam = '" + Achternaam + "'";
             rs = st.executeQuery(query);
             int i = 0;
             while (rs.next()) {
                 returnval[i][0] = rs.getString("PersoonID");
                 returnval[i][1] = rs.getString("Voornaam");
                 returnval[i][2] = rs.getString("Tussenvoegsel");
                 returnval[i][3] = rs.getString("Achternaam");
                 returnval[i][4] = rs.getString("Postcode");
                 returnval[i][5] = rs.getString("Huisnummer");
                 returnval[i][6] = rs.getString("Toevoeging");
                 returnval[i][7] = rs.getString("IBAN");
                 i++;
             }
             return returnval;
         } catch (Exception e) {
             System.out.println("error : " + e.getClass());
         }
         return null;
     }
 
     public Object[][] getPakketPersoon(int ID) {
         try {
             //get aantal personen.
             rs = st.executeQuery("Select count(*) from Persoon");
             int aantal = 0;
             while (rs.next()) {
                 aantal = rs.getInt("Count(*)");
             }
             //haal alles op.
             Object[][] returnval = new Object[aantal][7];
             query = "SELECT P.PersoonID, P.Voornaam, P.Tussenvoegsel, P.Achternaam, Tr.TrajectID, Tr.Begin, Tr.Eind FROM Persoon P JOIN Traject_BPS T ON P.PersoonID = T.PersoonID JOIN Traject Tr ON T.TrajectID = Tr.TrajectID WHERE P.PersoonID = \"" + ID + "\"";
             rs = st.executeQuery(query);
             int i = 0;
             while (rs.next()) {
                 returnval[i][0] = rs.getString("PersoonID");
                 returnval[i][1] = rs.getString("Voornaam");
                 returnval[i][2] = rs.getString("Tussenvoegsel");
                 returnval[i][3] = rs.getString("Achternaam");
                 returnval[i][4] = rs.getString("TrajectID");
                 returnval[i][5] = rs.getString("Begin");
                 returnval[i][6] = rs.getString("Eind");
                 i++;
             }
             return returnval;
         } catch (Exception e) {
             System.out.println("error : " + e.getClass());
 
         }
         return null;
     }
 
     public Object[][] getPersonen(int ID) {
         try {
             //get aantal personen.
             rs = st.executeQuery("SELECT Count(*) FROM Persoon P JOIN Traject_BPS T ON P.PersoonID = T.PersoonID JOIN Traject Tr ON T.TrajectID = Tr.TrajectID WHERE P.PersoonID = " + ID);
             int aantal = 0;
             while (rs.next()) {
                 aantal = rs.getInt("Count(*)");
             }
             //haal alles op.
             Object[][] returnval = new Object[aantal][3];
             query = "SELECT Tr.TrajectID, Tr.Begin, Tr.Eind FROM Persoon P JOIN Traject_BPS T ON P.PersoonID = T.PersoonID JOIN Traject Tr ON T.TrajectID = Tr.TrajectID WHERE P.PersoonID = " + ID;
             rs = st.executeQuery(query);
             int i = 0;
             while (rs.next()) {
                 returnval[i][0] = rs.getString("TrajectID");
                 returnval[i][1] = rs.getString("Begin");
                 returnval[i][2] = rs.getString("Eind");
                 i++;
             }
             return returnval;
         } catch (Exception e) {
             System.out.println("error : " + e.getClass());
 
         }
         return null;
     }
 
     public String[] getSpecifiekeGebruikerGegevens(Object ID) {
         // Auteur Dominique
         try {
             String[] returnval = new String[11];
             query = "SELECT * from Persoon Where PersoonID = " + ID;
             rs = st.executeQuery(query);
             int i = 0;
             while (rs.next()) {
                 returnval[0] = rs.getString("PersoonID");
                 returnval[1] = rs.getString("Voornaam");
                 returnval[2] = rs.getString("Tussenvoegsel");
                 returnval[3] = rs.getString("Achternaam");
                 returnval[4] = rs.getString("Emailadres");
                 returnval[5] = rs.getString("Wachtwoord");
                 returnval[6] = rs.getString("Geboortedatum");
                 returnval[7] = rs.getString("Mobielnummer");
                 returnval[8] = rs.getString("Profielfoto");
                 returnval[9] = rs.getString("IBAN");
                 returnval[10] = rs.getString("Rechten");
                 i++;
             }
             return returnval;
         } catch (Exception e) {
             System.out.println("error : " + e.getClass());
 
         }
         return null;
     }
 
     public String[] getSpecifiekeGebruikerLocatie(Object ID) {
         // Auteur Dominique
         try {
             String[] returnval = new String[6];
 
             // Haalt het bijbehorende LocatieID object op.
             query = "SELECT LocatieID from Persoon_Locatie Where PersoonID = " + ID;
             rs = st.executeQuery(query);
             while (rs.next()) {
                 returnval[0] = rs.getString("LocatieID");
             }
 
             // Gebruikt locatieID en haalt de records daarvan uit database
             query = "SELECT * from Locatie Where LocatieID = " + returnval[0];
             rs = st.executeQuery(query);
             while (rs.next()) {
                 returnval[0] = rs.getString("Plaatsnaam");
                 returnval[1] = rs.getString("Straatnaam");
                 returnval[2] = rs.getString("Huisnummer");
                 returnval[3] = rs.getString("Toevoeging");
                 returnval[4] = rs.getString("Postcode");
                 returnval[5] = rs.getString("TZTPoint");
             }
             return returnval;
         } catch (Exception e) {
             System.out.println("error : " + e.getClass());
         }
         return null;
     }
 
     public void updateGebruikerAccount(String[] data) {
         try {
             query = "UPDATE Persoon "
                     + "SET Voornaam = '" + data[1] + "', "
                     + "Tussenvoegsel = '" + data[2] + "',"
                     + "Achternaam = '" + data[3] + "',"
                     + "Wachtwoord = '" + data[4] + "',"
                     + "Emailadres = '" + data[5] + "',"
                     + "Geboortedatum = '" + data[6] + "',"
                     + "Rechten = '" + data[7] + "',"
                     + "Mobielnummer = '" + data[8] + "',"
                     + "IBAN = '" + data[9] + "' "
                     + "WHERE PersoonID = '" + data[0] + "'";
             System.out.println(query);
             st.executeUpdate(query);
 
             DbConnect dbc = new DbConnect();
             int LocatieID = dbc.getLocatieID("SELECT LocatieID From Persoon where PersoonID = " + data[0]);
 
             query = "UPDATE Locatie "
                     + "SET Huisnummer = '" + data[10] + "', "
                     + "Plaatsnaam= '" + data[11] + "', "
                     + "Straatnaam = '" + data[12] + "', "
                     + "Toevoeging = '" + data[13] + "', "
                     + "TZTPoint = '" + data[14] + "', "
                     + "Postcode = '" + data[15] + "', "
                     + "Latitude = '" + data[16] + "', "
                     + "Longitude = '" + data[17] + "' "
                     + "WHERE LocatieID = '" + LocatieID + "'";
             st.executeUpdate(query);
                     
         } catch (Exception e) {
             System.out.println("error : " + e.getMessage());
         }
     }
 
     public void updateGebruikerAccount2(String[] data) {
         try {
             query = "UPDATE Persoon "
                     + "SET Voornaam = '" + data[1] + "', "
                     + "Tussenvoegsel = '" + data[2] + "',"
                     + "Achternaam = '" + data[3] + "',"
                     + "Emailadres = '" + data[5] + "',"
                     + "Geboortedatum = '" + data[6] + "',"
                     + "Rechten = '" + data[7] + "',"
                     + "Mobielnummer = '" + data[8] + "',"
                     + "IBAN = '" + data[9] + "' "
                     + "WHERE PersoonID = '" + data[0] + "'";
             st.executeUpdate(query);
 
             DbConnect dbc = new DbConnect();
             int LocatieID = dbc.getLocatieID("SELECT LocatieID From Persoon where PersoonID = " + data[0]);
 
             query = "UPDATE Locatie "
                     + "SET Huisnummer = '" + data[10] + "', "
                     + "Plaatsnaam= '" + data[11] + "', "
                     + "Straatnaam = '" + data[12] + "', "
                     + "Toevoeging = '" + data[13] + "', "
                     + "TZTPoint = '" + data[14] + "', "
                     + "Postcode = '" + data[15] + "', "
                     + "Latitude = '" + data[16] + "', "
                     + "Longitude = '" + data[17] + "' "
                     + "WHERE LocatieID = '" + LocatieID + "'";
             st.executeUpdate(query);
         } catch (Exception e) {
             System.out.println("error : " + e.getMessage());
         }
     }
 
     /**
      * Nieuwe verzending opslaan.
      *
      * @param persoonsLocatie locatie van de verzender
      * @param data meegeven data (11 values) is volgorde: <OL start="0">
      * <LI>voornaam</LI> <LI>tussenvoegsel</LI> <LI>achternaam</LI>
      * <LI>straatnaam</LI> <LI>huisnr</LI> <LI>toevoeging</LI> <LI>postcode</LI>
      * <LI>plaats</LI> <LI>telefoonnummer</LI> <LI>gewicht</LI>
      * <LI>omschrijving</LI> </OL>
      * @return
      */
     public Boolean newVerzending(Locatie persoonsLocatie, String[] data) throws MultipleAdressesFoundException {
         String voornaam = data[0], tussenvoegsel = data[1], achternaam = data[2], straatnaam = data[3], huisnummer = data[4], toevoeging = data[5],
                 postcode = data[6], plaats = data[7], telefoonnummer = data[8], gewicht = data[9], omschrijving = data[10];
         if (plaats.substring(0, 3).matches("'s ")) {
             plaats = "Hertogenbosch";
         }
 
         Geocoding geo = new Geocoding();
         int locatieId = -1, persoonId = -1, pakketId = -1, verzendingId = -1;
         Coordinaten coordinatenToLocatie;
         coordinatenToLocatie = geo.QueryAndGetCoordinates(plaats, straatnaam, Integer.parseInt(huisnummer), toevoeging);
         try {
             // INSERT LOCATIE, get LocatieID
             query = "INSERT INTO Locatie "
                     + "(LocatieID, Latitude, Longitude, Plaatsnaam, Straatnaam, Huisnummer, Toevoeging, Postcode, Telefoonnummer, TZTPoint) "
                     + "VALUES (0, "
                     + "'" + coordinatenToLocatie.Latitude.toString() + "',"
                     + "'" + coordinatenToLocatie.Longitude.toString() + "',"
                     + "'" + plaats + "', "
                     + "'" + straatnaam + "', "
                     + "'" + huisnummer + "', "
                     + "'" + toevoeging + "', "
                     + "'" + postcode + "', "
                     + "'" + telefoonnummer + "', "
                     + "'0')";
             st.executeUpdate(query, Statement.RETURN_GENERATED_KEYS);
 
             rs = st.getGeneratedKeys();
 
             if (rs.next()) {
                 locatieId = rs.getInt(1);
             } else {
             }
             rs.close();
             rs = null;
 
             // INSERT PERSOON, get PersoonID
             query = "INSERT INTO Persoon "
                     + "(PersoonID, LocatieID, Voornaam, Tussenvoegsel, Achternaam) "
                     + "VALUES (0, "
                     + "'" + locatieId + "',"
                     + "'" + voornaam + "',"
                     + "'" + tussenvoegsel + "', "
                     + "'" + achternaam + "')";
             st.executeUpdate(query, Statement.RETURN_GENERATED_KEYS);
 
             rs = st.getGeneratedKeys();
 
             if (rs.next()) {
                 persoonId = rs.getInt(1);
             } else {
             }
             rs.close();
             rs = null;
 
             // INSERT Pakket, get PakketID
             String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
             query = "INSERT INTO Pakket "
                     + "(PakketID, Gewicht, Prijs, Omschrijving, Datum) "
                     + "VALUES (0, "
                     + "'" + gewicht + "',"
                     + "'0', " // TODO prijsberekening
                     + "'" + omschrijving + "', "
                     + "'" + timeStamp + "')";
             st.executeUpdate(query, Statement.RETURN_GENERATED_KEYS);
 
             rs = st.getGeneratedKeys();
 
             if (rs.next()) {
                 pakketId = rs.getInt(1);
             } else {
             }
             rs.close();
             rs = null;
 
             // INSERT Verzending, get VerzendingID
             query = "INSERT INTO Verzending "
                     + "(VerzendingID, PakketID, Aankomsttijd, Aflevertijd, Status) "
                     + "VALUES (0, "
                     + pakketId + ", "
                     + "null, " //todo
                     + "null, " //todo
                     + "'0')";
             st.executeUpdate(query, Statement.RETURN_GENERATED_KEYS);
 
             rs = st.getGeneratedKeys();
 
             if (rs.next()) {
                 verzendingId = rs.getInt(1);
             } else {
             }
             rs.close();
             rs = null;
 
 
             Coordinaten from = null, to = coordinatenToLocatie;
             if (persoonsLocatie.hasCoordinaten()) {
                 from = persoonsLocatie.getCoordinaten();
             } else {
                 from = geo.QueryAndGetCoordinates(persoonsLocatie);
             }
 
             Traject compleetTraject;
             compleetTraject = geo.GetRouteFrom(from, to);
             if (compleetTraject.Meters < 20000) {
                 // TODO FINANCIEN! @Daniel en @Leon
             } else if (compleetTraject.Meters > 20000) {
                 // Coordinaten van TZTPoint (station)
                 Coordinaten fromToTZT, TZTToTo;
                 fromToTZT = geo.GetNearestTZTPoint(from).getCoordinaten();
                 TZTToTo = geo.GetNearestTZTPoint(to).getCoordinaten();
 
                 Traject Traject1, Traject3;
                 int stop1, stop2;
                 Financien financien = new Financien();
                 double[] koerier;
                 Traject1 = geo.GetRouteFrom(from, fromToTZT);
                 koerier = financien.BerekenKoerier(Traject1.Meters);
                 // 1e gedeelte
                 stop1 = getLocatieId(fromToTZT, true);
                 insertTraject(verzendingId, persoonsLocatie.getId(), stop1, "2:00", Traject1.Meters, 0, (int) Math.round(koerier[1]));
                 // 2e gedeelte
                 stop2 = getLocatieId(TZTToTo, true);
                 insertTraject(verzendingId, stop1, stop2, "1:00", 333, 0, 0);
                 // 3e gedeelte
                 Traject3 = geo.GetRouteFrom(TZTToTo, to);
                 koerier = financien.BerekenKoerier(Traject3.Meters);
                 insertTraject(verzendingId, stop2, locatieId, "2:00", Traject3.Meters, 0, (int) Math.round(koerier[1]));
             }
 
             return true;
         } catch (Exception e) {
             System.out.println("(DbConnect.java) @ newVerzending - Error : " + e.getMessage());
         }
         return false;
     }
 
     public int getLocatieId(Coordinaten coordinaten, boolean isTZT) {
         try {
             query = "SELECT LocatieID "
                     + "FROM Locatie "
                     + "WHERE Latitude = '" + coordinaten.Latitude.toString() + "' "
                     + "AND Longitude = '" + coordinaten.Longitude.toString() + "' "
                     + "AND TZTPoint ";
             if (isTZT) {
                 query += "= 1";
             } else {
                 query += "!= 0";
             }
             rs = st.executeQuery(query);
             while (rs.next()) {
                 return rs.getInt("LocatieID");
             }
         } catch (Exception e) {
             System.out.println("(DbConnect.java) @ getLocatieId - Error: " + e.getMessage());
         }
         return 0;
     }
 
     /**
      * INSERT Traject in database
      *
      * @return TrajectID
      */
     public int insertTraject(int verzendingID, int begin, int eind, String reistijd, int kilometers, int bps, int koerierId) {
         try {
             query = "INSERT INTO Traject "
                     + "(TrajectID, VerzendingID, Begin, Eind, Reistijd, Kilometers, BPS, KoerierID) "
                     + "VALUES (0, "
                     + verzendingID + ","
                     + "'" + begin + "',"
                     + "'" + eind + "', "
                     + "'" + reistijd + "', "
                     + "'" + kilometers + "', "
                     + "'" + bps + "', "
                     + "'" + koerierId + "')";
             st.executeUpdate(query, Statement.RETURN_GENERATED_KEYS);
 
             rs = st.getGeneratedKeys();
 
             if (rs.next()) {
                 return rs.getInt(1);
             } else {
             }
             rs.close();
             rs = null;
         } catch (Exception e) {
             System.out.println("(DbConnect.java) @ insertTraject - Error: " + e.getMessage());
         }
         return 0;
     }
 
     /**
      * @author Laurens
      * @autorv2 Jelle
      * @param emailadres
      * @param wachtwoord
      * @param succes
      * @return Object of Persoon or NULL
      * @throws SQLException
      */
     public Persoon getLoginData(String emailadres, String wachtwoord, boolean succes) throws SQLException {
         //Query voor uitlezen login gegevens!!!!----->
         PreparedStatement stmt = null;
 
         try {
             //Select query
             //rechten >0 houd in dat iedereen met rechten BOVEN BPS'er in mogen loggen.
             stmt = con.prepareStatement("SELECT * FROM Persoon WHERE Emailadres = ? AND Wachtwoord = ? AND rechten > 0");
 
             stmt.setString(1, emailadres);
             stmt.setString(2, wachtwoord);
 
             //Select collum
             String password = "Wachtwoord";
             String email = "Emailadres";
 
             //Query uitvoeren
             rs = stmt.executeQuery();
 
             //Loop door de query data heen
             while (rs.next()) {
 
                 String content = rs.getString(password);
                 String content1 = rs.getString(email);
 
                 if (content1.equals(emailadres) && content.equals(wachtwoord)) {
                     //inloggen is gelukt, vul persoon object.
                     Persoon p = new Persoon();
                     p.setVoornaam(rs.getString("Voornaam"));
                     p.setTussenvoegsel(rs.getString("Tussenvoegsel"));
                     p.setAchternaam(rs.getString("Achternaam"));
                     p.setEmailadres(rs.getString("Emailadres"));
 
                     //zet geboortedatum om van String uit DB naar Date in Java
                     DateFormat df = new SimpleDateFormat("dd-MM-yyyy");
                     try {
                         p.setGeboortedatum(df.parse(rs.getString("Geboortedatum")));
                         System.out.println(p.getGeboortedatum());
                     } catch (ParseException ex) {
                         Logger.getLogger(DbConnect.class.getName()).log(Level.SEVERE, null, ex);
                     }
                     p.setMobielnummer(rs.getString("Mobielnummer"));
                     p.setPersoonID(rs.getInt("PersoonID"));
                     p.setRechten(rs.getInt("Rechten"));
 
                     //object is gevuld geef het object terug.
                     return p;
 
                 } else {
                     //geen match gevonden in het systeem, inloggen is niet succesvol.
                     return null;
                 }
             }
         } finally {
             //sluiten van databaseconnectie
             try {
                 if (stmt != null) {
                     stmt.close();
                 }
             } catch (Exception e) {
                 // log this error
             }
             try {
                 if (con != null) {
                     con.close();
                 }
             } catch (Exception e) {
                 // log this error
             }
         }
         return null;
     }
 
     public void insertData(String content, String aa) {
         //Query voor inserten!!!!----->       
 
         try {
 
             //Insert query
             query = "INSERT INTO test VALUES('" + content + "')";
 
             //Query uitvoeren
             st.executeUpdate(query);
 
         } catch (Exception ea) {
             System.out.println("Query schrijf ERROR: " + ea);
 
         }
 
     }
 
     public void insertData() {
         //Query voor inserten!!!!----->       
         try {
 
             //Insert query
             query = "INSERT INTO test VALUES('')";
 
             //Query uitvoeren
             st.executeUpdate(query);
 
         } catch (Exception ea) {
             System.out.println("Query schrijf ERROR: " + ea);
 
         }
 
     }
 
     public void insertData(String tabelnaam, String... value) {
         //Query voor inserten!!!!----->   
         String waardes = "";
 
         if (tabelnaam.equals("Persoon")) {
             this.tabel = this.persoontabel;
         } else if (tabelnaam.equals("Locatie")) {
             this.tabel = this.locatie;
         }
 
         for (int i = 0; i < value.length; i++) {
             waardes += i <= value.length - 2
                     ? value[i] + "','"
                     : value[i] + "";
         }
         System.out.println(tabelnaam);
         System.out.println(tabel);
         System.out.println(waardes);
 
         try {
 
             //Insert query
             query = "INSERT INTO  " + tabelnaam + " ( " + tabel + ")"
                     + " VALUES('" + waardes + "')";
 
             //Query uitvoeren
             st.executeUpdate(query);
             System.out.println(query);
 
         } catch (Exception e) {
             System.out.println(e);
         }
     }
 
     public void updateData(String field, String content) {
         //Query voor updaten!!!!----->
         try {
 
 
             //Select collum
             String collum = "YOLO";
 
             //Update query
             query = "UPDATE test SET " + collum + "='" + content + "' WHERE " + collum + "'" + field + "'";
 
             //Query uitvoeren
             st.executeUpdate(query);
 
         } catch (Exception ea) {
             System.out.println("Query update ERROR: " + ea);
 
         }
 
     }
 
     public Object[][] getPakket() {
         try {
             //LAURENS
             //pakt alle pakketen.
             query = "SELECT Count(*) AS aantalPakketten "
                     + "FROM Pakket A "
                     + "JOIN Verzending B ON A.PakketID = B.PakketID "
                     + "JOIN Traject C ON B.VerzendingID = C.VerzendingID "
                     + "JOIN Locatie D ON C.Eind = D.LocatieID "
                     + "JOIN Locatie E ON C.Begin = E.LocatieID";
             rs = st.executeQuery(query);
             int aantal = 0;
             while (rs.next()) {
                 aantal = rs.getInt("aantalPakketten");
             }
 
 
             Object[][] returnval = new Object[aantal][8];
             query = "SELECT A.PakketID, B.VerzendingID, C.TrajectID, A.Omschrijving, C.Eind, C.Begin, D.Plaatsnaam AS BeginPlaats, E.Plaatsnaam AS EindPlaats "
                     + "FROM Pakket A "
                     + "JOIN Verzending B ON A.PakketID = B.PakketID "
                     + "JOIN Traject C ON B.VerzendingID = C.VerzendingID "
                     + "JOIN Locatie D ON C.Eind = D.LocatieID "
                     + "JOIN Locatie E ON C.Begin = E.LocatieID";
             rs = st.executeQuery(query);
             int i = 0;
             while (rs.next()) {
                 returnval[i][0] = rs.getString("PakketID");
                 returnval[i][1] = rs.getString("VerzendingID");
                 returnval[i][2] = rs.getString("TrajectID");
                 returnval[i][3] = rs.getString("Omschrijving");
                 returnval[i][4] = rs.getString("Begin");
                 returnval[i][5] = rs.getString("Eind");
                 returnval[i][6] = rs.getString("BeginPlaats");
                 returnval[i][7] = rs.getString("Eindplaats");
                 i++;
             }
             return returnval;
         } catch (Exception e) {
             System.out.println("error : " + e.getClass());
 
         }
         return null;
     }
 
     public Object[] getLocatie() {
         try {
             //LAURENS
             String[] returnval = new String[26];
             //haal alles op.
 
             query = "SELECT Plaatsnaam "
                     + "FROM Locatie "
                     + "WHERE TZTPoint = '1'"
                     + "ORDER BY Plaatsnaam ASC";
             rs = st.executeQuery(query);
             int i = 0;
             while (rs.next()) {
                 returnval[i] = rs.getString("Plaatsnaam");
                 i++;
             }
             return returnval;
         } catch (Exception e) {
             System.out.println("error : " + e.getClass());
 
         }
         return null;
     }
 
     public Boolean bpsTrajectUpdate(String[] data) {
         /*
          * Auteur: Dominique
          */
         try {
             query = "UPDATE Traject "
                     + "SET Begin = \"" + data[0] + "\", "
                     + "Eind = \"" + data[1] + "\""
                     + "WHERE TrajectID = \"" + data[2] + "\"";
             System.out.println(query);
             st.executeUpdate(query);
         } catch (Exception e) {
             System.out.println("error : " + e.getMessage());
 
         }
         return null;
     }
 
     public Boolean verwijderPersoon(String query) {
         // Auteur Dominique
         try {
             st.executeUpdate(query);
         } catch (Exception e) {
             System.out.println("error : " + e.getMessage());
         }
         return null;
     }
 
     public Object[][] getSpecifiekPakket(String begin, String eind) {
         try {
             //LAURENS
             //get specifiekpakket.
             rs = st.executeQuery("SELECT COUNT(*) AS aantalPakketten "
                     + "FROM Pakket A "
                     + "JOIN Verzending B ON A.PakketID = B.PakketID "
                     + "JOIN Traject C ON B.VerzendingID = C.VerzendingID "
                     + "JOIN Locatie D ON C.Eind = D.LocatieID "
                     + "JOIN Locatie E ON C.Begin = E.LocatieID");
             int aantal = 0;
             while (rs.next()) {
                 aantal = rs.getInt("aantalPakketten");
             }
             //haal alles op.
 
 
             Object[][] returnval = new Object[aantal][8];
             query = "SELECT A.PakketID, B.VerzendingID, C.TrajectID, A.Omschrijving, C.Eind, C.Begin, D.Plaatsnaam AS BeginPlaats, E.Plaatsnaam AS EindPlaats "
                     + "FROM Pakket A "
                     + "JOIN Verzending B ON A.PakketID = B.PakketID "
                     + "JOIN Traject C ON B.VerzendingID = C.VerzendingID "
                     + "JOIN Locatie D ON C.Eind = D.LocatieID "
                     + "JOIN Locatie E ON C.Begin = E.LocatieID "
                     + "WHERE D.Plaatsnaam = '" + begin + "' AND E.Plaatsnaam = '" + eind + "'";
             int i = 0;
             rs = st.executeQuery(query);
             while (rs.next()) {
 
                 returnval[i][0] = rs.getString("PakketID");
                 returnval[i][1] = rs.getString("VerzendingID");
                 returnval[i][2] = rs.getString("TrajectID");
                 returnval[i][3] = rs.getString("Omschrijving");
                 returnval[i][4] = rs.getString("Begin");
                 returnval[i][5] = rs.getString("Eind");
                 returnval[i][6] = rs.getString("BeginPlaats");
                 returnval[i][7] = rs.getString("EindPlaats");
                 i++;
             }
             return returnval;
         } catch (Exception e) {
             System.out.println("error : " + e.getClass());
 
         }
         return null;
     }
 
     public Object[][] getGebruikStatistiek() {
         try {
             //get aantal personen.
             rs = st.executeQuery("SELECT COUNT(*) "
                     + "FROM Persoon P "
                     + "JOIN Traject_BPS TBPS on P.PersoonID = TBPS.PersoonID "
                     + "JOIN Traject T on TBPS.TrajectID = T.TrajectID "
                     + "JOIN Locatie LocB on T.Begin = LocB.LocatieID "
                     + "JOIN Locatie LocE on T.Eind = LocE.LocatieID");
             int aantal = 0;
             while (rs.next()) {
                 aantal = rs.getInt("COUNT(*)");
             }
             //haal alles op.
             Object[][] returnval = new Object[aantal][7];
             query = "SELECT P.PersoonID, P.Voornaam, P.Tussenvoegsel, P.Achternaam, T.TrajectID, LocB.Plaatsnaam AS Beginplaats, LocE.Plaatsnaam AS Eindplaats "
                     + "FROM Persoon P "
                     + "JOIN Traject_BPS TBPS on P.PersoonID = TBPS.PersoonID "
                     + "JOIN Traject T on TBPS.TrajectID = T.TrajectID "
                     + "JOIN Locatie LocB on T.Begin = LocB.LocatieID JOIN "
                     + "Locatie LocE on T.Eind = LocE.LocatieID";
             rs = st.executeQuery(query);
             int i = 0;
             while (rs.next()) {
                 returnval[i][0] = rs.getString("PersoonID");
                 returnval[i][1] = rs.getString("Voornaam");
                 returnval[i][2] = rs.getString("Tussenvoegsel");
                 returnval[i][3] = rs.getString("Achternaam");
                 returnval[i][4] = rs.getString("TrajectID");
                 returnval[i][5] = rs.getString("Beginplaats");
                 returnval[i][6] = rs.getString("Eindplaats");
                 i++;
             }
             return returnval;
         } catch (Exception e) {
             System.out.println("error : " + e.getClass());
 
         }
         return null;
     }
 
     public Object[][] getSpecifiekGebruikStatistiek(String begin, String eind) {
         try {
             query = "SELECT COUNT(*) "
                     + "FROM Persoon P "
                     + "JOIN Traject_BPS TBPS on P.PersoonID = TBPS.PersoonID "
                     + "JOIN Traject T on TBPS.TrajectID = T.TrajectID "
                     + "JOIN Locatie LocB on T.Begin = LocB.LocatieID "
                     + "JOIN Locatie LocE on T.Eind = LocE.LocatieID ";
             if (begin.equals("'s Hertogenbosch")) {
                 query += "WHERE LocB.Plaatsnaam = '\\" + begin + "' ";
             } else {
                 query += "WHERE LocB.Plaatsnaam = '" + begin + "' ";
             }
             if (eind.equals("'s Hertogenbosch")) {
                 query += "AND LocE.Plaatsnaam = '\\" + eind + "' ";
             } else {
                 query += "AND LocE.Plaatsnaam = '" + eind + "'";
             }
             rs = st.executeQuery(query);
             int aantal = 0;
             while (rs.next()) {
                 aantal = rs.getInt("COUNT(*)");
             }
 
 
             Object[][] returnval = new Object[aantal][7];
             query = "SELECT P.PersoonID, P.Voornaam, P.Tussenvoegsel, P.Achternaam, T.TrajectID, LocB.Plaatsnaam AS Beginplaats, LocE.Plaatsnaam AS Eindplaats "
                     + "FROM Persoon P "
                     + "JOIN Traject_BPS TBPS on P.PersoonID = TBPS.PersoonID "
                     + "JOIN Traject T on TBPS.TrajectID = T.TrajectID "
                     + "JOIN Locatie LocB on T.Begin = LocB.LocatieID JOIN "
                     + "Locatie LocE on T.Eind = LocE.LocatieID ";
             if (begin.equals("'s Hertogenbosch")) {
                 query += "WHERE LocB.Plaatsnaam = '\\" + begin + "' ";
             } else {
                 query += "WHERE LocB.Plaatsnaam = '" + begin + "' ";
             }
             if (eind.equals("'s Hertogenbosch")) {
                 query += "AND LocE.Plaatsnaam = '\\" + eind + "' ";
             } else {
                 query += "AND LocE.Plaatsnaam = '" + eind + "' ";
             }
             int i = 0;
             rs = st.executeQuery(query);
             while (rs.next()) {
                 returnval[i][0] = rs.getString("PersoonID");
                 returnval[i][1] = rs.getString("Voornaam");
                 returnval[i][2] = rs.getString("Tussenvoegsel");
                 returnval[i][3] = rs.getString("Achternaam");
                 returnval[i][4] = rs.getString("TrajectID");
                 returnval[i][5] = rs.getString("Beginplaats");
                 returnval[i][6] = rs.getString("Eindplaats");
                 i++;
             }
             return returnval;
         } catch (Exception e) {
             System.out.println("error : " + e.getClass());
 
         }
         return null;
     }
 
     /**
      * @autor Jelle
      * @param int year, year is the year you want the finance information from.
      * @description gets finance information from database with
      * @return returnval array with integer information about finance.
      */
     public int[][] getFinance(int year) {
         //make query still to do.
         //instancieer returnval
         int[][] returnval = new int[1][5];
         if (year == 0) {
             //als jaar 0 is toon dan alle waarden op 0 voor eeste invul van jtable. // lelijke fix
             returnval[0][0] = 0;
             returnval[0][1] = 0;
             returnval[0][2] = 0;
             returnval[0][3] = 0;
             returnval[0][4] = 0;
         } else {
             //maak query en vul returnval;
             returnval[0][0] = year;
             returnval[0][1] = 75;
             returnval[0][2] = 25;
             returnval[0][3] = 1000;
             returnval[0][4] = 100;
         }
         //return de array
         return returnval;
     }
 
     public Object[][] getPakketWijzigen(int pakketID) {
         try {
             //get aantal personen.
             rs = st.executeQuery("SELECT COUNT(*)"
                     + "FROM Pakket A "
                     + "JOIN Verzending B "
                     + "ON A.PakketID = B.PakketID "
                     + "JOIN Traject C "
                     + "ON B.VerzendingID = C.VerzendingID "
                     + "JOIN Locatie D "
                     + "ON C.Begin = D.LocatieID "
                     + "JOIN Locatie E "
                     + "On C.Eind = E.LocatieID "
                     + "WHERE A.PakketID = " + pakketID + ";");
 
             int aantal = 0;
             while (rs.next()) {
                 aantal = rs.getInt("Count(*)");
 
             }
 
             Object[][] returnval = new Object[aantal][6];
 
 
             //Select query
            query = "SELECT C.TrajectID AS tr, C.BPS AS bp, C.KoerierID AS k, D.Plaatsnaam AS begin, E.Plaatsnaam AS eind, B.Status AS s "
                     + "FROM Pakket A "
                     + "JOIN Verzending B "
                     + "ON A.PakketID = B.PakketID "
                     + "JOIN Traject C "
                     + "ON B.VerzendingID = C.VerzendingID "
                     + "JOIN Locatie D "
                     + "ON C.Begin = D.LocatieID "
                     + "JOIN Locatie E "
                     + "On C.Eind = E.LocatieID "
                     + "WHERE A.PakketID = " + pakketID + ";";
 
 
 
             //Query uitvoeren
             rs = st.executeQuery(query);
 
             //Loop door de query data heen
             int i = 0;
             while (rs.next()) {
 
                 returnval[i][0] = rs.getString("tr");
 
                 if (Integer.parseInt(rs.getString("bp")) == 0) {
                     returnval[i][1] = "Koerier";
                 } else if ((Integer.parseInt(rs.getString("bp")) == 1)) {
                     returnval[i][1] = "BPS'er";
                 } else {
                     returnval[i][1] = "onbekend";
                 }
 
                 returnval[i][2] = rs.getString("k");
                 returnval[i][3] = rs.getString("begin");
                 returnval[i][4] = rs.getString("eind");
 
                 if (Integer.parseInt(rs.getString("s")) == 0) {
                     returnval[i][5] = "Aangemeld";
                 } else if ((Integer.parseInt(rs.getString("s")) == 1)) {
                     returnval[i][5] = "Onderweg";
                 } else if ((Integer.parseInt(rs.getString("s")) == 2)) {
                     returnval[i][5] = "Verwacht";
                 } else if ((Integer.parseInt(rs.getString("s")) == 3)) {
                     returnval[i][5] = "Afgeleverd";
                 } else {
                     returnval[i][5] = "Onbekend";
                 }
                 i++;
             }
 
             //Afvangen fouten voor getdata
             return returnval;
         } catch (Exception ea) {
             System.out.println("Query lees ERROR: " + ea);
         }
         return null;
     }
 
     public int getLocatieID(String query) {
         // Dominque
         System.out.println(query);
         int locatieID = 0;
         try {
             rs = st.executeQuery(query);
             while (rs.next()) {
                 locatieID = Integer.parseInt(rs.getString("LocatieID"));
             }
             return locatieID;
         } catch (Exception e) {
             System.out.println("error : " + e.getClass());
         }
         return locatieID;
     }
 
     public int getPersoonID(String query) {
         // Dominque
         System.out.println(query);
         int persoonID = 0;
         try {
             rs = st.executeQuery(query);
             while (rs.next()) {
                 persoonID = Integer.parseInt(rs.getString("PersoonID"));
             }
             return persoonID;
         } catch (Exception e) {
             System.out.println("error : " + e.getClass());
         }
         return persoonID;
     }
 
     public void nieuweGebruiker(String query) {
         // Dominique
         System.out.println(query);
         try {
             //Query uitvoeren
             st.executeUpdate(query);
 
         } catch (Exception ea) {
             System.out.println("Query schrijf ERROR: " + ea);
         }
 
     }
 
     public Object[][] getPakketStatus() {
         try {
             //LAURENS
             //get status.
 
             rs = st.executeQuery("SELECT COUNT(*) FROM Traject t JOIN Verzending v ON t.VerzendingID = v.VerzendingID JOIN Pakket p ON p.PakketID = v.VerzendingID JOIN Koerier k ON k.KoerierID = t.KoerierID JOIN Locatie D ON t.Eind = D.LocatieID JOIN Locatie E ON t.Begin = E.LocatieID");
             int aantal = 0;
             while (rs.next()) {
                 aantal = rs.getInt("COUNT(*)");
             }
             //haal alles op.
 
 
             Object[][] returnval = new Object[aantal][12];
             query = "SELECT t.TrajectID as TrajectID, t.VerzendingID as VerzendingID, v.PakketID as PakketID, t.KoerierID as KoerierID, k.Bedrijfsnaam as Bedrijfsnaam, t.Begin as Begin, t.Eind as Eind, D.Plaatsnaam as Beginplaats, E.Plaatsnaam as Eindplaats, v.Aankomsttijd as Aankomsttijd, v.Aflevertijd as Aflevertijd, v.Status as Status FROM Traject t JOIN Verzending v ON t.VerzendingID = v.VerzendingID JOIN Pakket p ON p.PakketID = v.VerzendingID JOIN Koerier k ON k.KoerierID = t.KoerierID JOIN Locatie D ON t.Eind = D.LocatieID JOIN Locatie E ON t.Begin = E.LocatieID";
             int i = 0;
 
             rs = st.executeQuery(query);
             while (rs.next()) {
 
                 returnval[i][0] = rs.getString("TrajectID");
                 returnval[i][1] = rs.getString("VerzendingID");
                 returnval[i][2] = rs.getString("PakketID");
                 returnval[i][3] = rs.getString("KoerierID");
                 returnval[i][4] = rs.getString("Bedrijfsnaam");
                 returnval[i][5] = rs.getString("Begin");
                 returnval[i][6] = rs.getString("Eind");
                 returnval[i][7] = rs.getString("Beginplaats");
                 returnval[i][8] = rs.getString("Eindplaats");
                 returnval[i][9] = rs.getString("Aankomsttijd");
                 returnval[i][10] = rs.getString("Aflevertijd");
                 returnval[i][11] = rs.getString("Status");
                 i++;
             }
             return returnval;
         } catch (Exception e) {
             System.out.println("error : " + e.getClass());
 
         }
 
         return null;
     }
 
     /**
      * @author Leon Huzen
      * Haal een Locatie op met een PersoonID als invoer
      * @param persoonID String PersoonID
      * @return Locatie (ID, Coordinaten)
      */
     public Locatie getLocatieFromPersoonId(String persoonID) {
         Locatie result = null;
         try {
             query = "SELECT l.LocatieID, l.Latitude, l.Longitude "
                     + "FROM Locatie l "
                     + "JOIN Persoon_Locatie pl ON l.LocatieID = pl.LocatieID "
                     + "WHERE pl.PersoonID = " + persoonID;
             rs = st.executeQuery(query);
             while (rs.next()) {
                 Coordinaten coordinaten = new Coordinaten(Double.parseDouble(rs.getString("Latitude")), Double.parseDouble(rs.getString("Longitude")));
                 result = new Locatie(Integer.parseInt(rs.getString("LocatieID")), coordinaten);
             }
             return result;
         } catch (Exception e) {
             System.out.println("error : " + e.getMessage());
         }
         return result;
     }
     
     /**
      * @author Jelle(v2.)) en Daniel(v1.0)
      * @param PakketID
      * @description v1.0 return array of strings.
      * @description v2.0 return array of feedback objects.
      * @return array of feedback objects
      */
     public Feedback[] getFeedback(int PakketID) {
         try {
             //Daniel
             //tel hoeveel feedback er is.
 
             rs = st.executeQuery("SELECT COUNT(*) FROM Feedback WHERE PakketID = " + PakketID + ";");
             int aantal = 0;
             while (rs.next()) {
                 aantal = rs.getInt("COUNT(*)");
             }
             //haal alles op.
 
 
             Feedback[] returnval = new Feedback[aantal];
             query = "SELECT FeedbackID, PakketID, Waardering, Omschrijving, Ontvangstatus FROM Feedback WHERE PakketID = " + PakketID + ";";
             int i = 0;
 
             rs = st.executeQuery(query);
             while (rs.next()) {
                 //vull feedback objecten.
                 returnval[i] = new Feedback();
                 returnval[i].setFeedbackID(Integer.parseInt(rs.getString("FeedbackID")));
                 returnval[i].setOmschrijving(rs.getString("Omschrijving"));
                 returnval[i].setPakketID(Integer.parseInt(rs.getString("PakketID")));
                 returnval[i].setWaardering(Integer.parseInt(rs.getString("Waardering")));
                 returnval[i].setOntvangststatus(Integer.parseInt(rs.getString("Ontvangstatus")));
                 i++;
             }
             return returnval;
         } catch (Exception e) {
             System.out.println("error : " + e.getClass());
 
         }
 
         return null;
     }
 
     public void updateStatus(Object trajectID, int status) {
         try {
             query = "UPDATE Traject "
                     + "SET Status = " + status + " WHERE TrajectID = '" + trajectID + "'";
 
             System.out.println(query);
             st.executeUpdate(query);
 
 
         } catch (Exception e) {
             System.out.println("error : " + e.getMessage());
         }
     }
 }
