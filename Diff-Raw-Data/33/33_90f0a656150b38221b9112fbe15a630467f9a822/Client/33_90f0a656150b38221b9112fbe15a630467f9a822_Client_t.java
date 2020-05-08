 package ucbang.network;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.net.Socket;
 import java.util.LinkedList;
 import java.util.Random;
 
 import java.util.Iterator;
 
 import ucbang.core.Player;
 import ucbang.gui.ClientGUI;
 
 
 public class Client extends Thread{
 	String name;
 	static int players=0;
 	Socket socket=null;
 	Random r = new Random();
 	int port=12345;
 	String host ="127.0.0.1";
 	boolean connected=false;
 	LinkedList<String> outMsgs = new LinkedList<String>();
 	ClientGUI gui;
 	Player player;
 	public Client(String host, boolean guiEnabled) {
 		this.host=host;
 		name="Test client"+players;
 		if(guiEnabled)gui = new ClientGUI(players++, this);
 		this.start();
 	}
 	public Client(String host, boolean guiEnabled, String name) {
 		this.host=host;
 		this.name=name;
 		if(guiEnabled)gui = new ClientGUI(players++, this);
 		this.start();		
 	}
 
 	public static void main(String Args[]){
 		if(Args[1]==null)
 			new Client(Args[0],true);
 		else{
 			if(Args[1].equals("Dummy")){
 				new Client(Args[0],false);
 			}
 			else
 				new Client(Args[0],true).name=Args[1];
 		}	
 	}
 	public String getPlayerName(){
 		return name;
 	}
 	public void run(){
 		try{
 			socket = new Socket(host,port);
 		}
 		catch(Exception e){
 			System.err.println(e+"\nServer Socket Error!");
 		}
 		new ClientThread(socket, name, this);
 		
 		while(true){
 			try
 			{
 				sleep(45);
 			}
 			catch(InterruptedException e){}
 		}
 	}
 	void print(Object stuff){
     	if(gui!=null)
     		gui.appendText("Client:"+stuff);
     	else
     		System.out.println("Client:"+stuff);
     }
 	void addMsg(String msg){
 		synchronized(outMsgs){
 			outMsgs.add(msg);
 		}
 	}
 	public void addChat(String chat){
 		addMsg("Chat:"+chat);
 	}
 
 }
 
 class ClientThread extends Thread{
 	Socket server;
 	String name;
 	//Ship old;
 	BufferedReader in;
 	BufferedWriter out;
 	Client c;
 	String buffer;
 	public ClientThread(Socket theServer, String theName, Client c){
 		server=theServer;
 		name=theName;
 		this.c=c;
 		try {
   			out = new BufferedWriter(new OutputStreamWriter(server.getOutputStream()));
   			in= new BufferedReader(new InputStreamReader(server.getInputStream()));
  		}
  		catch(Exception e1) {
      		try {
         		server.close();
      		}
      		catch(Exception e) {
      		}
      	return;
      	}
      	this.start();
      	
 	}
 	public void run(){
 		while(!server.isClosed()){
 			try {
 				if(name!=null&&out!=null&&!c.connected){
 					out.write(name);
 					out.newLine();
 		         	out.flush();
 				}
 				synchronized(c.outMsgs){
 					if(!c.outMsgs.isEmpty()){
 						Iterator<String> iter = c.outMsgs.iterator();
 						while(iter.hasNext()){
 							out.write(iter.next());
 							out.newLine();
 							iter.remove();
 						}
 					}
 				}
 				out.flush();
 	         	if(in.ready()){
 		         	buffer=(String)in.readLine();
 		         	if(!c.connected&&buffer.equals("Successfully connected.")){
 		         		c.connected=true;
 		         		System.out.println("Successfully connected to server on "+server.getInetAddress());
 		         	}
					String[] temp = buffer.split(":",2);
 					if(temp[0].equals("Chat")){
 						c.gui.appendText(temp[1]);
 					}
 	         	}
 	      }
 	      catch(Exception e) {
 	      	if(e!=null&&e.getMessage()!=null&&e.getMessage().equals("Connection reset")){
 	      		print("Connection to server lost");
 	      			try{finalize();}catch(Throwable t){t.printStackTrace();}
 	      	}
 	      	e.printStackTrace();
 	      }
 		}
 		System.out.println("Server connection closed");
 	}
   	protected void finalize() throws Throwable{	
          	try{
          		in.close();
          		out.close();
          		server.close();
          	}
          	catch(Exception e){}
     }
     void print(Object stuff){
     	if(c.gui!=null)
     		c.gui.appendText("ClientThread:"+stuff);
     	else 
     		System.out.println("ClientThread:"+stuff);
     }
 }
