 package de.hsrm.inspector.services;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.lang.reflect.Field;
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.List;
 import java.util.concurrent.ConcurrentHashMap;
 
 import org.jdom2.Element;
 import org.jdom2.JDOMException;
 import org.jdom2.input.SAXBuilder;
 
 import android.app.IntentService;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.SharedPreferences;
 import android.net.Uri;
 import android.preference.PreferenceManager;
 import android.preference.PreferenceScreen;
 import android.util.Log;
 import de.hsrm.inspector.R;
 import de.hsrm.inspector.activities.SettingsActivity;
 import de.hsrm.inspector.broadcasts.SystemIntentReceiver;
 import de.hsrm.inspector.gadgets.intf.Gadget;
 import de.hsrm.inspector.gadgets.pool.ResponsePool;
 import de.hsrm.inspector.web.HttpServer;
 
 /**
  * {@link IntentService} to manage the {@link HttpServer} and its configuration.
  * 
  * @author dobae
  */
 public class ServerService extends IntentService {
 
 	private static HttpServer mServer;
 	private ResponsePool mResponsePool;
 
 	public static final String CMD_INIT = "init";
 	public static final String CMD_DESTROY = "destroy";
 	public static final String CMD_SETTINGS = "settings";
 	public static final String CMD_REFRESH = "refresh";
 	public static final String CMD_START_TIMEOUT = "start-timeout";
 	public static final String CMD_STOP_TIMEOUT = "stop-timeout";
 	public static final String CMD_PREFERENCE_CHANGED = "preference-changed";
 
 	public static final String DATA_CHANGED_PREFERENCE = "preference:changed";
 
 	public static final String EXTRA_PREFERENCES = "preferences";
 
 	/**
 	 * Default constructor.
 	 */
 	public ServerService() {
 		super("HttpService");
 	}
 
 	@Override
 	public void onCreate() {
 		super.onCreate();
 		mResponsePool = new ResponsePool();
 		android.os.Debug.waitForDebugger();
 		IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
 		filter.addAction(Intent.ACTION_SCREEN_OFF);
 		BroadcastReceiver mReceiver = new SystemIntentReceiver(mResponsePool);
 		registerReceiver(mReceiver, filter);
 	}
 
 	@Override
 	public int onStartCommand(Intent intent, int flags, int startId) {
 		if (intent != null) {
 			onHandleIntent(intent);
 		}
 		return START_STICKY;
 	}
 
 	@Override
 	protected void onHandleIntent(Intent intent) {
 		String command = Uri.parse(intent.toURI()).getHost();
 		if (command.equals(CMD_INIT)) {
 			Log.e("", "Init inspector server ...");
 			init();
 			start();
 		} else if (command.equals(CMD_DESTROY)) {
 			stop();
 		} else if (command.equals(CMD_SETTINGS)) {
 			init();
 			Intent i = new Intent(getApplicationContext(), SettingsActivity.class);
 			i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 
 			HashSet<String> prefs = new HashSet<String>();
 			for (Gadget g : mServer.getConfiguration().values()) {
 				if (g.getPreferences() != null) {
 					prefs.add(g.getPreferences());
 				}
 			}
 			String[] preferences = prefs.toArray(new String[0]);
 			Arrays.sort(preferences);
 			i.putExtra(EXTRA_PREFERENCES, preferences);
 
 			startActivity(i);
 		} else if (command.equals(CMD_REFRESH)) {
 			init();
 		} else if (command.equals(CMD_START_TIMEOUT)) {
 			if (mServer != null) {
 				mServer.startTimeout();
 			}
 		} else if (command.equals(CMD_STOP_TIMEOUT)) {
 			if (mServer != null) {
 				mServer.stopTimeout();
 			}
 		} else if (command.equals(CMD_PREFERENCE_CHANGED)) {
 			if (intent.hasExtra(DATA_CHANGED_PREFERENCE)) {
 				changeGadgetPreference(intent.getExtras().getString(DATA_CHANGED_PREFERENCE));
 			}
 		}
 	}
 
 	/**
 	 * Initializes {@link #mServer} if not initialized yet. Also reads
 	 * {@link SharedPreferences} of this application and parse them into a
 	 * {@link ConcurrentHashMap}.
 	 */
 	private void init() {
 		if (mServer == null) {
 			mServer = new HttpServer(getApplication(), mResponsePool);
 			mServer.setConfiguration(configure());
 		}
 	}
 
 	/**
 	 * Starts the {@link #mServer}.
 	 */
 	private void start() {
 		if (mServer != null) {
 			mServer.startThread();
 		}
 	}
 
 	/**
 	 * Stops the {@link #mServer}.
 	 */
 	private void stop() {
 		if (mServer != null) {
 			mServer.stopThread();
 		}
 	}
 
 	/**
 	 * Creates {@link Gadget} instances from inspector configuration xml and
 	 * sets default {@link SharedPreferences} values of their
 	 * {@link PreferenceScreen} attribute. After initializing all {@link Gadget}
 	 * instances, their runtime attributes will be set via
 	 * {@link SharedPreferences} and reflection api.
 	 * 
 	 * @return {@link ConcurrentHashMap} of {@link String} to {@link Gadget}
 	 */
 	private ConcurrentHashMap<String, Gadget> configure() {
 		ConcurrentHashMap<String, Gadget> config = mServer.getConfiguration();
 		if (config == null) {
 			config = new ConcurrentHashMap<String, Gadget>();
 		}
 		// If config is empty, try read inspector xml.
 		if (config.size() == 0) {
 			// Reads gadgets from inspector configuration file and sets default
 			// values of shared preferences.
 			readInspectorConfiguration(config);
 		}
 
 		// Reads all keys of shared preferences and sets instance attributes via
 		// reflection api.
 		configureGadgetInstances(config);
 		return config;
 	}
 
 	/**
 	 * Creates runtime preferences based on inspector.xml and
 	 * {@link SharedPreferences}.
 	 * 
 	 * @param config
 	 * @return
 	 */
 	private void configureGadgetInstances(ConcurrentHashMap<String, Gadget> config) {
 		Context c = getApplicationContext();
 		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
 
 		for (String key : prefs.getAll().keySet()) {
 			// <identifier>:<attribute>
 			String[] splits = key.split(":");
 
 			if (config.containsKey(splits[0].toUpperCase())) {
 				Gadget g = config.get(splits[0].toUpperCase());
 				Object value = getPreferenceValue(prefs, key);
 				if (value != null) {
 					setAttribute(g, splits[1], value);
 				}
 			}
 		}
 	}
 
 	/**
 	 * Creates a gadget object from inspector.xml gadget-element.
 	 * 
 	 * @param gadget
 	 * @param gadgets
 	 * @throws ClassNotFoundException
 	 * @throws IllegalAccessException
 	 * @throws InstantiationException
 	 */
 	@SuppressWarnings("unchecked")
 	private void createGadgetInstance(Element gadget, ConcurrentHashMap<String, Gadget> gadgets)
 			throws ClassNotFoundException, IllegalAccessException, InstantiationException {
 		try {
 			Class<Gadget> gadgetClass = (Class<Gadget>) Class.forName(gadget.getChildText(getApplicationContext()
 					.getString(R.string.configuration_class)));
 
 			if (gadget.getChild(getApplicationContext().getString(R.string.configuration_identifiers)) != null) {
 				Element identifiers = gadget.getChild(getApplicationContext().getString(
 						R.string.configuration_identifiers));
 				for (Element identifier : identifiers.getChildren(getApplicationContext().getString(
 						R.string.configuration_identifier))) {
 					Gadget g = (Gadget) gadgetClass.newInstance();
 					g.setIdentifier(identifier.getText().toUpperCase());
 					if (gadget.getChild(getString(R.string.configuration_preference_screen)) != null) {
 						g.setPreferences(gadget.getChildText(getString(R.string.configuration_preference_screen)));
 						PreferenceManager.setDefaultValues(getApplicationContext(),
 								getResources().getIdentifier(g.getPreferences(), "xml", getPackageName()), true);
 					}
 					if (gadgets.contains(g.getIdentifier())) {
 						gadgets.replace(g.getIdentifier(), g);
 					} else {
 						gadgets.put(g.getIdentifier(), g);
 					}
 				}
 			} else {
 				Gadget g = (Gadget) gadgetClass.newInstance();
 				g.setIdentifier(gadget.getChildText(
 						getApplicationContext().getString(R.string.configuration_identifier)).toUpperCase());
 				if (gadget.getChild(getString(R.string.configuration_preference_screen)) != null) {
 					g.setPreferences(gadget.getChildText(getString(R.string.configuration_preference_screen)));
 					PreferenceManager.setDefaultValues(getApplicationContext(),
 							getResources().getIdentifier(g.getPreferences(), "xml", getPackageName()), true);
 				}
 				if (gadgets.contains(g.getIdentifier())) {
 					gadgets.replace(g.getIdentifier(), g);
 				} else {
 					gadgets.put(g.getIdentifier(), g);
 				}
 			}
 		} catch (ClassCastException e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Reads inspector.xml configuration for all supported gadget identifiers
 	 * and their linked classes.
 	 * 
 	 * @param config
 	 */
 	private void readInspectorConfiguration(ConcurrentHashMap<String, Gadget> config) {
 		try {
 			SAXBuilder builder = new SAXBuilder();
 			InputStream file = getApplicationContext().getResources().openRawResource(R.raw.inspector_default);
 			Element root = builder.build(file).getRootElement();
 			List<Element> gadgets = root.getChildren(getApplicationContext().getString(R.string.configuration_gadgets));
 
 			for (Element gadget : gadgets) {
 				createGadgetInstance(gadget, config);
 			}
 			String serverTimeout = root.getAttributeValue(getString(R.string.configuration_timeout));
 			if (serverTimeout != null) {
 				try {
 					mServer.setTimeoutTime(Long.parseLong(serverTimeout) * 1000);
 				} catch (ClassCastException e) {
 					e.printStackTrace();
 				}
 			}
 		} catch (IOException e) {
 			e.printStackTrace();
 		} catch (JDOMException e) {
 			e.printStackTrace();
 		} catch (ClassNotFoundException e) {
 			e.printStackTrace();
 		} catch (IllegalAccessException e) {
 			e.printStackTrace();
 		} catch (InstantiationException e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Update a {@link Gadget} configuration at runtime.
 	 * 
 	 * @param key
 	 *            {@link String} key of {@link SharedPreferences} value to
 	 *            update.
 	 */
 	private void changeGadgetPreference(String key) {
 		Context c = getApplicationContext();
 		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
 		String[] splits = key.split(":");
 		if (splits.length == 2 && mServer != null && mServer.getConfiguration() != null) {
 			ConcurrentHashMap<String, Gadget> settings = mServer.getConfiguration();
 			Gadget gadget = settings.get(splits[0].toUpperCase());
 
 			setAttribute(gadget, splits[1], getPreferenceValue(prefs, key));
 		}
 	}
 
 	/**
 	 * Returns value of {@link SharedPreferences} key.
 	 * 
 	 * @param prefs
 	 *            {@link SharedPreferences}
 	 * @param key
 	 *            {@link String}
 	 * @return {@link Object}
 	 */
 	private Object getPreferenceValue(SharedPreferences prefs, String key) {
 		Object value = null;
 		try {
 			value = prefs.getBoolean(key, false);
 		} catch (Exception e1) {
 			try {
 				value = prefs.getFloat(key, 0f);
 			} catch (Exception e2) {
 				try {
 					value = prefs.getInt(key, 0);
 				} catch (Exception e3) {
 					try {
 						value = prefs.getLong(key, 0);
 					} catch (Exception e4) {
 						try {
 							value = prefs.getString(key, "");
 						} catch (Exception e5) {
 						}
 					}
 				}
 			}
 		}
 		return value;
 	}
 
 	/**
 	 * Returns {@link Field} of given {@link Class} and name recursively.
 	 * 
 	 * @param clazz
 	 *            {@link Class}
 	 * @param name
 	 *            {@link String}
 	 * @return {@link Field}
 	 * @throws NoSuchFieldException
 	 */
 	private Field getField(Class<?> clazz, String name) throws NoSuchFieldException {
 		try {
 			return clazz.getDeclaredField(name);
 		} catch (SecurityException e) {
 			e.printStackTrace();
 		} catch (NoSuchFieldException e) {
 			Class<?> superClazz = clazz.getSuperclass();
 			if (superClazz == null) {
 				throw e;
 			}
 			return getField(superClazz, name);
 		}
 		return null;
 	}
 
 	/**
 	 * Sets attribute of {@link Gadget} by field name.
 	 * 
 	 * @param gadget
 	 *            {@link Gadget}
 	 * @param name
 	 *            {@link String}
 	 * @param value
 	 *            {@link Object}
 	 */
 	private void setAttribute(Gadget gadget, String name, Object value) {
 		Class<?> clazz = gadget.getClass();
 		try {
 			Field field = getField(clazz, name);
 			if (field != null) {
 				if (!field.isAccessible()) {
 					field.setAccessible(true);
 				}
 				if (field.getType().equals(Integer.TYPE)) {
 					field.setInt(gadget, Integer.parseInt(value.toString()));
 				} else if (field.getType().equals(Long.TYPE)) {
 					field.setLong(gadget, Long.parseLong(value.toString()));
 				} else if (field.getType().equals(Float.TYPE)) {
 					field.setFloat(gadget, Float.parseFloat(value.toString()));
 				} else if (field.getType().equals(Double.TYPE)) {
 					field.setDouble(gadget, Double.parseDouble(value.toString()));
 				} else if (field.getType().equals(Boolean.TYPE)) {
 					field.setBoolean(gadget, Boolean.parseBoolean(value.toString()));
 				} else {
 					field.set(gadget, value);
 				}
 			}
 		} catch (NoSuchFieldException e) {
 		} catch (NumberFormatException e) {
 		} catch (IllegalArgumentException e) {
 		} catch (IllegalAccessException e) {
 		}
 	}
 }
