 package org.jboss.maven.plugins.util;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.sql.Timestamp;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 
 import org.apache.commons.httpclient.Credentials;
 import org.apache.commons.httpclient.Header;
 import org.apache.commons.httpclient.HttpClient;
 import org.apache.commons.httpclient.HttpException;
 import org.apache.commons.httpclient.HttpMethod;
 import org.apache.commons.httpclient.URI;
 import org.apache.commons.httpclient.URIException;
 import org.apache.commons.httpclient.UsernamePasswordCredentials;
 import org.apache.commons.httpclient.auth.AuthScope;
 import org.apache.commons.httpclient.methods.GetMethod;
 import org.apache.commons.httpclient.methods.InputStreamRequestEntity;
 import org.apache.commons.httpclient.methods.PostMethod;
 import org.apache.maven.plugin.AbstractMojo;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.logging.Log;
 import org.codehaus.plexus.util.FileUtils;
 import org.dom4j.Document;
 import org.dom4j.DocumentException;
 import org.dom4j.Element;
 import org.dom4j.io.SAXReader;
 
 /**
  * Given some basic params, publish a Hudson job to a given server
  * 
  * @goal run
  * 
  * @phase validate
  * 
  */
 public class HudsonJobSyncMojo extends AbstractMojo {
 
 	/**
 	 * @parameter expression="${verbose}" default-value="false"
 	 */
 	private boolean verbose = false;
 
 	public boolean getVerbose() {
 		return verbose;
 	}
 
 	public void setVerbose(boolean verbose) {
 		this.verbose = verbose;
 	}
 
 	/**
 	 * @parameter expression="${hudsonURL}"
 	 *            default-value="http://localhost:8080/"
 	 */
 	private String hudsonURL = "http://localhost:8080/";
 
 	public String getHudsonURL() {
 		return hudsonURL;
 	}
 
 	public void setHudsonURL(String hudsonURL) {
 		this.hudsonURL = hudsonURL;
 	}
 
 	/**
 	 * @parameter expression="${username}" default-value="admin"
 	 */
 	private String username = "admin";
 
 	public String getUsername() {
 		return username;
 	}
 
 	public void setUsername(String username) {
 		this.username = username;
 	}
 
 	/**
 	 * @parameter expression="${password}" default-value="none"
 	 */
 	private String password = "none";
 
 	public String getPassword() {
 		return password;
 	}
 
 	public void setPassword(String password) {
 		this.password = password;
 	}
 
 	/**
 	 * @parameter expression="${viewFilter}" default-value="view/myViewName/"
 	 */
 	private String viewFilter = "view/myViewName/";
 
 	// "view/DevStudio_Trunk/";
 	// "view/DevStudio_Stable_Branch/";
 	// "view/SAVARA/";
 	public String getViewFilter() {
 		return viewFilter;
 	}
 
 	public void setViewFilter(String viewFilter) {
 		this.viewFilter = viewFilter;
 	}
 
 	/**
 	 * @parameter expression="${regexFilter}" default-value=".*"
 	 */
 	private String regexFilter = ".*";
 
 	// "jbosstools-.+_trunk.*|devstudio-.+_trunk.*";
 	// "jbosstools-.+_stable_branch.*|devstudio-.+_stable_branch.*";
 	public String getRegexFilter() {
 		return regexFilter;
 	}
 
 	public void setRegexFilter(String regexFilter) {
 		this.regexFilter = regexFilter;
 	}
 
 	/**
 	 * @parameter expression="${operation}" default-value="pull"
 	 */
 	private String operation = "pull";
 
 	public String getOperation() {
 		return operation;
 	}
 
 	public void setOperation(String operation) {
 		this.operation = operation;
 	}
 
 	/**
 	 * @parameter expression="${overwriteExistingConfigXMLFile}"
 	 *            default-value="false"
 	 */
 	private boolean overwriteExistingConfigXMLFile = false;
 
 	public boolean getOverwriteExistingConfigXMLFile() {
 		return overwriteExistingConfigXMLFile;
 	}
 
 	public void setOverwriteExistingConfigXMLFile(
 			boolean overwriteExistingConfigXMLFile) {
 		this.overwriteExistingConfigXMLFile = overwriteExistingConfigXMLFile;
 	}
 
 	/**
 	 * @parameter expression="${storeSnapshotOnPush}" default-value="false"
 	 */
 	private boolean storeSnapshotOnPush = false;
 
 	public boolean getStoreSnapshotOnPush() {
 		return storeSnapshotOnPush;
 	}
 
 	public void setStoreSnapshotOnPush(boolean storeSnapshotOnPush) {
 		this.storeSnapshotOnPush = storeSnapshotOnPush;
 	}
 
 	public String[] getJobNames() throws Exception {
 		Log log = getLog();
 		HttpClient client = getHttpClient(username, password);
 		String URLSuffix = viewFilter;
 
 		// URLSuffix is usually either "" or something like
 		// "view/DevStudio_Trunk/" (with trailing slash)
 		// note: URLSuffix is ignored when accessing a localhost Hudson
 		// instance, as it's assumed we're testing + view may not exist
 		HttpMethod method = new GetMethod(hudsonURL
 				+ (hudsonURL.indexOf("localhost") >= 0 ? "" : URLSuffix)
 				+ "api/xml");
 		client.executeMethod(method);
 		checkResult(method.getStatusCode(), method.getURI());
 
 		ArrayList<String> jobNames = new ArrayList<String>();
 
 		Document dom = new SAXReader().read(method.getResponseBodyAsStream());
 		// scan through the job list and print its status
 		for (Element job : (List<Element>) dom.getRootElement().elements("job")) {
 			if (!job.elementText("name").toString().replaceAll(regexFilter, "")
 					.equals(job.elementText("name").toString())) {
 				// if (verbose) { getLog().info("Matched: " +
 				// job.elementText("name").toString()); }
 				jobNames.add(job.elementText("name").toString());
 			}
 		}
 		return jobNames.toArray(new String[jobNames.size()]);
 	}
 
 	public void execute() throws MojoExecutionException {
 		if (verbose) {
 			getLog().info("Hudson URL: " + hudsonURL);
 			getLog().info("Operation:  " + operation);
 		}
 
 		// for testing, can use static array declaration instead of pulling list
 		// from Hudson via viewFilter and regexFilter
 		// String[] jobNames = { "jbosstools-3.3_trunk.component--TEMPLATE" };
 		String[] jobNames = null;
 		try {
 			jobNames = getJobNames();
 		} catch (Exception e1) {
 			e1.printStackTrace();
 		}
 
 		boolean doOverwriteWarning = false;
 		if (jobNames != null) {
 			for (int i = 0; i < jobNames.length; i++) {
 				String jobName = jobNames[i];
 				getLog().info("");
 				getLog().info("== " + jobName + " ==");
 				File configXMLFile = null;
 				if (operation.equals("pull")) {
 					doOverwriteWarning = pull(jobName, doOverwriteWarning);
 				} else if (operation.equals("push")) {
 					push(jobName);
 				}
 			}
 
 			if (doOverwriteWarning && verbose) {
 				getLog().info("");
 				getLog().info(
 						"To overwrite local config.xml file(s) with server copy, use");
 				getLog().info(
 						"  `mvn clean install -DoverwriteExistingConfigXMLFile=true`, or ");
 				getLog().info(
 						"  <overwriteExistingConfigXMLFile>true</overwriteExistingConfigXMLFile> in pom");
 				getLog().info("");
 			}
 		}
 	}
 
 	public void push(String jobName) throws MojoExecutionException {
 		File configXMLFile;
 		configXMLFile = new File(getConfigXMLFilename(jobName));
 		postConfigXML(configXMLFile, jobName);
 		try {
 			if (verbose) {
 				getLog().info(
 						"Local config.xml pushed from: "
 								+ configXMLFile.toString());
 			} else {
 				getLog().info("Local config.xml pushed to job: " + jobName);
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		if (storeSnapshotOnPush) {
 			try {
 				// copy config.xml to config.2011-03-29_17:43:40.794.xml
 				FileUtils.copyFile(configXMLFile,
 						getConfigXMLFile(jobName, true));
 			} catch (IOException e1) {
 				e1.printStackTrace();
 			}
 		}
 	}
 
 	public boolean pull(String jobName, boolean doOverwriteWarning) {
 		File configXMLFile;
 		// returns
 		// cache/servername/view/viewname/job/jobName/config.$timestamp.xml
 		// or NULL, if nothing new on server so nothing to create
 		configXMLFile = getConfigXMLFile(jobName, true, true);
 		if (verbose) {
 			try {
 				if (configXMLFile != null) {
 					getLog().info(
 							"Snapshot config.xml generated: "
 									+ configXMLFile.toString());
 				} else {
 					getLog().info(
 							"Snapshot config.xml unchanged: "
 									+ getConfigXMLFilename(jobName));
 				}
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 		if (configXMLFile != null) {
 			if (overwriteExistingConfigXMLFile) {
 				try {
 					// copy config.2011-03-29_17:43:40.794.xml to config.xml
 					FileUtils.copyFile(configXMLFile, new File(
 							getConfigXMLFilename(jobName)));
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 
 				if (verbose) {
 					getLog().info(
 							"Latest config.xml    updated: "
 									+ getConfigXMLFilename(jobName));
 				}
 			} else {
 				if (verbose) {
 					getLog().info(
 							"Latest config.xml  exists in: "
 									+ getJobFolder(jobName));
 				}
 				doOverwriteWarning = true;
 			}
 		}
 		return doOverwriteWarning;
 	}
 
 	public String getConfigXMLFilename(String jobName) {
 		return getJobFolder(jobName) + "/config.xml";
 	}
 
 	// cache/$servername/view/$viewname/job/$jobName/
 	private File getJobFolder(String jobName) {
 		String jobFolderPath = "cache/" + hudsonURL.replaceAll("://", "/")
 				+ "/" + viewFilter + "/job/" + jobName + "/";
 		new File(jobFolderPath).mkdirs();
 		return new File(jobFolderPath);
 	}
 
 	// fetch config.xml from server, and store in
 	// cache/$servername/view/$viewname/job/jobName/config.$timestamp.xml
 	// see also createJobFolder(jobName)
 	public File getConfigXMLFile(String jobName, boolean includeTimestamp,
 			boolean writeOnlyIfNew) {
 		File configXMLFile = null;
 		Document configXML = null;
 		try {
 			configXML = getJobConfigXML(jobName);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 		if (configXML != null) {
 			// define file to write
 			if (configXMLFile == null) {
 				configXMLFile = getConfigXMLFile(jobName, includeTimestamp);
 			}
 			try {
 				// write to file
 				if (writeOnlyIfNew) {
 					String latestConfigXMLFilename = getConfigXMLFilename(jobName);
 					File latestConfigXMLFile = new File(latestConfigXMLFilename);
 					// if config.xml doesn't exist, create a new snapshot file
 					if (latestConfigXMLFile == null
 							|| !latestConfigXMLFile.exists()) {
 						writeDomToFile(configXML, configXMLFile);
 						return configXMLFile;
 					} else {
 						// check XML to see if it's different
 						Document latestConfigXML = null;
 						try {
 							latestConfigXML = new SAXReader()
 									.read(latestConfigXMLFile);
 						} catch (DocumentException e) {
 							e.printStackTrace();
 						}
 						if (latestConfigXML == null
 								|| !configXML.asXML().equals(
 										latestConfigXML.asXML())) {
 							writeDomToFile(configXML, configXMLFile);
 							return configXMLFile;
 						} else {
 							// XML not different.
 							// if (verbose) {
 							// getLog().info(
 							// "Server copy and local copy are equal: "
 							// + latestConfigXMLFilename);
 							// }
 						}
 					}
 				} else {
 					writeDomToFile(configXML, configXMLFile);
 					return configXMLFile;
 				}
 			} catch (MojoExecutionException e) {
 				e.printStackTrace();
 			}
 
 		}
 		return null;
 	}
 
 	public File getConfigXMLFile(String jobName, boolean includeTimestamp) {
 		return new File(getJobFolder(jobName), "config"
 				+ (includeTimestamp ? "." + createTimestamp() : "") + ".xml");
 	}
 
 	private String createTimestamp() {
 		// Timestamp currentTimestamp = new Timestamp(Calendar
 		// .getInstance().getTime().getTime());
 
 		Calendar cal = Calendar.getInstance();
 		Date now = new Date(cal.getTimeInMillis());
 		java.sql.Timestamp currentTimestamp = new java.sql.Timestamp(
 				now.getTime());
 		return currentTimestamp.toString().replaceAll(" ", "_");
 	}
 
 	public void postConfigXML(File configXMLFile, String jobName)
 			throws MojoExecutionException {
 		// and post it back to the server
 		postXML(configXMLFile, null, hudsonURL + "job/" + jobName
 				+ "/config.xml", true);
 
 	}
 
 	public void writeDomToFile(Document dom, String xmlFile) {
 		FileWriter w = null;
 		if (dom != null) {
 			try {
 				w = new FileWriter(new File(xmlFile));
 				dom.write(w);
 			} catch (IOException e) {
 				e.printStackTrace();
 			} finally {
 				if (w != null) {
 					try {
 						w.close();
 					} catch (IOException e) {
 						// ignore
 					}
 				}
 			}
 		}
 	}
 
 	public Object[] updateJob(File xmlFile, String jobName,
 			boolean getErrorMessage) {
 		if (verbose)
 			getLog().info("Update config.xml for job " + jobName);
 		return postXML(xmlFile, null, hudsonURL + "/job/" + jobName
 				+ "/config.xml", getErrorMessage);
 	}
 
 	public Object[] updateJob(String xmlFile, String jobName,
 			boolean getErrorMessage) {
 		if (verbose)
 			getLog().info("Update config.xml for job " + jobName);
 		return postXML(xmlFile, null, hudsonURL + "/job/" + jobName
 				+ "/config.xml", getErrorMessage);
 	}
 
 	public String[] createJob(File xmlFile, String jobName,
 			boolean getErrorMessage) {
 		if (verbose)
 			getLog().info("Create job " + jobName);
 		return postXML(xmlFile, null, hudsonURL + "createItem?name=" + jobName,
 				getErrorMessage);
 	}
 
 	public String[] createJob(String xmlFile, String jobName,
 			boolean getErrorMessage) {
 		if (verbose)
 			getLog().info("Create job " + jobName);
 		return postXML(xmlFile, null, hudsonURL + "createItem?name=" + jobName,
 				getErrorMessage);
 	}
 
 	public String[] deleteJob(File xmlFile, String jobName,
 			boolean getErrorMessage) {
 		if (verbose)
 			getLog().info("Delete job " + jobName);
 		return postXML(xmlFile, null, hudsonURL + "job/" + jobName
 				+ "/doDelete", getErrorMessage);
 	}
 
 	public String[] deleteJob(String xmlFile, String jobName,
 			boolean getErrorMessage) {
 		if (verbose)
 			getLog().info("Delete job " + jobName);
 		return postXML(xmlFile, null, hudsonURL + "job/" + jobName
 				+ "/doDelete", getErrorMessage);
 	}
 
 	private String getErrorMessage(PostMethod post, String jobName) {
		Log log = getLog();
 		// scan through the job list and retrieve error message
 		Document dom;
 		String error = null;
 		try {
 			// File tempfile = File.createTempFile("getErrorMessage", "");
 			// writeToFile(tempfile, message);
 			InputStream is = post.getResponseBodyAsStream();
 			dom = new SAXReader().read(is);
 			// <p>A job already exists with the name
 			// 'jbosstools-bpel'</p>
 			// see src/main/resources/400-JobExistsError.html
 			for (Element el : (List<Element>) dom
 					.selectNodes("/html/body/table[2]/tr/td/h1")) {
 				if (el.getTextTrim().equals("Error")) {
 					for (Element el2 : (List<Element>) el.getParent()
 							.selectNodes("p")) {
 						error = el2.getText();
 					}
 				}
 			}
 		} catch (DocumentException e) {
			log.error("Error reading from " + jobName);
 			// e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 			// } catch (MojoExecutionException e) {
 			// e.printStackTrace();
 		}
 		return error;
 	}
 
 	private String[] postXML(String xmlFile, String xmlContents, String jobURL,
 			boolean getErrorMessage) {
 		return postXML(new File(xmlFile), xmlContents, jobURL, getErrorMessage);
 	}
 
 	private String[] postXML(File xmlFile, String xmlContents, String jobURL,
 			boolean getErrorMessage) {
 		int resultCode = -1;
 		String responseBody = "";
 		PostMethod post = new PostMethod(jobURL);
 		HttpClient client = null;
 
 		if (xmlFile == null && xmlContents != null) {
 			File tempDir = null;
 			try {
 				tempDir = createTempDir(getClass().getSimpleName());
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 			xmlFile = new File(tempDir, "config.xml");
 			try {
 				writeToFile(xmlContents, xmlFile);
 			} catch (MojoExecutionException e) {
 				e.printStackTrace();
 			}
 		}
 		if (xmlFile != null) {
 			try {
 				post.setRequestEntity(new InputStreamRequestEntity(
 						new FileInputStream(xmlFile), xmlFile.length()));
 			} catch (FileNotFoundException e) {
				log.error("File not found: " + xmlFile);
 				e.printStackTrace();
 			}
 
 		} else {
 			try {
 				throw new MojoExecutionException("Error writing to " + xmlFile);
 			} catch (MojoExecutionException e) {
 				getLog().error(
 						"Error writing to " + xmlFile + " in postXML() !");
 				e.printStackTrace();
 			}
 		}
 		// Specify content type and encoding; default to ISO-8859-1
 		post.setRequestHeader("Content-type", "text/xml; charset=ISO-8859-1");
 
 		if (client == null) {
 			client = getHttpClient(username, password);
 		}
 		try {
 			resultCode = client.executeMethod(post);
 			if (getErrorMessage) {
 
 				responseBody = getResponseBody(post);
 				// resultString = getErrorMessage(post, jobURL);
 			}
 			// if (verbose) {
 			// getLog().info("Response status code: " + resultCode);
 			// getLog().info("Response body: ");
 			// getLog().info(resultString);
 			// }
 		} catch (HttpException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		} catch (DocumentException e) {
 			e.printStackTrace();
 		} finally {
 			post.releaseConnection();
 		}
 
 		// getLog().debug("Post result: " + resultCode);
 
 		if (getErrorMessage) {
 			return new String[] { resultCode + "", responseBody };
 		} else {
 			return new String[] { resultCode + "", "" };
 		}
 	}
 
 	// try {
 	// getLog().info(
 	// listJobsOnServer(hudsonURL + viewFilter + "api/xml"));
 	// getLog().info("====================");
 	// } catch (Exception e) {
 	// e.printStackTrace();
 	// }
 	public String listJobsOnServer(String url) throws Exception {
 		HttpClient client = getHttpClient(username, password);
 
 		HttpMethod method = new GetMethod(url);
 		client.executeMethod(method);
 		checkResult(method.getStatusCode(), method.getURI());
 
 		StringBuilder sb = new StringBuilder("\n");
 
 		// if (verbose) {
 		// getLog().info("Jobs URL: " + url);
 		// }
 		Document dom = new SAXReader().read(method.getResponseBodyAsStream());
 		// scan through the job list and print its status
 		int i = 0;
 		for (Element job : (List<Element>) dom.getRootElement().elements("job")) {
 			if (!job.elementText("name").toString().replaceAll(regexFilter, "")
 					.equals(job.elementText("name").toString())) {
 				i++;
 				sb.append(String.format("\n[%03d] " + "%s (%s)", i,
 						job.elementText("name"), job.elementText("color")));
 			}
 		}
 		return sb.toString();
 	}
 
 	public HttpClient getHttpClient(String username, String password) {
 		HttpClient client = new HttpClient();
 		// establish a connection within 5 seconds
 		client.getHttpConnectionManager().getParams()
 				.setConnectionTimeout(5000);
 
 		if (hudsonURL.indexOf("localhost") >= 0) {
 			/* simpler authentication method, may not work w/ secured Hudson */
 			Credentials creds = new UsernamePasswordCredentials(username,
 					password);
 			if (creds != null) {
 				client.getState().setCredentials(AuthScope.ANY, creds);
 			}
 		} else {
 			GetMethod login = new GetMethod(hudsonURL + "loginEntry");
 			try {
 				client.executeMethod(login);
 			} catch (HttpException e) {
 				e.printStackTrace();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 			try {
 				checkResult(login.getStatusCode(), login.getURI());
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 
 			String location = hudsonURL + "j_security_check";
 			while (true) {
 				PostMethod loginMethod = new PostMethod(location);
 				loginMethod.addParameter("j_username", username);
 				loginMethod.addParameter("j_password", password);
 				loginMethod.addParameter("action", "login");
 				try {
 					client.executeMethod(loginMethod);
 				} catch (HttpException e) {
 					e.printStackTrace();
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 				if (loginMethod.getStatusCode() / 100 == 3) {
 					// Commons HTTP client refuses to handle redirects for POST
 					// so we have to do it manually.
 					location = loginMethod.getResponseHeader("Location")
 							.getValue();
 					continue;
 				}
 				try {
 					checkResult(loginMethod.getStatusCode(),
 							loginMethod.getURI());
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 				break;
 			}
 		}
 		return client;
 	}
 
 	// for a given job name, return its config.xml as a Document
 	public Document getJobConfigXML(String name) throws Exception {
 		return getXML(hudsonURL + "job/" + name + "/config.xml", null);
 	}
 
 	// for a given job name, return its config.xml as a Document
 	public Document getJobConfigXML(String name, HttpClient client)
 			throws Exception {
 		return getXML(hudsonURL + "job/" + name + "/config.xml", client);
 	}
 
 	// for a given URL, return an XML Document
 	public Document getXML(String URL, HttpClient client) throws URIException,
 			IOException {
 		if (client == null) {
 			client = getHttpClient(username, password);
 		}
 		HttpMethod method = new GetMethod(URL);
 		// if (verbose) {
 		// getLog().info("Config: " + URL);
 		// }
 		try {
 			client.executeMethod(method);
 		} catch (HttpException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		checkResult(method.getStatusCode(), method.getURI());
 		Document xml = null;
 		try {
 			xml = new SAXReader().read(method.getResponseBodyAsStream());
 		} catch (DocumentException e) {
 			// e.printStackTrace();
 		}
 		return xml;
 	}
 
 	public String getResponseFromURL(String url) {
 		Log log = getLog();
 		String responseBody = null;
 		HttpClient client = getHttpClient(username, password);
 
 		HttpMethod method = null;
 		method = new GetMethod(url);
 		method.setFollowRedirects(true);
 		try {
 			client.executeMethod(method);
 			responseBody = getResponseBody(method);
 		} catch (HttpException he) {
 			log.error("Http error connecting to '" + url + "'");
 			log.error(he.getMessage());
 			System.exit(-4);
 		} catch (IOException ioe) {
 			log.error("Unable to connect to '" + url + "'");
 			System.exit(-3);
 		} catch (DocumentException e) {
 			e.printStackTrace();
 		}
 		if (verbose) {
 			log.debug("*** Request ***");
 			log.debug("Request Path: " + method.getPath());
 			log.debug("Request Query: " + method.getQueryString());
 			Header[] requestHeaders = method.getRequestHeaders();
 			for (int i = 0; i < requestHeaders.length; i++) {
 				log.debug(requestHeaders[i].toString());
 			}
 			log.debug("*** Response ***");
 			log.debug("Status Line: " + method.getStatusLine());
 			Header[] responseHeaders = method.getResponseHeaders();
 			for (int i = 0; i < responseHeaders.length; i++) {
 				log.debug(responseHeaders[i].toString());
 			}
 			log.debug("*** Response Body ***");
 			log.debug(responseBody);
 		}
 		method.releaseConnection();
 		return responseBody;
 	}
 
 	public String getResponseBody(HttpMethod method) throws DocumentException,
 			IOException {
 		InputStream is = method.getResponseBodyAsStream();
 		Document dom = null;
 		String out = "";
 		if (is.available() > 0) {
 			dom = new SAXReader().read(is);
 			out = dom.asXML();
 		} else {
 			if (verbose) {
 				// 200: OK
 				// 400: Bad Request (job already exists, cannot createItem)
 				if (method.getStatusCode() != 200
 						&& method.getStatusCode() != 400) {
 					getLog().info(
 							"["
 									+ method.getStatusCode()
 									+ "] "
 									+ method.getStatusText()
 									+ " for "
 									+ method.getName()
 									+ " to "
 									+ method.getPath()
 									+ (method.getQueryString() != null ? "?"
 											+ method.getQueryString() : ""));
 				}
 			}
 		}
 		return out;
 	}
 
 	private static String readFileAsString(String filePath)
 			throws java.io.IOException {
 		byte[] buffer = new byte[(int) new File(filePath).length()];
 		FileInputStream f = new FileInputStream(filePath);
 		f.read(buffer);
 		return new String(buffer);
 	}
 
 	public void writeDomToFile(Document dom, File file)
 			throws MojoExecutionException {
 		writeToFile(dom.asXML(), file);
 	}
 
 	public void writeToFile(String string, File file)
 			throws MojoExecutionException {
 		FileWriter w = null;
 		try {
 			w = new FileWriter(file);
 			w.write(string);
 		} catch (IOException e) {
 			throw new MojoExecutionException("Error updating file " + file, e);
 		} finally {
 			if (w != null) {
 				try {
 					w.close();
 				} catch (IOException e) {
 					// ignore
 				}
 			}
 
 		}
 	}
 
 	// private String domToString(Document dom) {
 	// StringBuffer s = new StringBuffer();
 	// Node project = dom.selectSingleNode("/project");
 	// s.append("<project\n");
 	// s.append(nodeAttributesToString(project));
 	// s.append(">");
 	// s.append(childNodesToString(project));
 	// s.append("\n</project>\n");
 	// return s.toString();
 	// }
 	//
 	// public String nodeAttributesToString(Node aNode) {
 	// StringBuffer s = new StringBuffer();
 	//
 	// for (Element el : (List<Element>) aNode.selectNodes("*")) {
 	// s.append("\"" + el.toString() + "\" ");
 	// }
 	// // Node n = el.selectSingleNode(arg0)
 	// // NamedNodeMap nnm = ((org.w3c.dom.Node) aNode).getAttributes();
 	// // for (int j = 0; j < nnm.getLength(); j++) {
 	// // Node n = (Node) nnm.item(j);
 	// // s.append(" " + n.getNodeName() + "=\"" + n.getTextContent() + "\"");
 	// // }
 	// return s.toString();
 	// }
 	//
 	// public String childNodesToString(Node aNode) {
 	// final StringBuffer s = new StringBuffer();
 	// // final NodeList nl = aNode.getChildNodes();
 	// // for (int j = 0; j < nl.getLength(); j++) {
 	// // Node n = nl.item(j);
 	// // if (n.getNodeName() != "#text") {
 	// // s.append("<" + n.getNodeName() + nodeAttributesToString(n)
 	// // + ">");
 	// // if (n.hasChildNodes()) {
 	// // s.append(childNodesToString(n));
 	// // } else {
 	// // s.append(n.getTextContent());
 	// // }
 	// // s.append("</" + n.getNodeName() + ">");
 	// // } else {
 	// // if (!n.getTextContent().replaceAll("[\n\r ]+", "").equals("")) {
 	// // s.append(n.getTextContent());
 	// // }
 	// // }
 	// // }
 	// return s.toString();
 	// }
 
 	private static void checkResult(int i, URI uri) throws IOException {
 		if (i / 100 != 2) {
 			// System.out.println("[WARN] Got result: " + i + " for "+
 			// uri.toString());
 			throw new IOException("Got result: " + i + " for " + uri.toString());
 		}
 	}
 
 	public File createTempDir(String prefix) throws IOException {
 		File directory = File.createTempFile(prefix, "");
 		if (directory.delete()) {
 			directory.mkdirs();
 			return directory;
 		} else {
 			throw new IOException("Could not create temp directory at: "
 					+ directory.getAbsolutePath());
 		}
 	}
 
 }
