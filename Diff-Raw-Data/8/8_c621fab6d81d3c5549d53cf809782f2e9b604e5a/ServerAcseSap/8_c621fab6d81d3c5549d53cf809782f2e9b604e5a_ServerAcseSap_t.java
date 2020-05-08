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
 package org.openmuc.openiec61850.internal.acse;
 
 import java.io.IOException;
 import java.net.InetAddress;
 import java.nio.ByteBuffer;
 import java.util.concurrent.TimeoutException;
 
 import javax.net.ServerSocketFactory;
 
 import org.openmuc.openiec61850.jositransport.ServerTSap;
 import org.openmuc.openiec61850.jositransport.TConnection;
 import org.openmuc.openiec61850.jositransport.TConnectionListener;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * This class implements the server Service Access Point (SAP) for the Application Control Service Element (ACSE)
  * protocol as defined by ISO 8650 or ITU X.217/X.227. The ACSE provides services for establishing and releasing
  * application-associations. The class also realizes the lower ISO Presentation Layer as defined by ISO 8823/ITU X226
  * and the ISO Session Layer as defined by 8327/ITU X.225.
  * 
  */
 public final class ServerAcseSap implements TConnectionListener {
 
 	private final static Logger logger = LoggerFactory.getLogger(ServerAcseSap.class);
 
 	private AcseAssociationListener associationListener = null;
 	public ServerTSap serverTSap = null;
 
 	public byte[] pSelLocal = ClientAcseSap.P_SEL_DEFAULT;
 
 	/**
 	 * Use this constructor to create a server ACSE SAP that listens on a fixed port.
 	 * 
 	 * @param associationListener
 	 *            the AssociationListener that will be notified when remote clients have associated. Once constructed
 	 *            the AcseSAP contains a public TSAP that can be accessed to set its configuration.
 	 */
 	public ServerAcseSap(int port, int backlog, InetAddress bindAddr, AcseAssociationListener associationListener) {
 		this(port, backlog, bindAddr, associationListener, ServerSocketFactory.getDefault());
 	}
 
 	/**
 	 * Use this constructor to create a server ACSE SAP that listens on a fixed port.
	 *
 	 * @param associationListener
 	 *            the AssociationListener that will be notified when remote clients have associated. Once constructed
 	 *            the AcseSAP contains a public TSAP that can be accessed to set its configuration.
 	 */
 	public ServerAcseSap(int port, int backlog, InetAddress bindAddr, AcseAssociationListener associationListener,
 			ServerSocketFactory serverSocketFactory) {
 		this.associationListener = associationListener;
		serverTSap = new ServerTSap(port, backlog, bindAddr, this, serverSocketFactory);
 	}
 
 	/**
 	 * Start listening for incoming connections. Only for server SAPs.
	 *
 	 * @throws IOException
 	 */
 	public void startListening() throws IOException {
 		if (associationListener == null || serverTSap == null) {
 			throw new IllegalStateException("AcseSAP is unable to listen because it was not initialized.");
 		}
 		serverTSap.startListening();
 	}
 
 	public void stopListening() {
 		serverTSap.stopListening();
 	}
 
 	/**
 	 * This function is internal and should not be called by users of this class.
 	 */
 	@Override
 	public void serverStoppedListeningIndication(IOException e) {
 		associationListener.serverStoppedListeningIndication(e);
 	}
 
 	/**
 	 * This function is internal and should not be called by users of this class.
 	 * 
 	 */
 	@Override
 	public void connectionIndication(TConnection tConnection) {
 		AcseAssociation acseAssociation = new AcseAssociation(tConnection, pSelLocal);
 
 		ByteBuffer asdu = ByteBuffer.allocate(1000);
 		try {
 			acseAssociation.listenForCn(asdu);
 		} catch (IOException e) {
 			logger.warn("Server: Connection unsuccessful. IOException:" + e.getMessage());
 			tConnection.close();
 			return;
 		} catch (TimeoutException e) {
 			logger.error("Illegal state: Timeout should not occur here");
 			tConnection.close();
 			return;
 		}
 		associationListener.connectionIndication(acseAssociation, asdu);
 	}
 }
