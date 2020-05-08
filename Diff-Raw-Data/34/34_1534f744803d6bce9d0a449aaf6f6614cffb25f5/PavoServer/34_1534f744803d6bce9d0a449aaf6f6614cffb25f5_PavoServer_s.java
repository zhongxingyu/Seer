 package com.jpii.navalbattle.pavo.io;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.util.ArrayList;
 
 import com.jpii.navalbattle.pavo.Game;
 
 public class PavoServer implements Runnable {
 	ServerSocket socket;
 	Socket client;
 	boolean doing = false;
 	boolean autoReboot = true;
 	String ipaddress = null;
 	Thread self;
 	BufferedReader reader;
 	PrintWriter output;
 	String clientAddress = null;
 	boolean noInternet = false;
 	ArrayList<NetAttribute> nas;
 	public PavoServer() {
 		nas = new ArrayList<NetAttribute>();
 	}
 	
 	public String getSelfIP() {
 		return ipaddress;
 	}
 	
 	public boolean isAutoRestarting() {
 		return autoReboot;
 	}
 	
 	public void setRebootOnDisconnect(boolean s) {
 		autoReboot = s;
 	}
 	
 	public boolean start() {
 		if (doing) {
 			System.out.println("Server already runnning!");
 			return false;
 		}
 		if (ipaddress == null) {
 			Socket s;
 			try {
 				s = new Socket("google.com", 80);
 				ipaddress = (s.getLocalAddress().getHostAddress());
 				if (ipaddress == null) {
 					noInternet = true;
 					ipaddress = "None";
 				}
 				s.close();
 			}
 			catch (Throwable t) {
 				
 			}
 			if (!noInternet)
 				Game.Settings.currentNetworkState = NetworkState.CONNECTED_TO_NETWORK_NO_INTERNET;
 			System.out.println("Your ip address is: " + ipaddress);
 		}
 		clientAddress = null;
 		doing = true;
 		self = new Thread(this);
 		self.start();
 		return true;
 	}
 
 	public void run() {
 		try {
 			socket = new ServerSocket(670);
 		} catch (IOException e2) {
 			if (!noInternet)
 				Game.Settings.currentNetworkState = NetworkState.CONNECTED_TO_INTERNET_NO_NETWORK;
 			else
 				Game.Settings.currentNetworkState = NetworkState.NO_CONNECTION;
 			e2.printStackTrace();
 		}
 		try {
 			client = socket.accept();
 		} catch (IOException e2) {
 			e2.printStackTrace();
 		}
 		if (clientAddress == null) {
 			clientAddress = client.getInetAddress().toString();
 			if (clientAddress.indexOf("/") > -1) {
 				clientAddress = clientAddress.replace("/", "");
 			}
 			System.out.println("The server connected to: " + clientAddress);
 		}
 		PrintWriter out = null;
 		try {
 			out = new PrintWriter(client.getOutputStream(),true);
 		} catch (IOException e2) {
 			e2.printStackTrace();
 		}
 		InputStream is = null;
 		try {
 			is = client.getInputStream();
 		} catch (IOException e1) {
 			e1.printStackTrace();
 		}
 		InputStreamReader isr = new InputStreamReader(is);
         BufferedReader br = new BufferedReader(isr);
 		while (doing) {
 			serverIsWriting = true;
 			out.println(sendTmp);
 			sendTmp = "";
 			serverIsWriting = false;
 			try {
 				Thread.sleep(50);
 			}
 			catch (Throwable t) {
 				
 			}
             try {
             	String rl = br.readLine();
             	if (rl.equals("HELLO")) {
             		onClientConnect();
             	}
             	else if (rl != null && !rl.equals("")) {
             		if (rl.startsWith("NA")) {
             			rl = rl.replace("NA", "");
             			String name = rl.substring(0,rl.indexOf(":")+1);
             			String val = rl.replace(name, "");
             			name = name.replace(":", "");
             			nas.add(new NetAttribute(name,val));
             		}
             		else {
             			onMessageRecieved(rl);
             		}
             	}
 			} catch (IOException e) {
 				if (e.getMessage().equals("Connection reset") || e.getMessage().equals("Software caused connection abort: recv failed")) {
 					doing = false;
 					break;
 				}
 				e.printStackTrace();
 			}
 		}
 		try {
 			br.close();
 		}
 		catch (Throwable te) {
 			
 		}
 		try {
 			isr.close();
 		}
 		catch (Throwable te) {
 			
 		}
 		try {
 			is.close();
 		}
 		catch (Throwable te) {
 			
 		}
 		try {
 			socket.close();
 		}
 		catch (Throwable te) {
 			
 		}
 		try {
 			client.close();
 		}
 		catch (Throwable te) {
 			
 		}
 		System.out.println("Client disconnected.");
 		if (autoReboot)
 			start();
 		else
 			halt();
 	}
 	boolean sendLock = false;
 	
 	public void onClientConnect() {
 		
 	}
 	
 	String sendTmp = "";
 	boolean serverIsWriting = false;
 	public void send(String msg) {
 		while (serverIsWriting) {
 			
 		}
 		sendTmp = msg;
 	}
 	
 	public void sendAttribute(NetAttribute attr) {
 		while (serverIsWriting) {
 			
 		}
 		sendTmp = attr.getComposite();
 	}
 	
 	public NetAttribute getNetAttribute(String name) {
 		NetAttribute attr = null;
 		for (int c = 0; c < nas.size(); c++) {
 			NetAttribute b = nas.get(c);
 			if (b.getName().toLowerCase().equals(name.toLowerCase())) {
 				attr = b;
 				c = nas.size() + 10;
 				break;
 			}
 		}
 		return attr;
 	}
 	
 	public void onMessageRecieved(String msg) {
 		System.out.println("Recieved from client (the requestor): " + msg);
 	}
 	
 	public void halt() {
 		doing = false;
 		try {
 			Thread.sleep(250);
 		}
 		catch (Throwable t) {
 			
 		}
 		try {
 			client.close();
 		}
 		catch (Throwable t) {
 			
 		}
 		try {
 			socket.close();
 		}
 		catch (Throwable t) {
 			
 		}
 		client = null;
 		socket = null;
 		try{
 			self.interrupt();
 		}
 		catch (Throwable t) {
 			
 		}
 		self = null;
 	}
 	
 	
 }
