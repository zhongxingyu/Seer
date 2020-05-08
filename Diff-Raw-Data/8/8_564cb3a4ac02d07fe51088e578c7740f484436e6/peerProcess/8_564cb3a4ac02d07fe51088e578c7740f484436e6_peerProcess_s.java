 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileReader;
 import java.net.ServerSocket;
 import java.util.ArrayList;
 import java.util.Vector;
 
 
 public class peerProcess extends Thread {
 
 	public static String myID;
 	public static int myPort;
 	public static WriteLog logger;
 	public static Vector<RemotePeerInfo> myPeerInfo;
 	static int haveFile;
 	static int myRank;
 	static int nofPreferredNeighbour;
 	static int unchokingInterval;
 	static int opUnchokingInterval;
 	static String fileName;
 	static int fileSize;
 	static int pieceSize;
 	static int nofPieces;
 	static int nofPeers;
 	static File theFile;
 
 	private ServerSocket server;
 
 	//Method to read PeerInfo.cfg
 	public void getPeerInfo() {
 		String st;
 		myPeerInfo = new Vector<RemotePeerInfo>();
 		try {
 			BufferedReader in = new BufferedReader(new FileReader("PeerInfo.cfg"));
 			int count=1; //to calculate myrank
 			while((st = in.readLine()) != null) {
 				
 				String[] tokens = st.split("\\s+");
 				myPeerInfo.addElement(new RemotePeerInfo(tokens[0], tokens[1], tokens[2]));
 
 				if(myID.equals(tokens[0])){
 					haveFile = Integer.parseInt(tokens[3]);
 					myPort = Integer.parseInt(tokens[2]);
 					myRank = count; 
 				}
 				count++;
 			}
			nofPeers = count;
 
 			//			in.close();
 		}
 		catch (Exception ex) {
 			logger.println(ex.toString());
 		}
 	}
 
 	//Method to read CommonInfo.cfg
 	public void getCommonConfig(){
 		String cfg;
 		ArrayList<String> configInfo = new ArrayList<String>();
 		try {
 			BufferedReader in = new BufferedReader(new FileReader("Common.cfg"));
 			while((cfg = in.readLine()) != null) {
 
 				String[] tokens = cfg.split("\\s+");
 				configInfo.add(tokens[1]);
 			}
 			
 			nofPreferredNeighbour = Integer.parseInt(configInfo.get(0));
 			unchokingInterval = Integer.parseInt(configInfo.get(1));
 			opUnchokingInterval = Integer.parseInt(configInfo.get(2));
 			fileName = configInfo.get(3);
 			fileSize = Integer.parseInt(configInfo.get(4));
 			pieceSize = Integer.parseInt(configInfo.get(5));
 			nofPieces = (int)Math.ceil((double)fileSize/pieceSize);
 			assert (pieceSize > 0);
 			assert (fileSize > 0);
 			assert (fileName.length() > 0);
 			
 			//			in.close();
 		}
 		catch (Exception ex) {
 			logger.println(ex.toString());
 		}		
 
 	}
 
 	//Constructor
 	public peerProcess(String arg) throws Exception {
 		logger = new WriteLog(arg);
 		myID = arg;
 		getPeerInfo();
 		getCommonConfig();
 		server = new ServerSocket(myPort);
 		this.start();
 	}
 
 	//Overriding run method
 	public void run() {
 		int nofLoops = nofPeers-myRank;
 		
 		//Accept 
 		while(nofLoops > 0) {
 			nofLoops--;	
 			try {
 				new Connect(server);
 			} catch(Exception e) {
 				peerProcess.logger.println(e.getMessage());
 
 			}
 		}
 		
 		nofLoops = myRank-1;
 		try{
 			for(int x=0;x<nofLoops;x++) {
 				if (!myPeerInfo.elementAt(x).peerId.equals(myID)) {
 					
 					new Connect(myPeerInfo.elementAt(x).peerAddress,Integer.parseInt(myPeerInfo.elementAt(x).peerPort));
 					
 				}
 			}
 		}catch(Exception e){
 			peerProcess.logger.println(e.getMessage());
 
 		}
 
 	}
 	
 
 	
 	//Main method
 	public static void main(String[] args)  throws Exception {
 
 		new peerProcess(args[0]);
 
 
 		/* test to print parameters
 		 * 
 		for(int x=0;x<p.myPeerInfo.size();x++) {
 			RemotePeerInfo peer = (RemotePeerInfo)p.myPeerInfo.elementAt(x);
 			logger.println(peer.peerId+" "+peer.peerAddress+" "+peer.peerPort);
 
 		}
 		logger.println( p.myID);
 		logger.println( p.haveFile);
 		logger.println (p.nofPreferredNeighbour);
 		logger.println (p.unchokingInterval);
 		logger.println (p.opUnchokingInterval);
 		logger.println (p.fileName);
 		logger.println (p.fileSize);
 		logger.println (p.pieceSize);
 		logger.println (p.nofPieces);
 		 */
 		//		new peerProcess();
 	}
 }
