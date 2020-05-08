 package StartUp;
 
 import java.io.IOException;
 import java.net.UnknownHostException;
 
 import Client.ClientApplication;
 
 public class ClientDriver {
 
	final static int PORT = 5000;
 	public static void main(String[]args) throws UnknownHostException, IOException{
		ClientApplication client = new ClientApplication("localhost",PORT);
 		client.start();
 	}
 }
