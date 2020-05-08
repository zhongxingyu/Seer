 package de.fernuni.pi3.interactionmanager;
 
 import static org.junit.Assert.assertEquals;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 
 import junit.framework.Assert;
 
 import org.junit.Test;
 
 public class EventTest {
 
 	private static final String EVENT_ID = "__ID__";
 	private static final String EVENT_NAME = "testevent";
 	private static final String VAR_VALUE = "testVarValue";
 	private static final String PROP_VALUE = "testPropValue";
 	private static final String VAR_KEY = "testVarKey";
 	private static final String PROP_KEY = "testPropKey";
 	private static final String APP_INSTANCE_ID = "appInstanceId";
 	private static final String APP_TYPE = EventTest.class
 			.getName();
 	private final static String TEST_EVENT_JSON = "{\"id\":\"__ID__\",\"name\":\"testevent\","
			+ "\"appType\":\"de.fernuni.pi3.interactionmanager.InteractionManagerEventTest\","
 			+ "\"appInstanceId\":\"appInstanceId\","
 			+ "\"properties\":{\"testPropKey\":\"testPropValue\",\"varKeyMap\":{\"varSubKeyMap\":\"varSubValueMap\"}},"
 			+ "\"customVars\":{\"testVarKey\":\"testVarValue\",\"varKeyMap\":{\"varSubKeyMap\":\"varSubValueMap\"}}}";
 
 	@Test
 	public void testToJson() throws Exception {
 
 		Event event = new Event(EVENT_NAME);
 
 		event.setAppInstanceId(APP_INSTANCE_ID);
 		event.setAppType(APP_TYPE);
 		event.setProperty(PROP_KEY, PROP_VALUE);
 		event.setCustomVar(VAR_KEY, VAR_VALUE);
 
 		HashMap<String, String> customVarMap = new HashMap<String, String>();
 		customVarMap.put("varSubKeyMap", "varSubValueMap");
 		event.setCustomVar("varKeyMap", customVarMap);
 		event.setProperty("varKeyMap", customVarMap);
 		assertEquals("Unexpected JSON string", TEST_EVENT_JSON.replace(EVENT_ID, event.getId()), event.toJson());
 	}
 
 	@Test
 	public void testId() throws Exception {
 		ArrayList<String> ids = new ArrayList<String>();
 		for (int i = 0; i < 100; i++) {
 			Event event = new Event(EVENT_NAME);
 			
 			Assert.assertFalse(event.getId().isEmpty());
 			Assert.assertFalse(ids.contains(event.getId()));
 			
 			ids.add(event.getId());
 		}
 	}
 	
 	@Test
 	@SuppressWarnings("unchecked")
 	public void testFromJson() throws Exception {
 
 		Event event = Event
 				.fromJson(TEST_EVENT_JSON);
 		Map<String, String> customVarMap = (Map<String, String>) event
 				.getCustomVar("varKeyMap");
 		Map<String, String> propertiesMap = (Map<String, String>) event
 				.getCustomVar("varKeyMap");
 
 		assertEquals("varSubValueMap", customVarMap.get("varSubKeyMap"));
 		assertEquals("varSubValueMap", propertiesMap.get("varSubKeyMap"));
 		assertEquals(APP_INSTANCE_ID, event.getAppInstanceId());
 		assertEquals(APP_TYPE, event.getAppType());
 		assertEquals(EVENT_ID, event.getId());
 		assertEquals(PROP_VALUE, event.getProperty(PROP_KEY));
 		assertEquals(VAR_VALUE, event.getCustomVar(VAR_KEY));
 
 	}
 
 }
