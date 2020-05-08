 package exp;
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.logging.Logger;
 
 import com.jcraft.jsch.Channel;
 import com.jcraft.jsch.ChannelExec;
 import com.jcraft.jsch.JSch;
 import com.jcraft.jsch.JSchException;
 import com.jcraft.jsch.Session;
 
 
 public class JschExperience {
 
 	private static final Logger LOG = Logger.getLogger("JschExperience");
 
 	public static void main(String[] args) {
 		
 		JSch jsch = new JSch();
 		ConnectionData connData = new ConnectionData("tuxknife", "tuxknife", "192.168.25.25", 22);
 		
 		try {
 			Session session = jsch.getSession(connData.getUsername(), connData.getHost(), connData.getPort());
 			session.setUserInfo(connData);
 			
 			LOG.info("Connecting...");
 			session.connect();
 			LOG.info("Connection with server (" + connData.getHost() + ") succesfully done");
 			
 			BufferedReader toServer = new BufferedReader(new InputStreamReader(System.in));
			BufferedReader fromServer = null;
 			Channel channel;
 			String line;
 			String command;
 			
 			while (true) {
				if ((command = toServer.readLine()).equalsIgnoreCase("OUCH")) {
					toServer.close();
					if (fromServer != null) fromServer.close();
					break;
				}
 				
 				channel = session.openChannel("exec");
 				((ChannelExec)channel).setCommand(command);
 				channel.setInputStream(null);
 				channel.setOutputStream(System.out);
 				channel.connect();
 				
 				fromServer = new BufferedReader(new InputStreamReader(channel.getInputStream()));
 				
 				while ((line = fromServer.readLine()) != null) {
 					System.out.println(line);
 				}
 				channel.disconnect();
 			}
 	        
 			LOG.info("All commands executed successfully");
 			session.disconnect();
 			LOG.info("Connection with server (" + connData.getHost() + ") succesfully finished");
 		} catch (JSchException e) {
 			LOG.severe(e.getMessage());
 		} catch (IOException e) {
 			LOG.severe(e.getMessage());
 		}
 	}
 }
