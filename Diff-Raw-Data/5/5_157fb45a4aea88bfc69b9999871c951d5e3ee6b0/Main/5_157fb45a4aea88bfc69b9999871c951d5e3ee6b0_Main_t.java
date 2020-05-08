 import java.io.BufferedOutputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.DatagramPacket;
 import java.net.DatagramSocket;
 import java.net.InetAddress;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 
 import javax.swing.JFrame;
 
 public class Main
 {
 	public static String transferType = "TCP";
 	
 	public static boolean connectionGUIStatus = false;
 
 	public static ServerSocket serverSocket;
 	public static String clientMessage;
 	static DatagramSocket UDPSocket;
 	public static BufferedOutputStream bOut;
 	public static InputStream inStream;
 	public static ByteArrayOutputStream baos;
 	public static int numBytes = 0;
 	
 	public static int userId = 0;
 	
 	public static ArrayList<User> userList = new ArrayList<User>();
 	public static ArrayList<String> ips = new ArrayList<String>();
 	public static ArrayList<ConnectionThread> clientThreads = new ArrayList<ConnectionThread>();
 	
 	public static void main(String[] args)
 	{
 		ConnectionGUI cGUI = new ConnectionGUI();
 		
 		cGUI.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		cGUI.setSize(300, 120);
 		cGUI.setResizable(false);
 		cGUI.setVisible(true);
 		
 		while(!connectionGUIStatus)
 		{
 			try { Thread.sleep(1);}
 			catch (InterruptedException e1) { e1.printStackTrace(); }
 		}
 		
 		if(transferType.equals("TCP"))
 		{
 			try
 			{
 				serverSocket = new ServerSocket(Integer.parseInt(Resource.PORT));
 			}
 			catch (Exception e) { e.printStackTrace(); }
 			
 			System.out.println("TCPServer Listening on Port: " + Resource.PORT);
 	
 			while(true) 
 			{
 				try
 				{
 					//System.out.println(serverSocket.getInetAddress().toString());
 					if(serverSocket.getInetAddress().toString() != "0.0.0.0/0.0.0.0" && !ips.contains(serverSocket.getInetAddress().toString()))
 					{
 						clientThreads.add(new ConnectionThread(++userId, serverSocket.accept()));
 						ips.add(serverSocket.getInetAddress().toString().split("/")[0]);
 					}
 				}
 				catch (Exception e) { e.printStackTrace(); }
 			}
 		}
 		else
 		{
 			try
 			{
 				UDPSocket = new DatagramSocket(8015);
 				byte[] receiveData = new byte[1024];
 				byte[] sendData = new byte[1024];
 				
 				while(true)
 				{
 					receiveData = new byte[1024];
 					DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
 					UDPSocket.receive(receivePacket);
 					clientMessage = new String(receivePacket.getData()).trim();
 					System.out.println(clientMessage);
 					if(receivePacket.getData().equals(null))
 					{
 							try { Thread.sleep(1);}
 							catch (InterruptedException e1) { e1.printStackTrace(); }
 					}
 					else if(clientMessage.contains("/connected"))
 					{
 						userList.add(new User(++userId, new String(receiveData).substring(11), receivePacket.getAddress().toString().substring(1)));
 						System.out.println(receivePacket.getAddress());
 						UDPSocket.send(new DatagramPacket(("/id " + String.valueOf(userId)).getBytes(), ("/id " + String.valueOf(userId)).getBytes().length,receivePacket.getAddress(),Integer.parseInt(Resource.UPORT)));
 						writeToAll("/userlist " + getUserList());
 					}
 					else if(clientMessage.contains("/name"))
 					{
 						writeToAll("/console ** " + getUserFromId(Integer.parseInt((clientMessage.split(" ")[1]).split("\\\\")[0])).trim() + " CHANGED THEIR NAME TO " + parseName(clientMessage) + " **");
 						writeToAll("/update " + updateUser(clientMessage));
 					}
 					else if(clientMessage.contains("/disconnect"))
 					{
 						writeToAll("/remove " + removeUser(clientMessage));
 						for(int i = 0; i < userList.size(); i++)
 						{
 							if(userList.get(i).getIp().equals(receivePacket.getAddress().toString().substring(1)))
 								userList.remove(i);
 						}
 					}
 					else
 						writeToAll("/msg " + clientMessage);
 				}
 			}
 			catch (Exception e) { e.printStackTrace(); }
 		}
 	}
 	
 	public static String getUserList()
 	{
 		String userStr = "";
 		
 		for(int i = 0; i < userList.size(); i++)
 		{
 			int id = userList.get(i).getId();
			String name = userList.get(i).getName().trim();
 
			userStr = userStr + String.valueOf(id) + "\\" + name;
 			if((i+1) < userList.size())
 				userStr = userStr + "\\";
 		}
 
 		return userStr;
 	}
 	
 	public static String updateUser(String userString)
 	{
 		userString = userString.substring(6);
 		int id = Integer.parseInt(userString.split("\\\\")[0]);
 		String username = userString.split("\\\\")[1];
 		if(username.charAt(0) == '"')
 			username = username.substring(1, username.length()-1);
 
 		String outputStr = "";
 
 		for(int i = 0; i < userList.size(); i++)
 		{
 			if(userList.get(i).getId() == id)
 			{
 				userList.get(i).setName(username);
 				outputStr = userList.get(i).getId() + "\\" + userList.get(i).getName();
 				break;
 			}
 		}
 
 		return outputStr;
 	}
 	
 	public static String removeUser(String id)
 	{
 		for(int i = 0; i < userList.size(); i++)
 		{
 			if(userList.get(i).getId() == Integer.parseInt(id.split(" ")[1]))
 			{
 				userList.remove(i);
 				break;
 			}
 		}
 		
 		return id.split(" ")[1];
 	}
 	
 	public static String getUserFromId(int id)
 	{
 		for(int i = 0; i < userList.size(); i++)
 		{
 			if(userList.get(i).getId() == id)
 				return userList.get(i).getName();
 		}
 		
 		return null;
 	}
 	
 	public static void receiveAndBounceMessage(String incomingMessage, Socket socket)
 	{
 		incomingMessage = incomingMessage.substring(6);
 		ServerSocket temp = null;
 		int id = Integer.parseInt(incomingMessage.split("\\\\")[0]);
 		String fileName = incomingMessage.split("\\\\")[1];
 		writeToAll("/file " + id + "\\" + fileName);
 		byte[] incomingBytes = new byte[1];
 		try
 		{
 			temp = new ServerSocket(Integer.parseInt(Resource.PORT)+10);
 			socket = temp.accept();
 			inStream = socket.getInputStream();
 		}
 		catch(Exception e) { e.printStackTrace(); }
 		
 		baos = new ByteArrayOutputStream();
 		try
 		{
 			numBytes = inStream.read(incomingBytes, 0, incomingBytes.length);
 			do
 			{
 				baos.write(incomingBytes);
 				numBytes = inStream.read(incomingBytes);
 			} while (numBytes != -1);
 			inStream.close();
 			socket.close();
 			temp.close();
 		}
 		catch(IOException ex) { ex.printStackTrace(); }
 		
 		for(int i = 0; i < userList.size(); i++)
 		{
 			try 
 			{
 				socket = new Socket(clientThreads.get(i).IP.substring(1),Integer.parseInt(Resource.PORT) + clientThreads.get(i).ID);
 				bOut = new BufferedOutputStream(socket.getOutputStream());
 			} 
 			catch (IOException e1) { e1.printStackTrace(); }
 			if (bOut != null)
 			{
 				byte[] outArray = baos.toByteArray();
 	            try 
 	            {
 	                bOut.write(outArray, 0, outArray.length);
 	                bOut.flush();
 	                bOut.close();
 	                socket.close();
 	            } catch (IOException ex) { ex.printStackTrace(); }
 			}
 		}
 	}
 	
 	public static void writeToAll(String message)
 	{
 		if (transferType.equals("UDP"))
 		{
 			for(int i = 0; i < userList.size(); i++)
 			{
 				try {
 					UDPSocket.send(new DatagramPacket(message.getBytes(),message.getBytes().length,InetAddress.getByName(userList.get(i).getIp()),Integer.parseInt(Resource.UPORT)));
 				} catch (Exception e){ e.printStackTrace(); }
 			}
 		}
 		else
 		{
 			for (int i = 0; i < clientThreads.size(); i++)
 			{
 				clientThreads.get(i).writeToClient(message);
 			}
 		}
 		
 	}
 	
 	public static String parseName(String clientMessage)
 	{
 		clientMessage = clientMessage.substring(6);
 		// 1\\"name"
 		clientMessage = clientMessage.split("\\\\")[1];
 		if(clientMessage.charAt(0) == '"')
 			clientMessage = clientMessage.substring(1, clientMessage.length()-1);
 		
 		return clientMessage.replace("/", "");
 	}
 }
