 package caviar;
 
 import peersim.core.Node;
 
 public class ArrivedMessage {
 	
 	/**
 	*	Message TYPES
 	*/
 	static final int HELLO = 0;			//	HELLO MSG
 	static final int GOODBYE = 1;		//	LEAVING MSG
 	static final int UPLOAD = 2;			//	DATA TRANSFER
 	static final int CONNECT = 3;		//	REQUEST FOR CONNECTION
 	static final int REQUEST_PEERS_FROM_THIS_BIN = 4;	
 	static final int NOONE_STREAMS_IN_THIS_BIN = 5;
 	static final int REQUEST_PEERS_FROM_OTHER_BINS = 6;
 	static final int GET_SUPERPEER = 7;
 	static final int YOUR_SUPERPEER = 8;
 	static final int DO_YOU_HAVE_THIS = 9;	// do you have this video? applicable message for CDN
 	static final int I_HAVE_IT = 10;			// reply to DO_YOU_HAVE_THIS
 	static final int I_DONT_HAVE_IT = 11;	// reply to DO_YOU_HAVE_THIS
 	static final int REQUEST_MY_CLIENTS = 12;// new super peer asks for his clients
 	static final int YOUR_CLIENTS = 13;		// reply to REQUEST_MY_CLIENTS
 	static final int YOUR_PEERS = 14;
 	static final int GOODBYE_LEECHER = 15;	// sent by a leaving node to its peer leechers
 	static final int UPLOAD_SPEED_THAT_CAN_BE_GIVEN = 16;
 	static final int ACCEPT_SPEED = 17;
 	static final int REJECT_SPEED = 18;		// sent by the requesting peer when its capacity is maxed
 	static final int FELLOW_SP_REQUEST_FOR_PEERS = 19;
 	static final int FIRED = 20;	//sent by a CDN to a SP when a new peer is nearer
 	static final int YOU_ARE_SUPERPEER = 21 ;
 	static final int GET_MY_CLIENTS = 22;
 	static final int REJECT = 23;
 	static final int UPDATE_SP = 24;		// To be sent to other SPs when a new peer is upgraded to SP
 	
 	final int    msgType;
 	final Node   sender;
 	public int    data0;
 	public int    data;
 	public Node[] nodeList;
 	public Node	 superPeer;
 	public int[] peerWatching;
 	public int [][] index;
 	public Node node1;
 	public Node node2;
 	
 	public ArrivedMessage(int typeOfMsg, Node sender, int data0, int data) // DO_YOU_HAVE_THIS, REQUEST_PEERS_FROM_THIS_BIN, REQUEST_PEERS_FROM_OTHER_BINS, ACCEPT_SPEED
 	{
 		this.msgType    = typeOfMsg;
 		this.sender       = sender;
 		this.data0     = data0;
 		this.data = data;
 	}
 	public ArrivedMessage(int typeOfMsg, Node sender, Node node1, Node node2) //UPDATE_SP, 
 	{
 		this.msgType    = typeOfMsg;
 		this.sender       = sender;
 		this.node1     = node1;
 		this.node2 = node2;
 	}
 	public ArrivedMessage(int typeOfMsg, Node sender, Node[] nodeList)
 	{
 		this.msgType    = typeOfMsg;
 		this.sender       = sender;
 		this.nodeList = nodeList;
 	}
 	public ArrivedMessage(int typeOfMsg, Node sender, Node[] nodeList, int data)// YOUR_PEERS, 
 	{
 		this.msgType    = typeOfMsg;
 		this.sender       = sender;
 		this.nodeList = nodeList;
 		this.data 		= data;
 	}
 	public ArrivedMessage(int typeOfMsg, Node sender, Node superPeer) // YOUR_SUPERPEER, I_DONT_HAVE_IT, I_HAVE_IT, FIRED
 	{
 		this.msgType    = typeOfMsg;
 		this.sender       = sender;
 		this.superPeer = superPeer;
 	}
 	public ArrivedMessage(int typeOfMsg, Node sender, int data) //GET_SUPERPEER,GET_MY_CLIENTS, UPLOAD, GOODBYE, CONNECT, UPLOAD_SPEED_THAT_CAN_BE_GIVEN, REJECT_SPEED
 	{
 		this.msgType    = typeOfMsg;
 		this.sender       = sender;
 		this.data = data;
 	}
 	public ArrivedMessage(int typeOfMsg, Node sender, Node[] nodeList, int[] peerWatching)
 	{
 		this.msgType    = typeOfMsg;
 		this.sender       = sender;
 		this.nodeList = nodeList;
 		this.peerWatching = peerWatching;
 	}
 	public ArrivedMessage(int typeOfMsg, Node sender, Node[] nodeList, int[] peerWatching, int[][] index) //YOUR_CLIENTS
 	{
 		this.msgType    = typeOfMsg;
 		this.sender       = sender;
 		this.nodeList = nodeList;
 		this.peerWatching = peerWatching;
 		this.index = index;
 	}
 }
