 /**
  * Copyright 2011-2012 Universite Joseph Fourier, LIG, ADELE team
  *   Licensed under the Apache License, Version 2.0 (the "License");
  *   you may not use this file except in compliance with the License.
  *   You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  *   Unless required by applicable law or agreed to in writing, software
  *   distributed under the License is distributed on an "AS IS" BASIS,
  *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *   See the License for the specific language governing permissions and
  *   limitations under the License.
  */
 package fr.imag.adele.apam.tests.helpers;
 
 import static org.ops4j.pax.exam.CoreOptions.cleanCaches;
 import static org.ops4j.pax.exam.CoreOptions.frameworkProperty;
 import static org.ops4j.pax.exam.CoreOptions.junitBundles;
 import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
 import static org.ops4j.pax.exam.CoreOptions.systemProperty;
 import static org.ops4j.pax.exam.CoreOptions.systemTimeout;
 import static org.ops4j.pax.exam.CoreOptions.vmOption;
 import static org.ops4j.pax.exam.CoreOptions.when;
 
 import java.io.File;
 import java.lang.management.ManagementFactory;
 import java.lang.management.RuntimeMXBean;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.inject.Inject;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Rule;
 import org.junit.rules.TestName;
 import org.junit.rules.TestRule;
 import org.ops4j.pax.exam.Configuration;
 import org.ops4j.pax.exam.Option;
 import org.ops4j.pax.exam.options.CompositeOption;
 import org.ops4j.pax.exam.options.DefaultCompositeOption;
 import org.ops4j.pax.exam.util.PathUtils;
 import org.osgi.framework.BundleContext;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import fr.imag.adele.apam.CST;
 import fr.imag.adele.apam.ComponentBroker;
 
 public abstract class ExtensionAbstract extends TestUtils {
 
 	// Based on the current running, no test should take longer than 2 minute
 	@Rule
 	public TestRule globalTimeout = new ApamTimeoutRule(isDebugModeOn() ? null
 			: 120000);
 
 	@Rule
 	public TestName name = new TestName();
 
 	@Inject
 	public BundleContext context;
 
 	private Logger logger = LoggerFactory.getLogger(getClass());
 
 	public ApAMHelper apam;
 
 	protected ComponentBroker broker;
 
 	protected static Option[] configuration=null;
 	
 	public List<Option> config() {
 
 		List<Option> config = new ArrayList<Option>();
 		config.add(packInitialConfig());
 		config.add(packOSGi());
 		config.add(packPax());
 		config.add(packApamCore());		
 		config.add(packApamShell());
 		config.add(packApamObrMan());
 		config.add(packAppForTestBundles());
 		config.add(packLog());
 		config.add(junitBundles());
 		config.add(packDebugConfiguration());
 		config.add(vmOption("-ea"));
 
 		return config;
 	}
 	
 	public List<Option> configWithoutTests() {
 
 		List<Option> config = new ArrayList<Option>();
 
 		config.add(packInitialConfig());
 		config.add(packOSGi());
 		config.add(packPax());
 		config.add(packApamCore());
 		config.add(packApamShell());
 		config.add(packLog());
 		config.add(junitBundles());
 		config.add(packDebugConfiguration());
 		config.add(vmOption("-ea"));
 
 		return config;
 	}
 
 	protected CompositeOption packInitialConfig() {
 
 		String logpath="file:" + PathUtils.getBaseDir() + "/log/logback.xml";
 		File log=new File(logpath);
 		
 		boolean includeLog=log.exists()&&log.isFile();
 		
 		CompositeOption initial = new DefaultCompositeOption(
 				frameworkProperty("org.osgi.service.http.port").value("8080"),
 				cleanCaches(),
 				when(includeLog).useOptions(systemProperty("logback.configurationFile").value(logpath)),
 				systemProperty("org.ops4j.pax.logging.DefaultServiceLog.level").value("NONE")
 				);
 
 		return initial;
 	}
 
 	protected CompositeOption packApamCore() {
 
 		CompositeOption apamCoreConfig = new DefaultCompositeOption(
 				mavenBundle().groupId("fr.imag.adele.apam")
 						.artifactId("apam-bundle").versionAsInProject(),
 				mavenBundle("fr.imag.adele.apam.tests", "apam-helpers")
 						.versionAsInProject());
 
 		return apamCoreConfig;
 	}
 
 	protected CompositeOption packApamObrMan() {
 		CompositeOption apamObrmanConfig = new DefaultCompositeOption(
 				mavenBundle().groupId("fr.imag.adele.apam")
 						.artifactId("obrman").versionAsInProject());
 
 		return apamObrmanConfig;
 	}
 
 	protected CompositeOption packApamDynaMan() {
 		CompositeOption apamObrmanConfig = new DefaultCompositeOption(
 				mavenBundle("fr.imag.adele.apam", "dynaman")
 						.versionAsInProject());
 
 		return apamObrmanConfig;
 	}
 	
 	protected CompositeOption packApamDistriMan() {
 		CompositeOption apamObrmanConfig = new DefaultCompositeOption(
 				//version as in project
 				org.ops4j.pax.exam.CoreOptions.repositories(
 						"http://repository.springsource.com/maven/bundles/release",
 						"http://repository.springsource.com/maven/bundles/external",
 						"http://repo.maven.apache.org/maven2/",
 						"http://repository.ow2.org/nexus/content/repositories/snapshots",
 						"http://repository.ow2.org/nexus/content/repositories/releases",
 						"http://repository.ow2.org/nexus/content/sites/ow2-utilities",
 						"http://repository.ow2.org/nexus/content/repositories/thirdparty",
 						"http://repository.ow2.org/nexus/content/repositories/ow2-legacy",
 						"http://repository.ow2.org/nexus/content/groups/public",
 						"https://maven.java.net/content/repositories/releases/",
 						"https://repository.apache.org/content/groups/public",
 						"https://repository.apache.org/content/groups/snapshots"),
 //				mavenBundle("fr.imag.adele.apam", "dependencies-distriman").versionAsInProject()
 				mavenBundle("org.ow2.asm", "asm-all").version("4.1"),
 				mavenBundle("javax.mail", "com.springsource.javax.mail").version("1.4.1"),
 				mavenBundle("javax.wsdl", "com.springsource.javax.wsdl").version("1.6.1"),
 				mavenBundle("javax.xml.stream", "com.springsource.javax.xml.stream").version("1.0.1"),
 				mavenBundle("org.apache.xml", "com.springsource.org.apache.xml.resolver").version("1.2.0"),
 				mavenBundle("org.dom4j", "com.springsource.org.dom4j").version("1.6.1"),
 				mavenBundle("joda-time", "joda-time").version("1.6.2"),
 				//mavenBundle("org.ow2.chameleon.commons.cxf", "cxf-bundle-minimal").version("2.5.2-0002"),
 				mavenBundle("org.apache.cxf", "cxf-bundle-minimal").version("2.5.2"),
 				mavenBundle("com.google.guava", "guava").version("13.0-rc1"),	
 				mavenBundle("javax.ws.rs", "javax.ws.rs-api").version("2.0-m09"),	
 				mavenBundle("org.eclipse.jetty", "jetty-continuation").version("7.6.8.v20121106"),
 				mavenBundle("org.eclipse.jetty", "jetty-http").version("7.6.8.v20121106"),
 				mavenBundle("org.eclipse.jetty", "jetty-io").version("7.6.8.v20121106"),
 				mavenBundle("org.eclipse.jetty", "jetty-server").version("7.6.8.v20121106"),
 				mavenBundle("org.eclipse.jetty", "jetty-util").version("7.6.8.v20121106"),
 				mavenBundle("javax.ws.rs", "jsr311-api").version("1.1"),
 				mavenBundle("org.apache.neethi", "neethi").version("3.0.2"),
 				mavenBundle("org.apache.felix", "org.apache.felix.configadmin").version("1.2.8"),
 				mavenBundle("org.apache.felix", "org.apache.felix.fileinstall").version("3.2.0"),
 				mavenBundle("org.apache.felix", "org.apache.felix.gogo.command").version("0.12.0"),
 				mavenBundle("org.apache.felix", "org.apache.felix.gogo.runtime").version("0.10.0"),
 				mavenBundle("org.apache.felix", "org.apache.felix.gogo.shell").version("0.10.0"),
 				mavenBundle("org.apache.felix", "org.apache.felix.http.jetty").version("2.2.0"),
 				mavenBundle("org.apache.felix", "org.apache.felix.ipojo.annotations").version("1.8.0"),
 				mavenBundle("org.apache.felix", "org.apache.felix.ipojo.api").version("1.6.0"),
 				mavenBundle("org.apache.felix", "org.apache.felix.ipojo.arch.gogo").version("1.0.1"),
 				mavenBundle("org.apache.felix", "org.apache.felix.ipojo.manipulator").version("1.8.0"),
 				mavenBundle("org.apache.servicemix.bundles", "org.apache.servicemix.bundles.opensaml").version("2.4.1_1"),
 				mavenBundle("org.apache.ws.xmlschema", "xmlschema-core").version("2.0"),
 				mavenBundle("org.codehaus.jackson", "jackson-core-asl").version("1.9.12"),
 				mavenBundle("org.codehaus.jackson", "jackson-mapper-asl").version("1.9.12"),				
 				mavenBundle("org.codehaus.jettison", "jettison").version("1.3.3"),
 				mavenBundle("fr.imag.adele.apam", "DISTRIMAN").versionAsInProject(),
 				mavenBundle("org.apache.felix", "org.apache.felix.main").version("1.8.0")
 				
 				);
 		
 		return apamObrmanConfig;
 	}
 
 	protected CompositeOption packPax() {
 		CompositeOption paxConfig = new DefaultCompositeOption(mavenBundle()
 				.groupId("org.ops4j.pax.url").artifactId("pax-url-mvn")
 				.versionAsInProject());
 		return paxConfig;
 	}
 
 	protected CompositeOption packOSGi() {
 		CompositeOption osgiConfig = new DefaultCompositeOption(
 				mavenBundle().groupId("org.apache.felix").artifactId("org.apache.felix.ipojo").versionAsInProject(),
 				mavenBundle().groupId("org.ow2.chameleon.testing")
 						.artifactId("osgi-helpers").versionAsInProject(),
 				mavenBundle().groupId("org.osgi")
 						.artifactId("org.osgi.compendium").version("4.2.0"),
 				mavenBundle().groupId("org.apache.felix")
 						.artifactId("org.apache.felix.bundlerepository")
 						.version("1.6.6"));
 
 		return osgiConfig;
 
 	}
 
 
 	
 	protected CompositeOption packLog() {
 		CompositeOption logConfig = new DefaultCompositeOption(
 				mavenBundle("ch.qos.logback", "logback-core").versionAsInProject(),
 				mavenBundle("ch.qos.logback", "logback-classic").versionAsInProject(), 
 				mavenBundle("org.slf4j","slf4j-api").versionAsInProject()
 				);
 
 		return logConfig;
 	}
 	
 	protected CompositeOption packApamShell() {
 		CompositeOption logConfig = new DefaultCompositeOption(
 				mavenBundle("fr.imag.adele.apam", "apam-universal-shell").
 				versionAsInProject(),
 			mavenBundle("org.apache.felix",
 				"org.apache.felix.gogo.command").version("0.12.0"),
 			mavenBundle("org.apache.felix",
 				"org.apache.felix.gogo.runtime").version("0.10.0"),
 			mavenBundle("org.apache.felix",
 				"org.apache.felix.gogo.shell").version("0.10.0"),
 			mavenBundle("org.apache.felix",
 				"org.apache.felix.ipojo.arch.gogo").version("1.0.1"),
 			mavenBundle("org.knowhowlab.osgi.shell",
 				"felix-gogo").version("1.1.0")
 				);
 
 		return logConfig;
 	}
 
 	protected CompositeOption packDebugConfiguration() {
 		CompositeOption debugConfig = new DefaultCompositeOption(
 				when(isDebugModeOn())
 						.useOptions(
 								vmOption(String
 										.format("-Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=%d",
 												Constants.CONST_DEBUG_PORT)),
 								systemTimeout(0)));
 
 		return debugConfig;
 	}
 
 	protected CompositeOption packAppForTestBundles() {
 
 		CompositeOption testAppBundle = new DefaultCompositeOption(
 				mavenBundle("fr.imag.adele.apam.tests", "apam-helpers").versionAsInProject(), 
 				
 				
 				mavenBundle("fr.imag.adele.apam.tests.obrman.app1.private","APP1-MainImpl").versionAsInProject(),
 				mavenBundle("fr.imag.adele.apam.tests.obrman.app1.private","APP1-MainSpec").versionAsInProject(), 
 				mavenBundle("fr.imag.adele.apam.tests.obrman.app1.private","APP1-S1-Spec").versionAsInProject(), 
 				mavenBundle("fr.imag.adele.apam.tests.obrman.app1.private","APP1-S2-Spec").versionAsInProject(), 
 				mavenBundle("fr.imag.adele.apam.tests.obrman.app1.private","APP1-S3-Spec").versionAsInProject(), 
 				mavenBundle("fr.imag.adele.apam.tests.obrman.app1.public","APP1-Spec").versionAsInProject(), 
 				mavenBundle("fr.imag.adele.apam.tests.obrman.app2.private","APP2-MainImpl").versionAsInProject(), 
 				mavenBundle("fr.imag.adele.apam.tests.obrman.app2.private","APP2-MainSpec").versionAsInProject(), 
 				mavenBundle("fr.imag.adele.apam.tests.obrman.app2.public","APP2-Spec").versionAsInProject(),
 				
 				mavenBundle("fr.imag.adele.apam.tests.services","apam-pax-samples-iface").versionAsInProject(),
 				mavenBundle("fr.imag.adele.apam.tests.services","apam-pax-samples-impl-s1").versionAsInProject(),
 				mavenBundle("fr.imag.adele.apam.tests.services","apam-pax-samples-impl-s2").versionAsInProject(),
 				mavenBundle("fr.imag.adele.apam.tests.services","apam-pax-samples-impl-s3").versionAsInProject(),
 				mavenBundle("fr.imag.adele.apam.tests.services","apam-pax-samples-impl-s6").versionAsInProject(),
 				
 				mavenBundle("fr.imag.adele.apam.tests.messages","apam-pax-samples-msg").versionAsInProject(),
				mavenBundle("fr.imag.adele.apam.tests.messages","apam-pax-samples-impl-m1").versionAsInProject());
 		return testAppBundle;
 
 	}
 
 	@Configuration
 	public Option[] apamConfig() {
 
 		Option conf[] = config().toArray(new Option[0]);
 
 		configuration=conf;
 		
 		return conf;
 	}
 
 	@Before
 	public void setUp() {
 		apam = new ApAMHelper(context);
 		broker = CST.componentBroker;
 		logger.info("[Run Test : " + name.getMethodName() + "]");
 		apam.waitForIt(1000);
 	}
 
 	@After
 	public void tearDown() {
 		apam.dispose();
 	}
 
 	private boolean isDebugModeOn() {
 		RuntimeMXBean RuntimemxBean = ManagementFactory.getRuntimeMXBean();
 		List<String> arguments = RuntimemxBean.getInputArguments();
 
 		boolean debugModeOn = false;
 
 		for (String string : arguments) {
 			debugModeOn = string.indexOf("jdwp") != -1;
 			if (debugModeOn)
 				break;
 		}
 
 		return debugModeOn;
 	}
 
 }
