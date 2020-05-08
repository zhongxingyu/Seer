 package client.gui;
 
 import org.apache.commons.cli.CommandLine;
 import org.apache.commons.cli.GnuParser;
 import org.apache.commons.cli.Options;
 import org.apache.commons.cli.ParseException;
 
 import client.system.StorageServerConnection;
 
 public class MainClass {
 	static StorageServerConnection sServer;
 	static GUI gui;
 
 	public static void main(String[] args) {
 		System.setProperty("file.encoding", "UTF-8");
 
 		gui = new GUI();
 
 		CommandLine cmd = parseArgs(args);
 
 		String srvAddr;
 		if (cmd.getArgs().length < 1) {
			server.system.StartUpServer.main(new String[] {});
 			srvAddr = "localhost";
 		} else
 			srvAddr = cmd.getArgs()[0];
 
 		sServer = new StorageServerConnection(srvAddr);
 		if (cmd.hasOption('L'))
 			loginOK();
 	}
 
 	public static void loginOK() {
 		gui.swapPane("main");
 	}
 
 	public static void runAddEvent() {
 		gui.swapPane("addEvent");
 	}
 
 	public static void logout() {
 		gui.swapPane("login");
 	}
 
 	public static void runAddMeating() {
 		gui.swapPane("addMeeting");
 	}
 
 	public static void runGroupSettings() {
 		gui.swapPane("Groups");
 	}
 
 	private static CommandLine parseArgs(String[] args) {
 		Options options = new Options();
 		options.addOption("L", false, "Skip login-screen");
 
 		try {
 			return new GnuParser().parse(options, args);
 		} catch (ParseException e) {
 			throw new IllegalArgumentException(e);
 		}
 	}
 }
