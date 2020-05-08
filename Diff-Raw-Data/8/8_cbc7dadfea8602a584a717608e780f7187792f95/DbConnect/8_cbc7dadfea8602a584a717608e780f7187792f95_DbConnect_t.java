 package Database;
 
 import Geolocatie.MultipleAdressesFoundException;
 import Geolocatie.Coordinaten;
 import Financien.Financien;
 import Geolocatie.Geocoding;
 import java.sql.*;
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import java_backend.Feedback;
 import java_backend.Koerier;
 import java_backend.Locatie;
 import java_backend.Persoon;
 import java_backend.Traject;
 import javax.swing.JOptionPane;
 
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
     String url = "jdbc:mysql://server48.firstfind.nl/vanderbe-2?allowMultiQuery=true";
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
     }
 
     /**
      * Controleerd of verbinding goed is of niet adhv dbconnect.
      *
      * @return Boolean
      * @author Jelle
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
 
             //Select kolom
             String kolom = "YOLO";
 
             //Query uitvoeren
             rs = st.executeQuery(query);
 
             //Loop door de query data heen
             while (rs.next()) {
                 String content = rs.getString(kolom);
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
             if (!Achternaam.equals("")) {
                 query += "AND p.Achternaam = '" + Achternaam + "'";
             }
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
             if (!Achternaam.equals("")) {
                 query += "AND p.Achternaam = '" + Achternaam + "'";
             }
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
      * @param afzender locatie van de verzender
      * @param pakket meegeven data (11 values) is volgorde: <OL start="0">
      * <LI>gewicht</LI>
      * <LI>omschrijving</LI> </OL>
      * @return
      */
     public Boolean newVerzending(Persoon afzender, Persoon ontvanger, Locatie afzenderlocatie, Locatie ontvangerlocatie, String[] pakket) throws MultipleAdressesFoundException {
         String gewicht = pakket[0], omschrijving = pakket[1], afzenderplaats = afzenderlocatie.getPlaatsnaam(), 
                 ontvangerplaats = ontvangerlocatie.getPlaatsnaam(), afzenderstraatnaam = afzenderlocatie.getStraatnaam();       
         if (afzenderplaats != null && afzenderplaats.substring(0, 3).matches("'s ")) {
             afzenderplaats = "Hertogenbosch";
         }
         if (ontvangerplaats != null &&  ontvangerplaats.substring(0, 3).matches("'s ")) {
             ontvangerplaats = "Hertogenbosch";
         }
         Geocoding geo = new Geocoding();
         int locatieId = -1, persoonId = -1, pakketId = -1, verzendingId = -1;
         try {
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
                     + "(VerzendingID, PakketID, Aankomsttijd, Aflevertijd, Status, KostPrijs) "
                     + "VALUES (0, "
                     + pakketId + ", "
                     + "'" + timeStamp + "', " 
                     + "null, " 
                     + "'0', "
                     + "0)"; //todo, kostprijs
             st.executeUpdate(query, Statement.RETURN_GENERATED_KEYS);
 
             rs = st.getGeneratedKeys();
 
             if (rs.next()) {
                 verzendingId = rs.getInt(1);
             } else {
             }
             rs.close();
             rs = null;
             
             Coordinaten from = afzenderlocatie.getCoordinaten(), to = ontvangerlocatie.getCoordinaten();
 
             Traject compleetTraject;
             compleetTraject = geo.GetRouteFrom(from, to);
             Koerier k = new Koerier();
             Financien financien = new Financien();
             // Coordinaten van TZTPoint (station)
             Coordinaten fromToTZT, TZTToTo;
             fromToTZT = geo.GetNearestTZTPoint(from).getCoordinaten();
             TZTToTo = geo.GetNearestTZTPoint(to).getCoordinaten();
 
             Traject Traject1, Traject3;
             int stop1, stop2;
 
             Traject1 = geo.GetRouteFrom(from, fromToTZT);
             Traject3 = geo.GetRouteFrom(TZTToTo, to);
             
             if (compleetTraject.Meters < 20000 
                     || (financien.BerekenGoedkoopsteKoerier(Traject1.Meters).RitPrijs + financien.BerekenGoedkoopsteKoerier(Traject3.Meters).RitPrijs + 2)
                     < financien.BerekenGoedkoopsteKoerier(compleetTraject.Meters).RitPrijs) {
                 k = financien.BerekenGoedkoopsteKoerier(compleetTraject.Meters);
                 insertTraject(verzendingId, afzenderlocatie.getId(), ontvangerlocatie.getId(), "0:30", compleetTraject.Meters, 0, k.KoerierID);
             } else if (compleetTraject.Meters > 20000) {
                 // 1e gedeelte
                 k = financien.BerekenGoedkoopsteKoerier(Traject1.Meters);
                 stop1 = getLocatieId(fromToTZT, true);
                 insertTraject(verzendingId, afzenderlocatie.getId(), stop1, "2:00", Traject1.Meters, 0, k.KoerierID);
                 // 2e gedeelte
                 stop2 = getLocatieId(TZTToTo, true);
                 insertTraject(verzendingId, stop1, stop2, "0:00", 0, 0, 0);
                 // 3e gedeelte
                 k = financien.BerekenGoedkoopsteKoerier(Traject3.Meters);
                 insertTraject(verzendingId, stop2, locatieId, "2:00", Traject3.Meters, 0, k.KoerierID);
             }
             
             int kostprijs = financien.getKostprijs(verzendingId);
            query = "UPDATE Verzending set KostPrijs = '" + kostprijs + "' where VerzendingID = '" + verzendingId + "'";
             st.executeUpdate(query);
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
 
             //Select kolom
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
 
         try {
 
             //Insert query
             query = "INSERT INTO  " + tabelnaam + " ( " + tabel + ")"
                     + " VALUES('" + waardes + "')";
 
             //Query uitvoeren
             st.executeUpdate(query);
 
         } catch (Exception e) {
             System.out.println(e);
         }
     }
 
     public void updateData(String field, String content) {
         //Query voor updaten!!!!----->
         try {
 
 
             //Select kolom
             String kolom = "YOLO";
 
             //Update query
             query = "UPDATE test SET " + kolom + "='" + content + "' WHERE " + kolom + "'" + field + "'";
 
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
 
 
             Object[][] returnval = new Object[aantal][16];
 
             query = "SELECT A.PakketID, B.VerzendingID, C.TrajectID, A.Omschrijving, D.Plaatsnaam AS Beginplaats, D.Straatnaam AS BeginStraatnaam, D.Huisnummer AS BeginHuisnummer, D.Toevoeging AS BeginToevoeging, D.Postcode AS BeginPostcode, D.TZTPoint AS TZTpointBegin, E.Plaatsnaam AS Eindplaats, E.Straatnaam AS EindStraatnaam, E.Huisnummer AS EindHuisnummer, E.Toevoeging AS EindToevoeging, E.Postcode AS EindPostcode, E.TZTPoint AS TZTpointEind "
                     + "FROM Pakket A"
                     + " JOIN Verzending B ON A.PakketID = B.PakketID"
                     + " JOIN Traject C ON B.VerzendingID = C.VerzendingID"
                     + " JOIN Locatie D ON C.Eind = D.LocatieID"
                     + " JOIN Locatie E ON C.Begin = E.LocatieID";
 
             rs = st.executeQuery(query);
 
 
             int i = 0;
             while (rs.next()) {
                 returnval[i][0] = rs.getString("PakketID");
                 returnval[i][1] = rs.getString("VerzendingID");
                 returnval[i][2] = rs.getString("TrajectID");
                 returnval[i][3] = rs.getString("Omschrijving");
                 returnval[i][4] = rs.getString("Beginplaats");
                 returnval[i][5] = rs.getString("BeginStraatnaam");
                 returnval[i][6] = rs.getString("BeginHuisnummer");
                 returnval[i][7] = rs.getString("BeginToevoeging");
                 returnval[i][8] = rs.getString("BeginPostcode");
                 returnval[i][9] = rs.getString("TZTpointBegin");
                 returnval[i][10] = rs.getString("Eindplaats");
                 returnval[i][11] = rs.getString("EindStraatnaam");
                 returnval[i][12] = rs.getString("EindHuisnummer");
                 returnval[i][13] = rs.getString("EindToevoeging");
                 returnval[i][14] = rs.getString("EindPostcode");
                 returnval[i][15] = rs.getString("TZTpointEind");
 
 
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
 
             query = "SELECT COUNT(*) FROM Pakket A JOIN Verzending B ON A.PakketID = B.PakketID JOIN Traject C ON B.VerzendingID = C.VerzendingID JOIN Locatie D ON C.Eind = D.LocatieID JOIN Locatie E ON C.Begin = E.LocatieID ";
             if (begin.equals("'s Hertogenbosch")) {
                 query += "WHERE D.Plaatsnaam = '\\" + begin + "' ";
             } else {
                 query += "WHERE D.Plaatsnaam = '" + begin + "' ";
             }
             if (eind.equals("'s Hertogenbosch")) {
                 query += "AND E.Plaatsnaam = '\\" + eind + "' ";
             } else {
                 query += "AND E.Plaatsnaam = '" + eind + "'";
             }
             
             rs = st.executeQuery(query);
            
 
             int aantal = 0;
             while (rs.next()) {
                 aantal = rs.getInt("COUNT(*)");
             }
             //haal alles op.
             if(aantal == 0){
              JOptionPane.showMessageDialog(null, "Er zijn geen records gevonden. Probeer het opnieuw.", "", JOptionPane.ERROR_MESSAGE);   
             }
 
             Object[][] returnval = new Object[aantal][16];
 
             query = "SELECT A.PakketID, B.VerzendingID, C.TrajectID, A.Omschrijving, D.Plaatsnaam AS Beginplaats, D.Straatnaam AS BeginStraatnaam, D.Huisnummer AS BeginHuisnummer, D.Toevoeging AS BeginToevoeging, D.Postcode AS BeginPostcode, D.TZTPoint AS TZTpointBegin, E.Plaatsnaam AS Eindplaats, E.Straatnaam AS EindStraatnaam, E.Huisnummer AS EindHuisnummer, E.Toevoeging AS EindToevoeging, E.Postcode AS EindPostcode, E.TZTPoint AS TZTpointEind FROM Pakket A JOIN Verzending B ON A.PakketID = B.PakketID JOIN Traject C ON B.VerzendingID = C.VerzendingID JOIN Locatie D ON C.Eind = D.LocatieID JOIN Locatie E ON C.Begin = E.LocatieID ";
             if (begin.equals("'s Hertogenbosch")) {
                 query += "WHERE D.Plaatsnaam = '\\" + begin + "' ";
             } else {
                 query += "WHERE D.Plaatsnaam = '" + begin + "' ";
             }
             if (eind.equals("'s Hertogenbosch")) {
                 query += "AND E.Plaatsnaam = '\\" + eind + "' ";
             } else {
                 query += "AND E.Plaatsnaam = '" + eind + "'";
             }
            
             rs = st.executeQuery(query);
 
 
             int i = 0;
             
             while (rs.next()) {
                 returnval[i][0] = rs.getString("PakketID");
                 returnval[i][1] = rs.getString("VerzendingID");
                 returnval[i][2] = rs.getString("TrajectID");
                 returnval[i][3] = rs.getString("Omschrijving");
                 returnval[i][4] = rs.getString("Beginplaats");
                 returnval[i][5] = rs.getString("BeginStraatnaam");
                 returnval[i][6] = rs.getString("BeginHuisnummer");
                 returnval[i][7] = rs.getString("BeginToevoeging");
                 returnval[i][8] = rs.getString("BeginPostcode");
                 returnval[i][9] = rs.getString("TZTpointBegin");
                 returnval[i][10] = rs.getString("Eindplaats");
                 returnval[i][11] = rs.getString("EindStraatnaam");
                 returnval[i][12] = rs.getString("EindHuisnummer");
                 returnval[i][13] = rs.getString("EindToevoeging");
                 returnval[i][14] = rs.getString("EindPostcode");
                 returnval[i][15] = rs.getString("TZTpointEind");
 
 
                 i++;
             }
             return returnval;
         } catch (Exception e) {
             System.out.println("error : " + e.getMessage() + e.toString());
 
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
 
     public Object[][] getZoekSpecifiekGebruikStatistiek(String zoekoptie) {
         try {
             query = "SELECT COUNT(*) "
                     + "FROM Persoon P "
                     + "JOIN Traject_BPS TBPS on P.PersoonID = TBPS.PersoonID "
                     + "JOIN Traject T on TBPS.TrajectID = T.TrajectID "
                     + "JOIN Locatie LocB on T.Begin = LocB.LocatieID "
                     + "JOIN Locatie LocE on T.Eind = LocE.LocatieID "
                     + "WHERE P.PersoonID = '" + zoekoptie + "' "
                     + "OR P.Voornaam = '" + zoekoptie + "' "
                     + "OR P.Tussenvoegsel = '" + zoekoptie + "' "
                     + "OR P.Achternaam = '" + zoekoptie + "' "
                     + "OR T.TrajectID = '" + zoekoptie + "' "
                     + "OR LocB.Plaatsnaam = '" + zoekoptie + "' "
                     + "OR LocE.Plaatsnaam = '" + zoekoptie + "' ";
 
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
                     + "Locatie LocE on T.Eind = LocE.LocatieID "
                     + "WHERE P.PersoonID = '" + zoekoptie + "' "
                     + "OR P.Voornaam = '" + zoekoptie + "' "
                     + "OR P.Tussenvoegsel = '" + zoekoptie + "' "
                     + "OR P.Achternaam = '" + zoekoptie + "' "
                     + "OR T.TrajectID = '" + zoekoptie + "' "
                     + "OR LocB.Plaatsnaam = '" + zoekoptie + "' "
                     + "OR LocE.Plaatsnaam = '" + zoekoptie + "' ";
 
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
             e.printStackTrace();
         }
         return null;
     }
     /**
      * Methode om aantal pakketten per jaar op te halen.
      * @author Jelle Smeets
      * @param year
      * @return int - aantal pakketten per jaar
      */
     public int getAantalPakketten(int year){
         //set aantal op 0;
         int aantal = 0;
         try{
             //haal aantal ritten op in jaar van year.
             String query = "select count(*) as 'aantal ritten' from Verzending where `Aankomsttijd` >= '1-1-"+year+" 00:00:00' && `Aankomsttijd` <= '31-12-"+year+" 23:59:59'";
             //voer query uit.
             rs = st.executeQuery(query);
             while(rs.next()){
                 //zet aantal ritten in aantal
                 aantal = rs.getInt("aantal ritten");
             }
         }catch(Exception e){
             //toon foutmelding in output.
             System.out.println(e.getMessage());
         }
         //return aantal
         return aantal;
     }
     /**
      * Methode om kostprijs per jaar op te halen.
      * @author Jelle Smeets
      * @param year
      * @return int - kostprijs per jaar
      */
     public double getKostprijs(int year){
     //set aantal op 0;
         double aantal = 0;
         try{
             //haal aantal ritten op in jaar van year.
             String query = "select sum(KostPrijs) as 'kostprijs' from Verzending where `Aankomsttijd` >= '1-1-"+year+" 00:00:00' && `Aankomsttijd` <= '31-12-"+year+" 23:59:59'";
             //voer query uit.
             rs = st.executeQuery(query);
             while(rs.next()){
                 //zet aantal ritten in aantal
                 aantal = rs.getDouble("kostprijs");
             }
         }catch(Exception e){
             //toon foutmelding in output.
             System.out.println(e.getMessage());
         }
         //return aantal
         return aantal;
     }
     /**
      * Bereken winst aan de hand van het jaar.
      * @param year
      * @return 
      */
     public double getWinst(int year){
         //prijs per pakket staat vast.
         int prijspakket = 25;
         //bereken de omzet
         double omzet = (this.getAantalPakketten(year) * prijspakket);
         //bereken de winst
         double winst = omzet - getKostprijs(year);
         //return de winst.
         return winst;
     }
     /**
      * Haal aantal bps ritten op per jaar.
      * @author Jelle
      * @param year
      * @return int - aantal bps ritten
      */
     public int getAantalBps(int year){
         //set aantal op 0;
         int aantal = 0;
         try{
             //haal aantal ritten op in jaar van year.
             String query = "select count(*) as 'aantal bps' from Traject where BPS != 0 AND VerzendingID IN (select VerzendingID from Verzending where `Aankomsttijd` >= '1-1-"+year+" 00:00:00' AND `Aankomsttijd` <= '31-12-"+year+" 23:59:59')";
             //voer query uit.
             rs = st.executeQuery(query);
             while(rs.next()){
                 //zet aantal ritten in aantal
                 aantal = rs.getInt("aantal bps");
             }
         }catch(Exception e){
             //toon foutmelding in output.
             System.out.println(e.getMessage());
         }
         //return aantal
         return aantal;
     }
         
     /**
      * Haal aantal koerier ritten op per jaar.
      * @author Jelle
      * @param year
      * @return int - aantal koerier ritten
      */
     public int getAantalKoerier(int year){
                 //set aantal op 0;
         int aantal = 0;
         try{
             //haal aantal ritten op in jaar van year.
             String query = "select count(*) as 'aantal koerier' from Traject where KoerierID != 0 AND VerzendingID IN (select VerzendingID from Verzending where `Aankomsttijd` >= '1-1-"+year+" 00:00:00' AND `Aankomsttijd` <= '31-12-"+year+" 23:59:59')";
             //voer query uit.
             rs = st.executeQuery(query);
             while(rs.next()){
                 //zet aantal ritten in aantal
                 aantal = rs.getInt("aantal koerier");
             }
         }catch(Exception e){
             //toon foutmelding in output.
             System.out.println(e.getMessage());
         }
         //return aantal
         return aantal;
     }
     /**
      * Haal financiele gegevens op in een array voor een JTable.
      * @param year
      * @return Multidimensionale array voor JTable.
      */
     public Object[][] getFinance(int year) {
 
         
         //instancieer returnval
         Object[][] returnval = new Object[1][5];
         if (year == 0) {
             //als jaar 0 is toon dan alle waarden op 0 voor eeste invul van jtable. // lelijke fix
             returnval[0][0] = 0;
             returnval[0][1] = 0;
             returnval[0][2] = 0;
             returnval[0][3] = 0;
             returnval[0][4] = 0;
         } else {
            //haal variabelen op.
             int aantalpakketten = this.getAantalPakketten(year);
             double kostprijs = this.getKostprijs(year);
             double winst = this.getWinst(year);
             int bps = this.getAantalBps(year);
             int koerier = this.getAantalKoerier(year);
             //maak query en vul returnval;
             //aantal pakketten
             returnval[0][0] = aantalpakketten;
             //aantal keer bps
             returnval[0][1] = bps;
             //aantal keer koerier
             returnval[0][2] = koerier;
             //kostprijs
             returnval[0][3] = kostprijs;
             //winst
             returnval[0][4] = winst;
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
             query = "SELECT C.TrajectID AS tr, C.BPS AS bp, C.KoerierID AS k, D.Plaatsnaam AS begin, E.Plaatsnaam AS eind, C.Status AS s "
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
      * @author Leon Huzen Haal een Locatie op met een PersoonID als invoer
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
     /**
      * Get persoon aan de hand van id uit de database.
      * @author Jelle
      * @param id
      * @return Persoon object/null
      */
     public Persoon getPersoonById(int id){
         try{
             Persoon p = new Persoon();
             query = "SELECT * from Persoon where PersoonID = '"+id+"'";
             rs = st.executeQuery(query);
             while(rs.next()){
                 p.setVoornaam(rs.getString("Voornaam"));
                 p.setTussenvoegsel(rs.getString("Tussenvoegsel"));
                 p.setAchternaam(rs.getString("Achternaam"));
                 p.setEmailadres(rs.getString("Emailadres"));
                 
                 //geboortedatumfix
                // p.setGeboortedatum(rs.getString("Geboortedatum"));
                 
                 p.setMobielnummer(rs.getString("Mobielnummer"));
                 p.setPersoonID(rs.getInt("PersoonID"));
                 p.setRechten(rs.getInt("Rechten"));
             }
             if (p != null)
                 return p;
         }catch(Exception e){
             
             
         }
         return null;
     }
       public ArrayList<Koerier> GetAllActiveKoeriers() {
          ArrayList<Koerier> result = new ArrayList<Koerier>();
         try {
             //Daniel
             //Haal de koeries (transport bedrijven op uit de database.
             
             rs = st.executeQuery("SELECT COUNT(*) FROM Koerier ;");
             int aantal = 0;
             while (rs.next()) {
                 aantal = rs.getInt("COUNT(*)");
             }
             
             //haal alles op.
             query = "SELECT KoerierID, Bedrijfsnaam, Prijsperkm, Starttarief, Startmeters, Actief "
                     + "FROM Koerier "
                     + "WHERE Actief = 1;";
             int i = 0;
 
             rs = st.executeQuery(query);
             Koerier k = new Koerier();
             while (rs.next()) {
                 //vull feedback objecten.
                 k.KoerierID = rs.getInt("KoerierID");
                 k.setBedrijfsnaam(rs.getString("Bedrijfsnaam"));
                 k.PrijsPerKm = rs.getDouble("PrijsperKm");
                 k.StartTarief = rs.getDouble("Starttarief");
                 k.StartMeters = rs.getInt("StartMeters");
                 k.Actief = rs.getInt("Actief");  
                 i++;
                 //koerier toevoegen aan resultaat!
                 result.add(k);
             }
 
             return result;
         } catch (Exception e) {
             System.out.println("error : " + e.getClass());
 
         }
 
         return result;
     }
     
        public Object[][] getKoerierKostprijs() {
         try {
             //LAURENS
             //get specifiekpakket.
             
             rs = st.executeQuery("SELECT COUNT(*) FROM Verzending v JOIN Traject t ON v.VerzendingID = t.VerzendingID JOIN Traject_BPS bps ON t.TrajectID = bps.TrajectID JOIN Persoon p ON bps.PersoonID = p.PersoonID JOIN Koerier k ON t.KoerierID = k.KoerierID WHERE v.status =  '3'");
             int aantal = 0;
             
             while (rs.next()) {
                 aantal = rs.getInt("COUNT(*)");
             }
             //haal alles op.
            
 
             Object[][] returnval = new Object[aantal][8];
             query = "SELECT p.achternaam AS Achternaam, k.bedrijfsnaam AS Bedrijfsnaam, p.emailadres AS Emailadres, v.status AS Status , v.Aankomsttijd AS Aankomsttijd, v.Aflevertijd AS Aflevertijd, v.KostPrijs AS Kostprijs, p.IBAN AS IBAN FROM Verzending v JOIN Traject t ON v.VerzendingID = t.VerzendingID JOIN Traject_BPS bps ON t.TrajectID = bps.TrajectID JOIN Persoon p ON bps.PersoonID = p.PersoonID JOIN Koerier k ON t.KoerierID = k.KoerierID WHERE v.status =  '3'";
             
             int i = 0;
             rs = st.executeQuery(query);
             while (rs.next()) {
 
                 returnval[i][0] = rs.getString("Achternaam");
                 returnval[i][1] = rs.getString("Bedrijfsnaam");
                 returnval[i][2] = rs.getString("Emailadres");
                 returnval[i][3] = rs.getString("Status");
                 returnval[i][4] = rs.getString("Aankomsttijd");
                 returnval[i][5] = rs.getString("Aflevertijd");
                 returnval[i][6] = rs.getString("Kostprijs");
                 returnval[i][7] = rs.getString("IBAN");
                 i++;
             }
             return returnval;
         } catch (Exception e) {
             System.out.println("error : " + e.getClass());
 
         }
         return null;
     }
     
     /**
      * Haal de kilometers en trajectID's op van de trajecten die nog geen BPS'er hebbem
      * @param uurGeleden Het aantal uur dat een traject nog geen BPS'er mag hebben
      * @author Leon Huzen
      * @since 28-05-2013
      */
     public void setKoeriersAlsErGeenBPSerIs (int uurGeleden) {
         try {
             /**
              * Haal de kilometers en trajectID's op van de 
              * trajecten die al een aantal @uurgeleden gemaakt zijn
              */
             String query = "SELECT t.Kilometers, t.TrajectID "
                         + "FROM Verzending v "
                         + "JOIN Traject t ON t.VerzendingID = v.VerzendingID "
                         + "WHERE YEAR(v.Aankomsttijd) = YEAR(CURDATE()) "
                         + "AND MONTH(v.Aankomsttijd) = MONTH(CURDATE()) "
                         + "AND DAY(v.Aankomsttijd) = DAY(CURDATE()) "
                         + "AND HOUR( v.Aankomsttijd ) < HOUR( SUBTIME( CURTIME( ) ,  '" + uurGeleden + ":00:00.000000' ) ) "
                         + "AND t.KoerierID = 0 "
                         + "AND t.BPS = 0";
             rs = st.executeQuery(query);
             
             // Maak een Statement
             st = con.createStatement();  
             query = "";
             // Voor elk traject de beste koerier
             while (rs.next()) {
                 int TrajectID = rs.getInt("TrajectID");
                 Double doubleMeters = rs.getDouble("Kilometers") * 1000; 
                 int meters = doubleMeters.intValue();
                 Financien fin = new Financien();
                 query = "UPDATE Traject t "
                         + "SET t.KoerierID = " + fin.BerekenGoedkoopsteKoerier(meters).KoerierID + " " // Goedkoopste koerier
                         + "WHERE TrajectID = " + TrajectID + ";"; // Van een bepaald traject
                 st.addBatch(query); // Toevoegen aan batch
             }
             try {  
                 st.executeBatch(); // Batch in de database updaten
             } catch (Exception e) {
                 System.out.println(e.getMessage());
             }
         } catch (Exception e) {
             System.out.println("Error in setKoeriersAlsErGeenBPSerIs: " + e.getMessage());
         }
     }
     
     public Object[][] getSpecifiekKoerierKostprijs(String m, String j) {
         try {
             //LAURENS
             //get specifiekpakket.
             rs = st.executeQuery("SELECT COUNT(*) "
                     + "FROM Verzending v "
                     + "JOIN Traject t ON v.VerzendingID = t.VerzendingID "
                     + "JOIN Traject_BPS bps ON t.TrajectID = bps.TrajectID "
                     + "JOIN Persoon p ON bps.PersoonID = p.PersoonID "
                     + "JOIN Koerier k ON t.KoerierID = k.KoerierID "
                     + "WHERE v.status =  '3' "
                     + "AND MONTH( Aankomsttijd ) =  '" + m + "' "
                     + "AND YEAR( Aankomsttijd ) ='" +  j + "'");
             int aantal = 0;
             
             while (rs.next()) {
                 aantal = rs.getInt("COUNT(*)");
             }
             //haal alles op.
             if(aantal == 0){
              JOptionPane.showMessageDialog(null, "Er zijn geen records gevonden. Probeer het opnieuw.", "", JOptionPane.ERROR_MESSAGE);   
             }
             
 
             Object[][] returnval = new Object[aantal][8];
             query = "SELECT p.achternaam AS Achternaam, k.bedrijfsnaam AS Bedrijfsnaam, p.emailadres AS Emailadres, v.status AS Status , v.Aankomsttijd AS Aankomsttijd, v.Aflevertijd AS Aflevertijd, v.KostPrijs AS Kostprijs, p.IBAN AS IBAN FROM Verzending v JOIN Traject t ON v.VerzendingID = t.VerzendingID JOIN Traject_BPS bps ON t.TrajectID = bps.TrajectID JOIN Persoon p ON bps.PersoonID = p.PersoonID JOIN Koerier k ON t.KoerierID = k.KoerierID WHERE v.status =  '3' AND MONTH( Aankomsttijd ) =  '" + m + "' AND YEAR( Aankomsttijd ) ='" +  j + "'";
             
             int i = 0;
             rs = st.executeQuery(query);
             while (rs.next()) {
 
                 returnval[i][0] = rs.getString("Achternaam");
                 returnval[i][1] = rs.getString("Bedrijfsnaam");
                 returnval[i][2] = rs.getString("Emailadres");
                 returnval[i][3] = rs.getString("Status");
                 returnval[i][4] = rs.getString("Aankomsttijd");
                 returnval[i][5] = rs.getString("Aflevertijd");
                 returnval[i][6] = rs.getString("Kostprijs");
                 returnval[i][7] = rs.getString("IBAN");
                 i++;
             }
             return returnval;
         } catch (Exception e) {
             System.out.println("error : " + e.getClass());
 
         }
         return null;
     }
 
     /**
      * Bepaal kostprijs  aan de hand van verzendingID
      * @param verzendingID
      * @author Jelle Smeets
      * @return int kostprijs
      */
     public int calculateKostprijsByVerzendingID(int verzendingID) {
         try{
             query = "select * from Traject where VerzendingID in('"+verzendingID+"')";   
             rs = st.executeQuery(query);
             int kostprijs = 0;
             while(rs.next()){
                 int meters = rs.getInt("Kilometers");
                 int bps = rs.getInt("BPS");
                 int koerier = rs.getInt("KoerierID");
                 if(bps == 0 && koerier == 0){
                     //geen koerier en bps. 
                     //betekend dat er bps er gekozen is maar nog niet beschikbaar is op het traject.
                     kostprijs += 2;
                 }else if(bps != 0){
                     //bpser is op dit traject actief.
                     kostprijs += 2;
                 }else if(koerier != 0){
                     //koerier actief op dit traject
                     Financien f = new Financien();
                     kostprijs += f.getKoerierskosten(meters, koerier);
                 }    
                 
             }
             return kostprijs;
         }catch(Exception ex){
             System.out.println(ex.getMessage());
             return 0;
         }
     }
     
     /**
      * Haal de kostprijs op aan de hand van het id.
      * @author Jelle Smeets
      * @param verzendingID
      * @return int kostprijs
      */
     public int getKostPrijsById(int verzendingID) {
         //haal query op.
         
         try{
             query = "select * from Verzending where VerzendingID = '"+verzendingID+"'";
             rs = st.executeQuery(query);
             int kostprijs = 0;
             //als er een kostprijs is return deze.
             while(rs.next()){
                 kostprijs = rs.getInt("KostPrijs");
             }
             //anders return de 0 waarde die al geset is.
             return kostprijs;
         }catch(Exception ex){
             System.out.println(ex.getMessage());
             return 0;
         }
     }
 
     public double[] getKostenberekeningKoerier(int KoerierID) {
         try{
             
             query = "select * from Koerier where KoerierID = '"+KoerierID+"'";
             rs = st.executeQuery(query);
             double[] returnval = new double[3];
             int i = 0;
             while(rs.next()){
                 returnval[0] = rs.getDouble("Prijsperkm");
                 returnval[1] = rs.getDouble("Starttarief");
                 returnval[2] = rs.getDouble("Startmeters");
                 i++;
             }
             if(i == 0){
                 return null;
             }else{
                 return returnval;
             }
         }catch(Exception ex){
             System.out.println(ex.getMessage());
             return null;
         }
     }
 
     public String LocatieIdtoString(Object id) {
         try{
             query = "select * from Locatie where LocatieID = '"+id+"'";
             rs = st.executeQuery(query);
             while(rs.next()){
                 return rs.getString("Plaatsnaam");
             }
             return "Station niet bekent.";
         }catch(Exception e){
            return "Station niet bekent."; 
         }
         
     }
     
 }
