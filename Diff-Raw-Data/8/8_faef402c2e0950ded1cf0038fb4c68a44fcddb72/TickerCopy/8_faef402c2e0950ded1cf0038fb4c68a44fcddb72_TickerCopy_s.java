 package ticker;
 
 import java.io.*;
 import java.net.*;
 import java.util.*;
 import java.sql.*;
 
 public final class TickerCopy {
 
 	public static void main(String[] args) {
		String stocks = null, opts = null, passOn = null;
 		if (args.length > 0) {
 			stocks = args[0];
			opts = args[1];
 		}
 		else { 
			System.out.println("You need to enter at least one argument");
 		}
 		try {
 			final String enEh = "N/A";
 			Connection conn = MySQLer();
 			URL url = new URL(URLGen(stocks, opts));
 			URLConnection connection = url.openConnection();
 			connection.connect();
 			Scanner in = new Scanner(connection.getInputStream());
 			while (in.hasNextLine()) {
 				passOn = in.nextLine();
 				String[] strArr = passOn.split(",");
 				for (int i = 0; i < strArr.length; i++) {
 					if (strArr[i].equals(enEh)) {
 						strArr[i] = "Null";
 					}
 				}
 				Depositor(strArr, conn);
 			}
 			conn.close();
 		}
 		catch (IOException e) {
 			e.printStackTrace();
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		
 	}
 	
    public static String GetTimeStamp() {
 	 java.util.Date date= new java.util.Date();
 	 String timeStamp = "\"" + new Timestamp(date.getTime()) + "\"";
 	 return timeStamp;
    }
    
 	public static void Depositor(String[] s, Connection conn) {
 		try {
 			String command = "INSERT INTO stockprices VALUES (" + s[0] + "," + s[1] + "," + 
 					GetTimeStamp() + "," + s[2] + "," + s[3] + "," + s[4] + "," + s[5] + "," +
 					s[6] + "," + s[7] + "," + s[8] + "," + s[9] + "," + s[10] + "," + s[11] + 
 					"," + s[12] + "," + s[13] + "," + s[14] + ");";
 			Statement stat = conn.createStatement();
 			stat.executeUpdate(command);
 					
 			} 
 		catch (SQLException e) {
 				e.printStackTrace();
 			}
 		
 
 	}
 	
 	public static String URLGen(String stocks, String opts) throws MalformedURLException {
 		String urlName = null;
 		urlName = "http://finance.yahoo.com/d/quotes.csv?s=" + stocks + 
 				"&f=" + opts;
 		System.out.println(urlName);
 		return urlName;
 	}
 	public static Connection MySQLer() {
 		Connection conn = null;
 		final String tableSetup = "CREATE TABLE IF NOT EXISTS stockprices (stockName VARCHAR(40), " +
 				"symbol VARCHAR(20), date TIMESTAMP, volume DECIMAL(40,10), stockExchange VARCHAR(20)," +
 				" askRealTime DECIMAL(20, 10), bidRealTime DECIMAL(20, 10), ask DECIMAL(20,10)," +
 				"bid DECIMAL(20,10),open DECIMAL(20,10),daysLow DECIMAL(20,10), daysHigh DECIMAL(20,10), " +
 				"52WeekLow DECIMAL(20,10), 52WeekHigh DECIMAL(20,10), 50DayMovingAvg DECIMAL(20,10), " +
 				"200DayMovingAvg DECIMAL (20,10),INDEX indi (symbol, date));";
 		final String url = "jdbc:mysql://localhost:3306/";
 		final String dbName = "hackerati";
 		final String driver = "com.mysql.jdbc.Driver";
 		final String userName = "sqluser"; 
 		final String password = "password";
 		
 		try {
 			Class.forName(driver).newInstance();
 			conn = DriverManager.getConnection(url+dbName,userName,password);
 		try {
 			Statement st = conn.createStatement();
 			st.executeUpdate(tableSetup);
 		} catch (SQLException s) {
 			System.out.println("Table already exists. Editing existing table");
 		}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return conn;	
 	}	
 }
