 package edu.uw.cs.cse461.sp12.OSConsoleApps;
 
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.net.SocketException;
 import java.net.UnknownHostException;
 
 import org.json.JSONObject;
 
 
 
 import edu.uw.cs.cse461.sp12.OS.DDNSRRecord;
 import edu.uw.cs.cse461.sp12.OS.DDNSResolverService;
 import edu.uw.cs.cse461.sp12.OS.OS;
 import edu.uw.cs.cse461.sp12.OS.RPCCallerSocket;
 import edu.uw.cs.cse461.sp12.OS.DDNSException.DDNSNoAddressException;
 import edu.uw.cs.cse461.sp12.OS.DDNSException.DDNSNoSuchNameException;
 import edu.uw.cs.cse461.sp12.util.Log;
 
 public class Ping implements OSConsoleApp {
 
 	private static final String TAG="PingConsole";
 	private String mServerHost;
 	private String mServerPort;
	private RPCCallerSocket socket = null;

 	@Override
 	public String appname() {
 		return "ping";
 	}
 
 	@Override
 	public void run() throws Exception {

 		try {
 			// Eclipse doesn't support System.console()
 			BufferedReader console = new BufferedReader(new InputStreamReader(System.in));
 			System.out.println("Enter lines like <target> <msg> to have <msg> echoed back");
 			while ( true ) {
 				try {
 					System.out.print("Enter a host ip, or exit to exit: ");
 					mServerHost = console.readLine();
 					if ( mServerHost == null ) mServerHost = "";
 					else if ( mServerHost.equals("exit")) break;
 
 					System.out.print("Enter the RPC port, or empty line: ");
 					mServerPort = console.readLine();
 					if ( mServerPort == null || mServerPort.isEmpty() ) mServerPort = "0";
 					socket = getSocket();
 					if(socket == null) continue;
 					long time = System.currentTimeMillis();
 					long newTime = time;
 					long overall = 0;
 					int runs = 5;
 					String host = socket.remotehost();
 					String ip = socket.getInetAddress().getHostAddress();
 					int port = socket.getPort();
 					Log.i("Ping","Pinging " + host + " @ " + ip + ":" + port + "\n\n");
 					for(int i = 0; i < runs; i++){
 						time = System.currentTimeMillis();
 						socket.invoke("echo", "echo", new JSONObject().put("msg", ""));
 						newTime = System.currentTimeMillis();
 						long diff = newTime - time;
 						overall += diff;
 						System.out.println("Run #" + i + " (msec): " + diff);
 						if(!socket.isPersistent() && i < runs){
 							socket.close();
 							socket = getSocket();
 						}
 						//socket.close();
 						//socket = new RPCCallerSocket(targetIP, targetIP, targetPort);
 					}
 					System.out.println("Average (msec): " + ((double)overall) / runs);
 					if(!socket.isClosed()) socket.close();
 				} catch (Exception e) {
 					System.out.println("Exception: " + e.getMessage());
 					break;
 				}
 			}
 		} catch (Exception e) {
 			Log.e(TAG, TAG + ".run() caught exception: " + e.getMessage());
		} finally {
			if (socket != null && !socket.isClosed()) socket.close();
 		}
 	}
 	private RPCCallerSocket getSocket(){
     	RPCCallerSocket socket;
     	try {
 			socket = new RPCCallerSocket(mServerHost, mServerHost, mServerPort);
 		} catch(UnknownHostException e){
 			socket = resolve();
 		} catch(SocketException se){
 			socket = resolve();
 		} catch (Exception e2) {
 			Log.i("Ping", e2.getMessage());
 			return null;
 		}
 		return socket;
     }
     private RPCCallerSocket resolve(){
     	RPCCallerSocket socket;
     	DDNSRRecord record = null;
 		try {
 			record = ((DDNSResolverService) OS.getService("ddnsresolver")).resolve(mServerHost);
 		} catch (DDNSNoAddressException nae) {
 			Log.i("Ping","No address is currently assoicated with the name: " + mServerHost + "\n");
 			return null;
 		} catch (DDNSNoSuchNameException nsne) {
 			Log.i("Ping","No such name: " + mServerHost  + "\n");
 			return null;
 		} catch (Exception genE) {
 			Log.i("Ping","Exception: " + genE.getMessage() + "\n");
 			return null;
 		}
 		try {
 			socket = new RPCCallerSocket(mServerHost, record.host, record.port);
 		} catch (Exception e1) {
 			Log.i("Ping","Exception: " + e1.getMessage() + "\n");
 			return null;
 		} 
 		return socket;
     }
 	@Override
 	public void shutdown() throws Exception {
 	}
 	
 }
