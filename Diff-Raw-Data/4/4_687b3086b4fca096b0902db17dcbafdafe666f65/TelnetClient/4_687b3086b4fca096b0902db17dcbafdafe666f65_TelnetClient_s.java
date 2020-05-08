 package com.sf.jintn3270.telnet;
 
 import com.sf.jintn3270.TerminalModel;
 import com.sf.jintn3270.DefaultTerminalModel;
 
 import java.net.InetSocketAddress;
 
 import java.io.IOException;
 import java.io.ByteArrayOutputStream;
 import java.io.BufferedInputStream;
 
 import java.net.Socket;
 import java.net.UnknownHostException;
 import javax.net.ssl.SSLSocketFactory;
 
 import java.nio.ByteBuffer;
 
 import java.util.AbstractQueue;
 import java.util.concurrent.ConcurrentLinkedQueue;
 
 /**
  * TelnetClient is a Runnable that implements all I/O for Telnet functions.
  * 
  * Options can be added to the TelnetClient. Options which are successfully 
  * negotiated with the remote host end up enabled. 
  * 
  * Options allow for injecting bytes to be sent, and handling bytes that have
  * been received. Doing this allows us to create Options which maintain their
  * own state and implement the behavior necessary for the Option to function.
  *
  * This class, as well as all Options implement a simple mechanism for making
  * sure all the data necessary to carry out a task is available at the time
  * the consumeIncommingBytes method is called. If no one consumes any bytes 
  * (everything returns 'zero') 
  */
 public class TelnetClient extends Thread implements TelnetConstants {
 	String host;
 	int port;
 	boolean ssl;
 	
 	Socket sock = null;
 	UByteInputStream inStream;
 	
 	UByteOutputStream outWriter;
 	ByteArrayOutputStream outStream;
 	
 	AbstractQueue<Option> options;
 	
 	TerminalModel model;
 	
 	/**
 	 * Create a TelnetClient with a DefaultTerminalModel that connects to the
 	 * given host and port, with the given ssl setting
 	 *
 	 * @param host The hostname / IP to connect to.
 	 * @param port The port to connect to (default for TELNET is 23)
 	 * @param ssl If <code>true</code> An SSLSocketFactory is used to create
 	 *        the socket. Otherwise, a normal client Socket is used.
 	 */
 	public TelnetClient(String host, int port, boolean ssl) {
 		this(host, port, ssl, new DefaultTerminalModel());
 	}
 	
 	
 	/**
 	 * Create a TelnetClient that writes to the given TerminalModel,
 	 * connects to the given host and port, with the given ssl setting
 	 *
 	 * @param host The hostname / IP to connect to.
 	 * @param port The port to connect to (default for TELNET is 23)
 	 * @param ssl If <code>true</code> An SSLSocketFactory is used to create
 	 *        the socket. Otherwise, a normal client Socket is used.
 	 * @param model The TerminalModel to write incoming data to.
 	 */
 	public TelnetClient(String host, int port, boolean ssl, TerminalModel model) {
 		this.model = model;
 		this.host = host;
 		this.port = port;
 		this.ssl = ssl;
 		
 		sock = null;
 		
 		options = new ConcurrentLinkedQueue<Option>();
 		for (Option o : model.getRequiredOptions()) {
 			options.add(o);
 		}
 		outStream = new ByteArrayOutputStream();
 		outWriter = new UByteOutputStream(outStream);
 	}
 	
 	
 	public TerminalModel getTerminalModel() {
 		return model;
 	}
 	
 	/**
 	 * You may want to call this from another thread...
 	 */
 	void connect() throws UnknownHostException, IOException {
 		if (isConnected()) {
 			disconnect();
 		}
 		
 		if (!ssl) {
 			sock = new Socket();
 		} else {
 			sock = SSLSocketFactory.getDefault().createSocket();
 		}
 		sock.connect(new InetSocketAddress(host, port));
 		
 		if (sock != null) {
 			sock.setKeepAlive(true);
 			inStream = new UByteInputStream(new BufferedInputStream(sock.getInputStream(), sock.getReceiveBufferSize()));
 			connected();
 		}
 	}
 	
 	/**
 	 * Disconnects from the host.
 	 */
 	public void disconnect() {
 		if (sock != null) {
 			try {
 				sock.close();
 			} catch (IOException ioe) {
 			} finally {
 				sock = null;
 			}
 		}
 		outStream.reset();
 		disconnected();
 	}
 	
 	/**
 	 * Gets the hostname we're connecting to.
 	 * @return the Hostname.
 	 */
 	public String getHost() {
 		return host;
 	}
 	
 	/**
 	 * Gets the port we're connecting to.
 	 * @return the port
 	 */
 	public int getPort() {
 		return port;
 	}
 	
 	/**
 	 * Returns weather or not we're attempting to use SSL.
 	 * @return ssl
 	 */
 	public boolean useSSL() {
 		return ssl;
 	}
 	
 	/**
 	 * Invoked when we're disconnected
 	 */
 	protected void disconnected() {
 		this.model.setClient(null);
 	}
 	
 	/**
 	 * Invoked when we're connected
 	 */
 	public void connected() {
 		this.model.setClient(this);
 		for (Option o : options) {
 			o.initiate(this);
 		}
 	}
 	
 	/**
 	 * Determines if we have an output channel
 	 */
 	public boolean isConnected() {
 		return sock != null;
 	}
 	
 	/**
 	 * Add the option and send a WILL.
 	 */
 	public void addOption(Option o) {
 		options.add(o);
 		if (isConnected()) {
 			o.initiate(this);
 		}
 	}
 	
 	
 	/**
 	 * Remove the option and send a WONT
 	 */
 	public void removeOption(Option o) {
 		if (o.isEnabled()) {
 			sendWont(o.getCode());
 		}
 	}
 	
 	/**
 	 * Send the given byte to the remote host.
 	 */
 	public void send(short b) {
 		// If we're using send to send 255, we need to escape it.
 		if (b == IAC) {
 			outWriter.write(new short[] {IAC, IAC});
 		} else {
 			try {
 				outWriter.write(b);
 			} catch (IOException ioe) {
 				System.err.println(ioe.toString());
 			}
 		}
 	}
 	
 	/**
 	 * Send the outgoing non-command bytes
 	 */
 	public void send(short[] bytes) {
 		for (short b : bytes) {
 			send(b);
 		}
 	}
 	
 	
 	/**
 	 * Sends the outgoing byte preceeded by an IAC marker
 	 */
 	public void sendCommand(short b) {
 		sendCommand(new short[] {b});
 	}
 	
 	/**
 	 * Sends the outgoing bytes preceeded by an IAC marker.
 	 */
 	public void sendCommand(short[] commandBytes) {
 		short[] toSend = new short[commandBytes.length + 1];
 		toSend[0] = IAC;
 		System.arraycopy(commandBytes, 0, toSend, 1, commandBytes.length);
 		outWriter.write(toSend);
 	}
 	
 	/**
 	 * Writes a DO option to the output buffer
 	 */
 	public void sendDo(short code) {
 		outWriter.write(new short[] {IAC, DO, code});
 	}
 	
 	/**
 	 * Writes a WILL option to the output buffer.
 	 */
 	public void sendWill(short code) {
 		outWriter.write(new short[] {IAC, WILL, code});
 	}
 	
 	/**
 	 * Writes a DONT option to the output buffer.
 	 */
 	public void sendDont(short code) {
 		outWriter.write(new short[] {IAC, DONT, code});
 	}
 	
 	/**
 	 * Writes a WONT option to the output buffer.
 	 */
 	public void sendWont(short code) {
 		outWriter.write(new short[] {IAC, WONT, code});
 	}
 	
 	
 	/**
 	 * The read/write loop (I/O thread) passing byte[] buffers to this method,
 	 * In this implementation, we look for IAC, and other TELNET commands to 
 	 * respond to. If we can handle the incoming data, we return the number of 
 	 * bytes we've read & handled. These bytes are then consumed by the input 
 	 * stream after this method is called. If zero bytes are consumed, the 
 	 * data is considered to be an incomplete frame, and will be re-delivered 
 	 * when more data is available.
 	 * 
 	 * @param incoming The bytes coming in from the buffer.
 	 * @return The number of bytes consumed in this pass.
 	 */
 	private int consumeIncoming(short[] incoming) {
 		int read = 0;
 		if (incoming[0] == IAC) {
 			if (incoming.length >= 2) {
 				switch(incoming[1]) {
 					case IAC: // Handle escaped 255. we leave 'read' at 0 so this will be passed along.
 						// Trim the first byte.
 						System.arraycopy(incoming, 1, incoming, 0, incoming.length - 1);
 						incoming[incoming.length - 1] = 0;
 						break;
 					case WILL: // Option Offered! Send do or don't.
 						if (incoming.length >= 3) {
 							boolean dosent = false;
 							for (Option o : options) {
 								if (o.getCode() == incoming[2]) {
 									sendDo(o.getCode());
 									dosent = true;
 									break;
 								}
 							}
 							if (!dosent) {
 								sendDont(incoming[2]);
 							}
 						}
 						read = 3;
 						break;
 					case DO: // Option requested. Send will or wont!
 						if (incoming.length >= 3) {
 							boolean enabled = false;
 							for (Option o : options) {
 								if (o.getCode() == incoming[2]) {
 									o.setEnabled(true, this);
 									enabled = true;
 									break;
 								}
 							}
 							if (enabled) {
 								sendWill(incoming[2]);
 							} else {
 								sendWont(incoming[2]);
 							}
 						}
 						read = 3;
 						break;
 					case DONT: // Handle disable requests.
 					case WONT:
 						if (incoming.length >= 3) {
 							for (Option o : options) {
 								if (o.getCode() == incoming[2]) {
 									o.setEnabled(false, this);
 								}
 							}
 						}
 						read = 3;
 						break;
 					case DM: // Data Mark?
 					case NOP:
 					case BRK:
 					case IP:
 					case AO:
 					case AYT:
 						read = 2;
 						break;
 					case EC: // Erase Character
 						model.eraseChar();
 						read = 2;
 						break;
 					case EL: // Erase Line
 						model.eraseLine();
 						read = 2;
 						break;
 					case GA: // We got a GO-Ahead.
 						read = 2;
 						break;
 					case SB: // Sub-negotiation!
 						if (incoming.length >= 5) { // Must be at least IAC, SB, <code>, IAC, SE
 							for (Option o : options) {
 								if (o.getCode() == incoming[2]) {
 									read = o.consumeIncomingSubcommand(incoming, this);
 									break;
 								}
 							}
 						}
 						break;
 				}
 			}
 		}
 		
 		// If we didn't handle anything, we need to find something that can.
 		if (read == 0) {
 			// For any enabled options, let's try them.
 			for (Option o : options) {
 				if (o.isEnabled()) {
 					read += o.consumeIncoming(incoming, this);
 				}
 			}
 		}
 		
 		// If no options handled the data, then we need to read up to the 
 		// next IAC, or the end of the buffer, and we'll treat that as 
 		// if it's data for display.
 		if (read == 0) {
 			for (short b : incoming) {
 				if (b == IAC) {
 					break;
 				}
 				read++;
 			}
 			model.print(incoming, 0, read);
 		}
 		return read;
 	}
 	
 	
 	/**
 	 * Gathers and combines all the outgoing bytes from all the enabled Options
 	 * and our own internal output buffer for writing to the socket.
 	 */
 	private byte[] outgoingBytes() {
 		// collect all options outgoing bytes.
 		for (Option o : options) {
 			outWriter.write(o.outgoing(outStream, this));
 		}
 		
 		// Convert to byte[] array for writing.
 		byte[] out = outStream.toByteArray();
 		outStream.reset();
 		
 		return out;
 	}
 	
 	
 	/**
 	 * The main I/O loop. As long as the socket is not null, and not closed, 
 	 * we continue.
 	 */
 	public void run() {
 		byte[] out;
 		short[] in = new short[0];
 		int read;
 		int consumed;
 		
 		try {
 			connect();
 			in = new short[sock.getReceiveBufferSize()];
 		} catch (Exception ex) {
 			ex.printStackTrace();
			disconnected();
 		}
 		
  		while (sock != null && !sock.isClosed()) {
 			try {
 				consumed = 0;
 				
 				// Read up to in.length before we're invalid...
 				inStream.mark(in.length);
 				
 				try {
 					read = inStream.read(in);
 				} catch (IOException ioe) {
 					read = 0;
 				}
 				
 				// Truncate the read buffer, and process it.
 				if (read > 0) {
 					short[] truncIn = new short[read];
 					System.arraycopy(in, 0, truncIn, 0, read);
 					
 					consumed = consumeIncoming(truncIn);
 				}
 				
 				// reset the mark, and then consume the bytes we've 
 				// processed.
 				inStream.reset();
 				if (consumed > 0) {
 					inStream.skip(consumed);
 				}
 				
 				
 				// Do I have data to write?
 				out = outgoingBytes();
 				if (out.length > 0) {
 					sock.getOutputStream().write(out);
 					sock.getOutputStream().flush();
 				}
 			} catch (Exception ex) {
 				ex.printStackTrace();
 				System.err.println(ex.toString());
 			}
 			// Play nice with thread schedulers.
 			Thread.yield();
 		}
 	}
 	
 	/**
 	 * Simple Test Harness
 	 */
 	public static void main(String[] args) {
 		TelnetClient client = new TelnetClient(args[0], Integer.parseInt(args[1]), Boolean.valueOf(args[2]).booleanValue());
 		client.addOption(new SuppressGA());
 		client.addOption(new Echo());
 		client.addOption(new EndOfRecord());
 		//client.addOption(new TerminalType());
 		client.start();
 	}
 }
