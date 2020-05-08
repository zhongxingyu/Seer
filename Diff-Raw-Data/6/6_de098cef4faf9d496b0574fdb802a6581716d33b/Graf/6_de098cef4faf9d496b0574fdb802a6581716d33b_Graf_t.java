 package ppc;
 
 import com.mysql.jdbc.Connection;
 import com.mysql.jdbc.Statement;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.util.HashMap;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 public class Graf {
     
     public Graf() {
         
     }
     
     private HashMap<IdentificatorStatie, Statie> tabelStatii = new HashMap<IdentificatorStatie, Statie>();
     
     private class IdentificatorStatie {
         
         public IdentificatorStatie(String nume_, String artera_) {
             nume = nume_;
             artera = artera_;
         }
        
        private String nume;
        private String artera;
     }
         
     // baga toate statiile din baza de date
     public void adaugaStatii() {
         try {
             Class.forName("com.mysql.jdbc.Driver");
             Connection conn = (Connection) DriverManager.getConnection("jdbc:mysql://localhost:3306/ppc", "root", "");
 
             // Calculeaza statiile
             Statement stmt = (Statement) conn.createStatement();
             String query = "select * from statii";
             ResultSet rs = stmt.executeQuery(query);
 
             int nrStatii = 0;
             while (rs.next()) {
                 String nume = rs.getString("nume");
                 String artera = rs.getString("artera");
                 int longitudine = rs.getInt("longitudine");
                 int latitudine = rs.getInt("longitudine");
 
                 ++nrStatii;
                 Statie statie = new Statie(nrStatii, nume, artera, longitudine, latitudine);
                 tabelStatii.put(new IdentificatorStatie(nume,artera), statie);
             }
             
             // Calculeaza legaturile
             stmt = (Statement) conn.createStatement();
             query = "select * from legaturi";
             rs = stmt.executeQuery(query);
 
             while (rs.next()) {
                 String statie1 = rs.getString("statie1");
                 String artera1 = rs.getString("artera1");
                 String statie2 = rs.getString("statie2");
                 String artera2 = rs.getString("artera2");
                 
                 Statie statie = tabelStatii.get(new IdentificatorStatie(statie1, artera1));
                 statie.adaugaLegatura(tabelStatii.get(new IdentificatorStatie(statie2, artera2)));
             }
             
             conn.close();
         } catch (Exception ex) {
             Logger.getLogger(Graf.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
     
     // baga toate traseele din baza de date
     public void adaugaTrasee() {
         try {
             Class.forName("com.mysql.jdbc.Driver");
             Connection conn = (Connection) DriverManager.getConnection("jdbc:mysql://localhost:3306/ppc", "root", "");
 
             // Calculeaza statiile
             Statement stmt = (Statement) conn.createStatement();
             
             for (int i=0; i<2; ++i) {
                 String query = "select * from linii where sens=" + i + " order by nr desc";
                 ResultSet rs = stmt.executeQuery(query);
 
                 int nrStatii = 0;
                 Statie start = null;
                 while (rs.next()) {
                     String numeStatie = rs.getString("numeStatie");
                     String artera = rs.getString("artera");
                     String linia = rs.getString("linia");
                     int nr = rs.getInt("nr");
                     int sens = rs.getInt("sens");
 
                     Statie destinatie = tabelStatii.get(new IdentificatorStatie(numeStatie, artera));
                     if (start != null) {
                         start.adaugaTransport(destinatie, linia);
                         start = destinatie;
                     }
                 }
             }
                     
             conn.close();
         } catch (Exception ex) {
             Logger.getLogger(Graf.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 
     // determina ruta
     public void getRuta(Statie start, Statie destinatie) {
         getRuta(start.id, destinatie.id);            
     }
 
     // determina ruta pt id-uri
     public void getRuta(int start, int destinatie) {
         // NOT IMPLEMENTED
     }
 
 }
