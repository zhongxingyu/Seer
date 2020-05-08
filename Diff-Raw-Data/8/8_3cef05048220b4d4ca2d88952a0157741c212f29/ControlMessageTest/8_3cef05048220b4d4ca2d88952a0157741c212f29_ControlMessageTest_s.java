 package raisa.comms;
 
 import static org.junit.Assert.assertEquals;
 
 import org.junit.Test;
 
 public class ControlMessageTest {
	private ControlMessage controlMessage = new ControlMessage(5, -4, true);
 	{
 		controlMessage.setTimestamp(10l);
 	}
 
 	@Test
 	public void jsonSerialization() {
		String value = "{\"leftSpeed\":5,\"rightSpeed\":-4,\"lights\":true,\"timestamp\":10}";
 		assertEquals(value, controlMessage.toJson());
 	}
 
 	@Test
 	public void jsonDeserialization() {
		String value = "{\"leftSpeed\":5,\"rightSpeed\":-4,\"lights\":true,\"timestamp\":10,\"unsupported-field\": 'abc'}";
 		assertEquals(controlMessage, ControlMessage.fromJson(value));
 	}
 
 }
