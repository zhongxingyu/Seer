 /* Copyright (c) Kenneth Prugh 2011
 
    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.
 
    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.
 
    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
 
 package modules;
 
 import database.Postgres;
 import irc.Message;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 
 public class ChannelFilter {
     private final static Connection db = Postgres.getInstance().getConnection();
     private static String selectStatement;
     private static PreparedStatement prepStmt;
     private static ResultSet rs;
 
     /* Should we check if a channel has any filtering at all first and return
      * immediately for channels with no filtering? Or just try to look up the
      * command immediately
      */
     public static boolean isCommandFiltered(final Message m)
     {
         rs = null;
         prepStmt = null;
 
         int channel_id = -1;
         int command_id = -1;
 
         channel_id = getChanID(m.channel);
        command_id = getCommID(m.command);
 
         try
         { 
             selectStatement = "SELECT * FROM filtered_commands, WHERE (chan_id = ?) AND (comm_id = ?)";
             prepStmt = db.prepareStatement(selectStatement);
            prepStmt.setInt(1, channel_id);
            prepStmt.setInt(2, command_id);
             rs = prepStmt.executeQuery();
             if (rs.next())
             {
                 return true;
             }
             else
             {
                 return false;
             }
         } catch (SQLException e) {
             return false;
         } finally {
             if (prepStmt != null) {
                 try { 
                     prepStmt.close();
                 } catch (SQLException e) {}
             }
             if (rs != null) {
                 try { 
                     rs.close();
                 } catch (SQLException e) {}
             }
         }
     }
 
     private static int getChanID(String channel)
     {
         selectStatement = "SELECT chan_id FROM channels where channel = ?";
         try { 
             prepStmt = db.prepareStatement(selectStatement);
             prepStmt.setString(1, channel);
             rs = prepStmt.executeQuery();
             if (rs.next())
             {
                 return rs.getInt("chan_id");
             }
             else
             {
                 return -1;
             }
         } catch (SQLException e) {
             return -1;
         } finally {
             if (prepStmt != null) {
                 try { 
                     prepStmt.close();
                 } catch (SQLException e) {}
             }
         }
     }
 
     private static int getCommID(String command)
     {
         selectStatement = "SELECT comm_id FROM commands where command = ?";
         try { 
             prepStmt = db.prepareStatement(selectStatement);
             prepStmt.setString(1, command);
             rs = prepStmt.executeQuery();
             if (rs.next())
             {
                 return rs.getInt("comm_id");
             }
             else
             {
                 return -1;
             }
         } catch (SQLException e) {
             return -1;
         } finally {
             if (prepStmt != null) {
                 try { 
                     prepStmt.close();
                 } catch (SQLException e) {}
             }
         }
     }
 
     private static void insertChannel(String channel)
     {
         selectStatement = "INSERT INTO channels (channel) VALUES(?)";
         try { 
             prepStmt = db.prepareStatement(selectStatement);
             prepStmt.setString(1, channel);
             prepStmt.execute();
         } catch (SQLException e) {
         } finally {
             if (prepStmt != null) {
                 try { 
                     prepStmt.close();
                 } catch (SQLException e) {}
             }
         }
     }
 
     private static void insertCommand(String command)
     {
         selectStatement = "INSERT INTO commands (command) VALUES(?)";
         try { 
             prepStmt = db.prepareStatement(selectStatement);
             prepStmt.setString(1, command);
             prepStmt.execute();
         } catch (SQLException e) {
         } finally {
             if (prepStmt != null) {
                 try { 
                     prepStmt.close();
                 } catch (SQLException e) {}
             }
         }
     }
 
     public static String enableFilter(final Message m)
     {
         String command;
         try
         { 
              command = m.msg.split(" ")[1].trim();
         }
         catch (ArrayIndexOutOfBoundsException e)
         {
             return "Invalid input";
         }
 
         if (command.equals(""))
         {
             return "Invalid input";
         }
 
         int channel_id = -1;
         int command_id = -1;
 
         channel_id = getChanID(m.channel);
         if (channel_id == -1)
         {
             insertChannel(m.channel);
             channel_id = getChanID(m.channel);
         }
 
         command_id = getCommID(command);
         if (command_id == -1)
         {
             insertCommand(command);
             command_id = getCommID(command);
         }
 
         selectStatement = "INSERT INTO filtered_commands (chan_id, comm_id) VALUES(?, ?) ";
         try
         {
             prepStmt = db.prepareStatement(selectStatement);
             prepStmt.setInt(1, channel_id);
             prepStmt.setInt(2, command_id);
             prepStmt.execute();
 
             return "command " + command + " disabled on channel " + m.channel;
         } catch (SQLException e) {
             return "Database error";
         } finally {
             if (prepStmt != null) {
                 try { 
                     prepStmt.close();
                 } catch (SQLException e) {}
             }
         }
     }
 
     public static String disableFilter(final Message m)
     {
         String command;
         try
         { 
              command = m.msg.split(" ")[1].trim();
         }
         catch (ArrayIndexOutOfBoundsException e)
         {
             return "Invalid input";
         }
 
         if (command.equals(""))
         {
             return "Invalid input";
         }
 
         int channel_id = -1;
         int command_id = -1;
 
         channel_id = getChanID(m.channel);
         command_id = getCommID(command);
 
         selectStatement = "DELETE FROM filtered_commands WHERE chan_id = ? and comm_id = ?";
         try
         {
             prepStmt = db.prepareStatement(selectStatement);
             prepStmt.setInt(1, channel_id);
             prepStmt.setInt(2, command_id);
             int changed = prepStmt.executeUpdate();
 
             if (changed >= 1)
             {
                 return "command " + command + " enabled on channel " + m.channel;
             }
             else
             {
                 return "command " + command + " filter on channel " + m.channel + " not found";
             }
         } catch (SQLException e) {
             return "Database error";
         } finally {
             if (prepStmt != null) {
                 try { 
                     prepStmt.close();
                 } catch (SQLException e) {}
             }
         }
     }
 }
 
