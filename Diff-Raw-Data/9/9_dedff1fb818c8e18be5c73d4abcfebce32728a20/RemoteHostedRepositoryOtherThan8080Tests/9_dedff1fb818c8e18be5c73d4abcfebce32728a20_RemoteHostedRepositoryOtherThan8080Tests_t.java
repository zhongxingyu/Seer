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
 import org.junit.Test;
 
 public class RemoteHostedRepositoryOtherThan8080Tests extends AbstractWebTests {
 
 	private static String serverHomeDir1 = null;
 	private static String usrDir1 = null;
 	private static String binDir1 = null;
 	private static String configDir1 = null;
 
 	private static String serverHomeDir2 = null;
 	private static String usrDir2 = null;
 	private static String binDir2 = null;
 	private static String configDir2 = null;
 	private static String pickupDir = null;
 	private static String hostedDir = null;
 
 	protected static final String HOSTED_REPO_SPLASHURL = "http://localhost:8084/";
 	protected static final String HOSTED_REPO_ADMINURL = "http://localhost:8084/admin";
 	protected static final String CLIENT_SPLASHURL = "http://localhost:8083/";
 	protected static final String CLEINT_ADMINURL = "http://localhost:8083/admin";
 
 	private static final String APP_URI_PLAN_UNSCOPED = "./../org.eclipse.virgo.server.svt/apps/helloworld_unscoped-1.0.0.RELEASE.plan";
 	private static final String APP_URI_SHAREDSERVICES_WAR1 = "./../org.eclipse.virgo.server.svt/apps/hello_war_shared_services.war";
 	private static final String APP_URI_SHAREDSERVICES_WAR2 = "./../org.eclipse.virgo.server.svt/apps/formtags-shared-services-war-2.0.1.BUILD-20100413113234.war";
 	private static final String APP_URI_PAR = "./../org.eclipse.virgo.server.svt/apps/greenpages-solution-2.0.1.SNAPSHOT.par";
 	private static final String APP_URI_PLAN = "./../test-apps/scopedplan.plan";
 
 	private static final String resourcesDir1 = "./../org.eclipse.virgo.server.svt/bundles";
 	private static final String resourcesDir2 = "./../org.eclipse.virgo.server.svt/src/test/resources/bundles";
     private static final String downloadDir = "target/bundles";
 
 	private static String[] bundleNames1 = new String[] {
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
 	
 	private static String[] downloadBundleNames1 = new String[] {
             "com.springsource.org.hibernate-3.2.6.ga.jar" };
 
 	private static String[] bundleNames2 = new String[] {
 			"formtags-shared-services-service-2.0.1.BUILD-20100413113234.jar",
 			"com.springsource.freemarker-2.3.15.jar",
 			"com.springsource.javax.persistence-1.0.0.jar",
 			"com.springsource.org.apache.commons.dbcp-1.2.2.osgi.jar",
 			"com.springsource.org.apache.commons.pool-1.3.0.jar",
 			"com.springsource.org.eclipse.persistence-1.0.0.jar",
 			"com.springsource.org.eclipse.persistence.antlr-1.0.0.jar",
 			"com.springsource.org.eclipse.persistence.asm-1.0.0.jar",
 			"com.springsource.org.eclipse.persistence.jpa-1.0.0.jar",
 			"com.springsource.org.h2-1.0.71.jar", "hello.domain.jar",
 			"hello.service.api.jar", "hello.service.impl-en.jar",
 			"bundleA.jar", "bundleB.war", "foo.properties" };
 
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
 	private static final String JMXURL = "service:jmx:rmi:///jndi/rmi://localhost:4444/jmxrmi";
 	private static final String KEYSTORE = "/config/keystore";
 	private static final String KEYPASSWORD = "changeit";
 	public static final long HALF_SECOND = 500;
 	public static final long TWO_MINUTES = 120 * 1000;
 
 	private static String[] signature = null;
 	private static Object[] params = null;
 
 	private static String artifactType1 = null;
 	private static String artifactName1 = null;
 	private static String artifactVersion1 = null;
 
 	private static String artifactType4 = null;
 	private static String artifactName4 = null;
 	private static String artifactVersion4 = null;
 
 	private static String artifactType5 = null;
 	private static String artifactName5 = null;
 	private static String artifactVersion5 = null;
 
 	private static String artifactType6 = null;
 	private static String artifactName6 = null;
 	private static String artifactVersion6 = null;
 
 	private static String artifactType7 = null;
 	private static String artifactName7 = null;
 	private static String artifactVersion7 = null;
 
 	@BeforeClass
 	public static void testSetUp() throws IOException, Exception {
 		FileCopyUtils.copy(new File("src/test/resources/tomcat-server1.xml"),
 				new File(getConfiguredRemoteServerConfigDir1(),
 						"tomcat-server.xml"));
 		new File(getConfiguredRemoteServerConfigDir1(), "tomcat-server1.xml")
 				.renameTo(new File(getConfiguredRemoteServerConfigDir1(),
 						"tomcat-server.xml"));
 		FileCopyUtils.copy(new File(
 				"src/test/resources/org.eclipse.virgo.kernel4.properties"),
 				new File(getConfiguredRemoteServerConfigDir1(),
 						"org.eclipse.virgo.kernel.properties"));
 		new File(getConfiguredRemoteServerConfigDir1(),
 				"org.eclipse.virgo.kernel4.properties").renameTo(new File(
 				getConfiguredRemoteServerConfigDir1(),
 				"org.eclipse.virgo.kernel.properties"));
 		copyBundlesToHostedRepository(getConfiguredRemoteServerHostedBundlesDir());
 		new Thread(new StartUpThread1()).start();
 		Thread.sleep(5000);
 		UrlWaitLatch.waitFor(HOSTED_REPO_SPLASHURL);
 
 		FileSystemUtils.deleteRecursively(new File(
 				getConfiguredRemoteServerBundlesUsrDir2()));
 		new File(getConfiguredRemoteServerBundlesUsrDir2()).mkdir();
 		FileSystemUtils.deleteRecursively(new File(
 				getConfiguredRemoteServerConfigDir2(),
 				"org.eclipse.virgo.apps.repository.properties"));
 		FileCopyUtils.copy(new File(
 				"src/test/resources/org.eclipse.virgo.repository1.properties"),
 				new File(getConfiguredRemoteServerConfigDir2(),
 						"org.eclipse.virgo.repository.properties"));
 		new File(getConfiguredRemoteServerConfigDir2(),
 				"org.eclipse.virgo.repository1.properties").renameTo(new File(
 				getConfiguredRemoteServerConfigDir2(),
 				"org.eclipse.virgo.repository.properties"));
 		copyDependentBundlesToRepository(getConfiguredRemoteServerBundlesUsrDir2());
 		FileCopyUtils.copy(new File("src/test/resources/tomcat-server2.xml"),
 				new File(getConfiguredRemoteServerConfigDir2(),
 						"tomcat-server.xml"));
 		new File(getConfiguredRemoteServerConfigDir2(), "tomcat-server2.xml")
 				.renameTo(new File(getConfiguredRemoteServerConfigDir2(),
 						"tomcat-server.xml"));
 		FileCopyUtils.copy(new File(
 				"src/test/resources/org.eclipse.virgo.kernel5.properties"),
 				new File(getConfiguredRemoteServerConfigDir2(),
 						"org.eclipse.virgo.kernel.properties"));
 		new File(getConfiguredRemoteServerConfigDir2(),
 				"org.eclipse.virgo.kernel5.properties").renameTo(new File(
 				getConfiguredRemoteServerConfigDir2(),
 				"org.eclipse.virgo.kernel.properties"));
 		new Thread(new StartUpThread2()).start();
 		Thread.sleep(5000);
 		UrlWaitLatch.waitFor(CLIENT_SPLASHURL);
 	}
 
 	protected static String getConfiguredRemoteServerHomeDir1()
 			throws Exception {
 		if (serverHomeDir1 == null) {
 			File testExpanded = new File("src/test/resources");
 			for (File candidate : testExpanded.listFiles()) {
 				if (candidate.isDirectory()
 						&& candidate.getName().equals("virgo-web-server1")) {
 					serverHomeDir1 = new File(candidate.getCanonicalPath())
 							.getCanonicalPath();
 					break;
 				}
 			}
 		}
 		return serverHomeDir1;
 	}
 
 	protected static String getConfiguredRemoteServerHomeDir2()
 			throws Exception {
 		if (serverHomeDir2 == null) {
 			File testExpanded = new File("src/test/resources");
 			for (File candidate : testExpanded.listFiles()) {
 				if (candidate.isDirectory()
 						&& candidate.getName().equals("virgo-web-server2")) {
 					serverHomeDir2 = new File(candidate.getCanonicalPath())
 							.getCanonicalPath();
 					break;
 				}
 			}
 		}
 		return serverHomeDir2;
 	}
 
 	protected static String getConfiguredRemoteServerPickupDir()
 			throws Exception {
 		if (pickupDir == null) {
 			File testExpanded = new File(getConfiguredRemoteServerHomeDir2());
 			for (File candidate : testExpanded.listFiles()) {
 				if (candidate.isDirectory()
 						&& candidate.getName().equals("pickup")) {
 					pickupDir = new File(candidate.getCanonicalPath())
 							.getCanonicalPath();
 					break;
 				}
 			}
 		}
 		return pickupDir;
 	}
 
 	public static String getConfiguredRemoteServerBinDir1() throws Exception {
 		if (binDir1 == null) {
 			File testExpanded = new File(getConfiguredRemoteServerHomeDir1());
 			for (File candidate : testExpanded.listFiles()) {
 				if (candidate.isDirectory()
 						&& candidate.getName().equals("bin")) {
 					binDir1 = new File(candidate.getCanonicalPath())
 							.getCanonicalPath();
 					break;
 				}
 			}
 		}
 		return binDir1;
 	}
 
 	public static String getConfiguredRemoteServerConfigDir1() throws Exception {
 		if (configDir1 == null) {
 			File testExpanded = new File(getConfiguredRemoteServerHomeDir1());
 			for (File candidate : testExpanded.listFiles()) {
 				if (candidate.isDirectory()
 						&& candidate.getName().equals("config")) {
 					configDir1 = new File(candidate.getCanonicalPath())
 							.getCanonicalPath();
 					break;
 				}
 			}
 		}
 		return configDir1;
 	}
 
 	public static String getConfiguredRemoteServerConfigDir2() throws Exception {
 		if (configDir2 == null) {
 			File testExpanded = new File(getConfiguredRemoteServerHomeDir2());
 			for (File candidate : testExpanded.listFiles()) {
 				if (candidate.isDirectory()
 						&& candidate.getName().equals("config")) {
 					configDir2 = new File(candidate.getCanonicalPath())
 							.getCanonicalPath();
 					break;
 				}
 			}
 		}
 		return configDir2;
 	}
 
 	public static String getConfiguredRemoteServerBinDir2() throws Exception {
 		if (binDir2 == null) {
 			File testExpanded = new File(getConfiguredRemoteServerHomeDir2());
 			for (File candidate : testExpanded.listFiles()) {
 				if (candidate.isDirectory()
 						&& candidate.getName().equals("bin")) {
 					binDir2 = new File(candidate.getCanonicalPath())
 							.getCanonicalPath();
 					break;
 				}
 			}
 		}
 		return binDir2;
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
 
 	protected static String getConfiguredRemoteServerBundlesUsrDir1()
 			throws Exception {
 		if (usrDir1 == null) {
 			File serverHomeDir = new File(getConfiguredRemoteServerHomeDir1());
 			if (serverHomeDir.isDirectory()) {
 				File repositoryDir = new File(serverHomeDir, "repository")
 						.getCanonicalFile();
 				usrDir1 = new File(repositoryDir, "usr").getCanonicalPath();
 			}
 		}
 
 		return usrDir1;
 	}
 
 	protected static String getConfiguredRemoteServerBundlesUsrDir2()
 			throws Exception {
 		if (usrDir2 == null) {
 			File serverHomeDir = new File(getConfiguredRemoteServerHomeDir2());
 			if (serverHomeDir.isDirectory()) {
 				File repositoryDir = new File(serverHomeDir, "repository")
 						.getCanonicalFile();
 				usrDir2 = new File(repositoryDir, "usr").getCanonicalPath();
 			}
 		}
 
 		return usrDir2;
 	}
 
 	protected static String getConfiguredRemoteServerHostedBundlesDir()
 			throws Exception {
 		if (hostedDir == null) {
 			File testExpanded = new File(getConfiguredRemoteServerHomeDir1());
 			for (File mainDir : testExpanded.listFiles()) {
 				if (mainDir.isDirectory()
 						&& mainDir.getName().equals("repository")) {
 					new File(mainDir, "hosted").mkdir();
 					hostedDir = new File(mainDir, "hosted").getCanonicalPath();
 					break;
 				}
 			}
 		}
 
 		return hostedDir;
 	}
 
 	protected static void copyDependentBundlesToRepository(String bundlesUsrDir)
 			throws Exception {
 
 		for (String bundleName : bundleNames1) {
 
 			FileCopyUtils.copy(new File(resourcesDir1, bundleName), new File(
 					bundlesUsrDir, bundleName));
 		}
 		
 		for (String bundleName : downloadBundleNames1) {
 
             FileCopyUtils.copy(new File(downloadDir, bundleName), new File(
                     bundlesUsrDir, bundleName));
         }
 	}
 
 	protected static void copyBundlesToHostedRepository(String bundlesUsrDir)
 			throws Exception {
 
 		for (String bundleName : bundleNames2) {
 
 			FileCopyUtils.copy(new File(resourcesDir2, bundleName), new File(
 					bundlesUsrDir, bundleName));
 		}
 	}
 
 	@Test
 	public void testScopedPlanArtifactDeployThatDependsUponArtifactsInRemoteHostedRepo()
 			throws Exception {
 		signature = new String[] { String.class.getName() };
 		params = new Object[] { new File(APP_URI_PLAN).toURI().toString() };
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
 	public void testScopedPlanArtifactUndeployThatDependsUponArtifactsInRemoteHostedRepo()
 			throws Exception {
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
 
 	@Test
 	public void testUnscopedPlanThatDependsUponArtifactsInRemoteHostedRepo()
 			throws IOException, Exception {
 		signature = new String[] { String.class.getName() };
 		params = new Object[] { new File(APP_URI_PLAN_UNSCOPED).toURI()
 				.toString() };
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
 				.waitFor("http://localhost:8083/hello_war_shared_services/");
 		assertArtifactExists(artifactType5, artifactName5, artifactVersion5);
 		assertArtifactState(artifactType5, artifactName5, artifactVersion5,
 				"ACTIVE");
 	}
 
 	@Test
 	public void testUnDeploymentOfSharedServicesWarDependsOnArtfiactsInUnscopedPlan()
 			throws Exception {
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
 				.waitForNotExistence("http://localhost:8083/hello_war_shared_services");
 		assertArtifactNotExists(artifactType5, artifactName5, artifactVersion5);
 	}
 
 	@Test
 	public void testSharedServicesWarDeployThatDependsUponArtifactsInRemoteHostedRepo()
 			throws Exception {
 		signature = new String[] { String.class.getName() };
 		params = new Object[] { new File(APP_URI_SHAREDSERVICES_WAR2).toURI()
 				.toString() };
 		CompositeData compositeData = deploy(signature, params);
 		artifactName6 = compositeData.get("symbolicName").toString();
 		artifactType6 = compositeData.get("type").toString();
 		artifactVersion6 = compositeData.get("version").toString();
 
 		waitForMBeanRegister(artifactType6, artifactName6, artifactVersion6,
 				HALF_SECOND, TWO_MINUTES);
 		UrlWaitLatch
 				.waitFor("http://localhost:8083/formtags-shared-services-war-2.0.1.BUILD-20100413113234");
 		UrlWaitLatch
 				.waitFor("http://localhost:8083/formtags-shared-services-war-2.0.1.BUILD-20100413113234/list.htm");
 		UrlWaitLatch
 				.waitFor("http://localhost:8083/formtags-shared-services-war-2.0.1.BUILD-20100413113234/form.htm?id=1");
 		assertArtifactExists(artifactType6, artifactName6, artifactVersion6);
 		assertArtifactState(artifactType6, artifactName6, artifactVersion6,
 				"ACTIVE");
 	}
 
 	@Test
 	public void testSharedServicesWarUnDeployThatDependsUponArtifactsInRemoteHostedRepo()
 			throws Exception {
 		signature = new String[] { String.class.getName(),
 				String.class.getName() };
 		params = new Object[] { artifactName6, artifactVersion6 };
 		getMBeanServerConnection()
 				.invoke(
 						new ObjectName(
 								"org.eclipse.virgo.kernel:category=Control,type=Deployer"),
 						"undeploy", params, signature);
 		waitForMBeanDeRegister(artifactType6, artifactName6, artifactVersion6,
 				HALF_SECOND, TWO_MINUTES);
 		UrlWaitLatch
 				.waitForNotExistence("http://localhost:8083/formtags-shared-services-war-2.0.1.BUILD-20100413113234");
 		assertArtifactNotExists(artifactType6, artifactName6, artifactVersion6);
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
 		UrlWaitLatch.waitFor("http://localhost:8083/greenpages/app/home.htm");
 		assertArtifactState(artifactType7, artifactName7, artifactVersion7,
 				"ACTIVE");
 	}
 
 	@Test
 	public void testParUnDeployThatDependsUponArtifactsInRemoteHostedRepo()
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
 		UrlWaitLatch.waitForNotExistence("http://localhost:8083/greenpages");
 		assertArtifactNotExists(artifactType7, artifactName7, artifactVersion7);
 	}
 
 	@AfterClass
 	public static void shutdownRemoteInstance() throws Exception {
 		new Thread(new ShutdownThread1()).start();
 		UrlWaitLatch.waitForServerShutdownFully(HOSTED_REPO_SPLASHURL);
 		 FileSystemUtils.deleteRecursively(new File(
 		 getConfiguredRemoteServerHomeDir1()));
 		new Thread(new ShutdownThread2()).start();
 		UrlWaitLatch.waitForServerShutdownFully(CLIENT_SPLASHURL);
 		 FileSystemUtils.deleteRecursively(new File(
 		 getConfiguredRemoteServerHomeDir2()));
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
 
 	private static MBeanServerConnection getMBeanServerConnection()
 			throws Exception {
 		String severDir = null;
 		String[] creds = { "admin", "springsource" };
 		Map<String, String[]> env = new HashMap<String, String[]>();
 
 		File testExpanded = new File("src/test/resources");
 		for (File mainDir : testExpanded.listFiles()) {
 			if (mainDir.isDirectory()
 					&& mainDir.getName().equals("virgo-web-server2")) {
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
 
 	private static class StartUpThread1 implements Runnable {
 		public StartUpThread1() {
 		}
 
 		public void run() {
 			String[] args = null;
 			try {
 				if (os.getName().contains("Windows")) {
 					startup = new File(getConfiguredRemoteServerBinDir1(),
 							"startup.bat");
 					startupURI = new File(startup.toURI());
 					startupFileName = startupURI.getCanonicalPath();
 
 				} else {
 					startup = new File(getConfiguredRemoteServerBinDir1(),
 							"startup.sh");
 					startupURI = new File(startup.toURI());
 					startupFileName = startupURI.getCanonicalPath();
 				}
 				args = new String[] { startupFileName, "-jmxport", "3333",
 						"-clean" };
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
 
 	private static class StartUpThread2 implements Runnable {
 		public StartUpThread2() {
 		}
 
 		public void run() {
 			String[] args = null;
 			try {
 				if (os.getName().contains("Windows")) {
 					startup = new File(getConfiguredRemoteServerBinDir2(),
 							"startup.bat");
 					startupURI = new File(startup.toURI());
 					startupFileName = startupURI.getCanonicalPath();
 
 				} else {
 					startup = new File(getConfiguredRemoteServerBinDir2(),
 							"startup.sh");
 					startupURI = new File(startup.toURI());
 					startupFileName = startupURI.getCanonicalPath();
 				}
 				args = new String[] { startupFileName, "-jmxport", "4444",
 						"-clean" };
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
 
 	private static class ShutdownThread1 implements Runnable {
 		public ShutdownThread1() {
 		}
 
 		public void run() {
 			String[] args = null;
 			try {
 				if (os.getName().contains("Windows")) {
 					shutdown = new File(getConfiguredRemoteServerBinDir1(),
 							"shutdown.bat");
 					shutdownURI = new File(shutdown.toURI());
 					shutdownFileName = shutdownURI.getCanonicalPath();
 				} else {
 					shutdown = new File(getConfiguredRemoteServerBinDir1(),
 							"shutdown.sh");
 					shutdownURI = new File(shutdown.toURI());
 					shutdownFileName = shutdownURI.getCanonicalPath();
 				}
 				args = new String[] { shutdownFileName, "-jmxport", "3333",
 						"-immediate" };
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
 
 	private static class ShutdownThread2 implements Runnable {
 		public ShutdownThread2() {
 		}
 
 		public void run() {
 			String[] args = null;
 			try {
 				if (os.getName().contains("Windows")) {
 					shutdown = new File(getConfiguredRemoteServerBinDir2(),
 							"shutdown.bat");
 					shutdownURI = new File(shutdown.toURI());
 					shutdownFileName = shutdownURI.getCanonicalPath();
 				} else {
 					shutdown = new File(getConfiguredRemoteServerBinDir2(),
 							"shutdown.sh");
 					shutdownURI = new File(shutdown.toURI());
 					shutdownFileName = shutdownURI.getCanonicalPath();
 				}
 				args = new String[] { shutdownFileName, "-jmxport", "4444",
 						"-immediate" };
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
