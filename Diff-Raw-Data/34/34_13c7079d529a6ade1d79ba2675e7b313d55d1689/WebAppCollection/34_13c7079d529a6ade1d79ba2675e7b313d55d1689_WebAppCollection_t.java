 /**
  * Meerkat Monitor - Network Monitor Tool
  * Copyright (C) 2011 Merkat-Monitor
  * mailto: contact AT meerkat-monitor DOT org
  * 
  * Meerkat Monitor is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * Meerkat Monitor is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Lesser General Public License for more details.
  *  
  * You should have received a copy of the GNU Lesser General Public License
  * along with Meerkat Monitor.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package org.meerkat.webapp;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.concurrent.CopyOnWriteArrayList;
 
 import org.apache.log4j.Logger;
 import org.meerkat.db.EmbeddedDB;
 import org.meerkat.group.AppGroupCollection;
 import org.meerkat.httpServer.HttpServer;
 import org.meerkat.services.WebApp;
 import org.meerkat.util.FileUtil;
 import org.meerkat.util.xml.XStreamMeerkatConfig;
 
 import com.thoughtworks.xstream.XStream;
 import com.thoughtworks.xstream.annotations.XStreamOmitField;
 import com.thoughtworks.xstream.io.xml.DomDriver;
 
 public class WebAppCollection {
 	@XStreamOmitField
 	private static Logger log = Logger.getLogger(WebAppCollection.class);
 
 	private List<WebApp> webAppsCollection;
 	@XStreamOmitField
 	private String tempWorkingDir;
 	@XStreamOmitField
 	private String dataFileName = "TimeLine.html";
 	@XStreamOmitField
 	private FileUtil fu;
 	@XStreamOmitField
 	private String appVersion;
 	@XStreamOmitField
 	private String configXMLFile;
 	@XStreamOmitField
 	XStream xstream = new XStream(new DomDriver("UTF-8"));
 	@XStreamOmitField
 	EmbeddedDB embDB = null;
 	@XStreamOmitField
 	Connection conn = null;
 	@XStreamOmitField
 	HttpServer httpServer;
 	@XStreamOmitField
 	AppGroupCollection appGroupCollection;
 
 
 	/**
 	 * WebAppCollection
 	 */
 	public WebAppCollection() {
 		webAppsCollection = new CopyOnWriteArrayList<WebApp>();
 	}
 
 	/**
 	 * setDB
 	 * @param embDB
 	 */
 	public final void setDB(EmbeddedDB embDB){
 		this.embDB = embDB;
 		conn = embDB.getConnForUpdates();
 	}	
 
 	/**
 	 * setHttpServer
 	 * @param httpServer
 	 */
 	public final void setHttpServer(HttpServer httpServer){
 		this.httpServer = httpServer;
 	}
 
 	/**
 	 * setGroupCollection
 	 * @param appGroupCollection
 	 */
 	public final void setGroupCollection(AppGroupCollection appGroupCollection){
 		this.appGroupCollection = appGroupCollection;
 	}
 
 	/**
 	 * setConfigFile
 	 * 
 	 * @param configXMLFile
 	 */
 	public final void setConfigFile(String configXMLFile) {
 		this.configXMLFile = configXMLFile;
 	}
 
 	public final void setTempWorkingDir(String tempWorkingDir) {
 		this.tempWorkingDir = tempWorkingDir;
 	}
 
 	/**
 	 * Return a copy - prevent ConcurrentModificationException
 	 */
 	public final List<WebApp> getCopyWebApps() {
 		List<WebApp> copiedList = new ArrayList<WebApp>(webAppsCollection);
 		return copiedList;
 	}
 
 	/**
 	 * addWebApp
 	 * 
 	 * @param app
 	 */
 	public final void addWebApp(WebApp app) {
 		app.setTempWorkingDir(this.tempWorkingDir);
 		webAppsCollection.add(app);
 		appGroupCollection.populateGroups(this);
 	}
 
 	/**
 	 * webAppCollectionIterator
 	 * 
 	 * @return webAppCollectionIterator
 	 */
 	public final Iterator<WebApp> getWebAppCollectionIterator() {
 		return webAppsCollection.iterator();
 	}
 
 	/**
 	 * webAppCollectionSize
 	 * 
 	 * @return webAppCollectionSize
 	 */
 	public final Integer getWebAppCollectionSize() {
 		return webAppsCollection.size();
 	}
 
 	/**
 	 * getCollectionDataFileName
 	 * 
 	 * @return CollectionDataFileName
 	 */
 	public final String getCollectionDataFileName() {
 		return dataFileName;
 	}
 
 	/**
 	 * setDataFileName
 	 * 
 	 * @param dataFileName
 	 */
 	public final void setDataFileName(String dataFileName) {
 		this.dataFileName = dataFileName;
 	}
 
 	/**
 	 * getTmpDir
 	 * 
 	 * @return tmpDir
 	 */
 	public final String getTmpDir() {
 		return tempWorkingDir;
 	}
 
 	/**
 	 * setTmpDir
 	 * 
 	 * @param tmpDir
 	 */
 	public final void setTmpDir(String tempWorkingDir) {
 		this.tempWorkingDir = tempWorkingDir;
 	}
 
 	/**
 	 * getJSDataTable
 	 * 
 	 * @return JSDataTable
 	 */
 	/**
 	private String DISABLED_getJSCollectionMotionChart() {
 		WebApp webApp;
 		WebAppEvent webAppEvent;
 		String statusText = "";
 		String dataTableData = "";
 		String dataTableBegin = "<script type='text/javascript'>\n"
 				+ "google.load('visualization', '1', {'packages':['motionchart']});\n"
 				+ "google.setOnLoadCallback(drawChart);\n"
 				+ "function drawChart() {\n"
 				+ "var data = new google.visualization.DataTable();\n"
 				+ "data.addColumn('string', 'Name');\n"
 				+ "data.addColumn('date', 'Date');\n"
 				+ "data.addColumn('string', 'Status');\n"
 				+ "data.addColumn('number', 'Availability');\n"
 				+ "data.addColumn('number', 'Network Latency (ms)');\n"
 				+ "data.addColumn('number', 'Load Time (s)');\n"
 				+ "data.addColumn('string', 'Event');\n";
 
 		String dataTableEnd = "var chart = new google.visualization.MotionChart(document.getElementById('chart_div'));;\n"
 				+ "chart.draw(data, {width: 900, height:500});\n"
 				+ "     }"
 				+ "</script>";
 
 		Iterator<WebApp> ia = webAppsCollection.iterator();
 
 		while (ia.hasNext()) {
 			webApp = ia.next();
 			Iterator<WebAppEvent> ie = new WebAppEventListIterator(webApp);
 			while (ie.hasNext()) {
 				webAppEvent = ie.next();
 
 				if (!webAppEvent.getStatus()) {
 					statusText = "OFFLINE";
 				} else {
 					statusText = "ONLINE";
 				}
 
 				String networklatency;
 				if (webAppEvent.getLatency().equalsIgnoreCase("N/A")) {
 					networklatency = "0.0";
 				} else {
 					networklatency = webAppEvent.getLatency();
 				}
 
 				dataTableData += "data.addRow(['" + webApp.getName()
 						+ "', new Date(" + webAppEvent.getDateFormatedGWT()
 						+ "), '" + statusText + "', "
 						+ webAppEvent.getAvailability() + "," + networklatency
 						+ "," + webAppEvent.getPageLoadTime() + ", '"
 						+ webAppEvent.getDescription() + "']);\n";
 			}
 		}
 		return dataTableBegin + dataTableData + dataTableEnd;	
 	}
 	 */
 
 
 	/**
 	 * writeWebAppCollectionDataFile
 	 */
 	/**
 	public final void DISABLED_writeWebAppCollectionMotionChart() {
 		final WebAppCollection wap = this;
 		// With many records this will be time consuming
 		Runnable dataCollectionWriter = new Runnable(){
 			@Override
 			public void run() {
 				fu = new FileUtil();
 				HTMLComponents htmlc = new HTMLComponents(appVersion);
 
 				String htmlFileContentsTop = "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/strict.dtd\">\n"
 						+ "<html>\n"
 						+ "<head>\n"
 						+ "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"/>"
 						+ "<title>TimeLine</title>\n"
 						+ "<link rel=\"icon\"  href=\"/favicon.ico\"  type=\"image/x-icon\"></link>\n"
 						+ "<script type='text/javascript' src='http://www.google.com/jsapi'></script>\n"
 
 				+ "<style type=\"text/css\" title=\"currentStyle\">\n"
 				+ "	@import \"resources/demo_page.css\";\n"
 				+ "	@import \"resources/demo_table_jui.css\";\n"
 				+ "	@import \"resources/jquery-ui-1.8.4.custom.css\";\n"
 				+ "</style>\n"
 
 				+ "<style type=\"text/css\">\n" + "#container {\n"
 				+ "	width: 1000px;\n" + "	margin: 10px auto;\n"
 				+ "	padding: 0;\n" + "}\n" + "</style>\n";
 
 				String htmlFileContentsEnd = "</head>\n"
 						+ "<body id=\"dt_example\">\n"
 						+ "<div id=\"container\">\n"
 						+ "<a href=\"javascript:history.go(-1)\"><img src=\"resources/tango-previous.png\" border=\"0\"></a>\n"
 						+ "<h1>Annotated Time Line</h1>"
 						+ "<div id=\"chart_div\" style=\"width: 900px; height: 500px;\"></div>"
 						+ htmlc.getFooter() + "</div></body>" + "</html>";
 
 				String htmlFileContents = htmlFileContentsTop
 						+ wap.getJSCollectionTimeLine() + htmlFileContentsEnd;
 
 				File tmp = new File(tempWorkingDir);
 				if (!tmp.exists()) {
 					if (!tmp.mkdirs()) {
 						log.error("ERROR creating temporary directory: "
 								+ tempWorkingDir);
 					}
 				}
 				fu.removeFile(tmp + "/" + wap.getCollectionDataFileName());
 				fu.writeToFile(tmp + "/" + wap.getCollectionDataFileName(),htmlFileContents);
 			}
 		};
 		Thread visDataWriterThread = new Thread(dataCollectionWriter);
 		visDataWriterThread.start();
 	}
 	 */
 
 	/**
 	 * isWebAppByNamePresent
 	 * 
 	 * @param name
 	 * @return
 	 */
 	public boolean isWebAppByNamePresent(String name) {
 		List<WebApp> webAppsCollectionCopy = this.webAppsCollection;
 		Iterator<WebApp> it = webAppsCollectionCopy.iterator();
 
 		while (it.hasNext()) {
 			WebApp wap = it.next();
 			if (wap.getName().equalsIgnoreCase(name)) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * getNumberOfEventsInCollection
 	 * 
 	 * @return
 	 */
 	public final int getNumberOfEventsInCollection() {
 		int events = 0;
 		WebApp currWebApp;
 		Iterator<WebApp> i = webAppsCollection.iterator();
 
 		while (i.hasNext()) {
 			currWebApp = i.next();
 			events += currWebApp.getNumberOfEvents();
 		}
 		return events;
 	}
 
 	/**
 	 * setAppVersion
 	 * 
 	 * @param version
 	 */
 	public final void setAppVersion(String version) {
 		appVersion = version;
 	}
 
 	/**
 	 * getAppVersion
 	 * 
 	 * @return
 	 */
 	public final String getAppVersion() {
 		return appVersion;
 	}
 
 	/**
 	 * getWebAppByName
 	 * 
 	 * @param name
 	 * @return
 	 */
 	public final WebApp getWebAppByName(String name) {
 		Iterator<WebApp> it = webAppsCollection.iterator();
 		WebApp currWebApp;
 		while (it.hasNext()) {
 			currWebApp = it.next();
 			if (currWebApp.getName().equalsIgnoreCase(name)) {
 				return currWebApp;
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * removeWebApp
 	 * 
 	 * @param wApp
 	 */
 	public final void removeWebApp(WebApp wApp) {
 		webAppsCollection.remove(wApp);
 		appGroupCollection.populateGroups(this); // Update Groups
 	}
 
 	/**
 	 * saveConfigXMLFile Save configuration file
 	 */
 	public synchronized void saveConfigXMLFile() {
 		XStreamMeerkatConfig xstreamConfig = new XStreamMeerkatConfig();
 		XStream xstream = xstreamConfig.getXstream();
 
 		FileUtil fu = new FileUtil();
 		fu.writeToFile(configXMLFile, xstream.toXML(this));
 	}
 
 	/**
 	 * initialize
 	 * 
 	 * @param version
 	 * @param tempWorkingDir
 	 * @param configFile
 	 */
 	public final void initialize(String version, String tempWorkingDir,
 			String configFile) {
 		setAppVersion(version);
 		setTempWorkingDir(tempWorkingDir);
 		setConfigFile(configFile);
 		dataFileName = "TimeLine.html";
 	}
 
 
 	/**
 	 * printContainingAppsInfo
 	 */
 	public final void printContainingAppsInfo(){
 		Iterator<WebApp> it = getWebAppCollectionIterator();
 		WebApp curr;
 		while(it.hasNext()){
 			curr = it.next();
 			log.info("Name: "+curr.getName()+" \t|\t Type: "+curr.getType());
 		}
 
 	}
 
 	/**
 	 * resetAllAppsData
 	 */
 	public final int resetAllAppsData(){
 		int nEvents = getNumberOfEventsInCollection();
 		String resetQuery = "DELETE FROM EVENTS";
 		PreparedStatement statement;
 		try {
 			statement = conn.prepareStatement(resetQuery);
 			statement.execute();
 			statement.close();
 			conn.commit();
 		} catch (SQLException e) {
 			log.error("Failed to reset all events from DB! - "+e.getMessage());
 		}
 
 		//this.writeWebAppCollectionMotionChart();
 		httpServer.refreshIndex();
 
 		return nEvents;
 	}
 
 	/**
 	 * resetAllAppDataFromName
 	 * @param appName
 	 */
 	public final int resetAllAppDataFromName(String appName){
 		int nEvents = this.getWebAppByName(appName).getNumberOfEvents();
 		String resetAppDataQuery = "DELETE FROM EVENTS WHERE APPNAME LIKE '"+appName+"'";
 		PreparedStatement statement;
 		try {
 			statement = conn.prepareStatement(resetAppDataQuery);
 			statement.execute();
 			statement.close();
 			conn.commit();
 		} catch (SQLException e) {
 			log.error("Failed to reset all events from DB! - "+e.getMessage());
 		}
 
 		this.getWebAppByName(appName).writeWebAppVisualizationDataFile();
 		httpServer.refreshIndex();
 
 		return nEvents;
 	}
 
 	/**
 	 * removeAllApps
 	 * @return
 	 */
 	public final int removeAllApps(){
 		int nWebApps = this.getWebAppCollectionSize();
 		this.resetAllAppsData(); // Clear DB
 		webAppsCollection.clear(); // Clear Apps
 		appGroupCollection.populateGroups(this); // Clear Groups
 
 		return nWebApps;
 	}
 
 	/**
 	 * AppGroupCollection
 	 * @return
 	 */
 	public final AppGroupCollection getpAppGroupCollection(){
 		return appGroupCollection;
 	}
 	
 	/**
 	 * getEmbeddedDB
 	 * @return
 	 */
 	public final EmbeddedDB getEmbeddedDB(){
 		return embDB;
 	}
 
 }
