 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package database;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import static org.junit.Assert.*;
 
 /**
  *
  * @author Julian
  */
 public class MysqlConnectTest {
     
     public MysqlConnectTest() {
     }
     
     @BeforeClass
     public static void setUpClass() {
     }
     
     @AfterClass
     public static void tearDownClass() {
     }
     
     @Before
     public void setUp() {
     }
     
     @After
     public void tearDown() {
     }
 
     /**
      * Beispielstatements
      */
     @Test
     public void testWrapper() {
         System.out.println("connect");
         MysqlConnect instance = new MysqlConnect();   //voodoo             
         // Verbindung erstellen
        instance.connect(); 
        //instance.connectLokal(); // bitte auskommentieren
         // datenbank test verwenden
        instance.otherStatements("use mydb;");
         // Tabelle löschen
         instance.otherStatements("drop table lehrer");
         // Tabelle erstellen
         instance.otherStatements("create table lehrer (id INT, name varchar(1000))ENGINE=InnoDB DEFAULT CHARSET=utf8 ;");
         // Werte in Tabelle einfügen
         instance.issueInsertOrDeleteStatement("insert into lehrer (id,name) values (?,?)", 1,"hello");
         // Werte in Tabelle einfügen (nochmal)
         instance.issueInsertOrDeleteStatement("insert into lehrer (id,name) values (?,?)", 2,"hello2");
         // Werte in Tabelle einfügen (nochmal)
         instance.issueInsertOrDeleteStatement("insert into lehrer (id,name) values (?,?)", 3,"hello3");
         // update ausführen
         instance.issueUpdateStatement("update lehrer set name = ? where id = ?", "updatedhello", 1);
         // delete ausführen
         int i = 5;
         
         instance.issueInsertOrDeleteStatement("delete from lehrer where id = ?", 2);
         // select ausführen
         VereinfachtesResultSet result = instance.issueSelectStatement("select id, name from lehrer");                
         // resultset auslesen
         ArrayList<IdNamePair> ergebnisAlsArray = new ArrayList<IdNamePair>();        
         while (result.next()) {                        
             ergebnisAlsArray.add(new IdNamePair(result.getInt("id"), result.getString("name")));
         }                        
         instance.close();
         // Ergebnis ausgeben
         for(IdNamePair idNamePair : ergebnisAlsArray) {
             System.out.println("Die ID lautet: " + idNamePair.getId());
             System.out.println("Der Name lautet: " + idNamePair.getName());
         }
     }
 
 }
