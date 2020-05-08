 package sql;
 import impl.GruppeImpl;
 import impl.KindImpl;
 import impl.KitaImpl;
 import interfaces.Gruppe;
 import interfaces.Kind;
 import interfaces.Kita;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Savepoint;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 /*
  *	@author Anton Romanov
 *	@date 11.10.2012
 *	@version 1.2
  */
 
 public class DBConnectorImpl {
 	
 	private final String USERNAME;
 	private final String PASSWORD;
 	private Connection con;
 	private final String URL="jdbc:oracle:thin:@ora.informatik.haw-hamburg.de:1521:inf09";
 	
 	private DBConnectorImpl(String user, String password){
 		this.PASSWORD = password;
 		this.USERNAME = user;
 	}
 	
 	public static DBConnectorImpl valueOf(String user, String password){
 		return new DBConnectorImpl(user, password);
 	}
 	
 	public boolean connect(){
 		try {
 			DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());
 			setConnection(DriverManager.getConnection(URL, getUser(), getPassword()));
 			getConn().setAutoCommit(false);
 		} catch (SQLException e) {
 			System.err.println(e.getMessage());
 			return false;
 		}
 		return true;
 	}
 	
 	private String getUser(){
 		return this.USERNAME;
 	}
 	
 	private String getPassword(){
 		return this.PASSWORD;
 	}
 	
 	private synchronized Connection getConn(){
 		return this.con;
 	}
 	
 	private synchronized void setConnection(Connection con){
 		this.con=con;
 	}
 	
 	public boolean disconnect(){
 		try {
 			getConn().close();
 		} catch (SQLException e) {
 			e.printStackTrace();
 			return false;
 		}
 		return true;
 	}
 	
 	private ResultSet executeStatement(String s) throws SQLException{
 		Statement st = getConn().createStatement();
 		return st.executeQuery(s);
 	}
 
 	/*
 	 * API
 	 */
 	
 	public Gruppe getGruppeByKindID(int id) throws SQLException{
 		String query_gruppeID = "select g.ID, g.Bezeichnung as Gbez, t.Bezeichnung as Tbez from KindGruppe, Gruppe g, Tageszeit t where Kind=? and Gruppe=g.ID and g.tageszeit = t.ID";
 		PreparedStatement ps = getConn().prepareStatement(query_gruppeID);
 		ps.setInt(1, id);
 		ResultSet rs = ps.executeQuery();
 		int gruppe_id = -1;
 		String bezeichnung = "";
 		String tageszeit = "";
 		while(rs.next()){
 			gruppe_id = rs.getInt("ID");
 			bezeichnung = rs.getString("Gbez");
 			tageszeit = rs.getString("Tbez");
 		}
 		return new GruppeImpl(bezeichnung, gruppe_id, tageszeit);
 	}
 	
 	public Gruppe getGruppeByKind(Kind k) throws SQLException{
 		return getGruppeByKindID(k.getId());
 	}
 	
 	public Kita getKitaByKindID(int id) throws SQLException{
 		Gruppe g = getGruppeByKindID(id);
 		String query = "select k.ID as KID, k.Bezeichnung as Kbez from Gruppe, Kita k where Gruppe.ID=?";
 		PreparedStatement ps = getConn().prepareStatement(query);
 		ps.setString(1, String.valueOf(g.getId()));
 		ResultSet rs = ps.executeQuery();
 		String bezeichnung = "";
 		int kita_id = -1;
 		while(rs.next()){
 			kita_id = rs.getInt("KID");
 			bezeichnung = rs.getString("Kbez");
 		}
 		return new KitaImpl(bezeichnung, kita_id);
 	}
 	
 	public Kita getKitaByKind(Kind k) throws SQLException{
 		return getKitaByKindID(k.getId());
 	}
 	
 	public Map<Integer, Kita> getKitas() throws SQLException {
 		Map<Integer, Kita> kitas = new HashMap<Integer, Kita>();
 		String query = "SELECT ID, Bezeichnung FROM Kita";
 		ResultSet rs = executeStatement(query);
 		while (rs.next()) {
 			Integer id = rs.getInt("ID");
 			String name = rs.getString("Bezeichnung");
 			kitas.put(id, new KitaImpl(name, id));
 		}
 		return kitas;
 	}
 	
 	public Map<Integer, Gruppe> getGruppenByKitaID(int kitaID) throws SQLException {
 		Map<Integer, Gruppe> gruppen = new HashMap<Integer, Gruppe>();
 		String query = "SELECT Gruppe.ID as GID, Gruppe.Bezeichnung as GBez, Tageszeit.Bezeichnung as Tbez FROM Gruppe JOIN Tageszeit ON Gruppe.Tageszeit = Tageszeit.ID WHERE Kita = ?";
 		PreparedStatement ps = getConn().prepareStatement(query);
 		ps.setInt(1, kitaID);
 		ResultSet rs = ps.executeQuery();
 		while (rs.next()) {
 			Integer id = rs.getInt("GID");
 			String name = rs.getString("GBez");
 			String zeit = rs.getString("Tbez");
 			gruppen.put(id, new GruppeImpl(name, id, zeit));
 		}
 		return gruppen;
 	}
 	
 	public Map<Integer, Gruppe> getGruppenByKita(Kita k) throws SQLException {
 		return getGruppenByKitaID(k.getId());
 	}
 	
 	public Map<Integer, Kind> getKinder(int gruppeID) throws SQLException {
 		Map<Integer, Kind> kinder = new HashMap<Integer, Kind>();
 		String query = "SELECT Vorname, Nachname, Gehalt, ID FROM Kind k, KindGruppe kg where k.ID = kg.Kind and kg.Gruppe=?";
 		PreparedStatement ps = getConn().prepareStatement(query);
 		ps.setInt(1, gruppeID);
 		ResultSet rs = ps.executeQuery();
 		while (rs.next()) {
 			String vname = rs.getString("Vorname");
 			String nname = rs.getString("Nachname");
 			double gehalt = rs.getDouble("Gehalt");
 			Integer id = rs.getInt("ID");
 			kinder.put(id, new KindImpl(vname, nname, gehalt, id));
 		}
 		return kinder;
 	}
 	
 	public boolean isPlatzFrei(int gruppeID) throws SQLException{
 		String query = "select count(*) as Anzahl from KindGruppe where Gruppe=?";
 		PreparedStatement ps = getConn().prepareStatement(query);
 		ps.setInt(1, gruppeID);
 		ResultSet rs = ps.executeQuery();
 		int anzahl = 30;
 		while (rs.next()) {
 			anzahl = rs.getInt("Anzahl");
 		}
 		return anzahl<30;
 	}
 	
 	public boolean isPlatzFrei(Gruppe g) throws SQLException{
 		return isPlatzFrei(g.getId());
 	}
 	
 	public double getPriceByKindID(int id) throws SQLException{
 		String query = "select getPriceByID(?) as Preis from dual";
 		PreparedStatement ps = getConn().prepareStatement(query);
 		ps.setInt(1, id);
 		ResultSet rs = ps.executeQuery();
 		double preis = Double.NaN;
 		while(rs.next()){
 			preis = rs.getDouble("Preis");
 		}
 		return preis;
 	}
 	
 	public double getPriceByKind(Kind k) throws SQLException {
 		return getPriceByKindID(k.getId());
 	}
 	
 	public double getPriceByValues(int famMitglieder, double gehalt, int dauerBetreueung) throws SQLException{
 		String query = "select getPriceByValues(?,?,?) as Preis from dual";
 		PreparedStatement ps = getConn().prepareStatement(query);
 		ps.setDouble(1, gehalt);
 		ps.setInt(2, dauerBetreueung);
 		ps.setInt(3, famMitglieder);
 		ResultSet rs = ps.executeQuery();
 		double preis = -1.0;
 		while(rs.next()){
 			preis = rs.getDouble("Preis");
 		}
 		return preis;
 	}
 	
 	
 	public Kind addKind(String vorame, String nachname, Calendar gDatum, double gehalt, int anzahlFamMit) throws SQLException{
 		String query = "insert into kind(id,vorname,nachname,Geburtsdatum,Gehalt, Familie) values(NULL,?,?,?,?,?)";
		String date = String.valueOf(gDatum.get(Calendar.DAY_OF_MONTH))+"."+String.valueOf(gDatum.get(Calendar.MONTH))+"."+String.valueOf(gDatum.get(Calendar.YEAR));
 		PreparedStatement ps = getConn().prepareStatement(query);
 		ps.setString(1, vorame);
 		ps.setString(2, nachname);
 		ps.setString(3, date);
 		ps.setDouble(4, gehalt);
 		ps.setInt(5, anzahlFamMit);
 		ps.executeQuery();
 		getConn().commit();
		String query_kind_id = "select max(id) from Kind";
 		int id = -1;
 		ResultSet rs =executeStatement(query_kind_id);
 		while(rs.next()){
 			id = rs.getInt("ID");
 		}
 		return getKindByID(id);
 	}
 	
 	public void eintragenInWarteliste(Kind k, Gruppe g) throws SQLException{
 		String query = "insert into Warteliste(Kind,Gruppe) values(?,?)";
 		PreparedStatement ps = getConn().prepareStatement(query);
 		ps.setInt(1, k.getId());
 		ps.setInt(2, g.getId());
 		ps.execute();
 		getConn().commit();
 	}
 	
 	public boolean addKindToGruppe(Kind k, Gruppe g){
 		String query = "insert into KindGruppe(Kind,Gruppe) values(?,?)";
 		PreparedStatement ps;
 		try {
 			ps = getConn().prepareStatement(query);
 			ps.setInt(1, k.getId());
 			ps.setInt(2, g.getId());
 			ps.execute();
 			getConn().commit();
 		} catch (SQLException e) {
 			e.printStackTrace();
 			return false;
 		}
 		return true;
 	}
 	
 	public boolean addKindToGruppe(String vorame, String nachname, Calendar gDatum, double gehalt, int anzahlFamMit, int gruppe_id){
		Savepoint savep=null;
 		try{
			savep = getConn().setSavepoint("Anfang");
 			Kind k = addKind(vorame, nachname, gDatum, gehalt, anzahlFamMit);
 //			String kind_query = "select max(ID) as ID from Kind";
 //			ResultSet rs = executeStatement(kind_query);
 //			int kind_id = -1;
 //			while(rs.next()){
 //				kind_id = rs.getInt("ID");
 //			}
			String query = "insert into KindGruppe(Kind,Gruppe) values(?,?)";
 			PreparedStatement ps = getConn().prepareStatement(query);
 			ps.setInt(1, k.getId());
 			ps.setInt(2, gruppe_id);
 			ps.execute();
 			getConn().commit();
 		} catch(SQLException e){
 			try {
				getConn().rollback(savep);
 				return false;
 			} catch (SQLException e1) {
 				e1.printStackTrace();
 			}
 		}
 		
 		return true;
 	}
 	
 	public boolean kindAbmelden(int kind_id, int gruppe_id){
 		Savepoint svp = null;
 		try {
 			svp = getConn().setSavepoint("KindAbmeldenAnfang");
 			String query_kg ="delete * from KindGruppe where Kind=? and Gruppe=?";
 			PreparedStatement ps = getConn().prepareStatement(query_kg);
 			ps.setInt(1, kind_id);
 			ps.setInt(2, gruppe_id);
 			return ps.execute();
 		} catch (SQLException e) {
 			e.printStackTrace();
 			try {
 				getConn().rollback(svp);
 			} catch (SQLException e1) {
 				// TODO Auto-generated catch block
 				e1.printStackTrace();
 			}
 			return false;
 		}
 	}
 	
 	public Kind getKindByID(int kindID) throws SQLException{
 		String query = "select Vorname, Nachname, Gehalt FROM Kind where id=?";
 		PreparedStatement ps = getConn().prepareStatement(query);
 		ps.setInt(1, kindID);
 		ResultSet rs = ps.executeQuery();
 		String vorname = "";
 		String nachname ="";
 		double gehalt = Double.NaN;
 		while(rs.next()){
 			vorname = rs.getString("Vorname");
 			nachname = rs.getString("Nachname");
 			gehalt = rs.getDouble("Gehalt");
 		}
 		return new KindImpl(vorname, nachname, gehalt, kindID);
 	}
 	
 	@Deprecated
 	public int getPlatzByKindID(int kindID) throws SQLException{
 		return 0;
 	}
 	
 	public Gruppe getGruppeByID(int gruppe_id) throws SQLException{
 		String query = "SELECT Gruppe.Bezeichnung as GBez, Tageszeit.Bezeichnung as TBez FROM Gruppe JOIN Tageszeit ON Gruppe.Tageszeit = Tageszeit.ID WHERE Gruppe.ID = ?";
 		PreparedStatement ps = getConn().prepareStatement(query);
 		ps.setInt(1, gruppe_id);
 		ResultSet rs = ps.executeQuery();
 		String bezeichnung = "";
 		String tageszeit = "";
 		while(rs.next()){
 			bezeichnung = rs.getString("GBez");
 			tageszeit = rs.getString("TBez");
 		}
 		return new GruppeImpl(bezeichnung, gruppe_id, tageszeit);
 	}
 	
 	
 	public Map<Gruppe,Integer> getWartelistePosition(int kindID) throws SQLException{
 		Map<Gruppe,Integer> result = new HashMap<Gruppe,Integer>();
 		String query_gruppen = "select Gruppe from Warteliste where Kind=?";
 		List<Integer> gruppen = new ArrayList<Integer>();
 		PreparedStatement ps = getConn().prepareStatement(query_gruppen);
 		ps.setInt(1, kindID);
 		ResultSet rs = ps.executeQuery();
 		while(rs.next()){
 			gruppen.add(rs.getInt("Gruppe"));
 		}
 		System.out.println(gruppen.toString());
 		for(Integer gruppe: gruppen){
 			String query = "select count(*) as Position from Warteliste where Gruppe=? and ID<=(select ID from Warteliste where Kind=? and Gruppe=?)";
 			PreparedStatement ps2 = getConn().prepareStatement(query);
 			ps2.setInt(1, gruppe);
 			ps2.setInt(2, kindID);
 			ps2.setInt(3, gruppe);
 			ResultSet rs2 = ps2.executeQuery();
 			while(rs2.next()){
 				result.put(getGruppeByID(gruppe), rs2.getInt("Position"));
 			}
 		}
 		return result;
 	}
 	
 	public int getWartelisteLaenge(int gruppe_id) throws SQLException{
 		int result = -1;
 		String query = "select count(*) as Anzahl from Warteliste where Gruppe=?";
 		PreparedStatement ps = getConn().prepareStatement(query);
 		ps.setInt(1, gruppe_id);
 		ResultSet rs = ps.executeQuery();
 		while(rs.next()){
 			result = rs.getInt("Anzahl");
 		}
 		return result;
 	}
 	
 }
