 /*
  * Copyright (C) 2010, 2011 Openismus GmbH
  *
  * This file is part of GWT-Glom.
  *
  * GWT-Glom is free software: you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as published by the
  * Free Software Foundation, either version 3 of the License, or (at your
  * option) any later version.
  *
  * GWT-Glom is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
  * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
  * for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with GWT-Glom.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package org.glom.web.server;
 
 import java.io.File;
 import java.io.InputStream;
 import java.sql.Connection;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Hashtable;
 import java.util.List;
 import java.util.Properties;
 
 import javax.servlet.ServletException;
 
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.io.FilenameUtils;
 import org.apache.commons.lang3.StringUtils;
 import org.glom.web.client.OnlineGlomService;
 import org.glom.web.server.libglom.Document;
 import org.glom.web.shared.DataItem;
 import org.glom.web.shared.DetailsLayoutAndData;
 import org.glom.web.shared.DocumentInfo;
 import org.glom.web.shared.Documents;
 import org.glom.web.shared.NavigationRecord;
 import org.glom.web.shared.Reports;
 import org.glom.web.shared.TypedDataItem;
 import org.glom.web.shared.libglom.Report;
 import org.glom.web.shared.libglom.layout.LayoutGroup;
 
 import com.google.gwt.user.server.rpc.RemoteServiceServlet;
 import com.mchange.v2.c3p0.DataSources;
 
 /**
  * This is the servlet class for setting up the server side of Online Glom. The client side can call the public methods
  * in this class via OnlineGlom
  * 
  * For instance, it loads all the available documents and provide a list - see getDocuments(). It then provides
  * information from each document. For instance, see getListViewLayout().
  * 
  * TODO: Watch for changes to the .glom files, to reload new versions and to load newly-added files. TODO: Watch for
  * changes to the properties (configuration)?
  */
 @SuppressWarnings("serial")
 public class OnlineGlomServiceImpl extends RemoteServiceServlet implements OnlineGlomService {
 
 	private static final String GLOM_FILE_EXTENSION = "glom";
 
 	// convenience class for dealing with the Online Glom configuration file
 	private static class OnlineGlomProperties extends Properties {
 		public String getKey(final String value) {
 			for (final String key : stringPropertyNames()) {
 				if (getProperty(key).trim().equals(value))
 					return key;
 			}
 			return null;
 		}
 	}
 
 	private final Hashtable<String, ConfiguredDocument> documentMapping = new Hashtable<String, ConfiguredDocument>();
 	private Exception configurationException = null;
 
 	/*
 	 * This is called when the servlet is started or restarted.
 	 * 
 	 * (non-Javadoc)
 	 * 
 	 * @see javax.servlet.GenericServlet#init()
 	 */
 	@Override
 	public void init() throws ServletException {
 
 		// All of the initialisation code is surrounded by a try/catch block so that the servlet can be in an
 		// initialised state and the error message can be retrieved by the client code.
 		try {
 			// Find the configuration file. See this thread for background info:
 			// http://stackoverflow.com/questions/2161054/where-to-place-properties-files-in-a-jsp-servlet-web-application
 			// FIXME move onlineglom.properties to the WEB-INF folder (option number 2 from the stackoverflow question)
 			final OnlineGlomProperties config = new OnlineGlomProperties();
 			final InputStream is = Thread.currentThread().getContextClassLoader()
 					.getResourceAsStream("onlineglom.properties");
 			if (is == null) {
 				final String errorMessage = "onlineglom.properties not found.";
 				Log.fatal(errorMessage);
 				throw new Exception(errorMessage);
 			}
 			config.load(is); // can throw an IOException
 
 			// check if we can read the configured glom file directory
 			final String documentDirName = config.getProperty("glom.document.directory");
 			final File documentDir = new File(documentDirName);
 			if (!documentDir.isDirectory()) {
 				final String errorMessage = documentDirName + " is not a directory.";
 				Log.fatal(errorMessage);
 				throw new Exception(errorMessage);
 			}
 			if (!documentDir.canRead()) {
 				final String errorMessage = "Can't read the files in directory " + documentDirName + " .";
 				Log.fatal(errorMessage);
 				throw new Exception(errorMessage);
 			}
 
 			// get and check the glom files in the specified directory
 			// TODO: Test this:
 			final String[] extensions = { GLOM_FILE_EXTENSION };
 			final List<File> glomFiles = (List<File>) FileUtils
 					.listFiles(documentDir, extensions, true /* recursive */);
 
 			// don't continue if there aren't any Glom files to configure
 			if (glomFiles.size() <= 0) {
 				final String errorMessage = "Unable to find any Glom documents in the configured directory "
 						+ documentDirName
 						+ " . Check the onlineglom.properties file to ensure that 'glom.document.directory' is set to the correct directory.";
 				Log.error(errorMessage);
 				throw new Exception(errorMessage);
 			}
 
 			// Check for a specified default locale,
 			// for table titles, field titles, etc:
 			final String globalLocaleID = StringUtils.defaultString(config.getProperty("glom.document.locale"));
 
 			for (final File glomFile : glomFiles) {
 				final Document document = new Document();
 				document.setFileURI("file://" + glomFile.getAbsolutePath());
				final boolean retval = document.load();
 				if (retval == false) {
					final String message = "An error occurred when trying to load file: " + glomFile.getAbsolutePath();
 					Log.error(message);
 					// continue with for loop because there may be other documents in the directory
 					continue;
 				}
 
 				final ConfiguredDocument configuredDocument = new ConfiguredDocument(document); // can throw a
 				// PropertyVetoException
 
 				// check if a username and password have been set and work for the current document
 				final String filename = glomFile.getName();
 				final String key = config.getKey(filename);
 				if (key != null) {
 					final String[] keyArray = key.split("\\.");
 					if (keyArray.length == 3 && "filename".equals(keyArray[2])) {
 						// username/password could be set, let's check to see if it works
 						final String usernameKey = key.replaceAll(keyArray[2], "username");
 						final String passwordKey = key.replaceAll(keyArray[2], "password");
 						configuredDocument.setUsernameAndPassword(config.getProperty(usernameKey).trim(),
 								config.getProperty(passwordKey)); // can throw an SQLException
 					}
 				}
 
 				// check the if the global username and password have been set and work with this document
 				if (!configuredDocument.isAuthenticated()) {
 					configuredDocument.setUsernameAndPassword(config.getProperty("glom.document.username").trim(),
 							config.getProperty("glom.document.password")); // can throw an SQLException
 				}
 
 				if (!StringUtils.isEmpty(globalLocaleID)) {
 					configuredDocument.setDefaultLocaleID(globalLocaleID.trim());
 				}
 
 				// The key for the hash table is the file name without the .glom extension and with spaces ( ) replaced
 				// with pluses (+). The space/plus replacement makes the key more friendly for URLs.
 				final String documentID = FilenameUtils.removeExtension(filename).replace(' ', '+');
 				configuredDocument.setDocumentID(documentID);
 				documentMapping.put(documentID, configuredDocument);
 			}
 
 		} catch (final Exception e) {
 			// Don't throw the Exception so that servlet will be initialised and the error message can be retrieved.
 			configurationException = e;
 		}
 
 	}
 
 	/*
 	 * This is called when the servlet is stopped or restarted.
 	 * 
 	 * @see javax.servlet.GenericServlet#destroy()
 	 */
 	@Override
 	public void destroy() {
 		for (final String documenTitle : documentMapping.keySet()) {
 			final ConfiguredDocument configuredDoc = documentMapping.get(documenTitle);
 			if (configuredDoc == null)
 				continue;
 
 			try {
 				DataSources.destroy(configuredDoc.getCpds());
 			} catch (final SQLException e) {
 				Log.error(documenTitle, "Error cleaning up the ComboPooledDataSource.", e);
 			}
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.glom.web.client.OnlineGlomService#getConfigurationErrorMessage()
 	 */
 	@Override
 	public String getConfigurationErrorMessage() {
 		if (configurationException == null)
 			return "No configuration errors to report.";
 		else if (configurationException.getMessage() == null)
 			return configurationException.toString();
 		else
 			return configurationException.getMessage();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.glom.web.client.OnlineGlomService#getDocumentInfo(java.lang.String)
 	 */
 	@Override
 	public DocumentInfo getDocumentInfo(final String documentID, final String localeID) {
 
 		final ConfiguredDocument configuredDoc = documentMapping.get(documentID);
 
 		// Avoid dereferencing a null object:
 		if (configuredDoc == null)
 			return new DocumentInfo();
 
 		// FIXME check for authentication
 
 		return configuredDoc.getDocumentInfo(StringUtils.defaultString(localeID));
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.glom.web.client.OnlineGlomService#getListViewLayout(java.lang.String, java.lang.String)
 	 */
 	@Override
 	public LayoutGroup getListViewLayout(final String documentID, final String tableName, final String localeID) {
 		final ConfiguredDocument configuredDoc = documentMapping.get(documentID);
 		if (configuredDoc == null)
 			return new LayoutGroup();
 
 		// FIXME check for authentication
 
 		return configuredDoc.getListViewLayoutGroup(tableName, StringUtils.defaultString(localeID));
 	}
 
 	// TODO: Specify the foundset (via a where clause) and maybe a default sort order.
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.glom.web.client.OnlineGlomService#getReportLayout(java.lang.String, java.lang.String, java.lang.String,
 	 * java.lang.String)
 	 */
 	@Override
 	public String getReportHTML(final String documentID, final String tableName, final String reportName,
 			final String quickFind, final String localeID) {
 		final ConfiguredDocument configuredDoc = documentMapping.get(documentID);
 		if (configuredDoc == null)
 			return "";
 
 		final Document glomDocument = configuredDoc.getDocument();
 		if (glomDocument == null) {
 			final String errorMessage = "getReportHTML(): getDocument() failed.";
 			Log.fatal(errorMessage);
 			// TODO: throw new Exception(errorMessage);
 			return "";
 		}
 
 		// FIXME check for authentication
 
 		final Report report = glomDocument.getReport(tableName, reportName);
 		if (report == null) {
 			Log.info(documentID, tableName, "The report layout is not defined for this table:" + reportName);
 			return "";
 		}
 
 		Connection connection;
 		try {
 			connection = configuredDoc.getCpds().getConnection();
 		} catch (final SQLException e2) {
 			// TODO Auto-generated catch block
 			e2.printStackTrace();
 			return "Connection Failed";
 		}
 
 		// TODO: Use quickFind
 		final ReportGenerator generator = new ReportGenerator(StringUtils.defaultString(localeID));
 		return generator.generateReport(glomDocument, tableName, report, connection, quickFind);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.glom.web.client.OnlineGlomService#getListViewData(java.lang.String, java.lang.String, int, int)
 	 */
 	@Override
 	public ArrayList<DataItem[]> getListViewData(final String documentID, final String tableName,
 			final String quickFind, final int start, final int length) {
 		final ConfiguredDocument configuredDoc = documentMapping.get(documentID);
 		if (!configuredDoc.isAuthenticated()) {
 			return new ArrayList<DataItem[]>();
 		}
 		return configuredDoc.getListViewData(tableName, quickFind, start, length, false, 0, false);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.glom.web.client.OnlineGlomService#getSortedListViewData(java.lang.String, java.lang.String, int, int,
 	 * int, boolean)
 	 */
 	@Override
 	public ArrayList<DataItem[]> getSortedListViewData(final String documentID, final String tableName,
 			final String quickFind, final int start, final int length, final int sortColumnIndex,
 			final boolean isAscending) {
 		final ConfiguredDocument configuredDoc = documentMapping.get(documentID);
 		if (configuredDoc == null)
 			return new ArrayList<DataItem[]>();
 
 		if (!configuredDoc.isAuthenticated()) {
 			return new ArrayList<DataItem[]>();
 		}
 		return configuredDoc.getListViewData(tableName, quickFind, start, length, true, sortColumnIndex, isAscending);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.glom.web.client.OnlineGlomService#getDocuments()
 	 */
 	@Override
 	public Documents getDocuments() {
 		final Documents documents = new Documents();
 		for (final String documentID : documentMapping.keySet()) {
 			final ConfiguredDocument configuredDoc = documentMapping.get(documentID);
 			if (configuredDoc == null)
 				continue;
 
 			final Document glomDocument = configuredDoc.getDocument();
 			if (glomDocument == null) {
 				final String errorMessage = "getDocuments(): getDocument() failed.";
 				Log.fatal(errorMessage);
 				// TODO: throw new Exception(errorMessage);
 				continue;
 			}
 
 			final String localeID = StringUtils.defaultString(configuredDoc.getDefaultLocaleID());
 			documents.addDocument(documentID, glomDocument.getDatabaseTitle(localeID), localeID);
 		}
 		return documents;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.glom.web.client.OnlineGlomService#isAuthenticated(java.lang.String)
 	 */
 	@Override
 	public boolean isAuthenticated(final String documentID) {
 		final ConfiguredDocument configuredDoc = documentMapping.get(documentID);
 		if (configuredDoc == null)
 			return false;
 
 		return configuredDoc.isAuthenticated();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.glom.web.client.OnlineGlomService#checkAuthentication(java.lang.String, java.lang.String,
 	 * java.lang.String)
 	 */
 	@Override
 	public boolean checkAuthentication(final String documentID, final String username, final String password) {
 		final ConfiguredDocument configuredDoc = documentMapping.get(documentID);
 		if (configuredDoc == null) {
 			Log.error(documentID, "The document could not be found for this ID: " + documentID);
 			return false;
 		}
 
 		try {
 			return configuredDoc.setUsernameAndPassword(username, password);
 		} catch (final SQLException e) {
 			Log.error(documentID, "Unknown SQL Error checking the database authentication.", e);
 			return false;
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.glom.web.client.OnlineGlomService#getReportsList(java.lang.String, java.lang.String, java.lang.String)
 	 */
 	@Override
 	public Reports getReportsList(final String documentID, final String tableName, final String localeID) {
 		final ConfiguredDocument configuredDoc = documentMapping.get(documentID);
 		return configuredDoc.getReports(tableName, localeID);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.glom.web.client.OnlineGlomService#getDetailsData(java.lang.String, java.lang.String, java.lang.String)
 	 */
 	@Override
 	public DataItem[] getDetailsData(final String documentID, final String tableName,
 			final TypedDataItem primaryKeyValue) {
 		final ConfiguredDocument configuredDoc = documentMapping.get(documentID);
 		if (configuredDoc == null)
 			return new DataItem[0];
 
 		// FIXME check for authentication
 
 		return configuredDoc.getDetailsData(tableName, primaryKeyValue);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.glom.web.client.OnlineGlomService#getDetailsLayoutAndData(java.lang.String, java.lang.String,
 	 * java.lang.String)
 	 */
 	@Override
 	public DetailsLayoutAndData getDetailsLayoutAndData(final String documentID, final String tableName,
 			final TypedDataItem primaryKeyValue, final String localeID) {
 		final ConfiguredDocument configuredDoc = documentMapping.get(documentID);
 		if (configuredDoc == null)
 			return null;
 
 		// FIXME check for authentication
 
 		final DetailsLayoutAndData initalDetailsView = new DetailsLayoutAndData();
 		initalDetailsView
 				.setLayout(configuredDoc.getDetailsLayoutGroup(tableName, StringUtils.defaultString(localeID)));
 		initalDetailsView.setData(configuredDoc.getDetailsData(tableName, primaryKeyValue));
 
 		return initalDetailsView;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.glom.web.client.OnlineGlomService#getRelatedListData(java.lang.String, java.lang.String, int, int)
 	 */
 	@Override
 	public ArrayList<DataItem[]> getRelatedListData(final String documentID, final String tableName,
 			final String relationshipName, final TypedDataItem foreignKeyValue, final int start, final int length) {
 		final ConfiguredDocument configuredDoc = documentMapping.get(documentID);
 
 		// FIXME check for authentication
 
 		return configuredDoc.getRelatedListData(tableName, relationshipName, foreignKeyValue, start, length, false, 0,
 				false);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.glom.web.client.OnlineGlomService#getSortedRelatedListData(java.lang.String, java.lang.String, int, int,
 	 * int, boolean)
 	 */
 	@Override
 	public ArrayList<DataItem[]> getSortedRelatedListData(final String documentID, final String tableName,
 			final String relationshipName, final TypedDataItem foreignKeyValue, final int start, final int length,
 			final int sortColumnIndex, final boolean ascending) {
 		final ConfiguredDocument configuredDoc = documentMapping.get(documentID);
 		if (configuredDoc == null)
 			return new ArrayList<DataItem[]>();
 
 		// FIXME check for authentication
 
 		return configuredDoc.getRelatedListData(tableName, relationshipName, foreignKeyValue, start, length, true,
 				sortColumnIndex, ascending);
 	}
 
 	@Override
 	public int getRelatedListRowCount(final String documentID, final String tableName, final String relationshipName,
 			final TypedDataItem foreignKeyValue) {
 		final ConfiguredDocument configuredDoc = documentMapping.get(documentID);
 		if (configuredDoc == null)
 			return 0;
 
 		// FIXME check for authentication
 
 		return configuredDoc.getRelatedListRowCount(tableName, relationshipName, foreignKeyValue);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.glom.web.client.OnlineGlomService#getSuitableRecordToViewDetails(java.lang.String, java.lang.String,
 	 * java.lang.String, java.lang.String)
 	 */
 	@Override
 	public NavigationRecord getSuitableRecordToViewDetails(final String documentID, final String tableName,
 			final String relationshipName, final TypedDataItem primaryKeyValue) {
 		final ConfiguredDocument configuredDoc = documentMapping.get(documentID);
 		if (configuredDoc == null)
 			return new NavigationRecord();
 
 		// FIXME check for authentication
 
 		return configuredDoc.getSuitableRecordToViewDetails(tableName, relationshipName, primaryKeyValue);
 	}
 
 }
