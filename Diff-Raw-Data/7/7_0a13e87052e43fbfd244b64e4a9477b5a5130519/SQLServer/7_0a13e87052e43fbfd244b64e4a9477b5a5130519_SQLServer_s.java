 /* OpenMark online assessment system
    Copyright (C) 2007 The Open University
 
    This program is free software; you can redistribute it and/or
    modify it under the terms of the GNU General Public License
    as published by the Free Software Foundation; either version 2
    of the License, or (at your option) any later version.
 
    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.
 
    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
  */
 package om.tnavigator.db.sqlserver;
 
 import java.sql.*;
 
 import om.tnavigator.db.*;
 import om.tnavigator.db.DatabaseAccess.Transaction;
 import util.misc.Strings;
 
 /**
  * Specialisation of OmQueries for Microsoft SQL Server.
  */
 public class SQLServer extends OmQueries
 {
 	/**
 	 * @param prefix the database prefix to use.
 	 */
 	public SQLServer(String prefix) {
 		super(prefix);
 	}
 
 	@Override
 	public String getURL(String server,String database,String username,String password)
 		throws ClassNotFoundException
 	{
 		Class.forName("com.microsoft.jdbc.sqlserver.SQLServerDriver");
 		return "jdbc:microsoft:sqlserver://"+server+";DatabaseName="+database+
 			";User="+username+";Password="+password;
 	}
 
 	@Override
 	public int getInsertedSequenceID(Transaction dat,String table,String column)
 		throws SQLException
 	{
 		ResultSet rs=dat.query("SELECT @@IDENTITY");
 		rs.next();
 		return rs.getInt(1);
 	}
 	
 	@Override
 	public ResultSet queryUnfinishedSessions(DatabaseAccess.Transaction dat,String oucu,String deploy) throws SQLException
 	{
 		return dat.query( 
			"SELECT TOP 1 ti,rseed,finished,variant,testposition " +
 			"FROM " + getPrefix() + "tests " +
 			"WHERE oucu="+Strings.sqlQuote(oucu)+" AND deploy="+Strings.sqlQuote(deploy)+" " +
 			"ORDER BY attempt DESC;");
 	}
 	
 	@Override
 	protected String currentDateFunction()
 	{
 		return "GETDATE()";
 	}
 
 	@Override
 	public void checkDatabaseConnection(DatabaseAccess.Transaction dat)
 		throws SQLException
 	{
 		dat.query("SELECT @@version");
 	}
 	
 	@Override
 	protected String unicode(String quotedString)
 	{
 		return "N"+quotedString;
 	}
 
 	protected String getCurrentDateFunction()
 	{
 		return "GETDATE()";
 	}
 	
 	@Override
 	protected void upgradeDatabaseTo131(DatabaseAccess.Transaction dat) throws SQLException
 	{
		dat.update("ALTER TABLE " + getPrefix() + "tests ADD COLUMN navigatorversion CHAR(16)");
		dat.update("UPDATE snav_tests SET navigatorversion = '1.3.0'");
 		dat.update("ALTER TABLE " + getPrefix() + "tests ALTER COLUMN navigatorversion CHAR(16) NOT NULL");
 	}
 }
