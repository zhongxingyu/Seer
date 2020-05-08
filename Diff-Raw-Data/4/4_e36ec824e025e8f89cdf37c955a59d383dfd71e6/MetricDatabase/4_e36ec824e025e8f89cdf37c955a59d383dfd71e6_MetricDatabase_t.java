 /*
  * Copyright 2011, United States Geological Survey or
  * third-party contributors as indicated by the @author tags.
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/  >.
  *
  */
 
 package asl.seedscan.database;
 
 import java.io.File;
 import java.sql.CallableStatement;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.PreparedStatement;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collection;
 import java.util.logging.Logger;
 
 import asl.security.*;
 import asl.seedscan.*;
 import asl.seedscan.config.*;
 import asl.seedscan.metrics.*;
 import asl.metadata.*;
 
 public class MetricDatabase
 {
     public static final Logger logger = Logger.getLogger("asl.seedscan.database.MetricDatabase");
 
     private Connection connection;
     private String URI;
     private String username;
     private String password;
     
     private CallableStatement callStatement;
 
     public MetricDatabase(DatabaseT config) {
        this(config.getUri(), config.getUsername(), config.getPassword().getPlain());
     }
     
     public MetricDatabase(String URI, String username, String password) {
     	this.URI = URI;
     	this.username = username;
     	this.password = password;
         System.out.println("MetricDatabase Constructor(): This is where we make the connection to the dbase");
         try {
            logger.info(String.format("Connection String = \"%s\", User = \"%s\", Pass = \"%s\"", URI, username, password));
             connection = DriverManager.getConnection(URI, username, password);
         } catch (SQLException e) {
             System.err.print(e);
             logger.severe("Could not open station database.");
             throw new RuntimeException("Could not open station database.");
         }
     }
     
     public Connection getConnection()
     {
     	return connection;
     }
     
     public int insertMetricData(Station station, Calendar date, Metric metric) {
     	int result = -1;
         try {
         	MetricResult metricResult = metric.getResult();
             for (String id: metricResult.getIdSet()) {
             	ResultSet resultSet = null;
             	String[] parts = id.split(",");
             	String location = parts[0];
             	String channel = parts[1];
 	            callStatement = connection.prepareCall("SELECT spInsertMetricData(?, ?, ?, ?, ?, ?, ?)");
 	            callStatement.setString(1, date.toString());
 	            callStatement.setString(2, metric.getName());
 	            callStatement.setString(3, station.getNetwork());
 	            callStatement.setString(4, station.getStation());
 	            callStatement.setString(5, location);
 	            callStatement.setString(6, channel);
 	            callStatement.setString(7, metricResult.getResult(id));
 	            //callStatement.registerOutParameter(8, java.sql.Types.INTEGER);
 	            resultSet = callStatement.executeQuery();
 	            //result = callStatement.getInt(8);
             }
             result = 0;
         }
         catch (SQLException e) {
             System.out.print(e);
         }
         return result;
     }
     
     public String selectAll(String startDate, String endDate){
     	String result = "";
         try {
             ResultSet resultSet = null;
             callStatement = connection.prepareCall("CALL spGetAll(?, ?, ?)");
             callStatement.setString(1, startDate);
             callStatement.setString(2, endDate);
             callStatement.registerOutParameter(3, java.sql.Types.VARCHAR);
             resultSet = callStatement.executeQuery();
             result = callStatement.getString(3);
         }
         catch (SQLException e) {
             System.out.print(e);
         }
         return result;
     }
     
 }
