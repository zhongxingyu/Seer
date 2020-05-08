 package data_source_interface;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.naming.Context;
 import javax.naming.InitialContext;
 import javax.naming.NamingException;
 import javax.sql.DataSource;
 
 import JSONtypes.Connect;
 import JSONtypes.Disconnect;
 import JSONtypes.Invalid;
 import JSONtypes.Line;
 import JSONtypes.Other;
 import JSONtypes.Server;
 import JSONtypes.SubSystemReq;
 import enums.AuthType;
 import enums.Status;
 import enums.SubSystem;
 
 public class Mysql_Datasource implements LogDataSource {
 
 	@Override
 	public List<Line> getEntriesFromDataSource(String serverName,
 			String startTime, String endTime) throws DataSourceException {
 		String query;
 		if (serverName == null) {
 			query = "SELECT entry.id, entry.timestamp, server.name as server, entry.connid, entry.reqtype, "
 					+ "entry.authtype, entry.status, user.name as user, entry.source, entry.port, entry.subsystem, entry.code, "
 					+ "entry.isfreqtime, entry.isfreqloc, entry.rawline "
 					+ "FROM entry LEFT JOIN server ON entry.server = server.id "
 					+ "LEFT JOIN user ON entry.user = user.id "
 					+ "WHERE entry.timestamp BETWEEN ? AND ?;";
 		} else {
 			query = "SELECT entry.id, entry.timestamp, server.name as server, entry.connid, entry.reqtype, "
 					+ "entry.authtype, entry.status, user.name as user, entry.source, entry.port, entry.subsystem, entry.code, "
 					+ "entry.isfreqtime, entry.isfreqloc, entry.rawline "
 					+ "FROM entry LEFT JOIN server ON entry.server = server.id "
 					+ "LEFT JOIN user ON entry.user = user.id "
 					+ "WHERE server.name = ? AND "
 					+ "entry.timestamp BETWEEN ? AND ?;";
 		}
 		List<Line> lines = null;
 		Context context = null;
 		Connection connection = null;
 		PreparedStatement state = null;
 		ResultSet result = null;
 		try {
 			context = new InitialContext();

 			connection = ((DataSource) context
 					.lookup("java:comp/env/jdbc/sshd_vis_db")).getConnection();
 			state = connection.prepareStatement(query);
 
 
 			if (serverName != null) {
 				state.setString(1, serverName);
 				state.setLong(2, Long.parseLong(startTime));
 				state.setLong(3, Long.parseLong(endTime));
 			} else {
 				state.setLong(1, Long.parseLong(startTime));
 				state.setLong(2, Long.parseLong(endTime));
 			}
 			state.execute();
 			result = state.getResultSet();
 			lines = new ArrayList<Line>();
 			Line res;
 			String colVal;
 			while (result.next()) {
 				colVal = result.getString("entry.reqtype");
 				if ("connect".equalsIgnoreCase(colVal)) {
 					res = loadConnect(result);
 				} else if ("disconnect".equalsIgnoreCase(colVal)) {
 					res = loadDisconnect(result);
 				} else if ("subsystem".equalsIgnoreCase(colVal)) {
 					res = loadSubsystem(result);
 				} else if ("invalid".equalsIgnoreCase(colVal)) {
 					res = loadInvalid(result);
 				} else {
 					res = loadOther(result);
 				}
 				lines.add(res);
 			}
 		} catch (SQLException e) {
 			throw new DataSourceException(e);
 		} catch (NumberFormatException e) {
 			throw new DataSourceException(e);
 		} catch (NamingException e) {
 			throw new DataSourceException(e);
 		} finally {
 			try {
 				if (result != null) {
 					result.close();
 				}
 				if (state != null) {
 					state.close();
 				}
 				if (connection != null) {
 					connection.close();
 				}
				if (context != null) {
 					context.close();
 				}
 			} catch (SQLException e) {
 				throw new DataSourceException(e);
 			} catch (NamingException e) {
 				throw new DataSourceException(e);
 			}
 		}
 		return lines;
 	}
 
 	private Line loadOther(ResultSet result) throws SQLException {
 		long time = result.getLong("timestamp");
 		Server s;
 		if (result.getMetaData().getColumnCount() == 11) {
 			s = new Server(null, null);
 		} else {
 			s = new Server(result.getString("server"), null);
 		}
 		String msg = result.getString("rawline");
 		msg = msg.substring(msg.indexOf("]:") + 2);
 		return new Other(result.getInt("id"), time, s, result.getInt("connid"), msg,
 				result.getString("rawline"));
 	}
 
 	private Line loadInvalid(ResultSet result) throws SQLException {
 		long time = result.getLong("timestamp");
 		Server s;
 		if (result.getMetaData().getColumnCount() == 11) {
 			s = new Server(null, null);
 		} else {
 			s = new Server(result.getString("server"), null);
 		}
 		return new Invalid(result.getInt("id"), time, s, result.getInt("connid"),
 				result.getString("user"), result.getString("source"),
 				result.getString("rawline"));
 	}
 
 	private Line loadSubsystem(ResultSet result) throws SQLException {
 		long time = result.getLong("timestamp");
 		Server s;
 		if (result.getMetaData().getColumnCount() == 11) {
 			s = new Server(null, null);
 		} else {
 			s = new Server(result.getString("server"), null);
 		}
 		SubSystem sub;
 		if (result.getString("subsystem").equals("sftp")) {
 			sub = SubSystem.SFTP;
 		} else {
 			sub = SubSystem.SCP;
 		}
 		return new SubSystemReq(result.getInt("id"), time, s, result.getInt("connid"), sub,
 				result.getString("rawline"));
 	}
 
 	private Line loadDisconnect(ResultSet result) throws SQLException {
 		long time = result.getLong("timestamp");
 		Server s;
 		if (result.getMetaData().getColumnCount() == 11) {
 			s = new Server(null, null);
 		} else {
 			s = new Server(result.getString("server"), null);
 		}
 		return new Disconnect(result.getInt("id"), time, s, result.getInt("connid"),
 				result.getInt("code"), result.getString("source"),
 				result.getString("rawline"));
 	}
 
 	private Line loadConnect(ResultSet result) throws SQLException {
 		long time = result.getLong("timestamp");;
 		Server s;
 		if (result.getMetaData().getColumnCount() == 11) {
 			s = null;
 		} else {
 			s = new Server(result.getString("server"), null);
 		}
 
 		AuthType auth;
 		String temp = result.getString("authtype");
 		if (temp.equals("pass")) {
 			auth = AuthType.PASS;
 		} else if (temp.equals("key")) {
 			auth = AuthType.KEY;
 		} else if (temp.equals("host")) {
 			auth = AuthType.HOST;
 		} else if (temp.equals("gssapi")) {
 			auth = AuthType.GSSAPI;
 		} else {
 			auth = AuthType.NONE;
 		}
 
 		Status status;
 		if (result.getString("status").equals("accepted")) {
 			status = Status.ACCEPTED;
 		} else {
 			status = Status.FAILED;
 		}
 
 		return new Connect(result.getInt("id"), time, s, result.getInt("connid"), status, auth,
 				result.getString("user"), result.getString("source"),
 				result.getInt("port"), result.getLong("isfreqtime"),
 				result.getInt("isfreqloc"), result.getString("rawline"));
 	}
 
 	@Override
 	public long[] getStartAndEndOfUniverse() throws DataSourceException {
 		String query = "SELECT MIN(timestamp) as start, MAX(timestamp) as end FROM entry;";
 		long[] res = new long[2];
 
 		Context context = null;
 		Connection connection = null;
 		Statement state = null;
 		ResultSet result = null;
 		try {
 			context = new InitialContext();
 
 			connection = ((DataSource) context
 					.lookup("java:comp/env/jdbc/sshd_vis_db")).getConnection();
 			state = connection.createStatement();
 			result = state.executeQuery(query);
 			if (result.first()){
 				res[0] = result.getLong("start");
 				res[1] = result.getLong("end");
 			}
 		} catch (SQLException e) {
 			throw new DataSourceException(e);
 		} catch (NumberFormatException e) {
 			throw new DataSourceException(e);
 		} catch (NamingException e) {
 			throw new DataSourceException(e);
 		} finally {
 			try {
 				if (result != null) {
 					result.close();
 				}
 				if (state != null) {
 					state.close();
 				}
 				if (connection != null) {
 					connection.close();
 				}
 				if (context != null) {
 					context.close();
 				}
 			} catch (SQLException e) {
 				throw new DataSourceException(e);
 			} catch (NamingException e) {
 				throw new DataSourceException(e);
 			}
 		}
 		return res;
 	}
 
 	@Override
 	public List<Server> getAllServers() throws DataSourceException {
 		String query = "SELECT id, name, block FROM server;";
 		List<Server> res = new ArrayList<Server>();
 
 		Context context = null;
 		Connection connection = null;
 		Statement state = null;
 		ResultSet result = null;
 		try {
 			context = new InitialContext();
 
 			connection = ((DataSource) context
 					.lookup("java:comp/env/jdbc/sshd_vis_db")).getConnection();
 			state = connection.createStatement();
 			result = state.executeQuery(query);
 			while (result.next()) {
 				res.add(new Server(result.getInt("id"), result.getString("name"), result.getString("block")));
 			}
 		} catch (SQLException e) {
 			throw new DataSourceException(e);
 		} catch (NumberFormatException e) {
 			throw new DataSourceException(e);
 		} catch (NamingException e) {
 			throw new DataSourceException(e);
 		} finally {
 			try {
 				if (result != null) {
 					result.close();
 				}
 				if (state != null) {
 					state.close();
 				}
 				if (connection != null) {
 					connection.close();
 				}
 				if (context != null) {
 					context.close();
 				}
 			} catch (SQLException e) {
 				throw new DataSourceException(e);
 			} catch (NamingException e) {
 				throw new DataSourceException(e);
 			}
 		}
 		return res;
 	}
 
 	@Override
 	public boolean writeComment(long entry_id, String comment)
 			throws DataSourceException {
 		if (entry_id < 0 || comment == null || comment.equals("")){
 			throw new DataSourceException("invalid arguments");
 		}
 		String query = "INSERT INTO entry_comment VALUE(DEFAULT, ?, ?)";
 		
 		Context context = null;
 		Connection connection = null;
 		PreparedStatement state = null;
 		boolean result = false;
 		try {
 			context = new InitialContext();
 
 			connection = ((DataSource) context
 					.lookup("java:comp/env/jdbc/sshd_vis_db")).getConnection();
 			state = connection.prepareStatement(query);
 			state.setLong(1, entry_id);
 			state.setString(2, comment);
 			int res = state.executeUpdate();
 			result = (res == 1) ? true : false;
 		} catch (SQLException e) {
 			throw new DataSourceException(e);
 		} catch (NumberFormatException e) {
 			throw new DataSourceException(e);
 		} catch (NamingException e) {
 			throw new DataSourceException(e);
 		} finally {
 			try {
 				if (state != null) {
 					state.close();
 				}
 				if (connection != null) {
 					connection.close();
 				}
 				if (context != null) {
 					context.close();
 				}
 			} catch (SQLException e) {
 				throw new DataSourceException(e);
 			} catch (NamingException e) {
 				throw new DataSourceException(e);
 			}
 		}		
 		return result;
 	}
 
 }
