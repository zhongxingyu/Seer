 /*
  * Copyright (C) 2012 Helsingfors Segelklubb ry
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Affero General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Affero General Public License for more details.
  *
  * You should have received a copy of the GNU Affero General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
 
 package fi.hoski.remote.sync;
 
 import fi.hoski.datastore.repository.DataObject;
 import java.io.PrintWriter;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.List;
 import java.util.Properties;
 import javax.swing.JOptionPane;
 
 /**
  * @author Timo Vesalainen
  */
 public class SqlConnection 
 {
     protected Properties properties;
     protected boolean debug;
     protected Connection connection;
 
     public SqlConnection(Properties properties) throws ClassNotFoundException, SQLException
     {
         this.properties = properties;
         debug = Boolean.parseBoolean(properties.getProperty("debug"));
         String driverName = properties.getProperty("driver");
        if (driverName == null)
        {
            throw new SQLException("driver not in property");
        }
         Class.forName(driverName);
         String databaseURL = properties.getProperty("databaseURL")+properties.getProperty("dsn");
         if (debug) DriverManager.setLogWriter(new PrintWriter(System.err));
         connection = DriverManager.getConnection(databaseURL, properties);
     }
     
     public ResultSet query(String sql) throws SQLException
     {
         PreparedStatement statement = connection.prepareStatement(sql);
         return statement.executeQuery();
     }
 }
