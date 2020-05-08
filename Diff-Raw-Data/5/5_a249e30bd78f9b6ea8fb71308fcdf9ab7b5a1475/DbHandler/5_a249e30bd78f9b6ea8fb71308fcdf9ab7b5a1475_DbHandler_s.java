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
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 import java.util.concurrent.Executors;
 import java.util.concurrent.ScheduledExecutorService;
 import java.util.concurrent.TimeUnit;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import ch.cern.atlas.apvs.client.service.SortOrder;
 import ch.cern.atlas.apvs.client.ui.Device;
 import ch.cern.atlas.apvs.client.ui.Intervention;
 import ch.cern.atlas.apvs.client.ui.User;
 import ch.cern.atlas.apvs.domain.Event;
 import ch.cern.atlas.apvs.domain.History;
 import ch.cern.atlas.apvs.domain.Ptu;
 import ch.cern.atlas.apvs.eventbus.shared.RemoteEventBus;
 import ch.cern.atlas.apvs.eventbus.shared.RequestRemoteEvent;
 import ch.cern.atlas.apvs.ptu.server.PtuServerConstants;
 import ch.cern.atlas.apvs.ptu.shared.PtuIdsChangedEvent;
 
 import com.google.gwt.view.client.Range;
 
 public class DbHandler extends DbReconnectHandler {
 
 	private Logger log = LoggerFactory.getLogger(getClass().getName());
 	private final RemoteEventBus eventBus;
 
 	private Ptus ptus = Ptus.getInstance();
 
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
 
 	public DbHandler(final RemoteEventBus eventBus) {
 		super();
 		this.eventBus = eventBus;
 
 		ptus.setDbHandler(this);
 
 		RequestRemoteEvent.register(eventBus, new RequestRemoteEvent.Handler() {
 
 			@Override
 			public void onRequestEvent(RequestRemoteEvent event) {
 				String type = event.getRequestedClassName();
 
 				if (type.equals(PtuIdsChangedEvent.class.getName())) {
 					eventBus.fireEvent(new PtuIdsChangedEvent(ptus.getPtuIds()));
 				}
 			}
 		});
 
 		ScheduledExecutorService executor = Executors
 				.newSingleThreadScheduledExecutor();
 		executor.scheduleAtFixedRate(new Runnable() {
 
 			@Override
 			public void run() {
 				try {
 					updatePtus();
 				} catch (SQLException e) {
 					log.warn("Could not regularly-update device list: ", e);
 				}
 			}
 		}, 30, 30, TimeUnit.SECONDS);
 	}
 
 	public History getHistory(String ptuId, String sensor, String unit) {
 		History history = null;
 		// check on history and load from DB
 		if ((historyQuery != null) && (historyQueryCount != null)) {
 
 			long PERIOD = 36; // hours
 			Date then = new Date(new Date().getTime() - (PERIOD * 3600000));
 			String timestamp = PtuServerConstants.timestampFormat.format(then);
 
 			try {
 				historyQueryCount.setString(1, sensor);
 				historyQueryCount.setString(2, ptuId);
 				historyQueryCount.setString(3, timestamp);
 				historyQuery.setString(1, sensor);
 				historyQuery.setString(2, ptuId);
 				historyQuery.setString(3, timestamp);
 
 				ResultSet result = historyQueryCount.executeQuery();
 				int n = 0;
 				try {
 					result.next();
 
 					n = result.getInt(1);
 				} finally {
 					result.close();
 				}
 
 				int MAX_ENTRIES = 1000;
 				long MIN_INTERVAL = 5000; // ms
 
 				if (n > 0) {
 					// limit entries
 					if (n > MAX_ENTRIES)
 						n = 1000;
 
 					Deque<Number[]> data = new ArrayDeque<Number[]>(n);
 
 					long lastTime = new Date().getTime();
 					result = historyQuery.executeQuery();
 					while (result.next() && (data.size() <= n)) {
 						long time = result.getTimestamp(1).getTime();
 
 						// limit entry separation (reverse order)
 						if (lastTime - time > MIN_INTERVAL) {
 							lastTime = time;
 
 							Number[] entry = new Number[2];
 							entry[0] = time;
 							entry[1] = Double.parseDouble(result.getString(2));
 							data.addFirst(entry);
 						}
 					}
 					result.close();
 
 					log.info("Creating history for " + ptuId + " " + sensor
 							+ " " + data.size() + " entries");
 					history = new History(
 							data.toArray(new Number[data.size()][]), unit);
 
 				}
 			} catch (SQLException ex) {
 				log.warn("Exception", ex);
 			}
 		}
 		return history;
 	}
 
 	@Override
 	public void dbConnected(Connection connection) throws SQLException {
 		super.dbConnected(connection);
 
 		connection.setAutoCommit(true);
 
 		historyQueryCount = connection
 				.prepareStatement("select count(*) from tbl_measurements "
 						+ "join tbl_devices on tbl_measurements.device_id = tbl_devices.id "
 						+ "where sensor = ? " + "and name = ? "
 						+ "and datetime > to_timestamp(?,"
 						+ PtuServerConstants.oracleFormat + ")");
 
 		historyQuery = connection
 				.prepareStatement("select DATETIME, VALUE from tbl_measurements "
 						+ "join tbl_devices on tbl_measurements.device_id = tbl_devices.id "
 						+ "where SENSOR = ? "
 						+ "and NAME = ? "
 						+ "and DATETIME > to_timestamp(?,"
 						+ PtuServerConstants.oracleFormat
 						+ ") "
 						+ "order by DATETIME desc");
 
 		deviceQuery = connection
 				.prepareStatement("select ID, NAME, IP, DSCR from tbl_devices order by NAME");
 
 		updatePtus();
 	}
 
 	@Override
 	public void dbDisconnected() throws SQLException {
 		super.dbDisconnected();
 
 		historyQueryCount = null;
 		historyQuery = null;
 		deviceQuery = null;
 	}
 
 	private void updatePtus() throws SQLException {
 		if (deviceQuery == null)
 			return;
 
 		Set<String> prune = new HashSet<String>();
 		prune.addAll(ptus.getPtuIds());
 
 		boolean ptuIdsChanged = false;
 		ResultSet result = deviceQuery.executeQuery();
 		try {
 			while (result.next()) {
 				// int id = result.getInt(1);
 				String ptuId = result.getString("NAME");
 
 				Ptu ptu = ptus.get(ptuId);
 				if (ptu == null) {
 					ptu = new Ptu(ptuId);
 					ptus.put(ptuId, ptu);
 					ptuIdsChanged = true;
 				} else {
 					prune.remove(ptuId);
 				}
 			}
 		} finally {
 			result.close();
 		}
 
 		log.info("Pruning " + prune.size() + " devices...");
 		for (Iterator<String> i = prune.iterator(); i.hasNext();) {
 			ptus.remove(i.next());
 			ptuIdsChanged = true;
 		}
 
 		if (ptuIdsChanged) {
 			eventBus.fireEvent(new PtuIdsChangedEvent(ptus.getPtuIds()));
 			ptuIdsChanged = false;
 		}
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
 		}
 
 		return list;
 	}
 
 	public int getEventCount() throws SQLException {
 		return getCount("select count(*) from tbl_events");
 	}
 
 	public List<Event> getEvents(Range range, SortOrder[] order)
 			throws SQLException {
 
 		String sql = "select tbl_devices.name, tbl_events.sensor, tbl_events.event_type, "
 				+ "tbl_events.value, tbl_events.threshold, tbl_events.datetime "
 				+ "from tbl_events "
 				+ "join tbl_devices on tbl_events.device_id = tbl_devices.id";
 
 		Statement statement = getConnection().createStatement();
 		ResultSet result = statement.executeQuery(getSql(sql, range, order));
 
 		List<Event> list = new ArrayList<Event>(range.getLength());
 		try {
 			// FIXME, #173 using some SQL this may be faster
 			// skip to start, result.absolute not implemented by Oracle
 			for (int i = 0; i < range.getStart() && result.next(); i++) {
 			}
 
 			for (int i = 0; i < range.getLength() && result.next(); i++) {
 				list.add(new Event(result.getString("name"), result
 						.getString("sensor"), result.getString("event_type"),
 						Double.parseDouble(result.getString("value")), Double
 								.parseDouble(result.getString("threshold")),
 						new Date(result.getTimestamp("datetime").getTime())));
 			}
 		} finally {
 			result.close();
 		}
 		return list;
 	}
 
 	public void addUser(User user) throws SQLException {
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
 
 	public void addDevice(Device device) throws SQLException {
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
 
 	public void addIntervention(Intervention intervention) throws SQLException {
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
 	}
 
 	public void endIntervention(int id, Date date) throws SQLException {
 		if (endIntervention == null) {
 			endIntervention = getConnection().prepareStatement(
 					"update tbl_inspections set endtime = ? where id = ?");
 		}
 
 		endIntervention.setTimestamp(1, new Timestamp(date.getTime()));
 		endIntervention.setInt(2, id);
 		endIntervention.executeUpdate();
 	}
 
 	public List<User> getUsers(boolean notBusy) throws SQLException {
 		return notBusy ? getNotBusyUsers() : getAllUsers();
 	}
 
 	private List<User> getNotBusyUsers() throws SQLException {
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
 
 	private List<User> getAllUsers() throws SQLException {
 		if (userQuery == null) {
 			userQuery = getConnection()
 					.prepareStatement(
 							"select ID, FNAME, LNAME, CERN_ID from tbl_users order by LNAME, FNAME");
 		}
 
 		return getUserList(userQuery.executeQuery());
 	}
 
 	private List<User> getUserList(ResultSet result) throws SQLException {
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
 
 	private List<Device> getNotBusyDevices() throws SQLException {
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
 
 	private List<Device> getDeviceList(ResultSet result) throws SQLException {
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
 
 	public void updateInterventionDescription(int id, String description)
 			throws SQLException {
 		if (updateInterventionDescription == null) {
 			updateInterventionDescription = getConnection().prepareStatement(
 					"update tbl_inspections set dscr = ? where id=?");
 		}
 
 		updateInterventionDescription.setString(1, description);
 		updateInterventionDescription.setInt(2, id);
 		updateInterventionDescription.executeUpdate();
 	}
 
 	public Intervention getIntervention(String ptuId) throws SQLException {
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
 
 }
