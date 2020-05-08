 package fatworm.sister;
 
 import java.io.FileNotFoundException;
 import java.io.PrintStream;
 import java.math.BigDecimal;
 import java.sql.Connection;
 import java.sql.Date;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.ResultSetMetaData;
 import java.sql.SQLException;
 import java.sql.Statement;
 
 import org.apache.log4j.Logger;
 
 public class SqlClient {
 	
 	private static Logger logger = Logger.getLogger(SqlClient.class);
 
 	static {
 		try {
 			Class.forName("fatworm.driver.Driver");
 		} catch (ClassNotFoundException e) {
 			logger.error(e);
 		}
 	}
 
 	private Connection conn;
 
 	private StringBuilder sb;
 
 	private PrintStream out;
 	
 	public SqlClient() {
 		sb = new StringBuilder();
 		out = System.out;
 		conn = null;
 	}
 
 	public void appendLine(String line) {
 		if (endOfSql(line)) {
 			execute(sb.toString());
 			sb = new StringBuilder();
 			return;
 		}
 		sb.append(line);
 		sb.append("\n");
 	}
 
 	private void execute(String str) {
 		try {
 			Statement stat = conn.createStatement();
 			stat.execute(str);
 			ResultSet res = stat.getResultSet();
 			displayResultSet(res);
 		} catch (SQLException e) {
 			logger.error(e);
 		}
 
 	}
 
 	private void displayResultSet(ResultSet res) throws SQLException {
 		ResultSetMetaData schema = res.getMetaData();
 		while (res.next())
 			outputOneRecord(res, schema);
 	}
 
 	private void outputOneRecord(ResultSet res, ResultSetMetaData schema)
 			throws SQLException {
 		StringBuilder sb = new StringBuilder();
 		sb.append("(");
 		for (int i = 1; i <= schema.getColumnCount(); ++i) {
 			if (i != 1)
 				sb.append(", ");
 			int type = schema.getColumnType(i);
 			Object o = res.getObject(i);
 			sb.append(getFieldString(o, type));
 		}
 		sb.append(")");
 		out.println(sb.toString());
 	}
 
 	private String getFieldString(Object o, int type) {
 		switch (type) {
 		case java.sql.Types.INTEGER:
 			return ((Integer)o).toString();
 		case java.sql.Types.BOOLEAN:
 			return ((Boolean)o).toString();
 		case java.sql.Types.CHAR:
 		case java.sql.Types.VARCHAR:
 			return "'" + (String)o + "'";
 		case java.sql.Types.FLOAT:
 			return ((Float)o).toString();
 		case java.sql.Types.DATE:
 			return ((Date)o).toString();
 		case java.sql.Types.DECIMAL:
 			return ((BigDecimal)o).toString();
 		case java.sql.Types.TIMESTAMP:
 			return ((java.sql.Timestamp)o).toString();
 		default:
 			logger.error("Undefined Type Number " + type);
 			return null;
 		}
 	}
 
 	private boolean endOfSql(String line) {
 		return line.equals(";");
 	}
 
 	public void connect(String url) {
 		try {
 			conn = DriverManager.getConnection(url);
 		} catch (SQLException e) {
 			logger.error(e);
 		}
 	}
 
 	public void setOutput(String outputFile) {
 		try {
 		out = new PrintStream(outputFile);
 		} catch(FileNotFoundException e) {
 			logger.error(e);
 		}
 	}
 }
