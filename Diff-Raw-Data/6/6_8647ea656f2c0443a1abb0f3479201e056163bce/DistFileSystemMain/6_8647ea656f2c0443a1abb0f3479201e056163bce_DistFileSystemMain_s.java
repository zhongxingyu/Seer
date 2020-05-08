 /**
  * @author paul
  */
 
 
 package distmain;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.Vector;
 
 import distclient.*;
 import distfilelisting.UserManagement;
 import distnodelisting.NodeSearchTable;
 import distserver.Server;
 
 
 public class DistFileSystemMain {
 	
 	private String prompt = "%s : ";
 	private BufferedReader inStream;
 	private UserManagement userManage = null;
 	private NodeSearchTable nst = null;
 	
 	private String ipAddress = null;
 	private Thread thServ = null;
 	private Vector<Thread> backgrounded = new Vector<Thread>();
 	
 	public DistFileSystemMain () {
 		try {
 			inStream = new BufferedReader(new InputStreamReader(System.in));
 			userManage = UserManagement.get_Instance();
 			nst = NodeSearchTable.get_Instance();
 			
 			System.out.print("Username: ");
 			String userName = inStream.readLine();
 			userManage.set_ownUserName(userName);
 			
 			System.out.print("IP Of Net: ");
 			ipAddress = inStream.readLine();
 		} 
 		catch (IOException e) {
 			e.printStackTrace();
 		}
 		
 		this.start_server();
 
 		// If IP address was given, connect to existing network
 		// Otherwise this will 
 		if (!ipAddress.equals(""))
 			this.connect_to_network();
 		
 		this.run_interface();
 	}
 	
 	public void start_server() {
 		System.out.println("Starting Server");
 		Server serv = new Server();
 		thServ = new Thread (serv);
 		thServ.start();
 	}
 	
 	public void connect_to_network() {
 		try {
 			int nextID;
 			String nextIP;
 			
 			Client cli = new Client();
 			
			cli.addTask(new ClntEnterNetwork(ipAddress, cli));
			
 			// Connect to the network and get initial search location
 			System.out.printf("Connecting to %s\n", ipAddress);
 		
 			nextID = cli.getServId();
 			nextIP = cli.getServIp();
 			
 			// Locate the servers location
 			cli.addTask(new ClntCheckPosition(nextIP, nextID, cli));
 			
 			String[] pred = cli.getPredecessor();
 			String[] succ = cli.getSuccessor();
 			
 			// Connect to the predecessor
 			System.out.printf("Connecting to predecessor %s\n", pred[1]);
 			cli.addTask(new ClntNewPredecessor(cli));
 			
 			// Connect to the successor
 			System.out.printf("Connecting to successor %s\n", succ[1]);
 			cli.addTask(new ClntNewSuccessor(cli));
 			
 			// Send new node notification
 			System.out.printf("Sending the new node notification\n");
 			cli.addTask(new ClntNewNode(succ[1]));
 			
 			System.out.println ("Connected to the network\n");
 			
 		} 
 		catch (Exception e) {
 			e.printStackTrace();
 			System.exit(-1);
 		}
 	}
 	
 	public void run_interface() {
 		String input;
 		boolean exit = false;
 		while (!exit) {
 			try {
 				System.out.printf(this.prompt, this.userManage.get_ownUserName());
 				input = inStream.readLine();
 				
 				if (input.equals("view predecessor")) {
 					System.out.printf("Predecessor ID = %s\n", this.nst.get_predecessorID());
 					System.out.printf("Predecessor IP = %s\n", this.nst.get_predecessorIPAddress());
 				}
 				else if (input.equals("view node search table")) {
 					for(int index = 0; index < this.nst.size(); index++) {
 						System.out.printf("Entry: %d\tID: %s\tIP: %s\n",
 								index, this.nst.get_IDAt(index), this.nst.get_IPAt(index));
 					}
 				}
 				
 			}
 			
 			catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 	
 	public static void main (String[] args) {
 		DistFileSystemMain dfsm = new DistFileSystemMain();
 	}
 }
 
