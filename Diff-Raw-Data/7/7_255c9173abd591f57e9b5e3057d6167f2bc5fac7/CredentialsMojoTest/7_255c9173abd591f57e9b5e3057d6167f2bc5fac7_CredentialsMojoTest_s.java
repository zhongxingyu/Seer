 package org.codehaus.mojo.credentials;
 
 /*
  * Copyright 2006 The Codehaus
  * 
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
  * in compliance with the License. You may obtain a copy of the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software distributed under the License
  * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
  * or implied. See the License for the specific language governing permissions and limitations under
  * the License.
  */
 
 import java.util.Properties;
 
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.testing.AbstractMojoTestCase;
 import org.apache.maven.plugin.testing.stubs.MavenProjectStub;
 import org.apache.maven.settings.Server;
 import org.apache.maven.settings.Settings;
 import org.sonatype.plexus.components.sec.dispatcher.SecDispatcher;
 
 /**
  * Unit test for simple CredentialsMojo.
  */
 public class CredentialsMojoTest extends AbstractMojoTestCase {
 	private CredentialsMojo mojo;
 
 	private Properties p;
 
 	private MavenProjectStub project;
 
 	@Override
 	public void setUp() throws Exception {
 		super.setUp();
		p = new Properties();
		p.load(getClass().getResourceAsStream("/test.properties"));
 
 		mojo = new CredentialsMojo();
 
		// populate parameters
		mojo.setUsername(p.getProperty("user"));
		mojo.setPassword(p.getProperty("password"));

 		SecDispatcher securityDispatcher = (SecDispatcher) lookup(
 				"org.sonatype.plexus.components.sec.dispatcher.SecDispatcher",
 				"default");
 		mojo.setSecurityDispatcher(securityDispatcher);
 
 		project = new MavenProjectStub();
 		setVariableValueToObject(mojo, "project", project);
 	}
 
 	public void testDefaultUsernamePassword() throws MojoExecutionException {
 
 		Settings settings = new Settings();
 		Server server = new Server();
 		settings.addServer(server);
 
 		mojo.setSettings(settings);
 
 		mojo.setUsername(null);
 		mojo.setPassword(null);
 		mojo.setUsernameProperty("usernameProperty");
 		mojo.setPasswordProperty("passwordProperty");
 		mojo.setUseSystemProperties(false);
 
 		mojo.execute();
 
 		assertEquals("", mojo.getUsername());
 		assertEquals("", mojo.getPassword());
 	}
 
 	public void testUsernamePasswordLookup() throws MojoExecutionException {
 
 		Settings settings = new Settings();
 		Server server = new Server();
 		server.setId("somekey");
 		server.setUsername("username");
 		server.setPassword("password");
 		settings.addServer(server);
 
 		mojo.setSettings(settings);
 
 		// force a lookup of username
 		mojo.setSettingsKey("somekey");
 		mojo.setUsername(null);
 		mojo.setPassword(null);
 		mojo.setUsernameProperty("usernameProperty");
 		mojo.setPasswordProperty("passwordProperty");
 		mojo.setUseSystemProperties(true);
 
 		mojo.execute();
 
 		assertEquals("username", mojo.getUsername());
 		assertEquals("password", mojo.getPassword());
 		assertEquals("username", System.getProperty("usernameProperty"));
 		assertEquals("password", System.getProperty("passwordProperty"));
 	}
 }
