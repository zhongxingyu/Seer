 package cs5204.fs.test;
 
 import cs5204.fs.client.SClient;
 import cs5204.fs.client.SFile;
 
 public class SimpleClient
 {
 	public static void main(String [] args)
 	{
         SClient client = new SClient("localhost", 2009);
 
         if (client.connect())
            System.out.println("Client connected.");
         else
			System.out.println("Connecting client failed! NOOOO!!!");
 		
 		if(client.createDirectory("/foo"))
 			System.out.println("Success in creating directory /foo!");
 		else
 			System.out.println("Failed to create /foo");
 			
 		SFile file = client.createFile("/foo/bar.txt");
 		
 		if (file != null)
 			System.out.println("Success in creating /foo/bar.txt!");
 		else
 			System.out.println("Failed to create /foo/bar.txt");
 
         client.disconnect();
 	}
 }
