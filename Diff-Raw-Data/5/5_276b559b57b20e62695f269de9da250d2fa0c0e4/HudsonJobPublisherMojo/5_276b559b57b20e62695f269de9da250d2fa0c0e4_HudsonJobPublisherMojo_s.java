 package org.jboss.maven.plugins.util;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.List;
 import java.util.Properties;
 
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
 public class HudsonJobPublisherMojo extends AbstractMojo {
 
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
 	 * @parameter expression="${replaceExistingJob}" default-value="true"
 	 */
 	private boolean replaceExistingJob = true;
 
 	public boolean isReplaceExistingJob() {
 		return replaceExistingJob;
 	}
 
 	public void setReplaceExistingJob(boolean replaceExistingJob) {
 		this.replaceExistingJob = replaceExistingJob;
 	}
 
 	/**
 	 * @parameter expression="${jobTemplateFile}" default-value="config.xml"
 	 */
 	private String jobTemplateFile = "config.xml";
 
 	public String getJobTemplateFile() {
 		return jobTemplateFile;
 	}
 
 	public void setJobTemplateFile(String jobTemplateFile) {
 		this.jobTemplateFile = jobTemplateFile;
 	}
 
 	/**
 	 * @parameter expression="${buildURL}"
 	 *            default-value="http://svn.jboss.org/repos/jbosstools/trunk/build"
 	 */
 	private String buildURL = "http://svn.jboss.org/repos/jbosstools/trunk/build";
 
 	public String getBuildURL() {
 		return buildURL;
 	}
 
 	public void setBuildURL(String buildURL) {
 		this.buildURL = buildURL;
 	}
 
 	/**
 	 * @parameter expression="${branchOrTag}"
 	 *            default-value="branches/someBranch"
 	 */
 	// branchOrTag is the one used when copying a job and sources' SVN URLs
 	private String branchOrTag = "branches/someBranch";
 
 	public String getBranchOrTag() {
 		return branchOrTag;
 	}
 
 	public void setBranchOrTag(String branchOrTag) {
 		this.branchOrTag = branchOrTag;
 	}
 
 	/**
 	 * @parameter expression="${properties}"
 	 */
 	private Properties jobProperties = new Properties();
 
 	public Properties getJobProperties() {
 		return jobProperties;
 	}
 
 	public void setJobProperties(Properties jobProperties) {
 		this.jobProperties = jobProperties;
 	}
 
 	/**
 	 * @parameter expression="${components}" default-value=""
 	 */
 	private String components = "";
 
 	public String getComponents() {
 		return components;
 	}
 
 	public void setComponents(String components) {
 		this.components = components;
 	}
 
 	/**
 	 * @parameter expression="${componentJobNameSuffix}" default-value=""
 	 */
 	private String componentJobNameSuffix = "";
 
 	public String getComponentJobNameSuffix() {
 		return componentJobNameSuffix;
 	}
 
 	public void setComponentJobNameSuffix(String componentJobNameSuffix) {
 		this.componentJobNameSuffix = componentJobNameSuffix;
 	}
 
 	/**
 	 * @parameter expression="${componentJobNameSuffix2}" default-value=""
 	 */
 	// replacement for componentJobNameSuffix when copying jobs
 	private String componentJobNameSuffix2 = "";
 
 	public String getComponentJobNameSuffix2() {
 		return componentJobNameSuffix2;
 	}
 
 	public void setComponentJobNameSuffix2(String componentJobNameSuffix2) {
 		this.componentJobNameSuffix2 = componentJobNameSuffix2;
 	}
 
 	/**
 	 * @parameter expression="${componentJobNamePrefix}"
 	 *            default-value="jbosstools-"
 	 */
 	// replacement for componentJobNameSuffix when copying jobs
 	private String componentJobNamePrefix = "jbosstools-";
 
 	public String getJbosstoolsJobnamePrefix() {
 		return componentJobNamePrefix;
 	}
 
 	public void setJbosstoolsJobnamePrefix(String componentJobNamePrefix) {
 		this.componentJobNamePrefix = componentJobNamePrefix;
 	}
 
 	/**
 	 * filter the server to show only one view's worth of jobs (not the whole
 	 * server list)
 	 * 
 	 * @parameter expression="${componentJobNamePrefix}" default-value=""
 	 */
 	// replacement for componentJobNameSuffix when copying jobs
 	private String viewPath = "view/DevStudio_Trunk/"; // "view/DevStudio_Stable_Branch/";
 
 	public String getViewPath() {
 		return viewPath;
 	}
 
 	public void setViewPath(String viewPath) {
 		this.viewPath = viewPath;
 	}
 
 	private static final String JOB_ALREADY_EXISTS = "A job already exists with the name ";
 	private static final String JOB_NAME = "Job Name: ";
 	private static final String JOBNAME_PATTERN = "jbosstools-.+_trunk.*|devstudio-.+_trunk.*|jbosstools-.+_stable_branch.*|devstudio-.+_stable_branch.*";
 
 	public static String getJobName() {
 		return JOB_NAME;
 	}
 
 	public void execute() throws MojoExecutionException {
 		Log log = getLog();
 
 		// run against a local or remote URL
 		setHudsonURL(hudsonURL);
 		if (verbose) {
 			log.info("Hudson URL: " + hudsonURL);
 		}
 
 		String xmlFile = jobTemplateFile; // "target/config.xml";
 
 		// merge components and properties into a single Properties list
 		loadComponentsIntoJobList();
 		// work on those Properties - create or update jobs as needed
 		createJobsFromJobList(xmlFile);
 
 		// for any jobs ending with componentJobNameSuffix, copy them and edit
 		// them using componentJobNameSuffix2 as new name suffix
 		// then update both sourcesURL and buildURL by replacing trunk w/
 		// branchOrTag
 		copyJobs();
 
 		if (verbose) {
 			try {
 				log.info(listJobsOnServer(hudsonURL + "api/xml",
 						JOBNAME_PATTERN));
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	// update both sourcesURL and buildURL in jobs by replacing trunk w/
 	// branchOrTag
 	public void copyJobs() throws MojoExecutionException {
 		// for all jobs, copy them if componentJobNameSuffix2 is set
 		if (componentJobNameSuffix2 != null
 				&& !componentJobNameSuffix2.equals("")) {
 			String[] jobNames = null;
 			try {
 				jobNames = getJobNames(viewPath);
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 			Enumeration jobNamesEnum = jobProperties.propertyNames();
 			while (jobNamesEnum.hasMoreElements()) {
 				String jobName = (String) jobNamesEnum.nextElement();
 				String sourcesURL = jobProperties.getProperty(jobName);
 				// getLog().debug(jobName + " == " + sourcesURL);
 			}
 			if (jobNames != null & jobNames.length > 0) {
 				for (int i = 0; i < jobNames.length; i++) {
 					String fromJobName = jobNames[i];
 					// getLog().debug("fromJobName = " + fromJobName +
 					// ", componentJobNameSuffix = " + componentJobNameSuffix +
 					// ", jobProperties.containsKey = " +
 					// jobProperties.containsKey(fromJobName));
 					if (fromJobName.indexOf(componentJobNameSuffix) > 0
 							&& jobProperties.containsKey(fromJobName)) {
 						// check if target job name exists
 						String newJobName = jobNames[i]
 								.replaceAll(componentJobNameSuffix,
 										componentJobNameSuffix2);
 
 						File tempDir = null;
 						try {
 							tempDir = createTempDir(getClass().getSimpleName());
 						} catch (IOException e) {
 							e.printStackTrace();
 						}
 
 						Document configXML = null;
 						File configXMLFile = null;
 						// if the new job already exists
 						if (jobExists(newJobName)) {
 							// get new job's config.xml as a Document
 							try {
 								configXML = getJobConfigXML(newJobName);
 							} catch (Exception e) {
 								e.printStackTrace();
 							}
 						} else {
 							// get old config.xml as a Document
 							try {
 								configXML = getJobConfigXML(fromJobName);
 							} catch (Exception e) {
 								e.printStackTrace();
 							}
 
 							if (configXML != null) {
 								// write to temp file
 								if (configXMLFile == null) {
 									configXMLFile = new File(tempDir,
 											"config.xml");
 								}
 								writeDomToFile(configXML, configXMLFile);
 
 								// create a new job
 								createJob(configXMLFile, newJobName, true);
 							}
 						}
 
 						if (configXML != null) {
 							getLog().info(
 									"Copy "
 											+ fromJobName
 											+ " to "
 											+ newJobName
 											+ " and replace config.xml values...");
 							configXML = replaceSCMURLsInConfigXML(configXML);
 							configXML = replaceDescriptionInConfigXML(configXML);
 							configXML = replaceChildProjectsInConfigXML(configXML);
 
 							// write to temp file
 							if (configXMLFile == null) {
 								configXMLFile = new File(tempDir, "config.xml");
 							}
 							writeDomToFile(configXML, configXMLFile);
 
 							// and post it back to the server
 							postXML(configXMLFile, null, hudsonURL + "job/"
 									+ newJobName + "/config.xml", true);
 
 						} else {
 							throw new MojoExecutionException(
 									"Error: config.xml for " + newJobName
 											+ " could not be loaded!");
 						}
 						// delete temp file
 						try {
 							FileUtils.deleteDirectory(tempDir);
 						} catch (IOException e) {
 							e.printStackTrace();
 						}
 					}
 				}
 			}
 		}
 	}
 
 	private Document replaceChildProjectsInConfigXML(Document configXML) {
 		if (configXML
 				.selectSingleNode("/project/publishers/hudson.tasks.BuildTrigger/childProjects") != null) {
 			// getLog().info(
 			// "/project/publishers/hudson.tasks.BuildTrigger/childProjects: "
 			// + configXML
 			// .selectSingleNode(
 			// "/project/publishers/hudson.tasks.BuildTrigger/childProjects")
 			// .getText());
 			if (configXML
 					.selectSingleNode(
 							"/project/publishers/hudson.tasks.BuildTrigger/childProjects")
 					.getText().indexOf("_trunk") >= 0) {
 				configXML
 						.selectSingleNode(
 								"/project/publishers/hudson.tasks.BuildTrigger/childProjects")
 						.setText(
 								configXML
 										.selectSingleNode(
 												"/project/publishers/hudson.tasks.BuildTrigger/childProjects")
 										.getText()
 										.replaceAll("_trunk", "_stable_branch"));
 			}
 		}
 		return configXML;
 	}
 
 	private Document replaceDescriptionInConfigXML(Document configXML) {
 		// getLog().info(
 		// "/project/description: "
 		// + configXML.selectSingleNode("/project/description")
 		// .getText());
 		if (configXML
 				.selectSingleNode("/project/description")
 				.getText()
 				.indexOf(
 						"<a style=\"color:#FF9933\" href=\"http://download.jboss.org/jbosstools/builds/cascade/trunk.html\">") >= 0) {
 			// replace with stable colour and link
 			configXML.selectSingleNode("/project/description").setText(
 					configXML.selectSingleNode("/project/description")
 							.getText()
 							.replaceAll("color:#FF9933", "color:green")
 							.replaceAll("cascade/trunk.html", "cascade/"));
 		}
 		return configXML;
 	}
 
 	// edit the configXML document - replace the scm paths -
 	// look for /trunk/ and replace with the branch or tag value, for matching
 	// job names
 	public Document replaceSCMURLsInConfigXML(Document configXML) {
 		// getLog().info(
 		// "/project/scm/locations/hudson.scm.SubversionSCM_-ModuleLocation[1]/remote: "
 		// + configXML
 		// .selectSingleNode(
 		// "/project/scm/locations/hudson.scm.SubversionSCM_-ModuleLocation[1]/remote")
 		// .getText());
 		configXML
 				.selectSingleNode(
 						"/project/scm/locations/hudson.scm.SubversionSCM_-ModuleLocation[1]/remote")
 				.setText(
 						configXML
 								.selectSingleNode(
 										"/project/scm/locations/hudson.scm.SubversionSCM_-ModuleLocation[1]/remote")
 								.getText()
 								.replaceAll("/trunk/",
 										"/" + getBranchOrTag() + "/")); // sourcesURL
 		// getLog().info(
 		// "/project/scm/locations/hudson.scm.SubversionSCM_-ModuleLocation[2]/remote: "
 		// + configXML
 		// .selectSingleNode(
 		// "/project/scm/locations/hudson.scm.SubversionSCM_-ModuleLocation[2]/remote")
 		// .getText());
 		configXML
 				.selectSingleNode(
 						"/project/scm/locations/hudson.scm.SubversionSCM_-ModuleLocation[2]/remote")
 				.setText(
 						configXML
 								.selectSingleNode(
 										"/project/scm/locations/hudson.scm.SubversionSCM_-ModuleLocation[2]/remote")
 								.getText()
 								.replaceAll("/trunk/",
 										"/" + getBranchOrTag() + "/")); // buildURL
 		return configXML;
 	}
 
 	public void createJobsFromJobList(String xmlFile)
 			throws MojoExecutionException {
 		// load jobName::sourcesURL mapping from jobProperties
 		Enumeration jobNamesEnum = jobProperties.propertyNames();
 		while (jobNamesEnum.hasMoreElements()) {
 			String jobName = (String) jobNamesEnum.nextElement();
 			String sourcesURL = jobProperties.getProperty(jobName);
 
 			// TODO: when copying a job, do more than just simple update here
 			updateConfigXML(sourcesURL, jobTemplateFile, xmlFile);
 
 			// delete existing job
 			if (replaceExistingJob) {
 				deleteJob(xmlFile, jobName, false);
 			}
 			createOrUpdateJob(xmlFile, jobName);
 
 		}
 	}
 
 	// given a set of defined <components></components>, create new
 	// jobProperties name/value pairs
 	public void loadComponentsIntoJobList() {
 		if (components != null && !components.isEmpty()) {
			String[] componentArray = components.split("[, ]+");
 			// getLog().debug(componentArray.length + " : " +
 			// componentArray);
 			for (int i = 0; i < componentArray.length; i++) {
 				// add new jobName to sourcesURL mappings
 				// getLog().debug(componentArray[i] + ", " +
 				// componentJobNameSuffix + ", " +
 				// buildURL.replaceAll("/build/*$", "/"));
 
 				// if the prefix (jbosstools-3.2_trunk.component--)
 				// contains the suffix (_trunk), don't suffix again
 				if (getJbosstoolsJobnamePrefix()
 						.indexOf(componentJobNameSuffix) > 0) {
 					jobProperties.put(getJbosstoolsJobnamePrefix()
 							+ componentArray[i],
 							buildURL.replaceAll("/build/*$", "/")
 									+ componentArray[i]);
 				} else {
 					jobProperties.put(getJbosstoolsJobnamePrefix()
 							+ componentArray[i] + componentJobNameSuffix,
 							buildURL.replaceAll("/build/*$", "/")
 									+ componentArray[i]);
 				}
 			}
 		}
 	}
 
 	public void createOrUpdateJob(String xmlFile, String jobName)
 			throws MojoExecutionException {
 		String[] result = createJob(xmlFile, jobName, !replaceExistingJob);
 		// getLog().debug(result[0] + "\n" + result[1]);
 		if (Integer.parseInt(result[0].trim()) == 400) {
 			String error = result[1];
 			if (error.indexOf(">" + JOB_ALREADY_EXISTS + "'" + jobName + "'<") != 0) {
 				if (replaceExistingJob) {
 					updateJob(xmlFile, jobName, false);
 				} else {
 					getLog().info(
 							JOB_ALREADY_EXISTS
 									+ "'"
 									+ jobName
 									+ "'. Set replaceExistingJob = true to overwrite existing jobs.");
 					// throw new MojoExecutionException(error);
 				}
 			}
 		}
 	}
 
 	public void updateConfigXML(String sourcesURL, String xmlTemplate,
 			String xmlFile) {
 		// replace params above into XML template
 		Document dom = null;
 		try {
 			dom = new SAXReader().read(new File(xmlTemplate));
 			dom.selectSingleNode(
 					"/project/scm/locations/hudson.scm.SubversionSCM_-ModuleLocation[1]/remote")
 					.setText(sourcesURL);
 			dom.selectSingleNode(
 					"/project/scm/locations/hudson.scm.SubversionSCM_-ModuleLocation[2]/remote")
 					.setText(buildURL);
 			// TODO: add destination folder option to publish.sh
 		} catch (DocumentException e) {
 			getLog().error("Problem reading XML from " + xmlTemplate);
 			e.printStackTrace();
 		}
 		writeDomToFile(dom, xmlFile);
 		// getLog().debug(dom.asXML());
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
 
 	// submit to
 	// http://hudson.qa.jboss.com/hudson/createItem?name=NEWJOBNAME&mode=copy&from=FROMJOBNAME
 	// this works but ends up losing XML in the resulting config.xml file
 	public String[] copyJob(String xmlContents, String fromJobName,
 			String newJobName, boolean getErrorMessage)
 			throws MojoExecutionException {
 		if (verbose)
 			getLog().info("Copy job " + fromJobName + " to " + newJobName);
 		String[] result = postXML(getJobTemplateFile(), null, hudsonURL
 				+ "createItem?name=" + newJobName + "&mode=copy&from="
 				+ fromJobName, getErrorMessage);
 		return result;
 	}
 
 	// submit to
 	// http://hudson.qa.jboss.com/hudson/createItem?name=NEWJOBNAME&mode=copy&from=FROMJOBNAME
 	// this works but ends up losing XML in the resulting config.xml file
 	public String[] copyJobConfigXML(String xmlContents, String fromJobName,
 			String newJobName, boolean getErrorMessage) throws IOException,
 			MojoExecutionException {
 		if (verbose)
 			getLog().info("Copy job " + fromJobName + " to " + newJobName);
 
 		// get source config.xml
 		Document configXML = null;
 		try {
 			configXML = getJobConfigXML(fromJobName);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 		File tempDir = createTempDir(getClass().getSimpleName());
 		File configXMLFile = new File(tempDir, "config.xml");
 		// write to temp file
 		if (configXML != null) {
 			writeDomToFile(configXML, configXMLFile);
 		}
 
 		// post to new job
 		String[] result = postXML(configXMLFile, null, hudsonURL
 				+ "createItem?name=" + newJobName + "&mode=copy&from="
 				+ fromJobName, getErrorMessage);
 
 		// delete temp file
 		FileUtils.deleteDirectory(tempDir);
 
 		return result;
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
 		Log log = getLog();
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
				log.equals("File not found: " + xmlFile);
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
 			// log.info("Response status code: " + resultCode);
 			// log.info("Response body: ");
 			// log.info(resultString);
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
 
 	public boolean jobExists(String jobName) {
 		String[] s = null;
 		try {
 			s = getJobNames(viewPath);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		if (s != null && s.length > 0) {
 			for (int i = 0; i < s.length; i++) {
 				if (s[i].equals(jobName)) {
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 
 	public String[] getJobNames() throws Exception {
 		return getJobNames("");
 	}
 
 	// note: URLSuffix is ignored when accessing a localhost Hudson instance, as
 	// we assume we're just testing and the view may not exist
 	public String[] getJobNames(String URLSuffix) throws Exception {
 		Log log = getLog();
 		HttpClient client = getHttpClient(username, password);
 
 		HttpMethod method = new GetMethod(hudsonURL
 				+ (hudsonURL.indexOf("localhost") >= 0 ? "" : URLSuffix)
 				+ "api/xml");
 		client.executeMethod(method);
 		checkResult(method.getStatusCode(), method.getURI());
 
 		ArrayList<String> jobNames = new ArrayList<String>();
 
 		Document dom = new SAXReader().read(method.getResponseBodyAsStream());
 		// scan through the job list and print its status
 		for (Element job : (List<Element>) dom.getRootElement().elements("job")) {
 			jobNames.add(job.elementText("name").toString());
 		}
 		return jobNames.toArray(new String[jobNames.size()]);
 	}
 
 	// dom.selectSingleNode("/project/scm/locations/hudson.scm.SubversionSCM_-ModuleLocation[1]/remote")
 	public String listJobsOnServer(String url, String pattern) throws Exception {
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
 			if (!job.elementText("name").toString().replaceAll(pattern, "")
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
