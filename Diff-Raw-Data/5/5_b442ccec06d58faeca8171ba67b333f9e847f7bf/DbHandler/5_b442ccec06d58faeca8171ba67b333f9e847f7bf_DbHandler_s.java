 package ch.cern.atlas.apvs.server;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Timestamp;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.concurrent.Executors;
 import java.util.concurrent.ScheduledExecutorService;
 import java.util.concurrent.ScheduledFuture;
 import java.util.concurrent.TimeUnit;
 
 import javax.sql.DataSource;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import ch.cern.atlas.apvs.client.domain.Device;
 import ch.cern.atlas.apvs.client.domain.HistoryMap;
 import ch.cern.atlas.apvs.client.domain.Intervention;
 import ch.cern.atlas.apvs.client.domain.InterventionMap;
 import ch.cern.atlas.apvs.client.domain.Ternary;
 import ch.cern.atlas.apvs.client.domain.User;
 import ch.cern.atlas.apvs.client.event.ConnectionStatusChangedRemoteEvent;
 import ch.cern.atlas.apvs.client.event.ConnectionStatusChangedRemoteEvent.ConnectionType;
 import ch.cern.atlas.apvs.client.event.InterventionMapChangedRemoteEvent;
 import ch.cern.atlas.apvs.client.service.SortOrder;
 import ch.cern.atlas.apvs.domain.Event;
 import ch.cern.atlas.apvs.domain.History;
 import ch.cern.atlas.apvs.domain.Measurement;
 import ch.cern.atlas.apvs.eventbus.shared.RemoteEventBus;
 import ch.cern.atlas.apvs.eventbus.shared.RequestRemoteEvent;
 import ch.cern.atlas.apvs.ptu.server.Scale;
 import ch.cern.atlas.apvs.util.StringUtils;
 
 import com.google.gwt.view.client.Range;
 
 public class DbHandler extends DbReconnectHandler {
 
 	private Logger log = LoggerFactory.getLogger(getClass().getName());
 	private final RemoteEventBus eventBus;
 
 	private InterventionMap interventions = new InterventionMap();
 
 	private static final boolean DEBUG = true;
 
 	private Ternary updated = Ternary.Unknown;
 
 	private long time;
 
 	public DbHandler(final RemoteEventBus eventBus) {
 		super(eventBus);
 		this.eventBus = eventBus;
 
 		time = new Date().getTime();
 
 		RequestRemoteEvent.register(eventBus, new RequestRemoteEvent.Handler() {
 
 			@Override
 			public void onRequestEvent(RequestRemoteEvent event) {
 				String type = event.getRequestedClassName();
 
 				if (type.equals(InterventionMapChangedRemoteEvent.class
 						.getName())) {
 					InterventionMapChangedRemoteEvent.fire(eventBus,
 							interventions);
 				} else if (type.equals(ConnectionStatusChangedRemoteEvent.class
 						.getName())) {
 					ConnectionStatusChangedRemoteEvent.fire(eventBus,
 							ConnectionType.databaseConnect, isConnected());
 					ConnectionStatusChangedRemoteEvent.fire(eventBus,
 							ConnectionType.databaseUpdate, updated);
 				}
 			}
 		});
 
 		ScheduledExecutorService executor = Executors
 				.newSingleThreadScheduledExecutor();
 		executor.scheduleWithFixedDelay(new Runnable() {
 
 			ScheduledFuture<?> watchdog;
 
 			@Override
 			public void run() {
 				try {
 					if (isConnected()) {
 						if (!checkConnection()) {
 							log.warn("DB no longer reachable");
 						}
 						try {
 							updateInterventions();
 							if (!checkUpdate()) {
 								log.warn("DB no longer updated");
 							}
 
 						} catch (SQLException e) {
 							log.warn(
 									"Could not regularly-update intervention list: ",
 									e);
 						}
 
 						if (watchdog != null) {
 							watchdog.cancel(false);
 						}
 						watchdog = scheduleWatchDog();
 					}
 				} catch (Exception e) {
 					log.warn(e.getMessage());
 				}
 			}
 		}, 0, 30, TimeUnit.SECONDS);
 
 	}
 
 	private ScheduledFuture<?> scheduleWatchDog() {
 		ScheduledExecutorService executor = Executors
 				.newSingleThreadScheduledExecutor();
 		return executor.schedule(new Runnable() {
 
 			@Override
 			public void run() {
 				Date now = new Date();
 				log.error("Failed to reset watchdog, terminating server at "
 						+ now + " after " + (now.getTime() - time) / 1000
 						+ " seconds.");
 				System.exit(1);
 			}
 		}, 45, TimeUnit.SECONDS);
 	}
 
 	public HistoryMap getHistoryMap(List<String> ptuIdList, Date from)
 			throws SQLException {
 		// NOTE: we could optimize the query by running a count and see if the #
 		// is not too large, then just move forward the from time.
 		String sql = "select tbl_measurements.ID, NAME, SENSOR, DATETIME, UNIT, VALUE, SAMPLINGRATE, METHOD, UP_THRES, DOWN_THRES from tbl_measurements, tbl_devices "
 				+ "where tbl_measurements.device_id = tbl_devices.id"
 				+ " and NAME = ?"
 				+ (from != null ? " and datetime > ?" : "")
 				+ " order by DATETIME asc";
 
 		Connection connection = getConnection();
 		PreparedStatement historyQuery = connection.prepareStatement(sql);
 
 		HistoryMap map = new HistoryMap();
 
 		try {
 			for (String ptuId : ptuIdList) {
 				// at most from fromTime or startTime
 				long startTime = interventions.get(ptuId).getStartTime()
 						.getTime();
 				long fromTime = from.getTime();
 
 				historyQuery.setString(1, ptuId);
 
 				if (from != null) {
 					historyQuery.setTimestamp(2,
 							new Timestamp(Math.max(startTime, fromTime)));
 				}
 
 				long now = new Date().getTime();
 				int total = 0;
 				ResultSet result = historyQuery.executeQuery();
 				try {
 					while (result.next()) {
 						long time = result.getTimestamp("datetime").getTime();
 						if (time > now + 60000) {
 							break;
 						}
 
 						Integer id = result.getInt("id");
 						String sensor = result.getString("sensor");
 						Number value = toDouble(result.getString("value"));
 						String unit = result.getString("unit");
 
 						// Fix for #488, invalid db entry
 						if ((sensor == null) || (value == null)
 								|| (unit == null)) {
 							log.warn("MeasurementTable ID "
 									+ id
 									+ " contains <null> sensor, value or unit ("
 									+ sensor + ", " + value + ", " + unit
 									+ ") for ptu: " + ptuId);
 							continue;
 						}
 
 						Number low = toDouble(result.getString("down_thres"));
 						Number high = toDouble(result.getString("up_thres"));
 
 						Integer samplingRate = toInt(result
 								.getString("samplingrate"));
 
 						// Scale down to microSievert
 						value = Scale.getValue(value, unit);
 						low = Scale.getLowLimit(low, unit);
 						high = Scale.getHighLimit(high, unit);
 						unit = Scale.getUnit(unit);
 
 						History history = map.get(ptuId, sensor);
 						if (history == null) {
 
 							if ((sensor.equals("Temperature") || sensor
 									.equals("BodyTemperature"))
 									&& unit.equals("C")) {
 								unit = "&deg;C";
 							}
 
 							history = new History(ptuId, sensor, unit);
 							map.put(history);
 						}
 
 						if (history.addEntry(time, value, low, high,
 								samplingRate)) {
 							total++;
 						}
 					}
 				} finally {
 					result.close();
 				}
 
 				if (DEBUG) {
 					System.err.println("Total entries in history for " + ptuId
 							+ " " + total);
 				}
 			}
 		} finally {
 			historyQuery.close();
 			connection.close();
 		}
 
 		return map;
 	}
 
 	@Override
 	public void dbConnected(DataSource datasource) throws SQLException {
 		super.dbConnected(datasource);
 
 		log.info("DB disconnected");
 
 		ConnectionStatusChangedRemoteEvent.fire(eventBus,
 				ConnectionType.databaseConnect, true);
 
 		updateInterventions();
 	}
 
 	@Override
 	public void dbDisconnected() throws SQLException {
 		super.dbDisconnected();
 
 		log.warn("DB disconnected");
 
 		ConnectionStatusChangedRemoteEvent.fire(eventBus,
 				ConnectionType.databaseConnect, false);
 		ConnectionStatusChangedRemoteEvent.fire(eventBus,
 				ConnectionType.databaseUpdate, false);
 
 		interventions.clear();
 		InterventionMapChangedRemoteEvent.fire(eventBus, interventions);
 	}
 
 	private boolean checkUpdate() throws SQLException {
 		String sql = "select DATETIME from tbl_measurements order by DATETIME DESC";
 
 		Connection connection = getConnection();
 		PreparedStatement updateQuery = connection.prepareStatement(sql);
 
 		long now = new Date().getTime();
 		ResultSet result = updateQuery.executeQuery();
 
 		try {
 			if (result.next()) {
 				long time = result.getTimestamp("datetime").getTime();
 				updated = (time > now - (3 * 60000)) ? Ternary.True
 						: Ternary.False;
 			} else {
 				updated = Ternary.False;
 			}
 		} finally {
 			result.close();
 			updateQuery.close();
 			connection.close();
 		}
 
 		ConnectionStatusChangedRemoteEvent.fire(eventBus,
 				ConnectionType.databaseUpdate, updated);
 
 		return !updated.isFalse();
 	}
 
 	private String getSql(String sql, Range range, SortOrder[] order) {
 		StringBuffer s = new StringBuffer(sql);
 		for (int i = 0; i < order.length; i++) {
 			if (i == 0) {
 				s.append(" order by ");
 			}
 			s.append(order[i].getName());
 			s.append(" ");
 			s.append(order[i].isAscending() ? "ASC" : "DESC");
 			if (i + 1 < order.length) {
 				s.append(", ");
 			}
 		}
 		return s.toString();
 	}
 
 	private int getCount(PreparedStatement statement) throws SQLException {
 		ResultSet result = statement.executeQuery();
 
 		try {
 			return result.next() ? result.getInt(1) : 0;
 		} finally {
 			result.close();
 			statement.close();
 		}
 	}
 
 	public int getInterventionCount() throws SQLException {
 		Connection connection = getConnection();
 
 		try {
 			return getCount(connection
 					.prepareStatement("select count(*) from tbl_inspections"));
 		} finally {
 			connection.close();
 		}
 	}
 
 	public List<Intervention> getInterventions(Range range, SortOrder[] order)
 			throws SQLException {
 		String sql = "select tbl_inspections.ID as ID, tbl_users.FNAME, tbl_users.LNAME, tbl_devices.NAME, "
 				+ "tbl_inspections.STARTTIME, tbl_inspections.ENDTIME, tbl_inspections.DSCR, "
 				+ "tbl_inspections.IMPACT_NUM, tbl_inspections.REC_STATUS, tbl_users.id as USER_ID, tbl_devices.id as DEVICE_ID "
 				+ "from tbl_inspections "
 				+ "join tbl_users on tbl_inspections.user_id = tbl_users.id "
 				+ "join tbl_devices on tbl_inspections.device_id = tbl_devices.id";
 
 		Connection connection = getConnection();
 		String fullSql = getSql(sql, range, order);
 //		System.err.println(fullSql);
 		PreparedStatement statement = connection.prepareStatement(fullSql);
 		ResultSet result = statement.executeQuery();
 
 		List<Intervention> list = new ArrayList<Intervention>(range.getLength());
 		try {
 			// FIXME, #173 using some SQL this may be faster
 			// skip to start, result.absolute not implemented by Oracle
 			for (int i = 0; i < range.getStart() && result.next(); i++) {
 			}
 
 			for (int i = 0; i < range.getLength() && result.next(); i++) {
 				list.add(new Intervention(result.getInt("id"), result
 						.getInt("user_id"), result.getString("fname"), result
 						.getString("lname"), result.getInt("device_id"), result
 						.getString("name"), new Date(result.getTimestamp(
 						"starttime").getTime()),
 						result.getTimestamp("endtime") != null ? new Date(
 								result.getTimestamp("endtime").getTime())
 								: null, result.getString("impact_num"), result
 								.getDouble("rec_status"), result
 								.getString("dscr")));
 			}
 		} finally {
 			result.close();
 			statement.close();
 			connection.close();
 		}
 
 		return list;
 	}
 
 	public int getEventCount(String ptuId, String measurementName)
 			throws SQLException {
 		String sql = "select count(*) from tbl_events join tbl_devices on tbl_events.device_id = tbl_devices.id";
 		if ((ptuId != null) || (measurementName != null)) {
 			sql += " where";
 			if (ptuId != null) {
 				sql += " tbl_devices.name = ?";
 			}
 			if (measurementName != null) {
 				if (ptuId != null) {
 					sql += " and";
 				}
 				sql += " tbl_events.sensor = ?";
 			}
 		}
 
 		Connection connection = getConnection();
 
 		try {
 			PreparedStatement statement = connection.prepareStatement(sql);
 			int param = 1;
 			if (ptuId != null) {
 				statement.setString(param, ptuId);
 				param++;
 			}
 			if (measurementName != null) {
 				statement.setString(param, measurementName);
 				param++;
 			}
 
 			return getCount(statement);
 		} finally {
 			connection.close();
 		}
 	}
 
 	public List<Event> getEvents(Range range, SortOrder[] order, String ptuId,
 			String measurementName) throws SQLException {
 
 		String sql = "select tbl_devices.name, tbl_events.sensor, tbl_events.event_type, "
 				+ "tbl_events.value, tbl_events.threshold, tbl_events.datetime, tbl_events.unit "
 				+ "from tbl_events "
 				+ "join tbl_devices on tbl_events.device_id = tbl_devices.id";
 		if ((ptuId != null) || (measurementName != null)) {
 			sql += " where";
 			if (ptuId != null) {
 				sql += " tbl_devices.name = ?";
 			}
 			if (measurementName != null) {
 				if (ptuId != null) {
 					sql += " and";
 				}
 				sql += " tbl_events.sensor = ?";
 			}
 		}
 
 		Connection connection = getConnection();
 
 		String s = getSql(sql, range, order);
 		// log.info("SQL: "+s);
 		PreparedStatement statement = connection.prepareStatement(s);
 		int param = 1;
 		if (ptuId != null) {
 			statement.setString(param, ptuId);
 			param++;
 		}
 		if (measurementName != null) {
 			statement.setString(param, measurementName);
 			param++;
 		}
 
 		ResultSet result = statement.executeQuery();
 
 		List<Event> list = new ArrayList<Event>(range.getLength());
 		try {
 			// FIXME, #173 using some SQL this may be faster
 			// skip to start, result.absolute not implemented by Oracle
 			for (int i = 0; i < range.getStart() && result.next(); i++) {
 			}
 
 			for (int i = 0; i < range.getLength() && result.next(); i++) {
 				String name = result.getString("name");
 				Double value = toDouble(result.getString("value"));
 				Double threshold = toDouble(result.getString("threshold"));
 				String unit = result.getString("unit");
 
 				list.add(new Event(name, result.getString("sensor"), result
 						.getString("event_type"), value, threshold, unit,
 						new Date(result.getTimestamp("datetime").getTime())));
 			}
 		} finally {
 			result.close();
 			statement.close();
 			connection.close();
 		}
 
 		return list;
 	}
 
 	private Double toDouble(String string) {
 		try {
 			return string != null ? Double.parseDouble(string) : null;
 		} catch (NumberFormatException e) {
 			log.warn("NumberFormat Exception (toDouble) in DbHandler: '"
 					+ string + "'");
 			return null;
 		}
 	}
 
 	private Integer toInt(String string) {
 		try {
 			return string != null ? Integer.parseInt(string) : null;
 		} catch (NumberFormatException e) {
 			log.warn("NumberFormat Exception (toInt) in DbHandler: '" + string
 					+ "'");
 			return null;
 		}
 	}
 
 	public synchronized void addUser(User user) throws SQLException {
 		Connection connection = getConnection();
 		PreparedStatement addUser = connection
 				.prepareStatement("insert into tbl_users (fname, lname, cern_id) values (?, ?, ?)");
 		try {
 			addUser.setString(1, user.getFirstName());
 			addUser.setString(2, user.getLastName());
 			addUser.setString(3, user.getCernId());
 			addUser.executeUpdate();
 		} finally {
 			addUser.close();
 			connection.close();
 		}
 	}
 
 	public synchronized void addDevice(Device device) throws SQLException {
 		Connection connection = getConnection();
 		PreparedStatement addDevice = connection
				.prepareStatement("insert into tbl_devices (name, ip, dscr) values (?, ?, ?)");
 		try {
 			addDevice.setString(1, device.getName());
 			addDevice.setString(2, device.getIp());
 			addDevice.setString(3, device.getDescription());
 			addDevice.executeUpdate();
 		} finally {
 			addDevice.close();
 			connection.close();
 		}
 	}
 
 	public synchronized void addIntervention(Intervention intervention)
 			throws SQLException {
 		Connection connection = getConnection();
 		PreparedStatement addIntervention = connection
 				.prepareStatement("insert into tbl_inspections (user_id, device_id, starttime, dscr, impact_num) values (?, ?, ?, ?, ?)");
 
 		try {
 			addIntervention.setInt(1, intervention.getUserId());
 			addIntervention.setInt(2, intervention.getDeviceId());
 			addIntervention.setTimestamp(3, new Timestamp(intervention
 					.getStartTime().getTime()));
 			addIntervention.setString(4, intervention.getDescription());
 			addIntervention.setString(5, intervention.getImpactNumber());
 			addIntervention.executeUpdate();
 
 			updateInterventions();
 		} finally {
 			addIntervention.close();
 			connection.close();
 		}
 	}
 
 	public synchronized void endIntervention(int id, Date date)
 			throws SQLException {
 		Connection connection = getConnection();
 		PreparedStatement endIntervention = connection
 				.prepareStatement("update tbl_inspections set endtime = ? where id = ?");
 
 		try {
 			endIntervention.setTimestamp(1, new Timestamp(date.getTime()));
 			endIntervention.setInt(2, id);
 			endIntervention.executeUpdate();
 
 			updateInterventions();
 		} finally {
 			endIntervention.close();
 			connection.close();
 		}
 	}
 
 	public List<User> getUsers(boolean notBusy) throws SQLException {
 		return notBusy ? getNotBusyUsers() : getAllUsers();
 	}
 
 	private synchronized List<User> getNotBusyUsers() throws SQLException {
 		Connection connection = getConnection();
 		PreparedStatement notBusyUserQuery = connection
 				.prepareStatement("select ID, FNAME, LNAME, CERN_ID from tbl_users "
 						+ "where id not in ("
 						+ "select user_id from tbl_inspections "
 						+ "where endtime is null) " + "order by LNAME, FNAME");
 		try {
 			return getUserList(notBusyUserQuery.executeQuery());
 		} finally {
 			notBusyUserQuery.close();
 			connection.close();
 		}
 	}
 
 	private synchronized List<User> getAllUsers() throws SQLException {
 		Connection connection = getConnection();
 		PreparedStatement userQuery = connection
 				.prepareStatement("select ID, FNAME, LNAME, CERN_ID from tbl_users order by LNAME, FNAME");
 
 		try {
 			return getUserList(userQuery.executeQuery());
 		} finally {
 			userQuery.close();
 			connection.close();
 		}
 	}
 
 	private synchronized List<User> getUserList(ResultSet result)
 			throws SQLException {
 		List<User> list = new ArrayList<User>();
 		try {
 			while (result.next()) {
 				list.add(new User(result.getInt("ID"), result
 						.getString("FNAME"), result.getString("LNAME"), result
 						.getString("CERN_ID")));
 			}
 		} finally {
 			result.close();
 		}
 		return list;
 	}
 
 	public List<Device> getDevices(boolean notBusy) throws SQLException {
 		return notBusy ? getNotBusyDevices() : getDevices();
 	}
 
 	private synchronized List<Device> getNotBusyDevices() throws SQLException {
 		Connection connection = getConnection();
 		PreparedStatement notBusyDeviceQuery = connection
 				.prepareStatement("select ID, NAME, IP, DSCR, MAC_ADDR, HOST_NAME from tbl_devices "
 						+ "where id not in ("
 						+ "select device_id from tbl_inspections "
 						+ "where endtime is null) " + "order by NAME");
 
 		try {
 			return getDeviceList(notBusyDeviceQuery.executeQuery());
 		} finally {
 			notBusyDeviceQuery.close();
 			connection.close();
 		}
 	}
 
 	private List<Device> getDevices() throws SQLException {
 		Connection connection = getConnection();
 		PreparedStatement deviceQuery = connection
 				.prepareStatement("select ID, NAME, IP, DSCR, MAC_ADDR, HOST_NAME from tbl_devices order by NAME");
 
 		try {
 			return getDeviceList(deviceQuery.executeQuery());
 		} finally {
 			deviceQuery.close();
 			connection.close();
 		}
 	}
 
 	private synchronized List<Device> getDeviceList(ResultSet result)
 			throws SQLException {
 		List<Device> list = new ArrayList<Device>();
 		try {
 			while (result.next()) {
 				list.add(new Device(result.getInt("ID"), result
 						.getString("NAME"), result.getString("IP"), result
 						.getString("DSCR"), result.getString("MAC_ADDR"),
 						result.getString("HOST_NAME")));
 			}
 		} finally {
 			result.close();
 		}
 		return list;
 	}
 
 	public void updateInterventionImpactNumber(int id, String impactNumber)
 			throws SQLException {
 		Connection connection = getConnection();
 		PreparedStatement updateInterventionImpactNumber = connection
 				.prepareStatement("update tbl_inspections set impact_num = ? where id=?");
 
 		try {
 			System.err.println("Updating impact");
 			updateInterventionImpactNumber.setString(1, impactNumber);
 			updateInterventionImpactNumber.setInt(2, id);
 			updateInterventionImpactNumber.executeUpdate();
 
 			updateInterventions();
 		} finally {
 			updateInterventionImpactNumber.close();
 			connection.close();
 		}
 	}
 
 	public synchronized void updateInterventionDescription(int id,
 			String description) throws SQLException {
 		Connection connection = getConnection();
 		PreparedStatement updateInterventionDescription = connection
 				.prepareStatement("update tbl_inspections set dscr = ? where id=?");
 
 		try {
 			updateInterventionDescription.setString(1, description);
 			updateInterventionDescription.setInt(2, id);
 			updateInterventionDescription.executeUpdate();
 
 			updateInterventions();
 		} finally {
 			updateInterventionDescription.close();
 			connection.close();
 		}
 	}
 
 	public synchronized Intervention getIntervention(String ptuId)
 			throws SQLException {
 		Connection connection = getConnection();
 		PreparedStatement getIntervention = connection
 				.prepareStatement("select tbl_inspections.ID, tbl_inspections.USER_ID, tbl_users.FNAME, tbl_users.LNAME, "
 						+ "tbl_inspections.DEVICE_ID, tbl_devices.NAME, tbl_inspections.STARTTIME, tbl_inspections.ENDTIME, "
 						+ "tbl_inspections.DSCR, tbl_inspections.IMPACT_NUM, tbl_inspections.REC_STATUS from tbl_inspections "
 						+ "join tbl_devices on tbl_inspections.device_id = tbl_devices.id "
 						+ "join tbl_users on tbl_inspections.user_id = tbl_users.id "
 						+ "where tbl_inspections.endtime is null "
 						+ "and tbl_devices.name = ? "
 						+ "order by starttime desc");
 
 		getIntervention.setString(1, ptuId);
 		ResultSet result = getIntervention.executeQuery();
 		try {
 			if (result.next()) {
 				return new Intervention(result.getInt("id"),
 						result.getInt("user_id"), result.getString("fname"),
 						result.getString("lname"), result.getInt("device_id"),
 						result.getString("name"),
 						result.getTimestamp("starttime"),
 						result.getTimestamp("endtime"),
 						result.getString("impact_num"),
 						result.getDouble("rec_status"),
 						result.getString("dscr"));
 			}
 		} finally {
 			result.close();
 			getIntervention.close();
 			connection.close();
 		}
 		return null;
 	}
 
 	private synchronized void updateInterventions() throws SQLException {
 
 		Connection connection = getConnection();
 		PreparedStatement updateInterventions = connection
 				.prepareStatement("select tbl_inspections.ID, tbl_inspections.USER_ID, tbl_users.FNAME, tbl_users.LNAME, "
 						+ "tbl_inspections.DEVICE_ID, tbl_devices.NAME, tbl_inspections.STARTTIME, tbl_inspections.ENDTIME, "
 						+ "tbl_inspections.DSCR, tbl_inspections.IMPACT_NUM, tbl_inspections.REC_STATUS from tbl_inspections "
 						+ "join tbl_devices on tbl_inspections.device_id = tbl_devices.id "
 						+ "join tbl_users on tbl_inspections.user_id = tbl_users.id "
 						+ "where tbl_inspections.endtime is null");
 
 		ResultSet result = updateInterventions.executeQuery();
 		InterventionMap newMap = new InterventionMap();
 		try {
 			while (result.next()) {
 				String name = result.getString("name");
 				newMap.put(
 						name,
 						new Intervention(result.getInt("id"), result
 								.getInt("user_id"), result.getString("fname"),
 								result.getString("lname"), result
 										.getInt("device_id"), name, result
 										.getTimestamp("starttime"), result
 										.getTimestamp("endtime"), result
 										.getString("impact_num"), result
 										.getDouble("rec_status"), result
 										.getString("dscr")));
 			}
 		} finally {
 			result.close();
 			updateInterventions.close();
 			connection.close();
 		}
 
 		if (!interventions.equals(newMap)) {
 			interventions = newMap;
 			InterventionMapChangedRemoteEvent.fire(eventBus, interventions);
 		}
 	}
 
 	public List<Measurement> getMeasurements(String ptuId, String name)
 			throws SQLException {
 		List<String> ptuIdList = null;
 		if (ptuId != null) {
 			ptuIdList = new ArrayList<String>();
 			ptuIdList.add(ptuId);
 		}
 		return getMeasurements(ptuIdList, name);
 	}
 
 	/**
 	 * 
 	 * @param ptuId
 	 *            can be null
 	 * @param name
 	 *            can be null
 	 * @return
 	 * @throws SQLException
 	 */
 	public List<Measurement> getMeasurements(List<String> ptuIdList, String name)
 			throws SQLException {
 
 		String sql = "select NAME, view_last_measurements_date.SENSOR, VALUE, SAMPLINGRATE, UNIT, METHOD, UP_THRES, DOWN_THRES, view_last_measurements_date.DATETIME "
 				+ "from view_last_measurements_date, tbl_measurements, tbl_devices "
 				+ "where view_last_measurements_date.datetime = tbl_measurements.datetime "
 				+ "and view_last_measurements_date.sensor = tbl_measurements.sensor "
 				+ "and view_last_measurements_date.device_id = tbl_measurements.device_id "
 				+ "and view_last_measurements_date.device_id = tbl_devices.id";
 		if (ptuIdList != null) {
 			sql += " and NAME in ("
 					+ StringUtils.join(ptuIdList.toArray(), ',', '\'') + ")";
 		}
 		if (name != null) {
 			sql += " and view_last_measurements_date.SENSOR = ?";
 		}
 
 		Connection connection = getConnection();
 		PreparedStatement statement = connection.prepareStatement(sql);
 		int param = 1;
 		if (name != null) {
 			statement.setString(param, name);
 			param++;
 		}
 
 		ResultSet result = statement.executeQuery();
 
 		List<Measurement> list = new ArrayList<Measurement>();
 		try {
 			while (result.next()) {
 				String sensor = result.getString("SENSOR");
 				String unit = result.getString("UNIT");
 				Number value = toDouble(result.getString("VALUE"));
 				Number low = toDouble(result.getString("DOWN_THRES"));
 				Number high = toDouble(result.getString("UP_THRES"));
 
 				// if equal of low higher than high, no limits to be shown
 				if (low != null && high != null
 						&& low.doubleValue() >= high.doubleValue()) {
 					low = null;
 					high = null;
 				}
 
 				// Scale down to microSievert
 				value = Scale.getValue(value, unit);
 				low = Scale.getLowLimit(low, unit);
 				high = Scale.getHighLimit(high, unit);
 				unit = Scale.getUnit(unit);
 
 				Measurement m = new Measurement(result.getString("NAME"),
 						sensor, value, low, high, unit,
 						toInt(result.getString("SAMPLINGRATE")), new Date(
 								result.getTimestamp("DATETIME").getTime()));
 				list.add(m);
 			}
 		} finally {
 			result.close();
 			statement.close();
 			connection.close();
 		}
 		return list;
 	}
 }
