 package StartUp;
 
 import java.io.IOException;
 import java.net.UnknownHostException;
 
 import Client.ClientApplication;
 
 public class ClientDriver {
 
 	public static void main(String[]args) throws UnknownHostException, IOException{
		ClientApplication client = new ClientApplication("localhost",5000);
 		client.start();
 	}
 }
