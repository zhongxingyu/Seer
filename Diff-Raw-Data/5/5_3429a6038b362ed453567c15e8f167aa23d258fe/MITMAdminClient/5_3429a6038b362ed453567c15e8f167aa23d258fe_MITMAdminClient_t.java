 /**
  * CSE 490K project 2
  */
 package mitm;
 
 import java.io.*;
 import java.net.*;
 
 public class MITMAdminClient
 {
 	private Socket m_remoteSocket;
 	private String username;
 	private String password;
 	private String command;
 	private String commonName = "";
 
 	public static void main( String [] args ) {
 		MITMAdminClient admin = new MITMAdminClient( args );
 		admin.run();
 	}
 
 	private Error printUsage() {
 		System.err.println(
 						   "\n" +
 						   "Usage: " +
 						   "\n java " + MITMAdminClient.class + " <options>" +
 						   "\n" +
 						   "\n Where options can include:" +
 						   "\n" +
 						   "\n   <-userName <type> >       " +
 						   "\n   <-userPassword <pass> >   " +
 						   "\n   <-cmd <shudown|stats>" +
 						   "\n   [-remoteHost <host name/ip>]  Default is localhost" +
 						   "\n   [-remotePort <port>]          Default is 8002" +
 						   "\n"
 						   );
 
 		System.exit(1);
 		return null;
 	}
 
 	private MITMAdminClient( String [] args ) {
 		int remotePort = 8002;
 		String remoteHost = "localhost";
 
 		if( args.length < 3 )
 			throw printUsage();
 
 		try {
 			for (int i=0; i<args.length; i++)
 				{
 					if (args[i].equals("-remoteHost")) {
 						remoteHost = args[++i];
 					} else if (args[i].equals("-remotePort")) {
 						remotePort = Integer.parseInt(args[++i]);
 					} else if (args[i].equals("-userName")) {
 						username = args[++i];
 					} else if (args[i].equals("-userPassword")) {
 						password = args[++i];
 					} else if (args[i].equals("-cmd")) {
 						command = args[++i];
 						if( command.equals("enable") || command.equals("disable") ) {
 							commonName = args[++i];
 						}
 					} else {
 						throw printUsage();
 					}
 				}
 
             MITMSSLSocketFactory socketFactory = new MITMSSLSocketFactory();
     		m_remoteSocket = socketFactory.createClientSocket( remoteHost, remotePort );
 
 		}
 		catch (Exception e) {
 			throw printUsage();
 		}
 
 	}
 
 	public void run()
 	{
 		try {
 			if( m_remoteSocket != null ) {
 				PrintWriter writer =
 					new PrintWriter( m_remoteSocket.getOutputStream() );
					                               writer.println("username:"+username);
                                                   writer.println("password:"+password);
                                                   writer.println("command:"+command);
                                                   writer.println("CN:"+commonName);
                    
 				writer.flush();
 			}
 
 			// now read back any response
 
 			System.out.println("");
 			System.out.println("Receiving input from MITM proxy:");
 			System.out.println("");
 			BufferedReader r = new BufferedReader(new InputStreamReader(m_remoteSocket.getInputStream()));
 			String line = null;
 			while ((line = r.readLine()) != null) {
 				System.out.println(line);
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		System.err.println("Admin Client exited");
 		System.exit(0);
 	}
 }
