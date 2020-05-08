 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package pkgOnlineShop;
 
 import java.sql.*;
 import java.util.ArrayList;
 import java.util.List;
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.SessionScoped;
 import javax.naming.Context;
 import javax.naming.InitialContext;
 import javax.sql.DataSource;
 
 /**
  *
  * @author matthias
  */
 
 @ManagedBean	
 @SessionScoped
 public class Database {
     Connection con = null;
     
     public Database() throws Exception {
         Context ctx = new InitialContext();
         Context envContext = (Context) ctx.lookup("java:/comp/env");
         DataSource ds = (DataSource) envContext.lookup("jdbc/resOnlineShop");
         con = ds.getConnection();
     }
     
     public List<Bestellung> getOrders(Person p) throws Exception
     {
         List<Bestellung> orders = new ArrayList<Bestellung>();
         
         String sql="select * from bestellung where p_nr = ?";        
         PreparedStatement pstm = con.prepareStatement(sql);            
         pstm.setInt(1,p.getId());
         ResultSet rs = pstm.executeQuery();
               
         while(rs.next())
         {
            Bestellung b = new Bestellung(rs.getInt("bid"),rs.getDate("orderdate"),rs.getString("creditcard"),
                    rs.getString("cvc"),rs.getString("cmonth"),rs.getString("cyear"),p,rs.getDate("shipped"));
            orders.add(b);
         }
                
         return orders;
     }
     
     public float getGesamtpreis(int id) throws Exception
     {
         float price=0;
         float partial_price=0;
         String sql="select * from orderproducts where bid=?";  
         
         PreparedStatement pstm = con.prepareStatement(sql);            
         pstm.setInt(1,id);
         
         ResultSet rs = pstm.executeQuery();
               
         while(rs.next())
         {
           String sqlp="select * from produkt where pr_id=?";  
         
           PreparedStatement pstmp = con.prepareStatement(sqlp);      
           pstmp.setInt(1,rs.getInt("pr_id"));
           ResultSet rsp = pstmp.executeQuery();
           rsp.next();
           
           partial_price = rs.getInt("menge") * rsp.getFloat("preis");
           price+=partial_price;
         }
         
         return price;
     }
     
     public List<Kategorie> getKategorien() throws Exception
     {
         List<Kategorie> kategorien = new ArrayList<Kategorie>();
         String sql="select * from kategorie";
         
         PreparedStatement pstm = con.prepareStatement(sql);            
         ResultSet rs = pstm.executeQuery();
         
         Kategorie k = null;        
         while(rs.next())
         {
             k = new Kategorie(rs.getInt("KAT_ID"),rs.getString("KAT_BEZEICHNUNG"));
             kategorien.add(k);
         }
         
         return kategorien;
     }
     
     public Person tryLogin(String login, String pw) throws Exception
     {
         Person p = null;
         String sql="select * from person where email=? and pass=?";
         
         PreparedStatement pstm = con.prepareStatement(sql);    
         pstm.setString(1, login);
         pstm.setString(2, pw);
         ResultSet rs = pstm.executeQuery();
         
         while(rs.next())
         {
             p = new Person(rs.getInt("p_id"),rs.getString("vorname"),rs.getString("nachname"),
                     rs.getString("strasse"),rs.getInt("hausnr"),rs.getInt("plz"),
                     rs.getString("ort"),rs.getString("land"),rs.getString("email"),
                     rs.getString("pass"),rs.getInt("anbieter"));
         }
         
         return p;
     }
     
     public void register(String vorname, String nachname, String strasse, int hausnr, int plz, String ort, String land, String email, String pass) throws Exception
     {
         String sql="insert into person(p_id,vorname,nachname,strasse,hausnr,plz,ort,land,email,pass,anbieter) values(SEQ_PERSON.nextval,?,?,?,?,?,?,?,?,?,0)";
         
         PreparedStatement pstm = con.prepareStatement(sql);   
         
         pstm.setString(1,vorname);
         pstm.setString(2,nachname);
         pstm.setString(3,strasse);
         pstm.setInt(4,hausnr);
         pstm.setInt(5,plz);
         pstm.setString(6,ort);
         pstm.setString(7,land);
         pstm.setString(8,email);
         pstm.setString(9,pass);
         
         pstm.execute();
     }
     
     public List<Person> getPersonen() throws Exception
     {
         List<Person> persons = new ArrayList<Person>();
         
         String sql="select * from person where anbieter=0";
         
         PreparedStatement pstm = con.prepareStatement(sql);    
         ResultSet rs = pstm.executeQuery();
         
         Person p;
         while(rs.next())
         {
             p = new Person(rs.getInt("p_id"),rs.getString("vorname"),rs.getString("nachname"),
                     rs.getString("strasse"),rs.getInt("hausnr"),rs.getInt("plz"),
                     rs.getString("ort"),rs.getString("land"),rs.getString("email"),
                     rs.getString("pass"),rs.getInt("anbieter"));
             persons.add(p);
         }
         
         return persons;
     }
     
     public List<Produkt> getProducts() throws Exception
     {
         String sql = "select * from produkt";
         List<Produkt> products = new ArrayList<Produkt>();
         
         PreparedStatement pstm = con.prepareStatement(sql);    
         ResultSet rs = pstm.executeQuery();
         
         Produkt p;
         while(rs.next())
         {
             p = new Produkt(rs.getInt("pr_id"),rs.getString("bezeichnung"),rs.getInt("preis"),rs.getString("kat"),
                     rs.getString("beschreibung"),rs.getInt("bestand"),rs.getString("bild"));
             
             products.add(p);
         }
         
         return products;
     }
     
     public List<Warenkorb> getWarenkorb(Person person) throws Exception{
         String sql = "SELECT produkt.pr_id, produkt.bezeichnung, produkt.preis, produkt.bild, "+
                      "produkt.beschreibung, produkt.bestand, produkt.kat, wk_id, quantity FROM warenkorb "+
                      "JOIN produkt ON( produkt.pr_id = warenkorb.pr_id) "+
                      "where warenkorb.p_id = ?";
         List<Warenkorb> warenkorb = new ArrayList<Warenkorb>();
         
         PreparedStatement pstm = con.prepareStatement(sql);
         pstm.setInt(1, person.getId());
         ResultSet rs = pstm.executeQuery();
         Produkt produkt;
         while(rs.next()){
             produkt = new Produkt(rs.getInt("pr_id"), rs.getString("bezeichnung"), rs.getInt("preis"), 
                    rs.getString("kat"), rs.getString("beschreibung"),rs.getInt("bestand"), rs.getString("bild"));
             warenkorb.add(new Warenkorb(rs.getInt("wk_id"),rs.getInt("quantity"),produkt,person));
         }
         
         return warenkorb;
     }
     
     public void deleteWarenkorb(int id) throws Exception{
         String sql = "delete from warenkorb where wk_id = ?";
         PreparedStatement pstm = con.prepareCall(sql);
         pstm.setInt(1, id);
         pstm.execute();
     }
     
     public void deleteUser(int id) throws Exception
     {
         String sql="delete from person where p_id=?";
         
         PreparedStatement pstm = con.prepareStatement(sql);    
         pstm.setInt(1,id);
         pstm.execute();
     }
     
     public void newCategory(String name) throws Exception
     {
         String sql="insert into kategorie(kat_id,kat_bezeichnung) values(seq_kategorie.nextval,?)";
         
         PreparedStatement pstm = con.prepareStatement(sql);    
         pstm.setString(1,name);
         pstm.execute();
     }
     
     public void deleteCategory(String name) throws Exception
     {
         String sql="delete from kategorie where kat_bezeichnung=? ";
         
         PreparedStatement pstm = con.prepareStatement(sql);    
         pstm.setString(1,name);
         pstm.execute();
     }
     
     public void newProduct(String bezeichnung,int preis,String kategorie,String beschreibung,int bestand,String imagePath) throws Exception
     {
         String sql="insert into produkt values(seq_produkt.nextval,?,?,?,?,?,?)";
         
         PreparedStatement pstm = con.prepareStatement(sql);
         pstm.setString(1,bezeichnung);
         pstm.setFloat(2,preis);
         pstm.setFloat(3,bestand);
         pstm.setString(4,imagePath);
         pstm.setString(5,kategorie);
         pstm.setString(6,beschreibung);
         
         pstm.execute();
     }
     
     public void deleteProduct(int id) throws Exception
     {
         String sql="delete from produkt where pr_id=?";
         
         PreparedStatement pstm = con.prepareStatement(sql);
         pstm.setInt(1,id);
         pstm.execute();
     }
     
     public ArrayList<Produkt> getProductsForCategory(String cat) throws Exception {
         ArrayList<Produkt> products = new ArrayList<Produkt>();
         
         String sql = "SELECT * FROM produkt WHERE kat like ? OR pr_id=3";
         PreparedStatement pstmt = con.prepareStatement(sql);
         pstmt.setString(1, cat);
         ResultSet rs = pstmt.executeQuery();
         
         while(rs.next()) {
             products.add(new Produkt(rs.getInt("pr_id"),rs.getString("bezeichnung"),rs.getInt("preis"),rs.getString("kat"),
                     rs.getString("beschreibung"),rs.getInt("bestand"),rs.getString("bild")));
         }
         
         return products;
     }
 
     public void updateProduct(Produkt p) throws Exception
     {
         String sql="update produkt set bezeichnung=?,preis=?,bestand=?,beschreibung=?,bild=?,kat=? where pr_id=?";
         
         PreparedStatement pstm = con.prepareStatement(sql);
         pstm.setString(1,p.getBezeichnung());
         pstm.setFloat(2,p.getPreis());
         pstm.setInt(3,p.getBestand());
         pstm.setString(4,p.getBeschreibung());
         pstm.setString(5,p.getBild());
         pstm.setString(6,p.getKategorie());
         pstm.setInt(7,p.getId());
         
         pstm.execute();
     }
     
     public void fillOrders(List<Warenkorb> waren, String knr, String cvc, String monat, String jahr,int pid) throws SQLException{
         String sql="insert into bestellung values(seq_bestellung.nextval,?,?,?,?,?,?,?)";
         java.util.Date curdate = new java.util.Date();
         java.sql.Date date = new java.sql.Date(curdate.getTime());
         
         PreparedStatement pstm = con.prepareStatement(sql);
         pstm.setInt(1,pid);
         pstm.setDate(2,date);
         pstm.setDate(3,null);
         pstm.setString(4,knr);
         pstm.setString(5,cvc);
         pstm.setString(6,monat);
         pstm.setString(7,jahr);
         pstm.execute();
         
         pstm = con.prepareStatement("select seq_bestellung.currval from dual");
         ResultSet res = pstm.executeQuery();
         
         res.next();
         int bid = res.getInt("currval");
         
         for(Object o:waren)
         {
             Warenkorb w = (Warenkorb)o;
             
             sql="insert into orderproducts values(?,?,?)";
             pstm = con.prepareStatement(sql);
             pstm.setInt(1,w.getProdukt().getId());
             pstm.setInt(2,bid);
             pstm.setInt(3,w.getQuantity());
             pstm.execute();
             
             this.cleanCart(w.getProdukt().getId());
         }
     }
     
     public void cleanCart(int id) throws SQLException
     {
         String sql="delete from warenkorb where wk_id=?";       
         PreparedStatement pstm = con.prepareStatement(sql);
         pstm.setInt(1,id);
         pstm.execute();
     }
     
     public List getBestellungen(Person person) throws SQLException{
       return null;
     }
 
     public void updatePerson(int id, String vorname, String nachname, String strasse, String hausnr, String plz, String ort, String land, String email, String pass) throws SQLException {
         String sql="update person set vorname=?,nachname=?,strasse=?,hausnr=?,plz=?,ort=?,land=?,email=?,pass=? where p_id=?";
         
         PreparedStatement pstm = con.prepareStatement(sql);
         pstm.setString(1,vorname);
         pstm.setString(2, nachname);
         pstm.setString(3,strasse);
         pstm.setString(4, hausnr);
         pstm.setString(5, plz);
         pstm.setString(6,ort);
         pstm.setString(7, land);
         pstm.setString(8, email);
         pstm.setString(9, pass);
         pstm.setInt(10, id);
         
         pstm.execute();
     }
 
 	public void addToCart(int amount, int pr_id, int p_id) throws Exception {
 		String sql = "INSERT INTO warenkorb VALUES(seq_warenkorb.nextval,?,?,?,1)";
 		
 		PreparedStatement pstmt = con.prepareStatement(sql);
 		
 		pstmt.setInt(1, amount);
 		pstmt.setInt(2, pr_id);
 		pstmt.setInt(3, p_id);
 		
 		pstmt.execute();
 		
 	}
 
 	public ArrayList<Rechnung> getRechnungenForCustomer(Person p) throws Exception {
 		ArrayList<Rechnung> rechnungen = new ArrayList<Rechnung>();
 		
 		String sql = "SELECT r.r_id, r.r_datum FROM rechnung r " +
 				"JOIN bestellung b ON (r.r_id = b.r_id) " +
 				"WHERE b.p_id=? " +
 				"GROUP BY r.r_id,r.r_datum";
 		
 		PreparedStatement pstmt = con.prepareStatement(sql);
 		pstmt.setInt(1, p.getId());
 		ResultSet rs = pstmt.executeQuery();
 		
 		while(rs.next()) {
 			rechnungen.add(
 						new Rechnung(
 									rs.getInt(1),
 									new java.util.Date(rs.getDate(2).getTime()) 
 								)
 					);
 		}
 		
 		return rechnungen;
 	}
 
 	public String getRechnungssumme(Bestellung b) throws Exception {
 		String retVal = "";
 		
 		String sql = "SELECT sum(p.preis) FROM orderproducts op " +
 				"JOIN produkt p ON (op.pr_id = p.pr_id) " +
 				"WHERE op.bid=?";
 		
 		PreparedStatement pstmt = con.prepareStatement(sql);
 		pstmt.setInt(1, b.getId());
 		ResultSet rs = pstmt.executeQuery();
 		
 		if(rs.next()) {
 			retVal = Integer.toString(rs.getInt(1));
 		}
 		
 		return retVal;
 	}
 
 	public String getQuantityForProduct(Produkt p,Bestellung b) throws Exception {
 		String retVal = "";
 		
 		String sql = "SELECT menge FROM orderproducts WHERE pr_id=? and bid=?";
 		
 		PreparedStatement pstmt = con.prepareStatement(sql);
 		pstmt.setInt(1, p.getId());
                 pstmt.setInt(2, b.getId());
 		ResultSet rs = pstmt.executeQuery();
 		
 		if(rs.next()) {
 			retVal = Integer.toString(rs.getInt("menge"));
 		}
 		
 		return retVal;
 	}
 
 	public ArrayList<Produkt> getProductsForSelectedBestellung (Bestellung selectedBestellung) throws Exception {
 		ArrayList<Produkt> produkte = new ArrayList<Produkt>();
 		
 		String sql = "SELECT p.pr_id, p.bezeichnung, p.preis, p.beschreibung, p.bestand, p.bild, p.kat " +
 				"FROM produkt p " +
 				"JOIN orderproducts op ON (p.pr_id = op.pr_id) " +
 				"WHERE op.bid=?";
 		
 		PreparedStatement pstmt = con.prepareStatement(sql);
 		pstmt.setInt(1, selectedBestellung.getId());
 		ResultSet rs = pstmt.executeQuery();
 		
 		while(rs.next()) {
 			Produkt pr = new Produkt(
 						rs.getInt("pr_id"),
 						rs.getString("bezeichnung"),
 						rs.getInt("preis"),
 						rs.getString("kat"),
 						rs.getString("beschreibung"),
 						rs.getInt("bestand"),
 						rs.getString("bild")
 					);
                         System.out.println(pr.toString());
 			produkte.add(pr);
 		}
 		
 		return produkte;
 	}
 
 	public ArrayList<Produkt> getProductsForSearch(String search) throws Exception {
 		ArrayList<Produkt> products = new ArrayList<Produkt>();
 		
 		String sql = "SELECT * FROM produkt WHERE bezeichnung like ? OR pr_id=3";
 		PreparedStatement pstmt = con.prepareStatement(sql);
 		pstmt.setString(1, "%" + search + "%");
 		ResultSet rs = pstmt.executeQuery();
 		
 		while(rs.next()) {
 			
             products.add(new Produkt(
                         rs.getInt("pr_id"),
                         rs.getString("bezeichnung"),
                         rs.getInt("preis"),
                         rs.getString("kat"),
                         rs.getString("beschreibung"),
                         rs.getInt("bestand"),
                         rs.getString("bild")
                     ));
         }
 		
 		return products;
 	}
 
 	public ArrayList<Produkt> getRandomProducts() throws Exception {
 		ArrayList<Produkt> products = new ArrayList<Produkt>();
 		
 		String sql = "SELECT * " +
 				"FROM (SELECT * " +
 				"FROM produkt " +
 				"ORDER BY dbms_random.VALUE) " +
 				"WHERE ROWNUM < 4";
 		
 		PreparedStatement pstmt = con.prepareStatement(sql);
 		ResultSet rs = pstmt.executeQuery();
 		
 		while(rs.next()) {
 			
             products.add(new Produkt(
                         rs.getInt("pr_id"),
                         rs.getString("bezeichnung"),
                         rs.getInt("preis"),
                         rs.getString("kat"),
                         rs.getString("beschreibung"),
                         rs.getInt("bestand"),
                         rs.getString("bild")
                     ));
         }
 		
 		return products;
 	}
 
     boolean isProductAvailable(Produkt selectedProduct, int amount) throws Exception {
         boolean retVal = false;
         
         String sql = "SELECT bestand FROM produkt WHERE pr_id=?";
         
         PreparedStatement pstmt = con.prepareStatement(sql);
         pstmt.setInt(1, selectedProduct.getId());
         ResultSet rs = pstmt.executeQuery();
         
         if(rs.next()) {
             if(rs.getInt("bestand") >= amount) {
                 retVal = true;
             }
         }
         
         return retVal;
     }
 }
     
