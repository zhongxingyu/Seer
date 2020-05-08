 package experiments.current.tracker.ui;
 
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.util.Set;
 
 import core.tools.InfoConsole;
 import experiments.current.tracker.core.Invitation;
 import experiments.current.tracker.core.NodeInfo;
 import experiments.current.tracker.core.Tracker;
 
 public class LaunchTracker {
 
 	/**
 	 * Put the application in background mode.
 	 */
 	public void BackgroundMode(Tracker tracker) {
 		synchronized(this){
 			try {
				System.out.println("127.0.0.1:" + tracker.getTransport().getPort());
 				this.wait();
 			} catch (InterruptedException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 	}
 
 	public static void main(String[] args) {
 		
 		try {
 			int port = 8000;
 			boolean backgroundMode = false;
 			for (int i = 0; i < args.length; i++) {
 				if(args[i].equals("-p") || args[i].equals("--port")) {
 					port = Integer.parseInt(args[i + 1]);
 				}
 				if(args[i].equals("--background") || args[i].equals("-b")) {
 					backgroundMode = true;
 				}
 			}
 
 			Tracker tracker = new Tracker(port);
 			
 			if(!backgroundMode){
 				System.out.print("Create tracker...");
 				System.out.println("\t\t\t[ ok ]");
 
 				BufferedReader input = new BufferedReader(new InputStreamReader(
 						System.in));
 				while (true) {
 					try {
 						System.out.println("\n\n0) print status");
 						System.out.println("1) clear history");
 						System.out.println("2) add invitation");
 						System.out.println("3) exit");
 						System.out.print("---> ");
 						int chx = Integer.parseInt(input.readLine().trim());
 						switch (chx) {
 						case 0:
 							System.out.println("\ntracker connected on : " +
 									InfoConsole.getIp() + ":" + tracker.getTransport().getPort() + "\n");
 							Set<String> keys = tracker.getPeerSet().keySet();
 							for (String key : keys) {
 								System.out.println(key + ":");
 								for (NodeInfo n : tracker.getPeerSet().get(key)) {
 									System.out.println("\t" + n);
 								}
 							}
 							System.out.println("\nInvitations:");
 							for(Invitation i : tracker.getInvitations()){
 								System.out.println("\t. networID: " + i.getNetworkID());
 								System.out.println("\t. access pass: " + i.getAccessPass() + "\n");
 							}
 							break;
 						case 1:
 							tracker.getPeerSet().clear();
 							break;
 						case 2:
 							System.out.print("networkID: ");
 							String networkID = input.readLine();
 							System.out.print("access pass: ");
 							String accessPass = input.readLine();
 							tracker.addInvitation(networkID, accessPass);
 							break;
 						case 3:
 							System.exit(0);
 						default:
 							break;
 						}
 					} catch (Exception e) {
 					}
 				}
 			} else {
 				new LaunchTracker().BackgroundMode(tracker);
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 }
