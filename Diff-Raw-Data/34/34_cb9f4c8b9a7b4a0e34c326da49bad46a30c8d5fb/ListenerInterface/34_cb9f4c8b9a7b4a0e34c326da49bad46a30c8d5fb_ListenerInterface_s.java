 /**
  Copyright (c) 2013 Karl Engstr�m, Sebastian Ivarsson, Jacob Lundberg, Joakim Karlsson, Alexander Persson and Fredrik Westling
  */
 
 /**
  This file is part of TouchDeck.
 
  TouchDeck is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 2 of the License, or
  (at your option) any later version.
 
  TouchDeck is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.
 
  You should have received a copy of the GNU General Public License
  along with TouchDeck.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package se.chalmers.touchdeck.network;
 
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.Serializable;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.util.HashMap;
 import java.util.Observable;
 
 import android.util.Log;
 
 public abstract class ListenerInterface extends Observable implements Runnable {
	private final boolean								mLoopForever;
 	private ServerSocket								mServerSocket;
 	private final HashMap<String, ConnectionHandler>	mHandlers	= new HashMap<String, ListenerInterface.ConnectionHandler>();
 	private final int									mPort;
 
 	public ListenerInterface(boolean loopForever, int port) {
 		mLoopForever = loopForever;
 		mPort = port;
 	}
 
 	@Override
 	public void run() {
 		mServerSocket = null;
 		// Create the "welcome" socket
 		try {
 			mServerSocket = new ServerSocket(mPort);
 			Log.d("ListenerInt" + mPort, "Server socket set up on port " + mPort);
 		} catch (IOException e1) {
 			Log.d("ListenerInt" + mPort, "Server socket could not be set up on port " + mPort);
 			return;
 		}
 		// Accept all incomming requests to this socket and assign them a connection handler
 		do {
 			try {
 				Socket clientSocket = mServerSocket.accept();
 				ConnectionHandler handler = new ConnectionHandler(clientSocket);
 				String ipAddr = clientSocket.getInetAddress().toString().substring(1); // Remove a "/"
 				Log.e("in Listener" + mPort, "IP : " + ipAddr);
 				mHandlers.put(ipAddr, handler);
 				new Thread(handler).start();
 				Log.d("ListenerInt" + mPort, "New connection handler started: " + clientSocket.getInetAddress().getHostAddress());
 			} catch (IOException e) {
 				Log.d("ListenerInt" + mPort, "Could not create client socket");
 				return;
 			}
 		} while (mLoopForever);
 	}
 
 	public void end(String ipAddr) {
		Log.e("e", ipAddr);
 		ConnectionHandler c = mHandlers.get(ipAddr);
 		mHandlers.remove(ipAddr);
 		if (c == null) {
 			return;
 		}
 		try {
 			c.interrupt();
 			c.isStopped = true;
 			c.clientSocket.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		if (mHandlers.size() == 0 || ipAddr.equals("127.0.0.1")) {
 			try {
 				Log.e("in Listener" + mPort, "All handlers killed");
 				mServerSocket.close();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	private class ConnectionHandler extends Thread {
 		private final Socket		clientSocket;
 		private volatile boolean	isStopped	= false;
 
 		public ConnectionHandler(Socket s) {
 			clientSocket = s;
 		}
 
 		// TODO Error handling
 		@Override
 		public void run() {
 			// Keep reading the input
 			while (!isStopped) {
 				Log.d("ListenerInt" + mPort, "In loop");
 				ObjectInputStream ois = null;
 				try {
 					if (clientSocket == null) {
 						return;
 					}
 					ois = (new ObjectInputStream(clientSocket.getInputStream()));
 					Log.d("ListenerInt" + mPort, "InputStream created");
 				} catch (IOException e) {
 					Log.d("ListenerInt" + mPort, "Exiting ConnectionHandler");
 					e.printStackTrace();
 					return; // break;
 				}
 				Serializable op;
 				try {
 					// Read the object and handle the operation
 					op = (Serializable) ois.readObject();
 					String ipAddr = clientSocket.getRemoteSocketAddress().toString();
 					ipAddr = ipAddr.substring(1, ipAddr.indexOf(":"));
 					handle(op, ipAddr);
 					Log.d("ListenerInt" + mPort, "Operation completed, ip : " + ipAddr + ", op : " + op.toString());
 				} catch (IOException e) {
 					Log.d("ListenerInt" + mPort, "Reading went wrong, IO");
 					e.printStackTrace();
 				} catch (ClassNotFoundException e) {
 					Log.d("ListenerInt" + mPort, "Reading went wrong, ClassNotFound");
 					e.printStackTrace();
 				}
 			}
 		}
 	}
 
 	public abstract void handle(Serializable s, String ipAddr);
 }
