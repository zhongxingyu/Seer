 /**
  * @author paul
  */
 
 
 package distmain;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.Vector;
 
 import distclient.*;
 import distconfig.Sha1Generator;
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
 			
 			// Connect to the network and get initial search location
 			// These have to all run in the same thread since they rely on each other
 			System.out.printf("Connecting to %s\n", ipAddress);
 			ClntEnterNetwork cen = new ClntEnterNetwork(ipAddress, cli);
 			cen.run();
 			cen = null;
 		
 			nextID = cli.getServId();
 			nextIP = cli.getServIp();
 			
 			// Locate the servers location
 			System.out.printf("Entering network at %s\n", nextIP);
 			ClntCheckPosition ccp = new ClntCheckPosition(nextIP, nextID, cli);
 			ccp.run();
 			ccp = null;
 			
 			String[] pred = cli.getPredecessor();
 			String[] succ = cli.getSuccessor();
 			
 			// Connect to the predecessor
 			System.out.printf("Connecting to predecessor %s\n", pred[1]);
 			ClntNewPredecessor cnp = new ClntNewPredecessor(cli);
 			cnp.run();
 			cnp = null;
 			
			/*// Connect to the successor
 			System.out.printf("Connecting to successor %s\n", succ[1]);
 			ClntNewSuccessor cns = new ClntNewSuccessor(cli);
 			cns.run();
 			cns = null;
 			
 			// Send new node notification
 			System.out.printf("Sending the new node notification\n");
 			ClntNewNode cnn = new ClntNewNode(succ[1]);
 			cnn.run();
 			cnn = null;
 			
 			System.out.println ("Connected to the network\n");
			*/
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
 				else if (input.equals("view own information")) {
 					System.out.printf("ID: %s\nIP: %s\n", this.nst.get_ownID(), this.nst.get_ownIPAddress());
 				}
 				else if (input.contains("sha1")) {
 					String[] vals = input.split(" ");
 					for (int index = 0; index < vals.length; index++) {
 						System.out.printf("%s :\t%s\n", vals[index], Sha1Generator.generate_Sha1(vals[index]));
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
 
