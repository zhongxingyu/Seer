 /*
  * Copyright Adele Team LIG
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *   http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package fr.liglab.adele.cilia.core.tests;
 
 import static org.ops4j.pax.exam.CoreOptions.felix;
 import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
 import static org.ops4j.pax.exam.CoreOptions.options;
 import static org.ops4j.pax.exam.CoreOptions.provision;
 
 import java.util.Hashtable;
 
 import org.apache.felix.ipojo.test.helpers.OSGiHelper;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.ops4j.pax.exam.Inject;
 import org.ops4j.pax.exam.Option;
 import org.ops4j.pax.exam.OptionUtils;
 import org.ops4j.pax.exam.junit.Configuration;
 import org.osgi.framework.BundleContext;
 
 import fr.liglab.adele.cilia.builder.Architecture;
 import fr.liglab.adele.cilia.builder.Builder;
 import fr.liglab.adele.cilia.exceptions.BuilderException;
 import fr.liglab.adele.cilia.ext.ContentBasedRouting;
 
 /**
  * 
  * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project
  *         Team</a>
  * 
  */
 public class BuilderTest {
 
 	@Inject
 	private BundleContext context;
 
 	private OSGiHelper osgi;
 
 	@Before
 	public void setUp() {
 		osgi = new OSGiHelper(context);
 	}
 
 	@After
 	public void tearDown() {
 		osgi.dispose();
 	}
 
 	@Configuration
 	public static Option[] configure() {
 		Option[] platform = options(felix());
 
 		Option[] bundles = options(provision(
 				mavenBundle().groupId("org.apache.felix")
 						.artifactId("org.apache.felix.ipojo")
 						.versionAsInProject(),
 				mavenBundle().groupId("org.apache.felix")
 						.artifactId("org.apache.felix.ipojo.test.helpers")
 						.versionAsInProject(),
 				mavenBundle().groupId("org.osgi")
 						.artifactId("org.osgi.compendium").versionAsInProject(),
 				mavenBundle().groupId("org.slf4j").artifactId("slf4j-api")
 						.versionAsInProject(),
 				mavenBundle().groupId("org.slf4j").artifactId("slf4j-simple")
 						.version("1.6.1"),
 				mavenBundle().groupId("fr.liglab.adele.cilia")
 						.artifactId("cilia-core").versionAsInProject(),
 				mavenBundle().groupId("fr.liglab.adele.cilia")
 						.artifactId("cilia-runtime").versionAsInProject())); // The
 																				// target
 		Option[] r = OptionUtils.combine(platform, bundles);
 		return r;
 	}
 
	@Test
 	public void test()  {
 		Builder builder = getBuilder();
 		try {
 			Architecture arch = builder.create("MyChain");
 			arch.create().mediator().type("toto").namespace("nspace")
 					.id("tata");
 			arch.create().mediator().type("toto").id("tata").configure()
 					.key("toto").value("rr").key("").value("sq");
 			arch.create().mediator().type("toto").id("dsds").configure();
 			arch.create().mediator().type("rere").id("dsds");
 			arch.create().mediator().type("dsds").namespace("dsds").id("dsds");
 			arch.bind().using("ea").from("mediator1:titi").to("mediator2:tito");
 			arch.bind().from("mediator:toto").to("mediator2:end");
 			arch.configure().mediator().id("toto").key("tata").value("value")
 					.key("isi").value("rere").key("tata").value("value")
 					.key("isi").value("rere").key("tata").value("value")
 					.key("isi").value("rere").key("tata").value("value")
 					.key("isi").value("rere").key("tata").value("value")
 					.key("isi").value("rere").set(new Hashtable());
 
 			// ContentBasedRouting cb = new ContentBasedRouting();
 			// cb.evaluator("ldap").condition("(toto)").to("portX");
 			// arch.create().mediator().type("b").id("i").configure().dispatcher(cbd);
 
 			builder.done();
 		} catch (BuilderException be) {
 			
		}
 	}
 
 	Builder getBuilder() {
 		return null;
 	}
 
 }
