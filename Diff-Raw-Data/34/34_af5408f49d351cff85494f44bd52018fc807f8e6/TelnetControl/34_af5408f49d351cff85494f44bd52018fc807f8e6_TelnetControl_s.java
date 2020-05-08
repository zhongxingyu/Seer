 /*
  * utils - TelnetControl.java - Copyright © 2008-2009 David Roden
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package net.pterodactylus.util.telnet;
 
 import java.io.IOException;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import net.pterodactylus.util.io.Closer;
 import net.pterodactylus.util.service.AbstractService;
 
 /**
  * TODO
  *
  * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
  */
 public class TelnetControl extends AbstractService {
 
 	/** The logger. */
	private static final Logger logger = Logger.getLogger(TelnetControl.class.getName());
 
 	/** The server socket. */
 	private ServerSocket serverSocket;
 
 	/** The port to listen on. */
 	private int listenPort = 20013;
 
 	/** Mapping from command names to commands. */
 	private final Map<String, Command> commands = new HashMap<String, Command>();
 
 	/**
 	 * Creates a new telnet control.
 	 */
 	public TelnetControl() {
 		super("Telnet Control");
 	}
 
 	//
 	// ACCESSORS
 	//
 
 	/**
 	 * Sets the port to listen on.
 	 *
 	 * @param listenPort
 	 *            The port to listen on
 	 */
 	public void setListenPort(int listenPort) {
 		this.listenPort = listenPort;
 	}
 
 	/**
 	 * Adds the given command to this control.
 	 *
 	 * @param command
 	 *            The command to add
 	 */
 	public void addCommand(Command command) {
 		commands.put(command.getName().toLowerCase(), command);
 	}
 
 	//
 	// SERVICE METHODS
 	//
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	protected void serviceRun() {
 		logger.log(Level.INFO, "starting telnet control main loop");
 		try {
 			serverSocket = new ServerSocket(listenPort);
 		} catch (IOException ioe1) {
 			logger.log(Level.SEVERE, "could not create server socket on port " + listenPort, ioe1);
 			return;
 		}
 		while (!shouldStop()) {
 			try {
 				Socket clientSocket = serverSocket.accept();
 				logger.log(Level.INFO, "acception client connection on " + clientSocket.getRemoteSocketAddress());
 				ControlConnection controlConnection = new ControlConnection(clientSocket.getInputStream(), clientSocket.getOutputStream());
 				for (Command command : commands.values()) {
 					controlConnection.addCommand(command);
 				}
 				controlConnection.start();
 			} catch (IOException ioe1) {
 				if (!shouldStop()) {
 					logger.log(Level.WARNING, "could not accept client connection", ioe1);
 				}
 			}
 		}
 		logger.log(Level.INFO, "stopped telnet control main loop.");
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	protected void serviceStop() {
 		Closer.close(serverSocket);
 	}
 
 	/**
 	 * VM entry point for testing.
 	 *
 	 * @param arguments
 	 *            Command-line arguments
 	 * @throws InterruptedException
 	 *             if {@link Thread#sleep(long)} is interrupted
 	 */
 	public static void main(String... arguments) throws InterruptedException {
 		TelnetControl telnetControl = new TelnetControl();
 		telnetControl.init();
 		telnetControl.addCommand(new MemoryCommand());
 		telnetControl.addCommand(new GarbageCollectionCommand());
 		telnetControl.start();
 		Thread.sleep(120 * 1000);
 	}
 
 }
