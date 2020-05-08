 package org.microx.archiftp.ftp.internal;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.SocketException;
 import java.util.Dictionary;
 
 import org.apache.commons.net.ftp.FTP;
 import org.apache.commons.net.ftp.FTPClient;
 import org.microx.archiftp.ftp.FtpService;
 import org.osgi.service.cm.ConfigurationException;
 import org.osgi.service.cm.ManagedService;
 import org.osgi.service.log.LogService;
 import org.osgi.util.tracker.ServiceTracker;
 
 public final class FtpServiceImpl implements FtpService, ManagedService {
 
 	// Services
 	private ServiceTracker logServiceTracker;
 	private FTPClient ftp = new FTPClient();
 
 	// Managed properties
 	private String hostname = "localhost";
 	private int port = 21;
 	private String username = "anonymous";
 	private String password = "";
 	private String directory = "/";
 
 	public FtpServiceImpl(ServiceTracker logServiceTracker) {
 		this.logServiceTracker = logServiceTracker;
 	}
 
 	public void uploadFile(File file) throws IOException, FileNotFoundException {
 		try {
 			connect();
 		}
 		catch (SocketException e) {
 			throw new IOException("socket timeout of FTPClient could not be set.", e);
 		}
 
 		enterPassiveMode();
 
 		if (login() == false) {
 			logErrorLoginFailed();
 			throw new IOException("Login to server with username '" + this.username + "' failed.");
 		}
 
 		if (changeFileTypeToBinary() == false) {
 			logErrorChangeFileTypeFailed();
 			throw new IOException("Changing upload file type to binary failed.");
 		}
 		
 		if (upload(file) == false) {
 			logErrorUploadFailed(file);
 			throw new IOException(
 					"Uploading file failed. (Is directory '" + this.directory + "' exists and writeable?)");
 		}
 
 		disconnect();
 		logDebugUploadComplete(file);
 	}
 
 	private void connect() throws SocketException, IOException {
 		ftp.connect(this.hostname, this.port);
 	}
 
 	private void enterPassiveMode() throws IOException {
 		ftp.pasv();
 	}
 
 	private boolean login() throws IOException {
 		return ftp.login(this.username, this.password);
 	}
 
 	private boolean changeFileTypeToBinary() throws IOException {
 		return ftp.setFileType(FTP.BINARY_FILE_TYPE);
 	}
 	
 	private boolean upload(File file) throws FileNotFoundException, IOException {
 		String path = getFilePathInServer(file);
 		InputStream stream = getFileStream(file);
 		boolean result = storeFile(path, stream);
 		stream.close();
 		return result;
 	}
 
 	private String getFilePathInServer(File file) {
 	    StringBuffer buff = new StringBuffer();
 	    buff.append(this.directory);
	    buff.append(File.separator);
 	    buff.append(file.getName());
 	    return buff.toString();
 	}
 
 	private InputStream getFileStream(File file) throws FileNotFoundException {
 		InputStream stream = new FileInputStream(file);
 		return stream;
 	}
 
 	private boolean storeFile(String path, InputStream stream) throws IOException {
 		return ftp.storeFile(path, stream);
 	}
 
 	private void disconnect() throws IOException {
 		ftp.disconnect();
 	}
 
 	public void updated(Dictionary properties) throws ConfigurationException {
 		if (properties != null) {
 			updatedHostname(properties);
 			updatedPort(properties);
 			updatedUsername(properties);
 			updatedPassword(properties);
 			updatedDirectory(properties);
 			logInfoPropertiesUpdated();
 		}
 	}
 
 	private void updatedHostname(Dictionary properties) {
 		String newHostname = (String) properties.get("hostname");
 		setHostname(newHostname);
 	}
 
 	private void updatedPort(Dictionary properties) throws ConfigurationException {
 		int newPort;
 		try {
 			newPort = ((Integer) properties.get("port")).intValue();
 		}
 		catch (ClassCastException e) {
 			throw new ConfigurationException("port",
 					"Invalid port. (Port must be integer between 1-65535.)", e);
 		}
 
 		if (isPortValid(newPort)) {
 			setPort(newPort);
 		}
 		else {
 			throw new ConfigurationException("port",
 					"Invalid port. (Port must be integer between 1-65535.)");
 		}
 	}
 
 	private boolean isPortValid(int port) {
 		return (port > 0) && (port < 65536);
 	}
 
 	private void updatedUsername(Dictionary properties) {
 		String newUsername = (String) properties.get("username");
 		setUsername(newUsername);
 	}
 
 	private void updatedPassword(Dictionary properties) {
 		String newPassword = (String) properties.get("password");
 		setPassword(newPassword);
 	}
 
 	private void updatedDirectory(Dictionary properties) {
 		String newDirectory = (String) properties.get("directory");
 		setDirectory(newDirectory);
 	}
 
 	public String getHostname() {
 		return hostname;
 	}
 
 	public void setHostname(String hostname) {
 		this.hostname = hostname;
 	}
 
 	public int getPort() {
 		return port;
 	}
 
 	public void setPort(int port) {
 		this.port = port;
 	}
 
 	public String getUsername() {
 		return username;
 	}
 
 	public void setUsername(String username) {
 		this.username = username;
 	}
 
 	public String getPassword() {
 		return password;
 	}
 
 	public void setPassword(String password) {
 		this.password = password;
 	}
 
 	public String getDirectory() {
 		return directory;
 	}
 
 	public void setDirectory(String directory) {
 		this.directory = directory;
 	}
 
 	// Log
 	private void logErrorLoginFailed() {
 	    logError("Login to '" + this.hostname + ":" + this.port + "' with username '"+ this.username + "' failed");
 	}
 	
 	private void logErrorChangeFileTypeFailed() {
 		logError("Changing upload file type to binary failed.");
 	}
 
 	private void logErrorUploadFailed(File file) {
 		logError("Uploading file '" + file.getAbsolutePath() + "' to '" + this.hostname + 
             ":" + this.port + this.directory + "' failed.  " +
 			"(Is directory exists and writeable?)");
 	}
 
 	private void logDebugUploadComplete(File file) {
 		logDebug("'" + file.getAbsolutePath() + "' was uploaded to '" + this.hostname + 
 		    ":" + this.port + this.directory + "'");
 	}
 
 	private void logInfoPropertiesUpdated() {
 		String logMessage = "Properties of FtpService updated. " +
 		        "{hostname=" + this.hostname +
 		        ",port=" + this.port + 
 		        ",username=" + this.username +
 		        ",directory=" + this.directory + "}";
 		logInfo(logMessage);
 	}
 
 	private void logError(String message) {
 		log(LogService.LOG_ERROR, message);
 	}
 
 	private void logDebug(String message) {
 		log(LogService.LOG_DEBUG, message);
 	}
 
 	private void logInfo(String message) {
 		log(LogService.LOG_INFO, message);
 	}
 
 	private void log(int level, String message) {
 		LogService logService = getLogService();
 		if (logService != null) {
 			logService.log(level, message);
 		}
 		else {
 			String nameOfLevel = getNameOfLevel(level);
 			System.out.println("[" + nameOfLevel + "] " +  message);
 		}
 	}
 
 	private String getNameOfLevel(int level) {
 		switch (level) {
 			case LogService.LOG_DEBUG:
 				return "Debug";
 			case LogService.LOG_ERROR:
 				return "Error";
 			case LogService.LOG_INFO:
 				return "Info";
 			case LogService.LOG_WARNING:
 				return "Warning";
 			default:
 				return "";
 		}
 	}
 
 	private LogService getLogService() {
 		if (this.logServiceTracker == null) {
 			return null;
 		}
 
 		return (LogService) this.logServiceTracker.getService();
 	}
 
 }
