 import java.util.HashMap;
 import java.util.Scanner;
 
 
 public class ControlTester {
 
 	/**
 	 * @param args
 	 */
 	private static int messageNumber = 0;
 	private static boolean emergencyStop = false;
 	private static boolean manual = false;
 	private static HashMap<String, Integer> internalVariableList;
 	public static void main(String[] args) {
 		Scanner scan = new Scanner(System.in); //replace the scanner with input from the control station once the control station is ready to be tested
 												// also replace output to the console with output to the control station
 		System.out.println("Enter \"manual\" for manual response mode, any other input will respond to the base station automatically");
 		String mode = scan.nextLine();
 		if(mode.equals("manual")) {
 			manual = true;
 		}
 		String input = scan.nextLine();
 		internalVariableList = new HashMap<String, Integer>();
 		internalVariableList.put("messageNumber", messageNumber);
 		while(!input.equals("disconnect")) {
 			if(manual == false) {
 				int calculatedChecksum = 0;
 				calculatedChecksum = calculateChecksum(input.substring(3, input.length()));
 				int receivedChecksum = 256*((int)input.charAt(1)) + (int)input.charAt(2);
 				if(!(input.charAt(0) == '#') || calculatedChecksum != receivedChecksum || !(input.charAt(3) == 'S')
 						|| !(input.charAt(input.length() - 1) == (char)0)) { //check the message formatting, evaluates to true if the message is invalid
 					incrementMessageNumber();
 					System.out.println(malformedInputResponse(input));
 				}
 				else {
 					if(input.length() > 8) {
 						switch(input.charAt(8)) { //the functions called in each case are responsible for checking the validity of the parameters.
 						case 'A': moveOrTurn(input);
 						break;
 						case 'B': moveOrTurn(input);
 						break;
 						case 'C': moveOrTurn(input);
 						break;
 						case 'D': moveOrTurn(input);
 						break;
 						case 'E': OpcodeE(input);
 						break;
 						case 'F': OpcodeF(input);
 						break;
 						case 'G': OpcodeG(input);
 						break;
 						case 'H': OpcodeH(input);
 						break;
 						case 'I': OpcodeI(input);
 						break;
 						case 'J': OpcodeJ(input);
 						break;
 						case 'K': OpcodeK(input);
 						break;
 						case 'L': OpcodeL(input);
 						break;
 						case 'M': OpcodeM(input);
 						break;
 						case 'N': OpcodeN(input);
 						break;
 						case 'O': OpcodeO(input);
 						break;
 						case 'P': OpcodeP(input);
 						break;
 						case 'Q': OpcodeQ(input);
 						break;
						case 'R': OpcodeR(input);
 						break;
						case 'S': OpcodeS(input);
 						break;
 						default: System.out.println(malformedInputResponse(input)); // executes if the opcode character is not a valid opcode
 						incrementMessageNumber();
 						break;
 
 
 						}
 					}
 					else {
 						//if the input length is <=8 then the message is unreadable
 						incrementMessageNumber();
 						System.out.println(unreadableInputResponse(input));
 
 					}
 				}
 			}
 			else {
 				String response = scan.nextLine(); //manual input to test the control stations response to malformed inputs
 				System.out.println(response);
 			}
 			input = scan.nextLine();
 		}
 
 	}
 	private static void incrementMessageNumber() {
 		messageNumber++;
 		internalVariableList.put("messageNumber", messageNumber);
 	}
 	private static void moveOrTurn(String input) {
 		if(input.length() == 18) {
 			System.out.println(generateCommandAck(input));
 			incrementMessageNumber();
 			System.out.println(generateExecutionResponse(input));
 			incrementMessageNumber();
 		}
 		else {
 			System.out.println(malformedInputResponse(input));
 			messageNumber++;
 			internalVariableList.put("messageNumber", messageNumber);
 		}
 	}
 	private static void OpcodeE(String input) {
 		if(input.length() == 15) {
 			System.out.println(generateCommandAck(input));
 			incrementMessageNumber();
 			System.out.println(generateExecutionResponse(input));
 			incrementMessageNumber();
 		}
 		else {
 			System.out.println(malformedInputResponse(input));
 			incrementMessageNumber();
 		}
 	}
 	private static void OpcodeF(String input) {
 		if(input.length() == 14) {
 			System.out.println(generateCommandAck(input));
 			incrementMessageNumber();
 			System.out.println(generateExecutionResponse(input));
 			incrementMessageNumber();
 		}
 		else {
 			System.out.println(malformedInputResponse(input));
 			incrementMessageNumber();
 		}
 	}
 	private static void OpcodeG(String input) {
 		if(input.length() == 14) {
 			System.out.println(generateCommandAck(input));
 			incrementMessageNumber();
 			emergencyStop = true;
 			System.out.println(generateExecutionResponse(input));
 			incrementMessageNumber();
 		}
 		else {
 			System.out.println(malformedInputResponse(input));
 			incrementMessageNumber();
 		}
 	}
 	private static void OpcodeH(String input) {
 		if(input.length() == 14) {
 			System.out.println(generateCommandAck(input));
 			incrementMessageNumber();
 			emergencyStop = false;
 			System.out.println(generateExecutionResponse(input));
 			incrementMessageNumber();
 		}
 		else {
 			System.out.println(malformedInputResponse(input));
 			incrementMessageNumber();
 		}
 	}
 	private static void OpcodeI(String input) {
 		if(input.length() == 14) {
 			System.out.println(generateCommandAck(input));
 			incrementMessageNumber();
 			resetVariables();
 			System.out.println(generateExecutionResponse(input));
 			incrementMessageNumber();
 		}
 		else {
 			System.out.println(malformedInputResponse(input));
 			incrementMessageNumber();
 		}
 	}
 	public static void OpcodeJ(String input) {
 		System.out.println(generateCommandAck(input));
 		incrementMessageNumber();
 		String variableName = input.substring(9, input.length() - 1);
 		int var = internalVariableList.get(variableName);
 		String messageNum = format4ByteNumber(messageNumber);
 		String response = "R" + messageNum + "R" + format4ByteNumber(var) + (char)0;
 		int checksum = calculateChecksum(response);
 		System.out.println("#" + formatChecksum(checksum) + response);
 
 	}
 	public static void OpcodeK(String input) {
 		System.out.println(malformedInputResponse(input));  //opcode K is for system status going from robot to base station, robot can't receive a packet with opcode K
 	}
 	public static void OpcodeL(String input) {
 		System.out.println(malformedInputResponse(input));  //opcode L is for execution response going from robot to base station, robot can't receive a packet with opcode L
 	}	
 	public static void OpcodeM(String input) {
 		System.out.println(malformedInputResponse(input));  //opcode M is for execution errors going from robot to base station, robot can't receive a packet with opcode M
 	}
 	public static void OpcodeN(String input) {
 		System.out.println(malformedInputResponse(input));  //opcode N is for command acknowledgments going from robot to base station, robot can't receive a packet with opcode N
 	}
 	public static void OpcodeO(String input) {
 		//no response is necessary when receiving an acknowledgment
 	}
 	public static void OpcodeP(String input) {
 		//no response is necessary when receiving an acknowledgment
 	}
 	public static void OpcodeQ(String input) {
 		//no response is necessary when receiving an acknowledgment
 	}
 	public static void OpcodeR(String input) {
 		System.out.println(malformedInputResponse(input));  //opcode R is for internal variables going from robot to base station, robot can't receive a packet with opcode R
 	}
 	public static void OpcodeS(String input) {
 		String messageNum = format4ByteNumber(messageNumber);
 		int ultrasound = (int)Math.random()*256;
 		int light = (int)Math.random()*256;
 		int sound = (int)Math.random()*256;
 		int touchA = (int)Math.random()*2;
 		int touchB = (int)Math.random()*2;
 		int battery = (int)Math.random()*100;
 		int signalStrength = (int)Math.random()*100;
 		String response = "R" + messageNum + "K" + format4ByteNumber(ultrasound) + format4ByteNumber(light) + format4ByteNumber(sound) + (char)touchA + (char)touchB +
 				format4ByteNumber(battery) + format4ByteNumber(signalStrength) + (char)0;
 		int checksum = calculateChecksum(response);
 		System.out.println("#" + formatChecksum(checksum) + response);
 		incrementMessageNumber();
 	}
 	public static int calculateChecksum(String message) {
 		int checksum = 0;
 		for(int i = 0; i < message.length(); i++) {
 			checksum += (int)message.charAt(i);
 		}
 		return checksum;
 	}
 	private static String format4ByteNumber(int messageNumber) {
 		return "" + (char)((messageNumber/16777216)%256) + (char)((messageNumber/65536)%256) +
 				(char)((messageNumber/256)%256) + (char)((messageNumber)%256);
 	}
 	private static String formatChecksum(int checksum) {
 		return "" + (char)((checksum/256)%256) + (char)(checksum %256);
 	}
 	private static String malformedInputResponse(String input) {
 		String messageNum = format4ByteNumber(messageNumber);
 		String response = "R" + messageNum + "M" + "h" + input.substring(3, 8) + (char)0;
 		int checksum = calculateChecksum(response);
 		return "#" + formatChecksum(checksum) + response;
 	}
 	private static String unreadableInputResponse(String input) {
 		String messageNum = format4ByteNumber(messageNumber);
 		String response = "R" + messageNum + "M" + "i" + (char)0;
 		int checksum = calculateChecksum(response);
 		return "#" + formatChecksum(checksum) + response;
 	}
 	private static String generateCommandAck(String input) {
 		String messageNum = format4ByteNumber(messageNumber);
 		String response = "R" + messageNum + "N" + input.substring(3, 8) + (char)0;
 		int checksum = calculateChecksum(response);
 		return "#" + formatChecksum(checksum) + response;
 	}
 	private static String generateExecutionResponse(String input) {
 		String messageNum = format4ByteNumber(messageNumber);
 		double random = Math.random();
 		String status = "";
 		if(random < 0.8) {
 			status = "S";
 		}
 		else {
 			status = "F";
 		}
 		String response = "R" + messageNum + "L" + input.substring(3, 8) + status + (char)0;
 		int checksum = calculateChecksum(response);
 		return "#" + formatChecksum(checksum) + response;
 	}
 	private static void resetVariables() {
 		emergencyStop = false;
 		messageNumber = 0;
 	}
 }
