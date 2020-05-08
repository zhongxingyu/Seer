 /**
  * @author sgujrati
  * This program implements a process running on a node. This process is implemented
  * as a stack of four layers: App <-> Routing <-> DLL <-> Phy. Phy has been implemented.
  */
 import java.io.BufferedWriter;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.io.IOException;
 import java.net.Socket;
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 import java.io.BufferedReader;
 
 /**
  * This class implements the application layer. 
  * @author sgujrati
  *
  */
 public class Node{
 	// nodeID: a string consisting of only one character
 	private String nodeID;
 	private Routing rl;
 	// This queue stores messages received from RL 
 	private SharedQueue<AppPacket> fromRl;
 
 	/**
 	 * Default constructor
 	 */
 	Node(){
 	}
 	
 	/**
 	 * This constructor validates arguments and creates a routing layer. 
 	 * @param args
 	 */
 	Node(String[] args){
 		if (args.length != 3) {
 			System.out
           .println("Usage: java NodeApp <nodeID> <host> <portNumber>\n");
 			System.exit(0);
 		}
 		nodeID = args[0];
 		rl = new Routing(args, this);
 		fromRl = new SharedQueue<AppPacket>(Common.queueCapacity);
 	}
 	
 	/**
 	 * Returns the Node ID of this node
 	 * @return
 	 */
 	public String getNodeID(){
 		return nodeID;
 	}
 	
 	/**
 	 * Creates a new AppPacket and sends it down to RL. destID = 0
 	 * indicates message broadcast. 
 	 * @param destID
 	 * @param payload
 	 */
 	public void send(String destID, String payload){
 		AppPacket appPkt = new AppPacket(nodeID, destID, payload);
 		rl.send(appPkt);
 	}
 	
 	/**
 	 * This function, when called by RL inserts an AppPacket to
 	 * fromRl queue.
 	 * @param appPkt
 	 */
 	public void receive(AppPacket appPkt){
 		fromRl.insert(appPkt);
 	}
 
 	/**
 	 * This function takes one AppPacket from fromRl and returns it.
 	 * @return
 	 */
 	public AppPacket recv(){
 		AppPacket appPkt = fromRl.remove();
 		return appPkt;
 	}
 	
 	public static void main(String[] args){
 		Node app = new Node(args);
 		
 		/* Node A opens and transfers a file. */
 		if(app.getNodeID().equals("A")){
 			BufferedReader br = null;
 			String line;
 			
 			try {
 				//br = new BufferedReader(new FileReader("temp.txt"));
 				br = new BufferedReader(new FileReader("test.txt"));
 			} catch (FileNotFoundException e) {
 				System.err.println(e);
 			}
 
 			try {
 				while((line = br.readLine()) != null){ 
 					app.send("B", line);
 				}
 				app.send("B", null);
 				br.close();
 			} catch (IOException e) {
 				System.err.println(e);
 			} 
 		}
 		
 		/* 
 		 * Node B receives a file from A. Start Node B before starting
 		 * Node A
 		 */
 		if(app.getNodeID().equals("B")){
 			BufferedWriter bw = null;
 			try {
 				bw = new BufferedWriter(new FileWriter("tempB.txt"));
 			} catch (IOException e) {
 				System.err.println(e);
 			}
 
 			try {
 				while(true){
                     AppPacket appPkt = app.recv();
                     String dest = appPkt.getDst();
                     String src = appPkt.getSrc(); // if src = 0, it means it is a broadcasted message
                     System.out.println(dest + " received \"" +
                     appPkt.getPayload() + "\" from " + src);
                     app.getNodeID();
 
                     if(appPkt.getPayload() == null){
                             bw.close();
                             System.out.println("I received the file. " +
                                             "I am closing it now and exiting");
                             //break;
                             System.exit(0);
                     }
                     else
                             /*
                              * Do not forget to append a new line character at
                              * the end of payload if you were reading the file line by line,
                              * and transmitting individual lines. If you are reading the file
                              * bye by byte, then you may not have to do this.
                              */
                             bw.write(appPkt.getPayload()+"\n");
                             //bw.close();
                             //System.exit(0);
 				}
 			} catch (IOException e) {
 				System.err.println(e);
 			}			
 		}		
 	}
 }
 
 /**
  * This class implements routing layer.
  * @author sgujrati
  *
  */
 class Routing{
 	private Dll dll;	
 	private SharedQueue<AppPacket> fromApp;
 	private SharedQueue<RlPacket> fromDll;
 	private char[] rlHeader;
 	
 	/**
 	 * Default constructor
 	 */
 	Routing(){		
 	}
 	
 	/**
 	 * This constructor creates a DLL and two threads waiting for packets
 	 * from app and dll respectively.
 	 * @param args
 	 * @param app
 	 */
 	Routing(String[] args, final Node app){	
 		dll = new Dll(args, this);
 		fromApp = new SharedQueue<AppPacket>(Common.queueCapacity);
 		fromDll = new SharedQueue<RlPacket>(Common.queueCapacity);
 
 		/* Waits for packets pushed down by App */
 		(new Thread(){
 			public void run(){
 				while(true){
 					AppPacket appPkt = fromApp.remove();
 					rlHeader = new char[Common.rlHeaderLen];
 					rlHeader[0]='r';rlHeader[1]='l';rlHeader[2]='h';
 					RlPacket rlPkt = new RlPacket(appPkt, rlHeader);
 					dll.send(rlPkt);				}
 			}
 		}).start();
 		
 		/* Waits for packets pushed by Dll */
 		(new Thread(){
 			public void run(){
 				while(true){
 					RlPacket rlPkt = fromDll.remove();
 					//String header = rlPkt.getHeader();
 					//System.out.println("RlHeader: " + header);
 					app.receive(rlPkt.getAppPacket());				}
 			}
 		}).start();
 	}	
 	
 	public void send(AppPacket appPkt){
 		fromApp.insert(appPkt);
 	}
 	
 	void receive(RlPacket rlPkt){
 		fromDll.insert(rlPkt);
 	}	
 }
 
 /**
  * This class implements DLL.
  * @author bccain
  *
  */
 class Dll{
 	private Phy phy;
 	private SharedQueue<RlPacket> fromRl;
 	private SharedQueue<DllPacket> fromPhy;
 	private char[] dllHeader;
 	private int num_sent, num_rec, num_ack;
 	private final int window = 10; // window size is 10
 	private ArrayList<DllPacket> sbuff = new ArrayList<DllPacket>(); // sendbuff List
 	private long timer;
 	private Boolean timer_running;
 	ArrayList<RlPacket> packetList = new ArrayList<RlPacket>();
 	
 	private String rest;
 	
 
 	/**
 	 * Default constructor
 	 */
 	Dll(){	
 	}
 	
 	/**
 	 * This constructor creates a DLL and two threads waiting
 	 * for data from rl and phy respectively.
 	 * @param args
 	 * @param rl
 	 */
 	Dll(String[] args, final Routing rl){
 		phy = new Phy(args, this);
 		fromRl = new SharedQueue<RlPacket>(Common.queueCapacity);
 		fromPhy = new SharedQueue<DllPacket>(Common.queueCapacity);
 		num_sent = 0;
 		num_rec = 0;
 		num_ack = 0;
 		timer_running = false;
 		rest = "";
 		
 		/* Waits for packets pushed down by Rl */
 		// Physical Layer Send thread
 		// DLL packet header looks like this below:
 		// dllXZZZZZZ - X: Msg == 1 or Ack == 0; Z: num_sent or num_received
 		// 
 		// Protocol based off of Dr Singh's CIS 725 lecture slides after_Data_Link_Layer_2.pdf Go Back N
 		(new Thread(){
 			public void run(){
 				while(true){
 					if (num_sent < (num_ack + window) && sbuff.size() - 1 < num_sent && fromRl.getSize() > 0){
 						
 						if (packetList.isEmpty()){
 							RlPacket rlPkt = fromRl.remove();
 							packetList = fragPayload(rlPkt);
 							RlPacket tempRlPkt = packetList.remove(0);
 							if (packetList.size() < 1){
 								// payload is smaller than 40 bytes
 								dllHeader = buildHeader("msg");
 							}
 							else{
 								dllHeader = buildHeader("frag");
 							}
 							DllPacket sendPkt = new DllPacket(tempRlPkt, dllHeader);
 							sbuff.add(sendPkt);
 							phy.send(sendPkt); num_sent++;
 						}
 						else if (packetList.size() == 1){
 							RlPacket tempRlPkt = packetList.remove(0);
 							dllHeader = buildHeader("frag_end");
 							DllPacket sendPkt = new DllPacket(tempRlPkt, dllHeader);
 							sbuff.add(sendPkt);
 							phy.send(sendPkt); num_sent++;
 						}
 						else{
 							RlPacket tempRlPkt = packetList.remove(0);
 							dllHeader = buildHeader("frag");
 							DllPacket sendPkt = new DllPacket(tempRlPkt, dllHeader);
 							sbuff.add(sendPkt);
 							phy.send(sendPkt); num_sent++;
 						}
 						
 						//start to time out if messages lost
 						if (num_sent == num_ack + window){
 							// start timer
 							System.out.println("Starting Timer...");
 							timer = System.currentTimeMillis();
 							timer_running = true;
 						}
 					}
 					else if (sbuff.size() != num_sent){
 						// if condition should be the same as sbuff[ns] != null
 						//System.out.println("sbuff[ns]!=null");
 						phy.send(sbuff.get(num_sent)); num_sent++;
 					}
 					else if ((System.currentTimeMillis() - timer > 100)){
 						// timeout
 						//System.out.println("Time out...");
 						num_sent = num_ack;
 						timer_running = false;
 					}
 				}
 			}
 		}).start();
 		
 		/* Waits for packets pushed by Phy */
 		// Routing Layer Receive Thread
 		(new Thread(){
 			public void run(){
 				String payload = "";
 				while(true){
 					if (fromPhy.getSize() > 0){
 						DllPacket dllPkt = fromPhy.remove();
 						// Print system header to console
 						String header = dllPkt.getHeader();
 						//System.out.println("DllHeader: " + header);
 						
 						if (isMsg(header)){
 							// another message to combine fragments with each other
 							if (isFrag(header)){
 								payload += dllPkt.getRlPacket().getAppPacket().getPayload();
 							}
 							else if (isEnd(header)){
 								// I don't know if this is working right....
 								payload += dllPkt.getRlPacket().getAppPacket().getPayload();
 								RlPacket rlnew = buildRlPkt(payload, dllPkt.getRlPacket());
 								DllPacket completeDllPkt = new DllPacket(rlnew, buildHeader("msg"));
 								
 								String sub_header = header.substring(4);
 								int x = Integer.parseInt(sub_header.trim());
 								System.out.println("This is a msg.");
 								if (num_rec == x){
 									System.out.println("I got x: " + x + "...Sending to RL");
 									num_rec++;
 									rl.receive(completeDllPkt.getRlPacket());// deliver message
 								}
 							}
 							else{
 								String sub_header = header.substring(4);
 								int x = Integer.parseInt(sub_header.trim());
 								System.out.println("This is a msg.");
 								if (num_rec == x){
 									System.out.println("I got x: " + x + "...Sending to RL");
 									num_rec++;
 									rl.receive(dllPkt.getRlPacket());// deliver message
 								}
 							}
 							
 							// send ack
 							System.out.println("Sending Ack.");
 							sendAck(dllPkt);
 							// send ack, start timer, if timeout, resend?
 							// ack dll == dll0<num_rec>
 						}
 						else if (isAck(header)){
 
 							String sub_header = header.substring(4);
 							int s = Integer.parseInt(sub_header.trim());
 							//System.out.println("This is an ack");
 							if (s > num_ack){
 								//System.out.println("s > na");
 								num_ack = s;
 							}
 							// cancel timer
 							timer = System.currentTimeMillis();
 							timer_running = false;
 						}
 					}
 				}
 			}
 		}).start();
 	}
 	
 	/**
 	 * 
 	 * @author bccain
 	 *
 	 */
 	public ArrayList<RlPacket> fragPayload(RlPacket rlPkt){
 		// assumes string char is 2 bytes each
 		ArrayList<RlPacket> rlpktlst = new ArrayList<RlPacket>();
 		String payload = rlPkt.getAppPacket().getPayload();
 		String tempPayload = "";
 		int count = 0;
 		
 		if (payload == null){
 			rlpktlst.add(rlPkt);
 		}
 		else if (payload.length() < 20){
 			RlPacket pkt = buildRlPkt(payload, rlPkt);
 			rlpktlst.add(pkt);
 		}
 		else{
 			for (int i = 0; i < payload.length(); i++){
 				if (count < 20){
 					tempPayload += payload.charAt(i); count++;
 				}
 				else{
 					count = 0;
 					System.out.println("This is the temp payload: " + tempPayload);
 					RlPacket pkt = buildRlPkt(tempPayload, rlPkt);
 					rlpktlst.add(pkt);
 					tempPayload = "";
 				}
 			}
 		}
 		
 		return rlpktlst;
 	}
 	
 	/**
 	 * 
 	 * @author bccain
 	 *
 	 */
 	public RlPacket buildRlPkt(String payload, RlPacket rlpkt){
 		String src = rlpkt.getAppPacket().getSrc();
 		String dst = rlpkt.getAppPacket().getDst();
 		AppPacket ap = new AppPacket(src, dst, payload);
 		
 		RlPacket rlp = new RlPacket(ap, rlpkt.getHeader().toCharArray());
 		
 		return rlp;
 	}
 	
 	/**
 	 * This method generates a new ack packet by reversing the
 	 * destination and source, giving a blank payload. Calls buildHeader
 	 * to give dllHeader an ack bit. Then sends ack through physical layer
 	 * @author bccain
 	 *
 	 */
 	public void sendAck(DllPacket dllpkt){
 		String src = dllpkt.getRlPacket().getAppPacket().getDst();
 		String dst = dllpkt.getRlPacket().getAppPacket().getSrc();
 		String payload = "";
 		AppPacket ap = new AppPacket(src, dst, payload);
 		
 		char[] rh = new char[Common.rlHeaderLen];
 		rh[0]='r';rh[1]='l';rh[2]='h';
 		RlPacket rlp = new RlPacket(ap, rh);
 
 		char[] head = buildHeader("ack");
 		DllPacket dllack = new DllPacket(rlp, head);
 		phy.send(dllack);
 	}
 	
 	/**
 	 * This method builds a new dllHeader char[]. Takes a
 	 * String headerType
 	 * @author bccain
 	 *
 	 */
 	public char[] buildHeader(String headerType){
 		char[] dllh = new char[Common.dllHeaderLen];
 		dllh[0]='d';dllh[1]='l';dllh[2]='l';
 		
 		if (headerType.equals("msg")){
 			// Header needs to be a message
 			dllh[3]='1';
 			char[] ns = Integer.toString(num_sent).toCharArray();
 			int index = 4;
 			
 			for (int i = 0; i < ns.length; i++){
 				if (index <= 10){
 					dllh[index] = ns[i];
 					index++;
 				}
 			}
 		}
 		else if(headerType.equals("frag")){
 			dllh[3]='F';
 			char[] ns = Integer.toString(num_sent).toCharArray();
 			int index = 4;
 			
 			for (int i = 0; i < ns.length; i++){
 				if (index <= 10){
 					dllh[index] = ns[i];
 					index++;
 				}
 			}
 		}
 		else if(headerType.equals("frag_end")){
 			dllh[3]='E';
 			char[] ns = Integer.toString(num_sent).toCharArray();
 			int index = 4;
 			
 			for (int i = 0; i < ns.length; i++){
 				if (index <= 10){
 					dllh[index] = ns[i];
 					index++;
 				}
 			}
 		}
 		else if (headerType.equals("ack")){
 			// Header needs to be an ack
 			dllh[3]='0';
 			char[] nr = Integer.toString(num_rec).toCharArray();
 			int index = 4;
 			
 			for (int i = 0; i < nr.length; i++){
 				if (index <= 10){
 					dllh[index] = nr[i];
 					index++;
 				}
 			}
 		}
 		
 		return dllh;
 	}
 	
 	public Boolean isFrag(String msg){
 		return (msg.charAt(3) == 'F');
 	}
 	
 	public Boolean isEnd(String msg){
 		return (msg.charAt(3) == 'E');
 	}
 	
 	public Boolean isMsg(String msg){
 		return (msg.charAt(3) == '1' || msg.charAt(3) == 'F' || msg.charAt(3) == 'E');
 	}
 	
 	public Boolean isAck(String msg){
 		return (msg.charAt(3) == '0');
 	}
 	
 	public void send(RlPacket rlPkt){
 		fromRl.insert(rlPkt);
 	}
 	
 	public void receive(DllPacket dllPkt){
 		fromPhy.insert(dllPkt);
 	}	
 }
 
 /**
  * This class implements Phy.
  * @author sgujrati
  * @param <PhyPacket>
  *
  */
 class Phy{
 	// The client socket
 	private Socket clientSocket = null;
 	// The input stream
 	private ObjectInputStream is = null;
 	// The output stream
 	private ObjectOutputStream os = null;
 	private int portNumber;
 	private String host;
 	private String nodeID;
 	// Packet received at physical layer
 	private PhyPacket rcvdPkt;
 	private Dll dll;
 	private char[] phyHeader;
 
 	/**
 	 * Default constructor
 	 */
 	Phy(){
 		
 	}
 
 	/**
 	 * This constructor creates Phy.
 	 * @param args
 	 * @param dll
 	 */
 	Phy(String[] args, Dll dll){
 		
 		this.dll = dll;
 		nodeID = args[0];
 		host = args[1];
 		portNumber = Integer.valueOf(args[2]).intValue();
 
 		/*
 		 * Open a socket on a given host and port. 
 		 * Open input and output streams.
 		 */
 		try {
 			clientSocket = new Socket(host, portNumber);
 			os = new ObjectOutputStream (clientSocket.getOutputStream());
 			is = new ObjectInputStream(clientSocket.getInputStream());
 		} catch (UnknownHostException e) {
 			System.err.println("Don't know about host " + host);
 		} catch (IOException e) {
 			System.err.println("Couldn't get I/O for the connection to the host "
           + host);
 		}		
 		
 		/*  Tell the server your ID */
 		StringPacket node = new StringPacket(nodeID);
 		try {
 			os.writeObject(node);
 		} catch (IOException e) {
 			System.err.println(e);
 		}
 		
 		/* Wait to receive data from other nodes */
 		receive();
 	}
 	
 	/* Sends the packet. */
 	public void send(DllPacket dllPkt){
 		phyHeader = new char[Common.phyHeaderLen];
 		phyHeader[0]='p';phyHeader[1]='h';phyHeader[2]='y';
 		PhyPacket phyPkt = new PhyPacket(dllPkt, phyHeader);
 		try {
 			os.writeObject(phyPkt);
 		} catch (IOException e) {
 			System.err.println(e);
 		}				
 	}
 	
 	public void receive(){		
 		(new Thread() {  
 			public void run() {					
 					try {
 						while ((rcvdPkt = (PhyPacket) is.readObject()) != null) {
 							//String header = rcvdPkt.getHeader();
 							//System.out.println("PhyHeader: " + header);
 							dll.receive(rcvdPkt.getDllPacket());
 						}
 					} catch (IOException e) {
 						System.err.println("IOException:  " + e);
 					} catch (ClassNotFoundException e){
 						System.err.println("IOException:  " + e);
 					}
 			  }
 			 }).start();
 	}
 }
