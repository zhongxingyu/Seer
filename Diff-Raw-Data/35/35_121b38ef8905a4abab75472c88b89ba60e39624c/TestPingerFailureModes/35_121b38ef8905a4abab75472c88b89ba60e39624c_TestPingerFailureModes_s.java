 
 package test.axirassa.pinger;
 
 import static org.junit.Assert.assertArrayEquals;
 import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
 
 import java.io.IOException;
 
 import org.apache.http.client.ClientProtocolException;
 import org.junit.AfterClass;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import axirassa.model.PingerEntity;
 import axirassa.services.exceptions.AxirassaServiceException;
 import axirassa.services.pinger.HttpPinger;
 import axirassa.trigger.StatusCodeTrigger;
 import axirassa.trigger.Trigger;
 
 public class TestPingerFailureModes {
 
 	private static PingerTestServer server;
 
 
 	public static void assertTriggers (HttpPinger pinger, Class<? extends Trigger>... triggers) {
 		Class<? extends Trigger>[] actualTriggers = new Class[triggers.length];
 
 		int i = 0;
 		for (Trigger trigger : pinger.getTriggers())
 			actualTriggers[i] = trigger.getClass();
 
 		assertArrayEquals(triggers, actualTriggers);
 	}
 
 
 	public static PingerEntity testPinger (String url) {
 		PingerEntity entity = new PingerEntity();
 		entity.setUrl("http://localhost:" + server.PORT_NUMBER + url);
 
 		return entity;
 	}
 
 
 	@BeforeClass
 	public static void startTestServer () throws Exception {
 		server = new PingerTestServer();
 		server.start();
 	}
 
 
 	@AfterClass
 	public static void stopTestServer () throws Exception {
 		server.stop();
 	}
 
 
 	//
 	// TEST CASES
 	//
 
 	@Test
 	public void testUnknownHost () throws ClientProtocolException, IOException, AxirassaServiceException {
 		HttpPinger pinger = new HttpPinger();
		pinger.ping(testPinger("/status/403"));
		assertNotNull(pinger.getTrigger(StatusCodeTrigger.class));
		assertEquals(403, pinger.getTrigger(StatusCodeTrigger.class).getCode());
 
		// pinger.ping(testPinger())
 	}
 }
