 /**
  * Copyright (c) 2009, Coral Reef Project
  * All rights reserved.
  * 
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  * 
  *  * Redistributions of source code must retain the above copyright notice,
  *    this list of conditions and the following disclaimer.
  *  * Redistributions in binary form must reproduce the above copyright notice,
  *    this list of conditions and the following disclaimer in the documentation
  *    and/or other materials provided with the distribution.
  *  * Neither the name of the Coral Reef Project nor the names of its
  *    contributors may be used to endorse or promote products derived from this
  *    software without specific prior written permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
  * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
  * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
  * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
  * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
  * POSSIBILITY OF SUCH DAMAGE.
  */
 package pl.graniec.coralreef.network.stream;
 
 import java.io.BufferedOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.NotSerializableException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.io.OutputStream;
 import java.net.Socket;
 import java.util.LinkedList;
 import java.util.List;
 
 import pl.graniec.coralreef.network.PacketListener;
 import pl.graniec.coralreef.network.server.RemoteClient;
 import pl.graniec.coralreef.network.server.Server;
 
 /**
  * @author Piotr Korzuszek <piotr.korzuszek@gmail.com>
  *
  */
 public class StreamRemoteClient implements RemoteClient {
 
 	private class Listener extends Thread {
 		private static final int SO_TIMEOUT = 100;
 
 		/*
 		 * @see java.lang.Thread#run()
 		 */
 		@Override
 		public void run() {
 
 			InputStream is;
 			ObjectInputStream ois;
 			
 			try {
 				// socket configuration
 				socket.setSoTimeout(SO_TIMEOUT);
 				
 				// get input stream or report disconnection
 				is = socket.getInputStream();
 				ois = new ObjectInputStream(is);
 				
 			} catch (IOException e) {
 				// synchronization will guarantee right order of notifying about connection
 				// and disconnection
 				synchronized (parent.remoteClients) {
					notifyClientDisconnected(Server.REASON_CONNECTION_RESET, e.getMessage());
 					return;
 				}
 			}
 			
 			while (!isInterrupted()) {
 				
 				//FIXME: Finish me!
 				
 			}
 		}
 	}
 
 	/** Parent Server */
 	private final StreamServer parent;
 	/** Socket of this client */
 	private final Socket socket;
 	
 	/** Output */
 	ObjectOutputStream oos;
 	/** Input */
 	ObjectInputStream ois;
 	
 	/** Packet listeners */
 	private final List<PacketListener> packetListeners = new LinkedList<PacketListener>();
 
 	public StreamRemoteClient(StreamServer parent, Socket socket) {
 		this.parent = parent;
 		this.socket = socket;
 		
 		// get proper output and input stream
 		
 		try {
 			final OutputStream os = socket.getOutputStream();
 			final BufferedOutputStream bos = new BufferedOutputStream(os);
 			oos = new ObjectOutputStream(bos);
 			
 			
 		} catch (IOException e) {
 			// TODO Sprawdzić co może wyrzucić
 			e.printStackTrace();
 		}
 	}
 	
 	/*
 	 * @see pl.graniec.coralreef.network.server.RemoteClient#addPacketListener(pl.graniec.coralreef.network.PacketListener)
 	 */
 	@Override
 	public boolean addPacketListener(PacketListener l) {
 		
 		if (l == null) {
 			throw new IllegalArgumentException("given object cannot be null");
 		}
 		
 		synchronized (packetListeners) {
 			return packetListeners.add(l);
 		}
 	}
 
 	/*
 	 * @see pl.graniec.coralreef.network.server.RemoteClient#disconnect()
 	 */
 	@Override
 	public void disconnect() {
 		try {
 			socket.close();
 		} catch (IOException e) {
 			// TODO: Sprawdzić kiedy występuje ten wyjątek
 			e.printStackTrace();
 		}
 	}
 
 	/*
 	 * @see pl.graniec.coralreef.network.server.RemoteClient#isConnected()
 	 */
 	@Override
 	public boolean isConnected() {
 		return socket.isConnected();
 	}
 
 	/*
 	 * @see pl.graniec.coralreef.network.server.RemoteClient#removePacketListener(pl.graniec.coralreef.network.PacketListener)
 	 */
 	@Override
 	public boolean removePacketListener(PacketListener l) {
 		
 		if (l == null) {
 			throw new IllegalArgumentException("given object cannot be null");
 		}
 		
 		synchronized (packetListeners) {
 			return packetListeners.remove(l);
 		}
 	}
 
 	/*
 	 * @see pl.graniec.coralreef.network.server.RemoteClient#send(java.lang.Object)
 	 */
 	@Override
 	public void send(Object data) throws NotSerializableException {
 		try {
 			oos.writeObject(data);
 			oos.flush();
 		} catch (NotSerializableException e) {
 			throw e;
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
	private void notifyClientDisconnected(int reason, String reasonStr) {
 		
 	}
 
 }
