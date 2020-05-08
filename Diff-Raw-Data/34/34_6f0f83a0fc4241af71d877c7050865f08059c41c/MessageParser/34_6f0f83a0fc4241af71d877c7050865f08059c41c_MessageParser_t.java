 package li.rudin.arduino.api.message;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class MessageParser
 {
 	/**
 	 * Parses a message from a byte array
 	 * @param bytes
 	 * @param length
 	 * @return
 	 */
 	public static List<Message> parse(byte[] bytes, int length)
 	{
 		return parse(new String(bytes, 0, length));
 	}
 	
 	/**
 	 * Parses a message from a string
 	 * @param msg
 	 * @return
 	 */
 	public static List<Message> parse(String msg)
 	{
 		ArrayList<Message> list = new ArrayList<>();
 		String[] parts = msg.split("[;]");
 		
 		for (String part: parts)
 		{
 			Message message = parseMessage(part);
 			if (message != null)
 				list.add(message);
 		}
 		
 		return list;
 	}
 	
 	/**
 	 * Parses a single message
 	 * @param msg
 	 * @return
 	 */
 	private static Message parseMessage(String msg)
 	{
		int delimPos = msg.indexOf(":");
 		
		if (delimPos < 0)
 			return null;

		String key = msg.substring(0, delimPos);
		String value = msg.substring(delimPos+1);
		
		return new Message(key, value);
 	}
 }
