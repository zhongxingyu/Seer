 package li.rudin.arduino.core.test;
 
 import java.util.List;
 
 import li.rudin.arduino.api.message.Message;
 import li.rudin.arduino.api.message.MessageParser;
 
 import org.junit.Assert;
 import org.junit.Test;
 
 public class MessageParserTest
 {
 	
 	@Test
 	public void testAssemble()
 	{
 		Assert.assertEquals("x:y;", new Message("x", "y").toString());
 	}
 	
 	@Test
 	public void testDisassembleSingle()
 	{
 		List<Message> list = MessageParser.parse("x:y;");
 		
 		Assert.assertEquals(1, list.size());
 		
 		Assert.assertEquals("x", list.get(0).key);
 		Assert.assertEquals("y", list.get(0).value);
 	}
 
 	@Test
 	public void testDisassembleSpaces()
 	{
 		List<Message> list = MessageParser.parse("x:   200.0;");
 		
 		Assert.assertEquals(1, list.size());
 		
 		Assert.assertEquals("x", list.get(0).key);
 		Assert.assertEquals(200.0, Double.parseDouble(list.get(0).value), 0);
 	}
 
 	@Test
 	public void testDisassembleRealData()
 	{
 		List<Message> list = MessageParser.parse("temperature:24.30;humidity:48.40;current:-27.48;");
 		
 		Assert.assertEquals(3, list.size());
 		
 		Assert.assertEquals("temperature", list.get(0).key);
 		Assert.assertEquals("24.30", list.get(0).value);
 		
 		Assert.assertEquals("humidity", list.get(1).key);
 		Assert.assertEquals("48.40", list.get(1).value);
 		
 		Assert.assertEquals("current", list.get(2).key);
 		Assert.assertEquals("-27.48", list.get(2).value);
 	}
 
 	@Test
 	public void testDisassembleKeyOnly()
 	{
 		List<Message> list = MessageParser.parse("x:;");
 		
 		Assert.assertEquals(1, list.size());
 		
 		Assert.assertEquals("x", list.get(0).key);
 		Assert.assertEquals("", list.get(0).value);
 	}
 
 	@Test
 	public void testDisassembleMultiple()
 	{
 		List<Message> list = MessageParser.parse("x:y;1:2;");
 		
 		Assert.assertEquals(2, list.size());
 		
 		Assert.assertEquals("x", list.get(0).key);
 		Assert.assertEquals("y", list.get(0).value);
 		
 		Assert.assertEquals("1", list.get(1).key);
 		Assert.assertEquals("2", list.get(1).value);
 	}
 
 	@Test
 	public void testDisassembleMultipleWithInvalid()
 	{
 		List<Message> list = MessageParser.parse("x:y;xxx;1:2;");
 		
 		Assert.assertEquals(2, list.size());
 		
 		Assert.assertEquals("x", list.get(0).key);
 		Assert.assertEquals("y", list.get(0).value);
 		
 		Assert.assertEquals("1", list.get(1).key);
 		Assert.assertEquals("2", list.get(1).value);
 	}
 
 	@Test
 	public void testDisassembleEmpty()
 	{
 		List<Message> list = MessageParser.parse("");
 		
 		Assert.assertEquals(0, list.size());
 		
 	}
 
 }
