 /*******************************************************************************
  * Copyright (c) 2008, 2010 VMware Inc.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *   VMware Inc. - initial contribution
  *******************************************************************************/
 
 package org.eclipse.virgo.server.svt.hostedrepo;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.fail;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.lang.management.ManagementFactory;
 import java.lang.management.OperatingSystemMXBean;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.management.MBeanServerConnection;
 import javax.management.MalformedObjectNameException;
 import javax.management.ObjectName;
 import javax.management.openmbean.CompositeData;
 import javax.management.remote.JMXConnector;
 import javax.management.remote.JMXConnectorFactory;
 import javax.management.remote.JMXServiceURL;
 
 import org.eclipse.virgo.server.svt.AbstractWebTests;
 import org.eclipse.virgo.server.svt.UrlWaitLatch;
 import org.eclipse.virgo.util.io.FileCopyUtils;
 import org.eclipse.virgo.util.io.FileSystemUtils;
 import org.junit.AfterClass;
 import org.junit.BeforeClass;
 import org.junit.Ignore;
 import org.junit.Test;
 
 import com.gargoylesoftware.htmlunit.ElementNotFoundException;
 
 public class RemoteHostedRepositoryTests extends AbstractWebTests {
 
 	private static String serverHomeDir = null;
 	private static String usrDir = null;
 	private static String binDir = null;
 	private static String configDir = null;
 
 	protected static final String SPLASHURL = "http://localhost:8081/";
 	protected static final String ADMINURL = "http://localhost:8081/admin";
 	
 	private static final String APP_URI_SHAREDSERVICES_WAR1 = "./../org.eclipse.virgo.server.svt/apps/hello_war_shared_services.war";
 	private static final String APP_URI_PAR = "./../org.eclipse.virgo.server.svt/apps/greenpages-solution-2.0.1.SNAPSHOT.par";
 	private static final String APP_URI_UNSCOPED_PLAN = "./../org.eclipse.virgo.server.svt/apps/helloworld_unscoped-1.0.0.RELEASE.plan";
 	private static final String APP_URI_PLAN1 = "./../test-apps/scopedplan.plan";
 	private static final String APP_URI_PLAN2 = "src/test/resources/parreferencedplan.plan";
 
 	private static final String resourcesDir = "./../org.eclipse.virgo.server.svt/bundles";
 	private static final String downloadDir = "target/bundles";
 
 	private static String[] bundleNames = new String[] {
			// "com.springsource.org.antlr-3.0.1.jar", deleted from git as not covered by a full CQ
 			"com.springsource.javassist-3.9.0.GA.jar",
 			"com.springsource.net.sf.cglib-2.2.0.jar",
 			"com.springsource.org.apache.commons.collections-3.2.1.jar",
 			"com.springsource.org.apache.commons.dbcp-1.2.2.osgi.jar",
 			"com.springsource.org.apache.commons.logging-1.1.1.jar",
 			"com.springsource.org.apache.commons.pool-1.3.0.jar",
 			"com.springsource.org.dom4j-1.6.1.jar",
 			"com.springsource.org.eclipse.persistence-1.0.0.jar",
 			"com.springsource.org.eclipse.persistence.antlr-1.0.0.jar",
 			"com.springsource.org.eclipse.persistence.asm-1.0.0.jar",
 			"com.springsource.org.eclipse.persistence.jpa-1.0.0.jar",
 			"com.springsource.org.hsqldb-1.8.0.9.jar",
 			"com.springsource.org.objectweb.asm-1.5.3.jar",
 			"com.springsource.org.objectweb.asm.attrs-1.5.3.jar" };
 	
 	private static String[] downloadBundleNames = new String[] {
             "com.springsource.org.hibernate-3.2.6.ga.jar" };
 
 	private static Process process = null;
 	private static ProcessBuilder pb = null;
 	private static File startup = null;
 	private static File startupURI = null;
 	private static File shutdownURI = null;
 	private static String startupFileName = null;
 	private static File shutdown = null;
 	private static String shutdownFileName = null;
 	private static OperatingSystemMXBean os = ManagementFactory
 			.getOperatingSystemMXBean();
 	private static MBeanServerConnection connection = null;
 	private static final String JMXURL = "service:jmx:rmi:///jndi/rmi://localhost:3333/jmxrmi";
 	private static final String KEYSTORE = "/config/keystore";
 	private static final String KEYPASSWORD = "changeit";
 	public static final long HALF_SECOND = 500;
 	public static final long TWO_MINUTES = 120 * 1000;
 
 	private static String artifactType1 = null;
 	private static String artifactName1 = null;
 	private static String artifactVersion1 = null;
 	
 	private static String artifactType3 = null;
 	private static String artifactName3 = null;
 	private static String artifactVersion3 = null;
 	
 	private static String artifactType4 = null;
 	private static String artifactName4 = null;
 	private static String artifactVersion4 = null;
 	
 	private static String artifactType5 = null;
 	private static String artifactName5 = null;
 	private static String artifactVersion5 = null;
 
 	
 	private static String artifactType7 = null;
 	private static String artifactName7 = null;
 	private static String artifactVersion7 = null;
 	
 	private static String artifactType8 = null;
 	private static String artifactName8 = null;
 	private static String artifactVersion8 = null;
 	
 	private static String[] signature = null;
 	private static Object[] params = null;
 
 	@BeforeClass
 	public static void testSetUp() throws IOException, Exception {
 		startServer();
 		FileSystemUtils.deleteRecursively(new File(
 				getConfiguredServerConfigDir(),
 				"org.eclipse.virgo.apps.repository.properties"));
 		FileSystemUtils.deleteRecursively(new File(
 				getConfiguredServerRepositoryUsrDir()));
 		new File(getConfiguredServerRepositoryUsrDir()).mkdir();
 		FileCopyUtils.copy(new File(
 				"src/test/resources/org.eclipse.virgo.repository.properties"),
 				new File(getConfiguredServerConfigDir(),
 						"org.eclipse.virgo.repository.properties"));
 		FileCopyUtils.copy(new File("src/test/resources/tomcat-server.xml"),
 				new File(getConfiguredServerConfigDir(), "tomcat-server.xml"));
 		FileCopyUtils.copy(new File(
 				"src/test/resources/org.eclipse.virgo.kernel3.properties"),
 				new File(getConfiguredServerConfigDir(),
 						"org.eclipse.virgo.kernel.properties"));
 		new File(getConfiguredServerConfigDir(),
 				"org.eclipse.virgo.kernel3.properties").renameTo(new File(
 				getConfiguredServerConfigDir(),
 				"org.eclipse.virgo.kernel.properties"));
 		copyDependentBundlesToRepository(getConfiguredServerRepositoryUsrDir());
 		new Thread(new StartUpThread()).start();
 		UrlWaitLatch.waitFor(SPLASHURL);
 	}
 
 	private static String getConfiguredServerHomeDir() throws Exception {
 		if (serverHomeDir == null) {
 			File testExpanded = new File("src/test/resources");
 			for (File candidate : testExpanded.listFiles()) {
 				if (candidate.isDirectory()
 						&& candidate.getName().equals("virgo-web-server")) {
 					serverHomeDir = new File(candidate.getCanonicalPath())
 							.getCanonicalPath();
 					break;
 				}
 			}
 		}
 		return serverHomeDir;
 	}
 
 	private static String getConfiguredServerConfigDir() throws Exception {
 		if (configDir == null) {
 			File serverHomeDir = new File(getConfiguredServerHomeDir());
 			if (serverHomeDir.isDirectory()) {
 				configDir = new File(serverHomeDir, "config")
 						.getCanonicalPath();
 			}
 		}
 		return configDir;
 	}
 
 	private static String getConfiguredServerBinDir() throws Exception {
 		if (binDir == null) {
 			File serverHomeDir = new File(getConfiguredServerHomeDir());
 			if (serverHomeDir.isDirectory()) {
 				binDir = new File(serverHomeDir, "bin").getCanonicalPath();
 			}
 		}
 		return binDir;
 	}
 
 	public static void copyFile(File source, File dest) throws IOException {
 		if (!dest.exists()) {
 			dest.createNewFile();
 		}
 		InputStream in = null;
 		OutputStream out = null;
 		try {
 			in = new FileInputStream(source);
 			out = new FileOutputStream(dest);
 			byte[] buf = new byte[1024];
 			int len;
 			while ((len = in.read(buf)) > 0) {
 				out.write(buf, 0, len);
 			}
 		}
 
 		finally {
 			in.close();
 			out.close();
 		}
 	}
 
 	protected static String getConfiguredServerRepositoryUsrDir()
 			throws Exception {
 		if (usrDir == null) {
 			File serverHomeDir = new File(getConfiguredServerHomeDir());
 			if (serverHomeDir.isDirectory()) {
 				File repositoryDir = new File(serverHomeDir, "repository")
 						.getCanonicalFile();
 				usrDir = new File(repositoryDir, "usr").getCanonicalPath();
 			}
 		}
 
 		return usrDir;
 	}
 
 	protected static void copyDependentBundlesToRepository(String bundlesUsrDir)
 			throws Exception {
 
 		for (String bundleName : bundleNames) {
 
 			FileCopyUtils.copy(new File(resourcesDir, bundleName), new File(
 					bundlesUsrDir, bundleName));
 		}
 		
 		for (String bundleName : downloadBundleNames) {
 
             FileCopyUtils.copy(new File(downloadDir, bundleName), new File(
                     bundlesUsrDir, bundleName));
         }
 	}
 
 
 	@Test
 	public void testPlanArtifactDeploy() throws Exception {
 		signature = new String[] { String.class.getName() };
 		params = new Object[] { new File(APP_URI_PLAN1).toURI().toString() };
 		CompositeData compositeData = deploy(signature, params);
 		artifactName1 = compositeData.get("symbolicName").toString();
 		artifactType1 = compositeData.get("type").toString();
 		artifactVersion1 = compositeData.get("version").toString();
 
 		waitForMBeanRegister(artifactType1, artifactName1, artifactVersion1,
 				HALF_SECOND, TWO_MINUTES);
 		assertArtifactExists(artifactType1, artifactName1, artifactVersion1);
 		waitForArtifactState("plan", "scoped.plan", "1.0.0", "ACTIVE",
 				HALF_SECOND, TWO_MINUTES);
 		ObjectName[] objectNameList = (ObjectName[]) getMBeanServerConnection()
 				.getAttribute(
 						getObjectName(artifactType1, artifactName1,
 								artifactVersion1), "Dependents");
 		for (ObjectName objectName : objectNameList) {
 			assertPlanReferencedArtifacts(objectName);
 		}
 		assertArtifactState(artifactType1, artifactName1, artifactVersion1,
 				"ACTIVE");
 	}
 
 	
 	@Test
 	public void testPlanArtifactUndeploy() throws Exception {
 		signature = new String[] { String.class.getName(),
 				String.class.getName() };
 		params = new Object[] { artifactName1, artifactVersion1 };
 		getMBeanServerConnection()
 				.invoke(
 						new ObjectName(
 								"org.eclipse.virgo.kernel:category=Control,type=Deployer"),
 						"undeploy", params, signature);
 		waitForMBeanDeRegister(artifactType1, artifactName1, artifactVersion1,
 				HALF_SECOND, TWO_MINUTES);
 		assertArtifactNotExists(artifactType1, artifactName1, artifactVersion1);
 	}
 	
 	@Ignore
 	@Test
 	public void planDeployReferencesParArtifactFromRemoteRepository() throws Exception {
 		signature = new String[] { String.class.getName() };
 		params = new Object[] { new File(APP_URI_PLAN2).toURI().toString() };
 
 		CompositeData compositeData = deploy(signature, params);
 		artifactName8 = compositeData.get("symbolicName").toString();
 		artifactType8 = compositeData.get("type").toString();
 		artifactVersion8 = compositeData.get("version").toString();
 
 		waitForMBeanRegister(artifactType8, artifactName8, artifactVersion8, HALF_SECOND, TWO_MINUTES);
 		assertArtifactExists(artifactType8, artifactName8, artifactVersion8);
 		assertArtifactState(artifactType8, artifactName8, artifactVersion8, "ACTIVE");
 	}
 	
 	@Ignore
 	@Test
 	public void planUnDeployReferencesParArtifactFromRemoteRepository() throws Exception {
 		signature = new String[] { String.class.getName(),
 				String.class.getName() };
 		params = new Object[] { artifactName8, artifactVersion8 };
 		getMBeanServerConnection()
 				.invoke(
 						new ObjectName(
 								"org.eclipse.virgo.kernel:category=Control,type=Deployer"),
 						"undeploy", params, signature);
 		waitForMBeanDeRegister(artifactType8, artifactName8, artifactVersion8, HALF_SECOND, TWO_MINUTES);
 		assertArtifactNotExists(artifactType8, artifactName8, artifactVersion8);
 	}
 
 	
 
 	@Test
 	public void testUnscopedPlanDeployThatDependsUponArtifactsInRemoteHostedRepo()
 			throws IOException, Exception {
 		signature = new String[] { String.class.getName() };
 		params = new Object[] { new File(APP_URI_UNSCOPED_PLAN).toURI().toString() };
 		CompositeData compositeData = deploy(signature, params);
 		artifactName3 = compositeData.get("symbolicName").toString();
 		artifactType3 = compositeData.get("type").toString();
 		artifactVersion3 = compositeData.get("version").toString();
 
 		waitForMBeanRegister(artifactType3, artifactName3, artifactVersion3,
 				HALF_SECOND, TWO_MINUTES);
 		assertArtifactExists(artifactType3, artifactName3, artifactVersion3);
 		assertArtifactState(artifactType3, artifactName3, artifactVersion3,
 				"ACTIVE");
 	}
 
 	@Test
 	public void testUnscopedPlanUnDeployThatDependsUponArtifactsInRemoteHostedRepo()
 			throws IOException, Exception {
 		signature = new String[] { String.class.getName(),
 				String.class.getName() };
 		params = new Object[] { artifactName3, artifactVersion3 };
 		getMBeanServerConnection()
 				.invoke(
 						new ObjectName(
 								"org.eclipse.virgo.kernel:category=Control,type=Deployer"),
 						"undeploy", params, signature);
 		waitForMBeanDeRegister(artifactType3, artifactName3, artifactVersion3,
 				HALF_SECOND, TWO_MINUTES);
 		assertArtifactNotExists(artifactType3, artifactName3, artifactVersion3);
 	}
 
 	@Test
 	public void testJmxDeploymentOfUnscopedPlanThatDependsUponArtifactsInRemoteHostedRepo()
 			throws IOException, Exception {
 		signature = new String[] { String.class.getName() };
 		params = new Object[] { new File(APP_URI_UNSCOPED_PLAN).toURI().toString() };
 		CompositeData compositeData = deploy(signature, params);
 		artifactName4 = compositeData.get("symbolicName").toString();
 		artifactType4 = compositeData.get("type").toString();
 		artifactVersion4 = compositeData.get("version").toString();
 
 		waitForMBeanRegister(artifactType4, artifactName4, artifactVersion4,
 				HALF_SECOND, TWO_MINUTES);
 		assertArtifactExists(artifactType4, artifactName4, artifactVersion4);
 		assertArtifactState(artifactType4, artifactName4, artifactVersion4,
 				"ACTIVE");
 	}
 
 	@Test
 	public void testDeploymentOfSharedServicesWarDependsOnArtfiactsInUnscopedPlan()
 			throws IOException, Exception {
 		signature = new String[] { String.class.getName() };
 		params = new Object[] { new File(APP_URI_SHAREDSERVICES_WAR1).toURI()
 				.toString() };
 		CompositeData compositeData = deploy(signature, params);
 		artifactName5 = compositeData.get("symbolicName").toString();
 		artifactType5 = compositeData.get("type").toString();
 		artifactVersion5 = compositeData.get("version").toString();
 
 		waitForMBeanRegister(artifactType5, artifactName5, artifactVersion5,
 				HALF_SECOND, TWO_MINUTES);
 		UrlWaitLatch
 				.waitFor("http://localhost:8081/hello_war_shared_services/");
 		assertArtifactExists(artifactType5, artifactName5, artifactVersion5);
 		assertArtifactState(artifactType5, artifactName5, artifactVersion5,
 				"ACTIVE");
 	}
 
 	@Test
 	public void testUnDeploymentOfSharedServicesWarDependsOnArtfiactsInUnscopedPlan()
 			throws ElementNotFoundException, Exception {
 		signature = new String[] { String.class.getName(),
 				String.class.getName() };
 		params = new Object[] { artifactName5, artifactVersion5 };
 		getMBeanServerConnection()
 				.invoke(
 						new ObjectName(
 								"org.eclipse.virgo.kernel:category=Control,type=Deployer"),
 						"undeploy", params, signature);
 		waitForMBeanDeRegister(artifactType5, artifactName5, artifactVersion5,
 				HALF_SECOND, TWO_MINUTES);
 		UrlWaitLatch
 				.waitForNotExistence("http://localhost:8081/hello_war_shared_services");
 		assertArtifactNotExists(artifactType5, artifactName5, artifactVersion5);
 	}
 
 	
 
 	@Test
 	public void testParDeployThatDependsUponArtifactsInRemoteHostedRepo()
 			throws Exception {
 		signature = new String[] { String.class.getName() };
 		params = new Object[] { new File(APP_URI_PAR).toURI().toString() };
 		CompositeData compositeData = deploy(signature, params);
 		artifactName7 = compositeData.get("symbolicName").toString();
 		artifactType7 = compositeData.get("type").toString();
 		artifactVersion7 = compositeData.get("version").toString();
 
 		waitForMBeanRegister(artifactType7, artifactName7, artifactVersion7,
 				HALF_SECOND, TWO_MINUTES);
 		assertArtifactExists(artifactType7, artifactName7, artifactVersion7);
 		UrlWaitLatch.waitFor("http://localhost:8081/greenpages/app/home.htm");
 		assertArtifactState(artifactType7, artifactName7, artifactVersion7,
 				"ACTIVE");
 	}
 
 	@Test
 	public void testParUndeployThatDependsUponArtifactsInRemoteHostedRepo()
 			throws Exception {
 		signature = new String[] { String.class.getName(),
 				String.class.getName() };
 		params = new Object[] { artifactName7, artifactVersion7 };
 		getMBeanServerConnection()
 				.invoke(
 						new ObjectName(
 								"org.eclipse.virgo.kernel:category=Control,type=Deployer"),
 						"undeploy", params, signature);
 		waitForMBeanDeRegister(artifactType7, artifactName7, artifactVersion7,
 				HALF_SECOND, TWO_MINUTES);
 		UrlWaitLatch.waitForNotExistence("http://localhost:8081/greenpages");
 		assertArtifactNotExists(artifactType7, artifactName7, artifactVersion7);
 	}
 
 	@AfterClass
 	public static void shutdownRemoteInstance() throws Exception {
 		new Thread(new ShutdownThread()).start();
 		UrlWaitLatch.waitForServerShutdownFully(SPLASHURL);
 		FileSystemUtils
 				.deleteRecursively(new File(getConfiguredServerHomeDir()));
 		shutdownServer();
 	}
 
 	private static CompositeData deploy(String[] signature, Object[] params)
 			throws Exception {
 		CompositeData compsiteData = (CompositeData) getMBeanServerConnection()
 				.invoke(
 						new ObjectName(
 								"org.eclipse.virgo.kernel:category=Control,type=Deployer"),
 						"deploy", params, signature);
 		return compsiteData;
 	}
 
 	private void assertArtifactExists(String type, String name, String version)
 			throws IOException, Exception, MalformedObjectNameException {
 		assertTrue(String.format("Artifact %s:%s:%s does not exist", type,
 				name, version), getMBeanServerConnection().isRegistered(
 				getObjectName(type, name, version)));
 	}
 
 	private void assertArtifactState(String type, String name, String version,
 			String state) throws MalformedObjectNameException, IOException,
 			Exception {
 		assertEquals(String.format("Artifact %s:%s:%s is not in state %s",
 				type, name, version, state), state, getMBeanServerConnection()
 				.getAttribute(getObjectName(type, name, version), "State"));
 	}
 
 	private void assertArtifactNotExists(String type, String name,
 			String version) throws IOException, Exception,
 			MalformedObjectNameException {
 		assertFalse(String.format("Artifact %s:%s:%s is still exists", type,
 				name, version), getMBeanServerConnection().isRegistered(
 				getObjectName(type, name, version)));
 	}
 
 	private void assertPlanReferencedArtifacts(ObjectName objectName)
 			throws MalformedObjectNameException, IOException, Exception {
 		assertTrue("plan referenced artifact does not exist",
 				getMBeanServerConnection().isRegistered(objectName));
 		assertEquals(String.format(
 				"plan referenced artifact is not in state %s", "ACTIVE"),
 				"ACTIVE", getMBeanServerConnection().getAttribute(objectName,
 						"State"));
 	}
 
 	private static MBeanServerConnection getMBeanServerConnection()
 			throws Exception {
 		String severDir = null;
 		String[] creds = { "admin", "springsource" };
 		Map<String, String[]> env = new HashMap<String, String[]>();
 
 		File testExpanded = new File("src/test/resources");
 		for (File mainDir : testExpanded.listFiles()) {
 			if (mainDir.isDirectory()
 					&& mainDir.getName().equals("virgo-web-server")) {
 				severDir = new File(mainDir.toURI()).getCanonicalPath();
 			}
 		}
 		env.put(JMXConnector.CREDENTIALS, creds);
 		System.setProperty("javax.net.ssl.trustStore", severDir + KEYSTORE);
 		System.setProperty("javax.net.ssl.trustStorePassword", KEYPASSWORD);
 		JMXServiceURL url = new JMXServiceURL(JMXURL);
 		connection = JMXConnectorFactory.connect(url, env)
 				.getMBeanServerConnection();
 		return connection;
 	}
 
 	private void waitForMBeanRegister(String type, String name, String version,
 			long interval, long duration) throws Exception {
 		long startTime = System.currentTimeMillis();
 		boolean mbeanStatus = false;
 		while (System.currentTimeMillis() - startTime < duration) {
 			mbeanStatus = getMBeanServerConnection().isRegistered(
 					getObjectName(type, name, version));
 			if (mbeanStatus) {
 				return;
 			}
 			Thread.sleep(interval);
 		}
 		fail(String.format("After %d ms, artifact %s mbean Status was",
 				duration, name)
 				+ mbeanStatus);
 	}
 
 	private void waitForMBeanDeRegister(String type, String name,
 			String version, long interval, long duration) throws Exception {
 		long startTime = System.currentTimeMillis();
 		boolean mbeanStatus = true;
 		while (System.currentTimeMillis() - startTime < duration) {
 			mbeanStatus = getMBeanServerConnection().isRegistered(
 					getObjectName(type, name, version));
 			if (!mbeanStatus) {
 				return;
 			}
 			Thread.sleep(interval);
 		}
 		fail(String.format("After %d ms, artifact %s mbean Status was",
 				duration, name)
 				+ mbeanStatus);
 	}
 
 	private void waitForArtifactState(String type, String name, String version,
 			String state, long interval, long duration) throws Exception {
 		long startTime = System.currentTimeMillis();
 		String artifactstate = null;
 		while (System.currentTimeMillis() - startTime < duration) {
 			artifactstate = getMBeanServerConnection().getAttribute(
 					getObjectName(type, name, version), "State").toString();
 			if (artifactstate.equals(state)) {
 				return;
 			}
 			Thread.sleep(interval);
 		}
 		fail(String
 				.format("After %d ms, artifact %s state was", duration, name)
 				+ artifactstate);
 	}
 
 	private static ObjectName getObjectName(String type, String name,
 			String version) throws MalformedObjectNameException {
 		return new ObjectName(
 				String
 						.format(
 								"org.eclipse.virgo.kernel:type=Model,artifact-type=%s,name=%s,version=%s",
 								type, name, version));
 	}
 
 	private static class StartUpThread implements Runnable {
 		public StartUpThread() {
 		}
 
 		public void run() {
 			String[] args = null;
 			try {
 				if (os.getName().contains("Windows")) {
 					startup = new File(getConfiguredServerBinDir(),
 							"startup.bat");
 					startupURI = new File(startup.toURI());
 					startupFileName = startupURI.getCanonicalPath();
 				} else {
 					startup = new File(getConfiguredServerBinDir(),
 							"startup.sh");
 					startupURI = new File(startup.toURI());
 					startupFileName = startupURI.getCanonicalPath();
 				}
 				args = new String[] { startupFileName, "-jmxport", "3333" ,"-clean"};
 				pb = new ProcessBuilder(args);
 				pb.redirectErrorStream(true);
 				Map<String, String> env = pb.environment();
 				env.put("JAVA_HOME", System.getProperty("java.home"));
 
 				process = pb.start();
 
 				InputStream is = process.getInputStream();
 				InputStreamReader isr = new InputStreamReader(is);
 				BufferedReader br = new BufferedReader(isr);
 				String line;
 				while ((line = br.readLine()) != null) {
 					System.out.println(line);
 				}
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	private static class ShutdownThread implements Runnable {
 		public ShutdownThread() {
 
 		}
 
 		public void run() {
 			String[] args = null;
 			try {
 				if (os.getName().contains("Windows")) {
 					shutdown = new File(getConfiguredServerBinDir(),
 							"shutdown.bat");
 					shutdownURI = new File(shutdown.toURI());
 					shutdownFileName = shutdownURI.getCanonicalPath();
 				} else {
 					shutdown = new File(getConfiguredServerBinDir(),
 							"shutdown.sh");
 					shutdownURI = new File(shutdown.toURI());
 					shutdownFileName = shutdownURI.getCanonicalPath();
 				}
 				args = new String[] { shutdownFileName, "-jmxport", "3333" };
 				pb = new ProcessBuilder(args);
 				Map<String, String> env = pb.environment();
 				env.put("JAVA_HOME", System.getProperty("java.home"));
 				pb.redirectErrorStream(true);
 
 				process = pb.start();
 
 				InputStream is = process.getInputStream();
 				InputStreamReader isr = new InputStreamReader(is);
 				BufferedReader br = new BufferedReader(isr);
 				String line;
 				while ((line = br.readLine()) != null) {
 					System.out.println(line);
 				}
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 
 		}
 	}
 
 }
