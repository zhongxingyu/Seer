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
 /*
  * Created on Sep 17, 2004
  */
 package cc.warlock.core.network;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.Reader;
 import java.net.Socket;
 
 /**
  * @author marshall
  */
 public class LineConnection extends Connection {
 	
 	protected BufferedReader bufferedReader;
 	
 	@Override
 	protected Reader createReader(Socket socket) throws IOException {
 		bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
 		return bufferedReader;
 	}
 	
 	@Override
 	protected Runnable createPollingRunnable() {
 		return new LineEventPollThread();
 	}
 	
 	protected class LineEventPollThread extends Connection.EventPollThread {
 		@Override
		protected void readData(Reader reader) throws IOException {
			String line = bufferedReader.readLine();
			listenersGotLine(line);
 		}
 		
 		private void listenersGotLine (String line)
 		{
 			for (IConnectionListener listener : connectionListeners) {
 				if (listener instanceof ILineConnectionListener) {
 					((ILineConnectionListener)listener).lineReady(LineConnection.this, line);
 				}
 			}
 		}
 	}
 }
