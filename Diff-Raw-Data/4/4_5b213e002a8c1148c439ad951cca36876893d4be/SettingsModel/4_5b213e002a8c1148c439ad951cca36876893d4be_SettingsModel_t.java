 package se.starbox.models;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.net.InetAddress;
 import java.net.Socket;
 import java.net.URL;
 import java.net.URLDecoder;
 import java.net.UnknownHostException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.List;
 
 import javax.xml.parsers.*;
 import javax.xml.transform.OutputKeys;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 
 import org.w3c.dom.Document;
 import org.xml.sax.SAXException;
 
 import org.openpipeline.scheduler.JobInfo;
 import org.openpipeline.scheduler.PipelineScheduler;
 
 /**
  * Model for getting and changing local user settings.
  * Keeps local variables and writes changes to UserSettings.xml.
  * @author Anders
  *
  */
 public class SettingsModel {
 	private static final String XML_PATH = SettingsModel.getProjectRootPath() + "/UserSettings.xml";
 	private static final String XML_HEADER = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n<UserSettings xmlns=\"starbox\">\n";
 	private static final String XML_FOOTER = "</UserSettings>";
 	
 	private static final String PATH_TO_OPENPIPELINE_JOB = SettingsModel.getProjectRootPath() + "/config/jobs/StarboxJob.xml";
 	
 	private static final String DEFAULT_STARBOX_FOLDER = "C:/Documents/";
 	private static final String DEFAULT_DISPLAY_NAME = "default display name";
 	private static final String DEFAULT_EMAIL = "";
 	private static final int DEFAULT_INDEX_UPDATE_INTERVAL = 30;
 	
 	private String starboxFolder;
 	private String displayName;
 	private String email;
 	private int indexUpdateInterval; // in minutes
 	
 	/**
 	 * Creates a new SettingsModel that immediately attempts to read from UserSettings.xml and populate local fields.
 	 * If UserSettings.xml does not exist, fields get default values as given by private constants in this class.
 	 */
 	public SettingsModel() {
 		initModel();
 	}
 	
 	/**
 	 * Loads and parses XML-document and updates local fields with the values. If the XML-document does not exist, the default values are used.
 	 */
 	private void initModel() {
 		File xml = new File(XML_PATH);
 		
 		// If UserSettings.xml does not exist, create it and write the default values to it, then return
 		if (!xml.exists()) {
 			try {
 				xml.createNewFile();
 				starboxFolder = DEFAULT_STARBOX_FOLDER;
 				displayName = DEFAULT_DISPLAY_NAME;
 				email = DEFAULT_EMAIL;
 				indexUpdateInterval = DEFAULT_INDEX_UPDATE_INTERVAL;
 				writeToFile();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			return;
 		}
 		
 		// Otherwise, we found a UserSettings.xml and can start parsing the XML document
 		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
 		try {
 			DocumentBuilder db = dbf.newDocumentBuilder();
 			Document d = db.parse(xml);
 			
 			starboxFolder = (d.getElementsByTagName("StarboxFolder")).item(0).getTextContent();//.replace('\\', '/');
 			displayName = (d.getElementsByTagName("DisplayName")).item(0).getTextContent();
 			email = (d.getElementsByTagName("Email")).item(0).getTextContent();
 			// If something other than a number is stored, just take the default value
 			try {
 				indexUpdateInterval = Integer.parseInt((d.getElementsByTagName("IndexUpdateInterval")).item(0).getTextContent());
 			} catch (NumberFormatException e) {
 				indexUpdateInterval = DEFAULT_INDEX_UPDATE_INTERVAL;
 			}
 			
 			setStarboxFolder(starboxFolder);
 			setIndexUpdateInterval(indexUpdateInterval);
 			
 		} catch (ParserConfigurationException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (SAXException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
 	/**
 	 * Writes current local values to UserSettings.xml
 	 */
 	private void writeToFile() {
 		File xml = new File(XML_PATH);
 		try {
 			BufferedWriter w = new BufferedWriter(new FileWriter(xml));
 			w.write(XML_HEADER);
 			w.write("\t<StarboxFolder><![CDATA[" + starboxFolder + "]]></StarboxFolder>\n");
 			w.write("\t<DisplayName><![CDATA[" + displayName + "]]></DisplayName>\n");
 			w.write("\t<Email><![CDATA[" + email + "]]></Email>\n");
 			w.write("\t<IndexUpdateInterval><![CDATA[" + indexUpdateInterval + "]]></IndexUpdateInterval>\n");
 			w.write(XML_FOOTER);
 			w.close();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}	
 	}
 	
 	/**
 	 * Gets user's IP address and returns it as a String.
 	 * First attempts to get the external IP via whatismyip.com, if this fails, it gets local IP.
 	 * If both fail, returns null.
 	 * @return The user's IP address (or null on failure)
 	 */
 	public static String getIP() {
 		String ip;
 		try {
			//Connect to externalip.net to fetch external IP
			URL whatismyip = new URL("http://api.externalip.net/ip/");
 			BufferedReader in = new BufferedReader(new InputStreamReader(whatismyip.openStream()));
 			
 			ip = in.readLine();
 		} catch (Exception e) {
 			//If external IP was unavailable for whatever reason, get internal IP
 			try {
 				ip = InetAddress.getLocalHost().getHostAddress();
 			} catch (UnknownHostException e2) {
 				// TODO Auto-generated catch block
 				e2.printStackTrace();
 				ip = null;
 			}
 		}
 		
 		return ip;
 	}
 	
 	/**
 	 * Returns the path to the root of the Starbox webapp running on Tomcat. This means it will point to the root of the WebContent folder!
 	 * @return The path of the webapp root (WebContent)
 	 */
 	public static String getProjectRootPath() {
 		// Get project path
 		String projectPath = null;
 		try {
 			String thisClassPath = SettingsModel.class.getProtectionDomain().getCodeSource().getLocation().getPath();
 			projectPath = URLDecoder.decode(thisClassPath, "UTF-8");
 			projectPath = projectPath.replace('\\', '/');
 			for (int i = projectPath.length() - 1, slashCount = 0; i >= 0; i--) {
 				if (projectPath.charAt(i) == '/') {
 					slashCount++;
 					if (slashCount == 6) {
 						projectPath = projectPath.substring(0, i);
 						break;
 					}
 				}
 			}
 		} catch (Exception e) {
 			return null;
 		}
 		return projectPath + ((projectPath.charAt(projectPath.length() - 1) != '/') ? "/" : "");
 	}
 	
 	/**
 	 * Starts the OpenPipeline job to begin indexing the local Starbox folder.
 	 */
 	public static void updateIndex() {
 		// TODO: delete index data, start the OpenPipeline job?
 		try {
 			String projectPath = SettingsModel.getProjectRootPath();
 			System.out.println("Project path: " + projectPath);
 			
 			// Set 'app.home' property, required by openpipeline
 			System.setProperty("app.home", projectPath);
 			
 			// Test: List all jobs
 			PipelineScheduler scheduler = PipelineScheduler.getInstance();
 			
 			@SuppressWarnings("unchecked")
 			List<JobInfo> jobs = scheduler.getJobs();
 			System.out.println("# of existing jobs: " + jobs.size());
 			for (Iterator<JobInfo> jobIt = jobs.iterator(); jobIt.hasNext(); )
 				System.out.println(jobIt.next().getJobName());
 			
 			// Test: Run all jobs
 			for (Iterator<JobInfo> jobIt = jobs.iterator(); jobIt.hasNext(); )
 				scheduler.startJob(jobIt.next().getJobName());
 			
 			//PipelineScheduler.stop();
 		}
 		catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 	
 	/**
 	 * Shuts down program cleanly
 	 */
 	public static void shutDown() {
 		// TODO: Before shutting down, make sure all changes have been saved, check current downloads are done etc.
 		try {
 			// Send shutdown command to shutdown port on Catalina
 			Socket socket = new Socket("localhost", 8005);
 			if (socket.isConnected()) {
 				PrintWriter pw = new PrintWriter(socket.getOutputStream(), true);
 				pw.println("SHUTDOWN");
 				pw.close();
 				socket.close();
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 	
 	/**
 	 * Gets starbox folder from UserSettings.xml
 	 * @return The user's starbox folder as a String
 	 */
 	public String getStarboxFolder() {
 		return starboxFolder;
 	}
 	
 	/**
 	 * Gets user's display name from UserSettings.xml
 	 * @return The user's display name as a String
 	 */
 	public String getDisplayName() {
 		return displayName;
 	}
 	
 	/**
 	 * Gets user's email from UserSettings.xml
 	 * @return The user's email as a String
 	 */
 	public String getEmail() {
 		return email;
 	}
 	
 	/**
 	 * Gets user's index update interval (in minutes) from UserSettings.xml
 	 * @return The number of minutes between each interval as an integer value
 	 */
 	public int getIndexUpdateInterval() {
 		return indexUpdateInterval;
 	}
 	
 	/**
 	 * Updates user's starbox folder setting and writes to UserSettings.xml and StarboxJob.xml
 	 * @param path The new starbox folder as a String
 	 * @return true if valid path
 	 */
 	public boolean setStarboxFolder(String path) {
 		if (!new File(path).isDirectory())
 			return false;
 		starboxFolder = path.replace('\\', '/');
 		writeToFile();
 		
 		// Write to StarboxJob.xml
 		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
 		try {
 			DocumentBuilder db = dbf.newDocumentBuilder();
 			Document d = db.parse(PATH_TO_OPENPIPELINE_JOB);
 			
 			d.getElementsByTagName("fileroots").item(0).setTextContent(path);
 			
 			TransformerFactory tf = TransformerFactory.newInstance();
 			Transformer t = tf.newTransformer();
 			t.setOutputProperty(OutputKeys.INDENT, "yes");
 			
 			DOMSource s = new DOMSource(d);
 			StreamResult sr = new StreamResult(new File(PATH_TO_OPENPIPELINE_JOB));
 			
 			t.transform(s, sr);
 			return true;
 		} catch (Exception e) {
 			System.err.println("SettingsModel - could not update StarboxJob.xml");
 		}
 		return false;
 	}
 	
 	/**
 	 * Updates user's display name setting and writes to UserSettings.xml
 	 * @param displayName The new display name as a String
 	 */
 	public void setDisplayName(String displayName) {
 		this.displayName = displayName;
 		writeToFile();
 	}
 	
 	/**
 	 * Updates user's email address and writes to UserSettings.xml
 	 * @param email The new email as a String
 	 */
 	public void setEmail(String email) {
 		this.email = email;
 		writeToFile();
 	}
 	
 	/**
 	 * Update's user's index update interval and writes to UserSettings.xml, also updates OpenPipeline job to use the new interval (not implemented yet)
 	 * @param minutes The new interval in minutes as an integer
 	 */
 	public void setIndexUpdateInterval(int minutes) {
 		indexUpdateInterval = minutes;
 		writeToFile();
 		
 		// Write to StarboxJob.xml
 		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
 		try {
 			DocumentBuilder db = dbf.newDocumentBuilder();
 			Document d = db.parse(PATH_TO_OPENPIPELINE_JOB);
 			
 			d.getElementsByTagName("period").item(0).setTextContent("minutes");
 			d.getElementsByTagName("period-interval").item(0).setTextContent(Integer.toString(minutes));
 			d.getElementsByTagName("starttime").item(0).setTextContent(new SimpleDateFormat("yyyy-MM-dd hh:mm:ss a").format(new Date()));
 			
 			
 			TransformerFactory tf = TransformerFactory.newInstance();
 			Transformer t = tf.newTransformer();
 			t.setOutputProperty(OutputKeys.INDENT, "yes");
 			
 			DOMSource s = new DOMSource(d);
 			StreamResult sr = new StreamResult(new File(PATH_TO_OPENPIPELINE_JOB));
 			
 			t.transform(s, sr);
 		} catch (Exception e) {
 			System.err.println("SettingsModel - could not update StarboxJob.xml");
 		}
 	}
 	
 	
 }
