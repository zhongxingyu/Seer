 import java.io.*;
 import java.net.*;
 
 public class BankServer {
    final static int portNumber = 80;
 
     public static void main(String[] args) throws IOException {
 
 	// listen on socket for network I/O
 	ServerSocket serverSocket = null;
 	try {
 	    serverSocket = new ServerSocket(portNumber);
 	} catch (IOException e) {
	    System.err.println("Listen on port: " + portNumber + " failed.");
 	    System.exit(-1);
 	}
 
 	// wait for a client connection
 	Socket clientSocket = null;
 	try {
 	    clientSocket = serverSocket.accept();
 	}
 	catch (IOException e) {
 	    System.out.println("Accept failed: " + portNumber);
 	    System.exit(-1);
 	}
 
 	// establish communication with client
 	DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());
 	DataInputStream in = new DataInputStream(clientSocket.getInputStream());
 
 	// initiate conversation with client (one at a time for now)
 	BankProtocol bp = new BankProtocol();
 	BankMessage inputMessage, outputMessage;
 
 	while((inputMessage = BankMessage.readMessage(in)) != null) {
 	    outputMessage = bp.processInput(inputMessage);
 	    outputMessage.writeMessage(out);
 	}
     }
 }
