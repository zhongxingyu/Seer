 package communication;
 
 public class Serializer {
 
 	public static String serialize(Message msg) {
 		String s = "";
 		s += "type:"+msg.getType()+";";
 		s += "to:"+msg.getTo()+";";
 		s += "from:"+msg.getFrom()+";";
 		s += "data;";
 		for (String key : msg.getKeys()) {
 			s += key+":";
 			s += msg.getData(key)+";";
 		}
 		return s;
 	}
 	
 	public static Message deSerialize(String msg) {
 		String type, to, from;
 		type = find("type", msg);
 		to = find("to", msg);
 		from = find("from", msg);
 		Message m = new Message(type, to, from);
 		int index2 = msg.indexOf("data;")+5;
 		while (index2 < msg.length()-1) {
 			int index1 = msg.indexOf(":", index2);
 			String key = msg.substring(index2, index1);
 			m.addData(key, find(key, msg));
 			index2 = msg.indexOf(";", index1)+1;
 		}
 		return m;
 	}
 	
 	private static String find(String key, String msg) {
 		int index1 = -1;
 		String value = null;
		if ((index1 = msg.indexOf(key)) >= 0) {
 			int index2 = msg.indexOf(";", index1);
 			value = msg.substring(index1+key.length()+1, index2);
 		}
 		return value;
 	}
 }
