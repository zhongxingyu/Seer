 package ch.cern.atlas.apvs.server;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.sql.Timestamp;
 import java.util.ArrayDeque;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Date;
 import java.util.Deque;
 import java.util.List;
 import java.util.concurrent.Executors;
 import java.util.concurrent.ScheduledExecutorService;
 import java.util.concurrent.TimeUnit;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import ch.cern.atlas.apvs.client.domain.Device;
 import ch.cern.atlas.apvs.client.domain.Intervention;
 import ch.cern.atlas.apvs.client.domain.User;
 import ch.cern.atlas.apvs.client.event.InterventionMapChangedEvent;
 import ch.cern.atlas.apvs.client.service.SortOrder;
 import ch.cern.atlas.apvs.client.settings.InterventionMap;
 import ch.cern.atlas.apvs.domain.Event;
 import ch.cern.atlas.apvs.domain.History;
 import ch.cern.atlas.apvs.domain.Measurement;
 import ch.cern.atlas.apvs.eventbus.shared.RemoteEventBus;
 import ch.cern.atlas.apvs.eventbus.shared.RequestRemoteEvent;
 import ch.cern.atlas.apvs.ptu.server.PtuServerConstants;
 
 import com.google.gwt.view.client.Range;
 
 public class DbHandler extends DbReconnectHandler {
 
 	private Logger log = LoggerFactory.getLogger(getClass().getName());
 	private final RemoteEventBus eventBus;
 
 	private InterventionMap interventions = new InterventionMap();
 
 	private PreparedStatement historyQueryCount;
 	private PreparedStatement historyQuery;
 	private PreparedStatement deviceQuery;
 	private PreparedStatement notBusyDeviceQuery;
 	private PreparedStatement userQuery;
 	private PreparedStatement notBusyUserQuery;
 	private PreparedStatement addUser;
 	private PreparedStatement addDevice;
 	private PreparedStatement addIntervention;
 	private PreparedStatement endIntervention;
 	private PreparedStatement getIntervention;
 	private PreparedStatement updateInterventionDescription;
 	private PreparedStatement measurementQuery;
 	private PreparedStatement measurementListQuery;
 
 	public DbHandler(final RemoteEventBus eventBus) {
 		super();
 		this.eventBus = eventBus;
 
 		RequestRemoteEvent.register(eventBus, new RequestRemoteEvent.Handler() {
 
 			@Override
 			public void onRequestEvent(RequestRemoteEvent event) {
 				String type = event.getRequestedClassName();
 
 				if (type.equals(InterventionMapChangedEvent.class.getName())) {
 					InterventionMapChangedEvent.fire(eventBus, interventions);
 				}
 			}
 		});
 
 		ScheduledExecutorService executor = Executors
 				.newSingleThreadScheduledExecutor();
 		executor.scheduleAtFixedRate(new Runnable() {
 
 			@Override
 			public void run() {
 				try {
 					updateInterventions();
 				} catch (SQLException e) {
 					log.warn("Could not regularly-update intervention list: ",
 							e);
 				}
 			}
 		}, 30, 30, TimeUnit.SECONDS);
 	}
 
 	// NOTE: needs to be synchronized OR needs to have separate
 	// preparedStatements...
 	public synchronized History getHistory(String ptuId, String sensor)
 			throws SQLException {
 		// check on history and load from DB
 		if ((historyQuery == null) || (historyQueryCount == null)) {
 			return null;
 		}
 
 		long PERIOD = 36; // hours
 		Date then = new Date(new Date().getTime() - (PERIOD * 3600000));
 		String timestamp = PtuServerConstants.timestampFormat.format(then);
 
 		historyQueryCount.setString(1, sensor);
 		historyQueryCount.setString(2, ptuId);
 		historyQueryCount.setString(3, timestamp);
 		historyQuery.setString(1, sensor);
 		historyQuery.setString(2, ptuId);
 		historyQuery.setString(3, timestamp);
 
 		ResultSet resultCount = historyQueryCount.executeQuery();
 		int n = 0;
 		try {
 			if (resultCount.next()) {
 				n = resultCount.getInt(1);
 			}
 		} finally {
 			resultCount.close();
 		}
 
 		int MAX_ENTRIES = 1000;
 		long MIN_INTERVAL = 5000; // ms
 
 		if (n <= 0) {
 			return null;
 		}
 
 		// limit entries
 		if (n > MAX_ENTRIES) {
 			n = 1000;
 		}
 
 		Deque<Number[]> data = new ArrayDeque<Number[]>(n);
 
 		long lastTime = new Date().getTime();
 		ResultSet result = historyQuery.executeQuery();
 		String unit = "";
 		try {
 			while (result.next() && (data.size() <= n)) {
 				long time = result.getTimestamp("datetime").getTime();
 				unit = result.getString("unit");
 				double value = Double.parseDouble(result.getString("value"));
 
 				// Scale down to microSievert
 				if (unit.equals("mSv")) {
 					unit = "&micro;Sv";
 					value *= 1000;
 				}
 
 				// limit entry separation (reverse order)
 				if (lastTime - time > MIN_INTERVAL) {
 					lastTime = time;
 
 					Number[] entry = new Number[2];
 					entry[0] = time;
 					entry[1] = value;
 					data.addFirst(entry);
 				}
 			}
 
 			log.info("Creating history for " + ptuId + " " + sensor + " "
 					+ data.size() + " entries");
 			return new History(data.toArray(new Number[data.size()][]), unit);
 		} finally {
 			result.close();
 		}
 	}
 
 	@Override
 	public void dbConnected(Connection connection) throws SQLException {
 		super.dbConnected(connection);
 
 		connection.setAutoCommit(true);
 
 		historyQueryCount = connection
 				.prepareStatement("select count(*) from tbl_measurements "
 						+ "join tbl_devices on tbl_measurements.device_id = tbl_devices.id "
 						+ "where SENSOR = ? " + "and NAME = ? "
 						+ "and DATETIME > to_timestamp(?,"
 						+ PtuServerConstants.oracleFormat + ")");
 
 		historyQuery = connection
 				.prepareStatement("select DATETIME, UNIT, VALUE from tbl_measurements "
 						+ "join tbl_devices on tbl_measurements.device_id = tbl_devices.id "
 						+ "where SENSOR = ? "
 						+ "and NAME = ? "
 						+ "and DATETIME > to_timestamp(?,"
 						+ PtuServerConstants.oracleFormat
 						+ ") "
 						+ "order by DATETIME desc");
 
 		deviceQuery = connection
 				.prepareStatement("select ID, NAME, IP, DSCR from tbl_devices order by NAME");
 
 		updateInterventions();
 	}
 
 	@Override
 	public void dbDisconnected() throws SQLException {
 		super.dbDisconnected();
 
 		historyQueryCount = null;
 		historyQuery = null;
 		deviceQuery = null;
 
 		interventions.clear();
 		InterventionMapChangedEvent.fire(eventBus, interventions);
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
 
 	private int getCount(String sql) throws SQLException {
 		Statement statement = getConnection().createStatement();
 		ResultSet result = statement.executeQuery(sql);
 
 		try {
 			return result.next() ? result.getInt(1) : 0;
 		} finally {
 			result.close();
 			statement.close();
 		}
 	}
 
 	public int getInterventionCount() throws SQLException {
 		return getCount("select count(*) from tbl_inspections");
 	}
 
 	public List<Intervention> getInterventions(Range range, SortOrder[] order)
 			throws SQLException {
 
 		String sql = "select tbl_inspections.id, tbl_users.fname, tbl_users.lname, tbl_devices.name, "
 				+ "tbl_inspections.starttime, tbl_inspections.endtime, tbl_inspections.dscr, tbl_users.id, tbl_devices.id "
 				+ "from tbl_inspections "
 				+ "join tbl_users on tbl_inspections.user_id = tbl_users.id "
 				+ "join tbl_devices on tbl_inspections.device_id = tbl_devices.id";
 
 		Statement statement = getConnection().createStatement();
 		ResultSet result = statement.executeQuery(getSql(sql, range, order));
 
 		List<Intervention> list = new ArrayList<Intervention>(range.getLength());
 		try {
 			// FIXME, #173 using some SQL this may be faster
 			// skip to start, result.absolute not implemented by Oracle
 			for (int i = 0; i < range.getStart() && result.next(); i++) {
 			}
 
 			for (int i = 0; i < range.getLength() && result.next(); i++) {
 				list.add(new Intervention(result.getInt(1), result.getInt(8),
 						result.getString(2), result.getString(3), result
 								.getInt(9), result.getString(4), new Date(
 								result.getTimestamp(5).getTime()), result
 								.getTimestamp(6) != null ? new Date(result
 								.getTimestamp(6).getTime()) : null, result
 								.getString(7)));
 			}
 		} finally {
 			result.close();
 			statement.close();
 		}
 
 		return list;
 	}
 
 	public int getEventCount(String ptuId) throws SQLException {
 		String sql = "select count(*) from tbl_events";
 		if (ptuId != null) {
 			sql += "join tbl_devices on tbl_events.device_id = tbl_devices.id where tbl_devices.name = '"
 					+ ptuId + "'";
 		}
 		return getCount(sql);
 	}
 
 	public List<Event> getEvents(Range range, SortOrder[] order, String ptuId)
 			throws SQLException {
 
 		String sql = "select tbl_devices.name, tbl_events.sensor, tbl_events.event_type, "
 				+ "tbl_events.value, tbl_events.threshold, tbl_events.datetime "
 				+ "from tbl_events "
 				+ "join tbl_devices on tbl_events.device_id = tbl_devices.id";
 		if (ptuId != null) {
 			sql += " where tbl_devices.name = '" + ptuId + "'";
 		}
 
 		Statement statement = getConnection().createStatement();
 		ResultSet result = statement.executeQuery(getSql(sql, range, order));
 
 		List<Event> list = new ArrayList<Event>(range.getLength());
 		try {
 			// FIXME, #173 using some SQL this may be faster
 			// skip to start, result.absolute not implemented by Oracle
 			for (int i = 0; i < range.getStart() && result.next(); i++) {
 			}
 
 			for (int i = 0; i < range.getLength() && result.next(); i++) {
 				double value = getDouble(result.getString("value"));
 				double threshold = getDouble(result.getString("threshold"));
 
 				list.add(new Event(result.getString("name"), result
 						.getString("sensor"), result.getString("event_type"),
 						value, threshold, new Date(result.getTimestamp(
 								"datetime").getTime())));
 			}
 		} finally {
 			result.close();
 			statement.close();
 		}
 		return list;
 	}
 
 	private double getDouble(String string) {
 		if (string == null)
 			return 0;
 
 		return Double.parseDouble(string);
 	}
 
 	public synchronized void addUser(User user) throws SQLException {
 		if (addUser == null) {
 			addUser = getConnection()
 					.prepareStatement(
 							"insert into tbl_users (fname, lname, cern_id) values (?, ?, ?)");
 		}
 
 		addUser.setString(1, user.getFirstName());
 		addUser.setString(2, user.getLastName());
 		addUser.setString(3, user.getCernId());
 		addUser.executeUpdate();
 	}
 
 	public synchronized void addDevice(Device device) throws SQLException {
 		if (addDevice == null) {
 			addDevice = getConnection()
 					.prepareStatement(
 							"insert into tbl_devices (name, ip, dscr) values (?, ?, ?)");
 		}
 
 		addDevice.setString(1, device.getName());
 		addDevice.setString(2, device.getIp());
 		addDevice.setString(3, device.getDescription());
 		addDevice.executeUpdate();
 	}
 
 	public synchronized void addIntervention(Intervention intervention)
 			throws SQLException {
 		if (addIntervention == null) {
 			addIntervention = getConnection()
 					.prepareStatement(
 							"insert into tbl_inspections (user_id, device_id, starttime, dscr) values (?, ?, ?, ?)");
 		}
 
 		addIntervention.setInt(1, intervention.getUserId());
 		addIntervention.setInt(2, intervention.getDeviceId());
 		addIntervention.setTimestamp(3, new Timestamp(intervention
 				.getStartTime().getTime()));
 		addIntervention.setString(4, intervention.getDescription());
 		addIntervention.executeUpdate();
 
 		updateInterventions();
 	}
 
 	public synchronized void endIntervention(int id, Date date)
 			throws SQLException {
 		if (endIntervention == null) {
 			endIntervention = getConnection().prepareStatement(
 					"update tbl_inspections set endtime = ? where id = ?");
 		}
 
 		endIntervention.setTimestamp(1, new Timestamp(date.getTime()));
 		endIntervention.setInt(2, id);
 		endIntervention.executeUpdate();
 
 		updateInterventions();
 	}
 
 	public List<User> getUsers(boolean notBusy) throws SQLException {
 		return notBusy ? getNotBusyUsers() : getAllUsers();
 	}
 
 	private synchronized List<User> getNotBusyUsers() throws SQLException {
 		if (notBusyUserQuery == null) {
 			notBusyUserQuery = getConnection().prepareStatement(
 					"select ID, FNAME, LNAME, CERN_ID from tbl_users "
 							+ "where id not in ("
 							+ "select user_id from tbl_inspections "
 							+ "where endtime is null) "
 							+ "order by LNAME, FNAME");
 		}
 
 		return getUserList(notBusyUserQuery.executeQuery());
 	}
 
 	private synchronized List<User> getAllUsers() throws SQLException {
 		if (userQuery == null) {
 			userQuery = getConnection()
 					.prepareStatement(
 							"select ID, FNAME, LNAME, CERN_ID from tbl_users order by LNAME, FNAME");
 		}
 
 		return getUserList(userQuery.executeQuery());
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
 		if (notBusyDeviceQuery == null) {
 			notBusyDeviceQuery = getConnection().prepareStatement(
 					"select ID, NAME, IP, DSCR from tbl_devices "
 							+ "where id not in ("
 							+ "select device_id from tbl_inspections "
 							+ "where endtime is null) " + "order by NAME");
 		}
 
 		return getDeviceList(notBusyDeviceQuery.executeQuery());
 	}
 
 	private List<Device> getDevices() throws SQLException {
 		if (deviceQuery == null) {
 			return Collections.emptyList();
 		}
 
 		return getDeviceList(deviceQuery.executeQuery());
 	}
 
 	private synchronized List<Device> getDeviceList(ResultSet result)
 			throws SQLException {
 		List<Device> list = new ArrayList<Device>();
 		try {
 			while (result.next()) {
 				list.add(new Device(result.getInt("ID"), result
 						.getString("NAME"), result.getString("IP"), result
 						.getString("DSCR")));
 			}
 		} finally {
 			result.close();
 		}
 		return list;
 	}
 
 	public synchronized void updateInterventionDescription(int id,
 			String description) throws SQLException {
 		if (updateInterventionDescription == null) {
 			updateInterventionDescription = getConnection().prepareStatement(
 					"update tbl_inspections set dscr = ? where id=?");
 		}
 
 		updateInterventionDescription.setString(1, description);
 		updateInterventionDescription.setInt(2, id);
 		updateInterventionDescription.executeUpdate();
 
 		updateInterventions();
 	}
 
 	public synchronized Intervention getIntervention(String ptuId)
 			throws SQLException {
 		if (getIntervention == null) {
 			getIntervention = getConnection()
 					.prepareStatement(
 							"select tbl_inspections.ID, tbl_inspections.USER_ID, tbl_users.FNAME, tbl_users.LNAME, tbl_inspections.DEVICE_ID, tbl_devices.NAME, tbl_inspections.STARTTIME, tbl_inspections.ENDTIME, tbl_inspections.DSCR from tbl_inspections "
 									+ "join tbl_devices on tbl_inspections.device_id = tbl_devices.id "
 									+ "join tbl_users on tbl_inspections.user_id = tbl_users.id "
 									+ "where tbl_inspections.endtime is null "
 									+ "and tbl_devices.name = ? "
 									+ "order by starttime desc");
 		}
 
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
 						result.getString("dscr"));
 			}
 		} finally {
 			result.close();
 		}
 		return null;
 	}
 
 	private synchronized void updateInterventions() throws SQLException {
 		String sql = "select tbl_inspections.ID, tbl_inspections.USER_ID, tbl_users.FNAME, tbl_users.LNAME, tbl_inspections.DEVICE_ID, tbl_devices.NAME, tbl_inspections.STARTTIME, tbl_inspections.ENDTIME, tbl_inspections.DSCR from tbl_inspections "
 				+ "join tbl_devices on tbl_inspections.device_id = tbl_devices.id "
 				+ "join tbl_users on tbl_inspections.user_id = tbl_users.id "
 				+ "where tbl_inspections.endtime is null";
 
 		Statement statement = getConnection().createStatement();
 		ResultSet result = statement.executeQuery(sql);
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
 										.getString("dscr")));
 			}
 		} finally {
 			result.close();
 			statement.close();
 		}
 
 		if (!interventions.equals(newMap)) {
 			interventions = newMap;
 			InterventionMapChangedEvent.fire(eventBus, interventions);
 		}
 	}
 
 	public synchronized Measurement getMeasurement(String ptuId, String name)
 			throws SQLException {
 		if (measurementQuery == null) {
 			measurementQuery = getConnection()
 					.prepareStatement(
 							"select NAME, SENSOR, VALUE, SAMPLINGRATE, UNIT, DATETIME from tbl_measurements "
 									+ "join tbl_devices on tbl_devices.id = tbl_measurements.device_id "
 									+ "where NAME = ? "
 									+ "and SENSOR = ? "
 									+ "order by DATETIME DESC");
 		}
 
 		measurementQuery.setString(1, ptuId);
 		measurementQuery.setString(2, name);
 		ResultSet result = measurementQuery.executeQuery();
 		try {
 			if (result.next()) {
 				String unit = result.getString("UNIT");
 				double value = Double.parseDouble(result.getString("VALUE"));
 
 				// Scale down to microSievert
 				if (unit.equals("mSv")) {
 					unit = "&micro;Sv";
 					value *= 1000;
 				}
 				if (unit.equals("mSv/h")) {
 					unit = "&micro;Sv/h";
 					value *= 1000;
 				}
 
 				return new Measurement(result.getString("NAME"),
 						result.getString("SENSOR"), value,
 						Integer.parseInt(result.getString("SAMPLINGRATE")),
 						unit, new Date(result.getTimestamp("DATETIME")
 								.getTime()));
 			}
 		} finally {
 			result.close();
 		}
 		return null;
 	}
 
 	public synchronized List<Measurement> getMeasurements(String ptuId)
 			throws SQLException {
 		// FIXME we could find a faster way
 		if (measurementListQuery == null) {
 			measurementListQuery = getConnection()
 					.prepareStatement(
 							"select distinct(SENSOR) from tbl_measurements "
 									+ "join tbl_devices on tbl_devices.id = tbl_measurements.device_id "
 									+ "where NAME = ? ");
 		}
 
 		measurementListQuery.setString(1, ptuId);
 		ResultSet result = measurementListQuery.executeQuery();
 
 		List<Measurement> list = new ArrayList<Measurement>();
 		try {
 			while (result.next()) {
 				Measurement m = getMeasurement(ptuId,
 						result.getString("SENSOR"));
 				if (m != null) {
 					list.add(m);
 				}
 			}
 		} finally {
 			result.close();
 		}
 		return list;
 	}
 }
