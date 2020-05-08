 package nl.utwente.db.neogeo.preaggregate;
 
 import java.sql.*;
 
 public class SqlUtils {
 
 	protected enum DbType {
 		POSTGRES, 
 		MYSQL
 	}
 
 	protected static String databaseType(Connection c) throws SQLException {
 		return c.getMetaData().getDatabaseProductName();
 	}
 
 	protected static Connection cached_connection = null;
 	protected static DbType	 cached_dbtype = DbType.POSTGRES;
 
 	protected synchronized static DbType dbType(Connection c)
 	throws SQLException {
 		if (false) return DbType.MYSQL;
 		if (c == cached_connection)
 			return cached_dbtype;
 		else {
 			String s = databaseType(c);
 
 			cached_connection = c;
 			if (s.equals("PostgreSQL"))
 				return cached_dbtype = DbType.POSTGRES;
 			else if (s.equals("MySQL"))
 				return cached_dbtype = DbType.MYSQL;
 			cached_connection = null;
 			throw new SQLException("Unknown database type: " + s);
 		}
 	}
 
 	public static boolean existsTable(Connection c, String schema, String table)
 	throws SQLException {
 		boolean res;
 
 		Statement st;
 		ResultSet rs;
 
 		String sql = null;
 		switch ( dbType(c) ) {
 		case POSTGRES:
 			sql = "SELECT COUNT(*) from pg_tables WHERE schemaname=\'"
 				+ schema + "\' AND tablename=\'" + table + "\';";
 			break;
 		case MYSQL:
 			sql = "SELECT COUNT(*) from information_schema.Tables WHERE table_schema='"
 				+ schema + "\' AND table_name=\'" + table + "\';";
 			break;
 		}
 		st = c.createStatement();
 		rs = st.executeQuery(sql);
 		rs.next();
 		res = (rs.getInt(1) == 1);
 		rs.close();
 		st.close();
 		return res;
 	}
 
 	public static void executeSCRIPT(Connection c, String sql)
 	throws SQLException {
 		switch ( dbType(c) ) {
 		case POSTGRES:
 			executeNORES(c,sql);
 			break;
 		case MYSQL:
 			executeNORES(c,sql);
 			break;
 		}	
 	}
 
 	public static void executeNORES(Connection c, String sql)
 	throws SQLException {
 		Statement st = c.createStatement();
 		st.executeUpdate(sql, Statement.NO_GENERATED_KEYS);
 		st.close();
 	}
 
 	public static void dropTable(Connection c, String schema, String table)
 	throws SQLException {
 		executeNORES(c,"DROP TABLE "+schema+"."+table+";");
 	}
 
 	public static ResultSet execute(Connection c, String sql)
 	throws SQLException {
 		Statement st = c.createStatement();
 		ResultSet rs = st.executeQuery(sql);
 		return rs;
 	}
 
 	public static ResultSet execute_big_read(Connection c, String sql)
 	throws SQLException {
 		Statement st = c.createStatement(ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
 		st.setFetchSize(1);
 		ResultSet rs = st.executeQuery(sql);
 		return rs;
 	}
 
 
 	public static ResultSet get_result(Connection c, String resname,
 			String restable, int fromPos, int toPos) throws SQLException {
 		String sql;
 
 		String between = "";
 		if (fromPos >= 0)
 			between = " AND (pos BETWEEN " + fromPos + " AND " + toPos + ")";
 		sql = "SELECT * FROM " + restable + " WHERE resname=\'" + resname
 		+ "\'" + between + " ORDER BY pos;";
 		return execute(c, sql);
 	}
 
 	public static void insert_kv_tuple(Connection c, String schema,
 			String table, String key, String value) throws SQLException {
 		PreparedStatement statement = c.prepareStatement("INSERT INTO "
 				+ schema + "." + table + "  (k, v) " + "VALUES" + "  (?, ?);");
 		statement.setString(1, key);
 		statement.setString(2, value);
 		statement.executeUpdate();
 		statement.close();
 	}
 
 	public static int execute_1int(Connection c, String psql)
 	throws SQLException {
 		ResultSet rs = execute(c, psql);
 
 		if (!rs.next())
 			throw new SQLException("execute_1int: no result");
 		int res = rs.getInt(1);
 		return res;
 	}
 
 	public static long execute_1long(Connection c, String psql)
 	throws SQLException {
 		ResultSet rs = execute(c, psql);
 
 		if (!rs.next())
 			throw new SQLException("execute_1int: no result");
 		long res = rs.getLong(1);
 		return res;
 	}
 
 	public static long count(Connection c, String schema, String table,String column)
 	throws SQLException {
 		return execute_1long(c,"SELECT COUNT("+column+") FROM "+schema+"."+table+";");
 	}
 
 	public static String execute_1str(Connection c, String psql)
 	throws SQLException {
 		ResultSet rs = execute(c, psql);
 
 		if (!rs.next())
 			return null;
 		return rs.getString(1);
 	}
 
 	public static String execute_1str(Connection c, PreparedStatement st)
 	throws SQLException {
 		ResultSet rs = st.executeQuery();
 
 		if (!rs.next())
 			return null;
 		return rs.getString(1);
 	}
 
 	public static boolean has1result(Connection c, String psql)
 	throws SQLException {
 		ResultSet rs = execute(c, psql);
 
 		if (!rs.next())
 			return false;
 		return true;
 	}
 
 	public static void create_index(Connection c, String schema, String table, String kind, String column) throws SQLException {
 		SqlUtils.executeNORES(c,
 				"CREATE INDEX "+table+"_"+column+" on "+schema+"."+table+" USING "+kind+"("+column+");"
 		);
 	}
 
 	public static long queryCount(Connection c, String q) throws SQLException {
 		return execute_1long(c,"SELECT COUNT(*) FROM (" + q + ") as countExpr;");
 
 	}
 
 	public static String bbox(double x1,double y1, double x2, double y2, String srid) {
 		return "ST_SetSRID(ST_MakeBox2D(ST_Point("+x1+","+y1+")," + "ST_Point("+x2+","+y2+")),"+srid+")";
 	}
 
 	public static String bbox_linestr(double x1,double y1, double x2, double y2) {
 		return "LINESTRING("+x1+" "+y1+","+x2+" "+y1+","+x2+" "+y2+","+x1+" "+y2+","+x1+" "+y1+")";
 	}
 
 	/*
 	 * 
 	 * 
 	 */
 
 	public static String gen_DIV(Connection c, String l, String r) throws SQLException {
 		switch ( dbType(c) ) {
 		case POSTGRES:
 			return "DIV("+l+","+r+")";
 		case MYSQL:
 			return "("+l+") div ("+r+")";
 		}	
 		throw new SQLException("UNEXPECTED");
 	}
 
 	public static String sql_assign(Connection c, String name, String value) throws SQLException {
 		switch ( dbType(c) ) {
 		case POSTGRES:
 			return name + " := " + value;
 		case MYSQL:
 			return "SET " + name + " := " + value;
 		}
 		throw new SQLException("Unknonwn Database type");
 	}
 
 	public static String gen_Create_Or_Replace_Function(Connection c, String name, String par, String restype, String declare, String body) throws SQLException {
 		switch ( dbType(c) ) {
 		case POSTGRES:
 			return "CREATE OR REPLACE FUNCTION " + name +  "(" + par + ") RETURNS " + restype + " AS $$\n"+
 			declare +
 			"BEGIN\n"+
 			body + "\n" +
 			"END\n"+
 			"$$ LANGUAGE plpgsql;\n";
 		case MYSQL:
			return	
			"DROP FUNCTION IF EXISTS " + name + ";\n" +
			"DELIMITER //\n" +
 			"CREATE FUNCTION " + name +  "(" + par + ") RETURNS " + restype + " DETERMINISTIC\n"+
 			"BEGIN\n"+
 			declare +
 			body + "\n" +
 			"END //\n"+
 			"DELIMITER ;\n";
 		}	
 		throw new SQLException("UNEXPECTED");
 	}
 
 	public static String gen_Select_INTO(Connection c, String table, String select_head, String select_tail) throws SQLException {
 		switch ( dbType(c) ) {
 		case POSTGRES:
 			return  select_head +"\n"+
 			"INTO "+ table +"\n" +
 			select_tail + ";\n";
 		case MYSQL:
 			return	"CREATE TABLE "+table+"\n"+
 			select_head + "\n"+
 			select_tail + ";\n";
 		}	
 		throw new SQLException("UNEXPECTED");
 	}
 	
 	public static String gen_DROP_FUNCTION(Connection c, String fun, String par_type) throws SQLException {
 		switch ( dbType(c) ) {
 		case POSTGRES:
 			return  "DROP FUNCTION "+fun+"("+par_type+");\n";
 		case MYSQL:
 			return  "DROP FUNCTION IF EXISTS "+fun+";\n";
 		}	
 		throw new SQLException("UNEXPECTED");
 	}
 }
