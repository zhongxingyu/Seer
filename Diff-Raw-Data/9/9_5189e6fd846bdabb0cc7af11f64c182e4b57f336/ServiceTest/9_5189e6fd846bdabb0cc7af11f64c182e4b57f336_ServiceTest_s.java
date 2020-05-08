 package de.soulworks.dam.webservice;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpDelete;
 import org.apache.http.client.methods.HttpPut;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.codehaus.jackson.map.ObjectMapper;
 import org.junit.Before;
 import org.junit.runner.RunWith;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 
 import java.io.IOException;
 
 /**
 * @author Christian Schmitz <csc@soulworks.de
  */
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(locations = {"classpath:/spring/app-config.xml", "classpath:/spring/ektorp-config.xml", "classpath:/spring/jcouchdb-config.xml", "classpath:/spring/jms-config.xml"}, inheritLocations = true)
 abstract public class ServiceTest {
 
 //	@Before
 	public void setup() {
 		ObjectMapper mapper = new ObjectMapper();
 		HttpClient hc = new DefaultHttpClient();
 		HttpResponse response = null;
 		HttpEntity entity = null;
 
 		try {
 			// Delete database
 			HttpDelete delete = new HttpDelete("http://localhost:5984/dam");
 			response = hc.execute(delete);
 			response.getEntity().getContent().close();
 
 			// Create database
 			HttpPut put = new HttpPut("http://localhost:5984/dam");
 			response = hc.execute(put);
 			response.getEntity().getContent().close();
 		} catch (IOException e) {
 			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
 		}
 	}
 
 }
