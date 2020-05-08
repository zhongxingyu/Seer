 package no.miles;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 
 import java.io.IOException;
 
 import org.apache.commons.httpclient.HttpClient;
 import org.apache.commons.httpclient.HttpException;
 import org.apache.commons.httpclient.methods.GetMethod;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 public class EmbeddedJettyExample {
 
 	private JettyEmbedder embedder;
 
 	@Before
 	public void setUp() {
 		embedder = JettyEmbedder.getJettyEmbedder();
 
 		assertTrue(embedder.start());
 	}
 
 	@Test
	public void shouldStartAJettyServerAccessAResourceAndShutDownTheServer()
			throws HttpException, IOException {
 		HttpClient client = new HttpClient();
 		assertEquals(200, client.executeMethod(new GetMethod(
 				"http://localhost:8080/restest/product")));
 	}
 
 	@After
 	public void tearDown() {
 		assertTrue(embedder.stop());
 	}
 }
