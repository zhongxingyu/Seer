 package ch.cern.atlas.apvs.db;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.Executors;
 import java.util.concurrent.ScheduledExecutorService;
 import java.util.concurrent.TimeUnit;
 
 import org.hibernate.HibernateException;
 import org.hibernate.Query;
 import org.hibernate.Session;
 import org.hibernate.SessionFactory;
 import org.hibernate.Transaction;
 import org.hibernate.cfg.Configuration;
 import org.hibernate.service.ServiceRegistry;
 import org.hibernate.service.ServiceRegistryBuilder;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import ch.cern.atlas.apvs.domain.Data;
 import ch.cern.atlas.apvs.domain.Device;
 import ch.cern.atlas.apvs.domain.DeviceData;
 import ch.cern.atlas.apvs.domain.Event;
 import ch.cern.atlas.apvs.domain.History;
 import ch.cern.atlas.apvs.domain.Intervention;
 import ch.cern.atlas.apvs.domain.InterventionMap;
 import ch.cern.atlas.apvs.domain.Measurement;
 import ch.cern.atlas.apvs.domain.Sensor;
 import ch.cern.atlas.apvs.domain.SortOrder;
 import ch.cern.atlas.apvs.domain.Ternary;
 import ch.cern.atlas.apvs.domain.User;
 import ch.cern.atlas.apvs.event.ConnectionStatusChangedRemoteEvent;
 import ch.cern.atlas.apvs.event.ConnectionStatusChangedRemoteEvent.ConnectionType;
 import ch.cern.atlas.apvs.event.InterventionMapChangedRemoteEvent;
 import ch.cern.atlas.apvs.eventbus.shared.RemoteEventBus;
 import ch.cern.atlas.apvs.eventbus.shared.RequestRemoteEvent;
 import ch.cern.atlas.apvs.hibernate.types.DoubleStringType;
 import ch.cern.atlas.apvs.hibernate.types.InetAddressType;
 import ch.cern.atlas.apvs.hibernate.types.IntegerStringType;
 import ch.cern.atlas.apvs.hibernate.types.MacAddressType;
 import ch.cern.atlas.apvs.util.StringUtils;
 
 public class Database {
 	private Logger log = LoggerFactory.getLogger(getClass().getName());
 
 	private static Database instance;
 
 	private Configuration configuration;
 	private ServiceRegistry serviceRegistry;
 	private SessionFactory sessionFactory;
 
 	private RemoteEventBus eventBus;
 	private Ternary connected = Ternary.Unknown;
 	private Ternary updated = Ternary.Unknown;
 
 	private InterventionMap interventions = new InterventionMap();
 
 	private Database(final RemoteEventBus eventBus) {
 		this.eventBus = eventBus;
 
 		configuration = new Configuration();
		configuration.configure("hibernate.cfg.xml");
 		configuration.registerTypeOverride(new DoubleStringType());
 		configuration.registerTypeOverride(new IntegerStringType());
 		configuration.registerTypeOverride(new MacAddressType());
 		configuration.registerTypeOverride(new InetAddressType());
 		
 		serviceRegistry = new ServiceRegistryBuilder().applySettings(
 				configuration.getProperties()).buildServiceRegistry();
 		sessionFactory = configuration.buildSessionFactory(serviceRegistry);
 
 		// new SchemaExport(configuration).create(true, false);
 
 		checkUpdate();
 
 		readInterventions();
 
 		if (eventBus != null) {
 
 			RequestRemoteEvent.register(eventBus,
 					new RequestRemoteEvent.Handler() {
 
 						@Override
 						public void onRequestEvent(RequestRemoteEvent event) {
 							String type = event.getRequestedClassName();
 
 							if (type.equals(InterventionMapChangedRemoteEvent.class
 									.getName())) {
 								InterventionMapChangedRemoteEvent.fire(
 										eventBus, interventions);
 							} else if (type
 									.equals(ConnectionStatusChangedRemoteEvent.class
 											.getName())) {
 								ConnectionStatusChangedRemoteEvent.fire(
 										eventBus,
 										ConnectionType.databaseConnect,
 										connected);
 								ConnectionStatusChangedRemoteEvent.fire(
 										eventBus,
 										ConnectionType.databaseUpdate, updated);
 							}
 						}
 					});
 		}
 
 		ScheduledExecutorService executor = Executors
 				.newSingleThreadScheduledExecutor();
 		executor.scheduleWithFixedDelay(new Runnable() {
 
 			// ScheduledFuture<?> watchdog;
 
 			@Override
 			public void run() {
 				try {
 					// rereadInterventions();
 					if (!checkUpdate()) {
 						log.warn("DB no longer updated");
 					} else if (!isConnected()) {
 						log.warn("DB no longer reachable");
 					}
 				} catch (HibernateException e) {
 					log.warn("Could not regularly-update intervention list: ",
 							e);
 				}
 
 			}
 		}, 0, 30, TimeUnit.SECONDS);
 	}
 
 	private boolean checkUpdate() throws HibernateException {
 
 		Ternary wasConnected = connected;
 		Ternary wasUpdated = updated;
 
 		Session session = null;
 		Transaction tx = null;
 		try {
 			session = sessionFactory.openSession();
 			tx = session.beginTransaction();
 
 			String sql = "select date from Measurement order by date desc";
 
 			long now = new Date().getTime();
 			@SuppressWarnings("unchecked")
 			Iterator<Date> i = session.createQuery(sql).iterate();
 			if (i.hasNext()) {
 				Date lastUpdate = i.next();
 				long time = lastUpdate.getTime();
 				updated = (time > now - (3 * 60000)) ? Ternary.True
 						: Ternary.False;
 			} else {
 				updated = Ternary.False;
 			}
 			tx.commit();
 			connected = Ternary.True;
 		} catch (HibernateException e) {
 			if (tx != null) {
 				tx.rollback();
 			}
 			connected = Ternary.False;
 			throw e;
 		} finally {
 			if (session != null) {
 				session.close();
 			}
 
 			if (eventBus != null) {
 				if (!updated.equals(wasUpdated)) {
 					ConnectionStatusChangedRemoteEvent.fire(eventBus,
 							ConnectionType.databaseUpdate, updated);
 				}
 
 				if (!connected.equals(wasConnected)) {
 					ConnectionStatusChangedRemoteEvent.fire(eventBus,
 							ConnectionType.databaseConnect, connected);
 				}
 			}
 		}
 
 		return !updated.isFalse();
 	}
 
 	@SuppressWarnings("unchecked")
 	private void readInterventions() {
 		InterventionMap newMap = new InterventionMap();
 		Session session = null;
 		Transaction tx = null;
 		try {
 			session = sessionFactory.openSession();
 			tx = session.beginTransaction();
 			for (Intervention intervention : (List<Intervention>) session
 					.createQuery("from Intervention i where i.endTime is null")
 					.list()) {
 				newMap.put(intervention.getDevice(), intervention);
 			}
 			tx.commit();
 		} catch (HibernateException e) {
 			if (tx != null) {
 				tx.rollback();
 			}
 			throw e;
 		} finally {
 			if (session != null) {
 				session.close();
 			}
 		}
 
 		if (!interventions.equals(newMap)) {
 			interventions = newMap;
 			if (eventBus != null) {
 				// InterventionMapChangedRemoteEvent.fire(eventBus,
 				// interventions);
 			}
 		}
 	}
 
 	public static Database getInstance(RemoteEventBus eventBus) {
 		if (instance == null) {
 			instance = new Database(eventBus);
 		}
 		return instance;
 	}
 
 	public void close() {
 		sessionFactory.close();
 	}
 	
 	public Configuration getConfiguration() {
 		return configuration;
 	}
 
 	public SessionFactory getSessionFactory() {
 		return sessionFactory;
 	}
 
 	public boolean isConnected() {
 		return connected.isTrue();
 	}
 
 	public List<Device> getDevices(boolean available) {
 		Session session = null;
 		Transaction tx = null;
 		try {
 			session = sessionFactory.openSession();
 			tx = session.beginTransaction();
 
 			String sql = "from Device d";
 			if (available) {
 				sql += " where d.id not in (select device.id from Intervention i where i.endTime is null)";
 			}
 			sql += " order by d.name";
 
 			@SuppressWarnings("unchecked")
 			List<Device> devices = session.createQuery(sql).list();
 
 			tx.commit();
 			return devices;
 		} catch (HibernateException e) {
 			if (tx != null) {
 				tx.rollback();
 			}
 			throw e;
 		} finally {
 			if (session != null) {
 				session.close();
 			}
 		}
 	}
 
 	public Map<String, Device> getDeviceMap() {
 		Map<String, Device> devices = new HashMap<String, Device>();
 		for (Device device : getDevices(false)) {
 			devices.put(device.getName(), device);
 		}
 		return devices;
 	}
 
 	public List<String> getSensorNames(Device device) {
 		Session session = null;
 		Transaction tx = null;
 		try {
 			session = sessionFactory.openSession();
 			tx = session.beginTransaction();
 
 			Query query = session
 					.createQuery("select distinct name from Measurement m where m.device = :device");
 			query.setEntity("device", device);
 			@SuppressWarnings("unchecked")
 			List<String> sensorNames = query.list();
 			log.info("Found " + sensorNames.size() + " sensor names for "
 					+ device.getName());
 
 			tx.commit();
 
 			return sensorNames;
 		} catch (HibernateException e) {
 			if (tx != null) {
 				tx.rollback();
 			}
 			throw e;
 		} finally {
 			if (session != null) {
 				session.close();
 			}
 		}
 
 	}
 
 	public void saveOrUpdate(Object object) {
 		if (object == null) {
 			return;
 		}
 
 		Session session = null;
 		Transaction tx = null;
 		try {
 			session = sessionFactory.openSession();
 			tx = session.beginTransaction();
 			session.saveOrUpdate(object);
 			tx.commit();
 		} catch (HibernateException e) {
 			if (tx != null) {
 				tx.rollback();
 			}
 			throw e;
 		} finally {
 			if (session != null) {
 				session.close();
 			}
 		}
 	}
 
 	@SuppressWarnings("unchecked")
 	public List<User> getUsers(boolean available) {
 		Session session = null;
 		Transaction tx = null;
 		try {
 			session = sessionFactory.openSession();
 			tx = session.beginTransaction();
 
 			String sql = "from User u";
 			if (available) {
 				sql += " where u.id not in (select user.id from Intervention i where i.endTime is null)";
 			}
 			sql += " order by u.lastName, u.firstName";
 			List<User> users = session.createQuery(sql).list();
 
 			tx.commit();
 
 			return users;
 		} catch (HibernateException e) {
 			if (tx != null) {
 				tx.rollback();
 			}
 			throw e;
 		} finally {
 			if (session != null) {
 				session.close();
 			}
 		}
 	}
 
 	public <T> List<T> getList(Class<T> clazz, Integer start, Integer length,
 			SortOrder[] order) {
 		Session session = null;
 		Transaction tx = null;
 		try {
 			session = sessionFactory.openSession();
 			tx = session.beginTransaction();
 			@SuppressWarnings("unchecked")
 			List<T> list = getQuery(session, clazz, start, length, order)
 					.list();
 			tx.commit();
 			return list;
 		} catch (HibernateException e) {
 			if (tx != null) {
 				tx.rollback();
 			}
 			throw e;
 		} finally {
 			if (session != null) {
 				session.close();
 			}
 		}
 	}
 
 	public Query getQuery(Session session, Class<?> clazz, Integer start,
 			Integer length, SortOrder[] order) {
 		Query query = session.createQuery(getSql("from " + clazz.getName()
 				+ " t", order));
 		if (start != null) {
 			query.setFirstResult(start);
 		}
 		if (length != null) {
 			query.setMaxResults(length);
 		}
 		return query;
 	}
 
 	public long getCount(Class<?> clazz) {
 		Session session = null;
 		Transaction tx = null;
 		try {
 			session = sessionFactory.openSession();
 			tx = session.beginTransaction();
 			Long count = (Long) session.createQuery(
 					"select count(*) from " + clazz.getName()).uniqueResult();
 			tx.commit();
 			// System.err.println("Getting count for "+clazz+" "+count);
 			return count;
 		} catch (HibernateException e) {
 			if (tx != null) {
 				tx.rollback();
 			}
 			throw e;
 		} finally {
 			if (session != null) {
 				session.close();
 			}
 		}
 	}
 
 	public long getEventCount(Device device, String sensor) {
 		Session session = null;
 		Transaction tx = null;
 		try {
 			session = sessionFactory.openSession();
 			tx = session.beginTransaction();
 			Query query = session.createQuery("select count(*) from "
 					+ Event.class.getName() + " t"
 					+ getEventClause(device, sensor));
 			addEventParams(query, device, sensor);
 			Long count = (Long) query.uniqueResult();
 			tx.commit();
 			return count;
 		} catch (HibernateException e) {
 			if (tx != null) {
 				tx.rollback();
 			}
 			throw e;
 		} finally {
 			if (session != null) {
 				session.close();
 			}
 		}
 	}
 
 	public List<Event> getEvents(int start, int length, SortOrder[] order,
 			Device device, String sensor) {
 		Session session = null;
 		Transaction tx = null;
 		try {
 			session = sessionFactory.openSession();
 			tx = session.beginTransaction();
 			Query query = session.createQuery(getSql(
 					"from " + Event.class.getName() + " t"
 							+ getEventClause(device, sensor), order));
 			addEventParams(query, device, sensor);
 			query.setFirstResult(start).setMaxResults(length);
 			@SuppressWarnings("unchecked")
 			List<Event> events = query.list();
 			tx.commit();
 			return events;
 		} catch (HibernateException e) {
 			if (tx != null) {
 				tx.rollback();
 			}
 			throw e;
 		} finally {
 			if (session != null) {
 				session.close();
 			}
 		}
 	}
 
 	private String getEventClause(Device device, String sensor) {
 		String sql = "";
 		if ((device != null) || (sensor != null)) {
 			sql += " where";
 			if (device != null) {
 				sql += " t.device = :device";
 			}
 			if (sensor != null) {
 				if (sensor != null) {
 					sql += " and";
 				}
 				sql += " t.name = :sensor";
 			}
 		}
 		return sql;
 	}
 
 	private void addEventParams(Query query, Device device, String sensor) {
 		if (device != null) {
 			query.setEntity("device", device);
 		}
 		if (sensor != null) {
 			query.setString("sensor", sensor);
 		}
 	}
 
 	private String getSql(String sql, SortOrder[] order) {
 		StringBuffer s = new StringBuffer(sql);
 		if (order != null) {
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
 		}
 		return s.toString();
 	}
 
 	public SensorMap getSensorMap() throws HibernateException {
 		Session session = null;
 		Transaction tx = null;
 		try {
 			SensorMap sensorMap = new SensorMap();
 
 			session = sessionFactory.openSession();
 			tx = session.beginTransaction();
 			@SuppressWarnings("unchecked")
 			List<Sensor> list = session.createQuery("from Sensor").list();
 			for (Iterator<Sensor> i = list.iterator(); i.hasNext();) {
 				Sensor sensor = i.next();
 				Boolean enabled = sensor.isEnabled() == null
 						|| sensor.isEnabled();
 				sensorMap.setEnabled(sensor.getDevice(), sensor.getName(),
 						enabled);
 			}
 			tx.commit();
 
 			return sensorMap;
 		} catch (HibernateException e) {
 			if (tx != null) {
 				tx.rollback();
 			}
 			throw e;
 		} finally {
 			if (session != null) {
 				session.close();
 			}
 		}
 	}
 
 	public Intervention getIntervention(Device device)
 			throws HibernateException {
 		Session session = null;
 		Transaction tx = null;
 		try {
 			session = sessionFactory.openSession();
 			tx = session.beginTransaction();
 			Intervention intervention = (Intervention) session
 					.createQuery(
 							"from Intervention i where i.endTime is null and i.device = :device order by i.startTime desc")
 					.setEntity("device", device).uniqueResult();
 			tx.commit();
 			return intervention;
 		} catch (HibernateException e) {
 			if (tx != null) {
 				tx.rollback();
 			}
 			throw e;
 		} finally {
 			if (session != null) {
 				session.close();
 			}
 		}
 	}
 
 	public List<Measurement> getMeasurements(Device ptu, String name)
 			throws HibernateException {
 		List<Device> ptuList = null;
 		if (ptu != null) {
 			ptuList = new ArrayList<Device>();
 			ptuList.add(ptu);
 		}
 		return getMeasurements(ptuList, name);
 	}
 
 	public List<Measurement> getMeasurements(List<Device> ptuList, String name)
 			throws HibernateException {
 
 		// FIXME could be part of the SQL
 		SensorMap sensorMap = getSensorMap();
 
 		String sql = "from Measurement m, view_last_measurements_date d "
 				+ "where d.datetime = m.date " + "and d.sensor = m.sensor "
 				+ "and d.device_id = m.device.id";
 
 		if (ptuList != null) {
 			sql += " and m.name in ("
 					+ StringUtils.join(ptuList.toArray(), ',', '\'') + ")";
 		}
 		if (name != null) {
 			sql += " and d.sensor = :sensor";
 		}
 
 		Session session = null;
 		Transaction tx = null;
 		try {
 			session = sessionFactory.openSession();
 			tx = session.beginTransaction();
 			Query query = session.createQuery(sql);
 			if (name != null) {
 				query.setString("sensor", name);
 			}
 
 			List<Measurement> list = new ArrayList<Measurement>();
 
 			for (@SuppressWarnings("unchecked")
 			Iterator<Measurement> i = query.iterate(); i.hasNext();) {
 				Measurement m = i.next();
 
 				if (!sensorMap.isEnabled(m.getDevice(), m.getSensor())) {
 					continue;
 				}
 
 				String unit = m.getUnit();
 				Double value = m.getValue();
 				Double low = m.getLowLimit();
 				Double high = m.getHighLimit();
 
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
 
 				list.add(new Measurement(m.getDevice(), m.getSensor(), value,
 						low, high, unit, m.getSamplingRate(), m.getDate()));
 			}
 			tx.commit();
 			return list;
 		} catch (HibernateException e) {
 			if (tx != null) {
 				tx.rollback();
 			}
 			throw e;
 		} finally {
 			if (session != null) {
 				session.close();
 			}
 		}
 	}
 
 	public List<Measurement> getLastMeasurements(Device device, String sensor,
 			int maxEntries) {
 		Session session = null;
 		Transaction tx = null;
 		try {
 			session = sessionFactory.openSession();
 			tx = session.beginTransaction();
 
 			Query query = session
 					.createQuery("from Measurement m where m.device = :device and m.name = :sensor order by m.date desc");
 			query.setEntity("device", device);
 			query.setString("sensor", sensor);
 
 			List<Measurement> lastMeasurements = new ArrayList<Measurement>();
 			int n = 0;
 			for (@SuppressWarnings("unchecked")
 			Iterator<Measurement> i = query.iterate(); i.hasNext()
 					&& (n < maxEntries); n++) {
 				Measurement m = i.next();
 				lastMeasurements.add(m);
 			}
 			log.info("Found " + lastMeasurements.size()
 					+ " last measurements for device " + device.getName()
 					+ " and sensor " + sensor);
 
 			tx.commit();
 
 			return lastMeasurements;
 		} catch (HibernateException e) {
 			if (tx != null) {
 				tx.rollback();
 			}
 			throw e;
 		} finally {
 			if (session != null) {
 				session.close();
 			}
 		}
 	}
 
 	public Map<Device, Map<String, List<Measurement>>> getLastMeasurements(
 			int maxEntries) {
 		Map<Device, Map<String, List<Measurement>>> result = new HashMap<Device, Map<String, List<Measurement>>>();
 		for (Device device : getDevices(false)) {
 			Map<String, List<Measurement>> sensors = new HashMap<String, List<Measurement>>();
 			result.put(device, sensors);
 
 			for (String sensor : getSensorNames(device)) {
 				sensors.put(sensor,
 						getLastMeasurements(device, sensor, maxEntries));
 			}
 		}
 		return result;
 	}
 
 	public History getHistory(List<Device> devices, Date from,
 			Integer maxEntries) {
 		History history = new History();
 
 		for (Device device : devices) {
 			history.put(getDeviceData(device, from, maxEntries));
 		}
 
 		return history;
 	}
 
 	public DeviceData getDeviceData(Device device, Date from, Integer maxEntries) {
 		DeviceData deviceData = new DeviceData(device);
 
 		String sql = "from Measurement m" + " where m.device = :device";
 		if (from != null) {
 			sql += " and m.date > :date";
 		}
 		sql += " order by m.date asc";
 
 		Date now = new Date();
 		
 		from = getAdjustedDate(sql, device, from, now, maxEntries);
 
 		Session session = null;
 		Transaction tx = null;
 		try {		
 			session = sessionFactory.openSession();
 			tx = session.beginTransaction();
 
 			Query query = session.createQuery(sql);
 
 			query.setEntity("device", device);
 
 			if (from != null) {
 				query.setTimestamp("date", from);
 			}
 
 			for (@SuppressWarnings("unchecked")
 			Iterator<Measurement> i = query.list().iterator(); i.hasNext();) {
 				Measurement measurement = i.next();
 				Date date = measurement.getDate();
 
 				long time = date.getTime();
 				if (time > now.getTime() + 60000) {
 					break;
 				}
 
 				Long id = measurement.getId();
 				String sensor = measurement.getSensor();
 				Double value = measurement.getValue();
 				String unit = measurement.getUnit();
 
 				// Fix for #488, invalid db entry
 				if ((sensor == null) || (value == null) || (unit == null)) {
 					log.warn("MeasurementTable ID " + id
 							+ " contains <null> sensor, value or unit ("
 							+ sensor + ", " + value + ", " + unit
 							+ ") for ptu: " + device.getName());
 					continue;
 				}
 
 				Double low = measurement.getLowLimit();
 				Double high = measurement.getHighLimit();
 
 				Integer samplingRate = measurement.getSamplingRate();
 
 				// Scale down to microSievert
 				value = Scale.getValue(value, unit);
 				low = Scale.getLowLimit(low, unit);
 				high = Scale.getHighLimit(high, unit);
 				unit = Scale.getUnit(unit);
 
 				// if (!sensorMap.isEnabled(ptu, sensor)) {
 				// continue;
 				// }
 
 				Data data = deviceData.get(sensor);
 				if (data == null) {
 
 					if ((sensor.equals("Temperature") || sensor
 							.equals("BodyTemperature")) && unit.equals("C")) {
 						unit = "&deg;C";
 					}
 
 					data = new Data(device, sensor, unit, maxEntries);
 					deviceData.put(data);
 				}
 
 				if (data.addEntry(time, value, low, high, samplingRate)) {
 					// total++;
 				}
 			}
 
 			tx.commit();
 
 			return deviceData;
 		} catch (HibernateException e) {
 			if (tx != null) {
 				tx.rollback();
 			}
 			throw e;
 		} finally {
 			if (session != null) {
 				session.close();
 			}
 		}
 	}
 
 	// NOTE #705: we could improve by binary search... rather then just half and see if we get fewer entries.
 	private Date getAdjustedDate(String sql, Device device, Date from, Date until, int maxEntries) {
 		if (from == null) {
 			return null;
 		}
 		
 		Session session = null;
 		Transaction tx = null;
 		
 		try {
 			session = sessionFactory.openSession();
 			tx = session.beginTransaction();
 			Query count = session.createQuery("select count(*) " + sql);
 
 			count.setEntity("device", device);
 
 			long entries;
 			do {
 				count.setTimestamp("date", from);
 
 				entries = (Long) count.uniqueResult();
 //				System.err.println("Entries " + entries+" "+from);
 
 				if (entries > maxEntries) {
 					from = new Date((from.getTime() + until.getTime()) / 2);
 				}
 
 			} while (entries > maxEntries);
 			tx.commit();
 
 			return from;
 		} catch (HibernateException e) {
 			if (tx != null) {
 				tx.rollback();
 			}
 			throw e;
 		} finally {
 			if (session != null) {
 				session.close();
 			}
 		}
 	}
 
 }
