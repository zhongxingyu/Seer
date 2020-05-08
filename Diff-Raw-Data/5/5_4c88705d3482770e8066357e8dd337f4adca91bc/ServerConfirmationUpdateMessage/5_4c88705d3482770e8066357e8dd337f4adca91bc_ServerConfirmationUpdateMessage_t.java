 package Messages;
 
 public class ServerConfirmationUpdateMessage extends Message {
 	String confirmedUsername = null;
 	String senderIP = null;
 
 	public static final long minSize = 143;
 
 	public ServerConfirmationUpdateMessage(int _op, long _length,
 			long _reserved, String _options, byte[] body) {
 		super(_op, _length, _reserved, _options);
 		processBody(body);
 		if (op != 7) {
 			correct = false;
 		}
 	}
 
 	public ServerConfirmationUpdateMessage(int _op, long _length,
 			long _reserved, String _options, String _confirmedUsername,
 			String _senderIP) {
 		super(_op, _length, _reserved, _options);
 		confirmedUsername = _confirmedUsername;
 		senderIP = _senderIP;
 		if (op != 7) {
 			correct = false;
 		}
 	}
 
 	private void processBody(byte[] body) {
 		if (body.length != 143) {
 			correct = false;
 			return;
 		}
 		byte[] confirmedUserArray = new byte[128];
		for (int i = 0; i < confirmedUserArray.length; i++) {
 			confirmedUserArray[i] = body[i];
 		}
 		confirmedUsername = new String(confirmedUserArray, 0,
 				confirmedUserArray.length);
 		
 		int offset = 128;
 
 		byte[] senderIPArray = new byte[15];
		for (int i = 0; i < senderIPArray.length && i < 15; i++) {
 			senderIPArray[i] = body[i + offset];
 		}
 
 		senderIP = new String(senderIPArray, 0, senderIPArray.length);
 		
 	}
 
 	public byte[] convert() {
 		byte[] upper = super.convert();
 		byte[] storage = new byte[(int) (upper.length + minSize)];
 		for (int i = 0; i < upper.length; i++) {
 			storage[i] = upper[i];
 		}
 
 		int total = upper.length;
 
 		byte[] tmp = null;
 
 		tmp = confirmedUsername.getBytes();
 		for (int i = 0; i < tmp.length; i++) {
 			storage[total + i] = tmp[i];
 		}
 
 		total += 128;
 
 		tmp = senderIP.getBytes();
 		for (int i = 0; i < tmp.length; i++) {
 			storage[total + i] = tmp[i];
 		}
 		
 		total += 15;
 		
 		return storage;
 	}
 }
