 /**
  * Warlock, the open-source cross-platform game client
  *  
  * Copyright 2008, Warlock LLC, and individual contributors as indicated
  * by the @authors tag. 
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  */
 package cc.warlock.core.network;
 
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.Reader;
 import java.net.ConnectException;
 import java.net.Socket;
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 
 import cc.warlock.core.client.IWarlockClient;
 
 /**
  * @author marshall
  */
 public class Connection implements IConnection {
 
 	protected Socket socket;
 	protected ArrayList<IConnectionListener> connectionListeners;
 	protected boolean connected;
 	protected Reader reader;
 	protected String host = null;
 	protected int port = -1;
 	
 	public Connection (String host, int port)
 		throws IOException
 	{
 		this();
 		connect (host, port);
 	}
 	
 	public Connection ()
 	{
 		connected = false;
 		connectionListeners = new ArrayList<IConnectionListener>();
 	}
 	
 	public void connect (String host, int port)
 		throws IOException
 	{
 		try {
 			socket = new Socket(host, port);
 			reader = createReader(socket);
 			
 			new Thread(createPollingRunnable()).start();
 		} catch (IOException e) {
 			if (e instanceof ConnectException && e.getMessage().contains("refused")) {
 				connectionError(ErrorType.ConnectionRefused);
 			} else if (e instanceof UnknownHostException) {
 				connectionError(ErrorType.UnknownHost);
 			} else throw e;
 		}
 	}
 	
 	protected Reader createReader (Socket socket)
 		throws IOException
 	{
 		return new InputStreamReader(socket.getInputStream());
 	}
 	
 	protected Runnable createPollingRunnable () {
 		return new EventPollThread();
 	}
 	
 	protected void connectionError (ErrorType errorType)
 	{
 		for (IConnectionListener listener : connectionListeners)
 		{
 			listener.connectionError(this, errorType);
 		}
 	}
 	
 	public boolean isConnected() {
 		return connected;
 	}
 	
 	public String getHost() {
 		return host;
 	}
 	
 	public int getPort() {
 		return port;
 	}
 	
 	public void disconnect()
 		throws IOException
 	{
 		socket.shutdownOutput();
 	}
 	
 	public void addConnectionListener (IConnectionListener listener)
 	{
 		connectionListeners.add(listener);
 	}
 	
 	public void send (String toSend)
 		throws IOException
 	{
 		send(toSend.getBytes());
 	}
 	
 	public void send (byte[] bytes)
 		throws IOException
 	{
 		socket.getOutputStream().write(bytes);
 	}
 	
 	public void sendLine (String line)
 		throws IOException
 	{
 		send (line + "\n");
 	}
 	
 	public IWarlockClient getClient() {
 		return null;
 	}
 	
 	protected class EventPollThread implements Runnable
 	{
 		
 		public void run ()
 		{
 			while (true)
 			{
 				if (!connected) {
 					if (isSocketActive()) {
 						connected = true;
 						listenersConnected();
 					}
 				} else {
 					if (!isSocketActive()) {
 						connected = false;
 						listenersDisconnected();
 						break;
 					} else {
 						try {
 							readData(reader);
 						} catch(Exception e) {
 							e.printStackTrace();
 						}
 					}
 				}
 			}
 		}
 		
		protected void readData (Reader reader) {
			try {
				char cbuf[] = new char[1024];
				
				int charsRead = reader.read(cbuf);
				listenersGotData(cbuf, 0, charsRead);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
 		}
 		
 		private void listenersDisconnected ()
 		{
 			for (IConnectionListener listener : connectionListeners) {
 				listener.disconnected(Connection.this);
 			}
 		}
 		
 		private void listenersConnected ()
 		{
 			for (IConnectionListener listener : connectionListeners) {
 				listener.connected(Connection.this);
 			}
 		}
 		
 		private void listenersGotData (char[] cbuf, int start, int length)
 		{
 			for (IConnectionListener listener : connectionListeners) {
 				listener.dataReady(Connection.this, cbuf, start, length);
 			}
 		}
 		
 		private boolean isSocketActive() {
 			if (!socket.isConnected()) return false;
 			if (socket.isClosed()) return false;
 			if (socket.isInputShutdown()) return false;
 			if (socket.isOutputShutdown()) return false;
 			
 			return true; // If we reach here... we're most likely still connected
 		}
 	}
 }
