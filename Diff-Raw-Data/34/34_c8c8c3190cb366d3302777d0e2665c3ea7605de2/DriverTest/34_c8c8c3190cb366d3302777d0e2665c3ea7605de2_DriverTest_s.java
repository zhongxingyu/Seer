 package fatworm.driver;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.ResultSetMetaData;
 import java.sql.Statement;
 
 public class DriverTest {
 	public static void main(String[] args) throws Exception {
 		
 		Class.forName("fatworm.driver.Driver"); //这句话负责load你的Driver类，此时静态代码执行
 		String url = "jdbc:fatworm://test/";
 		String username = "fatworm";
 		String password = "fatworm";
 		Connection con = DriverManager.getConnection(url, username, password);
 		Statement sql_statement = con.createStatement();
 
     	BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
     	String buffer = reader.readLine();
     	StringBuffer sql = new StringBuffer();
     	while(sql != null) {
     		//System.err.println(sql.toString());
     		if (buffer.startsWith("@")) {
     			buffer = reader.readLine();
     			continue;
     		}
     		if (buffer.startsWith(";")){
     			String state = sql.toString();
     			sql = new StringBuffer();	    			
 	    		try {
 
 		    		boolean result = sql_statement.execute(state);
 		    		if(result) {
 		    			ResultSet resultset = sql_statement.getResultSet();
 		    			ResultSetMetaData metadata = resultset.getMetaData();
 		    			int width = metadata.getColumnCount();
 		    			//resultset.beforeFirst();
 		    			while(resultset.next()) {
 			    			for (int i = 1; i <= width; i++)
 			    				System.out.print(resultset.getObject(i).toString() + " | ");
 			    			System.out.println();
 		    			}
 		    			System.out.println();
 		    		}
 	    		} catch(Exception e) {
 	    			e.printStackTrace();
 	    			System.err.println(state);
 	    		}
     		}
     		else {
     			sql.append(buffer);
     			sql.append(" ");
     		}
     		buffer = reader.readLine();
     	}		
 	}
 }
