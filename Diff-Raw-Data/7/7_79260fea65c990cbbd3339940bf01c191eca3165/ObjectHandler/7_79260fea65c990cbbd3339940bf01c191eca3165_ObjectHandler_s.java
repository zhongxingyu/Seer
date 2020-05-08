 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package DB;
 
 import Model.Player;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import javax.swing.JOptionPane;
 
 /**
  *
  * @author Nicklas Larsen
  */
 public class ObjectHandler {
 
     private DBConnection db;
 
     public ObjectHandler(DBConnection db) {
         this.db = db;
     }
 
    public Player getPlayer(String Player) {
         Player p = null;
        String sql = "SELECT * FROM Player";
         ResultSet rs;
         System.out.println("sql");
         try {
             rs = db.getResult(sql);
             if (rs.next()) {
                 Player player;
                 player = new Player("Navn");
                 p = player;
             }
             rs.close();
         } catch (SQLException ex) {
             System.out.println(ex);
             JOptionPane.showMessageDialog(null, "Fejl ved hentning af oplysninge!r", "FEJL", 1);
         }
         return p;
     }
 }
