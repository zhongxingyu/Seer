 package dataTypes;
 
 import java.net.InetAddress;
 import java.sql.CallableStatement;
 import java.sql.PreparedStatement;
 import java.sql.SQLException;
 import java.sql.Types;
 import java.util.concurrent.TimeUnit;
 
 import Parser.GeoLocator;
 import enums.AuthType;
 import enums.Status;
 
 public class Connect implements dataTypes.Line {
 	private static final int frequency = 5; // how often do we have to login
 											// somewhere before it's frequent?
 	private static final long ttl = 1000 * 60 * 60 * 24 * 7; // 1 week in seconds
	private static final long timeAllowance = 1000*60*10;// construct 
	private static final long day = 0;													// from
																		// milliseconds,
																		// set
																		// to 10
																		// minutes

 	private final long time;
 	private final Server server;
 	private final int connectID;
 	private final Status status;
 	private final AuthType type;
 	private final User user;
 	private final InetAddress source;
 	private final int port;
 	private final String rawLine;
 	private int isFreqLoc;
 	private long isFreqTime = -1;// not yet implemented.
 
 	public Connect(long time, Server server, int connectID, Status status,
 			AuthType type, User user, InetAddress address, int port,
 			String rawLine) {
 		super();
 		this.time = time;
 		this.server = server;
 		this.connectID = connectID;
 		this.status = status;
 		this.type = type;
 		this.user = user;
 		this.source = address;
 		this.port = port;
 		this.rawLine = rawLine;
 	}
 
 	public long getTime() {
 		return time;
 	}
 
 	public Server getServer() {
 		return server;
 	}
 
 	public int getConnectID() {
 		return connectID;
 	}
 
 	public Status getStatus() {
 		return status;
 	}
 
 	public AuthType getType() {
 		return type;
 	}
 
 	public User getUser() {
 		return user;
 	}
 
 	public InetAddress getSource() {
 		return source;
 	}
 
 	public int getPort() {
 		return port;
 	}
 
 	public String getRawLine() {
 		return rawLine;
 	}
 
 	@Override
 	public void writeToDB(PreparedStatement insert) throws SQLException {
 		insert.setLong(1, this.time);
 		insert.setInt(2, this.server.getId());
 		insert.setInt(3, this.connectID);
 		insert.setString(4, "connect");
 		insert.setString(5, this.type.toString().toLowerCase());
 		insert.setString(6, this.status.toString().toLowerCase());
 		insert.setInt(7, this.user.getId());
 		insert.setString(8, this.source.getHostAddress());
 		insert.setInt(9, this.port);
 		insert.setNull(10, Types.CHAR);
 		insert.setNull(11, Types.INTEGER);
 		if (this.isFreqTime == -1) {
 			insert.setNull(12, Types.INTEGER);
 		} else {
 			insert.setLong(12, this.isFreqTime);
 		}
 		if (this.isFreqLoc == -1) {
 			insert.setNull(13, Types.INTEGER);
 		} else {
 			insert.setInt(13, this.isFreqLoc);
 		}
 		;
 		insert.setString(14, this.rawLine);
 		insert.addBatch();
 	}
 
 	@Override
 	public void writeLoc(CallableStatement freq_loc_add,
 			PreparedStatement geoIP, CallableStatement lookup)
 			throws SQLException {
 		int loc = GeoLocator.getLocFromIp(this.source, geoIP);
 		if (loc != -1) { // do we even have a valid location?
 			if (this.status == Status.ACCEPTED) { // don't record locations for
 													// failed logins
 				freq_loc_add.setInt(1, this.user.getId());
 				freq_loc_add.setInt(2, loc);
 				freq_loc_add.setLong(3, Connect.ttl);
 				freq_loc_add.setLong(4, this.time);
 				freq_loc_add.registerOutParameter(5, Types.INTEGER);
 				freq_loc_add.registerOutParameter(6, Types.INTEGER);
 				freq_loc_add.execute();
 				int count = freq_loc_add.getInt(5);
 				if (count >= Connect.frequency) {
 					this.isFreqLoc = freq_loc_add.getInt(6);
 				} else {
 					this.isFreqLoc = -1;
 				}
 				return;
 			} else { // it's failed, don't record, just lookup
 				lookup.setInt(1, this.user.getId());
 				lookup.setInt(2, loc);
 				lookup.registerOutParameter(3, Types.INTEGER);
 				lookup.registerOutParameter(4, Types.INTEGER);
 				lookup.execute();
 				if (lookup.getInt(3) >= Connect.frequency) {
 					this.isFreqLoc = lookup.getInt(4);
 				} else {
 					this.isFreqLoc = -1;
 				}
 			}
 		} else {
 			System.out.println("Unknown location for ip: "
 					+ this.source.getHostAddress() + "user: "
 					+ this.user.getName());
 		}
 	}
 
 	@Override
 	public void writeTime(CallableStatement freq_time_add,
 			CallableStatement lookup) throws SQLException {
 		if (this.status == Status.ACCEPTED) {
 			freq_time_add.setInt(1, this.user.getId());
 			freq_time_add.setLong(2, this.time%TimeUnit.MILLISECONDS.convert(1l, TimeUnit.DAYS));
 			freq_time_add.setLong(3, Connect.timeAllowance);
 			freq_time_add.setLong(4, Connect.ttl);
 			freq_time_add.setLong(5, this.time);
 			freq_time_add.registerOutParameter(6, Types.INTEGER);
 			freq_time_add.registerOutParameter(7, Types.INTEGER);
 			freq_time_add.execute();
 			if (freq_time_add.getInt(6) >= Connect.frequency) {
 				this.isFreqTime = freq_time_add.getLong(7);
 			} else {
 				this.isFreqTime = -1;
 			}
 		} else {// failed login attempt, don't increment, just lookup
 			lookup.setInt(1, this.user.getId());
 			lookup.setLong(2, this.time%TimeUnit.MILLISECONDS.convert(1l, TimeUnit.DAYS));
 			lookup.registerOutParameter(3, Types.INTEGER);
 			lookup.registerOutParameter(4, Types.INTEGER);
 			lookup.execute();
 			if (lookup.getInt(3) >= Connect.frequency){
 				this.isFreqTime = lookup.getLong(4);
 			} else {
 				this.isFreqTime = -1;
 			}
 		}
 
 	}
 
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + connectID;
 		result = prime * result + isFreqLoc;
 		result = prime * result + (int) (isFreqTime ^ (isFreqTime >>> 32));
 		result = prime * result + port;
 		result = prime * result + ((rawLine == null) ? 0 : rawLine.hashCode());
 		result = prime * result + ((server == null) ? 0 : server.hashCode());
 		result = prime * result + ((source == null) ? 0 : source.hashCode());
 		result = prime * result + ((status == null) ? 0 : status.hashCode());
 		result = prime * result + (int) (time ^ (time >>> 32));
 		result = prime * result + ((type == null) ? 0 : type.hashCode());
 		result = prime * result + ((user == null) ? 0 : user.hashCode());
 		return result;
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj) {
 			return true;
 		}
 		if (obj == null) {
 			return false;
 		}
 		if (getClass() != obj.getClass()) {
 			return false;
 		}
 		Connect other = (Connect) obj;
 		if (connectID != other.connectID) {
 			return false;
 		}
 		if (isFreqLoc != other.isFreqLoc) {
 			return false;
 		}
 		if (isFreqTime != other.isFreqTime) {
 			return false;
 		}
 		if (port != other.port) {
 			return false;
 		}
 		if (rawLine == null) {
 			if (other.rawLine != null) {
 				return false;
 			}
 		} else if (!rawLine.equals(other.rawLine)) {
 			return false;
 		}
 		if (server == null) {
 			if (other.server != null) {
 				return false;
 			}
 		} else if (!server.equals(other.server)) {
 			return false;
 		}
 		if (source == null) {
 			if (other.source != null) {
 				return false;
 			}
 		} else if (!source.equals(other.source)) {
 			return false;
 		}
 		if (status != other.status) {
 			return false;
 		}
 		if (time != other.time) {
 			return false;
 		}
 		if (type != other.type) {
 			return false;
 		}
 		if (user == null) {
 			if (other.user != null) {
 				return false;
 			}
 		} else if (!user.equals(other.user)) {
 			return false;
 		}
 		return true;
 	}
 
 
 }
