 /*
 =============
 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU Lesser General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.
 
 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.
 
 You should have received a copy of the GNU Lesser General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package abelymiguel.miralaprima;
 
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.SQLException;
 
 /**
  *
  * @author refusta
  */
 public class Utils {
 
   public static Connection getConnection() throws URISyntaxException, SQLException {
       URI dbUri = new URI(System.getenv("CLEARDB_DATABASE_URL"));
 
       String username = dbUri.getUserInfo().split(":")[0];
       String password = dbUri.getUserInfo().split(":")[1];
       String dbUrl = "jdbc:mysql://" + dbUri.getHost() + dbUri.getPath();
      Connection con = DriverManager.getConnection(dbUrl, username, password);
     return con;
   }
 }
