 /*
  * Copyright 2011-13 Fraunhofer ISE, energy & meteo Systems GmbH and other contributors
  *
  * This file is part of OpenIEC61850.
  * For more information visit http://www.openmuc.org
  *
  * OpenIEC61850 is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, either version 2.1 of the License, or
  * (at your option) any later version.
  *
  * OpenIEC61850 is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with OpenIEC61850.  If not, see <http://www.gnu.org/licenses/>.
  *
  */
 package org.openmuc.openiec61850.jositransport;
 
 import java.io.IOException;
 import java.net.Socket;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * The class extends Thread and handles ISO Transport connections. Once a connection has been initiated (CR,CC) it gives
  * the connection in form of the Connection class to the ConnectionListener.
 *
  */
 public final class ConnectionHandler implements Runnable {
 
 	private final static Logger logger = LoggerFactory.getLogger(ConnectionHandler.class);
 
 	private final Socket socket;
 	private final ServerThread serverThread;
 
 	ConnectionHandler(Socket socket, ServerThread serverThread) {
 		this.socket = socket;
 		this.serverThread = serverThread;
 	}
 
 	@Override
 	public void run() {
 		try {
 			TConnection tConnection = null;
 			try {
 				tConnection = new TConnection(socket, serverThread.maxTPDUSizeParam, serverThread.messageTimeout,
 						serverThread.messageFragmentTimeout);
 			} catch (IOException e) {
				logger.warn("Exception occured when someone tried to connect: " + e.getMessage(), e);
 				return;
 			}
 			try {
 				tConnection.listenForCR();
 			} catch (IOException e) {
				logger.warn("Exception occured when someone tried to connect. Server was listening for ISO Transport CR packet: {}", e.getMessage());
 				tConnection.close();
 				return;
 			}
 			if (serverThread.isAlive() == true) {
 				serverThread.connectionIndication(tConnection);
 			}
 
 			if (tConnection != null) {
 				tConnection.close();
 			}
 
 		} finally {
 			serverThread.removeHandler(this);
 		}
 	}
 }
