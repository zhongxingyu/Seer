 package net.mms_projects.copyit.app;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.lang.reflect.Field;
 import java.net.URL;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.Map;
 import java.util.UUID;
 
 import net.mms_projects.copyit.ClipboardManager;
 import net.mms_projects.copyit.FileStreamBuilder;
 import net.mms_projects.copyit.PathBuilder;
 import net.mms_projects.copyit.Settings;
 import net.mms_projects.copyit.SettingsListener;
 import net.mms_projects.copyit.SyncListener;
 import net.mms_projects.copyit.SyncManager;
 import net.mms_projects.copyit.SyncingThread;
 import net.mms_projects.copyit.api.ServerApi;
 import net.mms_projects.copyit.api.endpoints.ClipboardContentEndpoint;
 import net.mms_projects.copyit.sync_services.ApiService;
 import net.mms_projects.copyit.sync_services.TestService;
 import net.mms_projects.copyit.ui.AbstractUi;
 import net.mms_projects.copyit.ui.SwtGui;
 import net.mms_projects.utils.OSValidator;
 
 import org.apache.commons.io.FileUtils;
 import org.freedesktop.Notifications;
 import org.freedesktop.dbus.DBusConnection;
 import org.freedesktop.dbus.UInt32;
 import org.freedesktop.dbus.Variant;
 import org.freedesktop.dbus.exceptions.DBusException;
 
 public class CopyItDesktop extends CopyIt {
 
 	protected Settings settings;
 	protected File lockFile;
 
 	static public DBusConnection dbusConnection;
 
 	static private boolean nativeLoadingInitialized;
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		CopyItDesktop app = new CopyItDesktop();
 		app.run();
 	}
 
 	public static String getVersion() {
 		String version = "";
 		if (CopyItDesktop.class.getPackage().getSpecificationVersion() != null) {
 			version += CopyItDesktop.class.getPackage()
 					.getSpecificationVersion();
 		} else {
 			version += "0.0.1";
 		}
 
 		if (CopyItDesktop.getBuildNumber() != 0) {
 			version += "-" + CopyItDesktop.getBuildNumber();
 		}
 		return version;
 	}
 
 	public static int getBuildNumber() {
 		try {
 			return Integer.parseInt(CopyItDesktop.class.getPackage()
 					.getImplementationVersion());
 		} catch (NumberFormatException e) {
 			return 0;
 		}
 	}
 
 	public void run() {
 		this.settings = new Settings();
 		try {
 			this.settings.setFileStreamBuilder(new StreamBuilder());
 		} catch (IOException e) {
 			e.printStackTrace();
 
 			System.exit(1);
 		}
 		this.settings.loadProperties();
 
 		final ClipboardManager clipboardManager = new ClipboardManager();
 
 		final SyncManager syncManager = new SyncManager(clipboardManager);
 
 		ServerApi api = new ServerApi();
 		api.deviceId = UUID.fromString(settings.get("device.id"));
 		api.devicePassword = settings.get("device.password");
 
 		TestService testService = new TestService(syncManager);
 		final ApiService apiService = new ApiService(syncManager,
 				new ClipboardContentEndpoint(api));
 		final SyncingThread syncThread = new SyncingThread(syncManager,
 				new ClipboardContentEndpoint(api));
 		SettingsListener apiServiceListener = new SettingsListener() {
 
 			@Override
 			public void onChange(String key, String value) {
 				ServerApi api = new ServerApi();
 				api.deviceId = UUID.fromString(settings.get("device.id"));
 				api.devicePassword = settings.get("device.password");
 				ClipboardContentEndpoint endpoint = new ClipboardContentEndpoint(
 						api);
 				apiService.setEndpoint(endpoint);
 				syncThread.setEndpoint(endpoint);
 			}
 		};
 		this.settings.addListener("device.id", apiServiceListener);
 		this.settings.addListener("device.password", apiServiceListener);
 
 		syncManager.addPushService(apiService);
 		syncManager.addPullService(apiService);
 		syncManager.addPushService(testService);
 		syncManager.addPullingService(syncThread);
 		syncManager.addPullingService(testService);
 		syncManager.addListener(new SyncListener() {
 
 			@Override
 			public void onPushed(String content, Date date) {
 				System.out.println("Content pushed");
 			}
 
 			@Override
 			public void onPulled(String content, Date date) {
 				System.out.println("The following content was pulled: "
 						+ content);
 			}
 		});
 
 		this.settings.addListener("sync.polling.enabled",
 				new SettingsListener() {
 					@Override
 					public void onChange(String key, String value) {
 						if (Boolean.parseBoolean(value)) {
 							syncManager.activatePolling();
							clipboardManager.activatePolling();
 						} else {
 							syncManager.deactivatePolling();
							clipboardManager.deactivatePolling();
 						}
 					}
 				});
 		if (this.settings.getBoolean("sync.polling.enabled")) {
 			syncManager.activatePolling();
			clipboardManager.activatePolling();
 		} else {
 			syncManager.deactivatePolling();
			clipboardManager.deactivatePolling();
 		}
 
 		if (OSValidator.isUnix()) {
 			this.exportResource("unix-java.so");
 			try {
 				CopyItDesktop.dbusConnection = DBusConnection
 						.getConnection(DBusConnection.SESSION);
 			} catch (DBusException e1) {
 				// TODO Auto-generated catch block
 				System.out
 						.println("Ahh could not connect to D-Bus. All kinds of explosions n'stuff. Fix it!");
 				e1.printStackTrace();
 				System.exit(1);
 			}
 		}
 
 		this.lockFile = new File(PathBuilder.getConfigDirectory(), ".lock");
 		if (this.lockFile.exists()) {
 			String message = "An instance is already running. "
 					+ "If not please remove the following lock file: "
 					+ this.lockFile.getAbsolutePath();
 
 			if (OSValidator.isUnix()) {
 				try {
 					Notifications notify = CopyItDesktop.dbusConnection
 							.getRemoteObject("org.freedesktop.Notifications",
 									"/org/freedesktop/Notifications",
 									Notifications.class);
 					Map<String, Variant<Byte>> hints = new HashMap<String, Variant<Byte>>();
 					hints.put("urgency", new Variant<Byte>((byte) 2));
 					notify.Notify("CopyIt", new UInt32(0), "", "CopyIt",
 							message, new LinkedList<String>(), hints, -1);
 				} catch (DBusException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 
 			System.out.println(message);
 			System.exit(0);
 		} else {
 			try {
 				this.lockFile.createNewFile();
 
 				this.lockFile.deleteOnExit();
 			} catch (IOException e) {
 				e.printStackTrace();
 
 				System.exit(1);
 			}
 		}
 
 		AbstractUi ui = new SwtGui(this.settings, syncManager, clipboardManager);
 		ui.open();
 
 		this.settings.saveProperties();
 	}
 
 	class StreamBuilder extends FileStreamBuilder {
 
 		private File settingsFile;
 
 		public StreamBuilder() throws IOException {
 			this.settingsFile = new File(PathBuilder.getConfigDirectory(),
 					"options.properties");
 			if (!this.settingsFile.exists()) {
 				System.out.println("No settings file. Creating it.");
 				this.settingsFile.createNewFile();
 			}
 		}
 
 		@Override
 		public FileInputStream getInputStream() throws IOException {
 			return new FileInputStream(this.settingsFile);
 		}
 
 		@Override
 		public FileOutputStream getOutputStream() throws IOException {
 			return new FileOutputStream(this.settingsFile);
 		}
 
 	}
 
 	public static File exportResource(String resource) {
 		if (!CopyItDesktop.nativeLoadingInitialized) {
 			System.setProperty(
 					"java.library.path",
 					System.getProperty("java.library.path")
 							+ System.getProperty("path.separator")
 							+ PathBuilder.getCacheDirectory().getAbsolutePath());
 			System.out.println(System.getProperty("java.library.path"));
 
 			Field fieldSysPath = null;
 			try {
 				fieldSysPath = ClassLoader.class.getDeclaredField("sys_paths");
 				fieldSysPath.setAccessible(true);
 				fieldSysPath.set(null, null);
 			} catch (SecurityException e2) {
 				// TODO Auto-generated catch block
 				e2.printStackTrace();
 			} catch (NoSuchFieldException e2) {
 				// TODO Auto-generated catch block
 				e2.printStackTrace();
 			} catch (IllegalArgumentException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (IllegalAccessException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			CopyItDesktop.nativeLoadingInitialized = true;
 		}
 		System.out.println("Exporting resource " + resource);
 		URL inputUrl = CopyItDesktop.class.getResource("/" + resource);
 		File dest = new File(PathBuilder.getCacheDirectory(), resource);
 		if (inputUrl == null) {
 			System.out
 					.println("No input resource available while exporting resource "
 							+ resource + ". " + "Ignoring it.");
 			return null;
 		}
 		try {
 			FileUtils.copyURLToFile(inputUrl, dest);
 		} catch (IOException e1) {
 			System.out.println("Could not copy " + resource
 					+ ". This might cause issues.");
 			e1.printStackTrace();
 		}
 		return dest;
 	}
 
 }
