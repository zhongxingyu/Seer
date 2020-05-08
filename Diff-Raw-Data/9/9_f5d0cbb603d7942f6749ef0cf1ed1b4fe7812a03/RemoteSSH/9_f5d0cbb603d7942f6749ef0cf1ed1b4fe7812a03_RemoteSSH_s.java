 package ssh;
 
 import grails.plugin.remotessh.SshConfig;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 
 import ch.ethz.ssh2.Connection;
 import ch.ethz.ssh2.Session;
 import ch.ethz.ssh2.StreamGobbler;
 
 public class RemoteSSH  {
 	String host = "";
 	String user = "";
 	String sudo = "";
 	Integer port=0;
 	String userpass="";
 	String usercommand = "";
 	String filter = "";
 
 	StringBuilder output = new StringBuilder();
 
 	public RemoteSSH(String host, String usercommand) {
 		this.host = host;
 		this.usercommand = usercommand;
 	}
 	public RemoteSSH(String host, String sudo, String usercommand) {
 		this.host = host;
 		this.sudo = sudo;
 		this.usercommand = usercommand;
 	}
 
 	public RemoteSSH(String host, String user, String sudo, String usercommand) {
 		this.host = host;
 		this.user = user;
 		this.sudo = sudo;
 		this.usercommand = usercommand;
 	}
 	public RemoteSSH(String host, String user, String sudo, String usercommand,String filter) {
 		this.host = host;
 		this.user = user;
 		this.sudo = sudo;
 		this.usercommand = usercommand;
 		this.filter = filter;
 	}
 	public RemoteSSH(String host, String user, String sudo, String usercommand, Integer port) {
 		this.host = host;
 		this.user = user;
 		this.sudo = sudo;
 		this.usercommand = usercommand;
 		this.port=port;
 	}
 	public RemoteSSH(String host, String user, String sudo, String usercommand,String filter, Integer port) {
 		this.host = host;
 		this.user = user;
 		this.sudo = sudo;
 		this.usercommand = usercommand;
 		this.filter = filter;
 		this.port=port;
 	}
 	public RemoteSSH(String host, String user, String userpass, String sudo, String usercommand,String filter,Integer port) {
 		this.host = host;
 		this.user = user;
 		this.userpass=userpass;
 		this.sudo = sudo;
 		this.usercommand = usercommand;
 		this.filter = filter;
 		this.port=port;
 	}
 
	public String Result(SshConfig ac) throws InterruptedException {
 		Object sshuser=ac.getConfig("USER");
 		Object sshpass=ac.getConfig("PASS");
 		Object sshkey=ac.getConfig("KEY");
 		Object sshkeypass=ac.getConfig("KEYPASS");
 		Object sshport=ac.getConfig("PORT");
 		//System.out.println("----"+sshuser.toString());
 		if (user.equals("")) {
 			user = sshuser.toString();
 		}
 		if (userpass.equals("")) {
 			userpass = sshpass.toString();
 		}
 		if (port==0) {
 			String sps=sshport.toString();
 			if (sps.matches("[0-9]+")) {
 				port=Integer.parseInt(sps);
 			}
 		}
 		String hostname = host;
 		String username = user;
 		File keyfile = new File(sshkey.toString());
 		String keyfilePass = sshkeypass.toString();
		try {
 			if (port==0){port=22; }
 			Connection conn = new Connection(hostname,port);
 			/* Now connect */
 			conn.connect();
 			/* Authenticate */
 			boolean isAuthenticated=false;
 			if (userpass.equals("")) {
 				isAuthenticated = conn.authenticateWithPublicKey(username,
 					keyfile, keyfilePass);
 			}else{
 				isAuthenticated = conn.authenticateWithPassword(username,userpass);
 			}
 
 			if (isAuthenticated == false)
 				throw new IOException("Authentication failed.");
 			/* Create a session */
 			Session sess = conn.openSession();
 			sess.requestPTY("vt220");
 			if (sudo.equals("sudo")) {
 				sess.execCommand("sudo bash");
 				// sess.execCommand("sudo bash");
 			} else {
 				sess.execCommand("/bin/bash");
 			}
 			Thread.sleep(10);
 			usercommand = usercommand + "\n";
 			sess.getStdin().write(usercommand.getBytes());
 			sess.getStdin().write("exit\n".getBytes());
 			sess.getStdin().write("exit\n".getBytes());
 			Thread.sleep(10);
 			InputStream stdout = new StreamGobbler(sess.getStdout());
 			BufferedReader br = new BufferedReader(new InputStreamReader(stdout));
 			// output.append("Remote execution of "+usercommand+" returned:<br>");
 			while (true) {
 				String line = br.readLine();
 				if (line == null)
 					break;
 				if (filter.equals("")) {
 					output.append(line).append("<br>");
 				} else {
 					if (line.startsWith(filter)) {
 						output.append(line).append("<br>");
 					}
 				}
 			}
 			/* Close this session */
 			sess.close();
 			/* Close the connection */
 			conn.close();

		} catch (IOException e) {
			output.append(e);
		}
 		return output.toString();
 	}
 }
