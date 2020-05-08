 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package DB;
 
 import DataStructur.DuengUndWasser;
 import DataStructur.Duenger;
 import DataStructur.PflanzenHoehe;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.sql.ResultSet;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 
 /**
  *
  * @author Jung-Ho Choi
  */
 public class DB {
         
     private Connection con = null;
     private Statement stmt = null;
     private ResultSet rslt = null;
     private PreparedStatement pstmt = null;
     private boolean userValidity = false;
     
     private SimpleDateFormat dbFormat = new SimpleDateFormat("yyyy.MM.dd");
     
     private Connection ConnectDB () throws Exception {
         
         // Informationen zusammensuchen
         String dbHost = "localhost";
         String dbPort = "3306";
         String dbName = "chilli_database";
         String dbUser = "root";
         String dbPassword = "test";
         
         // Treiberklasse laden
         Class.forName("com.mysql.jdbc.Driver");
         
         // URL zusammenbauen
         StringBuilder url = new StringBuilder ();
         url.append ("jdbc:mysql://").append(dbHost);
         url.append (":").append(dbPort).append("/");
         url.append (dbName);
         url.append ("?user=").append(dbUser);
         url.append ("&password=").append(dbPassword);
         
         con = DriverManager.getConnection(url.toString());
         
         return con;
         
     }
     
     private void CloseDBConnection() throws SQLException {
         if (rslt != null) {
             rslt.close();
         }
         
         if (stmt != null) {
             stmt.close();
         }
         
         if (con != null) {
             con.close();
         }
         
         if (pstmt != null) {
             pstmt.close();
         }
     }
 
     public boolean getuserValidity() {
         return userValidity;
     }
     
     public void InsertIntoBenutzer (String benutzername, String passwort) throws Exception{
         
         try {
             // Zur Datenbank verbinden
             con = ConnectDB();
             // Statement erstellen
             pstmt = con.prepareStatement("INSERT INTO tbl_benutzer VALUES(?,?,?)");
             //Query erstellen
             pstmt.setString(1, null);
             pstmt.setString(2, benutzername);
             pstmt.setString(3, passwort);
             pstmt.executeUpdate();
         }
         catch (Exception e) {
             
         }
         finally {
             this.CloseDBConnection();
         }
     }
     
     public void InsertIntoPflanzen_hohe(PflanzenHoehe h) throws Exception{   
         try { 
             // Zur Datenbank verbinden
             con = ConnectDB();
             // Statement erstellen                   
             pstmt = con.prepareStatement("INSERT INTO pflanzen_hoehe VALUES(?,?,?);");
             //Query erstellen 
             pstmt.setString(1, h.getSorte());  
             pstmt.setString(2, String.valueOf(dbFormat.format((Date)h.getDatum()))); 
             pstmt.setString(3, String.valueOf(h.getHoehe()));
             pstmt.executeUpdate();
             }
         catch (Exception e) {}
         
         finally{this.CloseDBConnection();}    
     }
  
     public void InsertIntoDuengvorgang(DuengUndWasser dw) throws Exception{   
         try { 
             // Zur Datenbank verbinden
             con = ConnectDB();
             // Statement erstellen                   
             pstmt = con.prepareStatement("INSERT INTO duengvorgang VALUES(?,?,?);");
             //Query erstellen 
             pstmt.setString(1, String.valueOf(dbFormat.format((Date)dw.getDatum())));  
             pstmt.setString(2, dw.getDuenger()); 
             pstmt.setString(3, String.valueOf(dw.getMenge()));
             pstmt.executeUpdate();
             }
         catch (Exception e) {}
         
         finally{this.CloseDBConnection();}    
     }    
     
     public void InsertIntoDuenger(Duenger duenger) throws Exception{
       
         try { 
             // Zur Datenbank verbinden
             con = ConnectDB();
             // Statement erstellen
             pstmt = con.prepareStatement("INSERT INTO tbl_duenger VALUES(?,?,?,?,?,?)");
             //Query erstellen
             pstmt.setString(1, null); 
             pstmt.setString(2, duenger.getName());  
             pstmt.setString(3, String.valueOf(duenger.getStickstoff())); 
             pstmt.setString(4, String.valueOf(duenger.getPhosphat()));
             pstmt.setString(5, String.valueOf(duenger.getKalium()));
            pstmt.setString(6, String.valueOf(duenger.getKalium()));
             pstmt.executeUpdate();
         }
     
         catch (Exception e) {}
         
         finally{this.CloseDBConnection();}
     }
     
     public boolean UsernameExists (String username) throws Exception {
         try {
             con = ConnectDB();
             pstmt = con.prepareStatement("SELECT * FROM tbl_benutzer WHERE username = (?);",ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
             pstmt.setString(1, username);
             rslt = pstmt.executeQuery();
             
             while (rslt.next()) {
                 if (rslt.getString(2).equals(username)) {
                 return true;
                 }
             }       
         }
         
         catch (Exception e) {
             
         }
         
         finally {
             this.CloseDBConnection();
         }
     return false;
     }
     
     public Duenger getDuengerInfo(String name) throws Exception{
         con = ConnectDB();
         pstmt = con.prepareStatement("SELECT * FROM tbl_duenger WHERE name like (?);");
         pstmt.setString(1, name);
         rslt = pstmt.executeQuery();
         
         if(rslt.next()){   
             Duenger d =  new Duenger(rslt.getString(2), rslt.getInt(3), rslt.getInt(4), rslt.getInt(5), rslt.getInt(6));
             this.CloseDBConnection();
             return d;
             }
         
         else {
             this.CloseDBConnection();
             return null;
             } 
 
     }
     
     public ArrayList getDuengerList() throws Exception{
     con = ConnectDB();
     pstmt = con.prepareStatement("SELECT name FROM tbl_duenger ORDER BY name");
     rslt = pstmt.executeQuery();
     
     ArrayList DuengerList= new ArrayList();
     while (rslt.next()) 
         {DuengerList.add(rslt.getString(1));}
     
     this.CloseDBConnection();
     return DuengerList;
     }
     
     public ArrayList<Object[]> getEreignissHoeheList(String name) throws Exception{
     con = ConnectDB();
     pstmt = con.prepareStatement("SELECT * FROM pflanzen_hoehe WHERE sorte like (?);");
     pstmt.setString(1, name);
     rslt = pstmt.executeQuery();
     
 
     ArrayList<Object[]> PflanzenHoeheList= new ArrayList();
     while (rslt.next()) 
         {
         ArrayList tmp = new ArrayList();
         tmp.add(convertDBDate(rslt.getString(2)));
         tmp.add("Höhen Messung");
         tmp.add("/");
         tmp.add("/");
         tmp.add(rslt.getString(3));
         PflanzenHoeheList.add(tmp.toArray());
         tmp.clear();
         }
     
     this.CloseDBConnection();
     return PflanzenHoeheList;
     }
     
     private String convertDBDate(String db)
     {
     String[] datum=db.split("-");
     return datum[2]+"."+datum[1]+"."+datum[0];
     }
     
     public Object[][] getTblPflanzenFromDB() throws Exception{
     con = ConnectDB();
     
     // erzeuge Statement:
     StringBuilder sb = new StringBuilder();
     sb.append("SELECT s.sorte, a.art, a.herkunft, e.datum_aussaat  ");
     sb.append("FROM tbl_pflanzen p, tbl_art a, tbl_sorte s, tbl_ereignisse e ");
     sb.append("WHERE a.ID = p.art_fk ");
     sb.append("AND s.ID = p.sorte_fk ");
     sb.append("AND e.ID = p.ereignisse_fk ");
     
     String sql = sb.toString();
     
     pstmt = con.prepareStatement(sql);
     rslt = pstmt.executeQuery();
 
     ArrayList row = new ArrayList();
     ArrayList<Object[]> column = new ArrayList();
     
     while (rslt.next()) {
         for(int i=1 ; i<=4;i++){    
             if(i==4){row.add(convertDBDate(String.valueOf(rslt.getObject(i))));}
             else {row.add(rslt.getObject(i));}
             }   
         column.add(row.toArray());
         row.clear();
         }
     
     Object[][] result = new Object[column.size()][4];
     for(int i=0;i<column.size();i++)
         {result[i]=column.get(i);}
 
     this.CloseDBConnection();
     return result;
     }
     
     
     public void CheckLogOn(String name, String pw) throws Exception {
         try{
             con = ConnectDB();
             pstmt = con.prepareStatement("SELECT * FROM tbl_benutzer WHERE username like (?) AND pass like (?);", ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
             pstmt.setString(1, name);
             pstmt.setString(2, pw);  
            
             boolean test = pstmt.execute();
             
             rslt = pstmt.executeQuery();
         
             // if Table size is >=1 -> username with this password 
             if(rslt.next()){userValidity = true;}
             
             /* //
             while (rslt.next()) {
                 System.out.println(rslt.getString(1));
                 if (rslt.getString(1).equals(name) && rslt.getString(2).equals("test")) {
                 userValidity = true;
                 }
             }*/
         }
         catch (Exception e) {System.out.println(e.getMessage());
             
         }
         finally {
             this.CloseDBConnection();
         }
     }
 }
