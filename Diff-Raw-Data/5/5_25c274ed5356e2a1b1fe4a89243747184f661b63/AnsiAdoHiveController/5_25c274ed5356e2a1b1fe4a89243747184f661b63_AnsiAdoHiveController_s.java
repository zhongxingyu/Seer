 /*
  * Copyright (C) 2010 The AdoHive Team
  * 
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 3 of the License, or (at your option) any later version.
  * 
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  * 
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
  */
 
 /**
  * 
  */
 package de.unistuttgart.iste.se.adohive.controller.ansi;
 
 import java.sql.DriverManager;
 import java.sql.SQLException;
 
 import de.unistuttgart.iste.se.adohive.controller.jdbc.JdbcAdoHiveController;
 import de.unistuttgart.iste.se.adohive.exceptions.AdoHiveDatabaseException;
 import de.unistuttgart.iste.se.adohive.exceptions.AdoHiveException;
 import java.sql.ResultSet;
 import java.sql.Statement;
 
 /**
  * @author rashfael
  *
  */
 public class AnsiAdoHiveController extends JdbcAdoHiveController {
 
 	public AnsiAdoHiveController(String connectionString, String driver) throws AdoHiveException {
 		super();
 		try {
 			//Init driver
 			if(driver == null || driver.isEmpty())
 				driver = "org.apache.derby.jdbc.EmbeddedDriver";
 			Class.forName(driver).newInstance(); 
 			//grab the connection conf
 			if(connectionString == null)
 				connectionString = "jdbc:derby:derbyDB;create=true";
 			connection = DriverManager.getConnection(connectionString);//"jdbc:derby:derbyDB;create=true");
 			//create one dialect Object for all managers
 			//NOTE: static does not really work, I would have done this with generics and static methods,
 			//but since generics in Java are crap (erasure), it does not work, so we are stuck with throwing around dialect objects
 			AnsiSqlDialect dialect = new AnsiSqlDialect();
 
                         // Try to get the SQL_MODE of the server if we're using a MySQL server
                         if (connectionString.contains("mysql")) {
                             Statement stmt = connection.createStatement();
                             ResultSet result = stmt.executeQuery("SELECT @@SESSION.SQL_MODE;");
                             result.first();
                             String sqlmode = result.getString(1);
 
                             if (!sqlmode.contains("ANSI")) {
                                sqlmode += ",ANSI, ANSI_QUOTES";
                                 connection.prepareStatement("SET @@SESSION.SQL_MODE = '" + sqlmode + "'").execute();
                             } else if (!sqlmode.contains("ANSI_QUOTES")) {
                                 sqlmode += ",ANSI_QUOTES";
                                 connection.prepareStatement("SET @@SESSION.SQL_MODE = '" + sqlmode + "'").execute();
                             }
 
                             result.close();
                             stmt.close();
                         }
 
 
 			//init all managers
 			assistantManager = new AnsiAssistantManager(connection, dialect);
 			financialCategoryManager = new AnsiFinancialCategoryManager(connection, dialect);
 			hourlyWageManager = new AnsiHourlyWageManager(connection, dialect);
 			
 			contractManager = new AnsiContractManager(connection, dialect);
 			courseManager = new AnsiCourseManager(connection, dialect);
 			activityManager = new AnsiActivityManager(connection, dialect);
 			
 			employmentManager = new AnsiEmploymentManager(connection, dialect);
 			
 		} catch (SQLException e) {
 			throw new AdoHiveDatabaseException(e);
 		} catch (InstantiationException e) {
 			throw new AdoHiveDatabaseException(e);
 		} catch (IllegalAccessException e) {
 			throw new AdoHiveDatabaseException(e);
 		} catch (ClassNotFoundException e) {
 			throw new AdoHiveDatabaseException(e);
 		}
 		
 		
 	}
 	
 	
 
 }
