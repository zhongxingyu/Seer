 package drawler.match.sql;
 
 import drawler.match.pattern.Matchable;
 import drawler.match.Outputable;
 import java.sql.*;
 import java.util.*;
 
 public class SQLOutput implements Outputable {
 	
 	// 用户名
 	private String user;
 	
 	// 密码
 	private String password;
 	
 	// 数据库名
 	private String database;
 	
 	// 表名
 	private String table;
 	
 	// 数据库服务器地址
 	private String host;
 	
 	// 数据库服务器端口，默认为3306
 	private int port = 3306;
 	
 	private Map<String, String> columns;
 	private List<String> fields;
 	
 	private Connection con = null;
 	
 	// java连接mysql驱动名
 	final private String MYSQL_DRIVER = "com.mysql.jdbc.Driver";
 	
 	private String url;
 
 	public SQLOutput(String host, int port, String user, String password, String database, String table) {
 		init(host, port, user, password, database, table);
 	}
 	
 	public SQLOutput(String host, String user, String password, String database, String table) {
 		init(host, 3306, user, password, database, table);
 	}
 	
 	public void init(String host, int port, String user, String password, String database, String table) {
 		this.host = host;
 		this.port = port;
 		this.user = user;
 		this.database = database;
 		this.password = password;
 		this.table = table;
 		this.url =  "jdbc:mysql://" 
 			+ this.host + ":" 
 			+ this.port 
 			+ "/?useUnicode=true&characterEncoding=UTF8";
 		
 		if (this.isValid())
 			this.close();
 		
 		if (columns == null)
 			columns = new HashMap<String, String>();
 		if (fields == null)
 			fields = new Vector<String>();
 		columns.clear();
 		fields.clear();
 	}
 	
 	private boolean connect() {
 		try {
 			if (this.isValid())
 				this.close();
 			
 			Class.forName(MYSQL_DRIVER);
 			con = DriverManager.getConnection(url, user, password);
 			return isValid();
 		}
 		catch (Exception e) {
 			System.err.println(e.toString());
 			return false;
 		}
 	}
 	
 	private boolean isValid(){
 		boolean r;
 		try {
 			if (con == null)
 				return false;
 			r = !con.isClosed();
 		}
 		catch (SQLException e) {
 			System.err.println(e.toString());
 			r = false;
 		}
 		return r;
 	}
 	
 	private boolean isDatabaseExisted() {
 		if (!isValid()) 
 			return false;
 		try {
 			Statement state = con.createStatement();
 			String sql = 
 					"SELECT count(*) FROM INFORMATION_SCHEMA.SCHEMATA " +
 					"WHERE SCHEMA_NAME='" +
 					database +"'";
 			ResultSet rs = state.executeQuery(sql);
 			if (rs.next() && rs.getInt(1) >= 1) {
 				state.close();
 				return true;
 			}
 			else {
 				state.close();
 				return false;
 			}
 		}
 		catch (SQLException e) {
 			System.err.println(e.toString());
 			return false;
 		}
 	}
 	
 	private boolean removeDatabase() {
 		if (!isValid()) {
 			System.out.println("Connection error!");
 			return false;
 		}
 		if (!isDatabaseExisted()) {
 			System.out.println("Database " + database + " is not existed!");
 			return true;
 		}
 		
 		try {
 			Statement state = con.createStatement();
 			boolean r = state.execute("DROP DATABASE " + database);
 			state.close();
 			return r;
 		}
 		catch (SQLException e) {
 			System.err.println(e.toString());
 			return false;
 		}
 	}
 	
 	private boolean createDatabase() {
 		if (!isValid()) {
 			System.err.println("Connection error!");
 			return false;
 		}
 		if (isDatabaseExisted()) {
 			System.err.println("Database " + database + " already exists!");
 			return false;
 		}
 		
 		try {
 			Statement state = con.createStatement();
			state.execute("CREATE DATABASE " + database + " CHARACTER SET `utf8`");
 			state.close();
			return true;
 		}
 		catch (SQLException e) {
 			System.err.println(e.toString());
 			return false;
 		}
 	}
 	
 	private boolean isTableExisted() {		
 		if (!isValid())
 			return false;
 		try {
 			Statement state = con.createStatement();
 			String sql = 
 					"SELECT count(*) FROM INFORMATION_SCHEMA.TABLES " +
 					"WHERE TABLE_SCHEMA='" + database +"' AND " +
 					"TABLE_NAME='" + table + "'";
 			ResultSet rs = state.executeQuery(sql);
 			if (rs.next() && rs.getInt(1) >= 1) {
 				state.close();
 				return true;
 			}
 			else {
 				state.close();
 				return false;
 			}
 		}
 		catch (SQLException e) {
 			System.err.println(e.toString());
 			return false;
 		}
 	}
 	
 	private boolean removeTable() {
 		if (!isValid()) {
 			System.err.println("Connection error!");
 			return false;
 		}
 		if (!isTableExisted()) {
 			System.err.println("Table " + table + " is not existed!");
 			return true;
 		}
 		try {
 			Statement state = con.createStatement();
 			boolean r = state.execute(
 					"DROP TABLE " 
 					+ database +"." 
 					+ table);
 			state.close();
 			return r;
 		}
 		catch (SQLException e) {
 			System.err.println(e.toString());
 			return false;
 		}
 	}
 	
 	private boolean createTable() {
 		if (!isValid()) {
 			System.err.println("Connection error!");
 			return false;
 		}
 		if (isTableExisted()) {
 			System.err.println("TABLE " + table + " already existed!");
 			return false;
 		}
 		String sql = "";
 		try {
 			Statement state = con.createStatement();
 			sql = 
 					"CREATE TABLE " + database + "." + table + 
 					"(";
 			for (int i=0; i<fields.size(); i++) {
 				String key = fields.get(i);
 				sql +=  key + " " + (String)columns.get(key) +",";
 			}
 			sql += ")";
 			boolean r = state.execute(sql.replace(",)",")"));
 			state.close();
 			return true;
 		}
 		catch (SQLException e) {
 			System.err.println(e.toString());
 			System.out.println("SQL is:");
 			System.out.println(sql);
 			return false;
 		}
 	}
 
 	private boolean checkTable() {
 		String sql = "";
 		try {
 			Statement state = con.createStatement();
 			sql = "DESC " + database + "." + table;
 			ResultSet rs = state.executeQuery(sql);
 			int size = 0;
 			while (rs.next()) {
 				String field = rs.getString("Field");
 				String type = rs.getString("Type");
 				if (!this.columns.containsKey(field)) {
 					System.err.println("Table column " + field + " is undefined in any patterns.");
 					return false;
 				}
 				else if (this.columns.get(field).toLowerCase().indexOf(type) == -1 
 					&& type.indexOf(this.columns.get(field).toLowerCase()) == -1) {
 					System.err.println("Table column " + field + ":" + type + 
 							  " doesn't match defined patterns " + field + ":" + this.columns.get(field).toLowerCase() + ".");
 					return false;
 				}
 				size ++;
 			}
 			if (size != this.columns.size()) {
 				System.err.println("[Error] " + size + " columns defined in table, but " + this.columns.size() + " patterns defined.");
 				return false;
 			}
 		}
 		catch (Exception e) {
 			System.err.println(e.toString());
 			return false;
 		}
 		return true;
 	}
 	
 	@Override
 	public boolean open(List<Matchable> patterns) {
 
 		Iterator<Matchable> it = patterns.iterator();
 		this.columns.clear();
 		while (it.hasNext()) {
 			Matchable pattern = it.next();
 			this.columns.put(pattern.getTitle(), pattern.getDescription());
 			this.fields.add(pattern.getTitle());
 		}
 			
 		if (this.connect()) {
 			if (this.isDatabaseExisted() || this.createDatabase() ) {
 				if (this.isTableExisted() || this.createTable() ) {
 					if (!this.checkTable()) {
 						System.out.println("TABLE " + table + " is already existed in DATABASE " + database + ".");
 						System.out.println("And it's structure is inconsistent to match patterns.");
 						
 						this.close();
 						return false;
 					}
 					return true;
 				}
 				else
 					System.err.println("Table failed");
 			}
 			else
 				System.err.println("Database failed");
 		}
 
 		return false;
 	}
 
 	@Override
 	public boolean close() {
 		try {
 			if (this.isValid())
 				con.close();
 		}
 		catch (SQLException e) {
 			return false;
 		}
 		return true;
 	}
 
 	@Override
 	public void write(Map<String, String> object) {
 			
 		Iterator<Map.Entry<String,String>> it = object.entrySet().iterator();
 		String sql = "INSERT INTO " + database + "." + table + " ";
 		String c = "(";
 		String v = "(";
 		while (it.hasNext()) {
 			Map.Entry<String, String> entry = it.next();
 			String field = entry.getKey();
 			String value = entry.getValue();
 			c += field;
 			v += "'" + value + "'";
 			if (it.hasNext()) {
 				c += ",";
 				v += ",";
 			}
 			else {
 				c += ")";
 				v += ")";
 			}
 		}
 		sql += c + " VALUES " + v;
 		try {
 			Statement state = con.createStatement();
 			state.execute(sql);
 			state.close();
 		}
 		catch (SQLException e) {
 			System.err.println(e.toString());
 			System.err.println("SQL is: " + sql);
 		}
 	}
 }
