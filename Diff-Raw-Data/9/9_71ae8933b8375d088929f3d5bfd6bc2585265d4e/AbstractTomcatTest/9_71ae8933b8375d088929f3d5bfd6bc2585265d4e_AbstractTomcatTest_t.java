 package de.lbe.sandbox.servlet30;
 
 import org.junit.After;
 import org.junit.Before;
 
 import com.zanox.lib.commons.catalina.test.EmbeddedTestTomcat;
 import com.zanox.lib.commons.net.httpclient.DefaultHttpClient;
 import com.zanox.lib.commons.test.junit.AbstractJUnit4Test;
 
 /**
  * Provides a Jetty start up for testing against Jetty throughout several test classes.
  * 
  * @author lars.beuster
  */
 public class AbstractTomcatTest extends AbstractJUnit4Test {
 
 	private EmbeddedTestTomcat tomcat;
 
 	protected DefaultHttpClient httpClient;
 
 	/**
 	 * 
 	 */
 	@Before
 	public void setUp() throws Exception {
 		this.tomcat = new EmbeddedTestTomcat();
		this.tomcat.setHttpConnectorPort(1234);
 		this.tomcat.start();
 
 		this.httpClient = new DefaultHttpClient(this.tomcat.getWebappContextURL());
 	}
 
 	/**
 	 * 
 	 */
 	@After
 	public void tearDown() throws Exception {
 		if (this.httpClient != null) {
 			this.httpClient.shutdown();
 			this.httpClient = null;
 		}
 		if (this.tomcat != null) {
 			this.tomcat.stop();
 			this.tomcat = null;
 		}
 	}
 }
