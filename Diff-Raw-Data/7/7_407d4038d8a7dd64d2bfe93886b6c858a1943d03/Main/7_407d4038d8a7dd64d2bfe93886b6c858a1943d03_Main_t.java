 /*
  * Copyright (c) 2011, Carlos Eduardo da Silva <kaduardo@gmail.com>
  *
  *
  * This file is part of jhgdc-text.
  *
  * jhgdc-text is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * jhgdc-text is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with jhgdc-text. If not, see <http://www.gnu.org/licenses/>.
  */
 package jhgdc.text;
 
 import jargs.gnu.CmdLineParser;
 
 import java.io.Console;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import jhgdc.library.HGDClient;
 import jhgdc.library.HGDConsts;
 import jhgdc.library.JHGDException;
 import jhgdc.text.commands.AbstractCommand;
 import jhgdc.text.commands.CommandFactory;
 
 /**
  * The command line jhgdc client.
  * 
  * @author Carlos Eduardo da silva
  * @since 29/03/2011
  * 
  */
 public class Main {
 
 	// The connection with the server
 	private static HGDClient client = null;
 
 	// The host in which we are going to connect to
 	private static String hostValue;
 
 	// The port we are going to use
 	private static Integer portValue;
 
 	// The username used in the connection
 	private static String usernameValue;
 
 	// Flag for exit code
 	private static boolean exitOk = false;
 
 	private static void printUsage() {
 		System.out.println("Usage: java jhgdc-text [opts] command [args]\n\n"
 				+ "  Options include:\n"
 				// + "    -e\t\t\tAlways require encryption\n"
 				// + "    -E\t\t\tRefuse to use encryption\n"
 				+ "    -h\t\t\tShow this message and exit\n"
 				+ "    -p port\t\tSet connection port\n"
 				+ "    -s host/ip\t\tSet connection address\n"
 				+ "    -u username\t\tSet username\n"
 				// + "    -x level\t\tSet debug level (0-3)\n"
 				+ "    -v\t\t\tShow version and exit\n"
 				// + "    -e\t\t\tEnable Encryption\n"
 				+ "  Commands include:\n" + "    q <filename>\tQueue a track\n"
 				+ "    vo\t\t\tVote-off current track\n"
 				+ "    ls\t\t\tShow playlist\n\n");
 	}
 
 	private static void printVersion() {
 		System.out.println("jhgdc-text Version 0.1");
 	}
 
 	private static void exitNicely() {
 		// close the socket
 		if (client != null) {
 			try {
 				client.disconnect(true);
 			} catch (Exception e) {
 				System.err.println(e.getLocalizedMessage());
 				e.printStackTrace();
 			}
 		}
 
 		// send the exit signal
 		System.exit(exitOk ? 0 : 1);
 	}
 
 	// Ask the user password
 	private static boolean authenticate(HGDClient client, String user) {
 		String password = null;
 
 		// Read password
 		Console cons;
 		char[] passwd;
 		if ((cons = System.console()) != null
 				&& (passwd = cons.readPassword("[%s]", "Password for user"
 						+ user + ":")) != null) {
 			password = new String(passwd);
 			java.util.Arrays.fill(passwd, ' ');
 		}
 
 		try {
 			client.login(user, password);
 			return true;
 		} catch (Exception e) {
			//System.err.println(e.getLocalizedMessage());
			//e.printStackTrace();
 			return false;
 		}
 	}
 
 	private static void processCommand(List<String> args) {
 		AbstractCommand command;
 		try {
 			// Ask factory to create the command
 			command = CommandFactory.createCommand(args.get(0));
 
 			// Once the command object has been created, remove it from the
 			// list.
 			args.remove(0);
 
 			// Validate the arguments
 			command.checkNumberOfArguments(args);
 
 			// Open the connection
 			client = new HGDClient();
 			client.connect(hostValue, portValue);
 
 			// Authenticate if necessary
 			if (command.isAuthenticationRequired()
 					&& !authenticate(client, usernameValue)) {
 				// Fail
 				System.err.println("Login as " + usernameValue + " failed!");
				exitOk = false;
				exitNicely();
 			}
 
 			// Execute the command
 			command.execute(args, client);
 
 		} catch (IOException ioe) {
 			System.err.println(ioe.getLocalizedMessage());
 			ioe.printStackTrace();
 			exitOk = false;
 			exitNicely();
 		} catch (IllegalStateException ise) {
 			System.err.println(ise.getLocalizedMessage());
 			ise.printStackTrace();
 			exitOk = false;
 			exitNicely();
 		} catch (JHGDException je) {
 			System.err.println(je.getLocalizedMessage());
 			je.printStackTrace();
 			exitOk = false;
 			exitNicely();
 		} catch (Exception e) {
 			System.err.println(e.getLocalizedMessage());
 			e.printStackTrace();
 			printUsage();
 			exitOk = false;
 			exitNicely();
 		}
 
 	}
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		CmdLineParser parser = new CmdLineParser();
 		// Options
 		CmdLineParser.Option help = parser.addBooleanOption('h', "help");
 		CmdLineParser.Option port = parser.addIntegerOption('p', "port");
 		CmdLineParser.Option server = parser.addStringOption('s', "server");
 		CmdLineParser.Option username = parser.addStringOption('u', "username");
 		CmdLineParser.Option version = parser.addBooleanOption('v', "version");
 
 		try {
 			parser.parse(args);
 		} catch (CmdLineParser.OptionException e) {
 			System.err.println(e.getMessage());
 			printUsage();
 			System.exit(2);
 		}
 
 		Boolean helpValue = (Boolean) parser
 				.getOptionValue(help, Boolean.FALSE);
 		portValue = (Integer) parser.getOptionValue(port, new Integer(
 				HGDConsts.DEFAULT_PORT));
 		hostValue = (String) parser.getOptionValue(server,
 				HGDConsts.DEFAULT_HOST);
 		usernameValue = (String) parser.getOptionValue(username,
 				System.getProperty("user.name"));
 		Boolean versionValue = (Boolean) parser.getOptionValue(version,
 				Boolean.FALSE);
 
 		System.out.println("Options received");
 		if (helpValue) {
 			System.out.println("help: " + helpValue);
 			printUsage();
 			exitOk = true;
 			exitNicely();
 		}
 
 		if (versionValue) {
 			System.out.println("version: " + versionValue);
 			printVersion();
 			exitOk = true;
 			exitNicely();
 		}
 
 		System.out.println("port: " + portValue);
 		System.out.println("server: " + hostValue);
 		System.out.println("username: " + usernameValue);
 
 		// Commands
 		String[] otherArgs = parser.getRemainingArgs();
 		System.out.println("Commands received");
 		for (int i = 0; i < otherArgs.length; i++) {
 			System.out.println(otherArgs[i]);
 		}
 
 		List<String> arguments = new ArrayList<String>(Arrays.asList(otherArgs));
 
 		processCommand(arguments);
 
 		exitOk = true;
 		exitNicely();
 	}
 
 }
