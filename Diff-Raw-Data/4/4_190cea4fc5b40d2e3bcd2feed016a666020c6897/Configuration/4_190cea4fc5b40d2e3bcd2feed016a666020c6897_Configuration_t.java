 /**
  * 
  */
 package configuration;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.Properties;
 
 import org.apache.log4j.Logger;
 
 import speech.Speech;
 
 /**
  * @author Nauman Badar <nauman.gwt@gmail.com>
  * @created Apr 18, 2011
  * 
  */
 public class Configuration {
 
 	private static final String HARDCODED_HTTP_PORT = "8080";
 	private static final String HARDCODED_SIP_USER = "robot";
 	private static final String HARDCODED_SIP_PORT = "5061";
 
 	private final static Logger log = Logger.getLogger(Configuration.class);
 
 	public static Configuration INSTANCE = new Configuration();
 
 	private Configuration() {
 		currentFilePath = "";
 		defaultFilePath = "sipspeaker.cfg";
 		defaultMessage = "default.wav";
 		workingFilePath = "wsipspeaker.cfg";
 		currentMessage = "";
 		currentText = "";
 		sipUser = "";
 		sipPort = "";
 		httpPort = "";
 	}
 
 	private String defaultMessage;
 	private String defaultText;
 	private String currentMessage;
 	private String currentText;
 	private String sipUser;
 	private String sipPort;
 	private String httpPort;
 	private String defaultFilePath;
 	private String currentFilePath;
 	private String workingFilePath;
 	private Properties properties = new Properties();
 
 	public void insert(String args[]) {
 		try {
 
 			if (args.length == 0 && !(new File(defaultFilePath).exists())) {
 				// FileOutputStream fileOutputStream = new FileOutputStream(new
 				// File(defaultFilePath));
 //				FileOutputStream fileOutputStream = new FileOutputStream(new File(workingFilePath));
 				FileOutputStream fileOutputStream = new FileOutputStream(new File(defaultFilePath));
 				properties.setProperty("default_message", "default.wav");
 				properties.setProperty("message_wav", "");
 				properties.setProperty("message_text", "");
 				properties.setProperty("sip_user", HARDCODED_SIP_USER);
 				properties.setProperty("sip_port", HARDCODED_SIP_PORT);
 				properties.setProperty("http_port", HARDCODED_HTTP_PORT);
 
 				properties.store(fileOutputStream, null);
 				fileOutputStream.flush();
 				fileOutputStream.close();
 
 				// defaultMessage = "default.wav";
 				// currentMessage = "";
 				sipUser = HARDCODED_SIP_USER;
 				sipPort = HARDCODED_SIP_PORT;
 				httpPort = HARDCODED_HTTP_PORT;
 				defaultText = "This is the dynamically generated message when no default configuration file exists.";
 				Speech.produce("default", "This is the dynamically generated message when no default configuration file exists.");
 
 			} else if (args.length == 0 && (new File(defaultFilePath).exists())) {
 
 				FileInputStream fileInputStream = new FileInputStream(new File(defaultFilePath));
 				properties.load(fileInputStream);
 				fileInputStream.close();
 				defaultMessage = properties.getProperty("default_message");
 				currentMessage = properties.getProperty("message_wav");
 				currentText = properties.getProperty("message_text");
 				sipUser = properties.getProperty("sip_user");
 				sipPort = properties.getProperty("sip_port");
 				httpPort = properties.getProperty("http_port");
 				defaultText = "No arguments given.";
 				if (currentMessage.isEmpty()) {
 					currentMessage = "current.wav";
 				}
 				// Speech.produce("default", "No arguments given.");
 				dumpToFile(properties);
 
 			} else if (args.length != 0) {
 
 				parseCommandLineArguments(args);
 				if (new File(currentFilePath).exists()) {
 					FileInputStream fileInputStream = new FileInputStream(new File(currentFilePath));
 					properties.load(fileInputStream);
 					if (sipUser.isEmpty()) {
 						sipUser = properties.getProperty("sip_user");
 					}
 					if (sipPort.isEmpty()) {
 						sipPort = properties.getProperty("sip_port");
 					}
 					if (httpPort.isEmpty()) {
 						httpPort = properties.getProperty("http_port");
 					}
 					defaultMessage = properties.getProperty("default_message");
 					defaultText = "Default Message, message from given configuration file was loaded.";
 					currentMessage = properties.getProperty("message_wav");
 					log.info("current mesg: " + currentMessage);
 					currentText = properties.getProperty("message_text");
 					if (currentMessage.isEmpty()) {
 						currentMessage = "current.wav";
 					}
 					log.info("Given configuration loaded.");
 
 				} else {
 
 					if (sipUser.isEmpty()) {
 						sipUser = HARDCODED_SIP_USER;
 					}
 					if (sipPort.isEmpty()) {
 						sipPort = HARDCODED_SIP_PORT;
 					}
 					if (httpPort.isEmpty()) {
 						httpPort = HARDCODED_HTTP_PORT;
 					}
 					defaultText = "Default Message, Configuration file name was given in arguments but it does not exist.";
					currentMessage = "wrongcfg.wav";
					currentText = "Current Message generated because of wrong configuration file name.";
 				}
 
 				dumpToFile(properties);
 			}
 
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 			log.info(e.getMessage());
 		} catch (IOException e) {
 			log.info(e.getMessage());
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * @param properties
 	 * @throws FileNotFoundException
 	 * @throws IOException
 	 */
 	private void dumpToFile(Properties properties) throws FileNotFoundException, IOException {
 
 		if (!(new File(defaultMessage).exists())) {
 			Speech.produce(defaultMessage.replace(".wav", ""), defaultText);
 		}
 		if (!currentText.isEmpty()) {
 			Speech.produce(currentMessage.replace(".wav", ""), currentText);
 		}
 		properties.setProperty("default_message", defaultMessage);
 		properties.setProperty("message_wav", currentMessage);
 		properties.setProperty("message_text", currentText);
 		properties.setProperty("sip_user", sipUser);
 		properties.setProperty("sip_port", sipPort);
 		properties.setProperty("http_port", httpPort);
 
 //		FileOutputStream fileOutputStream = new FileOutputStream(new File(workingFilePath));
 		FileOutputStream fileOutputStream = new FileOutputStream(new File(defaultFilePath));
 		properties.store(fileOutputStream, null);
 		fileOutputStream.flush();
 		fileOutputStream.close();
 
 		if (!currentFilePath.isEmpty()) {
 			fileOutputStream = new FileOutputStream(new File(currentFilePath));
 			properties.store(fileOutputStream, null);
 			fileOutputStream.flush();
 			fileOutputStream.close();
 		}
 	}
 
 	/**
 	 * @param args
 	 */
 	private void parseCommandLineArguments(String[] args) {
 		int index = 0;
 		for (int i = 0; i < args.length; i++) {
 			if (args[i].compareTo("-c") == 0) {
 				currentFilePath = args[i + 1];
 			}
 			if (args[i].compareTo("-user") == 0) {
 				index = args[i + 1].indexOf("@");
 				sipUser = args[i + 1].substring(0, index);
 				index = args[i + 1].indexOf(":");
 				sipPort = args[i + 1].substring(index + 1);
 			}
 			if (args[i].compareTo("-http") == 0) {
 				index = args[i + 1].indexOf(":");
 				httpPort = args[i + 1].substring(index + 1);
 			}
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see java.lang.Object#toString()
 	 */
 	@Override
 	public String toString() {
 		return "defaultMessage  " + defaultMessage + "\n" + "currentMessage  " + currentMessage + "\n" + "currentText     " + currentText + "\n" + "sipUser         " + sipUser + "\n" + "sipPort         " + sipPort + "\n" + "httpPort        " + httpPort + "\n" + "defaultFilePath " + defaultFilePath + "\n" + "currentFilePath " + currentFilePath + "\n";
 	}
 
 	/**
 	 * @param iNSTANCE
 	 *            the iNSTANCE to set
 	 */
 	public static void setINSTANCE(Configuration iNSTANCE) {
 		INSTANCE = iNSTANCE;
 	}
 
 	/**
 	 * @return the defaultMessage
 	 */
 	public String getDefaultMessage() {
 		return defaultMessage;
 	}
 
 	/**
 	 * @param defaultMessage
 	 *            the defaultMessage to set
 	 */
 	public void setDefaultMessage(String defaultMessage) {
 		this.defaultMessage = defaultMessage;
 	}
 
 	/**
 	 * @return the currentMessage
 	 */
 	public String getCurrentMessage() {
 		return currentMessage;
 	}
 
 	/**
 	 * @param currentMessage
 	 *            the currentMessage to set
 	 */
 	public void setCurrentMessage(String currentMessage) {
 		this.currentMessage = currentMessage;
 	}
 
 	/**
 	 * @return the currentText
 	 */
 	public String getCurrentText() {
 		if (currentMessage.isEmpty()) {
 			return "Fall back to default message!!";
 		}
 		
 		return currentText;
 	}
 
 	/**
 	 * @param currentText
 	 *            the currentText to set
 	 */
 	public void setCurrentText(String currentText) {
 		this.currentText = currentText;
 	}
 
 	/**
 	 * @return the sipUser
 	 */
 	public String getSipUser() {
 		return sipUser;
 	}
 
 	/**
 	 * @param sipUser
 	 *            the sipUser to set
 	 */
 	public void setSipUser(String sipUser) {
 		this.sipUser = sipUser;
 	}
 
 	/**
 	 * @return the sipPort
 	 */
 	public String getSipPort() {
 		return sipPort;
 	}
 
 	/**
 	 * @param sipPort
 	 *            the sipPort to set
 	 */
 	public void setSipPort(String sipPort) {
 		this.sipPort = sipPort;
 	}
 
 	/**
 	 * @return the httpPort
 	 */
 	public String getHttpPort() {
 		return httpPort;
 	}
 
 	/**
 	 * @param httpPort
 	 *            the httpPort to set
 	 */
 	public void setHttpPort(String httpPort) {
 		this.httpPort = httpPort;
 	}
 
 	/**
 	 * @return the defaultFilePath
 	 */
 	public String getDefaultFilePath() {
 		return defaultFilePath;
 	}
 
 	/**
 	 * @param defaultFilePath
 	 *            the defaultFilePath to set
 	 */
 	public void setDefaultFilePath(String defaultFilePath) {
 		this.defaultFilePath = defaultFilePath;
 	}
 
 	/**
 	 * @return the currentFilePath
 	 */
 	public String getCurrentFilePath() {
 		return currentFilePath;
 	}
 
 	/**
 	 * @param currentFilePath
 	 *            the currentFilePath to set
 	 */
 	public void setCurrentFilePath(String currentFilePath) {
 		this.currentFilePath = currentFilePath;
 	}
 
 	public String getPlayMessage() {
 		if (currentMessage.isEmpty()) {
 			return defaultMessage;
 		} else
 			return currentMessage;
 
 	}
 
 	public void updateFromWebserver(String text) {
 		if (text.isEmpty()) {
 			currentMessage = "";
 		} else {
 			currentText = text;
 			if (currentMessage.isEmpty()) {
 				Speech.produce("current", currentText);
 				currentMessage = "current.wav";
 			} else {
 				Speech.produce(currentMessage.replace(".wav", ""), currentText);
 			}
 
 			try {
 				properties.setProperty("message_text", currentText);
 				FileOutputStream fileOutputStream;
 				fileOutputStream = new FileOutputStream(new File(defaultFilePath));
 				properties.store(fileOutputStream, null);
 				fileOutputStream.flush();
 				fileOutputStream.close();
 
 				if (!currentFilePath.isEmpty()) {
 					fileOutputStream = new FileOutputStream(new File(currentFilePath));
 					properties.store(fileOutputStream, null);
 					fileOutputStream.flush();
 					fileOutputStream.close();
 				}
 
 			} catch (FileNotFoundException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 	}
 }
