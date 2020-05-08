 
 package test.axirassa.pinger;
 
 import static org.junit.Assert.assertArrayEquals;
 import static org.junit.Assert.assertEquals;
 
 import java.io.IOException;
 
 import org.apache.http.client.ClientProtocolException;
 import org.junit.AfterClass;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import axirassa.model.PingerEntity;
 import axirassa.services.exceptions.AxirassaServiceException;
 import axirassa.services.pinger.HttpPinger;
import axirassa.trigger.ProtocolErrorTrigger;
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
		Class<? extends StatusCodeTrigger> codeTrigger = StatusCodeTrigger.class;

 		HttpPinger pinger = new HttpPinger();
 
		// 403
		pinger.ping(testPinger("/status/403"));
		assertTriggers(pinger, StatusCodeTrigger.class);
		assertEquals(403, pinger.getTrigger(codeTrigger).getCode());

		// 200
		pinger.ping(testPinger("/status/200"));
		assertTriggers(pinger, StatusCodeTrigger.class);
		assertEquals(200, pinger.getTrigger(codeTrigger).getCode());

		// 301
		pinger.ping(testPinger("/status/301"));
		assertTriggers(pinger, ProtocolErrorTrigger.class);

		// 502
		pinger.ping(testPinger("/status/502"));
		assertTriggers(pinger, StatusCodeTrigger.class);
		assertEquals(502, pinger.getTrigger(codeTrigger).getCode());

		// 503
		pinger.ping(testPinger("/status/503"));
		assertTriggers(pinger, StatusCodeTrigger.class);
		assertEquals(503, pinger.getTrigger(codeTrigger).getCode());
 	}
 }
