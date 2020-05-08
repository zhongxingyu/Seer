 package de.deyovi.chat.core.utils;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.MissingResourceException;
 import java.util.PropertyResourceBundle;
 import java.util.ResourceBundle;
 
 import org.apache.log4j.Logger;
 
 import de.deyovi.chat.core.constants.ChatConstants;
 
 public class ChatConfiguration {
 
 	private final static Logger logger = Logger.getLogger(ChatConfiguration.class);
 
 	private static final int DEFAULT_UPLOAD_THRESHOLD_SIZE = 1024 * 1024 * 50;    // 1MB
 	private static final int DEFAULT_UPLOAD_MAX_FILE_SIZE = 1024 * 1024 * 50;    // 5MB
 	private static final int DEFAULT_UPLOAD_REQUEST_SIZE = 1024 * 1024 * 51;    // 6MB
 	
 	private static ChatConfiguration instance;
 
 	private byte[] passwordSalt = "TEST".getBytes();
 	private boolean invitationRequired = false;
 
 	private String[] channels = null;
 	private String dataDir = null;
 	private String defaultDataDir = null;
 	private String baseDir = null;
 	private String phantomjs = null;
 	private String renderjs = null;
 	private String ffmpegthumbnailer = null;
 	private Integer uploadThreshold = null;
 	private Integer uploadMaximum = null;
 	private Integer uploadRequest = null;
 	private List<String> extraPlugins = new LinkedList<String>();
 
 	public ChatConfiguration() {
 		try {
 			defaultDataDir = File.createTempFile("yourchat", "tmp").getParent();
 		} catch (IOException e) {
 			defaultDataDir = null;
 			logger.error("Cannot create tempfile!", e);
 		}
 		readConfig();
 	}
 
 	public void setDefaultDataDir(String defaultDataDir) {
 		this.defaultDataDir = defaultDataDir;
 	}
 
 	public static boolean isInvitationRequired() {
 		return instance.invitationRequired;
 	}
 
 	public static String getDataDir() {
 		return instance.dataDir;
 	}
 
 	public static String getBaseDir() {
 		return instance.baseDir;
 	}
 
 	public static byte[] getSalt() {
 		return instance.passwordSalt;
 	}
 
 	public static String getPhantomjs() {
 		return instance.phantomjs;
 	}
 
 	public static String getRenderJS() {
 		return instance.renderjs;
 	}
 
 	public static String getFFMPEGThumbnailer() {
 		return instance.ffmpegthumbnailer;
 	}
 
 	public static String[] getRooms() {
 		return instance.channels;
 	}
 
 	public static List<String> getExtraPlugins() {
 		return instance.extraPlugins;
 	}
 	
 	public static Integer getUploadMaximum() {
 		return instance.uploadMaximum;
 	}
 	
 	public static Integer getUploadRequest() {
 		return instance.uploadRequest;
 	}
 	
 	public static Integer getUploadThreshold() {
 		return instance.uploadThreshold;
 	}
 
 	public static File createTempDirectory(File parent) throws IOException {
 		final File temp = File.createTempFile("yourchat", "tmp", parent);
 
 		if (!(temp.delete())) {
 			throw new IOException("Could not delete temp file: "
 					+ temp.getAbsolutePath());
 		}
 
 		if (!(temp.mkdir())) {
 			throw new IOException("Could not create temp directory: "
 					+ temp.getAbsolutePath());
 		}
 
 		return (temp);
 	}
 	
 	public static void initialize() {
 		instance = new ChatConfiguration();
 	}
 
 	private void readConfig() {
 		ResourceBundle bundle = PropertyResourceBundle
 				.getBundle(ChatConstants.PROPERTY_BUNDLE);
 		// Channels
 		String channelsParam = readResource(bundle, ChatConstants.PROPERTY_CHANNELS);
 		if (channelsParam != null && !channelsParam.trim().isEmpty()) {
 			channels = channelsParam.split(";");
 		} else {
 			// TODO l10n
 			channels = new String[] { "Main" };
 		}
 		// PhantomJS
 		phantomjs = readResource(bundle, ChatConstants.PROPERTY_PHANTOMJS);
 		if (phantomjs != null) {
 			if (new File(phantomjs).isFile()) {
 				logger.info("path to phantomjs is: " + phantomjs + " (present)");
 			} else {
 				logger.warn("path to phantomjs is: " + phantomjs
 						+ " (NOT present)");
 				phantomjs = null;
 			}
 		}
 		// RenderJS-File
 		renderjs = readResource(bundle, ChatConstants.PROPERTY_RENDERJS);
 		if (renderjs != null) {
 			if (new File(renderjs).isFile()) {
 				logger.info("path to renderjs is: " + renderjs + " (present)");
 			} else {
 				logger.warn("path to renderjs is: " + renderjs
 						+ " (NOT present)");
 				renderjs = null;
 			}
 		}
 		// FFMPEGThumbnailer
 		ffmpegthumbnailer = readResource(bundle, ChatConstants.PROPERTY_FFMPEGTHUMBNAILER);
 		if (ffmpegthumbnailer != null) {
 			if (new File(ffmpegthumbnailer).isFile()) {
 				logger.info("path to ffmpegthumbnailer is: "
 						+ ffmpegthumbnailer + " (present)");
 			} else {
 				logger.warn("path to ffmpegthumbnailer is: "
 						+ ffmpegthumbnailer + " (NOT present)");
 				ffmpegthumbnailer = null;
 			}
 		}
 		// Data/Upload-Path
 		dataDir = readResource(bundle, ChatConstants.PROPERTY_DATAPATH);
 		if (dataDir != null && dataDir.trim().isEmpty()) {
 			dataDir = null;
 		}
 		if (dataDir != null) {
 			String state = checkAndCreateFile(dataDir);
 			logger.info("path for uploads is: " + dataDir + state);
 		} else {
 			logger.info("no path for uploads supplied, relying on current default (" + defaultDataDir + ")");
 			dataDir = defaultDataDir;
 		}
 		// Upload-Parameters
 		uploadThreshold = strToInt(readResource(bundle, ChatConstants.PROPERTY_UPLOAD_THRESHOLD));
 		if (uploadThreshold == null) {
 			uploadThreshold = DEFAULT_UPLOAD_THRESHOLD_SIZE;
 		}
 		uploadMaximum = strToInt(readResource(bundle, ChatConstants.PROPERTY_UPLOAD_MAXIMUM));
 		if (uploadMaximum == null) {
 			uploadMaximum = DEFAULT_UPLOAD_MAX_FILE_SIZE;
 		}
 		uploadRequest = strToInt(readResource(bundle, ChatConstants.PROPERTY_UPLOAD_REQUEST));
 		if (uploadRequest == null) {
 			uploadRequest = DEFAULT_UPLOAD_REQUEST_SIZE;
 		}
 		
 	}
 	
 	private String readResource(ResourceBundle bundle, String resourcePath) {
 		try {
 			return bundle.getString(resourcePath);
 		} catch (MissingResourceException mre) {
 			logger.warn(mre);
 			return null;
 		}
 	}
 
 	private String checkAndCreateFile(String dataDir) {
 		File dataFile = new File(dataDir);
 		int dirState = dataFile.isDirectory() ? 1 : 0;
 		if (dirState == 0) {
 			dirState = dataFile.mkdir() ? 2 : 0;
 		}
 		if (!dataFile.canWrite()) {
 			dirState = -1;
 		}
 		String state;
 		switch (dirState) {
 		case -1:
 			state = " (cannot write!)";
 			break;
 		case 0:
 			state = " (cannot create!)";
 			break;
 		case 1:
 			state = " (exists)";
 			break;
 		case 2:
 			state = " (created)";
 			break;
 		default:
 			state = " (?)";
 		}
 		return state;
 	}
 	
 	private Integer strToInt(String input) {
 		Integer output;
 		try {
 			output = Integer.parseInt(input);
 		} catch (Exception nfe) {
 			output = null;
 		}
 		return output;
 	}
 
 
 }
