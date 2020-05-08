 package devhood.im.sim.config;
 
 import java.awt.Color;
 import java.awt.Image;
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 import java.security.KeyPair;
 import java.security.KeyPairGenerator;
 import java.security.NoSuchAlgorithmException;
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import java.util.prefs.BackingStoreException;
 import java.util.prefs.Preferences;
 
 import javax.annotation.PostConstruct;
 import javax.inject.Named;
 import javax.swing.ImageIcon;
 import javax.swing.UIManager;
 
import org.apache.commons.lang3.StringUtils;
 import org.springframework.beans.factory.annotation.Value;
 
 import devhood.im.sim.ui.util.UiUtil;
 
 /**
  * Konfigurationsklasse von SIM.
  *
  * @author flo
  *
  */
 @Named("simConfiguration")
 public class SimConfiguration {
 
 	private Logger log = Logger.getLogger(SimConfiguration.class.toString());
 
 	private String applicationName = "SIM - S Instant Messenger";
 
 	/**
 	 * URL zum Projekt auf Github.
 	 */
 	private String projectGithuburl = "https://github.com/anrub/SIM";
 
 	/**
 	 * URL zur Smileyübersicht, vorsichtshalber aufs Wiki.
 	 */
 	private String smileyOverview = "https://github.com/anrub/SIM/wiki";
 
 	/**
 	 *
 	 */
 	private int maxLinkLength = 75;
 
 	/**
 	 * Port auf dem der Server lauscht. 0 sucht einen freien Port.
 	 */
 	private int port = 0;
 
 	/**
 	 * Anzahl der Threads zum versenden der Messages.
 	 */
 	private int senderThreads = 10;
 
 	/**
 	 * Laedt User 1 Sekunden nach dem Start.
 	 */
 	private int userLoadDelay = 1000;
 
 	/**
 	 * Laedt User alle 10 Sekunden.
 	 */
 	private int userLoadPeriod = 10000;
 
 	/**
 	 * Interval, in dem der Nutzerstatus in refresh(User) in der DB aktualisiert
 	 * wird.
 	 */
 	private int initRefreshUserStateInterval = 10000;
 
 	/**
 	 * Keypair zur Ver/Entschluesselung der Messages.
 	 */
 	private KeyPair keyPair;
 
 	/**
 	 * Name des Tabs in dem der Stream läuft.
 	 */
 	private String streamTabName = "Stream";
 
 	/**
 	 * Pfad zum Trayicon.
 	 */
 	private String trayIconPath = "/images/trayIcon.gif";
 
 	/**
 	 * Systray Icon.
 	 */
 	private Image trayIcon = UiUtil.createImage(trayIconPath);
 
 	/**
 	 * Icon der gelesenen Nachrichten.
 	 */
 	private ImageIcon readIcon = UiUtil.createImageIcon("/images/read.png",
 			"Gelesene Nachrichten");
 
 	/**
 	 * Icon der ungelesenen Nachrichten.
 	 */
 	private ImageIcon unreadIcon = UiUtil.createImageIcon("/images/unread.png",
 			"Ungelesene Nachrichten");
 
 	/**
 	 * Icon des Konfigframes.
 	 */
 	private Image configurationFrameImage = UiUtil
 			.createImage("/images/configIcon.png");
 
 	/**
 	 * Icon des Konfigframes.
 	 */
 	private ImageIcon configurationFrameIcon = UiUtil.createImageIcon(
 			"/images/configIcon.png", "Konfiguration");
 
 	/**
 	 * Look and Feel - default System Laf.
 	 */
 	private String lookAndFeel = UIManager.getSystemLookAndFeelClassName();
 
 	/**
 	 * Eigener Benutzername.
 	 */
 	@Value("#{systemProperties['sim.username']}")
 	private String username;
 
 	/**
 	 * Erweiterte Konfigurationen, die z.B bestimmte MessageProcessors nutzen.
 	 */
 	private Map<String, String> configuration;
 
 	/**
 	 * Sollen systray messages gezeigt werden oder nicht.
 	 */
 	private boolean showSystrayMessages = true;
 
 	/**
 	 * Zeigt die Stream Meldungen im systray an oder nicht.
 	 */
 	private boolean showStreamInTray = true;
 
 	/**
 	 * Zeigt die Statusmeldungen im Tray an oder nicht.
 	 */
 	private boolean showStatusInTray = true;
 
 	/**
 	 * Welches UserDao soll genutzt werden? "jtds","single","sqlite" Default:
 	 * jtds
 	 */
 	@Value("#{systemProperties['sim.userDao']}")
 	private String userDaoToUse;
 
 	/**
 	 * Key der Userfarbe im lokalen Preferences Speicher.
 	 */
 	public static String USER_COLOR_KEY = "userColor";
 
 	/**
 	 * Mindest differen zwischen den RGB Farben.
 	 */
 	private int minColorDiff = 70;
 
 	/**
 	 * Key der Konfig ob die Tabs gecached werde sollen.
 	 */
 	public static String TAB_CACHE_KEY = "userColor";
 
 	public List<String> autojoinRooms = new LinkedList<String>();
 
 	@PostConstruct
 	public void init() {
 		autojoinRooms.add(getStreamTabName());
 	}
 
 	public static String AUTOJOIN_KEY = "autojoinRooms";
 
 	public void saveAutojoinRooms(String rooms) {
 		Preferences p = getPreferences();
 		p.put(AUTOJOIN_KEY, rooms);
 	}
 
 	public List<String> getAutojoinRooms() {
 		Preferences p = getPreferences();
 		String s = p.get(AUTOJOIN_KEY, null);
 		List<String> autojoinRooms = new ArrayList<String>();
 
		if (!StringUtils.isEmpty(s)) {
 			String[] rooms = s.split(",");
 			for (String room : rooms) {
 				autojoinRooms.add(room);
 			}
 		}
 
 		return autojoinRooms;
 	}
 
 	/**
 	 * Ermittelt die aktuelle IP..
 	 *
 	 * @return ip
 	 */
 	public String getCurrentHostname() {
 		String myHostName = null;
 		try {
 			String hostName = InetAddress.getLocalHost().getHostName();
 			myHostName = hostName;
 		} catch (UnknownHostException e) {
 			log.log(Level.SEVERE,
 					"Lokaler Hostname konnte nicht ermittelt werden!", e);
 		}
 
 		return myHostName;
 	}
 
 	/**
 	 * Gibt das KeyPair fuer die Verschluesselung der Messages zurueck.
 	 *
 	 * @return KeyPair
 	 */
 	public KeyPair getKeyPair() {
 		if (keyPair == null) {
 			KeyPairGenerator kpg;
 			try {
 				kpg = KeyPairGenerator.getInstance("RSA");
 				kpg.initialize(2048);
 				keyPair = kpg.generateKeyPair();
 			} catch (NoSuchAlgorithmException e) {
 				log.log(Level.SEVERE, "Konnte Schluesselpaar nicht generieren",
 						e);
 			}
 		}
 
 		return keyPair;
 	}
 
 	/**
 	 * Versucht die Versionsnummer aus der Datei /SIM_VERSION.txt aus dem
 	 * classpath zu lesen.
 	 *
 	 * @return Versionsnummer oder ""
 	 */
 	public String getVersion() {
 		String version = "";
 		InputStream is = null;
 		try {
 			is = getClass().getResourceAsStream("/SIM_VERSION.txt");
 			BufferedReader read = new BufferedReader(new InputStreamReader(is));
 			version = read.readLine();
 		} catch (Exception e) {
 			log.log(Level.SEVERE, "Konnte Versionfile nicht lesen", e);
 		} finally {
 			try {
 				is.close();
 			} catch (IOException e) {
 				log.log(Level.SEVERE, "Konnte Inputstream nicht schliessen", e);
 			}
 		}
 
 		return version;
 	}
 
 	/**
 	 * Gibt das attribut USER_COLOR_KEY aus den preferences zurueck.
 	 *
 	 * @return usercolor
 	 */
 	public String getUserColor() {
 		Preferences prefs = getPreferences();
 
 		String userColor = "#" + prefs.get(USER_COLOR_KEY, "000000");
 
 		return userColor;
 	}
 
 	/**
 	 * Gibt die User color als int[] R,G,B zurueck.
 	 *
 	 * @return color
 	 */
 	public int[] getUserColorRgb(String color) {
 		color = color.replace("#", "");
 
 		int[] rgb = new int[3];
 		rgb[0] = 0;
 		rgb[1] = 0;
 		rgb[2] = 0;
 
 		if (color.length() == 6) {
 			String red = color.substring(0, 2);
 			String green = color.substring(2, 4);
 			String blue = color.substring(4, 6);
 			rgb[0] = Integer.parseInt(red, 16);
 			rgb[1] = Integer.parseInt(green, 16);
 			rgb[2] = Integer.parseInt(blue, 16);
 		}
 
 		return rgb;
 	}
 
 	/**
 	 * Gibt die User color zurueck.
 	 *
 	 * @return color
 	 */
 	public int[] getUserColorRgb() {
 		String color = getPreferences().get(USER_COLOR_KEY, "000000");
 
 		return getUserColorRgb(color);
 	}
 
 	/**
 	 * Gibt den RGB Wert zurueck.
 	 *
 	 * @return RGB int Wert.
 	 */
 	public int getUserColorRgbValue() {
 		int[] rgb = getUserColorRgb();
 		return rgb[0] + rgb[1] + rgb[2];
 	}
 
 	/**
 	 * Gibt den hexwert von c 2 stellig zurueck.
 	 *
 	 * @param c
 	 *            c
 	 * @return hexwert
 	 */
 	public String getHexValue(int c) {
 		String hex = Integer.toHexString(c);
 		if (hex.length() < 2) {
 			hex = "0" + hex;
 		}
 
 		return hex;
 	}
 
 	/**
 	 * Speichert die usercolor in den Preferences.
 	 *
 	 * @param c
 	 *            Color.
 	 */
 	public void saveUserColor(Color c) {
 		Preferences prefs = getPreferences();
 
 		prefs.put(USER_COLOR_KEY,
 				getHexValue(c.getRed()) + getHexValue(c.getGreen())
 						+ getHexValue(c.getBlue()));
 
 		try {
 			prefs.sync();
 		} catch (BackingStoreException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public Preferences getPreferences() {
 		return Preferences.userNodeForPackage(SimConfiguration.class);
 	}
 
 	/**
 	 * Gibt zurueck ob der TabCache aktiviert ist oder nicht.
 	 *
 	 * @return true/false;
 	 */
 	public boolean isTabCacheEnabled() {
 		// TODO Cache Konfig in ui anbieten.
 		return true;
 	}
 
 	public String getUsername() {
 		return username;
 	}
 
 	public void setUsername(String username) {
 		this.username = username;
 	}
 
 	public Map<String, String> getConfiguration() {
 		return configuration;
 	}
 
 	public void setConfiguration(Map<String, String> configuration) {
 		this.configuration = configuration;
 	}
 
 	public int getSenderThreads() {
 		return senderThreads;
 	}
 
 	public void setSenderThreads(int senderThreads) {
 		this.senderThreads = senderThreads;
 	}
 
 	public void setKeyPair(KeyPair keyPair) {
 		this.keyPair = keyPair;
 	}
 
 	public String getStreamTabName() {
 		return streamTabName;
 	}
 
 	public void setStreamTabName(String streamTabName) {
 		this.streamTabName = streamTabName;
 	}
 
 	public String getTrayIconPath() {
 		return trayIconPath;
 	}
 
 	public void setTrayIconPath(String trayIconPath) {
 		this.trayIconPath = trayIconPath;
 	}
 
 	public Image getTrayIcon() {
 		return trayIcon;
 	}
 
 	public void setTrayIcon(Image trayIcon) {
 		this.trayIcon = trayIcon;
 	}
 
 	public ImageIcon getReadIcon() {
 		return readIcon;
 	}
 
 	public void setReadIcon(ImageIcon readIcon) {
 		this.readIcon = readIcon;
 	}
 
 	public ImageIcon getUnreadIcon() {
 		return unreadIcon;
 	}
 
 	public void setUnreadIcon(ImageIcon unreadIcon) {
 		this.unreadIcon = unreadIcon;
 	}
 
 	public String getLookAndFeel() {
 		return lookAndFeel;
 	}
 
 	public void setLookAndFeel(String lookAndFeel) {
 		this.lookAndFeel = lookAndFeel;
 	}
 
 	public String getApplicationName() {
 		return applicationName;
 	}
 
 	public int getPort() {
 		return port;
 	}
 
 	public void setPort(int port) {
 		this.port = port;
 	}
 
 	public boolean isShowSystrayMessages() {
 		return showSystrayMessages;
 	}
 
 	public void setShowSystrayMessages(boolean showSystrayMessages) {
 		this.showSystrayMessages = showSystrayMessages;
 	}
 
 	public boolean isShowStreamInTray() {
 		return showStreamInTray;
 	}
 
 	public void setShowStreamInTray(boolean showStreamInTray) {
 		this.showStreamInTray = showStreamInTray;
 	}
 
 	public boolean isShowStatusInTray() {
 		return showStatusInTray;
 	}
 
 	public void setShowStatusInTray(boolean showStatusInTray) {
 		this.showStatusInTray = showStatusInTray;
 	}
 
 	public int getUserLoadDelay() {
 		return userLoadDelay;
 	}
 
 	public void setUserLoadDelay(int userLoadDelay) {
 		this.userLoadDelay = userLoadDelay;
 	}
 
 	public int getUserLoadPeriod() {
 		return userLoadPeriod;
 	}
 
 	public void setUserLoadPeriod(int userLoadPeriod) {
 		this.userLoadPeriod = userLoadPeriod;
 	}
 
 	public int getInitRefreshUserStateInterval() {
 		return initRefreshUserStateInterval;
 	}
 
 	public void setInitRefreshUserStateInterval(int initRefreshUserStateInterval) {
 		this.initRefreshUserStateInterval = initRefreshUserStateInterval;
 	}
 
 	public String getProjectGithuburl() {
 		return projectGithuburl;
 	}
 
 	public void setProjectGithuburl(String projectGithuburl) {
 		this.projectGithuburl = projectGithuburl;
 	}
 
 	public String getSmileyOverview() {
 		return smileyOverview;
 	}
 
 	public void setSmileyOverview(String smileyOverview) {
 		this.smileyOverview = smileyOverview;
 	}
 
 	public int getMaxLinkLength() {
 		return maxLinkLength;
 	}
 
 	public void setMaxLinkLength(int maxLinkLength) {
 		this.maxLinkLength = maxLinkLength;
 	}
 
 	public String getUserDaoToUse() {
 		return userDaoToUse;
 	}
 
 	public void setUserDaoToUse(String userDaoToUse) {
 		this.userDaoToUse = userDaoToUse;
 	}
 
 	public int getMinColorDiff() {
 		return minColorDiff;
 	}
 
 	public void setMinColorDiff(int minColorDiff) {
 		this.minColorDiff = minColorDiff;
 	}
 
 	public Image getConfigurationFrameImage() {
 		return configurationFrameImage;
 	}
 
 	public void setConfigurationFrameImage(Image configurationFrameImage) {
 		this.configurationFrameImage = configurationFrameImage;
 	}
 
 	public ImageIcon getConfigurationFrameIcon() {
 		return configurationFrameIcon;
 	}
 
 	public void setConfigurationFrameIcon(ImageIcon configurationFrameIcon) {
 		this.configurationFrameIcon = configurationFrameIcon;
 	}
 
 }
