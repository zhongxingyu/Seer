 package directtalkserver;
 
 import java.io.IOException;
 import java.io.OutputStream;
 import java.net.ServerSocket;
 import java.net.Socket;
 
 public class MainClass
 {
 	public static void main(String[] args)
 	{
 		ServerSocket ssocket = null;
 		Socket connection = null;
 		OutputStream client = null;
 		int ch = 0;
 
 		if (args.length != 1)
 		{
 			System.out.println("Wrong number of args (takes 1).");
 			return;
 		}
 		try
 		{
 			ssocket = new ServerSocket(Integer.parseInt(args[0]));
 		}
 		catch (IOException | NumberFormatException e)
 		{
 			e.printStackTrace();
 			return;
 		}
 
 		try
 		{
 			System.out.println("Listening on port 4444 (single connection).");
 			connection = ssocket.accept();
 			client = connection.getOutputStream();
 			System.out.println("Got Connection: "
 					+ connection.getInetAddress().getHostName());
 			while (true)
 			{
 				ch = (int) (Math.random() * 255);
 				System.out.println("Sending random byte " + ch);
 				client.write(ch);
				client.flush();
 				Thread.sleep(1000);
 			}
 		}
 		catch (IOException e)
 		{
			e.printStackTrace();
 		}
 		catch (InterruptedException e)
 		{
			e.printStackTrace();
 		}
 
 	}
 }
