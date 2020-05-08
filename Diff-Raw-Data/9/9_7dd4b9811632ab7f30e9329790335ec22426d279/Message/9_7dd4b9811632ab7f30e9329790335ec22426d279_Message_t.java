 package edu.cs319.connectionmanager.messaging;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import edu.cs319.util.Util;
 
 public class Message {
 	private MessageType messageType;
 	private String clientName;
 	private List<String> arguments;
 
 	public Message(MessageType m, String clientName, List<String> args) {
 		this.messageType = m;
 		this.arguments = args;
 		this.clientName = clientName;
 	}
 
 	public String getSentByClientName() {
 		return clientName;
 	}
 
 	public MessageType getMessageType() {
 		return messageType;
 	}
 
 	public List<String> getArgumentList() {
 		return arguments;
 	}
 
 	public byte[] encode() {
 		byte messageCode = messageType.getCode();
 		byte[] clietNameBytes = clientName.getBytes();
 		byte[] args = this.argsToByteArr();
 		// message type byte + clinet name bytes + delimmiter + args bytes + end byte
 		int lenghtNeeded = 1 + clietNameBytes.length + 1 + args.length + 1;
 		byte[] ret = new byte[lenghtNeeded];
 		int insertPos = 0;
 		ret[insertPos] = messageCode;
 		insertPos++;
 		System.arraycopy(clietNameBytes, 0, ret, insertPos, clietNameBytes.length);
 		insertPos += clietNameBytes.length;
 		ret[insertPos] = Byte.MIN_VALUE;
 		insertPos++;
 		System.arraycopy(args, 0, ret, insertPos, args.length);
 		ret[ret.length - 1] = Byte.MAX_VALUE;
 		return ret;
 	}
 
 	public static Message decode(byte[] info) {
 		MessageType mtype = MessageType.getMessageTypeByCode(info[0]);
 		if (mtype == null) {
 			if (Util.DEBUG) {
 				System.out.println("Message type did not exist.  Byte: " + info[0]);
 			}
 			throw new IllegalArgumentException();
 		}
 		int indexOfFirstDelim = 0;
 		for (int i = 1; i < info.length; i++) {
 			if (info[i] == Byte.MIN_VALUE) {
 				indexOfFirstDelim = i;
 				break;
 			}
 		}
 		if (indexOfFirstDelim == 0)
 			throw new IllegalMessageFormatException();
 		byte[] clientBytes = Arrays.copyOfRange(info, 1, indexOfFirstDelim);
 		String clientName = new String(clientBytes);
 		// skip delimiters
 		byte[] argBytes = Arrays.copyOfRange(info, clientBytes.length + 2, info.length);
 		List<String> args = Message.getArgsFromBytes(argBytes);
 		return new Message(mtype, clientName, args);
 	}
 
 	private byte[] argsToByteArr() {
 		byte[][] brokenRet = new byte[arguments.size()][];
 		for (int i = 0; i < arguments.size(); i++) {
 			if (arguments.get(i) != null) {
 				brokenRet[i] = arguments.get(i).getBytes();
			} else {
 				brokenRet[i] = new byte[1];
 			}
 		}
 		int lengthNeeded = 0;
 		for (byte[] bs : brokenRet) {
 			lengthNeeded += bs.length;
 			lengthNeeded++; // need the delimmiter
 		}
 		byte[] ret = new byte[lengthNeeded];
 		int offset = 0;
 		int numDone = 0;
 		while (numDone < brokenRet.length) {
 			if (Util.DEBUG) {
 				System.out.println("Encoding message, offest ammnt: " + offset);
 			}
 			System.arraycopy(brokenRet[numDone], 0, ret, offset, brokenRet[numDone].length);
 			offset += brokenRet[numDone].length;
 			ret[offset] = Byte.MIN_VALUE;
 			offset++;
 			numDone++;
 		}
 		return ret;
 	}
 
 	@Override
 	public String toString() {
 		return "MessageType: " + messageType + ", Client Name: " + clientName + ", Arguments: "
 				+ arguments.toString();
 	}
 
 	private static List<String> getArgsFromBytes(byte[] argBytes) {
 		List<String> ret = new ArrayList<String>();
 		int start = 0, end = Message.getNextDelimiterPos(argBytes, 0);
 		while (end != -1) {
			if (end - start == 1 && argBytes[start] == 0) {
				ret.add(null);
			} else {
				ret.add(new String(Arrays.copyOfRange(argBytes, start, end)));
			}
 			start = ++end;
 			end = Message.getNextDelimiterPos(argBytes, start);
 		}
 		return ret;
 	}
 
 	private static int getNextDelimiterPos(byte[] arr, int start) {
 		int ret = -1;
 		for (int i = start; i < arr.length; i++) {
 			if (arr[i] == Byte.MIN_VALUE)
 				return i;
 		}
 		return ret;
 	}
 
 	public static void main(String[] args) {
 		List<String> args2 = new ArrayList<String>() {
 			{
 				add("sadfghjkl");
 			}
 		};
 		Message m = new Message(MessageType.NEW_MESSAGE, "jjnguy", args2);
 		System.out.println(m);
 		System.out.println(Message.decode(m.encode()));
 	}
 }
