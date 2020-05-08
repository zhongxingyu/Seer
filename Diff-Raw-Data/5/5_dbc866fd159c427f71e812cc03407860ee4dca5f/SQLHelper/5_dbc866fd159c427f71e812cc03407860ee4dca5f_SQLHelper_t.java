 /*
 *
 *	Copyright (c) 2013 Andreas Reder
 *	Author      : Andreas Reder <andreas.reder@lielas.org>
 *	File		: 
 *
 *	This File is part of lielas, see www.lielas.org for more information.
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
 package lielas.core;
 
 import java.io.Serializable;
 import java.security.MessageDigest;
 import java.sql.*;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.TimeZone;
 
 import javax.xml.bind.DatatypeConverter;
 
 import lielas.core.Device;
 import lielas.core.Modul;
 import lielas.core.Channel;
 import lielas.core.Config;
 import lielas.core.ExceptionHandler;
 
 
 public class SQLHelper implements Serializable {
 	
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 8744849037419385578L;
 
 	private Connection conn = null;
 	
 	private String dbUser;
 	private String dbPass;
 	private String dbName;
 	private String csvDelimiter;
 	private String serverAddress;
 	private Integer serverPort;
 	
 	public SQLHelper(){
 			
 	}
 	
 	public void Connect(){
 		try{
 			Class.forName("org.postgresql.Driver");
 		}catch (ClassNotFoundException e){
 			ExceptionHandler.HandleException(e);
 		}
 		
 		LoadSettings();
 		
 		String url = "jdbc:postgresql://" + serverAddress + ":" + serverPort.toString() + "/" + dbName;
 		String user = dbUser;
 		String password = dbPass;
 		
 		
 		
 		try{
 			conn = DriverManager.getConnection(url, user, password);
 			
 			InitDatabase(true);
 		} catch(SQLException e){
 			ExceptionHandler.HandleException(e);
 		}		
 		
 	}
 	
 	public void LoadSettings(){
 		Config cfg =  new Config();
 		cfg.LoadSettings();
 		dbUser = cfg.getDbUser();
 		dbPass = cfg.getDbPass();
 		dbName = cfg.getDbName();
 		serverAddress = cfg.getSqlServerAddress();
 		serverPort = cfg.getSqlServerPort();
 		csvDelimiter = cfg.getCsvDelimiter();
 	}
 	
 	public void InitDatabase(boolean withTestData){
 		
 		
 		if( conn == null){
 			Connect();
 		}
 		
 	}
 		
 	public boolean DeleteDatabase(){
 		try{
 			Statement st = conn.createStatement();
 			
 			st.executeUpdate("DROP TABLE lielas.devices");
 			st.executeUpdate("DROP TABLE lielas.moduls");
 			st.executeUpdate("DROP TABLE lielas.channels");
 			st.executeUpdate("DROP TABLE lielas.users");
 			
 			st.close();
 			return true;
 		} catch(Exception e){
 			ExceptionHandler.HandleException(e);
 		}
 		return false;
 	}
 	
 	public boolean DeleteData(){
 		try{
 			Statement st = conn.createStatement();
 			
			if(TableExists("log_data")){
				st.executeUpdate("DROP TABLE lielas.log_data");
				st.executeUpdate("CREATE TABLE lielas.log_data(datetime timestamp NOT NULL, PRIMARY KEY(datetime))");
 			}
 			st.close();
 			return true;
 		} catch(Exception e){
 			ExceptionHandler.HandleException(e);
 		}
 		return false;
 	}
 	
 	public Device GetDevice(int ID){
 		Device d = null;
 		String address;
 		String mac;
 		boolean registered;
 		String name;
 		String group;
 		int mInt = 0;
 		int pInt = 0;
 		int aInt = 0;
 		String supply;
 		int id;
 		
 		String moduls;
 		String strModulIDs[];
 		
 		String channels;
 		String strChannelIDs[];
 		
 		String delimiter = ";";
 		
 		if( conn == null){
 			Connect();
 		}
 		
 		try{
 			Statement st = conn.createStatement();
 			ResultSet rs = st.executeQuery("SELECT id, address, mac, registered, name, dev_group, mint, pint, aint, moduls  FROM lielas.devices ORDER BY id");
 			
 			if(rs.next()){
 				while(rs.getInt(1) < ID){
 					if(!rs.next()){
 						rs.close();
 						st.close();
 						return null;
 					}
 				}
 
 				id = rs.getInt(1);
 				address = rs.getString(2);
 				mac = rs.getString(3);
 				registered = rs.getBoolean(4);
 				name = rs.getString(5);
 				if(name == null){
 					name = "";
 				}
 				group = rs.getString(6);
 				if(group == null){
 					group = "";
 				}
 				if(rs.getString(7) != null){
 					mInt = Integer.parseInt(rs.getString(7));
 				}
 				
 				if(rs.getString(8) != null){
 					pInt = Integer.parseInt(rs.getString(8));
 				}
 				
 				if(rs.getString(9) != null){
 					aInt = Integer.parseInt(rs.getString(9));
 				}
 				
 				supply = rs.getString(10);
 				moduls = GetDeviceModuls(id);
 				
 				d = new Device(address, mac, registered, name, group, mInt, pInt, aInt, supply, id);
 				
 				// get Moduls
 				if( moduls != null){
 					strModulIDs = moduls.split(delimiter);
 					for(int i=0; i<strModulIDs.length; i++){
 						Modul m = GetModul(Integer.parseInt(strModulIDs[i]));
 						if( m != null){
 							m.setAddress(i+1);
 							d.addModul(m.getID(), m);
 							
 							//get Channels
 							channels = GetModulChannels(m.getID());
 							if(channels != null){
 								strChannelIDs = channels.split(delimiter);
 								for(int j=0; j < strChannelIDs.length; j++){
 									Channel c = GetChannel(Integer.parseInt(strChannelIDs[j]));
 									if( c != null){
 										d.addChannel(m.getAddress(), c);
 									}
 								}
 							}
 							
 						}
 					}
 				}
 			}
 			rs.close();
 			st.close();
 			return d;
 		} catch (SQLException e){
 			ExceptionHandler.HandleException(e);
 		}
 		return null;
 	}
 	
 	public String GetDeviceModuls( int ID){
 		String moduls = null;
 		
 		try{
 			Statement st = conn.createStatement();
 			ResultSet rs = st.executeQuery("SELECT moduls FROM lielas.devices WHERE id = " + ID);
 			if(rs.next()){
 				moduls = rs.getString(1);
 			}
 			
 			rs.close();
 			st.close();
 			
 		}catch(Exception e){
 			ExceptionHandler.HandleException(e);
 		}
 		
 		return moduls;				
 	}
 	
 	public Modul GetModul(int ID){
 		Modul m = null;
 		
 		int id;
 		int address = 0;
 		int mInt = 60;
 		int pInt = 2;
 		int aInt = 60;
 		
 		try{
 			Statement st = conn.createStatement();
 			ResultSet rs = st.executeQuery("SELECT id, address, mint, pint, aint FROM lielas.moduls WHERE id = " + ID);
 			if(rs.next()){
 				id = rs.getInt(1);
 				if(rs.getString(2) != null)
 					address = Integer.parseInt(rs.getString(2));
 				if(rs.getString(3) != null)
 					mInt = Integer.parseInt(rs.getString(3));
 				if(rs.getString(4) != null)
 					pInt = Integer.parseInt(rs.getString(4));
 				if(rs.getString(5) != null)
 					aInt = Integer.parseInt(rs.getString(5));
 				
 				m = new Modul(id, mInt, pInt, aInt);
 				m.setAddress(address);
 			}
 			
 			rs.close();
 			st.close();
 			
 		}catch(Exception e){
 			ExceptionHandler.HandleException(e);
 		}
 		
 		return m;
 	}
 	
 	public String GetModulChannels(int ID){
 		String channels = null;
 		
 		try{
 			Statement st = conn.createStatement();
 			ResultSet rs = st.executeQuery("SELECT channels FROM lielas.moduls WHERE id = " + ID);
 			if(rs.next()){
 				channels = rs.getString(1);
 			}
 			
 			rs.close();
 			st.close();
 			
 		}catch(Exception e){
 			ExceptionHandler.HandleException(e);
 		}
 		
 		return channels;		
 	}
 	
 	public Channel GetChannel(int ID){
 		Channel c = null;
 		
 		int id;
 		int address;
 		String name;
 		String group;
 		String type;
 		String minValue;
 		String maxValue;
 		String unit;
 		
 		try{
 			Statement st = conn.createStatement();
 			ResultSet rs = st.executeQuery("SELECT id, address, name, channel_group, type, unit FROM lielas.channels WHERE id = " + ID);
 			if(rs.next()){
 				id = rs.getInt(1);
 				address = Integer.parseInt(rs.getString(2));
 				name = rs.getString(3);
 				group = rs.getString(4);
 				type = rs.getString(5);
 				minValue = "";
 				maxValue= "";
 				unit = rs.getString(6);
 				
 				c = new Channel(id, address, type, unit, minValue, maxValue);
 				
 				if(name != null){
 					c.setName(name);
 				}
 				
 				if(group != null){
 					c.setGroup(group);
 				}
 			}
 			
 			rs.close();
 			st.close();
 			
 		}catch(Exception e){
 			ExceptionHandler.HandleException(e);
 		}
 		
 		return c;
 	}
 	
 	public boolean SaveDevice(Device d){
 		String update;
 		
 		try{
 			Statement st = conn.createStatement();
 			update = "UPDATE lielas.devices SET name = '" + d.getName() + "' WHERE id = " + d.getID();
 			st.executeUpdate(update);
 			update = "UPDATE lielas.devices SET dev_group = '" + d.getGroup() + "' WHERE id = " + d.getID();
 			st.executeUpdate(update);
 			update = "UPDATE lielas.devices SET mint = '" + d.getMeassurementIntervall() + "' WHERE id = " + d.getID();
 			st.executeUpdate(update);
 			update = "UPDATE lielas.devices SET pint = '" + d.getProcessIntervall() + "' WHERE id = " + d.getID();
 			st.executeUpdate(update);
 			update = "UPDATE lielas.devices SET aint = '" + d.getAlarmIntervall() + "' WHERE id = " + d.getID();
 			st.executeUpdate(update);		
 			st.close();
 			
 			// update modules
 			for(int i = 1; i < (d.getModuls()+1);i++){
 				if(!SaveModul(d.getModul(i)))
 					return false;
 			}
 			
 		}catch (Exception e){
 			ExceptionHandler.HandleException(e);
 			return false;
 		}
 		return true;
 	}
 	
 	public boolean SaveModul(Modul m){
 		String update;
 		
 		try{
 			Statement st = conn.createStatement();
 			
 			update = "UPDATE lielas.moduls SET mint = '" + m.getMeassurementIntervall() + "' WHERE id = " + m.getID();
 			st.executeUpdate(update);
 
 			update = "UPDATE lielas.moduls SET pint = '" + m.getProcessIntervall() + "' WHERE id = " + m.getID();
 			st.executeUpdate(update);
 
 			update = "UPDATE lielas.moduls SET aint = '" + m.getAlarmIntervall() + "' WHERE id = " + m.getID();
 			st.executeUpdate(update);
 			
 			st.close();
 			
 			// update channels
 			for(int i = 1; i <= m.getChannels(); i++){
 				if(!SaveChannel(m.getChannel(i)))
 					return false;
 			}
 			
 		}catch (Exception e){
 			ExceptionHandler.HandleException(e);
 			return false;
 		}
 		return true;
 	}
 	
 	public boolean SaveChannel(Channel c){
 		String update;
 		
 		try{
 			Statement st = conn.createStatement();
 			
 			update = "UPDATE lielas.channels SET name = '" + c.getName() + "' WHERE id = " +  c.getID();
 			st.executeUpdate(update);
 
 			update = "UPDATE lielas.channels SET channel_group = '" + c.getGroup() + "' WHERE id = " + c.getID();
 			st.executeUpdate(update);
 			
 			st.close();
 			
 		}catch (Exception e){
 			ExceptionHandler.HandleException(e);
 			return false;
 		}
 		return true;
 	}
 
 	private boolean TableExists(String tableName){
 		
 		try{
 			DatabaseMetaData dbm = conn.getMetaData();
 			ResultSet rs = dbm.getTables(null, null, tableName, null);
 			if(rs.next()){
 				rs.close();
 				return true;
 			}
 			rs.close();			
 		}catch(Exception e){
 			ExceptionHandler.HandleException(e);
 		}	
 		return false;
 	}
 	
 	public void CreateTestData(DeviceContainer dc, int rows){
 
 		String sqlStr;
 		String columnOrder = "datetime";
 		String valueString = "";
 		
 		if( conn == null){
 			Connect();
 		}
 		
 		try{
 			Statement st = conn.createStatement();
 			
 		
 			if(TableExists("data")){
 				st.executeUpdate("DROP TABLE lielas.data");
 			}
 			CreateDataTable(dc);
 				
 			Calendar date = Calendar.getInstance();
 			java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
 			
 			
 			// create sql statement column order
 			Device d = dc.firstItemId();
 			for( int i = 0; i < dc.size(); i++){
 				for( int j = 0; j < d.getModuls(); j++){
 					Modul m = d.getModul(j);
 					for( int k = 1; k <= m.getChannels(); k++){
 						Channel c = m.getChannel(k);
 						columnOrder += ", \"" + d.getAddress() + "." + m.getAddress() + "." + c.getAddress() + "\"";
 						valueString += ", '22,5'";
 					}
 				}
 				d = dc.nextItemId(d);
 			}
 			
 			for( int i = 0; i < rows; i++){
 				/*for(int j=0; j < dc.size(); j++){
 				
 				}*/
 				
 				sqlStr = "INSERT INTO lielas.data ( " + columnOrder + " ) VALUES ( '" + sdf.format(date.getTime()) + "' " + valueString + " )";
 				st.executeUpdate(sqlStr);
 				date.add(Calendar.SECOND, 10);
 				
 			}
 			
 			st.close();
 		}catch(Exception e){
 			ExceptionHandler.HandleException(e);
 		}
 	}
 	
 	public void CreateDataTable(DeviceContainer dc){
 		if( conn == null){
 			Connect();
 		}
 		
 		try{
 			Statement st = conn.createStatement();
 
 			if(!TableExists("data")){
 				st.executeUpdate("CREATE TABLE lielas.data( datetime timestamp NOT NULL, PRIMARY KEY(datetime))");
 			}
 			
 			Device d = dc.firstItemId();
 				
 			for( int i = 0; i < dc.size(); i++){
 				for( int j = 0; j < d.getModuls(); j++){
 					Modul m = d.getModul(j);
 					for( int k = 1; k <= m.getChannels(); k++){
 						Channel c = m.getChannel(k);
 						st.executeUpdate("ALTER TABLE lielas.data ADD COLUMN \"" + d.getAddress() + "." + m.getAddress() + "." + c.getAddress() + "\" text");
 					}
 				}
 				d = dc.nextItemId(d);
 			}
 			
 			st.close();
 		}catch(Exception e){
 			ExceptionHandler.HandleException(e);
 		}		
 	}
 	
 	public StringBuilder GetDataTable(DeviceContainer dc, Integer dtOffset, LanguageHelper lh){
 
 		String tableOrder = "datetime";
 		StringBuilder csvStr = new StringBuilder(" ");
 		String data;
 		ResultSet rs;
 		
 		if( conn == null){
 			Connect();
 		}
 		
 		try{
 			Statement st = conn.createStatement();
 			
 			Device d = dc.firstItemId();
 			
 			// create sql statement column order
 			for( int i = 0; i < dc.size(); i++){
 				for( int j = 1; j < (d.getModuls()+1); j++){
 					Modul m = d.getModul(j);
 					for( int k = 1; k <= m.getChannels(); k++){
 						Channel c = m.getChannel(k);
 						if(!ColumnExists(d.getAddress() + "." + m.getAddress() + "." + c.getAddress() )){
 							st.execute("ALTER TABLE lielas.log_data ADD COLUMN \""+ d.getAddress() + "." + m.getAddress() + "." + c.getAddress() + "\" text");
 						}
 						tableOrder += ", \"" + d.getAddress() + "." + m.getAddress() + "." + c.getAddress() + "\"";
 					}
 				}
 				d = dc.nextItemId(d);
 			}
 			
 			d = dc.firstItemId();
 			
 			// create first header line
 			csvStr.append(lh.GetString(LanguageHelper.DL_CSV_MAC));
 			for( int i = 0; i < dc.size(); i++){
 				for( int j = 1; j < (d.getModuls()+1); j++){
 					Modul m = d.getModul(j);
 					for( int k = 1; k <= m.getChannels(); k++){
 						if( j == 1 && k ==1){
 							csvStr.append(csvDelimiter + d.getAddress());
 						}else{
 							csvStr.append(csvDelimiter);
 						}
 					}
 				}
 				d = dc.nextItemId(d);
 			}	
 			
 			// create second header line
 			csvStr.append("\r\n" + lh.GetString(LanguageHelper.DL_CSV_NAME));
 			d = dc.firstItemId();
 			for( int i = 0; i < dc.size(); i++){
 				for( int j = 1; j < (d.getModuls()+1); j++){
 					Modul m = d.getModul(j);
 					for( int k = 1; k <= m.getChannels(); k++){
 						Channel c = m.getChannel(k);
 						csvStr.append(csvDelimiter + " " + c.getName());
 					}
 				}
 				d = dc.nextItemId(d);
 			}
 
 			// create third header line
 			csvStr.append("\r\n" + lh.GetString(LanguageHelper.DL_CSV_GROUP));
 			d = dc.firstItemId();
 			for( int i = 0; i < dc.size(); i++){
 				for( int j = 1; j < (d.getModuls()+1); j++){
 					Modul m = d.getModul(j);
 					for( int k = 1; k <= m.getChannels(); k++){
 						Channel c = m.getChannel(k);
 						csvStr.append(csvDelimiter + " " + c.getGroup());
 					}
 				}
 				d = dc.nextItemId(d);
 			}
 			// create fourth header line
 			csvStr.append("\r\n" + lh.GetString(LanguageHelper.DL_CSV_MODUL_CHANNEL_ADDRESS));
 			d = dc.firstItemId();
 			for( int i = 0; i < dc.size(); i++){
 				for( int j = 1; j < (d.getModuls()+1); j++){
 					Modul m = d.getModul(j);
 					for( int k = 1; k <= m.getChannels(); k++){
 						Channel c = m.getChannel(k);
 						csvStr.append(csvDelimiter + " " + m.getAddress() + "." + c.getAddress());
 					}
 				}
 				d = dc.nextItemId(d);
 			}				
 
 
 			// create fifth header line
 			csvStr.append("\r\n" + lh.GetString(LanguageHelper.DL_CSV_UNIT));
 			d = dc.firstItemId();
 			for( int i = 0; i < dc.size(); i++){
 				for( int j = 1; j < (d.getModuls()+1); j++){
 					Modul m = d.getModul(j);
 					for( int k = 1; k <= m.getChannels(); k++){
 						Channel c = m.getChannel(k);
 						csvStr.append(csvDelimiter + " " + UnitConverter.convert(c.getUnit()));
 					}
 				}
 				d = dc.nextItemId(d);
 			}		
 			
 			if(TableExists("log_data")){
 				rs = st.executeQuery("SELECT " + tableOrder + " FROM lielas.log_data ORDER BY datetime ASC");
 			}else{
 				return csvStr;
 			}
 		
 			
 			csvStr.append("\r\n");
 			if(!tableOrder.equals("datetime")){	// add data if data was queried
 				ResultSetMetaData rsMetaData = rs.getMetaData();
 				int columnCount = rsMetaData.getColumnCount();
 				while(rs.next()){
 					//creat date and time String
 					SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
 					Calendar cal = Calendar.getInstance();
 					TimeZone tz = TimeZone.getTimeZone("UTC");
 					cal.setTimeZone(tz);
 					cal.setTime(rs.getTimestamp(1));
 					cal.add(Calendar.HOUR_OF_DAY, dtOffset);
 					csvStr.append(sdf.format(cal.getTime()));
 					//add data
 					for( int i = 1; i < columnCount; i++){
 						data = rs.getString(i + 1);
 						if(data == null){
 							csvStr.append(csvDelimiter);
 						}else{
 							csvStr.append(csvDelimiter + data);
 						}
 						
 					}
 					csvStr.append( "\r\n");
 				}
 			}
 				
 			st.close();
 			rs.close();
 			return csvStr;
 		}catch(Exception e){
 			ExceptionHandler.HandleException(e);
 		}		
 		
 		return null;
 	}
 	
 	public User GetUser(int id){
 		User user = null;
 		
 		try{
 
 			if( conn == null){
 				Connect();
 			}
 			
 			Statement st = conn.createStatement();
 			ResultSet rs = st.executeQuery("SELECT id, login, first_name, last_name, usergroup, timezone, password  FROM lielas.users WHERE id = " + id);
 			
 			if(rs.next()){
 				user = new User(id);
 				user.setLogin(rs.getString(2));
 				user.setForename(rs.getString(3));
 				user.setName(rs.getString(4));
 				user.setUsergroup(rs.getString(5));
 				user.setTimezone(Integer.parseInt(rs.getString(6)));
 				user.setPassword(rs.getString(7));
 			}
 			
 			rs.close();
 			st.close();
 		}catch(Exception e){
 			ExceptionHandler.HandleException(e);
 		}
 		return user;
 	}
 	
 	public void GetUser(UserContainer uc){
 		User user = null;
 		try{
 
 			if( conn == null){
 				Connect();
 			}
 			
 			Statement st = conn.createStatement();
 			ResultSet rs = st.executeQuery("SELECT id, login, first_name, last_name, usergroup, timezone, password  FROM lielas.users ORDER BY id ASC");
 			
 			while(rs.next()){
 				user = new User(rs.getInt(1));
 				user.setLogin(rs.getString(2));
 				user.setForename(rs.getString(3));
 				user.setName(rs.getString(4));
 				user.setUsergroup(rs.getString(5));
 				user.setTimezone(Integer.parseInt(rs.getString(6)));
 				user.setPassword(rs.getString(7));
 				uc.addItem(user);
 			}
 			
 			rs.close();
 			st.close();
 		}catch(Exception e){
 			ExceptionHandler.HandleException(e);
 		}		
 	}
 	
 	public int CreateUser(){
 		int id = 0;
 		
 		try{
 			Statement st = conn.createStatement();
 			
 			ResultSet rs = st.executeQuery("SELECT id FROM lielas.users ORDER BY id DESC LIMIT 1");
 			
 			if(rs.next()){
 				id = rs.getInt(1) + 1;
 			}else{
 				id = 1;
 			}
 		
 			st.executeUpdate("INSERT INTO lielas.users ( id ) VALUES ( " + id + ")");
 
 			rs.close();
 			st.close();
 		}catch(Exception e){
 			ExceptionHandler.HandleException(e);
 		}
 		
 		return id;
 	}
 	
 	public int GetNewUserId(){
 		int id = 0;
 		
 		try{
 			Statement st = conn.createStatement();
 			
 			ResultSet rs = st.executeQuery("SELECT id FROM lielas.users ORDER BY id DESC LIMIT 1");
 			
 			if(rs.next()){
 				id = rs.getInt(1) + 1;
 			}else{
 				id = 1;
 			}
 		
 			rs.close();
 			st.close();
 		}catch(Exception e){
 			ExceptionHandler.HandleException(e);
 		}
 		
 		return id;
 	}
 	
 	public void SaveUser(User user){
 		try{
 			Statement st = conn.createStatement();
 			if(this.GetUser(user.getID()) == null){
 				st.executeUpdate("INSERT INTO lielas.users ( id ) VALUES ( " + user.getID() + ")");
 			}
 			st.execute("UPDATE lielas.users SET login = '" + user.getLogin() + "' WHERE id = " + user.getID());
 			st.execute("UPDATE lielas.users SET first_name = '" + user.getForename() + "' WHERE id = " + user.getID());
 			st.execute("UPDATE lielas.users SET last_name = '" + user.getName() + "' WHERE id = " + user.getID());
 			st.execute("UPDATE lielas.users SET timezone = '" + user.getTimezone() + "' WHERE id = " + user.getID());
 			st.execute("UPDATE lielas.users SET password = '" + user.getPassword() + "' WHERE id = " + user.getID());
 
 			st.close();
 		}catch(Exception e){
 			ExceptionHandler.HandleException(e);
 		}
 	}
 	
 	public boolean RegisterDevice(Device d){
 		Modul m;
 		Channel c;
 		int i, j;
 		String columnName;
 		
 		try{
 			Statement st = conn.createStatement();
 			st.execute("UPDATE lielas.devices SET registered=true WHERE id=" + d.getID());
 			st.close();
 			
 			for( i = 1; i < 20; i++){
 				m = d.getModul(i);
 				if( m == null){
 					return true;
 				}
 				for(j = 1; j < 20 ; j++){
 					c = m.getChannel(j);
 					if(c == null){
 						break;
 					}
 					columnName= d.getAddress() + "." + m.getAddress() + "." + c.getAddress();
 					
 					if(!ColumnExists(columnName)){
 						st = conn.createStatement();
 						st.execute("ALTER TABLE lielas.data ADD COLUMN \"" + columnName + "\" text");
 						st.close();
 					}
 				}
 			}
 			
 			
 			return true;
 		}catch(Exception e){
 			ExceptionHandler.HandleException(e);
 		}
 		return false;
 	}
 	
 	public String GetLastValue(Device d, Modul m, Channel c, User u){
 		String value = "";
 		
 		try{
 			String adr = d.getAddress() + "." + m.getAddress() + "." + c.getAddress();
 			if(ColumnExists(adr)){
 				Statement st = conn.createStatement();
 				ResultSet rs = st.executeQuery("SELECT datetime, \"" + adr + "\" FROM lielas.log_data WHERE  \"" + adr + "\" NOT LIKE '' ORDER BY datetime DESC LIMIT 1");
 				if(rs.next()){
 					Calendar cal = Calendar.getInstance();
 					TimeZone tz = TimeZone.getTimeZone("UTC");
 					cal.setTimeZone(tz);
 					cal.setTime(rs.getTimestamp(1));
 					cal.add(Calendar.HOUR_OF_DAY, u.getTimezone());
 					value = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").format(cal.getTime()) + "\t" + rs.getString(2);
 				}
 				rs.close();
 				st.close();
 			}
 		}catch(Exception e){
 			ExceptionHandler.HandleException(e);
 		}		
 		return value;
 	}
 	
 	public boolean DeleteUser(int id){
 		
 		try{
 			Statement st = conn.createStatement();
 			
 			st.executeUpdate("DELETE FROM lielas.users WHERE id = " + id);
 			
 			st.close();
 		}catch(Exception e){
 			ExceptionHandler.HandleException(e);
 			return false;
 		}
 		return true;
 	}
 	
 	private boolean ColumnExists(String column){
 		ResultSet rs;
 		try{
 			Statement st = conn.createStatement();
 			st = conn.createStatement();
 			rs = st.executeQuery("SELECT column_name FROM information_schema.columns WHERE table_name='log_data' and column_name='" + column + "'");
 			if(rs.next()){
 				st.close();
 				rs.close();
 				return true;
 			}
 			st.close();
 			rs.close();
 		}catch(Exception e){
 			ExceptionHandler.HandleException(e);
 		}
 		return false;
 	}
 	
 /*	private String getInternalMD5Hash(String str){
 		String md5 = "";
 		MessageDigest md = null;
 		
 		try{
 			md = MessageDigest.getInstance("MD5");
 			md.reset();
 			md.update(str.getBytes());
 			byte[] md5Byte= md.digest();
 			md5 = DatatypeConverter.printHexBinary(md5Byte);
 			return md5;
 			
 		}catch(Exception e){
 			ExceptionHandler.HandleException(e);
 		}
 		return null;
 	}*/
 	
 	public static String getMD5Hash(String str){
 		String md5 = "";
 		MessageDigest md = null;
 		
 		try{
 			md = MessageDigest.getInstance("MD5");
 			md.reset();
 			md.update(str.getBytes());
 			byte[] md5Byte= md.digest();
 			md5 = DatatypeConverter.printHexBinary(md5Byte);
 			return md5;
 			
 		}catch(Exception e){
 			ExceptionHandler.HandleException(e);
 		}
 		return null;
 	}
 	
 	public boolean checkMD5Hash(String s1, String s2){
 		if(s1.equals(s2)){
 			return true;
 		}
 		return false;
 	}
 	
 	public String getPanid(){
 		String panid = "";
 		
 		try{
 			if(TableExists("settings")){
 				Statement st = conn.createStatement();
 				ResultSet rs = st.executeQuery("SELECT value FROM lielas.settings WHERE name='PANID'");
 				if(rs.next()){
 					panid = rs.getString(1);
 				}
 				rs.close();
 				st.close();
 				
 			}
 		}catch(Exception e){
 			ExceptionHandler.HandleException(e);
 		}		
 		if(panid == null)
 			return "";
 		
 		return panid;
 	}
 	
 	public Integer getRegMInt(){
 		Integer mint = 0;
 		
 		try{
 			if(TableExists("settings")){
 				Statement st = conn.createStatement();
 				ResultSet rs = st.executeQuery("SELECT value FROM lielas.settings WHERE name='REG_MINT'");
 				if(rs.next()){
 					mint = Integer.parseInt(rs.getString(1));
 				}
 				rs.close();
 				st.close();
 				
 			}
 		}catch(Exception e){
 			ExceptionHandler.HandleException(e);
 		}	
 		
 		if(mint == null)
 			return 0;
 		
 		return mint;
 	}
 	
 	public Integer getRegPInt(){
 		Integer pint = 0;
 		
 		try{
 			if(TableExists("settings")){
 				Statement st = conn.createStatement();
 				ResultSet rs = st.executeQuery("SELECT value FROM lielas.settings WHERE name='REG_PINT'");
 				if(rs.next()){
 					pint = Integer.parseInt(rs.getString(1));
 				}
 				rs.close();
 				st.close();
 				
 			}
 		}catch(Exception e){
 			ExceptionHandler.HandleException(e);
 		}	
 		
 		if(pint == null)
 			return 0;
 		
 		return pint;
 	}
 	
 	public Integer getRegAInt(){
 		Integer aint = 0;
 		
 		try{
 			if(TableExists("settings")){
 				Statement st = conn.createStatement();
 				ResultSet rs = st.executeQuery("SELECT value FROM lielas.settings WHERE name='REG_AINT'");
 				if(rs.next()){
 					aint = Integer.parseInt(rs.getString(1));
 				}
 				rs.close();
 				st.close();
 				
 			}
 		}catch(Exception e){
 			ExceptionHandler.HandleException(e);
 		}	
 		
 		if(aint == null)
 			return 0;
 		
 		return aint;
 	}
 	
 	public Integer getRegTime(){
 		Integer time = 0;
 		
 		try{
 			if(TableExists("settings")){
 				Statement st = conn.createStatement();
 				ResultSet rs = st.executeQuery("SELECT value FROM lielas.settings WHERE name='REGMODE_LEN'");
 				if(rs.next()){
 					time = Integer.parseInt(rs.getString(1));
 				}
 				rs.close();
 				st.close();
 				
 			}
 		}catch(Exception e){
 			ExceptionHandler.HandleException(e);
 		}	
 		
 		if(time == null)
 			return 0;
 		
 		return time;
 	}
 	
 	public Integer getRegMaxTime(){
 		Integer time = 0;
 		
 		try{
 			if(TableExists("settings")){
 				Statement st = conn.createStatement();
 				ResultSet rs = st.executeQuery("SELECT value FROM lielas.settings WHERE name='MAX_REGMODE_LEN'");
 				if(rs.next()){
 					time = Integer.parseInt(rs.getString(1));
 				}
 				rs.close();
 				st.close();
 				
 			}
 		}catch(Exception e){
 			ExceptionHandler.HandleException(e);
 		}	
 		
 		if(time == null)
 			return 0;
 		
 		return time;
 	}
 	
 	public String getNetType(){
 
 		String nettype = "";
 		
 		try{
 			if(TableExists("settings")){
 				Statement st = conn.createStatement();
 				ResultSet rs = st.executeQuery("SELECT value FROM lielas.settings WHERE name='NET_TYPE'");
 				if(rs.next()){
 					nettype = rs.getString(1);
 				}
 				rs.close();
 				st.close();
 				
 			}
 		}catch(Exception e){
 			ExceptionHandler.HandleException(e);
 		}		
 		if(nettype == null)
 			return "";
 		
 		return nettype;
 	}
 	
 	public void setNetType(String netTpye) {
 
 		try{
 			if(TableExists("settings")){
 				Statement st = conn.createStatement();
 				if(netTpye.equals("static")){
 					st.executeUpdate("UPDATE lielas.settings SET value='static' WHERE name='NET_NEW_TYPE'");
 				}else{
 					st.executeUpdate("UPDATE lielas.settings SET value='dhcp' WHERE name='NET_NEW_TYPE'");
 				}
 				st.close();
 			}
 		}catch(Exception e){
 			ExceptionHandler.HandleException(e);
 		}	
 	}
 	
 	
 	public String getNetAddress(){
 
 		String nettype = "";
 		
 		try{
 			if(TableExists("settings")){
 				Statement st = conn.createStatement();
 				ResultSet rs = st.executeQuery("SELECT value FROM lielas.settings WHERE name='NET_ADR'");
 				if(rs.next()){
 					nettype = rs.getString(1);
 				}
 				rs.close();
 				st.close();
 				
 			}
 		}catch(Exception e){
 			ExceptionHandler.HandleException(e);
 		}		
 		if(nettype == null)
 			return "";
 		
 		return nettype;
 	}
 	
 	public void setNetAddress(String netAddress) {
 
 		try{
 			if(TableExists("settings")){
 				Statement st = conn.createStatement();
 				st.executeUpdate("UPDATE lielas.settings SET value='" + netAddress + "' WHERE name='NET_NEW_ADR'");
 				st.close();
 			}
 		}catch(Exception e){
 			ExceptionHandler.HandleException(e);
 		}	
 	}
 	
 	public String getNetMask(){
 
 		String nettype = "";
 		
 		try{
 			if(TableExists("settings")){
 				Statement st = conn.createStatement();
 				ResultSet rs = st.executeQuery("SELECT value FROM lielas.settings WHERE name='NET_MASK'");
 				if(rs.next()){
 					nettype = rs.getString(1);
 				}
 				rs.close();
 				st.close();
 				
 			}
 		}catch(Exception e){
 			ExceptionHandler.HandleException(e);
 		}		
 		if(nettype == null)
 			return "";
 		
 		return nettype;
 	}
 	
 	public void setNetMask(String netMask) {
 
 		try{
 			if(TableExists("settings")){
 				Statement st = conn.createStatement();
 				st.executeUpdate("UPDATE lielas.settings SET value='" + netMask + "' WHERE name='NET_NEW_MASK'");
 				st.close();
 			}
 		}catch(Exception e){
 			ExceptionHandler.HandleException(e);
 		}	
 	}
 	
 	public String getNetGateway(){
 
 		String nettype = "";
 		
 		try{
 			if(TableExists("settings")){
 				Statement st = conn.createStatement();
 				ResultSet rs = st.executeQuery("SELECT value FROM lielas.settings WHERE name='NET_GATEWAY'");
 				if(rs.next()){
 					nettype = rs.getString(1);
 				}
 				rs.close();
 				st.close();
 				
 			}
 		}catch(Exception e){
 			ExceptionHandler.HandleException(e);
 		}		
 		if(nettype == null)
 			return "";
 		
 		return nettype;
 	}
 		
 		public void setNetGateway(String netGateway) {
 
 			try{
 				if(TableExists("settings")){
 					Statement st = conn.createStatement();
 					st.executeUpdate("UPDATE lielas.settings SET value='" + netGateway + "' WHERE name='NET_NEW_GATEWAY'");
 					st.close();
 				}
 			}catch(Exception e){
 				ExceptionHandler.HandleException(e);
 			}	
 		}
 }
