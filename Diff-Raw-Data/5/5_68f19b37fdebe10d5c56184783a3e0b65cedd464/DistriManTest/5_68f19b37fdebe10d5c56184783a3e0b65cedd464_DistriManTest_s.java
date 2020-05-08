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
 package fr.imag.adele.apam.test.testcases;
 
 import static org.ops4j.pax.exam.CoreOptions.junitBundles;
 import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
 import static org.ops4j.pax.exam.CoreOptions.vmOption;
 
 import java.io.File;
 import java.io.IOException;
 import java.lang.reflect.Method;
 import java.net.MalformedURLException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 
 import junit.framework.Assert;
 
 import org.apache.cxf.frontend.ClientProxyFactoryBean;
 import org.apache.felix.framework.Felix;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.ops4j.pax.exam.Configuration;
 import org.ops4j.pax.exam.Option;
 import org.ops4j.pax.exam.junit.JUnit4TestRunner;
 import org.osgi.framework.Bundle;
 import org.osgi.framework.launch.Framework;
 
 import fr.imag.adele.apam.CST;
 import fr.imag.adele.apam.Implementation;
 import fr.imag.adele.apam.Instance;
 import fr.imag.adele.apam.pax.distriman.test.iface.P2Spec;
 import fr.imag.adele.apam.test.support.distriman.DistrimanUtil;
 import fr.imag.adele.apam.tests.helpers.ExtensionAbstract;
 
 @RunWith(JUnit4TestRunner.class)
 public class DistriManTest extends ExtensionAbstract {
 
 	//CoreOptions.systemProperty("org.osgi.framework.system.packages.extra").value("org.ops4j.pax.url.mvn");
 	//CoreOptions.frameworkProperty("org.osgi.framework.system.packages.extra").value("org.ops4j.pax.url.mvn");
 	@Override
 	public List<Option> config() {
 		List<Option> config = new ArrayList<Option>();//super.config();
 		
 		config.add(packInitialConfig());
 		config.add(packOSGi());
 		config.add(packPax());
 		config.add(packApamCore());		
 		config.add(packApamObrMan());
 		config.add(packLog());
 		config.add(junitBundles());
 		config.add(packDebugConfiguration());
 		config.add(vmOption("-ea"));
		config.add(mavenBundle().groupId("org.ops4j.pax.url").artifactId("pax-url-aether").versionAsInProject());		
 		config.add(packApamDistriMan());
 		config.add(mavenBundle().groupId("fr.imag.adele.apam.tests.services")
 				.artifactId("apam-pax-distriman-iface").versionAsInProject());
 		config.add(mavenBundle().groupId("fr.imag.adele.apam.tests.services")
 				.artifactId("apam-pax-distriman-P2").versionAsInProject());
 		
 		return config;
 		
 	}
 
 	private static Method getConfigurationMethod(Class<?> klass) {
 		Method[] methods = klass.getMethods();
 		for (Method m : methods) {
 			Configuration conf = m.getAnnotation(Configuration.class);
 			if (conf != null) {
 				return m;
 			}
 		}
 		throw new IllegalArgumentException(klass.getName()
 				+ " has no @Configuration method");
 	}
 
 	@Test
 	public void ProviderDependencyInterface_tc086()
 			throws MalformedURLException, IOException {
 
 		Implementation p2Impl = CST.apamResolver.findImplByName(null,
 				"P2-singleinterface");
 
 		Instance p2Inst = p2Impl.createInstance(null, null);
 
 		String url = "http://127.0.0.1:8080/apam/machine";
 
 		final String jsonPayload = DistrimanUtil.httpRequestDependency("p2",
 				"itf", "fr.imag.adele.apam.pax.distriman.test.iface.P2Spec",
 				"P2-singleinterface", false, url);
 
 		Map<String, String> parameters = new HashMap<String, String>() {
 			{
 				put("content", jsonPayload);
 			}
 		};
 
 		String response = DistrimanUtil.curl(parameters, url);
 
 		System.err.println(response);
 
 		Map<String, String> endpoints = DistrimanUtil.endpointGet(response);
 
 		System.out.println("Class\tURL");
 		for (Map.Entry<String, String> entry : endpoints.entrySet()) {
 			System.out.println(String.format("%s\t%s", entry.getKey(),
 					entry.getValue()));
 		}
 
 		Assert.assertTrue(
 				String.format(
 						"distriman(provider host) did not create an endpoint, or not the right number of endpoints. Expected 1 but %s were provided",
 						endpoints.size()), endpoints.size() == 1);
 
 		try {
 
 			ClientProxyFactoryBean factory = new ClientProxyFactoryBean();
 			factory.setServiceClass(P2Spec.class);
 			factory.setAddress(endpoints
 					.get("fr.imag.adele.apam.pax.distriman.test.iface.P2Spec"));
 			P2Spec proxy = (P2Spec) factory.create();
 			System.err.println(proxy.getName());
 
 		} catch (Exception e) {
 			Assert.fail(String
 					.format("distriman(provider host) created an endpoint but was not possible to connect to it, failed with the message %s",
 							e.getMessage()));
 		}
 
 	}
 
 	@Test
 	public void ProviderDependencySpecificationMultipleInterface_tc087()
 			throws MalformedURLException, IOException {
 
 		Implementation p2Impl = CST.apamResolver.findImplByName(null,
				"P2-singleinterface");
 
 		Instance p2Inst = p2Impl.createInstance(null, null);
 
 		String url = "http://127.0.0.1:8080/apam/machine";
 
 		final String jsonPayload = DistrimanUtil.httpRequestDependency("p2",
 				"specification", "P2-spec-multipleinterface", "P2", false, url);
 
 		Map<String, String> parameters = new HashMap<String, String>() {
 			{
 				put("content", jsonPayload);
 			}
 		};
 
 		String response = DistrimanUtil.curl(parameters, url);
 
 		System.err.println(response);
 
 		Map<String, String> endpoints = DistrimanUtil.endpointGet(response);
 
 		System.err.println("Class\tURL");
 		for (Map.Entry<String, String> entry : endpoints.entrySet()) {
 			System.err.println(String.format("%s\t%s", entry.getKey(),
 					entry.getValue()));
 		}
 
 		Assert.assertTrue(
 				String.format(
 						"distriman(provider host) did not create an endpoint, or not the right number of endpoints. Expected 2 but %s were provided",
 						endpoints.size()), endpoints.size() == 2);
 
 		DistrimanUtil.endpointConnect(endpoints);
 
 	}
 
 	@Test
 	public void ProviderDependencySpecificationSingleInterface_tc088()
 			throws MalformedURLException, IOException {
 
 		Implementation p2Impl = CST.apamResolver.findImplByName(null,
 				"P2-singleinterface");
 
 		Instance p2Inst = p2Impl.createInstance(null, null);
 
 		String url = "http://127.0.0.1:8080/apam/machine";
 
 		final String jsonPayload = DistrimanUtil.httpRequestDependency("p2",
 				"specification", "P2-spec-singleinterface", "P2", false, url);
 
 		Map<String, String> parameters = new HashMap<String, String>() {
 			{
 				put("content", jsonPayload);
 			}
 		};
 
 		String response = DistrimanUtil.curl(parameters, url);
 
 		System.err.println(response);
 
 		Map<String, String> endpoints = DistrimanUtil.endpointGet(response);
 
 		System.err.println("Class\tURL");
 		for (Map.Entry<String, String> entry : endpoints.entrySet()) {
 			System.err.println(String.format("%s\t%s", entry.getKey(),
 					entry.getValue()));
 		}
 
 		Assert.assertTrue(
 				String.format(
 						"distriman(provider host) did not create an endpoint, or not the right number of endpoints. Expected 1 but %s were provided",
 						endpoints.size()), endpoints.size() == 1);
 
 		DistrimanUtil.endpointConnect(endpoints);
 
 	}
 
 //	@Before
 //	public void adapt() throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
 //
 //		Method m = getConfigurationMethod(DistriManTest.class);
 //		Object configClassInstance = DistriManTest.class.newInstance();
 //		Option[] options = (Option[]) m.invoke(configClassInstance);
 //
 //		configuration =options;
 //	}
 	
 //	@Test
 	public void second() throws Exception {
 
 //		for (Bundle b : context.getBundles()) {
 //			System.err.println("* " + b.getBundleId() + ":"
 //					+ b.getSymbolicName());
 //		}		
 //		for (Option value : configuration) {
 //			if (value instanceof MavenArtifactProvisionOption) {
 //				MavenArtifactProvisionOption m = (MavenArtifactProvisionOption) value;
 //				Parser mvnparser = new Parser(m.getURL().replaceAll("mvn:", ""));
 //				String path = String.format(
 //						"file:///home/jnascimento/.m2/repository/%s",
 //						mvnparser.getArtifactPath());
 //				System.err.println(path);
 //			} else {
 //				System.err.println("its not:" + value);
 //			}
 //
 //		}
 
 		Framework m_felix = null;
 		Properties configProps = new Properties();
 		
 		
 		try {
 			// Create an instance and start the framework.
 			m_felix = new Felix(configProps);
 			
 			m_felix.start();
 			
 			
 			
 			List<Bundle> li=new ArrayList<Bundle>();
 			
 			
 			File file=new File("/home/jnascimento/project/apam/src/distributions/basic-distribution/bundle/");
 			
 			for(File bun:file.listFiles()){
 				if(!file.isDirectory()) continue;
 				li.add(m_felix.getBundleContext().installBundle("file://"+bun.getAbsolutePath()));
 			}
 			
 			for(Bundle b:li){
 				b.start();
 			}
 
 			for (Bundle b : m_felix.getBundleContext().getBundles()) {
 				System.err.println("* " + b.getBundleId() + ":"
 						+ b.getSymbolicName());
 			}
 			
 			System.out.println("Total of bundles:"+li.size());
 			
 			// Wait for framework to stop to exit the VM.
 			System.err.println("----" + m_felix.getState());
 			m_felix.waitForStop(0);
 			System.exit(0);
 		} catch (Exception ex) {
 			System.err.println("Could not create framework: " + ex);
 			ex.printStackTrace();
 			System.exit(-1);
 		}
 		
 		// Parser mvnparser=new Parser(mavenBundle("org.apache.felix",
 		// "org.apache.felix.main").version("1.8.0").getURL().replaceAll("mvn:",
 		// ""));
 		// System.err.println(mvnparser.getArtifactPath());
 		// Main.main(new
 		// String[]{"/home/jnascimento/project/apam/src/distributions/basic-distribution/tmp"});
 
 	}
 
 }
