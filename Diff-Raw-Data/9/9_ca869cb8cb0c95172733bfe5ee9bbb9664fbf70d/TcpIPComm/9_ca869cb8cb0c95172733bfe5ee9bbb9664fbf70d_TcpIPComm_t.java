 import java.io.BufferedReader;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.Socket;
 import java.net.UnknownHostException;
 
 class TCPClient	{
 	
 	//Socket parameters
 	private static Socket clientSocket;
 	static int receivedMessageLength, receivedMessageType;
 	// Handling the DataStreams
 	static DataOutputStream outToServer; 
 	static BufferedReader inFromServer; 
 	
 	
 	public TCPClient(String IP, int port) throws UnknownHostException, IOException{
 		// Handling the DataStreams
 		TCPClient.clientSocket = new Socket(IP, port);
 		this.outToServer = new DataOutputStream(getClientSocket().getOutputStream());
 		this.inFromServer = new BufferedReader(new InputStreamReader(getClientSocket().getInputStream()));
 		// Constants about Header and Message
 	}
 	
 	/*
 	 * Method to send a message to the server and wait for an answer
 	 */
 	public static void bidirectComm(String message, Integer messageType) throws Exception{
 		// Building the Header with the lenght of the message to send
 		// Needed Variables
 		String headerToSend, receivedHeader, receivedMessage ,headerToPad = "";
 		int messageLength;
 		
 		messageLength = message.length();
 		headerToPad = messageLength + "," + messageType;
 		
 		
 		// Header Building
 		headerToSend = paddingHeaderMessage(headerToPad);
 		System.out.println("Header to Send :" +headerToSend);
 		outToServer.writeBytes(headerToSend);
 		outToServer.writeBytes(message);
 		
 		receivedHeader = readHeader(inFromServer);
 		//receivedMessage = convertStreamToString(clientSocket.getInputStream());
 		System.out.println("Header From the Server: " + receivedHeader);
 		decodeHeader(receivedHeader);		
 		receivedMessage = readMessage(receivedMessageLength, inFromServer);
 		
 		System.out.println("Message From the Server: " + receivedMessage );	
 		}
 	/*
 	 * Method to establish communication with the server side. (Changing port system)
 	 */
 	public static void establishComm(String IP) throws IOException{	
 		int newPort;
 		String receivedHeader, receivedMessage;
 		receivedHeader = readHeader(inFromServer);
 		decodeHeader(receivedHeader);
 		if(receivedMessageType==3){
 			// Change communication to received port
 			receivedMessage = readMessage(receivedMessageLength, inFromServer);
 			getClientSocket().close();	
 			newPort = Integer.parseInt(receivedMessage);
 			System.out.println("New port at: "+newPort);
 			// Open a new socket with an assigned port
 			TCPClient.clientSocket = new Socket(IP, newPort);
 
 			outToServer = new DataOutputStream(getClientSocket().getOutputStream());
 			inFromServer = new BufferedReader(new InputStreamReader(getClientSocket().getInputStream()));			
 			receivedHeader = readHeader(inFromServer);
 			decodeHeader(receivedHeader);
 			// Read Server Response in order to know its status
 			receivedMessage = readMessage(receivedMessageLength, inFromServer);
 			if(receivedMessageType==4){
 				// Connection in good health
				System.out.println("Connection established succesfully");
 				System.out.println(receivedMessage);
 			} else if (receivedMessageType==5){
 				// There has been a problem with the server side
 				System.out.println("Error while connection stablishment");
 				System.out.println(receivedMessage);
 				// Sending response to close communication
 				
 			}
 		}
 	}
 
 
 	/*
 	 * Method to padding with 0's at the beginning of the header in order to send a 9 bytes header
 	 */	
 	private static String paddingHeaderMessage(String headerToPad){
 		while(headerToPad.length()<9){
 			headerToPad = "0" + headerToPad;
 			}
 		System.out.println(headerToPad);
 		System.out.println(headerToPad.length());
 		return headerToPad;
 	 }
 	
 	/*
 	 *  Method to read the header of a response from the socket
 	 */
 	private static String readHeader(BufferedReader inFromServer) throws IOException{
 		int i = 0, read;
 		String receivedHeader = "";
 		while(i<9){
 			read = inFromServer.read();
 			// Character.toString((char) read)) converts ASCII code to the associated character
 			receivedHeader = receivedHeader + Character.toString((char) read);
 			System.out.println("Header: "+ Character.toString((char) read));
 			i = i+1;
 		}
 		System.out.println("Lenght of received header is: "+ receivedHeader.length());
 		return receivedHeader;
 	}
 	
 	private static String readMessage(Integer messageLength, BufferedReader inFromServer) throws IOException{
 		int i = 0, read;
 		String receivedMessage = "";
 		while(i<messageLength){
 			read = inFromServer.read();
 			// Converts ASCII code to the associated character
 			receivedMessage = receivedMessage + Character.toString((char) read);
 			i = i + 1;
 		}
 		return receivedMessage;
 	}
 	
 	/*
 	 *  Method to split the two parts of a header "Length of Message" + "Type of Message" and parse them to Integers
 	 */
 	private static void decodeHeader(String header){
 		int separator;
 		String length, type;
 		separator = header.indexOf(",");
 		
 		length = header.substring(0, separator);
 		type = header.substring(separator+1, 9);
 		
 		receivedMessageLength = Integer.parseInt(length);
 		receivedMessageType = Integer.parseInt(type);		
 		
 		System.out.println("Decoded Header Length: " + receivedMessageLength + "\n Decoded Header Type: " + receivedMessageType);		
 	}
 
 
 	public static void close() throws IOException {
 		// TODO Auto-generated method stub
 		getClientSocket().close();
 	}
 
 	public static Socket getClientSocket() {
 		return clientSocket;
 	}
 
 	public void setClientSocket(Socket clientSocket) {
 		this.clientSocket = clientSocket;
 	}
 }
