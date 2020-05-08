 /*******************************************************************************
  * Copyright (c) 2004 Composent, Inc. and others. All rights reserved. This
  * program and the accompanying materials are made available under the terms of
  * the Eclipse Public License v1.0 which accompanies this distribution, and is
  * available at http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors: Composent, Inc. - initial API and implementation
  ******************************************************************************/
 package org.eclipse.ecf.provider.jms.channel;
 
 import java.io.IOException;
 import java.util.Map;
 
 import org.activemq.transport.TransportChannel;
 import org.eclipse.ecf.core.identity.ID;
 import org.eclipse.ecf.core.util.ECFException;
 import org.eclipse.ecf.provider.comm.IConnectionListener;
 import org.eclipse.ecf.provider.comm.ISynchAsynchConnection;
 import org.eclipse.ecf.provider.jms.container.JMSServerSOContainer;
 
 /**
  * @author slewis
  * 
  */
 public class ClientChannelProxy implements ISynchAsynchConnection {
 
 	JMSServerSOContainer container;
 
 	ISynchAsynchConnection aconn;
 
 	ID remoteID;
 
 	TransportChannel channel;
 
 	boolean started;
 
 	public ClientChannelProxy(JMSServerSOContainer container,
 			ISynchAsynchConnection aconn, ID remote) {
 		super();
 		this.container = container;
 		this.aconn = aconn;
 		this.remoteID = remote;
 	}
 
 	public void start() {
 		started = true;
 	}
 
 	public void sendAsynch(ID receiver, byte[] data) throws IOException {
 		if (!isConnected() || !isStarted()) {
 			throw new IOException("connection not started");
 		}
 		aconn.sendAsynch(receiver, data);
 	}
 
 	public Object connect(ID remote, Object data, int timeout)
 			throws ECFException {
 		throw new ECFException("cannot connect via this proxy");
 	}
 
	public void disconnect() throws IOException {
 		stop();
 		aconn = null;
 	}
 
 	public boolean isConnected() {
 		return aconn.isConnected();
 	}
 
 	public ID getLocalID() {
 		return aconn.getLocalID();
 	}
 
 	public void stop() {
 		started = false;
 	}
 
 	public boolean isStarted() {
 		return started;
 	}
 
 	public Map getProperties() {
 		return null;
 	}
 
 	public void addListener(IConnectionListener listener) {
 	}
 
 	public void removeListener(IConnectionListener listener) {
 	}
 
 	public Object getAdapter(Class clazz) {
 		return null;
 	}
 
 	public Object sendSynch(ID receiver, byte[] data) throws IOException {
 		if (!isConnected() || !isStarted()) {
 			throw new IOException("proxy connection not started");
 		}
 		return aconn.sendSynch(receiver, data);
 	}
 }
