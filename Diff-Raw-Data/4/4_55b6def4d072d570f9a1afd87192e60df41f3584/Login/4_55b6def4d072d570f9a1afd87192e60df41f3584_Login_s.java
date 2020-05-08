 package database;
 
 import java.sql.*;
 
 /**
  *
  * @author i5a11
  */
 public class Login {
     Connection connessione;
     Query   login;
     
     public Login(Connection conn) throws Exception {
         this.connessione = conn;
         this.login = new Query(this.connessione);
     }
     
     public boolean login(String user, String pass) throws Exception {
         this.checkUser(user);
         ResultSet ris = this.login.esecuzioneQuery("SELECT * FROM \"UTENTE\" WHERE \"USERNAME\" = '" + user + "' AND \"PASS\" = '" + pass + "'");
         if (ris.next() == false){
             throw new Exception("Eccezione : Password errata") ;
         }
         return true;
     }
     
     public boolean checkUser(String user) throws Exception {
         ResultSet ris = this.login.esecuzioneQuery("SELECT * FROM \"UTENTE\" WHERE \"USERNAME\" = '" + user + "'");
         if (ris.next() == false){
             throw new Exception("Eccezione : Nome utente inesistente") ;
         }
        String ban = ris.getString("BAN");
        if (ban.equals("on")){
             throw new Exception("Eccezione : Utente bannato") ;
         }
         return true;
     }
 }
