 package com.mymed.controller.core.manager.connection;
 
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 
 import org.apache.cassandra.thrift.Cassandra.Client;
 import org.apache.cassandra.thrift.TBinaryProtocol;
 import org.apache.thrift.protocol.TProtocol;
 import org.apache.thrift.transport.TFramedTransport;
 import org.apache.thrift.transport.TSocket;
 import org.apache.thrift.transport.TTransportException;
 
 import com.mymed.controller.core.exception.InternalBackEndException;
 import com.mymed.utils.MLogger;
 
 /**
  * Class that represents a connection to a Cassandra instance in mymed
  * 
  * @author Milo Casagrande
  * 
  */
 public class Connection implements IConnection {
 
 	// Used for the hash code
 	private static final int PRIME = 31;
 
 	// The default port to use if nothing is passed as arguments
 	private static final int DEFAULT_PORT = 9160;
 
 	// The port and the address associated with this connection
 	private String address = null;
 	private int port = 0;
 
 	// Socket and transport used for the connection
 	private final TSocket socket;
 	private final TFramedTransport transport;
 	private final Client cassandra;
 
 	/**
 	 * Create a new connection using as address the localhost address and the
 	 * default value for the port (9160).
 	 */
 	public Connection() {
 		this(null, 0);
 	}
 
 	/**
 	 * Create a new connection with the provided address and port
 	 * 
 	 * @param address
 	 *            the IP address to be associated with this connection
 	 * @param port
 	 *            the port to use with this connection
 	 * @throws Exception
 	 */
 	public Connection(final String address, final int port) {
 		if (address == null && (port == 0 || port < 0)) {
 			try {
 				this.address = InetAddress.getLocalHost().getHostAddress();
 				this.port = DEFAULT_PORT;
 			} catch (final UnknownHostException ex) {
 				MLogger.getDebugLog().debug("Error recovering local host address", ex.getCause());
 			}
 		} else {
 			this.address = address;
 			this.port = port;
 		}
 
 		MLogger.getDebugLog().debug("Connection set to {}:{}", address, port);
 
 		socket = new TSocket(this.address, this.port);
 		transport = new TFramedTransport(socket);
		final TProtocol protocol = new TBinaryProtocol(transport);

 		cassandra = new Client(protocol);
 	}
 
 	/**
 	 * @return the Cassandra instance to use
 	 */
 	public Client getClient() {
 		return cassandra;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * com.mymed.controller.core.manager.connection.IConnection#getAddress()
 	 */
 	@Override
 	public String getAddress() {
 		return address;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.mymed.controller.core.manager.connection.IConnection#getPort()
 	 */
 	@Override
 	public int getPort() {
 		return port;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.mymed.controller.core.manager.connection.IConnection#isOpen()
 	 */
 	@Override
 	public boolean isOpen() {
 		return transport.isOpen();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.mymed.controller.core.manager.connection.IConnection#open()
 	 */
 	@Override
 	public void open() throws InternalBackEndException {
 		try {
 			if (!transport.isOpen()) {
 				transport.open();
 			}
 		} catch (final TTransportException ex) {
			throw new InternalBackEndException("Error opening the connection to " + address + ":" + port, ex.getCause()); // NOPMD
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.mymed.controller.core.manager.connection.IConnection#close()
 	 */
 	@Override
 	public void close() {
 		if (transport.isOpen()) {
 			transport.close();
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see java.lang.Object#hashCode()
 	 */
 	@Override
 	public int hashCode() {
 		int result = 1;
 		result = PRIME * result + (address == null ? 0 : address.hashCode());
 		result = PRIME * result + port;
 		result = PRIME * result + (socket == null ? 0 : socket.hashCode());
 		return result;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see java.lang.Object#equals(java.lang.Object)
 	 */
 	@Override
 	public boolean equals(final Object object) {
 
 		boolean equal = false;
 
 		if (this == object) {
 			equal = true;
 		} else if (object instanceof IConnection) {
 			final IConnection comparable = (Connection) object;
 			equal = true;
 
 			if (address == null && comparable.getAddress() != null) {
 				equal &= false;
 			} else {
 				equal &= address.equals(comparable.getAddress());
 			}
 
 			equal &= port == comparable.getPort();
 		}
 
 		return equal;
 	}
 }
