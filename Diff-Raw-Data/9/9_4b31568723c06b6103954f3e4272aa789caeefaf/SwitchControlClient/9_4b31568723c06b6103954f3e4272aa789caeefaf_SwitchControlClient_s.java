 package com.truthfulgiant.xbeeswitchcontrol.client;
 
 import java.io.*;
 import java.net.*;
 import java.util.Vector;
 
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.JAXBException;
 import javax.xml.bind.Unmarshaller;
 
 import com.truthfulgiant.xbeeswitchcontrol.protocol.AbstractCommand;
 import com.truthfulgiant.xbeeswitchcontrol.protocol.CommandFactory;
 import com.truthfulgiant.xbeeswitchcontrol.protocol.generated.CommandTestScript;
 
 public class SwitchControlClient {
 
 	/**
 	 * @param args
 	 */
 
 	public static void main(String[] args) {
 		// reads script into a vector, constructing commands
 		Vector<AbstractCommand> commandVector = new Vector<AbstractCommand>(256);
 		long processInterval = 0;
 		try {
 			JAXBContext jc = JAXBContext.newInstance("com.truthfulgiant.xbeeswitchcontrol.protocol.generated");
 			Unmarshaller u = jc.createUnmarshaller();
 
 
 			CommandTestScript script = (CommandTestScript) u.unmarshal(new FileInputStream("script.xml"));
 			processInterval = script.getCommandDelayInterval();
 			
 			CommandTestScript.Commands commands = script.getCommands();
 			for (com.truthfulgiant.xbeeswitchcontrol.protocol.generated.AbstractCommand c : commands.getCommand()) {
 				commandVector.add(CommandFactory.CreateCommand(c));
 			}
 			
 			System.out.println(commandVector.size() + "commands added.");
 		} catch (JAXBException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		// connects to server socket
 		Socket clientSocket = null;
 //		PrintWriter out = null;
 		BufferedReader in = null;
 
 		try {
			clientSocket = new Socket("127.0.0.1", 3940);
 //			out = new PrintWriter(clientSocket.getOutputStream(), true);
 			in = new BufferedReader(new InputStreamReader(
 					clientSocket.getInputStream()));
 		} catch (UnknownHostException e) {
 			System.err.println("Don't know about host: localhost.");
 			System.exit(1);
 		} catch (IOException e) {
 			System.err.println("Couldn't get I/O for "
 					+ "the connection to: localhost.");
 			System.err.println(e.getMessage());
 			System.exit(1);
 		}
 		
 		// processes command in the scripts
 		while (!commandVector.isEmpty()) {
 			try {
 				if (processInterval > 0) {
 					Thread.sleep(processInterval * 1000);
 				}
 				AbstractCommand command = commandVector.firstElement();
 				command.Send(clientSocket);
 				System.out.println("return: " + in.read());
 				commandVector.remove(0);
 			} catch (InterruptedException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 
 		// processes passthrough command from user input
 /*
 		BufferedReader stdIn = new BufferedReader(new InputStreamReader(
 				System.in));
 		String userInput;
 		try {
 			while ((userInput = stdIn.readLine()) != null) {
 				System.out.println(userInput + "///");
 				AbstractCommand command = new PassThroughCommand(userInput);
 				command.Send(clientSocket);
 				System.out.println("echo: " + in.read());
 			}
 		} catch (IOException e) {
 			System.err.println("Cannot get user input.");
 			System.exit(1);
 		}
 */
 		// closes socket connection 
 		try {
 //			out.close();
 			in.close();
 //			stdIn.close();
 			clientSocket.close();
 		} catch (IOException e) {
 			System.err.println("Cannot close streams properly.");
 			System.exit(1);
 		}
 
 
 	}
 }
