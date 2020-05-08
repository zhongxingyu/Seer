 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package treintayplaya;
 
 import java.sql.Connection;
 import java.sql.SQLException;
import javax.swing.JOptionPane;
 
 /**
  *
  * @author administrador
  */
 public class Conexion {
     private static Conexion instance = null;
     public Connection cnx = null;
     
     public Conexion(){
         updateConnection();
     }
     
     private void updateConnection(){
 		String baseURL = Configuracion.getInstance().baseURL;
 		String baseUsr = Configuracion.getInstance().baseUsr;
 		String basePass = Configuracion.getInstance().basePass;
 		cnx = getNewConnection(baseURL, baseUsr, basePass);
 	}
 
 	public static Connection getNewConnection(String baseURL, String baseUsr, String basePass){
 		Connection conn = null;
         try {
 			Class.forName("org.gjt.mm.mysql.Driver"); 
 			conn = java.sql.DriverManager.getConnection("jdbc:mysql://" + baseURL, baseUsr, basePass);
         } catch (Exception ex) {
            String msj = "Error al conectarse con la base de datos. El programa deberá cerrarse. \n\n\n" + ex.toString();
            JOptionPane.showMessageDialog(null, msj, "Error en conexión", JOptionPane.ERROR_MESSAGE, null);
            System.exit(1);
         }
 		return conn;
 	}
     
     public static synchronized Conexion getInstance() {
         if (instance == null) {
             instance = new Conexion();
         }
         return instance;
     }
 
     public Connection getConnection() {
         if (cnx == null) {
             this.updateConnection();
         }
         try {
             if (cnx.isClosed()) {
                 this.updateConnection();
             }
         } catch (SQLException ex) {
             return null;
         }
         return cnx;
     }
     
 }
